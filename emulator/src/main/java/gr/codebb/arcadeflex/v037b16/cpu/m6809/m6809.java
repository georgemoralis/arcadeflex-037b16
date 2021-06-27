/*
 * ported to v0.37b16
 *
 */
package gr.codebb.arcadeflex.v037b16.cpu.m6809;

//cpu imports
import static gr.codebb.arcadeflex.v037b16.cpu.m6809.m6809H.*;
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
    private static m6809_Regs m6809 = new m6809_Regs();

    public int ea;

    public void CHANGE_PC() {
        change_pc16(m6809.pc & 0xFFFF);//ensure it's 16bit just in case
    }

    public static final int M6809_CWAI = 8;/* set when CWAI is waiting for an interrupt */
    public static final int M6809_SYNC = 16;/* set when SYNC is waiting for an interrupt */
    public static final int M6809_LDS = 32;/* set when LDS occured at least once */

    public void CHECK_IRQ_LINES() {
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

    static int RM16(int addr) {
        int i = RM(addr + 1 & 0xFFFF);
        i |= RM(addr) << 8;
        return i & 0xFFFF;
    }

    static void WM16(int addr, int reg) {
        WM(addr + 1 & 0xFFFF, reg & 0xFF);
        WM(addr, reg >> 8);
    }

    /* macros to access memory */
    public static int IMMBYTE() {
        int reg = ROP_ARG(m6809.pc);
        m6809.pc = (m6809.pc + 1) & 0xFFFF;
        return reg & 0xFF;//insure it returns a 8bit value
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

    @Override
    public void init() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public void set_irq_callback(irqcallbacksPtr callback) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void internal_interrupt(int type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String cpu_info(Object context, int regnum) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
