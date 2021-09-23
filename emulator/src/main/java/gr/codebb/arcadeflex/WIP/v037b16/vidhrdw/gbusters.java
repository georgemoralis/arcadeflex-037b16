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
import gr.codebb.arcadeflex.v037b16.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b16.mame.palette.*;
import static gr.codebb.arcadeflex.v037b16.mame.paletteH.*;
import static mame037b7.palette.palette_init_used_colors;

public class gbusters
{
	
	public static int gbusters_priority, sprite_colorbase;
	static int[] layer_colorbase=new int[3];
	
	/***************************************************************************
	
	  Callbacks for the K052109
	
	***************************************************************************/
	
	public static K052109_callbackProcPtr tile_callback = new K052109_callbackProcPtr() { public void handler(int layer,int bank,int[] code,int[] color) 
	{
		/* (color & 0x02) is flip y handled internally by the 052109 */
		code[0] |= ((color[0] & 0x0d) << 8) | ((color[0] & 0x10) << 5) | (bank << 12);
		color[0] = layer_colorbase[layer] + ((color[0] & 0xe0) >> 5);
	} };
	
	/***************************************************************************
	
	  Callbacks for the K051960
	
	***************************************************************************/
	
	static K051960_callbackProcPtr sprite_callback = new K051960_callbackProcPtr() {
            @Override
            public void handler(int[] code, int[] color, int[] priority, int[] shadow) {
                priority[0] = (color[0] & 0x30) >> 4;
		color[0] = sprite_colorbase + (color[0] & 0x0f);
            }
        };
        
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr gbusters_vh_start = new VhStartPtr() { public int handler() 
	{
		layer_colorbase[0] = 48;
		layer_colorbase[1] = 0;
		layer_colorbase[2] = 16;
		sprite_colorbase = 32;
	
		if (K052109_vh_start(REGION_GFX1,0, 1, 2, 3/*NORMAL_PLANE_ORDER*/,tile_callback) != 0)
			return 1;
		if (K051960_vh_start(REGION_GFX2,0, 1, 2, 3/*NORMAL_PLANE_ORDER*/,sprite_callback) != 0)
		{
			K052109_vh_stop.handler();
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopPtr gbusters_vh_stop = new VhStopPtr() { public void handler() 
	{
		K052109_vh_stop.handler();
		K051960_vh_stop.handler();
	} };
	
	
	public static VhUpdatePtr gbusters_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		K052109_tilemap_update();
	
		palette_init_used_colors();
		K051960_mark_sprites_colors();
		palette_recalc();
	
		/* sprite priority 3 = disable */
		if (gbusters_priority != 0)
		{
	//		K051960_sprites_draw(bitmap,1,1);	/* are these used? */
			K052109_tilemap_draw(bitmap,2,TILEMAP_IGNORE_TRANSPARENCY,0);
			K051960_sprites_draw(bitmap,2,2);
			K052109_tilemap_draw(bitmap,1,0,0);
			K051960_sprites_draw(bitmap,0,0);
			K052109_tilemap_draw(bitmap,0,0,0);
		}
		else
		{
	//		K051960_sprites_draw(bitmap,1,1);	/* are these used? */
			K052109_tilemap_draw(bitmap,1,TILEMAP_IGNORE_TRANSPARENCY,0);
			K051960_sprites_draw(bitmap,2,2);
			K052109_tilemap_draw(bitmap,2,0,0);
			K051960_sprites_draw(bitmap,0,0);
			K052109_tilemap_draw(bitmap,0,0,0);
		}
	} };
}
