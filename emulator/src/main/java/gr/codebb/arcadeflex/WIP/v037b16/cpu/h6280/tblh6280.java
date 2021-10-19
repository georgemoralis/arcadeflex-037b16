/*****************************************************************************

    tblh6280.c

	Copyright (c) 1999 Bryan McPhail, mish@tendril.co.uk

	This source code is based (with permission!) on the 6502 emulator by
	Juergen Buchmueller.  It is released as part of the Mame emulator project.
	Let me know if you intend to use this code in any other project.

******************************************************************************/
package gr.codebb.arcadeflex.WIP.v037b16.cpu.h6280;

import static gr.codebb.arcadeflex.WIP.v037b16.cpu.h6280.h6280.h6280log;
import static common.libc.cstdio.*;

public class tblh6280 {
    
    public static abstract interface opcode {
        public abstract void handler();
    }

    /*****************************************************************************
     *****************************************************************************
     *
     *	 Hu6280 opcodes
     *
     *****************************************************************************
     * op	  temp	   cycles		      rdmem	  opc   wrmem   ******************/
    public static opcode h6280_000 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 8;		  BRK;		   } // 8 BRK
    public static opcode h6280_020 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 7; EA_ABS; JSR;		   } // 7 JSR  ABS
    public static opcode h6280_040 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 7;		  RTI;		   } // 7 RTI
    public static opcode h6280_060 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 7;		  RTS;		   } // 7 RTS
    public static opcode h6280_080 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp;  				          BRA(1);	   } // 4 BRA  REL
    public static opcode h6280_0a0 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 2; RD_IMM; LDY;		   } // 2 LDY  IMM
    public static opcode h6280_0c0 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 2; RD_IMM; CPY;		   } // 2 CPY  IMM
    public static opcode h6280_0e0 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 2; RD_IMM; CPX;		   } // 2 CPX  IMM

    public static opcode h6280_010 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp;							  BPL;		   } // 2/4 BPL  REL
    public static opcode h6280_030 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp;							  BMI;		   } // 2/4 BMI  REL
    public static opcode h6280_050 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp;							  BVC;		   } // 2/4 BVC  REL
    public static opcode h6280_070 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp;							  BVS;		   } // 2/4 BVS  REL
    public static opcode h6280_090 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp;							  BCC;		   } // 2/4 BCC  REL
    public static opcode h6280_0b0 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp;							  BCS;		   } // 2/4 BCS  REL
    public static opcode h6280_0d0 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp;							  BNE;		   } // 2/4 BNE  REL
    public static opcode h6280_0f0 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp;							  BEQ;		   } // 2/4 BEQ  REL

    public static opcode h6280_001 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_IDX; ORA;		   } // 7 ORA  IDX
    public static opcode h6280_021 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_IDX; AND;		   } // 7 AND  IDX
    public static opcode h6280_041 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_IDX; EOR;		   } // 7 EOR  IDX
    public static opcode h6280_061 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_IDX; ADC;		   } // 7 ADC  IDX
    public static opcode h6280_081 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7;         STA; WR_IDX; } // 7 STA  IDX
    public static opcode h6280_0a1 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_IDX; LDA;		   } // 7 LDA  IDX
    public static opcode h6280_0c1 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_IDX; CMP;		   } // 7 CMP  IDX
    public static opcode h6280_0e1 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_IDX; SBC;		   } // 7 SBC  IDX

    public static opcode h6280_011 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_IDY; ORA;		   } // 7 ORA  IDY
    public static opcode h6280_031 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_IDY; AND;		   } // 7 AND  IDY
    public static opcode h6280_051 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_IDY; EOR;		   } // 7 EOR  IDY
    public static opcode h6280_071 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_IDY; ADC;		   } // 7 ADC  AZP
    public static opcode h6280_091 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7;		  STA; WR_IDY; } // 7 STA  IDY
    public static opcode h6280_0b1 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_IDY; LDA;		   } // 7 LDA  IDY
    public static opcode h6280_0d1 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_IDY; CMP;		   } // 7 CMP  IDY
    public static opcode h6280_0f1 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_IDY; SBC;		   } // 7 SBC  IDY

    public static opcode h6280_002 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 3;		  SXY;		   } // 3 SXY
    public static opcode h6280_022 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 3;		  SAX;		   } // 3 SAX
    public static opcode h6280_042 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 3;		  SAY;		   } // 3 SAY
    public static opcode h6280_062 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  CLA;		   } // 2 CLA
    public static opcode h6280_082 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  CLX;		   } // 2 CLX
    public static opcode h6280_0a2 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 2; RD_IMM; LDX;		   } // 2 LDX  IMM
    public static opcode h6280_0c2 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  CLY;		   } // 2 CLY
    public static opcode h6280_0e2 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???

    public static opcode h6280_012 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPI; ORA;		   } // 7 ORA  ZPI
    public static opcode h6280_032 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPI; AND;		   } // 7 AND  ZPI
    public static opcode h6280_052 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPI; EOR;		   } // 7 EOR  ZPI
    public static opcode h6280_072 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPI; ADC;		   } // 7 ADC  ZPI
    public static opcode h6280_092 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7;		  STA; WR_ZPI; } // 7 STA  ZPI
    public static opcode h6280_0b2 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPI; LDA;		   } // 7 LDA  ZPI
    public static opcode h6280_0d2 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPI; CMP;		   } // 7 CMP  ZPI
    public static opcode h6280_0f2 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPI; SBC;		   } // 7 SBC  ZPI

    public static opcode h6280_003 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_IMM; ST0;		   } // 4 ST0  IMM
    public static opcode h6280_023 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_IMM; ST2;		   } // 4 ST2  IMM
    public static opcode h6280_043 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_IMM; TMA;		   } // 4 TMA
    public static opcode h6280_063 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???
    public static opcode h6280_083 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp,tmp2; h6280_ICount -= 7; RD_IMM2; RD_ZPG; TST; } // 7 TST  IMM,ZPG
    public static opcode h6280_0a3 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp,tmp2; h6280_ICount -= 7; RD_IMM2; RD_ZPX; TST; } // 7 TST  IMM,ZPX
    public static opcode h6280_0c3 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int to,from,length;			      TDD;		   } // 6*l+17 TDD  XFER
    public static opcode h6280_0e3 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int to,from,length,alternate;       TIA;		   } // 6*l+17 TIA  XFER

    public static opcode h6280_013 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_IMM; ST1;		   } // 4 ST1
    public static opcode h6280_033 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///       							  ILL;		   } // 2 ???
    public static opcode h6280_053 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_IMM; TAM;		   } // 5 TAM  IMM
    public static opcode h6280_073 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int to,from,length;    			  TII;		   } // 6*l+17 TII  XFER
    public static opcode h6280_093 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp,tmp2; h6280_ICount -= 8; RD_IMM2; RD_ABS; TST; } // 8 TST  IMM,ABS
    public static opcode h6280_0b3 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp,tmp2; h6280_ICount -= 8; RD_IMM2; RD_ABX; TST; } // 8 TST  IMM,ABX
    public static opcode h6280_0d3 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int to,from,length;			      TIN;		   } // 6*l+17 TIN  XFER
    public static opcode h6280_0f3 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int to,from,length,alternate;       TAI;		   } // 6*l+17 TAI  XFER

    public static opcode h6280_004 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 6; RD_ZPG; TSB; WB_EAZ; } // 6 TSB  ZPG
    public static opcode h6280_024 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; BIT;		   } // 4 BIT  ZPG
    public static opcode h6280_044 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp;							  BSR;		   } // 8 BSR  REL
    public static opcode h6280_064 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4;		  STZ; WR_ZPG; } // 4 STZ  ZPG
    public static opcode h6280_084 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4;		  STY; WR_ZPG; } // 4 STY  ZPG
    public static opcode h6280_0a4 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; LDY;		   } // 4 LDY  ZPG
    public static opcode h6280_0c4 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; CPY;		   } // 4 CPY  ZPG
    public static opcode h6280_0e4 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; CPX;		   } // 4 CPX  ZPG

    public static opcode h6280_014 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 6; RD_ZPG; TRB; WB_EAZ; } // 6 TRB  ZPG
    public static opcode h6280_034 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPX; BIT;		   } // 4 BIT  ZPX
    public static opcode h6280_054 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;					   } // 2 CSL
    public static opcode h6280_074 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4;		  STZ; WR_ZPX; } // 4 STZ  ZPX
    public static opcode h6280_094 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4;		  STY; WR_ZPX; } // 4 STY  ZPX
    public static opcode h6280_0b4 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPX; LDY;		   } // 4 LDY  ZPX
    public static opcode h6280_0d4 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;					   } // 2 CSH
    public static opcode h6280_0f4 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  SET;		   } // 2 SET

    public static opcode h6280_005 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; ORA;		   } // 4 ORA  ZPG
    public static opcode h6280_025 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; AND;		   } // 4 AND  ZPG
    public static opcode h6280_045 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; EOR;		   } // 4 EOR  ZPG
    public static opcode h6280_065 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; ADC;		   } // 4 ADC  ZPG
    public static opcode h6280_085 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4;		  STA; WR_ZPG; } // 4 STA  ZPG
    public static opcode h6280_0a5 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; LDA;		   } // 4 LDA  ZPG
    public static opcode h6280_0c5 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; CMP;		   } // 4 CMP  ZPG
    public static opcode h6280_0e5 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; SBC;		   } // 4 SBC  ZPG

    public static opcode h6280_015 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPX; ORA;		   } // 4 ORA  ZPX
    public static opcode h6280_035 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPX; AND;		   } // 4 AND  ZPX
    public static opcode h6280_055 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPX; EOR;		   } // 4 EOR  ZPX
    public static opcode h6280_075 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPX; ADC;		   } // 4 ADC  ZPX
    public static opcode h6280_095 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4;		  STA; WR_ZPX; } // 4 STA  ZPX
    public static opcode h6280_0b5 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPX; LDA;		   } // 4 LDA  ZPX
    public static opcode h6280_0d5 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPX; CMP;		   } // 4 CMP  ZPX
    public static opcode h6280_0f5 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPX; SBC;		   } // 4 SBC  ZPX

    public static opcode h6280_006 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 6; RD_ZPG; ASL; WB_EAZ; } // 6 ASL  ZPG
    public static opcode h6280_026 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 6; RD_ZPG; ROL; WB_EAZ; } // 6 ROL  ZPG
    public static opcode h6280_046 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 6; RD_ZPG; LSR; WB_EAZ; } // 6 LSR  ZPG
    public static opcode h6280_066 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 6; RD_ZPG; ROR; WB_EAZ; } // 6 ROR  ZPG
    public static opcode h6280_086 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4;		  STX; WR_ZPG; } // 4 STX  ZPG
    public static opcode h6280_0a6 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; LDX;		   } // 4 LDX  ZPG
    public static opcode h6280_0c6 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 6; RD_ZPG; DEC; WB_EAZ; } // 6 DEC  ZPG
    public static opcode h6280_0e6 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 6; RD_ZPG; INC; WB_EAZ; } // 6 INC  ZPG

    public static opcode h6280_016 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 6; RD_ZPX; ASL; WB_EAZ  } // 6 ASL  ZPX
    public static opcode h6280_036 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 6; RD_ZPX; ROL; WB_EAZ  } // 6 ROL  ZPX
    public static opcode h6280_056 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 6; RD_ZPX; LSR; WB_EAZ  } // 6 LSR  ZPX
    public static opcode h6280_076 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 6; RD_ZPX; ROR; WB_EAZ  } // 6 ROR  ZPX
    public static opcode h6280_096 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4;		  STX; WR_ZPY; } // 4 STX  ZPY
    public static opcode h6280_0b6 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPY; LDX;		   } // 4 LDX  ZPY
    public static opcode h6280_0d6 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 6; RD_ZPX; DEC; WB_EAZ; } // 6 DEC  ZPX
    public static opcode h6280_0f6 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 6; RD_ZPX; INC; WB_EAZ; } // 6 INC  ZPX

    public static opcode h6280_007 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPG; RMB(0);WB_EAZ;} // 7 RMB0 ZPG
    public static opcode h6280_027 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPG; RMB(2);WB_EAZ;} // 7 RMB2 ZPG
    public static opcode h6280_047 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPG; RMB(4);WB_EAZ;} // 7 RMB4 ZPG
    public static opcode h6280_067 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPG; RMB(6);WB_EAZ;} // 7 RMB6 ZPG
    public static opcode h6280_087 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPG; SMB(0);WB_EAZ;} // 7 SMB0 ZPG
    public static opcode h6280_0a7 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPG; SMB(2);WB_EAZ;} // 7 SMB2 ZPG
    public static opcode h6280_0c7 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPG; SMB(4);WB_EAZ;} // 7 SMB4 ZPG
    public static opcode h6280_0e7 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPG; SMB(6);WB_EAZ;} // 7 SMB6 ZPG

    public static opcode h6280_017 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPG; RMB(1);WB_EAZ;} // 7 RMB1 ZPG
    public static opcode h6280_037 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPG; RMB(3);WB_EAZ;} // 7 RMB3 ZPG
    public static opcode h6280_057 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPG; RMB(5);WB_EAZ;} // 7 RMB5 ZPG
    public static opcode h6280_077 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPG; RMB(7);WB_EAZ;} // 7 RMB7 ZPG
    public static opcode h6280_097 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPG; SMB(1);WB_EAZ;} // 7 SMB1 ZPG
    public static opcode h6280_0b7 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPG; SMB(3);WB_EAZ;} // 7 SMB3 ZPG
    public static opcode h6280_0d7 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPG; SMB(5);WB_EAZ;} // 7 SMB5 ZPG
    public static opcode h6280_0f7 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ZPG; SMB(7);WB_EAZ;} // 7 SMB7 ZPG

    public static opcode h6280_008 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 3;		  PHP;		   } // 3 PHP
    public static opcode h6280_028 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 4;		  PLP;		   } // 4 PLP
    public static opcode h6280_048 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 3;		  PHA;		   } // 3 PHA
    public static opcode h6280_068 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 4;		  PLA;		   } // 4 PLA
    public static opcode h6280_088 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  DEY;		   } // 2 DEY
    public static opcode h6280_0a8 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  TAY;		   } // 2 TAY
    public static opcode h6280_0c8 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  INY;		   } // 2 INY
    public static opcode h6280_0e8 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  INX;		   } // 2 INX

    public static opcode h6280_018 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  CLC;		   } // 2 CLC
    public static opcode h6280_038 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  SEC;		   } // 2 SEC
    public static opcode h6280_058 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  CLI;		   } // 2 CLI
    public static opcode h6280_078 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  SEI;		   } // 2 SEI
    public static opcode h6280_098 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  TYA;		   } // 2 TYA
    public static opcode h6280_0b8 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  CLV;		   } // 2 CLV
    public static opcode h6280_0d8 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  CLD;		   } // 2 CLD
    public static opcode h6280_0f8 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  SED;		   } // 2 SED

    public static opcode h6280_009 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 2; RD_IMM; ORA;		   } // 2 ORA  IMM
    public static opcode h6280_029 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 2; RD_IMM; AND;		   } // 2 AND  IMM
    public static opcode h6280_049 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 2; RD_IMM; EOR;		   } // 2 EOR  IMM
    public static opcode h6280_069 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 2; RD_IMM; ADC;		   } // 2 ADC  IMM
    public static opcode h6280_089 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 2; RD_IMM; BIT;		   } // 2 BIT  IMM
    public static opcode h6280_0a9 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 2; RD_IMM; LDA;		   } // 2 LDA  IMM
    public static opcode h6280_0c9 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 2; RD_IMM; CMP;		   } // 2 CMP  IMM
    public static opcode h6280_0e9 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 2; RD_IMM; SBC;		   } // 2 SBC  IMM

    public static opcode h6280_019 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABY; ORA;		   } // 5 ORA  ABY
    public static opcode h6280_039 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABY; AND;		   } // 5 AND  ABY
    public static opcode h6280_059 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABY; EOR;		   } // 5 EOR  ABY
    public static opcode h6280_079 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABY; ADC;		   } // 5 ADC  ABY
    public static opcode h6280_099 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5;		  STA; WR_ABY; } // 5 STA  ABY
    public static opcode h6280_0b9 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABY; LDA;		   } // 5 LDA  ABY
    public static opcode h6280_0d9 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABY; CMP;		   } // 5 CMP  ABY
    public static opcode h6280_0f9 = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABY; SBC;		   } // 5 SBC  ABY

    public static opcode h6280_00a = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 2; RD_ACC; ASL; WB_ACC; } // 2 ASL  A
    public static opcode h6280_02a = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 2; RD_ACC; ROL; WB_ACC; } // 2 ROL  A
    public static opcode h6280_04a = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 2; RD_ACC; LSR; WB_ACC; } // 2 LSR  A
    public static opcode h6280_06a = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 2; RD_ACC; ROR; WB_ACC; } // 2 ROR  A
    public static opcode h6280_08a = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  TXA;		   } // 2 TXA
    public static opcode h6280_0aa = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  TAX;		   } // 2 TAX
    public static opcode h6280_0ca = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  DEX;		   } // 2 DEX
    public static opcode h6280_0ea = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  NOP;		   } // 2 NOP

    public static opcode h6280_01a = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  INA;		   } // 2 INC  A
    public static opcode h6280_03a = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  DEA;		   } // 2 DEC  A
    public static opcode h6280_05a = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 3;		  PHY;		   } // 3 PHY
    public static opcode h6280_07a = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 4;		  PLY;		   } // 4 PLY
    public static opcode h6280_09a = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  TXS;		   } // 2 TXS
    public static opcode h6280_0ba = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 2;		  TSX;		   } // 2 TSX
    public static opcode h6280_0da = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 3;		  PHX;		   } // 3 PHX
    public static opcode h6280_0fa = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 4;		  PLX;		   } // 4 PLX

    public static opcode h6280_00b = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???
    public static opcode h6280_02b = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???
    public static opcode h6280_04b = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???
    public static opcode h6280_06b = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???
    public static opcode h6280_08b = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???
    public static opcode h6280_0ab = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???
    public static opcode h6280_0cb = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???
    public static opcode h6280_0eb = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???

    public static opcode h6280_01b = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???
    public static opcode h6280_03b = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???
    public static opcode h6280_05b = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???
    public static opcode h6280_07b = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???
    public static opcode h6280_09b = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???
    public static opcode h6280_0bb = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???
    public static opcode h6280_0db = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???
    public static opcode h6280_0fb = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???

    public static opcode h6280_00c = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ABS; TSB; WB_EA;  } // 7 TSB  ABS
    public static opcode h6280_02c = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABS; BIT;		   } // 5 BIT  ABS
    public static opcode h6280_04c = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///		   h6280_ICount -= 4; EA_ABS; JMP;		   } // 4 JMP  ABS
    public static opcode h6280_06c = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; EA_IND; JMP;		   } // 7 JMP  IND
    public static opcode h6280_08c = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5;		  STY; WR_ABS; } // 5 STY  ABS
    public static opcode h6280_0ac = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABS; LDY;		   } // 5 LDY  ABS
    public static opcode h6280_0cc = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABS; CPY;		   } // 5 CPY  ABS
    public static opcode h6280_0ec = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABS; CPX;		   } // 5 CPX  ABS

    public static opcode h6280_01c = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ABS; TRB; WB_EA;  } // 7 TRB  ABS
    public static opcode h6280_03c = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABX; BIT;		   } // 5 BIT  ABX
    public static opcode h6280_05c = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???
    public static opcode h6280_07c = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; EA_IAX; JMP;		   } // 7 JMP  IAX
    public static opcode h6280_09c = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5;		  STZ; WR_ABS; } // 5 STZ  ABS
    public static opcode h6280_0bc = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABX; LDY;		   } // 5 LDY  ABX
    public static opcode h6280_0dc = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???
    public static opcode h6280_0fc = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*///									  ILL;		   } // 2 ???

    public static opcode h6280_00d = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABS; ORA;		   } // 5 ORA  ABS
    public static opcode h6280_02d = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABS; AND;		   } // 4 AND  ABS
    public static opcode h6280_04d = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABS; EOR;		   } // 4 EOR  ABS
    public static opcode h6280_06d = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABS; ADC;		   } // 4 ADC  ABS
    public static opcode h6280_08d = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5;		  STA; WR_ABS; } // 4 STA  ABS
    public static opcode h6280_0ad = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABS; LDA;		   } // 4 LDA  ABS
    public static opcode h6280_0cd = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABS; CMP;		   } // 4 CMP  ABS
    public static opcode h6280_0ed = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABS; SBC;		   } // 4 SBC  ABS

    public static opcode h6280_01d = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABX; ORA;		   } // 5 ORA  ABX
    public static opcode h6280_03d = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABX; AND;		   } // 4 AND  ABX
    public static opcode h6280_05d = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABX; EOR;		   } // 4 EOR  ABX
    public static opcode h6280_07d = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABX; ADC;		   } // 4 ADC  ABX
    public static opcode h6280_09d = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5;		  STA; WR_ABX; } // 5 STA  ABX
    public static opcode h6280_0bd = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABX; LDA;		   } // 5 LDA  ABX
    public static opcode h6280_0dd = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABX; CMP;		   } // 4 CMP  ABX
    public static opcode h6280_0fd = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABX; SBC;		   } // 4 SBC  ABX

    public static opcode h6280_00e = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ABS; ASL; WB_EA;  } // 6 ASL  ABS
    public static opcode h6280_02e = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ABS; ROL; WB_EA;  } // 6 ROL  ABS
    public static opcode h6280_04e = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ABS; LSR; WB_EA;  } // 6 LSR  ABS
    public static opcode h6280_06e = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ABS; ROR; WB_EA;  } // 6 ROR  ABS
    public static opcode h6280_08e = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5;		  STX; WR_ABS; } // 4 STX  ABS
    public static opcode h6280_0ae = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABS; LDX;		   } // 5 LDX  ABS
    public static opcode h6280_0ce = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ABS; DEC; WB_EA;  } // 6 DEC  ABS
    public static opcode h6280_0ee = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ABS; INC; WB_EA;  } // 6 INC  ABS

    public static opcode h6280_01e = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ABX; ASL; WB_EA;  } // 7 ASL  ABX
    public static opcode h6280_03e = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ABX; ROL; WB_EA;  } // 7 ROL  ABX
    public static opcode h6280_05e = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ABX; LSR; WB_EA;  } // 7 LSR  ABX
    public static opcode h6280_07e = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ABX; ROR; WB_EA;  } // 7 ROR  ABX
    public static opcode h6280_09e = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5;		  STZ; WR_ABX; } // 5 STZ  ABX
    public static opcode h6280_0be = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 5; RD_ABY; LDX;		   } // 4 LDX  ABY
    public static opcode h6280_0de = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ABX; DEC; WB_EA;  } // 7 DEC  ABX
    public static opcode h6280_0fe = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 7; RD_ABX; INC; WB_EA;  } // 7 INC  ABX

    public static opcode h6280_00f = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; BBR(0);	   } // 6/8 BBR0 ZPG,REL
    public static opcode h6280_02f = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; BBR(2);	   } // 6/8 BBR2 ZPG,REL
    public static opcode h6280_04f = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; BBR(4);	   } // 6/8 BBR4 ZPG,REL
    public static opcode h6280_06f = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; BBR(6);	   } // 6/8 BBR6 ZPG,REL
    public static opcode h6280_08f = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; BBS(0);	   } // 6/8 BBS0 ZPG,REL
    public static opcode h6280_0af = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; BBS(2);	   } // 6/8 BBS2 ZPG,REL
    public static opcode h6280_0cf = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; BBS(4);	   } // 6/8 BBS4 ZPG,REL
    public static opcode h6280_0ef = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; BBS(6);	   } // 6/8 BBS6 ZPG,REL

    public static opcode h6280_01f = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; BBR(1);	   } // 6/8 BBR1 ZPG,REL
    public static opcode h6280_03f = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; BBR(3);	   } // 6/8 BBR3 ZPG,REL
    public static opcode h6280_05f = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; BBR(5);	   } // 6/8 BBR5 ZPG,REL
    public static opcode h6280_07f = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; BBR(7);	   } // 6/8 BBR7 ZPG,REL
    public static opcode h6280_09f = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; BBS(1);	   } // 6/8 BBS1 ZPG,REL
    public static opcode h6280_0bf = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; BBS(3);	   } // 6/8 BBS3 ZPG,REL
    public static opcode h6280_0df = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; BBS(5);	   } // 6/8 BBS5 ZPG,REL
    public static opcode h6280_0ff = new opcode() { public void handler() { if (h6280log != null) {  fclose(h6280log); } throw new UnsupportedOperationException("Unimplemented"); }}; /*TODO*/// int tmp; h6280_ICount -= 4; RD_ZPG; BBS(7);	   } // 6/8 BBS7 ZPG,REL

    static opcode[] insnh6280 = {
            h6280_000,h6280_001,h6280_002,h6280_003,h6280_004,h6280_005,h6280_006,h6280_007,
            h6280_008,h6280_009,h6280_00a,h6280_00b,h6280_00c,h6280_00d,h6280_00e,h6280_00f,
            h6280_010,h6280_011,h6280_012,h6280_013,h6280_014,h6280_015,h6280_016,h6280_017,
            h6280_018,h6280_019,h6280_01a,h6280_01b,h6280_01c,h6280_01d,h6280_01e,h6280_01f,
            h6280_020,h6280_021,h6280_022,h6280_023,h6280_024,h6280_025,h6280_026,h6280_027,
            h6280_028,h6280_029,h6280_02a,h6280_02b,h6280_02c,h6280_02d,h6280_02e,h6280_02f,
            h6280_030,h6280_031,h6280_032,h6280_033,h6280_034,h6280_035,h6280_036,h6280_037,
            h6280_038,h6280_039,h6280_03a,h6280_03b,h6280_03c,h6280_03d,h6280_03e,h6280_03f,
            h6280_040,h6280_041,h6280_042,h6280_043,h6280_044,h6280_045,h6280_046,h6280_047,
            h6280_048,h6280_049,h6280_04a,h6280_04b,h6280_04c,h6280_04d,h6280_04e,h6280_04f,
            h6280_050,h6280_051,h6280_052,h6280_053,h6280_054,h6280_055,h6280_056,h6280_057,
            h6280_058,h6280_059,h6280_05a,h6280_05b,h6280_05c,h6280_05d,h6280_05e,h6280_05f,
            h6280_060,h6280_061,h6280_062,h6280_063,h6280_064,h6280_065,h6280_066,h6280_067,
            h6280_068,h6280_069,h6280_06a,h6280_06b,h6280_06c,h6280_06d,h6280_06e,h6280_06f,
            h6280_070,h6280_071,h6280_072,h6280_073,h6280_074,h6280_075,h6280_076,h6280_077,
            h6280_078,h6280_079,h6280_07a,h6280_07b,h6280_07c,h6280_07d,h6280_07e,h6280_07f,
            h6280_080,h6280_081,h6280_082,h6280_083,h6280_084,h6280_085,h6280_086,h6280_087,
            h6280_088,h6280_089,h6280_08a,h6280_08b,h6280_08c,h6280_08d,h6280_08e,h6280_08f,
            h6280_090,h6280_091,h6280_092,h6280_093,h6280_094,h6280_095,h6280_096,h6280_097,
            h6280_098,h6280_099,h6280_09a,h6280_09b,h6280_09c,h6280_09d,h6280_09e,h6280_09f,
            h6280_0a0,h6280_0a1,h6280_0a2,h6280_0a3,h6280_0a4,h6280_0a5,h6280_0a6,h6280_0a7,
            h6280_0a8,h6280_0a9,h6280_0aa,h6280_0ab,h6280_0ac,h6280_0ad,h6280_0ae,h6280_0af,
            h6280_0b0,h6280_0b1,h6280_0b2,h6280_0b3,h6280_0b4,h6280_0b5,h6280_0b6,h6280_0b7,
            h6280_0b8,h6280_0b9,h6280_0ba,h6280_0bb,h6280_0bc,h6280_0bd,h6280_0be,h6280_0bf,
            h6280_0c0,h6280_0c1,h6280_0c2,h6280_0c3,h6280_0c4,h6280_0c5,h6280_0c6,h6280_0c7,
            h6280_0c8,h6280_0c9,h6280_0ca,h6280_0cb,h6280_0cc,h6280_0cd,h6280_0ce,h6280_0cf,
            h6280_0d0,h6280_0d1,h6280_0d2,h6280_0d3,h6280_0d4,h6280_0d5,h6280_0d6,h6280_0d7,
            h6280_0d8,h6280_0d9,h6280_0da,h6280_0db,h6280_0dc,h6280_0dd,h6280_0de,h6280_0df,
            h6280_0e0,h6280_0e1,h6280_0e2,h6280_0e3,h6280_0e4,h6280_0e5,h6280_0e6,h6280_0e7,
            h6280_0e8,h6280_0e9,h6280_0ea,h6280_0eb,h6280_0ec,h6280_0ed,h6280_0ee,h6280_0ef,
            h6280_0f0,h6280_0f1,h6280_0f2,h6280_0f3,h6280_0f4,h6280_0f5,h6280_0f6,h6280_0f7,
            h6280_0f8,h6280_0f9,h6280_0fa,h6280_0fb,h6280_0fc,h6280_0fd,h6280_0fe,h6280_0ff
    };
    
}
