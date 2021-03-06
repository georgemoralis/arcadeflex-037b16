/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class cabal
{
	
	static struct tilemap *background_layer,*text_layer;
	
	
	public static GetTileInfoPtr get_back_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int tile = videoram16[tile_index];
		int color = (tile>>12)&0xf;
	
		tile &= 0xfff;
	
		SET_TILE_INFO(
				1,
				tile,
				color,
				0)
	} };
	
	public static GetTileInfoPtr get_text_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int tile = colorram16[tile_index];
		int color = (tile>>10);
	
		tile &= 0x3ff;
	
		SET_TILE_INFO(
				0,
				tile,
				color,
				0)
	} };
	
	
	public static VhStartPtr cabal_vh_start = new VhStartPtr() { public int handler() 
	{
		background_layer = tilemap_create(get_back_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,16,16);
		text_layer       = tilemap_create(get_text_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,  8,8,32,32);
	
		if (!text_layer || !background_layer ) return 1;
	
		tilemap_set_transparent_pen(text_layer,3);
		tilemap_set_transparent_pen(background_layer,15);
	
		return 0;
	} };
	
	
	/**************************************************************************/
	
	WRITE16_HANDLER( cabal_flipscreen_w )
	{
		if (ACCESSING_LSB != 0)
		{
			int flip = (data & 0x20) ? (TILEMAP_FLIPX | TILEMAP_FLIPY) : 0;
			tilemap_set_flip(background_layer,flip);
			tilemap_set_flip(text_layer,flip);
	
			flip_screen_set(data & 0x20);
		}
	}
	
	WRITE16_HANDLER( cabal_background_videoram16_w )
	{
		int oldword = videoram16[offset];
		COMBINE_DATA(&videoram16[offset]);
		if (oldword != videoram16[offset])
			tilemap_mark_tile_dirty(background_layer,offset);
	}
	
	WRITE16_HANDLER( cabal_text_videoram16_w )
	{
		int oldword = colorram16[offset];
		COMBINE_DATA(&colorram16[offset]);
		if (oldword != colorram16[offset])
			tilemap_mark_tile_dirty(text_layer,offset);
	}
	
	
	/**************************************************************************/
	
	void cabal_mark_sprite_colours(void)
	{
		UINT16 palette_map[16*4],usage;
		int i,code,color,offs;
	
		memset (palette_map, 0, sizeof (palette_map));
	
		for (offs = 0;offs < (spriteram_size/2);offs += 4)
		{
			if (spriteram16[offs] &0x100)
			{
				code  = (spriteram16[offs+1] &0xfff);
				color = (spriteram16[offs+2] &0x7800 ) >> 11;
				palette_map[color + 0x10] |= Machine.gfx[2].pen_usage[code];
			}
		}
	
		/* expand it */
		for (color = 0; color < 16 * 4; color++)
		{
			usage = palette_map[color];
	
			if (usage != 0)
			{
				for (i = 0; i < 15; i++)
					if (usage & (1 << i))
						palette_used_colors[color * 16 + i] = PALETTE_COLOR_USED;
				palette_used_colors[color * 16 + 15] = PALETTE_COLOR_TRANSPARENT;
			}
		}
	}
	
	
	/********************************************************************
	
		Cabal Spriteram
		---------------
	
		+0   .......x ........  Sprite enable bit
		+0   ........ xxxxxxxx  Sprite Y coordinate
		+1   ..??.... ........  ??? unknown ???
		+1   ....xxxx xxxxxxxx  Sprite tile number
	 	+2   .xxxx... ........  Sprite color bank
		+2   .....x.. ........  Sprite flip x
		+2   .......x xxxxxxxx  Sprite X coordinate
		+3   (unused)
	
	            -------E YYYYYYYY
	            ----BBTT TTTTTTTT
	            -CCCCF-X XXXXXXXX
	            -------- --------
	
	********************************************************************/
	
	static void cabal_draw_sprites( struct osd_bitmap *bitmap )
	{
		int offs,data0,data1,data2;
	
		for( offs = spriteram_size/2 - 4; offs >= 0; offs -= 4 )
		{
			data0 = spriteram16[offs];
			data1 = spriteram16[offs+1];
			data2 = spriteram16[offs+2];
	
			if ((data0 & 0x100) != 0)
			{
				int tile_number = data1 & 0xfff;
				int color   = ( data2 & 0x7800 ) >> 11;
				int sy = ( data0 & 0xff );
				int sx = ( data2 & 0x1ff );
				int flipx = ( data2 & 0x0400 );
				int flipy = 0;
	
				if ( sx>256 )   sx -= 512;
	
				if (flip_screen != 0)
				{
					sx = 240 - sx;
					sy = 240 - sy;
					flipx = !flipx;
					flipy = !flipy;
				}
	
				drawgfx( bitmap,Machine.gfx[2],
					tile_number,
					color,
					flipx,flipy,
					sx,sy,
					&Machine.visible_area,TRANSPARENCY_PEN,0xf );
			}
		}
	}
	
	
	public static VhUpdatePtr cabal_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update(ALL_TILEMAPS);
		palette_init_used_colors();
		cabal_mark_sprite_colours();
		palette_recalc();
	
		tilemap_draw(bitmap,background_layer,TILEMAP_IGNORE_TRANSPARENCY,0);
		cabal_draw_sprites(bitmap);
		tilemap_draw(bitmap,text_layer,0,0);
	} };
	
	
}
