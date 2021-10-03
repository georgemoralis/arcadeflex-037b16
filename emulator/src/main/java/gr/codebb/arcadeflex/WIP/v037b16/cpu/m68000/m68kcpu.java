

/* ======================================================================== */
/* ========================= LICENSING & COPYRIGHT ======================== */
/* ======================================================================== */

//#if 0
//static const char* copyright_notice =
//"MUSASHI\n"
//"Version 3.3 (2001-01-29)\n"
//"A portable Motorola M680x0 processor emulation engine.\n"
//"Copyright 1998-2001 Karl Stenerud.  All rights reserved.\n"
//"\n"
//"This code may be freely used for non-commercial purpooses as long as this\n"
//"copyright notice remains unaltered in the source code and any binary files\n"
//"containing this code in compiled form.\n"
//"\n"
//"All other lisencing terms must be negotiated with the author\n"
//"(Karl Stenerud).\n"
//"\n"
//"The latest version of this code can be obtained at:\n"
//"http://kstenerud.cjb.net\n"
//;
//#endif


/* ======================================================================== */
/* ================================= NOTES ================================ */
/* ======================================================================== */



/* ======================================================================== */
/* ================================ INCLUDES ============================== */
/* ======================================================================== */

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.cpu.m68000;

import static gr.codebb.arcadeflex.WIP.v037b16.cpu.m68000.m68kcpuH.*;

public class m68kcpu
{
/*TODO*///	
/*TODO*///	/* ======================================================================== */
/*TODO*///	/* ================================= DATA ================================= */
/*TODO*///	/* ======================================================================== */
/*TODO*///	
/*TODO*///	int  m68ki_initial_cycles;
/*TODO*///	int  m68ki_remaining_cycles = 0;                     /* Number of clocks remaining */
/*TODO*///	uint m68ki_tracing = 0;
/*TODO*///	uint m68ki_address_space;
/*TODO*///	
/*TODO*///	#ifdef M68K_LOG_ENABLE
/*TODO*///	char* m68ki_cpu_names[9] =
/*TODO*///	{
/*TODO*///		"Invalid CPU",
/*TODO*///		"M68000",
/*TODO*///		"M68010",
/*TODO*///		"Invalid CPU",
/*TODO*///		"M68EC020"
/*TODO*///		"Invalid CPU",
/*TODO*///		"Invalid CPU",
/*TODO*///		"Invalid CPU",
/*TODO*///		"M68020"
/*TODO*///	};
/*TODO*///	#endif /* M68K_LOG_ENABLE */
/*TODO*///	
	/* The CPU core */
	public m68ki_cpu_core m68ki_cpu = null;
/*TODO*///	
/*TODO*///	#if M68K_EMULATE_ADDRESS_ERROR
/*TODO*///	jmp_buf m68ki_address_error_trap;
/*TODO*///	#endif /* M68K_EMULATE_ADDRESS_ERROR */
/*TODO*///	
/*TODO*///	/* Used by shift & rotate instructions */
/*TODO*///	uint8 m68ki_shift_8_table[65] =
/*TODO*///	{
/*TODO*///		0x00, 0x80, 0xc0, 0xe0, 0xf0, 0xf8, 0xfc, 0xfe, 0xff, 0xff, 0xff, 0xff,
/*TODO*///		0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
/*TODO*///		0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
/*TODO*///		0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
/*TODO*///		0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
/*TODO*///		0xff, 0xff, 0xff, 0xff, 0xff
/*TODO*///	};
/*TODO*///	uint16 m68ki_shift_16_table[65] =
/*TODO*///	{
/*TODO*///		0x0000, 0x8000, 0xc000, 0xe000, 0xf000, 0xf800, 0xfc00, 0xfe00, 0xff00,
/*TODO*///		0xff80, 0xffc0, 0xffe0, 0xfff0, 0xfff8, 0xfffc, 0xfffe, 0xffff, 0xffff,
/*TODO*///		0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff,
/*TODO*///		0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff,
/*TODO*///		0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff,
/*TODO*///		0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff,
/*TODO*///		0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff,
/*TODO*///		0xffff, 0xffff
/*TODO*///	};
/*TODO*///	uint m68ki_shift_32_table[65] =
/*TODO*///	{
/*TODO*///		0x00000000, 0x80000000, 0xc0000000, 0xe0000000, 0xf0000000, 0xf8000000,
/*TODO*///		0xfc000000, 0xfe000000, 0xff000000, 0xff800000, 0xffc00000, 0xffe00000,
/*TODO*///		0xfff00000, 0xfff80000, 0xfffc0000, 0xfffe0000, 0xffff0000, 0xffff8000,
/*TODO*///		0xffffc000, 0xffffe000, 0xfffff000, 0xfffff800, 0xfffffc00, 0xfffffe00,
/*TODO*///		0xffffff00, 0xffffff80, 0xffffffc0, 0xffffffe0, 0xfffffff0, 0xfffffff8,
/*TODO*///		0xfffffffc, 0xfffffffe, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
/*TODO*///		0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
/*TODO*///		0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
/*TODO*///		0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
/*TODO*///		0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
/*TODO*///		0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Number of clock cycles to use for exception processing.
/*TODO*///	 * I used 4 for any vectors that are undocumented for processing times.
/*TODO*///	 */
/*TODO*///	uint8 m68ki_exception_cycle_table[3][256] =
/*TODO*///	{
/*TODO*///		{ /* 000 */
/*TODO*///			  4, /*  0: Reset - Initial Stack Pointer                      */
/*TODO*///			  4, /*  1: Reset - Initial Program Counter                    */
/*TODO*///			 50, /*  2: Bus Error                             (unemulated) */
/*TODO*///			 50, /*  3: Address Error                         (unemulated) */
/*TODO*///			 34, /*  4: Illegal Instruction                                */
/*TODO*///			 38, /*  5: Divide by Zero -- ASG: changed from 42             */
/*TODO*///			 40, /*  6: CHK -- ASG: chanaged from 44                       */
/*TODO*///			 34, /*  7: TRAPV                                              */
/*TODO*///			 34, /*  8: Privilege Violation                                */
/*TODO*///			 34, /*  9: Trace                                              */
/*TODO*///			  4, /* 10: 1010                                               */
/*TODO*///			  4, /* 11: 1111                                               */
/*TODO*///			  4, /* 12: RESERVED                                           */
/*TODO*///			  4, /* 13: Coprocessor Protocol Violation        (unemulated) */
/*TODO*///			  4, /* 14: Format Error                                       */
/*TODO*///			 44, /* 15: Uninitialized Interrupt                            */
/*TODO*///			  4, /* 16: RESERVED                                           */
/*TODO*///			  4, /* 17: RESERVED                                           */
/*TODO*///			  4, /* 18: RESERVED                                           */
/*TODO*///			  4, /* 19: RESERVED                                           */
/*TODO*///			  4, /* 20: RESERVED                                           */
/*TODO*///			  4, /* 21: RESERVED                                           */
/*TODO*///			  4, /* 22: RESERVED                                           */
/*TODO*///			  4, /* 23: RESERVED                                           */
/*TODO*///			 44, /* 24: Spurious Interrupt                                 */
/*TODO*///			 44, /* 25: Level 1 Interrupt Autovector                       */
/*TODO*///			 44, /* 26: Level 2 Interrupt Autovector                       */
/*TODO*///			 44, /* 27: Level 3 Interrupt Autovector                       */
/*TODO*///			 44, /* 28: Level 4 Interrupt Autovector                       */
/*TODO*///			 44, /* 29: Level 5 Interrupt Autovector                       */
/*TODO*///			 44, /* 30: Level 6 Interrupt Autovector                       */
/*TODO*///			 44, /* 31: Level 7 Interrupt Autovector                       */
/*TODO*///			 34, /* 32: TRAP #0 -- ASG: chanaged from 38                   */
/*TODO*///			 34, /* 33: TRAP #1                                            */
/*TODO*///			 34, /* 34: TRAP #2                                            */
/*TODO*///			 34, /* 35: TRAP #3                                            */
/*TODO*///			 34, /* 36: TRAP #4                                            */
/*TODO*///			 34, /* 37: TRAP #5                                            */
/*TODO*///			 34, /* 38: TRAP #6                                            */
/*TODO*///			 34, /* 39: TRAP #7                                            */
/*TODO*///			 34, /* 40: TRAP #8                                            */
/*TODO*///			 34, /* 41: TRAP #9                                            */
/*TODO*///			 34, /* 42: TRAP #10                                           */
/*TODO*///			 34, /* 43: TRAP #11                                           */
/*TODO*///			 34, /* 44: TRAP #12                                           */
/*TODO*///			 34, /* 45: TRAP #13                                           */
/*TODO*///			 34, /* 46: TRAP #14                                           */
/*TODO*///			 34, /* 47: TRAP #15                                           */
/*TODO*///			  4, /* 48: FP Branch or Set on Unknown Condition (unemulated) */
/*TODO*///			  4, /* 49: FP Inexact Result                     (unemulated) */
/*TODO*///			  4, /* 50: FP Divide by Zero                     (unemulated) */
/*TODO*///			  4, /* 51: FP Underflow                          (unemulated) */
/*TODO*///			  4, /* 52: FP Operand Error                      (unemulated) */
/*TODO*///			  4, /* 53: FP Overflow                           (unemulated) */
/*TODO*///			  4, /* 54: FP Signaling NAN                      (unemulated) */
/*TODO*///			  4, /* 55: FP Unimplemented Data Type            (unemulated) */
/*TODO*///			  4, /* 56: MMU Configuration Error               (unemulated) */
/*TODO*///			  4, /* 57: MMU Illegal Operation Error           (unemulated) */
/*TODO*///			  4, /* 58: MMU Access Level Violation Error      (unemulated) */
/*TODO*///			  4, /* 59: RESERVED                                           */
/*TODO*///			  4, /* 60: RESERVED                                           */
/*TODO*///			  4, /* 61: RESERVED                                           */
/*TODO*///			  4, /* 62: RESERVED                                           */
/*TODO*///			  4, /* 63: RESERVED                                           */
/*TODO*///			     /* 64-255: User Defined                                   */
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4
/*TODO*///		},
/*TODO*///		{ /* 010 */
/*TODO*///			  4, /*  0: Reset - Initial Stack Pointer                      */
/*TODO*///			  4, /*  1: Reset - Initial Program Counter                    */
/*TODO*///			126, /*  2: Bus Error                             (unemulated) */
/*TODO*///			126, /*  3: Address Error                         (unemulated) */
/*TODO*///			 38, /*  4: Illegal Instruction                                */
/*TODO*///			 44, /*  5: Divide by Zero                                     */
/*TODO*///			 44, /*  6: CHK                                                */
/*TODO*///			 34, /*  7: TRAPV                                              */
/*TODO*///			 38, /*  8: Privilege Violation                                */
/*TODO*///			 38, /*  9: Trace                                              */
/*TODO*///			  4, /* 10: 1010                                               */
/*TODO*///			  4, /* 11: 1111                                               */
/*TODO*///			  4, /* 12: RESERVED                                           */
/*TODO*///			  4, /* 13: Coprocessor Protocol Violation        (unemulated) */
/*TODO*///			  4, /* 14: Format Error                                       */
/*TODO*///			 44, /* 15: Uninitialized Interrupt                            */
/*TODO*///			  4, /* 16: RESERVED                                           */
/*TODO*///			  4, /* 17: RESERVED                                           */
/*TODO*///			  4, /* 18: RESERVED                                           */
/*TODO*///			  4, /* 19: RESERVED                                           */
/*TODO*///			  4, /* 20: RESERVED                                           */
/*TODO*///			  4, /* 21: RESERVED                                           */
/*TODO*///			  4, /* 22: RESERVED                                           */
/*TODO*///			  4, /* 23: RESERVED                                           */
/*TODO*///			 46, /* 24: Spurious Interrupt                                 */
/*TODO*///			 46, /* 25: Level 1 Interrupt Autovector                       */
/*TODO*///			 46, /* 26: Level 2 Interrupt Autovector                       */
/*TODO*///			 46, /* 27: Level 3 Interrupt Autovector                       */
/*TODO*///			 46, /* 28: Level 4 Interrupt Autovector                       */
/*TODO*///			 46, /* 29: Level 5 Interrupt Autovector                       */
/*TODO*///			 46, /* 30: Level 6 Interrupt Autovector                       */
/*TODO*///			 46, /* 31: Level 7 Interrupt Autovector                       */
/*TODO*///			 38, /* 32: TRAP #0                                            */
/*TODO*///			 38, /* 33: TRAP #1                                            */
/*TODO*///			 38, /* 34: TRAP #2                                            */
/*TODO*///			 38, /* 35: TRAP #3                                            */
/*TODO*///			 38, /* 36: TRAP #4                                            */
/*TODO*///			 38, /* 37: TRAP #5                                            */
/*TODO*///			 38, /* 38: TRAP #6                                            */
/*TODO*///			 38, /* 39: TRAP #7                                            */
/*TODO*///			 38, /* 40: TRAP #8                                            */
/*TODO*///			 38, /* 41: TRAP #9                                            */
/*TODO*///			 38, /* 42: TRAP #10                                           */
/*TODO*///			 38, /* 43: TRAP #11                                           */
/*TODO*///			 38, /* 44: TRAP #12                                           */
/*TODO*///			 38, /* 45: TRAP #13                                           */
/*TODO*///			 38, /* 46: TRAP #14                                           */
/*TODO*///			 38, /* 47: TRAP #15                                           */
/*TODO*///			  4, /* 48: FP Branch or Set on Unknown Condition (unemulated) */
/*TODO*///			  4, /* 49: FP Inexact Result                     (unemulated) */
/*TODO*///			  4, /* 50: FP Divide by Zero                     (unemulated) */
/*TODO*///			  4, /* 51: FP Underflow                          (unemulated) */
/*TODO*///			  4, /* 52: FP Operand Error                      (unemulated) */
/*TODO*///			  4, /* 53: FP Overflow                           (unemulated) */
/*TODO*///			  4, /* 54: FP Signaling NAN                      (unemulated) */
/*TODO*///			  4, /* 55: FP Unimplemented Data Type            (unemulated) */
/*TODO*///			  4, /* 56: MMU Configuration Error               (unemulated) */
/*TODO*///			  4, /* 57: MMU Illegal Operation Error           (unemulated) */
/*TODO*///			  4, /* 58: MMU Access Level Violation Error      (unemulated) */
/*TODO*///			  4, /* 59: RESERVED                                           */
/*TODO*///			  4, /* 60: RESERVED                                           */
/*TODO*///			  4, /* 61: RESERVED                                           */
/*TODO*///			  4, /* 62: RESERVED                                           */
/*TODO*///			  4, /* 63: RESERVED                                           */
/*TODO*///			     /* 64-255: User Defined                                   */
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4
/*TODO*///		},
/*TODO*///		{ /* 020 */
/*TODO*///			  4, /*  0: Reset - Initial Stack Pointer                      */
/*TODO*///			  4, /*  1: Reset - Initial Program Counter                    */
/*TODO*///			 50, /*  2: Bus Error                             (unemulated) */
/*TODO*///			 50, /*  3: Address Error                         (unemulated) */
/*TODO*///			 20, /*  4: Illegal Instruction                                */
/*TODO*///			 38, /*  5: Divide by Zero                                     */
/*TODO*///			 40, /*  6: CHK                                                */
/*TODO*///			 20, /*  7: TRAPV                                              */
/*TODO*///			 34, /*  8: Privilege Violation                                */
/*TODO*///			 25, /*  9: Trace                                              */
/*TODO*///			 20, /* 10: 1010                                               */
/*TODO*///			 20, /* 11: 1111                                               */
/*TODO*///			  4, /* 12: RESERVED                                           */
/*TODO*///			  4, /* 13: Coprocessor Protocol Violation        (unemulated) */
/*TODO*///			  4, /* 14: Format Error                                       */
/*TODO*///			 30, /* 15: Uninitialized Interrupt                            */
/*TODO*///			  4, /* 16: RESERVED                                           */
/*TODO*///			  4, /* 17: RESERVED                                           */
/*TODO*///			  4, /* 18: RESERVED                                           */
/*TODO*///			  4, /* 19: RESERVED                                           */
/*TODO*///			  4, /* 20: RESERVED                                           */
/*TODO*///			  4, /* 21: RESERVED                                           */
/*TODO*///			  4, /* 22: RESERVED                                           */
/*TODO*///			  4, /* 23: RESERVED                                           */
/*TODO*///			 30, /* 24: Spurious Interrupt                                 */
/*TODO*///			 30, /* 25: Level 1 Interrupt Autovector                       */
/*TODO*///			 30, /* 26: Level 2 Interrupt Autovector                       */
/*TODO*///			 30, /* 27: Level 3 Interrupt Autovector                       */
/*TODO*///			 30, /* 28: Level 4 Interrupt Autovector                       */
/*TODO*///			 30, /* 29: Level 5 Interrupt Autovector                       */
/*TODO*///			 30, /* 30: Level 6 Interrupt Autovector                       */
/*TODO*///			 30, /* 31: Level 7 Interrupt Autovector                       */
/*TODO*///			 20, /* 32: TRAP #0                                            */
/*TODO*///			 20, /* 33: TRAP #1                                            */
/*TODO*///			 20, /* 34: TRAP #2                                            */
/*TODO*///			 20, /* 35: TRAP #3                                            */
/*TODO*///			 20, /* 36: TRAP #4                                            */
/*TODO*///			 20, /* 37: TRAP #5                                            */
/*TODO*///			 20, /* 38: TRAP #6                                            */
/*TODO*///			 20, /* 39: TRAP #7                                            */
/*TODO*///			 20, /* 40: TRAP #8                                            */
/*TODO*///			 20, /* 41: TRAP #9                                            */
/*TODO*///			 20, /* 42: TRAP #10                                           */
/*TODO*///			 20, /* 43: TRAP #11                                           */
/*TODO*///			 20, /* 44: TRAP #12                                           */
/*TODO*///			 20, /* 45: TRAP #13                                           */
/*TODO*///			 20, /* 46: TRAP #14                                           */
/*TODO*///			 20, /* 47: TRAP #15                                           */
/*TODO*///			  4, /* 48: FP Branch or Set on Unknown Condition (unemulated) */
/*TODO*///			  4, /* 49: FP Inexact Result                     (unemulated) */
/*TODO*///			  4, /* 50: FP Divide by Zero                     (unemulated) */
/*TODO*///			  4, /* 51: FP Underflow                          (unemulated) */
/*TODO*///			  4, /* 52: FP Operand Error                      (unemulated) */
/*TODO*///			  4, /* 53: FP Overflow                           (unemulated) */
/*TODO*///			  4, /* 54: FP Signaling NAN                      (unemulated) */
/*TODO*///			  4, /* 55: FP Unimplemented Data Type            (unemulated) */
/*TODO*///			  4, /* 56: MMU Configuration Error               (unemulated) */
/*TODO*///			  4, /* 57: MMU Illegal Operation Error           (unemulated) */
/*TODO*///			  4, /* 58: MMU Access Level Violation Error      (unemulated) */
/*TODO*///			  4, /* 59: RESERVED                                           */
/*TODO*///			  4, /* 60: RESERVED                                           */
/*TODO*///			  4, /* 61: RESERVED                                           */
/*TODO*///			  4, /* 62: RESERVED                                           */
/*TODO*///			  4, /* 63: RESERVED                                           */
/*TODO*///			     /* 64-255: User Defined                                   */
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
/*TODO*///			  4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4
/*TODO*///		}
/*TODO*///	};
/*TODO*///	
/*TODO*///	uint8 m68ki_ea_idx_cycle_table[64] =
/*TODO*///	{
/*TODO*///		 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
/*TODO*///		 0, /* ..01.000 no memory indirect, base NULL             */
/*TODO*///		 5, /* ..01..01 memory indirect,    base NULL, outer NULL */
/*TODO*///		 7, /* ..01..10 memory indirect,    base NULL, outer 16   */
/*TODO*///		 7, /* ..01..11 memory indirect,    base NULL, outer 32   */
/*TODO*///		 0,  5,  7,  7,  0,  5,  7,  7,  0,  5,  7,  7,
/*TODO*///		 2, /* ..10.000 no memory indirect, base 16               */
/*TODO*///		 7, /* ..10..01 memory indirect,    base 16,   outer NULL */
/*TODO*///		 9, /* ..10..10 memory indirect,    base 16,   outer 16   */
/*TODO*///		 9, /* ..10..11 memory indirect,    base 16,   outer 32   */
/*TODO*///		 0,  7,  9,  9,  0,  7,  9,  9,  0,  7,  9,  9,
/*TODO*///		 6, /* ..11.000 no memory indirect, base 32               */
/*TODO*///		11, /* ..11..01 memory indirect,    base 32,   outer NULL */
/*TODO*///		13, /* ..11..10 memory indirect,    base 32,   outer 16   */
/*TODO*///		13, /* ..11..11 memory indirect,    base 32,   outer 32   */
/*TODO*///		 0, 11, 13, 13,  0, 11, 13, 13,  0, 11, 13, 13
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ======================================================================== */
/*TODO*///	/* =============================== CALLBACKS ============================== */
/*TODO*///	/* ======================================================================== */
/*TODO*///	
/*TODO*///	/* Default callbacks used if the callback hasn't been set yet, or if the
/*TODO*///	 * callback is set to NULL
/*TODO*///	 */
/*TODO*///	
/*TODO*///	/* Interrupt acknowledge */
/*TODO*///	static int default_int_ack_callback_data;
/*TODO*///	static int default_int_ack_callback(int int_level)
/*TODO*///	{
/*TODO*///		default_int_ack_callback_data = int_level;
/*TODO*///		CPU_INT_LEVEL = 0;
/*TODO*///		return M68K_INT_ACK_AUTOVECTOR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Breakpoint acknowledge */
/*TODO*///	static unsigned int default_bkpt_ack_callback_data;
/*TODO*///	static void default_bkpt_ack_callback(unsigned int data)
/*TODO*///	{
/*TODO*///		default_bkpt_ack_callback_data = data;
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Called when a reset instruction is executed */
/*TODO*///	static void default_reset_instr_callback(void)
/*TODO*///	{
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Called when the program counter changed by a large value */
/*TODO*///	static unsigned int default_pc_changed_callback_data;
/*TODO*///	static void default_pc_changed_callback(unsigned int new_pc)
/*TODO*///	{
/*TODO*///		default_pc_changed_callback_data = new_pc;
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Called every time there's bus activity (read/write to/from memory */
/*TODO*///	static unsigned int default_set_fc_callback_data;
/*TODO*///	static void default_set_fc_callback(unsigned int new_fc)
/*TODO*///	{
/*TODO*///		default_set_fc_callback_data = new_fc;
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Called every instruction cycle prior to execution */
/*TODO*///	static void default_instr_hook_callback(void)
/*TODO*///	{
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ======================================================================== */
/*TODO*///	/* ================================= API ================================== */
/*TODO*///	/* ======================================================================== */
/*TODO*///	
/*TODO*///	/* Access the internals of the CPU */
/*TODO*///	unsigned int m68k_get_reg(void* context, m68k_register_t regnum)
/*TODO*///	{
/*TODO*///		m68ki_cpu_core* cpu = context != NULL ?(m68ki_cpu_core*)context : &m68ki_cpu;
/*TODO*///	
/*TODO*///		switch(regnum)
/*TODO*///		{
/*TODO*///			case M68K_REG_D0:	return cpu.dar[0];
/*TODO*///			case M68K_REG_D1:	return cpu.dar[1];
/*TODO*///			case M68K_REG_D2:	return cpu.dar[2];
/*TODO*///			case M68K_REG_D3:	return cpu.dar[3];
/*TODO*///			case M68K_REG_D4:	return cpu.dar[4];
/*TODO*///			case M68K_REG_D5:	return cpu.dar[5];
/*TODO*///			case M68K_REG_D6:	return cpu.dar[6];
/*TODO*///			case M68K_REG_D7:	return cpu.dar[7];
/*TODO*///			case M68K_REG_A0:	return cpu.dar[8];
/*TODO*///			case M68K_REG_A1:	return cpu.dar[9];
/*TODO*///			case M68K_REG_A2:	return cpu.dar[10];
/*TODO*///			case M68K_REG_A3:	return cpu.dar[11];
/*TODO*///			case M68K_REG_A4:	return cpu.dar[12];
/*TODO*///			case M68K_REG_A5:	return cpu.dar[13];
/*TODO*///			case M68K_REG_A6:	return cpu.dar[14];
/*TODO*///			case M68K_REG_A7:	return cpu.dar[15];
/*TODO*///			case M68K_REG_PC:	return MASK_OUT_ABOVE_32(cpu.pc);
/*TODO*///			case M68K_REG_SR:	return	cpu.t1_flag						|
/*TODO*///										cpu.t0_flag						|
/*TODO*///										(cpu.s_flag << 11)					|
/*TODO*///										(cpu.m_flag << 11)					|
/*TODO*///										cpu.int_mask						|
/*TODO*///										((cpu.x_flag & XFLAG_SET) >> 4)	|
/*TODO*///										((cpu.n_flag & NFLAG_SET) >> 4)	|
/*TODO*///										((!cpu.not_z_flag) << 2)			|
/*TODO*///										((cpu.v_flag & VFLAG_SET) >> 6)	|
/*TODO*///										((cpu.c_flag & CFLAG_SET) >> 8);
/*TODO*///			case M68K_REG_SP:	return cpu.dar[15];
/*TODO*///			case M68K_REG_USP:	return cpu.s_flag ? cpu.sp[0] : cpu.dar[15];
/*TODO*///			case M68K_REG_ISP:	return cpu.s_flag && !cpu.m_flag ? cpu.dar[15] : cpu.sp[4];
/*TODO*///			case M68K_REG_MSP:	return cpu.s_flag && cpu.m_flag ? cpu.dar[15] : cpu.sp[6];
/*TODO*///			case M68K_REG_SFC:	return cpu.sfc;
/*TODO*///			case M68K_REG_DFC:	return cpu.dfc;
/*TODO*///			case M68K_REG_VBR:	return cpu.vbr;
/*TODO*///			case M68K_REG_CACR:	return cpu.cacr;
/*TODO*///			case M68K_REG_CAAR:	return cpu.caar;
/*TODO*///			case M68K_REG_PREF_ADDR:	return cpu.pref_addr;
/*TODO*///			case M68K_REG_PREF_DATA:	return cpu.pref_data;
/*TODO*///			case M68K_REG_PPC:	return MASK_OUT_ABOVE_32(cpu.ppc);
/*TODO*///			case M68K_REG_IR:	return cpu.ir;
/*TODO*///			case M68K_REG_CPU_TYPE:
/*TODO*///				switch(cpu.cpu_type)
/*TODO*///				{
/*TODO*///					case CPU_TYPE_000:		return (unsigned int)M68K_CPU_TYPE_68000;
/*TODO*///					case CPU_TYPE_010:		return (unsigned int)M68K_CPU_TYPE_68010;
/*TODO*///					case CPU_TYPE_EC020:	return (unsigned int)M68K_CPU_TYPE_68EC020;
/*TODO*///					case CPU_TYPE_020:		return (unsigned int)M68K_CPU_TYPE_68020;
/*TODO*///				}
/*TODO*///				return M68K_CPU_TYPE_INVALID;
/*TODO*///			default:			return 0;
/*TODO*///		}
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68k_set_reg(m68k_register_t regnum, unsigned int value)
/*TODO*///	{
/*TODO*///		switch(regnum)
/*TODO*///		{
/*TODO*///			case M68K_REG_D0:	REG_D[0] = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_D1:	REG_D[1] = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_D2:	REG_D[2] = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_D3:	REG_D[3] = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_D4:	REG_D[4] = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_D5:	REG_D[5] = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_D6:	REG_D[6] = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_D7:	REG_D[7] = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_A0:	REG_A[0] = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_A1:	REG_A[1] = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_A2:	REG_A[2] = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_A3:	REG_A[3] = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_A4:	REG_A[4] = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_A5:	REG_A[5] = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_A6:	REG_A[6] = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_A7:	REG_A[7] = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_PC:	m68ki_jump(MASK_OUT_ABOVE_32(value)); return;
/*TODO*///			case M68K_REG_SR:	m68ki_set_sr(value); return;
/*TODO*///			case M68K_REG_SP:	REG_SP = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_USP:	if (FLAG_S != 0)
/*TODO*///									REG_USP = MASK_OUT_ABOVE_32(value);
/*TODO*///								else
/*TODO*///									REG_SP = MASK_OUT_ABOVE_32(value);
/*TODO*///								return;
/*TODO*///			case M68K_REG_ISP:	if(FLAG_S && !FLAG_M)
/*TODO*///									REG_SP = MASK_OUT_ABOVE_32(value);
/*TODO*///								else
/*TODO*///									REG_ISP = MASK_OUT_ABOVE_32(value);
/*TODO*///								return;
/*TODO*///			case M68K_REG_MSP:	if(FLAG_S && FLAG_M)
/*TODO*///									REG_SP = MASK_OUT_ABOVE_32(value);
/*TODO*///								else
/*TODO*///									REG_MSP = MASK_OUT_ABOVE_32(value);
/*TODO*///								return;
/*TODO*///			case M68K_REG_VBR:	REG_VBR = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_SFC:	REG_SFC = value & 7; return;
/*TODO*///			case M68K_REG_DFC:	REG_DFC = value & 7; return;
/*TODO*///			case M68K_REG_CACR:	REG_CACR = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_CAAR:	REG_CAAR = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_PPC:	REG_PPC = MASK_OUT_ABOVE_32(value); return;
/*TODO*///			case M68K_REG_IR:	REG_IR = MASK_OUT_ABOVE_16(value); return;
/*TODO*///			case M68K_REG_CPU_TYPE: m68k_set_cpu_type(value); return;
/*TODO*///			default:			return;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Set the callbacks */
/*TODO*///	void m68k_set_int_ack_callback(int  (*callback)(int int_level))
/*TODO*///	{
/*TODO*///		CALLBACK_INT_ACK = callback ? callback : default_int_ack_callback;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68k_set_bkpt_ack_callback(void  (*callback)(unsigned int data))
/*TODO*///	{
/*TODO*///		CALLBACK_BKPT_ACK = callback ? callback : default_bkpt_ack_callback;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68k_set_reset_instr_callback(void  (*callback)(void))
/*TODO*///	{
/*TODO*///		CALLBACK_RESET_INSTR = callback ? callback : default_reset_instr_callback;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68k_set_pc_changed_callback(void  (*callback)(unsigned int new_pc))
/*TODO*///	{
/*TODO*///		CALLBACK_PC_CHANGED = callback ? callback : default_pc_changed_callback;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68k_set_fc_callback(void  (*callback)(unsigned int new_fc))
/*TODO*///	{
/*TODO*///		CALLBACK_SET_FC = callback ? callback : default_set_fc_callback;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68k_set_instr_hook_callback(void  (*callback)(void))
/*TODO*///	{
/*TODO*///		CALLBACK_INSTR_HOOK = callback ? callback : default_instr_hook_callback;
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Set the CPU type. */
/*TODO*///	void m68k_set_cpu_type(unsigned int cpu_type)
/*TODO*///	{
/*TODO*///		switch(cpu_type)
/*TODO*///		{
/*TODO*///			case M68K_CPU_TYPE_68000:
/*TODO*///				CPU_TYPE         = CPU_TYPE_000;
/*TODO*///				CPU_ADDRESS_MASK = 0x00ffffff;
/*TODO*///				CPU_SR_MASK      = 0xa71f; /* T1 -- S  -- -- I2 I1 I0 -- -- -- X  N  Z  V  C  */
/*TODO*///				CYC_INSTRUCTION  = m68ki_cycles[0];
/*TODO*///				CYC_EXCEPTION    = m68ki_exception_cycle_table[0];
/*TODO*///				CYC_BCC_NOTAKE_B = -2;
/*TODO*///				CYC_BCC_NOTAKE_W = 2;
/*TODO*///				CYC_DBCC_F_NOEXP = -2;
/*TODO*///				CYC_DBCC_F_EXP   = 2;
/*TODO*///				CYC_SCC_R_FALSE  = 2;
/*TODO*///				CYC_MOVEM_W      = 2;
/*TODO*///				CYC_MOVEM_L      = 3;
/*TODO*///				CYC_SHIFT        = 1;
/*TODO*///				CYC_RESET        = 132;
/*TODO*///				return;
/*TODO*///			case M68K_CPU_TYPE_68010:
/*TODO*///				CPU_TYPE         = CPU_TYPE_010;
/*TODO*///				CPU_ADDRESS_MASK = 0x00ffffff;
/*TODO*///				CPU_SR_MASK      = 0xa71f; /* T1 -- S  -- -- I2 I1 I0 -- -- -- X  N  Z  V  C  */
/*TODO*///				CYC_INSTRUCTION  = m68ki_cycles[1];
/*TODO*///				CYC_EXCEPTION    = m68ki_exception_cycle_table[1];
/*TODO*///				CYC_BCC_NOTAKE_B = -4;
/*TODO*///				CYC_BCC_NOTAKE_W = 0;
/*TODO*///				CYC_DBCC_F_NOEXP = 0;
/*TODO*///				CYC_DBCC_F_EXP   = 6;
/*TODO*///				CYC_SCC_R_FALSE  = 0;
/*TODO*///				CYC_MOVEM_W      = 2;
/*TODO*///				CYC_MOVEM_L      = 3;
/*TODO*///				CYC_SHIFT        = 1;
/*TODO*///				CYC_RESET        = 130;
/*TODO*///				return;
/*TODO*///			case M68K_CPU_TYPE_68EC020:
/*TODO*///				CPU_TYPE         = CPU_TYPE_EC020;
/*TODO*///				CPU_ADDRESS_MASK = 0x00ffffff;
/*TODO*///				CPU_SR_MASK      = 0xf71f; /* T1 T0 S  M  -- I2 I1 I0 -- -- -- X  N  Z  V  C  */
/*TODO*///				CYC_INSTRUCTION  = m68ki_cycles[2];
/*TODO*///				CYC_EXCEPTION    = m68ki_exception_cycle_table[2];
/*TODO*///				CYC_BCC_NOTAKE_B = -2;
/*TODO*///				CYC_BCC_NOTAKE_W = 0;
/*TODO*///				CYC_DBCC_F_NOEXP = 0;
/*TODO*///				CYC_DBCC_F_EXP   = 4;
/*TODO*///				CYC_SCC_R_FALSE  = 0;
/*TODO*///				CYC_MOVEM_W      = 2;
/*TODO*///				CYC_MOVEM_L      = 2;
/*TODO*///				CYC_SHIFT        = 0;
/*TODO*///				CYC_RESET        = 518;
/*TODO*///				return;
/*TODO*///			case M68K_CPU_TYPE_68020:
/*TODO*///				CPU_TYPE         = CPU_TYPE_020;
/*TODO*///				CPU_ADDRESS_MASK = 0xffffffff;
/*TODO*///				CPU_SR_MASK      = 0xf71f; /* T1 T0 S  M  -- I2 I1 I0 -- -- -- X  N  Z  V  C  */
/*TODO*///				CYC_INSTRUCTION  = m68ki_cycles[2];
/*TODO*///				CYC_EXCEPTION    = m68ki_exception_cycle_table[2];
/*TODO*///				CYC_BCC_NOTAKE_B = -2;
/*TODO*///				CYC_BCC_NOTAKE_W = 0;
/*TODO*///				CYC_DBCC_F_NOEXP = 0;
/*TODO*///				CYC_DBCC_F_EXP   = 4;
/*TODO*///				CYC_SCC_R_FALSE  = 0;
/*TODO*///				CYC_MOVEM_W      = 2;
/*TODO*///				CYC_MOVEM_L      = 2;
/*TODO*///				CYC_SHIFT        = 0;
/*TODO*///				CYC_RESET        = 518;
/*TODO*///				return;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Execute some instructions until we use up num_cycles clock cycles */
/*TODO*///	/* ASG: removed per-instruction interrupt checks */
/*TODO*///	int m68k_execute(int num_cycles)
/*TODO*///	{
/*TODO*///		/* Make sure we're not stopped */
/*TODO*///		if(!CPU_STOPPED)
/*TODO*///		{
/*TODO*///			/* Set our pool of clock cycles available */
/*TODO*///			SET_CYCLES(num_cycles);
/*TODO*///			m68ki_initial_cycles = num_cycles;
/*TODO*///	
/*TODO*///			/* ASG: update cycles */
/*TODO*///			USE_CYCLES(CPU_INT_CYCLES);
/*TODO*///			CPU_INT_CYCLES = 0;
/*TODO*///	
/*TODO*///			/* Return point if we had an address error */
/*TODO*///			m68ki_set_address_error_trap(); /* auto-disable (see m68kcpu.h) */
/*TODO*///	
/*TODO*///			/* Main loop.  Keep going until we run out of clock cycles */
/*TODO*///			do
/*TODO*///			{
/*TODO*///				/* Set tracing accodring to T1. (T0 is done inside instruction) */
/*TODO*///				m68ki_trace_t1(); /* auto-disable (see m68kcpu.h) */
/*TODO*///	
/*TODO*///				/* Set the address space for reads */
/*TODO*///				m68ki_use_data_space(); /* auto-disable (see m68kcpu.h) */
/*TODO*///	
/*TODO*///				/* Call external hook to peek at CPU */
/*TODO*///				m68ki_instr_hook(); /* auto-disable (see m68kcpu.h) */
/*TODO*///	
/*TODO*///				/* Record previous program counter */
/*TODO*///				REG_PPC = REG_PC;
/*TODO*///	
/*TODO*///				/* Read an instruction and call its handler */
/*TODO*///				REG_IR = m68ki_read_imm_16();
/*TODO*///				m68ki_instruction_jump_table[REG_IR]();
/*TODO*///				USE_CYCLES(CYC_INSTRUCTION[REG_IR]);
/*TODO*///	
/*TODO*///				/* Trace m68k_exception, if necessary */
/*TODO*///				m68ki_exception_if_trace(); /* auto-disable (see m68kcpu.h) */
/*TODO*///			} while(GET_CYCLES() > 0);
/*TODO*///	
/*TODO*///			/* set previous PC to current PC for the next entry into the loop */
/*TODO*///			REG_PPC = REG_PC;
/*TODO*///	
/*TODO*///			/* ASG: update cycles */
/*TODO*///			USE_CYCLES(CPU_INT_CYCLES);
/*TODO*///			CPU_INT_CYCLES = 0;
/*TODO*///	
/*TODO*///			/* return how many clocks we used */
/*TODO*///			return m68ki_initial_cycles - GET_CYCLES();
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* We get here if the CPU is stopped or halted */
/*TODO*///		SET_CYCLES(0);
/*TODO*///		CPU_INT_CYCLES = 0;
/*TODO*///	
/*TODO*///		return num_cycles;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	int m68k_cycles_run(void)
/*TODO*///	{
/*TODO*///		return m68ki_initial_cycles - GET_CYCLES();
/*TODO*///	}
/*TODO*///	
/*TODO*///	int m68k_cycles_remaining(void)
/*TODO*///	{
/*TODO*///		return GET_CYCLES();
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Change the timeslice */
/*TODO*///	void m68k_modify_timeslice(int cycles)
/*TODO*///	{
/*TODO*///		m68ki_initial_cycles += cycles;
/*TODO*///		ADD_CYCLES(cycles);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	void m68k_end_timeslice(void)
/*TODO*///	{
/*TODO*///		m68ki_initial_cycles = GET_CYCLES();
/*TODO*///		SET_CYCLES(0);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ASG: rewrote so that the int_level is a mask of the IPL0/IPL1/IPL2 bits */
/*TODO*///	/* KS: Modified so that IPL* bits match with mask positions in the SR
/*TODO*///	 *     and cleaned out remenants of the interrupt controller.
/*TODO*///	 */
/*TODO*///	void m68k_set_irq(unsigned int int_level)
/*TODO*///	{
/*TODO*///		uint old_level = CPU_INT_LEVEL;
/*TODO*///		CPU_INT_LEVEL = int_level << 8;
/*TODO*///	
/*TODO*///		/* A transition from < 7 to 7 always interrupts (NMI) */
/*TODO*///		/* Note: Level 7 can also level trigger like a normal IRQ */
/*TODO*///		if(old_level != 0x0700 && CPU_INT_LEVEL == 0x0700)
/*TODO*///			m68ki_exception_interrupt(7); /* Edge triggered level 7 (NMI) */
/*TODO*///		else
/*TODO*///			m68ki_check_interrupts(); /* Level triggered (IRQ) */
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static void m68k_init()
/*TODO*///	{
/*TODO*///		static uint emulation_initialized = 0;
/*TODO*///	
/*TODO*///		/* The first call to this function initializes the opcode handler jump table */
/*TODO*///		if(!emulation_initialized)
/*TODO*///			{
/*TODO*///			m68ki_build_opcode_table();
/*TODO*///			emulation_initialized = 1;
/*TODO*///		}
/*TODO*///	
/*TODO*///		m68k_set_int_ack_callback(NULL);
/*TODO*///		m68k_set_bkpt_ack_callback(NULL);
/*TODO*///		m68k_set_reset_instr_callback(NULL);
/*TODO*///		m68k_set_pc_changed_callback(NULL);
/*TODO*///		m68k_set_fc_callback(NULL);
/*TODO*///		m68k_set_instr_hook_callback(NULL);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Pulse the RESET line on the CPU */
/*TODO*///	void m68k_pulse_reset(void)
/*TODO*///	{
/*TODO*///		/* Clear all stop levels and eat up all remaining cycles */
/*TODO*///		CPU_STOPPED = 0;
/*TODO*///		SET_CYCLES(0);
/*TODO*///	
/*TODO*///		/* Turn off tracing */
/*TODO*///		FLAG_T1 = FLAG_T0 = 0;
/*TODO*///		m68ki_clear_trace();
/*TODO*///		/* Interrupt mask to level 7 */
/*TODO*///		FLAG_INT_MASK = 0x0700;
/*TODO*///		/* Reset VBR */
/*TODO*///		REG_VBR = 0;
/*TODO*///		/* Go to supervisor mode */
/*TODO*///		m68ki_set_sm_flag(SFLAG_SET | MFLAG_CLEAR);
/*TODO*///	
/*TODO*///		/* Invalidate the prefetch queue */
/*TODO*///	#if M68K_EMULATE_PREFETCH
/*TODO*///		/* Set to arbitrary number since our first fetch is from 0 */
/*TODO*///		CPU_PREF_ADDR = 0x1000;
/*TODO*///	#endif /* M68K_EMULATE_PREFETCH */
/*TODO*///	
/*TODO*///		/* Read the initial stack pointer and program counter */
/*TODO*///		m68ki_jump(0);
/*TODO*///		REG_SP = m68ki_read_imm_32();
/*TODO*///		REG_PC = m68ki_read_imm_32();
/*TODO*///		m68ki_jump(REG_PC);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* Pulse the HALT line on the CPU */
/*TODO*///	void m68k_pulse_halt(void)
/*TODO*///	{
/*TODO*///		CPU_STOPPED |= STOP_LEVEL_HALT;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Get and set the current CPU context */
/*TODO*///	/* This is to allow for multiple CPUs */
/*TODO*///	unsigned int m68k_context_size()
/*TODO*///	{
/*TODO*///		return sizeof(m68ki_cpu_core);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned int m68k_get_context(void* dst)
/*TODO*///	{
/*TODO*///		if (dst != 0) *(m68ki_cpu_core*)dst = m68ki_cpu;
/*TODO*///		return sizeof(m68ki_cpu_core);
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68k_set_context(void* src)
/*TODO*///	{
/*TODO*///		if (src != 0) m68ki_cpu = *(m68ki_cpu_core*)src;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static struct {
/*TODO*///		UINT16 sr;
/*TODO*///		int stopped;
/*TODO*///		int halted;
/*TODO*///	} m68k_substate;
/*TODO*///	
/*TODO*///	static void m68k_prepare_substate(void)
/*TODO*///	{
/*TODO*///		m68k_substate.sr = m68ki_get_sr();
/*TODO*///		m68k_substate.stopped = (CPU_STOPPED & STOP_LEVEL_STOP) != 0;
/*TODO*///		m68k_substate.halted  = (CPU_STOPPED & STOP_LEVEL_HALT) != 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void m68k_post_load(void)
/*TODO*///	{
/*TODO*///		m68ki_set_sr_noint_nosp(m68k_substate.sr);
/*TODO*///		CPU_STOPPED = m68k_substate.stopped ? STOP_LEVEL_STOP : 0
/*TODO*///			        | m68k_substate.halted  ? STOP_LEVEL_HALT : 0;
/*TODO*///		m68ki_jump(REG_PC);
/*TODO*///	}
/*TODO*///	
/*TODO*///	void m68k_state_register(const char *type)
/*TODO*///	{
/*TODO*///		int cpu = cpu_getactivecpu();
/*TODO*///	
/*TODO*///		state_save_register_UINT32(type, cpu, "D"         , REG_D, 8);
/*TODO*///		state_save_register_UINT32(type, cpu, "A"         , REG_A, 8);
/*TODO*///		state_save_register_UINT32(type, cpu, "PPC"       , &REG_PPC, 1);
/*TODO*///		state_save_register_UINT32(type, cpu, "PC"        , &REG_PC, 1);
/*TODO*///		state_save_register_UINT32(type, cpu, "USP"       , &REG_USP, 1);
/*TODO*///		state_save_register_UINT32(type, cpu, "ISP"       , &REG_ISP, 1);
/*TODO*///		state_save_register_UINT32(type, cpu, "MSP"       , &REG_MSP, 1);
/*TODO*///		state_save_register_UINT32(type, cpu, "VBR"       , &REG_VBR, 1);
/*TODO*///		state_save_register_UINT32(type, cpu, "SFC"       , &REG_SFC, 1);
/*TODO*///		state_save_register_UINT32(type, cpu, "DFC"       , &REG_DFC, 1);
/*TODO*///		state_save_register_UINT32(type, cpu, "CACR"      , &REG_CACR, 1);
/*TODO*///		state_save_register_UINT32(type, cpu, "CAAR"      , &REG_CAAR, 1);
/*TODO*///		state_save_register_UINT16(type, cpu, "SR"        , &m68k_substate.sr, 1);
/*TODO*///		state_save_register_UINT32(type, cpu, "INT_LEVEL" , &CPU_INT_LEVEL, 1);
/*TODO*///		state_save_register_UINT32(type, cpu, "INT_CYCLES", &CPU_INT_CYCLES, 1);
/*TODO*///		state_save_register_int   (type, cpu, "STOPPED"   , &m68k_substate.stopped);
/*TODO*///		state_save_register_int   (type, cpu, "HALTED"    , &m68k_substate.halted);
/*TODO*///		state_save_register_UINT32(type, cpu, "PREF_ADDR" , &CPU_PREF_ADDR, 1);
/*TODO*///		state_save_register_UINT32(type, cpu, "PREF_DATA" , &CPU_PREF_DATA, 1);
/*TODO*///		state_save_register_func_presave(m68k_prepare_substate);
/*TODO*///		state_save_register_func_postload(m68k_post_load);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ======================================================================== */
/*TODO*///	/* ============================== END OF FILE ============================= */
/*TODO*///	/* ======================================================================== */
}
