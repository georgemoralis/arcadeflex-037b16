/*
To Do:
- get sound working
- map and test any remaining input ports

Looping
(C)1981 Venture Line

	Main CPU
		TMS9995

	COP420 Microcontroller
		manages CPU communnication?

	Sound CPU
		TMS9980
		AY-3-8910
		TMS5220 (SPEECH)

---------------------------------------------------------------

Sky Bumper
(C)1982 Venture Line

	This is a ROM swap for Looping.  There are two 6116's on
	the CPU board, where there is only one on Looping.

---------------------------------------------------------------

Super Tank
(C)19?? Venture Line

Runs on simpler hardware; not yet emulated.

===============================================================

LOOPING CHIP PLACEMENT

THERE ARE AT LEAST TWO VERSIONS OF THIS GAME
VERSION NUMBERS FOR THIS PURPOSE ARE CHOSEN AT RANDOM

IC NAME   POSITION   BOARD  TYPE   IC NAME  POSITION  TYPE
VER-1                         VER-2
---------------------------------------------------------------
LOS-2-7   13A        I/O    2532    SAME    13A       2532
LOS-1-1-2 11A         "      "      SAME    11A        "
LOS-3-1   13C         "      "      I-O-V2  13C        "

VLI1      2A         ROM    2764    VLI-7-1 2A         "
VLI3      5A          "      "      VLI-7-2 4A         "
VLI9-5    8A          "      "      VLI-4-3 5A         "
L056-6    9A          "      "      VLI-8-4 7A         "
                      "             LO56-5  8A         "
                      "             LO56-6  9A         "
                      "             VLI-8-7 10A        "
                  ON RIBBON CABLE   18S030  11B				color prom?
                     REAR BD      LOG.1-9-3 6A        2716	tiles
                                  LOG.3     8A         "	tiles
*/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class looping
{
	
	static struct tilemap *tilemap;
	
	void looping_vh_convert_color_prom(UBytePtr palette,unsigned short *colortable,const UBytePtr color_prom)
	{
		int i;
		for (i = 0;i < 0x20;i++)
		{
			int bit0,bit1,bit2;
	
			/* red component */
			bit0 = (*color_prom >> 0) & 0x01;
			bit1 = (*color_prom >> 1) & 0x01;
			bit2 = (*color_prom >> 2) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* green component */
			bit0 = (*color_prom >> 3) & 0x01;
			bit1 = (*color_prom >> 4) & 0x01;
			bit2 = (*color_prom >> 5) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* blue component */
			bit0 = (*color_prom >> 6) & 0x01;
			bit1 = (*color_prom >> 7) & 0x01;
			*(palette++) = 0x4f * bit0 + 0xa8 * bit1;
	
			color_prom++;
		}
	}
	
	static void get_tile_info( int offset )
	{
		int tile_number = videoram.read(offset);
		int color = colorram.read((offset&0x1f)*2+1)&0x7;
		SET_TILE_INFO(
				0,
				tile_number,
				color,
				0)
	}
	
	public static WriteHandlerPtr looping_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int i,offs;
		if( colorram.read(offset)!=data )
		{
			colorram.write(offset,data);
		if ((offset & 1) != 0)
			{
				/* odd bytes are column color attribute */
				offs = (offset/2);
				/* mark the whole column dirty */
				for( i=0; i<0x20; i++ )
				{
					tilemap_mark_tile_dirty( tilemap, offs );
					offs += 0x20;
				}
			}
			else
			{
				/* even bytes are column scroll */
				tilemap_set_scrolly( tilemap,offset/2,data );
			}
		}
	} };
	
	int looping_vh_init( void )
	{
		tilemap = tilemap_create( get_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,32,32 );
		if (tilemap != 0)
		{
			tilemap_set_scroll_cols( tilemap, 0x20 );
			return 0;
		}
		return -1;
	}
	
	public static WriteHandlerPtr looping_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if( videoram.read(offset)!=data )
		{
			videoram.write(offset,data);
		tilemap_mark_tile_dirty( tilemap, offset );
		}
	} };
	
	static void draw_sprites( struct osd_bitmap *bitmap )
	{
		int tile_number;
		const UINT8 *source = spriteram;
		const UINT8 *finish = source + 0x10*4; /* ? */
	
		while( source<finish )
		{
			tile_number = source[1];
			drawgfx( bitmap,
				Machine.gfx[1],
				tile_number&0x3f,
				source[2], /* color */
				tile_number&0x40, /* flipx */
				tile_number&0x80, /* flipy */
				source[3], /* xpos */
				240 - source[0], /* ypos */
				&Machine.visible_area,
				TRANSPARENCY_PEN,0 );
	
			source += 4;
		}
	}
	
	public static VhUpdatePtr looping_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update( ALL_TILEMAPS );
		palette_recalc();
		tilemap_draw( bitmap,tilemap,0,0 );
		draw_sprites( bitmap );
	} };
	
	public static WriteHandlerPtr looping_intack = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (data==0)
		{
			cpu_0_irq_line_vector_w(0, 4);
			cpu_set_irq_line(0, 0, CLEAR_LINE);
		}
	} };
	
	public static InterruptPtr looping_interrupt = new InterruptPtr() { public int handler() 
	{
		cpu_0_irq_line_vector_w(0, 4);
		cpu_set_irq_line(0, 0, ASSERT_LINE);
		return ignore_interrupt();
	} };
	
	/****** sound *******/
	
	public static WriteHandlerPtr looping_soundlatch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		soundlatch_w.handler(offset, data);
		cpu_1_irq_line_vector_w(0, 4);
		cpu_set_irq_line(1, 0, ASSERT_LINE);
	} };
	
	public static WriteHandlerPtr looping_souint_clr = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (data==0)
		{
			cpu_1_irq_line_vector_w(0, 4);
			cpu_set_irq_line(1, 0, CLEAR_LINE);
		}
	} };
	
	void looping_spcint(int state)
	{
		cpu_1_irq_line_vector_w(0, 6);
		cpu_set_irq_line(1, 0, state);
	}
	
	public static WriteHandlerPtr looping_sound_sw = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* this can be improved by adding the missing
		   signals for decay etc. (see schematics) */
		static int r[8];
		r[offset]=data^1;
		DAC_data_w(0, ((r[1]<<7) + (r[2]<<6))*r[6]);
	} };
	
	public static Memory_ReadAddress looping_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
	/*	new Memory_ReadAddress( 0x9000, 0x9fff, MRA_RAM ), videoram is write only? */
		new Memory_ReadAddress( 0xe000, 0xefff, MRA_RAM ),
		new Memory_ReadAddress( 0xf800, 0xf800, input_port_0_r ),	/* inp */
		new Memory_ReadAddress( 0xf801, 0xf801, input_port_1_r ),
		new Memory_ReadAddress( 0xf802, 0xf802, input_port_2_r ),	/* dsw */
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress looping_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0x9000, 0x93ff, looping_videoram_w, &videoram ),
		new Memory_WriteAddress( 0x9800, 0x983f, looping_colorram_w, &colorram ),
		new Memory_WriteAddress( 0x9840, 0x987f, MWA_RAM, &spriteram ),
		new Memory_WriteAddress( 0xe000, 0xefff, MWA_RAM ),
		new Memory_WriteAddress( 0xb006, 0xb007, MWA_RAM ), /* unknown */
		new Memory_WriteAddress( 0xf801, 0xf801, looping_soundlatch_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	static )
		{ 0x000, 0x000, MWA_NOP },
		{ 0x406, 0x406, looping_intack },
		{ 0x407, 0x407, watchdog_reset_w },
	

	public static Memory_ReadAddress looping_io_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0x37ff, MRA_ROM ),
		new Memory_ReadAddress( 0x3800, 0x3bff, MRA_RAM ),
		new Memory_ReadAddress( 0x3c00, 0x3c00, AY8910_read_port_0_r ),
		new Memory_ReadAddress( 0x3e02, 0x3e02, tms5220_status_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress looping_io_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0x37ff, MWA_ROM ),
		new Memory_WriteAddress( 0x3800, 0x3bff, MWA_RAM ),
		new Memory_WriteAddress( 0x3c00, 0x3c00, AY8910_control_port_0_w ),
		new Memory_WriteAddress( 0x3c02, 0x3c02, AY8910_write_port_0_w ),
		new Memory_WriteAddress( 0x3e00, 0x3e00, tms5220_data_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	static )
		{ 0x000, 0x000, looping_souint_clr },
		{ 0x001, 0x007, looping_sound_sw },
	

	static GfxLayout tile_layout = new GfxLayout
	(
		8,8,		/* 8*8 characters */
		0x100,		/* number of characters */
		2,			/* 2 bits per pixel */
		new int[] { 0,0x800*8 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8
	);
	
	static GfxLayout sprite_layout = new GfxLayout
	(
		16,16,		/* 8*8 characters */
		0x40,		/* number of characters */
		2,			/* 2 bits per pixel */
		new int[] { 0,0x800*8 },
		new int[] {
			0, 1, 2, 3, 4, 5, 6, 7,
			64+0, 64+1, 64+2, 64+3, 64+4, 64+5, 64+6, 64+7
		},
		new int[] {
			0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
			128+0*8, 128+1*8, 128+2*8, 128+3*8, 128+4*8, 128+5*8, 128+6*8, 128+7*8
		},
		8*8*4
	);
	
	static GfxDecodeInfo looping_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, tile_layout,		0, 8 ),
		new GfxDecodeInfo( REGION_GFX1, 0, sprite_layout,	0, 8 ),
		new GfxDecodeInfo( -1 )
	};
	
	static struct TMS5220interface tms5220_interface =
	{
		640000,         /* clock speed (80*samplerate) */
		50,             /* volume */
		looping_spcint  /* IRQ handler */
	};
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		1,
		2000000,
		new int[] { 20 },
		new ReadHandlerPtr[] { soundlatch_r },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	static DACinterface dac_interface = new DACinterface
	(
		1,
		new int[] { 30 }
	);
	
	static MachineDriver machine_driver_looping = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_TMS9995,
				3000000, /* ? */
				looping_readmem,looping_writemem,null,looping_writeport,
				looping_interrupt,1
			),
			new MachineCPU( /* sound */
				CPU_TMS9980,
				2000000, // ?
				looping_io_readmem,looping_io_writemem,null,looping_io_writeport,
				ignore_interrupt,1
			)
		},
		60, 2500,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame */
		null,
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		looping_gfxdecodeinfo,
		32,32,
		looping_vh_convert_color_prom,
		VIDEO_TYPE_RASTER,
		null,
		looping_vh_init,
		null, /*looping_vh_stop*/
		looping_vh_screenrefresh,
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			),
			new MachineSound(
				SOUND_TMS5220,
				tms5220_interface
			),
			new MachineSound(
				SOUND_DAC,
	 			dac_interface
			)
		}
	
	);
	
	static InputPortPtr input_ports_looping = new InputPortPtr(){ public void handler() { 
	PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 );/* shoot */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );/* accel? */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_START();  /* cocktail? */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_COCKTAIL );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_COCKTAIL );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0x0e, 0x02, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, "1 Coin/10 Credits" );	PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "No") );
		PORT_DIPSETTING(	0x10, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );	PORT_DIPSETTING(    0x20, "5" );	PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "No") );
		PORT_DIPSETTING(	0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "No") );
		PORT_DIPSETTING(	0x80, DEF_STR( "Yes") );
	INPUT_PORTS_END(); }}; 
	
	static RomLoadPtr rom_loopinga = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );/* 64k for TMS9995 code */
		ROM_LOAD( "vli3.5a",		0x0000, 0x2000, 0x1ac3ccdf );	ROM_LOAD( "vli-4-3",		0x2000, 0x1000, 0xf32cae2b );	ROM_LOAD( "vli-8-4",		0x3000, 0x1000, 0x611e1dbf );	ROM_LOAD( "l056-6.9a",		0x4000, 0x2000, 0x548afa52 );	ROM_LOAD( "vli9-5.8a",		0x6000, 0x2000, 0x5d122f86 );
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for TMS9980 code */
		ROM_LOAD( "i-o-v2.13c",		0x0000, 0x0800, 0x09765ebe );    ROM_LOAD( "i-o.13a",		0x0800, 0x1000, 0x1de29f25 );/* speech */
		ROM_LOAD( "i-o.11a",		0x2800, 0x1000, 0x61c74c79 );
		ROM_REGION( 0x1000, REGION_CPU3, 0 );/* COP420 microcontroller code */
		ROM_LOAD( "cop.bin",		0x0000, 0x1000, 0xbbfd26d5 );
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "log1-9-3.6a",	0x0000, 0x800, 0xc434c14c );	ROM_LOAD( "log2.8a",		0x0800, 0x800, 0xef3284ac );
		ROM_REGION( 0x0020, REGION_PROMS, 0 );/* color prom */
		ROM_LOAD( "18s030.11b",		0x0000, 0x0020, 0x6a0c7d87 );ROM_END(); }}; 
	
	static RomLoadPtr rom_looping = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );/* 64k for TMS9995 code */
		ROM_LOAD( "vli3.5a",		0x0000, 0x2000, 0x1ac3ccdf );	ROM_LOAD( "vli1.2a",		0x2000, 0x2000, 0x97755fd4 );	ROM_LOAD( "l056-6.9a",		0x4000, 0x2000, 0x548afa52 );	ROM_LOAD( "vli9-5.8a",		0x6000, 0x2000, 0x5d122f86 );
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for TMS9980 code */
		ROM_LOAD( "i-o.13c",		0x0000, 0x0800, 0x21e9350c );	ROM_LOAD( "i-o.13a",		0x0800, 0x1000, 0x1de29f25 );	ROM_LOAD( "i-o.11a",		0x2800, 0x1000, 0x61c74c79 );/* speech */
	
		ROM_REGION( 0x1000, REGION_CPU3, 0 );/* COP420 microcontroller code */
		ROM_LOAD( "cop.bin",		0x0000, 0x1000, 0xbbfd26d5 );
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "log1-9-3.6a",	0x0000, 0x800, 0xc434c14c );	ROM_LOAD( "log2.8a",		0x0800, 0x800, 0xef3284ac );
		ROM_REGION( 0x0020, REGION_PROMS, 0 );/* color prom */
		ROM_LOAD( "18s030.11b",		0x0000, 0x0020, 0x6a0c7d87 );ROM_END(); }}; 
	
	static RomLoadPtr rom_skybump = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );/* 64k for TMS9995 code */
		ROM_LOAD( "cpu.5a",			0x0000, 0x2000, 0xdca38df0 );	ROM_LOAD( "cpu.2a",			0x2000, 0x2000, 0x6bcc211a );	ROM_LOAD( "cpu.9a",			0x4000, 0x2000, 0xc7a50797 );	ROM_LOAD( "cpu.8a",			0x6000, 0x2000, 0xa718c6f2 );
	    ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for TMS9980 code */
		ROM_LOAD( "snd.13c",		0x0000, 0x0800, 0x21e9350c );	ROM_LOAD( "snd.13a",		0x0800, 0x1000, 0x1de29f25 );	ROM_LOAD( "snd.11a",		0x2800, 0x1000, 0x61c74c79 );
		ROM_REGION( 0x1000, REGION_CPU3, 0 );/* COP420 microcontroller code */
		ROM_LOAD( "cop.bin",		0x0000, 0x1000, 0xbbfd26d5 );
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "vid.6a",			0x0000, 0x800, 0x12ebbe74 );	ROM_LOAD( "vid.8a",			0x0800, 0x800, 0x459ccc55 );
		ROM_REGION( 0x0020, REGION_PROMS, 0 );/* color prom */
		ROM_LOAD( "vid.clr",		0x0000, 0x0020, 0x6a0c7d87 );ROM_END(); }}; 
	
	public static InitDriverPtr init_looping = new InitDriverPtr() { public void handler() {
		/* unscramble the TMS9995 ROMs */
		UINT8 *pMem = memory_region( REGION_CPU1 );
		UINT8 raw,code;
		int i;
		for( i=0; i<0x8000; i++ )
		{
			raw = pMem[i];
			code = 0;
			if ((raw & 0x01) != 0) code |= 0x80;
			if ((raw & 0x02) != 0) code |= 0x40;
			if ((raw & 0x04) != 0) code |= 0x20;
			if ((raw & 0x08) != 0) code |= 0x10;
			if ((raw & 0x10) != 0) code |= 0x08;
			if ((raw & 0x20) != 0) code |= 0x04;
			if ((raw & 0x40) != 0) code |= 0x02;
			if ((raw & 0x80) != 0) code |= 0x01;
			pMem[i] = code;
		}
	} };
	
	/*          rom       parent    machine   inp       init */
	public static GameDriver driver_looping	   = new GameDriver("1982"	,"looping"	,"looping.java"	,rom_looping,null	,machine_driver_looping	,input_ports_looping	,init_looping	,ROT90	,	"Venture Line", "Looping (set 1)" )
	public static GameDriver driver_loopinga	   = new GameDriver("1982"	,"loopinga"	,"looping.java"	,rom_loopinga,driver_looping	,machine_driver_looping	,input_ports_looping	,init_looping	,ROT90	,	"Venture Line", "Looping (set 2)" )
	public static GameDriver driver_skybump	   = new GameDriver("1982"	,"skybump"	,"looping.java"	,rom_skybump,null	,machine_driver_looping	,input_ports_looping	,init_looping	,ROT90	,	"Venture Line", "Sky Bumper" )
}
