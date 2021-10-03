/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.vidhrdw;

import static common.ptr.*;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
import gr.codebb.arcadeflex.v037b16.mame.osdependH.osd_bitmap;
import static mame037b16.mame.Machine;
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static common.libc.cstring.*;
import static mame037b16.drawgfx.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b16.mame.memoryH.*;

public class terracre
{
	
	
	public static UBytePtr /*data16_t*/ terrac_videoram2 = new UBytePtr();
	public static int[] terrac_videoram2_size = new int[1];
	public static UBytePtr /*data16_t*/ terrac_scrolly = new UBytePtr();
	
	static osd_bitmap tmpbitmap2;
	static UBytePtr dirtybuffer2=new UBytePtr();
	
	static UBytePtr spritepalettebank = new UBytePtr();
	
	
	/***************************************************************************
	  Convert color prom.
	***************************************************************************/
        public static int TOTAL_COLORS(int gfxn){ return (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity); }
	public static void COLOR(char []colortable, int gfxn, int offs, int value){ (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])=(char) value; }
	
	public static VhConvertColorPromPtr terrac_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
                int _palette=0;
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3;
	
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			palette[_palette++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
			bit0 = (color_prom.read(Machine.drv.total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(Machine.drv.total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(Machine.drv.total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(Machine.drv.total_colors)>> 3) & 0x01;
			palette[_palette++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
			bit0 = (color_prom.read(2*Machine.drv.total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(2*Machine.drv.total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(2*Machine.drv.total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(2*Machine.drv.total_colors)>> 3) & 0x01;
			palette[_palette++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
	
			color_prom.inc();
		}
	
		color_prom.inc( 2*Machine.drv.total_colors );
		/* color_prom now points to the beginning of the lookup table */
	
	
		/* characters use colors 0-15 */
		for (i = 0;i < TOTAL_COLORS(0);i++)
			COLOR(colortable, 0,i, i);
	
		/* background tiles use colors 192-255 in four banks */
		/* the bottom two bits of the color code select the palette bank for */
		/* pens 0-7; the top two bits for pens 8-15. */
		for (i = 0;i < TOTAL_COLORS(1);i++)
		{
			if ((i & 8) != 0) COLOR(colortable, 1,i, 192 + (i & 0x0f) + ((i & 0xc0) >> 2));
			else COLOR(colortable, 1,i, 192 + (i & 0x0f) + ((i & 0x30) >> 0));
		}
	
		/* sprites use colors 128-191 in four banks */
		/* The lookup table tells which colors to pick from the selected bank */
		/* the bank is selected by another PROM and depends on the top 8 bits of */
		/* the sprite code. The PROM selects the bank *separately* for pens 0-7 and */
		/* 8-15 (like for tiles). */
		for (i = 0;i < TOTAL_COLORS(2)/16;i++)
		{
			int j;
	
			for (j = 0;j < 16;j++)
			{
				if ((i & 8) != 0)
					COLOR(colortable, 2,i + j * (TOTAL_COLORS(2)/16), 128 + ((j & 0x0c) << 2) + (color_prom.read() & 0x0f));
				else
					COLOR(colortable, 2,i + j * (TOTAL_COLORS(2)/16), 128 + ((j & 0x03) << 4) + (color_prom.read() & 0x0f));
			}
	
			color_prom.inc();
		}
	
		/* color_prom now points to the beginning of the sprite palette bank table */
		spritepalettebank = color_prom;	/* we'll need it at run time */
	} };
	
        public static ReadHandlerPtr terrac_videoram2_r = new ReadHandlerPtr() {
            @Override
            public int handler(int offset) {
                return terrac_videoram2.read(offset);
            }
        };
	
	
	//WRITE16_HANDLER( terrac_videoram2_w )
        public static WriteHandlerPtr terrac_videoram2_w = new WriteHandlerPtr() {
            @Override
            public void handler(int offset, int data) {
                int oldword = terrac_videoram2.read(offset);
		COMBINE_DATA(oldword, terrac_videoram2.read(offset));
		if (oldword != terrac_videoram2.read(offset))
		{
			dirtybuffer2.write(offset, 1);
		}
            }
        };
        
	/***************************************************************************
	  Stop the video hardware emulation.
	***************************************************************************/
	
	public static VhStopPtr terrac_vh_stop = new VhStopPtr() { public void handler() 
	{
		dirtybuffer2 = null;
		bitmap_free(tmpbitmap2);
		generic_vh_stop.handler();
	} };
	
	/***************************************************************************
	  Start the video hardware emulation.
	***************************************************************************/
	
	
	public static VhStartPtr terrac_vh_start = new VhStartPtr() { public int handler() 
	{
		if (generic_vh_start.handler() != 0)
			return 1;
	
		if ((dirtybuffer2 = new UBytePtr(terrac_videoram2_size[0]/2)) == null)
		{
			terrac_vh_stop.handler();
			return 1;
		}
		memset(dirtybuffer2,1,terrac_videoram2_size[0]/2);
	
		/* the background area is 4 x 1 (90 Rotated!) */
		if ((tmpbitmap2 = bitmap_alloc(4*Machine.drv.screen_width,
				1*Machine.drv.screen_height)) == null)
		{
			terrac_vh_stop.handler();
			return 1;
		}
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr terracre_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs,x,y;
	
	
		for (y = 0; y < 64; y++)
		{
			for (x = 0; x < 16; x++)
			{
				if (dirtybuffer2.read(x + y*32) != 0)
				{
					int code = terrac_videoram2.read(x + y*32) & 0x01ff;
					int color = (terrac_videoram2.read(x + y*32)&0x7800)>>11;
	
					dirtybuffer2.write(x + y*32, 0);
	
					drawgfx(tmpbitmap2,Machine.gfx[1],
							code,
							color,
							0,0,
							16 * y,16 * x,
							null,TRANSPARENCY_NONE,0);
				}
			}
		}
	
		/* copy the background graphics */
		if ((terrac_scrolly.read() & 0x2000) != 0)	/* background disable */
			fillbitmap(bitmap,Machine.pens[0],Machine.visible_area);
		else
		{
			int scrollx;
	
			scrollx = -terrac_scrolly.read();
	
			copyscrollbitmap(bitmap,tmpbitmap2,1,new int[]{scrollx},0,null,Machine.visible_area,TRANSPARENCY_NONE,0);
		}
	
	
	
		for (x = 0;x <spriteram_size[0]/2;x += 4)
		{
			int code;
			int attr = spriteram.read(x+2) & 0xff;//spriteram16[x+2] & 0xff;
			int color = (attr & 0xf0) >> 4;
			int flipx = attr & 0x04;
			int flipy = attr & 0x08;
			int sx,sy;
	
			sx = (spriteram.read(x+3) & 0xff) - 0x80 + 256 * (attr & 1); //(spriteram16[x+3] & 0xff) - 0x80 + 256 * (attr & 1);
			sy = 240 - (spriteram.read(x) & 0xff); //240 - (spriteram16[x] & 0xff);
	
			code = (spriteram.read(x+1) & 0xff) + ((attr & 0x02) << 7); //(spriteram16[x+1] & 0xff) + ((attr & 0x02) << 7);
	
			drawgfx(bitmap,Machine.gfx[2],
					code,
					color + 16 * (spritepalettebank.read(code >> 1) & 0x0f),
					flipx,flipy,
					sx,sy,
					Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	
	
		for (offs = videoram_size[0]/2 - 1;offs >= 0;offs--)
		{
			int sx,sy;
	
	
			sx = offs / 32;
			sy = offs % 32;
	
			drawgfx(bitmap,Machine.gfx[0],
					videoram.read(offs) & 0xff,//videoram16[offs] & 0xff,
					0,
					0,0,
					8*sx,8*sy,
					Machine.visible_area,TRANSPARENCY_PEN,15);
		}
	} };
}
