/**
 * ported to v0.37b16
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.WIP.v037b16.vidhrdw;

//generic imports
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.osdependH.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
//vidhrdw imports
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.generic.*;
//to be organized
import static common.ptr.*;
import static mame037b16.mame.*;
import static mame037b16.drawgfx.*;
import static common.libc.expressions.NOT;

public class lsasquad {

    public static UBytePtr lsasquad_scrollram = new UBytePtr();

    public static VhConvertColorPromPtr lsasquad_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2, bit3;

                /* red component */
                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                bit3 = (color_prom.read(0) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
                /* green component */
                bit0 = (color_prom.read(0x400) >> 0) & 0x01;
                bit1 = (color_prom.read(0x400) >> 1) & 0x01;
                bit2 = (color_prom.read(0x400) >> 2) & 0x01;
                bit3 = (color_prom.read(0x400) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
                /* blue component */
                bit0 = (color_prom.read(2 * 0x400) >> 0) & 0x01;
                bit1 = (color_prom.read(2 * 0x400) >> 1) & 0x01;
                bit2 = (color_prom.read(2 * 0x400) >> 2) & 0x01;
                bit3 = (color_prom.read(2 * 0x400) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);

                color_prom.inc();
            }

            /* no color PROMs here, only RAM, but the gfx data is inverted so we */
 /* cannot use the default lookup table */
            for (i = 0; i < Machine.drv.color_table_len; i++) {
                colortable[i] = (char) (i ^ 0x0f);
            }
        }
    };

    static void draw_layer(osd_bitmap bitmap, UBytePtr scrollram) {
        int offs, scrollx, scrolly;

        scrollx = scrollram.read(3);
        scrolly = -scrollram.read(0);

        for (offs = 0; offs < 0x080; offs += 4) {
            int base, y, sx, sy, code, color;

            base = 64 * scrollram.read(offs + 1);
            sx = 8 * (offs / 4) + scrollx;
            if (flip_screen() != 0) {
                sx = 248 - sx;
            }
            sx &= 0xff;

            for (y = 0; y < 32; y++) {
                int attr;

                sy = 8 * y + scrolly;
                if (flip_screen() != 0) {
                    sy = 248 - sy;
                }
                sy &= 0xff;

                attr = videoram.read(base + 2 * y + 1);
                code = videoram.read(base + 2 * y) + ((attr & 0x0f) << 8);
                color = attr >> 4;

                drawgfx(bitmap, Machine.gfx[0],
                        code,
                        color,
                        flip_screen(), flip_screen(),
                        sx, sy,
                        Machine.visible_area, TRANSPARENCY_PEN, 0);
                if (sx > 248) /* wraparound */ {
                    drawgfx(bitmap, Machine.gfx[0],
                            code,
                            color,
                            flip_screen(), flip_screen(),
                            sx - 256, sy,
                            Machine.visible_area, TRANSPARENCY_PEN, 0);
                }
            }
        }
    }

    static void draw_sprites(osd_bitmap bitmap) {
        int offs;

        for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
            int sx, sy, attr, code, color, flipx, flipy;

            sx = spriteram.read(offs + 3);
            sy = 240 - spriteram.read(offs);
            attr = spriteram.read(offs + 1);
            code = spriteram.read(offs + 2) + ((attr & 0x30) << 4);
            color = attr & 0x0f;
            flipx = attr & 0x40;
            flipy = attr & 0x80;

            if (flip_screen() != 0) {
                sx = 240 - sx;
                sy = 240 - sy;
                flipx = NOT(flipx);
                flipy = NOT(flipy);
            }

            drawgfx(bitmap, Machine.gfx[1],
                    code,
                    color,
                    flipx, flipy,
                    sx, sy,
                    Machine.visible_area, TRANSPARENCY_PEN, 0);
            /* wraparound */
            drawgfx(bitmap, Machine.gfx[1],
                    code,
                    color,
                    flipx, flipy,
                    sx - 256, sy,
                    Machine.visible_area, TRANSPARENCY_PEN, 0);
        }
    }

    public static VhUpdatePtr lsasquad_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            fillbitmap(bitmap, Machine.pens[511], Machine.visible_area);

            draw_layer(bitmap, new UBytePtr(lsasquad_scrollram, 0x000));
            draw_layer(bitmap, new UBytePtr(lsasquad_scrollram, 0x080));
            draw_sprites(bitmap);
            draw_layer(bitmap, new UBytePtr(lsasquad_scrollram, 0x100));
        }
    };
}
