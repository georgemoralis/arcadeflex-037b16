/*
	Video Hardware for Shoot Out
	prom GB09.K6 may be related to background tile-sprite priority
*/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.vidhrdw;

import static common.libc.expressions.NOT;
import common.ptr.UBytePtr;
import static gr.codebb.arcadeflex.WIP.v037b16.drivers.shootout.shootout_textram;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapC.*;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.*;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import gr.codebb.arcadeflex.v037b16.mame.osdependH.osd_bitmap;
import static mame037b16.drawgfx.*;
import static mame037b16.mame.Machine;
import static gr.codebb.arcadeflex.v037b16.mame.palette.*;
import static gr.codebb.arcadeflex.v037b16.mame.paletteH.*;
import static mame037b7.palette.palette_init_used_colors;

public class shootout
{
	
	static struct_tilemap background, foreground;
	
	public static GetTileInfoPtr get_bg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) {
		int attributes = videoram.read(tile_index+0x400); /* CCCC -TTT */
		int tile_number = videoram.read(tile_index)+ 256*(attributes&7);
		int color = attributes>>4;
		SET_TILE_INFO(
				2,
				tile_number,
				color,
				0);
	} };
	
	public static GetTileInfoPtr get_fg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) {
		int attributes = shootout_textram.read(tile_index+0x400); /* CCCC --TT */
		int tile_number = shootout_textram.read(tile_index) + 256*(attributes&0x3);
		int color = attributes>>4;
		SET_TILE_INFO(
				0,
				tile_number,
				color,
				0);
	} };
	
	public static WriteHandlerPtr shootout_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if( videoram.read(offset)!=data ){
			videoram.write(offset,data);
		tilemap_mark_tile_dirty( background, offset&0x3ff );
		}
	} };
	public static WriteHandlerPtr shootout_textram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if( shootout_textram.read(offset)!=data ){
			shootout_textram.write(offset, data);
			tilemap_mark_tile_dirty( foreground, offset&0x3ff );
		}
	} };
	
	public static VhStartPtr shootout_vh_start = new VhStartPtr() { public int handler() {
		background = tilemap_create(get_bg_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,32,32);
		foreground = tilemap_create(get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
		if( background!=null && foreground!=null ){
			tilemap_set_transparent_pen( foreground, 0 );
			return 0;
		}
		return 1; /* error */
	} };
        
        static int bFlicker;
	
	static void draw_sprites( osd_bitmap bitmap, int bank_bits ){
		
		GfxElement gfx = Machine.gfx[1];
		rectangle clip = new rectangle(Machine.visible_area);
		UBytePtr source = new UBytePtr(spriteram, 127*4);
		int count;
	
		bFlicker = NOT(bFlicker);
	
		for( count=0; count<128; count++ ){
			int attributes = source.read(1);
			/*
			    76543210
				xxx-----	bank
				---x----	vertical size
				----x---	priority
				-----x--	horizontal flip
				------x-	flicker
				-------x	enable
			*/
			if ((attributes & 0x01) != 0){ /* visible */
				if( bFlicker!=0 || (attributes&0x02)==0 ){
					int priority_mask = (attributes&0x08)!=0?0xaa:0;
					int sx = (240 - source.read(2))&0xff;
					int sy = (240 - source.read(0))&0xff;
					int number = source.read(3) | ((attributes<<bank_bits)&0x700);
					int flipx = (attributes & 0x04);
	
					if ((attributes & 0x10) != 0){ /* double height */
						number = number&(~1);
						sy -= 16;
						pdrawgfx(bitmap,gfx,
							number,
							0 /*color*/,
							flipx,0 /*flipy*/,
							sx,sy,
							clip,TRANSPARENCY_PEN,0,
							priority_mask);
	
						number++;
						sy += 16;
					}
	
					pdrawgfx(bitmap,gfx,
							number,
							0 /*color*/,
							flipx,0 /*flipy*/,
							sx,sy,
							clip,TRANSPARENCY_PEN,0,
							priority_mask);
					}
			}
			source.offset -= 4;
		}
	}
	
	static void draw_sprites2( osd_bitmap bitmap ){
	}
	
	public static VhUpdatePtr shootout_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) {
		tilemap_update(ALL_TILEMAPS);
		palette_init_used_colors();
	//	mark_sprite_colors();
		palette_recalc();
		fillbitmap(priority_bitmap,0,null);
	
		tilemap_draw(bitmap,background,0,0);
		tilemap_draw(bitmap,foreground,0,1);
		draw_sprites( bitmap,3/*bank bits */ );
	} };
	
	public static VhUpdatePtr shootouj_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) {
		tilemap_update(ALL_TILEMAPS);
		palette_init_used_colors();
	//	mark_sprite_colors();
		palette_recalc();
		fillbitmap(priority_bitmap,0,null);
	
		tilemap_draw(bitmap,background,0,1);
		tilemap_draw(bitmap,foreground,0,2);
		draw_sprites( bitmap,2/*bank bits*/ );
	} };
}
