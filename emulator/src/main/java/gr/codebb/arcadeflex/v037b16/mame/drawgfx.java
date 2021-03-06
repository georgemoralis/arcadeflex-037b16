/*
 * ported to v0.37b16
 * 
 */
package gr.codebb.arcadeflex.v037b16.mame;

import common.ptr.UBytePtr;
import static gr.codebb.arcadeflex.WIP.v037b16.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b16.mame.driverH.*;
import gr.codebb.arcadeflex.v037b16.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b16.mame.usrintrf.usrintf_showmessage;
import static mame037b16.drawgfx.plot_pixel;
import static mame037b16.mame.Machine;

public class drawgfx {

    /*TODO*///#ifdef LSB_FIRST
/*TODO*///#define SHIFT0 0
/*TODO*///#define SHIFT1 8
/*TODO*///#define SHIFT2 16
/*TODO*///#define SHIFT3 24
/*TODO*///#else
/*TODO*///#define SHIFT3 0
/*TODO*///#define SHIFT2 8
/*TODO*///#define SHIFT1 16
/*TODO*///#define SHIFT0 24
/*TODO*///#endif
/*TODO*///
/*TODO*///
/*TODO*///UINT8 gfx_drawmode_table[256];
/*TODO*///plot_pixel_proc plot_pixel;
/*TODO*///read_pixel_proc read_pixel;
/*TODO*///plot_box_proc plot_box;
/*TODO*///mark_dirty_proc mark_dirty;
/*TODO*///
/*TODO*///static UINT8 is_raw[TRANSPARENCY_MODES];
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
/*TODO*///INLINE void write_dword(void *address, UINT32 data)
/*TODO*///{
/*TODO*///  	if ((long)address & 3)
/*TODO*///	{
/*TODO*///		*((UINT8 *)address)   = (data>>SHIFT0);
/*TODO*///		*((UINT8 *)address+1) = (data>>SHIFT1);
/*TODO*///		*((UINT8 *)address+2) = (data>>SHIFT2);
/*TODO*///		*((UINT8 *)address+3) = (data>>SHIFT3);
/*TODO*///		return;
/*TODO*///  	}
/*TODO*///  	else
/*TODO*///		*(UINT32 *)address = data;
/*TODO*///}
/*TODO*///#else
/*TODO*///#define read_dword(address) *(int *)address
/*TODO*///#define write_dword(address,data) *(int *)address=data
/*TODO*///#endif
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///INLINE int readbit(const UINT8 *src,int bitnum)
/*TODO*///{
/*TODO*///	return src[bitnum / 8] & (0x80 >> (bitnum % 8));
/*TODO*///}
/*TODO*///
/*TODO*///struct _alpha_cache alpha_cache;
/*TODO*///int alpha_active;
/*TODO*///
/*TODO*///void alpha_init(void)
/*TODO*///{
/*TODO*///	int lev, byte;
/*TODO*///	for(lev=0; lev<257; lev++)
/*TODO*///		for(byte=0; byte<256; byte++)
/*TODO*///			alpha_cache.alpha[lev][byte] = (byte*lev) >> 8;
/*TODO*///	alpha_set_level(255);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///static void calc_penusage(struct GfxElement *gfx,int num)
/*TODO*///{
/*TODO*///	int x,y;
/*TODO*///	UINT8 *dp;
/*TODO*///
/*TODO*///	if (!gfx->pen_usage) return;
/*TODO*///
/*TODO*///	/* fill the pen_usage array with info on the used pens */
/*TODO*///	gfx->pen_usage[num] = 0;
/*TODO*///
/*TODO*///	dp = gfx->gfxdata + num * gfx->char_modulo;
/*TODO*///
/*TODO*///	if (gfx->flags & GFX_PACKED)
/*TODO*///	{
/*TODO*///		for (y = 0;y < gfx->height;y++)
/*TODO*///		{
/*TODO*///			for (x = 0;x < gfx->width/2;x++)
/*TODO*///			{
/*TODO*///				gfx->pen_usage[num] |= 1 << (dp[x] & 0x0f);
/*TODO*///				gfx->pen_usage[num] |= 1 << (dp[x] >> 4);
/*TODO*///			}
/*TODO*///			dp += gfx->line_modulo;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		for (y = 0;y < gfx->height;y++)
/*TODO*///		{
/*TODO*///			for (x = 0;x < gfx->width;x++)
/*TODO*///			{
/*TODO*///				gfx->pen_usage[num] |= 1 << dp[x];
/*TODO*///			}
/*TODO*///			dp += gfx->line_modulo;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void decodechar(struct GfxElement *gfx,int num,const UINT8 *src,const struct GfxLayout *gl)
/*TODO*///{
/*TODO*///	int plane,x,y;
/*TODO*///	UINT8 *dp;
/*TODO*///	int baseoffs;
/*TODO*///	const UINT32 *xoffset,*yoffset;
/*TODO*///
/*TODO*///
/*TODO*///	xoffset = gl->xoffset;
/*TODO*///	yoffset = gl->yoffset;
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		const UINT32 *t = xoffset; xoffset = yoffset; yoffset = t;
/*TODO*///	}
/*TODO*///	if (gfx->flags & GFX_SWAPXY)
/*TODO*///	{
/*TODO*///		const UINT32 *t = xoffset; xoffset = yoffset; yoffset = t;
/*TODO*///	}
/*TODO*///
/*TODO*///	dp = gfx->gfxdata + num * gfx->char_modulo;
/*TODO*///	memset(dp,0,gfx->char_modulo);
/*TODO*///
/*TODO*///	baseoffs = num * gl->charincrement;
/*TODO*///
/*TODO*///	if (gfx->flags & GFX_PACKED)
/*TODO*///	{
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
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		for (plane = 0;plane < gl->planes;plane++)
/*TODO*///		{
/*TODO*///			int shiftedbit = 1 << (gl->planes-1-plane);
/*TODO*///			int offs = baseoffs + gl->planeoffset[plane];
/*TODO*///
/*TODO*///			dp = gfx->gfxdata + num * gfx->char_modulo + (gfx->height-1) * gfx->line_modulo;
/*TODO*///
/*TODO*///#ifdef PREROTATE_GFX
/*TODO*///			y = gfx->height;
/*TODO*///			while (--y >= 0)
/*TODO*///			{
/*TODO*///				int yoffs;
/*TODO*///
/*TODO*///				yoffs = y;
/*TODO*///				if (Machine->orientation & ORIENTATION_FLIP_Y)
/*TODO*///					yoffs = gfx->height-1 - yoffs;
/*TODO*///
/*TODO*///				x = gfx->width;
/*TODO*///				while (--x >= 0)
/*TODO*///				{
/*TODO*///					int xoffs;
/*TODO*///
/*TODO*///					xoffs = x;
/*TODO*///					if (Machine->orientation & ORIENTATION_FLIP_X)
/*TODO*///						xoffs = gfx->width-1 - xoffs;
/*TODO*///
/*TODO*///					if (readbit(src,offs + xoffset[xoffs] + yoffset[yoffs]))
/*TODO*///						dp[x] |= shiftedbit;
/*TODO*///				}
/*TODO*///				dp -= gfx->line_modulo;
/*TODO*///			}
/*TODO*///#else
/*TODO*///			y = gfx->height;
/*TODO*///			while (--y >= 0)
/*TODO*///			{
/*TODO*///				int offs2 = offs + yoffset[y];
/*TODO*///
/*TODO*///				x = gfx->width;
/*TODO*///				while (--x >= 0)
/*TODO*///				{
/*TODO*///					if (readbit(src,offs2 + xoffset[x]))
/*TODO*///						dp[x] |= shiftedbit;
/*TODO*///				}
/*TODO*///				dp -= gfx->line_modulo;
/*TODO*///			}
/*TODO*///#endif
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	calc_penusage(gfx,num);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///struct GfxElement *decodegfx(const UINT8 *src,const struct GfxLayout *gl)
/*TODO*///{
/*TODO*///	int c;
/*TODO*///	struct GfxElement *gfx;
/*TODO*///
/*TODO*///
/*TODO*///	if ((gfx = malloc(sizeof(struct GfxElement))) == 0)
/*TODO*///		return 0;
/*TODO*///	memset(gfx,0,sizeof(struct GfxElement));
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///#ifndef NOPRESWAP
/*TODO*///		gfx->width = gl->height;
/*TODO*///		gfx->height = gl->width;
/*TODO*///#else
/*TODO*///		gfx->width = gl->width;
/*TODO*///		gfx->height = gl->height;
/*TODO*///		gfx->flags |= GFX_SWAPXY;
/*TODO*///#endif
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		gfx->width = gl->width;
/*TODO*///		gfx->height = gl->height;
/*TODO*///	}
/*TODO*///
/*TODO*///	gfx->total_elements = gl->total;
/*TODO*///	gfx->color_granularity = 1 << gl->planes;
/*TODO*///
/*TODO*///	gfx->pen_usage = 0; /* need to make sure this is NULL if the next test fails) */
/*TODO*///	if (gfx->color_granularity <= 32)	/* can't handle more than 32 pens */
/*TODO*///		gfx->pen_usage = malloc(gfx->total_elements * sizeof(int));
/*TODO*///		/* no need to check for failure, the code can work without pen_usage */
/*TODO*///
/*TODO*///	if (gl->planeoffset[0] == GFX_RAW)
/*TODO*///	{
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
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		if (0 && gl->planes <= 4 && !(gfx->width & 1))
/*TODO*/////		if (gl->planes <= 4 && !(gfx->width & 1))
/*TODO*///		{
/*TODO*///			gfx->flags |= GFX_PACKED;
/*TODO*///			gfx->line_modulo = gfx->width/2;
/*TODO*///		}
/*TODO*///		else
/*TODO*///			gfx->line_modulo = gfx->width;
/*TODO*///		gfx->char_modulo = gfx->line_modulo * gfx->height;
/*TODO*///
/*TODO*///		if ((gfx->gfxdata = malloc(gfx->total_elements * gfx->char_modulo * sizeof(UINT8))) == 0)
/*TODO*///		{
/*TODO*///			free(gfx->pen_usage);
/*TODO*///			free(gfx);
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		for (c = 0;c < gfx->total_elements;c++)
/*TODO*///			decodechar(gfx,c,src,gl);
/*TODO*///	}
/*TODO*///
/*TODO*///	return gfx;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void freegfx(struct GfxElement *gfx)
/*TODO*///{
/*TODO*///	if (gfx)
/*TODO*///	{
/*TODO*///		free(gfx->pen_usage);
/*TODO*///		if (!(gfx->flags & GFX_DONT_FREE_GFXDATA))
/*TODO*///			free(gfx->gfxdata);
/*TODO*///		free(gfx);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///INLINE void blockmove_NtoN_transpen_noremap8(
/*TODO*///		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		UINT8 *dstdata,int dstmodulo,
/*TODO*///		int transpen)
/*TODO*///{
/*TODO*///	UINT8 *end;
/*TODO*///	int trans4;
/*TODO*///	UINT32 *sd4;
/*TODO*///
/*TODO*///	srcmodulo -= srcwidth;
/*TODO*///	dstmodulo -= srcwidth;
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
/*TODO*///			col = *(srcdata++);
/*TODO*///			if (col != transpen) *dstdata = col;
/*TODO*///			dstdata++;
/*TODO*///		}
/*TODO*///		sd4 = (UINT32 *)srcdata;
/*TODO*///		while (dstdata <= end - 4)
/*TODO*///		{
/*TODO*///			UINT32 col4;
/*TODO*///
/*TODO*///			if ((col4 = *(sd4++)) != trans4)
/*TODO*///			{
/*TODO*///				UINT32 xod4;
/*TODO*///
/*TODO*///				xod4 = col4 ^ trans4;
/*TODO*///				if( (xod4&0x000000ff) && (xod4&0x0000ff00) &&
/*TODO*///					(xod4&0x00ff0000) && (xod4&0xff000000) )
/*TODO*///				{
/*TODO*///					write_dword((UINT32 *)dstdata,col4);
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if (xod4 & (0xff<<SHIFT0)) dstdata[0] = col4>>SHIFT0;
/*TODO*///					if (xod4 & (0xff<<SHIFT1)) dstdata[1] = col4>>SHIFT1;
/*TODO*///					if (xod4 & (0xff<<SHIFT2)) dstdata[2] = col4>>SHIFT2;
/*TODO*///					if (xod4 & (0xff<<SHIFT3)) dstdata[3] = col4>>SHIFT3;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			dstdata += 4;
/*TODO*///		}
/*TODO*///		srcdata = (UINT8 *)sd4;
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
/*TODO*///INLINE void common_drawgfx(struct osd_bitmap *dest,const struct GfxElement *gfx,
/*TODO*///		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color,
/*TODO*///		struct osd_bitmap *pri_buffer,UINT32 pri_mask)
/*TODO*///{
/*TODO*///	struct rectangle myclip;
/*TODO*///
/*TODO*///	if (!gfx)
/*TODO*///	{
/*TODO*///		usrintf_showmessage("drawgfx() gfx == 0");
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	if (!gfx->colortable && !is_raw[transparency])
/*TODO*///	{
/*TODO*///		usrintf_showmessage("drawgfx() gfx->colortable == 0");
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	code %= gfx->total_elements;
/*TODO*///	if (!is_raw[transparency])
/*TODO*///		color %= gfx->total_colors;
/*TODO*///
/*TODO*///	if (!alpha_active && (transparency == TRANSPARENCY_ALPHAONE || transparency == TRANSPARENCY_ALPHA))
/*TODO*///	{
/*TODO*///		if (transparency == TRANSPARENCY_ALPHAONE && (cpu_getcurrentframe() & 1))
/*TODO*///		{
/*TODO*///			transparency = TRANSPARENCY_PENS;
/*TODO*///			transparent_color = (1 << (transparent_color & 0xff))|(1 << (transparent_color >> 8));
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			transparency = TRANSPARENCY_PEN;
/*TODO*///			transparent_color &= 0xff;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (gfx->pen_usage && (transparency == TRANSPARENCY_PEN || transparency == TRANSPARENCY_PENS))
/*TODO*///	{
/*TODO*///		int transmask = 0;
/*TODO*///
/*TODO*///		if (transparency == TRANSPARENCY_PEN)
/*TODO*///		{
/*TODO*///			transmask = 1 << (transparent_color & 0xff);
/*TODO*///		}
/*TODO*///		else	/* transparency == TRANSPARENCY_PENS */
/*TODO*///		{
/*TODO*///			transmask = transparent_color;
/*TODO*///		}
/*TODO*///
/*TODO*///		if ((gfx->pen_usage[code] & ~transmask) == 0)
/*TODO*///			/* character is totally transparent, no need to draw */
/*TODO*///			return;
/*TODO*///		else if ((gfx->pen_usage[code] & transmask) == 0)
/*TODO*///			/* character is totally opaque, can disable transparency */
/*TODO*///			transparency = TRANSPARENCY_NONE;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		int temp;
/*TODO*///
/*TODO*///		temp = sx;
/*TODO*///		sx = sy;
/*TODO*///		sy = temp;
/*TODO*///
/*TODO*///		temp = flipx;
/*TODO*///		flipx = flipy;
/*TODO*///		flipy = temp;
/*TODO*///
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_x;
/*TODO*///			myclip.min_x = clip->min_y;
/*TODO*///			myclip.min_y = temp;
/*TODO*///			temp = clip->max_x;
/*TODO*///			myclip.max_x = clip->max_y;
/*TODO*///			myclip.max_y = temp;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_X)
/*TODO*///	{
/*TODO*///		sx = dest->width - gfx->width - sx;
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_x;
/*TODO*///			myclip.min_x = dest->width-1 - clip->max_x;
/*TODO*///			myclip.max_x = dest->width-1 - temp;
/*TODO*///			myclip.min_y = clip->min_y;
/*TODO*///			myclip.max_y = clip->max_y;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///#ifndef PREROTATE_GFX
/*TODO*///		flipx = !flipx;
/*TODO*///#endif
/*TODO*///	}
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_Y)
/*TODO*///	{
/*TODO*///		sy = dest->height - gfx->height - sy;
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
/*TODO*///			myclip.min_x = clip->min_x;
/*TODO*///			myclip.max_x = clip->max_x;
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_y;
/*TODO*///			myclip.min_y = dest->height-1 - clip->max_y;
/*TODO*///			myclip.max_y = dest->height-1 - temp;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///#ifndef PREROTATE_GFX
/*TODO*///		flipy = !flipy;
/*TODO*///#endif
/*TODO*///	}
/*TODO*///
/*TODO*///	if (dest->depth == 8)
/*TODO*///		drawgfx_core8(dest,gfx,code,color,flipx,flipy,sx,sy,clip,transparency,transparent_color,pri_buffer,pri_mask);
/*TODO*///	else if(dest->depth == 15 || dest->depth == 16)
/*TODO*///		drawgfx_core16(dest,gfx,code,color,flipx,flipy,sx,sy,clip,transparency,transparent_color,pri_buffer,pri_mask);
/*TODO*///	else
/*TODO*///		drawgfx_core32(dest,gfx,code,color,flipx,flipy,sx,sy,clip,transparency,transparent_color,pri_buffer,pri_mask);
/*TODO*///}
/*TODO*///
/*TODO*///void drawgfx(struct osd_bitmap *dest,const struct GfxElement *gfx,
/*TODO*///		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color)
/*TODO*///{
/*TODO*///	profiler_mark(PROFILER_DRAWGFX);
/*TODO*///	common_drawgfx(dest,gfx,code,color,flipx,flipy,sx,sy,clip,transparency,transparent_color,NULL,0);
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///}
/*TODO*///
/*TODO*///void pdrawgfx(struct osd_bitmap *dest,const struct GfxElement *gfx,
/*TODO*///		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color,UINT32 priority_mask)
/*TODO*///{
/*TODO*///	profiler_mark(PROFILER_DRAWGFX);
/*TODO*///	common_drawgfx(dest,gfx,code,color,flipx,flipy,sx,sy,clip,transparency,transparent_color,priority_bitmap,priority_mask | (1<<31));
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///}
/*TODO*///
/*TODO*///void mdrawgfx(struct osd_bitmap *dest,const struct GfxElement *gfx,
/*TODO*///		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color,UINT32 priority_mask)
/*TODO*///{
/*TODO*///	profiler_mark(PROFILER_DRAWGFX);
/*TODO*///	common_drawgfx(dest,gfx,code,color,flipx,flipy,sx,sy,clip,transparency,transparent_color,priority_bitmap,priority_mask);
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Use drawgfx() to copy a bitmap onto another at the given position.
/*TODO*///  This function will very likely change in the future.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///void copybitmap(struct osd_bitmap *dest,struct osd_bitmap *src,int flipx,int flipy,int sx,int sy,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color)
/*TODO*///{
/*TODO*///	/* translate to proper transparency here */
/*TODO*///	if (transparency == TRANSPARENCY_NONE)
/*TODO*///		transparency = TRANSPARENCY_NONE_RAW;
/*TODO*///	else if (transparency == TRANSPARENCY_PEN)
/*TODO*///		transparency = TRANSPARENCY_PEN_RAW;
/*TODO*///	else if (transparency == TRANSPARENCY_COLOR)
/*TODO*///	{
/*TODO*///		transparent_color = Machine->pens[transparent_color];
/*TODO*///		transparency = TRANSPARENCY_PEN_RAW;
/*TODO*///	}
/*TODO*///
/*TODO*///	copybitmap_remap(dest,src,flipx,flipy,sx,sy,clip,transparency,transparent_color);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void copybitmap_remap(struct osd_bitmap *dest,struct osd_bitmap *src,int flipx,int flipy,int sx,int sy,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color)
/*TODO*///{
/*TODO*///	struct rectangle myclip;
/*TODO*///
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_COPYBITMAP);
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		int temp;
/*TODO*///
/*TODO*///		temp = sx;
/*TODO*///		sx = sy;
/*TODO*///		sy = temp;
/*TODO*///
/*TODO*///		temp = flipx;
/*TODO*///		flipx = flipy;
/*TODO*///		flipy = temp;
/*TODO*///
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_x;
/*TODO*///			myclip.min_x = clip->min_y;
/*TODO*///			myclip.min_y = temp;
/*TODO*///			temp = clip->max_x;
/*TODO*///			myclip.max_x = clip->max_y;
/*TODO*///			myclip.max_y = temp;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_X)
/*TODO*///	{
/*TODO*///		sx = dest->width - src->width - sx;
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_x;
/*TODO*///			myclip.min_x = dest->width-1 - clip->max_x;
/*TODO*///			myclip.max_x = dest->width-1 - temp;
/*TODO*///			myclip.min_y = clip->min_y;
/*TODO*///			myclip.max_y = clip->max_y;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_Y)
/*TODO*///	{
/*TODO*///		sy = dest->height - src->height - sy;
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
/*TODO*///			myclip.min_x = clip->min_x;
/*TODO*///			myclip.max_x = clip->max_x;
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_y;
/*TODO*///			myclip.min_y = dest->height-1 - clip->max_y;
/*TODO*///			myclip.max_y = dest->height-1 - temp;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (dest->depth == 8)
/*TODO*///		copybitmap_core8(dest,src,flipx,flipy,sx,sy,clip,transparency,transparent_color);
/*TODO*///	else if(dest->depth == 15 || dest->depth == 16)
/*TODO*///		copybitmap_core16(dest,src,flipx,flipy,sx,sy,clip,transparency,transparent_color);
/*TODO*///	else
/*TODO*///		copybitmap_core32(dest,src,flipx,flipy,sx,sy,clip,transparency,transparent_color);
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Copy a bitmap onto another with scroll and wraparound.
/*TODO*///  This function supports multiple independently scrolling rows/columns.
/*TODO*///  "rows" is the number of indepentently scrolling rows. "rowscroll" is an
/*TODO*///  array of integers telling how much to scroll each row. Same thing for
/*TODO*///  "cols" and "colscroll".
/*TODO*///  If the bitmap cannot scroll in one direction, set rows or columns to 0.
/*TODO*///  If the bitmap scrolls as a whole, set rows and/or cols to 1.
/*TODO*///  Bidirectional scrolling is, of course, supported only if the bitmap
/*TODO*///  scrolls as a whole in at least one direction.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///void copyscrollbitmap(struct osd_bitmap *dest,struct osd_bitmap *src,
/*TODO*///		int rows,const int *rowscroll,int cols,const int *colscroll,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color)
/*TODO*///{
/*TODO*///	/* translate to proper transparency here */
/*TODO*///	if (transparency == TRANSPARENCY_NONE)
/*TODO*///		transparency = TRANSPARENCY_NONE_RAW;
/*TODO*///	else if (transparency == TRANSPARENCY_PEN)
/*TODO*///		transparency = TRANSPARENCY_PEN_RAW;
/*TODO*///	else if (transparency == TRANSPARENCY_COLOR)
/*TODO*///	{
/*TODO*///		transparent_color = Machine->pens[transparent_color];
/*TODO*///		transparency = TRANSPARENCY_PEN_RAW;
/*TODO*///	}
/*TODO*///
/*TODO*///	copyscrollbitmap_remap(dest,src,rows,rowscroll,cols,colscroll,clip,transparency,transparent_color);
/*TODO*///}
/*TODO*///
/*TODO*///void copyscrollbitmap_remap(struct osd_bitmap *dest,struct osd_bitmap *src,
/*TODO*///		int rows,const int *rowscroll,int cols,const int *colscroll,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color)
/*TODO*///{
/*TODO*///	int srcwidth,srcheight,destwidth,destheight;
/*TODO*///	struct rectangle orig_clip;
/*TODO*///
/*TODO*///
/*TODO*///	if (clip)
/*TODO*///	{
/*TODO*///		orig_clip.min_x = clip->min_x;
/*TODO*///		orig_clip.max_x = clip->max_x;
/*TODO*///		orig_clip.min_y = clip->min_y;
/*TODO*///		orig_clip.max_y = clip->max_y;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		orig_clip.min_x = 0;
/*TODO*///		orig_clip.max_x = dest->width-1;
/*TODO*///		orig_clip.min_y = 0;
/*TODO*///		orig_clip.max_y = dest->height-1;
/*TODO*///	}
/*TODO*///	clip = &orig_clip;
/*TODO*///
/*TODO*///	if (rows == 0 && cols == 0)
/*TODO*///	{
/*TODO*///		copybitmap(dest,src,0,0,0,0,clip,transparency,transparent_color);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_COPYBITMAP);
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		srcwidth = src->height;
/*TODO*///		srcheight = src->width;
/*TODO*///		destwidth = dest->height;
/*TODO*///		destheight = dest->width;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		srcwidth = src->width;
/*TODO*///		srcheight = src->height;
/*TODO*///		destwidth = dest->width;
/*TODO*///		destheight = dest->height;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (rows == 0)
/*TODO*///	{
/*TODO*///		/* scrolling columns */
/*TODO*///		int col,colwidth;
/*TODO*///		struct rectangle myclip;
/*TODO*///
/*TODO*///
/*TODO*///		colwidth = srcwidth / cols;
/*TODO*///
/*TODO*///		myclip.min_y = clip->min_y;
/*TODO*///		myclip.max_y = clip->max_y;
/*TODO*///
/*TODO*///		col = 0;
/*TODO*///		while (col < cols)
/*TODO*///		{
/*TODO*///			int cons,scroll;
/*TODO*///
/*TODO*///
/*TODO*///			/* count consecutive columns scrolled by the same amount */
/*TODO*///			scroll = colscroll[col];
/*TODO*///			cons = 1;
/*TODO*///			while (col + cons < cols &&	colscroll[col + cons] == scroll)
/*TODO*///				cons++;
/*TODO*///
/*TODO*///			if (scroll < 0) scroll = srcheight - (-scroll) % srcheight;
/*TODO*///			else scroll %= srcheight;
/*TODO*///
/*TODO*///			myclip.min_x = col * colwidth;
/*TODO*///			if (myclip.min_x < clip->min_x) myclip.min_x = clip->min_x;
/*TODO*///			myclip.max_x = (col + cons) * colwidth - 1;
/*TODO*///			if (myclip.max_x > clip->max_x) myclip.max_x = clip->max_x;
/*TODO*///
/*TODO*///			copybitmap(dest,src,0,0,0,scroll,&myclip,transparency,transparent_color);
/*TODO*///			copybitmap(dest,src,0,0,0,scroll - srcheight,&myclip,transparency,transparent_color);
/*TODO*///
/*TODO*///			col += cons;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else if (cols == 0)
/*TODO*///	{
/*TODO*///		/* scrolling rows */
/*TODO*///		int row,rowheight;
/*TODO*///		struct rectangle myclip;
/*TODO*///
/*TODO*///
/*TODO*///		rowheight = srcheight / rows;
/*TODO*///
/*TODO*///		myclip.min_x = clip->min_x;
/*TODO*///		myclip.max_x = clip->max_x;
/*TODO*///
/*TODO*///		row = 0;
/*TODO*///		while (row < rows)
/*TODO*///		{
/*TODO*///			int cons,scroll;
/*TODO*///
/*TODO*///
/*TODO*///			/* count consecutive rows scrolled by the same amount */
/*TODO*///			scroll = rowscroll[row];
/*TODO*///			cons = 1;
/*TODO*///			while (row + cons < rows &&	rowscroll[row + cons] == scroll)
/*TODO*///				cons++;
/*TODO*///
/*TODO*///			if (scroll < 0) scroll = srcwidth - (-scroll) % srcwidth;
/*TODO*///			else scroll %= srcwidth;
/*TODO*///
/*TODO*///			myclip.min_y = row * rowheight;
/*TODO*///			if (myclip.min_y < clip->min_y) myclip.min_y = clip->min_y;
/*TODO*///			myclip.max_y = (row + cons) * rowheight - 1;
/*TODO*///			if (myclip.max_y > clip->max_y) myclip.max_y = clip->max_y;
/*TODO*///
/*TODO*///			copybitmap(dest,src,0,0,scroll,0,&myclip,transparency,transparent_color);
/*TODO*///			copybitmap(dest,src,0,0,scroll - srcwidth,0,&myclip,transparency,transparent_color);
/*TODO*///
/*TODO*///			row += cons;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else if (rows == 1 && cols == 1)
/*TODO*///	{
/*TODO*///		/* XY scrolling playfield */
/*TODO*///		int scrollx,scrolly,sx,sy;
/*TODO*///
/*TODO*///
/*TODO*///		if (rowscroll[0] < 0) scrollx = srcwidth - (-rowscroll[0]) % srcwidth;
/*TODO*///		else scrollx = rowscroll[0] % srcwidth;
/*TODO*///
/*TODO*///		if (colscroll[0] < 0) scrolly = srcheight - (-colscroll[0]) % srcheight;
/*TODO*///		else scrolly = colscroll[0] % srcheight;
/*TODO*///
/*TODO*///		for (sx = scrollx - srcwidth;sx < destwidth;sx += srcwidth)
/*TODO*///			for (sy = scrolly - srcheight;sy < destheight;sy += srcheight)
/*TODO*///				copybitmap(dest,src,0,0,sx,sy,clip,transparency,transparent_color);
/*TODO*///	}
/*TODO*///	else if (rows == 1)
/*TODO*///	{
/*TODO*///		/* scrolling columns + horizontal scroll */
/*TODO*///		int col,colwidth;
/*TODO*///		int scrollx;
/*TODO*///		struct rectangle myclip;
/*TODO*///
/*TODO*///
/*TODO*///		if (rowscroll[0] < 0) scrollx = srcwidth - (-rowscroll[0]) % srcwidth;
/*TODO*///		else scrollx = rowscroll[0] % srcwidth;
/*TODO*///
/*TODO*///		colwidth = srcwidth / cols;
/*TODO*///
/*TODO*///		myclip.min_y = clip->min_y;
/*TODO*///		myclip.max_y = clip->max_y;
/*TODO*///
/*TODO*///		col = 0;
/*TODO*///		while (col < cols)
/*TODO*///		{
/*TODO*///			int cons,scroll;
/*TODO*///
/*TODO*///
/*TODO*///			/* count consecutive columns scrolled by the same amount */
/*TODO*///			scroll = colscroll[col];
/*TODO*///			cons = 1;
/*TODO*///			while (col + cons < cols &&	colscroll[col + cons] == scroll)
/*TODO*///				cons++;
/*TODO*///
/*TODO*///			if (scroll < 0) scroll = srcheight - (-scroll) % srcheight;
/*TODO*///			else scroll %= srcheight;
/*TODO*///
/*TODO*///			myclip.min_x = col * colwidth + scrollx;
/*TODO*///			if (myclip.min_x < clip->min_x) myclip.min_x = clip->min_x;
/*TODO*///			myclip.max_x = (col + cons) * colwidth - 1 + scrollx;
/*TODO*///			if (myclip.max_x > clip->max_x) myclip.max_x = clip->max_x;
/*TODO*///
/*TODO*///			copybitmap(dest,src,0,0,scrollx,scroll,&myclip,transparency,transparent_color);
/*TODO*///			copybitmap(dest,src,0,0,scrollx,scroll - srcheight,&myclip,transparency,transparent_color);
/*TODO*///
/*TODO*///			myclip.min_x = col * colwidth + scrollx - srcwidth;
/*TODO*///			if (myclip.min_x < clip->min_x) myclip.min_x = clip->min_x;
/*TODO*///			myclip.max_x = (col + cons) * colwidth - 1 + scrollx - srcwidth;
/*TODO*///			if (myclip.max_x > clip->max_x) myclip.max_x = clip->max_x;
/*TODO*///
/*TODO*///			copybitmap(dest,src,0,0,scrollx - srcwidth,scroll,&myclip,transparency,transparent_color);
/*TODO*///			copybitmap(dest,src,0,0,scrollx - srcwidth,scroll - srcheight,&myclip,transparency,transparent_color);
/*TODO*///
/*TODO*///			col += cons;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else if (cols == 1)
/*TODO*///	{
/*TODO*///		/* scrolling rows + vertical scroll */
/*TODO*///		int row,rowheight;
/*TODO*///		int scrolly;
/*TODO*///		struct rectangle myclip;
/*TODO*///
/*TODO*///
/*TODO*///		if (colscroll[0] < 0) scrolly = srcheight - (-colscroll[0]) % srcheight;
/*TODO*///		else scrolly = colscroll[0] % srcheight;
/*TODO*///
/*TODO*///		rowheight = srcheight / rows;
/*TODO*///
/*TODO*///		myclip.min_x = clip->min_x;
/*TODO*///		myclip.max_x = clip->max_x;
/*TODO*///
/*TODO*///		row = 0;
/*TODO*///		while (row < rows)
/*TODO*///		{
/*TODO*///			int cons,scroll;
/*TODO*///
/*TODO*///
/*TODO*///			/* count consecutive rows scrolled by the same amount */
/*TODO*///			scroll = rowscroll[row];
/*TODO*///			cons = 1;
/*TODO*///			while (row + cons < rows &&	rowscroll[row + cons] == scroll)
/*TODO*///				cons++;
/*TODO*///
/*TODO*///			if (scroll < 0) scroll = srcwidth - (-scroll) % srcwidth;
/*TODO*///			else scroll %= srcwidth;
/*TODO*///
/*TODO*///			myclip.min_y = row * rowheight + scrolly;
/*TODO*///			if (myclip.min_y < clip->min_y) myclip.min_y = clip->min_y;
/*TODO*///			myclip.max_y = (row + cons) * rowheight - 1 + scrolly;
/*TODO*///			if (myclip.max_y > clip->max_y) myclip.max_y = clip->max_y;
/*TODO*///
/*TODO*///			copybitmap(dest,src,0,0,scroll,scrolly,&myclip,transparency,transparent_color);
/*TODO*///			copybitmap(dest,src,0,0,scroll - srcwidth,scrolly,&myclip,transparency,transparent_color);
/*TODO*///
/*TODO*///			myclip.min_y = row * rowheight + scrolly - srcheight;
/*TODO*///			if (myclip.min_y < clip->min_y) myclip.min_y = clip->min_y;
/*TODO*///			myclip.max_y = (row + cons) * rowheight - 1 + scrolly - srcheight;
/*TODO*///			if (myclip.max_y > clip->max_y) myclip.max_y = clip->max_y;
/*TODO*///
/*TODO*///			copybitmap(dest,src,0,0,scroll,scrolly - srcheight,&myclip,transparency,transparent_color);
/*TODO*///			copybitmap(dest,src,0,0,scroll - srcwidth,scrolly - srcheight,&myclip,transparency,transparent_color);
/*TODO*///
/*TODO*///			row += cons;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///}

    public static void copyrozbitmap_core8(osd_bitmap bitmap, osd_bitmap srcbitmap,
            long u32_startx, long u32_starty, int incxx, int incxy, int incyx, int incyy, int wraparound,
            rectangle clip, int transparency, int transparent_color,/*UINT32*/ int priority) {
        long u32_cx;
        long u32_cy;
        int x;
        int sx;
        int sy;
        int ex;
        int ey;
        int xmask = srcbitmap.width - 1;
        int ymask = srcbitmap.height - 1;
        int widthshifted = srcbitmap.width << 16;
        int heightshifted = srcbitmap.height << 16;
        UBytePtr dest;

        if (clip != null) {
            u32_startx = (u32_startx + clip.min_x * incxx + clip.min_y * incyx) & 0xFFFFFFFFL;
            u32_starty = (u32_starty + clip.min_x * incxy + clip.min_y * incyy) & 0xFFFFFFFFL;

            sx = clip.min_x;
            sy = clip.min_y;
            ex = clip.max_x;
            ey = clip.max_y;
        } else {
            sx = 0;
            sy = 0;
            ex = bitmap.width - 1;
            ey = bitmap.height - 1;
        }

        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
            int t;

            t = (int) u32_startx;
            u32_startx = u32_starty;
            u32_starty = t & 0xFFFFFFFFL;
            t = sx;
            sx = sy;
            sy = t;
            t = ex;
            ex = ey;
            ey = t;
            t = incxx;
            incxx = incyy;
            incyy = t;
            t = incxy;
            incxy = incyx;
            incyx = t;
        }

        if ((Machine.orientation & ORIENTATION_FLIP_X) != 0) {
            int w = ex - sx;

            incxy = -incxy;
            incyx = -incyx;
            u32_startx = (widthshifted - u32_startx - 1) & 0xFFFFFFFFL;
            u32_startx = (u32_startx - incxx * w) & 0xFFFFFFFFL;
            u32_starty = (u32_starty - incxy * w) & 0xFFFFFFFFL;

            w = sx;
            sx = bitmap.width - 1 - ex;
            ex = bitmap.width - 1 - w;
        }

        if ((Machine.orientation & ORIENTATION_FLIP_Y) != 0) {
            int h = ey - sy;

            incxy = -incxy;
            incyx = -incyx;
            u32_starty = (heightshifted - u32_starty - 1) & 0xFFFFFFFFL;
            u32_startx = (u32_startx - incyx * h) & 0xFFFFFFFFL;
            u32_starty = (u32_starty - incyy * h) & 0xFFFFFFFFL;

            h = sy;
            sy = bitmap.height - 1 - ey;
            ey = bitmap.height - 1 - h;
        }
        if (incxy == 0 && incyx == 0 && wraparound == 0) {
            /* optimized loop for the not rotated case */

            if (incxx == 0x10000) {
                /* optimized loop for the not zoomed case */

 /* startx is unsigned */
                u32_startx = (((int) u32_startx) >>> 16) & 0xFFFFFFFFL;

                if (u32_startx >= srcbitmap.width) {
                    sx += -u32_startx;
                    u32_startx = 0;
                }

                if (sx <= ex) {
                    while (sy <= ey) {
                        if (u32_starty < heightshifted) {
                            x = sx;
                            u32_cx = u32_startx & 0xFFFFFFFFL;
                            u32_cy = (u32_starty >>> 16) & 0xFFFFFFFFL;
                            dest = new UBytePtr(bitmap.line[sy], sx);
                            if (priority != 0) {
                                UBytePtr pri = new UBytePtr(priority_bitmap.line[sy], sx);
                                UBytePtr src = new UBytePtr(srcbitmap.line[(int) u32_cy]);

                                while (x <= ex && u32_cx < srcbitmap.width) {
                                    int c = src.read((int) u32_cx);

                                    if (c != transparent_color) {
                                        dest.write(c);
                                        pri.write(pri.read() | priority);
                                    }

                                    u32_cx = (u32_cx + 1) & 0xFFFFFFFFL;
                                    x++;
                                    dest.inc();
                                    pri.inc();
                                }
                            } else {
                                UBytePtr src = new UBytePtr(srcbitmap.line[(int) u32_cy]);

                                while (x <= ex && u32_cx < srcbitmap.width) {
                                    int c = src.read((int) u32_cx);

                                    if (c != transparent_color) {
                                        dest.write(c);
                                    }

                                    u32_cx = (u32_cx + 1) & 0xFFFFFFFFL;
                                    x++;
                                    dest.inc();
                                }
                            }
                        }
                        u32_starty = (u32_starty + incyy) & 0xFFFFFFFFL;
                        sy++;
                    }
                }
            } else {
                while (u32_startx >= widthshifted && sx <= ex) {
                    u32_startx = (u32_startx + incxx) & 0xFFFFFFFFL;
                    sx++;
                }

                if (sx <= ex) {
                    while (sy <= ey) {
                        if (u32_starty < heightshifted) {
                            x = sx;
                            u32_cx = u32_startx & 0xFFFFFFFFL;
                            u32_cy = (u32_starty >>> 16) & 0xFFFFFFFFL;
                            dest = new UBytePtr(bitmap.line[sy], sx);
                            if (priority != 0) {
                                UBytePtr pri = new UBytePtr(priority_bitmap.line[sy], sx);
                                UBytePtr src = new UBytePtr(srcbitmap.line[(int) u32_cy]);
                                while (x <= ex && u32_cx < widthshifted) {
                                    int c = src.read((int) ((u32_cx >>> 16) & 0xFFFFFFFFL));

                                    if (c != transparent_color) {
                                        dest.write(c);
                                        pri.write(pri.read() | priority);
                                    }
                                    u32_cx = (u32_cx + incxx) & 0xFFFFFFFFL;
                                    x++;
                                    dest.inc();
                                    pri.inc();
                                }
                            } else {
                                UBytePtr src = new UBytePtr(srcbitmap.line[(int) u32_cy]);

                                while (x <= ex && u32_cx < widthshifted) {
                                    int c = src.read((int) ((u32_cx >>> 16) & 0xFFFFFFFFL));

                                    if (c != transparent_color) {
                                        dest.write(c);
                                    }

                                    u32_cx = (u32_cx + incxx) & 0xFFFFFFFFL;
                                    x++;
                                    dest.inc();
                                }
                            }
                        }
                        u32_starty = (u32_starty + incyy) & 0xFFFFFFFFL;
                        sy++;
                    }
                }
            }
        } else {
            if (wraparound != 0) {
                /* plot with wraparound */
                while (sy <= ey) {
                    x = sx;
                    u32_cx = u32_startx;
                    u32_cy = u32_starty;
                    dest = new UBytePtr(bitmap.line[sy], sx);
                    if (priority != 0) {
                        UBytePtr pri = new UBytePtr(priority_bitmap.line[sy], sx);

                        while (x <= ex) {
                            UBytePtr c1 = new UBytePtr(srcbitmap.line[(int) (((u32_cy >>> 16) & xmask) & 0xFFFFFFFFL)]);
                            int c = c1.read((int) (((u32_cx >>> 16) & ymask) & 0xFFFFFFFFL));

                            if (c != transparent_color) {
                                dest.write(c);
                                pri.write(pri.read() | priority);
                            }

                            u32_cx = (u32_cx + incxx) & 0xFFFFFFFFL;;
                            u32_cy = (u32_cy + incxy) & 0xFFFFFFFFL;
                            x++;
                            dest.inc();
                            pri.inc();
                        }
                    } else {
                        while (x <= ex) {
                            UBytePtr c1 = new UBytePtr(srcbitmap.line[(int) (((u32_cy >>> 16) & xmask) & 0xFFFFFFFFL)]);
                            int c = c1.read((int) (((u32_cx >>> 16) & ymask) & 0xFFFFFFFFL));

                            if (c != transparent_color) {
                                dest.write(c);
                            }

                            u32_cx = (u32_cx + incxx) & 0xFFFFFFFFL;;
                            u32_cy = (u32_cy + incxy) & 0xFFFFFFFFL;
                            x++;
                            dest.inc();
                        }
                    }
                    u32_startx = (u32_startx + incyx) & 0xFFFFFFFFL;
                    u32_starty = (u32_starty + incyy) & 0xFFFFFFFFL;
                    sy++;
                }
            } else {
                while (sy <= ey) {
                    x = sx;
                    u32_cx = u32_startx;
                    u32_cy = u32_starty;
                    dest = new UBytePtr(bitmap.line[sy], sx);
                    if (priority != 0) {
                        UBytePtr pri = new UBytePtr(priority_bitmap.line[sy], sx);

                        while (x <= ex) {
                            if (u32_cx < widthshifted && u32_cy < heightshifted) {
                                UBytePtr c1 = new UBytePtr(srcbitmap.line[(int) ((u32_cy >>> 16) & 0xFFFFFFFFL)]);
                                int c = c1.read((int) ((u32_cx >>> 16) & 0xFFFFFFFFL));

                                if (c != transparent_color) {
                                    dest.write(c);
                                    pri.write(pri.read() | priority);
                                }
                            }

                            u32_cx = (u32_cx + incxx) & 0xFFFFFFFFL;;
                            u32_cy = (u32_cy + incxy) & 0xFFFFFFFFL;
                            x++;
                            dest.inc();
                            pri.inc();
                        }
                    } else {
                        while (x <= ex) {
                            if (u32_cx < widthshifted && u32_cy < heightshifted) {
                                UBytePtr c1 = new UBytePtr(srcbitmap.line[(int) ((u32_cy >>> 16) & 0xFFFFFFFFL)]);
                                int c = c1.read((int) ((u32_cx >>> 16) & 0xFFFFFFFFL));

                                if (c != transparent_color) {
                                    dest.write(c);
                                }
                            }

                            u32_cx = (u32_cx + incxx) & 0xFFFFFFFFL;;
                            u32_cy = (u32_cy + incxy) & 0xFFFFFFFFL;
                            x++;
                            dest.inc();
                        }
                    }
                    u32_startx = (u32_startx + incyx) & 0xFFFFFFFFL;
                    u32_starty = (u32_starty + incyy) & 0xFFFFFFFFL;
                    sy++;
                }
            }
        }
    }



    /* notes:
       - startx and starty MUST be UINT32 for calculations to work correctly
       - srcbitmap->width and height are assumed to be a power of 2 to speed up wraparound
       */
    public static void copyrozbitmap(osd_bitmap dest, osd_bitmap src,
                    int startx,int starty,int incxx,int incxy,int incyx,int incyy,int wraparound,
                    rectangle clip,int transparency,int transparent_color,int priority)
    {
    /*TODO*///	profiler_mark(PROFILER_COPYBITMAP);

            /* cheat, the core doesn't support TRANSPARENCY_NONE yet */
            if (transparency == TRANSPARENCY_NONE)
            {
                    transparency = TRANSPARENCY_PEN;
                    transparent_color = -1;
            }

            /* if necessary, remap the transparent color */
            if (transparency == TRANSPARENCY_COLOR)
            {
                    transparency = TRANSPARENCY_PEN;
                    transparent_color = Machine.pens[transparent_color];
            }

            if (transparency != TRANSPARENCY_PEN)
            {
                    usrintf_showmessage("copyrozbitmap unsupported trans %02x",transparency);
                    return;
            }

            if (dest.depth == 8)
                    copyrozbitmap_core8(dest,src,startx,starty,incxx,incxy,incyx,incyy,wraparound,clip,transparency,transparent_color,priority);
            else if(dest.depth == 15 || dest.depth == 16) {
                    throw new UnsupportedOperationException("Unsupported");
/*TODO*///                    copyrozbitmap_core16(dest,src,startx,starty,incxx,incxy,incyx,incyy,wraparound,clip,transparency,transparent_color,priority);
            } else {
                throw new UnsupportedOperationException("Unsupported");
/*TODO*///                    copyrozbitmap_core32(dest,src,startx,starty,incxx,incxy,incyx,incyy,wraparound,clip,transparency,transparent_color,priority);
            }

    /*TODO*///	profiler_mark(PROFILER_END);
    }



/*TODO*////* fill a bitmap using the specified pen */
/*TODO*///void fillbitmap(struct osd_bitmap *dest,int pen,const struct rectangle *clip)
/*TODO*///{
/*TODO*///	int sx,sy,ex,ey,y;
/*TODO*///	struct rectangle myclip;
/*TODO*///
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			myclip.min_x = clip->min_y;
/*TODO*///			myclip.max_x = clip->max_y;
/*TODO*///			myclip.min_y = clip->min_x;
/*TODO*///			myclip.max_y = clip->max_x;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_X)
/*TODO*///	{
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
/*TODO*///			temp = clip->min_x;
/*TODO*///			myclip.min_x = dest->width-1 - clip->max_x;
/*TODO*///			myclip.max_x = dest->width-1 - temp;
/*TODO*///			myclip.min_y = clip->min_y;
/*TODO*///			myclip.max_y = clip->max_y;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_Y)
/*TODO*///	{
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
/*TODO*///			myclip.min_x = clip->min_x;
/*TODO*///			myclip.max_x = clip->max_x;
/*TODO*///			temp = clip->min_y;
/*TODO*///			myclip.min_y = dest->height-1 - clip->max_y;
/*TODO*///			myclip.max_y = dest->height-1 - temp;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	sx = 0;
/*TODO*///	ex = dest->width - 1;
/*TODO*///	sy = 0;
/*TODO*///	ey = dest->height - 1;
/*TODO*///
/*TODO*///	if (clip && sx < clip->min_x) sx = clip->min_x;
/*TODO*///	if (clip && ex > clip->max_x) ex = clip->max_x;
/*TODO*///	if (sx > ex) return;
/*TODO*///	if (clip && sy < clip->min_y) sy = clip->min_y;
/*TODO*///	if (clip && ey > clip->max_y) ey = clip->max_y;
/*TODO*///	if (sy > ey) return;
/*TODO*///
/*TODO*///	if (Machine->drv->video_attributes & VIDEO_SUPPORTS_DIRTY)
/*TODO*///		osd_mark_dirty(sx,sy,ex,ey);
/*TODO*///
/*TODO*///	if (dest->depth == 32)
/*TODO*///	{
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
/*TODO*///	}
/*TODO*///	else if (dest->depth == 15 || dest->depth == 16)
/*TODO*///	{
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
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		for (y = sy;y <= ey;y++)
/*TODO*///			memset(&dest->line[y][sx],pen,ex-sx+1);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void common_drawgfxzoom( struct osd_bitmap *dest_bmp,const struct GfxElement *gfx,
/*TODO*///		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color,
/*TODO*///		int scalex, int scaley,struct osd_bitmap *pri_buffer,UINT32 pri_mask)
/*TODO*///{
/*TODO*///	struct rectangle myclip;
/*TODO*///	int alphapen = 0;
/*TODO*///
/*TODO*///	if (!scalex || !scaley) return;
/*TODO*///
/*TODO*///	if (scalex == 0x10000 && scaley == 0x10000)
/*TODO*///	{
/*TODO*///		common_drawgfx(dest_bmp,gfx,code,color,flipx,flipy,sx,sy,clip,transparency,transparent_color,pri_buffer,pri_mask);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	if (transparency != TRANSPARENCY_PEN && transparency != TRANSPARENCY_PEN_RAW
/*TODO*///			&& transparency != TRANSPARENCY_PENS && transparency != TRANSPARENCY_COLOR
/*TODO*///			&& transparency != TRANSPARENCY_PEN_TABLE && transparency != TRANSPARENCY_PEN_TABLE_RAW
/*TODO*///			&& transparency != TRANSPARENCY_BLEND_RAW && transparency != TRANSPARENCY_ALPHAONE
/*TODO*///			&& transparency != TRANSPARENCY_ALPHA)
/*TODO*///	{
/*TODO*///		usrintf_showmessage("drawgfxzoom unsupported trans %02x",transparency);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (!alpha_active && (transparency == TRANSPARENCY_ALPHAONE || transparency == TRANSPARENCY_ALPHA))
/*TODO*///	{
/*TODO*///		transparency = TRANSPARENCY_PEN;
/*TODO*///		transparent_color &= 0xff;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (transparency == TRANSPARENCY_ALPHAONE)
/*TODO*///	{
/*TODO*///		alphapen = transparent_color >> 8;
/*TODO*///		transparent_color &= 0xff;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (transparency == TRANSPARENCY_COLOR)
/*TODO*///		transparent_color = Machine->pens[transparent_color];
/*TODO*///
/*TODO*///
/*TODO*///	/*
/*TODO*///	scalex and scaley are 16.16 fixed point numbers
/*TODO*///	1<<15 : shrink to 50%
/*TODO*///	1<<16 : uniform scale
/*TODO*///	1<<17 : double to 200%
/*TODO*///	*/
/*TODO*///
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		int temp;
/*TODO*///
/*TODO*///		temp = sx;
/*TODO*///		sx = sy;
/*TODO*///		sy = temp;
/*TODO*///
/*TODO*///		temp = flipx;
/*TODO*///		flipx = flipy;
/*TODO*///		flipy = temp;
/*TODO*///
/*TODO*///		temp = scalex;
/*TODO*///		scalex = scaley;
/*TODO*///		scaley = temp;
/*TODO*///
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_x;
/*TODO*///			myclip.min_x = clip->min_y;
/*TODO*///			myclip.min_y = temp;
/*TODO*///			temp = clip->max_x;
/*TODO*///			myclip.max_x = clip->max_y;
/*TODO*///			myclip.max_y = temp;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_X)
/*TODO*///	{
/*TODO*///		sx = dest_bmp->width - ((gfx->width * scalex + 0x7fff) >> 16) - sx;
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_x;
/*TODO*///			myclip.min_x = dest_bmp->width-1 - clip->max_x;
/*TODO*///			myclip.max_x = dest_bmp->width-1 - temp;
/*TODO*///			myclip.min_y = clip->min_y;
/*TODO*///			myclip.max_y = clip->max_y;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///#ifndef PREROTATE_GFX
/*TODO*///		flipx = !flipx;
/*TODO*///#endif
/*TODO*///	}
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_Y)
/*TODO*///	{
/*TODO*///		sy = dest_bmp->height - ((gfx->height * scaley + 0x7fff) >> 16) - sy;
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
/*TODO*///			myclip.min_x = clip->min_x;
/*TODO*///			myclip.max_x = clip->max_x;
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_y;
/*TODO*///			myclip.min_y = dest_bmp->height-1 - clip->max_y;
/*TODO*///			myclip.max_y = dest_bmp->height-1 - temp;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///#ifndef PREROTATE_GFX
/*TODO*///		flipy = !flipy;
/*TODO*///#endif
/*TODO*///	}
/*TODO*///
/*TODO*///	/* KW 991012 -- Added code to force clip to bitmap boundary */
/*TODO*///	if(clip)
/*TODO*///	{
/*TODO*///		myclip.min_x = clip->min_x;
/*TODO*///		myclip.max_x = clip->max_x;
/*TODO*///		myclip.min_y = clip->min_y;
/*TODO*///		myclip.max_y = clip->max_y;
/*TODO*///
/*TODO*///		if (myclip.min_x < 0) myclip.min_x = 0;
/*TODO*///		if (myclip.max_x >= dest_bmp->width) myclip.max_x = dest_bmp->width-1;
/*TODO*///		if (myclip.min_y < 0) myclip.min_y = 0;
/*TODO*///		if (myclip.max_y >= dest_bmp->height) myclip.max_y = dest_bmp->height-1;
/*TODO*///
/*TODO*///		clip=&myclip;
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	/* ASG 980209 -- added 16-bit version */
/*TODO*///	if (dest_bmp->depth == 8)
/*TODO*///	{
/*TODO*///		if( gfx && gfx->colortable )
/*TODO*///		{
/*TODO*///			const UINT32 *pal = &gfx->colortable[gfx->color_granularity * (color % gfx->total_colors)]; /* ASG 980209 */
/*TODO*///			int source_base = (code % gfx->total_elements) * gfx->height;
/*TODO*///
/*TODO*///			int sprite_screen_height = (scaley*gfx->height+0x8000)>>16;
/*TODO*///			int sprite_screen_width = (scalex*gfx->width+0x8000)>>16;
/*TODO*///
/*TODO*///			if (sprite_screen_width && sprite_screen_height)
/*TODO*///			{
/*TODO*///				/* compute sprite increment per screen pixel */
/*TODO*///				int dx = (gfx->width<<16)/sprite_screen_width;
/*TODO*///				int dy = (gfx->height<<16)/sprite_screen_height;
/*TODO*///
/*TODO*///				int ex = sx+sprite_screen_width;
/*TODO*///				int ey = sy+sprite_screen_height;
/*TODO*///
/*TODO*///				int x_index_base;
/*TODO*///				int y_index;
/*TODO*///
/*TODO*///				if( flipx )
/*TODO*///				{
/*TODO*///					x_index_base = (sprite_screen_width-1)*dx;
/*TODO*///					dx = -dx;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					x_index_base = 0;
/*TODO*///				}
/*TODO*///
/*TODO*///				if( flipy )
/*TODO*///				{
/*TODO*///					y_index = (sprite_screen_height-1)*dy;
/*TODO*///					dy = -dy;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					y_index = 0;
/*TODO*///				}
/*TODO*///
/*TODO*///				if( clip )
/*TODO*///				{
/*TODO*///					if( sx < clip->min_x)
/*TODO*///					{ /* clip left */
/*TODO*///						int pixels = clip->min_x-sx;
/*TODO*///						sx += pixels;
/*TODO*///						x_index_base += pixels*dx;
/*TODO*///					}
/*TODO*///					if( sy < clip->min_y )
/*TODO*///					{ /* clip top */
/*TODO*///						int pixels = clip->min_y-sy;
/*TODO*///						sy += pixels;
/*TODO*///						y_index += pixels*dy;
/*TODO*///					}
/*TODO*///					/* NS 980211 - fixed incorrect clipping */
/*TODO*///					if( ex > clip->max_x+1 )
/*TODO*///					{ /* clip right */
/*TODO*///						int pixels = ex-clip->max_x-1;
/*TODO*///						ex -= pixels;
/*TODO*///					}
/*TODO*///					if( ey > clip->max_y+1 )
/*TODO*///					{ /* clip bottom */
/*TODO*///						int pixels = ey-clip->max_y-1;
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
/*TODO*///						if (pri_buffer)
/*TODO*///						{
/*TODO*///							if (gfx->flags & GFX_PACKED)
/*TODO*///							{
/*TODO*///								for( y=sy; y<ey; y++ )
/*TODO*///								{
/*TODO*///									UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///									UINT8 *dest = dest_bmp->line[y];
/*TODO*///									UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///									int x, x_index = x_index_base;
/*TODO*///									for( x=sx; x<ex; x++ )
/*TODO*///									{
/*TODO*///										int c = (source[x_index>>17] >> ((x_index & 0x10000) >> 14)) & 0x0f;
/*TODO*///										if( c != transparent_color )
/*TODO*///										{
/*TODO*///											if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///												dest[x] = pal[c];
/*TODO*///											pri[x] = 31;
/*TODO*///										}
/*TODO*///										x_index += dx;
/*TODO*///									}
/*TODO*///
/*TODO*///									y_index += dy;
/*TODO*///								}
/*TODO*///							}
/*TODO*///							else
/*TODO*///							{
/*TODO*///								for( y=sy; y<ey; y++ )
/*TODO*///								{
/*TODO*///									UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///									UINT8 *dest = dest_bmp->line[y];
/*TODO*///									UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///									int x, x_index = x_index_base;
/*TODO*///									for( x=sx; x<ex; x++ )
/*TODO*///									{
/*TODO*///										int c = source[x_index>>16];
/*TODO*///										if( c != transparent_color )
/*TODO*///										{
/*TODO*///											if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///												dest[x] = pal[c];
/*TODO*///											pri[x] = 31;
/*TODO*///										}
/*TODO*///										x_index += dx;
/*TODO*///									}
/*TODO*///
/*TODO*///									y_index += dy;
/*TODO*///								}
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							if (gfx->flags & GFX_PACKED)
/*TODO*///							{
/*TODO*///								for( y=sy; y<ey; y++ )
/*TODO*///								{
/*TODO*///									UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///									UINT8 *dest = dest_bmp->line[y];
/*TODO*///
/*TODO*///									int x, x_index = x_index_base;
/*TODO*///									for( x=sx; x<ex; x++ )
/*TODO*///									{
/*TODO*///										int c = (source[x_index>>17] >> ((x_index & 0x10000) >> 14)) & 0x0f;
/*TODO*///										if( c != transparent_color ) dest[x] = pal[c];
/*TODO*///										x_index += dx;
/*TODO*///									}
/*TODO*///
/*TODO*///									y_index += dy;
/*TODO*///								}
/*TODO*///							}
/*TODO*///							else
/*TODO*///							{
/*TODO*///								for( y=sy; y<ey; y++ )
/*TODO*///								{
/*TODO*///									UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///									UINT8 *dest = dest_bmp->line[y];
/*TODO*///
/*TODO*///									int x, x_index = x_index_base;
/*TODO*///									for( x=sx; x<ex; x++ )
/*TODO*///									{
/*TODO*///										int c = source[x_index>>16];
/*TODO*///										if( c != transparent_color ) dest[x] = pal[c];
/*TODO*///										x_index += dx;
/*TODO*///									}
/*TODO*///
/*TODO*///									y_index += dy;
/*TODO*///								}
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT8 *dest = dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest[x] = color + c;
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT8 *dest = dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color ) dest[x] = color + c;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT8 *dest = dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest[x] |= (color + c);
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT8 *dest = dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color ) dest[x] |= (color + c);
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT8 *dest = dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if (((1 << c) & transparent_color) == 0)
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest[x] = pal[c];
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT8 *dest = dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if (((1 << c) & transparent_color) == 0)
/*TODO*///										dest[x] = pal[c];
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT8 *dest = dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = pal[source[x_index>>16]];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest[x] = c;
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT8 *dest = dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = pal[source[x_index>>16]];
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT8 *dest = dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///										{
/*TODO*///											switch(gfx_drawmode_table[c])
/*TODO*///											{
/*TODO*///											case DRAWMODE_SOURCE:
/*TODO*///												dest[x] = pal[c];
/*TODO*///												break;
/*TODO*///											case DRAWMODE_SHADOW:
/*TODO*///												dest[x] = palette_shadow_table[dest[x]];
/*TODO*///												break;
/*TODO*///											}
/*TODO*///										}
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT8 *dest = dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										switch(gfx_drawmode_table[c])
/*TODO*///										{
/*TODO*///										case DRAWMODE_SOURCE:
/*TODO*///											dest[x] = pal[c];
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT8 *dest = dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///										{
/*TODO*///											switch(gfx_drawmode_table[c])
/*TODO*///											{
/*TODO*///											case DRAWMODE_SOURCE:
/*TODO*///												dest[x] = color + c;
/*TODO*///												break;
/*TODO*///											case DRAWMODE_SHADOW:
/*TODO*///												dest[x] = palette_shadow_table[dest[x]];
/*TODO*///												break;
/*TODO*///											}
/*TODO*///										}
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT8 *dest = dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										switch(gfx_drawmode_table[c])
/*TODO*///										{
/*TODO*///										case DRAWMODE_SOURCE:
/*TODO*///											dest[x] = color + c;
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
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* ASG 980209 -- new 16-bit part */
/*TODO*///	else if (dest_bmp->depth == 15 || dest_bmp->depth == 16)
/*TODO*///	{
/*TODO*///		if( gfx && gfx->colortable )
/*TODO*///		{
/*TODO*///			const UINT32 *pal = &gfx->colortable[gfx->color_granularity * (color % gfx->total_colors)]; /* ASG 980209 */
/*TODO*///			int source_base = (code % gfx->total_elements) * gfx->height;
/*TODO*///
/*TODO*///			int sprite_screen_height = (scaley*gfx->height+0x8000)>>16;
/*TODO*///			int sprite_screen_width = (scalex*gfx->width+0x8000)>>16;
/*TODO*///
/*TODO*///			if (sprite_screen_width && sprite_screen_height)
/*TODO*///			{
/*TODO*///				/* compute sprite increment per screen pixel */
/*TODO*///				int dx = (gfx->width<<16)/sprite_screen_width;
/*TODO*///				int dy = (gfx->height<<16)/sprite_screen_height;
/*TODO*///
/*TODO*///				int ex = sx+sprite_screen_width;
/*TODO*///				int ey = sy+sprite_screen_height;
/*TODO*///
/*TODO*///				int x_index_base;
/*TODO*///				int y_index;
/*TODO*///
/*TODO*///				if( flipx )
/*TODO*///				{
/*TODO*///					x_index_base = (sprite_screen_width-1)*dx;
/*TODO*///					dx = -dx;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					x_index_base = 0;
/*TODO*///				}
/*TODO*///
/*TODO*///				if( flipy )
/*TODO*///				{
/*TODO*///					y_index = (sprite_screen_height-1)*dy;
/*TODO*///					dy = -dy;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					y_index = 0;
/*TODO*///				}
/*TODO*///
/*TODO*///				if( clip )
/*TODO*///				{
/*TODO*///					if( sx < clip->min_x)
/*TODO*///					{ /* clip left */
/*TODO*///						int pixels = clip->min_x-sx;
/*TODO*///						sx += pixels;
/*TODO*///						x_index_base += pixels*dx;
/*TODO*///					}
/*TODO*///					if( sy < clip->min_y )
/*TODO*///					{ /* clip top */
/*TODO*///						int pixels = clip->min_y-sy;
/*TODO*///						sy += pixels;
/*TODO*///						y_index += pixels*dy;
/*TODO*///					}
/*TODO*///					/* NS 980211 - fixed incorrect clipping */
/*TODO*///					if( ex > clip->max_x+1 )
/*TODO*///					{ /* clip right */
/*TODO*///						int pixels = ex-clip->max_x-1;
/*TODO*///						ex -= pixels;
/*TODO*///					}
/*TODO*///					if( ey > clip->max_y+1 )
/*TODO*///					{ /* clip bottom */
/*TODO*///						int pixels = ey-clip->max_y-1;
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
/*TODO*///						if (pri_buffer)
/*TODO*///						{
/*TODO*///							if (gfx->flags & GFX_PACKED)
/*TODO*///							{
/*TODO*///								for( y=sy; y<ey; y++ )
/*TODO*///								{
/*TODO*///									UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///									UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///									UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///									int x, x_index = x_index_base;
/*TODO*///									for( x=sx; x<ex; x++ )
/*TODO*///									{
/*TODO*///										int c = (source[x_index>>17] >> ((x_index & 0x10000) >> 14)) & 0x0f;
/*TODO*///										if( c != transparent_color )
/*TODO*///										{
/*TODO*///											if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///												dest[x] = pal[c];
/*TODO*///											pri[x] = 31;
/*TODO*///										}
/*TODO*///										x_index += dx;
/*TODO*///									}
/*TODO*///
/*TODO*///									y_index += dy;
/*TODO*///								}
/*TODO*///							}
/*TODO*///							else
/*TODO*///							{
/*TODO*///								for( y=sy; y<ey; y++ )
/*TODO*///								{
/*TODO*///									UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///									UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///									UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///									int x, x_index = x_index_base;
/*TODO*///									for( x=sx; x<ex; x++ )
/*TODO*///									{
/*TODO*///										int c = source[x_index>>16];
/*TODO*///										if( c != transparent_color )
/*TODO*///										{
/*TODO*///											if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///												dest[x] = pal[c];
/*TODO*///											pri[x] = 31;
/*TODO*///										}
/*TODO*///										x_index += dx;
/*TODO*///									}
/*TODO*///
/*TODO*///									y_index += dy;
/*TODO*///								}
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							if (gfx->flags & GFX_PACKED)
/*TODO*///							{
/*TODO*///								for( y=sy; y<ey; y++ )
/*TODO*///								{
/*TODO*///									UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///									UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///									int x, x_index = x_index_base;
/*TODO*///									for( x=sx; x<ex; x++ )
/*TODO*///									{
/*TODO*///										int c = (source[x_index>>17] >> ((x_index & 0x10000) >> 14)) & 0x0f;
/*TODO*///										if( c != transparent_color ) dest[x] = pal[c];
/*TODO*///										x_index += dx;
/*TODO*///									}
/*TODO*///
/*TODO*///									y_index += dy;
/*TODO*///								}
/*TODO*///							}
/*TODO*///							else
/*TODO*///							{
/*TODO*///								for( y=sy; y<ey; y++ )
/*TODO*///								{
/*TODO*///									UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///									UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///									int x, x_index = x_index_base;
/*TODO*///									for( x=sx; x<ex; x++ )
/*TODO*///									{
/*TODO*///										int c = source[x_index>>16];
/*TODO*///										if( c != transparent_color ) dest[x] = pal[c];
/*TODO*///										x_index += dx;
/*TODO*///									}
/*TODO*///
/*TODO*///									y_index += dy;
/*TODO*///								}
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest[x] = color + c;
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color ) dest[x] = color + c;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest[x] |= color + c;
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color ) dest[x] |= color + c;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if (((1 << c) & transparent_color) == 0)
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest[x] = pal[c];
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if (((1 << c) & transparent_color) == 0)
/*TODO*///										dest[x] = pal[c];
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = pal[source[x_index>>16]];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest[x] = c;
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = pal[source[x_index>>16]];
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///										{
/*TODO*///											switch(gfx_drawmode_table[c])
/*TODO*///											{
/*TODO*///											case DRAWMODE_SOURCE:
/*TODO*///												dest[x] = pal[c];
/*TODO*///												break;
/*TODO*///											case DRAWMODE_SHADOW:
/*TODO*///												dest[x] = palette_shadow_table[dest[x]];
/*TODO*///												break;
/*TODO*///											}
/*TODO*///										}
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										switch(gfx_drawmode_table[c])
/*TODO*///										{
/*TODO*///										case DRAWMODE_SOURCE:
/*TODO*///											dest[x] = pal[c];
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///										{
/*TODO*///											switch(gfx_drawmode_table[c])
/*TODO*///											{
/*TODO*///											case DRAWMODE_SOURCE:
/*TODO*///												dest[x] = color + c;
/*TODO*///												break;
/*TODO*///											case DRAWMODE_SHADOW:
/*TODO*///												dest[x] = palette_shadow_table[dest[x]];
/*TODO*///												break;
/*TODO*///											}
/*TODO*///										}
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										switch(gfx_drawmode_table[c])
/*TODO*///										{
/*TODO*///										case DRAWMODE_SOURCE:
/*TODO*///											dest[x] = color + c;
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
/*TODO*///					/* case 5: TRANSPARENCY_ALPHAONE */
/*TODO*///					if (transparency == TRANSPARENCY_ALPHAONE)
/*TODO*///					{
/*TODO*///						if (pri_buffer)
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///										{
/*TODO*///											if( c == alphapen)
/*TODO*///												dest[x] = alpha_blend16(dest[x], pal[c]);
/*TODO*///											else
/*TODO*///												dest[x] = pal[c];
/*TODO*///										}
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if( c == alphapen)
/*TODO*///											dest[x] = alpha_blend16(dest[x], pal[c]);
/*TODO*///										else
/*TODO*///											dest[x] = pal[c];
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest[x] = alpha_blend16(dest[x], pal[c]);
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT16 *dest = (UINT16 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color ) dest[x] = alpha_blend16(dest[x], pal[c]);
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
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		if( gfx && gfx->colortable )
/*TODO*///		{
/*TODO*///			const UINT32 *pal = &gfx->colortable[gfx->color_granularity * (color % gfx->total_colors)]; /* ASG 980209 */
/*TODO*///			int source_base = (code % gfx->total_elements) * gfx->height;
/*TODO*///
/*TODO*///			int sprite_screen_height = (scaley*gfx->height+0x8000)>>16;
/*TODO*///			int sprite_screen_width = (scalex*gfx->width+0x8000)>>16;
/*TODO*///
/*TODO*///			if (sprite_screen_width && sprite_screen_height)
/*TODO*///			{
/*TODO*///				/* compute sprite increment per screen pixel */
/*TODO*///				int dx = (gfx->width<<16)/sprite_screen_width;
/*TODO*///				int dy = (gfx->height<<16)/sprite_screen_height;
/*TODO*///
/*TODO*///				int ex = sx+sprite_screen_width;
/*TODO*///				int ey = sy+sprite_screen_height;
/*TODO*///
/*TODO*///				int x_index_base;
/*TODO*///				int y_index;
/*TODO*///
/*TODO*///				if( flipx )
/*TODO*///				{
/*TODO*///					x_index_base = (sprite_screen_width-1)*dx;
/*TODO*///					dx = -dx;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					x_index_base = 0;
/*TODO*///				}
/*TODO*///
/*TODO*///				if( flipy )
/*TODO*///				{
/*TODO*///					y_index = (sprite_screen_height-1)*dy;
/*TODO*///					dy = -dy;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					y_index = 0;
/*TODO*///				}
/*TODO*///
/*TODO*///				if( clip )
/*TODO*///				{
/*TODO*///					if( sx < clip->min_x)
/*TODO*///					{ /* clip left */
/*TODO*///						int pixels = clip->min_x-sx;
/*TODO*///						sx += pixels;
/*TODO*///						x_index_base += pixels*dx;
/*TODO*///					}
/*TODO*///					if( sy < clip->min_y )
/*TODO*///					{ /* clip top */
/*TODO*///						int pixels = clip->min_y-sy;
/*TODO*///						sy += pixels;
/*TODO*///						y_index += pixels*dy;
/*TODO*///					}
/*TODO*///					/* NS 980211 - fixed incorrect clipping */
/*TODO*///					if( ex > clip->max_x+1 )
/*TODO*///					{ /* clip right */
/*TODO*///						int pixels = ex-clip->max_x-1;
/*TODO*///						ex -= pixels;
/*TODO*///					}
/*TODO*///					if( ey > clip->max_y+1 )
/*TODO*///					{ /* clip bottom */
/*TODO*///						int pixels = ey-clip->max_y-1;
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
/*TODO*///						if (pri_buffer)
/*TODO*///						{
/*TODO*///							for( y=sy; y<ey; y++ )
/*TODO*///							{
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest[x] = pal[c];
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color ) dest[x] = pal[c];
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest[x] = color + c;
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color ) dest[x] = color + c;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest[x] |= color + c;
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color ) dest[x] |= color + c;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if (((1 << c) & transparent_color) == 0)
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest[x] = pal[c];
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if (((1 << c) & transparent_color) == 0)
/*TODO*///										dest[x] = pal[c];
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = pal[source[x_index>>16]];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest[x] = c;
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = pal[source[x_index>>16]];
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///										{
/*TODO*///											switch(gfx_drawmode_table[c])
/*TODO*///											{
/*TODO*///											case DRAWMODE_SOURCE:
/*TODO*///												dest[x] = pal[c];
/*TODO*///												break;
/*TODO*///											case DRAWMODE_SHADOW:
/*TODO*///												dest[x] = palette_shadow_table[dest[x]];
/*TODO*///												break;
/*TODO*///											}
/*TODO*///										}
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										switch(gfx_drawmode_table[c])
/*TODO*///										{
/*TODO*///										case DRAWMODE_SOURCE:
/*TODO*///											dest[x] = pal[c];
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///										{
/*TODO*///											switch(gfx_drawmode_table[c])
/*TODO*///											{
/*TODO*///											case DRAWMODE_SOURCE:
/*TODO*///												dest[x] = color + c;
/*TODO*///												break;
/*TODO*///											case DRAWMODE_SHADOW:
/*TODO*///												dest[x] = palette_shadow_table[dest[x]];
/*TODO*///												break;
/*TODO*///											}
/*TODO*///										}
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										switch(gfx_drawmode_table[c])
/*TODO*///										{
/*TODO*///										case DRAWMODE_SOURCE:
/*TODO*///											dest[x] = color + c;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///										{
/*TODO*///											if( c == alphapen)
/*TODO*///												dest[x] = alpha_blend32(dest[x], pal[c]);
/*TODO*///											else
/*TODO*///												dest[x] = pal[c];
/*TODO*///										}
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if( c == alphapen)
/*TODO*///											dest[x] = alpha_blend32(dest[x], pal[c]);
/*TODO*///										else
/*TODO*///											dest[x] = pal[c];
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///								UINT8 *pri = pri_buffer->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
/*TODO*///									if( c != transparent_color )
/*TODO*///									{
/*TODO*///										if (((1 << pri[x]) & pri_mask) == 0)
/*TODO*///											dest[x] = alpha_blend32(dest[x], pal[c]);
/*TODO*///										pri[x] = 31;
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
/*TODO*///								UINT8 *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///								UINT32 *dest = (UINT32 *)dest_bmp->line[y];
/*TODO*///
/*TODO*///								int x, x_index = x_index_base;
/*TODO*///								for( x=sx; x<ex; x++ )
/*TODO*///								{
/*TODO*///									int c = source[x_index>>16];
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
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void drawgfxzoom( struct osd_bitmap *dest_bmp,const struct GfxElement *gfx,
/*TODO*///		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color,int scalex, int scaley)
/*TODO*///{
/*TODO*///	profiler_mark(PROFILER_DRAWGFX);
/*TODO*///	common_drawgfxzoom(dest_bmp,gfx,code,color,flipx,flipy,sx,sy,
/*TODO*///			clip,transparency,transparent_color,scalex,scaley,NULL,0);
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///}
/*TODO*///
/*TODO*///void pdrawgfxzoom( struct osd_bitmap *dest_bmp,const struct GfxElement *gfx,
/*TODO*///		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color,int scalex, int scaley,
/*TODO*///		UINT32 priority_mask)
/*TODO*///{
/*TODO*///	profiler_mark(PROFILER_DRAWGFX);
/*TODO*///	common_drawgfxzoom(dest_bmp,gfx,code,color,flipx,flipy,sx,sy,
/*TODO*///			clip,transparency,transparent_color,scalex,scaley,priority_bitmap,priority_mask | (1<<31));
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///}
/*TODO*///
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
    public static void plot_pixel2(osd_bitmap bitmap1, osd_bitmap bitmap2, int x, int y, int pen) {
        plot_pixel.handler(bitmap1, x, y, pen);
        plot_pixel.handler(bitmap2, x, y, pen);
    }
    /*TODO*///
/*TODO*///static void pp_8_nd(struct osd_bitmap *b,int x,int y,UINT32 p)  { b->line[y][x] = p; }
/*TODO*///static void pp_8_nd_fx(struct osd_bitmap *b,int x,int y,UINT32 p)  { b->line[y][b->width-1-x] = p; }
/*TODO*///static void pp_8_nd_fy(struct osd_bitmap *b,int x,int y,UINT32 p)  { b->line[b->height-1-y][x] = p; }
/*TODO*///static void pp_8_nd_fxy(struct osd_bitmap *b,int x,int y,UINT32 p)  { b->line[b->height-1-y][b->width-1-x] = p; }
/*TODO*///static void pp_8_nd_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { b->line[x][y] = p; }
/*TODO*///static void pp_8_nd_fx_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { b->line[x][b->width-1-y] = p; }
/*TODO*///static void pp_8_nd_fy_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { b->line[b->height-1-x][y] = p; }
/*TODO*///static void pp_8_nd_fxy_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { b->line[b->height-1-x][b->width-1-y] = p; }
/*TODO*///
/*TODO*///static void pp_8_d(struct osd_bitmap *b,int x,int y,UINT32 p)  { b->line[y][x] = p; osd_mark_dirty(x,y,x,y); }
/*TODO*///static void pp_8_d_fx(struct osd_bitmap *b,int x,int y,UINT32 p)  { x = b->width-1-x;  b->line[y][x] = p; osd_mark_dirty(x,y,x,y); }
/*TODO*///static void pp_8_d_fy(struct osd_bitmap *b,int x,int y,UINT32 p)  { y = b->height-1-y; b->line[y][x] = p; osd_mark_dirty(x,y,x,y); }
/*TODO*///static void pp_8_d_fxy(struct osd_bitmap *b,int x,int y,UINT32 p)  { x = b->width-1-x; y = b->height-1-y; b->line[y][x] = p; osd_mark_dirty(x,y,x,y); }
/*TODO*///static void pp_8_d_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { b->line[x][y] = p; osd_mark_dirty(y,x,y,x); }
/*TODO*///static void pp_8_d_fx_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { y = b->width-1-y; b->line[x][y] = p; osd_mark_dirty(y,x,y,x); }
/*TODO*///static void pp_8_d_fy_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { x = b->height-1-x; b->line[x][y] = p; osd_mark_dirty(y,x,y,x); }
/*TODO*///static void pp_8_d_fxy_s(struct osd_bitmap *b,int x,int y,UINT32 p)  { x = b->height-1-x; y = b->width-1-y; b->line[x][y] = p; osd_mark_dirty(y,x,y,x); }
/*TODO*///
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
/*TODO*///static int rp_8(struct osd_bitmap *b,int x,int y)  { return b->line[y][x]; }
/*TODO*///static int rp_8_fx(struct osd_bitmap *b,int x,int y)  { return b->line[y][b->width-1-x]; }
/*TODO*///static int rp_8_fy(struct osd_bitmap *b,int x,int y)  { return b->line[b->height-1-y][x]; }
/*TODO*///static int rp_8_fxy(struct osd_bitmap *b,int x,int y)  { return b->line[b->height-1-y][b->width-1-x]; }
/*TODO*///static int rp_8_s(struct osd_bitmap *b,int x,int y)  { return b->line[x][y]; }
/*TODO*///static int rp_8_fx_s(struct osd_bitmap *b,int x,int y)  { return b->line[x][b->width-1-y]; }
/*TODO*///static int rp_8_fy_s(struct osd_bitmap *b,int x,int y)  { return b->line[b->height-1-x][y]; }
/*TODO*///static int rp_8_fxy_s(struct osd_bitmap *b,int x,int y)  { return b->line[b->height-1-x][b->width-1-y]; }
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
/*TODO*///static void pb_8_nd(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; while(h-->0){ int c=w; x=t; while(c-->0){ b->line[y][x] = p; x++; } y++; } }
/*TODO*///static void pb_8_nd_fx(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->width-1-x; while(h-->0){ int c=w; x=t; while(c-->0){ b->line[y][x] = p; x--; } y++; } }
/*TODO*///static void pb_8_nd_fy(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; y = b->height-1-y; while(h-->0){ int c=w; x=t; while(c-->0){ b->line[y][x] = p; x++; } y--; } }
/*TODO*///static void pb_8_nd_fxy(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->width-1-x; y = b->height-1-y; while(h-->0){ int c=w; x=t; while(c-->0){ b->line[y][x] = p; x--; } y--; } }
/*TODO*///static void pb_8_nd_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; while(h-->0){ int c=w; x=t; while(c-->0){ b->line[x][y] = p; x++; } y++; } }
/*TODO*///static void pb_8_nd_fx_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; y = b->width-1-y; while(h-->0){ int c=w; x=t; while(c-->0){ b->line[x][y] = p; x++; } y--; } }
/*TODO*///static void pb_8_nd_fy_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->height-1-x; while(h-->0){ int c=w; x=t; while(c-->0){ b->line[x][y] = p; x--; } y++; } }
/*TODO*///static void pb_8_nd_fxy_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->height-1-x; y = b->width-1-y; while(h-->0){ int c=w; x=t; while(c-->0){ b->line[x][y] = p; x--; } y--; } }
/*TODO*///
/*TODO*///static void pb_8_d(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; osd_mark_dirty(t,y,t+w-1,y+h-1); while(h-->0){ int c=w; x=t; while(c-->0){ b->line[y][x] = p; x++; } y++; } }
/*TODO*///static void pb_8_d_fx(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->width-1-x;  osd_mark_dirty(t-w+1,y,t,y+h-1); while(h-->0){ int c=w; x=t; while(c-->0){ b->line[y][x] = p; x--; } y++; } }
/*TODO*///static void pb_8_d_fy(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; y = b->height-1-y; osd_mark_dirty(t,y-h+1,t+w-1,y); while(h-->0){ int c=w; x=t; while(c-->0){ b->line[y][x] = p; x++; } y--; } }
/*TODO*///static void pb_8_d_fxy(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->width-1-x; y = b->height-1-y; osd_mark_dirty(t-w+1,y-h+1,t,y); while(h-->0){ int c=w; x=t; while(c-->0){ b->line[y][x] = p; x--; } y--; } }
/*TODO*///static void pb_8_d_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; osd_mark_dirty(y,t,y+h-1,t+w-1); while(h-->0){ int c=w; x=t; while(c-->0){ b->line[x][y] = p; x++; } y++; } }
/*TODO*///static void pb_8_d_fx_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=x; y = b->width-1-y;  osd_mark_dirty(y-h+1,t,y,t+w-1); while(h-->0){ int c=w; x=t; while(c-->0){ b->line[x][y] = p; x++; } y--; } }
/*TODO*///static void pb_8_d_fy_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->height-1-x; osd_mark_dirty(y,t-w+1,y+h-1,t); while(h-->0){ int c=w; x=t; while(c-->0){ b->line[x][y] = p; x--; } y++; } }
/*TODO*///static void pb_8_d_fxy_s(struct osd_bitmap *b,int x,int y,int w,int h,UINT32 p)  { int t=b->height-1-x; y = b->width-1-y; osd_mark_dirty(y-h+1,t-w+1,y,t); while(h-->0){ int c=w; x=t; while(c-->0){ b->line[x][y] = p; x--; } y--; } }
/*TODO*///
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
/*TODO*///static void md(int sx,int sy,int ex,int ey)  { osd_mark_dirty(sx,sy,ex,ey); }
/*TODO*///static void md_fx(int sx,int sy,int ex,int ey)  { osd_mark_dirty(Machine->scrbitmap->width-1-ex,sy,Machine->scrbitmap->width-1-sx,ey); }
/*TODO*///static void md_fy(int sx,int sy,int ex,int ey)  { osd_mark_dirty(sx,Machine->scrbitmap->height-1-ey,ex,Machine->scrbitmap->height-1-sy); }
/*TODO*///static void md_fxy(int sx,int sy,int ex,int ey)  { osd_mark_dirty(Machine->scrbitmap->width-1-ex,Machine->scrbitmap->height-1-ey,Machine->scrbitmap->width-1-sx,Machine->scrbitmap->height-1-sy); }
/*TODO*///static void md_s(int sx,int sy,int ex,int ey)  { osd_mark_dirty(sy,sx,ey,ex); }
/*TODO*///static void md_fx_s(int sx,int sy,int ex,int ey)  { osd_mark_dirty(Machine->scrbitmap->width-1-ey,sx,Machine->scrbitmap->width-1-sy,ex); }
/*TODO*///static void md_fy_s(int sx,int sy,int ex,int ey)  { osd_mark_dirty(sy,Machine->scrbitmap->height-1-ex,ey,Machine->scrbitmap->height-1-sx); }
/*TODO*///static void md_fxy_s(int sx,int sy,int ex,int ey)  { osd_mark_dirty(Machine->scrbitmap->width-1-ey,Machine->scrbitmap->height-1-ex,Machine->scrbitmap->width-1-sy,Machine->scrbitmap->height-1-sx); }
/*TODO*///
/*TODO*///
/*TODO*///static plot_pixel_proc pps_8_nd[] =
/*TODO*///		{ pp_8_nd, 	 pp_8_nd_fx,   pp_8_nd_fy, 	 pp_8_nd_fxy,
/*TODO*///		  pp_8_nd_s, pp_8_nd_fx_s, pp_8_nd_fy_s, pp_8_nd_fxy_s };
/*TODO*///
/*TODO*///static plot_pixel_proc pps_8_d[] =
/*TODO*///		{ pp_8_d, 	pp_8_d_fx,   pp_8_d_fy,	  pp_8_d_fxy,
/*TODO*///		  pp_8_d_s, pp_8_d_fx_s, pp_8_d_fy_s, pp_8_d_fxy_s };
/*TODO*///
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
/*TODO*///
/*TODO*///static read_pixel_proc rps_8[] =
/*TODO*///		{ rp_8,	  rp_8_fx,   rp_8_fy,	rp_8_fxy,
/*TODO*///		  rp_8_s, rp_8_fx_s, rp_8_fy_s, rp_8_fxy_s };
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
/*TODO*///static plot_box_proc pbs_8_nd[] =
/*TODO*///		{ pb_8_nd, 	 pb_8_nd_fx,   pb_8_nd_fy, 	 pb_8_nd_fxy,
/*TODO*///		  pb_8_nd_s, pb_8_nd_fx_s, pb_8_nd_fy_s, pb_8_nd_fxy_s };
/*TODO*///
/*TODO*///static plot_box_proc pbs_8_d[] =
/*TODO*///		{ pb_8_d, 	pb_8_d_fx,   pb_8_d_fy,	  pb_8_d_fxy,
/*TODO*///		  pb_8_d_s, pb_8_d_fx_s, pb_8_d_fy_s, pb_8_d_fxy_s };
/*TODO*///
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
/*TODO*///static mark_dirty_proc mds[] =
/*TODO*///		{ md,   md_fx,   md_fy,   md_fxy,
/*TODO*///		  md_s, md_fx_s, md_fy_s, md_fxy_s };
/*TODO*///
/*TODO*///
/*TODO*///void set_pixel_functions(void)
/*TODO*///{
/*TODO*///	mark_dirty = mds[Machine->orientation];
/*TODO*///
/*TODO*///	if (Machine->color_depth == 8)
/*TODO*///	{
/*TODO*///		read_pixel = rps_8[Machine->orientation];
/*TODO*///
/*TODO*///		if (Machine->drv->video_attributes & VIDEO_SUPPORTS_DIRTY)
/*TODO*///		{
/*TODO*///			plot_pixel = pps_8_d[Machine->orientation];
/*TODO*///			plot_box = pbs_8_d[Machine->orientation];
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			plot_pixel = pps_8_nd[Machine->orientation];
/*TODO*///			plot_box = pbs_8_nd[Machine->orientation];
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else if(Machine->color_depth == 15 || Machine->color_depth == 16)
/*TODO*///	{
/*TODO*///		read_pixel = rps_16[Machine->orientation];
/*TODO*///
/*TODO*///		if (Machine->drv->video_attributes & VIDEO_SUPPORTS_DIRTY)
/*TODO*///		{
/*TODO*///			plot_pixel = pps_16_d[Machine->orientation];
/*TODO*///			plot_box = pbs_16_d[Machine->orientation];
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			plot_pixel = pps_16_nd[Machine->orientation];
/*TODO*///			plot_box = pbs_16_nd[Machine->orientation];
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		read_pixel = rps_32[Machine->orientation];
/*TODO*///
/*TODO*///		if (Machine->drv->video_attributes & VIDEO_SUPPORTS_DIRTY)
/*TODO*///		{
/*TODO*///			plot_pixel = pps_32_d[Machine->orientation];
/*TODO*///			plot_box = pbs_32_d[Machine->orientation];
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			plot_pixel = pps_32_nd[Machine->orientation];
/*TODO*///			plot_box = pbs_32_nd[Machine->orientation];
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* while we're here, fill in the raw drawing mode table as well */
/*TODO*///	is_raw[TRANSPARENCY_NONE_RAW]      = 1;
/*TODO*///	is_raw[TRANSPARENCY_PEN_RAW]       = 1;
/*TODO*///	is_raw[TRANSPARENCY_PENS_RAW]      = 1;
/*TODO*///	is_raw[TRANSPARENCY_PEN_TABLE_RAW] = 1;
/*TODO*///	is_raw[TRANSPARENCY_BLEND_RAW]     = 1;
/*TODO*///}
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
}
