/*****************************************************************************

	Irem M97 system games:

	Bomberman World / Atomic Punk
	Quiz F-1 1,2finish
	Risky Challenge / Gussun Oyoyo
	Shisensho 2

	Uses M72 sound hardware.

	Emulation by Bryan McPhail, mish@tendril.co.uk, thanks to Chris Hardy!

*****************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class m97
{
	
	
	extern UBytePtr m90_video_data;
	
	/***************************************************************************/
	
	public static WriteHandlerPtr m97_coincounter_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		coin_counter_w.handler(0,data & 0x01);
		coin_counter_w.handler(1,data & 0x02);
	
		if ((data & 0xfe) != 0) logerror("Coin counter %02x\n",data);
	} };
	
	/***************************************************************************/
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x00000, 0x7ffff, MRA_ROM ),
		new Memory_ReadAddress( 0xa0000, 0xa3fff, MRA_RAM ),
		new Memory_ReadAddress( 0xd0000, 0xdffff, MRA_RAM ),
		new Memory_ReadAddress( 0xe0000, 0xe03ff, paletteram_r ),
		new Memory_ReadAddress( 0xffff0, 0xfffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x00000, 0x7ffff, MWA_ROM ),
		new Memory_WriteAddress( 0xd0000, 0xdffff, m90_video_w, &m90_video_data ),
		new Memory_WriteAddress( 0xa0000, 0xa3fff, MWA_RAM ),
		new Memory_WriteAddress( 0xe0000, 0xe03ff, paletteram_xBBBBBGGGGGRRRRR_w, &paletteram ),
		new Memory_WriteAddress( 0xffff0, 0xfffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	public static IO_ReadPort readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),	new IO_ReadPort( 0x00, 0x00, input_port_0_r ), /* Player 1 */
		new IO_ReadPort( 0x01, 0x01, input_port_1_r ), /* Player 2 */
		new IO_ReadPort( 0x02, 0x02, input_port_2_r ), /* Coins */
		new IO_ReadPort( 0x03, 0x03, input_port_5_r ), /* Dip 1 */
		new IO_ReadPort( 0x04, 0x04, input_port_6_r ), /* Dip 2 */
		new IO_ReadPort( 0x05, 0x05, input_port_7_r ), /* Dip 3 */
		new IO_ReadPort( 0x06, 0x06, input_port_3_r ), /* Player 3 */
		new IO_ReadPort( 0x07, 0x07, input_port_4_r ), /* Player 4 */
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	public static IO_WritePort writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),	new IO_WritePort( 0x00, 0x00, m72_sound_command_w ),
		new IO_WritePort( 0x02, 0x02, m97_coincounter_w ),
		new IO_WritePort( 0x03, 0x03, IOWP_NOP ),
		new IO_WritePort( 0x80, 0x8f, m90_video_control_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	/*****************************************************************************/
	
	public static Memory_ReadAddress sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0xefff, MRA_ROM ),
		new Memory_ReadAddress( 0xf000, 0xffff, MRA_RAM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0xefff, MWA_ROM ),
		new Memory_WriteAddress( 0xf000, 0xffff, MWA_RAM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	public static IO_ReadPort sound_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),	new IO_ReadPort( 0x41, 0x41, YM2151_status_port_0_r ),
		new IO_ReadPort( 0x42, 0x42, soundlatch_r ),
	//	new IO_ReadPort( 0x41, 0x41, m72_sample_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	public static IO_WritePort sound_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),	new IO_WritePort( 0x40, 0x40, YM2151_register_port_0_w ),
		new IO_WritePort( 0x41, 0x41, YM2151_data_port_0_w ),
		new IO_WritePort( 0x42, 0x42, m72_sound_irq_ack_w ),
	//	new IO_WritePort( 0x40, 0x41, rtype2_sample_addr_w ),
	//	new IO_WritePort( 0x42, 0x42, m72_sample_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	/***************************************************************************/
	
	static InputPortPtr input_ports_m97 = new InputPortPtr(){ public void handler() { 
	PORT_START(); 
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_START(); 
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_START(); 
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START3 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
		PORT_START(); 
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START4 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN4 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );
		PORT_START(); 	/* Dip switch bank 1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "1" );	PORT_DIPSETTING(    0x03, "2" );	PORT_DIPSETTING(    0x02, "3" );	PORT_DIPSETTING(    0x01, "4" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") ); /* Probably difficulty */
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
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
		PORT_START(); 	/* Dip switch bank 2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
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
	
		PORT_START(); 	/* Dip switch bank 3 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
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
	INPUT_PORTS_END(); }}; 
	
	/*****************************************************************************/
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,4),
		4,
		new int[] { RGN_FRAC(0,4), RGN_FRAC(1,4), RGN_FRAC(2,4), RGN_FRAC(3,4) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,4),
		4,
		new int[] { RGN_FRAC(0,4), RGN_FRAC(1,4), RGN_FRAC(2,4), RGN_FRAC(3,4) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
			16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,     0, 16 ),
		new GfxDecodeInfo( REGION_GFX1, 0, spritelayout, 256, 16 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	/*****************************************************************************/
	
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,			/* 1 chip */
		3579545,	/* 3.579545 MHz */
		new int[] { YM3012_VOL(90,MIXER_PAN_LEFT,90,MIXER_PAN_RIGHT) },
		new WriteYmHandlerPtr[] { m72_ym2151_irq_handler },
		new WriteHandlerPtr[] { 0 }
	);
	
	static DACinterface dac_interface = new DACinterface
	(
		1,	/* 1 channel */
		new int[] { 40 }
	);
	
	public static InterruptPtr m90_interrupt = new InterruptPtr() { public int handler() 
	{
		return 0x60/4;
	} };
	
	static MachineDriver machine_driver_m97 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_V30,
				32000000/2,
				readmem,writemem,readport,writeport,
				m90_interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				3579545,	/* 3.579545 MHz */
				sound_readmem,sound_writemem,sound_readport,sound_writeport,
				nmi_interrupt,128	/* clocked by V1? (Vigilante) */
									/* IRQs are generated by main Z80 and YM2151 */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		m72_init_sound,
	
		/* video hardware */
	//	512, 512, new rectangle( 0, 511, 0, 511 ),
		512, 512, { 80, 511-112, 128+8, 511-128-8 }, /* 320 x 240 */
	
		gfxdecodeinfo,
		512,512,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		0,
		m90_vh_start,
		0,
		m90_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0, /* Mono? */
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_bbmanw = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000 * 2, REGION_CPU1, 0 );	ROM_LOAD16_BYTE( "db_h0-b.rom",    0x00001, 0x40000, 0x567d3709 )
		ROM_LOAD16_BYTE( "db_l0-b.rom",    0x00000, 0x40000, 0xe762c22b )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "db_sp.rom",           0x0000, 0x10000, 0x6bc1689e );
		ROM_REGION( 0x200000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "bbm2_c0.bin",  0x180000, 0x40000, 0xe7ce058a );	ROM_LOAD( "bbm2_c1.bin",  0x100000, 0x40000, 0x636a78a9 );	ROM_LOAD( "bbm2_c2.bin",  0x080000, 0x40000, 0x9ac2142f );	ROM_LOAD( "bbm2_c3.bin",  0x000000, 0x40000, 0x47af1750 );
		ROM_REGION( 0x20000, REGION_SOUND1, 0 );	ROM_LOAD( "db_w04m.rom",    0x0000, 0x20000, 0x4ad889ed );ROM_END(); }}; 
	
	static RomLoadPtr rom_bbmanwj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000 * 2, REGION_CPU1, 0 );	ROM_LOAD16_BYTE( "bbm2_h0.bin",    0x00001, 0x40000, 0xe1407b91 )
		ROM_LOAD16_BYTE( "bbm2_l0.bin",    0x00000, 0x40000, 0x20873b49 )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "bbm2sp-b.bin", 0x0000, 0x10000, 0xb8d8108c );
		ROM_REGION( 0x200000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "bbm2_c0.bin",  0x180000, 0x40000, 0xe7ce058a );	ROM_LOAD( "bbm2_c1.bin",  0x100000, 0x40000, 0x636a78a9 );	ROM_LOAD( "bbm2_c2.bin",  0x080000, 0x40000, 0x9ac2142f );	ROM_LOAD( "bbm2_c3.bin",  0x000000, 0x40000, 0x47af1750 );
		ROM_REGION( 0x20000, REGION_SOUND1, 0 );/* samples */
		ROM_LOAD( "bbm2_vo.bin",  0x0000, 0x20000, 0x0ae655ff );ROM_END(); }}; 
	
	static RomLoadPtr rom_atompunk = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000 * 2, REGION_CPU1, 0 );	ROM_LOAD16_BYTE( "bm2-ho-a.9f",  0x00001, 0x40000, 0x7d858682 )
		ROM_LOAD16_BYTE( "bm2-lo-a.9k",  0x00000, 0x40000, 0xc7568031 )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "db_sp.rom",             0x0000, 0x10000, 0x6bc1689e );
		ROM_REGION( 0x200000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "bbm2_c0.bin",  0x180000, 0x40000, 0xe7ce058a );	ROM_LOAD( "bbm2_c1.bin",  0x100000, 0x40000, 0x636a78a9 );	ROM_LOAD( "bbm2_c2.bin",  0x080000, 0x40000, 0x9ac2142f );	ROM_LOAD( "bbm2_c3.bin",  0x000000, 0x40000, 0x47af1750 );
		ROM_REGION( 0x20000, REGION_SOUND1, 0 );/* samples */
		ROM_LOAD( "db_w04m.rom",           0x0000, 0x20000, 0x4ad889ed );ROM_END(); }}; 
	
	static RomLoadPtr rom_quizf1 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x400000 * 2, REGION_CPU1, 0 );//todo
		ROM_LOAD16_BYTE( "qf1-h0-.77",   0x000001, 0x40000, 0x280e3049 )
		ROM_LOAD16_BYTE( "qf1-l0-.79",   0x000000, 0x40000, 0x94588a6f )
		ROM_LOAD16_BYTE( "qf1-h1-.78",   0x100001, 0x80000, 0xc6c2eb2b )	/* banked? */
		ROM_LOAD16_BYTE( "qf1-l1-.80",   0x100000, 0x80000, 0x3132c144 )	/* banked? */
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "qf1-sp-.33",   0x0000, 0x10000, 0x0664fa9f );
		ROM_REGION( 0x200000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "qf1-c0-.81",   0x000000, 0x80000, 0xc26b521e );	ROM_LOAD( "qf1-c1-.82",   0x080000, 0x80000, 0xdb9d7394 );	ROM_LOAD( "qf1-c2-.83",   0x100000, 0x80000, 0x0b1460ae );	ROM_LOAD( "qf1-c3-.84",   0x180000, 0x80000, 0x2d32ff37 );
		ROM_REGION( 0x40000, REGION_SOUND1, 0 );/* samples */
		ROM_LOAD( "qf1-v0-.30",   0x0000, 0x40000, 0xb8d16e7c );ROM_END(); }}; 
	
	static RomLoadPtr rom_riskchal = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000 * 2, REGION_CPU1, 0 );	ROM_LOAD16_BYTE( "rc_h0.rom",    0x00001, 0x40000, 0x4c9b5344 )
		ROM_LOAD16_BYTE( "rc_l0.rom",    0x00000, 0x40000, 0x0455895a )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "rc_sp.rom",    0x0000, 0x10000, 0xbb80094e );
		ROM_REGION( 0x200000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "rc_c0.rom",    0x000000, 0x80000, 0x84d0b907 );	ROM_LOAD( "rc_c1.rom",    0x080000, 0x80000, 0xcb3784ef );	ROM_LOAD( "rc_c2.rom",    0x100000, 0x80000, 0x687164d7 );	ROM_LOAD( "rc_c3.rom",    0x180000, 0x80000, 0xc86be6af );
		ROM_REGION( 0x40000, REGION_SOUND1, 0 );/* samples */
		ROM_LOAD( "rc_v0.rom",    0x0000, 0x40000, 0xcddac360 );ROM_END(); }}; 
	
	static RomLoadPtr rom_gussun = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000 * 2, REGION_CPU1, 0 );	ROM_LOAD16_BYTE( "l4_h0.rom",    0x00001, 0x40000, 0x9d585e61 )
		ROM_LOAD16_BYTE( "l4_l0.rom",    0x00000, 0x40000, 0xc7b4c519 )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "rc_sp.rom",    0x0000, 0x10000, 0xbb80094e );
		ROM_REGION( 0x200000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "rc_c0.rom",    0x000000, 0x80000, 0x84d0b907 );	ROM_LOAD( "rc_c1.rom",    0x080000, 0x80000, 0xcb3784ef );	ROM_LOAD( "rc_c2.rom",    0x100000, 0x80000, 0x687164d7 );	ROM_LOAD( "rc_c3.rom",    0x180000, 0x80000, 0xc86be6af );
		ROM_REGION( 0x40000, REGION_SOUND1, 0 );/* samples */
		ROM_LOAD( "rc_v0.rom",    0x0000, 0x40000, 0xcddac360 );ROM_END(); }}; 
	
	static RomLoadPtr rom_shisen2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000 * 2, REGION_CPU1, 0 );	ROM_LOAD16_BYTE( "sis2-ho-.rom", 0x00001, 0x40000, 0x6fae0aea )
		ROM_LOAD16_BYTE( "sis2-lo-.rom", 0x00000, 0x40000, 0x2af25182 )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "sis2-sp-.rom", 0x0000, 0x10000, 0x6fc0ff3a );
		ROM_REGION( 0x200000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "ic81.rom",     0x000000, 0x80000, 0x5a7cb88f );	ROM_LOAD( "ic82.rom",     0x080000, 0x80000, 0x54a7852c );	ROM_LOAD( "ic83.rom",     0x100000, 0x80000, 0x2bd65dc6 );	ROM_LOAD( "ic84.rom",     0x180000, 0x80000, 0x876d5fdb );ROM_END(); }}; 
	
	
	
	static public static InitDriverPtr init_riskchal = new InitDriverPtr() { public void handler() 
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		memcpy(RAM+0xffff0,RAM+0x7fff0,0x10); /* Start vector */
		irem_cpu_decrypt(0,gussun_decryption_table);
	} };
	
	static public static InitDriverPtr init_shisen2 = new InitDriverPtr() { public void handler() 
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		memcpy(RAM+0xffff0,RAM+0x7fff0,0x10); /* Start vector */
		irem_cpu_decrypt(0,shisen2_decryption_table);
	} };
	
	static public static InitDriverPtr init_quizf1 = new InitDriverPtr() { public void handler() 
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		memcpy(RAM+0xffff0,RAM+0x7fff0,0x10); /* Start vector */
		irem_cpu_decrypt(0,lethalth_decryption_table);
	} };
	
	/* Bomberman World executes encrypted code from RAM! */
	static WRITE_HANDLER (bbmanw_ram_write)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
		RAM[0x0a0c00+offset]=data;
		RAM[0x1a0c00+offset]=dynablaster_decryption_table[data];
	}
	
	static public static InitDriverPtr init_bbmanw = new InitDriverPtr() { public void handler() 
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		memcpy(RAM+0xffff0,RAM+0x7fff0,0x10); /* Start vector */
		irem_cpu_decrypt(0,dynablaster_decryption_table);
	
		install_mem_write_handler(0, 0xa0c00, 0xa0cff, bbmanw_ram_write);
	} };
	
	public static GameDriver driver_bbmanw	   = new GameDriver("1992"	,"bbmanw"	,"m97.java"	,rom_bbmanw,null	,machine_driver_m97	,input_ports_m97	,init_bbmanw	,ROT0	,	"Irem", "Bomber Man World (World)" )
	public static GameDriver driver_bbmanwj	   = new GameDriver("1992"	,"bbmanwj"	,"m97.java"	,rom_bbmanwj,driver_bbmanw	,machine_driver_m97	,input_ports_m97	,init_bbmanw	,ROT0	,	"Irem", "Bomber Man World (Japan)" )
	public static GameDriver driver_atompunk	   = new GameDriver("1992"	,"atompunk"	,"m97.java"	,rom_atompunk,driver_bbmanw	,machine_driver_m97	,input_ports_m97	,init_bbmanw	,ROT0	,	"Irem America", "New Atomic Punk - Global Quest (US)" )
	public static GameDriver driver_quizf1	   = new GameDriver("1992"	,"quizf1"	,"m97.java"	,rom_quizf1,null	,machine_driver_m97	,input_ports_m97	,init_quizf1	,ROT0	,	"Irem", "Quiz F-1 1,2finish", GAME_NOT_WORKING )
	public static GameDriver driver_riskchal	   = new GameDriver("1993"	,"riskchal"	,"m97.java"	,rom_riskchal,null	,machine_driver_m97	,input_ports_m97	,init_riskchal	,ROT0	,	"Irem", "Risky Challenge", GAME_NOT_WORKING )
	public static GameDriver driver_gussun	   = new GameDriver("1993"	,"gussun"	,"m97.java"	,rom_gussun,driver_riskchal	,machine_driver_m97	,input_ports_m97	,init_riskchal	,ROT0	,	"Irem", "Gussun Oyoyo (Japan)", GAME_NOT_WORKING )
	public static GameDriver driver_shisen2	   = new GameDriver("1993"	,"shisen2"	,"m97.java"	,rom_shisen2,null	,machine_driver_m97	,input_ports_m97	,init_shisen2	,ROT0	,	"Tamtex", "Shisensho II", GAME_NOT_WORKING )
}
