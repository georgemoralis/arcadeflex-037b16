/*
 * ported to v0.37b16
 * 
 */
package gr.codebb.arcadeflex.v037b16.mame;

import arcadeflex037b16.fucPtr.InterruptPtr;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;

public class cpuintrfH {

    public static class MachineCPU {

        public MachineCPU(int cpu_type, int cpu_clock, Object memory_read, Object memory_write, Object port_read, Object port_write, InterruptPtr vblank_interrupt, int vblank_interrupts_per_frame, InterruptPtr timed_interrupt, int timed_interrupts_per_second, Object reset_param) {
            this.cpu_type = cpu_type;
            this.cpu_clock = cpu_clock;
            this.memory_read = memory_read;
            this.memory_write = memory_write;
            this.port_read = port_read;
            this.port_write = port_write;
            this.vblank_interrupt = vblank_interrupt;
            this.vblank_interrupts_per_frame = vblank_interrupts_per_frame;
            this.timed_interrupt = timed_interrupt;
            this.timed_interrupts_per_second = timed_interrupts_per_second;
            this.reset_param = reset_param;
        }

        public MachineCPU(int cpu_type, int cpu_clock, Object memory_read, Object memory_write, Object port_read, Object port_write, InterruptPtr vblank_interrupt, int vblank_interrupts_per_frame, InterruptPtr timed_interrupt, int timed_interrupts_per_second) {
            this.cpu_type = cpu_type;
            this.cpu_clock = cpu_clock;
            this.memory_read = memory_read;
            this.memory_write = memory_write;
            this.port_read = port_read;
            this.port_write = port_write;
            this.vblank_interrupt = vblank_interrupt;
            this.vblank_interrupts_per_frame = vblank_interrupts_per_frame;
            this.timed_interrupt = timed_interrupt;
            this.timed_interrupts_per_second = timed_interrupts_per_second;
            this.reset_param = null;
        }

        public MachineCPU(int cpu_type, int cpu_clock, Object memory_read, Object memory_write, Object port_read, Object port_write, InterruptPtr vblank_interrupt, int vblank_interrupts_per_frame) {
            this.cpu_type = cpu_type;
            this.cpu_clock = cpu_clock;
            this.memory_read = memory_read;
            this.memory_write = memory_write;
            this.port_read = port_read;
            this.port_write = port_write;
            this.vblank_interrupt = vblank_interrupt;
            this.vblank_interrupts_per_frame = vblank_interrupts_per_frame;
            this.timed_interrupt = null;
            this.timed_interrupts_per_second = 0;
            this.reset_param = null;
        }

        public MachineCPU() {
            this(0, 0, null, null, null, null, null, 0, null, 0, null);
        }

        public static MachineCPU[] create(int n) {
            MachineCPU[] a = new MachineCPU[n];
            for (int k = 0; k < n; k++) {
                a[k] = new MachineCPU();
            }
            return a;
        }
        public int cpu_type;/* see #defines below. */
        public int cpu_clock;/* in Hertz */
        public Object memory_read;/* struct Memory_ReadAddress */
        public Object memory_write;/* struct Memory_WriteAddress */
        public Object port_read;
        public Object port_write;
        public InterruptPtr vblank_interrupt;/* for interrupts tied to VBLANK */
        public int vblank_interrupts_per_frame;/* usually 1 */
        public InterruptPtr timed_interrupt;/* for interrupts not tied to VBLANK */
        public int timed_interrupts_per_second;
        public Object reset_param;/* parameter for cpu_reset */

    }

    public static final int CPU_DUMMY = 0;
    public static final int CPU_Z80 = 1;
    /*TODO*///#if (HAS_8080)
/*TODO*///	CPU_8080,
/*TODO*///#endif
/*TODO*///#if (HAS_8085A)
/*TODO*///	CPU_8085A,
/*TODO*///#endif
/*TODO*///#if (HAS_M6502)
/*TODO*///	CPU_M6502,
/*TODO*///#endif
/*TODO*///#if (HAS_M65C02)
/*TODO*///	CPU_M65C02,
/*TODO*///#endif
/*TODO*///#if (HAS_M65SC02)
/*TODO*///	CPU_M65SC02,
/*TODO*///#endif
/*TODO*///#if (HAS_M65CE02)
/*TODO*///	CPU_M65CE02,
/*TODO*///#endif
/*TODO*///#if (HAS_M6509)
/*TODO*///	CPU_M6509,
/*TODO*///#endif
/*TODO*///#if (HAS_M6510)
/*TODO*///	CPU_M6510,
/*TODO*///#endif
/*TODO*///#if (HAS_M6510T)
/*TODO*///	CPU_M6510T,
/*TODO*///#endif
/*TODO*///#if (HAS_M7501)
/*TODO*///	CPU_M7501,
/*TODO*///#endif
/*TODO*///#if (HAS_M8502)
/*TODO*///	CPU_M8502,
/*TODO*///#endif
/*TODO*///#if (HAS_N2A03)
/*TODO*///	CPU_N2A03,
/*TODO*///#endif
/*TODO*///#if (HAS_M4510)
/*TODO*///	CPU_M4510,
/*TODO*///#endif
/*TODO*///#if (HAS_H6280)
/*TODO*///	CPU_H6280,
/*TODO*///#endif
/*TODO*///#if (HAS_I86)
/*TODO*///	CPU_I86,
/*TODO*///#endif
/*TODO*///#if (HAS_I88)
/*TODO*///	CPU_I88,
/*TODO*///#endif
/*TODO*///#if (HAS_I186)
/*TODO*///	CPU_I186,
/*TODO*///#endif
/*TODO*///#if (HAS_I188)
/*TODO*///	CPU_I188,
/*TODO*///#endif
/*TODO*///#if (HAS_I286)
/*TODO*///	CPU_I286,
/*TODO*///#endif
/*TODO*///#if (HAS_V20)
/*TODO*///	CPU_V20,
/*TODO*///#endif
/*TODO*///#if (HAS_V30)
/*TODO*///	CPU_V30,
/*TODO*///#endif
/*TODO*///#if (HAS_V33)
/*TODO*///	CPU_V33,
/*TODO*///#endif
/*TODO*///#if (HAS_I8035)
/*TODO*///	CPU_I8035,
/*TODO*///#endif
/*TODO*///#if (HAS_I8039)
/*TODO*///	CPU_I8039,
/*TODO*///#endif
/*TODO*///#if (HAS_I8048)
/*TODO*///	CPU_I8048,
/*TODO*///#endif
/*TODO*///#if (HAS_N7751)
/*TODO*///	CPU_N7751,
/*TODO*///#endif
/*TODO*///#if (HAS_I8X41)
/*TODO*///	CPU_I8X41,
/*TODO*///#endif
/*TODO*///#if (HAS_M6800)
/*TODO*///	CPU_M6800,
/*TODO*///#endif
/*TODO*///#if (HAS_M6801)
/*TODO*///	CPU_M6801,
/*TODO*///#endif
/*TODO*///#if (HAS_M6802)
/*TODO*///	CPU_M6802,
/*TODO*///#endif
/*TODO*///#if (HAS_M6803)
/*TODO*///	CPU_M6803,
/*TODO*///#endif
/*TODO*///#if (HAS_M6808)
/*TODO*///	CPU_M6808,
/*TODO*///#endif
/*TODO*///#if (HAS_HD63701)
/*TODO*///	CPU_HD63701,
/*TODO*///#endif
/*TODO*///#if (HAS_NSC8105)
/*TODO*///	CPU_NSC8105,
/*TODO*///#endif
/*TODO*///#if (HAS_M6805)
/*TODO*///	CPU_M6805,
/*TODO*///#endif
/*TODO*///#if (HAS_M68705)
/*TODO*///	CPU_M68705,
/*TODO*///#endif
/*TODO*///#if (HAS_HD63705)
/*TODO*///	CPU_HD63705,
/*TODO*///#endif
/*TODO*///#if (HAS_HD6309)
/*TODO*///	CPU_HD6309,
/*TODO*///#endif
/*TODO*///#if (HAS_M6809)
/*TODO*///	CPU_M6809,
/*TODO*///#endif
/*TODO*///#if (HAS_KONAMI)
/*TODO*///	CPU_KONAMI,
/*TODO*///#endif
/*TODO*///#if (HAS_M68000)
/*TODO*///	CPU_M68000,
/*TODO*///#endif
/*TODO*///#if (HAS_M68010)
/*TODO*///	CPU_M68010,
/*TODO*///#endif
/*TODO*///#if (HAS_M68EC020)
/*TODO*///	CPU_M68EC020,
/*TODO*///#endif
/*TODO*///#if (HAS_M68020)
/*TODO*///	CPU_M68020,
/*TODO*///#endif
/*TODO*///#if (HAS_T11)
/*TODO*///	CPU_T11,
/*TODO*///#endif
/*TODO*///#if (HAS_S2650)
/*TODO*///	CPU_S2650,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS34010)
/*TODO*///	CPU_TMS34010,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS34020)
/*TODO*///	CPU_TMS34020,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9900)
/*TODO*///	CPU_TMS9900,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9940)
/*TODO*///	CPU_TMS9940,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9980)
/*TODO*///	CPU_TMS9980,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9985)
/*TODO*///	CPU_TMS9985,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9989)
/*TODO*///	CPU_TMS9989,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9995)
/*TODO*///	CPU_TMS9995,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS99105A)
/*TODO*///	CPU_TMS99105A,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS99110A)
/*TODO*///	CPU_TMS99110A,
/*TODO*///#endif
/*TODO*///#if (HAS_Z8000)
/*TODO*///	CPU_Z8000,
/*TODO*///#endif
/*TODO*///#if (HAS_TMS320C10)
/*TODO*///	CPU_TMS320C10,
/*TODO*///#endif
/*TODO*///#if (HAS_CCPU)
/*TODO*///	CPU_CCPU,
/*TODO*///#endif
/*TODO*///#if (HAS_ADSP2100)
/*TODO*///	CPU_ADSP2100,
/*TODO*///#endif
/*TODO*///#if (HAS_ADSP2105)
/*TODO*///	CPU_ADSP2105,
/*TODO*///#endif
/*TODO*///#if (HAS_PSXCPU)
/*TODO*///	CPU_PSXCPU,
/*TODO*///#endif
/*TODO*///#if (HAS_ASAP)
/*TODO*///	CPU_ASAP,
/*TODO*///#endif
/*TODO*///#if (HAS_UPD7810)
/*TODO*///	CPU_UPD7810,
/*TODO*///#endif
/*TODO*///
/*TODO*///#ifdef MESS
/*TODO*///#if (HAS_APEXC)
/*TODO*///	CPU_APEXC,
/*TODO*///#endif
/*TODO*///#if (HAS_ARM)
/*TODO*///	CPU_ARM,
/*TODO*///#endif
/*TODO*///#if (HAS_CDP1802)
/*TODO*///	CPU_CDP1802,
/*TODO*///#endif
/*TODO*///#if (HAS_CP1600)
/*TODO*///	CPU_CP1600,
/*TODO*///#endif
/*TODO*///#if (HAS_F8)
/*TODO*///	CPU_F8,
/*TODO*///#endif
/*TODO*///#if (HAS_G65816)
/*TODO*///	CPU_G65816,
/*TODO*///#endif
/*TODO*///#if (HAS_LH5801)
/*TODO*///	CPU_LH5801,
/*TODO*///#endif
/*TODO*///#if (HAS_PDP1)
/*TODO*///	CPU_PDP1,
/*TODO*///#endif
/*TODO*///#if (HAS_SATURN)
/*TODO*///	CPU_SATURN,
/*TODO*///#endif
/*TODO*///#if (HAS_SC61860)
/*TODO*///	CPU_SC61860,
/*TODO*///#endif
/*TODO*///#if (HAS_SH2)
/*TODO*///	CPU_SH2,
/*TODO*///#endif
/*TODO*///#if (HAS_SPC700)
/*TODO*///	CPU_SPC700,
/*TODO*///#endif
/*TODO*///#if (HAS_Z80GB)
/*TODO*///	CPU_Z80GB,
/*TODO*///#endif
/*TODO*///#if (HAS_Z80_MSX)
/*TODO*///	CPU_Z80_MSX,
/*TODO*///#endif
/*TODO*///#endif
/*TODO*///    CPU_COUNT
/*TODO*///};
/*TODO*///
    /* set this if the CPU is used as a slave for audio. It will not be emulated if */
 /* sound is disabled, therefore speeding up a lot the emulation. */
    public static final int CPU_AUDIO_CPU = 0x8000;

    /* the Z80 can be wired to use 16 bit addressing for I/O ports */
    public static final int CPU_16BIT_PORT = 0x4000;

    public static final int CPU_FLAGS_MASK = 0xff00;

    /* The old system is obsolete and no longer supported by the core */
    public static final int NEW_INTERRUPT_SYSTEM = 1;
    public static final int MAX_IRQ_LINES = 8;/* maximum number of IRQ lines per CPU */

    public static final int CLEAR_LINE = 0;/* clear (a fired, held or pulsed) line */
    public static final int ASSERT_LINE = 1;/* assert an interrupt immediately */
    public static final int HOLD_LINE = 2;/* hold interrupt line until enable is true */
    public static final int PULSE_LINE = 3;/* pulse interrupt line for one instruction */
    public static final int MAX_REGS = 128;/* maximum number of register of any CPU */

 /* Values passed to the cpu_info function of a core to retrieve information */
    public static final int CPU_INFO_REG = 0;
    public static final int CPU_INFO_FLAGS = MAX_REGS;
    public static final int CPU_INFO_NAME = MAX_REGS + 1;
    public static final int CPU_INFO_FAMILY = MAX_REGS + 2;
    public static final int CPU_INFO_VERSION = MAX_REGS + 3;
    public static final int CPU_INFO_FILE = MAX_REGS + 4;
    public static final int CPU_INFO_CREDITS = MAX_REGS + 5;
    public static final int CPU_INFO_REG_LAYOUT = MAX_REGS + 6;
    public static final int CPU_INFO_WIN_LAYOUT = MAX_REGS + 7;

    public static final int CPU_IS_LE = 0;/* emulated CPU is little endian */
    public static final int CPU_IS_BE = 1;/* emulated CPU is big endian */

 /*
     * This value is passed to cpu_get_reg to retrieve the previous
     * program counter value, ie. before a CPU emulation started
     * to fetch opcodes and arguments for the current instrution.
     */
    public static final int REG_PREVIOUSPC = -1;
    /*
     * This value is passed to cpu_get_reg/cpu_set_reg, instead of one of
     * the names from the enum a CPU core defines for it's registers,
     * to get or set the contents of the memory pointed to by a stack pointer.
     * You can specify the n'th element on the stack by (REG_SP_CONTENTS-n),
     * ie. lower negative values. The actual element size (UINT16 or UINT32)
     * depends on the CPU core.
     * This is also used to replace the cpu_geturnpc() function.
     */
    public static final int REG_SP_CONTENTS = -2;

    public static abstract interface burnPtr {

        public abstract void handler(int cycles);
    }

    public static abstract interface irqcallbacksPtr {

        public abstract int handler(int irqline);
    }

    public static abstract class cpu_interface {

        public int cpu_num;

        public abstract void init();

        public abstract void reset(Object param);

        public abstract void exit();

        public abstract int execute(int cycles);
        public burnPtr burn;

        public abstract Object init_context(); //not in mame , used specific for arcadeflex

        public abstract Object get_context(); //different from mame returns reg object and not size since java doesn't support references

        public abstract void set_context(Object reg);

        public abstract int[] get_cycle_table(int which);

        public abstract void set_cycle_table(int which, int[] new_table);

        public abstract int get_pc();

        public abstract void set_pc(int val);

        public abstract int get_sp();

        public abstract void set_sp(int val);

        public abstract int get_reg(int regnum);

        public abstract void set_reg(int regnum, int val);

        public abstract void set_nmi_line(int linestate);

        public abstract void set_irq_line(int irqline, int linestate);

        public abstract void set_irq_callback(irqcallbacksPtr callback);

        public abstract void internal_interrupt(int type);

        public abstract String cpu_info(Object context, int regnum);
        public int num_irqs;
        public int default_vector;
        public int[] icount;
        public double overclock;
        public int no_int, irq_int, nmi_int;
        public int databus_width;

        public abstract int memory_read(int offset);

        public abstract void memory_write(int offset, int data);

        public abstract int internal_read(int offset);

        public abstract void internal_write(int offset, int data);
        public /*unsigned*/ int pgm_memory_base;

        public abstract void set_op_base(int pc);
        public int address_shift;
        public /*unsigned*/ int address_bits, endianess, align_unit, max_inst_len;

        public abstract int mem_address_bits_of_cpu();//arcadeflex function (based on the above table)
        /*	{ 16, cpu_readmem16 },
    	{ 20, cpu_readmem20 },
    	{ 21, cpu_readmem21 },
    	{ 24, cpu_readmem24 },
    
    	{ 16, cpu_readmem16bew },
    	{ 24, cpu_readmem24bew },
    	{ 32, cpu_readmem32bew },
    
    	{ 16, cpu_readmem16lew },
    	{ 17, cpu_readmem17lew },
    	{ 24, cpu_readmem24lew },
    	{ 29, cpu_readmem29lew },
    	{ 32, cpu_readmem32lew },
    
    	{ 24, cpu_readmem24bedw },
    	{ 29, cpu_readmem29bedw },
    	{ 32, cpu_readmem32bedw },
    
    	{ 26, cpu_readmem26ledw },
    	{ 29, cpu_readmem29ledw },
    	{ 32, cpu_readmem32ledw },
    
    	{ 18, cpu_readmem18bedw }*/
    };

    /* Returns previous pc (start of opcode causing read/write) */
    public static int cpu_getpreviouspc() {
        return cpu_get_reg(REG_PREVIOUSPC);
    }

    /* Returns the return address from the top of the stack (Z80 only) */
 /* int cpu_getreturnpc(void); */
 /* This can now be handled with a generic function */
    public static int cpu_geturnpc() {
        return cpu_get_reg(REG_SP_CONTENTS);
    }


    /* Load or save the game state */
    public static final int LOADSAVE_NONE = 0;
    public static final int LOADSAVE_SAVE = 1;
    public static final int LOADSAVE_LOAD = 2;

    public static abstract interface Interrupt_entryPtr {

        public abstract int handler(int i);
    }

    public static abstract interface ResetPtr {

        public abstract void handler(int i);
    }

    public static abstract interface Interrupt_retiPtr {

        public abstract void handler(int i);
    }

    /* daisy-chain link */
    public static class Z80_DaisyChain {

        public ResetPtr reset;/* reset callback     */
        public Interrupt_entryPtr interrupt_entry;/* entry callback     */
        public Interrupt_retiPtr interrupt_reti;/* reti callback      */
        public int irq_param;

        /* callback paramater */
        public Z80_DaisyChain(ResetPtr reset, Interrupt_entryPtr interrupt_entry, Interrupt_retiPtr interrupt_reti, int irq_param) {
            this.reset = reset;
            this.interrupt_entry = interrupt_entry;
            this.interrupt_reti = interrupt_reti;
            this.irq_param = irq_param;
        }
    }

    public static final int Z80_MAXDAISY = 4;/* maximum of daisy chan device */

    public static final int Z80_INT_REQ = 0x01;/* interrupt request mask       */
    public static final int Z80_INT_IEO = 0x02;/* interrupt disable mask(IEO)  */

    public static int Z80_VECTOR(int device, int state) {
        return (((device) << 8) & 0xFF | (state) & 0xFF);
    }
}
