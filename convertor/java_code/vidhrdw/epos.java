/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class epos
{
	
	
	static int current_palette;
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  These games has one 32 byte palette PROM, connected to the RGB output this way:
	
	  bit 7 -- 240 ohm resistor  -- RED
	        -- 510 ohm resistor  -- RED
	        -- 1  kohm resistor  -- RED
	        -- 240 ohm resistor  -- GREEN
	        -- 510 ohm resistor  -- GREEN
	        -- 1  kohm resistor  -- GREEN
	        -- 240 ohm resistor  -- BLUE
	  bit 0 -- 510 ohm resistor  -- BLUE
	
	***************************************************************************/
	public static VhConvertColorPromPtr epos_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
	
		for (i = 0; i < Machine.drv.total_colors; i++)
		{
			int bit0,bit1,bit2;
	
			/* red component */
			bit0 = (*color_prom >> 7) & 0x01;
			bit1 = (*color_prom >> 6) & 0x01;
			bit2 = (*color_prom >> 5) & 0x01;
			*(palette++) = 0x92 * bit0 + 0x4a * bit1 + 0x23 * bit2;
			/* green component */
			bit0 = (*color_prom >> 4) & 0x01;
			bit1 = (*color_prom >> 3) & 0x01;
			bit2 = (*color_prom >> 2) & 0x01;
			*(palette++) = 0x92 * bit0 + 0x4a * bit1 + 0x23 * bit2;
			/* blue component */
			bit0 = (*color_prom >> 1) & 0x01;
			bit1 = (*color_prom >> 0) & 0x01;
			*(palette++) = 0xad * bit0 + 0x52 * bit1;
	
			color_prom++;
		}
	} };
	
	
	public static WriteHandlerPtr epos_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int x,y;
	
		videoram.write(offset,data);

		x = (offset % 136) * 2;
		y = (offset / 136);
	
		plot_pixel(Machine.scrbitmap, x,     y, Machine.pens[current_palette | (data & 0x0f)]);
		plot_pixel(Machine.scrbitmap, x + 1, y, Machine.pens[current_palette | (data >> 4)]);
	} };
	
	
	public static WriteHandlerPtr epos_port_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* D0 - start light #1
		   D1 - start light #2
		   D2 - coin counter
		   D3 - palette select
		   D4-D7 - unused
		 */
	
		set_led_status(0, data & 1);
		set_led_status(1, data & 2);
	
		coin_counter_w.handler(0, data & 4);
	
		if (current_palette != ((data & 8) << 1))
		{
			current_palette = (data & 8) << 1;
	
			schedule_full_refresh();
		}
	} };
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  To be used by bitmapped games not using sprites.
	
	***************************************************************************/
	public static VhUpdatePtr epos_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		if (full_refresh != 0)
		{
			/* redraw bitmap */
	
			int offs;
	
			for (offs = 0; offs < videoram_size[0]; offs++)
			{
				epos_videoram_w(offs, videoram.read(offs));
			}
		}
	} };
}
