/***************************************************************************

						  -= Fuuki 16 Bit Games =-

					driver by	Luca Elia (l.elia@tin.it)


Note:	if MAME_DEBUG is defined, pressing Z with:

		Q / W / R / T		Shows Layer 0 / 1 / 2 / 3
		A					Shows Sprites

		Keys can be used together!


	[ 4 Scrolling Layers ]

							[ Layer 0 ]		[ Layer 1 ]		[ Layers 2&3 ]

	Tile Size:				16 x 16 x 4		16 x 16 x 8		8 x 8 x 4
	Layer Size (tiles):		64 x 32			64 x 32			64 x 32

	[ 1024? Zooming Sprites ]

	Sprites are made of 16 x 16 x 4 tiles. Size can vary from 1 to 16
	tiles both horizontally and vertically.
	There is zooming (from full size to half size) and 4 levels of
	priority (wrt layers)

	* Note: the game does hardware assisted raster effects *

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class fuuki16
{
	
	/* Variables that driver has access to: */
	
	data16_t *fuuki16_vram_0, *fuuki16_vram_1;
	data16_t *fuuki16_vram_2, *fuuki16_vram_3;
	data16_t *fuuki16_vregs,  *fuuki16_unknown, *fuuki16_priority;
	
	
	/***************************************************************************
	
	
										Tilemaps
	
		Offset: 	Bits:					Value:
	
			0.w								Code
	
			2.w		fedc ba98 ---- ----
					---- ---- 7--- ----		Flip Y
					---- ---- -6-- ----		Flip X
					---- ---- --54 3210		Color
	
	
	***************************************************************************/
	
	#define LAYER( _N_ ) \
	\
	static struct tilemap *tilemap_##_N_; \
	\
	public static GetTileInfoPtr get_tile_info_##_N_ = new GetTileInfoPtr() { public void handler(int tile_index)  \
	{ \
		data16_t code = fuuki16_vram_##_N_[ 2 * tile_index + 0 ]; \
		data16_t attr = fuuki16_vram_##_N_[ 2 * tile_index + 1 ]; \
		SET_TILE_INFO(1 + _N_, code, attr & 0x3f,TILE_FLIPYX( (attr >> 6) & 3 )) \
	} }; \
	\
	WRITE16_HANDLER( fuuki16_vram_##_N_##_w ) \
	{ \
		data16_t old_data	=	fuuki16_vram_##_N_[offset]; \
		data16_t new_data	=	COMBINE_DATA(&fuuki16_vram_##_N_[offset]); \
		if (old_data != new_data)	tilemap_mark_tile_dirty(tilemap_##_N_,offset/2); \
	}
	
	LAYER( 0 )
	LAYER( 1 )
	LAYER( 2 )
	LAYER( 3 )
	
	/***************************************************************************
	
	
								Video Hardware Init
	
	
	***************************************************************************/
	
	public static VhConvertColorPromPtr fuuki16_vh_init_palette = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int color, pen;
	
		/* Layer 0 has 8 bits per pixel, but the color code has
		   a 16 color granularity */
		for( color = 0; color < 64; color++ )
			for( pen = 0; pen < 256; pen++ )
				colortable[color * 256 + pen + 0x400*4] = ((color * 16 + pen)%(64*16)) + 0x400;
	
		/* The game does not initialise the palette at startup. It should
		   be totally black */
		memset(palette, 0, 3 * Machine.drv.total_colors);
	} };
	
	public static VhStartPtr fuuki16_vh_start = new VhStartPtr() { public int handler() 
	{
		tilemap_0 = tilemap_create(	get_tile_info_0, tilemap_scan_rows,
									TILEMAP_TRANSPARENT, 16,16, 0x40,0x20);
	
		tilemap_1 = tilemap_create(	get_tile_info_1,tilemap_scan_rows,
									TILEMAP_TRANSPARENT, 16,16, 0x40,0x20);
	
		tilemap_2 = tilemap_create(	get_tile_info_2,tilemap_scan_rows,
									TILEMAP_TRANSPARENT, 8,8, 0x40,0x20);
	
		tilemap_3 = tilemap_create(	get_tile_info_3,tilemap_scan_rows,
									TILEMAP_TRANSPARENT, 8,8, 0x40,0x20);
	
		if ( !tilemap_0 || !tilemap_1 || !tilemap_2 || !tilemap_3 )
			return 1;
	
		tilemap_set_transparent_pen(tilemap_0,0xff);	// 8 bits
	
		tilemap_set_transparent_pen(tilemap_1,0x0f);	// 4 bits
		tilemap_set_transparent_pen(tilemap_2,0x0f);
		tilemap_set_transparent_pen(tilemap_3,0x0f);
		return 0;
	} };
	
	
	/***************************************************************************
	
	
									Sprites Drawing
	
		Offset: 	Bits:					Value:
	
			0.w		fedc ---- ---- ----		Number Of Tiles Along X - 1
					---- b--- ---- ----		Flip X
					---- -a-- ---- ----		1 = Don't Draw This Sprite
					---- --98 7654 3210		X (Signed)
	
			2.w		fedc ---- ---- ----		Number Of Tiles Along Y - 1
					---- b--- ---- ----		Flip Y
					---- -a-- ---- ----
					---- --98 7654 3210		Y (Signed)
	
			4.w		fedc ---- ---- ----		Zoom X ($0 = Full Size, $F = Half Size)
					---- ba98 ---- ----		Zoom Y ""
					---- ---- 7--- ----		0 = Priority Over Foreground
					---- ---- -6-- ----		0 = Priority Over Background
					---- ---- --54 3210		Color
	
			6.w								Code
	
	
	***************************************************************************/
	
	static void fuuki16_draw_sprites(struct osd_bitmap *bitmap)
	{
		int offs;
	
		int max_x		=	Machine.visible_area.max_x+1;
		int max_y		=	Machine.visible_area.max_y+1;
	
		/* Draw them backwards, for pdrawgfx */
		for ( offs = (spriteram_size-8)/2; offs >=0; offs -= 8/2 )
		{
			int x, y, xstart, ystart, xend, yend, xinc, yinc;
			int xnum, ynum, xzoom, yzoom, flipx, flipy;
			int pri_mask;
	
			int sx			=		spriteram16[offs + 0];
			int sy			=		spriteram16[offs + 1];
			int attr		=		spriteram16[offs + 2];
			int code		=		spriteram16[offs + 3];
	
			if ((sx & 0x400) != 0)		continue;
	
			flipx		=		sx & 0x0800;
			flipy		=		sy & 0x0800;
	
			xnum		=		((sx >> 12) & 0xf) + 1;
			ynum		=		((sy >> 12) & 0xf) + 1;
	
			xzoom		=		16*8 - (8 * ((attr >> 12) & 0xf))/2;
			yzoom		=		16*8 - (8 * ((attr >>  8) & 0xf))/2;
	
			switch( (attr >> 6) & 3 )
			{
				case 3:		pri_mask = (1<<1)|(1<<2)|(1<<3);	break;
				case 2:		pri_mask = (1<<2)|(1<<3);			break;
	//			case 1:		pri_mask = (1<<1)|(1<<3);			break;
				case 0:
				default:	pri_mask = 0;
			}
	
			sx = (sx & 0x1ff) - (sx & 0x200);
			sy = (sy & 0x1ff) - (sy & 0x200);
	
			if (flip_screen != 0)
			{	flipx = !flipx;		sx = max_x - sx - xnum * 16;
				flipy = !flipy;		sy = max_y - sy - ynum * 16;		}
	
			if (flipx != 0)	{ xstart = xnum-1;  xend = -1;    xinc = -1; }
			else		{ xstart = 0;       xend = xnum;  xinc = +1; }
	
			if (flipy != 0)	{ ystart = ynum-1;  yend = -1;    yinc = -1; }
			else		{ ystart = 0;       yend = ynum;  yinc = +1; }
	
			for (y = ystart; y != yend; y += yinc)
			{
				for (x = xstart; x != xend; x += xinc)
				{
					if (xzoom == (16*8) && yzoom == (16*8))
						pdrawgfx(		bitmap,Machine.gfx[0],
										code++,
										attr & 0x3f,
										flipx, flipy,
										sx + x * 16, sy + y * 16,
										&Machine.visible_area,TRANSPARENCY_PEN,15,
										pri_mask	);
					else
						pdrawgfxzoom(	bitmap,Machine.gfx[0],
										code++,
										attr & 0x3f,
										flipx, flipy,
										sx + (x * xzoom) / 8, sy + (y * yzoom) / 8,
										&Machine.visible_area,TRANSPARENCY_PEN,15,
										(0x10000/0x10/8) * (xzoom + 8),(0x10000/0x10/8) * (yzoom + 8),	// nearest greater integer value to avoid holes
										pri_mask	);
				}
			}
	
	#ifdef MAME_DEBUG
	#if 1
	if (keyboard_pressed(KEYCODE_X))
	{	/* Display some info on each sprite */
		struct DisplayText dt[2];	char buf[10];
		sprintf(buf, "%Xx%X %X",xnum,ynum,(attr>>6)&3);
		dt[0].text = buf;	dt[0].color = UI_COLOR_NORMAL;
		dt[0].x = sx;		dt[0].y = sy;
		dt[1].text = 0;	/* terminate array */
		displaytext(Machine.scrbitmap,dt);		}
	#endif
	#endif
		}
	
	}
	
	static void fuuki16_mark_sprites_colors(void)
	{
		memset(palette_used_colors,PALETTE_COLOR_USED,Machine.drv.total_colors);
	}
	
	
	
	/***************************************************************************
	
	
									Screen Drawing
	
		Video Registers (fuuki16_vregs):
	
			00.w		Layer 1 Scroll Y
			02.w		Layer 1 Scroll X
			04.w		Layer 0 Scroll Y
			06.w		Layer 0 Scroll X
			08.w		Layer 2 Scroll Y
			0a.w		Layer 2 Scroll X
			0c.w		Layers Y Offset
			0e.w		Layers X Offset
	
			10-1a.w		? 0
			1c.w		Trigger a level 5 irq on this raster line
			1e.w		? $3390/$3393 (Flip Screen Off/On)
	
		Priority Register (fuuki16_priority):
	
			fedc ba98 7654 3---
			---- ---- ---- -2--		?
			---- ---- ---- --1-
			---- ---- ---- ---0		Swap Layers
	
	
		Unknown Registers (fuuki16_unknown):
	
			00.w		? $0200/$0201	(Flip Screen Off/On)
			02.w		? $f300/$0330
	
	***************************************************************************/
	
	
	static void fuuki16_draw_layer(struct osd_bitmap *bitmap, int ctrl, int i, int flag, int pri)
	{
		switch( i )
		{
			case 0:	if ((ctrl & 0x01) != 0)	{	tilemap_draw(bitmap,tilemap_0,flag,pri);	return;	}
					break;
			case 1:	if ((ctrl & 0x02) != 0)	{	tilemap_draw(bitmap,tilemap_1,flag,pri);	return;	}
					break;
			case 2:	if ((ctrl & 0x04) != 0)	{	tilemap_draw(bitmap,tilemap_3,flag,pri);	}
					if ((ctrl & 0x08) != 0)	{	tilemap_draw(bitmap,tilemap_2,flag,pri);	}
					if ((ctrl & 0x04) || (ctrl & 0x08))	return;
					break;
		}
		if (flag == TILEMAP_IGNORE_TRANSPARENCY)
		{
			fillbitmap(bitmap,palette_transparent_pen,&Machine.visible_area);
			fillbitmap(priority_bitmap,0,NULL);
		}
	}
	
	public static VhUpdatePtr fuuki16_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		data16_t layer0_scrollx, layer0_scrolly;
		data16_t layer1_scrollx, layer1_scrolly;
		data16_t layer2_scrollx, layer2_scrolly;
		data16_t scrollx_offs,   scrolly_offs;
	
		int background, middleground, foreground;
	
		int layers_ctrl = -1;
	
		flip_screen_set(fuuki16_vregs[0x1e/2] & 1);
	
		/* Layers scrolling */
	
		scrolly_offs = fuuki16_vregs[0xc/2] - (flip_screen ? 0x103 : 0x1f3);
		scrollx_offs = fuuki16_vregs[0xe/2] - (flip_screen ? 0x2a7 : 0x3f6);
	
		layer1_scrolly = fuuki16_vregs[0x0/2] + scrolly_offs;
		layer1_scrollx = fuuki16_vregs[0x2/2] + scrollx_offs;
		layer0_scrolly = fuuki16_vregs[0x4/2] + scrolly_offs;
		layer0_scrollx = fuuki16_vregs[0x6/2] + scrollx_offs;
	
		layer2_scrolly = fuuki16_vregs[0x8/2];
		layer2_scrollx = fuuki16_vregs[0xa/2];
	
		tilemap_set_scrollx(tilemap_0, 0, layer0_scrollx);
		tilemap_set_scrolly(tilemap_0, 0, layer0_scrolly);
		tilemap_set_scrollx(tilemap_1, 0, layer1_scrollx);
		tilemap_set_scrolly(tilemap_1, 0, layer1_scrolly);
	
		tilemap_set_scrollx(tilemap_2, 0, layer2_scrollx + 0x10);
		tilemap_set_scrolly(tilemap_2, 0, layer2_scrolly);
		tilemap_set_scrollx(tilemap_3, 0, layer2_scrollx + 0x10);
		tilemap_set_scrolly(tilemap_3, 0, layer2_scrolly);
	
	#ifdef MAME_DEBUG
	if ( keyboard_pressed(KEYCODE_Z) || keyboard_pressed(KEYCODE_X) )
	{
		int msk = 0;
		if (keyboard_pressed(KEYCODE_Q))	msk |= 0x01;
		if (keyboard_pressed(KEYCODE_W))	msk |= 0x02;
		if (keyboard_pressed(KEYCODE_E))	msk |= 0x04;
		if (keyboard_pressed(KEYCODE_R))	msk |= 0x08;
		if (keyboard_pressed(KEYCODE_A))	msk |= 0x10;
		if (msk != 0) layers_ctrl &= msk;
	
	#if 1
	{	char buf[10];
		sprintf(buf,"%04X %04X %04X",
			fuuki16_unknown[0],fuuki16_unknown[1],*fuuki16_priority);
		usrintf_showmessage(buf);	}
	#endif
	}
	#endif
	
		tilemap_update(ALL_TILEMAPS);
	
		palette_init_used_colors();
	
		fuuki16_mark_sprites_colors();
	
		palette_recalc();
	
		background   = 0;
		foreground   = 1;
		middleground = 2;
		/* swap bg with mg */
		if (*fuuki16_priority & 1)	{ int t = background;	background = foreground;	foreground = t;		}
		/* swap mg with fg */
		if (*fuuki16_priority & 2)	{ int t = foreground;	foreground = middleground;	middleground = t;	}
	
		/* The backmost tilemap decides the background color(s) but sprites can
		   go below the opaque pixels of that tilemap. We thus need to mark the
		   transparent pixels of this layer with a different priority value */
		fuuki16_draw_layer(bitmap, layers_ctrl, background,  TILEMAP_IGNORE_TRANSPARENCY, 0);
	
		fuuki16_draw_layer(bitmap, layers_ctrl, background,  0, 1);
		fuuki16_draw_layer(bitmap, layers_ctrl, foreground,  0, 2);
		fuuki16_draw_layer(bitmap, layers_ctrl, middleground,0, 2);
	
		if ((layers_ctrl & 0x10) != 0)	fuuki16_draw_sprites(bitmap);
	} };
}
