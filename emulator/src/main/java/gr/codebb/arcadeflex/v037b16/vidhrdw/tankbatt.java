/**
 * ported to v0.37b16
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.v037b16.vidhrdw;

//generic functions
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b16.mame.osdependH.*;
//vidhrdw imports
import static gr.codebb.arcadeflex.v037b16.vidhrdw.generic.*;
//to be organized
import static common.ptr.*;
import static mame037b16.mame.*;
import static mame037b16.drawgfx.*;

public class tankbatt {

    public static UBytePtr tankbatt_bulletsram = new UBytePtr();
    public static int[] tankbatt_bulletsram_size = new int[1];

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     **************************************************************************
     */
    public static int TOTAL_COLORS(int gfxn) {
        return (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity);
    }

    public static void COLOR(char[] colortable, int gfxn, int offs, int value) {
        colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs] = (char) value;
    }

    public static final int RES_1 = 0xc0;
    /* this is a guess */
    public static final int RES_2 = 0x3f;
    /* this is a guess */

    public static VhConvertColorPromPtr tankbatt_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            int _palette = 0;

            /* Stick black in there */
            palette[_palette++] = 0;
            palette[_palette++] = 0;
            palette[_palette++] = 0;

            /* ? Skip the first byte ? */
            color_prom.inc();

            for (i = 1; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2, bit3;

                bit0 = (color_prom.read() >> 0) & 0x01;
                /* intensity */
                bit1 = (color_prom.read() >> 1) & 0x01;
                /* red */
                bit2 = (color_prom.read() >> 2) & 0x01;
                /* green */
                bit3 = (color_prom.read() >> 3) & 0x01;
                /* blue */

 /* red component */
                palette[_palette] = (char) (RES_1 * bit1);
                if (bit1 != 0) {
                    palette[_palette] += RES_2 * bit0;
                }
                _palette++;
                /* green component */
                palette[_palette] = (char) (RES_1 * bit2);
                if (bit2 != 0) {
                    palette[_palette] += RES_2 * bit0;
                }
                _palette++;
                /* blue component */
                palette[_palette] = (char) (RES_1 * bit3);
                if (bit3 != 0) {
                    palette[_palette] += RES_2 * bit0;
                }
                _palette++;

                color_prom.inc(4);
            }

            for (i = 0; i < 128; i++) {
                colortable[i++] = 0;
                colortable[i] = (char) ((i / 2) + 1);
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
    public static VhUpdatePtr tankbatt_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy;

                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs),
                            (videoram.read(offs)) >> 2,
                            0, 0,
                            8 * sx, 8 * sy,
                            Machine.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);

            /* Draw the bullets */
            for (offs = 0; offs < tankbatt_bulletsram_size[0]; offs += 2) {
                int x, y;
                int color;

                color = 63;
                /* cyan, same color as the tanks */

                x = tankbatt_bulletsram.read(offs + 1);
                y = 255 - tankbatt_bulletsram.read(offs) - 2;

                drawgfx(bitmap, Machine.gfx[1],
                        0, /* this is just a square, generated by the hardware */
                        color,
                        0, 0,
                        x, y,
                        Machine.visible_area, TRANSPARENCY_NONE, 0);
            }

        }
    };

}
