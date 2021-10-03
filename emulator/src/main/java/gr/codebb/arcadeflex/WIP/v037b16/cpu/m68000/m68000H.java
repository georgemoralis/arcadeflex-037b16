package gr.codebb.arcadeflex.WIP.v037b16.cpu.m68000;

public class m68000H {
    
    /* NOTE: M68K_SP fetches the current SP, be it USP, ISP, or MSP */
    public static final int M68K_PC = 1;
    public static final int M68K_SP = 2;
    public static final int M68K_ISP = 3;
    public static final int M68K_USP = 4;
    public static final int M68K_MSP = 5;
    public static final int M68K_SR = 6;
    public static final int M68K_VBR = 7;
    public static final int M68K_SFC = 8;
    public static final int M68K_DFC = 9;
    public static final int M68K_CACR = 10;
    public static final int M68K_CAAR = 11;
    public static final int M68K_PREF_ADDR = 12;
    public static final int M68K_PREF_DATA = 13;
    public static final int M68K_D0 = 14;
    public static final int M68K_D1 = 15;
    public static final int M68K_D2 = 16;
    public static final int M68K_D3 = 17;
    public static final int M68K_D4 = 18;
    public static final int M68K_D5 = 19;
    public static final int M68K_D6 = 20;
    public static final int M68K_D7 = 21;
    public static final int M68K_A0 = 22;
    public static final int M68K_A1 = 23;
    public static final int M68K_A2 = 24;
    public static final int M68K_A3 = 25;
    public static final int M68K_A4 = 26;
    public static final int M68K_A5 = 27;
    public static final int M68K_A6 = 28;
    public static final int M68K_A7 = 29;
    
    /*TODO*///extern int m68k_ICount;
    /*TODO*///
    /*TODO*////* Redirect memory calls */
    /*TODO*///
    /*TODO*///struct m68k_memory_interface
    /*TODO*///{
    /*TODO*///	offs_t		opcode_xor;						// Address Calculation
    /*TODO*///	data8_t		(*read8)(offs_t);				// Normal read 8 bit
    /*TODO*///	data16_t	(*read16)(offs_t);				// Normal read 16 bit
    /*TODO*///	data32_t	(*read32)(offs_t);				// Normal read 32 bit
    /*TODO*///	void		(*write8)(offs_t, data8_t);		// Write 8 bit
    /*TODO*///	void		(*write16)(offs_t, data16_t);	// Write 16 bit
    /*TODO*///	void		(*write32)(offs_t, data32_t);	// Write 32 bit
    /*TODO*///	void		(*changepc)(offs_t);			// Change PC routine
    /*TODO*///
    /*TODO*///    // For Encrypted Stuff
    /*TODO*///
    /*TODO*///	data8_t		(*read8pc)(offs_t);				// PC Relative read 8 bit
    /*TODO*///	data16_t	(*read16pc)(offs_t);			// PC Relative read 16 bit
    /*TODO*///	data32_t	(*read32pc)(offs_t);			// PC Relative read 32 bit
    /*TODO*///
    /*TODO*///	data16_t	(*read16d)(offs_t);				// Direct read 16 bit
    /*TODO*///	data32_t	(*read32d)(offs_t);				// Direct read 32 bit
    /*TODO*///};
    
    /* The MAME API for MC68000 */    
    public static final int MC68000_INT_NONE = 0;
    public static final int MC68000_IRQ_1 = 1;
    public static final int MC68000_IRQ_2 = 2;
    public static final int MC68000_IRQ_3 = 3;
    public static final int MC68000_IRQ_4 = 4;
    public static final int MC68000_IRQ_5 = 5;
    public static final int MC68000_IRQ_6 = 6;
    public static final int MC68000_IRQ_7 = 7;
    
    public static final int MC68000_INT_ACK_AUTOVECTOR = -1;
    public static final int MC68000_INT_ACK_SPURIOUS = -2;
    
    /*TODO*///#define m68000_ICount                   m68k_ICount
    /*TODO*///extern void m68000_init(void);
    /*TODO*///extern void m68000_reset(void *param);
    /*TODO*///extern void m68000_exit(void);
    /*TODO*///extern int	m68000_execute(int cycles);
    /*TODO*///extern unsigned m68000_get_context(void *dst);
    /*TODO*///extern void m68000_set_context(void *src);
    /*TODO*///extern unsigned m68000_get_pc(void);
    /*TODO*///extern void m68000_set_pc(unsigned val);
    /*TODO*///extern unsigned m68000_get_sp(void);
    /*TODO*///extern void m68000_set_sp(unsigned val);
    /*TODO*///extern unsigned m68000_get_reg(int regnum);
    /*TODO*///extern void m68000_set_reg(int regnum, unsigned val);
    /*TODO*///extern void m68000_set_nmi_line(int state);
    /*TODO*///extern void m68000_set_irq_line(int irqline, int state);
    /*TODO*///extern void m68000_set_irq_callback(int (*callback)(int irqline));
    /*TODO*///extern const char *m68000_info(void *context, int regnum);
    /*TODO*///extern unsigned m68000_dasm(char *buffer, unsigned pc);
    /*TODO*///extern void m68000_memory_interface_set(int Entry,void * memory_routine);
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * M68010 section
    /*TODO*/// ****************************************************************************/
    /*TODO*///#if HAS_M68010
    /*TODO*///#define MC68010_INT_NONE                MC68000_INT_NONE
    /*TODO*///#define MC68010_IRQ_1					MC68000_IRQ_1
    /*TODO*///#define MC68010_IRQ_2					MC68000_IRQ_2
    /*TODO*///#define MC68010_IRQ_3					MC68000_IRQ_3
    /*TODO*///#define MC68010_IRQ_4					MC68000_IRQ_4
    /*TODO*///#define MC68010_IRQ_5					MC68000_IRQ_5
    /*TODO*///#define MC68010_IRQ_6					MC68000_IRQ_6
    /*TODO*///#define MC68010_IRQ_7					MC68000_IRQ_7
    /*TODO*///#define MC68010_INT_ACK_AUTOVECTOR		MC68000_INT_ACK_AUTOVECTOR
    /*TODO*///#define MC68010_INT_ACK_SPURIOUS		MC68000_INT_ACK_SPURIOUS
    /*TODO*///
    /*TODO*///#define m68010_ICount                   m68k_ICount
    /*TODO*///extern void m68010_init(void);
    /*TODO*///extern void m68010_reset(void *param);
    /*TODO*///extern void m68010_exit(void);
    /*TODO*///extern int	m68010_execute(int cycles);
    /*TODO*///extern unsigned m68010_get_context(void *dst);
    /*TODO*///extern void m68010_set_context(void *src);
    /*TODO*///extern unsigned m68010_get_pc(void);
    /*TODO*///extern void m68010_set_pc(unsigned val);
    /*TODO*///extern unsigned m68010_get_sp(void);
    /*TODO*///extern void m68010_set_sp(unsigned val);
    /*TODO*///extern unsigned m68010_get_reg(int regnum);
    /*TODO*///extern void m68010_set_reg(int regnum, unsigned val);
    /*TODO*///extern void m68010_set_nmi_line(int state);
    /*TODO*///extern void m68010_set_irq_line(int irqline, int state);
    /*TODO*///extern void m68010_set_irq_callback(int (*callback)(int irqline));
    /*TODO*///const char *m68010_info(void *context, int regnum);
    /*TODO*///extern unsigned m68010_dasm(char *buffer, unsigned pc);
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * M68EC020 section
    /*TODO*/// ****************************************************************************/
    /*TODO*///#if HAS_M68EC020
    /*TODO*///#define MC68EC020_INT_NONE				MC68000_INT_NONE
    /*TODO*///#define MC68EC020_IRQ_1					MC68000_IRQ_1
    /*TODO*///#define MC68EC020_IRQ_2					MC68000_IRQ_2
    /*TODO*///#define MC68EC020_IRQ_3					MC68000_IRQ_3
    /*TODO*///#define MC68EC020_IRQ_4					MC68000_IRQ_4
    /*TODO*///#define MC68EC020_IRQ_5					MC68000_IRQ_5
    /*TODO*///#define MC68EC020_IRQ_6					MC68000_IRQ_6
    /*TODO*///#define MC68EC020_IRQ_7					MC68000_IRQ_7
    /*TODO*///#define MC68EC020_INT_ACK_AUTOVECTOR	MC68000_INT_ACK_AUTOVECTOR
    /*TODO*///#define MC68EC020_INT_ACK_SPURIOUS		MC68000_INT_ACK_SPURIOUS
    /*TODO*///
    /*TODO*///#define m68ec020_ICount                 m68k_ICount
    /*TODO*///extern void m68ec020_init(void);
    /*TODO*///extern void m68ec020_reset(void *param);
    /*TODO*///extern void m68ec020_exit(void);
    /*TODO*///extern int	m68ec020_execute(int cycles);
    /*TODO*///extern unsigned m68ec020_get_context(void *dst);
    /*TODO*///extern void m68ec020_set_context(void *src);
    /*TODO*///extern unsigned m68ec020_get_pc(void);
    /*TODO*///extern void m68ec020_set_pc(unsigned val);
    /*TODO*///extern unsigned m68ec020_get_sp(void);
    /*TODO*///extern void m68ec020_set_sp(unsigned val);
    /*TODO*///extern unsigned m68ec020_get_reg(int regnum);
    /*TODO*///extern void m68ec020_set_reg(int regnum, unsigned val);
    /*TODO*///extern void m68ec020_set_nmi_line(int state);
    /*TODO*///extern void m68ec020_set_irq_line(int irqline, int state);
    /*TODO*///extern void m68ec020_set_irq_callback(int (*callback)(int irqline));
    /*TODO*///const char *m68ec020_info(void *context, int regnum);
    /*TODO*///extern unsigned m68ec020_dasm(char *buffer, unsigned pc);
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * M68020 section
    /*TODO*/// ****************************************************************************/
    /*TODO*///#if HAS_M68020
    /*TODO*///#define MC68020_INT_NONE				MC68000_INT_NONE
    /*TODO*///#define MC68020_IRQ_1					MC68000_IRQ_1
    /*TODO*///#define MC68020_IRQ_2					MC68000_IRQ_2
    /*TODO*///#define MC68020_IRQ_3					MC68000_IRQ_3
    /*TODO*///#define MC68020_IRQ_4					MC68000_IRQ_4
    /*TODO*///#define MC68020_IRQ_5					MC68000_IRQ_5
    /*TODO*///#define MC68020_IRQ_6					MC68000_IRQ_6
    /*TODO*///#define MC68020_IRQ_7					MC68000_IRQ_7
    /*TODO*///#define MC68020_INT_ACK_AUTOVECTOR		MC68000_INT_ACK_AUTOVECTOR
    /*TODO*///#define MC68020_INT_ACK_SPURIOUS		MC68000_INT_ACK_SPURIOUS
    /*TODO*///
    /*TODO*///#define m68020_ICount                   m68k_ICount
    /*TODO*///extern void m68020_init(void);
    /*TODO*///extern void m68020_reset(void *param);
    /*TODO*///extern void m68020_exit(void);
    /*TODO*///extern int	m68020_execute(int cycles);
    /*TODO*///extern unsigned m68020_get_context(void *dst);
    /*TODO*///extern void m68020_set_context(void *src);
    /*TODO*///extern unsigned m68020_get_pc(void);
    /*TODO*///extern void m68020_set_pc(unsigned val);
    /*TODO*///extern unsigned m68020_get_sp(void);
    /*TODO*///extern void m68020_set_sp(unsigned val);
    /*TODO*///extern unsigned m68020_get_reg(int regnum);
    /*TODO*///extern void m68020_set_reg(int regnum, unsigned val);
    /*TODO*///extern void m68020_set_nmi_line(int state);
    /*TODO*///extern void m68020_set_irq_line(int irqline, int state);
    /*TODO*///extern void m68020_set_irq_callback(int (*callback)(int irqline));
    /*TODO*///const char *m68020_info(void *context, int regnum);
    /*TODO*///extern unsigned m68020_dasm(char *buffer, unsigned pc);
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///// C Core header
    /*TODO*///#include "m68kmame.h"
    /*TODO*///
    /*TODO*///#endif /* M68000__HEADER */    
}
