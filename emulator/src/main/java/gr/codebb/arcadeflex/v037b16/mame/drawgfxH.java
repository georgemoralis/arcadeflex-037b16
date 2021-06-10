/*
 * ported to v0.37b16
 * 
 */
package gr.codebb.arcadeflex.v037b16.mame;

//mame imports
import gr.codebb.arcadeflex.v037b16.mame.osdependH.*;
//to be organized
import common.ptr.UBytePtr;
import common.subArrays.IntArray;
import static common.util.*;
import java.util.Arrays;


public class drawgfxH {

    public static final int MAX_GFX_PLANES = 8;
    public static final int MAX_GFX_SIZE = 64;

    public static int RGN_FRAC(int num, int den) {
        return (0x80000000 | (((num) & 0x0f) << 27) | (((den) & 0x0f) << 23));
    }

    public static int IS_FRAC(int offset) {
        return ((offset) & 0x80000000);
    }

    public static int FRAC_NUM(int offset) {
        return (((offset) >>> 27) & 0x0f);
    }

    public static int FRAC_DEN(int offset) {
        return (((offset) >>> 23) & 0x0f);
    }

    public static int FRAC_OFFSET(int offset) {
        return ((offset) & 0x007fffff);
    }

    public static int[] STEP4(int START, int STEP) {
        return new int[]{(START), (START) + 1 * (STEP), (START) + 2 * (STEP), (START) + 3 * (STEP)};
    }

    public static int[] STEP8(int START, int STEP) {
        return combineIntArrays(STEP4(START, STEP), STEP4((START) + 4 * (STEP), STEP));
    }

    public static int[] STEP16(int START, int STEP) {
        return combineIntArrays(STEP8(START, STEP), STEP8((START) + 8 * (STEP), STEP));
    }

    public static class GfxLayout {

        public GfxLayout() {
        }

        public GfxLayout(int width, int height, int total, int planes, int planeoffset[], int xoffset[], int yoffset[], int charincrement) {
            this.width = width;
            this.height = height;
            this.total = total;
            this.planes = planes;
            this.planeoffset = planeoffset;
            this.xoffset = xoffset;
            this.yoffset = yoffset;
            this.charincrement = charincrement;
        }

        public GfxLayout(GfxLayout c) {
            width = c.width;
            height = c.height;
            total = c.total;
            planes = c.planes;
            planeoffset = Arrays.copyOf(c.planeoffset, c.planeoffset.length);
            xoffset = Arrays.copyOf(c.xoffset, c.xoffset.length);
            yoffset = Arrays.copyOf(c.yoffset, c.yoffset.length);
            charincrement = c.charincrement;
        }

        public /*UNINT16*/ int width, height;/* width and height of chars/sprites */
        public /*UNINT32*/ int total;/* total numer of chars/sprites in the rom */
        public /*UNINT16*/ int planes;/* number of bitplanes */
        public /*UNINT32*/ int planeoffset[];/* start of every bitplane */
        public /*UNINT32*/ int xoffset[];/* coordinates of the bit corresponding to the pixel */
        public /*UNINT32*/ int yoffset[];/* of the given coordinates */
        public /*UNINT16*/ int charincrement;/* distance between two consecutive characters/sprites */
    }

    public static int GFX_RAW = 0x12345678;

    public static class GfxElement {

        public int width, height;
        public /*unsigned */ int total_elements;/* total number of characters/sprites */
        public int color_granularity;/* number of colors for each color code (for example, 4 for 2 bitplanes gfx) */
        public IntArray colortable;/* map color codes to screen pens */ /* if this is 0, the function does a verbatim copy */
        public int total_colors;
        public /*unsigned */ int[] pen_usage;/* an array of total_elements ints. */
        public UBytePtr gfxdata;/* pixel data */
        public int line_modulo;/* amount to add to get to the next line (usually = width) */
        public int char_modulo;/* = line_modulo * height */
        public int flags;
    }

    public static final int GFX_PACKED = 1;/* two 4bpp pixels are packed in one byte of gfxdata */
    public static final int GFX_SWAPXY = 2;/* characters are mirrored along the top-left/bottom-right diagonal */
    public static final int GFX_DONT_FREE_GFXDATA = 4;/* gfxdata was not malloc()ed, so don't free it on exit */

    public static class GfxDecodeInfo {

        public GfxDecodeInfo(int mr, int s, GfxLayout g, int ccs, int tcc) {
            memory_region = mr;
            start = s;
            if (g != null) {
                gfxlayout = new GfxLayout(g);
            } else {
                gfxlayout = null;
            }
            color_codes_start = ccs;
            total_color_codes = tcc;
        }

        public GfxDecodeInfo(int s, GfxLayout g, int ccs, int tcc) {
            start = s;
            if (g != null) {
                gfxlayout = new GfxLayout(g);
            } else {
                gfxlayout = null;
            }
            color_codes_start = ccs;
            total_color_codes = tcc;
        }

        public GfxDecodeInfo(int s) {
            this(s, s, null, 0, 0);
        }

        public int memory_region;/* memory region where the data resides (usually 1)  -1 marks the end of the array */
        public int start;/* beginning of data data to decode (offset in RAM[]) */
        public GfxLayout gfxlayout;
        public int color_codes_start;/* offset in the color lookup table where color codes start */
        public int total_color_codes;/* total number of color codes */
    }

    public static class rectangle {

        public rectangle() {
        }

        public rectangle(int min_x, int max_x, int min_y, int max_y) {
            this.min_x = min_x;
            this.max_x = max_x;
            this.min_y = min_y;
            this.max_y = max_y;
        }

        public rectangle(rectangle rec) {
            min_x = rec.min_x;
            max_x = rec.max_x;
            min_y = rec.min_y;
            max_y = rec.max_y;
        }

        public int min_x, max_x;
        public int min_y, max_y;
    }
    /*TODO*///
/*TODO*///struct _alpha_cache {
/*TODO*///	const UINT8 *alphas;
/*TODO*///	const UINT8 *alphad;
/*TODO*///	UINT8 alpha[0x101][0x100];
/*TODO*///};
/*TODO*///
    public static final int TRANSPARENCY_NONE = 0;/* opaque with remapping */
    public static final int TRANSPARENCY_NONE_RAW = 1;/* opaque with no remapping */
    public static final int TRANSPARENCY_PEN = 2;/* single pen transparency with remapping */
    public static final int TRANSPARENCY_PEN_RAW = 3;/* single pen transparency with no remapping */
    public static final int TRANSPARENCY_PENS = 4;/* multiple pen transparency with remapping */
    public static final int TRANSPARENCY_PENS_RAW = 5;/* multiple pen transparency with no remapping */
    public static final int TRANSPARENCY_COLOR = 6;/* single remapped pen transparency with remapping */
    public static final int TRANSPARENCY_PEN_TABLE = 7;/* special pen remapping modes (see DRAWMODE_xxx below) with remapping */
    public static final int TRANSPARENCY_PEN_TABLE_RAW = 8;/* special pen remapping modes (see DRAWMODE_xxx below) with no remapping */
    public static final int TRANSPARENCY_BLEND = 9;/* blend two bitmaps, shifting the source and ORing to the dest with remapping */
    public static final int TRANSPARENCY_BLEND_RAW = 10;/* blend two bitmaps, shifting the source and ORing to the dest with no remapping */
    public static final int TRANSPARENCY_ALPHAONE = 11;/* single pen transparency, single pen alpha */
    public static final int TRANSPARENCY_ALPHA = 12;/* single pen transparency, other pens alpha */

    public static final int TRANSPARENCY_MODES = 13;/* total number of modes; must be last */

 /* drawing mode case TRANSPARENCY_PEN_TABLE */
    public static final int DRAWMODE_NONE = 0;
    public static final int DRAWMODE_SOURCE = 1;
    public static final int DRAWMODE_SHADOW = 2;

    public static abstract interface plot_pixel_procPtr {

        public abstract void handler(osd_bitmap bitmap, int x, int y, int pen);
    }

    public static abstract interface read_pixel_procPtr {

        public abstract int handler(osd_bitmap bitmap, int x, int y);
    }

    public static abstract interface plot_box_procPtr {

        public abstract void handler(osd_bitmap bitmap, int x, int y, int width, int height, int pen);
    }

    public static abstract interface mark_dirty_procPtr {

        public abstract void handler(int sx, int sy, int ex, int ey);
    }

    /*TODO*///
/*TODO*///INLINE void alpha_set_level(int level) {
/*TODO*///	if(level == 0)
/*TODO*///		level = -1;
/*TODO*///	alpha_cache.alphas = alpha_cache.alpha[level+1];
/*TODO*///	alpha_cache.alphad = alpha_cache.alpha[255-level];
/*TODO*///}
/*TODO*///
/*TODO*///INLINE UINT32 alpha_blend16( UINT32 d, UINT32 s )
/*TODO*///{
/*TODO*///	const UINT8 *alphas = alpha_cache.alphas;
/*TODO*///	const UINT8 *alphad = alpha_cache.alphad;
/*TODO*///	return (alphas[s & 0x1f] | (alphas[(s>>5) & 0x1f] << 5) | (alphas[(s>>10) & 0x1f] << 10))
/*TODO*///		+ (alphad[d & 0x1f] | (alphad[(d>>5) & 0x1f] << 5) | (alphad[(d>>10) & 0x1f] << 10));
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE UINT32 alpha_blend32( UINT32 d, UINT32 s )
/*TODO*///{
/*TODO*///	const UINT8 *alphas = alpha_cache.alphas;
/*TODO*///	const UINT8 *alphad = alpha_cache.alphad;
/*TODO*///	return (alphas[s & 0xff] | (alphas[(s>>8) & 0xff] << 8) | (alphas[(s>>16) & 0xff] << 16))
/*TODO*///		+ (alphad[d & 0xff] | (alphad[(d>>8) & 0xff] << 8) | (alphad[(d>>16) & 0xff] << 16));
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///    
}
