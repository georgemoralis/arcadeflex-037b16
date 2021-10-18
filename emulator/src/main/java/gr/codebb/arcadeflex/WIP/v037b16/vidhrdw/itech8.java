/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.WIP.v037b16.vidhrdw;

//generic functions
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//cpu imports
import static gr.codebb.arcadeflex.v037b16.mame.paletteH.*;
import static gr.codebb.arcadeflex.v037b16.mame.palette.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
//driver imports
import static gr.codebb.arcadeflex.WIP.v037b16.drivers.itech8.*;
//vidhrdw imports
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.tms34061.*;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.tms34061H.*;
//to be organized
import static arcadeflex036.osdepend.logerror;
import common.libc.cstdio.FILE;
import static common.libc.cstdio.fopen;
import static common.libc.cstring.memset;
import common.ptr.UBytePtr;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.avgdvg.height;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.avgdvg.width;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.capbowl.tms34061intf;
import static gr.codebb.arcadeflex.v037b16.mame.common.memory_region;
import static gr.codebb.arcadeflex.v037b16.mame.common.memory_region_length;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.REGION_GFX1;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.cpu_getpreviouspc;
import static mame037b7.palette.palette_change_color;

public class itech8 {

    /**
     * ***********************************
     *
     * Debugging
     *
     ************************************
     */
    public static final int FULL_LOGGING = 0;
    public static final int BLIT_LOGGING = 0;
    public static final int INSTANT_BLIT = 1;

    /**
     * ***********************************
     *
     * Blitter constants
     *
     ************************************
     */
    public static final int BLITFLAG_SHIFT = 0x01;
    public static final int BLITFLAG_XFLIP = 0x02;
    public static final int BLITFLAG_YFLIP = 0x04;
    public static final int BLITFLAG_RLE = 0x08;
    public static final int BLITFLAG_TRANSPARENT = 0x10;

    /**
     * ***********************************
     *
     * Global variables
     *
     ************************************
     */
    public static UBytePtr itech8_grom_bank = new UBytePtr();
    public static UBytePtr itech8_display_page = new UBytePtr();

    static int/*UINT8*/ u8_palette_addr;
    static int/*UINT8*/ u8_palette_index;
    static int[]/*UINT8*/ u8_palette_data = new int[3];

    static int[]/*UINT8*/ u8_blitter_data = new int[16];
    static int/*UINT8*/ blit_in_progress;

    static int/*UINT8*/ slikshot;

    static tms34061_display tms_state = new tms34061_display();
    static UBytePtr grom_base;
    static int/*UINT32*/ grom_size;

    /**
     * ***********************************
     *
     * TMS34061 interfacing
     *
     ************************************
     */
    public static TmsInterruptPtr generate_interrupt = new TmsInterruptPtr() {
        public void handler(int state) {
            itech8_update_interrupts(-1, state, -1);

            if (FULL_LOGGING != 0 && state != 0) {
                logerror("------------ DISPLAY INT (%d) --------------\n", cpu_getscanline());
            }
        }
    };

    static tms34061_interface tms34061intf = new tms34061_interface(
            8, /* VRAM address is (row << rowshift) | col */
            0x40000, /* size of video RAM */
            0x100, /* size of dirty chunks (must be power of 2) */
            generate_interrupt /* interrupt gen callback */
    );

    /**
     * ***********************************
     *
     * Video start
     *
     ************************************
     */
    public static VhStartPtr itech8_vh_start = new VhStartPtr() {
        public int handler() {
            /* initialize TMS34061 emulation */
            if (tms34061_start(tms34061intf) != 0) {
                return 1;
            }

            /* get the TMS34061 display state */
            tms34061_get_display_state(tms_state);

            /* reset palette usage */
            memset(palette_used_colors, PALETTE_COLOR_USED, 256);

            /* reset statics */
            u8_palette_addr = 0;
            u8_palette_index = 0;
            slikshot = 0;

            /* fetch the GROM base */
            grom_base = memory_region(REGION_GFX1);
            grom_size = memory_region_length(REGION_GFX1);

            return 0;
        }
    };

    public static VhStartPtr slikshot_vh_start = new VhStartPtr() {
        public int handler() {
            int result = itech8_vh_start.handler();
            slikshot = 1;
            return result;
        }
    };

    /**
     * ***********************************
     *
     * Video stop
     *
     ************************************
     */
    public static VhStopPtr itech8_vh_stop = new VhStopPtr() {
        public void handler() {
            tms34061_stop();
        }
    };

    /**
     * ***********************************
     *
     * Palette I/O
     *
     ************************************
     */
    public static WriteHandlerPtr itech8_palette_address_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* latch the address */
            u8_palette_addr = data & 0xFF;
            u8_palette_index = 0;
        }
    };

    public static WriteHandlerPtr itech8_palette_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* wait for 3 bytes to come in, then update the color */
            u8_palette_data[u8_palette_index++] = data & 0xFF;
            if (u8_palette_index == 3) {
                palette_change_color(u8_palette_addr++, u8_palette_data[0] << 2, u8_palette_data[1] << 2, u8_palette_data[2] << 2);
                u8_palette_index = 0;
            }
        }
    };

    /**
     * ***********************************
     *
     * Low-level blitting primitives
     *
     ************************************
     */
    public static void draw_byte(int addr, int/*UINT8*/ val, int/*UINT8*/ mask, int/*UINT8*/ latch) {
        tms_state.vram.write(addr, val & mask);
        tms_state.latchram.write(addr, latch);
    }

    public static void draw_byte_trans4(int addr, int/*UINT8*/ val, int/*UINT8*/ mask, int/*UINT8*/ latch) {
        if (val == 0) {
            return;
        }

        if ((val & 0xf0) != 0) {
            if ((val & 0x0f) != 0) {
                tms_state.vram.write(addr, val & mask);
                tms_state.latchram.write(addr, latch);
            } else {
                tms_state.vram.write(addr, (tms_state.vram.read(addr) & 0x0f) | (val & mask & 0xf0));
                tms_state.latchram.write(addr, (tms_state.latchram.read(addr) & 0x0f) | (latch & 0xf0));
            }
        } else {
            tms_state.vram.write(addr, (tms_state.vram.read(addr) & 0xf0) | (val & mask & 0x0f));
            tms_state.latchram.write(addr, (tms_state.latchram.read(addr) & 0xf0) | (latch & 0x0f));
        }
    }

    public static void draw_byte_trans8(int addr, int/*UINT8*/ val, int/*UINT8*/ mask, int/*UINT8*/ latch) {
        if (val != 0) {
            draw_byte(addr, val & 0xFF, mask & 0xFF, latch & 0xFF);
        }
    }

    /**
     * ***********************************
     *
     * Low-level shifted blitting primitives
     *
     ************************************
     */
    public static void draw_byte_shift(int addr, int/*UINT8*/ val, int/*UINT8*/ mask, int/*UINT8*/ latch) {
        tms_state.vram.write(addr, (tms_state.vram.read(addr) & 0xf0) | ((val & mask) >> 4));
        tms_state.latchram.write(addr, (tms_state.latchram.read(addr) & 0xf0) | (latch >> 4));
        tms_state.vram.write(addr + 1, (tms_state.vram.read(addr + 1) & 0x0f) | ((val & mask) << 4));
        tms_state.latchram.write(addr + 1, (tms_state.latchram.read(addr + 1) & 0x0f) | (latch << 4));
    }

    public static void draw_byte_shift_trans4(int addr, int/*UINT8*/ val, int/*UINT8*/ mask, int/*UINT8*/ latch) {
        if (val == 0) {
            return;
        }

        if ((val & 0xf0) != 0) {
            tms_state.vram.write(addr, (tms_state.vram.read(addr) & 0xf0) | ((val & mask) >> 4));
            tms_state.latchram.write(addr, (tms_state.latchram.read(addr) & 0xf0) | (latch >> 4));
        }
        if ((val & 0x0f) != 0) {
            tms_state.vram.write(addr + 1, (tms_state.vram.read(addr + 1) & 0x0f) | ((val & mask) << 4));
            tms_state.latchram.write(addr + 1, (tms_state.latchram.read(addr + 1) & 0x0f) | (latch << 4));
        }
    }
    /*TODO*///	INLINE void draw_byte_shift_trans8(offs_t addr, UINT8 val, UINT8 mask, UINT8 latch)
/*TODO*///	{
/*TODO*///		if (val != 0) draw_byte_shift(addr, val, mask, latch);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Low-level flipped blitting primitives
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	INLINE void draw_byte_xflip(offs_t addr, UINT8 val, UINT8 mask, UINT8 latch)
/*TODO*///	{
/*TODO*///		val = (val >> 4) | (val << 4);
/*TODO*///		draw_byte(addr, val, mask, latch);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	INLINE void draw_byte_trans4_xflip(offs_t addr, UINT8 val, UINT8 mask, UINT8 latch)
/*TODO*///	{
/*TODO*///		val = (val >> 4) | (val << 4);
/*TODO*///		draw_byte_trans4(addr, val, mask, latch);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	INLINE void draw_byte_shift_xflip(offs_t addr, UINT8 val, UINT8 mask, UINT8 latch)
/*TODO*///	{
/*TODO*///		val = (val >> 4) | (val << 4);
/*TODO*///		draw_byte_shift(addr, val, mask, latch);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	INLINE void draw_byte_shift_trans4_xflip(offs_t addr, UINT8 val, UINT8 mask, UINT8 latch)
/*TODO*///	{
/*TODO*///		val = (val >> 4) | (val << 4);
/*TODO*///		draw_byte_shift_trans4(addr, val, mask, latch);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Uncompressed blitter macro
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	#define DRAW_RAW_MACRO(NAME, TRANSPARENT, OPERATION) 										\
/*TODO*///	static void NAME(void)																		\
/*TODO*///	{																							\
/*TODO*///		UINT8 *src = &grom_base[((itech8_grom_bank.read() << 16) | (u8_blitter_data[0] << 8) | u8_blitter_data[1]) % grom_size];\
/*TODO*///		offs_t addr = tms_state.regs[TMS34061_XYADDRESS] | ((tms_state.regs[TMS34061_XYOFFSET] & 0x300) << 8);\
/*TODO*///		int ydir = (u8_blitter_data[2] & BLITFLAG_YFLIP) ? -1 : 1;									\
/*TODO*///		int xdir = (u8_blitter_data[2] & BLITFLAG_XFLIP) ? -1 : 1;									\
/*TODO*///		int color = tms34061_latch_r(0);														\
/*TODO*///		int width = u8_blitter_data[4];																\
/*TODO*///		int height = u8_blitter_data[5];															\
/*TODO*///		UINT8 mask = u8_blitter_data[6];																\
/*TODO*///		UINT8 skip[3];																			\
/*TODO*///		int x, y;																				\
/*TODO*///																								\
/*TODO*///		/* compute horiz skip counts */															\
/*TODO*///		skip[0] = u8_blitter_data[8];																\
/*TODO*///		skip[1] = (width <= u8_blitter_data[10]) ? 0 : width - 1 - u8_blitter_data[10];						\
/*TODO*///		if (xdir == -1) { int temp = skip[0]; skip[0] = skip[1]; skip[1] = temp; }				\
/*TODO*///		width -= skip[0] + skip[1];																\
/*TODO*///																								\
/*TODO*///		/* compute vertical skip counts */														\
/*TODO*///		if (ydir == 1)																			\
/*TODO*///		{																						\
/*TODO*///			skip[2] = (height <= u8_blitter_data[9]) ? 0 : height - u8_blitter_data[9];					\
/*TODO*///			if (u8_blitter_data[11] > 1) height -= u8_blitter_data[11] - 1;									\
/*TODO*///		}																						\
/*TODO*///		else																					\
/*TODO*///		{																						\
/*TODO*///			skip[2] = (height <= u8_blitter_data[11]) ? 0 : height - u8_blitter_data[11];					\
/*TODO*///			if (u8_blitter_data[9] > 1) height -= u8_blitter_data[9] - 1;								\
/*TODO*///		}																						\
/*TODO*///																								\
/*TODO*///		/* skip top */																			\
/*TODO*///		for (y = 0; y < skip[2]; y++)															\
/*TODO*///		{																						\
/*TODO*///			/* skip src and dest */																\
/*TODO*///			addr += xdir * (width + skip[0] + skip[1]);											\
/*TODO*///			src += width + skip[0] + skip[1];													\
/*TODO*///																								\
/*TODO*///			/* back up one and reverse directions */											\
/*TODO*///			addr -= xdir;																		\
/*TODO*///			addr += ydir * 256;																	\
/*TODO*///			addr &= 0x3ffff;																	\
/*TODO*///			xdir = -xdir;																		\
/*TODO*///		}																						\
/*TODO*///																								\
/*TODO*///		/* loop over height */																	\
/*TODO*///		for (y = skip[2]; y < height; y++)														\
/*TODO*///		{																						\
/*TODO*///			/* skip left */																		\
/*TODO*///			addr += xdir * skip[y & 1];															\
/*TODO*///			src += skip[y & 1];																	\
/*TODO*///																								\
/*TODO*///			/* loop over width */																\
/*TODO*///			for (x = 0; x < width; x++)															\
/*TODO*///			{																					\
/*TODO*///				OPERATION(addr, *src++, mask, color);											\
/*TODO*///				addr += xdir;																	\
/*TODO*///			}																					\
/*TODO*///																								\
/*TODO*///			/* skip right */																	\
/*TODO*///			addr += xdir * skip[~y & 1];														\
/*TODO*///			src += skip[~y & 1];																\
/*TODO*///																								\
/*TODO*///			/* back up one and reverse directions */											\
/*TODO*///			addr -= xdir;																		\
/*TODO*///			addr += ydir * 256;																	\
/*TODO*///			addr &= 0x3ffff;																	\
/*TODO*///			xdir = -xdir;																		\
/*TODO*///		}																						\
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Compressed blitter macro
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	#define DRAW_RLE_MACRO(NAME, TRANSPARENT, OPERATION) 										\
/*TODO*///	static void NAME(void)																		\
/*TODO*///	{																							\
/*TODO*///		UINT8 *src = &grom_base[((itech8_grom_bank.read() << 16) | (u8_blitter_data[0] << 8) | u8_blitter_data[1]) % grom_size];\
/*TODO*///		offs_t addr = tms_state.regs[TMS34061_XYADDRESS] | ((tms_state.regs[TMS34061_XYOFFSET] & 0x300) << 8);\
/*TODO*///		int ydir = (u8_blitter_data[2] & BLITFLAG_YFLIP) ? -1 : 1;									\
/*TODO*///		int xdir = (u8_blitter_data[2] & BLITFLAG_XFLIP) ? -1 : 1;									\
/*TODO*///		int count = 0, val = -1, innercount;													\
/*TODO*///		int color = tms34061_latch_r(0);														\
/*TODO*///		int width = u8_blitter_data[4];																\
/*TODO*///		int height = u8_blitter_data[5];															\
/*TODO*///		UINT8 mask = u8_blitter_data[6];																\
/*TODO*///		UINT8 skip[3];																			\
/*TODO*///		int xleft, y;																			\
/*TODO*///																								\
/*TODO*///		/* skip past the double-0's */															\
/*TODO*///		src += 2;																				\
/*TODO*///																								\
/*TODO*///		/* compute horiz skip counts */															\
/*TODO*///		skip[0] = u8_blitter_data[8];																\
/*TODO*///		skip[1] = (width <= u8_blitter_data[10]) ? 0 : width - 1 - u8_blitter_data[10];						\
/*TODO*///		if (xdir == -1) { int temp = skip[0]; skip[0] = skip[1]; skip[1] = temp; }				\
/*TODO*///		width -= skip[0] + skip[1];																\
/*TODO*///																								\
/*TODO*///		/* compute vertical skip counts */														\
/*TODO*///		if (ydir == 1)																			\
/*TODO*///		{																						\
/*TODO*///			skip[2] = (height <= u8_blitter_data[9]) ? 0 : height - u8_blitter_data[9];					\
/*TODO*///			if (u8_blitter_data[11] > 1) height -= u8_blitter_data[11] - 1;									\
/*TODO*///		}																						\
/*TODO*///		else																					\
/*TODO*///		{																						\
/*TODO*///			skip[2] = (height <= u8_blitter_data[11]) ? 0 : height - u8_blitter_data[11];					\
/*TODO*///			if (u8_blitter_data[9] > 1) height -= u8_blitter_data[9] - 1;								\
/*TODO*///		}																						\
/*TODO*///																								\
/*TODO*///		/* skip top */																			\
/*TODO*///		for (y = 0; y < skip[2]; y++)															\
/*TODO*///		{																						\
/*TODO*///			/* skip dest */																		\
/*TODO*///			addr += xdir * (width + skip[0] + skip[1]);											\
/*TODO*///																								\
/*TODO*///			/* scan RLE until done */															\
/*TODO*///			for (xleft = width + skip[0] + skip[1]; xleft > 0; )								\
/*TODO*///			{																					\
/*TODO*///				/* load next RLE chunk if needed */												\
/*TODO*///				if (!count)																		\
/*TODO*///				{																				\
/*TODO*///					count = *src++;																\
/*TODO*///					val = (count & 0x80) ? -1 : *src++;											\
/*TODO*///					count &= 0x7f;																\
/*TODO*///				}																				\
/*TODO*///																								\
/*TODO*///				/* determine how much to bite off */											\
/*TODO*///				innercount = (xleft > count) ? count : xleft;									\
/*TODO*///				count -= innercount;															\
/*TODO*///				xleft -= innercount;															\
/*TODO*///																								\
/*TODO*///				/* skip past the data */														\
/*TODO*///				if (val == -1) src += innercount;												\
/*TODO*///			}																					\
/*TODO*///																								\
/*TODO*///			/* back up one and reverse directions */											\
/*TODO*///			addr -= xdir;																		\
/*TODO*///			addr += ydir * 256;																	\
/*TODO*///			addr &= 0x3ffff;																	\
/*TODO*///			xdir = -xdir;																		\
/*TODO*///		}																						\
/*TODO*///																								\
/*TODO*///		/* loop over height */																	\
/*TODO*///		for (y = skip[2]; y < height; y++)														\
/*TODO*///		{																						\
/*TODO*///			/* skip left */																		\
/*TODO*///			addr += xdir * skip[y & 1];															\
/*TODO*///			for (xleft = skip[y & 1]; xleft > 0; )												\
/*TODO*///			{																					\
/*TODO*///				/* load next RLE chunk if needed */												\
/*TODO*///				if (!count)																		\
/*TODO*///				{																				\
/*TODO*///					count = *src++;																\
/*TODO*///					val = (count & 0x80) ? -1 : *src++;											\
/*TODO*///					count &= 0x7f;																\
/*TODO*///				}																				\
/*TODO*///																								\
/*TODO*///				/* determine how much to bite off */											\
/*TODO*///				innercount = (xleft > count) ? count : xleft;									\
/*TODO*///				count -= innercount;															\
/*TODO*///				xleft -= innercount;															\
/*TODO*///																								\
/*TODO*///				/* skip past the data */														\
/*TODO*///				if (val == -1) src += innercount;												\
/*TODO*///			}																					\
/*TODO*///																								\
/*TODO*///			/* loop over width */																\
/*TODO*///			for (xleft = width; xleft > 0; )													\
/*TODO*///			{																					\
/*TODO*///				/* load next RLE chunk if needed */												\
/*TODO*///				if (!count)																		\
/*TODO*///				{																				\
/*TODO*///					count = *src++;																\
/*TODO*///					val = (count & 0x80) ? -1 : *src++;											\
/*TODO*///					count &= 0x7f;																\
/*TODO*///				}																				\
/*TODO*///																								\
/*TODO*///				/* determine how much to bite off */											\
/*TODO*///				innercount = (xleft > count) ? count : xleft;									\
/*TODO*///				count -= innercount;															\
/*TODO*///				xleft -= innercount;															\
/*TODO*///																								\
/*TODO*///				/* run of literals */															\
/*TODO*///				if (val == -1)																	\
/*TODO*///					for ( ; innercount--; addr += xdir)											\
/*TODO*///						OPERATION(addr, *src++, mask, color);									\
/*TODO*///																								\
/*TODO*///				/* run of non-transparent repeats */											\
/*TODO*///				else if (!TRANSPARENT || val)													\
/*TODO*///					for ( ; innercount--; addr += xdir)											\
/*TODO*///						OPERATION(addr, val, mask, color);										\
/*TODO*///																								\
/*TODO*///				/* run of transparent repeats */												\
/*TODO*///				else																			\
/*TODO*///					addr += xdir * innercount;													\
/*TODO*///			}																					\
/*TODO*///																								\
/*TODO*///			/* skip right */																	\
/*TODO*///			addr += xdir * skip[~y & 1];														\
/*TODO*///			for (xleft = skip[~y & 1]; xleft > 0; )												\
/*TODO*///			{																					\
/*TODO*///				/* load next RLE chunk if needed */												\
/*TODO*///				if (!count)																		\
/*TODO*///				{																				\
/*TODO*///					count = *src++;																\
/*TODO*///					val = (count & 0x80) ? -1 : *src++;											\
/*TODO*///					count &= 0x7f;																\
/*TODO*///				}																				\
/*TODO*///																								\
/*TODO*///				/* determine how much to bite off */											\
/*TODO*///				innercount = (xleft > count) ? count : xleft;									\
/*TODO*///				count -= innercount;															\
/*TODO*///				xleft -= innercount;															\
/*TODO*///																								\
/*TODO*///				/* skip past the data */														\
/*TODO*///				if (val == -1) src += innercount;												\
/*TODO*///			}																					\
/*TODO*///																								\
/*TODO*///			/* back up one and reverse directions */											\
/*TODO*///			addr -= xdir;																		\
/*TODO*///			addr += ydir * 256;																	\
/*TODO*///			addr &= 0x3ffff;																	\
/*TODO*///			xdir = -xdir;																		\
/*TODO*///		}																						\
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Blitter functions and tables
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	DRAW_RAW_MACRO(draw_raw,              0, draw_byte)
/*TODO*///	DRAW_RAW_MACRO(draw_raw_shift,        0, draw_byte_shift)
/*TODO*///	DRAW_RAW_MACRO(draw_raw_trans4,       1, draw_byte_trans4)
/*TODO*///	DRAW_RAW_MACRO(draw_raw_trans8,       1, draw_byte_trans8)
/*TODO*///	DRAW_RAW_MACRO(draw_raw_shift_trans4, 1, draw_byte_shift_trans4)
/*TODO*///	DRAW_RAW_MACRO(draw_raw_shift_trans8, 1, draw_byte_shift_trans8)
/*TODO*///	
/*TODO*///	DRAW_RLE_MACRO(draw_rle,              0, draw_byte)
/*TODO*///	DRAW_RLE_MACRO(draw_rle_shift,        0, draw_byte_shift)
/*TODO*///	DRAW_RLE_MACRO(draw_rle_trans4,       1, draw_byte_trans4)
/*TODO*///	DRAW_RLE_MACRO(draw_rle_trans8,       1, draw_byte_trans8)
/*TODO*///	DRAW_RLE_MACRO(draw_rle_shift_trans4, 1, draw_byte_shift_trans4)
/*TODO*///	DRAW_RLE_MACRO(draw_rle_shift_trans8, 1, draw_byte_shift_trans8)
/*TODO*///	
/*TODO*///	DRAW_RAW_MACRO(draw_raw_xflip,              0, draw_byte_xflip)
/*TODO*///	DRAW_RAW_MACRO(draw_raw_shift_xflip,        0, draw_byte_shift_xflip)
/*TODO*///	DRAW_RAW_MACRO(draw_raw_trans4_xflip,       1, draw_byte_trans4_xflip)
/*TODO*///	DRAW_RAW_MACRO(draw_raw_shift_trans4_xflip, 1, draw_byte_shift_trans4_xflip)
/*TODO*///	
/*TODO*///	DRAW_RLE_MACRO(draw_rle_xflip,              0, draw_byte_xflip)
/*TODO*///	DRAW_RLE_MACRO(draw_rle_shift_xflip,        0, draw_byte_shift_xflip)
/*TODO*///	DRAW_RLE_MACRO(draw_rle_trans4_xflip,       1, draw_byte_trans4_xflip)
/*TODO*///	DRAW_RLE_MACRO(draw_rle_shift_trans4_xflip, 1, draw_byte_shift_trans4_xflip)
/*TODO*///	
/*TODO*///	
/*TODO*///	static void (*blit_table4[0x20])(void) =
/*TODO*///	{
/*TODO*///		draw_raw,			draw_raw_shift,			draw_raw,			draw_raw_shift,
/*TODO*///		draw_raw,			draw_raw_shift,			draw_raw,			draw_raw_shift,
/*TODO*///		draw_rle,			draw_rle_shift,			draw_rle,			draw_rle_shift,
/*TODO*///		draw_rle,			draw_rle_shift,			draw_rle,			draw_rle_shift,
/*TODO*///		draw_raw_trans4,	draw_raw_shift_trans4,	draw_raw_trans4,	draw_raw_shift_trans4,
/*TODO*///		draw_raw_trans4,	draw_raw_shift_trans4,	draw_raw_trans4,	draw_raw_shift_trans4,
/*TODO*///		draw_rle_trans4,	draw_rle_shift_trans4,	draw_rle_trans4,	draw_rle_shift_trans4,
/*TODO*///		draw_rle_trans4,	draw_rle_shift_trans4,	draw_rle_trans4,	draw_rle_shift_trans4
/*TODO*///	};
/*TODO*///	
/*TODO*///	static void (*blit_table4_xflip[0x20])(void) =
/*TODO*///	{
/*TODO*///		draw_raw_xflip,			draw_raw_shift_xflip,			draw_raw_xflip,			draw_raw_shift_xflip,
/*TODO*///		draw_raw_xflip,			draw_raw_shift_xflip,			draw_raw_xflip,			draw_raw_shift_xflip,
/*TODO*///		draw_rle_xflip,			draw_rle_shift_xflip,			draw_rle_xflip,			draw_rle_shift_xflip,
/*TODO*///		draw_rle_xflip,			draw_rle_shift_xflip,			draw_rle_xflip,			draw_rle_shift_xflip,
/*TODO*///		draw_raw_trans4_xflip,	draw_raw_shift_trans4_xflip,	draw_raw_trans4_xflip,	draw_raw_shift_trans4_xflip,
/*TODO*///		draw_raw_trans4_xflip,	draw_raw_shift_trans4_xflip,	draw_raw_trans4_xflip,	draw_raw_shift_trans4_xflip,
/*TODO*///		draw_rle_trans4_xflip,	draw_rle_shift_trans4_xflip,	draw_rle_trans4_xflip,	draw_rle_shift_trans4_xflip,
/*TODO*///		draw_rle_trans4_xflip,	draw_rle_shift_trans4_xflip,	draw_rle_trans4_xflip,	draw_rle_shift_trans4_xflip
/*TODO*///	};
/*TODO*///	
/*TODO*///	static void (*blit_table8[0x20])(void) =
/*TODO*///	{
/*TODO*///		draw_raw,			draw_raw_shift,			draw_raw,			draw_raw_shift,
/*TODO*///		draw_raw,			draw_raw_shift,			draw_raw,			draw_raw_shift,
/*TODO*///		draw_rle,			draw_rle_shift,			draw_rle,			draw_rle_shift,
/*TODO*///		draw_rle,			draw_rle_shift,			draw_rle,			draw_rle_shift,
/*TODO*///		draw_raw_trans8,	draw_raw_shift_trans8,	draw_raw_trans8,	draw_raw_shift_trans8,
/*TODO*///		draw_raw_trans8,	draw_raw_shift_trans8,	draw_raw_trans8,	draw_raw_shift_trans8,
/*TODO*///		draw_rle_trans8,	draw_rle_shift_trans8,	draw_rle_trans8,	draw_rle_shift_trans8,
/*TODO*///		draw_rle_trans8,	draw_rle_shift_trans8,	draw_rle_trans8,	draw_rle_shift_trans8
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Blitter operations
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static int perform_blit()
/*TODO*///	{
/*TODO*///		/* debugging */
/*TODO*///		if (FULL_LOGGING != 0)
/*TODO*///			logerror("Blit: scan=%d  src=%06x @ (%05x) for %dx%d ... flags=%02x\n",
/*TODO*///					cpu_getscanline(),
/*TODO*///					(itech8_grom_bank.read() << 16) | (u8_blitter_data[0] << 8) | u8_blitter_data[1],
/*TODO*///					0, u8_blitter_data[4], u8_blitter_data[5], u8_blitter_data[2]);
/*TODO*///	
/*TODO*///		/* draw appropriately */
/*TODO*///		if ((u8_blitter_data[7] & 0x40) != 0)
/*TODO*///		{
/*TODO*///			if ((u8_blitter_data[2] & BLITFLAG_XFLIP) != 0)
/*TODO*///				(*blit_table4_xflip[u8_blitter_data[2] & 0x1f])();
/*TODO*///			else
/*TODO*///				(*blit_table4[u8_blitter_data[2] & 0x1f])();
/*TODO*///		}
/*TODO*///		else
/*TODO*///			(*blit_table8[u8_blitter_data[2] & 0x1f])();
/*TODO*///	
/*TODO*///		/* return the number of bytes processed */
/*TODO*///		return u8_blitter_data[4] * u8_blitter_data[5];
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static void blitter_done(int param)
/*TODO*///	{
/*TODO*///		/* turn off blitting and generate an interrupt */
/*TODO*///		blit_in_progress = 0;
/*TODO*///		itech8_update_interrupts(-1, -1, 1);
/*TODO*///	
/*TODO*///		if (FULL_LOGGING != 0) logerror("------------ BLIT DONE (%d) --------------\n", cpu_getscanline());
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Blitter I/O
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr itech8_blitter_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int result = u8_blitter_data[offset / 2];
/*TODO*///	
/*TODO*///		/* debugging */
/*TODO*///		if (FULL_LOGGING != 0) logerror("%04x:blitter_r(%02x)\n", cpu_getpreviouspc(), offset / 2);
/*TODO*///	
/*TODO*///		/* low bit seems to be ignored */
/*TODO*///		offset /= 2;
/*TODO*///	
/*TODO*///		/* a read from offset 3 clears the interrupt and returns the status */
/*TODO*///		if (offset == 3)
/*TODO*///		{
/*TODO*///			itech8_update_interrupts(-1, -1, 0);
/*TODO*///			if (blit_in_progress != 0)
/*TODO*///				result |= 0x80;
/*TODO*///			else
/*TODO*///				result &= 0x7f;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return result;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr itech8_blitter_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		/* low bit seems to be ignored */
/*TODO*///		offset /= 2;
/*TODO*///		u8_blitter_data[offset] = data&0xFF;
/*TODO*///	
/*TODO*///		/* a write to offset 3 starts things going */
/*TODO*///		if (offset == 3)
/*TODO*///		{
/*TODO*///			int pixels;
/*TODO*///	
/*TODO*///			/* log to the blitter file */
/*TODO*///			if (BLIT_LOGGING != 0)
/*TODO*///			{
/*TODO*///				static FILE *blitlog;
/*TODO*///				if (!blitlog) blitlog = fopen("blitter.log", "w");
/*TODO*///				if (blitlog != 0) fprintf(blitlog, "Blit: XY=%1X%02X%02X SRC=%02X%02X%02X SIZE=%3dx%3d FLAGS=%02x",
/*TODO*///							tms34061_r(14*4+2, 0, 0) & 0x0f, tms34061_r(15*4+2, 0, 0), tms34061_r(15*4+0, 0, 0),
/*TODO*///							itech8_grom_bank.read(), u8_blitter_data[0], u8_blitter_data[1],
/*TODO*///							u8_blitter_data[4], u8_blitter_data[5],
/*TODO*///							u8_blitter_data[2]);
/*TODO*///				if (blitlog != 0) fprintf(blitlog, "   %02X %02X %02X [%02X] %02X %02X %02X [%02X]-%02X %02X %02X %02X [%02X %02X %02X %02X]\n",
/*TODO*///							u8_blitter_data[0], u8_blitter_data[1],
/*TODO*///							u8_blitter_data[2], u8_blitter_data[3],
/*TODO*///							u8_blitter_data[4], u8_blitter_data[5],
/*TODO*///							u8_blitter_data[6], u8_blitter_data[7],
/*TODO*///							u8_blitter_data[8], u8_blitter_data[9],
/*TODO*///							u8_blitter_data[10], u8_blitter_data[11],
/*TODO*///							u8_blitter_data[12], u8_blitter_data[13],
/*TODO*///							u8_blitter_data[14], u8_blitter_data[15]);
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* perform the blit */
/*TODO*///			pixels = perform_blit();
/*TODO*///			blit_in_progress = 1;
/*TODO*///	
/*TODO*///			/* set a timer to go off when we're done */
/*TODO*///			if (INSTANT_BLIT != 0)
/*TODO*///				blitter_done(0);
/*TODO*///			else
/*TODO*///				timer_set((double)pixels * TIME_IN_HZ(12000000), 0, blitter_done);
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* debugging */
/*TODO*///		if (FULL_LOGGING != 0) logerror("%04x:blitter_w(%02x)=%02x\n", cpu_getpreviouspc(), offset, data);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	TMS34061 I/O
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr itech8_tms34061_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		int func = (offset >> 9) & 7;
/*TODO*///		int col = offset & 0xff;
/*TODO*///	
/*TODO*///		/* Column address (CA0-CA8) is hooked up the A0-A7, with A1 being inverted
/*TODO*///		   during register access. CA8 is ignored */
/*TODO*///		if (func == 0 || func == 2)
/*TODO*///			col ^= 2;
/*TODO*///	
/*TODO*///		/* Row address (RA0-RA8) is not dependent on the offset */
/*TODO*///		tms34061_w(col, 0xff, func, data);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr itech8_tms34061_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int func = (offset >> 9) & 7;
/*TODO*///		int col = offset & 0xff;
/*TODO*///	
/*TODO*///		/* Column address (CA0-CA8) is hooked up the A0-A7, with A1 being inverted
/*TODO*///		   during register access. CA8 is ignored */
/*TODO*///		if (func == 0 || func == 2)
/*TODO*///			col ^= 2;
/*TODO*///	
/*TODO*///		/* Row address (RA0-RA8) is not dependent on the offset */
/*TODO*///		return tms34061_r(col, 0xff, func);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Main refresh
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	public static VhUpdatePtr itech8_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
/*TODO*///	{
/*TODO*///		int y, ty;
/*TODO*///	
/*TODO*///		/* first get the current display state */
/*TODO*///		tms34061_get_display_state(&tms_state);
/*TODO*///	
/*TODO*///		/* if we're blanked, just fill with black */
/*TODO*///		if (tms_state.blanked)
/*TODO*///		{
/*TODO*///			fillbitmap(bitmap, palette_transparent_pen, &Machine.visible_area);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* recalc the palette */
/*TODO*///		palette_recalc();
/*TODO*///	
/*TODO*///		/* perform one of two types of blitting; I'm not sure if bit 40 in */
/*TODO*///		/* the blitter mode register really controls this type of behavior, but */
/*TODO*///		/* it is set consistently enough that we can use it */
/*TODO*///	
/*TODO*///		/* blit mode one: 4bpp in the TMS34061 RAM, plus 4bpp of latched data */
/*TODO*///		/* two pages are available, at 0x00000 and 0x20000 */
/*TODO*///		/* pages are selected via the display page register */
/*TODO*///		/* width can be up to 512 pixels */
/*TODO*///		if ((u8_blitter_data[7] & 0x40) != 0)
/*TODO*///		{
/*TODO*///			int halfwidth = (Machine.visible_area.max_x + 2) / 2;
/*TODO*///			UINT8 *base = &tms_state.vram[(~itech8_display_page.read() & 0x80) << 10];
/*TODO*///			UINT8 *latch = &tms_state.latchram[(~itech8_display_page.read() & 0x80) << 10];
/*TODO*///	
/*TODO*///			/* now regenerate the bitmap */
/*TODO*///			for (ty = 0, y = Machine.visible_area.min_y; y <= Machine.visible_area.max_y; y++, ty++)
/*TODO*///			{
/*TODO*///				UINT8 scanline[512];
/*TODO*///				int x;
/*TODO*///	
/*TODO*///				for (x = 0; x < halfwidth; x++)
/*TODO*///				{
/*TODO*///					scanline[x * 2 + 0] = (latch[256 * ty + x] & 0xf0) | (base[256 * ty + x] >> 4);
/*TODO*///					scanline[x * 2 + 1] = (latch[256 * ty + x] << 4) | (base[256 * ty + x] & 0x0f);
/*TODO*///				}
/*TODO*///				draw_scanline8(bitmap, 0, y, 2 * halfwidth, scanline, Machine.pens, -1);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* blit mode one: 8bpp in the TMS34061 RAM */
/*TODO*///		/* two planes are available, at 0x00000 and 0x20000 */
/*TODO*///		/* both planes are rendered; with 0x20000 transparent via color 0 */
/*TODO*///		/* width can be up to 256 pixels */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			UINT8 *base = &tms_state.vram[tms_state.dispstart & ~0x30000];
/*TODO*///	
/*TODO*///			/* now regenerate the bitmap */
/*TODO*///			for (ty = 0, y = Machine.visible_area.min_y; y <= Machine.visible_area.max_y; y++, ty++)
/*TODO*///			{
/*TODO*///				draw_scanline8(bitmap, 0, y, 256, &base[0x20000 + 256 * ty], Machine.pens, -1);
/*TODO*///				draw_scanline8(bitmap, 0, y, 256, &base[0x00000 + 256 * ty], Machine.pens, 0);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* extra rendering for slikshot */
/*TODO*///		if (slikshot != 0)
/*TODO*///			slikshot_extra_draw(bitmap);
/*TODO*///	} };
}
