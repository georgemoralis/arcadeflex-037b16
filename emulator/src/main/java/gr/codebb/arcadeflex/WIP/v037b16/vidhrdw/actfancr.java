/*******************************************************************************

	actfancr - Bryan McPhail, mish@tendril.co.uk

*******************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.vidhrdw;

import common.ptr.UBytePtr;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapC.*;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.*;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
import gr.codebb.arcadeflex.v037b16.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b16.mame.palette.*;
import static gr.codebb.arcadeflex.v037b16.mame.paletteH.*;
import static mame037b16.mame.Machine;
import static mame037b16.drawgfx.*;
import static mame037b7.palette.palette_init_used_colors;

public class actfancr
{
	
	
	static int[] actfancr_control_1=new int[0x20],actfancr_control_2=new int[0x20];
	public static UBytePtr actfancr_pf1_data=new UBytePtr(), actfancr_pf2_data=new UBytePtr(), actfancr_pf1_rowscroll_data=new UBytePtr();
	static struct_tilemap pf1_tilemap, pf1_alt_tilemap;
	static int flipscreen;
	
	static GetMemoryOffsetPtr actfancr_scan = new GetMemoryOffsetPtr() {
            @Override
            public int handler(int col, int row, int num_cols, int num_rows) {
                /* logical (col,row) . memory offset */
		return (col & 0x0f) + ((row & 0x0f) << 4) + ((col & 0xf0) << 4);
            }
        };
        
	static GetMemoryOffsetPtr actfancr_scan2 = new GetMemoryOffsetPtr() {
            @Override
            public int handler(int col, int row, int num_cols, int num_rows) {
		/* logical (col,row) . memory offset */
		return (col & 0x0f) + ((row & 0x0f) << 4) + ((row & 0x10) << 4) + ((col & 0x70) << 5);
            }
        };
	
	public static GetTileInfoPtr get_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int tile,color;
	
		tile=actfancr_pf1_data.read(2*tile_index)+(actfancr_pf1_data.read(2*tile_index+1)<<8);
		color=tile >> 12;
		tile=tile&0xfff;
	
		SET_TILE_INFO(
				2,
				tile,
				color,
				0);
	} };
	
	static GetMemoryOffsetPtr triothep_scan = new GetMemoryOffsetPtr() {
            @Override
            public int handler(int col, int row, int num_cols, int num_rows) {
		/* logical (col,row) . memory offset */
		return (col & 0x0f) + ((row & 0x0f) << 4) + ((row & 0x10) << 4) + ((col & 0x10) << 5);
            }
        };
	
	public static GetTileInfoPtr get_trio_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int tile,color;
	
		tile=actfancr_pf1_data.read(2*tile_index)+(actfancr_pf1_data.read(2*tile_index+1)<<8);
		color=tile >> 12;
		tile=tile&0xfff;
	
		SET_TILE_INFO(
				2,
				tile,
				color,
				0);
	} };
	
	/******************************************************************************/
	
	static void register_savestate()
	{
/*TODO*///		state_save_register_UINT8("video", 0, "control_1", actfancr_control_1, 0x20);
/*TODO*///		state_save_register_UINT8("video", 0, "control_2", actfancr_control_2, 0x20);
	}
	
	public static VhStartPtr actfancr_vh_start = new VhStartPtr() { public int handler() 
	{
		pf1_tilemap = tilemap_create(get_tile_info,actfancr_scan,TILEMAP_OPAQUE,16,16,256,16);
		pf1_alt_tilemap = tilemap_create(get_tile_info,actfancr_scan2,TILEMAP_OPAQUE,16,16,128,32);
	
		if (pf1_tilemap==null || pf1_alt_tilemap==null)
			return 1;
	
		register_savestate();
	
		return 0;
	} };
	
	public static VhStartPtr triothep_vh_start = new VhStartPtr() { public int handler() 
	{
		pf1_tilemap = tilemap_create(get_trio_tile_info,triothep_scan,TILEMAP_OPAQUE,16,16,32,32);
	
		if (pf1_tilemap==null)
			return 1;
	
		pf1_alt_tilemap=null;
	
		register_savestate();
	
		return 0;
	} };
	
	/******************************************************************************/
	
	public static WriteHandlerPtr actfancr_pf1_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		actfancr_control_1[offset]=data;
	} };
	
	public static WriteHandlerPtr actfancr_pf2_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		actfancr_control_2[offset]=data;
	} };
	
	public static WriteHandlerPtr actfancr_pf1_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		actfancr_pf1_data.write(offset, data);
		tilemap_mark_tile_dirty(pf1_tilemap,offset/2);
		if (pf1_alt_tilemap != null) tilemap_mark_tile_dirty(pf1_alt_tilemap,offset/2);
	} };
	
	public static ReadHandlerPtr actfancr_pf1_data_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return actfancr_pf1_data.read(offset);
	} };
	
	public static WriteHandlerPtr actfancr_pf2_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		actfancr_pf2_data.write(offset, data);
	} };
	
	public static ReadHandlerPtr actfancr_pf2_data_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return actfancr_pf2_data.read(offset);
	} };
	
	/******************************************************************************/
	
	public static VhUpdatePtr actfancr_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int my,mx,offs,color,tile,pal_base,i,mult;
                int[] colmask = new int[16];
		int scrollx=(actfancr_control_1[0x10]+(actfancr_control_1[0x11]<<8));
		int scrolly=(actfancr_control_1[0x12]+(actfancr_control_1[0x13]<<8));
	
		/* Draw playfield */
		flipscreen=actfancr_control_2[0]&0x80;
		tilemap_set_flip(ALL_TILEMAPS,flipscreen!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	
		tilemap_set_scrollx( pf1_tilemap,0, scrollx );
		tilemap_set_scrolly( pf1_tilemap,0, scrolly );
		tilemap_set_scrollx( pf1_alt_tilemap,0, scrollx );
		tilemap_set_scrolly( pf1_alt_tilemap,0, scrolly );
	
		tilemap_update(pf1_tilemap);
		tilemap_update(pf1_alt_tilemap);
	
		palette_init_used_colors();
		pal_base = Machine.drv.gfxdecodeinfo[0].color_codes_start;
		for (color = 0;color < 16;color++) colmask[color] = 0;
		for (offs = 0; offs < 0x800; offs += 2)
		{
			tile = actfancr_pf2_data.read(offs)+(actfancr_pf2_data.read(offs+1)<<8);
			colmask[tile>>12] |= Machine.gfx[0].pen_usage[tile&0xfff];
		}
		for (color = 0;color < 16;color++)
		{
			if ((colmask[color] & (1 << 0)) != 0)
				palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
			for (i = 1;i < 16;i++)
			{
				if ((colmask[color] & (1 << i)) != 0)
					palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
			}
		}
	
		pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;
		for (color = 0;color < 16;color++) colmask[color] = 0;
		for (offs = 0; offs < 0x800; offs += 2)
		{
			tile = buffered_spriteram.read(offs+2)+(buffered_spriteram.read(offs+3)<<8);
			color=buffered_spriteram.read(offs+5)>>4;
			colmask[color] |= Machine.gfx[1].pen_usage[tile&0xfff];
		}
		for (color = 0;color < 16;color++)
		{
			if ((colmask[color] & (1 << 0)) != 0)
				palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
			for (i = 1;i < 16;i++)
			{
				if ((colmask[color] & (1 << i)) != 0)
					palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
			}
		}
	
		palette_recalc();
	
		if (actfancr_control_1[6]==1)
			tilemap_draw(bitmap,pf1_alt_tilemap,0,0);
		else
			tilemap_draw(bitmap,pf1_tilemap,0,0);
	
		/* Sprites */
		for (offs = 0;offs < 0x800;offs += 8)
		{
			int x,y,sprite,colour,multi,fx,fy,inc,flash;
	
			y=buffered_spriteram.read(offs)+(buffered_spriteram.read(offs+1)<<8);
	 		if ((y&0x8000) == 0) continue;
			x = buffered_spriteram.read(offs+4)+(buffered_spriteram.read(offs+5)<<8);
			colour = ((x & 0xf000) >> 12);
			flash=x&0x800;
			if (flash!=0 && (cpu_getcurrentframe() & 1)!=0) continue;
	
			fx = y & 0x2000;
			fy = y & 0x4000;
			multi = (1 << ((y & 0x1800) >> 11)) - 1;	/* 1x, 2x, 4x, 8x height */
	
												/* multi = 0   1   3   7 */
			sprite = buffered_spriteram.read(offs+2)+(buffered_spriteram.read(offs+3)<<8);
			sprite &= 0x0fff;
	
			x = x & 0x01ff;
			y = y & 0x01ff;
			if (x >= 256) x -= 512;
			if (y >= 256) y -= 512;
			x = 240 - x;
			y = 240 - y;
	
			sprite &= ~multi;
			if (fy != 0)
				inc = -1;
			else
			{
				sprite += multi;
				inc = 1;
			}
	
			if (flipscreen != 0) {
				y=240-y;
				x=240-x;
				if (fx != 0) fx=0; else fx=1;
				if (fy != 0) fy=0; else fy=1;
				mult=16;
			}
			else mult=-16;
	
			while (multi >= 0)
			{
				drawgfx(bitmap,Machine.gfx[1],
						sprite - multi * inc,
						colour,
						fx,fy,
						x,y + mult * multi,
						Machine.visible_area,TRANSPARENCY_PEN,0);
				multi--;
			}
		}
	
		/* Draw character tiles */
		for (offs = 0x800 - 2;offs >= 0;offs -= 2) {
			tile=actfancr_pf2_data.read(offs)+(actfancr_pf2_data.read(offs+1)<<8);
			if (tile==0) continue;
			color=tile>>12;
			tile=tile&0xfff;
			mx = (offs/2) % 32;
			my = (offs/2) / 32;
			if (flipscreen != 0) {mx=31-mx; my=31-my;}
			drawgfx(bitmap,Machine.gfx[0],
				tile,color,flipscreen,flipscreen,8*mx,8*my,
				Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	} };
	
	public static VhUpdatePtr triothep_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int my,mx,offs,color,tile,pal_base,i,mult;
                int[] colmask=new int[16];
		int scrollx=(actfancr_control_1[0x10]+(actfancr_control_1[0x11]<<8));
		int scrolly=(actfancr_control_1[0x12]+(actfancr_control_1[0x13]<<8));
	
		/* Draw playfield */
		flipscreen=actfancr_control_2[0]&0x80;
		tilemap_set_flip(ALL_TILEMAPS,flipscreen!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	
		if ((actfancr_control_2[0]&0x4)!=0) {
			tilemap_set_scroll_rows(pf1_tilemap,32);
			tilemap_set_scrolly( pf1_tilemap,0, scrolly );
			for (i=0; i<32; i++)
				tilemap_set_scrollx( pf1_tilemap,i, scrollx+(actfancr_pf1_rowscroll_data.read(i*2) | actfancr_pf1_rowscroll_data.read(i*2+1)<<8) );
		}
		else {
			tilemap_set_scroll_rows(pf1_tilemap,1);
			tilemap_set_scrollx( pf1_tilemap,0, scrollx );
			tilemap_set_scrolly( pf1_tilemap,0, scrolly );
		}
	
		tilemap_update(pf1_tilemap);
	
		palette_init_used_colors();
		pal_base = Machine.drv.gfxdecodeinfo[0].color_codes_start;
		for (color = 0;color < 16;color++) colmask[color] = 0;
		for (offs = 0; offs < 0x800; offs += 2)
		{
			tile = actfancr_pf2_data.read(offs)+(actfancr_pf2_data.read(offs+1)<<8);
			colmask[tile>>12] |= Machine.gfx[0].pen_usage[tile&0xfff];
		}
		for (color = 0;color < 16;color++)
		{
			if ((colmask[color] & (1 << 0)) != 0)
				palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
			for (i = 1;i < 16;i++)
			{
				if ((colmask[color] & (1 << i)) != 0)
					palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
			}
		}
	
		pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;
		for (color = 0;color < 16;color++) colmask[color] = 0;
		for (offs = 0; offs < 0x800; offs += 2)
		{
			tile = buffered_spriteram.read(offs+2)+(buffered_spriteram.read(offs+3)<<8);
			color= buffered_spriteram.read(offs+5)>>4;
			colmask[color] |= Machine.gfx[1].pen_usage[tile&0xfff];
		}
		for (color = 0;color < 16;color++)
		{
			if ((colmask[color] & (1 << 0)) != 0)
				palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
			for (i = 1;i < 16;i++)
			{
				if ((colmask[color] & (1 << i)) != 0)
					palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
			}
		}
	
		palette_recalc();
	
		tilemap_draw(bitmap,pf1_tilemap,0,0);
	
		/* Sprites */
		for (offs = 0;offs < 0x800;offs += 8)
		{
			int x,y,sprite,colour,multi,fx,fy,inc,flash;
	
			y=buffered_spriteram.read(offs)+(buffered_spriteram.read(offs+1)<<8);
	 		if ((y&0x8000) == 0) continue;
			x = buffered_spriteram.read(offs+4)+(buffered_spriteram.read(offs+5)<<8);
			colour = ((x & 0xf000) >> 12);
			flash=x&0x800;
			if (flash!=0 && (cpu_getcurrentframe() & 1)!=0) continue;
	
			fx = y & 0x2000;
			fy = y & 0x4000;
			multi = (1 << ((y & 0x1800) >> 11)) - 1;	/* 1x, 2x, 4x, 8x height */
	
												/* multi = 0   1   3   7 */
			sprite = buffered_spriteram.read(offs+2)+(buffered_spriteram.read(offs+3)<<8);
			sprite &= 0x0fff;
	
			x = x & 0x01ff;
			y = y & 0x01ff;
			if (x >= 256) x -= 512;
			if (y >= 256) y -= 512;
			x = 240 - x;
			y = 240 - y;
	
			sprite &= ~multi;
			if (fy != 0)
				inc = -1;
			else
			{
				sprite += multi;
				inc = 1;
			}
	
			if (flipscreen != 0) {
				y=240-y;
				x=240-x;
				if (fx != 0) fx=0; else fx=1;
				if (fy != 0) fy=0; else fy=1;
				mult=16;
			}
			else mult=-16;
	
			while (multi >= 0)
			{
				drawgfx(bitmap,Machine.gfx[1],
						sprite - multi * inc,
						colour,
						fx,fy,
						x,y + mult * multi,
						Machine.visible_area,TRANSPARENCY_PEN,0);
				multi--;
			}
		}
	
		/* Draw character tiles */
		for (offs = 0x800 - 2;offs >= 0;offs -= 2) {
			tile=actfancr_pf2_data.read(offs)+(actfancr_pf2_data.read(offs+1)<<8);
			if (tile==0) continue;
			color=tile>>12;
			tile=tile&0xfff;
			mx = (offs/2) % 32;
			my = (offs/2) / 32;
			if (flipscreen != 0) {mx=31-mx; my=31-my;}
			drawgfx(bitmap,Machine.gfx[0],
				tile,color,flipscreen,flipscreen,8*mx,8*my,
				Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	} };
}
