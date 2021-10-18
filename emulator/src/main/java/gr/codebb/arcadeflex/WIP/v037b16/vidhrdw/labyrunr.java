/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.vidhrdw;

import common.ptr.UBytePtr;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapC.*;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.*;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.generic.spriteram;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b16.mame.palette.*;
import gr.codebb.arcadeflex.v037b16.mame.osdependH.osd_bitmap;
import static mame037b16.mame.Machine;

public class labyrunr
{
	
	public static UBytePtr labyrunr_videoram1=new UBytePtr(), labyrunr_videoram2=new UBytePtr();
	static struct_tilemap layer0, layer1;
	
	
	public static VhConvertColorPromPtr labyrunr_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i,pal;
                int _colortable=0;
	
		for (pal = 0;pal < 8;pal++)
		{
			if ((pal & 1) != 0)	/* chars, no lookup table */
			{
				for (i = 0;i < 256;i++)
					colortable[_colortable++] = (char) (16 * pal + (i & 0x0f));
			}
			else	/* sprites */
			{
				for (i = 0;i < 256;i++)
					if (color_prom.read(i) == 0)
						colortable[_colortable++] = 0;
					else
						colortable[_colortable++] = (char) (16 * pal + color_prom.read(i));
			}
		}
	} };
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoPtr get_tile_info0 = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int attr = labyrunr_videoram1.read(tile_index);
		int code = labyrunr_videoram1.read(tile_index + 0x400);
		int bit0 = (K007121_ctrlram[0][0x05] >> 0) & 0x03;
		int bit1 = (K007121_ctrlram[0][0x05] >> 2) & 0x03;
		int bit2 = (K007121_ctrlram[0][0x05] >> 4) & 0x03;
		int bit3 = (K007121_ctrlram[0][0x05] >> 6) & 0x03;
		int bank = ((attr & 0x80) >> 7) |
				((attr >> (bit0+2)) & 0x02) |
				((attr >> (bit1+1)) & 0x04) |
				((attr >> (bit2  )) & 0x08) |
				((attr >> (bit3-1)) & 0x10) |
				((K007121_ctrlram[0][0x03] & 0x01) << 5);
		int mask = (K007121_ctrlram[0][0x04] & 0xf0) >> 4;
	
		bank = (bank & ~(mask << 1)) | ((K007121_ctrlram[0][0x04] & mask) << 1);
	
		SET_TILE_INFO(
				0,
				code+bank*256,
				((K007121_ctrlram[0][6]&0x30)*2+16)+(attr&7),
				0);
	} };
	
	public static GetTileInfoPtr get_tile_info1 = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int attr = labyrunr_videoram2.read(tile_index);
		int code = labyrunr_videoram2.read(tile_index + 0x400);
		int bit0 = (K007121_ctrlram[0][0x05] >> 0) & 0x03;
		int bit1 = (K007121_ctrlram[0][0x05] >> 2) & 0x03;
		int bit2 = (K007121_ctrlram[0][0x05] >> 4) & 0x03;
		int bit3 = (K007121_ctrlram[0][0x05] >> 6) & 0x03;
		int bank = ((attr & 0x80) >> 7) |
				((attr >> (bit0+2)) & 0x02) |
				((attr >> (bit1+1)) & 0x04) |
				((attr >> (bit2  )) & 0x08) |
				((attr >> (bit3-1)) & 0x10) |
				((K007121_ctrlram[0][0x03] & 0x01) << 5);
		int mask = (K007121_ctrlram[0][0x04] & 0xf0) >> 4;
	
		bank = (bank & ~(mask << 1)) | ((K007121_ctrlram[0][0x04] & mask) << 1);
	
		SET_TILE_INFO(
				0,
				code+bank*256,
				((K007121_ctrlram[0][6]&0x30)*2+16)+(attr&7),
				0);
	} };
	
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr labyrunr_vh_start = new VhStartPtr() { public int handler() 
	{
		layer0 = tilemap_create(get_tile_info0,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,32,32);
		layer1 = tilemap_create(get_tile_info1,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,32,32);
	
		if (layer0!=null && layer1!=null)
		{
			rectangle clip = new rectangle(Machine.visible_area);
			clip.min_x += 40;
			tilemap_set_clip(layer0,clip);
	
			clip.max_x = 39;
			clip.min_x = 0;
			tilemap_set_clip(layer1,clip);
	
			return 0;
		}
		return 1;
	} };
	
	
	
	/***************************************************************************
	
	  Memory Handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr labyrunr_vram1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (labyrunr_videoram1.read(offset) != data)
		{
			labyrunr_videoram1.write(offset, data);
			tilemap_mark_tile_dirty(layer0,offset & 0x3ff);
		}
	} };
	
	public static WriteHandlerPtr labyrunr_vram2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (labyrunr_videoram2.read(offset) != data)
		{
			labyrunr_videoram2.write(offset, data);
			tilemap_mark_tile_dirty(layer1,offset & 0x3ff);
		}
	} };
	
	
	
	/***************************************************************************
	
	  Screen Refresh
	
	***************************************************************************/
	
	public static VhUpdatePtr labyrunr_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_set_scrollx(layer0,0,K007121_ctrlram[0][0x00] - 40);
		tilemap_set_scrolly(layer0,0,K007121_ctrlram[0][0x02]);
	
		tilemap_update(ALL_TILEMAPS);
		palette_recalc();
	
		tilemap_draw(bitmap,layer0,0,0);
		K007121_sprites_draw(0,bitmap,new UBytePtr(spriteram),(K007121_ctrlram[0][6]&0x30)*2,40,0,-1);
		tilemap_draw(bitmap,layer1,0,0);
	} };
}