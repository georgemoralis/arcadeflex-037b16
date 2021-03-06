#ifndef DRIVER_H
#define DRIVER_H

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package mame;

public class driverH
{
	
	#ifdef MAME_NET
	#endif /* MAME_NET */
	
	
	#define MAX_CPU 8	/* MAX_CPU is the maximum number of CPUs which cpuintrf.c */
						/* can run at the same time. Currently, 8 is enough. */
	
	#define MAX_SOUND 5	/* MAX_SOUND is the maximum number of sound subsystems */
						/* which can run at the same time. Currently, 5 is enough. */
	
	
	
	struct MachineDriver
	{
		/* basic machine hardware */
		const struct MachineCPU cpu[MAX_CPU];
		float frames_per_second;
		int vblank_duration;	/* in microseconds - see description below */
		int cpu_slices_per_frame;	/* for multicpu games. 1 is the minimum, meaning */
									/* that each CPU runs for the whole video frame */
									/* before giving control to the others. The higher */
									/* this setting, the more closely CPUs are interleaved */
									/* and therefore the more accurate the emulation is. */
									/* However, an higher setting also means slower */
									/* performance. */
		void (*init_machine)(void);
	#ifdef MESS
		void (*stop_machine)(void); /* needed for MESS */
	#endif
	
	    /* video hardware */
		int screen_width,screen_height;
		const struct rectangle default_visible_area;	/* the visible area can be changed at */
										/* run time, but it should never be larger than the */
										/* one specified here, in order not to force the */
										/* OS dependant code to resize the display window. */
		const struct GfxDecodeInfo *gfxdecodeinfo;
		unsigned int total_colors;	/* palette is 3*total_colors bytes long */
		unsigned int color_table_len;	/* length in shorts of the color lookup table */
		void (*vh_init_palette)(UBytePtr palette, unsigned short *colortable,const UBytePtr color_prom);
	
		int video_attributes;	/* ASG 081897 */
	
		void (*vh_eof_callback)(void);	/* called every frame after osd_update_video_and_audio() */
										/* This is useful when there are operations that need */
										/* to be performed every frame regardless of frameskip, */
										/* e.g. sprite buffering or collision detection. */
		int (*vh_start)(void);
		void (*vh_stop)(void);
		void (*vh_update)(struct osd_bitmap *bitmap,int full_refresh);
	
		/* sound hardware */
		int sound_attributes;
		int obsolete1;
		int obsolete2;
		int obsolete3;
		const struct MachineSound sound[MAX_SOUND];
	
		/*
		   use this to manage nvram/eeprom/cmos/etc.
		   It is called before the emulation starts and after it ends. Note that it is
		   NOT called when the game is reset, since it is not needed.
		   file == 0, read_or_write == 0 . first time the game is run, initialize nvram
		   file != 0, read_or_write == 0 . load nvram from disk
		   file == 0, read_or_write != 0 . not allowed
		   file != 0, read_or_write != 0 . save nvram to disk
		 */
		void (*nvram_handler)(void *file,int read_or_write);
	};
	
	
	
	/* VBlank is the period when the video beam is outside of the visible area and */
	/* returns from the bottom to the top of the screen to prepare for a new video frame. */
	/* VBlank duration is an important factor in how the game renders itself. MAME */
	/* generates the vblank_interrupt, lets the game run for vblank_duration microseconds, */
	/* and then updates the screen. This faithfully reproduces the behaviour of the real */
	/* hardware. In many cases, the game does video related operations both in its vblank */
	/* interrupt, and in the normal game code; it is therefore important to set up */
	/* vblank_duration accurately to have everything properly in sync. An example of this */
	/* is Commando: if you set vblank_duration to 0, therefore redrawing the screen BEFORE */
	/* the vblank interrupt is executed, sprites will be misaligned when the screen scrolls. */
	
	/* Here are some predefined, TOTALLY ARBITRARY values for vblank_duration, which should */
	/* be OK for most cases. I have NO IDEA how accurate they are compared to the real */
	/* hardware, they could be completely wrong. */
	#define DEFAULT_60HZ_VBLANK_DURATION 0
	#define DEFAULT_30HZ_VBLANK_DURATION 0
	/* If you use IPT_VBLANK, you need a duration different from 0. */
	#define DEFAULT_REAL_60HZ_VBLANK_DURATION 2500
	#define DEFAULT_REAL_30HZ_VBLANK_DURATION 2500
	
	
	
	/* flags for video_attributes */
	
	/* bit 1 of the video attributes indicates whether or not dirty rectangles will work */
	#define	VIDEO_SUPPORTS_DIRTY		0x0002
	
	/* bit 0 of the video attributes indicates raster or vector video hardware */
	#define	VIDEO_TYPE_RASTER			0x0000
	#define	VIDEO_TYPE_VECTOR			0x0001
	
	/* bit 2 of the video attributes indicates whether or not the driver modifies the palette */
	#define	VIDEO_MODIFIES_PALETTE	0x0004
	
	/* bit 3 of the video attributes indicates that the game's palette has 6 or more bits */
	/*       per gun, and would therefore require a 24-bit display. This is entirely up to */
	/*       the OS dependant layer, the bitmap will still be 16-bit. */
	#define VIDEO_NEEDS_6BITS_PER_GUN	0x0008
	
	/* ASG 980417 - added: */
	/* bit 4 of the video attributes indicates that the driver wants its refresh after */
	/*       the VBLANK instead of before. */
	#define	VIDEO_UPDATE_BEFORE_VBLANK	0x0000
	#define	VIDEO_UPDATE_AFTER_VBLANK	0x0010
	
	/* In most cases we assume pixels are square (1:1 aspect ratio) but some games need */
	/* different proportions, e.g. 1:2 for Blasteroids */
	#define VIDEO_PIXEL_ASPECT_RATIO_MASK 0x0060
	#define VIDEO_PIXEL_ASPECT_RATIO_1_1 0x0000
	#define VIDEO_PIXEL_ASPECT_RATIO_1_2 0x0020
	#define VIDEO_PIXEL_ASPECT_RATIO_2_1 0x0040
	
	#define VIDEO_DUAL_MONITOR 0x0080
	
	/* Mish 181099:  See comments in vidhrdw/generic.c for details */
	#define VIDEO_BUFFERS_SPRITERAM 0x0100
	
	/* game wants to use a hicolor or truecolor bitmap (e.g. for alpha blending) */
	#define VIDEO_RGB_DIRECT 0x0200
	
	/* generic aspect ratios */
	#define VIDEO_ASPECT_RATIO_MASK		0xffff0000
	#define VIDEO_ASPECT_RATIO_NUM(a)	(((a) >> 24) & 0xff)
	#define VIDEO_ASPECT_RATIO_DEN(a)	(((a) >> 16) & 0xff)
	#define VIDEO_ASPECT_RATIO(n,d)		((((n) & 0xff) << 24) | (((d) & 0xff) << 16))
	
	
	/* flags for sound_attributes */
	#define	SOUND_SUPPORTS_STEREO		0x0001
	
	
	
	struct GameDriver
	{
		const char *source_file;	/* set this to __FILE__ */
		const struct GameDriver *clone_of;	/* if this is a clone, point to */
											/* the main version of the game */
		const char *name;
		const char *description;
		const char *year;
		const char *manufacturer;
		const struct MachineDriver *drv;
		const struct InputPortTiny *input_ports;
		void (*driver_init)(void);	/* optional function to be called during initialization */
									/* This is called ONCE, unlike Machine.init_machine */
									/* which is called every time the game is reset. */
	
		const struct RomModule *rom;
	#ifdef MESS
		const struct IODevice *dev;
	#endif
	
		UINT32 flags;	/* orientation and other flags; see defines below */
	};
	
	
	/* values for the flags field */
	
	#define ORIENTATION_MASK        	0x0007
	#define	ORIENTATION_FLIP_X			0x0001	/* mirror everything in the X direction */
	#define	ORIENTATION_FLIP_Y			0x0002	/* mirror everything in the Y direction */
	#define ORIENTATION_SWAP_XY			0x0004	/* mirror along the top-left/bottom-right diagonal */
	
	#define GAME_NOT_WORKING			0x0008
	#define GAME_WRONG_COLORS			0x0010	/* colors are totally wrong */
	#define GAME_IMPERFECT_COLORS		0x0020	/* colors are not 100% accurate, but close */
	#define GAME_NO_SOUND				0x0040	/* sound is missing */
	#define GAME_IMPERFECT_SOUND		0x0080	/* sound is known to be wrong */
	#define	GAME_REQUIRES_16BIT			0x0100	/* cannot fit in 256 colors */
	#define GAME_NO_COCKTAIL			0x0200	/* screen flip support is missing */
	#define GAME_UNEMULATED_PROTECTION	0x0400	/* game's protection not fully emulated */
	#define GAME_IMPERFECT_GRAPHICS		0x0800	/* graphics are wrong/incomplete */
	#define NOT_A_DRIVER				0x4000	/* set by the fake "root" driver_0 and by "containers" */
												/* e.g. driver_neogeo. */
	#ifdef MESS
	#define GAME_COMPUTER               0x8000  /* Driver is a computer (needs full keyboard) */
	#define GAME_COMPUTER_MODIFIED      0x0800	/* Official? Hack */
	#define GAME_ALIAS                  NOT_A_DRIVER	/* Driver is only an alias for an existing model */
	#endif
	
	
	#define public static GameDriver driver_NAME	   = new GameDriver("YEAR"	,"NAME"	,"driverH.java"	,rom_NAME,driver_PARENT	,machine_driver_MACHINE	,input_ports_INPUT	,init_INIT	,MONITOR	,	COMPANY,FULLNAME)	\
	extern const struct GameDriver driver_##PARENT;	\
	const struct GameDriver driver_##NAME =		\
	{											\
		__FILE__,								\
		&driver_##PARENT,						\
		#NAME,									\
		FULLNAME,								\
		#YEAR,									\
		COMPANY,								\
		&machine_driver_##MACHINE,				\
		input_ports_##INPUT,					\
		init_##INIT,							\
		rom_##NAME,								\
		MONITOR,								\
	};
	
	#define public static GameDriver driver_NAME	   = new GameDriver("YEAR"	,"NAME"	,"driverH.java"	,rom_NAME,driver_PARENT	,machine_driver_MACHINE	,input_ports_INPUT	,init_INIT	,MONITOR	,	COMPANY,FULLNAME,FLAGS)	\
	extern const struct GameDriver driver_##PARENT;	\
	const struct GameDriver driver_##NAME =		\
	{											\
		__FILE__,								\
		&driver_##PARENT,						\
		#NAME,									\
		FULLNAME,								\
		#YEAR,									\
		COMPANY,								\
		&machine_driver_##MACHINE,				\
		input_ports_##INPUT,					\
		init_##INIT,							\
		rom_##NAME,								\
		(MONITOR)|(FLAGS),						\
	};
	
	
	/* monitor parameters to be used with the GAME() macro */
	#define	ROT0	0x0000
	#define	ROT90	(ORIENTATION_SWAP_XY|ORIENTATION_FLIP_X)	/* rotate clockwise 90 degrees */
	#define	ROT180	(ORIENTATION_FLIP_X|ORIENTATION_FLIP_Y)		/* rotate 180 degrees */
	#define	ROT270	(ORIENTATION_SWAP_XY|ORIENTATION_FLIP_Y)	/* rotate counter-clockwise 90 degrees */
	#define	ROT0_16BIT		(ROT0|GAME_REQUIRES_16BIT)
	#define	ROT90_16BIT		(ROT90|GAME_REQUIRES_16BIT)
	#define	ROT180_16BIT	(ROT180|GAME_REQUIRES_16BIT)
	#define	ROT270_16BIT	(ROT270|GAME_REQUIRES_16BIT)
	
	/* this allows to leave the INIT field empty in the GAME() macro call */
	#define init_0 0
	
	
	extern const struct GameDriver *drivers[];
	
	#endif
}
