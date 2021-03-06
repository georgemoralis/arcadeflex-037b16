/***************************************************************************

Fast Lane(GX752) (c) 1987 Konami

Driver by Manuel Abadia <manu@teleline.es>

TODO:
- verify that sound is correct (volume and bank switching)

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.drivers;

import common.ptr.UBytePtr;
import static gr.codebb.arcadeflex.WIP.v037b16.cpu.hd6309.hd6309H.*;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.fastlane.*;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b16.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b16.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b16.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b16.mame.memory.*;
import static gr.codebb.arcadeflex.v037b16.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b16.mame.palette.*;
import static gr.codebb.arcadeflex.v037b16.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b16.sound.k007232.*;
import static gr.codebb.arcadeflex.v037b16.sound.k007232H.*;
import static mame037b5.sound.mixerH.*;

public class fastlane
{
		
	public static InterruptPtr fastlane_interrupt = new InterruptPtr() { public int handler() 
	{
		if (cpu_getiloops() == 0)
		{
			if ((K007121_ctrlram[0][0x07] & 0x02)!=0) return HD6309_INT_IRQ;
		}
		else if ((cpu_getiloops() % 2) != 0)
		{
			if ((K007121_ctrlram[0][0x07] & 0x01)!=0) return nmi_interrupt.handler();
		}
		return ignore_interrupt.handler();
	} };
	
	public static WriteHandlerPtr k007121_registers_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset < 8)
			K007121_ctrl_0_w.handler(offset,data);
		else	/* scroll registers */
			fastlane_k007121_regs.write(offset, data);
	} };
	
	public static WriteHandlerPtr fastlane_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int bankaddress;
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		/* bits 0 & 1 coin counters */
		coin_counter_w(0,data & 0x01);
		coin_counter_w(1,data & 0x02);
	
		/* bits 2 & 3 = bank number */
		bankaddress = 0x10000 + ((data & 0x0c) >> 2) * 0x4000;
		cpu_setbank(1,new UBytePtr(RAM, bankaddress));
	
		/* bit 4: bank # for the 007232 (chip 2) */
		RAM = memory_region(REGION_SOUND2);
		K007232_bankswitch(1,new UBytePtr(RAM, 0x40000*((data & 0x10) >> 4)),new UBytePtr(RAM, 0x40000*((data & 0x10) >> 4)));
	
		/* other bits seems to be unused */
	} };
	
	/* Read and write handlers for one K007232 chip:
	   even and odd register are mapped swapped */
	
	public static ReadHandlerPtr fastlane_K007232_read_port_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K007232_read_port_0_r.handler(offset ^ 1);
	} };
	public static WriteHandlerPtr fastlane_K007232_write_port_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		K007232_write_port_0_w.handler(offset ^ 1, data);
	} };
	public static ReadHandlerPtr fastlane_K007232_read_port_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K007232_read_port_1_r.handler(offset ^ 1);
	} };
	public static WriteHandlerPtr fastlane_K007232_write_port_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		K007232_write_port_1_w.handler(offset ^ 1, data);
	} };
	
	
	
	public static Memory_ReadAddress fastlane_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0x005f, MRA_RAM ),
		new Memory_ReadAddress( 0x0800, 0x0800, input_port_2_r ), 	/* DIPSW #3 */
		new Memory_ReadAddress( 0x0801, 0x0801, input_port_5_r ), 	/* 2P inputs */
		new Memory_ReadAddress( 0x0802, 0x0802, input_port_4_r ), 	/* 1P inputs */
		new Memory_ReadAddress( 0x0803, 0x0803, input_port_3_r ), 	/* COINSW */
		new Memory_ReadAddress( 0x0900, 0x0900, input_port_0_r ), 	/* DIPSW #1 */
		new Memory_ReadAddress( 0x0901, 0x0901, input_port_1_r ), 	/* DISPW #2 */
		new Memory_ReadAddress( 0x0d00, 0x0d0d, fastlane_K007232_read_port_0_r ),/* 007232 registers (chip 1) */
		new Memory_ReadAddress( 0x0e00, 0x0e0d, fastlane_K007232_read_port_1_r ),/* 007232 registers (chip 2) */
		new Memory_ReadAddress( 0x0f00, 0x0f1f, K051733_r ),			/* 051733 (protection) */
		new Memory_ReadAddress( 0x1000, 0x1fff, MRA_RAM ),			/* Palette RAM/Work RAM */
		new Memory_ReadAddress( 0x2000, 0x3fff, MRA_RAM ),			/* Video RAM + Sprite RAM */
		new Memory_ReadAddress( 0x4000, 0x7fff, MRA_BANK1 ),			/* banked ROM */
		new Memory_ReadAddress( 0x8000, 0xffff, MRA_ROM ),			/* ROM */
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	public static Memory_WriteAddress fastlane_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0x005f, k007121_registers_w, fastlane_k007121_regs ),/* 007121 registers */
		new Memory_WriteAddress( 0x0b00, 0x0b00, watchdog_reset_w ),		/* watchdog reset */
		new Memory_WriteAddress( 0x0c00, 0x0c00, fastlane_bankswitch_w ),	/* bankswitch control */
		new Memory_WriteAddress( 0x0d00, 0x0d0d, fastlane_K007232_write_port_0_w ),	/* 007232 registers (chip 1) */
		new Memory_WriteAddress( 0x0e00, 0x0e0d, fastlane_K007232_write_port_1_w ),	/* 007232 registers (chip 2) */
		new Memory_WriteAddress( 0x0f00, 0x0f1f, K051733_w ),				/* 051733 (protection) */
		new Memory_WriteAddress( 0x1000, 0x17ff, paletteram_xBBBBBGGGGGRRRRR_swap_w, paletteram ),/* palette RAM */
		new Memory_WriteAddress( 0x1800, 0x1fff, MWA_RAM ),				/* Work RAM */
		new Memory_WriteAddress( 0x2000, 0x27ff, fastlane_vram1_w, fastlane_videoram1 ),
		new Memory_WriteAddress( 0x2800, 0x2fff, fastlane_vram2_w, fastlane_videoram2 ),
		new Memory_WriteAddress( 0x3000, 0x3fff, MWA_RAM, spriteram ),	/* Sprite RAM */
		new Memory_WriteAddress( 0x4000, 0xffff, MWA_ROM ),				/* ROM/banked ROM */
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	/***************************************************************************
	
		Input Ports
	
	***************************************************************************/
	
	static InputPortPtr input_ports_fastlane = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* DSW #1 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(	0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(	0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(	0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(	0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(	0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(	0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(	0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(	0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(	0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(	0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(	0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(	0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(	0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(	0x90, DEF_STR( "1C_7C") );
	//	PORT_DIPSETTING(	0x00, "Invalid" );
		PORT_START(); 	/* DSW #2 */
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x03, "2" );	PORT_DIPSETTING(	0x02, "3" );	PORT_DIPSETTING(	0x01, "4" );	PORT_DIPSETTING(	0x00, "7" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		/* The bonus life affects the starting high score too, 20000 or 30000 */
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	0x18, "20000 100000" );	PORT_DIPSETTING(	0x10, "30000 150000" );	PORT_DIPSETTING(	0x08, "20000" );	PORT_DIPSETTING(	0x00, "30000" );	PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW #3 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(	0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START(); 	/* COINSW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );/* service */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START(); 	/* PLAYER 1 INPUTS */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP	  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START(); 	/* PLAYER 2 INPUTS */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP	  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	static GfxLayout gfxlayout = new GfxLayout
	(
		8,8,
		0x80000/32,
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 2*4, 3*4, 0*4, 1*4, 6*4, 7*4, 4*4, 5*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, gfxlayout, 0, 64*16 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	/***************************************************************************
	
		Machine Driver
	
	***************************************************************************/
	
	static portwritehandlerPtr volume_callback0 = new portwritehandlerPtr() {
            @Override
            public void handler(int v) {
		K007232_set_volume(0,0,(v >> 4) * 0x11,0);
		K007232_set_volume(0,1,0,(v & 0x0f) * 0x11);
            }
        };
	
	static portwritehandlerPtr volume_callback1 = new portwritehandlerPtr() {
            @Override
            public void handler(int v) {
                K007232_set_volume(1,0,(v >> 4) * 0x11,0);
		K007232_set_volume(1,1,0,(v & 0x0f) * 0x11);
            }
        };
        
	static K007232_interface k007232_interface = new K007232_interface
	(
		2,			/* number of chips */
		new int[]{ REGION_SOUND1, REGION_SOUND2 },	/* memory regions */
		new int[]{ K007232_VOL(50,MIXER_PAN_CENTER,50,MIXER_PAN_CENTER),
				K007232_VOL(50,MIXER_PAN_LEFT,50,MIXER_PAN_RIGHT) },	/* volume */
		new portwritehandlerPtr[]{ volume_callback0,  volume_callback1 } /* external port callback */
	);
	
	static MachineDriver machine_driver_fastlane = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_HD6309,
				3000000,		/* 24MHz/8? */
				fastlane_readmem,fastlane_writemem,null,null,
				fastlane_interrupt,16	/* 1 IRQ + ??? NMI (generated by the 007121) */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,
		1,
		null,
	
		/* video hardware */
		37*8, 32*8, new rectangle( 0*8, 35*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		1024, 1024*16,
		fastlane_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		fastlane_vh_start,
		null,
		fastlane_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_K007232,
				k007232_interface
			)
		}
	);
	
	
	/***************************************************************************
	
	  Game ROMs
	
	***************************************************************************/
	
	static RomLoadPtr rom_fastlane = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x21000, REGION_CPU1, 0 );/* code + banked roms */
		ROM_LOAD( "752_m02.9h",  0x08000, 0x08000, 0xe1004489 ); /* fixed ROM */
		ROM_LOAD( "752_e01.10h", 0x10000, 0x10000, 0xff4d6029 ); /* banked ROM */
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );	ROM_LOAD( "752e04.2i",   0x00000, 0x80000, 0xa126e82d ); /* tiles + sprites */
	
		ROM_REGION( 0x0100, REGION_PROMS, 0 );	ROM_LOAD( "752e03.6h",   0x0000, 0x0100, 0x44300aeb );
		ROM_REGION( 0x20000, REGION_SOUND1, 0 );/* 007232 data */
		ROM_LOAD( "752e06.4c",   0x00000, 0x20000, 0x85d691ed );/* chip 1 */
	
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* 007232 data */
		ROM_LOAD( "752e05.12b",  0x00000, 0x80000, 0x119e9cbf );/* chip 2 */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_fastlane	   = new GameDriver("1987"	,"fastlane"	,"fastlane.java"	,rom_fastlane,null	,machine_driver_fastlane	,input_ports_fastlane	,null	,ROT90	,	"Konami", "Fast Lane", GAME_NOT_WORKING | GAME_IMPERFECT_COLORS );
	
}
