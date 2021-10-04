/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.cpu.m68000;

import static common.libc.cstdio.*;

public class m68k_in
{
	/*
must fix:
	callm
	chk
	chk2cmp2
*/
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



/* Input file for m68kmake
 * -----------------------
 *
 * All sections begin with 80 X's in a row followed by an end-of-line
 * sequence.
 * After this, m68kmake will expect to find one of the following section
 * identifiers:
 *    M68KMAKE_PROTOTYPE_HEADER      - header for opcode handler prototypes
 *    M68KMAKE_PROTOTYPE_FOOTER      - footer for opcode handler prototypes
 *    M68KMAKE_TABLE_HEADER          - header for opcode handler jumptable
 *    M68KMAKE_TABLE_FOOTER          - footer for opcode handler jumptable
 *    M68KMAKE_TABLE_BODY            - the table itself
 *    M68KMAKE_OPCODE_HANDLER_HEADER - header for opcode handler implementation
 *    M68KMAKE_OPCODE_HANDLER_FOOTER - footer for opcode handler implementation
 *    M68KMAKE_OPCODE_HANDLER_BODY   - body section for opcode handler implementation
 *
 * NOTE: M68KMAKE_OPCODE_HANDLER_BODY must be last in the file and
 *       M68KMAKE_TABLE_BODY must be second last in the file.
 *
 * The M68KMAKE_OPHANDLER_BODY section contains the opcode handler
 * primitives themselves.  Each opcode handler begins with:
 *    M68KMAKE_OP(A, B, C, D)
 *
 * where A is the opcode handler name, B is the size of the operation,
 * C denotes any special processing mode, and D denotes a specific
 * addressing mode.
 * For C and D where nothing is specified, use "."
 *
 * Example:
 *     M68KMAKE_OP(abcd, 8, rr, .)   abcd, size 8, register to register, default EA
 *     M68KMAKE_OP(abcd, 8, mm, ax7) abcd, size 8, memory to memory, register X is A7
 *     M68KMAKE_OP(tst, 16, ., pcix) tst, size 16, PCIX addressing
 *
 * All opcode handler primitives end with a closing curly brace "}" at column 1
 *
 * NOTE: Do not place a M68KMAKE_OP() directive inside the opcode handler,
 *       and do not put a closing curly brace at column 1 unless it is
 *       marking the end of the handler!
 *
 * Inside the handler, m68kmake will recognize M68KMAKE_GET_OPER_xx_xx,
 * M68KMAKE_GET_EA_xx_xx, and M68KMAKE_CC directives, and create multiple
 * opcode handlers to handle variations in the opcode handler.
 * Note: M68KMAKE_CC will only be interpreted in condition code opcodes.
 * As well, M68KMAKE_GET_EA_xx_xx and M68KMAKE_GET_OPER_xx_xx will only
 * be interpreted on instructions where the corresponding table entry
 * specifies multiple effective addressing modes.
 * Example:
 * clr       32  .     .     0100001010......  A+-DXWL...  U U U   12   6   4
 *
 * This table entry says that the clr.l opcde has 7 variations (A+-DXWL).
 * It is run in user or supervisor mode for all CPUs, and uses 12 cycles for
 * 68000, 6 cycles for 68010, and 4 cycles for 68020.
 */

/*TODO*///XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
/*TODO*///M68KMAKE_PROTOTYPE_HEADER
/*TODO*///
/*TODO*///#ifndef M68KOPS__HEADER
/*TODO*///#define M68KOPS__HEADER
/*TODO*///
/*TODO*////* ======================================================================== */
/*TODO*////* ============================ OPCODE HANDLERS =========================== */
/*TODO*////* ======================================================================== */
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
/*TODO*///M68KMAKE_PROTOTYPE_FOOTER
/*TODO*///
/*TODO*///
/*TODO*////* Build the opcode handler table */
/*TODO*///
/*TODO*///extern void (*m68ki_instruction_jump_table[0x10000])(void); /* opcode handler jump table */
/*TODO*///extern unsigned char m68ki_cycles[][0x10000];
/*TODO*///
/*TODO*///
/*TODO*////* ======================================================================== */
/*TODO*////* ============================== END OF FILE ============================= */
/*TODO*////* ======================================================================== */
/*TODO*///
/*TODO*///#endif /* M68KOPS__HEADER */
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
/*TODO*///M68KMAKE_TABLE_HEADER

/* ======================================================================== */
/* ========================= OPCODE TABLE BUILDER ========================= */
/* ======================================================================== */


	
	public static final int NUM_CPU_TYPES = 3;
	
        public static opcode[] m68ki_instruction_jump_table = new opcode[0x10000]; /* opcode handler jump table */
	public static char[][] m68ki_cycles = new char[NUM_CPU_TYPES][0x10000]; /* Cycles used by CPU type */
        
        public static abstract interface opcode {
            public abstract void handler();
        }

	/* This is used to generate the opcode handler jump table */
	public static class opcode_handler_struct implements Comparable<opcode_handler_struct>
	{
            public opcode opcode_handler; /* handler function */
            public int mask;			/* mask on opcode */
            public int match;			/* what to match after masking */
            public char[] cycles=new char[NUM_CPU_TYPES]; /* cycles each cpu type takes */

            public opcode_handler_struct(opcode opcode_handler, int mask, int match, char[] cycles) {
                this.opcode_handler = opcode_handler;
                this.mask = mask;
                this.match = match;
                this.cycles = cycles;
            }

            /*
             * Comparison function for qsort()
             * For entries with an equal number of set bits in
             * the mask compare the match values
             */
            @Override
            public int compareTo(opcode_handler_struct b) { //compare_nof_true_bits
/*TODO*///                if (cycles != ((opcode_handler_struct) b).cycles) {
/*TODO*///                    return (int) (cycles - ((opcode_handler_struct) b).cycles);
/*TODO*///                }
                if (mask != ((opcode_handler_struct) b).mask) {
                    return (int) (mask - ((opcode_handler_struct) b).mask);
                }
                return (int) (match - ((opcode_handler_struct) b).match);
            }
        }
	
	/* Build the opcode handler jump table */
	public static void m68ki_build_opcode_table()
	{
		opcode_handler_struct ostruct;
		int instr;
		int i;
		int j;
		int k;
	
		for(i = 0; i < 0x10000; i++)
		{
			/* default to illegal */
			m68ki_instruction_jump_table[i] = m68k_op_illegal;
			for(k=0;k<NUM_CPU_TYPES;k++)
				m68ki_cycles[k][i] = 0;
		}
	
		int ostruct_idx = 0;
                ostruct = m68k_opcode_handler_table[ostruct_idx];
		while(ostruct.mask != 0xff00)
		{
			for(i = 0;i < 0x10000;i++)
			{
				if((i & ostruct.mask) == ostruct.match)
				{
					m68ki_instruction_jump_table[i] = ostruct.opcode_handler;
					for(k=0;k<NUM_CPU_TYPES;k++)
						m68ki_cycles[k][i] = ostruct.cycles[k];
				}
			}
			ostruct_idx++;
		}
		while(ostruct.mask == 0xff00)
		{
			for(i = 0;i <= 0xff;i++)
			{
				m68ki_instruction_jump_table[ostruct.match | i] = ostruct.opcode_handler;
				for(k=0;k<NUM_CPU_TYPES;k++)
					m68ki_cycles[k][ostruct.match | i] = ostruct.cycles[k];
			}
			ostruct_idx++;
		}
		while(ostruct.mask == 0xf1f8)
		{
			for(i = 0;i < 8;i++)
			{
				for(j = 0;j < 8;j++)
				{
					instr = ostruct.match | (i << 9) | j;
					m68ki_instruction_jump_table[instr] = ostruct.opcode_handler;
					for(k=0;k<NUM_CPU_TYPES;k++)
						m68ki_cycles[k][instr] = ostruct.cycles[k];
					if((instr & 0xf000) == 0xe000 && ((instr & 0x20)==0))
						m68ki_cycles[0][instr] = m68ki_cycles[1][instr] = (char) (ostruct.cycles[k] + ((((j-1)&7)+1)<<1));
				}
			}
			ostruct_idx++;
		}
		while(ostruct.mask == 0xfff0)
		{
			for(i = 0;i <= 0x0f;i++)
			{
				m68ki_instruction_jump_table[ostruct.match | i] = ostruct.opcode_handler;
				for(k=0;k<NUM_CPU_TYPES;k++)
					m68ki_cycles[k][ostruct.match | i] = ostruct.cycles[k];
			}
			ostruct_idx++;
		}
		while(ostruct.mask == 0xf1ff)
		{
			for(i = 0;i <= 0x07;i++)
			{
				m68ki_instruction_jump_table[ostruct.match | (i << 9)] = ostruct.opcode_handler;
				for(k=0;k<NUM_CPU_TYPES;k++)
					m68ki_cycles[k][ostruct.match | (i << 9)] = ostruct.cycles[k];
			}
			ostruct_idx++;
		}
		while(ostruct.mask == 0xfff8)
		{
			for(i = 0;i <= 0x07;i++)
			{
				m68ki_instruction_jump_table[ostruct.match | i] = ostruct.opcode_handler;
				for(k=0;k<NUM_CPU_TYPES;k++)
					m68ki_cycles[k][ostruct.match | i] = ostruct.cycles[k];
			}
			ostruct_idx++;
		}
		while(ostruct.mask == 0xffff)
		{
			m68ki_instruction_jump_table[ostruct.match] = ostruct.opcode_handler;
			for(k=0;k<NUM_CPU_TYPES;k++)
				m68ki_cycles[k][ostruct.match] = ostruct.cycles[k];
			ostruct_idx++;
		}
	}
	
	
/*TODO*///	/* ======================================================================== */
/*TODO*///	/* ============================== END OF FILE ============================= */
/*TODO*///	/* ======================================================================== */
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
/*TODO*///	M68KMAKE_OPCODE_HANDLER_HEADER
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ======================================================================== */
/*TODO*///	/* ========================= INSTRUCTION HANDLERS ========================= */
/*TODO*///	/* ======================================================================== */
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
/*TODO*///	M68KMAKE_OPCODE_HANDLER_FOOTER
/*TODO*///	
/*TODO*///	/* ======================================================================== */
/*TODO*///	/* ============================== END OF FILE ============================= */
/*TODO*///	/* ======================================================================== */
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
/*TODO*///	M68KMAKE_TABLE_BODY
/*TODO*///	
/*TODO*///	The following table is arranged as follows:
/*TODO*///	
/*TODO*///	name:        Opcode mnemonic
/*TODO*///	
/*TODO*///	size:        Operation size
/*TODO*///	
/*TODO*///	spec proc:   Special processing mode:
/*TODO*///	                 .:    normal
/*TODO*///	                 s:    static operand
/*TODO*///	                 r:    register operand
/*TODO*///	                 rr:   register to register
/*TODO*///	                 mm:   memory to memory
/*TODO*///	                 er:   effective address to register
/*TODO*///	                 re:   register to effective address
/*TODO*///	                 dd:   data register to data register
/*TODO*///	                 da:   data register to address register
/*TODO*///	                 aa:   address register to address register
/*TODO*///	                 cr:   control register to register
/*TODO*///	                 rc:   register to control register
/*TODO*///	                 toc:  to condition code register
/*TODO*///	                 tos:  to status register
/*TODO*///	                 tou:  to user stack pointer
/*TODO*///	                 frc:  from condition code register
/*TODO*///	                 frs:  from status register
/*TODO*///	                 fru:  from user stack pointer
/*TODO*///	                 * for move.x, the special processing mode is a specific
/*TODO*///	                   destination effective addressing mode.
/*TODO*///	
/*TODO*///	spec ea:     Specific effective addressing mode:
/*TODO*///	                 .:    normal
/*TODO*///	                 i:    immediate
/*TODO*///	                 d:    data register
/*TODO*///	                 a:    address register
/*TODO*///	                 ai:   address register indirect
/*TODO*///	                 pi:   address register indirect with postincrement
/*TODO*///	                 pd:   address register indirect with predecrement
/*TODO*///	                 di:   address register indirect with displacement
/*TODO*///	                 ix:   address register indirect with index
/*TODO*///	                 aw:   absolute word address
/*TODO*///	                 al:   absolute long address
/*TODO*///	                 pcdi: program counter relative with displacement
/*TODO*///	                 pcix: program counter relative with index
/*TODO*///	                 a7:   register specified in instruction is A7
/*TODO*///	                 ax7:  register field X of instruction is A7
/*TODO*///	                 ay7:  register field Y of instruction is A7
/*TODO*///	                 axy7: register fields X and Y of instruction are A7
/*TODO*///	
/*TODO*///	bit pattern: Pattern to recognize this opcode.  "." means don't care.
/*TODO*///	
/*TODO*///	allowed ea:  List of allowed addressing modes:
/*TODO*///	                 .: not present
/*TODO*///	                 A: address register indirect
/*TODO*///	                 +: ARI (address register indirect) with postincrement
/*TODO*///	                 -: ARI with predecrement
/*TODO*///	                 D: ARI with displacement
/*TODO*///	                 X: ARI with index
/*TODO*///	                 W: absolute word address
/*TODO*///	                 L: absolute long address
/*TODO*///	                 d: program counter indirect with displacement
/*TODO*///	                 x: program counter indirect with index
/*TODO*///	                 I: immediate
/*TODO*///	mode:        CPU operating mode for each cpu type.  U = user or supervisor,
/*TODO*///	             S = supervisor only, "." = opcode not present.
/*TODO*///	
/*TODO*///	cpu cycles:  Base number of cycles required to execute this opcode on the
/*TODO*///	             specified CPU type.
/*TODO*///	             Use "." if CPU does not have this opcode.
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	              spec  spec                    allowed ea  mode   cpu cycles
/*TODO*///	name    size  proc   ea   bit pattern       A+-DXWLdxI  0 1 2  000 010 020  comments
/*TODO*///	======  ====  ====  ====  ================  ==========  = = =  === === ===  =============
/*TODO*///	M68KMAKE_TABLE_START
        /* Opcode handler table */
	static opcode_handler_struct m68k_opcode_handler_table[] =
	{
	/*   function                      mask    match    000  010  020 */
/*TODO*///	1010       0  .     .     1010............  ..........  U U U    4   4   4
/*TODO*///	1111       0  .     .     1111............  ..........  U U U    4   4   4
/*TODO*///	abcd       8  rr    .     1100...100000...  ..........  U U U    6   6   4
/*TODO*///	abcd       8  mm    ax7   1100111100001...  ..........  U U U   18  18  16
/*TODO*///	abcd       8  mm    ay7   1100...100001111  ..........  U U U   18  18  16
/*TODO*///	abcd       8  mm    axy7  1100111100001111  ..........  U U U   18  18  16
/*TODO*///	abcd       8  mm    .     1100...100001...  ..........  U U U   18  18  16
/*TODO*///	add        8  er    d     1101...000000...  ..........  U U U    4   4   2
/*TODO*///	add        8  er    .     1101...000......  A+-DXWLdxI  U U U    4   4   2
/*TODO*///	add       16  er    d     1101...001000...  ..........  U U U    4   4   2
/*TODO*///	add       16  er    a     1101...001001...  ..........  U U U    4   4   2
/*TODO*///	add       16  er    .     1101...001......  A+-DXWLdxI  U U U    4   4   2
/*TODO*///	add       32  er    d     1101...010000...  ..........  U U U    6   6   2
/*TODO*///	add       32  er    a     1101...010001...  ..........  U U U    6   6   2
/*TODO*///	add       32  er    .     1101...010......  A+-DXWLdxI  U U U    6   6   2
/*TODO*///	add        8  re    .     1101...100......  A+-DXWL...  U U U    8   8   4
/*TODO*///	add       16  re    .     1101...101......  A+-DXWL...  U U U    8   8   4
/*TODO*///	add       32  re    .     1101...110......  A+-DXWL...  U U U   12  12   4
/*TODO*///	adda      16  .     d     1101...011000...  ..........  U U U    8   8   2
/*TODO*///	adda      16  .     a     1101...011001...  ..........  U U U    8   8   2
/*TODO*///	adda      16  .     .     1101...011......  A+-DXWLdxI  U U U    8   8   2
/*TODO*///	adda      32  .     d     1101...111000...  ..........  U U U    6   6   2
/*TODO*///	adda      32  .     a     1101...111001...  ..........  U U U    6   6   2
/*TODO*///	adda      32  .     .     1101...111......  A+-DXWLdxI  U U U    6   6   2
/*TODO*///	addi       8  .     d     0000011000000...  ..........  U U U    8   8   2
/*TODO*///	addi       8  .     .     0000011000......  A+-DXWL...  U U U   12  12   4
/*TODO*///	addi      16  .     d     0000011001000...  ..........  U U U    8   8   2
/*TODO*///	addi      16  .     .     0000011001......  A+-DXWL...  U U U   12  12   4
/*TODO*///	addi      32  .     d     0000011010000...  ..........  U U U   16  14   2
/*TODO*///	addi      32  .     .     0000011010......  A+-DXWL...  U U U   20  20   4
/*TODO*///	addq       8  .     d     0101...000000...  ..........  U U U    4   4   2
/*TODO*///	addq       8  .     .     0101...000......  A+-DXWL...  U U U    8   8   4
/*TODO*///	addq      16  .     d     0101...001000...  ..........  U U U    4   4   2
/*TODO*///	addq      16  .     a     0101...001001...  ..........  U U U    4   4   2
/*TODO*///	addq      16  .     .     0101...001......  A+-DXWL...  U U U    8   8   4
/*TODO*///	addq      32  .     d     0101...010000...  ..........  U U U    8   8   2
/*TODO*///	addq      32  .     a     0101...010001...  ..........  U U U    8   8   2
/*TODO*///	addq      32  .     .     0101...010......  A+-DXWL...  U U U   12  12   4
/*TODO*///	addx       8  rr    .     1101...100000...  ..........  U U U    4   4   2
/*TODO*///	addx      16  rr    .     1101...101000...  ..........  U U U    4   4   2
/*TODO*///	addx      32  rr    .     1101...110000...  ..........  U U U    8   6   2
/*TODO*///	addx       8  mm    ax7   1101111100001...  ..........  U U U   18  18  12
/*TODO*///	addx       8  mm    ay7   1101...100001111  ..........  U U U   18  18  12
/*TODO*///	addx       8  mm    axy7  1101111100001111  ..........  U U U   18  18  12
/*TODO*///	addx       8  mm    .     1101...100001...  ..........  U U U   18  18  12
/*TODO*///	addx      16  mm    .     1101...101001...  ..........  U U U   18  18  12
/*TODO*///	addx      32  mm    .     1101...110001...  ..........  U U U   30  30  12
/*TODO*///	and        8  er    d     1100...000000...  ..........  U U U    4   4   2
/*TODO*///	and        8  er    .     1100...000......  A+-DXWLdxI  U U U    4   4   2
/*TODO*///	and       16  er    d     1100...001000...  ..........  U U U    4   4   2
/*TODO*///	and       16  er    .     1100...001......  A+-DXWLdxI  U U U    4   4   2
/*TODO*///	and       32  er    d     1100...010000...  ..........  U U U    6   6   2
/*TODO*///	and       32  er    .     1100...010......  A+-DXWLdxI  U U U    6   6   2
/*TODO*///	and        8  re    .     1100...100......  A+-DXWL...  U U U    8   8   4
/*TODO*///	and       16  re    .     1100...101......  A+-DXWL...  U U U    8   8   4
/*TODO*///	and       32  re    .     1100...110......  A+-DXWL...  U U U   12  12   4
/*TODO*///	andi      16  toc   .     0000001000111100  ..........  U U U   20  16  12
/*TODO*///	andi      16  tos   .     0000001001111100  ..........  S S S   20  16  12
/*TODO*///	andi       8  .     d     0000001000000...  ..........  U U U    8   8   2
/*TODO*///	andi       8  .     .     0000001000......  A+-DXWL...  U U U   12  12   4
/*TODO*///	andi      16  .     d     0000001001000...  ..........  U U U    8   8   2
/*TODO*///	andi      16  .     .     0000001001......  A+-DXWL...  U U U   12  12   4
/*TODO*///	andi      32  .     d     0000001010000...  ..........  U U U   14  14   2
/*TODO*///	andi      32  .     .     0000001010......  A+-DXWL...  U U U   20  20   4
/*TODO*///	asr        8  s     .     1110...000000...  ..........  U U U    6   6   6
/*TODO*///	asr       16  s     .     1110...001000...  ..........  U U U    6   6   6
/*TODO*///	asr       32  s     .     1110...010000...  ..........  U U U    8   8   6
/*TODO*///	asr        8  r     .     1110...000100...  ..........  U U U    6   6   6
/*TODO*///	asr       16  r     .     1110...001100...  ..........  U U U    6   6   6
/*TODO*///	asr       32  r     .     1110...010100...  ..........  U U U    8   8   6
/*TODO*///	asr       16  .     .     1110000011......  A+-DXWL...  U U U    8   8   5
/*TODO*///	asl        8  s     .     1110...100000...  ..........  U U U    6   6   8
/*TODO*///	asl       16  s     .     1110...101000...  ..........  U U U    6   6   8
/*TODO*///	asl       32  s     .     1110...110000...  ..........  U U U    8   8   8
/*TODO*///	asl        8  r     .     1110...100100...  ..........  U U U    6   6   8
/*TODO*///	asl       16  r     .     1110...101100...  ..........  U U U    6   6   8
/*TODO*///	asl       32  r     .     1110...110100...  ..........  U U U    8   8   8
/*TODO*///	asl       16  .     .     1110000111......  A+-DXWL...  U U U    8   8   6
/*TODO*///	bcc        8  .     .     0110............  ..........  U U U    8   8   6
/*TODO*///	bcc       16  .     .     0110....00000000  ..........  U U U   10  10   6
/*TODO*///	bcc       32  .     .     0110....11111111  ..........  . . U    .   .   6
/*TODO*///	bchg       8  r     .     0000...101......  A+-DXWL...  U U U    8   8   4
/*TODO*///	bchg      32  r     d     0000...101000...  ..........  U U U    8   8   4
/*TODO*///	bchg       8  s     .     0000100001......  A+-DXWL...  U U U   12  12   4
/*TODO*///	bchg      32  s     d     0000100001000...  ..........  U U U   12  12   4
/*TODO*///	bclr       8  r     .     0000...110......  A+-DXWL...  U U U    8  10   4
/*TODO*///	bclr      32  r     d     0000...110000...  ..........  U U U   10  10   4
/*TODO*///	bclr       8  s     .     0000100010......  A+-DXWL...  U U U   12  12   4
/*TODO*///	bclr      32  s     d     0000100010000...  ..........  U U U   14  14   4
/*TODO*///	bfchg     32  .     d     1110101011000...  ..........  . . U    .   .  12  timing not quite correct
/*TODO*///	bfchg     32  .     .     1110101011......  A..DXWL...  . . U    .   .  20
/*TODO*///	bfclr     32  .     d     1110110011000...  ..........  . . U    .   .  12
/*TODO*///	bfclr     32  .     .     1110110011......  A..DXWL...  . . U    .   .  20
/*TODO*///	bfexts    32  .     d     1110101111000...  ..........  . . U    .   .   8
/*TODO*///	bfexts    32  .     .     1110101111......  A..DXWLdx.  . . U    .   .  15
/*TODO*///	bfextu    32  .     d     1110100111000...  ..........  . . U    .   .   8
/*TODO*///	bfextu    32  .     .     1110100111......  A..DXWLdx.  . . U    .   .  15
/*TODO*///	bfffo     32  .     d     1110110111000...  ..........  . . U    .   .  18
/*TODO*///	bfffo     32  .     .     1110110111......  A..DXWLdx.  . . U    .   .  28
/*TODO*///	bfins     32  .     d     1110111111000...  ..........  . . U    .   .  10
/*TODO*///	bfins     32  .     .     1110111111......  A..DXWL...  . . U    .   .  17
/*TODO*///	bfset     32  .     d     1110111011000...  ..........  . . U    .   .  12
/*TODO*///	bfset     32  .     .     1110111011......  A..DXWL...  . . U    .   .  20
/*TODO*///	bftst     32  .     d     1110100011000...  ..........  . . U    .   .   6
/*TODO*///	bftst     32  .     .     1110100011......  A..DXWLdx.  . . U    .   .  13
/*TODO*///	bkpt       0  .     .     0100100001001...  ..........  . U U    .  10  10
/*TODO*///	bra        8  .     .     01100000........  ..........  U U U   10  10  10
/*TODO*///	bra       16  .     .     0110000000000000  ..........  U U U   10  10  10
/*TODO*///	bra       32  .     .     0110000011111111  ..........  U U U    .   .  10
/*TODO*///	bset      32  r     d     0000...111000...  ..........  U U U    8   8   4
/*TODO*///	bset       8  r     .     0000...111......  A+-DXWL...  U U U    8   8   4
/*TODO*///	bset       8  s     .     0000100011......  A+-DXWL...  U U U   12  12   4
/*TODO*///	bset      32  s     d     0000100011000...  ..........  U U U   12  12   4
/*TODO*///	bsr        8  .     .     01100001........  ..........  U U U   18  18   7
/*TODO*///	bsr       16  .     .     0110000100000000  ..........  U U U   18  18   7
/*TODO*///	bsr       32  .     .     0110000111111111  ..........  . . U    .   .   7
/*TODO*///	btst       8  r     .     0000...100......  A+-DXWLdxI  U U U    4   4   4
/*TODO*///	btst      32  r     d     0000...100000...  ..........  U U U    6   6   4
/*TODO*///	btst       8  s     .     0000100000......  A+-DXWLdx.  U U U    8   8   4
/*TODO*///	btst      32  s     d     0000100000000...  ..........  U U U   10  10   4
/*TODO*///	callm     32  .     .     0000011011......  A..DXWLdx.  . . U    .   .  60  not properly emulated
/*TODO*///	cas        8  .     .     0000101011......  A+-DXWL...  . . U    .   .  12
/*TODO*///	cas       16  .     .     0000110011......  A+-DXWL...  . . U    .   .  12
/*TODO*///	cas       32  .     .     0000111011......  A+-DXWL...  . . U    .   .  12
/*TODO*///	cas2      16  .     .     0000110011111100  ..........  . . U    .   .  12
/*TODO*///	cas2      32  .     .     0000111011111100  ..........  . . U    .   .  12
/*TODO*///	chk       16  .     d     0100...110000...  ..........  U U U   10   8   8
/*TODO*///	chk       16  .     .     0100...110......  A+-DXWLdxI  U U U   10   8   8
/*TODO*///	chk       32  .     d     0100...100000...  ..........  . . U    .   .   8
/*TODO*///	chk       32  .     .     0100...100......  A+-DXWLdxI  . . U    .   .   8
/*TODO*///	chk2cmp2   8  .     pcdi  0000000011111010  ..........  . . U    .   .  23
/*TODO*///	chk2cmp2   8  .     pcix  0000000011111011  ..........  . . U    .   .  23
/*TODO*///	chk2cmp2   8  .     .     0000000011......  A..DXWL...  . . U    .   .  18
/*TODO*///	chk2cmp2  16  .     pcdi  0000001011111010  ..........  . . U    .   .  23
/*TODO*///	chk2cmp2  16  .     pcix  0000001011111011  ..........  . . U    .   .  23
/*TODO*///	chk2cmp2  16  .     .     0000001011......  A..DXWL...  . . U    .   .  18
/*TODO*///	chk2cmp2  32  .     pcdi  0000010011111010  ..........  . . U    .   .  23
/*TODO*///	chk2cmp2  32  .     pcix  0000010011111011  ..........  . . U    .   .  23
/*TODO*///	chk2cmp2  32  .     .     0000010011......  A..DXWL...  . . U    .   .  18
/*TODO*///	clr        8  .     d     0100001000000...  ..........  U U U    4   4   2
/*TODO*///	clr        8  .     .     0100001000......  A+-DXWL...  U U U    8   4   4
/*TODO*///	clr       16  .     d     0100001001000...  ..........  U U U    4   4   2
/*TODO*///	clr       16  .     .     0100001001......  A+-DXWL...  U U U    8   4   4
/*TODO*///	clr       32  .     d     0100001010000...  ..........  U U U    6   6   2
/*TODO*///	clr       32  .     .     0100001010......  A+-DXWL...  U U U   12   6   4
/*TODO*///	cmp        8  .     d     1011...000000...  ..........  U U U    4   4   2
/*TODO*///	cmp        8  .     .     1011...000......  A+-DXWLdxI  U U U    4   4   2
/*TODO*///	cmp       16  .     d     1011...001000...  ..........  U U U    4   4   2
/*TODO*///	cmp       16  .     a     1011...001001...  ..........  U U U    4   4   2
/*TODO*///	cmp       16  .     .     1011...001......  A+-DXWLdxI  U U U    4   4   2
/*TODO*///	cmp       32  .     d     1011...010000...  ..........  U U U    6   6   2
/*TODO*///	cmp       32  .     a     1011...010001...  ..........  U U U    6   6   2
/*TODO*///	cmp       32  .     .     1011...010......  A+-DXWLdxI  U U U    6   6   2
/*TODO*///	cmpa      16  .     d     1011...011000...  ..........  U U U    6   6   4
/*TODO*///	cmpa      16  .     a     1011...011001...  ..........  U U U    6   6   4
/*TODO*///	cmpa      16  .     .     1011...011......  A+-DXWLdxI  U U U    6   6   4
/*TODO*///	cmpa      32  .     d     1011...111000...  ..........  U U U    6   6   4
/*TODO*///	cmpa      32  .     a     1011...111001...  ..........  U U U    6   6   4
/*TODO*///	cmpa      32  .     .     1011...111......  A+-DXWLdxI  U U U    6   6   4
/*TODO*///	cmpi       8  .     d     0000110000000...  ..........  U U U    8   8   2
/*TODO*///	cmpi       8  .     .     0000110000......  A+-DXWL...  U U U    8   8   2
/*TODO*///	cmpi       8  .     pcdi  0000110000111010  ..........  . . U    .   .   7
/*TODO*///	cmpi       8  .     pcix  0000110000111011  ..........  . . U    .   .   9
/*TODO*///	cmpi      16  .     d     0000110001000...  ..........  U U U    8   8   2
/*TODO*///	cmpi      16  .     .     0000110001......  A+-DXWL...  U U U    8   8   2
/*TODO*///	cmpi      16  .     pcdi  0000110001111010  ..........  . . U    .   .   7
/*TODO*///	cmpi      16  .     pcix  0000110001111011  ..........  . . U    .   .   9
/*TODO*///	cmpi      32  .     d     0000110010000...  ..........  U U U   14  12   2
/*TODO*///	cmpi      32  .     .     0000110010......  A+-DXWL...  U U U   12  12   2
/*TODO*///	cmpi      32  .     pcdi  0000110010111010  ..........  . . U    .   .   7
/*TODO*///	cmpi      32  .     pcix  0000110010111011  ..........  . . U    .   .   9
/*TODO*///	cmpm       8  .     ax7   1011111100001...  ..........  U U U   12  12   9
/*TODO*///	cmpm       8  .     ay7   1011...100001111  ..........  U U U   12  12   9
/*TODO*///	cmpm       8  .     axy7  1011111100001111  ..........  U U U   12  12   9
/*TODO*///	cmpm       8  .     .     1011...100001...  ..........  U U U   12  12   9
/*TODO*///	cmpm      16  .     .     1011...101001...  ..........  U U U   12  12   9
/*TODO*///	cmpm      32  .     .     1011...110001...  ..........  U U U   20  20   9
/*TODO*///	cpbcc     32  .     .     1111...01.......  ..........  . . U    .   .   4  unemulated
/*TODO*///	cpdbcc    32  .     .     1111...001001...  ..........  . . U    .   .   4  unemulated
/*TODO*///	cpgen     32  .     .     1111...000......  ..........  . . U    .   .   4  unemulated
/*TODO*///	cpscc     32  .     .     1111...001......  ..........  . . U    .   .   4  unemulated
/*TODO*///	cptrapcc  32  .     .     1111...001111...  ..........  . . U    .   .   4  unemulated
/*TODO*///	dbt       16  .     .     0101000011001...  ..........  U U U   12  12   6
/*TODO*///	dbf       16  .     .     0101000111001...  ..........  U U U   14  14   6
/*TODO*///	dbcc      16  .     .     0101....11001...  ..........  U U U   12  12   6
/*TODO*///	divs      16  .     d     1000...111000...  ..........  U U U  158 122  56
/*TODO*///	divs      16  .     .     1000...111......  A+-DXWLdxI  U U U  158 122  56
/*TODO*///	divu      16  .     d     1000...011000...  ..........  U U U  140 108  44
/*TODO*///	divu      16  .     .     1000...011......  A+-DXWLdxI  U U U  140 108  44
/*TODO*///	divl      32  .     d     0100110001000...  ..........  . . U    .   .  84
/*TODO*///	divl      32  .     .     0100110001......  A+-DXWLdxI  . . U    .   .  84
/*TODO*///	eor        8  .     d     1011...100000...  ..........  U U U    4   4   2
/*TODO*///	eor        8  .     .     1011...100......  A+-DXWL...  U U U    8   8   4
/*TODO*///	eor       16  .     d     1011...101000...  ..........  U U U    4   4   2
/*TODO*///	eor       16  .     .     1011...101......  A+-DXWL...  U U U    8   8   4
/*TODO*///	eor       32  .     d     1011...110000...  ..........  U U U    8   6   2
/*TODO*///	eor       32  .     .     1011...110......  A+-DXWL...  U U U   12  12   4
/*TODO*///	eori      16  toc   .     0000101000111100  ..........  U U U   20  16  12
/*TODO*///	eori      16  tos   .     0000101001111100  ..........  S S S   20  16  12
/*TODO*///	eori       8  .     d     0000101000000...  ..........  U U U    8   8   2
/*TODO*///	eori       8  .     .     0000101000......  A+-DXWL...  U U U   12  12   4
/*TODO*///	eori      16  .     d     0000101001000...  ..........  U U U    8   8   2
/*TODO*///	eori      16  .     .     0000101001......  A+-DXWL...  U U U   12  12   4
/*TODO*///	eori      32  .     d     0000101010000...  ..........  U U U   16  14   2
/*TODO*///	eori      32  .     .     0000101010......  A+-DXWL...  U U U   20  20   4
/*TODO*///	exg       32  dd    .     1100...101000...  ..........  U U U    6   6   2
/*TODO*///	exg       32  aa    .     1100...101001...  ..........  U U U    6   6   2
/*TODO*///	exg       32  da    .     1100...110001...  ..........  U U U    6   6   2
/*TODO*///	ext       16  .     .     0100100010000...  ..........  U U U    4   4   4
/*TODO*///	ext       32  .     .     0100100011000...  ..........  U U U    4   4   4
/*TODO*///	extb      32  .     .     0100100111000...  ..........  . . U    .   .   4
/*TODO*///	illegal    0  .     .     0100101011111100  ..........  U U U    4   4   4
/*TODO*///	jmp       32  .     .     0100111011......  A..DXWLdx.  U U U    4   4   0
/*TODO*///	jsr       32  .     .     0100111010......  A..DXWLdx.  U U U   12  12   0
/*TODO*///	lea       32  .     .     0100...111......  A..DXWLdx.  U U U    0   0   2
/*TODO*///	link      16  .     a7    0100111001010111  ..........  U U U   16  16   5
/*TODO*///	link      16  .     .     0100111001010...  ..........  U U U   16  16   5
/*TODO*///	link      32  .     a7    0100100000001111  ..........  . . U    .   .   6
/*TODO*///	link      32  .     .     0100100000001...  ..........  . . U    .   .   6
/*TODO*///	lsr        8  s     .     1110...000001...  ..........  U U U    6   6   4
/*TODO*///	lsr       16  s     .     1110...001001...  ..........  U U U    6   6   4
/*TODO*///	lsr       32  s     .     1110...010001...  ..........  U U U    8   8   4
/*TODO*///	lsr        8  r     .     1110...000101...  ..........  U U U    6   6   6
/*TODO*///	lsr       16  r     .     1110...001101...  ..........  U U U    6   6   6
/*TODO*///	lsr       32  r     .     1110...010101...  ..........  U U U    8   8   6
/*TODO*///	lsr       16  .     .     1110001011......  A+-DXWL...  U U U    8   8   5
/*TODO*///	lsl        8  s     .     1110...100001...  ..........  U U U    6   6   4
/*TODO*///	lsl       16  s     .     1110...101001...  ..........  U U U    6   6   4
/*TODO*///	lsl       32  s     .     1110...110001...  ..........  U U U    8   8   4
/*TODO*///	lsl        8  r     .     1110...100101...  ..........  U U U    6   6   6
/*TODO*///	lsl       16  r     .     1110...101101...  ..........  U U U    6   6   6
/*TODO*///	lsl       32  r     .     1110...110101...  ..........  U U U    8   8   6
/*TODO*///	lsl       16  .     .     1110001111......  A+-DXWL...  U U U    8   8   5
/*TODO*///	move       8  d     d     0001...000000...  ..........  U U U    4   4   2
/*TODO*///	move       8  d     .     0001...000......  A+-DXWLdxI  U U U    4   4   2
/*TODO*///	move       8  ai    d     0001...010000...  ..........  U U U    8   8   4
/*TODO*///	move       8  ai    .     0001...010......  A+-DXWLdxI  U U U    8   8   4
/*TODO*///	move       8  pi    d     0001...011000...  ..........  U U U    8   8   4
/*TODO*///	move       8  pi    .     0001...011......  A+-DXWLdxI  U U U    8   8   4
/*TODO*///	move       8  pi7   d     0001111011000...  ..........  U U U    8   8   4
/*TODO*///	move       8  pi7   .     0001111011......  A+-DXWLdxI  U U U    8   8   4
/*TODO*///	move       8  pd    d     0001...100000...  ..........  U U U    8   8   5
/*TODO*///	move       8  pd    .     0001...100......  A+-DXWLdxI  U U U    8   8   5
/*TODO*///	move       8  pd7   d     0001111100000...  ..........  U U U    8   8   5
/*TODO*///	move       8  pd7   .     0001111100......  A+-DXWLdxI  U U U    8   8   5
/*TODO*///	move       8  di    d     0001...101000...  ..........  U U U   12  12   5
/*TODO*///	move       8  di    .     0001...101......  A+-DXWLdxI  U U U   12  12   5
/*TODO*///	move       8  ix    d     0001...110000...  ..........  U U U   14  14   7
/*TODO*///	move       8  ix    .     0001...110......  A+-DXWLdxI  U U U   14  14   7
/*TODO*///	move       8  aw    d     0001000111000...  ..........  U U U   12  12   4
/*TODO*///	move       8  aw    .     0001000111......  A+-DXWLdxI  U U U   12  12   4
/*TODO*///	move       8  al    d     0001001111000...  ..........  U U U   16  16   6
/*TODO*///	move       8  al    .     0001001111......  A+-DXWLdxI  U U U   16  16   6
/*TODO*///	move      16  d     d     0011...000000...  ..........  U U U    4   4   2
/*TODO*///	move      16  d     a     0011...000001...  ..........  U U U    4   4   2
/*TODO*///	move      16  d     .     0011...000......  A+-DXWLdxI  U U U    4   4   2
/*TODO*///	move      16  ai    d     0011...010000...  ..........  U U U    8   8   4
/*TODO*///	move      16  ai    a     0011...010001...  ..........  U U U    8   8   4
/*TODO*///	move      16  ai    .     0011...010......  A+-DXWLdxI  U U U    8   8   4
/*TODO*///	move      16  pi    d     0011...011000...  ..........  U U U    8   8   4
/*TODO*///	move      16  pi    a     0011...011001...  ..........  U U U    8   8   4
/*TODO*///	move      16  pi    .     0011...011......  A+-DXWLdxI  U U U    8   8   4
/*TODO*///	move      16  pd    d     0011...100000...  ..........  U U U    8   8   5
/*TODO*///	move      16  pd    a     0011...100001...  ..........  U U U    8   8   5
/*TODO*///	move      16  pd    .     0011...100......  A+-DXWLdxI  U U U    8   8   5
/*TODO*///	move      16  di    d     0011...101000...  ..........  U U U   12  12   5
/*TODO*///	move      16  di    a     0011...101001...  ..........  U U U   12  12   5
/*TODO*///	move      16  di    .     0011...101......  A+-DXWLdxI  U U U   12  12   5
/*TODO*///	move      16  ix    d     0011...110000...  ..........  U U U   14  14   7
/*TODO*///	move      16  ix    a     0011...110001...  ..........  U U U   14  14   7
/*TODO*///	move      16  ix    .     0011...110......  A+-DXWLdxI  U U U   14  14   7
/*TODO*///	move      16  aw    d     0011000111000...  ..........  U U U   12  12   4
/*TODO*///	move      16  aw    a     0011000111001...  ..........  U U U   12  12   4
/*TODO*///	move      16  aw    .     0011000111......  A+-DXWLdxI  U U U   12  12   4
/*TODO*///	move      16  al    d     0011001111000...  ..........  U U U   16  16   6
/*TODO*///	move      16  al    a     0011001111001...  ..........  U U U   16  16   6
/*TODO*///	move      16  al    .     0011001111......  A+-DXWLdxI  U U U   16  16   6
/*TODO*///	move      32  d     d     0010...000000...  ..........  U U U    4   4   2
/*TODO*///	move      32  d     a     0010...000001...  ..........  U U U    4   4   2
/*TODO*///	move      32  d     .     0010...000......  A+-DXWLdxI  U U U    4   4   2
/*TODO*///	move      32  ai    d     0010...010000...  ..........  U U U   12  12   4
/*TODO*///	move      32  ai    a     0010...010001...  ..........  U U U   12  12   4
/*TODO*///	move      32  ai    .     0010...010......  A+-DXWLdxI  U U U   12  12   4
/*TODO*///	move      32  pi    d     0010...011000...  ..........  U U U   12  12   4
/*TODO*///	move      32  pi    a     0010...011001...  ..........  U U U   12  12   4
/*TODO*///	move      32  pi    .     0010...011......  A+-DXWLdxI  U U U   12  12   4
/*TODO*///	move      32  pd    d     0010...100000...  ..........  U U U   12  14   5
/*TODO*///	move      32  pd    a     0010...100001...  ..........  U U U   12  14   5
/*TODO*///	move      32  pd    .     0010...100......  A+-DXWLdxI  U U U   12  14   5
/*TODO*///	move      32  di    d     0010...101000...  ..........  U U U   16  16   5
/*TODO*///	move      32  di    a     0010...101001...  ..........  U U U   16  16   5
/*TODO*///	move      32  di    .     0010...101......  A+-DXWLdxI  U U U   16  16   5
/*TODO*///	move      32  ix    d     0010...110000...  ..........  U U U   18  18   7
/*TODO*///	move      32  ix    a     0010...110001...  ..........  U U U   18  18   7
/*TODO*///	move      32  ix    .     0010...110......  A+-DXWLdxI  U U U   18  18   7
/*TODO*///	move      32  aw    d     0010000111000...  ..........  U U U   16  16   4
/*TODO*///	move      32  aw    a     0010000111001...  ..........  U U U   16  16   4
/*TODO*///	move      32  aw    .     0010000111......  A+-DXWLdxI  U U U   16  16   4
/*TODO*///	move      32  al    d     0010001111000...  ..........  U U U   20  20   6
/*TODO*///	move      32  al    a     0010001111001...  ..........  U U U   20  20   6
/*TODO*///	move      32  al    .     0010001111......  A+-DXWLdxI  U U U   20  20   6
/*TODO*///	movea     16  .     d     0011...001000...  ..........  U U U    4   4   2
/*TODO*///	movea     16  .     a     0011...001001...  ..........  U U U    4   4   2
/*TODO*///	movea     16  .     .     0011...001......  A+-DXWLdxI  U U U    4   4   2
/*TODO*///	movea     32  .     d     0010...001000...  ..........  U U U    4   4   2
/*TODO*///	movea     32  .     a     0010...001001...  ..........  U U U    4   4   2
/*TODO*///	movea     32  .     .     0010...001......  A+-DXWLdxI  U U U    4   4   2
/*TODO*///	move      16  frc   d     0100001011000...  ..........  . U U    .   4   4
/*TODO*///	move      16  frc   .     0100001011......  A+-DXWL...  . U U    .   8   4
/*TODO*///	move      16  toc   d     0100010011000...  ..........  U U U   12  12   4
/*TODO*///	move      16  toc   .     0100010011......  A+-DXWLdxI  U U U   12  12   4
/*TODO*///	move      16  frs   d     0100000011000...  ..........  U S S    6   4   8 U only for 000
/*TODO*///	move      16  frs   .     0100000011......  A+-DXWL...  U S S    8   8   8 U only for 000
/*TODO*///	move      16  tos   d     0100011011000...  ..........  S S S   12  12   8
/*TODO*///	move      16  tos   .     0100011011......  A+-DXWLdxI  S S S   12  12   8
/*TODO*///	move      32  fru   .     0100111001101...  ..........  S S S    4   6   2
/*TODO*///	move      32  tou   .     0100111001100...  ..........  S S S    4   6   2
/*TODO*///	movec     32  cr    .     0100111001111010  ..........  . S S    .  12   6
/*TODO*///	movec     32  rc    .     0100111001111011  ..........  . S S    .  10  12
/*TODO*///	movem     16  re    pd    0100100010100...  ..........  U U U    8   8   4
/*TODO*///	movem     16  re    .     0100100010......  A..DXWL...  U U U    8   8   4
/*TODO*///	movem     32  re    pd    0100100011100...  ..........  U U U    8   8   4
/*TODO*///	movem     32  re    .     0100100011......  A..DXWL...  U U U    8   8   4
/*TODO*///	movem     16  er    pi    0100110010011...  ..........  U U U   12  12   8
/*TODO*///	movem     16  er    pcdi  0100110010111010  ..........  U U U   16  16   9
/*TODO*///	movem     16  er    pcix  0100110010111011  ..........  U U U   18  18  11
/*TODO*///	movem     16  er    .     0100110010......  A..DXWL...  U U U   12  12   8
/*TODO*///	movem     32  er    pi    0100110011011...  ..........  U U U   12  12   8
/*TODO*///	movem     32  er    pcdi  0100110011111010  ..........  U U U   20  20   9
/*TODO*///	movem     32  er    pcix  0100110011111011  ..........  U U U   22  22  11
/*TODO*///	movem     32  er    .     0100110011......  A..DXWL...  U U U   12  12   8
/*TODO*///	movep     16  er    .     0000...100001...  ..........  U U U   16  16  12
/*TODO*///	movep     32  er    .     0000...101001...  ..........  U U U   24  24  18
/*TODO*///	movep     16  re    .     0000...110001...  ..........  U U U   16  16  11
/*TODO*///	movep     32  re    .     0000...111001...  ..........  U U U   24  24  17
/*TODO*///	moveq     32  .     .     0111...0........  ..........  U U U    4   4   2
/*TODO*///	moves      8  .     .     0000111000......  A+-DXWL...  . S S    .  14   5
/*TODO*///	moves     16  .     .     0000111001......  A+-DXWL...  . S S    .  14   5
/*TODO*///	moves     32  .     .     0000111010......  A+-DXWL...  . S S    .  16   5
/*TODO*///	muls      16  .     d     1100...111000...  ..........  U U U   54  32  27
/*TODO*///	muls      16  .     .     1100...111......  A+-DXWLdxI  U U U   54  32  27
/*TODO*///	mulu      16  .     d     1100...011000...  ..........  U U U   54  30  27
/*TODO*///	mulu      16  .     .     1100...011......  A+-DXWLdxI  U U U   54  30  27
/*TODO*///	mull      32  .     d     0100110000000...  ..........  . . U    .   .  43
/*TODO*///	mull      32  .     .     0100110000......  A+-DXWLdxI  . . U    .   .  43
/*TODO*///	nbcd       8  .     d     0100100000000...  ..........  U U U    6   6   6
/*TODO*///	nbcd       8  .     .     0100100000......  A+-DXWL...  U U U    8   8   6
/*TODO*///	neg        8  .     d     0100010000000...  ..........  U U U    4   4   2
/*TODO*///	neg        8  .     .     0100010000......  A+-DXWL...  U U U    8   8   4
/*TODO*///	neg       16  .     d     0100010001000...  ..........  U U U    4   4   2
/*TODO*///	neg       16  .     .     0100010001......  A+-DXWL...  U U U    8   8   4
/*TODO*///	neg       32  .     d     0100010010000...  ..........  U U U    6   6   2
/*TODO*///	neg       32  .     .     0100010010......  A+-DXWL...  U U U   12  12   4
/*TODO*///	negx       8  .     d     0100000000000...  ..........  U U U    4   4   2
/*TODO*///	negx       8  .     .     0100000000......  A+-DXWL...  U U U    8   8   4
/*TODO*///	negx      16  .     d     0100000001000...  ..........  U U U    4   4   2
/*TODO*///	negx      16  .     .     0100000001......  A+-DXWL...  U U U    8   8   4
/*TODO*///	negx      32  .     d     0100000010000...  ..........  U U U    6   6   2
/*TODO*///	negx      32  .     .     0100000010......  A+-DXWL...  U U U   12  12   4
/*TODO*///	nop        0  .     .     0100111001110001  ..........  U U U    4   4   2
/*TODO*///	not        8  .     d     0100011000000...  ..........  U U U    4   4   2
/*TODO*///	not        8  .     .     0100011000......  A+-DXWL...  U U U    8   8   4
/*TODO*///	not       16  .     d     0100011001000...  ..........  U U U    4   4   2
/*TODO*///	not       16  .     .     0100011001......  A+-DXWL...  U U U    8   8   4
/*TODO*///	not       32  .     d     0100011010000...  ..........  U U U    6   6   2
/*TODO*///	not       32  .     .     0100011010......  A+-DXWL...  U U U   12  12   4
/*TODO*///	or         8  er    d     1000...000000...  ..........  U U U    4   4   2
/*TODO*///	or         8  er    .     1000...000......  A+-DXWLdxI  U U U    4   4   2
/*TODO*///	or        16  er    d     1000...001000...  ..........  U U U    4   4   2
/*TODO*///	or        16  er    .     1000...001......  A+-DXWLdxI  U U U    4   4   2
/*TODO*///	or        32  er    d     1000...010000...  ..........  U U U    6   6   2
/*TODO*///	or        32  er    .     1000...010......  A+-DXWLdxI  U U U    6   6   2
/*TODO*///	or         8  re    .     1000...100......  A+-DXWL...  U U U    8   8   4
/*TODO*///	or        16  re    .     1000...101......  A+-DXWL...  U U U    8   8   4
/*TODO*///	or        32  re    .     1000...110......  A+-DXWL...  U U U   12  12   4
/*TODO*///	ori       16  toc   .     0000000000111100  ..........  U U U   20  16  12
/*TODO*///	ori       16  tos   .     0000000001111100  ..........  S S S   20  16  12
/*TODO*///	ori        8  .     d     0000000000000...  ..........  U U U    8   8   2
/*TODO*///	ori        8  .     .     0000000000......  A+-DXWL...  U U U   12  12   4
/*TODO*///	ori       16  .     d     0000000001000...  ..........  U U U    8   8   2
/*TODO*///	ori       16  .     .     0000000001......  A+-DXWL...  U U U   12  12   4
/*TODO*///	ori       32  .     d     0000000010000...  ..........  U U U   16  14   2
/*TODO*///	ori       32  .     .     0000000010......  A+-DXWL...  U U U   20  20   4
/*TODO*///	pack      16  rr    .     1000...101000...  ..........  . . U    .   .   6
/*TODO*///	pack      16  mm    ax7   1000111101001...  ..........  . . U    .   .  13
/*TODO*///	pack      16  mm    ay7   1000...101001111  ..........  . . U    .   .  13
/*TODO*///	pack      16  mm    axy7  1000111101001111  ..........  . . U    .   .  13
/*TODO*///	pack      16  mm    .     1000...101001...  ..........  . . U    .   .  13
/*TODO*///	pea       32  .     .     0100100001......  A..DXWLdx.  U U U    6   6   5
/*TODO*///	reset      0  .     .     0100111001110000  ..........  S S S    0   0   0
/*TODO*///	ror        8  s     .     1110...000011...  ..........  U U U    6   6   8
/*TODO*///	ror       16  s     .     1110...001011...  ..........  U U U    6   6   8
/*TODO*///	ror       32  s     .     1110...010011...  ..........  U U U    8   8   8
/*TODO*///	ror        8  r     .     1110...000111...  ..........  U U U    6   6   8
/*TODO*///	ror       16  r     .     1110...001111...  ..........  U U U    6   6   8
/*TODO*///	ror       32  r     .     1110...010111...  ..........  U U U    8   8   8
/*TODO*///	ror       16  .     .     1110011011......  A+-DXWL...  U U U    8   8   7
/*TODO*///	rol        8  s     .     1110...100011...  ..........  U U U    6   6   8
/*TODO*///	rol       16  s     .     1110...101011...  ..........  U U U    6   6   8
/*TODO*///	rol       32  s     .     1110...110011...  ..........  U U U    8   8   8
/*TODO*///	rol        8  r     .     1110...100111...  ..........  U U U    6   6   8
/*TODO*///	rol       16  r     .     1110...101111...  ..........  U U U    6   6   8
/*TODO*///	rol       32  r     .     1110...110111...  ..........  U U U    8   8   8
/*TODO*///	rol       16  .     .     1110011111......  A+-DXWL...  U U U    8   8   7
/*TODO*///	roxr       8  s     .     1110...000010...  ..........  U U U    6   6  12
/*TODO*///	roxr      16  s     .     1110...001010...  ..........  U U U    6   6  12
/*TODO*///	roxr      32  s     .     1110...010010...  ..........  U U U    8   8  12
/*TODO*///	roxr       8  r     .     1110...000110...  ..........  U U U    6   6  12
/*TODO*///	roxr      16  r     .     1110...001110...  ..........  U U U    6   6  12
/*TODO*///	roxr      32  r     .     1110...010110...  ..........  U U U    8   8  12
/*TODO*///	roxr      16  .     .     1110010011......  A+-DXWL...  U U U    8   8   5
/*TODO*///	roxl       8  s     .     1110...100010...  ..........  U U U    6   6  12
/*TODO*///	roxl      16  s     .     1110...101010...  ..........  U U U    6   6  12
/*TODO*///	roxl      32  s     .     1110...110010...  ..........  U U U    8   8  12
/*TODO*///	roxl       8  r     .     1110...100110...  ..........  U U U    6   6  12
/*TODO*///	roxl      16  r     .     1110...101110...  ..........  U U U    6   6  12
/*TODO*///	roxl      32  r     .     1110...110110...  ..........  U U U    8   8  12
/*TODO*///	roxl      16  .     .     1110010111......  A+-DXWL...  U U U    8   8   5
/*TODO*///	rtd       32  .     .     0100111001110100  ..........  . U U    .  16  10
/*TODO*///	rte       32  .     .     0100111001110011  ..........  S S S   20  24  20  bus fault not emulated
/*TODO*///	rtm       32  .     .     000001101100....  ..........  . . U    .   .  19  not properly emulated
/*TODO*///	rtr       32  .     .     0100111001110111  ..........  U U U   20  20  14
/*TODO*///	rts       32  .     .     0100111001110101  ..........  U U U   16  16  10
/*TODO*///	sbcd       8  rr    .     1000...100000...  ..........  U U U    6   6   4
/*TODO*///	sbcd       8  mm    ax7   1000111100001...  ..........  U U U   18  18  16
/*TODO*///	sbcd       8  mm    ay7   1000...100001111  ..........  U U U   18  18  16
/*TODO*///	sbcd       8  mm    axy7  1000111100001111  ..........  U U U   18  18  16
/*TODO*///	sbcd       8  mm    .     1000...100001...  ..........  U U U   18  18  16
/*TODO*///	st         8  .     d     0101000011000...  ..........  U U U    6   4   4
/*TODO*///	st         8  .     .     0101000011......  A+-DXWL...  U U U    8   8   6
/*TODO*///	sf         8  .     d     0101000111000...  ..........  U U U    4   4   4
/*TODO*///	sf         8  .     .     0101000111......  A+-DXWL...  U U U    8   8   6
/*TODO*///	scc        8  .     d     0101....11000...  ..........  U U U    4   4   4
/*TODO*///	scc        8  .     .     0101....11......  A+-DXWL...  U U U    8   8   6
/*TODO*///	stop       0  .     .     0100111001110010  ..........  S S S    4   4   8
/*TODO*///	sub        8  er    d     1001...000000...  ..........  U U U    4   4   2
/*TODO*///	sub        8  er    .     1001...000......  A+-DXWLdxI  U U U    4   4   2
/*TODO*///	sub       16  er    d     1001...001000...  ..........  U U U    4   4   2
/*TODO*///	sub       16  er    a     1001...001001...  ..........  U U U    4   4   2
/*TODO*///	sub       16  er    .     1001...001......  A+-DXWLdxI  U U U    4   4   2
/*TODO*///	sub       32  er    d     1001...010000...  ..........  U U U    6   6   2
/*TODO*///	sub       32  er    a     1001...010001...  ..........  U U U    6   6   2
/*TODO*///	sub       32  er    .     1001...010......  A+-DXWLdxI  U U U    6   6   2
/*TODO*///	sub        8  re    .     1001...100......  A+-DXWL...  U U U    8   8   4
/*TODO*///	sub       16  re    .     1001...101......  A+-DXWL...  U U U    8   8   4
/*TODO*///	sub       32  re    .     1001...110......  A+-DXWL...  U U U   12  12   4
/*TODO*///	suba      16  .     d     1001...011000...  ..........  U U U    8   8   2
/*TODO*///	suba      16  .     a     1001...011001...  ..........  U U U    8   8   2
/*TODO*///	suba      16  .     .     1001...011......  A+-DXWLdxI  U U U    8   8   2
/*TODO*///	suba      32  .     d     1001...111000...  ..........  U U U    6   6   2
/*TODO*///	suba      32  .     a     1001...111001...  ..........  U U U    6   6   2
/*TODO*///	suba      32  .     .     1001...111......  A+-DXWLdxI  U U U    6   6   2
/*TODO*///	subi       8  .     d     0000010000000...  ..........  U U U    8   8   2
/*TODO*///	subi       8  .     .     0000010000......  A+-DXWL...  U U U   12  12   4
/*TODO*///	subi      16  .     d     0000010001000...  ..........  U U U    8   8   2
/*TODO*///	subi      16  .     .     0000010001......  A+-DXWL...  U U U   12  12   4
/*TODO*///	subi      32  .     d     0000010010000...  ..........  U U U   16  14   2
/*TODO*///	subi      32  .     .     0000010010......  A+-DXWL...  U U U   20  20   4
/*TODO*///	subq       8  .     d     0101...100000...  ..........  U U U    4   4   2
/*TODO*///	subq       8  .     .     0101...100......  A+-DXWL...  U U U    8   8   4
/*TODO*///	subq      16  .     d     0101...101000...  ..........  U U U    4   4   2
/*TODO*///	subq      16  .     a     0101...101001...  ..........  U U U    8   4   2
/*TODO*///	subq      16  .     .     0101...101......  A+-DXWL...  U U U    8   8   4
/*TODO*///	subq      32  .     d     0101...110000...  ..........  U U U    8   8   2
/*TODO*///	subq      32  .     a     0101...110001...  ..........  U U U    8   8   2
/*TODO*///	subq      32  .     .     0101...110......  A+-DXWL...  U U U   12  12   4
/*TODO*///	subx       8  rr    .     1001...100000...  ..........  U U U    4   4   2
/*TODO*///	subx      16  rr    .     1001...101000...  ..........  U U U    4   4   2
/*TODO*///	subx      32  rr    .     1001...110000...  ..........  U U U    8   6   2
/*TODO*///	subx       8  mm    ax7   1001111100001...  ..........  U U U   18  18  12
/*TODO*///	subx       8  mm    ay7   1001...100001111  ..........  U U U   18  18  12
/*TODO*///	subx       8  mm    axy7  1001111100001111  ..........  U U U   18  18  12
/*TODO*///	subx       8  mm    .     1001...100001...  ..........  U U U   18  18  12
/*TODO*///	subx      16  mm    .     1001...101001...  ..........  U U U   18  18  12
/*TODO*///	subx      32  mm    .     1001...110001...  ..........  U U U   30  30  12
/*TODO*///	swap      32  .     .     0100100001000...  ..........  U U U    4   4   4
/*TODO*///	tas        8  .     d     0100101011000...  ..........  U U U    4   4   4
/*TODO*///	tas        8  .     .     0100101011......  A+-DXWL...  U U U   14  14  12
/*TODO*///	trap       0  .     .     010011100100....  ..........  U U U    4   4   4
/*TODO*///	trapt      0  .     .     0101000011111100  ..........  . . U    .   .   4
/*TODO*///	trapt     16  .     .     0101000011111010  ..........  . . U    .   .   6
/*TODO*///	trapt     32  .     .     0101000011111011  ..........  . . U    .   .   8
/*TODO*///	trapf      0  .     .     0101000111111100  ..........  . . U    .   .   4
/*TODO*///	trapf     16  .     .     0101000111111010  ..........  . . U    .   .   6
/*TODO*///	trapf     32  .     .     0101000111111011  ..........  . . U    .   .   8
/*TODO*///	trapcc     0  .     .     0101....11111100  ..........  . . U    .   .   4
/*TODO*///	trapcc    16  .     .     0101....11111010  ..........  . . U    .   .   6
/*TODO*///	trapcc    32  .     .     0101....11111011  ..........  . . U    .   .   8
/*TODO*///	trapv      0  .     .     0100111001110110  ..........  U U U    4   4   4
/*TODO*///	tst        8  .     d     0100101000000...  ..........  U U U    4   4   2
/*TODO*///	tst        8  .     .     0100101000......  A+-DXWL...  U U U    4   4   2
/*TODO*///	tst        8  .     pcdi  0100101000111010  ..........  . . U    .   .   7
/*TODO*///	tst        8  .     pcix  0100101000111011  ..........  . . U    .   .   9
/*TODO*///	tst        8  .     i     0100101000111100  ..........  . . U    .   .   6
/*TODO*///	tst       16  .     d     0100101001000...  ..........  U U U    4   4   2
/*TODO*///	tst       16  .     a     0100101001001...  ..........  . . U    .   .   2
/*TODO*///	tst       16  .     .     0100101001......  A+-DXWL...  U U U    4   4   2
/*TODO*///	tst       16  .     pcdi  0100101001111010  ..........  . . U    .   .   7
/*TODO*///	tst       16  .     pcix  0100101001111011  ..........  . . U    .   .   9
/*TODO*///	tst       16  .     i     0100101001111100  ..........  . . U    .   .   6
/*TODO*///	tst       32  .     d     0100101010000...  ..........  U U U    4   4   2
/*TODO*///	tst       32  .     a     0100101010001...  ..........  . . U    .   .   2
/*TODO*///	tst       32  .     .     0100101010......  A+-DXWL...  U U U    4   4   2
/*TODO*///	tst       32  .     pcdi  0100101010111010  ..........  . . U    .   .   7
/*TODO*///	tst       32  .     pcix  0100101010111011  ..........  . . U    .   .   9
/*TODO*///	tst       32  .     i     0100101010111100  ..........  . . U    .   .   6
/*TODO*///	unlk      32  .     a7    0100111001011111  ..........  U U U   12  12   6
/*TODO*///	unlk      32  .     .     0100111001011...  ..........  U U U   12  12   6
/*TODO*///	unpk      16  rr    .     1000...110000...  ..........  . . U    .   .   8
/*TODO*///	unpk      16  mm    ax7   1000111110001...  ..........  . . U    .   .  13
/*TODO*///	unpk      16  mm    ay7   1000...110001111  ..........  . . U    .   .  13
/*TODO*///	unpk      16  mm    axy7  1000111110001111  ..........  . . U    .   .  13
/*TODO*///	unpk      16  mm    .     1000...110001...  ..........  . . U    .   .  13
/*TODO*///	
/*TODO*///	
        };
/*TODO*///	
/*TODO*///	XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
/*TODO*///	M68KMAKE_OPCODE_HANDLER_BODY
/*TODO*///	
/*TODO*///	M68KMAKE_OP(1010, 0, ., .)
/*TODO*///	{
/*TODO*///		m68ki_exception_1010();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(1111, 0, ., .)
/*TODO*///	{
/*TODO*///		m68ki_exception_1111();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(abcd, 8, rr, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = DY;
/*TODO*///		uint dst = *r_dst;
/*TODO*///		uint res = LOW_NIBBLE(src) + LOW_NIBBLE(dst) + XFLAG_AS_1();
/*TODO*///	
/*TODO*///		if(res > 9)
/*TODO*///			res += 6;
/*TODO*///		res += HIGH_NIBBLE(src) + HIGH_NIBBLE(dst);
/*TODO*///		FLAG_X = FLAG_C = (res > 0x99) << 8;
/*TODO*///		if (FLAG_C != 0)
/*TODO*///			res -= 0xa0;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res); /* officially undefined */
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(abcd, 8, mm, ax7)
/*TODO*///	{
/*TODO*///		uint src = OPER_AY_PD_8();
/*TODO*///		uint ea  = EA_A7_PD_8();
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = LOW_NIBBLE(src) + LOW_NIBBLE(dst) + XFLAG_AS_1();
/*TODO*///	
/*TODO*///		if(res > 9)
/*TODO*///			res += 6;
/*TODO*///		res += HIGH_NIBBLE(src) + HIGH_NIBBLE(dst);
/*TODO*///		FLAG_X = FLAG_C = (res > 0x99) << 8;
/*TODO*///		if (FLAG_C != 0)
/*TODO*///			res -= 0xa0;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res); /* officially undefined */
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(abcd, 8, mm, ay7)
/*TODO*///	{
/*TODO*///		uint src = OPER_A7_PD_8();
/*TODO*///		uint ea  = EA_AX_PD_8();
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = LOW_NIBBLE(src) + LOW_NIBBLE(dst) + XFLAG_AS_1();
/*TODO*///	
/*TODO*///		if(res > 9)
/*TODO*///			res += 6;
/*TODO*///		res += HIGH_NIBBLE(src) + HIGH_NIBBLE(dst);
/*TODO*///		FLAG_X = FLAG_C = (res > 0x99) << 8;
/*TODO*///		if (FLAG_C != 0)
/*TODO*///			res -= 0xa0;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res); /* officially undefined */
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(abcd, 8, mm, axy7)
/*TODO*///	{
/*TODO*///		uint src = OPER_A7_PD_8();
/*TODO*///		uint ea  = EA_A7_PD_8();
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = LOW_NIBBLE(src) + LOW_NIBBLE(dst) + XFLAG_AS_1();
/*TODO*///	
/*TODO*///		if(res > 9)
/*TODO*///			res += 6;
/*TODO*///		res += HIGH_NIBBLE(src) + HIGH_NIBBLE(dst);
/*TODO*///		FLAG_X = FLAG_C = (res > 0x99) << 8;
/*TODO*///		if (FLAG_C != 0)
/*TODO*///			res -= 0xa0;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res); /* officially undefined */
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(abcd, 8, mm, .)
/*TODO*///	{
/*TODO*///		uint src = OPER_AY_PD_8();
/*TODO*///		uint ea  = EA_AX_PD_8();
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = LOW_NIBBLE(src) + LOW_NIBBLE(dst) + XFLAG_AS_1();
/*TODO*///	
/*TODO*///		if(res > 9)
/*TODO*///			res += 6;
/*TODO*///		res += HIGH_NIBBLE(src) + HIGH_NIBBLE(dst);
/*TODO*///		FLAG_X = FLAG_C = (res > 0x99) << 8;
/*TODO*///		if (FLAG_C != 0)
/*TODO*///			res -= 0xa0;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res); /* officially undefined */
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(add, 8, er, d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(DY);
/*TODO*///		uint dst = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(add, 8, er, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = M68KMAKE_GET_OPER_AY_8;
/*TODO*///		uint dst = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(add, 16, er, d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(DY);
/*TODO*///		uint dst = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(add, 16, er, a)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(AY);
/*TODO*///		uint dst = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(add, 16, er, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = M68KMAKE_GET_OPER_AY_16;
/*TODO*///		uint dst = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(add, 32, er, d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = DY;
/*TODO*///		uint dst = *r_dst;
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///	
/*TODO*///		*r_dst = FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(add, 32, er, a)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = AY;
/*TODO*///		uint dst = *r_dst;
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///	
/*TODO*///		*r_dst = FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(add, 32, er, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = M68KMAKE_GET_OPER_AY_32;
/*TODO*///		uint dst = *r_dst;
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///	
/*TODO*///		*r_dst = FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(add, 8, re, .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(DX);
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(add, 16, re, .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(DX);
/*TODO*///		uint dst = m68ki_read_16(ea);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(add, 32, re, .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///		uint src = DX;
/*TODO*///		uint dst = m68ki_read_32(ea);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(adda, 16, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_ABOVE_32(*r_dst + MAKE_INT_16(DY));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(adda, 16, ., a)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_ABOVE_32(*r_dst + MAKE_INT_16(AY));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(adda, 16, ., .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_ABOVE_32(*r_dst + MAKE_INT_16(M68KMAKE_GET_OPER_AY_16));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(adda, 32, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_ABOVE_32(*r_dst + DY);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(adda, 32, ., a)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_ABOVE_32(*r_dst + AY);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(adda, 32, ., .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_ABOVE_32(*r_dst + M68KMAKE_GET_OPER_AY_32);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addi, 8, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint src = OPER_I_8();
/*TODO*///		uint dst = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addi, 8, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_8();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addi, 16, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint src = OPER_I_16();
/*TODO*///		uint dst = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addi, 16, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_16();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint dst = m68ki_read_16(ea);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addi, 32, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint src = OPER_I_32();
/*TODO*///		uint dst = *r_dst;
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///	
/*TODO*///		*r_dst = FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addi, 32, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_32();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///		uint dst = m68ki_read_32(ea);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addq, 8, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint src = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint dst = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addq, 8, ., .)
/*TODO*///	{
/*TODO*///		uint src = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addq, 16, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint src = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint dst = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addq, 16, ., a)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AY;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_ABOVE_32(*r_dst + (((REG_IR >> 9) - 1) & 7) + 1);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addq, 16, ., .)
/*TODO*///	{
/*TODO*///		uint src = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint dst = m68ki_read_16(ea);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addq, 32, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint src = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint dst = *r_dst;
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///	
/*TODO*///		*r_dst = FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addq, 32, ., a)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AY;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_ABOVE_32(*r_dst + (((REG_IR >> 9) - 1) & 7) + 1);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addq, 32, ., .)
/*TODO*///	{
/*TODO*///		uint src = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///		uint dst = m68ki_read_32(ea);
/*TODO*///		uint res = src + dst;
/*TODO*///	
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addx, 8, rr, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(DY);
/*TODO*///		uint dst = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = src + dst + XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addx, 16, rr, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(DY);
/*TODO*///		uint dst = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = src + dst + XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_16(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addx, 32, rr, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = DY;
/*TODO*///		uint dst = *r_dst;
/*TODO*///		uint res = src + dst + XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addx, 8, mm, ax7)
/*TODO*///	{
/*TODO*///		uint src = OPER_AY_PD_8();
/*TODO*///		uint ea  = EA_A7_PD_8();
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = src + dst + XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addx, 8, mm, ay7)
/*TODO*///	{
/*TODO*///		uint src = OPER_A7_PD_8();
/*TODO*///		uint ea  = EA_AX_PD_8();
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = src + dst + XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addx, 8, mm, axy7)
/*TODO*///	{
/*TODO*///		uint src = OPER_A7_PD_8();
/*TODO*///		uint ea  = EA_A7_PD_8();
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = src + dst + XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addx, 8, mm, .)
/*TODO*///	{
/*TODO*///		uint src = OPER_AY_PD_8();
/*TODO*///		uint ea  = EA_AX_PD_8();
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = src + dst + XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addx, 16, mm, .)
/*TODO*///	{
/*TODO*///		uint src = OPER_AY_PD_16();
/*TODO*///		uint ea  = EA_AX_PD_16();
/*TODO*///		uint dst = m68ki_read_16(ea);
/*TODO*///		uint res = src + dst + XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_16(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(addx, 32, mm, .)
/*TODO*///	{
/*TODO*///		uint src = OPER_AY_PD_32();
/*TODO*///		uint ea  = EA_AX_PD_32();
/*TODO*///		uint dst = m68ki_read_32(ea);
/*TODO*///		uint res = src + dst + XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(and, 8, er, d)
/*TODO*///	{
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(DX &= (DY | 0xffffff00));
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(FLAG_Z);
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(and, 8, er, .)
/*TODO*///	{
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(DX &= (M68KMAKE_GET_OPER_AY_8 | 0xffffff00));
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(FLAG_Z);
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(and, 16, er, d)
/*TODO*///	{
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(DX &= (DY | 0xffff0000));
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(FLAG_Z);
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(and, 16, er, .)
/*TODO*///	{
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(DX &= (M68KMAKE_GET_OPER_AY_16 | 0xffff0000));
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(FLAG_Z);
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(and, 32, er, d)
/*TODO*///	{
/*TODO*///		FLAG_Z = DX &= DY;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(FLAG_Z);
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(and, 32, er, .)
/*TODO*///	{
/*TODO*///		FLAG_Z = DX &= M68KMAKE_GET_OPER_AY_32;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(FLAG_Z);
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(and, 8, re, .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint res = DX & m68ki_read_8(ea);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(and, 16, re, .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint res = DX & m68ki_read_16(ea);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(and, 32, re, .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///		uint res = DX & m68ki_read_32(ea);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(andi, 8, ., d)
/*TODO*///	{
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(DY &= (OPER_I_8() | 0xffffff00));
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(FLAG_Z);
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(andi, 8, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_8();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint res = src & m68ki_read_8(ea);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(andi, 16, ., d)
/*TODO*///	{
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(DY &= (OPER_I_16() | 0xffff0000));
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(FLAG_Z);
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(andi, 16, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_16();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint res = src & m68ki_read_16(ea);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(andi, 32, ., d)
/*TODO*///	{
/*TODO*///		FLAG_Z = DY &= (OPER_I_32());
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(FLAG_Z);
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(andi, 32, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_32();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///		uint res = src & m68ki_read_32(ea);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(andi, 16, toc, .)
/*TODO*///	{
/*TODO*///		m68ki_set_ccr(m68ki_get_ccr() & OPER_I_16());
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(andi, 16, tos, .)
/*TODO*///	{
/*TODO*///		if (FLAG_S != 0)
/*TODO*///		{
/*TODO*///			uint src = OPER_I_16();
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_set_sr(m68ki_get_sr() & src);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_privilege_violation();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(asr, 8, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = src >> shift;
/*TODO*///	
/*TODO*///		if(GET_MSB_8(src))
/*TODO*///			res |= m68ki_shift_8_table[shift];
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_X = FLAG_C = src << (9-shift);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(asr, 16, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = src >> shift;
/*TODO*///	
/*TODO*///		if(GET_MSB_16(src))
/*TODO*///			res |= m68ki_shift_16_table[shift];
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_X = FLAG_C = src << (9-shift);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(asr, 32, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = *r_dst;
/*TODO*///		uint res = src >> shift;
/*TODO*///	
/*TODO*///		if(GET_MSB_32(src))
/*TODO*///			res |= m68ki_shift_32_table[shift];
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_X = FLAG_C = src << (9-shift);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(asr, 8, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = DX & 0x3f;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = src >> shift;
/*TODO*///	
/*TODO*///		if(shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			if(shift < 8)
/*TODO*///			{
/*TODO*///				if(GET_MSB_8(src))
/*TODO*///					res |= m68ki_shift_8_table[shift];
/*TODO*///	
/*TODO*///				*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	
/*TODO*///				FLAG_X = FLAG_C = src << (9-shift);
/*TODO*///				FLAG_N = NFLAG_8(res);
/*TODO*///				FLAG_Z = res;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			if(GET_MSB_8(src))
/*TODO*///			{
/*TODO*///				*r_dst |= 0xff;
/*TODO*///				FLAG_C = CFLAG_SET;
/*TODO*///				FLAG_X = XFLAG_SET;
/*TODO*///				FLAG_N = NFLAG_SET;
/*TODO*///				FLAG_Z = ZFLAG_CLEAR;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			*r_dst &= 0xffffff00;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			FLAG_X = XFLAG_CLEAR;
/*TODO*///			FLAG_N = NFLAG_CLEAR;
/*TODO*///			FLAG_Z = ZFLAG_SET;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_8(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(asr, 16, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = DX & 0x3f;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = src >> shift;
/*TODO*///	
/*TODO*///		if(shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			if(shift < 16)
/*TODO*///			{
/*TODO*///				if(GET_MSB_16(src))
/*TODO*///					res |= m68ki_shift_16_table[shift];
/*TODO*///	
/*TODO*///				*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///	
/*TODO*///				FLAG_C = FLAG_X = (src >> (shift - 1))<<8;
/*TODO*///				FLAG_N = NFLAG_16(res);
/*TODO*///				FLAG_Z = res;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			if(GET_MSB_16(src))
/*TODO*///			{
/*TODO*///				*r_dst |= 0xffff;
/*TODO*///				FLAG_C = CFLAG_SET;
/*TODO*///				FLAG_X = XFLAG_SET;
/*TODO*///				FLAG_N = NFLAG_SET;
/*TODO*///				FLAG_Z = ZFLAG_CLEAR;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			*r_dst &= 0xffff0000;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			FLAG_X = XFLAG_CLEAR;
/*TODO*///			FLAG_N = NFLAG_CLEAR;
/*TODO*///			FLAG_Z = ZFLAG_SET;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_16(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(asr, 32, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = DX & 0x3f;
/*TODO*///		uint src = *r_dst;
/*TODO*///		uint res = src >> shift;
/*TODO*///	
/*TODO*///		if(shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			if(shift < 32)
/*TODO*///			{
/*TODO*///				if(GET_MSB_32(src))
/*TODO*///					res |= m68ki_shift_32_table[shift];
/*TODO*///	
/*TODO*///				*r_dst = res;
/*TODO*///	
/*TODO*///				FLAG_C = FLAG_X = (src >> (shift - 1))<<8;
/*TODO*///				FLAG_N = NFLAG_32(res);
/*TODO*///				FLAG_Z = res;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			if(GET_MSB_32(src))
/*TODO*///			{
/*TODO*///				*r_dst = 0xffffffff;
/*TODO*///				FLAG_C = CFLAG_SET;
/*TODO*///				FLAG_X = XFLAG_SET;
/*TODO*///				FLAG_N = NFLAG_SET;
/*TODO*///				FLAG_Z = ZFLAG_CLEAR;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			*r_dst = 0;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			FLAG_X = XFLAG_CLEAR;
/*TODO*///			FLAG_N = NFLAG_CLEAR;
/*TODO*///			FLAG_Z = ZFLAG_SET;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_32(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(asr, 16, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint src = m68ki_read_16(ea);
/*TODO*///		uint res = src >> 1;
/*TODO*///	
/*TODO*///		if(GET_MSB_16(src))
/*TODO*///			res |= 0x8000;
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = FLAG_X = src << 8;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(asl, 8, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = MASK_OUT_ABOVE_8(src << shift);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_X = FLAG_C = src << shift;
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		src &= m68ki_shift_8_table[shift + 1];
/*TODO*///		FLAG_V = (!(src == 0 || (src == m68ki_shift_8_table[shift + 1] && shift < 8)))<<7;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(asl, 16, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = MASK_OUT_ABOVE_16(src << shift);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_X = FLAG_C = src >> (8-shift);
/*TODO*///		src &= m68ki_shift_16_table[shift + 1];
/*TODO*///		FLAG_V = (!(src == 0 || src == m68ki_shift_16_table[shift + 1]))<<7;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(asl, 32, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = *r_dst;
/*TODO*///		uint res = MASK_OUT_ABOVE_32(src << shift);
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_X = FLAG_C = src >> (24-shift);
/*TODO*///		src &= m68ki_shift_32_table[shift + 1];
/*TODO*///		FLAG_V = (!(src == 0 || src == m68ki_shift_32_table[shift + 1]))<<7;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(asl, 8, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = DX & 0x3f;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = MASK_OUT_ABOVE_8(src << shift);
/*TODO*///	
/*TODO*///		if(shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			if(shift < 8)
/*TODO*///			{
/*TODO*///				*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///				FLAG_X = FLAG_C = src << shift;
/*TODO*///				FLAG_N = NFLAG_8(res);
/*TODO*///				FLAG_Z = res;
/*TODO*///				src &= m68ki_shift_8_table[shift + 1];
/*TODO*///				FLAG_V = (!(src == 0 || src == m68ki_shift_8_table[shift + 1]))<<7;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			*r_dst &= 0xffffff00;
/*TODO*///			FLAG_X = FLAG_C = ((shift == 8 ? src & 1 : 0))<<8;
/*TODO*///			FLAG_N = NFLAG_CLEAR;
/*TODO*///			FLAG_Z = ZFLAG_SET;
/*TODO*///			FLAG_V = (!(src == 0))<<7;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_8(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(asl, 16, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = DX & 0x3f;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = MASK_OUT_ABOVE_16(src << shift);
/*TODO*///	
/*TODO*///		if(shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			if(shift < 16)
/*TODO*///			{
/*TODO*///				*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///				FLAG_X = FLAG_C = (src << shift) >> 8;
/*TODO*///				FLAG_N = NFLAG_16(res);
/*TODO*///				FLAG_Z = res;
/*TODO*///				src &= m68ki_shift_16_table[shift + 1];
/*TODO*///				FLAG_V = (!(src == 0 || src == m68ki_shift_16_table[shift + 1]))<<7;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			*r_dst &= 0xffff0000;
/*TODO*///			FLAG_X = FLAG_C = ((shift == 16 ? src & 1 : 0))<<8;
/*TODO*///			FLAG_N = NFLAG_CLEAR;
/*TODO*///			FLAG_Z = ZFLAG_SET;
/*TODO*///			FLAG_V = (!(src == 0))<<7;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_16(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(asl, 32, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = DX & 0x3f;
/*TODO*///		uint src = *r_dst;
/*TODO*///		uint res = MASK_OUT_ABOVE_32(src << shift);
/*TODO*///	
/*TODO*///		if(shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			if(shift < 32)
/*TODO*///			{
/*TODO*///				*r_dst = res;
/*TODO*///				FLAG_X = FLAG_C = (src >> (32 - shift)) << 8;
/*TODO*///				FLAG_N = NFLAG_32(res);
/*TODO*///				FLAG_Z = res;
/*TODO*///				src &= m68ki_shift_32_table[shift + 1];
/*TODO*///				FLAG_V = (!(src == 0 || src == m68ki_shift_32_table[shift + 1]))<<7;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			*r_dst = 0;
/*TODO*///			FLAG_X = FLAG_C = ((shift == 32 ? src & 1 : 0))<<8;
/*TODO*///			FLAG_N = NFLAG_CLEAR;
/*TODO*///			FLAG_Z = ZFLAG_SET;
/*TODO*///			FLAG_V = (!(src == 0))<<7;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_32(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(asl, 16, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint src = m68ki_read_16(ea);
/*TODO*///		uint res = MASK_OUT_ABOVE_16(src << 1);
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_X = FLAG_C = src >> 7;
/*TODO*///		src &= 0xc000;
/*TODO*///		FLAG_V = (!(src == 0 || src == 0xc000))<<7;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bcc, 8, ., .)
/*TODO*///	{
/*TODO*///		if (M68KMAKE_CC != 0)
/*TODO*///		{
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_8(MASK_OUT_ABOVE_8(REG_IR));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		USE_CYCLES(CYC_BCC_NOTAKE_B);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bcc, 16, ., .)
/*TODO*///	{
/*TODO*///		if (M68KMAKE_CC != 0)
/*TODO*///		{
/*TODO*///			uint offset = OPER_I_16();
/*TODO*///			REG_PC -= 2;
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_16(offset);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		REG_PC += 2;
/*TODO*///		USE_CYCLES(CYC_BCC_NOTAKE_W);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bcc, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			if (M68KMAKE_CC != 0)
/*TODO*///			{
/*TODO*///				uint offset = OPER_I_32();
/*TODO*///				REG_PC -= 4;
/*TODO*///				m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///				m68ki_branch_32(offset);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			REG_PC += 4;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bchg, 32, r, d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint mask = 1 << (DX & 0x1f);
/*TODO*///	
/*TODO*///		FLAG_Z = *r_dst & mask;
/*TODO*///		*r_dst /*TODO*///= mask;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bchg, 8, r, .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint src = m68ki_read_8(ea);
/*TODO*///		uint mask = 1 << (DX & 7);
/*TODO*///	
/*TODO*///		FLAG_Z = src & mask;
/*TODO*///		m68ki_write_8(ea, src /*TODO*/// mask);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bchg, 32, s, d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint mask = 1 << (OPER_I_8() & 0x1f);
/*TODO*///	
/*TODO*///		FLAG_Z = *r_dst & mask;
/*TODO*///		*r_dst /*TODO*///= mask;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bchg, 8, s, .)
/*TODO*///	{
/*TODO*///		uint mask = 1 << (OPER_I_8() & 7);
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint src = m68ki_read_8(ea);
/*TODO*///	
/*TODO*///		FLAG_Z = src & mask;
/*TODO*///		m68ki_write_8(ea, src /*TODO*/// mask);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bclr, 32, r, d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint mask = 1 << (DX & 0x1f);
/*TODO*///	
/*TODO*///		FLAG_Z = *r_dst & mask;
/*TODO*///		*r_dst &= ~mask;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bclr, 8, r, .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint src = m68ki_read_8(ea);
/*TODO*///		uint mask = 1 << (DX & 7);
/*TODO*///	
/*TODO*///		FLAG_Z = src & mask;
/*TODO*///		m68ki_write_8(ea, src & ~mask);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bclr, 32, s, d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint mask = 1 << (OPER_I_8() & 0x1f);
/*TODO*///	
/*TODO*///		FLAG_Z = *r_dst & mask;
/*TODO*///		*r_dst &= ~mask;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bclr, 8, s, .)
/*TODO*///	{
/*TODO*///		uint mask = 1 << (OPER_I_8() & 7);
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint src = m68ki_read_8(ea);
/*TODO*///	
/*TODO*///		FLAG_Z = src & mask;
/*TODO*///		m68ki_write_8(ea, src & ~mask);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bfchg, 32, ., d)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint offset = (word2>>6)&31;
/*TODO*///			uint width = word2;
/*TODO*///			uint* data = &DY;
/*TODO*///			uint64 mask;
/*TODO*///	
/*TODO*///	
/*TODO*///			if(BIT_B(word2))
/*TODO*///				offset = REG_D[offset&7];
/*TODO*///			if(BIT_5(word2))
/*TODO*///				width = REG_D[width&7];
/*TODO*///	
/*TODO*///			offset &= 31;
/*TODO*///			width = ((width-1) & 31) + 1;
/*TODO*///	
/*TODO*///			mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///			mask = ROR_32(mask, offset);
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_32(*data<<offset);
/*TODO*///			FLAG_Z = *data & mask;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			*data /*TODO*///= mask;
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bfchg, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			sint offset = (word2>>6)&31;
/*TODO*///			uint width = word2;
/*TODO*///			uint mask_base;
/*TODO*///			uint data_long;
/*TODO*///			uint mask_long;
/*TODO*///			uint data_byte = 0;
/*TODO*///			uint mask_byte = 0;
/*TODO*///			uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///	
/*TODO*///	
/*TODO*///			if(BIT_B(word2))
/*TODO*///				offset = MAKE_INT_32(REG_D[offset&7]);
/*TODO*///			if(BIT_5(word2))
/*TODO*///				width = REG_D[width&7];
/*TODO*///	
/*TODO*///			/* Offset is signed so we have to use ugly math =( */
/*TODO*///			ea += offset / 8;
/*TODO*///			offset %= 8;
/*TODO*///			if(offset < 0)
/*TODO*///			{
/*TODO*///				offset += 8;
/*TODO*///				ea--;
/*TODO*///			}
/*TODO*///			width = ((width-1) & 31) + 1;
/*TODO*///	
/*TODO*///			mask_base = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///			mask_long = mask_base >> offset;
/*TODO*///	
/*TODO*///			data_long = m68ki_read_32(ea);
/*TODO*///			FLAG_N = NFLAG_32(data_long << offset);
/*TODO*///			FLAG_Z = data_long & mask_long;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			m68ki_write_32(ea, data_long /*TODO*/// mask_long);
/*TODO*///	
/*TODO*///			if((width + offset) > 32)
/*TODO*///			{
/*TODO*///				mask_byte = MASK_OUT_ABOVE_8(mask_base);
/*TODO*///				data_byte = m68ki_read_8(ea+4);
/*TODO*///				FLAG_Z |= (data_byte & mask_byte);
/*TODO*///				m68ki_write_8(ea+4, data_byte /*TODO*/// mask_byte);
/*TODO*///			}
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bfclr, 32, ., d)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint offset = (word2>>6)&31;
/*TODO*///			uint width = word2;
/*TODO*///			uint* data = &DY;
/*TODO*///			uint64 mask;
/*TODO*///	
/*TODO*///	
/*TODO*///			if(BIT_B(word2))
/*TODO*///				offset = REG_D[offset&7];
/*TODO*///			if(BIT_5(word2))
/*TODO*///				width = REG_D[width&7];
/*TODO*///	
/*TODO*///	
/*TODO*///			offset &= 31;
/*TODO*///			width = ((width-1) & 31) + 1;
/*TODO*///	
/*TODO*///	
/*TODO*///			mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///			mask = ROR_32(mask, offset);
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_32(*data<<offset);
/*TODO*///			FLAG_Z = *data & mask;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			*data &= ~mask;
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bfclr, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			sint offset = (word2>>6)&31;
/*TODO*///			uint width = word2;
/*TODO*///			uint mask_base;
/*TODO*///			uint data_long;
/*TODO*///			uint mask_long;
/*TODO*///			uint data_byte = 0;
/*TODO*///			uint mask_byte = 0;
/*TODO*///			uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///	
/*TODO*///	
/*TODO*///			if(BIT_B(word2))
/*TODO*///				offset = MAKE_INT_32(REG_D[offset&7]);
/*TODO*///			if(BIT_5(word2))
/*TODO*///				width = REG_D[width&7];
/*TODO*///	
/*TODO*///			/* Offset is signed so we have to use ugly math =( */
/*TODO*///			ea += offset / 8;
/*TODO*///			offset %= 8;
/*TODO*///			if(offset < 0)
/*TODO*///			{
/*TODO*///				offset += 8;
/*TODO*///				ea--;
/*TODO*///			}
/*TODO*///			width = ((width-1) & 31) + 1;
/*TODO*///	
/*TODO*///			mask_base = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///			mask_long = mask_base >> offset;
/*TODO*///	
/*TODO*///			data_long = m68ki_read_32(ea);
/*TODO*///			FLAG_N = NFLAG_32(data_long << offset);
/*TODO*///			FLAG_Z = data_long & mask_long;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			m68ki_write_32(ea, data_long & ~mask_long);
/*TODO*///	
/*TODO*///			if((width + offset) > 32)
/*TODO*///			{
/*TODO*///				mask_byte = MASK_OUT_ABOVE_8(mask_base);
/*TODO*///				data_byte = m68ki_read_8(ea+4);
/*TODO*///				FLAG_Z |= (data_byte & mask_byte);
/*TODO*///				m68ki_write_8(ea+4, data_byte & ~mask_byte);
/*TODO*///			}
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bfexts, 32, ., d)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint offset = (word2>>6)&31;
/*TODO*///			uint width = word2;
/*TODO*///			uint64 data = DY;
/*TODO*///	
/*TODO*///	
/*TODO*///			if(BIT_B(word2))
/*TODO*///				offset = REG_D[offset&7];
/*TODO*///			if(BIT_5(word2))
/*TODO*///				width = REG_D[width&7];
/*TODO*///	
/*TODO*///			offset &= 31;
/*TODO*///			width = ((width-1) & 31) + 1;
/*TODO*///	
/*TODO*///			data = ROL_32(data, offset);
/*TODO*///			FLAG_N = NFLAG_32(data);
/*TODO*///			data = MAKE_INT_32(data) >> (32 - width);
/*TODO*///	
/*TODO*///			FLAG_Z = data;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			REG_D[(word2>>12)&7] = data;
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bfexts, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			sint offset = (word2>>6)&31;
/*TODO*///			uint width = word2;
/*TODO*///			uint data;
/*TODO*///			uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///	
/*TODO*///	
/*TODO*///			if(BIT_B(word2))
/*TODO*///				offset = MAKE_INT_32(REG_D[offset&7]);
/*TODO*///			if(BIT_5(word2))
/*TODO*///				width = REG_D[width&7];
/*TODO*///	
/*TODO*///			/* Offset is signed so we have to use ugly math =( */
/*TODO*///			ea += offset / 8;
/*TODO*///			offset %= 8;
/*TODO*///			if(offset < 0)
/*TODO*///			{
/*TODO*///				offset += 8;
/*TODO*///				ea--;
/*TODO*///			}
/*TODO*///			width = ((width-1) & 31) + 1;
/*TODO*///	
/*TODO*///			data = m68ki_read_32(ea);
/*TODO*///	
/*TODO*///			data = MASK_OUT_ABOVE_32(data<<offset);
/*TODO*///	
/*TODO*///			if((offset+width) > 32)
/*TODO*///				data |= (m68ki_read_8(ea+4) << offset) >> 8;
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_32(data);
/*TODO*///			data  = MAKE_INT_32(data) >> (32 - width);
/*TODO*///	
/*TODO*///			FLAG_Z = data;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			REG_D[(word2 >> 12) & 7] = data;
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bfextu, 32, ., d)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint offset = (word2>>6)&31;
/*TODO*///			uint width = word2;
/*TODO*///			uint64 data = DY;
/*TODO*///	
/*TODO*///	
/*TODO*///			if(BIT_B(word2))
/*TODO*///				offset = REG_D[offset&7];
/*TODO*///			if(BIT_5(word2))
/*TODO*///				width = REG_D[width&7];
/*TODO*///	
/*TODO*///			offset &= 31;
/*TODO*///			width = ((width-1) & 31) + 1;
/*TODO*///	
/*TODO*///			data = ROL_32(data, offset);
/*TODO*///			FLAG_N = NFLAG_32(data);
/*TODO*///			data >>= 32 - width;
/*TODO*///	
/*TODO*///			FLAG_Z = data;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			REG_D[(word2>>12)&7] = data;
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bfextu, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			sint offset = (word2>>6)&31;
/*TODO*///			uint width = word2;
/*TODO*///			uint data;
/*TODO*///			uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///	
/*TODO*///	
/*TODO*///			if(BIT_B(word2))
/*TODO*///			offset = MAKE_INT_32(REG_D[offset&7]);
/*TODO*///			if(BIT_5(word2))
/*TODO*///				width = REG_D[width&7];
/*TODO*///	
/*TODO*///			/* Offset is signed so we have to use ugly math =( */
/*TODO*///			ea += offset / 8;
/*TODO*///			offset %= 8;
/*TODO*///			if(offset < 0)
/*TODO*///			{
/*TODO*///				offset += 8;
/*TODO*///				ea--;
/*TODO*///			}
/*TODO*///			width = ((width-1) & 31) + 1;
/*TODO*///	
/*TODO*///			data = m68ki_read_32(ea);
/*TODO*///			data = MASK_OUT_ABOVE_32(data<<offset);
/*TODO*///	
/*TODO*///			if((offset+width) > 32)
/*TODO*///				data |= (m68ki_read_8(ea+4) << offset) >> 8;
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_32(data);
/*TODO*///			data  >>= (32 - width);
/*TODO*///	
/*TODO*///			FLAG_Z = data;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			REG_D[(word2 >> 12) & 7] = data;
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bfffo, 32, ., d)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint offset = (word2>>6)&31;
/*TODO*///			uint width = word2;
/*TODO*///			uint64 data = DY;
/*TODO*///			uint bit;
/*TODO*///	
/*TODO*///	
/*TODO*///			if(BIT_B(word2))
/*TODO*///				offset = REG_D[offset&7];
/*TODO*///			if(BIT_5(word2))
/*TODO*///				width = REG_D[width&7];
/*TODO*///	
/*TODO*///			offset &= 31;
/*TODO*///			width = ((width-1) & 31) + 1;
/*TODO*///	
/*TODO*///			data = ROL_32(data, offset);
/*TODO*///			FLAG_N = NFLAG_32(data);
/*TODO*///			data >>= 32 - width;
/*TODO*///	
/*TODO*///			FLAG_Z = data;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			for(bit = 1<<(width-1);bit && !(data & bit);bit>>= 1)
/*TODO*///				offset++;
/*TODO*///	
/*TODO*///			REG_D[(word2>>12)&7] = offset;
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bfffo, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			sint offset = (word2>>6)&31;
/*TODO*///			sint local_offset;
/*TODO*///			uint width = word2;
/*TODO*///			uint data;
/*TODO*///			uint bit;
/*TODO*///			uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///	
/*TODO*///	
/*TODO*///			if(BIT_B(word2))
/*TODO*///				offset = MAKE_INT_32(REG_D[offset&7]);
/*TODO*///			if(BIT_5(word2))
/*TODO*///				width = REG_D[width&7];
/*TODO*///	
/*TODO*///			/* Offset is signed so we have to use ugly math =( */
/*TODO*///			ea += offset / 8;
/*TODO*///			local_offset = offset % 8;
/*TODO*///			if(local_offset < 0)
/*TODO*///			{
/*TODO*///				local_offset += 8;
/*TODO*///				ea--;
/*TODO*///			}
/*TODO*///			width = ((width-1) & 31) + 1;
/*TODO*///	
/*TODO*///			data = m68ki_read_32(ea);
/*TODO*///			data = MASK_OUT_ABOVE_32(data<<local_offset);
/*TODO*///	
/*TODO*///			if((local_offset+width) > 32)
/*TODO*///				data |= (m68ki_read_8(ea+4) << local_offset) >> 8;
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_32(data);
/*TODO*///			data  >>= (32 - width);
/*TODO*///	
/*TODO*///			FLAG_Z = data;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			for(bit = 1<<(width-1);bit && !(data & bit);bit>>= 1)
/*TODO*///				offset++;
/*TODO*///	
/*TODO*///			REG_D[(word2>>12)&7] = offset;
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bfins, 32, ., d)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint offset = (word2>>6)&31;
/*TODO*///			uint width = word2;
/*TODO*///			uint* data = &DY;
/*TODO*///			uint64 mask;
/*TODO*///			uint64 insert = REG_D[(word2>>12)&7];
/*TODO*///	
/*TODO*///	
/*TODO*///			if(BIT_B(word2))
/*TODO*///				offset = REG_D[offset&7];
/*TODO*///			if(BIT_5(word2))
/*TODO*///				width = REG_D[width&7];
/*TODO*///	
/*TODO*///	
/*TODO*///			offset &= 31;
/*TODO*///			width = ((width-1) & 31) + 1;
/*TODO*///	
/*TODO*///	
/*TODO*///			mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///			mask = ROR_32(mask, offset);
/*TODO*///	
/*TODO*///			insert = MASK_OUT_ABOVE_32(insert << (32 - width));
/*TODO*///			FLAG_N = NFLAG_32(insert);
/*TODO*///			FLAG_Z = insert;
/*TODO*///			insert = ROR_32(insert, offset);
/*TODO*///	
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			*data &= ~mask;
/*TODO*///			*data |= insert;
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bfins, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			sint offset = (word2>>6)&31;
/*TODO*///			uint width = word2;
/*TODO*///			uint insert_base = REG_D[(word2>>12)&7];
/*TODO*///			uint insert_long;
/*TODO*///			uint insert_byte;
/*TODO*///			uint mask_base;
/*TODO*///			uint data_long;
/*TODO*///			uint mask_long;
/*TODO*///			uint data_byte = 0;
/*TODO*///			uint mask_byte = 0;
/*TODO*///			uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///	
/*TODO*///	
/*TODO*///			if(BIT_B(word2))
/*TODO*///				offset = MAKE_INT_32(REG_D[offset&7]);
/*TODO*///			if(BIT_5(word2))
/*TODO*///				width = REG_D[width&7];
/*TODO*///	
/*TODO*///			/* Offset is signed so we have to use ugly math =( */
/*TODO*///			ea += offset / 8;
/*TODO*///			offset %= 8;
/*TODO*///			if(offset < 0)
/*TODO*///			{
/*TODO*///				offset += 8;
/*TODO*///				ea--;
/*TODO*///			}
/*TODO*///			width = ((width-1) & 31) + 1;
/*TODO*///	
/*TODO*///			mask_base = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///			mask_long = mask_base >> offset;
/*TODO*///	
/*TODO*///			insert_base = MASK_OUT_ABOVE_32(insert_base << (32 - width));
/*TODO*///			FLAG_N = NFLAG_32(insert_base);
/*TODO*///			FLAG_Z = insert_base;
/*TODO*///			insert_long = insert_base >> offset;
/*TODO*///	
/*TODO*///			data_long = m68ki_read_32(ea);
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			m68ki_write_32(ea, (data_long & ~mask_long) | insert_long);
/*TODO*///	
/*TODO*///			if((width + offset) > 32)
/*TODO*///			{
/*TODO*///				mask_byte = MASK_OUT_ABOVE_8(mask_base);
/*TODO*///				insert_byte = MASK_OUT_ABOVE_8(insert_base);
/*TODO*///				data_byte = m68ki_read_8(ea+4);
/*TODO*///				FLAG_Z |= (data_byte & mask_byte);
/*TODO*///				m68ki_write_8(ea+4, (data_byte & ~mask_byte) | insert_byte);
/*TODO*///			}
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bfset, 32, ., d)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint offset = (word2>>6)&31;
/*TODO*///			uint width = word2;
/*TODO*///			uint* data = &DY;
/*TODO*///			uint64 mask;
/*TODO*///	
/*TODO*///	
/*TODO*///			if(BIT_B(word2))
/*TODO*///				offset = REG_D[offset&7];
/*TODO*///			if(BIT_5(word2))
/*TODO*///				width = REG_D[width&7];
/*TODO*///	
/*TODO*///	
/*TODO*///			offset &= 31;
/*TODO*///			width = ((width-1) & 31) + 1;
/*TODO*///	
/*TODO*///	
/*TODO*///			mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///			mask = ROR_32(mask, offset);
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_32(*data<<offset);
/*TODO*///			FLAG_Z = *data & mask;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			*data |= mask;
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bfset, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			sint offset = (word2>>6)&31;
/*TODO*///			uint width = word2;
/*TODO*///			uint mask_base;
/*TODO*///			uint data_long;
/*TODO*///			uint mask_long;
/*TODO*///			uint data_byte = 0;
/*TODO*///			uint mask_byte = 0;
/*TODO*///			uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///	
/*TODO*///	
/*TODO*///			if(BIT_B(word2))
/*TODO*///				offset = MAKE_INT_32(REG_D[offset&7]);
/*TODO*///			if(BIT_5(word2))
/*TODO*///				width = REG_D[width&7];
/*TODO*///	
/*TODO*///			/* Offset is signed so we have to use ugly math =( */
/*TODO*///			ea += offset / 8;
/*TODO*///			offset %= 8;
/*TODO*///			if(offset < 0)
/*TODO*///			{
/*TODO*///				offset += 8;
/*TODO*///				ea--;
/*TODO*///			}
/*TODO*///			width = ((width-1) & 31) + 1;
/*TODO*///	
/*TODO*///	
/*TODO*///			mask_base = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///			mask_long = mask_base >> offset;
/*TODO*///	
/*TODO*///			data_long = m68ki_read_32(ea);
/*TODO*///			FLAG_N = NFLAG_32(data_long << offset);
/*TODO*///			FLAG_Z = data_long & mask_long;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			m68ki_write_32(ea, data_long | mask_long);
/*TODO*///	
/*TODO*///			if((width + offset) > 32)
/*TODO*///			{
/*TODO*///				mask_byte = MASK_OUT_ABOVE_8(mask_base);
/*TODO*///				data_byte = m68ki_read_8(ea+4);
/*TODO*///				FLAG_Z |= (data_byte & mask_byte);
/*TODO*///				m68ki_write_8(ea+4, data_byte | mask_byte);
/*TODO*///			}
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bftst, 32, ., d)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint offset = (word2>>6)&31;
/*TODO*///			uint width = word2;
/*TODO*///			uint* data = &DY;
/*TODO*///			uint64 mask;
/*TODO*///	
/*TODO*///	
/*TODO*///			if(BIT_B(word2))
/*TODO*///				offset = REG_D[offset&7];
/*TODO*///			if(BIT_5(word2))
/*TODO*///				width = REG_D[width&7];
/*TODO*///	
/*TODO*///	
/*TODO*///			offset &= 31;
/*TODO*///			width = ((width-1) & 31) + 1;
/*TODO*///	
/*TODO*///	
/*TODO*///			mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///			mask = ROR_32(mask, offset);
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_32(*data<<offset);
/*TODO*///			FLAG_Z = *data & mask;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bftst, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			sint offset = (word2>>6)&31;
/*TODO*///			uint width = word2;
/*TODO*///			uint mask_base;
/*TODO*///			uint data_long;
/*TODO*///			uint mask_long;
/*TODO*///			uint data_byte = 0;
/*TODO*///			uint mask_byte = 0;
/*TODO*///			uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///	
/*TODO*///			if(BIT_B(word2))
/*TODO*///				offset = MAKE_INT_32(REG_D[offset&7]);
/*TODO*///			if(BIT_5(word2))
/*TODO*///				width = REG_D[width&7];
/*TODO*///	
/*TODO*///			/* Offset is signed so we have to use ugly math =( */
/*TODO*///			ea += offset / 8;
/*TODO*///			offset %= 8;
/*TODO*///			if(offset < 0)
/*TODO*///			{
/*TODO*///				offset += 8;
/*TODO*///				ea--;
/*TODO*///			}
/*TODO*///			width = ((width-1) & 31) + 1;
/*TODO*///	
/*TODO*///	
/*TODO*///			mask_base = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///			mask_long = mask_base >> offset;
/*TODO*///	
/*TODO*///			data_long = m68ki_read_32(ea);
/*TODO*///			FLAG_N = ((data_long & (0x80000000 >> offset))<<offset)>>24;
/*TODO*///			FLAG_Z = data_long & mask_long;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			if((width + offset) > 32)
/*TODO*///			{
/*TODO*///				mask_byte = MASK_OUT_ABOVE_8(mask_base);
/*TODO*///				data_byte = m68ki_read_8(ea+4);
/*TODO*///				FLAG_Z |= (data_byte & mask_byte);
/*TODO*///			}
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bkpt, 0, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_010_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			m68ki_bkpt_ack(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE) ? REG_IR & 7 : 0);	/* auto-disable (see m68kcpu.h) */
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bra, 8, ., .)
/*TODO*///	{
/*TODO*///		m68ki_trace_t0();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_8(MASK_OUT_ABOVE_8(REG_IR));
/*TODO*///		if(REG_PC == REG_PPC)
/*TODO*///			USE_ALL_CYCLES();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bra, 16, ., .)
/*TODO*///	{
/*TODO*///		uint offset = OPER_I_16();
/*TODO*///		REG_PC -= 2;
/*TODO*///		m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_16(offset);
/*TODO*///		if(REG_PC == REG_PPC)
/*TODO*///			USE_ALL_CYCLES();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bra, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint offset = OPER_I_32();
/*TODO*///			REG_PC -= 4;
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_32(offset);
/*TODO*///			if(REG_PC == REG_PPC)
/*TODO*///				USE_ALL_CYCLES();
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bset, 32, r, d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint mask = 1 << (DX & 0x1f);
/*TODO*///	
/*TODO*///		FLAG_Z = *r_dst & mask;
/*TODO*///		*r_dst |= mask;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bset, 8, r, .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint src = m68ki_read_8(ea);
/*TODO*///		uint mask = 1 << (DX & 7);
/*TODO*///	
/*TODO*///		FLAG_Z = src & mask;
/*TODO*///		m68ki_write_8(ea, src | mask);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bset, 32, s, d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint mask = 1 << (OPER_I_8() & 0x1f);
/*TODO*///	
/*TODO*///		FLAG_Z = *r_dst & mask;
/*TODO*///		*r_dst |= mask;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bset, 8, s, .)
/*TODO*///	{
/*TODO*///		uint mask = 1 << (OPER_I_8() & 7);
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint src = m68ki_read_8(ea);
/*TODO*///	
/*TODO*///		FLAG_Z = src & mask;
/*TODO*///		m68ki_write_8(ea, src | mask);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bsr, 8, ., .)
/*TODO*///	{
/*TODO*///		m68ki_trace_t0();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_push_32(REG_PC);
/*TODO*///		m68ki_branch_8(MASK_OUT_ABOVE_8(REG_IR));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bsr, 16, ., .)
/*TODO*///	{
/*TODO*///		uint offset = OPER_I_16();
/*TODO*///		m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_push_32(REG_PC);
/*TODO*///		REG_PC -= 2;
/*TODO*///		m68ki_branch_16(offset);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(bsr, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint offset = OPER_I_32();
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_push_32(REG_PC);
/*TODO*///			REG_PC -= 4;
/*TODO*///			m68ki_branch_32(offset);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(btst, 32, r, d)
/*TODO*///	{
/*TODO*///		FLAG_Z = DY & (1 << (DX & 0x1f));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(btst, 8, r, .)
/*TODO*///	{
/*TODO*///		FLAG_Z = M68KMAKE_GET_OPER_AY_8 & (1 << (DX & 7));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(btst, 32, s, d)
/*TODO*///	{
/*TODO*///		FLAG_Z = DY & (1 << (OPER_I_8() & 0x1f));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(btst, 8, s, .)
/*TODO*///	{
/*TODO*///		uint bit = OPER_I_8() & 7;
/*TODO*///	
/*TODO*///		FLAG_Z = M68KMAKE_GET_OPER_AY_8 & (1 << bit);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(callm, 32, ., .)
/*TODO*///	{
/*TODO*///		/* note: watch out for pcrelative modes */
/*TODO*///		if(CPU_TYPE_IS_020_VARIANT(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///	
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			REG_PC += 2;
/*TODO*///	(void)ea;	/* just to avoid an 'unused variable' warning */
/*TODO*///			M68K_DO_LOG((M68K_LOG_FILEHANDLE "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///						 m68ki_cpu_names[CPU_TYPE], ADDRESS_68K(REG_PC - 2), REG_IR,
/*TODO*///						 m68k_disassemble_quick(ADDRESS_68K(REG_PC - 2))));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cas, 8, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///			uint dest = m68ki_read_8(ea);
/*TODO*///			uint* compare = &REG_D[word2 & 7];
/*TODO*///			uint res = dest - MASK_OUT_ABOVE_8(*compare);
/*TODO*///	
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			FLAG_N = NFLAG_8(res);
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///			FLAG_V = VFLAG_SUB_8(*compare, dest, res);
/*TODO*///			FLAG_C = CFLAG_8(res);
/*TODO*///	
/*TODO*///			if(COND_NE())
/*TODO*///				*compare = MASK_OUT_BELOW_8(*compare) | dest;
/*TODO*///			else
/*TODO*///			{
/*TODO*///				USE_CYCLES(3);
/*TODO*///				m68ki_write_8(ea, MASK_OUT_ABOVE_8(REG_D[(word2 >> 6) & 7]));
/*TODO*///			}
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cas, 16, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///			uint dest = m68ki_read_16(ea);
/*TODO*///			uint* compare = &REG_D[word2 & 7];
/*TODO*///			uint res = dest - MASK_OUT_ABOVE_16(*compare);
/*TODO*///	
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			FLAG_N = NFLAG_16(res);
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///			FLAG_V = VFLAG_SUB_16(*compare, dest, res);
/*TODO*///			FLAG_C = CFLAG_16(res);
/*TODO*///	
/*TODO*///			if(COND_NE())
/*TODO*///				*compare = MASK_OUT_BELOW_16(*compare) | dest;
/*TODO*///			else
/*TODO*///			{
/*TODO*///				USE_CYCLES(3);
/*TODO*///				m68ki_write_16(ea, MASK_OUT_ABOVE_16(REG_D[(word2 >> 6) & 7]));
/*TODO*///			}
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cas, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///			uint dest = m68ki_read_32(ea);
/*TODO*///			uint* compare = &REG_D[word2 & 7];
/*TODO*///			uint res = dest - *compare;
/*TODO*///	
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			FLAG_N = NFLAG_32(res);
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///			FLAG_V = VFLAG_SUB_32(*compare, dest, res);
/*TODO*///			FLAG_C = CFLAG_SUB_32(*compare, dest, res);
/*TODO*///	
/*TODO*///			if(COND_NE())
/*TODO*///				*compare = dest;
/*TODO*///			else
/*TODO*///			{
/*TODO*///				USE_CYCLES(3);
/*TODO*///				m68ki_write_32(ea, REG_D[(word2 >> 6) & 7]);
/*TODO*///			}
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cas2, 16, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_32();
/*TODO*///			uint* compare1 = &REG_D[(word2 >> 16) & 7];
/*TODO*///			uint ea1 = REG_DA[(word2 >> 28) & 15];
/*TODO*///			uint dest1 = m68ki_read_16(ea1);
/*TODO*///			uint res1 = dest1 - MASK_OUT_ABOVE_16(*compare1);
/*TODO*///			uint* compare2 = &REG_D[word2 & 7];
/*TODO*///			uint ea2 = REG_DA[(word2 >> 12) & 15];
/*TODO*///			uint dest2 = m68ki_read_16(ea2);
/*TODO*///			uint res2;
/*TODO*///	
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			FLAG_N = NFLAG_16(res1);
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_16(res1);
/*TODO*///			FLAG_V = VFLAG_SUB_16(*compare1, dest1, res1);
/*TODO*///			FLAG_C = CFLAG_16(res1);
/*TODO*///	
/*TODO*///			if(COND_EQ())
/*TODO*///			{
/*TODO*///				res2 = dest2 - MASK_OUT_ABOVE_16(*compare2);
/*TODO*///	
/*TODO*///				FLAG_N = NFLAG_16(res2);
/*TODO*///				FLAG_Z = MASK_OUT_ABOVE_16(res2);
/*TODO*///				FLAG_V = VFLAG_SUB_16(*compare2, dest2, res2);
/*TODO*///				FLAG_C = CFLAG_16(res2);
/*TODO*///	
/*TODO*///				if(COND_EQ())
/*TODO*///				{
/*TODO*///					USE_CYCLES(3);
/*TODO*///					m68ki_write_16(ea1, REG_D[(word2 >> 22) & 7]);
/*TODO*///					m68ki_write_16(ea2, REG_D[(word2 >> 6) & 7]);
/*TODO*///					return;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			*compare1 = BIT_1F(word2) ? MAKE_INT_16(dest1) : MASK_OUT_BELOW_16(*compare1) | dest1;
/*TODO*///			*compare2 = BIT_F(word2) ? MAKE_INT_16(dest2) : MASK_OUT_BELOW_16(*compare2) | dest2;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cas2, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_32();
/*TODO*///			uint* compare1 = &REG_D[(word2 >> 16) & 7];
/*TODO*///			uint ea1 = REG_DA[(word2 >> 28) & 15];
/*TODO*///			uint dest1 = m68ki_read_32(ea1);
/*TODO*///			uint res1 = dest1 - *compare1;
/*TODO*///			uint* compare2 = &REG_D[word2 & 7];
/*TODO*///			uint ea2 = REG_DA[(word2 >> 12) & 15];
/*TODO*///			uint dest2 = m68ki_read_32(ea2);
/*TODO*///			uint res2;
/*TODO*///	
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			FLAG_N = NFLAG_32(res1);
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_32(res1);
/*TODO*///			FLAG_V = VFLAG_SUB_32(*compare1, dest1, res1);
/*TODO*///			FLAG_C = CFLAG_SUB_32(*compare1, dest1, res1);
/*TODO*///	
/*TODO*///			if(COND_EQ())
/*TODO*///			{
/*TODO*///				res2 = dest2 - *compare2;
/*TODO*///	
/*TODO*///				FLAG_N = NFLAG_32(res2);
/*TODO*///				FLAG_Z = MASK_OUT_ABOVE_32(res2);
/*TODO*///				FLAG_V = VFLAG_SUB_32(*compare2, dest2, res2);
/*TODO*///				FLAG_C = CFLAG_SUB_32(*compare2, dest2, res2);
/*TODO*///	
/*TODO*///				if(COND_EQ())
/*TODO*///				{
/*TODO*///					USE_CYCLES(3);
/*TODO*///					m68ki_write_32(ea1, REG_D[(word2 >> 22) & 7]);
/*TODO*///					m68ki_write_32(ea2, REG_D[(word2 >> 6) & 7]);
/*TODO*///					return;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			*compare1 = dest1;
/*TODO*///			*compare2 = dest2;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(chk, 16, ., d)
/*TODO*///	{
/*TODO*///		sint src = MAKE_INT_16(DX);
/*TODO*///		sint bound = MAKE_INT_16(DY);
/*TODO*///	
/*TODO*///		if(src >= 0 && src <= bound)
/*TODO*///		{
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		FLAG_N = (src < 0)<<7;
/*TODO*///		m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(chk, 16, ., .)
/*TODO*///	{
/*TODO*///		sint src = MAKE_INT_16(DX);
/*TODO*///		sint bound = MAKE_INT_16(M68KMAKE_GET_OPER_AY_16);
/*TODO*///	
/*TODO*///		if(src >= 0 && src <= bound)
/*TODO*///		{
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		FLAG_N = (src < 0)<<7;
/*TODO*///		m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(chk, 32, ., d)
/*TODO*///	{
/*TODO*///		logerror("%08x: Chk 32d\n",cpu_get_pc());
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			sint src = MAKE_INT_32(DX);
/*TODO*///			sint bound = MAKE_INT_32(DY);
/*TODO*///	
/*TODO*///			if(src >= 0 && src <= bound)
/*TODO*///			{
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			FLAG_N = (src < 0)<<7;
/*TODO*///			m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(chk, 32, ., .)
/*TODO*///	{
/*TODO*///			logerror("%08x: Chk 32\n",cpu_get_pc());
/*TODO*///	
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			sint src = MAKE_INT_32(DX);
/*TODO*///			sint bound = MAKE_INT_32(M68KMAKE_GET_OPER_AY_32);
/*TODO*///	
/*TODO*///			if(src >= 0 && src <= bound)
/*TODO*///			{
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			FLAG_N = (src < 0)<<7;
/*TODO*///			m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(chk2cmp2, 8, ., pcdi)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint compare = REG_DA[(word2 >> 12) & 15];
/*TODO*///			uint ea = EA_PCDI_8();
/*TODO*///			uint lower_bound = m68ki_read_pcrel_8(ea);
/*TODO*///			uint upper_bound = m68ki_read_pcrel_8(ea + 1);
/*TODO*///	
/*TODO*///			if(!BIT_F(word2))
/*TODO*///				FLAG_C = MAKE_INT_8(compare) - MAKE_INT_8(lower_bound);
/*TODO*///			else
/*TODO*///				FLAG_C = compare - lower_bound;
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_8(FLAG_C);
/*TODO*///			if(COND_CS())
/*TODO*///			{
/*TODO*///				if(BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			FLAG_C = upper_bound - compare;
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_8(FLAG_C);
/*TODO*///			if(COND_CS() && BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(chk2cmp2, 8, ., pcix)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint compare = REG_DA[(word2 >> 12) & 15];
/*TODO*///			uint ea = EA_PCIX_8();
/*TODO*///			uint lower_bound = m68ki_read_pcrel_8(ea);
/*TODO*///			uint upper_bound = m68ki_read_pcrel_8(ea + 1);
/*TODO*///	
/*TODO*///			if(!BIT_F(word2))
/*TODO*///				FLAG_C = MAKE_INT_8(compare) - MAKE_INT_8(lower_bound);
/*TODO*///			else
/*TODO*///				FLAG_C = compare - lower_bound;
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_8(FLAG_C);
/*TODO*///			if(COND_CS())
/*TODO*///			{
/*TODO*///				if(BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			FLAG_C = upper_bound - compare;
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_8(FLAG_C);
/*TODO*///			if(COND_CS() && BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(chk2cmp2, 8, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint compare = REG_DA[(word2 >> 12) & 15];
/*TODO*///			uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///			uint lower_bound = m68ki_read_8(ea);
/*TODO*///			uint upper_bound = m68ki_read_8(ea + 1);
/*TODO*///	
/*TODO*///			if(!BIT_F(word2))
/*TODO*///				FLAG_C = MAKE_INT_8(compare) - MAKE_INT_8(lower_bound);
/*TODO*///			else
/*TODO*///				FLAG_C = compare - lower_bound;
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_8(FLAG_C);
/*TODO*///			if(COND_CS())
/*TODO*///			{
/*TODO*///				if(BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			FLAG_C = upper_bound - compare;
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_8(FLAG_C);
/*TODO*///			if(COND_CS() && BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(chk2cmp2, 16, ., pcdi)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint compare = REG_DA[(word2 >> 12) & 15];
/*TODO*///			uint ea = EA_PCDI_16();
/*TODO*///			uint lower_bound = m68ki_read_pcrel_16(ea);
/*TODO*///			uint upper_bound = m68ki_read_pcrel_16(ea + 2);
/*TODO*///	
/*TODO*///			if(!BIT_F(word2))
/*TODO*///				FLAG_C = MAKE_INT_16(compare) - MAKE_INT_16(lower_bound);
/*TODO*///			else
/*TODO*///				FLAG_C = compare - lower_bound;
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_16(FLAG_C);
/*TODO*///			FLAG_C = CFLAG_16(FLAG_C);
/*TODO*///			if(COND_CS())
/*TODO*///			{
/*TODO*///				if(BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			if(!BIT_F(word2))
/*TODO*///				FLAG_C = MAKE_INT_16(upper_bound) - MAKE_INT_16(compare);
/*TODO*///			else
/*TODO*///				FLAG_C = upper_bound - compare;
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_16(FLAG_C);
/*TODO*///			FLAG_C = CFLAG_16(FLAG_C);
/*TODO*///			if(COND_CS() && BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(chk2cmp2, 16, ., pcix)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint compare = REG_DA[(word2 >> 12) & 15];
/*TODO*///			uint ea = EA_PCIX_16();
/*TODO*///			uint lower_bound = m68ki_read_pcrel_16(ea);
/*TODO*///			uint upper_bound = m68ki_read_pcrel_16(ea + 2);
/*TODO*///	
/*TODO*///			if(!BIT_F(word2))
/*TODO*///				FLAG_C = MAKE_INT_16(compare) - MAKE_INT_16(lower_bound);
/*TODO*///			else
/*TODO*///				FLAG_C = compare - lower_bound;
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_16(FLAG_C);
/*TODO*///			FLAG_C = CFLAG_16(FLAG_C);
/*TODO*///			if(COND_CS())
/*TODO*///			{
/*TODO*///				if(BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			if(!BIT_F(word2))
/*TODO*///				FLAG_C = MAKE_INT_16(upper_bound) - MAKE_INT_16(compare);
/*TODO*///			else
/*TODO*///				FLAG_C = upper_bound - compare;
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_16(FLAG_C);
/*TODO*///			FLAG_C = CFLAG_16(FLAG_C);
/*TODO*///			if(COND_CS() && BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(chk2cmp2, 16, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint compare = REG_DA[(word2 >> 12) & 15];
/*TODO*///			uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///			uint lower_bound = m68ki_read_16(ea);
/*TODO*///			uint upper_bound = m68ki_read_16(ea + 2);
/*TODO*///	
/*TODO*///			if(!BIT_F(word2))
/*TODO*///				FLAG_C = MAKE_INT_16(compare) - MAKE_INT_16(lower_bound);
/*TODO*///			else
/*TODO*///				FLAG_C = compare - lower_bound;
/*TODO*///	
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_16(FLAG_C);
/*TODO*///			FLAG_C = CFLAG_16(FLAG_C);
/*TODO*///			if(COND_CS())
/*TODO*///			{
/*TODO*///				if(BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			if(!BIT_F(word2))
/*TODO*///				FLAG_C = MAKE_INT_16(upper_bound) - MAKE_INT_16(compare);
/*TODO*///			else
/*TODO*///				FLAG_C = upper_bound - compare;
/*TODO*///	
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_16(FLAG_C);
/*TODO*///			FLAG_C = CFLAG_16(FLAG_C);
/*TODO*///			if(COND_CS() && BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(chk2cmp2, 32, ., pcdi)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint compare = REG_DA[(word2 >> 12) & 15];
/*TODO*///			uint ea = EA_PCDI_32();
/*TODO*///			uint lower_bound = m68ki_read_pcrel_32(ea);
/*TODO*///			uint upper_bound = m68ki_read_pcrel_32(ea + 4);
/*TODO*///	
/*TODO*///			FLAG_C = compare - lower_bound;
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_32(FLAG_C);
/*TODO*///			FLAG_C = CFLAG_SUB_32(lower_bound, compare, FLAG_C);
/*TODO*///			if(COND_CS())
/*TODO*///			{
/*TODO*///				if(BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			FLAG_C = upper_bound - compare;
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_32(FLAG_C);
/*TODO*///			FLAG_C = CFLAG_SUB_32(compare, upper_bound, FLAG_C);
/*TODO*///			if(COND_CS() && BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(chk2cmp2, 32, ., pcix)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint compare = REG_DA[(word2 >> 12) & 15];
/*TODO*///			uint ea = EA_PCIX_32();
/*TODO*///			uint lower_bound = m68ki_read_pcrel_32(ea);
/*TODO*///			uint upper_bound = m68ki_read_pcrel_32(ea + 4);
/*TODO*///	
/*TODO*///			FLAG_C = compare - lower_bound;
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_32(FLAG_C);
/*TODO*///			FLAG_C = CFLAG_SUB_32(lower_bound, compare, FLAG_C);
/*TODO*///			if(COND_CS())
/*TODO*///			{
/*TODO*///				if(BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			FLAG_C = upper_bound - compare;
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_32(FLAG_C);
/*TODO*///			FLAG_C = CFLAG_SUB_32(compare, upper_bound, FLAG_C);
/*TODO*///			if(COND_CS() && BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(chk2cmp2, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint compare = REG_DA[(word2 >> 12) & 15];
/*TODO*///			uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///			uint lower_bound = m68ki_read_32(ea);
/*TODO*///			uint upper_bound = m68ki_read_32(ea + 4);
/*TODO*///	
/*TODO*///			FLAG_C = compare - lower_bound;
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_32(FLAG_C);
/*TODO*///			FLAG_C = CFLAG_SUB_32(lower_bound, compare, FLAG_C);
/*TODO*///			if(COND_CS())
/*TODO*///			{
/*TODO*///				if(BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			FLAG_C = upper_bound - compare;
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_32(FLAG_C);
/*TODO*///			FLAG_C = CFLAG_SUB_32(compare, upper_bound, FLAG_C);
/*TODO*///			if(COND_CS() && BIT_B(word2))
/*TODO*///					m68ki_exception_trap(EXCEPTION_CHK);
/*TODO*///	
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(clr, 8, ., d)
/*TODO*///	{
/*TODO*///		DY &= 0xffffff00;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_Z = ZFLAG_SET;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(clr, 8, ., .)
/*TODO*///	{
/*TODO*///		m68ki_write_8(M68KMAKE_GET_EA_AY_8, 0);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_Z = ZFLAG_SET;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(clr, 16, ., d)
/*TODO*///	{
/*TODO*///		DY &= 0xffff0000;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_Z = ZFLAG_SET;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(clr, 16, ., .)
/*TODO*///	{
/*TODO*///		m68ki_write_16(M68KMAKE_GET_EA_AY_16, 0);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_Z = ZFLAG_SET;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(clr, 32, ., d)
/*TODO*///	{
/*TODO*///		DY = 0;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_Z = ZFLAG_SET;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(clr, 32, ., .)
/*TODO*///	{
/*TODO*///		m68ki_write_32(M68KMAKE_GET_EA_AY_32, 0);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_Z = ZFLAG_SET;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmp, 8, ., d)
/*TODO*///	{
/*TODO*///		uint src = MASK_OUT_ABOVE_8(DY);
/*TODO*///		uint dst = MASK_OUT_ABOVE_8(DX);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_8(res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmp, 8, ., .)
/*TODO*///	{
/*TODO*///		uint src = M68KMAKE_GET_OPER_AY_8;
/*TODO*///		uint dst = MASK_OUT_ABOVE_8(DX);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_8(res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmp, 16, ., d)
/*TODO*///	{
/*TODO*///		uint src = MASK_OUT_ABOVE_16(DY);
/*TODO*///		uint dst = MASK_OUT_ABOVE_16(DX);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///		FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_16(res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmp, 16, ., a)
/*TODO*///	{
/*TODO*///		uint src = MASK_OUT_ABOVE_16(AY);
/*TODO*///		uint dst = MASK_OUT_ABOVE_16(DX);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///		FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_16(res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmp, 16, ., .)
/*TODO*///	{
/*TODO*///		uint src = M68KMAKE_GET_OPER_AY_16;
/*TODO*///		uint dst = MASK_OUT_ABOVE_16(DX);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///		FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_16(res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmp, 32, ., d)
/*TODO*///	{
/*TODO*///		uint src = DY;
/*TODO*///		uint dst = DX;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmp, 32, ., a)
/*TODO*///	{
/*TODO*///		uint src = AY;
/*TODO*///		uint dst = DX;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmp, 32, ., .)
/*TODO*///	{
/*TODO*///		uint src = M68KMAKE_GET_OPER_AY_32;
/*TODO*///		uint dst = DX;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpa, 16, ., d)
/*TODO*///	{
/*TODO*///		uint src = MAKE_INT_16(DY);
/*TODO*///		uint dst = AX;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpa, 16, ., a)
/*TODO*///	{
/*TODO*///		uint src = MAKE_INT_16(AY);
/*TODO*///		uint dst = AX;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpa, 16, ., .)
/*TODO*///	{
/*TODO*///		uint src = MAKE_INT_16(M68KMAKE_GET_OPER_AY_16);
/*TODO*///		uint dst = AX;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpa, 32, ., d)
/*TODO*///	{
/*TODO*///		uint src = DY;
/*TODO*///		uint dst = AX;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpa, 32, ., a)
/*TODO*///	{
/*TODO*///		uint src = AY;
/*TODO*///		uint dst = AX;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpa, 32, ., .)
/*TODO*///	{
/*TODO*///		uint src = M68KMAKE_GET_OPER_AY_32;
/*TODO*///		uint dst = AX;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpi, 8, ., d)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_8();
/*TODO*///		uint dst = MASK_OUT_ABOVE_8(DY);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_8(res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpi, 8, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_8();
/*TODO*///		uint dst = M68KMAKE_GET_OPER_AY_8;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_8(res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpi, 8, ., pcdi)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint src = OPER_I_8();
/*TODO*///			uint dst = OPER_PCDI_8();
/*TODO*///			uint res = dst - src;
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_8(res);
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///			FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///			FLAG_C = CFLAG_8(res);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpi, 8, ., pcix)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint src = OPER_I_8();
/*TODO*///			uint dst = OPER_PCIX_8();
/*TODO*///			uint res = dst - src;
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_8(res);
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///			FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///			FLAG_C = CFLAG_8(res);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpi, 16, ., d)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_16();
/*TODO*///		uint dst = MASK_OUT_ABOVE_16(DY);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///		FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_16(res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpi, 16, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_16();
/*TODO*///		uint dst = M68KMAKE_GET_OPER_AY_16;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///		FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_16(res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpi, 16, ., pcdi)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint src = OPER_I_16();
/*TODO*///			uint dst = OPER_PCDI_16();
/*TODO*///			uint res = dst - src;
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_16(res);
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///			FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///			FLAG_C = CFLAG_16(res);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpi, 16, ., pcix)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint src = OPER_I_16();
/*TODO*///			uint dst = OPER_PCIX_16();
/*TODO*///			uint res = dst - src;
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_16(res);
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///			FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///			FLAG_C = CFLAG_16(res);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpi, 32, ., d)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_32();
/*TODO*///		uint dst = DY;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpi, 32, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_32();
/*TODO*///		uint dst = M68KMAKE_GET_OPER_AY_32;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpi, 32, ., pcdi)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint src = OPER_I_32();
/*TODO*///			uint dst = OPER_PCDI_32();
/*TODO*///			uint res = dst - src;
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_32(res);
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///			FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///			FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpi, 32, ., pcix)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint src = OPER_I_32();
/*TODO*///			uint dst = OPER_PCIX_32();
/*TODO*///			uint res = dst - src;
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_32(res);
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///			FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///			FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpm, 8, ., ax7)
/*TODO*///	{
/*TODO*///		uint src = OPER_AY_PI_8();
/*TODO*///		uint dst = OPER_A7_PI_8();
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_8(res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpm, 8, ., ay7)
/*TODO*///	{
/*TODO*///		uint src = OPER_A7_PI_8();
/*TODO*///		uint dst = OPER_AX_PI_8();
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_8(res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpm, 8, ., axy7)
/*TODO*///	{
/*TODO*///		uint src = OPER_A7_PI_8();
/*TODO*///		uint dst = OPER_A7_PI_8();
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_8(res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpm, 8, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_AY_PI_8();
/*TODO*///		uint dst = OPER_AX_PI_8();
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_8(res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpm, 16, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_AY_PI_16();
/*TODO*///		uint dst = OPER_AX_PI_16();
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///		FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_16(res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cmpm, 32, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_AY_PI_32();
/*TODO*///		uint dst = OPER_AX_PI_32();
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cpbcc, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			M68K_DO_LOG((M68K_LOG_FILEHANDLE "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///						 m68ki_cpu_names[CPU_TYPE], ADDRESS_68K(REG_PC - 2), REG_IR,
/*TODO*///						 m68k_disassemble_quick(ADDRESS_68K(REG_PC - 2))));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_1111();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cpdbcc, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			M68K_DO_LOG((M68K_LOG_FILEHANDLE "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///						 m68ki_cpu_names[CPU_TYPE], ADDRESS_68K(REG_PC - 2), REG_IR,
/*TODO*///						 m68k_disassemble_quick(ADDRESS_68K(REG_PC - 2))));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_1111();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cpgen, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			M68K_DO_LOG((M68K_LOG_FILEHANDLE "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///						 m68ki_cpu_names[CPU_TYPE], ADDRESS_68K(REG_PC - 2), REG_IR,
/*TODO*///						 m68k_disassemble_quick(ADDRESS_68K(REG_PC - 2))));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_1111();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cpscc, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			M68K_DO_LOG((M68K_LOG_FILEHANDLE "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///						 m68ki_cpu_names[CPU_TYPE], ADDRESS_68K(REG_PC - 2), REG_IR,
/*TODO*///						 m68k_disassemble_quick(ADDRESS_68K(REG_PC - 2))));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_1111();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(cptrapcc, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			M68K_DO_LOG((M68K_LOG_FILEHANDLE "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///						 m68ki_cpu_names[CPU_TYPE], ADDRESS_68K(REG_PC - 2), REG_IR,
/*TODO*///						 m68k_disassemble_quick(ADDRESS_68K(REG_PC - 2))));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_1111();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(dbt, 16, ., .)
/*TODO*///	{
/*TODO*///		REG_PC += 2;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(dbf, 16, ., .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(*r_dst - 1);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///		if(res != 0xffff)
/*TODO*///		{
/*TODO*///			uint offset = OPER_I_16();
/*TODO*///			REG_PC -= 2;
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_16(offset);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		REG_PC += 2;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(dbcc, 16, ., .)
/*TODO*///	{
/*TODO*///		if (M68KMAKE_NOT_CC != 0)
/*TODO*///		{
/*TODO*///			uint* r_dst = &DY;
/*TODO*///			uint res = MASK_OUT_ABOVE_16(*r_dst - 1);
/*TODO*///	
/*TODO*///			*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///			if(res != 0xffff)
/*TODO*///			{
/*TODO*///				uint offset = OPER_I_16();
/*TODO*///				REG_PC -= 2;
/*TODO*///				m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///				m68ki_branch_16(offset);
/*TODO*///				USE_CYCLES(CYC_DBCC_F_NOEXP);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			REG_PC += 2;
/*TODO*///			USE_CYCLES(CYC_DBCC_F_EXP);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		REG_PC += 2;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(divs, 16, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		sint src = MAKE_INT_16(DY);
/*TODO*///		sint quotient;
/*TODO*///		sint remainder;
/*TODO*///	
/*TODO*///		if(src != 0)
/*TODO*///		{
/*TODO*///			if((uint32)*r_dst == 0x80000000 && src == -1)
/*TODO*///			{
/*TODO*///				FLAG_Z = 0;
/*TODO*///				FLAG_N = NFLAG_CLEAR;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				FLAG_C = CFLAG_CLEAR;
/*TODO*///				*r_dst = 0;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			quotient = MAKE_INT_32(*r_dst) / src;
/*TODO*///			remainder = MAKE_INT_32(*r_dst) % src;
/*TODO*///	
/*TODO*///			if(quotient == MAKE_INT_16(quotient))
/*TODO*///			{
/*TODO*///				FLAG_Z = quotient;
/*TODO*///				FLAG_N = NFLAG_16(quotient);
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				FLAG_C = CFLAG_CLEAR;
/*TODO*///				*r_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			FLAG_V = VFLAG_SET;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_trap(EXCEPTION_ZERO_DIVIDE);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(divs, 16, ., .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		sint src = MAKE_INT_16(M68KMAKE_GET_OPER_AY_16);
/*TODO*///		sint quotient;
/*TODO*///		sint remainder;
/*TODO*///	
/*TODO*///		if(src != 0)
/*TODO*///		{
/*TODO*///			if((uint32)*r_dst == 0x80000000 && src == -1)
/*TODO*///			{
/*TODO*///				FLAG_Z = 0;
/*TODO*///				FLAG_N = NFLAG_CLEAR;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				FLAG_C = CFLAG_CLEAR;
/*TODO*///				*r_dst = 0;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			quotient = MAKE_INT_32(*r_dst) / src;
/*TODO*///			remainder = MAKE_INT_32(*r_dst) % src;
/*TODO*///	
/*TODO*///			if(quotient == MAKE_INT_16(quotient))
/*TODO*///			{
/*TODO*///				FLAG_Z = quotient;
/*TODO*///				FLAG_N = NFLAG_16(quotient);
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				FLAG_C = CFLAG_CLEAR;
/*TODO*///				*r_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			FLAG_V = VFLAG_SET;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_trap(EXCEPTION_ZERO_DIVIDE);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(divu, 16, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(DY);
/*TODO*///	
/*TODO*///		if(src != 0)
/*TODO*///		{
/*TODO*///			uint quotient = *r_dst / src;
/*TODO*///			uint remainder = *r_dst % src;
/*TODO*///	
/*TODO*///			if(quotient < 0x10000)
/*TODO*///			{
/*TODO*///				FLAG_Z = quotient;
/*TODO*///				FLAG_N = NFLAG_16(quotient);
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				FLAG_C = CFLAG_CLEAR;
/*TODO*///				*r_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			FLAG_V = VFLAG_SET;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_trap(EXCEPTION_ZERO_DIVIDE);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(divu, 16, ., .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = M68KMAKE_GET_OPER_AY_16;
/*TODO*///	
/*TODO*///		if(src != 0)
/*TODO*///		{
/*TODO*///			uint quotient = *r_dst / src;
/*TODO*///			uint remainder = *r_dst % src;
/*TODO*///	
/*TODO*///			if(quotient < 0x10000)
/*TODO*///			{
/*TODO*///				FLAG_Z = quotient;
/*TODO*///				FLAG_N = NFLAG_16(quotient);
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				FLAG_C = CFLAG_CLEAR;
/*TODO*///				*r_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			FLAG_V = VFLAG_SET;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_trap(EXCEPTION_ZERO_DIVIDE);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(divl, 32, ., d)
/*TODO*///	{
/*TODO*///	#if M68K_USE_64_BIT
/*TODO*///	
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint64 divisor   = DY;
/*TODO*///			uint64 dividend  = 0;
/*TODO*///			uint64 quotient  = 0;
/*TODO*///			uint64 remainder = 0;
/*TODO*///	
/*TODO*///			if(divisor != 0)
/*TODO*///			{
/*TODO*///				if(BIT_A(word2))	/* 64 bit */
/*TODO*///				{
/*TODO*///					dividend = REG_D[word2 & 7];
/*TODO*///					dividend <<= 32;
/*TODO*///					dividend |= REG_D[(word2 >> 12) & 7];
/*TODO*///	
/*TODO*///					if(BIT_B(word2))	   /* signed */
/*TODO*///					{
/*TODO*///						quotient  = (uint64)((sint64)dividend / (sint64)((sint32)divisor));
/*TODO*///						remainder = (uint64)((sint64)dividend % (sint64)((sint32)divisor));
/*TODO*///						if((sint64)quotient != (sint64)((sint32)quotient))
/*TODO*///						{
/*TODO*///							FLAG_V = VFLAG_SET;
/*TODO*///							return;
/*TODO*///						}
/*TODO*///					}
/*TODO*///					else					/* unsigned */
/*TODO*///					{
/*TODO*///						quotient = dividend / divisor;
/*TODO*///						if(quotient > 0xffffffff)
/*TODO*///						{
/*TODO*///							FLAG_V = VFLAG_SET;
/*TODO*///							return;
/*TODO*///						}
/*TODO*///						remainder = dividend % divisor;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else	/* 32 bit */
/*TODO*///				{
/*TODO*///					dividend = REG_D[(word2 >> 12) & 7];
/*TODO*///					if(BIT_B(word2))	   /* signed */
/*TODO*///					{
/*TODO*///						quotient  = (uint64)((sint64)((sint32)dividend) / (sint64)((sint32)divisor));
/*TODO*///						remainder = (uint64)((sint64)((sint32)dividend) % (sint64)((sint32)divisor));
/*TODO*///					}
/*TODO*///					else					/* unsigned */
/*TODO*///					{
/*TODO*///						quotient = dividend / divisor;
/*TODO*///						remainder = dividend % divisor;
/*TODO*///					}
/*TODO*///				}
/*TODO*///	
/*TODO*///				REG_D[word2 & 7] = remainder;
/*TODO*///				REG_D[(word2 >> 12) & 7] = quotient;
/*TODO*///	
/*TODO*///				FLAG_N = NFLAG_32(quotient);
/*TODO*///				FLAG_Z = quotient;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				FLAG_C = CFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			m68ki_exception_trap(EXCEPTION_ZERO_DIVIDE);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	
/*TODO*///	#else
/*TODO*///	
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint divisor = DY;
/*TODO*///			uint dividend_hi = REG_D[word2 & 7];
/*TODO*///			uint dividend_lo = REG_D[(word2 >> 12) & 7];
/*TODO*///			uint quotient = 0;
/*TODO*///			uint remainder = 0;
/*TODO*///			uint dividend_neg = 0;
/*TODO*///			uint divisor_neg = 0;
/*TODO*///			sint i;
/*TODO*///			uint overflow;
/*TODO*///	
/*TODO*///			if(divisor != 0)
/*TODO*///			{
/*TODO*///				/* quad / long : long quotient, long remainder */
/*TODO*///				if(BIT_A(word2))
/*TODO*///				{
/*TODO*///					if(BIT_B(word2))	   /* signed */
/*TODO*///					{
/*TODO*///						/* special case in signed divide */
/*TODO*///						if(dividend_hi == 0 && dividend_lo == 0x80000000 && divisor == 0xffffffff)
/*TODO*///						{
/*TODO*///							REG_D[word2 & 7] = 0;
/*TODO*///							REG_D[(word2 >> 12) & 7] = 0x80000000;
/*TODO*///	
/*TODO*///							FLAG_N = NFLAG_SET;
/*TODO*///							FLAG_Z = ZFLAG_CLEAR;
/*TODO*///							FLAG_V = VFLAG_CLEAR;
/*TODO*///							FLAG_C = CFLAG_CLEAR;
/*TODO*///							return;
/*TODO*///						}
/*TODO*///						if(GET_MSB_32(dividend_hi))
/*TODO*///						{
/*TODO*///							dividend_neg = 1;
/*TODO*///							dividend_hi = (uint)MASK_OUT_ABOVE_32((-(sint)dividend_hi) - (dividend_lo != 0));
/*TODO*///							dividend_lo = (uint)MASK_OUT_ABOVE_32(-(sint)dividend_lo);
/*TODO*///						}
/*TODO*///						if(GET_MSB_32(divisor))
/*TODO*///						{
/*TODO*///							divisor_neg = 1;
/*TODO*///							divisor = (uint)MASK_OUT_ABOVE_32(-(sint)divisor);
/*TODO*///	
/*TODO*///						}
/*TODO*///					}
/*TODO*///	
/*TODO*///					/* if the upper long is greater than the divisor, we're overflowing. */
/*TODO*///					if(dividend_hi >= divisor)
/*TODO*///					{
/*TODO*///						FLAG_V = VFLAG_SET;
/*TODO*///						return;
/*TODO*///					}
/*TODO*///	
/*TODO*///					for(i = 31; i >= 0; i--)
/*TODO*///					{
/*TODO*///						quotient <<= 1;
/*TODO*///						remainder = (remainder << 1) + ((dividend_hi >> i) & 1);
/*TODO*///						if(remainder >= divisor)
/*TODO*///						{
/*TODO*///							remainder -= divisor;
/*TODO*///							quotient++;
/*TODO*///						}
/*TODO*///					}
/*TODO*///					for(i = 31; i >= 0; i--)
/*TODO*///					{
/*TODO*///						quotient <<= 1;
/*TODO*///						overflow = GET_MSB_32(remainder);
/*TODO*///						remainder = (remainder << 1) + ((dividend_lo >> i) & 1);
/*TODO*///						if(remainder >= divisor || overflow)
/*TODO*///						{
/*TODO*///							remainder -= divisor;
/*TODO*///							quotient++;
/*TODO*///						}
/*TODO*///					}
/*TODO*///	
/*TODO*///					if(BIT_B(word2))	   /* signed */
/*TODO*///					{
/*TODO*///						if(quotient > 0x7fffffff)
/*TODO*///						{
/*TODO*///							FLAG_V = VFLAG_SET;
/*TODO*///							return;
/*TODO*///						}
/*TODO*///						if (dividend_neg != 0)
/*TODO*///						{
/*TODO*///							remainder = (uint)MASK_OUT_ABOVE_32(-(sint)remainder);
/*TODO*///							quotient = (uint)MASK_OUT_ABOVE_32(-(sint)quotient);
/*TODO*///						}
/*TODO*///						if (divisor_neg != 0)
/*TODO*///							quotient = (uint)MASK_OUT_ABOVE_32(-(sint)quotient);
/*TODO*///					}
/*TODO*///	
/*TODO*///					REG_D[word2 & 7] = remainder;
/*TODO*///					REG_D[(word2 >> 12) & 7] = quotient;
/*TODO*///	
/*TODO*///					FLAG_N = NFLAG_32(quotient);
/*TODO*///					FLAG_Z = quotient;
/*TODO*///					FLAG_V = VFLAG_CLEAR;
/*TODO*///					FLAG_C = CFLAG_CLEAR;
/*TODO*///					return;
/*TODO*///				}
/*TODO*///	
/*TODO*///				/* long / long: long quotient, maybe long remainder */
/*TODO*///				if(BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					/* Special case in divide */
/*TODO*///					if(dividend_lo == 0x80000000 && divisor == 0xffffffff)
/*TODO*///					{
/*TODO*///						FLAG_N = NFLAG_SET;
/*TODO*///						FLAG_Z = ZFLAG_CLEAR;
/*TODO*///						FLAG_V = VFLAG_CLEAR;
/*TODO*///						FLAG_C = CFLAG_CLEAR;
/*TODO*///						REG_D[(word2 >> 12) & 7] = 0x80000000;
/*TODO*///						REG_D[word2 & 7] = 0;
/*TODO*///						return;
/*TODO*///					}
/*TODO*///					REG_D[word2 & 7] = MAKE_INT_32(dividend_lo) % MAKE_INT_32(divisor);
/*TODO*///					quotient = REG_D[(word2 >> 12) & 7] = MAKE_INT_32(dividend_lo) / MAKE_INT_32(divisor);
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					REG_D[word2 & 7] = MASK_OUT_ABOVE_32(dividend_lo) % MASK_OUT_ABOVE_32(divisor);
/*TODO*///					quotient = REG_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(dividend_lo) / MASK_OUT_ABOVE_32(divisor);
/*TODO*///				}
/*TODO*///	
/*TODO*///				FLAG_N = NFLAG_32(quotient);
/*TODO*///				FLAG_Z = quotient;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				FLAG_C = CFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			m68ki_exception_trap(EXCEPTION_ZERO_DIVIDE);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	
/*TODO*///	#endif
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(divl, 32, ., .)
/*TODO*///	{
/*TODO*///	#if M68K_USE_64_BIT
/*TODO*///	
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint64 divisor = M68KMAKE_GET_OPER_AY_32;
/*TODO*///			uint64 dividend  = 0;
/*TODO*///			uint64 quotient  = 0;
/*TODO*///			uint64 remainder = 0;
/*TODO*///	
/*TODO*///			if(divisor != 0)
/*TODO*///			{
/*TODO*///				if(BIT_A(word2))	/* 64 bit */
/*TODO*///				{
/*TODO*///					dividend = REG_D[word2 & 7];
/*TODO*///					dividend <<= 32;
/*TODO*///					dividend |= REG_D[(word2 >> 12) & 7];
/*TODO*///	
/*TODO*///					if(BIT_B(word2))	   /* signed */
/*TODO*///					{
/*TODO*///						quotient  = (uint64)((sint64)dividend / (sint64)((sint32)divisor));
/*TODO*///						remainder = (uint64)((sint64)dividend % (sint64)((sint32)divisor));
/*TODO*///						if((sint64)quotient != (sint64)((sint32)quotient))
/*TODO*///						{
/*TODO*///							FLAG_V = VFLAG_SET;
/*TODO*///							return;
/*TODO*///						}
/*TODO*///					}
/*TODO*///					else					/* unsigned */
/*TODO*///					{
/*TODO*///						quotient = dividend / divisor;
/*TODO*///						if(quotient > 0xffffffff)
/*TODO*///						{
/*TODO*///							FLAG_V = VFLAG_SET;
/*TODO*///							return;
/*TODO*///						}
/*TODO*///						remainder = dividend % divisor;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else	/* 32 bit */
/*TODO*///				{
/*TODO*///					dividend = REG_D[(word2 >> 12) & 7];
/*TODO*///					if(BIT_B(word2))	   /* signed */
/*TODO*///					{
/*TODO*///						quotient  = (uint64)((sint64)((sint32)dividend) / (sint64)((sint32)divisor));
/*TODO*///						remainder = (uint64)((sint64)((sint32)dividend) % (sint64)((sint32)divisor));
/*TODO*///					}
/*TODO*///					else					/* unsigned */
/*TODO*///					{
/*TODO*///						quotient = dividend / divisor;
/*TODO*///						remainder = dividend % divisor;
/*TODO*///					}
/*TODO*///				}
/*TODO*///	
/*TODO*///				REG_D[word2 & 7] = remainder;
/*TODO*///				REG_D[(word2 >> 12) & 7] = quotient;
/*TODO*///	
/*TODO*///				FLAG_N = NFLAG_32(quotient);
/*TODO*///				FLAG_Z = quotient;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				FLAG_C = CFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			m68ki_exception_trap(EXCEPTION_ZERO_DIVIDE);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	
/*TODO*///	#else
/*TODO*///	
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint divisor = M68KMAKE_GET_OPER_AY_32;
/*TODO*///			uint dividend_hi = REG_D[word2 & 7];
/*TODO*///			uint dividend_lo = REG_D[(word2 >> 12) & 7];
/*TODO*///			uint quotient = 0;
/*TODO*///			uint remainder = 0;
/*TODO*///			uint dividend_neg = 0;
/*TODO*///			uint divisor_neg = 0;
/*TODO*///			sint i;
/*TODO*///			uint overflow;
/*TODO*///	
/*TODO*///			if(divisor != 0)
/*TODO*///			{
/*TODO*///				/* quad / long : long quotient, long remainder */
/*TODO*///				if(BIT_A(word2))
/*TODO*///				{
/*TODO*///					if(BIT_B(word2))	   /* signed */
/*TODO*///					{
/*TODO*///						/* special case in signed divide */
/*TODO*///						if(dividend_hi == 0 && dividend_lo == 0x80000000 && divisor == 0xffffffff)
/*TODO*///						{
/*TODO*///							REG_D[word2 & 7] = 0;
/*TODO*///							REG_D[(word2 >> 12) & 7] = 0x80000000;
/*TODO*///	
/*TODO*///							FLAG_N = NFLAG_SET;
/*TODO*///							FLAG_Z = ZFLAG_CLEAR;
/*TODO*///							FLAG_V = VFLAG_CLEAR;
/*TODO*///							FLAG_C = CFLAG_CLEAR;
/*TODO*///							return;
/*TODO*///						}
/*TODO*///						if(GET_MSB_32(dividend_hi))
/*TODO*///						{
/*TODO*///							dividend_neg = 1;
/*TODO*///							dividend_hi = (uint)MASK_OUT_ABOVE_32((-(sint)dividend_hi) - (dividend_lo != 0));
/*TODO*///							dividend_lo = (uint)MASK_OUT_ABOVE_32(-(sint)dividend_lo);
/*TODO*///						}
/*TODO*///						if(GET_MSB_32(divisor))
/*TODO*///						{
/*TODO*///							divisor_neg = 1;
/*TODO*///							divisor = (uint)MASK_OUT_ABOVE_32(-(sint)divisor);
/*TODO*///	
/*TODO*///						}
/*TODO*///					}
/*TODO*///	
/*TODO*///					/* if the upper long is greater than the divisor, we're overflowing. */
/*TODO*///					if(dividend_hi >= divisor)
/*TODO*///					{
/*TODO*///						FLAG_V = VFLAG_SET;
/*TODO*///						return;
/*TODO*///					}
/*TODO*///	
/*TODO*///					for(i = 31; i >= 0; i--)
/*TODO*///					{
/*TODO*///						quotient <<= 1;
/*TODO*///						remainder = (remainder << 1) + ((dividend_hi >> i) & 1);
/*TODO*///						if(remainder >= divisor)
/*TODO*///						{
/*TODO*///							remainder -= divisor;
/*TODO*///							quotient++;
/*TODO*///						}
/*TODO*///					}
/*TODO*///					for(i = 31; i >= 0; i--)
/*TODO*///					{
/*TODO*///						quotient <<= 1;
/*TODO*///						overflow = GET_MSB_32(remainder);
/*TODO*///						remainder = (remainder << 1) + ((dividend_lo >> i) & 1);
/*TODO*///						if(remainder >= divisor || overflow)
/*TODO*///						{
/*TODO*///							remainder -= divisor;
/*TODO*///							quotient++;
/*TODO*///						}
/*TODO*///					}
/*TODO*///	
/*TODO*///					if(BIT_B(word2))	   /* signed */
/*TODO*///					{
/*TODO*///						if(quotient > 0x7fffffff)
/*TODO*///						{
/*TODO*///							FLAG_V = VFLAG_SET;
/*TODO*///							return;
/*TODO*///						}
/*TODO*///						if (dividend_neg != 0)
/*TODO*///						{
/*TODO*///							remainder = (uint)MASK_OUT_ABOVE_32(-(sint)remainder);
/*TODO*///							quotient = (uint)MASK_OUT_ABOVE_32(-(sint)quotient);
/*TODO*///						}
/*TODO*///						if (divisor_neg != 0)
/*TODO*///							quotient = (uint)MASK_OUT_ABOVE_32(-(sint)quotient);
/*TODO*///					}
/*TODO*///	
/*TODO*///					REG_D[word2 & 7] = remainder;
/*TODO*///					REG_D[(word2 >> 12) & 7] = quotient;
/*TODO*///	
/*TODO*///					FLAG_N = NFLAG_32(quotient);
/*TODO*///					FLAG_Z = quotient;
/*TODO*///					FLAG_V = VFLAG_CLEAR;
/*TODO*///					FLAG_C = CFLAG_CLEAR;
/*TODO*///					return;
/*TODO*///				}
/*TODO*///	
/*TODO*///				/* long / long: long quotient, maybe long remainder */
/*TODO*///				if(BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					/* Special case in divide */
/*TODO*///					if(dividend_lo == 0x80000000 && divisor == 0xffffffff)
/*TODO*///					{
/*TODO*///						FLAG_N = NFLAG_SET;
/*TODO*///						FLAG_Z = ZFLAG_CLEAR;
/*TODO*///						FLAG_V = VFLAG_CLEAR;
/*TODO*///						FLAG_C = CFLAG_CLEAR;
/*TODO*///						REG_D[(word2 >> 12) & 7] = 0x80000000;
/*TODO*///						REG_D[word2 & 7] = 0;
/*TODO*///						return;
/*TODO*///					}
/*TODO*///					REG_D[word2 & 7] = MAKE_INT_32(dividend_lo) % MAKE_INT_32(divisor);
/*TODO*///					quotient = REG_D[(word2 >> 12) & 7] = MAKE_INT_32(dividend_lo) / MAKE_INT_32(divisor);
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					REG_D[word2 & 7] = MASK_OUT_ABOVE_32(dividend_lo) % MASK_OUT_ABOVE_32(divisor);
/*TODO*///					quotient = REG_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(dividend_lo) / MASK_OUT_ABOVE_32(divisor);
/*TODO*///				}
/*TODO*///	
/*TODO*///				FLAG_N = NFLAG_32(quotient);
/*TODO*///				FLAG_Z = quotient;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				FLAG_C = CFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			m68ki_exception_trap(EXCEPTION_ZERO_DIVIDE);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	
/*TODO*///	#endif
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(eor, 8, ., d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_8(DY /*TODO*///= MASK_OUT_ABOVE_8(DX));
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(eor, 8, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint res = MASK_OUT_ABOVE_8(DX /*TODO*/// m68ki_read_8(ea));
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(eor, 16, ., d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(DY /*TODO*///= MASK_OUT_ABOVE_16(DX));
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(eor, 16, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(DX /*TODO*/// m68ki_read_16(ea));
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(eor, 32, ., d)
/*TODO*///	{
/*TODO*///		uint res = DY /*TODO*///= DX;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(eor, 32, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///		uint res = DX /*TODO*/// m68ki_read_32(ea);
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(eori, 8, ., d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_8(DY /*TODO*///= OPER_I_8());
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(eori, 8, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_8();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint res = src /*TODO*/// m68ki_read_8(ea);
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(eori, 16, ., d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(DY /*TODO*///= OPER_I_16());
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(eori, 16, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_16();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint res = src /*TODO*/// m68ki_read_16(ea);
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(eori, 32, ., d)
/*TODO*///	{
/*TODO*///		uint res = DY /*TODO*///= OPER_I_32();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(eori, 32, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_32();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///		uint res = src /*TODO*/// m68ki_read_32(ea);
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(eori, 16, toc, .)
/*TODO*///	{
/*TODO*///		m68ki_set_ccr(m68ki_get_ccr() /*TODO*/// OPER_I_16());
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(eori, 16, tos, .)
/*TODO*///	{
/*TODO*///		if (FLAG_S != 0)
/*TODO*///		{
/*TODO*///			uint src = OPER_I_16();
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_set_sr(m68ki_get_sr() /*TODO*/// src);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_privilege_violation();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(exg, 32, dd, .)
/*TODO*///	{
/*TODO*///		uint* reg_a = &DX;
/*TODO*///		uint* reg_b = &DY;
/*TODO*///		uint tmp = *reg_a;
/*TODO*///		*reg_a = *reg_b;
/*TODO*///		*reg_b = tmp;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(exg, 32, aa, .)
/*TODO*///	{
/*TODO*///		uint* reg_a = &AX;
/*TODO*///		uint* reg_b = &AY;
/*TODO*///		uint tmp = *reg_a;
/*TODO*///		*reg_a = *reg_b;
/*TODO*///		*reg_b = tmp;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(exg, 32, da, .)
/*TODO*///	{
/*TODO*///		uint* reg_a = &DX;
/*TODO*///		uint* reg_b = &AY;
/*TODO*///		uint tmp = *reg_a;
/*TODO*///		*reg_a = *reg_b;
/*TODO*///		*reg_b = tmp;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(ext, 16, ., .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | MASK_OUT_ABOVE_8(*r_dst) | (GET_MSB_8(*r_dst) ? 0xff00 : 0);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(*r_dst);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(ext, 32, ., .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_ABOVE_16(*r_dst) | (GET_MSB_16(*r_dst) ? 0xffff0000 : 0);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(*r_dst);
/*TODO*///		FLAG_Z = *r_dst;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(extb, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint* r_dst = &DY;
/*TODO*///	
/*TODO*///			*r_dst = MASK_OUT_ABOVE_8(*r_dst) | (GET_MSB_8(*r_dst) ? 0xffffff00 : 0);
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_32(*r_dst);
/*TODO*///			FLAG_Z = *r_dst;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(illegal, 0, ., .)
/*TODO*///	{
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	M68KMAKE_OP(jmp, 32, ., .)
/*TODO*///	{
/*TODO*///		m68ki_jump(M68KMAKE_GET_EA_AY_32);
/*TODO*///		m68ki_trace_t0();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///		if(REG_PC == REG_PPC)
/*TODO*///			USE_ALL_CYCLES();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(jsr, 32, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///		m68ki_trace_t0();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_push_32(REG_PC);
/*TODO*///		m68ki_jump(ea);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(lea, 32, ., .)
/*TODO*///	{
/*TODO*///		AX = M68KMAKE_GET_EA_AY_32;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(link, 16, ., a7)
/*TODO*///	{
/*TODO*///		REG_A[7] -= 4;
/*TODO*///		m68ki_write_32(REG_A[7], REG_A[7]);
/*TODO*///		REG_A[7] = MASK_OUT_ABOVE_32(REG_A[7] + MAKE_INT_16(OPER_I_16()));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(link, 16, ., .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AY;
/*TODO*///	
/*TODO*///		m68ki_push_32(*r_dst);
/*TODO*///		*r_dst = REG_A[7];
/*TODO*///		REG_A[7] = MASK_OUT_ABOVE_32(REG_A[7] + MAKE_INT_16(OPER_I_16()));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(link, 32, ., a7)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			REG_A[7] -= 4;
/*TODO*///			m68ki_write_32(REG_A[7], REG_A[7]);
/*TODO*///			REG_A[7] = MASK_OUT_ABOVE_32(REG_A[7] + OPER_I_32());
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(link, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint* r_dst = &AY;
/*TODO*///	
/*TODO*///			m68ki_push_32(*r_dst);
/*TODO*///			*r_dst = REG_A[7];
/*TODO*///			REG_A[7] = MASK_OUT_ABOVE_32(REG_A[7] + OPER_I_32());
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(lsr, 8, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = src >> shift;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_CLEAR;
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_X = FLAG_C = src << (9-shift);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(lsr, 16, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = src >> shift;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_CLEAR;
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_X = FLAG_C = src << (9-shift);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(lsr, 32, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = *r_dst;
/*TODO*///		uint res = src >> shift;
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_CLEAR;
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_X = FLAG_C = src << (9-shift);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(lsr, 8, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = DX & 0x3f;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = src >> shift;
/*TODO*///	
/*TODO*///		if(shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			if(shift <= 8)
/*TODO*///			{
/*TODO*///				*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///				FLAG_X = FLAG_C = src << (9-shift);
/*TODO*///				FLAG_N = NFLAG_CLEAR;
/*TODO*///				FLAG_Z = res;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			*r_dst &= 0xffffff00;
/*TODO*///			FLAG_X = XFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			FLAG_N = NFLAG_CLEAR;
/*TODO*///			FLAG_Z = ZFLAG_SET;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_8(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(lsr, 16, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = DX & 0x3f;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = src >> shift;
/*TODO*///	
/*TODO*///		if(shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			if(shift <= 16)
/*TODO*///			{
/*TODO*///				*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///				FLAG_C = FLAG_X = (src >> (shift - 1))<<8;
/*TODO*///				FLAG_N = NFLAG_CLEAR;
/*TODO*///				FLAG_Z = res;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			*r_dst &= 0xffff0000;
/*TODO*///			FLAG_X = XFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			FLAG_N = NFLAG_CLEAR;
/*TODO*///			FLAG_Z = ZFLAG_SET;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_16(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(lsr, 32, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = DX & 0x3f;
/*TODO*///		uint src = *r_dst;
/*TODO*///		uint res = src >> shift;
/*TODO*///	
/*TODO*///		if(shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			if(shift < 32)
/*TODO*///			{
/*TODO*///				*r_dst = res;
/*TODO*///				FLAG_C = FLAG_X = (src >> (shift - 1))<<8;
/*TODO*///				FLAG_N = NFLAG_CLEAR;
/*TODO*///				FLAG_Z = res;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			*r_dst = 0;
/*TODO*///			FLAG_X = FLAG_C = (shift == 32 ? GET_MSB_32(src)>>23 : 0);
/*TODO*///			FLAG_N = NFLAG_CLEAR;
/*TODO*///			FLAG_Z = ZFLAG_SET;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_32(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(lsr, 16, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint src = m68ki_read_16(ea);
/*TODO*///		uint res = src >> 1;
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_CLEAR;
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = FLAG_X = src << 8;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(lsl, 8, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = MASK_OUT_ABOVE_8(src << shift);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_X = FLAG_C = src << shift;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(lsl, 16, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = MASK_OUT_ABOVE_16(src << shift);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_X = FLAG_C = src >> (8-shift);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(lsl, 32, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = *r_dst;
/*TODO*///		uint res = MASK_OUT_ABOVE_32(src << shift);
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_X = FLAG_C = src >> (24-shift);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(lsl, 8, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = DX & 0x3f;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = MASK_OUT_ABOVE_8(src << shift);
/*TODO*///	
/*TODO*///		if(shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			if(shift <= 8)
/*TODO*///			{
/*TODO*///				*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///				FLAG_X = FLAG_C = src << shift;
/*TODO*///				FLAG_N = NFLAG_8(res);
/*TODO*///				FLAG_Z = res;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			*r_dst &= 0xffffff00;
/*TODO*///			FLAG_X = XFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			FLAG_N = NFLAG_CLEAR;
/*TODO*///			FLAG_Z = ZFLAG_SET;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_8(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(lsl, 16, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = DX & 0x3f;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = MASK_OUT_ABOVE_16(src << shift);
/*TODO*///	
/*TODO*///		if(shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			if(shift <= 16)
/*TODO*///			{
/*TODO*///				*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///				FLAG_X = FLAG_C = (src << shift) >> 8;
/*TODO*///				FLAG_N = NFLAG_16(res);
/*TODO*///				FLAG_Z = res;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			*r_dst &= 0xffff0000;
/*TODO*///			FLAG_X = XFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			FLAG_N = NFLAG_CLEAR;
/*TODO*///			FLAG_Z = ZFLAG_SET;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_16(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(lsl, 32, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = DX & 0x3f;
/*TODO*///		uint src = *r_dst;
/*TODO*///		uint res = MASK_OUT_ABOVE_32(src << shift);
/*TODO*///	
/*TODO*///		if(shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			if(shift < 32)
/*TODO*///			{
/*TODO*///				*r_dst = res;
/*TODO*///				FLAG_X = FLAG_C = (src >> (32 - shift)) << 8;
/*TODO*///				FLAG_N = NFLAG_32(res);
/*TODO*///				FLAG_Z = res;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			*r_dst = 0;
/*TODO*///			FLAG_X = FLAG_C = ((shift == 32 ? src & 1 : 0))<<8;
/*TODO*///			FLAG_N = NFLAG_CLEAR;
/*TODO*///			FLAG_Z = ZFLAG_SET;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_32(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(lsl, 16, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint src = m68ki_read_16(ea);
/*TODO*///		uint res = MASK_OUT_ABOVE_16(src << 1);
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_X = FLAG_C = src >> 7;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, d, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///		uint* r_dst = &DX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, d, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_8;
/*TODO*///		uint* r_dst = &DX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, ai, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///		uint ea = EA_AX_AI_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, ai, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_8;
/*TODO*///		uint ea = EA_AX_AI_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, pi7, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///		uint ea = EA_A7_PI_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, pi, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///		uint ea = EA_AX_PI_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, pi7, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_8;
/*TODO*///		uint ea = EA_A7_PI_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, pi, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_8;
/*TODO*///		uint ea = EA_AX_PI_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, pd7, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///		uint ea = EA_A7_PD_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, pd, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///		uint ea = EA_AX_PD_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, pd7, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_8;
/*TODO*///		uint ea = EA_A7_PD_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, pd, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_8;
/*TODO*///		uint ea = EA_AX_PD_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, di, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///		uint ea = EA_AX_DI_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, di, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_8;
/*TODO*///		uint ea = EA_AX_DI_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, ix, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///		uint ea = EA_AX_IX_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, ix, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_8;
/*TODO*///		uint ea = EA_AX_IX_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, aw, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///		uint ea = EA_AW_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, aw, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_8;
/*TODO*///		uint ea = EA_AW_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, al, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///		uint ea = EA_AL_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 8, al, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_8;
/*TODO*///		uint ea = EA_AL_8();
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, d, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///		uint* r_dst = &DX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, d, a)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(AY);
/*TODO*///		uint* r_dst = &DX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, d, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_16;
/*TODO*///		uint* r_dst = &DX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, ai, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///		uint ea = EA_AX_AI_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, ai, a)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(AY);
/*TODO*///		uint ea = EA_AX_AI_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, ai, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_16;
/*TODO*///		uint ea = EA_AX_AI_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, pi, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///		uint ea = EA_AX_PI_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, pi, a)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(AY);
/*TODO*///		uint ea = EA_AX_PI_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, pi, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_16;
/*TODO*///		uint ea = EA_AX_PI_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, pd, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///		uint ea = EA_AX_PD_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, pd, a)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(AY);
/*TODO*///		uint ea = EA_AX_PD_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, pd, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_16;
/*TODO*///		uint ea = EA_AX_PD_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, di, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///		uint ea = EA_AX_DI_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, di, a)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(AY);
/*TODO*///		uint ea = EA_AX_DI_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, di, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_16;
/*TODO*///		uint ea = EA_AX_DI_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, ix, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///		uint ea = EA_AX_IX_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, ix, a)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(AY);
/*TODO*///		uint ea = EA_AX_IX_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, ix, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_16;
/*TODO*///		uint ea = EA_AX_IX_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, aw, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///		uint ea = EA_AW_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, aw, a)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(AY);
/*TODO*///		uint ea = EA_AW_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, aw, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_16;
/*TODO*///		uint ea = EA_AW_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, al, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///		uint ea = EA_AL_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, al, a)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(AY);
/*TODO*///		uint ea = EA_AL_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, al, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_16;
/*TODO*///		uint ea = EA_AL_16();
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, d, d)
/*TODO*///	{
/*TODO*///		uint res = DY;
/*TODO*///		uint* r_dst = &DX;
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, d, a)
/*TODO*///	{
/*TODO*///		uint res = AY;
/*TODO*///		uint* r_dst = &DX;
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, d, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_32;
/*TODO*///		uint* r_dst = &DX;
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, ai, d)
/*TODO*///	{
/*TODO*///		uint res = DY;
/*TODO*///		uint ea = EA_AX_AI_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, ai, a)
/*TODO*///	{
/*TODO*///		uint res = AY;
/*TODO*///		uint ea = EA_AX_AI_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, ai, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_32;
/*TODO*///		uint ea = EA_AX_AI_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, pi, d)
/*TODO*///	{
/*TODO*///		uint res = DY;
/*TODO*///		uint ea = EA_AX_PI_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, pi, a)
/*TODO*///	{
/*TODO*///		uint res = AY;
/*TODO*///		uint ea = EA_AX_PI_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, pi, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_32;
/*TODO*///		uint ea = EA_AX_PI_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, pd, d)
/*TODO*///	{
/*TODO*///		uint res = DY;
/*TODO*///		uint ea = EA_AX_PD_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, pd, a)
/*TODO*///	{
/*TODO*///		uint res = AY;
/*TODO*///		uint ea = EA_AX_PD_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, pd, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_32;
/*TODO*///		uint ea = EA_AX_PD_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, di, d)
/*TODO*///	{
/*TODO*///		uint res = DY;
/*TODO*///		uint ea = EA_AX_DI_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, di, a)
/*TODO*///	{
/*TODO*///		uint res = AY;
/*TODO*///		uint ea = EA_AX_DI_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, di, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_32;
/*TODO*///		uint ea = EA_AX_DI_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, ix, d)
/*TODO*///	{
/*TODO*///		uint res = DY;
/*TODO*///		uint ea = EA_AX_IX_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, ix, a)
/*TODO*///	{
/*TODO*///		uint res = AY;
/*TODO*///		uint ea = EA_AX_IX_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, ix, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_32;
/*TODO*///		uint ea = EA_AX_IX_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, aw, d)
/*TODO*///	{
/*TODO*///		uint res = DY;
/*TODO*///		uint ea = EA_AW_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, aw, a)
/*TODO*///	{
/*TODO*///		uint res = AY;
/*TODO*///		uint ea = EA_AW_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, aw, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_32;
/*TODO*///		uint ea = EA_AW_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, al, d)
/*TODO*///	{
/*TODO*///		uint res = DY;
/*TODO*///		uint ea = EA_AL_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, al, a)
/*TODO*///	{
/*TODO*///		uint res = AY;
/*TODO*///		uint ea = EA_AL_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, al, .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_32;
/*TODO*///		uint ea = EA_AL_32();
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movea, 16, ., d)
/*TODO*///	{
/*TODO*///		AX = MAKE_INT_16(DY);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movea, 16, ., a)
/*TODO*///	{
/*TODO*///		AX = MAKE_INT_16(AY);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movea, 16, ., .)
/*TODO*///	{
/*TODO*///		AX = MAKE_INT_16(M68KMAKE_GET_OPER_AY_16);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movea, 32, ., d)
/*TODO*///	{
/*TODO*///		AX = DY;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movea, 32, ., a)
/*TODO*///	{
/*TODO*///		AX = AY;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movea, 32, ., .)
/*TODO*///	{
/*TODO*///		AX = M68KMAKE_GET_OPER_AY_32;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, frc, d)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_010_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			DY = MASK_OUT_BELOW_16(DY) | m68ki_get_ccr();
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, frc, .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_010_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			m68ki_write_16(M68KMAKE_GET_EA_AY_16, m68ki_get_ccr());
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, toc, d)
/*TODO*///	{
/*TODO*///		m68ki_set_ccr(DY);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, toc, .)
/*TODO*///	{
/*TODO*///		m68ki_set_ccr(M68KMAKE_GET_OPER_AY_16);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, frs, d)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_000(CPU_TYPE) || FLAG_S)	/* NS990408 */
/*TODO*///		{
/*TODO*///			DY = MASK_OUT_BELOW_16(DY) | m68ki_get_sr();
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_privilege_violation();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, frs, .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_000(CPU_TYPE) || FLAG_S)	/* NS990408 */
/*TODO*///		{
/*TODO*///			uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///			m68ki_write_16(ea, m68ki_get_sr());
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_privilege_violation();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, tos, d)
/*TODO*///	{
/*TODO*///		if (FLAG_S != 0)
/*TODO*///		{
/*TODO*///			m68ki_set_sr(DY);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_privilege_violation();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 16, tos, .)
/*TODO*///	{
/*TODO*///		if (FLAG_S != 0)
/*TODO*///		{
/*TODO*///			uint new_sr = M68KMAKE_GET_OPER_AY_16;
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_set_sr(new_sr);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_privilege_violation();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, fru, .)
/*TODO*///	{
/*TODO*///		if (FLAG_S != 0)
/*TODO*///		{
/*TODO*///			AY = REG_USP;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_privilege_violation();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(move, 32, tou, .)
/*TODO*///	{
/*TODO*///		if (FLAG_S != 0)
/*TODO*///		{
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			REG_USP = AY;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_privilege_violation();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movec, 32, cr, .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_010_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			if (FLAG_S != 0)
/*TODO*///			{
/*TODO*///				uint word2 = OPER_I_16();
/*TODO*///	
/*TODO*///				m68ki_trace_t0();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///				switch (word2 & 0xfff)
/*TODO*///				{
/*TODO*///				case 0x000:			   /* SFC */
/*TODO*///					REG_DA[(word2 >> 12) & 15] = REG_SFC;
/*TODO*///					return;
/*TODO*///				case 0x001:			   /* DFC */
/*TODO*///					REG_DA[(word2 >> 12) & 15] = REG_DFC;
/*TODO*///					return;
/*TODO*///				case 0x002:			   /* CACR */
/*TODO*///					if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///					{
/*TODO*///						REG_DA[(word2 >> 12) & 15] = REG_CACR;
/*TODO*///						return;
/*TODO*///					}
/*TODO*///					return;
/*TODO*///				case 0x800:			   /* USP */
/*TODO*///					REG_DA[(word2 >> 12) & 15] = REG_USP;
/*TODO*///					return;
/*TODO*///				case 0x801:			   /* VBR */
/*TODO*///					REG_DA[(word2 >> 12) & 15] = REG_VBR;
/*TODO*///					return;
/*TODO*///				case 0x802:			   /* CAAR */
/*TODO*///					if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///					{
/*TODO*///						REG_DA[(word2 >> 12) & 15] = REG_CAAR;
/*TODO*///						return;
/*TODO*///					}
/*TODO*///					m68ki_exception_illegal();
/*TODO*///					break;
/*TODO*///				case 0x803:			   /* MSP */
/*TODO*///					if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///					{
/*TODO*///						REG_DA[(word2 >> 12) & 15] = FLAG_M ? REG_SP : REG_MSP;
/*TODO*///						return;
/*TODO*///					}
/*TODO*///					m68ki_exception_illegal();
/*TODO*///					return;
/*TODO*///				case 0x804:			   /* ISP */
/*TODO*///					if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///					{
/*TODO*///						REG_DA[(word2 >> 12) & 15] = FLAG_M ? REG_ISP : REG_SP;
/*TODO*///						return;
/*TODO*///					}
/*TODO*///					m68ki_exception_illegal();
/*TODO*///					return;
/*TODO*///				default:
/*TODO*///					m68ki_exception_illegal();
/*TODO*///					return;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			m68ki_exception_privilege_violation();
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movec, 32, rc, .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_010_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			if (FLAG_S != 0)
/*TODO*///			{
/*TODO*///				uint word2 = OPER_I_16();
/*TODO*///	
/*TODO*///				m68ki_trace_t0();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///				switch (word2 & 0xfff)
/*TODO*///				{
/*TODO*///				case 0x000:			   /* SFC */
/*TODO*///					REG_SFC = REG_DA[(word2 >> 12) & 15] & 7;
/*TODO*///					return;
/*TODO*///				case 0x001:			   /* DFC */
/*TODO*///					REG_DFC = REG_DA[(word2 >> 12) & 15] & 7;
/*TODO*///					return;
/*TODO*///				case 0x002:			   /* CACR */
/*TODO*///					if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///					{
/*TODO*///						REG_CACR = REG_DA[(word2 >> 12) & 15];
/*TODO*///						return;
/*TODO*///					}
/*TODO*///					m68ki_exception_illegal();
/*TODO*///					return;
/*TODO*///				case 0x800:			   /* USP */
/*TODO*///					REG_USP = REG_DA[(word2 >> 12) & 15];
/*TODO*///					return;
/*TODO*///				case 0x801:			   /* VBR */
/*TODO*///					REG_VBR = REG_DA[(word2 >> 12) & 15];
/*TODO*///					return;
/*TODO*///				case 0x802:			   /* CAAR */
/*TODO*///					if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///					{
/*TODO*///						REG_CAAR = REG_DA[(word2 >> 12) & 15];
/*TODO*///						return;
/*TODO*///					}
/*TODO*///					m68ki_exception_illegal();
/*TODO*///					return;
/*TODO*///				case 0x803:			   /* MSP */
/*TODO*///					if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///					{
/*TODO*///						/* we are in supervisor mode so just check for M flag */
/*TODO*///						if(!FLAG_M)
/*TODO*///						{
/*TODO*///							REG_MSP = REG_DA[(word2 >> 12) & 15];
/*TODO*///							return;
/*TODO*///						}
/*TODO*///						REG_SP = REG_DA[(word2 >> 12) & 15];
/*TODO*///						return;
/*TODO*///					}
/*TODO*///					m68ki_exception_illegal();
/*TODO*///					return;
/*TODO*///				case 0x804:			   /* ISP */
/*TODO*///					if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///					{
/*TODO*///						if(!FLAG_M)
/*TODO*///						{
/*TODO*///							REG_SP = REG_DA[(word2 >> 12) & 15];
/*TODO*///							return;
/*TODO*///						}
/*TODO*///						REG_ISP = REG_DA[(word2 >> 12) & 15];
/*TODO*///						return;
/*TODO*///					}
/*TODO*///					m68ki_exception_illegal();
/*TODO*///					return;
/*TODO*///				default:
/*TODO*///					m68ki_exception_illegal();
/*TODO*///					return;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			m68ki_exception_privilege_violation();
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movem, 16, re, pd)
/*TODO*///	{
/*TODO*///		uint i = 0;
/*TODO*///		uint register_list = OPER_I_16();
/*TODO*///		uint ea = AY;
/*TODO*///		uint count = 0;
/*TODO*///	
/*TODO*///		for(; i < 16; i++)
/*TODO*///			if(register_list & (1 << i))
/*TODO*///			{
/*TODO*///				ea -= 2;
/*TODO*///				m68ki_write_16(ea, MASK_OUT_ABOVE_16(REG_DA[15-i]));
/*TODO*///				count++;
/*TODO*///			}
/*TODO*///		AY = ea;
/*TODO*///	
/*TODO*///		USE_CYCLES(count<<CYC_MOVEM_W);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movem, 16, re, .)
/*TODO*///	{
/*TODO*///		uint i = 0;
/*TODO*///		uint register_list = OPER_I_16();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint count = 0;
/*TODO*///	
/*TODO*///		for(; i < 16; i++)
/*TODO*///			if(register_list & (1 << i))
/*TODO*///			{
/*TODO*///				m68ki_write_16(ea, MASK_OUT_ABOVE_16(REG_DA[i]));
/*TODO*///				ea += 2;
/*TODO*///				count++;
/*TODO*///			}
/*TODO*///	
/*TODO*///		USE_CYCLES(count<<CYC_MOVEM_W);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movem, 32, re, pd)
/*TODO*///	{
/*TODO*///		uint i = 0;
/*TODO*///		uint register_list = OPER_I_16();
/*TODO*///		uint ea = AY;
/*TODO*///		uint count = 0;
/*TODO*///	
/*TODO*///		for(; i < 16; i++)
/*TODO*///			if(register_list & (1 << i))
/*TODO*///			{
/*TODO*///				ea -= 4;
/*TODO*///				m68ki_write_32(ea, REG_DA[15-i]);
/*TODO*///				count++;
/*TODO*///			}
/*TODO*///		AY = ea;
/*TODO*///	
/*TODO*///		USE_CYCLES(count<<CYC_MOVEM_L);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movem, 32, re, .)
/*TODO*///	{
/*TODO*///		uint i = 0;
/*TODO*///		uint register_list = OPER_I_16();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///		uint count = 0;
/*TODO*///	
/*TODO*///		for(; i < 16; i++)
/*TODO*///			if(register_list & (1 << i))
/*TODO*///			{
/*TODO*///				m68ki_write_32(ea, REG_DA[i]);
/*TODO*///				ea += 4;
/*TODO*///				count++;
/*TODO*///			}
/*TODO*///	
/*TODO*///		USE_CYCLES(count<<CYC_MOVEM_L);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movem, 16, er, pi)
/*TODO*///	{
/*TODO*///		uint i = 0;
/*TODO*///		uint register_list = OPER_I_16();
/*TODO*///		uint ea = AY;
/*TODO*///		uint count = 0;
/*TODO*///	
/*TODO*///		for(; i < 16; i++)
/*TODO*///			if(register_list & (1 << i))
/*TODO*///			{
/*TODO*///				REG_DA[i] = MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_16(ea)));
/*TODO*///				ea += 2;
/*TODO*///				count++;
/*TODO*///			}
/*TODO*///		AY = ea;
/*TODO*///	
/*TODO*///		USE_CYCLES(count<<CYC_MOVEM_W);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movem, 16, er, pcdi)
/*TODO*///	{
/*TODO*///		uint i = 0;
/*TODO*///		uint register_list = OPER_I_16();
/*TODO*///		uint ea = EA_PCDI_16();
/*TODO*///		uint count = 0;
/*TODO*///	
/*TODO*///		for(; i < 16; i++)
/*TODO*///			if(register_list & (1 << i))
/*TODO*///			{
/*TODO*///				REG_DA[i] = MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_pcrel_16(ea)));
/*TODO*///				ea += 2;
/*TODO*///				count++;
/*TODO*///			}
/*TODO*///	
/*TODO*///		USE_CYCLES(count<<CYC_MOVEM_W);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movem, 16, er, pcix)
/*TODO*///	{
/*TODO*///		uint i = 0;
/*TODO*///		uint register_list = OPER_I_16();
/*TODO*///		uint ea = EA_PCIX_16();
/*TODO*///		uint count = 0;
/*TODO*///	
/*TODO*///		for(; i < 16; i++)
/*TODO*///			if(register_list & (1 << i))
/*TODO*///			{
/*TODO*///				REG_DA[i] = MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_pcrel_16(ea)));
/*TODO*///				ea += 2;
/*TODO*///				count++;
/*TODO*///			}
/*TODO*///	
/*TODO*///		USE_CYCLES(count<<CYC_MOVEM_W);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movem, 16, er, .)
/*TODO*///	{
/*TODO*///		uint i = 0;
/*TODO*///		uint register_list = OPER_I_16();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint count = 0;
/*TODO*///	
/*TODO*///		for(; i < 16; i++)
/*TODO*///			if(register_list & (1 << i))
/*TODO*///			{
/*TODO*///				REG_DA[i] = MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_16(ea)));
/*TODO*///				ea += 2;
/*TODO*///				count++;
/*TODO*///			}
/*TODO*///	
/*TODO*///		USE_CYCLES(count<<CYC_MOVEM_W);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movem, 32, er, pi)
/*TODO*///	{
/*TODO*///		uint i = 0;
/*TODO*///		uint register_list = OPER_I_16();
/*TODO*///		uint ea = AY;
/*TODO*///		uint count = 0;
/*TODO*///	
/*TODO*///		for(; i < 16; i++)
/*TODO*///			if(register_list & (1 << i))
/*TODO*///			{
/*TODO*///				REG_DA[i] = m68ki_read_32(ea);
/*TODO*///				ea += 4;
/*TODO*///				count++;
/*TODO*///			}
/*TODO*///		AY = ea;
/*TODO*///	
/*TODO*///		USE_CYCLES(count<<CYC_MOVEM_L);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movem, 32, er, pcdi)
/*TODO*///	{
/*TODO*///		uint i = 0;
/*TODO*///		uint register_list = OPER_I_16();
/*TODO*///		uint ea = EA_PCDI_32();
/*TODO*///		uint count = 0;
/*TODO*///	
/*TODO*///		for(; i < 16; i++)
/*TODO*///			if(register_list & (1 << i))
/*TODO*///			{
/*TODO*///				REG_DA[i] = m68ki_read_pcrel_32(ea);
/*TODO*///				ea += 4;
/*TODO*///				count++;
/*TODO*///			}
/*TODO*///	
/*TODO*///		USE_CYCLES(count<<CYC_MOVEM_L);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movem, 32, er, pcix)
/*TODO*///	{
/*TODO*///		uint i = 0;
/*TODO*///		uint register_list = OPER_I_16();
/*TODO*///		uint ea = EA_PCIX_32();
/*TODO*///		uint count = 0;
/*TODO*///	
/*TODO*///		for(; i < 16; i++)
/*TODO*///			if(register_list & (1 << i))
/*TODO*///			{
/*TODO*///				REG_DA[i] = m68ki_read_pcrel_32(ea);
/*TODO*///				ea += 4;
/*TODO*///				count++;
/*TODO*///			}
/*TODO*///	
/*TODO*///		USE_CYCLES(count<<CYC_MOVEM_L);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movem, 32, er, .)
/*TODO*///	{
/*TODO*///		uint i = 0;
/*TODO*///		uint register_list = OPER_I_16();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///		uint count = 0;
/*TODO*///	
/*TODO*///		for(; i < 16; i++)
/*TODO*///			if(register_list & (1 << i))
/*TODO*///			{
/*TODO*///				REG_DA[i] = m68ki_read_32(ea);
/*TODO*///				ea += 4;
/*TODO*///				count++;
/*TODO*///			}
/*TODO*///	
/*TODO*///		USE_CYCLES(count<<CYC_MOVEM_L);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movep, 16, re, .)
/*TODO*///	{
/*TODO*///		uint ea = EA_AY_DI_16();
/*TODO*///		uint src = DX;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, MASK_OUT_ABOVE_8(src >> 8));
/*TODO*///		m68ki_write_8(ea += 2, MASK_OUT_ABOVE_8(src));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movep, 32, re, .)
/*TODO*///	{
/*TODO*///		uint ea = EA_AY_DI_32();
/*TODO*///		uint src = DX;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, MASK_OUT_ABOVE_8(src >> 24));
/*TODO*///		m68ki_write_8(ea += 2, MASK_OUT_ABOVE_8(src >> 16));
/*TODO*///		m68ki_write_8(ea += 2, MASK_OUT_ABOVE_8(src >> 8));
/*TODO*///		m68ki_write_8(ea += 2, MASK_OUT_ABOVE_8(src));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movep, 16, er, .)
/*TODO*///	{
/*TODO*///		uint ea = EA_AY_DI_16();
/*TODO*///		uint* r_dst = &DX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | ((m68ki_read_8(ea) << 8) + m68ki_read_8(ea + 2));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(movep, 32, er, .)
/*TODO*///	{
/*TODO*///		uint ea = EA_AY_DI_32();
/*TODO*///	
/*TODO*///		DX = (m68ki_read_8(ea) << 24) + (m68ki_read_8(ea + 2) << 16)
/*TODO*///			+ (m68ki_read_8(ea + 4) << 8) + m68ki_read_8(ea + 6);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(moves, 8, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_010_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			if (FLAG_S != 0)
/*TODO*///			{
/*TODO*///				uint word2 = OPER_I_16();
/*TODO*///				uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///	
/*TODO*///				m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///				if(BIT_B(word2))		   /* Register to memory */
/*TODO*///				{
/*TODO*///					m68ki_write_8_fc(ea, REG_DFC, MASK_OUT_ABOVE_8(REG_DA[(word2 >> 12) & 15]));
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				if(BIT_F(word2))		   /* Memory to address register */
/*TODO*///				{
/*TODO*///					REG_A[(word2 >> 12) & 7] = MAKE_INT_8(m68ki_read_8_fc(ea, REG_SFC));
/*TODO*///					if(CPU_TYPE_IS_020_VARIANT(CPU_TYPE))
/*TODO*///						USE_CYCLES(2);
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				/* Memory to data register */
/*TODO*///				REG_D[(word2 >> 12) & 7] = MASK_OUT_BELOW_8(REG_D[(word2 >> 12) & 7]) | m68ki_read_8_fc(ea, REG_SFC);
/*TODO*///				if(CPU_TYPE_IS_020_VARIANT(CPU_TYPE))
/*TODO*///					USE_CYCLES(2);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			m68ki_exception_privilege_violation();
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(moves, 16, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_010_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			if (FLAG_S != 0)
/*TODO*///			{
/*TODO*///				uint word2 = OPER_I_16();
/*TODO*///				uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///	
/*TODO*///				m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///				if(BIT_B(word2))		   /* Register to memory */
/*TODO*///				{
/*TODO*///					m68ki_write_16_fc(ea, REG_DFC, MASK_OUT_ABOVE_16(REG_DA[(word2 >> 12) & 15]));
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				if(BIT_F(word2))		   /* Memory to address register */
/*TODO*///				{
/*TODO*///					REG_A[(word2 >> 12) & 7] = MAKE_INT_16(m68ki_read_16_fc(ea, REG_SFC));
/*TODO*///					if(CPU_TYPE_IS_020_VARIANT(CPU_TYPE))
/*TODO*///						USE_CYCLES(2);
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				/* Memory to data register */
/*TODO*///				REG_D[(word2 >> 12) & 7] = MASK_OUT_BELOW_16(REG_D[(word2 >> 12) & 7]) | m68ki_read_16_fc(ea, REG_SFC);
/*TODO*///				if(CPU_TYPE_IS_020_VARIANT(CPU_TYPE))
/*TODO*///					USE_CYCLES(2);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			m68ki_exception_privilege_violation();
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(moves, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_010_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			if (FLAG_S != 0)
/*TODO*///			{
/*TODO*///				uint word2 = OPER_I_16();
/*TODO*///				uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///	
/*TODO*///				m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///				if(BIT_B(word2))		   /* Register to memory */
/*TODO*///				{
/*TODO*///					m68ki_write_32_fc(ea, REG_DFC, REG_DA[(word2 >> 12) & 15]);
/*TODO*///					if(CPU_TYPE_IS_020_VARIANT(CPU_TYPE))
/*TODO*///						USE_CYCLES(2);
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				/* Memory to register */
/*TODO*///				REG_DA[(word2 >> 12) & 15] = m68ki_read_32_fc(ea, REG_SFC);
/*TODO*///				if(CPU_TYPE_IS_020_VARIANT(CPU_TYPE))
/*TODO*///					USE_CYCLES(2);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			m68ki_exception_privilege_violation();
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(moveq, 32, ., .)
/*TODO*///	{
/*TODO*///		uint res = DX = MAKE_INT_8(MASK_OUT_ABOVE_8(REG_IR));
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(muls, 16, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint res = MASK_OUT_ABOVE_32(MAKE_INT_16(DY) * MAKE_INT_16(MASK_OUT_ABOVE_16(*r_dst)));
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(muls, 16, ., .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint res = MASK_OUT_ABOVE_32(MAKE_INT_16(M68KMAKE_GET_OPER_AY_16) * MAKE_INT_16(MASK_OUT_ABOVE_16(*r_dst)));
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(mulu, 16, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(DY) * MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(mulu, 16, ., .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_16 * MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(mull, 32, ., d)
/*TODO*///	{
/*TODO*///	#if M68K_USE_64_BIT
/*TODO*///	
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint64 src = DY;
/*TODO*///			uint64 dst = REG_D[(word2 >> 12) & 7];
/*TODO*///			uint64 res;
/*TODO*///	
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			if(BIT_B(word2))			   /* signed */
/*TODO*///			{
/*TODO*///				res = (sint64)((sint32)src) * (sint64)((sint32)dst);
/*TODO*///				if(!BIT_A(word2))
/*TODO*///				{
/*TODO*///					FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///					FLAG_N = NFLAG_32(res);
/*TODO*///					FLAG_V = ((sint64)res != (sint32)res)<<7;
/*TODO*///					REG_D[(word2 >> 12) & 7] = FLAG_Z;
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				FLAG_Z = MASK_OUT_ABOVE_32(res) | (res>>32);
/*TODO*///				FLAG_N = NFLAG_64(res);
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				REG_D[word2 & 7] = (res >> 32);
/*TODO*///				REG_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(res);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			res = src * dst;
/*TODO*///			if(!BIT_A(word2))
/*TODO*///			{
/*TODO*///				FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///				FLAG_N = NFLAG_32(res);
/*TODO*///				FLAG_V = (res > 0xffffffff)<<7;
/*TODO*///				REG_D[(word2 >> 12) & 7] = FLAG_Z;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_32(res) | (res>>32);
/*TODO*///			FLAG_N = NFLAG_64(res);
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			REG_D[word2 & 7] = (res >> 32);
/*TODO*///			REG_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(res);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	
/*TODO*///	#else
/*TODO*///	
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint src = DY;
/*TODO*///			uint dst = REG_D[(word2 >> 12) & 7];
/*TODO*///			uint neg = GET_MSB_32(src /*TODO*/// dst);
/*TODO*///			uint src1;
/*TODO*///			uint src2;
/*TODO*///			uint dst1;
/*TODO*///			uint dst2;
/*TODO*///			uint r1;
/*TODO*///			uint r2;
/*TODO*///			uint r3;
/*TODO*///			uint r4;
/*TODO*///			uint lo;
/*TODO*///			uint hi;
/*TODO*///	
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			if(BIT_B(word2))			   /* signed */
/*TODO*///			{
/*TODO*///				if(GET_MSB_32(src))
/*TODO*///					src = (uint)MASK_OUT_ABOVE_32(-(sint)src);
/*TODO*///				if(GET_MSB_32(dst))
/*TODO*///					dst = (uint)MASK_OUT_ABOVE_32(-(sint)dst);
/*TODO*///			}
/*TODO*///	
/*TODO*///			src1 = MASK_OUT_ABOVE_16(src);
/*TODO*///			src2 = src>>16;
/*TODO*///			dst1 = MASK_OUT_ABOVE_16(dst);
/*TODO*///			dst2 = dst>>16;
/*TODO*///	
/*TODO*///	
/*TODO*///			r1 = src1 * dst1;
/*TODO*///			r2 = src1 * dst2;
/*TODO*///			r3 = src2 * dst1;
/*TODO*///			r4 = src2 * dst2;
/*TODO*///	
/*TODO*///			lo = r1 + (MASK_OUT_ABOVE_16(r2)<<16) + (MASK_OUT_ABOVE_16(r3)<<16);
/*TODO*///			hi = r4 + (r2>>16) + (r3>>16) + (((r1>>16) + MASK_OUT_ABOVE_16(r2) + MASK_OUT_ABOVE_16(r3)) >> 16);
/*TODO*///	
/*TODO*///			if(BIT_B(word2) && neg)
/*TODO*///			{
/*TODO*///				hi = (uint)MASK_OUT_ABOVE_32((-(sint)hi) - (lo != 0));
/*TODO*///				lo = (uint)MASK_OUT_ABOVE_32(-(sint)lo);
/*TODO*///			}
/*TODO*///	
/*TODO*///			if(BIT_A(word2))
/*TODO*///			{
/*TODO*///				REG_D[word2 & 7] = hi;
/*TODO*///				REG_D[(word2 >> 12) & 7] = lo;
/*TODO*///				FLAG_N = NFLAG_32(hi);
/*TODO*///				FLAG_Z = hi | lo;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			REG_D[(word2 >> 12) & 7] = lo;
/*TODO*///			FLAG_N = NFLAG_32(lo);
/*TODO*///			FLAG_Z = lo;
/*TODO*///			if(BIT_B(word2))
/*TODO*///				FLAG_V = (!((GET_MSB_32(lo) && hi == 0xffffffff) || (!GET_MSB_32(lo) && !hi)))<<7;
/*TODO*///			else
/*TODO*///				FLAG_V = (hi != 0) << 7;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	
/*TODO*///	#endif
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(mull, 32, ., .)
/*TODO*///	{
/*TODO*///	#if M68K_USE_64_BIT
/*TODO*///	
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint64 src = M68KMAKE_GET_OPER_AY_32;
/*TODO*///			uint64 dst = REG_D[(word2 >> 12) & 7];
/*TODO*///			uint64 res;
/*TODO*///	
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			if(BIT_B(word2))			   /* signed */
/*TODO*///			{
/*TODO*///				res = (sint64)((sint32)src) * (sint64)((sint32)dst);
/*TODO*///				if(!BIT_A(word2))
/*TODO*///				{
/*TODO*///					FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///					FLAG_N = NFLAG_32(res);
/*TODO*///					FLAG_V = ((sint64)res != (sint32)res)<<7;
/*TODO*///					REG_D[(word2 >> 12) & 7] = FLAG_Z;
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				FLAG_Z = MASK_OUT_ABOVE_32(res) | (res>>32);
/*TODO*///				FLAG_N = NFLAG_64(res);
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				REG_D[word2 & 7] = (res >> 32);
/*TODO*///				REG_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(res);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			res = src * dst;
/*TODO*///			if(!BIT_A(word2))
/*TODO*///			{
/*TODO*///				FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///				FLAG_N = NFLAG_32(res);
/*TODO*///				FLAG_V = (res > 0xffffffff)<<7;
/*TODO*///				REG_D[(word2 >> 12) & 7] = FLAG_Z;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			FLAG_Z = MASK_OUT_ABOVE_32(res) | (res>>32);
/*TODO*///			FLAG_N = NFLAG_64(res);
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			REG_D[word2 & 7] = (res >> 32);
/*TODO*///			REG_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(res);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	
/*TODO*///	#else
/*TODO*///	
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint word2 = OPER_I_16();
/*TODO*///			uint src = M68KMAKE_GET_OPER_AY_32;
/*TODO*///			uint dst = REG_D[(word2 >> 12) & 7];
/*TODO*///			uint neg = GET_MSB_32(src /*TODO*/// dst);
/*TODO*///			uint src1;
/*TODO*///			uint src2;
/*TODO*///			uint dst1;
/*TODO*///			uint dst2;
/*TODO*///			uint r1;
/*TODO*///			uint r2;
/*TODO*///			uint r3;
/*TODO*///			uint r4;
/*TODO*///			uint lo;
/*TODO*///			uint hi;
/*TODO*///	
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///	
/*TODO*///			if(BIT_B(word2))			   /* signed */
/*TODO*///			{
/*TODO*///				if(GET_MSB_32(src))
/*TODO*///					src = (uint)MASK_OUT_ABOVE_32(-(sint)src);
/*TODO*///				if(GET_MSB_32(dst))
/*TODO*///					dst = (uint)MASK_OUT_ABOVE_32(-(sint)dst);
/*TODO*///			}
/*TODO*///	
/*TODO*///			src1 = MASK_OUT_ABOVE_16(src);
/*TODO*///			src2 = src>>16;
/*TODO*///			dst1 = MASK_OUT_ABOVE_16(dst);
/*TODO*///			dst2 = dst>>16;
/*TODO*///	
/*TODO*///	
/*TODO*///			r1 = src1 * dst1;
/*TODO*///			r2 = src1 * dst2;
/*TODO*///			r3 = src2 * dst1;
/*TODO*///			r4 = src2 * dst2;
/*TODO*///	
/*TODO*///			lo = r1 + (MASK_OUT_ABOVE_16(r2)<<16) + (MASK_OUT_ABOVE_16(r3)<<16);
/*TODO*///			hi = r4 + (r2>>16) + (r3>>16) + (((r1>>16) + MASK_OUT_ABOVE_16(r2) + MASK_OUT_ABOVE_16(r3)) >> 16);
/*TODO*///	
/*TODO*///			if(BIT_B(word2) && neg)
/*TODO*///			{
/*TODO*///				hi = (uint)MASK_OUT_ABOVE_32((-(sint)hi) - (lo != 0));
/*TODO*///				lo = (uint)MASK_OUT_ABOVE_32(-(sint)lo);
/*TODO*///			}
/*TODO*///	
/*TODO*///			if(BIT_A(word2))
/*TODO*///			{
/*TODO*///				REG_D[word2 & 7] = hi;
/*TODO*///				REG_D[(word2 >> 12) & 7] = lo;
/*TODO*///				FLAG_N = NFLAG_32(hi);
/*TODO*///				FLAG_Z = hi | lo;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			REG_D[(word2 >> 12) & 7] = lo;
/*TODO*///			FLAG_N = NFLAG_32(lo);
/*TODO*///			FLAG_Z = lo;
/*TODO*///			if(BIT_B(word2))
/*TODO*///				FLAG_V = (!((GET_MSB_32(lo) && hi == 0xffffffff) || (!GET_MSB_32(lo) && !hi)))<<7;
/*TODO*///			else
/*TODO*///				FLAG_V = (hi != 0) << 7;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	
/*TODO*///	#endif
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(nbcd, 8, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint dst = *r_dst;
/*TODO*///		uint res = MASK_OUT_ABOVE_8(0x9a - dst - XFLAG_AS_1());
/*TODO*///	
/*TODO*///		if(res != 0x9a)
/*TODO*///		{
/*TODO*///			if((res & 0x0f) == 0xa)
/*TODO*///				res = (res & 0xf0) + 0x10;
/*TODO*///	
/*TODO*///			res = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///			*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	
/*TODO*///			FLAG_Z |= res;
/*TODO*///			FLAG_C = CFLAG_SET;
/*TODO*///			FLAG_X = XFLAG_SET;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			FLAG_X = XFLAG_CLEAR;
/*TODO*///		}
/*TODO*///		FLAG_N = NFLAG_8(res);	/* officially undefined */
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(nbcd, 8, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = MASK_OUT_ABOVE_8(0x9a - dst - XFLAG_AS_1());
/*TODO*///	
/*TODO*///		if(res != 0x9a)
/*TODO*///		{
/*TODO*///			if((res & 0x0f) == 0xa)
/*TODO*///				res = (res & 0xf0) + 0x10;
/*TODO*///	
/*TODO*///			res = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///			m68ki_write_8(ea, MASK_OUT_ABOVE_8(res));
/*TODO*///	
/*TODO*///			FLAG_Z |= res;
/*TODO*///			FLAG_C = CFLAG_SET;
/*TODO*///			FLAG_X = XFLAG_SET;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			FLAG_X = XFLAG_CLEAR;
/*TODO*///		}
/*TODO*///		FLAG_N = NFLAG_8(res);	/* officially undefined */
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(neg, 8, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint res = 0 - MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_C = FLAG_X = CFLAG_8(res);
/*TODO*///		FLAG_V = *r_dst & res;
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(neg, 8, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint src = m68ki_read_8(ea);
/*TODO*///		uint res = 0 - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_C = FLAG_X = CFLAG_8(res);
/*TODO*///		FLAG_V = src & res;
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(neg, 16, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint res = 0 - MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_C = FLAG_X = CFLAG_16(res);
/*TODO*///		FLAG_V = (*r_dst & res)>>8;
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(neg, 16, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint src = m68ki_read_16(ea);
/*TODO*///		uint res = 0 - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_C = FLAG_X = CFLAG_16(res);
/*TODO*///		FLAG_V = (src & res)>>8;
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(neg, 32, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint res = 0 - *r_dst;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_C = FLAG_X = CFLAG_SUB_32(*r_dst, 0, res);
/*TODO*///		FLAG_V = (*r_dst & res)>>24;
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///	
/*TODO*///		*r_dst = FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(neg, 32, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///		uint src = m68ki_read_32(ea);
/*TODO*///		uint res = 0 - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_C = FLAG_X = CFLAG_SUB_32(src, 0, res);
/*TODO*///		FLAG_V = (src & res)>>24;
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(negx, 8, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint res = 0 - MASK_OUT_ABOVE_8(*r_dst) - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_V = *r_dst & res;
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(negx, 8, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint src = m68ki_read_8(ea);
/*TODO*///		uint res = 0 - src - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_V = src & res;
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(negx, 16, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint res = 0 - MASK_OUT_ABOVE_16(*r_dst) - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_V = (*r_dst & res)>>8;
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_16(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(negx, 16, ., .)
/*TODO*///	{
/*TODO*///		uint ea  = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint src = m68ki_read_16(ea);
/*TODO*///		uint res = 0 - MASK_OUT_ABOVE_16(src) - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_V = (src & res)>>8;
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_16(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(negx, 32, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint res = 0 - MASK_OUT_ABOVE_32(*r_dst) - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_SUB_32(*r_dst, 0, res);
/*TODO*///		FLAG_V = (*r_dst & res)>>24;
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(negx, 32, ., .)
/*TODO*///	{
/*TODO*///		uint ea  = M68KMAKE_GET_EA_AY_32;
/*TODO*///		uint src = m68ki_read_32(ea);
/*TODO*///		uint res = 0 - MASK_OUT_ABOVE_32(src) - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_SUB_32(src, 0, res);
/*TODO*///		FLAG_V = (src & res)>>24;
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(nop, 0, ., .)
/*TODO*///	{
/*TODO*///		m68ki_trace_t0();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(not, 8, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint res = MASK_OUT_ABOVE_8(~*r_dst);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(not, 8, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint res = MASK_OUT_ABOVE_8(~m68ki_read_8(ea));
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(not, 16, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(~*r_dst);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(not, 16, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(~m68ki_read_16(ea));
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(not, 32, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint res = *r_dst = MASK_OUT_ABOVE_32(~*r_dst);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(not, 32, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///		uint res = MASK_OUT_ABOVE_32(~m68ki_read_32(ea));
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(or, 8, er, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_8((DX |= MASK_OUT_ABOVE_8(DY)));
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(or, 8, er, .)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_8((DX |= M68KMAKE_GET_OPER_AY_8));
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(or, 16, er, d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16((DX |= MASK_OUT_ABOVE_16(DY)));
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(or, 16, er, .)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16((DX |= M68KMAKE_GET_OPER_AY_16));
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(or, 32, er, d)
/*TODO*///	{
/*TODO*///		uint res = DX |= DY;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(or, 32, er, .)
/*TODO*///	{
/*TODO*///		uint res = DX |= M68KMAKE_GET_OPER_AY_32;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(or, 8, re, .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint res = MASK_OUT_ABOVE_8(DX | m68ki_read_8(ea));
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(or, 16, re, .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(DX | m68ki_read_16(ea));
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(or, 32, re, .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///		uint res = DX | m68ki_read_32(ea);
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(ori, 8, ., d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_8((DY |= OPER_I_8()));
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(ori, 8, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_8();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint res = MASK_OUT_ABOVE_8(src | m68ki_read_8(ea));
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(ori, 16, ., d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(DY |= OPER_I_16());
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(ori, 16, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_16();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(src | m68ki_read_16(ea));
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(ori, 32, ., d)
/*TODO*///	{
/*TODO*///		uint res = DY |= OPER_I_32();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(ori, 32, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_32();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///		uint res = src | m68ki_read_32(ea);
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(ori, 16, toc, .)
/*TODO*///	{
/*TODO*///		m68ki_set_ccr(m68ki_get_ccr() | OPER_I_16());
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(ori, 16, tos, .)
/*TODO*///	{
/*TODO*///		if (FLAG_S != 0)
/*TODO*///		{
/*TODO*///			uint src = OPER_I_16();
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_set_sr(m68ki_get_sr() | src);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_privilege_violation();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(pack, 16, rr, .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			/* Note: DX and DY are reversed in Motorola's docs */
/*TODO*///			uint src = DY + OPER_I_16();
/*TODO*///			uint* r_dst = &DX;
/*TODO*///	
/*TODO*///			*r_dst = MASK_OUT_BELOW_8(*r_dst) | ((src >> 4) & 0x00f0) | (src & 0x000f);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(pack, 16, mm, ax7)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			/* Note: AX and AY are reversed in Motorola's docs */
/*TODO*///			uint ea_src = EA_AY_PD_8();
/*TODO*///			uint src = m68ki_read_8(ea_src);
/*TODO*///			ea_src = EA_AY_PD_8();
/*TODO*///			src = ((src << 8) | m68ki_read_8(ea_src)) + OPER_I_16();
/*TODO*///	
/*TODO*///			m68ki_write_8(EA_A7_PD_8(), ((src >> 4) & 0x00f0) | (src & 0x000f));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(pack, 16, mm, ay7)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			/* Note: AX and AY are reversed in Motorola's docs */
/*TODO*///			uint ea_src = EA_A7_PD_8();
/*TODO*///			uint src = m68ki_read_8(ea_src);
/*TODO*///			ea_src = EA_A7_PD_8();
/*TODO*///			src = ((src << 8) | m68ki_read_8(ea_src)) + OPER_I_16();
/*TODO*///	
/*TODO*///			m68ki_write_8(EA_AX_PD_8(), ((src >> 4) & 0x00f0) | (src & 0x000f));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(pack, 16, mm, axy7)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint ea_src = EA_A7_PD_8();
/*TODO*///			uint src = m68ki_read_8(ea_src);
/*TODO*///			ea_src = EA_A7_PD_8();
/*TODO*///			src = ((src << 8) | m68ki_read_8(ea_src)) + OPER_I_16();
/*TODO*///	
/*TODO*///			m68ki_write_8(EA_A7_PD_8(), ((src >> 4) & 0x00f0) | (src & 0x000f));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(pack, 16, mm, .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			/* Note: AX and AY are reversed in Motorola's docs */
/*TODO*///			uint ea_src = EA_AY_PD_8();
/*TODO*///			uint src = m68ki_read_8(ea_src);
/*TODO*///			ea_src = EA_AY_PD_8();
/*TODO*///			src = ((src << 8) | m68ki_read_8(ea_src)) + OPER_I_16();
/*TODO*///	
/*TODO*///			m68ki_write_8(EA_AX_PD_8(), ((src >> 4) & 0x00f0) | (src & 0x000f));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(pea, 32, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///	
/*TODO*///		m68ki_push_32(ea);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(reset, 0, ., .)
/*TODO*///	{
/*TODO*///		if (FLAG_S != 0)
/*TODO*///		{
/*TODO*///			m68ki_output_reset();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			USE_CYCLES(CYC_RESET);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_privilege_violation();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(ror, 8, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint orig_shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint shift = orig_shift & 7;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = ROR_8(src, shift);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = src << (9-orig_shift);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(ror, 16, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = ROR_16(src, shift);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = src << (9-shift);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(ror, 32, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint64 src = *r_dst;
/*TODO*///		uint res = ROR_32(src, shift);
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = src << (9-shift);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(ror, 8, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint orig_shift = DX & 0x3f;
/*TODO*///		uint shift = orig_shift & 7;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = ROR_8(src, shift);
/*TODO*///	
/*TODO*///		if(orig_shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(orig_shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///			FLAG_C = src << (8-((shift-1)&7));
/*TODO*///			FLAG_N = NFLAG_8(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_8(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(ror, 16, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint orig_shift = DX & 0x3f;
/*TODO*///		uint shift = orig_shift & 15;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = ROR_16(src, shift);
/*TODO*///	
/*TODO*///		if(orig_shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(orig_shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///			FLAG_C = (src >> ((shift - 1) & 15)) << 8;
/*TODO*///			FLAG_N = NFLAG_16(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_16(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(ror, 32, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint orig_shift = DX & 0x3f;
/*TODO*///		uint shift = orig_shift & 31;
/*TODO*///		uint64 src = *r_dst;
/*TODO*///		uint res = ROR_32(src, shift);
/*TODO*///	
/*TODO*///		if(orig_shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(orig_shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			*r_dst = res;
/*TODO*///			FLAG_C = (src >> ((shift - 1) & 31)) << 8;
/*TODO*///			FLAG_N = NFLAG_32(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_32(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(ror, 16, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint src = m68ki_read_16(ea);
/*TODO*///		uint res = ROR_16(src, 1);
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = src << 8;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(rol, 8, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint orig_shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint shift = orig_shift & 7;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = ROL_8(src, shift);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = src << orig_shift;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(rol, 16, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = ROL_16(src, shift);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = src >> (8-shift);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(rol, 32, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint64 src = *r_dst;
/*TODO*///		uint res = ROL_32(src, shift);
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = src >> (24-shift);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(rol, 8, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint orig_shift = DX & 0x3f;
/*TODO*///		uint shift = orig_shift & 7;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = ROL_8(src, shift);
/*TODO*///	
/*TODO*///		if(orig_shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(orig_shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			if(shift != 0)
/*TODO*///			{
/*TODO*///				*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///				FLAG_C = src << shift;
/*TODO*///				FLAG_N = NFLAG_8(res);
/*TODO*///				FLAG_Z = res;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			FLAG_C = (src & 1)<<8;
/*TODO*///			FLAG_N = NFLAG_8(src);
/*TODO*///			FLAG_Z = src;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_8(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(rol, 16, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint orig_shift = DX & 0x3f;
/*TODO*///		uint shift = orig_shift & 15;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = MASK_OUT_ABOVE_16(ROL_16(src, shift));
/*TODO*///	
/*TODO*///		if(orig_shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(orig_shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			if(shift != 0)
/*TODO*///			{
/*TODO*///				*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///				FLAG_C = (src << shift) >> 8;
/*TODO*///				FLAG_N = NFLAG_16(res);
/*TODO*///				FLAG_Z = res;
/*TODO*///				FLAG_V = VFLAG_CLEAR;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			FLAG_C = (src & 1)<<8;
/*TODO*///			FLAG_N = NFLAG_16(src);
/*TODO*///			FLAG_Z = src;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_16(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(rol, 32, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint orig_shift = DX & 0x3f;
/*TODO*///		uint shift = orig_shift & 31;
/*TODO*///		uint64 src = *r_dst;
/*TODO*///		uint res = ROL_32(src, shift);
/*TODO*///	
/*TODO*///		if(orig_shift != 0)
/*TODO*///		{
/*TODO*///			USE_CYCLES(orig_shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			*r_dst = res;
/*TODO*///	
/*TODO*///			FLAG_C = (src >> (32 - shift)) << 8;
/*TODO*///			FLAG_N = NFLAG_32(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_N = NFLAG_32(src);
/*TODO*///		FLAG_Z = src;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(rol, 16, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint src = m68ki_read_16(ea);
/*TODO*///		uint res = MASK_OUT_ABOVE_16(ROL_16(src, 1));
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_C = src >> 7;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(roxr, 8, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = ROR_9(src | (XFLAG_AS_1() << 8), shift);
/*TODO*///	
/*TODO*///		FLAG_C = FLAG_X = res;
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(roxr, 16, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = ROR_17(src | (XFLAG_AS_1() << 16), shift);
/*TODO*///	
/*TODO*///		FLAG_C = FLAG_X = res >> 8;
/*TODO*///		res = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(roxr, 32, s, .)
/*TODO*///	{
/*TODO*///	#if M68K_USE_64_BIT
/*TODO*///	
/*TODO*///		uint*  r_dst = &DY;
/*TODO*///		uint   shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint64 src   = *r_dst;
/*TODO*///		uint64 res   = src | (((uint64)XFLAG_AS_1()) << 32);
/*TODO*///	
/*TODO*///		res = ROR_33_64(res, shift);
/*TODO*///	
/*TODO*///		FLAG_C = FLAG_X = res >> 24;
/*TODO*///		res = MASK_OUT_ABOVE_32(res);
/*TODO*///	
/*TODO*///		*r_dst =  res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	
/*TODO*///	#else
/*TODO*///	
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = *r_dst;
/*TODO*///		uint res = MASK_OUT_ABOVE_32((ROR_33(src, shift) & ~(1 << (32 - shift))) | (XFLAG_AS_1() << (32 - shift)));
/*TODO*///		uint new_x_flag = src & (1 << (shift - 1));
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	
/*TODO*///		FLAG_C = FLAG_X = (new_x_flag != 0)<<8;
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	
/*TODO*///	#endif
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(roxr, 8, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint orig_shift = DX & 0x3f;
/*TODO*///	
/*TODO*///		if(orig_shift != 0)
/*TODO*///		{
/*TODO*///			uint shift = orig_shift % 9;
/*TODO*///			uint src   = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///			uint res   = ROR_9(src | (XFLAG_AS_1() << 8), shift);
/*TODO*///	
/*TODO*///			USE_CYCLES(orig_shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			FLAG_C = FLAG_X = res;
/*TODO*///			res = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///			*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///			FLAG_N = NFLAG_8(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = FLAG_X;
/*TODO*///		FLAG_N = NFLAG_8(*r_dst);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(roxr, 16, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint orig_shift = DX & 0x3f;
/*TODO*///	
/*TODO*///		if(orig_shift != 0)
/*TODO*///		{
/*TODO*///			uint shift = orig_shift % 17;
/*TODO*///			uint src   = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///			uint res   = ROR_17(src | (XFLAG_AS_1() << 16), shift);
/*TODO*///	
/*TODO*///			USE_CYCLES(orig_shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			FLAG_C = FLAG_X = res >> 8;
/*TODO*///			res = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///			*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///			FLAG_N = NFLAG_16(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = FLAG_X;
/*TODO*///		FLAG_N = NFLAG_16(*r_dst);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(roxr, 32, r, .)
/*TODO*///	{
/*TODO*///	#if M68K_USE_64_BIT
/*TODO*///	
/*TODO*///		uint*  r_dst = &DY;
/*TODO*///		uint   orig_shift = DX & 0x3f;
/*TODO*///	
/*TODO*///		if(orig_shift != 0)
/*TODO*///		{
/*TODO*///			uint   shift = orig_shift % 33;
/*TODO*///			uint64 src   = *r_dst;
/*TODO*///			uint64 res   = src | (((uint64)XFLAG_AS_1()) << 32);
/*TODO*///	
/*TODO*///			res = ROR_33_64(res, shift);
/*TODO*///	
/*TODO*///			USE_CYCLES(orig_shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			FLAG_C = FLAG_X = res >> 24;
/*TODO*///			res = MASK_OUT_ABOVE_32(res);
/*TODO*///	
/*TODO*///			*r_dst = res;
/*TODO*///			FLAG_N = NFLAG_32(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = FLAG_X;
/*TODO*///		FLAG_N = NFLAG_32(*r_dst);
/*TODO*///		FLAG_Z = *r_dst;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	
/*TODO*///	#else
/*TODO*///	
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint orig_shift = DX & 0x3f;
/*TODO*///		uint shift = orig_shift % 33;
/*TODO*///		uint src = *r_dst;
/*TODO*///		uint res = MASK_OUT_ABOVE_32((ROR_33(src, shift) & ~(1 << (32 - shift))) | (XFLAG_AS_1() << (32 - shift)));
/*TODO*///		uint new_x_flag = src & (1 << (shift - 1));
/*TODO*///	
/*TODO*///		if(orig_shift != 0)
/*TODO*///			USE_CYCLES(orig_shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///		if(shift != 0)
/*TODO*///		{
/*TODO*///			*r_dst = res;
/*TODO*///			FLAG_X = (new_x_flag != 0)<<8;
/*TODO*///		}
/*TODO*///		else
/*TODO*///			res = src;
/*TODO*///		FLAG_C = FLAG_X;
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	
/*TODO*///	#endif
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(roxr, 16, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint src = m68ki_read_16(ea);
/*TODO*///		uint res = ROR_17(src | (XFLAG_AS_1() << 16), 1);
/*TODO*///	
/*TODO*///		FLAG_C = FLAG_X = res >> 8;
/*TODO*///		res = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(roxl, 8, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = ROL_9(src | (XFLAG_AS_1() << 8), shift);
/*TODO*///	
/*TODO*///		FLAG_C = FLAG_X = res;
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(roxl, 16, s, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = ROL_17(src | (XFLAG_AS_1() << 16), shift);
/*TODO*///	
/*TODO*///		FLAG_C = FLAG_X = res >> 8;
/*TODO*///		res = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(roxl, 32, s, .)
/*TODO*///	{
/*TODO*///	#if M68K_USE_64_BIT
/*TODO*///	
/*TODO*///		uint*  r_dst = &DY;
/*TODO*///		uint   shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint64 src   = *r_dst;
/*TODO*///		uint64 res   = src | (((uint64)XFLAG_AS_1()) << 32);
/*TODO*///	
/*TODO*///		res = ROL_33_64(res, shift);
/*TODO*///	
/*TODO*///		FLAG_C = FLAG_X = res >> 24;
/*TODO*///		res = MASK_OUT_ABOVE_32(res);
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	
/*TODO*///	#else
/*TODO*///	
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint shift = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint src = *r_dst;
/*TODO*///		uint res = MASK_OUT_ABOVE_32((ROL_33(src, shift) & ~(1 << (shift - 1))) | (XFLAG_AS_1() << (shift - 1)));
/*TODO*///		uint new_x_flag = src & (1 << (32 - shift));
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	
/*TODO*///		FLAG_C = FLAG_X = (new_x_flag != 0)<<8;
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	
/*TODO*///	#endif
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(roxl, 8, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint orig_shift = DX & 0x3f;
/*TODO*///	
/*TODO*///	
/*TODO*///		if(orig_shift != 0)
/*TODO*///		{
/*TODO*///			uint shift = orig_shift % 9;
/*TODO*///			uint src   = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///			uint res   = ROL_9(src | (XFLAG_AS_1() << 8), shift);
/*TODO*///	
/*TODO*///			USE_CYCLES(orig_shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			FLAG_C = FLAG_X = res;
/*TODO*///			res = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///			*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///			FLAG_N = NFLAG_8(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = FLAG_X;
/*TODO*///		FLAG_N = NFLAG_8(*r_dst);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(roxl, 16, r, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint orig_shift = DX & 0x3f;
/*TODO*///	
/*TODO*///		if(orig_shift != 0)
/*TODO*///		{
/*TODO*///			uint shift = orig_shift % 17;
/*TODO*///			uint src   = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///			uint res   = ROL_17(src | (XFLAG_AS_1() << 16), shift);
/*TODO*///	
/*TODO*///			USE_CYCLES(orig_shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			FLAG_C = FLAG_X = res >> 8;
/*TODO*///			res = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///			*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///			FLAG_N = NFLAG_16(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = FLAG_X;
/*TODO*///		FLAG_N = NFLAG_16(*r_dst);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(roxl, 32, r, .)
/*TODO*///	{
/*TODO*///	#if M68K_USE_64_BIT
/*TODO*///	
/*TODO*///		uint*  r_dst = &DY;
/*TODO*///		uint   orig_shift = DX & 0x3f;
/*TODO*///	
/*TODO*///		if(orig_shift != 0)
/*TODO*///		{
/*TODO*///			uint   shift = orig_shift % 33;
/*TODO*///			uint64 src   = *r_dst;
/*TODO*///			uint64 res   = src | (((uint64)XFLAG_AS_1()) << 32);
/*TODO*///	
/*TODO*///			res = ROL_33_64(res, shift);
/*TODO*///	
/*TODO*///			USE_CYCLES(orig_shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///			FLAG_C = FLAG_X = res >> 24;
/*TODO*///			res = MASK_OUT_ABOVE_32(res);
/*TODO*///	
/*TODO*///			*r_dst = res;
/*TODO*///			FLAG_N = NFLAG_32(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FLAG_C = FLAG_X;
/*TODO*///		FLAG_N = NFLAG_32(*r_dst);
/*TODO*///		FLAG_Z = *r_dst;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	
/*TODO*///	#else
/*TODO*///	
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint orig_shift = DX & 0x3f;
/*TODO*///		uint shift = orig_shift % 33;
/*TODO*///		uint src = *r_dst;
/*TODO*///		uint res = MASK_OUT_ABOVE_32((ROL_33(src, shift) & ~(1 << (shift - 1))) | (XFLAG_AS_1() << (shift - 1)));
/*TODO*///		uint new_x_flag = src & (1 << (32 - shift));
/*TODO*///	
/*TODO*///		if(orig_shift != 0)
/*TODO*///			USE_CYCLES(orig_shift<<CYC_SHIFT);
/*TODO*///	
/*TODO*///		if(shift != 0)
/*TODO*///		{
/*TODO*///			*r_dst = res;
/*TODO*///			FLAG_X = (new_x_flag != 0)<<8;
/*TODO*///		}
/*TODO*///		else
/*TODO*///			res = src;
/*TODO*///		FLAG_C = FLAG_X;
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	
/*TODO*///	#endif
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(roxl, 16, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint src = m68ki_read_16(ea);
/*TODO*///		uint res = ROL_17(src | (XFLAG_AS_1() << 16), 1);
/*TODO*///	
/*TODO*///		FLAG_C = FLAG_X = res >> 8;
/*TODO*///		res = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(rtd, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_010_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint new_pc = m68ki_pull_32();
/*TODO*///	
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			REG_A[7] = MASK_OUT_ABOVE_32(REG_A[7] + MAKE_INT_16(OPER_I_16()));
/*TODO*///			m68ki_jump(new_pc);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(rte, 32, ., .)
/*TODO*///	{
/*TODO*///		if (FLAG_S != 0)
/*TODO*///		{
/*TODO*///			uint new_sr;
/*TODO*///			uint new_pc;
/*TODO*///			uint format_word;
/*TODO*///	
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///	
/*TODO*///			if(CPU_TYPE_IS_000(CPU_TYPE))
/*TODO*///			{
/*TODO*///				new_sr = m68ki_pull_16();
/*TODO*///				new_pc = m68ki_pull_32();
/*TODO*///				m68ki_jump(new_pc);
/*TODO*///				m68ki_set_sr(new_sr);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			if(CPU_TYPE_IS_010(CPU_TYPE))
/*TODO*///			{
/*TODO*///				format_word = m68ki_read_16(REG_A[7]+6) >> 12;
/*TODO*///				if(format_word == 0)
/*TODO*///				{
/*TODO*///					new_sr = m68ki_pull_16();
/*TODO*///					new_pc = m68ki_pull_32();
/*TODO*///					m68ki_fake_pull_16();	/* format word */
/*TODO*///					m68ki_jump(new_pc);
/*TODO*///					m68ki_set_sr(new_sr);
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				/* Not handling bus fault (9) */
/*TODO*///				m68ki_exception_format_error();
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* Otherwise it's 020 */
/*TODO*///	rte_loop:
/*TODO*///			format_word = m68ki_read_16(REG_A[7]+6) >> 12;
/*TODO*///			switch(format_word)
/*TODO*///			{
/*TODO*///				case 0: /* Normal */
/*TODO*///					new_sr = m68ki_pull_16();
/*TODO*///					new_pc = m68ki_pull_32();
/*TODO*///					m68ki_fake_pull_16();	/* format word */
/*TODO*///					m68ki_jump(new_pc);
/*TODO*///					m68ki_set_sr(new_sr);
/*TODO*///					return;
/*TODO*///				case 1: /* Throwaway */
/*TODO*///					new_sr = m68ki_pull_16();
/*TODO*///					m68ki_fake_pull_32();	/* program counter */
/*TODO*///					m68ki_fake_pull_16();	/* format word */
/*TODO*///					m68ki_set_sr_noint(new_sr);
/*TODO*///					goto rte_loop;
/*TODO*///				case 2: /* Trap */
/*TODO*///					new_sr = m68ki_pull_16();
/*TODO*///					new_pc = m68ki_pull_32();
/*TODO*///					m68ki_fake_pull_16();	/* format word */
/*TODO*///					m68ki_fake_pull_32();	/* address */
/*TODO*///					m68ki_jump(new_pc);
/*TODO*///					m68ki_set_sr(new_sr);
/*TODO*///					return;
/*TODO*///			}
/*TODO*///			/* Not handling long or short bus fault */
/*TODO*///			m68ki_exception_format_error();
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_privilege_violation();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(rtm, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_020_VARIANT(CPU_TYPE))
/*TODO*///		{
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			M68K_DO_LOG((M68K_LOG_FILEHANDLE "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///						 m68ki_cpu_names[CPU_TYPE], ADDRESS_68K(REG_PC - 2), REG_IR,
/*TODO*///						 m68k_disassemble_quick(ADDRESS_68K(REG_PC - 2))));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(rtr, 32, ., .)
/*TODO*///	{
/*TODO*///		m68ki_trace_t0();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_set_ccr(m68ki_pull_16());
/*TODO*///		m68ki_jump(m68ki_pull_32());
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(rts, 32, ., .)
/*TODO*///	{
/*TODO*///		m68ki_trace_t0();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_jump(m68ki_pull_32());
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sbcd, 8, rr, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = DY;
/*TODO*///		uint dst = *r_dst;
/*TODO*///		uint res = LOW_NIBBLE(dst) - LOW_NIBBLE(src) - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		if(res > 9)
/*TODO*///			res -= 6;
/*TODO*///		res += HIGH_NIBBLE(dst) - HIGH_NIBBLE(src);
/*TODO*///		FLAG_X = FLAG_C = (res > 0x99) << 8;
/*TODO*///		if (FLAG_C != 0)
/*TODO*///			res += 0xa0;
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res); /* officially undefined */
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sbcd, 8, mm, ax7)
/*TODO*///	{
/*TODO*///		uint src = OPER_AY_PD_8();
/*TODO*///		uint ea  = EA_A7_PD_8();
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = LOW_NIBBLE(dst) - LOW_NIBBLE(src) - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		if(res > 9)
/*TODO*///			res -= 6;
/*TODO*///		res += HIGH_NIBBLE(dst) - HIGH_NIBBLE(src);
/*TODO*///		FLAG_X = FLAG_C = (res > 0x99) << 8;
/*TODO*///		if (FLAG_C != 0)
/*TODO*///			res += 0xa0;
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res); /* officially undefined */
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sbcd, 8, mm, ay7)
/*TODO*///	{
/*TODO*///		uint src = OPER_A7_PD_8();
/*TODO*///		uint ea  = EA_AX_PD_8();
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = LOW_NIBBLE(dst) - LOW_NIBBLE(src) - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		if(res > 9)
/*TODO*///			res -= 6;
/*TODO*///		res += HIGH_NIBBLE(dst) - HIGH_NIBBLE(src);
/*TODO*///		FLAG_X = FLAG_C = (res > 0x99) << 8;
/*TODO*///		if (FLAG_C != 0)
/*TODO*///			res += 0xa0;
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res); /* officially undefined */
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sbcd, 8, mm, axy7)
/*TODO*///	{
/*TODO*///		uint src = OPER_A7_PD_8();
/*TODO*///		uint ea  = EA_A7_PD_8();
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = LOW_NIBBLE(dst) - LOW_NIBBLE(src) - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		if(res > 9)
/*TODO*///			res -= 6;
/*TODO*///		res += HIGH_NIBBLE(dst) - HIGH_NIBBLE(src);
/*TODO*///		FLAG_X = FLAG_C = (res > 0x99) << 8;
/*TODO*///		if (FLAG_C != 0)
/*TODO*///			res += 0xa0;
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res); /* officially undefined */
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sbcd, 8, mm, .)
/*TODO*///	{
/*TODO*///		uint src = OPER_AY_PD_8();
/*TODO*///		uint ea  = EA_AX_PD_8();
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = LOW_NIBBLE(dst) - LOW_NIBBLE(src) - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		if(res > 9)
/*TODO*///			res -= 6;
/*TODO*///		res += HIGH_NIBBLE(dst) - HIGH_NIBBLE(src);
/*TODO*///		FLAG_X = FLAG_C = (res > 0x99) << 8;
/*TODO*///		if (FLAG_C != 0)
/*TODO*///			res += 0xa0;
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res); /* officially undefined */
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(st, 8, ., d)
/*TODO*///	{
/*TODO*///		DY |= 0xff;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(st, 8, ., .)
/*TODO*///	{
/*TODO*///		m68ki_write_8(M68KMAKE_GET_EA_AY_8, 0xff);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sf, 8, ., d)
/*TODO*///	{
/*TODO*///		DY &= 0xffffff00;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sf, 8, ., .)
/*TODO*///	{
/*TODO*///		m68ki_write_8(M68KMAKE_GET_EA_AY_8, 0);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(scc, 8, ., d)
/*TODO*///	{
/*TODO*///		if (M68KMAKE_CC != 0)
/*TODO*///		{
/*TODO*///			DY |= 0xff;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		DY &= 0xffffff00;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(scc, 8, ., .)
/*TODO*///	{
/*TODO*///		m68ki_write_8(M68KMAKE_GET_EA_AY_8, M68KMAKE_CC ? 0xff : 0);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(stop, 0, ., .)
/*TODO*///	{
/*TODO*///		if (FLAG_S != 0)
/*TODO*///		{
/*TODO*///			uint new_sr = OPER_I_16();
/*TODO*///			m68ki_trace_t0();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///			CPU_STOPPED |= STOP_LEVEL_STOP;
/*TODO*///			m68ki_set_sr(new_sr);
/*TODO*///			m68ki_remaining_cycles = 0;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_privilege_violation();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sub, 8, er, d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(DY);
/*TODO*///		uint dst = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sub, 8, er, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = M68KMAKE_GET_OPER_AY_8;
/*TODO*///		uint dst = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sub, 16, er, d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(DY);
/*TODO*///		uint dst = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sub, 16, er, a)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(AY);
/*TODO*///		uint dst = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sub, 16, er, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = M68KMAKE_GET_OPER_AY_16;
/*TODO*///		uint dst = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sub, 32, er, d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = DY;
/*TODO*///		uint dst = *r_dst;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///	
/*TODO*///		*r_dst = FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sub, 32, er, a)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = AY;
/*TODO*///		uint dst = *r_dst;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///	
/*TODO*///		*r_dst = FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sub, 32, er, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = M68KMAKE_GET_OPER_AY_32;
/*TODO*///		uint dst = *r_dst;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///	
/*TODO*///		*r_dst = FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sub, 8, re, .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(DX);
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sub, 16, re, .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(DX);
/*TODO*///		uint dst = m68ki_read_16(ea);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(sub, 32, re, .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///		uint src = DX;
/*TODO*///		uint dst = m68ki_read_32(ea);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(suba, 16, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_ABOVE_32(*r_dst - MAKE_INT_16(DY));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(suba, 16, ., a)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_ABOVE_32(*r_dst - MAKE_INT_16(AY));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(suba, 16, ., .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_ABOVE_32(*r_dst - MAKE_INT_16(M68KMAKE_GET_OPER_AY_16));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(suba, 32, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_ABOVE_32(*r_dst - DY);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(suba, 32, ., a)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_ABOVE_32(*r_dst - AY);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(suba, 32, ., .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AX;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_ABOVE_32(*r_dst - M68KMAKE_GET_OPER_AY_32);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subi, 8, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint src = OPER_I_8();
/*TODO*///		uint dst = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subi, 8, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_8();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subi, 16, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint src = OPER_I_16();
/*TODO*///		uint dst = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subi, 16, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_16();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint dst = m68ki_read_16(ea);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subi, 32, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint src = OPER_I_32();
/*TODO*///		uint dst = *r_dst;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	
/*TODO*///		*r_dst = FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subi, 32, ., .)
/*TODO*///	{
/*TODO*///		uint src = OPER_I_32();
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///		uint dst = m68ki_read_32(ea);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subq, 8, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint src = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint dst = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subq, 8, ., .)
/*TODO*///	{
/*TODO*///		uint src = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subq, 16, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint src = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint dst = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subq, 16, ., a)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AY;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_ABOVE_32(*r_dst - ((((REG_IR >> 9) - 1) & 7) + 1));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subq, 16, ., .)
/*TODO*///	{
/*TODO*///		uint src = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_16;
/*TODO*///		uint dst = m68ki_read_16(ea);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_16(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subq, 32, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///		uint src = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint dst = *r_dst;
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	
/*TODO*///		*r_dst = FLAG_Z;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subq, 32, ., a)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AY;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_ABOVE_32(*r_dst - ((((REG_IR >> 9) - 1) & 7) + 1));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subq, 32, ., .)
/*TODO*///	{
/*TODO*///		uint src = (((REG_IR >> 9) - 1) & 7) + 1;
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_32;
/*TODO*///		uint dst = m68ki_read_32(ea);
/*TODO*///		uint res = dst - src;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, FLAG_Z);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subx, 8, rr, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = MASK_OUT_ABOVE_8(DY);
/*TODO*///		uint dst = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		uint res = dst - src - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_8(*r_dst) | res;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subx, 16, rr, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = MASK_OUT_ABOVE_16(DY);
/*TODO*///		uint dst = MASK_OUT_ABOVE_16(*r_dst);
/*TODO*///		uint res = dst - src - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_16(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		*r_dst = MASK_OUT_BELOW_16(*r_dst) | res;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subx, 32, rr, .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DX;
/*TODO*///		uint src = DY;
/*TODO*///		uint dst = *r_dst;
/*TODO*///		uint res = dst - src - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		*r_dst = res;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subx, 8, mm, ax7)
/*TODO*///	{
/*TODO*///		uint src = OPER_AY_PD_8();
/*TODO*///		uint ea  = EA_A7_PD_8();
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = dst - src - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subx, 8, mm, ay7)
/*TODO*///	{
/*TODO*///		uint src = OPER_A7_PD_8();
/*TODO*///		uint ea  = EA_AX_PD_8();
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = dst - src - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subx, 8, mm, axy7)
/*TODO*///	{
/*TODO*///		uint src = OPER_A7_PD_8();
/*TODO*///		uint ea  = EA_A7_PD_8();
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = dst - src - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subx, 8, mm, .)
/*TODO*///	{
/*TODO*///		uint src = OPER_AY_PD_8();
/*TODO*///		uint ea  = EA_AX_PD_8();
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = dst - src - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_8(res);
/*TODO*///		FLAG_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_8(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subx, 16, mm, .)
/*TODO*///	{
/*TODO*///		uint src = OPER_AY_PD_16();
/*TODO*///		uint ea  = EA_AX_PD_16();
/*TODO*///		uint dst = m68ki_read_16(ea);
/*TODO*///		uint res = dst - src - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_16(res);
/*TODO*///		FLAG_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_16(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_16(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(subx, 32, mm, .)
/*TODO*///	{
/*TODO*///		uint src = OPER_AY_PD_32();
/*TODO*///		uint ea  = EA_AX_PD_32();
/*TODO*///		uint dst = m68ki_read_32(ea);
/*TODO*///		uint res = dst - src - XFLAG_AS_1();
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_X = FLAG_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///		FLAG_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	
/*TODO*///		res = MASK_OUT_ABOVE_32(res);
/*TODO*///		FLAG_Z |= res;
/*TODO*///	
/*TODO*///		m68ki_write_32(ea, res);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(swap, 32, ., .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///	
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_32(*r_dst<<16);
/*TODO*///		*r_dst = (*r_dst>>16) | FLAG_Z;
/*TODO*///	
/*TODO*///		FLAG_Z = *r_dst;
/*TODO*///		FLAG_N = NFLAG_32(*r_dst);
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tas, 8, ., d)
/*TODO*///	{
/*TODO*///		uint* r_dst = &DY;
/*TODO*///	
/*TODO*///		FLAG_Z = MASK_OUT_ABOVE_8(*r_dst);
/*TODO*///		FLAG_N = NFLAG_8(*r_dst);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		*r_dst |= 0x80;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tas, 8, ., .)
/*TODO*///	{
/*TODO*///		uint ea = M68KMAKE_GET_EA_AY_8;
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///	
/*TODO*///		FLAG_Z = dst;
/*TODO*///		FLAG_N = NFLAG_8(dst);
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///		m68ki_write_8(ea, dst | 0x80);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(trap, 0, ., .)
/*TODO*///	{
/*TODO*///		/* Trap#n stacks exception frame type 0 */
/*TODO*///		m68ki_exception_trapN(EXCEPTION_TRAP_BASE + (REG_IR & 0xf));	/* HJB 990403 */
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(trapt, 0, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			m68ki_exception_trap(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(trapt, 16, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			m68ki_exception_trap(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(trapt, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			m68ki_exception_trap(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(trapf, 0, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(trapf, 16, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			REG_PC += 2;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(trapf, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			REG_PC += 4;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(trapcc, 0, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			if (M68KMAKE_CC != 0)
/*TODO*///				m68ki_exception_trap(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(trapcc, 16, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			if (M68KMAKE_CC != 0)
/*TODO*///			{
/*TODO*///				m68ki_exception_trap(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			REG_PC += 2;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(trapcc, 32, ., .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			if (M68KMAKE_CC != 0)
/*TODO*///			{
/*TODO*///				m68ki_exception_trap(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			REG_PC += 4;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(trapv, 0, ., .)
/*TODO*///	{
/*TODO*///		if(COND_VC())
/*TODO*///		{
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_trap(EXCEPTION_TRAPV);  /* HJB 990403 */
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tst, 8, ., d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tst, 8, ., .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_8;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_8(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tst, 8, ., pcdi)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint res = OPER_PCDI_8();
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_8(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tst, 8, ., pcix)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint res = OPER_PCIX_8();
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_8(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tst, 8, ., i)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint res = OPER_I_8();
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_8(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tst, 16, ., d)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tst, 16, ., a)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint res = MAKE_INT_16(AY);
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_16(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tst, 16, ., .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_16;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_16(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tst, 16, ., pcdi)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint res = OPER_PCDI_16();
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_16(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tst, 16, ., pcix)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint res = OPER_PCIX_16();
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_16(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tst, 16, ., i)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint res = OPER_I_16();
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_16(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tst, 32, ., d)
/*TODO*///	{
/*TODO*///		uint res = DY;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tst, 32, ., a)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint res = AY;
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_32(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tst, 32, ., .)
/*TODO*///	{
/*TODO*///		uint res = M68KMAKE_GET_OPER_AY_32;
/*TODO*///	
/*TODO*///		FLAG_N = NFLAG_32(res);
/*TODO*///		FLAG_Z = res;
/*TODO*///		FLAG_V = VFLAG_CLEAR;
/*TODO*///		FLAG_C = CFLAG_CLEAR;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tst, 32, ., pcdi)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint res = OPER_PCDI_32();
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_32(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tst, 32, ., pcix)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint res = OPER_PCIX_32();
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_32(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(tst, 32, ., i)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint res = OPER_I_32();
/*TODO*///	
/*TODO*///			FLAG_N = NFLAG_32(res);
/*TODO*///			FLAG_Z = res;
/*TODO*///			FLAG_V = VFLAG_CLEAR;
/*TODO*///			FLAG_C = CFLAG_CLEAR;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(unlk, 32, ., a7)
/*TODO*///	{
/*TODO*///		REG_A[7] = m68ki_read_32(REG_A[7]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(unlk, 32, ., .)
/*TODO*///	{
/*TODO*///		uint* r_dst = &AY;
/*TODO*///	
/*TODO*///		REG_A[7] = *r_dst;
/*TODO*///		*r_dst = m68ki_pull_32();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(unpk, 16, rr, .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			/* Note: DX and DY are reversed in Motorola's docs */
/*TODO*///			uint src = DY;
/*TODO*///			uint* r_dst = &DX;
/*TODO*///	
/*TODO*///			*r_dst = MASK_OUT_BELOW_16(*r_dst) | (((((src << 4) & 0x0f00) | (src & 0x000f)) + OPER_I_16()) & 0xffff);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(unpk, 16, mm, ax7)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			/* Note: AX and AY are reversed in Motorola's docs */
/*TODO*///			uint src = OPER_AY_PD_8();
/*TODO*///			uint ea_dst;
/*TODO*///	
/*TODO*///			src = (((src << 4) & 0x0f00) | (src & 0x000f)) + OPER_I_16();
/*TODO*///			ea_dst = EA_A7_PD_8();
/*TODO*///			m68ki_write_8(ea_dst, (src >> 8) & 0xff);
/*TODO*///			ea_dst = EA_A7_PD_8();
/*TODO*///			m68ki_write_8(ea_dst, src & 0xff);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(unpk, 16, mm, ay7)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			/* Note: AX and AY are reversed in Motorola's docs */
/*TODO*///			uint src = OPER_A7_PD_8();
/*TODO*///			uint ea_dst;
/*TODO*///	
/*TODO*///			src = (((src << 4) & 0x0f00) | (src & 0x000f)) + OPER_I_16();
/*TODO*///			ea_dst = EA_AX_PD_8();
/*TODO*///			m68ki_write_8(ea_dst, (src >> 8) & 0xff);
/*TODO*///			ea_dst = EA_AX_PD_8();
/*TODO*///			m68ki_write_8(ea_dst, src & 0xff);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(unpk, 16, mm, axy7)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			uint src = OPER_A7_PD_8();
/*TODO*///			uint ea_dst;
/*TODO*///	
/*TODO*///			src = (((src << 4) & 0x0f00) | (src & 0x000f)) + OPER_I_16();
/*TODO*///			ea_dst = EA_A7_PD_8();
/*TODO*///			m68ki_write_8(ea_dst, (src >> 8) & 0xff);
/*TODO*///			ea_dst = EA_A7_PD_8();
/*TODO*///			m68ki_write_8(ea_dst, src & 0xff);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	M68KMAKE_OP(unpk, 16, mm, .)
/*TODO*///	{
/*TODO*///		if(CPU_TYPE_IS_EC020_PLUS(CPU_TYPE))
/*TODO*///		{
/*TODO*///			/* Note: AX and AY are reversed in Motorola's docs */
/*TODO*///			uint src = OPER_AY_PD_8();
/*TODO*///			uint ea_dst;
/*TODO*///	
/*TODO*///			src = (((src << 4) & 0x0f00) | (src & 0x000f)) + OPER_I_16();
/*TODO*///			ea_dst = EA_AX_PD_8();
/*TODO*///			m68ki_write_8(ea_dst, (src >> 8) & 0xff);
/*TODO*///			ea_dst = EA_AX_PD_8();
/*TODO*///			m68ki_write_8(ea_dst, src & 0xff);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_exception_illegal();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
/*TODO*///	M68KMAKE_END
    public static FILE m68klog = null;//fopen("m68k.log", "wa");  //for debug purposes
    
    public static opcode m68k_op_illegal = new opcode() {
        public void handler() {
            if (m68klog != null) {
                fclose(m68klog);
            }
            throw new UnsupportedOperationException("Unimplemented");
        }
    };
}
