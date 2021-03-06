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
import static mame037b7.palette.*;
import static gr.codebb.arcadeflex.v037b16.mame.palette.*;
import static gr.codebb.arcadeflex.v037b16.mame.paletteH.*;

public class _88games
{
	
	
	public static int k88games_priority;
	
	static int[] layer_colorbase = new int[3];
        static int sprite_colorbase,zoom_colorbase;
	
	
	
	/***************************************************************************
	
	  Callbacks for the K052109
	
	***************************************************************************/
	
	public static K052109_callbackProcPtr tile_callback = new K052109_callbackProcPtr() { public void handler(int layer,int bank,int[] code,int[] color) 
	{
		code[0] |= ((color[0] & 0x0f) << 8) | (bank << 12);
		color[0] = layer_colorbase[layer] + ((color[0] & 0xf0) >> 4);
	} };
	
	
	/***************************************************************************
	
	  Callbacks for the K051960
	
	***************************************************************************/
	
	static K051960_callbackProcPtr sprite_callback = new K051960_callbackProcPtr() {
            @Override
            public void handler(int[] code, int[] color, int[] priority, int[] shadow) {
                priority[0] = (color[0] & 0x20) >> 5;	/* ??? */
		color[0] = sprite_colorbase + (color[0] & 0x0f);
            }
        };
        
	
	/***************************************************************************
	
	  Callbacks for the K051316
	
	***************************************************************************/
	
	static K051316_callbackProcPtr zoom_callback = new K051316_callbackProcPtr() {
            @Override
            public void handler(int[] code, int[] color) {
                tile_info.flags = (color[0] & 0x40)!=0 ? TILE_FLIPX : 0;
		code[0] |= ((color[0] & 0x07) << 8);
		color[0] = zoom_colorbase + ((color[0] & 0x38) >> 3) + ((color[0] & 0x80) >> 4);
            }
        };
	
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr k88games_vh_start = new VhStartPtr() { public int handler() 
	{
		layer_colorbase[0] = 64;
		layer_colorbase[1] = 0;
		layer_colorbase[2] = 16;
		sprite_colorbase = 32;
		zoom_colorbase = 48;
		if (K052109_vh_start(REGION_GFX1,0, 1, 2, 3/*NORMAL_PLANE_ORDER*/,tile_callback) != 0)
		{
			return 1;
		}
		if (K051960_vh_start(REGION_GFX2,0, 1, 2, 3/*NORMAL_PLANE_ORDER*/,sprite_callback) != 0)
		{
			K052109_vh_stop.handler();
			return 1;
		}
		if (K051316_vh_start_0(REGION_GFX3,4,zoom_callback) != 0)
		{
			K052109_vh_stop.handler();
			K051960_vh_stop.handler();
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopPtr k88games_vh_stop = new VhStopPtr() { public void handler() 
	{
		K052109_vh_stop.handler();
		K051960_vh_stop.handler();
		K051316_vh_stop_0.handler();
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	public static VhUpdatePtr k88games_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
	
	
		K052109_tilemap_update();
		K051316_tilemap_update_0();
	
		palette_init_used_colors();
		K051960_mark_sprites_colors();
		/* set back pen for the zoom layer */
		for (i = 0;i < 16;i++)
			palette_used_colors.write((zoom_colorbase + i) * 16, PALETTE_COLOR_TRANSPARENT);
		palette_recalc();
	
		if (k88games_priority != 0)
		{
			K052109_tilemap_draw(bitmap,0,TILEMAP_IGNORE_TRANSPARENCY,0);
			K051960_sprites_draw(bitmap,1,1);
			K052109_tilemap_draw(bitmap,2,0,0);
			K052109_tilemap_draw(bitmap,1,0,0);
			K051960_sprites_draw(bitmap,0,0);
			K051316_zoom_draw_0(bitmap,0);
		}
		else
		{
			K052109_tilemap_draw(bitmap,2,TILEMAP_IGNORE_TRANSPARENCY,0);
			K051316_zoom_draw_0(bitmap,0);
			K051960_sprites_draw(bitmap,0,0);
			K052109_tilemap_draw(bitmap,1,0,0);
			K051960_sprites_draw(bitmap,1,1);
			K052109_tilemap_draw(bitmap,0,0,0);
		}
	} };
}
