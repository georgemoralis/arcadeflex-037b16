/*
 * ported to v0.37b16
 */
package gr.codebb.arcadeflex.v037b16.vidhrdw;

import static arcadeflex036.osdepend.logerror;
import static common.libc.cstring.memset;
import common.ptr.IntPtr;
import common.ptr.UBytePtr;
import common.subArrays.IntArray;
import gr.codebb.arcadeflex.v037b16.generic.fucPtr.VhStartPtr;
import gr.codebb.arcadeflex.v037b16.mame.drawgfxH.rectangle;
import gr.codebb.arcadeflex.v037b16.mame.osdependH.osd_bitmap;
import static mame037b16.mame.Machine;
import static gr.codebb.arcadeflex.WIP.v037b16.machine.williams.*;
import gr.codebb.arcadeflex.v037b16.generic.fucPtr.ReadHandlerPtr;
import gr.codebb.arcadeflex.v037b16.generic.fucPtr.VhStopPtr;
import gr.codebb.arcadeflex.v037b16.generic.fucPtr.VhUpdatePtr;
import gr.codebb.arcadeflex.v037b16.generic.fucPtr.WriteHandlerPtr;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.cpu_get_pc;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.cpu_getscanline;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfx_modes8.draw_scanline8;
import static gr.codebb.arcadeflex.v037b16.mame.memory.cpu_readmem16;
import static gr.codebb.arcadeflex.v037b16.mame.memory.cpu_writemem16;
import static gr.codebb.arcadeflex.v037b16.mame.palette.palette_recalc;
import static mame037b16.drawgfx.mark_dirty;
import static mame037b7.palette.palette_change_color;

public class williams {

    public static final int VIDEORAM_WIDTH = 304;
    public static final int VIDEORAM_HEIGHT = 256;
    public static final int VIDEORAM_SIZE = (VIDEORAM_WIDTH * VIDEORAM_HEIGHT);

    /* RAM globals */
    public static UBytePtr williams_videoram;
    public static UBytePtr williams_videoram_copy;
    public static UBytePtr williams2_paletteram;

    /* blitter variables */
    public static UBytePtr williams_blitterram = new UBytePtr();
    public static char/*UINT8*/ williams_blitter_xor;
    public static char/*UINT8*/ williams_blitter_remap;
    public static char/*UINT8*/ williams_blitter_clip;


    /* Blaster extra variables */
    public static UBytePtr blaster_video_bits = new UBytePtr();
    public static UBytePtr blaster_color_zero_flags = new UBytePtr();
    public static UBytePtr blaster_color_zero_table = new UBytePtr();
    public static UBytePtr blaster_remap;
    public static UBytePtr blaster_remap_lookup;

    /* tilemap variables */
    public static char/*UINT8*/ williams2_tilemap_mask;
    public static char[] williams2_row_to_palette;
    /* take care of IC79 and J1/J2 */
    public static char/*UINT8*/ williams2_M7_flip;
    public static byte williams2_videoshift;
    public static char/*UINT8*/ williams2_special_bg_color;
    public static char/*UINT8*/ williams2_fg_color;
    /* IC90 */
    public static char/*UINT8*/ williams2_bg_color;
    /* IC89 */

 /* later-Williams video control variables */
    public static UBytePtr williams2_blit_inhibit = new UBytePtr();
    public static UBytePtr williams2_xscroll_low = new UBytePtr();
    public static UBytePtr williams2_xscroll_high = new UBytePtr();

    /* pixel copiers */
    static UBytePtr scanline_dirty;

    public static abstract interface blitter_table_Ptr {

        public abstract void handler(int sstart, int dstart, int w, int h, int data);
    }

    static blitter_table_Ptr[] blitter_table;

    /**
     * ***********************************
     *
     * Copy pixels from videoram to the screen bitmap
     *
     ************************************
     */
    static void copy_pixels(osd_bitmap bitmap, rectangle clip, int transparent_pen) {
        int blaster_back_color = 0;
        int pairs = (clip.max_x - clip.min_x + 1) / 2;
        int xoffset = clip.min_x;
        int x, y;

        /* loop over rows */
        for (y = clip.min_y; y <= clip.max_y; y++) {
            UBytePtr source = new UBytePtr(williams_videoram_copy, y + 256 * (xoffset / 2));
            char[]/*UINT8*/ scanline = new char[400];
            UBytePtr dest = new UBytePtr(scanline);

            /* skip if not dirty (but only for non-transparent drawing) */
            if (transparent_pen == -1 && williams_blitter_remap == 0) {
                if (scanline_dirty.read(y) == 0) {
                    continue;
                }
                scanline_dirty.write(y,scanline_dirty.read()-1);//scanline_dirty[y]--;
            }

            /* mark the pixels dirty */
            mark_dirty.handler(clip.min_x, y, clip.max_x, y);

            /* draw all pairs */
             for (x = 0; x < pairs; x++, source.inc(256)) {
                int pix =  source.read();
                 dest.writeinc(pix >> 4);
                 dest.writeinc(pix & 0x0f);
            }

            /* handle general case */
             if (williams_blitter_remap==0) {
                draw_scanline8(bitmap, xoffset, y, pairs * 2, new UBytePtr(scanline), new IntArray(Machine.pens), transparent_pen);
            } /* handle Blaster special case */ else {
                int/*UINT8*/ saved_pen0;

            /* pick the background pen */
                 if ((blaster_video_bits.read() & 1)!=0) {
                    if ((blaster_color_zero_flags.read(y) & 1)!=0) {
                        blaster_back_color = 16 + y - Machine.visible_area.min_y;
                    }
                } else {
                    blaster_back_color = 0;
                }

                            /* draw the scanline, temporarily remapping pen 0 */
                saved_pen0 = Machine.pens[0];
                Machine.pens[0] = Machine.pens[blaster_back_color];
                draw_scanline8(bitmap, xoffset, y, pairs * 2, new UBytePtr(scanline), new IntArray(Machine.pens), transparent_pen);
                Machine.pens[0] = saved_pen0;
            }
        }
    }

    /**
     * ***********************************
     *
     * Early Williams video startup/shutdown
     *
     ************************************
     */
    public static VhStartPtr williams_vh_start = new VhStartPtr() {
        public int handler() {
            /* allocate space for video RAM and dirty scanlines */
            williams_videoram = new UBytePtr(2 * VIDEORAM_SIZE + 256);

            williams_videoram_copy = new UBytePtr(williams_videoram, VIDEORAM_SIZE);
            scanline_dirty = new UBytePtr(williams_videoram_copy, VIDEORAM_SIZE);

            /* reset everything */
            memset(williams_videoram, 0, VIDEORAM_SIZE);
            memset(williams_videoram_copy, 0, VIDEORAM_SIZE);
            memset(scanline_dirty, 2, 256);

            /* pick the blitters */
            blitter_table = williams_blitters;
            if (williams_blitter_remap != 0) {
                blitter_table = blaster_blitters;
            }
            if (williams_blitter_clip != 0) {
                blitter_table = sinistar_blitters;
            }

            /* reset special-purpose flags */
            blaster_remap_lookup = null;
            sinistar_clip = 0xffff;

            return 0;
        }
    };

    public static VhStopPtr williams_vh_stop = new VhStopPtr() {
        public void handler() {
            /* free any remap lookup tables */
            if (blaster_remap_lookup != null) {
                blaster_remap_lookup = null;
            }

            /* free video RAM */
            if (williams_videoram != null) {
                williams_videoram = null;
                williams_videoram_copy = null;
            }
            scanline_dirty = null;
        }
    };

    /**
     * ***********************************
     *
     * Early Williams video update
     *
     ************************************
     */
    public static void williams_vh_update(int scanline) {
        	int erase_behind = 0;
	IntPtr srcbase, dstbase;
	int x;

	/* wrap around at the bottom */
	if (scanline == 0)
		scanline = 256;

	/* should we erase as we draw? */
	if (williams_blitter_remap!=0 && scanline >= 32 && (blaster_video_bits.read() & 0x02)!=0)
		erase_behind = 1;

	/* determine the source and destination */
 	srcbase = new IntPtr(williams_videoram,scanline - 8);
 	dstbase = new IntPtr(williams_videoram_copy,scanline - 8);

 	/* loop over columns and copy a 16-row chunk */
 	for (x = 0; x < VIDEORAM_WIDTH/2; x++)
 	{
 		/* copy 16 rows' worth of data */
 		dstbase.write(0, srcbase.read(0));//dstbase[0] = srcbase[0];
 		dstbase.write(4, srcbase.read(4));//dstbase[1] = srcbase[1];

		/* handle Blaster autoerase for scanlines 24 and up */
		if (erase_behind!=0){
			srcbase.write(0,0);
                        srcbase.write(4,0);//srcbase[0] = srcbase[1] = 0;
                }

 		/* advance to the next column */
 		srcbase.base += 256;//srcbase += 256/4;
 		dstbase.base += 256;//dstbase += 256/4;
 	}
    }
    public static VhUpdatePtr williams_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            /* full refresh forces us to redraw everything */
            if (palette_recalc() != null || full_refresh != 0) {
                memset(scanline_dirty, 2, 256);
            }

            /* copy the pixels into the final result */
            copy_pixels(bitmap, Machine.visible_area, -1);
        }
    };

    /**
     * ***********************************
     *
     * Early Williams video I/O
     *
     ************************************
     */
    public static WriteHandlerPtr williams_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* only update if different */
            if (williams_videoram.read(offset) != data) {
                /* store to videoram and mark the scanline dirty */
                williams_videoram.write(offset, data);
                scanline_dirty.write(offset % 256, 2);
            }
        }
    };
    public static ReadHandlerPtr williams_video_counter_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_getscanline() & 0xfc;
        }
    };

    /**
     * ***********************************
     *
     * Later Williams video startup/shutdown
     *
     ************************************
     */
    public static VhStartPtr williams2_vh_start = new VhStartPtr() {
        public int handler() {
            /* standard initialization */
            if (williams_vh_start.handler() != 0) {
                return 1;
            }

            /* override the blitters */
            blitter_table = williams2_blitters;

            /* allocate a buffer for palette RAM */
            williams2_paletteram = new UBytePtr(4 * 1024 * 4 / 8);
            if (williams2_paletteram == null) {
                williams2_vh_stop.handler();
                return 1;
            }

            /* clear it */
            memset(williams2_paletteram, 0, 4 * 1024 * 4 / 8);

            /* reset the FG/BG colors */
            williams2_fg_color = 0;
            williams2_bg_color = 0;

            return 0;
        }
    };

    public static VhStopPtr williams2_vh_stop = new VhStopPtr() {
        public void handler() {
            /* free palette RAM */
            if (williams2_paletteram != null) {
                williams2_paletteram = null;
            }

            /* clean up other stuff */
            williams_vh_stop.handler();
        }
    };

    /**
     * ***********************************
     *
     * Later Williams video update
     *
     ************************************
     */
    public static VhUpdatePtr williams2_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            /*TODO*///	UINT8 *tileram = &memory_region(REGION_CPU1)[0xc000];
/*TODO*///	int xpixeloffset, xtileoffset;
/*TODO*///	int color, col, y;
/*TODO*///
/*TODO*///	/* full refresh forces us to redraw everything */
/*TODO*///	if (palette_recalc() || full_refresh)
/*TODO*///		memset(scanline_dirty, 2, 256);
/*TODO*///
/*TODO*///	/* assemble the bits that describe the X scroll offset */
/*TODO*///	xpixeloffset = (*williams2_xscroll_high & 1) * 12 +
/*TODO*///	               (*williams2_xscroll_low >> 7) * 6 +
/*TODO*///	               (*williams2_xscroll_low & 7) +
/*TODO*///	               williams2_videoshift;
/*TODO*///	xtileoffset = *williams2_xscroll_high >> 1;
/*TODO*///
/*TODO*///	/* adjust the offset for the row and compute the palette index */
/*TODO*///	for (y = 0; y < 256; y += 16, tileram++)
/*TODO*///	{
/*TODO*///		color = williams2_row_to_palette[y / 16];
/*TODO*///
/*TODO*///		/* 12 columns wide, each block is 24 pixels wide, 288 pixel lines */
/*TODO*///		for (col = 0; col <= 12; col++)
/*TODO*///		{
/*TODO*///			unsigned int map = tileram[((col + xtileoffset) * 16) & 0x07ff];
/*TODO*///
/*TODO*///			drawgfx(bitmap, Machine->gfx[0], map & williams2_tilemap_mask,
/*TODO*///					color, map & williams2_M7_flip, 0, col * 24 - xpixeloffset, y,
/*TODO*///					&Machine->visible_area, TRANSPARENCY_NONE, 0);
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* copy the bitmap data on top of that */
/*TODO*///	copy_pixels(bitmap, &Machine->visible_area, 0);
        }
    };
    /*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Later Williams palette I/O
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void williams2_modify_color(int color, int offset)
/*TODO*///{
/*TODO*///	static const UINT8 ztable[16] =
/*TODO*///	{
/*TODO*///		0x0, 0x3, 0x4,  0x5, 0x6, 0x7, 0x8,  0x9,
/*TODO*///		0xa, 0xb, 0xc,  0xd, 0xe, 0xf, 0x10, 0x11
/*TODO*///	};
/*TODO*///
/*TODO*///	UINT8 entry_lo = williams2_paletteram[offset * 2];
/*TODO*///	UINT8 entry_hi = williams2_paletteram[offset * 2 + 1];
/*TODO*///	UINT8 i = ztable[(entry_hi >> 4) & 15];
/*TODO*///	UINT8 b = ((entry_hi >> 0) & 15) * i;
/*TODO*///	UINT8 g = ((entry_lo >> 4) & 15) * i;
/*TODO*///	UINT8 r = ((entry_lo >> 0) & 15) * i;
/*TODO*///
/*TODO*///	palette_change_color(color, r, g, b);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///static void williams2_update_fg_color(unsigned int offset)
/*TODO*///{
/*TODO*///	unsigned int page_offset = williams2_fg_color * 16;
/*TODO*///
/*TODO*///	/* only modify the palette if we're talking to the current page */
/*TODO*///	if (offset >= page_offset && offset < page_offset + 16)
/*TODO*///		williams2_modify_color(offset - page_offset, offset);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///static void williams2_update_bg_color(unsigned int offset)
/*TODO*///{
/*TODO*///	unsigned int page_offset = williams2_bg_color * 16;
/*TODO*///
/*TODO*///	/* non-Mystic Marathon variant */
/*TODO*///	if (!williams2_special_bg_color)
/*TODO*///	{
/*TODO*///		/* only modify the palette if we're talking to the current page */
/*TODO*///		if (offset >= page_offset && offset < page_offset + Machine->drv->total_colors - 16)
/*TODO*///			williams2_modify_color(offset - page_offset + 16, offset);
/*TODO*///	}
/*TODO*///
/*TODO*///	/* Mystic Marathon variant */
/*TODO*///	else
/*TODO*///	{
/*TODO*///		/* only modify the palette if we're talking to the current page */
/*TODO*///		if (offset >= page_offset && offset < page_offset + 16)
/*TODO*///			williams2_modify_color(offset - page_offset + 16, offset);
/*TODO*///
/*TODO*///		/* check the secondary palette as well */
/*TODO*///		page_offset |= 0x10;
/*TODO*///		if (offset >= page_offset && offset < page_offset + 16)
/*TODO*///			williams2_modify_color(offset - page_offset + 32, offset);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
    public static WriteHandlerPtr williams2_fg_select_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*TODO*///	unsigned int i, palindex;
/*TODO*///
/*TODO*///	/* if we're already mapped, leave it alone */
/*TODO*///	if (williams2_fg_color == data)
/*TODO*///		return;
/*TODO*///	williams2_fg_color = data & 0x3f;
/*TODO*///
/*TODO*///	/* remap the foreground colors */
/*TODO*///	palindex = williams2_fg_color * 16;
/*TODO*///	for (i = 0; i < 16; i++)
/*TODO*///		williams2_modify_color(i, palindex++);
        }
    };

    public static WriteHandlerPtr williams2_bg_select_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*TODO*///	unsigned int i, palindex;
/*TODO*///
/*TODO*///	/* if we're already mapped, leave it alone */
/*TODO*///	if (williams2_bg_color == data)
/*TODO*///		return;
/*TODO*///	williams2_bg_color = data & 0x3f;
/*TODO*///
/*TODO*///	/* non-Mystic Marathon variant */
/*TODO*///	if (!williams2_special_bg_color)
/*TODO*///	{
/*TODO*///		/* remap the background colors */
/*TODO*///		palindex = williams2_bg_color * 16;
/*TODO*///		for (i = 16; i < Machine->drv->total_colors; i++)
/*TODO*///			williams2_modify_color(i, palindex++);
/*TODO*///	}
/*TODO*///
/*TODO*///	/* Mystic Marathon variant */
/*TODO*///	else
/*TODO*///	{
/*TODO*///		/* remap the background colors */
/*TODO*///		palindex = williams2_bg_color * 16;
/*TODO*///		for (i = 16; i < 32; i++)
/*TODO*///			williams2_modify_color(i, palindex++);
/*TODO*///
/*TODO*///		/* remap the secondary background colors */
/*TODO*///		palindex = (williams2_bg_color | 1) * 16;
/*TODO*///		for (i = 32; i < 48; i++)
/*TODO*///			williams2_modify_color(i, palindex++);
/*TODO*///	}
        }
    };
    /*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Later Williams video I/O
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
    public static WriteHandlerPtr williams2_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*TODO*///	/* bank 3 doesn't touch the screen */
/*TODO*///	if ((williams2_bank & 0x03) == 0x03)
/*TODO*///	{
/*TODO*///		/* bank 3 from $8000 - $8800 affects palette RAM */
/*TODO*///		if (offset >= 0x8000 && offset < 0x8800)
/*TODO*///		{
/*TODO*///			offset -= 0x8000;
/*TODO*///			williams2_paletteram[offset] = data;
/*TODO*///
/*TODO*///			/* update the palette value if necessary */
/*TODO*///			offset >>= 1;
/*TODO*///			williams2_update_fg_color(offset);
/*TODO*///			williams2_update_bg_color(offset);
/*TODO*///		}
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* everyone else talks to the screen */
/*TODO*///	williams_videoram[offset] = data;
        }
    };
    /*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Blaster-specific video start
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
    public static VhStartPtr blaster_vh_start = new VhStartPtr() {
        public int handler() {
            /*TODO*///	int i, j;
/*TODO*///
/*TODO*///	/* standard startup first */
/*TODO*///	if (williams_vh_start())
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	/* Expand the lookup table so that we do one lookup per byte */
/*TODO*///	blaster_remap_lookup = malloc(256 * 256);
/*TODO*///	if (blaster_remap_lookup)
/*TODO*///		for (i = 0; i < 256; i++)
/*TODO*///		{
/*TODO*///			const UINT8 *table = memory_region(REGION_PROMS) + (i & 0x7f) * 16;
/*TODO*///			for (j = 0; j < 256; j++)
/*TODO*///				blaster_remap_lookup[i * 256 + j] = (table[j >> 4] << 4) | table[j & 0x0f];
/*TODO*///		}
/*TODO*///
/*TODO*///	/* mark color 0 as transparent. we will draw the rainbow background behind it */
/*TODO*///	palette_used_colors[0] = PALETTE_COLOR_TRANSPARENT;
/*TODO*///	for (i = 0; i < 256; i++)
/*TODO*///	{
/*TODO*///		/* mark as used only the colors used for the visible background lines */
/*TODO*///		if (i < Machine->visible_area.min_y || i > Machine->visible_area.max_y)
/*TODO*///			palette_used_colors[16 + i] = PALETTE_COLOR_UNUSED;
/*TODO*///
/*TODO*///		/* TODO: this leaves us with a total of 255+1 colors used, which is just */
/*TODO*///		/* a bit too much for the palette system to handle them efficiently. */
/*TODO*///		/* As a quick workaround, I set the top three lines to be always black. */
/*TODO*///		/* To do it correctly, vh_screenrefresh() should group the background */
/*TODO*///		/* lines of the same color and mark the others as COLOR_UNUSED. */
/*TODO*///		/* The background is very redundant so this can be done easily. */
/*TODO*///		palette_used_colors[16 + 0 + Machine->visible_area.min_y] = PALETTE_COLOR_TRANSPARENT;
/*TODO*///		palette_used_colors[16 + 1 + Machine->visible_area.min_y] = PALETTE_COLOR_TRANSPARENT;
/*TODO*///		palette_used_colors[16 + 2 + Machine->visible_area.min_y] = PALETTE_COLOR_TRANSPARENT;
/*TODO*///	}

            return 0;
        }
    };

    /**
     * ***********************************
     *
     * Blaster-specific enhancements
     *
     ************************************
     */
    public static WriteHandlerPtr blaster_remap_select_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            blaster_remap = new UBytePtr(blaster_remap_lookup, data * 256);
        }
    };

    public static WriteHandlerPtr blaster_palette_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            blaster_color_zero_table.write(offset, data);
            data ^= 0xff;
            if (offset >= Machine.visible_area.min_y && offset <= Machine.visible_area.max_y) {
                int r = data & 7;
                int g = (data >> 3) & 7;
                int b = (data >> 6) & 3;

                r = (r << 5) | (r << 2) | (r >> 1);
                g = (g << 5) | (g << 2) | (g >> 1);
                b = (b << 6) | (b << 4) | (b << 2) | b;
                palette_change_color(16 + offset - Machine.visible_area.min_y, r, g, b);
            }
        }
    };

    /**
     * ***********************************
     *
     * Blitter I/O
     *
     ************************************
     */
    public static WriteHandlerPtr williams_blitter_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int sstart, dstart, w, h, count;

            /* store the data */
            williams_blitterram.write(offset, data);

            /* only writes to location 0 trigger the blit */
            if (offset != 0) {
                return;
            }

            /* compute the starting locations */
            sstart = (williams_blitterram.read(2) << 8) + williams_blitterram.read(3);
            dstart = (williams_blitterram.read(4) << 8) + williams_blitterram.read(5);

            /* compute the width and height */
            w = williams_blitterram.read(6) ^ williams_blitter_xor;
            h = williams_blitterram.read(7) ^ williams_blitter_xor;

            /* adjust the width and height */
            if (w == 0) {
                w = 1;
            }
            if (h == 0) {
                h = 1;
            }
            if (w == 255) {
                w = 256;
            }
            if (h == 255) {
                h = 256;
            }

            /* call the appropriate blitter */
            (blitter_table[(data >> 3) & 3]).handler(sstart, dstart, w, h, data);

            /* compute the ending address */
            if ((data & 0x02) != 0) {
                count = h;
            } else {
                count = w + w * h;
            }
            if (count > 256) {
                count = 256;
            }

            /* mark dirty */
            w = dstart % 256;
            while (count-- > 0) {
                scanline_dirty.write(w++ % 256, 2);
            }

            /* Log blits */
            logerror("---------- Blit %02X--------------PC: %04X\n", data, cpu_get_pc());
            logerror("Source : %02X %02X\n", williams_blitterram.read(2), williams_blitterram.read(3));
            logerror("Dest   : %02X %02X\n", williams_blitterram.read(4), williams_blitterram.read(5));
            logerror("W H    : %02X %02X (%d,%d)\n", williams_blitterram.read(6), williams_blitterram.read(7), williams_blitterram.read(6) ^ 4, williams_blitterram.read(7) ^ 4);
            logerror("Mask   : %02X\n", williams_blitterram.read(1));
        }
    };

    /**
     * ***********************************
     * Blitter macros ***********************************
     */

    /* blit with pixel color 0 == transparent */
    public static void BLASTER_BLIT_TRANSPARENT(int offset, int data, int keepmask) {
        data = blaster_remap.read((data) & 0xff);
        if (data != 0) {
            int pix = ((offset < 0x9700) ? williams_videoram.read(offset) : cpu_readmem16(offset));
            int tempmask = keepmask;

            if ((data & 0xf0) == 0) {
                tempmask |= 0xf0;
            }
            if ((data & 0x0f) == 0) {
                tempmask |= 0x0f;
            }

            pix = (pix & tempmask) | (data & ~tempmask);
            if (offset < 0x9700) {
                williams_videoram.write(offset, pix);
            } else {
                cpu_writemem16(offset, pix);
            }
        }
    }

    public static void WILLIAMS2_BLIT_TRANSPARENT(int offset, int data, int keepmask) {
        if (data != 0) {
            int pix = ((offset < 0x9000 && (williams2_bank & 0x03) != 0x03) ? williams_videoram.read(offset) : cpu_readmem16(offset));
            int tempmask = keepmask;

            if ((data & 0xf0) == 0) {
                tempmask |= 0xf0;
            }
            if ((data & 0x0f) == 0) {
                tempmask |= 0x0f;
            }

            pix = (pix & tempmask) | (data & ~tempmask);
            if (offset < 0x9000 && (williams2_bank & 0x03) != 0x03) {
                williams_videoram.write(offset, pix);
            } else if (offset < 0x9000 || offset >= 0xc000 || williams2_blit_inhibit.read() == 0) {
                cpu_writemem16(offset, pix);
            }
        }
    }

    public static void SINISTAR_BLIT_TRANSPARENT(int offset, int data, int keepmask) {
        if (data != 0) {
            int pix = ((offset < 0x9800) ? williams_videoram.read(offset) : cpu_readmem16(offset));
            int tempmask = keepmask;

            if ((data & 0xf0) == 0) {
                tempmask |= 0xf0;
            }
            if ((data & 0x0f) == 0) {
                tempmask |= 0x0f;
            }

            pix = (pix & tempmask) | (data & ~tempmask);
            if (offset < sinistar_clip) {
                if (offset < 0x9800) {
                    williams_videoram.write(offset, pix);
                } else {
                    cpu_writemem16(offset, pix);
                }
            }
        }
    }

    public static void WILLIAMS_BLIT_TRANSPARENT(int offset, int data, int keepmask) {
        if (data != 0) {
            int pix = ((offset < 0x9800) ? williams_videoram.read(offset) : cpu_readmem16(offset));
            int tempmask = keepmask;

            if ((data & 0xf0) == 0) {
                tempmask |= 0xf0;
            }
            if ((data & 0x0f) == 0) {
                tempmask |= 0x0f;
            }

            pix = (pix & tempmask) | (data & ~tempmask);
            if (offset < 0x9800) {
                williams_videoram.write(offset, pix);
            } else {
                cpu_writemem16(offset, pix);
            }
        }
    }


    /* blit with pixel color 0 == transparent, other pixels == solid color */
    public static void BLASTER_BLIT_TRANSPARENT_SOLID(int offset, int data, int keepmask, int solid) {
        data = blaster_remap.read((data) & 0xff);
        if (data != 0) {
            int pix = ((offset < 0x9700) ? williams_videoram.read(offset) : cpu_readmem16(offset));
            int tempmask = keepmask;

            if ((data & 0xf0) == 0) {
                tempmask |= 0xf0;
            }
            if ((data & 0x0f) == 0) {
                tempmask |= 0x0f;
            }

            pix = (pix & tempmask) | (solid & ~tempmask);
            if (offset < 0x9700) {
                williams_videoram.write(offset, pix);
            } else {
                cpu_writemem16(offset, pix);
            }
        }
    }

    public static void WILLIAMS2_BLIT_TRANSPARENT_SOLID(int offset, int data, int keepmask, int solid) {
        if (data != 0) {
            int pix = ((offset < 0x9000 && (williams2_bank & 0x03) != 0x03) ? williams_videoram.read(offset) : cpu_readmem16(offset));
            int tempmask = keepmask;

            if ((data & 0xf0) == 0) {
                tempmask |= 0xf0;
            }
            if ((data & 0x0f) == 0) {
                tempmask |= 0x0f;
            }

            pix = (pix & tempmask) | (solid & ~tempmask);
            if (offset < 0x9000 && (williams2_bank & 0x03) != 0x03) {
                williams_videoram.write(offset, pix);
            } else if (offset < 0x9000 || offset >= 0xc000 || williams2_blit_inhibit.read() == 0) {
                cpu_writemem16(offset, pix);
            }
        }
    }

    public static void SINISTAR_BLIT_TRANSPARENT_SOLID(int offset, int data, int keepmask, int solid) {
        if (data != 0) {
            int pix = ((offset < 0x9800) ? williams_videoram.read(offset) : cpu_readmem16(offset));
            int tempmask = keepmask;

            if ((data & 0xf0) == 0) {
                tempmask |= 0xf0;
            }
            if ((data & 0x0f) == 0) {
                tempmask |= 0x0f;
            }

            pix = (pix & tempmask) | (solid & ~tempmask);
            if (offset < sinistar_clip) {
                if (offset < 0x9800) {
                    williams_videoram.write(offset, pix);
                } else {
                    cpu_writemem16(offset, pix);
                }
            }
        }
    }

    public static void BLIT_TRANSPARENT_SOLID_WILLIAMS(int offset, int data, int keepmask, int solid) {
        if (data != 0) {
            int pix = ((offset < 0x9800) ? williams_videoram.read(offset) : cpu_readmem16(offset));
            int tempmask = keepmask;

            if ((data & 0xf0) == 0) {
                tempmask |= 0xf0;
            }
            if ((data & 0x0f) == 0) {
                tempmask |= 0x0f;
            }

            pix = (pix & tempmask) | (solid & ~tempmask);
            if (offset < 0x9800) {
                williams_videoram.write(offset, pix);
            } else {
                cpu_writemem16(offset, pix);
            }
        }
    }


    /* blit with no transparency */
    public static void BLASTER_BLIT_OPAQUE(int offset, int data, int keepmask) {
        int pix = ((offset < 0x9700) ? williams_videoram.read(offset) : cpu_readmem16(offset));
        data = blaster_remap.read((data) & 0xff);
        pix = (pix & keepmask) | (data & ~keepmask);
        if (offset < 0x9700) {
            williams_videoram.write(offset, pix);
        } else {
            cpu_writemem16(offset, pix);
        }
    }

    public static void WILLIAMS2_BLIT_OPAQUE(int offset, int data, int keepmask) {
        int pix = ((offset < 0x9000 && (williams2_bank & 0x03) != 0x03) ? williams_videoram.read(offset) : cpu_readmem16(offset));
        pix = (pix & keepmask) | (data & ~keepmask);
        if (offset < 0x9000 && (williams2_bank & 0x03) != 0x03) {
            williams_videoram.write(offset, pix);
        } else if (offset < 0x9000 || offset >= 0xc000 || williams2_blit_inhibit.read() == 0) {
            cpu_writemem16(offset, pix);
        }
    }

    public static void SINISTAR_BLIT_OPAQUE(int offset, int data, int keepmask) {
        int pix = ((offset < 0x9800) ? williams_videoram.read(offset) : cpu_readmem16(offset));
        pix = (pix & keepmask) | (data & ~keepmask);
        if (offset < sinistar_clip) {
            if (offset < 0x9800) {
                williams_videoram.write(offset, pix);
            } else {
                cpu_writemem16(offset, pix);
            }
        }
    }

    public static void WILLIAMS_BLIT_OPAQUE(int offset, int data, int keepmask) {
        int pix = ((offset < 0x9800) ? williams_videoram.read(offset) : cpu_readmem16(offset));
        pix = (pix & keepmask) | (data & ~keepmask);
        if (offset < 0x9800) {
            williams_videoram.write(offset, pix);
        } else {
            cpu_writemem16(offset, pix);
        }
    }

    /* blit with no transparency in a solid color */
    public static void BLASTER_BLIT_OPAQUE_SOLID(int offset, int data, int keepmask, int solid) {
        int pix = ((offset < 0x9700) ? williams_videoram.read(offset) : cpu_readmem16(offset));
        pix = (pix & keepmask) | (solid & ~keepmask);
        if (offset < 0x9700) {
            williams_videoram.write(offset, pix);
        } else {
            cpu_writemem16(offset, pix);
        }
    }

    public static void WILLIAMS2_BLIT_OPAQUE_SOLID(int offset, int data, int keepmask, int solid) {
        int pix = ((offset < 0x9000 && (williams2_bank & 0x03) != 0x03) ? williams_videoram.read(offset) : cpu_readmem16(offset));
        pix = (pix & keepmask) | (solid & ~keepmask);
        if (offset < 0x9000 && (williams2_bank & 0x03) != 0x03) {
            williams_videoram.write(offset, pix);
        } else if (offset < 0x9000 || offset >= 0xc000 || williams2_blit_inhibit.read() == 0) {
            cpu_writemem16(offset, pix);
        }
    }

    public static void SINISTAR_BLIT_OPAQUE_SOLID(int offset, int data, int keepmask, int solid) {
        int pix = ((offset < 0x9800) ? williams_videoram.read(offset) : cpu_readmem16(offset));
        pix = (pix & keepmask) | (solid & ~keepmask);
        if (offset < sinistar_clip) {
            if (offset < 0x9800) {
                williams_videoram.write(offset, pix);
            } else {
                cpu_writemem16(offset, pix);
            }
        }
    }

    public static void WILLIAMS_BLIT_OPAQUE_SOLID(int offset, int data, int keepmask, int solid) {
        int pix = ((offset < 0x9800) ? williams_videoram.read(offset) : cpu_readmem16(offset));
        pix = (pix & keepmask) | (solid & ~keepmask);
        if (offset < 0x9800) {
            williams_videoram.write(offset, pix);
        } else {
            cpu_writemem16(offset, pix);
        }
    }

    /**
     * ***********************************
     *
     * Blitter cores
     *
     ************************************
     */
    public static blitter_table_Ptr williams_blit_opaque = new blitter_table_Ptr() {

        @Override
        public void handler(int sstart, int dstart, int w, int h, int data) {
            int source, sxadv, syadv;
            int dest, dxadv, dyadv;
            int i, j, solid;
            int keepmask;

            /* compute how much to advance in the x and y loops */
            sxadv = (data & 0x01) != 0 ? 0x100 : 1;
            syadv = (data & 0x01) != 0 ? 1 : w;
            dxadv = (data & 0x02) != 0 ? 0x100 : 1;
            dyadv = (data & 0x02) != 0 ? 1 : w;

            /* determine the common mask */
            keepmask = 0x00;
            if ((data & 0x80) != 0) {
                keepmask |= 0xf0;
            }
            if ((data & 0x40) != 0) {
                keepmask |= 0x0f;
            }
            if (keepmask == 0xff) {
                return;
            }

            /* set the solid pixel value to the mask value */
            solid = williams_blitterram.read(1);

            /* first case: no shifting */
            if ((data & 0x20) == 0) {
                /* loop over the height */
                for (i = 0; i < h; i++) {
                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* loop over the width */
                    for (j = w; j > 0; j--) {
                        int srcdata = cpu_readmem16(source);
                        WILLIAMS_BLIT_OPAQUE(dest, srcdata, keepmask);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    sstart += syadv;
                    dstart += dyadv;
                }
            } /* second case: shifted one pixel */ else {
                /* swap halves of the keep mask and the solid color */
                keepmask = ((keepmask & 0xf0) >> 4) | ((keepmask & 0x0f) << 4);
                solid = ((solid & 0xf0) >> 4) | ((solid & 0x0f) << 4);

                /* loop over the height */
                for (i = 0; i < h; i++) {
                    int pixdata, srcdata, shiftedmask;

                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* left edge case */
                    pixdata = cpu_readmem16(source);
                    srcdata = (pixdata >> 4) & 0x0f;
                    shiftedmask = keepmask | 0xf0;
                    WILLIAMS_BLIT_OPAQUE(dest, srcdata, shiftedmask);

                    source = (source + sxadv) & 0xffff;
                    dest = (dest + dxadv) & 0xffff;

                    /* loop over the width */
                    for (j = w - 1; j > 0; j--) {
                        pixdata = (pixdata << 8) | cpu_readmem16(source);
                        srcdata = (pixdata >> 4) & 0xff;
                        WILLIAMS_BLIT_OPAQUE(dest, srcdata, keepmask);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    /* right edge case */
                    srcdata = (pixdata << 4) & 0xf0;
                    shiftedmask = keepmask | 0x0f;
                    WILLIAMS_BLIT_OPAQUE(dest, srcdata, shiftedmask);

                    sstart += syadv;
                    dstart += dyadv;
                }
            }
        }

    };
    public static blitter_table_Ptr williams_blit_transparent = new blitter_table_Ptr() {

        @Override
        public void handler(int sstart, int dstart, int w, int h, int data) {
            int source, sxadv, syadv;
            int dest, dxadv, dyadv;
            int i, j, solid;
            int keepmask;

            /* compute how much to advance in the x and y loops */
            sxadv = (data & 0x01) != 0 ? 0x100 : 1;
            syadv = (data & 0x01) != 0 ? 1 : w;
            dxadv = (data & 0x02) != 0 ? 0x100 : 1;
            dyadv = (data & 0x02) != 0 ? 1 : w;

            /* determine the common mask */
            keepmask = 0x00;
            if ((data & 0x80) != 0) {
                keepmask |= 0xf0;
            }
            if ((data & 0x40) != 0) {
                keepmask |= 0x0f;
            }
            if (keepmask == 0xff) {
                return;
            }

            /* set the solid pixel value to the mask value */
            solid = williams_blitterram.read(1);

            /* first case: no shifting */
            if ((data & 0x20) == 0) {
                /* loop over the height */
                for (i = 0; i < h; i++) {
                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* loop over the width */
                    for (j = w; j > 0; j--) {
                        int srcdata = cpu_readmem16(source);
                        WILLIAMS_BLIT_TRANSPARENT(dest, srcdata, keepmask);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    sstart += syadv;
                    dstart += dyadv;
                }
            } /* second case: shifted one pixel */ else {
                /* swap halves of the keep mask and the solid color */
                keepmask = ((keepmask & 0xf0) >> 4) | ((keepmask & 0x0f) << 4);
                solid = ((solid & 0xf0) >> 4) | ((solid & 0x0f) << 4);

                /* loop over the height */
                for (i = 0; i < h; i++) {
                    int pixdata, srcdata, shiftedmask;

                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* left edge case */
                    pixdata = cpu_readmem16(source);
                    srcdata = (pixdata >> 4) & 0x0f;
                    shiftedmask = keepmask | 0xf0;
                    WILLIAMS_BLIT_TRANSPARENT(dest, srcdata, shiftedmask);

                    source = (source + sxadv) & 0xffff;
                    dest = (dest + dxadv) & 0xffff;

                    /* loop over the width */
                    for (j = w - 1; j > 0; j--) {
                        pixdata = (pixdata << 8) | cpu_readmem16(source);
                        srcdata = (pixdata >> 4) & 0xff;
                        WILLIAMS_BLIT_TRANSPARENT(dest, srcdata, keepmask);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    /* right edge case */
                    srcdata = (pixdata << 4) & 0xf0;
                    shiftedmask = keepmask | 0x0f;
                    WILLIAMS_BLIT_TRANSPARENT(dest, srcdata, shiftedmask);

                    sstart += syadv;
                    dstart += dyadv;
                }
            }
        }
    };
    public static blitter_table_Ptr williams_blit_opaque_solid = new blitter_table_Ptr() {

        @Override
        public void handler(int sstart, int dstart, int w, int h, int data) {
            int source, sxadv, syadv;
            int dest, dxadv, dyadv;
            int i, j, solid;
            int keepmask;

            /* compute how much to advance in the x and y loops */
            sxadv = (data & 0x01) != 0 ? 0x100 : 1;
            syadv = (data & 0x01) != 0 ? 1 : w;
            dxadv = (data & 0x02) != 0 ? 0x100 : 1;
            dyadv = (data & 0x02) != 0 ? 1 : w;

            /* determine the common mask */
            keepmask = 0x00;
            if ((data & 0x80) != 0) {
                keepmask |= 0xf0;
            }
            if ((data & 0x40) != 0) {
                keepmask |= 0x0f;
            }
            if (keepmask == 0xff) {
                return;
            }

            /* set the solid pixel value to the mask value */
            solid = williams_blitterram.read(1);

            /* first case: no shifting */
            if ((data & 0x20) == 0) {
                /* loop over the height */
                for (i = 0; i < h; i++) {
                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* loop over the width */
                    for (j = w; j > 0; j--) {
                        int srcdata = cpu_readmem16(source);
                        WILLIAMS_BLIT_OPAQUE_SOLID(dest, srcdata, keepmask, solid);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    sstart += syadv;
                    dstart += dyadv;
                }
            } /* second case: shifted one pixel */ else {
                /* swap halves of the keep mask and the solid color */
                keepmask = ((keepmask & 0xf0) >> 4) | ((keepmask & 0x0f) << 4);
                solid = ((solid & 0xf0) >> 4) | ((solid & 0x0f) << 4);

                /* loop over the height */
                for (i = 0; i < h; i++) {
                    int pixdata, srcdata, shiftedmask;

                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* left edge case */
                    pixdata = cpu_readmem16(source);
                    srcdata = (pixdata >> 4) & 0x0f;
                    shiftedmask = keepmask | 0xf0;
                    WILLIAMS_BLIT_OPAQUE_SOLID(dest, srcdata, shiftedmask, solid);

                    source = (source + sxadv) & 0xffff;
                    dest = (dest + dxadv) & 0xffff;

                    /* loop over the width */
                    for (j = w - 1; j > 0; j--) {
                        pixdata = (pixdata << 8) | cpu_readmem16(source);
                        srcdata = (pixdata >> 4) & 0xff;
                        WILLIAMS_BLIT_OPAQUE_SOLID(dest, srcdata, keepmask, solid);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    /* right edge case */
                    srcdata = (pixdata << 4) & 0xf0;
                    shiftedmask = keepmask | 0x0f;
                    WILLIAMS_BLIT_OPAQUE_SOLID(dest, srcdata, shiftedmask, solid);

                    sstart += syadv;
                    dstart += dyadv;
                }
            }
        }
    };
    public static blitter_table_Ptr williams_blit_transparent_solid = new blitter_table_Ptr() {

        @Override
        public void handler(int sstart, int dstart, int w, int h, int data) {
            int source, sxadv, syadv;
            int dest, dxadv, dyadv;
            int i, j, solid;
            int keepmask;

            /* compute how much to advance in the x and y loops */
            sxadv = (data & 0x01) != 0 ? 0x100 : 1;
            syadv = (data & 0x01) != 0 ? 1 : w;
            dxadv = (data & 0x02) != 0 ? 0x100 : 1;
            dyadv = (data & 0x02) != 0 ? 1 : w;

            /* determine the common mask */
            keepmask = 0x00;
            if ((data & 0x80) != 0) {
                keepmask |= 0xf0;
            }
            if ((data & 0x40) != 0) {
                keepmask |= 0x0f;
            }
            if (keepmask == 0xff) {
                return;
            }

            /* set the solid pixel value to the mask value */
            solid = williams_blitterram.read(1);

            /* first case: no shifting */
            if ((data & 0x20) == 0) {
                /* loop over the height */
                for (i = 0; i < h; i++) {
                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* loop over the width */
                    for (j = w; j > 0; j--) {
                        int srcdata = cpu_readmem16(source);
                        BLIT_TRANSPARENT_SOLID_WILLIAMS(dest, srcdata, keepmask, solid);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    sstart += syadv;
                    dstart += dyadv;
                }
            } /* second case: shifted one pixel */ else {
                /* swap halves of the keep mask and the solid color */
                keepmask = ((keepmask & 0xf0) >> 4) | ((keepmask & 0x0f) << 4);
                solid = ((solid & 0xf0) >> 4) | ((solid & 0x0f) << 4);

                /* loop over the height */
                for (i = 0; i < h; i++) {
                    int pixdata, srcdata, shiftedmask;

                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* left edge case */
                    pixdata = cpu_readmem16(source);
                    srcdata = (pixdata >> 4) & 0x0f;
                    shiftedmask = keepmask | 0xf0;
                    BLIT_TRANSPARENT_SOLID_WILLIAMS(dest, srcdata, shiftedmask, solid);

                    source = (source + sxadv) & 0xffff;
                    dest = (dest + dxadv) & 0xffff;

                    /* loop over the width */
                    for (j = w - 1; j > 0; j--) {
                        pixdata = (pixdata << 8) | cpu_readmem16(source);
                        srcdata = (pixdata >> 4) & 0xff;
                        BLIT_TRANSPARENT_SOLID_WILLIAMS(dest, srcdata, keepmask, solid);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    /* right edge case */
                    srcdata = (pixdata << 4) & 0xf0;
                    shiftedmask = keepmask | 0x0f;
                    BLIT_TRANSPARENT_SOLID_WILLIAMS(dest, srcdata, shiftedmask, solid);

                    sstart += syadv;
                    dstart += dyadv;
                }
            }
        }
    };
    static blitter_table_Ptr williams_blitters[]
            = {
                williams_blit_opaque,
                williams_blit_transparent,
                williams_blit_opaque_solid,
                williams_blit_transparent_solid
            };

    public static blitter_table_Ptr sinistar_blit_opaque = new blitter_table_Ptr() {

        @Override
        public void handler(int sstart, int dstart, int w, int h, int data) {
            int source, sxadv, syadv;
            int dest, dxadv, dyadv;
            int i, j, solid;
            int keepmask;

            /* compute how much to advance in the x and y loops */
            sxadv = (data & 0x01) != 0 ? 0x100 : 1;
            syadv = (data & 0x01) != 0 ? 1 : w;
            dxadv = (data & 0x02) != 0 ? 0x100 : 1;
            dyadv = (data & 0x02) != 0 ? 1 : w;

            /* determine the common mask */
            keepmask = 0x00;
            if ((data & 0x80) != 0) {
                keepmask |= 0xf0;
            }
            if ((data & 0x40) != 0) {
                keepmask |= 0x0f;
            }
            if (keepmask == 0xff) {
                return;
            }

            /* set the solid pixel value to the mask value */
            solid = williams_blitterram.read(1);

            /* first case: no shifting */
            if ((data & 0x20) == 0) {
                /* loop over the height */
                for (i = 0; i < h; i++) {
                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* loop over the width */
                    for (j = w; j > 0; j--) {
                        int srcdata = cpu_readmem16(source);
                        SINISTAR_BLIT_OPAQUE(dest, srcdata, keepmask);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    sstart += syadv;
                    dstart += dyadv;
                }
            } /* second case: shifted one pixel */ else {
                /* swap halves of the keep mask and the solid color */
                keepmask = ((keepmask & 0xf0) >> 4) | ((keepmask & 0x0f) << 4);
                solid = ((solid & 0xf0) >> 4) | ((solid & 0x0f) << 4);

                /* loop over the height */
                for (i = 0; i < h; i++) {
                    int pixdata, srcdata, shiftedmask;

                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* left edge case */
                    pixdata = cpu_readmem16(source);
                    srcdata = (pixdata >> 4) & 0x0f;
                    shiftedmask = keepmask | 0xf0;
                    SINISTAR_BLIT_OPAQUE(dest, srcdata, shiftedmask);

                    source = (source + sxadv) & 0xffff;
                    dest = (dest + dxadv) & 0xffff;

                    /* loop over the width */
                    for (j = w - 1; j > 0; j--) {
                        pixdata = (pixdata << 8) | cpu_readmem16(source);
                        srcdata = (pixdata >> 4) & 0xff;
                        SINISTAR_BLIT_OPAQUE(dest, srcdata, keepmask);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    /* right edge case */
                    srcdata = (pixdata << 4) & 0xf0;
                    shiftedmask = keepmask | 0x0f;
                    SINISTAR_BLIT_OPAQUE(dest, srcdata, shiftedmask);

                    sstart += syadv;
                    dstart += dyadv;
                }
            }
        }
    };
    public static blitter_table_Ptr sinistar_blit_transparent = new blitter_table_Ptr() {

        @Override
        public void handler(int sstart, int dstart, int w, int h, int data) {
            int source, sxadv, syadv;
            int dest, dxadv, dyadv;
            int i, j, solid;
            int keepmask;

            /* compute how much to advance in the x and y loops */
            sxadv = (data & 0x01) != 0 ? 0x100 : 1;
            syadv = (data & 0x01) != 0 ? 1 : w;
            dxadv = (data & 0x02) != 0 ? 0x100 : 1;
            dyadv = (data & 0x02) != 0 ? 1 : w;

            /* determine the common mask */
            keepmask = 0x00;
            if ((data & 0x80) != 0) {
                keepmask |= 0xf0;
            }
            if ((data & 0x40) != 0) {
                keepmask |= 0x0f;
            }
            if (keepmask == 0xff) {
                return;
            }

            /* set the solid pixel value to the mask value */
            solid = williams_blitterram.read(1);

            /* first case: no shifting */
            if ((data & 0x20) == 0) {
                /* loop over the height */
                for (i = 0; i < h; i++) {
                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* loop over the width */
                    for (j = w; j > 0; j--) {
                        int srcdata = cpu_readmem16(source);
                        SINISTAR_BLIT_TRANSPARENT(dest, srcdata, keepmask);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    sstart += syadv;
                    dstart += dyadv;
                }
            } /* second case: shifted one pixel */ else {
                /* swap halves of the keep mask and the solid color */
                keepmask = ((keepmask & 0xf0) >> 4) | ((keepmask & 0x0f) << 4);
                solid = ((solid & 0xf0) >> 4) | ((solid & 0x0f) << 4);

                /* loop over the height */
                for (i = 0; i < h; i++) {
                    int pixdata, srcdata, shiftedmask;

                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* left edge case */
                    pixdata = cpu_readmem16(source);
                    srcdata = (pixdata >> 4) & 0x0f;
                    shiftedmask = keepmask | 0xf0;
                    SINISTAR_BLIT_TRANSPARENT(dest, srcdata, shiftedmask);

                    source = (source + sxadv) & 0xffff;
                    dest = (dest + dxadv) & 0xffff;

                    /* loop over the width */
                    for (j = w - 1; j > 0; j--) {
                        pixdata = (pixdata << 8) | cpu_readmem16(source);
                        srcdata = (pixdata >> 4) & 0xff;
                        SINISTAR_BLIT_TRANSPARENT(dest, srcdata, keepmask);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    /* right edge case */
                    srcdata = (pixdata << 4) & 0xf0;
                    shiftedmask = keepmask | 0x0f;
                    SINISTAR_BLIT_TRANSPARENT(dest, srcdata, shiftedmask);

                    sstart += syadv;
                    dstart += dyadv;
                }
            }
        }
    };
    public static blitter_table_Ptr sinistar_blit_opaque_solid = new blitter_table_Ptr() {

        @Override
        public void handler(int sstart, int dstart, int w, int h, int data) {
            int source, sxadv, syadv;
            int dest, dxadv, dyadv;
            int i, j, solid;
            int keepmask;

            /* compute how much to advance in the x and y loops */
            sxadv = (data & 0x01) != 0 ? 0x100 : 1;
            syadv = (data & 0x01) != 0 ? 1 : w;
            dxadv = (data & 0x02) != 0 ? 0x100 : 1;
            dyadv = (data & 0x02) != 0 ? 1 : w;

            /* determine the common mask */
            keepmask = 0x00;
            if ((data & 0x80) != 0) {
                keepmask |= 0xf0;
            }
            if ((data & 0x40) != 0) {
                keepmask |= 0x0f;
            }
            if (keepmask == 0xff) {
                return;
            }

            /* set the solid pixel value to the mask value */
            solid = williams_blitterram.read(1);

            /* first case: no shifting */
            if ((data & 0x20) == 0) {
                /* loop over the height */
                for (i = 0; i < h; i++) {
                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* loop over the width */
                    for (j = w; j > 0; j--) {
                        int srcdata = cpu_readmem16(source);
                        SINISTAR_BLIT_OPAQUE_SOLID(dest, srcdata, keepmask, solid);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    sstart += syadv;
                    dstart += dyadv;
                }
            } /* second case: shifted one pixel */ else {
                /* swap halves of the keep mask and the solid color */
                keepmask = ((keepmask & 0xf0) >> 4) | ((keepmask & 0x0f) << 4);
                solid = ((solid & 0xf0) >> 4) | ((solid & 0x0f) << 4);

                /* loop over the height */
                for (i = 0; i < h; i++) {
                    int pixdata, srcdata, shiftedmask;

                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* left edge case */
                    pixdata = cpu_readmem16(source);
                    srcdata = (pixdata >> 4) & 0x0f;
                    shiftedmask = keepmask | 0xf0;
                    SINISTAR_BLIT_OPAQUE_SOLID(dest, srcdata, shiftedmask, solid);

                    source = (source + sxadv) & 0xffff;
                    dest = (dest + dxadv) & 0xffff;

                    /* loop over the width */
                    for (j = w - 1; j > 0; j--) {
                        pixdata = (pixdata << 8) | cpu_readmem16(source);
                        srcdata = (pixdata >> 4) & 0xff;
                        SINISTAR_BLIT_OPAQUE_SOLID(dest, srcdata, keepmask, solid);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    /* right edge case */
                    srcdata = (pixdata << 4) & 0xf0;
                    shiftedmask = keepmask | 0x0f;
                    SINISTAR_BLIT_OPAQUE_SOLID(dest, srcdata, shiftedmask, solid);

                    sstart += syadv;
                    dstart += dyadv;
                }
            }
        }
    };
    public static blitter_table_Ptr sinistar_blit_transparent_solid = new blitter_table_Ptr() {

        @Override
        public void handler(int sstart, int dstart, int w, int h, int data) {
            int source, sxadv, syadv;
            int dest, dxadv, dyadv;
            int i, j, solid;
            int keepmask;

            /* compute how much to advance in the x and y loops */
            sxadv = (data & 0x01) != 0 ? 0x100 : 1;
            syadv = (data & 0x01) != 0 ? 1 : w;
            dxadv = (data & 0x02) != 0 ? 0x100 : 1;
            dyadv = (data & 0x02) != 0 ? 1 : w;

            /* determine the common mask */
            keepmask = 0x00;
            if ((data & 0x80) != 0) {
                keepmask |= 0xf0;
            }
            if ((data & 0x40) != 0) {
                keepmask |= 0x0f;
            }
            if (keepmask == 0xff) {
                return;
            }

            /* set the solid pixel value to the mask value */
            solid = williams_blitterram.read(1);

            /* first case: no shifting */
            if ((data & 0x20) == 0) {
                /* loop over the height */
                for (i = 0; i < h; i++) {
                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* loop over the width */
                    for (j = w; j > 0; j--) {
                        int srcdata = cpu_readmem16(source);
                        SINISTAR_BLIT_TRANSPARENT_SOLID(dest, srcdata, keepmask, solid);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    sstart += syadv;
                    dstart += dyadv;
                }
            } /* second case: shifted one pixel */ else {
                /* swap halves of the keep mask and the solid color */
                keepmask = ((keepmask & 0xf0) >> 4) | ((keepmask & 0x0f) << 4);
                solid = ((solid & 0xf0) >> 4) | ((solid & 0x0f) << 4);

                /* loop over the height */
                for (i = 0; i < h; i++) {
                    int pixdata, srcdata, shiftedmask;

                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* left edge case */
                    pixdata = cpu_readmem16(source);
                    srcdata = (pixdata >> 4) & 0x0f;
                    shiftedmask = keepmask | 0xf0;
                    SINISTAR_BLIT_TRANSPARENT_SOLID(dest, srcdata, shiftedmask, solid);

                    source = (source + sxadv) & 0xffff;
                    dest = (dest + dxadv) & 0xffff;

                    /* loop over the width */
                    for (j = w - 1; j > 0; j--) {
                        pixdata = (pixdata << 8) | cpu_readmem16(source);
                        srcdata = (pixdata >> 4) & 0xff;
                        SINISTAR_BLIT_TRANSPARENT_SOLID(dest, srcdata, keepmask, solid);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    /* right edge case */
                    srcdata = (pixdata << 4) & 0xf0;
                    shiftedmask = keepmask | 0x0f;
                    SINISTAR_BLIT_TRANSPARENT_SOLID(dest, srcdata, shiftedmask, solid);

                    sstart += syadv;
                    dstart += dyadv;
                }
            }
        }
    };

    static blitter_table_Ptr sinistar_blitters[]
            = {
                sinistar_blit_opaque,
                sinistar_blit_transparent,
                sinistar_blit_opaque_solid,
                sinistar_blit_transparent_solid
            };
    public static blitter_table_Ptr blaster_blit_opaque = new blitter_table_Ptr() {

        @Override
        public void handler(int sstart, int dstart, int w, int h, int data) {
            int source, sxadv, syadv;
            int dest, dxadv, dyadv;
            int i, j, solid;
            int keepmask;

            /* compute how much to advance in the x and y loops */
            sxadv = (data & 0x01) != 0 ? 0x100 : 1;
            syadv = (data & 0x01) != 0 ? 1 : w;
            dxadv = (data & 0x02) != 0 ? 0x100 : 1;
            dyadv = (data & 0x02) != 0 ? 1 : w;

            /* determine the common mask */
            keepmask = 0x00;
            if ((data & 0x80) != 0) {
                keepmask |= 0xf0;
            }
            if ((data & 0x40) != 0) {
                keepmask |= 0x0f;
            }
            if (keepmask == 0xff) {
                return;
            }

            /* set the solid pixel value to the mask value */
            solid = williams_blitterram.read(1);

            /* first case: no shifting */
            if ((data & 0x20) == 0) {
                /* loop over the height */
                for (i = 0; i < h; i++) {
                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* loop over the width */
                    for (j = w; j > 0; j--) {
                        int srcdata = cpu_readmem16(source);
                        BLASTER_BLIT_OPAQUE(dest, srcdata, keepmask);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    sstart += syadv;
                    dstart += dyadv;
                }
            } /* second case: shifted one pixel */ else {
                /* swap halves of the keep mask and the solid color */
                keepmask = ((keepmask & 0xf0) >> 4) | ((keepmask & 0x0f) << 4);
                solid = ((solid & 0xf0) >> 4) | ((solid & 0x0f) << 4);

                /* loop over the height */
                for (i = 0; i < h; i++) {
                    int pixdata, srcdata, shiftedmask;

                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* left edge case */
                    pixdata = cpu_readmem16(source);
                    srcdata = (pixdata >> 4) & 0x0f;
                    shiftedmask = keepmask | 0xf0;
                    BLASTER_BLIT_OPAQUE(dest, srcdata, shiftedmask);

                    source = (source + sxadv) & 0xffff;
                    dest = (dest + dxadv) & 0xffff;

                    /* loop over the width */
                    for (j = w - 1; j > 0; j--) {
                        pixdata = (pixdata << 8) | cpu_readmem16(source);
                        srcdata = (pixdata >> 4) & 0xff;
                        BLASTER_BLIT_OPAQUE(dest, srcdata, keepmask);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    /* right edge case */
                    srcdata = (pixdata << 4) & 0xf0;
                    shiftedmask = keepmask | 0x0f;
                    BLASTER_BLIT_OPAQUE(dest, srcdata, shiftedmask);

                    sstart += syadv;
                    dstart += dyadv;
                }
            }
        }
    };
    public static blitter_table_Ptr blaster_blit_transparent = new blitter_table_Ptr() {

        @Override
        public void handler(int sstart, int dstart, int w, int h, int data) {
            int source, sxadv, syadv;
            int dest, dxadv, dyadv;
            int i, j, solid;
            int keepmask;

            /* compute how much to advance in the x and y loops */
            sxadv = (data & 0x01) != 0 ? 0x100 : 1;
            syadv = (data & 0x01) != 0 ? 1 : w;
            dxadv = (data & 0x02) != 0 ? 0x100 : 1;
            dyadv = (data & 0x02) != 0 ? 1 : w;

            /* determine the common mask */
            keepmask = 0x00;
            if ((data & 0x80) != 0) {
                keepmask |= 0xf0;
            }
            if ((data & 0x40) != 0) {
                keepmask |= 0x0f;
            }
            if (keepmask == 0xff) {
                return;
            }

            /* set the solid pixel value to the mask value */
            solid = williams_blitterram.read(1);

            /* first case: no shifting */
            if ((data & 0x20) == 0) {
                /* loop over the height */
                for (i = 0; i < h; i++) {
                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* loop over the width */
                    for (j = w; j > 0; j--) {
                        int srcdata = cpu_readmem16(source);
                        BLASTER_BLIT_TRANSPARENT(dest, srcdata, keepmask);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    sstart += syadv;
                    dstart += dyadv;
                }
            } /* second case: shifted one pixel */ else {
                /* swap halves of the keep mask and the solid color */
                keepmask = ((keepmask & 0xf0) >> 4) | ((keepmask & 0x0f) << 4);
                solid = ((solid & 0xf0) >> 4) | ((solid & 0x0f) << 4);

                /* loop over the height */
                for (i = 0; i < h; i++) {
                    int pixdata, srcdata, shiftedmask;

                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* left edge case */
                    pixdata = cpu_readmem16(source);
                    srcdata = (pixdata >> 4) & 0x0f;
                    shiftedmask = keepmask | 0xf0;
                    BLASTER_BLIT_TRANSPARENT(dest, srcdata, shiftedmask);

                    source = (source + sxadv) & 0xffff;
                    dest = (dest + dxadv) & 0xffff;

                    /* loop over the width */
                    for (j = w - 1; j > 0; j--) {
                        pixdata = (pixdata << 8) | cpu_readmem16(source);
                        srcdata = (pixdata >> 4) & 0xff;
                        BLASTER_BLIT_TRANSPARENT(dest, srcdata, keepmask);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    /* right edge case */
                    srcdata = (pixdata << 4) & 0xf0;
                    shiftedmask = keepmask | 0x0f;
                    BLASTER_BLIT_TRANSPARENT(dest, srcdata, shiftedmask);

                    sstart += syadv;
                    dstart += dyadv;
                }
            }
        }
    };
    public static blitter_table_Ptr blaster_blit_opaque_solid = new blitter_table_Ptr() {

        @Override
        public void handler(int sstart, int dstart, int w, int h, int data) {
            int source, sxadv, syadv;
            int dest, dxadv, dyadv;
            int i, j, solid;
            int keepmask;

            /* compute how much to advance in the x and y loops */
            sxadv = (data & 0x01) != 0 ? 0x100 : 1;
            syadv = (data & 0x01) != 0 ? 1 : w;
            dxadv = (data & 0x02) != 0 ? 0x100 : 1;
            dyadv = (data & 0x02) != 0 ? 1 : w;

            /* determine the common mask */
            keepmask = 0x00;
            if ((data & 0x80) != 0) {
                keepmask |= 0xf0;
            }
            if ((data & 0x40) != 0) {
                keepmask |= 0x0f;
            }
            if (keepmask == 0xff) {
                return;
            }

            /* set the solid pixel value to the mask value */
            solid = williams_blitterram.read(1);

            /* first case: no shifting */
            if ((data & 0x20) == 0) {
                /* loop over the height */
                for (i = 0; i < h; i++) {
                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* loop over the width */
                    for (j = w; j > 0; j--) {
                        int srcdata = cpu_readmem16(source);
                        BLASTER_BLIT_OPAQUE_SOLID(dest, srcdata, keepmask, solid);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    sstart += syadv;
                    dstart += dyadv;
                }
            } /* second case: shifted one pixel */ else {
                /* swap halves of the keep mask and the solid color */
                keepmask = ((keepmask & 0xf0) >> 4) | ((keepmask & 0x0f) << 4);
                solid = ((solid & 0xf0) >> 4) | ((solid & 0x0f) << 4);

                /* loop over the height */
                for (i = 0; i < h; i++) {
                    int pixdata, srcdata, shiftedmask;

                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* left edge case */
                    pixdata = cpu_readmem16(source);
                    srcdata = (pixdata >> 4) & 0x0f;
                    shiftedmask = keepmask | 0xf0;
                    BLASTER_BLIT_OPAQUE_SOLID(dest, srcdata, shiftedmask, solid);

                    source = (source + sxadv) & 0xffff;
                    dest = (dest + dxadv) & 0xffff;

                    /* loop over the width */
                    for (j = w - 1; j > 0; j--) {
                        pixdata = (pixdata << 8) | cpu_readmem16(source);
                        srcdata = (pixdata >> 4) & 0xff;
                        BLASTER_BLIT_OPAQUE_SOLID(dest, srcdata, keepmask, solid);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    /* right edge case */
                    srcdata = (pixdata << 4) & 0xf0;
                    shiftedmask = keepmask | 0x0f;
                    BLASTER_BLIT_OPAQUE_SOLID(dest, srcdata, shiftedmask, solid);

                    sstart += syadv;
                    dstart += dyadv;
                }
            }
        }
    };
    public static blitter_table_Ptr blaster_blit_transparent_solid = new blitter_table_Ptr() {

        @Override
        public void handler(int sstart, int dstart, int w, int h, int data) {
            int source, sxadv, syadv;
            int dest, dxadv, dyadv;
            int i, j, solid;
            int keepmask;

            /* compute how much to advance in the x and y loops */
            sxadv = (data & 0x01) != 0 ? 0x100 : 1;
            syadv = (data & 0x01) != 0 ? 1 : w;
            dxadv = (data & 0x02) != 0 ? 0x100 : 1;
            dyadv = (data & 0x02) != 0 ? 1 : w;

            /* determine the common mask */
            keepmask = 0x00;
            if ((data & 0x80) != 0) {
                keepmask |= 0xf0;
            }
            if ((data & 0x40) != 0) {
                keepmask |= 0x0f;
            }
            if (keepmask == 0xff) {
                return;
            }

            /* set the solid pixel value to the mask value */
            solid = williams_blitterram.read(1);

            /* first case: no shifting */
            if ((data & 0x20) == 0) {
                /* loop over the height */
                for (i = 0; i < h; i++) {
                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* loop over the width */
                    for (j = w; j > 0; j--) {
                        int srcdata = cpu_readmem16(source);
                        BLASTER_BLIT_TRANSPARENT_SOLID(dest, srcdata, keepmask, solid);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    sstart += syadv;
                    dstart += dyadv;
                }
            } /* second case: shifted one pixel */ else {
                /* swap halves of the keep mask and the solid color */
                keepmask = ((keepmask & 0xf0) >> 4) | ((keepmask & 0x0f) << 4);
                solid = ((solid & 0xf0) >> 4) | ((solid & 0x0f) << 4);

                /* loop over the height */
                for (i = 0; i < h; i++) {
                    int pixdata, srcdata, shiftedmask;

                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* left edge case */
                    pixdata = cpu_readmem16(source);
                    srcdata = (pixdata >> 4) & 0x0f;
                    shiftedmask = keepmask | 0xf0;
                    BLASTER_BLIT_TRANSPARENT_SOLID(dest, srcdata, shiftedmask, solid);

                    source = (source + sxadv) & 0xffff;
                    dest = (dest + dxadv) & 0xffff;

                    /* loop over the width */
                    for (j = w - 1; j > 0; j--) {
                        pixdata = (pixdata << 8) | cpu_readmem16(source);
                        srcdata = (pixdata >> 4) & 0xff;
                        BLASTER_BLIT_TRANSPARENT_SOLID(dest, srcdata, keepmask, solid);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    /* right edge case */
                    srcdata = (pixdata << 4) & 0xf0;
                    shiftedmask = keepmask | 0x0f;
                    BLASTER_BLIT_TRANSPARENT_SOLID(dest, srcdata, shiftedmask, solid);

                    sstart += syadv;
                    dstart += dyadv;
                }
            }
        }
    };

    static blitter_table_Ptr blaster_blitters[]
            = {
                blaster_blit_opaque,
                blaster_blit_transparent,
                blaster_blit_opaque_solid,
                blaster_blit_transparent_solid
            };
    public static blitter_table_Ptr williams2_blit_opaque = new blitter_table_Ptr() {

        @Override
        public void handler(int sstart, int dstart, int w, int h, int data) {
            int source, sxadv, syadv;
            int dest, dxadv, dyadv;
            int i, j, solid;
            int keepmask;

            /* compute how much to advance in the x and y loops */
            sxadv = (data & 0x01) != 0 ? 0x100 : 1;
            syadv = (data & 0x01) != 0 ? 1 : w;
            dxadv = (data & 0x02) != 0 ? 0x100 : 1;
            dyadv = (data & 0x02) != 0 ? 1 : w;

            /* determine the common mask */
            keepmask = 0x00;
            if ((data & 0x80) != 0) {
                keepmask |= 0xf0;
            }
            if ((data & 0x40) != 0) {
                keepmask |= 0x0f;
            }
            if (keepmask == 0xff) {
                return;
            }

            /* set the solid pixel value to the mask value */
            solid = williams_blitterram.read(1);

            /* first case: no shifting */
            if ((data & 0x20) == 0) {
                /* loop over the height */
                for (i = 0; i < h; i++) {
                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* loop over the width */
                    for (j = w; j > 0; j--) {
                        int srcdata = cpu_readmem16(source);
                        WILLIAMS2_BLIT_OPAQUE(dest, srcdata, keepmask);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    sstart += syadv;
                    dstart += dyadv;
                }
            } /* second case: shifted one pixel */ else {
                /* swap halves of the keep mask and the solid color */
                keepmask = ((keepmask & 0xf0) >> 4) | ((keepmask & 0x0f) << 4);
                solid = ((solid & 0xf0) >> 4) | ((solid & 0x0f) << 4);

                /* loop over the height */
                for (i = 0; i < h; i++) {
                    int pixdata, srcdata, shiftedmask;

                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* left edge case */
                    pixdata = cpu_readmem16(source);
                    srcdata = (pixdata >> 4) & 0x0f;
                    shiftedmask = keepmask | 0xf0;
                    WILLIAMS2_BLIT_OPAQUE(dest, srcdata, shiftedmask);

                    source = (source + sxadv) & 0xffff;
                    dest = (dest + dxadv) & 0xffff;

                    /* loop over the width */
                    for (j = w - 1; j > 0; j--) {
                        pixdata = (pixdata << 8) | cpu_readmem16(source);
                        srcdata = (pixdata >> 4) & 0xff;
                        WILLIAMS2_BLIT_OPAQUE(dest, srcdata, keepmask);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    /* right edge case */
                    srcdata = (pixdata << 4) & 0xf0;
                    shiftedmask = keepmask | 0x0f;
                    WILLIAMS2_BLIT_OPAQUE(dest, srcdata, shiftedmask);

                    sstart += syadv;
                    dstart += dyadv;
                }
            }
        }
    };
    public static blitter_table_Ptr williams2_blit_transparent = new blitter_table_Ptr() {

        @Override
        public void handler(int sstart, int dstart, int w, int h, int data) {
            int source, sxadv, syadv;
            int dest, dxadv, dyadv;
            int i, j, solid;
            int keepmask;

            /* compute how much to advance in the x and y loops */
            sxadv = (data & 0x01) != 0 ? 0x100 : 1;
            syadv = (data & 0x01) != 0 ? 1 : w;
            dxadv = (data & 0x02) != 0 ? 0x100 : 1;
            dyadv = (data & 0x02) != 0 ? 1 : w;

            /* determine the common mask */
            keepmask = 0x00;
            if ((data & 0x80) != 0) {
                keepmask |= 0xf0;
            }
            if ((data & 0x40) != 0) {
                keepmask |= 0x0f;
            }
            if (keepmask == 0xff) {
                return;
            }

            /* set the solid pixel value to the mask value */
            solid = williams_blitterram.read(1);

            /* first case: no shifting */
            if ((data & 0x20) == 0) {
                /* loop over the height */
                for (i = 0; i < h; i++) {
                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* loop over the width */
                    for (j = w; j > 0; j--) {
                        int srcdata = cpu_readmem16(source);
                        WILLIAMS2_BLIT_TRANSPARENT(dest, srcdata, keepmask);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    sstart += syadv;
                    dstart += dyadv;
                }
            } /* second case: shifted one pixel */ else {
                /* swap halves of the keep mask and the solid color */
                keepmask = ((keepmask & 0xf0) >> 4) | ((keepmask & 0x0f) << 4);
                solid = ((solid & 0xf0) >> 4) | ((solid & 0x0f) << 4);

                /* loop over the height */
                for (i = 0; i < h; i++) {
                    int pixdata, srcdata, shiftedmask;

                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* left edge case */
                    pixdata = cpu_readmem16(source);
                    srcdata = (pixdata >> 4) & 0x0f;
                    shiftedmask = keepmask | 0xf0;
                    WILLIAMS2_BLIT_TRANSPARENT(dest, srcdata, shiftedmask);

                    source = (source + sxadv) & 0xffff;
                    dest = (dest + dxadv) & 0xffff;

                    /* loop over the width */
                    for (j = w - 1; j > 0; j--) {
                        pixdata = (pixdata << 8) | cpu_readmem16(source);
                        srcdata = (pixdata >> 4) & 0xff;
                        WILLIAMS2_BLIT_TRANSPARENT(dest, srcdata, keepmask);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    /* right edge case */
                    srcdata = (pixdata << 4) & 0xf0;
                    shiftedmask = keepmask | 0x0f;
                    WILLIAMS2_BLIT_TRANSPARENT(dest, srcdata, shiftedmask);

                    sstart += syadv;
                    dstart += dyadv;
                }
            }
        }
    };
    public static blitter_table_Ptr williams2_blit_opaque_solid = new blitter_table_Ptr() {

        @Override
        public void handler(int sstart, int dstart, int w, int h, int data) {
            int source, sxadv, syadv;
            int dest, dxadv, dyadv;
            int i, j, solid;
            int keepmask;

            /* compute how much to advance in the x and y loops */
            sxadv = (data & 0x01) != 0 ? 0x100 : 1;
            syadv = (data & 0x01) != 0 ? 1 : w;
            dxadv = (data & 0x02) != 0 ? 0x100 : 1;
            dyadv = (data & 0x02) != 0 ? 1 : w;

            /* determine the common mask */
            keepmask = 0x00;
            if ((data & 0x80) != 0) {
                keepmask |= 0xf0;
            }
            if ((data & 0x40) != 0) {
                keepmask |= 0x0f;
            }
            if (keepmask == 0xff) {
                return;
            }

            /* set the solid pixel value to the mask value */
            solid = williams_blitterram.read(1);

            /* first case: no shifting */
            if ((data & 0x20) == 0) {
                /* loop over the height */
                for (i = 0; i < h; i++) {
                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* loop over the width */
                    for (j = w; j > 0; j--) {
                        int srcdata = cpu_readmem16(source);
                        WILLIAMS2_BLIT_OPAQUE_SOLID(dest, srcdata, keepmask, solid);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    sstart += syadv;
                    dstart += dyadv;
                }
            } /* second case: shifted one pixel */ else {
                /* swap halves of the keep mask and the solid color */
                keepmask = ((keepmask & 0xf0) >> 4) | ((keepmask & 0x0f) << 4);
                solid = ((solid & 0xf0) >> 4) | ((solid & 0x0f) << 4);

                /* loop over the height */
                for (i = 0; i < h; i++) {
                    int pixdata, srcdata, shiftedmask;

                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* left edge case */
                    pixdata = cpu_readmem16(source);
                    srcdata = (pixdata >> 4) & 0x0f;
                    shiftedmask = keepmask | 0xf0;
                    WILLIAMS2_BLIT_OPAQUE_SOLID(dest, srcdata, shiftedmask, solid);

                    source = (source + sxadv) & 0xffff;
                    dest = (dest + dxadv) & 0xffff;

                    /* loop over the width */
                    for (j = w - 1; j > 0; j--) {
                        pixdata = (pixdata << 8) | cpu_readmem16(source);
                        srcdata = (pixdata >> 4) & 0xff;
                        WILLIAMS2_BLIT_OPAQUE_SOLID(dest, srcdata, keepmask, solid);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    /* right edge case */
                    srcdata = (pixdata << 4) & 0xf0;
                    shiftedmask = keepmask | 0x0f;
                    WILLIAMS2_BLIT_OPAQUE_SOLID(dest, srcdata, shiftedmask, solid);

                    sstart += syadv;
                    dstart += dyadv;
                }
            }
        }
    };
    public static blitter_table_Ptr williams2_blit_transparent_solid = new blitter_table_Ptr() {

        @Override
        public void handler(int sstart, int dstart, int w, int h, int data) {
            int source, sxadv, syadv;
            int dest, dxadv, dyadv;
            int i, j, solid;
            int keepmask;

            /* compute how much to advance in the x and y loops */
            sxadv = (data & 0x01) != 0 ? 0x100 : 1;
            syadv = (data & 0x01) != 0 ? 1 : w;
            dxadv = (data & 0x02) != 0 ? 0x100 : 1;
            dyadv = (data & 0x02) != 0 ? 1 : w;

            /* determine the common mask */
            keepmask = 0x00;
            if ((data & 0x80) != 0) {
                keepmask |= 0xf0;
            }
            if ((data & 0x40) != 0) {
                keepmask |= 0x0f;
            }
            if (keepmask == 0xff) {
                return;
            }

            /* set the solid pixel value to the mask value */
            solid = williams_blitterram.read(1);

            /* first case: no shifting */
            if ((data & 0x20) == 0) {
                /* loop over the height */
                for (i = 0; i < h; i++) {
                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* loop over the width */
                    for (j = w; j > 0; j--) {
                        int srcdata = cpu_readmem16(source);
                        WILLIAMS2_BLIT_TRANSPARENT_SOLID(dest, srcdata, keepmask, solid);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    sstart += syadv;
                    dstart += dyadv;
                }
            } /* second case: shifted one pixel */ else {
                /* swap halves of the keep mask and the solid color */
                keepmask = ((keepmask & 0xf0) >> 4) | ((keepmask & 0x0f) << 4);
                solid = ((solid & 0xf0) >> 4) | ((solid & 0x0f) << 4);

                /* loop over the height */
                for (i = 0; i < h; i++) {
                    int pixdata, srcdata, shiftedmask;

                    source = sstart & 0xffff;
                    dest = dstart & 0xffff;

                    /* left edge case */
                    pixdata = cpu_readmem16(source);
                    srcdata = (pixdata >> 4) & 0x0f;
                    shiftedmask = keepmask | 0xf0;
                    WILLIAMS2_BLIT_TRANSPARENT_SOLID(dest, srcdata, shiftedmask, solid);

                    source = (source + sxadv) & 0xffff;
                    dest = (dest + dxadv) & 0xffff;

                    /* loop over the width */
                    for (j = w - 1; j > 0; j--) {
                        pixdata = (pixdata << 8) | cpu_readmem16(source);
                        srcdata = (pixdata >> 4) & 0xff;
                        WILLIAMS2_BLIT_TRANSPARENT_SOLID(dest, srcdata, keepmask, solid);

                        source = (source + sxadv) & 0xffff;
                        dest = (dest + dxadv) & 0xffff;
                    }

                    /* right edge case */
                    srcdata = (pixdata << 4) & 0xf0;
                    shiftedmask = keepmask | 0x0f;
                    WILLIAMS2_BLIT_TRANSPARENT_SOLID(dest, srcdata, shiftedmask, solid);

                    sstart += syadv;
                    dstart += dyadv;
                }
            }
        }
    };
    static blitter_table_Ptr williams2_blitters[]
            = {
                williams2_blit_opaque,
                williams2_blit_transparent,
                williams2_blit_opaque_solid,
                williams2_blit_transparent_solid
            };
}
