/***************************************************************************

Xexex

Unemulated constant write accesses:
Hard.Addr	RAM Addr.	Value
d0001		80482		01
d0003		80483		ff
d0005		80484		00
d0007		80485		21
d0009		80486		00
d000b		80487		37
d000d		80488		01
d0013		80489		20
d0015		8048a		0c
d0017		8048b		0e
d0019		8048c		54
d001c		8048d		00

ca001		80468		00
ca002		80469		00
ca003		8046a		00

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class xexex
{
	
	
	void xexex_set_alpha(int on);
	
	READ16_HANDLER( xexexbg_r );
	WRITE16_HANDLER( xexexbg_w );
	READ16_HANDLER( xexexbg_ram_r );
	WRITE16_HANDLER( xexexbg_ram_w );
	READ16_HANDLER( xexexbg_rom_r );
	WRITE16_HANDLER( xexex_alpha_level_w );
	
	static data16_t cur_control2;
	static int init_eeprom_count;
	
	static EEPROM_interface eeprom_interface = new EEPROM_interface
	(
		7,				/* address bits */
		8,				/* data bits */
		"011000",		/*  read command */
		"011100",		/* write command */
		"0100100000000",/* erase command */
		"0100000000000",/* lock command */
		"0100110000000" /* unlock command */
	);
	
	public static nvramPtr nvram_handler  = new nvramPtr() { public void handler(Object file, int read_or_write) 
	{
		if (read_or_write != 0)
			EEPROM_save(file);
		else
		{
			EEPROM_init(&eeprom_interface);
	
			if (file != 0)
			{
				init_eeprom_count = 0;
				EEPROM_load(file);
			}
			else
				init_eeprom_count = 10;
		}
	} };
	
	/* the interface with the 053247 is weird. The chip can address only 0x1000 bytes */
	/* of RAM, but they put 0x8000 there. The CPU can access them all. Address lines */
	/* A1, A5 and A6 don't go to the 053247. */
	static READ16_HANDLER( K053247_scattered_word_r )
	{
		if ((offset & 0x0031) != 0)
			return spriteram16[offset];
		else
		{
			offset = ((offset & 0x000e) >> 1) | ((offset & 0x3fc0) >> 3);
			return K053247_word_r(offset,mem_mask);
		}
	}
	
	static WRITE16_HANDLER( K053247_scattered_word_w )
	{
		if ((offset & 0x0031) != 0)
			COMBINE_DATA(spriteram16+offset);
		else
		{
			offset = ((offset & 0x000e) >> 1) | ((offset & 0x3fc0) >> 3);
			K053247_word_w(offset,data,mem_mask);
		}
	}
	
	static READ16_HANDLER( control0_r )
	{
		return input_port_0_r(0);
	}
	
	static READ16_HANDLER( control1_r )
	{
		int res;
	
		/* bit 0 is EEPROM data */
		/* bit 1 is EEPROM ready */
		/* bit 3 is service button */
		res = EEPROM_read_bit() | input_port_1_r(0);
	
		if (init_eeprom_count != 0)
		{
			init_eeprom_count--;
			res &= 0xf7;
		}
	
		return res;
	}
	
	static void parse_control2(void)
	{
		/* bit 0  is data */
		/* bit 1  is cs (active low) */
		/* bit 2  is clock (active high) */
		/* bit 5  is enable irq 6 */
		/* bit 6  is enable irq 5 */
		/* bit 11 is watchdog */
	
		EEPROM_write_bit(cur_control2 & 0x01);
		EEPROM_set_cs_line((cur_control2 & 0x02) ? CLEAR_LINE : ASSERT_LINE);
		EEPROM_set_clock_line((cur_control2 & 0x04) ? ASSERT_LINE : CLEAR_LINE);
	
		/* bit 8 = enable sprite ROM reading */
		K053246_set_OBJCHA_line((cur_control2 & 0x0100) ? ASSERT_LINE : CLEAR_LINE);
	
		/* bit 9 = disable alpha channel on K054157 plane 0 */
		xexex_set_alpha(!(cur_control2 & 0x200));
	}
	
	static READ16_HANDLER( control2_r )
	{
		return cur_control2;
	}
	
	static WRITE16_HANDLER( control2_w )
	{
		COMBINE_DATA(&cur_control2);
		parse_control2();
	}
	
	public static InterruptPtr xexex_interrupt = new InterruptPtr() { public int handler() 
	{
		switch (cpu_getiloops())
		{
			case 1:
				if (K053246_is_IRQ_enabled())
					return 4;
				break;
	
			case 0:
				if (K053246_is_IRQ_enabled() && (cur_control2 & 0x0040))
					return 5;
				break;
	
			case 2:
				if (K053246_is_IRQ_enabled() && (cur_control2 & 0x0020))
					return 6;
				break;
		}
		return ignore_interrupt();
	} };
	
	static WRITE16_HANDLER( sound_cmd1_w )
	{
		if (ACCESSING_LSB != 0) {
			data &= 0xff;
			soundlatch_w(0, data);
			if(!Machine.sample_rate)
				if(data == 0xfc || data == 0xfe)
					soundlatch3_w(0, 0x7f);
		}
	}
	
	static WRITE16_HANDLER( sound_cmd2_w )
	{
		if (ACCESSING_LSB != 0)
			soundlatch2_w(0, data & 0xff);
	}
	
	static WRITE16_HANDLER( sound_irq_w )
	{
		cpu_set_irq_line(1, Z80_IRQ_INT, HOLD_LINE);
	}
	
	static READ16_HANDLER( sound_status_r )
	{
		return soundlatch3_r(0);
	}
	
	static int cur_sound_region = 0;
	
	static void reset_sound_region(void)
	{
		cpu_setbank(2, memory_region(REGION_CPU2) + 0x10000 + cur_sound_region*0x4000);
	}
	
	public static WriteHandlerPtr sound_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		cur_sound_region = data & 7;
		reset_sound_region();
	} };
	
	static void ym_set_mixing(double left, double right)
	{
		if(Machine.sample_rate) {
			int l = 71*left;
			int r = 71*right;
			int ch;
			for(ch=0; ch<MIXER_MAX_CHANNELS; ch++) {
				const char *name = mixer_get_name(ch);
				if(name && name[0] == 'Y')
					mixer_set_stereo_volume(ch, l, r);
			}
		}
	}
	
	static MEMORY_READ16_START( readmem )
		{ 0x000000, 0x07ffff, MRA16_ROM },
		{ 0x080000, 0x08ffff, MRA16_RAM },			/* Work RAM */
		{ 0x090000, 0x097fff, K053247_scattered_word_r },	/* Sprites */
		{ 0x098000, 0x09ffff, K053247_scattered_word_r },	/* Sprites (mirror) */
		{ 0x0c4000, 0x0c4001, K053246_word_r },
		{ 0x0c6000, 0x0c6fff, xexexbg_ram_r },			/* Background generator effects */
		{ 0x0c8000, 0x0c800f, xexexbg_r },
		{ 0x0d6014, 0x0d6015, sound_status_r },
		{ 0x0da000, 0x0da001, input_port_2_word_r },
		{ 0x0da002, 0x0da003, input_port_3_word_r },
		{ 0x0dc000, 0x0dc001, input_port_0_word_r },
		{ 0x0dc002, 0x0dc003, control1_r },
		{ 0x0de000, 0x0de001, control2_r },
		{ 0x100000, 0x17ffff, MRA16_ROM },
		{ 0x180000, 0x181fff, K054157_ram_word_r },		/* Graphic planes */
		{ 0x190000, 0x191fff, K054157_rom_word_r }, 	/* Passthrough to tile roms */
		{ 0x1a0000, 0x1a1fff, xexexbg_rom_r },
		{ 0x1b0000, 0x1b1fff, MRA16_RAM },
	MEMORY_END
	
	static MEMORY_WRITE16_START( writemem )
		{ 0x000000, 0x07ffff, MWA16_ROM },
		{ 0x080000, 0x08ffff, MWA16_RAM },
		{ 0x090000, 0x097fff, K053247_scattered_word_w, &spriteram16 },
			//	{ 0x098000, 0x09ffff, K053247_scattered_word_w },/* Mirror for some buggy levels */
		{ 0x0c0000, 0x0c003f, K054157_word_w },
		{ 0x0c2000, 0x0c2007, K053246_word_w },
		{ 0x0c6000, 0x0c6fff, xexexbg_ram_w },
		{ 0x0c8000, 0x0c800f, xexexbg_w },
		{ 0x0ca000, 0x0ca003, MWA16_NOP },
		{ 0x0ca01a, 0x0ca01b, xexex_alpha_level_w },
		{ 0x0cc000, 0x0cc01f, K053251_lsb_w },
		{ 0x0d0000, 0x0d001d, MWA16_NOP },
		{ 0x0d4000, 0x0d4001, sound_irq_w },
		{ 0x0d600c, 0x0d600d, sound_cmd1_w },
		{ 0x0d600e, 0x0d600f, sound_cmd2_w },
		{ 0x0d8000, 0x0d8007, K054157_b_word_w },
		{ 0x0de000, 0x0de001, control2_w },
		{ 0x100000, 0x17ffff, MWA16_ROM },
		{ 0x180000, 0x181fff, K054157_ram_word_w },
		{ 0x182000, 0x183fff, K054157_ram_word_w },		/* Mirror for some buggy levels */
		{ 0x190000, 0x191fff, MWA16_ROM },
		{ 0x1b0000, 0x1b1fff, paletteram16_xrgb_word_w, &paletteram16 },
	MEMORY_END
	
	public static Memory_ReadAddress sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0xbfff, MRA_BANK2 ),
		new Memory_ReadAddress( 0xc000, 0xdfff, MRA_RAM ),
		new Memory_ReadAddress( 0xe000, 0xe22f, K054539_0_r ),
		new Memory_ReadAddress( 0xec01, 0xec01, YM2151_status_port_0_r ),
		new Memory_ReadAddress( 0xf002, 0xf002, soundlatch_r ),
		new Memory_ReadAddress( 0xf003, 0xf003, soundlatch2_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xdfff, MWA_RAM ),
		new Memory_WriteAddress( 0xe000, 0xe22f, K054539_0_w ),
		new Memory_WriteAddress( 0xec00, 0xec00, YM2151_register_port_0_w ),
		new Memory_WriteAddress( 0xec01, 0xec01, YM2151_data_port_0_w ),
		new Memory_WriteAddress( 0xf000, 0xf000, soundlatch3_w ),
		new Memory_WriteAddress( 0xf800, 0xf800, sound_bankswitch_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	static InputPortPtr input_ports_xexex = new InputPortPtr(){ public void handler() { 
	PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_SERVICE2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_SPECIAL );/* EEPROM data */
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SPECIAL );/* EEPROM ready (always 1) */
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
		PORT_BIT( 0xf0, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );INPUT_PORTS_END(); }}; 
	
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,
		4000000,		/* Value found in Amuse */
		new int[] { YM3012_VOL(50,MIXER_PAN_CENTER,50,MIXER_PAN_CENTER) },
		new WriteYmHandlerPtr[] { 0 }
	);
	
	static K054539interface k054539_interface = new K054539interface
	(
		1,			/* 1 chip */
		48000,
		{ REGION_SOUND1 },
		{ { 100, 100 } },
		{ ym_set_mixing }
	);
	
	static MachineDriver machine_driver_xexex = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				16000000,	/* 16 MHz ? (xtal is 32MHz) */
				readmem, writemem, null, null,
				xexex_interrupt, 3	/* ??? */
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				5000000,	/* 5 MHz in Amuse (xtal is 32MHz/19.432MHz) */
				sound_readmem, sound_writemem, null, null,
				ignore_interrupt, 0
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 8*8, (64-8)*8-1, 0*8, 32*8-1 ),
		//	64*8, 64*8, { 0*8, (64-0)*8-1, 0*8, 64*8-1 },
		null,	/* gfx decoded by konamiic.c */
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_NEEDS_6BITS_PER_GUN | VIDEO_RGB_DIRECT,
		0,
		xexex_vh_start,
		xexex_vh_stop,
		xexex_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_K054539,
				k054539_interface
			)
		},
	
		nvram_handler
	);
	
	
	static RomLoadPtr rom_xexex = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x180000, REGION_CPU1, 0 );	ROM_LOAD16_BYTE( "xex_a01.rom",  0x000000, 0x40000, 0x3ebcb066 )
		ROM_LOAD16_BYTE( "xex_a02.rom",  0x000001, 0x40000, 0x36ea7a48 )
		ROM_LOAD16_BYTE( "xex_b03.rom",  0x100000, 0x40000, 0x97833086 )
		ROM_LOAD16_BYTE( "xex_b04.rom",  0x100001, 0x40000, 0x26ec5dc8 )
	
		ROM_REGION( 0x030000, REGION_CPU2, 0 );	ROM_LOAD( "xex_a05.rom", 0x000000, 0x020000, 0x0e33d6ec );	ROM_RELOAD(              0x010000, 0x020000 );
		ROM_REGION( 0x200000, REGION_GFX1, 0 );	ROM_LOAD( "xex_b14.rom", 0x000000, 0x100000, 0x02a44bfa );	ROM_LOAD( "xex_b13.rom", 0x100000, 0x100000, 0x633c8eb5 );
		ROM_REGION( 0x400000, REGION_GFX2, 0 );	ROM_LOAD( "xex_b12.rom", 0x000000, 0x100000, 0x08d611b0 );	ROM_LOAD( "xex_b11.rom", 0x100000, 0x100000, 0xa26f7507 );	ROM_LOAD( "xex_b10.rom", 0x200000, 0x100000, 0xee31db8d );	ROM_LOAD( "xex_b09.rom", 0x300000, 0x100000, 0x88f072ef );
		ROM_REGION( 0x080000, REGION_GFX3, 0 );	ROM_LOAD( "xex_b08.rom", 0x000000, 0x080000, 0xca816b7b );
		ROM_REGION( 0x300000, REGION_SOUND1, 0 );	ROM_LOAD( "xex_b06.rom", 0x000000, 0x200000, 0x3b12fce4 );	ROM_LOAD( "xex_b07.rom", 0x200000, 0x100000, 0xec87fe1b );ROM_END(); }}; 
	
	static RomLoadPtr rom_xexexj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x180000, REGION_CPU1, 0 );	ROM_LOAD16_BYTE( "067jaa01.16d", 0x000000, 0x40000, 0x06e99784 )
		ROM_LOAD16_BYTE( "067jaa02.16e", 0x000001, 0x40000, 0x30ae5bc4 )
		ROM_LOAD16_BYTE( "xex_b03.rom",  0x100000, 0x40000, 0x97833086 )
		ROM_LOAD16_BYTE( "xex_b04.rom",  0x100001, 0x40000, 0x26ec5dc8 )
	
		ROM_REGION( 0x030000, REGION_CPU2, 0 );	ROM_LOAD( "067jaa05.4e", 0x000000, 0x020000, 0x2f4dd0a8 );	ROM_RELOAD(              0x010000, 0x020000 );
		ROM_REGION( 0x200000, REGION_GFX1, 0 );	ROM_LOAD( "xex_b14.rom", 0x000000, 0x100000, 0x02a44bfa );	ROM_LOAD( "xex_b13.rom", 0x100000, 0x100000, 0x633c8eb5 );
		ROM_REGION( 0x400000, REGION_GFX2, 0 );	ROM_LOAD( "xex_b12.rom", 0x000000, 0x100000, 0x08d611b0 );	ROM_LOAD( "xex_b11.rom", 0x100000, 0x100000, 0xa26f7507 );	ROM_LOAD( "xex_b10.rom", 0x200000, 0x100000, 0xee31db8d );	ROM_LOAD( "xex_b09.rom", 0x300000, 0x100000, 0x88f072ef );
		ROM_REGION( 0x080000, REGION_GFX3, 0 );	ROM_LOAD( "xex_b08.rom", 0x000000, 0x080000, 0xca816b7b );
		ROM_REGION( 0x300000, REGION_SOUND1, 0 );	ROM_LOAD( "xex_b06.rom", 0x000000, 0x200000, 0x3b12fce4 );	ROM_LOAD( "xex_b07.rom", 0x200000, 0x100000, 0xec87fe1b );ROM_END(); }}; 
	
	
	static public static InitDriverPtr init_xexex = new InitDriverPtr() { public void handler() 
	{
		konami_rom_deinterleave_2(REGION_GFX1);
		konami_rom_deinterleave_4(REGION_GFX2);
	
		/* Invulnerability */
	#if 0
		if(1 && !strcmp(Machine.gamedrv.name, "xexex")) {
			*(data16_t *)(memory_region(REGION_CPU1) + 0x648d4) = 0x4a79;
			*(data16_t *)(memory_region(REGION_CPU1) + 0x00008) = 0x5500;
		}
	#endif
	
		state_save_register_UINT16("main", 0, "control2", &cur_control2, 1);
		state_save_register_func_postload(parse_control2);
		state_save_register_int("main", 0, "sound region", &cur_sound_region);
		state_save_register_func_postload(reset_sound_region);
	} };
	
	public static GameDriver driver_xexex	   = new GameDriver("1991"	,"xexex"	,"xexex.java"	,rom_xexex,null	,machine_driver_xexex	,input_ports_xexex	,init_xexex	,ROT0	,	"Konami", "Xexex (World)" )
	public static GameDriver driver_xexexj	   = new GameDriver("1991"	,"xexexj"	,"xexex.java"	,rom_xexexj,driver_xexex	,machine_driver_xexex	,input_ports_xexex	,init_xexex	,ROT0	,	"Konami", "Xexex (Japan)" )
}
