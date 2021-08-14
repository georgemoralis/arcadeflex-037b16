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
import common.ptr.UBytePtr;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapC.*;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import gr.codebb.arcadeflex.v037b16.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.generic.*;
import static mame037b16.drawgfx.drawgfx;
import static mame037b16.mame.Machine;
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
import static gr.codebb.arcadeflex.v037b16.mame.palette.palette_recalc;

public class gng
{
	
	public static UBytePtr gng_fgvideoram = new UBytePtr();
	public static UBytePtr gng_bgvideoram = new UBytePtr();
	
	static struct_tilemap bg_tilemap, fg_tilemap;
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoPtr get_fg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int attr = gng_fgvideoram.read(tile_index + 0x400);
		SET_TILE_INFO(
				0,
				gng_fgvideoram.read(tile_index) + ((attr & 0xc0) << 2),
				attr & 0x0f,
				TILE_FLIPYX((attr & 0x30) >> 4));
	} };
	
	public static GetTileInfoPtr get_bg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int attr = gng_bgvideoram.read(tile_index + 0x400);
		SET_TILE_INFO(
				1,
				gng_bgvideoram.read(tile_index) + ((attr & 0xc0) << 2),
				attr & 0x07,
				TILE_FLIPYX((attr & 0x30) >> 4) | TILE_SPLIT((attr & 0x08) >> 3));
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr gng_vh_start = new VhStartPtr() { public int handler() 
	{
		fg_tilemap = tilemap_create(get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
		bg_tilemap = tilemap_create(get_bg_tile_info,tilemap_scan_cols,TILEMAP_SPLIT,    16,16,32,32);
	
		if (fg_tilemap==null || bg_tilemap==null)
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap,3);
	
		tilemap_set_transmask(bg_tilemap,0,0xff,0x00); /* split type 0 is totally transparent in front half */
		tilemap_set_transmask(bg_tilemap,1,0x41,0xbe); /* split type 1 has pens 0 and 6 transparent in front half */
	
		return 0;
	} };
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr gng_fgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		gng_fgvideoram.write(offset, data);
		tilemap_mark_tile_dirty(fg_tilemap,offset & 0x3ff);
	} };
	
	public static WriteHandlerPtr gng_bgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		gng_bgvideoram.write(offset, data);
		tilemap_mark_tile_dirty(bg_tilemap,offset & 0x3ff);
	} };
	
        static char[] scrollx = new char[2];
	
	public static WriteHandlerPtr gng_bgscrollx_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{		
		scrollx[offset] = (char) data;
		tilemap_set_scrollx( bg_tilemap, 0, scrollx[0] + 256 * scrollx[1] );
	} };
        
        static char[] scrolly = new char[2];
	
	public static WriteHandlerPtr gng_bgscrolly_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		
		scrolly[offset] = (char) data;
		tilemap_set_scrolly( bg_tilemap, 0, scrolly[0] + 256 * scrolly[1] );
	} };
	
	
	public static WriteHandlerPtr gng_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		flip_screen_set(~data & 1);
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites(osd_bitmap bitmap)
	{
		GfxElement gfx = Machine.gfx[2];
		rectangle clip = new rectangle(Machine.visible_area);
		int offs;
	
	
		for (offs = spriteram_size[0] - 4;offs >= 0;offs -= 4)
		{
			char attributes = buffered_spriteram.read(offs+1);
			int sx = buffered_spriteram.read(offs + 3)- 0x100 * (attributes & 0x01);
			int sy = buffered_spriteram.read(offs + 2);
			int flipx = attributes & 0x04;
			int flipy = attributes & 0x08;
	
			if (flip_screen() != 0)
			{
				sx = 240 - sx;
				sy = 240 - sy;
				flipx = NOT(flipx);
				flipy = NOT(flipy);
			}
	
			drawgfx(bitmap,gfx,
					buffered_spriteram.read(offs)+ ((attributes<<2) & 0x300),
					(attributes >> 4) & 3,
					flipx,flipy,
					sx,sy,
					clip,TRANSPARENCY_PEN,15);
		}
	}
	
	public static VhUpdatePtr gng_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update(ALL_TILEMAPS);
	
		palette_recalc();
	
		tilemap_draw(bitmap,bg_tilemap,TILEMAP_BACK,0);
		draw_sprites(bitmap);
		tilemap_draw(bitmap,bg_tilemap,TILEMAP_FRONT,0);
		tilemap_draw(bitmap,fg_tilemap,0,0);
	} };
	
	public static VhEofCallbackPtr gng_eof_callback = new VhEofCallbackPtr() { public void handler() 
	{
		buffer_spriteram_w.handler(0,0);
	} };
}
