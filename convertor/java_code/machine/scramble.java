/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package machine;

public class scramble
{
	
	
	
	
	public static InitMachinePtr scramble_init_machine = new InitMachinePtr() { public void handler() 
	{
		/* we must start with NMI interrupts disabled, otherwise some games */
		/* (e.g. Lost Tomb, Rescue) will not pass the startup test. */
		cpu_interrupt_enable(0,0);
	
		if (cpu_gettotalcpu() == 2)
		{
			scramble_sh_init();
		}
	} };
	
	
	public static ReadHandlerPtr scrambls_input_port_2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int res;
	
	
		res = readinputport(2);
	
	/*logerror("%04x: read IN2\n",cpu_get_pc());*/
	
		/* avoid protection */
		if (cpu_get_pc() == 0x00e4) res &= 0x7f;
	
		return res;
	} };
	
	public static ReadHandlerPtr ckongs_input_port_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (readinputport(1) & 0xfc) | ((readinputport(2) & 0x06) >> 1);
	} };
	
	public static ReadHandlerPtr ckongs_input_port_2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (readinputport(2) & 0xf9) | ((readinputport(1) & 0x03) << 1);
	} };
	
	
	static int moonwar_port_select;
	
	public static WriteHandlerPtr moonwar_port_select_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		moonwar_port_select = data & 0x10;
	} };
	
	public static ReadHandlerPtr moonwar_input_port_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int sign;
		int delta;
	
		delta = (moonwar_port_select ? readinputport(3) : readinputport(4));
	
		sign = (delta & 0x80) >> 3;
		delta &= 0x0f;
	
		return ((readinputport(0) & 0xe0) | delta | sign );
	} };
	
	
	/* the coinage DIPs are spread accross two input ports */
	public static ReadHandlerPtr stratgyx_input_port_2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (readinputport(2) & ~0x06) | ((readinputport(4) << 1) & 0x06);
	} };
	
	public static ReadHandlerPtr stratgyx_input_port_3_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (readinputport(3) & ~0x03) | ((readinputport(4) >> 2) & 0x03);
	} };
	
	
	public static ReadHandlerPtr darkplnt_input_port_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		static UINT8 remap[] = {0x03, 0x02, 0x00, 0x01, 0x21, 0x20, 0x22, 0x23,
							    0x33, 0x32, 0x30, 0x31, 0x11, 0x10, 0x12, 0x13,
							    0x17, 0x16, 0x14, 0x15, 0x35, 0x34, 0x36, 0x37,
							    0x3f, 0x3e, 0x3c, 0x3d, 0x1d, 0x1c, 0x1e, 0x1f,
							    0x1b, 0x1a, 0x18, 0x19, 0x39, 0x38, 0x3a, 0x3b,
							    0x2b, 0x2a, 0x28, 0x29, 0x09, 0x08, 0x0a, 0x0b,
							    0x0f, 0x0e, 0x0c, 0x0d, 0x2d, 0x2c, 0x2e, 0x2f,
							    0x27, 0x26, 0x24, 0x25, 0x05, 0x04, 0x06, 0x07 };
		int val;
	
		val = readinputport(1);
	
		return ((val & 0x03) | (remap[val >> 2] << 2));
	} };
	
	
	
	public static WriteHandlerPtr scramble_protection_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* nothing to do yet */
	} };
	
	public static ReadHandlerPtr scramble_protection_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (cpu_get_pc())
		{
		case 0x00a8: return 0xf0;
		case 0x00be: return 0xb0;
		case 0x0c1d: return 0xf0;
		case 0x0c6a: return 0xb0;
		case 0x0ceb: return 0x40;
		case 0x0d37: return 0x60;
		case 0x1ca2: return 0x00;  /* I don't think it's checked */
		case 0x1d7e: return 0xb0;
		default:
			logerror("%04x: read protection\n",cpu_get_pc());
			return 0;
		}
	} };
	
	public static ReadHandlerPtr scrambls_protection_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		logerror("%04x: read protection\n",cpu_get_pc());
	
		return 0x6f;
	} };
	
	public static ReadHandlerPtr scramblb_protection_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (cpu_get_pc())
		{
		case 0x01da: return 0x80;
		case 0x01e4: return 0x00;
		default:
			logerror("%04x: read protection 1\n",cpu_get_pc());
			return 0;
		}
	} };
	
	public static ReadHandlerPtr scramblb_protection_2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (cpu_get_pc())
		{
		case 0x01ca: return 0x90;
		default:
			logerror("%04x: read protection 2\n",cpu_get_pc());
			return 0;
		}
	} };
	
	
	public static WriteHandlerPtr theend_coin_counter_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		coin_counter_w.handler(0, data & 0x80);
	} };
	
	
	public static ReadHandlerPtr mariner_protection_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return 7;
	} };
	
	public static ReadHandlerPtr mariner_protection_2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return 3;
	} };
	
	
	public static ReadHandlerPtr triplep_pip_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		logerror("PC %04x: read port 2\n",cpu_get_pc());
		if (cpu_get_pc() == 0x015a) return 0xff;
		else if (cpu_get_pc() == 0x0886) return 0x05;
		else return 0;
	} };
	
	public static ReadHandlerPtr triplep_pap_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		logerror("PC %04x: read port 3\n",cpu_get_pc());
		if (cpu_get_pc() == 0x015d) return 0x04;
		else return 0;
	} };
	
	
	static void cavelon_banksw(void)
	{
		/* any read/write access in the 0x8000-0xffff region causes a bank switch.
		   Only the lower 0x2000 is switched but we switch the whole region
		   to keep the CPU core happy at the boundaries */
	
		static int cavelon_bank;
	
		UBytePtr ROM = memory_region(REGION_CPU1);
	
		if (cavelon_bank != 0)
		{
			cavelon_bank = 0;
			cpu_setbank(1, &ROM[0x0000]);
		}
		else
		{
			cavelon_bank = 1;
			cpu_setbank(1, &ROM[0x10000]);
		}
	}
	
	public static ReadHandlerPtr cavelon_banksw_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		cavelon_banksw();
	
		if      ((offset >= 0x0100) && (offset <= 0x0103))
			return ppi8255_0_r(offset - 0x0100);
		else if ((offset >= 0x0200) && (offset <= 0x0203))
			return ppi8255_1_r(offset - 0x0200);
	
		return 0xff;
	} };
	
	public static WriteHandlerPtr cavelon_banksw_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		cavelon_banksw();
	
		if      ((offset >= 0x0100) && (offset <= 0x0103))
			ppi8255_0_w(offset - 0x0100, data);
		else if ((offset >= 0x0200) && (offset <= 0x0203))
			ppi8255_1_w(offset - 0x0200, data);
	} };
	
	
	public static ReadHandlerPtr frogger_ppi8255_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ppi8255_0_r(offset >> 1);
	} };
	
	public static ReadHandlerPtr frogger_ppi8255_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ppi8255_1_r(offset >> 1);
	} };
	
	public static WriteHandlerPtr frogger_ppi8255_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ppi8255_0_w(offset >> 1, data);
	} };
	
	public static WriteHandlerPtr frogger_ppi8255_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ppi8255_1_w(offset >> 1, data);
	} };
	
	
	public static ReadHandlerPtr scobra_type2_ppi8255_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ppi8255_0_r(offset >> 2);
	} };
	
	public static ReadHandlerPtr scobra_type2_ppi8255_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ppi8255_1_r(offset >> 2);
	} };
	
	public static WriteHandlerPtr scobra_type2_ppi8255_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ppi8255_0_w(offset >> 2, data);
	} };
	
	public static WriteHandlerPtr scobra_type2_ppi8255_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ppi8255_1_w(offset >> 2, data);
	} };
	
	
	public static ReadHandlerPtr hustler_ppi8255_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ppi8255_0_r(offset >> 3);
	} };
	
	public static ReadHandlerPtr hustler_ppi8255_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ppi8255_1_r(offset >> 3);
	} };
	
	public static WriteHandlerPtr hustler_ppi8255_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ppi8255_0_w(offset >> 3, data);
	} };
	
	public static WriteHandlerPtr hustler_ppi8255_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ppi8255_1_w(offset >> 3, data);
	} };
	
	
	public static ReadHandlerPtr amidar_ppi8255_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ppi8255_0_r(offset >> 4);
	} };
	
	public static ReadHandlerPtr amidar_ppi8255_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ppi8255_1_r(offset >> 4);
	} };
	
	public static WriteHandlerPtr amidar_ppi8255_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ppi8255_0_w(offset >> 4, data);
	} };
	
	public static WriteHandlerPtr amidar_ppi8255_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ppi8255_1_w(offset >> 4, data);
	} };
	
	
	public static ReadHandlerPtr mars_ppi8255_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ppi8255_0_r(((offset >> 2) & 0x02) | ((offset >> 1) & 0x01));
	} };
	
	public static ReadHandlerPtr mars_ppi8255_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ppi8255_1_r(((offset >> 2) & 0x02) | ((offset >> 1) & 0x01));
	} };
	
	public static WriteHandlerPtr mars_ppi8255_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ppi8255_0_w(((offset >> 2) & 0x02) | ((offset >> 1) & 0x01), data);
	} };
	
	public static WriteHandlerPtr mars_ppi8255_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ppi8255_1_w(((offset >> 2) & 0x02) | ((offset >> 1) & 0x01), data);
	} };
	
	
	static ppi8255_interface ppi8255_intf =
	{
		2, 								/* 2 chips */
		{input_port_0_r, 0},			/* Port A read */
		{input_port_1_r, 0},			/* Port B read */
		{input_port_2_r, 0},			/* Port C read */
		{0, soundlatch_w},				/* Port A write */
		{0, scramble_sh_irqtrigger_w},	/* Port B write */
		{0, 0}, 						/* Port C write */
	};
	
	
	public static InitDriverPtr init_scobra = new InitDriverPtr() { public void handler() 
	{
		ppi8255_init(&ppi8255_intf);
	} };
	
	public static InitDriverPtr init_scramble = new InitDriverPtr() { public void handler() 
	{
		init_scobra();
	
		ppi8255_set_portCread (1, scramble_protection_r);
		ppi8255_set_portCwrite(1, scramble_protection_w);
	} };
	
	public static InitDriverPtr init_scrambls = new InitDriverPtr() { public void handler() 
	{
		init_scobra();
	
		ppi8255_set_portCread(0, scrambls_input_port_2_r);
		ppi8255_set_portCread(1, scrambls_protection_r);
		ppi8255_set_portCwrite(1, scramble_protection_w);
	} };
	
	public static InitDriverPtr init_theend = new InitDriverPtr() { public void handler() 
	{
		init_scobra();
	
		ppi8255_set_portCwrite(0, theend_coin_counter_w);
	} };
	
	public static InitDriverPtr init_stratgyx = new InitDriverPtr() { public void handler() 
	{
		init_scobra();
	
		install_mem_write_handler(0, 0xb000, 0xb000, scramble_background_green_w);
		install_mem_write_handler(0, 0xb00a, 0xb00a, scramble_background_red_w);
	
		ppi8255_set_portCread(0, stratgyx_input_port_2_r);
		ppi8255_set_portCread(1, stratgyx_input_port_3_r);
	} };
	
	public static InitDriverPtr init_amidar = new InitDriverPtr() { public void handler() 
	{
		init_scobra();
	
		/* Amidar has a the DIP switches connected to port C of the 2nd 8255 */
		ppi8255_set_portCread(1, input_port_3_r);
	} };
	
	public static InitDriverPtr init_ckongs = new InitDriverPtr() { public void handler() 
	{
		init_scobra();
	
		ppi8255_set_portBread(0, ckongs_input_port_1_r);
		ppi8255_set_portCread(0, ckongs_input_port_2_r);
	} };
	
	public static InitDriverPtr init_mariner = new InitDriverPtr() { public void handler() 
	{
		init_scobra();
	
		/* extra ROM */
		install_mem_read_handler (0, 0x5800, 0x67ff, MRA_ROM);
		install_mem_write_handler(0, 0x5800, 0x67ff, MWA_ROM);
	
		install_mem_read_handler(0, 0x9008, 0x9008, mariner_protection_2_r);
		install_mem_read_handler(0, 0xb401, 0xb401, mariner_protection_1_r);
	
		/* ??? (it's NOT a background enable) */
		install_mem_write_handler(0, 0x6803, 0x6803, MWA_NOP);
	} };
	
	public static InitDriverPtr init_frogger = new InitDriverPtr() { public void handler() 
	{
		int A;
		UBytePtr rom;
	
	
		init_scobra();
	
	
		/* the first ROM of the second CPU has data lines D0 and D1 swapped. Decode it. */
		rom = memory_region(REGION_CPU2);
		for (A = 0;A < 0x0800;A++)
			rom[A] = BITSWAP8(rom[A],7,6,5,4,3,2,0,1);
	
		/* likewise, the 2nd gfx ROM has data lines D0 and D1 swapped. Decode it. */
		rom = memory_region(REGION_GFX1);
		for (A = 0x0800;A < 0x1000;A++)
			rom[A] = BITSWAP8(rom[A],7,6,5,4,3,2,0,1);
	} };
	
	public static InitDriverPtr init_froggers = new InitDriverPtr() { public void handler() 
	{
		int A;
		UBytePtr rom;
	
	
		init_scobra();
	
		/* the first ROM of the second CPU has data lines D0 and D1 swapped. Decode it. */
		rom = memory_region(REGION_CPU2);
		for (A = 0;A < 0x0800;A++)
			rom[A] = BITSWAP8(rom[A],7,6,5,4,3,2,0,1);
	} };
	
	public static InitDriverPtr init_mars = new InitDriverPtr() { public void handler() 
	{
		int i;
		UBytePtr RAM;
	
	
		init_scobra();
	
	
		ppi8255_set_portCread(1, input_port_3_r);
	
	
		/* Address lines are scrambled on the main CPU:
	
			A0 . A2
			A1 . A0
			A2 . A3
			A3 . A1 */
	
		RAM = memory_region(REGION_CPU1);
		for (i = 0; i < 0x10000; i += 16)
		{
			int j;
			unsigned char swapbuffer[16];
	
			for (j = 0; j < 16; j++)
			{
				swapbuffer[j] = RAM[i + ((j & 1) << 2) + ((j & 2) >> 1) + ((j & 4) << 1) + ((j & 8) >> 2)];
			}
	
			memcpy(&RAM[i], swapbuffer, 16);
		}
	} };
	
	public static InitDriverPtr init_hotshock = new InitDriverPtr() { public void handler() 
	{
		/* protection??? The game jumps into never-neverland here. I think
		   it just expects a RET there */
		memory_region(REGION_CPU1)[0x2ef9] = 0xc9;
	} };
	
	public static InitDriverPtr init_cavelon = new InitDriverPtr() { public void handler() 
	{
		init_scobra();
	
		/* banked ROM */
		install_mem_read_handler(0, 0x0000, 0x3fff, MRA_BANK1);
	
		/* A15 switches memory banks */
		install_mem_read_handler (0, 0x8000, 0xffff, cavelon_banksw_r);
		install_mem_write_handler(0, 0x8000, 0xffff, cavelon_banksw_w);
	
		install_mem_write_handler(0, 0x2000, 0x2000, MWA_NOP);	/* ??? */
		install_mem_write_handler(0, 0x3800, 0x3801, MWA_NOP);  /* looks suspicously like
																   an AY8910, but not sure */
	} };
	
	public static InitDriverPtr init_moonwar = new InitDriverPtr() { public void handler() 
	{
		init_scobra();
	
		/* special handler for the spinner */
		ppi8255_set_portAread (0, moonwar_input_port_0_r);
		ppi8255_set_portCwrite(0, moonwar_port_select_w);
	} };
	
	public static InitDriverPtr init_darkplnt = new InitDriverPtr() { public void handler() 
	{
		ppi8255_init(&ppi8255_intf);
	
		/* special handler for the spinner */
		ppi8255_set_portBread(0, darkplnt_input_port_1_r);
	
		install_mem_write_handler(0, 0xb00a, 0xb00a, darkplnt_bullet_color_w);
	} };
	
	
	static int bit(int i,int n)
	{
		return ((i >> n) & 1);
	}
	
	
	public static InitDriverPtr init_anteater = new InitDriverPtr() { public void handler() 
	{
		int i;
		UBytePtr RAM;
		UBytePtr scratch;
	
	
		init_scobra();
	
		/*
		*   Code To Decode Lost Tomb by Mirko Buffoni
		*   Optimizations done by Fabio Buffoni
		*/
	
		RAM = memory_region(REGION_GFX1);
	
		scratch = malloc(memory_region_length(REGION_GFX1));
	
		if (scratch != 0)
		{
			memcpy(scratch, RAM, memory_region_length(REGION_GFX1));
	
			for (i = 0; i < memory_region_length(REGION_GFX1); i++)
			{
				int j;
	
	
				j = i & 0x9bf;
				j |= ( bit(i,4) ^ bit(i,9) ^ ( bit(i,2) & bit(i,10) ) ) << 6;
				j |= ( bit(i,2) ^ bit(i,10) ) << 9;
				j |= ( bit(i,0) ^ bit(i,6) ^ 1 ) << 10;
	
				RAM[i] = scratch[j];
			}
	
			free(scratch);
		}
	} };
	
	public static InitDriverPtr init_rescue = new InitDriverPtr() { public void handler() 
	{
		int i;
		UBytePtr RAM;
		UBytePtr scratch;
	
	
		init_scobra();
	
		/*
		*   Code To Decode Lost Tomb by Mirko Buffoni
		*   Optimizations done by Fabio Buffoni
		*/
	
		RAM = memory_region(REGION_GFX1);
	
		scratch = malloc(memory_region_length(REGION_GFX1));
	
		if (scratch != 0)
		{
			memcpy(scratch, RAM, memory_region_length(REGION_GFX1));
	
			for (i = 0; i < memory_region_length(REGION_GFX1); i++)
			{
				int j;
	
	
				j = i & 0xa7f;
				j |= ( bit(i,3) ^ bit(i,10) ) << 7;
				j |= ( bit(i,1) ^ bit(i,7) ) << 8;
				j |= ( bit(i,0) ^ bit(i,8) ) << 10;
	
				RAM[i] = scratch[j];
			}
	
			free(scratch);
		}
	} };
	
	public static InitDriverPtr init_minefld = new InitDriverPtr() { public void handler() 
	{
		int i;
		UBytePtr RAM;
		UBytePtr scratch;
	
	
		init_scobra();
	
		/*
		*   Code To Decode Minefield by Mike Balfour and Nicola Salmoria
		*/
	
		RAM = memory_region(REGION_GFX1);
	
		scratch = malloc(memory_region_length(REGION_GFX1));
	
		if (scratch != 0)
		{
			memcpy(scratch, RAM, memory_region_length(REGION_GFX1));
	
			for (i = 0; i < memory_region_length(REGION_GFX1); i++)
			{
				int j;
	
	
				j  = i & 0xd5f;
				j |= ( bit(i,3) ^ bit(i,7) ) << 5;
				j |= ( bit(i,2) ^ bit(i,9) ^ ( bit(i,0) & bit(i,5) ) ^
					 ( bit(i,3) & bit(i,7) & ( bit(i,0) ^ bit(i,5) ))) << 7;
				j |= ( bit(i,0) ^ bit(i,5) ^ ( bit(i,3) & bit(i,7) ) ) << 9;
	
				RAM[i] = scratch[j];
			}
	
			free(scratch);
		}
	} };
	
	public static InitDriverPtr init_losttomb = new InitDriverPtr() { public void handler() 
	{
		int i;
		UBytePtr RAM;
		UBytePtr scratch;
	
	
		init_scobra();
	
		/*
		*   Code To Decode Lost Tomb by Mirko Buffoni
		*   Optimizations done by Fabio Buffoni
		*/
	
		RAM = memory_region(REGION_GFX1);
	
		scratch = malloc(memory_region_length(REGION_GFX1));
	
		if (scratch != 0)
		{
			memcpy(scratch, RAM, memory_region_length(REGION_GFX1));
	
			for (i = 0; i < memory_region_length(REGION_GFX1); i++)
			{
				int j;
	
	
				j = i & 0xa7f;
				j |= ( (bit(i,1) & bit(i,8)) | ((1 ^ bit(i,1)) & (bit(i,10)))) << 7;
				j |= ( bit(i,7) ^ (bit(i,1) & ( bit(i,7) ^ bit(i,10) ))) << 8;
				j |= ( (bit(i,1) & bit(i,7)) | ((1 ^ bit(i,1)) & (bit(i,8)))) << 10;
	
				RAM[i] = scratch[j];
			}
	
			free(scratch);
		}
	} };
	
	public static InitDriverPtr init_superbon = new InitDriverPtr() { public void handler() 
	{
		int i;
		UBytePtr RAM;
	
	
		init_scobra();
	
		/*
		*   Code rom deryption worked out by hand by Chris Hardy.
		*/
	
		RAM = memory_region(REGION_CPU1);
	
		for (i = 0;i < 0x1000;i++)
		{
			/* Code is encrypted depending on bit 7 and 9 of the address */
			switch (i & 0x0280)
			{
			case 0x0000:
				RAM[i] ^= 0x92;
				break;
			case 0x0080:
				RAM[i] ^= 0x82;
				break;
			case 0x0200:
				RAM[i] ^= 0x12;
				break;
			case 0x0280:
				RAM[i] ^= 0x10;
				break;
			}
		}
	} };
	
	
	public static InitDriverPtr init_hustler = new InitDriverPtr() { public void handler() 
	{
		int A;
	
	
		init_scobra();
	
	
		for (A = 0;A < 0x4000;A++)
		{
			unsigned char xormask;
			int bits[8];
			int i;
			UBytePtr RAM = memory_region(REGION_CPU1);
	
	
			for (i = 0;i < 8;i++)
				bits[i] = (A >> i) & 1;
	
			xormask = 0xff;
			if (bits[0] ^ bits[1]) xormask ^= 0x01;
			if (bits[3] ^ bits[6]) xormask ^= 0x02;
			if (bits[4] ^ bits[5]) xormask ^= 0x04;
			if (bits[0] ^ bits[2]) xormask ^= 0x08;
			if (bits[2] ^ bits[3]) xormask ^= 0x10;
			if (bits[1] ^ bits[5]) xormask ^= 0x20;
			if (bits[0] ^ bits[7]) xormask ^= 0x40;
			if (bits[4] ^ bits[6]) xormask ^= 0x80;
	
			RAM[A] ^= xormask;
		}
	
		/* the first ROM of the second CPU has data lines D0 and D1 swapped. Decode it. */
		{
			UBytePtr RAM = memory_region(REGION_CPU2);
	
	
			for (A = 0;A < 0x0800;A++)
				RAM[A] = (RAM[A] & 0xfc) | ((RAM[A] & 1) << 1) | ((RAM[A] & 2) >> 1);
		}
	} };
	
	public static InitDriverPtr init_billiard = new InitDriverPtr() { public void handler() 
	{
		int A;
	
	
		init_scobra();
	
	
		for (A = 0;A < 0x4000;A++)
		{
			unsigned char xormask;
			int bits[8];
			int i;
			UBytePtr RAM = memory_region(REGION_CPU1);
	
	
			for (i = 0;i < 8;i++)
				bits[i] = (A >> i) & 1;
	
			xormask = 0x55;
			if (bits[2] ^ ( bits[3] &  bits[6])) xormask ^= 0x01;
			if (bits[4] ^ ( bits[5] &  bits[7])) xormask ^= 0x02;
			if (bits[0] ^ ( bits[7] & !bits[3])) xormask ^= 0x04;
			if (bits[3] ^ (!bits[0] &  bits[2])) xormask ^= 0x08;
			if (bits[5] ^ (!bits[4] &  bits[1])) xormask ^= 0x10;
			if (bits[6] ^ (!bits[2] & !bits[5])) xormask ^= 0x20;
			if (bits[1] ^ (!bits[6] & !bits[4])) xormask ^= 0x40;
			if (bits[7] ^ (!bits[1] &  bits[0])) xormask ^= 0x80;
	
			RAM[A] ^= xormask;
	
			for (i = 0;i < 8;i++)
				bits[i] = (RAM[A] >> i) & 1;
	
			RAM[A] =
				(bits[7] << 0) +
				(bits[0] << 1) +
				(bits[3] << 2) +
				(bits[4] << 3) +
				(bits[5] << 4) +
				(bits[2] << 5) +
				(bits[1] << 6) +
				(bits[6] << 7);
		}
	
		/* the first ROM of the second CPU has data lines D0 and D1 swapped. Decode it. */
		{
			UBytePtr RAM = memory_region(REGION_CPU2);
	
	
			for (A = 0;A < 0x0800;A++)
				RAM[A] = (RAM[A] & 0xfc) | ((RAM[A] & 1) << 1) | ((RAM[A] & 2) >> 1);
		}
	} };
}
