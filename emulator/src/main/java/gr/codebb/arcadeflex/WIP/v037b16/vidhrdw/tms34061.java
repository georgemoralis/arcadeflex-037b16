/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.WIP.v037b16.vidhrdw;

//generic functions
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v056.mame.timer.*;
//vidhrdw imports
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.tms34061H.*;
//to be organized
import static arcadeflex036.osdepend.logerror;
import static common.libc.cstring.memcpy;
import static common.libc.cstring.memset;
import common.ptr.UBytePtr;

public class tms34061 {

    public static class tms34061_data {

        char[] regs = new char[TMS34061_REGCOUNT];
        char xmask;
        int /*UINT8*/ u8_yshift;
        int/*UINT32*/ vrammask;
        UBytePtr vram;
        UBytePtr latchram;
        int/*UINT8*/ u8_latchdata;
        UBytePtr shiftreg;
        char[] dirty;
        int /*UINT8*/ u8_dirtyshift;
        Object timer;
        tms34061_interface intf;
    }

    /**
     * ***********************************
     *
     * Global variables
     *
     ************************************
     */
    static tms34061_data _tms34061 = new tms34061_data();

    /**
     * ***********************************
     *
     * Hardware startup
     *
     ************************************
     */
    public static int tms34061_start(tms34061_interface _interface) {
        int temp;

        /* reset the data */
        //memset(&_tms34061, 0, sizeof(_tms34061));
        _tms34061.intf = _interface;
        _tms34061.vrammask = _tms34061.intf.vramsize - 1;

        /* compute the dirty shift */
        temp = _tms34061.intf.dirtychunk;
        while ((temp & 1) == 0) {
            _tms34061.u8_dirtyshift = (_tms34061.u8_dirtyshift + 1) & 0xFF;
            temp >>= 1;
        }

        /* allocate memory for VRAM */
        _tms34061.vram = new UBytePtr(_tms34061.intf.vramsize + 256 * 2);
        if (_tms34061.vram == null) {
            return 1;
        }
        memset(_tms34061.vram, 0, _tms34061.intf.vramsize + 256 * 2);

        /* allocate memory for latch RAM */
        _tms34061.latchram = new UBytePtr(_tms34061.intf.vramsize + 256 * 2);
        if (_tms34061.latchram == null) {
            _tms34061.vram = null;
            return 1;
        }
        memset(_tms34061.latchram, 0, _tms34061.intf.vramsize + 256 * 2);

        /* allocate memory for dirty rows */
        _tms34061.dirty = new char[1 << (20 - _tms34061.u8_dirtyshift)];
        if (_tms34061.dirty == null) {
            _tms34061.latchram = null;
            _tms34061.vram = null;
            return 1;
        }
        memset(_tms34061.dirty, 1, 1 << (20 - _tms34061.u8_dirtyshift));

        /* add some buffer space for VRAM and latch RAM */
        _tms34061.vram.inc(256);
        _tms34061.latchram.inc(256);

        /* point the shift register to the base of VRAM for now */
        _tms34061.shiftreg = _tms34061.vram;

        /* initialize registers to their default values from the manual */
        _tms34061.regs[TMS34061_HORENDSYNC] = 0x0010;
        _tms34061.regs[TMS34061_HORENDBLNK] = 0x0020;
        _tms34061.regs[TMS34061_HORSTARTBLNK] = 0x01f0;
        _tms34061.regs[TMS34061_HORTOTAL] = 0x0200;
        _tms34061.regs[TMS34061_VERENDSYNC] = 0x0004;
        _tms34061.regs[TMS34061_VERENDBLNK] = 0x0010;
        _tms34061.regs[TMS34061_VERSTARTBLNK] = 0x00f0;
        _tms34061.regs[TMS34061_VERTOTAL] = 0x0100;
        _tms34061.regs[TMS34061_DISPUPDATE] = 0x0000;
        _tms34061.regs[TMS34061_DISPSTART] = 0x0000;
        _tms34061.regs[TMS34061_VERINT] = 0x0000;
        _tms34061.regs[TMS34061_CONTROL1] = 0x7000;
        _tms34061.regs[TMS34061_CONTROL2] = 0x0600;
        _tms34061.regs[TMS34061_STATUS] = 0x0000;
        _tms34061.regs[TMS34061_XYOFFSET] = 0x0010;
        _tms34061.regs[TMS34061_XYADDRESS] = 0x0000;
        _tms34061.regs[TMS34061_DISPADDRESS] = 0x0000;
        _tms34061.regs[TMS34061_VERCOUNTER] = 0x0000;

        /* start vertical interrupt timer */
        _tms34061.timer = null;
        return 0;
    }

    /**
     * ***********************************
     *
     * Hardware shutdown
     *
     ************************************
     */
    public static void tms34061_stop() {
        /* remove buffer space for VRAM and latch RAM */
        _tms34061.vram.dec(256);
        _tms34061.latchram.dec(256);

        _tms34061.dirty = null;
        _tms34061.latchram = null;
        _tms34061.vram = null;
    }

    /**
     * ***********************************
     *
     * Interrupt handling
     *
     ************************************
     */
    static void update_interrupts() {
        /* if we have a callback, process it */
        if (_tms34061.intf.interrupt != null) {
            /* if the status bit is set, and ints are enabled, turn it on */
            if ((_tms34061.regs[TMS34061_STATUS] & 0x0001) != 0 && (_tms34061.regs[TMS34061_CONTROL1] & 0x0400) != 0) {
                (_tms34061.intf.interrupt).handler(ASSERT_LINE);
            } else {
                (_tms34061.intf.interrupt).handler(CLEAR_LINE);
            }
        }
    }

    public static timer_callback tms34061_interrupt = new timer_callback() {
        public void handler(int param) {
            /* set timer for next frame */
            _tms34061.timer = timer_set(cpu_getscanlinetime(_tms34061.regs[TMS34061_VERINT]), 0, tms34061_interrupt);

            /* set the interrupt bit in the status reg */
            _tms34061.regs[TMS34061_STATUS] |= 1;

            /* update the interrupt state */
            update_interrupts();
        }
    };

    /**
     * ***********************************
     *
     * Register writes
     *
     ************************************
     */
    public static WriteHandlerPtr register_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int regnum = offset >> 2;
            char oldval = _tms34061.regs[regnum];

            /* store the hi/lo half */
            if ((offset & 0x02) != 0) {
                _tms34061.regs[regnum] = (char) ((_tms34061.regs[regnum] & 0x00ff) | (data << 8));
            } else {
                _tms34061.regs[regnum] = (char) ((_tms34061.regs[regnum] & 0xff00) | data);
            }

            /* update the state of things */
            switch (regnum) {
                /* vertical interrupt: adjust the timer */
                case TMS34061_VERINT:
                    if (_tms34061.timer != null) {
                        timer_remove(_tms34061.timer);
                    }
                    _tms34061.timer = timer_set(cpu_getscanlinetime(_tms34061.regs[TMS34061_VERINT]), 0, tms34061_interrupt);
                    break;

                /* XY offset: set the X and Y masks */
                case TMS34061_XYOFFSET:
                    switch (_tms34061.regs[TMS34061_XYOFFSET] & 0x00ff) {
                        case 0x01:
                            _tms34061.u8_yshift = 2;
                            break;
                        case 0x02:
                            _tms34061.u8_yshift = 3;
                            break;
                        case 0x04:
                            _tms34061.u8_yshift = 4;
                            break;
                        case 0x08:
                            _tms34061.u8_yshift = 5;
                            break;
                        case 0x10:
                            _tms34061.u8_yshift = 6;
                            break;
                        case 0x20:
                            _tms34061.u8_yshift = 7;
                            break;
                        case 0x40:
                            _tms34061.u8_yshift = 8;
                            break;
                        case 0x80:
                            _tms34061.u8_yshift = 9;
                            break;
                        default:
                            logerror("Invalid value for XYOFFSET = %04x\n", _tms34061.regs[TMS34061_XYOFFSET]);
                            break;
                    }
                    _tms34061.xmask = (char) ((1 << _tms34061.u8_yshift) - 1);
                    break;

                /* CONTROL1: they could have turned interrupts on */
                case TMS34061_CONTROL1:
                    update_interrupts();
                    break;

                /* CONTROL2: they could have blanked the display */
                case TMS34061_CONTROL2:
                    if (((oldval ^ _tms34061.regs[TMS34061_CONTROL2]) & 0x2000) != 0) {
                        memset(_tms34061.dirty, 1, 1 << (20 - _tms34061.u8_dirtyshift));
                    }
                    break;

                /* other supported registers */
                case TMS34061_XYADDRESS:
                    break;

                /* report all others */
                default:
                    logerror("Unsupported tms34061 write. Reg #%02X=%04X - PC: %04X\n",
                            regnum, _tms34061.regs[regnum], cpu_getpreviouspc());
                    break;
            }
        }
    };

    /**
     * ***********************************
     *
     * Register reads
     *
     ************************************
     */
    public static ReadHandlerPtr register_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int regnum = offset >> 2;
            int u8_result;

            /* extract the correct portion of the register */
            if ((offset & 0x02) != 0) {
                u8_result = (_tms34061.regs[regnum] >> 8) & 0xFF;
            } else {
                u8_result = _tms34061.regs[regnum] & 0xFF;
            }

            /* special cases: */
            switch (regnum) {
                /* status register: a read here clears it */
                case TMS34061_STATUS:
                    _tms34061.regs[TMS34061_STATUS] = 0;
                    update_interrupts();
                    break;

                /* vertical count register: return the current scanline */
                case TMS34061_VERCOUNTER:
                    if ((offset & 0x02) != 0) {
                        u8_result = (cpu_getscanline() >> 8) & 0xFF;
                    } else {
                        u8_result = cpu_getscanline() & 0xFF;
                    }
                    break;

                /* report all others */
                default:
                    logerror("Unsupported tms34061 read.  Reg #%02X      - PC: %04X\n",
                            regnum, cpu_getpreviouspc());
                    break;
            }
            return u8_result & 0xFF;
        }
    };

    /**
     * ***********************************
     *
     * XY addressing
     *
     ************************************
     */
    public static void adjust_xyaddress(int offset) {
        /* note that carries are allowed if the Y coordinate isn't being modified */
        switch (offset & 0x1e) {
            case 0x00:
                /* no change */
                break;

            case 0x02:
                /* X + 1 */
                _tms34061.regs[TMS34061_XYADDRESS]++;
                break;

            case 0x04:
                /* X - 1 */
                _tms34061.regs[TMS34061_XYADDRESS]--;
                break;

            case 0x06:
                /* X = 0 */
                _tms34061.regs[TMS34061_XYADDRESS] &= ~_tms34061.xmask;
                break;

            case 0x08:
                /* Y + 1 */
                _tms34061.regs[TMS34061_XYADDRESS] += 1 << _tms34061.u8_yshift;
                break;

            case 0x0a:
                /* X + 1, Y + 1 */
                _tms34061.regs[TMS34061_XYADDRESS] = (char) ((_tms34061.regs[TMS34061_XYADDRESS] & ~_tms34061.xmask)
                        | ((_tms34061.regs[TMS34061_XYADDRESS] + 1) & _tms34061.xmask));
                _tms34061.regs[TMS34061_XYADDRESS] += 1 << _tms34061.u8_yshift;
                break;

            case 0x0c:
                /* X - 1, Y + 1 */
                _tms34061.regs[TMS34061_XYADDRESS] = (char) ((_tms34061.regs[TMS34061_XYADDRESS] & ~_tms34061.xmask)
                        | ((_tms34061.regs[TMS34061_XYADDRESS] - 1) & _tms34061.xmask));
                _tms34061.regs[TMS34061_XYADDRESS] += 1 << _tms34061.u8_yshift;
                break;

            case 0x0e:
                /* X = 0, Y + 1 */
                _tms34061.regs[TMS34061_XYADDRESS] &= ~_tms34061.xmask;
                _tms34061.regs[TMS34061_XYADDRESS] += 1 << _tms34061.u8_yshift;
                break;

            case 0x10:
                /* Y - 1 */
                _tms34061.regs[TMS34061_XYADDRESS] -= 1 << _tms34061.u8_yshift;
                break;

            case 0x12:
                /* X + 1, Y - 1 */
                _tms34061.regs[TMS34061_XYADDRESS] = (char) ((_tms34061.regs[TMS34061_XYADDRESS] & ~_tms34061.xmask)
                        | ((_tms34061.regs[TMS34061_XYADDRESS] + 1) & _tms34061.xmask));
                _tms34061.regs[TMS34061_XYADDRESS] -= 1 << _tms34061.u8_yshift;
                break;

            case 0x14:
                /* X - 1, Y - 1 */
                _tms34061.regs[TMS34061_XYADDRESS] = (char) ((_tms34061.regs[TMS34061_XYADDRESS] & ~_tms34061.xmask)
                        | ((_tms34061.regs[TMS34061_XYADDRESS] - 1) & _tms34061.xmask));
                _tms34061.regs[TMS34061_XYADDRESS] -= 1 << _tms34061.u8_yshift;
                break;

            case 0x16:
                /* X = 0, Y - 1 */
                _tms34061.regs[TMS34061_XYADDRESS] &= ~_tms34061.xmask;
                _tms34061.regs[TMS34061_XYADDRESS] -= 1 << _tms34061.u8_yshift;
                break;

            case 0x18:
                /* Y = 0 */
                _tms34061.regs[TMS34061_XYADDRESS] &= _tms34061.xmask;
                break;

            case 0x1a:
                /* X + 1, Y = 0 */
                _tms34061.regs[TMS34061_XYADDRESS]++;
                _tms34061.regs[TMS34061_XYADDRESS] &= _tms34061.xmask;
                break;

            case 0x1c:
                /* X - 1, Y = 0 */
                _tms34061.regs[TMS34061_XYADDRESS]--;
                _tms34061.regs[TMS34061_XYADDRESS] &= _tms34061.xmask;
                break;

            case 0x1e:
                /* X = 0, Y = 0 */
                _tms34061.regs[TMS34061_XYADDRESS] = 0;
                break;
        }
    }

    public static WriteHandlerPtr xypixel_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* determine the offset, then adjust it */
            int pixeloffs = _tms34061.regs[TMS34061_XYADDRESS];
            if (offset != 0) {
                adjust_xyaddress(offset);
            }

            /* adjust for the upper bits */
            pixeloffs |= (_tms34061.regs[TMS34061_XYOFFSET] & 0x0f00) << 8;

            /* mask to the VRAM size */
            pixeloffs &= _tms34061.vrammask;

            /* set the pixel data */
            if (_tms34061.vram.read(pixeloffs) != data || _tms34061.latchram.read(pixeloffs) != _tms34061.u8_latchdata) {
                _tms34061.vram.write(pixeloffs, data);
                _tms34061.latchram.write(pixeloffs, _tms34061.u8_latchdata);
                _tms34061.dirty[pixeloffs >> _tms34061.u8_dirtyshift] = 1;
            }
        }
    };

    public static ReadHandlerPtr xypixel_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* determine the offset, then adjust it */
            int pixeloffs = _tms34061.regs[TMS34061_XYADDRESS];
            if (offset != 0) {
                adjust_xyaddress(offset);
            }

            /* adjust for the upper bits */
            pixeloffs |= (_tms34061.regs[TMS34061_XYOFFSET] & 0x0f00) << 8;

            /* mask to the VRAM size */
            pixeloffs &= _tms34061.vrammask;

            /* return the result */
            return _tms34061.vram.read(pixeloffs);
        }
    };

    /**
     * ***********************************
     *
     * Core writes
     *
     ************************************
     */
    public static void tms34061_w(int col, int row, int func, int u8_data) {
        int offs;

        /* the function code determines what to do */
        switch (func) {
            /* both 0 and 2 map to register access */
            case 0:
            case 2:
                register_w.handler(col, u8_data);
                break;

            /* function 1 maps to XY access; col is the address adjustment */
            case 1:
                xypixel_w.handler(col, u8_data);
                break;

            /* function 3 maps to direct access */
            case 3:
                offs = ((row << _tms34061.intf.u8_rowshift) | col) & _tms34061.vrammask;
                if (_tms34061.vram.read(offs) != u8_data || _tms34061.latchram.read(offs) != _tms34061.u8_latchdata) {
                    _tms34061.vram.write(offs, u8_data);
                    _tms34061.latchram.write(offs, _tms34061.u8_latchdata);
                    _tms34061.dirty[offs >> _tms34061.u8_dirtyshift] = 1;
                }
                break;

            /* function 4 performs a shift reg transfer to VRAM */
            case 4:
                offs = col << _tms34061.intf.u8_rowshift;
                if ((_tms34061.regs[TMS34061_CONTROL2] & 0x0040) != 0) {
                    offs |= (_tms34061.regs[TMS34061_CONTROL2] & 3) << 16;
                }
                offs &= _tms34061.vrammask;

                memcpy(_tms34061.vram, offs, _tms34061.shiftreg, 1 << _tms34061.intf.u8_rowshift);
                memset(_tms34061.latchram, offs, _tms34061.u8_latchdata, 1 << _tms34061.intf.u8_rowshift);
                _tms34061.dirty[offs >> _tms34061.u8_dirtyshift] = 1;
                break;

            /* function 5 performs a shift reg transfer from VRAM */
            case 5:
                offs = col << _tms34061.intf.u8_rowshift;
                if ((_tms34061.regs[TMS34061_CONTROL2] & 0x0040) != 0) {
                    offs |= (_tms34061.regs[TMS34061_CONTROL2] & 3) << 16;
                }
                offs &= _tms34061.vrammask;

                _tms34061.shiftreg = new UBytePtr(_tms34061.vram, offs);
                break;

            /* log anything else */
            default:
                logerror("Unsupported TMS34061 function %d - PC: %04X\n",
                        func, cpu_get_pc());
                break;
        }
    }

    public static int tms34061_r(int col, int row, int func) {
        int result = 0;
        int offs;

        /* the function code determines what to do */
        switch (func) {
            /* both 0 and 2 map to register access */
            case 0:
            case 2:
                result = register_r.handler(col);
                break;

            /* function 1 maps to XY access; col is the address adjustment */
            case 1:
                result = xypixel_r.handler(col);
                break;

            /* funtion 3 maps to direct access */
            case 3:
                offs = ((row << _tms34061.intf.u8_rowshift) | col) & _tms34061.vrammask;
                result = _tms34061.vram.read(offs);
                break;

            /* function 4 performs a shift reg transfer to VRAM */
            case 4:
                offs = col << _tms34061.intf.u8_rowshift;
                if ((_tms34061.regs[TMS34061_CONTROL2] & 0x0040) != 0) {
                    offs |= (_tms34061.regs[TMS34061_CONTROL2] & 3) << 16;
                }
                offs &= _tms34061.vrammask;

                memcpy(_tms34061.vram, offs, _tms34061.shiftreg, 1 << _tms34061.intf.u8_rowshift);
                memset(_tms34061.latchram, offs, _tms34061.u8_latchdata, 1 << _tms34061.intf.u8_rowshift);
                _tms34061.dirty[offs >> _tms34061.u8_dirtyshift] = 1;
                break;

            /* function 5 performs a shift reg transfer from VRAM */
            case 5:
                offs = col << _tms34061.intf.u8_rowshift;
                if ((_tms34061.regs[TMS34061_CONTROL2] & 0x0040) != 0) {
                    offs |= (_tms34061.regs[TMS34061_CONTROL2] & 3) << 16;
                }
                offs &= _tms34061.vrammask;

                _tms34061.shiftreg = new UBytePtr(_tms34061.vram, offs);
                break;

            /* log anything else */
            default:
                logerror("Unsupported TMS34061 function %d - PC: %04X\n",
                        func, cpu_get_pc());
                break;
        }

        return result & 0xFF;
    }

    /**
     * ***********************************
     *
     * Misc functions
     *
     ************************************
     */
    public static ReadHandlerPtr tms34061_latch_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return _tms34061.u8_latchdata & 0xFF;
        }
    };

    public static WriteHandlerPtr tms34061_latch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            _tms34061.u8_latchdata = data & 0xFF;
        }
    };

    public static void tms34061_get_display_state(tms34061_display state) {
        state.blanked = (~_tms34061.regs[TMS34061_CONTROL2] >> 13) & 1;
        state.vram = _tms34061.vram;
        state.latchram = _tms34061.latchram;
        state.dirty = _tms34061.dirty;
        state.regs = _tms34061.regs;

        /* compute the display start */
        state.dispstart = _tms34061.regs[TMS34061_DISPSTART];

        /* if B6 of control reg 2 is set, upper bits of display start come from B0-B1 */
        if ((_tms34061.regs[TMS34061_CONTROL2] & 0x0040) != 0) {
            state.dispstart |= (_tms34061.regs[TMS34061_CONTROL2] & 3) << 16;
        }

        /* mask to actual VRAM size */
        state.dispstart &= _tms34061.vrammask;
    }
}
