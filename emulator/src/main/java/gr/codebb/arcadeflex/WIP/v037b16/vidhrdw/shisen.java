/**
 * ported to v0.37b16
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.WIP.v037b16.vidhrdw;

//generic functions
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//mame imports
import static arcadeflex036.osdepend.logerror;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b16.mame.osdependH.*;
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b16.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b16.mame.palette.*;
import static gr.codebb.arcadeflex.v037b16.mame.palette.*;
//vidhrdw imports
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.generic.*;
//to be organized
import static common.libc.cstring.memset;
import static common.ptr.*;
import static mame037b16.mame.*;
import static mame037b16.drawgfx.*;
import static mame037b7.palette.palette_change_color;

public class shisen {

    static int gfxbank;

    public static WriteHandlerPtr sichuan2_bankswitch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bankaddress;
            UBytePtr RAM = memory_region(REGION_CPU1);

            if ((data & 0xc0) != 0) {
                logerror("bank switch %02x\n", data);
            }

            /* bits 0-2 select ROM bank */
            bankaddress = 0x10000 + (data & 0x07) * 0x4000;
            cpu_setbank(1, new UBytePtr(RAM, bankaddress));

            /* bits 3-5 select gfx bank */
            if (gfxbank != ((data & 0x38) >> 3)) {
                gfxbank = (data & 0x38) >> 3;
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /* bits 6-7 unknown */
        }
    };

    public static WriteHandlerPtr sichuan2_paletteram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int r, g, b;

            paletteram.write(offset, data);

            offset &= 0xff;

            r = paletteram.read(offset + 0x000) & 0x1f;
            g = paletteram.read(offset + 0x100) & 0x1f;
            b = paletteram.read(offset + 0x200) & 0x1f;
            r = (r << 3) | (r >> 2);
            g = (g << 3) | (g >> 2);
            b = (b << 3) | (b >> 2);

            palette_change_color(offset, r, g, b);
        }
    };

    public static VhUpdatePtr sichuan2_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 2; offs >= 0; offs -= 2) {
                if (dirtybuffer[offs] != 0 || dirtybuffer[offs + 1] != 0) {
                    int sx, sy;

                    dirtybuffer[offs] = dirtybuffer[offs + 1] = 0;

                    sx = (offs / 2) % 64;
                    sy = (offs / 2) / 64;

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs) + ((videoram.read(offs + 1) & 0x0f) << 8) + (gfxbank << 12),
                            (videoram.read(offs + 1) & 0xf0) >> 4,
                            0, 0,
                            8 * sx, 8 * sy,
                            Machine.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the character mapped graphics */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);
        }
    };
}
