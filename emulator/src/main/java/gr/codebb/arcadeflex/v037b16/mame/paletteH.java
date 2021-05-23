/*
 * ported to v0.37b16
 * 
 */
package gr.codebb.arcadeflex.v037b16.mame;

public class paletteH {

    public static final int DYNAMIC_MAX_PENS = 254;/* the Mac cannot handle more than 254 dynamic pens */
    public static final int STATIC_MAX_PENS = 256;/* but 256 static pens can be handled */

    public static final int PALETTE_COLOR_UNUSED = 0;/* This color is not needed for this frame */
    public static final int PALETTE_COLOR_VISIBLE = 1;/* This color is currently visible */
    public static final int PALETTE_COLOR_CACHED = 2;/* This color is cached in temporary bitmaps (but */
    public static final int PALETTE_COLOR_TRANSPARENT_FLAG = 4;/* All colors using this attribute will be */

    public static final int PALETTE_COLOR_USED = (PALETTE_COLOR_VISIBLE | PALETTE_COLOR_CACHED);
    public static final int PALETTE_COLOR_TRANSPARENT = (PALETTE_COLOR_TRANSPARENT_FLAG | PALETTE_COLOR_USED);

}
