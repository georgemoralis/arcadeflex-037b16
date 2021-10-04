/* ======================================================================== */
/* ========================= LICENSING & COPYRIGHT ======================== */
/* ======================================================================== */
/*
 *                                  MUSASHI
 *                                Version 3.3
 *
 * A portable Motorola M680x0 processor emulation engine.
 * Copyright 1998-2001 Karl Stenerud.  All rights reserved.
 *
 * This code may be freely used for non-commercial purposes as long as this
 * copyright notice remains unaltered in the source code and any binary files
 * containing this code in compiled form.
 *
 * All other lisencing terms must be negotiated with the author
 * (Karl Stenerud).
 *
 * The latest version of this code can be obtained at:
 * http://kstenerud.cjb.net
 */

 /* ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.cpu.m68000;

public class m68kH
{
/*TODO*////* ======================================================================== */
/*TODO*////* ============================ GENERAL DEFINES =========================== */
/*TODO*///
/*TODO*////* ======================================================================== */
/*TODO*///
/*TODO*////* There are 7 levels of interrupt to the 68K.
/*TODO*/// * A transition from < 7 to 7 will cause a non-maskable interrupt (NMI).
/*TODO*/// */
/*TODO*///#define M68K_IRQ_NONE 0
/*TODO*///#define M68K_IRQ_1    1
/*TODO*///#define M68K_IRQ_2    2
/*TODO*///#define M68K_IRQ_3    3
/*TODO*///#define M68K_IRQ_4    4
/*TODO*///#define M68K_IRQ_5    5
/*TODO*///#define M68K_IRQ_6    6
/*TODO*///#define M68K_IRQ_7    7
/*TODO*///
/*TODO*///
/*TODO*////* Special interrupt acknowledge values.
/*TODO*/// * Use these as special returns from the interrupt acknowledge callback
/*TODO*/// * (specified later in this header).
/*TODO*/// */

    /* Causes an interrupt autovector (0x18 + interrupt level) to be taken.
     * This happens in a real 68K if VPA or AVEC is asserted during an interrupt
     * acknowledge cycle instead of DTACK.
     */
    public static final int M68K_INT_ACK_AUTOVECTOR    = 0xffffffff;

/*TODO*////* Causes the spurious interrupt vector (0x18) to be taken
/*TODO*/// * This happens in a real 68K if BERR is asserted during the interrupt
/*TODO*/// * acknowledge cycle (i.e. no devices responded to the acknowledge).
/*TODO*/// */
/*TODO*///#define M68K_INT_ACK_SPURIOUS      0xfffffffe
/*TODO*///
/*TODO*///
/*TODO*////* CPU types for use in m68k_set_cpu_type() */
/*TODO*///enum
/*TODO*///{
	public static final int M68K_CPU_TYPE_INVALID   = 0;
	public static final int M68K_CPU_TYPE_68000     = 1;
	public static final int M68K_CPU_TYPE_68010     = 2;
	public static final int M68K_CPU_TYPE_68EC020   = 3;
	public static final int M68K_CPU_TYPE_68020     = 4;
	public static final int M68K_CPU_TYPE_68030     = 5;	/* Supported by disassembler ONLY */
	public static final int M68K_CPU_TYPE_68040     = 6;	/* Supported by disassembler ONLY */
/*TODO*///};
/*TODO*///
/*TODO*////* Registers used by m68k_get_reg() and m68k_set_reg() */
/*TODO*///typedef enum
/*TODO*///{
/*TODO*///	/* Real registers */
/*TODO*///	M68K_REG_D0,		/* Data registers */
/*TODO*///	M68K_REG_D1,
/*TODO*///	M68K_REG_D2,
/*TODO*///	M68K_REG_D3,
/*TODO*///	M68K_REG_D4,
/*TODO*///	M68K_REG_D5,
/*TODO*///	M68K_REG_D6,
/*TODO*///	M68K_REG_D7,
/*TODO*///	M68K_REG_A0,		/* Address registers */
/*TODO*///	M68K_REG_A1,
/*TODO*///	M68K_REG_A2,
/*TODO*///	M68K_REG_A3,
/*TODO*///	M68K_REG_A4,
/*TODO*///	M68K_REG_A5,
/*TODO*///	M68K_REG_A6,
/*TODO*///	M68K_REG_A7,
/*TODO*///	M68K_REG_PC,		/* Program Counter */
/*TODO*///	M68K_REG_SR,		/* Status Register */
/*TODO*///	M68K_REG_SP,		/* The current Stack Pointer (located in A7) */
/*TODO*///	M68K_REG_USP,		/* User Stack Pointer */
/*TODO*///	M68K_REG_ISP,		/* Interrupt Stack Pointer */
/*TODO*///	M68K_REG_MSP,		/* Master Stack Pointer */
/*TODO*///	M68K_REG_SFC,		/* Source Function Code */
/*TODO*///	M68K_REG_DFC,		/* Destination Function Code */
/*TODO*///	M68K_REG_VBR,		/* Vector Base Register */
/*TODO*///	M68K_REG_CACR,		/* Cache Control Register */
/*TODO*///	M68K_REG_CAAR,		/* Cache Address Register */
/*TODO*///
/*TODO*///	/* Assumed registers */
/*TODO*///	/* These are cheat registers which emulate the 1-longword prefetch
/*TODO*///	 * present in the 68000 and 68010.
/*TODO*///	 */
/*TODO*///	M68K_REG_PREF_ADDR,	/* Last prefetch address */
/*TODO*///	M68K_REG_PREF_DATA,	/* Last prefetch data */
/*TODO*///
/*TODO*///	/* Convenience registers */
/*TODO*///	M68K_REG_PPC,		/* Previous value in the program counter */
/*TODO*///	M68K_REG_IR,		/* Instruction register */
/*TODO*///	M68K_REG_CPU_TYPE	/* Type of CPU being run */
/*TODO*///} m68k_register_t;
/*TODO*///
/*TODO*////* ======================================================================== */
/*TODO*////* ====================== FUNCTIONS CALLED BY THE CPU ===================== */
/*TODO*////* ======================================================================== */
/*TODO*///
/*TODO*////* You will have to implement these functions */
/*TODO*///
/*TODO*////* read/write functions called by the CPU to access memory.
/*TODO*/// * while values used are 32 bits, only the appropriate number
/*TODO*/// * of bits are relevant (i.e. in write_memory_8, only the lower 8 bits
/*TODO*/// * of value should be written to memory).
/*TODO*/// *
/*TODO*/// * NOTE: I have separated the immediate and PC-relative memory fetches
/*TODO*/// *       from the other memory fetches because some systems require
/*TODO*/// *       differentiation between PROGRAM and DATA fetches (usually
/*TODO*/// *       for security setups such as encryption).
/*TODO*/// *       This separation can either be achieved by setting
/*TODO*/// *       M68K_SEPARATE_READS in m68kconf.h and defining
/*TODO*/// *       the read functions, or by setting M68K_EMULATE_FC and
/*TODO*/// *       making a function code callback function.
/*TODO*/// *       Using the callback offers better emulation coverage
/*TODO*/// *       because you can also monitor whether the CPU is in SYSTEM or
/*TODO*/// *       USER mode, but it is also slower.
/*TODO*/// */
/*TODO*///
/*TODO*////* Read from anywhere */
/*TODO*///unsigned int  m68k_read_memory_8(unsigned int address);
/*TODO*///unsigned int  m68k_read_memory_16(unsigned int address);
/*TODO*///unsigned int  m68k_read_memory_32(unsigned int address);
/*TODO*///
/*TODO*////* Read data immediately following the PC */
/*TODO*///INLINE unsigned int  m68k_read_immediate_16(unsigned int address);
/*TODO*///INLINE unsigned int  m68k_read_immediate_32(unsigned int address);
/*TODO*///
/*TODO*////* Read data relative to the PC */
/*TODO*///INLINE unsigned int  m68k_read_pcrelative_8(unsigned int address);
/*TODO*///INLINE unsigned int  m68k_read_pcrelative_16(unsigned int address);
/*TODO*///INLINE unsigned int  m68k_read_pcrelative_32(unsigned int address);
/*TODO*///
/*TODO*////* Memory access for the disassembler */
/*TODO*///unsigned int m68k_read_disassembler_8  (unsigned int address);
/*TODO*///unsigned int m68k_read_disassembler_16 (unsigned int address);
/*TODO*///unsigned int m68k_read_disassembler_32 (unsigned int address);
/*TODO*///
/*TODO*////* Write to anywhere */
/*TODO*///void m68k_write_memory_8(unsigned int address, unsigned int value);
/*TODO*///void m68k_write_memory_16(unsigned int address, unsigned int value);
/*TODO*///void m68k_write_memory_32(unsigned int address, unsigned int value);
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////* ======================================================================== */
/*TODO*////* ============================== CALLBACKS =============================== */
/*TODO*////* ======================================================================== */
/*TODO*///
/*TODO*////* These functions allow you to set callbacks to the host when specific events
/*TODO*/// * occur.  Note that you must enable the corresponding value in m68kconf.h
/*TODO*/// * in order for these to do anything useful.
/*TODO*/// * Note: I have defined default callbacks which are used if you have enabled
/*TODO*/// * the corresponding #define in m68kconf.h but either haven't assigned a
/*TODO*/// * callback or have assigned a callback of NULL.
/*TODO*/// */
/*TODO*///
/*TODO*////* Set the callback for an interrupt acknowledge.
/*TODO*/// * You must enable M68K_EMULATE_INT_ACK in m68kconf.h.
/*TODO*/// * The CPU will call the callback with the interrupt level being acknowledged.
/*TODO*/// * The host program must return either a vector from 0x02-0xff, or one of the
/*TODO*/// * special interrupt acknowledge values specified earlier in this header.
/*TODO*/// * If this is not implemented, the CPU will always assume an autovectored
/*TODO*/// * interrupt, and will automatically clear the interrupt request when it
/*TODO*/// * services the interrupt.
/*TODO*/// * Default behavior: return M68K_INT_ACK_AUTOVECTOR.
/*TODO*/// */
/*TODO*///void m68k_set_int_ack_callback(int  (*callback)(int int_level));
/*TODO*///
/*TODO*///
/*TODO*////* Set the callback for a breakpoint acknowledge (68010+).
/*TODO*/// * You must enable M68K_EMULATE_BKPT_ACK in m68kconf.h.
/*TODO*/// * The CPU will call the callback with whatever was in the data field of the
/*TODO*/// * BKPT instruction for 68020+, or 0 for 68010.
/*TODO*/// * Default behavior: do nothing.
/*TODO*/// */
/*TODO*///void m68k_set_bkpt_ack_callback(void (*callback)(unsigned int data));
/*TODO*///
/*TODO*///
/*TODO*////* Set the callback for the RESET instruction.
/*TODO*/// * You must enable M68K_EMULATE_RESET in m68kconf.h.
/*TODO*/// * The CPU calls this callback every time it encounters a RESET instruction.
/*TODO*/// * Default behavior: do nothing.
/*TODO*/// */
/*TODO*///void m68k_set_reset_instr_callback(void  (*callback)(void));
/*TODO*///
/*TODO*///
/*TODO*////* Set the callback for informing of a large PC change.
/*TODO*/// * You must enable M68K_MONITOR_PC in m68kconf.h.
/*TODO*/// * The CPU calls this callback with the new PC value every time the PC changes
/*TODO*/// * by a large value (currently set for changes by longwords).
/*TODO*/// * Default behavior: do nothing.
/*TODO*/// */
/*TODO*///void m68k_set_pc_changed_callback(void  (*callback)(unsigned int new_pc));
/*TODO*///
/*TODO*///
/*TODO*////* Set the callback for CPU function code changes.
/*TODO*/// * You must enable M68K_EMULATE_FC in m68kconf.h.
/*TODO*/// * The CPU calls this callback with the function code before every memory
/*TODO*/// * access to set the CPU's function code according to what kind of memory
/*TODO*/// * access it is (supervisor/user, program/data and such).
/*TODO*/// * Default behavior: do nothing.
/*TODO*/// */
/*TODO*///void m68k_set_fc_callback(void  (*callback)(unsigned int new_fc));
/*TODO*///
/*TODO*///
/*TODO*////* Set a callback for the instruction cycle of the CPU.
/*TODO*/// * You must enable M68K_INSTRUCTION_HOOK in m68kconf.h.
/*TODO*/// * The CPU calls this callback just before fetching the opcode in the
/*TODO*/// * instruction cycle.
/*TODO*/// * Default behavior: do nothing.
/*TODO*/// */
/*TODO*///void m68k_set_instr_hook_callback(void  (*callback)(void));
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////* ======================================================================== */
/*TODO*////* ====================== FUNCTIONS TO ACCESS THE CPU ===================== */
/*TODO*////* ======================================================================== */
/*TODO*///
/*TODO*////* Use this function to set the CPU type you want to emulate.
/*TODO*/// * Currently supported types are: M68K_CPU_TYPE_68000, M68K_CPU_TYPE_68010,
/*TODO*/// * M68K_CPU_TYPE_EC020, and M68K_CPU_TYPE_68020.
/*TODO*/// */
/*TODO*///void m68k_set_cpu_type(unsigned int cpu_type);
/*TODO*///
/*TODO*////* Do whatever initialisations the core requires.  Should be called
/*TODO*/// * at least once at init time.
/*TODO*/// */
/*TODO*///
/*TODO*////* Pulse the RESET pin on the CPU.
/*TODO*/// * You *MUST* reset the CPU at least once to initialize the emulation
/*TODO*/// * Note: If you didn't call m68k_set_cpu_type() before resetting
/*TODO*/// *       the CPU for the first time, the CPU will be set to
/*TODO*/// *       M68K_CPU_TYPE_68000.
/*TODO*/// */
/*TODO*///
/*TODO*////* execute num_cycles worth of instructions.  returns number of cycles used */
/*TODO*///int m68k_execute(int num_cycles);
/*TODO*///
/*TODO*////* These functions let you read/write/modify the number of cycles left to run
/*TODO*/// * while m68k_execute() is running.
/*TODO*/// * These are useful if the 68k accesses a memory-mapped port on another device
/*TODO*/// * that requires immediate processing by another CPU.
/*TODO*/// */
/*TODO*///void m68k_modify_timeslice(int cycles); /* Modify cycles left */
/*TODO*///
/*TODO*////* Set the IPL0-IPL2 pins on the CPU (IRQ).
/*TODO*/// * A transition from < 7 to 7 will cause a non-maskable interrupt (NMI).
/*TODO*/// * Setting IRQ to 0 will clear an interrupt request.
/*TODO*/// */
/*TODO*///void m68k_set_irq(unsigned int int_level);
/*TODO*///
/*TODO*///
/*TODO*////* Halt the CPU as if you pulsed the HALT pin. */
/*TODO*///
/*TODO*///
/*TODO*////* Context switching to allow multiple CPUs */
/*TODO*///
/*TODO*////* Get the size of the cpu context in bytes */
/*TODO*///unsigned 
/*TODO*////* Get a cpu context */
/*TODO*///unsigned int m68k_get_context(void* dst);
/*TODO*///
/*TODO*////* set the current cpu context */
/*TODO*///void m68k_set_context(void* dst);
/*TODO*///
/*TODO*////* Register the CPU state information */
/*TODO*///void m68k_state_register(const char *type);
/*TODO*///
/*TODO*///
/*TODO*////* Peek at the internals of a CPU context.  This can either be a context
/*TODO*/// * retrieved using m68k_get_context() or the currently running context.
/*TODO*/// * If context is NULL, the currently running CPU context will be used.
/*TODO*/// */
/*TODO*///unsigned int m68k_get_reg(void* context, m68k_register_t reg);
/*TODO*///
/*TODO*////* Poke values into the internals of the currently running CPU context */
/*TODO*///void m68k_set_reg(m68k_register_t reg, unsigned int value);
/*TODO*///
/*TODO*////* Check if an instruction is valid for the specified CPU type */
/*TODO*///unsigned int m68k_is_valid_instruction(unsigned int instruction, unsigned int cpu_type);
/*TODO*///
/*TODO*////* Disassemble 1 instruction using the epecified CPU type at pc.  Stores
/*TODO*/// * disassembly in str_buff and returns the size of the instruction in bytes.
/*TODO*/// */
/*TODO*///unsigned int m68k_disassemble(char* str_buff, unsigned int pc, unsigned int cpu_type);
/*TODO*///
/*TODO*///
/*TODO*////* ======================================================================== */
/*TODO*////* ============================= CONFIGURATION ============================ */
/*TODO*////* ======================================================================== */
/*TODO*///
/*TODO*////* Import the configuration for this build */
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ======================================================================== */
/*TODO*///	/* ============================== END OF FILE ============================= */
/*TODO*///	/* ======================================================================== */
/*TODO*///	
/*TODO*///	#endif /* M68K__HEADER */
}
