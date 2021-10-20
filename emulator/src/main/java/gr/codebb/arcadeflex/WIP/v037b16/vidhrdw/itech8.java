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
import static common.libc.cstdio.fprintf;
import static common.libc.cstring.memset;
import common.ptr.UBytePtr;
import common.subArrays.IntArray;
import static gr.codebb.arcadeflex.v037b16.mame.common.memory_region;
import static gr.codebb.arcadeflex.v037b16.mame.common.memory_region_length;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.REGION_GFX1;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.cpu_getpreviouspc;
import gr.codebb.arcadeflex.v037b16.mame.osdependH.osd_bitmap;
import gr.codebb.arcadeflex.v056.mame.timer.timer_callback;
import static gr.codebb.arcadeflex.v056.mame.timer.timer_set;
import static gr.codebb.arcadeflex.v056.mame.timerH.TIME_IN_HZ;
import static mame037b16.drawgfx.fillbitmap;
import static mame037b16.mame.Machine;
import static mame037b7.palette.palette_change_color;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfx_modes8.*;

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
    public static abstract interface operation_Ptr {

        public abstract void handler(int addr, int/*UINT8*/ val, int/*UINT8*/ mask, int/*UINT8*/ latch);
    }
    public static operation_Ptr draw_byte = new operation_Ptr() {
        @Override
        public void handler(int addr, int/*UINT8*/ val, int/*UINT8*/ mask, int/*UINT8*/ latch) {
            tms_state.vram.write(addr, val & mask);
            tms_state.latchram.write(addr, latch);
        }
    };

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

    public static operation_Ptr draw_byte_trans8 = new operation_Ptr() {
        @Override
        public void handler(int addr, int/*UINT8*/ val, int/*UINT8*/ mask, int/*UINT8*/ latch) {
            if (val != 0) {
                draw_byte.handler(addr, val & 0xFF, mask & 0xFF, latch & 0xFF);
            }
        }
    };

    /**
     * ***********************************
     *
     * Low-level shifted blitting primitives
     *
     ************************************
     */
    public static operation_Ptr draw_byte_shift = new operation_Ptr() {
        @Override
        public void handler(int addr, int/*UINT8*/ val, int/*UINT8*/ mask, int/*UINT8*/ latch) {
            tms_state.vram.write(addr, (tms_state.vram.read(addr) & 0xf0) | ((val & mask) >> 4));
            tms_state.latchram.write(addr, (tms_state.latchram.read(addr) & 0xf0) | (latch >> 4));
            tms_state.vram.write(addr + 1, (tms_state.vram.read(addr + 1) & 0x0f) | ((val & mask) << 4));
            tms_state.latchram.write(addr + 1, (tms_state.latchram.read(addr + 1) & 0x0f) | (latch << 4));
        }
    };

    public static operation_Ptr draw_byte_shift_trans4 = new operation_Ptr() {
        @Override
        public void handler(int addr, int/*UINT8*/ val, int/*UINT8*/ mask, int/*UINT8*/ latch) {
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
    };

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
/*TODO*///	#define DRAW_RAW_MACRO(NAME, TRANSPARENT, OPERATION) 										
    static void DRAW_RAW(int TRANSPARENT, operation_Ptr OPERATION) {
        UBytePtr src = new UBytePtr(grom_base, ((itech8_grom_bank.read() << 16) | (u8_blitter_data[0] << 8) | u8_blitter_data[1]) % grom_size);
        int addr = tms_state.regs[TMS34061_XYADDRESS] | ((tms_state.regs[TMS34061_XYOFFSET] & 0x300) << 8);
        int ydir = (u8_blitter_data[2] & BLITFLAG_YFLIP) != 0 ? -1 : 1;
        int xdir = (u8_blitter_data[2] & BLITFLAG_XFLIP) != 0 ? -1 : 1;
        int color = tms34061_latch_r.handler(0);
        int width = u8_blitter_data[4];
        int height = u8_blitter_data[5];
        int/*UINT8*/ mask = u8_blitter_data[6];
        int[]/*UINT8*/ skip = new int[3];
        int x, y;

        /* compute horiz skip counts */
        skip[0] = u8_blitter_data[8];
        skip[1] = (width <= u8_blitter_data[10]) ? 0 : (width - 1 - u8_blitter_data[10]) & 0xFF;
        if (xdir == -1) {
            int temp = skip[0];
            skip[0] = skip[1];
            skip[1] = temp;
        }
        width -= skip[0] + skip[1];

        /* compute vertical skip counts */
        if (ydir == 1) {
            skip[2] = (height <= u8_blitter_data[9]) ? 0 : (height - u8_blitter_data[9]) & 0xFF;
            if (u8_blitter_data[11] > 1) {
                height -= u8_blitter_data[11] - 1;
            }
        } else {
            skip[2] = (height <= u8_blitter_data[11]) ? 0 : (height - u8_blitter_data[11]) & 0xFF;
            if (u8_blitter_data[9] > 1) {
                height -= u8_blitter_data[9] - 1;
            }
        }

        /* skip top */
        for (y = 0; y < skip[2]; y++) {
            /* skip src and dest */
            addr += xdir * (width + skip[0] + skip[1]);
            src.inc(width + skip[0] + skip[1]);

            /* back up one and reverse directions */
            addr -= xdir;
            addr += ydir * 256;
            addr &= 0x3ffff;
            xdir = -xdir;
        }

        /* loop over height */
        for (y = skip[2]; y < height; y++) {
            /* skip left */
            addr += xdir * skip[y & 1];
            src.inc(skip[y & 1]);

            /* loop over width */
            for (x = 0; x < width; x++) {
                OPERATION.handler(addr, src.readinc(), mask & 0xFF, color & 0xFF);
                addr += xdir;
            }

            /* skip right */
            addr += xdir * skip[~y & 1];
            src.inc(skip[~y & 1]);

            /* back up one and reverse directions */
            addr -= xdir;
            addr += ydir * 256;
            addr &= 0x3ffff;
            xdir = -xdir;
        }
    }

    /**
     * ***********************************
     *
     * Compressed blitter macro
     *
     ************************************
     */
    //#define DRAW_RLE_MACRO(NAME, TRANSPARENT, OPERATION) 										
    static void DRAW_RLE(int TRANSPARENT, operation_Ptr OPERATION) {
        UBytePtr src = new UBytePtr(grom_base, ((itech8_grom_bank.read() << 16) | (u8_blitter_data[0] << 8) | u8_blitter_data[1]) % grom_size);
        int addr = tms_state.regs[TMS34061_XYADDRESS] | ((tms_state.regs[TMS34061_XYOFFSET] & 0x300) << 8);
        int ydir = (u8_blitter_data[2] & BLITFLAG_YFLIP) != 0 ? -1 : 1;
        int xdir = (u8_blitter_data[2] & BLITFLAG_XFLIP) != 0 ? -1 : 1;
        int count = 0, val = -1, innercount;
        int color = tms34061_latch_r.handler(0);
        int width = u8_blitter_data[4];
        int height = u8_blitter_data[5];
        int/*UINT8*/ mask = u8_blitter_data[6];
        int[]/*UINT8*/ skip = new int[3];
        int xleft, y;

        /* skip past the double-0's */
        src.inc(2);

        /* compute horiz skip counts */
        skip[0] = u8_blitter_data[8] & 0xFF;
        skip[1] = (width <= u8_blitter_data[10]) ? 0 : (width - 1 - u8_blitter_data[10]) & 0xFF;
        if (xdir == -1) {
            int temp = skip[0];
            skip[0] = skip[1];
            skip[1] = temp;
        }
        width -= skip[0] + skip[1];

        /* compute vertical skip counts */
        if (ydir == 1) {
            skip[2] = (height <= u8_blitter_data[9]) ? 0 : (height - u8_blitter_data[9]) & 0xFF;
            if (u8_blitter_data[11] > 1) {
                height -= u8_blitter_data[11] - 1;
            }
        } else {
            skip[2] = (height <= u8_blitter_data[11]) ? 0 : (height - u8_blitter_data[11]) & 0xFF;
            if (u8_blitter_data[9] > 1) {
                height -= u8_blitter_data[9] - 1;
            }
        }

        /* skip top */
        for (y = 0; y < skip[2]; y++) {
            /* skip dest */
            addr += xdir * (width + skip[0] + skip[1]);

            /* scan RLE until done */
            for (xleft = width + skip[0] + skip[1]; xleft > 0;) {
                /* load next RLE chunk if needed */
                if (count == 0) {
                    count = src.readinc();
                    val = (count & 0x80) != 0 ? -1 : src.readinc();
                    count &= 0x7f;
                }

                /* determine how much to bite off */
                innercount = (xleft > count) ? count : xleft;
                count -= innercount;
                xleft -= innercount;

                /* skip past the data */
                if (val == -1) {
                    src.inc(innercount);
                }
            }

            /* back up one and reverse directions */
            addr -= xdir;
            addr += ydir * 256;
            addr &= 0x3ffff;
            xdir = -xdir;
        }

        /* loop over height */
        for (y = skip[2]; y < height; y++) {
            /* skip left */
            addr += xdir * skip[y & 1];
            for (xleft = skip[y & 1]; xleft > 0;) {
                /* load next RLE chunk if needed */
                if (count == 0) {
                    count = src.readinc();
                    val = (count & 0x80) != 0 ? -1 : src.readinc();
                    count &= 0x7f;
                }

                /* determine how much to bite off */
                innercount = (xleft > count) ? count : xleft;
                count -= innercount;
                xleft -= innercount;

                /* skip past the data */
                if (val == -1) {
                    src.inc(innercount);
                }
            }

            /* loop over width */
            for (xleft = width; xleft > 0;) {
                /* load next RLE chunk if needed */
                if (count == 0) {
                    count = src.readinc();
                    val = (count & 0x80) != 0 ? -1 : src.readinc();
                    count &= 0x7f;
                }

                /* determine how much to bite off */
                innercount = (xleft > count) ? count : xleft;
                count -= innercount;
                xleft -= innercount;

                /* run of literals */
                if (val == -1) {
                    for (; innercount-- != 0; addr += xdir) {
                        OPERATION.handler(addr, src.readinc(), mask & 0xFF, color & 0xFF);
                    }
                } /* run of non-transparent repeats */ else if (TRANSPARENT == 0 || val != 0) {
                    for (; innercount-- != 0; addr += xdir) {
                        OPERATION.handler(addr, val & 0xFF, mask & 0xFF, color & 0xFF);
                    }
                } /* run of transparent repeats */ else {
                    addr += xdir * innercount;
                }
            }

            /* skip right */
            addr += xdir * skip[~y & 1];
            for (xleft = skip[~y & 1]; xleft > 0;) {
                /* load next RLE chunk if needed */
                if (count == 0) {
                    count = src.readinc();
                    val = (count & 0x80) != 0 ? -1 : src.readinc();
                    count &= 0x7f;
                }

                /* determine how much to bite off */
                innercount = (xleft > count) ? count : xleft;
                count -= innercount;
                xleft -= innercount;

                /* skip past the data */
                if (val == -1) {
                    src.inc(innercount);
                }
            }

            /* back up one and reverse directions */
            addr -= xdir;
            addr += ydir * 256;
            addr &= 0x3ffff;
            xdir = -xdir;
        }
    }

    /*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Blitter functions and tables
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	
/*TODO*///	DRAW_RAW_MACRO(draw_raw_shift,        0, draw_byte_shift)
/*TODO*///	DRAW_RAW_MACRO(draw_raw_trans4,       1, draw_byte_trans4)
/*TODO*///	DRAW_RAW_MACRO(draw_raw_trans8,       1, draw_byte_trans8)
/*TODO*///	DRAW_RAW_MACRO(draw_raw_shift_trans4, 1, draw_byte_shift_trans4)
/*TODO*///	DRAW_RAW_MACRO(draw_raw_shift_trans8, 1, draw_byte_shift_trans8)
/*TODO*///	
/*TODO*///	DRAW_RLE_MACRO(draw_rle_shift,        0, draw_byte_shift)
/*TODO*///	DRAW_RLE_MACRO(draw_rle_trans4,       1, draw_byte_trans4)
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
    public static abstract interface blitter_table_Ptr {

        public abstract void handler();
    }
    public static blitter_table_Ptr draw_raw = new blitter_table_Ptr() {
        @Override
        public void handler() {
            DRAW_RAW(0, draw_byte);
        }
    };
    public static blitter_table_Ptr draw_raw_shift = new blitter_table_Ptr() {
        @Override
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    public static blitter_table_Ptr draw_rle = new blitter_table_Ptr() {
        @Override
        public void handler() {
            DRAW_RLE(0, draw_byte);
        }
    };
    public static blitter_table_Ptr draw_rle_shift = new blitter_table_Ptr() {
        @Override
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    public static blitter_table_Ptr draw_raw_trans4 = new blitter_table_Ptr() {
        @Override
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    public static blitter_table_Ptr draw_raw_shift_trans4 = new blitter_table_Ptr() {
        @Override
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    public static blitter_table_Ptr draw_rle_trans4 = new blitter_table_Ptr() {
        @Override
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    public static blitter_table_Ptr draw_rle_shift_trans4 = new blitter_table_Ptr() {
        @Override
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    static blitter_table_Ptr blit_table4[]
            = {
                draw_raw, draw_raw_shift, draw_raw, draw_raw_shift,
                draw_raw, draw_raw_shift, draw_raw, draw_raw_shift,
                draw_rle, draw_rle_shift, draw_rle, draw_rle_shift,
                draw_rle, draw_rle_shift, draw_rle, draw_rle_shift,
                draw_raw_trans4, draw_raw_shift_trans4, draw_raw_trans4, draw_raw_shift_trans4,
                draw_raw_trans4, draw_raw_shift_trans4, draw_raw_trans4, draw_raw_shift_trans4,
                draw_rle_trans4, draw_rle_shift_trans4, draw_rle_trans4, draw_rle_shift_trans4,
                draw_rle_trans4, draw_rle_shift_trans4, draw_rle_trans4, draw_rle_shift_trans4
            };

    public static blitter_table_Ptr draw_raw_xflip = new blitter_table_Ptr() {
        @Override
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    public static blitter_table_Ptr draw_raw_shift_xflip = new blitter_table_Ptr() {
        @Override
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    public static blitter_table_Ptr draw_rle_xflip = new blitter_table_Ptr() {
        @Override
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    public static blitter_table_Ptr draw_rle_shift_xflip = new blitter_table_Ptr() {
        @Override
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    public static blitter_table_Ptr draw_raw_trans4_xflip = new blitter_table_Ptr() {
        @Override
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    public static blitter_table_Ptr draw_raw_shift_trans4_xflip = new blitter_table_Ptr() {
        @Override
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    public static blitter_table_Ptr draw_rle_trans4_xflip = new blitter_table_Ptr() {
        @Override
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    public static blitter_table_Ptr draw_rle_shift_trans4_xflip = new blitter_table_Ptr() {
        @Override
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    static blitter_table_Ptr blit_table4_xflip[]
            = {
                draw_raw_xflip, draw_raw_shift_xflip, draw_raw_xflip, draw_raw_shift_xflip,
                draw_raw_xflip, draw_raw_shift_xflip, draw_raw_xflip, draw_raw_shift_xflip,
                draw_rle_xflip, draw_rle_shift_xflip, draw_rle_xflip, draw_rle_shift_xflip,
                draw_rle_xflip, draw_rle_shift_xflip, draw_rle_xflip, draw_rle_shift_xflip,
                draw_raw_trans4_xflip, draw_raw_shift_trans4_xflip, draw_raw_trans4_xflip, draw_raw_shift_trans4_xflip,
                draw_raw_trans4_xflip, draw_raw_shift_trans4_xflip, draw_raw_trans4_xflip, draw_raw_shift_trans4_xflip,
                draw_rle_trans4_xflip, draw_rle_shift_trans4_xflip, draw_rle_trans4_xflip, draw_rle_shift_trans4_xflip,
                draw_rle_trans4_xflip, draw_rle_shift_trans4_xflip, draw_rle_trans4_xflip, draw_rle_shift_trans4_xflip
            };

    public static blitter_table_Ptr draw_raw_trans8 = new blitter_table_Ptr() {
        @Override
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    public static blitter_table_Ptr draw_raw_shift_trans8 = new blitter_table_Ptr() {
        @Override
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    public static blitter_table_Ptr draw_rle_trans8 = new blitter_table_Ptr() {
        @Override
        public void handler() {
            DRAW_RLE(1, draw_byte_trans8);
        }
    };
    public static blitter_table_Ptr draw_rle_shift_trans8 = new blitter_table_Ptr() {
        @Override
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };

    static blitter_table_Ptr blit_table8[]
            = {
                draw_raw, draw_raw_shift, draw_raw, draw_raw_shift,
                draw_raw, draw_raw_shift, draw_raw, draw_raw_shift,
                draw_rle, draw_rle_shift, draw_rle, draw_rle_shift,
                draw_rle, draw_rle_shift, draw_rle, draw_rle_shift,
                draw_raw_trans8, draw_raw_shift_trans8, draw_raw_trans8, draw_raw_shift_trans8,
                draw_raw_trans8, draw_raw_shift_trans8, draw_raw_trans8, draw_raw_shift_trans8,
                draw_rle_trans8, draw_rle_shift_trans8, draw_rle_trans8, draw_rle_shift_trans8,
                draw_rle_trans8, draw_rle_shift_trans8, draw_rle_trans8, draw_rle_shift_trans8
            };

    /**
     * ***********************************
     *
     * Blitter operations
     *
     ************************************
     */
    static int perform_blit() {
        /* debugging */
        if (FULL_LOGGING != 0) {
            logerror("Blit: scan=%d  src=%06x @ (%05x) for %dx%d ... flags=%02x\n",
                    cpu_getscanline(),
                    (itech8_grom_bank.read() << 16) | (u8_blitter_data[0] << 8) | u8_blitter_data[1],
                    0, u8_blitter_data[4], u8_blitter_data[5], u8_blitter_data[2]);
        }

        /* draw appropriately */
        if ((u8_blitter_data[7] & 0x40) != 0) {
            if ((u8_blitter_data[2] & BLITFLAG_XFLIP) != 0) {
                (blit_table4_xflip[u8_blitter_data[2] & 0x1f]).handler();
            } else {
                (blit_table4[u8_blitter_data[2] & 0x1f]).handler();
            }
        } else {
            (blit_table8[u8_blitter_data[2] & 0x1f]).handler();
        }

        /* return the number of bytes processed */
        return u8_blitter_data[4] * u8_blitter_data[5];
    }

    public static timer_callback blitter_done = new timer_callback() {
        public void handler(int param) {
            /* turn off blitting and generate an interrupt */
            blit_in_progress = 0;
            itech8_update_interrupts(-1, -1, 1);

            if (FULL_LOGGING != 0) {
                logerror("------------ BLIT DONE (%d) --------------\n", cpu_getscanline());
            }
        }
    };

    /**
     * ***********************************
     *
     * Blitter I/O
     *
     ************************************
     */
    public static ReadHandlerPtr itech8_blitter_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int result = u8_blitter_data[offset / 2];

            /* debugging */
            if (FULL_LOGGING != 0) {
                logerror("%04x:blitter_r(%02x)\n", cpu_getpreviouspc(), offset / 2);
            }

            /* low bit seems to be ignored */
            offset /= 2;

            /* a read from offset 3 clears the interrupt and returns the status */
            if (offset == 3) {
                itech8_update_interrupts(-1, -1, 0);
                if (blit_in_progress != 0) {
                    result |= 0x80;
                } else {
                    result &= 0x7f;
                }
            }

            return result;
        }
    };

    static FILE blitlog;
    public static WriteHandlerPtr itech8_blitter_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* low bit seems to be ignored */
            offset /= 2;
            u8_blitter_data[offset] = data & 0xFF;

            /* a write to offset 3 starts things going */
            if (offset == 3) {
                int pixels;

                /* log to the blitter file */
                if (BLIT_LOGGING != 0) {

                    if (blitlog == null) {
                        blitlog = fopen("blitter.log", "w");
                    }
                    if (blitlog != null) {
                        fprintf(blitlog, "Blit: XY=%1X%02X%02X SRC=%02X%02X%02X SIZE=%3dx%3d FLAGS=%02x",
                                tms34061_r(14 * 4 + 2, 0, 0) & 0x0f, tms34061_r(15 * 4 + 2, 0, 0), tms34061_r(15 * 4 + 0, 0, 0),
                                itech8_grom_bank.read(), u8_blitter_data[0], u8_blitter_data[1],
                                u8_blitter_data[4], u8_blitter_data[5],
                                u8_blitter_data[2]);
                    }
                    if (blitlog != null) {
                        fprintf(blitlog, "   %02X %02X %02X [%02X] %02X %02X %02X [%02X]-%02X %02X %02X %02X [%02X %02X %02X %02X]\n",
                                u8_blitter_data[0], u8_blitter_data[1],
                                u8_blitter_data[2], u8_blitter_data[3],
                                u8_blitter_data[4], u8_blitter_data[5],
                                u8_blitter_data[6], u8_blitter_data[7],
                                u8_blitter_data[8], u8_blitter_data[9],
                                u8_blitter_data[10], u8_blitter_data[11],
                                u8_blitter_data[12], u8_blitter_data[13],
                                u8_blitter_data[14], u8_blitter_data[15]);
                    }
                }

                /* perform the blit */
                pixels = perform_blit();
                blit_in_progress = 1;

                /* set a timer to go off when we're done */
                if (INSTANT_BLIT != 0) {
                    blitter_done.handler(0);
                } else {
                    timer_set((double) pixels * TIME_IN_HZ(12000000), 0, blitter_done);
                }
            }

            /* debugging */
            if (FULL_LOGGING != 0) {
                logerror("%04x:blitter_w(%02x)=%02x\n", cpu_getpreviouspc(), offset, data);
            }
        }
    };

    /**
     * ***********************************
     *
     * TMS34061 I/O
     *
     ************************************
     */
    public static WriteHandlerPtr itech8_tms34061_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int func = (offset >> 9) & 7;
            int col = offset & 0xff;

            /* Column address (CA0-CA8) is hooked up the A0-A7, with A1 being inverted
		   during register access. CA8 is ignored */
            if (func == 0 || func == 2) {
                col ^= 2;
            }

            /* Row address (RA0-RA8) is not dependent on the offset */
            tms34061_w(col, 0xff, func, data);
        }
    };

    public static ReadHandlerPtr itech8_tms34061_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int func = (offset >> 9) & 7;
            int col = offset & 0xff;

            /* Column address (CA0-CA8) is hooked up the A0-A7, with A1 being inverted
		   during register access. CA8 is ignored */
            if (func == 0 || func == 2) {
                col ^= 2;
            }

            /* Row address (RA0-RA8) is not dependent on the offset */
            return tms34061_r(col, 0xff, func);
        }
    };

    /**
     * ***********************************
     *
     * Main refresh
     *
     ************************************
     */
    public static VhUpdatePtr itech8_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int y, ty;

            /* first get the current display state */
            tms34061_get_display_state(tms_state);

            /* if we're blanked, just fill with black */
            if (tms_state.blanked != 0) {
                fillbitmap(bitmap, palette_transparent_pen, Machine.visible_area);
                return;
            }

            /* recalc the palette */
            palette_recalc();

            /* perform one of two types of blitting; I'm not sure if bit 40 in */
 /* the blitter mode register really controls this type of behavior, but */
 /* it is set consistently enough that we can use it */
 /* blit mode one: 4bpp in the TMS34061 RAM, plus 4bpp of latched data */
 /* two pages are available, at 0x00000 and 0x20000 */
 /* pages are selected via the display page register */
 /* width can be up to 512 pixels */
            if ((u8_blitter_data[7] & 0x40) != 0) {
                throw new UnsupportedOperationException("Unsupported");
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
            } /* blit mode one: 8bpp in the TMS34061 RAM */ /* two planes are available, at 0x00000 and 0x20000 */ /* both planes are rendered; with 0x20000 transparent via color 0 */ /* width can be up to 256 pixels */ else {
                UBytePtr base = new UBytePtr(tms_state.vram, tms_state.dispstart & ~0x30000);

                /* now regenerate the bitmap */
                for (ty = 0, y = Machine.visible_area.min_y; y <= Machine.visible_area.max_y; y++, ty++) {
                    draw_scanline8(bitmap, 0, y, 256, new UBytePtr(base, 0x20000 + 256 * ty), new IntArray(Machine.pens), -1);
                    draw_scanline8(bitmap, 0, y, 256, new UBytePtr(base, 0x00000 + 256 * ty), new IntArray(Machine.pens), 0);
                }
            }

            /*TODO*///		/* extra rendering for slikshot */
/*TODO*///		if (slikshot != 0)
/*TODO*///			slikshot_extra_draw(bitmap);
        }
    };
}
