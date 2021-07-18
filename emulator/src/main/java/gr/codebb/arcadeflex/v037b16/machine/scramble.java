/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b16.machine;

//generic imports
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
//to be organized
import static arcadeflex036.osdepend.logerror;

public class scramble {

    /*TODO*///	public static InitMachinePtr scramble_init_machine = new InitMachinePtr() { public void handler() 
/*TODO*///	{
/*TODO*///		/* we must start with NMI interrupts disabled, otherwise some games */
/*TODO*///		/* (e.g. Lost Tomb, Rescue) will not pass the startup test. */
/*TODO*///		cpu_interrupt_enable(0,0);
/*TODO*///	
/*TODO*///		if (cpu_gettotalcpu() == 2)
/*TODO*///		{
/*TODO*///			scramble_sh_init();
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr scrambls_input_port_2_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int res;
/*TODO*///	
/*TODO*///	
/*TODO*///		res = readinputport(2);
/*TODO*///	
/*TODO*///	/*logerror("%04x: read IN2\n",cpu_get_pc());*/
/*TODO*///	
/*TODO*///		/* avoid protection */
/*TODO*///		if (cpu_get_pc() == 0x00e4) res &= 0x7f;
/*TODO*///	
/*TODO*///		return res;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr ckongs_input_port_1_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return (readinputport(1) & 0xfc) | ((readinputport(2) & 0x06) >> 1);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr ckongs_input_port_2_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return (readinputport(2) & 0xf9) | ((readinputport(1) & 0x03) << 1);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	static int moonwar_port_select;
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr moonwar_port_select_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		moonwar_port_select = data & 0x10;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr moonwar_input_port_0_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int sign;
/*TODO*///		int delta;
/*TODO*///	
/*TODO*///		delta = (moonwar_port_select ? readinputport(3) : readinputport(4));
/*TODO*///	
/*TODO*///		sign = (delta & 0x80) >> 3;
/*TODO*///		delta &= 0x0f;
/*TODO*///	
/*TODO*///		return ((readinputport(0) & 0xe0) | delta | sign );
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	/* the coinage DIPs are spread accross two input ports */
/*TODO*///	public static ReadHandlerPtr stratgyx_input_port_2_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return (readinputport(2) & ~0x06) | ((readinputport(4) << 1) & 0x06);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr stratgyx_input_port_3_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return (readinputport(3) & ~0x03) | ((readinputport(4) >> 2) & 0x03);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr darkplnt_input_port_1_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		static UINT8 remap[] = {0x03, 0x02, 0x00, 0x01, 0x21, 0x20, 0x22, 0x23,
/*TODO*///							    0x33, 0x32, 0x30, 0x31, 0x11, 0x10, 0x12, 0x13,
/*TODO*///							    0x17, 0x16, 0x14, 0x15, 0x35, 0x34, 0x36, 0x37,
/*TODO*///							    0x3f, 0x3e, 0x3c, 0x3d, 0x1d, 0x1c, 0x1e, 0x1f,
/*TODO*///							    0x1b, 0x1a, 0x18, 0x19, 0x39, 0x38, 0x3a, 0x3b,
/*TODO*///							    0x2b, 0x2a, 0x28, 0x29, 0x09, 0x08, 0x0a, 0x0b,
/*TODO*///							    0x0f, 0x0e, 0x0c, 0x0d, 0x2d, 0x2c, 0x2e, 0x2f,
/*TODO*///							    0x27, 0x26, 0x24, 0x25, 0x05, 0x04, 0x06, 0x07 };
/*TODO*///		int val;
/*TODO*///	
/*TODO*///		val = readinputport(1);
/*TODO*///	
/*TODO*///		return ((val & 0x03) | (remap[val >> 2] << 2));
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr scramble_protection_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		/* nothing to do yet */
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr scramble_protection_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		switch (cpu_get_pc())
/*TODO*///		{
/*TODO*///		case 0x00a8: return 0xf0;
/*TODO*///		case 0x00be: return 0xb0;
/*TODO*///		case 0x0c1d: return 0xf0;
/*TODO*///		case 0x0c6a: return 0xb0;
/*TODO*///		case 0x0ceb: return 0x40;
/*TODO*///		case 0x0d37: return 0x60;
/*TODO*///		case 0x1ca2: return 0x00;  /* I don't think it's checked */
/*TODO*///		case 0x1d7e: return 0xb0;
/*TODO*///		default:
/*TODO*///			logerror("%04x: read protection\n",cpu_get_pc());
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr scrambls_protection_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		logerror("%04x: read protection\n",cpu_get_pc());
/*TODO*///	
/*TODO*///		return 0x6f;
/*TODO*///	} };

    public static ReadHandlerPtr scramblb_protection_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (cpu_get_pc()) {
                case 0x01da:
                    return 0x80;
                case 0x01e4:
                    return 0x00;
                default:
                    logerror("%04x: read protection 1\n", cpu_get_pc());
                    return 0;
            }
        }
    };

    public static ReadHandlerPtr scramblb_protection_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (cpu_get_pc()) {
                case 0x01ca:
                    return 0x90;
                default:
                    logerror("%04x: read protection 2\n", cpu_get_pc());
                    return 0;
            }
        }
    };

    /*TODO*///	
/*TODO*///	public static WriteHandlerPtr theend_coin_counter_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		coin_counter_w.handler(0, data & 0x80);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr mariner_protection_1_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return 7;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr mariner_protection_2_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return 3;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr triplep_pip_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		logerror("PC %04x: read port 2\n",cpu_get_pc());
/*TODO*///		if (cpu_get_pc() == 0x015a) return 0xff;
/*TODO*///		else if (cpu_get_pc() == 0x0886) return 0x05;
/*TODO*///		else return 0;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr triplep_pap_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		logerror("PC %04x: read port 3\n",cpu_get_pc());
/*TODO*///		if (cpu_get_pc() == 0x015d) return 0x04;
/*TODO*///		else return 0;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	static void cavelon_banksw(void)
/*TODO*///	{
/*TODO*///		/* any read/write access in the 0x8000-0xffff region causes a bank switch.
/*TODO*///		   Only the lower 0x2000 is switched but we switch the whole region
/*TODO*///		   to keep the CPU core happy at the boundaries */
/*TODO*///	
/*TODO*///		static int cavelon_bank;
/*TODO*///	
/*TODO*///		UBytePtr ROM = memory_region(REGION_CPU1);
/*TODO*///	
/*TODO*///		if (cavelon_bank != 0)
/*TODO*///		{
/*TODO*///			cavelon_bank = 0;
/*TODO*///			cpu_setbank(1, &ROM[0x0000]);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			cavelon_bank = 1;
/*TODO*///			cpu_setbank(1, &ROM[0x10000]);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr cavelon_banksw_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		cavelon_banksw();
/*TODO*///	
/*TODO*///		if      ((offset >= 0x0100) && (offset <= 0x0103))
/*TODO*///			return ppi8255_0_r(offset - 0x0100);
/*TODO*///		else if ((offset >= 0x0200) && (offset <= 0x0203))
/*TODO*///			return ppi8255_1_r(offset - 0x0200);
/*TODO*///	
/*TODO*///		return 0xff;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr cavelon_banksw_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		cavelon_banksw();
/*TODO*///	
/*TODO*///		if      ((offset >= 0x0100) && (offset <= 0x0103))
/*TODO*///			ppi8255_0_w(offset - 0x0100, data);
/*TODO*///		else if ((offset >= 0x0200) && (offset <= 0x0203))
/*TODO*///			ppi8255_1_w(offset - 0x0200, data);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	READ_HANDLER(frogger_ppi8255_0_r)
/*TODO*///	{
/*TODO*///		return ppi8255_0_r(offset >> 1);
/*TODO*///	}
/*TODO*///	
/*TODO*///	READ_HANDLER(frogger_ppi8255_1_r)
/*TODO*///	{
/*TODO*///		return ppi8255_1_r(offset >> 1);
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE_HANDLER(frogger_ppi8255_0_w)
/*TODO*///	{
/*TODO*///		ppi8255_0_w(offset >> 1, data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE_HANDLER(frogger_ppi8255_1_w)
/*TODO*///	{
/*TODO*///		ppi8255_1_w(offset >> 1, data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	READ_HANDLER(scobra_type2_ppi8255_0_r)
/*TODO*///	{
/*TODO*///		return ppi8255_0_r(offset >> 2);
/*TODO*///	}
/*TODO*///	
/*TODO*///	READ_HANDLER(scobra_type2_ppi8255_1_r)
/*TODO*///	{
/*TODO*///		return ppi8255_1_r(offset >> 2);
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE_HANDLER(scobra_type2_ppi8255_0_w)
/*TODO*///	{
/*TODO*///		ppi8255_0_w(offset >> 2, data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE_HANDLER(scobra_type2_ppi8255_1_w)
/*TODO*///	{
/*TODO*///		ppi8255_1_w(offset >> 2, data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	READ_HANDLER(hustler_ppi8255_0_r)
/*TODO*///	{
/*TODO*///		return ppi8255_0_r(offset >> 3);
/*TODO*///	}
/*TODO*///	
/*TODO*///	READ_HANDLER(hustler_ppi8255_1_r)
/*TODO*///	{
/*TODO*///		return ppi8255_1_r(offset >> 3);
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE_HANDLER(hustler_ppi8255_0_w)
/*TODO*///	{
/*TODO*///		ppi8255_0_w(offset >> 3, data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE_HANDLER(hustler_ppi8255_1_w)
/*TODO*///	{
/*TODO*///		ppi8255_1_w(offset >> 3, data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	READ_HANDLER(amidar_ppi8255_0_r)
/*TODO*///	{
/*TODO*///		return ppi8255_0_r(offset >> 4);
/*TODO*///	}
/*TODO*///	
/*TODO*///	READ_HANDLER(amidar_ppi8255_1_r)
/*TODO*///	{
/*TODO*///		return ppi8255_1_r(offset >> 4);
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE_HANDLER(amidar_ppi8255_0_w)
/*TODO*///	{
/*TODO*///		ppi8255_0_w(offset >> 4, data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE_HANDLER(amidar_ppi8255_1_w)
/*TODO*///	{
/*TODO*///		ppi8255_1_w(offset >> 4, data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	READ_HANDLER(mars_ppi8255_0_r)
/*TODO*///	{
/*TODO*///		return ppi8255_0_r(((offset >> 2) & 0x02) | ((offset >> 1) & 0x01));
/*TODO*///	}
/*TODO*///	
/*TODO*///	READ_HANDLER(mars_ppi8255_1_r)
/*TODO*///	{
/*TODO*///		return ppi8255_1_r(((offset >> 2) & 0x02) | ((offset >> 1) & 0x01));
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE_HANDLER(mars_ppi8255_0_w)
/*TODO*///	{
/*TODO*///		ppi8255_0_w(((offset >> 2) & 0x02) | ((offset >> 1) & 0x01), data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE_HANDLER(mars_ppi8255_1_w)
/*TODO*///	{
/*TODO*///		ppi8255_1_w(((offset >> 2) & 0x02) | ((offset >> 1) & 0x01), data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static ppi8255_interface ppi8255_intf =
/*TODO*///	{
/*TODO*///		2, 								/* 2 chips */
/*TODO*///		{input_port_0_r, 0},			/* Port A read */
/*TODO*///		{input_port_1_r, 0},			/* Port B read */
/*TODO*///		{input_port_2_r, 0},			/* Port C read */
/*TODO*///		{0, soundlatch_w},				/* Port A write */
/*TODO*///		{0, scramble_sh_irqtrigger_w},	/* Port B write */
/*TODO*///		{0, 0}, 						/* Port C write */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_scobra = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		ppi8255_init(&ppi8255_intf);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_scramble = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///		ppi8255_set_portCread (1, scramble_protection_r);
/*TODO*///		ppi8255_set_portCwrite(1, scramble_protection_w);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_scrambls = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///		ppi8255_set_portCread(0, scrambls_input_port_2_r);
/*TODO*///		ppi8255_set_portCread(1, scrambls_protection_r);
/*TODO*///		ppi8255_set_portCwrite(1, scramble_protection_w);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_theend = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///		ppi8255_set_portCwrite(0, theend_coin_counter_w);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_stratgyx = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///		install_mem_write_handler(0, 0xb000, 0xb000, scramble_background_green_w);
/*TODO*///		install_mem_write_handler(0, 0xb00a, 0xb00a, scramble_background_red_w);
/*TODO*///	
/*TODO*///		ppi8255_set_portCread(0, stratgyx_input_port_2_r);
/*TODO*///		ppi8255_set_portCread(1, stratgyx_input_port_3_r);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_amidar = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///		/* Amidar has a the DIP switches connected to port C of the 2nd 8255 */
/*TODO*///		ppi8255_set_portCread(1, input_port_3_r);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_ckongs = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///		ppi8255_set_portBread(0, ckongs_input_port_1_r);
/*TODO*///		ppi8255_set_portCread(0, ckongs_input_port_2_r);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_mariner = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///		/* extra ROM */
/*TODO*///		install_mem_read_handler (0, 0x5800, 0x67ff, MRA_ROM);
/*TODO*///		install_mem_write_handler(0, 0x5800, 0x67ff, MWA_ROM);
/*TODO*///	
/*TODO*///		install_mem_read_handler(0, 0x9008, 0x9008, mariner_protection_2_r);
/*TODO*///		install_mem_read_handler(0, 0xb401, 0xb401, mariner_protection_1_r);
/*TODO*///	
/*TODO*///		/* ??? (it's NOT a background enable) */
/*TODO*///		install_mem_write_handler(0, 0x6803, 0x6803, MWA_NOP);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_frogger = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		int A;
/*TODO*///		UBytePtr rom;
/*TODO*///	
/*TODO*///	
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///	
/*TODO*///		/* the first ROM of the second CPU has data lines D0 and D1 swapped. Decode it. */
/*TODO*///		rom = memory_region(REGION_CPU2);
/*TODO*///		for (A = 0;A < 0x0800;A++)
/*TODO*///			rom[A] = BITSWAP8(rom[A],7,6,5,4,3,2,0,1);
/*TODO*///	
/*TODO*///		/* likewise, the 2nd gfx ROM has data lines D0 and D1 swapped. Decode it. */
/*TODO*///		rom = memory_region(REGION_GFX1);
/*TODO*///		for (A = 0x0800;A < 0x1000;A++)
/*TODO*///			rom[A] = BITSWAP8(rom[A],7,6,5,4,3,2,0,1);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_froggers = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		int A;
/*TODO*///		UBytePtr rom;
/*TODO*///	
/*TODO*///	
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///		/* the first ROM of the second CPU has data lines D0 and D1 swapped. Decode it. */
/*TODO*///		rom = memory_region(REGION_CPU2);
/*TODO*///		for (A = 0;A < 0x0800;A++)
/*TODO*///			rom[A] = BITSWAP8(rom[A],7,6,5,4,3,2,0,1);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_mars = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///		UBytePtr RAM;
/*TODO*///	
/*TODO*///	
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///	
/*TODO*///		ppi8255_set_portCread(1, input_port_3_r);
/*TODO*///	
/*TODO*///	
/*TODO*///		/* Address lines are scrambled on the main CPU:
/*TODO*///	
/*TODO*///			A0 . A2
/*TODO*///			A1 . A0
/*TODO*///			A2 . A3
/*TODO*///			A3 . A1 */
/*TODO*///	
/*TODO*///		RAM = memory_region(REGION_CPU1);
/*TODO*///		for (i = 0; i < 0x10000; i += 16)
/*TODO*///		{
/*TODO*///			int j;
/*TODO*///			unsigned char swapbuffer[16];
/*TODO*///	
/*TODO*///			for (j = 0; j < 16; j++)
/*TODO*///			{
/*TODO*///				swapbuffer[j] = RAM[i + ((j & 1) << 2) + ((j & 2) >> 1) + ((j & 4) << 1) + ((j & 8) >> 2)];
/*TODO*///			}
/*TODO*///	
/*TODO*///			memcpy(&RAM[i], swapbuffer, 16);
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_hotshock = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		/* protection??? The game jumps into never-neverland here. I think
/*TODO*///		   it just expects a RET there */
/*TODO*///		memory_region(REGION_CPU1)[0x2ef9] = 0xc9;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_cavelon = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///		/* banked ROM */
/*TODO*///		install_mem_read_handler(0, 0x0000, 0x3fff, MRA_BANK1);
/*TODO*///	
/*TODO*///		/* A15 switches memory banks */
/*TODO*///		install_mem_read_handler (0, 0x8000, 0xffff, cavelon_banksw_r);
/*TODO*///		install_mem_write_handler(0, 0x8000, 0xffff, cavelon_banksw_w);
/*TODO*///	
/*TODO*///		install_mem_write_handler(0, 0x2000, 0x2000, MWA_NOP);	/* ??? */
/*TODO*///		install_mem_write_handler(0, 0x3800, 0x3801, MWA_NOP);  /* looks suspicously like
/*TODO*///																   an AY8910, but not sure */
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_moonwar = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///		/* special handler for the spinner */
/*TODO*///		ppi8255_set_portAread (0, moonwar_input_port_0_r);
/*TODO*///		ppi8255_set_portCwrite(0, moonwar_port_select_w);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_darkplnt = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		ppi8255_init(&ppi8255_intf);
/*TODO*///	
/*TODO*///		/* special handler for the spinner */
/*TODO*///		ppi8255_set_portBread(0, darkplnt_input_port_1_r);
/*TODO*///	
/*TODO*///		install_mem_write_handler(0, 0xb00a, 0xb00a, darkplnt_bullet_color_w);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	static int bit(int i,int n)
/*TODO*///	{
/*TODO*///		return ((i >> n) & 1);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_anteater = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///		UBytePtr RAM;
/*TODO*///		UBytePtr scratch;
/*TODO*///	
/*TODO*///	
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///		/*
/*TODO*///		*   Code To Decode Lost Tomb by Mirko Buffoni
/*TODO*///		*   Optimizations done by Fabio Buffoni
/*TODO*///		*/
/*TODO*///	
/*TODO*///		RAM = memory_region(REGION_GFX1);
/*TODO*///	
/*TODO*///		scratch = malloc(memory_region_length(REGION_GFX1));
/*TODO*///	
/*TODO*///		if (scratch != 0)
/*TODO*///		{
/*TODO*///			memcpy(scratch, RAM, memory_region_length(REGION_GFX1));
/*TODO*///	
/*TODO*///			for (i = 0; i < memory_region_length(REGION_GFX1); i++)
/*TODO*///			{
/*TODO*///				int j;
/*TODO*///	
/*TODO*///	
/*TODO*///				j = i & 0x9bf;
/*TODO*///				j |= ( bit(i,4) ^ bit(i,9) ^ ( bit(i,2) & bit(i,10) ) ) << 6;
/*TODO*///				j |= ( bit(i,2) ^ bit(i,10) ) << 9;
/*TODO*///				j |= ( bit(i,0) ^ bit(i,6) ^ 1 ) << 10;
/*TODO*///	
/*TODO*///				RAM[i] = scratch[j];
/*TODO*///			}
/*TODO*///	
/*TODO*///			free(scratch);
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_rescue = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///		UBytePtr RAM;
/*TODO*///		UBytePtr scratch;
/*TODO*///	
/*TODO*///	
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///		/*
/*TODO*///		*   Code To Decode Lost Tomb by Mirko Buffoni
/*TODO*///		*   Optimizations done by Fabio Buffoni
/*TODO*///		*/
/*TODO*///	
/*TODO*///		RAM = memory_region(REGION_GFX1);
/*TODO*///	
/*TODO*///		scratch = malloc(memory_region_length(REGION_GFX1));
/*TODO*///	
/*TODO*///		if (scratch != 0)
/*TODO*///		{
/*TODO*///			memcpy(scratch, RAM, memory_region_length(REGION_GFX1));
/*TODO*///	
/*TODO*///			for (i = 0; i < memory_region_length(REGION_GFX1); i++)
/*TODO*///			{
/*TODO*///				int j;
/*TODO*///	
/*TODO*///	
/*TODO*///				j = i & 0xa7f;
/*TODO*///				j |= ( bit(i,3) ^ bit(i,10) ) << 7;
/*TODO*///				j |= ( bit(i,1) ^ bit(i,7) ) << 8;
/*TODO*///				j |= ( bit(i,0) ^ bit(i,8) ) << 10;
/*TODO*///	
/*TODO*///				RAM[i] = scratch[j];
/*TODO*///			}
/*TODO*///	
/*TODO*///			free(scratch);
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_minefld = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///		UBytePtr RAM;
/*TODO*///		UBytePtr scratch;
/*TODO*///	
/*TODO*///	
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///		/*
/*TODO*///		*   Code To Decode Minefield by Mike Balfour and Nicola Salmoria
/*TODO*///		*/
/*TODO*///	
/*TODO*///		RAM = memory_region(REGION_GFX1);
/*TODO*///	
/*TODO*///		scratch = malloc(memory_region_length(REGION_GFX1));
/*TODO*///	
/*TODO*///		if (scratch != 0)
/*TODO*///		{
/*TODO*///			memcpy(scratch, RAM, memory_region_length(REGION_GFX1));
/*TODO*///	
/*TODO*///			for (i = 0; i < memory_region_length(REGION_GFX1); i++)
/*TODO*///			{
/*TODO*///				int j;
/*TODO*///	
/*TODO*///	
/*TODO*///				j  = i & 0xd5f;
/*TODO*///				j |= ( bit(i,3) ^ bit(i,7) ) << 5;
/*TODO*///				j |= ( bit(i,2) ^ bit(i,9) ^ ( bit(i,0) & bit(i,5) ) ^
/*TODO*///					 ( bit(i,3) & bit(i,7) & ( bit(i,0) ^ bit(i,5) ))) << 7;
/*TODO*///				j |= ( bit(i,0) ^ bit(i,5) ^ ( bit(i,3) & bit(i,7) ) ) << 9;
/*TODO*///	
/*TODO*///				RAM[i] = scratch[j];
/*TODO*///			}
/*TODO*///	
/*TODO*///			free(scratch);
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_losttomb = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///		UBytePtr RAM;
/*TODO*///		UBytePtr scratch;
/*TODO*///	
/*TODO*///	
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///		/*
/*TODO*///		*   Code To Decode Lost Tomb by Mirko Buffoni
/*TODO*///		*   Optimizations done by Fabio Buffoni
/*TODO*///		*/
/*TODO*///	
/*TODO*///		RAM = memory_region(REGION_GFX1);
/*TODO*///	
/*TODO*///		scratch = malloc(memory_region_length(REGION_GFX1));
/*TODO*///	
/*TODO*///		if (scratch != 0)
/*TODO*///		{
/*TODO*///			memcpy(scratch, RAM, memory_region_length(REGION_GFX1));
/*TODO*///	
/*TODO*///			for (i = 0; i < memory_region_length(REGION_GFX1); i++)
/*TODO*///			{
/*TODO*///				int j;
/*TODO*///	
/*TODO*///	
/*TODO*///				j = i & 0xa7f;
/*TODO*///				j |= ( (bit(i,1) & bit(i,8)) | ((1 ^ bit(i,1)) & (bit(i,10)))) << 7;
/*TODO*///				j |= ( bit(i,7) ^ (bit(i,1) & ( bit(i,7) ^ bit(i,10) ))) << 8;
/*TODO*///				j |= ( (bit(i,1) & bit(i,7)) | ((1 ^ bit(i,1)) & (bit(i,8)))) << 10;
/*TODO*///	
/*TODO*///				RAM[i] = scratch[j];
/*TODO*///			}
/*TODO*///	
/*TODO*///			free(scratch);
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_superbon = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///		UBytePtr RAM;
/*TODO*///	
/*TODO*///	
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///		/*
/*TODO*///		*   Code rom deryption worked out by hand by Chris Hardy.
/*TODO*///		*/
/*TODO*///	
/*TODO*///		RAM = memory_region(REGION_CPU1);
/*TODO*///	
/*TODO*///		for (i = 0;i < 0x1000;i++)
/*TODO*///		{
/*TODO*///			/* Code is encrypted depending on bit 7 and 9 of the address */
/*TODO*///			switch (i & 0x0280)
/*TODO*///			{
/*TODO*///			case 0x0000:
/*TODO*///				RAM[i] ^= 0x92;
/*TODO*///				break;
/*TODO*///			case 0x0080:
/*TODO*///				RAM[i] ^= 0x82;
/*TODO*///				break;
/*TODO*///			case 0x0200:
/*TODO*///				RAM[i] ^= 0x12;
/*TODO*///				break;
/*TODO*///			case 0x0280:
/*TODO*///				RAM[i] ^= 0x10;
/*TODO*///				break;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_hustler = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		int A;
/*TODO*///	
/*TODO*///	
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///	
/*TODO*///		for (A = 0;A < 0x4000;A++)
/*TODO*///		{
/*TODO*///			unsigned char xormask;
/*TODO*///			int bits[8];
/*TODO*///			int i;
/*TODO*///			UBytePtr RAM = memory_region(REGION_CPU1);
/*TODO*///	
/*TODO*///	
/*TODO*///			for (i = 0;i < 8;i++)
/*TODO*///				bits[i] = (A >> i) & 1;
/*TODO*///	
/*TODO*///			xormask = 0xff;
/*TODO*///			if (bits[0] ^ bits[1]) xormask ^= 0x01;
/*TODO*///			if (bits[3] ^ bits[6]) xormask ^= 0x02;
/*TODO*///			if (bits[4] ^ bits[5]) xormask ^= 0x04;
/*TODO*///			if (bits[0] ^ bits[2]) xormask ^= 0x08;
/*TODO*///			if (bits[2] ^ bits[3]) xormask ^= 0x10;
/*TODO*///			if (bits[1] ^ bits[5]) xormask ^= 0x20;
/*TODO*///			if (bits[0] ^ bits[7]) xormask ^= 0x40;
/*TODO*///			if (bits[4] ^ bits[6]) xormask ^= 0x80;
/*TODO*///	
/*TODO*///			RAM[A] ^= xormask;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* the first ROM of the second CPU has data lines D0 and D1 swapped. Decode it. */
/*TODO*///		{
/*TODO*///			UBytePtr RAM = memory_region(REGION_CPU2);
/*TODO*///	
/*TODO*///	
/*TODO*///			for (A = 0;A < 0x0800;A++)
/*TODO*///				RAM[A] = (RAM[A] & 0xfc) | ((RAM[A] & 1) << 1) | ((RAM[A] & 2) >> 1);
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_billiard = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		int A;
/*TODO*///	
/*TODO*///	
/*TODO*///		init_scobra();
/*TODO*///	
/*TODO*///	
/*TODO*///		for (A = 0;A < 0x4000;A++)
/*TODO*///		{
/*TODO*///			unsigned char xormask;
/*TODO*///			int bits[8];
/*TODO*///			int i;
/*TODO*///			UBytePtr RAM = memory_region(REGION_CPU1);
/*TODO*///	
/*TODO*///	
/*TODO*///			for (i = 0;i < 8;i++)
/*TODO*///				bits[i] = (A >> i) & 1;
/*TODO*///	
/*TODO*///			xormask = 0x55;
/*TODO*///			if (bits[2] ^ ( bits[3] &  bits[6])) xormask ^= 0x01;
/*TODO*///			if (bits[4] ^ ( bits[5] &  bits[7])) xormask ^= 0x02;
/*TODO*///			if (bits[0] ^ ( bits[7] & !bits[3])) xormask ^= 0x04;
/*TODO*///			if (bits[3] ^ (!bits[0] &  bits[2])) xormask ^= 0x08;
/*TODO*///			if (bits[5] ^ (!bits[4] &  bits[1])) xormask ^= 0x10;
/*TODO*///			if (bits[6] ^ (!bits[2] & !bits[5])) xormask ^= 0x20;
/*TODO*///			if (bits[1] ^ (!bits[6] & !bits[4])) xormask ^= 0x40;
/*TODO*///			if (bits[7] ^ (!bits[1] &  bits[0])) xormask ^= 0x80;
/*TODO*///	
/*TODO*///			RAM[A] ^= xormask;
/*TODO*///	
/*TODO*///			for (i = 0;i < 8;i++)
/*TODO*///				bits[i] = (RAM[A] >> i) & 1;
/*TODO*///	
/*TODO*///			RAM[A] =
/*TODO*///				(bits[7] << 0) +
/*TODO*///				(bits[0] << 1) +
/*TODO*///				(bits[3] << 2) +
/*TODO*///				(bits[4] << 3) +
/*TODO*///				(bits[5] << 4) +
/*TODO*///				(bits[2] << 5) +
/*TODO*///				(bits[1] << 6) +
/*TODO*///				(bits[6] << 7);
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* the first ROM of the second CPU has data lines D0 and D1 swapped. Decode it. */
/*TODO*///		{
/*TODO*///			UBytePtr RAM = memory_region(REGION_CPU2);
/*TODO*///	
/*TODO*///	
/*TODO*///			for (A = 0;A < 0x0800;A++)
/*TODO*///				RAM[A] = (RAM[A] & 0xfc) | ((RAM[A] & 1) << 1) | ((RAM[A] & 2) >> 1);
/*TODO*///		}
/*TODO*///	} };    
}
