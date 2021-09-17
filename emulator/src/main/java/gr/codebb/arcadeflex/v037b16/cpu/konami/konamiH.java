/*** konami: Portable Konami cpu emulator ******************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b16.cpu.konami;

public class konamiH
{
	
/*TODO*///	enum {
/*TODO*///		KONAMI_PC=1, KONAMI_S, KONAMI_CC ,KONAMI_A, KONAMI_B, KONAMI_U, KONAMI_X, KONAMI_Y,
/*TODO*///		KONAMI_DP, KONAMI_NMI_STATE, KONAMI_IRQ_STATE, KONAMI_FIRQ_STATE };
	
    public static final int KONAMI_INT_NONE     =0;     /* No interrupt required */
    public static final int KONAMI_INT_IRQ      =1;	/* Standard IRQ interrupt */
    public static final int KONAMI_INT_FIRQ     =2;	/* Fast IRQ */
    public static final int KONAMI_INT_NMI      =4;	/* NMI */	/* NS 970909 */
    public static final int KONAMI_IRQ_LINE	=0;	/* IRQ line number */
    public static final int KONAMI_FIRQ_LINE    =1;     /* FIRQ line number */
	
	/* PUBLIC GLOBALS */
/*TODO*///	extern int  konami_ICount;
/*TODO*///	extern void (*konami_cpu_setlines_callback)( int lines ); /* callback called when A16-A23 are set */
	
	/* PUBLIC FUNCTIONS */
/*TODO*///	extern extern void konami_reset(void *param);
/*TODO*///	extern extern int konami_execute(int cycles);  /* NS 970908 */
/*TODO*///	extern unsigned konami_get_context(void *dst);
/*TODO*///	extern void konami_set_context(void *src);
/*TODO*///	extern unsigned konami_get_pc(void);
/*TODO*///	extern void konami_set_pc(unsigned val);
/*TODO*///	extern unsigned konami_get_sp(void);
/*TODO*///	extern void konami_set_sp(unsigned val);
/*TODO*///	extern unsigned konami_get_reg(int regnum);
/*TODO*///	extern void konami_set_reg(int regnum, unsigned val);
/*TODO*///	extern void konami_set_nmi_line(int state);
/*TODO*///	extern void konami_set_irq_line(int irqline, int state);
/*TODO*///	extern void konami_set_irq_callback(int (*callback)(int irqline));
/*TODO*///	extern const char *konami_info(void *context,int regnum);
/*TODO*///	extern unsigned konami_dasm(char *buffer, unsigned pc);
	
	/****************************************************************************/
	/* Read a byte from given memory location									*/
	/****************************************************************************/
/*TODO*///	#define KONAMI_RDMEM(Addr) ((unsigned)cpu_readmem16(Addr))
	
	/****************************************************************************/
	/* Write a byte to given memory location                                    */
	/****************************************************************************/
/*TODO*///	#define KONAMI_WRMEM(Addr,Value) (cpu_writemem16(Addr,Value))
	
	/****************************************************************************/
	/* Z80_RDOP() is identical to Z80_RDMEM() except it is used for reading     */
	/* opcodes. In case of system with memory mapped I/O, this function can be  */
	/* used to greatly speed up emulation                                       */
	/****************************************************************************/
/*TODO*///	#define KONAMI_RDOP(Addr) ((unsigned)cpu_readop(Addr))
	
	/****************************************************************************/
	/* Z80_RDOP_ARG() is identical to Z80_RDOP() except it is used for reading  */
	/* opcode arguments. This difference can be used to support systems that    */
	/* use different encoding mechanisms for opcodes and opcode arguments       */
	/****************************************************************************/
/*TODO*///	#define KONAMI_RDOP_ARG(Addr) ((unsigned)cpu_readop_arg(Addr))
	
/*TODO*///	#ifndef FALSE
/*TODO*///	#    define FALSE 0
/*TODO*///	#endif
/*TODO*///	#ifndef TRUE
/*TODO*///	#    define TRUE (!FALSE)
/*TODO*///	#endif
	
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///	extern unsigned Dasmknmi (char *buffer, unsigned pc);
/*TODO*///	#endif
	
/*TODO*///	#endif /* _KONAMI_H */
}
