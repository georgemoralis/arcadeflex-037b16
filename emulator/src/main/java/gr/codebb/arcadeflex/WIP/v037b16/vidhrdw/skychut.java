/**
 * ported to v0.37b16
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.WIP.v037b16.vidhrdw;

//generic functions
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b16.mame.osdependH.*;
//vidhrdw imports
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.generic.*;
//to be organized
import static common.ptr.*;
import static mame037b16.mame.*;
import static mame037b16.drawgfx.*;
import static common.libc.cstring.memset;

public class skychut {

    static int flipscreen;

    public static WriteHandlerPtr skychut_vh_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*	if (flipscreen != (data & 0x8f))
		{
			flipscreen = (data & 0x8f);
			memset(dirtybuffer,1,videoram_size[0]);
		}
             */
        }
    };

    public static WriteHandlerPtr skychut_colorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (colorram.read(offset) != data) {
                dirtybuffer[offset] = 1;

                colorram.write(offset, data);
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     *
     **************************************************************************
     */
    public static VhUpdatePtr skychut_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;
            if (full_refresh != 0) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy;

                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;

                    drawgfx(bitmap, Machine.gfx[0],
                            videoram.read(offs),
                            colorram.read(offs),
                            flipscreen, flipscreen,
                            8 * sx, 8 * sy,
                            Machine.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

        }
    };

    public static UBytePtr iremm15_chargen = new UBytePtr();

    static void iremm15_drawgfx(osd_bitmap bitmap, int ch, int color, int back, int x, int y) {
        int mask;
        int i;

        for (i = 0; i < 8; i++, x++) {
            mask = iremm15_chargen.read(ch * 8 + i);
            plot_pixel.handler(bitmap, x, y + 7, (mask & 0x80) != 0 ? color : back);
            plot_pixel.handler(bitmap, x, y + 6, (mask & 0x40) != 0 ? color : back);
            plot_pixel.handler(bitmap, x, y + 5, (mask & 0x20) != 0 ? color : back);
            plot_pixel.handler(bitmap, x, y + 4, (mask & 0x10) != 0 ? color : back);
            plot_pixel.handler(bitmap, x, y + 3, (mask & 8) != 0 ? color : back);
            plot_pixel.handler(bitmap, x, y + 2, (mask & 4) != 0 ? color : back);
            plot_pixel.handler(bitmap, x, y + 1, (mask & 2) != 0 ? color : back);
            plot_pixel.handler(bitmap, x, y, (mask & 1) != 0 ? color : back);
        }
    }

    /**
     * *************************************************************************
     *
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     *
     **************************************************************************
     */
    public static VhUpdatePtr iremm15_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;
            if (full_refresh != 0) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy;

                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;

                    iremm15_drawgfx(bitmap,
                            videoram.read(offs),
                            Machine.pens[colorram.read(offs)],
                            Machine.pens[7], // space beam not color 0
                            8 * sx, 8 * sy);
                }
            }

        }
    };

}
