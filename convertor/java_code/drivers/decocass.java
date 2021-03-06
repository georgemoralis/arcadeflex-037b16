/***********************************************************************

	DECO Cassette System driver

(01) Highway Chase (12/80)
(02) Sengoku Ninja tai (12/80)
(03) Manhattan (1/81)
(04) Terranean (81)
(??) Missile Sprinter (81)
(06) Nebula (3/81)
(07) Astro Fantasia (3/81)
(08) The Tower (2/81)
(09) Super Astro Fighter (5/81)
(??) Buramzon (81)
(11) Lock N Chase (4/81)
(12) The DECO BOY (Flash Boy) (8/81)
(13) Pro Golf (9/81)
(14) DS TeleJang (6/81)
(15) Lucky Poker (2/81)
(16) Treasure Island (1/82)
(18) Explorer (11/82)
(19) Disco No. 1 (3/82)
(20) Tornado (82)
(21) Mission X (3/82)
(22) Pro Tennis (5/82)
(??) Fishing (7/82)
(25) Angler Dandler
(26) Burger Time (6/83)
(27) Burnin' Rubber (11/82)
(27) Bump N Jump (4/83)
(28) Grapolop (11/82)
(29) Lappappa (11/82)
(30) Skater (3/83)
(31) Pro Bowling (10/83)
(32) Night Star (4/83)
(33) Pro Soccer (11/83)
(34) Super Doubles Tennis
(??) Cluster Buster (9/83)
(??) Genesis (11/83) <I think is may be Boomer Rangr'>
(??) Bambolin (83)
(37) Zeroize (10/83)
(38) Scrum Try (3/84)
(39) Peter Pepper's Ice Cream Factory (2/84)
(40) Fighting Ice Hockey (4/84)
(41) Oh Zumou (5/84)
(42) Hello Gate Ball (8/84)
(??) Yellow Cab (84)
(44) Boulder Dash (8/85)
(??) Tokyo Mie Sinryohjyo (10/84)
(??) Tokyo Mie Sinryohyo2 (1/85)
(??) Geinohijin Sikaku Siken (5/85)

 ***********************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class decocass
{
	
	/***************************************************************************
	 *
	 *	write decrypted opcodes
	 *
	 ***************************************************************************/
	
	INLINE int swap_bits_5_6(int data)
	{
		return (data & 0x9f) | ((data & 0x20) << 1) | ((data & 0x40) >> 1);
	}
	
	public static WriteHandlerPtr decocass_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr rom = memory_region(REGION_CPU1);
		int diff = memory_region_length(REGION_CPU1) / 2;
	
		if		(offset <= 0x5fff)					   ;
		else if (offset >= 0x6000 && offset <= 0xbfff) { decocass_charram_w(offset - 0x6000,data); return; }
		else if (offset >= 0xc000 && offset <= 0xc3ff) { decocass_fgvideoram_w(offset - 0xc000,data); return; }
		else if (offset >= 0xc400 && offset <= 0xc7ff) { decocass_colorram_w(offset - 0xc400,data); return; }
		else if (offset >= 0xc800 && offset <= 0xcbff) { decocass_mirrorvideoram_w(offset - 0xc800,data); return; }
		else if (offset >= 0xcc00 && offset <= 0xcfff) { decocass_mirrorcolorram_w(offset - 0xcc00,data); return; }
		else if (offset >= 0xd000 && offset <= 0xd7ff) { decocass_tileram_w(offset - 0xd000,data); return; }
		else if (offset >= 0xd800 && offset <= 0xdbff) { decocass_objectram_w(offset - 0xd800,data); return; }
		else if (offset >= 0xe000 && offset <= 0xe0ff) { decocass_paletteram_w(offset, data); return; }
		else if (offset == 0xe300 ) 				   { decocass_watchdog_count_w(offset - 0xe300,data); return; }
		else if (offset == 0xe301 ) 				   { decocass_watchdog_flip_w(offset - 0xe301,data); return; }
		else if (offset == 0xe302 ) 				   { decocass_color_missiles_w(offset - 0xe302,data); return; }
		else if (offset == 0xe400 ) 				   { decocass_reset_w(offset - 0xe400,data); return; }
		else if (offset == 0xe402 ) 				   { decocass_mode_set_w(offset - 0xe402,data); return; }
		else if (offset == 0xe403 ) 				   { decocass_back_h_shift_w(offset - 0xe403,data); return; }
		else if (offset == 0xe404 ) 				   { decocass_back_vl_shift_w(offset - 0xe404,data); return; }
		else if (offset == 0xe405 ) 				   { decocass_back_vr_shift_w(offset - 0xe405,data); return; }
		else if (offset == 0xe406 ) 				   { decocass_part_h_shift_w(offset - 0xe406,data); return; }
		else if (offset == 0xe407 ) 				   { decocass_part_v_shift_w(offset - 0xe407,data); return; }
		else if (offset == 0xe410 ) 				   { decocass_color_center_bot_w(offset - 0xe410,data); return; }
		else if (offset == 0xe411 ) 				   { decocass_center_h_shift_space_w(offset - 0xe411,data); return; }
		else if (offset == 0xe412 ) 				   { decocass_center_v_shift_w(offset - 0xe412,data); return; }
		else if (offset == 0xe413 ) 				   { decocass_coin_counter_w(offset - 0xe413,data); return; }
		else if (offset == 0xe414 ) 				   { decocass_sound_command_w(offset - 0xe414,data); return; }
		else if (offset >= 0xe415 && offset <= 0xe416) { decocass_quadrature_decoder_reset_w(offset - 0xe415,data); return; }
		else if (offset == 0xe417 ) 				   { decocass_nmi_reset_w(offset - 0xe417,data); return; }
		else if (offset >= 0xe420 && offset <= 0xe42f) { decocass_adc_w(offset - 0xe420,data); return; }
		else if (offset >= 0xe500 && offset <= 0xe5ff) { decocass_e5xx_w(offset - 0xe500,data); return; }
		else if (offset >= 0xf000 && offset <= 0xffff) { return; }
	
		else logerror("CPU #%d PC %04x: warning - write %02x to unmapped memory address %04x\n",cpu_getactivecpu(),cpu_get_pc(),data,offset);
	
		rom[offset] = data;
	
		/* Swap bits 5 & 6 for opcodes */
		rom[offset+diff] = swap_bits_5_6(data);
	} };
	
	public static Memory_ReadAddress decocass_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0x1fff, MRA_RAM ),
		new Memory_ReadAddress( 0x2000, 0xbfff, MRA_RAM ),				/* RMS3 RAM */
		new Memory_ReadAddress( 0xc000, 0xc7ff, MRA_RAM ),				/* DSP3 videoram + colorram */
		new Memory_ReadAddress( 0xc800, 0xcbff, decocass_mirrorvideoram_r ),
		new Memory_ReadAddress( 0xcc00, 0xcfff, decocass_mirrorcolorram_r ),
		new Memory_ReadAddress( 0xd000, 0xdbff, MRA_RAM ),				/* B103 RAM */
		new Memory_ReadAddress( 0xe300, 0xe300, input_port_7_r ), 		/* DSW1 */
		new Memory_ReadAddress( 0xe301, 0xe301, input_port_8_r ), 		/* DSW2 */
		new Memory_ReadAddress( 0xe500, 0xe5ff, decocass_e5xx_r ),		/* read data from 8041/status */
		new Memory_ReadAddress( 0xe600, 0xe6ff, decocass_input_r ),		/* inputs */
		new Memory_ReadAddress( 0xe700, 0xe700, decocass_sound_data_r ),	/* read sound CPU data */
		new Memory_ReadAddress( 0xe701, 0xe701, decocass_sound_ack_r ),	/* read sound CPU ack status */
		new Memory_ReadAddress( 0xf000, 0xffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress decocass_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0xffff, decocass_w ),
	
		new Memory_WriteAddress( 0x0000, 0x1fff, MWA_RAM ),
		new Memory_WriteAddress( 0x2000, 0x5fff, MWA_RAM ),	/* RMS3 RAM */
		new Memory_WriteAddress( 0x6000, 0xbfff, decocass_charram_w, &decocass_charram ), /* still RMS3 RAM */
		new Memory_WriteAddress( 0xc000, 0xc3ff, decocass_fgvideoram_w, &decocass_fgvideoram, &decocass_fgvideoram_size ),  /* DSP3 RAM */
		new Memory_WriteAddress( 0xc400, 0xc7ff, decocass_colorram_w, &decocass_colorram, &decocass_colorram_size ),
		new Memory_WriteAddress( 0xc800, 0xcbff, decocass_mirrorvideoram_w ),
		new Memory_WriteAddress( 0xcc00, 0xcfff, decocass_mirrorcolorram_w ),
		new Memory_WriteAddress( 0xd000, 0xd7ff, decocass_tileram_w, &decocass_tileram, &decocass_tileram_size ),
		new Memory_WriteAddress( 0xd800, 0xdbff, decocass_objectram_w, &decocass_objectram, &decocass_objectram_size ),
		new Memory_WriteAddress( 0xe000, 0xe0ff, decocass_paletteram_w, &paletteram ),
		new Memory_WriteAddress( 0xe300, 0xe300, decocass_watchdog_count_w ),
		new Memory_WriteAddress( 0xe301, 0xe301, decocass_watchdog_flip_w ),
		new Memory_WriteAddress( 0xe302, 0xe302, decocass_color_missiles_w ),
		new Memory_WriteAddress( 0xe400, 0xe400, decocass_reset_w ),
	
	/* BIO-3 board */
		new Memory_WriteAddress( 0xe402, 0xe402, decocass_mode_set_w ),
		new Memory_WriteAddress( 0xe403, 0xe403, decocass_back_h_shift_w ),
		new Memory_WriteAddress( 0xe404, 0xe404, decocass_back_vl_shift_w ),
		new Memory_WriteAddress( 0xe405, 0xe405, decocass_back_vr_shift_w ),
		new Memory_WriteAddress( 0xe406, 0xe406, decocass_part_h_shift_w ),
		new Memory_WriteAddress( 0xe407, 0xe407, decocass_part_v_shift_w ),
	
		new Memory_WriteAddress( 0xe410, 0xe410, decocass_color_center_bot_w ),
		new Memory_WriteAddress( 0xe411, 0xe411, decocass_center_h_shift_space_w ),
		new Memory_WriteAddress( 0xe412, 0xe412, decocass_center_v_shift_w ),
		new Memory_WriteAddress( 0xe413, 0xe413, decocass_coin_counter_w ),
		new Memory_WriteAddress( 0xe414, 0xe414, decocass_sound_command_w ),
		new Memory_WriteAddress( 0xe415, 0xe416, decocass_quadrature_decoder_reset_w ),
		new Memory_WriteAddress( 0xe417, 0xe417, decocass_nmi_reset_w ),
		new Memory_WriteAddress( 0xe420, 0xe42f, decocass_adc_w ),
	
		new Memory_WriteAddress( 0xe500, 0xe5ff, decocass_e5xx_w ),
	
		new Memory_WriteAddress( 0xf000, 0xffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_ReadAddress decocass_sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0x0fff, MRA_RAM ),
		new Memory_ReadAddress( 0x1000, 0x17ff, decocass_sound_nmi_enable_r ),
		new Memory_ReadAddress( 0x1800, 0x1fff, decocass_sound_data_ack_reset_r ),
		new Memory_ReadAddress( 0xa000, 0xafff, decocass_sound_command_r ),
		new Memory_ReadAddress( 0xf800, 0xffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress decocass_sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0x0fff, MWA_RAM ),
		new Memory_WriteAddress( 0x1000, 0x17ff, decocass_sound_nmi_enable_w ),
		new Memory_WriteAddress( 0x1800, 0x1fff, decocass_sound_data_ack_reset_w ),
		new Memory_WriteAddress( 0x2000, 0x2fff, AY8910_write_port_0_w ),
		new Memory_WriteAddress( 0x4000, 0x4fff, AY8910_control_port_0_w ),
		new Memory_WriteAddress( 0x6000, 0x6fff, AY8910_write_port_1_w ),
		new Memory_WriteAddress( 0x8000, 0x8fff, AY8910_control_port_1_w ),
		new Memory_WriteAddress( 0xc000, 0xcfff, decocass_sound_data_w ),
		new Memory_WriteAddress( 0xf800, 0xffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_ReadAddress decocass_mcu_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0x03ff, MRA_ROM ),
		new Memory_ReadAddress( 0x0800, 0x083f, MRA_RAM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress decocass_mcu_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0x03ff, MWA_ROM ),
		new Memory_WriteAddress( 0x0800, 0x083f, MWA_RAM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	public static IO_ReadPort decocass_mcu_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),	new IO_ReadPort( 0x01, 0x01, i8041_p1_r ),
		new IO_ReadPort( 0x02, 0x02, i8041_p2_r ),
	MEMORY_END
	
	public static IO_WritePort decocass_mcu_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),	new IO_WritePort( 0x01, 0x01, i8041_p1_w ),
		new IO_WritePort( 0x02, 0x02, i8041_p2_w ),
	MEMORY_END
	
	static InputPortPtr input_ports_decocass = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH,IPT_JOYSTICK_RIGHT );	PORT_BIT( 0x02, IP_ACTIVE_HIGH,IPT_JOYSTICK_LEFT );	PORT_BIT( 0x04, IP_ACTIVE_HIGH,IPT_JOYSTICK_UP );	PORT_BIT( 0x08, IP_ACTIVE_HIGH,IPT_JOYSTICK_DOWN );	PORT_BIT( 0x10, IP_ACTIVE_HIGH,IPT_BUTTON1 );	PORT_BIT( 0x20, IP_ACTIVE_HIGH,IPT_BUTTON2 );	PORT_BIT( 0x40, IP_ACTIVE_HIGH,IPT_UNUSED );	PORT_BIT( 0x80, IP_ACTIVE_HIGH,IPT_UNUSED );
		PORT_START(); 		/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH,IPT_JOYSTICK_RIGHT | IPF_COCKTAIL );	PORT_BIT( 0x02, IP_ACTIVE_HIGH,IPT_JOYSTICK_LEFT | IPF_COCKTAIL );	PORT_BIT( 0x04, IP_ACTIVE_HIGH,IPT_JOYSTICK_UP | IPF_COCKTAIL );	PORT_BIT( 0x08, IP_ACTIVE_HIGH,IPT_JOYSTICK_DOWN | IPF_COCKTAIL );	PORT_BIT( 0x10, IP_ACTIVE_HIGH,IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT( 0x20, IP_ACTIVE_HIGH,IPT_BUTTON2 | IPF_COCKTAIL );	PORT_BIT( 0x40, IP_ACTIVE_HIGH,IPT_UNUSED );	PORT_BIT( 0x80, IP_ACTIVE_HIGH,IPT_UNUSED );
		PORT_START(); 		/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH,IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_HIGH,IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_HIGH,IPT_UNKNOWN );	PORT_BIT_IMPULSE( 0x08, IP_ACTIVE_HIGH,IPT_START1, 1 );	PORT_BIT_IMPULSE( 0x10, IP_ACTIVE_HIGH,IPT_START2, 1 );	PORT_BIT( 0x20, IP_ACTIVE_HIGH,IPT_UNKNOWN );	PORT_BIT_IMPULSE( 0x40, IP_ACTIVE_LOW, IPT_COIN2, 1 );	PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 );
		PORT_START(); 		/* IN3 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_PLAYER1, 100, 10, 0x10, 0xf0 );
		PORT_START(); 		/* IN4 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_PLAYER1, 100, 10, 0x10, 0xf0 );
		PORT_START(); 		/* IN5 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_PLAYER2, 100, 10, 0x10, 0xf0 );
		PORT_START(); 		/* IN6 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_PLAYER2, 100, 10, 0x10, 0xf0 );
		PORT_START(); 		/* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(	0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(	0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x08, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x30, 0x30, "Board type" );   /* used by the "bios" */
		PORT_DIPSETTING(	0x00, "old" );	PORT_DIPSETTING(	0x10, "invalid?" );	PORT_DIPSETTING(	0x20, "invalid?" );	PORT_DIPSETTING(	0x30, "new" );	PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(	0x40, DEF_STR( "Cocktail") );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK	);
		PORT_START(); 		/* DSW2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x01, "3" );	PORT_DIPSETTING(	0x00, "5" );	PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	0x06, "20000" );	PORT_DIPSETTING(	0x04, "30000" );	PORT_DIPSETTING(	0x02, "40000"  );	PORT_DIPSETTING(	0x00, "50000"  );	PORT_DIPNAME( 0x08, 0x08, "Enemies" );	PORT_DIPSETTING(	0x08, "4" );	PORT_DIPSETTING(	0x00, "6" );	PORT_DIPNAME( 0x10, 0x10, "End of Level Pepper" );	PORT_DIPSETTING(	0x10, DEF_STR( "No") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		1024,	/* 1024 characters */
		3,		/* 3 bits per pixel */
		new int[] { 2*1024*8*8, 1024*8*8, 0 },	/* the bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8 	/* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		256,	/* 256 sprites */
		3,		/* 3 bits per pixel */
		new int[] { 2*256*16*16, 256*16*16, 0 },	/* the bitplanes are separated */
		new int[] { 16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7,
		  0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
		  8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8	/* every sprite takes 32 consecutive bytes */
	);
	
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16,	/* 16*16 characters */
		16+1,	/* 16 tiles (+1 empty tile used in the half-width bg tilemaps) */
		3,	/* 3 bits per pixel */
		new int[] { 2*16*16*16+4, 2*16*16*16+0, 4 },
		new int[] { 3*16*8+0, 3*16*8+1, 3*16*8+2, 3*16*8+3,
		  2*16*8+0, 2*16*8+1, 2*16*8+2, 2*16*8+3,
		  16*8+0, 16*8+1, 16*8+2, 16*8+3,
		  0, 1, 2, 3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
		  8*8, 9*8,10*8,11*8,12*8,13*8,14*8,15*8 },
		2*16*16 /* every tile takes 64 consecutive bytes */
	);
	
	static GfxLayout objlayout = new GfxLayout
	(
		64,64,	/* 64x64 object */
		2,		/* 2 objects */
		1,		/* 1 bits per pixel */
		new int[] { 0 },
		new int[] {
			7*8+0,7*8+1,7*8+2,7*8+3,7*8+4,7*8+5,7*8+6,7*8+7,
			6*8+0,6*8+1,6*8+2,6*8+3,6*8+4,6*8+5,6*8+6,6*8+7,
			5*8+0,5*8+1,5*8+2,5*8+3,5*8+4,5*8+5,5*8+6,5*8+7,
			4*8+0,4*8+1,4*8+2,4*8+3,4*8+4,4*8+5,4*8+6,4*8+7,
			3*8+0,3*8+1,3*8+2,3*8+3,3*8+4,3*8+5,3*8+6,3*8+7,
			2*8+0,2*8+1,2*8+2,2*8+3,2*8+4,2*8+5,2*8+6,2*8+7,
			1*8+0,1*8+1,1*8+2,1*8+3,1*8+4,1*8+5,1*8+6,1*8+7,
			0*8+0,0*8+1,0*8+2,0*8+3,0*8+4,0*8+5,0*8+6,0*8+7
		},
		new int[] {
			63*2*64,62*2*64,61*2*64,60*2*64,59*2*64,58*2*64,57*2*64,56*2*64,
			55*2*64,54*2*64,53*2*64,52*2*64,51*2*64,50*2*64,49*2*64,48*2*64,
			47*2*64,46*2*64,45*2*64,44*2*64,43*2*64,42*2*64,41*2*64,40*2*64,
			39*2*64,38*2*64,37*2*64,36*2*64,35*2*64,34*2*64,33*2*64,32*2*64,
			31*2*64,30*2*64,29*2*64,28*2*64,27*2*64,26*2*64,25*2*64,24*2*64,
			23*2*64,22*2*64,21*2*64,20*2*64,19*2*64,18*2*64,17*2*64,16*2*64,
			15*2*64,14*2*64,13*2*64,12*2*64,11*2*64,10*2*64, 9*2*64, 8*2*64,
			 7*2*64, 6*2*64, 5*2*64, 4*2*64, 3*2*64, 2*2*64, 1*2*64, 0*2*64
		},
		8*8 /* object takes 8 consecutive bytes */
	);
	
	static GfxLayout missilelayout = new GfxLayout
	(
		4,1,	/* 4x1 object ?? */
		1,		/* 1 object */
		1,		/* 1 bits per pixel */
		new int[] { 0 },
		new int[] { 0, 0, 0, 0, 0, 0, 0, 0 },
		new int[] { 0, 0, 0, 0, 0, 0, 0, 0 },
		8	/* object takes a 1 bit from somewhere */
	);
	
	static GfxDecodeInfo decocass_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( 0, 0x6000, charlayout,		 0, 4 ),  /* char set #1 */
		new GfxDecodeInfo( 0, 0x6000, spritelayout, 	 0, 4 ),  /* sprites */
		new GfxDecodeInfo( 0, 0xd000, tilelayout,		32, 2 ),  /* background tiles */
		new GfxDecodeInfo( 0, 0xd800, objlayout,		48, 4 ),  /* object */
		new GfxDecodeInfo( 0, 0xffff, missilelayout,	 0, 8 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		2,		/* 2 chips */
		1500000,		/* 1.5 MHz ? (hand tuned) */
		new int[] { 40, 40 },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	static void decocass_init_palette(UBytePtr sys_palette, unsigned short *sys_colortable,const UBytePtr color_prom)
	{
		int i;
		/* set up 32 colors 1:1 pens */
		for (i = 0; i < 32; i++)
			sys_colortable[i] = i;
	
		/* setup straight/flipped colors for background tiles (D7 of color_center_bot ?) */
		for (i = 0; i < 8; i++)
		{
			sys_colortable[32+i] = 3*8+i;
			sys_colortable[40+i] = 3*8+((i << 1) & 0x04) + ((i >> 1) & 0x02) + (i & 0x01);
		}
	
		/* setup 4 colors for 1bpp object */
		sys_colortable[48+0*2+0] = 0;
		sys_colortable[48+0*2+1] = 25;	/* testtape red from 4th palette section? */
		sys_colortable[48+1*2+0] = 0;
		sys_colortable[48+1*2+1] = 28;	/* testtape blue from 4th palette section? */
		sys_colortable[48+2*2+0] = 0;
		sys_colortable[48+2*2+1] = 26;	/* testtape green from 4th palette section? */
		sys_colortable[48+3*2+0] = 0;
		sys_colortable[48+3*2+1] = 23;	/* ???? */
	}
	
	#define MACHINE_DRIVER_DECOCASS(GAMENAME)	\
	static MachineDriver machine_driver_##GAMENAME = new MachineDriver\
	( \
		/* basic machine hardware */ \
		new MachineCPU[] { \
			new MachineCPU( \
				CPU_M6502, \
				750000, \
				decocass_readmem,decocass_writemem,null,null, \
				ignore_interrupt,0, \
			), \
			new MachineCPU(  \
				CPU_M6502, \
				500000, /* 500 kHz */ \
				decocass_sound_readmem,decocass_sound_writemem,null,null, \
				ignore_interrupt,0, \
			), \
			new MachineCPU( \
				CPU_I8X41, \
				500000, /* 500 kHz ( I doubt it is 400kHz Al! )*/ \
				decocass_mcu_readmem,decocass_mcu_writemem, \
				decocass_mcu_readport,decocass_mcu_writeport, \
				ignore_interrupt,0, \
			), \
		}, \
		57, 3072,		/* frames per second, vblank duration */ \
		7,				/* interleave CPUs */ \
		GAMENAME##_init_machine, \
	\
		/* video hardware */ \
		32*8, 32*8, new rectangle( 1*8, 31*8-1, 1*8, 31*8-1 ), \
		decocass_gfxdecodeinfo, \
		32,32+2*8+2*4, \
		decocass_init_palette, \
	\
		VIDEO_TYPE_RASTER|VIDEO_MODIFIES_PALETTE, \
		null, \
		decocass_vh_start, \
		decocass_vh_stop, \
		decocass_vh_screenrefresh, \
	\
		/* sound hardware */ \
		0,0,0,0, \
		new MachineSound[] { \
			new MachineSound( \
				SOUND_AY8910, \
				ay8910_interface \
			) \
		}, \
	)
	
	MACHINE_DRIVER_DECOCASS( decocass );	/* parent driver */
	MACHINE_DRIVER_DECOCASS( ctsttape );
	MACHINE_DRIVER_DECOCASS( clocknch );
	MACHINE_DRIVER_DECOCASS( ctisland );
	MACHINE_DRIVER_DECOCASS( csuperas );
	MACHINE_DRIVER_DECOCASS( castfant );
	MACHINE_DRIVER_DECOCASS( cluckypo );
	MACHINE_DRIVER_DECOCASS( cterrani );
	MACHINE_DRIVER_DECOCASS( cexplore );
	MACHINE_DRIVER_DECOCASS( cprogolf );
	MACHINE_DRIVER_DECOCASS( cmissnx  );
	MACHINE_DRIVER_DECOCASS( cdiscon1 );
	MACHINE_DRIVER_DECOCASS( cptennis );
	MACHINE_DRIVER_DECOCASS( ctornado );
	MACHINE_DRIVER_DECOCASS( cbnj	  );
	MACHINE_DRIVER_DECOCASS( cburnrub );
	MACHINE_DRIVER_DECOCASS( cbtime   );
	MACHINE_DRIVER_DECOCASS( cgraplop );
	MACHINE_DRIVER_DECOCASS( clapapa  );
	MACHINE_DRIVER_DECOCASS( cfghtice );
	MACHINE_DRIVER_DECOCASS( cprobowl );
	MACHINE_DRIVER_DECOCASS( cnightst );
	MACHINE_DRIVER_DECOCASS( cprosocc );
	MACHINE_DRIVER_DECOCASS( cppicf   );
	MACHINE_DRIVER_DECOCASS( cbdash   );
	MACHINE_DRIVER_DECOCASS( cscrtry );
	
	#define DECOCASS_COMMON_ROMS	\
		ROM_REGION( 2*0x10000, REGION_CPU1, 0 );/* 64k for code + 64k for decrypted opcodes */ \
		ROM_LOAD( "rms8.cpu",     0xf000, 0x1000, 0x23d929b7 );\
	/* the following two are just about the same stuff as the one above */ \
	/*	ROM_LOAD( "dsp3.p0b",     0xf000, 0x0800, 0xb67a91d9 );*/ \
	/*	ROM_LOAD( "dsp3.p1b",     0xf800, 0x0800, 0x3bfff5f3 );*/ \
	\
		ROM_REGION( 0x10000, REGION_CPU2, 0 );  /* 64k for the audio CPU */ \
		ROM_LOAD( "rms8.snd",     0xf800, 0x0800, 0xb66b2c2a );\
	\
		ROM_REGION( 0x01000, REGION_CPU3, 0 );  /* 4k for the MCU (actually 1K ROM + 64 bytes RAM @ 0x800) */ \
		ROM_LOAD( "cass8041.bin", 0x0000, 0x0400, 0xa6df18fd );\
	\
		ROM_REGION( 0x00060, REGION_PROMS, 0 );  /* PROMS */ \
		ROM_LOAD( "dsp8.3m",      0x0000, 0x0020, 0x238fdb40 );\
		ROM_LOAD( "dsp8.10d",     0x0020, 0x0020, 0x3b5836b4 );\
		ROM_LOAD( "rms8.j3",      0x0040, 0x0020, 0x51eef657 );/* DRAM banking and timing */ \
	
	
	static RomLoadPtr rom_decocass = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ctsttape = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x00020, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "testtape.pro", 0x0000, 0x0020, 0xe09ae5de );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "testtape.cas", 0x0000, 0x2000, 0x4f9d8efb );ROM_END(); }}; 
	
	static RomLoadPtr rom_clocknch = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x00020, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "clocknch.pro", 0x0000, 0x0020, 0xe09ae5de );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "clocknch.cas", 0x0000, 0x8000, 0xc9d163a4 );ROM_END(); }}; 
	
	static RomLoadPtr rom_ctisland = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x00020, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "ctisland.pro", 0x0000, 0x0020, 0xe09ae5de );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "ctisland.cas", 0x0000, 0x8000, 0x3f63b8f8 );ROM_END(); }}; 
	
	static RomLoadPtr rom_csuperas = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x00020, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "csuperas.pro", 0x0000, 0x0020, 0xe09ae5de );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "csuperas.cas", 0x0000, 0x8000, 0xfabcd07f );ROM_END(); }}; 
	
	static RomLoadPtr rom_castfant = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x00020, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "castfant.pro", 0x0000, 0x0020, 0xe09ae5de );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "castfant.cas", 0x0000, 0x8000, 0x6d77d1b5 );ROM_END(); }}; 
	
	static RomLoadPtr rom_cluckypo = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x00020, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "cluckypo.pro", 0x0000, 0x0020, 0xe09ae5de );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cluckypo.cas", 0x0000, 0x8000, 0x2070c243 );ROM_END(); }}; 
	
	static RomLoadPtr rom_cterrani = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x00020, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "cterrani.pro", 0x0000, 0x0020, 0xe09ae5de );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cterrani.cas", 0x0000, 0x8000, 0xeb71adbc );ROM_END(); }}; 
	
	static RomLoadPtr rom_cexplore = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x00020, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "cexplore.pro", 0x0000, 0x0020, 0xe09ae5de );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cexplore.cas", 0x0000, 0x8000, 0xfae49c66 );ROM_END(); }}; 
	
	static RomLoadPtr rom_cprogolf = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x00020, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "cprogolf.pro", 0x0000, 0x0020, 0xe09ae5de );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cprogolf.cas", 0x0000, 0x8000, 0x02123cd1 );ROM_END(); }}; 
	
	static RomLoadPtr rom_cmissnx = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x00800, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "cmissnx.pro",  0x0000, 0x0800, 0x8a41c071 );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cmissnx.cas",  0x0000, 0x8000, 0x3a094e11 );ROM_END(); }}; 
	
	static RomLoadPtr rom_cdiscon1 = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x00800, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "cdiscon1.pro", 0x0000, 0x0800, 0x0f793fab );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cdiscon1.cas", 0x0000, 0x8000, 0x1429a397 );ROM_END(); }}; 
	
	static RomLoadPtr rom_cptennis = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x00800, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "cptennis.pro", 0x0000, 0x0800, 0x59b8cede );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cptennis.cas", 0x0000, 0x8000, 0x6bb257fe );ROM_END(); }}; 
	
	static RomLoadPtr rom_ctornado = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x00800, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "ctornado.pro", 0x0000, 0x0800, 0xc9a91697 );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "ctornado.cas", 0x0000, 0x8000, 0xe4e36ce0 );ROM_END(); }}; 
	
	static RomLoadPtr rom_cbnj = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x01000, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "cbnj.pro",       0x0000, 0x1000, 0x9f396832 );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cbnj.cas",       0x0000, 0x8000, 0xeed41560 );ROM_END(); }}; 
	
	static RomLoadPtr rom_cburnrub = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x01000, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "cburnrub.pro",   0x0000, 0x1000, 0x9f396832 );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cburnrub.cas",   0x0000, 0x8000, 0x4528ac22 );ROM_END(); }}; 
	
	static RomLoadPtr rom_cbtime = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x01000, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "cbtime.pro",   0x0000, 0x1000, 0x25bec0f0 );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cbtime.cas",   0x0000, 0x8000, 0x56d7dc58 );ROM_END(); }}; 
	
	static RomLoadPtr rom_cgraplop = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x01000, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "cgraplop.pro", 0x0000, 0x1000, 0xee93787d );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cgraplop.cas", 0x0000, 0x8000, 0xd2c1c1bb );ROM_END(); }}; 
	
	static RomLoadPtr rom_clapapa = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x01000, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "clapapa.pro",  0x0000, 0x1000, 0xe172819a );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "clapapa.cas",  0x0000, 0x8000, 0x4ffbac24 );ROM_END(); }}; 
	
	static RomLoadPtr rom_cfghtice = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x01000, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "cfghtice.pro", 0x0000, 0x1000, 0x5abd27b5 );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cfghtice.cas", 0x0000, 0x10000, 0x906dd7fb );ROM_END(); }}; 
	
	static RomLoadPtr rom_cprobowl = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x01000, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "cprobowl.pro", 0x0000, 0x1000, 0xe3a88e60 );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cprobowl.cas", 0x0000, 0x8000, 0xcb86c5e1 );ROM_END(); }}; 
	
	static RomLoadPtr rom_cnightst = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x01000, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "cnightst.pro", 0x0000, 0x1000, 0x553b0fbc );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cnightst.cas", 0x0000, 0x8000, 0xc6f844cb );ROM_END(); }}; 
	
	static RomLoadPtr rom_cprosocc = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x01000, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "cprosocc.pro", 0x0000, 0x1000,  0x919fabb2 );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cprosocc.cas", 0x0000, 0x10000, 0x76b1ad2c );ROM_END(); }}; 
	
	static RomLoadPtr rom_cppicf = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x01000, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "cppicf.pro",   0x0000, 0x1000, 0x0b1a1ecb );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cppicf.cas",   0x0000, 0x8000, 0x8c02f160 );ROM_END(); }}; 
	
	static RomLoadPtr rom_cscrtry = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
		ROM_REGION( 0x08000, REGION_USER1, 0 );  /* dongle data */
		ROM_LOAD( "cscrtry.pro",  0x0000, 0x8000, 0x7bc3460b );
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cscrtry.cas",  0x0000, 0x8000, 0x5625f0ca );ROM_END(); }}; 
	
	static RomLoadPtr rom_cbdash = new RomLoadPtr(){ public void handler(){ 
		DECOCASS_COMMON_ROMS
	
	/*	ROM_REGION( 0x01000, REGION_USER1, 0 );*/ /* (max) 4k for dongle data */
		/* no proms */
	
		ROM_REGION( 0x10000, REGION_USER2, 0 );  /* (max) 64k for cassette image */
		ROM_LOAD( "cbdash.cas",   0x0000, 0x8000, 0xcba4c1af );ROM_END(); }}; 
	
	static public static InitDriverPtr init_decocass = new InitDriverPtr() { public void handler() 
	{
		int A;
		UBytePtr rom = memory_region(REGION_CPU1);
		int diff = memory_region_length(REGION_CPU1) / 2;
	
		memory_set_opcode_base(0,rom+diff);
	
		/* Swap bits 5 & 6 for opcodes */
		for (A = 0;A < diff;A++)
			rom[A+diff] = swap_bits_5_6(rom[A]);
	} };
	
	public static GameDriver driver_decocass	   = new GameDriver("1981"	,"decocass"	,"decocass.java"	,rom_decocass,null	,machine_driver_decocass	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette System", NOT_A_DRIVER )
	public static GameDriver driver_ctsttape	   = new GameDriver("1981"	,"ctsttape"	,"decocass.java"	,rom_ctsttape,driver_decocass	,machine_driver_ctsttape	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Test Tape" )
	public static GameDriver driver_clocknch	   = new GameDriver("1981"	,"clocknch"	,"decocass.java"	,rom_clocknch,driver_decocass	,machine_driver_clocknch	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Lock'n'Chase" )
	public static GameDriver driver_ctisland	   = new GameDriver("1981"	,"ctisland"	,"decocass.java"	,rom_ctisland,driver_decocass	,machine_driver_ctisland	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Treasure Island", GAME_IMPERFECT_GRAPHICS )
	public static GameDriver driver_csuperas	   = new GameDriver("1981"	,"csuperas"	,"decocass.java"	,rom_csuperas,driver_decocass	,machine_driver_csuperas	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Super Astro Fighter" )
	public static GameDriver driver_castfant	   = new GameDriver("1981"	,"castfant"	,"decocass.java"	,rom_castfant,driver_decocass	,machine_driver_castfant	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Astro Fantasia" )
	public static GameDriver driver_cluckypo	   = new GameDriver("1981"	,"cluckypo"	,"decocass.java"	,rom_cluckypo,driver_decocass	,machine_driver_cluckypo	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Lucky Poker" )
	public static GameDriver driver_cterrani	   = new GameDriver("1981"	,"cterrani"	,"decocass.java"	,rom_cterrani,driver_decocass	,machine_driver_cterrani	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Terranean" )
	public static GameDriver driver_cexplore	   = new GameDriver("1982"	,"cexplore"	,"decocass.java"	,rom_cexplore,driver_decocass	,machine_driver_cexplore	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Explorer", GAME_NOT_WORKING )
	public static GameDriver driver_cprogolf	   = new GameDriver("1981"	,"cprogolf"	,"decocass.java"	,rom_cprogolf,driver_decocass	,machine_driver_cprogolf	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Pro Golf" )
	public static GameDriver driver_cmissnx	   = new GameDriver("1982"	,"cmissnx"	,"decocass.java"	,rom_cmissnx,driver_decocass	,machine_driver_cmissnx	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Mission-X" )
	public static GameDriver driver_cdiscon1	   = new GameDriver("1982"	,"cdiscon1"	,"decocass.java"	,rom_cdiscon1,driver_decocass	,machine_driver_cdiscon1	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Disco No 1" )
	public static GameDriver driver_cptennis	   = new GameDriver("1982"	,"cptennis"	,"decocass.java"	,rom_cptennis,driver_decocass	,machine_driver_cptennis	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Pro Tennis" )
	public static GameDriver driver_ctornado	   = new GameDriver("1982"	,"ctornado"	,"decocass.java"	,rom_ctornado,driver_decocass	,machine_driver_ctornado	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Tornado" )
	public static GameDriver driver_cbnj	   = new GameDriver("1982"	,"cbnj"	,"decocass.java"	,rom_cbnj,driver_decocass	,machine_driver_cbnj	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Bump N Jump" )
	public static GameDriver driver_cbtime	   = new GameDriver("1983"	,"cbtime"	,"decocass.java"	,rom_cbtime,driver_decocass	,machine_driver_cbtime	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Burger Time" )
	public static GameDriver driver_cburnrub	   = new GameDriver("1982"	,"cburnrub"	,"decocass.java"	,rom_cburnrub,driver_decocass	,machine_driver_cburnrub	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Burnin' Rubber" )
	public static GameDriver driver_cgraplop	   = new GameDriver("1983"	,"cgraplop"	,"decocass.java"	,rom_cgraplop,driver_decocass	,machine_driver_cgraplop	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Graplop (aka Cluster Buster)" )
	public static GameDriver driver_clapapa	   = new GameDriver("1983"	,"clapapa"	,"decocass.java"	,rom_clapapa,driver_decocass	,machine_driver_clapapa	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: La.Pa.Pa (aka Rootin' Tootin')" )
	public static GameDriver driver_cfghtice	   = new GameDriver("1984"	,"cfghtice"	,"decocass.java"	,rom_cfghtice,driver_decocass	,machine_driver_cfghtice	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Fighting Ice Hockey" )
	public static GameDriver driver_cprobowl	   = new GameDriver("1983"	,"cprobowl"	,"decocass.java"	,rom_cprobowl,driver_decocass	,machine_driver_cprobowl	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Pro Bowling" )
	public static GameDriver driver_cnightst	   = new GameDriver("1983"	,"cnightst"	,"decocass.java"	,rom_cnightst,driver_decocass	,machine_driver_cnightst	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Night Star" )
	public static GameDriver driver_cprosocc	   = new GameDriver("1983"	,"cprosocc"	,"decocass.java"	,rom_cprosocc,driver_decocass	,machine_driver_cprosocc	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Pro Soccer" )
	public static GameDriver driver_cppicf	   = new GameDriver("1984"	,"cppicf"	,"decocass.java"	,rom_cppicf,driver_decocass	,machine_driver_cppicf	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Peter Pepper's Ice Cream Factory" )
	public static GameDriver driver_cscrtry	   = new GameDriver("1984"	,"cscrtry"	,"decocass.java"	,rom_cscrtry,driver_decocass	,machine_driver_cscrtry	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Scrum Try" )
	public static GameDriver driver_cbdash	   = new GameDriver("1985"	,"cbdash"	,"decocass.java"	,rom_cbdash,driver_decocass	,machine_driver_cbdash	,input_ports_decocass	,init_decocass	,ROT270	,	"DECO", "Cassette: Boulder Dash" )
	
}
