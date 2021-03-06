/***************************************************************************

							-= Clash Road =-

					driver by	Luca Elia (l.elia@tin.it)

Main  CPU   :	Z80A

Video Chips :	?

Sound CPU   :	Z80A

Sound Chips :	Custom (NAMCO?)

To Do:

- Colors (is there a color PROM?)
- Sound

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class clshroad
{
	
	data8_t *clshroad_sharedram;
	
	/* Variables & functions defined in vidhrdw: */
	
	extern data8_t *clshroad_vram_0, *clshroad_vram_1;
	extern data8_t *clshroad_vregs;
	
	
	
	
	/***************************************************************************
	
	
										Main CPU
	
	
	***************************************************************************/
	
	/* Shared RAM with the sound CPU */
	
	public static ReadHandlerPtr clshroad_sharedram_r  = new ReadHandlerPtr() { public int handler(int offset)	{	return clshroad_sharedram[offset];	} };
	public static WriteHandlerPtr clshroad_sharedram_w = new WriteHandlerPtr() {public void handler(int offset, int data)	{	clshroad_sharedram[offset] = data;	} };
	
	public static ReadHandlerPtr clshroad_input_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return	((~readinputport(0) & (1 << offset)) ? 1 : 0) |
				((~readinputport(1) & (1 << offset)) ? 2 : 0) |
				((~readinputport(2) & (1 << offset)) ? 4 : 0) |
				((~readinputport(3) & (1 << offset)) ? 8 : 0) ;
	} };
	
	public static Memory_ReadAddress clshroad_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM				),	// ROM
		new Memory_ReadAddress( 0x8000, 0x95ff, MRA_RAM				),	// Work   RAM
		new Memory_ReadAddress( 0x9600, 0x97ff, clshroad_sharedram_r	),	// Shared RAM
		new Memory_ReadAddress( 0x9800, 0x9dff, MRA_RAM				),	// Work   RAM
		new Memory_ReadAddress( 0x9e00, 0x9fff, MRA_RAM				),	// Sprite RAM
		new Memory_ReadAddress( 0xa100, 0xa107, clshroad_input_r		),	// Inputs
		new Memory_ReadAddress( 0xa800, 0xafff, MRA_RAM				),	// Layer  1
		new Memory_ReadAddress( 0xc000, 0xc7ff, MRA_RAM				),	// Layers 0
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress clshroad_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM									),	// ROM
		new Memory_WriteAddress( 0x8000, 0x95ff, MWA_RAM									),	// Work   RAM
		new Memory_WriteAddress( 0x9600, 0x97ff, clshroad_sharedram_w, &clshroad_sharedram	),	// Shared RAM
		new Memory_WriteAddress( 0x9800, 0x9dff, MWA_RAM									),	// Work   RAM
		new Memory_WriteAddress( 0x9e00, 0x9fff, MWA_RAM, &spriteram, &spriteram_size		),	// Sprite RAM
		new Memory_WriteAddress( 0xa001, 0xa001, MWA_NOP									),	// ? Interrupt related
		new Memory_WriteAddress( 0xa004, 0xa004, clshroad_flipscreen_w						),	// Flip Screen
		new Memory_WriteAddress( 0xa800, 0xafff, clshroad_vram_1_w, &clshroad_vram_1		),	// Layer 1
		new Memory_WriteAddress( 0xb000, 0xb003, MWA_RAM, &clshroad_vregs					),	// Scroll
		new Memory_WriteAddress( 0xc000, 0xc7ff, clshroad_vram_0_w, &clshroad_vram_0		),	// Layers 0
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	/***************************************************************************
	
	
									Sound CPU
	
	
	***************************************************************************/
	
	public static Memory_ReadAddress clshroad_sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0x1fff, MRA_ROM				),	// ROM
		new Memory_ReadAddress( 0x4000, 0x5fff, MRA_RAM				),	// RAM (wavedata?)
		new Memory_ReadAddress( 0x6000, 0x7fff, MRA_RAM				),	// RAM (wavedata?)
		new Memory_ReadAddress( 0x9600, 0x97ff, clshroad_sharedram_r	),	// Shared RAM
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress clshroad_sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0x1fff, MWA_ROM				),	// ROM
		new Memory_WriteAddress( 0x4000, 0x5fff, MWA_RAM				),	// RAM (wavedata?)
		new Memory_WriteAddress( 0x6000, 0x7fff, MWA_RAM				),	// RAM (wavedata?)
		new Memory_WriteAddress( 0x9600, 0x97ff, clshroad_sharedram_w	),	// Shared RAM
		new Memory_WriteAddress( 0xa003, 0xa003, MWA_NOP				),	// ? Interrupt related
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	/***************************************************************************
	
	
									Input Ports
	
	
	***************************************************************************/
	
	static InputPortPtr input_ports_clshroad = new InputPortPtr(){ public void handler() { 

		PORT_START(); 	// IN0 - Player 1
		PORT_BIT(  0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER1 );	PORT_BIT(  0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );	PORT_BIT(  0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );	PORT_BIT(  0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );	PORT_BIT(  0x10, IP_ACTIVE_LOW, IPT_BUTTON1        | IPF_PLAYER1 );	PORT_BIT(  0x20, IP_ACTIVE_LOW, IPT_BUTTON2        | IPF_PLAYER1 );	PORT_BIT(  0x40, IP_ACTIVE_LOW, IPT_COIN1    );	PORT_BIT(  0x80, IP_ACTIVE_LOW, IPT_UNKNOWN  );
		PORT_START(); 	// IN1 - Player 2
		PORT_BIT(  0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER2 );	PORT_BIT(  0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );	PORT_BIT(  0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );	PORT_BIT(  0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );	PORT_BIT(  0x10, IP_ACTIVE_LOW, IPT_BUTTON1        | IPF_PLAYER2 );	PORT_BIT(  0x20, IP_ACTIVE_LOW, IPT_BUTTON2        | IPF_PLAYER2 );	PORT_BIT(  0x40, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT(  0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START(); 	// IN2 - DSW 1
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Difficulty") );	// Damage when falling
		PORT_DIPSETTING(    0x18, "Normal"  );// 8
		PORT_DIPSETTING(    0x10, "Hard"    );// A
		PORT_DIPSETTING(    0x08, "Harder"  );// C
		PORT_DIPSETTING(    0x00, "Hardest" );// E
		PORT_BITX(    0x20, 0x20, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "Unknown 1-6" );	PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Unknown 1-7" );	PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	// IN3 - DSW 2
	/*
	first bit OFF is:	0 			0	<- value
						1			1
						2			2
						3			3
						4			4
						5			5
						6			6
						else		FF
	
	But the values seems unused then.
	*/
		PORT_DIPNAME( 0x01, 0x01, "Unknown 2-0" );	PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Unknown 2-1" );	PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, "Unknown 2-2" );	PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "Unknown 2-3" );	PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, "Unknown 2-4" );	PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, "Unknown 2-5" );	PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "Unknown 2-6" );	PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Unknown 2-7" );//?
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
	INPUT_PORTS_END(); }}; 
	
	
	/***************************************************************************
	
	
									Graphics Layouts
	
	
	***************************************************************************/
	
	static GfxLayout layout_8x8x4 = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,2),
		4,
		new int[] { 0, 4, RGN_FRAC(1,2) + 0, RGN_FRAC(1,2) + 4 },
		new int[] { STEP4(0,1), STEP4(8,1) },
		new int[] { STEP8(0,8*2) },
		8*8*2
	);
	
	static GfxLayout layout_16x16x4 = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,2),
		4,
		new int[] { 0, 4, RGN_FRAC(1,2) + 0, RGN_FRAC(1,2) + 4 },
		new int[] { STEP4(0,1), STEP4(8,1), STEP4(8*8*2+0,1), STEP4(8*8*2+8,1) },
		new int[] { STEP8(0,8*2), STEP8(8*8*2*2,8*2) },
		16*16*2
	);
	
	static GfxDecodeInfo clshroad_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, layout_16x16x4, 0, 16 ), // [0] Sprites
		new GfxDecodeInfo( REGION_GFX2, 0, layout_16x16x4, 0, 16 ), // [1] Layer 0
		new GfxDecodeInfo( REGION_GFX3, 0, layout_8x8x4,   0, 16 ), // [2] Layer 1
		new GfxDecodeInfo( -1 )
	};
	
	
	/***************************************************************************
	
	
									Machine Drivers
	
	
	***************************************************************************/
	
	public static InitMachinePtr clshroad_init_machine = new InitMachinePtr() { public void handler() 
	{
		flip_screen_set(0);
	} };
	
	static MachineDriver machine_driver_clshroad = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3000000,	/* ? */
				clshroad_readmem, clshroad_writemem,	null, null,
				interrupt, 1	/* IRQ, no NMI */
			),
	#if 0
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				3000000,	/* ? */
				clshroad_sound_readmem, clshroad_sound_writemem,	null, null,
				interrupt, 1	/* IRQ, no NMI */
			)
	#endif
		},
		60,DEFAULT_60HZ_VBLANK_DURATION,
		1,
		clshroad_init_machine,
	
		/* video hardware */
		0x120, 0x100, new rectangle( 0, 0x120-1, 0x0+16, 0x100-16-1 ),
		clshroad_gfxdecodeinfo,
		256, 256,
		clshroad_vh_convert_color_prom,	/* Is there a color PROM ? */
		VIDEO_TYPE_RASTER,
		null,
		clshroad_vh_start,
		null,
		clshroad_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound( 0 )	// Custom?
		}
	);
	
	
	/***************************************************************************
	
	
									ROMs Loading
	
	
	***************************************************************************/
	
	
	/***************************************************************************
									Clash Road
	***************************************************************************/
	
	static RomLoadPtr rom_clshroad = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );	/* Main Z80 Code */
		ROM_LOAD( "clashr3.bin", 0x00000, 0x8000, 0x865c32ae );
		ROM_REGION( 0x10000, REGION_CPU2, 0 );	/* Sound Z80 Code */
		ROM_LOAD( "clashr2.bin", 0x00000, 0x2000, 0xe6389ec1 );
		ROM_REGION( 0x08000, REGION_GFX1, ROMREGION_DISPOSE );/* Sprites */
		ROM_LOAD( "clashr6.bin", 0x00000, 0x4000, 0xdaa1daf3 );	ROM_LOAD( "clashr5.bin", 0x04000, 0x4000, 0x094858b8 );
		ROM_REGION( 0x08000, REGION_GFX2, ROMREGION_DISPOSE );/* Layer 0 */
		ROM_LOAD( "clashr9.bin", 0x00000, 0x4000, 0xc15e8eed );	ROM_LOAD( "clashr8.bin", 0x04000, 0x4000, 0xcbb66719 );
		ROM_REGION( 0x04000, REGION_GFX3, ROMREGION_DISPOSE );/* Layer 1 */
		ROM_LOAD( "clashr7.bin", 0x00000, 0x2000, 0x97973030 );	ROM_LOAD( "clashr4.bin", 0x02000, 0x2000, 0x664201d9 );
		ROM_REGION( 0x2000, REGION_SOUND1, 0 );	/* Samples ? */
		ROM_LOAD( "clashr1.bin", 0x00000, 0x2000, 0x0d0a8068 );
		ROM_REGION( 0x0200, REGION_PROMS, 0 );	/* Is there a color PROM ? */
		ROM_LOAD( "colors.bin", 0x0000, 0x0200, 0x00000000 );ROM_END(); }}; 
	
	
	public static GameDriver driver_clshroad	   = new GameDriver("1986"	,"clshroad"	,"clshroad.java"	,rom_clshroad,null	,machine_driver_clshroad	,input_ports_clshroad	,null	,ROT0	,	"Woodplace Inc.", "Clash Road", GAME_WRONG_COLORS | GAME_NO_SOUND )
}
