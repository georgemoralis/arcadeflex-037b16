/***************************************************************************

							-= Psikyo Games =-

				driver by	Luca Elia (l.elia@tin.it)


Note:	if MAME_DEBUG is defined, pressing Z with:

		Q			shows layer 0
		W			shows layer 1
		A			shows the sprites

		Keys can be used togheter!


							[ 2 Scrolling Layers ]

		- Dynamic Size
		- Line Scroll

		Layer Sizes:			 512 x 2048 ($20 x $80 tiles)
								1024 x 1048 ($40 x $40 tiles)
		Tiles:					16x16x4
		Color Codes:			8


					[ ~ $300 Multi-Tile Sprites With Zoom ]


		Each sprite is made of 16x16 tiles, up to 8x8 tiles.

		There are $300 sprites, followed by a list of the indexes
		of the sprites to actually display ($400 max). The list is
		terminated by the special index value FFFF.

		The tile code specified for a sprite is actually fed to a
		ROM holding a look-up table with the real tile code to display.

		Sprites can be shrinked up to ~50% following a nearly logaritmic
		curve of sizes.


**************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class psikyo
{
	
	
	/* Variables that driver has access to: */
	
	data32_t *psikyo_vram_0, *psikyo_vram_1, *psikyo_vregs;
	
	
	/* Variables only used here: */
	
	static struct tilemap *tilemap_0, *tilemap_1;
	
	
	/***************************************************************************
	
							Callbacks for the TileMap code
	
								  [ Tiles Format ]
	
	Offset:
	
	0000.w			fed- ---- ---- ----		Color
					---c ba98 7654 3210		Code
	
	***************************************************************************/
	
	public static GetTileInfoPtr get_tile_info_0 = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		data16_t code = ((data16_t *)psikyo_vram_0)[BYTE_XOR_BE(tile_index)];
		SET_TILE_INFO(
				1,
				(code & 0x1fff),
				(code >> 13) & 7,
				0)
	} };
	public static GetTileInfoPtr get_tile_info_1 = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		data16_t code = ((data16_t *)psikyo_vram_1)[BYTE_XOR_BE(tile_index)];
		SET_TILE_INFO(
				2,
				(code & 0x1fff),
				(code >> 13) & 7,
				0)
	} };
	
	
	WRITE32_HANDLER( psikyo_vram_0_w )
	{
		data32_t newlong = psikyo_vram_0[offset];
		data32_t oldlong = COMBINE_DATA(&psikyo_vram_0[offset]);
		if (oldlong == newlong)	return;
		if (ACCESSING_MSW32 != 0)	tilemap_mark_tile_dirty(tilemap_0, offset*2);
		if (ACCESSING_LSW32 != 0)	tilemap_mark_tile_dirty(tilemap_0, offset*2+1);
	}
	
	WRITE32_HANDLER( psikyo_vram_1_w )
	{
		data32_t newlong = psikyo_vram_1[offset];
		data32_t oldlong = COMBINE_DATA(&psikyo_vram_1[offset]);
		if (oldlong == newlong)	return;
		if (ACCESSING_MSW32 != 0)	tilemap_mark_tile_dirty(tilemap_1, offset*2);
		if (ACCESSING_LSW32 != 0)	tilemap_mark_tile_dirty(tilemap_1, offset*2+1);
	}
	
	
	
	
	
	public static VhStartPtr psikyo_vh_start = new VhStartPtr() { public int handler() 
	{
		tilemap_0	=	tilemap_create(	get_tile_info_0,
										tilemap_scan_rows,
										TILEMAP_TRANSPARENT,
										16,16,
										0x20, 0x80 );
	
		tilemap_1	=	tilemap_create(	get_tile_info_1,
										tilemap_scan_rows,
										TILEMAP_TRANSPARENT,
										16,16,
										0x20, 0x80 );
	
		if (tilemap_0 && tilemap_1)
		{
			tilemap_set_scroll_rows(tilemap_0,0x80*16);	// line scrolling
			tilemap_set_scroll_cols(tilemap_0,1);
			tilemap_set_transparent_pen(tilemap_0,15);
	
			tilemap_set_scroll_rows(tilemap_1,0x80*16);	// line scrolling
			tilemap_set_scroll_cols(tilemap_1,1);
			tilemap_set_transparent_pen(tilemap_1,15);
			return 0;
		}
		else return 1;
	} };
	
	
	
	/***************************************************************************
	
									Sprites Drawing
	
	Offset:			Value:
	
	0000/2.w		Y/X + Y/X Size
	
						fedc ---- ---- ----		Zoom Y/X ???
						---- ba9- ---- ----		Tiles along Y/X
						---- ---8 7654 3210		Position
	
	
	0004.w			Color + Flags
	
						f--- ---- ---- ----		Flip Y
						-e-- ---- ---- ----		Flip X
						--d- ---- ---- ----		? USED
						---c ba98 ---- ----		Color
						---- ---- 76-- ----		Priority
						---- ---- --54 321-		-
						---- ---- ---- ---0		Code High Bit
	
	
	0006.w										Code Low Bits
	
					(Code goes into a LUT in ROM where
					 the real tile code is.)
	
	
	Note:	Not all sprites are displayed: in the top part of spriteram
			(e.g. 401800-401fff) there's the list of sprites indexes to
			actually display, terminated by FFFF.
	
			The last entry (e.g. 401ffe) is special and holds some flags:
	
				fedc ba98 7654 ----
				---- ---- ---- 3---		1?
				---- ---- ---- -21-
				---- ---- ---- ---0		Sprites Disable
	
	
	***************************************************************************/
	
	static void psikyo_draw_sprites(struct osd_bitmap *bitmap/*,int priority*/)
	{
		int offs;
	
		data16_t *spritelist	=	(data16_t *)spriteram32_2;
	
		UBytePtr TILES	=	memory_region(REGION_USER1);	// Sprites LUT
		int TILES_LEN			=	memory_region_length(REGION_USER1);
	
		int width	=	Machine.drv.screen_width;
		int height	=	Machine.drv.screen_height;
	
	
		/* Exit if sprites are disabled */
		if ( spritelist[ BYTE_XOR_BE((0x800-2)/2) ] & 1 )	return;
	
		/* Look for "end of sprites" marker in the sprites list */
		for ( offs = 0/2 ; offs < (0x800-2)/2 ; offs += 2/2 )	// skip last "sprite"
		{
			data16_t sprite = spritelist[ BYTE_XOR_BE(offs) ];
			if (sprite == 0xffff)	break;
		}
		offs -= 2/2;
	
		for ( ; offs >= 0/2 ; offs -= 2/2 )
		{
			data32_t *source;
			int	sprite;
	
			int	x,y, attr,code, flipx,flipy, nx,ny, zoomx,zoomy;
			int dx,dy, xstart,ystart, xend,yend, xinc,yinc;
	
			/* From aerofgt.c : */
			/* table hand made by looking at the ship explosion in attract mode */
			/* it's almost a logarithmic scale but not exactly */
			int zoomtable[16] = { 0,7,14,20,25,30,34,38,42,46,49,52,54,57,59,61 };
	
			/* Get next entry in the list */
			sprite	=	spritelist[ BYTE_XOR_BE(offs) ];
	
			sprite	%=	0x300;
			source	=	&spriteram32[ sprite*8/4 ];
	
			/* Draw this sprite */
	
			y		=	source[ 0/4 ] >> 16;
			x		=	source[ 0/4 ] & 0xffff;
			attr	=	source[ 4/4 ] >> 16;
			code	=	source[ 4/4 ] & 0x1ffff;
	
			flipx	=	attr & 0x4000;
			flipy	=	attr & 0x8000;
	
			zoomx	=	((x & 0xf000) >> 12);
			zoomy	=	((y & 0xf000) >> 12);
			nx		=	((x & 0x0e00) >> 9) + 1;
			ny		=	((y & 0x0e00) >> 9) + 1;
			x		=	((x & 0x01ff));
			y		=	((y & 0x00ff)) - (y & 0x100);
	
			/* 180-1ff are negative coordinates. Note that $80 pixels is
			   the maximum extent of a sprite, which can therefore be moved
			   out of screen without problems */
			if (x >= 0x180)	x -= 0x200;
	
			zoomx = 16*8 - zoomtable[zoomx];
			zoomy = 16*8 - zoomtable[zoomy];
	
			if (flip_screen != 0)
			{
				x = width  - x - (nx * zoomx)/8;
				y = height - y - (ny * zoomy)/8;
				flipx = !flipx;
				flipy = !flipy;
			}
	
			if (flipx != 0)	{ xstart = nx-1;  xend = -1;  xinc = -1; }
			else		{ xstart = 0;     xend = nx;  xinc = +1; }
	
			if (flipy != 0)	{ ystart = ny-1;  yend = -1;   yinc = -1; }
			else		{ ystart = 0;     yend = ny;   yinc = +1; }
	
			for (dy = ystart; dy != yend; dy += yinc)
			{
				for (dx = xstart; dx != xend; dx += xinc)
				{
					int addr	=	(code*2) & (TILES_LEN-1);
	
					if (zoomx == (16*8) && zoomy == (16*8))
						pdrawgfx(bitmap,Machine.gfx[0],
								TILES[addr+1] * 256 + TILES[addr],
								attr >> 8,
								flipx, flipy,
								x + dx * 16, y + dy * 16,
								&Machine.visible_area,TRANSPARENCY_PEN,15,
								(attr & 0xc0) ? 2 : 0);	// layer 0&1 have pri 0&1
					else
						pdrawgfxzoom(bitmap,Machine.gfx[0],
									TILES[addr+1] * 256 + TILES[addr],
									attr >> 8,
									flipx, flipy,
									x + (dx * zoomx) / 8, y + (dy * zoomy) / 8,
									&Machine.visible_area,TRANSPARENCY_PEN,15,
									(0x10000/0x10/8) * (zoomx + 8),(0x10000/0x10/8) * (zoomy + 8),	// nearest greater integer value to avoid holes
									(attr & 0xc0) ? 2 : 0);	// layer 0&1 have pri 0&1
	
					code++;
				}
			}
		}
	}
	
	
	static void psikyo_mark_sprite_colors(void)
	{
		int count = 0;
		int offs,i,col,colmask[0x100];
	
		data16_t *spritelist	=	(data16_t *)spriteram32_2;
	
		UBytePtr TILES	=	memory_region(REGION_USER1);	// Sprites LUT
		int TILES_LEN			=	memory_region_length(REGION_USER1);
	
		unsigned int *pen_usage	=	Machine.gfx[0].pen_usage;
		int total_elements		=	Machine.gfx[0].total_elements;
		int color_codes_start	=	Machine.drv.gfxdecodeinfo[0].color_codes_start;
		int total_color_codes	=	Machine.drv.gfxdecodeinfo[0].total_color_codes;
	
		int xmin = Machine.visible_area.min_x;
		int xmax = Machine.visible_area.max_x;
		int ymin = Machine.visible_area.min_y;
		int ymax = Machine.visible_area.max_y;
	
		/* Exit if sprites are disabled */
		if ( spritelist[ BYTE_XOR_BE((0x800-2)/2) ] & 1 )	return;
	
		memset(colmask, 0, sizeof(colmask));
	
		for ( offs = 0/2; offs < (0x800-2)/2 ; offs += 2/2 )
		{
			data32_t *source;
			int	sprite;
	
			int	x,y,attr,code,flipx,flipy,nx,ny;
	
			int xstart, ystart, xend, yend;
			int xinc, yinc, dx, dy;
			int color;
	
			/* Get next entry in the list */
			sprite	=	spritelist[ BYTE_XOR_BE(offs) ];
	
			/* End of sprites list */
			if (sprite == 0xffff)	break;
	
			sprite	%=	0x300;
			source	=	&spriteram32[ sprite*8/4 ];
	
			/* Mark the pens used by the visible portion of this sprite */
	
			y		=	source[ 0/4 ] >> 16;
			x		=	source[ 0/4 ] & 0xffff;
			attr	=	source[ 4/4 ] >> 16;
			code	=	source[ 4/4 ] & 0x1ffff;
	
			flipx	=	attr & 0x4000;
			flipy	=	attr & 0x8000;
	
			color	=	(attr >> 8) % total_color_codes;
	
			nx	=	((x >> 9) & 0x7) + 1;
			ny	=	((y >> 9) & 0x7) + 1;
	
			x = (x & 0x1ff);
			y = (y & 0x0ff) - (y & 0x100);
	
			if (x >= 0x180)	x -= 0x200;
	
			/* No need to account for screen flipping, but we have
			   to consider sprite flipping though: */
	
			if (flipx != 0)	{ xstart = nx-1;  xend = -1;  xinc = -1; }
			else		{ xstart = 0;     xend = nx;  xinc = +1; }
	
			if (flipy != 0)	{ ystart = ny-1;  yend = -1;   yinc = -1; }
			else		{ ystart = 0;     yend = ny;   yinc = +1; }
	
			for (dy = ystart; dy != yend; dy += yinc)
			{
				for (dx = xstart; dx != xend; dx += xinc)
				{
					int addr	=	(code*2) & (TILES_LEN-1);
					int tile	=	TILES[addr+1] * 256 + TILES[addr];
	
					if (((x+dx*16+15) >= xmin) && ((x+dx*16) <= xmax) &&
						((y+dy*16+15) >= ymin) && ((y+dy*16) <= ymax))
						colmask[color] |= pen_usage[tile % total_elements];
	
					code++;
				}
			}
		}
	
		for (col = 0; col < total_color_codes; col++)
		 for (i = 0; i < 15; i++)	// pen 15 is transparent
		  if (colmask[col] & (1 << i))
		  {	palette_used_colors[16 * col + i + color_codes_start] = PALETTE_COLOR_USED;
			count++;	}
	
	#if 0
	{	char buf[80];
		sprintf(buf,"%d",count);
		usrintf_showmessage(buf);	}
	#endif
	}
	
	
	
	
	/***************************************************************************
	
									Screen Drawing
	
	***************************************************************************/
	
	public static VhUpdatePtr psikyo_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i, layers_ctrl = -1;
	
		static data32_t old_layer0_ctrl=0, old_layer1_ctrl=0;
	
		data32_t layer0_scrollx, layer0_scrolly;
		data32_t layer1_scrollx, layer1_scrolly;
		data32_t layer0_ctrl = psikyo_vregs[ 0x412/4 ];
		data32_t layer1_ctrl = psikyo_vregs[ 0x416/4 ];
	
		flip_screen_set(~readinputport(2) & 1);	// hardwired to a DSW bit
	
		/* The layers' sizes can dynamically change */
		if ((old_layer0_ctrl & 0x0100) != (layer0_ctrl & 0x0100))
		{
			tilemap_dispose(tilemap_0);
			tilemap_0 = tilemap_create(	get_tile_info_0, tilemap_scan_rows, TILEMAP_TRANSPARENT, 16,16,
										(layer0_ctrl & 0x0100) ? 0x40 : 0x20,
										(layer0_ctrl & 0x0100) ? 0x40 : 0x80 );
	
			tilemap_set_scroll_rows(tilemap_0,(layer0_ctrl & 0x0100) ? 0x40*16 : 0x80*16);	// line scrolling
			tilemap_set_scroll_cols(tilemap_0,1);
			tilemap_set_transparent_pen(tilemap_0,15);
		}
	
		if ((old_layer1_ctrl & 0x0100) != (layer1_ctrl & 0x0100))
		{
			tilemap_dispose(tilemap_1);
			tilemap_1 = tilemap_create(	get_tile_info_1, tilemap_scan_rows, TILEMAP_TRANSPARENT, 16,16,
										(layer1_ctrl & 0x0100) ? 0x40 : 0x20,
										(layer1_ctrl & 0x0100) ? 0x40 : 0x80 );
	
			tilemap_set_scroll_rows(tilemap_1,(layer1_ctrl & 0x0100) ? 0x40*16 : 0x80*16);	// line scrolling
			tilemap_set_scroll_cols(tilemap_1,1);
			tilemap_set_transparent_pen(tilemap_1,15);
		}
	
		old_layer0_ctrl = layer0_ctrl;
		old_layer1_ctrl = layer1_ctrl;
	
	
	#ifdef MAME_DEBUG
	if (keyboard_pressed(KEYCODE_Z))
	{
		int msk = 0;
		if (keyboard_pressed(KEYCODE_Q))	msk |= 1;
		if (keyboard_pressed(KEYCODE_W))	msk |= 2;
		if (keyboard_pressed(KEYCODE_A))	msk |= 4;
		if (msk != 0) layers_ctrl &= msk;
	
	#if 0
	{	char buf[80];
		sprintf(buf,"L:%04X-%04X S:%04X",
					psikyo_vregs[ 0x412/4 ],
					psikyo_vregs[ 0x416/4 ],
					spriteram32_2[ 0x7fe/4 ] );
		usrintf_showmessage(buf);	}
	#endif
	}
	#endif
	
		/* Layers enable (not quite right) */
	
	/*
		gunbird:	L:00d0-04d0	S:0008 (00e1 04e1 0009 or 00e2 04e2 000a, for a blink, on scene transitions)
		sngkace:	L:00d0-00d0	S:0008 (00d1 00d1 0009, for a blink, on scene transitions)
		btlkrodj:	L:0120-0510	S:0008 (0121 0511 0009, for a blink, on scene transitions)
	*/
		tilemap_set_enable(tilemap_0, ~layer0_ctrl & 1);
		tilemap_set_enable(tilemap_1, ~layer1_ctrl & 1);
	
		/* Layers scrolling */
	
		layer0_scrolly = psikyo_vregs[ 0x402/4 ];
		layer0_scrollx = psikyo_vregs[ 0x406/4 ];
		layer1_scrolly = psikyo_vregs[ 0x40a/4 ];
		layer1_scrollx = psikyo_vregs[ 0x40e/4 ];
	
		tilemap_set_scrolly(tilemap_0, 0, layer0_scrolly );
		tilemap_set_scrolly(tilemap_1, 0, layer1_scrolly );
	
		for (i=0; i<256; i++)	// 256 screen lines
		{
			tilemap_set_scrollx(
				tilemap_0,
				(i+layer0_scrolly) % ((layer0_ctrl & 0x0100) ? 0x40*16 : 0x80*16),
				layer0_scrollx + ((data16_t *)psikyo_vregs)[BYTE_XOR_BE(0x000/2 + i)] );
	
			tilemap_set_scrollx(
				tilemap_1,
				(i+layer1_scrolly) % ((layer1_ctrl & 0x0100) ? 0x40*16 : 0x80*16),
				layer1_scrollx + ((data16_t *)psikyo_vregs)[BYTE_XOR_BE(0x200/2 + i)] );
		}
	
	
	
		tilemap_update(ALL_TILEMAPS);
	
		palette_init_used_colors();
	
		psikyo_mark_sprite_colors();
	
		palette_recalc();
	
	
		fillbitmap(bitmap,palette_transparent_pen,&Machine.visible_area);
	
		fillbitmap(priority_bitmap,0,NULL);
	
		if ((layers_ctrl & 1) != 0)	tilemap_draw(bitmap,tilemap_0, TILEMAP_IGNORE_TRANSPARENCY, 0);
		if ((layers_ctrl & 2) != 0)	tilemap_draw(bitmap,tilemap_1, 0,                           1);
	
		/* Sprites can go below layer 1 (and 0?) */
		if ((layers_ctrl & 4) != 0)	psikyo_draw_sprites(bitmap);
	} };
}
