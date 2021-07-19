/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b16.vidhrdw;

//generic imports
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b16.mame.osdependH.*;
//to be organized
import common.ptr.UBytePtr;

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

    /* bullets circuit */
    static int darkplnt_bullet_color;
    /*TODO*///	static void (*draw_bullets)(struct osd_bitmap *,int,int,int);	/* function to call to draw a bullet */
/*TODO*///	
    /* background circuit */
    static int background_red, background_green, background_blue;
    static int background_start_pen;
    /*TODO*///	static void (*draw_background)(struct osd_bitmap *);	/* function to call to draw the background */

    public static VhConvertColorPromPtr galaxian_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            /* first, the character/sprite palette */
            int p_ptr = 0;
            for (i = 0; i < 32; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_ptr++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_ptr++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* blue component */
                bit0 = (color_prom.read() >> 6) & 0x01;
                bit1 = (color_prom.read() >> 7) & 0x01;
                palette[p_ptr++] = (char) (0x4f * bit0 + 0xa8 * bit1);

                color_prom.inc();
            }

            /*TODO*///		galaxian_init_stars(&palette);
/*TODO*///	
/*TODO*///	
/*TODO*///		/* bullets - yellow and white */
/*TODO*///	
/*TODO*///		*(palette++) = 0xef;
/*TODO*///		*(palette++) = 0xef;
/*TODO*///		*(palette++) = 0x00;
/*TODO*///	
/*TODO*///		*(palette++) = 0xef;
/*TODO*///		*(palette++) = 0xef;
/*TODO*///		*(palette++) = 0xef;
/*TODO*///	
/*TODO*///	
/*TODO*///		/* black background */
/*TODO*///	
/*TODO*///		background_start_pen = BACKGROUND_COLOR_BASE;
/*TODO*///	
/*TODO*///		*(palette++) = 0;
/*TODO*///		*(palette++) = 0;
/*TODO*///		*(palette++) = 0;
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
/*TODO*///	
/*TODO*///	    modify_charcode = 0;
/*TODO*///	    modify_spritecode = 0;
/*TODO*///	    modify_color = 0;
/*TODO*///	    modify_ypos = 0;
/*TODO*///	
/*TODO*///		mooncrst_gfxextend = 0;
/*TODO*///	
/*TODO*///		draw_bullets = 0;
/*TODO*///	
/*TODO*///		draw_background = 0;
/*TODO*///		background_red = 0;
/*TODO*///		background_green = 0;
/*TODO*///		background_blue = 0;
/*TODO*///	
/*TODO*///		flip_screen_x_set(0);
/*TODO*///		flip_screen_y_set(0);
/*TODO*///	
/*TODO*///	
/*TODO*///		/* all the games except New Sinbad 7 clip the sprites at the top of the screen,
/*TODO*///		   New Sinbad 7 does it at the bottom */
/*TODO*///		if (Machine.gamedrv == &driver_newsin7)
/*TODO*///		{
/*TODO*///			spritevisiblearea      = &_spritevisibleareaflipx;
/*TODO*///	        spritevisibleareaflipx = &_spritevisiblearea;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			spritevisiblearea      = &_spritevisiblearea;
/*TODO*///	        spritevisibleareaflipx = &_spritevisibleareaflipx;
/*TODO*///		}
/*TODO*///	

            return 0;
        }
    };

    public static VhStartPtr galaxian_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = galaxian_plain_vh_start.handler();
            /*TODO*///	
/*TODO*///		draw_stars = galaxian_draw_stars;
/*TODO*///	
/*TODO*///		draw_bullets = galaxian_draw_bullets;
/*TODO*///	
            return ret;
        }
    };
    /*TODO*///	
    public static VhStartPtr mooncrst_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = galaxian_vh_start.handler();
            /*TODO*///	
/*TODO*///		modify_charcode   = mooncrst_modify_charcode;
/*TODO*///		modify_spritecode = mooncrst_modify_spritecode;
/*TODO*///	
            return ret;
        }
    };

    public static VhStartPtr moonqsr_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = galaxian_vh_start.handler();
            /*TODO*///	
/*TODO*///		modify_charcode   = moonqsr_modify_charcode;
/*TODO*///		modify_spritecode = moonqsr_modify_spritecode;
/*TODO*///	
            return ret;
        }
    };

    public static VhStartPtr pisces_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = galaxian_vh_start.handler();

            /*TODO*///		modify_charcode   = pisces_modify_charcode;
/*TODO*///		modify_spritecode = pisces_modify_spritecode;
/*TODO*///	
            return ret;
        }
    };

    public static VhStartPtr batman2_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = galaxian_vh_start.handler();
            /*TODO*///	
/*TODO*///		modify_charcode   = batman2_modify_charcode;
/*TODO*///		modify_spritecode = batman2_modify_spritecode;
/*TODO*///	
            return ret;
        }
    };

    public static VhStartPtr scramble_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = galaxian_plain_vh_start.handler();
            /*TODO*///	
/*TODO*///		draw_stars = scramble_draw_stars;
/*TODO*///	
/*TODO*///		draw_bullets = scramble_draw_bullets;
/*TODO*///	
/*TODO*///		draw_background = scramble_draw_background;
/*TODO*///	
            return ret;
        }
    };

    public static VhStartPtr turtles_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = scramble_vh_start.handler();
            /*TODO*///	
/*TODO*///		draw_background = turtles_draw_background;
/*TODO*///	
            return ret;
        }
    };

    public static VhStartPtr theend_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = scramble_vh_start.handler();
            /*TODO*///	
/*TODO*///		draw_bullets = theend_draw_bullets;
/*TODO*///	
            return ret;
        }
    };
    /*TODO*///	
/*TODO*///	public static VhStartPtr darkplnt_vh_start = new VhStartPtr() { public int handler() 
/*TODO*///	{
/*TODO*///		int ret = galaxian_plain_vh_start.handler();
/*TODO*///	
/*TODO*///		draw_bullets = darkplnt_draw_bullets;
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	} };
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
/*TODO*///	
/*TODO*///		modify_charcode   = jumpbug_modify_charcode;
/*TODO*///		modify_spritecode = jumpbug_modify_spritecode;

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
    /*TODO*///	
/*TODO*///	
/*TODO*///	/* character banking functions */
/*TODO*///	
/*TODO*///	static void mooncrst_modify_charcode(int *code,int x)
/*TODO*///	{
/*TODO*///		if ((mooncrst_gfxextend & 4) && (*code & 0xc0) == 0x80)
/*TODO*///		{
/*TODO*///			*code = (*code & 0x3f) | (mooncrst_gfxextend << 6);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void moonqsr_modify_charcode(int *code,int x)
/*TODO*///	{
/*TODO*///		if (galaxian_attributesram[(x << 1) | 1] & 0x20)
/*TODO*///		{
/*TODO*///			*code += 256;
/*TODO*///		}
/*TODO*///	
/*TODO*///	    mooncrst_modify_charcode(code,x);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void pisces_modify_charcode(int *code,int x)
/*TODO*///	{
/*TODO*///		if (pisces_gfxbank != 0)
/*TODO*///		{
/*TODO*///			*code += 256;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void batman2_modify_charcode(int *code,int x)
/*TODO*///	{
/*TODO*///		if ((*code & 0x80) && pisces_gfxbank)
/*TODO*///		{
/*TODO*///			*code += 256;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mariner_modify_charcode(int *code,int x)
/*TODO*///	{
/*TODO*///		UINT8 *prom;
/*TODO*///	
/*TODO*///	
/*TODO*///		/* bit 0 of the PROM controls character banking */
/*TODO*///	
/*TODO*///		prom = memory_region(REGION_USER2);
/*TODO*///	
/*TODO*///		if (prom[x] & 0x01)
/*TODO*///		{
/*TODO*///			*code += 256;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void jumpbug_modify_charcode(int *code,int offs)
/*TODO*///	{
/*TODO*///		if (((*code & 0xc0) == 0x80) &&
/*TODO*///			 (jumpbug_gfxbank[2] & 1) != 0)
/*TODO*///		{
/*TODO*///			*code += 128 + (( jumpbug_gfxbank[0] & 1) << 6) +
/*TODO*///						   (( jumpbug_gfxbank[1] & 1) << 7) +
/*TODO*///						   ((~jumpbug_gfxbank[4] & 1) << 8);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/* sprite banking functions */
/*TODO*///	
/*TODO*///	static void mooncrst_modify_spritecode(int *code,int *flipx,int *flipy,int offs)
/*TODO*///	{
/*TODO*///		if ((mooncrst_gfxextend & 4) && (*code & 0x30) == 0x20)
/*TODO*///		{
/*TODO*///			*code = (*code & 0x0f) | (mooncrst_gfxextend << 4);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void moonqsr_modify_spritecode(int *code,int *flipx,int *flipy,int offs)
/*TODO*///	{
/*TODO*///		if (galaxian_spriteram[offs + 2] & 0x20)
/*TODO*///		{
/*TODO*///			*code += 64;
/*TODO*///		}
/*TODO*///	
/*TODO*///	    mooncrst_modify_spritecode(code, flipx, flipy, offs);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void ckongs_modify_spritecode(int *code,int *flipx,int *flipy,int offs)
/*TODO*///	{
/*TODO*///		if (galaxian_spriteram[offs + 2] & 0x10)
/*TODO*///		{
/*TODO*///			*code += 64;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void calipso_modify_spritecode(int *code,int *flipx,int *flipy,int offs)
/*TODO*///	{
/*TODO*///		/* No flips */
/*TODO*///		*code = galaxian_spriteram[offs + 1];
/*TODO*///		*flipx = 0;
/*TODO*///		*flipy = 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void pisces_modify_spritecode(int *code,int *flipx,int *flipy,int offs)
/*TODO*///	{
/*TODO*///		if (pisces_gfxbank != 0)
/*TODO*///		{
/*TODO*///			*code += 64;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void batman2_modify_spritecode(int *code,int *flipx,int *flipy,int offs)
/*TODO*///	{
/*TODO*///		/* only the upper 64 sprites are used */
/*TODO*///	
/*TODO*///		*code += 64;
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void jumpbug_modify_spritecode(int *code,int *flipx,int *flipy,int offs)
/*TODO*///	{
/*TODO*///		if (((*code & 0x30) == 0x20) &&
/*TODO*///			 (jumpbug_gfxbank[2] & 1) != 0)
/*TODO*///		{
/*TODO*///			*code += 32 + (( jumpbug_gfxbank[0] & 1) << 4) +
/*TODO*///						  (( jumpbug_gfxbank[1] & 1) << 5) +
/*TODO*///						  ((~jumpbug_gfxbank[4] & 1) << 6);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
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
/*TODO*///	
/*TODO*///	/* bullet drawing functions */
/*TODO*///	
/*TODO*///	static void galaxian_draw_bullets(struct osd_bitmap *bitmap, int offs, int x, int y)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///	
/*TODO*///		for (i = 0; i < 3; i++)
/*TODO*///		{
/*TODO*///			x--;
/*TODO*///	
/*TODO*///			if (x >= Machine.visible_area.min_x &&
/*TODO*///				x <= Machine.visible_area.max_x)
/*TODO*///			{
/*TODO*///				int color;
/*TODO*///	
/*TODO*///	
/*TODO*///				/* yellow missile, white shells (this is the terminology on the schematics) */
/*TODO*///				color = ((offs == 7*4) ? BULLETS_COLOR_BASE : BULLETS_COLOR_BASE + 1);
/*TODO*///	
/*TODO*///				plot_pixel(bitmap, x, y, Machine.pens[color]);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void scramble_draw_bullets(struct osd_bitmap *bitmap, int offs, int x, int y)
/*TODO*///	{
/*TODO*///		x = x - 7;
/*TODO*///	
/*TODO*///		if (x >= Machine.visible_area.min_x &&
/*TODO*///			x <= Machine.visible_area.max_x)
/*TODO*///		{
/*TODO*///			/* yellow bullets */
/*TODO*///			plot_pixel(bitmap, x, y, Machine.pens[BULLETS_COLOR_BASE]);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void darkplnt_draw_bullets(struct osd_bitmap *bitmap, int offs, int x, int y)
/*TODO*///	{
/*TODO*///		x = x - 7;
/*TODO*///	
/*TODO*///		if (x >= Machine.visible_area.min_x &&
/*TODO*///			x <= Machine.visible_area.max_x)
/*TODO*///		{
/*TODO*///			plot_pixel(bitmap, x, y, Machine.pens[32 + darkplnt_bullet_color]);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void theend_draw_bullets(struct osd_bitmap *bitmap, int offs, int x, int y)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///	
/*TODO*///		x = x - 3;
/*TODO*///	
/*TODO*///		for (i = 0; i < 4; i++)
/*TODO*///		{
/*TODO*///			x--;
/*TODO*///	
/*TODO*///			if (x >= Machine.visible_area.min_x &&
/*TODO*///				x <= Machine.visible_area.max_x)
/*TODO*///			{
/*TODO*///				plot_pixel(bitmap, x, y, Machine.pens[BULLETS_COLOR_BASE]);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/* background drawing functions */
/*TODO*///	
/*TODO*///	static void scramble_draw_background(struct osd_bitmap *bitmap)
/*TODO*///	{
/*TODO*///		fillbitmap(bitmap,Machine.pens[background_start_pen + background_blue],&Machine.visible_area);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void turtles_draw_background(struct osd_bitmap *bitmap)
/*TODO*///	{
/*TODO*///		int color = (background_blue << 2) | (background_green << 1) | background_red;
/*TODO*///	
/*TODO*///		fillbitmap(bitmap,Machine.pens[background_start_pen + color],&Machine.visible_area);
/*TODO*///	}
/*TODO*///	
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
/*TODO*///	
/*TODO*///	/* star drawing functions */
/*TODO*///	
/*TODO*///	void galaxian_init_stars(UBytePtr *palette)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///		int total_stars;
/*TODO*///		UINT32 generator;
/*TODO*///		int x,y;
/*TODO*///	
/*TODO*///	
/*TODO*///		draw_stars = 0;
/*TODO*///		galaxian_stars_on = 0;
/*TODO*///		stars_blink_state = 0;
/*TODO*///		if (stars_blink_timer != 0)  timer_remove(stars_blink_timer);
/*TODO*///	
/*TODO*///	
/*TODO*///		for (i = 0;i < 64;i++)
/*TODO*///		{
/*TODO*///			int bits;
/*TODO*///			int map[4] = { 0x00, 0x88, 0xcc, 0xff };
/*TODO*///	
/*TODO*///	
/*TODO*///			bits = (i >> 0) & 0x03;
/*TODO*///			*((*palette)++) = map[bits];
/*TODO*///			bits = (i >> 2) & 0x03;
/*TODO*///			*((*palette)++) = map[bits];
/*TODO*///			bits = (i >> 4) & 0x03;
/*TODO*///			*((*palette)++) = map[bits];
/*TODO*///		}
/*TODO*///	
/*TODO*///	
/*TODO*///		/* precalculate the star background */
/*TODO*///	
/*TODO*///		total_stars = 0;
/*TODO*///		generator = 0;
/*TODO*///	
/*TODO*///		for (y = 255;y >= 0;y--)
/*TODO*///		{
/*TODO*///			for (x = 511;x >= 0;x--)
/*TODO*///			{
/*TODO*///				UINT32 bit0;
/*TODO*///	
/*TODO*///	
/*TODO*///				bit0 = ((~generator >> 16) & 1) ^ ((generator >> 4) & 1);
/*TODO*///	
/*TODO*///				generator = (generator << 1) | bit0;
/*TODO*///	
/*TODO*///				if (((~generator >> 16) & 1) && (generator & 0xff) == 0xff)
/*TODO*///				{
/*TODO*///					int color;
/*TODO*///	
/*TODO*///					color = (~(generator >> 8)) & 0x3f;
/*TODO*///					if (color != 0)
/*TODO*///					{
/*TODO*///						stars[total_stars].x = x;
/*TODO*///						stars[total_stars].y = y;
/*TODO*///						stars[total_stars].color = color;
/*TODO*///	
/*TODO*///						total_stars++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
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
            /*TODO*///		int x,y;
/*TODO*///		int offs,color_mask;
/*TODO*///		int transparency;
/*TODO*///	
/*TODO*///	
/*TODO*///		color_mask = (Machine.gfx[0].color_granularity == 4) ? 7 : 3;
/*TODO*///	
/*TODO*///	
/*TODO*///		/* draw the bacground */
/*TODO*///		if (draw_background != 0)
/*TODO*///		{
/*TODO*///			draw_background(bitmap);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (draw_stars != 0)
/*TODO*///			{
/*TODO*///				/* black base for stars */
/*TODO*///				fillbitmap(bitmap,Machine.pens[background_start_pen],&Machine.visible_area);
/*TODO*///			}
/*TODO*///		}
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
/*TODO*///		transparency = (draw_background || draw_stars) ? TRANSPARENCY_PEN : TRANSPARENCY_NONE;
/*TODO*///	
/*TODO*///		for (x = 0; x < 32; x++)
/*TODO*///		{
/*TODO*///			UINT8 sx,scroll;
/*TODO*///			int color;
/*TODO*///	
/*TODO*///	
/*TODO*///			scroll = galaxian_attributesram[ x << 1];
/*TODO*///			color  = galaxian_attributesram[(x << 1) | 1] & color_mask;
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
/*TODO*///	
/*TODO*///			sx = 8 * x;
/*TODO*///	
/*TODO*///			if (flip_screen_x != 0)
/*TODO*///			{
/*TODO*///				sx = 248 - sx;
/*TODO*///			}
/*TODO*///	
/*TODO*///	
/*TODO*///			for (y = 0; y < 32; y++)
/*TODO*///			{
/*TODO*///				UINT8 sy;
/*TODO*///				int code;
/*TODO*///	
/*TODO*///	
/*TODO*///				sy = (8 * y) - scroll;
/*TODO*///	
/*TODO*///				if (flip_screen_y != 0)
/*TODO*///				{
/*TODO*///					sy = 248 - sy;
/*TODO*///				}
/*TODO*///	
/*TODO*///	
/*TODO*///				code = galaxian_videoram[(y << 5) | x];
/*TODO*///	
/*TODO*///				if (modify_charcode != 0)
/*TODO*///				{
/*TODO*///					modify_charcode(&code, x);
/*TODO*///				}
/*TODO*///	
/*TODO*///				drawgfx(bitmap,Machine.gfx[0],
/*TODO*///						code,color,
/*TODO*///						flip_screen_x,flip_screen_y,
/*TODO*///						sx,sy,
/*TODO*///						0, transparency, 0);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///	
/*TODO*///		/* draw the bullets */
/*TODO*///		if (draw_bullets != 0)
/*TODO*///		{
/*TODO*///			for (offs = 0;offs < galaxian_bulletsram_size;offs += 4)
/*TODO*///			{
/*TODO*///				y = 255 - galaxian_bulletsram[offs + 1];
/*TODO*///				x = 255 - galaxian_bulletsram[offs + 3];
/*TODO*///	
/*TODO*///				if (y < Machine.visible_area.min_y ||
/*TODO*///					y > Machine.visible_area.max_y)
/*TODO*///					continue;
/*TODO*///	
/*TODO*///				if (flip_screen_y != 0)  y = 255 - y;
/*TODO*///	
/*TODO*///				draw_bullets(bitmap, offs, x, y);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///	
/*TODO*///		/* draw the sprites */
/*TODO*///		for (offs = galaxian_spriteram_size - 4;offs >= 0;offs -= 4)
/*TODO*///		{
/*TODO*///			UINT8 sx,sy;
/*TODO*///			int flipx,flipy,code,color;
/*TODO*///	
/*TODO*///	
/*TODO*///			sx = galaxian_spriteram[offs + 3]; /* This is definately correct in Mariner. Look at
/*TODO*///													  the 'gate' moving up/down. It stops at the
/*TODO*///	  												  right spots */
/*TODO*///			sy = galaxian_spriteram[offs];
/*TODO*///			flipx = galaxian_spriteram[offs + 1] & 0x40;
/*TODO*///			flipy = galaxian_spriteram[offs + 1] & 0x80;
/*TODO*///			code = galaxian_spriteram[offs + 1] & 0x3f;
/*TODO*///			color = galaxian_spriteram[offs + 2] & color_mask;
/*TODO*///	
/*TODO*///			if (modify_spritecode != 0)
/*TODO*///			{
/*TODO*///				modify_spritecode(&code, &flipx, &flipy, offs);
/*TODO*///			}
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
/*TODO*///			if (flip_screen_x != 0)
/*TODO*///			{
/*TODO*///				sx = 240 - sx;	/* I checked a bunch of games including Scramble
/*TODO*///								   (# of pixels the ship is from the top of the mountain),
/*TODO*///				                   Mariner and Checkman. This is correct for them */
/*TODO*///				flipx = !flipx;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				sx += 2;
/*TODO*///			}
/*TODO*///	
/*TODO*///			if (flip_screen_y != 0)
/*TODO*///			{
/*TODO*///				flipy = !flipy;
/*TODO*///				if (offs >= 3*4)
/*TODO*///					sy++;
/*TODO*///				else
/*TODO*///					sy += 2;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				sy = 240 - sy;
/*TODO*///				if (offs >= 3*4) sy++;
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* In Amidar, */
/*TODO*///			/* Sprites #0, #1 and #2 need to be offset one pixel to be correctly */
/*TODO*///			/* centered on the ladders in Turtles (we move them down, but since this */
/*TODO*///			/* is a rotated game, we actually move them left). */
/*TODO*///			/* Note that the adjustment must be done AFTER handling flipscreen, thus */
/*TODO*///			/* proving that this is a hardware related "feature" */
/*TODO*///			/* This is not Amidar, it is Galaxian/Scramble/hundreds of clones, and I'm */
/*TODO*///			/* not sure it should be the same. A good game to test alignment is Armored Car */
/*TODO*///	
/*TODO*///			drawgfx(bitmap,Machine.gfx[1],
/*TODO*///					code,color,
/*TODO*///					flipx,flipy,
/*TODO*///					sx,sy,
/*TODO*///					flip_screen_x ? spritevisibleareaflipx : spritevisiblearea,TRANSPARENCY_PEN,0);
/*TODO*///		}
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
