/*****************************************************************************

	h6280.c - Portable HuC6280 emulator

	Copyright (c) 1999, 2000 Bryan McPhail, mish@tendril.co.uk

	This source code is based (with permission!) on the 6502 emulator by
	Juergen Buchmueller.  It is released as part of the Mame emulator project.
	Let me know if you intend to use this code in any other project.


	NOTICE:

	This code is around 99% complete!  Several things are unimplemented,
	some due to lack of time, some due to lack of documentation, mainly
	due to lack of programs using these features.

	csh, csl opcodes are not supported.
	set opcode and T flag behaviour are not supported.

	I am unsure if instructions like SBC take an extra cycle when used in
	decimal mode.  I am unsure if flag B is set upon execution of rti.

	Cycle counts should be quite accurate, illegal instructions are assumed
	to take two cycles.


	Changelog, version 1.02:
		JMP + indirect X (0x7c) opcode fixed.
		SMB + RMB opcodes fixed in disassembler.
		change_pc function calls removed.
		TSB & TRB now set flags properly.
		BIT opcode altered.

	Changelog, version 1.03:
		Swapped IRQ mask for IRQ1 & IRQ2 (thanks Yasuhiro)

	Changelog, version 1.04, 28/9/99-22/10/99:
		Adjusted RTI (thanks Karl)
 		TST opcodes fixed in disassembler (missing break statements in a case!).
		TST behaviour fixed.
		SMB/RMB/BBS/BBR fixed in disassembler.

	Changelog, version 1.05, 8/12/99-16/12/99:
		Added CAB's timer implementation (note: irq ack & timer reload are changed).
		Fixed STA IDX.
		Fixed B flag setting on BRK.
		Assumed CSH & CSL to take 2 cycles each.

		Todo:  Performance could be improved by precalculating timer fire position.

	Changelog, version 1.06, 4/5/00 - last opcode bug found?
		JMP indirect was doing a EAL++; instead of EAD++; - Obviously causing
		a corrupt read when L = 0xff!  This fixes Bloody Wolf and Trio The Punch!

	Changelog, version 1.07, 3/9/00:
		Changed timer to be single shot - fixes Crude Buster music in level 1.

******************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.cpu.h6280;

import common.libc.cstdio.FILE;
import static common.libc.cstdio.*;
import static gr.codebb.arcadeflex.WIP.v037b16.cpu.h6280.h6280H.*;
import static gr.codebb.arcadeflex.WIP.v037b16.cpu.h6280.h6280ops.*;
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
import gr.codebb.arcadeflex.v037b16.mame.cpuintrfH;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;

public class h6280 extends cpu_interface {
    
    public static FILE h6280log = null;//fopen("h6280.log", "wa");  //for debug purposes

    public static int[] h6280_ICount = new int[1];

    public h6280() {
        cpu_num = CPU_H6280;
        num_irqs = 3;
        default_vector = 0;
        overclock = 1.0;
        no_int = H6280_INT_NONE;
        irq_int = -1;
        nmi_int = H6280_INT_NMI;
        databus_width = 8;
        address_bits = 21;
        address_shift = 0;
        endianess = CPU_IS_LE;
        align_unit = 1;
        max_inst_len = 3;
        //abits1 = ABITS1_21;
        //abits2 = ABITS2_21;
        //abitsmin = ABITS_MIN_21;
        icount = h6280_ICount;
    }

    @Override
    public void init() {
        h6280_init();
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
        Object reg = new h6280_Regs();
        return reg;
    }

    @Override
    public Object get_context() {
        return h6280_get_context();
    }

    @Override
    public void set_context(Object reg) {
        h6280_set_context(reg);
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
        return h6280_info(context, regnum);
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
        return 21; //21
    }
	
	
/*TODO*///	extern FILE * errorlog;
/*TODO*///	extern unsigned cpu_get_pc(void);
/*TODO*///	
/*TODO*///	static UINT8 reg_layout[] = {
/*TODO*///		H6280_PC, H6280_S, H6280_P, H6280_A, H6280_X, H6280_Y, -1,
/*TODO*///		H6280_IRQ_MASK, H6280_TIMER_STATE, H6280_NMI_STATE, H6280_IRQ1_STATE, H6280_IRQ2_STATE, H6280_IRQT_STATE,
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///		-1,
/*TODO*///		H6280_M1, H6280_M2, H6280_M3, H6280_M4, -1,
/*TODO*///		H6280_M5, H6280_M6, H6280_M7, H6280_M8,
/*TODO*///	#endif
/*TODO*///		0
/*TODO*///	};
/*TODO*///	
/*TODO*///	/* Layout of the debugger windows x,y,w,h */
/*TODO*///	static UINT8 win_layout[] = {
/*TODO*///		25, 0,55, 4,	/* register window (top rows) */
/*TODO*///		 0, 0,24,22,	/* disassembler window (left colums) */
/*TODO*///		25, 5,55, 8,	/* memory #1 window (right, upper middle) */
/*TODO*///		25,14,55, 8,	/* memory #2 window (right, lower middle) */
/*TODO*///		 0,23,80, 1,	/* command line window (bottom rows) */
/*TODO*///	};
/*TODO*///	
	
        public static class PAIR {
            //L = low 8 bits
            //H = high 8 bits
            //D = whole 16 bits

            public int H, L, D;

            public void SetH(int val) {
                H = val & 0xFF;
                D = ((H << 8) | L) & 0xFFFF;
            }

            public void SetL(int val) {
                L = val & 0xFF;
                D = ((H << 8) | L) & 0xFFFF;
            }

            public void SetD(int val) {
                D = val & 0xFFFF;
                H = D >> 8 & 0xFF;
                L = D & 0xFF;
            }

            public void AddH(int val) {
                H = (H + val) & 0xFF;
                D = ((H << 8) | L)&0xFFFF;
            }

            public void AddL(int val) {
                L = (L + val) & 0xFF;
                D = ((H << 8) | L)&0xFFFF;
            }

            public void AddD(int val) {
                D = (D + val) & 0xFFFF;
                H = D >> 8 & 0xFF;
                L = D & 0xFF;
            }
        };
	
	/****************************************************************************
	 * The 6280 registers.
	 ****************************************************************************/
	public static class h6280_Regs
	{
            PAIR ppc = new PAIR();
            /* previous program counter */
            PAIR pc = new PAIR();
            /* program counter */
            PAIR sp = new PAIR();
            /* stack pointer (always 100 - 1FF) */
            PAIR zp = new PAIR();
            /* zero page address */
            PAIR ea = new PAIR();
            /* effective address */
            int u8_a;
            /* Accumulator */
            int u8_x;
            /* X index register */
            int u8_y;
            /* Y index register */
            int u8_p;
            /* Processor status */
            int[] u8_mmr = new int[8];
            /* Hu6280 memory mapper registers */
            int u8_irq_mask;
            /* interrupt enable/disable */
            int u8_timer_status;
            /* timer status */
            int u8_timer_ack;
            /* timer acknowledge */
            int timer_value;
            /* timer interrupt */
            int timer_load;
            /* reload value */
            int extra_cycles;
            /* cycles used taking an interrupt */
            int nmi_state;
            int[] irq_state = new int[3];
            public irqcallbacksPtr irq_callback;
	
/*TODO*///	#if LAZY_FLAGS
/*TODO*///	    int NZ;             /* last value (lazy N and Z flag) */
/*TODO*///	#endif
	
	};

	public static  h6280_Regs  h6280 = new h6280_Regs();

/*TODO*///	#ifdef  MAME_DEBUG /* Need some public segmentation registers for debugger */
/*TODO*///	UINT8	H6280_debug_mmr[8];
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	/* include the macros */
/*TODO*///	
/*TODO*///	/* include the opcode macros, functions and function pointer tables */
	
	/*****************************************************************************/
	public void h6280_init()
	{
	}
	
	public void h6280_reset(Object param)
	{
            int i;

            /* wipe out the h6280 structure */
            h6280 = new h6280_Regs();//memset(&h6280, 0, sizeof(h6280_Regs));

            /* set I and Z flags */
            h6280.u8_p = _fI | _fZ;

            /* stack starts at 0x01ff */
            h6280.sp.SetD(0x1ff);

            /* read the reset vector into PC */
            h6280.pc.SetL(RDMEM(H6280_RESET_VEC));
            h6280.pc.SetH(RDMEM((H6280_RESET_VEC + 1)));

            /* timer off by default */
            h6280.u8_timer_status = 0;
            h6280.u8_timer_ack = 1;

            /* clear pending interrupts */
            for (i = 0; i < 3; i++) {
                h6280.irq_state[i] = CLEAR_LINE;
            }
            if (h6280log != null) {
                fprintf(h6280log, "reset :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
	
/*TODO*///	void h6280_exit(void)
/*TODO*///	{
/*TODO*///		/* nothing */
/*TODO*///	}
/*TODO*///	
/*TODO*///	int h6280_execute(int cycles)
/*TODO*///	{
/*TODO*///		int in,lastcycle,deltacycle;
/*TODO*///		h6280_ICount = cycles;
/*TODO*///	
/*TODO*///	    /* Subtract cycles used for taking an interrupt */
/*TODO*///	    h6280_ICount -= h6280.extra_cycles;
/*TODO*///		h6280.extra_cycles = 0;
/*TODO*///		lastcycle = h6280_ICount;
/*TODO*///	
/*TODO*///		/* Execute instructions */
/*TODO*///		do
/*TODO*///	    {
/*TODO*///			h6280.ppc = h6280.pc;
/*TODO*///	
/*TODO*///	#ifdef  MAME_DEBUG
/*TODO*///		 	{
/*TODO*///				if (mame_debug != 0)
/*TODO*///				{
/*TODO*///					/* Copy the segmentation registers for debugger to use */
/*TODO*///					int i;
/*TODO*///					for (i=0; i<8; i++)
/*TODO*///						H6280_debug_mmr[i]=h6280.mmr[i];
/*TODO*///	
/*TODO*///					MAME_Debug();
/*TODO*///				}
/*TODO*///			}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///			/* Execute 1 instruction */
/*TODO*///			in=RDOP();
/*TODO*///			PCW++;
/*TODO*///			insnh6280[in]();
/*TODO*///	
/*TODO*///			/* Check internal timer */
/*TODO*///			if(h6280.timer_status)
/*TODO*///			{
/*TODO*///				deltacycle = lastcycle - h6280_ICount;
/*TODO*///				h6280.timer_value -= deltacycle;
/*TODO*///				if(h6280.timer_value<=0 && h6280.timer_ack==1)
/*TODO*///				{
/*TODO*///					h6280.timer_ack=h6280.timer_status=0;
/*TODO*///					h6280_set_irq_line(2,ASSERT_LINE);
/*TODO*///				}
/*TODO*///			}
/*TODO*///			lastcycle = h6280_ICount;
/*TODO*///	
/*TODO*///			/* If PC has not changed we are stuck in a tight loop, may as well finish */
/*TODO*///			if( h6280.pc.d == h6280.ppc.d )
/*TODO*///			{
/*TODO*///				if (h6280_ICount > 0) h6280_ICount=0;
/*TODO*///				h6280.extra_cycles = 0;
/*TODO*///				return cycles;
/*TODO*///			}
/*TODO*///	
/*TODO*///		} while (h6280_ICount > 0);
/*TODO*///	
/*TODO*///		/* Subtract cycles used for taking an interrupt */
/*TODO*///	    h6280_ICount -= h6280.extra_cycles;
/*TODO*///	    h6280.extra_cycles = 0;
/*TODO*///	
/*TODO*///	    return cycles - h6280_ICount;
/*TODO*///	}
	
	public Object h6280_get_context ()
	{
            h6280_Regs regs = new h6280_Regs();
            regs.ppc.SetD(h6280.ppc.D);
            regs.pc.SetD(h6280.pc.D);
            regs.sp.SetD(h6280.sp.D);
            regs.zp.SetD(h6280.zp.D);
            regs.ea.SetD(h6280.ea.D);
            regs.u8_a = h6280.u8_a;
            regs.u8_x = h6280.u8_x;
            regs.u8_y = h6280.u8_y;
            regs.u8_p = h6280.u8_p;
            regs.u8_mmr[0] = h6280.u8_mmr[0];
            regs.u8_mmr[1] = h6280.u8_mmr[1];
            regs.u8_mmr[2] = h6280.u8_mmr[2];
            regs.u8_mmr[3] = h6280.u8_mmr[3];
            regs.u8_mmr[4] = h6280.u8_mmr[4];
            regs.u8_mmr[5] = h6280.u8_mmr[5];
            regs.u8_mmr[6] = h6280.u8_mmr[6];
            regs.u8_mmr[7] = h6280.u8_mmr[7];
            regs.u8_irq_mask = h6280.u8_irq_mask;
            regs.u8_timer_status = h6280.u8_timer_status;
            regs.u8_timer_ack = h6280.u8_timer_ack;
            regs.timer_value = h6280.timer_value;
            regs.timer_load = h6280.timer_load;
            regs.extra_cycles = h6280.extra_cycles;
            regs.nmi_state = h6280.nmi_state;
            regs.irq_state[0] = h6280.irq_state[0];
            regs.irq_state[1] = h6280.irq_state[1];
            regs.irq_state[2] = h6280.irq_state[2];
            regs.irq_callback = h6280.irq_callback;
            return regs;
	}
	
	public void h6280_set_context (Object reg)
	{
            h6280_Regs regs = (h6280_Regs) reg;
            h6280.ppc.SetD(regs.ppc.D);
            h6280.pc.SetD(regs.pc.D);
            h6280.sp.SetD(regs.sp.D);
            h6280.zp.SetD(regs.zp.D);
            h6280.ea.SetD(regs.ea.D);
            h6280.u8_a = regs.u8_a;
            h6280.u8_x = regs.u8_x;
            h6280.u8_y = regs.u8_y;
            h6280.u8_p = regs.u8_p;
            h6280.u8_mmr[0] = regs.u8_mmr[0];
            h6280.u8_mmr[1] = regs.u8_mmr[1];
            h6280.u8_mmr[2] = regs.u8_mmr[2];
            h6280.u8_mmr[3] = regs.u8_mmr[3];
            h6280.u8_mmr[4] = regs.u8_mmr[4];
            h6280.u8_mmr[5] = regs.u8_mmr[5];
            h6280.u8_mmr[6] = regs.u8_mmr[6];
            h6280.u8_mmr[7] = regs.u8_mmr[7];
            h6280.u8_irq_mask = regs.u8_irq_mask;
            h6280.u8_timer_status = regs.u8_timer_status;
            h6280.u8_timer_ack = regs.u8_timer_ack;
            h6280.timer_value = regs.timer_value;
            h6280.timer_load = regs.timer_load;
            h6280.extra_cycles = regs.extra_cycles;
            h6280.nmi_state = regs.nmi_state;
            h6280.irq_state[0] = regs.irq_state[0];
            h6280.irq_state[1] = regs.irq_state[1];
            h6280.irq_state[2] = regs.irq_state[2];
            h6280.irq_callback = regs.irq_callback;
	}
	
/*TODO*///	unsigned h6280_get_pc (void)
/*TODO*///	{
/*TODO*///	    return PCD;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void h6280_set_pc (unsigned val)
/*TODO*///	{
/*TODO*///		PCW = val;
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned h6280_get_sp (void)
/*TODO*///	{
/*TODO*///		return S;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void h6280_set_sp (unsigned val)
/*TODO*///	{
/*TODO*///		S = val;
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned h6280_get_reg (int regnum)
/*TODO*///	{
/*TODO*///		switch( regnum )
/*TODO*///		{
/*TODO*///			case H6280_PC: return PCD;
/*TODO*///			case H6280_S: return S;
/*TODO*///			case H6280_P: return P;
/*TODO*///			case H6280_A: return A;
/*TODO*///			case H6280_X: return X;
/*TODO*///			case H6280_Y: return Y;
/*TODO*///			case H6280_IRQ_MASK: return h6280.irq_mask;
/*TODO*///			case H6280_TIMER_STATE: return h6280.timer_status;
/*TODO*///			case H6280_NMI_STATE: return h6280.nmi_state;
/*TODO*///			case H6280_IRQ1_STATE: return h6280.irq_state[0];
/*TODO*///			case H6280_IRQ2_STATE: return h6280.irq_state[1];
/*TODO*///			case H6280_IRQT_STATE: return h6280.irq_state[2];
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///			case H6280_M1: return h6280.mmr[0];
/*TODO*///			case H6280_M2: return h6280.mmr[1];
/*TODO*///			case H6280_M3: return h6280.mmr[2];
/*TODO*///			case H6280_M4: return h6280.mmr[3];
/*TODO*///			case H6280_M5: return h6280.mmr[4];
/*TODO*///			case H6280_M6: return h6280.mmr[5];
/*TODO*///			case H6280_M7: return h6280.mmr[6];
/*TODO*///			case H6280_M8: return h6280.mmr[7];
/*TODO*///	#endif
/*TODO*///			case REG_PREVIOUSPC: return h6280.ppc.d;
/*TODO*///			default:
/*TODO*///				if( regnum <= REG_SP_CONTENTS )
/*TODO*///				{
/*TODO*///					unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///					if( offset < 0x1ff )
/*TODO*///						return RDMEM( offset ) | ( RDMEM( offset+1 ) << 8 );
/*TODO*///				}
/*TODO*///		}
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void h6280_set_reg (int regnum, unsigned val)
/*TODO*///	{
/*TODO*///		switch( regnum )
/*TODO*///		{
/*TODO*///			case H6280_PC: PCW = val; break;
/*TODO*///			case H6280_S: S = val; break;
/*TODO*///			case H6280_P: P = val; break;
/*TODO*///			case H6280_A: A = val; break;
/*TODO*///			case H6280_X: X = val; break;
/*TODO*///			case H6280_Y: Y = val; break;
/*TODO*///			case H6280_IRQ_MASK: h6280.irq_mask = val; CHECK_IRQ_LINES; break;
/*TODO*///			case H6280_TIMER_STATE: h6280.timer_status = val; break;
/*TODO*///			case H6280_NMI_STATE: h6280_set_nmi_line( val ); break;
/*TODO*///			case H6280_IRQ1_STATE: h6280_set_irq_line( 0, val ); break;
/*TODO*///			case H6280_IRQ2_STATE: h6280_set_irq_line( 1, val ); break;
/*TODO*///			case H6280_IRQT_STATE: h6280_set_irq_line( 2, val ); break;
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///			case H6280_M1: h6280.mmr[0] = val; break;
/*TODO*///			case H6280_M2: h6280.mmr[1] = val; break;
/*TODO*///			case H6280_M3: h6280.mmr[2] = val; break;
/*TODO*///			case H6280_M4: h6280.mmr[3] = val; break;
/*TODO*///			case H6280_M5: h6280.mmr[4] = val; break;
/*TODO*///			case H6280_M6: h6280.mmr[5] = val; break;
/*TODO*///			case H6280_M7: h6280.mmr[6] = val; break;
/*TODO*///			case H6280_M8: h6280.mmr[7] = val; break;
/*TODO*///	#endif
/*TODO*///			default:
/*TODO*///				if( regnum <= REG_SP_CONTENTS )
/*TODO*///				{
/*TODO*///					unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///					if( offset < 0x1ff )
/*TODO*///					{
/*TODO*///						WRMEM( offset, val & 0xff );
/*TODO*///						WRMEM( offset+1, (val >> 8) & 0xff );
/*TODO*///					}
/*TODO*///				}
/*TODO*///	    }
/*TODO*///	}
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	
/*TODO*///	void h6280_set_nmi_line(int state)
/*TODO*///	{
/*TODO*///		if (h6280.nmi_state == state) return;
/*TODO*///		h6280.nmi_state = state;
/*TODO*///		if (state != CLEAR_LINE)
/*TODO*///	    {
/*TODO*///			DO_INTERRUPT(H6280_NMI_VEC);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	void h6280_set_irq_line(int irqline, int state)
/*TODO*///	{
/*TODO*///	    h6280.irq_state[irqline] = state;
/*TODO*///	
/*TODO*///		/* If line is cleared, just exit */
/*TODO*///		if (state == CLEAR_LINE) return;
/*TODO*///	
/*TODO*///		/* Check if interrupts are enabled and the IRQ mask is clear */
/*TODO*///		CHECK_IRQ_LINES;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void h6280_set_irq_callback(int (*callback)(int irqline))
/*TODO*///	{
/*TODO*///		h6280.irq_callback = callback;
/*TODO*///	}
	
	/****************************************************************************
	 * Return a formatted string for a register
	 ****************************************************************************/
	public String h6280_info(Object context, int regnum)
	{
/*TODO*///		static char buffer[32][47+1];
/*TODO*///		static int which = 0;
/*TODO*///		h6280_Regs *r = context;
/*TODO*///	
/*TODO*///		which = (which+1) % 32;
/*TODO*///		buffer[which][0] = '\0';
/*TODO*///		if( !context )
/*TODO*///			r = &h6280;
	
		switch( regnum )
		{
/*TODO*///			case CPU_INFO_REG+H6280_PC: sprintf(buffer[which], "PC:%04X", r.pc.w.l); break;
/*TODO*///	        case CPU_INFO_REG+H6280_S: sprintf(buffer[which], "S:%02X", r.sp.b.l); break;
/*TODO*///	        case CPU_INFO_REG+H6280_P: sprintf(buffer[which], "P:%02X", r.p); break;
/*TODO*///	        case CPU_INFO_REG+H6280_A: sprintf(buffer[which], "A:%02X", r.a); break;
/*TODO*///			case CPU_INFO_REG+H6280_X: sprintf(buffer[which], "X:%02X", r.x); break;
/*TODO*///			case CPU_INFO_REG+H6280_Y: sprintf(buffer[which], "Y:%02X", r.y); break;
/*TODO*///			case CPU_INFO_REG+H6280_IRQ_MASK: sprintf(buffer[which], "IM:%02X", r.irq_mask); break;
/*TODO*///			case CPU_INFO_REG+H6280_TIMER_STATE: sprintf(buffer[which], "TMR:%02X", r.timer_status); break;
/*TODO*///			case CPU_INFO_REG+H6280_NMI_STATE: sprintf(buffer[which], "NMI:%X", r.nmi_state); break;
/*TODO*///			case CPU_INFO_REG+H6280_IRQ1_STATE: sprintf(buffer[which], "IRQ1:%X", r.irq_state[0]); break;
/*TODO*///			case CPU_INFO_REG+H6280_IRQ2_STATE: sprintf(buffer[which], "IRQ2:%X", r.irq_state[1]); break;
/*TODO*///			case CPU_INFO_REG+H6280_IRQT_STATE: sprintf(buffer[which], "IRQT:%X", r.irq_state[2]); break;
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///			case CPU_INFO_REG+H6280_M1: sprintf(buffer[which], "M1:%02X", r.mmr[0]); break;
/*TODO*///			case CPU_INFO_REG+H6280_M2: sprintf(buffer[which], "M2:%02X", r.mmr[1]); break;
/*TODO*///			case CPU_INFO_REG+H6280_M3: sprintf(buffer[which], "M3:%02X", r.mmr[2]); break;
/*TODO*///			case CPU_INFO_REG+H6280_M4: sprintf(buffer[which], "M4:%02X", r.mmr[3]); break;
/*TODO*///			case CPU_INFO_REG+H6280_M5: sprintf(buffer[which], "M5:%02X", r.mmr[4]); break;
/*TODO*///			case CPU_INFO_REG+H6280_M6: sprintf(buffer[which], "M6:%02X", r.mmr[5]); break;
/*TODO*///			case CPU_INFO_REG+H6280_M7: sprintf(buffer[which], "M7:%02X", r.mmr[6]); break;
/*TODO*///			case CPU_INFO_REG+H6280_M8: sprintf(buffer[which], "M8:%02X", r.mmr[7]); break;
/*TODO*///	#endif
/*TODO*///			case CPU_INFO_FLAGS:
/*TODO*///				sprintf(buffer[which], "%c%c%c%c%c%c%c%c",
/*TODO*///					r.p & 0x80 ? 'N':'.',
/*TODO*///					r.p & 0x40 ? 'V':'.',
/*TODO*///					r.p & 0x20 ? 'R':'.',
/*TODO*///					r.p & 0x10 ? 'B':'.',
/*TODO*///					r.p & 0x08 ? 'D':'.',
/*TODO*///					r.p & 0x04 ? 'I':'.',
/*TODO*///					r.p & 0x02 ? 'Z':'.',
/*TODO*///					r.p & 0x01 ? 'C':'.');
/*TODO*///				break;
			case CPU_INFO_NAME: return "HuC6280";
			case CPU_INFO_FAMILY: return "Hudsonsoft 6280";
			case CPU_INFO_VERSION: return "1.07";
			case CPU_INFO_FILE: return "h6280.java";
			case CPU_INFO_CREDITS: return "Copyright (c) 1999, 2000 Bryan McPhail, mish@tendril.co.uk";
/*TODO*///			case CPU_INFO_REG_LAYOUT: return (const char*)reg_layout;
/*TODO*///			case CPU_INFO_WIN_LAYOUT: return (const char*)win_layout;
	    }
/*TODO*///		return buffer[which];
             throw new UnsupportedOperationException("Not supported yet.");
	}
	
/*TODO*///	unsigned h6280_dasm(char *buffer, unsigned pc)
/*TODO*///	{
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///	    return Dasm6280(buffer,pc);
/*TODO*///	#else
/*TODO*///		sprintf( buffer, "$%02X", cpu_readop(pc) );
/*TODO*///		return 1;
/*TODO*///	#endif
/*TODO*///	}
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr H6280_irq_status_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int status;
/*TODO*///	
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			case 0: /* Read irq mask */
/*TODO*///				return h6280.irq_mask;
/*TODO*///	
/*TODO*///			case 1: /* Read irq status */
/*TODO*///				status=0;
/*TODO*///				if(h6280.irq_state[1]!=CLEAR_LINE) status|=1; /* IRQ 2 */
/*TODO*///				if(h6280.irq_state[0]!=CLEAR_LINE) status|=2; /* IRQ 1 */
/*TODO*///				if(h6280.irq_state[2]!=CLEAR_LINE) status|=4; /* TIMER */
/*TODO*///				return status;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return 0;
/*TODO*///	} };
	
	public static WriteHandlerPtr H6280_irq_status_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (offset)
		{
			case 0: /* Write irq mask */
				h6280.u8_irq_mask=data&0x7;
				CHECK_IRQ_LINES();
				break;
	
			case 1: /* Timer irq ack - timer is reloaded here */
				h6280.timer_value = h6280.timer_load;
				h6280.u8_timer_ack=1; /* Timer can't refire until ack'd */
				break;
		}
	} };
	
/*TODO*///	public static ReadHandlerPtr H6280_timer_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		switch (offset) {
/*TODO*///			case 0: /* Counter value */
/*TODO*///				return (h6280.timer_value/1024)&127;
/*TODO*///	
/*TODO*///			case 1: /* Read counter status */
/*TODO*///				return h6280.timer_status;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return 0;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr H6280_timer_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		switch (offset) {
/*TODO*///			case 0: /* Counter preload */
/*TODO*///				h6280.timer_load=h6280.timer_value=((data&127)+1)*1024;
/*TODO*///				return;
/*TODO*///	
/*TODO*///			case 1: /* Counter enable */
/*TODO*///				if ((data & 1) != 0)
/*TODO*///				{	/* stop . start causes reload */
/*TODO*///					if(h6280.timer_status==0) h6280.timer_value=h6280.timer_load;
/*TODO*///				}
/*TODO*///				h6280.timer_status=data&1;
/*TODO*///				return;
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*****************************************************************************/
}
