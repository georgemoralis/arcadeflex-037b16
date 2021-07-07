/**
 * ported to v0.37b16
 *
 */
package gr.codebb.arcadeflex.WIP.v037b16.mame;

import static arcadeflex036.osdepend.logerror;
import static arcadeflex036.video.osd_alloc_bitmap;
import static arcadeflex036.video.osd_free_bitmap;
import static common.libc.cstring.memset;
import common.ptr.UBytePtr;
import common.subArrays.IntArray;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.ALL_TILEMAPS;
import gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.GetMemoryOffsetPtr;
import gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.GetTileInfoPtr;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.TILEMAP_ALPHA;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.TILEMAP_BACK;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.TILEMAP_BITMASK;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.TILEMAP_FLIPX;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.TILEMAP_FLIPY;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.TILEMAP_IGNORE_TRANSPARENCY;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.TILEMAP_OPAQUE;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.TILEMAP_SPLIT;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.TILEMAP_TRANSPARENT;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.TILEMAP_TRANSPARENT_COLOR;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.TILE_4BPP;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.TILE_FLIPX;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.TILE_FLIPY;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.TILE_IGNORE_TRANSPARENCY;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.TILE_LINE_DISABLED;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.TILE_SPLIT_OFFSET;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.TILE_SWAPXY;
import gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.struct_tile_info;
import gr.codebb.arcadeflex.v037b16.mame.drawgfxH.rectangle;
import static gr.codebb.arcadeflex.v037b16.mame.driverH.ORIENTATION_FLIP_X;
import static gr.codebb.arcadeflex.v037b16.mame.driverH.ORIENTATION_FLIP_Y;
import static gr.codebb.arcadeflex.v037b16.mame.driverH.ORIENTATION_SWAP_XY;
import gr.codebb.arcadeflex.v037b16.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b16.mame.palette.palette_used_colors;
import static gr.codebb.arcadeflex.v037b16.mame.paletteH.PALETTE_COLOR_CACHED;
import static gr.codebb.arcadeflex.v037b16.mame.paletteH.PALETTE_COLOR_VISIBLE;
import static mame037b16.mame.Machine;
import static mame037b7.palette.palette_decrease_usage_count;
import static mame037b7.palette.palette_decrease_usage_countx;
import static mame037b7.palette.palette_increase_usage_count;
import static mame037b7.palette.palette_increase_usage_countx;
import static mame037b7.palette.*;
import static common.libc.cstring.memcpy;
import common.subArrays.UShortArray;
import gr.codebb.arcadeflex.v037b16.generic.fucPtr.WriteHandlerPtr;

public class tilemapC {

    /*TODO*///#ifndef DECLARE
/*TODO*///
/*TODO*///#include "driver.h"
/*TODO*///#include "tilemap.h"
/*TODO*///#include "state.h"
/*TODO*///
/*TODO*///#define SWAP(X,Y) { UINT32 temp=X; X=Y; Y=temp; }
    public static final int MAX_TILESIZE = 32;
/*TODO*///#define MASKROWBYTES(W) (((W)+7)/8)

    public static class cached_tile_info {

        public UBytePtr pen_data;
        public IntArray pal_data;
        public int u32_pen_usage;
        public int u32_flags;
        public int skip;
    }

    public static class tilemap_mask
    {
	public osd_bitmap bitmask;
	public int line_offset;
	public char[]/*UINT8*/ u8_data;
        public UBytePtr[] data_row;
    };
    
    public static abstract interface DrawHandlerPtr { public abstract void handler(int col, int row); }
    public static abstract interface DrawTileHandlerPtr { public abstract void handler( struct_tilemap tilemap, int cached_indx, int col, int row );}

    public static class struct_tilemap {

        public GetMemoryOffsetPtr get_memory_offset;
        public int[] memory_offset_to_cached_indx;
        public int[] cached_indx_to_memory_offset;
        public int[] logical_flip_to_cached_flip = new int[4];

        /* callback to interpret video RAM for the tilemap */
        public GetTileInfoPtr tile_get_info;

        public int/*UINT32*/ max_memory_offset;
        public int/*UINT32*/ num_tiles;

        public int/*UINT32*/ num_logical_rows, num_logical_cols;
        public int/*UINT32*/ num_cached_rows, num_cached_cols;

        public int/*UINT32*/ tile_size;
        public int/*UINT32*/ num_pens;
        public int/*UINT32*/ cached_width, cached_height;

        public cached_tile_info[] cached_tile_info;
        public int dx, dx_if_flipped;
        public int dy, dy_if_flipped;
        public int scrollx_delta, scrolly_delta;

        public int enable;
        public int attributes;

        public int type;
        public int transparent_pen;
        public int[] fgmask = new int[4];
        public int[] bgmask = new int[4];

        public int bNeedRender;

        public IntArray[] pPenToPixel = new IntArray[8];
        
        public DrawTileHandlerPtr draw_tile;

        public DrawHandlerPtr draw;
        public DrawHandlerPtr draw_opaque;
        public DrawHandlerPtr draw_alpha;

        public char[] u8_priority;/* priority for each tile */
        public UBytePtr[] priority_row;

        public int[] u8_visible;/* boolean flag for each tile */
        public int[] u8_dirty_vram;/* boolean flag for each tile */
        public int[] u8_dirty_pixels;

        public int scroll_rows, scroll_cols;
        public int[] rowscroll;
        public int[] colscroll;

        public int orientation;
        public int clip_left, clip_right, clip_top, clip_bottom;
        
        public int /*UINT16*/ tile_depth, tile_granularity;
        public UBytePtr tile_dirty_map;

        /* cached color data */
        public osd_bitmap pixmap;
        public int pixmap_line_offset;
        public tilemap_mask foreground;
        /* for transparent layers, or the front half of a split layer */

        public tilemap_mask background;
/*TODO*///	/* for the back half of a split layer */
/*TODO*///
        public struct_tilemap next;
    }

    public static osd_bitmap priority_bitmap; /* priority buffer (corresponds to screen bitmap) */
    public static int priority_bitmap_line_offset;

    public static int[] /*UINT8*/ flip_bit_table=new int[0x100]; /* horizontal flip for 8 pixels */
    public static struct_tilemap first_tilemap; /* resource tracking */
    public static int screen_width, screen_height;
    public static struct_tile_info tile_info = new struct_tile_info();

    public static final int TILE_TRANSPARENT = 0;
    public static final int TILE_MASKED = 1;
    public static final int TILE_OPAQUE = 2;

    /* the following parameters are constant across tilemap_draw calls */
    public static class _blit
    {
	public int clip_left;
        public int clip_top;
        public int clip_right;
        public int clip_bottom;
	public int source_width, source_height;
	public int dest_line_offset,source_line_offset,mask_line_offset;
	public int dest_row_offset,source_row_offset,mask_row_offset;
	public osd_bitmap screen;
        public osd_bitmap pixmap;
        public osd_bitmap bitmask;
	public UBytePtr[] mask_data_row;
	public UBytePtr[] priority_data_row;
	public int tile_priority;
	public int tilemap_priority_code;
    };
    
    static _blit  blit = new _blit();

    public static int PenToPixel_Init( struct_tilemap tilemap )
    {
            /*
                    Construct a table for all tile orientations in advance.
                    This simplifies drawing tiles and masks tremendously.
                    If performance is an issue, we can always (re)introduce
                    customized code for each case and forgo tables.
            */
            int i,x,y,tx,ty;
            int tile_size = tilemap.tile_size;
            IntArray pPenToPixel;
            int lError;

            lError = 0;
            for( i=0; i<8; i++ )
            {
                    pPenToPixel = new IntArray( tilemap.num_pens );
                    if( pPenToPixel==null )
                    {
                            lError = 1;
                    }
                    else
                    {
                            tilemap.pPenToPixel[i] = pPenToPixel;
                            for( ty=0; ty<tile_size; ty++ )
                            {
                                    for( tx=0; tx<tile_size; tx++ )
                                    {
                                            if(( i&TILE_SWAPXY ) != 0)
                                            {
                                                    x = ty;
                                                    y = tx;
                                            }
                                            else
                                            {
                                                    x = tx;
                                                    y = ty;
                                            }
                                            if(( i&TILE_FLIPX ) != 0) x = tile_size-1-x;
                                            if(( i&TILE_FLIPY ) != 0) y = tile_size-1-y;
                                            pPenToPixel.writeinc( x+y*MAX_TILESIZE );
                                    }
                            }
                    }
            }
            return lError;
    }

/*TODO*///void PenToPixel_Term( struct tilemap *tilemap )
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	for( i=0; i<8; i++ )
/*TODO*///	{
/*TODO*///		free( tilemap->pPenToPixel[i] );
/*TODO*///	}
/*TODO*///}
    
    static void tmap_render( struct_tilemap tilemap )
    {
    	if( tilemap.bNeedRender != 0){
    		tilemap.bNeedRender = 0;
    		if( tilemap.enable != 0){
    			int[] dirty_pixels = tilemap.u8_dirty_pixels;
    			int[] visible = tilemap.u8_visible;
    			int /*UINT32*/ cached_indx = 0;
    			int /*UINT32*/ row,col;
    
    			/* walk over cached rows/cols (better to walk screen coords) */
    			for( row=0; row<tilemap.num_cached_rows; row++ ){
    				for( col=0; col<tilemap.num_cached_cols; col++ ){
    					if( visible[cached_indx]!=0 && dirty_pixels[cached_indx]!=0 ){
    						tilemap.draw_tile.handler( tilemap, cached_indx, col, row );
    						dirty_pixels[cached_indx] = 0;
                                                //System.out.println("check if i have to add tilemap.u8_dirty_pixels[cached_indx] = 0;");
                                                tilemap.u8_dirty_pixels[cached_indx] = 0;
    					}
    					cached_indx++;
    				} /* next col */
    			} /* next row */
    		}
    	}
    }
    
/*TODO*///struct osd_bitmap *tilemap_get_pixmap( struct tilemap * tilemap )
/*TODO*///{
/*TODO*///profiler_mark(PROFILER_TILEMAP_DRAW);
/*TODO*///	tmap_render( tilemap );
/*TODO*///profiler_mark(PROFILER_END);
/*TODO*///	return tilemap->pixmap;
/*TODO*///}

    public static void tilemap_set_transparent_pen( struct_tilemap tilemap, int pen )
    {
            tilemap.transparent_pen = pen;
    }

/*TODO*///void tilemap_set_transmask( struct tilemap *tilemap, int which, UINT32 fgmask, UINT32 bgmask )
/*TODO*///{
/*TODO*///	tilemap->fgmask[which] = fgmask;
/*TODO*///	tilemap->bgmask[which] = bgmask;
/*TODO*///}
/*TODO*///
/*TODO*///void tilemap_set_depth( struct tilemap *tilemap, int tile_depth, int tile_granularity )
/*TODO*///{
/*TODO*///	if( tilemap->tile_dirty_map )
/*TODO*///	{
/*TODO*///		free( tilemap->tile_dirty_map);
/*TODO*///	}
/*TODO*///	tilemap->tile_dirty_map = malloc( Machine->drv->total_colors >> tile_granularity);
/*TODO*///	if( tilemap->tile_dirty_map )
/*TODO*///	{
/*TODO*///		tilemap->tile_depth = tile_depth;
/*TODO*///		tilemap->tile_granularity = tile_granularity;
/*TODO*///	}
/*TODO*///}

    /***********************************************************************************/
    /* some common mappings */

    public static GetMemoryOffsetPtr tilemap_scan_rows = new GetMemoryOffsetPtr() {
        @Override
        public int handler(int u32_col, int u32_row, int u32_num_cols, int u32_num_rows) {
            /* logical (col,row) -> memory offset */
            return u32_row*u32_num_cols + u32_col;
        }
    };
        
    public static GetMemoryOffsetPtr tilemap_scan_cols = new GetMemoryOffsetPtr() {
        @Override
        public int handler(int u32_col, int u32_row, int u32_num_cols, int u32_num_rows) {
            /* logical (col,row) -> memory offset */
            return u32_col*u32_num_rows + u32_row;
        }
    };

    /*********************************************************************************/

    public static osd_bitmap create_tmpbitmap( int width, int height, int depth )
    {
            return osd_alloc_bitmap( width,height,depth );
    }

    static osd_bitmap create_bitmask( int width, int height )
    {
            width = (width+7)/8; /* 8 bits per byte */
            return osd_alloc_bitmap( width,height, 8 );
    }

    /***********************************************************************************/

    static int mappings_create( struct_tilemap tilemap )
    {
            int max_memory_offset = 0;
            int /*UINT32*/ col,row;
            int /*UINT32*/ num_logical_rows = tilemap.num_logical_rows;
            int /*UINT32*/ num_logical_cols = tilemap.num_logical_cols;
            /* count offsets (might be larger than num_tiles) */
            for( row=0; row<num_logical_rows; row++ )
            {
                    for( col=0; col<num_logical_cols; col++ )
                    {
                            int /*UINT32*/ memory_offset = tilemap.get_memory_offset.handler( col, row, num_logical_cols, num_logical_rows );
                            if( memory_offset>max_memory_offset ) max_memory_offset = memory_offset;
                    }
            }
            max_memory_offset++;
            tilemap.max_memory_offset = max_memory_offset;
            /* logical to cached (tilemap_mark_dirty) */
            tilemap.memory_offset_to_cached_indx = new int[max_memory_offset];
            if( tilemap.memory_offset_to_cached_indx != null)
            {
                    /* cached to logical (get_tile_info) */
                    tilemap.cached_indx_to_memory_offset = new int[tilemap.num_tiles];
                    if( tilemap.cached_indx_to_memory_offset !=null ) return 0; /* no error */
                    tilemap.memory_offset_to_cached_indx = null;
            }
            return -1; /* error */
    }

/*TODO*///static void mappings_dispose( struct tilemap *tilemap )
/*TODO*///{
/*TODO*///	free( tilemap->cached_indx_to_memory_offset );
/*TODO*///	free( tilemap->memory_offset_to_cached_indx );
/*TODO*///}

static void mappings_update( struct_tilemap tilemap )
{
	int logical_flip;
	int /*UINT32*/ logical_indx, cached_indx;
	int /*UINT32*/ num_cached_rows = tilemap.num_cached_rows;
	int /*UINT32*/ num_cached_cols = tilemap.num_cached_cols;
	int /*UINT32*/ num_logical_rows = tilemap.num_logical_rows;
	int /*UINT32*/ num_logical_cols = tilemap.num_logical_cols;
	for( logical_indx=0; logical_indx<tilemap.max_memory_offset; logical_indx++ )
	{
		tilemap.memory_offset_to_cached_indx[logical_indx] = -1;
	}

	for( logical_indx=0; logical_indx<tilemap.num_tiles; logical_indx++ )
	{
		int /*UINT32*/ logical_col = logical_indx%num_logical_cols;
		int /*UINT32*/ logical_row = logical_indx/num_logical_cols;
		int memory_offset = tilemap.get_memory_offset.handler(logical_col, logical_row, num_logical_cols, num_logical_rows );
		int /*UINT32*/ cached_col = logical_col;
		int /*UINT32*/ cached_row = logical_row;
		if(( tilemap.orientation & ORIENTATION_SWAP_XY ) != 0) {
                    //SWAP(cached_col, cached_row)
                    int temp = cached_col;
                    cached_col = cached_row;
                    cached_row = temp;
                }
                    
		if(( tilemap.orientation & ORIENTATION_FLIP_X ) != 0) cached_col = (num_cached_cols-1)-cached_col;
		if(( tilemap.orientation & ORIENTATION_FLIP_Y ) != 0) cached_row = (num_cached_rows-1)-cached_row;
		cached_indx = cached_row*num_cached_cols+cached_col;
		tilemap.memory_offset_to_cached_indx[memory_offset] = cached_indx;
		tilemap.cached_indx_to_memory_offset[cached_indx] = memory_offset;
	}
	for( logical_flip = 0; logical_flip<4; logical_flip++ )
	{
		int cached_flip = logical_flip;
		if(( tilemap.attributes&TILEMAP_FLIPX ) != 0) cached_flip ^= TILE_FLIPX;
		if(( tilemap.attributes&TILEMAP_FLIPY ) != 0) cached_flip ^= TILE_FLIPY;
/*TODO*///#ifndef PREROTATE_GFX
/*TODO*///		if( Machine->orientation & ORIENTATION_SWAP_XY )
/*TODO*///		{
/*TODO*///			if( Machine->orientation & ORIENTATION_FLIP_X ) cached_flip ^= TILE_FLIPY;
/*TODO*///			if( Machine->orientation & ORIENTATION_FLIP_Y ) cached_flip ^= TILE_FLIPX;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if( Machine->orientation & ORIENTATION_FLIP_X ) cached_flip ^= TILE_FLIPX;
/*TODO*///			if( Machine->orientation & ORIENTATION_FLIP_Y ) cached_flip ^= TILE_FLIPY;
/*TODO*///		}
/*TODO*///#endif
		if(( tilemap.orientation & ORIENTATION_SWAP_XY ) != 0)
		{
			cached_flip = ((cached_flip&1)<<1) | ((cached_flip&2)>>1);
		}
		tilemap.logical_flip_to_cached_flip[logical_flip] = cached_flip;
	}
}

/*TODO*////***********************************************************************************/
/*TODO*///
static void memsetbitmask8(UBytePtr dest, int value, UBytePtr bitmask, int count) {
        /* TBA: combine with memcpybitmask */
        for (;;) {
            int/*UINT32*/ data = bitmask.readinc();
            if ((data & 0x80) != 0) {
                dest.write(0, dest.read(0) | value);
            }
            if ((data & 0x40) != 0) {
                dest.write(1, dest.read(1) | value);
            }
            if ((data & 0x20) != 0) {
                dest.write(2, dest.read(2) | value);
            }
            if ((data & 0x10) != 0) {
                dest.write(3, dest.read(3) | value);
            }
            if ((data & 0x08) != 0) {
                dest.write(4, dest.read(4) | value);
            }
            if ((data & 0x04) != 0) {
                dest.write(5, dest.read(5) | value);
            }
            if ((data & 0x02) != 0) {
                dest.write(6, dest.read(6) | value);
            }
            if ((data & 0x01) != 0) {
                dest.write(7, dest.read(7) | value);
            }
            if (--count == 0) {
                break;
            }
            dest.offset += 8;
        }
    }


   static void memcpybitmask8(UBytePtr dest, UBytePtr source, UBytePtr bitmask, int count) {
        for (;;) {
            int/*UINT32*/ data = bitmask.readinc();
            if ((data & 0x80) != 0) {
                dest.write(0, source.read(0));
            }
            if ((data & 0x40) != 0) {
                dest.write(1, source.read(1));
            }
            if ((data & 0x20) != 0) {
                dest.write(2, source.read(2));
            }
            if ((data & 0x10) != 0) {
                dest.write(3, source.read(3));
            }
            if ((data & 0x08) != 0) {
                dest.write(4, source.read(4));
            }
            if ((data & 0x04) != 0) {
                dest.write(5, source.read(5));
            }
            if ((data & 0x02) != 0) {
                dest.write(6, source.read(6));
            }
            if ((data & 0x01) != 0) {
                dest.write(7, source.read(7));
            }
            if (--count == 0) {
                break;
            }
            source.offset += 8;
            dest.offset += 8;
        }
    }

/*TODO*///static void memcpybitmask16( UINT16 *dest, const UINT16 *source, const UINT8 *bitmask, int count )
/*TODO*///{
/*TODO*///	for(;;)
/*TODO*///	{
/*TODO*///		UINT32 data = *bitmask++;
/*TODO*///		if( data&0x80 ) dest[0] = source[0];
/*TODO*///		if( data&0x40 ) dest[1] = source[1];
/*TODO*///		if( data&0x20 ) dest[2] = source[2];
/*TODO*///		if( data&0x10 ) dest[3] = source[3];
/*TODO*///		if( data&0x08 ) dest[4] = source[4];
/*TODO*///		if( data&0x04 ) dest[5] = source[5];
/*TODO*///		if( data&0x02 ) dest[6] = source[6];
/*TODO*///		if( data&0x01 ) dest[7] = source[7];
/*TODO*///		if( --count == 0 ) break;
/*TODO*///		source+=8;
/*TODO*///		dest+=8;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void memcpybitmask32( UINT32 *dest, const UINT32 *source, const UINT8 *bitmask, int count )
/*TODO*///{
/*TODO*///	for(;;)
/*TODO*///	{
/*TODO*///		UINT32 data = *bitmask++;
/*TODO*///		if( data&0x80 ) dest[0] = source[0];
/*TODO*///		if( data&0x40 ) dest[1] = source[1];
/*TODO*///		if( data&0x20 ) dest[2] = source[2];
/*TODO*///		if( data&0x10 ) dest[3] = source[3];
/*TODO*///		if( data&0x08 ) dest[4] = source[4];
/*TODO*///		if( data&0x04 ) dest[5] = source[5];
/*TODO*///		if( data&0x02 ) dest[6] = source[6];
/*TODO*///		if( data&0x01 ) dest[7] = source[7];
/*TODO*///		if( --count == 0 ) break;
/*TODO*///		source+=8;
/*TODO*///		dest+=8;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////***********************************************************************************/
/*TODO*///
/*TODO*///static void blend16( UINT16 *dest, const UINT16 *source, int count )
/*TODO*///{
/*TODO*///	for(;;)
/*TODO*///	{
/*TODO*///		*dest = alpha_blend16(*dest, *source);
/*TODO*///		if( --count == 0 ) break;
/*TODO*///		source++;
/*TODO*///		dest++;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void blendbitmask16( UINT16 *dest, const UINT16 *source, const UINT8 *bitmask, int count )
/*TODO*///{
/*TODO*///	for(;;)
/*TODO*///	{
/*TODO*///		UINT32 data = *bitmask++;
/*TODO*///		if( data&0x80 ) dest[0] = alpha_blend16(dest[0], source[0]);
/*TODO*///		if( data&0x40 ) dest[1] = alpha_blend16(dest[1], source[1]);
/*TODO*///		if( data&0x20 ) dest[2] = alpha_blend16(dest[2], source[2]);
/*TODO*///		if( data&0x10 ) dest[3] = alpha_blend16(dest[3], source[3]);
/*TODO*///		if( data&0x08 ) dest[4] = alpha_blend16(dest[4], source[4]);
/*TODO*///		if( data&0x04 ) dest[5] = alpha_blend16(dest[5], source[5]);
/*TODO*///		if( data&0x02 ) dest[6] = alpha_blend16(dest[6], source[6]);
/*TODO*///		if( data&0x01 ) dest[7] = alpha_blend16(dest[7], source[7]);
/*TODO*///		if( --count == 0 ) break;
/*TODO*///		source+=8;
/*TODO*///		dest+=8;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void blend32( UINT32 *dest, const UINT32 *source, int count )
/*TODO*///{
/*TODO*///	for(;;)
/*TODO*///	{
/*TODO*///		*dest = alpha_blend32(*dest, *source);
/*TODO*///		if( --count == 0 ) break;
/*TODO*///		source++;
/*TODO*///		dest++;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void blendbitmask32( UINT32 *dest, const UINT32 *source, const UINT8 *bitmask, int count )
/*TODO*///{
/*TODO*///	for(;;)
/*TODO*///	{
/*TODO*///		UINT32 data = *bitmask++;
/*TODO*///		if( data&0x80 ) dest[0] = alpha_blend32(dest[0], source[0]);
/*TODO*///		if( data&0x40 ) dest[1] = alpha_blend32(dest[1], source[1]);
/*TODO*///		if( data&0x20 ) dest[2] = alpha_blend32(dest[2], source[2]);
/*TODO*///		if( data&0x10 ) dest[3] = alpha_blend32(dest[3], source[3]);
/*TODO*///		if( data&0x08 ) dest[4] = alpha_blend32(dest[4], source[4]);
/*TODO*///		if( data&0x04 ) dest[5] = alpha_blend32(dest[5], source[5]);
/*TODO*///		if( data&0x02 ) dest[6] = alpha_blend32(dest[6], source[6]);
/*TODO*///		if( data&0x01 ) dest[7] = alpha_blend32(dest[7], source[7]);
/*TODO*///		if( --count == 0 ) break;
/*TODO*///		source+=8;
/*TODO*///		dest+=8;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////***********************************************************************************/
/*TODO*///
/*TODO*////**** DEPTH == 8 ****/
/*TODO*///
/*TODO*///#define DEPTH 8
    public static int TILE_SIZE	= 8;
/*TODO*///#define DATA_TYPE UINT8
/*TODO*///#define memcpybitmask memcpybitmask8
/*TODO*///#define DECLARE(function,args,body) static void function##8x8x8BPP args body
/*TODO*///#include "tilemap.c"
/*TODO*///
/*TODO*///#define TILE_SIZE	16
/*TODO*///#define DATA_TYPE UINT8
/*TODO*///#define memcpybitmask memcpybitmask8
/*TODO*///#define DECLARE(function,args,body) static void function##16x16x8BPP args body
/*TODO*///#include "tilemap.c"
/*TODO*///
/*TODO*///#define TILE_SIZE	32
/*TODO*///#define DATA_TYPE UINT8
/*TODO*///#define memcpybitmask memcpybitmask8
/*TODO*///#define DECLARE(function,args,body) static void function##32x32x8BPP args body
/*TODO*///#include "tilemap.c"
/*TODO*///#undef DEPTH
/*TODO*///
/*TODO*////**** DEPTH == 16 ****/
/*TODO*///
/*TODO*///#define DEPTH 16
/*TODO*///#define TILE_SIZE	8
/*TODO*///#define DATA_TYPE UINT16
/*TODO*///#define memcpybitmask memcpybitmask16
/*TODO*///#define blend blend16
/*TODO*///#define blendbitmask blendbitmask16
/*TODO*///#define DECLARE(function,args,body) static void function##8x8x16BPP args body
/*TODO*///#include "tilemap.c"
/*TODO*///
/*TODO*///#define TILE_SIZE	16
/*TODO*///#define DATA_TYPE UINT16
/*TODO*///#define memcpybitmask memcpybitmask16
/*TODO*///#define DECLARE(function,args,body) static void function##16x16x16BPP args body
/*TODO*///#include "tilemap.c"
/*TODO*///
/*TODO*///#define TILE_SIZE	32
/*TODO*///#define DATA_TYPE UINT16
/*TODO*///#define memcpybitmask memcpybitmask16
/*TODO*///#define DECLARE(function,args,body) static void function##32x32x16BPP args body
/*TODO*///#include "tilemap.c"
/*TODO*///#undef DEPTH
/*TODO*///#undef blend
/*TODO*///#undef blendbitmask
/*TODO*///
/*TODO*////**** DEPTH == 32 ****/
/*TODO*///
/*TODO*///#define DEPTH 32
/*TODO*///#define TILE_SIZE	8
/*TODO*///#define DATA_TYPE UINT32
/*TODO*///#define memcpybitmask memcpybitmask32
/*TODO*///#define blend blend32
/*TODO*///#define blendbitmask blendbitmask32
/*TODO*///#define DECLARE(function,args,body) static void function##8x8x32BPP args body
/*TODO*///#include "tilemap.c"
/*TODO*///
/*TODO*///#define TILE_SIZE	16
/*TODO*///#define DATA_TYPE UINT32
/*TODO*///#define memcpybitmask memcpybitmask32
/*TODO*///#define DECLARE(function,args,body) static void function##16x16x32BPP args body
/*TODO*///#include "tilemap.c"
/*TODO*///
/*TODO*///#define TILE_SIZE	32
/*TODO*///#define DATA_TYPE UINT32
/*TODO*///#define memcpybitmask memcpybitmask32
/*TODO*///#define DECLARE(function,args,body) static void function##32x32x32BPP args body
/*TODO*///#include "tilemap.c"
/*TODO*///#undef DEPTH
/*TODO*///#undef blend
/*TODO*///#undef blendbitmask

    /*********************************************************************************/

    static void mask_dispose( tilemap_mask mask )
    {
            if (mask != null) {
                mask.data_row = null;
                mask.u8_data = null;
                osd_free_bitmap(mask.bitmask);
                mask = null;
            }
    }

    static tilemap_mask mask_create( struct_tilemap tilemap )
    {
            int row;
            tilemap_mask mask = new tilemap_mask();
            if( mask != null )
            {
                    mask.u8_data = new char[ tilemap.num_tiles ];
                    mask.data_row = new UBytePtr[tilemap.num_cached_rows];
                    mask.bitmask = create_bitmask( tilemap.cached_width, tilemap.cached_height );
                    if( mask.u8_data!=null && mask.data_row!=null && mask.bitmask!=null )
                    {
                            for( row=0; row<tilemap.num_cached_rows; row++ )
                            {
                                    mask.data_row[row] = new UBytePtr(mask.u8_data, row * tilemap.num_cached_cols);
                            }
                            mask.line_offset = mask.bitmask.line[1].offset - mask.bitmask.line[0].offset;
                            return mask;
                    }
            }
            mask_dispose( mask );
            return null;
    }

    /***********************************************************************************/

    static void install_draw_handlers( struct_tilemap tilemap )
    {
            int tile_size = tilemap.tile_size;
            tilemap.draw = tilemap.draw_opaque = tilemap.draw_alpha = null;
            switch( Machine.scrbitmap.depth )
            {
    	case 32:
            throw new UnsupportedOperationException("Not supported yet.");
/*TODO*///    		tilemap.draw_tile = draw_tile8x8x32BPP;
    
/*TODO*///    		if( tile_size==8 )
/*TODO*///    		{
/*TODO*///    			tilemap.draw = draw8x8x32BPP;
/*TODO*///    			tilemap.draw_opaque = draw_opaque8x8x32BPP;
/*TODO*///    			tilemap.draw_alpha = draw_alpha8x8x32BPP;
/*TODO*///    		}
/*TODO*///    		else if( tile_size==16 )
/*TODO*///    		{
/*TODO*///    			tilemap.draw = draw16x16x32BPP;
/*TODO*///    			tilemap.draw_opaque = draw_opaque16x16x32BPP;
/*TODO*///    			tilemap.draw_alpha = draw_alpha16x16x32BPP;
/*TODO*///    		}
/*TODO*///    		else if( tile_size==32 )
/*TODO*///    		{
/*TODO*///    			tilemap.draw = draw32x32x32BPP;
/*TODO*///    			tilemap.draw_opaque = draw_opaque32x32x32BPP;
/*TODO*///    			tilemap.draw_alpha = draw_alpha32x32x32BPP;
/*TODO*///    		}
/*TODO*///    		break;
    
    	case 15:
    	case 16:
            throw new UnsupportedOperationException("Not supported yet.");
/*TODO*///    		tilemap.draw_tile = draw_tile8x8x16BPP;
    
/*TODO*///    		if( tile_size==8 )
/*TODO*///    		{
/*TODO*///    			tilemap.draw = draw8x8x16BPP;
/*TODO*///    			tilemap.draw_opaque = draw_opaque8x8x16BPP;
/*TODO*///    			tilemap.draw_alpha = draw_alpha8x8x16BPP;
/*TODO*///    		}
/*TODO*///    		else if( tile_size==16 )
/*TODO*///    		{
/*TODO*///    			tilemap.draw = draw16x16x16BPP;
/*TODO*///    			tilemap.draw_opaque = draw_opaque16x16x16BPP;
/*TODO*///    			tilemap.draw_alpha = draw_alpha16x16x16BPP;
/*TODO*///    		}
/*TODO*///    		else if( tile_size==32 )
/*TODO*///    		{
/*TODO*///    			tilemap.draw = draw32x32x16BPP;
/*TODO*///    			tilemap.draw_opaque = draw_opaque32x32x16BPP;
/*TODO*///    			tilemap.draw_alpha = draw_alpha32x32x16BPP;
/*TODO*///    		}
/*TODO*///    		break;
    
    	case 8:
            
    		tilemap.draw_tile = draw_tile8x8x8BPP;

    		if( tile_size==8 )
    		{                    
    			tilemap.draw = draw8x8x8BPP;
    			tilemap.draw_opaque = draw_opaque8x8x8BPP;
    			tilemap.draw_alpha = draw8x8x8BPP;
    		}
    		else if( tile_size==16 )
    		{                    
    			tilemap.draw = draw16x16x8BPP;
    			tilemap.draw_opaque = draw_opaque16x16x8BPP;
    			tilemap.draw_alpha = draw16x16x8BPP;
    		}
    		else if( tile_size==32 )
    		{                    
    			tilemap.draw = draw32x32x8BPP;
    			tilemap.draw_opaque = draw_opaque32x32x8BPP;
    			tilemap.draw_alpha = draw32x32x8BPP;
    		}
    		break;
    	}
    }

/*TODO*////***********************************************************************************/
/*TODO*///
/*TODO*///static void tilemap_reset(void)
/*TODO*///{
/*TODO*///	tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
/*TODO*///}
/*TODO*///
    public static int tilemap_init() {
        
        int /*UINT32*/ value, data, bit;
	for( value=0; value<0x100; value++ )
	{
		data = 0;
		for( bit=0; bit<8; bit++ ) if(( (value>>bit)&1 ) != 0) data |= 0x80>>bit;
		flip_bit_table[value] = data;
	}
	screen_width = Machine.scrbitmap.width;
	screen_height = Machine.scrbitmap.height;
	first_tilemap = null;
/*TODO*///	state_save_register_func_postload(tilemap_reset);
	priority_bitmap = create_tmpbitmap( screen_width, screen_height, 8 );
	if( priority_bitmap != null ){
		priority_bitmap_line_offset = priority_bitmap.line[1].offset - priority_bitmap.line[0].offset;
		return 0;
	}
        return -1;
    }

    public static void tilemap_close() {
        System.out.println("todo tilemap_close");
        /*TODO*///	while( first_tilemap )
/*TODO*///	{
/*TODO*///		struct tilemap *next = first_tilemap->next;
/*TODO*///		tilemap_dispose( first_tilemap );
/*TODO*///		first_tilemap = next;
/*TODO*///	}
/*TODO*///	osd_free_bitmap( priority_bitmap );
    }

    
    /***********************************************************************************/

    public static struct_tilemap tilemap_create(GetTileInfoPtr tile_get_info,
            GetMemoryOffsetPtr get_memory_offset,
            int type,
            int tile_width,
            int tile_height, /* in pixels */
            int num_cols,
            int num_rows /* in tiles */) {
        
	struct_tilemap tilemap = new struct_tilemap();

	if( tile_width != tile_height )
	{
		logerror( "tilemap_create: tile_width must be equal to tile_height\n" );
		return null;
	}

	
	if( tilemap != null )
	{
		int num_tiles = num_cols*num_rows;
		tilemap.num_logical_cols = num_cols;
		tilemap.num_logical_rows = num_rows;
		if(( Machine.orientation & ORIENTATION_SWAP_XY ) != 0)
		{
			//SWAP( num_cols,num_rows )
                        int temp2 = num_cols;
                        num_cols = num_rows;
                        num_rows = temp2;
		}
		tilemap.num_cached_cols = num_cols;
		tilemap.num_cached_rows = num_rows;
		tilemap.num_tiles = num_tiles;
		tilemap.num_pens = tile_width*tile_height;
		tilemap.tile_size = tile_width; /* tile_width and tile_height are equal */
		tilemap.cached_width = tile_width*num_cols;
		tilemap.cached_height = tile_height*num_rows;
		tilemap.tile_get_info = tile_get_info;
		tilemap.get_memory_offset = get_memory_offset;
		tilemap.orientation = Machine.orientation;

		/* various defaults */
		tilemap.enable = 1;
		tilemap.type = type;
		tilemap.scroll_rows = 1;
		tilemap.scroll_cols = 1;
		tilemap.transparent_pen = -1;
		tilemap.tile_depth = 0;
		tilemap.tile_granularity = 0;
		tilemap.tile_dirty_map = null;

		tilemap.cached_tile_info = new cached_tile_info[num_tiles];//tilemap.cached_tile_info = calloc( num_tiles, sizeof(struct cached_tile_info) );
                for (int i = 0; i < num_tiles; i++) {
                    tilemap.cached_tile_info[i] = new cached_tile_info();//init cache_tiles
                }
		tilemap.u8_priority = new char[num_tiles];
                tilemap.u8_visible = new int[num_tiles];
                tilemap.u8_dirty_vram = new int[num_tiles];
                tilemap.u8_dirty_pixels = new int[num_tiles];
		tilemap.rowscroll = new int[tilemap.cached_height];//calloc(tilemap->cached_height,sizeof(int));
                tilemap.colscroll = new int[tilemap.cached_width];//calloc(tilemap->cached_width,sizeof(int));
                tilemap.priority_row = new UBytePtr[num_rows];//malloc( sizeof(UINT8 *)*num_rows );
		tilemap.pixmap = create_tmpbitmap( tilemap.cached_width, tilemap.cached_height, Machine.scrbitmap.depth );
		tilemap.foreground = mask_create(tilemap);
                tilemap.background = (type & TILEMAP_SPLIT) != 0 ? mask_create(tilemap) : null;

		if( tilemap.cached_tile_info!=null &&
			tilemap.u8_priority!=null && tilemap.u8_visible!=null &&
			tilemap.u8_dirty_vram!=null && tilemap.u8_dirty_pixels!=null &&
			tilemap.rowscroll!=null && tilemap.colscroll!=null &&
			tilemap.priority_row!=null &&
			tilemap.pixmap!=null && tilemap.foreground!=null &&
			((type&TILEMAP_SPLIT)==0 || tilemap.background!=null) &&
			(mappings_create( tilemap )==0) )
                    {
                            int row;
                            for( row=0; row<num_rows; row++ ){
                                    tilemap.priority_row[row] = new UBytePtr(tilemap.u8_priority, num_cols * row);
                            }
                            install_draw_handlers( tilemap );
                            mappings_update( tilemap );
                            tilemap_set_clip( tilemap, new rectangle(Machine.visible_area) );
                            memset( tilemap.u8_dirty_vram, 1, num_tiles );
                            memset( tilemap.u8_dirty_pixels, 1, num_tiles );
                            tilemap.pixmap_line_offset = tilemap.pixmap.line[1].offset - tilemap.pixmap.line[0].offset;
                            tilemap.next = first_tilemap;
                            first_tilemap = tilemap;
                            if( PenToPixel_Init( tilemap ) == 0 )
                            {
                                    return tilemap;
                            }
                    }
                    tilemap_dispose( tilemap );
            }
            return null;
    }

    public static void tilemap_dispose( struct_tilemap tilemap )
    {
/*TODO*///	struct tilemap *prev;
/*TODO*///
/*TODO*///	if( tilemap==first_tilemap )
/*TODO*///	{
/*TODO*///		first_tilemap = tilemap->next;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		prev = first_tilemap;
/*TODO*///		while( prev->next != tilemap ) prev = prev->next;
/*TODO*///		prev->next =tilemap->next;
/*TODO*///	}
/*TODO*///	PenToPixel_Term( tilemap );
/*TODO*///	free( tilemap->cached_tile_info );
/*TODO*///	free( tilemap->priority );
/*TODO*///	free( tilemap->visible );
/*TODO*///	free( tilemap->dirty_vram );
/*TODO*///	free( tilemap->dirty_pixels );
/*TODO*///	free( tilemap->rowscroll );
/*TODO*///	free( tilemap->colscroll );
/*TODO*///	free( tilemap->priority_row );
/*TODO*///	osd_free_bitmap( tilemap->pixmap );
/*TODO*///	mask_dispose( tilemap->foreground );
/*TODO*///	mask_dispose( tilemap->background );
/*TODO*///	mappings_dispose( tilemap );
/*TODO*///	free( tilemap );
    }

    /***********************************************************************************/
    
    static void unregister_pens( cached_tile_info cached_tile_info, int num_pens )
    {
    	if( palette_used_colors != null )
    	{
    		IntArray pal_data = new IntArray(cached_tile_info.pal_data);
    		if( pal_data != null )
    		{
    			int pen_usage = cached_tile_info.u32_pen_usage;
    			if( pen_usage != 0 )
    			{
    				palette_decrease_usage_count(
    					pal_data.offset-Machine.remapped_colortable.offset,
    					pen_usage,
    					PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
    			}
    			else
    			{
    				palette_decrease_usage_countx(
    					pal_data.offset-Machine.remapped_colortable.offset,
    					num_pens,
    					cached_tile_info.pen_data,
    					PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
    			}
    			cached_tile_info.pal_data = null;
    		}
    	}
    }
    
    public static void register_pens( cached_tile_info cached_tile_info, int num_pens )
    {
    	if (palette_used_colors != null)
    	{
    		int /*UINT32*/ pen_usage = cached_tile_info.u32_pen_usage;
    		if( pen_usage != 0 )
    		{
    			palette_increase_usage_count(
    				cached_tile_info.pal_data.offset-Machine.remapped_colortable.offset,
    				pen_usage,
    				PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
    		}
    		else
    		{
    			palette_increase_usage_countx(
    				cached_tile_info.pal_data.offset-Machine.remapped_colortable.offset,
    				num_pens,
    				cached_tile_info.pen_data,
    				PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
    		}
    	}
    }
    
/*TODO*////***********************************************************************************/
/*TODO*///
/*TODO*///void tilemap_set_enable( struct tilemap *tilemap, int enable )
/*TODO*///{
/*TODO*///	tilemap->enable = enable?1:0;
/*TODO*///}
/*TODO*///
    public static void tilemap_set_flip(struct_tilemap tilemap, int attributes) {
        
        if( tilemap==ALL_TILEMAPS )
	{
		tilemap = first_tilemap;
		while( tilemap != null )
		{
			tilemap_set_flip( tilemap, attributes );
			tilemap = tilemap.next;
		}
	}
	else if( tilemap.attributes!=attributes )
	{
		tilemap.attributes = attributes;
		tilemap.orientation = Machine.orientation;
		if(( attributes&TILEMAP_FLIPY ) != 0)
		{
			tilemap.orientation ^= ORIENTATION_FLIP_Y;
			tilemap.scrolly_delta = tilemap.dy_if_flipped;
		}
		else
		{
			tilemap.scrolly_delta = tilemap.dy;
		}
		if(( attributes&TILEMAP_FLIPX ) != 0)
		{
			tilemap.orientation ^= ORIENTATION_FLIP_X;
			tilemap.scrollx_delta = tilemap.dx_if_flipped;
		}
		else
		{
			tilemap.scrollx_delta = tilemap.dx;
		}

		mappings_update( tilemap );
		tilemap_mark_all_tiles_dirty( tilemap );
	}
    }
    
public static void tilemap_set_clip(struct_tilemap tilemap, rectangle clip) {
        int left, top, right, bottom;
        if (clip != null) {
            left = clip.min_x;
            top = clip.min_y;
            right = clip.max_x + 1;
            bottom = clip.max_y + 1;
            if ((tilemap.orientation & ORIENTATION_SWAP_XY) != 0) {
                //SWAP(left, top)
                int temp = left;
                left = top;
                top = temp;
                //SWAP(right, bottom)
                int temp2 = right;
                right = bottom;
                bottom = temp2;
            }
            if ((tilemap.orientation & ORIENTATION_FLIP_X) != 0) {
                //SWAP(left, right)
                int temp = left;
                left = right;
                right = temp;
                left = screen_width - left;
                right = screen_width - right;
            }
            if ((tilemap.orientation & ORIENTATION_FLIP_Y) != 0) {
                //SWAP(top, bottom)
                int temp = top;
                top = bottom;
                bottom = temp;
                top = screen_height - top;
                bottom = screen_height - bottom;
            }
        } else {
            left = 0;
            top = 0;
            right = tilemap.cached_width;
            bottom = tilemap.cached_height;
        }
        tilemap.clip_left = left;
        tilemap.clip_right = right;
        tilemap.clip_top = top;
        tilemap.clip_bottom = bottom;
//	logerror("clip: %d,%d,%d,%d\n", left,top,right,bottom );
    }

/*TODO*////***********************************************************************************/
/*TODO*///
/*TODO*///void tilemap_set_scroll_cols( struct tilemap *tilemap, int n )
/*TODO*///{
/*TODO*///	if( tilemap->orientation & ORIENTATION_SWAP_XY )
/*TODO*///	{
/*TODO*///		if (tilemap->scroll_rows != n)
/*TODO*///		{
/*TODO*///			tilemap->scroll_rows = n;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		if (tilemap->scroll_cols != n)
/*TODO*///		{
/*TODO*///			tilemap->scroll_cols = n;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void tilemap_set_scroll_rows( struct tilemap *tilemap, int n )
/*TODO*///{
/*TODO*///	if( tilemap->orientation & ORIENTATION_SWAP_XY )
/*TODO*///	{
/*TODO*///		if (tilemap->scroll_cols != n)
/*TODO*///		{
/*TODO*///			tilemap->scroll_cols = n;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		if (tilemap->scroll_rows != n)
/*TODO*///		{
/*TODO*///			tilemap->scroll_rows = n;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}

    /***********************************************************************************/

    public static void tilemap_mark_tile_dirty( struct_tilemap tilemap, int memory_offset )
    {
            if( memory_offset<tilemap.max_memory_offset )
            {
                    int cached_indx = tilemap.memory_offset_to_cached_indx[memory_offset];
                    if( cached_indx>=0 )
                    {
                            tilemap.u8_dirty_vram[cached_indx] = 1;
                    }
            }
    }

    public static void tilemap_mark_all_tiles_dirty( struct_tilemap tilemap )
    {
            if( tilemap==ALL_TILEMAPS )
            {
                    tilemap = first_tilemap;
                    while( tilemap != null )
                    {
                            tilemap_mark_all_tiles_dirty( tilemap );
                            tilemap = tilemap.next;
                    }
            }
            else
            {
                    memset( tilemap.u8_dirty_vram, 1, tilemap.num_tiles );
            }
    }

/*TODO*///static void tilemap_mark_all_pixels_dirty( struct tilemap *tilemap )
/*TODO*///{
/*TODO*///	if( tilemap==ALL_TILEMAPS )
/*TODO*///	{
/*TODO*///		tilemap = first_tilemap;
/*TODO*///		while( tilemap )
/*TODO*///		{
/*TODO*///			tilemap_mark_all_pixels_dirty( tilemap );
/*TODO*///			tilemap = tilemap->next;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		/* invalidate all offscreen tiles */
/*TODO*///		UINT32 cached_tile_indx;
/*TODO*///		UINT32 num_pens = tilemap->tile_size*tilemap->tile_size;
/*TODO*///		for( cached_tile_indx=0; cached_tile_indx<tilemap->num_tiles; cached_tile_indx++ )
/*TODO*///		{
/*TODO*///			if( !tilemap->visible[cached_tile_indx] )
/*TODO*///			{
/*TODO*///				unregister_pens( &tilemap->cached_tile_info[cached_tile_indx], num_pens );
/*TODO*///				tilemap->dirty_vram[cached_tile_indx] = 1;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		memset( tilemap->dirty_pixels, 1, tilemap->num_tiles );
/*TODO*///	}
/*TODO*///}
/*TODO*///
    public static void tilemap_dirty_palette(UBytePtr dirty_pens) {
        System.out.println("tilemap_dirty_palette TODO");
        /*TODO*///	UINT32 *color_base = Machine->remapped_colortable;
/*TODO*///	struct tilemap *tilemap = first_tilemap;
/*TODO*///	while( tilemap )
/*TODO*///	{
/*TODO*///		if( !tilemap->tile_dirty_map)
/*TODO*///			tilemap_mark_all_pixels_dirty( tilemap );
/*TODO*///		else
/*TODO*///		{
/*TODO*///			UINT8 *dirty_map = tilemap->tile_dirty_map;
/*TODO*///			int i, j, pen, row, col;
/*TODO*///			int step = 1 << tilemap->tile_granularity;
/*TODO*///			int count = 1 << tilemap->tile_depth;
/*TODO*///			int limit = Machine->drv->total_colors - count;
/*TODO*///			pen = 0;
/*TODO*///			for( i=0; i<limit; i+=step )
/*TODO*///			{
/*TODO*///				for( j=0; j<count; j++ )
/*TODO*///					if( dirty_pens[i+j] )
/*TODO*///					{
/*TODO*///						dirty_map[pen++] = 1;
/*TODO*///						goto next;
/*TODO*///					}
/*TODO*///				dirty_map[pen++] = 0;
/*TODO*///			next:
/*TODO*///				;
/*TODO*///			}
/*TODO*///
/*TODO*///			i = 0;
/*TODO*///			for( row=0; row<tilemap->num_cached_rows; row++ )
/*TODO*///			{
/*TODO*///				for( col=0; col<tilemap->num_cached_cols; col++ )
/*TODO*///				{
/*TODO*///					if (!tilemap->dirty_vram[i] && !tilemap->dirty_pixels[i])
/*TODO*///					{
/*TODO*///						struct cached_tile_info *cached_tile = tilemap->cached_tile_info+i;
/*TODO*///						j = (cached_tile->pal_data - color_base) >> tilemap->tile_granularity;
/*TODO*///						if( dirty_map[j] )
/*TODO*///						{
/*TODO*///							if( tilemap->visible[i] )
/*TODO*///							{
/*TODO*///								tilemap->draw_tile( tilemap, i, col, row );
/*TODO*///							}
/*TODO*///							else
/*TODO*///							{
/*TODO*///								tilemap->dirty_pixels[i] = 1;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///					i++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///		tilemap = tilemap->next;
/*TODO*///	}
    }
    /*TODO*///
/*TODO*////***********************************************************************************/
/*TODO*///
/*TODO*///static void draw_bitmask(
/*TODO*///		struct osd_bitmap *mask,
/*TODO*///		UINT32 x0, UINT32 y0,
/*TODO*///		UINT32 tile_size,
/*TODO*///		const UINT8 *maskdata,
/*TODO*///		UINT32 flags )
/*TODO*///{
/*TODO*///	UINT8 data;
/*TODO*///	UINT8 *pDest;
/*TODO*///	int x,sy,y1,y2,dy;
/*TODO*///
/*TODO*///	if( flags&TILE_FLIPY )
/*TODO*///	{
/*TODO*///		y1 = y0+tile_size-1;
/*TODO*///		y2 = y1-tile_size;
/*TODO*/// 		dy = -1;
/*TODO*/// 	}
/*TODO*/// 	else
/*TODO*/// 	{
/*TODO*///		y1 = y0;
/*TODO*///		y2 = y1+tile_size;
/*TODO*/// 		dy = 1;
/*TODO*/// 	}
/*TODO*///	/* to do:
/*TODO*///	 * 	support screen orientation here, so pre-rotate code can be removed from
/*TODO*///	 *	namcos1,namcos2,namconb1
/*TODO*///	 */
/*TODO*///	if( flags&TILE_FLIPX )
/*TODO*///	{
/*TODO*///		tile_size--;
/*TODO*///		for( sy=y1; sy!=y2; sy+=dy )
/*TODO*///		{
/*TODO*///			pDest = mask->line[sy]+x0/8;
/*TODO*///			for( x=tile_size/8; x>=0; x-- )
/*TODO*///			{
/*TODO*///				data = flip_bit_table[*maskdata++];
/*TODO*///				pDest[x] = data;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		for( sy=y1; sy!=y2; sy+=dy )
/*TODO*///		{
/*TODO*///			pDest = mask->line[sy]+x0/8;
/*TODO*///			for( x=0; x<tile_size/8; x++ )
/*TODO*///			{
/*TODO*///				data = *maskdata++;
/*TODO*///				pDest[x] = data;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///}

    public static void draw_color_mask(
            struct_tilemap tilemap,
            osd_bitmap mask,
            int /*UINT32*/ x0, int /*UINT32*/ y0,
            int /*UINT32*/ tile_size,
            UBytePtr pPenData,
            UShortArray clut,
            int transparent_color,
            int /*UINT32*/ flags,
            int pitch )
    {
        
    	IntArray pPenToPixel = new IntArray(tilemap.pPenToPixel[flags&(TILE_SWAPXY|TILE_FLIPY|TILE_FLIPX)]);
        pPenToPixel.offset=0;
    	int tx,ty;
    	UBytePtr pSource;
    	int /*UINT8*/ data;
    	int /*UINT32*/ yx;
    
    	if(( flags&TILE_4BPP ) != 0)
    	{
    		for( ty=tile_size; ty!=0; ty-- )
    		{
    			pSource = new UBytePtr(pPenData);
    			for( tx=tile_size/2; tx!=0; tx-- )
    			{
    				data = pSource.readinc();
    				yx = pPenToPixel.read(); pPenToPixel.offset++;
    				if( clut.read(data&0xf)!=transparent_color )
    				{
                                        int _i=mask.line[y0+yx/MAX_TILESIZE].read((x0+(yx%MAX_TILESIZE))/8);
    					mask.line[y0+yx/MAX_TILESIZE].write((x0+(yx%MAX_TILESIZE))/8, _i | 0x80>>(yx%8));
    				}
    				yx = pPenToPixel.read(); pPenToPixel.offset++;
    				if( clut.read(data>>4)!=transparent_color )
    				{
                                        int _i=mask.line[y0+yx/MAX_TILESIZE].read((x0+(yx%MAX_TILESIZE))/8);
    					mask.line[y0+yx/MAX_TILESIZE].write((x0+(yx%MAX_TILESIZE))/8, _i | 0x80>>(yx%8));
    				}
    			}
    			pPenData.offset += pitch/2;
    		}
    	}
    	else
    	{
    		for( ty=tile_size; ty!=0; ty-- )
    		{
    			pSource = new UBytePtr(pPenData);
                        pSource.offset=0;
    			for( tx=tile_size; tx!=0; tx-- )
    			{
    				data = pSource.readinc();
    				yx = pPenToPixel.read(); pPenToPixel.offset++;
    				if( clut.read(data)!=transparent_color )
    				{
                                        int _i=mask.line[y0+yx/MAX_TILESIZE].read((x0+(yx%MAX_TILESIZE))/8);
    					mask.line[y0+yx/MAX_TILESIZE].write((x0+(yx%MAX_TILESIZE))/8, _i | 0x80>>(yx%8));
    				}
    			}
    			pPenData.inc( pitch );
    		}
    	}
    }
    
/*TODO*///static void draw_pen_mask(
/*TODO*///	struct tilemap *tilemap,
/*TODO*///	struct osd_bitmap *mask,
/*TODO*///	UINT32 x0, UINT32 y0,
/*TODO*///	UINT32 tile_size,
/*TODO*///	const UINT8 *pPenData,
/*TODO*///	int transparent_pen,
/*TODO*///	UINT32 flags,
/*TODO*///	int pitch )
/*TODO*///{
/*TODO*///	UINT32 *pPenToPixel = tilemap->pPenToPixel[flags&(TILE_SWAPXY|TILE_FLIPY|TILE_FLIPX)];
/*TODO*///	int tx,ty;
/*TODO*///	const UINT8 *pSource;
/*TODO*///	UINT8 data;
/*TODO*///	UINT32 yx;
/*TODO*///
/*TODO*///	if( flags&TILE_4BPP )
/*TODO*///	{
/*TODO*///		for( ty=tile_size; ty!=0; ty-- )
/*TODO*///		{
/*TODO*///			pSource = pPenData;
/*TODO*///			for( tx=tile_size/2; tx!=0; tx-- )
/*TODO*///			{
/*TODO*///				data = *pSource++;
/*TODO*///				yx = *pPenToPixel++;
/*TODO*///				if( (data&0xf)!=transparent_pen )
/*TODO*///				{
/*TODO*///					mask->line[y0+yx/MAX_TILESIZE][(x0+(yx%MAX_TILESIZE))/8] |= 0x80>>(yx%8);
/*TODO*///				}
/*TODO*///				yx = *pPenToPixel++;
/*TODO*///				if( (data>>4)!=transparent_pen )
/*TODO*///				{
/*TODO*///					mask->line[y0+yx/MAX_TILESIZE][(x0+(yx%MAX_TILESIZE))/8] |= 0x80>>(yx%8);
/*TODO*///				}
/*TODO*///			}
/*TODO*///			pPenData += pitch/2;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		for( ty=tile_size; ty!=0; ty-- )
/*TODO*///		{
/*TODO*///			pSource = pPenData;
/*TODO*///			for( tx=tile_size; tx!=0; tx-- )
/*TODO*///			{
/*TODO*///				data = *pSource++;
/*TODO*///				yx = *pPenToPixel++;
/*TODO*///				if( data!=transparent_pen )
/*TODO*///				{
/*TODO*///					mask->line[y0+yx/MAX_TILESIZE][(x0+(yx%MAX_TILESIZE))/8] |= 0x80>>(yx%8);
/*TODO*///				}
/*TODO*///			}
/*TODO*///			pPenData += pitch;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
    
    static void draw_mask(
    	struct_tilemap tilemap,
    	osd_bitmap mask,
    	int x0, int y0,
    	int tile_size,
    	UBytePtr pPenData,
    	int transmask,
    	int flags,
    	int pitch )
    {
    	IntArray pPenToPixel = new IntArray(tilemap.pPenToPixel[flags&(TILE_SWAPXY|TILE_FLIPY|TILE_FLIPX)]);
    	int tx,ty;
    	UBytePtr pSource;
    	int /*UINT8*/ data;
    	int /*UINT32*/ yx;
    
    	if(( flags&TILE_4BPP ) != 0)
    	{
    		for( ty=tile_size; ty!=0; ty-- )
    		{
    			pSource = pPenData;
    			for( tx=tile_size/2; tx!=0; tx-- )
    			{
    				data = pSource.readinc();
    				yx = pPenToPixel.read(); pPenToPixel.offset++;
    				if( ((1<<(data&0xf))&transmask) == 0)
    				{
                                        int _i=mask.line[y0+yx/MAX_TILESIZE].read((x0+(yx%MAX_TILESIZE))/8);
    					mask.line[y0+yx/MAX_TILESIZE].write((x0+(yx%MAX_TILESIZE))/8,  _i| 0x80>>(yx%8));
    				}
    				yx = pPenToPixel.read(); pPenToPixel.offset++;
    				if( ((1<<(data>>4))&transmask) == 0)
    				{
                                        int _i=mask.line[y0+yx/MAX_TILESIZE].read((x0+(yx%MAX_TILESIZE))/8);
    					mask.line[y0+yx/MAX_TILESIZE].write((x0+(yx%MAX_TILESIZE))/8, _i| 0x80>>(yx%8));
    				}
    			}
    			pPenData.inc( pitch/2 );
    		}
    	}
    	else
    	{
    		for( ty=tile_size; ty!=0; ty-- )
    		{
    			pSource = pPenData;
    			for( tx=tile_size; tx!=0; tx-- )
    			{
    				data = pSource.readinc();
    				yx = pPenToPixel.read(); pPenToPixel.offset++;
    				if( ((1<<data)&transmask) == 0)
    				{
                                        int _i=mask.line[y0+yx/MAX_TILESIZE].read((x0+(yx%MAX_TILESIZE))/8);
    					mask.line[y0+yx/MAX_TILESIZE].write((x0+(yx%MAX_TILESIZE))/8, _i| 0x80>>(yx%8));
    				}
    			}
    			pPenData.inc( pitch );
    		}
    	}
    }

    static void ClearMask( osd_bitmap bitmap, int tile_size, int x0, int y0 )
    {
            UBytePtr pDest;
            int ty,tx;
            for( ty=0; ty<tile_size; ty++ )
            {
                    pDest = new UBytePtr(bitmap.line[y0+ty], x0/8);
                    for( tx=tile_size/8; tx!=0; tx-- )
                    {
                            pDest.writeinc( 0x00 );
                    }
            }
    }

    static int InspectMask( osd_bitmap bitmap, int tile_size, int x0, int y0 )
    {
    	UBytePtr pSource;
    	int ty,tx;
    
    	switch( bitmap.line[y0].read(x0/8) )
    	{
    	case 0xff: /* possibly opaque */
    		for( ty=0; ty<tile_size; ty++ )
    		{
    			pSource = new UBytePtr(bitmap.line[y0+ty], x0/8);
    			for( tx=tile_size/8; tx!=0; tx-- )
    			{
    				if( pSource.readinc() != 0xff ) return TILE_MASKED;
    			}
    		}
    		return TILE_OPAQUE;
    
    	case 0x00: /* possibly transparent */
    		for( ty=0; ty<tile_size; ty++ )
    		{
    			pSource = new UBytePtr(bitmap.line[y0+ty], x0/8);
    			for( tx=tile_size/8; tx!=0; tx-- )
    			{
    				if( pSource.readinc() != 0x00 ) return TILE_MASKED;
    			}
    		}
    		return TILE_TRANSPARENT;
    
    	default:
    		return TILE_MASKED;
    	}
    }
    
    static void render_mask( struct_tilemap tilemap, int cached_indx )
    {
    	cached_tile_info cached_tile_info = tilemap.cached_tile_info[cached_indx];
    	int /*UINT32*/ col = cached_indx%tilemap.num_cached_cols;
    	int /*UINT32*/ row = cached_indx/tilemap.num_cached_cols;
    	int /*UINT32*/ type = tilemap.type;
    	int /*UINT32*/ tile_size = tilemap.tile_size;
    	int /*UINT32*/ y0 = tile_size*row;
    	int /*UINT32*/ x0 = tile_size*col;
    	int pitch = tile_size + cached_tile_info.skip;
    	int /*UINT32*/ pen_usage = cached_tile_info.u32_pen_usage;
    	UBytePtr pen_data = new UBytePtr(cached_tile_info.pen_data);
    	int /*UINT32*/ flags = cached_tile_info.u32_flags;
    
    	if(( type & TILEMAP_BITMASK ) != 0)
    	{
            throw new UnsupportedOperationException("Not supported yet.");
    		/* hack; games using TILEMAP_BITMASK may pass in NULL or (~0) to indicate
    		 * tiles that are wholly transparent or opaque.
    		 */
/*TODO*///    		if( tile_info.mask_data == TILEMAP_BITMASK_TRANSPARENT )
/*TODO*///    		{
/*TODO*///    			tilemap.foreground.data_row[row][col] = TILE_TRANSPARENT;
/*TODO*///    		}
/*TODO*///    		else if( tile_info.mask_data == TILEMAP_BITMASK_OPAQUE )
/*TODO*///    		{
/*TODO*///    			tilemap.foreground.data_row[row][col] = TILE_OPAQUE;
/*TODO*///    		}
/*TODO*///    		else
/*TODO*///    		{
/*TODO*///    			/* We still inspect the tile data, since not all games
/*TODO*///    			 * using TILEMAP_BITMASK use the above hack.
/*TODO*///    			 */
/*TODO*///    			draw_bitmask( tilemap.foreground.bitmask,
/*TODO*///    				x0, y0, tile_size, tile_info.mask_data, flags );
/*TODO*///    
/*TODO*///    			tilemap.foreground.data_row[row][col] =
/*TODO*///    				InspectMask( tilemap.foreground.bitmask, tile_size, x0, y0 );
/*TODO*///    		}
    	}
    	else if(( type & TILEMAP_SPLIT ) != 0)
    	{
    		int /*UINT32*/ fgmask = tilemap.fgmask[(flags>>TILE_SPLIT_OFFSET)&3];
    		int /*UINT32*/ bgmask = tilemap.bgmask[(flags>>TILE_SPLIT_OFFSET)&3];
    
    		if( (pen_usage & fgmask)==0 || (flags&TILE_IGNORE_TRANSPARENCY)!=0 )
    		{ /* foreground totally opaque */
    			tilemap.foreground.data_row[row].write(col, TILE_OPAQUE);
    		}
    		else if( (pen_usage & ~fgmask)==0 )
    		{ /* foreground transparent */
    			ClearMask( tilemap.background.bitmask, tile_size, x0, y0 );
    			draw_mask( tilemap,tilemap.background.bitmask,
    				x0, y0, tile_size, pen_data, bgmask, flags, pitch );
    			tilemap.foreground.data_row[row].write(col, TILE_TRANSPARENT);
    		}
    		else
    		{ /* masked tile */
    			ClearMask( tilemap.foreground.bitmask, tile_size, x0, y0 );
    			draw_mask( tilemap,tilemap.foreground.bitmask,
    				x0, y0, tile_size, pen_data, fgmask, flags, pitch );
    			tilemap.foreground.data_row[row].write(col, TILE_MASKED);
    		}
    
    		if( (pen_usage & bgmask)==0 || (flags&TILE_IGNORE_TRANSPARENCY)!=0 )
    		{ /* background totally opaque */
    			tilemap.background.data_row[row].write(col, TILE_OPAQUE);
    		}
    		else if( (pen_usage & ~bgmask)==0 )
    		{ /* background transparent */
    			ClearMask( tilemap.foreground.bitmask, tile_size, x0, y0 );
    			draw_mask( tilemap,tilemap.foreground.bitmask,
    				x0, y0, tile_size, pen_data, fgmask, flags, pitch );
    				tilemap.foreground.data_row[row].write(col, TILE_MASKED);
    			tilemap.background.data_row[row].write(col, TILE_TRANSPARENT);
    		}
    		else
    		{ /* masked tile */
    			ClearMask( tilemap.background.bitmask, tile_size, x0, y0 );
    			draw_mask( tilemap,tilemap.background.bitmask,
    				x0, y0, tile_size, pen_data, bgmask, flags, pitch );
    			tilemap.background.data_row[row].write(col, TILE_MASKED);
    		}
    	}
    	else if( type==TILEMAP_TRANSPARENT )
    	{
    		if( pen_usage != 0 )
    		{
    			int /*UINT32*/ fgmask = 1 << tilemap.transparent_pen;
    		 	if(( flags&TILE_IGNORE_TRANSPARENCY ) != 0) fgmask = 0;
    			if( pen_usage == fgmask )
    			{
    				tilemap.foreground.data_row[row].write(col, TILE_TRANSPARENT);
    			}
    			else if(( pen_usage & fgmask ) != 0)
    			{
    				ClearMask( tilemap.foreground.bitmask, tile_size, x0, y0 );
    				draw_mask( tilemap,tilemap.foreground.bitmask,
    					x0, y0, tile_size, pen_data, fgmask, flags, pitch );
    				tilemap.foreground.data_row[row].write(col, TILE_MASKED);
    			}
    			else
    			{
    				tilemap.foreground.data_row[row].write(col, TILE_OPAQUE);
    			}
    		}
    		else
    		{
    			ClearMask( tilemap.foreground.bitmask, tile_size, x0, y0 );
                        throw new UnsupportedOperationException("Not supported yet!");
/*TODO*///    			draw_pen_mask(
/*TODO*///    					tilemap,tilemap.foreground.bitmask,
/*TODO*///    					x0, y0, tile_size, pen_data, tilemap.transparent_pen, flags, pitch );
/*TODO*///    			tilemap.foreground.data_row[row].write(col,
/*TODO*///    				InspectMask( tilemap.foreground.bitmask, tile_size, x0, y0 ));
    		}
    	}
    	else if( type==TILEMAP_TRANSPARENT_COLOR )
    	{
    		ClearMask( tilemap.foreground.bitmask, tile_size, x0, y0 );
                
    
    		draw_color_mask(
    				tilemap,tilemap.foreground.bitmask,
    				x0, y0, tile_size, new UBytePtr(pen_data),
    				new UShortArray(Machine.game_colortable, (cached_tile_info.pal_data.offset - Machine.remapped_colortable.offset)),
    				tilemap.transparent_pen, flags, pitch );
    
    		tilemap.foreground.data_row[row].write(col,
    				InspectMask( tilemap.foreground.bitmask, tile_size, x0, y0 ));
    	}
    	else
    	{
    		tilemap.foreground.data_row[row].write(col, TILE_OPAQUE);
    	}
    }
    
    static void update_tile_info( struct_tilemap tilemap )
    {
    	int[] logical_flip_to_cached_flip = tilemap.logical_flip_to_cached_flip;
    	int /*UINT32*/ num_pens = tilemap.tile_size * tilemap.tile_size;
    	int /*UINT32*/ num_tiles = tilemap.num_tiles;
    	int /*UINT32*/ cached_indx;
    	int[] /*UINT8*/ visible = tilemap.u8_visible;
    	int[] /*UINT8*/ dirty_vram = tilemap.u8_dirty_vram;
    	int[] /*UINT8*/ dirty_pixels = tilemap.u8_dirty_pixels;
    
    	//memset( tile_info, 0x00, sizeof(tile_info) ); /* initialize defaults */
        tile_info = new struct_tile_info();
    
    	for( cached_indx=0; cached_indx<num_tiles; cached_indx++ )
    	{
    		if( visible[cached_indx]!=0 && dirty_vram[cached_indx]!=0 )
    		{
    			cached_tile_info cached_tile_info = tilemap.cached_tile_info[cached_indx];
    			int /*UINT32*/ memory_offset = tilemap.cached_indx_to_memory_offset[cached_indx];
    			unregister_pens( cached_tile_info, num_pens );
    			tilemap.tile_get_info.handler( memory_offset );
    			{
    				int /*UINT32*/ flags = tile_info.flags;
    				cached_tile_info.u32_flags = (flags&0xfc)|logical_flip_to_cached_flip[flags&0x3];
    			}
    			cached_tile_info.u32_pen_usage = tile_info.pen_usage;
    			cached_tile_info.pen_data = tile_info.pen_data;
    			cached_tile_info.pal_data = tile_info.pal_data;
    			cached_tile_info.skip = tile_info.skip;
    			tilemap.u8_priority[cached_indx] = (char) tile_info.priority;
    			register_pens( cached_tile_info, num_pens );
    			dirty_pixels[cached_indx] = 1;
    			dirty_vram[cached_indx] = 0;
    			render_mask( tilemap, cached_indx );
    		}
    	}
    }

    static void update_visible( struct_tilemap tilemap )
    {
            // visibility marking is not currently implemented
            memset( tilemap.u8_visible, 1, tilemap.num_tiles );
    }

    public static void tilemap_update( struct_tilemap tilemap )
    {
    /*TODO*///profiler_mark(PROFILER_TILEMAP_UPDATE);
            if( tilemap==ALL_TILEMAPS )
            {
                    tilemap = first_tilemap;
                    while( tilemap != null )
                    {
                            tilemap_update( tilemap );
                            tilemap = tilemap.next;
                    }
            }
            else if( tilemap.enable != 0)
            {
                    tilemap.bNeedRender = 1;
                    update_visible( tilemap );
                    update_tile_info( tilemap );
            }
    /*TODO*///profiler_mark(PROFILER_END);
    }

/*TODO*////***********************************************************************************/
/*TODO*///
/*TODO*///void tilemap_set_scrolldx( struct tilemap *tilemap, int dx, int dx_if_flipped )
/*TODO*///{
/*TODO*///	tilemap->dx = dx;
/*TODO*///	tilemap->dx_if_flipped = dx_if_flipped;
/*TODO*///	tilemap->scrollx_delta = ( tilemap->attributes & TILEMAP_FLIPX )?dx_if_flipped:dx;
/*TODO*///}
/*TODO*///
/*TODO*///void tilemap_set_scrolldy( struct tilemap *tilemap, int dy, int dy_if_flipped )
/*TODO*///{
/*TODO*///	tilemap->dy = dy;
/*TODO*///	tilemap->dy_if_flipped = dy_if_flipped;
/*TODO*///	tilemap->scrolly_delta = ( tilemap->attributes & TILEMAP_FLIPY )?dy_if_flipped:dy;
/*TODO*///}

    public static void tilemap_set_scrollx( struct_tilemap tilemap, int which, int value )
    {
            value = tilemap.scrollx_delta-value;

            if(( tilemap.orientation & ORIENTATION_SWAP_XY ) != 0)
            {
                    if(( tilemap.orientation & ORIENTATION_FLIP_X ) != 0) which = tilemap.scroll_cols-1 - which;
                    if(( tilemap.orientation & ORIENTATION_FLIP_Y ) != 0) value = screen_height-tilemap.cached_height-value;
                    if( tilemap.colscroll[which]!=value )
                    {
                            tilemap.colscroll[which] = value;
                    }
            }
            else
            {
                    if(( tilemap.orientation & ORIENTATION_FLIP_Y ) != 0) which = tilemap.scroll_rows-1 - which;
                    if(( tilemap.orientation & ORIENTATION_FLIP_X ) != 0) value = screen_width-tilemap.cached_width-value;
                    if( tilemap.rowscroll[which]!=value )
                    {
                            tilemap.rowscroll[which] = value;
                    }
            }
    }

    public static void tilemap_set_scrolly( struct_tilemap tilemap, int which, int value )
    {
    	value = tilemap.scrolly_delta - value;
    
    	if(( tilemap.orientation & ORIENTATION_SWAP_XY ) != 0)
    	{
    		if(( tilemap.orientation & ORIENTATION_FLIP_Y ) != 0) which = tilemap.scroll_rows-1 - which;
    		if(( tilemap.orientation & ORIENTATION_FLIP_X ) != 0) value = screen_width-tilemap.cached_width-value;
    		if( tilemap.rowscroll[which]!=value )
    		{
    			tilemap.rowscroll[which] = value;
    		}
    	}
    	else
    	{
    		if(( tilemap.orientation & ORIENTATION_FLIP_X ) != 0) which = tilemap.scroll_cols-1 - which;
    		if(( tilemap.orientation & ORIENTATION_FLIP_Y ) != 0) value = screen_height-tilemap.cached_height-value;
    		if( tilemap.colscroll[which]!=value )
    		{
    			tilemap.colscroll[which] = value;
    		}
    	}
    }
    
    /***********************************************************************************/

    public static void tilemap_draw( osd_bitmap dest, struct_tilemap tilemap, int /*UINT32*/ flags, int /*UINT32*/ priority )
    {
            int xpos,ypos;
    /*TODO*///profiler_mark(PROFILER_TILEMAP_DRAW);
    	tmap_render( tilemap );
    
    	if( tilemap.enable != 0)
    	{
            
    		DrawHandlerPtr draw;
    
    		int rows = tilemap.scroll_rows;
    		int[] rowscroll = tilemap.rowscroll;
    		int cols = tilemap.scroll_cols;
    		int[] colscroll = tilemap.colscroll;
    
    		int left = tilemap.clip_left;
    		int right = tilemap.clip_right;
    		int top = tilemap.clip_top;
    		int bottom = tilemap.clip_bottom;
    
    		int tile_size = tilemap.tile_size;
    
    		blit.screen = dest;
    		if( dest != null )
    		{
    			blit.pixmap = tilemap.pixmap;
    			blit.source_line_offset = tilemap.pixmap_line_offset;
    
    			blit.dest_line_offset = dest.line[1].offset - dest.line[0].offset;
    
    			switch( dest.depth )
    			{
    			case 15:
    			case 16:
    				blit.dest_line_offset /= 2;
    				blit.source_line_offset /= 2;
    				break;
    			case 32:
    				blit.dest_line_offset /= 4;
    				blit.source_line_offset /= 4;
    				break;
    			}
    			blit.dest_row_offset = tile_size*blit.dest_line_offset;
    		}
    
    
    		if( tilemap.type==TILEMAP_OPAQUE || (flags&TILEMAP_IGNORE_TRANSPARENCY)!=0 )
    		{
    			draw = tilemap.draw_opaque;
    		}
    		else
    		{
    			if(( flags & TILEMAP_ALPHA ) != 0)
    				draw = tilemap.draw_alpha;
    			else
    				draw = tilemap.draw;
    
    			if(( flags&TILEMAP_BACK ) != 0)
    			{
    				blit.bitmask = tilemap.background.bitmask;
    				blit.mask_line_offset = tilemap.background.line_offset;
    				blit.mask_data_row = tilemap.background.data_row;
    			}
    			else
    			{
    				blit.bitmask = tilemap.foreground.bitmask;
    				blit.mask_line_offset = tilemap.foreground.line_offset;
    				blit.mask_data_row = tilemap.foreground.data_row;
    			}
    
    			blit.mask_row_offset = tile_size*blit.mask_line_offset;
    		}
    
    		blit.source_row_offset = tile_size*blit.source_line_offset;
    
    		blit.priority_data_row = tilemap.priority_row;
    		blit.source_width = tilemap.cached_width;
    		blit.source_height = tilemap.cached_height;
    		blit.tile_priority = flags&0xf;
    		blit.tilemap_priority_code = priority;
    
    		if( rows == 1 && cols == 1 )
    		{ /* XY scrolling playfield */
    			int scrollx = rowscroll[0];
    			int scrolly = colscroll[0];
    
    			if( scrollx < 0 )
    			{
    				scrollx = blit.source_width - (-scrollx) % blit.source_width;
    			}
    			else
    			{
    				scrollx = scrollx % blit.source_width;
    			}
    
    			if( scrolly < 0 )
    			{
    				scrolly = blit.source_height - (-scrolly) % blit.source_height;
    			}
    			else
    			{
    				scrolly = scrolly % blit.source_height;
    			}
    
    	 		blit.clip_left = left;
    	 		blit.clip_top = top;
    	 		blit.clip_right = right;
    	 		blit.clip_bottom = bottom;
    
    			for(
    				ypos = scrolly - blit.source_height;
    				ypos < blit.clip_bottom;
    				ypos += blit.source_height )
    			{
    				for(
    					xpos = scrollx - blit.source_width;
    					xpos < blit.clip_right;
    					xpos += blit.source_width )
    				{
    					draw.handler( xpos,ypos );
    				}
    			}
    		}
    		else if( rows == 1 )
    		{ /* scrolling columns + horizontal scroll */
    			int col = 0;
    			int colwidth = blit.source_width / cols;
    			int scrollx = rowscroll[0];
    
    			if( scrollx < 0 )
    			{
    				scrollx = blit.source_width - (-scrollx) % blit.source_width;
    			}
    			else
    			{
    				scrollx = scrollx % blit.source_width;
    			}
    
    			blit.clip_top = top;
    			blit.clip_bottom = bottom;
    
    			while( col < cols )
    			{
    				int cons = 1;
    				int scrolly = colscroll[col];
    
    	 			/* count consecutive columns scrolled by the same amount */
    				if( scrolly != TILE_LINE_DISABLED )
    				{
    					while( col + cons < cols &&	colscroll[col + cons] == scrolly ) cons++;
    
    					if( scrolly < 0 )
    					{
    						scrolly = blit.source_height - (-scrolly) % blit.source_height;
    					}
    					else
    					{
    						scrolly %= blit.source_height;
    					}
    
    					blit.clip_left = col * colwidth + scrollx;
    					if (blit.clip_left < left) blit.clip_left = left;
    					blit.clip_right = (col + cons) * colwidth + scrollx;
    					if (blit.clip_right > right) blit.clip_right = right;
    
    					for(
    						ypos = scrolly - blit.source_height;
    						ypos < blit.clip_bottom;
    						ypos += blit.source_height )
    					{
    						draw.handler( scrollx,ypos );
    					}
    
    					blit.clip_left = col * colwidth + scrollx - blit.source_width;
    					if (blit.clip_left < left) blit.clip_left = left;
    					blit.clip_right = (col + cons) * colwidth + scrollx - blit.source_width;
    					if (blit.clip_right > right) blit.clip_right = right;
    
    					for(
    						ypos = scrolly - blit.source_height;
    						ypos < blit.clip_bottom;
    						ypos += blit.source_height )
    					{
    						draw.handler( scrollx - blit.source_width,ypos );
    					}
    				}
    				col += cons;
    			}
    		}
    		else if( cols == 1 )
    		{ /* scrolling rows + vertical scroll */
    			int row = 0;
    			int rowheight = blit.source_height / rows;
    			int scrolly = colscroll[0];
    			if( scrolly < 0 )
    			{
    				scrolly = blit.source_height - (-scrolly) % blit.source_height;
    			}
    			else
    			{
    				scrolly = scrolly % blit.source_height;
    			}
    			blit.clip_left = left;
    			blit.clip_right = right;
    			while( row < rows )
    			{
    				int cons = 1;
    				int scrollx = rowscroll[row];
    				/* count consecutive rows scrolled by the same amount */
    				if( scrollx != TILE_LINE_DISABLED )
    				{
    					while( row + cons < rows &&	rowscroll[row + cons] == scrollx ) cons++;
    					if( scrollx < 0)
    					{
    						scrollx = blit.source_width - (-scrollx) % blit.source_width;
    					}
    					else
    					{
    						scrollx %= blit.source_width;
    					}
    					blit.clip_top = row * rowheight + scrolly;
    					if (blit.clip_top < top) blit.clip_top = top;
    					blit.clip_bottom = (row + cons) * rowheight + scrolly;
    					if (blit.clip_bottom > bottom) blit.clip_bottom = bottom;
    					for(
    						xpos = scrollx - blit.source_width;
    						xpos < blit.clip_right;
    						xpos += blit.source_width )
    					{
    						draw.handler( xpos,scrolly );
    					}
    					blit.clip_top = row * rowheight + scrolly - blit.source_height;
    					if (blit.clip_top < top) blit.clip_top = top;
    					blit.clip_bottom = (row + cons) * rowheight + scrolly - blit.source_height;
    					if (blit.clip_bottom > bottom) blit.clip_bottom = bottom;
    					for(
    						xpos = scrollx - blit.source_width;
    						xpos < blit.clip_right;
    						xpos += blit.source_width )
    					{
    						draw.handler( xpos,scrolly - blit.source_height );
    					}
    				}
    				row += cons;
    			}
    		}
    	}
    /*TODO*///profiler_mark(PROFILER_END);
    }
    
/*TODO*////***********************************************************************************/
/*TODO*///
/*TODO*///#else // DECLARE
/*TODO*////*
/*TODO*///	The following procedure body is #included several times by
/*TODO*///	tilemap.c to implement a suite of tilemap_draw subroutines.
/*TODO*///
/*TODO*///	The constant TILE_SIZE is different in each instance of this
/*TODO*///	code, allowing arithmetic shifts to be used by the compiler
/*TODO*///	instead of multiplies/divides.
/*TODO*///
/*TODO*///	This routine should be fairly optimal, for C code, though of
/*TODO*///	course there is room for improvement.
/*TODO*///
/*TODO*///	It renders pixels one row at a time, skipping over runs of totally
/*TODO*///	transparent tiles, and calling custom blitters to handle runs of
/*TODO*///	masked/totally opaque tiles.
/*TODO*///*/
/*TODO*///DECLARE( draw, (int xpos, int ypos),
/*TODO*///{
/*TODO*///	struct osd_bitmap *screen = blit.screen;
/*TODO*///	int tilemap_priority_code = blit.tilemap_priority_code;
/*TODO*///	int x1 = xpos;
/*TODO*///	int y1 = ypos;
/*TODO*///	int x2 = xpos+blit.source_width;
/*TODO*///	int y2 = ypos+blit.source_height;
/*TODO*///	DATA_TYPE *dest_baseaddr = NULL;
/*TODO*///	DATA_TYPE *dest_next;
/*TODO*///	int dy;
/*TODO*///	int count;
/*TODO*///	const DATA_TYPE *source0;
/*TODO*///	DATA_TYPE *dest0;
/*TODO*///	UINT8 *pmap0;
/*TODO*///	int i;
/*TODO*///	int row;
/*TODO*///	UINT8 *priority_data;
/*TODO*///	int tile_type;
/*TODO*///	int prev_tile_type;
/*TODO*///	int x_start;
/*TODO*///	int x_end;
/*TODO*///	int column;
/*TODO*///	int c1;
/*TODO*///	int c2; /* leftmost and rightmost visible columns in source tilemap */
/*TODO*///	int y; /* current screen line to render */
/*TODO*///	int y_next;
/*TODO*///	int num_pixels;
/*TODO*///	int priority_bitmap_row_offset;
/*TODO*///	UINT8 *priority_bitmap_baseaddr;
/*TODO*///	UINT8 *priority_bitmap_next;
/*TODO*///	int priority;
/*TODO*///	const DATA_TYPE *source_baseaddr;
/*TODO*///	const DATA_TYPE *source_next;
/*TODO*///	const UINT8 *mask0;
/*TODO*///	UINT8 *mask_data;
/*TODO*///	const UINT8 *mask_baseaddr;
/*TODO*///	const UINT8 *mask_next;
/*TODO*///
/*TODO*///	/* clip source coordinates */
/*TODO*///	if( x1<blit.clip_left ) x1 = blit.clip_left;
/*TODO*///	if( x2>blit.clip_right ) x2 = blit.clip_right;
/*TODO*///	if( y1<blit.clip_top ) y1 = blit.clip_top;
/*TODO*///	if( y2>blit.clip_bottom ) y2 = blit.clip_bottom;
/*TODO*///
/*TODO*///	if( x1<x2 && y1<y2 )
/*TODO*///	{ /* do nothing if totally clipped */
/*TODO*///		priority_bitmap_row_offset = priority_bitmap_line_offset*TILE_SIZE;
/*TODO*///		priority_bitmap_baseaddr = xpos + (UINT8 *)priority_bitmap->line[y1];
/*TODO*///		priority = blit.tile_priority;
/*TODO*///		if( screen )
/*TODO*///		{
/*TODO*///			dest_baseaddr = xpos + (DATA_TYPE *)screen->line[y1];
/*TODO*///		}
/*TODO*///
/*TODO*///		/* convert screen coordinates to source tilemap coordinates */
/*TODO*///		x1 -= xpos;
/*TODO*///		y1 -= ypos;
/*TODO*///		x2 -= xpos;
/*TODO*///		y2 -= ypos;
/*TODO*///
/*TODO*///		source_baseaddr = (DATA_TYPE *)blit.pixmap->line[y1];
/*TODO*///		mask_baseaddr = blit.bitmask->line[y1];
/*TODO*///
/*TODO*///		c1 = x1/TILE_SIZE; /* round down */
/*TODO*///		c2 = (x2+TILE_SIZE-1)/TILE_SIZE; /* round up */
/*TODO*///
/*TODO*///		y = y1;
/*TODO*///		y_next = TILE_SIZE*(y1/TILE_SIZE) + TILE_SIZE;
/*TODO*///		if( y_next>y2 ) y_next = y2;
/*TODO*///
/*TODO*///		dy = y_next-y;
/*TODO*///		dest_next = dest_baseaddr + dy*blit.dest_line_offset;
/*TODO*///		priority_bitmap_next = priority_bitmap_baseaddr + dy*priority_bitmap_line_offset;
/*TODO*///		source_next = source_baseaddr + dy*blit.source_line_offset;
/*TODO*///		mask_next = mask_baseaddr + dy*blit.mask_line_offset;
/*TODO*///		for(;;)
/*TODO*///		{
/*TODO*///			row = y/TILE_SIZE;
/*TODO*///			mask_data = blit.mask_data_row[row];
/*TODO*///			priority_data = blit.priority_data_row[row];
/*TODO*///			prev_tile_type = TILE_TRANSPARENT;
/*TODO*///			x_start = x1;
/*TODO*///
/*TODO*///			for( column=c1; column<=c2; column++ )
/*TODO*///			{
/*TODO*///				if( column==c2 || priority_data[column]!=priority )
/*TODO*///				{
/*TODO*///					tile_type = TILE_TRANSPARENT;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					tile_type = mask_data[column];
/*TODO*///				}
/*TODO*///
/*TODO*///				if( tile_type!=prev_tile_type )
/*TODO*///				{
/*TODO*///					x_end = column*TILE_SIZE;
/*TODO*///					if( x_end<x1 ) x_end = x1;
/*TODO*///					if( x_end>x2 ) x_end = x2;
/*TODO*///
/*TODO*///					if( prev_tile_type != TILE_TRANSPARENT )
/*TODO*///					{
/*TODO*///						if( prev_tile_type == TILE_MASKED )
/*TODO*///						{
/*TODO*///							count = (x_end+7)/8 - x_start/8;
/*TODO*///							mask0 = mask_baseaddr + x_start/8;
/*TODO*///							source0 = source_baseaddr + (x_start&0xfff8);
/*TODO*///							dest0 = dest_baseaddr + (x_start&0xfff8);
/*TODO*///							pmap0 = priority_bitmap_baseaddr + (x_start&0xfff8);
/*TODO*///							i = y;
/*TODO*///							for(;;)
/*TODO*///							{
/*TODO*///								if( screen ) memcpybitmask( dest0, source0, mask0, count );
/*TODO*///								memsetbitmask8( pmap0, tilemap_priority_code, mask0, count );
/*TODO*///								if( ++i == y_next ) break;
/*TODO*///
/*TODO*///								dest0 += blit.dest_line_offset;
/*TODO*///								source0 += blit.source_line_offset;
/*TODO*///								mask0 += blit.mask_line_offset;
/*TODO*///								pmap0 += priority_bitmap_line_offset;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{ /* TILE_OPAQUE */
/*TODO*///							num_pixels = x_end - x_start;
/*TODO*///							dest0 = dest_baseaddr+x_start;
/*TODO*///							source0 = source_baseaddr+x_start;
/*TODO*///							pmap0 = priority_bitmap_baseaddr + x_start;
/*TODO*///							i = y;
/*TODO*///							for(;;)
/*TODO*///							{
/*TODO*///								if( screen ) memcpy( dest0, source0, num_pixels*sizeof(DATA_TYPE) );
/*TODO*///								memset( pmap0, tilemap_priority_code, num_pixels );
/*TODO*///								if( ++i == y_next ) break;
/*TODO*///
/*TODO*///								dest0 += blit.dest_line_offset;
/*TODO*///								source0 += blit.source_line_offset;
/*TODO*///								pmap0 += priority_bitmap_line_offset;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///					x_start = x_end;
/*TODO*///				}
/*TODO*///
/*TODO*///				prev_tile_type = tile_type;
/*TODO*///			}
/*TODO*///
/*TODO*///			if( y_next==y2 ) break; /* we are done! */
/*TODO*///
/*TODO*///			priority_bitmap_baseaddr = priority_bitmap_next;
/*TODO*///			dest_baseaddr = dest_next;
/*TODO*///			source_baseaddr = source_next;
/*TODO*///			mask_baseaddr = mask_next;
/*TODO*///			y = y_next;
/*TODO*///			y_next += TILE_SIZE;
/*TODO*///
/*TODO*///			if( y_next>=y2 )
/*TODO*///			{
/*TODO*///				y_next = y2;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				dest_next += blit.dest_row_offset;
/*TODO*///				priority_bitmap_next += priority_bitmap_row_offset;
/*TODO*///				source_next += blit.source_row_offset;
/*TODO*///				mask_next += blit.mask_row_offset;
/*TODO*///			}
/*TODO*///		} /* process next row */
/*TODO*///	} /* not totally clipped */
/*TODO*///})
/*TODO*///
/*TODO*///DECLARE( draw_opaque, (int xpos, int ypos),
/*TODO*///{
/*TODO*///	struct osd_bitmap *screen = blit.screen;
/*TODO*///	int tilemap_priority_code = blit.tilemap_priority_code;
/*TODO*///	int x1 = xpos;
/*TODO*///	int y1 = ypos;
/*TODO*///	int x2 = xpos+blit.source_width;
/*TODO*///	int y2 = ypos+blit.source_height;
/*TODO*///	DATA_TYPE *dest_baseaddr = NULL;
/*TODO*///	DATA_TYPE *dest_next;
/*TODO*///	int dy;
/*TODO*/////	int count;
/*TODO*///	const DATA_TYPE *source0;
/*TODO*///	DATA_TYPE *dest0;
/*TODO*///	UINT8 *pmap0;
/*TODO*///	int i;
/*TODO*///	int row;
/*TODO*///	UINT8 *priority_data;
/*TODO*///	int tile_type;
/*TODO*///	int prev_tile_type;
/*TODO*///	int x_start;
/*TODO*///	int x_end;
/*TODO*///	int column;
/*TODO*///	int c1;
/*TODO*///	int c2; /* leftmost and rightmost visible columns in source tilemap */
/*TODO*///	int y; /* current screen line to render */
/*TODO*///	int y_next;
/*TODO*///	int num_pixels;
/*TODO*///	int priority_bitmap_row_offset;
/*TODO*///	UINT8 *priority_bitmap_baseaddr;
/*TODO*///	UINT8 *priority_bitmap_next;
/*TODO*///	int priority;
/*TODO*///	const DATA_TYPE *source_baseaddr;
/*TODO*///	const DATA_TYPE *source_next;
/*TODO*///
/*TODO*/////	const UINT8 *mask0;
/*TODO*/////	UINT8 *mask_data;
/*TODO*/////	const UINT8 *mask_baseaddr;
/*TODO*/////	const UINT8 *mask_next;
/*TODO*///
/*TODO*///	/* clip source coordinates */
/*TODO*///	if( x1<blit.clip_left ) x1 = blit.clip_left;
/*TODO*///	if( x2>blit.clip_right ) x2 = blit.clip_right;
/*TODO*///	if( y1<blit.clip_top ) y1 = blit.clip_top;
/*TODO*///	if( y2>blit.clip_bottom ) y2 = blit.clip_bottom;
/*TODO*///
/*TODO*///	if( x1<x2 && y1<y2 )
/*TODO*///	{ /* do nothing if totally clipped */
/*TODO*///		priority_bitmap_row_offset = priority_bitmap_line_offset*TILE_SIZE;
/*TODO*///		priority_bitmap_baseaddr = xpos + (UINT8 *)priority_bitmap->line[y1];
/*TODO*///		priority = blit.tile_priority;
/*TODO*///		if( screen )
/*TODO*///		{
/*TODO*///			dest_baseaddr = xpos + (DATA_TYPE *)screen->line[y1];
/*TODO*///		}
/*TODO*///
/*TODO*///		/* convert screen coordinates to source tilemap coordinates */
/*TODO*///		x1 -= xpos;
/*TODO*///		y1 -= ypos;
/*TODO*///		x2 -= xpos;
/*TODO*///		y2 -= ypos;
/*TODO*///
/*TODO*///		source_baseaddr = (DATA_TYPE *)blit.pixmap->line[y1];
/*TODO*/////		mask_baseaddr = blit.bitmask->line[y1];
/*TODO*///
/*TODO*///		c1 = x1/TILE_SIZE; /* round down */
/*TODO*///		c2 = (x2+TILE_SIZE-1)/TILE_SIZE; /* round up */
/*TODO*///
/*TODO*///		y = y1;
/*TODO*///		y_next = TILE_SIZE*(y1/TILE_SIZE) + TILE_SIZE;
/*TODO*///		if( y_next>y2 ) y_next = y2;
/*TODO*///
/*TODO*///		dy = y_next-y;
/*TODO*///		dest_next = dest_baseaddr + dy*blit.dest_line_offset;
/*TODO*///		priority_bitmap_next = priority_bitmap_baseaddr + dy*priority_bitmap_line_offset;
/*TODO*///		source_next = source_baseaddr + dy*blit.source_line_offset;
/*TODO*/////		mask_next = mask_baseaddr + dy*blit.mask_line_offset;
/*TODO*///		for(;;)
/*TODO*///		{
/*TODO*///			row = y/TILE_SIZE;
/*TODO*/////			mask_data = blit.mask_data_row[row];
/*TODO*///			priority_data = blit.priority_data_row[row];
/*TODO*///			prev_tile_type = TILE_TRANSPARENT;
/*TODO*///			x_start = x1;
/*TODO*///
/*TODO*///			for( column=c1; column<=c2; column++ )
/*TODO*///			{
/*TODO*///				if( column==c2 || priority_data[column]!=priority )
/*TODO*///				{
/*TODO*///					tile_type = TILE_TRANSPARENT;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*/////					tile_type = mask_data[column];
/*TODO*///					tile_type = TILE_OPAQUE;
/*TODO*///				}
/*TODO*///
/*TODO*///				if( tile_type!=prev_tile_type )
/*TODO*///				{
/*TODO*///					x_end = column*TILE_SIZE;
/*TODO*///					if( x_end<x1 ) x_end = x1;
/*TODO*///					if( x_end>x2 ) x_end = x2;
/*TODO*///
/*TODO*///					if( prev_tile_type != TILE_TRANSPARENT )
/*TODO*///					{
/*TODO*/////						if( prev_tile_type == TILE_MASKED )
/*TODO*/////						{
/*TODO*/////							count = (x_end+7)/8 - x_start/8;
/*TODO*/////							mask0 = mask_baseaddr + x_start/8;
/*TODO*/////							source0 = source_baseaddr + (x_start&0xfff8);
/*TODO*/////							dest0 = dest_baseaddr + (x_start&0xfff8);
/*TODO*/////							pmap0 = priority_bitmap_baseaddr + (x_start&0xfff8);
/*TODO*/////							i = y;
/*TODO*/////							for(;;)
/*TODO*/////							{
/*TODO*/////								if( screen ) memcpybitmask( dest0, source0, mask0, count );
/*TODO*/////								memsetbitmask8( pmap0, tilemap_priority_code, mask0, count );
/*TODO*/////								if( ++i == y_next ) break;
/*TODO*/////
/*TODO*/////								dest0 += blit.dest_line_offset;
/*TODO*/////								source0 += blit.source_line_offset;
/*TODO*/////								mask0 += blit.mask_line_offset;
/*TODO*/////								pmap0 += priority_bitmap_line_offset;
/*TODO*/////							}
/*TODO*/////						}
/*TODO*/////						else
/*TODO*///						{ /* TILE_OPAQUE */
/*TODO*///							num_pixels = x_end - x_start;
/*TODO*///							dest0 = dest_baseaddr+x_start;
/*TODO*///							source0 = source_baseaddr+x_start;
/*TODO*///							pmap0 = priority_bitmap_baseaddr + x_start;
/*TODO*///							i = y;
/*TODO*///							for(;;)
/*TODO*///							{
/*TODO*///								if( screen ) memcpy( dest0, source0, num_pixels*sizeof(DATA_TYPE) );
/*TODO*///								memset( pmap0, tilemap_priority_code, num_pixels );
/*TODO*///								if( ++i == y_next ) break;
/*TODO*///
/*TODO*///								dest0 += blit.dest_line_offset;
/*TODO*///								source0 += blit.source_line_offset;
/*TODO*///								pmap0 += priority_bitmap_line_offset;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///					x_start = x_end;
/*TODO*///				}
/*TODO*///
/*TODO*///				prev_tile_type = tile_type;
/*TODO*///			}
/*TODO*///
/*TODO*///			if( y_next==y2 ) break; /* we are done! */
/*TODO*///
/*TODO*///			priority_bitmap_baseaddr = priority_bitmap_next;
/*TODO*///			dest_baseaddr = dest_next;
/*TODO*///			source_baseaddr = source_next;
/*TODO*/////			mask_baseaddr = mask_next;
/*TODO*///			y = y_next;
/*TODO*///			y_next += TILE_SIZE;
/*TODO*///
/*TODO*///			if( y_next>=y2 )
/*TODO*///			{
/*TODO*///				y_next = y2;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				dest_next += blit.dest_row_offset;
/*TODO*///				priority_bitmap_next += priority_bitmap_row_offset;
/*TODO*///				source_next += blit.source_row_offset;
/*TODO*/////				mask_next += blit.mask_row_offset;
/*TODO*///			}
/*TODO*///		} /* process next row */
/*TODO*///	} /* not totally clipped */
/*TODO*///})
/*TODO*///#undef IGNORE_TRANSPARENCY
/*TODO*///
/*TODO*///#if DEPTH >= 16
/*TODO*///DECLARE( draw_alpha, (int xpos, int ypos),
/*TODO*///{
/*TODO*///	int tilemap_priority_code = blit.tilemap_priority_code;
/*TODO*///	int x1 = xpos;
/*TODO*///	int y1 = ypos;
/*TODO*///	int x2 = xpos+blit.source_width;
/*TODO*///	int y2 = ypos+blit.source_height;
/*TODO*///
/*TODO*///	/* clip source coordinates */
/*TODO*///	if( x1<blit.clip_left ) x1 = blit.clip_left;
/*TODO*///	if( x2>blit.clip_right ) x2 = blit.clip_right;
/*TODO*///	if( y1<blit.clip_top ) y1 = blit.clip_top;
/*TODO*///	if( y2>blit.clip_bottom ) y2 = blit.clip_bottom;
/*TODO*///
/*TODO*///	if( x1<x2 && y1<y2 )
/*TODO*///	{ /* do nothing if totally clipped */
/*TODO*///		DATA_TYPE *dest_baseaddr = xpos + (DATA_TYPE *)blit.screen->line[y1];
/*TODO*///		DATA_TYPE *dest_next;
/*TODO*///
/*TODO*///		int priority_bitmap_row_offset = priority_bitmap_line_offset*TILE_SIZE;
/*TODO*///		UINT8 *priority_bitmap_baseaddr = xpos + (UINT8 *)priority_bitmap->line[y1];
/*TODO*///		UINT8 *priority_bitmap_next;
/*TODO*///
/*TODO*///		int priority = blit.tile_priority;
/*TODO*///		const DATA_TYPE *source_baseaddr;
/*TODO*///		const DATA_TYPE *source_next;
/*TODO*///		const UINT8 *mask_baseaddr;
/*TODO*///		const UINT8 *mask_next;
/*TODO*///
/*TODO*///		int c1;
/*TODO*///		int c2; /* leftmost and rightmost visible columns in source tilemap */
/*TODO*///		int y; /* current screen line to render */
/*TODO*///		int y_next;
/*TODO*///
/*TODO*///		/* convert screen coordinates to source tilemap coordinates */
/*TODO*///		x1 -= xpos;
/*TODO*///		y1 -= ypos;
/*TODO*///		x2 -= xpos;
/*TODO*///		y2 -= ypos;
/*TODO*///
/*TODO*///		source_baseaddr = (DATA_TYPE *)blit.pixmap->line[y1];
/*TODO*///		mask_baseaddr = blit.bitmask->line[y1];
/*TODO*///
/*TODO*///		c1 = x1/TILE_SIZE; /* round down */
/*TODO*///		c2 = (x2+TILE_SIZE-1)/TILE_SIZE; /* round up */
/*TODO*///
/*TODO*///		y = y1;
/*TODO*///		y_next = TILE_SIZE*(y1/TILE_SIZE) + TILE_SIZE;
/*TODO*///		if( y_next>y2 ) y_next = y2;
/*TODO*///
/*TODO*///		{
/*TODO*///			int dy = y_next-y;
/*TODO*///			dest_next = dest_baseaddr + dy*blit.dest_line_offset;
/*TODO*///			priority_bitmap_next = priority_bitmap_baseaddr + dy*priority_bitmap_line_offset;
/*TODO*///			source_next = source_baseaddr + dy*blit.source_line_offset;
/*TODO*///			mask_next = mask_baseaddr + dy*blit.mask_line_offset;
/*TODO*///		}
/*TODO*///
/*TODO*///		for(;;)
/*TODO*///		{
/*TODO*///			int row = y/TILE_SIZE;
/*TODO*///			UINT8 *mask_data = blit.mask_data_row[row];
/*TODO*///			UINT8 *priority_data = blit.priority_data_row[row];
/*TODO*///
/*TODO*///			int tile_type;
/*TODO*///			int prev_tile_type = TILE_TRANSPARENT;
/*TODO*///
/*TODO*///			int x_start = x1;
/*TODO*///			int x_end;
/*TODO*///
/*TODO*///			int column;
/*TODO*///			for( column=c1; column<=c2; column++ )
/*TODO*///			{
/*TODO*///				if( column==c2 || priority_data[column]!=priority )
/*TODO*///					tile_type = TILE_TRANSPARENT;
/*TODO*///				else
/*TODO*///					tile_type = mask_data[column];
/*TODO*///
/*TODO*///				if( tile_type!=prev_tile_type )
/*TODO*///				{
/*TODO*///					x_end = column*TILE_SIZE;
/*TODO*///					if( x_end<x1 ) x_end = x1;
/*TODO*///					if( x_end>x2 ) x_end = x2;
/*TODO*///
/*TODO*///					if( prev_tile_type != TILE_TRANSPARENT )
/*TODO*///					{
/*TODO*///						if( prev_tile_type == TILE_MASKED )
/*TODO*///						{
/*TODO*///							int count = (x_end+7)/8 - x_start/8;
/*TODO*///							const UINT8 *mask0 = mask_baseaddr + x_start/8;
/*TODO*///							const DATA_TYPE *source0 = source_baseaddr + (x_start&0xfff8);
/*TODO*///							DATA_TYPE *dest0 = dest_baseaddr + (x_start&0xfff8);
/*TODO*///							UINT8 *pmap0 = priority_bitmap_baseaddr + (x_start&0xfff8);
/*TODO*///							int i = y;
/*TODO*///							for(;;)
/*TODO*///							{
/*TODO*///								blendbitmask( dest0, source0, mask0, count );
/*TODO*///								memsetbitmask8( pmap0, tilemap_priority_code, mask0, count );
/*TODO*///								if( ++i == y_next ) break;
/*TODO*///
/*TODO*///								dest0 += blit.dest_line_offset;
/*TODO*///								source0 += blit.source_line_offset;
/*TODO*///								mask0 += blit.mask_line_offset;
/*TODO*///								pmap0 += priority_bitmap_line_offset;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{ /* TILE_OPAQUE */
/*TODO*///							int num_pixels = x_end - x_start;
/*TODO*///							DATA_TYPE *dest0 = dest_baseaddr+x_start;
/*TODO*///							const DATA_TYPE *source0 = source_baseaddr+x_start;
/*TODO*///							UINT8 *pmap0 = priority_bitmap_baseaddr + x_start;
/*TODO*///							int i = y;
/*TODO*///							for(;;)
/*TODO*///							{
/*TODO*///								blend( dest0, source0, num_pixels );
/*TODO*///								memset( pmap0, tilemap_priority_code, num_pixels );
/*TODO*///								if( ++i == y_next ) break;
/*TODO*///
/*TODO*///								dest0 += blit.dest_line_offset;
/*TODO*///								source0 += blit.source_line_offset;
/*TODO*///								pmap0 += priority_bitmap_line_offset;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///					x_start = x_end;
/*TODO*///				}
/*TODO*///
/*TODO*///				prev_tile_type = tile_type;
/*TODO*///			}
/*TODO*///
/*TODO*///			if( y_next==y2 ) break; /* we are done! */
/*TODO*///
/*TODO*///			priority_bitmap_baseaddr = priority_bitmap_next;
/*TODO*///			dest_baseaddr = dest_next;
/*TODO*///			source_baseaddr = source_next;
/*TODO*///			mask_baseaddr = mask_next;
/*TODO*///
/*TODO*///			y = y_next;
/*TODO*///			y_next += TILE_SIZE;
/*TODO*///
/*TODO*///			if( y_next>=y2 )
/*TODO*///			{
/*TODO*///				y_next = y2;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				dest_next += blit.dest_row_offset;
/*TODO*///				priority_bitmap_next += priority_bitmap_row_offset;
/*TODO*///				source_next += blit.source_row_offset;
/*TODO*///				mask_next += blit.mask_row_offset;
/*TODO*///			}
/*TODO*///		} /* process next row */
/*TODO*///	} /* not totally clipped */
/*TODO*///})
/*TODO*///#endif
/*TODO*///
/*TODO*///#if (TILE_SIZE == 8) /* only construct once for each depth */
/*TODO*///DECLARE( draw_tile, (struct tilemap *tilemap, UINT32 cached_indx, UINT32 col, UINT32 row ),
/*TODO*///{
/*TODO*///	struct cached_tile_info *cached_tile_info = &tilemap->cached_tile_info[cached_indx];
/*TODO*///	UINT32 tile_size = tilemap->tile_size;
/*TODO*///	const UINT8 *pPenData = cached_tile_info->pen_data;
/*TODO*///	int pitch = tile_size + cached_tile_info->skip;
/*TODO*///	const UINT32 *pPalData = cached_tile_info->pal_data;
/*TODO*///	UINT32 flags = cached_tile_info->flags;
/*TODO*///	UINT32 y0 = tile_size*row;
/*TODO*///	UINT32 x0 = tile_size*col;
/*TODO*///	struct osd_bitmap *pPixmap = tilemap->pixmap;
/*TODO*///	UINT32 *pPenToPixel = tilemap->pPenToPixel[flags&(TILE_SWAPXY|TILE_FLIPY|TILE_FLIPX)];
/*TODO*///	int tx;
/*TODO*///	int ty;
/*TODO*///	const UINT8 *pSource;
/*TODO*///	UINT8 data;
/*TODO*///	UINT32 yx;
/*TODO*///
/*TODO*///	if( flags&TILE_4BPP )
/*TODO*///	{
/*TODO*///		for( ty=tile_size; ty!=0; ty-- )
/*TODO*///		{
/*TODO*///			pSource = pPenData;
/*TODO*///			for( tx=tile_size/2; tx!=0; tx-- )
/*TODO*///			{
/*TODO*///				data = *pSource++;
/*TODO*///				yx = *pPenToPixel++;
/*TODO*///				*(x0+(yx%MAX_TILESIZE)+(DATA_TYPE *)pPixmap->line[y0+yx/MAX_TILESIZE]) = pPalData[data&0xf];
/*TODO*///				yx = *pPenToPixel++;
/*TODO*///				*(x0+(yx%MAX_TILESIZE)+(DATA_TYPE *)pPixmap->line[y0+yx/MAX_TILESIZE]) = pPalData[data>>4];
/*TODO*///			}
/*TODO*///			pPenData += pitch/2;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		for( ty=tile_size; ty!=0; ty-- )
/*TODO*///		{
/*TODO*///			pSource = pPenData;
/*TODO*///			for( tx=tile_size; tx!=0; tx-- )
/*TODO*///			{
/*TODO*///				data = *pSource++;
/*TODO*///				yx = *pPenToPixel++;
/*TODO*///				*(x0+(yx%MAX_TILESIZE)+(DATA_TYPE *)pPixmap->line[y0+yx/MAX_TILESIZE]) = pPalData[data];
/*TODO*///			}
/*TODO*///			pPenData += pitch;
/*TODO*///		}
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///#endif /* draw_tile */
/*TODO*///
/*TODO*///#undef TILE_SIZE
/*TODO*///#undef DATA_TYPE
/*TODO*///#undef memcpybitmask
/*TODO*///#undef DECLARE
/*TODO*///
/*TODO*///#endif /* DECLARE */
/*TODO*///    
    
    public static DrawTileHandlerPtr draw_tile8x8x8BPP = new DrawTileHandlerPtr() {
        @Override
        public void handler(struct_tilemap tilemap, int cached_indx, int col, int row) {
            
            cached_tile_info cached_tile_info = tilemap.cached_tile_info[cached_indx];
            int /*UINT32*/ tile_size = tilemap.tile_size;
            UBytePtr pPenData = new UBytePtr(cached_tile_info.pen_data);
            int pitch = tile_size + cached_tile_info.skip;
            IntArray pPalData = new IntArray(cached_tile_info.pal_data);
            pPalData.offset=0;
            int /*UINT32*/ flags = cached_tile_info.u32_flags;
            int /*UINT32*/ y0 = tile_size*row;
            int /*UINT32*/ x0 = tile_size*col;
            osd_bitmap pPixmap = tilemap.pixmap;
            IntArray pPenToPixel = new IntArray(tilemap.pPenToPixel[flags&(TILE_SWAPXY|TILE_FLIPY|TILE_FLIPX)]);
            pPenToPixel.offset=0;
            int tx;
            int ty;
            UBytePtr pSource;
            int /*UINT8*/ data;
            int /*UINT32*/ yx;

            if(( flags&TILE_4BPP ) != 0)
            {
                    for( ty=tile_size; ty!=0; ty-- )
                    {
                            pSource = new UBytePtr(pPenData);
                            pSource.offset = 0;
                            
                            for( tx=tile_size/2; tx!=0; tx-- )
                            {
                                    data = pSource.readinc();
                                    yx = pPenToPixel.read(); pPenToPixel.offset++;
                                    //*(x0+(yx%MAX_TILESIZE)+(DATA_TYPE *)pPixmap.line[y0+yx/MAX_TILESIZE]) = pPalData[data&0xf];
                                    (new UBytePtr(pPixmap.line[y0+yx/MAX_TILESIZE])).write((x0+(yx%MAX_TILESIZE)), pPalData.read(data&0xf));
                                    yx = pPenToPixel.read(); pPenToPixel.offset++;
                                    //*(x0+(yx%MAX_TILESIZE)+(DATA_TYPE *)pPixmap.line[y0+yx/MAX_TILESIZE]) = pPalData[data>>4];
                                    (new UBytePtr(pPixmap.line[y0+yx/MAX_TILESIZE])).write((x0+(yx%MAX_TILESIZE)), pPalData.read(data>>4));
                            }
                            pPenData.offset += pitch/2;
                    }
            }
            else
            {
                    for( ty=tile_size; ty!=0; ty-- )
                    {
                            pSource = new UBytePtr(pPenData);
                            pSource.offset=0;
                            
                            for( tx=tile_size; tx!=0; tx-- )
                            {
                                    data = pSource.readinc();
                                    yx = pPenToPixel.read(); pPenToPixel.offset++;
                                    //*(x0+(yx%MAX_TILESIZE)+(DATA_TYPE *)pPixmap.line[y0+yx/MAX_TILESIZE]) = pPalData[data];
                                    (new UBytePtr(pPixmap.line[y0+yx/MAX_TILESIZE])).write((x0+(yx%MAX_TILESIZE)), pPalData.read(data));
                            }
                            pPenData.offset += pitch;
                    }
            }

        }
    };
    
    public static void generic8draw(int xpos, int ypos, int TILE_WIDTH, int TILE_HEIGHT) {
        int tilemap_priority_code = blit.tilemap_priority_code;
        int x1 = xpos;
        int y1 = ypos;
        int x2 = xpos + blit.source_width;
        int y2 = ypos + blit.source_height;

        /* clip source coordinates */
        if (x1 < blit.clip_left) {
            x1 = blit.clip_left;
        }
        if (x2 > blit.clip_right) {
            x2 = blit.clip_right;
        }
        if (y1 < blit.clip_top) {
            y1 = blit.clip_top;
        }
        if (y2 > blit.clip_bottom) {
            y2 = blit.clip_bottom;
        }

        if (x1 < x2 && y1 < y2) {
            /* do nothing if totally clipped */
            UBytePtr dest_baseaddr = new UBytePtr(blit.screen.line[y1], xpos);
            UBytePtr dest_next;

            int priority_bitmap_row_offset = priority_bitmap_line_offset * TILE_HEIGHT;
            UBytePtr priority_bitmap_baseaddr = new UBytePtr(priority_bitmap.line[y1], xpos);
            UBytePtr priority_bitmap_next;

            int priority = blit.tile_priority;
            UBytePtr source_baseaddr;
            UBytePtr source_next;
            UBytePtr mask_baseaddr;
            UBytePtr mask_next;

            int c1;
            int c2;
            /* leftmost and rightmost visible columns in source tilemap */
            int y;
            /* current screen line to render */
            int y_next;

            /* convert screen coordinates to source tilemap coordinates */
            x1 -= xpos;
            y1 -= ypos;
            x2 -= xpos;
            y2 -= ypos;

            source_baseaddr = new UBytePtr(blit.pixmap.line[y1]);
            mask_baseaddr = new UBytePtr(blit.bitmask.line[y1]);

            c1 = x1 / TILE_WIDTH;
            /* round down */
            c2 = (x2 + TILE_WIDTH - 1) / TILE_WIDTH;
            /* round up */

            y = y1;
            y_next = TILE_HEIGHT * (y1 / TILE_HEIGHT) + TILE_HEIGHT;
            if (y_next > y2) {
                y_next = y2;
            }

            {
                int dy = y_next - y;
                dest_next = new UBytePtr(dest_baseaddr, dy * blit.dest_line_offset);
                priority_bitmap_next = new UBytePtr(priority_bitmap_baseaddr, dy * priority_bitmap_line_offset);
                source_next = new UBytePtr(source_baseaddr, dy * blit.source_line_offset);
                mask_next = new UBytePtr(mask_baseaddr, dy * blit.mask_line_offset);
            }

            for (;;) {
                int row = y / TILE_HEIGHT;
                UBytePtr mask_data = new UBytePtr(blit.mask_data_row[row]);
                UBytePtr priority_data = new UBytePtr(blit.priority_data_row[row]);

                int tile_type;
                int prev_tile_type = TILE_TRANSPARENT;

                int x_start = x1;
                int x_end;

                int column;
                for (column = c1; column <= c2; column++) {
                    if (column == c2 || priority_data.read(column) != priority) {
                        tile_type = TILE_TRANSPARENT;
                    } else {
                        tile_type = mask_data.read(column);
                    }

                    if (tile_type != prev_tile_type) {
                        x_end = column * TILE_WIDTH;
                        if (x_end < x1) {
                            x_end = x1;
                        }
                        if (x_end > x2) {
                            x_end = x2;
                        }

                        if (prev_tile_type != TILE_TRANSPARENT) {
                            if (prev_tile_type == TILE_MASKED) {
                                int count = (x_end + 7) / 8 - x_start / 8;
                                UBytePtr mask0 = new UBytePtr(mask_baseaddr, x_start / 8);
                                UBytePtr source0 = new UBytePtr(source_baseaddr, (x_start & 0xfff8));
                                UBytePtr dest0 = new UBytePtr(dest_baseaddr, (x_start & 0xfff8));
                                UBytePtr pmap0 = new UBytePtr(priority_bitmap_baseaddr, (x_start & 0xfff8));
                                int i = y;
                                for (;;) {
                                    memcpybitmask8(new UBytePtr(dest0), new UBytePtr(source0), new UBytePtr(mask0), count);
                                    memsetbitmask8(new UBytePtr(pmap0), tilemap_priority_code, new UBytePtr(mask0), count);
                                    if (++i == y_next) {
                                        break;
                                    }

                                    dest0.offset += blit.dest_line_offset;
                                    source0.offset += blit.source_line_offset;
                                    mask0.offset += blit.mask_line_offset;
                                    pmap0.offset += priority_bitmap_line_offset;
                                }
                            } else {
                                /* TILE_OPAQUE */
                                int num_pixels = x_end - x_start;
                                UBytePtr dest0 = new UBytePtr(dest_baseaddr, x_start);
                                UBytePtr source0 = new UBytePtr(source_baseaddr, x_start);
                                UBytePtr pmap0 = new UBytePtr(priority_bitmap_baseaddr, x_start);
                                int i = y;
                                for (;;) {
                                    memcpy(dest0, source0, num_pixels);
                                    memset(pmap0, tilemap_priority_code, num_pixels);
                                    if (++i == y_next) {
                                        break;
                                    }

                                    dest0.offset += blit.dest_line_offset;
                                    source0.offset += blit.source_line_offset;
                                    pmap0.offset += priority_bitmap_line_offset;
                                }
                            }
                        }
                        x_start = x_end;
                    }

                    prev_tile_type = tile_type;
                }

                if (y_next == y2) {
                    break;
                    /* we are done! */
                }

                priority_bitmap_baseaddr = new UBytePtr(priority_bitmap_next);
                dest_baseaddr = new UBytePtr(dest_next);
                source_baseaddr = new UBytePtr(source_next);
                mask_baseaddr = new UBytePtr(mask_next);

                y = y_next;
                y_next += TILE_HEIGHT;

                if (y_next >= y2) {
                    y_next = y2;
                } else {
                    dest_next.offset += blit.dest_row_offset;
                    priority_bitmap_next.offset += priority_bitmap_row_offset;
                    source_next.offset += blit.source_row_offset;
                    mask_next.offset += blit.mask_row_offset;
                }
            }
            /* process next row */
        }
        /* not totally clipped */
    }
    
    
    public static DrawHandlerPtr draw8x8x8BPP = new DrawHandlerPtr() {
        @Override
        public void handler(int xpos, int ypos) {      
             generic8draw(xpos, ypos, 8, 8);
        }
    };
    
    public static DrawHandlerPtr draw_opaque8x8x8BPP = new DrawHandlerPtr() {
        @Override
        public void handler(int xpos, int ypos) {
            throw new UnsupportedOperationException("Not supported yet.");
            /*TODO*///DECLARE( draw_opaque, (int xpos, int ypos),
/*TODO*///{
/*TODO*///	struct osd_bitmap *screen = blit.screen;
/*TODO*///	int tilemap_priority_code = blit.tilemap_priority_code;
/*TODO*///	int x1 = xpos;
/*TODO*///	int y1 = ypos;
/*TODO*///	int x2 = xpos+blit.source_width;
/*TODO*///	int y2 = ypos+blit.source_height;
/*TODO*///	DATA_TYPE *dest_baseaddr = NULL;
/*TODO*///	DATA_TYPE *dest_next;
/*TODO*///	int dy;
/*TODO*/////	int count;
/*TODO*///	const DATA_TYPE *source0;
/*TODO*///	DATA_TYPE *dest0;
/*TODO*///	UINT8 *pmap0;
/*TODO*///	int i;
/*TODO*///	int row;
/*TODO*///	UINT8 *priority_data;
/*TODO*///	int tile_type;
/*TODO*///	int prev_tile_type;
/*TODO*///	int x_start;
/*TODO*///	int x_end;
/*TODO*///	int column;
/*TODO*///	int c1;
/*TODO*///	int c2; /* leftmost and rightmost visible columns in source tilemap */
/*TODO*///	int y; /* current screen line to render */
/*TODO*///	int y_next;
/*TODO*///	int num_pixels;
/*TODO*///	int priority_bitmap_row_offset;
/*TODO*///	UINT8 *priority_bitmap_baseaddr;
/*TODO*///	UINT8 *priority_bitmap_next;
/*TODO*///	int priority;
/*TODO*///	const DATA_TYPE *source_baseaddr;
/*TODO*///	const DATA_TYPE *source_next;
/*TODO*///
/*TODO*/////	const UINT8 *mask0;
/*TODO*/////	UINT8 *mask_data;
/*TODO*/////	const UINT8 *mask_baseaddr;
/*TODO*/////	const UINT8 *mask_next;
/*TODO*///
/*TODO*///	/* clip source coordinates */
/*TODO*///	if( x1<blit.clip_left ) x1 = blit.clip_left;
/*TODO*///	if( x2>blit.clip_right ) x2 = blit.clip_right;
/*TODO*///	if( y1<blit.clip_top ) y1 = blit.clip_top;
/*TODO*///	if( y2>blit.clip_bottom ) y2 = blit.clip_bottom;
/*TODO*///
/*TODO*///	if( x1<x2 && y1<y2 )
/*TODO*///	{ /* do nothing if totally clipped */
/*TODO*///		priority_bitmap_row_offset = priority_bitmap_line_offset*TILE_SIZE;
/*TODO*///		priority_bitmap_baseaddr = xpos + (UINT8 *)priority_bitmap->line[y1];
/*TODO*///		priority = blit.tile_priority;
/*TODO*///		if( screen )
/*TODO*///		{
/*TODO*///			dest_baseaddr = xpos + (DATA_TYPE *)screen->line[y1];
/*TODO*///		}
/*TODO*///
/*TODO*///		/* convert screen coordinates to source tilemap coordinates */
/*TODO*///		x1 -= xpos;
/*TODO*///		y1 -= ypos;
/*TODO*///		x2 -= xpos;
/*TODO*///		y2 -= ypos;
/*TODO*///
/*TODO*///		source_baseaddr = (DATA_TYPE *)blit.pixmap->line[y1];
/*TODO*/////		mask_baseaddr = blit.bitmask->line[y1];
/*TODO*///
/*TODO*///		c1 = x1/TILE_SIZE; /* round down */
/*TODO*///		c2 = (x2+TILE_SIZE-1)/TILE_SIZE; /* round up */
/*TODO*///
/*TODO*///		y = y1;
/*TODO*///		y_next = TILE_SIZE*(y1/TILE_SIZE) + TILE_SIZE;
/*TODO*///		if( y_next>y2 ) y_next = y2;
/*TODO*///
/*TODO*///		dy = y_next-y;
/*TODO*///		dest_next = dest_baseaddr + dy*blit.dest_line_offset;
/*TODO*///		priority_bitmap_next = priority_bitmap_baseaddr + dy*priority_bitmap_line_offset;
/*TODO*///		source_next = source_baseaddr + dy*blit.source_line_offset;
/*TODO*/////		mask_next = mask_baseaddr + dy*blit.mask_line_offset;
/*TODO*///		for(;;)
/*TODO*///		{
/*TODO*///			row = y/TILE_SIZE;
/*TODO*/////			mask_data = blit.mask_data_row[row];
/*TODO*///			priority_data = blit.priority_data_row[row];
/*TODO*///			prev_tile_type = TILE_TRANSPARENT;
/*TODO*///			x_start = x1;
/*TODO*///
/*TODO*///			for( column=c1; column<=c2; column++ )
/*TODO*///			{
/*TODO*///				if( column==c2 || priority_data[column]!=priority )
/*TODO*///				{
/*TODO*///					tile_type = TILE_TRANSPARENT;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*/////					tile_type = mask_data[column];
/*TODO*///					tile_type = TILE_OPAQUE;
/*TODO*///				}
/*TODO*///
/*TODO*///				if( tile_type!=prev_tile_type )
/*TODO*///				{
/*TODO*///					x_end = column*TILE_SIZE;
/*TODO*///					if( x_end<x1 ) x_end = x1;
/*TODO*///					if( x_end>x2 ) x_end = x2;
/*TODO*///
/*TODO*///					if( prev_tile_type != TILE_TRANSPARENT )
/*TODO*///					{
/*TODO*/////						if( prev_tile_type == TILE_MASKED )
/*TODO*/////						{
/*TODO*/////							count = (x_end+7)/8 - x_start/8;
/*TODO*/////							mask0 = mask_baseaddr + x_start/8;
/*TODO*/////							source0 = source_baseaddr + (x_start&0xfff8);
/*TODO*/////							dest0 = dest_baseaddr + (x_start&0xfff8);
/*TODO*/////							pmap0 = priority_bitmap_baseaddr + (x_start&0xfff8);
/*TODO*/////							i = y;
/*TODO*/////							for(;;)
/*TODO*/////							{
/*TODO*/////								if( screen ) memcpybitmask( dest0, source0, mask0, count );
/*TODO*/////								memsetbitmask8( pmap0, tilemap_priority_code, mask0, count );
/*TODO*/////								if( ++i == y_next ) break;
/*TODO*/////
/*TODO*/////								dest0 += blit.dest_line_offset;
/*TODO*/////								source0 += blit.source_line_offset;
/*TODO*/////								mask0 += blit.mask_line_offset;
/*TODO*/////								pmap0 += priority_bitmap_line_offset;
/*TODO*/////							}
/*TODO*/////						}
/*TODO*/////						else
/*TODO*///						{ /* TILE_OPAQUE */
/*TODO*///							num_pixels = x_end - x_start;
/*TODO*///							dest0 = dest_baseaddr+x_start;
/*TODO*///							source0 = source_baseaddr+x_start;
/*TODO*///							pmap0 = priority_bitmap_baseaddr + x_start;
/*TODO*///							i = y;
/*TODO*///							for(;;)
/*TODO*///							{
/*TODO*///								if( screen ) memcpy( dest0, source0, num_pixels*sizeof(DATA_TYPE) );
/*TODO*///								memset( pmap0, tilemap_priority_code, num_pixels );
/*TODO*///								if( ++i == y_next ) break;
/*TODO*///
/*TODO*///								dest0 += blit.dest_line_offset;
/*TODO*///								source0 += blit.source_line_offset;
/*TODO*///								pmap0 += priority_bitmap_line_offset;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///					x_start = x_end;
/*TODO*///				}
/*TODO*///
/*TODO*///				prev_tile_type = tile_type;
/*TODO*///			}
/*TODO*///
/*TODO*///			if( y_next==y2 ) break; /* we are done! */
/*TODO*///
/*TODO*///			priority_bitmap_baseaddr = priority_bitmap_next;
/*TODO*///			dest_baseaddr = dest_next;
/*TODO*///			source_baseaddr = source_next;
/*TODO*/////			mask_baseaddr = mask_next;
/*TODO*///			y = y_next;
/*TODO*///			y_next += TILE_SIZE;
/*TODO*///
/*TODO*///			if( y_next>=y2 )
/*TODO*///			{
/*TODO*///				y_next = y2;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				dest_next += blit.dest_row_offset;
/*TODO*///				priority_bitmap_next += priority_bitmap_row_offset;
/*TODO*///				source_next += blit.source_row_offset;
/*TODO*/////				mask_next += blit.mask_row_offset;
/*TODO*///			}
/*TODO*///		} /* process next row */
/*TODO*///	} /* not totally clipped */
/*TODO*///})
        }
    };
    
    public static DrawHandlerPtr draw16x16x8BPP = new DrawHandlerPtr() {
        @Override
        public void handler(int xpos, int ypos) {      
            throw new UnsupportedOperationException("Not supported yet.");
/*TODO*///	struct osd_bitmap *screen = blit.screen;
/*TODO*///	int tilemap_priority_code = blit.tilemap_priority_code;
/*TODO*///	int x1 = xpos;
/*TODO*///	int y1 = ypos;
/*TODO*///	int x2 = xpos+blit.source_width;
/*TODO*///	int y2 = ypos+blit.source_height;
/*TODO*///	DATA_TYPE *dest_baseaddr = NULL;
/*TODO*///	DATA_TYPE *dest_next;
/*TODO*///	int dy;
/*TODO*///	int count;
/*TODO*///	const DATA_TYPE *source0;
/*TODO*///	DATA_TYPE *dest0;
/*TODO*///	UINT8 *pmap0;
/*TODO*///	int i;
/*TODO*///	int row;
/*TODO*///	UINT8 *priority_data;
/*TODO*///	int tile_type;
/*TODO*///	int prev_tile_type;
/*TODO*///	int x_start;
/*TODO*///	int x_end;
/*TODO*///	int column;
/*TODO*///	int c1;
/*TODO*///	int c2; /* leftmost and rightmost visible columns in source tilemap */
/*TODO*///	int y; /* current screen line to render */
/*TODO*///	int y_next;
/*TODO*///	int num_pixels;
/*TODO*///	int priority_bitmap_row_offset;
/*TODO*///	UINT8 *priority_bitmap_baseaddr;
/*TODO*///	UINT8 *priority_bitmap_next;
/*TODO*///	int priority;
/*TODO*///	const DATA_TYPE *source_baseaddr;
/*TODO*///	const DATA_TYPE *source_next;
/*TODO*///	const UINT8 *mask0;
/*TODO*///	UINT8 *mask_data;
/*TODO*///	const UINT8 *mask_baseaddr;
/*TODO*///	const UINT8 *mask_next;
/*TODO*///
/*TODO*///	/* clip source coordinates */
/*TODO*///	if( x1<blit.clip_left ) x1 = blit.clip_left;
/*TODO*///	if( x2>blit.clip_right ) x2 = blit.clip_right;
/*TODO*///	if( y1<blit.clip_top ) y1 = blit.clip_top;
/*TODO*///	if( y2>blit.clip_bottom ) y2 = blit.clip_bottom;
/*TODO*///
/*TODO*///	if( x1<x2 && y1<y2 )
/*TODO*///	{ /* do nothing if totally clipped */
/*TODO*///		priority_bitmap_row_offset = priority_bitmap_line_offset*TILE_SIZE;
/*TODO*///		priority_bitmap_baseaddr = xpos + (UINT8 *)priority_bitmap->line[y1];
/*TODO*///		priority = blit.tile_priority;
/*TODO*///		if( screen )
/*TODO*///		{
/*TODO*///			dest_baseaddr = xpos + (DATA_TYPE *)screen->line[y1];
/*TODO*///		}
/*TODO*///
/*TODO*///		/* convert screen coordinates to source tilemap coordinates */
/*TODO*///		x1 -= xpos;
/*TODO*///		y1 -= ypos;
/*TODO*///		x2 -= xpos;
/*TODO*///		y2 -= ypos;
/*TODO*///
/*TODO*///		source_baseaddr = (DATA_TYPE *)blit.pixmap->line[y1];
/*TODO*///		mask_baseaddr = blit.bitmask->line[y1];
/*TODO*///
/*TODO*///		c1 = x1/TILE_SIZE; /* round down */
/*TODO*///		c2 = (x2+TILE_SIZE-1)/TILE_SIZE; /* round up */
/*TODO*///
/*TODO*///		y = y1;
/*TODO*///		y_next = TILE_SIZE*(y1/TILE_SIZE) + TILE_SIZE;
/*TODO*///		if( y_next>y2 ) y_next = y2;
/*TODO*///
/*TODO*///		dy = y_next-y;
/*TODO*///		dest_next = dest_baseaddr + dy*blit.dest_line_offset;
/*TODO*///		priority_bitmap_next = priority_bitmap_baseaddr + dy*priority_bitmap_line_offset;
/*TODO*///		source_next = source_baseaddr + dy*blit.source_line_offset;
/*TODO*///		mask_next = mask_baseaddr + dy*blit.mask_line_offset;
/*TODO*///		for(;;)
/*TODO*///		{
/*TODO*///			row = y/TILE_SIZE;
/*TODO*///			mask_data = blit.mask_data_row[row];
/*TODO*///			priority_data = blit.priority_data_row[row];
/*TODO*///			prev_tile_type = TILE_TRANSPARENT;
/*TODO*///			x_start = x1;
/*TODO*///
/*TODO*///			for( column=c1; column<=c2; column++ )
/*TODO*///			{
/*TODO*///				if( column==c2 || priority_data[column]!=priority )
/*TODO*///				{
/*TODO*///					tile_type = TILE_TRANSPARENT;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					tile_type = mask_data[column];
/*TODO*///				}
/*TODO*///
/*TODO*///				if( tile_type!=prev_tile_type )
/*TODO*///				{
/*TODO*///					x_end = column*TILE_SIZE;
/*TODO*///					if( x_end<x1 ) x_end = x1;
/*TODO*///					if( x_end>x2 ) x_end = x2;
/*TODO*///
/*TODO*///					if( prev_tile_type != TILE_TRANSPARENT )
/*TODO*///					{
/*TODO*///						if( prev_tile_type == TILE_MASKED )
/*TODO*///						{
/*TODO*///							count = (x_end+7)/8 - x_start/8;
/*TODO*///							mask0 = mask_baseaddr + x_start/8;
/*TODO*///							source0 = source_baseaddr + (x_start&0xfff8);
/*TODO*///							dest0 = dest_baseaddr + (x_start&0xfff8);
/*TODO*///							pmap0 = priority_bitmap_baseaddr + (x_start&0xfff8);
/*TODO*///							i = y;
/*TODO*///							for(;;)
/*TODO*///							{
/*TODO*///								if( screen ) memcpybitmask( dest0, source0, mask0, count );
/*TODO*///								memsetbitmask8( pmap0, tilemap_priority_code, mask0, count );
/*TODO*///								if( ++i == y_next ) break;
/*TODO*///
/*TODO*///								dest0 += blit.dest_line_offset;
/*TODO*///								source0 += blit.source_line_offset;
/*TODO*///								mask0 += blit.mask_line_offset;
/*TODO*///								pmap0 += priority_bitmap_line_offset;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{ /* TILE_OPAQUE */
/*TODO*///							num_pixels = x_end - x_start;
/*TODO*///							dest0 = dest_baseaddr+x_start;
/*TODO*///							source0 = source_baseaddr+x_start;
/*TODO*///							pmap0 = priority_bitmap_baseaddr + x_start;
/*TODO*///							i = y;
/*TODO*///							for(;;)
/*TODO*///							{
/*TODO*///								if( screen ) memcpy( dest0, source0, num_pixels*sizeof(DATA_TYPE) );
/*TODO*///								memset( pmap0, tilemap_priority_code, num_pixels );
/*TODO*///								if( ++i == y_next ) break;
/*TODO*///
/*TODO*///								dest0 += blit.dest_line_offset;
/*TODO*///								source0 += blit.source_line_offset;
/*TODO*///								pmap0 += priority_bitmap_line_offset;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///					x_start = x_end;
/*TODO*///				}
/*TODO*///
/*TODO*///				prev_tile_type = tile_type;
/*TODO*///			}
/*TODO*///
/*TODO*///			if( y_next==y2 ) break; /* we are done! */
/*TODO*///
/*TODO*///			priority_bitmap_baseaddr = priority_bitmap_next;
/*TODO*///			dest_baseaddr = dest_next;
/*TODO*///			source_baseaddr = source_next;
/*TODO*///			mask_baseaddr = mask_next;
/*TODO*///			y = y_next;
/*TODO*///			y_next += TILE_SIZE;
/*TODO*///
/*TODO*///			if( y_next>=y2 )
/*TODO*///			{
/*TODO*///				y_next = y2;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				dest_next += blit.dest_row_offset;
/*TODO*///				priority_bitmap_next += priority_bitmap_row_offset;
/*TODO*///				source_next += blit.source_row_offset;
/*TODO*///				mask_next += blit.mask_row_offset;
/*TODO*///			}
/*TODO*///		} /* process next row */
/*TODO*///	} /* not totally clipped */
/*TODO*///})
        }
    };
    
    public static void generic8draw_opaque(int xpos, int ypos, int TILE_WIDTH, int TILE_HEIGHT) {
        int tilemap_priority_code = blit.tilemap_priority_code;
        int x1 = xpos;
        int y1 = ypos;
        int x2 = xpos + blit.source_width;
        int y2 = ypos + blit.source_height;
        /* clip source coordinates */
        if (x1 < blit.clip_left) {
            x1 = blit.clip_left;
        }
        if (x2 > blit.clip_right) {
            x2 = blit.clip_right;
        }
        if (y1 < blit.clip_top) {
            y1 = blit.clip_top;
        }
        if (y2 > blit.clip_bottom) {
            y2 = blit.clip_bottom;
        }

        if (x1 < x2 && y1 < y2) {
            /* do nothing if totally clipped */
            UBytePtr priority_bitmap_baseaddr = new UBytePtr(priority_bitmap.line[y1], xpos);
            int priority_bitmap_row_offset = priority_bitmap_line_offset * TILE_HEIGHT;

            int priority = blit.tile_priority;
            UBytePtr dest_baseaddr = new UBytePtr(blit.screen.line[y1], xpos);
            UBytePtr dest_next;
            UBytePtr source_baseaddr;
            UBytePtr source_next;

            int c1;
            int c2;
            /* leftmost and rightmost visible columns in source tilemap */
            int y;
            /* current screen line to render */
            int y_next;

            /* convert screen coordinates to source tilemap coordinates */
            x1 -= xpos;
            y1 -= ypos;
            x2 -= xpos;
            y2 -= ypos;

            source_baseaddr = new UBytePtr(blit.pixmap.line[y1]);

            c1 = x1 / TILE_WIDTH;
            /* round down */
            c2 = (x2 + TILE_WIDTH - 1) / TILE_WIDTH;
            /* round up */

            y = y1;
            y_next = TILE_HEIGHT * (y1 / TILE_HEIGHT) + TILE_HEIGHT;
            if (y_next > y2) {
                y_next = y2;
            }

            {
                int dy = y_next - y;
                dest_next = new UBytePtr(dest_baseaddr, dy * blit.dest_line_offset);
                source_next = new UBytePtr(source_baseaddr, dy * blit.source_line_offset);
            }

            for (;;) {
                int row = y / TILE_HEIGHT;
                UBytePtr priority_data = new UBytePtr(blit.priority_data_row[row]);

                int tile_type;
                int prev_tile_type = TILE_TRANSPARENT;

                int x_start = x1;
                int x_end;

                int column;
                for (column = c1; column <= c2; column++) {
                    if (column == c2 || priority_data.read(column) != priority) {
                        tile_type = TILE_TRANSPARENT;
                    } else {
                        tile_type = TILE_OPAQUE;
                    }

                    if (tile_type != prev_tile_type) {
                        x_end = column * TILE_WIDTH;
                        if (x_end < x1) {
                            x_end = x1;
                        }
                        if (x_end > x2) {
                            x_end = x2;
                        }

                        if (prev_tile_type != TILE_TRANSPARENT) {
                            /* TILE_OPAQUE */
                            int num_pixels = x_end - x_start;
                            UBytePtr dest0 = new UBytePtr(dest_baseaddr, x_start);
                            UBytePtr pmap0 = new UBytePtr(priority_bitmap_baseaddr, x_start);
                            UBytePtr source0 = new UBytePtr(source_baseaddr, x_start);
                            int i = y;
                            for (;;) {
                                memcpy(new UBytePtr(dest0), new UBytePtr(source0), num_pixels);
                                memset(new UBytePtr(pmap0), tilemap_priority_code, num_pixels);
                                if (++i == y_next) {
                                    break;
                                }

                                dest0.offset += blit.dest_line_offset;
                                pmap0.offset += priority_bitmap_line_offset;
                                source0.offset += blit.source_line_offset;
                            }
                        }
                        x_start = x_end;
                    }

                    prev_tile_type = tile_type;
                }

                if (y_next == y2) {
                    break;
                    /* we are done! */
                }

                priority_bitmap_baseaddr.offset += priority_bitmap_row_offset;
                dest_baseaddr = new UBytePtr(dest_next);
                source_baseaddr = new UBytePtr(source_next);

                y = y_next;
                y_next += TILE_HEIGHT;

                if (y_next >= y2) {
                    y_next = y2;
                } else {
                    dest_next.offset += blit.dest_row_offset;
                    source_next.offset += blit.source_row_offset;
                }
            }
            /* process next row */
        }
        /* not totally clipped */
    }
        
    public static DrawHandlerPtr draw_opaque16x16x8BPP = new DrawHandlerPtr() {
        public void handler(int xpos, int ypos) {
            generic8draw_opaque(xpos, ypos, 16, 16);
        }
    };    
    
    public static DrawHandlerPtr draw32x32x8BPP = new DrawHandlerPtr() {
        @Override
        public void handler(int xpos, int ypos) {      
            generic8draw(xpos, ypos, 32, 32);
        }
    };
    
    public static DrawHandlerPtr draw_opaque32x32x8BPP = new DrawHandlerPtr() {
        @Override
        public void handler(int xpos, int ypos) {
            throw new UnsupportedOperationException("Not supported yet.");
            /*TODO*///DECLARE( draw_opaque, (int xpos, int ypos),
/*TODO*///{
/*TODO*///	struct osd_bitmap *screen = blit.screen;
/*TODO*///	int tilemap_priority_code = blit.tilemap_priority_code;
/*TODO*///	int x1 = xpos;
/*TODO*///	int y1 = ypos;
/*TODO*///	int x2 = xpos+blit.source_width;
/*TODO*///	int y2 = ypos+blit.source_height;
/*TODO*///	DATA_TYPE *dest_baseaddr = NULL;
/*TODO*///	DATA_TYPE *dest_next;
/*TODO*///	int dy;
/*TODO*/////	int count;
/*TODO*///	const DATA_TYPE *source0;
/*TODO*///	DATA_TYPE *dest0;
/*TODO*///	UINT8 *pmap0;
/*TODO*///	int i;
/*TODO*///	int row;
/*TODO*///	UINT8 *priority_data;
/*TODO*///	int tile_type;
/*TODO*///	int prev_tile_type;
/*TODO*///	int x_start;
/*TODO*///	int x_end;
/*TODO*///	int column;
/*TODO*///	int c1;
/*TODO*///	int c2; /* leftmost and rightmost visible columns in source tilemap */
/*TODO*///	int y; /* current screen line to render */
/*TODO*///	int y_next;
/*TODO*///	int num_pixels;
/*TODO*///	int priority_bitmap_row_offset;
/*TODO*///	UINT8 *priority_bitmap_baseaddr;
/*TODO*///	UINT8 *priority_bitmap_next;
/*TODO*///	int priority;
/*TODO*///	const DATA_TYPE *source_baseaddr;
/*TODO*///	const DATA_TYPE *source_next;
/*TODO*///
/*TODO*/////	const UINT8 *mask0;
/*TODO*/////	UINT8 *mask_data;
/*TODO*/////	const UINT8 *mask_baseaddr;
/*TODO*/////	const UINT8 *mask_next;
/*TODO*///
/*TODO*///	/* clip source coordinates */
/*TODO*///	if( x1<blit.clip_left ) x1 = blit.clip_left;
/*TODO*///	if( x2>blit.clip_right ) x2 = blit.clip_right;
/*TODO*///	if( y1<blit.clip_top ) y1 = blit.clip_top;
/*TODO*///	if( y2>blit.clip_bottom ) y2 = blit.clip_bottom;
/*TODO*///
/*TODO*///	if( x1<x2 && y1<y2 )
/*TODO*///	{ /* do nothing if totally clipped */
/*TODO*///		priority_bitmap_row_offset = priority_bitmap_line_offset*TILE_SIZE;
/*TODO*///		priority_bitmap_baseaddr = xpos + (UINT8 *)priority_bitmap->line[y1];
/*TODO*///		priority = blit.tile_priority;
/*TODO*///		if( screen )
/*TODO*///		{
/*TODO*///			dest_baseaddr = xpos + (DATA_TYPE *)screen->line[y1];
/*TODO*///		}
/*TODO*///
/*TODO*///		/* convert screen coordinates to source tilemap coordinates */
/*TODO*///		x1 -= xpos;
/*TODO*///		y1 -= ypos;
/*TODO*///		x2 -= xpos;
/*TODO*///		y2 -= ypos;
/*TODO*///
/*TODO*///		source_baseaddr = (DATA_TYPE *)blit.pixmap->line[y1];
/*TODO*/////		mask_baseaddr = blit.bitmask->line[y1];
/*TODO*///
/*TODO*///		c1 = x1/TILE_SIZE; /* round down */
/*TODO*///		c2 = (x2+TILE_SIZE-1)/TILE_SIZE; /* round up */
/*TODO*///
/*TODO*///		y = y1;
/*TODO*///		y_next = TILE_SIZE*(y1/TILE_SIZE) + TILE_SIZE;
/*TODO*///		if( y_next>y2 ) y_next = y2;
/*TODO*///
/*TODO*///		dy = y_next-y;
/*TODO*///		dest_next = dest_baseaddr + dy*blit.dest_line_offset;
/*TODO*///		priority_bitmap_next = priority_bitmap_baseaddr + dy*priority_bitmap_line_offset;
/*TODO*///		source_next = source_baseaddr + dy*blit.source_line_offset;
/*TODO*/////		mask_next = mask_baseaddr + dy*blit.mask_line_offset;
/*TODO*///		for(;;)
/*TODO*///		{
/*TODO*///			row = y/TILE_SIZE;
/*TODO*/////			mask_data = blit.mask_data_row[row];
/*TODO*///			priority_data = blit.priority_data_row[row];
/*TODO*///			prev_tile_type = TILE_TRANSPARENT;
/*TODO*///			x_start = x1;
/*TODO*///
/*TODO*///			for( column=c1; column<=c2; column++ )
/*TODO*///			{
/*TODO*///				if( column==c2 || priority_data[column]!=priority )
/*TODO*///				{
/*TODO*///					tile_type = TILE_TRANSPARENT;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*/////					tile_type = mask_data[column];
/*TODO*///					tile_type = TILE_OPAQUE;
/*TODO*///				}
/*TODO*///
/*TODO*///				if( tile_type!=prev_tile_type )
/*TODO*///				{
/*TODO*///					x_end = column*TILE_SIZE;
/*TODO*///					if( x_end<x1 ) x_end = x1;
/*TODO*///					if( x_end>x2 ) x_end = x2;
/*TODO*///
/*TODO*///					if( prev_tile_type != TILE_TRANSPARENT )
/*TODO*///					{
/*TODO*/////						if( prev_tile_type == TILE_MASKED )
/*TODO*/////						{
/*TODO*/////							count = (x_end+7)/8 - x_start/8;
/*TODO*/////							mask0 = mask_baseaddr + x_start/8;
/*TODO*/////							source0 = source_baseaddr + (x_start&0xfff8);
/*TODO*/////							dest0 = dest_baseaddr + (x_start&0xfff8);
/*TODO*/////							pmap0 = priority_bitmap_baseaddr + (x_start&0xfff8);
/*TODO*/////							i = y;
/*TODO*/////							for(;;)
/*TODO*/////							{
/*TODO*/////								if( screen ) memcpybitmask( dest0, source0, mask0, count );
/*TODO*/////								memsetbitmask8( pmap0, tilemap_priority_code, mask0, count );
/*TODO*/////								if( ++i == y_next ) break;
/*TODO*/////
/*TODO*/////								dest0 += blit.dest_line_offset;
/*TODO*/////								source0 += blit.source_line_offset;
/*TODO*/////								mask0 += blit.mask_line_offset;
/*TODO*/////								pmap0 += priority_bitmap_line_offset;
/*TODO*/////							}
/*TODO*/////						}
/*TODO*/////						else
/*TODO*///						{ /* TILE_OPAQUE */
/*TODO*///							num_pixels = x_end - x_start;
/*TODO*///							dest0 = dest_baseaddr+x_start;
/*TODO*///							source0 = source_baseaddr+x_start;
/*TODO*///							pmap0 = priority_bitmap_baseaddr + x_start;
/*TODO*///							i = y;
/*TODO*///							for(;;)
/*TODO*///							{
/*TODO*///								if( screen ) memcpy( dest0, source0, num_pixels*sizeof(DATA_TYPE) );
/*TODO*///								memset( pmap0, tilemap_priority_code, num_pixels );
/*TODO*///								if( ++i == y_next ) break;
/*TODO*///
/*TODO*///								dest0 += blit.dest_line_offset;
/*TODO*///								source0 += blit.source_line_offset;
/*TODO*///								pmap0 += priority_bitmap_line_offset;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///					x_start = x_end;
/*TODO*///				}
/*TODO*///
/*TODO*///				prev_tile_type = tile_type;
/*TODO*///			}
/*TODO*///
/*TODO*///			if( y_next==y2 ) break; /* we are done! */
/*TODO*///
/*TODO*///			priority_bitmap_baseaddr = priority_bitmap_next;
/*TODO*///			dest_baseaddr = dest_next;
/*TODO*///			source_baseaddr = source_next;
/*TODO*/////			mask_baseaddr = mask_next;
/*TODO*///			y = y_next;
/*TODO*///			y_next += TILE_SIZE;
/*TODO*///
/*TODO*///			if( y_next>=y2 )
/*TODO*///			{
/*TODO*///				y_next = y2;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				dest_next += blit.dest_row_offset;
/*TODO*///				priority_bitmap_next += priority_bitmap_row_offset;
/*TODO*///				source_next += blit.source_row_offset;
/*TODO*/////				mask_next += blit.mask_row_offset;
/*TODO*///			}
/*TODO*///		} /* process next row */
/*TODO*///	} /* not totally clipped */
/*TODO*///})
        }
    };
    
}
