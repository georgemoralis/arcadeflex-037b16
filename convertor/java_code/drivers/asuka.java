/***************************************************************************

Asuka & Asuka  (+ Taito/Visco games on similar hardware)
=============

David Graves, Brian Troha

Made out of:	Rastan driver by Jarek Burczynski
				MAME Taito F2 driver
				Raine source - very special thanks to
				  Richard Bush and the Raine Team.
				two different drivers for Bonze Adventure that were
				  written at the same time by Yochizo and Frotz

	Bonze Adventure (c) 1988 Taito Corporation
	Asuka & Asuka   (c) 1988 Taito Corporation
	Maze of Flott   (c) 1989 Taito Corporation
	Galmedes        (c) 1992 Visco Corporation
	Earth Joker     (c) 1993 Visco Corporation
	Kokontouzai Eto Monogatari (c) 1994 Visco Corporation

Main CPU: MC68000 uses irq 5 (4 in bonze).
Sound   : Z80 & YM2151 + MSM5205 (YM2610 in bonze)
Chips   : TC0100SCN + TC0002OBJ + TC0110PCR (+ C-Chip in bonze)

Memory map for Asuka & Asuka
----------------------------

The other games seem identical but Eto is slightly different.

0x000000 - 0x0fffff : ROM (not all used for each game)
0x100000 - 0x103fff : 16k of RAM
0x200000 - 0x20000f : palette generator
0x400000 - 0x40000f : input ports and dipswitches
0x3a0000 - 0x3a0003 : sprite control
0x3e0000 - 0x3e0003 : communication with sound CPU
0xc00000 - 0xc2000f : TC0100SCN (see taitoic.c)
0xd00000 - 0xd007ff : sprite RAM


From "garmedes.txt"
-------------------

The following cord is written, on PCB:  K1100388A   J1100169A   M6100708A
There are the parts that were written as B68 on this PCB.
The original title of the game called B68 is unknown.
This PCB is the same as the one that is used with EARTH-JOKER.
<I think B68 is the Taito ROM id# for Asuka & Asuka - B.Troha>


Use of TC0100SCN
----------------

Asuka & Asuka: $e6a init code clearing TC0100SCN areas is erroneous.
It only clears 1/8 of the BG layers; then it clears too much of the
rowscroll areas [0xc000, 0xc400] causing overrun into next 64K block.

Asuka is one of the early Taito games using the TC0100SCN. (Ninja
Warriors was probably the first.) They didn't bother using its FG (text)
layer facility, instead placing text in the BG / sprite layers.

Maze of Flott [(c) one year later] and most other games with the
TC0100SCN do use the FG layer for text (Driftout is an exception).


TODO
----

DIPs

Bonze:
  - sprite priorities seem to be wrong sometimes
  - c-chip level data is close but not perfect
  - player restart positions are missing

Mofflot: $14c46 sub inits sound system: in a pause loop during this
it reads a dummy address.

Earthjkr: Wrong screen size? Left edge of green blueprints in
attract looks like it's incorrectly off screen.

Galmedes: Test mode has select1/2 stuck at on.

Eto: $76d0 might be a protection check? It reads to and writes from
the prog rom. Doesn't seem to cause problems though.

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class asuka
{
	
	WRITE16_HANDLER( asuka_spritectrl_w );
	WRITE16_HANDLER( asuka_spriteflip_w );
	
	
	
	
	WRITE16_HANDLER( bonzeadv_c_chip_w );
	READ16_HANDLER( bonzeadv_c_chip_r );
	
	
	/************************************************
				SOUND
	************************************************/
	
	static int banknum = -1;
	
	static void reset_sound_region(void)
	{
		cpu_setbank( 1, memory_region(REGION_CPU2) + (banknum * 0x4000) + 0x10000 );
	}
	
	public static WriteHandlerPtr sound_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		banknum = (data-1) & 0x03;
		reset_sound_region();
	} };
	
	
	/***********************************************************
				 MEMORY STRUCTURES
	***********************************************************/
	
	static MEMORY_READ16_START( bonzeadv_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x080000, 0x0fffff, MRA16_ROM },
		{ 0x10c000, 0x10ffff, MRA16_RAM },	/* main RAM */
		{ 0x200000, 0x200007, TC0110PCR_word_r },
		{ 0x390000, 0x390001, input_port_0_word_r },
		{ 0x3b0000, 0x3b0001, input_port_1_word_r },
		{ 0x3d0000, 0x3d0001, MRA16_NOP },
		{ 0x3e0002, 0x3e0003, taitosound_comm16_lsb_r },
		{ 0x800000, 0x800803, bonzeadv_c_chip_r },
		{ 0xc00000, 0xc0ffff, TC0100SCN_word_0_r },	/* tilemaps */
		{ 0xc20000, 0xc2000f, TC0100SCN_ctrl_word_0_r },
		{ 0xd00000, 0xd007ff, MRA16_RAM },	/* sprite ram */
		{ 0xd00800, 0xd01fff, MRA16_RAM },
	MEMORY_END
	
	static MEMORY_WRITE16_START( bonzeadv_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x10c000, 0x10ffff, MWA16_RAM },
		{ 0x200000, 0x200007, TC0110PCR_step1_word_w },
		{ 0x3a0000, 0x3a0001, asuka_spritectrl_w },
		{ 0x3c0000, 0x3c0001, MWA16_NOP },	/* watchdog ?? */
		{ 0x3e0000, 0x3e0001, taitosound_port16_lsb_w },
		{ 0x3e0002, 0x3e0003, taitosound_comm16_lsb_w },
		{ 0x800000, 0x800c01, bonzeadv_c_chip_w },
		{ 0xc00000, 0xc0ffff, TC0100SCN_word_0_w },	/* tilemaps */
		{ 0xc20000, 0xc2000f, TC0100SCN_ctrl_word_0_w },
		{ 0xd00000, 0xd007ff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0xd01bfe, 0xd01bff, asuka_spriteflip_w },
		{ 0xd00800, 0xd01fff, MWA16_RAM },
	MEMORY_END
	
	static MEMORY_READ16_START( asuka_readmem )
		{ 0x000000, 0x0fffff, MRA16_ROM },
		{ 0x100000, 0x103fff, MRA16_RAM },	/* RAM */
		{ 0x1076f0, 0x1076f1, MRA16_NOP },	/* Mofflott init does dummy reads here */
		{ 0x200000, 0x20000f, TC0110PCR_word_r },
		{ 0x3e0000, 0x3e0001, MRA16_NOP },
		{ 0x3e0002, 0x3e0003, taitosound_comm16_lsb_r },
		{ 0x400000, 0x40000f, TC0220IOC_halfword_r },
		{ 0xc00000, 0xc0ffff, TC0100SCN_word_0_r },	/* tilemaps */
		{ 0xc20000, 0xc2000f, TC0100SCN_ctrl_word_0_r },
		{ 0xd00000, 0xd007ff, MRA16_RAM },	/* sprite ram */
	MEMORY_END
	
	static MEMORY_WRITE16_START( asuka_writemem )
		{ 0x000000, 0x0fffff, MWA16_ROM },
		{ 0x100000, 0x103fff, MWA16_RAM },
		{ 0x200000, 0x20000f, TC0110PCR_step1_word_w },
		{ 0x3a0000, 0x3a0003, asuka_spritectrl_w },
		{ 0x3e0000, 0x3e0001, taitosound_port16_lsb_w },
		{ 0x3e0002, 0x3e0003, taitosound_comm16_lsb_w },
		{ 0x400000, 0x40000f, TC0220IOC_halfword_w },
		{ 0xc00000, 0xc0ffff, TC0100SCN_word_0_w },	/* tilemaps */
		{ 0xc10000, 0xc103ff, MWA16_NOP },	/* error in Asuka init code */
		{ 0xc20000, 0xc2000f, TC0100SCN_ctrl_word_0_w },
		{ 0xd00000, 0xd007ff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0xd01bfe, 0xd01bff, asuka_spriteflip_w },
	MEMORY_END
	
	static MEMORY_READ16_START( eto_readmem )
		{ 0x000000, 0x0fffff, MRA16_ROM },
		{ 0x100000, 0x10000f, TC0110PCR_word_r },
		{ 0x200000, 0x203fff, MRA16_RAM },	/* RAM */
		{ 0x300000, 0x30000f, TC0220IOC_halfword_r },
		{ 0x400000, 0x40000f, TC0220IOC_halfword_r },	/* service mode mirror */
		{ 0x4e0000, 0x4e0001, MRA16_NOP },
		{ 0x4e0002, 0x4e0003, taitosound_comm16_lsb_r },
		{ 0xc00000, 0xc007ff, MRA16_RAM },	/* sprite ram */
		{ 0xd00000, 0xd0ffff, TC0100SCN_word_0_r },	/* tilemaps */
		{ 0xd20000, 0xd2000f, TC0100SCN_ctrl_word_0_r },
	MEMORY_END
	
	static MEMORY_WRITE16_START( eto_writemem )	/* N.B. tc100scn mirror overlaps spriteram */
		{ 0x000000, 0x0fffff, MWA16_ROM },
		{ 0x100000, 0x10000f, TC0110PCR_step1_word_w },
		{ 0x200000, 0x203fff, MWA16_RAM },
		{ 0x300000, 0x30000f, TC0220IOC_halfword_w },
		{ 0x4a0000, 0x4a0003, asuka_spritectrl_w },
		{ 0x4e0000, 0x4e0001, taitosound_port16_lsb_w },
		{ 0x4e0002, 0x4e0003, taitosound_comm16_lsb_w },
		{ 0xc00000, 0xc007ff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0xc01bfe, 0xc01bff, asuka_spriteflip_w },
		{ 0xc00000, 0xc0ffff, TC0100SCN_word_0_w },	/* service mode mirror */
		{ 0xd00000, 0xd0ffff, TC0100SCN_word_0_w },	/* tilemaps */
		{ 0xd20000, 0xd2000f, TC0100SCN_ctrl_word_0_w },
	MEMORY_END
	
	
	/***************************************************************************/
	
	public static Memory_ReadAddress bonzeadv_z80_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new Memory_ReadAddress( 0x4000, 0x7fff, MRA_BANK1 ),
		new Memory_ReadAddress( 0xc000, 0xdfff, MRA_RAM ),
		new Memory_ReadAddress( 0xe000, 0xe000, YM2610_status_port_0_A_r ),
		new Memory_ReadAddress( 0xe001, 0xe001, YM2610_read_port_0_r ),
		new Memory_ReadAddress( 0xe002, 0xe002, YM2610_status_port_0_B_r ),
		new Memory_ReadAddress( 0xe201, 0xe201, taitosound_slave_comm_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress bonzeadv_z80_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xdfff, MWA_RAM ),
		new Memory_WriteAddress( 0xe000, 0xe000, YM2610_control_port_0_A_w ),
		new Memory_WriteAddress( 0xe001, 0xe001, YM2610_data_port_0_A_w ),
		new Memory_WriteAddress( 0xe002, 0xe002, YM2610_control_port_0_B_w ),
		new Memory_WriteAddress( 0xe003, 0xe003, YM2610_data_port_0_B_w ),
		new Memory_WriteAddress( 0xe200, 0xe200, taitosound_slave_port_w ),
		new Memory_WriteAddress( 0xe201, 0xe201, taitosound_slave_comm_w ),
		new Memory_WriteAddress( 0xe400, 0xe403, MWA_NOP ), /* pan */
		new Memory_WriteAddress( 0xe600, 0xe600, MWA_NOP ),
		new Memory_WriteAddress( 0xee00, 0xee00, MWA_NOP ),
		new Memory_WriteAddress( 0xf000, 0xf000, MWA_NOP ),
		new Memory_WriteAddress( 0xf200, 0xf200, sound_bankswitch_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_ReadAddress z80_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new Memory_ReadAddress( 0x4000, 0x7fff, MRA_BANK1 ),
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
	
	#define TAITO_DIFFICULTY_8 \
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") ); \
		PORT_DIPSETTING(    0x02, "Easy" );\
		PORT_DIPSETTING(    0x03, "Medium" );\
		PORT_DIPSETTING(    0x01, "Hard" );\
		PORT_DIPSETTING(    0x00, "Hardest" );
	#define ASUKA_PLAYERS_INPUT( player ) \
		PORT_START();  \
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | player );\
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | player );\
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | player );\
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | player );\
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | player );\
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | player );\
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );\
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	#define ASUKA_SYSTEM_INPUT \
		PORT_START();  \
		PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_TILT );\
		PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_SERVICE1 );\
		PORT_BIT( 0x04, IP_ACTIVE_LOW,  IPT_COIN1 );\
		PORT_BIT( 0x08, IP_ACTIVE_LOW,  IPT_COIN2 );\
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );\
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );\
		PORT_BIT( 0x40, IP_ACTIVE_LOW,  IPT_START1 );\
		PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_START2 );
	
	static InputPortPtr input_ports_bonzeadv = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* DSWA */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
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
	
		PORT_START(); 	/* DSWB */
		TAITO_DIFFICULTY_8
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x08, "40k,100k" );	PORT_DIPSETTING(    0x0c, "50k,150k" );	PORT_DIPSETTING(    0x04, "60k,200k" );	PORT_DIPSETTING(    0x00, "80k,250k" );	PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "2" );	PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x10, "4" );	PORT_DIPSETTING(    0x00, "5" );	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );	/* probably unused */
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* 800007 */
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_START();  /* 800009 */
		PORT_BIT_IMPULSE( 0x01, IP_ACTIVE_HIGH, IPT_COIN1, 1 );	PORT_BIT_IMPULSE( 0x02, IP_ACTIVE_HIGH, IPT_COIN2, 1 );
		PORT_START(); 	/* 80000B */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_START(); 	/* 80000d */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_jigkmgri = new InputPortPtr(){ public void handler() {  /* coinage DIPs differ from bonzeadv */
		PORT_START(); 	/* DSWA */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		TAITO_COINAGE_JAPAN_8
	
		PORT_START(); 	/* DSWB */
		TAITO_DIFFICULTY_8
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x08, "40k,100k" );	PORT_DIPSETTING(    0x0c, "50k,150k" );	PORT_DIPSETTING(    0x04, "60k,200k" );	PORT_DIPSETTING(    0x00, "80k,250k" );	PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "2" );	PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x10, "4" );	PORT_DIPSETTING(    0x00, "5" );	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );	/* probably unused */
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* 800007 */
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_START();  /* 800009 */
		PORT_BIT_IMPULSE( 0x01, IP_ACTIVE_HIGH, IPT_COIN1, 1 );	PORT_BIT_IMPULSE( 0x02, IP_ACTIVE_HIGH, IPT_COIN2, 1 );
		PORT_START(); 	/* 80000B */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_START(); 	/* 80000d */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_asuka = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* DSWA */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		TAITO_COINAGE_JAPAN_8
	
		PORT_START(); 	/* DSWB */
		TAITO_DIFFICULTY_8
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x10, "1" );	PORT_DIPSETTING(    0x20, "2" );	PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x00, "4" );	PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0xc0, 0x40, "Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0xc0, "Up to Level 2" );	PORT_DIPSETTING(    0x80, "Up to Level 3" );	PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
	
		/* IN0 */
		ASUKA_PLAYERS_INPUT( IPF_PLAYER1 )
	
		/* IN1 */
		ASUKA_PLAYERS_INPUT( IPF_PLAYER2 )
	
		/* IN2 */
		ASUKA_SYSTEM_INPUT
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_mofflott = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* DSWA */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		TAITO_COINAGE_JAPAN_8
	
		PORT_START(); 	/* DSWB */
		TAITO_DIFFICULTY_8
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x0c, "20k and every 50k" );	PORT_DIPSETTING(    0x08, "50k and every 100k" );	PORT_DIPSETTING(    0x04, "100k only" );	PORT_DIPSETTING(    0x00, "None" );	PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "2" );	PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x10, "4" );	PORT_DIPSETTING(    0x20, "5" );	PORT_BITX(    0x40, 0x40, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		/* IN0 */
		ASUKA_PLAYERS_INPUT( IPF_PLAYER1 )
	
		/* IN1 */
		ASUKA_PLAYERS_INPUT( IPF_PLAYER2 )
	
		/* IN2 */
		ASUKA_SYSTEM_INPUT
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_galmedes = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* DSWA */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		TAITO_COINAGE_JAPAN_8
	
		PORT_START(); 	/* DSWB */
		TAITO_DIFFICULTY_8
		PORT_DIPNAME( 0x0c, 0x08, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x08, "every 100k" );	PORT_DIPSETTING(    0x0c, "100k and every 200k" );	PORT_DIPSETTING(    0x04, "150k and every 200k" );	PORT_DIPSETTING(    0x00, "every 200k" );	PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "1" );	PORT_DIPSETTING(    0x10, "2" );	PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x00, "4" );	PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		/* IN0 */
		ASUKA_PLAYERS_INPUT( IPF_PLAYER1 )
	
		/* IN1 */
		ASUKA_PLAYERS_INPUT( IPF_PLAYER2 )
	
		/* IN2 */
		ASUKA_SYSTEM_INPUT
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_earthjkr = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* DSWA */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		TAITO_COINAGE_JAPAN_8
	
		PORT_START(); 	/* DSWB */
		TAITO_DIFFICULTY_8
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "1" );	PORT_DIPSETTING(    0x30, "2" );	PORT_DIPSETTING(    0x20, "3" );	PORT_DIPSETTING(    0x10, "4" );	PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		/* IN0 */
		ASUKA_PLAYERS_INPUT( IPF_PLAYER1 )
	
		/* IN1 */
		ASUKA_PLAYERS_INPUT( IPF_PLAYER2 )
	
		/* IN2 */
		ASUKA_SYSTEM_INPUT
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_eto = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* DSWA */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		TAITO_COINAGE_JAPAN_8
	
		PORT_START(); 	/* DSWB */
		TAITO_DIFFICULTY_8
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		/* IN0 */
		ASUKA_PLAYERS_INPUT( IPF_PLAYER1 )
	
		/* IN1 */
		ASUKA_PLAYERS_INPUT( IPF_PLAYER2 )
	
		/* IN2 */
		ASUKA_SYSTEM_INPUT
	INPUT_PORTS_END(); }}; 
	
	
	/**************************************************************
					GFX DECODING
	**************************************************************/
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,1),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 2*4, 3*4, 0*4, 1*4, 6*4, 7*4, 4*4, 5*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8
	);
	
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,1),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 2*4, 3*4, 0*4, 1*4, 6*4, 7*4, 4*4, 5*4,
		  10*4, 11*4, 8*4, 9*4, 14*4, 15*4, 12*4, 13*4 },
		new int[] { 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64,
		  8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
		128*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout,  0, 256 ),	/* OBJ */
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,  0, 256 ),	/* SCR */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	/**************************************************************
					SOUND
	**************************************************************/
	
	static void irq_handler(int irq)
	{
		cpu_set_irq_line(1,0,irq ? ASSERT_LINE : CLEAR_LINE);
	}
	
	static YM2610interface ym2610_interface = new YM2610interface
	(
		1,	/* 1 chip */
		8000000,	/* 8 MHz */
		new int[] { 30 },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteYmHandlerPtr[] { irq_handler },
		new int[] { REGION_SOUND1 },	/* Delta-T */
		new int[] { REGION_SOUND1 },	/* ADPCM */
		new int[] { YM3012_VOL(60,MIXER_PAN_LEFT,60,MIXER_PAN_RIGHT) }
	);
	
	
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
	
	static MachineDriver machine_driver_bonzeadv = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				8000000,    /* RAINE suggests 12 MHz */
				bonzeadv_readmem,bonzeadv_writemem,null,null,
				m68_level4_irq, 1
			),
			new MachineCPU(
				CPU_Z80,    /* sound CPU, also required for test mode */
				4000000,
				bonzeadv_z80_readmem,bonzeadv_z80_writemem,0,null,
				ignore_interrupt,0
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		10,	/* 10 CPU slices per frame - enough for the sound CPU to read all commands */
		null,
	
		/* video hardware */
		40*8, 32*8, new rectangle( 0*8, 40*8-1, 2*8, 32*8-1 ),
		gfxdecodeinfo,
		4096, 4096,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		asuka_vh_start,
		asuka_vh_stop,
		asuka_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2610,
				ym2610_interface
			)
		}
	);
	
	static MachineDriver machine_driver_asuka = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				8000000,	/* 8 MHz ??? */
				asuka_readmem,asuka_writemem,null,null,
				m68_level5_irq,1
			),
			new MachineCPU(
				CPU_Z80,
				4000000,	/* 4 MHz ??? */
				z80_readmem,z80_writemem,null,null,
				ignore_interrupt,0
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		10,	/* 10 CPU slices per frame - enough for the sound CPU to read all commands */
		null,
	
		/* video hardware */
		40*8, 32*8, new rectangle( 0*8, 40*8-1, 2*8, 32*8-1 ),
		gfxdecodeinfo,
		4096, 4096,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		asuka_vh_start,
		asuka_vh_stop,
		asuka_vh_screenrefresh,
	
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
	
	static MachineDriver machine_driver_galmedes = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				8000000,	/* 8 MHz ??? */
				asuka_readmem,asuka_writemem,null,null,
				m68_level5_irq,1
			),
			new MachineCPU(
				CPU_Z80,
				4000000,	/* 4 MHz ??? */
				z80_readmem,z80_writemem,null,null,
				ignore_interrupt,0
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		10,	/* 10 CPU slices per frame - enough for the sound CPU to read all commands */
		null,
	
		/* video hardware */
		40*8, 32*8, new rectangle( 0*8, 40*8-1, 2*8, 32*8-1 ),
		gfxdecodeinfo,
		4096, 4096,	/* only Mofflott uses full palette space */
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		galmedes_vh_start,
		asuka_vh_stop,
		asuka_vh_screenrefresh,
	
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
	
	static MachineDriver machine_driver_eto = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				8000000,	/* 8 MHz ??? */
				eto_readmem,eto_writemem,null,null,
				m68_level5_irq,1
			),
			new MachineCPU(
				CPU_Z80,
				4000000,	/* 4 MHz ??? */
				z80_readmem,z80_writemem,null,null,
				ignore_interrupt,0
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		10,	/* 10 CPU slices per frame - enough for the sound CPU to read all commands */
		null,
	
		/* video hardware */
		40*8, 32*8, new rectangle( 0*8, 40*8-1, 2*8, 32*8-1 ),
		gfxdecodeinfo,
		4096, 4096,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		galmedes_vh_start,
		asuka_vh_stop,
		asuka_vh_screenrefresh,
	
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
	
	static RomLoadPtr rom_bonzeadv = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1, 0 );    /* 68000 code */
		ROM_LOAD16_BYTE( "b41-09-1", 0x00000, 0x10000, 0xaf821fbc )
		ROM_LOAD16_BYTE( "b41-11-1", 0x00001, 0x10000, 0x823fff00 )
		ROM_LOAD16_BYTE( "b41-10",   0x20000, 0x10000, 0x4ca94d77 )
		ROM_LOAD16_BYTE( "b41-15",   0x20001, 0x10000, 0xaed7a0d0 )
		ROM_LOAD16_WORD_SWAP( "b41-01", 0x80000, 0x80000, 0x5d072fa4 )
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "b41-03",  0x00000, 0x80000, 0x736d35d0 );/* SCR tiles (8 x 8) */
	
		ROM_REGION( 0x80000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD( "b41-02",  0x00000, 0x80000, 0x29f205d9 );/* Sprites (16 x 16) */
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );    /* sound cpu */
		ROM_LOAD( "b41-13",  0x00000, 0x04000, 0x9e464254 );	ROM_CONTINUE(        0x10000, 0x0c000 );
		ROM_REGION( 0x80000, REGION_SOUND1, 0 );  /* ADPCM samples */
		ROM_LOAD( "b41-04",  0x00000, 0x80000, 0xc668638f );ROM_END(); }}; 
	
	static RomLoadPtr rom_bonzeadu = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1, 0 );    /* 68000 code */
		ROM_LOAD16_BYTE( "b41-09-1", 0x00000, 0x10000, 0xaf821fbc )
		ROM_LOAD16_BYTE( "b41-11-1", 0x00001, 0x10000, 0x823fff00 )
		ROM_LOAD16_BYTE( "b41-10",   0x20000, 0x10000, 0x4ca94d77 )
		ROM_LOAD16_BYTE( "b41-14",   0x20001, 0x10000, 0x37def16a )
		ROM_LOAD16_WORD_SWAP( "b41-01", 0x80000, 0x80000, 0x5d072fa4 )
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "b41-03",  0x00000, 0x80000, 0x736d35d0 );/* SCR tiles (8 x 8) */
	
		ROM_REGION( 0x80000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD( "b41-02",  0x00000, 0x80000, 0x29f205d9 );/* Sprites (16 x 16) */
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );    /* sound cpu */
		ROM_LOAD( "b41-13",  0x00000, 0x04000, 0x9e464254 );	ROM_CONTINUE(        0x10000, 0x0c000 );
		ROM_REGION( 0x80000, REGION_SOUND1, 0 );  /* ADPCM samples */
		ROM_LOAD( "b41-04",  0x00000, 0x80000, 0xc668638f );ROM_END(); }}; 
	
	static RomLoadPtr rom_jigkmgri = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1, 0 );    /* 68000 code */
		ROM_LOAD16_BYTE( "b41-09-1", 0x00000, 0x10000, 0xaf821fbc )
		ROM_LOAD16_BYTE( "b41-11-1", 0x00001, 0x10000, 0x823fff00 )
		ROM_LOAD16_BYTE( "b41-10",   0x20000, 0x10000, 0x4ca94d77 )
		ROM_LOAD16_BYTE( "b41-12",   0x20001, 0x10000, 0x40d9c1fc )
		ROM_LOAD16_WORD_SWAP( "b41-01", 0x80000, 0x80000, 0x5d072fa4 )
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "b41-03",  0x00000, 0x80000, 0x736d35d0 );/* Tiles (8 x 8) */
	
		ROM_REGION( 0x80000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD( "b41-02",  0x00000, 0x80000, 0x29f205d9 );/* Sprites (16 x 16) */
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );    /* sound cpu */
		ROM_LOAD( "b41-13",  0x00000, 0x04000, 0x9e464254 );	ROM_CONTINUE(        0x10000, 0x0c000 );  /* banked stuff */
	
		ROM_REGION( 0x80000, REGION_SOUND1, 0 );  /* ADPCM samples */
		ROM_LOAD( "b41-04",  0x00000, 0x80000, 0xc668638f );ROM_END(); }}; 
	
	static RomLoadPtr rom_asuka = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1, 0 );    /* 1024k for 68000 code */
		ROM_LOAD16_BYTE( "asuka_13.rom",  0x00000, 0x20000, 0x855efb3e )
		ROM_LOAD16_BYTE( "asuka_12.rom",  0x00001, 0x20000, 0x271eeee9 )
	
		/* 0x040000 - 0x7ffff is intentionally empty */
		ROM_LOAD16_WORD( "asuka_03.rom",  0x80000, 0x80000, 0xd3a59b10 )	/* Fix ROM */
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "asuka_01.rom",  0x00000, 0x80000, 0x89f32c94 );/* SCR tiles (8 x 8) */
	
		ROM_REGION( 0xa0000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD       ( "asuka_02.rom", 0x00000, 0x80000, 0xf5018cd3 );/* Sprites (16 x 16) */
		ROM_LOAD16_BYTE( "asuka_07.rom", 0x80000, 0x10000, 0xc113acc8 )
		ROM_LOAD16_BYTE( "asuka_06.rom", 0x80001, 0x10000, 0xf517e64d )
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* sound cpu */
		ROM_LOAD( "asuka_11.rom", 0x00000, 0x04000, 0xc378b508 );	ROM_CONTINUE(             0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x10000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "asuka_10.rom", 0x00000, 0x10000, 0x387aaf40 );ROM_END(); }}; 
	
	static RomLoadPtr rom_mofflott = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1, 0 );    /* 1024k for 68000 code */
		ROM_LOAD16_BYTE( "c17-09.bin",  0x00000, 0x20000, 0x05ee110f )
		ROM_LOAD16_BYTE( "c17-08.bin",  0x00001, 0x20000, 0xd0aacffd )
	
		/* 0x40000 - 0x7ffff is intentionally empty */
		ROM_LOAD16_WORD( "c17-03.bin",  0x80000, 0x80000, 0x27047fc3 )	/* Fix ROM */
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "c17-01.bin",  0x00000, 0x80000, 0xe9466d42 );/* SCR tiles (8 x 8) */
	
		ROM_REGION( 0xa0000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD       ( "c17-02.bin", 0x00000, 0x80000, 0x8860a8db );/* Sprites (16 x 16) */
		ROM_LOAD16_BYTE( "c17-05.bin", 0x80000, 0x10000, 0x57ac4741 )
		ROM_LOAD16_BYTE( "c17-04.bin", 0x80001, 0x10000, 0xf4250410 )
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* sound cpu */
		ROM_LOAD( "c17-07.bin", 0x00000, 0x04000, 0xcdb7bc2c );	ROM_CONTINUE(           0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x10000, REGION_SOUND1, 0 );/* ADPCM samples */
		ROM_LOAD( "c17-06.bin", 0x00000, 0x10000, 0x5c332125 );ROM_END(); }}; 
	
	static RomLoadPtr rom_galmedes = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1, 0 );    /* 1024k for 68000 code */
		ROM_LOAD16_BYTE( "gm-prg1.bin",  0x00000, 0x20000, 0x32a70753 )
		ROM_LOAD16_BYTE( "gm-prg0.bin",  0x00001, 0x20000, 0xfae546a4 )
	
		/* 0x40000 - 0x7ffff is intentionally empty */
		ROM_LOAD16_WORD( "gm-30.rom",    0x80000, 0x80000, 0x4da2a407 )	/* Fix ROM */
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "gm-scn.bin", 0x00000, 0x80000, 0x3bab0581 );/* SCR tiles (8 x 8) */
	
		ROM_REGION( 0x80000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD( "gm-obj.bin", 0x00000, 0x80000, 0x7a4a1315 );/* Sprites (16 x 16) */
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* sound cpu */
		ROM_LOAD( "gm-snd.bin", 0x00000, 0x04000, 0xd6f56c21 );	ROM_CONTINUE(           0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x10000, REGION_SOUND1, 0 );	/* empty region */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_earthjkr = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1, 0 );    /* 1024k for 68000 code */
		ROM_LOAD16_BYTE( "ej_3b.rom",  0x00000, 0x20000, 0xbdd86fc2 )
		ROM_LOAD16_BYTE( "ej_3a.rom",  0x00001, 0x20000, 0x9c8050c6 )
	
		/* 0x40000 - 0x7ffff is intentionally empty */
		ROM_LOAD16_WORD( "ej_30e.rom", 0x80000, 0x80000, 0x49d1f77f )	/* Fix ROM */
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "ej_chr.rom", 0x00000, 0x80000, 0xac675297 );/* SCR tiles (8 x 8) */
	
		ROM_REGION( 0xa0000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD       ( "ej_obj.rom", 0x00000, 0x80000, 0x5f21ac47 );/* Sprites (16 x 16) */
		ROM_LOAD16_BYTE( "ej_1.rom",   0x80000, 0x10000, 0xcb4891db )
		ROM_LOAD16_BYTE( "ej_0.rom",   0x80001, 0x10000, 0xb612086f )
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* sound cpu */
		ROM_LOAD( "ej_2.rom", 0x00000, 0x04000, 0x42ba2566 );	ROM_CONTINUE(         0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x10000, REGION_SOUND1, 0 );	/* empty region */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_eto = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1, 0 );    /* 1024k for 68000 code */
		ROM_LOAD16_BYTE( "eto-1.23",  0x00000, 0x20000, 0x44286597 )
		ROM_LOAD16_BYTE( "eto-0.8",   0x00001, 0x20000, 0x57b79370 )
	
		/* 0x40000 - 0x7ffff is intentionally empty */
		ROM_LOAD16_WORD( "eto-2.30",    0x80000, 0x80000, 0x12f46fb5 )	/* Fix ROM */
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "eto-4.3", 0x00000, 0x80000, 0xa8768939 );/* Sprites (16 x 16) */
	
		ROM_REGION( 0x80000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD( "eto-3.6", 0x00000, 0x80000, 0xdd247397 );/* SCR tiles (8 x 8) */
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* sound cpu */
		ROM_LOAD( "eto-5.27", 0x00000, 0x04000, 0xb3689da0 );	ROM_CONTINUE(         0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x10000, REGION_SOUND1, 0 );	/* empty region */
	ROM_END(); }}; 
	
	
	static public static InitDriverPtr init_asuka = new InitDriverPtr() { public void handler() 
	{
		state_save_register_int("sound1", 0, "sound region", &banknum);
		state_save_register_func_postload(reset_sound_region);
	} };
	
	
	public static GameDriver driver_bonzeadv	   = new GameDriver("1988"	,"bonzeadv"	,"asuka.java"	,rom_bonzeadv,null	,machine_driver_bonzeadv	,input_ports_bonzeadv	,init_asuka	,ROT0	,	"Taito Corporation Japan", "Bonze Adventure (World)" )
	public static GameDriver driver_bonzeadu	   = new GameDriver("1988"	,"bonzeadu"	,"asuka.java"	,rom_bonzeadu,driver_bonzeadv	,machine_driver_bonzeadv	,input_ports_jigkmgri	,init_asuka	,ROT0	,	"Taito America Corporation", "Bonze Adventure (US)" )
	public static GameDriver driver_jigkmgri	   = new GameDriver("1988"	,"jigkmgri"	,"asuka.java"	,rom_jigkmgri,driver_bonzeadv	,machine_driver_bonzeadv	,input_ports_jigkmgri	,init_asuka	,ROT0	,	"Taito Corporation", "Jigoku Meguri (Japan)" )
	public static GameDriver driver_asuka	   = new GameDriver("1988"	,"asuka"	,"asuka.java"	,rom_asuka,null	,machine_driver_asuka	,input_ports_asuka	,init_asuka	,ROT270	,	"Taito Corporation", "Asuka & Asuka (Japan)" )
	public static GameDriver driver_mofflott	   = new GameDriver("1989"	,"mofflott"	,"asuka.java"	,rom_mofflott,null	,machine_driver_galmedes	,input_ports_mofflott	,init_asuka	,ROT270	,	"Taito Corporation", "Maze of Flott (Japan)" )
	public static GameDriver driver_galmedes	   = new GameDriver("1992"	,"galmedes"	,"asuka.java"	,rom_galmedes,null	,machine_driver_galmedes	,input_ports_galmedes	,init_asuka	,ROT270	,	"Visco", "Galmedes (Japan)" )
	public static GameDriver driver_earthjkr	   = new GameDriver("1993"	,"earthjkr"	,"asuka.java"	,rom_earthjkr,null	,machine_driver_galmedes	,input_ports_earthjkr	,init_asuka	,ROT270	,	"Visco", "U.N. Defense Force: Earth Joker (Japan)" )
	public static GameDriver driver_eto	   = new GameDriver("1994"	,"eto"	,"asuka.java"	,rom_eto,null	,machine_driver_eto	,input_ports_eto	,init_asuka	,ROT0	,	"Visco", "Kokontouzai Eto Monogatari (Japan)" )
}
