/*
 * ported to v0.37b16
 *
 */
package gr.codebb.arcadeflex.v037b16.cpu.z80;
//cpu imports
import static gr.codebb.arcadeflex.v037b16.cpu.z80.z80H.*;
import static gr.codebb.arcadeflex.v037b16.cpu.z80.z80opcodes.*;
//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b16.mame.memory.*;
import static gr.codebb.arcadeflex.v037b16.mame.memoryH.*;

public class z80 extends cpu_interface {

    public static int[] z80_ICount = new int[1];

    public z80() {
        cpu_num = CPU_Z80;
        num_irqs = 1;
        default_vector = 255;
        overclock = 1.0;
        no_int = Z80_IGNORE_INT;
        irq_int = Z80_IRQ_INT;
        nmi_int = Z80_NMI_INT;
        databus_width = 8;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_LE;
        align_unit = 1;
        max_inst_len = 4;
        icount = z80_ICount;
        pgm_memory_base = 0;
        //intialize interfaces
        burn = burn_function;
    }

    /**
     * *************************************************************************
     *
     * The Z80 registers. HALT is set to 1 when the CPU is halted, the refresh
     * register is calculated as follows: refresh=(Regs.R&127)|(Regs.R2&128) /**
     * *************************************************************************
     */
    public static class Z80_Regs {

        public int PREPC, PC, SP, A, F, B, C, D, E, H, L, IX, IY;
        public int A2, F2, B2, C2, D2, E2, H2, L2;
        public int WZ; //MEMPTR, esoteric register of the ZiLOG Z80 CPU
        public int R, R2, IFF1, IFF2, HALT, IM, I;
        public int irq_max;/* number of daisy chain devices        */
        public int request_irq;/* daisy chain next request device		*/
        public int service_irq;/* daisy chain next reti handling device */
        public int nmi_state;/* nmi line state */
        public int irq_state;/* irq line state */
        public int[] int_state = new int[Z80_MAXDAISY];
        public Z80_DaisyChain[] irq = new Z80_DaisyChain[Z80_MAXDAISY];
        public irqcallbacksPtr irq_callback;
        public int extra_cycles;/* extra cycles for interrupts */
    }

    public static int AF() {
        return ((Z80.A << 8) | Z80.F) & 0xFFFF;
    }

    public static int AF2() {
        return ((Z80.A2 << 8) | Z80.F2) & 0xFFFF;
    }

    public static int BC() {
        return ((Z80.B << 8) | Z80.C) & 0xFFFF;
    }

    public static int BC2() {
        return ((Z80.B2 << 8) | Z80.C2) & 0xFFFF;
    }

    public static int DE() {
        return ((Z80.D << 8) | Z80.E) & 0xFFFF;
    }

    public static int DE2() {
        return ((Z80.D2 << 8) | Z80.E2) & 0xFFFF;
    }

    public static int HL() {
        return ((Z80.H << 8) | Z80.L) & 0xFFFF;
    }

    public static int HL2() {
        return ((Z80.H2 << 8) | Z80.L2) & 0xFFFF;
    }

    public static void AF(int nn) {
        Z80.A = (nn >> 8) & 0xff;
        Z80.F = nn & 0xff;
    }

    public static void AF2(int nn) {
        Z80.A2 = (nn >> 8) & 0xff;
        Z80.F2 = nn & 0xff;
    }

    public static void BC(int nn) {
        Z80.B = (nn >> 8) & 0xff;
        Z80.C = nn & 0xff;
    }

    public static void BC2(int nn) {
        Z80.B2 = (nn >> 8) & 0xff;
        Z80.C2 = nn & 0xff;
    }

    public static void DE(int nn) {
        Z80.D = (nn >> 8) & 0xff;
        Z80.E = nn & 0xff;
    }

    public static void DE2(int nn) {
        Z80.D2 = (nn >> 8) & 0xff;
        Z80.E2 = nn & 0xff;
    }

    public static void HL(int nn) {
        Z80.H = (nn >> 8) & 0xff;
        Z80.L = nn & 0xff;
    }

    public static void HL2(int nn) {
        Z80.H2 = (nn >> 8) & 0xff;
        Z80.L2 = nn & 0xff;
    }

    public static final int CF = 0x01;
    public static final int NF = 0x02;
    public static final int PF = 0x04;
    public static final int VF = PF;
    public static final int XF = 0x08;
    public static final int HF = 0x10;
    public static final int YF = 0x20;
    public static final int ZF = 0x40;
    public static final int SF = 0x80;

    public static final int INT_IRQ = 0x01;
    public static final int NMI_IRQ = 0x02;

    static Z80_Regs Z80 = new Z80_Regs();
    static int/*UINT32*/ EA;
    static int after_EI = 0;

    public static int SZ[] = new int[256];
    /* zero and sign flags */
    public static int SZ_BIT[] = new int[256];
    /* zero, sign and parity/overflow (=zero) flags for BIT opcode */
    public static int SZP[] = new int[256];
    /* zero, sign and parity flags */
    public static int SZHV_inc[] = new int[256];
    /* zero, sign, half carry and overflow flags INC r8 */
    public static int SZHV_dec[] = new int[256];
    /* zero, sign, half carry and overflow flags DEC r8 */

    public static int SZHVC_Add[] = new int[2 * 256 * 256];
    public static int SZHVC_sub[] = new int[2 * 256 * 256];

    /* tmp1 value for ini/inir/outi/otir for [C.1-0][io.1-0] */
    static int irep_tmp1[][] = {
        {0, 0, 1, 0}, {0, 1, 0, 1}, {1, 0, 1, 1}, {0, 1, 1, 0}
    };

    /* tmp1 value for ind/indr/outd/otdr for [C.1-0][io.1-0] */
    static int drep_tmp1[][] = {
        {0, 1, 0, 0}, {1, 0, 0, 1}, {0, 0, 1, 0}, {0, 1, 0, 1}
    };

    /* tmp2 value for all in/out repeated opcodes for B.7-0 */
    static int breg_tmp2[] = {
        0, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1,
        0, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0,
        1, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0,
        1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1,
        0, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0,
        1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1,
        0, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1,
        0, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0,
        1, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0,
        1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1,
        0, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1,
        0, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0,
        1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1,
        0, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0,
        1, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0,
        1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1
    };

    static int cc_op[] = {
        4, 10, 7, 6, 4, 4, 7, 4, 4, 11, 7, 6, 4, 4, 7, 4,
        8, 10, 7, 6, 4, 4, 7, 4, 12, 11, 7, 6, 4, 4, 7, 4,
        7, 10, 16, 6, 4, 4, 7, 4, 7, 11, 16, 6, 4, 4, 7, 4,
        7, 10, 13, 6, 11, 11, 10, 4, 7, 11, 13, 6, 4, 4, 7, 4,
        4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
        4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
        4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
        7, 7, 7, 7, 7, 7, 4, 7, 4, 4, 4, 4, 4, 4, 7, 4,
        4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
        4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
        4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
        4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
        5, 10, 10, 10, 10, 11, 7, 11, 5, 10, 10, 0, 10, 17, 7, 11,
        5, 10, 10, 11, 10, 11, 7, 11, 5, 4, 10, 11, 10, 0, 7, 11,
        5, 10, 10, 19, 10, 11, 7, 11, 5, 4, 10, 4, 10, 0, 7, 11,
        5, 10, 10, 4, 10, 11, 7, 11, 5, 6, 10, 4, 10, 0, 7, 11};

    static int cc_cb[] = {
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8,
        8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8,
        8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8,
        8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8};

    static int cc_ed[] = {
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        12, 12, 15, 20, 8, 8, 8, 9, 12, 12, 15, 20, 8, 8, 8, 9,
        12, 12, 15, 20, 8, 8, 8, 9, 12, 12, 15, 20, 8, 8, 8, 9,
        12, 12, 15, 20, 8, 8, 8, 18, 12, 12, 15, 20, 8, 8, 8, 18,
        12, 12, 15, 20, 8, 8, 8, 8, 12, 12, 15, 20, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        16, 16, 16, 16, 8, 8, 8, 8, 16, 16, 16, 16, 8, 8, 8, 8,
        16, 16, 16, 16, 8, 8, 8, 8, 16, 16, 16, 16, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8};

    static int cc_xy[] = {
        4, 4, 4, 4, 4, 4, 4, 4, 4, 15, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 15, 4, 4, 4, 4, 4, 4,
        4, 14, 20, 10, 9, 9, 9, 4, 4, 15, 20, 10, 9, 9, 9, 4,
        4, 4, 4, 4, 23, 23, 19, 4, 4, 15, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 9, 9, 19, 4, 4, 4, 4, 4, 9, 9, 19, 4,
        4, 4, 4, 4, 9, 9, 19, 4, 4, 4, 4, 4, 9, 9, 19, 4,
        9, 9, 9, 9, 9, 9, 19, 9, 9, 9, 9, 9, 9, 9, 19, 9,
        19, 19, 19, 19, 19, 19, 4, 19, 4, 4, 4, 4, 9, 9, 19, 4,
        4, 4, 4, 4, 9, 9, 19, 4, 4, 4, 4, 4, 9, 9, 19, 4,
        4, 4, 4, 4, 9, 9, 19, 4, 4, 4, 4, 4, 9, 9, 19, 4,
        4, 4, 4, 4, 9, 9, 19, 4, 4, 4, 4, 4, 9, 9, 19, 4,
        4, 4, 4, 4, 9, 9, 19, 4, 4, 4, 4, 4, 9, 9, 19, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 0, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 14, 4, 23, 4, 15, 4, 4, 4, 8, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 10, 4, 4, 4, 4, 4, 4};

    static int cc_xycb[] = {
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
        20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
        20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
        20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23};

    /* extra cycles if jr/jp/call taken and 'interrupt latency' on rst 0-7 */
    static int cc_ex[] = {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, /* DJNZ */
        5, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, /* JR NZ/JR Z */
        5, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, /* JR NC/JR C */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        5, 5, 5, 5, 0, 0, 0, 0, 5, 5, 5, 5, 0, 0, 0, 0, /* LDIR/CPIR/INIR/OTIR LDDR/CPDR/INDR/OTDR */
        6, 0, 0, 0, 7, 0, 0, 2, 6, 0, 0, 0, 7, 0, 0, 2,
        6, 0, 0, 0, 7, 0, 0, 2, 6, 0, 0, 0, 7, 0, 0, 2,
        6, 0, 0, 0, 7, 0, 0, 2, 6, 0, 0, 0, 7, 0, 0, 2,
        6, 0, 0, 0, 7, 0, 0, 2, 6, 0, 0, 0, 7, 0, 0, 2};

    static int cc[][] = {cc_op, cc_cb, cc_ed, cc_xy, cc_xycb, cc_ex};

    /**
     * *************************************************************************
     * Burn an odd amount of cycles, that is instructions taking something
     * different from 4 T-states per opcode (and R increment)
     * *************************************************************************
     */
    public static void BURNODD(int cycles, int opcodes, int cyclesum) {
        if (cycles > 0) {
            Z80.R = (Z80.R + (cycles / cyclesum) * opcodes) & 0xFF;
            z80_ICount[0] -= (cycles / cyclesum) * cyclesum;
        }
    }

    /**
     * *************************************************************
     * Enter HALT state; write 1 to fake port on first execution
     * *************************************************************
     */
    public static void ENTER_HALT() {
        Z80.PC = (Z80.PC - 1) & 0xFFFF;
        Z80.HALT = 1;
        if (after_EI == 0) {
            burn.handler(z80_ICount[0]);
        }
    }

    /**
     * *************************************************************
     * Leave HALT state; write 0 to fake port
     * *************************************************************
     */
    public static void LEAVE_HALT() {
        if (Z80.HALT != 0) {
            Z80.HALT = 0;
            Z80.PC = (Z80.PC + 1) & 0xFFFF;
        }
    }

    /**
     * *************************************************************
     * Input a byte from given I/O port
     * *************************************************************
     */
    public static int IN(int port) {
        return cpu_readport16(port) & 0xff;
    }

    /**
     * *************************************************************
     * Output a byte to given I/O port
     * *************************************************************
     */
    public static void OUT(int port, int value) {
        cpu_writeport16(port, value & 0xFF);
    }

    /**
     * *************************************************************
     * Read a byte from given memory location
     * *************************************************************
     */
    public static int RM(int addr) {
        return cpu_readmem16(addr) & 0xFF;
    }

    /**
     * *************************************************************
     * Read a word from given memory location
     * *************************************************************
     */
    public static int RM16(int addr) {
        return (RM(addr) | (RM((addr + 1) & 0xffff) << 8)) & 0xFFFF;
    }

    /**
     * *************************************************************
     * Write a byte to given memory location
     * *************************************************************
     */
    public static void WM(int addr, int value) {
        cpu_writemem16(addr, value & 0xFF);
    }

    /**
     * *************************************************************
     * Write a word to given memory location
     * *************************************************************
     */
    public static void WM16(int address, int data) {
        WM(address, data & 0xFF);
        WM((address + 1) & 0xffff, data >> 8);
    }

    /**
     * *************************************************************
     * ROP() is identical to RM() except it is used for reading opcodes. In case
     * of system with memory mapped I/O, this function can be used to greatly
     * speed up emulation
     * *************************************************************
     */
    public static /*UINT8*/ int ROP() {
        int pc = Z80.PC & 0xFFFF;
        Z80.PC = (Z80.PC + 1) & 0xFFFF;
        return cpu_readop(pc) & 0xFF;
    }

    /**
     * **************************************************************
     * ARG() is identical to ROP() except it is used for reading opcode
     * arguments. This difference can be used to support systems that use
     * different encoding mechanisms for opcodes and opcode arguments
     * *************************************************************
     */
    public static /*UINT8*/ int ARG() {
        int pc = Z80.PC & 0xFFFF;
        Z80.PC = (Z80.PC + 1 & 0xFFFF);
        return cpu_readop_arg(pc) & 0xFF;
    }

    public static int /*UINT32*/ ARG16() {
        int pc = Z80.PC & 0xFFFF;
        Z80.PC = (Z80.PC + 2) & 0xFFFF;
        return (cpu_readop_arg(pc) | (cpu_readop_arg((pc + 1) & 0xffff) << 8)) & 0xFFFF;
    }

    /**
     * *************************************************************
     * Calculate the effective address EA of an opcode using IX+offset resp.
     * IY+offset addressing.
     * *************************************************************
     */
    public static void EAX() {
        EA = (Z80.IX + (byte) ARG()) & 0xffff;
        Z80.WZ = EA;
    }

    public static void EAY() {
        EA = (Z80.IY + (byte) ARG()) & 0xffff;
        Z80.WZ = EA;
    }

    /**
     * *************************************************************
     * POP *************************************************************
     */
    public static int POP() {
        int nn = RM16(Z80.SP);//RM16( _SPD, &Z80.DR );
        Z80.SP = (Z80.SP + 2) & 0xffff;
        return nn;
    }

    /**
     * *************************************************************
     * PUSH *************************************************************
     */
    public static void PUSH(int nn) {
        Z80.SP = (Z80.SP - 2) & 0xffff;
        WM16(Z80.SP, nn);
    }

    /**
     * *************************************************************
     * JP *************************************************************
     */
    public static void JP() {
        Z80.PC = ARG16();
        Z80.WZ = Z80.PC;
        change_pc16(Z80.PC);
    }

    /**
     * *************************************************************
     * JP_COND *************************************************************
     */
    public static void JP_COND(boolean cond) {
        if (cond) {
            Z80.PC = ARG16();
            Z80.WZ = Z80.PC;
            change_pc16(Z80.PC);
        } else {
            Z80.WZ = ARG16();
            /* implicit do PC += 2 */
        }
    }

    /**
     * *************************************************************
     * JR *************************************************************
     */
    public static void JR() {
        byte arg = (byte) ARG();
        Z80.PC = (Z80.PC + arg) & 0xFFFF;
        Z80.WZ = Z80.PC;
        change_pc16(Z80.PC);
    }

    /*TODO*///#define JR()													\
/*TODO*///{																\
/*TODO*///	unsigned oldpc = _PCD-1;									\
/*TODO*///	INT8 arg = (INT8)ARG(); /* ARG() also increments _PC */ 	\
/*TODO*///	_PC += arg; 			/* so don't do _PC += ARG() */      \
/*TODO*///	change_pc16(_PCD);											\
/*TODO*///    /* speed up busy loop */                                    \
/*TODO*///	if( _PCD == oldpc ) 										\
/*TODO*///	{															\
/*TODO*///		if( !after_EI ) 										\
/*TODO*///			BURNODD( z80_ICount, 1, cc[Z80_TABLE_op][0x18] );	\
/*TODO*///	}															\
/*TODO*///	else														\
/*TODO*///	{															\
/*TODO*///		UINT8 op = cpu_readop(_PCD);							\
/*TODO*///		if( _PCD == oldpc-1 )									\
/*TODO*///		{														\
/*TODO*///			/* NOP - JR $-1 or EI - JR $-1 */					\
/*TODO*///			if ( op == 0x00 || op == 0xfb ) 					\
/*TODO*///			{													\
/*TODO*///				if( !after_EI ) 								\
/*TODO*///				   BURNODD( z80_ICount-cc[Z80_TABLE_op][0x00],	\
/*TODO*///					   2, cc[Z80_TABLE_op][0x00]+cc[Z80_TABLE_op][0x18]); \
/*TODO*///			}													\
/*TODO*///		}														\
/*TODO*///		else													\
/*TODO*///		/* LD SP,#xxxx - JR $-3 */								\
/*TODO*///		if( _PCD == oldpc-3 && op == 0x31 ) 					\
/*TODO*///		{														\
/*TODO*///			if( !after_EI ) 									\
/*TODO*///			   BURNODD( z80_ICount-cc[Z80_TABLE_op][0x31],		\
/*TODO*///				   2, cc[Z80_TABLE_op][0x31]+cc[Z80_TABLE_op][0x18]); \
/*TODO*///		}														\
/*TODO*///    }                                                           \
/*TODO*///}
    /**
     * *************************************************************
     * JR_COND *************************************************************
     */
    public static void JR_COND(boolean cond, int opcode) {
        if (cond) {
            byte arg = (byte) ARG();
            Z80.PC = (Z80.PC + arg) & 0xFFFF;
            z80_ICount[0] -= cc[Z80_TABLE_ex][opcode];//CC(ex,opcode);											
            change_pc16(Z80.PC);
        } else {
            Z80.PC = (Z80.PC + 1) & 0xFFFF;
        }
    }

    /**
     * *************************************************************
     * CALL *************************************************************
     */
    public static void CALL() {
        EA = ARG16();
        Z80.WZ = EA;
        PUSH(Z80.PC);
        Z80.PC = EA;
        change_pc16(Z80.PC);
    }

    /**
     * *************************************************************
     * CALL_COND *************************************************************
     */
    public static void CALL_COND(boolean cond, int opcode) {
        if (cond) {
            EA = ARG16();
            Z80.WZ = EA;
            PUSH(Z80.PC);
            Z80.PC = EA;
            z80_ICount[0] -= cc[Z80_TABLE_ex][opcode];
            change_pc16(Z80.PC);
        } else {
            Z80.WZ = ARG16();
            /* implicit call PC+=2; */
        }
    }

    /**
     * *************************************************************
     * RET_COND *************************************************************
     */
    public static void RET_COND(boolean cond, int opcode) {
        if (cond) {
            Z80.PC = POP();
            Z80.WZ = Z80.PC;
            change_pc16(Z80.PC);
            z80_ICount[0] -= cc[Z80_TABLE_ex][opcode];
        }
    }

    /**
     * *************************************************************
     * RETN / **************************************************************
     */
    public static void RETN() {
        //LOG(("Z80 #%d RETN IFF1:%d IFF2:%d\n", cpu_getactivecpu(), _IFF1, _IFF2)); 
        Z80.PC = POP();
        Z80.WZ = Z80.PC;
        change_pc16(Z80.PC);
        if (Z80.IFF1 == 0 && Z80.IFF2 == 1) {
            Z80.IFF1 = 1;
            if (Z80.irq_state != CLEAR_LINE || Z80.request_irq >= 0) {
                //LOG(("Z80 #%d RETN takes IRQ\n",cpu_getactivecpu()));							
                take_interrupt();
            }
        } else {
            Z80.IFF1 = Z80.IFF2;
        }
    }

    /**
     * *************************************************************
     * RETI *************************************************************
     */
    public static void RETI() {
        int device = Z80.service_irq;
        Z80.PC = POP();
        Z80.WZ = Z80.PC;
        change_pc16(Z80.PC);
        /* according to http://www.msxnet.org/tech/Z80/z80undoc.txt */
 /*	_IFF1 = _IFF2;	*/
        if (device >= 0) {
            //LOG(("Z80 #%d RETI device %d: $%02x\n",cpu_getactivecpu(), device, Z80.irq[device].irq_param)); 
            Z80.irq[device].interrupt_reti.handler(Z80.irq[device].irq_param);
        }
    }

    /**
     * *************************************************************
     * LD	R,A *************************************************************
     */
    public static void LD_R_A() {
        Z80.R = Z80.A & 0xFF;
        Z80.R2 = Z80.A & 0x80;
        /* keep bit 7 of R */
    }

    /**
     * *************************************************************
     * LD	A,R *************************************************************
     */
    public static void LD_A_R() {
        Z80.A = ((Z80.R & 0x7f) | Z80.R2) & 0xFF;
        Z80.F = (Z80.F & CF) | SZ[Z80.A] | (Z80.IFF2 << 2);
    }

    /**
     * *************************************************************
     * LD	I,A *************************************************************
     */
    public static void LD_I_A() {
        Z80.I = Z80.A & 0xFF;
    }

    /**
     * *************************************************************
     * LD	A,I *************************************************************
     */
    public static void LD_A_I() {
        Z80.A = Z80.I & 0xFF;
        Z80.F = (Z80.F & CF) | SZ[Z80.A] | (Z80.IFF2 << 2);
    }

    /**
     * *************************************************************
     * RST *************************************************************
     */
    public static void RST(int addr) {
        PUSH(Z80.PC);
        Z80.PC = addr & 0xFFFF;
        Z80.WZ = Z80.PC;
        change_pc16(Z80.PC);
    }

    /**
     * *************************************************************
     * INC	r8 *************************************************************
     */
    public static int INC(int value) {
        value = (value + 1) & 0xFF;
        Z80.F = (Z80.F & CF | SZHV_inc[value]);
        return value;
    }

    /**
     * *************************************************************
     * DEC	r8 *************************************************************
     */
    public static int DEC(int value) {
        value = (value - 1) & 0xFF;
        Z80.F = (Z80.F & CF | SZHV_dec[value]);
        return value;
    }

    /**
     * *************************************************************
     * RLCA *************************************************************
     */
    public static void RLCA() {
        Z80.A = ((Z80.A << 1) | (Z80.A >> 7)) & 0xFF;
        Z80.F = (Z80.F & (SF | ZF | PF)) | (Z80.A & (YF | XF | CF));
    }

    /**
     * *************************************************************
     * RRCA *************************************************************
     */
    public static void RRCA() {
        Z80.F = (Z80.F & (SF | ZF | PF)) | (Z80.A & CF);
        Z80.A = ((Z80.A >> 1) | (Z80.A << 7)) & 0xFF;
        Z80.F |= (Z80.A & (YF | XF));
    }

    /**
     * *************************************************************
     * RLA *************************************************************
     */
    public static void RLA() {
        int res = (Z80.A << 1 | Z80.F & CF) & 0xFF;
        int c = (Z80.A & 0x80) != 0 ? CF : 0;
        Z80.F = (Z80.F & (SF | ZF | PF)) | c | (res & (YF | XF));
        Z80.A = res;
    }

    /**
     * *************************************************************
     * RRA *************************************************************
     */
    public static void RRA() {
        int res = (Z80.A >> 1 | Z80.F << 7) & 0xFF;
        int c = (Z80.A & 0x1) != 0 ? CF : 0;
        Z80.F = (Z80.F & (SF | ZF | PF)) | c | (res & (YF | XF));
        Z80.A = res;
    }

    /**
     * *************************************************************
     * RRD *************************************************************
     */
    public static void RRD() {
        int n = RM(HL());
        Z80.WZ = (HL() + 1) & 0xFFFF;
        WM(HL(), ((n >> 4) | (Z80.A << 4)) & 0xFF);
        Z80.A = ((Z80.A & 0xf0) | (n & 0x0f)) & 0xFF;
        Z80.F = (Z80.F & CF) | SZP[Z80.A];
    }

    /**
     * *************************************************************
     * RLD *************************************************************
     */
    public static void RLD() {
        int n = RM(HL());
        Z80.WZ = (HL() + 1) & 0xFFFF;
        WM(HL(), ((n << 4) | (Z80.A & 0x0f)) & 0xFF);
        Z80.A = ((Z80.A & 0xf0) | (n >> 4)) & 0xFF;
        Z80.F = (Z80.F & CF) | SZP[Z80.A];
    }

    /**
     * *************************************************************
     * ADD	A,n *************************************************************
     */
    public static void ADD(int value) {
        int res = (Z80.A + value) & 0xFF;
        Z80.F = SZHVC_Add[(Z80.A << 8 | res)];
        Z80.A = res;
    }

    /**
     * *************************************************************
     * ADC	A,n *************************************************************
     */
    public static void ADC(int value) {
        int c = Z80.F & 0x1;
        int result = (Z80.A + value + c) & 0xFF;
        Z80.F = SZHVC_Add[(c << 16 | Z80.A << 8 | result)];
        Z80.A = result;
    }

    /**
     * *************************************************************
     * SUB	n *************************************************************
     */
    public static void SUB(int value) {
        int result = (Z80.A - value) & 0xFF;
        Z80.F = SZHVC_sub[(Z80.A << 8 | result)];
        Z80.A = result;
    }

    /**
     * *************************************************************
     * SBC	A,n *************************************************************
     */
    public static void SBC(int value) {
        int c = Z80.F & 1;
        int result = (Z80.A - value - c) & 0xff;
        Z80.F = SZHVC_sub[(c << 16) | (Z80.A << 8) | result];
        Z80.A = result;
    }

    /**
     * *************************************************************
     * NEG *************************************************************
     */
    public static void NEG() {
        int value = Z80.A & 0xFF;
        Z80.A = 0;
        SUB(value);
    }

    /**
     * *************************************************************
     * DAA *************************************************************
     */
    public static void DAA() {
        int a = Z80.A & 0xFF;
        if ((Z80.F & NF) != 0) {
            if ((Z80.F & HF) != 0 | ((Z80.A & 0xf) > 9)) {
                a = (a - 6) & 0xFF;
            }
            if ((Z80.F & CF) != 0 | (Z80.A > 0x99)) {
                a = (a - 0x60) & 0xFF;
            }
        } else {
            if ((Z80.F & HF) != 0 | ((Z80.A & 0xf) > 9)) {
                a = (a + 6) & 0xFF;
            }
            if ((Z80.F & CF) != 0 | (Z80.A > 0x99)) {
                a = (a + 0x60) & 0xFF;
            }
        }

        Z80.F = (Z80.F & (CF | NF)) | ((Z80.A > 0x99) ? 1 : 0) | ((Z80.A ^ a) & HF) | SZP[a];
        Z80.A = a & 0xFF;
    }

    /**
     * *************************************************************
     * AND	n *************************************************************
     */
    public static void AND(int value) {
        Z80.A = (Z80.A & value) & 0xff;
        Z80.F = SZP[Z80.A] | HF;
    }

    /**
     * *************************************************************
     * OR	n *************************************************************
     */
    public static void OR(int value) {
        Z80.A = (Z80.A | value) & 0xff;
        Z80.F = SZP[Z80.A];
    }

    /**
     * *************************************************************
     * XOR	n *************************************************************
     */
    public static void XOR(int value) {
        Z80.A = (Z80.A ^ value) & 0xff;
        Z80.F = SZP[Z80.A];
    }

    /**
     * *************************************************************
     * CP	n *************************************************************
     */
    public static void CP(int value) {
        int val = value & 0xFF;
        int result = (Z80.A - value) & 0xFF;
        Z80.F = (SZHVC_sub[(Z80.A << 8 | result)] & ~(YF | XF)) | (val & (YF | XF));
    }

    /**
     * *************************************************************
     * EX AF,AF' *************************************************************
     */
    public static void EX_AF() {
        int tmp = Z80.A;
        Z80.A = Z80.A2;
        Z80.A2 = tmp;
        tmp = Z80.F;
        Z80.F = Z80.F2;
        Z80.F2 = tmp;
    }

    /**
     * *************************************************************
     * EX DE,HL *************************************************************
     */
    public static void EX_DE_HL() {
        int tmp = Z80.D;
        Z80.D = Z80.H;
        Z80.H = tmp;
        tmp = Z80.E;
        Z80.E = Z80.L;
        Z80.L = tmp;
    }

    /**
     * *************************************************************
     * EXX *************************************************************
     */
    public static void EXX() {
        int tmp = Z80.B;
        Z80.B = Z80.B2;
        Z80.B2 = tmp;
        tmp = Z80.C;
        Z80.C = Z80.C2;
        Z80.C2 = tmp;
        tmp = Z80.D;
        Z80.D = Z80.D2;
        Z80.D2 = tmp;
        tmp = Z80.E;
        Z80.E = Z80.E2;
        Z80.E2 = tmp;
        tmp = Z80.H;
        Z80.H = Z80.H2;
        Z80.H2 = tmp;
        tmp = Z80.L;
        Z80.L = Z80.L2;
        Z80.L2 = tmp;
    }

    /**
     * *************************************************************
     * EX (SP),r16 *************************************************************
     */
    public static int EXSP(int DR) {
        int tmp = RM16(Z80.SP);
        WM16(Z80.SP, DR);
        Z80.WZ = tmp;
        /*TODO*///recheck
        return tmp;
    }

    /**
     * *************************************************************
     * ADD16 *************************************************************
     */
    public static int ADD16(int a, int b) {
        int result = a + b;
        Z80.WZ = (a + 1) & 0xFFFF;
        Z80.F = (Z80.F & (SF | ZF | VF)) | (((a ^ result ^ b) >> 8) & HF) | ((result >> 16) & CF) | ((result >> 8) & (YF | XF));
        return (result & 0xffff);
    }

    /**
     * *************************************************************
     * ADC	r16,r16 *************************************************************
     */
    public static void ADC16(int value) {
        int _HLD = HL();
        Z80.WZ = (HL() + 1) & 0xFFFF;
        int result = _HLD + value + (Z80.F & CF);
        Z80.F = (((_HLD ^ result ^ value) >> 8) & HF) | ((result >> 16) & CF) | ((result >> 8) & (SF | YF | XF))
                | (((result & 0xffff) != 0) ? 0 : ZF) | (((value ^ _HLD ^ 0x8000) & (value ^ result) & 0x8000) >> 13);
        Z80.H = (result >> 8) & 0xff;
        Z80.L = result & 0xff;
    }

    /**
     * *************************************************************
     * SBC	r16,r16 *************************************************************
     */
    public static void SBC16(int value) {
        int _HLD = HL();
        Z80.WZ = (HL() + 1) & 0xFFFF;
        int result = _HLD - value - (Z80.F & CF);
        Z80.F = (((_HLD ^ result ^ value) >> 8) & HF) | NF | ((result >> 16) & CF) | ((result >> 8) & (SF | YF | XF))
                | (((result & 0xffff) != 0) ? 0 : ZF) | (((value ^ _HLD) & (_HLD ^ result) & 0x8000) >> 13);
        Z80.H = (result >> 8) & 0xff;
        Z80.L = result & 0xff;
    }

    /**
     * *************************************************************
     * RLC	r8 *************************************************************
     */
    public static int RLC(int value) {
        int c = (value & 0x80) >> 7;
        value = (value << 1 | value >> 7) & 0xFF;
        Z80.F = (SZP[value] | c);
        return value;
    }

    /**
     * *************************************************************
     * RRC	r8 *************************************************************
     */
    public static int RRC(int value) {
        int res = value;
        int c = (res & 0x01) != 0 ? CF : 0;
        res = (res >> 1 | res << 7) & 0xFF;
        Z80.F = (SZP[res] | c);
        return res;
    }

    /**
     * *************************************************************
     * RL	r8 *************************************************************
     */
    public static int RL(int value) {
        int c = (value & 0x80) >> 7;
        value = (value << 1 | Z80.F & 0x1) & 0xFF;
        Z80.F = (SZP[value] | c);
        return value;
    }

    /**
     * *************************************************************
     * RR	r8 *************************************************************
     */
    public static int RR(int value) {
        int c = value & 0x1;
        value = (value >> 1 | Z80.F << 7) & 0xFF;
        Z80.F = (SZP[value] | c);
        return value;
    }

    /**
     * *************************************************************
     * SLA	r8 *************************************************************
     */
    public static int SLA(int value) {
        int c = (value & 0x80) >> 7;
        value = value << 1 & 0xFF;
        Z80.F = (SZP[value] | c);
        return value;
    }

    /**
     * *************************************************************
     * SRA	r8 *************************************************************
     */
    public static int SRA(int value) {
        int c = value & 0x1;
        value = value >> 1 | value & 0x80;
        Z80.F = (SZP[value] | c);
        return value;
    }

    /**
     * *************************************************************
     * SLL	r8 *************************************************************
     */
    public static int SLL(int value) {
        int c = (value & 0x80) >> 7;
        value = (value << 1 | 0x1) & 0xFF;
        Z80.F = (SZP[value] | c);
        return value;
    }

    /**
     * *************************************************************
     * SRL	r8 *************************************************************
     */
    public static int SRL(int value) {
        int c = value & 0x1;
        value = value >> 1 & 0xFF;
        Z80.F = (SZP[value] | c);
        return value;
    }

    /**
     * *************************************************************
     * BIT bit,r8 *************************************************************
     */
    public static final int[] bitSet = {1, 2, 4, 8, 16, 32, 64, 128};           // lookup table for setting a bit of an 8-bit value using OR
    public static final int[] bitRes = {254, 253, 251, 247, 239, 223, 191, 127}; // lookup table for resetting a bit of an 8-bit value using AND

    public static void BIT(int bitNumber, int value) {
        Z80.F = (Z80.F & CF) | HF | (SZ_BIT[value & bitSet[bitNumber]] & ~(YF | XF)) | (value & (YF | XF));
    }

    /**
     * *************************************************************
     * BIT bit,(HL)
     * *************************************************************
     */
    public static void BIT_HL(int bitNumber, int value) {
        Z80.F = (Z80.F & CF) | HF | (SZ_BIT[value & bitSet[bitNumber]] & ~(YF | XF)) | (((Z80.WZ >> 8) & 0xFF) & (YF | XF));
    }

    /**
     * *************************************************************
     * BIT	bit,(IX/Y+o)
     * *************************************************************
     */
    public static void BIT_XY(int bitNumber, int value) {
        Z80.F = (Z80.F & CF) | HF | (SZ_BIT[value & bitSet[bitNumber]] & ~(YF | XF)) | ((EA >> 8) & (YF | XF));
    }

    /**
     * *************************************************************
     * RES	bit,r8 *************************************************************
     */
    public static int RES(int bitNumber, int value) {
        value = value & bitRes[bitNumber];
        return value;
    }

    /**
     * *************************************************************
     * SET bit,r8 *************************************************************
     */
    public static int SET(int bitNumber, int value) {
        value = value | bitSet[bitNumber];
        return value;
    }

    /**
     * *************************************************************
     * LDI *************************************************************
     */
    public static void LDI() {
        int io = RM(HL());
        WM(DE(), io);
        Z80.F &= SF | ZF | CF;
        if (((Z80.A + io) & 0x02) != 0) {
            Z80.F |= YF;
            /* bit 1 -> flag 5 */
        }
        if (((Z80.A + io) & 0x08) != 0) {
            Z80.F |= XF;
            /* bit 3 -> flag 3 */
        }
        HL((HL() + 1) & 0xFFFF);
        DE((DE() + 1) & 0xFFFF);
        BC((BC() - 1) & 0xFFFF);
        if (BC() != 0) {
            Z80.F |= VF;
        }
    }

    /**
     * *************************************************************
     * CPI *************************************************************
     */
    public static void CPI() {
        int val = RM(HL());
        int res = (Z80.A - val) & 0xFF;
        Z80.WZ = (Z80.WZ + 1) & 0xFFFF;
        HL((HL() + 1) & 0xFFFF);
        BC((BC() - 1) & 0xFFFF);
        Z80.F = (Z80.F & CF) | (SZ[res] & ~(YF | XF)) | ((Z80.A ^ val ^ res) & HF) | NF;
        if ((Z80.F & HF) != 0) {
            res = (res - 1) & 0xff;
        }
        if ((res & 0x02) != 0) {
            Z80.F |= YF;
            /* bit 1 -> flag 5 */
        }
        if ((res & 0x08) != 0) {
            Z80.F |= XF;
            /* bit 3 -> flag 3 */
        }
        if (BC() != 0) {
            Z80.F |= VF;
        }
    }

    /**
     * *************************************************************
     * INI *************************************************************
     */
    public static void INI() {
        int io = IN(BC());
        Z80.WZ = (BC() + 1) & 0xFFFF;
        Z80.B = (Z80.B - 1) & 0xFF;
        WM(HL(), io);
        HL((HL() + 1) & 0xFFFF);
        Z80.F = SZ[Z80.B];
        if ((io & SF) != 0) {
            Z80.F |= NF;
        }
        if (((((Z80.C + 1) & 0xff) + io) & 0x100) != 0) {
            Z80.F |= HF | CF;
        }
        if (((irep_tmp1[Z80.C & 3][io & 3]
                ^ breg_tmp2[Z80.B]
                ^ (Z80.C >> 2)
                ^ (io >> 2)) & 1) != 0) {
            Z80.F |= PF;
        }
    }

    /**
     * *************************************************************
     * / * OUTI *************************************************************
     */
    public static void OUTI() {
        int io = RM(HL());
        Z80.B = (Z80.B - 1) & 0xFF;
        Z80.WZ = (BC() + 1) & 0xFFFF;
        OUT(BC(), io);
        HL((HL() + 1) & 0xFFFF);
        Z80.F = SZ[Z80.B];
        if ((io & SF) != 0) {
            Z80.F |= NF;
        }
        if (((((Z80.C + 1) & 0xff) + io) & 0x100) != 0) {
            Z80.F |= HF | CF;
        }
        if (((irep_tmp1[Z80.C & 3][io & 3]
                ^ breg_tmp2[Z80.B]
                ^ (Z80.C >> 2)
                ^ (io >> 2)) & 1) != 0) {
            Z80.F |= PF;
        }
    }

    /**
     * *************************************************************
     * LDD *************************************************************
     */
    public static void LDD() {
        int io = RM(HL());
        WM(DE(), io);
        Z80.F &= SF | ZF | CF;
        if (((Z80.A + io) & 0x02) != 0) {
            Z80.F |= YF;
            /* bit 1 -> flag 5 */
        }
        if (((Z80.A + io) & 0x08) != 0) {
            Z80.F |= XF;
            /* bit 3 -> flag 3 */
        }
        HL((HL() - 1) & 0xFFFF);
        DE((DE() - 1) & 0xFFFF);
        BC((BC() - 1) & 0xFFFF);
        if (BC() != 0) {
            Z80.F |= VF;
        }
    }

    /**
     * *************************************************************
     * CPD *************************************************************
     */
    public static void CPD() {
        int val = RM(HL());
        int res = (Z80.A - val) & 0xFF;
        Z80.WZ = (Z80.WZ - 1) & 0xFFFF;
        HL((HL() - 1) & 0xFFFF);
        BC((BC() - 1) & 0xFFFF);
        Z80.F = (Z80.F & CF) | (SZ[res] & ~(YF | XF)) | ((Z80.A ^ val ^ res) & HF) | NF;
        if ((Z80.F & HF) != 0) {
            res = (res - 1) & 0xff;
        }
        if ((res & 0x02) != 0) {
            Z80.F |= YF;
            /* bit 1 -> flag 5 */
        }
        if ((res & 0x08) != 0) {
            Z80.F |= XF;
            /* bit 3 -> flag 3 */
        }
        if (BC() != 0) {
            Z80.F |= VF;
        }
    }

    /**
     * *************************************************************
     * IND *************************************************************
     */
    public static void IND() {
        int io = IN(BC());
        Z80.WZ = (BC() - 1) & 0xFFFF;
        Z80.B = (Z80.B - 1) & 0xFF;
        WM(HL(), io);
        HL((HL() - 1) & 0xFFFF);
        Z80.F = SZ[Z80.B];
        if ((io & SF) != 0) {
            Z80.F |= NF;
        }
        if (((((Z80.C - 1) & 0xff) + io) & 0x100) != 0) {
            Z80.F |= HF | CF;
        }
        if (((drep_tmp1[Z80.C & 3][io & 3]
                ^ breg_tmp2[Z80.B]
                ^ (Z80.C >> 2)
                ^ (io >> 2)) & 1) != 0) {
            Z80.F |= PF;
        }
    }

    /**
     * *************************************************************
     * OUTD *************************************************************
     */
    public static void OUTD() {
        int io = RM(HL());
        Z80.B = (Z80.B - 1) & 0xFF;
        Z80.WZ = (BC() - 1) & 0xFFFF;
        OUT(BC(), io);
        HL((HL() - 1) & 0xFFFF);
        Z80.F = SZ[Z80.B];
        if ((io & SF) != 0) {
            Z80.F |= NF;
        }
        if (((((Z80.C - 1) & 0xff) + io) & 0x100) != 0) {
            Z80.F |= HF | CF;
        }
        if (((drep_tmp1[Z80.C & 3][io & 3]
                ^ breg_tmp2[Z80.B]
                ^ (Z80.C >> 2)
                ^ (io >> 2)) & 1) != 0) {
            Z80.F |= PF;
        }
    }

    /**
     * *************************************************************
     * LDIR *************************************************************
     */
    public static void LDIR() {
        LDI();
        if (BC() != 0) {
            Z80.PC = (Z80.PC - 2) & 0xFFFF;
            Z80.WZ = (Z80.PC + 1) & 0xFFFF;
            z80_ICount[0] -= cc[Z80_TABLE_ex][0xb0];//CC(ex,0xb0);											
        }
    }

    /**
     * *************************************************************
     * CPIR *************************************************************
     */
    public static void CPIR() {
        CPI();
        if (BC() != 0 && (Z80.F & ZF) == 0) {
            Z80.PC = (Z80.PC - 2) & 0xFFFF;
            Z80.WZ = (Z80.PC + 1) & 0xFFFF;
            z80_ICount[0] -= cc[Z80_TABLE_ex][0xb1];//CC(ex,0xb1);											
        }
    }

    /**
     * *************************************************************
     * INIR *************************************************************
     */
    public static void INIR() {
        INI();
        if (Z80.B != 0) {
            Z80.PC = (Z80.PC - 2) & 0xFFFF;
            z80_ICount[0] -= cc[Z80_TABLE_ex][0xb2];
        }
    }

    /**
     * *************************************************************
     * OTIR *************************************************************
     */
    public static void OTIR() {
        OUTI();
        if (Z80.B != 0) {
            Z80.PC = (Z80.PC - 2) & 0xFFFF;
            z80_ICount[0] -= cc[Z80_TABLE_ex][0xb3];
        }
    }

    /**
     * *************************************************************
     * LDDR *************************************************************
     */
    public static void LDDR() {
        LDD();
        if (BC() != 0) {
            Z80.PC = (Z80.PC - 2) & 0xFFFF;
            Z80.WZ = (Z80.PC + 1) & 0xFFFF;
            z80_ICount[0] -= cc[Z80_TABLE_ex][0xb8];
        }
    }

    /**
     * *************************************************************
     * CPDR *************************************************************
     */
    public static void CPDR() {
        CPD();
        if (BC() != 0 && (Z80.F & ZF) == 0) {
            Z80.PC = (Z80.PC - 2) & 0xFFFF;
            Z80.WZ = (Z80.PC + 1) & 0xFFFF;
            z80_ICount[0] -= cc[Z80_TABLE_ex][0xb9];
        }
    }

    /**
     * *************************************************************
     * INDR *************************************************************
     */
    public static void INDR() {
        IND();
        if (Z80.B != 0) {
            Z80.PC = (Z80.PC - 2) & 0xFFFF;
            z80_ICount[0] -= cc[Z80_TABLE_ex][0xba];
        }
    }

    /**
     * *************************************************************
     * OTDR *************************************************************
     */
    public static void OTDR() {
        OUTD();
        if (Z80.B != 0) {
            Z80.PC = (Z80.PC - 2) & 0xFFFF;
            z80_ICount[0] -= cc[Z80_TABLE_ex][0xbb];
        }
    }

    /**
     * *************************************************************
     * EI *************************************************************
     */
    public static void EI() {
        /* If interrupts were disabled, execute one more			
         * instruction and check the IRQ line.                      
         * If not, simply set interrupt flip-flop 2                 
         */
        if (Z80.IFF1 == 0) {
            Z80.IFF1 = Z80.IFF2 = 1;
            Z80.PREPC = Z80.PC & 0xFFFF;
            Z80.R = (Z80.R + 1) & 0xFF;
            while (cpu_readop(Z80.PC) == 0xfb) /* more EIs? */ {
                //LOG(("Z80 #%d multiple EI opcodes at %04X\n",cpu_getactivecpu(), _PC));						
                z80_ICount[0] -= cc[Z80_TABLE_op][0xfb];//CC(op,0xfb);										
                Z80.PREPC = Z80.PC & 0xFFFF;
                Z80.PC = (Z80.PC + 1) & 0xFFFF;
                Z80.R = (Z80.R + 1) & 0xFF;
            }
            if (Z80.irq_state != CLEAR_LINE || Z80.request_irq >= 0) {
                after_EI = 1;
                /* avoid cycle skip hacks */
                int op = ROP();
                z80_ICount[0] -= cc[Z80_TABLE_op][op];
                Z80op[op].handler();//EXEC(op,ROP()); 									
                after_EI = 0;
                //LOG(("Z80 #%d EI takes irq\n", cpu_getactivecpu())); 
                take_interrupt();
            } else {
                int op = ROP();
                z80_ICount[0] -= cc[Z80_TABLE_op][op];
                Z80op[op].handler();//EXEC(op,ROP());
            }
        } else {
            Z80.IFF2 = 1;
        }
    }

    static void take_interrupt() {
        if (Z80.IFF1 != 0) {

            int irq_vector;

            /* there isn't a valid previous program counter */
            Z80.PREPC = -1;

            /* Check if processor was halted */
            LEAVE_HALT();

            if (Z80.irq_max != 0) /* daisy chain mode */ {
                if (Z80.request_irq >= 0) {
                    /* Clear both interrupt flip flops */
                    Z80.IFF1 = Z80.IFF2 = 0;
                    irq_vector = Z80.irq[Z80.request_irq].interrupt_entry.handler(Z80.irq[Z80.request_irq].irq_param);
                    //LOG(("Z80 #%d daisy chain irq_vector $%02x\n", cpu_getactivecpu(), irq_vector));
                    Z80.request_irq = -1;
                } else {
                    return;
                }
            } else {
                /* Clear both interrupt flip flops */
                Z80.IFF1 = Z80.IFF2 = 0;
                /* call back the cpu interface to retrieve the vector */
                irq_vector = (Z80.irq_callback).handler(0);
                //LOG(("Z80 #%d single int. irq_vector $%02x\n", cpu_getactivecpu(), irq_vector));
            }

            /* Interrupt mode 2. Call [Z80.I:databyte] */
            if (Z80.IM == 2) {
                irq_vector = (irq_vector & 0xff) | (Z80.I << 8);
                PUSH(Z80.PC);
                Z80.PC = RM16(irq_vector);
                //LOG(("Z80 #%d IM2 [$%04x] = $%04x\n",cpu_getactivecpu() , irq_vector, _PCD));
                /* CALL opcode timing */
                Z80.extra_cycles += cc[Z80_TABLE_op][0xcd];
            } else /* Interrupt mode 1. RST 38h */ if (Z80.IM == 1) {
                //LOG(("Z80 #%d IM1 $0038\n",cpu_getactivecpu() ));
                PUSH(Z80.PC);
                Z80.PC = 0x0038;
                /* RST $38 + 'interrupt latency' cycles */
                Z80.extra_cycles += cc[Z80_TABLE_op][0xff] + cc[Z80_TABLE_ex][0xff];
            } else {
                /* Interrupt mode 0. We check for CALL and JP instructions, */
 /* if neither of these were found we assume a 1 byte opcode */
 /* was placed on the databus                                */
                //LOG(("Z80 #%d IM0 $%04x\n",cpu_getactivecpu() , irq_vector));
                switch (irq_vector & 0xff0000) {
                    case 0xcd0000:
                        /* call */
                        PUSH(Z80.PC);
                        Z80.PC = irq_vector & 0xffff;
                        /* CALL $xxxx + 'interrupt latency' cycles */
                        Z80.extra_cycles += cc[Z80_TABLE_op][0xcd] + cc[Z80_TABLE_ex][0xff];
                        break;
                    case 0xc30000:
                        /* jump */
                        Z80.PC = irq_vector & 0xffff;
                        /* JP $xxxx + 2 cycles */
                        Z80.extra_cycles += cc[Z80_TABLE_op][0xc3] + cc[Z80_TABLE_ex][0xff];
                        break;
                    default:
                        /* rst (or other opcodes?) */
                        PUSH(Z80.PC);
                        Z80.PC = irq_vector & 0x0038;
                        /* RST $xx + 2 cycles */
                        Z80.extra_cycles += cc[Z80_TABLE_op][Z80.PC] + cc[Z80_TABLE_ex][Z80.PC];
                        break;
                }
            }
            Z80.WZ = Z80.PC;//TODO check if it has to run always and not only if IFF1!=0
            change_pc16(Z80.PC);
        }
    }

    @Override
    public void init() {
        int cpu = cpu_getactivecpu();
        int i, p;
        int oldval, newval, val;
        int padd, padc, psub, psbc;
        padd = 0 * 256;
        padc = 256 * 256;
        psub = 0 * 256;
        psbc = 256 * 256;
        for (oldval = 0; oldval < 256; oldval++) {
            for (newval = 0; newval < 256; newval++) {
                /* add or adc w/o carry set */
                val = newval - oldval;

                if (newval != 0) {
                    if ((newval & 0x80) != 0) {
                        SZHVC_Add[padd] = SF;
                    } else {
                        SZHVC_Add[padd] = 0;
                    }
                } else {
                    SZHVC_Add[padd] = ZF;
                }

                SZHVC_Add[padd] |= (newval & (YF | XF));/* undocumented flag bits 5+3 */

                if ((newval & 0x0f) < (oldval & 0x0f)) {
                    SZHVC_Add[padd] |= HF;
                }
                if (newval < oldval) {
                    SZHVC_Add[padd] |= CF;
                }
                if (((val ^ oldval ^ 0x80) & (val ^ newval) & 0x80) != 0) {
                    SZHVC_Add[padd] |= VF;
                }
                padd++;

                /* adc with carry set */
                val = newval - oldval - 1;
                if (newval != 0) {
                    if ((newval & 0x80) != 0) {
                        SZHVC_Add[padc] = SF;
                    } else {
                        SZHVC_Add[padc] = 0;
                    }
                } else {
                    SZHVC_Add[padc] = ZF;
                }

                SZHVC_Add[padc] |= (newval & (YF | XF));/* undocumented flag bits 5+3 */

                if ((newval & 0x0f) <= (oldval & 0x0f)) {
                    SZHVC_Add[padc] |= HF;
                }
                if (newval <= oldval) {
                    SZHVC_Add[padc] |= CF;
                }
                if (((val ^ oldval ^ 0x80) & (val ^ newval) & 0x80) != 0) {
                    SZHVC_Add[padc] |= VF;
                }
                padc++;

                /* cp, sub or sbc w/o carry set */
                val = oldval - newval;
                if (newval != 0) {
                    if ((newval & 0x80) != 0) {
                        SZHVC_sub[psub] = NF | SF;
                    } else {
                        SZHVC_sub[psub] = NF;
                    }
                } else {
                    SZHVC_sub[psub] = NF | ZF;
                }

                SZHVC_sub[psub] |= (newval & (YF | XF));/* undocumented flag bits 5+3 */

                if ((newval & 0x0f) > (oldval & 0x0f)) {
                    SZHVC_sub[psub] |= HF;
                }
                if (newval > oldval) {
                    SZHVC_sub[psub] |= CF;
                }
                if (((val ^ oldval) & (oldval ^ newval) & 0x80) != 0) {
                    SZHVC_sub[psub] |= VF;
                }
                psub++;

                /* sbc with carry set */
                val = oldval - newval - 1;
                if (newval != 0) {
                    if ((newval & 0x80) != 0) {
                        SZHVC_sub[psbc] = NF | SF;
                    } else {
                        SZHVC_sub[psbc] = NF;
                    }
                } else {
                    SZHVC_sub[psbc] = NF | ZF;
                }

                SZHVC_sub[psbc] |= (newval & (YF | XF));/* undocumented flag bits 5+3 */

                if ((newval & 0x0f) >= (oldval & 0x0f)) {
                    SZHVC_sub[psbc] |= HF;
                }
                if (newval >= oldval) {
                    SZHVC_sub[psbc] |= CF;
                }
                if (((val ^ oldval) & (oldval ^ newval) & 0x80) != 0) {
                    SZHVC_sub[psbc] |= VF;
                }
                psbc++;
            }
        }
        for (i = 0; i < 256; i++) {
            p = 0;
            if ((i & 0x01) != 0) {
                ++p;
            }
            if ((i & 0x02) != 0) {
                ++p;
            }
            if ((i & 0x04) != 0) {
                ++p;
            }
            if ((i & 0x08) != 0) {
                ++p;
            }
            if ((i & 0x10) != 0) {
                ++p;
            }
            if ((i & 0x20) != 0) {
                ++p;
            }
            if ((i & 0x40) != 0) {
                ++p;
            }
            if ((i & 0x80) != 0) {
                ++p;
            }
            SZ[i] = (i != 0) ? i & 0x80 : 0x40;
            SZ[i] |= (i & (0x20 | 0x08));/* undocumented flag bits 5+3 */

            SZ_BIT[i] = (i != 0) ? i & 0x80 : 0x40 | 0x04;
            SZ_BIT[i] |= (i & (0x20 | 0x08));/* undocumented flag bits 5+3 */

            SZP[i] = SZ[i] | (((p & 1) != 0) ? 0 : 0x04);
            SZHV_inc[i] = SZ[i];
            if (i == 0x80) {
                SZHV_inc[i] |= 0x04;
            }
            if ((i & 0x0f) == 0x00) {
                SZHV_inc[i] |= 0x10;
            }
            SZHV_dec[i] = SZ[i] | 0x02;
            if (i == 0x7f) {
                SZHV_dec[i] |= 0x04;
            }
            if ((i & 0x0f) == 0x0f) {
                SZHV_dec[i] |= 0x10;
            }
        }
        /*TODO*///	state_save_register_UINT16("z80", cpu, "AF", &Z80.AF.w.l, 1);
/*TODO*///	state_save_register_UINT16("z80", cpu, "BC", &Z80.BC.w.l, 1);
/*TODO*///	state_save_register_UINT16("z80", cpu, "DE", &Z80.DE.w.l, 1);
/*TODO*///	state_save_register_UINT16("z80", cpu, "HL", &Z80.HL.w.l, 1);
/*TODO*///	state_save_register_UINT16("z80", cpu, "IX", &Z80.IX.w.l, 1);
/*TODO*///	state_save_register_UINT16("z80", cpu, "IY", &Z80.IY.w.l, 1);
/*TODO*///	state_save_register_UINT16("z80", cpu, "PC", &Z80.PC.w.l, 1);
/*TODO*///	state_save_register_UINT16("z80", cpu, "SP", &Z80.SP.w.l, 1);
/*TODO*///	state_save_register_UINT16("z80", cpu, "AF2", &Z80.AF2.w.l, 1);
/*TODO*///	state_save_register_UINT16("z80", cpu, "BC2", &Z80.BC2.w.l, 1);
/*TODO*///	state_save_register_UINT16("z80", cpu, "DE2", &Z80.DE2.w.l, 1);
/*TODO*///	state_save_register_UINT16("z80", cpu, "HL2", &Z80.HL2.w.l, 1);
/*TODO*///	state_save_register_UINT8("z80", cpu, "R", &Z80.R, 1);
/*TODO*///	state_save_register_UINT8("z80", cpu, "R2", &Z80.R2, 1);
/*TODO*///	state_save_register_UINT8("z80", cpu, "IFF1", &Z80.IFF1, 1);
/*TODO*///	state_save_register_UINT8("z80", cpu, "IFF2", &Z80.IFF2, 1);
/*TODO*///	state_save_register_UINT8("z80", cpu, "HALT", &Z80.HALT, 1);
/*TODO*///	state_save_register_UINT8("z80", cpu, "IM", &Z80.IM, 1);
/*TODO*///	state_save_register_UINT8("z80", cpu, "I", &Z80.I, 1);
/*TODO*///	state_save_register_UINT8("z80", cpu, "irq_max", &Z80.irq_max, 1);
/*TODO*///	state_save_register_INT8("z80", cpu, "request_irq", &Z80.request_irq, 1);
/*TODO*///	state_save_register_INT8("z80", cpu, "service_irq", &Z80.service_irq, 1);
/*TODO*///	state_save_register_UINT8("z80", cpu, "int_state", Z80.int_state, 4);
/*TODO*///	state_save_register_UINT8("z80", cpu, "nmi_state", &Z80.nmi_state, 1);
/*TODO*///	state_save_register_UINT8("z80", cpu, "irq_state", &Z80.irq_state, 1);
        /* daisy chain needs to be saved by z80ctc.c somehow */
    }

    @Override
    public void reset(Object param) {
        Z80_DaisyChain[] daisy_chain = (Z80_DaisyChain[]) param;
        //memset(&Z80, 0, sizeof(Z80));
        Z80.PREPC = 0;
        Z80.PC = 0;
        Z80.SP = 0;
        Z80.A = 0;
        Z80.F = 0;
        Z80.B = 0;
        Z80.C = 0;
        Z80.D = 0;
        Z80.E = 0;
        Z80.H = 0;
        Z80.L = 0;
        Z80.IX = 0;
        Z80.IY = 0;
        Z80.A2 = 0;
        Z80.F2 = 0;
        Z80.B2 = 0;
        Z80.C2 = 0;
        Z80.D2 = 0;
        Z80.E2 = 0;
        Z80.H2 = 0;
        Z80.L2 = 0;
        Z80.WZ = 0;
        Z80.R = 0;
        Z80.R2 = 0;
        Z80.IFF1 = 0;
        Z80.IFF2 = 0;
        Z80.HALT = 0;
        Z80.IM = 0;
        Z80.I = 0;
        Z80.irq_max = 0;
        Z80.request_irq = 0;
        Z80.service_irq = 0;
        Z80.nmi_state = 0;
        Z80.irq_state = 0;
        Z80.int_state = new int[Z80_MAXDAISY];
        Z80.irq = new Z80_DaisyChain[Z80_MAXDAISY];
        Z80.irq_callback = null;
        Z80.extra_cycles = 0;

        Z80.IX = Z80.IY = 0xffff;
        /* IX and IY are FFFF after a reset! */
        Z80.F = ZF;
        /* Zero flag is set */
        Z80.request_irq = -1;
        Z80.service_irq = -1;
        Z80.nmi_state = CLEAR_LINE;
        Z80.irq_state = CLEAR_LINE;

        int dci = 0;
        if (daisy_chain != null) {
            while (daisy_chain[dci].irq_param != -1 && Z80.irq_max < Z80_MAXDAISY) {
                /* set callbackhandler after reti */
                Z80.irq[Z80.irq_max] = daisy_chain[dci];
                /* device reset */

                Z80.irq[Z80.irq_max].reset.handler(Z80.irq[Z80.irq_max].irq_param);
                Z80.irq_max++;
                dci++;
            }
        }
        change_pc16(Z80.PC);
    }

    @Override
    public void exit() {
        SZHVC_Add = null;
        SZHVC_sub = null;
    }

    @Override
    public int execute(int cycles) {
        z80_ICount[0] = cycles - Z80.extra_cycles;
        Z80.extra_cycles = 0;

        do {
            Z80.PREPC = Z80.PC & 0xFFFF;
            Z80.R = (Z80.R + 1) & 0xFF;//_R++;
            int op = ROP();
            z80_ICount[0] -= cc[Z80_TABLE_op][op];
            Z80op[op].handler();//EXEC_INLINE(op, ROP());
        } while (z80_ICount[0] > 0);

        z80_ICount[0] -= Z80.extra_cycles;
        Z80.extra_cycles = 0;

        return cycles - z80_ICount[0];
    }

    /**
     * **************************************************************************
     * Burn 'cycles' T-states. Adjust R register for the lost time
     * **************************************************************************
     */
    public static burnPtr burn_function = new burnPtr() {
        public void handler(int cycles) {
            if (cycles > 0) {
                /* NOP takes 4 cycles per instruction */
                int n = (cycles + 3) / 4;
                Z80.R = (Z80.R + n) & 0xFF;
                z80_ICount[0] -= 4 * n;
            }
        }
    };

    /**
     * **************************************************************************
     * Get all registers in given buffer
     * **************************************************************************
     */
    @Override
    public Object get_context() {
        Z80_Regs Regs = new Z80_Regs();
        Regs.PREPC = Z80.PREPC;
        Regs.PC = Z80.PC;
        Regs.SP = Z80.SP;

        Regs.A = Z80.A;
        Regs.F = Z80.F;
        Regs.B = Z80.B;
        Regs.C = Z80.C;
        Regs.D = Z80.D;
        Regs.E = Z80.E;
        Regs.H = Z80.H;
        Regs.L = Z80.L;
        Regs.IX = Z80.IX;
        Regs.IY = Z80.IY;
        Regs.A2 = Z80.A2;
        Regs.F2 = Z80.F2;
        Regs.B2 = Z80.B2;
        Regs.C2 = Z80.C2;
        Regs.D2 = Z80.D2;
        Regs.E2 = Z80.E2;
        Regs.H2 = Z80.H2;
        Regs.L2 = Z80.L2;
        Regs.WZ = Z80.WZ;
        Regs.R = Z80.R;
        Regs.R2 = Z80.R2;
        Regs.IFF1 = Z80.IFF1;
        Regs.IFF2 = Z80.IFF2;
        Regs.HALT = Z80.HALT;
        Regs.IM = Z80.IM;
        Regs.I = Z80.I;
        Regs.irq_max = Z80.irq_max;
        Regs.request_irq = Z80.request_irq;
        Regs.service_irq = Z80.service_irq;
        Regs.nmi_state = Z80.nmi_state;
        Regs.irq_state = Z80.irq_state;
        Regs.int_state[0] = Z80.int_state[0];
        Regs.int_state[1] = Z80.int_state[1];
        Regs.int_state[2] = Z80.int_state[2];
        Regs.int_state[3] = Z80.int_state[3];
        Regs.irq[0] = Z80.irq[0];
        Regs.irq[1] = Z80.irq[1];
        Regs.irq[2] = Z80.irq[2];
        Regs.irq[3] = Z80.irq[3];
        Regs.irq_callback = Z80.irq_callback;
        Regs.extra_cycles = Z80.extra_cycles;
        return Regs;
    }

    /**
     * **************************************************************************
     * Set all registers to given values
     * **************************************************************************
     */
    @Override
    public void set_context(Object reg) {
        Z80_Regs Regs = (Z80_Regs) reg;
        Z80.PREPC = Regs.PREPC;
        Z80.PC = Regs.PC;
        Z80.SP = Regs.SP;

        Z80.A = Regs.A;
        Z80.F = Regs.F;
        Z80.B = Regs.B;
        Z80.C = Regs.C;
        Z80.D = Regs.D;
        Z80.E = Regs.E;
        Z80.H = Regs.H;
        Z80.L = Regs.L;
        Z80.IX = Regs.IX;
        Z80.IY = Regs.IY;
        Z80.A2 = Regs.A2;
        Z80.F2 = Regs.F2;
        Z80.B2 = Regs.B2;
        Z80.C2 = Regs.C2;
        Z80.D2 = Regs.D2;
        Z80.E2 = Regs.E2;
        Z80.H2 = Regs.H2;
        Z80.L2 = Regs.L2;
        Z80.WZ = Regs.WZ;
        Z80.R = Regs.R;
        Z80.R2 = Regs.R2;
        Z80.IFF1 = Regs.IFF1;
        Z80.IFF2 = Regs.IFF2;
        Z80.HALT = Regs.HALT;
        Z80.IM = Regs.IM;
        Z80.I = Regs.I;
        Z80.irq_max = Regs.irq_max;
        Z80.request_irq = Regs.request_irq;
        Z80.service_irq = Regs.service_irq;
        Z80.nmi_state = Regs.nmi_state;
        Z80.irq_state = Regs.irq_state;
        Z80.int_state[0] = Regs.int_state[0];
        Z80.int_state[1] = Regs.int_state[1];
        Z80.int_state[2] = Regs.int_state[2];
        Z80.int_state[3] = Regs.int_state[3];
        Z80.irq[0] = Regs.irq[0];
        Z80.irq[1] = Regs.irq[1];
        Z80.irq[2] = Regs.irq[2];
        Z80.irq[3] = Regs.irq[3];
        Z80.irq_callback = Regs.irq_callback;
        Z80.extra_cycles = Regs.extra_cycles;
        change_pc16(Z80.PC);
    }

    /**
     * **************************************************************************
     * Get a pointer to a cycle count table
     * **************************************************************************
     */
    @Override
    public int[] get_cycle_table(int which) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///	if (which >= 0 && which <= Z80_TABLE_xycb)
/*TODO*///		return cc[which];
/*TODO*///	return NULL;
    }

    /**
     * **************************************************************************
     * Set a new cycle count table
     * **************************************************************************
     */
    @Override
    public void set_cycle_table(int which, int[] new_table) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///	if (which >= 0 && which <= Z80_TABLE_ex)
/*TODO*///		cc[which] = new_table;
    }

    /**
     * **************************************************************************
     * Return program counter
     * **************************************************************************
     */
    @Override
    public int get_pc() {
        return Z80.PC & 0xFFFF;
    }

    /**
     * **************************************************************************
     * Set program counter
     * **************************************************************************
     */
    @Override
    public void set_pc(int val) {
        Z80.PC = val & 0xFFFF;
        change_pc16(Z80.PC);
    }

    /**
     * **************************************************************************
     * Return stack pointer
     * **************************************************************************
     */
    @Override
    public int get_sp() {
        return Z80.SP & 0xFFFF;
    }

    /**
     * **************************************************************************
     * Set stack pointer
     * **************************************************************************
     */
    @Override
    public void set_sp(int val) {
        Z80.SP = val & 0xFFFF;
    }

    /**
     * **************************************************************************
     * Return a specific register
     * **************************************************************************
     */
    @Override
    public int get_reg(int regnum) {
        switch (regnum) {
            case Z80_PC:
                return Z80.PC & 0xFFFF;
            case Z80_SP:
                return Z80.SP & 0xFFFF;
            case Z80_AF:
                return AF();
            case Z80_BC:
                return BC();
            case Z80_DE:
                return DE();
            case Z80_HL:
                return HL();
            case Z80_IX:
                return Z80.IX & 0xFFFF;
            case Z80_IY:
                return Z80.IY & 0xFFFF;
            case Z80_R:
                return (Z80.R & 0x7f) | (Z80.R2 & 0x80);
            case Z80_I:
                return Z80.I;
            case Z80_AF2:
                return AF2();
            case Z80_BC2:
                return BC2();
            case Z80_DE2:
                return DE2();
            case Z80_HL2:
                return HL2();
            case Z80_IM:
                return Z80.IM;
            case Z80_IFF1:
                return Z80.IFF1;
            case Z80_IFF2:
                return Z80.IFF2;
            case Z80_HALT:
                return Z80.HALT;
            case Z80_NMI_STATE:
                return Z80.nmi_state;
            case Z80_IRQ_STATE:
                return Z80.irq_state;
            case Z80_DC0:
                return Z80.int_state[0];
            case Z80_DC1:
                return Z80.int_state[1];
            case Z80_DC2:
                return Z80.int_state[2];
            case Z80_DC3:
                return Z80.int_state[3];
            case REG_PREVIOUSPC:
                return Z80.PREPC & 0xFFFF;
            default:
                if (regnum <= REG_SP_CONTENTS) {
                    int offset = Z80.SP + 2 * (REG_SP_CONTENTS - regnum);
                    if (offset < 0xffff) {
                        return RM(offset) | (RM(offset + 1) << 8);
                    }
                }
        }
        return 0;
    }

    /**
     * **************************************************************************
     * Set a specific register
     * **************************************************************************
     */
    @Override
    public void set_reg(int regnum, int val) {
        switch (regnum) {
            case Z80_PC:
                Z80.PC = val & 0xFFFF;
                break;
            case Z80_SP:
                Z80.SP = val & 0xFFFF;
                break;
            case Z80_AF:
                AF(val);
                break;
            case Z80_BC:
                BC(val);
                break;
            case Z80_DE:
                DE(val);
                break;
            case Z80_HL:
                HL(val);
                break;
            case Z80_IX:
                Z80.IX = val & 0xFFFF;
                break;
            case Z80_IY:
                Z80.IY = val & 0xFFFF;
                break;
            case Z80_R:
                Z80.R = val;
                Z80.R2 = val & 0x80;
                break;
            case Z80_I:
                Z80.I = val;
                break;
            case Z80_AF2:
                AF2(val);
                break;
            case Z80_BC2:
                BC2(val);
                break;
            case Z80_DE2:
                DE2(val);
                break;
            case Z80_HL2:
                HL2(val);
                break;
            case Z80_IM:
                Z80.IM = val;
                break;
            case Z80_IFF1:
                Z80.IFF1 = val;
                break;
            case Z80_IFF2:
                Z80.IFF2 = val;
                break;
            case Z80_HALT:
                Z80.HALT = val;
                break;
            case Z80_NMI_STATE:
                set_nmi_line(val);
                break;
            case Z80_IRQ_STATE:
                set_irq_line(0, val);
                break;
            case Z80_DC0:
                Z80.int_state[0] = val;
                break;
            case Z80_DC1:
                Z80.int_state[1] = val;
                break;
            case Z80_DC2:
                Z80.int_state[2] = val;
                break;
            case Z80_DC3:
                Z80.int_state[3] = val;
                break;
            default:
                if (regnum <= REG_SP_CONTENTS) {
                    int offset = Z80.SP + 2 * (REG_SP_CONTENTS - regnum);
                    if (offset < 0xffff) {
                        WM(offset, val & 0xff);
                        WM(offset + 1, (val >> 8) & 0xff);
                    }
                }
        }
    }

    /**
     * **************************************************************************
     * Set NMI line state
     * **************************************************************************
     */
    @Override
    public void set_nmi_line(int state) {
        if (Z80.nmi_state == state) {
            return;
        }
        //LOG(("Z80 #%d set_nmi_line %d\n", cpu_getactivecpu(), state));
        Z80.nmi_state = state;
        if (state == CLEAR_LINE) {
            return;
        }
        //LOG(("Z80 #%d take NMI\n", cpu_getactivecpu()));
        Z80.PREPC = -1;
        /* there isn't a valid previous program counter */
        LEAVE_HALT();
        /* Check if processor was halted */

        Z80.IFF1 = 0;
        PUSH(Z80.PC);
        Z80.PC = 0x0066;
        Z80.WZ = Z80.PC;
        Z80.extra_cycles += 11;
    }

    /**
     * **************************************************************************
     * Set IRQ line state
     * **************************************************************************
     */
    @Override
    public void set_irq_line(int irqline, int state) {
        //LOG(("Z80 #%d set_irq_line %d\n",cpu_getactivecpu() , state));
        Z80.irq_state = state;
        if (state == CLEAR_LINE) {
            return;
        }

        if (Z80.irq_max != 0) {
            int daisychain, device, int_state;
            daisychain = Z80.irq_callback.handler(irqline);
            device = daisychain >> 8;
            int_state = daisychain & 0xff;
            //LOG(("Z80 #%d daisy chain $%04x -> device %d, state $%02x",cpu_getactivecpu(), daisychain, device, int_state));

            if (Z80.int_state[device] != int_state) {
                //LOG((" change\n"));
                /* set new interrupt status */
                Z80.int_state[device] = int_state;
                /* check interrupt status */
                Z80.request_irq = Z80.service_irq = -1;

                /* search higher IRQ or IEO */
                for (device = 0; device < Z80.irq_max; device++) {
                    /* IEO = disable ? */
                    if ((Z80.int_state[device] & Z80_INT_IEO) != 0) {
                        Z80.request_irq = -1;
                        /* if IEO is disable , masking lower IRQ */
                        Z80.service_irq = device;
                        /* set highest interrupt service device */
                    }
                    /* IRQ = request ? */
                    if ((Z80.int_state[device] & Z80_INT_REQ) != 0) {
                        Z80.request_irq = device;
                    }
                }
                //LOG(("Z80 #%d daisy chain service_irq $%02x, request_irq $%02x\n", cpu_getactivecpu(), Z80.service_irq, Z80.request_irq));
                if (Z80.request_irq < 0) {
                    return;
                }
            } else {
                //LOG((" no change\n"));
                return;
            }
        }
        take_interrupt();
    }

    /**
     * **************************************************************************
     * Set IRQ vector callback
     * **************************************************************************
     */
    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
        //LOG(("Z80 #%d set_irq_callback $%08x\n",cpu_getactivecpu() , (int)callback));
        Z80.irq_callback = callback;
    }

    /**
     * **************************************************************************
     * Return a formatted string for a register
     * **************************************************************************
     */
    @Override
    public String cpu_info(Object context, int regnum) {
        /*TODO*///	static char buffer[32][47+1];
/*TODO*///	static int which = 0;
/*TODO*///	Z80_Regs *r = context;
/*TODO*///
/*TODO*///	which = (which+1) % 32;
/*TODO*///    buffer[which][0] = '\0';
/*TODO*///	if( !context )
/*TODO*///		r = &Z80;
/*TODO*///
        switch (regnum) {
            /*TODO*///		case CPU_INFO_REG+Z80_PC: sprintf(buffer[which], "PC:%04X", r->PC.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_SP: sprintf(buffer[which], "SP:%04X", r->SP.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_AF: sprintf(buffer[which], "AF:%04X", r->AF.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_BC: sprintf(buffer[which], "BC:%04X", r->BC.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_DE: sprintf(buffer[which], "DE:%04X", r->DE.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_HL: sprintf(buffer[which], "HL:%04X", r->HL.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_IX: sprintf(buffer[which], "IX:%04X", r->IX.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_IY: sprintf(buffer[which], "IY:%04X", r->IY.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_R: sprintf(buffer[which], "R:%02X", (r->R & 0x7f) | (r->R2 & 0x80)); break;
/*TODO*///		case CPU_INFO_REG+Z80_I: sprintf(buffer[which], "I:%02X", r->I); break;
/*TODO*///		case CPU_INFO_REG+Z80_AF2: sprintf(buffer[which], "AF'%04X", r->AF2.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_BC2: sprintf(buffer[which], "BC'%04X", r->BC2.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_DE2: sprintf(buffer[which], "DE'%04X", r->DE2.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_HL2: sprintf(buffer[which], "HL'%04X", r->HL2.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_IM: sprintf(buffer[which], "IM:%X", r->IM); break;
/*TODO*///		case CPU_INFO_REG+Z80_IFF1: sprintf(buffer[which], "IFF1:%X", r->IFF1); break;
/*TODO*///		case CPU_INFO_REG+Z80_IFF2: sprintf(buffer[which], "IFF2:%X", r->IFF2); break;
/*TODO*///		case CPU_INFO_REG+Z80_HALT: sprintf(buffer[which], "HALT:%X", r->HALT); break;
/*TODO*///		case CPU_INFO_REG+Z80_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
/*TODO*///		case CPU_INFO_REG+Z80_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state); break;
/*TODO*///		case CPU_INFO_REG+Z80_DC0: if(Z80.irq_max >= 1) sprintf(buffer[which], "DC0:%X", r->int_state[0]); break;
/*TODO*///		case CPU_INFO_REG+Z80_DC1: if(Z80.irq_max >= 2) sprintf(buffer[which], "DC1:%X", r->int_state[1]); break;
/*TODO*///		case CPU_INFO_REG+Z80_DC2: if(Z80.irq_max >= 3) sprintf(buffer[which], "DC2:%X", r->int_state[2]); break;
/*TODO*///		case CPU_INFO_REG+Z80_DC3: if(Z80.irq_max >= 4) sprintf(buffer[which], "DC3:%X", r->int_state[3]); break;
/*TODO*///        case CPU_INFO_FLAGS:
/*TODO*///			sprintf(buffer[which], "%c%c%c%c%c%c%c%c",
/*TODO*///				r->AF.b.l & 0x80 ? 'S':'.',
/*TODO*///				r->AF.b.l & 0x40 ? 'Z':'.',
/*TODO*///				r->AF.b.l & 0x20 ? '5':'.',
/*TODO*///				r->AF.b.l & 0x10 ? 'H':'.',
/*TODO*///				r->AF.b.l & 0x08 ? '3':'.',
/*TODO*///				r->AF.b.l & 0x04 ? 'P':'.',
/*TODO*///				r->AF.b.l & 0x02 ? 'N':'.',
/*TODO*///				r->AF.b.l & 0x01 ? 'C':'.');
/*TODO*///			break;
            case CPU_INFO_NAME:
                return "Z80";
            case CPU_INFO_FAMILY:
                return "Zilog Z80";
            case CPU_INFO_VERSION:
                return "3.3";
            case CPU_INFO_FILE:
                return "z80.java";
            case CPU_INFO_CREDITS:
                return "Copyright (C) 1998,1999 Juergen Buchmueller, all rights reserved.";
            /*TODO*///		case CPU_INFO_REG_LAYOUT: return (const char *)z80_reg_layout;
/*TODO*///		case CPU_INFO_WIN_LAYOUT: return (const char *)z80_win_layout;
            }
        /*TODO*///	return buffer[which];
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /* 
     *
     * arcadeflex functions
     */
    @Override
    public Object init_context() {
        Object reg = new Z80_Regs();
        return reg;
    }

    @Override
    public void set_op_base(int pc) {
        cpu_setOPbase16.handler(pc);
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
    public void internal_interrupt(int type) {
        //doesn't exist in z80 cpu
    }

    @Override
    public int internal_read(int offset) {
        return 0; //doesn't exist in z80 cpu
    }

    @Override
    public void internal_write(int offset, int data) {
        //doesesn't exist in z80 cpu
    }

    @Override
    public int mem_address_bits_of_cpu() {
        return 16;
    }

}
