/******************************************************************************

Markham (c) 1983 Sun Electronics

Video hardware driver by Uki

	17/Jun/2001 -

******************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class markham
{
	
	static UINT8 flipscreen,markham_xscroll[2];
	
	public static VhConvertColorPromPtr markham_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			*(palette++) = color_prom.read(0)*0x11;
			*(palette++) = color_prom.read(Machine->drv->total_colors)*0x11;
			*(palette++) = color_prom.read(2*Machine->drv->total_colors)*0x11;
	
			color_prom++;
		}
	
		color_prom += 2*Machine.drv.total_colors;
	
		/* color_prom now points to the beginning of the lookup table */
	
		/* sprites lookup table */
		for (i=0; i<512; i++)
			*(colortable++) = *(color_prom++);
	
		/* bg lookup table */
		for (i=0; i<512; i++)
			*(colortable++) = *(color_prom++);
	
	} };
	
	public static WriteHandlerPtr markham_scroll_x_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		markham_xscroll[offset] = data;
	} };
	
	public static WriteHandlerPtr markham_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		flipscreen = data & 1;
	} };
	
	public static VhUpdatePtr markham_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
	
		int offs,chr,col,x,y,px,py,fx,fy,bank ;
	
		/* draw bg layer */
	
		for (offs=0; offs<(videoram_size[0]/2); offs++)
		{
			int sx,sy;
	
			sx = offs / 32;
			sy = offs % 32;
	
			py = sy*8;
			px = sx*8;
	
			if ( (sy > 3) && (sy<16) )
				px = ((sx*8 + ~markham_xscroll[0]) & 0xff);
			if (sy>=16)
				px = ((sx*8 + ~markham_xscroll[1]) & 0xff);
	
			if (flipscreen != 0)
			{
				px = 248-px;
				py = 248-py;
			}
	
			col = videoram.read(offs*2);
			fx = flipscreen;
			fy = flipscreen;
			bank = (col & 0x60) << 3;
			col = (col & 0x1f) | ((col & 0x80) >> 2);
	
			drawgfx(bitmap,Machine.gfx[0],
				videoram.read(offs*2+1)+ bank,
				col,
				fx,fy,
				px,py,
				&Machine.visible_area,TRANSPARENCY_NONE,0);
		}
	
	/* draw sprites */
	
		/* c860 - c8ff */
		for (offs=0x60; offs<0x100; offs +=4)
		{
			chr = spriteram.read(offs+1);
			col = spriteram.read(offs+2);
	
			fx = flipscreen;
			fy = flipscreen;
	
			x = spriteram.read(offs+3);
			y = spriteram.read(offs+0);
	
			col &= 0x3f ;
	
			if (flipscreen==0)
			{
				px = x-2;
				py = 240-y;
			}
			else
			{
				px = 240-x;
				py = y;
			}
	
			px = px & 0xff;
	
			if (px>248)
				px = px-256;
	
			drawgfx(bitmap,Machine.gfx[1],
				chr,
				col,
				fx,fy,
				px,py,
				&Machine.visible_area,TRANSPARENCY_COLOR,0);
		}
	
	} };
}
