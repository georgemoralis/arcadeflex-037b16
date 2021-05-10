/*****************************************************************************

	Irem M90 system games:

	Hasamu
	Bomberman

	Uses M72 sound hardware.

	Emulation by Bryan McPhail, mish@tendril.co.uk, thanks to Chris Hardy!

*****************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class m90
{
	
	
	extern UBytePtr m90_video_data;
	
	
	public static WriteHandlerPtr m90_coincounter_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset==0) {
			coin_counter_w.handler(0,data & 0x01);
			coin_counter_w.handler(1,data & 0x02);
	
			if ((data & 0xfe) != 0) logerror("Coin counter %02x\n",data);
		}
	} };
	
	/***************************************************************************/
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x00000, 0x3ffff, MRA_ROM ),
		new Memory_ReadAddress( 0x60000, 0x60fff, MRA_RAM ),
		new Memory_ReadAddress( 0xa0000, 0xa3fff, MRA_RAM ),
		new Memory_ReadAddress( 0xd0000, 0xdffff, MRA_RAM ),
		new Memory_ReadAddress( 0xe0000, 0xe03ff, paletteram_r ),
		new Memory_ReadAddress( 0xffff0, 0xfffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x00000, 0x3ffff, MWA_ROM ),
		new Memory_WriteAddress( 0xa0000, 0xa3fff, MWA_RAM ),
		new Memory_WriteAddress( 0xd0000, 0xdffff, m90_video_w, &m90_video_data ),
		new Memory_WriteAddress( 0xe0000, 0xe03ff, paletteram_xBBBBBGGGGGRRRRR_w, &paletteram ),
		new Memory_WriteAddress( 0xffff0, 0xfffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_ReadAddress bootleg_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x00000, 0x3ffff, MRA_ROM ),
		new Memory_ReadAddress( 0x60000, 0x60fff, MRA_RAM ),
		new Memory_ReadAddress( 0xa0000, 0xa3fff, MRA_RAM ),
		new Memory_ReadAddress( 0xd0000, 0xdffff, MRA_RAM ),
		new Memory_ReadAddress( 0xe0000, 0xe03ff, paletteram_r ),
		new Memory_ReadAddress( 0xffff0, 0xfffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress bootleg_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x00000, 0x3ffff, MWA_ROM ),
		new Memory_WriteAddress( 0x6000e, 0x60fff, MWA_RAM, &spriteram ),
		new Memory_WriteAddress( 0xa0000, 0xa3fff, MWA_RAM ),
		//new Memory_WriteAddress( 0xd0000, 0xdffff, m90_bootleg_video_w, &m90_video_data ),
		new Memory_WriteAddress( 0xe0000, 0xe03ff, paletteram_xBBBBBGGGGGRRRRR_w, &paletteram ),
		new Memory_WriteAddress( 0xffff0, 0xfffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	public static IO_ReadPort readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),	new IO_ReadPort( 0x00, 0x00, input_port_0_r ), /* Player 1 */
		new IO_ReadPort( 0x01, 0x01, input_port_1_r ), /* Player 2 */
		new IO_ReadPort( 0x02, 0x02, input_port_2_r ), /* Coins */
		new IO_ReadPort( 0x03, 0x03, input_port_2_r ), /* Unused?  High byte of above */
		new IO_ReadPort( 0x04, 0x04, input_port_5_r ), /* Dip 1 */
		new IO_ReadPort( 0x05, 0x05, input_port_6_r ), /* Dip 2 */
		new IO_ReadPort( 0x06, 0x06, input_port_3_r ), /* Player 3 */
		new IO_ReadPort( 0x07, 0x07, input_port_4_r ), /* Player 4 */
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	public static IO_WritePort writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),	new IO_WritePort( 0x00, 0x01, m72_sound_command_w ),
		new IO_WritePort( 0x02, 0x03, m90_coincounter_w ),
		new IO_WritePort( 0x80, 0x8f, m90_video_control_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	/*****************************************************************************/
	
	public static Memory_ReadAddress sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0xffff, MRA_RAM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0xffff, MWA_RAM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	public static IO_ReadPort sound_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),	new IO_ReadPort( 0x01, 0x01, YM2151_status_port_0_r ),
		new IO_ReadPort( 0x80, 0x80, soundlatch_r ),
		new IO_ReadPort( 0x84, 0x84, m72_sample_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	public static IO_WritePort sound_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),	new IO_WritePort( 0x00, 0x00, YM2151_register_port_0_w ),
		new IO_WritePort( 0x01, 0x01, YM2151_data_port_0_w ),
		new IO_WritePort( 0x80, 0x81, rtype2_sample_addr_w ),
		new IO_WritePort( 0x82, 0x82, m72_sample_w ),
		new IO_WritePort( 0x83, 0x83, m72_sound_irq_ack_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	/*****************************************************************************/
	
	static InputPortPtr input_ports_m90 = new InputPortPtr(){ public void handler() { 
	PORT_START(); 
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_START(); 
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_START(); 
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );//service?
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_START(); 
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
		PORT_START(); 
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );
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
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
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
		8,8,	/* 8*8 characters */
		RGN_FRAC(1,4),
		4,
		new int[] { RGN_FRAC(0,4), RGN_FRAC(1,4), RGN_FRAC(2,4), RGN_FRAC(3,4) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 8*8 characters */
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
		new int[] { 60 }
	);
	
	public static InterruptPtr m90_interrupt = new InterruptPtr() { public int handler() 
	{
		return 0x60/4;
	} };
	
	static MachineDriver machine_driver_m90 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_V30,
				32000000/2,	/* 16 MHz */
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
		},60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
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
		0,0,0,0, /* Mono */
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
	
	static MachineDriver machine_driver_bootleg = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_V30,
				32000000/2,	/* 16 MHz */
				bootleg_readmem,bootleg_writemem,readport,writeport,
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
		320, 240, new rectangle( 0, 319, 0, 239 ),
	
		gfxdecodeinfo,
		512,512,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		m90_vh_start,
		null,
		m90_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0, /* Mono */
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
	
	/***************************************************************************/
	
	static RomLoadPtr rom_hasamu = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000 * 2, REGION_CPU1, 0 );	ROM_LOAD16_BYTE( "hasc-p1.bin",    0x00001, 0x20000, 0x53df9834 )
		ROM_LOAD16_BYTE( "hasc-p0.bin",    0x00000, 0x20000, 0xdff0ba6e )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "hasc-sp.bin",    0x0000, 0x10000, 0x259b1687 );
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "hasc-c0.bin",    0x000000, 0x20000, 0xdd5a2174 );	ROM_LOAD( "hasc-c1.bin",    0x020000, 0x20000, 0x76b8217c );	ROM_LOAD( "hasc-c2.bin",    0x040000, 0x20000, 0xd90f9a68 );	ROM_LOAD( "hasc-c3.bin",    0x060000, 0x20000, 0x6cfe0d39 );
		ROM_REGION( 0x20000, REGION_SOUND1, 0 );/* samples */
		/* No samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_bombrman = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000 * 2, REGION_CPU1, 0 );	ROM_LOAD16_BYTE( "bbm-p1.bin",    0x00001, 0x20000, 0x982bd166 )
		ROM_LOAD16_BYTE( "bbm-p0.bin",    0x00000, 0x20000, 0x0a20afcc )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "bbm-sp.bin",    0x0000, 0x10000, 0x251090cd );
		ROM_REGION( 0x100000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "bbm-c3.bin",    0x000000, 0x40000, 0x3c3613af );	ROM_LOAD( "bbm-c2.bin",    0x040000, 0x40000, 0x0700d406 );	ROM_LOAD( "bbm-c1.bin",    0x080000, 0x40000, 0x4c7c8bbc );	ROM_LOAD( "bbm-c0.bin",    0x0c0000, 0x40000, 0x695d2019 );
		ROM_REGION( 0x20000, REGION_SOUND1, 0 );/* samples */
		ROM_LOAD( "bbm-v0.bin",    0x0000, 0x20000, 0x0fa803fe );ROM_END(); }}; 
	
	static RomLoadPtr rom_dynablsb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1, 0 );	ROM_LOAD16_BYTE( "db2-26.bin",    0x00001, 0x20000, 0xa78c72f8 )
		ROM_LOAD16_BYTE( "db3-25.bin",    0x00000, 0x20000, 0xbf3137c3 )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "db1-17.bin",    0x0000, 0x10000, 0xe693c32f );
		ROM_REGION( 0x100000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "bbm-c3.bin",    0x000000, 0x40000, 0x3c3613af );	ROM_LOAD( "bbm-c2.bin",    0x040000, 0x40000, 0x0700d406 );	ROM_LOAD( "bbm-c1.bin",    0x080000, 0x40000, 0x4c7c8bbc );	ROM_LOAD( "bbm-c0.bin",    0x0c0000, 0x40000, 0x695d2019 );
		ROM_REGION( 0x20000, REGION_SOUND1, 0 );/* samples */
		/* Does this have a sample rom? */
	ROM_END(); }}; 
	
	
	static public static InitDriverPtr init_m90 = new InitDriverPtr() { public void handler() 
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		memcpy(RAM+0xffff0,RAM+0x3fff0,0x10); /* Start vector */
	} };
	
	static public static InitDriverPtr init_hasamu = new InitDriverPtr() { public void handler() 
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		memcpy(RAM+0xffff0,RAM+0x3fff0,0x10); /* Start vector */
		irem_cpu_decrypt(0,gunforce_decryption_table);
	} };
	
	static public static InitDriverPtr init_bombrman = new InitDriverPtr() { public void handler() 
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		memcpy(RAM+0xffff0,RAM+0x3fff0,0x10); /* Start vector */
		irem_cpu_decrypt(0,bomberman_decryption_table);
	} };
	
	public static GameDriver driver_hasamu	   = new GameDriver("1991"	,"hasamu"	,"m90.java"	,rom_hasamu,null	,machine_driver_m90	,input_ports_m90	,init_hasamu	,ROT0	,	"Irem", "Hasamu (Japan)" )
	public static GameDriver driver_bombrman	   = new GameDriver("1992"	,"bombrman"	,"m90.java"	,rom_bombrman,null	,machine_driver_m90	,input_ports_m90	,init_bombrman	,ROT0	,	"Irem (licensed from Hudson Soft)", "Bomberman (Japan)" )
	public static GameDriver driver_dynablsb	   = new GameDriver("1992"	,"dynablsb"	,"m90.java"	,rom_dynablsb,driver_bombrman	,machine_driver_bootleg	,input_ports_m90	,init_m90	,ROT0	,	"bootleg", "Dynablaster (bootleg)" )
}
