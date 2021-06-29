/*
 * ported to v0.37b16
 *
 */
package gr.codebb.arcadeflex.v037b16.cpu.m6809;

//cpu imports
import static gr.codebb.arcadeflex.v037b16.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v037b16.cpu.m6809.m6809tlb.*;
//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b16.mame.memory.*;
import static gr.codebb.arcadeflex.v037b16.mame.memoryH.*;

public class m6809 extends cpu_interface {

    public static int[] m6809_ICount = new int[1];

    public m6809() {
        cpu_num = CPU_M6809;
        num_irqs = 2;
        default_vector = 0;
        overclock = 1.0;
        no_int = M6809_INT_NONE;
        irq_int = M6809_INT_IRQ;
        nmi_int = M6809_INT_NMI;
        databus_width = 8;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_BE;
        align_unit = 1;
        max_inst_len = 4;
        pgm_memory_base = 0;
        icount = m6809_ICount;
        m6809_ICount[0] = 50000;
    }

    /* 6809 Registers */
    public static class m6809_Regs {

        public /*PAIR*/ int pc;/* Program counter */
        public /*PAIR*/ int ppc;/* Previous program counter */
        public int a;
        public int b;//PAIR	d;/* Accumulator a and b */
        public /*PAIR*/ int dp;/* Direct Page register (page in MSB) */
        public int u;
        public int s;//PAIR	u, s;/* Stack pointers */
        public int x;
        public int y;//PAIR	x, y;/* Index registers */
        public int /*UINT8*/ cc;
        public int /*UINT8*/ ireg;/* First opcode */
        public int[] /*UINT8*/ irq_state = new int[2];
        public int extra_cycles;/* cycles used up by interrupts */
        public irqcallbacksPtr irq_callback;
        public int /*UINT8*/ int_state;/* SYNC and CWAI flags */
        public int /*UINT8*/ nmi_state;
    }

    static int getDreg()//compose dreg
    {
        return (m6809.a << 8 | m6809.b) & 0xFFFF;
    }

    static void setDreg(int reg) //write to dreg
    {
        m6809.a = reg >> 8 & 0xFF;
        m6809.b = reg & 0xFF;
    }
    /* flag bits in the cc register */
    public static final int CC_C = 0x01;/* Carry */
    public static final int CC_V = 0x02;/* Overflow */
    public static final int CC_Z = 0x04;/* Zero */
    public static final int CC_N = 0x08;/* Negative */
    public static final int CC_II = 0x10;/* Inhibit IRQ */
    public static final int CC_H = 0x20;/* Half (auxiliary) carry */
    public static final int CC_IF = 0x40;/* Inhibit FIRQ */
    public static final int CC_E = 0x80;/* entire state pushed */

 /* 6809 registers */
    public static m6809_Regs m6809 = new m6809_Regs();

    public static int ea;

    public static void CHANGE_PC() {
        change_pc16(m6809.pc & 0xFFFF);//ensure it's 16bit just in case
    }

    public static final int M6809_CWAI = 8;/* set when CWAI is waiting for an interrupt */
    public static final int M6809_SYNC = 16;/* set when SYNC is waiting for an interrupt */
    public static final int M6809_LDS = 32;/* set when LDS occured at least once */

    public static void CHECK_IRQ_LINES() {
        if (m6809.irq_state[M6809_IRQ_LINE] != CLEAR_LINE || m6809.irq_state[M6809_FIRQ_LINE] != CLEAR_LINE) {
            m6809.int_state &= ~M6809_SYNC;/* clear SYNC flag */
        }
        if (m6809.irq_state[M6809_FIRQ_LINE] != CLEAR_LINE && ((m6809.cc & CC_IF) == 0)) {
            /* fast IRQ */
 /* HJB 990225: state already saved by CWAI? */
            if ((m6809.int_state & M6809_CWAI) != 0) {
                m6809.int_state &= ~M6809_CWAI;/* clear CWAI */
                m6809.extra_cycles += 7;/* subtract +7 cycles */
            } else {
                m6809.cc &= ~CC_E;/* save 'short' state */
                PUSHWORD(m6809.pc);
                PUSHBYTE(m6809.cc);
                m6809.extra_cycles += 10;/* subtract +10 cycles */
            }
            m6809.cc |= CC_IF | CC_II;/* inhibit FIRQ and IRQ */
            m6809.pc = RM16(0xfff6);
            CHANGE_PC();
            m6809.irq_callback.handler(M6809_FIRQ_LINE);
        } else if (m6809.irq_state[M6809_IRQ_LINE] != CLEAR_LINE && ((m6809.cc & CC_II) == 0)) {
            /* standard IRQ */
 /* HJB 990225: state already saved by CWAI? */
            if ((m6809.int_state & M6809_CWAI) != 0) {
                m6809.int_state &= ~M6809_CWAI;/* clear CWAI flag */
                m6809.extra_cycles += 7;/* subtract +7 cycles */
            } else {
                m6809.cc |= CC_E;/* save entire state */
                PUSHWORD(m6809.pc);
                PUSHWORD(m6809.u);
                PUSHWORD(m6809.y);
                PUSHWORD(m6809.x);
                PUSHBYTE(m6809.dp);
                PUSHBYTE(m6809.b);
                PUSHBYTE(m6809.a);
                PUSHBYTE(m6809.cc);
                m6809.extra_cycles += 19;/* subtract +19 cycles */
            }
            m6809.cc |= CC_II;/* inhibit IRQ */
            m6809.pc = RM16(0xfff8);
            CHANGE_PC();
            m6809.irq_callback.handler(M6809_IRQ_LINE);
        }
    }

    public static int RM(int addr) {
        return (cpu_readmem16(addr) & 0xFF);
    }

    public static void WM(int addr, int value) {
        cpu_writemem16(addr, value & 0xFF);
    }

    public static char ROP(int addr) {
        return cpu_readop(addr);
    }

    public static char ROP_ARG(int addr) {
        return cpu_readop_arg(addr);
    }

    public static int RM16(int addr) {
        int i = RM(addr + 1 & 0xFFFF);
        i |= RM(addr) << 8;
        return i & 0xFFFF;
    }

    public static void WM16(int addr, int reg) {
        WM(addr + 1 & 0xFFFF, reg & 0xFF);
        WM(addr, reg >> 8);
    }

    /* macros to access memory */
    public static int IMMBYTE() {
        int reg = ROP_ARG(m6809.pc);
        m6809.pc = (m6809.pc + 1) & 0xFFFF;
        return reg & 0xFF;
    }

    public static int IMMWORD() {
        int reg = ((ROP_ARG(m6809.pc) << 8) | ROP_ARG((m6809.pc + 1)) & 0xffff);
        m6809.pc = (m6809.pc) + 2 & 0xFFFF;
        return reg;
    }

    public static void PUSHBYTE(int w) {
        m6809.s = m6809.s - 1 & 0xFFFF;
        WM(m6809.s, w);
    }

    public static void PUSHWORD(int w) {
        m6809.s = m6809.s - 1 & 0xFFFF;
        WM(m6809.s, w & 0xFF);
        m6809.s = m6809.s - 1 & 0xFFFF;
        WM(m6809.s, w >> 8);
    }

    public static int PULLBYTE() {
        int b = RM(m6809.s);
        m6809.s = (m6809.s + 1) & 0xFFFF;
        return b & 0xFF;
    }

    public static int PULLWORD() {
        int w = RM(m6809.s) << 8;
        m6809.s = m6809.s + 1 & 0xFFFF;
        w |= RM(m6809.s);
        m6809.s = m6809.s + 1 & 0xFFFF;
        return w & 0xFFFF;
    }

    public static void PSHUBYTE(int w) {
        m6809.u = (m6809.u - 1) & 0xFFFF;
        WM(m6809.u, w);
    }

    public static void PSHUWORD(int w) {
        m6809.u = (m6809.u - 1) & 0xFFFF;
        WM(m6809.u, w & 0xFF);
        m6809.u = (m6809.u - 1) & 0xFFFF;
        WM(m6809.u, w >>> 8);
    }

    public static int PULUBYTE() {
        int b = RM(m6809.u);
        m6809.u = (m6809.u + 1) & 0xFFFF;
        return b;
    }

    public static int PULUWORD()//TODO recheck
    {
        int w = RM(m6809.u) << 8;
        m6809.u = (m6809.u + 1) & 0xFFFF;
        w |= RM(m6809.u);
        m6809.u = (m6809.u + 1) & 0xFFFF;
        return w;
    }

    public static void CLR_HNZVC() {
        m6809.cc &= ~(CC_H | CC_N | CC_Z | CC_V | CC_C);
    }

    public static void CLR_NZV() {
        m6809.cc &= ~(CC_N | CC_Z | CC_V);
    }

    public static void CLR_HNZC() {
        m6809.cc &= ~(CC_H | CC_N | CC_Z | CC_C);
    }

    public static void CLR_NZVC() {
        m6809.cc &= ~(CC_N | CC_Z | CC_V | CC_C);
    }

    public static void CLR_Z() {
        m6809.cc &= ~(CC_Z);
    }

    public static void CLR_NZC() {
        m6809.cc &= ~(CC_N | CC_Z | CC_C);
    }

    public static void CLR_ZC() {
        m6809.cc &= ~(CC_Z | CC_C);
    }

    /* macros for CC -- CC bits affected should be reset before calling */
    public static void SET_Z(int a) {
        if (a == 0) {
            SEZ();
        }
    }

    public static void SET_Z8(int a) {
        SET_Z(a & 0xFF);
    }

    public static void SET_Z16(int a) {
        SET_Z(a & 0xFFFF);
    }

    public static void SET_N8(int a) {
        m6809.cc |= ((a & 0x80) >> 4);
    }

    public static void SET_N16(int a) {
        m6809.cc |= ((a & 0x8000) >> 12);
    }

    public static void SET_H(int a, int b, int r) {
        m6809.cc |= (((a ^ b ^ r) & 0x10) << 1);
    }

    public static void SET_C8(int a) {
        m6809.cc |= ((a & 0x100) >> 8);
    }

    public static void SET_C16(int a) {
        m6809.cc |= ((a & 0x10000) >> 16);
    }

    public static void SET_V8(int a, int b, int r) {
        m6809.cc |= (((a ^ b ^ r ^ (r >> 1)) & 0x80) >> 6);
    }

    public static void SET_V16(int a, int b, int r) {
        m6809.cc |= (((a ^ b ^ r ^ (r >> 1)) & 0x8000) >> 14);
    }

    public static int flags8i[]
            = /* increment */ {
                CC_Z, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                CC_N | CC_V, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N
            };
    public static int flags8d[]
            = /* decrement */ {
                CC_Z, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, CC_V,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N
            };

    public static void SET_FLAGS8I(int a) {
        m6809.cc |= flags8i[(a) & 0xff];
    }

    public static void SET_FLAGS8D(int a) {
        m6809.cc |= flags8d[(a) & 0xff];
    }

    /* combos */
    public static void SET_NZ8(int a) {
        SET_N8(a);
        SET_Z(a);
    }

    public static void SET_NZ16(int a) {
        SET_N16(a);
        SET_Z(a);
    }

    public static void SET_FLAGS8(int a, int b, int r) {
        SET_N8(r);
        SET_Z8(r);
        SET_V8(a, b, r);
        SET_C8(r);
    }

    public static void SET_FLAGS16(int a, int b, int r) {
        SET_N16(r);
        SET_Z16(r);
        SET_V16(a, b, r);
        SET_C16(r);
    }

    /* for treating an unsigned byte as a signed word */
    public static int SIGNED(int b) {
        return (((b & 0x80) != 0 ? b | 0xff00 : b)) & 0xFFFF;
    }

    public static void DIRECT() {
        ea = IMMBYTE();
        ea |= m6809.dp << 8;
    }

    public static void IMM8() {
        ea = m6809.pc;
        m6809.pc = (m6809.pc + 1) & 0xFFFF;
    }

    public static void IMM16() {
        ea = m6809.pc;
        m6809.pc = (m6809.pc + 2) & 0xFFFF;
    }

    public static void EXTENDED() {
        ea = IMMWORD();
    }

    /* macros to set status flags */
    public static void SEC() {
        m6809.cc |= CC_C;
    }

    /*TODO*///#define CLC CC&=~CC_C
    public static void SEZ() {
        m6809.cc |= CC_Z;
    }

    /*TODO*///#define CLZ CC&=~CC_Z
    /*TODO*///#define SEN CC|=CC_N
    /*TODO*///#define CLN CC&=~CC_N
    /*TODO*///#define SEV CC|=CC_V
    /*TODO*///#define CLV CC&=~CC_V
    /*TODO*///#define SEH CC|=CC_H
    /*TODO*///#define CLH CC&=~CC_H
    /* macros for convenience */
    public static int DIRBYTE() {
        DIRECT();
        return RM(ea) & 0xFF;
    }

    public static int DIRWORD() {
        DIRECT();
        return RM16(ea) & 0xFFFF;
    }

    public static int EXTBYTE() {
        EXTENDED();
        return RM(ea) & 0xFF;
    }

    public static int EXTWORD() {
        EXTENDED();
        return RM16(ea) & 0xFFFF;
    }

    /* macros for branch instructions */
    public static void BRANCH(boolean f) {
        int t = IMMBYTE();
        if (f) {
            m6809.pc = (m6809.pc + SIGNED(t)) & 0xFFFF;
            CHANGE_PC();
        }
    }

    public static void LBRANCH(boolean f) {
        int t = IMMWORD();
        if (f) {
            m6809_ICount[0] -= 1;
            m6809.pc = (m6809.pc + t) & 0xFFFF;
            CHANGE_PC();
        }
    }

    public static int NXORV() {
        return ((m6809.cc & CC_N) ^ ((m6809.cc & CC_V) << 2));
    }

    /* timings for 1-byte opcodes */
    public static int cycles1[]
            = {
                /*	 0	1  2  3  4	5  6  7  8	9  A  B  C	D  E  F */
                /*0*/6, 0, 0, 6, 6, 0, 6, 6, 6, 6, 6, 0, 6, 6, 3, 6,
                /*1*/ 0, 0, 2, 4, 0, 0, 5, 9, 0, 2, 3, 0, 3, 2, 8, 6,
                /*2*/ 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
                /*3*/ 4, 4, 4, 4, 5, 5, 5, 5, 0, 5, 3, 6, 20, 11, 0, 19,
                /*4*/ 2, 0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 0, 2,
                /*5*/ 2, 0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 0, 2,
                /*6*/ 6, 0, 0, 6, 6, 0, 6, 6, 6, 6, 6, 0, 6, 6, 3, 6,
                /*7*/ 7, 0, 0, 7, 7, 0, 7, 7, 7, 7, 7, 0, 7, 7, 4, 7,
                /*8*/ 2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 2, 4, 7, 3, 0,
                /*9*/ 4, 4, 4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 6, 7, 5, 5,
                /*A*/ 4, 4, 4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 6, 7, 5, 5,
                /*B*/ 5, 5, 5, 7, 5, 5, 5, 5, 5, 5, 5, 5, 7, 8, 6, 6,
                /*C*/ 2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 2, 3, 0, 3, 3,
                /*D*/ 4, 4, 4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5,
                /*E*/ 4, 4, 4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5,
                /*F*/ 5, 5, 5, 7, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6
            };

    /**
     * **************************************************************************
     * Get all registers in given buffer
     * **************************************************************************
     */
    @Override
    public Object get_context() {
        m6809_Regs regs = new m6809_Regs();
        regs.pc = m6809.pc;
        regs.ppc = m6809.ppc;
        regs.a = m6809.a;
        regs.b = m6809.b;
        regs.dp = m6809.dp;
        regs.u = m6809.u;
        regs.s = m6809.s;
        regs.x = m6809.x;
        regs.y = m6809.y;
        regs.cc = m6809.cc;
        regs.ireg = m6809.ireg;
        regs.irq_state[0] = m6809.irq_state[0];
        regs.irq_state[1] = m6809.irq_state[1];
        regs.extra_cycles = m6809.extra_cycles;
        regs.irq_callback = m6809.irq_callback;
        regs.int_state = m6809.int_state;
        regs.nmi_state = m6809.nmi_state;
        return regs;
    }

    /**
     * **************************************************************************
     * Set all registers to given values /
     * ***************************************************************************
     */
    @Override
    public void set_context(Object reg) {
        m6809_Regs Regs = (m6809_Regs) reg;
        m6809.pc = Regs.pc;
        m6809.ppc = Regs.ppc;
        m6809.a = Regs.a;
        m6809.b = Regs.b;
        m6809.dp = Regs.dp;
        m6809.u = Regs.u;
        m6809.s = Regs.s;
        m6809.x = Regs.x;
        m6809.y = Regs.y;
        m6809.cc = Regs.cc;
        m6809.ireg = Regs.ireg;
        m6809.irq_state[0] = Regs.irq_state[0];
        m6809.irq_state[1] = Regs.irq_state[1];
        m6809.extra_cycles = Regs.extra_cycles;
        m6809.irq_callback = Regs.irq_callback;
        m6809.int_state = Regs.int_state;
        m6809.nmi_state = Regs.nmi_state;

        CHANGE_PC();
        CHECK_IRQ_LINES();
    }

    /**
     * **************************************************************************
     * Return program counter
     * **************************************************************************
     */
    @Override
    public int get_pc() {
        return m6809.pc & 0xFFFF;
    }

    /**
     * **************************************************************************
     * Set program counter
     * **************************************************************************
     */
    @Override
    public void set_pc(int val) {
        m6809.pc = val & 0xFFFF;
        CHANGE_PC();
    }

    /**
     * **************************************************************************
     * Return stack pointer
     * **************************************************************************
     */
    @Override
    public int get_sp() {
        return m6809.s & 0xFFFF;
    }

    /**
     * **************************************************************************
     * Set stack pointer
     * **************************************************************************
     */
    @Override
    public void set_sp(int val) {
        m6809.s = val & 0xFFFF;
    }

    /**
     * **************************************************************************
     * Return a specific register
     * **************************************************************************
     */
    @Override
    public int get_reg(int regnum) {
        switch (regnum) {
            case M6809_PC:
                return m6809.pc;
            case M6809_S:
                return m6809.s;
            case M6809_CC:
                return m6809.cc;
            case M6809_U:
                return m6809.u;
            case M6809_A:
                return m6809.a;
            case M6809_B:
                return m6809.b;
            case M6809_X:
                return m6809.x;
            case M6809_Y:
                return m6809.y;
            case M6809_DP:
                return m6809.dp;
            case M6809_NMI_STATE:
                return m6809.nmi_state;
            case M6809_IRQ_STATE:
                return m6809.irq_state[M6809_IRQ_LINE];
            case M6809_FIRQ_STATE:
                return m6809.irq_state[M6809_FIRQ_LINE];
            case REG_PREVIOUSPC:
                return m6809.ppc;
            default:
                throw new UnsupportedOperationException("Not supported");
            /*TODO*///			if( regnum <= REG_SP_CONTENTS )
            /*TODO*///			{
            /*TODO*///				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
            /*TODO*///				if( offset < 0xffff )
            /*TODO*///					return ( RM( offset ) << 8 ) | RM( offset + 1 );
            /*TODO*///			}
        }
        /*TODO*///	return 0;
    }

    /**
     * **************************************************************************
     * Set a specific register
     * **************************************************************************
     */
    @Override
    public void set_reg(int regnum, int val) {
        switch (regnum) {
            /*TODO*///		case M6809_PC: PC = val; CHANGE_PC; break;
            /*TODO*///		case M6809_S: S = val; break;
            /*TODO*///		case M6809_CC: CC = val; CHECK_IRQ_LINES; break;
            /*TODO*///		case M6809_U: U = val; break;
            /*TODO*///		case M6809_A: A = val; break;
            /*TODO*///		case M6809_B: B = val; break;
            /*TODO*///		case M6809_X: X = val; break;
            case M6809_Y:
                m6809.y = (char) ((val & 0xFFFF));
                break;
            /*TODO*///		case M6809_DP: DP = val; break;
            /*TODO*///		case M6809_NMI_STATE: m6809.nmi_state = val; break;
            /*TODO*///		case M6809_IRQ_STATE: m6809.irq_state[M6809_IRQ_LINE] = val; break;
            /*TODO*///		case M6809_FIRQ_STATE: m6809.irq_state[M6809_FIRQ_LINE] = val; break;
            default:
                throw new UnsupportedOperationException("Not supported");
            /*TODO*///			if( regnum <= REG_SP_CONTENTS )
            /*TODO*///			{
            /*TODO*///				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
            /*TODO*///				if( offset < 0xffff )
            /*TODO*///				{
            /*TODO*///					WM( offset, (val >> 8) & 0xff );
            /*TODO*///					WM( offset+1, val & 0xff );
            /*TODO*///				}
            /*TODO*///			}
        }
    }

    /**
     * **************************************************************************
     * Reset registers to their initial values
     * **************************************************************************
     */
    @Override
    public void init() {
        /*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_save_register_UINT16("m6809", cpu, "PC", &PC, 1);
/*TODO*///	state_save_register_UINT16("m6809", cpu, "U", &U, 1);
/*TODO*///	state_save_register_UINT16("m6809", cpu, "S", &S, 1);
/*TODO*///	state_save_register_UINT16("m6809", cpu, "X", &X, 1);
/*TODO*///	state_save_register_UINT16("m6809", cpu, "Y", &Y, 1);
/*TODO*///	state_save_register_UINT8("m6809", cpu, "DP", &DP, 1);
/*TODO*///	state_save_register_UINT8("m6809", cpu, "CC", &CC, 1);
/*TODO*///	state_save_register_UINT8("m6809", cpu, "INT", &m6809.int_state, 1);
/*TODO*///	state_save_register_UINT8("m6809", cpu, "NMI", &m6809.nmi_state, 1);
/*TODO*///	state_save_register_UINT8("m6809", cpu, "IRQ", &m6809.irq_state[0], 1);
/*TODO*///	state_save_register_UINT8("m6809", cpu, "FIRQ", &m6809.irq_state[1], 1);
    }

    @Override
    public void reset(Object param) {
        m6809.int_state = 0;
        m6809.nmi_state = CLEAR_LINE;
        m6809.irq_state[0] = CLEAR_LINE;
        m6809.irq_state[0] = CLEAR_LINE;

        m6809.dp = 0;/* Reset direct page register */

        m6809.cc |= CC_II;/* IRQ disabled */
        m6809.cc |= CC_IF;/* FIRQ disabled */

        m6809.pc = (RM16(0xfffe)) & 0xFFFF;
        CHANGE_PC();
    }

    @Override
    public void exit() {
        /* nothing to do ? */
    }

    /* Generate interrupts */
    /**
     * **************************************************************************
     * Set NMI line state
     * **************************************************************************
     */
    @Override
    public void set_nmi_line(int state) {
        if (m6809.nmi_state == state) {
            return;
        }
        m6809.nmi_state = state;
        //LOG(("M6809#%d set_nmi_line %d\n", cpu_getactivecpu(), state));
        if (state == CLEAR_LINE) {
            return;
        }

        /* if the stack was not yet initialized */
        if ((m6809.int_state & M6809_LDS) == 0) {
            return;
        }

        m6809.int_state &= ~M6809_SYNC;
        /* HJB 990225: state already saved by CWAI? */
        if ((m6809.int_state & M6809_CWAI) != 0) {
            m6809.int_state &= ~M6809_CWAI;
            m6809.extra_cycles += 7;
            /* subtract +7 cycles next time */
        } else {
            m6809.cc |= CC_E;
            /* save entire state */
            PUSHWORD(m6809.pc);
            PUSHWORD(m6809.u);
            PUSHWORD(m6809.y);
            PUSHWORD(m6809.x);
            PUSHBYTE(m6809.dp);
            PUSHBYTE(m6809.b);
            PUSHBYTE(m6809.a);
            PUSHBYTE(m6809.cc);
            m6809.extra_cycles += 19;
            /* subtract +19 cycles next time */
        }
        m6809.cc |= CC_IF | CC_II;
        /* inhibit FIRQ and IRQ */
        m6809.pc = RM16(0xfffc) & 0xFFFF;
        CHANGE_PC();
    }

    /**
     * **************************************************************************
     * Set IRQ line state
     * **************************************************************************
     */
    @Override
    public void set_irq_line(int irqline, int state) {
        //LOG(("M6809#%d set_irq_line %d, %d\n", cpu_getactivecpu(), irqline, state));
        m6809.irq_state[irqline] = state;
        if (state == CLEAR_LINE) {
            return;
        }
        CHECK_IRQ_LINES();
    }

    /**
     * **************************************************************************
     * Set IRQ vector callback
     * **************************************************************************
     */
    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
        m6809.irq_callback = callback;
    }

    /**
     * **************************************************************************
     * Return a formatted string for a register
     * **************************************************************************
     */
    @Override
    public String cpu_info(Object context, int regnum) {
        /*TODO*///const char *m6809_info(void *context, int regnum)
        /*TODO*///{
        /*TODO*///	static char buffer[16][47+1];
        /*TODO*///	static int which = 0;
        /*TODO*///	m6809_Regs *r = context;
        /*TODO*///
        /*TODO*///	which = (which+1) % 16;
        /*TODO*///    buffer[which][0] = '\0';
        /*TODO*///	if( !context )
        /*TODO*///		r = &m6809;
        /*TODO*///
        switch (regnum) {
            case CPU_INFO_NAME:
                return "M6809";
            case CPU_INFO_FAMILY:
                return "Motorola 6809";
            case CPU_INFO_VERSION:
                return "1.1";
            case CPU_INFO_FILE:
                return "m6809.java";
            case CPU_INFO_CREDITS:
                return "Copyright (C) John Butler 1997";
            /*TODO*///		case CPU_INFO_REG_LAYOUT: return (const char*)m6809_reg_layout;
            /*TODO*///		case CPU_INFO_WIN_LAYOUT: return (const char*)m6809_win_layout;
            /*TODO*///
            /*TODO*///		case CPU_INFO_FLAGS:
            /*TODO*///			sprintf(buffer[which], "%c%c%c%c%c%c%c%c",
            /*TODO*///				r->cc & 0x80 ? 'E':'.',
            /*TODO*///				r->cc & 0x40 ? 'F':'.',
            /*TODO*///                r->cc & 0x20 ? 'H':'.',
            /*TODO*///                r->cc & 0x10 ? 'I':'.',
            /*TODO*///                r->cc & 0x08 ? 'N':'.',
            /*TODO*///                r->cc & 0x04 ? 'Z':'.',
            /*TODO*///                r->cc & 0x02 ? 'V':'.',
            /*TODO*///                r->cc & 0x01 ? 'C':'.');
            /*TODO*///            break;
            /*TODO*///		case CPU_INFO_REG+M6809_PC: sprintf(buffer[which], "PC:%04X", r->pc.w.l); break;
            /*TODO*///		case CPU_INFO_REG+M6809_S: sprintf(buffer[which], "S:%04X", r->s.w.l); break;
            /*TODO*///		case CPU_INFO_REG+M6809_CC: sprintf(buffer[which], "CC:%02X", r->cc); break;
            /*TODO*///		case CPU_INFO_REG+M6809_U: sprintf(buffer[which], "U:%04X", r->u.w.l); break;
            /*TODO*///		case CPU_INFO_REG+M6809_A: sprintf(buffer[which], "A:%02X", r->d.b.h); break;
            /*TODO*///		case CPU_INFO_REG+M6809_B: sprintf(buffer[which], "B:%02X", r->d.b.l); break;
            /*TODO*///		case CPU_INFO_REG+M6809_X: sprintf(buffer[which], "X:%04X", r->x.w.l); break;
            /*TODO*///		case CPU_INFO_REG+M6809_Y: sprintf(buffer[which], "Y:%04X", r->y.w.l); break;
            /*TODO*///		case CPU_INFO_REG+M6809_DP: sprintf(buffer[which], "DP:%02X", r->dp.b.h); break;
            /*TODO*///		case CPU_INFO_REG+M6809_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
            /*TODO*///		case CPU_INFO_REG+M6809_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state[M6809_IRQ_LINE]); break;
            /*TODO*///		case CPU_INFO_REG+M6809_FIRQ_STATE: sprintf(buffer[which], "FIRQ:%X", r->irq_state[M6809_FIRQ_LINE]); break;
        }
        /*TODO*///	return buffer[which];
        throw new UnsupportedOperationException("unsupported m6809 cpu_info");
    }

    /* execute instructions on this CPU until icount expires */
    @Override
    public int execute(int cycles) {
        m6809_ICount[0] = cycles - m6809.extra_cycles;
        m6809.extra_cycles = 0;

        if ((m6809.int_state & (M6809_CWAI | M6809_SYNC)) != 0) {
            m6809_ICount[0] = 0;
        } else {
            do {
                m6809.ppc = m6809.pc;
                //CALL_MAME_DEBUG;
                m6809.ireg = ROP(m6809.pc);
                m6809.pc = (m6809.pc + 1) & 0xFFFF;
                m6809_main[m6809.ireg].handler();
                m6809_ICount[0] -= cycles1[m6809.ireg];
            } while (m6809_ICount[0] > 0);

            m6809_ICount[0] -= m6809.extra_cycles;
            m6809.extra_cycles = 0;
        }

        return cycles - m6809_ICount[0];/* NS 970908 */
    }

    public static void fetch_effective_address() {
        int postbyte = ROP_ARG(m6809.pc) & 0xFF;
        m6809.pc = (m6809.pc + 1) & 0xFFFF;

        switch (postbyte) {
            /*TODO*///	case 0x00: EA=X;												m6809_ICount-=1;   break;
/*TODO*///	case 0x01: EA=X+1;												m6809_ICount-=1;   break;
/*TODO*///	case 0x02: EA=X+2;												m6809_ICount-=1;   break;
/*TODO*///	case 0x03: EA=X+3;												m6809_ICount-=1;   break;
/*TODO*///	case 0x04: EA=X+4;												m6809_ICount-=1;   break;
/*TODO*///	case 0x05: EA=X+5;												m6809_ICount-=1;   break;
/*TODO*///	case 0x06: EA=X+6;												m6809_ICount-=1;   break;
/*TODO*///	case 0x07: EA=X+7;												m6809_ICount-=1;   break;
/*TODO*///	case 0x08: EA=X+8;												m6809_ICount-=1;   break;
/*TODO*///	case 0x09: EA=X+9;												m6809_ICount-=1;   break;
/*TODO*///	case 0x0a: EA=X+10; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x0b: EA=X+11; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x0c: EA=X+12; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x0d: EA=X+13; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x0e: EA=X+14; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x0f: EA=X+15; 											m6809_ICount-=1;   break;
/*TODO*///
/*TODO*///	case 0x10: EA=X-16; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x11: EA=X-15; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x12: EA=X-14; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x13: EA=X-13; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x14: EA=X-12; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x15: EA=X-11; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x16: EA=X-10; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x17: EA=X-9;												m6809_ICount-=1;   break;
/*TODO*///	case 0x18: EA=X-8;												m6809_ICount-=1;   break;
/*TODO*///	case 0x19: EA=X-7;												m6809_ICount-=1;   break;
/*TODO*///	case 0x1a: EA=X-6;												m6809_ICount-=1;   break;
/*TODO*///	case 0x1b: EA=X-5;												m6809_ICount-=1;   break;
/*TODO*///	case 0x1c: EA=X-4;												m6809_ICount-=1;   break;
/*TODO*///	case 0x1d: EA=X-3;												m6809_ICount-=1;   break;
/*TODO*///	case 0x1e: EA=X-2;												m6809_ICount-=1;   break;
            case 0x1f:
                ea = (m6809.x - 1) & 0xFFFF;
                m6809_ICount[0] -= 1;
                break;
            /*TODO*///
/*TODO*///	case 0x20: EA=Y;												m6809_ICount-=1;   break;
/*TODO*///	case 0x21: EA=Y+1;												m6809_ICount-=1;   break;
/*TODO*///	case 0x22: EA=Y+2;												m6809_ICount-=1;   break;
/*TODO*///	case 0x23: EA=Y+3;												m6809_ICount-=1;   break;
/*TODO*///	case 0x24: EA=Y+4;												m6809_ICount-=1;   break;
/*TODO*///	case 0x25: EA=Y+5;												m6809_ICount-=1;   break;
/*TODO*///	case 0x26: EA=Y+6;												m6809_ICount-=1;   break;
/*TODO*///	case 0x27: EA=Y+7;												m6809_ICount-=1;   break;
/*TODO*///	case 0x28: EA=Y+8;												m6809_ICount-=1;   break;
/*TODO*///	case 0x29: EA=Y+9;												m6809_ICount-=1;   break;
/*TODO*///	case 0x2a: EA=Y+10; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x2b: EA=Y+11; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x2c: EA=Y+12; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x2d: EA=Y+13; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x2e: EA=Y+14; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x2f: EA=Y+15; 											m6809_ICount-=1;   break;
/*TODO*///
/*TODO*///	case 0x30: EA=Y-16; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x31: EA=Y-15; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x32: EA=Y-14; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x33: EA=Y-13; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x34: EA=Y-12; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x35: EA=Y-11; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x36: EA=Y-10; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x37: EA=Y-9;												m6809_ICount-=1;   break;
/*TODO*///	case 0x38: EA=Y-8;												m6809_ICount-=1;   break;
/*TODO*///	case 0x39: EA=Y-7;												m6809_ICount-=1;   break;
/*TODO*///	case 0x3a: EA=Y-6;												m6809_ICount-=1;   break;
/*TODO*///	case 0x3b: EA=Y-5;												m6809_ICount-=1;   break;
/*TODO*///	case 0x3c: EA=Y-4;												m6809_ICount-=1;   break;
/*TODO*///	case 0x3d: EA=Y-3;												m6809_ICount-=1;   break;
/*TODO*///	case 0x3e: EA=Y-2;												m6809_ICount-=1;   break;
/*TODO*///	case 0x3f: EA=Y-1;												m6809_ICount-=1;   break;
/*TODO*///
/*TODO*///	case 0x40: EA=U;												m6809_ICount-=1;   break;
/*TODO*///	case 0x41: EA=U+1;												m6809_ICount-=1;   break;
/*TODO*///	case 0x42: EA=U+2;												m6809_ICount-=1;   break;
/*TODO*///	case 0x43: EA=U+3;												m6809_ICount-=1;   break;
/*TODO*///	case 0x44: EA=U+4;												m6809_ICount-=1;   break;
/*TODO*///	case 0x45: EA=U+5;												m6809_ICount-=1;   break;
/*TODO*///	case 0x46: EA=U+6;												m6809_ICount-=1;   break;
/*TODO*///	case 0x47: EA=U+7;												m6809_ICount-=1;   break;
/*TODO*///	case 0x48: EA=U+8;												m6809_ICount-=1;   break;
/*TODO*///	case 0x49: EA=U+9;												m6809_ICount-=1;   break;
/*TODO*///	case 0x4a: EA=U+10; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x4b: EA=U+11; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x4c: EA=U+12; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x4d: EA=U+13; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x4e: EA=U+14; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x4f: EA=U+15; 											m6809_ICount-=1;   break;
/*TODO*///
/*TODO*///	case 0x50: EA=U-16; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x51: EA=U-15; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x52: EA=U-14; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x53: EA=U-13; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x54: EA=U-12; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x55: EA=U-11; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x56: EA=U-10; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x57: EA=U-9;												m6809_ICount-=1;   break;
/*TODO*///	case 0x58: EA=U-8;												m6809_ICount-=1;   break;
/*TODO*///	case 0x59: EA=U-7;												m6809_ICount-=1;   break;
/*TODO*///	case 0x5a: EA=U-6;												m6809_ICount-=1;   break;
/*TODO*///	case 0x5b: EA=U-5;												m6809_ICount-=1;   break;
/*TODO*///	case 0x5c: EA=U-4;												m6809_ICount-=1;   break;
/*TODO*///	case 0x5d: EA=U-3;												m6809_ICount-=1;   break;
/*TODO*///	case 0x5e: EA=U-2;												m6809_ICount-=1;   break;
/*TODO*///	case 0x5f: EA=U-1;												m6809_ICount-=1;   break;
/*TODO*///
/*TODO*///	case 0x60: EA=S;												m6809_ICount-=1;   break;
/*TODO*///	case 0x61: EA=S+1;												m6809_ICount-=1;   break;
/*TODO*///	case 0x62: EA=S+2;												m6809_ICount-=1;   break;
/*TODO*///	case 0x63: EA=S+3;												m6809_ICount-=1;   break;
/*TODO*///	case 0x64: EA=S+4;												m6809_ICount-=1;   break;
/*TODO*///	case 0x65: EA=S+5;												m6809_ICount-=1;   break;
/*TODO*///	case 0x66: EA=S+6;												m6809_ICount-=1;   break;
/*TODO*///	case 0x67: EA=S+7;												m6809_ICount-=1;   break;
/*TODO*///	case 0x68: EA=S+8;												m6809_ICount-=1;   break;
/*TODO*///	case 0x69: EA=S+9;												m6809_ICount-=1;   break;
/*TODO*///	case 0x6a: EA=S+10; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x6b: EA=S+11; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x6c: EA=S+12; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x6d: EA=S+13; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x6e: EA=S+14; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x6f: EA=S+15; 											m6809_ICount-=1;   break;
/*TODO*///
/*TODO*///	case 0x70: EA=S-16; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x71: EA=S-15; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x72: EA=S-14; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x73: EA=S-13; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x74: EA=S-12; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x75: EA=S-11; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x76: EA=S-10; 											m6809_ICount-=1;   break;
/*TODO*///	case 0x77: EA=S-9;												m6809_ICount-=1;   break;
/*TODO*///	case 0x78: EA=S-8;												m6809_ICount-=1;   break;
/*TODO*///	case 0x79: EA=S-7;												m6809_ICount-=1;   break;
/*TODO*///	case 0x7a: EA=S-6;												m6809_ICount-=1;   break;
/*TODO*///	case 0x7b: EA=S-5;												m6809_ICount-=1;   break;
/*TODO*///	case 0x7c: EA=S-4;												m6809_ICount-=1;   break;
/*TODO*///	case 0x7d: EA=S-3;												m6809_ICount-=1;   break;
/*TODO*///	case 0x7e: EA=S-2;												m6809_ICount-=1;   break;
/*TODO*///	case 0x7f: EA=S-1;												m6809_ICount-=1;   break;
            case 0x80:
                ea = m6809.x & 0xFFFF;
                m6809.x = (m6809.x + 1) & 0xFFFF;
                m6809_ICount[0] -= 2;
                break;
            /*TODO*///	case 0x81: EA=X;	X+=2;										m6809_ICount-=3;   break;
/*TODO*///	case 0x82: X--; 	EA=X;										m6809_ICount-=2;   break;
/*TODO*///	case 0x83: X-=2;	EA=X;										m6809_ICount-=3;   break;
            case 0x84:
                ea = m6809.x & 0xFFFF;
                break;
            /*TODO*///	case 0x85: EA=X+SIGNED(B);										m6809_ICount-=1;   break;
/*TODO*///	case 0x86: EA=X+SIGNED(A);										m6809_ICount-=1;   break;
/*TODO*///	case 0x87: EA=0;																   break; /*   ILLEGAL*/
/*TODO*///	case 0x88: IMMBYTE(EA); 	EA=X+SIGNED(EA);					m6809_ICount-=1;   break; /* this is a hack to make Vectrex work. It should be m6809_ICount-=1. Dunno where the cycle was lost :( */
/*TODO*///	case 0x89: IMMWORD(ea); 	EA+=X;								m6809_ICount-=4;   break;
/*TODO*///	case 0x8a: EA=0;																   break; /*   ILLEGAL*/
/*TODO*///	case 0x8b: EA=X+D;												m6809_ICount-=4;   break;
/*TODO*///	case 0x8c: IMMBYTE(EA); 	EA=PC+SIGNED(EA);					m6809_ICount-=1;   break;
/*TODO*///	case 0x8d: IMMWORD(ea); 	EA+=PC; 							m6809_ICount-=5;   break;
/*TODO*///	case 0x8e: EA=0;																   break; /*   ILLEGAL*/
/*TODO*///	case 0x8f: IMMWORD(ea); 										m6809_ICount-=5;   break;
/*TODO*///
/*TODO*///	case 0x90: EA=X;	X++;						EAD=RM16(EAD);	m6809_ICount-=5;   break; /* Indirect ,R+ not in my specs */
/*TODO*///	case 0x91: EA=X;	X+=2;						EAD=RM16(EAD);	m6809_ICount-=6;   break;
/*TODO*///	case 0x92: X--; 	EA=X;						EAD=RM16(EAD);	m6809_ICount-=5;   break;
/*TODO*///	case 0x93: X-=2;	EA=X;						EAD=RM16(EAD);	m6809_ICount-=6;   break;
/*TODO*///	case 0x94: EA=X;								EAD=RM16(EAD);	m6809_ICount-=3;   break;
/*TODO*///	case 0x95: EA=X+SIGNED(B);						EAD=RM16(EAD);	m6809_ICount-=4;   break;
/*TODO*///	case 0x96: EA=X+SIGNED(A);						EAD=RM16(EAD);	m6809_ICount-=4;   break;
/*TODO*///	case 0x97: EA=0;																   break; /*   ILLEGAL*/
/*TODO*///	case 0x98: IMMBYTE(EA); 	EA=X+SIGNED(EA);	EAD=RM16(EAD);	m6809_ICount-=4;   break;
/*TODO*///	case 0x99: IMMWORD(ea); 	EA+=X;				EAD=RM16(EAD);	m6809_ICount-=7;   break;
/*TODO*///	case 0x9a: EA=0;																   break; /*   ILLEGAL*/
/*TODO*///	case 0x9b: EA=X+D;								EAD=RM16(EAD);	m6809_ICount-=7;   break;
/*TODO*///	case 0x9c: IMMBYTE(EA); 	EA=PC+SIGNED(EA);	EAD=RM16(EAD);	m6809_ICount-=4;   break;
/*TODO*///	case 0x9d: IMMWORD(ea); 	EA+=PC; 			EAD=RM16(EAD);	m6809_ICount-=8;   break;
/*TODO*///	case 0x9e: EA=0;																   break; /*   ILLEGAL*/
/*TODO*///	case 0x9f: IMMWORD(ea); 						EAD=RM16(EAD);	m6809_ICount-=8;   break;
/*TODO*///
/*TODO*///	case 0xa0: EA=Y;	Y++;										m6809_ICount-=2;   break;
/*TODO*///	case 0xa1: EA=Y;	Y+=2;										m6809_ICount-=3;   break;
/*TODO*///	case 0xa2: Y--; 	EA=Y;										m6809_ICount-=2;   break;
/*TODO*///	case 0xa3: Y-=2;	EA=Y;										m6809_ICount-=3;   break;
/*TODO*///	case 0xa4: EA=Y;																   break;
/*TODO*///	case 0xa5: EA=Y+SIGNED(B);										m6809_ICount-=1;   break;
/*TODO*///	case 0xa6: EA=Y+SIGNED(A);										m6809_ICount-=1;   break;
/*TODO*///	case 0xa7: EA=0;																   break; /*   ILLEGAL*/
/*TODO*///	case 0xa8: IMMBYTE(EA); 	EA=Y+SIGNED(EA);					m6809_ICount-=1;   break;
/*TODO*///	case 0xa9: IMMWORD(ea); 	EA+=Y;								m6809_ICount-=4;   break;
/*TODO*///	case 0xaa: EA=0;																   break; /*   ILLEGAL*/
/*TODO*///	case 0xab: EA=Y+D;												m6809_ICount-=4;   break;
/*TODO*///	case 0xac: IMMBYTE(EA); 	EA=PC+SIGNED(EA);					m6809_ICount-=1;   break;
/*TODO*///	case 0xad: IMMWORD(ea); 	EA+=PC; 							m6809_ICount-=5;   break;
/*TODO*///	case 0xae: EA=0;																   break; /*   ILLEGAL*/
/*TODO*///	case 0xaf: IMMWORD(ea); 										m6809_ICount-=5;   break;
/*TODO*///
/*TODO*///	case 0xb0: EA=Y;	Y++;						EAD=RM16(EAD);	m6809_ICount-=5;   break;
/*TODO*///	case 0xb1: EA=Y;	Y+=2;						EAD=RM16(EAD);	m6809_ICount-=6;   break;
/*TODO*///	case 0xb2: Y--; 	EA=Y;						EAD=RM16(EAD);	m6809_ICount-=5;   break;
/*TODO*///	case 0xb3: Y-=2;	EA=Y;						EAD=RM16(EAD);	m6809_ICount-=6;   break;
/*TODO*///	case 0xb4: EA=Y;								EAD=RM16(EAD);	m6809_ICount-=3;   break;
/*TODO*///	case 0xb5: EA=Y+SIGNED(B);						EAD=RM16(EAD);	m6809_ICount-=4;   break;
/*TODO*///	case 0xb6: EA=Y+SIGNED(A);						EAD=RM16(EAD);	m6809_ICount-=4;   break;
/*TODO*///	case 0xb7: EA=0;																   break; /*   ILLEGAL*/
/*TODO*///	case 0xb8: IMMBYTE(EA); 	EA=Y+SIGNED(EA);	EAD=RM16(EAD);	m6809_ICount-=4;   break;
/*TODO*///	case 0xb9: IMMWORD(ea); 	EA+=Y;				EAD=RM16(EAD);	m6809_ICount-=7;   break;
/*TODO*///	case 0xba: EA=0;																   break; /*   ILLEGAL*/
/*TODO*///	case 0xbb: EA=Y+D;								EAD=RM16(EAD);	m6809_ICount-=7;   break;
/*TODO*///	case 0xbc: IMMBYTE(EA); 	EA=PC+SIGNED(EA);	EAD=RM16(EAD);	m6809_ICount-=4;   break;
/*TODO*///	case 0xbd: IMMWORD(ea); 	EA+=PC; 			EAD=RM16(EAD);	m6809_ICount-=8;   break;
/*TODO*///	case 0xbe: EA=0;																   break; /*   ILLEGAL*/
/*TODO*///	case 0xbf: IMMWORD(ea); 						EAD=RM16(EAD);	m6809_ICount-=8;   break;
/*TODO*///
            case 0xc0:
                ea = m6809.u & 0xFFFF;
                m6809.u = (m6809.u + 1) & 0xFFFF;
                m6809_ICount[0] -= 2;
                break;
            /*TODO*///	case 0xc1: EA=U;			U+=2;								m6809_ICount-=3;   break;
/*TODO*///	case 0xc2: U--; 			EA=U;								m6809_ICount-=2;   break;
/*TODO*///	case 0xc3: U-=2;			EA=U;								m6809_ICount-=3;   break;
            case 0xc4:
                ea = m6809.u & 0xFFFF;
                break;
            /*TODO*///	case 0xc5: EA=U+SIGNED(B);										m6809_ICount-=1;   break;
/*TODO*///	case 0xc6: EA=U+SIGNED(A);										m6809_ICount-=1;   break;
/*TODO*///	case 0xc7: EA=0;																   break; /*ILLEGAL*/
/*TODO*///	case 0xc8: IMMBYTE(EA); 	EA=U+SIGNED(EA);					m6809_ICount-=1;   break;
/*TODO*///	case 0xc9: IMMWORD(ea); 	EA+=U;								m6809_ICount-=4;   break;
/*TODO*///	case 0xca: EA=0;																   break; /*ILLEGAL*/
/*TODO*///	case 0xcb: EA=U+D;												m6809_ICount-=4;   break;
/*TODO*///	case 0xcc: IMMBYTE(EA); 	EA=PC+SIGNED(EA);					m6809_ICount-=1;   break;
/*TODO*///	case 0xcd: IMMWORD(ea); 	EA+=PC; 							m6809_ICount-=5;   break;
/*TODO*///	case 0xce: EA=0;																   break; /*ILLEGAL*/
/*TODO*///	case 0xcf: IMMWORD(ea); 										m6809_ICount-=5;   break;
/*TODO*///
/*TODO*///	case 0xd0: EA=U;	U++;						EAD=RM16(EAD);	m6809_ICount-=5;   break;
/*TODO*///	case 0xd1: EA=U;	U+=2;						EAD=RM16(EAD);	m6809_ICount-=6;   break;
/*TODO*///	case 0xd2: U--; 	EA=U;						EAD=RM16(EAD);	m6809_ICount-=5;   break;
/*TODO*///	case 0xd3: U-=2;	EA=U;						EAD=RM16(EAD);	m6809_ICount-=6;   break;
/*TODO*///	case 0xd4: EA=U;								EAD=RM16(EAD);	m6809_ICount-=3;   break;
/*TODO*///	case 0xd5: EA=U+SIGNED(B);						EAD=RM16(EAD);	m6809_ICount-=4;   break;
/*TODO*///	case 0xd6: EA=U+SIGNED(A);						EAD=RM16(EAD);	m6809_ICount-=4;   break;
/*TODO*///	case 0xd7: EA=0;																   break; /*ILLEGAL*/
/*TODO*///	case 0xd8: IMMBYTE(EA); 	EA=U+SIGNED(EA);	EAD=RM16(EAD);	m6809_ICount-=4;   break;
/*TODO*///	case 0xd9: IMMWORD(ea); 	EA+=U;				EAD=RM16(EAD);	m6809_ICount-=7;   break;
/*TODO*///	case 0xda: EA=0;																   break; /*ILLEGAL*/
/*TODO*///	case 0xdb: EA=U+D;								EAD=RM16(EAD);	m6809_ICount-=7;   break;
/*TODO*///	case 0xdc: IMMBYTE(EA); 	EA=PC+SIGNED(EA);	EAD=RM16(EAD);	m6809_ICount-=4;   break;
/*TODO*///	case 0xdd: IMMWORD(ea); 	EA+=PC; 			EAD=RM16(EAD);	m6809_ICount-=8;   break;
/*TODO*///	case 0xde: EA=0;																   break; /*ILLEGAL*/
/*TODO*///	case 0xdf: IMMWORD(ea); 						EAD=RM16(EAD);	m6809_ICount-=8;   break;
/*TODO*///
/*TODO*///	case 0xe0: EA=S;	S++;										m6809_ICount-=2;   break;
/*TODO*///	case 0xe1: EA=S;	S+=2;										m6809_ICount-=3;   break;
/*TODO*///	case 0xe2: S--; 	EA=S;										m6809_ICount-=2;   break;
/*TODO*///	case 0xe3: S-=2;	EA=S;										m6809_ICount-=3;   break;
/*TODO*///	case 0xe4: EA=S;																   break;
/*TODO*///	case 0xe5: EA=S+SIGNED(B);										m6809_ICount-=1;   break;
/*TODO*///	case 0xe6: EA=S+SIGNED(A);										m6809_ICount-=1;   break;
/*TODO*///	case 0xe7: EA=0;																   break; /*ILLEGAL*/
/*TODO*///	case 0xe8: IMMBYTE(EA); 	EA=S+SIGNED(EA);					m6809_ICount-=1;   break;
/*TODO*///	case 0xe9: IMMWORD(ea); 	EA+=S;								m6809_ICount-=4;   break;
/*TODO*///	case 0xea: EA=0;																   break; /*ILLEGAL*/
/*TODO*///	case 0xeb: EA=S+D;												m6809_ICount-=4;   break;
/*TODO*///	case 0xec: IMMBYTE(EA); 	EA=PC+SIGNED(EA);					m6809_ICount-=1;   break;
/*TODO*///	case 0xed: IMMWORD(ea); 	EA+=PC; 							m6809_ICount-=5;   break;
/*TODO*///	case 0xee: EA=0;																   break;  /*ILLEGAL*/
/*TODO*///	case 0xef: IMMWORD(ea); 										m6809_ICount-=5;   break;
/*TODO*///
/*TODO*///	case 0xf0: EA=S;	S++;						EAD=RM16(EAD);	m6809_ICount-=5;   break;
/*TODO*///	case 0xf1: EA=S;	S+=2;						EAD=RM16(EAD);	m6809_ICount-=6;   break;
/*TODO*///	case 0xf2: S--; 	EA=S;						EAD=RM16(EAD);	m6809_ICount-=5;   break;
/*TODO*///	case 0xf3: S-=2;	EA=S;						EAD=RM16(EAD);	m6809_ICount-=6;   break;
/*TODO*///	case 0xf4: EA=S;								EAD=RM16(EAD);	m6809_ICount-=3;   break;
/*TODO*///	case 0xf5: EA=S+SIGNED(B);						EAD=RM16(EAD);	m6809_ICount-=4;   break;
/*TODO*///	case 0xf6: EA=S+SIGNED(A);						EAD=RM16(EAD);	m6809_ICount-=4;   break;
/*TODO*///	case 0xf7: EA=0;																   break; /*ILLEGAL*/
/*TODO*///	case 0xf8: IMMBYTE(EA); 	EA=S+SIGNED(EA);	EAD=RM16(EAD);	m6809_ICount-=4;   break;
/*TODO*///	case 0xf9: IMMWORD(ea); 	EA+=S;				EAD=RM16(EAD);	m6809_ICount-=7;   break;
/*TODO*///	case 0xfa: EA=0;																   break; /*ILLEGAL*/
/*TODO*///	case 0xfb: EA=S+D;								EAD=RM16(EAD);	m6809_ICount-=7;   break;
/*TODO*///	case 0xfc: IMMBYTE(EA); 	EA=PC+SIGNED(EA);	EAD=RM16(EAD);	m6809_ICount-=4;   break;
/*TODO*///	case 0xfd: IMMWORD(ea); 	EA+=PC; 			EAD=RM16(EAD);	m6809_ICount-=8;   break;
/*TODO*///	case 0xfe: EA=0;																   break; /*ILLEGAL*/
/*TODO*///	case 0xff: IMMWORD(ea); 						EAD=RM16(EAD);	m6809_ICount-=8;   break;
            default://TODO to be removed
                System.out.println("6809 effective address : 0x" + Integer.toHexString(postbyte));
                throw new UnsupportedOperationException("Unsupported");
        }
    }

    /* 
     *
     * arcadeflex functions
     */
    @Override
    public Object init_context() {
        Object reg = new m6809_Regs();
        return reg;
    }

    @Override
    public int[] get_cycle_table(int which) {
        return null;//doesn't exist in 6809 cpu
    }

    @Override
    public void set_cycle_table(int which, int[] new_table) {
        //doesn't exist in 6809 cpu
    }

    @Override
    public void internal_interrupt(int type) {
        //doesn't exist in 6809 cpu
    }

    @Override
    public int memory_read(int offset) {
        return cpu_readmem16(offset);
    }

    @Override
    public void memory_write(int offset, int data) {
        cpu_writemem16(offset, data);
    }

    @Override
    public int internal_read(int offset) {
        return 0;//doesn't exist in 6809 cpu
    }

    @Override
    public void internal_write(int offset, int data) {
        //doesn't exist in 6809 cpu
    }

    @Override
    public void set_op_base(int pc) {
        cpu_setOPbase16.handler(pc);
    }

    @Override
    public int mem_address_bits_of_cpu() {
        return 16;
    }

}
