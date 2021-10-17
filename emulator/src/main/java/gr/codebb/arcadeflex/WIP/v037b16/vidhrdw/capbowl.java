/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.WIP.v037b16.vidhrdw;

//generic functions
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//cpu imports
import static gr.codebb.arcadeflex.v037b16.cpu.m6809.m6809H.*;
//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.osdependH.*;
import static gr.codebb.arcadeflex.v037b16.mame.paletteH.*;
import static gr.codebb.arcadeflex.v037b16.mame.palette.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfx_modes8.*;
//vidhrdw imports
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.tms34061.*;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.tms34061H.*;
//to be organized
import static common.libc.cstring.memset;
import common.ptr.UBytePtr;
import common.subArrays.IntArray;
import static mame037b16.mame.*;
import static mame037b16.drawgfx.*;
import static mame037b7.palette.palette_change_color;
import static mame037b7.palette.palette_init_used_colors;

public class capbowl {

    public static UBytePtr capbowl_rowaddress=new UBytePtr();

    static UBytePtr color_usage;

    /**
     * ***********************************
     *
     * TMS34061 interfacing
     *
     ************************************
     */
    public static TmsInterruptPtr generate_interrupt = new TmsInterruptPtr() {
        public void handler(int state) {
            cpu_set_irq_line(0, M6809_FIRQ_LINE, state);
        }
    };

    static tms34061_interface tms34061intf = new tms34061_interface(
            8, /* VRAM address is (row << rowshift) | col */
            0x10000, /* size of video RAM */
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
    public static VhStartPtr capbowl_vh_start = new VhStartPtr() {
        public int handler() {
            /* initialize TMS34061 emulation */
            if (tms34061_start(tms34061intf) != 0) {
                return 1;
            }

            /* allocate memory for color tracking */
            color_usage = new UBytePtr(256 * 16);
            if (color_usage == null) {
                tms34061_stop();
                return 1;
            }
            memset(color_usage, 0, color_usage.memory.length);
            return 0;
        }
    };

    /**
     * ***********************************
     *
     * Video stop
     *
     ************************************
     */
    public static VhStopPtr capbowl_vh_stop = new VhStopPtr() {
        public void handler() {
            color_usage = null;
            tms34061_stop();
        }
    };

    /**
     * ***********************************
     *
     * TMS34061 I/O
     *
     ************************************
     */
    public static WriteHandlerPtr capbowl_tms34061_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int func = (offset >> 8) & 3;
            int col = offset & 0xff;

            /* Column address (CA0-CA8) is hooked up the A0-A7, with A1 being inverted
		   during register access. CA8 is ignored */
            if (func == 0 || func == 2) {
                col ^= 2;
            }

            /* Row address (RA0-RA8) is not dependent on the offset */
            tms34061_w(col, capbowl_rowaddress.read(), func, data);
        }
    };

    public static ReadHandlerPtr capbowl_tms34061_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int func = (offset >> 8) & 3;
            int col = offset & 0xff;

            /* Column address (CA0-CA8) is hooked up the A0-A7, with A1 being inverted
		   during register access. CA8 is ignored */
            if (func == 0 || func == 2) {
                col ^= 2;
            }

            /* Row address (RA0-RA8) is not dependent on the offset */
            return tms34061_r(col, capbowl_rowaddress.read(), func);
        }
    };

    /**
     * ***********************************
     *
     * Main refresh
     *
     ************************************
     */
    public static VhUpdatePtr capbowl_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int halfwidth = (Machine.visible_area.max_x - Machine.visible_area.min_x + 1) / 2;
            tms34061_display state = new tms34061_display();
            int x, y, palindex;

            /* first get the current display state */
            tms34061_get_display_state(state);

            /* if we're blanked, just fill with black */
            if (state.blanked != 0) {
                fillbitmap(bitmap, palette_transparent_pen, Machine.visible_area);
                return;
            }

            /* update the palette and color usage */
            for (y = Machine.visible_area.min_y; y <= Machine.visible_area.max_y; y++) {
                if (state.dirty[y] != 0) {
                    UBytePtr src = new UBytePtr(state.vram, 256 * y);
                    UBytePtr usage = new UBytePtr(color_usage, 16 * y);

                    /* update the palette */
                    for (x = 0; x < 16; x++) {
                        int r = src.readinc() & 0x0f;
                        int g = src.read() >> 4;
                        int b = src.readinc() & 0x0f;

                        palette_change_color(y * 16 + x, (r << 4) | r, (g << 4) | g, (b << 4) | b);
                    }

                    /* recount the colors */
                    memset(usage, 0, 16);
                    for (x = 0; x < halfwidth; x++) {
                        int pix = src.readinc();
                        usage.write(pix >> 4, 1);
                        usage.write(pix & 0x0f, 1);
                    }
                }
            }

            /* reset the usage */
            palette_init_used_colors();
            palindex = Machine.visible_area.min_y * 16;

            /* mark used colors */
            for (y = Machine.visible_area.min_y; y <= Machine.visible_area.max_y; y++) {
                for (x = 0; x < 16; x++, palindex++) {
                    if (color_usage.read(palindex) != 0) {
                        palette_used_colors.write(palindex, PALETTE_COLOR_USED);
                    }
                }
            }

            /* recalc */
            if (palette_recalc() != null) {
                full_refresh = 1;
            }

            /* now regenerate the bitmap */
            for (y = Machine.visible_area.min_y; y <= Machine.visible_area.max_y; y++) {
                if (full_refresh != 0 || state.dirty[y] != 0) {
                    UBytePtr src = new UBytePtr(state.vram, 256 * y + 32);
                    char[] scanline = new char[400];
                    UBytePtr dst = new UBytePtr(scanline);

                    /* expand row to 8bpp */
                    for (x = 0; x < halfwidth; x++) {
                        int pix = src.readinc();
                        dst.writeinc(pix >> 4);
                        dst.writeinc(pix & 0x0f);
                    }

                    /* redraw the scanline and mark it no longer dirty */
                    draw_scanline8(bitmap, Machine.visible_area.min_x, y, halfwidth * 2, new UBytePtr(scanline), new IntArray(Machine.pens, 16 * y), -1);
                    state.dirty[y] = 0;
                }
            }
        }
    };
}
