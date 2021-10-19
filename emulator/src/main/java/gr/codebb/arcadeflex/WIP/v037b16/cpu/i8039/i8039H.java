/**************************************************************************
 *                      Intel 8039 Portable Emulator                      *
 *                                                                        *
 *                   Copyright (C) 1997 by Mirko Buffoni                  *
 *  Based on the original work (C) 1997 by Dan Boris, an 8048 emulator    *
 *     You are not allowed to distribute this software commercially       *
 *        Please, notify me, if you make any changes to this file         *
 **************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.cpu.i8039;

public class i8039H
{
	
/*TODO*///	enum { I8039_PC=1, I8039_SP, I8039_PSW, I8039_A,  I8039_IRQ_STATE,
/*TODO*///		   I8039_R0,   I8039_R1, I8039_R2,  I8039_R3, I8039_R4,
/*TODO*///		   I8039_R5,   I8039_R6, I8039_R7,  I8039_P1, I8039_P2
/*TODO*///	};
/*TODO*///	
/*TODO*///	extern int i8039_ICount;        /* T-state count                          */
/*TODO*///	
/*TODO*///	/* HJB 01/05/99 changed to positive values to use pending_irq as a flag */
/*TODO*///	#define I8039_IGNORE_INT    0   /* Ignore interrupt                     */
/*TODO*///	#define I8039_EXT_INT		1	/* Execute a normal extern interrupt	*/
/*TODO*///	#define I8039_TIMER_INT 	2	/* Execute a Timer interrupt			*/
/*TODO*///	#define I8039_COUNT_INT 	4	/* Execute a Counter interrupt			*/
/*TODO*///	
/*TODO*///	extern extern void i8039_reset(void *param);			/* Reset processor & registers	*/
/*TODO*///	extern extern int i8039_execute(int cycles);			/* Execute cycles T-States - returns number of cycles actually run */
/*TODO*///	extern unsigned i8039_get_context(void *dst);	/* Get registers				*/
/*TODO*///	extern void i8039_set_context(void *src);		/* Set registers				*/
/*TODO*///	extern unsigned i8039_get_pc(void); 			/* Get program counter			*/
/*TODO*///	extern void i8039_set_pc(unsigned val); 		/* Set program counter			*/
/*TODO*///	extern unsigned i8039_get_sp(void); 			/* Get stack pointer			*/
/*TODO*///	extern void i8039_set_sp(unsigned val); 		/* Set stack pointer			*/
/*TODO*///	extern unsigned i8039_get_reg(int regnum);		/* Get specific register	  */
/*TODO*///	extern void i8039_set_reg(int regnum, unsigned val);    /* Set specific register 	 */
/*TODO*///	extern void i8039_set_nmi_line(int state);
/*TODO*///	extern void i8039_set_irq_line(int irqline, int state);
/*TODO*///	extern void i8039_set_irq_callback(int (*callback)(int irqline));
/*TODO*///	extern const char *i8039_info(void *context, int regnum);
/*TODO*///	extern unsigned i8039_dasm(char *buffer, unsigned pc);
	
    /*   This handling of special I/O ports should be better for actual MAME
     *   architecture.  (i.e., define access to ports { I8039_p1, I8039_p1, dkong_out_w })
     */

    public static final int  I8039_p0	= 0x100;   /* Not used */
    public static final int  I8039_p1	= 0x101;
    public static final int  I8039_p2	= 0x102;
    public static final int  I8039_p4	= 0x104;
    public static final int  I8039_p5	= 0x105;
    public static final int  I8039_p6	= 0x106;
    public static final int  I8039_p7	= 0x107;
    public static final int  I8039_t0	= 0x110;
    public static final int  I8039_t1	= 0x111;
    public static final int  I8039_bus	= 0x120;

/*TODO*///	/**************************************************************************
/*TODO*///	 * I8035 section
/*TODO*///	 **************************************************************************/
/*TODO*///	#if (HAS_I8035)
/*TODO*///	#define I8035_PC				I8039_PC
/*TODO*///	#define I8035_SP				I8039_SP
/*TODO*///	#define I8035_PSW				I8039_PSW
/*TODO*///	#define I8035_A 				I8039_A
/*TODO*///	#define I8035_IRQ_STATE 		I8039_IRQ_STATE
/*TODO*///	#define I8035_R0				I8039_R0
/*TODO*///	#define I8035_R1				I8039_R1
/*TODO*///	#define I8035_R2				I8039_R2
/*TODO*///	#define I8035_R3				I8039_R3
/*TODO*///	#define I8035_R4				I8039_R4
/*TODO*///	#define I8035_R5				I8039_R5
/*TODO*///	#define I8035_R6				I8039_R6
/*TODO*///	#define I8035_R7				I8039_R7
/*TODO*///	#define I8035_P1				I8039_P1
/*TODO*///	#define I8035_P2				I8039_P2
/*TODO*///	
/*TODO*///	#define I8035_IGNORE_INT        I8039_IGNORE_INT
/*TODO*///	#define I8035_EXT_INT           I8039_EXT_INT
/*TODO*///	#define I8035_TIMER_INT         I8039_TIMER_INT
/*TODO*///	#define I8035_COUNT_INT         I8039_COUNT_INT
/*TODO*///	#define I8035_IRQ_STATE 		I8039_IRQ_STATE
/*TODO*///	
/*TODO*///	#define i8035_ICount            i8039_ICount
/*TODO*///	
/*TODO*///	extern extern void i8035_reset(void *param);
/*TODO*///	extern extern int i8035_execute(int cycles);
/*TODO*///	extern unsigned i8035_get_context(void *dst);
/*TODO*///	extern void i8035_set_context(void *src);
/*TODO*///	extern unsigned i8035_get_pc(void);
/*TODO*///	extern void i8035_set_pc(unsigned val);
/*TODO*///	extern unsigned i8035_get_sp(void);
/*TODO*///	extern void i8035_set_sp(unsigned val);
/*TODO*///	extern unsigned i8035_get_reg(int regnum);
/*TODO*///	extern void i8035_set_reg(int regnum, unsigned val);
/*TODO*///	extern void i8035_set_nmi_line(int state);
/*TODO*///	extern void i8035_set_irq_line(int irqline, int state);
/*TODO*///	extern void i8035_set_irq_callback(int (*callback)(int irqline));
/*TODO*///	extern const char *i8035_info(void *context, int regnum);
/*TODO*///	extern unsigned i8035_dasm(char *buffer, unsigned pc);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	/**************************************************************************
/*TODO*///	 * I8048 section
/*TODO*///	 **************************************************************************/
/*TODO*///	#if (HAS_I8048)
/*TODO*///	#define I8048_PC				I8039_PC
/*TODO*///	#define I8048_SP				I8039_SP
/*TODO*///	#define I8048_PSW				I8039_PSW
/*TODO*///	#define I8048_A 				I8039_A
/*TODO*///	#define I8048_IRQ_STATE 		I8039_IRQ_STATE
/*TODO*///	#define I8048_R0				I8039_R0
/*TODO*///	#define I8048_R1				I8039_R1
/*TODO*///	#define I8048_R2				I8039_R2
/*TODO*///	#define I8048_R3				I8039_R3
/*TODO*///	#define I8048_R4				I8039_R4
/*TODO*///	#define I8048_R5				I8039_R5
/*TODO*///	#define I8048_R6				I8039_R6
/*TODO*///	#define I8048_R7				I8039_R7
/*TODO*///	#define I8048_P1				I8039_P1
/*TODO*///	#define I8048_P2				I8039_P2
/*TODO*///	
/*TODO*///	#define I8048_IGNORE_INT        I8039_IGNORE_INT
/*TODO*///	#define I8048_EXT_INT           I8039_EXT_INT
/*TODO*///	#define I8048_TIMER_INT         I8039_TIMER_INT
/*TODO*///	#define I8048_COUNT_INT         I8039_COUNT_INT
/*TODO*///	
/*TODO*///	#define i8048_ICount            i8039_ICount
/*TODO*///	
/*TODO*///	extern extern void i8048_reset(void *param);
/*TODO*///	extern extern int i8048_execute(int cycles);
/*TODO*///	extern unsigned i8048_get_context(void *dst);
/*TODO*///	extern void i8048_set_context(void *src);
/*TODO*///	extern unsigned i8048_get_pc(void);
/*TODO*///	extern void i8048_set_pc(unsigned val);
/*TODO*///	extern unsigned i8048_get_sp(void);
/*TODO*///	extern void i8048_set_sp(unsigned val);
/*TODO*///	extern unsigned i8048_get_reg(int regnum);
/*TODO*///	extern void i8048_set_reg(int regnum, unsigned val);
/*TODO*///	extern void i8048_set_nmi_line(int state);
/*TODO*///	extern void i8048_set_irq_line(int irqline, int state);
/*TODO*///	extern void i8048_set_irq_callback(int (*callback)(int irqline));
/*TODO*///	const char *i8048_info(void *context, int regnum);
/*TODO*///	extern unsigned i8048_dasm(char *buffer, unsigned pc);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	/**************************************************************************
/*TODO*///	 * N7751 section
/*TODO*///	 **************************************************************************/
/*TODO*///	#if (HAS_N7751)
/*TODO*///	#define N7751_PC				I8039_PC
/*TODO*///	#define N7751_SP				I8039_SP
/*TODO*///	#define N7751_PSW				I8039_PSW
/*TODO*///	#define N7751_A 				I8039_A
/*TODO*///	#define N7751_IRQ_STATE 		I8039_IRQ_STATE
/*TODO*///	#define N7751_R0				I8039_R0
/*TODO*///	#define N7751_R1				I8039_R1
/*TODO*///	#define N7751_R2				I8039_R2
/*TODO*///	#define N7751_R3				I8039_R3
/*TODO*///	#define N7751_R4				I8039_R4
/*TODO*///	#define N7751_R5				I8039_R5
/*TODO*///	#define N7751_R6				I8039_R6
/*TODO*///	#define N7751_R7				I8039_R7
/*TODO*///	#define N7751_P1				I8039_P1
/*TODO*///	#define N7751_P2				I8039_P2
/*TODO*///	
/*TODO*///	#define N7751_IGNORE_INT        I8039_IGNORE_INT
/*TODO*///	#define N7751_EXT_INT           I8039_EXT_INT
/*TODO*///	#define N7751_TIMER_INT         I8039_TIMER_INT
/*TODO*///	#define N7751_COUNT_INT         I8039_COUNT_INT
/*TODO*///	
/*TODO*///	#define n7751_ICount            i8039_ICount
/*TODO*///	
/*TODO*///	extern extern void n7751_reset(void *param);
/*TODO*///	extern extern int n7751_execute(int cycles);
/*TODO*///	extern unsigned n7751_get_context(void *dst);
/*TODO*///	extern void n7751_set_context(void *src);
/*TODO*///	extern unsigned n7751_get_pc(void);
/*TODO*///	extern void n7751_set_pc(unsigned val);
/*TODO*///	extern unsigned n7751_get_sp(void);
/*TODO*///	extern void n7751_set_sp(unsigned val);
/*TODO*///	extern unsigned n7751_get_reg(int regnum);
/*TODO*///	extern void n7751_set_reg(int regnum, unsigned val);
/*TODO*///	extern void n7751_set_nmi_line(int state);
/*TODO*///	extern void n7751_set_irq_line(int irqline, int state);
/*TODO*///	extern void n7751_set_irq_callback(int (*callback)(int irqline));
/*TODO*///	extern const char *n7751_info(void *context, int regnum);
/*TODO*///	extern unsigned n7751_dasm(char *buffer, unsigned pc);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 *	 Input a UINT8 from given I/O port
/*TODO*///	 */
/*TODO*///	#define I8039_In(Port) ((UINT8)cpu_readport16(Port))
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 *	 Output a UINT8 to given I/O port
/*TODO*///	 */
/*TODO*///	#define I8039_Out(Port,Value) (cpu_writeport16(Port,Value))
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 *	 Read a UINT8 from given memory location
/*TODO*///	 */
/*TODO*///	#define I8039_RDMEM(A) ((unsigned)cpu_readmem16(A))
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 *	 Write a UINT8 to given memory location
/*TODO*///	 */
/*TODO*///	#define I8039_WRMEM(A,V) (cpu_writemem16(A,V))
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 *   I8039_RDOP() is identical to I8039_RDMEM() except it is used for reading
/*TODO*///	 *   opcodes. In case of system with memory mapped I/O, this function can be
/*TODO*///	 *   used to greatly speed up emulation
/*TODO*///	 */
/*TODO*///	#define I8039_RDOP(A) ((unsigned)cpu_readop(A))
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 *   I8039_RDOP_ARG() is identical to I8039_RDOP() except it is used for reading
/*TODO*///	 *   opcode arguments. This difference can be used to support systems that
/*TODO*///	 *   use different encoding mechanisms for opcodes and opcode arguments
/*TODO*///	 */
/*TODO*///	#define I8039_RDOP_ARG(A) ((unsigned)cpu_readop_arg(A))
/*TODO*///	
/*TODO*///	#ifdef  MAME_DEBUG
/*TODO*///	int 	Dasm8039(char *dst, unsigned pc);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#endif  /* _I8039_H */
}
