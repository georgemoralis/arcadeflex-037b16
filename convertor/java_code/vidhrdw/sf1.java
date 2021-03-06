/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class sf1
{
	
	
	data16_t *sf1_objectram,*sf1_videoram;
	
	static int sf1_active = 0;
	
	static struct tilemap *bg_tilemap, *fg_tilemap, *tx_tilemap;
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoPtr get_bg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		UBytePtr base = memory_region(REGION_GFX5) + 2*tile_index;
		int attr = base[0x10000];
		int color = base[0];
		int code = (base[0x10000+1]<<8) | base[1];
		SET_TILE_INFO(
				0,
				code,
				color,
				TILE_FLIPYX(attr & 3))
	} };
	
	public static GetTileInfoPtr get_fg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		UBytePtr base = memory_region(REGION_GFX5) + 0x20000 + 2*tile_index;
		int attr = base[0x10000];
		int color = base[0];
		int code = (base[0x10000+1]<<8) | base[1];
		SET_TILE_INFO(
				1,
				code,
				color,
				TILE_FLIPYX(attr & 3))
	} };
	
	public static GetTileInfoPtr get_tx_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int code = sf1_videoram[tile_index];
		SET_TILE_INFO(
				3,
				code & 0x3ff,
				code>>12,
				TILE_FLIPYX((code & 0xc00)>>10))
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr sf1_vh_start = new VhStartPtr() { public int handler() 
	{
		int i;
	
		bg_tilemap =  tilemap_create(get_bg_tile_info, tilemap_scan_cols,TILEMAP_OPAQUE,     16,16,2048,16);
		fg_tilemap =  tilemap_create(get_fg_tile_info, tilemap_scan_cols,TILEMAP_TRANSPARENT,16,16,2048,16);
		tx_tilemap = tilemap_create(get_tx_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT, 8, 8,  64,32);
	
		if (!bg_tilemap || !fg_tilemap || !tx_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap,15);
		tilemap_set_transparent_pen(tx_tilemap,3);
	
		for(i = 832; i<1024; i++)
			palette_used_colors[i] = PALETTE_COLOR_UNUSED;
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	WRITE16_HANDLER( sf1_videoram_w )
	{
		int oldword = sf1_videoram[offset];
		COMBINE_DATA(&sf1_videoram[offset]);
		if (oldword != sf1_videoram[offset])
			tilemap_mark_tile_dirty(tx_tilemap,offset);
	}
	
	WRITE16_HANDLER( sf1_bg_scroll_w )
	{
		static data16_t scroll;
		COMBINE_DATA(&scroll);
		tilemap_set_scrollx(bg_tilemap,0,scroll);
	}
	
	WRITE16_HANDLER( sf1_fg_scroll_w )
	{
		static data16_t scroll;
		COMBINE_DATA(&scroll);
		tilemap_set_scrollx(fg_tilemap,0,scroll);
	}
	
	WRITE16_HANDLER( sf1_gfxctrl_w )
	{
	/* b0 = reset, or maybe "set anyway" */
	/* b1 = pulsed when control6.b6==0 until it's 1 */
	/* b2 = active when dip 8 (flip) on */
	/* b3 = active character plane */
	/* b4 = unused */
	/* b5 = active background plane */
	/* b6 = active middle plane */
	/* b7 = active sprites */
		if (ACCESSING_LSB != 0)
		{
			sf1_active = data & 0xff;
			flip_screen_set(data & 0x04);
			tilemap_set_enable(tx_tilemap,data & 0x08);
			tilemap_set_enable(bg_tilemap,data & 0x20);
			tilemap_set_enable(fg_tilemap,data & 0x40);
		}
	}
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void mark_sprite_colors(void)
	{
		unsigned int cmap = 0;
		int offs,i,j;
	
		for (offs = 0x1000-0x20;offs >= 0;offs -= 0x20)
		{
			int color = sf1_objectram[offs+1] & 0x0f;
	
			cmap |= 1 << color;
		}
	
		for (i = 0;i < 16;i++)
		{
			if (cmap & (1 << i))
			{
				UBytePtr umap =
						&palette_used_colors[Machine.drv.gfxdecodeinfo[2].color_codes_start + 16*i];
	
				for (j = 0;j < 15;j++)
					*umap++ = PALETTE_COLOR_USED;
			}
		}
	}
	
	INLINE int sf1_invert(int nb)
	{
		static int delta[4] = {0x00, 0x18, 0x18, 0x00};
		return nb ^ delta[(nb >> 3) & 3];
	}
	
	static void draw_sprites(struct osd_bitmap *bitmap)
	{
		int offs;
	
		for (offs = 0x1000-0x20;offs >= 0;offs -= 0x20)
		{
			int c = sf1_objectram[offs];
			int attr = sf1_objectram[offs+1];
			int sy = sf1_objectram[offs+2];
			int sx = sf1_objectram[offs+3];
			int color = attr & 0x000f;
			int flipx = attr & 0x0100;
			int flipy = attr & 0x0200;
	
			if ((attr & 0x400) != 0)	/* large sprite */
			{
				int c1,c2,c3,c4,t;
	
				if (flip_screen != 0)
				{
					sx = 480 - sx;
					sy = 224 - sy;
					flipx = !flipx;
					flipy = !flipy;
				}
	
				c1 = c;
				c2 = c+1;
				c3 = c+16;
				c4 = c+17;
	
				if (flipx != 0)
				{
					t = c1; c1 = c2; c2 = t;
					t = c3; c3 = c4; c4 = t;
				}
				if (flipy != 0)
				{
					t = c1; c1 = c3; c3 = t;
					t = c2; c2 = c4; c4 = t;
				}
	
				drawgfx(bitmap,
						Machine.gfx[2],
						sf1_invert(c1),
						color,
						flipx,flipy,
						sx,sy,
						&Machine.visible_area, TRANSPARENCY_PEN, 15);
				drawgfx(bitmap,
						Machine.gfx[2],
						sf1_invert(c2),
						color,
						flipx,flipy,
						sx+16,sy,
						&Machine.visible_area, TRANSPARENCY_PEN, 15);
				drawgfx(bitmap,
						Machine.gfx[2],
						sf1_invert(c3),
						color,
						flipx,flipy,
						sx,sy+16,
						&Machine.visible_area, TRANSPARENCY_PEN, 15);
				drawgfx(bitmap,
						Machine.gfx[2],
						sf1_invert(c4),
						color,
						flipx,flipy,
						sx+16,sy+16,
						&Machine.visible_area, TRANSPARENCY_PEN, 15);
			}
			else
			{
				if (flip_screen != 0)
				{
					sx = 496 - sx;
					sy = 240 - sy;
					flipx = !flipx;
					flipy = !flipy;
				}
	
				drawgfx(bitmap,
						Machine.gfx[2],
						sf1_invert(c),
						color,
						flipx,flipy,
						sx,sy,
						&Machine.visible_area, TRANSPARENCY_PEN, 15);
			}
		}
	}
	
	
	public static VhUpdatePtr sf1_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update(ALL_TILEMAPS);
	
		palette_init_used_colors();
		if ((sf1_active & 0x80) != 0)
			mark_sprite_colors();
		palette_recalc();
	
		if ((sf1_active & 0x20) != 0)
			tilemap_draw(bitmap,bg_tilemap,0,0);
		else
			fillbitmap(bitmap,palette_transparent_pen,&Machine.visible_area);
	
		tilemap_draw(bitmap,fg_tilemap,0,0);
	
		if ((sf1_active & 0x80) != 0)
			draw_sprites(bitmap);
	
		tilemap_draw(bitmap,tx_tilemap,0,0);
	} };
}
