/***************************************************************************

Cadash
======

David Graves

Made out of:	Rastan driver by Jarek Burczynski
			MAME Taito F2 driver
			Raine source - thanks to Richard Bush
			  and the Raine Team.

Main CPU: MC68000 uses irq 4,5.
Sound   : Z80 & YM2151

Cadashu Info (Malcor)
---------------------

Main PCB (JAMMA) K1100528A
Main processor  - 68000 12MHz
                - HD64180RP8 8MHz (8 bit processor, dual channel DMAC,
                             memory mapped I/O, used for multigame link)
Misc custom ICs including three PQFPs, one PGA, and one SIP


Memory map for Cadash
---------------------

0x000000 - 0x07ffff : ROM
0x080000 - 0x080003 : sprite control
0x0c0000 - 0x0c0003 : communication with sound CPU
0x100000 - 0x107fff : 32k of RAM
0x800000 - 0x800fff : network RAM (for 2 machine hookup)
0x900000 - 0x90000f : input ports and dipswitches
0xa00000 - 0xa0000f : palette generator
0xb00000 - 0xb007ff : sprite RAM
0xc00000 - c200000f : standard TC0100SCN memory map (see taitoic.c)


TODO
----

Hooks for twin arcade machine setup: will involve emulating an extra
microcontroller, the 07 rom might be the program for it.

DIPs


***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class cadash
{
	
	
	WRITE16_HANDLER( cadash_spritectrl_w );
	WRITE16_HANDLER( cadash_spriteflip_w );
	
	
	
	
	
	/***********************************************************
					INTERRUPTS
	***********************************************************/
	
	void cadash_irq_handler(int irq);
	
	void cadash_interrupt5(int x)
	{
		cpu_cause_interrupt(0,5);
	}
	
	public static InterruptPtr cadash_interrupt = new InterruptPtr() { public int handler() 
	{
		timer_set(TIME_IN_CYCLES(500,0),0, cadash_interrupt5);
		return 4;  /* interrupt vector 4 */
	} };
	
	
	/************************************************
				SOUND
	************************************************/
	
	public static WriteHandlerPtr cadash_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int bankaddress;
		UBytePtr RAM = memory_region(REGION_CPU2);
	
		bankaddress = 0x10000 + ((data-1) & 0x03) * 0x4000;
		cpu_setbank(10,&RAM[bankaddress]);
	} };
	
	
	/***********************************************************
				 MEMORY STRUCTURES
	***********************************************************/
	
	static MEMORY_READ16_START( cadash_readmem )
		{ 0x000000, 0x07ffff, MRA16_ROM },
		{ 0x0c0000, 0x0c0001, MRA16_NOP },
		{ 0x0c0002, 0x0c0003, taitosound_comm16_lsb_r },
		{ 0x100000, 0x107fff, MRA16_RAM },	/* RAM */
		{ 0x800000, 0x800fff, MRA16_RAM },	/* network ram */
		{ 0x900000, 0x90000f, TC0220IOC_halfword_r },
		{ 0xa00000, 0xa0000f, TC0110PCR_word_r },
		{ 0xb00000, 0xb007ff, MRA16_RAM },	/* sprite ram */
		{ 0xc00000, 0xc0ffff, TC0100SCN_word_0_r },	/* tilemaps */
		{ 0xc20000, 0xc2000f, TC0100SCN_ctrl_word_0_r },
	MEMORY_END
	
	static MEMORY_WRITE16_START( cadash_writemem )
		{ 0x000000, 0x07ffff, MWA16_ROM },
		{ 0x080000, 0x080003, cadash_spritectrl_w },
		{ 0x0c0000, 0x0c0001, taitosound_port16_lsb_w },
		{ 0x0c0002, 0x0c0003, taitosound_comm16_lsb_w },
		{ 0x100000, 0x107fff, MWA16_RAM },
		{ 0x800000, 0x800fff, MWA16_RAM },	/* network ram */
		{ 0x900000, 0x90000f, TC0220IOC_halfword_w },
		{ 0xa00000, 0xa0000f, TC0110PCR_step1_4bpg_word_w },
		{ 0xb00000, 0xb007ff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0xb01bfe, 0xb01bff, cadash_spriteflip_w },
		{ 0xc00000, 0xc0ffff, TC0100SCN_word_0_w },	/* tilemaps */
		{ 0xc20000, 0xc2000f, TC0100SCN_ctrl_word_0_w },
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
	//	new Memory_WriteAddress( 0xb000, 0xb000, rastan_adpcm_trigger_w ), /* No ADPCM in this game */
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
	#define ASUKA_PLAYERS_INPUT( player ) \
		PORT_START();  \
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );\
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );\
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | player );\
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | player );\
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | player );\
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | player );\
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | player );\
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | player );
	#define ASUKA_SYSTEM_INPUT \
		PORT_START();  \
		PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 );\
		PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_COIN2 );\
		PORT_BIT( 0x04, IP_ACTIVE_LOW,  IPT_START2 );\
		PORT_BIT( 0x08, IP_ACTIVE_LOW,  IPT_START1 );\
		PORT_BIT( 0x10, IP_ACTIVE_LOW,  IPT_SERVICE1 );\
		PORT_BIT( 0x20, IP_ACTIVE_LOW,  IPT_TILT );\
		PORT_BIT( 0x40, IP_ACTIVE_LOW,  IPT_UNKNOWN );\
		PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN );
	
	static InputPortPtr input_ports_cadash = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* DSWA */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );	// Manual says leave it off
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		TAITO_COINAGE_WORLD_8
	
		PORT_START(); 	/* DSWB */
		TAITO_DIFFICULTY_8
		PORT_DIPNAME( 0x0c, 0x0c, "Starting Time" );	PORT_DIPSETTING(    0x00, "5:00" );	PORT_DIPSETTING(    0x04, "6:00" );	PORT_DIPSETTING(    0x0c, "7:00" );	PORT_DIPSETTING(    0x08, "8:00" );	/* Round cleared   Added time	*/
		/*       1            8:00	*/
		/*       2           10:00	*/
		/*       3            8:00	*/
		/*       4            7:00	*/
		/*       5            9:00	*/
		PORT_DIPNAME( 0x30, 0x30, "Added Time (after round clear); )
		PORT_DIPSETTING(    0x00, "Default - 2:00" );	PORT_DIPSETTING(    0x10, "Default - 1:00" );	PORT_DIPSETTING(    0x30, "Default" );	PORT_DIPSETTING(    0x20, "Default + 1:00" );	PORT_DIPNAME( 0xc0, 0xc0, "Communication Mode" );	PORT_DIPSETTING(    0xc0, "Stand alone" );	PORT_DIPSETTING(    0x80, "Master" );	PORT_DIPSETTING(    0x00, "Slave" );//	PORT_DIPSETTING(    0x40, "Stand alone" );
		/* IN0 */
		ASUKA_PLAYERS_INPUT( IPF_PLAYER1 )
	
		/* IN1 */
		ASUKA_PLAYERS_INPUT( IPF_PLAYER2 )
	
		/* IN2 */
		ASUKA_SYSTEM_INPUT
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_cadashj = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* DSWA */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );	// Manual says leave it off
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
		PORT_DIPNAME( 0x0c, 0x0c, "Starting Time" );	PORT_DIPSETTING(    0x00, "5:00" );	PORT_DIPSETTING(    0x04, "6:00" );	PORT_DIPSETTING(    0x0c, "7:00" );	PORT_DIPSETTING(    0x08, "8:00" );	PORT_DIPNAME( 0x30, 0x30, "Added Time (after round clear); )
		PORT_DIPSETTING(    0x00, "Default - 2:00" );	PORT_DIPSETTING(    0x10, "Default - 1:00" );	PORT_DIPSETTING(    0x30, "Default" );	PORT_DIPSETTING(    0x20, "Default + 1:00" );	PORT_DIPNAME( 0xc0, 0xc0, "Communication Mode" );	PORT_DIPSETTING(    0xc0, "Stand alone" );	PORT_DIPSETTING(    0x80, "Master" );	PORT_DIPSETTING(    0x00, "Slave" );//	PORT_DIPSETTING(    0x40, "Stand alone" );
		/* IN0 */
		ASUKA_PLAYERS_INPUT( IPF_PLAYER1 )
	
		/* IN1 */
		ASUKA_PLAYERS_INPUT( IPF_PLAYER2 )
	
		/* IN2 */
		ASUKA_SYSTEM_INPUT
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_cadashu = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* DSWA */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );	// Manual says leave it off
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		TAITO_COINAGE_US_8
	
		PORT_START(); 	/* DSWB */
		TAITO_DIFFICULTY_8
		PORT_DIPNAME( 0x0c, 0x0c, "Starting Time" );	PORT_DIPSETTING(    0x00, "5:00" );	PORT_DIPSETTING(    0x04, "6:00" );	PORT_DIPSETTING(    0x0c, "7:00" );	PORT_DIPSETTING(    0x08, "8:00" );	PORT_DIPNAME( 0x30, 0x30, "Added Time (after round clear); )
		PORT_DIPSETTING(    0x00, "Default - 2:00" );	PORT_DIPSETTING(    0x10, "Default - 1:00" );	PORT_DIPSETTING(    0x30, "Default" );	PORT_DIPSETTING(    0x20, "Default + 1:00" );	PORT_DIPNAME( 0xc0, 0xc0, "Communication Mode" );	PORT_DIPSETTING(    0xc0, "Stand alone" );	PORT_DIPSETTING(    0x80, "Master" );	PORT_DIPSETTING(    0x00, "Slave" );//	PORT_DIPSETTING(    0x40, "Stand alone" );
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
		8,8,	/* 8*8 characters */
		RGN_FRAC(1,1),
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 2*4, 3*4, 0*4, 1*4, 6*4, 7*4, 4*4, 5*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8	/* every sprite takes 32 consecutive bytes */
	);
	
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		RGN_FRAC(1,1),
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 2*4, 3*4, 0*4, 1*4, 6*4, 7*4, 4*4, 5*4, 10*4, 11*4, 8*4, 9*4, 14*4, 15*4, 12*4, 13*4 },
		new int[] { 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64, 8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
		128*8	/* every sprite takes 128 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout,  0, 256 ),	/* sprites  playfield */
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,  0, 256 ),	/* sprites  playfield */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	/**************************************************************
				     YM2151 (SOUND)
	**************************************************************/
	
	/* handler called by the YM2151 emulator when the internal timers cause an IRQ */
	
	static void irq_handler(int irq)
	{
		cpu_set_irq_line(1,0,irq ? ASSERT_LINE : CLEAR_LINE);
	}
	
	
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,			/* 1 chip */
		4000000,	/* 4 MHz ? */
		new int[] { YM3012_VOL(50,MIXER_PAN_CENTER,50,MIXER_PAN_CENTER) },
		new WriteYmHandlerPtr[] { irq_handler },
		new WriteHandlerPtr[] { cadash_bankswitch_w }
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
	
	public static VhEofCallbackPtr cadash_eof_callback = new VhEofCallbackPtr() { public void handler() 
	{
		buffer_spriteram16_w(0,0,0);
	} };
	
	static MachineDriver machine_driver_cadash = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz ??? */
				cadash_readmem,cadash_writemem,null,null,
				cadash_interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				4000000,	/* 4 MHz ??? */
				z80_readmem,z80_writemem,null,null,
				ignore_interrupt,1
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
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_BUFFERS_SPRITERAM,
		cadash_eof_callback,
		cadash_vh_start,
		cadash_vh_stop,
		cadash_vh_screenrefresh,
	
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
	
	static RomLoadPtr rom_cadash = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );    /* 512k for 68000 code */
		ROM_LOAD16_BYTE( "c21-14",  0x00000, 0x20000, 0x5daf13fb )
		ROM_LOAD16_BYTE( "c21-16",  0x00001, 0x20000, 0xcbaa2e75 )
		ROM_LOAD16_BYTE( "c21-13",  0x40000, 0x20000, 0x6b9e0ee9 )
		ROM_LOAD16_BYTE( "c21-17",  0x40001, 0x20000, 0xbf9a578a )
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "c21-02.9",  0x00000, 0x80000, 0x205883b9 );/* SCR tiles (8 x 8) */
	
		ROM_REGION( 0x80000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD( "c21-01.1",  0x00000, 0x80000, 0x1ff6f39c );/* Sprites (16 x 16) */
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* sound cpu */
		ROM_LOAD( "c21-08.38",   0x00000, 0x04000, 0xdca495a0 );	ROM_CONTINUE(            0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x10000, REGION_SOUND1, 0 );/* empty region */
	
		ROM_REGION( 0x08000, REGION_USER1, 0 );/* 2 machine interface mcu rom ? */
		ROM_LOAD( "c21-07.57",   0x00000, 0x08000, 0xf02292bd );
		ROM_REGION( 0x01000, REGION_USER2, 0 );/* pals ? */
	//	ROM_LOAD( "c21-09",   0x00000, 0x00ada, 0xf02292bd );//	ROM_LOAD( "c21-10",   0x00000, 0x00ada, 0xf02292bd );//	ROM_LOAD( "c21-11-1", 0x00000, 0x00ada, 0xf02292bd );//	ROM_LOAD( "c21-12",   0x00000, 0x00cd5, 0xf02292bd );ROM_END(); }}; 
	
	static RomLoadPtr rom_cadashj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );    /* 512k for 68000 code */
		ROM_LOAD16_BYTE( "c21-04.11",  0x00000, 0x20000, 0xcc22ebe5 )
		ROM_LOAD16_BYTE( "c21-06.15",  0x00001, 0x20000, 0x26e03304 )
		ROM_LOAD16_BYTE( "c21-03.10",  0x40000, 0x20000, 0xc54888ed )
		ROM_LOAD16_BYTE( "c21-05.14",  0x40001, 0x20000, 0x834018d2 )
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "c21-02.9",  0x00000, 0x80000, 0x205883b9 );/* SCR tiles (8 x 8) */
	
		ROM_REGION( 0x80000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD( "c21-01.1",  0x00000, 0x80000, 0x1ff6f39c );/* Sprites (16 x 16) */
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* sound cpu */
		ROM_LOAD( "c21-08.38",   0x00000, 0x04000, 0xdca495a0 );	ROM_CONTINUE(            0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x10000, REGION_SOUND1, 0 );/* empty region */
	
		ROM_REGION( 0x08000, REGION_USER1, 0 );/* 2 machine interface mcu rom ? */
		ROM_LOAD( "c21-07.57",   0x00000, 0x08000, 0xf02292bd );ROM_END(); }}; 
	
	static RomLoadPtr rom_cadashu = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );    /* 512k for 68000 code */
		ROM_LOAD16_BYTE( "c21-14-2.11",  0x00000, 0x20000, 0xf823d418 )
		ROM_LOAD16_BYTE( "c21-16-2.15",  0x00001, 0x20000, 0x90165577 )
		ROM_LOAD16_BYTE( "c21-13-2.10",  0x40000, 0x20000, 0x92dcc3ae )
		ROM_LOAD16_BYTE( "c21-15-2.14",  0x40001, 0x20000, 0xf915d26a )
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	// bad dump so used checksum from other sets //
		ROM_LOAD( "c21-02.9",  0x00000, 0x80000, 0x205883b9 );/* SCR tiles (8 x 8) */
	
		ROM_REGION( 0x80000, REGION_GFX2, ROMREGION_DISPOSE );	// bad dump so used checksum from other sets //
		ROM_LOAD( "c21-01.1",  0x00000, 0x80000, 0x1ff6f39c );/* Sprites (16 x 16) */
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* sound cpu */
		ROM_LOAD( "c21-08.38",   0x00000, 0x04000, 0xdca495a0 );	ROM_CONTINUE(            0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x10000, REGION_SOUND1, 0 );/* empty region */
	
		ROM_REGION( 0x08000, REGION_USER1, 0 );/* 2 machine interface mcu rom ? */
		ROM_LOAD( "c21-07.57",   0x00000, 0x08000, 0xf02292bd );ROM_END(); }}; 
	
	static RomLoadPtr rom_cadashi = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );    /* 512k for 68000 code */
		ROM_LOAD16_BYTE( "c21-14it",  0x00000, 0x20000, 0xd1d9e613 )
		ROM_LOAD16_BYTE( "c21-16it",  0x00001, 0x20000, 0x142256ef )
		ROM_LOAD16_BYTE( "c21-13it",  0x40000, 0x20000, 0xc9cf6e30 )
		ROM_LOAD16_BYTE( "c21-17it",  0x40001, 0x20000, 0x641fc9dd )
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "c21-02.9",  0x00000, 0x80000, 0x205883b9 );/* SCR tiles (8 x 8) */
	
		ROM_REGION( 0x80000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD( "c21-01.1",  0x00000, 0x80000, 0x1ff6f39c );/* Sprites (16 x 16) */
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* sound cpu */
		ROM_LOAD( "c21-08.38",   0x00000, 0x04000, 0xdca495a0 );	ROM_CONTINUE(            0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x10000, REGION_SOUND1, 0 );/* empty region */
	
		ROM_REGION( 0x08000, REGION_USER1, 0 );/* 2 machine interface mcu rom ? */
		ROM_LOAD( "c21-07.57",   0x00000, 0x08000, 0xf02292bd );ROM_END(); }}; 
	
	static RomLoadPtr rom_cadashf = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );    /* 512k for 68000 code */
		ROM_LOAD16_BYTE( "c21-19",  0x00000, 0x20000, 0x4d70543b )
		ROM_LOAD16_BYTE( "c21-21",  0x00001, 0x20000, 0x0e5b9950 )
		ROM_LOAD16_BYTE( "c21-18",  0x40000, 0x20000, 0x8a19e59b )
		ROM_LOAD16_BYTE( "c21-20",  0x40001, 0x20000, 0xb96acfd9 )
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "c21-02.9",  0x00000, 0x80000, 0x205883b9 );/* SCR tiles (8 x 8) */
	
		ROM_REGION( 0x80000, REGION_GFX2, ROMREGION_DISPOSE );	ROM_LOAD( "c21-01.1",  0x00000, 0x80000, 0x1ff6f39c );/* Sprites (16 x 16) */
	
		ROM_REGION( 0x1c000, REGION_CPU2, 0 );/* sound cpu */
		ROM_LOAD( "c21-08.38",   0x00000, 0x04000, 0xdca495a0 );	ROM_CONTINUE(            0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x10000, REGION_SOUND1, 0 );/* empty region */
	
		ROM_REGION( 0x08000, REGION_USER1, 0 );/* 2 machine interface mcu rom ? */
		ROM_LOAD( "c21-07.57",   0x00000, 0x08000, 0xf02292bd );ROM_END(); }}; 
	
	
	/* Working games */
	
	/*    year  rom       parent    machine   inp       init */
	public static GameDriver driver_cadash	   = new GameDriver("1989"	,"cadash"	,"cadash.java"	,rom_cadash,null	,machine_driver_cadash	,input_ports_cadash	,null	,ROT0	,	"Taito Corporation Japan", "Cadash (World)" )
	public static GameDriver driver_cadashj	   = new GameDriver("1989"	,"cadashj"	,"cadash.java"	,rom_cadashj,driver_cadash	,machine_driver_cadash	,input_ports_cadashj	,null	,ROT0	,	"Taito Corporation", "Cadash (Japan)" )
	public static GameDriver driver_cadashu	   = new GameDriver("1989"	,"cadashu"	,"cadash.java"	,rom_cadashu,driver_cadash	,machine_driver_cadash	,input_ports_cadashu	,null	,ROT0	,	"Taito America Corporation", "Cadash (US)" )
	public static GameDriver driver_cadashi	   = new GameDriver("1989"	,"cadashi"	,"cadash.java"	,rom_cadashi,driver_cadash	,machine_driver_cadash	,input_ports_cadash	,null	,ROT0	,	"Taito Corporation Japan", "Cadash (Italy)" )
	public static GameDriver driver_cadashf	   = new GameDriver("1989"	,"cadashf"	,"cadash.java"	,rom_cadashf,driver_cadash	,machine_driver_cadash	,input_ports_cadash	,null	,ROT0	,	"Taito Corporation Japan", "Cadash (France)" )
	
}
