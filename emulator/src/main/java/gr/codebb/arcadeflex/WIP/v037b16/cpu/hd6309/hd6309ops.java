package gr.codebb.arcadeflex.WIP.v037b16.cpu.hd6309;

public class hd6309ops {

    /*TODO*////*
    /*TODO*///
    /*TODO*///HNZVC
    /*TODO*///
    /*TODO*///? = undefined
    /*TODO*///* = affected
    /*TODO*///- = unaffected
    /*TODO*///0 = cleared
    /*TODO*///1 = set
    /*TODO*///# = CCr directly affected by instruction
    /*TODO*///@ = special - carry set if bit 7 is set
    /*TODO*///
    /*TODO*///*/
    /*TODO*///
    /*TODO*///INLINE void illegal( void )
    /*TODO*///{
    /*TODO*///	LOG(("HD6309: illegal opcode at %04x\nVectoring to [$fff0]\n",PC));
    /*TODO*///
    /*TODO*///	CC |= CC_E; 				/* save entire state */
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PUSHWORD(pU);
    /*TODO*///	PUSHWORD(pY);
    /*TODO*///	PUSHWORD(pX);
    /*TODO*///	PUSHBYTE(DP);
    /*TODO*///
    /*TODO*///	if ((MD & MD_EM) != 0)
    /*TODO*///	{
    /*TODO*///		PUSHBYTE(F);
    /*TODO*///		PUSHBYTE(E);
    /*TODO*///		hd6309_ICount -= 2;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	PUSHBYTE(B);
    /*TODO*///	PUSHBYTE(A);
    /*TODO*///	PUSHBYTE(CC);
    /*TODO*///
    /*TODO*///	PCD = RM16(0xfff0);
    /*TODO*///	CHANGE_PC;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void IIError(void)
    /*TODO*///{
    /*TODO*///	SEII;			// Set illegal Instruction Flag
    /*TODO*///	illegal();		// Vector to Trap handler
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void DZError(void)
    /*TODO*///{
    /*TODO*///	SEDZ;			// Set Division by Zero Flag
    /*TODO*///	illegal();		// Vector to Trap handler
    /*TODO*///}
    /*TODO*///
    /*TODO*///#ifdef macintosh
    /*TODO*///#pragma mark ____0x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $00 NEG direct ?**** */
    /*TODO*///INLINE void neg_di( void )
    /*TODO*///{
    /*TODO*///	UINT16 r,t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = -t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(0,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $01 OIM direct ?**** */
    /*TODO*///INLINE void oim_di( void )
    /*TODO*///{
    /*TODO*///	UINT8	r,t,im;
    /*TODO*///	IMMBYTE(im);
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = im | t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $02 AIM direct */
    /*TODO*///INLINE void aim_di( void )
    /*TODO*///{
    /*TODO*///	UINT8	r,t,im;
    /*TODO*///	IMMBYTE(im);
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = im & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $03 COM direct -**01 */
    /*TODO*///INLINE void com_di( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	t = ~t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(t);
    /*TODO*///	SEC;
    /*TODO*///	WM(EAD,t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $04 LSR direct -0*-* */
    /*TODO*///INLINE void lsr_di( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t & CC_C);
    /*TODO*///	t >>= 1;
    /*TODO*///	SET_Z8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $05 EIM direct */
    /*TODO*///INLINE void eim_di( void )
    /*TODO*///{
    /*TODO*///	UINT8	r,t,im;
    /*TODO*///	IMMBYTE(im);
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = im ^ t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $06 ROR direct -**-* */
    /*TODO*///INLINE void ror_di( void )
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r= (CC & CC_C) << 7;
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t & CC_C);
    /*TODO*///	r |= t>>1;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $07 ASR direct ?**-* */
    /*TODO*///INLINE void asr_di( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t & CC_C);
    /*TODO*///	t = (t & 0x80) | (t >> 1);
    /*TODO*///	SET_NZ8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $08 ASL direct ?**** */
    /*TODO*///INLINE void asl_di( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = t << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $09 ROL direct -**** */
    /*TODO*///INLINE void rol_di( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = (CC & CC_C) | (t << 1);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $0A DEC direct -***- */
    /*TODO*///INLINE void dec_di( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	--t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS8D(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $0B TIM direct */
    /*TODO*///INLINE void tim_di( void )
    /*TODO*///{
    /*TODO*///	UINT8	r,t,im;
    /*TODO*///	IMMBYTE(im);
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = im & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $OC INC direct -***- */
    /*TODO*///INLINE void inc_di( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	++t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS8I(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $OD TST direct -**0- */
    /*TODO*///INLINE void tst_di( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $0E JMP direct ----- */
    /*TODO*///INLINE void jmp_di( void )
    /*TODO*///{
    /*TODO*///	DIRECT;
    /*TODO*///	PCD = EAD;
    /*TODO*///	CHANGE_PC;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $0F CLR direct -0100 */
    /*TODO*///INLINE void clr_di( void )
    /*TODO*///{
    /*TODO*///	DIRECT;
    /*TODO*///	WM(EAD,0);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SEZ;
    /*TODO*///}
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
    /*TODO*///INLINE void nop( void )
    /*TODO*///{
    /*TODO*///	;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $13 SYNC inherent ----- */
    /*TODO*///INLINE void sync( void )
    /*TODO*///{
    /*TODO*///	/* SYNC stops processing instructions until an interrupt request happens. */
    /*TODO*///	/* This doesn't require the corresponding interrupt to be enabled: if it */
    /*TODO*///	/* is disabled, execution continues with the next instruction. */
    /*TODO*///	hd6309.int_state |= HD6309_SYNC;	 /* HJB 990227 */
    /*TODO*///	CHECK_IRQ_LINES();
    /*TODO*///	/* if HD6309_SYNC has not been cleared by CHECK_IRQ_LINES(),
    /*TODO*///	 * stop execution until the interrupt lines change. */
    /*TODO*///	if( hd6309.int_state & HD6309_SYNC )
    /*TODO*///		if (hd6309_ICount > 0) hd6309_ICount = 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $14 sexw inherent */
    /*TODO*///INLINE void sexw( void )
    /*TODO*///{
    /*TODO*///	UINT32 t;
    /*TODO*///	t = SIGNED_16(W);
    /*TODO*///	D = t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_N8(A);
    /*TODO*///	if ( D == 0 && W == 0 ) SEZ;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $15 ILLEGAL */
    /*TODO*///
    /*TODO*////* $16 LBRA relative ----- */
    /*TODO*///INLINE void lbra( void )
    /*TODO*///{
    /*TODO*///	IMMWORD(ea);
    /*TODO*///	PC += EA;
    /*TODO*///	CHANGE_PC;
    /*TODO*///
    /*TODO*///	if ( EA == 0xfffd )  /* EHC 980508 speed up busy loop */
    /*TODO*///		if ( hd6309_ICount > 0)
    /*TODO*///			hd6309_ICount = 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $17 LBSR relative ----- */
    /*TODO*///INLINE void lbsr( void )
    /*TODO*///{
    /*TODO*///	IMMWORD(ea);
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PC += EA;
    /*TODO*///	CHANGE_PC;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $18 ILLEGAL */
    /*TODO*///
    /*TODO*////* $19 DAA inherent (A) -**0* */
    /*TODO*///INLINE void daa( void )
    /*TODO*///{
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
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1A ORCC immediate ##### */
    /*TODO*///INLINE void orcc( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	CC |= t;
    /*TODO*///	CHECK_IRQ_LINES();	/* HJB 990116 */
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1B ILLEGAL */
    /*TODO*///
    /*TODO*////* $1C ANDCC immediate ##### */
    /*TODO*///INLINE void andcc( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	CC &= t;
    /*TODO*///	CHECK_IRQ_LINES();	/* HJB 990116 */
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1D SEX inherent -**0- */
    /*TODO*///INLINE void sex( void )
    /*TODO*///{
    /*TODO*///	UINT16 t;
    /*TODO*///	t = SIGNED(B);
    /*TODO*///	D = t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(t);
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE void exg( void )
    /*TODO*///{
    /*TODO*///	UINT16 t1,t2;
    /*TODO*///	UINT8 tb;
    /*TODO*///	int 	promote = FALSE;
    /*TODO*///
    /*TODO*///	IMMBYTE(tb);
    /*TODO*///	if( (tb^(tb>>4)) & 0x08 )	/* HJB 990225: mixed 8/16 bit case? */
    /*TODO*///	{
    /*TODO*///		promote = TRUE;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	switch(tb>>4) {
    /*TODO*///		case  0: t1 = D;  break;
    /*TODO*///		case  1: t1 = X;  break;
    /*TODO*///		case  2: t1 = Y;  break;
    /*TODO*///		case  3: t1 = U;  break;
    /*TODO*///		case  4: t1 = S;  break;
    /*TODO*///		case  5: t1 = PC; break;
    /*TODO*///		case  6: t1 = W;  break;
    /*TODO*///		case  7: t1 = V;  break;
    /*TODO*///		case  8: t1 = (promote ? D : A);  break;
    /*TODO*///		case  9: t1 = (promote ? D : B);  break;
    /*TODO*///		case 10: t1 = CC; break;
    /*TODO*///		case 11: t1 = DP; break;
    /*TODO*///		case 12: t1 = 0;  break;
    /*TODO*///		case 13: t1 = 0;  break;
    /*TODO*///		case 14: t1 = (promote ? W : E ); break;
    /*TODO*///		default: t1 = (promote ? W : F ); break;
    /*TODO*///	}
    /*TODO*///	switch(tb&15) {
    /*TODO*///		case  0: t2 = D;  break;
    /*TODO*///		case  1: t2 = X;  break;
    /*TODO*///		case  2: t2 = Y;  break;
    /*TODO*///		case  3: t2 = U;  break;
    /*TODO*///		case  4: t2 = S;  break;
    /*TODO*///		case  5: t2 = PC; break;
    /*TODO*///		case  6: t2 = W;  break;
    /*TODO*///		case  7: t2 = V;  break;
    /*TODO*///		case  8: t2 = (promote ? D : A);  break;
    /*TODO*///		case  9: t2 = (promote ? D : B);  break;
    /*TODO*///		case 10: t2 = CC; break;
    /*TODO*///		case 11: t2 = DP; break;
    /*TODO*///		case 12: t2 = 0;  break;
    /*TODO*///		case 13: t2 = 0;  break;
    /*TODO*///		case 14: t2 = (promote ? W : E); break;
    /*TODO*///		default: t2 = (promote ? W : F); break;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	switch(tb>>4) {
    /*TODO*///		case  0: D = t2;  break;
    /*TODO*///		case  1: X = t2;  break;
    /*TODO*///		case  2: Y = t2;  break;
    /*TODO*///		case  3: U = t2;  break;
    /*TODO*///		case  4: S = t2;  break;
    /*TODO*///		case  5: PC = t2; CHANGE_PC; break;
    /*TODO*///		case  6: W = t2;  break;
    /*TODO*///		case  7: V = t2;  break;
    /*TODO*///		case  8: if (promote != 0) D = t2; else A = t2; break;
    /*TODO*///		case  9: if (promote != 0) D = t2; else B = t2; break;
    /*TODO*///		case 10: CC = t2; break;
    /*TODO*///		case 11: DP = t2; break;
    /*TODO*///		case 12: /* 0 = t2 */ break;
    /*TODO*///		case 13: /* 0 = t2 */ break;
    /*TODO*///		case 14: if (promote != 0) W = t2; else E = t2; break;
    /*TODO*///		case 15: if (promote != 0) W = t2; else F = t2; break;
    /*TODO*///	}
    /*TODO*///	switch(tb&15) {
    /*TODO*///		case  0: D = t1;  break;
    /*TODO*///		case  1: X = t1;  break;
    /*TODO*///		case  2: Y = t1;  break;
    /*TODO*///		case  3: U = t1;  break;
    /*TODO*///		case  4: S = t1;  break;
    /*TODO*///		case  5: PC = t1; CHANGE_PC; break;
    /*TODO*///		case  6: W = t1;  break;
    /*TODO*///		case  7: V = t1;  break;
    /*TODO*///		case  8: if (promote != 0) D = t1; else A = t1; break;
    /*TODO*///		case  9: if (promote != 0) D = t1; else B = t1; break;
    /*TODO*///		case 10: CC = t1; break;
    /*TODO*///		case 11: DP = t1; break;
    /*TODO*///		case 12: /* 0 = t1 */ break;
    /*TODO*///		case 13: /* 0 = t1 */ break;
    /*TODO*///		case 14: if (promote != 0) W = t1; else E = t1; break;
    /*TODO*///		case 15: if (promote != 0) W = t1; else F = t1; break;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1F TFR inherent ----- */
    /*TODO*///INLINE void tfr( void )
    /*TODO*///{
    /*TODO*///	UINT8 tb;
    /*TODO*///	UINT16 t;
    /*TODO*///	int 	promote = FALSE;
    /*TODO*///
    /*TODO*///	IMMBYTE(tb);
    /*TODO*///	if( (tb^(tb>>4)) & 0x08 )
    /*TODO*///	{
    /*TODO*///		promote = TRUE;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	switch(tb>>4) {
    /*TODO*///		case  0: t = D;  break;
    /*TODO*///		case  1: t = X;  break;
    /*TODO*///		case  2: t = Y;  break;
    /*TODO*///		case  3: t = U;  break;
    /*TODO*///		case  4: t = S;  break;
    /*TODO*///		case  5: t = PC; break;
    /*TODO*///		case  6: t = W;  break;
    /*TODO*///		case  7: t = V;  break;
    /*TODO*///		case  8: t = (promote ? D : A );  break;
    /*TODO*///		case  9: t = (promote ? D : B );  break;
    /*TODO*///		case 10: t = CC; break;
    /*TODO*///		case 11: t = DP; break;
    /*TODO*///		case 12: t = 0;  break;
    /*TODO*///		case 13: t = 0;  break;
    /*TODO*///		case 14: t = (promote ? W : E ); break;
    /*TODO*///		default: t = (promote ? W : F ); break;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	switch(tb&15) {
    /*TODO*///		case  0: D = t;  break;
    /*TODO*///		case  1: X = t;  break;
    /*TODO*///		case  2: Y = t;  break;
    /*TODO*///		case  3: U = t;  break;
    /*TODO*///		case  4: S = t;  break;
    /*TODO*///		case  5: PC = t; CHANGE_PC; break;
    /*TODO*///		case  6: W = t;  break;
    /*TODO*///		case  7: V = t;  break;
    /*TODO*///		case  8: if (promote != 0) D = t; else A = t; break;
    /*TODO*///		case  9: if (promote != 0) D = t; else B = t; break;
    /*TODO*///		case 10: CC = t; break;
    /*TODO*///		case 11: DP = t; break;
    /*TODO*///		case 12: /* 0 = t1 */ break;
    /*TODO*///		case 13: /* 0 = t1 */ break;
    /*TODO*///		case 14: if (promote != 0) W = t; else E = t; break;
    /*TODO*///		case 15: if (promote != 0) W = t; else F = t; break;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///#ifdef macintosh
    /*TODO*///#pragma mark ____2x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $20 BRA relative ----- */
    /*TODO*///INLINE void bra( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	PC += SIGNED(t);
    /*TODO*///	CHANGE_PC;
    /*TODO*///	/* JB 970823 - speed up busy loops */
    /*TODO*///	if( t == 0xfe )
    /*TODO*///		if( hd6309_ICount > 0 ) hd6309_ICount = 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $21 BRN relative ----- */
    /*TODO*///INLINE void brn( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1021 LBRN relative ----- */
    /*TODO*///INLINE void lbrn( void )
    /*TODO*///{
    /*TODO*///	IMMWORD(ea);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $22 BHI relative ----- */
    /*TODO*///INLINE void bhi( void )
    /*TODO*///{
    /*TODO*///	BRANCH( !(CC & (CC_Z|CC_C)) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1022 LBHI relative ----- */
    /*TODO*///INLINE void lbhi( void )
    /*TODO*///{
    /*TODO*///	LBRANCH( !(CC & (CC_Z|CC_C)) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $23 BLS relative ----- */
    /*TODO*///INLINE void bls( void )
    /*TODO*///{
    /*TODO*///	BRANCH( (CC & (CC_Z|CC_C)) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1023 LBLS relative ----- */
    /*TODO*///INLINE void lbls( void )
    /*TODO*///{
    /*TODO*///	LBRANCH( (CC&(CC_Z|CC_C)) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $24 BCC relative ----- */
    /*TODO*///INLINE void bcc( void )
    /*TODO*///{
    /*TODO*///	BRANCH( !(CC&CC_C) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1024 LBCC relative ----- */
    /*TODO*///INLINE void lbcc( void )
    /*TODO*///{
    /*TODO*///	LBRANCH( !(CC&CC_C) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $25 BCS relative ----- */
    /*TODO*///INLINE void bcs( void )
    /*TODO*///{
    /*TODO*///	BRANCH( (CC&CC_C) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1025 LBCS relative ----- */
    /*TODO*///INLINE void lbcs( void )
    /*TODO*///{
    /*TODO*///	LBRANCH( (CC&CC_C) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $26 BNE relative ----- */
    /*TODO*///INLINE void bne( void )
    /*TODO*///{
    /*TODO*///	BRANCH( !(CC&CC_Z) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1026 LBNE relative ----- */
    /*TODO*///INLINE void lbne( void )
    /*TODO*///{
    /*TODO*///	LBRANCH( !(CC&CC_Z) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $27 BEQ relative ----- */
    /*TODO*///INLINE void beq( void )
    /*TODO*///{
    /*TODO*///	BRANCH( (CC&CC_Z) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1027 LBEQ relative ----- */
    /*TODO*///INLINE void lbeq( void )
    /*TODO*///{
    /*TODO*///	LBRANCH( (CC&CC_Z) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $28 BVC relative ----- */
    /*TODO*///INLINE void bvc( void )
    /*TODO*///{
    /*TODO*///	BRANCH( !(CC&CC_V) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1028 LBVC relative ----- */
    /*TODO*///INLINE void lbvc( void )
    /*TODO*///{
    /*TODO*///	LBRANCH( !(CC&CC_V) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $29 BVS relative ----- */
    /*TODO*///INLINE void bvs( void )
    /*TODO*///{
    /*TODO*///	BRANCH( (CC&CC_V) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1029 LBVS relative ----- */
    /*TODO*///INLINE void lbvs( void )
    /*TODO*///{
    /*TODO*///	LBRANCH( (CC&CC_V) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $2A BPL relative ----- */
    /*TODO*///INLINE void bpl( void )
    /*TODO*///{
    /*TODO*///	BRANCH( !(CC&CC_N) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $102A LBPL relative ----- */
    /*TODO*///INLINE void lbpl( void )
    /*TODO*///{
    /*TODO*///	LBRANCH( !(CC&CC_N) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $2B BMI relative ----- */
    /*TODO*///INLINE void bmi( void )
    /*TODO*///{
    /*TODO*///	BRANCH( (CC&CC_N) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $102B LBMI relative ----- */
    /*TODO*///INLINE void lbmi( void )
    /*TODO*///{
    /*TODO*///	LBRANCH( (CC&CC_N) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $2C BGE relative ----- */
    /*TODO*///INLINE void bge( void )
    /*TODO*///{
    /*TODO*///	BRANCH( !NXORV );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $102C LBGE relative ----- */
    /*TODO*///INLINE void lbge( void )
    /*TODO*///{
    /*TODO*///	LBRANCH( !NXORV );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $2D BLT relative ----- */
    /*TODO*///INLINE void blt( void )
    /*TODO*///{
    /*TODO*///	BRANCH( NXORV );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $102D LBLT relative ----- */
    /*TODO*///INLINE void lblt( void )
    /*TODO*///{
    /*TODO*///	LBRANCH( NXORV );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $2E BGT relative ----- */
    /*TODO*///INLINE void bgt( void )
    /*TODO*///{
    /*TODO*///	BRANCH( !(NXORV || (CC&CC_Z)) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $102E LBGT relative ----- */
    /*TODO*///INLINE void lbgt( void )
    /*TODO*///{
    /*TODO*///	LBRANCH( !(NXORV || (CC&CC_Z)) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $2F BLE relative ----- */
    /*TODO*///INLINE void ble( void )
    /*TODO*///{
    /*TODO*///	BRANCH( (NXORV || (CC&CC_Z)) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $102F LBLE relative ----- */
    /*TODO*///INLINE void lble( void )
    /*TODO*///{
    /*TODO*///	LBRANCH( (NXORV || (CC&CC_Z)) );
    /*TODO*///}
    /*TODO*///
    /*TODO*///#ifdef macintosh
    /*TODO*///#pragma mark ____3x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///#define REGREG_PREAMBLE														\
    /*TODO*///	IMMBYTE(tb);															\
    /*TODO*///	if( (tb^(tb>>4)) & 0x08 )												\
    /*TODO*///		{promote = TRUE;}													\
    /*TODO*///	switch(tb>>4) {															\
    /*TODO*///		case  0: src16Reg = &D; large = TRUE;  break;						\
    /*TODO*///		case  1: src16Reg = &X; large = TRUE;  break;						\
    /*TODO*///		case  2: src16Reg = &Y; large = TRUE;  break;						\
    /*TODO*///		case  3: src16Reg = &U; large = TRUE;  break;						\
    /*TODO*///		case  4: src16Reg = &S; large = TRUE;  break;						\
    /*TODO*///		case  5: src16Reg = &PC; large = TRUE; break;						\
    /*TODO*///		case  6: src16Reg = &W; large = TRUE;  break;						\
    /*TODO*///		case  7: src16Reg = &V; large = TRUE;  break;						\
    /*TODO*///		case  8: if (promote != 0) src16Reg = &D; else src8Reg = &A; break;		\
    /*TODO*///		case  9: if (promote != 0) src16Reg = &D; else src8Reg = &B; break;		\
    /*TODO*///		case 10: if (promote != 0) src16Reg = &z16; else src8Reg = &CC; break;	\
    /*TODO*///		case 11: if (promote != 0) src16Reg = &z16; else src8Reg = &DP; break;	\
    /*TODO*///		case 12: if (promote != 0) src16Reg = &z16; else src8Reg = &z8; break;	\
    /*TODO*///		case 13: if (promote != 0) src16Reg = &z16; else src8Reg = &z8; break;	\
    /*TODO*///		case 14: if (promote != 0) src16Reg = &W; else src8Reg = &E; break;		\
    /*TODO*///		default: if (promote != 0) src16Reg = &W; else src8Reg = &F; break;		\
    /*TODO*///	}																		\
    /*TODO*///	switch(tb&15) {															\
    /*TODO*///		case  0: dst16Reg = &D; large = TRUE;  break;						\
    /*TODO*///		case  1: dst16Reg = &X; large = TRUE;  break;						\
    /*TODO*///		case  2: dst16Reg = &Y; large = TRUE;  break;						\
    /*TODO*///		case  3: dst16Reg = &U; large = TRUE;  break;						\
    /*TODO*///		case  4: dst16Reg = &S; large = TRUE;  break;						\
    /*TODO*///		case  5: dst16Reg = &PC; large = TRUE; break;						\
    /*TODO*///		case  6: dst16Reg = &W; large = TRUE;  break;						\
    /*TODO*///		case  7: dst16Reg = &V; large = TRUE;  break;						\
    /*TODO*///		case  8: if (promote != 0) dst16Reg = &D; else dst8Reg = &A; break;		\
    /*TODO*///		case  9: if (promote != 0) dst16Reg = &D; else dst8Reg = &B; break;		\
    /*TODO*///		case 10: if (promote != 0) dst16Reg = &z16; else dst8Reg = &CC; break;	\
    /*TODO*///		case 11: if (promote != 0) dst16Reg = &z16; else dst8Reg = &DP; break;	\
    /*TODO*///		case 12: if (promote != 0) dst16Reg = &z16; else dst8Reg = &z8; break;	\
    /*TODO*///		case 13: if (promote != 0) dst16Reg = &z16; else dst8Reg = &z8; break;	\
    /*TODO*///		case 14: if (promote != 0) dst16Reg = &W; else dst8Reg = &E; break;		\
    /*TODO*///		default: if (promote != 0) dst16Reg = &W; else dst8Reg = &F; break;		\
    /*TODO*///	}																		\
    /*TODO*///
    /*TODO*////* $1030 addr_r r1 + r2 . r2 */
    /*TODO*///
    /*TODO*///INLINE void addr_r( void )
    /*TODO*///{
    /*TODO*///	UINT8	tb, z8 = 0;
    /*TODO*///	UINT16	z16 = 0, r8;
    /*TODO*///	UINT32	r16;
    /*TODO*///	UINT8	*src8Reg = NULL, *dst8Reg = NULL;
    /*TODO*///	UINT16	*src16Reg = NULL, *dst16Reg = NULL;
    /*TODO*///	int 	promote = FALSE, large = FALSE;
    /*TODO*///
    /*TODO*///	REGREG_PREAMBLE;
    /*TODO*///
    /*TODO*///	if (large != 0)
    /*TODO*///	{
    /*TODO*///		r16 = *src16Reg + *dst16Reg;
    /*TODO*///		CLR_HNZVC;
    /*TODO*///		SET_FLAGS16(*src16Reg,*dst16Reg,r16);
    /*TODO*///		*dst16Reg = r16;
    /*TODO*///
    /*TODO*///		if ( (tb&15) == 5 )
    /*TODO*///		{
    /*TODO*///			CHANGE_PC;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		r8 = *src8Reg + *dst8Reg;
    /*TODO*///		CLR_HNZVC;
    /*TODO*///		SET_FLAGS8(*src8Reg,*dst8Reg,r8);
    /*TODO*///		/* SET_H(*src8Reg,*src8Reg,r8);*/ /*Experimentation prooved this not to be the case */
    /*TODO*///		*dst8Reg = r8;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE void adcr( void )
    /*TODO*///{
    /*TODO*///	UINT8	tb, z8 = 0;
    /*TODO*///	UINT16	z16 = 0, r8;
    /*TODO*///	UINT32	r16;
    /*TODO*///	UINT8	*src8Reg = NULL, *dst8Reg = NULL;
    /*TODO*///	UINT16	*src16Reg = NULL, *dst16Reg = NULL;
    /*TODO*///	int 	promote = FALSE, large = FALSE;
    /*TODO*///
    /*TODO*///	REGREG_PREAMBLE;
    /*TODO*///
    /*TODO*///	if (large != 0)
    /*TODO*///	{
    /*TODO*///		r16 = *src16Reg + *dst16Reg + (CC & CC_C);
    /*TODO*///		CLR_HNZVC;
    /*TODO*///		SET_FLAGS16(*src16Reg,*dst16Reg,r16);
    /*TODO*///		*dst16Reg = r16;
    /*TODO*///
    /*TODO*///		if ( (tb&15) == 5 )
    /*TODO*///		{
    /*TODO*///			CHANGE_PC;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		r8 = *src8Reg + *dst8Reg + (CC & CC_C);
    /*TODO*///		CLR_HNZVC;
    /*TODO*///		SET_FLAGS8(*src8Reg,*dst8Reg,r8);
    /*TODO*///		/* SET_H(*src8Reg,*src8Reg,r8);*/ /*Experimentation prooved this not to be the case */
    /*TODO*///		*dst8Reg = r8;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1032 SUBR r1 - r2 . r2 */
    /*TODO*///INLINE void subr( void )
    /*TODO*///{
    /*TODO*///	UINT8	tb, z8 = 0;
    /*TODO*///	UINT16	z16 = 0, r8;
    /*TODO*///	UINT32	r16;
    /*TODO*///	UINT8	*src8Reg = NULL, *dst8Reg = NULL;
    /*TODO*///	UINT16	*src16Reg = NULL, *dst16Reg = NULL;
    /*TODO*///	int 	promote = FALSE, large = FALSE;
    /*TODO*///
    /*TODO*///	REGREG_PREAMBLE;
    /*TODO*///
    /*TODO*///	if (large != 0)
    /*TODO*///	{
    /*TODO*///		r16 = (UINT32)*dst16Reg - (UINT32)*src16Reg;
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_FLAGS16((UINT32)*dst16Reg,(UINT32)*src16Reg,r16);
    /*TODO*///		*dst16Reg = r16;
    /*TODO*///
    /*TODO*///		if ( (tb&15) == 5 )
    /*TODO*///		{
    /*TODO*///			CHANGE_PC;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		r8 = *dst8Reg - *src8Reg;
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_FLAGS8(*dst8Reg,*src8Reg,r8);
    /*TODO*///		*dst8Reg = r8;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1033 SBCR r1 - r2 - C . r2 */
    /*TODO*///INLINE void sbcr( void )
    /*TODO*///{
    /*TODO*///	UINT8	tb, z8 = 0;
    /*TODO*///	UINT16	z16 = 0, r8;
    /*TODO*///	UINT32	r16;
    /*TODO*///	UINT8	*src8Reg = NULL, *dst8Reg = NULL;
    /*TODO*///	UINT16	*src16Reg = NULL, *dst16Reg = NULL;
    /*TODO*///	int 	promote = FALSE, large = FALSE;
    /*TODO*///
    /*TODO*///	REGREG_PREAMBLE;
    /*TODO*///
    /*TODO*///	if (large != 0)
    /*TODO*///	{
    /*TODO*///		r16 = (UINT32)*dst16Reg - (UINT32)*src16Reg - (CC & CC_C);
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_FLAGS16((UINT32)*dst16Reg,(UINT32)*src16Reg,r16);
    /*TODO*///		*dst16Reg = r16;
    /*TODO*///
    /*TODO*///		if ( (tb&15) == 5 )
    /*TODO*///		{
    /*TODO*///			CHANGE_PC;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		r8 = *dst8Reg - *src8Reg - (CC & CC_C);
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_FLAGS8(*dst8Reg,*src8Reg,r8);
    /*TODO*///		*dst8Reg = r8;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1034 ANDR r1 & r2 . r2 */
    /*TODO*///INLINE void andr( void )
    /*TODO*///{
    /*TODO*///	UINT8	tb, z8 = 0;
    /*TODO*///	UINT16	z16 = 0, r8;
    /*TODO*///	UINT32	r16;
    /*TODO*///	UINT8	*src8Reg = NULL, *dst8Reg = NULL;
    /*TODO*///	UINT16	*src16Reg = NULL, *dst16Reg = NULL;
    /*TODO*///	int 	promote = FALSE, large = FALSE;
    /*TODO*///
    /*TODO*///	REGREG_PREAMBLE;
    /*TODO*///
    /*TODO*///	if (large != 0)
    /*TODO*///	{
    /*TODO*///		r16 = *src16Reg & *dst16Reg;
    /*TODO*///		CLR_NZV;
    /*TODO*///		SET_NZ16(r16);
    /*TODO*///		*dst16Reg = r16;
    /*TODO*///
    /*TODO*///		if ( (tb&15) == 5 )
    /*TODO*///		{
    /*TODO*///			CHANGE_PC;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		r8 = *src8Reg & *dst8Reg;
    /*TODO*///		CLR_NZV;
    /*TODO*///		SET_NZ8(r8);
    /*TODO*///		*dst8Reg = r8;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1035 ORR r1 | r2 . r2 */
    /*TODO*///INLINE void orr( void )
    /*TODO*///{
    /*TODO*///	UINT8	tb, z8 = 0;
    /*TODO*///	UINT16	z16 = 0, r8;
    /*TODO*///	UINT32	r16;
    /*TODO*///	UINT8	*src8Reg = NULL, *dst8Reg = NULL;
    /*TODO*///	UINT16	*src16Reg = NULL, *dst16Reg = NULL;
    /*TODO*///	int 	promote = FALSE, large = FALSE;
    /*TODO*///
    /*TODO*///	REGREG_PREAMBLE;
    /*TODO*///
    /*TODO*///	if (large != 0)
    /*TODO*///	{
    /*TODO*///		r16 = *src16Reg | *dst16Reg;
    /*TODO*///		CLR_NZV;
    /*TODO*///		SET_NZ16(r16);
    /*TODO*///		*dst16Reg = r16;
    /*TODO*///
    /*TODO*///		if ( (tb&15) == 5 )
    /*TODO*///		{
    /*TODO*///			CHANGE_PC;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		r8 = *src8Reg | *dst8Reg;
    /*TODO*///		CLR_NZV;
    /*TODO*///		SET_NZ8(r8);
    /*TODO*///		*dst8Reg = r8;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1036 EORR r1 ^ r2 . r2 */
    /*TODO*///INLINE void eorr( void )
    /*TODO*///{
    /*TODO*///	UINT8	tb, z8 = 0;
    /*TODO*///	UINT16	z16 = 0, r8;
    /*TODO*///	UINT32	r16;
    /*TODO*///	UINT8	*src8Reg = NULL, *dst8Reg = NULL;
    /*TODO*///	UINT16	*src16Reg = NULL, *dst16Reg = NULL;
    /*TODO*///	int 	promote = FALSE, large = FALSE;
    /*TODO*///
    /*TODO*///	REGREG_PREAMBLE;
    /*TODO*///
    /*TODO*///	if (large != 0)
    /*TODO*///	{
    /*TODO*///		r16 = *src16Reg ^ *dst16Reg;
    /*TODO*///		CLR_NZV;
    /*TODO*///		SET_NZ16(r16);
    /*TODO*///		*dst16Reg = r16;
    /*TODO*///
    /*TODO*///		if ( (tb&15) == 5 )
    /*TODO*///		{
    /*TODO*///			CHANGE_PC;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		r8 = *src8Reg ^ *dst8Reg;
    /*TODO*///		CLR_NZV;
    /*TODO*///		SET_NZ8(r8);
    /*TODO*///		*dst8Reg = r8;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1037 CMPR r1 - r2 */
    /*TODO*///INLINE void cmpr( void )
    /*TODO*///{
    /*TODO*///	UINT8	tb, z8 = 0;
    /*TODO*///	UINT16	z16 = 0, r8;
    /*TODO*///	UINT32	r16;
    /*TODO*///	UINT8	*src8Reg = NULL, *dst8Reg = NULL;
    /*TODO*///	UINT16	*src16Reg = NULL, *dst16Reg = NULL;
    /*TODO*///	int 	promote = FALSE, large = FALSE;
    /*TODO*///
    /*TODO*///	REGREG_PREAMBLE;
    /*TODO*///
    /*TODO*///	if (large != 0)
    /*TODO*///	{
    /*TODO*///		r16 = (UINT32)*dst16Reg - (UINT32)*src16Reg;
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_FLAGS16((UINT32)*dst16Reg,(UINT32)*src16Reg,r16);
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		r8 = *dst8Reg - *src8Reg;
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_FLAGS8(*dst8Reg,*src8Reg,r8);
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1138 TFM R0+,R1+ */
    /*TODO*///INLINE void tfmpp( void )
    /*TODO*///{
    /*TODO*///	UINT8	tb, srcValue = 0;
    /*TODO*///	int 	done = FALSE;
    /*TODO*///
    /*TODO*///	IMMBYTE(tb);
    /*TODO*///
    /*TODO*///	if ( W != 0 )
    /*TODO*///	{
    /*TODO*///		switch(tb>>4) {
    /*TODO*///			case  0: srcValue = RM(D++); break;
    /*TODO*///			case  1: srcValue = RM(X++); break;
    /*TODO*///			case  2: srcValue = RM(Y++); break;
    /*TODO*///			case  3: srcValue = RM(U++); break;
    /*TODO*///			case  4: srcValue = RM(S++); break;
    /*TODO*///			case  5: /* PC */ done = TRUE; break;
    /*TODO*///			case  6: /* W  */ done = TRUE; break;
    /*TODO*///			case  7: /* V  */ done = TRUE; break;
    /*TODO*///			case  8: /* A  */ done = TRUE; break;
    /*TODO*///			case  9: /* B  */ done = TRUE; break;
    /*TODO*///			case 10: /* CC */ done = TRUE; break;
    /*TODO*///			case 11: /* DP */ done = TRUE; break;
    /*TODO*///			case 12: /* 0  */ done = TRUE; break;
    /*TODO*///			case 13: /* 0  */ done = TRUE; break;
    /*TODO*///			case 14: /* E  */ done = TRUE; break;
    /*TODO*///			default: /* F  */ done = TRUE; break;
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		if ( !done )
    /*TODO*///		{
    /*TODO*///			switch(tb&15) {
    /*TODO*///				case  0: WM(D++, srcValue); break;
    /*TODO*///				case  1: WM(X++, srcValue); break;
    /*TODO*///				case  2: WM(Y++, srcValue); break;
    /*TODO*///				case  3: WM(U++, srcValue); break;
    /*TODO*///				case  4: WM(S++, srcValue); break;
    /*TODO*///				case  5: /* PC */ done = TRUE; break;
    /*TODO*///				case  6: /* W  */ done = TRUE; break;
    /*TODO*///				case  7: /* V  */ done = TRUE; break;
    /*TODO*///				case  8: /* A  */ done = TRUE; break;
    /*TODO*///				case  9: /* B  */ done = TRUE; break;
    /*TODO*///				case 10: /* CC */ done = TRUE; break;
    /*TODO*///				case 11: /* DP */ done = TRUE; break;
    /*TODO*///				case 12: /* 0  */ done = TRUE; break;
    /*TODO*///				case 13: /* 0  */ done = TRUE; break;
    /*TODO*///				case 14: /* E  */ done = TRUE; break;
    /*TODO*///				default: /* F  */ done = TRUE; break;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			PCD = PCD - 3;
    /*TODO*///			CHANGE_PC;
    /*TODO*///			W--;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///		hd6309_ICount -= 3;   /* Needs three aditional cycles  to get the 6+3n */
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1139 TFM R0-,R1- */
    /*TODO*///INLINE void tfmmm( void )
    /*TODO*///{
    /*TODO*///	UINT8	tb, srcValue = 0;
    /*TODO*///	int 	done = FALSE;
    /*TODO*///
    /*TODO*///	IMMBYTE(tb);
    /*TODO*///
    /*TODO*///	if ( W != 0 )
    /*TODO*///	{
    /*TODO*///		switch(tb>>4) {
    /*TODO*///			case  0: srcValue = RM(D--); break;
    /*TODO*///			case  1: srcValue = RM(X--); break;
    /*TODO*///			case  2: srcValue = RM(Y--); break;
    /*TODO*///			case  3: srcValue = RM(U--); break;
    /*TODO*///			case  4: srcValue = RM(S--); break;
    /*TODO*///			case  5: /* PC */ done = TRUE; break;
    /*TODO*///			case  6: /* W  */ done = TRUE; break;
    /*TODO*///			case  7: /* V  */ done = TRUE; break;
    /*TODO*///			case  8: /* A  */ done = TRUE; break;
    /*TODO*///			case  9: /* B  */ done = TRUE; break;
    /*TODO*///			case 10: /* CC */ done = TRUE; break;
    /*TODO*///			case 11: /* DP */ done = TRUE; break;
    /*TODO*///			case 12: /* 0  */ done = TRUE; break;
    /*TODO*///			case 13: /* 0  */ done = TRUE; break;
    /*TODO*///			case 14: /* E  */ done = TRUE; break;
    /*TODO*///			default: /* F  */ done = TRUE; break;
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		if ( !done )
    /*TODO*///		{
    /*TODO*///			switch(tb&15) {
    /*TODO*///				case  0: WM(D--, srcValue); break;
    /*TODO*///				case  1: WM(X--, srcValue); break;
    /*TODO*///				case  2: WM(Y--, srcValue); break;
    /*TODO*///				case  3: WM(U--, srcValue); break;
    /*TODO*///				case  4: WM(S--, srcValue); break;
    /*TODO*///				case  5: /* PC */ done = TRUE; break;
    /*TODO*///				case  6: /* W  */ done = TRUE; break;
    /*TODO*///				case  7: /* V  */ done = TRUE; break;
    /*TODO*///				case  8: /* A  */ done = TRUE; break;
    /*TODO*///				case  9: /* B  */ done = TRUE; break;
    /*TODO*///				case 10: /* CC */ done = TRUE; break;
    /*TODO*///				case 11: /* DP */ done = TRUE; break;
    /*TODO*///				case 12: /* 0  */ done = TRUE; break;
    /*TODO*///				case 13: /* 0  */ done = TRUE; break;
    /*TODO*///				case 14: /* E  */ done = TRUE; break;
    /*TODO*///				default: /* F  */ done = TRUE; break;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			PCD = PCD - 3;
    /*TODO*///			CHANGE_PC;
    /*TODO*///			W--;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///		hd6309_ICount -= 3;   /* Needs three aditional cycles  to get the 6+3n */
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $113A TFM R0+,R1 */
    /*TODO*///INLINE void tfmpc( void )
    /*TODO*///{
    /*TODO*///	UINT8	tb, srcValue = 0;
    /*TODO*///	int 	done = FALSE;
    /*TODO*///
    /*TODO*///	IMMBYTE(tb);
    /*TODO*///
    /*TODO*///	if ( W != 0 )
    /*TODO*///	{
    /*TODO*///		switch(tb>>4) {
    /*TODO*///			case  0: srcValue = RM(D++); break;
    /*TODO*///			case  1: srcValue = RM(X++); break;
    /*TODO*///			case  2: srcValue = RM(Y++); break;
    /*TODO*///			case  3: srcValue = RM(U++); break;
    /*TODO*///			case  4: srcValue = RM(S++); break;
    /*TODO*///			case  5: /* PC */ done = TRUE; break;
    /*TODO*///			case  6: /* W  */ done = TRUE; break;
    /*TODO*///			case  7: /* V  */ done = TRUE; break;
    /*TODO*///			case  8: /* A  */ done = TRUE; break;
    /*TODO*///			case  9: /* B  */ done = TRUE; break;
    /*TODO*///			case 10: /* CC */ done = TRUE; break;
    /*TODO*///			case 11: /* DP */ done = TRUE; break;
    /*TODO*///			case 12: /* 0  */ done = TRUE; break;
    /*TODO*///			case 13: /* 0  */ done = TRUE; break;
    /*TODO*///			case 14: /* E  */ done = TRUE; break;
    /*TODO*///			default: /* F  */ done = TRUE; break;
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		if ( !done )
    /*TODO*///		{
    /*TODO*///			switch(tb&15) {
    /*TODO*///				case  0: WM(D, srcValue); break;
    /*TODO*///				case  1: WM(X, srcValue); break;
    /*TODO*///				case  2: WM(Y, srcValue); break;
    /*TODO*///				case  3: WM(U, srcValue); break;
    /*TODO*///				case  4: WM(S, srcValue); break;
    /*TODO*///				case  5: /* PC */ done = TRUE; break;
    /*TODO*///				case  6: /* W  */ done = TRUE; break;
    /*TODO*///				case  7: /* V  */ done = TRUE; break;
    /*TODO*///				case  8: /* A  */ done = TRUE; break;
    /*TODO*///				case  9: /* B  */ done = TRUE; break;
    /*TODO*///				case 10: /* CC */ done = TRUE; break;
    /*TODO*///				case 11: /* DP */ done = TRUE; break;
    /*TODO*///				case 12: /* 0  */ done = TRUE; break;
    /*TODO*///				case 13: /* 0  */ done = TRUE; break;
    /*TODO*///				case 14: /* E  */ done = TRUE; break;
    /*TODO*///				default: /* F  */ done = TRUE; break;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			PCD = PCD - 3;
    /*TODO*///			CHANGE_PC;
    /*TODO*///			W--;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///		hd6309_ICount -= 3;   /* Needs three aditional cycles  to get the 6+3n */
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $113B TFM R0,R1+ */
    /*TODO*///INLINE void tfmcp( void )
    /*TODO*///{
    /*TODO*///	UINT8	tb, srcValue = 0;
    /*TODO*///	int 	done = FALSE;
    /*TODO*///
    /*TODO*///	IMMBYTE(tb);
    /*TODO*///
    /*TODO*///	if ( W != 0 )
    /*TODO*///	{
    /*TODO*///		switch(tb>>4) {
    /*TODO*///			case  0: srcValue = RM(D); break;
    /*TODO*///			case  1: srcValue = RM(X); break;
    /*TODO*///			case  2: srcValue = RM(Y); break;
    /*TODO*///			case  3: srcValue = RM(U); break;
    /*TODO*///			case  4: srcValue = RM(S); break;
    /*TODO*///			case  5: /* PC */ done = TRUE; break;
    /*TODO*///			case  6: /* W  */ done = TRUE; break;
    /*TODO*///			case  7: /* V  */ done = TRUE; break;
    /*TODO*///			case  8: /* A  */ done = TRUE; break;
    /*TODO*///			case  9: /* B  */ done = TRUE; break;
    /*TODO*///			case 10: /* CC */ done = TRUE; break;
    /*TODO*///			case 11: /* DP */ done = TRUE; break;
    /*TODO*///			case 12: /* 0  */ done = TRUE; break;
    /*TODO*///			case 13: /* 0  */ done = TRUE; break;
    /*TODO*///			case 14: /* E  */ done = TRUE; break;
    /*TODO*///			default: /* F  */ done = TRUE; break;
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		if ( !done )
    /*TODO*///		{
    /*TODO*///			switch(tb&15) {
    /*TODO*///				case  0: WM(D++, srcValue); break;
    /*TODO*///				case  1: WM(X++, srcValue); break;
    /*TODO*///				case  2: WM(Y++, srcValue); break;
    /*TODO*///				case  3: WM(U++, srcValue); break;
    /*TODO*///				case  4: WM(S++, srcValue); break;
    /*TODO*///				case  5: /* PC */ done = TRUE; break;
    /*TODO*///				case  6: /* W  */ done = TRUE; break;
    /*TODO*///				case  7: /* V  */ done = TRUE; break;
    /*TODO*///				case  8: /* A  */ done = TRUE; break;
    /*TODO*///				case  9: /* B  */ done = TRUE; break;
    /*TODO*///				case 10: /* CC */ done = TRUE; break;
    /*TODO*///				case 11: /* DP */ done = TRUE; break;
    /*TODO*///				case 12: /* 0  */ done = TRUE; break;
    /*TODO*///				case 13: /* 0  */ done = TRUE; break;
    /*TODO*///				case 14: /* E  */ done = TRUE; break;
    /*TODO*///				default: /* F  */ done = TRUE; break;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			PCD = PCD - 3;
    /*TODO*///			CHANGE_PC;
    /*TODO*///			W--;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///		hd6309_ICount -= 3;   /* Needs three aditional cycles  to get the 6+3n */
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $30 LEAX indexed --*-- */
    /*TODO*///INLINE void leax( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	X = EA;
    /*TODO*///	CLR_Z;
    /*TODO*///	SET_Z(X);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $31 LEAY indexed --*-- */
    /*TODO*///INLINE void leay( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	Y = EA;
    /*TODO*///	CLR_Z;
    /*TODO*///	SET_Z(Y);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $32 LEAS indexed ----- */
    /*TODO*///INLINE void leas( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	S = EA;
    /*TODO*///	hd6309.int_state |= HD6309_LDS;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $33 LEAU indexed ----- */
    /*TODO*///INLINE void leau( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	U = EA;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $34 PSHS inherent ----- */
    /*TODO*///INLINE void pshs( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	if ((t & 0x80) != 0) { PUSHWORD(pPC); hd6309_ICount -= 2; }
    /*TODO*///	if ((t & 0x40) != 0) { PUSHWORD(pU);  hd6309_ICount -= 2; }
    /*TODO*///	if ((t & 0x20) != 0) { PUSHWORD(pY);  hd6309_ICount -= 2; }
    /*TODO*///	if ((t & 0x10) != 0) { PUSHWORD(pX);  hd6309_ICount -= 2; }
    /*TODO*///	if ((t & 0x08) != 0) { PUSHBYTE(DP);  hd6309_ICount -= 1; }
    /*TODO*///	if ((t & 0x04) != 0) { PUSHBYTE(B);   hd6309_ICount -= 1; }
    /*TODO*///	if ((t & 0x02) != 0) { PUSHBYTE(A);   hd6309_ICount -= 1; }
    /*TODO*///	if ((t & 0x01) != 0) { PUSHBYTE(CC);  hd6309_ICount -= 1; }
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1038 PSHSW inherent ----- */
    /*TODO*///INLINE void pshsw( void )
    /*TODO*///{
    /*TODO*///	PUSHWORD(pW);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $103a PSHUW inherent ----- */
    /*TODO*///INLINE void pshuw( void )
    /*TODO*///{
    /*TODO*///	PSHUWORD(pW);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $35 PULS inherent ----- */
    /*TODO*///INLINE void puls( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	if ((t & 0x01) != 0) { PULLBYTE(CC); hd6309_ICount -= 1; }
    /*TODO*///	if ((t & 0x02) != 0) { PULLBYTE(A);  hd6309_ICount -= 1; }
    /*TODO*///	if ((t & 0x04) != 0) { PULLBYTE(B);  hd6309_ICount -= 1; }
    /*TODO*///	if ((t & 0x08) != 0) { PULLBYTE(DP); hd6309_ICount -= 1; }
    /*TODO*///	if ((t & 0x10) != 0) { PULLWORD(XD); hd6309_ICount -= 2; }
    /*TODO*///	if ((t & 0x20) != 0) { PULLWORD(YD); hd6309_ICount -= 2; }
    /*TODO*///	if ((t & 0x40) != 0) { PULLWORD(UD); hd6309_ICount -= 2; }
    /*TODO*///	if ((t & 0x80) != 0) { PULLWORD(PCD); CHANGE_PC; hd6309_ICount -= 2; }
    /*TODO*///
    /*TODO*///	/* HJB 990225: moved check after all PULLs */
    /*TODO*///	if ((t & 0x01) != 0) { CHECK_IRQ_LINES(); }
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1039 PULSW inherent ----- */
    /*TODO*///INLINE void pulsw( void )
    /*TODO*///{
    /*TODO*///	PULLWORD(W);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $103b PULUW inherent ----- */
    /*TODO*///INLINE void puluw( void )
    /*TODO*///{
    /*TODO*///	PULUWORD(W);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $36 PSHU inherent ----- */
    /*TODO*///INLINE void pshu( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	if ((t & 0x80) != 0) { PSHUWORD(pPC); hd6309_ICount -= 2; }
    /*TODO*///	if ((t & 0x40) != 0) { PSHUWORD(pS);  hd6309_ICount -= 2; }
    /*TODO*///	if ((t & 0x20) != 0) { PSHUWORD(pY);  hd6309_ICount -= 2; }
    /*TODO*///	if ((t & 0x10) != 0) { PSHUWORD(pX);  hd6309_ICount -= 2; }
    /*TODO*///	if ((t & 0x08) != 0) { PSHUBYTE(DP);  hd6309_ICount -= 1; }
    /*TODO*///	if ((t & 0x04) != 0) { PSHUBYTE(B);   hd6309_ICount -= 1; }
    /*TODO*///	if ((t & 0x02) != 0) { PSHUBYTE(A);   hd6309_ICount -= 1; }
    /*TODO*///	if ((t & 0x01) != 0) { PSHUBYTE(CC);  hd6309_ICount -= 1; }
    /*TODO*///}
    /*TODO*///
    /*TODO*////* 37 PULU inherent ----- */
    /*TODO*///INLINE void pulu( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	if ((t & 0x01) != 0) { PULUBYTE(CC); hd6309_ICount -= 1; }
    /*TODO*///	if ((t & 0x02) != 0) { PULUBYTE(A);  hd6309_ICount -= 1; }
    /*TODO*///	if ((t & 0x04) != 0) { PULUBYTE(B);  hd6309_ICount -= 1; }
    /*TODO*///	if ((t & 0x08) != 0) { PULUBYTE(DP); hd6309_ICount -= 1; }
    /*TODO*///	if ((t & 0x10) != 0) { PULUWORD(XD); hd6309_ICount -= 2; }
    /*TODO*///	if ((t & 0x20) != 0) { PULUWORD(YD); hd6309_ICount -= 2; }
    /*TODO*///	if ((t & 0x40) != 0) { PULUWORD(SD); hd6309_ICount -= 2; }
    /*TODO*///	if ((t & 0x80) != 0) { PULUWORD(PCD); CHANGE_PC; hd6309_ICount -= 2; }
    /*TODO*///
    /*TODO*///	/* HJB 990225: moved check after all PULLs */
    /*TODO*///	if ((t & 0x01) != 0) { CHECK_IRQ_LINES(); }
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $38 ILLEGAL */
    /*TODO*///
    /*TODO*////* $39 RTS inherent ----- */
    /*TODO*///INLINE void rts( void )
    /*TODO*///{
    /*TODO*///	PULLWORD(PCD);
    /*TODO*///	CHANGE_PC;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $3A ABX inherent ----- */
    /*TODO*///INLINE void abx( void )
    /*TODO*///{
    /*TODO*///	X += B;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $3B RTI inherent ##### */
    /*TODO*///INLINE void rti( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	PULLBYTE(CC);
    /*TODO*///	t = CC & CC_E;		/* HJB 990225: entire state saved? */
    /*TODO*///	if (t != 0)
    /*TODO*///	{
    /*TODO*///		hd6309_ICount -= 9;
    /*TODO*///		PULLBYTE(A);
    /*TODO*///		PULLBYTE(B);
    /*TODO*///		if ((MD & MD_EM) != 0)
    /*TODO*///		{
    /*TODO*///			PULLBYTE(E);
    /*TODO*///			PULLBYTE(F);
    /*TODO*///			hd6309_ICount -= 2;
    /*TODO*///		}
    /*TODO*///		PULLBYTE(DP);
    /*TODO*///		PULLWORD(XD);
    /*TODO*///		PULLWORD(YD);
    /*TODO*///		PULLWORD(UD);
    /*TODO*///	}
    /*TODO*///	PULLWORD(PCD);
    /*TODO*///	CHANGE_PC;
    /*TODO*///	CHECK_IRQ_LINES();	/* HJB 990116 */
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $3C CWAI inherent ----1 */
    /*TODO*///INLINE void cwai( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	CC &= t;
    /*TODO*///	/*
    /*TODO*///	 * CWAI stacks the entire machine state on the hardware stack,
    /*TODO*///	 * then waits for an interrupt; when the interrupt is taken
    /*TODO*///	 * later, the state is *not* saved again after CWAI.
    /*TODO*///	 */
    /*TODO*///	CC |= CC_E; 		/* HJB 990225: save entire state */
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PUSHWORD(pU);
    /*TODO*///	PUSHWORD(pY);
    /*TODO*///	PUSHWORD(pX);
    /*TODO*///	PUSHBYTE(DP);
    /*TODO*///	if ((MD & MD_EM) != 0)
    /*TODO*///	{
    /*TODO*///		PUSHBYTE(E);
    /*TODO*///		PUSHBYTE(F);
    /*TODO*///	}
    /*TODO*///	PUSHBYTE(B);
    /*TODO*///	PUSHBYTE(A);
    /*TODO*///	PUSHBYTE(CC);
    /*TODO*///	hd6309.int_state |= HD6309_CWAI;	 /* HJB 990228 */
    /*TODO*///	CHECK_IRQ_LINES();	  /* HJB 990116 */
    /*TODO*///	if( hd6309.int_state & HD6309_CWAI )
    /*TODO*///		if( hd6309_ICount > 0 )
    /*TODO*///			hd6309_ICount = 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $3D MUL inherent --*-@ */
    /*TODO*///INLINE void mul( void )
    /*TODO*///{
    /*TODO*///	UINT16 t;
    /*TODO*///	t = A * B;
    /*TODO*///	CLR_ZC; SET_Z16(t); if ((t & 0x80) != 0) SEC;
    /*TODO*///	D = t;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $3E ILLEGAL */
    /*TODO*///
    /*TODO*////* $3F SWI (SWI2 SWI3) absolute indirect ----- */
    /*TODO*///INLINE void swi( void )
    /*TODO*///{
    /*TODO*///	CC |= CC_E; 			/* HJB 980225: save entire state */
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PUSHWORD(pU);
    /*TODO*///	PUSHWORD(pY);
    /*TODO*///	PUSHWORD(pX);
    /*TODO*///	PUSHBYTE(DP);
    /*TODO*///	if ((MD & MD_EM) != 0)
    /*TODO*///	{
    /*TODO*///		PUSHBYTE(F);
    /*TODO*///		PUSHBYTE(E);
    /*TODO*///		hd6309_ICount -= 2;
    /*TODO*///	}
    /*TODO*///	PUSHBYTE(B);
    /*TODO*///	PUSHBYTE(A);
    /*TODO*///	PUSHBYTE(CC);
    /*TODO*///	CC |= CC_IF | CC_II;	/* inhibit FIRQ and IRQ */
    /*TODO*///	PCD=RM16(0xfffa);
    /*TODO*///	CHANGE_PC;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1130 BAND */
    /*TODO*///
    /*TODO*///#define decodePB_tReg(n)	((n)&3)
    /*TODO*///#define decodePB_src(n) 	(((n)>>2)&7)
    /*TODO*///#define decodePB_dst(n) 	(((n)>>5)&7)
    /*TODO*///
    /*TODO*///static UBytePtr 	regTable[4] = { &(CC), &(A), &(B), &(E) };
    /*TODO*///
    /*TODO*///static UINT8	bitTable[] = { 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80 };
    /*TODO*///
    /*TODO*///INLINE void band( void )
    /*TODO*///{
    /*TODO*///	UINT8		pb;
    /*TODO*///	UINT16		db;
    /*TODO*///
    /*TODO*///	IMMBYTE(pb);
    /*TODO*///
    /*TODO*///	DIRBYTE(db);
    /*TODO*///
    /*TODO*///	if ( ( *(regTable[decodePB_tReg(pb)]) & bitTable[decodePB_dst(pb)] ) && ( db & bitTable[decodePB_src(pb)] ))
    /*TODO*///		*(regTable[decodePB_tReg(pb)]) |= bitTable[decodePB_dst(pb)];
    /*TODO*///	else
    /*TODO*///		*(regTable[decodePB_tReg(pb)]) &= (~bitTable[decodePB_dst(pb)]);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1131 BIAND */
    /*TODO*///
    /*TODO*///INLINE void biand( void )
    /*TODO*///{
    /*TODO*///	UINT8		pb;
    /*TODO*///	UINT16		db;
    /*TODO*///
    /*TODO*///	IMMBYTE(pb);
    /*TODO*///
    /*TODO*///	DIRBYTE(db);
    /*TODO*///
    /*TODO*///	if ( ( *(regTable[decodePB_tReg(pb)]) & bitTable[decodePB_dst(pb)] ) && ( (~db) & bitTable[decodePB_src(pb)] ))
    /*TODO*///		*(regTable[decodePB_tReg(pb)]) |= bitTable[decodePB_dst(pb)];
    /*TODO*///	else
    /*TODO*///		*(regTable[decodePB_tReg(pb)]) &= (~bitTable[decodePB_dst(pb)]);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1132 BOR */
    /*TODO*///
    /*TODO*///INLINE void bor( void )
    /*TODO*///{
    /*TODO*///	UINT8		pb;
    /*TODO*///	UINT16		db;
    /*TODO*///
    /*TODO*///	IMMBYTE(pb);
    /*TODO*///
    /*TODO*///	DIRBYTE(db);
    /*TODO*///
    /*TODO*///	if ( ( *(regTable[decodePB_tReg(pb)]) & bitTable[decodePB_dst(pb)] ) || ( db & bitTable[decodePB_src(pb)] ))
    /*TODO*///		*(regTable[decodePB_tReg(pb)]) |= bitTable[decodePB_dst(pb)];
    /*TODO*///	else
    /*TODO*///		*(regTable[decodePB_tReg(pb)]) &= (~bitTable[decodePB_dst(pb)]);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1133 BIOR */
    /*TODO*///
    /*TODO*///INLINE void bior( void )
    /*TODO*///{
    /*TODO*///	UINT8		pb;
    /*TODO*///	UINT16		db;
    /*TODO*///
    /*TODO*///	IMMBYTE(pb);
    /*TODO*///
    /*TODO*///	DIRBYTE(db);
    /*TODO*///
    /*TODO*///	if ( ( *(regTable[decodePB_tReg(pb)]) & bitTable[decodePB_dst(pb)] ) || ( (~db) & bitTable[decodePB_src(pb)] ))
    /*TODO*///		*(regTable[decodePB_tReg(pb)]) |= bitTable[decodePB_dst(pb)];
    /*TODO*///	else
    /*TODO*///		*(regTable[decodePB_tReg(pb)]) &= (~bitTable[decodePB_dst(pb)]);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1134 BEOR */
    /*TODO*///
    /*TODO*///INLINE void beor( void )
    /*TODO*///{
    /*TODO*///	UINT8		pb;
    /*TODO*///	UINT16		db;
    /*TODO*///	UINT8		tReg, tMem;
    /*TODO*///
    /*TODO*///	IMMBYTE(pb);
    /*TODO*///
    /*TODO*///	DIRBYTE(db);
    /*TODO*///
    /*TODO*///	tReg = *(regTable[decodePB_tReg(pb)]) & bitTable[decodePB_dst(pb)];
    /*TODO*///	tMem = db & bitTable[decodePB_src(pb)];
    /*TODO*///
    /*TODO*///	if ( (tReg || tMem ) && !(tReg && tMem) )
    /*TODO*///		*(regTable[decodePB_tReg(pb)]) |= bitTable[decodePB_dst(pb)];
    /*TODO*///	else
    /*TODO*///		*(regTable[decodePB_tReg(pb)]) &= (~bitTable[decodePB_dst(pb)]);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1135 BIEOR */
    /*TODO*///
    /*TODO*///INLINE void bieor( void )
    /*TODO*///{
    /*TODO*///	UINT8		pb;
    /*TODO*///	UINT16		db;
    /*TODO*///	UINT8		tReg, tMem;
    /*TODO*///
    /*TODO*///	IMMBYTE(pb);
    /*TODO*///
    /*TODO*///	DIRBYTE(db);
    /*TODO*///
    /*TODO*///	tReg = *(regTable[decodePB_tReg(pb)]) & bitTable[decodePB_dst(pb)];
    /*TODO*///	tMem = (~db) & bitTable[decodePB_src(pb)];
    /*TODO*///
    /*TODO*///	if ( (tReg || tMem ) && !(tReg && tMem) )
    /*TODO*///		*(regTable[decodePB_tReg(pb)]) |= bitTable[decodePB_dst(pb)];
    /*TODO*///	else
    /*TODO*///		*(regTable[decodePB_tReg(pb)]) &= (~bitTable[decodePB_dst(pb)]);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1133 LDBT */
    /*TODO*///
    /*TODO*///INLINE void ldbt( void )
    /*TODO*///{
    /*TODO*///	UINT8		pb;
    /*TODO*///	UINT16		db;
    /*TODO*///
    /*TODO*///	IMMBYTE(pb);
    /*TODO*///
    /*TODO*///	DIRBYTE(db);
    /*TODO*///
    /*TODO*///	if ( ( db & bitTable[decodePB_src(pb)] ) )
    /*TODO*///		*(regTable[decodePB_tReg(pb)]) |= bitTable[decodePB_dst(pb)];
    /*TODO*///	else
    /*TODO*///		*(regTable[decodePB_tReg(pb)]) &= (~bitTable[decodePB_dst(pb)]);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1134 STBT */
    /*TODO*///
    /*TODO*///INLINE void stbt( void )
    /*TODO*///{
    /*TODO*///	UINT8		pb;
    /*TODO*///	UINT16		db;
    /*TODO*///
    /*TODO*///	IMMBYTE(pb);
    /*TODO*///
    /*TODO*///	DIRBYTE(db);
    /*TODO*///
    /*TODO*///	if ( ( *(regTable[decodePB_tReg(pb)]) & bitTable[decodePB_dst(pb)] ) )
    /*TODO*///		WM( EAD, db | bitTable[decodePB_src(pb)] );
    /*TODO*///	else
    /*TODO*///		WM( EAD, db & (~bitTable[decodePB_src(pb)]) );
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $103F SWI2 absolute indirect ----- */
    /*TODO*///INLINE void swi2( void )
    /*TODO*///{
    /*TODO*///	CC |= CC_E; 			/* HJB 980225: save entire state */
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PUSHWORD(pU);
    /*TODO*///	PUSHWORD(pY);
    /*TODO*///	PUSHWORD(pX);
    /*TODO*///	PUSHBYTE(DP);
    /*TODO*///	if ((MD & MD_EM) != 0)
    /*TODO*///	{
    /*TODO*///		PUSHBYTE(F);
    /*TODO*///		PUSHBYTE(E);
    /*TODO*///		hd6309_ICount -= 2;
    /*TODO*///	}
    /*TODO*///	PUSHBYTE(B);
    /*TODO*///	PUSHBYTE(A);
    /*TODO*///	PUSHBYTE(CC);
    /*TODO*///	PCD = RM16(0xfff4);
    /*TODO*///	CHANGE_PC;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $113F SWI3 absolute indirect ----- */
    /*TODO*///INLINE void swi3( void )
    /*TODO*///{
    /*TODO*///	CC |= CC_E; 			/* HJB 980225: save entire state */
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PUSHWORD(pU);
    /*TODO*///	PUSHWORD(pY);
    /*TODO*///	PUSHWORD(pX);
    /*TODO*///	PUSHBYTE(DP);
    /*TODO*///	if ((MD & MD_EM) != 0)
    /*TODO*///	{
    /*TODO*///		PUSHBYTE(F);
    /*TODO*///		PUSHBYTE(E);
    /*TODO*///		hd6309_ICount -= 2;
    /*TODO*///	}
    /*TODO*///	PUSHBYTE(B);
    /*TODO*///	PUSHBYTE(A);
    /*TODO*///	PUSHBYTE(CC);
    /*TODO*///	PCD = RM16(0xfff2);
    /*TODO*///	CHANGE_PC;
    /*TODO*///}
    /*TODO*///
    /*TODO*///#ifdef macintosh
    /*TODO*///#pragma mark ____4x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $40 NEGA inherent ?**** */
    /*TODO*///INLINE void nega( void )
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	r = -A;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(0,A,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $41 ILLEGAL */
    /*TODO*///
    /*TODO*////* $42 ILLEGAL */
    /*TODO*///
    /*TODO*////* $43 COMA inherent -**01 */
    /*TODO*///INLINE void coma( void )
    /*TODO*///{
    /*TODO*///	A = ~A;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///	SEC;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $44 LSRA inherent -0*-* */
    /*TODO*///INLINE void lsra( void )
    /*TODO*///{
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (A & CC_C);
    /*TODO*///	A >>= 1;
    /*TODO*///	SET_Z8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $45 ILLEGAL */
    /*TODO*///
    /*TODO*////* $46 RORA inherent -**-* */
    /*TODO*///INLINE void rora( void )
    /*TODO*///{
    /*TODO*///	UINT8 r;
    /*TODO*///	r = (CC & CC_C) << 7;
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (A & CC_C);
    /*TODO*///	r |= A >> 1;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $47 ASRA inherent ?**-* */
    /*TODO*///INLINE void asra( void )
    /*TODO*///{
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (A & CC_C);
    /*TODO*///	A = (A & 0x80) | (A >> 1);
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $48 ASLA inherent ?**** */
    /*TODO*///INLINE void asla( void )
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	r = A << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,A,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $49 ROLA inherent -**** */
    /*TODO*///INLINE void rola( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	t = A;
    /*TODO*///	r = (CC & CC_C) | (t<<1);
    /*TODO*///	CLR_NZVC; SET_FLAGS8(t,t,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $4A DECA inherent -***- */
    /*TODO*///INLINE void deca( void )
    /*TODO*///{
    /*TODO*///	--A;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS8D(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $4B ILLEGAL */
    /*TODO*///
    /*TODO*////* $4C INCA inherent -***- */
    /*TODO*///INLINE void inca( void )
    /*TODO*///{
    /*TODO*///	++A;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS8I(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $4D TSTA inherent -**0- */
    /*TODO*///INLINE void tsta( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $4E ILLEGAL */
    /*TODO*///
    /*TODO*////* $4F CLRA inherent -0100 */
    /*TODO*///INLINE void clra( void )
    /*TODO*///{
    /*TODO*///	A = 0;
    /*TODO*///	CLR_NZVC; SEZ;
    /*TODO*///}
    /*TODO*///
    /*TODO*///#ifdef macintosh
    /*TODO*///#pragma mark ____5x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $50 NEGB inherent ?**** */
    /*TODO*///INLINE void negb( void )
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	r = -B;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(0,B,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1040 NEGD inherent ?**** */
    /*TODO*///INLINE void negd( void )
    /*TODO*///{
    /*TODO*///	UINT32 r;
    /*TODO*///	r = -D;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(0,D,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $51 ILLEGAL */
    /*TODO*///
    /*TODO*////* $52 ILLEGAL */
    /*TODO*///
    /*TODO*////* $53 COMB inherent -**01 */
    /*TODO*///INLINE void comb( void )
    /*TODO*///{
    /*TODO*///	B = ~B;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///	SEC;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1143 COME inherent -**01 */
    /*TODO*///INLINE void come( void )
    /*TODO*///{
    /*TODO*///	E = ~E;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(E);
    /*TODO*///	SEC;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1153 COMF inherent -**01 */
    /*TODO*///INLINE void comf( void )
    /*TODO*///{
    /*TODO*///	F = ~F;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(F);
    /*TODO*///	SEC;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1043 COMD inherent -**01 */
    /*TODO*///INLINE void comd( void )
    /*TODO*///{
    /*TODO*///	D = ~D;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///	SEC;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1053 COMW inherent -**01 */
    /*TODO*///INLINE void comw( void )
    /*TODO*///{
    /*TODO*///	W = ~W;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(W);
    /*TODO*///	SEC;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $54 LSRB inherent -0*-* */
    /*TODO*///INLINE void lsrb( void )
    /*TODO*///{
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (B & CC_C);
    /*TODO*///	B >>= 1;
    /*TODO*///	SET_Z8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1044 LSRD inherent -0*-* */
    /*TODO*///INLINE void lsrd( void )
    /*TODO*///{
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (B & CC_C);
    /*TODO*///	D >>= 1;
    /*TODO*///	SET_Z16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1054 LSRW inherent -0*-* */
    /*TODO*///INLINE void lsrw( void )
    /*TODO*///{
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (F & CC_C);
    /*TODO*///	W >>= 1;
    /*TODO*///	SET_Z16(W);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $55 ILLEGAL */
    /*TODO*///
    /*TODO*////* $56 RORB inherent -**-* */
    /*TODO*///INLINE void rorb( void )
    /*TODO*///{
    /*TODO*///	UINT8 r;
    /*TODO*///	r = (CC & CC_C) << 7;
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (B & CC_C);
    /*TODO*///	r |= B >> 1;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1046 RORD inherent -**-* */
    /*TODO*///INLINE void rord( void )
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	r = (CC & CC_C) << 15;
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (D & CC_C);
    /*TODO*///	r |= D >> 1;
    /*TODO*///	SET_NZ16(r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1056 RORW inherent -**-* */
    /*TODO*///INLINE void rorw( void )
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	r = (CC & CC_C) << 15;
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (W & CC_C);
    /*TODO*///	r |= W >> 1;
    /*TODO*///	SET_NZ16(r);
    /*TODO*///	W = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $57 ASRB inherent ?**-* */
    /*TODO*///INLINE void asrb( void )
    /*TODO*///{
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (B & CC_C);
    /*TODO*///	B= (B & 0x80) | (B >> 1);
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1047 ASRD inherent ?**-* */
    /*TODO*///INLINE void asrd( void )
    /*TODO*///{
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (D & CC_C);
    /*TODO*///	D= (D & 0x8000) | (D >> 1);
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $58 ASLB inherent ?**** */
    /*TODO*///INLINE void aslb( void )
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	r = B << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,B,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1048 ASLD inherent ?**** */
    /*TODO*///INLINE void asld( void )
    /*TODO*///{
    /*TODO*///	UINT32 r;
    /*TODO*///	r = D << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(D,D,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $59 ROLB inherent -**** */
    /*TODO*///INLINE void rolb( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	t = B;
    /*TODO*///	r = CC & CC_C;
    /*TODO*///	r |= t << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(t,t,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1049 ROLD inherent -**** */
    /*TODO*///INLINE void rold( void )
    /*TODO*///{
    /*TODO*///	UINT32 t,r;
    /*TODO*///	t = D;
    /*TODO*///	r = CC & CC_C;
    /*TODO*///	r |= t << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(t,t,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1059 ROLW inherent -**** */
    /*TODO*///INLINE void rolw( void )
    /*TODO*///{
    /*TODO*///	UINT32 t,r;
    /*TODO*///	t = W;
    /*TODO*///	r = CC & CC_C;
    /*TODO*///	r |= t << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(t,t,r);
    /*TODO*///	W = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $5A DECB inherent -***- */
    /*TODO*///INLINE void decb( void )
    /*TODO*///{
    /*TODO*///	--B;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS8D(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $114a DECE inherent -***- */
    /*TODO*///INLINE void dece( void )
    /*TODO*///{
    /*TODO*///	--E;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS8D(E);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $115a DECF inherent -***- */
    /*TODO*///INLINE void decf( void )
    /*TODO*///{
    /*TODO*///	--F;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS8D(F);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $104a DECD inherent -***- */
    /*TODO*///INLINE void decd( void )
    /*TODO*///{
    /*TODO*///	UINT32 r;
    /*TODO*///	r = D - 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(D,D,r)
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $105a DECW inherent -***- */
    /*TODO*///INLINE void decw( void )
    /*TODO*///{
    /*TODO*///	UINT32 r;
    /*TODO*///	r = W - 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(W,W,r)
    /*TODO*///	W = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $5B ILLEGAL */
    /*TODO*///
    /*TODO*////* $5C INCB inherent -***- */
    /*TODO*///INLINE void incb( void )
    /*TODO*///{
    /*TODO*///	++B;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS8I(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $114c INCE inherent -***- */
    /*TODO*///INLINE void ince( void )
    /*TODO*///{
    /*TODO*///	++E;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS8I(E);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $115c INCF inherent -***- */
    /*TODO*///INLINE void incf( void )
    /*TODO*///{
    /*TODO*///	++F;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS8I(F);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $104c INCD inherent -***- */
    /*TODO*///INLINE void incd( void )
    /*TODO*///{
    /*TODO*///	UINT32 r;
    /*TODO*///	r = D + 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(D,D,r)
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $105c INCW inherent -***- */
    /*TODO*///INLINE void incw( void )
    /*TODO*///{
    /*TODO*///	UINT32 r;
    /*TODO*///	r = W + 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(W,W,r)
    /*TODO*///	W = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $5D TSTB inherent -**0- */
    /*TODO*///INLINE void tstb( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $104d TSTD inherent -**0- */
    /*TODO*///INLINE void tstd( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $105d TSTW inherent -**0- */
    /*TODO*///INLINE void tstw( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(W);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $114d TSTE inherent -**0- */
    /*TODO*///INLINE void tste( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(E);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $115d TSTF inherent -**0- */
    /*TODO*///INLINE void tstf( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(F);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $5E ILLEGAL */
    /*TODO*///
    /*TODO*////* $5F CLRB inherent -0100 */
    /*TODO*///INLINE void clrb( void )
    /*TODO*///{
    /*TODO*///	B = 0;
    /*TODO*///	CLR_NZVC; SEZ;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $104f CLRD inherent -0100 */
    /*TODO*///INLINE void clrd( void )
    /*TODO*///{
    /*TODO*///	D = 0;
    /*TODO*///	CLR_NZVC; SEZ;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $114f CLRE inherent -0100 */
    /*TODO*///INLINE void clre( void )
    /*TODO*///{
    /*TODO*///	E = 0;
    /*TODO*///	CLR_NZVC; SEZ;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $115f CLRF inherent -0100 */
    /*TODO*///INLINE void clrf( void )
    /*TODO*///{
    /*TODO*///	F = 0;
    /*TODO*///	CLR_NZVC; SEZ;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $105f CLRW inherent -0100 */
    /*TODO*///INLINE void clrw( void )
    /*TODO*///{
    /*TODO*///	W = 0;
    /*TODO*///	CLR_NZVC; SEZ;
    /*TODO*///}
    /*TODO*///
    /*TODO*///#ifdef macintosh
    /*TODO*///#pragma mark ____6x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $60 NEG indexed ?**** */
    /*TODO*///INLINE void neg_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16 r,t;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r=-t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(0,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $61 OIM indexed */
    /*TODO*///INLINE void oim_ix( void )
    /*TODO*///{
    /*TODO*///	UINT8	r,im;
    /*TODO*///	IMMBYTE(im);
    /*TODO*///	fetch_effective_address();
    /*TODO*///	r = im | RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $62 AIM indexed */
    /*TODO*///INLINE void aim_ix( void )
    /*TODO*///{
    /*TODO*///	UINT8	r,im;
    /*TODO*///	IMMBYTE(im);
    /*TODO*///	fetch_effective_address();
    /*TODO*///	r = im & RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $63 COM indexed -**01 */
    /*TODO*///INLINE void com_ix( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = ~RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(t);
    /*TODO*///	SEC;
    /*TODO*///	WM(EAD,t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $64 LSR indexed -0*-* */
    /*TODO*///INLINE void lsr_ix( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t=RM(EAD);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t & CC_C);
    /*TODO*///	t>>=1; SET_Z8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $65 EIM indexed */
    /*TODO*///INLINE void eim_ix( void )
    /*TODO*///{
    /*TODO*///	UINT8	r,im;
    /*TODO*///	IMMBYTE(im);
    /*TODO*///	fetch_effective_address();
    /*TODO*///	r = im ^ RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*////* $66 ROR indexed -**-* */
    /*TODO*///INLINE void ror_ix( void )
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t=RM(EAD);
    /*TODO*///	r = (CC & CC_C) << 7;
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t & CC_C);
    /*TODO*///	r |= t>>1; SET_NZ8(r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $67 ASR indexed ?**-* */
    /*TODO*///INLINE void asr_ix( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t=RM(EAD);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t & CC_C);
    /*TODO*///	t=(t&0x80)|(t>>1);
    /*TODO*///	SET_NZ8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $68 ASL indexed ?**** */
    /*TODO*///INLINE void asl_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t=RM(EAD);
    /*TODO*///	r = t << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $69 ROL indexed -**** */
    /*TODO*///INLINE void rol_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t=RM(EAD);
    /*TODO*///	r = CC & CC_C;
    /*TODO*///	r |= t << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $6A DEC indexed -***- */
    /*TODO*///INLINE void dec_ix( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD) - 1;
    /*TODO*///	CLR_NZV; SET_FLAGS8D(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $6B TIM indexed */
    /*TODO*///INLINE void tim_ix( void )
    /*TODO*///{
    /*TODO*///	UINT8	r,im,m;
    /*TODO*///	IMMBYTE(im);
    /*TODO*///	fetch_effective_address();
    /*TODO*///	m = RM(EAD);
    /*TODO*///	r = im & m;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $6C INC indexed -***- */
    /*TODO*///INLINE void inc_ix( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD) + 1;
    /*TODO*///	CLR_NZV; SET_FLAGS8I(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $6D TST indexed -**0- */
    /*TODO*///INLINE void tst_ix( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $6E JMP indexed ----- */
    /*TODO*///INLINE void jmp_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	PCD = EAD;
    /*TODO*///	CHANGE_PC;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $6F CLR indexed -0100 */
    /*TODO*///INLINE void clr_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	WM(EAD,0);
    /*TODO*///	CLR_NZVC; SEZ;
    /*TODO*///}
    /*TODO*///
    /*TODO*///#ifdef macintosh
    /*TODO*///#pragma mark ____7x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $70 NEG extended ?**** */
    /*TODO*///INLINE void neg_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16 r,t;
    /*TODO*///	EXTBYTE(t); r=-t;
    /*TODO*///	CLR_NZVC; SET_FLAGS8(0,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $71 OIM extended */
    /*TODO*///INLINE void oim_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8	r,t,im;
    /*TODO*///	IMMBYTE(im);
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = im | t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $72 AIM extended */
    /*TODO*///INLINE void aim_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8	r,t,im;
    /*TODO*///	IMMBYTE(im);
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = im & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $73 COM extended -**01 */
    /*TODO*///INLINE void com_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t); t = ~t;
    /*TODO*///	CLR_NZV; SET_NZ8(t); SEC;
    /*TODO*///	WM(EAD,t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $74 LSR extended -0*-* */
    /*TODO*///INLINE void lsr_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t); CLR_NZC; CC |= (t & CC_C);
    /*TODO*///	t>>=1; SET_Z8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $75 EIM extended */
    /*TODO*///INLINE void eim_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8	r,t,im;
    /*TODO*///	IMMBYTE(im);
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = im ^ t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $76 ROR extended -**-* */
    /*TODO*///INLINE void ror_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	EXTBYTE(t); r=(CC & CC_C) << 7;
    /*TODO*///	CLR_NZC; CC |= (t & CC_C);
    /*TODO*///	r |= t>>1; SET_NZ8(r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $77 ASR extended ?**-* */
    /*TODO*///INLINE void asr_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t); CLR_NZC; CC |= (t & CC_C);
    /*TODO*///	t=(t&0x80)|(t>>1);
    /*TODO*///	SET_NZ8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $78 ASL extended ?**** */
    /*TODO*///INLINE void asl_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t); r=t<<1;
    /*TODO*///	CLR_NZVC; SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $79 ROL extended -**** */
    /*TODO*///INLINE void rol_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t); r = (CC & CC_C) | (t << 1);
    /*TODO*///	CLR_NZVC; SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $7A DEC extended -***- */
    /*TODO*///INLINE void dec_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t); --t;
    /*TODO*///	CLR_NZV; SET_FLAGS8D(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $7B TIM extended */
    /*TODO*///INLINE void tim_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8	r,t,im;
    /*TODO*///	IMMBYTE(im);
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = im & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $7C INC extended -***- */
    /*TODO*///INLINE void inc_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t); ++t;
    /*TODO*///	CLR_NZV; SET_FLAGS8I(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $7D TST extended -**0- */
    /*TODO*///INLINE void tst_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t); CLR_NZV; SET_NZ8(t);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $7E JMP extended ----- */
    /*TODO*///INLINE void jmp_ex( void )
    /*TODO*///{
    /*TODO*///	EXTENDED;
    /*TODO*///	PCD = EAD;
    /*TODO*///	CHANGE_PC;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $7F CLR extended -0100 */
    /*TODO*///INLINE void clr_ex( void )
    /*TODO*///{
    /*TODO*///	EXTENDED;
    /*TODO*///	WM(EAD,0);
    /*TODO*///	CLR_NZVC; SEZ;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///#ifdef macintosh
    /*TODO*///#pragma mark ____8x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $80 SUBA immediate ?**** */
    /*TODO*///INLINE void suba_im( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $81 CMPA immediate ?**** */
    /*TODO*///INLINE void cmpa_im( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $82 SBCA immediate ?**** */
    /*TODO*///INLINE void sbca_im( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = A - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $83 SUBD (CMPD CMPU) immediate -**** */
    /*TODO*///INLINE void subd_im( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1080 SUBW immediate -**** */
    /*TODO*///INLINE void subw_im( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = W;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	W = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1083 CMPD immediate -**** */
    /*TODO*///INLINE void cmpd_im( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1081 CMPW immediate -**** */
    /*TODO*///INLINE void cmpw_im( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = W;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1183 CMPU immediate -**** */
    /*TODO*///INLINE void cmpu_im( void )
    /*TODO*///{
    /*TODO*///	UINT32 r, d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = U;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $84 ANDA immediate -**0- */
    /*TODO*///INLINE void anda_im( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	A &= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $85 BITA immediate -**0- */
    /*TODO*///INLINE void bita_im( void )
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = A & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $86 LDA immediate -**0- */
    /*TODO*///INLINE void lda_im( void )
    /*TODO*///{
    /*TODO*///	IMMBYTE(A);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $88 EORA immediate -**0- */
    /*TODO*///INLINE void eora_im( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	A ^= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $89 ADCA immediate ***** */
    /*TODO*///INLINE void adca_im( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = A + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $8A ORA immediate -**0- */
    /*TODO*///INLINE void ora_im( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	A |= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $8B ADDA immediate ***** */
    /*TODO*///INLINE void adda_im( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = A + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $8C CMPX (CMPY CMPS) immediate -**** */
    /*TODO*///INLINE void cmpx_im( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = X;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $108C CMPY immediate -**** */
    /*TODO*///INLINE void cmpy_im( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = Y;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $118C CMPS immediate -**** */
    /*TODO*///INLINE void cmps_im( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = S;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $8D BSR ----- */
    /*TODO*///INLINE void bsr( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PC += SIGNED(t);
    /*TODO*///	CHANGE_PC;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $8E LDX (LDY) immediate -**0- */
    /*TODO*///INLINE void ldx_im( void )
    /*TODO*///{
    /*TODO*///	IMMWORD(pX);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $CD LDQ immediate -**0- */
    /*TODO*///INLINE void ldq_im( void )
    /*TODO*///{
    /*TODO*///	PAIR	q;
    /*TODO*///
    /*TODO*///	IMMLONG(q);
    /*TODO*///	D = q.w.h;
    /*TODO*///	W = q.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_N8(A);
    /*TODO*///	SET_Z(q.d);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $108E LDY immediate -**0- */
    /*TODO*///INLINE void ldy_im( void )
    /*TODO*///{
    /*TODO*///	IMMWORD(pY);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(Y);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $118f MULD immediate */
    /*TODO*///INLINE void muld_im( void )
    /*TODO*///{
    /*TODO*///	PAIR t, q;
    /*TODO*///
    /*TODO*///	IMMWORD( t );
    /*TODO*///	q.d = (INT16) D * (INT16)t.w.l;
    /*TODO*///	D = q.w.h;
    /*TODO*///	W = q.w.l;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $118d DIVD immediate */
    /*TODO*///INLINE void divd_im( void )
    /*TODO*///{
    /*TODO*///	UINT8   t;
    /*TODO*///	INT16   v;
    /*TODO*///
    /*TODO*///	IMMBYTE( t );
    /*TODO*///
    /*TODO*///	if( t != 0 )
    /*TODO*///	{
    /*TODO*///		v = (INT16) D / (INT8) t;
    /*TODO*///		A = (INT16) D % (INT8) t;
    /*TODO*///		B = v;
    /*TODO*///
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_NZ8(B);
    /*TODO*///
    /*TODO*///		if ((B & 0x01) != 0)
    /*TODO*///			SEC;
    /*TODO*///
    /*TODO*///		if ( (v > 127) || (v < -128) )
    /*TODO*///			SEV;
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		hd6309_ICount -= 8;
    /*TODO*///		DZError();
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $118e DIVQ immediate */
    /*TODO*///INLINE void divq_im( void )
    /*TODO*///{
    /*TODO*///	PAIR	t,q;
    /*TODO*///	INT32	v;
    /*TODO*///
    /*TODO*///	IMMWORD( t );
    /*TODO*///	q.w.h = D;
    /*TODO*///	q.w.l = W;
    /*TODO*///
    /*TODO*///	if( t.w.l != 0 )
    /*TODO*///	{
    /*TODO*///		v = (INT32) q.d / (INT16) t.w.l;
    /*TODO*///		D = (INT32) q.d % (INT16) t.w.l;
    /*TODO*///		W = v;
    /*TODO*///
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_NZ16(W);
    /*TODO*///
    /*TODO*///		if ((W & 0x0001) != 0)
    /*TODO*///			SEC;
    /*TODO*///
    /*TODO*///		if ( (v > 65534) || (v < -65535) )
    /*TODO*///			SEV;
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///		DZError();
    /*TODO*///}
    /*TODO*///
    /*TODO*///#ifdef macintosh
    /*TODO*///#pragma mark ____9x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $90 SUBA direct ?**** */
    /*TODO*///INLINE void suba_di( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $91 CMPA direct ?**** */
    /*TODO*///INLINE void cmpa_di( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $92 SBCA direct ?**** */
    /*TODO*///INLINE void sbca_di( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = A - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $93 SUBD (CMPD CMPU) direct -**** */
    /*TODO*///INLINE void subd_di( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1090 SUBW direct -**** */
    /*TODO*///INLINE void subw_di( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = W;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	W = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1093 CMPD direct -**** */
    /*TODO*///INLINE void cmpd_di( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1091 CMPW direct -**** */
    /*TODO*///INLINE void cmpw_di( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = W;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1193 CMPU direct -**** */
    /*TODO*///INLINE void cmpu_di( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = U;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(U,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $94 ANDA direct -**0- */
    /*TODO*///INLINE void anda_di( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	A &= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $95 BITA direct -**0- */
    /*TODO*///INLINE void bita_di( void )
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = A & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $96 LDA direct -**0- */
    /*TODO*///INLINE void lda_di( void )
    /*TODO*///{
    /*TODO*///	DIRBYTE(A);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $113d LDMD direct -**0- */
    /*TODO*///INLINE void ldmd_di( void )
    /*TODO*///{
    /*TODO*///	DIRBYTE(MD);
    /*TODO*///	UpdateState();
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $97 STA direct -**0- */
    /*TODO*///INLINE void sta_di( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///	DIRECT;
    /*TODO*///	WM(EAD,A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $98 EORA direct -**0- */
    /*TODO*///INLINE void eora_di( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	A ^= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $99 ADCA direct ***** */
    /*TODO*///INLINE void adca_di( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = A + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $9A ORA direct -**0- */
    /*TODO*///INLINE void ora_di( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	A |= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $9B ADDA direct ***** */
    /*TODO*///INLINE void adda_di( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = A + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $9C CMPX (CMPY CMPS) direct -**** */
    /*TODO*///INLINE void cmpx_di( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = X;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $109C CMPY direct -**** */
    /*TODO*///INLINE void cmpy_di( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = Y;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $119C CMPS direct -**** */
    /*TODO*///INLINE void cmps_di( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = S;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $9D JSR direct ----- */
    /*TODO*///INLINE void jsr_di( void )
    /*TODO*///{
    /*TODO*///	DIRECT;
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PCD = EAD;
    /*TODO*///	CHANGE_PC;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $9E LDX (LDY) direct -**0- */
    /*TODO*///INLINE void ldx_di( void )
    /*TODO*///{
    /*TODO*///	DIRWORD(pX);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $119f MULD direct -**0- */
    /*TODO*///INLINE void muld_di( void )
    /*TODO*///{
    /*TODO*///	PAIR	t,q;
    /*TODO*///
    /*TODO*///	DIRWORD(t);
    /*TODO*///	q.d = (INT16) D * (INT16)t.w.l;
    /*TODO*///
    /*TODO*///	D = q.w.h;
    /*TODO*///	W = q.w.l;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $119d DIVD direct -**0- */
    /*TODO*///INLINE void divd_di( void )
    /*TODO*///{
    /*TODO*///	UINT8	t;
    /*TODO*///	INT16   v;
    /*TODO*///
    /*TODO*///	DIRBYTE(t);
    /*TODO*///
    /*TODO*///	if( t != 0 )
    /*TODO*///	{
    /*TODO*///		v = (INT16) D / (INT8) t;
    /*TODO*///		A = (INT16) D % (INT8) t;
    /*TODO*///		B = v;
    /*TODO*///
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_NZ8(B);
    /*TODO*///
    /*TODO*///		if ((B & 0x01) != 0)
    /*TODO*///			SEC;
    /*TODO*///
    /*TODO*///		if ( (v > 127) || (v < -128) )
    /*TODO*///			SEV;
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		hd6309_ICount -= 8;
    /*TODO*///		DZError();
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $119e DIVQ direct -**0- */
    /*TODO*///INLINE void divq_di( void )
    /*TODO*///{
    /*TODO*///	PAIR	t, q;
    /*TODO*///	INT32	v;
    /*TODO*///
    /*TODO*///	q.w.h = D;
    /*TODO*///	q.w.l = W;
    /*TODO*///
    /*TODO*///	DIRWORD(t);
    /*TODO*///
    /*TODO*///	if( t.w.l != 0 )
    /*TODO*///	{
    /*TODO*///		v = (INT32) q.d / (INT16) t.w.l;
    /*TODO*///		D = (INT32) q.d % (INT16) t.w.l;
    /*TODO*///		W = v;
    /*TODO*///
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_NZ16(W);
    /*TODO*///
    /*TODO*///		if ((W & 0x0001) != 0)
    /*TODO*///			SEC;
    /*TODO*///
    /*TODO*///		if ( (v > 65534) || (v < -65535) )
    /*TODO*///			SEV;
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///		DZError();
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10dc LDQ direct -**0- */
    /*TODO*///INLINE void ldq_di( void )
    /*TODO*///{
    /*TODO*///	PAIR	q;
    /*TODO*///
    /*TODO*///	DIRLONG(q);
    /*TODO*///	D = q.w.h;
    /*TODO*///	W = q.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_N8(A);
    /*TODO*///	SET_Z(q.d);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $109E LDY direct -**0- */
    /*TODO*///INLINE void ldy_di( void )
    /*TODO*///{
    /*TODO*///	DIRWORD(pY);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(Y);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $9F STX (STY) direct -**0- */
    /*TODO*///INLINE void stx_di( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);
    /*TODO*///	DIRECT;
    /*TODO*///	WM16(EAD,&pX);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10dd STQ direct -**0- */
    /*TODO*///INLINE void stq_di( void )
    /*TODO*///{
    /*TODO*///	PAIR	q;
    /*TODO*///
    /*TODO*///	q.w.h = D;
    /*TODO*///	q.w.l = W;
    /*TODO*///	DIRECT;
    /*TODO*///	WM32(EAD,&q);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_N8(A);
    /*TODO*///	SET_Z(q.d);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $109F STY direct -**0- */
    /*TODO*///INLINE void sty_di( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(Y);
    /*TODO*///	DIRECT;
    /*TODO*///	WM16(EAD,&pY);
    /*TODO*///}
    /*TODO*///
    /*TODO*///#ifdef macintosh
    /*TODO*///#pragma mark ____Ax____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///
    /*TODO*////* $a0 SUBA indexed ?**** */
    /*TODO*///INLINE void suba_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $a1 CMPA indexed ?**** */
    /*TODO*///INLINE void cmpa_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $a2 SBCA indexed ?**** */
    /*TODO*///INLINE void sbca_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = A - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $a3 SUBD (CMPD CMPU) indexed -**** */
    /*TODO*///INLINE void subd_ix( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	d = D;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10a0 SUBW indexed -**** */
    /*TODO*///INLINE void subw_ix( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	d = W;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	W = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10a3 CMPD indexed -**** */
    /*TODO*///INLINE void cmpd_ix( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	d = D;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10a1 CMPW indexed -**** */
    /*TODO*///INLINE void cmpw_ix( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	d = W;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11a3 CMPU indexed -**** */
    /*TODO*///INLINE void cmpu_ix( void )
    /*TODO*///{
    /*TODO*///	UINT32 r;
    /*TODO*///	PAIR b;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	r = U - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(U,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $a4 ANDA indexed -**0- */
    /*TODO*///INLINE void anda_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	A &= RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $a5 BITA indexed -**0- */
    /*TODO*///INLINE void bita_ix( void )
    /*TODO*///{
    /*TODO*///	UINT8 r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	r = A & RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $a6 LDA indexed -**0- */
    /*TODO*///INLINE void lda_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	A = RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $a7 STA indexed -**0- */
    /*TODO*///INLINE void sta_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///	WM(EAD,A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $a8 EORA indexed -**0- */
    /*TODO*///INLINE void eora_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	A ^= RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $a9 ADCA indexed ***** */
    /*TODO*///INLINE void adca_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = A + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $aA ORA indexed -**0- */
    /*TODO*///INLINE void ora_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	A |= RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $aB ADDA indexed ***** */
    /*TODO*///INLINE void adda_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = A + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $aC CMPX (CMPY CMPS) indexed -**** */
    /*TODO*///INLINE void cmpx_ix( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	d = X;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10aC CMPY indexed -**** */
    /*TODO*///INLINE void cmpy_ix( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	d = Y;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11aC CMPS indexed -**** */
    /*TODO*///INLINE void cmps_ix( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	d = S;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $aD JSR indexed ----- */
    /*TODO*///INLINE void jsr_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PCD = EAD;
    /*TODO*///	CHANGE_PC;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $aE LDX (LDY) indexed -**0- */
    /*TODO*///INLINE void ldx_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	X=RM16(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11af MULD indexed -**0- */
    /*TODO*///INLINE void muld_ix( void )
    /*TODO*///{
    /*TODO*///	PAIR	q;
    /*TODO*///	UINT16	t;
    /*TODO*///
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t=RM16(EAD);
    /*TODO*///	q.d = (INT16) D * (INT16)t;
    /*TODO*///
    /*TODO*///	D = q.w.h;
    /*TODO*///	W = q.w.l;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11ad DIVD indexed -**0- */
    /*TODO*///INLINE void divd_ix( void )
    /*TODO*///{
    /*TODO*///	UINT8	t;
    /*TODO*///	INT16   v;
    /*TODO*///
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t=RM(EAD);
    /*TODO*///
    /*TODO*///	if( t != 0 )
    /*TODO*///	{
    /*TODO*///		v = (INT16) D / (INT8) t;
    /*TODO*///		A = (INT16) D % (INT8) t;
    /*TODO*///		B = v;
    /*TODO*///
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_NZ8(B);
    /*TODO*///
    /*TODO*///		if ((B & 0x01) != 0)
    /*TODO*///			SEC;
    /*TODO*///
    /*TODO*///		if ( (v > 127) || (v < -128) )
    /*TODO*///			SEV;
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		hd6309_ICount -= 8;
    /*TODO*///		DZError();
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11ae DIVQ indexed -**0- */
    /*TODO*///INLINE void divq_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16	t;
    /*TODO*///	INT32	v;
    /*TODO*///	PAIR	q;
    /*TODO*///
    /*TODO*///	q.w.h = D;
    /*TODO*///	q.w.l = W;
    /*TODO*///
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t=RM16(EAD);
    /*TODO*///
    /*TODO*///	if( t != 0 )
    /*TODO*///	{
    /*TODO*///		v = (INT32) q.d / (INT16) t;
    /*TODO*///		D = (INT32) q.d % (INT16) t;
    /*TODO*///		W = v;
    /*TODO*///
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_NZ16(W);
    /*TODO*///
    /*TODO*///		if ((W & 0x0001) != 0)
    /*TODO*///			SEC;
    /*TODO*///
    /*TODO*///		if ( (v > 65534) || (v < -65535) )
    /*TODO*///			SEV;
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///		DZError();
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10ec LDQ indexed -**0- */
    /*TODO*///INLINE void ldq_ix( void )
    /*TODO*///{
    /*TODO*///	PAIR	q;
    /*TODO*///
    /*TODO*///	fetch_effective_address();
    /*TODO*///	q.d=RM32(EAD);
    /*TODO*///	D = q.w.h;
    /*TODO*///	W = q.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_N8(A);
    /*TODO*///	SET_Z(q.d);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10aE LDY indexed -**0- */
    /*TODO*///INLINE void ldy_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	Y=RM16(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(Y);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $aF STX (STY) indexed -**0- */
    /*TODO*///INLINE void stx_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);
    /*TODO*///	WM16(EAD,&pX);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10ed STQ indexed -**0- */
    /*TODO*///INLINE void stq_ix( void )
    /*TODO*///{
    /*TODO*///	PAIR	q;
    /*TODO*///
    /*TODO*///	q.w.h = D;
    /*TODO*///	q.w.l = W;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	WM32(EAD,&q);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_N8(A);
    /*TODO*///	SET_Z(q.d);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10aF STY indexed -**0- */
    /*TODO*///INLINE void sty_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(Y);
    /*TODO*///	WM16(EAD,&pY);
    /*TODO*///}
    /*TODO*///
    /*TODO*///#ifdef macintosh
    /*TODO*///#pragma mark ____Bx____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $b0 SUBA extended ?**** */
    /*TODO*///INLINE void suba_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $b1 CMPA extended ?**** */
    /*TODO*///INLINE void cmpa_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $b2 SBCA extended ?**** */
    /*TODO*///INLINE void sbca_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = A - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $b3 SUBD (CMPD CMPU) extended -**** */
    /*TODO*///INLINE void subd_ex( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b = {{0,}};
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10b0 SUBW extended -**** */
    /*TODO*///INLINE void subw_ex( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b = {{0,}};
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = W;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	W = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10b3 CMPD extended -**** */
    /*TODO*///INLINE void cmpd_ex( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b = {{0,}};
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10b1 CMPW extended -**** */
    /*TODO*///INLINE void cmpw_ex( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b = {{0,}};
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = W;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11b3 CMPU extended -**** */
    /*TODO*///INLINE void cmpu_ex( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b = {{0,}};
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = U;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $b4 ANDA extended -**0- */
    /*TODO*///INLINE void anda_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	A &= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $b5 BITA extended -**0- */
    /*TODO*///INLINE void bita_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = A & t;
    /*TODO*///	CLR_NZV; SET_NZ8(r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $b6 LDA extended -**0- */
    /*TODO*///INLINE void lda_ex( void )
    /*TODO*///{
    /*TODO*///	EXTBYTE(A);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $b7 STA extended -**0- */
    /*TODO*///INLINE void sta_ex( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///	EXTENDED;
    /*TODO*///	WM(EAD,A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $b8 EORA extended -**0- */
    /*TODO*///INLINE void eora_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	A ^= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $b9 ADCA extended ***** */
    /*TODO*///INLINE void adca_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = A + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $bA ORA extended -**0- */
    /*TODO*///INLINE void ora_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	A |= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $bB ADDA extended ***** */
    /*TODO*///INLINE void adda_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = A + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $bC CMPX (CMPY CMPS) extended -**** */
    /*TODO*///INLINE void cmpx_ex( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b = {{0,}};
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = X;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10bC CMPY extended -**** */
    /*TODO*///INLINE void cmpy_ex( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b = {{0,}};
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = Y;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11bC CMPS extended -**** */
    /*TODO*///INLINE void cmps_ex( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b = {{0,}};
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = S;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $bD JSR extended ----- */
    /*TODO*///INLINE void jsr_ex( void )
    /*TODO*///{
    /*TODO*///	EXTENDED;
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PCD = EAD;
    /*TODO*///	CHANGE_PC;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $bE LDX (LDY) extended -**0- */
    /*TODO*///INLINE void ldx_ex( void )
    /*TODO*///{
    /*TODO*///	EXTWORD(pX);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11bf MULD extended -**0- */
    /*TODO*///INLINE void muld_ex( void )
    /*TODO*///{
    /*TODO*///	PAIR	t, q;
    /*TODO*///
    /*TODO*///	EXTWORD(t);
    /*TODO*///	q.d = (INT16) D * (INT16)t.w.l;
    /*TODO*///
    /*TODO*///	D = q.w.h;
    /*TODO*///	W = q.w.l;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11bd DIVD extended -**0- */
    /*TODO*///INLINE void divd_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8	t;
    /*TODO*///	INT16   v;
    /*TODO*///
    /*TODO*///	EXTBYTE(t);
    /*TODO*///
    /*TODO*///	if( t != 0 )
    /*TODO*///	{
    /*TODO*///		v = (INT16) D / (INT8) t;
    /*TODO*///		A = (INT16) D % (INT8) t;
    /*TODO*///		B = v;
    /*TODO*///
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_NZ8(B);
    /*TODO*///
    /*TODO*///		if ((B & 0x01) != 0)
    /*TODO*///			SEC;
    /*TODO*///
    /*TODO*///		if ( (v > 127) || (v < -128) )
    /*TODO*///			SEV;
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		hd6309_ICount -= 8;
    /*TODO*///		DZError();
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11be DIVQ extended -**0- */
    /*TODO*///INLINE void divq_ex( void )
    /*TODO*///{
    /*TODO*///	PAIR	t, q;
    /*TODO*///	INT32	v;
    /*TODO*///
    /*TODO*///	q.w.h = D;
    /*TODO*///	q.w.l = W;
    /*TODO*///
    /*TODO*///	EXTWORD(t);
    /*TODO*///
    /*TODO*///	if( t.w.l != 0 )
    /*TODO*///	{
    /*TODO*///		v = (INT32) q.d / (INT16) t.w.l;
    /*TODO*///		D = (INT32) q.d % (INT16) t.w.l;
    /*TODO*///		W = v;
    /*TODO*///
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_NZ16(W);
    /*TODO*///
    /*TODO*///		if ((W & 0x0001) != 0)
    /*TODO*///			SEC;
    /*TODO*///
    /*TODO*///		if ( (v > 65534) || (v < -65535) )
    /*TODO*///			SEV;
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///		DZError();
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10fc LDQ extended -**0- */
    /*TODO*///INLINE void ldq_ex( void )
    /*TODO*///{
    /*TODO*///	PAIR	q;
    /*TODO*///
    /*TODO*///	EXTLONG(q);
    /*TODO*///	D = q.w.h;
    /*TODO*///	W = q.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_N8(A);
    /*TODO*///	SET_Z(q.d);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10bE LDY extended -**0- */
    /*TODO*///INLINE void ldy_ex( void )
    /*TODO*///{
    /*TODO*///	EXTWORD(pY);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(Y);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $bF STX (STY) extended -**0- */
    /*TODO*///INLINE void stx_ex( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);
    /*TODO*///	EXTENDED;
    /*TODO*///	WM16(EAD,&pX);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10fd STQ extended -**0- */
    /*TODO*///INLINE void stq_ex( void )
    /*TODO*///{
    /*TODO*///	PAIR	q;
    /*TODO*///
    /*TODO*///	q.w.h = D;
    /*TODO*///	q.w.l = W;
    /*TODO*///	EXTENDED;
    /*TODO*///	WM32(EAD,&q);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_N8(A);
    /*TODO*///	SET_Z(q.d);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10bF STY extended -**0- */
    /*TODO*///INLINE void sty_ex( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(Y);
    /*TODO*///	EXTENDED;
    /*TODO*///	WM16(EAD,&pY);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///#ifdef macintosh
    /*TODO*///#pragma mark ____Cx____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $c0 SUBB immediate ?**** */
    /*TODO*///INLINE void subb_im( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = B - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1180 SUBE immediate ?**** */
    /*TODO*///INLINE void sube_im( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = E - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(E,t,r);
    /*TODO*///	E = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11C0 SUBF immediate ?**** */
    /*TODO*///INLINE void subf_im( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = F - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(F,t,r);
    /*TODO*///	F = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $c1 CMPB immediate ?**** */
    /*TODO*///INLINE void cmpb_im( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = B - t;
    /*TODO*///	CLR_NZVC; SET_FLAGS8(B,t,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1181 CMPE immediate ?**** */
    /*TODO*///INLINE void cmpe_im( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = E - t;
    /*TODO*///	CLR_NZVC; SET_FLAGS8(E,t,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11C1 CMPF immediate ?**** */
    /*TODO*///INLINE void cmpf_im( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = F - t;
    /*TODO*///	CLR_NZVC; SET_FLAGS8(F,t,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $c2 SBCB immediate ?**** */
    /*TODO*///INLINE void sbcb_im( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = B - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1082 SBCD immediate ?**** */
    /*TODO*///INLINE void sbcd_im( void )
    /*TODO*///{
    /*TODO*///	PAIR	t;
    /*TODO*///	UINT32	 r;
    /*TODO*///	IMMWORD(t);
    /*TODO*///	r = D - t.w.l - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(D,t.w.l,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $c3 ADDD immediate -**** */
    /*TODO*///INLINE void addd_im( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d + b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $108b ADDW immediate -**** */
    /*TODO*///INLINE void addw_im( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = W;
    /*TODO*///	r = d + b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	W = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $118b ADDE immediate -**** */
    /*TODO*///INLINE void adde_im( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = E + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(E,t,r);
    /*TODO*///	SET_H(E,t,r);
    /*TODO*///	E = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11Cb ADDF immediate -**** */
    /*TODO*///INLINE void addf_im( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = F + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(F,t,r);
    /*TODO*///	SET_H(F,t,r);
    /*TODO*///	F = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $c4 ANDB immediate -**0- */
    /*TODO*///INLINE void andb_im( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	B &= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1084 ANDD immediate -**0- */
    /*TODO*///INLINE void andd_im( void )
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	IMMWORD(t);
    /*TODO*///	D &= t.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $c5 BITB immediate -**0- */
    /*TODO*///INLINE void bitb_im( void )
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = B & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1085 BITD immediate -**0- */
    /*TODO*///INLINE void bitd_im( void )
    /*TODO*///{
    /*TODO*///	PAIR	t;
    /*TODO*///	UINT16	r;
    /*TODO*///	IMMWORD(t);
    /*TODO*///	r = B & t.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $113c BITMD immediate -**0- */
    /*TODO*///INLINE void bitmd_im( void )
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = MD & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///
    /*TODO*///	CLDZ;
    /*TODO*///	CLII;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $c6 LDB immediate -**0- */
    /*TODO*///INLINE void ldb_im( void )
    /*TODO*///{
    /*TODO*///	IMMBYTE(B);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $113d LDMD immediate -**0- */
    /*TODO*///INLINE void ldmd_im( void )
    /*TODO*///{
    /*TODO*///	IMMBYTE(MD);
    /*TODO*////*	CLR_NZV;	*/
    /*TODO*////*	SET_NZ8(B); */
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1186 LDE immediate -**0- */
    /*TODO*///INLINE void lde_im( void )
    /*TODO*///{
    /*TODO*///	IMMBYTE(E);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(E);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11C6 LDF immediate -**0- */
    /*TODO*///INLINE void ldf_im( void )
    /*TODO*///{
    /*TODO*///	IMMBYTE(F);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(F);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $c8 EORB immediate -**0- */
    /*TODO*///INLINE void eorb_im( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	B ^= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1088 EORD immediate -**0- */
    /*TODO*///INLINE void eord_im( void )
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	IMMWORD(t);
    /*TODO*///	D ^= t.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $c9 ADCB immediate ***** */
    /*TODO*///INLINE void adcb_im( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = B + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1089 ADCD immediate ***** */
    /*TODO*///INLINE void adcd_im( void )
    /*TODO*///{
    /*TODO*///	PAIR	t;
    /*TODO*///	UINT32	r;
    /*TODO*///	IMMWORD(t);
    /*TODO*///	r = D + t.w.l + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS16(D,t.w.l,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $cA ORB immediate -**0- */
    /*TODO*///INLINE void orb_im( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	B |= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $108a ORD immediate -**0- */
    /*TODO*///INLINE void ord_im( void )
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	IMMWORD(t);
    /*TODO*///	D |= t.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $cB ADDB immediate ***** */
    /*TODO*///INLINE void addb_im( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = B + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $cC LDD immediate -**0- */
    /*TODO*///INLINE void ldd_im( void )
    /*TODO*///{
    /*TODO*///	PAIR	t;
    /*TODO*///
    /*TODO*///	IMMWORD(t);
    /*TODO*///	D=t.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1086 LDW immediate -**0- */
    /*TODO*///INLINE void ldw_im( void )
    /*TODO*///{
    /*TODO*///	PAIR	t;
    /*TODO*///	IMMWORD(t);
    /*TODO*///	W=t.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(W);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $cE LDU (LDS) immediate -**0- */
    /*TODO*///INLINE void ldu_im( void )
    /*TODO*///{
    /*TODO*///	IMMWORD(pU);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(U);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10cE LDS immediate -**0- */
    /*TODO*///INLINE void lds_im( void )
    /*TODO*///{
    /*TODO*///	IMMWORD(pS);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///	hd6309.int_state |= HD6309_LDS;
    /*TODO*///}
    /*TODO*///
    /*TODO*///#ifdef macintosh
    /*TODO*///#pragma mark ____Dx____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $d0 SUBB direct ?**** */
    /*TODO*///INLINE void subb_di( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = B - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1190 SUBE direct ?**** */
    /*TODO*///INLINE void sube_di( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = E - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(E,t,r);
    /*TODO*///	E = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11d0 SUBF direct ?**** */
    /*TODO*///INLINE void subf_di( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = F - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(F,t,r);
    /*TODO*///	F = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $d1 CMPB direct ?**** */
    /*TODO*///INLINE void cmpb_di( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = B - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1191 CMPE direct ?**** */
    /*TODO*///INLINE void cmpe_di( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = E - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(E,t,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11D1 CMPF direct ?**** */
    /*TODO*///INLINE void cmpf_di( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = F - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(F,t,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $d2 SBCB direct ?**** */
    /*TODO*///INLINE void sbcb_di( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = B - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1092 SBCD direct ?**** */
    /*TODO*///INLINE void sbcd_di( void )
    /*TODO*///{
    /*TODO*///	PAIR	t;
    /*TODO*///	UINT32	r;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	r = D - t.w.l - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(D,t.w.l,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $d3 ADDD direct -**** */
    /*TODO*///INLINE void addd_di( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d + b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $109b ADDW direct -**** */
    /*TODO*///INLINE void addw_di( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = W;
    /*TODO*///	r = d + b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	W = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $119b ADDE direct -**** */
    /*TODO*///INLINE void adde_di( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = E + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(E,t,r);
    /*TODO*///	SET_H(E,t,r);
    /*TODO*///	E = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11db ADDF direct -**** */
    /*TODO*///INLINE void addf_di( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = F + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(F,t,r);
    /*TODO*///	SET_H(F,t,r);
    /*TODO*///	F = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $d4 ANDB direct -**0- */
    /*TODO*///INLINE void andb_di( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	B &= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1094 ANDD direct -**0- */
    /*TODO*///INLINE void andd_di( void )
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	D &= t.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $d5 BITB direct -**0- */
    /*TODO*///INLINE void bitb_di( void )
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = B & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1095 BITD direct -**0- */
    /*TODO*///INLINE void bitd_di( void )
    /*TODO*///{
    /*TODO*///	PAIR	t;
    /*TODO*///	UINT16	r;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	r = B & t.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $d6 LDB direct -**0- */
    /*TODO*///INLINE void ldb_di( void )
    /*TODO*///{
    /*TODO*///	DIRBYTE(B);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1196 LDE direct -**0- */
    /*TODO*///INLINE void lde_di( void )
    /*TODO*///{
    /*TODO*///	DIRBYTE(E);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(E);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11d6 LDF direct -**0- */
    /*TODO*///INLINE void ldf_di( void )
    /*TODO*///{
    /*TODO*///	DIRBYTE(F);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(F);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $d7 STB direct -**0- */
    /*TODO*///INLINE void stb_di( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///	DIRECT;
    /*TODO*///	WM(EAD,B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1197 STE direct -**0- */
    /*TODO*///INLINE void ste_di( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(E);
    /*TODO*///	DIRECT;
    /*TODO*///	WM(EAD,E);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11D7 STF direct -**0- */
    /*TODO*///INLINE void stf_di( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(F);
    /*TODO*///	DIRECT;
    /*TODO*///	WM(EAD,F);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $d8 EORB direct -**0- */
    /*TODO*///INLINE void eorb_di( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	B ^= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1098 EORD direct -**0- */
    /*TODO*///INLINE void eord_di( void )
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	D ^= t.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $d9 ADCB direct ***** */
    /*TODO*///INLINE void adcb_di( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = B + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1099 adcd direct ***** */
    /*TODO*///INLINE void adcd_di( void )
    /*TODO*///{
    /*TODO*///	UINT32 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = D + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS16(D,t,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $dA ORB direct -**0- */
    /*TODO*///INLINE void orb_di( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	B |= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $109a ORD direct -**0- */
    /*TODO*///INLINE void ord_di( void )
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	D |= t.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $dB ADDB direct ***** */
    /*TODO*///INLINE void addb_di( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = B + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $dC LDD direct -**0- */
    /*TODO*///INLINE void ldd_di( void )
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	D=t.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1096 LDW direct -**0- */
    /*TODO*///INLINE void ldw_di( void )
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	W=t.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(W);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $dD STD direct -**0- */
    /*TODO*///INLINE void std_di( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///	DIRECT;
    /*TODO*///	WM16(EAD,&pD);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $1097 STW direct -**0- */
    /*TODO*///INLINE void stw_di( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(W);
    /*TODO*///	DIRECT;
    /*TODO*///	WM16(EAD,&pW);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $dE LDU (LDS) direct -**0- */
    /*TODO*///INLINE void ldu_di( void )
    /*TODO*///{
    /*TODO*///	DIRWORD(pU);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(U);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10dE LDS direct -**0- */
    /*TODO*///INLINE void lds_di( void )
    /*TODO*///{
    /*TODO*///	DIRWORD(pS);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///	hd6309.int_state |= HD6309_LDS;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $dF STU (STS) direct -**0- */
    /*TODO*///INLINE void stu_di( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(U);
    /*TODO*///	DIRECT;
    /*TODO*///	WM16(EAD,&pU);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10dF STS direct -**0- */
    /*TODO*///INLINE void sts_di( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///	DIRECT;
    /*TODO*///	WM16(EAD,&pS);
    /*TODO*///}
    /*TODO*///
    /*TODO*///#ifdef macintosh
    /*TODO*///#pragma mark ____Ex____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///
    /*TODO*////* $e0 SUBB indexed ?**** */
    /*TODO*///INLINE void subb_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = B - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11a0 SUBE indexed ?**** */
    /*TODO*///INLINE void sube_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = E - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(E,t,r);
    /*TODO*///	E = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11e0 SUBF indexed ?**** */
    /*TODO*///INLINE void subf_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = F - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(F,t,r);
    /*TODO*///	F = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $e1 CMPB indexed ?**** */
    /*TODO*///INLINE void cmpb_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = B - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11a1 CMPE indexed ?**** */
    /*TODO*///INLINE void cmpe_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = E - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(E,t,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11e1 CMPF indexed ?**** */
    /*TODO*///INLINE void cmpf_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = F - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(F,t,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $e2 SBCB indexed ?**** */
    /*TODO*///INLINE void sbcb_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = B - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10a2 SBCD indexed ?**** */
    /*TODO*///INLINE void sbcd_ix( void )
    /*TODO*///{
    /*TODO*///	UINT32	  t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM16(EAD);
    /*TODO*///	r = D - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(D,t,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $e3 ADDD indexed -**** */
    /*TODO*///INLINE void addd_ix( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	d = D;
    /*TODO*///	r = d + b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10ab ADDW indexed -**** */
    /*TODO*///INLINE void addw_ix( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	d = W;
    /*TODO*///	r = d + b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	W = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11ab ADDE indexed -**** */
    /*TODO*///INLINE void adde_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = E + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(E,t,r);
    /*TODO*///	SET_H(E,t,r);
    /*TODO*///	E = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11eb ADDF indexed -**** */
    /*TODO*///INLINE void addf_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = F + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(F,t,r);
    /*TODO*///	SET_H(F,t,r);
    /*TODO*///	F = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $e4 ANDB indexed -**0- */
    /*TODO*///INLINE void andb_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	B &= RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10a4 ANDD indexed -**0- */
    /*TODO*///INLINE void andd_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	D &= RM16(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $e5 BITB indexed -**0- */
    /*TODO*///INLINE void bitb_ix( void )
    /*TODO*///{
    /*TODO*///	UINT8 r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	r = B & RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10a5 BITD indexed -**0- */
    /*TODO*///INLINE void bitd_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	r = D & RM16(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $e6 LDB indexed -**0- */
    /*TODO*///INLINE void ldb_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	B = RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11a6 LDE indexed -**0- */
    /*TODO*///INLINE void lde_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	E = RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(E);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11e6 LDF indexed -**0- */
    /*TODO*///INLINE void ldf_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	F = RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(F);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $e7 STB indexed -**0- */
    /*TODO*///INLINE void stb_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///	WM(EAD,B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11a7 STE indexed -**0- */
    /*TODO*///INLINE void ste_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(E);
    /*TODO*///	WM(EAD,E);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11e7 STF indexed -**0- */
    /*TODO*///INLINE void stf_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(F);
    /*TODO*///	WM(EAD,F);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $e8 EORB indexed -**0- */
    /*TODO*///INLINE void eorb_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	B ^= RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10a8 EORD indexed -**0- */
    /*TODO*///INLINE void eord_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	D ^= RM16(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $e9 ADCB indexed ***** */
    /*TODO*///INLINE void adcb_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = B + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10a9 ADCD indexed ***** */
    /*TODO*///INLINE void adcd_ix( void )
    /*TODO*///{
    /*TODO*///	UINT32 t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = D + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS16(D,t,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $eA ORB indexed -**0- */
    /*TODO*///INLINE void orb_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	B |= RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10aa ORD indexed -**0- */
    /*TODO*///INLINE void ord_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	D |= RM16(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $eB ADDB indexed ***** */
    /*TODO*///INLINE void addb_ix( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	fetch_effective_address();
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = B + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $eC LDD indexed -**0- */
    /*TODO*///INLINE void ldd_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	D=RM16(EAD);
    /*TODO*///	CLR_NZV; SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10a6 LDW indexed -**0- */
    /*TODO*///INLINE void ldw_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	W=RM16(EAD);
    /*TODO*///	CLR_NZV; SET_NZ16(W);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $eD STD indexed -**0- */
    /*TODO*///INLINE void std_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///	WM16(EAD,&pD);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10a7 STW indexed -**0- */
    /*TODO*///INLINE void stw_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(W);
    /*TODO*///	WM16(EAD,&pW);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $eE LDU (LDS) indexed -**0- */
    /*TODO*///INLINE void ldu_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	U=RM16(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(U);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10eE LDS indexed -**0- */
    /*TODO*///INLINE void lds_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	S=RM16(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///	hd6309.int_state |= HD6309_LDS;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $eF STU (STS) indexed -**0- */
    /*TODO*///INLINE void stu_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(U);
    /*TODO*///	WM16(EAD,&pU);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10eF STS indexed -**0- */
    /*TODO*///INLINE void sts_ix( void )
    /*TODO*///{
    /*TODO*///	fetch_effective_address();
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///	WM16(EAD,&pS);
    /*TODO*///}
    /*TODO*///
    /*TODO*///#ifdef macintosh
    /*TODO*///#pragma mark ____Fx____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $f0 SUBB extended ?**** */
    /*TODO*///INLINE void subb_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = B - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11b0 SUBE extended ?**** */
    /*TODO*///INLINE void sube_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = E - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(E,t,r);
    /*TODO*///	E = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11f0 SUBF extended ?**** */
    /*TODO*///INLINE void subf_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = F - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(F,t,r);
    /*TODO*///	F = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $f1 CMPB extended ?**** */
    /*TODO*///INLINE void cmpb_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = B - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11b1 CMPE extended ?**** */
    /*TODO*///INLINE void cmpe_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = E - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(E,t,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11f1 CMPF extended ?**** */
    /*TODO*///INLINE void cmpf_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = F - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(F,t,r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $f2 SBCB extended ?**** */
    /*TODO*///INLINE void sbcb_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = B - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10b2 SBCD extended ?**** */
    /*TODO*///INLINE void sbcd_ex( void )
    /*TODO*///{
    /*TODO*///	PAIR t = {{0,}};
    /*TODO*///	UINT32 r;
    /*TODO*///
    /*TODO*///	EXTWORD(t);
    /*TODO*///	r = D - t.w.l - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(D,t.w.l,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $f3 ADDD extended -**** */
    /*TODO*///INLINE void addd_ex( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b = {{0,}};
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d + b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10bb ADDW extended -**** */
    /*TODO*///INLINE void addw_ex( void )
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b = {{0,}};
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = W;
    /*TODO*///	r = d + b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	W = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11bb ADDE extended -**** */
    /*TODO*///INLINE void adde_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = E + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(E,t,r);
    /*TODO*///	SET_H(E,t,r);
    /*TODO*///	E = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11fb ADDF extended -**** */
    /*TODO*///INLINE void addf_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = F + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(F,t,r);
    /*TODO*///	SET_H(F,t,r);
    /*TODO*///	F = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $f4 ANDB extended -**0- */
    /*TODO*///INLINE void andb_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	B &= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10b4 ANDD extended -**0- */
    /*TODO*///INLINE void andd_ex( void )
    /*TODO*///{
    /*TODO*///	PAIR t = {{0,}};
    /*TODO*///	EXTWORD(t);
    /*TODO*///	D &= t.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $f5 BITB extended -**0- */
    /*TODO*///INLINE void bitb_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = B & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10b5 BITD extended -**0- */
    /*TODO*///INLINE void bitd_ex( void )
    /*TODO*///{
    /*TODO*///	PAIR t = {{0,}};
    /*TODO*///	UINT8 r;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	r = B & t.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(r);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $f6 LDB extended -**0- */
    /*TODO*///INLINE void ldb_ex( void )
    /*TODO*///{
    /*TODO*///	EXTBYTE(B);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11b6 LDE extended -**0- */
    /*TODO*///INLINE void lde_ex( void )
    /*TODO*///{
    /*TODO*///	EXTBYTE(E);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(E);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11f6 LDF extended -**0- */
    /*TODO*///INLINE void ldf_ex( void )
    /*TODO*///{
    /*TODO*///	EXTBYTE(F);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(F);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $f7 STB extended -**0- */
    /*TODO*///INLINE void stb_ex( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///	EXTENDED;
    /*TODO*///	WM(EAD,B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11b7 STE extended -**0- */
    /*TODO*///INLINE void ste_ex( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(E);
    /*TODO*///	EXTENDED;
    /*TODO*///	WM(EAD,E);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11f7 STF extended -**0- */
    /*TODO*///INLINE void stf_ex( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(F);
    /*TODO*///	EXTENDED;
    /*TODO*///	WM(EAD,F);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $f8 EORB extended -**0- */
    /*TODO*///INLINE void eorb_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	B ^= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10b8 EORD extended -**0- */
    /*TODO*///INLINE void eord_ex( void )
    /*TODO*///{
    /*TODO*///	PAIR t = {{0,}};
    /*TODO*///	EXTWORD(t);
    /*TODO*///	D ^= t.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $f9 ADCB extended ***** */
    /*TODO*///INLINE void adcb_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = B + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10b9 ADCD extended ***** */
    /*TODO*///INLINE void adcd_ex( void )
    /*TODO*///{
    /*TODO*///	UINT32 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = D + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS16(D,t,r);
    /*TODO*///	D = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $fA ORB extended -**0- */
    /*TODO*///INLINE void orb_ex( void )
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	B |= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10ba ORD extended -**0- */
    /*TODO*///INLINE void ord_ex( void )
    /*TODO*///{
    /*TODO*///	PAIR t = {{0,}};
    /*TODO*///	EXTWORD(t);
    /*TODO*///	D |= t.w.l;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $fB ADDB extended ***** */
    /*TODO*///INLINE void addb_ex( void )
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = B + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $fC LDD extended -**0- */
    /*TODO*///INLINE void ldd_ex( void )
    /*TODO*///{
    /*TODO*///	EXTWORD(pD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10b6 LDW extended -**0- */
    /*TODO*///INLINE void ldw_ex( void )
    /*TODO*///{
    /*TODO*///	EXTWORD(pW);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(W);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $fD STD extended -**0- */
    /*TODO*///INLINE void std_ex( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///	EXTENDED;
    /*TODO*///	WM16(EAD,&pD);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10b7 STW extended -**0- */
    /*TODO*///INLINE void stw_ex( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(W);
    /*TODO*///	EXTENDED;
    /*TODO*///	WM16(EAD,&pW);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $fE LDU (LDS) extended -**0- */
    /*TODO*///INLINE void ldu_ex( void )
    /*TODO*///{
    /*TODO*///	EXTWORD(pU);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(U);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10fE LDS extended -**0- */
    /*TODO*///INLINE void lds_ex( void )
    /*TODO*///{
    /*TODO*///	EXTWORD(pS);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///	hd6309.int_state |= HD6309_LDS;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $fF STU (STS) extended -**0- */
    /*TODO*///INLINE void stu_ex( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(U);
    /*TODO*///	EXTENDED;
    /*TODO*///	WM16(EAD,&pU);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10fF STS extended -**0- */
    /*TODO*///INLINE void sts_ex( void )
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///	EXTENDED;
    /*TODO*///	WM16(EAD,&pS);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $10xx opcodes */
    /*TODO*///INLINE void pref10( void )
    /*TODO*///{
    /*TODO*///	UINT8 ireg2 = ROP(PCD);
    /*TODO*///	PC++;
    /*TODO*///
    /*TODO*///#ifdef BIG_SWITCH
    /*TODO*///	switch( ireg2 )
    /*TODO*///	{
    /*TODO*///		case 0x21: lbrn();			break;
    /*TODO*///		case 0x22: lbhi();			break;
    /*TODO*///		case 0x23: lbls();			break;
    /*TODO*///		case 0x24: lbcc();			break;
    /*TODO*///		case 0x25: lbcs();			break;
    /*TODO*///		case 0x26: lbne();			break;
    /*TODO*///		case 0x27: lbeq();			break;
    /*TODO*///		case 0x28: lbvc();			break;
    /*TODO*///		case 0x29: lbvs();			break;
    /*TODO*///		case 0x2a: lbpl();			break;
    /*TODO*///		case 0x2b: lbmi();			break;
    /*TODO*///		case 0x2c: lbge();			break;
    /*TODO*///		case 0x2d: lblt();			break;
    /*TODO*///		case 0x2e: lbgt();			break;
    /*TODO*///		case 0x2f: lble();			break;
    /*TODO*///
    /*TODO*///		case 0x30: addr_r();		break;
    /*TODO*///		case 0x31: adcr();			break;
    /*TODO*///		case 0x32: subr();			break;
    /*TODO*///		case 0x33: sbcr();			break;
    /*TODO*///		case 0x34: andr();			break;
    /*TODO*///		case 0x35: orr();			break;
    /*TODO*///		case 0x36: eorr();			break;
    /*TODO*///		case 0x37: cmpr();			break;
    /*TODO*///		case 0x38: pshsw(); 		break;
    /*TODO*///		case 0x39: pulsw(); 		break;
    /*TODO*///		case 0x3a: pshuw(); 		break;
    /*TODO*///		case 0x3b: puluw(); 		break;
    /*TODO*///		case 0x3f: swi2();		    break;
    /*TODO*///
    /*TODO*///		case 0x40: negd();			break;
    /*TODO*///		case 0x43: comd();			break;
    /*TODO*///		case 0x44: lsrd();			break;
    /*TODO*///		case 0x46: rord();			break;
    /*TODO*///		case 0x47: asrd();			break;
    /*TODO*///		case 0x48: asld();			break;
    /*TODO*///		case 0x49: rold();			break;
    /*TODO*///		case 0x4a: decd();			break;
    /*TODO*///		case 0x4c: incd();			break;
    /*TODO*///		case 0x4d: tstd();			break;
    /*TODO*///		case 0x4f: clrd();			break;
    /*TODO*///
    /*TODO*///		case 0x53: comw();			break;
    /*TODO*///		case 0x54: lsrw();			break;
    /*TODO*///		case 0x56: rorw();			break;
    /*TODO*///		case 0x59: rolw();			break;
    /*TODO*///		case 0x5a: decw();			break;
    /*TODO*///		case 0x5c: incw();			break;
    /*TODO*///		case 0x5d: tstw();			break;
    /*TODO*///		case 0x5f: clrw();			break;
    /*TODO*///
    /*TODO*///		case 0x80: subw_im();		break;
    /*TODO*///		case 0x81: cmpw_im();		break;
    /*TODO*///		case 0x82: sbcd_im();		break;
    /*TODO*///		case 0x83: cmpd_im();		break;
    /*TODO*///		case 0x84: andd_im();		break;
    /*TODO*///		case 0x85: bitd_im();		break;
    /*TODO*///		case 0x86: ldw_im();		break;
    /*TODO*///		case 0x88: eord_im();		break;
    /*TODO*///		case 0x89: adcd_im();		break;
    /*TODO*///		case 0x8a: ord_im();		break;
    /*TODO*///		case 0x8b: addw_im();		break;
    /*TODO*///		case 0x8c: cmpy_im();		break;
    /*TODO*///		case 0x8e: ldy_im();		break;
    /*TODO*///
    /*TODO*///		case 0x90: subw_di();		break;
    /*TODO*///		case 0x91: cmpw_di();		break;
    /*TODO*///		case 0x92: sbcd_di();		break;
    /*TODO*///		case 0x93: cmpd_di();		break;
    /*TODO*///		case 0x94: andd_di();		break;
    /*TODO*///		case 0x95: bitd_di();		break;
    /*TODO*///		case 0x96: ldw_di();		break;
    /*TODO*///		case 0x97: stw_di();		break;
    /*TODO*///		case 0x98: eord_di();		break;
    /*TODO*///		case 0x99: adcd_di();		break;
    /*TODO*///		case 0x9a: ord_di();		break;
    /*TODO*///		case 0x9b: addw_di();		break;
    /*TODO*///		case 0x9c: cmpy_di();		break;
    /*TODO*///		case 0x9e: ldy_di();		break;
    /*TODO*///		case 0x9f: sty_di();		break;
    /*TODO*///
    /*TODO*///		case 0xa0: subw_ix();		break;
    /*TODO*///		case 0xa1: cmpw_ix();		break;
    /*TODO*///		case 0xa2: sbcd_ix();		break;
    /*TODO*///		case 0xa3: cmpd_ix();		break;
    /*TODO*///		case 0xa4: andd_ix();		break;
    /*TODO*///		case 0xa5: bitd_ix();		break;
    /*TODO*///		case 0xa6: ldw_ix();		break;
    /*TODO*///		case 0xa7: stw_ix();		break;
    /*TODO*///		case 0xa8: eord_ix();		break;
    /*TODO*///		case 0xa9: adcd_ix();		break;
    /*TODO*///		case 0xaa: ord_ix();		break;
    /*TODO*///		case 0xab: addw_ix();		break;
    /*TODO*///		case 0xac: cmpy_ix();		break;
    /*TODO*///		case 0xae: ldy_ix();		break;
    /*TODO*///		case 0xaf: sty_ix();		break;
    /*TODO*///
    /*TODO*///		case 0xb0: subw_ex();		break;
    /*TODO*///		case 0xb1: cmpw_ex();		break;
    /*TODO*///		case 0xb2: sbcd_ex();		break;
    /*TODO*///		case 0xb3: cmpd_ex();		break;
    /*TODO*///		case 0xb4: andd_ex();		break;
    /*TODO*///		case 0xb5: bitd_ex();		break;
    /*TODO*///		case 0xb6: ldw_ex();		break;
    /*TODO*///		case 0xb7: stw_ex();		break;
    /*TODO*///		case 0xb8: eord_ex();		break;
    /*TODO*///		case 0xb9: adcd_ex();		break;
    /*TODO*///		case 0xba: ord_ex();		break;
    /*TODO*///		case 0xbb: addw_ex();		break;
    /*TODO*///		case 0xbc: cmpy_ex();		break;
    /*TODO*///		case 0xbe: ldy_ex();		break;
    /*TODO*///		case 0xbf: sty_ex();		break;
    /*TODO*///
    /*TODO*///		case 0xce: lds_im();		break;
    /*TODO*///
    /*TODO*///		case 0xdc: ldq_di();		break;
    /*TODO*///		case 0xdd: stq_di();		break;
    /*TODO*///		case 0xde: lds_di();		break;
    /*TODO*///		case 0xdf: sts_di();		break;
    /*TODO*///
    /*TODO*///		case 0xec: ldq_ix();		break;
    /*TODO*///		case 0xed: stq_ix();		break;
    /*TODO*///		case 0xee: lds_ix();		break;
    /*TODO*///		case 0xef: sts_ix();		break;
    /*TODO*///
    /*TODO*///		case 0xfc: ldq_ex();		break;
    /*TODO*///		case 0xfd: stq_ex();		break;
    /*TODO*///		case 0xfe: lds_ex();		break;
    /*TODO*///		case 0xff: sts_ex();		break;
    /*TODO*///
    /*TODO*///		default:  IIError();        break;
    /*TODO*///	}
    /*TODO*///#else
    /*TODO*///
    /*TODO*///	(*hd6309_page01[ireg2])();
    /*TODO*///
    /*TODO*///#endif /* BIG_SWITCH */
    /*TODO*///
    /*TODO*///	hd6309_ICount -= cycle_counts_page01[ireg2];
    /*TODO*///}
    /*TODO*///
    /*TODO*////* $11xx opcodes */
    /*TODO*///INLINE void pref11( void )
    /*TODO*///{
    /*TODO*///	UINT8 ireg2 = ROP(PCD);
    /*TODO*///	PC++;
    /*TODO*///
    /*TODO*///#ifdef BIG_SWITCH
    /*TODO*///	switch( ireg2 )
    /*TODO*///	{
    /*TODO*///		case 0x30: band();			break;
    /*TODO*///		case 0x31: biand(); 		break;
    /*TODO*///		case 0x32: bor();			break;
    /*TODO*///		case 0x33: bior();			break;
    /*TODO*///		case 0x34: beor();			break;
    /*TODO*///		case 0x35: bieor(); 		break;
    /*TODO*///		case 0x36: ldbt();			break;
    /*TODO*///		case 0x37: stbt();			break;
    /*TODO*///		case 0x38: tfmpp(); 		break;	/* Timing for TFM is actually 6+3n.        */
    /*TODO*///		case 0x39: tfmmm(); 		break;	/* To avoid saving the state, I decided    */
    /*TODO*///		case 0x3a: tfmpc(); 		break;	/* to push the initial 6 cycles to the end */
    /*TODO*///		case 0x3b: tfmcp(); 		break;  /* We will soon see how this fairs!        */
    /*TODO*///		case 0x3c: bitmd_im();		break;
    /*TODO*///		case 0x3d: ldmd_im();		break;
    /*TODO*///		case 0x3f: swi3();			break;
    /*TODO*///
    /*TODO*///		case 0x43: come();			break;
    /*TODO*///		case 0x4a: dece();			break;
    /*TODO*///		case 0x4c: ince();			break;
    /*TODO*///		case 0x4d: tste();			break;
    /*TODO*///		case 0x4f: clre();			break;
    /*TODO*///
    /*TODO*///		case 0x53: comf();			break;
    /*TODO*///		case 0x5a: decf();			break;
    /*TODO*///		case 0x5c: incf();			break;
    /*TODO*///		case 0x5d: tstf();			break;
    /*TODO*///		case 0x5f: clrf();			break;
    /*TODO*///
    /*TODO*///		case 0x80: sube_im();		break;
    /*TODO*///		case 0x81: cmpe_im();		break;
    /*TODO*///		case 0x83: cmpu_im();		break;
    /*TODO*///		case 0x86: lde_im();		break;
    /*TODO*///		case 0x8b: adde_im();		break;
    /*TODO*///		case 0x8c: cmps_im();		break;
    /*TODO*///		case 0x8d: divd_im();		break;
    /*TODO*///		case 0x8e: divq_im();		break;
    /*TODO*///		case 0x8f: muld_im();		break;
    /*TODO*///
    /*TODO*///		case 0x90: sube_di();		break;
    /*TODO*///		case 0x91: cmpe_di();		break;
    /*TODO*///		case 0x93: cmpu_di();		break;
    /*TODO*///		case 0x96: lde_di();		break;
    /*TODO*///		case 0x97: ste_di();		break;
    /*TODO*///		case 0x9b: adde_di();		break;
    /*TODO*///		case 0x9c: cmps_di();		break;
    /*TODO*///		case 0x9d: divd_di();		break;
    /*TODO*///		case 0x9e: divq_di();		break;
    /*TODO*///		case 0x9f: muld_di();		break;
    /*TODO*///
    /*TODO*///		case 0xa0: sube_ix();		break;
    /*TODO*///		case 0xa1: cmpe_ix();		break;
    /*TODO*///		case 0xa3: cmpu_ix();		break;
    /*TODO*///		case 0xa6: lde_ix();		break;
    /*TODO*///		case 0xa7: ste_ix();		break;
    /*TODO*///		case 0xab: adde_ix();		break;
    /*TODO*///		case 0xac: cmps_ix();		break;
    /*TODO*///		case 0xad: divd_ix();		break;
    /*TODO*///		case 0xae: divq_ix();		break;
    /*TODO*///		case 0xaf: muld_ix();		break;
    /*TODO*///
    /*TODO*///		case 0xb0: sube_ex();		break;
    /*TODO*///		case 0xb1: cmpe_ex();		break;
    /*TODO*///		case 0xb3: cmpu_ex();		break;
    /*TODO*///		case 0xb6: lde_ex();		break;
    /*TODO*///		case 0xb7: ste_ex();		break;
    /*TODO*///		case 0xbb: adde_ex();		break;
    /*TODO*///		case 0xbc: cmps_ex();		break;
    /*TODO*///		case 0xbd: divd_ex();		break;
    /*TODO*///		case 0xbe: divq_ex();		break;
    /*TODO*///		case 0xbf: muld_ex();		break;
    /*TODO*///
    /*TODO*///		case 0xc0: subf_im();		break;
    /*TODO*///		case 0xc1: cmpf_im();		break;
    /*TODO*///		case 0xc6: ldf_im();		break;
    /*TODO*///		case 0xcb: addf_im();		break;
    /*TODO*///
    /*TODO*///		case 0xd0: subf_di();		break;
    /*TODO*///		case 0xd1: cmpf_di();		break;
    /*TODO*///		case 0xd6: ldf_di();		break;
    /*TODO*///		case 0xd7: stf_di();		break;
    /*TODO*///		case 0xdb: addf_di();		break;
    /*TODO*///
    /*TODO*///		case 0xe0: subf_ix();		break;
    /*TODO*///		case 0xe1: cmpf_ix();		break;
    /*TODO*///		case 0xe6: ldf_ix();		break;
    /*TODO*///		case 0xe7: stf_ix();		break;
    /*TODO*///		case 0xeb: addf_ix();		break;
    /*TODO*///
    /*TODO*///		case 0xf0: subf_ex();		break;
    /*TODO*///		case 0xf1: cmpf_ex();		break;
    /*TODO*///		case 0xf6: ldf_ex();		break;
    /*TODO*///		case 0xf7: stf_ex();		break;
    /*TODO*///		case 0xfb: addf_ex();		break;
    /*TODO*///
    /*TODO*///		default:   IIError();		break;
    /*TODO*///	}
    /*TODO*///#else
    /*TODO*///
    /*TODO*///	(*hd6309_page11[ireg2])();
    /*TODO*///
    /*TODO*///#endif /* BIG_SWITCH */
    /*TODO*///	hd6309_ICount -= cycle_counts_page11[ireg2];
    /*TODO*///}
    /*TODO*///
    
}
