/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b16.vidhrdw;

//generic imports
import static common.libc.expressions.NOT;
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b16.mame.osdependH.*;
//to be organized
import common.ptr.UBytePtr;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.REGION_USER2;
import static gr.codebb.arcadeflex.v056.mame.timer.timer_remove;
import static mame037b16.drawgfx.drawgfx;
import static mame037b16.drawgfx.fillbitmap;
import static mame037b16.drawgfx.plot_pixel;
import static mame037b16.mame.Machine;

public class galaxian {

    static rectangle _spritevisiblearea = new rectangle(
            2 * 8 + 1, 32 * 8 - 1,
            2 * 8, 30 * 8 - 1
    );
    static rectangle _spritevisibleareaflipx = new rectangle(
            0 * 8, 30 * 8 - 2,
            2 * 8, 30 * 8 - 1
    );

    static rectangle spritevisiblearea;
    static rectangle spritevisibleareaflipx;

    public static final int STARS_COLOR_BASE = 32;
    public static final int BULLETS_COLOR_BASE = (STARS_COLOR_BASE + 64);
    public static final int BACKGROUND_COLOR_BASE = (BULLETS_COLOR_BASE + 2);

    public static UBytePtr galaxian_videoram = new UBytePtr();
    public static UBytePtr galaxian_spriteram = new UBytePtr();
    public static UBytePtr galaxian_attributesram = new UBytePtr();
    public static UBytePtr galaxian_bulletsram = new UBytePtr();

    public static int[] galaxian_spriteram_size = new int[1];
    public static int[] galaxian_bulletsram_size = new int[1];

    static int mooncrst_gfxextend;
    static int[] pisces_gfxbank = new int[1];
    static int[] jumpbug_gfxbank = new int[5];

    public static abstract interface modify_charcodePtr {

        public abstract void handler(int[] code, int x);
    }

    public static modify_spritecodePtr modify_spritecode;

    public static abstract interface modify_spritecodePtr {

        public abstract void handler(int[] code, int[] flipx, int[] flipy, int offs);
    }

    public static modify_charcodePtr modify_charcode;
    /*TODO*///	
/*TODO*///	static void (*modify_color)(int*);	/* function to call to do modify how the color codes map to the PROM */
/*TODO*///	static void frogger_modify_color(int *code);
/*TODO*///	
/*TODO*///	static void (*modify_ypos)(UINT8*);	/* function to call to do modify how vertical positioning bits are connected */
/*TODO*///	static void frogger_modify_ypos(UINT8 *sy);
/*TODO*///	
    /* star circuit */
    static final int MAX_STARS = 250;

    static class star {

        public star() {
        }
        public int x, y, color;
    };
    static star stars[] = new star[MAX_STARS];

    static {
        for (int k = 0; k < MAX_STARS; k++) {
            stars[k] = new star();
        }
    }
    static int galaxian_stars_on;
    static int stars_blink_state;
    static Object stars_blink_timer = null;

    public static abstract interface draw_starsPtr {

        public abstract void handler(osd_bitmap bitmap);
    }

    public static draw_starsPtr draw_stars;/* function to call to draw the star layer */

 /* bullets circuit */
    static int darkplnt_bullet_color;

    public static abstract interface draw_bulletsPtr {

        public abstract void handler(osd_bitmap bitmap, int offs, int x, int y);
    }

    public static draw_bulletsPtr draw_bullets;/* function to call to draw a bullet */

 /* background circuit */
    static int background_red, background_green, background_blue;
    static int background_start_pen;

    public static abstract interface draw_backgroundPtr {

        public abstract void handler(osd_bitmap bitmap);
    }

    public static draw_backgroundPtr draw_background;/* function to call to draw the background */

    public static VhConvertColorPromPtr galaxian_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            int[] p_ptr = new int[1];
            for (i = 0; i < 32; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_ptr[0]++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_ptr[0]++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* blue component */
                bit0 = (color_prom.read() >> 6) & 0x01;
                bit1 = (color_prom.read() >> 7) & 0x01;
                palette[p_ptr[0]++] = (char) (0x4f * bit0 + 0xa8 * bit1);

                color_prom.inc();
            }

            galaxian_init_stars(palette, p_ptr);

            /* bullets - yellow and white */
            palette[p_ptr[0]++] = (char) (0xef);
            palette[p_ptr[0]++] = (char) (0xef);
            palette[p_ptr[0]++] = (char) (0x00);

            palette[p_ptr[0]++] = (char) (0xef);
            palette[p_ptr[0]++] = (char) (0xef);
            palette[p_ptr[0]++] = (char) (0xef);

            /* black background */
            background_start_pen = BACKGROUND_COLOR_BASE;

            palette[p_ptr[0]++] = (char) (0);
            palette[p_ptr[0]++] = (char) (0);
            palette[p_ptr[0]++] = (char) (0);
        }
    };

    public static VhConvertColorPromPtr scramble_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            galaxian_vh_convert_color_prom.handler(palette, colortable, color_prom);

            /* blue background - 390 ohm resistor */
            palette[(background_start_pen + 1) * 3 + 0] = (char) 0;
            palette[(background_start_pen + 1) * 3 + 1] = (char) 0;
            palette[(background_start_pen + 1) * 3 + 2] = (char) 0x56;
        }
    };

    public static VhConvertColorPromPtr moonwar_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            scramble_vh_convert_color_prom.handler(palette, colortable, color_prom);

            /* wire mod to connect the bullet blue output to the 220 ohm resistor */
            palette[BULLETS_COLOR_BASE * 3 + 2] = (char) 0x97;
        }
    };

    public static VhConvertColorPromPtr turtles_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            galaxian_vh_convert_color_prom.handler(palette, colortable, color_prom);

            /*  The background color generator is connected this way:
	
			RED   - 390 ohm resistor
			GREEN - 470 ohm resistor
			BLUE  - 390 ohm resistor */
            for (i = 0; i < 8; i++) {
                palette[(background_start_pen + i) * 3 + 0] = (i & 0x01) != 0 ? (char) 0x55 : (char) 0x00;
                palette[(background_start_pen + i) * 3 + 1] = (i & 0x02) != 0 ? (char) 0x47 : (char) 0x00;
                palette[(background_start_pen + i) * 3 + 2] = (i & 0x04) != 0 ? (char) 0x55 : (char) 0x00;
            }
        }
    };

    public static VhConvertColorPromPtr stratgyx_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            galaxian_vh_convert_color_prom.handler(palette, colortable, color_prom);

            /*  The background color generator is connected this way:
	
			RED   - 270 ohm resistor
			GREEN - 560 ohm resistor
			BLUE  - 470 ohm resistor */
            for (i = 0; i < 8; i++) {
                palette[(background_start_pen + i) * 3 + 0] = (i & 0x01) != 0 ? (char) 0x7c : (char) 0x00;
                palette[(background_start_pen + i) * 3 + 1] = (i & 0x02) != 0 ? (char) 0x3c : (char) 0x00;
                palette[(background_start_pen + i) * 3 + 2] = (i & 0x04) != 0 ? (char) 0x47 : (char) 0x00;
            }
        }
    };

    public static VhConvertColorPromPtr frogger_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            galaxian_vh_convert_color_prom.handler(palette, colortable, color_prom);

            /* blue background - 470 ohm resistor */
            palette[(background_start_pen + 1) * 3 + 0] = (char) 0;
            palette[(background_start_pen + 1) * 3 + 1] = (char) 0;
            palette[(background_start_pen + 1) * 3 + 2] = (char) 0x47;
        }
    };

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Dark Planet has one 32 bytes palette PROM, connected to the RGB output
     * this way:
     *
     * bit 5 -- 220 ohm resistor -- BLUE -- 470 ohm resistor -- BLUE -- 1 kohm
     * resistor -- BLUE -- 220 ohm resistor -- RED -- 470 ohm resistor -- RED
     * bit 0 -- 1 kohm resistor -- RED
     *
     * The bullet RGB outputs go through 100 ohm resistors.
     *
     * The RGB outputs have a 470 ohm pull-down each.
     *
     **************************************************************************
     */
    public static VhConvertColorPromPtr darkplnt_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            /*TODO*///		int i;
/*TODO*///	
/*TODO*///	
/*TODO*///		/* first, the character/sprite palette */
/*TODO*///                int p_ptr=0;
/*TODO*///		for (i = 0;i < 32;i++)
/*TODO*///		{
/*TODO*///			int bit0,bit1,bit2;
/*TODO*///	
/*TODO*///			/* red component */
/*TODO*///			bit0 = (color_prom.read() >> 0) & 0x01;
/*TODO*///			bit1 = (color_prom.read() >> 1) & 0x01;
/*TODO*///			bit2 = (color_prom.read() >> 2) & 0x01;
/*TODO*///			palette[p_ptr++] = (char)(0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
/*TODO*///			/* green component */
/*TODO*///			palette[p_ptr++] = (char)(0x00);
/*TODO*///			/* blue component */
/*TODO*///			bit0 = (color_prom.read() >> 3) & 0x01;
/*TODO*///			bit1 = (color_prom.read() >> 4) & 0x01;
/*TODO*///			bit2 = (color_prom.read() >> 5) & 0x01;
/*TODO*///			palette[p_ptr++] = (char)(0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
/*TODO*///	
/*TODO*///			color_prom++;
/*TODO*///		}
/*TODO*///	
/*TODO*///	
/*TODO*///		/* bullets - red and blue */
/*TODO*///	
/*TODO*///		*(palette++) = 0xef;
/*TODO*///		*(palette++) = 0x00;
/*TODO*///		*(palette++) = 0x00;
/*TODO*///	
/*TODO*///		*(palette++) = 0x00;
/*TODO*///		*(palette++) = 0x00;
/*TODO*///		*(palette++) = 0xef;
/*TODO*///	
/*TODO*///	
/*TODO*///		/* black background */
/*TODO*///	
/*TODO*///		background_start_pen = 34;
/*TODO*///	
/*TODO*///		*(palette++) = 0;
/*TODO*///		*(palette++) = 0;
/*TODO*///		*(palette++) = 0;
        }
    };

    public static VhConvertColorPromPtr minefld_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            galaxian_vh_convert_color_prom.handler(palette, colortable, color_prom);

            /* set up background colors */
 /* graduated blue */
            for (i = 0; i < 64; i++) {
                palette[(background_start_pen + i) * 3 + 0] = (char) 0;
                palette[(background_start_pen + i) * 3 + 1] = (char) (i * 2);
                palette[(background_start_pen + i) * 3 + 2] = (char) (i * 4);
            }

            /* graduated brown */
            for (i = 0; i < 64; i++) {
                palette[(background_start_pen + 64 + i) * 3 + 0] = (char) (i * 3);
                palette[(background_start_pen + 64 + i) * 3 + 1] = (char) (i * 1.5);
                palette[(background_start_pen + 64 + i) * 3 + 2] = (char) (i);
            }
        }
    };

    public static VhConvertColorPromPtr rescue_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            galaxian_vh_convert_color_prom.handler(palette, colortable, color_prom);

            /* set up background colors */
 /* graduated blue */
            for (i = 0; i < 64; i++) {
                palette[(background_start_pen + i) * 3 + 0] = (char) 0;
                palette[(background_start_pen + i) * 3 + 1] = (char) (i * 2);
                palette[(background_start_pen + i) * 3 + 2] = (char) (i * 4);
            }
        }
    };

    public static VhConvertColorPromPtr mariner_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            /*TODO*///		int i;
/*TODO*///	
/*TODO*///	
/*TODO*///	    galaxian_vh_convert_color_prom(palette, colortable, color_prom);
/*TODO*///	
/*TODO*///	
/*TODO*///		/* set up background colors */
/*TODO*///	
/*TODO*///	   	/* 16 shades of blue - the 4 bits are connected to the following resistors
/*TODO*///	
/*TODO*///	  		bit 0 -- 4.7 kohm resistor
/*TODO*///	        	  -- 2.2 kohm resistor
/*TODO*///	        	  -- 1   kohm resistor
/*TODO*///	  		bit 0 -- .47 kohm resistor */
/*TODO*///	
/*TODO*///	   	for (i = 0; i < 16; i++)
/*TODO*///	    {
/*TODO*///			int bit0,bit1,bit2,bit3;
/*TODO*///	
/*TODO*///			bit0 = (i >> 0) & 0x01;
/*TODO*///			bit1 = (i >> 1) & 0x01;
/*TODO*///			bit2 = (i >> 2) & 0x01;
/*TODO*///			bit3 = (i >> 3) & 0x01;
/*TODO*///	
/*TODO*///			palette[(background_start_pen + i) * 3 + 0] = 0;
/*TODO*///	       	palette[(background_start_pen + i) * 3 + 1] = 0;
/*TODO*///	       	palette[(background_start_pen + i) * 3 + 2] = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
/*TODO*///	    }
        }
    };
    /*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///	  Start the video hardware emulation.
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
    public static VhStartPtr galaxian_plain_vh_start = new VhStartPtr() {
        public int handler() {
            /*TODO*///		extern struct GameDriver driver_newsin7;
/*TODO*///	

            modify_charcode = null;
            modify_spritecode = null;
            /*TODO*///	    modify_color = 0;
/*TODO*///	    modify_ypos = 0;
/*TODO*///	
            mooncrst_gfxextend = 0;

            draw_bullets = null;

            draw_background = null;
            background_red = 0;
            background_green = 0;
            background_blue = 0;

            flip_screen_x_set(0);
            flip_screen_y_set(0);

            /*TODO*///		/* all the games except New Sinbad 7 clip the sprites at the top of the screen,
/*TODO*///		   New Sinbad 7 does it at the bottom */
/*TODO*///		if (Machine.gamedrv == &driver_newsin7)
/*TODO*///		{
/*TODO*///			spritevisiblearea      = &_spritevisibleareaflipx;
/*TODO*///	        spritevisibleareaflipx = &_spritevisiblearea;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
            spritevisiblearea = _spritevisiblearea;
            spritevisibleareaflipx = _spritevisibleareaflipx;
            /*TODO*///		}

            return 0;
        }
    };

    public static VhStartPtr galaxian_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = galaxian_plain_vh_start.handler();

            /*TODO*///		draw_stars = galaxian_draw_stars;
            draw_bullets = galaxian_draw_bullets;

            return ret;
        }
    };

    public static VhStartPtr mooncrst_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = galaxian_vh_start.handler();

            modify_charcode = mooncrst_modify_charcode;
            modify_spritecode = mooncrst_modify_spritecode;

            return ret;
        }
    };

    public static VhStartPtr moonqsr_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = galaxian_vh_start.handler();

            modify_charcode = moonqsr_modify_charcode;
            modify_spritecode = moonqsr_modify_spritecode;

            return ret;
        }
    };

    public static VhStartPtr pisces_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = galaxian_vh_start.handler();

            modify_charcode = pisces_modify_charcode;
            modify_spritecode = pisces_modify_spritecode;

            return ret;
        }
    };

    public static VhStartPtr batman2_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = galaxian_vh_start.handler();

            modify_charcode = batman2_modify_charcode;
            modify_spritecode = batman2_modify_spritecode;

            return ret;
        }
    };

    public static VhStartPtr scramble_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = galaxian_plain_vh_start.handler();

            /*TODO*///		draw_stars = scramble_draw_stars;
            draw_bullets = scramble_draw_bullets;

            draw_background = scramble_draw_background;

            return ret;
        }
    };

    public static VhStartPtr turtles_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = scramble_vh_start.handler();

            draw_background = turtles_draw_background;

            return ret;
        }
    };

    public static VhStartPtr theend_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = scramble_vh_start.handler();

            draw_bullets = theend_draw_bullets;

            return ret;
        }
    };

    public static VhStartPtr darkplnt_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = galaxian_plain_vh_start.handler();

            draw_bullets = darkplnt_draw_bullets;

            return ret;
        }
    };
    /*TODO*///	
/*TODO*///	public static VhStartPtr rescue_vh_start = new VhStartPtr() { public int handler() 
/*TODO*///	{
/*TODO*///		int ret = scramble_vh_start();
/*TODO*///	
/*TODO*///		draw_stars = rescue_draw_stars;
/*TODO*///	
/*TODO*///		draw_background = rescue_draw_background;
/*TODO*///	
/*TODO*///	    return ret;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static VhStartPtr minefld_vh_start = new VhStartPtr() { public int handler() 
/*TODO*///	{
/*TODO*///		int ret = scramble_vh_start.handler();
/*TODO*///	
/*TODO*///		draw_stars = rescue_draw_stars;
/*TODO*///	
/*TODO*///		draw_background = minefld_draw_background;
/*TODO*///	
/*TODO*///	    return ret;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static VhStartPtr stratgyx_vh_start = new VhStartPtr() { public int handler() 
/*TODO*///	{
/*TODO*///		int ret = galaxian_plain_vh_start();
/*TODO*///	
/*TODO*///		draw_background = stratgyx_draw_background;
/*TODO*///	
/*TODO*///	    return ret;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static VhStartPtr ckongs_vh_start = new VhStartPtr() { public int handler() 
/*TODO*///	{
/*TODO*///		int ret = scramble_vh_start.handler();
/*TODO*///	
/*TODO*///		modify_spritecode = ckongs_modify_spritecode;
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static VhStartPtr calipso_vh_start = new VhStartPtr() { public int handler() 
/*TODO*///	{
/*TODO*///		int ret = galaxian_plain_vh_start.handler();
/*TODO*///	
/*TODO*///		draw_bullets = scramble_draw_bullets;
/*TODO*///	
/*TODO*///		draw_background = scramble_draw_background;
/*TODO*///	
/*TODO*///		modify_spritecode = calipso_modify_spritecode;
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static VhStartPtr mariner_vh_start = new VhStartPtr() { public int handler() 
/*TODO*///	{
/*TODO*///		int ret = galaxian_plain_vh_start.handler();
/*TODO*///	
/*TODO*///		draw_stars = mariner_draw_stars;
/*TODO*///	
/*TODO*///		draw_bullets = scramble_draw_bullets;
/*TODO*///	
/*TODO*///		draw_background = mariner_draw_background;
/*TODO*///	
/*TODO*///		modify_charcode = mariner_modify_charcode;
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static VhStartPtr froggers_vh_start = new VhStartPtr() { public int handler() 
/*TODO*///	{
/*TODO*///		int ret = galaxian_plain_vh_start.handler();
/*TODO*///	
/*TODO*///		draw_background = frogger_draw_background;
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static VhStartPtr frogger_vh_start = new VhStartPtr() { public int handler() 
/*TODO*///	{
/*TODO*///		int ret = froggers_vh_start.handler();
/*TODO*///	
/*TODO*///		modify_color = frogger_modify_color;
/*TODO*///		modify_ypos = frogger_modify_ypos;
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static VhStartPtr froggrmc_vh_start = new VhStartPtr() { public int handler() 
/*TODO*///	{
/*TODO*///		int ret = froggers_vh_start.handler();
/*TODO*///	
/*TODO*///		modify_color = frogger_modify_color;
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	} };

    public static VhStartPtr jumpbug_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = scramble_vh_start.handler();
            /*TODO*///	
/*TODO*///		draw_stars = jumpbug_draw_stars;

            modify_charcode = jumpbug_modify_charcode;
            modify_spritecode = jumpbug_modify_spritecode;

            return ret;
        }
    };

    public static WriteHandlerPtr galaxian_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            galaxian_videoram.write(offset, data);
        }
    };

    public static ReadHandlerPtr galaxian_videoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return galaxian_videoram.read(offset);
        }
    };

    public static WriteHandlerPtr galaxian_flip_screen_x_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            flip_screen_x_set(data);
        }
    };

    public static WriteHandlerPtr galaxian_flip_screen_y_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            flip_screen_y_set(data);
        }
    };

    public static WriteHandlerPtr scramble_background_red_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            background_red = data & 1;
        }
    };

    public static WriteHandlerPtr scramble_background_green_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            background_green = data & 1;
        }
    };

    public static WriteHandlerPtr scramble_background_blue_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            background_blue = data & 1;
        }
    };

    public static WriteHandlerPtr galaxian_stars_enable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            galaxian_stars_on = data & 1;
        }
    };

    public static WriteHandlerPtr darkplnt_bullet_color_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            darkplnt_bullet_color = data & 1;
        }
    };

    public static WriteHandlerPtr mooncrst_gfxextend_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (data != 0) {
                mooncrst_gfxextend |= (1 << offset);
            } else {
                mooncrst_gfxextend &= ~(1 << offset);
            }
        }
    };

    public static WriteHandlerPtr mooncrgx_gfxextend_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* for the Moon Cresta bootleg on Galaxian H/W the gfx_extend is
	     located at 0x6000-0x6002.  Also, 0x6000 and 0x6001 are reversed. */
            if (offset == 1) {
                offset = 0;
            } else if (offset == 0) {
                offset = 1;
                /* switch 0x6000 and 0x6001 */
            }
            mooncrst_gfxextend_w.handler(offset, data);
        }
    };

    public static WriteHandlerPtr pisces_gfxbank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            set_vh_global_attribute(pisces_gfxbank, data & 1);
        }
    };

    public static WriteHandlerPtr jumpbug_gfxbank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            set_vh_global_attribute(jumpbug_gfxbank, offset, data & 1);
        }
    };

    /* Character banking routines */
    public static modify_charcodePtr mooncrst_modify_charcode = new modify_charcodePtr() {
        public void handler(int[] charcode, int offs) {
            if ((mooncrst_gfxextend & 4) != 0 && (charcode[0] & 0xc0) == 0x80) {
                charcode[0] = (charcode[0] & 0x3f) | (mooncrst_gfxextend << 6);
            }
        }
    };
    public static modify_charcodePtr moonqsr_modify_charcode = new modify_charcodePtr() {
        public void handler(int[] code, int x) {
            if ((galaxian_attributesram.read((x << 1) | 1) & 0x20) != 0) {
                code[0] += 256;
            }

            mooncrst_modify_charcode.handler(code, x);
        }
    };

    public static modify_charcodePtr pisces_modify_charcode = new modify_charcodePtr() {
        public void handler(int[] charcode, int offs) {
            if (pisces_gfxbank[0] != 0) {
                charcode[0] += 256;
            }
        }
    };

    public static modify_charcodePtr batman2_modify_charcode = new modify_charcodePtr() {
        public void handler(int[] charcode, int offs) {
            if ((charcode[0] & 0x80) != 0 && pisces_gfxbank[0] != 0) {
                charcode[0] += 256;
            }
        }
    };

    public static modify_charcodePtr mariner_modify_charcode = new modify_charcodePtr() {
        public void handler(int[] code, int x) {
            UBytePtr prom;
            /* bit 0 of the PROM controls character banking */
            prom = memory_region(REGION_USER2);

            if ((prom.read(x) & 0x01) != 0) {
                code[0] += 256;
            }
        }
    };

    public static modify_charcodePtr jumpbug_modify_charcode = new modify_charcodePtr() {
        public void handler(int[] charcode, int offs) {
            if (((charcode[0] & 0xc0) == 0x80)
                    && (jumpbug_gfxbank[2] & 1) != 0) {
                charcode[0] += 128 + ((jumpbug_gfxbank[0] & 1) << 6)
                        + ((jumpbug_gfxbank[1] & 1) << 7)
                        + ((~jumpbug_gfxbank[4] & 1) << 8);
            }
        }
    };

    /* Sprite banking routines */
    public static modify_spritecodePtr mooncrst_modify_spritecode = new modify_spritecodePtr() {
        public void handler(int[] spritecode, int[] flipx, int[] flipy, int offs) {
            if ((mooncrst_gfxextend & 4) != 0 && (spritecode[0] & 0x30) == 0x20) {
                spritecode[0] = (spritecode[0] & 0x0f) | (mooncrst_gfxextend << 4);
            }
        }
    };

    public static modify_spritecodePtr moonqsr_modify_spritecode = new modify_spritecodePtr() {
        public void handler(int[] spritecode, int[] flipx, int[] flipy, int offs) {
            if ((galaxian_spriteram.read(offs + 2) & 0x20) != 0) {
                spritecode[0] += 64;
            }

            mooncrst_modify_spritecode.handler(spritecode, flipx, flipy, offs);
        }
    };

    public static modify_spritecodePtr ckongs_modify_spritecode = new modify_spritecodePtr() {
        public void handler(int[] spritecode, int[] flipx, int[] flipy, int offs) {
            if ((galaxian_spriteram.read(offs + 2) & 0x10) != 0) {
                spritecode[0] += 64;
            }
        }
    };

    public static modify_spritecodePtr calipso_modify_spritecode = new modify_spritecodePtr() {
        public void handler(int[] spritecode, int[] flipx, int[] flipy, int offs) {
            /* No flips */
            spritecode[0] = galaxian_spriteram.read(offs + 1);
            flipx[0] = 0;
            flipy[0] = 0;
        }
    };

    public static modify_spritecodePtr pisces_modify_spritecode = new modify_spritecodePtr() {
        public void handler(int[] spritecode, int[] flipx, int[] flipy, int offs) {
            if (pisces_gfxbank[0] != 0) {
                spritecode[0] += 64;
            }

        }
    };

    public static modify_spritecodePtr batman2_modify_spritecode = new modify_spritecodePtr() {
        public void handler(int[] spritecode, int[] flipx, int[] flipy, int offs) {
            /* only the upper 64 sprites are used */
            spritecode[0] += 64;
        }
    };
    public static modify_spritecodePtr jumpbug_modify_spritecode = new modify_spritecodePtr() {
        public void handler(int[] spritecode, int[] flipx, int[] flipy, int offs) {
            if (((spritecode[0] & 0x30) == 0x20)
                    && (jumpbug_gfxbank[2] & 1) != 0) {
                spritecode[0] += 32 + ((jumpbug_gfxbank[0] & 1) << 4)
                        + ((jumpbug_gfxbank[1] & 1) << 5)
                        + ((~jumpbug_gfxbank[4] & 1) << 6);
            }
        }
    };

    /*TODO*///	
/*TODO*///	/* color PROM mapping functions */
/*TODO*///	
/*TODO*///	static void frogger_modify_color(int *color)
/*TODO*///	{
/*TODO*///		*color = ((*color >> 1) & 0x03) | ((*color << 2) & 0x04);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/* y position mapping functions */
/*TODO*///	
/*TODO*///	static void frogger_modify_ypos(UINT8 *sy)
/*TODO*///	{
/*TODO*///		*sy = (*sy << 4) | (*sy >> 4);
/*TODO*///	}
/*TODO*///	
    /* bullet drawing functions */
    public static draw_bulletsPtr galaxian_draw_bullets = new draw_bulletsPtr() {
        public void handler(osd_bitmap bitmap, int offs, int x, int y) {
            int i;

            for (i = 0; i < 3; i++) {
                x--;

                if (x >= Machine.visible_area.min_x
                        && x <= Machine.visible_area.max_x) {
                    int color;

                    /* yellow missile, white shells (this is the terminology on the schematics) */
                    color = ((offs == 7 * 4) ? BULLETS_COLOR_BASE : BULLETS_COLOR_BASE + 1);

                    plot_pixel.handler(bitmap, x, y, Machine.pens[color]);
                }
            }
        }
    };
    public static draw_bulletsPtr scramble_draw_bullets = new draw_bulletsPtr() {
        public void handler(osd_bitmap bitmap, int offs, int x, int y) {
            x = x - 7;

            if (x >= Machine.visible_area.min_x
                    && x <= Machine.visible_area.max_x) {
                /* yellow bullets */
                plot_pixel.handler(bitmap, x, y, Machine.pens[BULLETS_COLOR_BASE]);
            }
        }
    };

    public static draw_bulletsPtr darkplnt_draw_bullets = new draw_bulletsPtr() {
        public void handler(osd_bitmap bitmap, int offs, int x, int y) {
            x = x - 7;

            if (x >= Machine.visible_area.min_x
                    && x <= Machine.visible_area.max_x) {
                plot_pixel.handler(bitmap, x, y, Machine.pens[32 + darkplnt_bullet_color]);
            }
        }
    };

    public static draw_bulletsPtr theend_draw_bullets = new draw_bulletsPtr() {
        public void handler(osd_bitmap bitmap, int offs, int x, int y) {
            int i;

            x = x - 3;

            for (i = 0; i < 4; i++) {
                x--;

                if (x >= Machine.visible_area.min_x
                        && x <= Machine.visible_area.max_x) {
                    plot_pixel.handler(bitmap, x, y, Machine.pens[BULLETS_COLOR_BASE]);
                }
            }
        }
    };

    /* background drawing functions */
    public static draw_backgroundPtr scramble_draw_background = new draw_backgroundPtr() {
        public void handler(osd_bitmap bitmap) {
            fillbitmap(bitmap, Machine.pens[background_start_pen + background_blue], Machine.visible_area);
        }
    };
    public static draw_backgroundPtr turtles_draw_background = new draw_backgroundPtr() {
        public void handler(osd_bitmap bitmap) {
            int color = (background_blue << 2) | (background_green << 1) | background_red;

            fillbitmap(bitmap, Machine.pens[background_start_pen + color], Machine.visible_area);
        }
    };

    /*TODO*///	static void frogger_draw_background(struct osd_bitmap *bitmap)
/*TODO*///	{
/*TODO*///		if (flip_screen_x != 0)
/*TODO*///		{
/*TODO*///			plot_box(bitmap,   0, 0, 120, 256, Machine.pens[background_start_pen]);
/*TODO*///			plot_box(bitmap, 120, 0, 136, 256, Machine.pens[background_start_pen + 1]);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			plot_box(bitmap,   0, 0, 136, 256, Machine.pens[background_start_pen + 1]);
/*TODO*///			plot_box(bitmap, 136, 0, 120, 256, Machine.pens[background_start_pen]);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void stratgyx_draw_background(struct osd_bitmap *bitmap)
/*TODO*///	{
/*TODO*///		UINT8 x;
/*TODO*///		UINT8 *prom;
/*TODO*///	
/*TODO*///	
/*TODO*///	    /* the background PROM is connected the following way:
/*TODO*///	
/*TODO*///	       bit 0 = 0 enables the blue gun if BCB is asserted
/*TODO*///	       bit 1 = 0 enables the red gun if BCR is asserted and
/*TODO*///	                 the green gun if BCG is asserted
/*TODO*///	       bits 2-7 are unconnected */
/*TODO*///	
/*TODO*///		prom = memory_region(REGION_USER1);
/*TODO*///	
/*TODO*///		for (x = 0; x < 32; x++)
/*TODO*///		{
/*TODO*///			int sx,color;
/*TODO*///	
/*TODO*///	
/*TODO*///			color = 0;
/*TODO*///	
/*TODO*///			if ((~prom[x] & 0x02) && background_red)   color |= 0x01;
/*TODO*///			if ((~prom[x] & 0x02) && background_green) color |= 0x02;
/*TODO*///			if ((~prom[x] & 0x01) && background_blue)  color |= 0x04;
/*TODO*///	
/*TODO*///			if (flip_screen_x != 0)
/*TODO*///			{
/*TODO*///				sx = 8 * (31 - x);
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				sx = 8 * x;
/*TODO*///			}
/*TODO*///	
/*TODO*///			plot_box(bitmap, sx, 0, 8, 256, Machine.pens[background_start_pen + color]);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void minefld_draw_background(struct osd_bitmap *bitmap)
/*TODO*///	{
/*TODO*///		if (background_blue != 0)
/*TODO*///		{
/*TODO*///			int x;
/*TODO*///	
/*TODO*///	
/*TODO*///			for (x = 0; x < 64; x++)
/*TODO*///			{
/*TODO*///				plot_box(bitmap, x * 2,        0, 2, 256, Machine.pens[background_start_pen + x]);
/*TODO*///			}
/*TODO*///	
/*TODO*///			for (x = 0; x < 60; x++)
/*TODO*///			{
/*TODO*///				plot_box(bitmap, (x + 64) * 2, 0, 2, 256, Machine.pens[background_start_pen + x + 64]);
/*TODO*///			}
/*TODO*///	
/*TODO*///			plot_box(bitmap, 248, 0, 16, 256, Machine.pens[background_start_pen]);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			fillbitmap(bitmap,Machine.pens[background_start_pen],&Machine.visible_area);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void rescue_draw_background(struct osd_bitmap *bitmap)
/*TODO*///	{
/*TODO*///		if (background_blue != 0)
/*TODO*///		{
/*TODO*///			int x;
/*TODO*///	
/*TODO*///	
/*TODO*///			for (x = 0; x < 64; x++)
/*TODO*///			{
/*TODO*///				plot_box(bitmap, x * 2,        0, 2, 256, Machine.pens[background_start_pen + x]);
/*TODO*///			}
/*TODO*///	
/*TODO*///			for (x = 0; x < 60; x++)
/*TODO*///			{
/*TODO*///				plot_box(bitmap, (x + 64) * 2, 0, 2, 256, Machine.pens[background_start_pen + x + 4]);
/*TODO*///			}
/*TODO*///	
/*TODO*///			plot_box(bitmap, 248, 0, 16, 256, Machine.pens[background_start_pen]);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			fillbitmap(bitmap,Machine.pens[background_start_pen],&Machine.visible_area);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mariner_draw_background(struct osd_bitmap *bitmap)
/*TODO*///	{
/*TODO*///		UINT8 x;
/*TODO*///		UINT8 *prom;
/*TODO*///	
/*TODO*///	
/*TODO*///	    /* the background PROM contains the color codes for each 8 pixel
/*TODO*///	       line (column) of the screen.  The first 0x20 bytes for unflipped,
/*TODO*///	       and the 2nd 0x20 bytes for flipped screen. */
/*TODO*///	
/*TODO*///		prom = memory_region(REGION_USER1);
/*TODO*///	
/*TODO*///		if (flip_screen_x != 0)
/*TODO*///		{
/*TODO*///			for (x = 0; x < 32; x++)
/*TODO*///			{
/*TODO*///				int color;
/*TODO*///	
/*TODO*///	
/*TODO*///				if (x == 0)
/*TODO*///					color = 0;
/*TODO*///				else
/*TODO*///					color = prom[0x20 + x - 1];
/*TODO*///	
/*TODO*///				plot_box(bitmap, 8 * (31 - x), 0, 8, 256, Machine.pens[background_start_pen + color]);
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			for (x = 0; x < 32; x++)
/*TODO*///			{
/*TODO*///				int color;
/*TODO*///	
/*TODO*///	
/*TODO*///				if (x == 31)
/*TODO*///					color = 0;
/*TODO*///				else
/*TODO*///					color = prom[x + 1];
/*TODO*///	
/*TODO*///				plot_box(bitmap, 8 * x, 0, 8, 256, Machine.pens[background_start_pen + color]);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
    /* star drawing functions */
    public static void galaxian_init_stars(char[] palette, int[] p_inc) {
        int i;
        int total_stars;
        int/*UINT32*/ generator;
        int x, y;

        draw_stars = null;
        galaxian_stars_on = 0;
        stars_blink_state = 0;
        if (stars_blink_timer != null) {
            timer_remove(stars_blink_timer);
        }

        for (i = 0; i < 64; i++) {
            int bits;
            int map[] = {0x00, 0x88, 0xcc, 0xff};

            bits = (i >> 0) & 0x03;
            palette[p_inc[0]++] = (char) (map[bits]);
            bits = (i >> 2) & 0x03;
            palette[p_inc[0]++] = (char) (map[bits]);
            bits = (i >> 4) & 0x03;
            palette[p_inc[0]++] = (char) (map[bits]);
        }

        /* precalculate the star background */
        total_stars = 0;
        generator = 0;

        for (y = 255; y >= 0; y--) {
            for (x = 511; x >= 0; x--) {
                int/*UINT32*/ bit0;

                bit0 = ((~generator >> 16) & 1) ^ ((generator >> 4) & 1);

                generator = (generator << 1) | bit0;

                if (((~generator >> 16) & 1) != 0 && (generator & 0xff) == 0xff) {
                    int color;

                    color = (~(generator >> 8)) & 0x3f;
                    if (color != 0 && total_stars < MAX_STARS) {
                        stars[total_stars].x = x;
                        stars[total_stars].y = y;
                        stars[total_stars].color = color;

                        total_stars++;
                    }
                }
            }
        }
    }
    /*TODO*///	
/*TODO*///	static void plot_star(struct osd_bitmap *bitmap, int x, int y, int color)
/*TODO*///	{
/*TODO*///		if (y < Machine.visible_area.min_y ||
/*TODO*///			y > Machine.visible_area.max_y ||
/*TODO*///		    x < Machine.visible_area.min_x ||
/*TODO*///			x > Machine.visible_area.max_x)
/*TODO*///			return;
/*TODO*///	
/*TODO*///	
/*TODO*///		if (flip_screen_x != 0)
/*TODO*///		{
/*TODO*///			x = 255 - x;
/*TODO*///		}
/*TODO*///		if (flip_screen_y != 0)
/*TODO*///		{
/*TODO*///			y = 255 - y;
/*TODO*///		}
/*TODO*///	
/*TODO*///		plot_pixel(bitmap, x, y, Machine.pens[STARS_COLOR_BASE + color]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void galaxian_draw_stars(struct osd_bitmap *bitmap)
/*TODO*///	{
/*TODO*///		int offs;
/*TODO*///		int currentframe;
/*TODO*///	
/*TODO*///	
/*TODO*///		currentframe = cpu_getcurrentframe();
/*TODO*///	
/*TODO*///		for (offs = 0;offs < STAR_COUNT;offs++)
/*TODO*///		{
/*TODO*///			int x,y;
/*TODO*///	
/*TODO*///	
/*TODO*///			x = ((stars[offs].x +   currentframe) & 0x01ff) >> 1;
/*TODO*///			y = ( stars[offs].y + ((currentframe + stars[offs].x) >> 9)) & 0xff;
/*TODO*///	
/*TODO*///			if ((y & 1) ^ ((x >> 3) & 1))
/*TODO*///			{
/*TODO*///				plot_star(bitmap, x, y, stars[offs].color);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	void scramble_draw_stars(struct osd_bitmap *bitmap)
/*TODO*///	{
/*TODO*///		int offs;
/*TODO*///	
/*TODO*///	
/*TODO*///		if (stars_blink_timer == 0)
/*TODO*///		{
/*TODO*///			start_stars_blink_timer(100000, 10000, 0.00001);
/*TODO*///		}
/*TODO*///	
/*TODO*///	
/*TODO*///		for (offs = 0;offs < STAR_COUNT;offs++)
/*TODO*///		{
/*TODO*///			int x,y;
/*TODO*///	
/*TODO*///	
/*TODO*///			x = stars[offs].x >> 1;
/*TODO*///			y = stars[offs].y;
/*TODO*///	
/*TODO*///			if ((y & 1) ^ ((x >> 3) & 1))
/*TODO*///			{
/*TODO*///				/* determine when to skip plotting */
/*TODO*///				switch (stars_blink_state & 0x03)
/*TODO*///				{
/*TODO*///				case 0:
/*TODO*///					if (!(stars[offs].color & 1))  continue;
/*TODO*///					break;
/*TODO*///				case 1:
/*TODO*///					if (!(stars[offs].color & 4))  continue;
/*TODO*///					break;
/*TODO*///				case 2:
/*TODO*///					if (!(stars[offs].y & 2))  continue;
/*TODO*///					break;
/*TODO*///				case 3:
/*TODO*///					/* always plot */
/*TODO*///					break;
/*TODO*///				}
/*TODO*///	
/*TODO*///				plot_star(bitmap, x, y, stars[offs].color);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void rescue_draw_stars(struct osd_bitmap *bitmap)
/*TODO*///	{
/*TODO*///		int offs;
/*TODO*///	
/*TODO*///	
/*TODO*///		/* same as Scramble, but only top (left) half of screen */
/*TODO*///	
/*TODO*///		if (stars_blink_timer == 0)
/*TODO*///		{
/*TODO*///			start_stars_blink_timer(100000, 10000, 0.00001);
/*TODO*///		}
/*TODO*///	
/*TODO*///	
/*TODO*///		for (offs = 0;offs < STAR_COUNT;offs++)
/*TODO*///		{
/*TODO*///			int x,y;
/*TODO*///	
/*TODO*///	
/*TODO*///			x = stars[offs].x >> 1;
/*TODO*///			y = stars[offs].y;
/*TODO*///	
/*TODO*///			if ((x < 128) && ((y & 1) ^ ((x >> 3) & 1)))
/*TODO*///			{
/*TODO*///				/* determine when to skip plotting */
/*TODO*///				switch (stars_blink_state & 0x03)
/*TODO*///				{
/*TODO*///				case 0:
/*TODO*///					if (!(stars[offs].color & 1))  continue;
/*TODO*///					break;
/*TODO*///				case 1:
/*TODO*///					if (!(stars[offs].color & 4))  continue;
/*TODO*///					break;
/*TODO*///				case 2:
/*TODO*///					if (!(stars[offs].y & 2))  continue;
/*TODO*///					break;
/*TODO*///				case 3:
/*TODO*///					/* always plot */
/*TODO*///					break;
/*TODO*///				}
/*TODO*///	
/*TODO*///				plot_star(bitmap, x, y, stars[offs].color);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mariner_draw_stars(struct osd_bitmap *bitmap)
/*TODO*///	{
/*TODO*///		int offs;
/*TODO*///		UINT8 *prom;
/*TODO*///		int currentframe;
/*TODO*///	
/*TODO*///	
/*TODO*///		/* bit 2 of the PROM controls star visibility */
/*TODO*///	
/*TODO*///		prom = memory_region(REGION_USER2);
/*TODO*///	
/*TODO*///		currentframe = cpu_getcurrentframe();
/*TODO*///	
/*TODO*///		for (offs = 0;offs < STAR_COUNT;offs++)
/*TODO*///		{
/*TODO*///			int x,y;
/*TODO*///	
/*TODO*///	
/*TODO*///			x = ((stars[offs].x +   -currentframe) & 0x01ff) >> 1;
/*TODO*///			y = ( stars[offs].y + ((-currentframe + stars[offs].x) >> 9)) & 0xff;
/*TODO*///	
/*TODO*///			if ((y & 1) ^ ((x >> 3) & 1))
/*TODO*///			{
/*TODO*///				if (prom[(x/8 + 1) & 0x1f] & 0x04)
/*TODO*///				{
/*TODO*///					plot_star(bitmap, x, y, stars[offs].color);
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void jumpbug_draw_stars(struct osd_bitmap *bitmap)
/*TODO*///	{
/*TODO*///		int offs;
/*TODO*///		int currentframe;
/*TODO*///	
/*TODO*///	
/*TODO*///		if (stars_blink_timer == 0)
/*TODO*///		{
/*TODO*///			start_stars_blink_timer(100000, 10000, 0.00001);
/*TODO*///		}
/*TODO*///	
/*TODO*///	
/*TODO*///		currentframe = cpu_getcurrentframe();
/*TODO*///	
/*TODO*///		for (offs = 0;offs < STAR_COUNT;offs++)
/*TODO*///		{
/*TODO*///			int x,y;
/*TODO*///	
/*TODO*///	
/*TODO*///			x = stars[offs].x >> 1;
/*TODO*///			y = stars[offs].y;
/*TODO*///	
/*TODO*///			/* determine when to skip plotting */
/*TODO*///			if ((y & 1) ^ ((x >> 3) & 1))
/*TODO*///			{
/*TODO*///				switch (stars_blink_state & 0x03)
/*TODO*///				{
/*TODO*///				case 0:
/*TODO*///					if (!(stars[offs].color & 1))  continue;
/*TODO*///					break;
/*TODO*///				case 1:
/*TODO*///					if (!(stars[offs].color & 4))  continue;
/*TODO*///					break;
/*TODO*///				case 2:
/*TODO*///					if (!(stars[offs].y & 2))  continue;
/*TODO*///					break;
/*TODO*///				case 3:
/*TODO*///					/* always plot */
/*TODO*///					break;
/*TODO*///				}
/*TODO*///	
/*TODO*///				x = ((stars[offs].x +   currentframe) & 0x01ff) >> 1;
/*TODO*///				y = ( stars[offs].y + ((currentframe + stars[offs].x) >> 9)) & 0xff;
/*TODO*///	
/*TODO*///				/* no stars in the status area */
/*TODO*///				if (x >= 240)  continue;
/*TODO*///	
/*TODO*///				plot_star(bitmap, x, y, stars[offs].color);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	public static timer_callback stars_blink_callback = new timer_callback() { public void handler(int param) 
/*TODO*///	{
/*TODO*///		stars_blink_state++;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static void start_stars_blink_timer(double ra, double rb, double c)
/*TODO*///	{
/*TODO*///		/* calculate the period using the formula given in the 555 datasheet */
/*TODO*///	
/*TODO*///		double period = 0.693 * (ra + 2.0 * rb) * c;
/*TODO*///	
/*TODO*///		stars_blink_timer = timer_pulse(TIME_IN_SEC(period), 0, stars_blink_callback);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
    /**
     * *************************************************************************
     *
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     *
     **************************************************************************
     */
    public static VhUpdatePtr galaxian_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int x, y;
            int offs, color_mask;
            int transparency;

            color_mask = (Machine.gfx[0].color_granularity == 4) ? 7 : 3;

            /* draw the bacground */
            if (draw_background != null) {
                draw_background.handler(bitmap);
            } else {
                if (draw_stars != null) {
                    /* black base for stars */
                    fillbitmap(bitmap, Machine.pens[background_start_pen], Machine.visible_area);
                }
            }
            /*TODO*///	
/*TODO*///	
/*TODO*///		/* draw the stars */
/*TODO*///		if (draw_stars && galaxian_stars_on)
/*TODO*///		{
/*TODO*///			draw_stars(bitmap);
/*TODO*///		}
/*TODO*///	
/*TODO*///	
/*TODO*///		/* draw the character layer */
            transparency = (draw_background != null || draw_stars != null) ? TRANSPARENCY_PEN : TRANSPARENCY_NONE;

            for (x = 0; x < 32; x++) {
                /*UINT8*/
                int u8_sx;
                int[] scroll = new int[1];
                int[] color = new int[1];

                scroll[0] = galaxian_attributesram.read(x << 1);
                color[0] = galaxian_attributesram.read((x << 1) | 1) & color_mask;
                /*TODO*///	
/*TODO*///			if (modify_color != 0)
/*TODO*///			{
/*TODO*///				modify_color(&color);
/*TODO*///			}
/*TODO*///	
/*TODO*///			if (modify_ypos != 0)
/*TODO*///			{
/*TODO*///				modify_ypos(&scroll);
/*TODO*///			}
/*TODO*///	

                u8_sx = (8 * x) & 0xFF;

                if (flip_screen_x[0] != 0) {
                    u8_sx = (248 - u8_sx) & 0xFF;
                }

                for (y = 0; y < 32; y++) {
                    int/*UINT8*/ u8_sy;
                    int[] code = new int[1];

                    u8_sy = ((8 * y) - scroll[0]) & 0xFF;

                    if (flip_screen_y[0] != 0) {
                        u8_sy = (248 - u8_sy) & 0xFF;
                    }

                    code[0] = galaxian_videoram.read((y << 5) | x);

                    if (modify_charcode != null) {
                        modify_charcode.handler(code, x);
                    }

                    drawgfx(bitmap, Machine.gfx[0],
                            code[0], color[0],
                            flip_screen_x[0], flip_screen_y[0],
                            u8_sx, u8_sy,
                            null, transparency, 0);
                }
            }

            /* draw the bullets */
            if (draw_bullets != null) {
                for (offs = 0; offs < galaxian_bulletsram_size[0]; offs += 4) {
                    y = 255 - galaxian_bulletsram.read(offs + 1);
                    x = 255 - galaxian_bulletsram.read(offs + 3);

                    if (y < Machine.visible_area.min_y
                            || y > Machine.visible_area.max_y) {
                        continue;
                    }

                    if (flip_screen_y[0] != 0) {
                        y = 255 - y;
                    }

                    draw_bullets.handler(bitmap, offs, x, y);
                }
            }


            /* draw the sprites */
            for (offs = galaxian_spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                int /*UINT8*/ u8_sx;
                int[] u8_sy = new int[1];
                int[] flipx = new int[1];
                int[] flipy = new int[1];
                int[] code = new int[1];
                int[] color = new int[1];

                u8_sx = galaxian_spriteram.read(offs + 3) & 0xFF;
                /* This is definately correct in Mariner. Look at
													  the 'gate' moving up/down. It stops at the
	  												  right spots */
                u8_sy[0] = galaxian_spriteram.read(offs) & 0xFF;
                flipx[0] = galaxian_spriteram.read(offs + 1) & 0x40;
                flipy[0] = galaxian_spriteram.read(offs + 1) & 0x80;
                code[0] = galaxian_spriteram.read(offs + 1) & 0x3f;
                color[0] = galaxian_spriteram.read(offs + 2) & color_mask;

                if (modify_spritecode != null) {
                    modify_spritecode.handler(code, flipx, flipy, offs);
                }
                /*TODO*///	
/*TODO*///			if (modify_color != 0)
/*TODO*///			{
/*TODO*///				modify_color(&color);
/*TODO*///			}
/*TODO*///	
/*TODO*///			if (modify_ypos != 0)
/*TODO*///			{
/*TODO*///				modify_ypos(&sy);
/*TODO*///			}
/*TODO*///	
                if (flip_screen_x[0] != 0) {
                    u8_sx = (240 - u8_sx) & 0xFF;
                    /* I checked a bunch of games including Scramble
								   (# of pixels the ship is from the top of the mountain),
				                   Mariner and Checkman. This is correct for them */
                    flipx[0] = NOT(flipx[0]);
                } else {
                    u8_sx = (u8_sx + 2) & 0xFF;
                }

                if (flip_screen_y[0] != 0) {
                    flipy[0] = NOT(flipy[0]);
                    if (offs >= 3 * 4) {
                        u8_sy[0] = (u8_sy[0] + 1) & 0xFF;//sy++;
                    } else {
                        u8_sy[0] = (u8_sy[0] + 2) & 0xFF;
                    }
                } else {
                    u8_sy[0] = (240 - u8_sy[0]) & 0xFF;
                    if (offs >= 3 * 4) {
                        u8_sy[0] = (u8_sy[0] + 1) & 0xFF;//sy++;
                    }
                }

                /* In Amidar, */
 /* Sprites #0, #1 and #2 need to be offset one pixel to be correctly */
 /* centered on the ladders in Turtles (we move them down, but since this */
 /* is a rotated game, we actually move them left). */
 /* Note that the adjustment must be done AFTER handling flipscreen, thus */
 /* proving that this is a hardware related "feature" */
 /* This is not Amidar, it is Galaxian/Scramble/hundreds of clones, and I'm */
 /* not sure it should be the same. A good game to test alignment is Armored Car */
                drawgfx(bitmap, Machine.gfx[1],
                        code[0], color[0],
                        flipx[0], flipy[0],
                        u8_sx, u8_sy[0],
                        flip_screen_x[0] != 0 ? spritevisibleareaflipx : spritevisiblearea, TRANSPARENCY_PEN, 0);
            }
        }
    };

    public static InterruptPtr hunchbks_vh_interrupt = new InterruptPtr() {
        public int handler() {
            cpu_irq_line_vector_w(0, 0, 0x03);
            cpu_set_irq_line(0, 0, PULSE_LINE);

            return ignore_interrupt.handler();
        }
    };
}
