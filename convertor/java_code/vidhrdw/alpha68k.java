/***************************************************************************

   Alpha 68k video emulation - Bryan McPhail, mish@tendril.co.uk

****************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class alpha68k
{
	
	static int bank_base,flipscreen;
	static struct tilemap *fix_tilemap;
	
	/******************************************************************************/
	
	void alpha68k_flipscreen_w(int flip)
	{
		flipscreen = flip;
	}
	
	void alpha68k_V_video_bank_w(int bank)
	{
		bank_base = bank&0xf;
	}
	
	WRITE16_HANDLER( alpha68k_paletteram_w )
	{
		int newword;
		int r,g,b;
	
		COMBINE_DATA(paletteram16 + offset);
		newword = paletteram16[offset];
	
		r = ((newword >> 7) & 0x1e) | ((newword >> 14) & 0x01);
		g = ((newword >> 3) & 0x1e) | ((newword >> 13) & 0x01);
		b = ((newword << 1) & 0x1e) | ((newword >> 12) & 0x01);
	
		r = (r << 3) | (r >> 2);
		g = (g << 3) | (g >> 2);
		b = (b << 3) | (b >> 2);
	
		palette_change_color(offset,r,g,b);
	}
	
	/******************************************************************************/
	
	public static GetTileInfoPtr get_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int tile  = videoram16[2*tile_index]   &0xff;
		int color = videoram16[2*tile_index+1] &0x0f;
	
		tile=tile | (bank_base<<8);
	
		SET_TILE_INFO(
				0,
				tile,
				color,
				0)
	} };
	
	WRITE16_HANDLER( alpha68k_videoram_w )
	{
		/* Doh. */
		if (ACCESSING_LSB != 0)
			if (ACCESSING_MSB != 0)
				videoram16[offset] = data;
			else
				videoram16[offset] = data & 0xff;
		else
			videoram16[offset] = (data >> 8) & 0xff;
	
		tilemap_mark_tile_dirty(fix_tilemap,offset/2);
	}
	
	public static VhStartPtr alpha68k_vh_start = new VhStartPtr() { public int handler() 
	{
		fix_tilemap = tilemap_create(get_tile_info,tilemap_scan_cols,TILEMAP_TRANSPARENT,8,8,32,32);
	
		if (!fix_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(fix_tilemap,0);
	
		return 0;
	} };
	
	/******************************************************************************/
	
	static void draw_sprites(struct osd_bitmap *bitmap, int j, int pos)
	{
		int offs,mx,my,color,tile,fx,fy,i;
	
		for (offs = pos; offs < pos+0x400; offs += 0x40 )
		{
			mx = spriteram16[offs+2+(2*j)]<<1;
			my = spriteram16[offs+3+(2*j)];
			if ((my & 0x8000) != 0) mx++;
	
			mx=(mx+0x100)&0x1ff;
			my=(my+0x100)&0x1ff;
			mx-=0x100;
			my-=0x100;
			my=0x200 - my;
			my-=0x200;
	
			if (flipscreen != 0) {
				mx=240-mx;
				my=240-my;
			}
	
			for (i=0; i<0x40; i+=2) {
				tile  = spriteram16[offs+1+i+(0x800*j)+0x800];
				color = spriteram16[offs  +i+(0x800*j)+0x800]&0x7f;
	
				fy=tile&0x8000;
				fx=tile&0x4000;
				tile&=0x3fff;
	
				if (flipscreen != 0) {
					if (fx != 0) fx=0; else fx=1;
					if (fy != 0) fy=0; else fy=1;
				}
	
				if (color != 0)
					drawgfx(bitmap,Machine.gfx[1],
						tile,
						color,
						fx,fy,
						mx,my,
						0,TRANSPARENCY_PEN,0);
	
				if (flipscreen != 0)
					my=(my-16)&0x1ff;
				else
					my=(my+16)&0x1ff;
			}
		}
	}
	
	/******************************************************************************/
	
	public static VhUpdatePtr alpha68k_II_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		static int last_bank=0;
		int offs,color,i;
		int colmask[0x80],code,pal_base;
	
		if (last_bank!=bank_base)
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		last_bank=bank_base;
		tilemap_set_flip(ALL_TILEMAPS,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
		tilemap_update(fix_tilemap);
	
		/* Build the dynamic palette */
		palette_init_used_colors();
		pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;
		for (color = 0;color < 128;color++) colmask[color] = 0;
		for (offs = 0x800;offs <0x2000;offs += 2 )
		{
			color= spriteram16[offs] & 0x7f;
			if (!color) continue;
			code = spriteram16[offs+1] &0x3fff;
			colmask[color] |= Machine.gfx[1].pen_usage[code];
		}
	
		for (color = 1;color < 128;color++)
		{
			for (i = 1;i < 16;i++)
			{
				if (colmask[color] & (1 << i))
					palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
			}
		}
	
		palette_used_colors[2047] = PALETTE_COLOR_USED;
		palette_recalc();
		fillbitmap(bitmap,Machine.pens[2047],&Machine.visible_area);
	
		draw_sprites(bitmap,1,0x000);
		draw_sprites(bitmap,1,0x400);
		draw_sprites(bitmap,0,0x000);
		draw_sprites(bitmap,0,0x400);
		draw_sprites(bitmap,2,0x000);
		draw_sprites(bitmap,2,0x400);
		tilemap_draw(bitmap,fix_tilemap,0,0);
	} };
	
	/******************************************************************************/
	
	/*
		Video banking:
	
		Write to these locations in this order for correct bank:
	
		20 28 30 for Bank 0
		60 28 30 for Bank 1
		20 68 30 etc
		60 68 30
		20 28 70
		60 28 70
		20 68 70
		60 68 70 for Bank 7
	
		Actual data values written don't matter!
	
	*/
	
	WRITE16_HANDLER( alpha68k_II_video_bank_w )
	{
		static int buffer_28,buffer_60,buffer_68;
	
		switch (offset) {
			case 0x10: /* Reset */
				bank_base=buffer_28=buffer_60=buffer_68=0;
				return;
			case 0x14:
				buffer_28=1;
				return;
			case 0x18:
				if (buffer_68 != 0) {if (buffer_60 != 0) bank_base=3; else bank_base=2; }
				if (buffer_28 != 0) {if (buffer_60 != 0) bank_base=1; else bank_base=0; }
				return;
			case 0x30:
				bank_base=buffer_28=buffer_68=0;
				buffer_60=1;
				return;
			case 0x34:
				buffer_68=1;
				return;
			case 0x38:
				if (buffer_68 != 0) {if (buffer_60 != 0) bank_base=7; else bank_base=6; }
				if (buffer_28 != 0) {if (buffer_60 != 0) bank_base=5; else bank_base=4; }
				return;
			case 0x08: /* Graphics flags?  Not related to fix chars anyway */
			case 0x0c:
			case 0x28:
			case 0x2c:
				return;
		}
	
		logerror("%04x \n",offset);
	}
	
	/******************************************************************************/
	
	WRITE16_HANDLER( alpha68k_V_video_control_w )
	{
		switch (offset) {
			case 0x08: /* Graphics flags?  Not related to fix chars anyway */
			case 0x0c:
			case 0x28:
			case 0x2c:
				return;
		}
	}
	
	static void draw_sprites_V(struct osd_bitmap *bitmap, int j, int s, int e, int fx_mask, int fy_mask, int sprite_mask)
	{
		int offs,mx,my,color,tile,fx,fy,i;
	
		for (offs = s; offs < e; offs += 0x40 )
		{
			mx = spriteram16[offs+2+(2*j)]<<1;
			my = spriteram16[offs+3+(2*j)];
			if ((my & 0x8000) != 0) mx++;
	
			mx=(mx+0x100)&0x1ff;
			my=(my+0x100)&0x1ff;
			mx-=0x100;
			my-=0x100;
			my=0x200 - my;
			my-=0x200;
	
			if (flipscreen != 0) {
				mx=240-mx;
				my=240-my;
			}
	
			for (i=0; i<0x40; i+=2) {
				tile  = spriteram16[offs+1+i+(0x800*j)+0x800];
				color = spriteram16[offs  +i+(0x800*j)+0x800]&0xff;
	
				fx=tile&fx_mask;
				fy=tile&fy_mask;
				tile=tile&sprite_mask;
				if (tile>0x4fff) continue;
	
				if (flipscreen != 0) {
					if (fx != 0) fx=0; else fx=1;
					if (fy != 0) fy=0; else fy=1;
				}
	
				if (color != 0)
					drawgfx(bitmap,Machine.gfx[1],
						tile,
						color,
						fx,fy,
						mx,my,
						0,TRANSPARENCY_PEN,0);
	
				if (flipscreen != 0)
					my=(my-16)&0x1ff;
				else
					my=(my+16)&0x1ff;
			}
		}
	}
	
	public static VhUpdatePtr alpha68k_V_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		static int last_bank=0;
		int offs,color,i;
		int colmask[256],code,pal_base;
	
		if (last_bank!=bank_base)
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		last_bank=bank_base;
		tilemap_set_flip(ALL_TILEMAPS,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
		tilemap_update(fix_tilemap);
	
		/* Build the dynamic palette */
		palette_init_used_colors();
		pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;
		for (color = 0;color < 256;color++) colmask[color] = 0;
		for (offs = 0x0800;offs <0x2000;offs += 2 )
		{
			color= spriteram16[offs]&0xff;
			if (!color) continue;
			code = spriteram16[offs+1]&0x7fff;
			colmask[color] |= Machine.gfx[1].pen_usage[code];
		}
	
		for (color = 1;color < 256;color++)
		{
			for (i = 1;i < 16;i++)
			{
				if (colmask[color] & (1 << i))
					palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
			}
		}
	
		palette_used_colors[4095] = PALETTE_COLOR_USED;
		palette_recalc();
		fillbitmap(bitmap,Machine.pens[4095],&Machine.visible_area);
	
		/* This appears to be correct priority */
		if (!strcmp(Machine.gamedrv.name,"skyadvnt") || !strcmp(Machine.gamedrv.name,"skyadvnu")) /* Todo */
		{
			draw_sprites_V(bitmap,0,0x07c0,0x0800,0,0x8000,0x7fff);
			draw_sprites_V(bitmap,1,0x0000,0x0800,0,0x8000,0x7fff);
			draw_sprites_V(bitmap,2,0x0000,0x0800,0,0x8000,0x7fff);
			draw_sprites_V(bitmap,0,0x0000,0x07c0,0,0x8000,0x7fff);
		}
		else	/* gangwars */
		{
			draw_sprites_V(bitmap,0,0x07c0,0x0800,0x8000,0,0x7fff);
			draw_sprites_V(bitmap,1,0x0000,0x0800,0x8000,0,0x7fff);
			draw_sprites_V(bitmap,2,0x0000,0x0800,0x8000,0,0x7fff);
			draw_sprites_V(bitmap,0,0x0000,0x07c0,0x8000,0,0x7fff);
		}
	
		tilemap_draw(bitmap,fix_tilemap,0,0);
	} };
	
	public static VhUpdatePtr alpha68k_V_sb_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		static int last_bank=0;
		int offs,color,i;
		int colmask[256],code,pal_base;
	
		if (last_bank!=bank_base)
	 		tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		last_bank=bank_base;
		tilemap_set_flip(ALL_TILEMAPS,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	
		tilemap_update(fix_tilemap);
	
		/* Build the dynamic palette */
		palette_init_used_colors();
	
		/* Tiles */
		pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;
		for (color = 0;color < 256;color++) colmask[color] = 0;
		for (offs = 0x0800;offs <0x2000;offs += 2 )
		{
			color= spriteram16[offs]&0xff;
			if (!color) continue;
			code = spriteram16[offs+1]&0x7fff;
			colmask[color] |= Machine.gfx[1].pen_usage[code];
		}
	
		for (color = 1;color < 256;color++)
		{
			for (i = 1;i < 16;i++)
			{
				if (colmask[color] & (1 << i))
					palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
			}
		}
	
		palette_used_colors[4095] = PALETTE_COLOR_USED;
		palette_recalc();
		fillbitmap(bitmap,Machine.pens[4095],&Machine.visible_area);
	
		/* This appears to be correct priority */
		draw_sprites_V(bitmap,0,0x07c0,0x0800,0x4000,0x8000,0x3fff);
		draw_sprites_V(bitmap,1,0x0000,0x0800,0x4000,0x8000,0x3fff);
		draw_sprites_V(bitmap,2,0x0000,0x0800,0x4000,0x8000,0x3fff);
		draw_sprites_V(bitmap,0,0x0000,0x07c0,0x4000,0x8000,0x3fff);
	
		tilemap_draw(bitmap,fix_tilemap,0,0);
	} };
	
	/******************************************************************************/
	
	public static VhConvertColorPromPtr alpha68k_I_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i,bit0,bit1,bit2,bit3;
	
		for( i=0; i<256; i++ )
		{
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			*palette++ = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			bit0 = (color_prom.read(0x100)>> 0) & 0x01;
			bit1 = (color_prom.read(0x100)>> 1) & 0x01;
			bit2 = (color_prom.read(0x100)>> 2) & 0x01;
			bit3 = (color_prom.read(0x100)>> 3) & 0x01;
			*palette++ = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			bit0 = (color_prom.read(0x200)>> 0) & 0x01;
			bit1 = (color_prom.read(0x200)>> 1) & 0x01;
			bit2 = (color_prom.read(0x200)>> 2) & 0x01;
			bit3 = (color_prom.read(0x200)>> 3) & 0x01;
			*palette++ = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			color_prom++;
		}
	} };
	
	static void draw_sprites2(struct osd_bitmap *bitmap, int c,int d)
	{
		int offs,mx,my,color,tile,i;
	
		for (offs = 0x0000; offs < 0x400; offs += 0x20 )
		{
			mx=spriteram16[offs+c];
	
			my=mx>>8;
			mx=mx&0xff;
	
			mx=(mx+0x100)&0x1ff;
			my=(my+0x100)&0x1ff;
			mx-=0x110;
			my-=0x100;
			my=0x200 - my;
			my-=0x200;
	
			for (i=0; i<0x20; i++) {
				tile=spriteram16[offs+d+i];
				color=1;
				tile&=0x3fff;
	
				if (tile && tile!=0x3000 && tile!=0x26)
					drawgfx(bitmap,Machine.gfx[0],
						tile,
						color,
						0,0,
						mx+16,my,
						0,TRANSPARENCY_PEN,0);
	
				my=(my+8)&0xff;
			}
		}
	}
	
	public static VhUpdatePtr alpha68k_I_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		fillbitmap(bitmap,Machine.pens[0],&Machine.visible_area);
	
		/* This appears to be correct priority */
	draw_sprites2(bitmap,3,0x0c00);
	draw_sprites2(bitmap,2,0x0800);
	draw_sprites2(bitmap,1,0x0400);
	//
	} };
	
	/******************************************************************************/
	
	public static VhConvertColorPromPtr kyros_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i,bit0,bit1,bit2,bit3;
	
		for (i = 0;i < 256;i++)
		{
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			*palette++ = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			bit0 = (color_prom.read(0x100)>> 0) & 0x01;
			bit1 = (color_prom.read(0x100)>> 1) & 0x01;
			bit2 = (color_prom.read(0x100)>> 2) & 0x01;
			bit3 = (color_prom.read(0x100)>> 3) & 0x01;
			*palette++ = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			bit0 = (color_prom.read(0x200)>> 0) & 0x01;
			bit1 = (color_prom.read(0x200)>> 1) & 0x01;
			bit2 = (color_prom.read(0x200)>> 2) & 0x01;
			bit3 = (color_prom.read(0x200)>> 3) & 0x01;
			*palette++ = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			color_prom++;
		}
	
		color_prom += 0x200;
	
		for (i = 0;i < 256;i++)
		{
			*colortable++ = ((color_prom.read(0)& 0x0f) << 4) | (color_prom.read(0x100)& 0x0f);
			color_prom++;
		}
	} };
	
	
	static void kyros_draw_sprites(struct osd_bitmap *bitmap, int c,int d)
	{
		int offs,mx,my,color,tile,i,bank,fy;
	
		for (offs = 0x0000; offs < 0x400; offs += 0x20 )
		{
			mx=spriteram16[offs+c];
			my=0x100-(mx>>8);
			mx=mx&0xff;
	
			for (i=0; i<0x20; i++) {
				tile=spriteram16[offs+d+i];
				color=(tile&0x4000)>>14;
				fy=tile&0x1000;
				bank=((tile>>10)&0x3)+((tile&0x8000)?4:0);
				tile=(tile&0x3ff)+((tile&0x2000)?0x400:0);
	
				drawgfx(bitmap,Machine.gfx[bank],
						tile,
						color,
						0,fy,
						mx,my,
						0,TRANSPARENCY_PEN,0);
	
				my=(my+8)&0xff;
			}
		}
	}
	
	public static VhUpdatePtr kyros_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		fillbitmap(bitmap,Machine.pens[0],&Machine.visible_area);
	
		kyros_draw_sprites(bitmap,2,0x0800);
		kyros_draw_sprites(bitmap,3,0x0c00);
		kyros_draw_sprites(bitmap,1,0x0400);
	} };
	
	/******************************************************************************/
	
	static void sstingry_draw_sprites(struct osd_bitmap *bitmap, int c,int d)
	{
		int offs,mx,my,color,tile,i,bank,fx,fy;
	
		for (offs = 0x0000; offs < 0x400; offs += 0x20 )
		{
			mx=spriteram16[offs+c];
			my=0x100-(mx>>8);
			mx=mx&0xff;
	
			for (i=0; i<0x20; i++) {
				tile=spriteram16[offs+d+i];
				color=0; //bit 0x4000
				fy=tile&0x1000;
				fx=0;
				tile=(tile&0xfff);
				bank=tile/0x400;
	
				drawgfx(bitmap,Machine.gfx[bank],
						tile&0x3ff,
						color,
						fx,fy,
						mx,my,
						0,TRANSPARENCY_PEN,0);
	
				my=(my+8)&0xff;
			}
		}
	}
	
	public static VhUpdatePtr sstingry_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		fillbitmap(bitmap,Machine.pens[0],&Machine.visible_area);
	
		sstingry_draw_sprites(bitmap,2,0x0800);
		sstingry_draw_sprites(bitmap,3,0x0c00);
		sstingry_draw_sprites(bitmap,1,0x0400);
	} };
	
	/******************************************************************************/
	
	static void get_kouyakyu_info( int tile_index )
	{
		int offs=tile_index*2;
		int tile=videoram16[offs]&0xff;
		int color=videoram16[offs+1]&0xf;
	
		SET_TILE_INFO(
				0,
				tile,
				color,
				0)
	}
	
	WRITE16_HANDLER( kouyakyu_video_w )
	{
		COMBINE_DATA(videoram16+offset);
		tilemap_mark_tile_dirty( fix_tilemap, offset/2 );
	}
	
	public static VhStartPtr kouyakyu_vh_start = new VhStartPtr() { public int handler() 
	{
		fix_tilemap = tilemap_create(get_kouyakyu_info,tilemap_scan_cols,TILEMAP_TRANSPARENT,8,8,32,32);
	
		if (!fix_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(fix_tilemap,0);
	
		return 0;
	} };
	
	public static VhUpdatePtr kouyakyu_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		fillbitmap(bitmap,1,&Machine.visible_area);
	
	sstingry_draw_sprites(bitmap,2,0x0800);
	sstingry_draw_sprites(bitmap,3,0x0c00);
	sstingry_draw_sprites(bitmap,1,0x0400);
	
		tilemap_update(ALL_TILEMAPS);
		tilemap_draw(bitmap,fix_tilemap,0,0);
	} };
}
