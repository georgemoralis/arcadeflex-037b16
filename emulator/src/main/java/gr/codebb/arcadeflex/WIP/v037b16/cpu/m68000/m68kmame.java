/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.cpu.m68000;

import static gr.codebb.arcadeflex.WIP.v037b16.cpu.m68000.m68000H.*;
import static gr.codebb.arcadeflex.WIP.v037b16.cpu.m68000.m68kH.*;
import static gr.codebb.arcadeflex.WIP.v037b16.cpu.m68000.m68kcpu.*;
import static gr.codebb.arcadeflex.WIP.v037b16.cpu.m68000.m68kcpuH.*;
import gr.codebb.arcadeflex.v037b16.mame.cpuintrfH;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;

public class m68kmame  extends cpu_interface {

    public m68kmame() {
        cpu_num = CPU_M68000;
        num_irqs = 8;
        default_vector = -1;
        overclock = 1.0;
        no_int = MC68000_INT_NONE;
        irq_int = -1;
        nmi_int = -1;
        address_shift = 0;
        address_bits = 24;
        endianess = CPU_IS_BE;
        align_unit = 2;
        max_inst_len = 10;
//        abits1 = ABITS1_24;
//        abits2 = ABITS2_24;
//        abitsmin = ABITS_MIN_24;
        icount = m68k_ICount;
        m68k_ICount[0] = 0;
    }

    /* global access */
    public static int[] m68k_ICount = new int[1];
/*TODO*///	struct m68k_memory_interface m68k_memory_intf;
/*TODO*///	
/*TODO*///	#ifndef A68K0
/*TODO*///	
/*TODO*///	/****************************************************************************
/*TODO*///	 * 24-bit address, 16-bit data memory interface
/*TODO*///	 ****************************************************************************/
/*TODO*///	
/*TODO*///	static data32_t readlong_a24_d16(offs_t address)
/*TODO*///	{
/*TODO*///		data32_t result = cpu_readmem24bew_word(address) << 16;
/*TODO*///		return result | cpu_readmem24bew_word(address + 2);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void writelong_a24_d16(offs_t address, data32_t data)
/*TODO*///	{
/*TODO*///		cpu_writemem24bew_word(address, data >> 16);
/*TODO*///		cpu_writemem24bew_word(address + 2, data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void changepc_a24_d16(offs_t pc)
/*TODO*///	{
/*TODO*///		change_pc24bew(pc);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* interface for 24-bit address bus, 16-bit data bus (68000, 68010) */
/*TODO*///	static const struct m68k_memory_interface interface_a24_d16 =
/*TODO*///	{
/*TODO*///		0,
/*TODO*///		cpu_readmem24bew,
/*TODO*///		cpu_readmem24bew_word,
/*TODO*///		readlong_a24_d16,
/*TODO*///		cpu_writemem24bew,
/*TODO*///		cpu_writemem24bew_word,
/*TODO*///		writelong_a24_d16,
/*TODO*///		changepc_a24_d16
/*TODO*///	};
/*TODO*///	
/*TODO*///	#endif // A68K0
/*TODO*///	
/*TODO*///	/****************************************************************************
/*TODO*///	 * 24-bit address, 32-bit data memory interface
/*TODO*///	 ****************************************************************************/
/*TODO*///	
/*TODO*///	#ifndef A68K2
/*TODO*///	
/*TODO*///	/* potentially misaligned 16-bit reads with a 32-bit data bus (and 24-bit address bus) */
/*TODO*///	static data16_t readword_a24_d32(offs_t address)
/*TODO*///	{
/*TODO*///		data16_t result;
/*TODO*///	
/*TODO*///		if (!(address & 1))
/*TODO*///			return cpu_readmem24bedw_word(address);
/*TODO*///		result = cpu_readmem24bedw(address) << 8;
/*TODO*///		return result | cpu_readmem24bedw(address + 1);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* potentially misaligned 16-bit writes with a 32-bit data bus (and 24-bit address bus) */
/*TODO*///	static void writeword_a24_d32(offs_t address, data16_t data)
/*TODO*///	{
/*TODO*///		if (!(address & 1))
/*TODO*///		{
/*TODO*///			cpu_writemem24bedw_word(address, data);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		cpu_writemem24bedw(address, data >> 8);
/*TODO*///		cpu_writemem24bedw(address + 1, data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* potentially misaligned 32-bit reads with a 32-bit data bus (and 24-bit address bus) */
/*TODO*///	static data32_t readlong_a24_d32(offs_t address)
/*TODO*///	{
/*TODO*///		data32_t result;
/*TODO*///	
/*TODO*///		if (!(address & 3))
/*TODO*///			return cpu_readmem24bedw_dword(address);
/*TODO*///		else if (!(address & 1))
/*TODO*///		{
/*TODO*///			result = cpu_readmem24bedw_word(address) << 16;
/*TODO*///			return result | cpu_readmem24bedw_word(address + 2);
/*TODO*///		}
/*TODO*///		result = cpu_readmem24bedw(address) << 24;
/*TODO*///		result |= cpu_readmem24bedw_word(address + 1) << 8;
/*TODO*///		return result | cpu_readmem24bedw(address + 3);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* potentially misaligned 32-bit writes with a 32-bit data bus (and 24-bit address bus) */
/*TODO*///	static void writelong_a24_d32(offs_t address, data32_t data)
/*TODO*///	{
/*TODO*///		if (!(address & 3))
/*TODO*///		{
/*TODO*///			cpu_writemem24bedw_dword(address, data);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		else if (!(address & 1))
/*TODO*///		{
/*TODO*///			cpu_writemem24bedw_word(address, data >> 16);
/*TODO*///			cpu_writemem24bedw_word(address + 2, data);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		cpu_writemem24bedw(address, data >> 24);
/*TODO*///		cpu_writemem24bedw_word(address + 1, data >> 8);
/*TODO*///		cpu_writemem24bedw(address + 3, data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void changepc_a24_d32(offs_t pc)
/*TODO*///	{
/*TODO*///		change_pc24bedw(pc);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* interface for 24-bit address bus, 32-bit data bus (68EC020) */
/*TODO*///	static const struct m68k_memory_interface interface_a24_d32 =
/*TODO*///	{
/*TODO*///		WORD_XOR_BE(0),
/*TODO*///		cpu_readmem24bedw,
/*TODO*///		readword_a24_d32,
/*TODO*///		readlong_a24_d32,
/*TODO*///		cpu_writemem24bedw,
/*TODO*///		writeword_a24_d32,
/*TODO*///		writelong_a24_d32,
/*TODO*///		changepc_a24_d32
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	/****************************************************************************
/*TODO*///	 * 32-bit address, 32-bit data memory interface
/*TODO*///	 ****************************************************************************/
/*TODO*///	
/*TODO*///	/* potentially misaligned 16-bit reads with a 32-bit data bus (and 32-bit address bus) */
/*TODO*///	static data16_t readword_a32_d32(offs_t address)
/*TODO*///	{
/*TODO*///		data16_t result;
/*TODO*///	
/*TODO*///		if (!(address & 1))
/*TODO*///			return cpu_readmem32bedw_word(address);
/*TODO*///		result = cpu_readmem32bedw(address) << 8;
/*TODO*///		return result | cpu_readmem32bedw(address + 1);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* potentially misaligned 16-bit writes with a 32-bit data bus (and 32-bit address bus) */
/*TODO*///	static void writeword_a32_d32(offs_t address, data16_t data)
/*TODO*///	{
/*TODO*///		if (!(address & 1))
/*TODO*///		{
/*TODO*///			cpu_writemem32bedw_word(address, data);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		cpu_writemem32bedw(address, data >> 8);
/*TODO*///		cpu_writemem32bedw(address + 1, data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* potentially misaligned 32-bit reads with a 32-bit data bus (and 32-bit address bus) */
/*TODO*///	static data32_t readlong_a32_d32(offs_t address)
/*TODO*///	{
/*TODO*///		data32_t result;
/*TODO*///	
/*TODO*///		if (!(address & 3))
/*TODO*///			return cpu_readmem32bedw_dword(address);
/*TODO*///		else if (!(address & 1))
/*TODO*///		{
/*TODO*///			result = cpu_readmem32bedw_word(address) << 16;
/*TODO*///			return result | cpu_readmem32bedw_word(address + 2);
/*TODO*///		}
/*TODO*///		result = cpu_readmem32bedw(address) << 24;
/*TODO*///		result |= cpu_readmem32bedw_word(address + 1) << 8;
/*TODO*///		return result | cpu_readmem32bedw(address + 3);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* potentially misaligned 32-bit writes with a 32-bit data bus (and 32-bit address bus) */
/*TODO*///	static void writelong_a32_d32(offs_t address, data32_t data)
/*TODO*///	{
/*TODO*///		if (!(address & 3))
/*TODO*///		{
/*TODO*///			cpu_writemem32bedw_dword(address, data);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		else if (!(address & 1))
/*TODO*///		{
/*TODO*///			cpu_writemem32bedw_word(address,     data >> 16);
/*TODO*///			cpu_writemem32bedw_word(address + 2, data);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		cpu_writemem32bedw(address, data >> 24);
/*TODO*///		cpu_writemem32bedw_word(address + 1, data >> 8);
/*TODO*///		cpu_writemem32bedw(address + 3, data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void changepc_a32_d32(offs_t pc)
/*TODO*///	{
/*TODO*///		change_pc32bedw(pc);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* interface for 24-bit address bus, 32-bit data bus (68020) */
/*TODO*///	static const struct m68k_memory_interface interface_a32_d32 =
/*TODO*///	{
/*TODO*///		WORD_XOR_BE(0),
/*TODO*///		cpu_readmem32bedw,
/*TODO*///		readword_a32_d32,
/*TODO*///		readlong_a32_d32,
/*TODO*///		cpu_writemem32bedw,
/*TODO*///		writeword_a32_d32,
/*TODO*///		writelong_a32_d32,
/*TODO*///		changepc_a32_d32
/*TODO*///	};
/*TODO*///	
/*TODO*///	/* global access */
/*TODO*///	struct m68k_memory_interface m68k_memory_intf;
/*TODO*///	
/*TODO*///	#endif // A68K2
/*TODO*///	
/*TODO*///	/****************************************************************************
/*TODO*///	 * 68000 section
/*TODO*///	 ****************************************************************************/
/*TODO*///	
/*TODO*///	#ifndef A68K0
/*TODO*///	
/*TODO*///	static UINT8 m68000_reg_layout[] = {
/*TODO*///		M68K_PC, M68K_ISP, -1,
/*TODO*///		M68K_SR, M68K_USP, -1,
/*TODO*///		M68K_D0, M68K_A0, -1,
/*TODO*///		M68K_D1, M68K_A1, -1,
/*TODO*///		M68K_D2, M68K_A2, -1,
/*TODO*///		M68K_D3, M68K_A3, -1,
/*TODO*///		M68K_D4, M68K_A4, -1,
/*TODO*///		M68K_D5, M68K_A5, -1,
/*TODO*///		M68K_D6, M68K_A6, -1,
/*TODO*///		M68K_D7, M68K_A7, 0
/*TODO*///	};
/*TODO*///	
/*TODO*///	static UINT8 m68000_win_layout[] = {
/*TODO*///		48, 0,32,13,	/* register window (top right) */
/*TODO*///		 0, 0,47,13,	/* disassembler window (top left) */
/*TODO*///		 0,14,47, 8,	/* memory #1 window (left, middle) */
/*TODO*///		48,14,32, 8,	/* memory #2 window (right, middle) */
/*TODO*///		 0,23,80, 1 	/* command line window (bottom rows) */
/*TODO*///	};
	
	public static void m68000_init()
	{
		m68k_init();
		m68k_set_cpu_type(M68K_CPU_TYPE_68000);
/*TODO*///		m68k_memory_intf = interface_a24_d16;
                System.out.println("m68k_memory_intf not implemented!!!!");
/*TODO*///		m68k_state_register("m68000");
	}
/*TODO*///	
/*TODO*///	void m68000_reset(void* param)
/*TODO*///	{
/*TODO*///		m68k_pulse_reset();
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68000_exit(void)
/*TODO*///	{
/*TODO*///		/* nothing to do */
/*TODO*///	}
/*TODO*///	
/*TODO*///	int m68000_execute(int cycles)
/*TODO*///	{
/*TODO*///		return m68k_execute(cycles);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68000_get_context(void *dst)
/*TODO*///	{
/*TODO*///		return m68k_get_context(dst);
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68000_set_context(void *src)
/*TODO*///	{
/*TODO*///		if (m68k_memory_intf.read8 != cpu_readmem24bew)
/*TODO*///			m68k_memory_intf = interface_a24_d16;
/*TODO*///		m68k_set_context(src);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68000_get_pc(void)
/*TODO*///	{
/*TODO*///		return m68k_get_reg(NULL, M68K_REG_PC)&0x00ffffff;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68000_set_pc(unsigned val)
/*TODO*///	{
/*TODO*///		m68k_set_reg(M68K_REG_PC, val&0x00ffffff);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68000_get_sp(void)
/*TODO*///	{
/*TODO*///		return m68k_get_reg(NULL, M68K_REG_SP);
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68000_set_sp(unsigned val)
/*TODO*///	{
/*TODO*///		m68k_set_reg(M68K_REG_SP, val);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68000_get_reg(int regnum)
/*TODO*///	{
/*TODO*///		switch( regnum )
/*TODO*///		{
/*TODO*///			case M68K_PC:  return m68k_get_reg(NULL, M68K_REG_PC);
/*TODO*///			case M68K_SP:  return m68k_get_reg(NULL, M68K_REG_SP);
/*TODO*///			case M68K_ISP: return m68k_get_reg(NULL, M68K_REG_ISP);
/*TODO*///			case M68K_USP: return m68k_get_reg(NULL, M68K_REG_USP);
/*TODO*///			case M68K_SR:  return m68k_get_reg(NULL, M68K_REG_SR);
/*TODO*///			case M68K_D0:  return m68k_get_reg(NULL, M68K_REG_D0);
/*TODO*///			case M68K_D1:  return m68k_get_reg(NULL, M68K_REG_D1);
/*TODO*///			case M68K_D2:  return m68k_get_reg(NULL, M68K_REG_D2);
/*TODO*///			case M68K_D3:  return m68k_get_reg(NULL, M68K_REG_D3);
/*TODO*///			case M68K_D4:  return m68k_get_reg(NULL, M68K_REG_D4);
/*TODO*///			case M68K_D5:  return m68k_get_reg(NULL, M68K_REG_D5);
/*TODO*///			case M68K_D6:  return m68k_get_reg(NULL, M68K_REG_D6);
/*TODO*///			case M68K_D7:  return m68k_get_reg(NULL, M68K_REG_D7);
/*TODO*///			case M68K_A0:  return m68k_get_reg(NULL, M68K_REG_A0);
/*TODO*///			case M68K_A1:  return m68k_get_reg(NULL, M68K_REG_A1);
/*TODO*///			case M68K_A2:  return m68k_get_reg(NULL, M68K_REG_A2);
/*TODO*///			case M68K_A3:  return m68k_get_reg(NULL, M68K_REG_A3);
/*TODO*///			case M68K_A4:  return m68k_get_reg(NULL, M68K_REG_A4);
/*TODO*///			case M68K_A5:  return m68k_get_reg(NULL, M68K_REG_A5);
/*TODO*///			case M68K_A6:  return m68k_get_reg(NULL, M68K_REG_A6);
/*TODO*///			case M68K_A7:  return m68k_get_reg(NULL, M68K_REG_A7);
/*TODO*///			case M68K_PREF_ADDR:  return m68k_get_reg(NULL, M68K_REG_PREF_ADDR);
/*TODO*///			case M68K_PREF_DATA:  return m68k_get_reg(NULL, M68K_REG_PREF_DATA);
/*TODO*///			case REG_PREVIOUSPC: return m68k_get_reg(NULL, M68K_REG_PPC);
/*TODO*///	/* TODO: return contents of [SP + wordsize * (REG_SP_CONTENTS-regnum)] */
/*TODO*///			default:
/*TODO*///				if( regnum < REG_SP_CONTENTS )
/*TODO*///				{
/*TODO*///					unsigned offset = m68k_get_reg(NULL, M68K_REG_SP) + 4 * (REG_SP_CONTENTS - regnum);
/*TODO*///					if( offset < 0xfffffd )
/*TODO*///						return m68k_read_memory_32( offset );
/*TODO*///				}
/*TODO*///		}
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68000_set_reg(int regnum, unsigned val)
/*TODO*///	{
/*TODO*///		switch( regnum )
/*TODO*///		{
/*TODO*///			case M68K_PC:  m68k_set_reg(M68K_REG_PC, val); break;
/*TODO*///			case M68K_SP:  m68k_set_reg(M68K_REG_SP, val); break;
/*TODO*///			case M68K_ISP: m68k_set_reg(M68K_REG_ISP, val); break;
/*TODO*///			case M68K_USP: m68k_set_reg(M68K_REG_USP, val); break;
/*TODO*///			case M68K_SR:  m68k_set_reg(M68K_REG_SR, val); break;
/*TODO*///			case M68K_D0:  m68k_set_reg(M68K_REG_D0, val); break;
/*TODO*///			case M68K_D1:  m68k_set_reg(M68K_REG_D1, val); break;
/*TODO*///			case M68K_D2:  m68k_set_reg(M68K_REG_D2, val); break;
/*TODO*///			case M68K_D3:  m68k_set_reg(M68K_REG_D3, val); break;
/*TODO*///			case M68K_D4:  m68k_set_reg(M68K_REG_D4, val); break;
/*TODO*///			case M68K_D5:  m68k_set_reg(M68K_REG_D5, val); break;
/*TODO*///			case M68K_D6:  m68k_set_reg(M68K_REG_D6, val); break;
/*TODO*///			case M68K_D7:  m68k_set_reg(M68K_REG_D7, val); break;
/*TODO*///			case M68K_A0:  m68k_set_reg(M68K_REG_A0, val); break;
/*TODO*///			case M68K_A1:  m68k_set_reg(M68K_REG_A1, val); break;
/*TODO*///			case M68K_A2:  m68k_set_reg(M68K_REG_A2, val); break;
/*TODO*///			case M68K_A3:  m68k_set_reg(M68K_REG_A3, val); break;
/*TODO*///			case M68K_A4:  m68k_set_reg(M68K_REG_A4, val); break;
/*TODO*///			case M68K_A5:  m68k_set_reg(M68K_REG_A5, val); break;
/*TODO*///			case M68K_A6:  m68k_set_reg(M68K_REG_A6, val); break;
/*TODO*///			case M68K_A7:  m68k_set_reg(M68K_REG_A7, val); break;
/*TODO*///	/* TODO: set contents of [SP + wordsize * (REG_SP_CONTENTS-regnum)] */
/*TODO*///			default:
/*TODO*///				if( regnum < REG_SP_CONTENTS )
/*TODO*///				{
/*TODO*///					unsigned offset = m68k_get_reg(NULL, M68K_REG_SP) + 4 * (REG_SP_CONTENTS - regnum);
/*TODO*///					if( offset < 0xfffffd )
/*TODO*///						m68k_write_memory_16( offset, val );
/*TODO*///				}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68000_set_nmi_line(int state)
/*TODO*///	{
/*TODO*///		switch(state)
/*TODO*///		{
/*TODO*///			case CLEAR_LINE:
/*TODO*///				m68k_set_irq(0);
/*TODO*///				break;
/*TODO*///			case ASSERT_LINE:
/*TODO*///				m68k_set_irq(7);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				m68k_set_irq(7);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68000_set_irq_line(int irqline, int state)
/*TODO*///	{
/*TODO*///		switch(state)
/*TODO*///		{
/*TODO*///			case CLEAR_LINE:
/*TODO*///				m68k_set_irq(0);
/*TODO*///				break;
/*TODO*///			case ASSERT_LINE:
/*TODO*///				m68k_set_irq(irqline);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				m68k_set_irq(irqline);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68000_set_irq_callback(int (*callback)(int irqline))
/*TODO*///	{
/*TODO*///		m68k_set_int_ack_callback(callback);
/*TODO*///	}
	
	
	public static String m68000_info(Object context, int regnum) 
	{
/*TODO*///		static char buffer[32][47+1];
/*TODO*///		static int which = 0;
/*TODO*///		int sr;
/*TODO*///	
/*TODO*///		which = (which+1) % 32;
/*TODO*///		buffer[which][0] = '\0';

		switch( regnum )
		{
/*TODO*///			case CPU_INFO_REG+M68K_PC:	sprintf(buffer[which], "PC :%08X", m68k_get_reg(context, M68K_REG_PC)); break;
/*TODO*///			case CPU_INFO_REG+M68K_SR:  sprintf(buffer[which], "SR :%04X", m68k_get_reg(context, M68K_REG_SR)); break;
/*TODO*///			case CPU_INFO_REG+M68K_SP:  sprintf(buffer[which], "SP :%08X", m68k_get_reg(context, M68K_REG_SP)); break;
/*TODO*///			case CPU_INFO_REG+M68K_ISP: sprintf(buffer[which], "ISP:%08X", m68k_get_reg(context, M68K_REG_ISP)); break;
/*TODO*///			case CPU_INFO_REG+M68K_USP: sprintf(buffer[which], "USP:%08X", m68k_get_reg(context, M68K_REG_USP)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D0:	sprintf(buffer[which], "D0 :%08X", m68k_get_reg(context, M68K_REG_D0)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D1:	sprintf(buffer[which], "D1 :%08X", m68k_get_reg(context, M68K_REG_D1)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D2:	sprintf(buffer[which], "D2 :%08X", m68k_get_reg(context, M68K_REG_D2)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D3:	sprintf(buffer[which], "D3 :%08X", m68k_get_reg(context, M68K_REG_D3)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D4:	sprintf(buffer[which], "D4 :%08X", m68k_get_reg(context, M68K_REG_D4)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D5:	sprintf(buffer[which], "D5 :%08X", m68k_get_reg(context, M68K_REG_D5)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D6:	sprintf(buffer[which], "D6 :%08X", m68k_get_reg(context, M68K_REG_D6)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D7:	sprintf(buffer[which], "D7 :%08X", m68k_get_reg(context, M68K_REG_D7)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A0:	sprintf(buffer[which], "A0 :%08X", m68k_get_reg(context, M68K_REG_A0)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A1:	sprintf(buffer[which], "A1 :%08X", m68k_get_reg(context, M68K_REG_A1)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A2:	sprintf(buffer[which], "A2 :%08X", m68k_get_reg(context, M68K_REG_A2)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A3:	sprintf(buffer[which], "A3 :%08X", m68k_get_reg(context, M68K_REG_A3)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A4:	sprintf(buffer[which], "A4 :%08X", m68k_get_reg(context, M68K_REG_A4)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A5:	sprintf(buffer[which], "A5 :%08X", m68k_get_reg(context, M68K_REG_A5)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A6:	sprintf(buffer[which], "A6 :%08X", m68k_get_reg(context, M68K_REG_A6)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A7:	sprintf(buffer[which], "A7 :%08X", m68k_get_reg(context, M68K_REG_A7)); break;
/*TODO*///			case CPU_INFO_REG+M68K_PREF_ADDR:	sprintf(buffer[which], "PAR:%08X", m68k_get_reg(context, M68K_REG_PREF_ADDR)); break;
/*TODO*///			case CPU_INFO_REG+M68K_PREF_DATA:	sprintf(buffer[which], "PDA:%08X", m68k_get_reg(context, M68K_REG_PREF_DATA)); break;
/*TODO*///			case CPU_INFO_FLAGS:
/*TODO*///				sr = m68k_get_reg(context, M68K_REG_SR);
/*TODO*///				sprintf(buffer[which], "%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c",
/*TODO*///					sr & 0x8000 ? 'T':'.',
/*TODO*///					sr & 0x4000 ? '?':'.',
/*TODO*///					sr & 0x2000 ? 'S':'.',
/*TODO*///					sr & 0x1000 ? '?':'.',
/*TODO*///					sr & 0x0800 ? '?':'.',
/*TODO*///					sr & 0x0400 ? 'I':'.',
/*TODO*///					sr & 0x0200 ? 'I':'.',
/*TODO*///					sr & 0x0100 ? 'I':'.',
/*TODO*///					sr & 0x0080 ? '?':'.',
/*TODO*///					sr & 0x0040 ? '?':'.',
/*TODO*///					sr & 0x0020 ? '?':'.',
/*TODO*///					sr & 0x0010 ? 'X':'.',
/*TODO*///					sr & 0x0008 ? 'N':'.',
/*TODO*///					sr & 0x0004 ? 'Z':'.',
/*TODO*///					sr & 0x0002 ? 'V':'.',
/*TODO*///					sr & 0x0001 ? 'C':'.');
/*TODO*///				break;
			case CPU_INFO_NAME: return "68000";
			case CPU_INFO_FAMILY: return "Motorola 68K";
			case CPU_INFO_VERSION: return "3.2";
			case CPU_INFO_FILE: return "m68000.java";
			case CPU_INFO_CREDITS: return "Copyright 1999-2000 Karl Stenerud. All rights reserved. (2.1 fixes HJB)";
/*TODO*///			case CPU_INFO_REG_LAYOUT: return (const char*)m68000_reg_layout;
/*TODO*///			case CPU_INFO_WIN_LAYOUT: return (const char*)m68000_win_layout;
		}
/*TODO*///		return buffer[which];
                throw new UnsupportedOperationException("Unsupported");
	}
	
/*TODO*///	unsigned m68000_dasm(char *buffer, unsigned pc)
/*TODO*///	{
/*TODO*///		M68K_SET_PC_CALLBACK(pc);
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///		return m68k_disassemble( buffer, pc, M68K_CPU_TYPE_68000 );
/*TODO*///	#else
/*TODO*///		sprintf( buffer, "$%04X", m68k_read_immediate_16(pc) );
/*TODO*///		return 2;
/*TODO*///	#endif
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/****************************************************************************
/*TODO*///	 * M68010 section
/*TODO*///	 ****************************************************************************/
/*TODO*///	#if HAS_M68010
/*TODO*///	
/*TODO*///	static UINT8 m68010_reg_layout[] = {
/*TODO*///		M68K_PC,  M68K_ISP, -1,
/*TODO*///		M68K_SR,  M68K_USP, -1,
/*TODO*///		M68K_SFC, M68K_VBR, -1,
/*TODO*///		M68K_DFC, -1,
/*TODO*///		M68K_D0,  M68K_A0, -1,
/*TODO*///		M68K_D1,  M68K_A1, -1,
/*TODO*///		M68K_D2,  M68K_A2, -1,
/*TODO*///		M68K_D3,  M68K_A3, -1,
/*TODO*///		M68K_D4,  M68K_A4, -1,
/*TODO*///		M68K_D5,  M68K_A5, -1,
/*TODO*///		M68K_D6,  M68K_A6, -1,
/*TODO*///		M68K_D7,  M68K_A7, 0
/*TODO*///	};
/*TODO*///	
/*TODO*///	static UINT8 m68010_win_layout[] = {
/*TODO*///		48, 0,32,13,	/* register window (top right) */
/*TODO*///		 0, 0,47,13,	/* disassembler window (top left) */
/*TODO*///		 0,14,47, 8,	/* memory #1 window (left, middle) */
/*TODO*///		48,14,32, 8,	/* memory #2 window (right, middle) */
/*TODO*///		 0,23,80, 1 	/* command line window (bottom rows) */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	void m68010_init(void)
/*TODO*///	{
/*TODO*///		m68k_init();
/*TODO*///		m68k_set_cpu_type(M68K_CPU_TYPE_68010);
/*TODO*///		m68k_memory_intf = interface_a24_d16;
/*TODO*///		m68k_state_register("m68010");
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68010_reset(void* param)
/*TODO*///	{
/*TODO*///		m68k_pulse_reset();
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68010_exit(void)
/*TODO*///	{
/*TODO*///		/* nothing to do */
/*TODO*///	}
/*TODO*///	
/*TODO*///	int m68010_execute(int cycles)
/*TODO*///	{
/*TODO*///		return m68k_execute(cycles);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68010_get_context(void *dst)
/*TODO*///	{
/*TODO*///		return m68k_get_context(dst);
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68010_set_context(void *src)
/*TODO*///	{
/*TODO*///		if (m68k_memory_intf.read8 != cpu_readmem24bew)
/*TODO*///			m68k_memory_intf = interface_a24_d16;
/*TODO*///		m68k_set_context(src);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68010_get_pc(void)
/*TODO*///	{
/*TODO*///		return m68k_get_reg(NULL, M68K_REG_PC)&0x00ffffff;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68010_set_pc(unsigned val)
/*TODO*///	{
/*TODO*///		m68k_set_reg(M68K_REG_PC, val&0x00ffffff);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68010_get_sp(void)
/*TODO*///	{
/*TODO*///		return m68k_get_reg(NULL, M68K_REG_SP);
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68010_set_sp(unsigned val)
/*TODO*///	{
/*TODO*///		m68k_set_reg(M68K_REG_SP, val);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68010_get_reg(int regnum)
/*TODO*///	{
/*TODO*///		switch( regnum )
/*TODO*///		{
/*TODO*///			case M68K_VBR: return m68k_get_reg(NULL, M68K_REG_VBR); /* 68010+ */
/*TODO*///			case M68K_SFC: return m68k_get_reg(NULL, M68K_REG_SFC); /* 68010" */
/*TODO*///			case M68K_DFC: return m68k_get_reg(NULL, M68K_REG_DFC); /* 68010+ */
/*TODO*///			case M68K_PC:  return m68k_get_reg(NULL, M68K_REG_PC);
/*TODO*///			case M68K_SP:  return m68k_get_reg(NULL, M68K_REG_SP);
/*TODO*///			case M68K_ISP: return m68k_get_reg(NULL, M68K_REG_ISP);
/*TODO*///			case M68K_USP: return m68k_get_reg(NULL, M68K_REG_USP);
/*TODO*///			case M68K_SR:  return m68k_get_reg(NULL, M68K_REG_SR);
/*TODO*///			case M68K_D0:  return m68k_get_reg(NULL, M68K_REG_D0);
/*TODO*///			case M68K_D1:  return m68k_get_reg(NULL, M68K_REG_D1);
/*TODO*///			case M68K_D2:  return m68k_get_reg(NULL, M68K_REG_D2);
/*TODO*///			case M68K_D3:  return m68k_get_reg(NULL, M68K_REG_D3);
/*TODO*///			case M68K_D4:  return m68k_get_reg(NULL, M68K_REG_D4);
/*TODO*///			case M68K_D5:  return m68k_get_reg(NULL, M68K_REG_D5);
/*TODO*///			case M68K_D6:  return m68k_get_reg(NULL, M68K_REG_D6);
/*TODO*///			case M68K_D7:  return m68k_get_reg(NULL, M68K_REG_D7);
/*TODO*///			case M68K_A0:  return m68k_get_reg(NULL, M68K_REG_A0);
/*TODO*///			case M68K_A1:  return m68k_get_reg(NULL, M68K_REG_A1);
/*TODO*///			case M68K_A2:  return m68k_get_reg(NULL, M68K_REG_A2);
/*TODO*///			case M68K_A3:  return m68k_get_reg(NULL, M68K_REG_A3);
/*TODO*///			case M68K_A4:  return m68k_get_reg(NULL, M68K_REG_A4);
/*TODO*///			case M68K_A5:  return m68k_get_reg(NULL, M68K_REG_A5);
/*TODO*///			case M68K_A6:  return m68k_get_reg(NULL, M68K_REG_A6);
/*TODO*///			case M68K_A7:  return m68k_get_reg(NULL, M68K_REG_A7);
/*TODO*///			case M68K_PREF_ADDR:  return m68k_get_reg(NULL, M68K_REG_PREF_ADDR);
/*TODO*///			case M68K_PREF_DATA:  return m68k_get_reg(NULL, M68K_REG_PREF_DATA);
/*TODO*///			case REG_PREVIOUSPC: return m68k_get_reg(NULL, M68K_REG_PPC);
/*TODO*///	/* TODO: return contents of [SP + wordsize * (REG_SP_CONTENTS-regnum)] */
/*TODO*///			default:
/*TODO*///				if( regnum < REG_SP_CONTENTS )
/*TODO*///				{
/*TODO*///					unsigned offset = m68k_get_reg(NULL, M68K_REG_SP) + 4 * (REG_SP_CONTENTS - regnum);
/*TODO*///					if( offset < 0xfffffd )
/*TODO*///						return m68k_read_memory_32( offset );
/*TODO*///				}
/*TODO*///		}
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68010_set_reg(int regnum, unsigned val)
/*TODO*///	{
/*TODO*///		switch( regnum )
/*TODO*///		{
/*TODO*///			case M68K_VBR: m68k_set_reg(M68K_REG_VBR, val); break; /* 68010+ */
/*TODO*///			case M68K_SFC: m68k_set_reg(M68K_REG_SFC, val); break; /* 68010+ */
/*TODO*///			case M68K_DFC: m68k_set_reg(M68K_REG_DFC, val); break; /* 68010+ */
/*TODO*///			case M68K_PC:  m68k_set_reg(M68K_REG_PC, val); break;
/*TODO*///			case M68K_SP:  m68k_set_reg(M68K_REG_SP, val); break;
/*TODO*///			case M68K_ISP: m68k_set_reg(M68K_REG_ISP, val); break;
/*TODO*///			case M68K_USP: m68k_set_reg(M68K_REG_USP, val); break;
/*TODO*///			case M68K_SR:  m68k_set_reg(M68K_REG_SR, val); break;
/*TODO*///			case M68K_D0:  m68k_set_reg(M68K_REG_D0, val); break;
/*TODO*///			case M68K_D1:  m68k_set_reg(M68K_REG_D1, val); break;
/*TODO*///			case M68K_D2:  m68k_set_reg(M68K_REG_D2, val); break;
/*TODO*///			case M68K_D3:  m68k_set_reg(M68K_REG_D3, val); break;
/*TODO*///			case M68K_D4:  m68k_set_reg(M68K_REG_D4, val); break;
/*TODO*///			case M68K_D5:  m68k_set_reg(M68K_REG_D5, val); break;
/*TODO*///			case M68K_D6:  m68k_set_reg(M68K_REG_D6, val); break;
/*TODO*///			case M68K_D7:  m68k_set_reg(M68K_REG_D7, val); break;
/*TODO*///			case M68K_A0:  m68k_set_reg(M68K_REG_A0, val); break;
/*TODO*///			case M68K_A1:  m68k_set_reg(M68K_REG_A1, val); break;
/*TODO*///			case M68K_A2:  m68k_set_reg(M68K_REG_A2, val); break;
/*TODO*///			case M68K_A3:  m68k_set_reg(M68K_REG_A3, val); break;
/*TODO*///			case M68K_A4:  m68k_set_reg(M68K_REG_A4, val); break;
/*TODO*///			case M68K_A5:  m68k_set_reg(M68K_REG_A5, val); break;
/*TODO*///			case M68K_A6:  m68k_set_reg(M68K_REG_A6, val); break;
/*TODO*///			case M68K_A7:  m68k_set_reg(M68K_REG_A7, val); break;
/*TODO*///	/* TODO: set contents of [SP + wordsize * (REG_SP_CONTENTS-regnum)] */
/*TODO*///			default:
/*TODO*///				if( regnum < REG_SP_CONTENTS )
/*TODO*///				{
/*TODO*///					unsigned offset = m68k_get_reg(NULL, M68K_REG_SP) + 4 * (REG_SP_CONTENTS - regnum);
/*TODO*///					if( offset < 0xfffffd )
/*TODO*///						m68k_write_memory_16( offset, val );
/*TODO*///				}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68010_set_nmi_line(int state)
/*TODO*///	{
/*TODO*///		switch(state)
/*TODO*///		{
/*TODO*///			case CLEAR_LINE:
/*TODO*///				m68k_set_irq(0);
/*TODO*///				break;
/*TODO*///			case ASSERT_LINE:
/*TODO*///				m68k_set_irq(7);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				m68k_set_irq(7);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68010_set_irq_line(int irqline, int state)
/*TODO*///	{
/*TODO*///		switch(state)
/*TODO*///		{
/*TODO*///			case CLEAR_LINE:
/*TODO*///				m68k_set_irq(0);
/*TODO*///				break;
/*TODO*///			case ASSERT_LINE:
/*TODO*///				m68k_set_irq(irqline);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				m68k_set_irq(irqline);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68010_set_irq_callback(int (*callback)(int irqline))
/*TODO*///	{
/*TODO*///		m68k_set_int_ack_callback(callback);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	const char *m68010_info(void *context, int regnum)
/*TODO*///	{
/*TODO*///		static char buffer[32][47+1];
/*TODO*///		static int which = 0;
/*TODO*///		int sr;
/*TODO*///	
/*TODO*///		which = (which+1) % 32;
/*TODO*///		buffer[which][0] = '\0';
/*TODO*///	
/*TODO*///		switch( regnum )
/*TODO*///		{
/*TODO*///			case CPU_INFO_REG+M68K_SFC: sprintf(buffer[which], "SFC:%X",   m68k_get_reg(context, M68K_REG_SFC)); break;
/*TODO*///			case CPU_INFO_REG+M68K_DFC: sprintf(buffer[which], "DFC:%X",   m68k_get_reg(context, M68K_REG_DFC)); break;
/*TODO*///			case CPU_INFO_REG+M68K_VBR: sprintf(buffer[which], "VBR:%08X", m68k_get_reg(context, M68K_REG_VBR)); break;
/*TODO*///			case CPU_INFO_REG+M68K_PC:	sprintf(buffer[which], "PC :%08X", m68k_get_reg(context, M68K_REG_PC)); break;
/*TODO*///			case CPU_INFO_REG+M68K_SR:  sprintf(buffer[which], "SR :%04X", m68k_get_reg(context, M68K_REG_SR)); break;
/*TODO*///			case CPU_INFO_REG+M68K_SP:  sprintf(buffer[which], "SP :%08X", m68k_get_reg(context, M68K_REG_SP)); break;
/*TODO*///			case CPU_INFO_REG+M68K_ISP: sprintf(buffer[which], "ISP:%08X", m68k_get_reg(context, M68K_REG_ISP)); break;
/*TODO*///			case CPU_INFO_REG+M68K_USP: sprintf(buffer[which], "USP:%08X", m68k_get_reg(context, M68K_REG_USP)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D0:	sprintf(buffer[which], "D0 :%08X", m68k_get_reg(context, M68K_REG_D0)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D1:	sprintf(buffer[which], "D1 :%08X", m68k_get_reg(context, M68K_REG_D1)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D2:	sprintf(buffer[which], "D2 :%08X", m68k_get_reg(context, M68K_REG_D2)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D3:	sprintf(buffer[which], "D3 :%08X", m68k_get_reg(context, M68K_REG_D3)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D4:	sprintf(buffer[which], "D4 :%08X", m68k_get_reg(context, M68K_REG_D4)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D5:	sprintf(buffer[which], "D5 :%08X", m68k_get_reg(context, M68K_REG_D5)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D6:	sprintf(buffer[which], "D6 :%08X", m68k_get_reg(context, M68K_REG_D6)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D7:	sprintf(buffer[which], "D7 :%08X", m68k_get_reg(context, M68K_REG_D7)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A0:	sprintf(buffer[which], "A0 :%08X", m68k_get_reg(context, M68K_REG_A0)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A1:	sprintf(buffer[which], "A1 :%08X", m68k_get_reg(context, M68K_REG_A1)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A2:	sprintf(buffer[which], "A2 :%08X", m68k_get_reg(context, M68K_REG_A2)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A3:	sprintf(buffer[which], "A3 :%08X", m68k_get_reg(context, M68K_REG_A3)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A4:	sprintf(buffer[which], "A4 :%08X", m68k_get_reg(context, M68K_REG_A4)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A5:	sprintf(buffer[which], "A5 :%08X", m68k_get_reg(context, M68K_REG_A5)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A6:	sprintf(buffer[which], "A6 :%08X", m68k_get_reg(context, M68K_REG_A6)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A7:	sprintf(buffer[which], "A7 :%08X", m68k_get_reg(context, M68K_REG_A7)); break;
/*TODO*///			case CPU_INFO_REG+M68K_PREF_ADDR:	sprintf(buffer[which], "PAR:%08X", m68k_get_reg(context, M68K_REG_PREF_ADDR)); break;
/*TODO*///			case CPU_INFO_REG+M68K_PREF_DATA:	sprintf(buffer[which], "PDA:%08X", m68k_get_reg(context, M68K_REG_PREF_DATA)); break;
/*TODO*///			case CPU_INFO_FLAGS:
/*TODO*///				sr = m68k_get_reg(context, M68K_REG_SR);
/*TODO*///				sprintf(buffer[which], "%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c",
/*TODO*///					sr & 0x8000 ? 'T':'.',
/*TODO*///					sr & 0x4000 ? '?':'.',
/*TODO*///					sr & 0x2000 ? 'S':'.',
/*TODO*///					sr & 0x1000 ? '?':'.',
/*TODO*///					sr & 0x0800 ? '?':'.',
/*TODO*///					sr & 0x0400 ? 'I':'.',
/*TODO*///					sr & 0x0200 ? 'I':'.',
/*TODO*///					sr & 0x0100 ? 'I':'.',
/*TODO*///					sr & 0x0080 ? '?':'.',
/*TODO*///					sr & 0x0040 ? '?':'.',
/*TODO*///					sr & 0x0020 ? '?':'.',
/*TODO*///					sr & 0x0010 ? 'X':'.',
/*TODO*///					sr & 0x0008 ? 'N':'.',
/*TODO*///					sr & 0x0004 ? 'Z':'.',
/*TODO*///					sr & 0x0002 ? 'V':'.',
/*TODO*///					sr & 0x0001 ? 'C':'.');
/*TODO*///				break;
/*TODO*///			case CPU_INFO_NAME: return "68010";
/*TODO*///			case CPU_INFO_FAMILY: return "Motorola 68K";
/*TODO*///			case CPU_INFO_VERSION: return "3.2";
/*TODO*///			case CPU_INFO_FILE: return __FILE__;
/*TODO*///			case CPU_INFO_CREDITS: return "Copyright 1999-2000 Karl Stenerud. All rights reserved. (2.1 fixes HJB)";
/*TODO*///			case CPU_INFO_REG_LAYOUT: return (const char*)m68010_reg_layout;
/*TODO*///			case CPU_INFO_WIN_LAYOUT: return (const char*)m68010_win_layout;
/*TODO*///		}
/*TODO*///		return buffer[which];
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68010_dasm(char *buffer, unsigned pc)
/*TODO*///	{
/*TODO*///		M68K_SET_PC_CALLBACK(pc);
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///		return m68k_disassemble(buffer, pc, M68K_CPU_TYPE_68010);
/*TODO*///	#else
/*TODO*///		sprintf( buffer, "$%04X", m68k_read_immediate_16(pc) );
/*TODO*///		return 2;
/*TODO*///	#endif
/*TODO*///	}
/*TODO*///	
/*TODO*///	#endif /* HAS_M68010 */
/*TODO*///	
/*TODO*///	#endif // A68K0
/*TODO*///	
/*TODO*///	/****************************************************************************
/*TODO*///	 * M680EC20 section
/*TODO*///	 ****************************************************************************/
/*TODO*///	
/*TODO*///	#ifndef A68K2
/*TODO*///	
/*TODO*///	#if HAS_M68EC020
/*TODO*///	
/*TODO*///	static UINT8 m68ec020_reg_layout[] = {
/*TODO*///		M68K_PC,  M68K_MSP, -1,
/*TODO*///		M68K_SR,  M68K_ISP, -1,
/*TODO*///		M68K_SFC, M68K_USP, -1,
/*TODO*///		M68K_DFC, M68K_VBR, -1,
/*TODO*///		M68K_D0,  M68K_A0, -1,
/*TODO*///		M68K_D1,  M68K_A1, -1,
/*TODO*///		M68K_D2,  M68K_A2, -1,
/*TODO*///		M68K_D3,  M68K_A3, -1,
/*TODO*///		M68K_D4,  M68K_A4, -1,
/*TODO*///		M68K_D5,  M68K_A5, -1,
/*TODO*///		M68K_D6,  M68K_A6, -1,
/*TODO*///		M68K_D7,  M68K_A7, 0
/*TODO*///	};
/*TODO*///	
/*TODO*///	static UINT8 m68ec020_win_layout[] = {
/*TODO*///		48, 0,32,13,	/* register window (top right) */
/*TODO*///		 0, 0,47,13,	/* disassembler window (top left) */
/*TODO*///		 0,14,47, 8,	/* memory #1 window (left, middle) */
/*TODO*///		48,14,32, 8,	/* memory #2 window (right, middle) */
/*TODO*///		 0,23,80, 1 	/* command line window (bottom rows) */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	void m68ec020_init(void)
/*TODO*///	{
/*TODO*///		m68k_init();
/*TODO*///		m68k_set_cpu_type(M68K_CPU_TYPE_68EC020);
/*TODO*///		m68k_memory_intf = interface_a24_d32;
/*TODO*///		m68k_state_register("m68ec020");
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68ec020_reset(void* param)
/*TODO*///	{
/*TODO*///		m68k_pulse_reset();
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68ec020_exit(void)
/*TODO*///	{
/*TODO*///		/* nothing to do */
/*TODO*///	}
/*TODO*///	
/*TODO*///	int m68ec020_execute(int cycles)
/*TODO*///	{
/*TODO*///		return m68k_execute(cycles);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68ec020_get_context(void *dst)
/*TODO*///	{
/*TODO*///		return m68k_get_context(dst);
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68ec020_set_context(void *src)
/*TODO*///	{
/*TODO*///		if (m68k_memory_intf.read8 != cpu_readmem24bedw)
/*TODO*///			m68k_memory_intf = interface_a24_d32;
/*TODO*///		m68k_set_context(src);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68ec020_get_pc(void)
/*TODO*///	{
/*TODO*///		return m68k_get_reg(NULL, M68K_REG_PC)&0x00ffffff;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68ec020_set_pc(unsigned val)
/*TODO*///	{
/*TODO*///		m68k_set_reg(M68K_REG_PC, val&0x00ffffff);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68ec020_get_sp(void)
/*TODO*///	{
/*TODO*///		return m68k_get_reg(NULL, M68K_REG_SP);
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68ec020_set_sp(unsigned val)
/*TODO*///	{
/*TODO*///		m68k_set_reg(M68K_REG_SP, val);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68ec020_get_reg(int regnum)
/*TODO*///	{
/*TODO*///		switch( regnum )
/*TODO*///		{
/*TODO*///			case M68K_MSP: return m68k_get_reg(NULL, M68K_REG_MSP); /* 68020+ */
/*TODO*///			case M68K_CACR:  return m68k_get_reg(NULL, M68K_REG_CACR); /* 68020+ */
/*TODO*///			case M68K_CAAR:  return m68k_get_reg(NULL, M68K_REG_CAAR); /* 68020+ */
/*TODO*///			case M68K_VBR: return m68k_get_reg(NULL, M68K_REG_VBR); /* 68010+ */
/*TODO*///			case M68K_SFC: return m68k_get_reg(NULL, M68K_REG_SFC); /* 68010" */
/*TODO*///			case M68K_DFC: return m68k_get_reg(NULL, M68K_REG_DFC); /* 68010+ */
/*TODO*///			case M68K_PC:  return m68k_get_reg(NULL, M68K_REG_PC);
/*TODO*///			case M68K_SP:  return m68k_get_reg(NULL, M68K_REG_SP);
/*TODO*///			case M68K_ISP: return m68k_get_reg(NULL, M68K_REG_ISP);
/*TODO*///			case M68K_USP: return m68k_get_reg(NULL, M68K_REG_USP);
/*TODO*///			case M68K_SR:  return m68k_get_reg(NULL, M68K_REG_SR);
/*TODO*///			case M68K_D0:  return m68k_get_reg(NULL, M68K_REG_D0);
/*TODO*///			case M68K_D1:  return m68k_get_reg(NULL, M68K_REG_D1);
/*TODO*///			case M68K_D2:  return m68k_get_reg(NULL, M68K_REG_D2);
/*TODO*///			case M68K_D3:  return m68k_get_reg(NULL, M68K_REG_D3);
/*TODO*///			case M68K_D4:  return m68k_get_reg(NULL, M68K_REG_D4);
/*TODO*///			case M68K_D5:  return m68k_get_reg(NULL, M68K_REG_D5);
/*TODO*///			case M68K_D6:  return m68k_get_reg(NULL, M68K_REG_D6);
/*TODO*///			case M68K_D7:  return m68k_get_reg(NULL, M68K_REG_D7);
/*TODO*///			case M68K_A0:  return m68k_get_reg(NULL, M68K_REG_A0);
/*TODO*///			case M68K_A1:  return m68k_get_reg(NULL, M68K_REG_A1);
/*TODO*///			case M68K_A2:  return m68k_get_reg(NULL, M68K_REG_A2);
/*TODO*///			case M68K_A3:  return m68k_get_reg(NULL, M68K_REG_A3);
/*TODO*///			case M68K_A4:  return m68k_get_reg(NULL, M68K_REG_A4);
/*TODO*///			case M68K_A5:  return m68k_get_reg(NULL, M68K_REG_A5);
/*TODO*///			case M68K_A6:  return m68k_get_reg(NULL, M68K_REG_A6);
/*TODO*///			case M68K_A7:  return m68k_get_reg(NULL, M68K_REG_A7);
/*TODO*///			case M68K_PREF_ADDR:  return m68k_get_reg(NULL, M68K_REG_PREF_ADDR);
/*TODO*///			case M68K_PREF_DATA:  return m68k_get_reg(NULL, M68K_REG_PREF_DATA);
/*TODO*///			case REG_PREVIOUSPC: return m68k_get_reg(NULL, M68K_REG_PPC);
/*TODO*///	/* TODO: return contents of [SP + wordsize * (REG_SP_CONTENTS-regnum)] */
/*TODO*///			default:
/*TODO*///				if( regnum < REG_SP_CONTENTS )
/*TODO*///				{
/*TODO*///					unsigned offset = m68k_get_reg(NULL, M68K_REG_SP) + 4 * (REG_SP_CONTENTS - regnum);
/*TODO*///					if( offset < 0xfffffd )
/*TODO*///						return m68k_read_memory_32( offset );
/*TODO*///				}
/*TODO*///		}
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68ec020_set_reg(int regnum, unsigned val)
/*TODO*///	{
/*TODO*///		switch( regnum )
/*TODO*///		{
/*TODO*///			case M68K_MSP:  m68k_set_reg(M68K_REG_MSP, val); break; /* 68020+ */
/*TODO*///			case M68K_CACR: m68k_set_reg(M68K_REG_CACR, val); break; /* 68020+ */
/*TODO*///			case M68K_CAAR: m68k_set_reg(M68K_REG_CAAR, val); break; /* 68020+ */
/*TODO*///			case M68K_VBR: m68k_set_reg(M68K_REG_VBR, val); break; /* 68010+ */
/*TODO*///			case M68K_SFC: m68k_set_reg(M68K_REG_SFC, val); break; /* 68010+ */
/*TODO*///			case M68K_DFC: m68k_set_reg(M68K_REG_DFC, val); break; /* 68010+ */
/*TODO*///			case M68K_PC:  m68k_set_reg(M68K_REG_PC, val); break;
/*TODO*///			case M68K_SP:  m68k_set_reg(M68K_REG_SP, val); break;
/*TODO*///			case M68K_ISP: m68k_set_reg(M68K_REG_ISP, val); break;
/*TODO*///			case M68K_USP: m68k_set_reg(M68K_REG_USP, val); break;
/*TODO*///			case M68K_SR:  m68k_set_reg(M68K_REG_SR, val); break;
/*TODO*///			case M68K_D0:  m68k_set_reg(M68K_REG_D0, val); break;
/*TODO*///			case M68K_D1:  m68k_set_reg(M68K_REG_D1, val); break;
/*TODO*///			case M68K_D2:  m68k_set_reg(M68K_REG_D2, val); break;
/*TODO*///			case M68K_D3:  m68k_set_reg(M68K_REG_D3, val); break;
/*TODO*///			case M68K_D4:  m68k_set_reg(M68K_REG_D4, val); break;
/*TODO*///			case M68K_D5:  m68k_set_reg(M68K_REG_D5, val); break;
/*TODO*///			case M68K_D6:  m68k_set_reg(M68K_REG_D6, val); break;
/*TODO*///			case M68K_D7:  m68k_set_reg(M68K_REG_D7, val); break;
/*TODO*///			case M68K_A0:  m68k_set_reg(M68K_REG_A0, val); break;
/*TODO*///			case M68K_A1:  m68k_set_reg(M68K_REG_A1, val); break;
/*TODO*///			case M68K_A2:  m68k_set_reg(M68K_REG_A2, val); break;
/*TODO*///			case M68K_A3:  m68k_set_reg(M68K_REG_A3, val); break;
/*TODO*///			case M68K_A4:  m68k_set_reg(M68K_REG_A4, val); break;
/*TODO*///			case M68K_A5:  m68k_set_reg(M68K_REG_A5, val); break;
/*TODO*///			case M68K_A6:  m68k_set_reg(M68K_REG_A6, val); break;
/*TODO*///			case M68K_A7:  m68k_set_reg(M68K_REG_A7, val); break;
/*TODO*///	/* TODO: set contents of [SP + wordsize * (REG_SP_CONTENTS-regnum)] */
/*TODO*///			default:
/*TODO*///				if( regnum < REG_SP_CONTENTS )
/*TODO*///				{
/*TODO*///					unsigned offset = m68k_get_reg(NULL, M68K_REG_SP) + 4 * (REG_SP_CONTENTS - regnum);
/*TODO*///					if( offset < 0xfffffd )
/*TODO*///						m68k_write_memory_16( offset, val );
/*TODO*///				}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68ec020_set_nmi_line(int state)
/*TODO*///	{
/*TODO*///		switch(state)
/*TODO*///		{
/*TODO*///			case CLEAR_LINE:
/*TODO*///				m68k_set_irq(0);
/*TODO*///				break;
/*TODO*///			case ASSERT_LINE:
/*TODO*///				m68k_set_irq(7);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				m68k_set_irq(7);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68ec020_set_irq_line(int irqline, int state)
/*TODO*///	{
/*TODO*///		switch(state)
/*TODO*///		{
/*TODO*///			case CLEAR_LINE:
/*TODO*///				m68k_set_irq(0);
/*TODO*///				break;
/*TODO*///			case ASSERT_LINE:
/*TODO*///				m68k_set_irq(irqline);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				m68k_set_irq(irqline);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68ec020_set_irq_callback(int (*callback)(int irqline))
/*TODO*///	{
/*TODO*///		m68k_set_int_ack_callback(callback);
/*TODO*///	}
/*TODO*///	
/*TODO*///	const char *m68ec020_info(void *context, int regnum)
/*TODO*///	{
/*TODO*///		static char buffer[32][47+1];
/*TODO*///		static int which = 0;
/*TODO*///		int sr;
/*TODO*///	
/*TODO*///		which = (which+1) % 32;
/*TODO*///		buffer[which][0] = '\0';
/*TODO*///	
/*TODO*///		switch( regnum )
/*TODO*///		{
/*TODO*///			case CPU_INFO_REG+M68K_MSP:  sprintf(buffer[which], "MSP:%08X", m68k_get_reg(context, M68K_REG_MSP)); break;
/*TODO*///			case CPU_INFO_REG+M68K_CACR: sprintf(buffer[which], "CCR:%08X", m68k_get_reg(context, M68K_REG_CACR)); break;
/*TODO*///			case CPU_INFO_REG+M68K_CAAR: sprintf(buffer[which], "CAR:%08X", m68k_get_reg(context, M68K_REG_CAAR)); break;
/*TODO*///			case CPU_INFO_REG+M68K_SFC: sprintf(buffer[which], "SFC:%X",   m68k_get_reg(context, M68K_REG_SFC)); break;
/*TODO*///			case CPU_INFO_REG+M68K_DFC: sprintf(buffer[which], "DFC:%X",   m68k_get_reg(context, M68K_REG_DFC)); break;
/*TODO*///			case CPU_INFO_REG+M68K_VBR: sprintf(buffer[which], "VBR:%08X", m68k_get_reg(context, M68K_REG_VBR)); break;
/*TODO*///			case CPU_INFO_REG+M68K_PC:	sprintf(buffer[which], "PC :%08X", m68k_get_reg(context, M68K_REG_PC)); break;
/*TODO*///			case CPU_INFO_REG+M68K_SR:  sprintf(buffer[which], "SR :%04X", m68k_get_reg(context, M68K_REG_SR)); break;
/*TODO*///			case CPU_INFO_REG+M68K_SP:  sprintf(buffer[which], "SP :%08X", m68k_get_reg(context, M68K_REG_SP)); break;
/*TODO*///			case CPU_INFO_REG+M68K_ISP: sprintf(buffer[which], "ISP:%08X", m68k_get_reg(context, M68K_REG_ISP)); break;
/*TODO*///			case CPU_INFO_REG+M68K_USP: sprintf(buffer[which], "USP:%08X", m68k_get_reg(context, M68K_REG_USP)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D0:	sprintf(buffer[which], "D0 :%08X", m68k_get_reg(context, M68K_REG_D0)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D1:	sprintf(buffer[which], "D1 :%08X", m68k_get_reg(context, M68K_REG_D1)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D2:	sprintf(buffer[which], "D2 :%08X", m68k_get_reg(context, M68K_REG_D2)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D3:	sprintf(buffer[which], "D3 :%08X", m68k_get_reg(context, M68K_REG_D3)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D4:	sprintf(buffer[which], "D4 :%08X", m68k_get_reg(context, M68K_REG_D4)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D5:	sprintf(buffer[which], "D5 :%08X", m68k_get_reg(context, M68K_REG_D5)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D6:	sprintf(buffer[which], "D6 :%08X", m68k_get_reg(context, M68K_REG_D6)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D7:	sprintf(buffer[which], "D7 :%08X", m68k_get_reg(context, M68K_REG_D7)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A0:	sprintf(buffer[which], "A0 :%08X", m68k_get_reg(context, M68K_REG_A0)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A1:	sprintf(buffer[which], "A1 :%08X", m68k_get_reg(context, M68K_REG_A1)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A2:	sprintf(buffer[which], "A2 :%08X", m68k_get_reg(context, M68K_REG_A2)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A3:	sprintf(buffer[which], "A3 :%08X", m68k_get_reg(context, M68K_REG_A3)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A4:	sprintf(buffer[which], "A4 :%08X", m68k_get_reg(context, M68K_REG_A4)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A5:	sprintf(buffer[which], "A5 :%08X", m68k_get_reg(context, M68K_REG_A5)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A6:	sprintf(buffer[which], "A6 :%08X", m68k_get_reg(context, M68K_REG_A6)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A7:	sprintf(buffer[which], "A7 :%08X", m68k_get_reg(context, M68K_REG_A7)); break;
/*TODO*///			case CPU_INFO_REG+M68K_PREF_ADDR:	sprintf(buffer[which], "PAR:%08X", m68k_get_reg(context, M68K_REG_PREF_ADDR)); break;
/*TODO*///			case CPU_INFO_REG+M68K_PREF_DATA:	sprintf(buffer[which], "PDA:%08X", m68k_get_reg(context, M68K_REG_PREF_DATA)); break;
/*TODO*///			case CPU_INFO_FLAGS:
/*TODO*///				sr = m68k_get_reg(context, M68K_REG_SR);
/*TODO*///				sprintf(buffer[which], "%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c",
/*TODO*///					sr & 0x8000 ? 'T':'.',
/*TODO*///					sr & 0x4000 ? 't':'.',
/*TODO*///					sr & 0x2000 ? 'S':'.',
/*TODO*///					sr & 0x1000 ? 'M':'.',
/*TODO*///					sr & 0x0800 ? '?':'.',
/*TODO*///					sr & 0x0400 ? 'I':'.',
/*TODO*///					sr & 0x0200 ? 'I':'.',
/*TODO*///					sr & 0x0100 ? 'I':'.',
/*TODO*///					sr & 0x0080 ? '?':'.',
/*TODO*///					sr & 0x0040 ? '?':'.',
/*TODO*///					sr & 0x0020 ? '?':'.',
/*TODO*///					sr & 0x0010 ? 'X':'.',
/*TODO*///					sr & 0x0008 ? 'N':'.',
/*TODO*///					sr & 0x0004 ? 'Z':'.',
/*TODO*///					sr & 0x0002 ? 'V':'.',
/*TODO*///					sr & 0x0001 ? 'C':'.');
/*TODO*///				break;
/*TODO*///			case CPU_INFO_NAME: return "68EC020";
/*TODO*///			case CPU_INFO_FAMILY: return "Motorola 68K";
/*TODO*///			case CPU_INFO_VERSION: return "3.2";
/*TODO*///			case CPU_INFO_FILE: return __FILE__;
/*TODO*///			case CPU_INFO_CREDITS: return "Copyright 1999-2000 Karl Stenerud. All rights reserved. (2.1 fixes HJB)";
/*TODO*///			case CPU_INFO_REG_LAYOUT: return (const char*)m68ec020_reg_layout;
/*TODO*///			case CPU_INFO_WIN_LAYOUT: return (const char*)m68ec020_win_layout;
/*TODO*///		}
/*TODO*///		return buffer[which];
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68ec020_dasm(char *buffer, unsigned pc)
/*TODO*///	{
/*TODO*///		M68K_SET_PC_CALLBACK(pc);
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///		return m68k_disassemble(buffer, pc, M68K_CPU_TYPE_68020);
/*TODO*///	#else
/*TODO*///		sprintf( buffer, "$%04X", m68k_read_immediate_16(pc) );
/*TODO*///		return 2;
/*TODO*///	#endif
/*TODO*///	}
/*TODO*///	#endif /* HAS_M68EC020 */
/*TODO*///	
/*TODO*///	/****************************************************************************
/*TODO*///	 * M68020 section
/*TODO*///	 ****************************************************************************/
/*TODO*///	#if HAS_M68020
/*TODO*///	
/*TODO*///	static UINT8 m68020_reg_layout[] = {
/*TODO*///		M68K_PC,  M68K_MSP, -1,
/*TODO*///		M68K_SR,  M68K_ISP, -1,
/*TODO*///		M68K_SFC, M68K_USP, -1,
/*TODO*///		M68K_DFC, M68K_VBR, -1,
/*TODO*///		M68K_D0,  M68K_A0, -1,
/*TODO*///		M68K_D1,  M68K_A1, -1,
/*TODO*///		M68K_D2,  M68K_A2, -1,
/*TODO*///		M68K_D3,  M68K_A3, -1,
/*TODO*///		M68K_D4,  M68K_A4, -1,
/*TODO*///		M68K_D5,  M68K_A5, -1,
/*TODO*///		M68K_D6,  M68K_A6, -1,
/*TODO*///		M68K_D7,  M68K_A7, 0
/*TODO*///	};
/*TODO*///	
/*TODO*///	static UINT8 m68020_win_layout[] = {
/*TODO*///		48, 0,32,13,	/* register window (top right) */
/*TODO*///		 0, 0,47,13,	/* disassembler window (top left) */
/*TODO*///		 0,14,47, 8,	/* memory #1 window (left, middle) */
/*TODO*///		48,14,32, 8,	/* memory #2 window (right, middle) */
/*TODO*///		 0,23,80, 1 	/* command line window (bottom rows) */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	void m68020_init(void)
/*TODO*///	{
/*TODO*///		m68k_init();
/*TODO*///		m68k_set_cpu_type(M68K_CPU_TYPE_68020);
/*TODO*///		m68k_memory_intf = interface_a32_d32;
/*TODO*///		m68k_state_register("m68020");
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68020_reset(void* param)
/*TODO*///	{
/*TODO*///		m68k_pulse_reset();
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68020_exit(void)
/*TODO*///	{
/*TODO*///		/* nothing to do */
/*TODO*///	}
/*TODO*///	
/*TODO*///	int m68020_execute(int cycles)
/*TODO*///	{
/*TODO*///		return m68k_execute(cycles);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68020_get_context(void *dst)
/*TODO*///	{
/*TODO*///		return m68k_get_context(dst);
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68020_set_context(void *src)
/*TODO*///	{
/*TODO*///		if (m68k_memory_intf.read8 != cpu_readmem32bedw)
/*TODO*///			m68k_memory_intf = interface_a32_d32;
/*TODO*///		m68k_set_context(src);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68020_get_pc(void)
/*TODO*///	{
/*TODO*///		return m68k_get_reg(NULL, M68K_REG_PC);
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68020_set_pc(unsigned val)
/*TODO*///	{
/*TODO*///		m68k_set_reg(M68K_REG_PC, val);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68020_get_sp(void)
/*TODO*///	{
/*TODO*///		return m68k_get_reg(NULL, M68K_REG_SP);
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68020_set_sp(unsigned val)
/*TODO*///	{
/*TODO*///		m68k_set_reg(M68K_REG_SP, val);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68020_get_reg(int regnum)
/*TODO*///	{
/*TODO*///		switch( regnum )
/*TODO*///		{
/*TODO*///			case M68K_MSP: return m68k_get_reg(NULL, M68K_REG_MSP); /* 68020+ */
/*TODO*///			case M68K_CACR:  return m68k_get_reg(NULL, M68K_REG_CACR); /* 68020+ */
/*TODO*///			case M68K_CAAR:  return m68k_get_reg(NULL, M68K_REG_CAAR); /* 68020+ */
/*TODO*///			case M68K_VBR: return m68k_get_reg(NULL, M68K_REG_VBR); /* 68010+ */
/*TODO*///			case M68K_SFC: return m68k_get_reg(NULL, M68K_REG_SFC); /* 68010" */
/*TODO*///			case M68K_DFC: return m68k_get_reg(NULL, M68K_REG_DFC); /* 68010+ */
/*TODO*///			case M68K_PC:  return m68k_get_reg(NULL, M68K_REG_PC);
/*TODO*///			case M68K_SP:  return m68k_get_reg(NULL, M68K_REG_SP);
/*TODO*///			case M68K_ISP: return m68k_get_reg(NULL, M68K_REG_ISP);
/*TODO*///			case M68K_USP: return m68k_get_reg(NULL, M68K_REG_USP);
/*TODO*///			case M68K_SR:  return m68k_get_reg(NULL, M68K_REG_SR);
/*TODO*///			case M68K_D0:  return m68k_get_reg(NULL, M68K_REG_D0);
/*TODO*///			case M68K_D1:  return m68k_get_reg(NULL, M68K_REG_D1);
/*TODO*///			case M68K_D2:  return m68k_get_reg(NULL, M68K_REG_D2);
/*TODO*///			case M68K_D3:  return m68k_get_reg(NULL, M68K_REG_D3);
/*TODO*///			case M68K_D4:  return m68k_get_reg(NULL, M68K_REG_D4);
/*TODO*///			case M68K_D5:  return m68k_get_reg(NULL, M68K_REG_D5);
/*TODO*///			case M68K_D6:  return m68k_get_reg(NULL, M68K_REG_D6);
/*TODO*///			case M68K_D7:  return m68k_get_reg(NULL, M68K_REG_D7);
/*TODO*///			case M68K_A0:  return m68k_get_reg(NULL, M68K_REG_A0);
/*TODO*///			case M68K_A1:  return m68k_get_reg(NULL, M68K_REG_A1);
/*TODO*///			case M68K_A2:  return m68k_get_reg(NULL, M68K_REG_A2);
/*TODO*///			case M68K_A3:  return m68k_get_reg(NULL, M68K_REG_A3);
/*TODO*///			case M68K_A4:  return m68k_get_reg(NULL, M68K_REG_A4);
/*TODO*///			case M68K_A5:  return m68k_get_reg(NULL, M68K_REG_A5);
/*TODO*///			case M68K_A6:  return m68k_get_reg(NULL, M68K_REG_A6);
/*TODO*///			case M68K_A7:  return m68k_get_reg(NULL, M68K_REG_A7);
/*TODO*///			case M68K_PREF_ADDR:  return m68k_get_reg(NULL, M68K_REG_PREF_ADDR);
/*TODO*///			case M68K_PREF_DATA:  return m68k_get_reg(NULL, M68K_REG_PREF_DATA);
/*TODO*///			case REG_PREVIOUSPC: return m68k_get_reg(NULL, M68K_REG_PPC);
/*TODO*///	/* TODO: return contents of [SP + wordsize * (REG_SP_CONTENTS-regnum)] */
/*TODO*///			default:
/*TODO*///				if( regnum < REG_SP_CONTENTS )
/*TODO*///				{
/*TODO*///					unsigned offset = m68k_get_reg(NULL, M68K_REG_SP) + 4 * (REG_SP_CONTENTS - regnum);
/*TODO*///					if( offset < 0xfffffd )
/*TODO*///						return m68k_read_memory_32( offset );
/*TODO*///				}
/*TODO*///		}
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68020_set_reg(int regnum, unsigned val)
/*TODO*///	{
/*TODO*///		switch( regnum )
/*TODO*///		{
/*TODO*///			case M68K_MSP:  m68k_set_reg(M68K_REG_MSP, val); break; /* 68020+ */
/*TODO*///			case M68K_CACR: m68k_set_reg(M68K_REG_CACR, val); break; /* 68020+ */
/*TODO*///			case M68K_CAAR: m68k_set_reg(M68K_REG_CAAR, val); break; /* 68020+ */
/*TODO*///			case M68K_VBR: m68k_set_reg(M68K_REG_VBR, val); break; /* 68010+ */
/*TODO*///			case M68K_SFC: m68k_set_reg(M68K_REG_SFC, val); break; /* 68010+ */
/*TODO*///			case M68K_DFC: m68k_set_reg(M68K_REG_DFC, val); break; /* 68010+ */
/*TODO*///			case M68K_PC:  m68k_set_reg(M68K_REG_PC, val); break;
/*TODO*///			case M68K_SP:  m68k_set_reg(M68K_REG_SP, val); break;
/*TODO*///			case M68K_ISP: m68k_set_reg(M68K_REG_ISP, val); break;
/*TODO*///			case M68K_USP: m68k_set_reg(M68K_REG_USP, val); break;
/*TODO*///			case M68K_SR:  m68k_set_reg(M68K_REG_SR, val); break;
/*TODO*///			case M68K_D0:  m68k_set_reg(M68K_REG_D0, val); break;
/*TODO*///			case M68K_D1:  m68k_set_reg(M68K_REG_D1, val); break;
/*TODO*///			case M68K_D2:  m68k_set_reg(M68K_REG_D2, val); break;
/*TODO*///			case M68K_D3:  m68k_set_reg(M68K_REG_D3, val); break;
/*TODO*///			case M68K_D4:  m68k_set_reg(M68K_REG_D4, val); break;
/*TODO*///			case M68K_D5:  m68k_set_reg(M68K_REG_D5, val); break;
/*TODO*///			case M68K_D6:  m68k_set_reg(M68K_REG_D6, val); break;
/*TODO*///			case M68K_D7:  m68k_set_reg(M68K_REG_D7, val); break;
/*TODO*///			case M68K_A0:  m68k_set_reg(M68K_REG_A0, val); break;
/*TODO*///			case M68K_A1:  m68k_set_reg(M68K_REG_A1, val); break;
/*TODO*///			case M68K_A2:  m68k_set_reg(M68K_REG_A2, val); break;
/*TODO*///			case M68K_A3:  m68k_set_reg(M68K_REG_A3, val); break;
/*TODO*///			case M68K_A4:  m68k_set_reg(M68K_REG_A4, val); break;
/*TODO*///			case M68K_A5:  m68k_set_reg(M68K_REG_A5, val); break;
/*TODO*///			case M68K_A6:  m68k_set_reg(M68K_REG_A6, val); break;
/*TODO*///			case M68K_A7:  m68k_set_reg(M68K_REG_A7, val); break;
/*TODO*///	/* TODO: set contents of [SP + wordsize * (REG_SP_CONTENTS-regnum)] */
/*TODO*///			default:
/*TODO*///				if( regnum < REG_SP_CONTENTS )
/*TODO*///				{
/*TODO*///					unsigned offset = m68k_get_reg(NULL, M68K_REG_SP) + 4 * (REG_SP_CONTENTS - regnum);
/*TODO*///					if( offset < 0xfffffd )
/*TODO*///						m68k_write_memory_16( offset, val );
/*TODO*///				}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68020_set_nmi_line(int state)
/*TODO*///	{
/*TODO*///		switch(state)
/*TODO*///		{
/*TODO*///			case CLEAR_LINE:
/*TODO*///				m68k_set_irq(0);
/*TODO*///				break;
/*TODO*///			case ASSERT_LINE:
/*TODO*///				m68k_set_irq(7);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				m68k_set_irq(7);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68020_set_irq_line(int irqline, int state)
/*TODO*///	{
/*TODO*///		switch(state)
/*TODO*///		{
/*TODO*///			case CLEAR_LINE:
/*TODO*///				m68k_set_irq(0);
/*TODO*///				break;
/*TODO*///			case ASSERT_LINE:
/*TODO*///				m68k_set_irq(irqline);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				m68k_set_irq(irqline);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68020_set_irq_callback(int (*callback)(int irqline))
/*TODO*///	{
/*TODO*///		m68k_set_int_ack_callback(callback);
/*TODO*///	}
/*TODO*///	
/*TODO*///	const char *m68020_info(void *context, int regnum)
/*TODO*///	{
/*TODO*///		static char buffer[32][47+1];
/*TODO*///		static int which = 0;
/*TODO*///		int sr;
/*TODO*///	
/*TODO*///		which = (which+1) % 32;
/*TODO*///		buffer[which][0] = '\0';
/*TODO*///	
/*TODO*///		switch( regnum )
/*TODO*///		{
/*TODO*///			case CPU_INFO_REG+M68K_MSP:  sprintf(buffer[which], "MSP:%08X", m68k_get_reg(context, M68K_REG_MSP)); break;
/*TODO*///			case CPU_INFO_REG+M68K_CACR: sprintf(buffer[which], "CCR:%08X", m68k_get_reg(context, M68K_REG_CACR)); break;
/*TODO*///			case CPU_INFO_REG+M68K_CAAR: sprintf(buffer[which], "CAR:%08X", m68k_get_reg(context, M68K_REG_CAAR)); break;
/*TODO*///			case CPU_INFO_REG+M68K_SFC: sprintf(buffer[which], "SFC:%X",   m68k_get_reg(context, M68K_REG_SFC)); break;
/*TODO*///			case CPU_INFO_REG+M68K_DFC: sprintf(buffer[which], "DFC:%X",   m68k_get_reg(context, M68K_REG_DFC)); break;
/*TODO*///			case CPU_INFO_REG+M68K_VBR: sprintf(buffer[which], "VBR:%08X", m68k_get_reg(context, M68K_REG_VBR)); break;
/*TODO*///			case CPU_INFO_REG+M68K_PC:	sprintf(buffer[which], "PC :%08X", m68k_get_reg(context, M68K_REG_PC)); break;
/*TODO*///			case CPU_INFO_REG+M68K_SR:  sprintf(buffer[which], "SR :%04X", m68k_get_reg(context, M68K_REG_SR)); break;
/*TODO*///			case CPU_INFO_REG+M68K_SP:  sprintf(buffer[which], "SP :%08X", m68k_get_reg(context, M68K_REG_SP)); break;
/*TODO*///			case CPU_INFO_REG+M68K_ISP: sprintf(buffer[which], "ISP:%08X", m68k_get_reg(context, M68K_REG_ISP)); break;
/*TODO*///			case CPU_INFO_REG+M68K_USP: sprintf(buffer[which], "USP:%08X", m68k_get_reg(context, M68K_REG_USP)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D0:	sprintf(buffer[which], "D0 :%08X", m68k_get_reg(context, M68K_REG_D0)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D1:	sprintf(buffer[which], "D1 :%08X", m68k_get_reg(context, M68K_REG_D1)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D2:	sprintf(buffer[which], "D2 :%08X", m68k_get_reg(context, M68K_REG_D2)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D3:	sprintf(buffer[which], "D3 :%08X", m68k_get_reg(context, M68K_REG_D3)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D4:	sprintf(buffer[which], "D4 :%08X", m68k_get_reg(context, M68K_REG_D4)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D5:	sprintf(buffer[which], "D5 :%08X", m68k_get_reg(context, M68K_REG_D5)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D6:	sprintf(buffer[which], "D6 :%08X", m68k_get_reg(context, M68K_REG_D6)); break;
/*TODO*///			case CPU_INFO_REG+M68K_D7:	sprintf(buffer[which], "D7 :%08X", m68k_get_reg(context, M68K_REG_D7)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A0:	sprintf(buffer[which], "A0 :%08X", m68k_get_reg(context, M68K_REG_A0)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A1:	sprintf(buffer[which], "A1 :%08X", m68k_get_reg(context, M68K_REG_A1)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A2:	sprintf(buffer[which], "A2 :%08X", m68k_get_reg(context, M68K_REG_A2)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A3:	sprintf(buffer[which], "A3 :%08X", m68k_get_reg(context, M68K_REG_A3)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A4:	sprintf(buffer[which], "A4 :%08X", m68k_get_reg(context, M68K_REG_A4)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A5:	sprintf(buffer[which], "A5 :%08X", m68k_get_reg(context, M68K_REG_A5)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A6:	sprintf(buffer[which], "A6 :%08X", m68k_get_reg(context, M68K_REG_A6)); break;
/*TODO*///			case CPU_INFO_REG+M68K_A7:	sprintf(buffer[which], "A7 :%08X", m68k_get_reg(context, M68K_REG_A7)); break;
/*TODO*///			case CPU_INFO_REG+M68K_PREF_ADDR:	sprintf(buffer[which], "PAR:%08X", m68k_get_reg(context, M68K_REG_PREF_ADDR)); break;
/*TODO*///			case CPU_INFO_REG+M68K_PREF_DATA:	sprintf(buffer[which], "PDA:%08X", m68k_get_reg(context, M68K_REG_PREF_DATA)); break;
/*TODO*///			case CPU_INFO_FLAGS:
/*TODO*///				sr = m68k_get_reg(context, M68K_REG_SR);
/*TODO*///				sprintf(buffer[which], "%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c",
/*TODO*///					sr & 0x8000 ? 'T':'.',
/*TODO*///					sr & 0x4000 ? 't':'.',
/*TODO*///					sr & 0x2000 ? 'S':'.',
/*TODO*///					sr & 0x1000 ? 'M':'.',
/*TODO*///					sr & 0x0800 ? '?':'.',
/*TODO*///					sr & 0x0400 ? 'I':'.',
/*TODO*///					sr & 0x0200 ? 'I':'.',
/*TODO*///					sr & 0x0100 ? 'I':'.',
/*TODO*///					sr & 0x0080 ? '?':'.',
/*TODO*///					sr & 0x0040 ? '?':'.',
/*TODO*///					sr & 0x0020 ? '?':'.',
/*TODO*///					sr & 0x0010 ? 'X':'.',
/*TODO*///					sr & 0x0008 ? 'N':'.',
/*TODO*///					sr & 0x0004 ? 'Z':'.',
/*TODO*///					sr & 0x0002 ? 'V':'.',
/*TODO*///					sr & 0x0001 ? 'C':'.');
/*TODO*///				break;
/*TODO*///			case CPU_INFO_NAME: return "68020";
/*TODO*///			case CPU_INFO_FAMILY: return "Motorola 68K";
/*TODO*///			case CPU_INFO_VERSION: return "3.2";
/*TODO*///			case CPU_INFO_FILE: return __FILE__;
/*TODO*///			case CPU_INFO_CREDITS: return "Copyright 1999-2000 Karl Stenerud. All rights reserved. (2.1 fixes HJB)";
/*TODO*///			case CPU_INFO_REG_LAYOUT: return (const char*)m68020_reg_layout;
/*TODO*///			case CPU_INFO_WIN_LAYOUT: return (const char*)m68020_win_layout;
/*TODO*///		}
/*TODO*///		return buffer[which];
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned m68020_dasm(char *buffer, unsigned pc)
/*TODO*///	{
/*TODO*///		M68K_SET_PC_CALLBACK(pc);
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///		return m68k_disassemble(buffer, pc, M68K_CPU_TYPE_68020);
/*TODO*///	#else
/*TODO*///		sprintf( buffer, "$%04X", m68k_read_immediate_16(pc) );
/*TODO*///		return 2;
/*TODO*///	#endif
/*TODO*///	}
/*TODO*///	#endif /* HAS_M68020 */
/*TODO*///	
/*TODO*///	#endif // A68K2

    @Override
    public void init() {
        m68000_init();
    }

    @Override
    public void reset(Object param) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void exit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int execute(int cycles) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object init_context() {
        Object reg = new m68ki_cpu_core();
        return reg;
    }

    @Override
    public Object get_context() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_context(Object reg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int[] get_cycle_table(int which) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_cycle_table(int which, int[] new_table) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_pc() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_pc(int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_sp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_sp(int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_reg(int regnum) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_reg(int regnum, int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_nmi_line(int linestate) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_irq_line(int irqline, int linestate) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_irq_callback(cpuintrfH.irqcallbacksPtr callback) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void internal_interrupt(int type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String cpu_info(Object context, int regnum) {
        return m68000_info(context, regnum);
    }

    @Override
    public int memory_read(int offset) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void memory_write(int offset, int data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int internal_read(int offset) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void internal_write(int offset, int data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_op_base(int pc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int mem_address_bits_of_cpu() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
