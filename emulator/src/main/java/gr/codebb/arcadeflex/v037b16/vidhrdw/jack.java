/**
 * ported to v0.37b16
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.v037b16.vidhrdw;

//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b16.mame.osdependH.*;
//vidhrdw imports
import static gr.codebb.arcadeflex.v037b16.vidhrdw.generic.*;
//to be organized
import static arcadeflex037b16.fucPtr.*;
import static mame037b16.mame.*;
import static mame037b16.drawgfx.*;
import static common.libc.cstring.*;
import static common.libc.expressions.NOT;
import static mame037b7.palette.palette_recalc;
import static mame037b7.palette.paletteram_BBGGGRRR_w;

public class jack {

    public static WriteHandlerPtr jack_paletteram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* RGB output is inverted */
            paletteram_BBGGGRRR_w.handler(offset, ~data);
        }
    };

    public static ReadHandlerPtr jack_flipscreen_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            flip_screen_set(offset);
            return 0;
        }
    };

    public static WriteHandlerPtr jack_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            flip_screen_set(offset);
        }
    };

    public static VhUpdatePtr jack_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            if (palette_recalc() != null || full_refresh != 0) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy;

                    dirtybuffer[offs] = 0;

                    sx = offs / 32;
                    sy = 31 - offs % 32;

                    if (flip_screen() != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs) + ((colorram.read(offs) & 0x18) << 5),
                            colorram.read(offs) & 0x07,
                            flip_screen(), flip_screen(),
                            8 * sx, 8 * sy,
                            Machine.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);

            /* draw sprites */
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                int sx, sy, num, color, flipx, flipy;

                sx = spriteram.read(offs + 1);
                sy = spriteram.read(offs);
                num = spriteram.read(offs + 2) + ((spriteram.read(offs + 3) & 0x08) << 5);
                color = spriteram.read(offs + 3) & 0x07;
                flipx = (spriteram.read(offs + 3) & 0x80);
                flipy = (spriteram.read(offs + 3) & 0x40);

                if (flip_screen() != 0) {
                    sx = 248 - sx;
                    sy = 248 - sy;
                    flipx = NOT(flipx);
                    flipy = NOT(flipy);
                }

                drawgfx(bitmap, Machine.gfx[0],
                        num,
                        color,
                        flipx, flipy,
                        sx, sy,
                        Machine.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };
}
