/******************************************************************************

	Game Driver for Nichibutsu Mahjong series.

	Niyanpai
	(c)1996 Nihon Bussan Co.,Ltd.

	Driver by Takahiro Nogi <nogi@kt.rim.or.jp> 2000/12/23 -

******************************************************************************/
/******************************************************************************
Memo:

- TMP68301 emulation is not implemented (machine/m68kfmly.c, .h does nothing).

- niyanpai's 2p start does not mean 2p simultaneous or exchanging play.
  Simply uses controls for 2p side.

- Some games display "GFXROM BANK OVER!!" or "GFXROM ADDRESS OVER!!"
  in Debug build.

- Screen flip is not perfect.

******************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class niyanpai
{
	
	
	#define	SIGNED_DAC	0		// 0:unsigned DAC, 1:signed DAC
	
	
	
	READ16_HANDLER( niyanpai_palette_r );
	WRITE16_HANDLER( niyanpai_palette_w );
	
	WRITE16_HANDLER( niyanpai_gfxflag_0_w );
	WRITE16_HANDLER( niyanpai_scrollx_0_w );
	WRITE16_HANDLER( niyanpai_scrolly_0_w );
	WRITE16_HANDLER( niyanpai_radr_0_w );
	WRITE16_HANDLER( niyanpai_sizex_0_w );
	WRITE16_HANDLER( niyanpai_sizey_0_w );
	WRITE16_HANDLER( niyanpai_drawx_0_w );
	WRITE16_HANDLER( niyanpai_drawy_0_w );
	
	WRITE16_HANDLER( niyanpai_gfxflag_1_w );
	WRITE16_HANDLER( niyanpai_scrollx_1_w );
	WRITE16_HANDLER( niyanpai_scrolly_1_w );
	WRITE16_HANDLER( niyanpai_radr_1_w );
	WRITE16_HANDLER( niyanpai_sizex_1_w );
	WRITE16_HANDLER( niyanpai_sizey_1_w );
	WRITE16_HANDLER( niyanpai_drawx_1_w );
	WRITE16_HANDLER( niyanpai_drawy_1_w );
	
	WRITE16_HANDLER( niyanpai_gfxflag_2_w );
	WRITE16_HANDLER( niyanpai_scrollx_2_w );
	WRITE16_HANDLER( niyanpai_scrolly_2_w );
	WRITE16_HANDLER( niyanpai_radr_2_w );
	WRITE16_HANDLER( niyanpai_sizex_2_w );
	WRITE16_HANDLER( niyanpai_sizey_2_w );
	WRITE16_HANDLER( niyanpai_drawx_2_w );
	WRITE16_HANDLER( niyanpai_drawy_2_w );
	
	WRITE16_HANDLER( niyanpai_paltblnum_0_w );
	WRITE16_HANDLER( niyanpai_paltblnum_1_w );
	WRITE16_HANDLER( niyanpai_paltblnum_2_w );
	WRITE16_HANDLER( niyanpai_paltbl_0_w );
	WRITE16_HANDLER( niyanpai_paltbl_1_w );
	WRITE16_HANDLER( niyanpai_paltbl_2_w );
	
	READ16_HANDLER( niyanpai_gfxbusy_0_r );
	READ16_HANDLER( niyanpai_gfxbusy_1_r );
	READ16_HANDLER( niyanpai_gfxbusy_2_r );
	READ16_HANDLER( niyanpai_gfxrom_0_r );
	READ16_HANDLER( niyanpai_gfxrom_1_r );
	READ16_HANDLER( niyanpai_gfxrom_2_r );
	
	
	static data16_t *niyanpai_nvram;
	static size_t niyanpai_nvram_size;
	
	
	static public static nvramPtr niyanpai_nvram_handler  = new nvramPtr() { public void handler(Object file, int read_or_write) 
	{
		if (read_or_write != 0)
			osd_fwrite(file, niyanpai_nvram, niyanpai_nvram_size);
		else
		{
			if (file != 0)
				osd_fread(file, niyanpai_nvram, niyanpai_nvram_size);
			else
				memset(niyanpai_nvram, 0, niyanpai_nvram_size);
		}
	} };
	
	static void niyanpai_soundbank_w(int data)
	{
		UBytePtr SNDROM = memory_region(REGION_CPU2);
	
		cpu_setbank(1, &SNDROM[0x08000 + (0x8000 * (data & 0x03))]);
	}
	
	static int niyanpai_sound_r(int offset)
	{
		return soundlatch_r(0);
	}
	
	static WRITE16_HANDLER( niyanpai_sound_w )
	{
		soundlatch_w(0, ((data >> 8) & 0xff));
	}
	
	static void niyanpai_soundclr_w(int offset, int data)
	{
		soundlatch_clear_w(0, 0);
	}
	
	
	/* TMPZ84C011 PIO emulation */
	static unsigned char pio_dir[5], pio_latch[5];
	
	static int tmpz84c011_pio_r(int offset)
	{
		int portdata;
	
		switch (offset)
		{
			case	0:			/* PA_0 */
				portdata = 0xff;
				break;
			case	1:			/* PB_0 */
				portdata = 0xff;
				break;
			case	2:			/* PC_0 */
				portdata = 0xff;
				break;
			case	3:			/* PD_0 */
				portdata = niyanpai_sound_r(0);
				break;
			case	4:			/* PE_0 */
				portdata = 0xff;
				break;
	
			default:
				logerror("PC %04X: TMPZ84C011_PIO Unknown Port Read %02X\n", cpu_get_pc(), offset);
				portdata = 0xff;
				break;
		}
	
		return portdata;
	}
	
	static void tmpz84c011_pio_w(int offset, int data)
	{
		switch (offset)
		{
			case	0:			/* PA_0 */
				niyanpai_soundbank_w(data & 0x03);
				break;
			case	1:			/* PB_0 */
	#if SIGNED_DAC
				DAC_1_signed_data_w(0, data);
	#else
				DAC_1_data_w(0, data);
	#endif
				break;
			case	2:			/* PC_0 */
	#if SIGNED_DAC
				DAC_0_signed_data_w(0, data);
	#else
				DAC_0_data_w(0, data);
	#endif
				break;
			case	3:			/* PD_0 */
				break;
			case	4:			/* PE_0 */
				if (!(data & 0x01)) niyanpai_soundclr_w(0, 0);
				break;
	
			default:
				logerror("PC %04X: TMPZ84C011_PIO Unknown Port Write %02X, %02X\n", cpu_get_pc(), offset, data);
				break;
		}
	}
	
	/* CPU interface */
	public static ReadHandlerPtr tmpz84c011_0_pa_r  = new ReadHandlerPtr() { public int handler(int offset) { return (tmpz84c011_pio_r(0) & ~pio_dir[0]) | (pio_latch[0] & pio_dir[0]); } };
	public static ReadHandlerPtr tmpz84c011_0_pb_r  = new ReadHandlerPtr() { public int handler(int offset) { return (tmpz84c011_pio_r(1) & ~pio_dir[1]) | (pio_latch[1] & pio_dir[1]); } };
	public static ReadHandlerPtr tmpz84c011_0_pc_r  = new ReadHandlerPtr() { public int handler(int offset) { return (tmpz84c011_pio_r(2) & ~pio_dir[2]) | (pio_latch[2] & pio_dir[2]); } };
	public static ReadHandlerPtr tmpz84c011_0_pd_r  = new ReadHandlerPtr() { public int handler(int offset) { return (tmpz84c011_pio_r(3) & ~pio_dir[3]) | (pio_latch[3] & pio_dir[3]); } };
	public static ReadHandlerPtr tmpz84c011_0_pe_r  = new ReadHandlerPtr() { public int handler(int offset) { return (tmpz84c011_pio_r(4) & ~pio_dir[4]) | (pio_latch[4] & pio_dir[4]); } };
	
	public static WriteHandlerPtr tmpz84c011_0_pa_w = new WriteHandlerPtr() {public void handler(int offset, int data) { pio_latch[0] = data; tmpz84c011_pio_w(0, data); } };
	public static WriteHandlerPtr tmpz84c011_0_pb_w = new WriteHandlerPtr() {public void handler(int offset, int data) { pio_latch[1] = data; tmpz84c011_pio_w(1, data); } };
	public static WriteHandlerPtr tmpz84c011_0_pc_w = new WriteHandlerPtr() {public void handler(int offset, int data) { pio_latch[2] = data; tmpz84c011_pio_w(2, data); } };
	public static WriteHandlerPtr tmpz84c011_0_pd_w = new WriteHandlerPtr() {public void handler(int offset, int data) { pio_latch[3] = data; tmpz84c011_pio_w(3, data); } };
	public static WriteHandlerPtr tmpz84c011_0_pe_w = new WriteHandlerPtr() {public void handler(int offset, int data) { pio_latch[4] = data; tmpz84c011_pio_w(4, data); } };
	
	public static ReadHandlerPtr tmpz84c011_0_dir_pa_r  = new ReadHandlerPtr() { public int handler(int offset) { return pio_dir[0]; } };
	public static ReadHandlerPtr tmpz84c011_0_dir_pb_r  = new ReadHandlerPtr() { public int handler(int offset) { return pio_dir[1]; } };
	public static ReadHandlerPtr tmpz84c011_0_dir_pc_r  = new ReadHandlerPtr() { public int handler(int offset) { return pio_dir[2]; } };
	public static ReadHandlerPtr tmpz84c011_0_dir_pd_r  = new ReadHandlerPtr() { public int handler(int offset) { return pio_dir[3]; } };
	public static ReadHandlerPtr tmpz84c011_0_dir_pe_r  = new ReadHandlerPtr() { public int handler(int offset) { return pio_dir[4]; } };
	
	public static WriteHandlerPtr tmpz84c011_0_dir_pa_w = new WriteHandlerPtr() {public void handler(int offset, int data) { pio_dir[0] = data; } };
	public static WriteHandlerPtr tmpz84c011_0_dir_pb_w = new WriteHandlerPtr() {public void handler(int offset, int data) { pio_dir[1] = data; } };
	public static WriteHandlerPtr tmpz84c011_0_dir_pc_w = new WriteHandlerPtr() {public void handler(int offset, int data) { pio_dir[2] = data; } };
	public static WriteHandlerPtr tmpz84c011_0_dir_pd_w = new WriteHandlerPtr() {public void handler(int offset, int data) { pio_dir[3] = data; } };
	public static WriteHandlerPtr tmpz84c011_0_dir_pe_w = new WriteHandlerPtr() {public void handler(int offset, int data) { pio_dir[4] = data; } };
	
	
	static void ctc0_interrupt(int state)
	{
		cpu_cause_interrupt(1, Z80_VECTOR(0, state));
	}
	
	static z80ctc_interface ctc_intf =
	{
		1,			/* 1 chip */
		{ 1 },			/* clock */
		{ 0 },			/* timer disables */
		{ ctc0_interrupt },	/* interrupt handler */
		{ z80ctc_0_trg3_w },	/* ZC/TO0 callback ctc1.zc0 . ctc1.trg3 */
		{ 0 },			/* ZC/TO1 callback */
		{ 0 },			/* ZC/TO2 callback */
	};
	
	static void tmpz84c011_init(void)
	{
		int i;
	
		// initialize TMPZ84C011 PIO
		for (i = 0; i < 5; i++)
		{
			pio_dir[i] = pio_latch[i] = 0;
			tmpz84c011_pio_w(i, 0);
		}
	
		// initialize the CTC
		ctc_intf.baseclock[0] = Machine.drv.cpu[1].cpu_clock;
		z80ctc_init(&ctc_intf);
	}
	
	static public static InitMachinePtr niyanpai_init_machine = new InitMachinePtr() { public void handler() 
	{
		//
	} };
	
	static void initialize_driver(void)
	{
		UBytePtr MAINROM = memory_region(REGION_CPU1);
		UBytePtr SNDROM = memory_region(REGION_CPU2);
	
		// main program patch (USR0 . IRQ LEVEL1)
		MAINROM[(25 * 4) + 0] = MAINROM[(64 * 4) + 0];
		MAINROM[(25 * 4) + 1] = MAINROM[(64 * 4) + 1];
		MAINROM[(25 * 4) + 2] = MAINROM[(64 * 4) + 2];
		MAINROM[(25 * 4) + 3] = MAINROM[(64 * 4) + 3];
	
		// sound program patch
		SNDROM[0x0213] = 0x00;			// DI . NOP
	
		// initialize TMPZ84C011 PIO and CTC
		tmpz84c011_init();
	
		// initialize sound rom bank
		niyanpai_soundbank_w(0);
	}
	
	
	static public static InitDriverPtr init_niyanpai = new InitDriverPtr() { public void handler()  { initialize_driver(); } };
	
	
	static READ16_HANDLER( niyanpai_dipsw_r )
	{
		unsigned char dipsw_a, dipsw_b;
	
		dipsw_a = (((readinputport(0) & 0x01) << 7) | ((readinputport(0) & 0x02) << 5) |
			   ((readinputport(0) & 0x04) << 3) | ((readinputport(0) & 0x08) << 1) |
			   ((readinputport(0) & 0x10) >> 1) | ((readinputport(0) & 0x20) >> 3) |
			   ((readinputport(0) & 0x40) >> 5) | ((readinputport(0) & 0x80) >> 7));
	
		dipsw_b = (((readinputport(1) & 0x01) << 7) | ((readinputport(1) & 0x02) << 5) |
			   ((readinputport(1) & 0x04) << 3) | ((readinputport(1) & 0x08) << 1) |
			   ((readinputport(1) & 0x10) >> 1) | ((readinputport(1) & 0x20) >> 3) |
			   ((readinputport(1) & 0x40) >> 5) | ((readinputport(1) & 0x80) >> 7));
	
		return ((dipsw_a << 8) | dipsw_b);
	}
	
	static READ16_HANDLER( niyanpai_inputport_0_r )
	{
		return ((readinputport(3) << 8) | (readinputport(4) << 0));
	}
	
	static READ16_HANDLER( niyanpai_inputport_1_r )
	{
		return ((readinputport(2) << 8) | 0xff);
	}
	
	
	static MEMORY_READ16_START( niyanpai_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x040000, 0x040fff, MRA16_RAM },
	
		{ 0x0a0000, 0x0a08ff, niyanpai_palette_r },
		{ 0x0a0900, 0x0a11ff, MRA16_RAM },		// palette work ram?
	
		{ 0x0bf800, 0x0bffff, MRA16_RAM },
	
		{ 0x240400, 0x240401, niyanpai_gfxbusy_0_r },
		{ 0x240402, 0x240403, niyanpai_gfxrom_0_r },
		{ 0x240600, 0x240601, niyanpai_gfxbusy_1_r },
		{ 0x240402, 0x240403, niyanpai_gfxrom_1_r },
		{ 0x240800, 0x240801, niyanpai_gfxbusy_2_r },
		{ 0x240402, 0x240403, niyanpai_gfxrom_2_r },
	
		{ 0x280000, 0x280001, niyanpai_dipsw_r },
		{ 0x280200, 0x280201, niyanpai_inputport_0_r },
		{ 0x280400, 0x280401, niyanpai_inputport_1_r },
	
		{ 0xfffc00, 0xfffc0f, tmp68301_address_decoder_r },
		{ 0xfffc80, 0xfffc9f, tmp68301_interrupt_controller_r },
		{ 0xfffd00, 0xfffd0f, tmp68301_parallel_interface_r },
		{ 0xfffd80, 0xfffdaf, tmp68301_serial_interface_r },
		{ 0xfffe00, 0xfffe4f, tmp68301_timer_r },
	MEMORY_END
	
	static MEMORY_WRITE16_START( niyanpai_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x040000, 0x040fff, MWA16_RAM, &niyanpai_nvram, &niyanpai_nvram_size },
	
		{ 0x0a0000, 0x0a08ff, niyanpai_palette_w },
		{ 0x0a0900, 0x0a11ff, MWA16_RAM },		// palette work ram?
	
		{ 0x0bf800, 0x0bffff, MWA16_RAM },
	
		{ 0x200000, 0x200001, niyanpai_sound_w },
	
		{ 0x200200, 0x200201, MWA16_NOP },		// unknown
		{ 0x240000, 0x240009, MWA16_NOP },		// unknown
		{ 0x240200, 0x2403ff, MWA16_NOP },		// unknown
	
		{ 0x240400, 0x240401, niyanpai_gfxflag_0_w },
		{ 0x240402, 0x240405, niyanpai_scrollx_0_w },
		{ 0x240406, 0x240409, niyanpai_scrolly_0_w },
		{ 0x24040a, 0x24040f, niyanpai_radr_0_w },
		{ 0x240410, 0x240411, niyanpai_sizex_0_w },
		{ 0x240412, 0x240413, niyanpai_sizey_0_w },
		{ 0x240414, 0x240417, niyanpai_drawx_0_w },
		{ 0x240418, 0x24041b, niyanpai_drawy_0_w },
	
		{ 0x240420, 0x24043f, niyanpai_paltbl_0_w },
	
		{ 0x240600, 0x240601, niyanpai_gfxflag_1_w },
		{ 0x240602, 0x240605, niyanpai_scrollx_1_w },
		{ 0x240606, 0x240609, niyanpai_scrolly_1_w },
		{ 0x24060a, 0x24060f, niyanpai_radr_1_w },
		{ 0x240610, 0x240611, niyanpai_sizex_1_w },
		{ 0x240612, 0x240613, niyanpai_sizey_1_w },
		{ 0x240614, 0x240617, niyanpai_drawx_1_w },
		{ 0x240618, 0x24061b, niyanpai_drawy_1_w },
	
		{ 0x240620, 0x24063f, niyanpai_paltbl_1_w },
	
		{ 0x240800, 0x240801, niyanpai_gfxflag_2_w },
		{ 0x240802, 0x240805, niyanpai_scrollx_2_w },
		{ 0x240806, 0x240809, niyanpai_scrolly_2_w },
		{ 0x24080a, 0x24080f, niyanpai_radr_2_w },
		{ 0x240810, 0x240811, niyanpai_sizex_2_w },
		{ 0x240812, 0x240813, niyanpai_sizey_2_w },
		{ 0x240814, 0x240817, niyanpai_drawx_2_w },
		{ 0x240818, 0x24081b, niyanpai_drawy_2_w },
	
		{ 0x240820, 0x24083f, niyanpai_paltbl_2_w },
	
		{ 0x240a00, 0x240a01, niyanpai_paltblnum_0_w },
		{ 0x240c00, 0x240c01, niyanpai_paltblnum_1_w },
		{ 0x240e00, 0x240e01, niyanpai_paltblnum_2_w },
	
		{ 0xfffc00, 0xfffc0f, tmp68301_address_decoder_w },
		{ 0xfffc80, 0xfffc9f, tmp68301_interrupt_controller_w },
		{ 0xfffd00, 0xfffd0f, tmp68301_parallel_interface_w },
		{ 0xfffd80, 0xfffdaf, tmp68301_serial_interface_w },
		{ 0xfffe00, 0xfffe4f, tmp68301_timer_w },
	MEMORY_END
	
	
	public static Memory_ReadAddress sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0x77ff, MRA_ROM ),
		new Memory_ReadAddress( 0x7800, 0x7fff, MRA_RAM ),
		new Memory_ReadAddress( 0x8000, 0xffff, MRA_BANK1 ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0x77ff, MWA_ROM ),
		new Memory_WriteAddress( 0x7800, 0x7fff, MWA_RAM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort sound_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),	new IO_ReadPort( 0x10, 0x13, z80ctc_0_r ),
		new IO_ReadPort( 0x50, 0x50, tmpz84c011_0_pa_r ),
		new IO_ReadPort( 0x51, 0x51, tmpz84c011_0_pb_r ),
		new IO_ReadPort( 0x52, 0x52, tmpz84c011_0_pc_r ),
		new IO_ReadPort( 0x30, 0x30, tmpz84c011_0_pd_r ),
		new IO_ReadPort( 0x40, 0x40, tmpz84c011_0_pe_r ),
		new IO_ReadPort( 0x54, 0x54, tmpz84c011_0_dir_pa_r ),
		new IO_ReadPort( 0x55, 0x55, tmpz84c011_0_dir_pb_r ),
		new IO_ReadPort( 0x56, 0x56, tmpz84c011_0_dir_pc_r ),
		new IO_ReadPort( 0x34, 0x34, tmpz84c011_0_dir_pd_r ),
		new IO_ReadPort( 0x44, 0x44, tmpz84c011_0_dir_pe_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	public static IO_WritePort sound_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),	new IO_WritePort( 0x10, 0x13, z80ctc_0_w ),
		new IO_WritePort( 0x50, 0x50, tmpz84c011_0_pa_w ),
		new IO_WritePort( 0x51, 0x51, tmpz84c011_0_pb_w ),
		new IO_WritePort( 0x52, 0x52, tmpz84c011_0_pc_w ),
		new IO_WritePort( 0x30, 0x30, tmpz84c011_0_pd_w ),
		new IO_WritePort( 0x40, 0x40, tmpz84c011_0_pe_w ),
		new IO_WritePort( 0x54, 0x54, tmpz84c011_0_dir_pa_w ),
		new IO_WritePort( 0x55, 0x55, tmpz84c011_0_dir_pb_w ),
		new IO_WritePort( 0x56, 0x56, tmpz84c011_0_dir_pc_w ),
		new IO_WritePort( 0x34, 0x34, tmpz84c011_0_dir_pd_w ),
		new IO_WritePort( 0x44, 0x44, tmpz84c011_0_dir_pe_w ),
		new IO_WritePort( 0x80, 0x80, YM3812_control_port_0_w ),
		new IO_WritePort( 0x81, 0x81, YM3812_write_port_0_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	static InputPortPtr input_ports_niyanpai = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* (0) DIPSW-A */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x03, "1" );	PORT_DIPSETTING(    0x02, "2" );	PORT_DIPSETTING(    0x01, "3" );	PORT_DIPSETTING(    0x00, "4" );	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, "Game Sounds" );	PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Cocktail") );
	
		PORT_START(); 	/* (1) DIPSW-B */
		PORT_DIPNAME( 0x01, 0x00, "Nudity" );	PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0x7e, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_DIPNAME( 0x80, 0x80, "Graphic ROM Test" );	PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* (2) PORT 0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );	// COIN1
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );	// COIN2
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START3 );	// CREDIT CLEAR
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START2 );	// START2
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_SERVICE2 );	// ANALYZER
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START1 );	// START1
	//	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_SERVICE3 );	// MEMORY RESET
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	// ?
	 	PORT_SERVICE( 0x80, IP_ACTIVE_LOW );		// TEST
	
		PORT_START(); 	/* (3) PLAYER-1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	/* (4) PLAYER-2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	
	public static InterruptPtr niyanpai_interrupt = new InterruptPtr() { public int handler() 
	{
		return m68_level1_irq();
	} };
	
	static Z80_DaisyChain daisy_chain_sound[] =
	{
		{ z80ctc_reset, z80ctc_interrupt, z80ctc_reti, 0 },	/* device 0 = CTC_1 */
		{ 0, 0, 0, -1 }		/* end mark */
	};
	
	
	static YM3812interface ym3812_interface = new YM3812interface
	(
		1,				/* 1 chip */
		4000000,			/* 4.00 MHz */
		new int[] { 35 }
	);
	
	static DACinterface dac_interface = new DACinterface
	(
		2,				/* 2 channels */
		new int[] { 50, 75 },
	);
	
	
	static MachineDriver machine_driver_niyanpai = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,		/* TMP68301 */
				12288000/2,		/* 6.144 MHz */
			/*	12288000/1,	*/	/* 12.288 MHz */
				niyanpai_readmem, niyanpai_writemem, 0, null,
				niyanpai_interrupt, 1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,/* TMPZ84C011 */
			/*	8000000/2,	*/	/* 4.00 MHz */ \
				8000000/1,		/* 8.00 MHz */ \
				sound_readmem, sound_writemem, sound_readport, sound_writeport,
				0, 0,	/* interrupts are made by z80 daisy chain system */
				0, 0, daisy_chain_sound
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
		1,
		niyanpai_init_machine,
	
		/* video hardware */
		1024, 512, new rectangle( 0, 640-1, 0, 240-1 ),
		null,
		768, 768,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_PIXEL_ASPECT_RATIO_1_2,
		null,
		niyanpai_vh_start,
		niyanpai_vh_stop,
		niyanpai_vh_screenrefresh,
	
		/* sound hardware */
		0, 0, 0, 0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM3812,
				ym3812_interface
			),
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		},
		niyanpai_nvram_handler
	);
	
	
	static RomLoadPtr rom_niyanpai = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 );/* TMP68301 main program */
		ROM_LOAD16_BYTE( "npai_01.bin", 0x00000, 0x20000, 0xa904e8a1 )
		ROM_LOAD16_BYTE( "npai_02.bin", 0x00001, 0x20000, 0x244f9d6f )
	
		ROM_REGION( 0x20000, REGION_CPU2, 0 );/* TMPZ84C011 sound program */
		ROM_LOAD( "npai_03.bin", 0x000000, 0x20000, 0xd154306b );
		ROM_REGION( 0x400000, REGION_GFX1, 0 );/* gfx */
		ROM_LOAD( "npai_04.bin", 0x000000, 0x80000, 0xbec845b5 );	ROM_LOAD( "npai_05.bin", 0x080000, 0x80000, 0x3300ce07 );	ROM_LOAD( "npai_06.bin", 0x100000, 0x80000, 0x448e4e39 );	ROM_LOAD( "npai_07.bin", 0x180000, 0x80000, 0x2ad47e55 );	ROM_LOAD( "npai_08.bin", 0x200000, 0x80000, 0x2ff980a0 );	ROM_LOAD( "npai_09.bin", 0x280000, 0x80000, 0x74037ee3 );	ROM_LOAD( "npai_10.bin", 0x300000, 0x80000, 0xd35a9af6 );	ROM_LOAD( "npai_11.bin", 0x380000, 0x80000, 0x0748eb73 );ROM_END(); }}; 
	
	
	//     YEAR,     NAME,   PARENT,  MACHINE,    INPUT,     INIT,    MONITOR, COMPANY, FULLNAME, FLAGS
	public static GameDriver driver_niyanpai	   = new GameDriver("1996"	,"niyanpai"	,"niyanpai.java"	,rom_niyanpai,null	,machine_driver_niyanpai	,input_ports_niyanpai	,init_niyanpai	,ROT0_16BIT	,	"Nichibutsu", "Niyanpai (Japan)", 0 )
}
