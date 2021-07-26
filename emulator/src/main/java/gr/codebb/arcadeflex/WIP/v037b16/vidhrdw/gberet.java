/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.vidhrdw;

import static common.libc.expressions.NOT;
import static common.ptr.*;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapC.*;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import gr.codebb.arcadeflex.v037b16.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b16.vidhrdw.generic.*;
import static mame037b16.drawgfx.drawgfx;
import static mame037b16.mame.Machine;

public class gberet
{
	
	public static UBytePtr gberet_videoram=new UBytePtr(), gberet_colorram=new UBytePtr();
	public static UBytePtr gberet_spritebank=new UBytePtr();
	public static UBytePtr gberet_scrollram=new UBytePtr();
	public static struct_tilemap bg_tilemap;
	static int interruptenable;
	static int flipscreen;
	static int sprites_type;
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Green Beret has a 32 bytes palette PROM and two 256 bytes color lookup table
	  PROMs (one for sprites, one for characters).
	  The palette PROM is connected to the RGB output, this way:
	
	  bit 7 -- 220 ohm resistor  -- BLUE
	        -- 470 ohm resistor  -- BLUE
	        -- 220 ohm resistor  -- GREEN
	        -- 470 ohm resistor  -- GREEN
	        -- 1  kohm resistor  -- GREEN
	        -- 220 ohm resistor  -- RED
	        -- 470 ohm resistor  -- RED
	  bit 0 -- 1  kohm resistor  -- RED
	
	***************************************************************************/
	public static int TOTAL_COLORS(int gfxn) { return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity; }
	public static void COLOR(char [] colortable, int gfxn, int offs, int value) {colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs]=(char) value; }
	        
	public static VhConvertColorPromPtr gberet_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		int _palette=0;
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2;
	
	
			bit0 = (color_prom.read() >> 0) & 0x01;
			bit1 = (color_prom.read() >> 1) & 0x01;
			bit2 = (color_prom.read() >> 2) & 0x01;
			palette[_palette++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
			bit0 = (color_prom.read() >> 3) & 0x01;
			bit1 = (color_prom.read() >> 4) & 0x01;
			bit2 = (color_prom.read() >> 5) & 0x01;
			palette[_palette++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
			bit0 = 0;
			bit1 = (color_prom.read() >> 6) & 0x01;
			bit2 = (color_prom.read() >> 7) & 0x01;
			palette[_palette++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
	
			color_prom.inc();
		}
	
		for (i = 0;i < TOTAL_COLORS(1);i++)
		{
			if ((color_prom.read() & 0x0f) != 0) COLOR(colortable,1,i, color_prom.read() & 0x0f);
			else COLOR(colortable,1,i, 0);
			color_prom.inc();
		}
		for (i = 0;i < TOTAL_COLORS(0);i++)
		{
			COLOR(colortable,0,i, (color_prom.readinc() & 0x0f) + 0x10);
		}
	} };
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoPtr get_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int attr = gberet_colorram.read(tile_index);
		SET_TILE_INFO(
				0,
				gberet_videoram.read(tile_index) + ((attr & 0x40) << 2),
				attr & 0x0f,
				TILE_FLIPYX((attr & 0x30) >> 4) | TILE_SPLIT((attr & 0x80) >> 7));
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr gberet_vh_start = new VhStartPtr() { public int handler() 
	{
		bg_tilemap = tilemap_create(get_tile_info,tilemap_scan_rows,TILEMAP_SPLIT,8,8,64,32);
	
		if (bg_tilemap==null)
			return 0;
	
		tilemap_set_transmask(bg_tilemap,0,0x0001,0xfffe); /* split type 0 has pen 1 transparent in front half */
		tilemap_set_transmask(bg_tilemap,1,0xffff,0x0000); /* split type 1 is totally transparent in front half */
		tilemap_set_scroll_rows(bg_tilemap,32);
	
		return 0;
	} };
	
	public static InitDriverPtr init_gberet = new InitDriverPtr() { public void handler() 
	{
		sprites_type = 0;
	} };
	
	public static InitDriverPtr init_gberetb = new InitDriverPtr() { public void handler() 
	{
		sprites_type = 1;
	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr gberet_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (gberet_videoram.read(offset) != data)
		{
			gberet_videoram.write(offset, data);
			tilemap_mark_tile_dirty(bg_tilemap,offset);
		}
	} };
	
	public static WriteHandlerPtr gberet_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (gberet_colorram.read(offset) != data)
		{
			gberet_colorram.write(offset, data);
			tilemap_mark_tile_dirty(bg_tilemap,offset);
		}
	} };
	
	public static WriteHandlerPtr gberet_e044_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* bit 0 enables interrupts */
		interruptenable = data & 1;
	
		/* bit 3 flips screen */
		flipscreen = data & 0x08;
		tilemap_set_flip(ALL_TILEMAPS,flipscreen!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	
		/* don't know about the other bits */
	} };
	
	public static WriteHandlerPtr gberet_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int scroll;
	
		gberet_scrollram.write(offset, data);
	
		scroll = gberet_scrollram.read(offset & 0x1f) | (gberet_scrollram.read(offset | 0x20) << 8);
		tilemap_set_scrollx(bg_tilemap,offset & 0x1f,scroll);
	} };
	
	public static WriteHandlerPtr gberetb_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int scroll;
	
		scroll = data;
		if (offset != 0) scroll |= 0x100;
	
		for (offset = 6;offset < 29;offset++)
			tilemap_set_scrollx(bg_tilemap,offset,scroll + 64-8);
	} };
	
	
	public static InterruptPtr gberet_interrupt = new InterruptPtr() { public int handler() 
	{
		if (cpu_getiloops() == 0) return interrupt.handler();
		else if ((cpu_getiloops() % 2) != 0)
		{
			if (interruptenable != 0) return nmi_interrupt.handler();
		}
	
		return ignore_interrupt.handler();
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites0(osd_bitmap bitmap)
	{
		int offs;
		UBytePtr sr;
	
		if ((gberet_spritebank.read() & 0x08) != 0)
			sr = spriteram_2;
		else sr = spriteram;
	
		for (offs = 0;offs < spriteram_size[0];offs += 4)
		{
			if (sr.read(offs+3) != 0)
			{
				int sx,sy,flipx,flipy;
	
	
				sx = sr.read(offs+2) - 2*(sr.read(offs+1) & 0x80);
				sy = sr.read(offs+3);
				if (sprites_type != 0) sy = 240 - sy;
				flipx = sr.read(offs+1) & 0x10;
				flipy = sr.read(offs+1) & 0x20;
	
				if (flipscreen != 0)
				{
					sx = 240 - sx;
					sy = 240 - sy;
					flipx = NOT(flipx);
					flipy = NOT(flipy);
				}
	
				drawgfx(bitmap,Machine.gfx[1],
						sr.read(offs+0) + ((sr.read(offs+1) & 0x40) << 2),
						sr.read(offs+1) & 0x0f,
						flipx,flipy,
						sx,sy,
						Machine.visible_area,TRANSPARENCY_COLOR,0);
			}
		}
	}
	
	static void draw_sprites1(osd_bitmap bitmap)
	{
		int offs;
		UBytePtr sr;
	
		sr = spriteram;
	
		for (offs = spriteram_size[0] - 4;offs >= 0;offs -= 4)
		{
			if (sr.read(offs+1) != 0)
			{
				int sx,sy,flipx,flipy;
	
	
				sx = sr.read(offs+2) - 2*(sr.read(offs+3) & 0x80);
				sy = sr.read(offs+1);
				if (sprites_type != 0) sy = 240 - sy;
				flipx = sr.read(offs+3) & 0x10;
				flipy = sr.read(offs+3) & 0x20;
	
				if (flipscreen != 0)
				{
					sx = 240 - sx;
					sy = 240 - sy;
					flipx = NOT(flipx);
					flipy = NOT(flipy);
				}
	
				drawgfx(bitmap,Machine.gfx[1],
						sr.read(offs+0) + ((sr.read(offs+3) & 0x40) << 2),
						sr.read(offs+3) & 0x0f,
						flipx,flipy,
						sx,sy,
						Machine.visible_area,TRANSPARENCY_COLOR,0);
			}
		}
	}
	
	
	public static VhUpdatePtr gberet_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update(ALL_TILEMAPS);
	
		tilemap_draw(bitmap,bg_tilemap,TILEMAP_BACK,0);
		if (sprites_type == 0) draw_sprites0(bitmap);	/* original */
		else draw_sprites1(bitmap);	/* bootleg */
		tilemap_draw(bitmap,bg_tilemap,TILEMAP_FRONT,0);
	} };
}
