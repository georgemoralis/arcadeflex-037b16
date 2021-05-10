/******************************************************************************

	Video Hardware for Nichibutsu Mahjong series.

	Driver by Takahiro Nogi <nogi@kt.rim.or.jp> 1999/11/05 -

******************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class gionbana
{
	
	
	static int gionbana_scrolly1, gionbana_scrolly2;
	static int gionbana_drawx, gionbana_drawy;
	static int gionbana_sizex, gionbana_sizey;
	static int gionbana_radrx, gionbana_radry;
	static int gionbana_vram;
	static int gionbana_gfxrom;
	static int gionbana_dispflag;
	static int gionbana_flipscreen;
	static int gionbana_flipx, gionbana_flipy;
	static int gionbana_paltblnum;
	static int gionbana_screen_refresh;
	static int gfxdraw_mode;
	
	static struct osd_bitmap *gionbana_tmpbitmap0, *gionbana_tmpbitmap1;
	static UBytePtr gionbana_videoram0, *gionbana_videoram1;
	static UBytePtr gionbana_palette;
	static UBytePtr gionbana_paltbl;
	
	
	static 
	
	/******************************************************************************
	
	
	******************************************************************************/
	public static ReadHandlerPtr gionbana_palette_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return gionbana_palette[offset];
	} };
	
	public static WriteHandlerPtr gionbana_palette_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int r, g, b;
	
		gionbana_palette[offset] = data;
	
		if (!(offset & 1)) return;
	
		offset &= 0x1fe;
	
		r = ((gionbana_palette[offset + 0] & 0x0f) << 4);
		g = ((gionbana_palette[offset + 1] & 0xf0) << 0);
		b = ((gionbana_palette[offset + 1] & 0x0f) << 4);
	
		r = (r | (r >> 4));
		g = (g | (g >> 4));
		b = (b | (b >> 4));
	
		palette_change_color((offset >> 1), r, g, b);
	} };
	
	public static ReadHandlerPtr maiko_palette_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return gionbana_palette[offset];
	} };
	
	public static WriteHandlerPtr maiko_palette_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int r, g, b;
	
		gionbana_palette[offset] = data;
	
		if (!(offset & 0x100)) return;
	
		offset &= 0x0ff;
	
		r = ((gionbana_palette[offset + 0x000] & 0x0f) << 4);
		g = ((gionbana_palette[offset + 0x000] & 0xf0) << 0);
		b = ((gionbana_palette[offset + 0x100] & 0x0f) << 4);
	
		r = (r | (r >> 4));
		g = (g | (g >> 4));
		b = (b | (b >> 4));
	
		palette_change_color((offset & 0x0ff), r, g, b);
	} };
	
	/******************************************************************************
	
	
	******************************************************************************/
	void gionbana_radrx_w(int data)
	{
		gionbana_radrx = data;
	}
	
	void gionbana_radry_w(int data)
	{
		gionbana_radry = data;
	}
	
	void gionbana_sizex_w(int data)
	{
		gionbana_sizex = data;
	}
	
	void gionbana_sizey_w(int data)
	{
		gionbana_sizey = data;
	
		gionbana_gfxdraw();
	}
	
	void gionbana_gfxflag_w(int data)
	{
		static int gionbana_flipscreen_old = -1;
	
		gionbana_flipx = (data & 0x01) ? 1 : 0;
		gionbana_flipy = (data & 0x02) ? 1 : 0;
		gionbana_flipscreen = (data & 0x04) ? 0 : 1;
		gionbana_dispflag = (data & 0x08) ? 0 : 1;
	
		if (nb1413m3_type == NB1413M3_HANAMOMO)
		{
			gionbana_flipscreen ^= 1;
		}
	
		if (gionbana_flipscreen != gionbana_flipscreen_old)
		{
			if (gfxdraw_mode != 0) gionbana_vramflip(1);
			gionbana_vramflip(0);
			gionbana_screen_refresh = 1;
			gionbana_flipscreen_old = gionbana_flipscreen;
		}
	}
	
	void gionbana_drawx_w(int data)
	{
		gionbana_drawx = (data ^ 0xff) & 0xff;
	}
	
	void gionbana_drawy_w(int data)
	{
		gionbana_drawy = (data ^ 0xff) & 0xff;
	}
	
	void gionbana_scrolly_w(int data)
	{
		if (gionbana_flipscreen != 0) gionbana_scrolly1 = -2;
		else gionbana_scrolly1 = 0;
	
		if (gionbana_flipscreen != 0) gionbana_scrolly2 = (data ^ 0xff) & 0xff;
		else gionbana_scrolly2 = (data - 1) & 0xff;
	}
	
	void gionbana_vramsel_w(int data)
	{
		gionbana_vram = data;
	}
	
	void gionbana_romsel_w(int data)
	{
		gionbana_gfxrom = (data & 0x0f);
	
		if ((0x20000 * gionbana_gfxrom) > (memory_region_length(REGION_GFX1) - 1))
		{
	#ifdef MAME_DEBUG
			usrintf_showmessage("GFXROM BANK OVER!!");
	#endif
			gionbana_gfxrom = 0;
		}
	}
	
	void gionbana_paltblnum_w(int data)
	{
		gionbana_paltblnum = data;
	}
	
	public static ReadHandlerPtr gionbana_paltbl_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return gionbana_paltbl[offset];
	} };
	
	public static WriteHandlerPtr gionbana_paltbl_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		gionbana_paltbl[((gionbana_paltblnum & 0x7f) * 0x10) + (offset & 0x0f)] = data;
	} };
	
	/******************************************************************************
	
	
	******************************************************************************/
	void gionbana_vramflip(int vram)
	{
		int x, y;
		unsigned char color1, color2;
		UBytePtr vidram;
	
		vidram = vram ? gionbana_videoram1 : gionbana_videoram0;
	
		for (y = 0; y < (Machine.drv.screen_height / 2); y++)
		{
			for (x = 0; x < Machine.drv.screen_width; x++)
			{
				color1 = vidram[(y * Machine.drv.screen_width) + x];
				color2 = vidram[((y ^ 0xff) * Machine.drv.screen_width) + (x ^ 0x1ff)];
				vidram[(y * Machine.drv.screen_width) + x] = color2;
				vidram[((y ^ 0xff) * Machine.drv.screen_width) + (x ^ 0x1ff)] = color1;
			}
		}
	}
	
	static void gionbana_gfxdraw(void)
	{
		UBytePtr GFX = memory_region(REGION_GFX1);
	
		int x, y;
		int dx1, dx2, dy1, dy2;
		int startx, starty;
		int sizex, sizey;
		int skipx, skipy;
		int ctrx, ctry;
		int tflag1, tflag2;
		unsigned char color, color1, color2;
		unsigned char drawcolor1, drawcolor2;
		int gfxaddr;
	
		if (gionbana_flipx != 0)
		{
			gionbana_drawx -= (gionbana_sizex << 1);
			startx = gionbana_sizex;
			sizex = ((gionbana_sizex ^ 0xff) + 1);
			skipx = -1;
		}
		else
		{
			gionbana_drawx = (gionbana_drawx - gionbana_sizex);
			startx = 0;
			sizex = (gionbana_sizex + 1);
			skipx = 1;
		}
	
		if (gionbana_flipy != 0)
		{
			gionbana_drawy -= ((gionbana_sizey << 1) + 1);
			starty = gionbana_sizey;
			sizey = ((gionbana_sizey ^ 0xff) + 1);
			skipy = -1;
		}
		else
		{
			gionbana_drawy = (gionbana_drawy - gionbana_sizey - 1);
			starty = 0;
			sizey = (gionbana_sizey + 1);
			skipy = 1;
		}
	
		gfxaddr = ((gionbana_gfxrom << 17) + (gionbana_radry << 9) + (gionbana_radrx << 1));
	
		for (y = starty, ctry = sizey; ctry > 0; y += skipy, ctry--)
		{
			for (x = startx, ctrx = sizex; ctrx > 0; x += skipx, ctrx--)
			{
				if ((gfxaddr > (memory_region_length(REGION_GFX1) - 1)))
				{
	#ifdef MAME_DEBUG
					usrintf_showmessage("GFXROM ADDRESS OVER!!");
	#endif
					gfxaddr = 0;
				}
	
				color = GFX[gfxaddr++];
	
				if (gionbana_flipscreen != 0)
				{
					dx1 = (((((gionbana_drawx + x) * 2) + 0) ^ 0x1ff) & 0x1ff);
					dx2 = (((((gionbana_drawx + x) * 2) + 1) ^ 0x1ff) & 0x1ff);
					dy1 = (((gionbana_drawy + y) ^ 0xff) & 0xff);
					dy2 = (((gionbana_drawy + y + (gionbana_scrolly2 & 0xff) + 2) ^ 0xff) & 0xff);
				}
				else
				{
					dx1 = ((((gionbana_drawx + x) * 2) + 0) & 0x1ff);
					dx2 = ((((gionbana_drawx + x) * 2) + 1) & 0x1ff);
					dy1 = ((gionbana_drawy + y) & 0xff);
					dy2 = ((gionbana_drawy + y + (-gionbana_scrolly2 & 0xff)) & 0xff);
				}
	
				if (gionbana_flipx != 0)
				{
					// flip
					color1 = (color & 0xf0) >> 4;
					color2 = (color & 0x0f) >> 0;
				}
				else
				{
					// normal
					color1 = (color & 0x0f) >> 0;
					color2 = (color & 0xf0) >> 4;
				}
	
				drawcolor1 = gionbana_paltbl[((gionbana_paltblnum & 0x7f) << 4) + color1];
				drawcolor2 = gionbana_paltbl[((gionbana_paltblnum & 0x7f) << 4) + color2];
	
				if (gfxdraw_mode != 0)
				{
					if ((gionbana_vram & 0x01) != 0)
					{
						tflag1 = (drawcolor1 != 0xff) ? 1 : 0;
						tflag2 = (drawcolor2 != 0xff) ? 1 : 0;
					}
					else
					{
						if ((gionbana_vram & 0x08) != 0)
						{
							tflag1 = (drawcolor1 != 0xff) ? 1 : 0;
							tflag2 = (drawcolor2 != 0xff) ? 1 : 0;
						}
						else
						{
							tflag1 = tflag2 = 1;
						}
	
						if (drawcolor1 == 0x7f) drawcolor1 = 0xff;
						if (drawcolor2 == 0x7f) drawcolor2 = 0xff;
					}
				}
				else
				{
					tflag1 = (drawcolor1 != 0xff) ? 1 : 0;
					tflag2 = (drawcolor2 != 0xff) ? 1 : 0;
					gionbana_vram = 0x02;
				}
	
				nb1413m3_busyctr++;
	
				if (gfxdraw_mode != 0)
				{
					if ((gionbana_vram & 0x01) != 0)
					{
						if (tflag1 != 0)
						{
							gionbana_videoram0[(dy1 * Machine.drv.screen_width) + dx1] = drawcolor1;
							plot_pixel(gionbana_tmpbitmap0, dx1, dy1, Machine.pens[drawcolor1]);
						}
						if (tflag2 != 0)
						{
							gionbana_videoram0[(dy1 * Machine.drv.screen_width) + dx2] = drawcolor2;
							plot_pixel(gionbana_tmpbitmap0, dx2, dy1, Machine.pens[drawcolor2]);
						}
					}
					if ((gionbana_vram & 0x02) != 0)
					{
						if (tflag1 != 0)
						{
							gionbana_videoram1[(dy2 * Machine.drv.screen_width) + dx1] = drawcolor1;
							plot_pixel(gionbana_tmpbitmap1, dx1, dy2, Machine.pens[drawcolor1]);
						}
						if (tflag2 != 0)
						{
							gionbana_videoram1[(dy2 * Machine.drv.screen_width) + dx2] = drawcolor2;
							plot_pixel(gionbana_tmpbitmap1, dx2, dy2, Machine.pens[drawcolor2]);
						}
					}
				}
				else
				{
					if (tflag1 != 0)
					{
						gionbana_videoram0[(dy2 * Machine.drv.screen_width) + dx1] = drawcolor1;
						plot_pixel(gionbana_tmpbitmap0, dx1, dy2, Machine.pens[drawcolor1]);
					}
					if (tflag2 != 0)
					{
						gionbana_videoram0[(dy2 * Machine.drv.screen_width) + dx2] = drawcolor2;
						plot_pixel(gionbana_tmpbitmap0, dx2, dy2, Machine.pens[drawcolor2]);
					}
				}
			}
		}
	
		nb1413m3_busyflag = (nb1413m3_busyctr > 4650) ? 0 : 1;
	
	}
	
	/******************************************************************************
	
	
	******************************************************************************/
	public static VhStartPtr gionbana_vh_start = new VhStartPtr() { public int handler() 
	{
		if ((gionbana_tmpbitmap0 = bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height)) == 0) return 1;
		if ((gionbana_tmpbitmap1 = bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height)) == 0) return 1;
		if ((gionbana_videoram0 = malloc(Machine.drv.screen_width * Machine.drv.screen_height * sizeof(char))) == 0) return 1;
		if ((gionbana_videoram1 = malloc(Machine.drv.screen_width * Machine.drv.screen_height * sizeof(char))) == 0) return 1;
		if ((gionbana_palette = malloc(0x200 * sizeof(char))) == 0) return 1;
		if ((gionbana_paltbl = malloc(0x800 * sizeof(char))) == 0) return 1;
		memset(gionbana_videoram0, 0x00, (Machine.drv.screen_width * Machine.drv.screen_height * sizeof(char)));
		memset(gionbana_videoram1, 0x00, (Machine.drv.screen_width * Machine.drv.screen_height * sizeof(char)));
		gfxdraw_mode = 1;
		return 0;
	} };
	
	public static VhStopPtr gionbana_vh_stop = new VhStopPtr() { public void handler() 
	{
		free(gionbana_paltbl);
		free(gionbana_palette);
		free(gionbana_videoram1);
		free(gionbana_videoram0);
		bitmap_free(gionbana_tmpbitmap1);
		bitmap_free(gionbana_tmpbitmap0);
		gionbana_paltbl = 0;
		gionbana_palette = 0;
		gionbana_videoram1 = 0;
		gionbana_videoram0 = 0;
		gionbana_tmpbitmap1 = 0;
		gionbana_tmpbitmap0 = 0;
	} };
	
	public static VhStartPtr hanamomo_vh_start = new VhStartPtr() { public int handler() 
	{
		if ((gionbana_tmpbitmap0 = bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height)) == 0) return 1;
		if ((gionbana_videoram0 = malloc(Machine.drv.screen_width * Machine.drv.screen_height * sizeof(char))) == 0) return 1;
		if ((gionbana_palette = malloc(0x200 * sizeof(char))) == 0) return 1;
		if ((gionbana_paltbl = malloc(0x800 * sizeof(char))) == 0) return 1;
		memset(gionbana_videoram0, 0x00, (Machine.drv.screen_width * Machine.drv.screen_height * sizeof(char)));
		gfxdraw_mode = 0;
		return 0;
	} };
	
	public static VhStopPtr hanamomo_vh_stop = new VhStopPtr() { public void handler() 
	{
		free(gionbana_paltbl);
		free(gionbana_palette);
		free(gionbana_videoram0);
		bitmap_free(gionbana_tmpbitmap0);
		gionbana_paltbl = 0;
		gionbana_palette = 0;
		gionbana_videoram0 = 0;
		gionbana_tmpbitmap0 = 0;
	} };
	
	/******************************************************************************
	
	
	******************************************************************************/
	public static VhUpdatePtr gionbana_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int x, y;
		unsigned char color;
	
		if (palette_recalc() || full_refresh || gionbana_screen_refresh)
		{
			gionbana_screen_refresh = 0;
			for (y = 0; y < Machine.drv.screen_height; y++)
			{
				for (x = 0; x < Machine.drv.screen_width; x++)
				{
					color = gionbana_videoram0[(y * Machine.drv.screen_width) + x];
					plot_pixel(gionbana_tmpbitmap0, x, y, Machine.pens[color]);
				}
			}
			if (gfxdraw_mode != 0)
			{
				for (y = 0; y < Machine.drv.screen_height; y++)
				{
					for (x = 0; x < Machine.drv.screen_width; x++)
					{
						color = gionbana_videoram1[(y * Machine.drv.screen_width) + x];
						plot_pixel(gionbana_tmpbitmap1, x, y, Machine.pens[color]);
					}
				}
			}
		}
	
		if (gionbana_dispflag != 0)
		{
			if (gfxdraw_mode != 0)
			{
				copyscrollbitmap(bitmap, gionbana_tmpbitmap0, 0, 0, 1, &gionbana_scrolly1, &Machine.visible_area, TRANSPARENCY_NONE, 0);
				copyscrollbitmap(bitmap, gionbana_tmpbitmap1, 0, 0, 1, &gionbana_scrolly2, &Machine.visible_area, TRANSPARENCY_PEN, Machine.pens[0xff]);
			}
			else
			{
				copyscrollbitmap(bitmap, gionbana_tmpbitmap0, 0, 0, 1, &gionbana_scrolly2, &Machine.visible_area, TRANSPARENCY_NONE, 0);
			}
		}
		else
		{
			fillbitmap(bitmap, Machine.pens[0xff], 0);
		}
	} };
}
