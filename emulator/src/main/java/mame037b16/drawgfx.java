/**
 * ported to v037b16
 */
package mame037b16;

import static gr.codebb.arcadeflex.v037b16.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b16.mame.usrintrf.*;
import static arcadeflex036.video.*;
import static common.libc.cstring.*;
import static common.libc.expressions.*;
import static common.ptr.*;
import common.subArrays.IntArray;
import common.subArrays.UShortArray;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapC.priority_bitmap;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.cpu_getcurrentframe;
import java.util.Arrays;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfx_modes8.blockmove_8toN_opaque8;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfx_modes8.blockmove_8toN_transcolor8;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfx_modes8.blockmove_8toN_transpen8;
import static mame037b16.mame.Machine;
import static gr.codebb.arcadeflex.v037b16.mame.osdependH.*;
import static mame037b7.palette.palette_shadow_table;

public class drawgfx {

    public static final int SHIFT0 = 0;
    public static final int SHIFT1 = 8;
    public static final int SHIFT2 = 16;
    public static final int SHIFT3 = 24;

    public static int[] gfx_drawmode_table = new int[256];
    public static plot_pixel_procPtr plot_pixel;
    public static read_pixel_procPtr read_pixel;
    public static plot_box_procPtr plot_box;
    public static mark_dirty_procPtr mark_dirty;

    static int[] is_raw = new int[TRANSPARENCY_MODES];

    /*TODO*///
/*TODO*///
/*TODO*///#ifdef ALIGN_INTS /* GSL 980108 read/write nonaligned dword routine for ARM processor etc */
/*TODO*///
/*TODO*///INLINE UINT32 read_dword(void *address)
/*TODO*///{
/*TODO*///	if ((long)address & 3)
/*TODO*///	{
/*TODO*///  		return	(*((UINT8 *)address  ) << SHIFT0) +
/*TODO*///				(*((UINT8 *)address+1) << SHIFT1) +
/*TODO*///				(*((UINT8 *)address+2) << SHIFT2) +
/*TODO*///				(*((UINT8 *)address+3) << SHIFT3);
/*TODO*///	}
/*TODO*///	else
/*TODO*///		return *(UINT32 *)address;
/*TODO*///}
/*TODO*///
/*TODO*///
    static void write_dword(UBytePtr address, int data) {
        address.write(0, data & 0xff);
        address.write(1, (data >> 8) & 0xff);
        address.write(2, (data >> 16) & 0xff);
        address.write(3, (data >> 24) & 0xff);
    }

    /*TODO*///#else
/*TODO*///#define read_dword(address) *(int *)address
/*TODO*///#define write_dword(address,data) *(int *)address=data
/*TODO*///#endif
/*TODO*///
/*TODO*///
/*TODO*///
    public static int readbit(UBytePtr src, int bitnum) {
        return src.read(bitnum / 8) & (0x80 >> (bitnum % 8));
    }

    /*TODO*///    public static _alpha_cache alpha_cache=new _alpha_cache();
    public static int alpha_active;

    /*TODO*///    public static void alpha_init()
/*TODO*///    {
/*TODO*///            int lev, _byte;
/*TODO*///            for(lev=0; lev<257; lev++)
/*TODO*///                    for(_byte=0; _byte<256; _byte++)
/*TODO*///                            alpha_cache.alpha[lev][_byte] = (_byte*lev) >> 8;
/*TODO*///            alpha_set_level(255);
/*TODO*///    }
    static void calc_penusage(GfxElement gfx, int num) {
        int x, y;
        UBytePtr dp;

        if (gfx.pen_usage == null) {
            return;
        }

        /* fill the pen_usage array with info on the used pens */
        gfx.pen_usage[num] = 0;

        dp = new UBytePtr(gfx.gfxdata, num * gfx.char_modulo);

        if ((gfx.flags & GFX_PACKED) != 0) {
            throw new UnsupportedOperationException("Unsupported");
            /*TODO*///		for (y = 0;y < gfx->height;y++)
/*TODO*///		{
/*TODO*///			for (x = 0;x < gfx->width/2;x++)
/*TODO*///			{
/*TODO*///				gfx->pen_usage[num] |= 1 << (dp[x] & 0x0f);
/*TODO*///				gfx->pen_usage[num] |= 1 << (dp[x] >> 4);
/*TODO*///			}
/*TODO*///			dp += gfx->line_modulo;
/*TODO*///		}
        } else {
            for (y = 0; y < gfx.height; y++) {
                for (x = 0; x < gfx.width; x++) {
                    gfx.pen_usage[num] |= 1 << dp.read(x);
                }
                dp.inc(gfx.line_modulo);
            }
        }
    }

    public static void decodechar(GfxElement gfx, int num, UBytePtr src, GfxLayout gl) {
        int plane, x, y;
        UBytePtr dp;
        int baseoffs;
        int[] xoffset;
        int[] yoffset;

        xoffset = Arrays.copyOf(gl.xoffset, gl.xoffset.length);
        yoffset = Arrays.copyOf(gl.yoffset, gl.yoffset.length);
        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
            int[] t = xoffset;
            xoffset = Arrays.copyOf(yoffset, yoffset.length);
            yoffset = Arrays.copyOf(t, t.length);
        }
        if ((gfx.flags & GFX_SWAPXY) != 0) {
            throw new UnsupportedOperationException("unsupported");
            /*TODO*///		const UINT32 *t = xoffset; xoffset = yoffset; yoffset = t;
        }

        dp = new UBytePtr(gfx.gfxdata, num * gfx.char_modulo);
        memset(dp, 0, gfx.char_modulo);

        baseoffs = num * gl.charincrement;

        if ((gfx.flags & GFX_PACKED) != 0) {
            throw new UnsupportedOperationException("unsupported");
            /*TODO*///		for (plane = 0;plane < gl->planes;plane++)
/*TODO*///		{
/*TODO*///			int shiftedbit = 1 << (gl->planes-1-plane);
/*TODO*///			int offs = baseoffs + gl->planeoffset[plane];
/*TODO*///
/*TODO*///			dp = gfx->gfxdata + num * gfx->char_modulo + (gfx->height-1) * gfx->line_modulo;
/*TODO*///
/*TODO*///			y = gfx->height;
/*TODO*///			while (--y >= 0)
/*TODO*///			{
/*TODO*///				int offs2 = offs + yoffset[y];
/*TODO*///
/*TODO*///				x = gfx->width/2;
/*TODO*///				while (--x >= 0)
/*TODO*///				{
/*TODO*///					if (readbit(src,offs2 + xoffset[2*x+1]))
/*TODO*///						dp[x] |= shiftedbit << 4;
/*TODO*///					if (readbit(src,offs2 + xoffset[2*x]))
/*TODO*///						dp[x] |= shiftedbit;
/*TODO*///				}
/*TODO*///				dp -= gfx->line_modulo;
/*TODO*///			}
/*TODO*///		}
        } else {
            for (plane = 0; plane < gl.planes; plane++) {
                int shiftedbit = 1 << (gl.planes - 1 - plane);
                int offs = baseoffs + gl.planeoffset[plane];

                dp = new UBytePtr(gfx.gfxdata, num * gfx.char_modulo + (gfx.height - 1) * gfx.line_modulo);
                y = gfx.height;
                while (--y >= 0) {
                    int offs2 = offs + yoffset[y];

                    x = gfx.width;
                    while (--x >= 0) {
                        if (readbit(src, offs2 + xoffset[x]) != 0) {
                            dp.or(x, shiftedbit);
                        }
                    }
                    dp.dec(gfx.line_modulo);
                }
            }
        }

        calc_penusage(gfx, num);
    }

    public static GfxElement decodegfx(UBytePtr src, GfxLayout gl) {
        int c;
        GfxElement gfx = new GfxElement();

        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
            gfx.width = gl.height;
            gfx.height = gl.width;
        } else {
            gfx.width = gl.width;
            gfx.height = gl.height;
        }

        gfx.total_elements = gl.total;
        gfx.color_granularity = 1 << gl.planes;

        gfx.pen_usage = null;
        /* need to make sure this is NULL if the next test fails) */
        if (gfx.color_granularity <= 32) /* can't handle more than 32 pens */ {
            gfx.pen_usage = new int[gfx.total_elements * 4];
        }
        /* no need to check for failure, the code can work without pen_usage */

        if (gl.planeoffset[0] == GFX_RAW) {
            throw new UnsupportedOperationException("Unsupported");
            /*TODO*///		if (gl->planes <= 4) gfx->flags |= GFX_PACKED;
/*TODO*///		if (Machine->orientation & ORIENTATION_SWAP_XY) gfx->flags |= GFX_SWAPXY;
/*TODO*///
/*TODO*///		gfx->line_modulo = gl->yoffset[0] / 8;
/*TODO*///		gfx->char_modulo = gl->charincrement / 8;
/*TODO*///
/*TODO*///		gfx->gfxdata = (UINT8 *)src + gl->xoffset[0] / 8;
/*TODO*///		gfx->flags |= GFX_DONT_FREE_GFXDATA;
/*TODO*///
/*TODO*///		for (c = 0;c < gfx->total_elements;c++)
/*TODO*///			calc_penusage(gfx,c);
        } else {
            if (false && gl.planes <= 4 && (gfx.width & 1) == 0) //if (gl->planes <= 4 && !(gfx->width & 1))
            {
                gfx.flags |= GFX_PACKED;
                gfx.line_modulo = gfx.width / 2;
            } else {
                gfx.line_modulo = gfx.width;
            }
            gfx.char_modulo = gfx.line_modulo * gfx.height;

            gfx.gfxdata = new UBytePtr(gfx.total_elements * gfx.char_modulo);

            for (c = 0; c < gfx.total_elements; c++) {
                decodechar(gfx, c, src, gl);
            }
        }

        return gfx;
    }

    public static void freegfx(GfxElement gfx) {
        if (gfx != null) {
            gfx.pen_usage = null;
            if ((gfx.flags & GFX_DONT_FREE_GFXDATA) == 0) {
                gfx.gfxdata = null;
            }
            gfx = null;
        }
    }

    public static void blockmove_NtoN_transpen_noremap8(UBytePtr srcdata, int srcwidth, int srcheight, int srcmodulo, UBytePtr dstdata, int dstmodulo, int transpen) {
        int end;//UINT8 *end;
        int trans4;
        IntPtr sd4;//UINT32 *sd4;

        srcmodulo -= srcwidth;
        dstmodulo -= srcwidth;
        trans4 = transpen * 0x01010101;

        while (srcheight != 0) {
            end = dstdata.offset + srcwidth;
            while (((long) srcdata.offset & 3) != 0 && dstdata.offset < end) /* longword align */ {
                int col;

                col = srcdata.readinc();
                if (col != transpen) {
                    dstdata.write(0, col);
                }
                dstdata.inc();
            }
            sd4 = new IntPtr(srcdata);
            while (dstdata.offset <= end - 4) {
                int/*UINT32*/ col4;

                if ((col4 = (sd4.read(0))) != trans4) {
                    /*UINT32*/
                    int xod4;

                    xod4 = col4 ^ trans4;
                    if ((xod4 & 0x000000ff) != 0 && (xod4 & 0x0000ff00) != 0
                            && (xod4 & 0x00ff0000) != 0 && (xod4 & 0xff000000) != 0) {
                        write_dword(dstdata, (int) col4);//write_dword((UINT32 *)dstdata,col4);
                    } else {
                        if ((xod4 & (0xff << SHIFT0)) != 0) {
                            dstdata.write(0, (col4 >> SHIFT0));
                        }
                        if ((xod4 & (0xff << SHIFT1)) != 0) {
                            dstdata.write(1, col4 >> SHIFT1);
                        }
                        if ((xod4 & (0xff << SHIFT2)) != 0) {
                            dstdata.write(2, col4 >> SHIFT2);
                        }
                        if ((xod4 & (0xff << SHIFT3)) != 0) {
                            dstdata.write(3, col4 >> SHIFT3);
                        }
                    }
                }
                sd4.base += 4;
                dstdata.offset += 4;
            }
            srcdata.set(sd4.readCA(), sd4.getBase());
            while (dstdata.offset < end) {
                int col;

                col = (srcdata.readinc());
                if (col != transpen) {
                    dstdata.write(0, col);
                }
                dstdata.inc();
            }

            srcdata.inc(srcmodulo);
            dstdata.inc(dstmodulo);
            srcheight--;
        }

    }

    /*TODO*///
/*TODO*///INLINE void blockmove_NtoN_transpen_noremap_flipx8(
/*TODO*///		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		UINT8 *dstdata,int dstmodulo,
/*TODO*///		int transpen)
/*TODO*///{
/*TODO*///	UINT8 *end;
/*TODO*///	int trans4;
/*TODO*///	UINT32 *sd4;
/*TODO*///
/*TODO*///	srcmodulo += srcwidth;
/*TODO*///	dstmodulo -= srcwidth;
/*TODO*///	//srcdata += srcwidth-1;
/*TODO*///	srcdata -= 3;
/*TODO*///
/*TODO*///	trans4 = transpen * 0x01010101;
/*TODO*///
/*TODO*///	while (srcheight)
/*TODO*///	{
/*TODO*///		end = dstdata + srcwidth;
/*TODO*///		while (((long)srcdata & 3) && dstdata < end)	/* longword align */
/*TODO*///		{
/*TODO*///			int col;
/*TODO*///
/*TODO*///			col = srcdata[3];
/*TODO*///			srcdata--;
/*TODO*///			if (col != transpen) *dstdata = col;
/*TODO*///			dstdata++;
/*TODO*///		}
/*TODO*///		sd4 = (UINT32 *)srcdata;
/*TODO*///		while (dstdata <= end - 4)
/*TODO*///		{
/*TODO*///			UINT32 col4;
/*TODO*///
/*TODO*///			if ((col4 = *(sd4--)) != trans4)
/*TODO*///			{
/*TODO*///				UINT32 xod4;
/*TODO*///
/*TODO*///				xod4 = col4 ^ trans4;
/*TODO*///				if (xod4 & (0xff<<SHIFT0)) dstdata[3] = (col4>>SHIFT0);
/*TODO*///				if (xod4 & (0xff<<SHIFT1)) dstdata[2] = (col4>>SHIFT1);
/*TODO*///				if (xod4 & (0xff<<SHIFT2)) dstdata[1] = (col4>>SHIFT2);
/*TODO*///				if (xod4 & (0xff<<SHIFT3)) dstdata[0] = (col4>>SHIFT3);
/*TODO*///			}
/*TODO*///			dstdata += 4;
/*TODO*///		}
/*TODO*///		srcdata = (UINT8 *)sd4;
/*TODO*///		while (dstdata < end)
/*TODO*///		{
/*TODO*///			int col;
/*TODO*///
/*TODO*///			col = srcdata[3];
/*TODO*///			srcdata--;
/*TODO*///			if (col != transpen) *dstdata = col;
/*TODO*///			dstdata++;
/*TODO*///		}
/*TODO*///
/*TODO*///		srcdata += srcmodulo;
/*TODO*///		dstdata += dstmodulo;
/*TODO*///		srcheight--;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void blockmove_NtoN_transpen_noremap16(
/*TODO*///		const UINT16 *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		UINT16 *dstdata,int dstmodulo,
/*TODO*///		int transpen)
/*TODO*///{
/*TODO*///	UINT16 *end;
/*TODO*///
/*TODO*///	srcmodulo -= srcwidth;
/*TODO*///	dstmodulo -= srcwidth;
/*TODO*///
/*TODO*///	while (srcheight)
/*TODO*///	{
/*TODO*///		end = dstdata + srcwidth;
/*TODO*///		while (dstdata < end)
/*TODO*///		{
/*TODO*///			int col;
/*TODO*///
/*TODO*///			col = *(srcdata++);
/*TODO*///			if (col != transpen) *dstdata = col;
/*TODO*///			dstdata++;
/*TODO*///		}
/*TODO*///
/*TODO*///		srcdata += srcmodulo;
/*TODO*///		dstdata += dstmodulo;
/*TODO*///		srcheight--;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///INLINE void blockmove_NtoN_transpen_noremap_flipx16(
/*TODO*///		const UINT16 *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		UINT16 *dstdata,int dstmodulo,
/*TODO*///		int transpen)
/*TODO*///{
/*TODO*///	UINT16 *end;
/*TODO*///
/*TODO*///	srcmodulo += srcwidth;
/*TODO*///	dstmodulo -= srcwidth;
/*TODO*///	//srcdata += srcwidth-1;
/*TODO*///
/*TODO*///	while (srcheight)
/*TODO*///	{
/*TODO*///		end = dstdata + srcwidth;
/*TODO*///		while (dstdata < end)
/*TODO*///		{
/*TODO*///			int col;
/*TODO*///
/*TODO*///			col = *(srcdata--);
/*TODO*///			if (col != transpen) *dstdata = col;
/*TODO*///			dstdata++;
/*TODO*///		}
/*TODO*///
/*TODO*///		srcdata += srcmodulo;
/*TODO*///		dstdata += dstmodulo;
/*TODO*///		srcheight--;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///INLINE void blockmove_NtoN_transpen_noremap32(
/*TODO*///		const UINT32 *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		UINT32 *dstdata,int dstmodulo,
/*TODO*///		int transpen)
/*TODO*///{
/*TODO*///	UINT32 *end;
/*TODO*///
/*TODO*///	srcmodulo -= srcwidth;
/*TODO*///	dstmodulo -= srcwidth;
/*TODO*///
/*TODO*///	while (srcheight)
/*TODO*///	{
/*TODO*///		end = dstdata + srcwidth;
/*TODO*///		while (dstdata < end)
/*TODO*///		{
/*TODO*///			int col;
/*TODO*///
/*TODO*///			col = *(srcdata++);
/*TODO*///			if (col != transpen) *dstdata = col;
/*TODO*///			dstdata++;
/*TODO*///		}
/*TODO*///
/*TODO*///		srcdata += srcmodulo;
/*TODO*///		dstdata += dstmodulo;
/*TODO*///		srcheight--;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///INLINE void blockmove_NtoN_transpen_noremap_flipx32(
/*TODO*///		const UINT32 *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		UINT32 *dstdata,int dstmodulo,
/*TODO*///		int transpen)
/*TODO*///{
/*TODO*///	UINT32 *end;
/*TODO*///
/*TODO*///	srcmodulo += srcwidth;
/*TODO*///	dstmodulo -= srcwidth;
/*TODO*///	//srcdata += srcwidth-1;
/*TODO*///
/*TODO*///	while (srcheight)
/*TODO*///	{
/*TODO*///		end = dstdata + srcwidth;
/*TODO*///		while (dstdata < end)
/*TODO*///		{
/*TODO*///			int col;
/*TODO*///
/*TODO*///			col = *(srcdata--);
/*TODO*///			if (col != transpen) *dstdata = col;
/*TODO*///			dstdata++;
/*TODO*///		}
/*TODO*///
/*TODO*///		srcdata += srcmodulo;
/*TODO*///		dstdata += dstmodulo;
/*TODO*///		srcheight--;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* 8-bit version */
/*TODO*///#define DATA_TYPE UINT8
/*TODO*///#define DEPTH 8
/*TODO*///
/*TODO*///#define DECLARE(function,args,body)
/*TODO*///#define DECLAREG(function,args,body)
/*TODO*///
/*TODO*///#define VMODULO 1
/*TODO*///#define HMODULO dstmodulo
/*TODO*///#define COMMON_ARGS														\
/*TODO*///		const UINT8 *srcdata,int srcheight,int srcwidth,int srcmodulo,	\
/*TODO*///		int topskip,int leftskip,int flipy,int flipx,					\
/*TODO*///		DATA_TYPE *dstdata,int dstheight,int dstwidth,int dstmodulo
/*TODO*///
/*TODO*///
/*TODO*///#define COLOR_ARG unsigned int colorbase,UINT8 *pridata,UINT32 pmask
/*TODO*///#define INCREMENT_DST(n) {dstdata+=(n);pridata += (n);}
/*TODO*///#define LOOKUP(n) (colorbase + (n))
/*TODO*///#define SETPIXELCOLOR(dest,n) { if (((1 << pridata[dest]) & pmask) == 0) { dstdata[dest] = (n);} pridata[dest] = 31; }
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_swapxy_raw_pri8 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG const UINT32 *paldata,UINT8 *pridata,UINT32 pmask
/*TODO*///#define LOOKUP(n) (paldata[n])
/*TODO*///#define SETPIXELCOLOR(dest,n) { if (((1 << pridata[dest]) & pmask) == 0) { dstdata[dest] = (n);} pridata[dest] = 31; }
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_swapxy_pri8 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef INCREMENT_DST
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG unsigned int colorbase
/*TODO*///#define INCREMENT_DST(n) {dstdata+=(n);}
/*TODO*///#define LOOKUP(n) (colorbase + (n))
/*TODO*///#define SETPIXELCOLOR(dest,n) {dstdata[dest] = (n);}
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_swapxy_raw8 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG const UINT32 *paldata
/*TODO*///#define LOOKUP(n) (paldata[n])
/*TODO*///#define SETPIXELCOLOR(dest,n) {dstdata[dest] = (n);}
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_swapxy8 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef INCREMENT_DST
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#undef HMODULO
/*TODO*///#undef VMODULO
/*TODO*///#undef COMMON_ARGS
/*TODO*///
/*TODO*///#define HMODULO 1
/*TODO*///#define VMODULO dstmodulo
/*TODO*///#define COMMON_ARGS														\
/*TODO*///		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,	\
/*TODO*///		int leftskip,int topskip,int flipx,int flipy,					\
/*TODO*///		DATA_TYPE *dstdata,int dstwidth,int dstheight,int dstmodulo
/*TODO*///
/*TODO*///#define COLOR_ARG unsigned int colorbase,UINT8 *pridata,UINT32 pmask
/*TODO*///#define INCREMENT_DST(n) {dstdata+=(n);pridata += (n);}
/*TODO*///#define LOOKUP(n) (colorbase + (n))
/*TODO*///#define SETPIXELCOLOR(dest,n) { if (((1 << pridata[dest]) & pmask) == 0) { dstdata[dest] = (n);} pridata[dest] = 31; }
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_raw_pri8 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG const UINT32 *paldata,UINT8 *pridata,UINT32 pmask
/*TODO*///#define LOOKUP(n) (paldata[n])
/*TODO*///#define SETPIXELCOLOR(dest,n) { if (((1 << pridata[dest]) & pmask) == 0) { dstdata[dest] = (n);} pridata[dest] = 31; }
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_pri8 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef INCREMENT_DST
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG unsigned int colorbase
/*TODO*///#define INCREMENT_DST(n) {dstdata+=(n);}
/*TODO*///#define LOOKUP(n) (colorbase + (n))
/*TODO*///#define SETPIXELCOLOR(dest,n) {dstdata[dest] = (n);}
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_raw8 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG const UINT32 *paldata
/*TODO*///#define LOOKUP(n) (paldata[n])
/*TODO*///#define SETPIXELCOLOR(dest,n) {dstdata[dest] = (n);}
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##8 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef INCREMENT_DST
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#undef HMODULO
/*TODO*///#undef VMODULO
/*TODO*///#undef COMMON_ARGS
/*TODO*///#undef DECLARE
/*TODO*///#undef DECLAREG
/*TODO*///
/*TODO*///#define DECLARE(function,args,body) void function##8 args body
/*TODO*///#define DECLAREG(function,args,body) void function##8 args body
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body)
/*TODO*///#define BLOCKMOVE(function,flipx,args) \
/*TODO*///	if (flipx) blockmove_##function##_flipx##8 args ; \
/*TODO*///	else blockmove_##function##8 args
/*TODO*///#define BLOCKMOVELU(function,args) \
/*TODO*///	if (gfx->flags & GFX_SWAPXY) blockmove_##function##_swapxy##8 args ; \
/*TODO*///	else blockmove_##function##8 args
/*TODO*///#define BLOCKMOVERAW(function,args) \
/*TODO*///	if (gfx->flags & GFX_SWAPXY) blockmove_##function##_swapxy##_raw##8 args ; \
/*TODO*///	else blockmove_##function##_raw##8 args
/*TODO*///#define BLOCKMOVEPRI(function,args) \
/*TODO*///	if (gfx->flags & GFX_SWAPXY) blockmove_##function##_swapxy##_pri##8 args ; \
/*TODO*///	else blockmove_##function##_pri##8 args
/*TODO*///#define BLOCKMOVERAWPRI(function,args) \
/*TODO*///	if (gfx->flags & GFX_SWAPXY) blockmove_##function##_swapxy##_raw_pri##8 args ; \
/*TODO*///	else blockmove_##function##_raw_pri##8 args
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef DECLAREG
/*TODO*///#undef BLOCKMOVE
/*TODO*///#undef BLOCKMOVELU
/*TODO*///#undef BLOCKMOVERAW
/*TODO*///#undef BLOCKMOVEPRI
/*TODO*///#undef BLOCKMOVERAWPRI
/*TODO*///
/*TODO*///#undef DEPTH
/*TODO*///#undef DATA_TYPE
/*TODO*///
/*TODO*////* 16-bit version */
/*TODO*///#define DATA_TYPE UINT16
/*TODO*///#define DEPTH 16
/*TODO*///#define alpha_blend alpha_blend16
/*TODO*///
/*TODO*///#define DECLARE(function,args,body)
/*TODO*///#define DECLAREG(function,args,body)
/*TODO*///
/*TODO*///#define VMODULO 1
/*TODO*///#define HMODULO dstmodulo
/*TODO*///#define COMMON_ARGS														\
/*TODO*///		const UINT8 *srcdata,int srcheight,int srcwidth,int srcmodulo,	\
/*TODO*///		int topskip,int leftskip,int flipy,int flipx,					\
/*TODO*///		DATA_TYPE *dstdata,int dstheight,int dstwidth,int dstmodulo
/*TODO*///
/*TODO*///#define COLOR_ARG unsigned int colorbase,UINT8 *pridata,UINT32 pmask
/*TODO*///#define INCREMENT_DST(n) {dstdata+=(n);pridata += (n);}
/*TODO*///#define LOOKUP(n) (colorbase + (n))
/*TODO*///#define SETPIXELCOLOR(dest,n) { if (((1 << pridata[dest]) & pmask) == 0) { dstdata[dest] = n;} pridata[dest] = 31; }
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_swapxy_raw_pri16 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG const UINT32 *paldata,UINT8 *pridata,UINT32 pmask
/*TODO*///#define LOOKUP(n) (paldata[n])
/*TODO*///#define SETPIXELCOLOR(dest,n) { if (((1 << pridata[dest]) & pmask) == 0) { dstdata[dest] = (n);} pridata[dest] = 31; }
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_swapxy_pri16 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef INCREMENT_DST
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG unsigned int colorbase
/*TODO*///#define INCREMENT_DST(n) {dstdata+=(n);}
/*TODO*///#define LOOKUP(n) (colorbase + (n))
/*TODO*///#define SETPIXELCOLOR(dest,n) {dstdata[dest] = (n);}
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_swapxy_raw16 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG const UINT32 *paldata
/*TODO*///#define LOOKUP(n) (paldata[n])
/*TODO*///#define SETPIXELCOLOR(dest,n) {dstdata[dest] = (n);}
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_swapxy16 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef INCREMENT_DST
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#undef HMODULO
/*TODO*///#undef VMODULO
/*TODO*///#undef COMMON_ARGS
/*TODO*///
/*TODO*///#define HMODULO 1
/*TODO*///#define VMODULO dstmodulo
/*TODO*///#define COMMON_ARGS														\
/*TODO*///		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,	\
/*TODO*///		int leftskip,int topskip,int flipx,int flipy,					\
/*TODO*///		DATA_TYPE *dstdata,int dstwidth,int dstheight,int dstmodulo
/*TODO*///
/*TODO*///#define COLOR_ARG unsigned int colorbase,UINT8 *pridata,UINT32 pmask
/*TODO*///#define INCREMENT_DST(n) {dstdata+=(n);pridata += (n);}
/*TODO*///#define LOOKUP(n) (colorbase + (n))
/*TODO*///#define SETPIXELCOLOR(dest,n) { if (((1 << pridata[dest]) & pmask) == 0) { dstdata[dest] = n;} pridata[dest] = 31; }
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_raw_pri16 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG const UINT32 *paldata,UINT8 *pridata,UINT32 pmask
/*TODO*///#define LOOKUP(n) (paldata[n])
/*TODO*///#define SETPIXELCOLOR(dest,n) { if (((1 << pridata[dest]) & pmask) == 0) { dstdata[dest] = (n);} pridata[dest] = 31; }
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_pri16 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef INCREMENT_DST
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG unsigned int colorbase
/*TODO*///#define INCREMENT_DST(n) {dstdata+=(n);}
/*TODO*///#define LOOKUP(n) (colorbase + (n))
/*TODO*///#define SETPIXELCOLOR(dest,n) {dstdata[dest] = (n);}
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_raw16 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG const UINT32 *paldata
/*TODO*///#define LOOKUP(n) (paldata[n])
/*TODO*///#define SETPIXELCOLOR(dest,n) {dstdata[dest] = (n);}
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##16 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef INCREMENT_DST
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#undef HMODULO
/*TODO*///#undef VMODULO
/*TODO*///#undef COMMON_ARGS
/*TODO*///#undef DECLARE
/*TODO*///#undef DECLAREG
/*TODO*///
/*TODO*///#define DECLARE(function,args,body) void function##16 args body
/*TODO*///#define DECLAREG(function,args,body) void function##16 args body
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body)
/*TODO*///#define BLOCKMOVE(function,flipx,args) \
/*TODO*///	if (flipx) blockmove_##function##_flipx##16 args ; \
/*TODO*///	else blockmove_##function##16 args
/*TODO*///#define BLOCKMOVELU(function,args) \
/*TODO*///	if (gfx->flags & GFX_SWAPXY) blockmove_##function##_swapxy##16 args ; \
/*TODO*///	else blockmove_##function##16 args
/*TODO*///#define BLOCKMOVERAW(function,args) \
/*TODO*///	if (gfx->flags & GFX_SWAPXY) blockmove_##function##_swapxy##_raw##16 args ; \
/*TODO*///	else blockmove_##function##_raw##16 args
/*TODO*///#define BLOCKMOVEPRI(function,args) \
/*TODO*///	if (gfx->flags & GFX_SWAPXY) blockmove_##function##_swapxy##_pri##16 args ; \
/*TODO*///	else blockmove_##function##_pri##16 args
/*TODO*///#define BLOCKMOVERAWPRI(function,args) \
/*TODO*///	if (gfx->flags & GFX_SWAPXY) blockmove_##function##_swapxy##_raw_pri##16 args ; \
/*TODO*///	else blockmove_##function##_raw_pri##16 args
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef DECLAREG
/*TODO*///#undef BLOCKMOVE
/*TODO*///#undef BLOCKMOVELU
/*TODO*///#undef BLOCKMOVERAW
/*TODO*///#undef BLOCKMOVEPRI
/*TODO*///#undef BLOCKMOVERAWPRI
/*TODO*///
/*TODO*///#undef DEPTH
/*TODO*///#undef DATA_TYPE
/*TODO*///#undef alpha_blend
/*TODO*///
/*TODO*////* 32-bit version */
/*TODO*///#define DATA_TYPE UINT32
/*TODO*///#define DEPTH 32
/*TODO*///#define alpha_blend alpha_blend32
/*TODO*///
/*TODO*///#define DECLARE(function,args,body)
/*TODO*///#define DECLAREG(function,args,body)
/*TODO*///
/*TODO*///#define VMODULO 1
/*TODO*///#define HMODULO dstmodulo
/*TODO*///#define COMMON_ARGS														\
/*TODO*///		const UINT8 *srcdata,int srcheight,int srcwidth,int srcmodulo,	\
/*TODO*///		int topskip,int leftskip,int flipy,int flipx,					\
/*TODO*///		DATA_TYPE *dstdata,int dstheight,int dstwidth,int dstmodulo
/*TODO*///
/*TODO*///#define COLOR_ARG unsigned int colorbase,UINT8 *pridata,UINT32 pmask
/*TODO*///#define INCREMENT_DST(n) {dstdata+=(n);pridata += (n);}
/*TODO*///#define LOOKUP(n) (colorbase + (n))
/*TODO*///#define SETPIXELCOLOR(dest,n) { if (((1 << pridata[dest]) & pmask) == 0) { dstdata[dest] = (n);} pridata[dest] = 31; }
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_swapxy_raw_pri32 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG const UINT32 *paldata,UINT8 *pridata,UINT32 pmask
/*TODO*///#define LOOKUP(n) (paldata[n])
/*TODO*///#define SETPIXELCOLOR(dest,n) { if (((1 << pridata[dest]) & pmask) == 0) { dstdata[dest] = (n);} pridata[dest] = 31; }
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_swapxy_pri32 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef INCREMENT_DST
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG unsigned int colorbase
/*TODO*///#define INCREMENT_DST(n) {dstdata+=(n);}
/*TODO*///#define LOOKUP(n) (colorbase + (n))
/*TODO*///#define SETPIXELCOLOR(dest,n) {dstdata[dest] = (n);}
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_swapxy_raw32 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG const UINT32 *paldata
/*TODO*///#define LOOKUP(n) (paldata[n])
/*TODO*///#define SETPIXELCOLOR(dest,n) {dstdata[dest] = (n);}
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_swapxy32 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef INCREMENT_DST
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#undef HMODULO
/*TODO*///#undef VMODULO
/*TODO*///#undef COMMON_ARGS
/*TODO*///
/*TODO*///#define HMODULO 1
/*TODO*///#define VMODULO dstmodulo
/*TODO*///#define COMMON_ARGS														\
/*TODO*///		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,	\
/*TODO*///		int leftskip,int topskip,int flipx,int flipy,					\
/*TODO*///		DATA_TYPE *dstdata,int dstwidth,int dstheight,int dstmodulo
/*TODO*///
/*TODO*///#define COLOR_ARG unsigned int colorbase,UINT8 *pridata,UINT32 pmask
/*TODO*///#define INCREMENT_DST(n) {dstdata+=(n);pridata += (n);}
/*TODO*///#define LOOKUP(n) (colorbase + (n))
/*TODO*///#define SETPIXELCOLOR(dest,n) { if (((1 << pridata[dest]) & pmask) == 0) { dstdata[dest] = (n);} pridata[dest] = 31; }
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_raw_pri32 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG const UINT32 *paldata,UINT8 *pridata,UINT32 pmask
/*TODO*///#define LOOKUP(n) (paldata[n])
/*TODO*///#define SETPIXELCOLOR(dest,n) { if (((1 << pridata[dest]) & pmask) == 0) { dstdata[dest] = (n);} pridata[dest] = 31; }
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_pri32 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef INCREMENT_DST
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG unsigned int colorbase
/*TODO*///#define INCREMENT_DST(n) {dstdata+=(n);}
/*TODO*///#define LOOKUP(n) (colorbase + (n))
/*TODO*///#define SETPIXELCOLOR(dest,n) {dstdata[dest] = (n);}
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##_raw32 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#define COLOR_ARG const UINT32 *paldata
/*TODO*///#define LOOKUP(n) (paldata[n])
/*TODO*///#define SETPIXELCOLOR(dest,n) {dstdata[dest] = (n);}
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body) void function##32 args body
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef COLOR_ARG
/*TODO*///#undef LOOKUP
/*TODO*///#undef INCREMENT_DST
/*TODO*///#undef SETPIXELCOLOR
/*TODO*///
/*TODO*///#undef HMODULO
/*TODO*///#undef VMODULO
/*TODO*///#undef COMMON_ARGS
/*TODO*///#undef DECLARE
/*TODO*///#undef DECLAREG
/*TODO*///
/*TODO*///#define DECLARE(function,args,body) void function##32 args body
/*TODO*///#define DECLAREG(function,args,body) void function##32 args body
/*TODO*///#define DECLARE_SWAP_RAW_PRI(function,args,body)
/*TODO*///#define BLOCKMOVE(function,flipx,args) \
/*TODO*///	if (flipx) blockmove_##function##_flipx##32 args ; \
/*TODO*///	else blockmove_##function##32 args
/*TODO*///#define BLOCKMOVELU(function,args) \
/*TODO*///	if (gfx->flags & GFX_SWAPXY) blockmove_##function##_swapxy##32 args ; \
/*TODO*///	else blockmove_##function##32 args
/*TODO*///#define BLOCKMOVERAW(function,args) \
/*TODO*///	if (gfx->flags & GFX_SWAPXY) blockmove_##function##_swapxy##_raw##32 args ; \
/*TODO*///	else blockmove_##function##_raw##32 args
/*TODO*///#define BLOCKMOVEPRI(function,args) \
/*TODO*///	if (gfx->flags & GFX_SWAPXY) blockmove_##function##_swapxy##_pri##32 args ; \
/*TODO*///	else blockmove_##function##_pri##32 args
/*TODO*///#define BLOCKMOVERAWPRI(function,args) \
/*TODO*///	if (gfx->flags & GFX_SWAPXY) blockmove_##function##_swapxy##_raw_pri##32 args ; \
/*TODO*///	else blockmove_##function##_raw_pri##32 args
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DECLARE
/*TODO*///#undef DECLARE_SWAP_RAW_PRI
/*TODO*///#undef DECLAREG
/*TODO*///#undef BLOCKMOVE
/*TODO*///#undef BLOCKMOVELU
/*TODO*///#undef BLOCKMOVERAW
/*TODO*///#undef BLOCKMOVEPRI
/*TODO*///#undef BLOCKMOVERAWPRI
/*TODO*///
/*TODO*///#undef DEPTH
/*TODO*///#undef DATA_TYPE
/*TODO*///#undef alpha_blend
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Draw graphic elements in the specified bitmap.
/*TODO*///
/*TODO*///  transparency == TRANSPARENCY_NONE - no transparency.
/*TODO*///  transparency == TRANSPARENCY_PEN - bits whose _original_ value is == transparent_color
/*TODO*///                                     are transparent. This is the most common kind of
/*TODO*///									 transparency.
/*TODO*///  transparency == TRANSPARENCY_PENS - as above, but transparent_color is a mask of
/*TODO*///  									 transparent pens.
/*TODO*///  transparency == TRANSPARENCY_COLOR - bits whose _remapped_ palette index (taken from
/*TODO*///                                     Machine->game_colortable) is == transparent_color
/*TODO*///
/*TODO*///  transparency == TRANSPARENCY_PEN_TABLE - the transparency condition is same as TRANSPARENCY_PEN
/*TODO*///					A special drawing is done according to gfx_drawmode_table[source pixel].
/*TODO*///					DRAWMODE_NONE      transparent
/*TODO*///					DRAWMODE_SOURCE    normal, draw source pixel.
/*TODO*///					DRAWMODE_SHADOW    destination is changed through palette_shadow_table[]
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
    public static void common_drawgfx(osd_bitmap dest, GfxElement gfx,/*unsigned*/ int code,/*unsigned*/ int color, int flipx, int flipy, int sx, int sy, rectangle clip, int transparency, int transparent_color, osd_bitmap pri_buffer, int/*UINT32*/ pri_mask) {
        rectangle myclip = new rectangle();

        if (gfx == null) {
            usrintf_showmessage("drawgfx() gfx == 0");
            return;
        }
        if (gfx.colortable == null && is_raw[transparency] == 0) {
            usrintf_showmessage("drawgfx() gfx->colortable == 0");
            return;
        }

        code %= gfx.total_elements;
        if (is_raw[transparency] == 0) {
            color %= gfx.total_colors;
        }

        if (alpha_active == 0 && (transparency == TRANSPARENCY_ALPHAONE || transparency == TRANSPARENCY_ALPHA)) {
            if (transparency == TRANSPARENCY_ALPHAONE && (cpu_getcurrentframe() & 1) != 0) {
                transparency = TRANSPARENCY_PENS;
                transparent_color = (1 << (transparent_color & 0xff)) | (1 << (transparent_color >> 8));
            } else {
                transparency = TRANSPARENCY_PEN;
                transparent_color &= 0xff;
            }
        }

        if (gfx.pen_usage != null && (transparency == TRANSPARENCY_PEN || transparency == TRANSPARENCY_PENS)) {
            int transmask = 0;

            if (transparency == TRANSPARENCY_PEN) {
                transmask = 1 << (transparent_color & 0xff);
            } else /* transparency == TRANSPARENCY_PENS */ {
                transmask = transparent_color;
            }

            if ((gfx.pen_usage[code] & ~transmask) == 0)/* character is totally transparent, no need to draw */ {
                return;
            } else if ((gfx.pen_usage[code] & transmask) == 0)/* character is totally opaque, can disable transparency */ {
                transparency = TRANSPARENCY_NONE;
            }
        }

        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
            int temp;

            temp = sx;
            sx = sy;
            sy = temp;

            temp = flipx;
            flipx = flipy;
            flipy = temp;

            if (clip != null) {
                /* clip and myclip might be the same, so we need a temporary storage */
                temp = clip.min_x;
                myclip.min_x = clip.min_y;
                myclip.min_y = temp;
                temp = clip.max_x;
                myclip.max_x = clip.max_y;
                myclip.max_y = temp;
                clip = myclip;
            }
        }
        if ((Machine.orientation & ORIENTATION_FLIP_X) != 0) {
            sx = dest.width - gfx.width - sx;
            if (clip != null) {
                int temp;


                /* clip and myclip might be the same, so we need a temporary storage */
                temp = clip.min_x;
                myclip.min_x = dest.width - 1 - clip.max_x;
                myclip.max_x = dest.width - 1 - temp;
                myclip.min_y = clip.min_y;
                myclip.max_y = clip.max_y;
                clip = myclip;
            }
            flipx = NOT(flipx);
        }
        if ((Machine.orientation & ORIENTATION_FLIP_Y) != 0) {
            sy = dest.height - gfx.height - sy;
            if (clip != null) {
                int temp;

                myclip.min_x = clip.min_x;
                myclip.max_x = clip.max_x;
                /* clip and myclip might be the same, so we need a temporary storage */
                temp = clip.min_y;
                myclip.min_y = dest.height - 1 - clip.max_y;
                myclip.max_y = dest.height - 1 - temp;
                clip = myclip;
            }

            flipy = NOT(flipy);

        }

        if (dest.depth == 8) {
            drawgfx_core8(dest, gfx, code, color, flipx, flipy, sx, sy, clip, transparency, transparent_color, pri_buffer, pri_mask);
        } else if (dest.depth == 15 || dest.depth == 16) {
            throw new UnsupportedOperationException("Unsupported");
            //drawgfx_core16(dest, gfx, code, color, flipx, flipy, sx, sy, clip, transparency, transparent_color, pri_buffer, pri_mask);
        } else {
            throw new UnsupportedOperationException("Unsupported");
            //drawgfx_core32(dest,gfx,code,color,flipx,flipy,sx,sy,clip,transparency,transparent_color,pri_buffer,pri_mask);
        }
    }

    public static void drawgfx(osd_bitmap dest, GfxElement gfx, int code, int color, int flipx, int flipy, int sx, int sy, rectangle clip, int transparency, int transparent_color) {
        common_drawgfx(dest, gfx, code, color, flipx, flipy, sx, sy, clip, transparency, transparent_color, null, 0);
    }

    public static void pdrawgfx(osd_bitmap dest, GfxElement gfx,
            int code, int color, int flipx, int flipy, int sx, int sy,
            rectangle clip, int transparency, int transparent_color, int priority_mask) {
        /*TODO*///	profiler_mark(PROFILER_DRAWGFX);
        common_drawgfx(dest, gfx, code, color, flipx, flipy, sx, sy, clip, transparency, transparent_color, priority_bitmap, priority_mask | (1 << 31));
        /*TODO*///	profiler_mark(PROFILER_END);
    }

    /*TODO*///void mdrawgfx(struct osd_bitmap *dest,const struct GfxElement *gfx,
/*TODO*///		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color,UINT32 priority_mask)
/*TODO*///{
/*TODO*///	profiler_mark(PROFILER_DRAWGFX);
/*TODO*///	common_drawgfx(dest,gfx,code,color,flipx,flipy,sx,sy,clip,transparency,transparent_color,priority_bitmap,priority_mask);
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///}
/*TODO*///
    /**
     * *************************************************************************
     *
     * Use drawgfx() to copy a bitmap onto another at the given position. This
     * function will very likely change in the future.
     *
     **************************************************************************
     */
    public static void copybitmap(osd_bitmap dest, osd_bitmap src, int flipx, int flipy, int sx, int sy, rectangle clip, int transparency, int transparent_color) {
        /* translate to proper transparency here */
        if (transparency == TRANSPARENCY_NONE) {
            transparency = TRANSPARENCY_NONE_RAW;
        } else if (transparency == TRANSPARENCY_PEN) {
            transparency = TRANSPARENCY_PEN_RAW;
        } else if (transparency == TRANSPARENCY_COLOR) {
            transparent_color = Machine.pens[transparent_color];
            transparency = TRANSPARENCY_PEN_RAW;
        }

        copybitmap_remap(dest, src, flipx, flipy, sx, sy, clip, transparency, transparent_color);
    }

    public static void copybitmap_remap(osd_bitmap dest, osd_bitmap src, int flipx, int flipy, int sx, int sy, rectangle clip, int transparency, int transparent_color) {
        rectangle myclip = new rectangle();

        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
            int temp;

            temp = sx;
            sx = sy;
            sy = temp;

            temp = flipx;
            flipx = flipy;
            flipy = temp;

            if (clip != null) {
                /* clip and myclip might be the same, so we need a temporary storage */
                temp = clip.min_x;
                myclip.min_x = clip.min_y;
                myclip.min_y = temp;
                temp = clip.max_x;
                myclip.max_x = clip.max_y;
                myclip.max_y = temp;
                clip = myclip;
            }
        }
        if ((Machine.orientation & ORIENTATION_FLIP_X) != 0) {
            sx = dest.width - src.width - sx;
            if (clip != null) {
                int temp;


                /* clip and myclip might be the same, so we need a temporary storage */
                temp = clip.min_x;
                myclip.min_x = dest.width - 1 - clip.max_x;
                myclip.max_x = dest.width - 1 - temp;
                myclip.min_y = clip.min_y;
                myclip.max_y = clip.max_y;
                clip = myclip;
            }
        }
        if ((Machine.orientation & ORIENTATION_FLIP_Y) != 0) {
            sy = dest.height - src.height - sy;
            if (clip != null) {
                int temp;

                myclip.min_x = clip.min_x;
                myclip.max_x = clip.max_x;
                /* clip and myclip might be the same, so we need a temporary storage */
                temp = clip.min_y;
                myclip.min_y = dest.height - 1 - clip.max_y;
                myclip.max_y = dest.height - 1 - temp;
                clip = myclip;
            }
        }

        if (dest.depth == 8) {
            copybitmap_core8(dest, src, flipx, flipy, sx, sy, clip, transparency, transparent_color);
        } else if (dest.depth == 15 || dest.depth == 16) {
            System.out.println("copybitmap_core16 todo");
            /*TODO*///		copybitmap_core16(dest,src,flipx,flipy,sx,sy,clip,transparency,transparent_color);
        } else {
            System.out.println("copybitmap_core32 todo");
            /*TODO*///		copybitmap_core32(dest,src,flipx,flipy,sx,sy,clip,transparency,transparent_color);
        }

    }

    /**
     * *************************************************************************
     *
     * Copy a bitmap onto another with scroll and wraparound. This function
     * supports multiple independently scrolling rows/columns. "rows" is the
     * number of indepentently scrolling rows. "rowscroll" is an array of
     * integers telling how much to scroll each row. Same thing for "cols" and
     * "colscroll". If the bitmap cannot scroll in one direction, set rows or
     * columns to 0. If the bitmap scrolls as a whole, set rows and/or cols to
     * 1. Bidirectional scrolling is, of course, supported only if the bitmap
     * scrolls as a whole in at least one direction.
     *
     **************************************************************************
     */
    public static void copyscrollbitmap(osd_bitmap dest, osd_bitmap src, int rows, int[] rowscroll, int cols, int[] colscroll, rectangle clip, int transparency, int transparent_color) {
        /* translate to proper transparency here */
        if (transparency == TRANSPARENCY_NONE) {
            transparency = TRANSPARENCY_NONE_RAW;
        } else if (transparency == TRANSPARENCY_PEN) {
            transparency = TRANSPARENCY_PEN_RAW;
        } else if (transparency == TRANSPARENCY_COLOR) {
            transparent_color = Machine.pens[transparent_color];
            transparency = TRANSPARENCY_PEN_RAW;
        }

        copyscrollbitmap_remap(dest, src, rows, rowscroll, cols, colscroll, clip, transparency, transparent_color);
    }

    public static void copyscrollbitmap_remap(osd_bitmap dest, osd_bitmap src, int rows, int[] rowscroll, int cols, int[] colscroll, rectangle clip, int transparency, int transparent_color) {
        int srcwidth, srcheight, destwidth, destheight;
        rectangle orig_clip = new rectangle();

        if (clip != null) {
            orig_clip.min_x = clip.min_x;
            orig_clip.max_x = clip.max_x;
            orig_clip.min_y = clip.min_y;
            orig_clip.max_y = clip.max_y;
        } else {
            orig_clip.min_x = 0;
            orig_clip.max_x = dest.width - 1;
            orig_clip.min_y = 0;
            orig_clip.max_y = dest.height - 1;
        }
        clip = orig_clip;

        if (rows == 0 && cols == 0) {
            copybitmap(dest, src, 0, 0, 0, 0, clip, transparency, transparent_color);
            return;
        }

        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
            srcwidth = src.height;
            srcheight = src.width;
            destwidth = dest.height;
            destheight = dest.width;
        } else {
            srcwidth = src.width;
            srcheight = src.height;
            destwidth = dest.width;
            destheight = dest.height;
        }

        if (rows == 0) {
            /* scrolling columns */
            int col, colwidth;
            rectangle myclip = new rectangle();

            colwidth = srcwidth / cols;

            myclip.min_y = clip.min_y;
            myclip.max_y = clip.max_y;

            col = 0;
            while (col < cols) {
                int cons, scroll;


                /* count consecutive columns scrolled by the same amount */
                scroll = colscroll[col];
                cons = 1;
                while (col + cons < cols && colscroll[col + cons] == scroll) {
                    cons++;
                }

                if (scroll < 0) {
                    scroll = srcheight - (-scroll) % srcheight;
                } else {
                    scroll %= srcheight;
                }

                myclip.min_x = col * colwidth;
                if (myclip.min_x < clip.min_x) {
                    myclip.min_x = clip.min_x;
                }
                myclip.max_x = (col + cons) * colwidth - 1;
                if (myclip.max_x > clip.max_x) {
                    myclip.max_x = clip.max_x;
                }

                copybitmap(dest, src, 0, 0, 0, scroll, myclip, transparency, transparent_color);
                copybitmap(dest, src, 0, 0, 0, scroll - srcheight, myclip, transparency, transparent_color);

                col += cons;
            }
        } else if (cols == 0) {
            /* scrolling rows */
            int row, rowheight;
            rectangle myclip = new rectangle();

            rowheight = srcheight / rows;

            myclip.min_x = clip.min_x;
            myclip.max_x = clip.max_x;

            row = 0;
            while (row < rows) {
                int cons, scroll;


                /* count consecutive rows scrolled by the same amount */
                scroll = rowscroll[row];
                cons = 1;
                while (row + cons < rows && rowscroll[row + cons] == scroll) {
                    cons++;
                }

                if (scroll < 0) {
                    scroll = srcwidth - (-scroll) % srcwidth;
                } else {
                    scroll %= srcwidth;
                }

                myclip.min_y = row * rowheight;
                if (myclip.min_y < clip.min_y) {
                    myclip.min_y = clip.min_y;
                }
                myclip.max_y = (row + cons) * rowheight - 1;
                if (myclip.max_y > clip.max_y) {
                    myclip.max_y = clip.max_y;
                }

                copybitmap(dest, src, 0, 0, scroll, 0, myclip, transparency, transparent_color);
                copybitmap(dest, src, 0, 0, scroll - srcwidth, 0, myclip, transparency, transparent_color);

                row += cons;
            }
        } else if (rows == 1 && cols == 1) {
            /* XY scrolling playfield */
            int scrollx, scrolly, sx, sy;

            if (rowscroll[0] < 0) {
                scrollx = srcwidth - (-rowscroll[0]) % srcwidth;
            } else {
                scrollx = rowscroll[0] % srcwidth;
            }

            if (colscroll[0] < 0) {
                scrolly = srcheight - (-colscroll[0]) % srcheight;
            } else {
                scrolly = colscroll[0] % srcheight;
            }

            for (sx = scrollx - srcwidth; sx < destwidth; sx += srcwidth) {
                for (sy = scrolly - srcheight; sy < destheight; sy += srcheight) {
                    copybitmap(dest, src, 0, 0, sx, sy, clip, transparency, transparent_color);
                }
            }
        } else if (rows == 1) {
            /* scrolling columns + horizontal scroll */
            int col, colwidth;
            int scrollx;
            rectangle myclip = new rectangle();

            if (rowscroll[0] < 0) {
                scrollx = srcwidth - (-rowscroll[0]) % srcwidth;
            } else {
                scrollx = rowscroll[0] % srcwidth;
            }

            colwidth = srcwidth / cols;

            myclip.min_y = clip.min_y;
            myclip.max_y = clip.max_y;

            col = 0;
            while (col < cols) {
                int cons, scroll;


                /* count consecutive columns scrolled by the same amount */
                scroll = colscroll[col];
                cons = 1;
                while (col + cons < cols && colscroll[col + cons] == scroll) {
                    cons++;
                }

                if (scroll < 0) {
                    scroll = srcheight - (-scroll) % srcheight;
                } else {
                    scroll %= srcheight;
                }

                myclip.min_x = col * colwidth + scrollx;
                if (myclip.min_x < clip.min_x) {
                    myclip.min_x = clip.min_x;
                }
                myclip.max_x = (col + cons) * colwidth - 1 + scrollx;
                if (myclip.max_x > clip.max_x) {
                    myclip.max_x = clip.max_x;
                }

                copybitmap(dest, src, 0, 0, scrollx, scroll, myclip, transparency, transparent_color);
                copybitmap(dest, src, 0, 0, scrollx, scroll - srcheight, myclip, transparency, transparent_color);

                myclip.min_x = col * colwidth + scrollx - srcwidth;
                if (myclip.min_x < clip.min_x) {
                    myclip.min_x = clip.min_x;
                }
                myclip.max_x = (col + cons) * colwidth - 1 + scrollx - srcwidth;
                if (myclip.max_x > clip.max_x) {
                    myclip.max_x = clip.max_x;
                }

                copybitmap(dest, src, 0, 0, scrollx - srcwidth, scroll, myclip, transparency, transparent_color);
                copybitmap(dest, src, 0, 0, scrollx - srcwidth, scroll - srcheight, myclip, transparency, transparent_color);

                col += cons;
            }
        } else if (cols == 1) {
            /* scrolling rows + vertical scroll */
            int row, rowheight;
            int scrolly;
            rectangle myclip = new rectangle();

            if (colscroll[0] < 0) {
                scrolly = srcheight - (-colscroll[0]) % srcheight;
            } else {
                scrolly = colscroll[0] % srcheight;
            }

            rowheight = srcheight / rows;

            myclip.min_x = clip.min_x;
            myclip.max_x = clip.max_x;

            row = 0;
            while (row < rows) {
                int cons, scroll;


                /* count consecutive rows scrolled by the same amount */
                scroll = rowscroll[row];
                cons = 1;
                while (row + cons < rows && rowscroll[row + cons] == scroll) {
                    cons++;
                }

                if (scroll < 0) {
                    scroll = srcwidth - (-scroll) % srcwidth;
                } else {
                    scroll %= srcwidth;
                }

                myclip.min_y = row * rowheight + scrolly;
                if (myclip.min_y < clip.min_y) {
                    myclip.min_y = clip.min_y;
                }
                myclip.max_y = (row + cons) * rowheight - 1 + scrolly;
                if (myclip.max_y > clip.max_y) {
                    myclip.max_y = clip.max_y;
                }

                copybitmap(dest, src, 0, 0, scroll, scrolly, myclip, transparency, transparent_color);
                copybitmap(dest, src, 0, 0, scroll - srcwidth, scrolly, myclip, transparency, transparent_color);

                myclip.min_y = row * rowheight + scrolly - srcheight;
                if (myclip.min_y < clip.min_y) {
                    myclip.min_y = clip.min_y;
                }
                myclip.max_y = (row + cons) * rowheight - 1 + scrolly - srcheight;
                if (myclip.max_y > clip.max_y) {
                    myclip.max_y = clip.max_y;
                }

                copybitmap(dest, src, 0, 0, scroll, scrolly - srcheight, myclip, transparency, transparent_color);
                copybitmap(dest, src, 0, 0, scroll - srcwidth, scrolly - srcheight, myclip, transparency, transparent_color);

                row += cons;
            }
        }

    }

    /*TODO*///
/*TODO*///
/*TODO*////* notes:
/*TODO*///   - startx and starty MUST be UINT32 for calculations to work correctly
/*TODO*///   - srcbitmap->width and height are assumed to be a power of 2 to speed up wraparound
/*TODO*///   */
/*TODO*///void copyrozbitmap(struct osd_bitmap *dest,struct osd_bitmap *src,
/*TODO*///		UINT32 startx,UINT32 starty,int incxx,int incxy,int incyx,int incyy,int wraparound,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color,UINT32 priority)
/*TODO*///{
/*TODO*///	profiler_mark(PROFILER_COPYBITMAP);
/*TODO*///
/*TODO*///	/* cheat, the core doesn't support TRANSPARENCY_NONE yet */
/*TODO*///	if (transparency == TRANSPARENCY_NONE)
/*TODO*///	{
/*TODO*///		transparency = TRANSPARENCY_PEN;
/*TODO*///		transparent_color = -1;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* if necessary, remap the transparent color */
/*TODO*///	if (transparency == TRANSPARENCY_COLOR)
/*TODO*///	{
/*TODO*///		transparency = TRANSPARENCY_PEN;
/*TODO*///		transparent_color = Machine->pens[transparent_color];
/*TODO*///	}
/*TODO*///
/*TODO*///	if (transparency != TRANSPARENCY_PEN)
/*TODO*///	{
/*TODO*///		usrintf_showmessage("copyrozbitmap unsupported trans %02x",transparency);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (dest->depth == 8)
/*TODO*///		copyrozbitmap_core8(dest,src,startx,starty,incxx,incxy,incyx,incyy,wraparound,clip,transparency,transparent_color,priority);
/*TODO*///	else if(dest->depth == 15 || dest->depth == 16)
/*TODO*///		copyrozbitmap_core16(dest,src,startx,starty,incxx,incxy,incyx,incyy,wraparound,clip,transparency,transparent_color,priority);
/*TODO*///	else
/*TODO*///		copyrozbitmap_core32(dest,src,startx,starty,incxx,incxy,incyx,incyy,wraparound,clip,transparency,transparent_color,priority);
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////* fill a bitmap using the specified pen */
    public static void fillbitmap(osd_bitmap dest, int pen, rectangle clip) {
        int sx, sy, ex, ey, y;
        rectangle myclip = new rectangle();

        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
            if (clip != null) {
                myclip.min_x = clip.min_y;
                myclip.max_x = clip.max_y;
                myclip.min_y = clip.min_x;
                myclip.max_y = clip.max_x;
                clip = myclip;
            }
        }
        if ((Machine.orientation & ORIENTATION_FLIP_X) != 0) {
            if (clip != null) {
                int temp;

                temp = clip.min_x;
                myclip.min_x = dest.width - 1 - clip.max_x;
                myclip.max_x = dest.width - 1 - temp;
                myclip.min_y = clip.min_y;
                myclip.max_y = clip.max_y;
                clip = myclip;
            }
        }
        if ((Machine.orientation & ORIENTATION_FLIP_Y) != 0) {
            if (clip != null) {
                int temp;

                myclip.min_x = clip.min_x;
                myclip.max_x = clip.max_x;
                temp = clip.min_y;
                myclip.min_y = dest.height - 1 - clip.max_y;
                myclip.max_y = dest.height - 1 - temp;
                clip = myclip;
            }
        }

        sx = 0;
        ex = dest.width - 1;
        sy = 0;
        ey = dest.height - 1;

        if (clip != null && sx < clip.min_x) {
            sx = clip.min_x;
        }
        if (clip != null && ex > clip.max_x) {
            ex = clip.max_x;
        }
        if (sx > ex) {
            return;
        }
        if (clip != null && sy < clip.min_y) {
            sy = clip.min_y;
        }
        if (clip != null && ey > clip.max_y) {
            ey = clip.max_y;
        }
        if (sy > ey) {
            return;
        }

        if ((Machine.drv.video_attributes & VIDEO_SUPPORTS_DIRTY) != 0) {
            osd_mark_dirty(sx, sy, ex, ey);
        }
        if (dest.depth == 32) {
            throw new UnsupportedOperationException("Unsupported");
            /*TODO*///		if (((pen >> 8) == (pen & 0xff)) && ((pen>>16) == (pen & 0xff)))
/*TODO*///		{
/*TODO*///			for (y = sy;y <= ey;y++)
/*TODO*///				memset(&dest->line[y][sx*4],pen&0xff,(ex-sx+1)*4);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			UINT32 *sp = (UINT32 *)dest->line[sy];
/*TODO*///			int x;
/*TODO*///
/*TODO*///			for (x = sx;x <= ex;x++)
/*TODO*///				sp[x] = pen;
/*TODO*///			sp+=sx;
/*TODO*///			for (y = sy+1;y <= ey;y++)
/*TODO*///				memcpy(&dest->line[y][sx*4],sp,(ex-sx+1)*4);
/*TODO*///		}
        } else if (dest.depth == 15 || dest.depth == 16) {
            throw new UnsupportedOperationException("Unsupported");
            /*TODO*///		if ((pen >> 8) == (pen & 0xff))
/*TODO*///		{
/*TODO*///			for (y = sy;y <= ey;y++)
/*TODO*///				memset(&dest->line[y][sx*2],pen&0xff,(ex-sx+1)*2);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			UINT16 *sp = (UINT16 *)dest->line[sy];
/*TODO*///			int x;
/*TODO*///
/*TODO*///			for (x = sx;x <= ex;x++)
/*TODO*///				sp[x] = pen;
/*TODO*///			sp+=sx;
/*TODO*///			for (y = sy+1;y <= ey;y++)
/*TODO*///				memcpy(&dest->line[y][sx*2],sp,(ex-sx+1)*2);
/*TODO*///		}
        } else {
            for (y = sy; y <= ey; y++) {
                memset(dest.line[y], sx, pen, ex - sx + 1);
            }
        }
    }
    

static void common_drawgfxzoom( osd_bitmap dest_bmp, GfxElement gfx,
		int code,int color,int flipx,int flipy,int sx,int sy,
		rectangle clip,int transparency,int transparent_color,
		int scalex, int scaley,osd_bitmap pri_buffer,int pri_mask)
{
	rectangle myclip=new rectangle();
	int alphapen = 0;

	if (scalex==0 || scaley==0) return;

	if (scalex == 0x10000 && scaley == 0x10000)
	{
		common_drawgfx(dest_bmp,gfx,code,color,flipx,flipy,sx,sy,clip,transparency,transparent_color,pri_buffer,pri_mask);
		return;
	}


	if (transparency != TRANSPARENCY_PEN && transparency != TRANSPARENCY_PEN_RAW
			&& transparency != TRANSPARENCY_PENS && transparency != TRANSPARENCY_COLOR
			&& transparency != TRANSPARENCY_PEN_TABLE && transparency != TRANSPARENCY_PEN_TABLE_RAW
			&& transparency != TRANSPARENCY_BLEND_RAW && transparency != TRANSPARENCY_ALPHAONE
			&& transparency != TRANSPARENCY_ALPHA)
	{
		usrintf_showmessage("drawgfxzoom unsupported trans %02x",transparency);
		return;
	}

	if (alpha_active==0 && (transparency == TRANSPARENCY_ALPHAONE || transparency == TRANSPARENCY_ALPHA))
	{
		transparency = TRANSPARENCY_PEN;
		transparent_color &= 0xff;
	}

	if (transparency == TRANSPARENCY_ALPHAONE)
	{
		alphapen = transparent_color >> 8;
		transparent_color &= 0xff;
	}

	if (transparency == TRANSPARENCY_COLOR)
		transparent_color = Machine.pens[transparent_color];


	/*
	scalex and scaley are 16.16 fixed point numbers
	1<<15 : shrink to 50%
	1<<16 : uniform scale
	1<<17 : double to 200%
	*/


	if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0)
	{
		int temp;

		temp = sx;
		sx = sy;
		sy = temp;

		temp = flipx;
		flipx = flipy;
		flipy = temp;

		temp = scalex;
		scalex = scaley;
		scaley = temp;

		if (clip != null)
		{
			/* clip and myclip might be the same, so we need a temporary storage */
			temp = clip.min_x;
			myclip.min_x = clip.min_y;
			myclip.min_y = temp;
			temp = clip.max_x;
			myclip.max_x = clip.max_y;
			myclip.max_y = temp;
			clip = new rectangle(myclip);
		}
	}
	if ((Machine.orientation & ORIENTATION_FLIP_X) != 0)
	{
		sx = dest_bmp.width - ((gfx.width * scalex + 0x7fff) >> 16) - sx;
		if (clip != null)
		{
			int temp;


			/* clip and myclip might be the same, so we need a temporary storage */
			temp = clip.min_x;
			myclip.min_x = dest_bmp.width-1 - clip.max_x;
			myclip.max_x = dest_bmp.width-1 - temp;
			myclip.min_y = clip.min_y;
			myclip.max_y = clip.max_y;
			clip = new rectangle(myclip);
		}
/*TODO*///#ifndef PREROTATE_GFX
		flipx = NOT(flipx);
/*TODO*///#endif
	}
	if ((Machine.orientation & ORIENTATION_FLIP_Y) != 0)
	{
		sy = dest_bmp.height - ((gfx.height * scaley + 0x7fff) >> 16) - sy;
		if (clip != null)
		{
			int temp;


			myclip.min_x = clip.min_x;
			myclip.max_x = clip.max_x;
			/* clip and myclip might be the same, so we need a temporary storage */
			temp = clip.min_y;
			myclip.min_y = dest_bmp.height-1 - clip.max_y;
			myclip.max_y = dest_bmp.height-1 - temp;
			clip = new rectangle(myclip);
		}
/*TODO*///#ifndef PREROTATE_GFX
		flipy = NOT(flipy);
/*TODO*///#endif
	}

	/* KW 991012 -- Added code to force clip to bitmap boundary */
	if(clip != null)
	{
		myclip.min_x = clip.min_x;
		myclip.max_x = clip.max_x;
		myclip.min_y = clip.min_y;
		myclip.max_y = clip.max_y;

		if (myclip.min_x < 0) myclip.min_x = 0;
		if (myclip.max_x >= dest_bmp.width) myclip.max_x = dest_bmp.width-1;
		if (myclip.min_y < 0) myclip.min_y = 0;
		if (myclip.max_y >= dest_bmp.height) myclip.max_y = dest_bmp.height-1;

		clip=new rectangle(myclip);
	}


	/* ASG 980209 -- added 16-bit version */
	if (dest_bmp.depth == 8)
	{
		if( gfx!=null && gfx.colortable!=null )
		{
			IntArray pal = new IntArray(gfx.colortable, gfx.color_granularity * (color % gfx.total_colors)); /* ASG 980209 */
			int source_base = (code % gfx.total_elements) * gfx.height;

			int sprite_screen_height = (scaley*gfx.height+0x8000)>>16;
			int sprite_screen_width = (scalex*gfx.width+0x8000)>>16;

			if (sprite_screen_width!=0 && sprite_screen_height!=0)
			{
				/* compute sprite increment per screen pixel */
				int dx = (gfx.width<<16)/sprite_screen_width;
				int dy = (gfx.height<<16)/sprite_screen_height;

				int ex = sx+sprite_screen_width;
				int ey = sy+sprite_screen_height;

				int x_index_base;
				int y_index;

				if( flipx != 0 )
				{
					x_index_base = (sprite_screen_width-1)*dx;
					dx = -dx;
				}
				else
				{
					x_index_base = 0;
				}

				if( flipy != 0 )
				{
					y_index = (sprite_screen_height-1)*dy;
					dy = -dy;
				}
				else
				{
					y_index = 0;
				}

				if( clip != null )
				{
					if( sx < clip.min_x)
					{ /* clip left */
						int pixels = clip.min_x-sx;
						sx += pixels;
						x_index_base += pixels*dx;
					}
					if( sy < clip.min_y )
					{ /* clip top */
						int pixels = clip.min_y-sy;
						sy += pixels;
						y_index += pixels*dy;
					}
					/* NS 980211 - fixed incorrect clipping */
					if( ex > clip.max_x+1 )
					{ /* clip right */
						int pixels = ex-clip.max_x-1;
						ex -= pixels;
					}
					if( ey > clip.max_y+1 )
					{ /* clip bottom */
						int pixels = ey-clip.max_y-1;
						ey -= pixels;
					}
				}

				if( ex>sx )
				{ /* skip if inner loop doesn't draw anything */
					int y;

					/* case 1: TRANSPARENCY_PEN */
					if (transparency == TRANSPARENCY_PEN)
					{
						if (pri_buffer != null)
						{
							if ((gfx.flags & GFX_PACKED) != 0)
							{
								for( y=sy; y<ey; y++ )
								{
									UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
									UBytePtr dest = new UBytePtr(dest_bmp.line[y]);
									UBytePtr pri = new UBytePtr(pri_buffer.line[y]);

									int x, x_index = x_index_base;
									for( x=sx; x<ex; x++ )
									{
										int c = (source.read(x_index>>17) >> ((x_index & 0x10000) >> 14)) & 0x0f;
										if( c != transparent_color )
										{
											if (((1 << pri.read(x)) & pri_mask) == 0)
												dest.write(x, pal.read(c));
											pri.write(x, 31);
										}
										x_index += dx;
									}

									y_index += dy;
								}
							}
							else
							{
								for( y=sy; y<ey; y++ )
								{
									UBytePtr source = new UBytePtr (gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
									UBytePtr dest = new UBytePtr (dest_bmp.line[y]);
									UBytePtr pri = new UBytePtr (pri_buffer.line[y]);

									int x, x_index = x_index_base;
									for( x=sx; x<ex; x++ )
									{
										int c = source.read(x_index>>16);
										if( c != transparent_color )
										{
											if (((1 << pri.read(x)) & pri_mask) == 0)
												dest.write(x, pal.read(c));
											pri.write(x, 31);
										}
										x_index += dx;
									}

									y_index += dy;
								}
							}
						}
						else
						{
							if ((gfx.flags & GFX_PACKED) != 0)
							{
								for( y=sy; y<ey; y++ )
								{
									UBytePtr source = new UBytePtr (gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
									UBytePtr dest = new UBytePtr (dest_bmp.line[y]);

									int x, x_index = x_index_base;
									for( x=sx; x<ex; x++ )
									{
										int c = (source.read(x_index>>17) >> ((x_index & 0x10000) >> 14)) & 0x0f;
										if( c != transparent_color ) dest.write(x, pal.read(c));
										x_index += dx;
									}

									y_index += dy;
								}
							}
							else
							{
								for( y=sy; y<ey; y++ )
								{
									UBytePtr source = new UBytePtr (gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
									UBytePtr dest = new UBytePtr (dest_bmp.line[y]);

									int x, x_index = x_index_base;
									for( x=sx; x<ex; x++ )
									{
										int c = source.read(x_index>>16);
										if( c != transparent_color ) dest.write(x, pal.read(c));
										x_index += dx;
									}

									y_index += dy;
								}
							}
						}
					}

					/* case 1b: TRANSPARENCY_PEN_RAW */
					if (transparency == TRANSPARENCY_PEN_RAW)
					{
						if (pri_buffer != null)
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UBytePtr dest = new UBytePtr(dest_bmp.line[y]);
								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color )
									{
										if (((1 << pri.read(x)) & pri_mask) == 0)
											dest.write(x, color + c);
										pri.write(x, 31);
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
						else
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UBytePtr dest = new UBytePtr(dest_bmp.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color ) dest.write(x, color + c);
									x_index += dx;
								}

								y_index += dy;
							}
						}
					}

					/* case 1c: TRANSPARENCY_BLEND_RAW */
					if (transparency == TRANSPARENCY_BLEND_RAW)
					{
						if (pri_buffer != null)
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UBytePtr dest = new UBytePtr(dest_bmp.line[y]);
								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color )
									{
										if (((1 << pri.read(x)) & pri_mask) == 0)
											dest.write(x, dest.read(x) | (color + c));
										pri.write(x, 31);
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
						else
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UBytePtr dest = new UBytePtr(dest_bmp.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color ) dest.write(x, dest.read(x) | (color + c));
									x_index += dx;
								}

								y_index += dy;
							}
						}
					}

					/* case 2: TRANSPARENCY_PENS */
					if (transparency == TRANSPARENCY_PENS)
					{
						if (pri_buffer != null)
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UBytePtr dest = new UBytePtr(dest_bmp.line[y]);
								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if (((1 << c) & transparent_color) == 0)
									{
										if (((1 << pri.read(x)) & pri_mask) == 0)
											dest.write(x, pal.read(c));
										pri.write(x, 31);
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
						else
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UBytePtr dest = new UBytePtr(dest_bmp.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if (((1 << c) & transparent_color) == 0)
										dest.write(x, pal.read(c));
									x_index += dx;
								}

								y_index += dy;
							}
						}
					}

					/* case 3: TRANSPARENCY_COLOR */
					else if (transparency == TRANSPARENCY_COLOR)
					{
						if (pri_buffer != null)
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UBytePtr dest = new UBytePtr(dest_bmp.line[y]);
								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = pal.read(source.read(x_index>>16));
									if( c != transparent_color )
									{
										if (((1 << pri.read(x)) & pri_mask) == 0)
											dest.write(x, c);
										pri.write(x, 31);
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
						else
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UBytePtr dest = new UBytePtr(dest_bmp.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = pal.read(source.read(x_index>>16));
									if( c != transparent_color ) dest.write(x, c);
									x_index += dx;
								}

								y_index += dy;
							}
						}
					}

					/* case 4: TRANSPARENCY_PEN_TABLE */
					if (transparency == TRANSPARENCY_PEN_TABLE)
					{
						if (pri_buffer != null)
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UBytePtr dest = new UBytePtr(dest_bmp.line[y]);
								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color )
									{
										if (((1 << pri.read(x)) & pri_mask) == 0)
										{
											switch(gfx_drawmode_table[c])
											{
											case DRAWMODE_SOURCE:
												dest.write(x, pal.read(c));
												break;
											case DRAWMODE_SHADOW:
												dest.write(x, palette_shadow_table[dest.read(x)]);
												break;
											}
										}
										pri.write(x, 31);
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
						else
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UBytePtr dest = new UBytePtr(dest_bmp.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color )
									{
										switch(gfx_drawmode_table[c])
										{
										case DRAWMODE_SOURCE:
											dest.write(x, pal.read(c));
											break;
										case DRAWMODE_SHADOW:
											dest.write(x, palette_shadow_table[dest.read(x)]);
											break;
										}
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
					}

					/* case 4b: TRANSPARENCY_PEN_TABLE_RAW */
					if (transparency == TRANSPARENCY_PEN_TABLE_RAW)
					{
						if (pri_buffer != null)
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UBytePtr dest = new UBytePtr(dest_bmp.line[y]);
								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color )
									{
										if (((1 << pri.read(x)) & pri_mask) == 0)
										{
											switch(gfx_drawmode_table[c])
											{
											case DRAWMODE_SOURCE:
												dest.write(x, color + c);
												break;
											case DRAWMODE_SHADOW:
												dest.write(x, palette_shadow_table[dest.read(x)]);
												break;
											}
										}
										pri.write(x, 31);
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
						else
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UBytePtr dest = new UBytePtr(dest_bmp.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color )
									{
										switch(gfx_drawmode_table[c])
										{
										case DRAWMODE_SOURCE:
											dest.write(x, color + c);
											break;
										case DRAWMODE_SHADOW:
											dest.write(x, palette_shadow_table[dest.read(x)]);
											break;
										}
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
					}
				}
			}
		}
	}

	/* ASG 980209 -- new 16-bit part */
	else if (dest_bmp.depth == 15 || dest_bmp.depth == 16)
	{
		if( gfx!=null && gfx.colortable!=null )
		{
			IntArray pal = new IntArray(gfx.colortable, gfx.color_granularity * (color % gfx.total_colors)); /* ASG 980209 */
			int source_base = (code % gfx.total_elements) * gfx.height;

			int sprite_screen_height = (scaley*gfx.height+0x8000)>>16;
			int sprite_screen_width = (scalex*gfx.width+0x8000)>>16;

			if (sprite_screen_width!=0 && sprite_screen_height!=0)
			{
				/* compute sprite increment per screen pixel */
				int dx = (gfx.width<<16)/sprite_screen_width;
				int dy = (gfx.height<<16)/sprite_screen_height;

				int ex = sx+sprite_screen_width;
				int ey = sy+sprite_screen_height;

				int x_index_base;
				int y_index;

				if( flipx != 0 )
				{
					x_index_base = (sprite_screen_width-1)*dx;
					dx = -dx;
				}
				else
				{
					x_index_base = 0;
				}

				if( flipy != 0 )
				{
					y_index = (sprite_screen_height-1)*dy;
					dy = -dy;
				}
				else
				{
					y_index = 0;
				}

				if( clip != null )
				{
					if( sx < clip.min_x)
					{ /* clip left */
						int pixels = clip.min_x-sx;
						sx += pixels;
						x_index_base += pixels*dx;
					}
					if( sy < clip.min_y )
					{ /* clip top */
						int pixels = clip.min_y-sy;
						sy += pixels;
						y_index += pixels*dy;
					}
					/* NS 980211 - fixed incorrect clipping */
					if( ex > clip.max_x+1 )
					{ /* clip right */
						int pixels = ex-clip.max_x-1;
						ex -= pixels;
					}
					if( ey > clip.max_y+1 )
					{ /* clip bottom */
						int pixels = ey-clip.max_y-1;
						ey -= pixels;
					}
				}

				if( ex>sx )
				{ /* skip if inner loop doesn't draw anything */
					int y;

					/* case 1: TRANSPARENCY_PEN */
					if (transparency == TRANSPARENCY_PEN)
					{
						if (pri_buffer != null)
						{
							if ((gfx.flags & GFX_PACKED) != 0)
							{
								for( y=sy; y<ey; y++ )
								{
									UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
									UShortPtr dest = new UShortPtr(dest_bmp.line[y]);
									UBytePtr pri = new UBytePtr(pri_buffer.line[y]);

									int x, x_index = x_index_base;
									for( x=sx; x<ex; x++ )
									{
										int c = (source.read(x_index>>17) >> ((x_index & 0x10000) >> 14)) & 0x0f;
										if( c != transparent_color )
										{
											if (((1 << pri.read(x)) & pri_mask) == 0)
												dest.write(x, (char) pal.read(c));
											pri.write(x, 31);
										}
										x_index += dx;
									}

									y_index += dy;
								}
							}
							else
							{
								for( y=sy; y<ey; y++ )
								{
									UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
									UShortPtr dest = new UShortPtr(dest_bmp.line[y]);
									UBytePtr pri = new UBytePtr(pri_buffer.line[y]);

									int x, x_index = x_index_base;
									for( x=sx; x<ex; x++ )
									{
										int c = source.read(x_index>>16);
										if( c != transparent_color )
										{
											if (((1 << pri.read(x)) & pri_mask) == 0)
												dest.write(x, (char) pal.read(c));
											pri.write(x, 31);
										}
										x_index += dx;
									}

									y_index += dy;
								}
							}
						}
						else
						{
							if ((gfx.flags & GFX_PACKED) != 0)
							{
								for( y=sy; y<ey; y++ )
								{
									UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
									UShortPtr dest = new UShortPtr(dest_bmp.line[y]);

									int x, x_index = x_index_base;
									for( x=sx; x<ex; x++ )
									{
										int c = (source.read(x_index>>17) >> ((x_index & 0x10000) >> 14)) & 0x0f;
										if( c != transparent_color ) dest.write(x, (char) pal.read(c));
										x_index += dx;
									}

									y_index += dy;
								}
							}
							else
							{
								for( y=sy; y<ey; y++ )
								{
									UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
									UShortPtr dest = new UShortPtr(dest_bmp.line[y]);

									int x, x_index = x_index_base;
									for( x=sx; x<ex; x++ )
									{
										int c = source.read(x_index>>16);
										if( c != transparent_color ) dest.write(x, (char) pal.read(c));
										x_index += dx;
									}

									y_index += dy;
								}
							}
						}
					}

					/* case 1b: TRANSPARENCY_PEN_RAW */
					if (transparency == TRANSPARENCY_PEN_RAW)
					{
						if (pri_buffer != null)
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UShortPtr dest = new UShortPtr(dest_bmp.line[y]);
								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color )
									{
										if (((1 << pri.read(x)) & pri_mask) == 0)
											dest.write(x, (char) (color + c));
										pri.write(x, 31);
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
						else
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UShortPtr dest = new UShortPtr(dest_bmp.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color ) dest.write(x, (char) (color + c));
									x_index += dx;
								}

								y_index += dy;
							}
						}
					}

					/* case 1c: TRANSPARENCY_BLEND_RAW */
					if (transparency == TRANSPARENCY_BLEND_RAW)
					{
						if (pri_buffer != null)
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UShortPtr dest = new UShortPtr(dest_bmp.line[y]);
								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color )
									{
										if (((1 << pri.read(x)) & pri_mask) == 0)
											dest.write(x, (char) (dest.read(x) | color + c));
										pri.write(x, 31);
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
						else
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UShortPtr dest = new UShortPtr(dest_bmp.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color ) dest.write(x, (char) (dest.read(x) | color + c));
									x_index += dx;
								}

								y_index += dy;
							}
						}
					}

					/* case 2: TRANSPARENCY_PENS */
					if (transparency == TRANSPARENCY_PENS)
					{
						if (pri_buffer != null)
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UShortPtr dest = new UShortPtr(dest_bmp.line[y]);
								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if (((1 << c) & transparent_color) == 0)
									{
										if (((1 << pri.read(x)) & pri_mask) == 0)
											dest.write(x, (char) pal.read(c));
										pri.write(x, 31);
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
						else
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UShortPtr dest = new UShortPtr(dest_bmp.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if (((1 << c) & transparent_color) == 0)
										dest.write(x, (char) pal.read(c));
									x_index += dx;
								}

								y_index += dy;
							}
						}
					}

					/* case 3: TRANSPARENCY_COLOR */
					else if (transparency == TRANSPARENCY_COLOR)
					{
						if (pri_buffer != null)
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UShortPtr dest = new UShortPtr(dest_bmp.line[y]);
								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = pal.read(source.read(x_index>>16));
									if( c != transparent_color )
									{
										if (((1 << pri.read(x)) & pri_mask) == 0)
											dest.write(x, (char) c);
										pri.write(x, 31);
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
						else
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UShortPtr dest = new UShortPtr(dest_bmp.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = pal.read(source.read(x_index>>16));
									if( c != transparent_color ) dest.write(x, (char) c);
									x_index += dx;
								}

								y_index += dy;
							}
						}
					}

					/* case 4: TRANSPARENCY_PEN_TABLE */
					if (transparency == TRANSPARENCY_PEN_TABLE)
					{
						if (pri_buffer != null)
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UShortPtr dest = new UShortPtr(dest_bmp.line[y]);
								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color )
									{
										if (((1 << pri.read(x)) & pri_mask) == 0)
										{
											switch(gfx_drawmode_table[c])
											{
											case DRAWMODE_SOURCE:
												dest.write(x, (char) pal.read(c));
												break;
											case DRAWMODE_SHADOW:
												dest.write(x, palette_shadow_table[dest.read(x)]);
												break;
											}
										}
										pri.write(x, 31);
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
						else
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UShortPtr dest = new UShortPtr(dest_bmp.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color )
									{
										switch(gfx_drawmode_table[c])
										{
										case DRAWMODE_SOURCE:
											dest.write(x, (char) pal.read(c));
											break;
										case DRAWMODE_SHADOW:
											dest.write(x, palette_shadow_table[dest.read(x)]);
											break;
										}
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
					}

					/* case 4b: TRANSPARENCY_PEN_TABLE_RAW */
					if (transparency == TRANSPARENCY_PEN_TABLE_RAW)
					{
						if (pri_buffer != null)
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UShortPtr dest = new UShortPtr(dest_bmp.line[y]);
								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color )
									{
										if (((1 << pri.read(x)) & pri_mask) == 0)
										{
											switch(gfx_drawmode_table[c])
											{
											case DRAWMODE_SOURCE:
												dest.write(x, (char) (color + c));
												break;
											case DRAWMODE_SHADOW:
												dest.write(x, palette_shadow_table[dest.read(x)]);
												break;
											}
										}
										pri.write(x, 31);
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
						else
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UShortPtr dest = new UShortPtr(dest_bmp.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color )
									{
										switch(gfx_drawmode_table[c])
										{
										case DRAWMODE_SOURCE:
											dest.write(x, (char) (color + c));
											break;
										case DRAWMODE_SHADOW:
											dest.write(x, palette_shadow_table[dest.read(x)]);
											break;
										}
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
					}

					/* case 5: TRANSPARENCY_ALPHAONE */
					if (transparency == TRANSPARENCY_ALPHAONE)
					{
						if (pri_buffer != null)
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UShortPtr dest = new UShortPtr(dest_bmp.line[y]);
								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color )
									{
										if (((1 << pri.read(x)) & pri_mask) == 0)
										{
											if( c == alphapen){
                                                                                            throw new UnsupportedOperationException("Not implemented!!!!");
/*TODO*///												dest.write(x, alpha_blend16(dest.read(x), pal.read(c)));
                                                                                        } else {
												dest.write(x, (char) pal.read(c));
                                                                                        }
										}
										pri.write(x, 31);
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
						else
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UShortPtr dest = new UShortPtr(dest_bmp.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color )
									{
										if( c == alphapen) {
                                                                                    throw new UnsupportedOperationException("Not implemented!!!!");
/*TODO*///											dest.write(x, alpha_blend16(dest.read(x), pal.read(c)));
                                                                                } else {
											dest.write(x, (char) pal.read(c));
                                                                                }
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
					}

					/* case 6: TRANSPARENCY_ALPHA */
					if (transparency == TRANSPARENCY_ALPHA)
					{
						if (pri_buffer != null)
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UShortPtr dest = new UShortPtr(dest_bmp.line[y]);
								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color )
									{
										if (((1 << pri.read(x)) & pri_mask) == 0){
                                                                                    throw new UnsupportedOperationException("Not implemented!!!!");
/*TODO*///											dest.write(x, alpha_blend16(dest.read(x), pal.read(c)));
                                                                                }
										pri.write(x, 31);
									}
									x_index += dx;
								}

								y_index += dy;
							}
						}
						else
						{
							for( y=sy; y<ey; y++ )
							{
								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
								UShortPtr dest = new UShortPtr(dest_bmp.line[y]);

								int x, x_index = x_index_base;
								for( x=sx; x<ex; x++ )
								{
									int c = source.read(x_index>>16);
									if( c != transparent_color ) {
                                                                            throw new UnsupportedOperationException("Not implemented!!!!");
/*TODO*///                                                                            dest.write(x, alpha_blend16(dest.read(x), pal.read(c)));
                                                                        };
									x_index += dx;
								}

								y_index += dy;
							}
						}
					}
				}
			}
		}
	}
	else
	{
            throw new UnsupportedOperationException("Not implemented!!!!");
/*TODO*///if( gfx!=null && gfx.colortable!=null )
/*TODO*///		{
/*TODO*///			IntArray pal = new IntArray(gfx.colortable, gfx.color_granularity * (color % gfx.total_colors)); /* ASG 980209 */
/*TODO*///			int source_base = (code % gfx.total_elements) * gfx.height;
/*TODO*///
/*TODO*///			int sprite_screen_height = (scaley*gfx.height+0x8000)>>16;
/*TODO*///			int sprite_screen_width = (scalex*gfx.width+0x8000)>>16;
/*TODO*///
/*TODO*///			if (sprite_screen_width!=0 && sprite_screen_height!=0)
/*TODO*///			{
/*TODO*///				/* compute sprite increment per screen pixel */
/*TODO*///				int dx = (gfx.width<<16)/sprite_screen_width;
/*TODO*///				int dy = (gfx.height<<16)/sprite_screen_height;
/*TODO*///
/*TODO*///				int ex = sx+sprite_screen_width;
/*TODO*///				int ey = sy+sprite_screen_height;
/*TODO*///
/*TODO*///				int x_index_base;
/*TODO*///				int y_index;
/*TODO*///
/*TODO*///				if( flipx != 0 )
/*TODO*///				{
/*TODO*///					x_index_base = (sprite_screen_width-1)*dx;
/*TODO*///					dx = -dx;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					x_index_base = 0;
/*TODO*///				}
/*TODO*///
/*TODO*///				if( flipy != 0 )
/*TODO*///				{
/*TODO*///					y_index = (sprite_screen_height-1)*dy;
/*TODO*///					dy = -dy;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					y_index = 0;
/*TODO*///				}
/*TODO*///
/*TODO*///				if( clip != null )
/*TODO*///				{
/*TODO*///					if( sx < clip.min_x)
/*TODO*///					{ /* clip left */
/*TODO*///						int pixels = clip.min_x-sx;
/*TODO*///						sx += pixels;
/*TODO*///						x_index_base += pixels*dx;
/*TODO*///					}
/*TODO*///					if( sy < clip.min_y )
/*TODO*///					{ /* clip top */
/*TODO*///						int pixels = clip.min_y-sy;
/*TODO*///						sy += pixels;
/*TODO*///						y_index += pixels*dy;
/*TODO*///					}
/*TODO*///					/* NS 980211 - fixed incorrect clipping */
/*TODO*///					if( ex > clip.max_x+1 )
/*TODO*///					{ /* clip right */
/*TODO*///						int pixels = ex-clip.max_x-1;
/*TODO*///						ex -= pixels;
/*TODO*///					}
/*TODO*///					if( ey > clip.max_y+1 )
/*TODO*///					{ /* clip bottom */
/*TODO*///						int pixels = ey-clip.max_y-1;
/*TODO*///						ey -= pixels;
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				if( ex>sx )
/*TODO*///				{ /* skip if inner loop doesn't draw anything */
/*TODO*///					int y;
/*TODO*///
/*TODO*///					/* case 1: TRANSPARENCY_PEN */
/*TODO*///					if (transparency == TRANSPARENCY_PEN)
/*TODO*///					{
/*TODO*///						if (pri_buffer != null)
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source.read(x_index>>16);
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest.write(x, (char) pal.read(c));
/*TODO*///										pri.write(x, 31);
/*TODO*///									}
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source.read(x_index>>16);
/*TODO*///									if( c != transparent_color ) dest.write(x, (char) pal.read(c));
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					/* case 1b: TRANSPARENCY_PEN_RAW */
/*TODO*///					if (transparency == TRANSPARENCY_PEN_RAW)
/*TODO*///					{
/*TODO*///						if (pri_buffer)
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source.read(x_index>>16);
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest.write(x, (char) (color + c));
/*TODO*///										pri.write(x, 31);
/*TODO*///									}
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source.read(x_index>>16);
/*TODO*///									if( c != transparent_color ) dest.write(x, (char) (color + c));
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					/* case 1c: TRANSPARENCY_BLEND_RAW */
/*TODO*///					if (transparency == TRANSPARENCY_BLEND_RAW)
/*TODO*///					{
/*TODO*///						if (pri_buffer)
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source.read(x_index>>16);
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest.write(x, (char) (dest.read(x) | color + c));
/*TODO*///										pri.write(x, 31);
/*TODO*///									}
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source.read(x_index>>16);
/*TODO*///									if( c != transparent_color ) dest.write(x, (char) (dest.read(x) | color + c));
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					/* case 2: TRANSPARENCY_PENS */
/*TODO*///					if (transparency == TRANSPARENCY_PENS)
/*TODO*///					{
/*TODO*///						if (pri_buffer)
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source.read(x_index>>16);
/*TODO*///									if (((1 << c) & transparent_color) == 0)
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest.write(x, (char) pal.read(c));
/*TODO*///										pri.write(x, 31);
/*TODO*///									}
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source.read(x_index>>16);
/*TODO*///									if (((1 << c) & transparent_color) == 0)
/*TODO*///										dest.write(x, (char) pal.read(c));
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					/* case 3: TRANSPARENCY_COLOR */
/*TODO*///					else if (transparency == TRANSPARENCY_COLOR)
/*TODO*///					{
/*TODO*///						if (pri_buffer)
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = pal.read(source.read(x_index>>16));
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest[x] = c;
/*TODO*///										pri.write(x, 31);
/*TODO*///									}
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = pal.read(source.read(x_index>>16));
/*TODO*///									if( c != transparent_color ) dest[x] = c;
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					/* case 4: TRANSPARENCY_PEN_TABLE */
/*TODO*///					if (transparency == TRANSPARENCY_PEN_TABLE)
/*TODO*///					{
/*TODO*///						if (pri_buffer)
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source.read(x_index>>16);
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///										{
/*TODO*///											switch(gfx_drawmode_table[c])
/*TODO*///											{
/*TODO*///											case DRAWMODE_SOURCE:
/*TODO*///												dest.write(x, (char) pal.read(c));
/*TODO*///												break;
/*TODO*///											case DRAWMODE_SHADOW:
/*TODO*///												dest[x] = palette_shadow_table[dest[x]];
/*TODO*///												break;
/*TODO*///											}
/*TODO*///										}
/*TODO*///										pri.write(x, 31);
/*TODO*///									}
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source.read(x_index>>16);
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										switch(gfx_drawmode_table[c])
/*TODO*///										{
/*TODO*///										case DRAWMODE_SOURCE:
/*TODO*///											dest.write(x, (char) pal.read(c));
/*TODO*///											break;
/*TODO*///										case DRAWMODE_SHADOW:
/*TODO*///											dest[x] = palette_shadow_table[dest[x]];
/*TODO*///											break;
/*TODO*///										}
/*TODO*///									}
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					/* case 4b: TRANSPARENCY_PEN_TABLE_RAW */
/*TODO*///					if (transparency == TRANSPARENCY_PEN_TABLE_RAW)
/*TODO*///					{
/*TODO*///						if (pri_buffer)
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source.read(x_index>>16);
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///										{
/*TODO*///											switch(gfx_drawmode_table[c])
/*TODO*///											{
/*TODO*///											case DRAWMODE_SOURCE:
/*TODO*///												dest.write(x, (char) (color + c));
/*TODO*///												break;
/*TODO*///											case DRAWMODE_SHADOW:
/*TODO*///												dest[x] = palette_shadow_table[dest[x]];
/*TODO*///												break;
/*TODO*///											}
/*TODO*///										}
/*TODO*///										pri.write(x, 31);
/*TODO*///									}
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source.read(x_index>>16);
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										switch(gfx_drawmode_table[c])
/*TODO*///										{
/*TODO*///										case DRAWMODE_SOURCE:
/*TODO*///											dest.write(x, (char) (color + c));
/*TODO*///											break;
/*TODO*///										case DRAWMODE_SHADOW:
/*TODO*///											dest[x] = palette_shadow_table[dest[x]];
/*TODO*///											break;
/*TODO*///										}
/*TODO*///									}
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///
/*TODO*///					/* case 5: TRANSPARENCY_ALPHAONE */
/*TODO*///					if (transparency == TRANSPARENCY_ALPHAONE)
/*TODO*///					{
/*TODO*///						if (pri_buffer)
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source.read(x_index>>16);
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///										{
/*TODO*///											if( c == alphapen)
/*TODO*///												dest[x] = alpha_blend32(dest[x], pal[c]);
/*TODO*///											else
/*TODO*///												dest.write(x, (char) pal.read(c));
/*TODO*///										}
/*TODO*///										pri.write(x, 31);
/*TODO*///									}
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source.read(x_index>>16);
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if( c == alphapen)
/*TODO*///											dest[x] = alpha_blend32(dest[x], pal[c]);
/*TODO*///										else
/*TODO*///											dest.write(x, (char) pal.read(c));
/*TODO*///									}
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					/* case 6: TRANSPARENCY_ALPHA */
/*TODO*///					if (transparency == TRANSPARENCY_ALPHA)
/*TODO*///					{
/*TODO*///						if (pri_buffer)
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///								UBytePtr pri = new UBytePtr(pri_buffer.line[y]);
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source.read(x_index>>16);
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest[x] = alpha_blend32(dest[x], pal[c]);
/*TODO*///										pri.write(x, 31);
/*TODO*///									}
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UBytePtr source = new UBytePtr(gfx.gfxdata, (source_base+(y_index>>16)) * gfx.line_modulo);
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp.line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source.read(x_index>>16);
/*TODO*///									if( c != transparent_color ) dest[x] = alpha_blend32(dest[x], pal[c]);
/*TODO*///									x_index += dx;
/*TODO*///								}
/*TODO*///
/*TODO*///								y_index += dy;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}		
	}
    }

    public static void drawgfxzoom( osd_bitmap dest_bmp, GfxElement gfx,
                    int code, int color,int flipx,int flipy,int sx,int sy,
                    rectangle clip,int transparency,int transparent_color,int scalex, int scaley)
    {        
    /*TODO*///	profiler_mark(PROFILER_DRAWGFX);
    	common_drawgfxzoom(dest_bmp,gfx,code,color,flipx,flipy,sx,sy,
    			clip,transparency,transparent_color,scalex,scaley,null,0);
    /*TODO*///	profiler_mark(PROFILER_END);
    }
    
    public static void pdrawgfxzoom( osd_bitmap dest_bmp, GfxElement gfx,
                    int code, int color,int flipx,int flipy,int sx,int sy,
                    rectangle clip,int transparency,int transparent_color,int scalex, int scaley,
                    int priority_mask)
    {
        
    /*TODO*///	profiler_mark(PROFILER_DRAWGFX);
    	common_drawgfxzoom(dest_bmp,gfx,code,color,flipx,flipy,sx,sy,
    			clip,transparency,transparent_color,scalex,scaley,priority_bitmap,priority_mask | (1<<31));
    /*TODO*///	profiler_mark(PROFILER_END);
    }
/*TODO*///void mdrawgfxzoom( struct osd_bitmap *dest_bmp,const struct GfxElement *gfx,
/*TODO*///		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color,int scalex, int scaley,
/*TODO*///		UINT32 priority_mask)
/*TODO*///{
/*TODO*///	profiler_mark(PROFILER_DRAWGFX);
/*TODO*///	common_drawgfxzoom(dest_bmp,gfx,code,color,flipx,flipy,sx,sy,
/*TODO*///			clip,transparency,transparent_color,scalex,scaley,priority_bitmap,priority_mask);
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///}
/*TODO*///
/*TODO*///void plot_pixel2(struct osd_bitmap *bitmap1,struct osd_bitmap *bitmap2,int x,int y,int pen)
/*TODO*///{
/*TODO*///	plot_pixel(bitmap1, x, y, pen);
/*TODO*///	plot_pixel(bitmap2, x, y, pen);
/*TODO*///}
/*TODO*///
    public static plot_pixel_procPtr pp_8_nd = new plot_pixel_procPtr() {
        public void handler(osd_bitmap b, int x, int y,/*UINT32*/ int p) {
            b.line[y].write(x, p);
        }
    };
    public static plot_pixel_procPtr pp_8_nd_fx = new plot_pixel_procPtr() {
        public void handler(osd_bitmap b, int x, int y,/*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//((UINT8 *)b->line[y])[b->width-1-x] = p; 
        }
    };
    public static plot_pixel_procPtr pp_8_nd_fy = new plot_pixel_procPtr() {
        public void handler(osd_bitmap b, int x, int y,/*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//((UINT8 *)b->line[b->height-1-y])[x] = p; 
        }
    };
    public static plot_pixel_procPtr pp_8_nd_fxy = new plot_pixel_procPtr() {
        public void handler(osd_bitmap b, int x, int y,/*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//((UINT8 *)b->line[b->height-1-y])[b->width-1-x] = p; 
        }
    };
    public static plot_pixel_procPtr pp_8_nd_s = new plot_pixel_procPtr() {
        public void handler(osd_bitmap b, int x, int y,/*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//((UINT8 *)b->line[x])[y] = p; 
        }
    };
    public static plot_pixel_procPtr pp_8_nd_fx_s = new plot_pixel_procPtr() {
        public void handler(osd_bitmap b, int x, int y,/*UINT32*/ int p) {
            b.line[x].write(b.width - 1 - y, p);
        }
    };
    public static plot_pixel_procPtr pp_8_nd_fy_s = new plot_pixel_procPtr() {
        public void handler(osd_bitmap b, int x, int y,/*UINT32*/ int p) {
            b.line[b.height - 1 - x].write(y, p);
        }
    };
    public static plot_pixel_procPtr pp_8_nd_fxy_s = new plot_pixel_procPtr() {
        public void handler(osd_bitmap b, int x, int y,/*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//((UINT8 *)b->line[b->height-1-x])[b->width-1-y] = p; 
        }
    };

    public static plot_pixel_procPtr pp_8_d = new plot_pixel_procPtr() {
        public void handler(osd_bitmap b, int x, int y,/*UINT32*/ int p) {
            b.line[y].write(x, p);
            osd_mark_dirty(x, y, x, y);
        }
    };
    public static plot_pixel_procPtr pp_8_d_fx = new plot_pixel_procPtr() {
        public void handler(osd_bitmap b, int x, int y,/*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//x = b->width-1-x; ((UINT8 *)b->line[y])[x] = p; osd_mark_dirty(x,y,x,y); 
        }
    };
    public static plot_pixel_procPtr pp_8_d_fy = new plot_pixel_procPtr() {
        public void handler(osd_bitmap b, int x, int y,/*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//y = b->height-1-y; ((UINT8 *)b->line[y])[x] = p; osd_mark_dirty(x,y,x,y); 
        }
    };
    public static plot_pixel_procPtr pp_8_d_fxy = new plot_pixel_procPtr() {
        public void handler(osd_bitmap b, int x, int y,/*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");// x = b->width-1-x; y = b->height-1-y; ((UINT8 *)b->line[y])[x] = p; osd_mark_dirty(x,y,x,y); 
        }
    };
    public static plot_pixel_procPtr pp_8_d_s = new plot_pixel_procPtr() {
        public void handler(osd_bitmap b, int x, int y,/*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//((UINT8 *)b->line[x])[y] = p; osd_mark_dirty(y,x,y,x); 
        }
    };
    public static plot_pixel_procPtr pp_8_d_fx_s = new plot_pixel_procPtr() {
        public void handler(osd_bitmap b, int x, int y,/*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//y = b->width-1-y; ((UINT8 *)b->line[x])[y] = p; osd_mark_dirty(y,x,y,x); 
        }
    };
    public static plot_pixel_procPtr pp_8_d_fy_s = new plot_pixel_procPtr() {
        public void handler(osd_bitmap b, int x, int y,/*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//x = b->height-1-x; ((UINT8 *)b->line[x])[y] = p; osd_mark_dirty(y,x,y,x); 
        }
    };
    public static plot_pixel_procPtr pp_8_d_fxy_s = new plot_pixel_procPtr() {
        public void handler(osd_bitmap b, int x, int y,/*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//x = b->height-1-x; y = b->width-1-y; ((UINT8 *)b->line[x])[y] = p; osd_mark_dirty(y,x,y,x); 
        }
    };

    /*TODO*///static void pp_16_nd(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT16 *)b->line[y])[x] = p; }
/*TODO*///static void pp_16_nd_fx(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT16 *)b->line[y])[b->width-1-x] = p; }
/*TODO*///static void pp_16_nd_fy(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT16 *)b->line[b->height-1-y])[x] = p; }
/*TODO*///static void pp_16_nd_fxy(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT16 *)b->line[b->height-1-y])[b->width-1-x] = p; }
/*TODO*///static void pp_16_nd_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT16 *)b->line[x])[y] = p; }
/*TODO*///static void pp_16_nd_fx_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT16 *)b->line[x])[b->width-1-y] = p; }
/*TODO*///static void pp_16_nd_fy_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT16 *)b->line[b->height-1-x])[y] = p; }
/*TODO*///static void pp_16_nd_fxy_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT16 *)b->line[b->height-1-x])[b->width-1-y] = p; }
/*TODO*///
/*TODO*///static void pp_16_d(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT16 *)b->line[y])[x] = p; osd_mark_dirty(x,y,x,y); }
/*TODO*///static void pp_16_d_fx(struct osd_bitmap *b,int x,int y,UINT32 p)  { x = b->width-1-x;  ((UINT16 *)b->line[y])[x] = p; osd_mark_dirty(x,y,x,y); }
/*TODO*///static void pp_16_d_fy(struct osd_bitmap *b,int x,int y,UINT32 p)  { y = b->height-1-y; ((UINT16 *)b->line[y])[x] = p; osd_mark_dirty(x,y,x,y); }
/*TODO*///static void pp_16_d_fxy(struct osd_bitmap *b,int x,int y,UINT32 p)  { x = b->width-1-x; y = b->height-1-y; ((UINT16 *)b->line[y])[x] = p; osd_mark_dirty(x,y,x,y); }
/*TODO*///static void pp_16_d_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT16 *)b->line[x])[y] = p; osd_mark_dirty(y,x,y,x); }
/*TODO*///static void pp_16_d_fx_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { y = b->width-1-y; ((UINT16 *)b->line[x])[y] = p; osd_mark_dirty(y,x,y,x); }
/*TODO*///static void pp_16_d_fy_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { x = b->height-1-x; ((UINT16 *)b->line[x])[y] = p; osd_mark_dirty(y,x,y,x); }
/*TODO*///static void pp_16_d_fxy_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { x = b->height-1-x; y = b->width-1-y; ((UINT16 *)b->line[x])[y] = p; osd_mark_dirty(y,x,y,x); }
/*TODO*///
/*TODO*///static void pp_32_nd(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT32 *)b->line[y])[x] = p; }
/*TODO*///static void pp_32_nd_fx(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT32 *)b->line[y])[b->width-1-x] = p; }
/*TODO*///static void pp_32_nd_fy(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT32 *)b->line[b->height-1-y])[x] = p; }
/*TODO*///static void pp_32_nd_fxy(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT32 *)b->line[b->height-1-y])[b->width-1-x] = p; }
/*TODO*///static void pp_32_nd_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT32 *)b->line[x])[y] = p; }
/*TODO*///static void pp_32_nd_fx_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT32 *)b->line[x])[b->width-1-y] = p; }
/*TODO*///static void pp_32_nd_fy_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT32 *)b->line[b->height-1-x])[y] = p; }
/*TODO*///static void pp_32_nd_fxy_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT32 *)b->line[b->height-1-x])[b->width-1-y] = p; }
/*TODO*///
/*TODO*///static void pp_32_d(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT32 *)b->line[y])[x] = p; osd_mark_dirty(x,y,x,y); }
/*TODO*///static void pp_32_d_fx(struct osd_bitmap *b,int x,int y,UINT32 p)  { x = b->width-1-x;  ((UINT32 *)b->line[y])[x] = p; osd_mark_dirty(x,y,x,y); }
/*TODO*///static void pp_32_d_fy(struct osd_bitmap *b,int x,int y,UINT32 p)  { y = b->height-1-y; ((UINT32 *)b->line[y])[x] = p; osd_mark_dirty(x,y,x,y); }
/*TODO*///static void pp_32_d_fxy(struct osd_bitmap *b,int x,int y,UINT32 p)  { x = b->width-1-x; y = b->height-1-y; ((UINT32 *)b->line[y])[x] = p; osd_mark_dirty(x,y,x,y); }
/*TODO*///static void pp_32_d_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { ((UINT32 *)b->line[x])[y] = p; osd_mark_dirty(y,x,y,x); }
/*TODO*///static void pp_32_d_fx_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { y = b->width-1-y; ((UINT32 *)b->line[x])[y] = p; osd_mark_dirty(y,x,y,x); }
/*TODO*///static void pp_32_d_fy_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { x = b->height-1-x; ((UINT32 *)b->line[x])[y] = p; osd_mark_dirty(y,x,y,x); }
/*TODO*///static void pp_32_d_fxy_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { x = b->height-1-x; y = b->width-1-y; ((UINT32 *)b->line[x])[y] = p; osd_mark_dirty(y,x,y,x); }
/*TODO*///
/*TODO*///
    public static read_pixel_procPtr rp_8 = new read_pixel_procPtr() {
        public int handler(osd_bitmap b, int x, int y) {
            return b.line[y].read(x);
        }
    };
    public static read_pixel_procPtr rp_8_fx = new read_pixel_procPtr() {
        public int handler(osd_bitmap bitmap, int x, int y) {
            throw new UnsupportedOperationException("unsupported");//return ((UINT8 *)b->line[y])[b->width-1-x]; 
        }
    };
    public static read_pixel_procPtr rp_8_fy = new read_pixel_procPtr() {
        public int handler(osd_bitmap bitmap, int x, int y) {
            throw new UnsupportedOperationException("unsupported");//return ((UINT8 *)b->line[b->height-1-y])[x]; 
        }
    };
    public static read_pixel_procPtr rp_8_fxy = new read_pixel_procPtr() {
        public int handler(osd_bitmap bitmap, int x, int y) {
            throw new UnsupportedOperationException("unsupported");//return ((UINT8 *)b->line[b->height-1-y])[b->width-1-x]; 
        }
    };
    public static read_pixel_procPtr rp_8_s = new read_pixel_procPtr() {
        public int handler(osd_bitmap bitmap, int x, int y) {
            throw new UnsupportedOperationException("unsupported");//return ((UINT8 *)b->line[x])[y]; 
        }
    };
    public static read_pixel_procPtr rp_8_fx_s = new read_pixel_procPtr() {
        public int handler(osd_bitmap b, int x, int y) {
            return b.line[x].read(b.width - 1 - y);
        }
    };
    public static read_pixel_procPtr rp_8_fy_s = new read_pixel_procPtr() {
        public int handler(osd_bitmap b, int x, int y) {
            return b.line[b.height - 1 - x].read(y);
        }
    };
    public static read_pixel_procPtr rp_8_fxy_s = new read_pixel_procPtr() {
        public int handler(osd_bitmap bitmap, int x, int y) {
            throw new UnsupportedOperationException("unsupported");//return ((UINT8 *)b->line[b->height-1-x])[b->width-1-y]; 
        }
    };
    /*TODO*///
/*TODO*///static int rp_16(struct osd_bitmap *b,int x,int y)  { return ((UINT16 *)b->line[y])[x]; }
/*TODO*///static int rp_16_fx(struct osd_bitmap *b,int x,int y)  { return ((UINT16 *)b->line[y])[b->width-1-x]; }
/*TODO*///static int rp_16_fy(struct osd_bitmap *b,int x,int y)  { return ((UINT16 *)b->line[b->height-1-y])[x]; }
/*TODO*///static int rp_16_fxy(struct osd_bitmap *b,int x,int y)  { return ((UINT16 *)b->line[b->height-1-y])[b->width-1-x]; }
/*TODO*///static int rp_16_s(struct osd_bitmap *b,int x,int y)  { return ((UINT16 *)b->line[x])[y]; }
/*TODO*///static int rp_16_fx_s(struct osd_bitmap *b,int x,int y)  { return ((UINT16 *)b->line[x])[b->width-1-y]; }
/*TODO*///static int rp_16_fy_s(struct osd_bitmap *b,int x,int y)  { return ((UINT16 *)b->line[b->height-1-x])[y]; }
/*TODO*///static int rp_16_fxy_s(struct osd_bitmap *b,int x,int y)  { return ((UINT16 *)b->line[b->height-1-x])[b->width-1-y]; }
/*TODO*///
/*TODO*///static int rp_32(struct osd_bitmap *b,int x,int y)  { return ((UINT32 *)b->line[y])[x]; }
/*TODO*///static int rp_32_fx(struct osd_bitmap *b,int x,int y)  { return ((UINT32 *)b->line[y])[b->width-1-x]; }
/*TODO*///static int rp_32_fy(struct osd_bitmap *b,int x,int y)  { return ((UINT32 *)b->line[b->height-1-y])[x]; }
/*TODO*///static int rp_32_fxy(struct osd_bitmap *b,int x,int y)  { return ((UINT32 *)b->line[b->height-1-y])[b->width-1-x]; }
/*TODO*///static int rp_32_s(struct osd_bitmap *b,int x,int y)  { return ((UINT32 *)b->line[x])[y]; }
/*TODO*///static int rp_32_fx_s(struct osd_bitmap *b,int x,int y)  { return ((UINT32 *)b->line[x])[b->width-1-y]; }
/*TODO*///static int rp_32_fy_s(struct osd_bitmap *b,int x,int y)  { return ((UINT32 *)b->line[b->height-1-x])[y]; }
/*TODO*///static int rp_32_fxy_s(struct osd_bitmap *b,int x,int y)  { return ((UINT32 *)b->line[b->height-1-x])[b->width-1-y]; }
/*TODO*///
/*TODO*///
    public static plot_box_procPtr pb_8_nd = new plot_box_procPtr() {
        public void handler(osd_bitmap b, int x, int y, int w, int h, /*UINT32*/ int p) {
            int t = x;
            while (h-- > 0) {
                int c = w;
                x = t;
                while (c-- > 0) {
                    b.line[y].write(x, p);
                    x++;
                }
                y++;
            }
        }
    };
    public static plot_box_procPtr pb_8_nd_fx = new plot_box_procPtr() {
        public void handler(osd_bitmap b, int x, int y, int w, int h, /*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//int t=b->width-1-x; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT8 *)b->line[y])[x] = p; x--; } y++; } 
        }
    };
    public static plot_box_procPtr pb_8_nd_fy = new plot_box_procPtr() {
        public void handler(osd_bitmap b, int x, int y, int w, int h, /*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//int t=x; y = b->height-1-y; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT8 *)b->line[y])[x] = p; x++; } y--; } 
        }
    };
    public static plot_box_procPtr pb_8_nd_fxy = new plot_box_procPtr() {
        public void handler(osd_bitmap b, int x, int y, int w, int h, /*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//int t=b->width-1-x; y = b->height-1-y; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT8 *)b->line[y])[x] = p; x--; } y--; } 
        }
    };
    public static plot_box_procPtr pb_8_nd_s = new plot_box_procPtr() {
        public void handler(osd_bitmap b, int x, int y, int w, int h, /*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//int t=x; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT8 *)b->line[x])[y] = p; x++; } y++; } 
        }
    };
    public static plot_box_procPtr pb_8_nd_fx_s = new plot_box_procPtr() {
        public void handler(osd_bitmap b, int x, int y, int w, int h, /*UINT32*/ int p) {
            int t = x;
            y = b.width - 1 - y;
            while (h-- > 0) {
                int c = w;
                x = t;
                while (c-- > 0) {
                    b.line[x].write(y, p);
                    x++;
                }
                y--;
            }
        }
    };
    public static plot_box_procPtr pb_8_nd_fy_s = new plot_box_procPtr() {
        public void handler(osd_bitmap b, int x, int y, int w, int h, /*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//int t=b->height-1-x; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT8 *)b->line[x])[y] = p; x--; } y++; } 
        }
    };
    public static plot_box_procPtr pb_8_nd_fxy_s = new plot_box_procPtr() {
        public void handler(osd_bitmap b, int x, int y, int w, int h, /*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//int t=b->height-1-x; y = b->width-1-y; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT8 *)b->line[x])[y] = p; x--; } y--; } 
        }
    };

    public static plot_box_procPtr pb_8_d = new plot_box_procPtr() {
        public void handler(osd_bitmap b, int x, int y, int w, int h, /*UINT32*/ int p) {
            int t = x;
            osd_mark_dirty(t, y, t + w - 1, y + h - 1);
            while (h-- > 0) {
                int c = w;
                x = t;
                while (c-- > 0) {
                    b.line[y].write(x, p);
                    x++;
                }
                y++;
            }
        }
    };
    public static plot_box_procPtr pb_8_d_fx = new plot_box_procPtr() {
        public void handler(osd_bitmap b, int x, int y, int w, int h, /*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//int t=b->width-1-x;  osd_mark_dirty(t-w+1,y,t,y+h-1); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT8 *)b->line[y])[x] = p; x--; } y++; } 
        }
    };
    public static plot_box_procPtr pb_8_d_fy = new plot_box_procPtr() {
        public void handler(osd_bitmap b, int x, int y, int w, int h, /*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//int t=x; y = b->height-1-y; osd_mark_dirty(t,y-h+1,t+w-1,y); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT8 *)b->line[y])[x] = p; x++; } y--; } 
        }
    };
    public static plot_box_procPtr pb_8_d_fxy = new plot_box_procPtr() {
        public void handler(osd_bitmap b, int x, int y, int w, int h, /*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//int t=b->width-1-x; y = b->height-1-y; osd_mark_dirty(t-w+1,y-h+1,t,y); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT8 *)b->line[y])[x] = p; x--; } y--; }
        }
    };
    public static plot_box_procPtr pb_8_d_s = new plot_box_procPtr() {
        public void handler(osd_bitmap b, int x, int y, int w, int h, /*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//int t=x; osd_mark_dirty(y,t,y+h-1,t+w-1); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT8 *)b->line[x])[y] = p; x++; } y++; } 
        }
    };
    public static plot_box_procPtr pb_8_d_fx_s = new plot_box_procPtr() {
        public void handler(osd_bitmap b, int x, int y, int w, int h, /*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//int t=x; y = b->width-1-y;  osd_mark_dirty(y-h+1,t,y,t+w-1); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT8 *)b->line[x])[y] = p; x++; } y--; } 
        }
    };
    public static plot_box_procPtr pb_8_d_fy_s = new plot_box_procPtr() {
        public void handler(osd_bitmap b, int x, int y, int w, int h, /*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//int t=b->height-1-x; osd_mark_dirty(y,t-w+1,y+h-1,t); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT8 *)b->line[x])[y] = p; x--; } y++; } 
        }
    };
    public static plot_box_procPtr pb_8_d_fxy_s = new plot_box_procPtr() {
        public void handler(osd_bitmap b, int x, int y, int w, int h, /*UINT32*/ int p) {
            throw new UnsupportedOperationException("unsupported");//int t=b->height-1-x; y = b->width-1-y; osd_mark_dirty(y-h+1,t-w+1,y,t); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT8 *)b->line[x])[y] = p; x--; } y--; } 
        }
    };
    /*TODO*///static void pb_16_nd(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT16 *)b->line[y])[x] = p; x++; } y++; } }
/*TODO*///static void pb_16_nd_fx(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->width-1-x; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT16 *)b->line[y])[x] = p; x--; } y++; } }
/*TODO*///static void pb_16_nd_fy(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; y = b->height-1-y; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT16 *)b->line[y])[x] = p; x++; } y--; } }
/*TODO*///static void pb_16_nd_fxy(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->width-1-x; y = b->height-1-y; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT16 *)b->line[y])[x] = p; x--; } y--; } }
/*TODO*///static void pb_16_nd_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT16 *)b->line[x])[y] = p; x++; } y++; } }
/*TODO*///static void pb_16_nd_fx_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; y = b->width-1-y; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT16 *)b->line[x])[y] = p; x++; } y--; } }
/*TODO*///static void pb_16_nd_fy_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->height-1-x; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT16 *)b->line[x])[y] = p; x--; } y++; } }
/*TODO*///static void pb_16_nd_fxy_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->height-1-x; y = b->width-1-y; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT16 *)b->line[x])[y] = p; x--; } y--; } }
/*TODO*///
/*TODO*///static void pb_16_d(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; osd_mark_dirty(t,y,t+w-1,y+h-1); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT16 *)b->line[y])[x] = p; x++; } y++; } }
/*TODO*///static void pb_16_d_fx(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->width-1-x;  osd_mark_dirty(t-w+1,y,t,y+h-1); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT16 *)b->line[y])[x] = p; x--; } y++; } }
/*TODO*///static void pb_16_d_fy(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; y = b->height-1-y; osd_mark_dirty(t,y-h+1,t+w-1,y); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT16 *)b->line[y])[x] = p; x++; } y--; } }
/*TODO*///static void pb_16_d_fxy(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->width-1-x; y = b->height-1-y; osd_mark_dirty(t-w+1,y-h+1,t,y); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT16 *)b->line[y])[x] = p; x--; } y--; } }
/*TODO*///static void pb_16_d_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; osd_mark_dirty(y,t,y+h-1,t+w-1); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT16 *)b->line[x])[y] = p; x++; } y++; } }
/*TODO*///static void pb_16_d_fx_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; y = b->width-1-y; osd_mark_dirty(y-h+1,t,y,t+w-1); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT16 *)b->line[x])[y] = p; x++; } y--; } }
/*TODO*///static void pb_16_d_fy_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->height-1-x; osd_mark_dirty(y,t-w+1,y+h-1,t); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT16 *)b->line[x])[y] = p; x--; } y++; } }
/*TODO*///static void pb_16_d_fxy_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->height-1-x; y = b->width-1-y; osd_mark_dirty(y-h+1,t-w+1,y,t); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT16 *)b->line[x])[y] = p; x--; } y--; } }
/*TODO*///
/*TODO*///
/*TODO*///static void pb_32_nd(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT32 *)b->line[y])[x] = p; x++; } y++; } }
/*TODO*///static void pb_32_nd_fx(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->width-1-x; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT32 *)b->line[y])[x] = p; x--; } y++; } }
/*TODO*///static void pb_32_nd_fy(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; y = b->height-1-y; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT32 *)b->line[y])[x] = p; x++; } y--; } }
/*TODO*///static void pb_32_nd_fxy(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->width-1-x; y = b->height-1-y; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT32 *)b->line[y])[x] = p; x--; } y--; } }
/*TODO*///static void pb_32_nd_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT32 *)b->line[x])[y] = p; x++; } y++; } }
/*TODO*///static void pb_32_nd_fx_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; y = b->width-1-y; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT32 *)b->line[x])[y] = p; x++; } y--; } }
/*TODO*///static void pb_32_nd_fy_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->height-1-x; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT32 *)b->line[x])[y] = p; x--; } y++; } }
/*TODO*///static void pb_32_nd_fxy_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->height-1-x; y = b->width-1-y; while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT32 *)b->line[x])[y] = p; x--; } y--; } }
/*TODO*///
/*TODO*///static void pb_32_d(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; osd_mark_dirty(t,y,t+w-1,y+h-1); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT32 *)b->line[y])[x] = p; x++; } y++; } }
/*TODO*///static void pb_32_d_fx(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->width-1-x;  osd_mark_dirty(t-w+1,y,t,y+h-1); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT32 *)b->line[y])[x] = p; x--; } y++; } }
/*TODO*///static void pb_32_d_fy(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; y = b->height-1-y; osd_mark_dirty(t,y-h+1,t+w-1,y); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT32 *)b->line[y])[x] = p; x++; } y--; } }
/*TODO*///static void pb_32_d_fxy(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->width-1-x; y = b->height-1-y; osd_mark_dirty(t-w+1,y-h+1,t,y); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT32 *)b->line[y])[x] = p; x--; } y--; } }
/*TODO*///static void pb_32_d_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; osd_mark_dirty(y,t,y+h-1,t+w-1); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT32 *)b->line[x])[y] = p; x++; } y++; } }
/*TODO*///static void pb_32_d_fx_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; y = b->width-1-y; osd_mark_dirty(y-h+1,t,y,t+w-1); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT32 *)b->line[x])[y] = p; x++; } y--; } }
/*TODO*///static void pb_32_d_fy_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->height-1-x; osd_mark_dirty(y,t-w+1,y+h-1,t); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT32 *)b->line[x])[y] = p; x--; } y++; } }
/*TODO*///static void pb_32_d_fxy_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->height-1-x; y = b->width-1-y; osd_mark_dirty(y-h+1,t-w+1,y,t); while(h-->0){ int c=w; x=t; while(c-->0){ ((UINT32 *)b->line[x])[y] = p; x--; } y--; } }
/*TODO*///
/*TODO*///
    public static mark_dirty_procPtr md = new mark_dirty_procPtr() {
        public void handler(int sx, int sy, int ex, int ey) {
            osd_mark_dirty(sx, sy, ex, ey);
        }
    };
    public static mark_dirty_procPtr md_fx = new mark_dirty_procPtr() {
        public void handler(int sx, int sy, int ex, int ey) {
            osd_mark_dirty(Machine.scrbitmap.width - 1 - ex, sy, Machine.scrbitmap.width - 1 - sx, ey);
        }
    };
    public static mark_dirty_procPtr md_fy = new mark_dirty_procPtr() {
        public void handler(int sx, int sy, int ex, int ey) {
            osd_mark_dirty(sx, Machine.scrbitmap.height - 1 - ey, ex, Machine.scrbitmap.height - 1 - sy);
        }
    };
    public static mark_dirty_procPtr md_fxy = new mark_dirty_procPtr() {
        public void handler(int sx, int sy, int ex, int ey) {
            osd_mark_dirty(Machine.scrbitmap.width - 1 - ex, Machine.scrbitmap.height - 1 - ey, Machine.scrbitmap.width - 1 - sx, Machine.scrbitmap.height - 1 - sy);
        }
    };
    public static mark_dirty_procPtr md_s = new mark_dirty_procPtr() {
        public void handler(int sx, int sy, int ex, int ey) {
            osd_mark_dirty(sy, sx, ey, ex);
        }
    };
    public static mark_dirty_procPtr md_fx_s = new mark_dirty_procPtr() {
        public void handler(int sx, int sy, int ex, int ey) {
            osd_mark_dirty(Machine.scrbitmap.width - 1 - ey, sx, Machine.scrbitmap.width - 1 - sy, ex);
        }
    };
    public static mark_dirty_procPtr md_fy_s = new mark_dirty_procPtr() {
        public void handler(int sx, int sy, int ex, int ey) {
            osd_mark_dirty(sy, Machine.scrbitmap.height - 1 - ex, ey, Machine.scrbitmap.height - 1 - sx);
        }
    };
    public static mark_dirty_procPtr md_fxy_s = new mark_dirty_procPtr() {
        public void handler(int sx, int sy, int ex, int ey) {
            osd_mark_dirty(Machine.scrbitmap.width - 1 - ey, Machine.scrbitmap.height - 1 - ex, Machine.scrbitmap.width - 1 - sy, Machine.scrbitmap.height - 1 - sx);
        }
    };
    static plot_pixel_procPtr pps_8_nd[]
            = {pp_8_nd, pp_8_nd_fx, pp_8_nd_fy, pp_8_nd_fxy,
                pp_8_nd_s, pp_8_nd_fx_s, pp_8_nd_fy_s, pp_8_nd_fxy_s};

    static plot_pixel_procPtr pps_8_d[]
            = {pp_8_d, pp_8_d_fx, pp_8_d_fy, pp_8_d_fxy,
                pp_8_d_s, pp_8_d_fx_s, pp_8_d_fy_s, pp_8_d_fxy_s};

    /*TODO*///static plot_pixel_proc pps_16_nd[] =
/*TODO*///		{ pp_16_nd,   pp_16_nd_fx,   pp_16_nd_fy, 	pp_16_nd_fxy,
/*TODO*///		  pp_16_nd_s, pp_16_nd_fx_s, pp_16_nd_fy_s, pp_16_nd_fxy_s };
/*TODO*///
/*TODO*///static plot_pixel_proc pps_16_d[] =
/*TODO*///		{ pp_16_d,   pp_16_d_fx,   pp_16_d_fy, 	 pp_16_d_fxy,
/*TODO*///		  pp_16_d_s, pp_16_d_fx_s, pp_16_d_fy_s, pp_16_d_fxy_s };
/*TODO*///
/*TODO*///static plot_pixel_proc pps_32_nd[] =
/*TODO*///		{ pp_32_nd,   pp_32_nd_fx,   pp_32_nd_fy, 	pp_32_nd_fxy,
/*TODO*///		  pp_32_nd_s, pp_32_nd_fx_s, pp_32_nd_fy_s, pp_32_nd_fxy_s };
/*TODO*///
/*TODO*///static plot_pixel_proc pps_32_d[] =
/*TODO*///		{ pp_32_d,   pp_32_d_fx,   pp_32_d_fy, 	 pp_32_d_fxy,
/*TODO*///		  pp_32_d_s, pp_32_d_fx_s, pp_32_d_fy_s, pp_32_d_fxy_s };
/*TODO*///
    static read_pixel_procPtr rps_8[]
            = {rp_8, rp_8_fx, rp_8_fy, rp_8_fxy,
                rp_8_s, rp_8_fx_s, rp_8_fy_s, rp_8_fxy_s};
    /*TODO*///
/*TODO*///static read_pixel_proc rps_16[] =
/*TODO*///		{ rp_16,   rp_16_fx,   rp_16_fy,   rp_16_fxy,
/*TODO*///		  rp_16_s, rp_16_fx_s, rp_16_fy_s, rp_16_fxy_s };
/*TODO*///
/*TODO*///static read_pixel_proc rps_32[] =
/*TODO*///		{ rp_32,   rp_32_fx,   rp_32_fy,   rp_32_fxy,
/*TODO*///		  rp_32_s, rp_32_fx_s, rp_32_fy_s, rp_32_fxy_s };
/*TODO*///
/*TODO*///
    static plot_box_procPtr pbs_8_nd[]
            = {pb_8_nd, pb_8_nd_fx, pb_8_nd_fy, pb_8_nd_fxy,
                pb_8_nd_s, pb_8_nd_fx_s, pb_8_nd_fy_s, pb_8_nd_fxy_s};

    static plot_box_procPtr pbs_8_d[]
            = {pb_8_d, pb_8_d_fx, pb_8_d_fy, pb_8_d_fxy,
                pb_8_d_s, pb_8_d_fx_s, pb_8_d_fy_s, pb_8_d_fxy_s};

    /*TODO*///static plot_box_proc pbs_16_nd[] =
/*TODO*///		{ pb_16_nd,   pb_16_nd_fx,   pb_16_nd_fy, 	pb_16_nd_fxy,
/*TODO*///		  pb_16_nd_s, pb_16_nd_fx_s, pb_16_nd_fy_s, pb_16_nd_fxy_s };
/*TODO*///
/*TODO*///static plot_box_proc pbs_16_d[] =
/*TODO*///		{ pb_16_d,   pb_16_d_fx,   pb_16_d_fy, 	 pb_16_d_fxy,
/*TODO*///		  pb_16_d_s, pb_16_d_fx_s, pb_16_d_fy_s, pb_16_d_fxy_s };
/*TODO*///
/*TODO*///static plot_box_proc pbs_32_nd[] =
/*TODO*///		{ pb_32_nd,   pb_32_nd_fx,   pb_32_nd_fy, 	pb_32_nd_fxy,
/*TODO*///		  pb_32_nd_s, pb_32_nd_fx_s, pb_32_nd_fy_s, pb_32_nd_fxy_s };
/*TODO*///
/*TODO*///static plot_box_proc pbs_32_d[] =
/*TODO*///		{ pb_32_d,   pb_32_d_fx,   pb_32_d_fy, 	 pb_32_d_fxy,
/*TODO*///		  pb_32_d_s, pb_32_d_fx_s, pb_32_d_fy_s, pb_32_d_fxy_s };
/*TODO*///
/*TODO*///
    static mark_dirty_procPtr mds[]
            = {md, md_fx, md_fy, md_fxy,
                md_s, md_fx_s, md_fy_s, md_fxy_s};

    public static void set_pixel_functions() {
        mark_dirty = mds[Machine.orientation];

        if (Machine.color_depth == 8) {
            read_pixel = rps_8[Machine.orientation];

            if ((Machine.drv.video_attributes & VIDEO_SUPPORTS_DIRTY) != 0) {
                plot_pixel = pps_8_d[Machine.orientation];
                plot_box = pbs_8_d[Machine.orientation];
            } else {
                plot_pixel = pps_8_nd[Machine.orientation];
                plot_box = pbs_8_nd[Machine.orientation];
            }
        } else if (Machine.color_depth == 15 || Machine.color_depth == 16) {
            throw new UnsupportedOperationException("Unsupported");
            /*TODO*///            read_pixel = rps_16[Machine.orientation];
/*TODO*///
            /*TODO*///  if ((Machine.drv.video_attributes & VIDEO_SUPPORTS_DIRTY) != 0) {
            /*TODO*///      plot_pixel = pps_16_d[Machine.orientation];
            /*TODO*///      plot_box = pbs_16_d[Machine.orientation];
            /*TODO*///  } else {
            /*TODO*///      plot_pixel = pps_16_nd[Machine.orientation];
            /*TODO*///      plot_box = pbs_16_nd[Machine.orientation];
            /*TODO*///  }
        } else {
            throw new UnsupportedOperationException("Unsupported");
            /*TODO*///  read_pixel = rps_32[Machine.orientation];

            /*TODO*///  if ((Machine.drv.video_attributes & VIDEO_SUPPORTS_DIRTY) != 0) {
            /*TODO*///      plot_pixel = pps_32_d[Machine.orientation];
            /*TODO*///      plot_box = pbs_32_d[Machine.orientation];
            /*TODO*///  } else {
            /*TODO*///      plot_pixel = pps_32_nd[Machine.orientation];
            /*TODO*///      plot_box = pbs_32_nd[Machine.orientation];
            /*TODO*///  }
        }

        /* while we're here, fill in the raw drawing mode table as well */
        is_raw[TRANSPARENCY_NONE_RAW] = 1;
        is_raw[TRANSPARENCY_PEN_RAW] = 1;
        is_raw[TRANSPARENCY_PENS_RAW] = 1;
        is_raw[TRANSPARENCY_PEN_TABLE_RAW] = 1;
        is_raw[TRANSPARENCY_BLEND_RAW] = 1;
    }

    /*TODO*///
/*TODO*///
/*TODO*///INLINE void plotclip(struct osd_bitmap *bitmap,int x,int y,int pen,const struct rectangle *clip)
/*TODO*///{
/*TODO*///	if (x >= clip->min_x && x <= clip->max_x && y >= clip->min_y && y <= clip->max_y)
/*TODO*///		plot_pixel(bitmap,x,y,pen);
/*TODO*///}
/*TODO*///
/*TODO*///void draw_crosshair(struct osd_bitmap *bitmap,int x,int y,const struct rectangle *clip)
/*TODO*///{
/*TODO*///	unsigned short black,white;
/*TODO*///	int i;
/*TODO*///
/*TODO*///	black = Machine->uifont->colortable[0];
/*TODO*///	white = Machine->uifont->colortable[1];
/*TODO*///
/*TODO*///	for (i = 1;i < 6;i++)
/*TODO*///	{
/*TODO*///		plotclip(bitmap,x+i,y,white,clip);
/*TODO*///		plotclip(bitmap,x-i,y,white,clip);
/*TODO*///		plotclip(bitmap,x,y+i,white,clip);
/*TODO*///		plotclip(bitmap,x,y-i,white,clip);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///#else /* DECLARE */
/*TODO*///
/*TODO*////* -------------------- included inline section --------------------- */
/*TODO*///
/*TODO*////* this is #included to generate 8-bit and 16-bit versions */
/*TODO*///
/*TODO*///#define ADJUST_8													\
/*TODO*///	int ydir;														\
/*TODO*///	if (flipy)														\
/*TODO*///	{																\
/*TODO*///		INCREMENT_DST(VMODULO * (dstheight-1))						\
/*TODO*///		srcdata += (srcheight - dstheight - topskip) * srcmodulo;	\
/*TODO*///		ydir = -1;													\
/*TODO*///	}																\
/*TODO*///	else															\
/*TODO*///	{																\
/*TODO*///		srcdata += topskip * srcmodulo;								\
/*TODO*///		ydir = 1;													\
/*TODO*///	}																\
/*TODO*///	if (flipx)														\
/*TODO*///	{																\
/*TODO*///		INCREMENT_DST(HMODULO * (dstwidth-1))						\
/*TODO*///		srcdata += (srcwidth - dstwidth - leftskip);				\
/*TODO*///	}																\
/*TODO*///	else															\
/*TODO*///		srcdata += leftskip;										\
/*TODO*///	srcmodulo -= dstwidth;
/*TODO*///
/*TODO*///
/*TODO*///#define ADJUST_4													\
/*TODO*///	int ydir;														\
/*TODO*///	if (flipy)														\
/*TODO*///	{																\
/*TODO*///		INCREMENT_DST(VMODULO * (dstheight-1))						\
/*TODO*///		srcdata += (srcheight - dstheight - topskip) * srcmodulo;	\
/*TODO*///		ydir = -1;													\
/*TODO*///	}																\
/*TODO*///	else															\
/*TODO*///	{																\
/*TODO*///		srcdata += topskip * srcmodulo;								\
/*TODO*///		ydir = 1;													\
/*TODO*///	}																\
/*TODO*///	if (flipx)														\
/*TODO*///	{																\
/*TODO*///		INCREMENT_DST(HMODULO * (dstwidth-1))						\
/*TODO*///		srcdata += (srcwidth - dstwidth - leftskip)/2;				\
/*TODO*///		leftskip = (srcwidth - dstwidth - leftskip) & 1;			\
/*TODO*///	}																\
/*TODO*///	else															\
/*TODO*///	{																\
/*TODO*///		srcdata += leftskip/2;										\
/*TODO*///		leftskip &= 1;												\
/*TODO*///	}																\
/*TODO*///	srcmodulo -= (dstwidth+leftskip)/2;
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///DECLARE_SWAP_RAW_PRI(blockmove_8toN_opaque,(COMMON_ARGS,
/*TODO*///		COLOR_ARG),
/*TODO*///{
/*TODO*///	ADJUST_8
/*TODO*///
/*TODO*///	if (flipx)
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata - dstwidth*HMODULO;
/*TODO*///			while (dstdata >= end + 8*HMODULO)
/*TODO*///			{
/*TODO*///				INCREMENT_DST(-8*HMODULO)
/*TODO*///				SETPIXELCOLOR(8*HMODULO,LOOKUP(srcdata[0]))
/*TODO*///				SETPIXELCOLOR(7*HMODULO,LOOKUP(srcdata[1]))
/*TODO*///				SETPIXELCOLOR(6*HMODULO,LOOKUP(srcdata[2]))
/*TODO*///				SETPIXELCOLOR(5*HMODULO,LOOKUP(srcdata[3]))
/*TODO*///				SETPIXELCOLOR(4*HMODULO,LOOKUP(srcdata[4]))
/*TODO*///				SETPIXELCOLOR(3*HMODULO,LOOKUP(srcdata[5]))
/*TODO*///				SETPIXELCOLOR(2*HMODULO,LOOKUP(srcdata[6]))
/*TODO*///				SETPIXELCOLOR(1*HMODULO,LOOKUP(srcdata[7]))
/*TODO*///				srcdata += 8;
/*TODO*///			}
/*TODO*///			while (dstdata > end)
/*TODO*///			{
/*TODO*///				SETPIXELCOLOR(0,LOOKUP(*srcdata))
/*TODO*///				srcdata++;
/*TODO*///				INCREMENT_DST(-HMODULO)
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO + dstwidth*HMODULO)
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata + dstwidth*HMODULO;
/*TODO*///			while (dstdata <= end - 8*HMODULO)
/*TODO*///			{
/*TODO*///				SETPIXELCOLOR(0*HMODULO,LOOKUP(srcdata[0]))
/*TODO*///				SETPIXELCOLOR(1*HMODULO,LOOKUP(srcdata[1]))
/*TODO*///				SETPIXELCOLOR(2*HMODULO,LOOKUP(srcdata[2]))
/*TODO*///				SETPIXELCOLOR(3*HMODULO,LOOKUP(srcdata[3]))
/*TODO*///				SETPIXELCOLOR(4*HMODULO,LOOKUP(srcdata[4]))
/*TODO*///				SETPIXELCOLOR(5*HMODULO,LOOKUP(srcdata[5]))
/*TODO*///				SETPIXELCOLOR(6*HMODULO,LOOKUP(srcdata[6]))
/*TODO*///				SETPIXELCOLOR(7*HMODULO,LOOKUP(srcdata[7]))
/*TODO*///				srcdata += 8;
/*TODO*///				INCREMENT_DST(8*HMODULO)
/*TODO*///			}
/*TODO*///			while (dstdata < end)
/*TODO*///			{
/*TODO*///				SETPIXELCOLOR(0,LOOKUP(*srcdata))
/*TODO*///				srcdata++;
/*TODO*///				INCREMENT_DST(HMODULO)
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO - dstwidth*HMODULO)
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///DECLARE_SWAP_RAW_PRI(blockmove_4toN_opaque,(COMMON_ARGS,
/*TODO*///		COLOR_ARG),
/*TODO*///{
/*TODO*///	ADJUST_4
/*TODO*///
/*TODO*///	if (flipx)
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata - dstwidth*HMODULO;
/*TODO*///			if (leftskip)
/*TODO*///			{
/*TODO*///				SETPIXELCOLOR(0,LOOKUP(*srcdata>>4))
/*TODO*///				srcdata++;
/*TODO*///				INCREMENT_DST(-HMODULO)
/*TODO*///			}
/*TODO*///			while (dstdata >= end + 8*HMODULO)
/*TODO*///			{
/*TODO*///				INCREMENT_DST(-8*HMODULO)
/*TODO*///				SETPIXELCOLOR(8*HMODULO,LOOKUP(srcdata[0]&0x0f))
/*TODO*///				SETPIXELCOLOR(7*HMODULO,LOOKUP(srcdata[0]>>4))
/*TODO*///				SETPIXELCOLOR(6*HMODULO,LOOKUP(srcdata[1]&0x0f))
/*TODO*///				SETPIXELCOLOR(5*HMODULO,LOOKUP(srcdata[1]>>4))
/*TODO*///				SETPIXELCOLOR(4*HMODULO,LOOKUP(srcdata[2]&0x0f))
/*TODO*///				SETPIXELCOLOR(3*HMODULO,LOOKUP(srcdata[2]>>4))
/*TODO*///				SETPIXELCOLOR(2*HMODULO,LOOKUP(srcdata[3]&0x0f))
/*TODO*///				SETPIXELCOLOR(1*HMODULO,LOOKUP(srcdata[3]>>4))
/*TODO*///				srcdata += 4;
/*TODO*///			}
/*TODO*///			while (dstdata > end)
/*TODO*///			{
/*TODO*///				SETPIXELCOLOR(0,LOOKUP(*srcdata&0x0f))
/*TODO*///				INCREMENT_DST(-HMODULO)
/*TODO*///				if (dstdata > end)
/*TODO*///				{
/*TODO*///					SETPIXELCOLOR(0,LOOKUP(*srcdata>>4))
/*TODO*///					srcdata++;
/*TODO*///					INCREMENT_DST(-HMODULO)
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO + dstwidth*HMODULO)
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata + dstwidth*HMODULO;
/*TODO*///			if (leftskip)
/*TODO*///			{
/*TODO*///				SETPIXELCOLOR(0,LOOKUP(*srcdata>>4))
/*TODO*///				srcdata++;
/*TODO*///				INCREMENT_DST(HMODULO)
/*TODO*///			}
/*TODO*///			while (dstdata <= end - 8*HMODULO)
/*TODO*///			{
/*TODO*///				SETPIXELCOLOR(0*HMODULO,LOOKUP(srcdata[0]&0x0f))
/*TODO*///				SETPIXELCOLOR(1*HMODULO,LOOKUP(srcdata[0]>>4))
/*TODO*///				SETPIXELCOLOR(2*HMODULO,LOOKUP(srcdata[1]&0x0f))
/*TODO*///				SETPIXELCOLOR(3*HMODULO,LOOKUP(srcdata[1]>>4))
/*TODO*///				SETPIXELCOLOR(4*HMODULO,LOOKUP(srcdata[2]&0x0f))
/*TODO*///				SETPIXELCOLOR(5*HMODULO,LOOKUP(srcdata[2]>>4))
/*TODO*///				SETPIXELCOLOR(6*HMODULO,LOOKUP(srcdata[3]&0x0f))
/*TODO*///				SETPIXELCOLOR(7*HMODULO,LOOKUP(srcdata[3]>>4))
/*TODO*///				srcdata += 4;
/*TODO*///				INCREMENT_DST(8*HMODULO)
/*TODO*///			}
/*TODO*///			while (dstdata < end)
/*TODO*///			{
/*TODO*///				SETPIXELCOLOR(0,LOOKUP(*srcdata&0x0f))
/*TODO*///				INCREMENT_DST(HMODULO)
/*TODO*///				if (dstdata < end)
/*TODO*///				{
/*TODO*///					SETPIXELCOLOR(0,LOOKUP(*srcdata>>4))
/*TODO*///					srcdata++;
/*TODO*///					INCREMENT_DST(HMODULO)
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO - dstwidth*HMODULO)
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///DECLARE_SWAP_RAW_PRI(blockmove_8toN_transpen,(COMMON_ARGS,
/*TODO*///		COLOR_ARG,int transpen),
/*TODO*///{
/*TODO*///	ADJUST_8
/*TODO*///
/*TODO*///	if (flipx)
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///		int trans4;
/*TODO*///		UINT32 *sd4;
/*TODO*///
/*TODO*///		trans4 = transpen * 0x01010101;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata - dstwidth*HMODULO;
/*TODO*///			while (((long)srcdata & 3) && dstdata > end)	/* longword align */
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transpen) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				INCREMENT_DST(-HMODULO)
/*TODO*///			}
/*TODO*///			sd4 = (UINT32 *)srcdata;
/*TODO*///			while (dstdata >= end + 4*HMODULO)
/*TODO*///			{
/*TODO*///				UINT32 col4;
/*TODO*///
/*TODO*///				INCREMENT_DST(-4*HMODULO)
/*TODO*///				if ((col4 = *(sd4++)) != trans4)
/*TODO*///				{
/*TODO*///					UINT32 xod4;
/*TODO*///
/*TODO*///					xod4 = col4 ^ trans4;
/*TODO*///					if (xod4 & (0xff<<SHIFT0)) SETPIXELCOLOR(4*HMODULO,LOOKUP((col4>>SHIFT0) & 0xff))
/*TODO*///					if (xod4 & (0xff<<SHIFT1)) SETPIXELCOLOR(3*HMODULO,LOOKUP((col4>>SHIFT1) & 0xff))
/*TODO*///					if (xod4 & (0xff<<SHIFT2)) SETPIXELCOLOR(2*HMODULO,LOOKUP((col4>>SHIFT2) & 0xff))
/*TODO*///					if (xod4 & (0xff<<SHIFT3)) SETPIXELCOLOR(1*HMODULO,LOOKUP((col4>>SHIFT3) & 0xff))
/*TODO*///				}
/*TODO*///			}
/*TODO*///			srcdata = (UINT8 *)sd4;
/*TODO*///			while (dstdata > end)
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transpen) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				INCREMENT_DST(-HMODULO)
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO + dstwidth*HMODULO);
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///		int trans4;
/*TODO*///		UINT32 *sd4;
/*TODO*///
/*TODO*///		trans4 = transpen * 0x01010101;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata + dstwidth*HMODULO;
/*TODO*///			while (((long)srcdata & 3) && dstdata < end)	/* longword align */
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transpen) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				INCREMENT_DST(HMODULO)
/*TODO*///			}
/*TODO*///			sd4 = (UINT32 *)srcdata;
/*TODO*///			while (dstdata <= end - 4*HMODULO)
/*TODO*///			{
/*TODO*///				UINT32 col4;
/*TODO*///
/*TODO*///				if ((col4 = *(sd4++)) != trans4)
/*TODO*///				{
/*TODO*///					UINT32 xod4;
/*TODO*///
/*TODO*///					xod4 = col4 ^ trans4;
/*TODO*///					if (xod4 & (0xff<<SHIFT0)) SETPIXELCOLOR(0*HMODULO,LOOKUP((col4>>SHIFT0) & 0xff))
/*TODO*///					if (xod4 & (0xff<<SHIFT1)) SETPIXELCOLOR(1*HMODULO,LOOKUP((col4>>SHIFT1) & 0xff))
/*TODO*///					if (xod4 & (0xff<<SHIFT2)) SETPIXELCOLOR(2*HMODULO,LOOKUP((col4>>SHIFT2) & 0xff))
/*TODO*///					if (xod4 & (0xff<<SHIFT3)) SETPIXELCOLOR(3*HMODULO,LOOKUP((col4>>SHIFT3) & 0xff))
/*TODO*///				}
/*TODO*///				INCREMENT_DST(4*HMODULO)
/*TODO*///			}
/*TODO*///			srcdata = (UINT8 *)sd4;
/*TODO*///			while (dstdata < end)
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transpen) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				INCREMENT_DST(HMODULO)
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO - dstwidth*HMODULO);
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///DECLARE_SWAP_RAW_PRI(blockmove_4toN_transpen,(COMMON_ARGS,
/*TODO*///		COLOR_ARG,int transpen),
/*TODO*///{
/*TODO*///	ADJUST_4
/*TODO*///
/*TODO*///	if (flipx)
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			int col;
/*TODO*///
/*TODO*///			end = dstdata - dstwidth*HMODULO;
/*TODO*///			if (leftskip)
/*TODO*///			{
/*TODO*///				col = *(srcdata++)>>4;
/*TODO*///				if (col != transpen) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				INCREMENT_DST(-HMODULO)
/*TODO*///			}
/*TODO*///			while (dstdata > end)
/*TODO*///			{
/*TODO*///				col = *(srcdata)&0x0f;
/*TODO*///				if (col != transpen) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				INCREMENT_DST(-HMODULO)
/*TODO*///				if (dstdata > end)
/*TODO*///				{
/*TODO*///					col = *(srcdata++)>>4;
/*TODO*///					if (col != transpen) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///					INCREMENT_DST(-HMODULO)
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO + dstwidth*HMODULO)
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			int col;
/*TODO*///
/*TODO*///			end = dstdata + dstwidth*HMODULO;
/*TODO*///			if (leftskip)
/*TODO*///			{
/*TODO*///				col = *(srcdata++)>>4;
/*TODO*///				if (col != transpen) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				INCREMENT_DST(HMODULO)
/*TODO*///			}
/*TODO*///			while (dstdata < end)
/*TODO*///			{
/*TODO*///				col = *(srcdata)&0x0f;
/*TODO*///				if (col != transpen) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				INCREMENT_DST(HMODULO)
/*TODO*///				if (dstdata < end)
/*TODO*///				{
/*TODO*///					col = *(srcdata++)>>4;
/*TODO*///					if (col != transpen) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///					INCREMENT_DST(HMODULO)
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO - dstwidth*HMODULO)
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///DECLARE_SWAP_RAW_PRI(blockmove_8toN_transblend,(COMMON_ARGS,
/*TODO*///		COLOR_ARG,int transpen),
/*TODO*///{
/*TODO*///	ADJUST_8
/*TODO*///
/*TODO*///	if (flipx)
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///		int trans4;
/*TODO*///		UINT32 *sd4;
/*TODO*///
/*TODO*///		trans4 = transpen * 0x01010101;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata - dstwidth*HMODULO;
/*TODO*///			while (((long)srcdata & 3) && dstdata > end)	/* longword align */
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transpen) SETPIXELCOLOR(0,*dstdata | LOOKUP(col))
/*TODO*///				INCREMENT_DST(-HMODULO);
/*TODO*///			}
/*TODO*///			sd4 = (UINT32 *)srcdata;
/*TODO*///			while (dstdata >= end + 4*HMODULO)
/*TODO*///			{
/*TODO*///				UINT32 col4;
/*TODO*///
/*TODO*///				INCREMENT_DST(-4*HMODULO);
/*TODO*///				if ((col4 = *(sd4++)) != trans4)
/*TODO*///				{
/*TODO*///					UINT32 xod4;
/*TODO*///
/*TODO*///					xod4 = col4 ^ trans4;
/*TODO*///					if (xod4 & (0xff<<SHIFT0)) SETPIXELCOLOR(4*HMODULO,dstdata[4*HMODULO] | LOOKUP((col4>>SHIFT0) & 0xff))
/*TODO*///					if (xod4 & (0xff<<SHIFT1)) SETPIXELCOLOR(3*HMODULO,dstdata[3*HMODULO] | LOOKUP((col4>>SHIFT1) & 0xff))
/*TODO*///					if (xod4 & (0xff<<SHIFT2)) SETPIXELCOLOR(2*HMODULO,dstdata[2*HMODULO] | LOOKUP((col4>>SHIFT2) & 0xff))
/*TODO*///					if (xod4 & (0xff<<SHIFT3)) SETPIXELCOLOR(1*HMODULO,dstdata[1*HMODULO] | LOOKUP((col4>>SHIFT3) & 0xff))
/*TODO*///				}
/*TODO*///			}
/*TODO*///			srcdata = (UINT8 *)sd4;
/*TODO*///			while (dstdata > end)
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transpen) SETPIXELCOLOR(0,*dstdata | LOOKUP(col))
/*TODO*///				INCREMENT_DST(-HMODULO);
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO + dstwidth*HMODULO);
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///		int trans4;
/*TODO*///		UINT32 *sd4;
/*TODO*///
/*TODO*///		trans4 = transpen * 0x01010101;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata + dstwidth*HMODULO;
/*TODO*///			while (((long)srcdata & 3) && dstdata < end)	/* longword align */
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transpen) SETPIXELCOLOR(0,*dstdata | LOOKUP(col))
/*TODO*///				INCREMENT_DST(HMODULO);
/*TODO*///			}
/*TODO*///			sd4 = (UINT32 *)srcdata;
/*TODO*///			while (dstdata <= end - 4*HMODULO)
/*TODO*///			{
/*TODO*///				UINT32 col4;
/*TODO*///
/*TODO*///				if ((col4 = *(sd4++)) != trans4)
/*TODO*///				{
/*TODO*///					UINT32 xod4;
/*TODO*///
/*TODO*///					xod4 = col4 ^ trans4;
/*TODO*///					if (xod4 & (0xff<<SHIFT0)) SETPIXELCOLOR(0*HMODULO,dstdata[0*HMODULO] | LOOKUP((col4>>SHIFT0) & 0xff))
/*TODO*///					if (xod4 & (0xff<<SHIFT1)) SETPIXELCOLOR(1*HMODULO,dstdata[1*HMODULO] | LOOKUP((col4>>SHIFT1) & 0xff))
/*TODO*///					if (xod4 & (0xff<<SHIFT2)) SETPIXELCOLOR(2*HMODULO,dstdata[2*HMODULO] | LOOKUP((col4>>SHIFT2) & 0xff))
/*TODO*///					if (xod4 & (0xff<<SHIFT3)) SETPIXELCOLOR(3*HMODULO,dstdata[3*HMODULO] | LOOKUP((col4>>SHIFT3) & 0xff))
/*TODO*///				}
/*TODO*///				INCREMENT_DST(4*HMODULO);
/*TODO*///			}
/*TODO*///			srcdata = (UINT8 *)sd4;
/*TODO*///			while (dstdata < end)
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transpen) SETPIXELCOLOR(0,*dstdata | LOOKUP(col))
/*TODO*///				INCREMENT_DST(HMODULO);
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO - dstwidth*HMODULO);
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///
/*TODO*///#define PEN_IS_OPAQUE ((1<<col)&transmask) == 0
/*TODO*///
/*TODO*///DECLARE_SWAP_RAW_PRI(blockmove_8toN_transmask,(COMMON_ARGS,
/*TODO*///		COLOR_ARG,int transmask),
/*TODO*///{
/*TODO*///	ADJUST_8
/*TODO*///
/*TODO*///	if (flipx)
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///		UINT32 *sd4;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata - dstwidth*HMODULO;
/*TODO*///			while (((long)srcdata & 3) && dstdata > end)	/* longword align */
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (PEN_IS_OPAQUE) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				INCREMENT_DST(-HMODULO)
/*TODO*///			}
/*TODO*///			sd4 = (UINT32 *)srcdata;
/*TODO*///			while (dstdata >= end + 4*HMODULO)
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///				UINT32 col4;
/*TODO*///
/*TODO*///				INCREMENT_DST(-4*HMODULO)
/*TODO*///				col4 = *(sd4++);
/*TODO*///				col = (col4 >> SHIFT0) & 0xff;
/*TODO*///				if (PEN_IS_OPAQUE) SETPIXELCOLOR(4*HMODULO,LOOKUP(col))
/*TODO*///				col = (col4 >> SHIFT1) & 0xff;
/*TODO*///				if (PEN_IS_OPAQUE) SETPIXELCOLOR(3*HMODULO,LOOKUP(col))
/*TODO*///				col = (col4 >> SHIFT2) & 0xff;
/*TODO*///				if (PEN_IS_OPAQUE) SETPIXELCOLOR(2*HMODULO,LOOKUP(col))
/*TODO*///				col = (col4 >> SHIFT3) & 0xff;
/*TODO*///				if (PEN_IS_OPAQUE) SETPIXELCOLOR(1*HMODULO,LOOKUP(col))
/*TODO*///			}
/*TODO*///			srcdata = (UINT8 *)sd4;
/*TODO*///			while (dstdata > end)
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (PEN_IS_OPAQUE) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				INCREMENT_DST(-HMODULO)
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO + dstwidth*HMODULO)
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///		UINT32 *sd4;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata + dstwidth*HMODULO;
/*TODO*///			while (((long)srcdata & 3) && dstdata < end)	/* longword align */
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (PEN_IS_OPAQUE) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				INCREMENT_DST(HMODULO)
/*TODO*///			}
/*TODO*///			sd4 = (UINT32 *)srcdata;
/*TODO*///			while (dstdata <= end - 4*HMODULO)
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///				UINT32 col4;
/*TODO*///
/*TODO*///				col4 = *(sd4++);
/*TODO*///				col = (col4 >> SHIFT0) & 0xff;
/*TODO*///				if (PEN_IS_OPAQUE) SETPIXELCOLOR(0*HMODULO,LOOKUP(col))
/*TODO*///				col = (col4 >> SHIFT1) & 0xff;
/*TODO*///				if (PEN_IS_OPAQUE) SETPIXELCOLOR(1*HMODULO,LOOKUP(col))
/*TODO*///				col = (col4 >> SHIFT2) & 0xff;
/*TODO*///				if (PEN_IS_OPAQUE) SETPIXELCOLOR(2*HMODULO,LOOKUP(col))
/*TODO*///				col = (col4 >> SHIFT3) & 0xff;
/*TODO*///				if (PEN_IS_OPAQUE) SETPIXELCOLOR(3*HMODULO,LOOKUP(col))
/*TODO*///				INCREMENT_DST(4*HMODULO)
/*TODO*///			}
/*TODO*///			srcdata = (UINT8 *)sd4;
/*TODO*///			while (dstdata < end)
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (PEN_IS_OPAQUE) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				INCREMENT_DST(HMODULO)
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO - dstwidth*HMODULO)
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///DECLARE_SWAP_RAW_PRI(blockmove_8toN_transcolor,(COMMON_ARGS,
/*TODO*///		COLOR_ARG,const UINT16 *colortable,int transcolor),
/*TODO*///{
/*TODO*///	ADJUST_8
/*TODO*///
/*TODO*///	if (flipx)
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata - dstwidth*HMODULO;
/*TODO*///			while (dstdata > end)
/*TODO*///			{
/*TODO*///				if (colortable[*srcdata] != transcolor) SETPIXELCOLOR(0,LOOKUP(*srcdata))
/*TODO*///				srcdata++;
/*TODO*///				INCREMENT_DST(-HMODULO)
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO + dstwidth*HMODULO)
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata + dstwidth*HMODULO;
/*TODO*///			while (dstdata < end)
/*TODO*///			{
/*TODO*///				if (colortable[*srcdata] != transcolor) SETPIXELCOLOR(0,LOOKUP(*srcdata))
/*TODO*///				srcdata++;
/*TODO*///				INCREMENT_DST(HMODULO)
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO - dstwidth*HMODULO)
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///DECLARE_SWAP_RAW_PRI(blockmove_4toN_transcolor,(COMMON_ARGS,
/*TODO*///		COLOR_ARG,const UINT16 *colortable,int transcolor),
/*TODO*///{
/*TODO*///	ADJUST_4
/*TODO*///
/*TODO*///	if (flipx)
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			int col;
/*TODO*///
/*TODO*///			end = dstdata - dstwidth*HMODULO;
/*TODO*///			if (leftskip)
/*TODO*///			{
/*TODO*///				col = *(srcdata++)>>4;
/*TODO*///				if (colortable[col] != transcolor) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				INCREMENT_DST(-HMODULO)
/*TODO*///			}
/*TODO*///			while (dstdata > end)
/*TODO*///			{
/*TODO*///				col = *(srcdata)&0x0f;
/*TODO*///				if (colortable[col] != transcolor) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				INCREMENT_DST(-HMODULO)
/*TODO*///				if (dstdata > end)
/*TODO*///				{
/*TODO*///					col = *(srcdata++)>>4;
/*TODO*///					if (colortable[col] != transcolor) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///					INCREMENT_DST(-HMODULO)
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO + dstwidth*HMODULO)
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			int col;
/*TODO*///
/*TODO*///			end = dstdata + dstwidth*HMODULO;
/*TODO*///			if (leftskip)
/*TODO*///			{
/*TODO*///				col = *(srcdata++)>>4;
/*TODO*///				if (colortable[col] != transcolor) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				INCREMENT_DST(HMODULO)
/*TODO*///			}
/*TODO*///			while (dstdata < end)
/*TODO*///			{
/*TODO*///				col = *(srcdata)&0x0f;
/*TODO*///				if (colortable[col] != transcolor) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				INCREMENT_DST(HMODULO)
/*TODO*///				if (dstdata < end)
/*TODO*///				{
/*TODO*///					col = *(srcdata++)>>4;
/*TODO*///					if (colortable[col] != transcolor) SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///					INCREMENT_DST(HMODULO)
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO - dstwidth*HMODULO)
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///DECLARE_SWAP_RAW_PRI(blockmove_8toN_pen_table,(COMMON_ARGS,
/*TODO*///		COLOR_ARG,int transcolor),
/*TODO*///{
/*TODO*///	ADJUST_8
/*TODO*///
/*TODO*///	if (flipx)
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata - dstwidth*HMODULO;
/*TODO*///			while (dstdata > end)
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transcolor)
/*TODO*///				{
/*TODO*///					switch(gfx_drawmode_table[col])
/*TODO*///					{
/*TODO*///					case DRAWMODE_SOURCE:
/*TODO*///						SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///						break;
/*TODO*///					case DRAWMODE_SHADOW:
/*TODO*///						SETPIXELCOLOR(0,palette_shadow_table[*dstdata])
/*TODO*///						break;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				INCREMENT_DST(-HMODULO)
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO + dstwidth*HMODULO)
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata + dstwidth*HMODULO;
/*TODO*///			while (dstdata < end)
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transcolor)
/*TODO*///				{
/*TODO*///					switch(gfx_drawmode_table[col])
/*TODO*///					{
/*TODO*///					case DRAWMODE_SOURCE:
/*TODO*///						SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///						break;
/*TODO*///					case DRAWMODE_SHADOW:
/*TODO*///						SETPIXELCOLOR(0,palette_shadow_table[*dstdata])
/*TODO*///						break;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				INCREMENT_DST(HMODULO)
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO - dstwidth*HMODULO)
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///
/*TODO*///#if DEPTH >= 16
/*TODO*///DECLARE_SWAP_RAW_PRI(blockmove_8toN_alphaone,(COMMON_ARGS,
/*TODO*///		COLOR_ARG,int transpen, int alphapen),
/*TODO*///{
/*TODO*///	ADJUST_8
/*TODO*///
/*TODO*///	if (flipx)
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///		int trans4;
/*TODO*///		UINT32 *sd4;
/*TODO*///		UINT32 alphacolor = LOOKUP(alphapen);
/*TODO*///
/*TODO*///		trans4 = transpen * 0x01010101;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata - dstwidth*HMODULO;
/*TODO*///			while (((long)srcdata & 3) && dstdata > end)	/* longword align */
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transpen)
/*TODO*///				{
/*TODO*///					if (col == alphapen)
/*TODO*///						SETPIXELCOLOR(0,alpha_blend(*dstdata,alphacolor))
/*TODO*///					else
/*TODO*///						SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				}
/*TODO*///				INCREMENT_DST(-HMODULO);
/*TODO*///			}
/*TODO*///			sd4 = (UINT32 *)srcdata;
/*TODO*///			while (dstdata >= end + 4*HMODULO)
/*TODO*///			{
/*TODO*///				UINT32 col4;
/*TODO*///
/*TODO*///				INCREMENT_DST(-4*HMODULO);
/*TODO*///				if ((col4 = *(sd4++)) != trans4)
/*TODO*///				{
/*TODO*///					UINT32 xod4;
/*TODO*///
/*TODO*///					xod4 = col4 ^ trans4;
/*TODO*///					if (xod4 & (0xff<<SHIFT0))
/*TODO*///					{
/*TODO*///						if (((col4>>SHIFT0) & 0xff) == alphapen)
/*TODO*///							SETPIXELCOLOR(4*HMODULO,alpha_blend(dstdata[4*HMODULO], alphacolor))
/*TODO*///						else
/*TODO*///							SETPIXELCOLOR(4*HMODULO,LOOKUP((col4>>SHIFT0) & 0xff))
/*TODO*///					}
/*TODO*///					if (xod4 & (0xff<<SHIFT1))
/*TODO*///					{
/*TODO*///						if (((col4>>SHIFT1) & 0xff) == alphapen)
/*TODO*///							SETPIXELCOLOR(3*HMODULO,alpha_blend(dstdata[3*HMODULO], alphacolor))
/*TODO*///						else
/*TODO*///							SETPIXELCOLOR(3*HMODULO,LOOKUP((col4>>SHIFT1) & 0xff))
/*TODO*///					}
/*TODO*///					if (xod4 & (0xff<<SHIFT2))
/*TODO*///					{
/*TODO*///						if (((col4>>SHIFT2) & 0xff) == alphapen)
/*TODO*///							SETPIXELCOLOR(2*HMODULO,alpha_blend(dstdata[2*HMODULO], alphacolor))
/*TODO*///						else
/*TODO*///							SETPIXELCOLOR(2*HMODULO,LOOKUP((col4>>SHIFT2) & 0xff))
/*TODO*///					}
/*TODO*///					if (xod4 & (0xff<<SHIFT3))
/*TODO*///					{
/*TODO*///						if (((col4>>SHIFT3) & 0xff) == alphapen)
/*TODO*///							SETPIXELCOLOR(1*HMODULO,alpha_blend(dstdata[1*HMODULO], alphacolor))
/*TODO*///						else
/*TODO*///							SETPIXELCOLOR(1*HMODULO,LOOKUP((col4>>SHIFT3) & 0xff))
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///			srcdata = (UINT8 *)sd4;
/*TODO*///			while (dstdata > end)
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transpen)
/*TODO*///				{
/*TODO*///					if (col == alphapen)
/*TODO*///						SETPIXELCOLOR(0,alpha_blend(*dstdata, alphacolor))
/*TODO*///					else
/*TODO*///						SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				}
/*TODO*///				INCREMENT_DST(-HMODULO);
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO + dstwidth*HMODULO);
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///		int trans4;
/*TODO*///		UINT32 *sd4;
/*TODO*///		UINT32 alphacolor = LOOKUP(alphapen);
/*TODO*///
/*TODO*///		trans4 = transpen * 0x01010101;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata + dstwidth*HMODULO;
/*TODO*///			while (((long)srcdata & 3) && dstdata < end)	/* longword align */
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transpen)
/*TODO*///				{
/*TODO*///					if (col == alphapen)
/*TODO*///						SETPIXELCOLOR(0,alpha_blend(*dstdata, alphacolor))
/*TODO*///					else
/*TODO*///						SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				}
/*TODO*///				INCREMENT_DST(HMODULO);
/*TODO*///			}
/*TODO*///			sd4 = (UINT32 *)srcdata;
/*TODO*///			while (dstdata <= end - 4*HMODULO)
/*TODO*///			{
/*TODO*///				UINT32 col4;
/*TODO*///
/*TODO*///				if ((col4 = *(sd4++)) != trans4)
/*TODO*///				{
/*TODO*///					UINT32 xod4;
/*TODO*///
/*TODO*///					xod4 = col4 ^ trans4;
/*TODO*///					if (xod4 & (0xff<<SHIFT0))
/*TODO*///					{
/*TODO*///						if (((col4>>SHIFT0) & 0xff) == alphapen)
/*TODO*///							SETPIXELCOLOR(0*HMODULO,alpha_blend(dstdata[0*HMODULO], alphacolor))
/*TODO*///						else
/*TODO*///							SETPIXELCOLOR(0*HMODULO,LOOKUP((col4>>SHIFT0) & 0xff))
/*TODO*///					}
/*TODO*///					if (xod4 & (0xff<<SHIFT1))
/*TODO*///					{
/*TODO*///						if (((col4>>SHIFT1) & 0xff) == alphapen)
/*TODO*///							SETPIXELCOLOR(1*HMODULO,alpha_blend(dstdata[1*HMODULO], alphacolor))
/*TODO*///						else
/*TODO*///							SETPIXELCOLOR(1*HMODULO,LOOKUP((col4>>SHIFT1) & 0xff))
/*TODO*///					}
/*TODO*///					if (xod4 & (0xff<<SHIFT2))
/*TODO*///					{
/*TODO*///						if (((col4>>SHIFT2) & 0xff) == alphapen)
/*TODO*///							SETPIXELCOLOR(2*HMODULO,alpha_blend(dstdata[2*HMODULO], alphacolor))
/*TODO*///						else
/*TODO*///							SETPIXELCOLOR(2*HMODULO,LOOKUP((col4>>SHIFT2) & 0xff))
/*TODO*///					}
/*TODO*///					if (xod4 & (0xff<<SHIFT3))
/*TODO*///					{
/*TODO*///						if (((col4>>SHIFT3) & 0xff) == alphapen)
/*TODO*///							SETPIXELCOLOR(3*HMODULO,alpha_blend(dstdata[3*HMODULO], alphacolor))
/*TODO*///						else
/*TODO*///							SETPIXELCOLOR(3*HMODULO,LOOKUP((col4>>SHIFT3) & 0xff))
/*TODO*///					}
/*TODO*///				}
/*TODO*///				INCREMENT_DST(4*HMODULO);
/*TODO*///			}
/*TODO*///			srcdata = (UINT8 *)sd4;
/*TODO*///			while (dstdata < end)
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transpen)
/*TODO*///				{
/*TODO*///					if (col == alphapen)
/*TODO*///						SETPIXELCOLOR(0,alpha_blend(*dstdata, alphacolor))
/*TODO*///					else
/*TODO*///						SETPIXELCOLOR(0,LOOKUP(col))
/*TODO*///				}
/*TODO*///				INCREMENT_DST(HMODULO);
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO - dstwidth*HMODULO);
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///DECLARE_SWAP_RAW_PRI(blockmove_8toN_alpha,(COMMON_ARGS,
/*TODO*///		COLOR_ARG,int transpen),
/*TODO*///{
/*TODO*///	ADJUST_8
/*TODO*///
/*TODO*///	if (flipx)
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///		int trans4;
/*TODO*///		UINT32 *sd4;
/*TODO*///
/*TODO*///		trans4 = transpen * 0x01010101;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata - dstwidth*HMODULO;
/*TODO*///			while (((long)srcdata & 3) && dstdata > end)	/* longword align */
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transpen) SETPIXELCOLOR(0,alpha_blend(*dstdata, LOOKUP(col)));
/*TODO*///				INCREMENT_DST(-HMODULO);
/*TODO*///			}
/*TODO*///			sd4 = (UINT32 *)srcdata;
/*TODO*///			while (dstdata >= end + 4*HMODULO)
/*TODO*///			{
/*TODO*///				UINT32 col4;
/*TODO*///
/*TODO*///				INCREMENT_DST(-4*HMODULO);
/*TODO*///				if ((col4 = *(sd4++)) != trans4)
/*TODO*///				{
/*TODO*///					UINT32 xod4;
/*TODO*///
/*TODO*///					xod4 = col4 ^ trans4;
/*TODO*///					if (xod4 & (0xff<<SHIFT0)) SETPIXELCOLOR(4*HMODULO,alpha_blend(dstdata[4*HMODULO], LOOKUP((col4>>SHIFT0) & 0xff)));
/*TODO*///					if (xod4 & (0xff<<SHIFT1)) SETPIXELCOLOR(3*HMODULO,alpha_blend(dstdata[3*HMODULO], LOOKUP((col4>>SHIFT1) & 0xff)));
/*TODO*///					if (xod4 & (0xff<<SHIFT2)) SETPIXELCOLOR(2*HMODULO,alpha_blend(dstdata[2*HMODULO], LOOKUP((col4>>SHIFT2) & 0xff)));
/*TODO*///					if (xod4 & (0xff<<SHIFT3)) SETPIXELCOLOR(1*HMODULO,alpha_blend(dstdata[1*HMODULO], LOOKUP((col4>>SHIFT3) & 0xff)));
/*TODO*///				}
/*TODO*///			}
/*TODO*///			srcdata = (UINT8 *)sd4;
/*TODO*///			while (dstdata > end)
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transpen) SETPIXELCOLOR(0,alpha_blend(*dstdata, LOOKUP(col)));
/*TODO*///				INCREMENT_DST(-HMODULO);
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO + dstwidth*HMODULO);
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		DATA_TYPE *end;
/*TODO*///		int trans4;
/*TODO*///		UINT32 *sd4;
/*TODO*///
/*TODO*///		trans4 = transpen * 0x01010101;
/*TODO*///
/*TODO*///		while (dstheight)
/*TODO*///		{
/*TODO*///			end = dstdata + dstwidth*HMODULO;
/*TODO*///			while (((long)srcdata & 3) && dstdata < end)	/* longword align */
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transpen) SETPIXELCOLOR(0,alpha_blend(*dstdata, LOOKUP(col)));
/*TODO*///				INCREMENT_DST(HMODULO);
/*TODO*///			}
/*TODO*///			sd4 = (UINT32 *)srcdata;
/*TODO*///			while (dstdata <= end - 4*HMODULO)
/*TODO*///			{
/*TODO*///				UINT32 col4;
/*TODO*///
/*TODO*///				if ((col4 = *(sd4++)) != trans4)
/*TODO*///				{
/*TODO*///					UINT32 xod4;
/*TODO*///
/*TODO*///					xod4 = col4 ^ trans4;
/*TODO*///					if (xod4 & (0xff<<SHIFT0)) SETPIXELCOLOR(0*HMODULO,alpha_blend(dstdata[0*HMODULO], LOOKUP((col4>>SHIFT0) & 0xff)));
/*TODO*///					if (xod4 & (0xff<<SHIFT1)) SETPIXELCOLOR(1*HMODULO,alpha_blend(dstdata[1*HMODULO], LOOKUP((col4>>SHIFT1) & 0xff)));
/*TODO*///					if (xod4 & (0xff<<SHIFT2)) SETPIXELCOLOR(2*HMODULO,alpha_blend(dstdata[2*HMODULO], LOOKUP((col4>>SHIFT2) & 0xff)));
/*TODO*///					if (xod4 & (0xff<<SHIFT3)) SETPIXELCOLOR(3*HMODULO,alpha_blend(dstdata[3*HMODULO], LOOKUP((col4>>SHIFT3) & 0xff)));
/*TODO*///				}
/*TODO*///				INCREMENT_DST(4*HMODULO);
/*TODO*///			}
/*TODO*///			srcdata = (UINT8 *)sd4;
/*TODO*///			while (dstdata < end)
/*TODO*///			{
/*TODO*///				int col;
/*TODO*///
/*TODO*///				col = *(srcdata++);
/*TODO*///				if (col != transpen) SETPIXELCOLOR(0,alpha_blend(*dstdata, LOOKUP(col)));
/*TODO*///				INCREMENT_DST(HMODULO);
/*TODO*///			}
/*TODO*///
/*TODO*///			srcdata += srcmodulo;
/*TODO*///			INCREMENT_DST(ydir*VMODULO - dstwidth*HMODULO);
/*TODO*///			dstheight--;
/*TODO*///		}
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///#else
/*TODO*///
/*TODO*///DECLARE_SWAP_RAW_PRI(blockmove_8toN_alphaone,(COMMON_ARGS,
/*TODO*///		COLOR_ARG,int transpen, int alphapen),{})
/*TODO*///
/*TODO*///DECLARE_SWAP_RAW_PRI(blockmove_8toN_alpha,(COMMON_ARGS,
/*TODO*///		COLOR_ARG,int transpen),{})
/*TODO*///
/*TODO*///#endif
/*TODO*///
/*TODO*///DECLARE(blockmove_NtoN_opaque_noremap,(
/*TODO*///		const DATA_TYPE *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		DATA_TYPE *dstdata,int dstmodulo),
/*TODO*///{
/*TODO*///	while (srcheight)
/*TODO*///	{
/*TODO*///		memcpy(dstdata,srcdata,srcwidth * sizeof(DATA_TYPE));
/*TODO*///		srcdata += srcmodulo;
/*TODO*///		dstdata += dstmodulo;
/*TODO*///		srcheight--;
/*TODO*///	}
/*TODO*///})
    public static void blockmove_NtoN_opaque_noremap8(UBytePtr srcdata, int srcwidth, int srcheight, int srcmodulo, UBytePtr dstdata, int dstmodulo) {

        while (srcheight != 0) {
            System.arraycopy(srcdata.memory, (int) srcdata.offset, dstdata.memory, (int) dstdata.offset, srcwidth);
            srcdata.inc(srcmodulo);
            dstdata.inc(dstmodulo);
            srcheight--;
        }
    }

    public static void blockmove_NtoN_opaque_noremap8_flipx(
		UBytePtr srcdata,int srcwidth,int srcheight,int srcmodulo,
		UBytePtr dstdata,int dstmodulo)
    {
        UBytePtr end;

        srcmodulo += srcwidth;
        dstmodulo -= srcwidth;
        //srcdata += srcwidth-1;

        while (srcheight != 0)
        {
		end = new UBytePtr(dstdata, srcwidth);
		while (dstdata.offset <= end.offset - 8)
		{
			srcdata.offset -= 8;
			dstdata.write(0, srcdata.read(8));
			dstdata.write(1, srcdata.read(7));
			dstdata.write(2, srcdata.read(6));
			dstdata.write(3, srcdata.read(5));
			dstdata.write(4, srcdata.read(4));
			dstdata.write(5, srcdata.read(3));
			dstdata.write(6, srcdata.read(2));
			dstdata.write(7, srcdata.read(1));
			dstdata.offset += 8;
		}
		while (dstdata.offset < end.offset){
			dstdata.writeinc( srcdata.read() );
                        srcdata.dec();
                }

		srcdata.offset += srcmodulo;
		dstdata.offset += dstmodulo;
		srcheight--;
    	}
    }
/*TODO*///
/*TODO*///DECLARE(blockmove_NtoN_opaque_remap,(
/*TODO*///		const DATA_TYPE *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		DATA_TYPE *dstdata,int dstmodulo,
/*TODO*///		const UINT32 *paldata),
/*TODO*///{
/*TODO*///	DATA_TYPE *end;
/*TODO*///
/*TODO*///	srcmodulo -= srcwidth;
/*TODO*///	dstmodulo -= srcwidth;
/*TODO*///
/*TODO*///	while (srcheight)
/*TODO*///	{
/*TODO*///		end = dstdata + srcwidth;
/*TODO*///		while (dstdata <= end - 8)
/*TODO*///		{
/*TODO*///			dstdata[0] = paldata[srcdata[0]];
/*TODO*///			dstdata[1] = paldata[srcdata[1]];
/*TODO*///			dstdata[2] = paldata[srcdata[2]];
/*TODO*///			dstdata[3] = paldata[srcdata[3]];
/*TODO*///			dstdata[4] = paldata[srcdata[4]];
/*TODO*///			dstdata[5] = paldata[srcdata[5]];
/*TODO*///			dstdata[6] = paldata[srcdata[6]];
/*TODO*///			dstdata[7] = paldata[srcdata[7]];
/*TODO*///			dstdata += 8;
/*TODO*///			srcdata += 8;
/*TODO*///		}
/*TODO*///		while (dstdata < end)
/*TODO*///			*(dstdata++) = paldata[*(srcdata++)];
/*TODO*///
/*TODO*///		srcdata += srcmodulo;
/*TODO*///		dstdata += dstmodulo;
/*TODO*///		srcheight--;
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///DECLARE(blockmove_NtoN_opaque_remap_flipx,(
/*TODO*///		const DATA_TYPE *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		DATA_TYPE *dstdata,int dstmodulo,
/*TODO*///		const UINT32 *paldata),
/*TODO*///{
/*TODO*///	DATA_TYPE *end;
/*TODO*///
/*TODO*///	srcmodulo += srcwidth;
/*TODO*///	dstmodulo -= srcwidth;
/*TODO*///	//srcdata += srcwidth-1;
/*TODO*///
/*TODO*///	while (srcheight)
/*TODO*///	{
/*TODO*///		end = dstdata + srcwidth;
/*TODO*///		while (dstdata <= end - 8)
/*TODO*///		{
/*TODO*///			srcdata -= 8;
/*TODO*///			dstdata[0] = paldata[srcdata[8]];
/*TODO*///			dstdata[1] = paldata[srcdata[7]];
/*TODO*///			dstdata[2] = paldata[srcdata[6]];
/*TODO*///			dstdata[3] = paldata[srcdata[5]];
/*TODO*///			dstdata[4] = paldata[srcdata[4]];
/*TODO*///			dstdata[5] = paldata[srcdata[3]];
/*TODO*///			dstdata[6] = paldata[srcdata[2]];
/*TODO*///			dstdata[7] = paldata[srcdata[1]];
/*TODO*///			dstdata += 8;
/*TODO*///		}
/*TODO*///		while (dstdata < end)
/*TODO*///			*(dstdata++) = paldata[*(srcdata--)];
/*TODO*///
/*TODO*///		srcdata += srcmodulo;
/*TODO*///		dstdata += dstmodulo;
/*TODO*///		srcheight--;
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///
/*TODO*///DECLARE(blockmove_NtoN_blend_noremap,(
/*TODO*///		const DATA_TYPE *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		DATA_TYPE *dstdata,int dstmodulo,
/*TODO*///		int srcshift),
/*TODO*///{
/*TODO*///	DATA_TYPE *end;
/*TODO*///
/*TODO*///	srcmodulo -= srcwidth;
/*TODO*///	dstmodulo -= srcwidth;
/*TODO*///
/*TODO*///	while (srcheight)
/*TODO*///	{
/*TODO*///		end = dstdata + srcwidth;
/*TODO*///		while (dstdata <= end - 8)
/*TODO*///		{
/*TODO*///			dstdata[0] |= srcdata[0] << srcshift;
/*TODO*///			dstdata[1] |= srcdata[1] << srcshift;
/*TODO*///			dstdata[2] |= srcdata[2] << srcshift;
/*TODO*///			dstdata[3] |= srcdata[3] << srcshift;
/*TODO*///			dstdata[4] |= srcdata[4] << srcshift;
/*TODO*///			dstdata[5] |= srcdata[5] << srcshift;
/*TODO*///			dstdata[6] |= srcdata[6] << srcshift;
/*TODO*///			dstdata[7] |= srcdata[7] << srcshift;
/*TODO*///			dstdata += 8;
/*TODO*///			srcdata += 8;
/*TODO*///		}
/*TODO*///		while (dstdata < end)
/*TODO*///			*(dstdata++) |= *(srcdata++) << srcshift;
/*TODO*///
/*TODO*///		srcdata += srcmodulo;
/*TODO*///		dstdata += dstmodulo;
/*TODO*///		srcheight--;
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///DECLARE(blockmove_NtoN_blend_noremap_flipx,(
/*TODO*///		const DATA_TYPE *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		DATA_TYPE *dstdata,int dstmodulo,
/*TODO*///		int srcshift),
/*TODO*///{
/*TODO*///	DATA_TYPE *end;
/*TODO*///
/*TODO*///	srcmodulo += srcwidth;
/*TODO*///	dstmodulo -= srcwidth;
/*TODO*///	//srcdata += srcwidth-1;
/*TODO*///
/*TODO*///	while (srcheight)
/*TODO*///	{
/*TODO*///		end = dstdata + srcwidth;
/*TODO*///		while (dstdata <= end - 8)
/*TODO*///		{
/*TODO*///			srcdata -= 8;
/*TODO*///			dstdata[0] |= srcdata[8] << srcshift;
/*TODO*///			dstdata[1] |= srcdata[7] << srcshift;
/*TODO*///			dstdata[2] |= srcdata[6] << srcshift;
/*TODO*///			dstdata[3] |= srcdata[5] << srcshift;
/*TODO*///			dstdata[4] |= srcdata[4] << srcshift;
/*TODO*///			dstdata[5] |= srcdata[3] << srcshift;
/*TODO*///			dstdata[6] |= srcdata[2] << srcshift;
/*TODO*///			dstdata[7] |= srcdata[1] << srcshift;
/*TODO*///			dstdata += 8;
/*TODO*///		}
/*TODO*///		while (dstdata < end)
/*TODO*///			*(dstdata++) |= *(srcdata--) << srcshift;
/*TODO*///
/*TODO*///		srcdata += srcmodulo;
/*TODO*///		dstdata += dstmodulo;
/*TODO*///		srcheight--;
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///DECLARE(blockmove_NtoN_blend_remap,(
/*TODO*///		const DATA_TYPE *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		DATA_TYPE *dstdata,int dstmodulo,
/*TODO*///		const UINT32 *paldata,int srcshift),
/*TODO*///{
/*TODO*///	DATA_TYPE *end;
/*TODO*///
/*TODO*///	srcmodulo -= srcwidth;
/*TODO*///	dstmodulo -= srcwidth;
/*TODO*///
/*TODO*///	while (srcheight)
/*TODO*///	{
/*TODO*///		end = dstdata + srcwidth;
/*TODO*///		while (dstdata <= end - 8)
/*TODO*///		{
/*TODO*///			dstdata[0] = paldata[dstdata[0] | (srcdata[0] << srcshift)];
/*TODO*///			dstdata[1] = paldata[dstdata[1] | (srcdata[1] << srcshift)];
/*TODO*///			dstdata[2] = paldata[dstdata[2] | (srcdata[2] << srcshift)];
/*TODO*///			dstdata[3] = paldata[dstdata[3] | (srcdata[3] << srcshift)];
/*TODO*///			dstdata[4] = paldata[dstdata[4] | (srcdata[4] << srcshift)];
/*TODO*///			dstdata[5] = paldata[dstdata[5] | (srcdata[5] << srcshift)];
/*TODO*///			dstdata[6] = paldata[dstdata[6] | (srcdata[6] << srcshift)];
/*TODO*///			dstdata[7] = paldata[dstdata[7] | (srcdata[7] << srcshift)];
/*TODO*///			dstdata += 8;
/*TODO*///			srcdata += 8;
/*TODO*///		}
/*TODO*///		while (dstdata < end)
/*TODO*///		{
/*TODO*///			*dstdata = paldata[*dstdata | (*(srcdata++) << srcshift)];
/*TODO*///			dstdata++;
/*TODO*///		}
/*TODO*///
/*TODO*///		srcdata += srcmodulo;
/*TODO*///		dstdata += dstmodulo;
/*TODO*///		srcheight--;
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///DECLARE(blockmove_NtoN_blend_remap_flipx,(
/*TODO*///		const DATA_TYPE *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		DATA_TYPE *dstdata,int dstmodulo,
/*TODO*///		const UINT32 *paldata,int srcshift),
/*TODO*///{
/*TODO*///	DATA_TYPE *end;
/*TODO*///
/*TODO*///	srcmodulo += srcwidth;
/*TODO*///	dstmodulo -= srcwidth;
/*TODO*///	//srcdata += srcwidth-1;
/*TODO*///
/*TODO*///	while (srcheight)
/*TODO*///	{
/*TODO*///		end = dstdata + srcwidth;
/*TODO*///		while (dstdata <= end - 8)
/*TODO*///		{
/*TODO*///			srcdata -= 8;
/*TODO*///			dstdata[0] = paldata[dstdata[0] | (srcdata[8] << srcshift)];
/*TODO*///			dstdata[1] = paldata[dstdata[1] | (srcdata[7] << srcshift)];
/*TODO*///			dstdata[2] = paldata[dstdata[2] | (srcdata[6] << srcshift)];
/*TODO*///			dstdata[3] = paldata[dstdata[3] | (srcdata[5] << srcshift)];
/*TODO*///			dstdata[4] = paldata[dstdata[4] | (srcdata[4] << srcshift)];
/*TODO*///			dstdata[5] = paldata[dstdata[5] | (srcdata[3] << srcshift)];
/*TODO*///			dstdata[6] = paldata[dstdata[6] | (srcdata[2] << srcshift)];
/*TODO*///			dstdata[7] = paldata[dstdata[7] | (srcdata[1] << srcshift)];
/*TODO*///			dstdata += 8;
/*TODO*///		}
/*TODO*///		while (dstdata < end)
/*TODO*///		{
/*TODO*///			*dstdata = paldata[*dstdata | (*(srcdata--) << srcshift)];
/*TODO*///			dstdata++;
/*TODO*///		}
/*TODO*///
/*TODO*///		srcdata += srcmodulo;
/*TODO*///		dstdata += dstmodulo;
/*TODO*///		srcheight--;
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///DECLARE(drawgfx_core,(
/*TODO*///		struct osd_bitmap *dest,const struct GfxElement *gfx,
/*TODO*///		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color,
/*TODO*///		struct osd_bitmap *pri_buffer,UINT32 pri_mask),
/*TODO*///{
/*TODO*///	int ox;
/*TODO*///	int oy;
/*TODO*///	int ex;
/*TODO*///	int ey;
/*TODO*///
/*TODO*///
/*TODO*///	/* check bounds */
/*TODO*///	ox = sx;
/*TODO*///	oy = sy;
/*TODO*///
/*TODO*///	ex = sx + gfx->width-1;
/*TODO*///	if (sx < 0) sx = 0;
/*TODO*///	if (clip && sx < clip->min_x) sx = clip->min_x;
/*TODO*///	if (ex >= dest->width) ex = dest->width-1;
/*TODO*///	if (clip && ex > clip->max_x) ex = clip->max_x;
/*TODO*///	if (sx > ex) return;
/*TODO*///
/*TODO*///	ey = sy + gfx->height-1;
/*TODO*///	if (sy < 0) sy = 0;
/*TODO*///	if (clip && sy < clip->min_y) sy = clip->min_y;
/*TODO*///	if (ey >= dest->height) ey = dest->height-1;
/*TODO*///	if (clip && ey > clip->max_y) ey = clip->max_y;
/*TODO*///	if (sy > ey) return;
/*TODO*///
/*TODO*///	if (Machine->drv->video_attributes & VIDEO_SUPPORTS_DIRTY)
/*TODO*///		osd_mark_dirty(sx,sy,ex,ey);
/*TODO*///
/*TODO*///	{
/*TODO*///		UINT8 *sd = gfx->gfxdata + code * gfx->char_modulo;		/* source data */
/*TODO*///		int sw = gfx->width;									/* source width */
/*TODO*///		int sh = gfx->height;									/* source height */
/*TODO*///		int sm = gfx->line_modulo;								/* source modulo */
/*TODO*///		int ls = sx-ox;											/* left skip */
/*TODO*///		int ts = sy-oy;											/* top skip */
/*TODO*///		DATA_TYPE *dd = ((DATA_TYPE *)dest->line[sy]) + sx;		/* dest data */
/*TODO*///		int dw = ex-sx+1;										/* dest width */
/*TODO*///		int dh = ey-sy+1;										/* dest height */
/*TODO*///		int dm = ((DATA_TYPE *)dest->line[1])-((DATA_TYPE *)dest->line[0]);	/* dest modulo */
/*TODO*///		const UINT32 *paldata = &gfx->colortable[gfx->color_granularity * color];
/*TODO*///		UINT8 *pribuf = (pri_buffer) ? pri_buffer->line[sy] + sx : NULL;
/*TODO*///
/*TODO*///		switch (transparency)
/*TODO*///		{
/*TODO*///			case TRANSPARENCY_NONE:
/*TODO*///				if (gfx->flags & GFX_PACKED)
/*TODO*///				{
/*TODO*///					if (pribuf)
/*TODO*///						BLOCKMOVEPRI(4toN_opaque,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask));
/*TODO*///					else
/*TODO*///						BLOCKMOVELU(4toN_opaque,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata));
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if (pribuf)
/*TODO*///						BLOCKMOVEPRI(8toN_opaque,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask));
/*TODO*///					else
/*TODO*///						BLOCKMOVELU(8toN_opaque,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_NONE_RAW:
/*TODO*///				if (gfx->flags & GFX_PACKED)
/*TODO*///				{
/*TODO*///					if (pribuf)
/*TODO*///						BLOCKMOVERAWPRI(4toN_opaque,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,pribuf,pri_mask));
/*TODO*///					else
/*TODO*///						BLOCKMOVERAW(4toN_opaque,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color));
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if (pribuf)
/*TODO*///						BLOCKMOVERAWPRI(8toN_opaque,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,pribuf,pri_mask));
/*TODO*///					else
/*TODO*///						BLOCKMOVERAW(8toN_opaque,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_PEN:
/*TODO*///				if (gfx->flags & GFX_PACKED)
/*TODO*///				{
/*TODO*///					if (pribuf)
/*TODO*///						BLOCKMOVEPRI(4toN_transpen,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask,transparent_color));
/*TODO*///					else
/*TODO*///						BLOCKMOVELU(4toN_transpen,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,transparent_color));
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if (pribuf)
/*TODO*///						BLOCKMOVEPRI(8toN_transpen,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask,transparent_color));
/*TODO*///					else
/*TODO*///						BLOCKMOVELU(8toN_transpen,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,transparent_color));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_PEN_RAW:
/*TODO*///				if (gfx->flags & GFX_PACKED)
/*TODO*///				{
/*TODO*///					if (pribuf)
/*TODO*///						BLOCKMOVERAWPRI(4toN_transpen,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,pribuf,pri_mask,transparent_color));
/*TODO*///					else
/*TODO*///						BLOCKMOVERAW(4toN_transpen,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,transparent_color));
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if (pribuf)
/*TODO*///						BLOCKMOVERAWPRI(8toN_transpen,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,pribuf,pri_mask,transparent_color));
/*TODO*///					else
/*TODO*///						BLOCKMOVERAW(8toN_transpen,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,transparent_color));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_PENS:
/*TODO*///				if (pribuf)
/*TODO*///					BLOCKMOVEPRI(8toN_transmask,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask,transparent_color));
/*TODO*///				else
/*TODO*///					BLOCKMOVELU(8toN_transmask,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,transparent_color));
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_PENS_RAW:
/*TODO*///				if (pribuf)
/*TODO*///					BLOCKMOVERAWPRI(8toN_transmask,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,pribuf,pri_mask,transparent_color));
/*TODO*///				else
/*TODO*///					BLOCKMOVERAW(8toN_transmask,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,transparent_color));
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_COLOR:
/*TODO*///				if (gfx->flags & GFX_PACKED)
/*TODO*///				{
/*TODO*///					if (pribuf)
/*TODO*///						BLOCKMOVEPRI(4toN_transcolor,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask,Machine->game_colortable + (paldata - Machine->remapped_colortable),transparent_color));
/*TODO*///					else
/*TODO*///						BLOCKMOVELU(4toN_transcolor,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,Machine->game_colortable + (paldata - Machine->remapped_colortable),transparent_color));
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if (pribuf)
/*TODO*///						BLOCKMOVEPRI(8toN_transcolor,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask,Machine->game_colortable + (paldata - Machine->remapped_colortable),transparent_color));
/*TODO*///					else
/*TODO*///						BLOCKMOVELU(8toN_transcolor,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,Machine->game_colortable + (paldata - Machine->remapped_colortable),transparent_color));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_PEN_TABLE:
/*TODO*///				if (pribuf)
/*TODO*///					BLOCKMOVEPRI(8toN_pen_table,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask,transparent_color));
/*TODO*///				else
/*TODO*///					BLOCKMOVELU(8toN_pen_table,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,transparent_color));
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_PEN_TABLE_RAW:
/*TODO*///				if (pribuf)
/*TODO*///					BLOCKMOVERAWPRI(8toN_pen_table,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,pribuf,pri_mask,transparent_color));
/*TODO*///				else
/*TODO*///					BLOCKMOVERAW(8toN_pen_table,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,transparent_color));
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_BLEND_RAW:
/*TODO*///				if (pribuf)
/*TODO*///					BLOCKMOVERAWPRI(8toN_transblend,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,pribuf,pri_mask,transparent_color));
/*TODO*///				else
/*TODO*///					BLOCKMOVERAW(8toN_transblend,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,transparent_color));
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_ALPHAONE:
/*TODO*///				if (pribuf)
/*TODO*///					BLOCKMOVEPRI(8toN_alphaone,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask,transparent_color & 0xff, (transparent_color>>8) & 0xff));
/*TODO*///				else
/*TODO*///					BLOCKMOVELU(8toN_alphaone,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,transparent_color & 0xff, (transparent_color>>8) & 0xff));
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_ALPHA:
/*TODO*///				if (pribuf)
/*TODO*///					BLOCKMOVEPRI(8toN_alpha,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask,transparent_color));
/*TODO*///				else
/*TODO*///					BLOCKMOVELU(8toN_alpha,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,transparent_color));
/*TODO*///				break;
/*TODO*///
/*TODO*///			default:
/*TODO*///				if (pribuf)
/*TODO*///					usrintf_showmessage("pdrawgfx pen mode not supported");
/*TODO*///				else
/*TODO*///					usrintf_showmessage("drawgfx pen mode not supported");
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///})
    public static void drawgfx_core8(osd_bitmap dest, GfxElement gfx,/*unsigned*/ int code,/*unsigned*/ int color, int flipx, int flipy, int sx, int sy, rectangle clip, int transparency, int transparent_color, osd_bitmap pri_buffer, int/*UINT32*/ pri_mask) {
        int ox;
        int oy;
        int ex;
        int ey;


        /* check bounds */
        ox = sx;
        oy = sy;

        ex = sx + gfx.width - 1;
        if (sx < 0) {
            sx = 0;
        }
        if (clip != null && sx < clip.min_x) {
            sx = clip.min_x;
        }
        if (ex >= dest.width) {
            ex = dest.width - 1;
        }
        if (clip != null && ex > clip.max_x) {
            ex = clip.max_x;
        }
        if (sx > ex) {
            return;
        }

        ey = sy + gfx.height - 1;
        if (sy < 0) {
            sy = 0;
        }
        if (clip != null && sy < clip.min_y) {
            sy = clip.min_y;
        }
        if (ey >= dest.height) {
            ey = dest.height - 1;
        }
        if (clip != null && ey > clip.max_y) {
            ey = clip.max_y;
        }
        if (sy > ey) {
            return;
        }

        if ((Machine.drv.video_attributes & VIDEO_SUPPORTS_DIRTY) != 0) {
            osd_mark_dirty(sx, sy, ex, ey);
        }

        UBytePtr sd = new UBytePtr(gfx.gfxdata, code * gfx.char_modulo);/* source data */
        int sw = gfx.width;/* source width */
        int sh = gfx.height;/* source height */
        int sm = gfx.line_modulo;/* source modulo */
        int ls = sx - ox;/* left skip */
        int ts = sy - oy;/* top skip */
        UBytePtr dd = new UBytePtr(dest.line[sy], sx);/* dest data */
        int dw = ex - sx + 1;/* dest width */
        int dh = ey - sy + 1;/* dest height */
        int dm = (dest.line[1].offset) - (dest.line[0].offset);/* dest modulo */

        IntArray paldata = null;
        if (gfx.colortable != null) {
            paldata = new IntArray(gfx.colortable, gfx.color_granularity * color);
        }
        UBytePtr pribuf = null;/*TODO*///		UINT8 *pribuf = (pri_buffer) ? ((UINT8 *)pri_buffer->line[sy]) + sx : NULL;
        /* optimizations for 1:1 mapping */

        switch (transparency) {
            case TRANSPARENCY_NONE:
                if ((gfx.flags & GFX_PACKED) != 0) {
                    throw new UnsupportedOperationException("Unsupported");
                    /*TODO*///					if (pribuf)
/*TODO*///						BLOCKMOVEPRI(4toN_opaque,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask));
/*TODO*///					else
/*TODO*///						BLOCKMOVELU(4toN_opaque,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata));
                } else {
                    if (pribuf != null) {
                        throw new UnsupportedOperationException("Unsupported");
                        /*TODO*///						BLOCKMOVEPRI(8toN_opaque,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask));
                    } else {
                        if ((gfx.flags & GFX_SWAPXY) != 0) {
                            throw new UnsupportedOperationException("unsupported");//blockmove_##function##_swapxy##8 args ;
                        } else {
                            blockmove_8toN_opaque8(sd, sw, sh, sm, ls, ts, flipx, flipy, dd, dw, dh, dm, paldata);
                        }
                    }
                }
                break;
            /*TODO*///
/*TODO*///			case TRANSPARENCY_NONE_RAW:
/*TODO*///				if (gfx->flags & GFX_PACKED)
/*TODO*///				{
/*TODO*///					if (pribuf)
/*TODO*///						BLOCKMOVERAWPRI(4toN_opaque,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,pribuf,pri_mask));
/*TODO*///					else
/*TODO*///						BLOCKMOVERAW(4toN_opaque,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color));
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if (pribuf)
/*TODO*///						BLOCKMOVERAWPRI(8toN_opaque,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,pribuf,pri_mask));
/*TODO*///					else
/*TODO*///						BLOCKMOVERAW(8toN_opaque,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///
            case TRANSPARENCY_PEN:
                if ((gfx.flags & GFX_PACKED) != 0) {
                    throw new UnsupportedOperationException("Unsupported");
                    /*TODO*///					if (pribuf)
/*TODO*///						BLOCKMOVEPRI(4toN_transpen,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask,transparent_color));
/*TODO*///					else
/*TODO*///						BLOCKMOVELU(4toN_transpen,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,transparent_color));
                } else {
                    if (pribuf != null) {
                        throw new UnsupportedOperationException("Unsupported");
                        /*TODO*///						BLOCKMOVEPRI(8toN_transpen,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask,transparent_color));
                    } else {
                        if ((gfx.flags & GFX_SWAPXY) != 0) {
                            throw new UnsupportedOperationException("Unsupported");//blockmove_##function##_swapxy##16 args ;
                            //BLOCKMOVELU(8toN_transpen,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,transparent_color));
                        } else {
                            blockmove_8toN_transpen8(sd, sw, sh, sm, ls, ts, flipx, flipy, dd, dw, dh, dm, paldata, transparent_color);
                        }
                    }
                }
                break;
            /*TODO*///
/*TODO*///			case TRANSPARENCY_PEN_RAW:
/*TODO*///				if (gfx->flags & GFX_PACKED)
/*TODO*///				{
/*TODO*///					if (pribuf)
/*TODO*///						BLOCKMOVERAWPRI(4toN_transpen,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,pribuf,pri_mask,transparent_color));
/*TODO*///					else
/*TODO*///						BLOCKMOVERAW(4toN_transpen,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,transparent_color));
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if (pribuf)
/*TODO*///						BLOCKMOVERAWPRI(8toN_transpen,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,pribuf,pri_mask,transparent_color));
/*TODO*///					else
/*TODO*///						BLOCKMOVERAW(8toN_transpen,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,transparent_color));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_PENS:
/*TODO*///				if (pribuf)
/*TODO*///					BLOCKMOVEPRI(8toN_transmask,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask,transparent_color));
/*TODO*///				else
/*TODO*///					BLOCKMOVELU(8toN_transmask,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,transparent_color));
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_PENS_RAW:
/*TODO*///				if (pribuf)
/*TODO*///					BLOCKMOVERAWPRI(8toN_transmask,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,pribuf,pri_mask,transparent_color));
/*TODO*///				else
/*TODO*///					BLOCKMOVERAW(8toN_transmask,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,transparent_color));
/*TODO*///				break;
/*TODO*///
            case TRANSPARENCY_COLOR:
                if ((gfx.flags & GFX_PACKED) != 0) {
                    throw new UnsupportedOperationException("Unsupported");
                    /*TODO*///					if (pribuf)
/*TODO*///						BLOCKMOVEPRI(4toN_transcolor,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask,Machine->game_colortable + (paldata - Machine->remapped_colortable),transparent_color));
/*TODO*///					else
/*TODO*///						BLOCKMOVELU(4toN_transcolor,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,Machine->game_colortable + (paldata - Machine->remapped_colortable),transparent_color));
                } else {
                    if (pribuf != null) {
                        throw new UnsupportedOperationException("Unsupported");/*TODO*///						BLOCKMOVEPRI(8toN_transcolor,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask,Machine->game_colortable + (paldata - Machine->remapped_colortable),transparent_color));
                    } else {
                        if ((gfx.flags & GFX_SWAPXY) != 0) {
                            throw new UnsupportedOperationException("Unsupported");//BLOCKMOVELU(8toN_transcolor,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,Machine->game_colortable + (paldata - Machine->remapped_colortable),transparent_color));
                        } else {
                            blockmove_8toN_transcolor8(sd, sw, sh, sm, ls, ts, flipx, flipy, dd, dw, dh, dm, paldata, new UShortArray(Machine.game_colortable, (paldata.offset - Machine.remapped_colortable.offset)), transparent_color);
                        }
                    }
                }
                break;

            /*TODO*///
/*TODO*///			case TRANSPARENCY_PEN_TABLE:
/*TODO*///				if (pribuf)
/*TODO*///					BLOCKMOVEPRI(8toN_pen_table,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask,transparent_color));
/*TODO*///				else
/*TODO*///					BLOCKMOVELU(8toN_pen_table,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,transparent_color));
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_PEN_TABLE_RAW:
/*TODO*///				if (pribuf)
/*TODO*///					BLOCKMOVERAWPRI(8toN_pen_table,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,pribuf,pri_mask,transparent_color));
/*TODO*///				else
/*TODO*///					BLOCKMOVERAW(8toN_pen_table,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,transparent_color));
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_BLEND_RAW:
/*TODO*///				if (pribuf)
/*TODO*///					BLOCKMOVERAWPRI(8toN_transblend,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,pribuf,pri_mask,transparent_color));
/*TODO*///				else
/*TODO*///					BLOCKMOVERAW(8toN_transblend,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,color,transparent_color));
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_ALPHAONE:
/*TODO*///				if (pribuf)
/*TODO*///					BLOCKMOVEPRI(8toN_alphaone,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask,transparent_color & 0xff, (transparent_color>>8) & 0xff));
/*TODO*///				else
/*TODO*///					BLOCKMOVELU(8toN_alphaone,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,transparent_color & 0xff, (transparent_color>>8) & 0xff));
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_ALPHA:
/*TODO*///				if (pribuf)
/*TODO*///					BLOCKMOVEPRI(8toN_alpha,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,pribuf,pri_mask,transparent_color));
/*TODO*///				else
/*TODO*///					BLOCKMOVELU(8toN_alpha,(sd,sw,sh,sm,ls,ts,flipx,flipy,dd,dw,dh,dm,paldata,transparent_color));
/*TODO*///				break;
/*TODO*///
            default:
                throw new UnsupportedOperationException("Unsupported");
            /*TODO*///				if (pribuf)
/*TODO*///					usrintf_showmessage("pdrawgfx pen mode not supported");
/*TODO*///				else
/*TODO*///					usrintf_showmessage("drawgfx pen mode not supported");
/*TODO*///				break;
        }

    }

    /*TODO*///
/*TODO*///DECLARE(copybitmap_core,(
/*TODO*///		struct osd_bitmap *dest,struct osd_bitmap *src,
/*TODO*///		int flipx,int flipy,int sx,int sy,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color),
/*TODO*///{
/*TODO*///	int ox;
/*TODO*///	int oy;
/*TODO*///	int ex;
/*TODO*///	int ey;
/*TODO*///
/*TODO*///
/*TODO*///	/* check bounds */
/*TODO*///	ox = sx;
/*TODO*///	oy = sy;
/*TODO*///
/*TODO*///	ex = sx + src->width-1;
/*TODO*///	if (sx < 0) sx = 0;
/*TODO*///	if (clip && sx < clip->min_x) sx = clip->min_x;
/*TODO*///	if (ex >= dest->width) ex = dest->width-1;
/*TODO*///	if (clip && ex > clip->max_x) ex = clip->max_x;
/*TODO*///	if (sx > ex) return;
/*TODO*///
/*TODO*///	ey = sy + src->height-1;
/*TODO*///	if (sy < 0) sy = 0;
/*TODO*///	if (clip && sy < clip->min_y) sy = clip->min_y;
/*TODO*///	if (ey >= dest->height) ey = dest->height-1;
/*TODO*///	if (clip && ey > clip->max_y) ey = clip->max_y;
/*TODO*///	if (sy > ey) return;
/*TODO*///
/*TODO*///	{
/*TODO*///		DATA_TYPE *sd = ((DATA_TYPE *)src->line[0]);							/* source data */
/*TODO*///		int sw = ex-sx+1;														/* source width */
/*TODO*///		int sh = ey-sy+1;														/* source height */
/*TODO*///		int sm = ((DATA_TYPE *)src->line[1])-((DATA_TYPE *)src->line[0]);		/* source modulo */
/*TODO*///		DATA_TYPE *dd = ((DATA_TYPE *)dest->line[sy]) + sx;						/* dest data */
/*TODO*///		int dm = ((DATA_TYPE *)dest->line[1])-((DATA_TYPE *)dest->line[0]);		/* dest modulo */
/*TODO*///
/*TODO*///		if (flipx)
/*TODO*///		{
/*TODO*///			//if ((sx-ox) == 0) sd += gfx->width - sw;
/*TODO*///			sd += src->width -1 -(sx-ox);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			sd += (sx-ox);
/*TODO*///
/*TODO*///		if (flipy)
/*TODO*///		{
/*TODO*///			//if ((sy-oy) == 0) sd += sm * (gfx->height - sh);
/*TODO*///			//dd += dm * (sh - 1);
/*TODO*///			//dm = -dm;
/*TODO*///			sd += sm * (src->height -1 -(sy-oy));
/*TODO*///			sm = -sm;
/*TODO*///		}
/*TODO*///		else
/*TODO*///			sd += sm * (sy-oy);
/*TODO*///
/*TODO*///		switch (transparency)
/*TODO*///		{
/*TODO*///			case TRANSPARENCY_NONE:
/*TODO*///				BLOCKMOVE(NtoN_opaque_remap,flipx,(sd,sw,sh,sm,dd,dm,Machine->pens));
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_NONE_RAW:
/*TODO*///				BLOCKMOVE(NtoN_opaque_noremap,flipx,(sd,sw,sh,sm,dd,dm));
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_PEN_RAW:
/*TODO*///				BLOCKMOVE(NtoN_transpen_noremap,flipx,(sd,sw,sh,sm,dd,dm,transparent_color));
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_BLEND:
/*TODO*///				BLOCKMOVE(NtoN_blend_remap,flipx,(sd,sw,sh,sm,dd,dm,Machine->pens,transparent_color));
/*TODO*///				break;
/*TODO*///
/*TODO*///			case TRANSPARENCY_BLEND_RAW:
/*TODO*///				BLOCKMOVE(NtoN_blend_noremap,flipx,(sd,sw,sh,sm,dd,dm,transparent_color));
/*TODO*///				break;
/*TODO*///
/*TODO*///			default:
/*TODO*///				usrintf_showmessage("copybitmap pen mode not supported");
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///})
    public static void copybitmap_core8(osd_bitmap dest, osd_bitmap src, int flipx, int flipy, int sx, int sy, rectangle clip, int transparency, int transparent_color) {
        int ox;
        int oy;
        int ex;
        int ey;


        /* check bounds */
        ox = sx;
        oy = sy;

        ex = sx + src.width - 1;
        if (sx < 0) {
            sx = 0;
        }
        if (clip != null && sx < clip.min_x) {
            sx = clip.min_x;
        }
        if (ex >= dest.width) {
            ex = dest.width - 1;
        }
        if (clip != null && ex > clip.max_x) {
            ex = clip.max_x;
        }
        if (sx > ex) {
            return;
        }

        ey = sy + src.height - 1;
        if (sy < 0) {
            sy = 0;
        }
        if (clip != null && sy < clip.min_y) {
            sy = clip.min_y;
        }
        if (ey >= dest.height) {
            ey = dest.height - 1;
        }
        if (clip != null && ey > clip.max_y) {
            ey = clip.max_y;
        }
        if (sy > ey) {
            return;
        }

        UBytePtr sd = new UBytePtr(src.line[0]);/* source data */
        int sw = ex - sx + 1;/* source width */
        int sh = ey - sy + 1;/* source height */
        int sm = src.line[1].offset - (src.line[0].offset);/* source modulo */
        UBytePtr dd = new UBytePtr(dest.line[sy], sx);/* dest data */
        int dm = (dest.line[1].offset) - (dest.line[0].offset);/* dest modulo */

        if (flipx != 0) {
            //if ((sx-ox) == 0) sd += gfx->width - sw;
            sd.inc(src.width - 1 - (sx - ox));
        } else {
            sd.inc(sx - ox);
        }

        if (flipy != 0) {
            //if ((sy-oy) == 0) sd += sm * (gfx->height - sh);
            //dd += dm * (sh - 1);
            //dm = -dm;
            sd.inc(sm * (src.height - 1 - (sy - oy)));
            sm = -sm;
        } else {
            sd.inc(sm * (sy - oy));
        }

        switch (transparency) {
            case TRANSPARENCY_NONE:
                throw new UnsupportedOperationException("Unsupported");
            /*TODO*///				BLOCKMOVE(NtoN_opaque_remap,flipx,(sd,sw,sh,sm,dd,dm,Machine->pens));
/*TODO*///				break;
/*TODO*///
            case TRANSPARENCY_NONE_RAW:
                if (flipx != 0) {
                    //throw new UnsupportedOperationException("Unsupported");
                    //blockmove_##function##_flipx##8 args ;//BLOCKMOVE(NtoN_opaque_noremap,flipx,(sd,sw,sh,sm,dd,dm));
                    blockmove_NtoN_opaque_noremap8_flipx(sd,sw,sh,sm,dd,dm);
                } else {
                    blockmove_NtoN_opaque_noremap8(sd, sw, sh, sm, dd, dm);
                }
                break;
            /*TODO*///
            case TRANSPARENCY_PEN_RAW:
                if (flipx != 0) {
                    throw new UnsupportedOperationException("Unsupported");
                    //BLOCKMOVE(NtoN_transpen_noremap,flipx,(sd,sw,sh,sm,dd,dm,transparent_color));
                } else {
                    blockmove_NtoN_transpen_noremap8(sd, sw, sh, sm, dd, dm, transparent_color);
                }
                break;

            case TRANSPARENCY_BLEND:
                throw new UnsupportedOperationException("Unsupported");
            /*TODO*///				BLOCKMOVE(NtoN_blend_remap,flipx,(sd,sw,sh,sm,dd,dm,Machine->pens,transparent_color));
/*TODO*///				break;
/*TODO*///
            case TRANSPARENCY_BLEND_RAW:
                throw new UnsupportedOperationException("Unsupported");
            /*TODO*///				BLOCKMOVE(NtoN_blend_noremap,flipx,(sd,sw,sh,sm,dd,dm,transparent_color));
/*TODO*///				break;

            default:
                usrintf_showmessage("copybitmap pen mode not supported");
                break;
        }
    }

    /*TODO*///
/*TODO*///DECLARE(copyrozbitmap_core,(struct osd_bitmap *bitmap,struct osd_bitmap *srcbitmap,
/*TODO*///		UINT32 startx,UINT32 starty,int incxx,int incxy,int incyx,int incyy,int wraparound,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color,UINT32 priority),
/*TODO*///{
/*TODO*///	UINT32 cx;
/*TODO*///	UINT32 cy;
/*TODO*///	int x;
/*TODO*///	int sx;
/*TODO*///	int sy;
/*TODO*///	int ex;
/*TODO*///	int ey;
/*TODO*///	const int xmask = srcbitmap->width-1;
/*TODO*///	const int ymask = srcbitmap->height-1;
/*TODO*///	const int widthshifted = srcbitmap->width << 16;
/*TODO*///	const int heightshifted = srcbitmap->height << 16;
/*TODO*///	DATA_TYPE *dest;
/*TODO*///
/*TODO*///
/*TODO*///	if (clip)
/*TODO*///	{
/*TODO*///		startx += clip->min_x * incxx + clip->min_y * incyx;
/*TODO*///		starty += clip->min_x * incxy + clip->min_y * incyy;
/*TODO*///
/*TODO*///		sx = clip->min_x;
/*TODO*///		sy = clip->min_y;
/*TODO*///		ex = clip->max_x;
/*TODO*///		ey = clip->max_y;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		sx = 0;
/*TODO*///		sy = 0;
/*TODO*///		ex = bitmap->width-1;
/*TODO*///		ey = bitmap->height-1;
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		int t;
/*TODO*///
/*TODO*///		t = startx; startx = starty; starty = t;
/*TODO*///		t = sx; sx = sy; sy = t;
/*TODO*///		t = ex; ex = ey; ey = t;
/*TODO*///		t = incxx; incxx = incyy; incyy = t;
/*TODO*///		t = incxy; incxy = incyx; incyx = t;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_X)
/*TODO*///	{
/*TODO*///		int w = ex - sx;
/*TODO*///
/*TODO*///		incxy = -incxy;
/*TODO*///		incyx = -incyx;
/*TODO*///		startx = widthshifted - startx - 1;
/*TODO*///		startx -= incxx * w;
/*TODO*///		starty -= incxy * w;
/*TODO*///
/*TODO*///		w = sx;
/*TODO*///		sx = bitmap->width-1 - ex;
/*TODO*///		ex = bitmap->width-1 - w;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_Y)
/*TODO*///	{
/*TODO*///		int h = ey - sy;
/*TODO*///
/*TODO*///		incxy = -incxy;
/*TODO*///		incyx = -incyx;
/*TODO*///		starty = heightshifted - starty - 1;
/*TODO*///		startx -= incyx * h;
/*TODO*///		starty -= incyy * h;
/*TODO*///
/*TODO*///		h = sy;
/*TODO*///		sy = bitmap->height-1 - ey;
/*TODO*///		ey = bitmap->height-1 - h;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (incxy == 0 && incyx == 0 && !wraparound)
/*TODO*///	{
/*TODO*///		/* optimized loop for the not rotated case */
/*TODO*///
/*TODO*///		if (incxx == 0x10000)
/*TODO*///		{
/*TODO*///			/* optimized loop for the not zoomed case */
/*TODO*///
/*TODO*///			/* startx is unsigned */
/*TODO*///			startx = ((INT32)startx) >> 16;
/*TODO*///
/*TODO*///			if (startx >= srcbitmap->width)
/*TODO*///			{
/*TODO*///				sx += -startx;
/*TODO*///				startx = 0;
/*TODO*///			}
/*TODO*///
/*TODO*///			if (sx <= ex)
/*TODO*///			{
/*TODO*///				while (sy <= ey)
/*TODO*///				{
/*TODO*///					if (starty < heightshifted)
/*TODO*///					{
/*TODO*///						x = sx;
/*TODO*///						cx = startx;
/*TODO*///						cy = starty >> 16;
/*TODO*///						dest = ((DATA_TYPE *)bitmap->line[sy]) + sx;
/*TODO*///						if (priority)
/*TODO*///						{
/*TODO*///							UINT8 *pri = &priority_bitmap->line[sy][sx];
/*TODO*///							DATA_TYPE *src = (DATA_TYPE *)srcbitmap->line[cy];
/*TODO*///
/*TODO*///							while (x <= ex && cx < srcbitmap->width)
/*TODO*///							{
/*TODO*///								int c = src[cx];
/*TODO*///
/*TODO*///								if (c != transparent_color)
/*TODO*///								{
/*TODO*///									*dest = c;
/*TODO*///									*pri |= priority;
/*TODO*///								}
/*TODO*///
/*TODO*///								cx++;
/*TODO*///								x++;
/*TODO*///								dest++;
/*TODO*///								pri++;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							DATA_TYPE *src = (DATA_TYPE *)srcbitmap->line[cy];
/*TODO*///
/*TODO*///							while (x <= ex && cx < srcbitmap->width)
/*TODO*///							{
/*TODO*///								int c = src[cx];
/*TODO*///
/*TODO*///								if (c != transparent_color)
/*TODO*///									*dest = c;
/*TODO*///
/*TODO*///								cx++;
/*TODO*///								x++;
/*TODO*///								dest++;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///					starty += incyy;
/*TODO*///					sy++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			while (startx >= widthshifted && sx <= ex)
/*TODO*///			{
/*TODO*///				startx += incxx;
/*TODO*///				sx++;
/*TODO*///			}
/*TODO*///
/*TODO*///			if (sx <= ex)
/*TODO*///			{
/*TODO*///				while (sy <= ey)
/*TODO*///				{
/*TODO*///					if (starty < heightshifted)
/*TODO*///					{
/*TODO*///						x = sx;
/*TODO*///						cx = startx;
/*TODO*///						cy = starty >> 16;
/*TODO*///						dest = ((DATA_TYPE *)bitmap->line[sy]) + sx;
/*TODO*///						if (priority)
/*TODO*///						{
/*TODO*///							UINT8 *pri = &priority_bitmap->line[sy][sx];
/*TODO*///							DATA_TYPE *src = (DATA_TYPE *)srcbitmap->line[cy];
/*TODO*///
/*TODO*///							while (x <= ex && cx < widthshifted)
/*TODO*///							{
/*TODO*///								int c = src[cx >> 16];
/*TODO*///
/*TODO*///								if (c != transparent_color)
/*TODO*///								{
/*TODO*///									*dest = c;
/*TODO*///									*pri |= priority;
/*TODO*///								}
/*TODO*///
/*TODO*///								cx += incxx;
/*TODO*///								x++;
/*TODO*///								dest++;
/*TODO*///								pri++;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							DATA_TYPE *src = (DATA_TYPE *)srcbitmap->line[cy];
/*TODO*///
/*TODO*///							while (x <= ex && cx < widthshifted)
/*TODO*///							{
/*TODO*///								int c = src[cx >> 16];
/*TODO*///
/*TODO*///								if (c != transparent_color)
/*TODO*///									*dest = c;
/*TODO*///
/*TODO*///								cx += incxx;
/*TODO*///								x++;
/*TODO*///								dest++;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///					starty += incyy;
/*TODO*///					sy++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		if (wraparound)
/*TODO*///		{
/*TODO*///			/* plot with wraparound */
/*TODO*///			while (sy <= ey)
/*TODO*///			{
/*TODO*///				x = sx;
/*TODO*///				cx = startx;
/*TODO*///				cy = starty;
/*TODO*///				dest = ((DATA_TYPE *)bitmap->line[sy]) + sx;
/*TODO*///				if (priority)
/*TODO*///				{
/*TODO*///					UINT8 *pri = &priority_bitmap->line[sy][sx];
/*TODO*///
/*TODO*///					while (x <= ex)
/*TODO*///					{
/*TODO*///						int c = ((DATA_TYPE *)srcbitmap->line[(cy >> 16) & ymask])[(cx >> 16) & xmask];
/*TODO*///
/*TODO*///						if (c != transparent_color)
/*TODO*///						{
/*TODO*///							*dest = c;
/*TODO*///							*pri |= priority;
/*TODO*///						}
/*TODO*///
/*TODO*///						cx += incxx;
/*TODO*///						cy += incxy;
/*TODO*///						x++;
/*TODO*///						dest++;
/*TODO*///						pri++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					while (x <= ex)
/*TODO*///					{
/*TODO*///						int c = ((DATA_TYPE *)srcbitmap->line[(cy >> 16) & ymask])[(cx >> 16) & xmask];
/*TODO*///
/*TODO*///						if (c != transparent_color)
/*TODO*///							*dest = c;
/*TODO*///
/*TODO*///						cx += incxx;
/*TODO*///						cy += incxy;
/*TODO*///						x++;
/*TODO*///						dest++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				startx += incyx;
/*TODO*///				starty += incyy;
/*TODO*///				sy++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			while (sy <= ey)
/*TODO*///			{
/*TODO*///				x = sx;
/*TODO*///				cx = startx;
/*TODO*///				cy = starty;
/*TODO*///				dest = ((DATA_TYPE *)bitmap->line[sy]) + sx;
/*TODO*///				if (priority)
/*TODO*///				{
/*TODO*///					UINT8 *pri = &priority_bitmap->line[sy][sx];
/*TODO*///
/*TODO*///					while (x <= ex)
/*TODO*///					{
/*TODO*///						if (cx < widthshifted && cy < heightshifted)
/*TODO*///						{
/*TODO*///							int c = ((DATA_TYPE *)srcbitmap->line[cy >> 16])[cx >> 16];
/*TODO*///
/*TODO*///							if (c != transparent_color)
/*TODO*///							{
/*TODO*///								*dest = c;
/*TODO*///								*pri |= priority;
/*TODO*///							}
/*TODO*///						}
/*TODO*///
/*TODO*///						cx += incxx;
/*TODO*///						cy += incxy;
/*TODO*///						x++;
/*TODO*///						dest++;
/*TODO*///						pri++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					while (x <= ex)
/*TODO*///					{
/*TODO*///						if (cx < widthshifted && cy < heightshifted)
/*TODO*///						{
/*TODO*///							int c = ((DATA_TYPE *)srcbitmap->line[cy >> 16])[cx >> 16];
/*TODO*///
/*TODO*///							if (c != transparent_color)
/*TODO*///								*dest = c;
/*TODO*///						}
/*TODO*///
/*TODO*///						cx += incxx;
/*TODO*///						cy += incxy;
/*TODO*///						x++;
/*TODO*///						dest++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				startx += incyx;
/*TODO*///				starty += incyy;
/*TODO*///				sy++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///#define ADJUST_FOR_ORIENTATION(type, orientation, bitmap, x, y)				\
/*TODO*///	type *dst = &((type *)bitmap->line[y])[x];								\
/*TODO*///	int xadv = 1;															\
/*TODO*///	if (orientation)														\
/*TODO*///	{																		\
/*TODO*///		int dy = bitmap->line[1] - bitmap->line[0];							\
/*TODO*///		int tx = x, ty = y, temp;											\
/*TODO*///		if (orientation & ORIENTATION_SWAP_XY)								\
/*TODO*///		{																	\
/*TODO*///			temp = tx; tx = ty; ty = temp;									\
/*TODO*///			xadv = dy / sizeof(type);										\
/*TODO*///		}																	\
/*TODO*///		if (orientation & ORIENTATION_FLIP_X)								\
/*TODO*///		{																	\
/*TODO*///			tx = bitmap->width - 1 - tx;									\
/*TODO*///			if (!(orientation & ORIENTATION_SWAP_XY)) xadv = -xadv;			\
/*TODO*///		}																	\
/*TODO*///		if (orientation & ORIENTATION_FLIP_Y)								\
/*TODO*///		{																	\
/*TODO*///			ty = bitmap->height - 1 - ty;									\
/*TODO*///			if (orientation & ORIENTATION_SWAP_XY) xadv = -xadv;			\
/*TODO*///		}																	\
/*TODO*///		/* can't lookup line because it may be negative! */					\
/*TODO*///		dst = (type *)(bitmap->line[0] + dy * ty) + tx;						\
/*TODO*///	}
/*TODO*///
/*TODO*///DECLAREG(draw_scanline, (
/*TODO*///		struct osd_bitmap *bitmap,int x,int y,int length,
/*TODO*///		const DATA_TYPE *src,UINT32 *pens,int transparent_pen),
/*TODO*///{
/*TODO*///	/* 8bpp destination */
/*TODO*///	if (bitmap->depth == 8)
/*TODO*///	{
/*TODO*///		/* adjust in case we're oddly oriented */
/*TODO*///		ADJUST_FOR_ORIENTATION(UINT8, Machine->orientation, bitmap, x, y);
/*TODO*///
/*TODO*///		/* with pen lookups */
/*TODO*///		if (pens)
/*TODO*///		{
/*TODO*///			if (transparent_pen == -1)
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					*dst = pens[*src++];
/*TODO*///					dst += xadv;
/*TODO*///				}
/*TODO*///			else
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					UINT32 spixel = *src++;
/*TODO*///					if (spixel != transparent_pen)
/*TODO*///						*dst = pens[spixel];
/*TODO*///					dst += xadv;
/*TODO*///				}
/*TODO*///		}
/*TODO*///
/*TODO*///		/* without pen lookups */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (transparent_pen == -1)
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					*dst = *src++;
/*TODO*///					dst += xadv;
/*TODO*///				}
/*TODO*///			else
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					UINT32 spixel = *src++;
/*TODO*///					if (spixel != transparent_pen)
/*TODO*///						*dst = spixel;
/*TODO*///					dst += xadv;
/*TODO*///				}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* 16bpp destination */
/*TODO*///	else if(bitmap->depth == 15 || bitmap->depth == 16)
/*TODO*///	{
/*TODO*///		/* adjust in case we're oddly oriented */
/*TODO*///		ADJUST_FOR_ORIENTATION(UINT16, Machine->orientation, bitmap, x, y);
/*TODO*///
/*TODO*///		/* with pen lookups */
/*TODO*///		if (pens)
/*TODO*///		{
/*TODO*///			if (transparent_pen == -1)
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					*dst = pens[*src++];
/*TODO*///					dst += xadv;
/*TODO*///				}
/*TODO*///			else
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					UINT32 spixel = *src++;
/*TODO*///					if (spixel != transparent_pen)
/*TODO*///						*dst = pens[spixel];
/*TODO*///					dst += xadv;
/*TODO*///				}
/*TODO*///		}
/*TODO*///
/*TODO*///		/* without pen lookups */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (transparent_pen == -1)
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					*dst = *src++;
/*TODO*///					dst += xadv;
/*TODO*///				}
/*TODO*///			else
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					UINT32 spixel = *src++;
/*TODO*///					if (spixel != transparent_pen)
/*TODO*///						*dst = spixel;
/*TODO*///					dst += xadv;
/*TODO*///				}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* 32bpp destination */
/*TODO*///	else
/*TODO*///	{
/*TODO*///		/* adjust in case we're oddly oriented */
/*TODO*///		ADJUST_FOR_ORIENTATION(UINT32, Machine->orientation, bitmap, x, y);
/*TODO*///
/*TODO*///		/* with pen lookups */
/*TODO*///		if (pens)
/*TODO*///		{
/*TODO*///			if (transparent_pen == -1)
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					*dst = pens[*src++];
/*TODO*///					dst += xadv;
/*TODO*///				}
/*TODO*///			else
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					UINT32 spixel = *src++;
/*TODO*///					if (spixel != transparent_pen)
/*TODO*///						*dst = pens[spixel];
/*TODO*///					dst += xadv;
/*TODO*///				}
/*TODO*///		}
/*TODO*///
/*TODO*///		/* without pen lookups */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (transparent_pen == -1)
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					*dst = *src++;
/*TODO*///					dst += xadv;
/*TODO*///				}
/*TODO*///			else
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					UINT32 spixel = *src++;
/*TODO*///					if (spixel != transparent_pen)
/*TODO*///						*dst = spixel;
/*TODO*///					dst += xadv;
/*TODO*///				}
/*TODO*///		}
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///#undef ADJUST_FOR_ORIENTATION
/*TODO*///
/*TODO*///#define ADJUST_FOR_ORIENTATION(type, orientation, bitmapi, bitmapp, x, y)	\
/*TODO*///	type *dsti = &((type *)bitmapi->line[y])[x];							\
/*TODO*///	UINT8 *dstp = &((UINT8 *)bitmapp->line[y])[x];							\
/*TODO*///	int xadv = 1;															\
/*TODO*///	if (orientation)														\
/*TODO*///	{																		\
/*TODO*///		int dy = bitmap->line[1] - bitmap->line[0];							\
/*TODO*///		int tx = x, ty = y, temp;											\
/*TODO*///		if ((orientation) & ORIENTATION_SWAP_XY)							\
/*TODO*///		{																	\
/*TODO*///			temp = tx; tx = ty; ty = temp;									\
/*TODO*///			xadv = dy / sizeof(type);										\
/*TODO*///		}																	\
/*TODO*///		if ((orientation) & ORIENTATION_FLIP_X)								\
/*TODO*///		{																	\
/*TODO*///			tx = bitmap->width - 1 - tx;									\
/*TODO*///			if (!((orientation) & ORIENTATION_SWAP_XY)) xadv = -xadv;		\
/*TODO*///		}																	\
/*TODO*///		if ((orientation) & ORIENTATION_FLIP_Y)								\
/*TODO*///		{																	\
/*TODO*///			ty = bitmap->height - 1 - ty;									\
/*TODO*///			if ((orientation) & ORIENTATION_SWAP_XY) xadv = -xadv;			\
/*TODO*///		}																	\
/*TODO*///		/* can't lookup line because it may be negative! */					\
/*TODO*///		dsti = (type *)(bitmapi->line[0] + dy * ty) + tx;					\
/*TODO*///		dstp = (UINT8 *)(bitmapp->line[0] + dy * ty / sizeof(type)) + tx;	\
/*TODO*///	}
/*TODO*///
/*TODO*///DECLAREG(pdraw_scanline, (
/*TODO*///		struct osd_bitmap *bitmap,int x,int y,int length,
/*TODO*///		const DATA_TYPE *src,UINT32 *pens,int transparent_pen,UINT32 orient,int pri),
/*TODO*///{
/*TODO*///	/* 8bpp destination */
/*TODO*///	if (bitmap->depth == 8)
/*TODO*///	{
/*TODO*///		/* adjust in case we're oddly oriented */
/*TODO*///		ADJUST_FOR_ORIENTATION(UINT8, orient^Machine->orientation, bitmap, priority_bitmap, x, y);
/*TODO*///
/*TODO*///		/* with pen lookups */
/*TODO*///		if (pens)
/*TODO*///		{
/*TODO*///			if (transparent_pen == -1)
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					*dsti = pens[*src++];
/*TODO*///					*dstp = pri;
/*TODO*///					dsti += xadv;
/*TODO*///					dstp += xadv;
/*TODO*///				}
/*TODO*///			else
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					UINT32 spixel = *src++;
/*TODO*///					if (spixel != transparent_pen)
/*TODO*///					{
/*TODO*///						*dsti = pens[spixel];
/*TODO*///						*dstp = pri;
/*TODO*///					}
/*TODO*///					dsti += xadv;
/*TODO*///					dstp += xadv;
/*TODO*///				}
/*TODO*///		}
/*TODO*///
/*TODO*///		/* without pen lookups */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (transparent_pen == -1)
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					*dsti = *src++;
/*TODO*///					*dstp = pri;
/*TODO*///					dsti += xadv;
/*TODO*///					dstp += xadv;
/*TODO*///				}
/*TODO*///			else
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					UINT32 spixel = *src++;
/*TODO*///					if (spixel != transparent_pen)
/*TODO*///					{
/*TODO*///						*dsti = spixel;
/*TODO*///						*dstp = pri;
/*TODO*///					}
/*TODO*///					dsti += xadv;
/*TODO*///					dstp += xadv;
/*TODO*///				}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* 16bpp destination */
/*TODO*///	else if(bitmap->depth == 15 || bitmap->depth == 16)
/*TODO*///	{
/*TODO*///		/* adjust in case we're oddly oriented */
/*TODO*///		ADJUST_FOR_ORIENTATION(UINT16, Machine->orientation ^ orient, bitmap, priority_bitmap, x, y);
/*TODO*///		/* with pen lookups */
/*TODO*///		if (pens)
/*TODO*///		{
/*TODO*///			if (transparent_pen == -1)
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					*dsti = pens[*src++];
/*TODO*///					*dstp = pri;
/*TODO*///					dsti += xadv;
/*TODO*///					dstp += xadv;
/*TODO*///				}
/*TODO*///			else
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					UINT32 spixel = *src++;
/*TODO*///					if (spixel != transparent_pen)
/*TODO*///					{
/*TODO*///						*dsti = pens[spixel];
/*TODO*///						*dstp = pri;
/*TODO*///					}
/*TODO*///					dsti += xadv;
/*TODO*///					dstp += xadv;
/*TODO*///				}
/*TODO*///		}
/*TODO*///
/*TODO*///		/* without pen lookups */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (transparent_pen == -1)
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					*dsti = *src++;
/*TODO*///					*dstp = pri;
/*TODO*///					dsti += xadv;
/*TODO*///					dstp += xadv;
/*TODO*///				}
/*TODO*///			else
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					UINT32 spixel = *src++;
/*TODO*///					if (spixel != transparent_pen)
/*TODO*///					{
/*TODO*///						*dsti = spixel;
/*TODO*///						*dstp = pri;
/*TODO*///					}
/*TODO*///					dsti += xadv;
/*TODO*///					dstp += xadv;
/*TODO*///				}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* 32bpp destination */
/*TODO*///	else
/*TODO*///	{
/*TODO*///		/* adjust in case we're oddly oriented */
/*TODO*///		ADJUST_FOR_ORIENTATION(UINT32, Machine->orientation ^ orient, bitmap, priority_bitmap, x, y);
/*TODO*///		/* with pen lookups */
/*TODO*///		if (pens)
/*TODO*///		{
/*TODO*///			if (transparent_pen == -1)
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					*dsti = pens[*src++];
/*TODO*///					*dstp = pri;
/*TODO*///					dsti += xadv;
/*TODO*///					dstp += xadv;
/*TODO*///				}
/*TODO*///			else
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					UINT32 spixel = *src++;
/*TODO*///					if (spixel != transparent_pen)
/*TODO*///					{
/*TODO*///						*dsti = pens[spixel];
/*TODO*///						*dstp = pri;
/*TODO*///					}
/*TODO*///					dsti += xadv;
/*TODO*///					dstp += xadv;
/*TODO*///				}
/*TODO*///		}
/*TODO*///
/*TODO*///		/* without pen lookups */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (transparent_pen == -1)
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					*dsti = *src++;
/*TODO*///					*dstp = pri;
/*TODO*///					dsti += xadv;
/*TODO*///					dstp += xadv;
/*TODO*///				}
/*TODO*///			else
/*TODO*///				while (length--)
/*TODO*///				{
/*TODO*///					UINT32 spixel = *src++;
/*TODO*///					if (spixel != transparent_pen)
/*TODO*///					{
/*TODO*///						*dsti = spixel;
/*TODO*///						*dstp = pri;
/*TODO*///					}
/*TODO*///					dsti += xadv;
/*TODO*///					dstp += xadv;
/*TODO*///				}
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///)
/*TODO*///
/*TODO*///#undef ADJUST_FOR_ORIENTATION
/*TODO*///
/*TODO*///#define ADJUST_FOR_ORIENTATION(type, orientation, bitmap, x, y)				\
/*TODO*///	type *src = &((type *)bitmap->line[y])[x];								\
/*TODO*///	int xadv = 1;															\
/*TODO*///	if (orientation)														\
/*TODO*///	{																		\
/*TODO*///		int dy = bitmap->line[1] - bitmap->line[0];							\
/*TODO*///		int tx = x, ty = y, temp;											\
/*TODO*///		if (orientation & ORIENTATION_SWAP_XY)								\
/*TODO*///		{																	\
/*TODO*///			temp = tx; tx = ty; ty = temp;									\
/*TODO*///			xadv = dy / sizeof(type);										\
/*TODO*///		}																	\
/*TODO*///		if (orientation & ORIENTATION_FLIP_X)								\
/*TODO*///		{																	\
/*TODO*///			tx = bitmap->width - 1 - tx;									\
/*TODO*///			if (!(orientation & ORIENTATION_SWAP_XY)) xadv = -xadv;			\
/*TODO*///		}																	\
/*TODO*///		if (orientation & ORIENTATION_FLIP_Y)								\
/*TODO*///		{																	\
/*TODO*///			ty = bitmap->height - 1 - ty;									\
/*TODO*///			if (orientation & ORIENTATION_SWAP_XY) xadv = -xadv;			\
/*TODO*///		}																	\
/*TODO*///		/* can't lookup line because it may be negative! */					\
/*TODO*///		src = (type *)(bitmap->line[0] + dy * ty) + tx;						\
/*TODO*///	}
/*TODO*///
/*TODO*///DECLAREG(extract_scanline, (
/*TODO*///		struct osd_bitmap *bitmap,int x,int y,int length,
/*TODO*///		DATA_TYPE *dst),
/*TODO*///{
/*TODO*///	/* 8bpp destination */
/*TODO*///	if (bitmap->depth == 8)
/*TODO*///	{
/*TODO*///		/* adjust in case we're oddly oriented */
/*TODO*///		ADJUST_FOR_ORIENTATION(UINT8, Machine->orientation, bitmap, x, y);
/*TODO*///
/*TODO*///		while (length--)
/*TODO*///		{
/*TODO*///			*dst++ = *src;
/*TODO*///			src += xadv;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* 16bpp destination */
/*TODO*///	else if(bitmap->depth == 15 || bitmap->depth == 16)
/*TODO*///	{
/*TODO*///		/* adjust in case we're oddly oriented */
/*TODO*///		ADJUST_FOR_ORIENTATION(UINT16, Machine->orientation, bitmap, x, y);
/*TODO*///
/*TODO*///		while (length--)
/*TODO*///		{
/*TODO*///			*dst++ = *src;
/*TODO*///			src += xadv;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* 32bpp destination */
/*TODO*///	else
/*TODO*///	{
/*TODO*///		/* adjust in case we're oddly oriented */
/*TODO*///		ADJUST_FOR_ORIENTATION(UINT32, Machine->orientation, bitmap, x, y);
/*TODO*///
/*TODO*///		while (length--)
/*TODO*///		{
/*TODO*///			*dst++ = *src;
/*TODO*///			src += xadv;
/*TODO*///		}
/*TODO*///	}
/*TODO*///})
/*TODO*///
/*TODO*///#undef ADJUST_FOR_ORIENTATION
/*TODO*///
/*TODO*///#endif /* DECLARE */
/*TODO*///    
}
