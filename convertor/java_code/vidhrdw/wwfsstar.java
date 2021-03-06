/*******************************************************************************
 WWF Superstars (C) 1989 Technos Japan  (vidhrdw/wwfsstar.c)
********************************************************************************
 driver by David Haywood

 see (drivers/wwfsstar.c) for more notes
*******************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class wwfsstar
{
	
	extern data16_t *fg0_videoram, *bg0_videoram;
	extern int wwfsstar_scrollx, wwfsstar_scrolly;
	static struct tilemap *fg0_tilemap, *bg0_tilemap;
	
	/*******************************************************************************
	 Write Handlers
	********************************************************************************
	 for writes to Video Ram
	*******************************************************************************/
	
	WRITE16_HANDLER( wwfsstar_fg0_videoram_w )
	{
		int oldword = fg0_videoram[offset];
		COMBINE_DATA(&fg0_videoram[offset]);
		if (oldword != fg0_videoram[offset])
			tilemap_mark_tile_dirty(fg0_tilemap,offset/2);
	}
	
	WRITE16_HANDLER( wwfsstar_bg0_videoram_w )
	{
		int oldword =bg0_videoram[offset];
		COMBINE_DATA(&bg0_videoram[offset]);
		if (oldword != bg0_videoram[offset])
			tilemap_mark_tile_dirty(bg0_tilemap,offset/2);
	}
	
	/*******************************************************************************
	 Tilemap Related Functions
	*******************************************************************************/
	
	public static GetTileInfoPtr get_fg0_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		/*- FG0 RAM Format -**
	
		  0x1000 sized region (4096 bytes)
	
		  32x32 tilemap, 4 bytes per tile
	
		  ---- ----  CCCC TTTT  ---- ----  TTTT TTTT
	
		  C = Colour Bank (0-15)
		  T = Tile Number (0 - 4095)
	
		  other bits unknown / unused
	
		**- End of Comments -*/
	
		data16_t *tilebase;
		int tileno;
		int colbank;
		tilebase =  &fg0_videoram[tile_index*2];
		tileno =  (tilebase[1] & 0x00ff) | ((tilebase[0] & 0x000f) << 8);
		colbank = (tilebase[0] & 0x00f0) >> 4;
		SET_TILE_INFO(
				0,
				tileno,
				colbank,
				0)
	} };
	
	static UINT32 bg0_scan(UINT32 col,UINT32 row,UINT32 num_cols,UINT32 num_rows)
	{
		return (col & 0x0f) + ((row & 0x0f) << 4) + ((col & 0x10) << 4) + ((row & 0x10) << 5);
	}
	
	public static GetTileInfoPtr get_bg0_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		/*- BG0 RAM Format -**
	
		  0x1000 sized region (4096 bytes)
	
		  32x32 tilemap, 4 bytes per tile
	
		  ---- ----  FCCC TTTT  ---- ----  TTTT TTTT
	
		  C = Colour Bank (0-7)
		  T = Tile Number (0 - 4095)
		  F = FlipX
	
		  other bits unknown / unused
	
		**- End of Comments -*/
	
		data16_t *tilebase;
		int tileno, colbank, flipx;
		tilebase =  &bg0_videoram[tile_index*2];
		tileno =  (tilebase[1] & 0x00ff) | ((tilebase[0] & 0x000f) << 8);
		colbank = (tilebase[0] & 0x0070) >> 4;
		flipx   = (tilebase[0] & 0x0080) >> 7;
		SET_TILE_INFO(
				2,
				tileno,
				colbank,
				flipx ? TILE_FLIPX : 0)
	} };
	
	/*******************************************************************************
	 Sprite Related Functions
	********************************************************************************
	 sprite colour marking could probably be improved..
	*******************************************************************************/
	
	static void wwfsstar_sprites_mark_colors(void)
	{
		/* this is very crude */
		int i;
		data16_t *source = spriteram16;
		data16_t *finish = source + 0x3ff/2;
	
		while( source<finish )
		{
			int colourbank;
			colourbank = (source [1] & 0x00f0) >> 4;
			for (i = 0;i < 16;i++)
				palette_used_colors[128 + 16 * colourbank + i] = PALETTE_COLOR_USED;
	
			source += 5;
		}
	}
	
	static void wwfsstar_drawsprites( struct osd_bitmap *bitmap )
	{
		/*- SPR RAM Format -**
	
		  0x3FF sized region (1024 bytes)
	
		  10 bytes per sprite
	
		  ---- ---- yyyy yyyy ---- ---- CCCC XYLE ---- ---- fFNN NNNN ---- ---- nnnn nnnn ---- ---- xxxx xxxx
	
		  Yy = sprite Y Position
		  Xx = sprite X Position
		  C  = colour bank
		  f  = flip Y
		  F  = flip X
		  L  = chain sprite (32x16)
		  E  = sprite enable
		  Nn = Sprite Number
	
		  other bits unused
	
		**- End of Comments -*/
	
		const struct rectangle *clip = &Machine.visible_area;
		const struct GfxElement *gfx = Machine.gfx[1];
		data16_t *source = spriteram16;
		data16_t *finish = source + 0x3ff/2;
	
		while( source<finish )
		{
			int xpos, ypos, colourbank, flipx, flipy, chain, enable, number;
	
			ypos = ((source [0] & 0x00ff) | ((source [1] & 0x0004) << 6) );
		    ypos=(((256-ypos)&0x1ff)-16) ;
			xpos = ((source [4] & 0x00ff) | ((source [1] & 0x0008) << 5) );
			xpos = (((256-xpos)&0x1ff)-16);
			colourbank = (source [1] & 0x00f0) >> 4;
			flipx = (source [2] & 0x0080 ) >> 7;
			flipy = (source [2] & 0x0040 ) >> 6;
			chain = (source [1] & 0x0002 ) >> 1;
			enable = (source [1] & 0x0001);
			number = (source [3] & 0x00ff) | ((source [2] & 0x003f) << 8);
	
			 if (enable != 0) {
				if (chain != 0){
					drawgfx(bitmap,gfx,number,colourbank,flipx,flipy,xpos,ypos-16,clip,TRANSPARENCY_PEN,0);
					drawgfx(bitmap,gfx,number+1,colourbank,flipx,flipy,xpos,ypos,clip,TRANSPARENCY_PEN,0);
				} else {
					drawgfx(bitmap,gfx,number,colourbank,flipx,flipy,xpos,ypos,clip,TRANSPARENCY_PEN,0);
				}
			}
	
		source+=5;
		}
	}
	
	/*******************************************************************************
	 Video Start and Refresh Functions
	********************************************************************************
	 Drawing Order is simple
	 BG0 - Back
	 SPR - Middle
	 FG0 - Front
	*******************************************************************************/
	
	
	public static VhStartPtr wwfsstar_vh_start = new VhStartPtr() { public int handler() 
	{
		fg0_tilemap = tilemap_create(get_fg0_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT, 8, 8,32,32);
		tilemap_set_transparent_pen(fg0_tilemap,0);
	
		bg0_tilemap = tilemap_create(get_bg0_tile_info,bg0_scan,TILEMAP_OPAQUE, 16, 16,32,32);
		tilemap_set_transparent_pen(fg0_tilemap,0);
	
		if (!fg0_tilemap || !bg0_tilemap)
			return 1;
	
		return 0;
	} };
	
	public static VhUpdatePtr wwfsstar_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_set_scrolly( bg0_tilemap, 0, wwfsstar_scrolly  );
		tilemap_set_scrollx( bg0_tilemap, 0, wwfsstar_scrollx  );
	
		tilemap_update(ALL_TILEMAPS);
	
		palette_init_used_colors();
	
		wwfsstar_sprites_mark_colors();
		palette_recalc();
	
		tilemap_draw(bitmap,bg0_tilemap,0,0);
		wwfsstar_drawsprites( bitmap );
		tilemap_draw(bitmap,fg0_tilemap,0,0);
	} };
}
