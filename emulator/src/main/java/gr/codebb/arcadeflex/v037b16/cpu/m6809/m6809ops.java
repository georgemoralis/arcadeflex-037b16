/**
 * ported to v0.37b16
 */
package gr.codebb.arcadeflex.v037b16.cpu.m6809;

import static gr.codebb.arcadeflex.v037b16.cpu.m6809.m6809.*;
import static arcadeflex036.osdepend.logerror;

public class m6809ops {

    public static opcode illegal = new opcode() {
        public void handler() {
            logerror("M6809: illegal opcode at %04x\n", m6809.pc);
        }
    };

    public static opcode neg_di = new opcode() {
        public void handler() {
            int/*UINT16*/ r, t;
            t = DIRBYTE();
            r = -t & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(0, t, r);
            WM(ea, r & 0xFF);
        }
    };

    public static opcode com_di = new opcode() {
        public void handler() {
            int t;
            t = DIRBYTE();
            t = ~t & 0xFF;
            CLR_NZV();
            SET_NZ8(t);
            SEC();
            WM(ea, t);
        }
    };

    public static opcode lsr_di = new opcode() {
        public void handler() {
            int t = DIRBYTE();
            CLR_NZC();
            m6809.cc |= (t & CC_C);
            t = (t >>> 1) & 0XFF;
            SET_Z8(t);
            WM(ea, t);
        }
    };

    public static opcode ror_di = new opcode() {
        public void handler() {
            int/*UINT8*/ t, r;
            t = DIRBYTE();
            r = ((m6809.cc & CC_C) << 7) & 0xFF;
            CLR_NZC();
            m6809.cc |= (t & CC_C);
            r = (r | t >>> 1) & 0xFF;
            SET_NZ8(r);
            WM(ea, r);
        }
    };

    /*TODO*////* $07 ASR direct ?**-* */
/*TODO*///public static opcode asr_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	DIRBYTE(t);
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (t & CC_C);
/*TODO*///	t = (t & 0x80) | (t >> 1);
/*TODO*///	SET_NZ8(t);
/*TODO*///	WM(EAD,t);
/*TODO*///}};
/*TODO*///
    public static opcode asl_di = new opcode() {
        public void handler() {
            int/*UINT16*/ t, r;
            t = DIRBYTE();
            r = (t << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t, t, r);
            WM(ea, r);
        }
    };

    public static opcode rol_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE();
            r = m6809.cc & CC_C;
            r = (r | t << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t, t, r);
            WM(ea, r & 0xFF);
        }
    };

    public static opcode dec_di = new opcode() {
        public void handler() {
            int t = DIRBYTE();
            t = (t - 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8D(t);
            WM(ea, t);
        }
    };

    public static opcode inc_di = new opcode() {
        public void handler() {
            int t = DIRBYTE();
            t = (t + 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8I(t);
            WM(ea, t);
        }
    };

    public static opcode tst_di = new opcode() {
        public void handler() {
            int t;
            t = DIRBYTE();
            CLR_NZVC();
            SET_NZ8(t);
        }
    };
    /*TODO*///
/*TODO*////* $0E JMP direct ----- */
/*TODO*///public static opcode jmp_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	DIRECT;
/*TODO*///	PCD = EAD;
/*TODO*///	CHANGE_PC;
/*TODO*///}};
/*TODO*///
/*TODO*////* $0F CLR direct -0100 */
/*TODO*///public static opcode clr_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	DIRECT;
/*TODO*///	WM(EAD,0);
/*TODO*///	CLR_NZVC;
/*TODO*///	SEZ;
/*TODO*///}};
/*TODO*///
/*TODO*///#ifdef macintosh
/*TODO*///#pragma mark ____1x____
/*TODO*///#endif
/*TODO*///
/*TODO*////* $10 FLAG */
/*TODO*///
/*TODO*////* $11 FLAG */
/*TODO*///
/*TODO*////* $12 NOP inherent ----- */
/*TODO*///public static opcode nop= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	;
/*TODO*///}};
/*TODO*///
/*TODO*////* $13 SYNC inherent ----- */
/*TODO*///public static opcode sync= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	/* SYNC stops processing instructions until an interrupt request happens. */
/*TODO*///	/* This doesn't require the corresponding interrupt to be enabled: if it */
/*TODO*///	/* is disabled, execution continues with the next instruction. */
/*TODO*///	m6809.int_state |= M6809_SYNC;	 /* HJB 990227 */
/*TODO*///	CHECK_IRQ_LINES;
/*TODO*///	/* if M6809_SYNC has not been cleared by CHECK_IRQ_LINES,
/*TODO*///	 * stop execution until the interrupt lines change. */
/*TODO*///	if( m6809.int_state & M6809_SYNC )
/*TODO*///		if (m6809_ICount > 0) m6809_ICount = 0;
/*TODO*///}};
/*TODO*///
/*TODO*////* $14 ILLEGAL */
/*TODO*///
/*TODO*////* $15 ILLEGAL */
/*TODO*///
/*TODO*////* $16 LBRA relative ----- */
/*TODO*///public static opcode lbra= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	IMMWORD(ea);
/*TODO*///	PC += EA;
/*TODO*///	CHANGE_PC;
/*TODO*///
/*TODO*///	if ( EA == 0xfffd )  /* EHC 980508 speed up busy loop */
/*TODO*///		if ( m6809_ICount > 0)
/*TODO*///			m6809_ICount = 0;
/*TODO*///}};
/*TODO*///
/*TODO*////* $17 LBSR relative ----- */
/*TODO*///public static opcode lbsr= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	IMMWORD(ea);
/*TODO*///	PUSHWORD(pPC);
/*TODO*///	PC += EA;
/*TODO*///	CHANGE_PC;
/*TODO*///}};
/*TODO*///
/*TODO*////* $18 ILLEGAL */
/*TODO*///
/*TODO*///#if 1
/*TODO*////* $19 DAA inherent (A) -**0* */
/*TODO*///public static opcode daa= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 msn, lsn;
/*TODO*///	UINT16 t, cf = 0;
/*TODO*///	msn = A & 0xf0; lsn = A & 0x0f;
/*TODO*///	if( lsn>0x09 || CC & CC_H) cf |= 0x06;
/*TODO*///	if( msn>0x80 && lsn>0x09 ) cf |= 0x60;
/*TODO*///	if( msn>0x90 || CC & CC_C) cf |= 0x60;
/*TODO*///	t = cf + A;
/*TODO*///	CLR_NZV; /* keep carry from previous operation */
/*TODO*///	SET_NZ8((UINT8)t); SET_C8(t);
/*TODO*///	A = t;
/*TODO*///}};
/*TODO*///#else
/*TODO*////* $19 DAA inherent (A) -**0* */
/*TODO*///public static opcode daa= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t;
/*TODO*///	t = A;
/*TODO*///	if (CC & CC_H) t+=0x06;
/*TODO*///	if ((t&0x0f)>9) t+=0x06;		/* ASG -- this code is broken! $66+$99=$FF -> DAA should = $65, we get $05! */
/*TODO*///	if (CC & CC_C) t+=0x60;
/*TODO*///	if ((t&0xf0)>0x90) t+=0x60;
/*TODO*///	if (t&0x100) SEC;
/*TODO*///	A = t;
/*TODO*///}};
/*TODO*///#endif
/*TODO*///
/*TODO*////* $1A ORCC immediate ##### */
/*TODO*///public static opcode orcc= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	CC |= t;
/*TODO*///	CHECK_IRQ_LINES;	/* HJB 990116 */
/*TODO*///}};
/*TODO*///
/*TODO*////* $1B ILLEGAL */
/*TODO*///
/*TODO*////* $1C ANDCC immediate ##### */
/*TODO*///public static opcode andcc= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	CC &= t;
/*TODO*///	CHECK_IRQ_LINES;	/* HJB 990116 */
/*TODO*///}};
/*TODO*///
/*TODO*////* $1D SEX inherent -**0- */
/*TODO*///public static opcode sex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t;
/*TODO*///	t = SIGNED(B);
/*TODO*///	D = t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(t);
/*TODO*///}};
/*TODO*///
/*TODO*////* $1E EXG inherent ----- */
/*TODO*///public static opcode exg= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t1,t2;
/*TODO*///	UINT8 tb;
/*TODO*///
/*TODO*///	IMMBYTE(tb);
/*TODO*///	if( (tb^(tb>>4)) & 0x08 )	/* HJB 990225: mixed 8/16 bit case? */
/*TODO*///	{
/*TODO*///		/* transfer $ff to both registers */
/*TODO*///		t1 = t2 = 0xff;
/*TODO*///	}};
/*TODO*///	else
/*TODO*///	{
/*TODO*///		switch(tb>>4) {
/*TODO*///			case  0: t1 = D;  break;
/*TODO*///			case  1: t1 = X;  break;
/*TODO*///			case  2: t1 = Y;  break;
/*TODO*///			case  3: t1 = U;  break;
/*TODO*///			case  4: t1 = S;  break;
/*TODO*///			case  5: t1 = PC; break;
/*TODO*///			case  8: t1 = A;  break;
/*TODO*///			case  9: t1 = B;  break;
/*TODO*///			case 10: t1 = CC; break;
/*TODO*///			case 11: t1 = DP; break;
/*TODO*///			default: t1 = 0xff;
/*TODO*///		}};
/*TODO*///		switch(tb&15) {
/*TODO*///			case  0: t2 = D;  break;
/*TODO*///			case  1: t2 = X;  break;
/*TODO*///			case  2: t2 = Y;  break;
/*TODO*///			case  3: t2 = U;  break;
/*TODO*///			case  4: t2 = S;  break;
/*TODO*///			case  5: t2 = PC; break;
/*TODO*///			case  8: t2 = A;  break;
/*TODO*///			case  9: t2 = B;  break;
/*TODO*///			case 10: t2 = CC; break;
/*TODO*///			case 11: t2 = DP; break;
/*TODO*///			default: t2 = 0xff;
/*TODO*///        }};
/*TODO*///	}};
/*TODO*///	switch(tb>>4) {
/*TODO*///		case  0: D = t2;  break;
/*TODO*///		case  1: X = t2;  break;
/*TODO*///		case  2: Y = t2;  break;
/*TODO*///		case  3: U = t2;  break;
/*TODO*///		case  4: S = t2;  break;
/*TODO*///		case  5: PC = t2; CHANGE_PC; break;
/*TODO*///		case  8: A = t2;  break;
/*TODO*///		case  9: B = t2;  break;
/*TODO*///		case 10: CC = t2; break;
/*TODO*///		case 11: DP = t2; break;
/*TODO*///	}};
/*TODO*///	switch(tb&15) {
/*TODO*///		case  0: D = t1;  break;
/*TODO*///		case  1: X = t1;  break;
/*TODO*///		case  2: Y = t1;  break;
/*TODO*///		case  3: U = t1;  break;
/*TODO*///		case  4: S = t1;  break;
/*TODO*///		case  5: PC = t1; CHANGE_PC; break;
/*TODO*///		case  8: A = t1;  break;
/*TODO*///		case  9: B = t1;  break;
/*TODO*///		case 10: CC = t1; break;
/*TODO*///		case 11: DP = t1; break;
/*TODO*///	}};
/*TODO*///}};
/*TODO*///
/*TODO*////* $1F TFR inherent ----- */
/*TODO*///public static opcode tfr= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 tb;
/*TODO*///	UINT16 t;
/*TODO*///
/*TODO*///	IMMBYTE(tb);
/*TODO*///	if( (tb^(tb>>4)) & 0x08 )	/* HJB 990225: mixed 8/16 bit case? */
/*TODO*///	{
/*TODO*///		/* transfer $ff to register */
/*TODO*///		t = 0xff;
/*TODO*///    }};
/*TODO*///	else
/*TODO*///	{
/*TODO*///		switch(tb>>4) {
/*TODO*///			case  0: t = D;  break;
/*TODO*///			case  1: t = X;  break;
/*TODO*///			case  2: t = Y;  break;
/*TODO*///			case  3: t = U;  break;
/*TODO*///			case  4: t = S;  break;
/*TODO*///			case  5: t = PC; break;
/*TODO*///			case  8: t = A;  break;
/*TODO*///			case  9: t = B;  break;
/*TODO*///			case 10: t = CC; break;
/*TODO*///			case 11: t = DP; break;
/*TODO*///			default: t = 0xff;
/*TODO*///        }};
/*TODO*///	}};
/*TODO*///	switch(tb&15) {
/*TODO*///		case  0: D = t;  break;
/*TODO*///		case  1: X = t;  break;
/*TODO*///		case  2: Y = t;  break;
/*TODO*///		case  3: U = t;  break;
/*TODO*///		case  4: S = t;  break;
/*TODO*///		case  5: PC = t; CHANGE_PC; break;
/*TODO*///		case  8: A = t;  break;
/*TODO*///		case  9: B = t;  break;
/*TODO*///		case 10: CC = t; break;
/*TODO*///		case 11: DP = t; break;
/*TODO*///    }};
/*TODO*///}};
/*TODO*///
/*TODO*///#ifdef macintosh
/*TODO*///#pragma mark ____2x____
/*TODO*///#endif
/*TODO*///
/*TODO*////* $20 BRA relative ----- */
/*TODO*///public static opcode bra= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	PC += SIGNED(t);
/*TODO*///    CHANGE_PC;
/*TODO*///	/* JB 970823 - speed up busy loops */
/*TODO*///	if( t == 0xfe )
/*TODO*///		if( m6809_ICount > 0 ) m6809_ICount = 0;
/*TODO*///}};
/*TODO*///
/*TODO*////* $21 BRN relative ----- */
/*TODO*///public static opcode brn= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///}};
/*TODO*///
/*TODO*////* $1021 LBRN relative ----- */
/*TODO*///public static opcode lbrn= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	IMMWORD(ea);
/*TODO*///}};
/*TODO*///
/*TODO*////* $22 BHI relative ----- */
/*TODO*///public static opcode bhi= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	BRANCH( !(CC & (CC_Z|CC_C)) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $1022 LBHI relative ----- */
/*TODO*///public static opcode lbhi= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	LBRANCH( !(CC & (CC_Z|CC_C)) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $23 BLS relative ----- */
/*TODO*///public static opcode bls= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	BRANCH( (CC & (CC_Z|CC_C)) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $1023 LBLS relative ----- */
/*TODO*///public static opcode lbls= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	LBRANCH( (CC&(CC_Z|CC_C)) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $24 BCC relative ----- */
/*TODO*///public static opcode bcc= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	BRANCH( !(CC&CC_C) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $1024 LBCC relative ----- */
/*TODO*///public static opcode lbcc= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	LBRANCH( !(CC&CC_C) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $25 BCS relative ----- */
/*TODO*///public static opcode bcs= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	BRANCH( (CC&CC_C) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $1025 LBCS relative ----- */
/*TODO*///public static opcode lbcs= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	LBRANCH( (CC&CC_C) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $26 BNE relative ----- */
/*TODO*///public static opcode bne= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	BRANCH( !(CC&CC_Z) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $1026 LBNE relative ----- */
/*TODO*///public static opcode lbne= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	LBRANCH( !(CC&CC_Z) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $27 BEQ relative ----- */
/*TODO*///public static opcode beq= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	BRANCH( (CC&CC_Z) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $1027 LBEQ relative ----- */
/*TODO*///public static opcode lbeq= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	LBRANCH( (CC&CC_Z) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $28 BVC relative ----- */
/*TODO*///public static opcode bvc= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	BRANCH( !(CC&CC_V) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $1028 LBVC relative ----- */
/*TODO*///public static opcode lbvc= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	LBRANCH( !(CC&CC_V) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $29 BVS relative ----- */
/*TODO*///public static opcode bvs= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	BRANCH( (CC&CC_V) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $1029 LBVS relative ----- */
/*TODO*///public static opcode lbvs= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	LBRANCH( (CC&CC_V) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $2A BPL relative ----- */
/*TODO*///public static opcode bpl= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	BRANCH( !(CC&CC_N) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $102A LBPL relative ----- */
/*TODO*///public static opcode lbpl= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	LBRANCH( !(CC&CC_N) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $2B BMI relative ----- */
/*TODO*///public static opcode bmi= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	BRANCH( (CC&CC_N) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $102B LBMI relative ----- */
/*TODO*///public static opcode lbmi= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	LBRANCH( (CC&CC_N) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $2C BGE relative ----- */
/*TODO*///public static opcode bge= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	BRANCH( !NXORV );
/*TODO*///}};
/*TODO*///
/*TODO*////* $102C LBGE relative ----- */
/*TODO*///public static opcode lbge= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	LBRANCH( !NXORV );
/*TODO*///}};
/*TODO*///
/*TODO*////* $2D BLT relative ----- */
/*TODO*///public static opcode blt= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	BRANCH( NXORV );
/*TODO*///}};
/*TODO*///
/*TODO*////* $102D LBLT relative ----- */
/*TODO*///public static opcode lblt= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	LBRANCH( NXORV );
/*TODO*///}};
/*TODO*///
/*TODO*////* $2E BGT relative ----- */
/*TODO*///public static opcode bgt= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	BRANCH( !(NXORV || (CC&CC_Z)) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $102E LBGT relative ----- */
/*TODO*///public static opcode lbgt= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	LBRANCH( !(NXORV || (CC&CC_Z)) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $2F BLE relative ----- */
/*TODO*///public static opcode ble= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	BRANCH( (NXORV || (CC&CC_Z)) );
/*TODO*///}};
/*TODO*///
/*TODO*////* $102F LBLE relative ----- */
/*TODO*///public static opcode lble= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	LBRANCH( (NXORV || (CC&CC_Z)) );
/*TODO*///}};
/*TODO*///
/*TODO*///#ifdef macintosh
/*TODO*///#pragma mark ____3x____
/*TODO*///#endif
/*TODO*///
/*TODO*////* $30 LEAX indexed --*-- */
/*TODO*///public static opcode leax= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    X = EA;
/*TODO*///	CLR_Z;
/*TODO*///	SET_Z(X);
/*TODO*///}};
/*TODO*///
/*TODO*////* $31 LEAY indexed --*-- */
/*TODO*///public static opcode leay= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    Y = EA;
/*TODO*///	CLR_Z;
/*TODO*///	SET_Z(Y);
/*TODO*///}};
/*TODO*///
/*TODO*////* $32 LEAS indexed ----- */
/*TODO*///public static opcode leas= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    S = EA;
/*TODO*///	m6809.int_state |= M6809_LDS;
/*TODO*///}};
/*TODO*///
/*TODO*////* $33 LEAU indexed ----- */
/*TODO*///public static opcode leau= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    U = EA;
/*TODO*///}};
/*TODO*///
/*TODO*////* $34 PSHS inherent ----- */
/*TODO*///public static opcode pshs= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	if( t&0x80 ) { PUSHWORD(pPC); m6809_ICount -= 2; }};
/*TODO*///	if( t&0x40 ) { PUSHWORD(pU);  m6809_ICount -= 2; }};
/*TODO*///	if( t&0x20 ) { PUSHWORD(pY);  m6809_ICount -= 2; }};
/*TODO*///	if( t&0x10 ) { PUSHWORD(pX);  m6809_ICount -= 2; }};
/*TODO*///	if( t&0x08 ) { PUSHBYTE(DP);  m6809_ICount -= 1; }};
/*TODO*///	if( t&0x04 ) { PUSHBYTE(B);   m6809_ICount -= 1; }};
/*TODO*///	if( t&0x02 ) { PUSHBYTE(A);   m6809_ICount -= 1; }};
/*TODO*///	if( t&0x01 ) { PUSHBYTE(CC);  m6809_ICount -= 1; }};
/*TODO*///}};
/*TODO*///
/*TODO*////* 35 PULS inherent ----- */
/*TODO*///public static opcode puls= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	if( t&0x01 ) { PULLBYTE(CC); m6809_ICount -= 1; }};
/*TODO*///	if( t&0x02 ) { PULLBYTE(A);  m6809_ICount -= 1; }};
/*TODO*///	if( t&0x04 ) { PULLBYTE(B);  m6809_ICount -= 1; }};
/*TODO*///	if( t&0x08 ) { PULLBYTE(DP); m6809_ICount -= 1; }};
/*TODO*///	if( t&0x10 ) { PULLWORD(XD); m6809_ICount -= 2; }};
/*TODO*///	if( t&0x20 ) { PULLWORD(YD); m6809_ICount -= 2; }};
/*TODO*///	if( t&0x40 ) { PULLWORD(UD); m6809_ICount -= 2; }};
/*TODO*///	if( t&0x80 ) { PULLWORD(PCD); CHANGE_PC; m6809_ICount -= 2; }};
/*TODO*///
/*TODO*///	/* HJB 990225: moved check after all PULLs */
/*TODO*///	if( t&0x01 ) { CHECK_IRQ_LINES; }};
/*TODO*///}};
/*TODO*///
/*TODO*////* $36 PSHU inherent ----- */
/*TODO*///public static opcode pshu= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	if( t&0x80 ) { PSHUWORD(pPC); m6809_ICount -= 2; }};
/*TODO*///	if( t&0x40 ) { PSHUWORD(pS);  m6809_ICount -= 2; }};
/*TODO*///	if( t&0x20 ) { PSHUWORD(pY);  m6809_ICount -= 2; }};
/*TODO*///	if( t&0x10 ) { PSHUWORD(pX);  m6809_ICount -= 2; }};
/*TODO*///	if( t&0x08 ) { PSHUBYTE(DP);  m6809_ICount -= 1; }};
/*TODO*///	if( t&0x04 ) { PSHUBYTE(B);   m6809_ICount -= 1; }};
/*TODO*///	if( t&0x02 ) { PSHUBYTE(A);   m6809_ICount -= 1; }};
/*TODO*///	if( t&0x01 ) { PSHUBYTE(CC);  m6809_ICount -= 1; }};
/*TODO*///}};
/*TODO*///
/*TODO*////* 37 PULU inherent ----- */
/*TODO*///public static opcode pulu= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	if( t&0x01 ) { PULUBYTE(CC); m6809_ICount -= 1; }};
/*TODO*///	if( t&0x02 ) { PULUBYTE(A);  m6809_ICount -= 1; }};
/*TODO*///	if( t&0x04 ) { PULUBYTE(B);  m6809_ICount -= 1; }};
/*TODO*///	if( t&0x08 ) { PULUBYTE(DP); m6809_ICount -= 1; }};
/*TODO*///	if( t&0x10 ) { PULUWORD(XD); m6809_ICount -= 2; }};
/*TODO*///	if( t&0x20 ) { PULUWORD(YD); m6809_ICount -= 2; }};
/*TODO*///	if( t&0x40 ) { PULUWORD(SD); m6809_ICount -= 2; }};
/*TODO*///	if( t&0x80 ) { PULUWORD(PCD); CHANGE_PC; m6809_ICount -= 2; }};
/*TODO*///
/*TODO*///	/* HJB 990225: moved check after all PULLs */
/*TODO*///	if( t&0x01 ) { CHECK_IRQ_LINES; }};
/*TODO*///}};
/*TODO*///
/*TODO*////* $38 ILLEGAL */
/*TODO*///
/*TODO*////* $39 RTS inherent ----- */
/*TODO*///public static opcode rts= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	PULLWORD(PCD);
/*TODO*///	CHANGE_PC;
/*TODO*///}};
/*TODO*///
/*TODO*////* $3A ABX inherent ----- */
/*TODO*///public static opcode abx= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	X += B;
/*TODO*///}};
/*TODO*///
/*TODO*////* $3B RTI inherent ##### */
/*TODO*///public static opcode rti= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	PULLBYTE(CC);
/*TODO*///	t = CC & CC_E;		/* HJB 990225: entire state saved? */
/*TODO*///	if(t)
/*TODO*///	{
/*TODO*///        m6809_ICount -= 9;
/*TODO*///		PULLBYTE(A);
/*TODO*///		PULLBYTE(B);
/*TODO*///		PULLBYTE(DP);
/*TODO*///		PULLWORD(XD);
/*TODO*///		PULLWORD(YD);
/*TODO*///		PULLWORD(UD);
/*TODO*///	}};
/*TODO*///	PULLWORD(PCD);
/*TODO*///	CHANGE_PC;
/*TODO*///	CHECK_IRQ_LINES;	/* HJB 990116 */
/*TODO*///}};
/*TODO*///
/*TODO*////* $3C CWAI inherent ----1 */
/*TODO*///public static opcode cwai= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	CC &= t;
/*TODO*///	/*
/*TODO*///     * CWAI stacks the entire machine state on the hardware stack,
/*TODO*///     * then waits for an interrupt; when the interrupt is taken
/*TODO*///     * later, the state is *not* saved again after CWAI.
/*TODO*///     */
/*TODO*///	CC |= CC_E; 		/* HJB 990225: save entire state */
/*TODO*///	PUSHWORD(pPC);
/*TODO*///	PUSHWORD(pU);
/*TODO*///	PUSHWORD(pY);
/*TODO*///	PUSHWORD(pX);
/*TODO*///	PUSHBYTE(DP);
/*TODO*///	PUSHBYTE(B);
/*TODO*///	PUSHBYTE(A);
/*TODO*///	PUSHBYTE(CC);
/*TODO*///	m6809.int_state |= M6809_CWAI;	 /* HJB 990228 */
/*TODO*///    CHECK_IRQ_LINES;    /* HJB 990116 */
/*TODO*///	if( m6809.int_state & M6809_CWAI )
/*TODO*///		if( m6809_ICount > 0 )
/*TODO*///			m6809_ICount = 0;
/*TODO*///}};
/*TODO*///
/*TODO*////* $3D MUL inherent --*-@ */
/*TODO*///public static opcode mul= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t;
/*TODO*///	t = A * B;
/*TODO*///	CLR_ZC; SET_Z16(t); if(t&0x80) SEC;
/*TODO*///	D = t;
/*TODO*///}};
/*TODO*///
/*TODO*////* $3E ILLEGAL */
/*TODO*///
/*TODO*////* $3F SWI (SWI2 SWI3) absolute indirect ----- */
/*TODO*///public static opcode swi= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CC |= CC_E; 			/* HJB 980225: save entire state */
/*TODO*///	PUSHWORD(pPC);
/*TODO*///	PUSHWORD(pU);
/*TODO*///	PUSHWORD(pY);
/*TODO*///	PUSHWORD(pX);
/*TODO*///	PUSHBYTE(DP);
/*TODO*///	PUSHBYTE(B);
/*TODO*///	PUSHBYTE(A);
/*TODO*///	PUSHBYTE(CC);
/*TODO*///	CC |= CC_IF | CC_II;	/* inhibit FIRQ and IRQ */
/*TODO*///	PCD=RM16(0xfffa);
/*TODO*///	CHANGE_PC;
/*TODO*///}};
/*TODO*///
/*TODO*////* $103F SWI2 absolute indirect ----- */
/*TODO*///public static opcode swi2= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CC |= CC_E; 			/* HJB 980225: save entire state */
/*TODO*///	PUSHWORD(pPC);
/*TODO*///	PUSHWORD(pU);
/*TODO*///	PUSHWORD(pY);
/*TODO*///	PUSHWORD(pX);
/*TODO*///	PUSHBYTE(DP);
/*TODO*///	PUSHBYTE(B);
/*TODO*///	PUSHBYTE(A);
/*TODO*///    PUSHBYTE(CC);
/*TODO*///	PCD = RM16(0xfff4);
/*TODO*///	CHANGE_PC;
/*TODO*///}};
/*TODO*///
/*TODO*////* $113F SWI3 absolute indirect ----- */
/*TODO*///public static opcode swi3= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CC |= CC_E; 			/* HJB 980225: save entire state */
/*TODO*///	PUSHWORD(pPC);
/*TODO*///	PUSHWORD(pU);
/*TODO*///	PUSHWORD(pY);
/*TODO*///	PUSHWORD(pX);
/*TODO*///	PUSHBYTE(DP);
/*TODO*///	PUSHBYTE(B);
/*TODO*///	PUSHBYTE(A);
/*TODO*///    PUSHBYTE(CC);
/*TODO*///	PCD = RM16(0xfff2);
/*TODO*///	CHANGE_PC;
/*TODO*///}};
/*TODO*///
/*TODO*///#ifdef macintosh
/*TODO*///#pragma mark ____4x____
/*TODO*///#endif
/*TODO*///
/*TODO*////* $40 NEGA inherent ?**** */
/*TODO*///public static opcode nega= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 r;
/*TODO*///	r = -A;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(0,A,r);
/*TODO*///	A = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $41 ILLEGAL */
/*TODO*///
/*TODO*////* $42 ILLEGAL */
/*TODO*///
/*TODO*////* $43 COMA inherent -**01 */
/*TODO*///public static opcode coma= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	A = ~A;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///	SEC;
/*TODO*///}};
/*TODO*///
/*TODO*////* $44 LSRA inherent -0*-* */
/*TODO*///public static opcode lsra= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (A & CC_C);
/*TODO*///	A >>= 1;
/*TODO*///	SET_Z8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $45 ILLEGAL */
/*TODO*///
/*TODO*////* $46 RORA inherent -**-* */
/*TODO*///public static opcode rora= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 r;
/*TODO*///	r = (CC & CC_C) << 7;
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (A & CC_C);
/*TODO*///	r |= A >> 1;
/*TODO*///	SET_NZ8(r);
/*TODO*///	A = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $47 ASRA inherent ?**-* */
/*TODO*///public static opcode asra= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (A & CC_C);
/*TODO*///	A = (A & 0x80) | (A >> 1);
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $48 ASLA inherent ?**** */
/*TODO*///public static opcode asla= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 r;
/*TODO*///	r = A << 1;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(A,A,r);
/*TODO*///	A = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $49 ROLA inherent -**** */
/*TODO*///public static opcode rola= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	t = A;
/*TODO*///	r = (CC & CC_C) | (t<<1);
/*TODO*///	CLR_NZVC; SET_FLAGS8(t,t,r);
/*TODO*///	A = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $4A DECA inherent -***- */
/*TODO*///public static opcode deca= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	--A;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_FLAGS8D(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $4B ILLEGAL */
/*TODO*///
/*TODO*////* $4C INCA inherent -***- */
/*TODO*///public static opcode inca= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	++A;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_FLAGS8I(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $4D TSTA inherent -**0- */
/*TODO*///public static opcode tsta= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $4E ILLEGAL */
/*TODO*///
/*TODO*////* $4F CLRA inherent -0100 */
/*TODO*///public static opcode clra= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	A = 0;
/*TODO*///	CLR_NZVC; SEZ;
/*TODO*///}};
/*TODO*///
/*TODO*///#ifdef macintosh
/*TODO*///#pragma mark ____5x____
/*TODO*///#endif
/*TODO*///
/*TODO*////* $50 NEGB inherent ?**** */
/*TODO*///public static opcode negb= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 r;
/*TODO*///	r = -B;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(0,B,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $51 ILLEGAL */
/*TODO*///
/*TODO*////* $52 ILLEGAL */
/*TODO*///
/*TODO*////* $53 COMB inherent -**01 */
/*TODO*///public static opcode comb= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	B = ~B;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///	SEC;
/*TODO*///}};
/*TODO*///
/*TODO*////* $54 LSRB inherent -0*-* */
/*TODO*///public static opcode lsrb= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (B & CC_C);
/*TODO*///	B >>= 1;
/*TODO*///	SET_Z8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $55 ILLEGAL */
/*TODO*///
/*TODO*////* $56 RORB inherent -**-* */
/*TODO*///public static opcode rorb= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 r;
/*TODO*///	r = (CC & CC_C) << 7;
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (B & CC_C);
/*TODO*///	r |= B >> 1;
/*TODO*///	SET_NZ8(r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $57 ASRB inherent ?**-* */
/*TODO*///public static opcode asrb= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (B & CC_C);
/*TODO*///	B= (B & 0x80) | (B >> 1);
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $58 ASLB inherent ?**** */
/*TODO*///public static opcode aslb= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 r;
/*TODO*///	r = B << 1;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(B,B,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $59 ROLB inherent -**** */
/*TODO*///public static opcode rolb= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	t = B;
/*TODO*///	r = CC & CC_C;
/*TODO*///	r |= t << 1;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(t,t,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $5A DECB inherent -***- */
/*TODO*///public static opcode decb= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	--B;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_FLAGS8D(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $5B ILLEGAL */
/*TODO*///
/*TODO*////* $5C INCB inherent -***- */
/*TODO*///public static opcode incb= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	++B;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_FLAGS8I(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $5D TSTB inherent -**0- */
/*TODO*///public static opcode tstb= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $5E ILLEGAL */
/*TODO*///
/*TODO*////* $5F CLRB inherent -0100 */
/*TODO*///public static opcode clrb= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	B = 0;
/*TODO*///	CLR_NZVC; SEZ;
/*TODO*///}};
/*TODO*///
/*TODO*///#ifdef macintosh
/*TODO*///#pragma mark ____6x____
/*TODO*///#endif
/*TODO*///
    public static opcode neg_ix = new opcode() {
        public void handler() {
            int/*UINT16*/ r, t;
            fetch_effective_address();
            t = RM(ea);
            r = -t & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(0, t, r);
            WM(ea, r & 0xFF);
        }
    };

    public static opcode com_ix = new opcode() {
        public void handler() {
            int t;
            fetch_effective_address();
            t = ~RM(ea) & 0xFF;
            CLR_NZV();
            SET_NZ8(t);
            SEC();
            WM(ea, t);
        }
    };

    public static opcode lsr_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            int t = RM(ea);
            CLR_NZC();
            m6809.cc |= (t & CC_C);
            t = (t >>> 1) & 0XFF;
            SET_Z8(t);
            WM(ea, t);
        }
    };

    public static opcode ror_ix = new opcode() {
        public void handler() {
            int/*UINT8*/ t, r;
            fetch_effective_address();
            t = RM(ea);
            r = ((m6809.cc & CC_C) << 7) & 0xFF;
            CLR_NZC();
            m6809.cc |= (t & CC_C);
            r = (r | t >>> 1) & 0xFF;
            SET_NZ8(r);
            WM(ea, r);
        }
    };

    /*TODO*////* $67 ASR indexed ?**-* */
/*TODO*///public static opcode asr_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	fetch_effective_address();
/*TODO*///	t=RM(EAD);
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (t & CC_C);
/*TODO*///	t=(t&0x80)|(t>>1);
/*TODO*///	SET_NZ8(t);
/*TODO*///	WM(EAD,t);
/*TODO*///}};
/*TODO*///
    public static opcode asl_ix = new opcode() {
        public void handler() {
            int/*UINT16*/ t, r;
            fetch_effective_address();
            t = RM(ea);
            r = (t << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t, t, r);
            WM(ea, r);
        }
    };

    public static opcode rol_ix = new opcode() {
        public void handler() {
            int t, r;
            fetch_effective_address();
            t = RM(ea);
            r = m6809.cc & CC_C;
            r = (r | t << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t, t, r);
            WM(ea, r & 0xFF);
        }
    };

    public static opcode dec_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            int t = (RM(ea) - 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8D(t);
            WM(ea, t);

        }
    };

    public static opcode inc_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            int t = (RM(ea) + 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8I(t);
            WM(ea, t);
        }
    };

    public static opcode tst_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            int t = RM(ea);
            CLR_NZVC();
            SET_NZ8(t);
        }
    };

    /*TODO*////* $6E JMP indexed ----- */
/*TODO*///public static opcode jmp_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///	PCD = EAD;
/*TODO*///	CHANGE_PC;
/*TODO*///}};
/*TODO*///
/*TODO*////* $6F CLR indexed -0100 */
/*TODO*///public static opcode clr_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    WM(EAD,0);
/*TODO*///	CLR_NZVC; SEZ;
/*TODO*///}};
/*TODO*///
/*TODO*///#ifdef macintosh
/*TODO*///#pragma mark ____7x____
/*TODO*///#endif
/*TODO*///
    public static opcode neg_ex = new opcode() {
        public void handler() {
            int/*UINT16*/ r, t;
            t = EXTBYTE();
            r = -t & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(0, t, r);
            WM(ea, r & 0xFF);
        }
    };

    public static opcode com_ex = new opcode() {
        public void handler() {
            int t;
            t = EXTBYTE();
            t = ~t & 0xFF;
            CLR_NZV();
            SET_NZ8(t);
            SEC();
            WM(ea, t);
        }
    };

    public static opcode lsr_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE();
            CLR_NZC();
            m6809.cc |= (t & CC_C);
            t = (t >>> 1) & 0XFF;
            SET_Z8(t);
            WM(ea, t);
        }
    };

    public static opcode ror_ex = new opcode() {
        public void handler() {
            int/*UINT8*/ t, r;
            t = EXTBYTE();
            r = ((m6809.cc & CC_C) << 7) & 0xFF;
            CLR_NZC();
            m6809.cc |= (t & CC_C);
            r = (r | t >>> 1) & 0xFF;
            SET_NZ8(r);
            WM(ea, r);
        }
    };
    /*TODO*////* $77 ASR extended ?**-* */
/*TODO*///public static opcode asr_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	EXTBYTE(t); CLR_NZC; CC |= (t & CC_C);
/*TODO*///	t=(t&0x80)|(t>>1);
/*TODO*///	SET_NZ8(t);
/*TODO*///	WM(EAD,t);
/*TODO*///}};
/*TODO*///

    public static opcode asl_ex = new opcode() {
        public void handler() {
            int/*UINT16*/ t, r;
            t = EXTBYTE();
            r = (t << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t, t, r);
            WM(ea, r);
        }
    };

    public static opcode rol_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE();
            r = m6809.cc & CC_C;
            r = (r | t << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t, t, r);
            WM(ea, r & 0xFF);
        }
    };

    public static opcode dec_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE();
            t = (t - 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8D(t);
            WM(ea, t);
        }
    };

    public static opcode inc_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE();
            t = (t + 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8I(t);
            WM(ea, t);
        }
    };

    public static opcode tst_ex = new opcode() {
        public void handler() {
            int t;
            t = EXTBYTE();
            CLR_NZVC();
            SET_NZ8(t);
        }
    };

    /*TODO*////* $7E JMP extended ----- */
/*TODO*///public static opcode jmp_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	EXTENDED;
/*TODO*///	PCD = EAD;
/*TODO*///	CHANGE_PC;
/*TODO*///}};
/*TODO*///
/*TODO*////* $7F CLR extended -0100 */
/*TODO*///public static opcode clr_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	EXTENDED;
/*TODO*///	WM(EAD,0);
/*TODO*///	CLR_NZVC; SEZ;
/*TODO*///}};
    public static opcode suba_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE();
            r = (m6809.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.a, t, r);
            m6809.a = r & 0xFF;
        }
    };

    /*TODO*////* $81 CMPA immediate ?**** */
/*TODO*///public static opcode cmpa_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	IMMBYTE(t);
/*TODO*///	r = A - t;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $82 SBCA immediate ?**** */
/*TODO*///public static opcode sbca_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	IMMBYTE(t);
/*TODO*///	r = A - t - (CC & CC_C);
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///	A = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $83 SUBD (CMPD CMPU) immediate -**** */
/*TODO*///public static opcode subd_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	IMMWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///	D = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $1083 CMPD immediate -**** */
/*TODO*///public static opcode cmpd_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	IMMWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $1183 CMPU immediate -**** */
/*TODO*///public static opcode cmpu_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r, d;
/*TODO*///	PAIR b;
/*TODO*///	IMMWORD(b);
/*TODO*///	d = U;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $84 ANDA immediate -**0- */
/*TODO*///public static opcode anda_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	A &= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $85 BITA immediate -**0- */
/*TODO*///public static opcode bita_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t,r;
/*TODO*///	IMMBYTE(t);
/*TODO*///	r = A & t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $86 LDA immediate -**0- */
/*TODO*///public static opcode lda_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	IMMBYTE(A);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* is this a legal instruction? */
/*TODO*////* $87 STA immediate -**0- */
/*TODO*///public static opcode sta_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///	IMM8;
/*TODO*///	WM(EAD,A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $88 EORA immediate -**0- */
/*TODO*///public static opcode eora_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	A ^= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $89 ADCA immediate ***** */
/*TODO*///public static opcode adca_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	IMMBYTE(t);
/*TODO*///	r = A + t + (CC & CC_C);
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///	SET_H(A,t,r);
/*TODO*///	A = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $8A ORA immediate -**0- */
/*TODO*///public static opcode ora_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	A |= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $8B ADDA immediate ***** */
/*TODO*///public static opcode adda_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	IMMBYTE(t);
/*TODO*///	r = A + t;
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///	SET_H(A,t,r);
/*TODO*///	A = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $8C CMPX (CMPY CMPS) immediate -**** */
/*TODO*///public static opcode cmpx_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	IMMWORD(b);
/*TODO*///	d = X;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $108C CMPY immediate -**** */
/*TODO*///public static opcode cmpy_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	IMMWORD(b);
/*TODO*///	d = Y;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $118C CMPS immediate -**** */
/*TODO*///public static opcode cmps_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	IMMWORD(b);
/*TODO*///	d = S;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $8D BSR ----- */
/*TODO*///public static opcode bsr= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	PUSHWORD(pPC);
/*TODO*///	PC += SIGNED(t);
/*TODO*///	CHANGE_PC;
/*TODO*///}};
/*TODO*///
/*TODO*////* $8E LDX (LDY) immediate -**0- */
/*TODO*///public static opcode ldx_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	IMMWORD(pX);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(X);
/*TODO*///}};
/*TODO*///
/*TODO*////* $108E LDY immediate -**0- */
/*TODO*///public static opcode ldy_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	IMMWORD(pY);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(Y);
/*TODO*///}};
/*TODO*///
/*TODO*////* is this a legal instruction? */
/*TODO*////* $8F STX (STY) immediate -**0- */
/*TODO*///public static opcode stx_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(X);
/*TODO*///	IMM16;
/*TODO*///	WM16(EAD,&pX);
/*TODO*///}};
/*TODO*///
/*TODO*////* is this a legal instruction? */
/*TODO*////* $108F STY immediate -**0- */
/*TODO*///public static opcode sty_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(Y);
/*TODO*///	IMM16;
/*TODO*///	WM16(EAD,&pY);
/*TODO*///}};
    public static opcode suba_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE();
            r = (m6809.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.a, t, r);
            m6809.a = r & 0xFF;
        }
    };

    /*TODO*////* $91 CMPA direct ?**** */
/*TODO*///public static opcode cmpa_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	DIRBYTE(t);
/*TODO*///	r = A - t;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $92 SBCA direct ?**** */
/*TODO*///public static opcode sbca_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	DIRBYTE(t);
/*TODO*///	r = A - t - (CC & CC_C);
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///	A = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $93 SUBD (CMPD CMPU) direct -**** */
/*TODO*///public static opcode subd_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	DIRWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///	D = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $1093 CMPD direct -**** */
/*TODO*///public static opcode cmpd_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	DIRWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $1193 CMPU direct -**** */
/*TODO*///public static opcode cmpu_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	DIRWORD(b);
/*TODO*///	d = U;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(U,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $94 ANDA direct -**0- */
/*TODO*///public static opcode anda_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	DIRBYTE(t);
/*TODO*///	A &= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $95 BITA direct -**0- */
/*TODO*///public static opcode bita_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t,r;
/*TODO*///	DIRBYTE(t);
/*TODO*///	r = A & t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $96 LDA direct -**0- */
/*TODO*///public static opcode lda_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	DIRBYTE(A);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $97 STA direct -**0- */
/*TODO*///public static opcode sta_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///	DIRECT;
/*TODO*///	WM(EAD,A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $98 EORA direct -**0- */
/*TODO*///public static opcode eora_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	DIRBYTE(t);
/*TODO*///	A ^= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $99 ADCA direct ***** */
/*TODO*///public static opcode adca_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	DIRBYTE(t);
/*TODO*///	r = A + t + (CC & CC_C);
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///	SET_H(A,t,r);
/*TODO*///	A = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $9A ORA direct -**0- */
/*TODO*///public static opcode ora_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	DIRBYTE(t);
/*TODO*///	A |= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $9B ADDA direct ***** */
/*TODO*///public static opcode adda_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	DIRBYTE(t);
/*TODO*///	r = A + t;
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///	SET_H(A,t,r);
/*TODO*///	A = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $9C CMPX (CMPY CMPS) direct -**** */
/*TODO*///public static opcode cmpx_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	DIRWORD(b);
/*TODO*///	d = X;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $109C CMPY direct -**** */
/*TODO*///public static opcode cmpy_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	DIRWORD(b);
/*TODO*///	d = Y;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $119C CMPS direct -**** */
/*TODO*///public static opcode cmps_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	DIRWORD(b);
/*TODO*///	d = S;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $9D JSR direct ----- */
/*TODO*///public static opcode jsr_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	DIRECT;
/*TODO*///	PUSHWORD(pPC);
/*TODO*///	PCD = EAD;
/*TODO*///	CHANGE_PC;
/*TODO*///}};
/*TODO*///
/*TODO*////* $9E LDX (LDY) direct -**0- */
/*TODO*///public static opcode ldx_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	DIRWORD(pX);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(X);
/*TODO*///}};
/*TODO*///
/*TODO*////* $109E LDY direct -**0- */
/*TODO*///public static opcode ldy_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	DIRWORD(pY);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(Y);
/*TODO*///}};
/*TODO*///
/*TODO*////* $9F STX (STY) direct -**0- */
/*TODO*///public static opcode stx_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(X);
/*TODO*///	DIRECT;
/*TODO*///	WM16(EAD,&pX);
/*TODO*///}};
/*TODO*///
/*TODO*////* $109F STY direct -**0- */
/*TODO*///public static opcode sty_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(Y);
/*TODO*///	DIRECT;
/*TODO*///	WM16(EAD,&pY);
/*TODO*///}};
/*TODO*///
    public static opcode suba_ix = new opcode() {
        public void handler() {
            int t, r;
            fetch_effective_address();
            t = RM(ea);
            r = (m6809.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.a, t, r);
            m6809.a = r & 0xFF;
        }
    };

    /*TODO*////* $a1 CMPA indexed ?**** */
/*TODO*///public static opcode cmpa_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	fetch_effective_address();
/*TODO*///	t = RM(EAD);
/*TODO*///	r = A - t;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $a2 SBCA indexed ?**** */
/*TODO*///public static opcode sbca_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	fetch_effective_address();
/*TODO*///	t = RM(EAD);
/*TODO*///	r = A - t - (CC & CC_C);
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///	A = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $a3 SUBD (CMPD CMPU) indexed -**** */
/*TODO*///public static opcode subd_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	fetch_effective_address();
/*TODO*///    b.d=RM16(EAD);
/*TODO*///	d = D;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///	D = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $10a3 CMPD indexed -**** */
/*TODO*///public static opcode cmpd_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	fetch_effective_address();
/*TODO*///    b.d=RM16(EAD);
/*TODO*///	d = D;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $11a3 CMPU indexed -**** */
/*TODO*///public static opcode cmpu_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r;
/*TODO*///	PAIR b;
/*TODO*///	fetch_effective_address();
/*TODO*///    b.d=RM16(EAD);
/*TODO*///	r = U - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(U,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $a4 ANDA indexed -**0- */
/*TODO*///public static opcode anda_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///	A &= RM(EAD);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $a5 BITA indexed -**0- */
/*TODO*///public static opcode bita_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 r;
/*TODO*///	fetch_effective_address();
/*TODO*///	r = A & RM(EAD);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $a6 LDA indexed -**0- */
/*TODO*///public static opcode lda_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///	A = RM(EAD);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $a7 STA indexed -**0- */
/*TODO*///public static opcode sta_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///	WM(EAD,A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $a8 EORA indexed -**0- */
/*TODO*///public static opcode eora_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///	A ^= RM(EAD);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $a9 ADCA indexed ***** */
/*TODO*///public static opcode adca_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	fetch_effective_address();
/*TODO*///	t = RM(EAD);
/*TODO*///	r = A + t + (CC & CC_C);
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///	SET_H(A,t,r);
/*TODO*///	A = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $aA ORA indexed -**0- */
/*TODO*///public static opcode ora_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///	A |= RM(EAD);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $aB ADDA indexed ***** */
/*TODO*///public static opcode adda_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	fetch_effective_address();
/*TODO*///	t = RM(EAD);
/*TODO*///	r = A + t;
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///	SET_H(A,t,r);
/*TODO*///	A = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $aC CMPX (CMPY CMPS) indexed -**** */
/*TODO*///public static opcode cmpx_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	fetch_effective_address();
/*TODO*///    b.d=RM16(EAD);
/*TODO*///	d = X;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $10aC CMPY indexed -**** */
/*TODO*///public static opcode cmpy_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	fetch_effective_address();
/*TODO*///    b.d=RM16(EAD);
/*TODO*///	d = Y;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $11aC CMPS indexed -**** */
/*TODO*///public static opcode cmps_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	fetch_effective_address();
/*TODO*///    b.d=RM16(EAD);
/*TODO*///	d = S;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $aD JSR indexed ----- */
/*TODO*///public static opcode jsr_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    PUSHWORD(pPC);
/*TODO*///	PCD = EAD;
/*TODO*///	CHANGE_PC;
/*TODO*///}};
/*TODO*///
/*TODO*////* $aE LDX (LDY) indexed -**0- */
/*TODO*///public static opcode ldx_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    X=RM16(EAD);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(X);
/*TODO*///}};
/*TODO*///
/*TODO*////* $10aE LDY indexed -**0- */
/*TODO*///public static opcode ldy_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    Y=RM16(EAD);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(Y);
/*TODO*///}};
/*TODO*///
/*TODO*////* $aF STX (STY) indexed -**0- */
/*TODO*///public static opcode stx_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    CLR_NZV;
/*TODO*///	SET_NZ16(X);
/*TODO*///	WM16(EAD,&pX);
/*TODO*///}};
/*TODO*///
/*TODO*////* $10aF STY indexed -**0- */
/*TODO*///public static opcode sty_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    CLR_NZV;
/*TODO*///	SET_NZ16(Y);
/*TODO*///	WM16(EAD,&pY);
/*TODO*///}};
/*TODO*///
/*TODO*///#ifdef macintosh
/*TODO*///#pragma mark ____Bx____
/*TODO*///#endif
/*TODO*///
/*TODO*////* $b0 SUBA extended ?**** */
/*TODO*///public static opcode suba_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	EXTBYTE(t);
/*TODO*///	r = A - t;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///	A = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $b1 CMPA extended ?**** */
/*TODO*///public static opcode cmpa_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	EXTBYTE(t);
/*TODO*///	r = A - t;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $b2 SBCA extended ?**** */
/*TODO*///public static opcode sbca_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	EXTBYTE(t);
/*TODO*///	r = A - t - (CC & CC_C);
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///	A = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $b3 SUBD (CMPD CMPU) extended -**** */
/*TODO*///public static opcode subd_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	EXTWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///	D = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $10b3 CMPD extended -**** */
/*TODO*///public static opcode cmpd_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	EXTWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $11b3 CMPU extended -**** */
/*TODO*///public static opcode cmpu_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	EXTWORD(b);
/*TODO*///	d = U;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $b4 ANDA extended -**0- */
/*TODO*///public static opcode anda_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	EXTBYTE(t);
/*TODO*///	A &= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $b5 BITA extended -**0- */
/*TODO*///public static opcode bita_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t,r;
/*TODO*///	EXTBYTE(t);
/*TODO*///	r = A & t;
/*TODO*///	CLR_NZV; SET_NZ8(r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $b6 LDA extended -**0- */
/*TODO*///public static opcode lda_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	EXTBYTE(A);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $b7 STA extended -**0- */
/*TODO*///public static opcode sta_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///	EXTENDED;
/*TODO*///	WM(EAD,A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $b8 EORA extended -**0- */
/*TODO*///public static opcode eora_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	EXTBYTE(t);
/*TODO*///	A ^= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $b9 ADCA extended ***** */
/*TODO*///public static opcode adca_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	EXTBYTE(t);
/*TODO*///	r = A + t + (CC & CC_C);
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///	SET_H(A,t,r);
/*TODO*///	A = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $bA ORA extended -**0- */
/*TODO*///public static opcode ora_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	EXTBYTE(t);
/*TODO*///	A |= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///}};
/*TODO*///
/*TODO*////* $bB ADDA extended ***** */
/*TODO*///public static opcode adda_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	EXTBYTE(t);
/*TODO*///	r = A + t;
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///	SET_H(A,t,r);
/*TODO*///	A = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $bC CMPX (CMPY CMPS) extended -**** */
/*TODO*///public static opcode cmpx_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	EXTWORD(b);
/*TODO*///	d = X;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $10bC CMPY extended -**** */
/*TODO*///public static opcode cmpy_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	EXTWORD(b);
/*TODO*///	d = Y;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $11bC CMPS extended -**** */
/*TODO*///public static opcode cmps_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	EXTWORD(b);
/*TODO*///	d = S;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $bD JSR extended ----- */
/*TODO*///public static opcode jsr_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	EXTENDED;
/*TODO*///	PUSHWORD(pPC);
/*TODO*///	PCD = EAD;
/*TODO*///	CHANGE_PC;
/*TODO*///}};
/*TODO*///
/*TODO*////* $bE LDX (LDY) extended -**0- */
/*TODO*///public static opcode ldx_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	EXTWORD(pX);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(X);
/*TODO*///}};
/*TODO*///
/*TODO*////* $10bE LDY extended -**0- */
/*TODO*///public static opcode ldy_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	EXTWORD(pY);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(Y);
/*TODO*///}};
/*TODO*///
/*TODO*////* $bF STX (STY) extended -**0- */
/*TODO*///public static opcode stx_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(X);
/*TODO*///	EXTENDED;
/*TODO*///	WM16(EAD,&pX);
/*TODO*///}};
/*TODO*///
/*TODO*////* $10bF STY extended -**0- */
/*TODO*///public static opcode sty_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(Y);
/*TODO*///	EXTENDED;
/*TODO*///	WM16(EAD,&pY);
/*TODO*///}};
/*TODO*///
/*TODO*///
/*TODO*///#ifdef macintosh
/*TODO*///#pragma mark ____Cx____
/*TODO*///#endif
/*TODO*///
/*TODO*////* $c0 SUBB immediate ?**** */
/*TODO*///public static opcode subb_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	IMMBYTE(t);
/*TODO*///	r = B - t;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $c1 CMPB immediate ?**** */
/*TODO*///public static opcode cmpb_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	IMMBYTE(t);
/*TODO*///	r = B - t;
/*TODO*///	CLR_NZVC; SET_FLAGS8(B,t,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $c2 SBCB immediate ?**** */
/*TODO*///public static opcode sbcb_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	IMMBYTE(t);
/*TODO*///	r = B - t - (CC & CC_C);
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $c3 ADDD immediate -**** */
/*TODO*///public static opcode addd_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	IMMWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d + b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///	D = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $c4 ANDB immediate -**0- */
/*TODO*///public static opcode andb_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	B &= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $c5 BITB immediate -**0- */
/*TODO*///public static opcode bitb_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t,r;
/*TODO*///	IMMBYTE(t);
/*TODO*///	r = B & t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $c6 LDB immediate -**0- */
/*TODO*///public static opcode ldb_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	IMMBYTE(B);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* is this a legal instruction? */
/*TODO*////* $c7 STB immediate -**0- */
/*TODO*///public static opcode stb_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///	IMM8;
/*TODO*///	WM(EAD,B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $c8 EORB immediate -**0- */
/*TODO*///public static opcode eorb_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	B ^= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $c9 ADCB immediate ***** */
/*TODO*///public static opcode adcb_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	IMMBYTE(t);
/*TODO*///	r = B + t + (CC & CC_C);
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	SET_H(B,t,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $cA ORB immediate -**0- */
/*TODO*///public static opcode orb_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	B |= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $cB ADDB immediate ***** */
/*TODO*///public static opcode addb_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	IMMBYTE(t);
/*TODO*///	r = B + t;
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	SET_H(B,t,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $cC LDD immediate -**0- */
/*TODO*///public static opcode ldd_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	IMMWORD(pD);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(D);
/*TODO*///}};
/*TODO*///
/*TODO*////* is this a legal instruction? */
/*TODO*////* $cD STD immediate -**0- */
/*TODO*///public static opcode std_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(D);
/*TODO*///    IMM16;
/*TODO*///	WM16(EAD,&pD);
/*TODO*///}};
/*TODO*///
/*TODO*////* $cE LDU (LDS) immediate -**0- */
/*TODO*///public static opcode ldu_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	IMMWORD(pU);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(U);
/*TODO*///}};
/*TODO*///
/*TODO*////* $10cE LDS immediate -**0- */
/*TODO*///public static opcode lds_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	IMMWORD(pS);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(S);
/*TODO*///	m6809.int_state |= M6809_LDS;
/*TODO*///}};
/*TODO*///
/*TODO*////* is this a legal instruction? */
/*TODO*////* $cF STU (STS) immediate -**0- */
/*TODO*///public static opcode stu_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(U);
/*TODO*///    IMM16;
/*TODO*///	WM16(EAD,&pU);
/*TODO*///}};
/*TODO*///
/*TODO*////* is this a legal instruction? */
/*TODO*////* $10cF STS immediate -**0- */
/*TODO*///public static opcode sts_im= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(S);
/*TODO*///    IMM16;
/*TODO*///	WM16(EAD,&pS);
/*TODO*///}};
/*TODO*///
/*TODO*///
/*TODO*///#ifdef macintosh
/*TODO*///#pragma mark ____Dx____
/*TODO*///#endif
/*TODO*///
/*TODO*////* $d0 SUBB direct ?**** */
/*TODO*///public static opcode subb_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	DIRBYTE(t);
/*TODO*///	r = B - t;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $d1 CMPB direct ?**** */
/*TODO*///public static opcode cmpb_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	DIRBYTE(t);
/*TODO*///	r = B - t;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $d2 SBCB direct ?**** */
/*TODO*///public static opcode sbcb_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	DIRBYTE(t);
/*TODO*///	r = B - t - (CC & CC_C);
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $d3 ADDD direct -**** */
/*TODO*///public static opcode addd_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	DIRWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d + b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///	D = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $d4 ANDB direct -**0- */
/*TODO*///public static opcode andb_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	DIRBYTE(t);
/*TODO*///	B &= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $d5 BITB direct -**0- */
/*TODO*///public static opcode bitb_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t,r;
/*TODO*///	DIRBYTE(t);
/*TODO*///	r = B & t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $d6 LDB direct -**0- */
/*TODO*///public static opcode ldb_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	DIRBYTE(B);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $d7 STB direct -**0- */
/*TODO*///public static opcode stb_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///	DIRECT;
/*TODO*///	WM(EAD,B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $d8 EORB direct -**0- */
/*TODO*///public static opcode eorb_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	DIRBYTE(t);
/*TODO*///	B ^= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $d9 ADCB direct ***** */
/*TODO*///public static opcode adcb_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	DIRBYTE(t);
/*TODO*///	r = B + t + (CC & CC_C);
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	SET_H(B,t,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $dA ORB direct -**0- */
/*TODO*///public static opcode orb_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	DIRBYTE(t);
/*TODO*///	B |= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $dB ADDB direct ***** */
/*TODO*///public static opcode addb_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	DIRBYTE(t);
/*TODO*///	r = B + t;
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	SET_H(B,t,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $dC LDD direct -**0- */
/*TODO*///public static opcode ldd_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	DIRWORD(pD);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(D);
/*TODO*///}};
/*TODO*///
/*TODO*////* $dD STD direct -**0- */
/*TODO*///public static opcode std_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(D);
/*TODO*///    DIRECT;
/*TODO*///	WM16(EAD,&pD);
/*TODO*///}};
/*TODO*///
/*TODO*////* $dE LDU (LDS) direct -**0- */
/*TODO*///public static opcode ldu_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	DIRWORD(pU);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(U);
/*TODO*///}};
/*TODO*///
/*TODO*////* $10dE LDS direct -**0- */
/*TODO*///public static opcode lds_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	DIRWORD(pS);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(S);
/*TODO*///	m6809.int_state |= M6809_LDS;
/*TODO*///}};
/*TODO*///
/*TODO*////* $dF STU (STS) direct -**0- */
/*TODO*///public static opcode stu_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(U);
/*TODO*///	DIRECT;
/*TODO*///	WM16(EAD,&pU);
/*TODO*///}};
/*TODO*///
/*TODO*////* $10dF STS direct -**0- */
/*TODO*///public static opcode sts_di= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(S);
/*TODO*///	DIRECT;
/*TODO*///	WM16(EAD,&pS);
/*TODO*///}};
/*TODO*///
/*TODO*///#ifdef macintosh
/*TODO*///#pragma mark ____Ex____
/*TODO*///#endif
/*TODO*///
/*TODO*///
/*TODO*////* $e0 SUBB indexed ?**** */
/*TODO*///public static opcode subb_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	fetch_effective_address();
/*TODO*///	t = RM(EAD);
/*TODO*///	r = B - t;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $e1 CMPB indexed ?**** */
/*TODO*///public static opcode cmpb_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	fetch_effective_address();
/*TODO*///	t = RM(EAD);
/*TODO*///	r = B - t;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $e2 SBCB indexed ?**** */
/*TODO*///public static opcode sbcb_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	fetch_effective_address();
/*TODO*///	t = RM(EAD);
/*TODO*///	r = B - t - (CC & CC_C);
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $e3 ADDD indexed -**** */
/*TODO*///public static opcode addd_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///    PAIR b;
/*TODO*///    fetch_effective_address();
/*TODO*///	b.d=RM16(EAD);
/*TODO*///	d = D;
/*TODO*///	r = d + b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///	D = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $e4 ANDB indexed -**0- */
/*TODO*///public static opcode andb_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///	B &= RM(EAD);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $e5 BITB indexed -**0- */
/*TODO*///public static opcode bitb_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 r;
/*TODO*///	fetch_effective_address();
/*TODO*///	r = B & RM(EAD);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $e6 LDB indexed -**0- */
/*TODO*///public static opcode ldb_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///	B = RM(EAD);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $e7 STB indexed -**0- */
/*TODO*///public static opcode stb_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///	WM(EAD,B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $e8 EORB indexed -**0- */
/*TODO*///public static opcode eorb_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///	B ^= RM(EAD);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $e9 ADCB indexed ***** */
/*TODO*///public static opcode adcb_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	fetch_effective_address();
/*TODO*///	t = RM(EAD);
/*TODO*///	r = B + t + (CC & CC_C);
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	SET_H(B,t,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $eA ORB indexed -**0- */
/*TODO*///public static opcode orb_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///	B |= RM(EAD);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $eB ADDB indexed ***** */
/*TODO*///public static opcode addb_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	fetch_effective_address();
/*TODO*///	t = RM(EAD);
/*TODO*///	r = B + t;
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	SET_H(B,t,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $eC LDD indexed -**0- */
/*TODO*///public static opcode ldd_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    D=RM16(EAD);
/*TODO*///	CLR_NZV; SET_NZ16(D);
/*TODO*///}};
/*TODO*///
/*TODO*////* $eD STD indexed -**0- */
/*TODO*///public static opcode std_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    CLR_NZV;
/*TODO*///	SET_NZ16(D);
/*TODO*///	WM16(EAD,&pD);
/*TODO*///}};
/*TODO*///
/*TODO*////* $eE LDU (LDS) indexed -**0- */
/*TODO*///public static opcode ldu_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    U=RM16(EAD);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(U);
/*TODO*///}};
/*TODO*///
/*TODO*////* $10eE LDS indexed -**0- */
/*TODO*///public static opcode lds_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    S=RM16(EAD);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(S);
/*TODO*///	m6809.int_state |= M6809_LDS;
/*TODO*///}};
/*TODO*///
/*TODO*////* $eF STU (STS) indexed -**0- */
/*TODO*///public static opcode stu_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    CLR_NZV;
/*TODO*///	SET_NZ16(U);
/*TODO*///	WM16(EAD,&pU);
/*TODO*///}};
/*TODO*///
/*TODO*////* $10eF STS indexed -**0- */
/*TODO*///public static opcode sts_ix= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	fetch_effective_address();
/*TODO*///    CLR_NZV;
/*TODO*///	SET_NZ16(S);
/*TODO*///	WM16(EAD,&pS);
/*TODO*///}};
/*TODO*///
/*TODO*///#ifdef macintosh
/*TODO*///#pragma mark ____Fx____
/*TODO*///#endif
/*TODO*///
/*TODO*////* $f0 SUBB extended ?**** */
/*TODO*///public static opcode subb_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	EXTBYTE(t);
/*TODO*///	r = B - t;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $f1 CMPB extended ?**** */
/*TODO*///public static opcode cmpb_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	EXTBYTE(t);
/*TODO*///	r = B - t;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $f2 SBCB extended ?**** */
/*TODO*///public static opcode sbcb_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16	  t,r;
/*TODO*///	EXTBYTE(t);
/*TODO*///	r = B - t - (CC & CC_C);
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $f3 ADDD extended -**** */
/*TODO*///public static opcode addd_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	EXTWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d + b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///	D = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $f4 ANDB extended -**0- */
/*TODO*///public static opcode andb_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	EXTBYTE(t);
/*TODO*///	B &= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $f5 BITB extended -**0- */
/*TODO*///public static opcode bitb_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t,r;
/*TODO*///	EXTBYTE(t);
/*TODO*///	r = B & t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(r);
/*TODO*///}};
/*TODO*///
/*TODO*////* $f6 LDB extended -**0- */
/*TODO*///public static opcode ldb_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	EXTBYTE(B);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $f7 STB extended -**0- */
/*TODO*///public static opcode stb_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///	EXTENDED;
/*TODO*///	WM(EAD,B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $f8 EORB extended -**0- */
/*TODO*///public static opcode eorb_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	EXTBYTE(t);
/*TODO*///	B ^= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $f9 ADCB extended ***** */
/*TODO*///public static opcode adcb_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	EXTBYTE(t);
/*TODO*///	r = B + t + (CC & CC_C);
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	SET_H(B,t,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $fA ORB extended -**0- */
/*TODO*///public static opcode orb_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 t;
/*TODO*///	EXTBYTE(t);
/*TODO*///	B |= t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///}};
/*TODO*///
/*TODO*////* $fB ADDB extended ***** */
/*TODO*///public static opcode addb_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT16 t,r;
/*TODO*///	EXTBYTE(t);
/*TODO*///	r = B + t;
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	SET_H(B,t,r);
/*TODO*///	B = r;
/*TODO*///}};
/*TODO*///
/*TODO*////* $fC LDD extended -**0- */
/*TODO*///public static opcode ldd_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	EXTWORD(pD);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(D);
/*TODO*///}};
/*TODO*///
/*TODO*////* $fD STD extended -**0- */
/*TODO*///public static opcode std_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(D);
/*TODO*///    EXTENDED;
/*TODO*///	WM16(EAD,&pD);
/*TODO*///}};
/*TODO*///
/*TODO*////* $fE LDU (LDS) extended -**0- */
/*TODO*///public static opcode ldu_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	EXTWORD(pU);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(U);
/*TODO*///}};
/*TODO*///
/*TODO*////* $10fE LDS extended -**0- */
/*TODO*///public static opcode lds_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	EXTWORD(pS);
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(S);
/*TODO*///	m6809.int_state |= M6809_LDS;
/*TODO*///}};
/*TODO*///
/*TODO*////* $fF STU (STS) extended -**0- */
/*TODO*///public static opcode stu_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(U);
/*TODO*///	EXTENDED;
/*TODO*///	WM16(EAD,&pU);
/*TODO*///}};
/*TODO*///
/*TODO*////* $10fF STS extended -**0- */
/*TODO*///public static opcode sts_ex= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(S);
/*TODO*///	EXTENDED;
/*TODO*///	WM16(EAD,&pS);
/*TODO*///}};
/*TODO*///
/*TODO*////* $10xx opcodes */
/*TODO*///public static opcode pref10= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 ireg2 = ROP(PCD);
/*TODO*///	PC++;
/*TODO*///	switch( ireg2 )
/*TODO*///	{
/*TODO*///		case 0x21: lbrn();		m6809_ICount-=5;	break;
/*TODO*///		case 0x22: lbhi();		m6809_ICount-=5;	break;
/*TODO*///		case 0x23: lbls();		m6809_ICount-=5;	break;
/*TODO*///		case 0x24: lbcc();		m6809_ICount-=5;	break;
/*TODO*///		case 0x25: lbcs();		m6809_ICount-=5;	break;
/*TODO*///		case 0x26: lbne();		m6809_ICount-=5;	break;
/*TODO*///		case 0x27: lbeq();		m6809_ICount-=5;	break;
/*TODO*///		case 0x28: lbvc();		m6809_ICount-=5;	break;
/*TODO*///		case 0x29: lbvs();		m6809_ICount-=5;	break;
/*TODO*///		case 0x2a: lbpl();		m6809_ICount-=5;	break;
/*TODO*///		case 0x2b: lbmi();		m6809_ICount-=5;	break;
/*TODO*///		case 0x2c: lbge();		m6809_ICount-=5;	break;
/*TODO*///		case 0x2d: lblt();		m6809_ICount-=5;	break;
/*TODO*///		case 0x2e: lbgt();		m6809_ICount-=5;	break;
/*TODO*///		case 0x2f: lble();		m6809_ICount-=5;	break;
/*TODO*///
/*TODO*///		case 0x3f: swi2();		m6809_ICount-=20;	break;
/*TODO*///
/*TODO*///		case 0x83: cmpd_im();	m6809_ICount-=5;	break;
/*TODO*///		case 0x8c: cmpy_im();	m6809_ICount-=5;	break;
/*TODO*///		case 0x8e: ldy_im();	m6809_ICount-=4;	break;
/*TODO*///		case 0x8f: sty_im();	m6809_ICount-=4;	break;
/*TODO*///
/*TODO*///		case 0x93: cmpd_di();	m6809_ICount-=7;	break;
/*TODO*///		case 0x9c: cmpy_di();	m6809_ICount-=7;	break;
/*TODO*///		case 0x9e: ldy_di();	m6809_ICount-=6;	break;
/*TODO*///		case 0x9f: sty_di();	m6809_ICount-=6;	break;
/*TODO*///
/*TODO*///		case 0xa3: cmpd_ix();	m6809_ICount-=7;	break;
/*TODO*///		case 0xac: cmpy_ix();	m6809_ICount-=7;	break;
/*TODO*///		case 0xae: ldy_ix();	m6809_ICount-=6;	break;
/*TODO*///		case 0xaf: sty_ix();	m6809_ICount-=6;	break;
/*TODO*///
/*TODO*///		case 0xb3: cmpd_ex();	m6809_ICount-=8;	break;
/*TODO*///		case 0xbc: cmpy_ex();	m6809_ICount-=8;	break;
/*TODO*///		case 0xbe: ldy_ex();	m6809_ICount-=7;	break;
/*TODO*///		case 0xbf: sty_ex();	m6809_ICount-=7;	break;
/*TODO*///
/*TODO*///		case 0xce: lds_im();	m6809_ICount-=4;	break;
/*TODO*///		case 0xcf: sts_im();	m6809_ICount-=4;	break;
/*TODO*///
/*TODO*///		case 0xde: lds_di();	m6809_ICount-=6;	break;
/*TODO*///		case 0xdf: sts_di();	m6809_ICount-=6;	break;
/*TODO*///
/*TODO*///		case 0xee: lds_ix();	m6809_ICount-=6;	break;
/*TODO*///		case 0xef: sts_ix();	m6809_ICount-=6;	break;
/*TODO*///
/*TODO*///		case 0xfe: lds_ex();	m6809_ICount-=7;	break;
/*TODO*///		case 0xff: sts_ex();	m6809_ICount-=7;	break;
/*TODO*///
/*TODO*///		default:   illegal();						break;
/*TODO*///	}};
/*TODO*///}};
/*TODO*///
/*TODO*////* $11xx opcodes */
/*TODO*///public static opcode pref11= new opcode() { public void handler() { throw new UnsupportedOperationException("Unsupported");
/*TODO*///	UINT8 ireg2 = ROP(PCD);
/*TODO*///	PC++;
/*TODO*///	switch( ireg2 )
/*TODO*///	{
/*TODO*///		case 0x3f: swi3();		m6809_ICount-=20;	break;
/*TODO*///
/*TODO*///		case 0x83: cmpu_im();	m6809_ICount-=5;	break;
/*TODO*///		case 0x8c: cmps_im();	m6809_ICount-=5;	break;
/*TODO*///
/*TODO*///		case 0x93: cmpu_di();	m6809_ICount-=7;	break;
/*TODO*///		case 0x9c: cmps_di();	m6809_ICount-=7;	break;
/*TODO*///
/*TODO*///		case 0xa3: cmpu_ix();	m6809_ICount-=7;	break;
/*TODO*///		case 0xac: cmps_ix();	m6809_ICount-=7;	break;
/*TODO*///
/*TODO*///		case 0xb3: cmpu_ex();	m6809_ICount-=8;	break;
/*TODO*///		case 0xbc: cmps_ex();	m6809_ICount-=8;	break;
/*TODO*///
/*TODO*///		default:   illegal();						break;
/*TODO*///	}};
/*TODO*///}};
/*TODO*///
/*TODO*///
/*TODO*///
    public static abstract interface opcode {

        public abstract void handler();
    }
}
