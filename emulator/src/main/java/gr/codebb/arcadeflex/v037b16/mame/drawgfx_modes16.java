/*
 * ported to v0.37b16
 * 
 */
package gr.codebb.arcadeflex.v037b16.mame;

public class drawgfx_modes16 {
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
/*TODO*///
/*TODO*///DECLARE(blockmove_NtoN_opaque_noremap_flipx,(
/*TODO*///		const DATA_TYPE *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		DATA_TYPE *dstdata,int dstmodulo),
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
/*TODO*///			dstdata[0] = srcdata[8];
/*TODO*///			dstdata[1] = srcdata[7];
/*TODO*///			dstdata[2] = srcdata[6];
/*TODO*///			dstdata[3] = srcdata[5];
/*TODO*///			dstdata[4] = srcdata[4];
/*TODO*///			dstdata[5] = srcdata[3];
/*TODO*///			dstdata[6] = srcdata[2];
/*TODO*///			dstdata[7] = srcdata[1];
/*TODO*///			dstdata += 8;
/*TODO*///		}
/*TODO*///		while (dstdata < end)
/*TODO*///			*(dstdata++) = *(srcdata--);
/*TODO*///
/*TODO*///		srcdata += srcmodulo;
/*TODO*///		dstdata += dstmodulo;
/*TODO*///		srcheight--;
/*TODO*///	}
/*TODO*///})
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
}
