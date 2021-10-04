/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.cpu.m68000;

import static gr.codebb.arcadeflex.WIP.v037b16.cpu.m68000.m68kcpu.m68ki_cpu;
import gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.irqcallbacksPtr;

public class m68kcpuH
{
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
	
    
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	#ifndef M68KCPU__HEADER
/*TODO*///	#define M68KCPU__HEADER
/*TODO*///	
/*TODO*///	
/*TODO*///	#if M68K_EMULATE_ADDRESS_ERROR
/*TODO*///	#endif /* M68K_EMULATE_ADDRESS_ERROR */
/*TODO*///	
/*TODO*///	/* ======================================================================== */
/*TODO*///	/* ==================== ARCHITECTURE-DEPENDANT DEFINES ==================== */
/*TODO*///	/* ======================================================================== */
/*TODO*///	
/*TODO*///	/* Check for > 32bit sizes */
/*TODO*///	#if UINT_MAX > 0xffffffff
/*TODO*///		#define M68K_INT_GT_32_BIT  1
/*TODO*///	#else
/*TODO*///		#define M68K_INT_GT_32_BIT  0
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	/* Data types used in this emulation core */
/*TODO*///	#undef sint8
/*TODO*///	#undef sint16
/*TODO*///	#undef sint32
/*TODO*///	#undef sint64
/*TODO*///	#undef uint8
/*TODO*///	#undef uint16
/*TODO*///	#undef uint32
/*TODO*///	#undef uint64
/*TODO*///	#undef sint
/*TODO*///	#undef uint
/*TODO*///	
/*TODO*///	#define sint8  signed   char			/* ASG: changed from char to signed char */
/*TODO*///	#define sint16 signed   short
/*TODO*///	#define sint32 signed   long
/*TODO*///	#define uint8  unsigned char
/*TODO*///	#define uint16 unsigned short
/*TODO*///	#define uint32 unsigned long
/*TODO*///	
/*TODO*///	/* signed and unsigned int must be at least 32 bits wide */
/*TODO*///	#define sint   signed   int
/*TODO*///	#define uint   unsigned int
/*TODO*///	
/*TODO*///	
/*TODO*///	#if M68K_USE_64_BIT
/*TODO*///	#define sint64 signed   long long
/*TODO*///	#define uint64 unsigned long long
/*TODO*///	#else
/*TODO*///	#define sint64 sint32
/*TODO*///	#define uint64 uint32
/*TODO*///	#endif /* M68K_USE_64_BIT */
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Allow for architectures that don't have 8-bit sizes */
/*TODO*///	#if UCHAR_MAX == 0xff
/*TODO*///		#define MAKE_INT_8(A) (sint8)(A)
/*TODO*///	#else
/*TODO*///		#undef  sint8
/*TODO*///		#define sint8  signed   int
/*TODO*///		#undef  uint8
/*TODO*///		#define uint8  unsigned int
/*TODO*///		INLINE sint MAKE_INT_8(uint value)
/*TODO*///		{
/*TODO*///			return (value & 0x80) ? value | ~0xff : value & 0xff;
/*TODO*///		}
/*TODO*///	#endif /* UCHAR_MAX == 0xff */
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Allow for architectures that don't have 16-bit sizes */
/*TODO*///	#if USHRT_MAX == 0xffff
/*TODO*///		#define MAKE_INT_16(A) (sint16)(A)
/*TODO*///	#else
/*TODO*///		#undef  sint16
/*TODO*///		#define sint16 signed   int
/*TODO*///		#undef  uint16
/*TODO*///		#define uint16 unsigned int
/*TODO*///		INLINE sint MAKE_INT_16(uint value)
/*TODO*///		{
/*TODO*///			return (value & 0x8000) ? value | ~0xffff : value & 0xffff;
/*TODO*///		}
/*TODO*///	#endif /* USHRT_MAX == 0xffff */
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Allow for architectures that don't have 32-bit sizes */
/*TODO*///	#if ULONG_MAX == 0xffffffff
/*TODO*///		#define MAKE_INT_32(A) (sint32)(A)
/*TODO*///	#else
/*TODO*///		#undef  sint32
/*TODO*///		#define sint32  signed   int
/*TODO*///		#undef  uint32
/*TODO*///		#define uint32  unsigned int
/*TODO*///		INLINE sint MAKE_INT_32(uint value)
/*TODO*///		{
/*TODO*///			return (value & 0x80000000) ? value | ~0xffffffff : value & 0xffffffff;
/*TODO*///		}
/*TODO*///	#endif /* ULONG_MAX == 0xffffffff */
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ======================================================================== */
/*TODO*///	/* ============================ GENERAL DEFINES =========================== */
/*TODO*///	/* ======================================================================== */
/*TODO*///	
/*TODO*///	/* Exception Vectors handled by emulation */
/*TODO*///	#define EXCEPTION_BUS_ERROR                2 /* This one is not emulated! */
/*TODO*///	#define EXCEPTION_ADDRESS_ERROR            3 /* This one is partially emulated (doesn't stack a proper frame yet) */
/*TODO*///	#define EXCEPTION_ILLEGAL_INSTRUCTION      4
/*TODO*///	#define EXCEPTION_ZERO_DIVIDE              5
/*TODO*///	#define EXCEPTION_CHK                      6
/*TODO*///	#define EXCEPTION_TRAPV                    7
/*TODO*///	#define EXCEPTION_PRIVILEGE_VIOLATION      8
/*TODO*///	#define EXCEPTION_TRACE                    9
/*TODO*///	#define EXCEPTION_1010                    10
/*TODO*///	#define EXCEPTION_1111                    11
/*TODO*///	#define EXCEPTION_FORMAT_ERROR            14
/*TODO*///	#define EXCEPTION_UNINITIALIZED_INTERRUPT 15
/*TODO*///	#define EXCEPTION_SPURIOUS_INTERRUPT      24
/*TODO*///	#define EXCEPTION_INTERRUPT_AUTOVECTOR    24
/*TODO*///	#define EXCEPTION_TRAP_BASE               32
/*TODO*///	
/*TODO*///	/* Function codes set by CPU during data/address bus activity */
/*TODO*///	#define FUNCTION_CODE_USER_DATA          1
/*TODO*///	#define FUNCTION_CODE_USER_PROGRAM       2
/*TODO*///	#define FUNCTION_CODE_SUPERVISOR_DATA    5
/*TODO*///	#define FUNCTION_CODE_SUPERVISOR_PROGRAM 6
/*TODO*///	#define FUNCTION_CODE_CPU_SPACE          7
	
	/* CPU types for deciding what to emulate */
	public static final int CPU_TYPE_000   = 1;
	public static final int CPU_TYPE_010   = 2;
	public static final int CPU_TYPE_EC020 = 4;
	public static final int CPU_TYPE_020   = 8;
	
/*TODO*///	/* Different ways to stop the CPU */
/*TODO*///	#define STOP_LEVEL_STOP 1
/*TODO*///	#define STOP_LEVEL_HALT 2
/*TODO*///	
/*TODO*///	#ifndef NULL
/*TODO*///	#define NULL ((void*)0)
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	/* ======================================================================== */
/*TODO*///	/* ================================ MACROS ================================ */
/*TODO*///	/* ======================================================================== */
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ---------------------------- General Macros ---------------------------- */
/*TODO*///	
/*TODO*///	/* Bit Isolation Macros */
/*TODO*///	#define BIT_0(A)  ((A) & 0x00000001)
/*TODO*///	#define BIT_1(A)  ((A) & 0x00000002)
/*TODO*///	#define BIT_2(A)  ((A) & 0x00000004)
/*TODO*///	#define BIT_3(A)  ((A) & 0x00000008)
/*TODO*///	#define BIT_4(A)  ((A) & 0x00000010)
/*TODO*///	#define BIT_5(A)  ((A) & 0x00000020)
/*TODO*///	#define BIT_6(A)  ((A) & 0x00000040)
/*TODO*///	#define BIT_7(A)  ((A) & 0x00000080)
/*TODO*///	#define BIT_8(A)  ((A) & 0x00000100)
/*TODO*///	#define BIT_9(A)  ((A) & 0x00000200)
/*TODO*///	#define BIT_A(A)  ((A) & 0x00000400)
/*TODO*///	#define BIT_B(A)  ((A) & 0x00000800)
/*TODO*///	#define BIT_C(A)  ((A) & 0x00001000)
/*TODO*///	#define BIT_D(A)  ((A) & 0x00002000)
/*TODO*///	#define BIT_E(A)  ((A) & 0x00004000)
/*TODO*///	#define BIT_F(A)  ((A) & 0x00008000)
/*TODO*///	#define BIT_10(A) ((A) & 0x00010000)
/*TODO*///	#define BIT_11(A) ((A) & 0x00020000)
/*TODO*///	#define BIT_12(A) ((A) & 0x00040000)
/*TODO*///	#define BIT_13(A) ((A) & 0x00080000)
/*TODO*///	#define BIT_14(A) ((A) & 0x00100000)
/*TODO*///	#define BIT_15(A) ((A) & 0x00200000)
/*TODO*///	#define BIT_16(A) ((A) & 0x00400000)
/*TODO*///	#define BIT_17(A) ((A) & 0x00800000)
/*TODO*///	#define BIT_18(A) ((A) & 0x01000000)
/*TODO*///	#define BIT_19(A) ((A) & 0x02000000)
/*TODO*///	#define BIT_1A(A) ((A) & 0x04000000)
/*TODO*///	#define BIT_1B(A) ((A) & 0x08000000)
/*TODO*///	#define BIT_1C(A) ((A) & 0x10000000)
/*TODO*///	#define BIT_1D(A) ((A) & 0x20000000)
/*TODO*///	#define BIT_1E(A) ((A) & 0x40000000)
/*TODO*///	#define BIT_1F(A) ((A) & 0x80000000)
/*TODO*///	
/*TODO*///	/* Get the most significant bit for specific sizes */
/*TODO*///	#define GET_MSB_8(A)  ((A) & 0x80)
/*TODO*///	#define GET_MSB_9(A)  ((A) & 0x100)
/*TODO*///	#define GET_MSB_16(A) ((A) & 0x8000)
/*TODO*///	#define GET_MSB_17(A) ((A) & 0x10000)
/*TODO*///	#define GET_MSB_32(A) ((A) & 0x80000000)
/*TODO*///	#if M68K_USE_64_BIT
/*TODO*///	#define GET_MSB_33(A) ((A) & 0x100000000)
/*TODO*///	#endif /* M68K_USE_64_BIT */
/*TODO*///	
/*TODO*///	/* Isolate nibbles */
/*TODO*///	#define LOW_NIBBLE(A)  ((A) & 0x0f)
/*TODO*///	#define HIGH_NIBBLE(A) ((A) & 0xf0)
/*TODO*///	
/*TODO*///	/* These are used to isolate 8, 16, and 32 bit sizes */
/*TODO*///	#define MASK_OUT_ABOVE_2(A)  ((A) & 3)
/*TODO*///	#define MASK_OUT_ABOVE_8(A)  ((A) & 0xff)
/*TODO*///	#define MASK_OUT_ABOVE_16(A) ((A) & 0xffff)
/*TODO*///	#define MASK_OUT_BELOW_2(A)  ((A) & ~3)
/*TODO*///	#define MASK_OUT_BELOW_8(A)  ((A) & ~0xff)
/*TODO*///	#define MASK_OUT_BELOW_16(A) ((A) & ~0xffff)
/*TODO*///	
/*TODO*///	/* No need to mask if we are 32 bit */
/*TODO*///	#if M68K_INT_GT_32_BIT || M68K_USE_64_BIT
/*TODO*///		#define MASK_OUT_ABOVE_32(A) ((A) & 0xffffffff)
/*TODO*///		#define MASK_OUT_BELOW_32(A) ((A) & ~0xffffffff)
/*TODO*///	#else
/*TODO*///		#define MASK_OUT_ABOVE_32(A) (A)
/*TODO*///		#define MASK_OUT_BELOW_32(A) 0
/*TODO*///	#endif /* M68K_INT_GT_32_BIT || M68K_USE_64_BIT */
/*TODO*///	
/*TODO*///	/* Simulate address lines of 68k family */
/*TODO*///	#define ADDRESS_68K(A) ((A)&CPU_ADDRESS_MASK)
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Shift & Rotate Macros. */
/*TODO*///	#define LSL(A, C) ((A) << (C))
/*TODO*///	#define LSR(A, C) ((A) >> (C))
/*TODO*///	
/*TODO*///	/* Some > 32-bit optimizations */
/*TODO*///	#if M68K_INT_GT_32_BIT
/*TODO*///		/* Shift left and right */
/*TODO*///		#define LSR_32(A, C) ((A) >> (C))
/*TODO*///		#define LSL_32(A, C) ((A) << (C))
/*TODO*///	#else
/*TODO*///		/* We have to do this because the morons at ANSI decided that shifts
/*TODO*///		 * by >= data size are undefined.
/*TODO*///		 */
/*TODO*///		#define LSR_32(A, C) ((C) < 32 ? (A) >> (C) : 0)
/*TODO*///		#define LSL_32(A, C) ((C) < 32 ? (A) << (C) : 0)
/*TODO*///	#endif /* M68K_INT_GT_32_BIT */
/*TODO*///	
/*TODO*///	#if M68K_USE_64_BIT
/*TODO*///		#define LSL_32_64(A, C) ((A) << (C))
/*TODO*///		#define LSR_32_64(A, C) ((A) >> (C))
/*TODO*///		#define ROL_33_64(A, C) (LSL_32_64(A, C) | LSR_32_64(A, 33-(C)))
/*TODO*///		#define ROR_33_64(A, C) (LSR_32_64(A, C) | LSL_32_64(A, 33-(C)))
/*TODO*///	#endif /* M68K_USE_64_BIT */
/*TODO*///	
/*TODO*///	#define ROL_8(A, C)      MASK_OUT_ABOVE_8(LSL(A, C) | LSR(A, 8-(C)))
/*TODO*///	#define ROL_9(A, C)                      (LSL(A, C) | LSR(A, 9-(C)))
/*TODO*///	#define ROL_16(A, C)    MASK_OUT_ABOVE_16(LSL(A, C) | LSR(A, 16-(C)))
/*TODO*///	#define ROL_17(A, C)                     (LSL(A, C) | LSR(A, 17-(C)))
/*TODO*///	#define ROL_32(A, C)    MASK_OUT_ABOVE_32(LSL_32(A, C) | LSR_32(A, 32-(C)))
/*TODO*///	#define ROL_33(A, C)                     (LSL_32(A, C) | LSR_32(A, 33-(C)))
/*TODO*///	
/*TODO*///	#define ROR_8(A, C)      MASK_OUT_ABOVE_8(LSR(A, C) | LSL(A, 8-(C)))
/*TODO*///	#define ROR_9(A, C)                      (LSR(A, C) | LSL(A, 9-(C)))
/*TODO*///	#define ROR_16(A, C)    MASK_OUT_ABOVE_16(LSR(A, C) | LSL(A, 16-(C)))
/*TODO*///	#define ROR_17(A, C)                     (LSR(A, C) | LSL(A, 17-(C)))
/*TODO*///	#define ROR_32(A, C)    MASK_OUT_ABOVE_32(LSR_32(A, C) | LSL_32(A, 32-(C)))
/*TODO*///	#define ROR_33(A, C)                     (LSR_32(A, C) | LSL_32(A, 33-(C)))
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ------------------------------ CPU Access ------------------------------ */
/*TODO*///	
/*TODO*///	/* Access the CPU registers */
/*TODO*///	#define CPU_TYPE         m68ki_cpu.cpu_type
/*TODO*///	
/*TODO*///	#define REG_DA           m68ki_cpu.dar /* easy access to data and address regs */
/*TODO*///	#define REG_D            m68ki_cpu.dar
/*TODO*///	#define REG_A            (m68ki_cpu.dar+8)
/*TODO*///	#define REG_PPC 		 m68ki_cpu.ppc
/*TODO*///	#define REG_PC           m68ki_cpu.pc
/*TODO*///	#define REG_SP_BASE      m68ki_cpu.sp
/*TODO*///	#define REG_USP          m68ki_cpu.sp[0]
/*TODO*///	#define REG_ISP          m68ki_cpu.sp[4]
/*TODO*///	#define REG_MSP          m68ki_cpu.sp[6]
/*TODO*///	#define REG_SP           m68ki_cpu.dar[15]
/*TODO*///	#define REG_VBR          m68ki_cpu.vbr
/*TODO*///	#define REG_SFC          m68ki_cpu.sfc
/*TODO*///	#define REG_DFC          m68ki_cpu.dfc
/*TODO*///	#define REG_CACR         m68ki_cpu.cacr
/*TODO*///	#define REG_CAAR         m68ki_cpu.caar
/*TODO*///	#define REG_IR           m68ki_cpu.ir
/*TODO*///	
/*TODO*///	#define FLAG_T1          m68ki_cpu.t1_flag
/*TODO*///	#define FLAG_T0          m68ki_cpu.t0_flag
/*TODO*///	#define FLAG_S           m68ki_cpu.s_flag
/*TODO*///	#define FLAG_M           m68ki_cpu.m_flag
/*TODO*///	#define FLAG_X           m68ki_cpu.x_flag
/*TODO*///	#define FLAG_N           m68ki_cpu.n_flag
/*TODO*///	#define FLAG_Z           m68ki_cpu.not_z_flag
/*TODO*///	#define FLAG_V           m68ki_cpu.v_flag
/*TODO*///	#define FLAG_C           m68ki_cpu.c_flag
/*TODO*///	#define FLAG_INT_MASK    m68ki_cpu.int_mask
/*TODO*///	
/*TODO*///	#define CPU_INT_LEVEL    m68ki_cpu.int_level /* ASG: changed from CPU_INTS_PENDING */
/*TODO*///	#define CPU_INT_CYCLES   m68ki_cpu.int_cycles /* ASG */
/*TODO*///	#define CPU_STOPPED      m68ki_cpu.stopped
/*TODO*///	#define CPU_PREF_ADDR    m68ki_cpu.pref_addr
/*TODO*///	#define CPU_PREF_DATA    m68ki_cpu.pref_data
/*TODO*///	#define CPU_ADDRESS_MASK m68ki_cpu.address_mask
/*TODO*///	#define CPU_SR_MASK      m68ki_cpu.sr_mask
/*TODO*///	
/*TODO*///	#define CYC_INSTRUCTION  m68ki_cpu.cyc_instruction
/*TODO*///	#define CYC_EXCEPTION    m68ki_cpu.cyc_exception
/*TODO*///	#define CYC_BCC_NOTAKE_B m68ki_cpu.cyc_bcc_notake_b
/*TODO*///	#define CYC_BCC_NOTAKE_W m68ki_cpu.cyc_bcc_notake_w
/*TODO*///	#define CYC_DBCC_F_NOEXP m68ki_cpu.cyc_dbcc_f_noexp
/*TODO*///	#define CYC_DBCC_F_EXP   m68ki_cpu.cyc_dbcc_f_exp
/*TODO*///	#define CYC_SCC_R_FALSE  m68ki_cpu.cyc_scc_r_false
/*TODO*///	#define CYC_MOVEM_W      m68ki_cpu.cyc_movem_w
/*TODO*///	#define CYC_MOVEM_L      m68ki_cpu.cyc_movem_l
/*TODO*///	#define CYC_SHIFT        m68ki_cpu.cyc_shift
/*TODO*///	#define CYC_RESET        m68ki_cpu.cyc_reset
/*TODO*///	
/*TODO*///	
        public static abstract interface bkpt_ack_callbackPtr {
        public abstract void handler(int data);
    }

    public static abstract interface reset_instr_callbackPtr {
        public abstract void handler();
    }

    public static abstract interface pc_changed_callbackPtr {
        public abstract void handler(int new_pc);
    }

    public static abstract interface set_fc_callbackPtr {
        public abstract void handler(int new_fc);
    }

    public static abstract interface instr_hook_callbackPtr {
        public abstract void handler();
    }

    public static void set_CPU_INT_ACK_CALLBACK(irqcallbacksPtr int_ack_callback) {
        m68ki_cpu.int_ack_callback = int_ack_callback;
    }
    
    public static void set_CALLBACK_BKPT_ACK(bkpt_ack_callbackPtr bkpt_ack_callback) {    
        m68ki_cpu.bkpt_ack_callback = bkpt_ack_callback;
    }
    
    public static void set_CALLBACK_RESET_INSTR(reset_instr_callbackPtr reset_instr_callback) {
        m68ki_cpu.reset_instr_callback = reset_instr_callback;
    }
    
    public static void set_CALLBACK_PC_CHANGED(pc_changed_callbackPtr pc_changed_callback) {
            m68ki_cpu.pc_changed_callback = pc_changed_callback;
    }
    
    public static void set_CALLBACK_SET_FC(set_fc_callbackPtr set_fc_callback) {
        m68ki_cpu.set_fc_callback = set_fc_callback;
    }
    
    public static void set_CALLBACK_INSTR_HOOK(instr_hook_callbackPtr instr_hook_callback) {
        m68ki_cpu.instr_hook_callback = instr_hook_callback;
    }
	
	
	
/*TODO*///	/* ----------------------------- Configuration ---------------------------- */
/*TODO*///	
/*TODO*///	/* These defines are dependant on the configuration defines in m68kconf.h */
/*TODO*///	
/*TODO*///	/* Disable certain comparisons if we're not using all CPU types */
/*TODO*///	#if M68K_EMULATE_020
/*TODO*///		#define CPU_TYPE_IS_020_PLUS(A)    ((A) & CPU_TYPE_020)
/*TODO*///		#define CPU_TYPE_IS_020_LESS(A)    1
/*TODO*///	#else
/*TODO*///		#define CPU_TYPE_IS_020_PLUS(A)    0
/*TODO*///		#define CPU_TYPE_IS_020_LESS(A)    1
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#if M68K_EMULATE_EC020
/*TODO*///		#define CPU_TYPE_IS_EC020_PLUS(A)  ((A) & (CPU_TYPE_EC020 | CPU_TYPE_020))
/*TODO*///		#define CPU_TYPE_IS_EC020_LESS(A)  ((A) & (CPU_TYPE_000 | CPU_TYPE_010 | CPU_TYPE_EC020))
/*TODO*///	#else
/*TODO*///		#define CPU_TYPE_IS_EC020_PLUS(A)  CPU_TYPE_IS_020_PLUS(A)
/*TODO*///		#define CPU_TYPE_IS_EC020_LESS(A)  CPU_TYPE_IS_020_LESS(A)
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#if M68K_EMULATE_010
/*TODO*///		#define CPU_TYPE_IS_010(A)         ((A) == CPU_TYPE_010)
/*TODO*///		#define CPU_TYPE_IS_010_PLUS(A)    ((A) & (CPU_TYPE_010 | CPU_TYPE_EC020 | CPU_TYPE_020))
/*TODO*///		#define CPU_TYPE_IS_010_LESS(A)    ((A) & (CPU_TYPE_000 | CPU_TYPE_010))
/*TODO*///	#else
/*TODO*///		#define CPU_TYPE_IS_010(A)         0
/*TODO*///		#define CPU_TYPE_IS_010_PLUS(A)    CPU_TYPE_IS_EC020_PLUS(A)
/*TODO*///		#define CPU_TYPE_IS_010_LESS(A)    CPU_TYPE_IS_EC020_LESS(A)
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#if M68K_EMULATE_020 || M68K_EMULATE_EC020
/*TODO*///		#define CPU_TYPE_IS_020_VARIANT(A) ((A) & (CPU_TYPE_EC020 | CPU_TYPE_020))
/*TODO*///	#else
/*TODO*///		#define CPU_TYPE_IS_020_VARIANT(A) 0
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#if M68K_EMULATE_020 || M68K_EMULATE_EC020 || M68K_EMULATE_010
/*TODO*///		#define CPU_TYPE_IS_000(A)         ((A) == CPU_TYPE_000)
/*TODO*///	#else
/*TODO*///		#define CPU_TYPE_IS_000(A)         1
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	
/*TODO*///	#if !M68K_SEPARATE_READS
/*TODO*///	#define m68k_read_immediate_16(A) m68ki_read_program_16(A)
/*TODO*///	#define m68k_read_immediate_32(A) m68ki_read_program_32(A)
/*TODO*///	
/*TODO*///	#define m68k_read_pcrelative_8(A) m68ki_read_program_8(A)
/*TODO*///	#define m68k_read_pcrelative_16(A) m68ki_read_program_16(A)
/*TODO*///	#define m68k_read_pcrelative_32(A) m68ki_read_program_32(A)
/*TODO*///	#endif /* M68K_SEPARATE_READS */
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Enable or disable callback functions */
/*TODO*///	#if M68K_EMULATE_INT_ACK
/*TODO*///		#if M68K_EMULATE_INT_ACK == OPT_SPECIFY_HANDLER
/*TODO*///			#define m68ki_int_ack(A) M68K_INT_ACK_CALLBACK(A)
/*TODO*///		#else
/*TODO*///			#define m68ki_int_ack(A) CALLBACK_INT_ACK(A)
/*TODO*///		#endif
/*TODO*///	#else
/*TODO*///		/* Default action is to used autovector mode, which is most common */
/*TODO*///		#define m68ki_int_ack(A) M68K_INT_ACK_AUTOVECTOR
/*TODO*///	#endif /* M68K_EMULATE_INT_ACK */
/*TODO*///	
/*TODO*///	#if M68K_EMULATE_BKPT_ACK
/*TODO*///		#if M68K_EMULATE_BKPT_ACK == OPT_SPECIFY_HANDLER
/*TODO*///			#define m68ki_bkpt_ack(A) M68K_BKPT_ACK_CALLBACK(A)
/*TODO*///		#else
/*TODO*///			#define m68ki_bkpt_ack(A) CALLBACK_BKPT_ACK(A)
/*TODO*///		#endif
/*TODO*///	#else
/*TODO*///		#define m68ki_bkpt_ack(A)
/*TODO*///	#endif /* M68K_EMULATE_BKPT_ACK */
/*TODO*///	
/*TODO*///	#if M68K_EMULATE_RESET
/*TODO*///		#if M68K_EMULATE_RESET == OPT_SPECIFY_HANDLER
/*TODO*///			#define m68ki_output_reset() M68K_RESET_CALLBACK()
/*TODO*///		#else
/*TODO*///			#define m68ki_output_reset() CALLBACK_RESET_INSTR()
/*TODO*///		#endif
/*TODO*///	#else
/*TODO*///		#define m68ki_output_reset()
/*TODO*///	#endif /* M68K_EMULATE_RESET */
/*TODO*///	
/*TODO*///	#if M68K_INSTRUCTION_HOOK
/*TODO*///		#if M68K_INSTRUCTION_HOOK == OPT_SPECIFY_HANDLER
/*TODO*///			#define m68ki_instr_hook() M68K_INSTRUCTION_CALLBACK()
/*TODO*///		#else
/*TODO*///			#define m68ki_instr_hook() CALLBACK_INSTR_HOOK()
/*TODO*///		#endif
/*TODO*///	#else
/*TODO*///		#define m68ki_instr_hook()
/*TODO*///	#endif /* M68K_INSTRUCTION_HOOK */
/*TODO*///	
/*TODO*///	#if M68K_MONITOR_PC
/*TODO*///		#if M68K_MONITOR_PC == OPT_SPECIFY_HANDLER
/*TODO*///			#define m68ki_pc_changed(A) M68K_SET_PC_CALLBACK(ADDRESS_68K(A))
/*TODO*///		#else
/*TODO*///			#define m68ki_pc_changed(A) CALLBACK_PC_CHANGED(ADDRESS_68K(A))
/*TODO*///		#endif
/*TODO*///	#else
/*TODO*///		#define m68ki_pc_changed(A)
/*TODO*///	#endif /* M68K_MONITOR_PC */
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Enable or disable function code emulation */
/*TODO*///	#if M68K_EMULATE_FC
/*TODO*///		#if M68K_EMULATE_FC == OPT_SPECIFY_HANDLER
/*TODO*///			#define m68ki_set_fc(A) M68K_SET_FC_CALLBACK(A)
/*TODO*///		#else
/*TODO*///			#define m68ki_set_fc(A) CALLBACK_SET_FC(A)
/*TODO*///		#endif
/*TODO*///		#define m68ki_use_data_space() m68ki_address_space = FUNCTION_CODE_USER_DATA
/*TODO*///		#define m68ki_use_program_space() m68ki_address_space = FUNCTION_CODE_USER_PROGRAM
/*TODO*///		#define m68ki_get_address_space() m68ki_address_space
/*TODO*///	#else
/*TODO*///		#define m68ki_set_fc(A)
/*TODO*///		#define m68ki_use_data_space()
/*TODO*///		#define m68ki_use_program_space()
/*TODO*///		#define m68ki_get_address_space() FUNCTION_CODE_USER_DATA
/*TODO*///	#endif /* M68K_EMULATE_FC */
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Enable or disable trace emulation */
/*TODO*///	#if M68K_EMULATE_TRACE
/*TODO*///		/* Initiates trace checking before each instruction (t1) */
/*TODO*///		#define m68ki_trace_t1() m68ki_tracing = FLAG_T1
/*TODO*///		/* adds t0 to trace checking if we encounter change of flow */
/*TODO*///		#define m68ki_trace_t0() m68ki_tracing |= FLAG_T0
/*TODO*///		/* Clear all tracing */
/*TODO*///		#define m68ki_clear_trace() m68ki_tracing = 0
/*TODO*///		/* Cause a trace exception if we are tracing */
/*TODO*///		#define m68ki_exception_if_trace() if (m68ki_tracing != 0) m68ki_exception_trace()
/*TODO*///	#else
/*TODO*///		#define m68ki_trace_t1()
/*TODO*///		#define m68ki_trace_t0()
/*TODO*///		#define m68ki_clear_trace()
/*TODO*///		#define m68ki_exception_if_trace()
/*TODO*///	#endif /* M68K_EMULATE_TRACE */
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Address error */
/*TODO*///	#if M68K_EMULATE_ADDRESS_ERROR
/*TODO*///		extern jmp_buf m68ki_address_error_trap;
/*TODO*///		#define m68ki_set_address_error_trap() if(setjmp(m68ki_address_error_trap)) m68ki_exception_address_error();
/*TODO*///		#define m68ki_check_address_error(A) if((A)&1) longjmp(m68ki_address_error_jump, 1);
/*TODO*///	#else
/*TODO*///		#define m68ki_set_address_error_trap()
/*TODO*///		#define m68ki_check_address_error(A)
/*TODO*///	#endif /* M68K_ADDRESS_ERROR */
/*TODO*///	
/*TODO*///	/* Logging */
/*TODO*///	#if M68K_LOG_ENABLE
/*TODO*///			extern FILE* M68K_LOG_FILEHANDLE
/*TODO*///		extern char* m68ki_cpu_names[];
/*TODO*///	
/*TODO*///		#define M68K_DO_LOG(A) if (M68K_LOG_FILEHANDLE != 0) fprintf A
/*TODO*///		#if M68K_LOG_1010_1111
/*TODO*///			#define M68K_DO_LOG_EMU(A) if (M68K_LOG_FILEHANDLE != 0) fprintf A
/*TODO*///		#else
/*TODO*///			#define M68K_DO_LOG_EMU(A)
/*TODO*///		#endif
/*TODO*///	#else
/*TODO*///		#define M68K_DO_LOG(A)
/*TODO*///		#define M68K_DO_LOG_EMU(A)
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* -------------------------- EA / Operand Access ------------------------- */
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 * The general instruction format follows this pattern:
/*TODO*///	 * .... XXX. .... .YYY
/*TODO*///	 * where XXX is register X and YYY is register Y
/*TODO*///	 */
/*TODO*///	/* Data Register Isolation */
/*TODO*///	#define DX (REG_D[(REG_IR >> 9) & 7])
/*TODO*///	#define DY (REG_D[REG_IR & 7])
/*TODO*///	/* Address Register Isolation */
/*TODO*///	#define AX (REG_A[(REG_IR >> 9) & 7])
/*TODO*///	#define AY (REG_A[REG_IR & 7])
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Effective Address Calculations */
/*TODO*///	#define EA_AY_AI_8()   AY                                    /* address register indirect */
/*TODO*///	#define EA_AY_AI_16()  EA_AY_AI_8()
/*TODO*///	#define EA_AY_AI_32()  EA_AY_AI_8()
/*TODO*///	#define EA_AY_PI_8()   (AY++)                                /* postincrement (size = byte) */
/*TODO*///	#define EA_AY_PI_16()  ((AY+=2)-2)                           /* postincrement (size = word) */
/*TODO*///	#define EA_AY_PI_32()  ((AY+=4)-4)                           /* postincrement (size = long) */
/*TODO*///	#define EA_AY_PD_8()   (--AY)                                /* predecrement (size = byte) */
/*TODO*///	#define EA_AY_PD_16()  (AY-=2)                               /* predecrement (size = word) */
/*TODO*///	#define EA_AY_PD_32()  (AY-=4)                               /* predecrement (size = long) */
/*TODO*///	#define EA_AY_DI_8()   (AY+MAKE_INT_16(m68ki_read_imm_16())) /* displacement */
/*TODO*///	#define EA_AY_DI_16()  EA_AY_DI_8()
/*TODO*///	#define EA_AY_DI_32()  EA_AY_DI_8()
/*TODO*///	#define EA_AY_IX_8()   m68ki_get_ea_ix(AY)                   /* indirect + index */
/*TODO*///	#define EA_AY_IX_16()  EA_AY_IX_8()
/*TODO*///	#define EA_AY_IX_32()  EA_AY_IX_8()
/*TODO*///	
/*TODO*///	#define EA_AX_AI_8()   AX
/*TODO*///	#define EA_AX_AI_16()  EA_AX_AI_8()
/*TODO*///	#define EA_AX_AI_32()  EA_AX_AI_8()
/*TODO*///	#define EA_AX_PI_8()   (AX++)
/*TODO*///	#define EA_AX_PI_16()  ((AX+=2)-2)
/*TODO*///	#define EA_AX_PI_32()  ((AX+=4)-4)
/*TODO*///	#define EA_AX_PD_8()   (--AX)
/*TODO*///	#define EA_AX_PD_16()  (AX-=2)
/*TODO*///	#define EA_AX_PD_32()  (AX-=4)
/*TODO*///	#define EA_AX_DI_8()   (AX+MAKE_INT_16(m68ki_read_imm_16()))
/*TODO*///	#define EA_AX_DI_16()  EA_AX_DI_8()
/*TODO*///	#define EA_AX_DI_32()  EA_AX_DI_8()
/*TODO*///	#define EA_AX_IX_8()   m68ki_get_ea_ix(AX)
/*TODO*///	#define EA_AX_IX_16()  EA_AX_IX_8()
/*TODO*///	#define EA_AX_IX_32()  EA_AX_IX_8()
/*TODO*///	
/*TODO*///	#define EA_A7_PI_8()   ((REG_A[7]+=2)-2)
/*TODO*///	#define EA_A7_PD_8()   (REG_A[7]-=2)
/*TODO*///	
/*TODO*///	#define EA_AW_8()      MAKE_INT_16(m68ki_read_imm_16())      /* absolute word */
/*TODO*///	#define EA_AW_16()     EA_AW_8()
/*TODO*///	#define EA_AW_32()     EA_AW_8()
/*TODO*///	#define EA_AL_8()      m68ki_read_imm_32()                   /* absolute long */
/*TODO*///	#define EA_AL_16()     EA_AL_8()
/*TODO*///	#define EA_AL_32()     EA_AL_8()
/*TODO*///	#define EA_PCDI_8()    m68ki_get_ea_pcdi()                   /* pc indirect + displacement */
/*TODO*///	#define EA_PCDI_16()   EA_PCDI_8()
/*TODO*///	#define EA_PCDI_32()   EA_PCDI_8()
/*TODO*///	#define EA_PCIX_8()    m68ki_get_ea_pcix()                   /* pc indirect + index */
/*TODO*///	#define EA_PCIX_16()   EA_PCIX_8()
/*TODO*///	#define EA_PCIX_32()   EA_PCIX_8()
/*TODO*///	
/*TODO*///	
/*TODO*///	#define OPER_I_8()     m68ki_read_imm_8()
/*TODO*///	#define OPER_I_16()    m68ki_read_imm_16()
/*TODO*///	#define OPER_I_32()    m68ki_read_imm_32()
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* --------------------------- Status Register ---------------------------- */
/*TODO*///	
/*TODO*///	/* Flag Calculation Macros */
/*TODO*///	#define CFLAG_8(A) (A)
/*TODO*///	#define CFLAG_16(A) ((A)>>8)
/*TODO*///	
/*TODO*///	#if M68K_INT_GT_32_BIT
/*TODO*///		#define CFLAG_ADD_32(S, D, R) ((R)>>24)
/*TODO*///		#define CFLAG_SUB_32(S, D, R) ((R)>>24)
/*TODO*///	#else
/*TODO*///		#define CFLAG_ADD_32(S, D, R) (((S & D) | (~R & (S | D)))>>23)
/*TODO*///		#define CFLAG_SUB_32(S, D, R) (((S & R) | (~D & (S | R)))>>23)
/*TODO*///	#endif /* M68K_INT_GT_32_BIT */
/*TODO*///	
/*TODO*///	#define VFLAG_ADD_8(S, D, R) ((S^R) & (D^R))
/*TODO*///	#define VFLAG_ADD_16(S, D, R) (((S^R) & (D^R))>>8)
/*TODO*///	#define VFLAG_ADD_32(S, D, R) (((S^R) & (D^R))>>24)
/*TODO*///	
/*TODO*///	#define VFLAG_SUB_8(S, D, R) ((S^D) & (R^D))
/*TODO*///	#define VFLAG_SUB_16(S, D, R) (((S^D) & (R^D))>>8)
/*TODO*///	#define VFLAG_SUB_32(S, D, R) (((S^D) & (R^D))>>24)
/*TODO*///	
/*TODO*///	#define NFLAG_8(A) (A)
/*TODO*///	#define NFLAG_16(A) ((A)>>8)
/*TODO*///	#define NFLAG_32(A) ((A)>>24)
/*TODO*///	#define NFLAG_64(A) ((A)>>56)
/*TODO*///	
/*TODO*///	#define ZFLAG_8(A) MASK_OUT_ABOVE_8(A)
/*TODO*///	#define ZFLAG_16(A) MASK_OUT_ABOVE_16(A)
/*TODO*///	#define ZFLAG_32(A) MASK_OUT_ABOVE_32(A)
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Flag values */
/*TODO*///	#define NFLAG_SET   0x80
/*TODO*///	#define NFLAG_CLEAR 0
/*TODO*///	#define CFLAG_SET   0x100
/*TODO*///	#define CFLAG_CLEAR 0
/*TODO*///	#define XFLAG_SET   0x100
/*TODO*///	#define XFLAG_CLEAR 0
/*TODO*///	#define VFLAG_SET   0x80
/*TODO*///	#define VFLAG_CLEAR 0
/*TODO*///	#define ZFLAG_SET   0
/*TODO*///	#define ZFLAG_CLEAR 0xffffffff
/*TODO*///	
/*TODO*///	#define SFLAG_SET   4
/*TODO*///	#define SFLAG_CLEAR 0
/*TODO*///	#define MFLAG_SET   2
/*TODO*///	#define MFLAG_CLEAR 0
/*TODO*///	
/*TODO*///	/* Turn flag values into 1 or 0 */
/*TODO*///	#define XFLAG_AS_1() ((FLAG_X>>8)&1)
/*TODO*///	#define NFLAG_AS_1() ((FLAG_N>>7)&1)
/*TODO*///	#define VFLAG_AS_1() ((FLAG_V>>7)&1)
/*TODO*///	#define ZFLAG_AS_1() (!FLAG_Z)
/*TODO*///	#define CFLAG_AS_1() ((FLAG_C>>8)&1)
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Conditions */
/*TODO*///	#define COND_CS() (FLAG_C&0x100)
/*TODO*///	#define COND_CC() (!COND_CS())
/*TODO*///	#define COND_VS() (FLAG_V&0x80)
/*TODO*///	#define COND_VC() (!COND_VS())
/*TODO*///	#define COND_NE() FLAG_Z
/*TODO*///	#define COND_EQ() (!COND_NE())
/*TODO*///	#define COND_MI() (FLAG_N&0x80)
/*TODO*///	#define COND_PL() (!COND_MI())
/*TODO*///	#define COND_LT() ((FLAG_N^FLAG_V)&0x80)
/*TODO*///	#define COND_GE() (!COND_LT())
/*TODO*///	#define COND_HI() (COND_CC() && COND_NE())
/*TODO*///	#define COND_LS() (COND_CS() || COND_EQ())
/*TODO*///	#define COND_GT() (COND_GE() && COND_NE())
/*TODO*///	#define COND_LE() (COND_LT() || COND_EQ())
/*TODO*///	
/*TODO*///	/* Reversed conditions */
/*TODO*///	#define COND_NOT_CS() COND_CC()
/*TODO*///	#define COND_NOT_CC() COND_CS()
/*TODO*///	#define COND_NOT_VS() COND_VC()
/*TODO*///	#define COND_NOT_VC() COND_VS()
/*TODO*///	#define COND_NOT_NE() COND_EQ()
/*TODO*///	#define COND_NOT_EQ() COND_NE()
/*TODO*///	#define COND_NOT_MI() COND_PL()
/*TODO*///	#define COND_NOT_PL() COND_MI()
/*TODO*///	#define COND_NOT_LT() COND_GE()
/*TODO*///	#define COND_NOT_GE() COND_LT()
/*TODO*///	#define COND_NOT_HI() COND_LS()
/*TODO*///	#define COND_NOT_LS() COND_HI()
/*TODO*///	#define COND_NOT_GT() COND_LE()
/*TODO*///	#define COND_NOT_LE() COND_GT()
/*TODO*///	
/*TODO*///	/* Not real conditions, but here for convenience */
/*TODO*///	#define COND_XS() (FLAG_X&0x100)
/*TODO*///	#define COND_XC() (!COND_XS)
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Get the condition code register */
/*TODO*///	#define m68ki_get_ccr() ((COND_XS() >> 4) | \
/*TODO*///							 (COND_MI() >> 4) | \
/*TODO*///							 (COND_EQ() << 2) | \
/*TODO*///							 (COND_VS() >> 6) | \
/*TODO*///							 (COND_CS() >> 8))
/*TODO*///	
/*TODO*///	/* Get the status register */
/*TODO*///	#define m68ki_get_sr() ( FLAG_T1              | \
/*TODO*///							 FLAG_T0              | \
/*TODO*///							(FLAG_S        << 11) | \
/*TODO*///							(FLAG_M        << 11) | \
/*TODO*///							 FLAG_INT_MASK        | \
/*TODO*///							 m68ki_get_ccr())
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ---------------------------- Cycle Counting ---------------------------- */
/*TODO*///	
/*TODO*///	#define ADD_CYCLES(A)    m68ki_remaining_cycles += (A)
/*TODO*///	#define USE_CYCLES(A)    m68ki_remaining_cycles -= (A)
/*TODO*///	#define SET_CYCLES(A)    m68ki_remaining_cycles = A
/*TODO*///	#define GET_CYCLES()     m68ki_remaining_cycles
/*TODO*///	#define USE_ALL_CYCLES() m68ki_remaining_cycles = 0
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ----------------------------- Read / Write ----------------------------- */
/*TODO*///	
/*TODO*///	/* Read from the current address space */
/*TODO*///	#define m68ki_read_8(A)  m68ki_read_8_fc (A, FLAG_S | m68ki_get_address_space())
/*TODO*///	#define m68ki_read_16(A) m68ki_read_16_fc(A, FLAG_S | m68ki_get_address_space())
/*TODO*///	#define m68ki_read_32(A) m68ki_read_32_fc(A, FLAG_S | m68ki_get_address_space())
/*TODO*///	
/*TODO*///	/* Write to the current data space */
/*TODO*///	#define m68ki_write_8(A, V)  m68ki_write_8_fc (A, FLAG_S | FUNCTION_CODE_USER_DATA, V)
/*TODO*///	#define m68ki_write_16(A, V) m68ki_write_16_fc(A, FLAG_S | FUNCTION_CODE_USER_DATA, V)
/*TODO*///	#define m68ki_write_32(A, V) m68ki_write_32_fc(A, FLAG_S | FUNCTION_CODE_USER_DATA, V)
/*TODO*///	
/*TODO*///	/* map read immediate 8 to read immediate 16 */
/*TODO*///	#define m68ki_read_imm_8() MASK_OUT_ABOVE_8(m68ki_read_imm_16())
/*TODO*///	
/*TODO*///	/* Map PC-relative reads */
/*TODO*///	#define m68ki_read_pcrel_8(A) m68k_read_pcrelative_8(A)
/*TODO*///	#define m68ki_read_pcrel_16(A) m68k_read_pcrelative_16(A)
/*TODO*///	#define m68ki_read_pcrel_32(A) m68k_read_pcrelative_32(A)
/*TODO*///	
/*TODO*///	/* Read from the program space */
/*TODO*///	#define m68ki_read_program_8(A) 	m68ki_read_8_fc(A, FLAG_S | FUNCTION_CODE_USER_PROGRAM)
/*TODO*///	#define m68ki_read_program_16(A) 	m68ki_read_16_fc(A, FLAG_S | FUNCTION_CODE_USER_PROGRAM)
/*TODO*///	#define m68ki_read_program_32(A) 	m68ki_read_32_fc(A, FLAG_S | FUNCTION_CODE_USER_PROGRAM)
/*TODO*///	
/*TODO*///	/* Read from the data space */
/*TODO*///	#define m68ki_read_data_8(A) 	m68ki_read_8_fc(A, FLAG_S | FUNCTION_CODE_USER_DATA)
/*TODO*///	#define m68ki_read_data_16(A) 	m68ki_read_16_fc(A, FLAG_S | FUNCTION_CODE_USER_DATA)
/*TODO*///	#define m68ki_read_data_32(A) 	m68ki_read_32_fc(A, FLAG_S | FUNCTION_CODE_USER_DATA)
	
	
	
	/* ======================================================================== */
	/* =============================== PROTOTYPES ============================= */
	/* ======================================================================== */
	
	public static class m68ki_cpu_core
	{
		int cpu_type;     /* CPU Type: 68000, 68010, 68EC020, or 68020 */
/*TODO*///		uint dar[16];      /* Data and Address Registers */
/*TODO*///		uint ppc;		   /* Previous program counter */
/*TODO*///		uint pc;           /* Program Counter */
/*TODO*///		uint sp[7];        /* User, Interrupt, and Master Stack Pointers */
/*TODO*///		uint vbr;          /* Vector Base Register (m68010+) */
/*TODO*///		uint sfc;          /* Source Function Code Register (m68010+) */
/*TODO*///		uint dfc;          /* Destination Function Code Register (m68010+) */
/*TODO*///		uint cacr;         /* Cache Control Register (m68020, unemulated) */
/*TODO*///		uint caar;         /* Cache Address Register (m68020, unemulated) */
/*TODO*///		uint ir;           /* Instruction Register */
/*TODO*///		uint t1_flag;      /* Trace 1 */
/*TODO*///		uint t0_flag;      /* Trace 0 */
/*TODO*///		uint s_flag;       /* Supervisor */
/*TODO*///		uint m_flag;       /* Master/Interrupt state */
/*TODO*///		uint x_flag;       /* Extend */
/*TODO*///		uint n_flag;       /* Negative */
/*TODO*///		uint not_z_flag;   /* Zero, inverted for speedups */
/*TODO*///		uint v_flag;       /* Overflow */
/*TODO*///		uint c_flag;       /* Carry */
/*TODO*///		uint int_mask;     /* I0-I2 */
		int int_level;    /* State of interrupt pins IPL0-IPL2 -- ASG: changed from ints_pending */
/*TODO*///		uint int_cycles;   /* ASG: extra cycles from generated interrupts */
/*TODO*///		uint stopped;      /* Stopped state */
/*TODO*///		uint pref_addr;    /* Last prefetch address */
/*TODO*///		uint pref_data;    /* Data in the prefetch queue */
		int address_mask; /* Available address pins */
		int sr_mask;      /* Implemented status register bits */
	
		/* Clocks required for instructions / exceptions */
		int cyc_bcc_notake_b;
		int cyc_bcc_notake_w;
		int cyc_dbcc_f_noexp;
		int cyc_dbcc_f_exp;
		int cyc_scc_r_false;
		int cyc_movem_w;
		int cyc_movem_l;
		int cyc_shift;
		int cyc_reset;
		char[] /*uint8* */ cyc_instruction;
		char[] /*uint8* */ cyc_exception;
	
		/* Callbacks to host */
		irqcallbacksPtr int_ack_callback;           /* Interrupt Acknowledge */
                bkpt_ack_callbackPtr bkpt_ack_callback;     /* Breakpoint Acknowledge */
                reset_instr_callbackPtr reset_instr_callback;               /* Called when a RESET instruction is encountered */
                pc_changed_callbackPtr pc_changed_callback; /* Called when the PC changes by a large amount */
                set_fc_callbackPtr set_fc_callback;     /* Called when the CPU function code changes */
                instr_hook_callbackPtr instr_hook_callback;                /* Called every instruction cycle prior to execution */

	};


/*TODO*///	extern m68ki_cpu_core m68ki_cpu;
/*TODO*///	extern sint           m68ki_remaining_cycles;
/*TODO*///	extern uint           m68ki_tracing;
/*TODO*///	extern uint8          m68ki_shift_8_table[];
/*TODO*///	extern uint16         m68ki_shift_16_table[];
/*TODO*///	extern uint           m68ki_shift_32_table[];
/*TODO*///	extern uint8          m68ki_exception_cycle_table[][256];
/*TODO*///	extern uint           m68ki_address_space;
/*TODO*///	extern uint8          m68ki_ea_idx_cycle_table[];
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Read data immediately after the program counter */
/*TODO*///	INLINE uint m68ki_read_imm_16(void);
/*TODO*///	INLINE uint m68ki_read_imm_32(void);
/*TODO*///	
/*TODO*///	/* Read data with specific function code */
/*TODO*///	INLINE uint m68ki_read_8_fc  (uint address, uint fc);
/*TODO*///	INLINE uint m68ki_read_16_fc (uint address, uint fc);
/*TODO*///	INLINE uint m68ki_read_32_fc (uint address, uint fc);
/*TODO*///	
/*TODO*///	/* Write data with specific function code */
/*TODO*///	INLINE void m68ki_write_8_fc (uint address, uint fc, uint value);
/*TODO*///	INLINE void m68ki_write_16_fc(uint address, uint fc, uint value);
/*TODO*///	INLINE void m68ki_write_32_fc(uint address, uint fc, uint value);
/*TODO*///	
/*TODO*///	/* Indexed and PC-relative ea fetching */
/*TODO*///	INLINE uint m68ki_get_ea_pcdi(void);
/*TODO*///	INLINE uint m68ki_get_ea_pcix(void);
/*TODO*///	INLINE uint m68ki_get_ea_ix(uint An);
/*TODO*///	
/*TODO*///	/* Operand fetching */
/*TODO*///	INLINE uint OPER_AY_AI_8(void);
/*TODO*///	INLINE uint OPER_AY_AI_16(void);
/*TODO*///	INLINE uint OPER_AY_AI_32(void);
/*TODO*///	INLINE uint OPER_AY_PI_8(void);
/*TODO*///	INLINE uint OPER_AY_PI_16(void);
/*TODO*///	INLINE uint OPER_AY_PI_32(void);
/*TODO*///	INLINE uint OPER_AY_PD_8(void);
/*TODO*///	INLINE uint OPER_AY_PD_16(void);
/*TODO*///	INLINE uint OPER_AY_PD_32(void);
/*TODO*///	INLINE uint OPER_AY_DI_8(void);
/*TODO*///	INLINE uint OPER_AY_DI_16(void);
/*TODO*///	INLINE uint OPER_AY_DI_32(void);
/*TODO*///	INLINE uint OPER_AY_IX_8(void);
/*TODO*///	INLINE uint OPER_AY_IX_16(void);
/*TODO*///	INLINE uint OPER_AY_IX_32(void);
/*TODO*///	
/*TODO*///	INLINE uint OPER_AX_AI_8(void);
/*TODO*///	INLINE uint OPER_AX_AI_16(void);
/*TODO*///	INLINE uint OPER_AX_AI_32(void);
/*TODO*///	INLINE uint OPER_AX_PI_8(void);
/*TODO*///	INLINE uint OPER_AX_PI_16(void);
/*TODO*///	INLINE uint OPER_AX_PI_32(void);
/*TODO*///	INLINE uint OPER_AX_PD_8(void);
/*TODO*///	INLINE uint OPER_AX_PD_16(void);
/*TODO*///	INLINE uint OPER_AX_PD_32(void);
/*TODO*///	INLINE uint OPER_AX_DI_8(void);
/*TODO*///	INLINE uint OPER_AX_DI_16(void);
/*TODO*///	INLINE uint OPER_AX_DI_32(void);
/*TODO*///	INLINE uint OPER_AX_IX_8(void);
/*TODO*///	INLINE uint OPER_AX_IX_16(void);
/*TODO*///	INLINE uint OPER_AX_IX_32(void);
/*TODO*///	
/*TODO*///	INLINE uint OPER_A7_PI_8(void);
/*TODO*///	INLINE uint OPER_A7_PD_8(void);
/*TODO*///	
/*TODO*///	INLINE uint OPER_AW_8(void);
/*TODO*///	INLINE uint OPER_AW_16(void);
/*TODO*///	INLINE uint OPER_AW_32(void);
/*TODO*///	INLINE uint OPER_AL_8(void);
/*TODO*///	INLINE uint OPER_AL_16(void);
/*TODO*///	INLINE uint OPER_AL_32(void);
/*TODO*///	INLINE uint OPER_PCDI_8(void);
/*TODO*///	INLINE uint OPER_PCDI_16(void);
/*TODO*///	INLINE uint OPER_PCDI_32(void);
/*TODO*///	INLINE uint OPER_PCIX_8(void);
/*TODO*///	INLINE uint OPER_PCIX_16(void);
/*TODO*///	INLINE uint OPER_PCIX_32(void);
/*TODO*///	
/*TODO*///	/* Stack operations */
/*TODO*///	INLINE void m68ki_push_16(uint value);
/*TODO*///	INLINE void m68ki_push_32(uint value);
/*TODO*///	INLINE uint m68ki_pull_16(void);
/*TODO*///	INLINE uint m68ki_pull_32(void);
/*TODO*///	
/*TODO*///	/* Program flow operations */
/*TODO*///	INLINE void m68ki_jump(uint new_pc);
/*TODO*///	INLINE void m68ki_jump_vector(uint vector);
/*TODO*///	INLINE void m68ki_branch_8(uint offset);
/*TODO*///	INLINE void m68ki_branch_16(uint offset);
/*TODO*///	INLINE void m68ki_branch_32(uint offset);
/*TODO*///	
/*TODO*///	/* Status register operations. */
/*TODO*///	INLINE void m68ki_set_s_flag(uint value);            /* Only bit 2 of value should be set (i.e. 4 or 0) */
/*TODO*///	INLINE void m68ki_set_sm_flag(uint value);           /* only bits 1 and 2 of value should be set */
/*TODO*///	INLINE void m68ki_set_ccr(uint value);               /* set the condition code register */
/*TODO*///	INLINE void m68ki_set_sr(uint value);                /* set the status register */
/*TODO*///	INLINE void m68ki_set_sr_noint(uint value);          /* set the status register */
/*TODO*///	
/*TODO*///	/* Exception processing */
/*TODO*///	INLINE uint m68ki_init_exception(void);              /* Initial exception processing */
/*TODO*///	
/*TODO*///	INLINE void m68ki_stack_frame_3word(uint pc, uint sr); /* Stack various frame types */
/*TODO*///	INLINE void m68ki_stack_frame_buserr(uint pc, uint sr, uint address, uint write, uint instruction, uint fc);
/*TODO*///	
/*TODO*///	INLINE void m68ki_stack_frame_0000(uint pc, uint sr, uint vector);
/*TODO*///	INLINE void m68ki_stack_frame_0001(uint pc, uint sr, uint vector);
/*TODO*///	INLINE void m68ki_stack_frame_0010(uint sr, uint vector);
/*TODO*///	INLINE void m68ki_stack_frame_1000(uint pc, uint sr, uint vector);
/*TODO*///	INLINE void m68ki_stack_frame_1010(uint sr, uint vector, uint pc);
/*TODO*///	INLINE void m68ki_stack_frame_1011(uint sr, uint vector, uint pc);
/*TODO*///	
/*TODO*///	INLINE void m68ki_exception_trap(uint vector);
/*TODO*///	INLINE void m68ki_exception_trapN(uint vector);
/*TODO*///	INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE void m68ki_exception_interrupt(uint int_level);
/*TODO*///	INLINE 
/*TODO*///	/* quick disassembly (used for logging) */
/*TODO*///	char* m68ki_disassemble_quick(unsigned int pc, unsigned int cpu_type);
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ======================================================================== */
/*TODO*///	/* =========================== UTILITY FUNCTIONS ========================== */
/*TODO*///	/* ======================================================================== */
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ---------------------------- Read Immediate ---------------------------- */
/*TODO*///	
/*TODO*///	/* Handles all immediate reads, does address error check, function code setting,
/*TODO*///	 * and prefetching if they are enabled in m68kconf.h
/*TODO*///	 */
/*TODO*///	INLINE uint m68ki_read_imm_16(void)
/*TODO*///	{
/*TODO*///		m68ki_set_fc(FLAG_S | FUNCTION_CODE_USER_PROGRAM); /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_check_address_error(REG_PC); /* auto-disable (see m68kcpu.h) */
/*TODO*///	#if M68K_EMULATE_PREFETCH
/*TODO*///		if(MASK_OUT_BELOW_2(REG_PC) != CPU_PREF_ADDR)
/*TODO*///		{
/*TODO*///			CPU_PREF_ADDR = MASK_OUT_BELOW_2(REG_PC);
/*TODO*///			CPU_PREF_DATA = m68k_read_immediate_32(ADDRESS_68K(CPU_PREF_ADDR));
/*TODO*///		}
/*TODO*///		REG_PC += 2;
/*TODO*///		return MASK_OUT_ABOVE_16(CPU_PREF_DATA >> ((2-((REG_PC-2)&2))<<3));
/*TODO*///	#else
/*TODO*///		REG_PC += 2;
/*TODO*///		return m68k_read_immediate_16(ADDRESS_68K(REG_PC-2));
/*TODO*///	#endif /* M68K_EMULATE_PREFETCH */
/*TODO*///	}
/*TODO*///	INLINE uint m68ki_read_imm_32(void)
/*TODO*///	{
/*TODO*///	#if M68K_EMULATE_PREFETCH
/*TODO*///		uint temp_val;
/*TODO*///	
/*TODO*///		m68ki_set_fc(FLAG_S | FUNCTION_CODE_USER_PROGRAM); /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_check_address_error(REG_PC); /* auto-disable (see m68kcpu.h) */
/*TODO*///		if(MASK_OUT_BELOW_2(REG_PC) != CPU_PREF_ADDR)
/*TODO*///		{
/*TODO*///			CPU_PREF_ADDR = MASK_OUT_BELOW_2(REG_PC);
/*TODO*///			CPU_PREF_DATA = m68k_read_immediate_32(ADDRESS_68K(CPU_PREF_ADDR));
/*TODO*///		}
/*TODO*///		temp_val = CPU_PREF_DATA;
/*TODO*///		REG_PC += 2;
/*TODO*///		if(MASK_OUT_BELOW_2(REG_PC) != CPU_PREF_ADDR)
/*TODO*///		{
/*TODO*///			CPU_PREF_ADDR = MASK_OUT_BELOW_2(REG_PC);
/*TODO*///			CPU_PREF_DATA = m68k_read_immediate_32(ADDRESS_68K(CPU_PREF_ADDR));
/*TODO*///			temp_val = MASK_OUT_ABOVE_32((temp_val << 16) | (CPU_PREF_DATA >> 16));
/*TODO*///		}
/*TODO*///		REG_PC += 2;
/*TODO*///	
/*TODO*///		return temp_val;
/*TODO*///	#else
/*TODO*///		m68ki_set_fc(FLAG_S | FUNCTION_CODE_USER_PROGRAM); /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_check_address_error(REG_PC); /* auto-disable (see m68kcpu.h) */
/*TODO*///		REG_PC += 4;
/*TODO*///		return m68k_read_immediate_32(ADDRESS_68K(REG_PC-4));
/*TODO*///	#endif /* M68K_EMULATE_PREFETCH */
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ------------------------- Top level read/write ------------------------- */
/*TODO*///	
/*TODO*///	/* Handles all memory accesses (except for immediate reads if they are
/*TODO*///	 * configured to use separate functions in m68kconf.h).
/*TODO*///	 * All memory accesses must go through these top level functions.
/*TODO*///	 * These functions will also check for address error and set the function
/*TODO*///	 * code if they are enabled in m68kconf.h.
/*TODO*///	 */
/*TODO*///	INLINE uint m68ki_read_8_fc(uint address, uint fc)
/*TODO*///	{
/*TODO*///		m68ki_set_fc(fc); /* auto-disable (see m68kcpu.h) */
/*TODO*///		return m68k_read_memory_8(ADDRESS_68K(address));
/*TODO*///	}
/*TODO*///	INLINE uint m68ki_read_16_fc(uint address, uint fc)
/*TODO*///	{
/*TODO*///		m68ki_set_fc(fc); /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_check_address_error(address); /* auto-disable (see m68kcpu.h) */
/*TODO*///		return m68k_read_memory_16(ADDRESS_68K(address));
/*TODO*///	}
/*TODO*///	INLINE uint m68ki_read_32_fc(uint address, uint fc)
/*TODO*///	{
/*TODO*///		m68ki_set_fc(fc); /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_check_address_error(address); /* auto-disable (see m68kcpu.h) */
/*TODO*///		return m68k_read_memory_32(ADDRESS_68K(address));
/*TODO*///	}
/*TODO*///	
/*TODO*///	INLINE void m68ki_write_8_fc(uint address, uint fc, uint value)
/*TODO*///	{
/*TODO*///		m68ki_set_fc(fc); /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68k_write_memory_8(ADDRESS_68K(address), value);
/*TODO*///	}
/*TODO*///	INLINE void m68ki_write_16_fc(uint address, uint fc, uint value)
/*TODO*///	{
/*TODO*///		m68ki_set_fc(fc); /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_check_address_error(address); /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68k_write_memory_16(ADDRESS_68K(address), value);
/*TODO*///	}
/*TODO*///	INLINE void m68ki_write_32_fc(uint address, uint fc, uint value)
/*TODO*///	{
/*TODO*///		m68ki_set_fc(fc); /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_check_address_error(address); /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68k_write_memory_32(ADDRESS_68K(address), value);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* --------------------- Effective Address Calculation -------------------- */
/*TODO*///	
/*TODO*///	/* The program counter relative addressing modes cause operands to be
/*TODO*///	 * retrieved from program space, not data space.
/*TODO*///	 */
/*TODO*///	INLINE uint m68ki_get_ea_pcdi(void)
/*TODO*///	{
/*TODO*///		uint old_pc = REG_PC;
/*TODO*///		m68ki_use_program_space(); /* auto-disable */
/*TODO*///		return old_pc + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	INLINE uint m68ki_get_ea_pcix(void)
/*TODO*///	{
/*TODO*///		m68ki_use_program_space(); /* auto-disable */
/*TODO*///		return m68ki_get_ea_ix(REG_PC);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Indexed addressing modes are encoded as follows:
/*TODO*///	 *
/*TODO*///	 * Base instruction format:
/*TODO*///	 * F E D C B A 9 8 7 6 | 5 4 3 | 2 1 0
/*TODO*///	 * x x x x x x x x x x | 1 1 0 | BASE REGISTER      (An)
/*TODO*///	 *
/*TODO*///	 * Base instruction format for destination EA in move instructions:
/*TODO*///	 * F E D C | B A 9    | 8 7 6 | 5 4 3 2 1 0
/*TODO*///	 * x x x x | BASE REG | 1 1 0 | X X X X X X       (An)
/*TODO*///	 *
/*TODO*///	 * Brief extension format:
/*TODO*///	 *  F  |  E D C   |  B  |  A 9  | 8 | 7 6 5 4 3 2 1 0
/*TODO*///	 * D/A | REGISTER | W/L | SCALE | 0 |  DISPLACEMENT
/*TODO*///	 *
/*TODO*///	 * Full extension format:
/*TODO*///	 *  F     E D C      B     A 9    8   7    6    5 4       3   2 1 0
/*TODO*///	 * D/A | REGISTER | W/L | SCALE | 1 | BS | IS | BD SIZE | 0 | I/IS
/*TODO*///	 * BASE DISPLACEMENT (0, 16, 32 bit)                (bd)
/*TODO*///	 * OUTER DISPLACEMENT (0, 16, 32 bit)               (od)
/*TODO*///	 *
/*TODO*///	 * D/A:     0 = Dn, 1 = An                          (Xn)
/*TODO*///	 * W/L:     0 = W (sign extend), 1 = L              (.SIZE)
/*TODO*///	 * SCALE:   00=1, 01=2, 10=4, 11=8                  (*SCALE)
/*TODO*///	 * BS:      0=add base reg, 1=suppress base reg     (An suppressed)
/*TODO*///	 * IS:      0=add index, 1=suppress index           (Xn suppressed)
/*TODO*///	 * BD SIZE: 00=reserved, 01=NULL, 10=Word, 11=Long  (size of bd)
/*TODO*///	 *
/*TODO*///	 * IS I/IS Operation
/*TODO*///	 * 0  000  No Memory Indirect
/*TODO*///	 * 0  001  indir prex with null outer
/*TODO*///	 * 0  010  indir prex with word outer
/*TODO*///	 * 0  011  indir prex with long outer
/*TODO*///	 * 0  100  reserved
/*TODO*///	 * 0  101  indir postx with null outer
/*TODO*///	 * 0  110  indir postx with word outer
/*TODO*///	 * 0  111  indir postx with long outer
/*TODO*///	 * 1  000  no memory indirect
/*TODO*///	 * 1  001  mem indir with null outer
/*TODO*///	 * 1  010  mem indir with word outer
/*TODO*///	 * 1  011  mem indir with long outer
/*TODO*///	 * 1  100-111  reserved
/*TODO*///	 */
/*TODO*///	INLINE uint m68ki_get_ea_ix(uint An)
/*TODO*///	{
/*TODO*///		/* An = base register */
/*TODO*///		uint extension = m68ki_read_imm_16();
/*TODO*///		uint Xn = 0;                        /* Index register */
/*TODO*///		uint bd = 0;                        /* Base Displacement */
/*TODO*///		uint od = 0;                        /* Outer Displacement */
/*TODO*///	
/*TODO*///		if(CPU_TYPE_IS_010_LESS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			/* Calculate index */
/*TODO*///			Xn = REG_DA[extension>>12];     /* Xn */
/*TODO*///			if(!BIT_B(extension))           /* W/L */
/*TODO*///				Xn = MAKE_INT_16(Xn);
/*TODO*///	
/*TODO*///			/* Add base register and displacement and return */
/*TODO*///			return An + Xn + MAKE_INT_8(extension);
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* Brief extension format */
/*TODO*///		if(!BIT_8(extension))
/*TODO*///		{
/*TODO*///			/* Calculate index */
/*TODO*///			Xn = REG_DA[extension>>12];     /* Xn */
/*TODO*///			if(!BIT_B(extension))           /* W/L */
/*TODO*///				Xn = MAKE_INT_16(Xn);
/*TODO*///			/* Add scale if proper CPU type */
/*TODO*///			if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///				Xn <<= (extension>>9) & 3;  /* SCALE */
/*TODO*///	
/*TODO*///			/* Add base register and displacement and return */
/*TODO*///			return An + Xn + MAKE_INT_8(extension);
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* Full extension format */
/*TODO*///	
/*TODO*///		USE_CYCLES(m68ki_ea_idx_cycle_table[extension&0x3f]);
/*TODO*///	
/*TODO*///		/* Check if base register is present */
/*TODO*///		if(BIT_7(extension))                /* BS */
/*TODO*///			An = 0;                         /* An */
/*TODO*///	
/*TODO*///		/* Check if index is present */
/*TODO*///		if(!BIT_6(extension))               /* IS */
/*TODO*///		{
/*TODO*///			Xn = REG_DA[extension>>12];     /* Xn */
/*TODO*///			if(!BIT_B(extension))           /* W/L */
/*TODO*///				Xn = MAKE_INT_16(Xn);
/*TODO*///			Xn <<= (extension>>9) & 3;      /* SCALE */
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* Check if base displacement is present */
/*TODO*///		if(BIT_5(extension))                /* BD SIZE */
/*TODO*///			bd = BIT_4(extension) ? m68ki_read_imm_32() : MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///	
/*TODO*///		/* If no indirect action, we are done */
/*TODO*///		if(!(extension&7))                  /* No Memory Indirect */
/*TODO*///			return An + bd + Xn;
/*TODO*///	
/*TODO*///		/* Check if outer displacement is present */
/*TODO*///		if(BIT_1(extension))                /* I/IS:  od */
/*TODO*///			od = BIT_0(extension) ? m68ki_read_imm_32() : MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///	
/*TODO*///		/* Postindex */
/*TODO*///		if(BIT_2(extension))                /* I/IS:  0 = preindex, 1 = postindex */
/*TODO*///			return m68ki_read_32(An + bd) + Xn + od;
/*TODO*///	
/*TODO*///		/* Preindex */
/*TODO*///		return m68ki_read_32(An + bd + Xn) + od;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Fetch operands */
/*TODO*///	INLINE uint OPER_AY_AI_8(void)  {uint ea = EA_AY_AI_8();  return m68ki_read_8(ea); }
/*TODO*///	INLINE uint OPER_AY_AI_16(void) {uint ea = EA_AY_AI_16(); return m68ki_read_16(ea);}
/*TODO*///	INLINE uint OPER_AY_AI_32(void) {uint ea = EA_AY_AI_32(); return m68ki_read_32(ea);}
/*TODO*///	INLINE uint OPER_AY_PI_8(void)  {uint ea = EA_AY_PI_8();  return m68ki_read_8(ea); }
/*TODO*///	INLINE uint OPER_AY_PI_16(void) {uint ea = EA_AY_PI_16(); return m68ki_read_16(ea);}
/*TODO*///	INLINE uint OPER_AY_PI_32(void) {uint ea = EA_AY_PI_32(); return m68ki_read_32(ea);}
/*TODO*///	INLINE uint OPER_AY_PD_8(void)  {uint ea = EA_AY_PD_8();  return m68ki_read_8(ea); }
/*TODO*///	INLINE uint OPER_AY_PD_16(void) {uint ea = EA_AY_PD_16(); return m68ki_read_16(ea);}
/*TODO*///	INLINE uint OPER_AY_PD_32(void) {uint ea = EA_AY_PD_32(); return m68ki_read_32(ea);}
/*TODO*///	INLINE uint OPER_AY_DI_8(void)  {uint ea = EA_AY_DI_8();  return m68ki_read_8(ea); }
/*TODO*///	INLINE uint OPER_AY_DI_16(void) {uint ea = EA_AY_DI_16(); return m68ki_read_16(ea);}
/*TODO*///	INLINE uint OPER_AY_DI_32(void) {uint ea = EA_AY_DI_32(); return m68ki_read_32(ea);}
/*TODO*///	INLINE uint OPER_AY_IX_8(void)  {uint ea = EA_AY_IX_8();  return m68ki_read_8(ea); }
/*TODO*///	INLINE uint OPER_AY_IX_16(void) {uint ea = EA_AY_IX_16(); return m68ki_read_16(ea);}
/*TODO*///	INLINE uint OPER_AY_IX_32(void) {uint ea = EA_AY_IX_32(); return m68ki_read_32(ea);}
/*TODO*///	
/*TODO*///	INLINE uint OPER_AX_AI_8(void)  {uint ea = EA_AX_AI_8();  return m68ki_read_8(ea); }
/*TODO*///	INLINE uint OPER_AX_AI_16(void) {uint ea = EA_AX_AI_16(); return m68ki_read_16(ea);}
/*TODO*///	INLINE uint OPER_AX_AI_32(void) {uint ea = EA_AX_AI_32(); return m68ki_read_32(ea);}
/*TODO*///	INLINE uint OPER_AX_PI_8(void)  {uint ea = EA_AX_PI_8();  return m68ki_read_8(ea); }
/*TODO*///	INLINE uint OPER_AX_PI_16(void) {uint ea = EA_AX_PI_16(); return m68ki_read_16(ea);}
/*TODO*///	INLINE uint OPER_AX_PI_32(void) {uint ea = EA_AX_PI_32(); return m68ki_read_32(ea);}
/*TODO*///	INLINE uint OPER_AX_PD_8(void)  {uint ea = EA_AX_PD_8();  return m68ki_read_8(ea); }
/*TODO*///	INLINE uint OPER_AX_PD_16(void) {uint ea = EA_AX_PD_16(); return m68ki_read_16(ea);}
/*TODO*///	INLINE uint OPER_AX_PD_32(void) {uint ea = EA_AX_PD_32(); return m68ki_read_32(ea);}
/*TODO*///	INLINE uint OPER_AX_DI_8(void)  {uint ea = EA_AX_DI_8();  return m68ki_read_8(ea); }
/*TODO*///	INLINE uint OPER_AX_DI_16(void) {uint ea = EA_AX_DI_16(); return m68ki_read_16(ea);}
/*TODO*///	INLINE uint OPER_AX_DI_32(void) {uint ea = EA_AX_DI_32(); return m68ki_read_32(ea);}
/*TODO*///	INLINE uint OPER_AX_IX_8(void)  {uint ea = EA_AX_IX_8();  return m68ki_read_8(ea); }
/*TODO*///	INLINE uint OPER_AX_IX_16(void) {uint ea = EA_AX_IX_16(); return m68ki_read_16(ea);}
/*TODO*///	INLINE uint OPER_AX_IX_32(void) {uint ea = EA_AX_IX_32(); return m68ki_read_32(ea);}
/*TODO*///	
/*TODO*///	INLINE uint OPER_A7_PI_8(void)  {uint ea = EA_A7_PI_8();  return m68ki_read_8(ea); }
/*TODO*///	INLINE uint OPER_A7_PD_8(void)  {uint ea = EA_A7_PD_8();  return m68ki_read_8(ea); }
/*TODO*///	
/*TODO*///	INLINE uint OPER_AW_8(void)     {uint ea = EA_AW_8();     return m68ki_read_8(ea); }
/*TODO*///	INLINE uint OPER_AW_16(void)    {uint ea = EA_AW_16();    return m68ki_read_16(ea);}
/*TODO*///	INLINE uint OPER_AW_32(void)    {uint ea = EA_AW_32();    return m68ki_read_32(ea);}
/*TODO*///	INLINE uint OPER_AL_8(void)     {uint ea = EA_AL_8();     return m68ki_read_8(ea); }
/*TODO*///	INLINE uint OPER_AL_16(void)    {uint ea = EA_AL_16();    return m68ki_read_16(ea);}
/*TODO*///	INLINE uint OPER_AL_32(void)    {uint ea = EA_AL_32();    return m68ki_read_32(ea);}
/*TODO*///	INLINE uint OPER_PCDI_8(void)   {uint ea = EA_PCDI_8();   return m68ki_read_pcrel_8(ea); }
/*TODO*///	INLINE uint OPER_PCDI_16(void)  {uint ea = EA_PCDI_16();  return m68ki_read_pcrel_16(ea);}
/*TODO*///	INLINE uint OPER_PCDI_32(void)  {uint ea = EA_PCDI_32();  return m68ki_read_pcrel_32(ea);}
/*TODO*///	INLINE uint OPER_PCIX_8(void)   {uint ea = EA_PCIX_8();   return m68ki_read_pcrel_8(ea); }
/*TODO*///	INLINE uint OPER_PCIX_16(void)  {uint ea = EA_PCIX_16();  return m68ki_read_pcrel_16(ea);}
/*TODO*///	INLINE uint OPER_PCIX_32(void)  {uint ea = EA_PCIX_32();  return m68ki_read_pcrel_32(ea);}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ---------------------------- Stack Functions --------------------------- */
/*TODO*///	
/*TODO*///	/* Push/pull data from the stack */
/*TODO*///	INLINE void m68ki_push_16(uint value)
/*TODO*///	{
/*TODO*///		REG_SP = MASK_OUT_ABOVE_32(REG_SP - 2);
/*TODO*///		m68ki_write_16(REG_SP, value);
/*TODO*///	}
/*TODO*///	
/*TODO*///	INLINE void m68ki_push_32(uint value)
/*TODO*///	{
/*TODO*///		REG_SP = MASK_OUT_ABOVE_32(REG_SP - 4);
/*TODO*///		m68ki_write_32(REG_SP, value);
/*TODO*///	}
/*TODO*///	
/*TODO*///	INLINE uint m68ki_pull_16(void)
/*TODO*///	{
/*TODO*///		REG_SP = MASK_OUT_ABOVE_32(REG_SP + 2);
/*TODO*///		return m68ki_read_16(REG_SP-2);
/*TODO*///	}
/*TODO*///	
/*TODO*///	INLINE uint m68ki_pull_32(void)
/*TODO*///	{
/*TODO*///		REG_SP = MASK_OUT_ABOVE_32(REG_SP + 4);
/*TODO*///		return m68ki_read_32(REG_SP-4);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Increment/decrement the stack as if doing a push/pull but
/*TODO*///	 * don't do any memory access.
/*TODO*///	 */
/*TODO*///	INLINE void m68ki_fake_push_16(void)
/*TODO*///	{
/*TODO*///		REG_SP = MASK_OUT_ABOVE_32(REG_SP - 2);
/*TODO*///	}
/*TODO*///	
/*TODO*///	INLINE void m68ki_fake_push_32(void)
/*TODO*///	{
/*TODO*///		REG_SP = MASK_OUT_ABOVE_32(REG_SP - 4);
/*TODO*///	}
/*TODO*///	
/*TODO*///	INLINE void m68ki_fake_pull_16(void)
/*TODO*///	{
/*TODO*///		REG_SP = MASK_OUT_ABOVE_32(REG_SP + 2);
/*TODO*///	}
/*TODO*///	
/*TODO*///	INLINE void m68ki_fake_pull_32(void)
/*TODO*///	{
/*TODO*///		REG_SP = MASK_OUT_ABOVE_32(REG_SP + 4);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ----------------------------- Program Flow ----------------------------- */
/*TODO*///	
/*TODO*///	/* Jump to a new program location or vector.
/*TODO*///	 * These functions will also call the pc_changed callback if it was enabled
/*TODO*///	 * in m68kconf.h.
/*TODO*///	 */
/*TODO*///	INLINE void m68ki_jump(uint new_pc)
/*TODO*///	{
/*TODO*///		REG_PC = new_pc;
/*TODO*///		m68ki_pc_changed(REG_PC);
/*TODO*///	}
/*TODO*///	
/*TODO*///	INLINE void m68ki_jump_vector(uint vector)
/*TODO*///	{
/*TODO*///		REG_PC = (vector<<2) + REG_VBR;
/*TODO*///		REG_PC = m68ki_read_data_32(REG_PC);
/*TODO*///		m68ki_pc_changed(REG_PC);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Branch to a new memory location.
/*TODO*///	 * The 32-bit branch will call pc_changed if it was enabled in m68kconf.h.
/*TODO*///	 * So far I've found no problems with not calling pc_changed for 8 or 16
/*TODO*///	 * bit branches.
/*TODO*///	 */
/*TODO*///	INLINE void m68ki_branch_8(uint offset)
/*TODO*///	{
/*TODO*///		REG_PC += MAKE_INT_8(offset);
/*TODO*///	}
/*TODO*///	
/*TODO*///	INLINE void m68ki_branch_16(uint offset)
/*TODO*///	{
/*TODO*///		REG_PC += MAKE_INT_16(offset);
/*TODO*///	}
/*TODO*///	
/*TODO*///	INLINE void m68ki_branch_32(uint offset)
/*TODO*///	{
/*TODO*///		REG_PC += offset;
/*TODO*///		m68ki_pc_changed(REG_PC);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ---------------------------- Status Register --------------------------- */
/*TODO*///	
/*TODO*///	/* Set the S flag and change the active stack pointer.
/*TODO*///	 * Note that value MUST be 4 or 0.
/*TODO*///	 */
/*TODO*///	INLINE void m68ki_set_s_flag(uint value)
/*TODO*///	{
/*TODO*///		/* Backup the old stack pointer */
/*TODO*///		REG_SP_BASE[FLAG_S | ((FLAG_S>>1) & FLAG_M)] = REG_SP;
/*TODO*///		/* Set the S flag */
/*TODO*///		FLAG_S = value;
/*TODO*///		/* Set the new stack pointer */
/*TODO*///		REG_SP = REG_SP_BASE[FLAG_S | ((FLAG_S>>1) & FLAG_M)];
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Set the S and M flags and change the active stack pointer.
/*TODO*///	 * Note that value MUST be 0, 2, 4, or 6 (bit2 = S, bit1 = M).
/*TODO*///	 */
/*TODO*///	INLINE void m68ki_set_sm_flag(uint value)
/*TODO*///	{
/*TODO*///		/* Backup the old stack pointer */
/*TODO*///		REG_SP_BASE[FLAG_S | ((FLAG_S>>1) & FLAG_M)] = REG_SP;
/*TODO*///		/* Set the S and M flags */
/*TODO*///		FLAG_S = value & SFLAG_SET;
/*TODO*///		FLAG_M = value & MFLAG_SET;
/*TODO*///		/* Set the new stack pointer */
/*TODO*///		REG_SP = REG_SP_BASE[FLAG_S | ((FLAG_S>>1) & FLAG_M)];
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Set the S and M flags.  Don't touch the stack pointer. */
/*TODO*///	INLINE void m68ki_set_sm_flag_nosp(uint value)
/*TODO*///	{
/*TODO*///		/* Set the S and M flags */
/*TODO*///		FLAG_S = value & SFLAG_SET;
/*TODO*///		FLAG_M = value & MFLAG_SET;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Set the condition code register */
/*TODO*///	INLINE void m68ki_set_ccr(uint value)
/*TODO*///	{
/*TODO*///		FLAG_X = BIT_4(value)  << 4;
/*TODO*///		FLAG_N = BIT_3(value)  << 4;
/*TODO*///		FLAG_Z = !BIT_2(value);
/*TODO*///		FLAG_V = BIT_1(value)  << 6;
/*TODO*///		FLAG_C = BIT_0(value)  << 8;
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Set the status register but don't check for interrupts */
/*TODO*///	INLINE void m68ki_set_sr_noint(uint value)
/*TODO*///	{
/*TODO*///		/* Mask out the "unimplemented" bits */
/*TODO*///		value &= CPU_SR_MASK;
/*TODO*///	
/*TODO*///		/* Now set the status register */
/*TODO*///		FLAG_T1 = BIT_F(value);
/*TODO*///		FLAG_T0 = BIT_E(value);
/*TODO*///		FLAG_INT_MASK = value & 0x0700;
/*TODO*///		m68ki_set_ccr(value);
/*TODO*///		m68ki_set_sm_flag((value >> 11) & 6);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Set the status register but don't check for interrupts nor
/*TODO*///	 * change the stack pointer
/*TODO*///	 */
/*TODO*///	INLINE void m68ki_set_sr_noint_nosp(uint value)
/*TODO*///	{
/*TODO*///		/* Mask out the "unimplemented" bits */
/*TODO*///		value &= CPU_SR_MASK;
/*TODO*///	
/*TODO*///		/* Now set the status register */
/*TODO*///		FLAG_T1 = BIT_F(value);
/*TODO*///		FLAG_T0 = BIT_E(value);
/*TODO*///		FLAG_INT_MASK = value & 0x0700;
/*TODO*///		m68ki_set_ccr(value);
/*TODO*///		m68ki_set_sm_flag_nosp((value >> 11) & 6);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Set the status register and check for interrupts */
/*TODO*///	INLINE void m68ki_set_sr(uint value)
/*TODO*///	{
/*TODO*///		m68ki_set_sr_noint(value);
/*TODO*///		m68ki_check_interrupts();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ------------------------- Exception Processing ------------------------- */
/*TODO*///	
/*TODO*///	/* Initiate exception processing */
/*TODO*///	INLINE uint m68ki_init_exception(void)
/*TODO*///	{
/*TODO*///		/* Save the old status register */
/*TODO*///		uint sr = m68ki_get_sr();
/*TODO*///	
/*TODO*///		/* Turn off trace flag, clear pending traces */
/*TODO*///		FLAG_T1 = FLAG_T0 = 0;
/*TODO*///		m68ki_clear_trace();
/*TODO*///		/* Enter supervisor mode */
/*TODO*///		m68ki_set_s_flag(SFLAG_SET);
/*TODO*///	
/*TODO*///		return sr;
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* 3 word stack frame (68000 only) */
/*TODO*///	INLINE void m68ki_stack_frame_3word(uint pc, uint sr)
/*TODO*///	{
/*TODO*///		m68ki_push_32(pc);
/*TODO*///		m68ki_push_16(sr);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Format 0 stack frame.
/*TODO*///	 * This is the standard stack frame for 68010+.
/*TODO*///	 */
/*TODO*///	INLINE void m68ki_stack_frame_0000(uint pc, uint sr, uint vector)
/*TODO*///	{
/*TODO*///		/* Stack a 3-word frame if we are 68000 */
/*TODO*///		if(CPU_TYPE == CPU_TYPE_000)
/*TODO*///		{
/*TODO*///			m68ki_stack_frame_3word(pc, sr);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_push_16(vector<<2);
/*TODO*///		m68ki_push_32(pc);
/*TODO*///		m68ki_push_16(sr);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Format 1 stack frame (68020).
/*TODO*///	 * For 68020, this is the 4 word throwaway frame.
/*TODO*///	 */
/*TODO*///	INLINE void m68ki_stack_frame_0001(uint pc, uint sr, uint vector)
/*TODO*///	{
/*TODO*///		m68ki_push_16(0x1000 | (vector<<2));
/*TODO*///		m68ki_push_32(pc);
/*TODO*///		m68ki_push_16(sr);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Format 2 stack frame.
/*TODO*///	 * This is used only by 68020 for trap exceptions.
/*TODO*///	 */
/*TODO*///	INLINE void m68ki_stack_frame_0010(uint sr, uint vector)
/*TODO*///	{
/*TODO*///		m68ki_push_32(REG_PPC);
/*TODO*///		m68ki_push_16(0x2000 | (vector<<2));
/*TODO*///		m68ki_push_32(REG_PC);
/*TODO*///		m68ki_push_16(sr);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Bus error stack frame (68000 only).
/*TODO*///	 */
/*TODO*///	INLINE void m68ki_stack_frame_buserr(uint pc, uint sr, uint address, uint write, uint instruction, uint fc)
/*TODO*///	{
/*TODO*///		m68ki_push_32(pc);
/*TODO*///		m68ki_push_16(sr);
/*TODO*///		m68ki_push_16(REG_IR);
/*TODO*///		m68ki_push_32(address);	/* access address */
/*TODO*///		/* 0 0 0 0 0 0 0 0 0 0 0 R/W I/N FC
/*TODO*///		 * R/W  0 = write, 1 = read
/*TODO*///		 * I/N  0 = instruction, 1 = not
/*TODO*///		 * FC   3-bit function code
/*TODO*///		 */
/*TODO*///		m68ki_push_16(((!write)<<4) | ((!instruction)<<3) | fc);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Format 8 stack frame (68010).
/*TODO*///	 * 68010 only.  This is the 29 word bus/address error frame.
/*TODO*///	 */
/*TODO*///	void m68ki_stack_frame_1000(uint pc, uint sr, uint vector)
/*TODO*///	{
/*TODO*///		/* VERSION
/*TODO*///		 * NUMBER
/*TODO*///		 * INTERNAL INFORMATION, 16 WORDS
/*TODO*///		 */
/*TODO*///		m68ki_fake_push_32();
/*TODO*///		m68ki_fake_push_32();
/*TODO*///		m68ki_fake_push_32();
/*TODO*///		m68ki_fake_push_32();
/*TODO*///		m68ki_fake_push_32();
/*TODO*///		m68ki_fake_push_32();
/*TODO*///		m68ki_fake_push_32();
/*TODO*///		m68ki_fake_push_32();
/*TODO*///	
/*TODO*///		/* INSTRUCTION INPUT BUFFER */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* UNUSED, RESERVED (not written) */
/*TODO*///		m68ki_fake_push_16();
/*TODO*///	
/*TODO*///		/* DATA INPUT BUFFER */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* UNUSED, RESERVED (not written) */
/*TODO*///		m68ki_fake_push_16();
/*TODO*///	
/*TODO*///		/* DATA OUTPUT BUFFER */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* UNUSED, RESERVED (not written) */
/*TODO*///		m68ki_fake_push_16();
/*TODO*///	
/*TODO*///		/* FAULT ADDRESS */
/*TODO*///		m68ki_push_32(0);
/*TODO*///	
/*TODO*///		/* SPECIAL STATUS WORD */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* 1000, VECTOR OFFSET */
/*TODO*///		m68ki_push_16(0x8000 | (vector<<2));
/*TODO*///	
/*TODO*///		/* PROGRAM COUNTER */
/*TODO*///		m68ki_push_32(pc);
/*TODO*///	
/*TODO*///		/* STATUS REGISTER */
/*TODO*///		m68ki_push_16(sr);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Format A stack frame (short bus fault).
/*TODO*///	 * This is used only by 68020 for bus fault and address error
/*TODO*///	 * if the error happens at an instruction boundary.
/*TODO*///	 * PC stacked is address of next instruction.
/*TODO*///	 */
/*TODO*///	void m68ki_stack_frame_1010(uint sr, uint vector, uint pc)
/*TODO*///	{
/*TODO*///		/* INTERNAL REGISTER */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* INTERNAL REGISTER */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* DATA OUTPUT BUFFER (2 words) */
/*TODO*///		m68ki_push_32(0);
/*TODO*///	
/*TODO*///		/* INTERNAL REGISTER */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* INTERNAL REGISTER */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* DATA CYCLE FAULT ADDRESS (2 words) */
/*TODO*///		m68ki_push_32(0);
/*TODO*///	
/*TODO*///		/* INSTRUCTION PIPE STAGE B */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* INSTRUCTION PIPE STAGE C */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* SPECIAL STATUS REGISTER */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* INTERNAL REGISTER */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* 1010, VECTOR OFFSET */
/*TODO*///		m68ki_push_16(0xa000 | (vector<<2));
/*TODO*///	
/*TODO*///		/* PROGRAM COUNTER */
/*TODO*///		m68ki_push_32(pc);
/*TODO*///	
/*TODO*///		/* STATUS REGISTER */
/*TODO*///		m68ki_push_16(sr);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Format B stack frame (long bus fault).
/*TODO*///	 * This is used only by 68020 for bus fault and address error
/*TODO*///	 * if the error happens during instruction execution.
/*TODO*///	 * PC stacked is address of instruction in progress.
/*TODO*///	 */
/*TODO*///	void m68ki_stack_frame_1011(uint sr, uint vector, uint pc)
/*TODO*///	{
/*TODO*///		/* INTERNAL REGISTERS (18 words) */
/*TODO*///		m68ki_push_32(0);
/*TODO*///		m68ki_push_32(0);
/*TODO*///		m68ki_push_32(0);
/*TODO*///		m68ki_push_32(0);
/*TODO*///		m68ki_push_32(0);
/*TODO*///		m68ki_push_32(0);
/*TODO*///		m68ki_push_32(0);
/*TODO*///		m68ki_push_32(0);
/*TODO*///		m68ki_push_32(0);
/*TODO*///	
/*TODO*///		/* VERSION# (4 bits), INTERNAL INFORMATION */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* INTERNAL REGISTERS (3 words) */
/*TODO*///		m68ki_push_32(0);
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* DATA INTPUT BUFFER (2 words) */
/*TODO*///		m68ki_push_32(0);
/*TODO*///	
/*TODO*///		/* INTERNAL REGISTERS (2 words) */
/*TODO*///		m68ki_push_32(0);
/*TODO*///	
/*TODO*///		/* STAGE B ADDRESS (2 words) */
/*TODO*///		m68ki_push_32(0);
/*TODO*///	
/*TODO*///		/* INTERNAL REGISTER (4 words) */
/*TODO*///		m68ki_push_32(0);
/*TODO*///		m68ki_push_32(0);
/*TODO*///	
/*TODO*///		/* DATA OUTPUT BUFFER (2 words) */
/*TODO*///		m68ki_push_32(0);
/*TODO*///	
/*TODO*///		/* INTERNAL REGISTER */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* INTERNAL REGISTER */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* DATA CYCLE FAULT ADDRESS (2 words) */
/*TODO*///		m68ki_push_32(0);
/*TODO*///	
/*TODO*///		/* INSTRUCTION PIPE STAGE B */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* INSTRUCTION PIPE STAGE C */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* SPECIAL STATUS REGISTER */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* INTERNAL REGISTER */
/*TODO*///		m68ki_push_16(0);
/*TODO*///	
/*TODO*///		/* 1011, VECTOR OFFSET */
/*TODO*///		m68ki_push_16(0xb000 | (vector<<2));
/*TODO*///	
/*TODO*///		/* PROGRAM COUNTER */
/*TODO*///		m68ki_push_32(pc);
/*TODO*///	
/*TODO*///		/* STATUS REGISTER */
/*TODO*///		m68ki_push_16(sr);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Used for Group 2 exceptions.
/*TODO*///	 * These stack a type 2 frame on the 020.
/*TODO*///	 */
/*TODO*///	INLINE void m68ki_exception_trap(uint vector)
/*TODO*///	{
/*TODO*///		uint sr = m68ki_init_exception();
/*TODO*///	
/*TODO*///		if(CPU_TYPE_IS_010_LESS(CPU_TYPE))
/*TODO*///			m68ki_stack_frame_0000(REG_PC, sr, vector);
/*TODO*///		else
/*TODO*///			m68ki_stack_frame_0010(sr, vector);
/*TODO*///	
/*TODO*///		m68ki_jump_vector(vector);
/*TODO*///	
/*TODO*///		/* Use up some clock cycles */
/*TODO*///		USE_CYCLES(CYC_EXCEPTION[vector]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Trap#n stacks a 0 frame but behaves like group2 otherwise */
/*TODO*///	INLINE void m68ki_exception_trapN(uint vector)
/*TODO*///	{
/*TODO*///		uint sr = m68ki_init_exception();
/*TODO*///		m68ki_stack_frame_0000(REG_PC, sr, vector);
/*TODO*///		m68ki_jump_vector(vector);
/*TODO*///	
/*TODO*///		/* Use up some clock cycles */
/*TODO*///		USE_CYCLES(CYC_EXCEPTION[vector]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Exception for trace mode */
/*TODO*///	INLINE void m68ki_exception_trace(void)
/*TODO*///	{
/*TODO*///		uint sr = m68ki_init_exception();
/*TODO*///	
/*TODO*///		if(CPU_TYPE_IS_010_LESS(CPU_TYPE))
/*TODO*///			m68ki_stack_frame_0000(REG_PC, sr, EXCEPTION_TRACE);
/*TODO*///		else
/*TODO*///			m68ki_stack_frame_0010(sr, EXCEPTION_TRACE);
/*TODO*///	
/*TODO*///		m68ki_jump_vector(EXCEPTION_TRACE);
/*TODO*///	
/*TODO*///		/* Trace nullifies a STOP instruction */
/*TODO*///		CPU_STOPPED &= ~STOP_LEVEL_STOP;
/*TODO*///	
/*TODO*///		/* Use up some clock cycles */
/*TODO*///		USE_CYCLES(CYC_EXCEPTION[EXCEPTION_TRACE]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Exception for privilege violation */
/*TODO*///	INLINE void m68ki_exception_privilege_violation(void)
/*TODO*///	{
/*TODO*///		uint sr = m68ki_init_exception();
/*TODO*///		m68ki_stack_frame_0000(REG_PC, sr, EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///		m68ki_jump_vector(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///	
/*TODO*///		/* Use up some clock cycles and undo the instruction's cycles */
/*TODO*///		USE_CYCLES(CYC_EXCEPTION[EXCEPTION_PRIVILEGE_VIOLATION] - CYC_INSTRUCTION[REG_IR]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Exception for A-Line instructions */
/*TODO*///	INLINE void m68ki_exception_1010(void)
/*TODO*///	{
/*TODO*///		uint sr;
/*TODO*///	#if M68K_LOG_1010_1111 == OPT_ON
/*TODO*///		M68K_DO_LOG_EMU((M68K_LOG_FILEHANDLE "%s at %08x: called 1010 instruction %04x (%s)\n",
/*TODO*///						 m68ki_cpu_names[CPU_TYPE], ADDRESS_68K(REG_PPC), REG_IR,
/*TODO*///						 m68ki_disassemble_quick(ADDRESS_68K(REG_PPC))));
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		sr = m68ki_init_exception();
/*TODO*///		m68ki_stack_frame_0000(REG_PC-2, sr, EXCEPTION_1010);
/*TODO*///		m68ki_jump_vector(EXCEPTION_1010);
/*TODO*///	
/*TODO*///		/* Use up some clock cycles and undo the instruction's cycles */
/*TODO*///		USE_CYCLES(CYC_EXCEPTION[EXCEPTION_1010] - CYC_INSTRUCTION[REG_IR]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Exception for F-Line instructions */
/*TODO*///	INLINE void m68ki_exception_1111(void)
/*TODO*///	{
/*TODO*///		uint sr;
/*TODO*///	
/*TODO*///	#if M68K_LOG_1010_1111 == OPT_ON
/*TODO*///		M68K_DO_LOG_EMU((M68K_LOG_FILEHANDLE "%s at %08x: called 1111 instruction %04x (%s)\n",
/*TODO*///						 m68ki_cpu_names[CPU_TYPE], ADDRESS_68K(REG_PPC), REG_IR,
/*TODO*///						 m68ki_disassemble_quick(ADDRESS_68K(REG_PPC))));
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		sr = m68ki_init_exception();
/*TODO*///		m68ki_stack_frame_0000(REG_PC-2, sr, EXCEPTION_1111);
/*TODO*///		m68ki_jump_vector(EXCEPTION_1111);
/*TODO*///	
/*TODO*///		/* Use up some clock cycles and undo the instruction's cycles */
/*TODO*///		USE_CYCLES(CYC_EXCEPTION[EXCEPTION_1111] - CYC_INSTRUCTION[REG_IR]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Exception for illegal instructions */
/*TODO*///	INLINE void m68ki_exception_illegal(void)
/*TODO*///	{
/*TODO*///		uint sr;
/*TODO*///	
/*TODO*///		M68K_DO_LOG((M68K_LOG_FILEHANDLE "%s at %08x: illegal instruction %04x (%s)\n",
/*TODO*///					 m68ki_cpu_names[CPU_TYPE], ADDRESS_68K(REG_PPC), REG_IR,
/*TODO*///					 m68ki_disassemble_quick(ADDRESS_68K(REG_PPC))));
/*TODO*///	
/*TODO*///		sr = m68ki_init_exception();
/*TODO*///		m68ki_stack_frame_0000(REG_PC, sr, EXCEPTION_ILLEGAL_INSTRUCTION);
/*TODO*///		m68ki_jump_vector(EXCEPTION_ILLEGAL_INSTRUCTION);
/*TODO*///	
/*TODO*///		/* Use up some clock cycles and undo the instruction's cycles */
/*TODO*///		USE_CYCLES(CYC_EXCEPTION[EXCEPTION_ILLEGAL_INSTRUCTION] - CYC_INSTRUCTION[REG_IR]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Exception for format errror in RTE */
/*TODO*///	INLINE void m68ki_exception_format_error(void)
/*TODO*///	{
/*TODO*///		uint sr = m68ki_init_exception();
/*TODO*///		m68ki_stack_frame_0000(REG_PC, sr, EXCEPTION_FORMAT_ERROR);
/*TODO*///		m68ki_jump_vector(EXCEPTION_FORMAT_ERROR);
/*TODO*///	
/*TODO*///		/* Use up some clock cycles and undo the instruction's cycles */
/*TODO*///		USE_CYCLES(CYC_EXCEPTION[EXCEPTION_FORMAT_ERROR] - CYC_INSTRUCTION[REG_IR]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Exception for address error */
/*TODO*///	INLINE void m68ki_exception_address_error(void)
/*TODO*///	{
/*TODO*///		/* Not emulated yet */
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Service an interrupt request and start exception processing */
/*TODO*///	void m68ki_exception_interrupt(uint int_level)
/*TODO*///	{
/*TODO*///		uint vector;
/*TODO*///		uint sr;
/*TODO*///		uint new_pc;
/*TODO*///	
/*TODO*///		/* Turn off the stopped state */
/*TODO*///		CPU_STOPPED &= ~STOP_LEVEL_STOP;
/*TODO*///	
/*TODO*///		/* If we are halted, don't do anything */
/*TODO*///		if (CPU_STOPPED != 0)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* Acknowledge the interrupt */
/*TODO*///		vector = m68ki_int_ack(int_level);
/*TODO*///	
/*TODO*///		/* Get the interrupt vector */
/*TODO*///		if(vector == M68K_INT_ACK_AUTOVECTOR)
/*TODO*///			/* Use the autovectors.  This is the most commonly used implementation */
/*TODO*///			vector = EXCEPTION_INTERRUPT_AUTOVECTOR+int_level;
/*TODO*///		else if(vector == M68K_INT_ACK_SPURIOUS)
/*TODO*///			/* Called if no devices respond to the interrupt acknowledge */
/*TODO*///			vector = EXCEPTION_SPURIOUS_INTERRUPT;
/*TODO*///		else if(vector > 255)
/*TODO*///		{
/*TODO*///			M68K_DO_LOG_EMU((M68K_LOG_FILEHANDLE "%s at %08x: Interrupt acknowledge returned invalid vector $%x\n",
/*TODO*///					 m68ki_cpu_names[CPU_TYPE], ADDRESS_68K(REG_PC), vector));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* Start exception processing */
/*TODO*///		sr = m68ki_init_exception();
/*TODO*///	
/*TODO*///		/* Set the interrupt mask to the level of the one being serviced */
/*TODO*///		FLAG_INT_MASK = int_level<<8;
/*TODO*///	
/*TODO*///		/* Get the new PC */
/*TODO*///		new_pc = m68ki_read_data_32((vector<<2) + REG_VBR);
/*TODO*///	
/*TODO*///		/* If vector is uninitialized, call the uninitialized interrupt vector */
/*TODO*///		if(new_pc == 0)
/*TODO*///			new_pc = m68ki_read_data_32((EXCEPTION_UNINITIALIZED_INTERRUPT<<2) + REG_VBR);
/*TODO*///	
/*TODO*///		/* Generate a stack frame */
/*TODO*///		m68ki_stack_frame_0000(REG_PC, sr, vector);
/*TODO*///		if(FLAG_M && CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			/* Create throwaway frame */
/*TODO*///			m68ki_set_sm_flag(FLAG_S);	/* clear M */
/*TODO*///			sr |= 0x2000; /* Same as SR in master stack frame except S is forced high */
/*TODO*///			m68ki_stack_frame_0001(REG_PC, sr, vector);
/*TODO*///		}
/*TODO*///	
/*TODO*///		m68ki_jump(new_pc);
/*TODO*///	
/*TODO*///		/* Defer cycle counting until later */
/*TODO*///		CPU_INT_CYCLES += CYC_EXCEPTION[vector];
/*TODO*///	
/*TODO*///	#if !M68K_EMULATE_INT_ACK
/*TODO*///		/* Automatically clear IRQ if we are not using an acknowledge scheme */
/*TODO*///		CPU_INT_LEVEL = 0;
/*TODO*///	#endif /* M68K_EMULATE_INT_ACK */
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ASG: Check for interrupts */
/*TODO*///	INLINE void m68ki_check_interrupts(void)
/*TODO*///	{
/*TODO*///		if(CPU_INT_LEVEL > FLAG_INT_MASK)
/*TODO*///			m68ki_exception_interrupt(CPU_INT_LEVEL>>8);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ======================================================================== */
/*TODO*///	/* ============================== END OF FILE ============================= */
/*TODO*///	/* ======================================================================== */
/*TODO*///	
/*TODO*///	#endif /* M68KCPU__HEADER */
}
