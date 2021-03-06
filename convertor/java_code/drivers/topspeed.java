/***************************************************************************

Top Speed / Full Throttle    (c) Taito 1987
-------------------------

David Graves

Sources:		Rastan driver by Jarek Burczynski
			MAME Taito F2 & Z drivers
			Raine source - special thanks to Richard Bush
			  and the Raine Team.

				*****

Top Speed / Full Throttle is the forerunner of the Taito Z system on
which Taito's driving games were based from 1988-91. (You can spot some
similarities with Continental Circus, the first of the TaitoZ games.)

The game hardware has 5 separate layers of graphics - four 64x64 tiled
scrolling background planes of 8x8 tiles (two of which are used for
drawing the road), and a sprite plane.

Taito got round the limitations of the tilemap generator they were using
(which only supports two layers) by using a pair of them.

[Trivia: Taito employed the same trick three years later, this time with
the TC0100SCN in "Thunderfox".]

Top Speed's sprites are 16x8 tiles aggregated through a RAM sprite map
area into 128x128 big sprites. (The TaitoZ system also used a similar
sprite map system, but moved the sprite map from RAM to ROM.)

Top Speed has twin 68K CPUs which communicate via $10000 bytes of
shared ram. The first 68000 handles screen, palette and sprites, and
the road. The second 68000 handles inputs/dips, and does data processing
in shared ram to relieve CPUA. There is also a Z80, which takes over
sound duties.


Dumper's info (topspedu)
-------------

Main CPUs: Dual 68000
Sound: YM2151, OKI M5205

Some of the custom Taito chips look like Rastan Hardware

Comments: Note b14-06, and b14-07, are duplicated twice on this board
for some type of hardware graphics reasons. (DG: that's because of
the twin tilemap generator chips. Can someone confirm they are
PC080SN's please...?)

There is a weird chip that is probably a Microcontroller made by Sharp.
Part number: b14-31 - Sharp LH763J-70


TODO Lists
==========

(Want to verify 68000 clocks)

Accel and brake bits work differently depending on cab DSW
Mame cannot yet support this, so accel/brake are not hooked up
sensibly when upright cabinet is selected.

The 8 level brake and accel inputs for the cockpit version
should be mapped to a pedal for analogue pedal control.
(Warlock did this but his changes need remerging.)

Minor black glitches on the road: these are all on the right
hand edge of the tilemap making up the "left" half: this is
the upper of the two road tilemaps so any gunk will be visible.
Maybe a road color issue or a timing glitch?

Extra effects to make road "move"? The unknown 0xffff memory
area could be responsible. First 0x800 of this looks as though
it contains per-pixel-row information for the two road tilemaps.
It consists of words from 0xffe0 to 0x001f (-31 to +31).
These change quite a bit.

Currently road tile colors are in the range 0x100-104 (?).
I can't see how these extra offsets would add to this to
produce a per-pixel-row color, since color bytes > 0x104 are
unused parts of the palette.

*Loads* of complaints from the Taito sound system in the log.

CPUA (on all variants) could have a spin_until_int at $63a.

Motor CPU: appears to be identical to one in ChaseHQ.

DIPs


***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class topspeed
{
	
	WRITE16_HANDLER( rainbow_spritectrl_w );
	WRITE16_HANDLER( rastan_spriteflip_w );
	
	
	
	static UINT16 cpua_ctrl = 0xff;
	static int ioc220_port = 0;
	
	extern data16_t *topspeed_spritemap;
	
	static size_t sharedram_size;
	static data16_t *sharedram;
	
	
	static READ16_HANDLER( sharedram_r )
	{
		return sharedram[offset];
	}
	
	static WRITE16_HANDLER( sharedram_w )
	{
		COMBINE_DATA(&sharedram[offset]);
	}
	
	static void parse_control(void)	/* assumes Z80 sandwiched between 68Ks */
	{
		/* bit 0 enables cpu B */
		/* however this fails when recovering from a save state
		   if cpu B is disabled !! */
		cpu_set_reset_line(2,(cpua_ctrl &0x1) ? CLEAR_LINE : ASSERT_LINE);
	
	}
	
	static WRITE16_HANDLER( cpua_ctrl_w )
	{
		if ((data &0xff00) && ((data &0xff) == 0))
			data = data >> 8;	/* for Wgp */
		cpua_ctrl = data;
	
		parse_control();
	
		logerror("CPU #0 PC %06x: write %04x to cpu control\n",cpu_get_pc(),data);
	}
	
	
	/***********************************************************
					INTERRUPTS
	***********************************************************/
	
	/* 68000 A */
	
	void topspeed_interrupt6(int x)
	{
		cpu_cause_interrupt(0,6);
	}
	
	/* 68000 B */
	
	void topspeed_cpub_interrupt6(int x)
	{
		cpu_cause_interrupt(2,6);	/* assumes Z80 sandwiched between the 68Ks */
	}
	
	
	public static InterruptPtr topspeed_interrupt = new InterruptPtr() { public int handler() 
	{
		/* Unsure how many int6's per frame */
		timer_set(TIME_IN_CYCLES(200000-500,0),0, topspeed_interrupt6);
		return 5;
	} };
	
	public static InterruptPtr topspeed_cpub_interrupt = new InterruptPtr() { public int handler() 
	{
		/* Unsure how many int6's per frame */
		timer_set(TIME_IN_CYCLES(200000-500,0),0, topspeed_cpub_interrupt6);
		return 5;
	} };
	
	
	
	/**********************************************************
					GAME INPUTS
	**********************************************************/
	
	static READ16_HANDLER( topspeed_input_bypass_r )
	{
		UINT8 port = TC0220IOC_port_r(0);	/* read port number */
		int steer = 0;
		int analogue_steer = input_port_5_word_r(0,0);
		int fake = input_port_6_word_r(0,0);
	
		if (!(fake &0x10))	/* Analogue steer (the real control method) */
		{
			steer = analogue_steer;
	
		}
		else	/* Digital steer */
		{
			if ((fake & 0x8) != 0)	/* pressing down */
				steer = 0xff40;
	
			if ((fake & 0x2) != 0)	/* pressing right */
				steer = 0x007f;
	
			if ((fake & 0x1) != 0)	/* pressing left */
				steer = 0xff80;
	
			/* To allow hiscore input we must let you return to
			   continuous input type while you press up */
	
			if ((fake & 0x4) != 0)	/* pressing up */
				steer = analogue_steer;
		}
	
		switch (port)
		{
			case 0x0c:
				return steer &0xff;
	
			case 0x0d:
				return steer >> 8;
	
			default:
				return TC0220IOC_portreg_r(offset);
		}
	}
	
	
	static READ16_HANDLER( topspeed_motor_r )
	{
		switch (offset)
		{
			case 0x0:
				return (rand() &0xff);	/* motor status ?? */
	
			case 0x101:
				return 0x55;	/* motor cpu status ? */
	
			default:
	logerror("CPU #0 PC %06x: warning - read from motor cpu %03x\n",cpu_get_pc(),offset);
				return 0;
		}
	}
	
	static WRITE16_HANDLER( topspeed_motor_w )
	{
		/* Writes $900000-25 and $900200-219 */
	
	logerror("CPU #0 PC %06x: warning - write %04x to motor cpu %03x\n",cpu_get_pc(),data,offset);
	
	}
	
	
	/*****************************************************
					SOUND
	*****************************************************/
	
	static int banknum = -1;
	
	static void reset_sound_region(void)
	{
		cpu_setbank( 10, memory_region(REGION_CPU2) + (banknum * 0x4000) + 0x10000 );
	}
	
	public static WriteHandlerPtr sound_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)	/* assumes Z80 sandwiched between 68Ks */
	{
		banknum = (data - 1) & 7;
		reset_sound_region();
	} };
	
	
	/***********************************************************
				 MEMORY STRUCTURES
	***********************************************************/
	
	
	static MEMORY_READ16_START( topspeed_readmem )
		{ 0x000000, 0x0fffff, MRA16_ROM },
		{ 0x400000, 0x40ffff, sharedram_r },	// block of ram seems to be all shared?
		{ 0x500000, 0x503fff, paletteram16_word_r },
		{ 0x7e0000, 0x7e0001, MRA16_NOP },
		{ 0x7e0002, 0x7e0003, taitosound_comm16_lsb_r },
		{ 0x800000, 0x80ffff, MRA16_RAM },	// unknown, road related?
		{ 0xa00000, 0xa0ffff, PC080SN_word_0_r },	/* tilemaps */
		{ 0xb00000, 0xb0ffff, PC080SN_word_1_r },	/* tilemaps */
		{ 0xd00000, 0xd00fff, MRA16_RAM },	/* sprite ram */
		{ 0xe00000, 0xe0ffff, MRA16_RAM },	/* sprite map */
	MEMORY_END
	
	static MEMORY_WRITE16_START( topspeed_writemem )
		{ 0x000000, 0x0fffff, MWA16_ROM },
		{ 0x400000, 0x40ffff, sharedram_w, &sharedram, &sharedram_size },
		{ 0x500000, 0x503fff, paletteram16_xBBBBBGGGGGRRRRR_word_w, &paletteram16 },
		{ 0x600002, 0x600003, cpua_ctrl_w },
		{ 0x7e0000, 0x7e0001, taitosound_port16_lsb_w },
		{ 0x7e0002, 0x7e0003, taitosound_comm16_lsb_w },
		{ 0x800000, 0x80ffff, MWA16_RAM },	// unknown, road related?
		{ 0xa00000, 0xa0ffff, PC080SN_word_0_w },
		{ 0xa20000, 0xa20003, PC080SN_yscroll_word_0_w },
		{ 0xa40000, 0xa40003, PC080SN_xscroll_word_0_w },
		{ 0xa50000, 0xa50003, PC080SN_ctrl_word_0_w },
		{ 0xb00000, 0xb0ffff, PC080SN_word_1_w },
		{ 0xb20000, 0xb20003, PC080SN_yscroll_word_1_w },
		{ 0xb40000, 0xb40003, PC080SN_xscroll_word_1_w },
		{ 0xb50000, 0xb50003, PC080SN_ctrl_word_1_w },
		{ 0xd00000, 0xd00fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0xe00000, 0xe0ffff, MWA16_RAM, &topspeed_spritemap },
	MEMORY_END
	
	static MEMORY_READ16_START( topspeed_cpub_readmem )
		{ 0x000000, 0x01ffff, MRA16_ROM },
		{ 0x400000, 0x40ffff, sharedram_r },
		{ 0x880000, 0x880001, topspeed_input_bypass_r },
		{ 0x880002, 0x880003, TC0220IOC_halfword_port_r },
		{ 0x900000, 0x9003ff, topspeed_motor_r },	/* motor CPU */
	MEMORY_END
	
	static MEMORY_WRITE16_START( topspeed_cpub_writemem )
		{ 0x000000, 0x01ffff, MWA16_ROM },
		{ 0x400000, 0X40ffff, sharedram_w, &sharedram },
		{ 0x880000, 0x880001, TC0220IOC_halfword_portreg_w },
		{ 0x880002, 0x880003, TC0220IOC_halfword_port_w },
		{ 0x900000, 0x9003ff, topspeed_motor_w },	/* motor CPU */
	MEMORY_END
	
	
	/***************************************************************************/
	
	public static Memory_ReadAddress z80_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new Memory_ReadAddress( 0x4000, 0x7fff, MRA_BANK10 ),
		new Memory_ReadAddress( 0x8000, 0x8fff, MRA_RAM ),
		new Memory_ReadAddress( 0x9001, 0x9001, YM2151_status_port_0_r ),
		new Memory_ReadAddress( 0x9002, 0x9100, MRA_RAM ),
		new Memory_ReadAddress( 0xa001, 0xa001, taitosound_slave_comm_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress z80_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0x8fff, MWA_RAM ),
		new Memory_WriteAddress( 0x9000, 0x9000, YM2151_register_port_0_w ),
		new Memory_WriteAddress( 0x9001, 0x9001, YM2151_data_port_0_w ),
		new Memory_WriteAddress( 0xa000, 0xa000, taitosound_slave_port_w ),
		new Memory_WriteAddress( 0xa001, 0xa001, taitosound_slave_comm_w ),
		new Memory_WriteAddress( 0xb000, 0xb000, rastan_adpcm_trigger_w ),
		new Memory_WriteAddress( 0xc000, 0xc000, rastan_c000_w ),
		new Memory_WriteAddress( 0xd000, 0xd000, rastan_d000_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	/***********************************************************
				 INPUT PORTS, DIPs
	***********************************************************/
	
	#define TAITO_COINAGE_WORLD_8 \
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") ); \
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") ); \
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") ); \
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") ); \
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") ); \
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") ); \
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") ); \
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") ); \
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_4C") ); \
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
	#define TAITO_COINAGE_JAPAN_8 \
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") ); \
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") ); \
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") ); \
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") ); \
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") ); \
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") ); \
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") ); \
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") ); \
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") ); \
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
	
	#define TAITO_COINAGE_US_8 \
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coinage") ); \
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") ); \
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") ); \
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") ); \
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") ); \
		PORT_DIPNAME( 0xc0, 0xc0, "Price to Continue" );\
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") ); \
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") ); \
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_1C") ); \
		PORT_DIPSETTING(    0xc0, "Same as Start" );
	#define TAITO_DIFFICULTY_8 \
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") ); \
		PORT_DIPSETTING(    0x02, "Easy" );\
		PORT_DIPSETTING(    0x03, "Medium" );\
		PORT_DIPSETTING(    0x01, "Hard" );\
		PORT_DIPSETTING(    0x00, "Hardest" );
	static InputPortPtr input_ports_topspeed = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x03, "Deluxe Motorized Cockpit" );	PORT_DIPSETTING(    0x02, "Upright (?); )
		PORT_DIPSETTING(    0x01, "Upright (alt?); )
		PORT_DIPSETTING(    0x00, "Standard Cockpit" );	PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		TAITO_COINAGE_WORLD_8
	
		PORT_START();  /* DSW B */
		TAITO_DIFFICULTY_8
		PORT_DIPNAME( 0x0c, 0x0c, "Initial Time" );	PORT_DIPSETTING(    0x00, "40 seconds" );	PORT_DIPSETTING(    0x04, "50 seconds" );	PORT_DIPSETTING(    0x0c, "60 seconds" );	PORT_DIPSETTING(    0x08, "70 seconds" );	PORT_DIPNAME( 0x30, 0x30, "Nitros" );	PORT_DIPSETTING(    0x20, "2" );	PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x10, "4" );	PORT_DIPSETTING(    0x00, "5" );	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x40, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN2 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_COIN1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW,  IPT_SERVICE1 );	/* Next bit is brake key (active low) for non-cockpit */
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON6 | IPF_PLAYER1 );/* 3 for brake [7 levels] */
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON8 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );/* main brake key */
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_BUTTON3 | IPF_PLAYER1 );/* nitro */
		PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW,  IPT_TILT );	PORT_BIT( 0x08, IP_ACTIVE_LOW,  IPT_START1 );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER1 );/* gear shift lo/hi */
		/* Next bit is accel key (active low/high, depends on cab DSW) for non-cockpit */
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON5 | IPF_PLAYER1 );/* 3 for accel [7 levels] */
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON7 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );/* main accel key */
	
		PORT_START();       /* IN2, unused */
	
		/* Note that sensitivity is chosen to suit keyboard control (for
		   sound selection in test mode and hi score name entry). With
		   an analogue wheel, the user will need to adjust this. */
	
		PORT_START(); 	/* continuous steer */
		PORT_ANALOG( 0xffff, 0x00, IPT_AD_STICK_X | IPF_PLAYER1, 10, 2, 0xff7f, 0x80);
		PORT_START();       /* fake, allowing digital steer */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_DIPNAME( 0x10, 0x10, "Steering type" );	PORT_DIPSETTING(    0x10, "Digital" );	PORT_DIPSETTING(    0x00, "Analogue" );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_topspedu = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x03, "Deluxe Motorized Cockpit" );	PORT_DIPSETTING(    0x02, "Upright (?); )
		PORT_DIPSETTING(    0x01, "Upright (alt?); )
		PORT_DIPSETTING(    0x00, "Standard Cockpit" );	PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		TAITO_COINAGE_WORLD_8
	
		PORT_START();  /* DSW B */
		TAITO_DIFFICULTY_8
		PORT_DIPNAME( 0x0c, 0x0c, "Initial Time" );	PORT_DIPSETTING(    0x00, "40 seconds" );	PORT_DIPSETTING(    0x04, "50 seconds" );	PORT_DIPSETTING(    0x0c, "60 seconds" );	PORT_DIPSETTING(    0x08, "70 seconds" );	PORT_DIPNAME( 0x30, 0x30, "Nitros" );	PORT_DIPSETTING(    0x20, "2" );	PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x10, "4" );	PORT_DIPSETTING(    0x00, "5" );	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x40, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN2 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_COIN1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW,  IPT_SERVICE1 );	/* Next bit is brake key (active low) for non-cockpit */
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON6 | IPF_PLAYER1 );/* 3 for brake [7 levels] */
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON8 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );/* main brake key */
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_BUTTON3 | IPF_PLAYER1 );/* nitro */
		PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW,  IPT_TILT );	PORT_BIT( 0x08, IP_ACTIVE_LOW,  IPT_START1 );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER1 );/* gear shift lo/hi */
		/* Next bit is accel key (active low/high, depends on cab DSW) for non-cockpit */
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON5 | IPF_PLAYER1 );/* 3 for accel [7 levels] */
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON7 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );/* main accel key */
	
		PORT_START();       /* IN2, unused */
	
		/* Note that sensitivity is chosen to suit keyboard control (for
		   sound selection in test mode and hi score name entry). With
		   an analogue wheel, the user will need to adjust this. */
	
		PORT_START(); 	/* continuous steer */
		PORT_ANALOG( 0xffff, 0x00, IPT_AD_STICK_X | IPF_PLAYER1, 10, 2, 0xff7f, 0x80);
		PORT_START();       /* fake, allowing digital steer */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_DIPNAME( 0x10, 0x10, "Steering type" );	PORT_DIPSETTING(    0x10, "Digital" );	PORT_DIPSETTING(    0x00, "Analogue" );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_fullthrl = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x03, "Deluxe Motorized Cockpit" );	PORT_DIPSETTING(    0x02, "Upright (?); )
		PORT_DIPSETTING(    0x01, "Upright (alt?); )
		PORT_DIPSETTING(    0x00, "Standard Cockpit" );	PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		TAITO_COINAGE_WORLD_8
	
		PORT_START();  /* DSW B */
		TAITO_DIFFICULTY_8
		PORT_DIPNAME( 0x0c, 0x0c, "Initial Time" );	PORT_DIPSETTING(    0x00, "40 seconds" );	PORT_DIPSETTING(    0x04, "50 seconds" );	PORT_DIPSETTING(    0x0c, "60 seconds" );	PORT_DIPSETTING(    0x08, "70 seconds" );	PORT_DIPNAME( 0x30, 0x30, "Nitros" );	PORT_DIPSETTING(    0x20, "2" );	PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x10, "4" );	PORT_DIPSETTING(    0x00, "5" );	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x40, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN2 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_COIN1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW,  IPT_SERVICE1 );	/* Next bit is brake key (active low) for non-cockpit */
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON6 | IPF_PLAYER1 );/* 3 for brake [7 levels] */
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON8 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );/* main brake key */
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_BUTTON3 | IPF_PLAYER1 );/* nitro */
		PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW,  IPT_TILT );	PORT_BIT( 0x08, IP_ACTIVE_LOW,  IPT_START1 );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER1 );/* gear shift lo/hi */
		/* Next bit is accel key (active low/high, depends on cab DSW) for non-cockpit */
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON5 | IPF_PLAYER1 );/* 3 for accel [7 levels] */
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON7 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );/* main accel key */
	
		PORT_START();       /* IN2, unused */
	
		/* Note that sensitivity is chosen to suit keyboard control (for
		   sound selection in test mode and hi score name entry). With
		   an analogue wheel, the user will need to adjust this. */
	
		PORT_START(); 	/* continuous steer */
		PORT_ANALOG( 0xffff, 0x00, IPT_AD_STICK_X | IPF_PLAYER1, 10, 2, 0xff7f, 0x80);
		PORT_START();       /* fake, allowing digital steer */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_DIPNAME( 0x10, 0x10, "Steering type" );	PORT_DIPSETTING(    0x10, "Digital" );	PORT_DIPSETTING(    0x00, "Analogue" );INPUT_PORTS_END(); }}; 
	
	
	/**************************************************************
					GFX DECODING
	**************************************************************/
	
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
	
	static GfxDecodeInfo topspeed_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX2, 0x0, tile16x8_layout,  0, 256 ),	/* sprite parts */
		new GfxDecodeInfo( REGION_GFX1, 0x0, charlayout,  0, 256 ),		/* sprites  playfield */
		// Road Lines gfxdecodable ?
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	/**************************************************************
				     YM2151 (SOUND)
	**************************************************************/
	
	/* handler called by the YM2151 emulator when the internal timers cause an IRQ */
	
	static void irq_handler(int irq)	/* assumes Z80 sandwiched between 68Ks */
	{
		cpu_set_irq_line(1,0,irq ? ASSERT_LINE : CLEAR_LINE);
	}
	
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,			/* 1 chip */
		4000000,	/* 4 MHz ? */
		new int[] { YM3012_VOL(50,MIXER_PAN_CENTER,50,MIXER_PAN_CENTER) },
		new WriteYmHandlerPtr[] { irq_handler },
		new WriteHandlerPtr[] { sound_bankswitch_w }
	);
	
	static struct ADPCMinterface adpcm_interface =
	{
		1,			/* 1 chip */
		8000,       /* 8000Hz playback */
		REGION_SOUND1,	/* memory region */
		{ 60 }
	};
	
	
	/***********************************************************
				     MACHINE DRIVERS
	***********************************************************/
	
	static MachineDriver machine_driver_topspeed = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz ??? */
				topspeed_readmem,topspeed_writemem,null,null,
				topspeed_interrupt, 1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				16000000/4,	/* 4 MHz ??? */
				z80_readmem, z80_writemem,null,null,
				ignore_interrupt,0	/* IRQs are triggered by the YM2151 */
			),
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz ??? */
				topspeed_cpub_readmem,topspeed_cpub_writemem,null,null,
				topspeed_cpub_interrupt, 1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* CPU slices */
		null,
	
		/* video hardware */
		40*8, 32*8, new rectangle( 0*8, 40*8-1, 2*8, 32*8-1 ),
	
		topspeed_gfxdecodeinfo,
		8192, 8192,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		topspeed_vh_start,
		topspeed_vh_stop,
		topspeed_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_ADPCM,
				adpcm_interface
			)
		}
	);
	
	
	
	/***************************************************************************
						DRIVERS
	***************************************************************************/
	
	static RomLoadPtr rom_topspeed = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1, 0 );/* 128K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE( "b14-67-1", 0x00000, 0x10000, 0x23f17616 )
		ROM_LOAD16_BYTE( "b14-68-1", 0x00001, 0x10000, 0x835659d9 )
		ROM_LOAD16_BYTE( "b14-54",   0x80000, 0x20000, 0x172924d5 )	/* 4 data roms */
		ROM_LOAD16_BYTE( "b14-52",   0x80001, 0x20000, 0xe1b5b2a1 )
		ROM_LOAD16_BYTE( "b14-55",   0xc0000, 0x20000, 0xa1f15499 )
		ROM_LOAD16_BYTE( "b14-53",   0xc0001, 0x20000, 0x04a04f5f )
	
		ROM_REGION( 0x20000, REGION_CPU3, 0 );/* 128K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "b14-69",   0x00000, 0x10000, 0xd652e300 )
		ROM_LOAD16_BYTE( "b14-70",   0x00001, 0x10000, 0xb720592b )
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* Z80 sound cpu */
		ROM_LOAD( "b14-25", 0x00000, 0x04000, 0x9eab28ef );	ROM_CONTINUE(       0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x40000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD16_BYTE( "b14-07",   0x00000, 0x20000, 0xc6025fff )	/* SCR tiles */
		ROM_LOAD16_BYTE( "b14-06",   0x00001, 0x20000, 0xb4e2536e )
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );	ROMX_LOAD( "b14-48", 0x000003, 0x20000, 0x30c7f265, ROM_SKIP(7) )	/* OBJ, bitplane 3 */
		ROMX_LOAD( "b14-49", 0x100003, 0x20000, 0x32ba4265, ROM_SKIP(7) )
		ROMX_LOAD( "b14-50", 0x000007, 0x20000, 0xec1ef311, ROM_SKIP(7) )
		ROMX_LOAD( "b14-51", 0x100007, 0x20000, 0x35041c5f, ROM_SKIP(7) )
	
		ROMX_LOAD( "b14-44", 0x000002, 0x20000, 0x9f6c030e, ROM_SKIP(7) )	/* OBJ, bitplane 2 */
		ROMX_LOAD( "b14-45", 0x100002, 0x20000, 0x63e4ce03, ROM_SKIP(7) )
		ROMX_LOAD( "b14-46", 0x000006, 0x20000, 0xd489adf2, ROM_SKIP(7) )
		ROMX_LOAD( "b14-47", 0x100006, 0x20000, 0xb3a1f75b, ROM_SKIP(7) )
	
		ROMX_LOAD( "b14-40", 0x000001, 0x20000, 0xfa2a3cb3, ROM_SKIP(7) )	/* OBJ, bitplane 1 */
		ROMX_LOAD( "b14-41", 0x100001, 0x20000, 0x09455a14, ROM_SKIP(7) )
		ROMX_LOAD( "b14-42", 0x000005, 0x20000, 0xab51f53c, ROM_SKIP(7) )
		ROMX_LOAD( "b14-43", 0x100005, 0x20000, 0x1e6d2b38, ROM_SKIP(7) )
	
		ROMX_LOAD( "b14-36", 0x000000, 0x20000, 0x20a7c1b8, ROM_SKIP(7) )	/* OBJ, bitplane 0 */
		ROMX_LOAD( "b14-37", 0x100000, 0x20000, 0x801b703b, ROM_SKIP(7) )
		ROMX_LOAD( "b14-38", 0x000004, 0x20000, 0xde0c213e, ROM_SKIP(7) )
		ROMX_LOAD( "b14-39", 0x100004, 0x20000, 0x798c28c5, ROM_SKIP(7) )
	
		ROM_REGION( 0x10000, REGION_GFX3, 0 );/* don't dispose */
		ROM_LOAD( "b14-30",  0x00000, 0x10000, 0xdccb0c7f );/* road gfx ?? */
	
	// One dump has this 0x10000 long, but just contains the same stuff repeated 8 times //
		ROM_REGION( 0x2000, REGION_USER1, 0 );	ROM_LOAD( "b14-31",  0x0000,  0x2000,  0x5c6b013d );/* microcontroller ? */
	
		ROM_REGION( 0x20000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "b14-28",  0x00000, 0x10000, 0xdf11d0ae );	ROM_LOAD( "b14-29",  0x10000, 0x10000, 0x7ad983e7 );ROM_END(); }}; 
	
	static RomLoadPtr rom_topspedu = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1, 0 );/* 128K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE     ( "b14-23", 0x00000, 0x10000, 0xdd0307fd )
		ROM_LOAD16_BYTE     ( "b14-24", 0x00001, 0x10000, 0xacdf08d4 )
		ROM_LOAD16_WORD_SWAP( "b14-05", 0x80000, 0x80000, 0x6557e9d8 )	/* data rom */
	
		ROM_REGION( 0x20000, REGION_CPU3, 0 );/* 128K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "b14-26", 0x00000, 0x10000, 0x659dc872 )
		ROM_LOAD16_BYTE( "b14-56", 0x00001, 0x10000, 0xd165cf1b )
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* Z80 sound cpu */
		ROM_LOAD( "b14-25", 0x00000, 0x04000, 0x9eab28ef );	ROM_CONTINUE(       0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x40000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD16_BYTE( "b14-07", 0x00000, 0x20000, 0xc6025fff )	/* SCR tiles */
		ROM_LOAD16_BYTE( "b14-06", 0x00001, 0x20000, 0xb4e2536e )
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "b14-01", 0x00000, 0x80000, 0x84a56f37 )	/* OBJ: each rom has 1 bitplane, forming 16x8 tiles */
		ROM_LOAD32_BYTE( "b14-02", 0x00001, 0x80000, 0x6889186b )
		ROM_LOAD32_BYTE( "b14-03", 0x00002, 0x80000, 0xd1ed9e71 )
		ROM_LOAD32_BYTE( "b14-04", 0x00003, 0x80000, 0xb63f0519 )
	
		ROM_REGION( 0x10000, REGION_GFX3, 0 );/* don't dispose */
		ROM_LOAD( "b14-30", 0x00000, 0x10000, 0xdccb0c7f );/* road gfx ?? */
	
		ROM_REGION( 0x2000, REGION_USER1, 0 );	ROM_LOAD( "b14-31", 0x0000,  0x2000,  0x5c6b013d );/* microcontroller ? */
	
		ROM_REGION( 0x20000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "b14-28", 0x00000, 0x10000, 0xdf11d0ae );	ROM_LOAD( "b14-29", 0x10000, 0x10000, 0x7ad983e7 );ROM_END(); }}; 
	
	static RomLoadPtr rom_fullthrl = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1, 0 );/* 128K for 68000 code (CPU A) */
		ROM_LOAD16_BYTE     ( "b14-67", 0x00000, 0x10000, 0x284c943f )
		ROM_LOAD16_BYTE     ( "b14-68", 0x00001, 0x10000, 0x54cf6196 )
		ROM_LOAD16_WORD_SWAP( "b14-05", 0x80000, 0x80000, 0x6557e9d8 )	/* data rom */
	
		ROM_REGION( 0x20000, REGION_CPU3, 0 );/* 128K for 68000 code (CPU B) */
		ROM_LOAD16_BYTE( "b14-69", 0x00000, 0x10000, 0xd652e300 )
		ROM_LOAD16_BYTE( "b14-71", 0x00001, 0x10000, 0xf7081727 )
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* Z80 sound cpu */
		ROM_LOAD( "b14-25", 0x00000, 0x04000, 0x9eab28ef );	ROM_CONTINUE(       0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x40000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD16_BYTE( "b14-07", 0x00000, 0x20000, 0xc6025fff )	/* SCR tiles */
		ROM_LOAD16_BYTE( "b14-06", 0x00001, 0x20000, 0xb4e2536e )
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD32_BYTE( "b14-01", 0x00000, 0x80000, 0x84a56f37 )	/* OBJ: each rom has 1 bitplane, forming 16x8 tiles */
		ROM_LOAD32_BYTE( "b14-02", 0x00001, 0x80000, 0x6889186b )
		ROM_LOAD32_BYTE( "b14-03", 0x00002, 0x80000, 0xd1ed9e71 )
		ROM_LOAD32_BYTE( "b14-04", 0x00003, 0x80000, 0xb63f0519 )
	
		ROM_REGION( 0x10000, REGION_GFX3, 0 );/* don't dispose */
		ROM_LOAD( "b14-30", 0x00000, 0x10000, 0xdccb0c7f );/* road gfx ?? */
	
		ROM_REGION( 0x2000, REGION_USER1, 0 );	ROM_LOAD( "b14-31", 0x0000,  0x2000,  0x5c6b013d );/* microcontroller ? */
	
		ROM_REGION( 0x20000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "b14-28", 0x00000, 0x10000, 0xdf11d0ae );	ROM_LOAD( "b14-29", 0x10000, 0x10000, 0x7ad983e7 );ROM_END(); }}; 
	
	
	public static InitDriverPtr init_topspeed = new InitDriverPtr() { public void handler() 
	{
	//	taitosnd_setz80_soundcpu( 2 );
	
		cpua_ctrl = 0xff;
		state_save_register_UINT16("main1", 0, "control", &cpua_ctrl, 1);
		state_save_register_func_postload(parse_control);
	
		state_save_register_int   ("main2", 0, "register", &ioc220_port);
	
		state_save_register_int   ("sound1", 0, "sound region", &banknum);
		state_save_register_func_postload(reset_sound_region);
	} };
	
	
	public static GameDriver driver_topspeed	   = new GameDriver("1987"	,"topspeed"	,"topspeed.java"	,rom_topspeed,null	,machine_driver_topspeed	,input_ports_topspeed	,init_topspeed	,ROT0	,	"Taito Corporation Japan", "Top Speed (World)" )
	public static GameDriver driver_topspedu	   = new GameDriver("1987"	,"topspedu"	,"topspeed.java"	,rom_topspedu,driver_topspeed	,machine_driver_topspeed	,input_ports_topspedu	,init_topspeed	,ROT0	,	"Taito America Corporation (Romstar license)", "Top Speed (US)" )
	public static GameDriver driver_fullthrl	   = new GameDriver("1987"	,"fullthrl"	,"topspeed.java"	,rom_fullthrl,driver_topspeed	,machine_driver_topspeed	,input_ports_fullthrl	,init_topspeed	,ROT0	,	"Taito Corporation", "Full Throttle (Japan)" )
	
}
