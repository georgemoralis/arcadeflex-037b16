/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.vidhrdw;

import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapC.*;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.*;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b16.mame.palette.*;
import static gr.codebb.arcadeflex.v037b16.mame.paletteH.*;
import gr.codebb.arcadeflex.v037b16.mame.osdependH.osd_bitmap;
import static mame037b16.drawgfx.*;

public class mainevt
{
	
	
	static int[] layer_colorbase=new int[3];
        static int sprite_colorbase;
	
	
	
	/***************************************************************************
	
	  Callbacks for the K052109
	
	***************************************************************************/
	
	public static K052109_callbackProcPtr mainevt_tile_callback = new K052109_callbackProcPtr() { public void handler(int layer,int bank,int[] code,int[] color) 
	{
		tile_info.flags = (color[0] & 0x02)!=0 ? TILE_FLIPX : 0;
	
		/* priority relative to HALF priority sprites */
		if (layer == 2) tile_info.priority = (color[0] & 0x20) >> 5;
		else tile_info.priority = 0;
	
		code[0] |= ((color[0] & 0x01) << 8) | ((color[0] & 0x1c) << 7);
		color[0] = layer_colorbase[layer] + ((color[0] & 0xc0) >> 6);
	} };
	
	public static K052109_callbackProcPtr dv_tile_callback = new K052109_callbackProcPtr() { public void handler(int layer,int bank,int[] code,int[] color) 
	{
		/* (color & 0x02) is flip y handled internally by the 052109 */
		code[0] |= ((color[0] & 0x01) << 8) | ((color[0] & 0x3c) << 7);
		color[0] = layer_colorbase[layer] + ((color[0] & 0xc0) >> 6);
	} };
	
	
	/***************************************************************************
	
	  Callbacks for the K051960
	
	***************************************************************************/
	
	public static K051960_callbackProcPtr mainevt_sprite_callback = new K051960_callbackProcPtr() { public void handler(int[] code,int[] color,int[] priority_mask, int[] shadow) 
	{
		/* bit 5 = priority over layer B (has precedence) */
		/* bit 6 = HALF priority over layer B (used for crowd when you get out of the ring) */
		if ((color[0] & 0x20)!=0)		priority_mask[0] = 0xff00;
		else if ((color[0] & 0x40)!=0)	priority_mask[0] = 0xff00|0xf0f0;
		else					priority_mask[0] = 0xff00|0xf0f0|0xcccc;
		/* bit 7 is shadow, not used */
	
		color[0] = sprite_colorbase + (color[0] & 0x03);
	} };
	
	static K051960_callbackProcPtr dv_sprite_callback = new K051960_callbackProcPtr() {
            @Override
            public void handler(int[] code, int[] color, int[] priority, int[] shadow) {
                /* TODO: the priority/shadow handling (bits 5-7) seems to be quite complex (see PROM) */
		color[0] = sprite_colorbase + (color[0] & 0x07);
            }
        };
        	
	/*****************************************************************************/
	
	public static VhStartPtr mainevt_vh_start = new VhStartPtr() { public int handler() 
	{
		layer_colorbase[0] = 0;
		layer_colorbase[1] = 8;
		layer_colorbase[2] = 4;
		sprite_colorbase = 12;
	
		if (K052109_vh_start(REGION_GFX1,0, 1, 2, 3/*NORMAL_PLANE_ORDER*/,mainevt_tile_callback)!=0)
			return 1;
		if (K051960_vh_start(REGION_GFX2,0, 1, 2, 3/*NORMAL_PLANE_ORDER*/,mainevt_sprite_callback)!=0)
		{
			K052109_vh_stop.handler();
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStartPtr dv_vh_start = new VhStartPtr() { public int handler() 
	{
		layer_colorbase[0] = 0;
		layer_colorbase[1] = 0;
		layer_colorbase[2] = 4;
		sprite_colorbase = 8;
	
		if (K052109_vh_start(REGION_GFX1,0, 1, 2, 3/*NORMAL_PLANE_ORDER*/,dv_tile_callback)!=0)
			return 1;
		if (K051960_vh_start(REGION_GFX2,0, 1, 2, 3/*NORMAL_PLANE_ORDER*/,dv_sprite_callback)!=0)
		{
			K052109_vh_stop.handler();
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopPtr mainevt_vh_stop = new VhStopPtr() { public void handler() 
	{
		K052109_vh_stop.handler();
		K051960_vh_stop.handler();
	} };
	
	/*****************************************************************************/
	
	public static VhUpdatePtr mainevt_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		K052109_tilemap_update();
	
		palette_recalc();
	
		fillbitmap(priority_bitmap,0,null);
		K052109_tilemap_draw(bitmap,1,TILEMAP_IGNORE_TRANSPARENCY,1);
		K052109_tilemap_draw(bitmap,2,1,2);	/* low priority part of layer */
		K052109_tilemap_draw(bitmap,2,0,4);	/* high priority part of layer */
		K052109_tilemap_draw(bitmap,0,0,8);
	
		K051960_sprites_draw(bitmap,-1,-1);
	} };
	
	public static VhUpdatePtr dv_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		K052109_tilemap_update();
	
		palette_recalc();
	
		K052109_tilemap_draw(bitmap,1,TILEMAP_IGNORE_TRANSPARENCY,0);
		K052109_tilemap_draw(bitmap,2,0,0);
		K051960_sprites_draw(bitmap,0,0);
		K052109_tilemap_draw(bitmap,0,0,0);
	} };
}
