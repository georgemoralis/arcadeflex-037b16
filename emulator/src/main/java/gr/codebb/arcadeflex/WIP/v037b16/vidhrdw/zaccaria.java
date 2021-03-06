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

public class zaccaria {

    public static UBytePtr zaccaria_attributesram = new UBytePtr();

    static rectangle spritevisiblearea = new rectangle(
            2 * 8 + 1, 29 * 8 - 1,
            2 * 8, 30 * 8 - 1
    );

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     *
     * Here's the hookup from the proms (82s131) to the r-g-b-outputs
     *
     * Prom 9F 74LS374 ----------- ____________ 12 | 3 2 |---680 ohm----| blue
     * out 11 | 4 5 |---1k ohm-----| 10 | 7 6 |---820 ohm-------| 9 | 8 9 |---1k
     * ohm--------| green out Prom 9G | | | 12 | 13 12 |---1.2k ohm------| 11 |
     * 14 15 |---820 ohm----------| 10 | 17 16 |---1k ohm-----------| red out 9
     * | 18 19 |---1.2k ohm---------| |__________|
     *
     *
     **************************************************************************
     */
    public static VhConvertColorPromPtr zaccaria_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i, j, k;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])

            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2;

                /* I'm not sure, but I think that pen 0 must always be black, otherwise */
 /* there's some junk brown background in Jack Rabbit */
                if (((i % 64) / 8) == 0) {
                    palette[p_inc++] = ((char) (0));
                    palette[p_inc++] = ((char) (0));
                    palette[p_inc++] = ((char) (0));
                } else {
                    /* red component */
                    bit0 = (color_prom.read(0) >> 3) & 0x01;
                    bit1 = (color_prom.read(0) >> 2) & 0x01;
                    bit2 = (color_prom.read(0) >> 1) & 0x01;
                    palette[p_inc++] = ((char) (0x46 * bit0 + 0x53 * bit1 + 0x66 * bit2));
                    /* green component */
                    bit0 = (color_prom.read(0) >> 0) & 0x01;
                    bit1 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
                    bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
                    palette[p_inc++] = ((char) (0x46 * bit0 + 0x53 * bit1 + 0x66 * bit2));
                    /* blue component */
                    bit0 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
                    bit1 = (color_prom.read(Machine.drv.total_colors) >> 0) & 0x01;
                    palette[p_inc++] = ((char) (0x53 * bit0 + 0x7b * bit1));
                }

                color_prom.inc();
            }

            /* There are 512 unique colors, which seem to be organized in 8 blocks */
 /* of 64. In each block, colors are not in the usual sequential order */
 /* but in interleaved order, like Phoenix. Additionally, colors for */
 /* background and sprites are interleaved. */
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 4; j++) {
                    for (k = 0; k < 8; k++) {
                        /* swap j and k to make the colors sequential */
                        colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + 32 * i + 8 * j + k] = (char) (64 * i + 8 * k + 2 * j);
                    }
                }
            }
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 4; j++) {
                    for (k = 0; k < 8; k++) {
                        /* swap j and k to make the colors sequential */
                        colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + 32 * i + 8 * j + k] = (char) (64 * i + 8 * k + 2 * j + 1);
                    }
                }
            }
        }
    };

    public static WriteHandlerPtr zaccaria_attributes_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((offset & 1) != 0 && zaccaria_attributesram.read(offset) != data) {
                int i;

                for (i = offset / 2; i < videoram_size[0]; i += 32) {
                    dirtybuffer[i] = 1;
                }
            }

            zaccaria_attributesram.write(offset, data);
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
    public static VhUpdatePtr zaccaria_vh_screenrefresh = new VhUpdatePtr() {
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
                            videoram.read(offs) + ((colorram.read(offs) & 0x03) << 8),
                            4 * (zaccaria_attributesram.read(2 * (offs % 32) + 1) & 0x07)
                            + ((colorram.read(offs) & 0x0c) >> 2),
                            0, 0,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int[] scroll = new int[32];

                for (offs = 0; offs < 32; offs++) {
                    scroll[offs] = -zaccaria_attributesram.read(2 * offs);
                }

                copyscrollbitmap(bitmap, tmpbitmap, 0, null, 32, scroll, Machine.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* draw sprites */
 /* TODO: sprites have 32 color codes, but we are using only 8. In Jack */
 /* Rabbit the extra codes are all duplicates, but there is a quadruple */
 /* of codes in Money Money which contains two different combinations. */
 /* TODO: sprite placement is not perfect, I made the Jack Rabbit mouth */
 /* animation correct but this moves one pixel to the left the sprite */
 /* which masks the holes when you fall in them. The hardware is probably */
 /* similar to Amidar, but the code in the Amidar driver is not good either. */
            for (offs = 0; offs < spriteram_2_size[0]; offs += 4) {
                drawgfx(bitmap, Machine.gfx[1],
                        (spriteram_2.read(offs + 2) & 0x3f) + (spriteram_2.read(offs + 1) & 0xc0),
                        4 * (spriteram_2.read(offs + 1) & 0x07),
                        spriteram_2.read(offs + 2) & 0x40, spriteram_2.read(offs + 2) & 0x80,
                        spriteram_2.read(offs + 3) + 1, 242 - spriteram_2.read(offs),
                        spritevisiblearea, TRANSPARENCY_PEN, 0);
            }

            for (offs = 0; offs < spriteram_size[0]; offs += 4) {
                drawgfx(bitmap, Machine.gfx[1],
                        (spriteram.read(offs + 1) & 0x3f) + (spriteram.read(offs + 2) & 0xc0),
                        4 * (spriteram.read(offs + 2) & 0x07),
                        spriteram.read(offs + 1) & 0x40, spriteram.read(offs + 1) & 0x80,
                        spriteram.read(offs + 3) + 1, 242 - spriteram.read(offs),
                        spritevisiblearea, TRANSPARENCY_PEN, 0);
            }
        }
    };
}
