/**
 * ported to v0.37b7
 */
package mame037b7;

import static common.ptr.*;
import static common.subArrays.*;
import static arcadeflex036.osdepend.*;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v037b16.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b16.mame.paletteH.*;
import static gr.codebb.arcadeflex.v037b16.mame.usrintrf.*;
import static mame037b16.mame.*;
//import static mame.artworkC.*;
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static arcadeflex036.video.*;
import static gr.codebb.arcadeflex.v037b16.mame.palette.colormode;
import static gr.codebb.arcadeflex.v037b16.mame.palette.palette_change_color_8;

public class palette {

    public static char[] game_palette;/* RGB palette as set by the driver. */
    public static UBytePtr new_palette;/* changes to the palette are stored here before being moved to game_palette by palette_recalc() */
    public static UBytePtr palette_dirty;
    /* arrays which keep track of colors actually used, to help in the palette shrinking. */
    public static UBytePtr palette_used_colors;
    public static UBytePtr old_used_colors;
    public static IntArray pen_visiblecount;
    public static IntArray pen_cachedcount;
    public static UBytePtr just_remapped;/* colors which have been remapped in this frame, returned by palette_recalc() */

    public static final int NO_16BIT = 0;
    public static final int STATIC_16BIT = 1;
    public static final int PALETTIZED_16BIT = 2;

    static int total_shrinked_pens;
    public static int[] shrinked_pens;
    public static /*UINT16*/ char[] shrinked_palette;
    public static /*UINT16*/ char[] palette_map;/* map indexes from game_palette to shrinked_palette */

    public static /*UINT16*/ char[] pen_usage_count = new char[DYNAMIC_MAX_PENS];

    public static /*UINT16*/ char palette_transparent_pen;
    public static int palette_transparent_color;

    public static final int BLACK_PEN = 0;
    public static final int TRANSPARENT_PEN = 1;
    public static final int RESERVED_PENS = 2;

    public static final int PALETTE_COLOR_NEEDS_REMAP = 0x80;

    /* helper macro for 16-bit mode */
    static int rgbpenindex(int r, int g, int b) {
        return ((Machine.scrbitmap.depth == 16) ? ((((r) >> 3) << 10) + (((g) >> 3) << 5) + ((b) >> 3)) : ((((r) >> 5) << 5) + (((g) >> 5) << 2) + ((b) >> 6)));
    }

    public static /*UINT16*/ char[] palette_shadow_table;

    public static int palette_start() {
        int i, num;
        game_palette = new char[3 * Machine.drv.total_colors];
        palette_map = new char[Machine.drv.total_colors * 2];
        if (Machine.drv.color_table_len != 0) {
            Machine.game_colortable = new char[Machine.drv.color_table_len * 2];
            Machine.remapped_colortable = new IntArray(Machine.drv.color_table_len * 2);

        } else {
            Machine.game_colortable = null;
            Machine.remapped_colortable = null;

        }
        if (Machine.color_depth == 16 || (Machine.gamedrv.flags & GAME_REQUIRES_16BIT) != 0) {
            if (Machine.color_depth == 8 || Machine.drv.total_colors > 65532) {
                colormode = STATIC_16BIT;
            } else {
                colormode = PALETTIZED_16BIT;
            }
        } else {
            colormode = NO_16BIT;
        }

        switch (colormode) {
            case NO_16BIT:
                if ((Machine.drv.video_attributes & VIDEO_MODIFIES_PALETTE) != 0) {
                    total_shrinked_pens = DYNAMIC_MAX_PENS;
                } else {
                    total_shrinked_pens = STATIC_MAX_PENS;
                }
                break;
            case STATIC_16BIT:
                total_shrinked_pens = 32768;
                break;
            case PALETTIZED_16BIT:
                total_shrinked_pens = Machine.drv.total_colors + RESERVED_PENS;
                break;
        }

        shrinked_pens = new int[total_shrinked_pens * 4];
        shrinked_palette = new char[3 * total_shrinked_pens];

        Machine.pens = new int[Machine.drv.total_colors * 2];

        if ((Machine.drv.video_attributes & VIDEO_MODIFIES_PALETTE) != 0) {
            /* if the palette changes dynamically, we'll need the usage arrays to help in shrinking. */
            palette_used_colors = new UBytePtr((1 + 1 + 1 + 3 + 1) * Machine.drv.total_colors);
            pen_visiblecount = new IntArray((int) (2 * Machine.drv.total_colors * 4));

            if (palette_used_colors == null || pen_visiblecount == null) {
                palette_stop();
                return 1;
            }
            old_used_colors = new UBytePtr(palette_used_colors, Machine.drv.total_colors);
            just_remapped = new UBytePtr(old_used_colors, Machine.drv.total_colors);
            new_palette = new UBytePtr(just_remapped, Machine.drv.total_colors);
            palette_dirty = new UBytePtr(new_palette, 3 * Machine.drv.total_colors);
            for (int mem = 0; mem < Machine.drv.total_colors; mem++) {
                palette_used_colors.write(mem, PALETTE_COLOR_USED);
            }
            for (int mem = 0; mem < Machine.drv.total_colors; mem++) {
                old_used_colors.write(mem, PALETTE_COLOR_UNUSED);
            }
            for (int mem = 0; mem < Machine.drv.total_colors; mem++) {
                palette_dirty.write(mem, 0);
            }
            pen_cachedcount = new IntArray(pen_visiblecount, (int) Machine.drv.total_colors);
            for (i = 0; i < Machine.drv.total_colors * 4; i++) {
                pen_visiblecount.write(i, 0);
                pen_cachedcount.write(i, 0);
            }
        } else {
            palette_used_colors = old_used_colors = just_remapped = new_palette = palette_dirty = null;
        }

        if (Machine.color_depth == 8) {
            num = 256;
        } else {
            num = 65536;
        }
        palette_shadow_table = new char[num * 2];
        if (palette_shadow_table == null) {
            palette_stop();
            return 1;
        }
        for (i = 0; i < num; i++) {
            palette_shadow_table[i] = (char) i;
        }

        if ((Machine.drv.color_table_len != 0 && (Machine.game_colortable == null || Machine.remapped_colortable == null))
                || game_palette == null || palette_map == null
                || shrinked_pens == null || shrinked_palette == null || Machine.pens == null) {
            palette_stop();
            return 1;
        }

        return 0;
    }

    public static void palette_stop() {
        palette_used_colors = old_used_colors = just_remapped = new_palette = palette_dirty = null;
        pen_visiblecount = null;
        game_palette = null;
        palette_map = null;
        Machine.game_colortable = null;
        Machine.remapped_colortable = null;
        shrinked_pens = null;
        shrinked_palette = null;
        Machine.pens = null;
        palette_shadow_table = null;
    }

    public static int palette_init() {
        int i;

        /* We initialize the palette and colortable to some default values so that drivers which dynamically change the palette don't need a vh_init_palette() function (provided the default color table fits their needs). */
        for (i = 0; i < Machine.drv.total_colors; i++) {
            game_palette[3 * i + 0] = (char) (((i & 1) >> 0) * 0xff);
            game_palette[3 * i + 1] = (char) (((i & 2) >> 1) * 0xff);
            game_palette[3 * i + 2] = (char) (((i & 4) >> 2) * 0xff);
        }

        /* Preload the colortable with a default setting, following the same order of the palette. The driver can overwrite this in vh_init_palette() */
        for (i = 0; i < Machine.drv.color_table_len; i++) {
            Machine.game_colortable[i] = (char) (i % Machine.drv.total_colors);
        }

        /* by default we use -1 to identify the transparent color, the driver */
 /* can modify this. */
        palette_transparent_color = -1;

        /* now the driver can modify the default values if it wants to. */
        if (Machine.drv.vh_init_palette != null) {
            (Machine.drv.vh_init_palette).handler(game_palette, Machine.game_colortable, memory_region(REGION_PROMS));
        }

        switch (colormode) {
            case NO_16BIT: {
                /* initialize shrinked palette to all black */
                for (i = 0; i < total_shrinked_pens; i++) {
                    shrinked_palette[3 * i + 0] = 0;
                    shrinked_palette[3 * i + 1] = 0;
                    shrinked_palette[3 * i + 2] = 0;
                }

                if ((Machine.drv.video_attributes & VIDEO_MODIFIES_PALETTE) != 0) {
                    /* initialize pen usage counters */
                    for (i = 0; i < DYNAMIC_MAX_PENS; i++) {
                        pen_usage_count[i] = 0;
                    }

                    /* allocate two fixed pens at the beginning: */
 /* transparent black */
                    pen_usage_count[TRANSPARENT_PEN] = 1;
                    /* so the pen will not be reused */

 /* non transparent black */
                    pen_usage_count[BLACK_PEN] = 1;

                    /* create some defaults associations of game colors to shrinked pens. */
 /* They will be dynamically modified at run time. */
                    for (i = 0; i < Machine.drv.total_colors; i++) {
                        palette_map[i] = (char) ((i & 7) + 8);
                    }

                    if (osd_allocate_colors(total_shrinked_pens, shrinked_palette, shrinked_pens, 1) != 0) {
                        return 1;
                    }
                } else {
                    int j, used;

                    logerror("shrinking %d colors palette...\n", Machine.drv.total_colors);

                    /* shrink palette to fit */
                    used = 0;

                    for (i = 0; i < Machine.drv.total_colors; i++) {
                        for (j = 0; j < used; j++) {
                            if (shrinked_palette[3 * j + 0] == game_palette[3 * i + 0]
                                    && shrinked_palette[3 * j + 1] == game_palette[3 * i + 1]
                                    && shrinked_palette[3 * j + 2] == game_palette[3 * i + 2]) {
                                break;
                            }
                        }

                        palette_map[i] = (char) j;

                        if (j == used) {
                            used++;
                            if (used > total_shrinked_pens) {
                                used = total_shrinked_pens;
                                palette_map[i] = (char) (total_shrinked_pens - 1);
                                usrintf_showmessage("cannot shrink static palette");
                                logerror("error: ran out of free pens to shrink the palette.\n");
                            } else {
                                shrinked_palette[3 * j + 0] = game_palette[3 * i + 0];
                                shrinked_palette[3 * j + 1] = game_palette[3 * i + 1];
                                shrinked_palette[3 * j + 2] = game_palette[3 * i + 2];
                            }
                        }
                    }

                    logerror("shrinked palette uses %d colors\n", used);

                    if (osd_allocate_colors(used, shrinked_palette, shrinked_pens, 0) != 0) {
                        return 1;
                    }
                }

                for (i = 0; i < Machine.drv.total_colors; i++) {
                    Machine.pens[i] = shrinked_pens[palette_map[i]];
                }

                palette_transparent_pen = (char)shrinked_pens[TRANSPARENT_PEN];
                /* for dynamic palette games */
            }
            break;

            case STATIC_16BIT: {
                char[] p = shrinked_palette;
                int r, g, b;
                int p_ptr = 0;
                if (Machine.scrbitmap.depth == 16) {
                    for (r = 0; r < 32; r++) {
                        for (g = 0; g < 32; g++) {
                            for (b = 0; b < 32; b++) {
                                p[p_ptr++] = (char) ((r << 3) | (r >> 2));
                                p[p_ptr++] = (char) ((g << 3) | (g >> 2));
                                p[p_ptr++] = (char) ((b << 3) | (b >> 2));
                            }
                        }
                    }

                    if (osd_allocate_colors(32768, shrinked_palette, shrinked_pens, 0) != 0) {
                        return 1;
                    }
                } else {
                    for (r = 0; r < 8; r++) {
                        for (g = 0; g < 8; g++) {
                            for (b = 0; b < 4; b++) {
                                p[p_ptr++] = (char) ((r << 5) | (r << 2) | (r >> 1));
                                p[p_ptr++] = (char) ((g << 5) | (g << 2) | (g >> 1));
                                p[p_ptr++] = (char) ((b << 6) | (b << 4) | (b << 2) | b);
                            }
                        }
                    }

                    if (osd_allocate_colors(256, shrinked_palette, shrinked_pens, 0) != 0) {
                        return 1;
                    }
                }

                for (i = 0; i < Machine.drv.total_colors; i++) {
                    r = game_palette[3 * i + 0];
                    g = game_palette[3 * i + 1];
                    b = game_palette[3 * i + 2];

                    Machine.pens[i] = shrinked_pens[rgbpenindex(r, g, b)];
                }

                palette_transparent_pen = (char)shrinked_pens[0];
                /* we are forced to use black for the transparent pen */
            }
            break;

            case PALETTIZED_16BIT: {
                for (i = 0; i < RESERVED_PENS; i++) {
                    shrinked_palette[3 * i + 0]
                            = shrinked_palette[3 * i + 1]
                            = shrinked_palette[3 * i + 2] = 0;
                }

                for (i = 0; i < Machine.drv.total_colors; i++) {
                    shrinked_palette[3 * (i + RESERVED_PENS) + 0] = game_palette[3 * i + 0];
                    shrinked_palette[3 * (i + RESERVED_PENS) + 1] = game_palette[3 * i + 1];
                    shrinked_palette[3 * (i + RESERVED_PENS) + 2] = game_palette[3 * i + 2];
                }

                if (osd_allocate_colors(total_shrinked_pens, shrinked_palette, shrinked_pens, (Machine.drv.video_attributes & VIDEO_MODIFIES_PALETTE)) != 0) {
                    return 1;
                }

                for (i = 0; i < Machine.drv.total_colors; i++) {
                    Machine.pens[i] = shrinked_pens[i + RESERVED_PENS];
                }

                palette_transparent_pen = (char)shrinked_pens[TRANSPARENT_PEN];
                /* for dynamic palette games */
            }
            break;
        }

        for (i = 0; i < Machine.drv.color_table_len; i++) {
            int color = Machine.game_colortable[i];

            /* check for invalid colors set by Machine.drv.vh_init_palette */
            if (color < Machine.drv.total_colors) {
                Machine.remapped_colortable.write(i, Machine.pens[color]);
            } else {
                usrintf_showmessage("colortable[%d] (=%d) out of range (total_colors = %d)", i, color, Machine.drv.total_colors);
            }
        }

        return 0;
    }

    public static void palette_change_color_16_static(int color, int red, int green, int blue) {
        if (color == palette_transparent_color) {
            int i;

            palette_transparent_pen = (char)shrinked_pens[rgbpenindex(red, green, blue)];

            if (color == -1) {
                return;
                /* by default, palette_transparent_color is -1 */
            }

            for (i = 0; i < Machine.drv.total_colors; i++) {
                if ((old_used_colors.read(i) & (PALETTE_COLOR_VISIBLE | PALETTE_COLOR_TRANSPARENT_FLAG))
                        == (PALETTE_COLOR_VISIBLE | PALETTE_COLOR_TRANSPARENT_FLAG)) {
                    old_used_colors.write(i, old_used_colors.read(i) | PALETTE_COLOR_NEEDS_REMAP);
                }
            }
        }
        if (game_palette[3 * color + 0] == red
                && game_palette[3 * color + 1] == green
                && game_palette[3 * color + 2] == blue) {
            return;
        }

        game_palette[3 * color + 0] = (char) (red & 0xFF);
        game_palette[3 * color + 1] = (char) (green & 0xFF);
        game_palette[3 * color + 2] = (char) (blue & 0xFF);

        if ((old_used_colors.read(color) & PALETTE_COLOR_VISIBLE) != 0) /* we'll have to reassign the color in palette_recalc() */ {
            old_used_colors.write(color, old_used_colors.read(color) | PALETTE_COLOR_NEEDS_REMAP);
        }

    }

    public static void palette_change_color_16_palettized(int color, int red, int green, int blue) {
        if (color == palette_transparent_color) {
            osd_modify_pen(palette_transparent_pen, red, green, blue);

            if (color == -1) {
                return;
                /* by default, palette_transparent_color is -1 */
            }
        }

        if (game_palette[3 * color + 0] == red
                && game_palette[3 * color + 1] == green
                && game_palette[3 * color + 2] == blue) {
            return;
        }

        /* Machine.pens[color] might have been remapped to transparent_pen, so I */
 /* use shrinked_pens[] directly */
        osd_modify_pen(shrinked_pens[color + RESERVED_PENS], red, green, blue);
        game_palette[3 * color + 0] = (char) (red & 0xFF);
        game_palette[3 * color + 1] = (char) (green & 0xFF);
        game_palette[3 * color + 2] = (char) (blue & 0xFF);
    }


    public static void palette_change_color(int color, int red, int green, int blue) {
        if ((Machine.drv.video_attributes & VIDEO_MODIFIES_PALETTE) == 0) {
            logerror("Error: palette_change_color() called, but VIDEO_MODIFIES_PALETTE not set.\n");
            return;
        }

        if (color >= Machine.drv.total_colors) {
            logerror("error: palette_change_color() called with color %d, but only %d allocated.\n", color, Machine.drv.total_colors);
            return;
        }

        switch (colormode) {
            case NO_16BIT:
                palette_change_color_8(color, red & 0xFF, green & 0xFF, blue & 0xFF);
                break;
            case STATIC_16BIT:
                palette_change_color_16_static(color, red & 0xFF, green & 0xFF, blue & 0xFF);
                break;
            case PALETTIZED_16BIT:
                palette_change_color_16_palettized(color, red & 0xFF, green & 0xFF, blue & 0xFF);
                break;
        }
    }

    public static void palette_increase_usage_count(int table_offset, int usage_mask, int color_flags) {
        if (palette_used_colors == null) {
            return;
        }
        while (usage_mask != 0) {
            if ((usage_mask & 1) != 0) {
                if ((color_flags & PALETTE_COLOR_VISIBLE) != 0) {
                    pen_visiblecount.write(Machine.game_colortable[table_offset], Machine.game_colortable[table_offset] + 1);
                }
                if ((color_flags & PALETTE_COLOR_CACHED) != 0) {
                    pen_cachedcount.write(Machine.game_colortable[table_offset], Machine.game_colortable[table_offset] + 1);
                }
            }
            table_offset++;
            usage_mask >>= 1;
        }
    }

    public static void palette_decrease_usage_count(int table_offset, int usage_mask, int color_flags) {
        if (palette_used_colors == null) {
            return;
        }

        while (usage_mask != 0) {
            if ((usage_mask & 1) != 0) {
                if ((color_flags & PALETTE_COLOR_VISIBLE) != 0) {
                    pen_visiblecount.write(Machine.game_colortable[table_offset], Machine.game_colortable[table_offset] - 1);
                }
                if ((color_flags & PALETTE_COLOR_CACHED) != 0) {
                    pen_cachedcount.write(Machine.game_colortable[table_offset], Machine.game_colortable[table_offset] - 1);
                }
            }
            table_offset++;
            usage_mask >>= 1;
        }
    }

    public static void palette_increase_usage_countx(int table_offset, int num_pens, UBytePtr pen_data, int color_flags) {
        int[] flag = new int[256];
        memset(flag, 0, 256);

        while ((num_pens--) != 0) {
            int pen = pen_data.read(num_pens);
            if (flag[pen] == 0) {
                if ((color_flags & PALETTE_COLOR_VISIBLE) != 0) {
                    pen_visiblecount.write(Machine.game_colortable[table_offset + pen], Machine.game_colortable[table_offset + pen] + 1);
                }
                if ((color_flags & PALETTE_COLOR_CACHED) != 0) {
                    pen_cachedcount.write(Machine.game_colortable[table_offset + pen], Machine.game_colortable[table_offset + pen] + 1);
                }
                flag[pen] = 1;
            }
        }
    }

    public static void palette_decrease_usage_countx(int table_offset, int num_pens, UBytePtr pen_data, int color_flags) {
        int[] flag = new int[256];
        memset(flag, 0, 256);

        while ((num_pens--) != 0) {
            int pen = pen_data.read(num_pens);
            if (flag[pen] == 0) {
                if ((color_flags & PALETTE_COLOR_VISIBLE) != 0) {
                    pen_visiblecount.write(Machine.game_colortable[table_offset + pen], Machine.game_colortable[table_offset + pen] - 1);
                }
                if ((color_flags & PALETTE_COLOR_CACHED) != 0) {
                    pen_cachedcount.write(Machine.game_colortable[table_offset + pen], Machine.game_colortable[table_offset + pen] - 1);
                }
                flag[pen] = 1;
            }
        }
    }

    public static void palette_init_used_colors() {
        int pen;

        /* if we are not dynamically reducing the palette, return immediately. */
        if (palette_used_colors == null) {
            return;
        }

        //memset(palette_used_colors,PALETTE_COLOR_UNUSED,Machine.drv.total_colors * sizeof(unsigned char));
        for (int i = 0; i < Machine.drv.total_colors; i++) {
            palette_used_colors.write(i, PALETTE_COLOR_UNUSED);
        }

        for (pen = 0; pen < Machine.drv.total_colors; pen++) {
            if (pen_visiblecount.read(pen) != 0) {
                palette_used_colors.write(pen, palette_used_colors.read(pen) | PALETTE_COLOR_VISIBLE);
            }
            if (pen_cachedcount.read(pen) != 0) {
                palette_used_colors.write(pen, palette_used_colors.read(pen) | PALETTE_COLOR_CACHED);
            }
        }
    }

    public static UBytePtr palette_recalc_16_static() {
        int i, color;
        int did_remap = 0;
        int need_refresh = 0;

        //memset(just_remapped,0,Machine.drv.total_colors * sizeof(unsigned char));
        for (int mem = 0; mem < Machine.drv.total_colors; mem++) {
            just_remapped.write(mem, 0);
        }

        for (color = 0; color < Machine.drv.total_colors; color++) {
            /* the comparison between palette_used_colors and old_used_colors also includes PALETTE_COLOR_NEEDS_REMAP which might have been set by palette_change_color() */
            if ((palette_used_colors.read(color) & PALETTE_COLOR_VISIBLE) != 0
                    && palette_used_colors.read(color) != old_used_colors.read(color)) {
                int r, g, b;
                did_remap = 1;
                if ((old_used_colors.read(color) & palette_used_colors.read(color) & PALETTE_COLOR_CACHED) != 0) {
                    /* the color was and still is cached, we'll have to redraw everything */
                    need_refresh = 1;
                    just_remapped.write(color, 1);
                }

                if ((palette_used_colors.read(color) & PALETTE_COLOR_TRANSPARENT_FLAG) != 0) {
                    Machine.pens[color] = palette_transparent_pen;
                } else {
                    r = game_palette[3 * color + 0];
                    g = game_palette[3 * color + 1];
                    b = game_palette[3 * color + 2];

                    Machine.pens[color] = shrinked_pens[rgbpenindex(r, g, b)];
                }
            }

            old_used_colors.write(color, palette_used_colors.read(color));
        }

        if (did_remap != 0) {
            /* rebuild the color lookup table */
            for (i = 0; i < Machine.drv.color_table_len; i++) {
                Machine.remapped_colortable.write(i, Machine.pens[Machine.game_colortable[i]]);
            }
        }

        if (need_refresh != 0) {
            return just_remapped;
        } else {
            return null;
        }
    }

    public static UBytePtr palette_recalc_16_palettized() {
        int i, color;
        int did_remap = 0;
        int need_refresh = 0;

        //memset(just_remapped,0,Machine.drv.total_colors * sizeof(unsigned char));
        for (int mem = 0; mem < Machine.drv.total_colors; mem++) {
            just_remapped.write(mem, 0);
        }

        for (color = 0; color < Machine.drv.total_colors; color++) {
            if ((palette_used_colors.read(color) & PALETTE_COLOR_TRANSPARENT_FLAG)
                    != (old_used_colors.read(color) & PALETTE_COLOR_TRANSPARENT_FLAG)) {
                did_remap = 1;
                if ((old_used_colors.read(color) & palette_used_colors.read(color) & PALETTE_COLOR_CACHED) != 0) {
                    /* the color was and still is cached, we'll have to redraw everything */
                    need_refresh = 1;
                    just_remapped.write(color, 1);
                }

                if ((palette_used_colors.read(color) & PALETTE_COLOR_TRANSPARENT_FLAG) != 0) {
                    Machine.pens[color] = palette_transparent_pen;
                } else {
                    Machine.pens[color] = shrinked_pens[color + RESERVED_PENS];
                }
            }

            old_used_colors.write(color, palette_used_colors.read(color));
        }
        if (did_remap != 0) {
            /* rebuild the color lookup table */
            for (i = 0; i < Machine.drv.color_table_len; i++) {
                Machine.remapped_colortable.write(i, Machine.pens[Machine.game_colortable[i]]);
            }
        }

        if (need_refresh != 0) {
            return just_remapped;
        } else {
            return null;
        }
    }

    /**
     * ****************************************************************************
     *
     * Commonly used palette RAM handling functions
     *
     *****************************************************************************
     */


/*    public static WriteHandlerPtr paletteram_RRRRGGGGBBBBIIII_word_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = paletteram.READ_WORD(offset);
            int newword = COMBINE_WORD(oldword, data);

            paletteram.WRITE_WORD(offset, newword);
            changecolor_RRRRGGGGBBBBIIII(offset / 2, newword);
        }
    };*/
    
    public static void palette_post_screen_update_cb()
    {
        //TODO
    }
}
