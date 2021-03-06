/***************************************************************************

Taito Z System [twin 68K with optional Z80]
-------------------------------------------

David Graves

(this is based on the F2 driver by Bryan McPhail, Brad Oliver, Andrew Prime,
Nicola Salmoria. Thanks to Richard Bush and the Raine team, whose open
source was very helpful in many areas particularly the sprites.)

- Changes Log -

05-27-01 Inputs through taitoic ioc routines, contcirc subwoofer filter
04-12-01 Centered steering AD inputs, added digital steer
02-18-01 Added Spacegun gunsights (Insideoutboy)


				*****

The Taito Z system has a number of similarities with the Taito F2 system,
and uses some of the same custom Taito components.

TaitoZ supports 5 separate layers of graphics - one 64x64 tiled scrolling
background plane of 8x8 tiles, a similar foreground plane, another optional
plane used for drawing a road (e.g. Chasehq), a sprite plane [with varying
properties], and a text plane with character definitions held in ram.

(Double Axle has four rather than two background planes, and they contain
32x32 16x16 tiles. This is because it uses a TC0480SCP rather than the
older TC0100SCN tilemap generator used in previous Taito Z games. The
hardware for Taito's Super Chase was a further development of this, with a
68020 for main CPU and Ensoniq sound - standard features of Taito's F3
system. Taito's F3 system superceded both Taito B and F2 systems, but the
Taito Z system was enhanced with F3 features and continued in games like
Super Chase and Under Fire up to the mid 1990s.)

The sprites are typically 16x8 tiles aggregated through a spritemap rom
into bigger sizes. Spacegun has 64x64 sprites, but some of the games use
different [128x128] or even multiple sizes. Some of the games aggregate
16x16 tiles to create their sprites. The size of the sprite ram area varies
from 0x400 to 0x4000 bytes, suggesting there is no standard object chip.

The Z system has twin 68K CPUs which communicate via shared ram.
Typically they share $4000 bytes, but Spacegun / Dbleaxle share $10000.

The first 68000 handles screen, palette and sprites, and sometimes other
jobs [e.g. inputs; in one game it also handles the road].

The second 68000 may handle functions such as:
	(i)  inputs/dips, sound (through a YM2610) and/or
	(ii) the "road" that's in every TaitoZ game except Spacegun.

Most Z system games have a Z80 as well, which takes over sound duties.
Commands are written to it by the one of the 68000s.

The memory map for the Taito Z games is similar in outline but usually
shuffled around: some games have different i/o because of analogue
sticks, light guns, cockpit hardware etc.


Contcirc custom chips (incomplete)
---------------------

Cpu board: TC0100SCN, TC0150ROD, TC040IOC (DG: early port
	based io chip)

Video board: TC0020VRA, TC0050VDZ (three of these)
	(DG: precursor to the 370MSO/300FLA combination ??)

cc_3_01 cc_3_02 cc_3_06 cc_3_07 (234000)
1 x lh5763j-70 (27hc64 - eeprom ?)
cc_3_64 (234000)
cc_3_97 (27512)


Aquajack top board (Guru)
------------------

68000-12 x 2
OSC: 26.686, 24.000, 16.000
i dont see any recognisable sound chips, but i do see a YM3016F

TCO110PCR
TCO220IOC
TCO100SCN
TCO140SYT
TCO3200BR
TCO150ROD
TCO020VAR [DG: not "VRA" as contcirc dump notes claim?]
TCO050VDZ
TCO050VDZ
TCO050VDZ


ChaseHQ2(SCI) custom chips (Guru) (DG: same as Bshark except 0140SYT?)
--------------------------

CPU PCB:
TC0170ABT
TC0150ROD
TC0140SYT
TC0220IOC

c09-23.rom is a
PROM type AM27S21PC, location looks like this...

-------------
|   68000   |
-------------

c09-25    c09-26
c09-24

|-------|
|       |
| ABT   |
|       |
|-------|

c09-23     c09-07

|-------|
|       |
| ROD   |
|       |
|-------|

c09-32   c09-33
-------------
|   68000   |
-------------

c09-21  c09-22

Lower PCB:
TCO270MOD
TC0300FLA
TC0260DAR
TC0370MSO
TC0100SCN
TC0380BSH

c09-16.rom is located next to
c09-05, which is located next to Taito TCO370MSO.


BShark custom chips
-------------------

TC0220IOC (known io chip)
TC0260DAR (known palette chip)
TC0400YSC  substitute for TC0140SYT when 68K writes directly to YM2610 ??
TC0170ABT  = same in Dblaxle
TC0100SCN (known tilemap chip)
TC0370MSO  = same in Dblaxle, Motion Objects ?
TC0300FLA  = same in Dblaxle
TC0270MOD  ???
TC0380BSH  ???
TC0150ROD (known road generator chip)


DblAxle custom chip info
------------------------

TC0150ROD is next to road lines gfx chip [c78-09] but also
c78-15, an unused 256 byte rom. Perhaps this contains color
info for the road lines? Raine makes an artificial "pal map"
for the road, AFAICS.

TC0170ABT is between 68000 CPUA and the TC0140SYT. Next to
that is the Z80A, the YM2610, and the three adpcm roms.

On the graphics board we have the TC0480SCP next to its two
scr gfx roms: c78-10 & 11.

The STY object mapping rom is next to c78-25, an unused
0x10000 byte rom which compresses by 98%. To right of this
are TC0370MSO (motion objects?), then TC0300FLA.

Below c78-25 are two unused 1K roms: c84-10 and c84-11.
Below right is another unused 256 byte rom, c78-21.
(At the bottom are the 5 obj gfx roms.)

K11000635A
----------
 43256   c78-11 SCN1 CHR
 43256   c78-10 SCN0 CHR   TC0480SCP

 c78-04
 STY ROM
            c78-25   TC0370MSO   TC0300FLA
            c84-10
            c84-11                                      c78-21

                       43256 43256 43256 43256
                 43256 43256 43256 43256 43256
                 43256 43256 43256 43256 43256
                                   43256 43256

                             c78-05L
            c78-06 OBJ1
                             c78-05H

            c78-08 OBJ3      c78-07 OBJ2

Power Wheels
------------

Cpu PCB

CPU:	68000-16 x2
Sound:	Z80-A
	YM2610
OSC:	32.000MHz
Chips:	TC0140SYT
	TC0150ROD
	TC0170ABT
	TC0310FAM
	TC0510NIO


Video PCB

OSC:	26.686MHz
Chips:	TC0260DAR
	TC0270MOD
	TC0300FLA
	TC0370MSO
	TC0380BSH
	TC0480SCP


LAN interface board

OSC:	40.000MHz
	16.000MHz
Chips:	uPD72105C


TODO Lists
==========

NB: Some historic comments related to fixed issues have been
left in for reference. They are enclosed in square brackets.
Obviously they can be ditched as soon as the basic remaining
problem - emulating the TC0150ROD - is solved.

Add cpu idle time skip to improve speed.

Is the no-Z80 sound handling correct: some voices in Bshark
aren't that clear.

Make taitosnd cpu-independent so we can restore Z80 to CPU3.

DIPs


Continental Circus
------------------

No road.

The 8 level accel / brake should be possible to control with
analogue pedal. Don't think mame can do this.

Junk (?) stuff often written in high byte of sound word.

Speculative YM2610 a/b/c channel filtering as these may be
outputs to subwoofer (vibration). They sound a lot better now.


Chasehq
-------

No road.

Mask sprites not implemented, Raine seems to fudge these...
(e.g. junk sprites when you reach criminal car)

Motor CPU: appears to be identical to one in Topspeed.


Battle Shark
------------

No road [only used on some levels].


Chasehq2
--------

No road.

Sprite frames were plotted in opposite order so flickered.
Reversing this has lost us alternate frames: we may have
to buffer sprite ram by one frame to solve this?


Night Striker
-------------

No road.

Control stick unsatisfactory: there is a "dead patch" around
stick center (in case the centering of the arcade stick
wasn't very good?) so your movement gets very sluggish there.
Use a lookup table to eliminate dead patch?

Strange page in test mode which lets you alter all sorts of settings
that may relate to the game's sit-in cockpit? Can't find a dip that
disables this - perhaps only cockpit version existed.

Does a variety of writes to TC0220IOC offset 3... significant?

[CPUA int4 at $11c0 calls sub which writes to control stick area
requesting a/d conversion. When done this hardware causes int6
($106xx) which reads out the value from the hardware [and also
requests another a/d conversion, causing another int6... probably
4 per frame to get all the necessary values]. Bshark stick works
in a similar way.]


Aqua Jack
---------

Sprites left on screen under hiscore table. Maybe there is a
sprite disable bit somewhere.

No road. Not sure of visible screen size (your boat seems slightly
cut off at bottom, but that was needed so the grey garage as you
start game stretches properly to bottom of screen).

Hangs briefly fairly often without massive cpu interleaving (500).
Even now I don't think the keys are very responsive in test mode.

The problem code is this:

CPUA
$1fe02 hangs waiting for ($6002,A5) in shared ram to be zero.

CPUB
$1056 calls $11ea routine which starts by setting ($6002,A5) non-
zero. At end (after $1218 waiting for a bit from sound comm port)
it alters ($6002,A5) to zero (but this value lasts briefly!).

Unless context rapidly switches back to cpua this change is missed
because $11ea gets called again *very* rapidly at times when sounds
are being written [that's when the problem manifested].

$108a-c2 reads 0x20 bytes from unmapped area, not sure
what it's doing. Perhaps this machine had some optional
exotic input device...


Spacegun
--------

Problem with the zoomed sprites not matching up very well
when forming the background. They jerk a bit relative to
each other... probably a cpu sync thing, perhaps also some
fine-tuning required on the zoomed sprite dimension calcs.

Light gun interrupt timing arbitrary.


Double Axle
-----------

No road.

Double Axle has poor sound: one ADPCM rom should be twice as long?
[In log we saw stuff like this, suggesting extra ADPCM rom needed:
YM2610: ADPCM-A end out of range: $001157ff
YM2610: ADPCM-A start out of range: $00111f00]

Various sprites go missing e.g. mountains half way through cross
country course. Fall off the ledge and crash and you will see
the explosion sprites make other mountain sprites vanish, as
though their entries in spriteram are being overwritten. (Perhaps
an int6 timing/number issue: sprites seem to be ChaseHQ2ish with
a spriteframe toggle - currently this never changes which seems
wrong.)

No sprite/tile variable priority implemented, I'm hoping it is
only sprite/road priority that changes - but it's probaby more
complicated.


***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class taito_z
{
	
	
	
	READ16_HANDLER ( sci_spriteframe_r );
	WRITE16_HANDLER( sci_spriteframe_w );
	
	//  These TC0150ROD prototypes will go in taitoic.h  //
	READ16_HANDLER ( TC0150ROD_word_r );	/* Road generator */
	WRITE16_HANDLER( TC0150ROD_word_w );
	
	static UINT16 cpua_ctrl = 0xff;
	static int sci_int6 = 0;
	static int dblaxle_int6 = 0;
	static int ioc220_port = 0;
	static data16_t eep_latch = 0;
	
	//static data16_t *taitoz_ram;
	//static data16_t *motor_ram;
	
	static size_t taitoz_sharedram_size;
	data16_t *taitoz_sharedram;	/* read externally to draw Spacegun crosshair */
	
	static READ16_HANDLER( sharedram_r )
	{
		return taitoz_sharedram[offset];
	}
	
	static WRITE16_HANDLER( sharedram_w )
	{
		COMBINE_DATA(&taitoz_sharedram[offset]);
	}
	
	static void parse_control(void)
	{
		/* bit 0 enables cpu B */
		/* however this fails when recovering from a save state
		   if cpu B is disabled !! */
		cpu_set_reset_line(2,(cpua_ctrl &0x1) ? CLEAR_LINE : ASSERT_LINE);
	
	}
	
	static void parse_control_noz80(void)
	{
		/* bit 0 enables cpu B */
		/* however this fails when recovering from a save state
		   if cpu B is disabled !! */
		cpu_set_reset_line(1,(cpua_ctrl &0x1) ? CLEAR_LINE : ASSERT_LINE);
	
	}
	
	static WRITE16_HANDLER( cpua_ctrl_w )	/* assumes Z80 sandwiched between 68Ks */
	{
		if ((data &0xff00) && ((data &0xff) == 0))
			data = data >> 8;	/* for Wgp */
		cpua_ctrl = data;
	
		parse_control();
	
		logerror("CPU #0 PC %06x: write %04x to cpu control\n",cpu_get_pc(),data);
	}
	
	static WRITE16_HANDLER( cpua_noz80_ctrl_w )	/* assumes no Z80 */
	{
		if ((data &0xff00) && ((data &0xff) == 0))
			data = data >> 8;	/* for Wgp */
		cpua_ctrl = data;
	
		parse_control_noz80();
	
		logerror("CPU #0 PC %06x: write %04x to cpu control\n",cpu_get_pc(),data);
	}
	
	
	/***********************************************************
					INTERRUPTS
	***********************************************************/
	
	/* 68000 A */
	
	public static InterruptPtr taitoz_interrupt = new InterruptPtr() { public int handler() 
	{
		return 4;
	} };
	
	static void taitoz_interrupt6(int x)
	{
		cpu_cause_interrupt(0,6);
	}
	
	/* 68000 B */
	
	public static InterruptPtr taitoz_cpub_interrupt = new InterruptPtr() { public int handler() 
	{
		return 4;
	} };
	
	static void taitoz_cpub_interrupt5(int x)
	{
		cpu_cause_interrupt(2,5);	/* assumes Z80 sandwiched between the 68Ks */
	}
	
	static void taitoz_sg_cpub_interrupt5(int x)
	{
		cpu_cause_interrupt(1,5);	/* assumes no Z80 */
	}
	
	static void taitoz_cpub_interrupt6(int x)
	{
		cpu_cause_interrupt(2,6);	/* assumes Z80 sandwiched between the 68Ks */
	}
	
	
	/***** Routines for particular games *****/
	
	public static InterruptPtr contcirc_interrupt = new InterruptPtr() { public int handler() 
	{
		return 6;
	} };
	
	public static InterruptPtr contcirc_cpub_interrupt = new InterruptPtr() { public int handler() 
	{
		return 6;
	} };
	
	public static InterruptPtr chasehq_interrupt = new InterruptPtr() { public int handler() 
	{
		return 4;
	} };
	
	public static InterruptPtr chq_cpub_interrupt = new InterruptPtr() { public int handler() 
	{
		return 4;
	} };
	
	public static InterruptPtr bshark_interrupt = new InterruptPtr() { public int handler() 
	{
		return 4;
	} };
	
	public static InterruptPtr bshark_cpub_interrupt = new InterruptPtr() { public int handler() 
	{
		return 4;
	} };
	
	public static InterruptPtr sci_interrupt = new InterruptPtr() { public int handler() 
	{
		/* Need 2 int4's per int6 else (-$6b63,A5) never set to 1 which
		   causes all sprites to vanish! Spriteram has areas for 2 frames
		   so in theory only needs updating every other frame. */
	
		sci_int6 = !sci_int6;
	
		if (sci_int6 != 0)
			timer_set(TIME_IN_CYCLES(200000-500,0),0, taitoz_interrupt6);
		return 4;
	} };
	
	public static InterruptPtr sci_cpub_interrupt = new InterruptPtr() { public int handler() 
	{
		return 4;
	} };
	
	public static InterruptPtr nightstr_interrupt = new InterruptPtr() { public int handler() 
	{
		return 4;
	} };
	
	public static InterruptPtr nightstr_cpub_interrupt = new InterruptPtr() { public int handler() 
	{
		return 4;
	} };
	
	public static InterruptPtr aquajack_interrupt = new InterruptPtr() { public int handler() 
	{
		return 4;
	} };
	
	public static InterruptPtr aquajack_cpub_interrupt = new InterruptPtr() { public int handler() 
	{
		return 4;	/* int4 goes straight to RTE */
	} };
	
	public static InterruptPtr spacegun_interrupt = new InterruptPtr() { public int handler() 
	{
		return 4;
	} };
	
	public static InterruptPtr spacegun_cpub_interrupt = new InterruptPtr() { public int handler() 
	{
		return 4;
	} };
	
	/* Double Axle seems to keep only 1 sprite frame in sprite ram,
	   which is probably wrong. Game seems to work with no int 6's
	   at all. Cpu control byte has 0,4,8,c poked into 2nd nibble
	   and it seems possible this should be causing int6's ? */
	
	public static InterruptPtr dblaxle_interrupt = new InterruptPtr() { public int handler() 
	{
		// Unsure how many int6's per frame, copy SCI for now
		dblaxle_int6 = !dblaxle_int6;
	
		if (dblaxle_int6 != 0)
			timer_set(TIME_IN_CYCLES(200000-500,0),0, taitoz_interrupt6);
	
		return 4;
	} };
	
	public static InterruptPtr dblaxle_cpub_interrupt = new InterruptPtr() { public int handler() 
	{
		// Unsure how many int6's per frame
		timer_set(TIME_IN_CYCLES(200000-500,0),0, taitoz_interrupt6);
		return 4;
	} };
	
	
	/******************************************************************
						EEPROM
	
	This is an earlier version of the eeprom used in some TaitoB games.
	The eeprom unlock command is different, and the write/clock/reset
	bits are different.
	******************************************************************/
	
	static data8_t default_eeprom[128]=
	{
		0x00,0x00,0x00,0xff,0x00,0x01,0x41,0x41,0x00,0x00,0x00,0xff,0x00,0x00,0xf0,0xf0,
		0x00,0x00,0x00,0xff,0x00,0x01,0x41,0x41,0x00,0x00,0x00,0xff,0x00,0x00,0xf0,0xf0,
		0x00,0x80,0x00,0x80,0x00,0x80,0x00,0x80,0x00,0x01,0x40,0x00,0x00,0x00,0xf0,0x00,
		0x00,0x01,0x42,0x85,0x00,0x00,0xf1,0xe3,0x00,0x01,0x40,0x00,0x00,0x00,0xf0,0x00,
		0x00,0x01,0x42,0x85,0x00,0x00,0xf1,0xe3,0xcc,0xcb,0xff,0xff,0xff,0xff,0xff,0xff,
		0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,
		0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,
		0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff
	};
	
	static EEPROM_interface eeprom_interface = new EEPROM_interface
	(
		6,				/* address bits */
		16,				/* data bits */
		"0110",			/* read command */
		"0101",			/* write command */
		"0111",			/* erase command */
		"0100000000",	/* lock command */
		"0100111111" 	/* unlock command */
	);
	
	public static nvramPtr nvram_handler  = new nvramPtr() { public void handler(Object file, int read_or_write) 
	{
		if (read_or_write != 0)
			EEPROM_save(file);
		else
		{
			EEPROM_init(&eeprom_interface);
	
			if (file != 0)
				EEPROM_load(file);
			else
				EEPROM_set_data(default_eeprom,128);  /* Default the gun setup values */
		}
	} };
	
	static int eeprom_r(void)
	{
		return (EEPROM_read_bit() & 0x01)<<7;
	}
	
	static READ16_HANDLER( eep_latch_r )
	{
		return eep_latch;
	}
	
	static WRITE16_HANDLER( spacegun_output_bypass_w )
	{
		switch (offset)
		{
			case 0x03:
	
	/*			0000xxxx	(unused)
				000x0000	eeprom reset (active low)
				00x00000	eeprom clock
				0x000000	eeprom data
				x0000000	(unused)                  */
	
				COMBINE_DATA(&eep_latch);
				EEPROM_write_bit(data & 0x40);
				EEPROM_set_clock_line((data & 0x20) ? ASSERT_LINE : CLEAR_LINE);
				EEPROM_set_cs_line((data & 0x10) ? CLEAR_LINE : ASSERT_LINE);
				break;
	
			default:
				TC0220IOC_w( offset,data );	/* might be a 510NIO ! */
		}
	}
	
	
	/**********************************************************
					GAME INPUTS
	**********************************************************/
	
	static READ16_HANDLER( contcirc_input_bypass_r )
	{
		/* Bypass TC0220IOC controller for analog input */
	
		data8_t port = TC0220IOC_port_r(0);	/* read port number */
		int steer = 0;
		int fake = input_port_6_word_r(0,0);
	
		if (!(fake &0x10))	/* Analogue steer (the real control method) */
		{
			steer = input_port_5_word_r(0,0);	/* steer */
	
		}
		else	/* Digital steer */
		{
			if ((fake & 0x4) != 0)
			{
				steer = 0x60;
			}
			else if ((fake & 0x8) != 0)
			{
				steer = 0xff9f;
			}
		}
	
		switch (port)
		{
			case 0x08:
				return steer &0xff;
	
			case 0x09:
				return steer >> 8;
	
			default:
				return TC0220IOC_portreg_r( offset );
		}
	}
	
	
	static READ16_HANDLER( chasehq_input_bypass_r )
	{
		/* Bypass TC0220IOC controller for extra inputs */
	
		data8_t port = TC0220IOC_port_r(0);	/* read port number */
		int steer = 0;
		int fake = input_port_10_word_r(0,0);
	
		if (!(fake &0x10))	/* Analogue steer (the real control method) */
		{
			steer = input_port_9_word_r(0,0);	/* IN6 */
	
		}
		else	/* Digital steer */
		{
			if ((fake & 0x4) != 0)
			{
				steer = 0xff80;
			}
			else if ((fake & 0x8) != 0)
			{
				steer = 0x7f;
			}
		}
	
		switch (port)
		{
			case 0x08:
				return input_port_5_word_r(0,mem_mask);
	
			case 0x09:
				return input_port_6_word_r(0,mem_mask);
	
			case 0x0a:
				return input_port_7_word_r(0,mem_mask);
	
			case 0x0b:
				return input_port_8_word_r(0,mem_mask);
	
			case 0x0c:
				return steer &0xff;
	
			case 0x0d:
				return steer >> 8;
	
			default:
				return TC0220IOC_portreg_r( offset );
		}
	}
	
	
	static READ16_HANDLER( bshark_stick_r )
	{
		switch (offset)
		{
			case 0x00:
				return input_port_5_word_r(0,mem_mask);
	
			case 0x01:
				return input_port_6_word_r(0,mem_mask);
	
			case 0x02:
				return input_port_7_word_r(0,mem_mask);
	
			case 0x03:
				return input_port_8_word_r(0,mem_mask);
		}
	
	logerror("CPU #0 PC %06x: warning - read unmapped stick offset %06x\n",cpu_get_pc(),offset);
	
		return 0xff;
	}
	
	static WRITE16_HANDLER( bshark_stick_w )
	{
		/* Each write invites a new interrupt as soon as the
		   hardware has got the next a/d conversion ready. We set a token
		   delay of 10000 cycles; our "coords" are always ready
		   but we don't want CPUA to have an int6 before int4 is over (?)
		*/
	
		timer_set(TIME_IN_CYCLES(10000,0),0, taitoz_interrupt6);
	}
	
	
	static READ16_HANDLER( sci_steer_input_r )
	{
		int steer = 0;
		int fake = input_port_6_word_r(0,0);
	
		if (!(fake &0x10))	/* Analogue steer (the real control method) */
		{
			steer = input_port_5_word_r(0,0) - 0x80;	/* steer */
		}
		else	/* Digital steer */
		{
			if ((fake & 0x4) != 0)
			{
				steer = 0xffa0;
			}
			else if ((fake & 0x8) != 0)
			{
				steer = 0x5f;
			}
		}
	
		switch (offset)
		{
			case 0x04:
				return (steer & 0xff);
	
	 		case 0x05:
				return (steer & 0xff00) >> 8;
		}
	
	logerror("CPU #0 PC %06x: warning - read unmapped steer input offset %06x\n",cpu_get_pc(),offset);
	
		return 0xff;
	}
	
	
	static READ16_HANDLER( spacegun_input_bypass_r )
	{
		switch (offset)
		{
			case 0x03:
				return eeprom_r();
	
			default:
				return TC0220IOC_r( offset );	/* might be a 510NIO ! */
		}
	}
	
	static READ16_HANDLER( spacegun_lightgun_r )
	{
		switch (offset)
		{
			case 0x00:
				return input_port_5_word_r(0,mem_mask);	/* P1X */
	
			case 0x01:
				return input_port_6_word_r(0,mem_mask);	/* P1Y */
	
			case 0x02:
				return input_port_7_word_r(0,mem_mask);	/* P2X */
	
			case 0x03:
				return input_port_8_word_r(0,mem_mask);	/* P2Y */
		}
	
		return 0x0;
	}
	
	static WRITE16_HANDLER( spacegun_lightgun_w )
	{
		/* Each write invites a new lightgun interrupt as soon as the
		   hardware has got the next coordinate ready. We set a token
		   delay of 10000 cycles; our "lightgun" coords are always ready
		   but we don't want CPUB to have an int5 before int4 is over (?).
	
		   Four lightgun interrupts happen before the collected coords
		   are moved to shared ram where CPUA can use them. */
	
		timer_set(TIME_IN_CYCLES(10000,0),0, taitoz_sg_cpub_interrupt5);
	}
	
	
	static READ16_HANDLER( dblaxle_steer_input_r )
	{
		int steer = 0;
		int fake = input_port_6_word_r(0,0);
	
		if (!(fake &0x10))	/* Analogue steer (the real control method) */
		{
			steer = input_port_5_word_r(0,0);	/* steer */
		}
		else	/* Digital steer */
		{
			if ((fake & 0x4) != 0)
			{
				steer = 0xffc0;
			}
			else if ((fake & 0x8) != 0)
			{
				steer = 0x3f;
			}
		}
	
		switch (offset)
		{
			case 0x04:
				return steer >> 8;
	
			case 0x05:
				return steer &0xff;
		}
	
	logerror("CPU #0 PC %06x: warning - read unmapped steer input offset %02x\n",cpu_get_pc(),offset);
		return 0x00;
	}
	
	
	static READ16_HANDLER( chasehq_motor_r )
	{
		switch (offset)
		{
			case 0x0:
				return (rand() &0xff);	/* motor status ?? */
	
			case 0x101:
				return 0x55;	/* motor cpu status ? */
	
			default:
	logerror("CPU #0 PC %06x: warning - read motor cpu %03x\n",cpu_get_pc(),offset);
				return 0;
		}
	}
	
	static WRITE16_HANDLER( chasehq_motor_w )
	{
		/* Writes $e00000-25 and $e00200-219 */
	
	logerror("CPU #0 PC %06x: warning - write %04x to motor cpu %03x\n",cpu_get_pc(),data,offset);
	
	}
	
	static READ16_HANDLER( aquajack_unknown_r )
	{
		return 0xff;
	}
	
	
	/*****************************************************
					SOUND
	*****************************************************/
	
	static int banknum = -1;
	
	static void reset_sound_region(void)	/* assumes Z80 sandwiched between 68Ks */
	{
		cpu_setbank( 10, memory_region(REGION_CPU2) + (banknum * 0x4000) + 0x10000 );
	}
	
	public static WriteHandlerPtr sound_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		banknum = (data - 1) & 7;
		reset_sound_region();
	} };
	
	static WRITE16_HANDLER( taitoz_sound_w )
	{
		if (offset == 0)
			taitosound_port_w (0, data & 0xff);
		else if (offset == 1)
			taitosound_comm_w (0, data & 0xff);
	
	#ifdef MAME_DEBUG
		if ((data & 0xff00) != 0)
		{
			char buf[80];
	
			sprintf(buf,"taitoz_sound_w to high byte: %04x",data);
			usrintf_showmessage(buf);
		}
	#endif
	}
	
	static READ16_HANDLER( taitoz_sound_r )
	{
		if (offset == 1)
			return ((taitosound_comm_r (0) & 0xff));
		else return 0;
	}
	
	static WRITE16_HANDLER( taitoz_msb_sound_w )
	{
		if (offset == 0)
			taitosound_port_w (0,(data >> 8) & 0xff);
		else if (offset == 1)
			taitosound_comm_w (0,(data >> 8) & 0xff);
	
	#ifdef MAME_DEBUG
		if ((data & 0xff) != 0)
		{
			char buf[80];
	
			sprintf(buf,"taitoz_msb_sound_w to low byte: %04x",data);
			usrintf_showmessage(buf);
		}
	#endif
	}
	
	static READ16_HANDLER( taitoz_msb_sound_r )
	{
		if (offset == 1)
			return ((taitosound_comm_r (0) & 0xff) << 8);
		else return 0;
	}
	
	
	/***********************************************************
				 MEMORY STRUCTURES
	***********************************************************/
	
	
	static MEMORY_READ16_START( contcirc_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x080000, 0x083fff, MRA16_RAM },	/* main CPUA ram */
		{ 0x084000, 0x087fff, sharedram_r },
		{ 0x100000, 0x100007, TC0110PCR_word_r },	/* palette */
		{ 0x200000, 0x20ffff, TC0100SCN_word_0_r },	/* tilemaps */
		{ 0x220000, 0x22000f, TC0100SCN_ctrl_word_0_r },
		{ 0x300000, 0x301fff, TC0150ROD_word_r },	/* "root ram" */
		{ 0x400000, 0x4006ff, MRA16_RAM },	/* spriteram */
	MEMORY_END
	
	static MEMORY_WRITE16_START( contcirc_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x080000, 0x083fff, MWA16_RAM },
		{ 0x084000, 0x087fff, sharedram_w, &taitoz_sharedram, &taitoz_sharedram_size },
	//	{ 0x090000, 0x090001, MWA16_NOP },	/* ??? */
		{ 0x100000, 0x100007, TC0110PCR_step1_rbswap_word_w },	/* palette */
		{ 0x200000, 0x20ffff, TC0100SCN_word_0_w },	/* tilemaps */
		{ 0x220000, 0x22000f, TC0100SCN_ctrl_word_0_w },
		{ 0x300000, 0x301fff, TC0150ROD_word_w },	/* "root ram" */
		{ 0x400000, 0x4006ff, MWA16_RAM, &spriteram16, &spriteram_size },
	MEMORY_END
	
	static MEMORY_READ16_START( contcirc_cpub_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x080000, 0x083fff, MRA16_RAM },
		{ 0x084000, 0x087fff, sharedram_r },
		{ 0x100000, 0x100001, contcirc_input_bypass_r },
		{ 0x100002, 0x100003, TC0220IOC_halfword_port_r },	/* (actually game uses TC040IOC) */
		{ 0x200000, 0x200003, taitoz_sound_r },
	MEMORY_END
	
	static MEMORY_WRITE16_START( contcirc_cpub_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x080000, 0x083fff, MWA16_RAM },
		{ 0x084000, 0x087fff, sharedram_w, &taitoz_sharedram },
		{ 0x100000, 0x100001, TC0220IOC_halfword_portreg_w },
		{ 0x100002, 0x100003, TC0220IOC_halfword_port_w },
		{ 0x200000, 0x200003, taitoz_sound_w },
	MEMORY_END
	
	
	static MEMORY_READ16_START( chasehq_readmem )
		{ 0x000000, 0x07ffff, MRA16_ROM },
		{ 0x100000, 0x107fff, MRA16_RAM },	/* main CPUA ram */
		{ 0x108000, 0x10bfff, sharedram_r },
		{ 0x10c000, 0x10ffff, MRA16_RAM },	/* extra CPUA ram */
		{ 0x400000, 0x400001, chasehq_input_bypass_r },
		{ 0x400002, 0x400003, TC0220IOC_halfword_port_r },
		{ 0x820000, 0x820003, taitoz_sound_r },
		{ 0xa00000, 0xa00007, TC0110PCR_word_r },	/* palette */
		{ 0xc00000, 0xc0ffff, TC0100SCN_word_0_r },	/* tilemaps */
		{ 0xc20000, 0xc2000f, TC0100SCN_ctrl_word_0_r },
		{ 0xd00000, 0xd007ff, MRA16_RAM },	/* spriteram */
		{ 0xe00000, 0xe003ff, chasehq_motor_r },	/* motor cpu */
	MEMORY_END
	
	static MEMORY_WRITE16_START( chasehq_writemem )
		{ 0x000000, 0x07ffff, MWA16_ROM },
		{ 0x100000, 0x107fff, MWA16_RAM },
		{ 0x108000, 0x10bfff, sharedram_w, &taitoz_sharedram, &taitoz_sharedram_size },
		{ 0x10c000, 0x10ffff, MWA16_RAM },
		{ 0x400000, 0x400001, TC0220IOC_halfword_portreg_w },
		{ 0x400002, 0x400003, TC0220IOC_halfword_port_w },
		{ 0x800000, 0x800001, cpua_ctrl_w },
		{ 0x820000, 0x820003, taitoz_sound_w },
		{ 0xa00000, 0xa00007, TC0110PCR_step1_word_w },	/* palette */
		{ 0xc00000, 0xc0ffff, TC0100SCN_word_0_w },	/* tilemaps */
		{ 0xc20000, 0xc2000f, TC0100SCN_ctrl_word_0_w },
		{ 0xd00000, 0xd007ff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0xe00000, 0xe003ff, chasehq_motor_w },	/* motor cpu */
	MEMORY_END
	
	static MEMORY_READ16_START( chq_cpub_readmem )
		{ 0x000000, 0x01ffff, MRA16_ROM },
		{ 0x100000, 0x103fff, MRA16_RAM },
		{ 0x108000, 0x10bfff, sharedram_r },
		{ 0x800000, 0x801fff, TC0150ROD_word_r },
	MEMORY_END
	
	static MEMORY_WRITE16_START( chq_cpub_writemem )
		{ 0x000000, 0x01ffff, MWA16_ROM },
		{ 0x100000, 0x103fff, MWA16_RAM },
		{ 0x108000, 0x10bfff, sharedram_w, &taitoz_sharedram },
		{ 0x800000, 0x801fff, TC0150ROD_word_w },
	MEMORY_END
	
	
	static MEMORY_READ16_START( bshark_readmem )
		{ 0x000000, 0x07ffff, MRA16_ROM },
		{ 0x100000, 0x10ffff, MRA16_RAM },	/* main CPUA ram */
		{ 0x110000, 0x113fff, sharedram_r },
		{ 0x400000, 0x40000f, TC0220IOC_halfword_r },
		{ 0x800000, 0x800007, bshark_stick_r },
		{ 0xa00000, 0xa01fff, paletteram16_word_r },	/* palette */
		{ 0xc00000, 0xc00fff, MRA16_RAM },	/* spriteram */
		{ 0xd00000, 0xd0ffff, TC0100SCN_word_0_r },	/* tilemaps */
		{ 0xd20000, 0xd2000f, TC0100SCN_ctrl_word_0_r },
	MEMORY_END
	
	static MEMORY_WRITE16_START( bshark_writemem )
		{ 0x000000, 0x07ffff, MWA16_ROM },
		{ 0x100000, 0x10ffff, MWA16_RAM },
		{ 0x110000, 0x113fff, sharedram_w, &taitoz_sharedram, &taitoz_sharedram_size },
		{ 0x400000, 0x40000f, TC0220IOC_halfword_w },
		{ 0x600000, 0x600001, cpua_noz80_ctrl_w },
		{ 0x800000, 0x800007, bshark_stick_w },
		{ 0xa00000, 0xa01fff, paletteram16_xBBBBBGGGGGRRRRR_word_w, &paletteram16 },
		{ 0xc00000, 0xc00fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0xd00000, 0xd0ffff, TC0100SCN_word_0_w },	/* tilemaps */
		{ 0xd20000, 0xd2000f, TC0100SCN_ctrl_word_0_w },
	MEMORY_END
	
	static MEMORY_READ16_START( bshark_cpub_readmem )
		{ 0x000000, 0x07ffff, MRA16_ROM },
		{ 0x108000, 0x10bfff, MRA16_RAM },
		{ 0x110000, 0x113fff, sharedram_r },
	//	{ 0x40000a, 0x40000b, taitoz_unknown_r },	// ???
		{ 0x600000, 0x600001, YM2610_status_port_0_A_lsb_r },
		{ 0x600002, 0x600003, YM2610_read_port_0_lsb_r },
		{ 0x600004, 0x600005, YM2610_status_port_0_B_lsb_r },
		{ 0x60000c, 0x60000d, MRA16_NOP },
		{ 0x60000e, 0x60000f, MRA16_NOP },
		{ 0x800000, 0x801fff, TC0150ROD_word_r },	/* "root ram" */
	MEMORY_END
	
	static MEMORY_WRITE16_START( bshark_cpub_writemem )
		{ 0x000000, 0x07ffff, MWA16_ROM },
		{ 0x108000, 0x10bfff, MWA16_RAM },
		{ 0x110000, 0x113fff, sharedram_w },
	//	{ 0x400000, 0x400007, MWA16_NOP },   // pan ???
		{ 0x600000, 0x600001, YM2610_control_port_0_A_lsb_w },
		{ 0x600002, 0x600003, YM2610_data_port_0_A_lsb_w },
		{ 0x600004, 0x600005, YM2610_control_port_0_B_lsb_w },
		{ 0x600006, 0x600007, YM2610_data_port_0_B_lsb_w },
		{ 0x60000c, 0x60000d, MWA16_NOP },	// interrupt controller?
		{ 0x60000e, 0x60000f, MWA16_NOP },
		{ 0x800000, 0x801fff, TC0150ROD_word_w },	/* "root ram" */
	MEMORY_END
	
	
	static MEMORY_READ16_START( sci_readmem )
		{ 0x000000, 0x07ffff, MRA16_ROM },
		{ 0x100000, 0x107fff, MRA16_RAM },	/* main CPUA ram */
		{ 0x108000, 0x10bfff, sharedram_r },	/* extent ?? */
		{ 0x10c000, 0x10ffff, MRA16_RAM },	/* extra CPUA ram */
		{ 0x200000, 0x20000f, TC0220IOC_halfword_r },
		{ 0x200010, 0x20001f, sci_steer_input_r },
		{ 0x420000, 0x420003, taitoz_sound_r },
		{ 0x800000, 0x801fff, paletteram16_word_r },
		{ 0xa00000, 0xa0ffff, TC0100SCN_word_0_r },	/* tilemaps */
		{ 0xa20000, 0xa2000f, TC0100SCN_ctrl_word_0_r },
		{ 0xc00000, 0xc03fff, MRA16_RAM },	/* spriteram */	// Raine draws only 0x1000
		{ 0xc08000, 0xc08001, sci_spriteframe_r },	// debugging
	MEMORY_END
	
	static MEMORY_WRITE16_START( sci_writemem )
		{ 0x000000, 0x07ffff, MWA16_ROM },
		{ 0x100000, 0x107fff, MWA16_RAM },
		{ 0x108000, 0x10bfff, sharedram_w, &taitoz_sharedram, &taitoz_sharedram_size },
		{ 0x10c000, 0x10ffff, MWA16_RAM },
		{ 0x200000, 0x20000f, TC0220IOC_halfword_w },
	//	{ 0x400000, 0x400001, cpua_ctrl_w },	// ?? doesn't seem to fit what's written
		{ 0x420000, 0x420003, taitoz_sound_w },
		{ 0x800000, 0x801fff, paletteram16_xBBBBBGGGGGRRRRR_word_w, &paletteram16 },
		{ 0xa00000, 0xa0ffff, TC0100SCN_word_0_w },	/* tilemaps */
		{ 0xa20000, 0xa2000f, TC0100SCN_ctrl_word_0_w },
		{ 0xc00000, 0xc03fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0xc08000, 0xc08001, sci_spriteframe_w },
	MEMORY_END
	
	static MEMORY_READ16_START( sci_cpub_readmem )
		{ 0x000000, 0x01ffff, MRA16_ROM },
		{ 0x200000, 0x203fff, MRA16_RAM },
		{ 0x208000, 0x20bfff, sharedram_r },
		{ 0xa00000, 0xa01fff, TC0150ROD_word_r },
	MEMORY_END
	
	static MEMORY_WRITE16_START( sci_cpub_writemem )
		{ 0x000000, 0x01ffff, MWA16_ROM },
		{ 0x200000, 0x203fff, MWA16_RAM },
		{ 0x208000, 0x20bfff, sharedram_w, &taitoz_sharedram },
		{ 0xa00000, 0xa01fff, TC0150ROD_word_w },
	MEMORY_END
	
	
	static MEMORY_READ16_START( nightstr_readmem )
		{ 0x000000, 0x07ffff, MRA16_ROM },
		{ 0x100000, 0x10ffff, MRA16_RAM },	/* main CPUA ram */
		{ 0x110000, 0x113fff, sharedram_r },
		{ 0x400000, 0x40000f, TC0220IOC_halfword_r },
		{ 0x820000, 0x820003, taitoz_sound_r },
		{ 0xa00000, 0xa00007, TC0110PCR_word_r },	/* palette */
		{ 0xc00000, 0xc0ffff, TC0100SCN_word_0_r },	/* tilemaps */
		{ 0xc20000, 0xc2000f, TC0100SCN_ctrl_word_0_r },
		{ 0xd00000, 0xd007ff, MRA16_RAM },	/* spriteram */
		{ 0xe40000, 0xe40007, bshark_stick_r },
	MEMORY_END
	
	static MEMORY_WRITE16_START( nightstr_writemem )
		{ 0x000000, 0x07ffff, MWA16_ROM },
		{ 0x100000, 0x10ffff, MWA16_RAM },
		{ 0x110000, 0x113fff, sharedram_w, &taitoz_sharedram, &taitoz_sharedram_size },
		{ 0x400000, 0x40000f, TC0220IOC_halfword_w },
		{ 0x800000, 0x800001, cpua_ctrl_w },
		{ 0x820000, 0x820003, taitoz_sound_w },
		{ 0xa00000, 0xa00007, TC0110PCR_step1_word_w },	/* palette */
		{ 0xc00000, 0xc0ffff, TC0100SCN_word_0_w },	/* tilemaps */
		{ 0xc20000, 0xc2000f, TC0100SCN_ctrl_word_0_w },
		{ 0xd00000, 0xd007ff, MWA16_RAM, &spriteram16, &spriteram_size },
	//	{ 0xe00000, 0xe00001, MWA16_NOP },	/* ??? */
	//	{ 0xe00008, 0xe00009, MWA16_NOP },	/* ??? */
	//	{ 0xe00010, 0xe00011, MWA16_NOP },	/* ??? */
		{ 0xe40000, 0xe40007, bshark_stick_w },
	MEMORY_END
	
	static MEMORY_READ16_START( nightstr_cpub_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x100000, 0x103fff, MRA16_RAM },
		{ 0x104000, 0x107fff, sharedram_r },
		{ 0x800000, 0x801fff, TC0150ROD_word_r },	/* "root ram" */
	MEMORY_END
	
	static MEMORY_WRITE16_START( nightstr_cpub_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x100000, 0x103fff, MWA16_RAM },
		{ 0x104000, 0x107fff, sharedram_w, &taitoz_sharedram },
		{ 0x800000, 0x801fff, TC0150ROD_word_w },	/* "root ram" */
	MEMORY_END
	
	
	static MEMORY_READ16_START( aquajack_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x100000, 0x103fff, MRA16_RAM },	/* main CPUA ram */
		{ 0x104000, 0x107fff, sharedram_r },
		{ 0x300000, 0x300007, TC0110PCR_word_r },	/* palette */
		{ 0x800000, 0x801fff, TC0150ROD_word_r },	/* (like Contcirc, uses CPUA for road) */
		{ 0xa00000, 0xa0ffff, TC0100SCN_word_0_r },	/* tilemaps */
		{ 0xa20000, 0xa2000f, TC0100SCN_ctrl_word_0_r },
		{ 0xc40000, 0xc403ff, MRA16_RAM },	/* spriteram */
	MEMORY_END
	
	static MEMORY_WRITE16_START( aquajack_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x100000, 0x103fff, MWA16_RAM },
		{ 0x104000, 0x107fff, sharedram_w, &taitoz_sharedram, &taitoz_sharedram_size },
		{ 0x200000, 0x200001, cpua_ctrl_w },	// not needed, but it's probably like the others
		{ 0x300000, 0x300007, TC0110PCR_step1_word_w },	/* palette */
		{ 0x800000, 0x801fff, TC0150ROD_word_w },
		{ 0xa00000, 0xa0ffff, TC0100SCN_word_0_w },	/* tilemaps */
		{ 0xa20000, 0xa2000f, TC0100SCN_ctrl_word_0_w },
		{ 0xc40000, 0xc403ff, MWA16_RAM, &spriteram16, &spriteram_size },
	MEMORY_END
	
	static MEMORY_READ16_START( aquajack_cpub_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x100000, 0x103fff, MRA16_RAM },
		{ 0x104000, 0x107fff, sharedram_r },
		{ 0x200000, 0x20000f, TC0220IOC_halfword_r },
		{ 0x300000, 0x300003, taitoz_sound_r },
		{ 0x800800, 0x80083f, aquajack_unknown_r }, // Read regularly after write to 800800...
	//	{ 0x900000, 0x900007, taitoz_unknown_r },
	MEMORY_END
	
	static MEMORY_WRITE16_START( aquajack_cpub_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x100000, 0x103fff, MWA16_RAM },
		{ 0x104000, 0x107fff, sharedram_w, &taitoz_sharedram },
		{ 0x200000, 0x20000f, TC0220IOC_halfword_w },
		{ 0x300000, 0x300003, taitoz_sound_w },
	//	{ 0x800800, 0x800801, taitoz_unknown_w },
	//	{ 0x900000, 0x900007, taitoz_unknown_w },
	MEMORY_END
	
	
	static MEMORY_READ16_START( spacegun_readmem )
		{ 0x000000, 0x07ffff, MRA16_ROM },
		{ 0x30c000, 0x30ffff, MRA16_RAM },	/* local CPUA ram */
		{ 0x310000, 0x31ffff, sharedram_r },	/* extent correct acc. to CPUB inits */
		{ 0x500000, 0x5005ff, MRA16_RAM },	/* spriteram */
		{ 0x900000, 0x90ffff, TC0100SCN_word_0_r },	/* tilemaps */
		{ 0x920000, 0x92000f, TC0100SCN_ctrl_word_0_r },
		{ 0xb00000, 0xb00007, TC0110PCR_word_r },	/* palette */
	MEMORY_END
	
	static MEMORY_WRITE16_START( spacegun_writemem )
		{ 0x000000, 0x07ffff, MWA16_ROM },
		{ 0x30c000, 0x30ffff, MWA16_RAM },
		{ 0x310000, 0x31ffff, sharedram_w, &taitoz_sharedram, &taitoz_sharedram_size  },
		{ 0x500000, 0x5005ff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0x900000, 0x90ffff, TC0100SCN_word_0_w },	/* tilemaps */
		{ 0x920000, 0x92000f, TC0100SCN_ctrl_word_0_w },
		{ 0xb00000, 0xb00007, TC0110PCR_step1_rbswap_word_w },	/* palette */
	MEMORY_END
	
	static MEMORY_READ16_START( spacegun_cpub_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x20c000, 0x20ffff, MRA16_RAM },	/* local CPUB ram */
		{ 0x210000, 0x21ffff, sharedram_r },
		{ 0x800000, 0x80000f, spacegun_input_bypass_r },
		{ 0xc00000, 0xc00001, YM2610_status_port_0_A_lsb_r },
		{ 0xc00002, 0xc00003, YM2610_read_port_0_lsb_r },
		{ 0xc00004, 0xc00005, YM2610_status_port_0_B_lsb_r },
		{ 0xc0000c, 0xc0000d, MRA16_NOP },
		{ 0xc0000e, 0xc0000f, MRA16_NOP },
		{ 0xf00000, 0xf00007, spacegun_lightgun_r },
	MEMORY_END
	
	static MEMORY_WRITE16_START( spacegun_cpub_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x20c000, 0x20ffff, MWA16_RAM },
		{ 0x210000, 0x21ffff, sharedram_w },
		{ 0x800000, 0x80000f, spacegun_output_bypass_w },
		{ 0xc00000, 0xc00001, YM2610_control_port_0_A_lsb_w },
		{ 0xc00002, 0xc00003, YM2610_data_port_0_A_lsb_w },
		{ 0xc00004, 0xc00005, YM2610_control_port_0_B_lsb_w },
		{ 0xc00006, 0xc00007, YM2610_data_port_0_B_lsb_w },
		{ 0xc0000c, 0xc0000d, MWA16_NOP },	// interrupt controller?
		{ 0xc0000e, 0xc0000f, MWA16_NOP },
	//	{ 0xc20000, 0xc20003, YM2610_???? },	/* Pan (acc. to Raine) */
	//	{ 0xe00000, 0xe00001, MWA16_NOP },	/* ??? */
		{ 0xf00000, 0xf00007, spacegun_lightgun_w },
	MEMORY_END
	
	
	static MEMORY_READ16_START( dblaxle_readmem )
		{ 0x000000, 0x07ffff, MRA16_ROM },
		{ 0x200000, 0x203fff, MRA16_RAM },	/* main CPUA ram */
		{ 0x210000, 0x21ffff, sharedram_r },
		{ 0x400000, 0x40000f, TC0510NIO_halfword_wordswap_r },
		{ 0x400010, 0x40001f, dblaxle_steer_input_r },
		{ 0x620000, 0x620003, taitoz_sound_r },
		{ 0x800000, 0x801fff, paletteram16_word_r },	/* palette */
		{ 0xa00000, 0xa0ffff, TC0480SCP_word_r },	  /* tilemaps */
		{ 0xa30000, 0xa3002f, TC0480SCP_ctrl_word_r },
		{ 0xc00000, 0xc03fff, MRA16_RAM },	/* spriteram */
		{ 0xc08000, 0xc08001, sci_spriteframe_r },	// debugging
	MEMORY_END
	
	static MEMORY_WRITE16_START( dblaxle_writemem )
		{ 0x000000, 0x07ffff, MWA16_ROM },
		{ 0x200000, 0x203fff, MWA16_RAM },
		{ 0x210000, 0x21ffff, sharedram_w, &taitoz_sharedram, &taitoz_sharedram_size },
		{ 0x400000, 0x40000f, TC0510NIO_halfword_wordswap_w },
		{ 0x600000, 0x600001, cpua_ctrl_w },	/* could this be causing int6 ? */
		{ 0x620000, 0x620003, taitoz_sound_w },
		{ 0x800000, 0x801fff, paletteram16_xBBBBBGGGGGRRRRR_word_w, &paletteram16 },
		{ 0x900000, 0x90ffff, TC0480SCP_word_w },	  /* tilemap mirror */
		{ 0xa00000, 0xa0ffff, TC0480SCP_word_w },	  /* tilemaps */
		{ 0xa30000, 0xa3002f, TC0480SCP_ctrl_word_w },
		{ 0xc00000, 0xc03fff, MWA16_RAM, &spriteram16, &spriteram_size }, /* mostly unused ? */
		{ 0xc08000, 0xc08001, sci_spriteframe_w },	/* set in int6, seems to stay zero */
	MEMORY_END
	
	static MEMORY_READ16_START( dblaxle_cpub_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x100000, 0x103fff, MRA16_RAM },
		{ 0x110000, 0x11ffff, sharedram_r },
		{ 0x300000, 0x301fff, TC0150ROD_word_r },
		{ 0x500000, 0x503fff, MRA16_RAM },	/* network ram ? (see Gunbustr) */
	MEMORY_END
	
	static MEMORY_WRITE16_START( dblaxle_cpub_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x100000, 0x103fff, MWA16_RAM },
		{ 0x110000, 0x11ffff, sharedram_w, &taitoz_sharedram },
		{ 0x300000, 0x301fff, TC0150ROD_word_w },
		{ 0x500000, 0x503fff, MWA16_RAM },	/* network ram ? (see Gunbustr) */
	MEMORY_END
	
	
	/***************************************************************************/
	
	public static Memory_ReadAddress z80_sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new Memory_ReadAddress( 0x4000, 0x7fff, MRA_BANK10 ),
		new Memory_ReadAddress( 0xc000, 0xdfff, MRA_RAM ),
		new Memory_ReadAddress( 0xe000, 0xe000, YM2610_status_port_0_A_r ),
		new Memory_ReadAddress( 0xe001, 0xe001, YM2610_read_port_0_r ),
		new Memory_ReadAddress( 0xe002, 0xe002, YM2610_status_port_0_B_r ),
		new Memory_ReadAddress( 0xe200, 0xe200, MRA_NOP ),
		new Memory_ReadAddress( 0xe201, 0xe201, taitosound_slave_comm_r ),
		new Memory_ReadAddress( 0xea00, 0xea00, MRA_NOP ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress z80_sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xdfff, MWA_RAM ),
		new Memory_WriteAddress( 0xe000, 0xe000, YM2610_control_port_0_A_w ),
		new Memory_WriteAddress( 0xe001, 0xe001, YM2610_data_port_0_A_w ),
		new Memory_WriteAddress( 0xe002, 0xe002, YM2610_control_port_0_B_w ),
		new Memory_WriteAddress( 0xe003, 0xe003, YM2610_data_port_0_B_w ),
		new Memory_WriteAddress( 0xe200, 0xe200, taitosound_slave_port_w ),
		new Memory_WriteAddress( 0xe201, 0xe201, taitosound_slave_comm_w ),
		new Memory_WriteAddress( 0xe400, 0xe403, MWA_NOP ), /* pan */
		new Memory_WriteAddress( 0xee00, 0xee00, MWA_NOP ), /* ? */
		new Memory_WriteAddress( 0xf000, 0xf000, MWA_NOP ), /* ? */
		new Memory_WriteAddress( 0xf200, 0xf200, sound_bankswitch_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	/***********************************************************
				 INPUT PORTS, DIPs
	***********************************************************/
	
	static InputPortPtr input_ports_contcirc = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN2 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_COIN1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW,  IPT_SERVICE1 );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON6 | IPF_PLAYER1 );/* 3 for accel [7 levels] */
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON5 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );/* main accel key */
	
		PORT_START();       /* IN1: b3 not mapped: standardized on holding b4=lo gear */
		PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW,  IPT_TILT );	PORT_BIT( 0x08, IP_ACTIVE_LOW,  IPT_START1 );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER1 );/* gear shift lo/hi */
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON8 | IPF_PLAYER1 );/* 3 for brake [7 levels] */
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON7 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );/* main brake key */
	
		PORT_START();       /* IN2, unused */
	
		PORT_START();       /* IN3, "handle" used for steering */
		PORT_ANALOG( 0xffff, 0x00, IPT_AD_STICK_X | IPF_REVERSE | IPF_PLAYER1, 50, 15, 0xff9f, 0x60);
		PORT_START();       /* IN4, fake allowing digital steer */
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER1 );	PORT_DIPNAME( 0x10, 0x00, "Steering type" );	PORT_DIPSETTING(    0x10, "Digital" );	PORT_DIPSETTING(    0x00, "Analogue" );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_chasehq = new InputPortPtr(){ public void handler() { 	// IN3-6 perhaps used with cockpit setup? //
		PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x03, "Upright / Steering Lock" );	PORT_DIPSETTING(    0x02, "Upright / No Steering Lock" );	PORT_DIPSETTING(    0x01, "Full Throttle Convert, Cockpit" );	PORT_DIPSETTING(    0x00, "Full Throttle Convert, Deluxe" );	PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, "Timer Setting" );	PORT_DIPSETTING(    0x0c, "60 Seconds" );	PORT_DIPSETTING(    0x08, "70 Seconds" );	PORT_DIPSETTING(    0x04, "65 Seconds" );	PORT_DIPSETTING(    0x00, "55 Seconds" );	PORT_DIPNAME( 0x10, 0x10, "Turbos Stocked" );	PORT_DIPSETTING(    0x10, "3" );	PORT_DIPSETTING(    0x00, "5" );	PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "Damage Cleared at Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Allow Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW,  IPT_SERVICE1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER1 );/* brake */
		PORT_BIT( 0x40, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_BUTTON3 | IPF_PLAYER1 );/* turbo */
		PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_TILT );	PORT_BIT( 0x04, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW,  IPT_START1 );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER1 );/* gear */
		PORT_BIT( 0x20, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER1 );/* accel */
		PORT_BIT( 0x40, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN );
		PORT_START();       /* IN2, unused */
	
		PORT_START();       /* IN3, ??? */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN4, ??? */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN5, ??? */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN6, ??? */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN7, steering */
		PORT_ANALOG( 0xffff, 0x00, IPT_AD_STICK_X | IPF_PLAYER1, 50, 25, 0xff80, 0x7f );
		PORT_START();       /* IN8, fake allowing digital steer */
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER1 );	PORT_DIPNAME( 0x10, 0x00, "Steering type" );	PORT_DIPSETTING(    0x10, "Digital" );	PORT_DIPSETTING(    0x00, "Analogue" );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_bshark = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, "Mirror screen" );	PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN1, unused */
	
		PORT_START();       /* IN2, b2-5 affect sound num in service mode but otherwise useless (?) */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );/* "Fire" */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON6 | IPF_PLAYER1 );/* same as "Fire" */
	
		PORT_START(); 	/* values chosen to match allowed crosshair area */
		PORT_ANALOG( 0xff, 0x00, IPT_AD_STICK_X | IPF_REVERSE | IPF_PLAYER1, 20, 4, 0xcc, 0x35);
		PORT_START(); 	/* "X adjust" */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START(); 	/* values chosen to match allowed crosshair area */
		PORT_ANALOG( 0xff, 0x00, IPT_AD_STICK_Y | IPF_PLAYER1, 20, 4, 0xd5, 0x32);
		PORT_START(); 	/* "Y adjust" */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_sci = new InputPortPtr(){ public void handler() { 	// dsws may be slightly wrong
		PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x01, "Cockpit" );	PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, "Timer Setting" );	PORT_DIPSETTING(    0x0c, "60 Seconds" );	PORT_DIPSETTING(    0x08, "70 Seconds" );	PORT_DIPSETTING(    0x04, "65 Seconds" );	PORT_DIPSETTING(    0x00, "55 Seconds" );	PORT_DIPNAME( 0x10, 0x10, "Turbos Stocked" );	PORT_DIPSETTING(    0x10, "3" );	PORT_DIPSETTING(    0x00, "5" );	PORT_DIPNAME( 0x20, 0x20, "Respond to Controls" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "Damage Cleared at Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Siren Volume" );	PORT_DIPSETTING(    0x80, "Normal" );	PORT_DIPSETTING(    0x00, "Low" );
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );/* fire */
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );/* brake */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_BUTTON5 | IPF_PLAYER1 );/* turbo */
		PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_TILT );	PORT_BIT( 0x04, IP_ACTIVE_LOW,  IPT_BUTTON6 | IPF_PLAYER1 );/* "center" */
		PORT_BIT( 0x08, IP_ACTIVE_LOW,  IPT_START1 );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER1 );/* gear */
		PORT_BIT( 0x20, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER1 );/* accel */
		PORT_BIT( 0x40, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN );
		PORT_START();       /* IN2, unused */
	
		PORT_START();       /* IN3, steering */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_PLAYER1, 50, 15, 0x20, 0xdf );
		PORT_START();       /* IN4, fake allowing digital steer */
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER1 );	PORT_DIPNAME( 0x10, 0x00, "Steering type" );	PORT_DIPSETTING(    0x10, "Digital" );	PORT_DIPSETTING(    0x00, "Analogue" );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_nightstr = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_TILT );
		PORT_START();       /* IN1, unused */
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_START(); 	/* boundary values seem about right, bit too wide perhaps */
		PORT_ANALOG( 0xff, 0x00, IPT_AD_STICK_X | IPF_PLAYER1, 20, 10, 0xb8, 0x49);
		PORT_START(); 	/* boundary values seem about right, bit too wide perhaps */
		PORT_ANALOG( 0xff, 0x00, IPT_AD_STICK_Y | IPF_REVERSE | IPF_PLAYER1, 20, 10, 0xb8, 0x49);
		PORT_START(); 	/* X offset */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START(); 	/* Y offset */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_aquajack = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x80, "Cockpit" );	PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x20, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_B") );	/* Actually "Price to Continue" */
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_2C") );	/* Actually "Same as current Coin A" */
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0xc0, "Normal" );	PORT_DIPSETTING(    0x40, "Easy" );	PORT_DIPSETTING(    0x80, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x30, "50k" );	PORT_DIPSETTING(    0x10, "80k" );	PORT_DIPSETTING(    0x20, "100k" );	PORT_DIPSETTING(    0x00, "30k" );	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x0c, "3" );	PORT_DIPSETTING(    0x04, "2" );	PORT_DIPSETTING(    0x08, "1" );	PORT_DIPSETTING(    0x00, "5" );	PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unused") ); /* Dips 7 & 8 shown as "Do Not Touch" in manual */
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN2, what is it ??? */
		PORT_ANALOG( 0xff, 0x80, IPT_DIAL | IPF_PLAYER1, 50, 10, 0, 0 );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_spacegun = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Always have gunsight power up" );	PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Disable Pedal (?); )
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1);	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2);
		PORT_START();       /* IN1, unused */
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_REVERSE | IPF_PLAYER1, 20, 22, 0, 0xff);
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_PLAYER1, 20, 22, 0, 0xff);
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_REVERSE | IPF_PLAYER2, 20, 22, 0, 0xff);
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_PLAYER2, 20, 22, 0, 0xff);
		PORT_START(); 	/* Fake DSW */
		PORT_BITX(    0x01, 0x00, IPT_DIPSWITCH_NAME | IPF_TOGGLE, "Show gun target", KEYCODE_F1, IP_JOY_NONE );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_dblaxle = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x00, "Multi-machine hookup ?" );// doesn't boot if on
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, "Nitros Stocked ???" );	PORT_DIPSETTING(    0x10, "3" );	PORT_DIPSETTING(    0x00, "5" );	PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );/* shift */
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );/* brake */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );/* "back" */
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );/* nitro */
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON6 | IPF_PLAYER1 );/* "center" */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );/* accel */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN2, unused */
	
		PORT_START();       /* IN3, steering: unsure of range */
		PORT_ANALOG( 0xffff, 0x00, IPT_AD_STICK_X | IPF_PLAYER1, 20, 10, 0xffc0, 0x3f );
		PORT_START();       /* IN4, fake allowing digital steer */
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER1 );	PORT_DIPNAME( 0x10, 0x00, "Steering type" );	PORT_DIPSETTING(    0x10, "Digital" );	PORT_DIPSETTING(    0x00, "Analogue" );INPUT_PORTS_END(); }}; 
	
	
	/***********************************************************
					GFX DECODING
	
	Raine gives these details on obj layouts:
	- Chase HQ
	- Night Striker
	
	00000-3FFFF = Object 128x128 [16x16] [19900/80:0332] gfx bank#1
	40000-5FFFF = Object  64x128 [16x16] [0CC80/40:0332] gfx bank#2
	60000-7FFFF = Object  32x128 [16x16] [06640/20:0332] gfx bank#2
	
	- Top Speed
	- Full Throttle
	- Continental Circus
	
	00000-7FFFF = Object 128x128 [16x8] [xxxxx/100:xxxx] gfx bank#1
	
	- Aqua Jack
	- Chase HQ 2
	- Battle Shark
	- Space Gun
	- Operation Thunderbolt
	
	00000-7FFFF = Object 64x64 [16x8] [xxxxx/40:xxxx] gfx bank#1
	***********************************************************/
	
	static GfxLayout tile16x8_layout = new GfxLayout
	(
		16,8,	/* 16*8 sprites */
		RGN_FRAC(1,1),
		4,	/* 4 bits per pixel */
		new int[] { 0, 8, 16, 24 },
		new int[] { 32, 33, 34, 35, 36, 37, 38, 39, 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64 },
		64*8	/* every sprite takes 64 consecutive bytes */
	);
	
	static GfxLayout tile16x16_layout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		RGN_FRAC(1,1),
		4,	/* 4 bits per pixel */
		new int[] { 0, 8, 16, 24 },
		new int[] { 32, 33, 34, 35, 36, 37, 38, 39, 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*64, 1*64,  2*64,  3*64,  4*64,  5*64,  6*64,  7*64,
		  8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
		64*16	/* every sprite takes 128 consecutive bytes */
	);
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		RGN_FRAC(1,1),
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 2*4, 3*4, 0*4, 1*4, 6*4, 7*4, 4*4, 5*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8	/* every sprite takes 32 consecutive bytes */
	);
	
	static GfxLayout dblaxle_charlayout = new GfxLayout
	(
		16,16,    /* 16*16 characters */
		RGN_FRAC(1,1),
		4,        /* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 1*4, 0*4, 5*4, 4*4, 3*4, 2*4, 7*4, 6*4, 9*4, 8*4, 13*4, 12*4, 11*4, 10*4, 15*4, 14*4 },
		new int[] { 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64, 8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
		128*8     /* every sprite takes 128 consecutive bytes */
	);
	
	/* taitoic.c TC0100SCN routines expect scr stuff to be in second gfx
	   slot, so 2nd batch of obj must be placed third */
	
	static GfxDecodeInfo chasehq_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX2, 0x0, tile16x16_layout,  0, 256 ),	/* sprite parts */
		new GfxDecodeInfo( REGION_GFX1, 0x0, charlayout,  0, 256 ),		/* sprites  playfield */
		new GfxDecodeInfo( REGION_GFX4, 0x0, tile16x16_layout,  0, 256 ),	/* sprite parts */
		// Road Lines too wide for gfxdecoding ?
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo sci_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX2, 0x0, tile16x8_layout,  0, 256 ),	/* sprite parts */
		new GfxDecodeInfo( REGION_GFX1, 0x0, charlayout,  0, 256 ),		/* sprites  playfield */
		// Road Lines too wide for gfxdecoding ?
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo contcirc_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX2, 0x0, tile16x8_layout,  0, 256 ),	/* sprite parts */
		new GfxDecodeInfo( REGION_GFX1, 0x0, charlayout,  0, 256 ),		/* sprites  playfield */
		// Road Lines too wide for gfxdecoding ?
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo spacegun_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX2, 0, tile16x8_layout,  0, 256 ),	/* sprite parts */
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,  0, 256 ),	/* sprites  playfield */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo dblaxle_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX2, 0x0, tile16x8_layout,  0, 256 ),	/* sprite parts */
		new GfxDecodeInfo( REGION_GFX1, 0x0, dblaxle_charlayout,  0, 256 ),	/* sprites  playfield */
		// Road Lines too wide for gfxdecoding ?
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	/**************************************************************
				     YM2610 (SOUND)
	
	The first interface is for game boards with twin 68000 and Z80.
	
	Interface B is for games which lack a Z80 (Spacegun, Bshark).
	**************************************************************/
	
	/* handler called by the YM2610 emulator when the internal timers cause an IRQ */
	static void irqhandler(int irq)	// assumes Z80 sandwiched between 68Ks
	{
		cpu_set_irq_line(1,0,irq ? ASSERT_LINE : CLEAR_LINE);
	}
	
	/* handler called by the YM2610 emulator when the internal timers cause an IRQ */
	static void irqhandlerb(int irq)
	{
		// DG: this is probably specific to Z80 and wrong?
	//	cpu_set_irq_line(1,0,irq ? ASSERT_LINE : CLEAR_LINE);
	}
	
	static YM2610interface ym2610_interface = new YM2610interface
	(
		1,	/* 1 chip */
		16000000/2,	/* 8 MHz ?? */
		new int[] { 30 },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteYmHandlerPtr[] { irqhandler },
		new int[] { REGION_SOUND2 },	/* Delta-T */
		new int[] { REGION_SOUND1 },	/* ADPCM */
		new int[] { YM3012_VOL(60,MIXER_PAN_LEFT,60,MIXER_PAN_RIGHT) }
	);
	
	static YM2610interface ym2610_interfaceb = new YM2610interface
	(
		1,	/* 1 chip */
		16000000/2,	/* 8 MHz ?? */
		new int[] { 30 },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteYmHandlerPtr[] { irqhandlerb },
		new int[] { REGION_SOUND2 },	/* Delta-T */
		new int[] { REGION_SOUND1 },	/* ADPCM */
		new int[] { YM3012_VOL(60,MIXER_PAN_LEFT,60,MIXER_PAN_RIGHT) }
	);
	
	
	/**************************************************************
				     SUBWOOFER (SOUND)
	**************************************************************/
	
	static public static ShStartPtr subwoofer_sh_start = new ShStartPtr() { public int handler(MachineSound msound) 
	{
		/* Adjust the lowpass filter of the first three YM2610 channels */
	
		/* 150 Hz is a common top frequency played by a generic */
		/* subwoofer, the real Arcade Machine may differs */
	
		mixer_set_lowpass_frequency(0,50);
		mixer_set_lowpass_frequency(1,50);
		mixer_set_lowpass_frequency(2,50);
	
		return 0;
	} };
	
	static CustomSound_interface subwoofer_interface = new CustomSound_interface
	(
		subwoofer_sh_start,
		null, /* none */
		null /* none */
	);
	
	
	/***********************************************************
				     MACHINE DRIVERS
	
	Chasehq2 needs high interleaving to have sound.
	Bshark needs the high cpu interleaving to run test mode.
	Nightstr needed the high cpu interleaving to get through init.
	Aquajack has it VERY high to cure frequent sound-related
	hangs.
	Dblaxle has 10 to boot up reliably.
	
	Suspect minimum interleaving of 10 or so may be needed to
	avoid road glitches: mostly it's CPUB which writes to road
	chip, so presumably syncing between it and the master CPUA
	is important.
	***********************************************************/
	
	static MachineDriver machine_driver_contcirc = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz ??? */
				contcirc_readmem,contcirc_writemem,null,null,
				contcirc_interrupt, 1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				16000000/4,	/* 4 MHz ??? */
				z80_sound_readmem, z80_sound_writemem,null,null,
				ignore_interrupt,0	/* IRQs are triggered by the YM2610 */
			),
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz ??? */
				contcirc_cpub_readmem,contcirc_cpub_writemem,null,null,
				contcirc_cpub_interrupt, 1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* CPU slices */
		null,
	
		/* video hardware */
		40*8, 32*8, new rectangle( 0*8, 40*8-1, 2*8, 32*8-1 ),
	
		contcirc_gfxdecodeinfo,
		4096, 4096,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		taitoz_vh_start,
		taitoz_vh_stop,
		contcirc_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2610,
				ym2610_interface
			),
			new MachineSound(
				SOUND_CUSTOM,
				subwoofer_interface
			)
		}
	);
	
	static MachineDriver machine_driver_chasehq = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz ??? */
				chasehq_readmem,chasehq_writemem,null,null,
				chasehq_interrupt, 1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				16000000/4,	/* 4 MHz ??? */
				z80_sound_readmem, z80_sound_writemem,null,null,
				ignore_interrupt,0	/* IRQs are triggered by the YM2610 */
			),
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz ??? */
				chq_cpub_readmem,chq_cpub_writemem,null,null,
				chq_cpub_interrupt, 1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* CPU slices */
		null,
	
		/* video hardware */
		40*8, 32*8, new rectangle( 0*8, 40*8-1, 2*8, 32*8-1 ),
	
		chasehq_gfxdecodeinfo,
		4096, 4096,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		taitoz_vh_start,
		taitoz_vh_stop,
		chasehq_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2610,
				ym2610_interface
			)
		}
	);
	
	static MachineDriver machine_driver_bshark = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz ??? */
				bshark_readmem,bshark_writemem,null,null,
				bshark_interrupt, 1
			),
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz ??? */
				bshark_cpub_readmem,bshark_cpub_writemem,null,null,
				bshark_cpub_interrupt, 1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		100,	/* CPU slices */
		null,
	
		/* video hardware */
		40*8, 32*8, new rectangle( 0*8, 40*8-1, 2*8, 32*8-1 ),
	
		sci_gfxdecodeinfo,
		4096, 4096,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		taitoz_vh_start,
		taitoz_vh_stop,
		bshark_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2610,
				ym2610_interfaceb
			)
		}
	);
	
	static MachineDriver machine_driver_sci = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz ??? */
				sci_readmem,sci_writemem,null,null,
				sci_interrupt, 1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				16000000/4,	/* 4 MHz ??? */
				z80_sound_readmem, z80_sound_writemem,null,null,
				ignore_interrupt,0	/* IRQs are triggered by the YM2610 */
			),
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz ??? */
				sci_cpub_readmem,sci_cpub_writemem,null,null,
				sci_cpub_interrupt, 1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		50,	/* CPU slices */
		null,
	
		/* video hardware */
		40*8, 32*8, new rectangle( 0*8, 40*8-1, 2*8, 32*8-1 ),
	
		sci_gfxdecodeinfo,
		4096, 4096,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		taitoz_vh_start,
		taitoz_vh_stop,
		sci_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2610,
				ym2610_interface
			)
		}
	);
	
	static MachineDriver machine_driver_nightstr = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz ??? */
				nightstr_readmem,nightstr_writemem,null,null,
				nightstr_interrupt, 1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				16000000/4,	/* 4 MHz ??? */
				z80_sound_readmem, z80_sound_writemem,null,null,
				ignore_interrupt,0	/* IRQs are triggered by the YM2610 */
			),
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz ??? */
				nightstr_cpub_readmem,nightstr_cpub_writemem,null,null,
				nightstr_cpub_interrupt, 1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		100,	/* CPU slices */
		null,
	
		/* video hardware */
		40*8, 32*8, new rectangle( 0*8, 40*8-1, 2*8, 32*8-1 ),
	
		chasehq_gfxdecodeinfo,
		4096, 4096,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		taitoz_vh_start,
		taitoz_vh_stop,
		chasehq_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2610,
				ym2610_interface
			)
		}
	);
	
	static MachineDriver machine_driver_aquajack = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz ??? */
				aquajack_readmem,aquajack_writemem,null,null,
				aquajack_interrupt, 1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				16000000/4,	/* 4 MHz ??? */
				z80_sound_readmem, z80_sound_writemem,null,null,
				ignore_interrupt,0	/* IRQs are triggered by the YM2610 */
			),
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz ??? */
				aquajack_cpub_readmem,aquajack_cpub_writemem,null,null,
				aquajack_cpub_interrupt, 1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		500,	/* CPU slices */
		null,
	
		/* video hardware */
		40*8, 32*8, new rectangle( 0*8, 40*8-1, 2*8, 32*8-1 ),
	
		sci_gfxdecodeinfo,
		4096, 4096,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		taitoz_vh_start,
		taitoz_vh_stop,
		aquajack_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2610,
				ym2610_interface
			)
		}
	);
	
	static MachineDriver machine_driver_spacegun = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				16000000,	/* 16 MHz ??? */
				spacegun_readmem,spacegun_writemem,null,null,
				spacegun_interrupt, 1
			),
			new MachineCPU(
				CPU_M68000,
				16000000,	/* 16 MHz ??? */
				spacegun_cpub_readmem,spacegun_cpub_writemem,null,null,
				spacegun_cpub_interrupt, 1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* CPU slices: sprites much worse at 10, high values don't seem better */
		null,
	
		/* video hardware */
		40*8, 32*8, new rectangle( 0*8, 40*8-1, 2*8, 32*8-1 ),
	
		spacegun_gfxdecodeinfo,
		4096, 4096,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		spacegun_vh_start,
		taitoz_vh_stop,
		spacegun_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2610,
				ym2610_interfaceb
			)
		},
	
		nvram_handler	/* for the eerom */
	
	);
	
	static MachineDriver machine_driver_dblaxle = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				16000000,	/* 16 MHz ??? */
				dblaxle_readmem,dblaxle_writemem,null,null,
				dblaxle_interrupt, 1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				16000000/4,	/* 4 MHz ??? */
				z80_sound_readmem, z80_sound_writemem,null,null,
				ignore_interrupt,0	/* IRQs are triggered by the YM2610 */
			),
			new MachineCPU(
				CPU_M68000,
				16000000,	/* 16 MHz ??? */
				dblaxle_cpub_readmem,dblaxle_cpub_writemem,null,null,
				dblaxle_cpub_interrupt, 1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		10,	/* CPU slices */
		null,
	
		/* video hardware */
		40*8, 32*8, new rectangle( 0*8, 40*8-1, 2*8, 32*8-1 ),
	
		dblaxle_gfxdecodeinfo,
		4096, 4096,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		taitoz_vh_start,
		taitoz_vh_stop,
		dblaxle_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2610,
				ym2610_interface
			)
		}
	);
	
	
	/***************************************************************************
						DRIVERS
	
	Contcirc, Dblaxle sound sample rom order is uncertain as sound imperfect
	***************************************************************************/
	
	static RomLoadPtr rom_contcirc = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 );/* 256K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE( "ic25",      0x00000, 0x20000, 0xf5c92e42 )
		ROM_LOAD16_BYTE( "cc_26.bin", 0x00001, 0x20000, 0x1345ebe6 )
	
		ROM_REGION( 0x40000, REGION_CPU3, 0 );/* 256K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "ic35",      0x00000, 0x20000, 0x16522f2d )
		ROM_LOAD16_BYTE( "cc_36.bin", 0x00001, 0x20000, 0xa1732ea5 )
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* Z80 sound cpu */
		ROM_LOAD( "b33_30",   0x00000, 0x04000, 0xd8746234 );	ROM_CONTINUE(         0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "b33_02", 0x00000, 0x80000, 0xf6fb3ba2 );/* SCR 8x8 */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "b33_06", 0x000000, 0x080000, 0x2cb40599 )	/* OBJ 16x8 */
		ROM_LOAD32_BYTE( "b33_05", 0x000001, 0x080000, 0xbddf9eea )
		ROM_LOAD32_BYTE( "b33_04", 0x000002, 0x080000, 0x8df866a2 )
		ROM_LOAD32_BYTE( "b33_03", 0x000003, 0x080000, 0x4f6c36d9 )
	
		ROM_REGION( 0x80000, REGION_GFX3, 0 );/* don't dispose */
		ROM_LOAD( "b33_01", 0x00000, 0x80000, 0xf11f2be8 );/* ROD, road lines */
	
		ROM_REGION16_LE( 0x80000, REGION_USER1, 0 )
		ROM_LOAD16_WORD( "b33_07", 0x00000, 0x80000, 0x151e1f52 )	/* STY spritemap */
	
		ROM_REGION( 0x100000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "b33_09", 0x00000, 0x80000, 0x1e6724b5 );	ROM_LOAD( "b33_10", 0x80000, 0x80000, 0xe9ce03ab );
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* Delta-T samples */
		ROM_LOAD( "b33_08", 0x00000, 0x80000, 0xcaa1c4c8 );
		ROM_REGION( 0x10000, REGION_USER2, 0 );	ROM_LOAD( "b14-30", 0x00000, 0x10000, 0xdccb0c7f );/* unused roms */
		ROM_LOAD( "b14-31", 0x00000, 0x02000, 0x5c6b013d );ROM_END(); }}; 
	
	static RomLoadPtr rom_contcrcu = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 );/* 256K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE( "ic25", 0x00000, 0x20000, 0xf5c92e42 )
		ROM_LOAD16_BYTE( "ic26", 0x00001, 0x20000, 0xe7c1d1fa )
	
		ROM_REGION( 0x40000, REGION_CPU3, 0 );/* 256K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "ic35", 0x00000, 0x20000, 0x16522f2d )
		ROM_LOAD16_BYTE( "ic36", 0x00001, 0x20000, 0xd6741e33 )
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* Z80 sound cpu */
		ROM_LOAD( "b33_30",   0x00000, 0x04000, 0xd8746234 );	ROM_CONTINUE(         0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "b33_02", 0x00000, 0x80000, 0xf6fb3ba2 );/* SCR 8x8 */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "b33_06", 0x000000, 0x080000, 0x2cb40599 )	/* OBJ 16x8 */
		ROM_LOAD32_BYTE( "b33_05", 0x000001, 0x080000, 0xbddf9eea )
		ROM_LOAD32_BYTE( "b33_04", 0x000002, 0x080000, 0x8df866a2 )
		ROM_LOAD32_BYTE( "b33_03", 0x000003, 0x080000, 0x4f6c36d9 )
	
		ROM_REGION( 0x80000, REGION_GFX3, 0 );/* don't dispose */
		ROM_LOAD( "b33_01", 0x00000, 0x80000, 0xf11f2be8 );/* ROD, road lines */
	
		ROM_REGION16_LE( 0x80000, REGION_USER1, 0 )
		ROM_LOAD16_WORD( "b33_07", 0x00000, 0x80000, 0x151e1f52 )	/* STY spritemap */
	
		ROM_REGION( 0x100000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "b33_09", 0x00000, 0x80000, 0x1e6724b5 );	ROM_LOAD( "b33_10", 0x80000, 0x80000, 0xe9ce03ab );
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* Delta-T samples */
		ROM_LOAD( "b33_08", 0x00000, 0x80000, 0xcaa1c4c8 );
		ROM_REGION( 0x10000, REGION_USER2, 0 );	ROM_LOAD( "b14-30", 0x00000, 0x10000, 0xdccb0c7f );/* unused roms */
		ROM_LOAD( "b14-31", 0x00000, 0x02000, 0x5c6b013d );ROM_END(); }}; 
	
	static RomLoadPtr rom_chasehq = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );/* 512K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE( "b52-130.rom", 0x00000, 0x20000, 0x4e7beb46 )
		ROM_LOAD16_BYTE( "b52-136.rom", 0x00001, 0x20000, 0x2f414df0 )
		ROM_LOAD16_BYTE( "b52-131.rom", 0x40000, 0x20000, 0xaa945d83 )
		ROM_LOAD16_BYTE( "b52-129.rom", 0x40001, 0x20000, 0x0eaebc08 )
	
		ROM_REGION( 0x20000, REGION_CPU3, 0 );/* 128K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "b52-132.rom", 0x00000, 0x10000, 0xa2f54789 )
		ROM_LOAD16_BYTE( "b52-133.rom", 0x00001, 0x10000, 0x12232f95 )
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* Z80 sound cpu */
		ROM_LOAD( "b52-137.rom",   0x00000, 0x04000, 0x37abb74a );	ROM_CONTINUE(              0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "b52-m29.rom", 0x00000, 0x80000, 0x8366d27c );/* SCR 8x8 */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "b52-m34.rom", 0x000000, 0x080000, 0x7d8dce36 )
		ROM_LOAD32_BYTE( "b52-m35.rom", 0x000001, 0x080000, 0x78eeec0d )	/* OBJ A 16x16 */
		ROM_LOAD32_BYTE( "b52-m36.rom", 0x000002, 0x080000, 0x61e89e91 )
		ROM_LOAD32_BYTE( "b52-m37.rom", 0x000003, 0x080000, 0xf02e47b9 )
	
		ROM_REGION( 0x80000, REGION_GFX3, 0 );/* don't dispose */
		ROM_LOAD( "b52-m28.rom", 0x00000, 0x80000, 0x963bc82b );/* ROD, road lines */
	
		ROM_REGION( 0x200000, REGION_GFX4, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "b52-m30.rom", 0x000000, 0x080000, 0x1b8cc647 )
		ROM_LOAD32_BYTE( "b52-m31.rom", 0x000001, 0x080000, 0xf1998e20 )	/* OBJ B 16x16 */
		ROM_LOAD32_BYTE( "b52-m32.rom", 0x000002, 0x080000, 0x8620780c )
		ROM_LOAD32_BYTE( "b52-m33.rom", 0x000003, 0x080000, 0xe6f4b8c4 )
	
		ROM_REGION16_LE( 0x80000, REGION_USER1, 0 )
		ROM_LOAD16_WORD( "b52-m38.fix", 0x00000, 0x80000, 0x5b5bf7f6 )	/* STY spritemap */
	
		ROM_REGION( 0x180000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "b52-m115.rom", 0x000000, 0x080000, 0x4e117e93 );	ROM_LOAD( "b52-m114.rom", 0x080000, 0x080000, 0x3a73d6b1 );	ROM_LOAD( "b52-m113.rom", 0x100000, 0x080000, 0x2c6a3a05 );
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* Delta-T samples */
		ROM_LOAD( "b52-m116.rom", 0x00000, 0x80000, 0xad46983c );
		ROM_REGION( 0x10000, REGION_USER2, 0 );	ROM_LOAD( "b52-50.rom", 0x00000, 0x10000, 0xc189781c );/* unused roms */
		ROM_LOAD( "b52-51.rom", 0x00000, 0x10000, 0x30cc1f79 );
		// Many more are listed in Malcor's notes: b52-118 thru 127,
		// b52-1/3/6, b52-16 thru 21, b52-25 thru 27
	ROM_END(); }}; 
	
	static RomLoadPtr rom_chasehqj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );/* 512K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE( "b52-140",     0x00000, 0x20000, 0xc1298a4b )
		ROM_LOAD16_BYTE( "b52-139",     0x00001, 0x20000, 0x997f732e )
		ROM_LOAD16_BYTE( "b52-131.rom", 0x40000, 0x20000, 0xaa945d83 )
		ROM_LOAD16_BYTE( "b52-129.rom", 0x40001, 0x20000, 0x0eaebc08 )
	
		ROM_REGION( 0x20000, REGION_CPU3, 0 );/* 128K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "b52-132.rom", 0x00000, 0x10000, 0xa2f54789 )
		ROM_LOAD16_BYTE( "b52-133.rom", 0x00001, 0x10000, 0x12232f95 )
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* Z80 sound cpu */
		ROM_LOAD( "b52-134",    0x00000, 0x04000, 0x91faac7f );	ROM_CONTINUE(           0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "b52-m29.rom", 0x00000, 0x80000, 0x8366d27c );/* SCR 8x8*/
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "b52-m34.rom", 0x000000, 0x080000, 0x7d8dce36 )
		ROM_LOAD32_BYTE( "b52-m35.rom", 0x000001, 0x080000, 0x78eeec0d )	/* OBJ A 16x16 */
		ROM_LOAD32_BYTE( "b52-m36.rom", 0x000002, 0x080000, 0x61e89e91 )
		ROM_LOAD32_BYTE( "b52-m37.rom", 0x000003, 0x080000, 0xf02e47b9 )
	
		ROM_REGION( 0x80000, REGION_GFX3, 0 );/* don't dispose */
		ROM_LOAD( "b52-m28.rom", 0x00000, 0x80000, 0x963bc82b );/* ROD, road lines */
	
		ROM_REGION( 0x200000, REGION_GFX4, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "b52-m30.rom", 0x000000, 0x080000, 0x1b8cc647 )
		ROM_LOAD32_BYTE( "b52-m31.rom", 0x000001, 0x080000, 0xf1998e20 )	/* OBJ B 16x16 */
		ROM_LOAD32_BYTE( "b52-m32.rom", 0x000002, 0x080000, 0x8620780c )
		ROM_LOAD32_BYTE( "b52-m33.rom", 0x000003, 0x080000, 0xe6f4b8c4 )
	
		ROM_REGION16_LE( 0x80000, REGION_USER1, 0 )
		ROM_LOAD16_WORD( "b52-m38.fix", 0x00000, 0x80000, 0x5b5bf7f6 )	/* STY spritemap */
	
		ROM_REGION( 0x180000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "b52-41", 0x000000, 0x80000, 0x8204880c );	ROM_LOAD( "b52-40", 0x080000, 0x80000, 0xf0551055 );	ROM_LOAD( "b52-39", 0x100000, 0x80000, 0xac9cbbd3 );
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* Delta-T samples */
		ROM_LOAD( "b52-42", 0x00000, 0x80000, 0x6e617df1 );
		ROM_REGION( 0x10000, REGION_USER2, 0 );	ROM_LOAD( "b52-50.rom", 0x00000, 0x10000, 0xc189781c );/* unused roms */
		ROM_LOAD( "b52-51.rom", 0x00000, 0x10000, 0x30cc1f79 );ROM_END(); }}; 
	
	static RomLoadPtr rom_bshark = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );/* 512K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE( "c34_71.98",    0x00000, 0x20000, 0xdf1fa629 )
		ROM_LOAD16_BYTE( "c34_69.75",    0x00001, 0x20000, 0xa54c137a )
		ROM_LOAD16_BYTE( "c34_70.97",    0x40000, 0x20000, 0xd77d81e2 )
		ROM_LOAD16_BYTE( "bshark67.bin", 0x40001, 0x20000, 0x39307c74 )
	
		ROM_REGION( 0x80000, REGION_CPU2, 0 );/* 512K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "c34_74.128", 0x00000, 0x20000, 0x6869fa99 )
		ROM_LOAD16_BYTE( "c34_72.112", 0x00001, 0x20000, 0xc09c0f91 )
		ROM_LOAD16_BYTE( "c34_75.129", 0x40000, 0x20000, 0x6ba65542 )
		ROM_LOAD16_BYTE( "c34_73.113", 0x40001, 0x20000, 0xf2fe62b5 )
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "c34_05.3", 0x00000, 0x80000, 0x596b83da );/* SCR 8x8 */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "c34_04.17", 0x000000, 0x080000, 0x2446b0da )	/* OBJ 16x8 */
		ROM_LOAD32_BYTE( "c34_03.16", 0x000001, 0x080000, 0xa18eab78 )
		ROM_LOAD32_BYTE( "c34_02.15", 0x000002, 0x080000, 0x8488ba10 )
		ROM_LOAD32_BYTE( "c34_01.14", 0x000003, 0x080000, 0x3ebe8c63 )
	
		ROM_REGION( 0x80000, REGION_GFX3, 0 );/* don't dispose */
		ROM_LOAD( "c34_07.42", 0x00000, 0x80000, 0xedb07808 );/* ROD, road lines */
	
		ROM_REGION16_LE( 0x80000, REGION_USER1, 0 )
		ROM_LOAD16_WORD( "c34_06.12", 0x00000, 0x80000, 0xd200b6eb )	/* STY spritemap */
	
		ROM_REGION( 0x80000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "c34_08.127", 0x00000, 0x80000, 0x89a30450 );
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* Delta-T samples */
		ROM_LOAD( "c34_09.126", 0x00000, 0x80000, 0x39d12b50 );
		ROM_REGION( 0x10000, REGION_USER2, 0 );	ROM_LOAD( "c34_18.22", 0x00000, 0x10000, 0x7245a6f6 );/* unused roms */
		ROM_LOAD( "c34_19.72", 0x00000, 0x00100, 0x2ee9c404 );	ROM_LOAD( "c34_20.89", 0x00000, 0x00100, 0xfbf81f30 );	ROM_LOAD( "c34_21.7",  0x00000, 0x00400, 0x10728853 );	ROM_LOAD( "c34_22.8",  0x00000, 0x00400, 0x643e8bfc );ROM_END(); }}; 
	
	static RomLoadPtr rom_bsharkj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );/* 512K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE( "c34_71.98", 0x00000, 0x20000, 0xdf1fa629 )
		ROM_LOAD16_BYTE( "c34_69.75", 0x00001, 0x20000, 0xa54c137a )
		ROM_LOAD16_BYTE( "c34_70.97", 0x40000, 0x20000, 0xd77d81e2 )
		ROM_LOAD16_BYTE( "c34_66.74", 0x40001, 0x20000, 0xa0392dce )
	
		ROM_REGION( 0x80000, REGION_CPU2, 0 );/* 512K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "c34_74.128", 0x00000, 0x20000, 0x6869fa99 )
		ROM_LOAD16_BYTE( "c34_72.112", 0x00001, 0x20000, 0xc09c0f91 )
		ROM_LOAD16_BYTE( "c34_75.129", 0x40000, 0x20000, 0x6ba65542 )
		ROM_LOAD16_BYTE( "c34_73.113", 0x40001, 0x20000, 0xf2fe62b5 )
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "c34_05.3", 0x00000, 0x80000, 0x596b83da );/* SCR 8x8 */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "c34_04.17", 0x000000, 0x080000, 0x2446b0da )	/* OBJ 16x8 */
		ROM_LOAD32_BYTE( "c34_03.16", 0x000001, 0x080000, 0xa18eab78 )
		ROM_LOAD32_BYTE( "c34_02.15", 0x000002, 0x080000, 0x8488ba10 )
		ROM_LOAD32_BYTE( "c34_01.14", 0x000003, 0x080000, 0x3ebe8c63 )
	
		ROM_REGION( 0x80000, REGION_GFX3, 0 );/* don't dispose */
		ROM_LOAD( "c34_07.42", 0x00000, 0x80000, 0xedb07808 );/* ROD, road lines */
	
		ROM_REGION16_LE( 0x80000, REGION_USER1, 0 )
		ROM_LOAD16_WORD( "c34_06.12", 0x00000, 0x80000, 0xd200b6eb )	/* STY spritemap */
	
		ROM_REGION( 0x80000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "c34_08.127", 0x00000, 0x80000, 0x89a30450 );
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* Delta-T samples */
		ROM_LOAD( "c34_09.126", 0x00000, 0x80000, 0x39d12b50 );
		ROM_REGION( 0x10000, REGION_USER2, 0 );	ROM_LOAD( "c34_18.22", 0x00000, 0x10000, 0x7245a6f6 );/* unused roms */
		ROM_LOAD( "c34_19.72", 0x00000, 0x00100, 0x2ee9c404 );	ROM_LOAD( "c34_20.89", 0x00000, 0x00100, 0xfbf81f30 );	ROM_LOAD( "c34_21.7",  0x00000, 0x00400, 0x10728853 );	ROM_LOAD( "c34_22.8",  0x00000, 0x00400, 0x643e8bfc );ROM_END(); }}; 
	
	static RomLoadPtr rom_sci = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );/* 512K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE( "c09-37.rom", 0x00000, 0x20000, 0x0fecea17 )
		ROM_LOAD16_BYTE( "c09-40.rom", 0x00001, 0x20000, 0xe46ebd9b )
		ROM_LOAD16_BYTE( "c09-38.rom", 0x40000, 0x20000, 0xf4404f87 )
		ROM_LOAD16_BYTE( "c09-41.rom", 0x40001, 0x20000, 0xde87bcb9 )
	
		ROM_REGION( 0x20000, REGION_CPU3, 0 );/* 128K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "c09-33.rom", 0x00000, 0x10000, 0xcf4e6c5b )
		ROM_LOAD16_BYTE( "c09-32.rom", 0x00001, 0x10000, 0xa4713719 )
	
		ROM_REGION( 0x2c000, REGION_CPU2, 0 );/* Z80 sound cpu */
		ROM_LOAD( "c09-34.rom",   0x00000, 0x04000, 0xa21b3151 );	ROM_CONTINUE(             0x10000, 0x1c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "c09-05.rom", 0x00000, 0x80000, 0x890b38f0 );/* SCR 8x8 */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "c09-04.rom", 0x000000, 0x080000, 0x2cbb3c9b )	/* OBJ 16x8 */
		ROM_LOAD32_BYTE( "c09-02.rom", 0x000001, 0x080000, 0xa83a0389 )
		ROM_LOAD32_BYTE( "c09-03.rom", 0x000002, 0x080000, 0xa31d0e80 )
		ROM_LOAD32_BYTE( "c09-01.rom", 0x000003, 0x080000, 0x64bfea10 )
	
		ROM_REGION( 0x80000, REGION_GFX3, 0 );/* don't dispose */
		ROM_LOAD( "c09-07.rom", 0x00000, 0x80000, 0x963bc82b );/* ROD, road lines */
	
		ROM_REGION16_LE( 0x80000, REGION_USER1, 0 )
		ROM_LOAD16_WORD( "c09-06.rom", 0x00000, 0x80000, 0x12df6d7b )	/* STY spritemap */
	
		ROM_REGION( 0x180000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "c09-14.rom", 0x000000, 0x080000, 0xad78bf46 );	ROM_LOAD( "c09-13.rom", 0x080000, 0x080000, 0xd57c41d3 );	ROM_LOAD( "c09-12.rom", 0x100000, 0x080000, 0x56c99fa5 );
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* Delta-T samples */
		ROM_LOAD( "c09-15.rom", 0x00000, 0x80000, 0xe63b9095 );
		ROM_REGION( 0x10000, REGION_USER2, 0 );	ROM_LOAD( "c09-16.rom", 0x00000, 0x10000, 0x7245a6f6 );/* unused roms */
		ROM_LOAD( "c09-23.rom", 0x00000, 0x00100, 0xfbf81f30 );//	ROM_LOAD( "c09-21.rom", 0x00000, 0x00???, 0x00000000 );/* pals (Guru dump) */
	//	ROM_LOAD( "c09-22.rom", 0x00000, 0x00???, 0x00000000 );//	ROM_LOAD( "c09-24.rom", 0x00000, 0x00???, 0x00000000 );//	ROM_LOAD( "c09-25.rom", 0x00000, 0x00???, 0x00000000 );//	ROM_LOAD( "c09-26.rom", 0x00000, 0x00???, 0x00000000 );ROM_END(); }}; 
	
	static RomLoadPtr rom_scia = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );/* 512K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE( "c09-28.bin",  0x00000, 0x20000, 0x630dbaad )
		ROM_LOAD16_BYTE( "c09-30.bin",  0x00001, 0x20000, 0x68b1a97d )
		ROM_LOAD16_BYTE( "c09-36.bin",  0x40000, 0x20000, 0x59e47cba )
		ROM_LOAD16_BYTE( "c09-31.bin",  0x40001, 0x20000, 0x962b1fbf )
	
		ROM_REGION( 0x20000, REGION_CPU3, 0 );/* 128K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "c09-33.rom", 0x00000, 0x10000, 0xcf4e6c5b )
		ROM_LOAD16_BYTE( "c09-32.rom", 0x00001, 0x10000, 0xa4713719 )
	
		ROM_REGION( 0x2c000, REGION_CPU2, 0 );/* Z80 sound cpu */
		ROM_LOAD( "c09-34.rom",   0x00000, 0x04000, 0xa21b3151 );	ROM_CONTINUE(             0x10000, 0x1c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "c09-05.rom", 0x00000, 0x80000, 0x890b38f0 );/* SCR 8x8 */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "c09-04.rom", 0x000000, 0x080000, 0x2cbb3c9b )	/* OBJ 16x8 */
		ROM_LOAD32_BYTE( "c09-02.rom", 0x000001, 0x080000, 0xa83a0389 )
		ROM_LOAD32_BYTE( "c09-03.rom", 0x000002, 0x080000, 0xa31d0e80 )
		ROM_LOAD32_BYTE( "c09-01.rom", 0x000003, 0x080000, 0x64bfea10 )
	
		ROM_REGION( 0x80000, REGION_GFX3, 0 );/* don't dispose */
		ROM_LOAD( "c09-07.rom", 0x00000, 0x80000, 0x963bc82b );/* ROD, road lines */
	
		ROM_REGION16_LE( 0x80000, REGION_USER1, 0 )
		ROM_LOAD16_WORD( "c09-06.rom", 0x00000, 0x80000, 0x12df6d7b )	/* STY spritemap */
	
		ROM_REGION( 0x180000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "c09-14.rom", 0x000000, 0x080000, 0xad78bf46 );	ROM_LOAD( "c09-13.rom", 0x080000, 0x080000, 0xd57c41d3 );	ROM_LOAD( "c09-12.rom", 0x100000, 0x080000, 0x56c99fa5 );
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* Delta-T samples */
		ROM_LOAD( "c09-15.rom", 0x00000, 0x80000, 0xe63b9095 );
		ROM_REGION( 0x10000, REGION_USER2, 0 );	ROM_LOAD( "c09-16.rom", 0x00000, 0x10000, 0x7245a6f6 );/* unused roms */
		ROM_LOAD( "c09-23.rom", 0x00000, 0x00100, 0xfbf81f30 );ROM_END(); }}; 
	
	static RomLoadPtr rom_sciu = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );/* 512K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE( "c09-43.37",  0x00000, 0x20000, 0x20a9343e )
		ROM_LOAD16_BYTE( "c09-44.40",  0x00001, 0x20000, 0x7524338a )
		ROM_LOAD16_BYTE( "c09-41.38",  0x40000, 0x20000, 0x83477f11 )
		ROM_LOAD16_BYTE( "c09-41.rom", 0x40001, 0x20000, 0xde87bcb9 )
	
		ROM_REGION( 0x20000, REGION_CPU3, 0 );/* 128K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "c09-33.rom", 0x00000, 0x10000, 0xcf4e6c5b )
		ROM_LOAD16_BYTE( "c09-32.rom", 0x00001, 0x10000, 0xa4713719 )
	
		ROM_REGION( 0x2c000, REGION_CPU2, 0 );/* Z80 sound cpu */
		ROM_LOAD( "c09-34.rom",   0x00000, 0x04000, 0xa21b3151 );	ROM_CONTINUE(             0x10000, 0x1c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "c09-05.rom", 0x00000, 0x80000, 0x890b38f0 );/* SCR 8x8 */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "c09-04.rom", 0x000000, 0x080000, 0x2cbb3c9b )	/* OBJ 16x8 */
		ROM_LOAD32_BYTE( "c09-02.rom", 0x000001, 0x080000, 0xa83a0389 )
		ROM_LOAD32_BYTE( "c09-03.rom", 0x000002, 0x080000, 0xa31d0e80 )
		ROM_LOAD32_BYTE( "c09-01.rom", 0x000003, 0x080000, 0x64bfea10 )
	
		ROM_REGION( 0x80000, REGION_GFX3, 0 );/* don't dispose */
		ROM_LOAD( "c09-07.rom", 0x00000, 0x80000, 0x963bc82b );/* ROD, road lines */
	
		ROM_REGION16_LE( 0x80000, REGION_USER1, 0 )
		ROM_LOAD16_WORD( "c09-06.rom", 0x00000, 0x80000, 0x12df6d7b )	/* STY spritemap */
	
		ROM_REGION( 0x180000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "c09-14.rom", 0x000000, 0x080000, 0xad78bf46 );	ROM_LOAD( "c09-13.rom", 0x080000, 0x080000, 0xd57c41d3 );	ROM_LOAD( "c09-12.rom", 0x100000, 0x080000, 0x56c99fa5 );
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* Delta-T samples */
		ROM_LOAD( "c09-15.rom", 0x00000, 0x80000, 0xe63b9095 );
		ROM_REGION( 0x10000, REGION_USER2, 0 );	ROM_LOAD( "c09-16.rom", 0x00000, 0x10000, 0x7245a6f6 );/* unused roms */
		ROM_LOAD( "c09-23.rom", 0x00000, 0x00100, 0xfbf81f30 );ROM_END(); }}; 
	
	static RomLoadPtr rom_nightstr = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );/* 512K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE( "b91-45.bin", 0x00000, 0x20000, 0x7ad63421 )
		ROM_LOAD16_BYTE( "b91-44.bin", 0x00001, 0x20000, 0x4bc30adf )
		ROM_LOAD16_BYTE( "b91-43.bin", 0x40000, 0x20000, 0x3e6f727a )
		ROM_LOAD16_BYTE( "b91-46.bin", 0x40001, 0x20000, 0xe870be95 )
	
		ROM_REGION( 0x40000, REGION_CPU3, 0 );/* 256K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "b91-39.bin", 0x00000, 0x20000, 0x725b23ae )
		ROM_LOAD16_BYTE( "b91-40.bin", 0x00001, 0x20000, 0x81fb364d )
	
		ROM_REGION( 0x2c000, REGION_CPU2, 0 );/* Z80 sound cpu */
		ROM_LOAD( "b91-41.bin",   0x00000, 0x04000, 0x2694bb42 );	ROM_CONTINUE(             0x10000, 0x1c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "b91-11.bin", 0x00000, 0x80000, 0xfff8ce31 );/* SCR 8x8 */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "b91-04.bin", 0x000000, 0x080000, 0x8ca1970d )	/* OBJ A 16x16 */
		ROM_LOAD32_BYTE( "b91-03.bin", 0x000001, 0x080000, 0xcd5fed39 )
		ROM_LOAD32_BYTE( "b91-02.bin", 0x000002, 0x080000, 0x457c64b8 )
		ROM_LOAD32_BYTE( "b91-01.bin", 0x000003, 0x080000, 0x3731d94f )
	
		ROM_REGION( 0x80000, REGION_GFX3, 0 );/* don't dispose */
		ROM_LOAD( "b91-10.bin", 0x00000, 0x80000, 0x1d8f05b4 );/* ROD, road lines */
	
		ROM_REGION( 0x200000, REGION_GFX4, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "b91-08.bin", 0x000000, 0x080000, 0x66f35c34 )	/* OBJ B 16x16 */
		ROM_LOAD32_BYTE( "b91-07.bin", 0x000001, 0x080000, 0x4d8ec6cf )
		ROM_LOAD32_BYTE( "b91-06.bin", 0x000002, 0x080000, 0xa34dc839 )
		ROM_LOAD32_BYTE( "b91-05.bin", 0x000003, 0x080000, 0x5e72ac90 )
	
		ROM_REGION16_LE( 0x80000, REGION_USER1, 0 )
		ROM_LOAD16_WORD( "b91-09.bin", 0x00000, 0x80000, 0x5f247ca2 )	/* STY spritemap */
	
		ROM_REGION( 0x100000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "b91-13.bin", 0x00000, 0x80000, 0x8c7bf0f5 );	ROM_LOAD( "b91-12.bin", 0x80000, 0x80000, 0xda77c7af );
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* Delta-T samples */
		ROM_LOAD( "b91-14.bin", 0x00000, 0x80000, 0x6bc314d3 );
		ROM_REGION( 0x10000, REGION_USER2, 0 );	ROM_LOAD( "b91-26.bin", 0x00000, 0x400,   0x77682a4f );/* unused roms */
		ROM_LOAD( "b91-27.bin", 0x00000, 0x400,   0xa3f8490d );	ROM_LOAD( "b91-28.bin", 0x00000, 0x400,   0xfa2f840e );	ROM_LOAD( "b91-29.bin", 0x00000, 0x2000,  0xad685be8 );	ROM_LOAD( "b91-30.bin", 0x00000, 0x10000, 0x30cc1f79 );	ROM_LOAD( "b91-31.bin", 0x00000, 0x10000, 0xc189781c );	ROM_LOAD( "b91-32.bin", 0x00000, 0x100,   0xfbf81f30 );	ROM_LOAD( "b91-33.bin", 0x00000, 0x100,   0x89719d17 );ROM_END(); }}; 
	
	static RomLoadPtr rom_aquajack = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 );/* 256K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE( "b77-22.rom", 0x00000, 0x20000, 0x67400dde )
		ROM_LOAD16_BYTE( "34.17",      0x00001, 0x20000, 0xcd4d0969 )
	
		ROM_REGION( 0x40000, REGION_CPU3, 0 );/* 256K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "b77-24.rom", 0x00000, 0x20000, 0x95e643ed )
		ROM_LOAD16_BYTE( "b77-23.rom", 0x00001, 0x20000, 0x395a7d1c )
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* sound cpu */
		ROM_LOAD( "b77-20.rom",   0x00000, 0x04000, 0x84ba54b7 );	ROM_CONTINUE(             0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "b77-05.rom", 0x00000, 0x80000, 0x7238f0ff );/* SCR 8x8 */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "b77-04.rom", 0x000000, 0x80000, 0xbed0be6c )	/* OBJ 16x8 */
		ROM_LOAD32_BYTE( "b77-03.rom", 0x000001, 0x80000, 0x9a3030a7 )
		ROM_LOAD32_BYTE( "b77-02.rom", 0x000002, 0x80000, 0xdaea0d2e )
		ROM_LOAD32_BYTE( "b77-01.rom", 0x000003, 0x80000, 0xcdab000d )
	
		ROM_REGION( 0x80000, REGION_GFX3, 0 );/* don't dispose */
		ROM_LOAD( "b77-07.rom", 0x000000, 0x80000, 0x7db1fc5e );/* ROD, road lines */
	
		ROM_REGION16_LE( 0x80000, REGION_USER1, 0 )
		ROM_LOAD16_WORD( "b77-06.rom", 0x00000, 0x80000, 0xce2aed00 )	/* STY spritemap */
	
		ROM_REGION( 0x80000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "b77-09.rom", 0x00000, 0x80000, 0x948e5ad9 );
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* Delta-T samples */
		ROM_LOAD( "b77-08.rom", 0x00000, 0x80000, 0x119b9485 );
	/*	(no unused roms in my set, there should be an 0x10000 one like the rest) */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_aquajckj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 );/* 256K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE( "b77-22.rom", 0x00000, 0x20000, 0x67400dde )
		ROM_LOAD16_BYTE( "b77-21.rom", 0x00001, 0x20000, 0x23436845 )
	
		ROM_REGION( 0x40000, REGION_CPU3, 0 );/* 256K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "b77-24.rom", 0x00000, 0x20000, 0x95e643ed )
		ROM_LOAD16_BYTE( "b77-23.rom", 0x00001, 0x20000, 0x395a7d1c )
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* sound cpu */
		ROM_LOAD( "b77-20.rom",   0x00000, 0x04000, 0x84ba54b7 );	ROM_CONTINUE(             0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "b77-05.rom", 0x00000, 0x80000, 0x7238f0ff );/* SCR 8x8 */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "b77-04.rom", 0x000000, 0x80000, 0xbed0be6c )	/* OBJ 16x8 */
		ROM_LOAD32_BYTE( "b77-03.rom", 0x000001, 0x80000, 0x9a3030a7 )
		ROM_LOAD32_BYTE( "b77-02.rom", 0x000002, 0x80000, 0xdaea0d2e )
		ROM_LOAD32_BYTE( "b77-01.rom", 0x000003, 0x80000, 0xcdab000d )
	
		ROM_REGION( 0x80000, REGION_GFX3, 0 );/* don't dispose */
		ROM_LOAD( "b77-07.rom", 0x000000, 0x80000, 0x7db1fc5e );/* ROD, road lines */
	
		ROM_REGION16_LE( 0x80000, REGION_USER1, 0 )
		ROM_LOAD16_WORD( "b77-06.rom", 0x00000, 0x80000, 0xce2aed00 )	/* STY spritemap */
	
		ROM_REGION( 0x80000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "b77-09.rom", 0x00000, 0x80000, 0x948e5ad9 );
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* Delta-T samples */
		ROM_LOAD( "b77-08.rom", 0x00000, 0x80000, 0x119b9485 );
	/*	(no unused roms in my set, there should be an 0x10000 one like the rest) */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_spacegun = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );/* 512K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE( "c57-18", 0x00000, 0x20000, 0x19d7d52e )
		ROM_LOAD16_BYTE( "c57-20", 0x00001, 0x20000, 0x2e58253f )
		ROM_LOAD16_BYTE( "c57-17", 0x40000, 0x20000, 0xe197edb8 )
		ROM_LOAD16_BYTE( "c57-22", 0x40001, 0x20000, 0x5855fde3 )
	
		ROM_REGION( 0x40000, REGION_CPU2, 0 );/* 256K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "c57-15", 0x00000, 0x20000, 0xb36eb8f1 )
		ROM_LOAD16_BYTE( "c57-16", 0x00001, 0x20000, 0xbfb5d1e7 )
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "c57-06", 0x00000, 0x80000, 0x4ebadd5b );	/* SCR 8x8 */
	
		ROM_REGION( 0x400000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "c57-01", 0x000000, 0x100000, 0xf901b04e )	/* OBJ 16x8 */
		ROM_LOAD32_BYTE( "c57-02", 0x000001, 0x100000, 0x21ee4633 )
		ROM_LOAD32_BYTE( "c57-03", 0x000002, 0x100000, 0xfafca86f )
		ROM_LOAD32_BYTE( "c57-04", 0x000003, 0x100000, 0xa9787090 )
	
		ROM_REGION16_LE( 0x80000, REGION_USER1, 0 )
		ROM_LOAD16_WORD( "c57-05", 0x00000, 0x80000, 0x6a70eb2e )	/* STY spritemap */
	
		ROM_REGION( 0x80000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "c57-07", 0x00000, 0x80000, 0xad653dc1 );
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* Delta-T samples */
		ROM_LOAD( "c57-08", 0x00000, 0x80000, 0x22593550 );
	/*	(there probably should be an unused 0x10000 rom like the rest) */
		ROM_REGION( 0x10000, REGION_USER2, 0 );//	ROM_LOAD( "c57-09", 0x00000, 0xada, 0x306f130b );/* pals ? */
	//	ROM_LOAD( "c57-10", 0x00000, 0xcd5, 0xf11474bd );//	ROM_LOAD( "c57-11", 0x00000, 0xada, 0xb33be19f );//	ROM_LOAD( "c57-12", 0x00000, 0xcd5, 0xf1847096 );//	ROM_LOAD( "c57-13", 0x00000, 0xada, 0x795f0a85 );//	ROM_LOAD( "c57-14", 0x00000, 0xada, 0x5b3c40b7 );ROM_END(); }}; 
	
	static RomLoadPtr rom_dblaxle = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );/* 512K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE( "c78-41.3",  0x00000, 0x20000, 0xcf297fe4 )
		ROM_LOAD16_BYTE( "c78-43.5",  0x00001, 0x20000, 0x38a8bad6 )
		ROM_LOAD16_BYTE( "c78-42.4",  0x40000, 0x20000, 0x4124ab2b )
		ROM_LOAD16_BYTE( "c78-44.6",  0x40001, 0x20000, 0x50a55b6e )
	
		ROM_REGION( 0x40000, REGION_CPU3, 0 );/* 256K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "c78-30-1.35", 0x00000, 0x20000, 0x026aac18 )
		ROM_LOAD16_BYTE( "c78-31-1.36", 0x00001, 0x20000, 0x67ce23e8 )
	
		ROM_REGION( 0x2c000, REGION_CPU2, 0 );/* sound cpu */
		ROM_LOAD    ( "ic42", 0x00000, 0x04000, 0xf2186943 );	ROM_CONTINUE(         0x10000, 0x1c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD16_BYTE( "c78-10.12", 0x00000, 0x80000, 0x44b1897c )	/* SCR 8x8 */
		ROM_LOAD16_BYTE( "c78-11.11", 0x00001, 0x80000, 0x7db3d4a3 )
	
		ROM_REGION( 0x400000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "c78-08.25", 0x000000, 0x100000, 0x6c725211 )	/* OBJ 16x8 */
		ROM_LOAD32_BYTE( "c78-07.33", 0x000001, 0x100000, 0x9da00d5b )
		ROM_LOAD32_BYTE( "c78-06.23", 0x000002, 0x100000, 0x8309e91b )
		ROMX_LOAD      ( "c78-05l.1", 0x000003, 0x080000, 0xf24bf972, ROM_SKIP(7) )
		ROMX_LOAD      ( "c78-05h.2", 0x000007, 0x080000, 0xc01039b5, ROM_SKIP(7) )
	
		ROM_REGION( 0x80000, REGION_GFX3, 0 );/* don't dispose */
		ROM_LOAD( "c78-09.12", 0x000000, 0x80000, 0x0dbde6f5 );/* ROD, road lines */
	
		ROM_REGION16_LE( 0x80000, REGION_USER1, 0 )
		ROM_LOAD16_WORD( "c78-04.3", 0x00000, 0x80000, 0xcc1aa37c )	/* STY spritemap */
	
		ROM_REGION( 0x180000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "c78-12.33", 0x000000, 0x80000, 0xfbb39585 );// Half size ??
		// ??? gap 0x80000-fffff //
		ROM_LOAD( "c78-13.46", 0x100000, 0x80000, 0x1b363aa2 );
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* Delta-T samples */
		ROM_LOAD( "c78-14.31",  0x00000, 0x80000, 0x9cad4dfb );
		ROM_REGION( 0x10000, REGION_USER2, 0 );/* unused roms */
		ROM_LOAD( "c78-25.15",  0x00000, 0x10000, 0x7245a6f6 );// 98% compression
		ROM_LOAD( "c78-15.22",  0x00000, 0x00100, 0xfbf81f30 );	ROM_LOAD( "c78-21.74",  0x00000, 0x00100, 0x2926bf27 );	ROM_LOAD( "c84-10.16",  0x00000, 0x00400, 0x643e8bfc );	ROM_LOAD( "c84-11.17",  0x00000, 0x00400, 0x10728853 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pwheelsj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );/* 512K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE( "c78-26-2.2",  0x00000, 0x20000, 0x25c8eb2e )
		ROM_LOAD16_BYTE( "c78-28-2.4",  0x00001, 0x20000, 0xa9500eb1 )
		ROM_LOAD16_BYTE( "c78-27-2.3",  0x40000, 0x20000, 0x08d2cffb )
		ROM_LOAD16_BYTE( "c78-29-2.5",  0x40001, 0x20000, 0xe1608004 )
	
		ROM_REGION( 0x40000, REGION_CPU3, 0 );/* 256K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "c78-30-1.35", 0x00000, 0x20000, 0x026aac18 )
		ROM_LOAD16_BYTE( "c78-31-1.36", 0x00001, 0x20000, 0x67ce23e8 )
	
		ROM_REGION( 0x2c000, REGION_CPU2, 0 );/* sound cpu */
		ROM_LOAD    ( "c78-32.42",    0x00000, 0x04000, 0x1494199c );	ROM_CONTINUE(                 0x10000, 0x1c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD16_BYTE( "c78-10.12", 0x00000, 0x80000, 0x44b1897c )	/* SCR 8x8 */
		ROM_LOAD16_BYTE( "c78-11.11", 0x00001, 0x80000, 0x7db3d4a3 )
	
		ROM_REGION( 0x400000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "c78-08.25", 0x000000, 0x100000, 0x6c725211 )	/* OBJ 16x8 */
		ROM_LOAD32_BYTE( "c78-07.33", 0x000001, 0x100000, 0x9da00d5b )
		ROM_LOAD32_BYTE( "c78-06.23", 0x000002, 0x100000, 0x8309e91b )
		ROM_LOAD32_BYTE( "c78-05.31", 0x000003, 0x100000, 0x90001f68 )
	
		ROM_REGION( 0x80000, REGION_GFX3, 0 );/* don't dispose */
		ROM_LOAD( "c78-09.12", 0x000000, 0x80000, 0x0dbde6f5 );/* ROD, road lines */
	
		ROM_REGION16_LE( 0x80000, REGION_USER1, 0 )
		ROM_LOAD16_WORD( "c78-04.3", 0x00000, 0x80000, 0xcc1aa37c )	/* STY spritemap */
	
		ROM_REGION( 0x180000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "c78-01.33", 0x000000, 0x100000, 0x90ff1e72 );	ROM_LOAD( "c78-02.46", 0x100000, 0x080000, 0x8882d2b7 );
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* Delta-T samples */
		ROM_LOAD( "c78-03.31",  0x00000, 0x80000, 0x9b926a2f );
		ROM_REGION( 0x10000, REGION_USER2, 0 );/* unused roms */
		ROM_LOAD( "c78-25.15",  0x00000, 0x10000, 0x7245a6f6 );// 98% compression
		ROM_LOAD( "c78-15.22",  0x00000, 0x00100, 0xfbf81f30 );	ROM_LOAD( "c78-21.74",  0x00000, 0x00100, 0x2926bf27 );	ROM_LOAD( "c84-10.16",  0x00000, 0x00400, 0x643e8bfc );	ROM_LOAD( "c84-11.17",  0x00000, 0x00400, 0x10728853 );ROM_END(); }}; 
	
	
	static public static InitDriverPtr init_taitoz = new InitDriverPtr() { public void handler() 
	{
	//	taitosnd_setz80_soundcpu( 2 );
	
		cpua_ctrl = 0xff;
		state_save_register_UINT16("main1", 0, "control", &cpua_ctrl, 1);
		state_save_register_func_postload(parse_control);
	
		/* these are specific to various games: we ought to split the inits */
		state_save_register_int   ("main2", 0, "control", &sci_int6);
		state_save_register_int   ("main3", 0, "control", &dblaxle_int6);
		state_save_register_int   ("main4", 0, "register", &ioc220_port);
	
		state_save_register_int   ("sound1", 0, "sound region", &banknum);
		state_save_register_func_postload(reset_sound_region);
	} };
	
	static public static InitDriverPtr init_bshark = new InitDriverPtr() { public void handler() 
	{
		cpua_ctrl = 0xff;
		state_save_register_UINT16("main1", 0, "control", &cpua_ctrl, 1);
		state_save_register_func_postload(parse_control_noz80);
	
		state_save_register_UINT16("main2", 0, "control", &eep_latch, 1);
	} };
	
	
	/* Working Games */
	
	// Spacegun will come after Aquajack in release date order //
	public static GameDriver driver_spacegun	   = new GameDriver("1990"	,"spacegun"	,"taito_z.java"	,rom_spacegun,null	,machine_driver_spacegun	,input_ports_spacegun	,init_bshark	,ORIENTATION_FLIP_X	,	"Taito Corporation Japan", "Space Gun (World)" )
	
	/* Busted Games, release date order: contcirc 1989 (c) date is bogus */
	
	public static GameDriver driver_contcirc	   = new GameDriver("1989"	,"contcirc"	,"taito_z.java"	,rom_contcirc,null	,machine_driver_contcirc	,input_ports_contcirc	,init_taitoz	,ROT0	,	"Taito Corporation Japan", "Continental Circus (World)", GAME_NOT_WORKING )
	public static GameDriver driver_contcrcu	   = new GameDriver("1987"	,"contcrcu"	,"taito_z.java"	,rom_contcrcu,driver_contcirc	,machine_driver_contcirc	,input_ports_contcirc	,init_taitoz	,ROT0	,	"Taito America Corporation", "Continental Circus (US)", GAME_NOT_WORKING )
	public static GameDriver driver_chasehq	   = new GameDriver("1988"	,"chasehq"	,"taito_z.java"	,rom_chasehq,null	,machine_driver_chasehq	,input_ports_chasehq	,init_taitoz	,ROT0	,	"Taito Corporation Japan", "Chase HQ (World)", GAME_NOT_WORKING )
	public static GameDriver driver_chasehqj	   = new GameDriver("1988"	,"chasehqj"	,"taito_z.java"	,rom_chasehqj,driver_chasehq	,machine_driver_chasehq	,input_ports_chasehq	,init_taitoz	,ROT0	,	"Taito Corporation", "Chase HQ (Japan)", GAME_NOT_WORKING )
	public static GameDriver driver_bshark	   = new GameDriver("1989"	,"bshark"	,"taito_z.java"	,rom_bshark,null	,machine_driver_bshark	,input_ports_bshark	,init_bshark	,ORIENTATION_FLIP_X	,	"Taito America Corporation", "Battle Shark (US)", GAME_NOT_WORKING )
	public static GameDriver driver_bsharkj	   = new GameDriver("1989"	,"bsharkj"	,"taito_z.java"	,rom_bsharkj,driver_bshark	,machine_driver_bshark	,input_ports_bshark	,init_bshark	,ORIENTATION_FLIP_X	,	"Taito Corporation", "Battle Shark (Japan)", GAME_NOT_WORKING )
	public static GameDriver driver_sci	   = new GameDriver("1989"	,"sci"	,"taito_z.java"	,rom_sci,null	,machine_driver_sci	,input_ports_sci	,init_taitoz	,ROT0	,	"Taito Corporation Japan", "Special Criminal Investigation (World set 1)", GAME_NOT_WORKING )
	public static GameDriver driver_scia	   = new GameDriver("1989"	,"scia"	,"taito_z.java"	,rom_scia,driver_sci	,machine_driver_sci	,input_ports_sci	,init_taitoz	,ROT0	,	"Taito Corporation Japan", "Special Criminal Investigation (World set 2)", GAME_NOT_WORKING )
	public static GameDriver driver_sciu	   = new GameDriver("1989"	,"sciu"	,"taito_z.java"	,rom_sciu,driver_sci	,machine_driver_sci	,input_ports_sci	,init_taitoz	,ROT0	,	"Taito America Corporation", "Special Criminal Investigation (US)", GAME_NOT_WORKING )
	public static GameDriver driver_nightstr	   = new GameDriver("1989"	,"nightstr"	,"taito_z.java"	,rom_nightstr,null	,machine_driver_nightstr	,input_ports_nightstr	,init_taitoz	,ROT0	,	"Taito America Corporation", "Night Striker (US)", GAME_NOT_WORKING )
	public static GameDriver driver_aquajack	   = new GameDriver("1990"	,"aquajack"	,"taito_z.java"	,rom_aquajack,null	,machine_driver_aquajack	,input_ports_aquajack	,init_taitoz	,ROT0	,	"Taito Corporation Japan", "Aqua Jack (World)", GAME_NOT_WORKING )
	public static GameDriver driver_aquajckj	   = new GameDriver("1990"	,"aquajckj"	,"taito_z.java"	,rom_aquajckj,driver_aquajack	,machine_driver_aquajack	,input_ports_aquajack	,init_taitoz	,ROT0	,	"Taito Corporation", "Aqua Jack (Japan)", GAME_NOT_WORKING )
	public static GameDriver driver_dblaxle	   = new GameDriver("1991"	,"dblaxle"	,"taito_z.java"	,rom_dblaxle,null	,machine_driver_dblaxle	,input_ports_dblaxle	,init_taitoz	,ROT0	,	"Taito America Corporation", "Double Axle (US)", GAME_NOT_WORKING )
	public static GameDriver driver_pwheelsj	   = new GameDriver("1991"	,"pwheelsj"	,"taito_z.java"	,rom_pwheelsj,driver_dblaxle	,machine_driver_dblaxle	,input_ports_dblaxle	,init_taitoz	,ROT0	,	"Taito Corporation", "Power Wheels (Japan)", GAME_NOT_WORKING )
}
