/*****************************************************************************

	h6280ops.h - Addressing modes and opcode macros for the Hu6820 cpu

	Copyright (c) 1999 Bryan McPhail, mish@tendril.co.uk

	This source code is based (with permission!) on the 6502 emulator by
	Juergen Buchmueller.  It is released as part of the Mame emulator project.
	Let me know if you intend to use this code in any other project.

******************************************************************************/
package gr.codebb.arcadeflex.WIP.v037b16.cpu.h6280;

import static gr.codebb.arcadeflex.WIP.v037b16.cpu.h6280.h6280H.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b16.mame.memory.*;
import static gr.codebb.arcadeflex.v037b16.mame.memoryH.*;
import static gr.codebb.arcadeflex.WIP.v037b16.cpu.h6280.h6280.*;

public class h6280ops {

    /* 6280 flags */
    public static final int _fC = 0x01;
    public static final int _fZ = 0x02;
    public static final int _fI = 0x04;
    public static final int _fD = 0x08;
    public static final int _fB = 0x10;
    public static final int _fT = 0x20;
    public static final int _fV = 0x40;
    public static final int _fN = 0x80;

/*TODO*////* some shortcuts for improved readability */
/*TODO*///#define A	h6280.a
/*TODO*///#define X	h6280.x
/*TODO*///#define Y	h6280.y
/*TODO*///#define P	h6280.p
/*TODO*///#define S	h6280.sp.b.l

/*TODO*///#if LAZY_FLAGS
/*TODO*///
/*TODO*///#define NZ	h6280.NZ
/*TODO*///#define SET_NZ(n)				
/*TODO*///	P &= ~_fT;					
/*TODO*///    NZ = ((n & _fN) << 8) | n
/*TODO*///
/*TODO*///#else
/*TODO*///
/*TODO*///#define SET_NZ(n)				
/*TODO*///	P = (P & ~(_fN|_fT|_fZ)) |	
/*TODO*///		(n & _fN) | 			
/*TODO*///		((n == 0) ? _fZ : 0)
/*TODO*///
/*TODO*///#endif
/*TODO*///
/*TODO*///#define EAL h6280.ea.b.l
/*TODO*///#define EAH h6280.ea.b.h
/*TODO*///#define EAW h6280.ea.w.l
/*TODO*///#define EAD h6280.ea.d
/*TODO*///
/*TODO*///#define ZPL h6280.zp.b.l
/*TODO*///#define ZPH h6280.zp.b.h
/*TODO*///#define ZPW h6280.zp.w.l
/*TODO*///#define ZPD h6280.zp.d
/*TODO*///
/*TODO*///#define PCL h6280.pc.b.l
/*TODO*///#define PCH h6280.pc.b.h
/*TODO*///#define PCW h6280.pc.w.l
/*TODO*///#define PCD h6280.pc.d

    public static void DO_INTERRUPT(int vector) {
        h6280.extra_cycles += 7;
        /* 7 cycles for an int */
        PUSH(h6280.pc.H);
        PUSH(h6280.pc.L);
        COMPOSE_P(0, _fB);
        PUSH(h6280.u8_p);
        h6280.u8_p = ((h6280.u8_p & ~_fD) | _fI) & 0xFF;
        /* knock out D and set I flag */
        h6280.pc.SetL(RDMEM(vector));
        h6280.pc.SetH(RDMEM((vector + 1)));
    }

    public static void CHECK_IRQ_LINES() {
        if ((h6280.u8_p & _fI) == 0) {
            if (h6280.irq_state[0] != CLEAR_LINE
                    && (h6280.u8_irq_mask & 0x2) == 0) {
                DO_INTERRUPT(H6280_IRQ1_VEC);
                (h6280.irq_callback).handler(0);
            } else if (h6280.irq_state[1] != CLEAR_LINE
                    && (h6280.u8_irq_mask & 0x1) == 0) {
                DO_INTERRUPT(H6280_IRQ2_VEC);
                (h6280.irq_callback).handler(1);
            } else if (h6280.irq_state[2] != CLEAR_LINE
                    && (h6280.u8_irq_mask & 0x4) == 0) {
                h6280.irq_state[2] = CLEAR_LINE;
                DO_INTERRUPT(H6280_TIMER_VEC);
            }
        }
    }

/**
     * *************************************************************
     * RDMEM read memory
     * *************************************************************
     */
    public static int RDMEM(int addr) {
        return cpu_readmem21((h6280.u8_mmr[(addr) >>> 13] << 13) | ((addr) & 0x1fff));
    }

    /**
     * *************************************************************
     * WRMEM write memory
     * *************************************************************
     */
    public static void WRMEM(int addr, int data) {
        cpu_writemem21((h6280.u8_mmr[(addr) >>> 13] << 13) | ((addr) & 0x1fff), data & 0xFF);
    }
    
/*TODO*////***************************************************************
/*TODO*/// *  RDMEMZ   read memory - zero page
/*TODO*/// ***************************************************************/
/*TODO*///#define RDMEMZ(addr) 											
/*TODO*///	cpu_readmem21( (h6280.mmr[1] << 13) | ((addr)&0x1fff));
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *  WRMEMZ   write memory - zero page
/*TODO*/// ***************************************************************/
/*TODO*///#define WRMEMZ(addr,data) 										
/*TODO*///	cpu_writemem21( (h6280.mmr[1] << 13) | ((addr)&0x1fff),data);
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *  RDMEMW   read word from memory
/*TODO*/// ***************************************************************/
/*TODO*///#define RDMEMW(addr)											
/*TODO*///	cpu_readmem21( (h6280.mmr[(addr)  >>13] << 13) | ((addr  )&0x1fff)) 
/*TODO*///| ( cpu_readmem21( (h6280.mmr[(addr+1)>>13] << 13) | ((addr+1)&0x1fff)) << 8 )
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *  RDZPWORD    read a word from a zero page address
/*TODO*/// ***************************************************************/
/*TODO*///#define RDZPWORD(addr)											
/*TODO*///	((addr&0xff)==0xff) ?										
/*TODO*///		cpu_readmem21( (h6280.mmr[1] << 13) | ((addr)&0x1fff))				
/*TODO*///		+(cpu_readmem21( (h6280.mmr[1] << 13) | ((addr-0xff)&0x1fff))<<8) : 
/*TODO*///		cpu_readmem21( (h6280.mmr[1] << 13) | ((addr)&0x1fff))				
/*TODO*///		+(cpu_readmem21( (h6280.mmr[1] << 13) | ((addr+1)&0x1fff))<<8)


    /***************************************************************
     * push a register onto the stack
     ***************************************************************/
    public static void PUSH(int Rg) {
        cpu_writemem21((h6280.u8_mmr[1] << 13) | h6280.sp.D, Rg);
        h6280.sp.AddL(-1);//S--
    }

/*TODO*////***************************************************************
/*TODO*/// * pull a register from the stack
/*TODO*/// ***************************************************************/
/*TODO*///#define PULL(Rg) S++; Rg = cpu_readmem21( (h6280.mmr[1] << 13) | h6280.sp.d)
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *  RDOP    read an opcode
/*TODO*/// ***************************************************************/
/*TODO*///#define RDOP()													
/*TODO*///	cpu_readop((h6280.mmr[PCW>>13] << 13) | (PCW&0x1fff))
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *  RDOPARG read an opcode argument
/*TODO*/// ***************************************************************/
/*TODO*///#define RDOPARG()												
/*TODO*///	cpu_readop_arg((h6280.mmr[PCW>>13] << 13) | (PCW&0x1fff))
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *	BRA  branch relative
/*TODO*/// ***************************************************************/
/*TODO*///#define BRA(cond)												
/*TODO*///	if (cond != 0)													
/*TODO*///	{															
/*TODO*///		h6280_ICount -= 4;										
/*TODO*///		tmp = RDOPARG();										
/*TODO*///		PCW++;													
/*TODO*///		EAW = PCW + (signed char)tmp;							
/*TODO*///		PCD = EAD;												
/*TODO*///	}															
/*TODO*///	else														
/*TODO*///	{															
/*TODO*///		PCW++;													
/*TODO*///		h6280_ICount -= 2;										
/*TODO*///	}
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *
/*TODO*/// * Helper macros to build the effective address
/*TODO*/// *
/*TODO*/// ***************************************************************/
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *  EA = zero page address
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_ZPG													
/*TODO*///	ZPL = RDOPARG();											
/*TODO*///	PCW++;														
/*TODO*///	EAD = ZPD
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *  EA = zero page address + X
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_ZPX													
/*TODO*///	ZPL = RDOPARG() + X;										
/*TODO*///	PCW++;														
/*TODO*///	EAD = ZPD
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *  EA = zero page address + Y
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_ZPY													
/*TODO*///	ZPL = RDOPARG() + Y;										
/*TODO*///	PCW++;														
/*TODO*///	EAD = ZPD
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *  EA = absolute address
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_ABS													
/*TODO*///	EAL = RDOPARG();											
/*TODO*///	PCW++;														
/*TODO*///	EAH = RDOPARG();											
/*TODO*///	PCW++
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *  EA = absolute address + X
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_ABX                                                  
/*TODO*///	EA_ABS; 													
/*TODO*///	EAW += X
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *	EA = absolute address + Y
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_ABY													
/*TODO*///	EA_ABS; 													
/*TODO*///	EAW += Y
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *	EA = zero page indirect (65c02 pre indexed w/o X)
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_ZPI													
/*TODO*///	ZPL = RDOPARG();											
/*TODO*///	PCW++;														
/*TODO*///	EAD = RDZPWORD(ZPD)
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *  EA = zero page + X indirect (pre indexed)
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_IDX													
/*TODO*///	ZPL = RDOPARG() + X;										
/*TODO*///	PCW++;														
/*TODO*///	EAD = RDZPWORD(ZPD);
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *  EA = zero page indirect + Y (post indexed)
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_IDY													
/*TODO*///	ZPL = RDOPARG();											
/*TODO*///	PCW++;														
/*TODO*///	EAD = RDZPWORD(ZPD);										
/*TODO*///	EAW += Y
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *	EA = indirect (only used by JMP)
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_IND													
/*TODO*///	EA_ABS; 													
/*TODO*///	tmp = RDMEM(EAD);											
/*TODO*///	EAD++; 														
/*TODO*///	EAH = RDMEM(EAD);											
/*TODO*///	EAL = tmp
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *	EA = indirect plus x (only used by JMP)
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_IAX                                                  
/*TODO*///	EA_ABS;														
/*TODO*///	EAD+=X;														
/*TODO*///	tmp = RDMEM(EAD);											
/*TODO*///	EAD++; 	 													
/*TODO*///	EAH = RDMEM(EAD);											
/*TODO*///	EAL = tmp
/*TODO*///
/*TODO*////* read a value into tmp */
/*TODO*///#define RD_IMM	tmp = RDOPARG(); PCW++
/*TODO*///#define RD_IMM2	tmp2 = RDOPARG(); PCW++
/*TODO*///#define RD_ACC	tmp = A
/*TODO*///#define RD_ZPG	EA_ZPG; tmp = RDMEMZ(EAD)
/*TODO*///#define RD_ZPX	EA_ZPX; tmp = RDMEMZ(EAD)
/*TODO*///#define RD_ZPY	EA_ZPY; tmp = RDMEMZ(EAD)
/*TODO*///#define RD_ABS	EA_ABS; tmp = RDMEM(EAD)
/*TODO*///#define RD_ABX	EA_ABX; tmp = RDMEM(EAD)
/*TODO*///#define RD_ABY	EA_ABY; tmp = RDMEM(EAD)
/*TODO*///#define RD_ZPI	EA_ZPI; tmp = RDMEM(EAD)
/*TODO*///#define RD_IDX	EA_IDX; tmp = RDMEM(EAD)
/*TODO*///#define RD_IDY	EA_IDY; tmp = RDMEM(EAD)
/*TODO*///
/*TODO*////* write a value from tmp */
/*TODO*///#define WR_ZPG	EA_ZPG; WRMEMZ(EAD, tmp)
/*TODO*///#define WR_ZPX	EA_ZPX; WRMEMZ(EAD, tmp)
/*TODO*///#define WR_ZPY	EA_ZPY; WRMEMZ(EAD, tmp)
/*TODO*///#define WR_ABS	EA_ABS; WRMEM(EAD, tmp)
/*TODO*///#define WR_ABX	EA_ABX; WRMEM(EAD, tmp)
/*TODO*///#define WR_ABY	EA_ABY; WRMEM(EAD, tmp)
/*TODO*///#define WR_ZPI	EA_ZPI; WRMEM(EAD, tmp)
/*TODO*///#define WR_IDX	EA_IDX; WRMEM(EAD, tmp)
/*TODO*///#define WR_IDY	EA_IDY; WRMEM(EAD, tmp)
/*TODO*///
/*TODO*////* write back a value from tmp to the last EA */
/*TODO*///#define WB_ACC	A = (UINT8)tmp;
/*TODO*///#define WB_EA	WRMEM(EAD, tmp)
/*TODO*///#define WB_EAZ	WRMEMZ(EAD, tmp)
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *
/*TODO*/// * Macros to emulate the 6280 opcodes
/*TODO*/// *
/*TODO*/// ***************************************************************/
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * compose the real flag register by
/*TODO*/// * including N and Z and set any
/*TODO*/// * SET and clear any CLR bits also
/*TODO*/// ***************************************************************/
/*TODO*///#if LAZY_FLAGS
/*TODO*///
/*TODO*///#define COMPOSE_P(SET,CLR)										
/*TODO*///	P = (P & ~(_fN | _fZ | CLR)) |								
/*TODO*///		(NZ >> 8) | 											
/*TODO*///		((NZ & 0xff) ? 0 : _fZ) |								
/*TODO*///		SET
/*TODO*///
/*TODO*///#else
/*TODO*///
/*TODO*///#define COMPOSE_P(SET,CLR)										
/*TODO*///	P = (P & ~CLR) | SET
     public static void COMPOSE_P(int SET, int CLR) {
        h6280.u8_p = ((h6280.u8_p & ~CLR) | SET) & 0xFF;
    }
/*TODO*///#endif
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	ADC Add with carry
/*TODO*/// ***************************************************************/
/*TODO*///#define ADC 													
/*TODO*///	if ((P & _fD) != 0)												
/*TODO*///	{															
/*TODO*///	int c = (P & _fC);											
/*TODO*///	int lo = (A & 0x0f) + (tmp & 0x0f) + c; 					
/*TODO*///	int hi = (A & 0xf0) + (tmp & 0xf0); 						
/*TODO*///		P &= ~(_fV | _fC);										
/*TODO*///		if (lo > 0x09)											
/*TODO*///		{														
/*TODO*///			hi += 0x10; 										
/*TODO*///			lo += 0x06; 										
/*TODO*///		}														
/*TODO*///		if (~(A^tmp) & (A^hi) & _fN)							
/*TODO*///			P |= _fV;											
/*TODO*///		if (hi > 0x90)											
/*TODO*///			hi += 0x60; 										
/*TODO*///		if ((hi & 0xff00) != 0)										
/*TODO*///			P |= _fC;											
/*TODO*///		A = (lo & 0x0f) + (hi & 0xf0);							
/*TODO*///	}															
/*TODO*///	else														
/*TODO*///	{															
/*TODO*///	int c = (P & _fC);											
/*TODO*///	int sum = A + tmp + c;										
/*TODO*///		P &= ~(_fV | _fC);										
/*TODO*///		if (~(A^tmp) & (A^sum) & _fN)							
/*TODO*///			P |= _fV;											
/*TODO*///		if ((sum & 0xff00) != 0)										
/*TODO*///			P |= _fC;											
/*TODO*///		A = (UINT8) sum;										
/*TODO*///	}															
/*TODO*///	SET_NZ(A)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	AND Logical and
/*TODO*/// ***************************************************************/
/*TODO*///#define AND 													
/*TODO*///	A = (UINT8)(A & tmp);										
/*TODO*///	SET_NZ(A)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	ASL Arithmetic shift left
/*TODO*/// ***************************************************************/
/*TODO*///#define ASL 													
/*TODO*///	P = (P & ~_fC) | ((tmp >> 7) & _fC);						
/*TODO*///	tmp = (UINT8)(tmp << 1);									
/*TODO*///	SET_NZ(tmp)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  BBR Branch if bit is reset
/*TODO*/// ***************************************************************/
/*TODO*///#define BBR(bit)                                                
/*TODO*///    BRA(!(tmp & (1<<bit)))
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  BBS Branch if bit is set
/*TODO*/// ***************************************************************/
/*TODO*///#define BBS(bit)                                                
/*TODO*///    BRA(tmp & (1<<bit))
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	BCC Branch if carry clear
/*TODO*/// ***************************************************************/
/*TODO*///#define BCC BRA(!(P & _fC))
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	BCS Branch if carry set
/*TODO*/// ***************************************************************/
/*TODO*///#define BCS BRA(P & _fC)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	BEQ Branch if equal
/*TODO*/// ***************************************************************/
/*TODO*///#if LAZY_FLAGS
/*TODO*///#define BEQ BRA(!(NZ & 0xff))
/*TODO*///#else
/*TODO*///#define BEQ BRA(P & _fZ)
/*TODO*///#endif
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	BIT Bit test
/*TODO*/// ***************************************************************/
/*TODO*///#define BIT														
/*TODO*///	P = (P & ~(_fN|_fV|_fT|_fZ))								
/*TODO*///		| ((tmp&0x80) ? _fN:0)									
/*TODO*///		| ((tmp&0x40) ? _fV:0)									
/*TODO*///		| ((tmp&A)  ? 0:_fZ)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	BMI Branch if minus
/*TODO*/// ***************************************************************/
/*TODO*///#if LAZY_FLAGS
/*TODO*///#define BMI BRA(NZ & 0x8000)
/*TODO*///#else
/*TODO*///#define BMI BRA(P & _fN)
/*TODO*///#endif
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	BNE Branch if not equal
/*TODO*/// ***************************************************************/
/*TODO*///#if LAZY_FLAGS
/*TODO*///#define BNE BRA(NZ & 0xff)
/*TODO*///#else
/*TODO*///#define BNE BRA(!(P & _fZ))
/*TODO*///#endif
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	BPL Branch if plus
/*TODO*/// ***************************************************************/
/*TODO*///#if LAZY_FLAGS
/*TODO*///#define BPL BRA(!(NZ & 0x8000))
/*TODO*///#else
/*TODO*///#define BPL BRA(!(P & _fN))
/*TODO*///#endif
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	BRK Break
/*TODO*/// *	increment PC, push PC hi, PC lo, flags (with B bit set),
/*TODO*/// *	set I flag, reset D flag and jump via IRQ vector
/*TODO*/// ***************************************************************/
/*TODO*///#define BRK 													
/*TODO*///	logerror("BRK %04xn",cpu_get_pc());	
/*TODO*///	PCW++;														
/*TODO*///	PUSH(PCH);													
/*TODO*///	PUSH(PCL);													
/*TODO*///	PUSH(P | _fB);												
/*TODO*///	P = (P & ~_fD) | _fI;										
/*TODO*///	PCL = RDMEM(H6280_IRQ2_VEC); 								
/*TODO*///	PCH = RDMEM(H6280_IRQ2_VEC+1)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	BSR Branch to subroutine
/*TODO*/// ***************************************************************/
/*TODO*///#define BSR 													
/*TODO*///	PUSH(PCH);													
/*TODO*///	PUSH(PCL);													
/*TODO*///	h6280_ICount -= 4; /* 4 cycles here, 4 in BRA */			
/*TODO*///	BRA(1)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	BVC Branch if overflow clear
/*TODO*/// ***************************************************************/
/*TODO*///#define BVC BRA(!(P & _fV))
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	BVS Branch if overflow set
/*TODO*/// ***************************************************************/
/*TODO*///#define BVS BRA(P & _fV)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  CLA Clear accumulator
/*TODO*/// ***************************************************************/
/*TODO*///#define CLA                                                     
/*TODO*///    A = 0
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	CLC Clear carry flag
/*TODO*/// ***************************************************************/
/*TODO*///#define CLC 													
/*TODO*///	P &= ~_fC
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	CLD Clear decimal flag
/*TODO*/// ***************************************************************/
/*TODO*///#define CLD 													
/*TODO*///	P &= ~_fD
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	CLI Clear interrupt flag
/*TODO*/// ***************************************************************/
/*TODO*///#define CLI 													
/*TODO*///	if ((P & _fI) != 0)												
/*TODO*///	{															
/*TODO*///		P &= ~_fI;												
/*TODO*///		CHECK_IRQ_LINES;										
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	CLV Clear overflow flag
/*TODO*/// ***************************************************************/
/*TODO*///#define CLV 													
/*TODO*///	P &= ~_fV
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  CLX Clear index X
/*TODO*/// ***************************************************************/
/*TODO*///#define CLX                                                     
/*TODO*///    X = 0
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  CLY Clear index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define CLY                                                     
/*TODO*///    Y = 0
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	CMP Compare accumulator
/*TODO*/// ***************************************************************/
/*TODO*///#define CMP 													
/*TODO*///	P &= ~_fC;													
/*TODO*///	if (A >= tmp)												
/*TODO*///		P |= _fC;												
/*TODO*///	SET_NZ((UINT8)(A - tmp))
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	CPX Compare index X
/*TODO*/// ***************************************************************/
/*TODO*///#define CPX 													
/*TODO*///	P &= ~_fC;													
/*TODO*///	if (X >= tmp)												
/*TODO*///		P |= _fC;												
/*TODO*///	SET_NZ((UINT8)(X - tmp))
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	CPY Compare index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define CPY 													
/*TODO*///	P &= ~_fC;													
/*TODO*///	if (Y >= tmp)												
/*TODO*///		P |= _fC;												
/*TODO*///	SET_NZ((UINT8)(Y - tmp))
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  DEA Decrement accumulator
/*TODO*/// ***************************************************************/
/*TODO*///#define DEA                                                     
/*TODO*///	A = (UINT8)--A; 											
/*TODO*///    SET_NZ(A)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	DEC Decrement memory
/*TODO*/// ***************************************************************/
/*TODO*///#define DEC 													
/*TODO*///	tmp = (UINT8)(tmp-1); 										
/*TODO*///	SET_NZ(tmp)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	DEX Decrement index X
/*TODO*/// ***************************************************************/
/*TODO*///#define DEX 													
/*TODO*///	X = (UINT8)--X; 											
/*TODO*///	SET_NZ(X)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	DEY Decrement index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define DEY 													
/*TODO*///	Y = (UINT8)--Y; 											
/*TODO*///	SET_NZ(Y)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	EOR Logical exclusive or
/*TODO*/// ***************************************************************/
/*TODO*///#define EOR 													
/*TODO*///	A = (UINT8)(A ^ tmp);										
/*TODO*///	SET_NZ(A)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	ILL Illegal opcode
/*TODO*/// ***************************************************************/
/*TODO*///#define ILL 													
/*TODO*///	h6280_ICount -= 2; /* (assumed) */							
/*TODO*///	logerror("%04x: WARNING - h6280 illegal opcoden",cpu_get_pc())
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  INA Increment accumulator
/*TODO*/// ***************************************************************/
/*TODO*///#define INA                                                     
/*TODO*///	A = (UINT8)++A; 											
/*TODO*///    SET_NZ(A)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	INC Increment memory
/*TODO*/// ***************************************************************/
/*TODO*///#define INC 													
/*TODO*///	tmp = (UINT8)(tmp+1); 										
/*TODO*///	SET_NZ(tmp)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	INX Increment index X
/*TODO*/// ***************************************************************/
/*TODO*///#define INX 													
/*TODO*///	X = (UINT8)++X; 											
/*TODO*///	SET_NZ(X)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	INY Increment index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define INY 													
/*TODO*///	Y = (UINT8)++Y; 											
/*TODO*///	SET_NZ(Y)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	JMP Jump to address
/*TODO*/// *	set PC to the effective address
/*TODO*/// ***************************************************************/
/*TODO*///#define JMP 													
/*TODO*///	PCD = EAD
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	JSR Jump to subroutine
/*TODO*/// *	decrement PC (sic!) push PC hi, push PC lo and set
/*TODO*/// *	PC to the effective address
/*TODO*/// ***************************************************************/
/*TODO*///#define JSR 													
/*TODO*///	PCW--;														
/*TODO*///	PUSH(PCH);													
/*TODO*///	PUSH(PCL);													
/*TODO*///	PCD = EAD
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	LDA Load accumulator
/*TODO*/// ***************************************************************/
/*TODO*///#define LDA 													
/*TODO*///	A = (UINT8)tmp; 											
/*TODO*///	SET_NZ(A)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	LDX Load index X
/*TODO*/// ***************************************************************/
/*TODO*///#define LDX 													
/*TODO*///	X = (UINT8)tmp; 											
/*TODO*///	SET_NZ(X)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	LDY Load index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define LDY 													
/*TODO*///	Y = (UINT8)tmp; 											
/*TODO*///	SET_NZ(Y)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	LSR Logic shift right
/*TODO*/// *	0 . [7][6][5][4][3][2][1][0] . C
/*TODO*/// ***************************************************************/
/*TODO*///#define LSR 													
/*TODO*///	P = (P & ~_fC) | (tmp & _fC);								
/*TODO*///	tmp = (UINT8)tmp >> 1;										
/*TODO*///	SET_NZ(tmp)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	NOP No operation
/*TODO*/// ***************************************************************/
/*TODO*///#define NOP
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	ORA Logical inclusive or
/*TODO*/// ***************************************************************/
/*TODO*///#define ORA 													
/*TODO*///	A = (UINT8)(A | tmp);										
/*TODO*///	SET_NZ(A)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	PHA Push accumulator
/*TODO*/// ***************************************************************/
/*TODO*///#define PHA 													
/*TODO*///	PUSH(A)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	PHP Push processor status (flags)
/*TODO*/// ***************************************************************/
/*TODO*///#define PHP 													
/*TODO*///	COMPOSE_P(0,0); 											
/*TODO*///	PUSH(P)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  PHX Push index X
/*TODO*/// ***************************************************************/
/*TODO*///#define PHX                                                     
/*TODO*///    PUSH(X)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  PHY Push index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define PHY                                                     
/*TODO*///    PUSH(Y)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	PLA Pull accumulator
/*TODO*/// ***************************************************************/
/*TODO*///#define PLA 													
/*TODO*///	PULL(A);													
/*TODO*///	SET_NZ(A)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	PLP Pull processor status (flags)
/*TODO*/// ***************************************************************/
/*TODO*///#if LAZY_FLAGS
/*TODO*///
/*TODO*///#define PLP 													
/*TODO*///	PULL(P);													
/*TODO*///	NZ = ((P & _fN) << 8) | 									
/*TODO*///		 ((P & _fZ) ^ _fZ); 									
/*TODO*///	CHECK_IRQ_LINES
/*TODO*///
/*TODO*///#else
/*TODO*///
/*TODO*///#define PLP 													
/*TODO*///	PULL(P); 													
/*TODO*///	CHECK_IRQ_LINES
/*TODO*///#endif
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  PLX Pull index X
/*TODO*/// ***************************************************************/
/*TODO*///#define PLX                                                     
/*TODO*///    PULL(X)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  PLY Pull index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define PLY                                                     
/*TODO*///    PULL(Y)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  RMB Reset memory bit
/*TODO*/// ***************************************************************/
/*TODO*///#define RMB(bit)                                                
/*TODO*///    tmp &= ~(1<<bit)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	ROL Rotate left
/*TODO*/// *	new C <- [7][6][5][4][3][2][1][0] <- C
/*TODO*/// ***************************************************************/
/*TODO*///#define ROL 													
/*TODO*///	tmp = (tmp << 1) | (P & _fC);								
/*TODO*///	P = (P & ~_fC) | ((tmp >> 8) & _fC);						
/*TODO*///	tmp = (UINT8)tmp;											
/*TODO*///	SET_NZ(tmp)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	ROR Rotate right
/*TODO*/// *	C . [7][6][5][4][3][2][1][0] . new C
/*TODO*/// ***************************************************************/
/*TODO*///#define ROR 													
/*TODO*///	tmp |= (P & _fC) << 8;										
/*TODO*///	P = (P & ~_fC) | (tmp & _fC);								
/*TODO*///	tmp = (UINT8)(tmp >> 1);									
/*TODO*///	SET_NZ(tmp)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	RTI Return from interrupt
/*TODO*/// *	pull flags, pull PC lo, pull PC hi and increment PC
/*TODO*/// ***************************************************************/
/*TODO*///#if LAZY_FLAGS
/*TODO*///
/*TODO*///#define RTI 													
/*TODO*///	PULL(P);													
/*TODO*///	NZ = ((P & _fN) << 8) | 									
/*TODO*///		 ((P & _fZ) ^ _fZ); 									
/*TODO*///	PULL(PCL);													
/*TODO*///	PULL(PCH);													
/*TODO*///	CHECK_IRQ_LINES
/*TODO*///#else
/*TODO*///
/*TODO*///#define RTI 													
/*TODO*///	PULL(P);													
/*TODO*///	PULL(PCL);													
/*TODO*///	PULL(PCH);													
/*TODO*///	CHECK_IRQ_LINES
/*TODO*///#endif
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	RTS Return from subroutine
/*TODO*/// *	pull PC lo, PC hi and increment PC
/*TODO*/// ***************************************************************/
/*TODO*///#define RTS 													
/*TODO*///	PULL(PCL);													
/*TODO*///	PULL(PCH);													
/*TODO*///	PCW++;														
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  SAX Swap accumulator and index X
/*TODO*/// ***************************************************************/
/*TODO*///#define SAX                                                     
/*TODO*///    tmp = X;                                                    
/*TODO*///    X = A;                                                      
/*TODO*///    A = tmp
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  SAY Swap accumulator and index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define SAY                                                     
/*TODO*///    tmp = Y;                                                    
/*TODO*///    Y = A;                                                      
/*TODO*///    A = tmp
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	SBC Subtract with carry
/*TODO*/// ***************************************************************/
/*TODO*///#define SBC 													
/*TODO*///	if ((P & _fD) != 0)												
/*TODO*///	{															
/*TODO*///	int c = (P & _fC) ^ _fC;									
/*TODO*///	int sum = A - tmp - c;										
/*TODO*///	int lo = (A & 0x0f) - (tmp & 0x0f) - c; 					
/*TODO*///	int hi = (A & 0xf0) - (tmp & 0xf0); 						
/*TODO*///		P &= ~(_fV | _fC);										
/*TODO*///		if ((A^tmp) & (A^sum) & _fN)							
/*TODO*///			P |= _fV;											
/*TODO*///		if ((lo & 0xf0) != 0)											
/*TODO*///			lo -= 6;											
/*TODO*///		if ((lo & 0x80) != 0)											
/*TODO*///			hi -= 0x10; 										
/*TODO*///		if ((hi & 0x0f00) != 0)										
/*TODO*///			hi -= 0x60; 										
/*TODO*///		if ((sum & 0xff00) == 0)								
/*TODO*///			P |= _fC;											
/*TODO*///		A = (lo & 0x0f) + (hi & 0xf0);							
/*TODO*///	}															
/*TODO*///	else														
/*TODO*///	{															
/*TODO*///	int c = (P & _fC) ^ _fC;									
/*TODO*///	int sum = A - tmp - c;										
/*TODO*///		P &= ~(_fV | _fC);										
/*TODO*///		if ((A^tmp) & (A^sum) & _fN)							
/*TODO*///			P |= _fV;											
/*TODO*///		if ((sum & 0xff00) == 0)								
/*TODO*///			P |= _fC;											
/*TODO*///		A = (UINT8) sum;										
/*TODO*///	}															
/*TODO*///	SET_NZ(A)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	SEC Set carry flag
/*TODO*/// ***************************************************************/
/*TODO*///#define SEC 													
/*TODO*///	P |= _fC
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	SED Set decimal flag
/*TODO*/// ***************************************************************/
/*TODO*///#define SED 													
/*TODO*///	P |= _fD
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	SEI Set interrupt flag
/*TODO*/// ***************************************************************/
/*TODO*///#define SEI 													
/*TODO*///	P |= _fI
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	SET Set t flag
/*TODO*/// ***************************************************************/
/*TODO*///#define SET 													
/*TODO*///	P |= _fT;													
/*TODO*///	logerror("%04x: WARNING H6280 SETn",cpu_get_pc())
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  SMB Set memory bit
/*TODO*/// ***************************************************************/
/*TODO*///#define SMB(bit)                                                
/*TODO*///    tmp |= (1<<bit)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  ST0 Store at hardware address 0
/*TODO*/// ***************************************************************/
/*TODO*///#define ST0                                                     
/*TODO*///    cpu_writeport16(0x0000,tmp)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  ST1 Store at hardware address 2
/*TODO*/// ***************************************************************/
/*TODO*///#define ST1                                                     
/*TODO*///    cpu_writeport16(0x0002,tmp)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  ST2 Store at hardware address 3
/*TODO*/// ***************************************************************/
/*TODO*///#define ST2                                                     
/*TODO*///    cpu_writeport16(0x0003,tmp)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	STA Store accumulator
/*TODO*/// ***************************************************************/
/*TODO*///#define STA 													
/*TODO*///	tmp = A
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	STX Store index X
/*TODO*/// ***************************************************************/
/*TODO*///#define STX 													
/*TODO*///	tmp = X
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	STY Store index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define STY 													
/*TODO*///	tmp = Y
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// * STZ  Store zero
/*TODO*/// ***************************************************************/
/*TODO*///#define STZ                                                     
/*TODO*///    tmp = 0
/*TODO*///
/*TODO*////* H6280 *******************************************************
/*TODO*/// *  SXY Swap index X and index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define SXY                                                    
/*TODO*///    tmp = X;                                                   
/*TODO*///    X = Y;                                                     
/*TODO*///    Y = tmp
/*TODO*///
/*TODO*////* H6280 *******************************************************
/*TODO*/// *  TAI
/*TODO*/// ***************************************************************/
/*TODO*///#define TAI 													
/*TODO*///	from=RDMEMW(PCW);											
/*TODO*///	to  =RDMEMW(PCW+2);											
/*TODO*///	length=RDMEMW(PCW+4);										
/*TODO*///	PCW+=6; 													
/*TODO*///	alternate=0; 												
/*TODO*///	while ((length--) != 0) { 									
/*TODO*///		WRMEM(to,RDMEM(from+alternate)); 						
/*TODO*///		to++; 													
/*TODO*///		alternate ^= 1; 										
/*TODO*///	}		 													
/*TODO*///	h6280_ICount-=(6 * length) + 17;
/*TODO*///
/*TODO*////* H6280 *******************************************************
/*TODO*/// *  TAM Transfer accumulator to memory mapper register(s)
/*TODO*/// ***************************************************************/
/*TODO*///#define TAM                                                     
/*TODO*///    if ((tmp & 0x01) != 0) h6280.mmr[0] = A;                             
/*TODO*///    if ((tmp & 0x02) != 0) h6280.mmr[1] = A;                             
/*TODO*///    if ((tmp & 0x04) != 0) h6280.mmr[2] = A;                             
/*TODO*///    if ((tmp & 0x08) != 0) h6280.mmr[3] = A;                             
/*TODO*///    if ((tmp & 0x10) != 0) h6280.mmr[4] = A;                             
/*TODO*///    if ((tmp & 0x20) != 0) h6280.mmr[5] = A;                             
/*TODO*///    if ((tmp & 0x40) != 0) h6280.mmr[6] = A;                             
/*TODO*///    if ((tmp & 0x80) != 0) h6280.mmr[7] = A
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	TAX Transfer accumulator to index X
/*TODO*/// ***************************************************************/
/*TODO*///#define TAX 													
/*TODO*///	X = A;														
/*TODO*///	SET_NZ(X)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	TAY Transfer accumulator to index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define TAY 													
/*TODO*///	Y = A;														
/*TODO*///	SET_NZ(Y)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  TDD
/*TODO*/// ***************************************************************/
/*TODO*///#define TDD 													
/*TODO*///	from=RDMEMW(PCW);											
/*TODO*///	to  =RDMEMW(PCW+2);											
/*TODO*///	length=RDMEMW(PCW+4);										
/*TODO*///	PCW+=6; 													
/*TODO*///	while ((length--) != 0) { 									
/*TODO*///		WRMEM(to,RDMEM(from)); 									
/*TODO*///		to--; 													
/*TODO*///		from--;													
/*TODO*///	}		 													
/*TODO*///	h6280_ICount-=(6 * length) + 17;
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  TIA
/*TODO*/// ***************************************************************/
/*TODO*///#define TIA 													
/*TODO*///	from=RDMEMW(PCW);											
/*TODO*///	to  =RDMEMW(PCW+2);											
/*TODO*///	length=RDMEMW(PCW+4);										
/*TODO*///	PCW+=6; 													
/*TODO*///	alternate=0; 												
/*TODO*///	while ((length--) != 0) { 									
/*TODO*///		WRMEM(to+alternate,RDMEM(from));						
/*TODO*///		from++; 												
/*TODO*///		alternate ^= 1; 										
/*TODO*///	}		 													
/*TODO*///	h6280_ICount-=(6 * length) + 17;
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  TII
/*TODO*/// ***************************************************************/
/*TODO*///#define TII 													
/*TODO*///	from=RDMEMW(PCW);											
/*TODO*///	to  =RDMEMW(PCW+2);											
/*TODO*///	length=RDMEMW(PCW+4);										
/*TODO*///	PCW+=6; 													
/*TODO*///	while ((length--) != 0) { 									
/*TODO*///		WRMEM(to,RDMEM(from)); 									
/*TODO*///		to++; 													
/*TODO*///		from++;													
/*TODO*///	}		 													
/*TODO*///	h6280_ICount-=(6 * length) + 17;
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  TIN Transfer block, source increments every loop
/*TODO*/// ***************************************************************/
/*TODO*///#define TIN 													
/*TODO*///	from=RDMEMW(PCW);											
/*TODO*///	to  =RDMEMW(PCW+2);											
/*TODO*///	length=RDMEMW(PCW+4);										
/*TODO*///	PCW+=6; 													
/*TODO*///	while ((length--) != 0) { 									
/*TODO*///		WRMEM(to,RDMEM(from)); 									
/*TODO*///		from++;													
/*TODO*///	}		 													
/*TODO*///	h6280_ICount-=(6 * length) + 17;
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  TMA Transfer memory mapper register(s) to accumulator
/*TODO*/// *  the highest bit set in tmp is the one that counts
/*TODO*/// ***************************************************************/
/*TODO*///#define TMA                                                     
/*TODO*///    if ((tmp & 0x01) != 0) A = h6280.mmr[0];                             
/*TODO*///    if ((tmp & 0x02) != 0) A = h6280.mmr[1];                             
/*TODO*///    if ((tmp & 0x04) != 0) A = h6280.mmr[2];                             
/*TODO*///    if ((tmp & 0x08) != 0) A = h6280.mmr[3];                             
/*TODO*///    if ((tmp & 0x10) != 0) A = h6280.mmr[4];                             
/*TODO*///    if ((tmp & 0x20) != 0) A = h6280.mmr[5];                             
/*TODO*///    if ((tmp & 0x40) != 0) A = h6280.mmr[6];                             
/*TODO*///    if ((tmp & 0x80) != 0) A = h6280.mmr[7]
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// * TRB  Test and reset bits
/*TODO*/// ***************************************************************/
/*TODO*///#define TRB                                                   	
/*TODO*///	P = (P & ~(_fN|_fV|_fT|_fZ))								
/*TODO*///		| ((tmp&0x80) ? _fN:0)									
/*TODO*///		| ((tmp&0x40) ? _fV:0)									
/*TODO*///		| ((tmp&A)  ? 0:_fZ);									
/*TODO*///    tmp &= ~A
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// * TSB  Test and set bits
/*TODO*/// ***************************************************************/
/*TODO*///#define TSB                                                     
/*TODO*///	P = (P & ~(_fN|_fV|_fT|_fZ))								
/*TODO*///		| ((tmp&0x80) ? _fN:0)									
/*TODO*///		| ((tmp&0x40) ? _fV:0)									
/*TODO*///		| ((tmp&A)  ? 0:_fZ);									
/*TODO*///    tmp |= A
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	TSX Transfer stack LSB to index X
/*TODO*/// ***************************************************************/
/*TODO*///#define TSX 													
/*TODO*///	X = S;														
/*TODO*///	SET_NZ(X)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	TST
/*TODO*/// ***************************************************************/
/*TODO*///#define TST														
/*TODO*///	P = (P & ~(_fN|_fV|_fT|_fZ))								
/*TODO*///		| ((tmp2&0x80) ? _fN:0)									
/*TODO*///		| ((tmp2&0x40) ? _fV:0)									
/*TODO*///		| ((tmp2&tmp)  ? 0:_fZ)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	TXA Transfer index X to accumulator
/*TODO*/// ***************************************************************/
/*TODO*///#define TXA 													
/*TODO*///	A = X;														
/*TODO*///	SET_NZ(A)
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	TXS Transfer index X to stack LSB
/*TODO*/// *	no flags changed (sic!)
/*TODO*/// ***************************************************************/
/*TODO*///#define TXS 													
/*TODO*///	S = X
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	TYA Transfer index Y to accumulator
/*TODO*/// ***************************************************************/
/*TODO*///#define TYA 													
/*TODO*///	A = Y;														
/*TODO*///	SET_NZ(A)
    
}
