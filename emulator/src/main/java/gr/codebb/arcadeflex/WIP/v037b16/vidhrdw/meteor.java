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

public class meteor {

    public static UBytePtr meteor_scrollram = new UBytePtr();

    /**
     * *************************************************************************
     *
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     *
     **************************************************************************
     */
    public static VhUpdatePtr meteor_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* draw the characters as sprites because they could be overlapping */
            fillbitmap(bitmap, Machine.pens[0], Machine.visible_area);

            for (offs = 0; offs < videoram_size[0]; offs++) {
                int code, sx, sy, col;

                sy = 8 * (offs / 32) - (meteor_scrollram.read(offs) & 0x0f);
                sx = 8 * (offs % 32) + ((meteor_scrollram.read(offs) >> 4) & 0x0f);

                code = videoram.read(offs) + ((colorram.read(offs) & 0x01) << 8);
                col = (~colorram.read(offs) >> 4) & 0x07;

                drawgfx(bitmap, Machine.gfx[0],
                        code,
                        col,
                        0, 0,
                        sx, sy,
                        Machine.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };
}
