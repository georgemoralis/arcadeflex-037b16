/**
 * ported to v0.37b16
 *
 */
package gr.codebb.arcadeflex.WIP.v037b16.mame;

import common.ptr.UBytePtr;
import common.subArrays.IntArray;
import gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapC.struct_tilemap;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapC.tile_info;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.GFX_PACKED;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.GFX_SWAPXY;
import gr.codebb.arcadeflex.v037b16.mame.drawgfxH.GfxElement;
import static mame037b16.mame.Machine;

public class tilemapH {

    public static abstract interface GetTileInfoPtr {

        public abstract void handler(int memory_offset);
    }

    public static abstract interface GetMemoryOffsetPtr {

        public abstract /*UINT32*/ int handler(int u32_col, int u32_row, int u32_num_cols, int u32_num_rows);
    }

    public static final struct_tilemap ALL_TILEMAPS = null;

    public static final int TILEMAP_OPAQUE = 0x00;
    public static final int TILEMAP_TRANSPARENT = 0x01;
    public static final int TILEMAP_SPLIT = 0x02;
    public static final int TILEMAP_BITMASK = 0x04;
    public static final int TILEMAP_TRANSPARENT_COLOR = 0x08;

    public static final int TILEMAP_IGNORE_TRANSPARENCY = 0x10;
    public static final int TILEMAP_BACK = 0x20;
    public static final int TILEMAP_FRONT = 0x40;
    public static final int TILEMAP_ALPHA = 0x80;

    public static final int TILEMAP_BITMASK_TRANSPARENT = (0);
    public static final int TILEMAP_BITMASK_OPAQUE = (~0);

    public static class struct_tile_info {

        /*
		you must set tile_info.pen_data, tile_info.pal_data and tile_info.pen_usage
		in the callback.  You can use the SET_TILE_INFO() macro below to do this.
		tile_info.flags and tile_info.priority will be automatically preset to 0,
		games that don't need them don't need to explicitly set them to 0
         */
        public UBytePtr pen_data;
        public IntArray pal_data;
        public int/*UINT32*/ tile_number;
        public int/*UINT32*/ pen_usage;
        public int/*UINT32*/ flags;
        public int/*UINT32*/ priority;
        public UBytePtr mask_data;
        public int skip;
    }

    public static void SET_TILE_INFO(int GFX, int CODE, int COLOR, int FLAGS) {
        GfxElement gfx = Machine.gfx[(GFX)];
        int _code = (CODE) % gfx.total_elements;
        tile_info.tile_number = _code;
        tile_info.pen_data = new UBytePtr(gfx.gfxdata, _code * gfx.char_modulo);
        tile_info.pal_data = new IntArray(gfx.colortable, gfx.color_granularity * (COLOR));
        tile_info.pen_usage = gfx.pen_usage != null ? gfx.pen_usage[_code] : 0;
        tile_info.flags = FLAGS;
        if ((gfx.flags & GFX_PACKED) != 0) {
            tile_info.flags |= TILE_4BPP;
        }
        if ((gfx.flags & GFX_SWAPXY) != 0) {
            tile_info.flags |= TILE_SWAPXY;
        }
    }

    public static final int TILE_FLIPX = 0x01;
    public static final int TILE_FLIPY = 0x02;
    public static final int TILE_SWAPXY = 0x04;
    public static final int TILE_IGNORE_TRANSPARENCY = 0x08;
    public static final int TILE_4BPP = 0x10;
    public static int TILE_SPLIT_OFFSET = 5;

    public static int TILE_SPLIT(int T) {
        return ((T) << TILE_SPLIT_OFFSET);
    }

    public static int TILE_FLIPYX(int YX) {
        return YX;
    }

    public static int TILE_FLIPXY(int XY) {
        return ((((XY) >>> 1) | ((XY) << 1)) & 3);
    }

    public static int TILE_LINE_DISABLED = 0x80000000;

    public static final int TILEMAP_FLIPX = 0x1;
    public static final int TILEMAP_FLIPY = 0x2;
}
