/***************************************************************************

						  -= ESD 16 Bit Games =-

					driver by	Luca Elia (l.elia@tin.it)


Main  CPU	:	M68000
Video Chips	:	2 x ACTEL A40MX04 (84 Pin Square Socketed)

Sound CPU	:	Z80
Sound Chips	:	M6295 (AD-65)  +  YM3812 (U6612)  +  YM3014 (U6614)

---------------------------------------------------------------------------
Year + Game			PCB				Notes
---------------------------------------------------------------------------
98	Multi Champ		ESD 11-09-98
---------------------------------------------------------------------------

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class esd16
{
	
	/* Variables defined in vidhrdw: */
	
	extern data16_t *esd16_vram_0, *esd16_scroll_0;
	extern data16_t *esd16_vram_1, *esd16_scroll_1;
	
	/* Functions defined in vidhrdw: */
	
	WRITE16_HANDLER( esd16_vram_0_w );
	WRITE16_HANDLER( esd16_vram_1_w );
	
	
	
	/***************************************************************************
	
	
								Memory Maps - Main CPU
	
	
	***************************************************************************/
	
	WRITE16_HANDLER( esd16_spriteram_w ) {	COMBINE_DATA(&spriteram16[offset]);	}
	
	WRITE16_HANDLER( esd16_flip_screen_w )
	{
		if (ACCESSING_LSB && !(data & 0x7e))
		{
			flip_screen_set( data & 0x80 );
			//               data & 0x01 ?? always 1
		}
		else	logerror("CPU #0 - PC %06X: unknown flip screen bits: %02X\n",cpu_get_pc(),data);
	}
	
	WRITE16_HANDLER( esd16_sound_command_w )
	{
		if (ACCESSING_LSB != 0)
		{
			soundlatch_w(0,data & 0xff);
			cpu_set_irq_line(1,0,ASSERT_LINE);		// Generate an IRQ
			cpu_spinuntil_time(TIME_IN_USEC(50));	// Allow the other CPU to reply
		}
	}
	
	/*
	 Lines starting with an empty comment in the following MemoryReadAddress
	 arrays are there for debug (e.g. the game does not read from those ranges
	 AFAIK)
	*/
	
	static MEMORY_READ16_START( multchmp_readmem )
		{ 0x000000, 0x07ffff, MRA16_ROM				},	// ROM
		{ 0x100000, 0x10ffff, MRA16_RAM				},	// RAM
		{ 0x200000, 0x2005ff, MRA16_RAM				},	// Palette
	/**/{ 0x300000, 0x3007ff, MRA16_RAM				},	// Sprites
	/**/{ 0x400000, 0x403fff, MRA16_RAM				},	// Layers
	/**/{ 0x420000, 0x423fff, MRA16_RAM				},	//
	/**/{ 0x500000, 0x500003, MRA16_RAM				},	// Scroll
	/**/{ 0x500004, 0x500007, MRA16_RAM				},	//
	/**/{ 0x500008, 0x50000b, MRA16_RAM				},	//
	/**/{ 0x50000c, 0x50000f, MRA16_RAM				},	//
		{ 0x600002, 0x600003, input_port_0_word_r	},	// Inputs
		{ 0x600004, 0x600005, input_port_1_word_r	},	//
		{ 0x600006, 0x600007, input_port_2_word_r	},	//
		{ 0x700008, 0x70000b, MRA16_NOP				},	// ? Only read once
	MEMORY_END
	
	static MEMORY_WRITE16_START( multchmp_writemem )
		{ 0x000000, 0x07ffff, MWA16_ROM						},	// ROM
		{ 0x100000, 0x10ffff, MWA16_RAM						},	// RAM
		{ 0x200000, 0x2005ff, paletteram16_xRRRRRGGGGGBBBBB_word_w, &paletteram16	},	// Palette
		{ 0x300000, 0x3007ff, MWA16_RAM, &spriteram16, &spriteram_size	},	// Sprites
		{ 0x300800, 0x300807, esd16_spriteram_w				},	// Sprites (Mirrored)
		{ 0x400000, 0x403fff, esd16_vram_0_w, &esd16_vram_0	},	// Layers
		{ 0x420000, 0x423fff, esd16_vram_1_w, &esd16_vram_1	},	// Scroll
		{ 0x500000, 0x500003, MWA16_RAM, &esd16_scroll_0	},	//
		{ 0x500004, 0x500007, MWA16_RAM, &esd16_scroll_1	},	//
		{ 0x500008, 0x50000b, MWA16_RAM						},	// ? 0
		{ 0x50000c, 0x50000f, MWA16_RAM						},	// ? 0
		{ 0x600000, 0x600001, MWA16_NOP						},	// IRQ Ack
		{ 0x600008, 0x600009, esd16_flip_screen_w			},	// Flip Screen + ?
		{ 0x60000a, 0x60000b, MWA16_NOP						},	// ? 2
		{ 0x60000c, 0x60000d, esd16_sound_command_w			},	// To Sound CPU
	MEMORY_END
	
	
	/***************************************************************************
	
	
								Memory Maps - Sound CPU
	
	
	***************************************************************************/
	
	public static WriteHandlerPtr esd16_sound_rombank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int bank = data & 0x7;
		if (data != bank)	logerror("CPU #1 - PC %04X: unknown bank bits: %02X\n",cpu_get_pc(),data);
		if (bank >= 3)	bank += 1;
		cpu_setbank(1, memory_region(REGION_CPU2) + 0x4000 * bank);
	} };
	
	public static Memory_ReadAddress multchmp_sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM		),	// ROM
		new Memory_ReadAddress( 0x8000, 0xbfff, MRA_BANK1		),	// Banked ROM
		new Memory_ReadAddress( 0xf800, 0xffff, MRA_RAM		),	// RAM
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress multchmp_sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM		),	// ROM
		new Memory_WriteAddress( 0x8000, 0xbfff, MWA_ROM		),	// Banked ROM
		new Memory_WriteAddress( 0xf800, 0xffff, MWA_RAM		),	// RAM
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	public static ReadHandlerPtr esd16_sound_command_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* Clear IRQ only after reading the command, or some get lost */
		cpu_set_irq_line(1,0,CLEAR_LINE);
		return soundlatch_r(0);
	} };
	
	public static IO_ReadPort multchmp_sound_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),	new IO_ReadPort( 0x02, 0x02, OKIM6295_status_0_r		),	// M6295
		new IO_ReadPort( 0x03, 0x03, esd16_sound_command_r		),	// From Main CPU
		new IO_ReadPort( 0x06, 0x06, IORP_NOP					),	// ? At the start
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	public static IO_WritePort multchmp_sound_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),	new IO_WritePort( 0x00, 0x00, YM3812_control_port_0_w	),	// YM3812
		new IO_WritePort( 0x01, 0x01, YM3812_write_port_0_w		),
		new IO_WritePort( 0x02, 0x02, OKIM6295_data_0_w			),	// M6295
		new IO_WritePort( 0x04, 0x04, IOWP_NOP					),	// ? $00, $30
		new IO_WritePort( 0x05, 0x05, esd16_sound_rombank_w 	),	// ROM Bank
		new IO_WritePort( 0x06, 0x06, IOWP_NOP					),	// ? 1 (End of NMI routine)
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	/***************************************************************************
	
	
									Input Ports
	
	
	***************************************************************************/
	
	static InputPortPtr input_ports_multchmp = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	// IN0 - $600002.w
		PORT_BIT(  0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER1 );	PORT_BIT(  0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );	PORT_BIT(  0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );	PORT_BIT(  0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );	PORT_BIT(  0x0010, IP_ACTIVE_LOW, IPT_BUTTON1        | IPF_PLAYER1 );	PORT_BIT(  0x0020, IP_ACTIVE_LOW, IPT_BUTTON2        | IPF_PLAYER1 );	PORT_BIT(  0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT(  0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER2 );	PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );	PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );	PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );	PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_BUTTON1        | IPF_PLAYER2 );	PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_BUTTON2        | IPF_PLAYER2 );	PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );// Resets the test mode
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	// IN1 - $600005.b
		PORT_BIT(  0x0001, IP_ACTIVE_LOW, IPT_COIN1   );	PORT_BIT(  0x0002, IP_ACTIVE_LOW, IPT_COIN2   );	PORT_BIT(  0x0004, IP_ACTIVE_LOW, IPT_START1  );	PORT_BIT(  0x0008, IP_ACTIVE_LOW, IPT_START2  );	PORT_BIT(  0x0010, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT(  0x0020, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT(  0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT(  0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT(  0xff00, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	// IN2 - $600006.w
		PORT_SERVICE( 0x0001, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x0002, 0x0002, "Coinage Type" );// Not Supported
		PORT_DIPSETTING(      0x0002, "1" );//	PORT_DIPSETTING(      0x0000, "2" );	PORT_DIPNAME( 0x0004, 0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(      0x0004, DEF_STR( "No") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x0008, 0x0008, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(      0x0008, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0030, 0x0030, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(      0x0010, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0030, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(      0x0020, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x00c0, 0x00c0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(      0x0040, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x00c0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(      0x0080, DEF_STR( "1C_2C") );
	
	//	PORT_DIPNAME( 0x0300, 0x0300, DEF_STR( "Difficulty")" );	CRASH CPP??
		PORT_DIPNAME( 0x0300, 0x0300, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(      0x0200, "Easy" );	PORT_DIPSETTING(      0x0300, "Normal" );	PORT_DIPSETTING(      0x0100, "Hard" );	PORT_DIPSETTING(      0x0000, "Hardest" );	PORT_DIPNAME( 0x0c00, 0x0c00, DEF_STR( "Lives") );
		PORT_DIPSETTING(      0x0000, "2" );	PORT_DIPSETTING(      0x0c00, "3" );	PORT_DIPSETTING(      0x0800, "4" );	PORT_DIPSETTING(      0x0400, "5" );	PORT_DIPNAME( 0x1000, 0x1000, "Selectable Games" );	PORT_DIPSETTING(      0x1000, "3" );	PORT_DIPSETTING(      0x0000, "4" );	PORT_DIPNAME( 0x2000, 0x2000, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(      0x2000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x4000, 0x4000, "Unknown 2-6" );// unused
		PORT_DIPSETTING(      0x4000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x8000, 0x8000, "Unknown 2-7" );// unused
		PORT_DIPSETTING(      0x8000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	/***************************************************************************
	
	
								Graphics Layouts
	
	
	***************************************************************************/
	
	/* 16x16x5, made of four 8x8 tiles */
	static GfxLayout layout_16x16x5 = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,5),
		5,
		new int[] { RGN_FRAC(4,5),RGN_FRAC(3,5),RGN_FRAC(2,5),RGN_FRAC(1,5), RGN_FRAC(0,5) },
		new int[] { STEP8(0+7,-1), STEP8(8*16+7,-1) },
		new int[] { STEP16(0,8) },
		16*16
	);
	
	/* 8x8x8 */
	static GfxLayout layout_8x8x8 = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,4),
		8,
		new int[] { STEP8(0,1) },
		new int[] { RGN_FRAC(3,4)+0*8,RGN_FRAC(2,4)+0*8,RGN_FRAC(1,4)+0*8,RGN_FRAC(0,4)+0*8,
		  RGN_FRAC(3,4)+1*8,RGN_FRAC(2,4)+1*8,RGN_FRAC(1,4)+1*8,RGN_FRAC(0,4)+1*8 },
		new int[] { STEP8(0,2*8) },
		8*8*2,
	);
	
	static GfxDecodeInfo esd16_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, layout_16x16x5, 0x200, 8 ), // [0] Sprites
		new GfxDecodeInfo( REGION_GFX2, 0, layout_8x8x8,   0x000, 2 ), // [1] Layers
		new GfxDecodeInfo( -1 )
	};
	
	
	/***************************************************************************
	
	
									Machine Drivers
	
	
	***************************************************************************/
	
	static YM3812interface esd16_ym3812_intf = new YM3812interface
	(
		1,
		4000000,	/* ? */
		new int[] { 20 },
		new WriteYmHandlerPtr[] {  0 },		/* IRQ Line */
	);
	
	static OKIM6295interface esd16_m6295_intf = new OKIM6295interface
	(
		1,
		new int[] { 8000 },	/* ? */
		new int[] { REGION_SOUND1 },
		new int[] { 80 }
	);
	
	static MachineDriver machine_driver_multchmp = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				16000000,
				multchmp_readmem, multchmp_writemem,null,null,
				m68_level6_irq, 1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				4000000,	/* ? */
				multchmp_sound_readmem,  multchmp_sound_writemem,
				multchmp_sound_readport, multchmp_sound_writeport,
				nmi_interrupt, 32	/* IRQ By Main CPU */
			),
		},
		60,DEFAULT_60HZ_VBLANK_DURATION,
		1,
		null,
	
		/* video hardware */
		0x140, 0x100, new rectangle( 0, 0x140-1, 0+8, 0x100-8-1 ),
		esd16_gfxdecodeinfo,
		256*3, 256*3,
		null,
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		esd16_vh_start,
		null,
		esd16_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound( SOUND_YM3812,   esd16_ym3812_intf ),
			new MachineSound( SOUND_OKIM6295, esd16_m6295_intf  )
		},
	);
	
	
	/***************************************************************************
	
	
									ROMs Loading
	
	
	***************************************************************************/
	
	/***************************************************************************
	
									Multi Champ
	
	(C) ESD 1998
	PCB No. ESD 11-09-98    (Probably the manufacture date)
	CPU: MC68HC000FN16 (68000, 68 pin square socketed)
	SND: Z80, U6612 (Probably YM3812), AD-65 (OKI 6295), U6614 (YM3014)
	OSC: 16.000MHz, 14.000MHz
	RAM: 4 x 62256, 9 x 6116
	DIPS: 2 x 8 position
	Dip info is in Japanese! I will scan and make it available on my site for translation.
	
	Other Chips: 2 x ACTEL A40MX04 (84 pin square socketed)
	8 PAL's (not dumped)
	
	ROMS:
	
	MULTCHMP.U02  \   Main Program     MX27C2000
	MULTCHMP.U03  /                    MX27C2000
	MULTCHMP.U06   -- Sound Program    27C010
	MULTCHMP.U10   -- ADPCM Samples ?  27C010
	MULTCHMP.U27 -\                    27C4001
	MULTCHMP.U28   \                   27C4001
	MULTCHMP.U29    |                  27C4001
	MULTCHMP.U30    |                  27C4001
	MULTCHMP.U31    |                  27C4001
	MULTCHMP.U32    |                  27C4001
	MULTCHMP.U33    +- GFX             27C4001
	MULTCHMP.U34    |                  27C4001
	MULTCHMP.U35    |                  MX27C2000
	MULTCHMP.U36    |                  MX27C2000
	MULTCHMP.U37    |                  MX27C2000
	MULTCHMP.U38   /                   MX27C2000
	MULTCHMP.U39 -/                    MX27C2000
	
	***************************************************************************/
	
	static RomLoadPtr rom_multchmp = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x080000, REGION_CPU1, 0 );	/* 68000 Code */
		ROM_LOAD16_BYTE( "multchmp.u02", 0x000000, 0x040000, 0x7da8c0df )
		ROM_LOAD16_BYTE( "multchmp.u03", 0x000001, 0x040000, 0x5dc62799 )
	
		ROM_REGION( 0x24000, REGION_CPU2, 0 );	/* Z80 Code */
		ROM_LOAD( "multchmp.u06", 0x00000, 0x0c000, 0x7c178bd7 );	ROM_CONTINUE(             0x10000, 0x14000             );
		ROM_REGION( 0x140000, REGION_GFX1, ROMREGION_DISPOSE );/* Sprites, 16x16x5 */
		ROM_LOAD( "multchmp.u36", 0x000000, 0x040000, 0xd8f06fa8 );	ROM_LOAD( "multchmp.u37", 0x040000, 0x040000, 0xb1ae7f08 );	ROM_LOAD( "multchmp.u38", 0x080000, 0x040000, 0x88e252e8 );	ROM_LOAD( "multchmp.u39", 0x0c0000, 0x040000, 0x51f01067 );	ROM_LOAD( "multchmp.u35", 0x100000, 0x040000, 0x9d1590a6 );
		ROM_REGION( 0x400000, REGION_GFX2, ROMREGION_DISPOSE );/* Layers, 16x16x8 */
		ROM_LOAD( "multchmp.u27", 0x000000, 0x080000, 0xdc42704e );	ROM_LOAD( "multchmp.u28", 0x080000, 0x080000, 0x449991fa );	ROM_LOAD( "multchmp.u33", 0x100000, 0x080000, 0xe4c0ec96 );	ROM_LOAD( "multchmp.u34", 0x180000, 0x080000, 0xbffaaccc );	ROM_LOAD( "multchmp.u29", 0x200000, 0x080000, 0x01bd1399 );	ROM_LOAD( "multchmp.u30", 0x280000, 0x080000, 0xc6b4cc18 );	ROM_LOAD( "multchmp.u31", 0x300000, 0x080000, 0xb1e4e9e3 );	ROM_LOAD( "multchmp.u32", 0x380000, 0x080000, 0xf05cb5b4 );
		ROM_REGION( 0x20000, REGION_SOUND1, ROMREGION_SOUNDONLY );/* Samples */
		ROM_LOAD( "multchmp.u10", 0x00000, 0x20000, 0x6e741fcd );ROM_END(); }}; 
	
	
	/***************************************************************************
	
	
									Game Drivers
	
	
	***************************************************************************/
	
	public static GameDriver driver_multchmp	   = new GameDriver("1998"	,"multchmp"	,"esd16.java"	,rom_multchmp,null	,machine_driver_multchmp	,input_ports_multchmp	,null	,ROT0_16BIT	,	"ESD", "Multi Champ (Korea)" )
}
