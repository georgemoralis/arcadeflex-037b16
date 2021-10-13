/*** hd6309: Portable 6309 emulator ******************************************

	Copyright (C) John Butler 1997
	Copyright (C) Tim Lindner 2000

	References:

		HD63B09EP Technical Refrence Guide, by Chet Simpson with addition
							by Alan Dekok
		6809 Simulator V09, By L.C. Benschop, Eidnhoven The Netherlands.

		hd6309: Portable 6809 emulator, DS (6809 code in MAME, derived from
			the 6809 Simulator V09)

		6809 Microcomputer Programming & Interfacing with Experiments"
			by Andrew C. Staugaard, Jr.; Howard W. Sams & Co., Inc.

	System dependencies:	UINT16 must be 16 bit unsigned int
							UINT8 must be 8 bit unsigned int
							UINT32 must be more than 16 bits
							arrays up to 65536 bytes must be supported
							machine must be twos complement

	History:
991026 HJB:
	Fixed missing calls to cpu_changepc() for the TFR and EXG ocpodes.
	Replaced hd6309_slapstic checks by a macro (CHANGE_PC). ESB still
	needs the tweaks.

991024 HJB:
	Tried to improve speed: Using bit7 of cycles1/2 as flag for multi
	byte opcodes is gone, those opcodes now call fetch_effective_address().
	Got rid of the slow/fast flags for stack (S and U) memory accesses.
	Minor changes to use 32 bit values as arguments to memory functions
	and added defines for that purpose (e.g. X = 16bit XD = 32bit).

990312 HJB:
	Added bugfixes according to Aaron's findings.
	Reset only sets CC_II and CC_IF, DP to zero and PC from reset vector.
990311 HJB:
	Added _info functions. Now uses static m6808_Regs struct instead
	of single statics. Changed the 16 bit registers to use the generic
	PAIR union. Registers defined using macros. Split the core into
	four execution loops for M6802, M6803, M6808 and HD63701.
	TST, TSTA and TSTB opcodes reset carry flag.
	Modified the read/write stack handlers to push LSB first then MSB
	and pull MSB first then LSB.

990228 HJB:
	Changed the interrupt handling again. Now interrupts are taken
	either right at the moment the lines are asserted or whenever
	an interrupt is enabled and the corresponding line is still
	asserted. That way the pending_interrupts checks are not
	needed anymore. However, the CWAI and SYNC flags still need
	some flags, so I changed the name to 'int_state'.
	This core also has the code for the old interrupt system removed.

990225 HJB:
	Cleaned up the code here and there, added some comments.
	Slightly changed the SAR opcodes (similiar to other CPU cores).
	Added symbolic names for the flag bits.
	Changed the way CWAI/Interrupt() handle CPU state saving.
	A new flag HD6309_STATE in pending_interrupts is used to determine
	if a state save is needed on interrupt entry or already done by CWAI.
	Added HD6309_IRQ_LINE and HD6309_FIRQ_LINE defines to hd6309.h
	Moved the internal interrupt_pending flags from hd6309.h to hd6309.c
	Changed CWAI cycles2[0x3c] to be 2 (plus all or at least 19 if
	CWAI actually pushes the entire state).
	Implemented undocumented TFR/EXG for undefined source and mixed 8/16
	bit transfers (they should transfer/exchange the constant $ff).
	Removed unused jmp/jsr _slap functions from 6809ops.c,
	hd6309_slapstick check moved into the opcode functions.

000809 TJL:
	Started converting hd6309 into hd6309

001217 TJL:
	Finished:
		All opcodes
		Dual Timing
	To Do:
		Verify new DIV opcodes.

*****************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.cpu.hd6309;

import static common.libc.cstdio.fclose;
import static gr.codebb.arcadeflex.WIP.v037b16.cpu.hd6309.hd6309H.*;
import static gr.codebb.arcadeflex.WIP.v037b16.cpu.hd6309.hd6309ops.hd6309log;
import static gr.codebb.arcadeflex.WIP.v037b16.cpu.hd6309.hd6309tbl.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b16.mame.memory.*;
import static gr.codebb.arcadeflex.v037b16.mame.memoryH.*;

public class hd6309 extends cpu_interface {
    
    /* public globals */
    public static int[] hd6309_ICount = new int[1];
    
    public hd6309() {
        cpu_num = CPU_HD6309;
        num_irqs = 2;
        default_vector = 0;
        overclock = 1.0;
        no_int = HD6309_INT_NONE;
        irq_int = HD6309_INT_IRQ;
        nmi_int = HD6309_INT_NMI;
        databus_width = 8;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_BE;
        align_unit = 1;
        max_inst_len = 4;
        pgm_memory_base = 0;
        icount = hd6309_ICount;
        hd6309_ICount[0] = 50000;
    }

    @Override
    public void init() {
        hd6309_init();
    }

    @Override
    public void reset(Object param) {
        hd6309_reset(param);
    }

    @Override
    public void exit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int execute(int cycles) {
        return hd6309_execute(cycles);
    }

    @Override
    public Object init_context() {
        Object reg = new hd6309_Regs();
        return reg;
    }

    @Override
    public Object get_context() {
        return hd6309_get_context();
    }

    @Override
    public void set_context(Object reg) {
        hd6309_set_context(reg);
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
        return hd6309_get_pc();
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
        hd6309_set_nmi_line(linestate);
    }

    @Override
    public void set_irq_line(int irqline, int linestate) {
        hd6309_set_irq_line(irqline, linestate);
    }

    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
        hd6309.irq_callback = callback;
    }

    @Override
    public void internal_interrupt(int type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String cpu_info(Object context, int regnum) {
        return hd6309_info(context, regnum);
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
        cpu_setOPbase16.handler(pc);
    }

    @Override
    public int mem_address_bits_of_cpu() {
        return 16;
    }
    
    static void setDreg(int reg) //write to dreg
    {
        hd6309.a = reg >> 8 & 0xFF;
        hd6309.b = reg & 0xFF;
    }
    
    static int getDreg()//compose dreg
    {
        return (hd6309.a << 8 | hd6309.b) & 0xFFFF;
    }
    
    static void setWreg(int reg) //write to dreg
    {
        hd6309.e = reg >> 8 & 0xFF;
        hd6309.f = reg & 0xFF;
    }
    
    static int getWreg()//compose dreg
    {
        return (hd6309.e << 8 | hd6309.f) & 0xFFFF;
    }

/*TODO*///	
/*TODO*///	#define VERBOSE 0
/*TODO*///	
/*TODO*///	#if VERBOSE
/*TODO*///	#define LOG(x)	logerror x
/*TODO*///	#else
/*TODO*///	#define LOG(x)
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifndef true
/*TODO*///	#define true 1
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifndef false
/*TODO*///	#define false 0
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	static static 
/*TODO*///	static UINT8 hd6309_reg_layout[] = {
/*TODO*///		HD6309_A, HD6309_B, HD6309_E, HD6309_F, HD6309_MD, HD6309_CC, HD6309_DP,  -1,
/*TODO*///		HD6309_X, HD6309_Y, HD6309_S, HD6309_U, HD6309_V, -1,
/*TODO*///		HD6309_PC, HD6309_NMI_STATE, HD6309_IRQ_STATE, HD6309_FIRQ_STATE, 0
/*TODO*///	};
/*TODO*///	
/*TODO*///	/* Layout of the debugger windows x,y,w,h */
/*TODO*///	static UINT8 hd6309_win_layout[] = {
/*TODO*///		27, 0,53, 4,	/* register window (top, right rows) */
/*TODO*///		 0, 0,26,22,	/* disassembler window (left colums) */
/*TODO*///		27, 5,53, 8,	/* memory #1 window (right, upper middle) */
/*TODO*///		27,14,53, 8,	/* memory #2 window (right, lower middle) */
/*TODO*///		 0,23,80, 1,	/* command line window (bottom rows) */
/*TODO*///	};


	/* 6309 Registers */
        public static class hd6309_Regs {

            public /*PAIR*/ int pc;/* Program counter */
            public /*PAIR*/ int ppc;/* Previous program counter */
            public int a;
            public int b;//PAIR	d;/* Accumlator d and w (ab = d, ef = w, abef = q) */
            public int e;
            public int f;//PAIR	w;
            public /*PAIR*/ int dp;/* Direct Page register (page in MSB) */
            public int u;
            public int s;//PAIR	u, s;/* Stack pointers */
            public int x;
            public int y;//PAIR	x, y;/* Index registers */
            public /*PAIR*/ int v;/* New 6309 register */
            public int /*UINT8*/ cc;
            public int /*UINT8*/ md; /* Special mode register */
            public int /*UINT8*/ ireg;/* First opcode */
            public int[] /*UINT8*/ irq_state = new int[2];
            public int extra_cycles;/* cycles used up by interrupts */
            public irqcallbacksPtr irq_callback;
            public int /*UINT8*/ int_state;/* SYNC and CWAI flags */
            public int /*UINT8*/ nmi_state;
        }

        /* flag bits in the cc register */
        public static final int CC_C    = 0x01;    /* Carry */
        public static final int CC_V    = 0x02;    /* Overflow */
        public static final int CC_Z    = 0x04;    /* Zero */
        public static final int CC_N    = 0x08;    /* Negative */
        public static final int CC_II   = 0x10;    /* Inhibit IRQ */
        public static final int CC_H    = 0x20;    /* Half (auxiliary) carry */
        public static final int CC_IF   = 0x40;    /* Inhibit FIRQ */
        public static final int CC_E    = 0x80;    /* entire state pushed */

	/* flag bits in the md register */
	public static final int MD_EM	= 0x01;     /* Execution mode */
	public static final int MD_FM	= 0x02;     /* FIRQ mode */
	public static final int MD_II	= 0x40;     /* Illegal instruction */
	public static final int MD_DZ	= 0x80;     /* Division by zero */

	/* 6309 registers */
	public static hd6309_Regs hd6309 = new hd6309_Regs();
/*TODO*///	int hd6309_slapstic = 0;

/*TODO*///	#define pPPC	hd6309.ppc
/*TODO*///	#define pPC 	hd6309.pc
/*TODO*///	#define pU		hd6309.u
/*TODO*///	#define pS		hd6309.s
/*TODO*///	#define pX		hd6309.x
/*TODO*///	#define pY		hd6309.y
/*TODO*///	#define pV		hd6309.v
/*TODO*///	/*#define pQ		hd6309.q*/
/*TODO*///	#define pD		hd6309.d
/*TODO*///	#define pW		hd6309.w
/*TODO*///	#define pZ		hd6309.z
/*TODO*///	
/*TODO*///	#define PPC 	hd6309.ppc.w.l
/*TODO*///	#define PC		hd6309.pc.w.l
/*TODO*///	#define PCD 	hd6309.pc.d
/*TODO*///	#define U		hd6309.u.w.l
/*TODO*///	#define UD		hd6309.u.d
/*TODO*///	#define S		hd6309.s.w.l
/*TODO*///	#define SD		hd6309.s.d
/*TODO*///	#define X		hd6309.x.w.l
/*TODO*///	#define XD		hd6309.x.d
/*TODO*///	#define Y		hd6309.y.w.l
/*TODO*///	#define YD		hd6309.y.d
/*TODO*///	#define V		hd6309.v.w.l
/*TODO*///	#define VD		hd6309.v.d
/*TODO*///	#define D		hd6309.d.w.l
/*TODO*///	#define A		hd6309.d.b.h
/*TODO*///	#define B		hd6309.d.b.l
/*TODO*///	#define W		hd6309.w.w.l
/*TODO*///	#define E		hd6309.w.b.h
/*TODO*///	#define F		hd6309.w.b.l
/*TODO*///	#define DP		hd6309.dp.b.h
/*TODO*///	#define DPD 	hd6309.dp.d
/*TODO*///	#define CC		hd6309.cc
/*TODO*///	#define MD		hd6309.md

/*TODO*///	static PAIR ea; 		/* effective address */
/*TODO*///	#define EA	ea.w.l
/*TODO*///	#define EAD ea.d
        public static int ea;

        public static void CHANGE_PC() {
            change_pc16(hd6309.pc & 0xFFFF);//ensure it's 16bit just in case
        }
/*TODO*///	#if 0
/*TODO*///	#define CHANGE_PC	{			\
/*TODO*///		if (hd6309_slapstic != 0)		\
/*TODO*///			cpu_setOPbase16(PCD);	\
/*TODO*///		else						\
/*TODO*///			change_pc16(PCD);		\
/*TODO*///		}
/*TODO*///	#endif
/*TODO*///	
        public static final int HD6309_CWAI 	= 8;	/* set when CWAI is waiting for an interrupt */
	public static final int HD6309_SYNC 	= 16;	/* set when SYNC is waiting for an interrupt */
        public static final int HD6309_LDS	= 32;	/* set when LDS occured at least once */

	/* these are re-defined in hd6309.h TO RAM, ROM or functions in cpuintrf.c */
        //#define RM(mAddr)		HD6309_RDMEM(mAddr)
        public static int RM(int addr) {
            return (cpu_readmem16(addr) & 0xFF);
        }
        
        //#define WM(mAddr,Value) HD6309_WRMEM(mAddr,Value)
        public static void WM(int addr, int value) {
            cpu_writemem16(addr, value & 0xFF);
        }
        
        //#define ROP(mAddr)		HD6309_RDOP(mAddr)
        public static char ROP(int addr) {
            return cpu_readop(addr);
        }
        
        //#define ROP_ARG(mAddr)	HD6309_RDOP_ARG(mAddr)
        public static char ROP_ARG(int addr) {
            return cpu_readop_arg(addr);
        }
	
	/* macros to access memory */
        //#define IMMBYTE(b)	b = ROP_ARG(PCD); PC++
        public static int IMMBYTE() {
            int reg = ROP_ARG(hd6309.pc);
            hd6309.pc = (hd6309.pc + 1) & 0xFFFF;
            return reg & 0xFF;
        }
        
        //#define IMMWORD(w)	w.d = (ROP_ARG(PCD)<<8) | ROP_ARG((PCD+1)&0xffff); PC+=2
        public static int IMMWORD() {
            int reg = ((ROP_ARG(hd6309.pc) << 8) | ROP_ARG((hd6309.pc + 1)) & 0xffff);
            hd6309.pc = (hd6309.pc) + 2 & 0xFFFF;
            return reg;
        }
        
/*TODO*///	#define IMMLONG(w)	w.d = (ROP_ARG(PCD)<<24) + (ROP_ARG(PCD+1)<<16) + (ROP_ARG(PCD+2)<<8) + (ROP_ARG(PCD+3)); PC+=4

        //#define PUSHBYTE(b) --S; WM(SD,b)
        public static void PUSHBYTE(int w) {
            hd6309.s = hd6309.s - 1 & 0xFFFF;
            WM(hd6309.s, w);
        }
        
        //#define PUSHWORD(w) --S; WM(SD,w.b.l); --S; WM(SD,w.b.h)
        public static void PUSHWORD(int w) {
            hd6309.s = hd6309.s - 1 & 0xFFFF;
            WM(hd6309.s, w & 0xFF);
            hd6309.s = hd6309.s - 1 & 0xFFFF;
            WM(hd6309.s, w >> 8);
        }

        //#define PULLBYTE(b) b = RM(SD); S++
        public static int PULLBYTE() {
            int b = RM(hd6309.s);
            hd6309.s = (hd6309.s + 1) & 0xFFFF;
            return b & 0xFF;
        }

        //#define PULLWORD(w) w = RM(SD)<<8; S++; w |= RM(SD); S++
        public static int PULLWORD() {
            int w = RM(hd6309.s) << 8;
            hd6309.s = hd6309.s + 1 & 0xFFFF;
            w |= RM(hd6309.s);
            hd6309.s = hd6309.s + 1 & 0xFFFF;
            return w & 0xFFFF;
        }

        //#define PSHUBYTE(b) --U; WM(UD,b);
        public static void PSHUBYTE(int w) {
            hd6309.u = (hd6309.u - 1) & 0xFFFF;
            WM(hd6309.u, w);
        }
        //#define PSHUWORD(w) --U; WM(UD,w.b.l); --U; WM(UD,w.b.h)
        public static void PSHUWORD(int w) {
            hd6309.u = (hd6309.u - 1) & 0xFFFF;
            WM(hd6309.u, w & 0xFF);
            hd6309.u = (hd6309.u - 1) & 0xFFFF;
            WM(hd6309.u, w >>> 8);
        }
        
	//#define PULUBYTE(b) b = RM(UD); U++
        public static int PULUBYTE() {
            int b = RM(hd6309.u);
            hd6309.u = (hd6309.u + 1) & 0xFFFF;
            return b;
        }
	
        //#define PULUWORD(w) w = RM(UD)<<8; U++; w |= RM(UD); U++
        public static int PULUWORD()//TODO recheck
        {
            int w = RM(hd6309.u) << 8;
            hd6309.u = (hd6309.u + 1) & 0xFFFF;
            w |= RM(hd6309.u);
            hd6309.u = (hd6309.u + 1) & 0xFFFF;
            return w;
        }

        //#define CLR_HNZVC	CC&=~(CC_H|CC_N|CC_Z|CC_V|CC_C)
        public static void CLR_HNZVC() {
            hd6309.cc &= ~(CC_H | CC_N | CC_Z | CC_V | CC_C);
        }
        //#define CLR_NZV 	CC&=~(CC_N|CC_Z|CC_V)
        public static void CLR_NZV() {
            hd6309.cc &= ~(CC_N | CC_Z | CC_V);
        }
/*TODO*///	#define CLR_HNZC	CC&=~(CC_H|CC_N|CC_Z|CC_C)
        //#define CLR_NZVC	CC&=~(CC_N|CC_Z|CC_V|CC_C)
        public static void CLR_NZVC() {
            hd6309.cc &= ~(CC_N | CC_Z | CC_V | CC_C);
        }

        //#define CLR_Z		CC&=~(CC_Z)
        public static void CLR_Z() {
            hd6309.cc &= ~(CC_Z);
        }
/*TODO*///	#define CLR_N		CC&=~(CC_N)

        //#define CLR_NZC 	CC&=~(CC_N|CC_Z|CC_C)
        public static void CLR_NZC() {
            hd6309.cc &= ~(CC_N | CC_Z | CC_C);
        }
        
        //#define CLR_ZC		CC&=~(CC_Z|CC_C)
        public static void CLR_ZC() {
            hd6309.cc &= ~(CC_Z | CC_C);
        }
	
	/* macros for CC -- CC bits affected should be reset before calling */
        //#define SET_Z(a)		if(!a)SEZ
        public static void SET_Z(int a) {
            if (a == 0) {
                SEZ();
            }
        }
        
        //#define SET_Z8(a)		SET_Z((UINT8)a)
        public static void SET_Z8(int a) {
            SET_Z(a & 0xFF);
        }

        //#define SET_Z16(a)		SET_Z((UINT16)a)
        public static void SET_Z16(int a) {
            SET_Z(a & 0xFFFF);
        }
        
        //#define SET_N8(a)		CC|=((a&0x80)>>4)
        public static void SET_N8(int a) {
            hd6309.cc |= ((a & 0x80) >> 4);
        }

        //#define SET_N16(a)		CC|=((a&0x8000)>>12)
        public static void SET_N16(int a) {
            hd6309.cc |= ((a & 0x8000) >> 12);
        }
/*TODO*///	#define SET_N32(a)		CC|=((a&0x8000)>>20)
        //#define SET_H(a,b,r)	CC|=(((a^b^r)&0x10)<<1)
        public static void SET_H(int a, int b, int r) {
            hd6309.cc |= (((a ^ b ^ r) & 0x10) << 1);
        }
        //#define SET_C8(a)		CC|=((a&0x100)>>8)
        public static void SET_C8(int a) {
            hd6309.cc |= ((a & 0x100) >> 8);
        }

        //#define SET_C16(a)		CC|=((a&0x10000)>>16)
        public static void SET_C16(int a) {
            hd6309.cc |= ((a & 0x10000) >> 16);
        }
        
        //#define SET_V8(a,b,r)	CC|=(((a^b^r^(r>>1))&0x80)>>6)
        public static void SET_V8(int a, int b, int r) {
            hd6309.cc |= (((a ^ b ^ r ^ (r >> 1)) & 0x80) >> 6);
        }

        //#define SET_V16(a,b,r)	CC|=(((a^b^r^(r>>1))&0x8000)>>14)
        public static void SET_V16(int a, int b, int r) {
            hd6309.cc |= (((a ^ b ^ r ^ (r >> 1)) & 0x8000) >> 14);
        }

        //#define SET_FLAGS8I(a)		{CC|=flags8i[(a)&0xff];}
        public static void SET_FLAGS8I(int a) {
            hd6309.cc |= flags8i[(a) & 0xff];
        }

        //#define SET_FLAGS8D(a)		{CC|=flags8d[(a)&0xff];}
        public static void SET_FLAGS8D(int a) {
            hd6309.cc |= flags8d[(a) & 0xff];
        }

	static int[] cycle_counts_page0;
        static int[] cycle_counts_page01;
        static int[] cycle_counts_page11;
        static int[] index_cycle;

	/* combos */
        //#define SET_NZ8(a)			{SET_N8(a);SET_Z(a);}
        public static void SET_NZ8(int a) {
            SET_N8(a);
            SET_Z(a);
        }
        
        //#define SET_NZ16(a) 		{SET_N16(a);SET_Z(a);}
        public static void SET_NZ16(int a) {
            SET_N16(a);
            SET_Z(a);
        }
/*TODO*///	#define SET_FLAGS8(a,b,r)	{SET_N8(r);SET_Z8(r);SET_V8(a,b,r);SET_C8(r);}
        public static void SET_FLAGS8(int a, int b, int r) {
            SET_N8(r);
            SET_Z8(r);
            SET_V8(a, b, r);
            SET_C8(r);
        }

        //#define SET_FLAGS16(a,b,r)	{SET_N16(r);SET_Z16(r);SET_V16(a,b,r);SET_C16(r);}
        public static void SET_FLAGS16(int a, int b, int r) {
            SET_N16(r);
            SET_Z16(r);
            SET_V16(a, b, r);
            SET_C16(r);
        }
	
        //#define NXORV				((CC&CC_N)^((CC&CC_V)<<2))
        public static int NXORV() {
            return ((hd6309.cc & CC_N) ^ ((hd6309.cc & CC_V) << 2));
        }

	/* for treating an unsigned byte as a signed word */
        //#define SIGNED(b) ((UINT16)(b&0x80?b|0xff00:b))
        public static int SIGNED(int b) {
            return (((b & 0x80) != 0 ? b | 0xff00 : b)) & 0xFFFF;
        }
/*TODO*///	/* for treating an unsigned short as a signed long */
/*TODO*///	#define SIGNED_16(b) ((UINT32)(b&0x8000?b|0xffff0000:b))
/*TODO*///	
/*TODO*///	/* macros for addressing modes (postbytes have their own code) */
/*TODO*///	#define DIRECT	EAD = DPD; IMMBYTE(ea.b.l)
        public static void DIRECT() {
            ea = IMMBYTE();
            ea |= hd6309.dp << 8;
        }
/*TODO*///	#define IMM8	EAD = PCD; PC++
/*TODO*///	#define IMM16	EAD = PCD; PC+=2
        //#define EXTENDED IMMWORD(ea)
        public static void EXTENDED() {
            ea = IMMWORD();
        }
	
	/* macros to set status flags */
        //#define SEC CC|=CC_C
        public static void SEC() {
            hd6309.cc |= CC_C;
        }
/*TODO*///	#define CLC CC&=~CC_C
        //#define SEZ CC|=CC_Z
        public static void SEZ() {
            hd6309.cc |= CC_Z;
        }
/*TODO*///	#define CLZ CC&=~CC_Z
/*TODO*///	#define SEN CC|=CC_N
/*TODO*///	#define CLN CC&=~CC_N
/*TODO*///	#define SEV CC|=CC_V
/*TODO*///	#define CLV CC&=~CC_V
/*TODO*///	#define SEH CC|=CC_H
/*TODO*///	#define CLH CC&=~CC_H
/*TODO*///	
/*TODO*///	/* Macros to set mode flags */
/*TODO*///	#define SEDZ MD|=MD_DZ
/*TODO*///	#define CLDZ MD&=~MD_DZ
/*TODO*///	#define SEII MD|=MD_II
/*TODO*///	#define CLII MD&=~MD_II
/*TODO*///	#define SEFM MD|=MD_FM
/*TODO*///	#define CLFM MD&=~MD_FM
/*TODO*///	#define SEEM MD|=MD_EM
/*TODO*///	#define CLEM MD&=~MD_EM
/*TODO*///	
/*TODO*///	/* macros for convenience */
        //#define DIRBYTE(b) {DIRECT;b=RM(EAD);}
        public static int DIRBYTE() {
            DIRECT();
            return RM(ea) & 0xFF;
        }

        //#define DIRWORD(w) {DIRECT;w.d=RM16(EAD);}
        public static int DIRWORD() {
            DIRECT();
            return RM16(ea) & 0xFFFF;
        }
/*TODO*///	#define DIRLONG(lng) {DIRECT;lng.w.h=RM16(EAD);lng.w.l=RM16(EAD+2);}

        //#define EXTBYTE(b) {EXTENDED;b=RM(EAD);}
        public static int EXTBYTE() {
            EXTENDED();
            return RM(ea) & 0xFF;
        }
        
        //#define EXTWORD(w) {EXTENDED;w.d=RM16(EAD);}
        public static int EXTWORD() {
            EXTENDED();
            return RM16(ea) & 0xFFFF;
        }
        
/*TODO*///	#define EXTLONG(lng) {EXTENDED;lng.w.h=RM16(EAD);lng.w.l=RM16(EAD+2);}
/*TODO*///	
/*TODO*///	/* includes the static function prototypes and other tables */
	
	/* macros for branch instructions */
	public static void BRANCH(boolean f) {
            int t = IMMBYTE();
            if (f) {
                hd6309.pc = (hd6309.pc + SIGNED(t)) & 0xFFFF;
                CHANGE_PC();
            }
        }
	
/*TODO*///	#define LBRANCH(f) {					\
/*TODO*///		PAIR t; 							\
/*TODO*///		IMMWORD(t); 						\
/*TODO*///		if (f != 0) 							\
/*TODO*///		{									\
/*TODO*///			hd6309_ICount -= 1; 			\
/*TODO*///			PC += t.w.l; *CHECK IT!
/*TODO*///			CHANGE_PC;						\
/*TODO*///		}									\
/*TODO*///	}
        public static void LBRANCH(boolean f) {
            int t = IMMWORD();
            if (f) {
                hd6309_ICount[0] -= 1;
                hd6309.pc = (hd6309.pc + t) & 0xFFFF;
                CHANGE_PC();
            }
        }

        public static int RM16(int addr) {
            int i = RM(addr + 1 & 0xFFFF);
            i |= RM(addr) << 8;
            return i & 0xFFFF;
        }
/*TODO*///	
/*TODO*///	INLINE UINT32 RM32( UINT32 mAddr );
/*TODO*///	INLINE UINT32 RM32( UINT32 mAddr )
/*TODO*///	{
/*TODO*///		UINT32 result = RM(mAddr) << 24;
/*TODO*///		result += RM(mAddr+1) << 16;
/*TODO*///		result += RM(mAddr+2) << 8;
/*TODO*///		result += RM(mAddr+3);
/*TODO*///		return result;
/*TODO*///	}
/*TODO*///	
/*TODO*///	INLINE void WM16( UINT32 mAddr, PAIR *p )
/*TODO*///	{
/*TODO*///		WM( mAddr, p.b.h );
/*TODO*///		WM( (mAddr+1)&0xffff, p.b.l );
/*TODO*///	}
        public static void WM16(int addr, int reg) {
            WM(addr + 1 & 0xFFFF, reg & 0xFF);
            WM(addr, reg >> 8);
        }

/*TODO*///	INLINE void WM32( UINT32 mAddr, PAIR *p );
/*TODO*///	INLINE void WM32( UINT32 mAddr, PAIR *p )
/*TODO*///	{
/*TODO*///		WM( mAddr, p.b.h3 );
/*TODO*///		WM( (mAddr+1)&0xffff, p.b.h2 );
/*TODO*///		WM( (mAddr+2)&0xffff, p.b.h );
/*TODO*///		WM( (mAddr+3)&0xffff, p.b.l );
/*TODO*///	}
	
	public static void UpdateState()
	{
		if (( hd6309.md & MD_EM ) != 0)
		{
			cycle_counts_page0  = ccounts_page0_na;
			cycle_counts_page01 = ccounts_page01_na;
			cycle_counts_page11 = ccounts_page11_na;
			index_cycle         = index_cycle_na;
		}
		else
		{
			cycle_counts_page0  = ccounts_page0_em;
			cycle_counts_page01 = ccounts_page01_em;
			cycle_counts_page11 = ccounts_page11_em;
			index_cycle         = index_cycle_em;
		}
	}
        
        public static void CHECK_IRQ_LINES() {
        if (hd6309.irq_state[HD6309_IRQ_LINE] != CLEAR_LINE || hd6309.irq_state[HD6309_FIRQ_LINE] != CLEAR_LINE) {
            hd6309.int_state &= ~HD6309_SYNC;/* clear SYNC flag */
        }
        if (hd6309.irq_state[HD6309_FIRQ_LINE] != CLEAR_LINE && ((hd6309.cc & CC_IF) == 0)) {
            /* fast IRQ */
 /* HJB 990225: state already saved by CWAI? */
            if ((hd6309.int_state & HD6309_CWAI) != 0) {
                hd6309.int_state &= ~HD6309_CWAI;/* clear CWAI */
                hd6309.extra_cycles += 7;/* subtract +7 cycles */
            } else {
                hd6309.cc &= ~CC_E;/* save 'short' state */
                PUSHWORD(hd6309.pc);
                PUSHBYTE(hd6309.cc);
                hd6309.extra_cycles += 10;/* subtract +10 cycles */
            }
            hd6309.cc |= CC_IF | CC_II;/* inhibit FIRQ and IRQ */
            hd6309.pc = RM16(0xfff6);
            CHANGE_PC();
            hd6309.irq_callback.handler(HD6309_FIRQ_LINE);
        } else if (hd6309.irq_state[HD6309_IRQ_LINE] != CLEAR_LINE && ((hd6309.cc & CC_II) == 0)) {
            /* standard IRQ */
 /* HJB 990225: state already saved by CWAI? */
            if ((hd6309.int_state & HD6309_CWAI) != 0) {
                hd6309.int_state &= ~HD6309_CWAI;/* clear CWAI flag */
                hd6309.extra_cycles += 7;/* subtract +7 cycles */
            } else {
                hd6309.cc |= CC_E;/* save entire state */
                PUSHWORD(hd6309.pc);
                PUSHWORD(hd6309.u);
                PUSHWORD(hd6309.y);
                PUSHWORD(hd6309.x);
                PUSHBYTE(hd6309.dp);
                PUSHBYTE(hd6309.b);
                PUSHBYTE(hd6309.a);
                PUSHBYTE(hd6309.cc);
                hd6309.extra_cycles += 19;/* subtract +19 cycles */
            }
            hd6309.cc |= CC_II;/* inhibit IRQ */
            hd6309.pc = RM16(0xfff8);
            CHANGE_PC();
            hd6309.irq_callback.handler(HD6309_IRQ_LINE);
        }
    }

	
	public static void CHECK_IRQ_LINES2()
	{
		if( hd6309.irq_state[HD6309_IRQ_LINE] != CLEAR_LINE ||
			hd6309.irq_state[HD6309_FIRQ_LINE] != CLEAR_LINE )
			hd6309.int_state &= ~HD6309_SYNC; /* clear SYNC flag */
		if( hd6309.irq_state[HD6309_FIRQ_LINE]!=CLEAR_LINE && (hd6309.cc & CC_IF)==0)
		{
			/* fast IRQ */
			/* HJB 990225: state already saved by CWAI? */
			if(( hd6309.int_state & HD6309_CWAI ) != 0)
			{
				hd6309.int_state &= ~HD6309_CWAI;
				hd6309.extra_cycles += 7;		 /* subtract +7 cycles */
			}
			else
			{
				if ((hd6309.md & MD_FM) != 0)
				{
					hd6309.cc |= CC_E; 				/* save entire state */
					PUSHWORD(hd6309.ppc);
					PUSHWORD(hd6309.u);
					PUSHWORD(hd6309.y);
					PUSHWORD(hd6309.x);
					PUSHBYTE(hd6309.dp);
					if ((hd6309.md & MD_EM) != 0)
					{
						PUSHBYTE(hd6309.f);
						PUSHBYTE(hd6309.e);
						hd6309.extra_cycles += 2; /* subtract +2 cycles */
					}
					PUSHBYTE(hd6309.b);
					PUSHBYTE(hd6309.a);
					PUSHBYTE(hd6309.cc);
					hd6309.extra_cycles += 19;	 /* subtract +19 cycles */
				}
				else
				{
					hd6309.cc &= ~CC_E;				/* save 'short' state */
					PUSHWORD(hd6309.ppc);
					PUSHBYTE(hd6309.cc);
					hd6309.extra_cycles += 10;	/* subtract +10 cycles */
				}
			}
			hd6309.cc |= CC_IF | CC_II;			/* inhibit FIRQ and IRQ */
			hd6309.pc=RM16(0xfff6);
			CHANGE_PC();
			(hd6309.irq_callback).handler(HD6309_FIRQ_LINE);
		}
		else
		if( hd6309.irq_state[HD6309_IRQ_LINE]!=CLEAR_LINE && (hd6309.cc & CC_II)==0 )
		{
			/* standard IRQ */
			/* HJB 990225: state already saved by CWAI? */
			if(( hd6309.int_state & HD6309_CWAI ) != 0)
			{
				hd6309.int_state &= ~HD6309_CWAI;  /* clear CWAI flag */
				hd6309.extra_cycles += 7;		 /* subtract +7 cycles */
			}
			else
			{
				hd6309.cc |= CC_E; 				/* save entire state */
				PUSHWORD(hd6309.ppc);
				PUSHWORD(hd6309.u);
				PUSHWORD(hd6309.y);
				PUSHWORD(hd6309.x);
				PUSHBYTE(hd6309.dp);
				if ((hd6309.md & MD_EM) != 0)
				{
					PUSHBYTE(hd6309.f);
					PUSHBYTE(hd6309.e);
					hd6309.extra_cycles += 2; /* subtract +2 cycles */
				}
				PUSHBYTE(hd6309.b);
				PUSHBYTE(hd6309.a);
				PUSHBYTE(hd6309.cc);
				hd6309.extra_cycles += 19;	 /* subtract +19 cycles */
			}
			hd6309.cc |= CC_II;					/* inhibit IRQ */
			hd6309.pc=RM16(0xfff8);
			CHANGE_PC();
			(hd6309.irq_callback).handler(HD6309_IRQ_LINE);
		}
	}
	
	/****************************************************************************
	 * Get all registers in given buffer
	 ****************************************************************************/
	public static Object hd6309_get_context()
	{
/*TODO*///		if (dst != 0)
/*TODO*///			*(hd6309_Regs*)dst = hd6309;
/*TODO*///		return sizeof(hd6309_Regs);
            hd6309_Regs regs = new hd6309_Regs();
            regs.pc = hd6309.pc;
            regs.ppc = hd6309.ppc;
            regs.a = hd6309.a;
            regs.b = hd6309.b;
            regs.dp = hd6309.dp;
            regs.u = hd6309.u;
            regs.s = hd6309.s;
            regs.x = hd6309.x;
            regs.y = hd6309.y;
            regs.cc = hd6309.cc;
            regs.ireg = hd6309.ireg;
            regs.irq_state[0] = hd6309.irq_state[0];
            regs.irq_state[1] = hd6309.irq_state[1];
            regs.extra_cycles = hd6309.extra_cycles;
            regs.irq_callback = hd6309.irq_callback;
            regs.int_state = hd6309.int_state;
            regs.nmi_state = hd6309.nmi_state;
            regs.e = hd6309.e;
            regs.f = hd6309.f;
            regs.v = hd6309.v;
            regs.md = hd6309.md;
            
            return regs;
        
	}
	
	/****************************************************************************
	 * Set all registers to given values
	 ****************************************************************************/
	public static void hd6309_set_context(Object src)
	{
/*TODO*///		if (src != 0)
/*TODO*///			hd6309 = *(hd6309_Regs*)src;
/*TODO*///		CHANGE_PC;
/*TODO*///	
/*TODO*///		CHECK_IRQ_LINES();
/*TODO*///		UpdateState();
            hd6309_Regs Regs = (hd6309_Regs) src;
            hd6309.pc = Regs.pc;
            hd6309.ppc = Regs.ppc;
            hd6309.a = Regs.a;
            hd6309.b = Regs.b;
            hd6309.dp = Regs.dp;
            hd6309.u = Regs.u;
            hd6309.s = Regs.s;
            hd6309.x = Regs.x;
            hd6309.y = Regs.y;
            hd6309.cc = Regs.cc;
            hd6309.ireg = Regs.ireg;
            hd6309.irq_state[0] = Regs.irq_state[0];
            hd6309.irq_state[1] = Regs.irq_state[1];
            hd6309.extra_cycles = Regs.extra_cycles;
            hd6309.irq_callback = Regs.irq_callback;
            hd6309.int_state = Regs.int_state;
            hd6309.nmi_state = Regs.nmi_state;
            hd6309.e = Regs.e;
            hd6309.f = Regs.f;
            hd6309.v = Regs.v;
            hd6309.md = Regs.md;

            CHANGE_PC();
            CHECK_IRQ_LINES();
            
            UpdateState();
	}
	
	/****************************************************************************
	 * Return program counter
	 ****************************************************************************/
	public static int hd6309_get_pc()
	{
		return hd6309.pc;
	}
	
	
/*TODO*///	/****************************************************************************
/*TODO*///	 * Set program counter
/*TODO*///	 ****************************************************************************/
/*TODO*///	void hd6309_set_pc(unsigned val)
/*TODO*///	{
/*TODO*///		PC = val;
/*TODO*///		CHANGE_PC;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/****************************************************************************
/*TODO*///	 * Return stack pointer
/*TODO*///	 ****************************************************************************/
/*TODO*///	unsigned hd6309_get_sp(void)
/*TODO*///	{
/*TODO*///		return S;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/****************************************************************************
/*TODO*///	 * Set stack pointer
/*TODO*///	 ****************************************************************************/
/*TODO*///	void hd6309_set_sp(unsigned val)
/*TODO*///	{
/*TODO*///		S = val;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/****************************************************************************/
/*TODO*///	/* Return a specific register												*/
/*TODO*///	/****************************************************************************/
/*TODO*///	unsigned hd6309_get_reg(int regnum)
/*TODO*///	{
/*TODO*///		switch( regnum )
/*TODO*///		{
/*TODO*///			case HD6309_PC: return PC;
/*TODO*///			case HD6309_S: return S;
/*TODO*///			case HD6309_CC: return CC;
/*TODO*///			case HD6309_MD: return MD;
/*TODO*///			case HD6309_U: return U;
/*TODO*///			case HD6309_A: return A;
/*TODO*///			case HD6309_B: return B;
/*TODO*///			case HD6309_E: return E;
/*TODO*///			case HD6309_F: return F;
/*TODO*///			case HD6309_X: return X;
/*TODO*///			case HD6309_Y: return Y;
/*TODO*///			case HD6309_V: return V;
/*TODO*///			case HD6309_DP: return DP;
/*TODO*///			case HD6309_NMI_STATE: return hd6309.nmi_state;
/*TODO*///			case HD6309_IRQ_STATE: return hd6309.irq_state[HD6309_IRQ_LINE];
/*TODO*///			case HD6309_FIRQ_STATE: return hd6309.irq_state[HD6309_FIRQ_LINE];
/*TODO*///			case REG_PREVIOUSPC: return PPC;
/*TODO*///			default:
/*TODO*///				if( regnum <= REG_SP_CONTENTS )
/*TODO*///				{
/*TODO*///					unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///					if( offset < 0xffff )
/*TODO*///						return ( RM( offset ) << 8 ) | RM( offset + 1 );
/*TODO*///				}
/*TODO*///		}
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/****************************************************************************/
/*TODO*///	/* Set a specific register													*/
/*TODO*///	/****************************************************************************/
/*TODO*///	void hd6309_set_reg(int regnum, unsigned val)
/*TODO*///	{
/*TODO*///		switch( regnum )
/*TODO*///		{
/*TODO*///			case HD6309_PC: PC = val; CHANGE_PC; break;
/*TODO*///			case HD6309_S: S = val; break;
/*TODO*///			case HD6309_CC: CC = val; CHECK_IRQ_LINES(); break;
/*TODO*///			case HD6309_MD: MD = val; UpdateState(); break;
/*TODO*///			case HD6309_U: U = val; break;
/*TODO*///			case HD6309_A: A = val; break;
/*TODO*///			case HD6309_B: B = val; break;
/*TODO*///			case HD6309_E: E = val; break;
/*TODO*///			case HD6309_F: F = val; break;
/*TODO*///			case HD6309_X: X = val; break;
/*TODO*///			case HD6309_Y: Y = val; break;
/*TODO*///			case HD6309_V: V = val; break;
/*TODO*///			case HD6309_DP: DP = val; break;
/*TODO*///			case HD6309_NMI_STATE: hd6309.nmi_state = val; break;
/*TODO*///			case HD6309_IRQ_STATE: hd6309.irq_state[HD6309_IRQ_LINE] = val; break;
/*TODO*///			case HD6309_FIRQ_STATE: hd6309.irq_state[HD6309_FIRQ_LINE] = val; break;
/*TODO*///			default:
/*TODO*///				if( regnum <= REG_SP_CONTENTS )
/*TODO*///				{
/*TODO*///					unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///					if( offset < 0xffff )
/*TODO*///					{
/*TODO*///						WM( offset, (val >> 8) & 0xff );
/*TODO*///						WM( offset+1, val & 0xff );
/*TODO*///					}
/*TODO*///				}
/*TODO*///		}
/*TODO*///	}
	
	public static void hd6309_init()
	{
/*TODO*///		int cpu = cpu_getactivecpu();
/*TODO*///		state_save_register_UINT16("hd6309", cpu, "PC", &PC, 1);
/*TODO*///		state_save_register_UINT16("hd6309", cpu, "U", &U, 1);
/*TODO*///		state_save_register_UINT16("hd6309", cpu, "S", &S, 1);
/*TODO*///		state_save_register_UINT16("hd6309", cpu, "X", &X, 1);
/*TODO*///		state_save_register_UINT16("hd6309", cpu, "Y", &Y, 1);
/*TODO*///		state_save_register_UINT16("hd6309", cpu, "V", &V, 1);
/*TODO*///		state_save_register_UINT8("hd6309", cpu, "DP", &DP, 1);
/*TODO*///		state_save_register_UINT8("hd6309", cpu, "CC", &CC, 1);
/*TODO*///		state_save_register_UINT8("hd6309", cpu, "MD", &MD, 1);
/*TODO*///		state_save_register_UINT8("hd6309", cpu, "INT", &hd6309.int_state, 1);
/*TODO*///		state_save_register_UINT8("hd6309", cpu, "NMI", &hd6309.nmi_state, 1);
/*TODO*///		state_save_register_UINT8("hd6309", cpu, "IRQ", &hd6309.irq_state[0], 1);
/*TODO*///		state_save_register_UINT8("hd6309", cpu, "FIRQ", &hd6309.irq_state[1], 1);
	}
	
	/****************************************************************************/
	/* Reset registers to their initial values									*/
	/****************************************************************************/
	public static void hd6309_reset(Object param)
	{
            hd6309.int_state = 0;
            hd6309.nmi_state = CLEAR_LINE;
            hd6309.irq_state[0] = CLEAR_LINE;
            hd6309.irq_state[0] = CLEAR_LINE;

            hd6309.dp = 0;/* Reset direct page register */
            hd6309.md = 0;/* Mode register gets reset */

            hd6309.cc |= CC_II;/* IRQ disabled */
            hd6309.cc |= CC_IF;/* FIRQ disabled */

            hd6309.pc = (RM16(0xfffe)) & 0xFFFF;
            CHANGE_PC();
            UpdateState();
	}
	
/*TODO*///	void hd6309_exit(void)
/*TODO*///	{
/*TODO*///		/* nothing to do ? */
/*TODO*///	}
	
	/* Generate interrupts */
	/****************************************************************************
	 * Set NMI line state
	 ****************************************************************************/
        public static void hd6309_set_nmi_line_old(int state)
	{
            if (hd6309.nmi_state == state) {
            return;
        }
        hd6309.nmi_state = state;
        //LOG(("M6809#%d set_nmi_line %d\n", cpu_getactivecpu(), state));
        if (state == CLEAR_LINE) {
            return;
        }

        /* if the stack was not yet initialized */
        if ((hd6309.int_state & HD6309_LDS) == 0) {
            return;
        }

        hd6309.int_state &= ~HD6309_SYNC;
        /* HJB 990225: state already saved by CWAI? */
        if ((hd6309.int_state & HD6309_CWAI) != 0) {
            hd6309.int_state &= ~HD6309_CWAI;
            hd6309.extra_cycles += 7;
            /* subtract +7 cycles next time */
        } else {
            hd6309.cc |= CC_E;
            /* save entire state */
            PUSHWORD(hd6309.pc);
            PUSHWORD(hd6309.u);
            PUSHWORD(hd6309.y);
            PUSHWORD(hd6309.x);
            PUSHBYTE(hd6309.dp);
            PUSHBYTE(hd6309.b);
            PUSHBYTE(hd6309.a);
            PUSHBYTE(hd6309.cc);
            hd6309.extra_cycles += 19;
            /* subtract +19 cycles next time */
        }
        hd6309.cc |= CC_IF | CC_II;
        /* inhibit FIRQ and IRQ */
        hd6309.pc = RM16(0xfffc) & 0xFFFF;
        CHANGE_PC();
        }
        
	public static void hd6309_set_nmi_line(int state)
	{
		if (hd6309.nmi_state == state) return;
		hd6309.nmi_state = state;
		//LOG(("HD6309#%d set_nmi_line %d\n", cpu_getactivecpu(), state));
		if( state == CLEAR_LINE ) return;
	
		/* if the stack was not yet initialized */
		if( (hd6309.int_state & HD6309_LDS) == 0 ) return;
	
		hd6309.int_state &= ~HD6309_SYNC;
		/* HJB 990225: state already saved by CWAI? */
		if(( hd6309.int_state & HD6309_CWAI ) != 0)
		{
			hd6309.int_state &= ~HD6309_CWAI;
			hd6309.extra_cycles += 7;	/* subtract +7 cycles next time */
		}
		else
		{
			hd6309.cc |= CC_E; 				/* save entire state */
			PUSHWORD(hd6309.pc);
			PUSHWORD(hd6309.u);
			PUSHWORD(hd6309.y);
			PUSHWORD(hd6309.x);
			PUSHBYTE(hd6309.dp);
			if ((hd6309.md & MD_EM) != 0)
			{
				PUSHBYTE(hd6309.f);
				PUSHBYTE(hd6309.e);
				hd6309.extra_cycles += 2; /* subtract +2 cycles */
			}
	
			PUSHBYTE(hd6309.b);
			PUSHBYTE(hd6309.a);
			PUSHBYTE(hd6309.cc);
			hd6309.extra_cycles += 19;	/* subtract +19 cycles next time */
		}
		hd6309.cc |= CC_IF | CC_II;			/* inhibit FIRQ and IRQ */
		hd6309.pc = RM16(0xfffc);
		CHANGE_PC();
	}
	
	/****************************************************************************
	 * Set IRQ line state
	 ****************************************************************************/
	public static void hd6309_set_irq_line(int irqline, int state)
	{
		//LOG(("HD6309#%d set_irq_line %d, %d\n", cpu_getactivecpu(), irqline, state));
		hd6309.irq_state[irqline] = state;
		if (state == CLEAR_LINE) return;
		CHECK_IRQ_LINES();
	}
	
/*TODO*///	/****************************************************************************
/*TODO*///	 * Set IRQ vector callback
/*TODO*///	 ****************************************************************************/
/*TODO*///	void hd6309_set_irq_callback(int (*callback)(int irqline))
/*TODO*///	{
/*TODO*///		hd6309.irq_callback = callback;
/*TODO*///	}
	
	/****************************************************************************
	 * Return a formatted string for a register
	 ****************************************************************************/
	public String hd6309_info(Object context, int regnum)
	{
/*TODO*///		static char buffer[16][47+1];
/*TODO*///		static int which = 0;
/*TODO*///		hd6309_Regs *r = context;
/*TODO*///	
/*TODO*///		which = (which+1) % 16;
/*TODO*///		buffer[which][0] = '\0';
/*TODO*///		if( !context )
/*TODO*///			r = &hd6309;
	
		switch( regnum )
		{
			case CPU_INFO_NAME: return "HD6309";
			case CPU_INFO_FAMILY: return "Hitachi 6309";
			case CPU_INFO_VERSION: return "1.0";
			case CPU_INFO_FILE: return "hd6309.java";
			case CPU_INFO_CREDITS: return "Copyright (C) John Butler 1997 and Tim Lindner 2000";
/*TODO*///			case CPU_INFO_REG_LAYOUT: return (const char*)hd6309_reg_layout;
/*TODO*///			case CPU_INFO_WIN_LAYOUT: return (const char*)hd6309_win_layout;
/*TODO*///	
/*TODO*///			case CPU_INFO_FLAGS:
/*TODO*///				sprintf(buffer[which], "%c%c%c%c%c%c%c%c (MD:%c%c%c%c)",
/*TODO*///					r.cc & 0x80 ? 'E':'.',
/*TODO*///					r.cc & 0x40 ? 'F':'.',
/*TODO*///					r.cc & 0x20 ? 'H':'.',
/*TODO*///					r.cc & 0x10 ? 'I':'.',
/*TODO*///					r.cc & 0x08 ? 'N':'.',
/*TODO*///					r.cc & 0x04 ? 'Z':'.',
/*TODO*///					r.cc & 0x02 ? 'V':'.',
/*TODO*///					r.cc & 0x01 ? 'C':'.',
/*TODO*///	
/*TODO*///					r.md & 0x80 ? 'E':'e',
/*TODO*///					r.md & 0x40 ? 'F':'f',
/*TODO*///					r.md & 0x02 ? 'I':'i',
/*TODO*///					r.md & 0x01 ? 'Z':'z');
/*TODO*///				break;
/*TODO*///			case CPU_INFO_REG+HD6309_PC: sprintf(buffer[which], "PC:%04X", r.pc.w.l); break;
/*TODO*///			case CPU_INFO_REG+HD6309_S: sprintf(buffer[which], "S:%04X", r.s.w.l); break;
/*TODO*///			case CPU_INFO_REG+HD6309_CC: sprintf(buffer[which], "CC:%02X", r.cc); break;
/*TODO*///			case CPU_INFO_REG+HD6309_MD: sprintf(buffer[which], "MD:%02X", r.md); break;
/*TODO*///			case CPU_INFO_REG+HD6309_U: sprintf(buffer[which], "U:%04X", r.u.w.l); break;
/*TODO*///			case CPU_INFO_REG+HD6309_A: sprintf(buffer[which], "A:%02X", r.d.b.h); break;
/*TODO*///			case CPU_INFO_REG+HD6309_B: sprintf(buffer[which], "B:%02X", r.d.b.l); break;
/*TODO*///			case CPU_INFO_REG+HD6309_E: sprintf(buffer[which], "E:%02X", r.w.b.h); break;
/*TODO*///			case CPU_INFO_REG+HD6309_F: sprintf(buffer[which], "F:%02X", r.w.b.l); break;
/*TODO*///			case CPU_INFO_REG+HD6309_X: sprintf(buffer[which], "X:%04X", r.x.w.l); break;
/*TODO*///			case CPU_INFO_REG+HD6309_Y: sprintf(buffer[which], "Y:%04X", r.y.w.l); break;
/*TODO*///			case CPU_INFO_REG+HD6309_V: sprintf(buffer[which], "V:%04X", r.v.w.l); break;
/*TODO*///			case CPU_INFO_REG+HD6309_DP: sprintf(buffer[which], "DP:%02X", r.dp.b.h); break;
/*TODO*///			case CPU_INFO_REG+HD6309_NMI_STATE: sprintf(buffer[which], "NMI:%X", r.nmi_state); break;
/*TODO*///			case CPU_INFO_REG+HD6309_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r.irq_state[HD6309_IRQ_LINE]); break;
/*TODO*///			case CPU_INFO_REG+HD6309_FIRQ_STATE: sprintf(buffer[which], "FIRQ:%X", r.irq_state[HD6309_FIRQ_LINE]); break;
		}
/*TODO*///		return buffer[which];
            throw new UnsupportedOperationException("unsupported hd6309 cpu_info");
        }

/*TODO*///	unsigned hd6309_dasm(char *buffer, unsigned pc)
/*TODO*///	{
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///		return Dasm6309(buffer,pc);
/*TODO*///	#else
/*TODO*///		sprintf( buffer, "$%02X", cpu_readop(pc) );
/*TODO*///		return 1;
/*TODO*///	#endif
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* includes the actual opcode implementations */
	
	/* execute instructions on this CPU until icount expires */
	public static int hd6309_execute(int cycles)	/* NS 970908 */
	{
		hd6309_ICount[0] = cycles - hd6309.extra_cycles;
		hd6309.extra_cycles = 0;
	
		if ((hd6309.int_state & (HD6309_CWAI | HD6309_SYNC)) != 0)
		{
			hd6309_ICount[0] = 0;
		}
		else
		{
			do
			{
				hd6309.ppc = hd6309.pc;
	
/*TODO*///				CALL_MAME_DEBUG;
	
				hd6309.ireg = ROP(hd6309.pc);
				hd6309.pc = (hd6309.pc + 1) & 0xFFFF;
	
/*TODO*///	#ifdef BIG_SWITCH
/*TODO*///				switch( hd6309.ireg )
/*TODO*///				{
/*TODO*///				case 0x00: neg_di();   				break;
/*TODO*///				case 0x01: oim_di();   				break;
/*TODO*///				case 0x02: aim_di();   				break;
/*TODO*///				case 0x03: com_di();   				break;
/*TODO*///				case 0x04: lsr_di();   				break;
/*TODO*///				case 0x05: eim_di();   				break;
/*TODO*///				case 0x06: ror_di();   				break;
/*TODO*///				case 0x07: asr_di();   				break;
/*TODO*///				case 0x08: asl_di();   				break;
/*TODO*///				case 0x09: rol_di();   				break;
/*TODO*///				case 0x0a: dec_di();   				break;
/*TODO*///				case 0x0b: tim_di();   				break;
/*TODO*///				case 0x0c: inc_di();   				break;
/*TODO*///				case 0x0d: tst_di();   				break;
/*TODO*///				case 0x0e: jmp_di();   				break;
/*TODO*///				case 0x0f: clr_di();   				break;
/*TODO*///				case 0x10: pref10();				break;
/*TODO*///				case 0x11: pref11();				break;
/*TODO*///				case 0x12: nop();	   				break;
/*TODO*///				case 0x13: sync();	   				break;
/*TODO*///				case 0x14: sexw();	   				break;
/*TODO*///				case 0x15: IIError();				break;
/*TODO*///				case 0x16: lbra();	   				break;
/*TODO*///				case 0x17: lbsr();	   				break;
/*TODO*///				case 0x18: IIError();				break;
/*TODO*///				case 0x19: daa();	   				break;
/*TODO*///				case 0x1a: orcc();	   				break;
/*TODO*///				case 0x1b: IIError();				break;
/*TODO*///				case 0x1c: andcc();    				break;
/*TODO*///				case 0x1d: sex();	   				break;
/*TODO*///				case 0x1e: exg();	   				break;
/*TODO*///				case 0x1f: tfr();	   				break;
/*TODO*///				case 0x20: bra();	   				break;
/*TODO*///				case 0x21: brn();	   				break;
/*TODO*///				case 0x22: bhi();	   				break;
/*TODO*///				case 0x23: bls();	   				break;
/*TODO*///				case 0x24: bcc();	   				break;
/*TODO*///				case 0x25: bcs();	   				break;
/*TODO*///				case 0x26: bne();	   				break;
/*TODO*///				case 0x27: beq();	   				break;
/*TODO*///				case 0x28: bvc();	   				break;
/*TODO*///				case 0x29: bvs();	   				break;
/*TODO*///				case 0x2a: bpl();	   				break;
/*TODO*///				case 0x2b: bmi();	   				break;
/*TODO*///				case 0x2c: bge();	   				break;
/*TODO*///				case 0x2d: blt();	   				break;
/*TODO*///				case 0x2e: bgt();	   				break;
/*TODO*///				case 0x2f: ble();	   				break;
/*TODO*///				case 0x30: leax();	   				break;
/*TODO*///				case 0x31: leay();	   				break;
/*TODO*///				case 0x32: leas();	   				break;
/*TODO*///				case 0x33: leau();	   				break;
/*TODO*///				case 0x34: pshs();	   				break;
/*TODO*///				case 0x35: puls();	   				break;
/*TODO*///				case 0x36: pshu();	   				break;
/*TODO*///				case 0x37: pulu();	   				break;
/*TODO*///				case 0x38: IIError();				break;
/*TODO*///				case 0x39: rts();	   				break;
/*TODO*///				case 0x3a: abx();	   				break;
/*TODO*///				case 0x3b: rti();	   				break;
/*TODO*///				case 0x3c: cwai();					break;
/*TODO*///				case 0x3d: mul();					break;
/*TODO*///				case 0x3e: IIError();				break;
/*TODO*///				case 0x3f: swi();					break;
/*TODO*///				case 0x40: nega();	   				break;
/*TODO*///				case 0x41: IIError();				break;
/*TODO*///				case 0x42: IIError();				break;
/*TODO*///				case 0x43: coma();	   				break;
/*TODO*///				case 0x44: lsra();	   				break;
/*TODO*///				case 0x45: IIError();				break;
/*TODO*///				case 0x46: rora();	   				break;
/*TODO*///				case 0x47: asra();	   				break;
/*TODO*///				case 0x48: asla();	   				break;
/*TODO*///				case 0x49: rola();	   				break;
/*TODO*///				case 0x4a: deca();	   				break;
/*TODO*///				case 0x4b: IIError();				break;
/*TODO*///				case 0x4c: inca();	   				break;
/*TODO*///				case 0x4d: tsta();	   				break;
/*TODO*///				case 0x4e: IIError();				break;
/*TODO*///				case 0x4f: clra();	   				break;
/*TODO*///				case 0x50: negb();	   				break;
/*TODO*///				case 0x51: IIError();				break;
/*TODO*///				case 0x52: IIError();				break;
/*TODO*///				case 0x53: comb();	   				break;
/*TODO*///				case 0x54: lsrb();	   				break;
/*TODO*///				case 0x55: IIError();				break;
/*TODO*///				case 0x56: rorb();	   				break;
/*TODO*///				case 0x57: asrb();	   				break;
/*TODO*///				case 0x58: aslb();	   				break;
/*TODO*///				case 0x59: rolb();	   				break;
/*TODO*///				case 0x5a: decb();	   				break;
/*TODO*///				case 0x5b: IIError();				break;
/*TODO*///				case 0x5c: incb();	   				break;
/*TODO*///				case 0x5d: tstb();	   				break;
/*TODO*///				case 0x5e: IIError();				break;
/*TODO*///				case 0x5f: clrb();	   				break;
/*TODO*///				case 0x60: neg_ix();   				break;
/*TODO*///				case 0x61: oim_ix();   				break;
/*TODO*///				case 0x62: aim_ix();   				break;
/*TODO*///				case 0x63: com_ix();   				break;
/*TODO*///				case 0x64: lsr_ix();   				break;
/*TODO*///				case 0x65: eim_ix();   				break;
/*TODO*///				case 0x66: ror_ix();   				break;
/*TODO*///				case 0x67: asr_ix();   				break;
/*TODO*///				case 0x68: asl_ix();   				break;
/*TODO*///				case 0x69: rol_ix();   				break;
/*TODO*///				case 0x6a: dec_ix();   				break;
/*TODO*///				case 0x6b: tim_ix();   				break;
/*TODO*///				case 0x6c: inc_ix();   				break;
/*TODO*///				case 0x6d: tst_ix();   				break;
/*TODO*///				case 0x6e: jmp_ix();   				break;
/*TODO*///				case 0x6f: clr_ix();   				break;
/*TODO*///				case 0x70: neg_ex();   				break;
/*TODO*///				case 0x71: oim_ex();   				break;
/*TODO*///				case 0x72: aim_ex();   				break;
/*TODO*///				case 0x73: com_ex();   				break;
/*TODO*///				case 0x74: lsr_ex();   				break;
/*TODO*///				case 0x75: eim_ex();   				break;
/*TODO*///				case 0x76: ror_ex();   				break;
/*TODO*///				case 0x77: asr_ex();   				break;
/*TODO*///				case 0x78: asl_ex();   				break;
/*TODO*///				case 0x79: rol_ex();   				break;
/*TODO*///				case 0x7a: dec_ex();   				break;
/*TODO*///				case 0x7b: tim_ex();   				break;
/*TODO*///				case 0x7c: inc_ex();   				break;
/*TODO*///				case 0x7d: tst_ex();   				break;
/*TODO*///				case 0x7e: jmp_ex();   				break;
/*TODO*///				case 0x7f: clr_ex();   				break;
/*TODO*///				case 0x80: suba_im();  				break;
/*TODO*///				case 0x81: cmpa_im();  				break;
/*TODO*///				case 0x82: sbca_im();  				break;
/*TODO*///				case 0x83: subd_im();  				break;
/*TODO*///				case 0x84: anda_im();  				break;
/*TODO*///				case 0x85: bita_im();  				break;
/*TODO*///				case 0x86: lda_im();   				break;
/*TODO*///				case 0x87: IIError(); 				break;
/*TODO*///				case 0x88: eora_im();  				break;
/*TODO*///				case 0x89: adca_im();  				break;
/*TODO*///				case 0x8a: ora_im();   				break;
/*TODO*///				case 0x8b: adda_im();  				break;
/*TODO*///				case 0x8c: cmpx_im();  				break;
/*TODO*///				case 0x8d: bsr();	   				break;
/*TODO*///				case 0x8e: ldx_im();   				break;
/*TODO*///				case 0x8f: IIError();  				break;
/*TODO*///				case 0x90: suba_di();  				break;
/*TODO*///				case 0x91: cmpa_di();  				break;
/*TODO*///				case 0x92: sbca_di();  				break;
/*TODO*///				case 0x93: subd_di();  				break;
/*TODO*///				case 0x94: anda_di();  				break;
/*TODO*///				case 0x95: bita_di();  				break;
/*TODO*///				case 0x96: lda_di();   				break;
/*TODO*///				case 0x97: sta_di();   				break;
/*TODO*///				case 0x98: eora_di();  				break;
/*TODO*///				case 0x99: adca_di();  				break;
/*TODO*///				case 0x9a: ora_di();   				break;
/*TODO*///				case 0x9b: adda_di();  				break;
/*TODO*///				case 0x9c: cmpx_di();  				break;
/*TODO*///				case 0x9d: jsr_di();   				break;
/*TODO*///				case 0x9e: ldx_di();   				break;
/*TODO*///				case 0x9f: stx_di();   				break;
/*TODO*///				case 0xa0: suba_ix();  				break;
/*TODO*///				case 0xa1: cmpa_ix();  				break;
/*TODO*///				case 0xa2: sbca_ix();  				break;
/*TODO*///				case 0xa3: subd_ix();  				break;
/*TODO*///				case 0xa4: anda_ix();  				break;
/*TODO*///				case 0xa5: bita_ix();  				break;
/*TODO*///				case 0xa6: lda_ix();   				break;
/*TODO*///				case 0xa7: sta_ix();   				break;
/*TODO*///				case 0xa8: eora_ix();  				break;
/*TODO*///				case 0xa9: adca_ix();  				break;
/*TODO*///				case 0xaa: ora_ix();   				break;
/*TODO*///				case 0xab: adda_ix();  				break;
/*TODO*///				case 0xac: cmpx_ix();  				break;
/*TODO*///				case 0xad: jsr_ix();   				break;
/*TODO*///				case 0xae: ldx_ix();   				break;
/*TODO*///				case 0xaf: stx_ix();   				break;
/*TODO*///				case 0xb0: suba_ex();  				break;
/*TODO*///				case 0xb1: cmpa_ex();  				break;
/*TODO*///				case 0xb2: sbca_ex();  				break;
/*TODO*///				case 0xb3: subd_ex();  				break;
/*TODO*///				case 0xb4: anda_ex();  				break;
/*TODO*///				case 0xb5: bita_ex();  				break;
/*TODO*///				case 0xb6: lda_ex();   				break;
/*TODO*///				case 0xb7: sta_ex();   				break;
/*TODO*///				case 0xb8: eora_ex();  				break;
/*TODO*///				case 0xb9: adca_ex();  				break;
/*TODO*///				case 0xba: ora_ex();   				break;
/*TODO*///				case 0xbb: adda_ex();  				break;
/*TODO*///				case 0xbc: cmpx_ex();  				break;
/*TODO*///				case 0xbd: jsr_ex();   				break;
/*TODO*///				case 0xbe: ldx_ex();   				break;
/*TODO*///				case 0xbf: stx_ex();   				break;
/*TODO*///				case 0xc0: subb_im();  				break;
/*TODO*///				case 0xc1: cmpb_im();  				break;
/*TODO*///				case 0xc2: sbcb_im();  				break;
/*TODO*///				case 0xc3: addd_im();  				break;
/*TODO*///				case 0xc4: andb_im();  				break;
/*TODO*///				case 0xc5: bitb_im();  				break;
/*TODO*///				case 0xc6: ldb_im();   				break;
/*TODO*///				case 0xc7: IIError(); 				break;
/*TODO*///				case 0xc8: eorb_im();  				break;
/*TODO*///				case 0xc9: adcb_im();  				break;
/*TODO*///				case 0xca: orb_im();   				break;
/*TODO*///				case 0xcb: addb_im();  				break;
/*TODO*///				case 0xcc: ldd_im();   				break;
/*TODO*///				case 0xcd: ldq_im();   				break; /* in hd6309 was std_im */
/*TODO*///				case 0xce: ldu_im();   				break;
/*TODO*///				case 0xcf: IIError();  				break;
/*TODO*///				case 0xd0: subb_di();  				break;
/*TODO*///				case 0xd1: cmpb_di();  				break;
/*TODO*///				case 0xd2: sbcb_di();  				break;
/*TODO*///				case 0xd3: addd_di();  				break;
/*TODO*///				case 0xd4: andb_di();  				break;
/*TODO*///				case 0xd5: bitb_di();  				break;
/*TODO*///				case 0xd6: ldb_di();   				break;
/*TODO*///				case 0xd7: stb_di();   				break;
/*TODO*///				case 0xd8: eorb_di();  				break;
/*TODO*///				case 0xd9: adcb_di();  				break;
/*TODO*///				case 0xda: orb_di();   				break;
/*TODO*///				case 0xdb: addb_di();  				break;
/*TODO*///				case 0xdc: ldd_di();   				break;
/*TODO*///				case 0xdd: std_di();   				break;
/*TODO*///				case 0xde: ldu_di();   				break;
/*TODO*///				case 0xdf: stu_di();   				break;
/*TODO*///				case 0xe0: subb_ix();  				break;
/*TODO*///				case 0xe1: cmpb_ix();  				break;
/*TODO*///				case 0xe2: sbcb_ix();  				break;
/*TODO*///				case 0xe3: addd_ix();  				break;
/*TODO*///				case 0xe4: andb_ix();  				break;
/*TODO*///				case 0xe5: bitb_ix();  				break;
/*TODO*///				case 0xe6: ldb_ix();   				break;
/*TODO*///				case 0xe7: stb_ix();   				break;
/*TODO*///				case 0xe8: eorb_ix();  				break;
/*TODO*///				case 0xe9: adcb_ix();  				break;
/*TODO*///				case 0xea: orb_ix();   				break;
/*TODO*///				case 0xeb: addb_ix();  				break;
/*TODO*///				case 0xec: ldd_ix();   				break;
/*TODO*///				case 0xed: std_ix();   				break;
/*TODO*///				case 0xee: ldu_ix();   				break;
/*TODO*///				case 0xef: stu_ix();   				break;
/*TODO*///				case 0xf0: subb_ex();  				break;
/*TODO*///				case 0xf1: cmpb_ex();  				break;
/*TODO*///				case 0xf2: sbcb_ex();  				break;
/*TODO*///				case 0xf3: addd_ex();  				break;
/*TODO*///				case 0xf4: andb_ex();  				break;
/*TODO*///				case 0xf5: bitb_ex();  				break;
/*TODO*///				case 0xf6: ldb_ex();   				break;
/*TODO*///				case 0xf7: stb_ex();   				break;
/*TODO*///				case 0xf8: eorb_ex();  				break;
/*TODO*///				case 0xf9: adcb_ex();  				break;
/*TODO*///				case 0xfa: orb_ex();   				break;
/*TODO*///				case 0xfb: addb_ex();  				break;
/*TODO*///				case 0xfc: ldd_ex();   				break;
/*TODO*///				case 0xfd: std_ex();   				break;
/*TODO*///				case 0xfe: ldu_ex();   				break;
/*TODO*///				case 0xff: stu_ex();   				break;
/*TODO*///				}
/*TODO*///	#else
				(hd6309_main[hd6309.ireg]).handler();
/*TODO*///	#endif    /* BIG_SWITCH */
	
				hd6309_ICount[0] -= cycle_counts_page0[hd6309.ireg];
	
			} while( hd6309_ICount[0] > 0 );
	
			hd6309_ICount[0] -= hd6309.extra_cycles;
			hd6309.extra_cycles = 0;
		}
	
		return cycles - hd6309_ICount[0];	 /* NS 970908 */
	}
	
	public static void fetch_effective_address()
	{

            int postbyte = ROP_ARG(hd6309.pc) & 0xFF;
            hd6309.pc = (hd6309.pc + 1) & 0xFFFF;
            
            	switch(postbyte)
		{
		case 0x00: 
                    ea = (hd6309.x) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x01: 
                    ea = (hd6309.x + 1) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x02: 
                    ea = (hd6309.x + 2) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x03: 
                    ea = (hd6309.x + 3) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x04: 
                    ea = (hd6309.x + 4) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x05: 
                    ea = (hd6309.x + 5) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x06: 
                    ea = (hd6309.x + 6) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x07: 
                    ea = (hd6309.x + 7) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x08: 
                    ea = (hd6309.x + 8) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x09: 
                    ea = (hd6309.x + 9) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x0a: 
                    ea = (hd6309.x + 10) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x0b: 
                    ea = (hd6309.x + 11) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x0c: 
                    ea = (hd6309.x + 12) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x0d: 
                    ea = (hd6309.x + 13) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x0e: 
                    ea = (hd6309.x + 14) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x0f: 
                    ea = (hd6309.x + 15) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
	
		case 0x10: 
                    ea = (hd6309.x - 16) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x11: 
                    ea = (hd6309.x - 15) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x12: 
                    ea = (hd6309.x - 14) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x13: 
                    ea = (hd6309.x - 13) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x14: 
                    ea = (hd6309.x - 12) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x15: 
                    ea = (hd6309.x - 11) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x16: 
                    ea = (hd6309.x - 10) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x17: 
                    ea = (hd6309.x - 9) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x18: 
                    ea = (hd6309.x - 8) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x19: 
                    ea = (hd6309.x - 7) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x1a: 
                    ea = (hd6309.x - 6) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x1b: 
                    ea = (hd6309.x - 5) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x1c: 
                    ea = (hd6309.x - 4) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x1d: 
                    ea = (hd6309.x - 3) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x1e: 
                    ea = (hd6309.x - 2) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x1f: 
                    ea = (hd6309.x - 1) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
	
		case 0x20: 
                    ea = (hd6309.y) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x21: 
                    ea = (hd6309.y + 1) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x22: 
                    ea = (hd6309.y + 2) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x23: 
                    ea = (hd6309.y + 3) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x24: 
                    ea = (hd6309.y + 4) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x25: 
                    ea = (hd6309.y + 5) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x26: 
                    ea = (hd6309.y + 6) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x27: 
                    ea = (hd6309.y + 7) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x28: 
                    ea = (hd6309.y + 8) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x29: 
                    ea = (hd6309.y + 9) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x2a: 
                    ea = (hd6309.y + 10) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x2b: 
                    ea = (hd6309.y + 11) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x2c: 
                    ea = (hd6309.y + 12) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x2d: 
                    ea = (hd6309.y + 13) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x2e: 
                    ea = (hd6309.y + 14) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x2f: 
                    ea = (hd6309.y + 15) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
	
		case 0x30: 
                    ea = (hd6309.y - 16) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x31: 
                    ea = (hd6309.y - 15) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x32: 
                    ea = (hd6309.y - 14) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x33: 
                    ea = (hd6309.y - 3) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x34: 
                    ea = (hd6309.y - 12) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x35: 
                    ea = (hd6309.y - 11) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x36: 
                    ea = (hd6309.y - 10) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x37: 
                    ea = (hd6309.y - 9) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x38: 
                    ea = (hd6309.y - 8) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x39: 
                    ea = (hd6309.y - 7) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x3a: 
                    ea = (hd6309.y - 6) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x3b: 
                    ea = (hd6309.y - 5) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x3c: 
                    ea = (hd6309.y - 4) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x3d: 
                    ea = (hd6309.y - 3) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x3e: 
                    ea = (hd6309.y - 2) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x3f:
                    ea = (hd6309.y - 1) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;

		case 0x40: 
                    ea = (hd6309.u) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x41: 
                    ea = (hd6309.u + 1) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x42: 
                    ea = (hd6309.u + 2) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x43: 
                    ea = (hd6309.u + 3) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x44: 
                    ea = (hd6309.u + 4) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x45: 
                    ea = (hd6309.u + 5) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x46: 
                    ea = (hd6309.u + 6) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x47: 
                    ea = (hd6309.u + 7) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x48: 
                    ea = (hd6309.u + 8) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x49: 
                    ea = (hd6309.u + 9) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x4a: 
                    ea = (hd6309.u + 10) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x4b: 
                    ea = (hd6309.u + 11) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x4c: 
                    ea = (hd6309.u + 12) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x4d: 
                    ea = (hd6309.u + 13) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x4e: 
                    ea = (hd6309.u + 14) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x4f: 
                    ea = (hd6309.u + 15) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
	
		case 0x50: 
                    ea = (hd6309.u - 16) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x51: 
                    ea = (hd6309.u - 15) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x52: 
                    ea = (hd6309.u - 14) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x53: 
                    ea = (hd6309.u - 13) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x54: 
                    ea = (hd6309.u - 12) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x55: 
                    ea = (hd6309.u - 11) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x56: 
                    ea = (hd6309.u - 10) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x57: 
                    ea = (hd6309.u - 9) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x58: 
                    ea = (hd6309.u - 8) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x59: 
                    ea = (hd6309.u - 7) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x5a: 
                    ea = (hd6309.u - 6) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x5b: 
                    ea = (hd6309.u - 5) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x5c: 
                    ea = (hd6309.u - 4) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x5d: 
                    ea = (hd6309.u - 3) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x5e: 
                    ea = (hd6309.u - 2) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x5f: 
                    ea = (hd6309.u - 1) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
	
		case 0x60: 
                    ea = (hd6309.s) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x61: 
                    ea = (hd6309.s + 1) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x62: 
                    ea = (hd6309.s + 2) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x63: 
                    ea = (hd6309.s + 3) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x64: 
                    ea = (hd6309.s + 4) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x65: 
                    ea = (hd6309.s + 5) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x66: 
                    ea = (hd6309.s + 6) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x67: 
                    ea = (hd6309.s + 7) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x68: 
                    ea = (hd6309.s + 8) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x69: 
                    ea = (hd6309.s + 9) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x6a: 
                    ea = (hd6309.s + 10) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x6b: 
                    ea = (hd6309.s + 11) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x6c: 
                    ea = (hd6309.s + 12) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x6d: 
                    ea = (hd6309.s + 13) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x6e: 
                    ea = (hd6309.s + 14) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x6f: 
                    ea = (hd6309.s + 15) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
	
		case 0x70: 
                    ea = (hd6309.s - 16) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x71: 
                    ea = (hd6309.s - 15) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x72: 
                    ea = (hd6309.s - 14) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x73: 
                    ea = (hd6309.s - 13) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x74: 
                    ea = (hd6309.s - 12) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x75: 
                    ea = (hd6309.s - 11) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x76: 
                    ea = (hd6309.s - 10) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x77: 
                    ea = (hd6309.s - 9) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x78: 
                    ea = (hd6309.s - 8) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x79: 
                    ea = (hd6309.s - 7) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x7a: 
                    ea = (hd6309.s - 6) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x7b: 
                    ea = (hd6309.s - 5) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x7c: 
                    ea = (hd6309.s - 4) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x7d: 
                    ea = (hd6309.s - 3) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x7e: 
                    ea = (hd6309.s - 2) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x7f: 
                    ea = (hd6309.s - 1) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;

		case 0x80: 
                    ea = hd6309.x & 0xFFFF;
                    hd6309.x = (hd6309.x + 1) & 0xFFFF;
                    hd6309_ICount[0] -= 2;
                    break;
		case 0x81: 
                    ea = hd6309.x & 0xFFFF;
                    hd6309.x = (hd6309.x + 2) & 0xFFFF;
                    hd6309_ICount[0] -= 3;
                    break;
		case 0x82: 
                    hd6309.x = (hd6309.x - 1) & 0xFFFF;
                    ea = hd6309.x & 0xFFFF;
                    hd6309_ICount[0] -= 3;
                    break;
		case 0x83: 
                    hd6309.x = (hd6309.x - 2) & 0xFFFF;
                    ea = hd6309.x & 0xFFFF;
                    hd6309_ICount[0] -= 3;
                    break;
		case 0x84: 
                    ea = hd6309.x & 0xFFFF;
                    break;
		case 0x85: 
                    ea = (hd6309.x + SIGNED(hd6309.b)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x86: 
                    ea = (hd6309.x + SIGNED(hd6309.a)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x87: 
                    ea = (hd6309.x + SIGNED(hd6309.e)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x88: 
                    ea = IMMBYTE();
                    ea = (hd6309.x + SIGNED(ea)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x89: 
                    ea = IMMWORD();
                    ea = (ea + hd6309.x) & 0xFFFF;
                    hd6309_ICount[0] -= 4;
                    break;
                    
		case 0x8a: 
                    ea = (hd6309.x + SIGNED(hd6309.f)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0x8b: 
                    ea = (hd6309.x + getDreg()) & 0xFFFF;
                    hd6309_ICount[0] -= 4;
                    break;
                    
/*TODO*///		case 0x8c: IMMBYTE(EA); 	EA=PC+SIGNED(EA);						break;
/*TODO*///		case 0x8d: IMMWORD(ea); 	EA+=PC; 								break;
/*TODO*///		case 0x8e: EA=X+W;													break;
/*TODO*///		case 0x8f: EA=W;		 											break;

/*TODO*///		case 0x90: EA=W;								EAD=RM16(EAD);		break;
/*TODO*///		case 0x91: EA=X;	X+=2;						EAD=RM16(EAD);		break;
/*TODO*///		case 0x92: X--; 	EA=X;						EAD=RM16(EAD);		break;
/*TODO*///		case 0x93: X-=2;	EA=X;						EAD=RM16(EAD);		break;
		case 0x94: 
                    ea = (hd6309.x) & 0xFFFF;
                    ea = RM16(ea);
                    hd6309_ICount[0] -= 4;
                    break;
		case 0x95: 
                    ea = (hd6309.x + SIGNED(hd6309.b)) & 0xFFFF;
                    ea = RM16(ea);
                    hd6309_ICount[0] -= 4;
                    break;
		case 0x96: 
                    ea = (hd6309.x + SIGNED(hd6309.a)) & 0xFFFF;
                    ea = RM16(ea);
                    hd6309_ICount[0] -= 4;
                    break;
		case 0x97: 
                    ea = (hd6309.x + SIGNED(hd6309.e)) & 0xFFFF;
                    ea = RM16(ea);
                    hd6309_ICount[0] -= 4;
                    break;
/*TODO*///		case 0x98: IMMBYTE(EA); 	EA=X+SIGNED(EA);	EAD=RM16(EAD);		break;
/*TODO*///		case 0x99: IMMWORD(ea); 	EA+=X;				EAD=RM16(EAD);		break;
/*TODO*///		case 0x9a: EA=X+SIGNED(F);						EAD=RM16(EAD);		break;
/*TODO*///		case 0x9b: EA=X+D;								EAD=RM16(EAD);		break;
/*TODO*///		case 0x9c: IMMBYTE(EA); 	EA=PC+SIGNED(EA);	EAD=RM16(EAD);		break;
/*TODO*///		case 0x9d: IMMWORD(ea); 	EA+=PC; 			EAD=RM16(EAD);		break;
/*TODO*///		case 0x9e: EA=X+W;								EAD=RM16(EAD);		break;
		case 0x9f: 
                    ea = IMMWORD();
                    ea = RM16(ea);
                    hd6309_ICount[0] -= 8;
                    break;
	
		case 0xa0:                    
                    ea = hd6309.y & 0xFFFF;
                    hd6309.y = (hd6309.y + 1) & 0xFFFF;
                    hd6309_ICount[0] -= 2;
                    break;
		case 0xa1: 
                    ea = hd6309.y & 0xFFFF;
                    hd6309.y = (hd6309.y + 2) & 0xFFFF;
                    hd6309_ICount[0] -= 3;                
                    break;
		case 0xa2: 
                    hd6309.y = (hd6309.y - 1) & 0xFFFF;
                    ea = hd6309.y;
                    hd6309_ICount[0] -= 2;
                    break;
		case 0xa3: 
                    hd6309.y = (hd6309.y - 2) & 0xFFFF;
                    ea = hd6309.y;
                    hd6309_ICount[0] -= 3;
                    break;
		case 0xa4:                     
                    ea = hd6309.y & 0xFFFF;
                    hd6309_ICount[0] -= 2; // added by Chuso
                    break;
		case 0xa5: 
                    ea = (hd6309.y + SIGNED(hd6309.b)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0xa6: 
                    ea = (hd6309.y + SIGNED(hd6309.a)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0xa7: 
                    ea = (hd6309.y + SIGNED(hd6309.e)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0xa8: 
                    ea = IMMBYTE();
                    ea = (hd6309.y + SIGNED(ea)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0xa9: 
                    ea = IMMWORD();
                    ea = (ea + hd6309.y) & 0xFFFF;
                    hd6309_ICount[0] -= 4;
                    break;
		case 0xaa: 
                    ea = (hd6309.y + SIGNED(hd6309.f)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0xab: 
                    ea = (hd6309.y + getDreg()) & 0xFFFF;
                    hd6309_ICount[0] -= 4;
                    break;
/*TODO*///		case 0xac: IMMBYTE(EA); 	EA=PC+SIGNED(EA);						break;
/*TODO*///		case 0xad: IMMWORD(ea); 	EA+=PC; 								break;
/*TODO*///		case 0xae: EA=Y+W;													break;
/*TODO*///		case 0xaf: IMMWORD(ea);     EA+=W;									break;
/*TODO*///	
/*TODO*///		case 0xb0: IMMWORD(ea); 	EA+=W;				EAD=RM16(EAD);		break;
/*TODO*///		case 0xb1: EA=Y;	Y+=2;						EAD=RM16(EAD);		break;
/*TODO*///		case 0xb2: Y--; 	EA=Y;						EAD=RM16(EAD);		break;
/*TODO*///		case 0xb3: Y-=2;	EA=Y;						EAD=RM16(EAD);		break;
/*TODO*///		case 0xb4: EA=Y;								EAD=RM16(EAD);		break;
/*TODO*///		case 0xb5: EA=Y+SIGNED(B);						EAD=RM16(EAD);		break;
		case 0xb6: 
                    ea = (hd6309.y + SIGNED(hd6309.a)) & 0xFFFF;
                    ea = RM16(ea);
                    hd6309_ICount[0] -= 4;
                    break;
		case 0xb7: 
                    ea = (hd6309.y + SIGNED(hd6309.e)) & 0xFFFF;
                    ea = RM16(ea);
                    hd6309_ICount[0] -= 4;
                    break;
/*TODO*///		case 0xb8: IMMBYTE(EA); 	EA=Y+SIGNED(EA);	EAD=RM16(EAD);		break;
/*TODO*///		case 0xb9: IMMWORD(ea); 	EA+=Y;				EAD=RM16(EAD);		break;
		case 0xba: 
                    ea = (hd6309.y + SIGNED(hd6309.f)) & 0xFFFF;
                    ea = RM16(ea);
                    hd6309_ICount[0] -= 4;
                    break;
		case 0xbb: 
                    ea = (hd6309.y + getDreg()) & 0xFFFF;
                    ea = RM16(ea);
                    hd6309_ICount[0] -= 7;
                    break;
/*TODO*///		case 0xbc: IMMBYTE(EA); 	EA=PC+SIGNED(EA);	EAD=RM16(EAD);		break;
/*TODO*///		case 0xbd: IMMWORD(ea); 	EA+=PC; 			EAD=RM16(EAD);		break;
/*TODO*///		case 0xbe: EA=Y+W;								EAD=RM16(EAD);		break;
/*TODO*///		case 0xbf: IMMWORD(ea); 						EAD=RM16(EAD);		break;
	
		case 0xc0: 
                    ea = hd6309.u & 0xFFFF;
                    hd6309.u = (hd6309.u + 1) & 0xFFFF;
                    hd6309_ICount[0] -= 2;
                    break;
		case 0xc1: 
                    ea = hd6309.u & 0xFFFF;
                    hd6309.u = (hd6309.u + 2) & 0xFFFF;
                    hd6309_ICount[0] -= 3;
                    break;
		case 0xc2: 
                    hd6309.u = (hd6309.u - 1) & 0xFFFF;
                    ea = hd6309.u & 0xFFFF;
                    hd6309_ICount[0] -= 2;
                    break;
		case 0xc3: 
                    hd6309.u = (hd6309.u - 2) & 0xFFFF;
                    ea = hd6309.u & 0xFFFF;
                    hd6309_ICount[0] -= 3;
                    break;
		case 0xc4: 
                    ea = hd6309.u & 0xFFFF;
                    break;
		case 0xc5: 
                    ea = (hd6309.u + SIGNED(hd6309.b)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0xc6: 
                    ea = (hd6309.u + SIGNED(hd6309.a)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0xc7: 
                    ea = (hd6309.u + SIGNED(hd6309.e)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0xc8:                    						
                    ea = IMMBYTE();
                    ea = (hd6309.u + SIGNED(ea)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
/*TODO*///		case 0xc9: IMMWORD(ea); 	EA+=U;									break;
/*TODO*///		case 0xca: EA=U+SIGNED(F);											break;
		case 0xcb: 
                    ea = (hd6309.u + getDreg()) & 0xFFFF;
                    hd6309_ICount[0] -= 4;
                    break;
/*TODO*///		case 0xcc: IMMBYTE(EA); 	EA=PC+SIGNED(EA);						break;
/*TODO*///		case 0xcd: IMMWORD(ea); 	EA+=PC; 								break;
/*TODO*///		case 0xce: EA=U+W;													break;
/*TODO*///		case 0xcf: EA=W;            W+=2;									break;
/*TODO*///	
/*TODO*///		case 0xd0: EA=W;	W+=2;						EAD=RM16(EAD);		break;
/*TODO*///		case 0xd1: EA=U;	U+=2;						EAD=RM16(EAD);		break;
/*TODO*///		case 0xd2: U--; 	EA=U;						EAD=RM16(EAD);		break;
/*TODO*///		case 0xd3: U-=2;	EA=U;						EAD=RM16(EAD);		break;
/*TODO*///		case 0xd4: EA=U;								EAD=RM16(EAD);		break;
		case 0xd5: 
                    ea = (hd6309.u + SIGNED(hd6309.b)) & 0xFFFF;
                    ea = RM16(ea);
                    hd6309_ICount[0] -= 4;
                    break;
		case 0xd6: 
                    ea = (hd6309.u + SIGNED(hd6309.a)) & 0xFFFF;
                    ea = RM16(ea);
                    hd6309_ICount[0] -= 4;
                    break;
		case 0xd7: 
                    ea = (hd6309.u + SIGNED(hd6309.e)) & 0xFFFF;
                    ea = RM16(ea);
                    hd6309_ICount[0] -= 4;
                    break;
/*TODO*///		case 0xd8: IMMBYTE(EA); 	EA=U+SIGNED(EA);	EAD=RM16(EAD);		break;
/*TODO*///		case 0xd9: IMMWORD(ea); 	EA+=U;				EAD=RM16(EAD);		break;
		case 0xda: 
                    ea = (hd6309.u + SIGNED(hd6309.f)) & 0xFFFF;
                    ea = RM16(ea);
                    hd6309_ICount[0] -= 4;
                    break;
/*TODO*///		case 0xdb: EA=U+D;								EAD=RM16(EAD);		break;
/*TODO*///		case 0xdc: IMMBYTE(EA); 	EA=PC+SIGNED(EA);	EAD=RM16(EAD);		break;
/*TODO*///		case 0xdd: IMMWORD(ea); 	EA+=PC; 			EAD=RM16(EAD);		break;
/*TODO*///		case 0xde: EA=U+W;								EAD=RM16(EAD);		break;
/*TODO*///		case 0xdf: IMMWORD(ea); 						EAD=RM16(EAD);		break;

		case 0xe0: 
                    ea = hd6309.s & 0xFFFF;
                    hd6309.s = (hd6309.s + 1) & 0xFFFF;
                    hd6309_ICount[0] -= 2;
                    break;
		case 0xe1: 
                    ea = hd6309.s & 0xFFFF;
                    hd6309.s = (hd6309.s + 2) & 0xFFFF;
                    hd6309_ICount[0] -= 3;
                    break;
		case 0xe2: 
                    hd6309.s = (hd6309.s - 1) & 0xFFFF;
                    ea = hd6309.s;
                    hd6309_ICount[0] -= 2;
                    break;
		case 0xe3: 
                    hd6309.s = (hd6309.s - 2) & 0xFFFF;
                    ea = hd6309.s;
                    hd6309_ICount[0] -= 3;
                    break;
		case 0xe4: 
                    ea = hd6309.s & 0xFFFF;
                    break;
		case 0xe5: 
                    ea = (hd6309.s + SIGNED(hd6309.b)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0xe6: 
                    ea = (hd6309.s + SIGNED(hd6309.a)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0xe7: 
                    ea = (hd6309.s + SIGNED(hd6309.e)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
		case 0xe8: 
                    ea = IMMBYTE();
                    ea = (hd6309.s + SIGNED(ea)) & 0xFFFF;
                    hd6309_ICount[0] -= 1;
                    break;
/*TODO*///		case 0xe9: IMMWORD(ea); 	EA+=S;									break;
/*TODO*///		case 0xea: EA=S+SIGNED(F);											break;
/*TODO*///		case 0xeb: EA=S+D;													break;
/*TODO*///		case 0xec: IMMBYTE(EA); 	EA=PC+SIGNED(EA);						break;
/*TODO*///		case 0xed: IMMWORD(ea); 	EA+=PC; 								break;
/*TODO*///		case 0xee: EA=S+W;													break;
/*TODO*///		case 0xef: W-=2;	EA=W;											break;
/*TODO*///	
/*TODO*///		case 0xf0: W-=2;	EA=W;						EAD=RM16(EAD);		break;
/*TODO*///		case 0xf1: EA=S;	S+=2;						EAD=RM16(EAD);		break;
/*TODO*///		case 0xf2: S--; 	EA=S;						EAD=RM16(EAD);		break;
/*TODO*///		case 0xf3: S-=2;	EA=S;						EAD=RM16(EAD);		break;
/*TODO*///		case 0xf4: EA=S;								EAD=RM16(EAD);		break;
/*TODO*///		case 0xf5: EA=S+SIGNED(B);						EAD=RM16(EAD);		break;
/*TODO*///		case 0xf6: EA=S+SIGNED(A);						EAD=RM16(EAD);		break;
/*TODO*///		case 0xf7: EA=S+SIGNED(E);						EAD=RM16(EAD);		break;
/*TODO*///		case 0xf8: IMMBYTE(EA); 	EA=S+SIGNED(EA);	EAD=RM16(EAD);		break;
/*TODO*///		case 0xf9: IMMWORD(ea); 	EA+=S;				EAD=RM16(EAD);		break;
/*TODO*///		case 0xfa: EA=S+SIGNED(F);						EAD=RM16(EAD);		break;
/*TODO*///		case 0xfb: EA=S+D;								EAD=RM16(EAD);		break;
/*TODO*///		case 0xfc: IMMBYTE(EA); 	EA=PC+SIGNED(EA);	EAD=RM16(EAD);		break;
/*TODO*///		case 0xfd: IMMWORD(ea); 	EA+=PC; 			EAD=RM16(EAD);		break;
/*TODO*///		case 0xfe: EA=S+W;								EAD=RM16(EAD);		break;
/*TODO*///		case 0xff: IMMWORD(ea); 						EAD=RM16(EAD);		break;
                    
                default:
                    if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented fetch_effective_address postbyte="+postbyte);
		}
	
		hd6309_ICount[0] -= index_cycle[postbyte];
	}
	
}
