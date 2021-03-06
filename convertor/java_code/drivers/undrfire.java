/***************************************************************************

	Underfire  							(c) 1993 Taito

	Driver by Bryan McPhail & David Graves.

	Board Info:

		TC0470LIN : ?
		TC0480SCP : known tilemap chip
		TC0510NIO : known input chip
		TC0570SPC : must be the object chip (next to spritemap and OBJ roms)
		TC0590PIV : Piv tilemaps
		TC0620SCC : lightgun ??? pivot port ???
		TC0650FDA : palette ? (Slapshot and F3 games also have one)

	M43E0278A
	K1100744A Main Board

	2018 2088           43256    43256   68020-25
	2018 2088           D67-23   D67-17                    93C46
	2018 2088           43256    43256
	2018                D67-18   D67-19                TC0510NIO
	2018
	2018 TC0570 SPC                      43256
		                                     43256
	 D67-13                              43256  TC0650FDA
	          D67-07                            2018
	          D67-06
	TC0470LIN D67-05
	          D67-04                      43256
	TC0590PIV D67-03    43256    D67-10   43256
	                    43256    D67-11
	   D67-09    TC0480SCP       D67-12   TC0620SCC
	   D67-08

	  MB8421
	  MB8421   43256             EnsoniqOTIS
	           D67-20    D67-01
	           43256                             EnsoniqESP-R6
	 68000-12  D67-21    D67-02     EnsoniqSuperGlu

	           40MHz            16MHz   30.476MHz    68681


	Under Fire combines the sprite system used in Taito Z games with
	the TC0480SCP tilemap chip plus some features from the Taito F3 system.
	It has an extra tilemap chip which is a dead ringer for the TC0100SCN
	(check the inits). Why did Taito give it a different name in this
	incarnation?


	Game misbehaviours
	------------------

	(i) Sprites on some rounds had sprite/tile priority issues.
	Solved by upping sprite priority while TC0480SCP row zoom is
	enabled - kludge.

	(ii) Wrong colour palettes are used in some background scenes?!

	(iii) End sequence hangs reading an unknown input bit - fixed via a patch just now.
	In normal game, this bit acts as input freeze.

	Todo
	----

	This game needs a fake aim target!

	Occasional bad colors on the male torso shooting (attract)
	and tendency for correct background colors to be "delayed" by 1
	second or so. Blending-related?

	What does the 0xb00000 area do... alpha blending ??

	What is the unknown hardware at 0x600000... an alternative
	or legacy lightgun hookup?

	Pivot port which may be used for rotation: but not
	seen changing except in game inits. Perhaps only used
	in later levels?


	Gun calibration
	---------------

	The values below work well (set speed down to 4 so you can enter
	them). They give a little reloading margin all around screen.

	Use X=0x2000  Y=0x100 for top center
	Use X=0x3740  (same Y) top left
	Use X=0x8c0   (same Y) top right
	Then for the points from left to right near bottom (all Y=0x3400):
	X=0x3f00,0x3740,0x2f80,0x27c0,0x2000,0x1840,0x1080,0x8c0,0x100


Code documentation
------------------

$17b6e2: loop which keeps displaying the trail of aim dots in test mode

$181826: routine which calls subs to derive aim coords for P1 and P2 and
pokes them in the game's internal sprite table format into RAM - along
with the nine blue "flag point" sprites on the calibration screen
which seem to have fixed coordinates. [It also refreshes some green
text on screen - the calls to $1bfa.]

$18141a sub appears to be doing all the calculations - including an
indirected subroutine so there may be quite a lot of possible code.
It is called with parameter of 1 for player 2 (and 0 for player 1).

$1821c8 sub is called just after - this is simpler and seems to be
copying the calculation results (modified slightly by 3 pixels in each
direction - to adjust for size of aim sprite?) into the table in ram.

(Subsequently a standard conversion routine turns the table on the fly
into dwords that are actually poked into spriteram. To locate the code
do a watchpoint on the first sprite in spriteram - the P1 aim point.)

In-game: $18141a is called 3 times when you hit fire - 3 bullets - and
once when you hit shotgun. Like Spacegun it is only doing the aim
calculations when it needs to, so to provide an artificial target we
need to reproduce the $18141a calculations.


***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class undrfire
{
	
	
	/* F3 sound */
	READ16_HANDLER(f3_68000_share_r);
	WRITE16_HANDLER(f3_68000_share_w);
	READ16_HANDLER(f3_68681_r);
	WRITE16_HANDLER(f3_68681_w);
	READ16_HANDLER(es5510_dsp_r);
	WRITE16_HANDLER(es5510_dsp_w);
	WRITE16_HANDLER(f3_volume_w);
	WRITE16_HANDLER(f3_es5505_bank_w);
	extern data32_t *f3_shared_ram;
	
	static UINT16 coin_word;
	static UINT16 port_sel = 0;
	extern UINT16 undrfire_rotate_ctrl[8];
	
	data32_t *undrfire_ram;	/* will be read in vidhrdw for gun target calcs */
	
	
	/***********************************************************
					COLOR RAM
	
	Extract a standard version of this
	("taito_8bpg_palette_word_w"?) to Taitoic.c ?
	***********************************************************/
	
	static WRITE32_HANDLER( color_ram_w )
	{
		int a,r,g,b;
		COMBINE_DATA(&paletteram32[offset]);
	
		{
			a = paletteram32[offset];
			r = (a &0xff0000) >> 16;
			g = (a &0xff00) >> 8;
			b = (a &0xff);
	
			palette_change_color(offset,r,g,b);
		}
	}
	
	
	/***********************************************************
					INTERRUPTS
	***********************************************************/
	
	void undrfire_interrupt5(int x)
	{
		cpu_cause_interrupt(0,5);
	}
	
	public static InterruptPtr undrfire_interrupt = new InterruptPtr() { public int handler() 
	{
		return 4;
	} };
	
	
	/**********************************************************
					EPROM
	**********************************************************/
	
	static data8_t default_eeprom[128]=
	{
		0x02,0x01,0x11,0x12,0x01,0x01,0x01,0x00,0x80,0x80,0x30,0x01,0x00,0x00,0x62,0x45,
		0xe0,0xa0,0xff,0x28,0xff,0xff,0xfa,0xd7,0x33,0x28,0x00,0x00,0x33,0x28,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0xe0,0xa0,0xff,0x28,0xff,0xff,0xff,0xff,0xfa,0xd7,
		0x33,0x28,0x00,0x00,0x33,0x28,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xff,0xff,
		0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,
		0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,
		0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,
		0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff
	};
	
	static EEPROM_interface undrfire_eeprom_interface = new EEPROM_interface
	(
		6,				/* address bits */
		16,				/* data bits */
		"0110",			/* read command */
		"0101",			/* write command */
		"0111",			/* erase command */
		"0100000000",	/* unlock command */
		"0100110000",	/* lock command */
	);
	
	public static nvramPtr nvram_handler  = new nvramPtr() { public void handler(Object file, int read_or_write) 
	{
		if (read_or_write != 0)
			EEPROM_save(file);
		else {
			EEPROM_init(&undrfire_eeprom_interface);
			if (file != 0)
				EEPROM_load(file);
			else
				EEPROM_set_data(default_eeprom,128);  /* Default the gun setup values */
		}
	} };
	
	
	/**********************************************************
				GAME INPUTS
	**********************************************************/
	
	static READ32_HANDLER( undrfire_input_r )
	{
		switch (offset)
		{
			case 0x00:
			{
				return (input_port_0_word_r(0,0) << 16) | input_port_1_word_r(0,0) |
					  (EEPROM_read_bit() << 7);
			}
	
			case 0x01:
			{
				return input_port_2_word_r(0,0) | (coin_word << 16);
			}
	 	}
	
		return 0xffffffff;
	}
	
	static WRITE32_HANDLER( undrfire_input_w )
	{
		switch (offset)
		{
			case 0x00:
			{
				if (ACCESSING_MSB32 != 0)	/* $500000 is watchdog */
				{
					watchdog_reset_w(0,data >> 24);
				}
	
				if (ACCESSING_LSB32 != 0)
				{
					EEPROM_set_clock_line((data & 0x20) ? ASSERT_LINE : CLEAR_LINE);
					EEPROM_write_bit(data & 0x40);
					EEPROM_set_cs_line((data & 0x10) ? CLEAR_LINE : ASSERT_LINE);
					return;
				}
	
				return;
			}
	
			case 0x01:
			{
				if (ACCESSING_MSB32 != 0)
				{
					coin_lockout_w(0,~data & 0x01000000);
					coin_lockout_w(1,~data & 0x02000000);
					coin_counter_w(0, data & 0x04000000);
					coin_counter_w(1, data & 0x08000000);
					coin_word = (data >> 16) &0xffff;
				}
			}
		}
	}
	
	
	/* Some unknown hardware byte mapped at $600002-5 */
	
	static READ32_HANDLER( unknown_hardware_r )
	{
		switch (offset)	/* four single bytes are read in sequence at $156e */
		{
			case 0x00:	/* $600002-3 */
			{
				return 0xffff;	// no idea what they should be
			}
	
			case 0x01:	/* $600004-5 */
			{
				return 0xffff0000;	// no idea what they should be
			}
		}
	
		return 0x0;
	}
	
	
	static WRITE32_HANDLER( unknown_int_req_w )
	{
		/* 10000 cycle delay is arbitrary */
		timer_set(TIME_IN_CYCLES(10000,0),0, undrfire_interrupt5);
	}
	
	
	static READ32_HANDLER( undrfire_lightgun_r )
	{
		int x,y;
	
		switch (offset)
		{
			/* NB we are raising the raw inputs by an arbitrary amount,
			   but presumably the guns on the original will not have had
			   full 0-0xffff travel. We don't center around 0x8000... but
			   who knows if the real machine does. */
	
			case 0x00:	/* P1 */
			{
				x = input_port_3_word_r(0,0) << 6;
				y = input_port_4_word_r(0,0) << 6;
	
				return ((x << 24) &0xff000000) | ((x << 8) &0xff0000)
					 | ((y << 8) &0xff00) | ((y >> 8) &0xff) ;
			}
	
			case 0x01:	/* P2 */
			{
				x = input_port_5_word_r(0,0) << 6;
				y = input_port_6_word_r(0,0) << 6;
	
				return ((x << 24) &0xff000000) | ((x << 8) &0xff0000)
					 | ((y << 8) &0xff00) | ((y >> 8) &0xff) ;
			}
		}
	
	logerror("CPU #0 PC %06x: warning - read unmapped lightgun offset %06x\n",cpu_get_pc(),offset);
	
		return 0x0;
	}
	
	
	static WRITE32_HANDLER( rotate_control_w )	/* only a guess that it's rotation */
	{
			if (ACCESSING_LSW32 != 0)
			{
				undrfire_rotate_ctrl[port_sel] = data;
				return;
			}
	
			if (ACCESSING_MSW32 != 0)
			{
				port_sel = (data &0x70000) >> 16;
			}
	}
	
	
	static WRITE32_HANDLER( motor_control_w )
	{
	/*
		Standard value poked is 0x00910200 (we ignore lsb and msb
		which seem to be always zero)
	
		0x0, 0x8000 and 0x9100 are written at startup
	
		Two bits are written in test mode to this middle word
		to test gun vibration:
	
		........ .x......   P1 gun vibration
		........ x.......   P2 gun vibration
	*/
	}
	
	
	/***********************************************************
				 MEMORY STRUCTURES
	***********************************************************/
	
	static MEMORY_READ32_START( undrfire_readmem )
		{ 0x000000, 0x1fffff, MRA32_ROM },
		{ 0x200000, 0x21ffff, MRA32_RAM },	/* main CPUA ram */
		{ 0x300000, 0x303fff, MRA32_RAM },	/* sprite ram */
	//	{ 0x304000, 0x304003, MRA32_RAM },	// debugging
	//	{ 0x304400, 0x304403, MRA32_RAM },	// debugging
		{ 0x500000, 0x500007, undrfire_input_r },
		{ 0x600000, 0x600007, unknown_hardware_r },	/* unknown byte reads at $156e */
		{ 0x700000, 0x7007ff, MRA32_RAM },
		{ 0x800000, 0x80ffff, TC0480SCP_long_r },	  /* tilemaps */
		{ 0x830000, 0x83002f, TC0480SCP_ctrl_long_r },	// debugging
		{ 0x900000, 0x90ffff, TC0100SCN_long_r },	/* piv tilemaps */
		{ 0x920000, 0x92000f, TC0100SCN_ctrl_long_r },
		{ 0xa00000, 0xa0ffff, MRA32_RAM },	/* palette ram */
		{ 0xb00000, 0xb003ff, MRA32_RAM },	// ?? single bytes
		{ 0xf00000, 0xf00007, undrfire_lightgun_r },	/* stick coords read at $11b2-bc */
	MEMORY_END
	
	static MEMORY_WRITE32_START( undrfire_writemem )
		{ 0x000000, 0x1fffff, MWA32_ROM },
		{ 0x200000, 0x21ffff, MWA32_RAM, &undrfire_ram },
		{ 0x300000, 0x303fff, MWA32_RAM, &spriteram32, &spriteram_size },
	//	{ 0x304000, 0x304003, MWA32_RAM },	// ??? doesn't change
	//	{ 0x304400, 0x304403, MWA32_RAM },	// ??? doesn't change
		{ 0x400000, 0x400003, motor_control_w },	/* gun vibration */
		{ 0x500000, 0x500007, undrfire_input_w },	/* eerom etc. */
		{ 0x600000, 0x600007, unknown_int_req_w },	/* int request for unknown hardware */
		{ 0x700000, 0x7007ff, MWA32_RAM, &f3_shared_ram },
		{ 0x800000, 0x80ffff, TC0480SCP_long_w },	  /* tilemaps */
		{ 0x830000, 0x83002f, TC0480SCP_ctrl_long_w },
		{ 0x900000, 0x90ffff, TC0100SCN_long_w },	/* piv tilemaps */
		{ 0x920000, 0x92000f, TC0100SCN_ctrl_long_w },
		{ 0xa00000, 0xa0ffff, color_ram_w, &paletteram32 },
		{ 0xb00000, 0xb003ff, MWA32_RAM },	// single bytes, blending ??
		{ 0xd00000, 0xd00003, rotate_control_w },	/* perhaps port based rotate control? */
	MEMORY_END
	
	/******************************************************************************/
	
	static MEMORY_READ16_START( sound_readmem )
		{ 0x000000, 0x03ffff, MRA16_RAM },
		{ 0x140000, 0x140fff, f3_68000_share_r },
		{ 0x200000, 0x20001f, ES5505_data_0_r },
		{ 0x260000, 0x2601ff, es5510_dsp_r },
		{ 0x280000, 0x28001f, f3_68681_r },
		{ 0xc00000, 0xcfffff, MRA16_BANK1 },
		{ 0xff8000, 0xffffff, MRA16_RAM },
	MEMORY_END
	
	static MEMORY_WRITE16_START( sound_writemem )
		{ 0x000000, 0x03ffff, MWA16_RAM },
		{ 0x140000, 0x140fff, f3_68000_share_w },
		{ 0x200000, 0x20001f, ES5505_data_0_w },
		{ 0x260000, 0x2601ff, es5510_dsp_w },
		{ 0x280000, 0x28001f, f3_68681_w },
		{ 0x300000, 0x30003f, f3_es5505_bank_w },
		{ 0x340000, 0x340003, f3_volume_w }, /* 8 channel volume control */
		{ 0xc00000, 0xcfffff, MWA16_ROM },
		{ 0xff8000, 0xffffff, MWA16_RAM },
	MEMORY_END
	
	
	/***********************************************************
				 INPUT PORTS (dips in eprom)
	***********************************************************/
	
	static InputPortPtr input_ports_undrfire = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x0002, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x0004, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x0008, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER1 );/* ? where is freeze input */
		PORT_BIT( 0x0010, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x0020, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x0040, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x0080, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x0100, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x0200, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x0400, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x0800, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x1000, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x2000, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x4000, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x8000, IP_ACTIVE_LOW,  IPT_UNKNOWN );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW,  IPT_UNKNOWN );/* ?!  Needs to toggle in end sequence */
		PORT_BIT( 0x0002, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x0004, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x0008, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x0010, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x0020, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x0040, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x0080, IP_ACTIVE_HIGH, IPT_UNUSED );/* reserved for EEROM */
		PORT_BIT( 0x0100, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x0200, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x0400, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x0800, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x1000, IP_ACTIVE_LOW,  IPT_START1 );	PORT_BIT( 0x2000, IP_ACTIVE_LOW,  IPT_START2 );	PORT_BIT( 0x4000, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x8000, IP_ACTIVE_LOW,  IPT_UNKNOWN );
		PORT_START();       /* IN2 */
		PORT_BITX(0x01, IP_ACTIVE_LOW,  IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
		PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW,  IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW,  IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN );
		/* Gun inputs (real range is 0-0xffff: we use standard 0-255 and shift later) */
	
		PORT_START(); 	/* IN 3, P1X */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_REVERSE | IPF_PLAYER1, 20, 25, 0, 0xff);
		PORT_START(); 	/* IN 4, P1Y */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_PLAYER1, 20, 25, 0, 0xff);
		PORT_START(); 	/* IN 5, P2X */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_REVERSE | IPF_PLAYER2, 20, 25, 0, 0xff);
		PORT_START(); 	/* IN 6, P2Y */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_PLAYER2, 20, 25, 0, 0xff);
		PORT_START(); 	/* Fake DSW */
		PORT_BITX(    0x01, 0x00, IPT_DIPSWITCH_NAME | IPF_TOGGLE, "Show gun target", KEYCODE_F1, IP_JOY_NONE );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Yes") );
	INPUT_PORTS_END(); }}; 
	
	
	
	/**********************************************************
					GFX DECODING
	**********************************************************/
	
	static GfxLayout tile16x16_layout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		RGN_FRAC(1,2),
		5,	/* 5 bits per pixel */
		new int[] { RGN_FRAC(1,2), 0, 8, 16, 24,},
		new int[] { 32, 33, 34, 35, 36, 37, 38, 39, 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*64, 1*64,  2*64,  3*64,  4*64,  5*64,  6*64,  7*64,
		  8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
		64*16	/* every sprite takes 128 consecutive bytes */
	);
	
	static GfxLayout charlayout = new GfxLayout
	(
		16,16,    /* 16*16 characters */
		RGN_FRAC(1,1),
		4,        /* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 1*4, 0*4, 5*4, 4*4, 3*4, 2*4, 7*4, 6*4, 9*4, 8*4, 13*4, 12*4, 11*4, 10*4, 15*4, 14*4 },
		new int[] { 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64, 8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
		128*8     /* every sprite takes 128 consecutive bytes */
	);
	
	static GfxLayout pivlayout = new GfxLayout
	(
		8,8,    /* 8*8 characters */
		RGN_FRAC(1,2),
		6,      /* 4 bits per pixel */
		new int[] { RGN_FRAC(1,2), RGN_FRAC(1,2)+1, 0, 1, 2, 3 },
		new int[] { 2*4, 3*4, 0*4, 1*4, 6*4, 7*4, 4*4, 5*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8    /* every sprite takes 32 consecutive bytes */
	);
	
	static GfxDecodeInfo undrfire_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX2, 0x0, tile16x16_layout,  0, 512 ),
		new GfxDecodeInfo( REGION_GFX1, 0x0, charlayout,        0, 512 ),
		new GfxDecodeInfo( REGION_GFX3, 0x0, pivlayout,         0, 512 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	/***********************************************************
				     MACHINE DRIVERS
	***********************************************************/
	
	static void undrfire_machine_reset(void)
	{
		/* Sound cpu program loads to 0xc00000 so we use a bank */
		data16_t *RAM = (data16_t *)memory_region(REGION_CPU2);
		cpu_setbank(1,&RAM[0x80000]);
	
		RAM[0]=RAM[0x80000]; /* Stack and Reset vectors */
		RAM[1]=RAM[0x80001];
		RAM[2]=RAM[0x80002];
		RAM[3]=RAM[0x80003];
	
		f3_68681_reset();
	}
	
	static struct ES5505interface es5505_interface =
	{
		1,					/* total number of chips */
		{ 16000000 },		/* freq */
		{ REGION_SOUND1 },	/* Bank 0: Unused by F3 games? */
		{ REGION_SOUND1 },	/* Bank 1: All games seem to use this */
		{ YM3012_VOL(100,MIXER_PAN_LEFT,100,MIXER_PAN_RIGHT) },		/* master volume */
	};
	
	
	static MachineDriver machine_driver_undrfire = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68EC020,
				16000000,	/* 16 MHz */
				undrfire_readmem,undrfire_writemem,null,null,
				undrfire_interrupt, 1
			),
			new MachineCPU(
				CPU_M68000 | CPU_AUDIO_CPU,
				16000000,
				sound_readmem,sound_writemem,null,null,
				ignore_interrupt,0
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* CPU slices */
		undrfire_machine_reset,
	
		/* video hardware */
		40*8, 32*8, new rectangle( 0, 40*8-1, 3*8, 32*8-1 ),
	
		undrfire_gfxdecodeinfo,
		16384, 16384,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_NEEDS_6BITS_PER_GUN,
		null,
		undrfire_vh_start,
		undrfire_vh_stop,
		undrfire_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_ES5505,
				es5505_interface
			)
		},
	
		nvram_handler
	);
	
	
	
	/***************************************************************************
						DRIVERS
	***************************************************************************/
	
	static RomLoadPtr rom_undrfire = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x200000, REGION_CPU1, 0 );/* 2048K for 68020 code (CPU A) */
		ROM_LOAD32_BYTE( "d67-19", 0x00000, 0x80000, 0x1d88fa5a )
		ROM_LOAD32_BYTE( "d67-18", 0x00001, 0x80000, 0xf41ae7fd )
		ROM_LOAD32_BYTE( "d67-17", 0x00002, 0x80000, 0x34e030b7 )
		ROM_LOAD32_BYTE( "d67-23", 0x00003, 0x80000, 0x28e84e0a )
	
		ROM_REGION( 0x140000, REGION_CPU2, 0 );	ROM_LOAD16_BYTE( "d67-20", 0x100000, 0x20000,  0x974ebf69 )
		ROM_LOAD16_BYTE( "d67-21", 0x100001, 0x20000,  0x8fc6046f )
	
		ROM_REGION( 0x400000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD16_BYTE( "d67-08", 0x000000, 0x200000, 0x56730d44 )	/* SCR 16x16 tiles */
		ROM_LOAD16_BYTE( "d67-09", 0x000001, 0x200000, 0x3c19f9e3 )
	
		ROM_REGION( 0x1000000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "d67-03", 0x000003, 0x200000, 0x3b6e99a9 )	/* OBJ 16x16 tiles */
		ROM_LOAD32_BYTE( "d67-04", 0x000002, 0x200000, 0x8f2934c9 )
		ROM_LOAD32_BYTE( "d67-05", 0x000001, 0x200000, 0xe2e7dcf3 )
		ROM_LOAD32_BYTE( "d67-06", 0x000000, 0x200000, 0xa2a63488 )
		ROM_LOAD32_BYTE( "d67-07", 0x800000, 0x200000, 0x189c0ee5 )
	
		ROM_REGION( 0x400000, REGION_GFX3, ROMREGION_DISPOSE );	ROM_LOAD16_BYTE( "d67-10", 0x000000, 0x100000, 0xd79e6ce9 )	/* PIV 8x8 tiles, 6bpp */
		ROM_LOAD16_BYTE( "d67-11", 0x000001, 0x100000, 0x7a401bb3 )
		ROM_LOAD       ( "d67-12", 0x300000, 0x100000, 0x67b16fec );	ROM_FILL       (           0x200000, 0x100000, 0 )
	
		ROM_REGION16_LE( 0x80000, REGION_USER1, 0 )
		ROM_LOAD16_WORD( "d67-13", 0x00000,  0x80000,  0x42e7690d )	/* STY, spritemap */
	
		ROM_REGION16_BE( 0x1000000, REGION_SOUND1, ROMREGION_SOUNDONLY | ROMREGION_ERASE00 )
		ROM_LOAD16_BYTE( "d67-01", 0x000000, 0x200000, 0xa2f18122 )	/* Ensoniq samples */
		ROM_LOAD16_BYTE( "d67-02", 0xc00000, 0x200000, 0xfceb715e )
	ROM_END(); }}; 
	
	
	static READ32_HANDLER( main_cycle_r )
	{
		int ptr;
		if ((cpu_get_sp()&2)==0) ptr=undrfire_ram[(cpu_get_sp()&0x1ffff)/4];
		else ptr=(((undrfire_ram[(cpu_get_sp()&0x1ffff)/4])&0x1ffff)<<16) |
		(undrfire_ram[((cpu_get_sp()&0x1ffff)/4)+1]>>16);
	
		if (cpu_get_pc()==0x682 && ptr==0x1156)
			cpu_spinuntil_int();
	
		return undrfire_ram[0x4f8/4];
	}
	
	public static InitDriverPtr init_undrfire = new InitDriverPtr() { public void handler() 
	{
		unsigned int offset,i;
		UINT8 *gfx = memory_region(REGION_GFX3);
		UINT32 *rom = (UINT32*)memory_region(REGION_CPU1);
		int size=memory_region_length(REGION_GFX3);
		int data;
	
		/* Speedup handlers */
		install_mem_read32_handler(0, 0x2004f8, 0x2004fb, main_cycle_r);
	
		/* make piv tile GFX format suitable for gfxdecode */
		offset = size/2;
		for (i = size/2+size/4; i<size; i++)
		{
			int d1,d2,d3,d4;
	
			/* Expand 2bits into 4bits format */
			data = gfx[i];
			d1 = (data>>0) & 3;
			d2 = (data>>2) & 3;
			d3 = (data>>4) & 3;
			d4 = (data>>6) & 3;
	
			gfx[offset] = (d1<<2) | (d2<<6);
			offset++;
	
			gfx[offset] = (d3<<2) | (d4<<6);
			offset++;
		}
	
		/* Game hangs in the end sequence polling an unknown input (that usually freezes the game?!) */
		rom[0x4a5d8/4]=(rom[0x4a5d8/4]&0xffff0000)|0x4e71;
	} };
	
	
	public static GameDriver driver_undrfire	   = new GameDriver("1993"	,"undrfire"	,"undrfire.java"	,rom_undrfire,null	,machine_driver_undrfire	,input_ports_undrfire	,init_undrfire	,ROT0_16BIT	,	"Taito Corporation Japan", "Under Fire (World)", GAME_IMPERFECT_COLORS )
}
