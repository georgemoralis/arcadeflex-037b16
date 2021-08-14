/**
 * ported to v0.37b16
 *
 */
package gr.codebb.arcadeflex.v037b16.vidhrdw;

//generic imports
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//drivers imports
import static gr.codebb.arcadeflex.v037b16.drivers.balsente.*;
//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.osdependH.*;
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfx_modes8.*;
import static gr.codebb.arcadeflex.v037b16.mame.palette.*;
import static gr.codebb.arcadeflex.v037b16.mame.paletteH.*;
//vidhrdw imports
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.generic.*;

//to be organized
import static arcadeflex036.osdepend.logerror;
import static common.libc.cstring.memset;
import common.ptr.UBytePtr;
import common.subArrays.IntArray;
import static mame037b16.drawgfx.plot_pixel;
import static mame037b16.mame.Machine;
import static mame037b7.palette.palette_change_color;

public class balsente {

    /**
     * ***********************************
     *
     * Statics
     *
     ************************************
     */
    static UBytePtr local_videoram;
    static char[]/*UINT8 */ scanline_dirty;
    static char[]/*UINT8 */ scanline_palette;
    static UBytePtr sprite_data;

    static int/*UINT8*/ u8_last_scanline_palette;
    static int/*UINT8*/ u8_screen_refresh_counter;
    static int/*UINT8*/ u8_palettebank_vis;

    /**
     * ***********************************
     *
     * Video system start
     *
     ************************************
     */
    public static VhStartPtr balsente_vh_start = new VhStartPtr() {
        public int handler() {
            /* reset the system */
            u8_palettebank_vis = 0;

            /* allocate a local copy of video RAM */
            local_videoram = new UBytePtr(256 * 256);

            /* allocate a scanline dirty array */
            scanline_dirty = new char[256];

            /* allocate a scanline palette array */
            scanline_palette = new char[256];

            /* mark everything dirty to start */
            memset(scanline_dirty, 1, 256);

            /* reset the scanline palette */
            memset(scanline_palette, 0, 256);
            u8_last_scanline_palette = 0;

            sprite_data = memory_region(REGION_GFX1);

            return 0;
        }
    };

    /**
     * ***********************************
     *
     * Video system shutdown
     *
     ************************************
     */
    public static VhStopPtr balsente_vh_stop = new VhStopPtr() {
        public void handler() {
            /* free the local video RAM array */
            if (local_videoram != null) {
                local_videoram = null;
            }

            /* free the scanline dirty array */
            if (scanline_dirty != null) {
                scanline_dirty = null;
            }

            /* free the scanline dirty array */
            if (scanline_palette != null) {
                scanline_palette = null;
            }
        }
    };

    /**
     * ***********************************
     *
     * Video RAM write
     *
     ************************************
     */
    public static WriteHandlerPtr balsente_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            videoram.write(offset, data);

            /* expand the two pixel values into two bytes */
            local_videoram.write(offset * 2 + 0, data >> 4);
            local_videoram.write(offset * 2 + 1, data & 15);

            /* mark the scanline dirty */
            scanline_dirty[offset / 128] = 1;
        }
    };

    /**
     * ***********************************
     *
     * Palette banking
     *
     ************************************
     */
    static void update_palette() {
        int scanline = cpu_getscanline(), i;
        if (scanline > 255) {
            scanline = 0;
        }

        /* special case: the scanline is the same as last time, but a screen refresh has occurred */
        if (scanline == u8_last_scanline_palette && u8_screen_refresh_counter != 0) {
            for (i = 0; i < 256; i++) {
                /* mark the scanline dirty if it was a different palette */
                if (scanline_palette[i] != u8_palettebank_vis) {
                    scanline_dirty[i] = 1;
                }
                scanline_palette[i] = (char) (u8_palettebank_vis & 0xFF);
            }
        } /* fill in the scanlines up till now */ else {
            for (i = u8_last_scanline_palette; i != scanline; i = (i + 1) & 255) {
                /* mark the scanline dirty if it was a different palette */
                if (scanline_palette[i] != u8_palettebank_vis) {
                    scanline_dirty[i] = 1;
                }
                scanline_palette[i] = (char) (u8_palettebank_vis & 0xFF);
            }

            /* remember where we left off */
            u8_last_scanline_palette = scanline & 0xFF;
        }

        /* reset the screen refresh counter */
        u8_screen_refresh_counter = 0;
    }

    public static WriteHandlerPtr balsente_palette_select_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* only update if changed */
            if (u8_palettebank_vis != (data & 3)) {
                /* update the scanline palette */
                update_palette();
                u8_palettebank_vis = data & 3;
            }

            logerror("balsente_palette_select_w(%d) scanline=%d\n", data & 3, cpu_getscanline());
        }
    };

    /**
     * ***********************************
     *
     * Palette RAM write
     *
     ************************************
     */
    public static WriteHandlerPtr balsente_paletteram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int r, g, b;

            paletteram.write(offset, data & 0x0f);

            r = paletteram.read((offset & ~3) + 0);
            g = paletteram.read((offset & ~3) + 1);
            b = paletteram.read((offset & ~3) + 2);
            palette_change_color(offset / 4, (r << 4) | r, (g << 4) | g, (b << 4) | b);
        }
    };

    /**
     * ***********************************
     *
     * Sprite drawing
     *
     ************************************
     */
    static void draw_one_sprite(osd_bitmap bitmap, UBytePtr sprite) {
        int flags = sprite.read(0);
        int image = sprite.read(1) | ((flags & 3) << 8);
        int ypos = sprite.read(2) + 17;
        int xpos = sprite.read(3);
        UBytePtr src;
        int x, y;

        /* get a pointer to the source image */
        src = new UBytePtr(sprite_data, 64 * image);
        if ((flags & 0x80) != 0) {
            src.inc(4 * 15);
        }

        /* loop over y */
        for (y = 0; y < 16; y++, ypos = (ypos + 1) & 255) {
            if (ypos >= 16 && ypos < 240) {
                IntArray pens = new IntArray(Machine.pens, scanline_palette[y] * 256);
                UBytePtr old = new UBytePtr(local_videoram, ypos * 256 + xpos);
                int currx = xpos;

                /* mark this scanline dirty */
                scanline_dirty[ypos] = 1;

                /* standard case */
                if ((flags & 0x40) == 0) {
                    /* loop over x */
                    for (x = 0; x < 4; x++, old.offset += 2) {
                        int ipixel = src.readinc();
                        int left = ipixel & 0xf0;
                        int right = (ipixel << 4) & 0xf0;

                        /* left pixel, combine with the background */
                        if (left != 0 && currx >= 0 && currx < 256) {
                            plot_pixel.handler(bitmap, currx, ypos, pens.read(left | old.read(0)));
                        }
                        currx++;

                        /* right pixel, combine with the background */
                        if (right != 0 && currx >= 0 && currx < 256) {
                            plot_pixel.handler(bitmap, currx, ypos, pens.read(right | old.read(1)));
                        }
                        currx++;
                    }
                } /* hflip case */ else {
                    src.inc(4);

                    /* loop over x */
                    for (x = 0; x < 4; x++, old.offset += 2) {
                        src.dec();
                        int ipixel = src.read();//int ipixel = *--src;

                        int left = (ipixel << 4) & 0xf0;
                        int right = ipixel & 0xf0;

                        /* left pixel, combine with the background */
                        if (left != 0 && currx >= 0 && currx < 256) {
                            plot_pixel.handler(bitmap, currx, ypos, pens.read(left | old.read(0)));
                        }
                        currx++;

                        /* right pixel, combine with the background */
                        if (right != 0 && currx >= 0 && currx < 256) {
                            plot_pixel.handler(bitmap, currx, ypos, pens.read(right | old.read(1)));
                        }
                        currx++;
                    }
                    src.inc(4);
                }
            } else {
                src.inc(4);
            }
            if ((flags & 0x80) != 0) {
                src.dec(2 * 4);
            }
        }
    }

    /**
     * ***********************************
     *
     * Main screen refresh
     *
     ************************************
     */
    public static VhUpdatePtr balsente_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int/*UINT8*/[] palette_used = new int[4];
            int x, y, i;

            /* update the remaining scanlines */
            u8_screen_refresh_counter = (u8_screen_refresh_counter + 1) & 0xff;
            update_palette();

            /* determine which palette banks were used */
            palette_used[0] = palette_used[1] = palette_used[2] = palette_used[3] = 0;
            for (i = 0; i < 240; i++) {
                palette_used[scanline_palette[i]] = 1;
            }

            /* make sure color 1024 is white for our crosshair */
            palette_change_color(1024, 0xff, 0xff, 0xff);

            /* set the used status of all the palette entries */
            for (x = 0; x < 4; x++) {
                if (palette_used[x] != 0) {
                    memset(palette_used_colors, x * 256, PALETTE_COLOR_USED, 256);
                } else {
                    memset(palette_used_colors, x * 256, PALETTE_COLOR_UNUSED, 256);
                }
            }
            palette_used_colors.write(1024, balsente_shooter != 0 ? PALETTE_COLOR_USED : PALETTE_COLOR_UNUSED);

            /* recompute the palette, and mark all scanlines dirty if we need to redraw */
            if (palette_recalc() != null) {
                memset(scanline_dirty, 1, 256);
            }

            /* draw any dirty scanlines from the VRAM directly */
            for (y = 0; y < 240; y++) {
                if (scanline_dirty[y] != 0 || full_refresh != 0) {
                    IntArray pens = new IntArray(Machine.pens, scanline_palette[y] * 256);
                    draw_scanline8(bitmap, 0, y, 256, new UBytePtr(local_videoram, y * 256), pens, -1);
                    scanline_dirty[y] = 0;
                }
            }

            /* draw the sprite images */
            for (i = 0; i < 40; i++) {
                draw_one_sprite(bitmap, new UBytePtr(spriteram, (0xe0 + i * 4) & 0xff));
            }

            /* draw a crosshair */
            if (balsente_shooter != 0) {
                int beamx = balsente_shooter_x;
                int beamy = balsente_shooter_y - 12;

                int xoffs = beamx - 3;
                int yoffs = beamy - 3;

                for (y = -3; y <= 3; y++, yoffs++, xoffs++) {
                    if (yoffs >= 0 && yoffs < 240 && beamx >= 0 && beamx < 256) {
                        plot_pixel.handler(bitmap, beamx, yoffs, Machine.pens[1024]);
                        scanline_dirty[yoffs] = 1;
                    }
                    if (xoffs >= 0 && xoffs < 256 && beamy >= 0 && beamy < 240) {
                        plot_pixel.handler(bitmap, xoffs, beamy, Machine.pens[1024]);
                    }
                }
            }
        }
    };
}
