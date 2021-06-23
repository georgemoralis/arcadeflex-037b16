/**
 * ported to v0.37b16
 *
 */
package gr.codebb.arcadeflex.WIP.v037b16.mame;

import common.ptr.UBytePtr;
import common.subArrays.IntArray;
import gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.GetMemoryOffsetPtr;
import gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.GetTileInfoPtr;
import gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapH.struct_tile_info;
import gr.codebb.arcadeflex.v037b16.mame.osdependH.osd_bitmap;

public class tilemapC {

    /*TODO*///#ifndef DECLARE
/*TODO*///
/*TODO*///#include "driver.h"
/*TODO*///#include "tilemap.h"
/*TODO*///#include "state.h"
/*TODO*///
/*TODO*///#define SWAP(X,Y) { UINT32 temp=X; X=Y; Y=temp; }
/*TODO*///#define MAX_TILESIZE 32
/*TODO*///#define MASKROWBYTES(W) (((W)+7)/8)
/*TODO*///
/*TODO*///struct cached_tile_info
/*TODO*///{
/*TODO*///	const UINT8 *pen_data;
/*TODO*///	const UINT32 *pal_data;
/*TODO*///	UINT32 pen_usage;
/*TODO*///	UINT32 flags;
/*TODO*///	int skip;
/*TODO*///};
/*TODO*///
/*TODO*///struct tilemap_mask
/*TODO*///{
/*TODO*///	struct osd_bitmap *bitmask;
/*TODO*///	int line_offset;
/*TODO*///	UINT8 *data;
/*TODO*///	UINT8 **data_row;
/*TODO*///};
/*TODO*///
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

        /*TODO*///	struct cached_tile_info *cached_tile_info;
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
        /*TODO*///
/*TODO*///	void (*draw_tile)( struct tilemap *tilemap, UINT32 cached_indx, UINT32 col, UINT32 row );
/*TODO*///
/*TODO*///	void (*draw)( int, int );
/*TODO*///	void (*draw_opaque)( int, int );
/*TODO*///	void (*draw_alpha)( int, int );
/*TODO*///
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
        /*TODO*///
/*TODO*///	UINT16 tile_depth, tile_granularity;
/*TODO*///	UINT8 *tile_dirty_map;
/*TODO*///
        /* cached color data */
        public osd_bitmap pixmap;
        public int pixmap_line_offset;
        /*TODO*///
/*TODO*///	struct tilemap_mask *foreground;
/*TODO*///	/* for transparent layers, or the front half of a split layer */
/*TODO*///
/*TODO*///	struct tilemap_mask *background;
/*TODO*///	/* for the back half of a split layer */
/*TODO*///
        public struct_tilemap next;
    }
    /*TODO*///
/*TODO*///struct osd_bitmap *priority_bitmap; /* priority buffer (corresponds to screen bitmap) */
/*TODO*///int priority_bitmap_line_offset;
/*TODO*///
/*TODO*///static UINT8 flip_bit_table[0x100]; /* horizontal flip for 8 pixels */
/*TODO*///static struct tilemap *first_tilemap; /* resource tracking */
/*TODO*///static int screen_width, screen_height;
    public static struct_tile_info tile_info = new struct_tile_info();

    /*TODO*///
/*TODO*///enum
/*TODO*///{
/*TODO*///	TILE_TRANSPARENT,
/*TODO*///	TILE_MASKED,
/*TODO*///	TILE_OPAQUE
/*TODO*///};
/*TODO*///
/*TODO*////* the following parameters are constant across tilemap_draw calls */
/*TODO*///static struct
/*TODO*///{
/*TODO*///	int clip_left, clip_top, clip_right, clip_bottom;
/*TODO*///	int source_width, source_height;
/*TODO*///	int dest_line_offset,source_line_offset,mask_line_offset;
/*TODO*///	int dest_row_offset,source_row_offset,mask_row_offset;
/*TODO*///	struct osd_bitmap *screen, *pixmap, *bitmask;
/*TODO*///	UINT8 **mask_data_row;
/*TODO*///	UINT8 **priority_data_row;
/*TODO*///	int tile_priority;
/*TODO*///	int tilemap_priority_code;
/*TODO*///} blit;
/*TODO*///
/*TODO*///int PenToPixel_Init( struct tilemap *tilemap )
/*TODO*///{
/*TODO*///	/*
/*TODO*///		Construct a table for all tile orientations in advance.
/*TODO*///		This simplifies drawing tiles and masks tremendously.
/*TODO*///		If performance is an issue, we can always (re)introduce
/*TODO*///		customized code for each case and forgo tables.
/*TODO*///	*/
/*TODO*///	int i,x,y,tx,ty;
/*TODO*///	int tile_size = tilemap->tile_size;
/*TODO*///	UINT32 *pPenToPixel;
/*TODO*///	int lError;
/*TODO*///
/*TODO*///	lError = 0;
/*TODO*///	for( i=0; i<8; i++ )
/*TODO*///	{
/*TODO*///		pPenToPixel = malloc( tilemap->num_pens*sizeof(UINT32) );
/*TODO*///		if( pPenToPixel==NULL )
/*TODO*///		{
/*TODO*///			lError = 1;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			tilemap->pPenToPixel[i] = pPenToPixel;
/*TODO*///			for( ty=0; ty<tile_size; ty++ )
/*TODO*///			{
/*TODO*///				for( tx=0; tx<tile_size; tx++ )
/*TODO*///				{
/*TODO*///					if( i&TILE_SWAPXY )
/*TODO*///					{
/*TODO*///						x = ty;
/*TODO*///						y = tx;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						x = tx;
/*TODO*///						y = ty;
/*TODO*///					}
/*TODO*///					if( i&TILE_FLIPX ) x = tile_size-1-x;
/*TODO*///					if( i&TILE_FLIPY ) y = tile_size-1-y;
/*TODO*///					*pPenToPixel++ = x+y*MAX_TILESIZE;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return lError;
/*TODO*///}
/*TODO*///
/*TODO*///void PenToPixel_Term( struct tilemap *tilemap )
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	for( i=0; i<8; i++ )
/*TODO*///	{
/*TODO*///		free( tilemap->pPenToPixel[i] );
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void tmap_render( struct tilemap *tilemap )
/*TODO*///{
/*TODO*///	if( tilemap->bNeedRender ){
/*TODO*///		tilemap->bNeedRender = 0;
/*TODO*///		if( tilemap->enable ){
/*TODO*///			UINT8 *dirty_pixels = tilemap->dirty_pixels;
/*TODO*///			const UINT8 *visible = tilemap->visible;
/*TODO*///			UINT32 cached_indx = 0;
/*TODO*///			UINT32 row,col;
/*TODO*///
/*TODO*///			/* walk over cached rows/cols (better to walk screen coords) */
/*TODO*///			for( row=0; row<tilemap->num_cached_rows; row++ ){
/*TODO*///				for( col=0; col<tilemap->num_cached_cols; col++ ){
/*TODO*///					if( visible[cached_indx] && dirty_pixels[cached_indx] ){
/*TODO*///						tilemap->draw_tile( tilemap, cached_indx, col, row );
/*TODO*///						dirty_pixels[cached_indx] = 0;
/*TODO*///					}
/*TODO*///					cached_indx++;
/*TODO*///				} /* next col */
/*TODO*///			} /* next row */
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///struct osd_bitmap *tilemap_get_pixmap( struct tilemap * tilemap )
/*TODO*///{
/*TODO*///profiler_mark(PROFILER_TILEMAP_DRAW);
/*TODO*///	tmap_render( tilemap );
/*TODO*///profiler_mark(PROFILER_END);
/*TODO*///	return tilemap->pixmap;
/*TODO*///}
/*TODO*///
/*TODO*///void tilemap_set_transparent_pen( struct tilemap *tilemap, int pen )
/*TODO*///{
/*TODO*///	tilemap->transparent_pen = pen;
/*TODO*///}
/*TODO*///
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
/*TODO*///
/*TODO*////***********************************************************************************/
/*TODO*////* some common mappings */
/*TODO*///
/*TODO*///UINT32 tilemap_scan_rows( UINT32 col, UINT32 row, UINT32 num_cols, UINT32 num_rows )
/*TODO*///{
/*TODO*///	/* logical (col,row) -> memory offset */
/*TODO*///	return row*num_cols + col;
/*TODO*///}
/*TODO*///UINT32 tilemap_scan_cols( UINT32 col, UINT32 row, UINT32 num_cols, UINT32 num_rows )
/*TODO*///{
/*TODO*///	/* logical (col,row) -> memory offset */
/*TODO*///	return col*num_rows + row;
/*TODO*///}
/*TODO*///
/*TODO*////*********************************************************************************/
/*TODO*///
/*TODO*///static struct osd_bitmap *create_tmpbitmap( int width, int height, int depth )
/*TODO*///{
/*TODO*///	return osd_alloc_bitmap( width,height,depth );
/*TODO*///}
/*TODO*///
/*TODO*///static struct osd_bitmap *create_bitmask( int width, int height )
/*TODO*///{
/*TODO*///	width = (width+7)/8; /* 8 bits per byte */
/*TODO*///	return osd_alloc_bitmap( width,height, 8 );
/*TODO*///}
/*TODO*///
/*TODO*////***********************************************************************************/
/*TODO*///
/*TODO*///static int mappings_create( struct tilemap *tilemap )
/*TODO*///{
/*TODO*///	int max_memory_offset = 0;
/*TODO*///	UINT32 col,row;
/*TODO*///	UINT32 num_logical_rows = tilemap->num_logical_rows;
/*TODO*///	UINT32 num_logical_cols = tilemap->num_logical_cols;
/*TODO*///	/* count offsets (might be larger than num_tiles) */
/*TODO*///	for( row=0; row<num_logical_rows; row++ )
/*TODO*///	{
/*TODO*///		for( col=0; col<num_logical_cols; col++ )
/*TODO*///		{
/*TODO*///			UINT32 memory_offset = tilemap->get_memory_offset( col, row, num_logical_cols, num_logical_rows );
/*TODO*///			if( memory_offset>max_memory_offset ) max_memory_offset = memory_offset;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	max_memory_offset++;
/*TODO*///	tilemap->max_memory_offset = max_memory_offset;
/*TODO*///	/* logical to cached (tilemap_mark_dirty) */
/*TODO*///	tilemap->memory_offset_to_cached_indx = malloc( sizeof(int)*max_memory_offset );
/*TODO*///	if( tilemap->memory_offset_to_cached_indx )
/*TODO*///	{
/*TODO*///		/* cached to logical (get_tile_info) */
/*TODO*///		tilemap->cached_indx_to_memory_offset = malloc( sizeof(UINT32)*tilemap->num_tiles );
/*TODO*///		if( tilemap->cached_indx_to_memory_offset ) return 0; /* no error */
/*TODO*///		free( tilemap->memory_offset_to_cached_indx );
/*TODO*///	}
/*TODO*///	return -1; /* error */
/*TODO*///}
/*TODO*///
/*TODO*///static void mappings_dispose( struct tilemap *tilemap )
/*TODO*///{
/*TODO*///	free( tilemap->cached_indx_to_memory_offset );
/*TODO*///	free( tilemap->memory_offset_to_cached_indx );
/*TODO*///}
/*TODO*///
/*TODO*///static void mappings_update( struct tilemap *tilemap )
/*TODO*///{
/*TODO*///	int logical_flip;
/*TODO*///	UINT32 logical_indx, cached_indx;
/*TODO*///	UINT32 num_cached_rows = tilemap->num_cached_rows;
/*TODO*///	UINT32 num_cached_cols = tilemap->num_cached_cols;
/*TODO*///	UINT32 num_logical_rows = tilemap->num_logical_rows;
/*TODO*///	UINT32 num_logical_cols = tilemap->num_logical_cols;
/*TODO*///	for( logical_indx=0; logical_indx<tilemap->max_memory_offset; logical_indx++ )
/*TODO*///	{
/*TODO*///		tilemap->memory_offset_to_cached_indx[logical_indx] = -1;
/*TODO*///	}
/*TODO*///
/*TODO*///	for( logical_indx=0; logical_indx<tilemap->num_tiles; logical_indx++ )
/*TODO*///	{
/*TODO*///		UINT32 logical_col = logical_indx%num_logical_cols;
/*TODO*///		UINT32 logical_row = logical_indx/num_logical_cols;
/*TODO*///		int memory_offset = tilemap->get_memory_offset( logical_col, logical_row, num_logical_cols, num_logical_rows );
/*TODO*///		UINT32 cached_col = logical_col;
/*TODO*///		UINT32 cached_row = logical_row;
/*TODO*///		if( tilemap->orientation & ORIENTATION_SWAP_XY ) SWAP(cached_col,cached_row)
/*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_X ) cached_col = (num_cached_cols-1)-cached_col;
/*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_Y ) cached_row = (num_cached_rows-1)-cached_row;
/*TODO*///		cached_indx = cached_row*num_cached_cols+cached_col;
/*TODO*///		tilemap->memory_offset_to_cached_indx[memory_offset] = cached_indx;
/*TODO*///		tilemap->cached_indx_to_memory_offset[cached_indx] = memory_offset;
/*TODO*///	}
/*TODO*///	for( logical_flip = 0; logical_flip<4; logical_flip++ )
/*TODO*///	{
/*TODO*///		int cached_flip = logical_flip;
/*TODO*///		if( tilemap->attributes&TILEMAP_FLIPX ) cached_flip ^= TILE_FLIPX;
/*TODO*///		if( tilemap->attributes&TILEMAP_FLIPY ) cached_flip ^= TILE_FLIPY;
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
/*TODO*///		if( tilemap->orientation & ORIENTATION_SWAP_XY )
/*TODO*///		{
/*TODO*///			cached_flip = ((cached_flip&1)<<1) | ((cached_flip&2)>>1);
/*TODO*///		}
/*TODO*///		tilemap->logical_flip_to_cached_flip[logical_flip] = cached_flip;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////***********************************************************************************/
/*TODO*///
/*TODO*///static void memsetbitmask8( UINT8 *dest, int value, const UINT8 *bitmask, int count )
/*TODO*///{
/*TODO*///	for(;;)
/*TODO*///	{
/*TODO*///		UINT32 data = *bitmask++;
/*TODO*///		if( data&0x80 ) dest[0] |= value;
/*TODO*///		if( data&0x40 ) dest[1] |= value;
/*TODO*///		if( data&0x20 ) dest[2] |= value;
/*TODO*///		if( data&0x10 ) dest[3] |= value;
/*TODO*///		if( data&0x08 ) dest[4] |= value;
/*TODO*///		if( data&0x04 ) dest[5] |= value;
/*TODO*///		if( data&0x02 ) dest[6] |= value;
/*TODO*///		if( data&0x01 ) dest[7] |= value;
/*TODO*///		if( --count == 0 ) break;
/*TODO*///		dest+=8;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void memcpybitmask8( UINT8 *dest, const UINT8 *source, const UINT8 *bitmask, int count )
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
/*TODO*///#define TILE_SIZE	8
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
/*TODO*///
/*TODO*////*********************************************************************************/
/*TODO*///
/*TODO*///static void mask_dispose( struct tilemap_mask *mask )
/*TODO*///{
/*TODO*///	if( mask )
/*TODO*///	{
/*TODO*///		free( mask->data_row );
/*TODO*///		free( mask->data );
/*TODO*///		osd_free_bitmap( mask->bitmask );
/*TODO*///		free( mask );
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static struct tilemap_mask *mask_create( struct tilemap *tilemap )
/*TODO*///{
/*TODO*///	int row;
/*TODO*///	struct tilemap_mask *mask = malloc( sizeof(struct tilemap_mask) );
/*TODO*///	if( mask )
/*TODO*///	{
/*TODO*///		mask->data = malloc( tilemap->num_tiles );
/*TODO*///		mask->data_row = malloc( tilemap->num_cached_rows * sizeof(UINT8 *) );
/*TODO*///		mask->bitmask = create_bitmask( tilemap->cached_width, tilemap->cached_height );
/*TODO*///		if( mask->data && mask->data_row && mask->bitmask )
/*TODO*///		{
/*TODO*///			for( row=0; row<tilemap->num_cached_rows; row++ )
/*TODO*///			{
/*TODO*///				mask->data_row[row] = mask->data + row*tilemap->num_cached_cols;
/*TODO*///			}
/*TODO*///			mask->line_offset = mask->bitmask->line[1] - mask->bitmask->line[0];
/*TODO*///			return mask;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	mask_dispose( mask );
/*TODO*///	return NULL;
/*TODO*///}
/*TODO*///
/*TODO*////***********************************************************************************/
/*TODO*///
/*TODO*///static void install_draw_handlers( struct tilemap *tilemap )
/*TODO*///{
/*TODO*///	int tile_size = tilemap->tile_size;
/*TODO*///	tilemap->draw = tilemap->draw_opaque = tilemap->draw_alpha = NULL;
/*TODO*///	switch( Machine->scrbitmap->depth )
/*TODO*///	{
/*TODO*///	case 32:
/*TODO*///		tilemap->draw_tile = draw_tile8x8x32BPP;
/*TODO*///
/*TODO*///		if( tile_size==8 )
/*TODO*///		{
/*TODO*///			tilemap->draw = draw8x8x32BPP;
/*TODO*///			tilemap->draw_opaque = draw_opaque8x8x32BPP;
/*TODO*///			tilemap->draw_alpha = draw_alpha8x8x32BPP;
/*TODO*///		}
/*TODO*///		else if( tile_size==16 )
/*TODO*///		{
/*TODO*///			tilemap->draw = draw16x16x32BPP;
/*TODO*///			tilemap->draw_opaque = draw_opaque16x16x32BPP;
/*TODO*///			tilemap->draw_alpha = draw_alpha16x16x32BPP;
/*TODO*///		}
/*TODO*///		else if( tile_size==32 )
/*TODO*///		{
/*TODO*///			tilemap->draw = draw32x32x32BPP;
/*TODO*///			tilemap->draw_opaque = draw_opaque32x32x32BPP;
/*TODO*///			tilemap->draw_alpha = draw_alpha32x32x32BPP;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///
/*TODO*///	case 15:
/*TODO*///	case 16:
/*TODO*///		tilemap->draw_tile = draw_tile8x8x16BPP;
/*TODO*///
/*TODO*///		if( tile_size==8 )
/*TODO*///		{
/*TODO*///			tilemap->draw = draw8x8x16BPP;
/*TODO*///			tilemap->draw_opaque = draw_opaque8x8x16BPP;
/*TODO*///			tilemap->draw_alpha = draw_alpha8x8x16BPP;
/*TODO*///		}
/*TODO*///		else if( tile_size==16 )
/*TODO*///		{
/*TODO*///			tilemap->draw = draw16x16x16BPP;
/*TODO*///			tilemap->draw_opaque = draw_opaque16x16x16BPP;
/*TODO*///			tilemap->draw_alpha = draw_alpha16x16x16BPP;
/*TODO*///		}
/*TODO*///		else if( tile_size==32 )
/*TODO*///		{
/*TODO*///			tilemap->draw = draw32x32x16BPP;
/*TODO*///			tilemap->draw_opaque = draw_opaque32x32x16BPP;
/*TODO*///			tilemap->draw_alpha = draw_alpha32x32x16BPP;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///
/*TODO*///	case 8:
/*TODO*///		tilemap->draw_tile = draw_tile8x8x8BPP;
/*TODO*///
/*TODO*///		if( tile_size==8 )
/*TODO*///		{
/*TODO*///			tilemap->draw = draw8x8x8BPP;
/*TODO*///			tilemap->draw_opaque = draw_opaque8x8x8BPP;
/*TODO*///			tilemap->draw_alpha = draw8x8x8BPP;
/*TODO*///		}
/*TODO*///		else if( tile_size==16 )
/*TODO*///		{
/*TODO*///			tilemap->draw = draw16x16x8BPP;
/*TODO*///			tilemap->draw_opaque = draw_opaque16x16x8BPP;
/*TODO*///			tilemap->draw_alpha = draw16x16x8BPP;
/*TODO*///		}
/*TODO*///		else if( tile_size==32 )
/*TODO*///		{
/*TODO*///			tilemap->draw = draw32x32x8BPP;
/*TODO*///			tilemap->draw_opaque = draw_opaque32x32x8BPP;
/*TODO*///			tilemap->draw_alpha = draw32x32x8BPP;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////***********************************************************************************/
/*TODO*///
/*TODO*///static void tilemap_reset(void)
/*TODO*///{
/*TODO*///	tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
/*TODO*///}
/*TODO*///
    public static int tilemap_init() {
        System.out.println("todo tilemap_init");
        /*TODO*///	UINT32 value, data, bit;
/*TODO*///	for( value=0; value<0x100; value++ )
/*TODO*///	{
/*TODO*///		data = 0;
/*TODO*///		for( bit=0; bit<8; bit++ ) if( (value>>bit)&1 ) data |= 0x80>>bit;
/*TODO*///		flip_bit_table[value] = data;
/*TODO*///	}
/*TODO*///	screen_width = Machine->scrbitmap->width;
/*TODO*///	screen_height = Machine->scrbitmap->height;
/*TODO*///	first_tilemap = 0;
/*TODO*///	state_save_register_func_postload(tilemap_reset);
/*TODO*///	priority_bitmap = create_tmpbitmap( screen_width, screen_height, 8 );
/*TODO*///	if( priority_bitmap ){
/*TODO*///		priority_bitmap_line_offset = priority_bitmap->line[1] - priority_bitmap->line[0];
/*TODO*///		return 0;
/*TODO*///	}
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

    /*TODO*///
/*TODO*////***********************************************************************************/
/*TODO*///
/*TODO*///struct tilemap *tilemap_create(
/*TODO*///	void (*tile_get_info)( int memory_offset ),
/*TODO*///	UINT32 (*get_memory_offset)( UINT32 col, UINT32 row, UINT32 num_cols, UINT32 num_rows ),
/*TODO*///	int type,
/*TODO*///	int tile_width, int tile_height,
/*TODO*///	int num_cols, int num_rows )
/*TODO*///{
/*TODO*///	struct tilemap *tilemap;
/*TODO*///
/*TODO*///	if( tile_width != tile_height )
/*TODO*///	{
/*TODO*///		logerror( "tilemap_create: tile_width must be equal to tile_height\n" );
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	tilemap = calloc( 1,sizeof( struct tilemap ) );
/*TODO*///	if( tilemap )
/*TODO*///	{
/*TODO*///		int num_tiles = num_cols*num_rows;
/*TODO*///		tilemap->num_logical_cols = num_cols;
/*TODO*///		tilemap->num_logical_rows = num_rows;
/*TODO*///		if( Machine->orientation & ORIENTATION_SWAP_XY )
/*TODO*///		{
/*TODO*///			SWAP( num_cols,num_rows )
/*TODO*///		}
/*TODO*///		tilemap->num_cached_cols = num_cols;
/*TODO*///		tilemap->num_cached_rows = num_rows;
/*TODO*///		tilemap->num_tiles = num_tiles;
/*TODO*///		tilemap->num_pens = tile_width*tile_height;
/*TODO*///		tilemap->tile_size = tile_width; /* tile_width and tile_height are equal */
/*TODO*///		tilemap->cached_width = tile_width*num_cols;
/*TODO*///		tilemap->cached_height = tile_height*num_rows;
/*TODO*///		tilemap->tile_get_info = tile_get_info;
/*TODO*///		tilemap->get_memory_offset = get_memory_offset;
/*TODO*///		tilemap->orientation = Machine->orientation;
/*TODO*///
/*TODO*///		/* various defaults */
/*TODO*///		tilemap->enable = 1;
/*TODO*///		tilemap->type = type;
/*TODO*///		tilemap->scroll_rows = 1;
/*TODO*///		tilemap->scroll_cols = 1;
/*TODO*///		tilemap->transparent_pen = -1;
/*TODO*///		tilemap->tile_depth = 0;
/*TODO*///		tilemap->tile_granularity = 0;
/*TODO*///		tilemap->tile_dirty_map = 0;
/*TODO*///
/*TODO*///		tilemap->cached_tile_info = calloc( num_tiles, sizeof(struct cached_tile_info) );
/*TODO*///		tilemap->priority = calloc( num_tiles,1 );
/*TODO*///		tilemap->visible = calloc( num_tiles,1 );
/*TODO*///		tilemap->dirty_vram = malloc( num_tiles );
/*TODO*///		tilemap->dirty_pixels = malloc( num_tiles );
/*TODO*///		tilemap->rowscroll = calloc(tilemap->cached_height,sizeof(int));
/*TODO*///		tilemap->colscroll = calloc(tilemap->cached_width,sizeof(int));
/*TODO*///		tilemap->priority_row = malloc( sizeof(UINT8 *)*num_rows );
/*TODO*///		tilemap->pixmap = create_tmpbitmap( tilemap->cached_width, tilemap->cached_height, Machine->scrbitmap->depth );
/*TODO*///		tilemap->foreground = mask_create( tilemap );
/*TODO*///		tilemap->background = (type & TILEMAP_SPLIT)?mask_create( tilemap ):NULL;
/*TODO*///
/*TODO*///		if( tilemap->cached_tile_info &&
/*TODO*///			tilemap->priority && tilemap->visible &&
/*TODO*///			tilemap->dirty_vram && tilemap->dirty_pixels &&
/*TODO*///			tilemap->rowscroll && tilemap->colscroll &&
/*TODO*///			tilemap->priority_row &&
/*TODO*///			tilemap->pixmap && tilemap->foreground &&
/*TODO*///			((type&TILEMAP_SPLIT)==0 || tilemap->background) &&
/*TODO*///			(mappings_create( tilemap )==0) )
/*TODO*///		{
/*TODO*///			UINT32 row;
/*TODO*///			for( row=0; row<num_rows; row++ ){
/*TODO*///				tilemap->priority_row[row] = tilemap->priority+num_cols*row;
/*TODO*///			}
/*TODO*///			install_draw_handlers( tilemap );
/*TODO*///			mappings_update( tilemap );
/*TODO*///			tilemap_set_clip( tilemap, &Machine->visible_area );
/*TODO*///			memset( tilemap->dirty_vram, 1, num_tiles );
/*TODO*///			memset( tilemap->dirty_pixels, 1, num_tiles );
/*TODO*///			tilemap->pixmap_line_offset = tilemap->pixmap->line[1] - tilemap->pixmap->line[0];
/*TODO*///			tilemap->next = first_tilemap;
/*TODO*///			first_tilemap = tilemap;
/*TODO*///			if( PenToPixel_Init( tilemap ) == 0 )
/*TODO*///			{
/*TODO*///				return tilemap;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		tilemap_dispose( tilemap );
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///void tilemap_dispose( struct tilemap *tilemap )
/*TODO*///{
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
/*TODO*///}
/*TODO*///
/*TODO*////***********************************************************************************/
/*TODO*///
/*TODO*///static void unregister_pens( struct cached_tile_info *cached_tile_info, int num_pens )
/*TODO*///{
/*TODO*///	if( palette_used_colors )
/*TODO*///	{
/*TODO*///		const UINT32 *pal_data = cached_tile_info->pal_data;
/*TODO*///		if( pal_data )
/*TODO*///		{
/*TODO*///			UINT32 pen_usage = cached_tile_info->pen_usage;
/*TODO*///			if( pen_usage )
/*TODO*///			{
/*TODO*///				palette_decrease_usage_count(
/*TODO*///					pal_data-Machine->remapped_colortable,
/*TODO*///					pen_usage,
/*TODO*///					PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				palette_decrease_usage_countx(
/*TODO*///					pal_data-Machine->remapped_colortable,
/*TODO*///					num_pens,
/*TODO*///					cached_tile_info->pen_data,
/*TODO*///					PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
/*TODO*///			}
/*TODO*///			cached_tile_info->pal_data = NULL;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void register_pens( struct cached_tile_info *cached_tile_info, int num_pens )
/*TODO*///{
/*TODO*///	if (palette_used_colors)
/*TODO*///	{
/*TODO*///		UINT32 pen_usage = cached_tile_info->pen_usage;
/*TODO*///		if( pen_usage )
/*TODO*///		{
/*TODO*///			palette_increase_usage_count(
/*TODO*///				cached_tile_info->pal_data-Machine->remapped_colortable,
/*TODO*///				pen_usage,
/*TODO*///				PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			palette_increase_usage_countx(
/*TODO*///				cached_tile_info->pal_data-Machine->remapped_colortable,
/*TODO*///				num_pens,
/*TODO*///				cached_tile_info->pen_data,
/*TODO*///				PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////***********************************************************************************/
/*TODO*///
/*TODO*///void tilemap_set_enable( struct tilemap *tilemap, int enable )
/*TODO*///{
/*TODO*///	tilemap->enable = enable?1:0;
/*TODO*///}
/*TODO*///
    public static void tilemap_set_flip(struct_tilemap tilemap, int attributes) {
        System.out.println("TODO tilemap_set_flip");
        /*TODO*///	if( tilemap==ALL_TILEMAPS )
/*TODO*///	{
/*TODO*///		tilemap = first_tilemap;
/*TODO*///		while( tilemap )
/*TODO*///		{
/*TODO*///			tilemap_set_flip( tilemap, attributes );
/*TODO*///			tilemap = tilemap->next;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else if( tilemap->attributes!=attributes )
/*TODO*///	{
/*TODO*///		tilemap->attributes = attributes;
/*TODO*///		tilemap->orientation = Machine->orientation;
/*TODO*///		if( attributes&TILEMAP_FLIPY )
/*TODO*///		{
/*TODO*///			tilemap->orientation ^= ORIENTATION_FLIP_Y;
/*TODO*///			tilemap->scrolly_delta = tilemap->dy_if_flipped;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			tilemap->scrolly_delta = tilemap->dy;
/*TODO*///		}
/*TODO*///		if( attributes&TILEMAP_FLIPX )
/*TODO*///		{
/*TODO*///			tilemap->orientation ^= ORIENTATION_FLIP_X;
/*TODO*///			tilemap->scrollx_delta = tilemap->dx_if_flipped;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			tilemap->scrollx_delta = tilemap->dx;
/*TODO*///		}
/*TODO*///
/*TODO*///		mappings_update( tilemap );
/*TODO*///		tilemap_mark_all_tiles_dirty( tilemap );
/*TODO*///	}
    }

    /*TODO*///
/*TODO*///void tilemap_set_clip( struct tilemap *tilemap, const struct rectangle *clip )
/*TODO*///{
/*TODO*///	int left,top,right,bottom;
/*TODO*///	if( clip )
/*TODO*///	{
/*TODO*///		left = clip->min_x;
/*TODO*///		top = clip->min_y;
/*TODO*///		right = clip->max_x+1;
/*TODO*///		bottom = clip->max_y+1;
/*TODO*///		if( tilemap->orientation & ORIENTATION_SWAP_XY )
/*TODO*///		{
/*TODO*///			SWAP(left,top)
/*TODO*///			SWAP(right,bottom)
/*TODO*///		}
/*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_X )
/*TODO*///		{
/*TODO*///			SWAP(left,right)
/*TODO*///			left = screen_width-left;
/*TODO*///			right = screen_width-right;
/*TODO*///		}
/*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_Y )
/*TODO*///		{
/*TODO*///			SWAP(top,bottom)
/*TODO*///			top = screen_height-top;
/*TODO*///			bottom = screen_height-bottom;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		left = 0;
/*TODO*///		top = 0;
/*TODO*///		right = tilemap->cached_width;
/*TODO*///		bottom = tilemap->cached_height;
/*TODO*///	}
/*TODO*///	tilemap->clip_left = left;
/*TODO*///	tilemap->clip_right = right;
/*TODO*///	tilemap->clip_top = top;
/*TODO*///	tilemap->clip_bottom = bottom;
/*TODO*///}
/*TODO*///
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
/*TODO*///
/*TODO*////***********************************************************************************/
/*TODO*///
/*TODO*///void tilemap_mark_tile_dirty( struct tilemap *tilemap, int memory_offset )
/*TODO*///{
/*TODO*///	if( memory_offset<tilemap->max_memory_offset )
/*TODO*///	{
/*TODO*///		int cached_indx = tilemap->memory_offset_to_cached_indx[memory_offset];
/*TODO*///		if( cached_indx>=0 )
/*TODO*///		{
/*TODO*///			tilemap->dirty_vram[cached_indx] = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void tilemap_mark_all_tiles_dirty( struct tilemap *tilemap )
/*TODO*///{
/*TODO*///	if( tilemap==ALL_TILEMAPS )
/*TODO*///	{
/*TODO*///		tilemap = first_tilemap;
/*TODO*///		while( tilemap )
/*TODO*///		{
/*TODO*///			tilemap_mark_all_tiles_dirty( tilemap );
/*TODO*///			tilemap = tilemap->next;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		memset( tilemap->dirty_vram, 1, tilemap->num_tiles );
/*TODO*///	}
/*TODO*///}
/*TODO*///
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
/*TODO*///
/*TODO*///static void draw_color_mask(
/*TODO*///	struct tilemap *tilemap,
/*TODO*///	struct osd_bitmap *mask,
/*TODO*///	UINT32 x0, UINT32 y0,
/*TODO*///	UINT32 tile_size,
/*TODO*///	const UINT8 *pPenData,
/*TODO*///	const UINT16 *clut,
/*TODO*///	int transparent_color,
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
/*TODO*///				if( clut[data&0xf]!=transparent_color )
/*TODO*///				{
/*TODO*///					mask->line[y0+yx/MAX_TILESIZE][(x0+(yx%MAX_TILESIZE))/8] |= 0x80>>(yx%8);
/*TODO*///				}
/*TODO*///				yx = *pPenToPixel++;
/*TODO*///				if( clut[data>>4]!=transparent_color )
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
/*TODO*///				if( clut[data]!=transparent_color )
/*TODO*///				{
/*TODO*///					mask->line[y0+yx/MAX_TILESIZE][(x0+(yx%MAX_TILESIZE))/8] |= 0x80>>(yx%8);
/*TODO*///				}
/*TODO*///			}
/*TODO*///			pPenData += pitch;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
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
/*TODO*///
/*TODO*///static void draw_mask(
/*TODO*///	struct tilemap *tilemap,
/*TODO*///	struct osd_bitmap *mask,
/*TODO*///	UINT32 x0, UINT32 y0,
/*TODO*///	UINT32 tile_size,
/*TODO*///	const UINT8 *pPenData,
/*TODO*///	UINT32 transmask,
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
/*TODO*///				if( !((1<<(data&0xf))&transmask) )
/*TODO*///				{
/*TODO*///					mask->line[y0+yx/MAX_TILESIZE][(x0+(yx%MAX_TILESIZE))/8] |= 0x80>>(yx%8);
/*TODO*///				}
/*TODO*///				yx = *pPenToPixel++;
/*TODO*///				if( !((1<<(data>>4))&transmask) )
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
/*TODO*///				if( !((1<<data)&transmask) )
/*TODO*///				{
/*TODO*///					mask->line[y0+yx/MAX_TILESIZE][(x0+(yx%MAX_TILESIZE))/8] |= 0x80>>(yx%8);
/*TODO*///				}
/*TODO*///			}
/*TODO*///			pPenData += pitch;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void ClearMask( struct osd_bitmap *bitmap, int tile_size, int x0, int y0 )
/*TODO*///{
/*TODO*///	UINT8 *pDest;
/*TODO*///	int ty,tx;
/*TODO*///	for( ty=0; ty<tile_size; ty++ )
/*TODO*///	{
/*TODO*///		pDest = bitmap->line[y0+ty]+x0/8;
/*TODO*///		for( tx=tile_size/8; tx!=0; tx-- )
/*TODO*///		{
/*TODO*///			*pDest++ = 0x00;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static int InspectMask( struct osd_bitmap *bitmap, int tile_size, int x0, int y0 )
/*TODO*///{
/*TODO*///	const UINT8 *pSource;
/*TODO*///	int ty,tx;
/*TODO*///
/*TODO*///	switch( bitmap->line[y0][x0/8] )
/*TODO*///	{
/*TODO*///	case 0xff: /* possibly opaque */
/*TODO*///		for( ty=0; ty<tile_size; ty++ )
/*TODO*///		{
/*TODO*///			pSource = bitmap->line[y0+ty]+x0/8;
/*TODO*///			for( tx=tile_size/8; tx!=0; tx-- )
/*TODO*///			{
/*TODO*///				if( *pSource++ != 0xff ) return TILE_MASKED;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		return TILE_OPAQUE;
/*TODO*///
/*TODO*///	case 0x00: /* possibly transparent */
/*TODO*///		for( ty=0; ty<tile_size; ty++ )
/*TODO*///		{
/*TODO*///			pSource = bitmap->line[y0+ty]+x0/8;
/*TODO*///			for( tx=tile_size/8; tx!=0; tx-- )
/*TODO*///			{
/*TODO*///				if( *pSource++ != 0x00 ) return TILE_MASKED;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		return TILE_TRANSPARENT;
/*TODO*///
/*TODO*///	default:
/*TODO*///		return TILE_MASKED;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void render_mask( struct tilemap *tilemap, UINT32 cached_indx )
/*TODO*///{
/*TODO*///	const struct cached_tile_info *cached_tile_info = &tilemap->cached_tile_info[cached_indx];
/*TODO*///	UINT32 col = cached_indx%tilemap->num_cached_cols;
/*TODO*///	UINT32 row = cached_indx/tilemap->num_cached_cols;
/*TODO*///	UINT32 type = tilemap->type;
/*TODO*///	UINT32 tile_size = tilemap->tile_size;
/*TODO*///	UINT32 y0 = tile_size*row;
/*TODO*///	UINT32 x0 = tile_size*col;
/*TODO*///	int pitch = tile_size + cached_tile_info->skip;
/*TODO*///	UINT32 pen_usage = cached_tile_info->pen_usage;
/*TODO*///	const UINT8 *pen_data = cached_tile_info->pen_data;
/*TODO*///	UINT32 flags = cached_tile_info->flags;
/*TODO*///
/*TODO*///	if( type & TILEMAP_BITMASK )
/*TODO*///	{
/*TODO*///		/* hack; games using TILEMAP_BITMASK may pass in NULL or (~0) to indicate
/*TODO*///		 * tiles that are wholly transparent or opaque.
/*TODO*///		 */
/*TODO*///		if( tile_info.mask_data == TILEMAP_BITMASK_TRANSPARENT )
/*TODO*///		{
/*TODO*///			tilemap->foreground->data_row[row][col] = TILE_TRANSPARENT;
/*TODO*///		}
/*TODO*///		else if( tile_info.mask_data == TILEMAP_BITMASK_OPAQUE )
/*TODO*///		{
/*TODO*///			tilemap->foreground->data_row[row][col] = TILE_OPAQUE;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			/* We still inspect the tile data, since not all games
/*TODO*///			 * using TILEMAP_BITMASK use the above hack.
/*TODO*///			 */
/*TODO*///			draw_bitmask( tilemap->foreground->bitmask,
/*TODO*///				x0, y0, tile_size, tile_info.mask_data, flags );
/*TODO*///
/*TODO*///			tilemap->foreground->data_row[row][col] =
/*TODO*///				InspectMask( tilemap->foreground->bitmask, tile_size, x0, y0 );
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else if( type & TILEMAP_SPLIT )
/*TODO*///	{
/*TODO*///		UINT32 fgmask = tilemap->fgmask[(flags>>TILE_SPLIT_OFFSET)&3];
/*TODO*///		UINT32 bgmask = tilemap->bgmask[(flags>>TILE_SPLIT_OFFSET)&3];
/*TODO*///
/*TODO*///		if( (pen_usage & fgmask)==0 || (flags&TILE_IGNORE_TRANSPARENCY) )
/*TODO*///		{ /* foreground totally opaque */
/*TODO*///			tilemap->foreground->data_row[row][col] = TILE_OPAQUE;
/*TODO*///		}
/*TODO*///		else if( (pen_usage & ~fgmask)==0 )
/*TODO*///		{ /* foreground transparent */
/*TODO*///			ClearMask( tilemap->background->bitmask, tile_size, x0, y0 );
/*TODO*///			draw_mask( tilemap,tilemap->background->bitmask,
/*TODO*///				x0, y0, tile_size, pen_data, bgmask, flags, pitch );
/*TODO*///			tilemap->foreground->data_row[row][col] = TILE_TRANSPARENT;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{ /* masked tile */
/*TODO*///			ClearMask( tilemap->foreground->bitmask, tile_size, x0, y0 );
/*TODO*///			draw_mask( tilemap,tilemap->foreground->bitmask,
/*TODO*///				x0, y0, tile_size, pen_data, fgmask, flags, pitch );
/*TODO*///			tilemap->foreground->data_row[row][col] = TILE_MASKED;
/*TODO*///		}
/*TODO*///
/*TODO*///		if( (pen_usage & bgmask)==0 || (flags&TILE_IGNORE_TRANSPARENCY) )
/*TODO*///		{ /* background totally opaque */
/*TODO*///			tilemap->background->data_row[row][col] = TILE_OPAQUE;
/*TODO*///		}
/*TODO*///		else if( (pen_usage & ~bgmask)==0 )
/*TODO*///		{ /* background transparent */
/*TODO*///			ClearMask( tilemap->foreground->bitmask, tile_size, x0, y0 );
/*TODO*///			draw_mask( tilemap,tilemap->foreground->bitmask,
/*TODO*///				x0, y0, tile_size, pen_data, fgmask, flags, pitch );
/*TODO*///				tilemap->foreground->data_row[row][col] = TILE_MASKED;
/*TODO*///			tilemap->background->data_row[row][col] = TILE_TRANSPARENT;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{ /* masked tile */
/*TODO*///			ClearMask( tilemap->background->bitmask, tile_size, x0, y0 );
/*TODO*///			draw_mask( tilemap,tilemap->background->bitmask,
/*TODO*///				x0, y0, tile_size, pen_data, bgmask, flags, pitch );
/*TODO*///			tilemap->background->data_row[row][col] = TILE_MASKED;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else if( type==TILEMAP_TRANSPARENT )
/*TODO*///	{
/*TODO*///		if( pen_usage )
/*TODO*///		{
/*TODO*///			UINT32 fgmask = 1 << tilemap->transparent_pen;
/*TODO*///		 	if( flags&TILE_IGNORE_TRANSPARENCY ) fgmask = 0;
/*TODO*///			if( pen_usage == fgmask )
/*TODO*///			{
/*TODO*///				tilemap->foreground->data_row[row][col] = TILE_TRANSPARENT;
/*TODO*///			}
/*TODO*///			else if( pen_usage & fgmask )
/*TODO*///			{
/*TODO*///				ClearMask( tilemap->foreground->bitmask, tile_size, x0, y0 );
/*TODO*///				draw_mask( tilemap,tilemap->foreground->bitmask,
/*TODO*///					x0, y0, tile_size, pen_data, fgmask, flags, pitch );
/*TODO*///				tilemap->foreground->data_row[row][col] = TILE_MASKED;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				tilemap->foreground->data_row[row][col] = TILE_OPAQUE;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			ClearMask( tilemap->foreground->bitmask, tile_size, x0, y0 );
/*TODO*///			draw_pen_mask(
/*TODO*///					tilemap,tilemap->foreground->bitmask,
/*TODO*///					x0, y0, tile_size, pen_data, tilemap->transparent_pen, flags, pitch );
/*TODO*///			tilemap->foreground->data_row[row][col] =
/*TODO*///				InspectMask( tilemap->foreground->bitmask, tile_size, x0, y0 );
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else if( type==TILEMAP_TRANSPARENT_COLOR )
/*TODO*///	{
/*TODO*///		ClearMask( tilemap->foreground->bitmask, tile_size, x0, y0 );
/*TODO*///
/*TODO*///		draw_color_mask(
/*TODO*///				tilemap,tilemap->foreground->bitmask,
/*TODO*///				x0, y0, tile_size, pen_data,
/*TODO*///				Machine->game_colortable + (cached_tile_info->pal_data - Machine->remapped_colortable),
/*TODO*///				tilemap->transparent_pen, flags, pitch );
/*TODO*///
/*TODO*///		tilemap->foreground->data_row[row][col] =
/*TODO*///				InspectMask( tilemap->foreground->bitmask, tile_size, x0, y0 );
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		tilemap->foreground->data_row[row][col] = TILE_OPAQUE;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void update_tile_info( struct tilemap *tilemap )
/*TODO*///{
/*TODO*///	int *logical_flip_to_cached_flip = tilemap->logical_flip_to_cached_flip;
/*TODO*///	UINT32 num_pens = tilemap->tile_size * tilemap->tile_size;
/*TODO*///	UINT32 num_tiles = tilemap->num_tiles;
/*TODO*///	UINT32 cached_indx;
/*TODO*///	UINT8 *visible = tilemap->visible;
/*TODO*///	UINT8 *dirty_vram = tilemap->dirty_vram;
/*TODO*///	UINT8 *dirty_pixels = tilemap->dirty_pixels;
/*TODO*///
/*TODO*///	memset( &tile_info, 0x00, sizeof(tile_info) ); /* initialize defaults */
/*TODO*///
/*TODO*///	for( cached_indx=0; cached_indx<num_tiles; cached_indx++ )
/*TODO*///	{
/*TODO*///		if( visible[cached_indx] && dirty_vram[cached_indx] )
/*TODO*///		{
/*TODO*///			struct cached_tile_info *cached_tile_info = &tilemap->cached_tile_info[cached_indx];
/*TODO*///			UINT32 memory_offset = tilemap->cached_indx_to_memory_offset[cached_indx];
/*TODO*///			unregister_pens( cached_tile_info, num_pens );
/*TODO*///			tilemap->tile_get_info( memory_offset );
/*TODO*///			{
/*TODO*///				UINT32 flags = tile_info.flags;
/*TODO*///				cached_tile_info->flags = (flags&0xfc)|logical_flip_to_cached_flip[flags&0x3];
/*TODO*///			}
/*TODO*///			cached_tile_info->pen_usage = tile_info.pen_usage;
/*TODO*///			cached_tile_info->pen_data = tile_info.pen_data;
/*TODO*///			cached_tile_info->pal_data = tile_info.pal_data;
/*TODO*///			cached_tile_info->skip = tile_info.skip;
/*TODO*///			tilemap->priority[cached_indx] = tile_info.priority;
/*TODO*///			register_pens( cached_tile_info, num_pens );
/*TODO*///			dirty_pixels[cached_indx] = 1;
/*TODO*///			dirty_vram[cached_indx] = 0;
/*TODO*///			render_mask( tilemap, cached_indx );
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void update_visible( struct tilemap *tilemap )
/*TODO*///{
/*TODO*///	// visibility marking is not currently implemented
/*TODO*///	memset( tilemap->visible, 1, tilemap->num_tiles );
/*TODO*///}
/*TODO*///
/*TODO*///void tilemap_update( struct tilemap *tilemap )
/*TODO*///{
/*TODO*///profiler_mark(PROFILER_TILEMAP_UPDATE);
/*TODO*///	if( tilemap==ALL_TILEMAPS )
/*TODO*///	{
/*TODO*///		tilemap = first_tilemap;
/*TODO*///		while( tilemap )
/*TODO*///		{
/*TODO*///			tilemap_update( tilemap );
/*TODO*///			tilemap = tilemap->next;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else if( tilemap->enable )
/*TODO*///	{
/*TODO*///		tilemap->bNeedRender = 1;
/*TODO*///		update_visible( tilemap );
/*TODO*///		update_tile_info( tilemap );
/*TODO*///	}
/*TODO*///profiler_mark(PROFILER_END);
/*TODO*///}
/*TODO*///
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
/*TODO*///
/*TODO*///void tilemap_set_scrollx( struct tilemap *tilemap, int which, int value )
/*TODO*///{
/*TODO*///	value = tilemap->scrollx_delta-value;
/*TODO*///
/*TODO*///	if( tilemap->orientation & ORIENTATION_SWAP_XY )
/*TODO*///	{
/*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_X ) which = tilemap->scroll_cols-1 - which;
/*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_Y ) value = screen_height-tilemap->cached_height-value;
/*TODO*///		if( tilemap->colscroll[which]!=value )
/*TODO*///		{
/*TODO*///			tilemap->colscroll[which] = value;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_Y ) which = tilemap->scroll_rows-1 - which;
/*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_X ) value = screen_width-tilemap->cached_width-value;
/*TODO*///		if( tilemap->rowscroll[which]!=value )
/*TODO*///		{
/*TODO*///			tilemap->rowscroll[which] = value;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void tilemap_set_scrolly( struct tilemap *tilemap, int which, int value )
/*TODO*///{
/*TODO*///	value = tilemap->scrolly_delta - value;
/*TODO*///
/*TODO*///	if( tilemap->orientation & ORIENTATION_SWAP_XY )
/*TODO*///	{
/*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_Y ) which = tilemap->scroll_rows-1 - which;
/*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_X ) value = screen_width-tilemap->cached_width-value;
/*TODO*///		if( tilemap->rowscroll[which]!=value )
/*TODO*///		{
/*TODO*///			tilemap->rowscroll[which] = value;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_X ) which = tilemap->scroll_cols-1 - which;
/*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_Y ) value = screen_height-tilemap->cached_height-value;
/*TODO*///		if( tilemap->colscroll[which]!=value )
/*TODO*///		{
/*TODO*///			tilemap->colscroll[which] = value;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*////***********************************************************************************/
/*TODO*///
/*TODO*///void tilemap_draw( struct osd_bitmap *dest, struct tilemap *tilemap, UINT32 flags, UINT32 priority )
/*TODO*///{
/*TODO*///	int xpos,ypos;
/*TODO*///profiler_mark(PROFILER_TILEMAP_DRAW);
/*TODO*///	tmap_render( tilemap );
/*TODO*///
/*TODO*///	if( tilemap->enable )
/*TODO*///	{
/*TODO*///		void (*draw)( int, int );
/*TODO*///
/*TODO*///		int rows = tilemap->scroll_rows;
/*TODO*///		const int *rowscroll = tilemap->rowscroll;
/*TODO*///		int cols = tilemap->scroll_cols;
/*TODO*///		const int *colscroll = tilemap->colscroll;
/*TODO*///
/*TODO*///		int left = tilemap->clip_left;
/*TODO*///		int right = tilemap->clip_right;
/*TODO*///		int top = tilemap->clip_top;
/*TODO*///		int bottom = tilemap->clip_bottom;
/*TODO*///
/*TODO*///		int tile_size = tilemap->tile_size;
/*TODO*///
/*TODO*///		blit.screen = dest;
/*TODO*///		if( dest )
/*TODO*///		{
/*TODO*///			blit.pixmap = tilemap->pixmap;
/*TODO*///			blit.source_line_offset = tilemap->pixmap_line_offset;
/*TODO*///
/*TODO*///			blit.dest_line_offset = dest->line[1] - dest->line[0];
/*TODO*///
/*TODO*///			switch( dest->depth )
/*TODO*///			{
/*TODO*///			case 15:
/*TODO*///			case 16:
/*TODO*///				blit.dest_line_offset /= 2;
/*TODO*///				blit.source_line_offset /= 2;
/*TODO*///				break;
/*TODO*///			case 32:
/*TODO*///				blit.dest_line_offset /= 4;
/*TODO*///				blit.source_line_offset /= 4;
/*TODO*///				break;
/*TODO*///			}
/*TODO*///			blit.dest_row_offset = tile_size*blit.dest_line_offset;
/*TODO*///		}
/*TODO*///
/*TODO*///
/*TODO*///		if( tilemap->type==TILEMAP_OPAQUE || (flags&TILEMAP_IGNORE_TRANSPARENCY) )
/*TODO*///		{
/*TODO*///			draw = tilemap->draw_opaque;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if( flags & TILEMAP_ALPHA )
/*TODO*///				draw = tilemap->draw_alpha;
/*TODO*///			else
/*TODO*///				draw = tilemap->draw;
/*TODO*///
/*TODO*///			if( flags&TILEMAP_BACK )
/*TODO*///			{
/*TODO*///				blit.bitmask = tilemap->background->bitmask;
/*TODO*///				blit.mask_line_offset = tilemap->background->line_offset;
/*TODO*///				blit.mask_data_row = tilemap->background->data_row;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				blit.bitmask = tilemap->foreground->bitmask;
/*TODO*///				blit.mask_line_offset = tilemap->foreground->line_offset;
/*TODO*///				blit.mask_data_row = tilemap->foreground->data_row;
/*TODO*///			}
/*TODO*///
/*TODO*///			blit.mask_row_offset = tile_size*blit.mask_line_offset;
/*TODO*///		}
/*TODO*///
/*TODO*///		blit.source_row_offset = tile_size*blit.source_line_offset;
/*TODO*///
/*TODO*///		blit.priority_data_row = tilemap->priority_row;
/*TODO*///		blit.source_width = tilemap->cached_width;
/*TODO*///		blit.source_height = tilemap->cached_height;
/*TODO*///		blit.tile_priority = flags&0xf;
/*TODO*///		blit.tilemap_priority_code = priority;
/*TODO*///
/*TODO*///		if( rows == 1 && cols == 1 )
/*TODO*///		{ /* XY scrolling playfield */
/*TODO*///			int scrollx = rowscroll[0];
/*TODO*///			int scrolly = colscroll[0];
/*TODO*///
/*TODO*///			if( scrollx < 0 )
/*TODO*///			{
/*TODO*///				scrollx = blit.source_width - (-scrollx) % blit.source_width;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				scrollx = scrollx % blit.source_width;
/*TODO*///			}
/*TODO*///
/*TODO*///			if( scrolly < 0 )
/*TODO*///			{
/*TODO*///				scrolly = blit.source_height - (-scrolly) % blit.source_height;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				scrolly = scrolly % blit.source_height;
/*TODO*///			}
/*TODO*///
/*TODO*///	 		blit.clip_left = left;
/*TODO*///	 		blit.clip_top = top;
/*TODO*///	 		blit.clip_right = right;
/*TODO*///	 		blit.clip_bottom = bottom;
/*TODO*///
/*TODO*///			for(
/*TODO*///				ypos = scrolly - blit.source_height;
/*TODO*///				ypos < blit.clip_bottom;
/*TODO*///				ypos += blit.source_height )
/*TODO*///			{
/*TODO*///				for(
/*TODO*///					xpos = scrollx - blit.source_width;
/*TODO*///					xpos < blit.clip_right;
/*TODO*///					xpos += blit.source_width )
/*TODO*///				{
/*TODO*///					draw( xpos,ypos );
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else if( rows == 1 )
/*TODO*///		{ /* scrolling columns + horizontal scroll */
/*TODO*///			int col = 0;
/*TODO*///			int colwidth = blit.source_width / cols;
/*TODO*///			int scrollx = rowscroll[0];
/*TODO*///
/*TODO*///			if( scrollx < 0 )
/*TODO*///			{
/*TODO*///				scrollx = blit.source_width - (-scrollx) % blit.source_width;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				scrollx = scrollx % blit.source_width;
/*TODO*///			}
/*TODO*///
/*TODO*///			blit.clip_top = top;
/*TODO*///			blit.clip_bottom = bottom;
/*TODO*///
/*TODO*///			while( col < cols )
/*TODO*///			{
/*TODO*///				int cons = 1;
/*TODO*///				int scrolly = colscroll[col];
/*TODO*///
/*TODO*///	 			/* count consecutive columns scrolled by the same amount */
/*TODO*///				if( scrolly != TILE_LINE_DISABLED )
/*TODO*///				{
/*TODO*///					while( col + cons < cols &&	colscroll[col + cons] == scrolly ) cons++;
/*TODO*///
/*TODO*///					if( scrolly < 0 )
/*TODO*///					{
/*TODO*///						scrolly = blit.source_height - (-scrolly) % blit.source_height;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						scrolly %= blit.source_height;
/*TODO*///					}
/*TODO*///
/*TODO*///					blit.clip_left = col * colwidth + scrollx;
/*TODO*///					if (blit.clip_left < left) blit.clip_left = left;
/*TODO*///					blit.clip_right = (col + cons) * colwidth + scrollx;
/*TODO*///					if (blit.clip_right > right) blit.clip_right = right;
/*TODO*///
/*TODO*///					for(
/*TODO*///						ypos = scrolly - blit.source_height;
/*TODO*///						ypos < blit.clip_bottom;
/*TODO*///						ypos += blit.source_height )
/*TODO*///					{
/*TODO*///						draw( scrollx,ypos );
/*TODO*///					}
/*TODO*///
/*TODO*///					blit.clip_left = col * colwidth + scrollx - blit.source_width;
/*TODO*///					if (blit.clip_left < left) blit.clip_left = left;
/*TODO*///					blit.clip_right = (col + cons) * colwidth + scrollx - blit.source_width;
/*TODO*///					if (blit.clip_right > right) blit.clip_right = right;
/*TODO*///
/*TODO*///					for(
/*TODO*///						ypos = scrolly - blit.source_height;
/*TODO*///						ypos < blit.clip_bottom;
/*TODO*///						ypos += blit.source_height )
/*TODO*///					{
/*TODO*///						draw( scrollx - blit.source_width,ypos );
/*TODO*///					}
/*TODO*///				}
/*TODO*///				col += cons;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else if( cols == 1 )
/*TODO*///		{ /* scrolling rows + vertical scroll */
/*TODO*///			int row = 0;
/*TODO*///			int rowheight = blit.source_height / rows;
/*TODO*///			int scrolly = colscroll[0];
/*TODO*///			if( scrolly < 0 )
/*TODO*///			{
/*TODO*///				scrolly = blit.source_height - (-scrolly) % blit.source_height;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				scrolly = scrolly % blit.source_height;
/*TODO*///			}
/*TODO*///			blit.clip_left = left;
/*TODO*///			blit.clip_right = right;
/*TODO*///			while( row < rows )
/*TODO*///			{
/*TODO*///				int cons = 1;
/*TODO*///				int scrollx = rowscroll[row];
/*TODO*///				/* count consecutive rows scrolled by the same amount */
/*TODO*///				if( scrollx != TILE_LINE_DISABLED )
/*TODO*///				{
/*TODO*///					while( row + cons < rows &&	rowscroll[row + cons] == scrollx ) cons++;
/*TODO*///					if( scrollx < 0)
/*TODO*///					{
/*TODO*///						scrollx = blit.source_width - (-scrollx) % blit.source_width;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						scrollx %= blit.source_width;
/*TODO*///					}
/*TODO*///					blit.clip_top = row * rowheight + scrolly;
/*TODO*///					if (blit.clip_top < top) blit.clip_top = top;
/*TODO*///					blit.clip_bottom = (row + cons) * rowheight + scrolly;
/*TODO*///					if (blit.clip_bottom > bottom) blit.clip_bottom = bottom;
/*TODO*///					for(
/*TODO*///						xpos = scrollx - blit.source_width;
/*TODO*///						xpos < blit.clip_right;
/*TODO*///						xpos += blit.source_width )
/*TODO*///					{
/*TODO*///						draw( xpos,scrolly );
/*TODO*///					}
/*TODO*///					blit.clip_top = row * rowheight + scrolly - blit.source_height;
/*TODO*///					if (blit.clip_top < top) blit.clip_top = top;
/*TODO*///					blit.clip_bottom = (row + cons) * rowheight + scrolly - blit.source_height;
/*TODO*///					if (blit.clip_bottom > bottom) blit.clip_bottom = bottom;
/*TODO*///					for(
/*TODO*///						xpos = scrollx - blit.source_width;
/*TODO*///						xpos < blit.clip_right;
/*TODO*///						xpos += blit.source_width )
/*TODO*///					{
/*TODO*///						draw( xpos,scrolly - blit.source_height );
/*TODO*///					}
/*TODO*///				}
/*TODO*///				row += cons;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///profiler_mark(PROFILER_END);
/*TODO*///}
/*TODO*///
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
}
