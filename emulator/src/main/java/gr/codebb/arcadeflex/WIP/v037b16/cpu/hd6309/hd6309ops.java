package gr.codebb.arcadeflex.WIP.v037b16.cpu.hd6309;

import static common.libc.cstdio.*;
import static gr.codebb.arcadeflex.WIP.v037b16.cpu.hd6309.hd6309tbl.opcode;
import static gr.codebb.arcadeflex.WIP.v037b16.cpu.hd6309.hd6309.*;
import static gr.codebb.arcadeflex.WIP.v037b16.cpu.hd6309.hd6309tbl.hd6309_page01;

public class hd6309ops {
    
    public static FILE hd6309log = null;//fopen("m68k.log", "wa");  //for debug purposes
    
    public static opcode xxx = new opcode() {
        public void handler() {
            if (hd6309log != null) {
                fclose(hd6309log);
            }
            throw new UnsupportedOperationException("Unimplemented");
        }
    };

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
    public static opcode  illegal = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
    };
    
    public static opcode  IIError = new opcode() {
            public void handler() {
    /*TODO*///	SEII;			// Set illegal Instruction Flag
    /*TODO*///	illegal();		// Vector to Trap handler
            }
    };
    
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
        /* $00 NEG direct ?**** */
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
    
        /* $01 OIM direct ?**** */
        public static opcode oim_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
                /*TODO*///	UINT8	r,t,im;
                /*TODO*///	IMMBYTE(im);
                /*TODO*///	DIRBYTE(t);
                /*TODO*///	r = im | t;
                /*TODO*///	CLR_NZV;
                /*TODO*///	SET_NZ8(r);
                /*TODO*///	WM(EAD,r);
            }
        };
        
        /* $02 AIM direct */
        public static opcode  aim_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8	r,t,im;
        /*TODO*///	IMMBYTE(im);
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = im & t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
        /*TODO*///	WM(EAD,r);
            }
        };
        
        /* $03 COM direct -**01 */
        public static opcode  com_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	t = ~t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(t);
        /*TODO*///	SEC;
        /*TODO*///	WM(EAD,t);
            }
        };
        
        /* $04 LSR direct -0*-* */
        public static opcode  lsr_di = new opcode() {
            public void handler() {
                int t = DIRBYTE();
                CLR_NZC();
                hd6309.cc |= (t & CC_C);
                t = (t >>> 1) & 0XFF;
                SET_Z8(t);
                WM(ea, t);
            }
        };
        
        /* $05 EIM direct */
        public static opcode  eim_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8	r,t,im;
        /*TODO*///	IMMBYTE(im);
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = im ^ t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
        /*TODO*///	WM(EAD,r);
            }
        };
        
        /* $06 ROR direct -**-* */
        public static opcode  ror_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r= (CC & CC_C) << 7;
        /*TODO*///	CLR_NZC;
        /*TODO*///	CC |= (t & CC_C);
        /*TODO*///	r |= t>>1;
        /*TODO*///	SET_NZ8(r);
        /*TODO*///	WM(EAD,r);
            }
        };
        
        /* $07 ASR direct ?**-* */
        public static opcode  asr_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	CLR_NZC;
        /*TODO*///	CC |= (t & CC_C);
        /*TODO*///	t = (t & 0x80) | (t >> 1);
        /*TODO*///	SET_NZ8(t);
        /*TODO*///	WM(EAD,t);
            }
        };
        
        /* $08 ASL direct ?**** */
        public static opcode  asl_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = t << 1;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(t,t,r);
        /*TODO*///	WM(EAD,r);
            }
        };
        
        /* $09 ROL direct -**** */
        public static opcode  rol_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = (CC & CC_C) | (t << 1);
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(t,t,r);
        /*TODO*///	WM(EAD,r);
            }
        };
        
        /* $0A DEC direct -***- */
        public static opcode  dec_di = new opcode() {
            public void handler() {
        /*TODO*///	UINT8 t;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	--t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_FLAGS8D(t);
        /*TODO*///	WM(EAD,t);
                int t = DIRBYTE();
                t = (t - 1) & 0xFF;
                CLR_NZV();
                SET_FLAGS8D(t);
                WM(ea, t);
            }
        };
        
        /* $0B TIM direct */
        public static opcode  tim_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8	r,t,im;
        /*TODO*///	IMMBYTE(im);
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = im & t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
            }
        };
        
        /* $OC INC direct -***- */
        public static opcode  inc_di = new opcode() {
            public void handler() {
        /*TODO*///	UINT8 t;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	++t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_FLAGS8I(t);
        /*TODO*///	WM(EAD,t);
                int t = DIRBYTE();
                t = (t + 1) & 0xFF;
                CLR_NZV();
                SET_FLAGS8I(t);
                WM(ea, t);
            }
        };
        
        /* $OD TST direct -**0- */
        public static opcode  tst_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(t);
            }
        };
        
        /* $0E JMP direct ----- */
        public static opcode  jmp_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	DIRECT;
        /*TODO*///	PCD = EAD;
        /*TODO*///	CHANGE_PC;
            }
        };
        
        /* $0F CLR direct -0100 */
        public static opcode  clr_di = new opcode() {
            public void handler() {
                DIRECT();
                WM(ea, 0);
                CLR_NZVC();
                SEZ();
            }
        };
                
        /* $10 FLAG */
        /*TODO*///
        /* $11 FLAG */
        /*TODO*///
        /* $12 NOP inherent ----- */
        public static opcode  nop = new opcode() {
            public void handler() {                
        	;
            }
        };
        
        /* $13 SYNC inherent ----- */
        public static opcode  sync = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	/* SYNC stops processing instructions until an interrupt request happens. */
        /*TODO*///	/* This doesn't require the corresponding interrupt to be enabled: if it */
        /*TODO*///	/* is disabled, execution continues with the next instruction. */
        /*TODO*///	hd6309.int_state |= HD6309_SYNC;	 /* HJB 990227 */
        /*TODO*///	CHECK_IRQ_LINES();
        /*TODO*///	/* if HD6309_SYNC has not been cleared by CHECK_IRQ_LINES(),
        /*TODO*///	 * stop execution until the interrupt lines change. */
        /*TODO*///	if( hd6309.int_state & HD6309_SYNC )
        /*TODO*///		if (hd6309_ICount > 0) hd6309_ICount = 0;
            }
        };
        
        /* $14 sexw inherent */
        public static opcode  sexw = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 t;
        /*TODO*///	t = SIGNED_16(W);
        /*TODO*///	D = t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_N8(A);
        /*TODO*///	if ( D == 0 && W == 0 ) SEZ;
            }
        };
        
        /* $15 ILLEGAL */
        /*TODO*///
        /* $16 LBRA relative ----- */
        public static opcode  lbra = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	IMMWORD(ea);
        /*TODO*///	PC += EA;
        /*TODO*///	CHANGE_PC;
        /*TODO*///
        /*TODO*///	if ( EA == 0xfffd )  /* EHC 980508 speed up busy loop */
        /*TODO*///		if ( hd6309_ICount > 0)
        /*TODO*///			hd6309_ICount = 0;
            }
        };
        
        /* $17 LBSR relative ----- */
        public static opcode  lbsr = new opcode() {
            public void handler() {
                ea = IMMWORD();
                PUSHWORD(hd6309.pc);
                hd6309.pc = ((hd6309.pc + ea) & 0xFFFF);
                CHANGE_PC();
            }
        };
        
        /* $18 ILLEGAL */
        /*TODO*///
        /* $19 DAA inherent (A) -**0* */
        public static opcode  daa = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };
        
        /* $1A ORCC immediate ##### */
        public static opcode  orcc = new opcode() {
            public void handler() {                
                int t = IMMBYTE();
                hd6309.cc = (hd6309.cc | t) & 0xFF;
                CHECK_IRQ_LINES();
            }
        };
        
        /* $1B ILLEGAL */
        /*TODO*///
        /* $1C ANDCC immediate ##### */
        public static opcode  andcc = new opcode() {
            public void handler() {
                int t = IMMBYTE();
                hd6309.cc = (hd6309.cc & t) & 0xFF;
                CHECK_IRQ_LINES();
            }
        };
        
        /* $1D SEX inherent -**0- */
        public static opcode  sex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t;
        /*TODO*///	t = SIGNED(B);
        /*TODO*///	D = t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(t);
            }
        };
        
        public static opcode  exg = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };
        
        /* $1F TFR inherent ----- */
        public static opcode  tfr = new opcode() {
            public void handler() {
                
        	int /*UINT8*/ tb;
                int /*UINT16*/ t;
        	int promote = 0;
        
        	tb = IMMBYTE();
        	if(( (tb^(tb>>4)) & 0x08 ) != 0)
        	{
        		promote = 1;
        	}
        
        	switch(tb>>4) {
        		case  0: t = getDreg();  break;
        		case  1: t = hd6309.x;  break;
        		case  2: t = hd6309.y;  break;
        		case  3: t = hd6309.u;  break;
        		case  4: t = hd6309.s;  break;
        		case  5: t = hd6309.pc; break;
        		case  6: t = getWreg();  break;
        		case  7: t = hd6309.v;  break;
        		case  8: t = (promote!=0 ? getDreg() : hd6309.a );  break;
        		case  9: t = (promote!=0 ? getDreg() : hd6309.b );  break;
        		case 10: t = hd6309.cc; break;
        		case 11: t = hd6309.dp; break;
        		case 12: t = 0;  break;
        		case 13: t = 0;  break;
        		case 14: t = (promote!=0 ? getWreg() : hd6309.e ); break;
        		default: t = (promote!=0 ? getWreg() : hd6309.f ); break;
        	}
        
        	switch(tb&15) {
        		case  0: setDreg(t);  break;
        		case  1: hd6309.x = t;  break;
        		case  2: hd6309.y = t;  break;
        		case  3: hd6309.u = t;  break;
        		case  4: hd6309.s = t;  break;
        		case  5: hd6309.pc = t; CHANGE_PC(); break;
        		case  6: setWreg(t);  break;
        		case  7: hd6309.v = t;  break;
        		case  8: if (promote != 0) setDreg(t); else hd6309.a = t; break;
        		case  9: if (promote != 0) setDreg(t); else hd6309.b = t; break;
        		case 10: hd6309.cc = t; break;
        		case 11: hd6309.dp = t; break;
        		case 12: /* 0 = t1 */ break;
        		case 13: /* 0 = t1 */ break;
        		case 14: if (promote != 0) setWreg(t); else hd6309.e = t; break;
        		case 15: if (promote != 0) setWreg(t); else hd6309.f = t; break;
        	}
            }
        };
            
        /* $20 BRA relative ----- */
        public static opcode  bra = new opcode() {
            public void handler() {
        /*TODO*///	UINT8 t;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	PC += SIGNED(t);
        /*TODO*///	CHANGE_PC;
        /*TODO*///	/* JB 970823 - speed up busy loops */
        /*TODO*///	if( t == 0xfe )
        /*TODO*///		if( hd6309_ICount > 0 ) hd6309_ICount = 0;
                int t;
                t = IMMBYTE();
                hd6309.pc = (hd6309.pc + SIGNED(t)) & 0xFFFF;
                CHANGE_PC();
                /* JB 970823 - speed up busy loops */
                if (t == 0xfe) {
                    if (hd6309_ICount[0] > 0) {
                        hd6309_ICount[0] = 0;
                    }
                }
            }
        };
        
        /* $21 BRN relative ----- */
        public static opcode  brn = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	IMMBYTE(t);
            }
        };
        
        /* $1021 LBRN relative ----- */
        public static opcode  lbrn = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	IMMWORD(ea);
            }
        };
        
        /* $22 BHI relative ----- */
        public static opcode  bhi = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	BRANCH( !(CC & (CC_Z|CC_C)) );
            }
        };
        
        /* $1022 LBHI relative ----- */
        public static opcode  lbhi = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	LBRANCH( !(CC & (CC_Z|CC_C)) );
            }
        };
        
        /* $23 BLS relative ----- */
        public static opcode  bls = new opcode() {
            public void handler() {
                BRANCH((hd6309.cc & (CC_Z | CC_C)) != 0);
            }
        };

        /* $1023 LBLS relative ----- */
        public static opcode  lbls = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	LBRANCH( (CC&(CC_Z|CC_C)) );
            }
        };

        /* $24 BCC relative ----- */
        public static opcode  bcc = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	BRANCH( !(CC&CC_C) );
              }
        };
      
        /* $1024 LBCC relative ----- */
        public static opcode  lbcc = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	LBRANCH( !(CC&CC_C) );
            }
        };

        /* $25 BCS relative ----- */
        public static opcode  bcs = new opcode() {
            public void handler() {
                BRANCH((hd6309.cc & CC_C) != 0);
            }
        };

        /* $1025 LBCS relative ----- */
        public static opcode  lbcs = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	LBRANCH( (CC&CC_C) );
            }
        };

        /* $26 BNE relative ----- */
        public static opcode  bne = new opcode() {
            public void handler() {
                BRANCH( (hd6309.cc & CC_Z) == 0 );
            }
        };

        /* $1026 LBNE relative ----- */
        public static opcode  lbne = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	LBRANCH( !(CC&CC_Z) );
            }
        };

        /* $27 BEQ relative ----- */
        public static opcode  beq = new opcode() {
            public void handler() {
                BRANCH((hd6309.cc & CC_Z) != 0);
            }
        };

        /* $1027 LBEQ relative ----- */
        public static opcode  lbeq = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	LBRANCH( (CC&CC_Z) );
            }
        };

        /* $28 BVC relative ----- */
        public static opcode  bvc = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	BRANCH( !(CC&CC_V) );
            }
        };

        /* $1028 LBVC relative ----- */
        public static opcode  lbvc = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	LBRANCH( !(CC&CC_V) );
            }
        };

        /* $29 BVS relative ----- */
        public static opcode  bvs = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	BRANCH( (CC&CC_V) );
            }
        };

        /* $1029 LBVS relative ----- */
        public static opcode  lbvs = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	LBRANCH( (CC&CC_V) );
            }
        };

        /* $2A BPL relative ----- */
        public static opcode  bpl = new opcode() {
            public void handler() {
                BRANCH((hd6309.cc & CC_N) == 0);
            }
        };

        /* $102A LBPL relative ----- */
        public static opcode  lbpl = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	LBRANCH( !(CC&CC_N) );
            }
        };

        /* $2B BMI relative ----- */
        public static opcode  bmi = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	BRANCH( (CC&CC_N) );
            }
        };

        /* $102B LBMI relative ----- */
        public static opcode  lbmi = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	LBRANCH( (CC&CC_N) );
            }
        };

        /* $2C BGE relative ----- */
        public static opcode  bge = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	BRANCH( !NXORV );
            }
        };

        /* $102C LBGE relative ----- */
        public static opcode  lbge = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	LBRANCH( !NXORV );
            }
        };

        /* $2D BLT relative ----- */
        public static opcode  blt = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	BRANCH( NXORV );
            }
        };

        /* $102D LBLT relative ----- */
        public static opcode  lblt = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	LBRANCH( NXORV );
            }
        };

        /* $2E BGT relative ----- */
        public static opcode  bgt = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	BRANCH( !(NXORV || (CC&CC_Z)) );
            }
        };

        /* $102E LBGT relative ----- */
        public static opcode  lbgt = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	LBRANCH( !(NXORV || (CC&CC_Z)) );
            }
        };

        /* $2F BLE relative ----- */
        public static opcode  ble = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	BRANCH( (NXORV || (CC&CC_Z)) );
            }
        };

        /* $102F LBLE relative ----- */
        public static opcode  lble = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $1030 addr_r r1 + r2 . r2 */
        /*TODO*///
        public static opcode  addr_r = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        public static opcode  adcr = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $1032 SUBR r1 - r2 . r2 */
        public static opcode  subr = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $1033 SBCR r1 - r2 - C . r2 */
        public static opcode  sbcr = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $1034 ANDR r1 & r2 . r2 */
        public static opcode  andr = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $1035 ORR r1 | r2 . r2 */
        public static opcode  orr = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $1036 EORR r1 ^ r2 . r2 */
        public static opcode  eorr = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $1037 CMPR r1 - r2 */
        public static opcode  cmpr = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $1138 TFM R0+,R1+ */
        public static opcode  tfmpp = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $1139 TFM R0-,R1- */
        public static opcode  tfmmm = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $113A TFM R0+,R1 */
        public static opcode  tfmpc = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $113B TFM R0,R1+ */
        public static opcode  tfmcp = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $30 LEAX indexed --*-- */
        public static opcode  leax = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	X = EA;
        /*TODO*///	CLR_Z;
        /*TODO*///	SET_Z(X);
            }
        };

        /* $31 LEAY indexed --*-- */
        public static opcode  leay = new opcode() {
            public void handler() {                
                fetch_effective_address();
                hd6309.y = ea & 0xFFFF;
                CLR_Z();
                SET_Z(hd6309.y);
            }
        };

        /* $32 LEAS indexed ----- */
        public static opcode  leas = new opcode() {
            public void handler() {
                fetch_effective_address();
                hd6309.s = ea & 0xFFFF;
                hd6309.int_state |= HD6309_LDS;
            }
        };

        /* $33 LEAU indexed ----- */
        public static opcode  leau = new opcode() {
            public void handler() {
                fetch_effective_address();
                hd6309.u = ea & 0xFFFF;
            }
        };

        /* $34 PSHS inherent ----- */
        public static opcode  pshs = new opcode() {
            public void handler() {
                
                int t = IMMBYTE();
        	if ((t & 0x80) != 0) { PUSHWORD(hd6309.pc); hd6309_ICount[0] -= 2; }
        	if ((t & 0x40) != 0) { PUSHWORD(hd6309.u);  hd6309_ICount[0] -= 2; }
        	if ((t & 0x20) != 0) { PUSHWORD(hd6309.y);  hd6309_ICount[0] -= 2; }
        	if ((t & 0x10) != 0) { PUSHWORD(hd6309.x);  hd6309_ICount[0] -= 2; }
        	if ((t & 0x08) != 0) { PUSHBYTE(hd6309.dp);  hd6309_ICount[0] -= 1; }
        	if ((t & 0x04) != 0) { PUSHBYTE(hd6309.b);   hd6309_ICount[0] -= 1; }
        	if ((t & 0x02) != 0) { PUSHBYTE(hd6309.a);   hd6309_ICount[0] -= 1; }
        	if ((t & 0x01) != 0) { PUSHBYTE(hd6309.cc);  hd6309_ICount[0] -= 1; }
            }
        };

        /* $1038 PSHSW inherent ----- */
        public static opcode  pshsw = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PUSHWORD(pW);
            }
        };

        /* $103a PSHUW inherent ----- */
        public static opcode  pshuw = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PSHUWORD(pW);
            }
        };

        /* $35 PULS inherent ----- */
        public static opcode  puls = new opcode() {
            public void handler() {
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
                int t = IMMBYTE();
                if ((t & 0x01) != 0) {
                    hd6309.cc = (PULLBYTE());
                    hd6309_ICount[0] -= 1;
                }
                if ((t & 0x02) != 0) {
                    hd6309.a = (PULLBYTE());
                    hd6309_ICount[0] -= 1;
                }
                if ((t & 0x04) != 0) {
                    hd6309.b = (PULLBYTE());
                    hd6309_ICount[0] -= 1;
                }
                if ((t & 0x08) != 0) {
                    hd6309.dp = (PULLBYTE());
                    hd6309_ICount[0] -= 1;
                }
                if ((t & 0x10) != 0) {
                    hd6309.x = (PULLWORD());
                    hd6309_ICount[0] -= 2;
                }
                if ((t & 0x20) != 0) {
                    hd6309.y = (PULLWORD());
                    hd6309_ICount[0] -= 2;
                }
                if ((t & 0x40) != 0) {
                    hd6309.u = (PULLWORD());
                    hd6309_ICount[0] -= 2;
                }
                if ((t & 0x80) != 0) {
                    hd6309.pc = (PULLWORD());
                    CHANGE_PC();
                    hd6309_ICount[0] -= 2;
                }

                /* HJB 990225: moved check after all PULLs */
                if ((t & 0x01) != 0) {
                    CHECK_IRQ_LINES();
                }
            }
        };

        /* $1039 PULSW inherent ----- */
        public static opcode  pulsw = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PULLWORD(W);
            }
        };

        /* $103b PULUW inherent ----- */
        public static opcode  puluw = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PULUWORD(W);
            }
        };

        /* $36 PSHU inherent ----- */
        public static opcode  pshu = new opcode() {
            public void handler() {
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
                int t = IMMBYTE();
                if ((t & 0x80) != 0) {
                    PSHUWORD(hd6309.pc);
                    hd6309_ICount[0] -= 2;
                }
                if ((t & 0x40) != 0) {
                    PSHUWORD(hd6309.s);
                    hd6309_ICount[0] -= 2;
                }
                if ((t & 0x20) != 0) {
                    PSHUWORD(hd6309.y);
                    hd6309_ICount[0] -= 2;
                }
                if ((t & 0x10) != 0) {
                    PSHUWORD(hd6309.x);
                    hd6309_ICount[0] -= 2;
                }
                if ((t & 0x08) != 0) {
                    PSHUBYTE(hd6309.dp);
                    hd6309_ICount[0] -= 1;
                }
                if ((t & 0x04) != 0) {
                    PSHUBYTE(hd6309.b);
                    hd6309_ICount[0] -= 1;
                }
                if ((t & 0x02) != 0) {
                    PSHUBYTE(hd6309.a);
                    hd6309_ICount[0] -= 1;
                }
                if ((t & 0x01) != 0) {
                    PSHUBYTE(hd6309.cc);
                    hd6309_ICount[0] -= 1;
                }
            }
        };

        /*TODO*////* 37 PULU inherent ----- */
        public static opcode  pulu = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $38 ILLEGAL */
        /*TODO*///
        /* $39 RTS inherent ----- */
        public static opcode  rts = new opcode() {
            public void handler() {                
                hd6309.pc = PULLWORD() & 0xFFFF;
                CHANGE_PC();
            }
        };

        /* $3A ABX inherent ----- */
        public static opcode  abx = new opcode() {
            public void handler() {
                hd6309.x = (hd6309.x + hd6309.b) & 0xFFFF;
            }
        };

        /* $3B RTI inherent ##### */
        public static opcode rti = new opcode() {
        public void handler() {
            int t;
            hd6309.cc = (PULLBYTE());
            t = hd6309.cc & CC_E;
            /* HJB 990225: entire state saved? */
            if (t != 0) {
                hd6309_ICount[0] -= 9;
                hd6309.a = (PULLBYTE());
                hd6309.b = (PULLBYTE());
                hd6309.dp = (PULLBYTE());
                hd6309.x = (PULLWORD());
                hd6309.y = (PULLWORD());
                hd6309.u = (PULLWORD());
            }
            hd6309.pc = (PULLWORD());
            CHANGE_PC();
            CHECK_IRQ_LINES();
        }
    };
        public static opcode  rti_2 = new opcode() {
            public void handler() {        
                int t;
                hd6309.cc = (PULLBYTE());
                t = hd6309.cc & CC_E;
                /* HJB 990225: entire state saved? */
                if (t != 0) {
                    hd6309_ICount[0] -= 9;
                    hd6309.a = (PULLBYTE());
                    hd6309.b = (PULLBYTE());
                    if ((hd6309.md & MD_EM) != 0)
                    {
                            hd6309.e = PULLBYTE();
                            hd6309.f = PULLBYTE();
                            hd6309_ICount[0] -= 2;
                    }
                    hd6309.dp = (PULLBYTE());
                    hd6309.x = (PULLWORD());
                    hd6309.y = (PULLWORD());
                    hd6309.u = (PULLWORD());
                }
                hd6309.pc = (PULLWORD());
                CHANGE_PC();
                CHECK_IRQ_LINES();
            }
        };

        /* $3C CWAI inherent ----1 */
        public static opcode  cwai = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $3D MUL inherent --*-@ */
        public static opcode  mul = new opcode() {
            public void handler() {
        /*TODO*///	UINT16 t;
        /*TODO*///	t = A * B;
        /*TODO*///	CLR_ZC; SET_Z16(t); if ((t & 0x80) != 0) SEC;
        /*TODO*///	D = t;
                int t;
                t = ((hd6309.a & 0xff) * (hd6309.b & 0xff)) & 0xFFFF;
                CLR_ZC();
                SET_Z16(t);
                if ((t & 0x80) != 0) {
                    SEC();
                }
                setDreg(t);
            }
        };

        /* $3E ILLEGAL */
        /*TODO*///
        /* $3F SWI (SWI2 SWI3) absolute indirect ----- */
        public static opcode  swi = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $1130 BAND */
        /*TODO*///
        /*TODO*///#define decodePB_tReg(n)	((n)&3)
        /*TODO*///#define decodePB_src(n) 	(((n)>>2)&7)
        /*TODO*///#define decodePB_dst(n) 	(((n)>>5)&7)
        /*TODO*///
        /*TODO*///static UBytePtr 	regTable[4] = { &(CC), &(A), &(B), &(E) };
        /*TODO*///
        /*TODO*///static UINT8	bitTable[] = { 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80 };
        /*TODO*///
        public static opcode  band = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $1131 BIAND */
        /*TODO*///
        public static opcode  biand = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $1132 BOR */
        /*TODO*///
        public static opcode  bor = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
               }
        };

        /* $1133 BIOR */
        /*TODO*///
        public static opcode  bior = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $1134 BEOR */
        /*TODO*///
        public static opcode  beor = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $1135 BIEOR */
        /*TODO*///
        public static opcode  bieor = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $1133 LDBT */
        /*TODO*///
        public static opcode  ldbt = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $1134 STBT */
        /*TODO*///
        public static opcode  stbt = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $103F SWI2 absolute indirect ----- */
        public static opcode  swi2 = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $113F SWI3 absolute indirect ----- */
        public static opcode  swi3 = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $40 NEGA inherent ?**** */
        public static opcode  nega = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 r;
        /*TODO*///	r = -A;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(0,A,r);
        /*TODO*///	A = r;
            }
        };

        /* $41 ILLEGAL */
        /*TODO*///
        /* $42 ILLEGAL */
        /*TODO*///
        /* $43 COMA inherent -**01 */
        public static opcode  coma = new opcode() {
            public void handler() {
                hd6309.a = ~hd6309.a & 0xFF;
                CLR_NZV();
                SET_NZ8(hd6309.a);
                SEC();        
            }
        };

        /* $44 LSRA inherent -0*-* */
        public static opcode  lsra = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZC;
        /*TODO*///	CC |= (A & CC_C);
        /*TODO*///	A >>= 1;
        /*TODO*///	SET_Z8(A);
            }
        };

        /* $45 ILLEGAL */
        /*TODO*///
        /* $46 RORA inherent -**-* */
        public static opcode  rora = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 r;
        /*TODO*///	r = (CC & CC_C) << 7;
        /*TODO*///	CLR_NZC;
        /*TODO*///	CC |= (A & CC_C);
        /*TODO*///	r |= A >> 1;
        /*TODO*///	SET_NZ8(r);
        /*TODO*///	A = r;
            }
        };

        /* $47 ASRA inherent ?**-* */
        public static opcode  asra = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZC;
        /*TODO*///	CC |= (A & CC_C);
        /*TODO*///	A = (A & 0x80) | (A >> 1);
        /*TODO*///	SET_NZ8(A);
            }
        };

        /* $48 ASLA inherent ?**** */
        public static opcode  asla = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 r;
        /*TODO*///	r = A << 1;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(A,A,r);
        /*TODO*///	A = r;
            }
        };

        /* $49 ROLA inherent -**** */
        public static opcode  rola = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	t = A;
        /*TODO*///	r = (CC & CC_C) | (t<<1);
        /*TODO*///	CLR_NZVC; SET_FLAGS8(t,t,r);
        /*TODO*///	A = r;
            }
        };

        /* $4A DECA inherent -***- */
        public static opcode  deca = new opcode() {
            public void handler() {
                hd6309.a = (hd6309.a - 1) & 0xFF;//--A;
                CLR_NZV();
                SET_FLAGS8D(hd6309.a);
            }
        };

        /* $4B ILLEGAL */
        /*TODO*///
        /* $4C INCA inherent -***- */
        public static opcode  inca = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	++A;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_FLAGS8I(A);
            }
        };

        /* $4D TSTA inherent -**0- */
        public static opcode  tsta = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(A);
            }
        };

        /* $4E ILLEGAL */
        /*TODO*///
        /* $4F CLRA inherent -0100 */
        public static opcode  clra = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
        
                hd6309.a = 0;
                CLR_NZVC();
                SEZ();
            }
        };

        /* $50 NEGB inherent ?**** */
        public static opcode  negb = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 r;
        /*TODO*///	r = -B;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(0,B,r);
        /*TODO*///	B = r;
            }
        };

        /* $1040 NEGD inherent ?**** */
        public static opcode  negd = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r;
        /*TODO*///	r = -D;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(0,D,r);
        /*TODO*///	D = r;
            }
        };

        /* $51 ILLEGAL */
        /*TODO*///
        /* $52 ILLEGAL */
        /*TODO*///
        /* $53 COMB inherent -**01 */
        public static opcode  comb = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	B = ~B;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(B);
        /*TODO*///	SEC;
            }
        };

        /* $1143 COME inherent -**01 */
        public static opcode  come = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	E = ~E;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(E);
        /*TODO*///	SEC;
            }
        };

        /* $1153 COMF inherent -**01 */
        public static opcode  comf = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	F = ~F;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(F);
        /*TODO*///	SEC;
            }
        };

        /* $1043 COMD inherent -**01 */
        public static opcode  comd = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	D = ~D;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
        /*TODO*///	SEC;
            }
        };

        /* $1053 COMW inherent -**01 */
        public static opcode  comw = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	W = ~W;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(W);
        /*TODO*///	SEC;
            }
        };

        /* $54 LSRB inherent -0*-* */
        public static opcode  lsrb = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZC;
        /*TODO*///	CC |= (B & CC_C);
        /*TODO*///	B >>= 1;
        /*TODO*///	SET_Z8(B);
            }
        };

        /* $1044 LSRD inherent -0*-* */
        public static opcode  lsrd = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZC;
        /*TODO*///	CC |= (B & CC_C);
        /*TODO*///	D >>= 1;
        /*TODO*///	SET_Z16(D);
            }
        };

        /* $1054 LSRW inherent -0*-* */
        public static opcode  lsrw = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZC;
        /*TODO*///	CC |= (F & CC_C);
        /*TODO*///	W >>= 1;
        /*TODO*///	SET_Z16(W);
            }
        };

        /* $55 ILLEGAL */
        /*TODO*///
        /* $56 RORB inherent -**-* */
        public static opcode  rorb = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 r;
        /*TODO*///	r = (CC & CC_C) << 7;
        /*TODO*///	CLR_NZC;
        /*TODO*///	CC |= (B & CC_C);
        /*TODO*///	r |= B >> 1;
        /*TODO*///	SET_NZ8(r);
        /*TODO*///	B = r;
            }
        };

        /* $1046 RORD inherent -**-* */
        public static opcode  rord = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 r;
        /*TODO*///	r = (CC & CC_C) << 15;
        /*TODO*///	CLR_NZC;
        /*TODO*///	CC |= (D & CC_C);
        /*TODO*///	r |= D >> 1;
        /*TODO*///	SET_NZ16(r);
        /*TODO*///	D = r;
            }
        };

        /* $1056 RORW inherent -**-* */
        public static opcode  rorw = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 r;
        /*TODO*///	r = (CC & CC_C) << 15;
        /*TODO*///	CLR_NZC;
        /*TODO*///	CC |= (W & CC_C);
        /*TODO*///	r |= W >> 1;
        /*TODO*///	SET_NZ16(r);
        /*TODO*///	W = r;
            }
        };

        /* $57 ASRB inherent ?**-* */
        public static opcode  asrb = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZC;
        /*TODO*///	CC |= (B & CC_C);
        /*TODO*///	B= (B & 0x80) | (B >> 1);
        /*TODO*///	SET_NZ8(B);
            }
        };

        /* $1047 ASRD inherent ?**-* */
        public static opcode  asrd = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZC;
        /*TODO*///	CC |= (D & CC_C);
        /*TODO*///	D= (D & 0x8000) | (D >> 1);
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $58 ASLB inherent ?**** */
        public static opcode  aslb = new opcode() {
            public void handler() {
                int r = (hd6309.b << 1) & 0xFFFF;
                CLR_NZVC();
                SET_FLAGS8(hd6309.b, hd6309.b, r);
                hd6309.b = r & 0xFF;
            }
        };

        /* $1048 ASLD inherent ?**** */
        public static opcode  asld = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r;
        /*TODO*///	r = D << 1;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(D,D,r);
        /*TODO*///	D = r;
            }
        };

        /* $59 ROLB inherent -**** */
        public static opcode  rolb = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	t = B;
        /*TODO*///	r = CC & CC_C;
        /*TODO*///	r |= t << 1;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(t,t,r);
        /*TODO*///	B = r;
            }
        };

        /* $1049 ROLD inherent -**** */
        public static opcode  rold = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 t,r;
        /*TODO*///	t = D;
        /*TODO*///	r = CC & CC_C;
        /*TODO*///	r |= t << 1;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(t,t,r);
        /*TODO*///	D = r;
            }
        };

        /* $1059 ROLW inherent -**** */
        public static opcode  rolw = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 t,r;
        /*TODO*///	t = W;
        /*TODO*///	r = CC & CC_C;
        /*TODO*///	r |= t << 1;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(t,t,r);
        /*TODO*///	W = r;
            }
        };

        /* $5A DECB inherent -***- */
        public static opcode  decb = new opcode() {
            public void handler() {
                hd6309.b = (hd6309.b - 1) & 0xFF;
                CLR_NZV();
                SET_FLAGS8D(hd6309.b);
            }
        };

        /* $114a DECE inherent -***- */
        public static opcode  dece = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	--E;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_FLAGS8D(E);
            }
        };

        /* $115a DECF inherent -***- */
        public static opcode  decf = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	--F;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_FLAGS8D(F);
            }
        };

        /* $104a DECD inherent -***- */
        public static opcode  decd = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r;
        /*TODO*///	r = D - 1;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(D,D,r)
        /*TODO*///	D = r;
            }
        };

        /* $105a DECW inherent -***- */
        public static opcode  decw = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r;
        /*TODO*///	r = W - 1;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(W,W,r)
        /*TODO*///	W = r;
            }
        };

        /* $5B ILLEGAL */
        /*TODO*///
        /* $5C INCB inherent -***- */
        public static opcode  incb = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	++B;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_FLAGS8I(B);
            }
        };

        /* $114c INCE inherent -***- */
        public static opcode  ince = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	++E;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_FLAGS8I(E);
            }
        };

        /* $115c INCF inherent -***- */
        public static opcode  incf = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	++F;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_FLAGS8I(F);
            }
        };

        /* $104c INCD inherent -***- */
        public static opcode  incd = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r;
        /*TODO*///	r = D + 1;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(D,D,r)
        /*TODO*///	D = r;
            }
        };

        /* $105c INCW inherent -***- */
        public static opcode  incw = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r;
        /*TODO*///	r = W + 1;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(W,W,r)
        /*TODO*///	W = r;
            }
        };

        /* $5D TSTB inherent -**0- */
        public static opcode  tstb = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(B);
            }
        };

        /* $104d TSTD inherent -**0- */
        public static opcode  tstd = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $105d TSTW inherent -**0- */
        public static opcode  tstw = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(W);
            }
        };

        /* $114d TSTE inherent -**0- */
        public static opcode  tste = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(E);
            }
        };

        /* $115d TSTF inherent -**0- */
        public static opcode  tstf = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(F);
            }
        };

        /* $5E ILLEGAL */
        /*TODO*///
        /* $5F CLRB inherent -0100 */
        public static opcode  clrb = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	B = 0;
        /*TODO*///	CLR_NZVC; SEZ;
            }
        };

        /* $104f CLRD inherent -0100 */
        public static opcode  clrd = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	D = 0;
        /*TODO*///	CLR_NZVC; SEZ;
            }
        };

        /* $114f CLRE inherent -0100 */
        public static opcode  clre = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	E = 0;
        /*TODO*///	CLR_NZVC; SEZ;
            }
        };

        /* $115f CLRF inherent -0100 */
        public static opcode  clrf = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	F = 0;
        /*TODO*///	CLR_NZVC; SEZ;
            }
        };

        /* $105f CLRW inherent -0100 */
        public static opcode  clrw = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	W = 0;
        /*TODO*///	CLR_NZVC; SEZ;
            }
        };

        /* $60 NEG indexed ?**** */
        public static opcode  neg_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 r,t;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r=-t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(0,t,r);
        /*TODO*///	WM(EAD,r);
            }
        };

        /* $61 OIM indexed */
        public static opcode  oim_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8	r,im;
        /*TODO*///	IMMBYTE(im);
        /*TODO*///	fetch_effective_address();
        /*TODO*///	r = im | RM(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
        /*TODO*///	WM(EAD,r);
            }
        };

        /* $62 AIM indexed */
        public static opcode  aim_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8	r,im;
        /*TODO*///	IMMBYTE(im);
        /*TODO*///	fetch_effective_address();
        /*TODO*///	r = im & RM(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
        /*TODO*///	WM(EAD,r);
            }
        };

        /* $63 COM indexed -**01 */
        public static opcode  com_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = ~RM(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(t);
        /*TODO*///	SEC;
        /*TODO*///	WM(EAD,t);
            }
        };

        /* $64 LSR indexed -0*-* */
        public static opcode  lsr_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t=RM(EAD);
        /*TODO*///	CLR_NZC;
        /*TODO*///	CC |= (t & CC_C);
        /*TODO*///	t>>=1; SET_Z8(t);
        /*TODO*///	WM(EAD,t);
            }
        };

        /* $65 EIM indexed */
        public static opcode  eim_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8	r,im;
        /*TODO*///	IMMBYTE(im);
        /*TODO*///	fetch_effective_address();
        /*TODO*///	r = im ^ RM(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
        /*TODO*///	WM(EAD,r);
            }
        };

        /* $66 ROR indexed -**-* */
        public static opcode  ror_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t=RM(EAD);
        /*TODO*///	r = (CC & CC_C) << 7;
        /*TODO*///	CLR_NZC;
        /*TODO*///	CC |= (t & CC_C);
        /*TODO*///	r |= t>>1; SET_NZ8(r);
        /*TODO*///	WM(EAD,r);
            }
        };

        /* $67 ASR indexed ?**-* */
        public static opcode  asr_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t=RM(EAD);
        /*TODO*///	CLR_NZC;
        /*TODO*///	CC |= (t & CC_C);
        /*TODO*///	t=(t&0x80)|(t>>1);
        /*TODO*///	SET_NZ8(t);
        /*TODO*///	WM(EAD,t);
            }
        };

        /* $68 ASL indexed ?**** */
        public static opcode  asl_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t=RM(EAD);
        /*TODO*///	r = t << 1;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(t,t,r);
        /*TODO*///	WM(EAD,r);
            }
        };

        /* $69 ROL indexed -**** */
        public static opcode  rol_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t=RM(EAD);
        /*TODO*///	r = CC & CC_C;
        /*TODO*///	r |= t << 1;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(t,t,r);
        /*TODO*///	WM(EAD,r);
            }
        };

        /* $6A DEC indexed -***- */
        public static opcode  dec_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD) - 1;
        /*TODO*///	CLR_NZV; SET_FLAGS8D(t);
        /*TODO*///	WM(EAD,t);
            }
        };

        /* $6B TIM indexed */
        public static opcode  tim_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8	r,im,m;
        /*TODO*///	IMMBYTE(im);
        /*TODO*///	fetch_effective_address();
        /*TODO*///	m = RM(EAD);
        /*TODO*///	r = im & m;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
            }
        };

        /* $6C INC indexed -***- */
        public static opcode  inc_ix = new opcode() {
            public void handler() {
                fetch_effective_address();
                int t = (RM(ea) + 1) & 0xFF;
                CLR_NZV();
                SET_FLAGS8I(t);
                WM(ea, t);
            }
        };

        /* $6D TST indexed -**0- */
        public static opcode  tst_ix = new opcode() {
            public void handler() {
                fetch_effective_address();
                int t = RM(ea);
                CLR_NZV();
                SET_NZ8(t);
            }
        };

        /* $6E JMP indexed ----- */
        public static opcode  jmp_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	PCD = EAD;
        /*TODO*///	CHANGE_PC;
            }
        };

        /* $6F CLR indexed -0100 */
        public static opcode  clr_ix = new opcode() {
            public void handler() {
                fetch_effective_address();
                WM(ea, 0);
                CLR_NZVC();
                SEZ();
            }
        };

        /* $70 NEG extended ?**** */
        public static opcode  neg_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 r,t;
        /*TODO*///	EXTBYTE(t); r=-t;
        /*TODO*///	CLR_NZVC; SET_FLAGS8(0,t,r);
        /*TODO*///	WM(EAD,r);
            }
        };

        /* $71 OIM extended */
        public static opcode  oim_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8	r,t,im;
        /*TODO*///	IMMBYTE(im);
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = im | t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
        /*TODO*///	WM(EAD,r);
            }
        };

        /* $72 AIM extended */
        public static opcode  aim_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8	r,t,im;
        /*TODO*///	IMMBYTE(im);
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = im & t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
        /*TODO*///	WM(EAD,r);
            }
        };

        /* $73 COM extended -**01 */
        public static opcode  com_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	EXTBYTE(t); t = ~t;
        /*TODO*///	CLR_NZV; SET_NZ8(t); SEC;
        /*TODO*///	WM(EAD,t);
            }
        };

        /* $74 LSR extended -0*-* */
        public static opcode  lsr_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	EXTBYTE(t); CLR_NZC; CC |= (t & CC_C);
        /*TODO*///	t>>=1; SET_Z8(t);
        /*TODO*///	WM(EAD,t);
            }
        };

        /* $75 EIM extended */
        public static opcode  eim_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8	r,t,im;
        /*TODO*///	IMMBYTE(im);
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = im ^ t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
        /*TODO*///	WM(EAD,r);
            }
        };

        /* $76 ROR extended -**-* */
        public static opcode  ror_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t,r;
        /*TODO*///	EXTBYTE(t); r=(CC & CC_C) << 7;
        /*TODO*///	CLR_NZC; CC |= (t & CC_C);
        /*TODO*///	r |= t>>1; SET_NZ8(r);
        /*TODO*///	WM(EAD,r);
            }
        };

        /* $77 ASR extended ?**-* */
        public static opcode  asr_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	EXTBYTE(t); CLR_NZC; CC |= (t & CC_C);
        /*TODO*///	t=(t&0x80)|(t>>1);
        /*TODO*///	SET_NZ8(t);
        /*TODO*///	WM(EAD,t);
            }
        };

        /* $78 ASL extended ?**** */
        public static opcode  asl_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	EXTBYTE(t); r=t<<1;
        /*TODO*///	CLR_NZVC; SET_FLAGS8(t,t,r);
        /*TODO*///	WM(EAD,r);
            }
        };

        /* $79 ROL extended -**** */
        public static opcode  rol_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	EXTBYTE(t); r = (CC & CC_C) | (t << 1);
        /*TODO*///	CLR_NZVC; SET_FLAGS8(t,t,r);
        /*TODO*///	WM(EAD,r);
            }
        };

        /* $7A DEC extended -***- */
        public static opcode  dec_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	EXTBYTE(t); --t;
        /*TODO*///	CLR_NZV; SET_FLAGS8D(t);
        /*TODO*///	WM(EAD,t);
            }
        };

        /* $7B TIM extended */
        public static opcode  tim_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8	r,t,im;
        /*TODO*///	IMMBYTE(im);
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = im & t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
            }
        };

        /* $7C INC extended -***- */
        public static opcode  inc_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	EXTBYTE(t); ++t;
        /*TODO*///	CLR_NZV; SET_FLAGS8I(t);
        /*TODO*///	WM(EAD,t);
            }
        };

        /* $7D TST extended -**0- */
        public static opcode  tst_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	EXTBYTE(t); CLR_NZV; SET_NZ8(t);
            }
        };

        /* $7E JMP extended ----- */
        public static opcode  jmp_ex = new opcode() {
            public void handler() {        
                EXTENDED();
                hd6309.pc = ea & 0xFFFF;
                CHANGE_PC();
            }
        };

        /* $7F CLR extended -0100 */
        public static opcode  clr_ex = new opcode() {
            public void handler() {
                EXTENDED();
                WM(ea, 0);
                CLR_NZVC();
                SEZ();
            }
        };

        /* $80 SUBA immediate ?**** */
        public static opcode  suba_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	r = A - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(A,t,r);
        /*TODO*///	A = r;
            }
        };

        /* $81 CMPA immediate ?**** */
        public static opcode  cmpa_im = new opcode() {
            public void handler() {
                int t, r;
                t = IMMBYTE();
                r = (hd6309.a - t) & 0xFFFF;
                CLR_NZVC();
                SET_FLAGS8(hd6309.a, t, r);
            }
        };

        /* $82 SBCA immediate ?**** */
        public static opcode  sbca_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	r = A - t - (CC & CC_C);
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(A,t,r);
        /*TODO*///	A = r;
            }
        };

        /* $83 SUBD (CMPD CMPU) immediate -**** */
        public static opcode  subd_im = new opcode() {
            public void handler() {
                int r, d;
                int b;
                b = IMMWORD();
                d = getDreg();
                r = d - b;
                CLR_NZVC();
                SET_FLAGS16(d, b, r);
                setDreg(r);
            }
        };

        /* $1080 SUBW immediate -**** */
        public static opcode  subw_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	IMMWORD(b);
        /*TODO*///	d = W;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
        /*TODO*///	W = r;
            }
        };

        /* $1083 CMPD immediate -**** */
        public static opcode  cmpd_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	IMMWORD(b);
        /*TODO*///	d = D;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $1081 CMPW immediate -**** */
        public static opcode  cmpw_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	IMMWORD(b);
        /*TODO*///	d = W;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $1183 CMPU immediate -**** */
        public static opcode  cmpu_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r, d;
        /*TODO*///	PAIR b;
        /*TODO*///	IMMWORD(b);
        /*TODO*///	d = U;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $84 ANDA immediate -**0- */
        public static opcode  anda_im = new opcode() {
            public void handler() {
                int t = IMMBYTE();
                hd6309.a = (hd6309.a & t) & 0xFF;
                CLR_NZV();
                SET_NZ8(hd6309.a);
            }
        };

        /* $85 BITA immediate -**0- */
        public static opcode  bita_im = new opcode() {
            public void handler() {
                int t, r;
                t = IMMBYTE();
                r = (hd6309.a & t) & 0xFF;
                CLR_NZV();
                SET_NZ8(r);
            }
        };

        /* $86 LDA immediate -**0- */
        public static opcode  lda_im = new opcode() {
            public void handler() {
                hd6309.a = IMMBYTE();
                CLR_NZV();
                SET_NZ8(hd6309.a);
            }
        };

        /* $88 EORA immediate -**0- */
        public static opcode  eora_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	A ^= t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(A);
            }
        };

        /* $89 ADCA immediate ***** */
        public static opcode  adca_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	r = A + t + (CC & CC_C);
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(A,t,r);
        /*TODO*///	SET_H(A,t,r);
        /*TODO*///	A = r;
            }
        };

        /* $8A ORA immediate -**0- */
        public static opcode  ora_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	A |= t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(A);
            }
        };

        /* $8B ADDA immediate ***** */
        public static opcode  adda_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	r = A + t;
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(A,t,r);
        /*TODO*///	SET_H(A,t,r);
        /*TODO*///	A = r;
            }
        };

        /* $8C CMPX (CMPY CMPS) immediate -**** */
        public static opcode  cmpx_im = new opcode() {
            public void handler() {
                int/*UINT32*/ r, d;
                int b = IMMWORD();
                d = hd6309.x;
                r = (d - b);
                CLR_NZVC();
                SET_FLAGS16(d, b, r);
            }
        };

        /* $108C CMPY immediate -**** */
        public static opcode  cmpy_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	IMMWORD(b);
        /*TODO*///	d = Y;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $118C CMPS immediate -**** */
        public static opcode  cmps_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	IMMWORD(b);
        /*TODO*///	d = S;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $8D BSR ----- */
        public static opcode  bsr = new opcode() {
            public void handler() {                
                int t = IMMBYTE();
                PUSHWORD(hd6309.pc);
                hd6309.pc = (hd6309.pc + SIGNED(t)) & 0xFFFF;
                CHANGE_PC();
            }
        };

        /* $8E LDX (LDY) immediate -**0- */
        public static opcode  ldx_im = new opcode() {
            public void handler() {                
                hd6309.x = IMMWORD();
                CLR_NZV();
                SET_NZ16(hd6309.x);
            }
        };

        /* $CD LDQ immediate -**0- */
        public static opcode  ldq_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR	q;
        /*TODO*///
        /*TODO*///	IMMLONG(q);
        /*TODO*///	D = q.w.h;
        /*TODO*///	W = q.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_N8(A);
        /*TODO*///	SET_Z(q.d);
            }
        };

        /* $108E LDY immediate -**0- */
        public static opcode  ldy_im = new opcode() {
            public void handler() {                        
                hd6309.y = IMMWORD();
                CLR_NZV();
                SET_NZ16(hd6309.y);
            }
        };

        /* $118f MULD immediate */
        public static opcode  muld_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR t, q;
        /*TODO*///
        /*TODO*///	IMMWORD( t );
        /*TODO*///	q.d = (INT16) D * (INT16)t.w.l;
        /*TODO*///	D = q.w.h;
        /*TODO*///	W = q.w.l;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $118d DIVD immediate */
        public static opcode  divd_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $118e DIVQ immediate */
        public static opcode  divq_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $90 SUBA direct ?**** */
        public static opcode  suba_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = A - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(A,t,r);
        /*TODO*///	A = r;
            }
        };

        /* $91 CMPA direct ?**** */
        public static opcode  cmpa_di = new opcode() {
            public void handler() {
                int t, r;
                t = DIRBYTE();
                r = (hd6309.a - t) & 0xFFFF;
                CLR_NZVC();
                SET_FLAGS8(hd6309.a, t, r);
            }
        };

        /* $92 SBCA direct ?**** */
        public static opcode  sbca_di = new opcode() {
            public void handler() {        
                int t, r;
                t = DIRBYTE();
                r = (hd6309.a - t - (hd6309.cc & CC_C)) & 0xFFFF;
                CLR_NZVC();
                SET_FLAGS8(hd6309.a, t, r);
                hd6309.a = r & 0xFF;
            }
        };

        /* $93 SUBD (CMPD CMPU) direct -**** */
        public static opcode  subd_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	DIRWORD(b);
        /*TODO*///	d = D;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
        /*TODO*///	D = r;
            }
        };

        /* $1090 SUBW direct -**** */
        public static opcode  subw_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	DIRWORD(b);
        /*TODO*///	d = W;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
        /*TODO*///	W = r;
            }
        };

        /* $1093 CMPD direct -**** */
        public static opcode  cmpd_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	DIRWORD(b);
        /*TODO*///	d = D;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $1091 CMPW direct -**** */
        public static opcode  cmpw_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	DIRWORD(b);
        /*TODO*///	d = W;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $1193 CMPU direct -**** */
        public static opcode  cmpu_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	DIRWORD(b);
        /*TODO*///	d = U;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(U,b.d,r);
            }
        };

        /* $94 ANDA direct -**0- */
        public static opcode  anda_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	A &= t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(A);
            }
        };

        /* $95 BITA direct -**0- */
        public static opcode  bita_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = A & t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
            }
        };

        /* $96 LDA direct -**0- */
        public static opcode  lda_di = new opcode() {
            public void handler() {
                hd6309.a = DIRBYTE();
                CLR_NZV();
                SET_NZ8(hd6309.a);
            }
        };

        /* $113d LDMD direct -**0- */
        public static opcode  ldmd_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	DIRBYTE(MD);
        /*TODO*///	UpdateState();
            }
        };

        /* $97 STA direct -**0- */
        public static opcode  sta_di = new opcode() {
            public void handler() {
                CLR_NZV();
                SET_NZ8(hd6309.a);
                DIRECT();
                WM(ea, hd6309.a);
            }
        };

        /* $98 EORA direct -**0- */
        public static opcode  eora_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	A ^= t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(A);
            }
        };

        /* $99 ADCA direct ***** */
        public static opcode  adca_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = A + t + (CC & CC_C);
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(A,t,r);
        /*TODO*///	SET_H(A,t,r);
        /*TODO*///	A = r;
            }
        };

        /* $9A ORA direct -**0- */
        public static opcode  ora_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	A |= t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(A);
            }
        };

        /* $9B ADDA direct ***** */
        public static opcode  adda_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = A + t;
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(A,t,r);
        /*TODO*///	SET_H(A,t,r);
        /*TODO*///	A = r;
            }
        };

        /* $9C CMPX (CMPY CMPS) direct -**** */
        public static opcode  cmpx_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	DIRWORD(b);
        /*TODO*///	d = X;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $109C CMPY direct -**** */
        public static opcode  cmpy_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	DIRWORD(b);
        /*TODO*///	d = Y;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $119C CMPS direct -**** */
        public static opcode  cmps_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	DIRWORD(b);
        /*TODO*///	d = S;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $9D JSR direct ----- */
        public static opcode  jsr_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	DIRECT;
        /*TODO*///	PUSHWORD(pPC);
        /*TODO*///	PCD = EAD;
        /*TODO*///	CHANGE_PC;
            }
        };

        /* $9E LDX (LDY) direct -**0- */
        public static opcode  ldx_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	DIRWORD(pX);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(X);
            }
        };

        /* $119f MULD direct -**0- */
        public static opcode  muld_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR	t,q;
        /*TODO*///
        /*TODO*///	DIRWORD(t);
        /*TODO*///	q.d = (INT16) D * (INT16)t.w.l;
        /*TODO*///
        /*TODO*///	D = q.w.h;
        /*TODO*///	W = q.w.l;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $119d DIVD direct -**0- */
        public static opcode  divd_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $119e DIVQ direct -**0- */
        public static opcode  divq_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $10dc LDQ direct -**0- */
        public static opcode  ldq_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR	q;
        /*TODO*///
        /*TODO*///	DIRLONG(q);
        /*TODO*///	D = q.w.h;
        /*TODO*///	W = q.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_N8(A);
        /*TODO*///	SET_Z(q.d);
            }
        };

        /* $109E LDY direct -**0- */
        public static opcode  ldy_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	DIRWORD(pY);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(Y);
            }
        };

        /* $9F STX (STY) direct -**0- */
        public static opcode  stx_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(X);
        /*TODO*///	DIRECT;
        /*TODO*///	WM16(EAD,&pX);
            }
        };

        /* $10dd STQ direct -**0- */
        public static opcode  stq_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR	q;
        /*TODO*///
        /*TODO*///	q.w.h = D;
        /*TODO*///	q.w.l = W;
        /*TODO*///	DIRECT;
        /*TODO*///	WM32(EAD,&q);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_N8(A);
        /*TODO*///	SET_Z(q.d);
            }
        };

        /* $109F STY direct -**0- */
        public static opcode  sty_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(Y);
        /*TODO*///	DIRECT;
        /*TODO*///	WM16(EAD,&pY);
            }
        };

        /* $a0 SUBA indexed ?**** */
        public static opcode  suba_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r = A - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(A,t,r);
        /*TODO*///	A = r;
            }
        };

        /* $a1 CMPA indexed ?**** */
        public static opcode  cmpa_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r = A - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(A,t,r);
            }
        };

        /* $a2 SBCA indexed ?**** */
        public static opcode  sbca_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r = A - t - (CC & CC_C);
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(A,t,r);
        /*TODO*///	A = r;
            }
        };

        /* $a3 SUBD (CMPD CMPU) indexed -**** */
        public static opcode  subd_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	b.d=RM16(EAD);
        /*TODO*///	d = D;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
        /*TODO*///	D = r;
            }
        };

        /* $10a0 SUBW indexed -**** */
        public static opcode  subw_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	b.d=RM16(EAD);
        /*TODO*///	d = W;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
        /*TODO*///	W = r;
            }
        };

        /* $10a3 CMPD indexed -**** */
        public static opcode  cmpd_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	b.d=RM16(EAD);
        /*TODO*///	d = D;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $10a1 CMPW indexed -**** */
        public static opcode  cmpw_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	b.d=RM16(EAD);
        /*TODO*///	d = W;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $11a3 CMPU indexed -**** */
        public static opcode  cmpu_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r;
        /*TODO*///	PAIR b;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	b.d=RM16(EAD);
        /*TODO*///	r = U - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(U,b.d,r);
            }
        };

        /* $a4 ANDA indexed -**0- */
        public static opcode  anda_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	A &= RM(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(A);
            }
        };

        /* $a5 BITA indexed -**0- */
        public static opcode  bita_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	r = A & RM(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
            }
        };

        /* $a6 LDA indexed -**0- */
        public static opcode  lda_ix = new opcode() {
            public void handler() {
                fetch_effective_address();
                hd6309.a = RM(ea) & 0xFF;
                CLR_NZV();
                SET_NZ8(hd6309.a);
            }
        };

        /* $a7 STA indexed -**0- */
        public static opcode  sta_ix = new opcode() {
            public void handler() {
                fetch_effective_address();
                CLR_NZV();
                SET_NZ8(hd6309.a);
                WM(ea, hd6309.a);
            }
        };

        /* $a8 EORA indexed -**0- */
        public static opcode  eora_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	A ^= RM(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(A);
            }
        };

        /* $a9 ADCA indexed ***** */
        public static opcode  adca_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r = A + t + (CC & CC_C);
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(A,t,r);
        /*TODO*///	SET_H(A,t,r);
        /*TODO*///	A = r;
            }
        };

        /* $aA ORA indexed -**0- */
        public static opcode  ora_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	A |= RM(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(A);
            }
        };

        /* $aB ADDA indexed ***** */
        public static opcode  adda_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r = A + t;
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(A,t,r);
        /*TODO*///	SET_H(A,t,r);
        /*TODO*///	A = r;
            }
        };

        /* $aC CMPX (CMPY CMPS) indexed -**** */
        public static opcode  cmpx_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	b.d=RM16(EAD);
        /*TODO*///	d = X;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $10aC CMPY indexed -**** */
        public static opcode  cmpy_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	b.d=RM16(EAD);
        /*TODO*///	d = Y;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $11aC CMPS indexed -**** */
        public static opcode  cmps_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	b.d=RM16(EAD);
        /*TODO*///	d = S;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $aD JSR indexed ----- */
        public static opcode  jsr_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	PUSHWORD(pPC);
        /*TODO*///	PCD = EAD;
        /*TODO*///	CHANGE_PC;
            }
        };

        /* $aE LDX (LDY) indexed -**0- */
        public static opcode  ldx_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	X=RM16(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(X);
            }
        };

        /* $11af MULD indexed -**0- */
        public static opcode  muld_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $11ad DIVD indexed -**0- */
        public static opcode  divd_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $11ae DIVQ indexed -**0- */
        public static opcode  divq_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $10ec LDQ indexed -**0- */
        public static opcode  ldq_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR	q;
        /*TODO*///
        /*TODO*///	fetch_effective_address();
        /*TODO*///	q.d=RM32(EAD);
        /*TODO*///	D = q.w.h;
        /*TODO*///	W = q.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_N8(A);
        /*TODO*///	SET_Z(q.d);
            }
        };

        /* $10aE LDY indexed -**0- */
        public static opcode  ldy_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	Y=RM16(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(Y);
            }
        };

        /* $aF STX (STY) indexed -**0- */
        public static opcode  stx_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(X);
        /*TODO*///	WM16(EAD,&pX);
            }
        };

        /* $10ed STQ indexed -**0- */
        public static opcode  stq_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR	q;
        /*TODO*///
        /*TODO*///	q.w.h = D;
        /*TODO*///	q.w.l = W;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	WM32(EAD,&q);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_N8(A);
        /*TODO*///	SET_Z(q.d);
            }
        };

        /* $10aF STY indexed -**0- */
        public static opcode  sty_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(Y);
        /*TODO*///	WM16(EAD,&pY);
            }
        };

        /* $b0 SUBA extended ?**** */
        public static opcode  suba_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = A - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(A,t,r);
        /*TODO*///	A = r;
            }
        };

        /* $b1 CMPA extended ?**** */
        public static opcode  cmpa_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = A - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(A,t,r);
            }
        };

        /* $b2 SBCA extended ?**** */
        public static opcode  sbca_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = A - t - (CC & CC_C);
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(A,t,r);
        /*TODO*///	A = r;
            }
        };

        /* $b3 SUBD (CMPD CMPU) extended -**** */
        public static opcode  subd_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b = {{0,}};
        /*TODO*///	EXTWORD(b);
        /*TODO*///	d = D;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
        /*TODO*///	D = r;
            }
        };

        /* $10b0 SUBW extended -**** */
        public static opcode  subw_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b = {{0,}};
        /*TODO*///	EXTWORD(b);
        /*TODO*///	d = W;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
        /*TODO*///	W = r;
            }
        };

        /* $10b3 CMPD extended -**** */
        public static opcode  cmpd_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b = {{0,}};
        /*TODO*///	EXTWORD(b);
        /*TODO*///	d = D;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $10b1 CMPW extended -**** */
        public static opcode  cmpw_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b = {{0,}};
        /*TODO*///	EXTWORD(b);
        /*TODO*///	d = W;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $11b3 CMPU extended -**** */
        public static opcode  cmpu_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b = {{0,}};
        /*TODO*///	EXTWORD(b);
        /*TODO*///	d = U;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $b4 ANDA extended -**0- */
        public static opcode  anda_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	A &= t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(A);
            }
        };

        /* $b5 BITA extended -**0- */
        public static opcode  bita_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = A & t;
        /*TODO*///	CLR_NZV; SET_NZ8(r);
            }
        };

        /* $b6 LDA extended -**0- */
        public static opcode  lda_ex = new opcode() {
            public void handler() {
                hd6309.a = EXTBYTE();
                CLR_NZV();
                SET_NZ8(hd6309.a);
            }
        };

        /* $b7 STA extended -**0- */
        public static opcode  sta_ex = new opcode() {
            public void handler() {                
                CLR_NZV();
                SET_NZ8(hd6309.a);
                EXTENDED();
                WM(ea, hd6309.a);
            }
        };

        /* $b8 EORA extended -**0- */
        public static opcode  eora_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	A ^= t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(A);
            }
        };

        /* $b9 ADCA extended ***** */
        public static opcode  adca_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = A + t + (CC & CC_C);
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(A,t,r);
        /*TODO*///	SET_H(A,t,r);
        /*TODO*///	A = r;
            }
        };

        /* $bA ORA extended -**0- */
        public static opcode  ora_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	A |= t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(A);
            }
        };

        /* $bB ADDA extended ***** */
        public static opcode  adda_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = A + t;
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(A,t,r);
        /*TODO*///	SET_H(A,t,r);
        /*TODO*///	A = r;
            }
        };

        /* $bC CMPX (CMPY CMPS) extended -**** */
        public static opcode  cmpx_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b = {{0,}};
        /*TODO*///	EXTWORD(b);
        /*TODO*///	d = X;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $10bC CMPY extended -**** */
        public static opcode  cmpy_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b = {{0,}};
        /*TODO*///	EXTWORD(b);
        /*TODO*///	d = Y;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $11bC CMPS extended -**** */
        public static opcode  cmps_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b = {{0,}};
        /*TODO*///	EXTWORD(b);
        /*TODO*///	d = S;
        /*TODO*///	r = d - b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
            }
        };

        /* $bD JSR extended ----- */
        public static opcode  jsr_ex = new opcode() {
            public void handler() {
                EXTENDED();
                PUSHWORD(hd6309.pc);
                hd6309.pc = ea & 0xFFFF;
                CHANGE_PC();
            }
        };

        /* $bE LDX (LDY) extended -**0- */
        public static opcode  ldx_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	EXTWORD(pX);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(X);
            }
        };

        /* $11bf MULD extended -**0- */
        public static opcode  muld_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR	t, q;
        /*TODO*///
        /*TODO*///	EXTWORD(t);
        /*TODO*///	q.d = (INT16) D * (INT16)t.w.l;
        /*TODO*///
        /*TODO*///	D = q.w.h;
        /*TODO*///	W = q.w.l;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $11bd DIVD extended -**0- */
        public static opcode  divd_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $11be DIVQ extended -**0- */
        public static opcode  divq_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
        };

        /* $10fc LDQ extended -**0- */
        public static opcode  ldq_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR	q;
        /*TODO*///
        /*TODO*///	EXTLONG(q);
        /*TODO*///	D = q.w.h;
        /*TODO*///	W = q.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_N8(A);
        /*TODO*///	SET_Z(q.d);
            }
        };

        /* $10bE LDY extended -**0- */
        public static opcode  ldy_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	EXTWORD(pY);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(Y);
            }
        };

        /* $bF STX (STY) extended -**0- */
        public static opcode  stx_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(X);
        /*TODO*///	EXTENDED;
        /*TODO*///	WM16(EAD,&pX);
            }
        };

        /* $10fd STQ extended -**0- */
        public static opcode  stq_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR	q;
        /*TODO*///
        /*TODO*///	q.w.h = D;
        /*TODO*///	q.w.l = W;
        /*TODO*///	EXTENDED;
        /*TODO*///	WM32(EAD,&q);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_N8(A);
        /*TODO*///	SET_Z(q.d);
            }
        };

        /* $10bF STY extended -**0- */
        public static opcode  sty_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(Y);
        /*TODO*///	EXTENDED;
        /*TODO*///	WM16(EAD,&pY);
            }
        };

        /* $c0 SUBB immediate ?**** */
        public static opcode  subb_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	r = B - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
        /*TODO*///	B = r;
            }
        };

        /* $1180 SUBE immediate ?**** */
        public static opcode  sube_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	r = E - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(E,t,r);
        /*TODO*///	E = r;
            }
        };

        /* $11C0 SUBF immediate ?**** */
        public static opcode  subf_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	r = F - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(F,t,r);
        /*TODO*///	F = r;
            }
        };

        /* $c1 CMPB immediate ?**** */
        public static opcode  cmpb_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	r = B - t;
        /*TODO*///	CLR_NZVC; SET_FLAGS8(B,t,r);
            }
        };

        /* $1181 CMPE immediate ?**** */
        public static opcode  cmpe_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	r = E - t;
        /*TODO*///	CLR_NZVC; SET_FLAGS8(E,t,r);
            }
        };

        /* $11C1 CMPF immediate ?**** */
        public static opcode  cmpf_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	r = F - t;
        /*TODO*///	CLR_NZVC; SET_FLAGS8(F,t,r);
            }
        };

        /* $c2 SBCB immediate ?**** */
        public static opcode  sbcb_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	r = B - t - (CC & CC_C);
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
        /*TODO*///	B = r;
            }
        };

        /* $1082 SBCD immediate ?**** */
        public static opcode  sbcd_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR	t;
        /*TODO*///	UINT32	 r;
        /*TODO*///	IMMWORD(t);
        /*TODO*///	r = D - t.w.l - (CC & CC_C);
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(D,t.w.l,r);
        /*TODO*///	D = r;
            }
        };

        /* $c3 ADDD immediate -**** */
        public static opcode  addd_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	IMMWORD(b);
        /*TODO*///	d = D;
        /*TODO*///	r = d + b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
        /*TODO*///	D = r;
            }
        };

        /* $108b ADDW immediate -**** */
        public static opcode  addw_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	IMMWORD(b);
        /*TODO*///	d = W;
        /*TODO*///	r = d + b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
        /*TODO*///	W = r;
            }
        };

        /* $118b ADDE immediate -**** */
        public static opcode  adde_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	r = E + t;
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(E,t,r);
        /*TODO*///	SET_H(E,t,r);
        /*TODO*///	E = r;
            }
        };

        /* $11Cb ADDF immediate -**** */
        public static opcode  addf_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	r = F + t;
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(F,t,r);
        /*TODO*///	SET_H(F,t,r);
        /*TODO*///	F = r;
            }
        };

        /* $c4 ANDB immediate -**0- */
        public static opcode  andb_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	B &= t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(B);
            }
        };

        /* $1084 ANDD immediate -**0- */
        public static opcode  andd_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR t;
        /*TODO*///	IMMWORD(t);
        /*TODO*///	D &= t.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $c5 BITB immediate -**0- */
        public static opcode  bitb_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t,r;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	r = B & t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
            }
        };

        /* $1085 BITD immediate -**0- */
        public static opcode  bitd_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR	t;
        /*TODO*///	UINT16	r;
        /*TODO*///	IMMWORD(t);
        /*TODO*///	r = B & t.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(r);
            }
        };

        /* $113c BITMD immediate -**0- */
        public static opcode  bitmd_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t,r;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	r = MD & t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
        /*TODO*///
        /*TODO*///	CLDZ;
        /*TODO*///	CLII;
            }
        };

        /* $c6 LDB immediate -**0- */
        public static opcode  ldb_im = new opcode() {
            public void handler() {
        /*TODO*///	IMMBYTE(B);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(B);
                hd6309.b = IMMBYTE();
                CLR_NZV();
                SET_NZ8(hd6309.b);
            }
        };

        /* $113d LDMD immediate -**0- */
        public static opcode  ldmd_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	IMMBYTE(MD);
        /*TODO*////*	CLR_NZV;	*/
        /*TODO*////*	SET_NZ8(B); */
            }
        };

        /* $1186 LDE immediate -**0- */
        public static opcode  lde_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	IMMBYTE(E);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(E);
            }
        };

        /* $11C6 LDF immediate -**0- */
        public static opcode  ldf_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	IMMBYTE(F);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(F);
            }
        };

        /* $c8 EORB immediate -**0- */
        public static opcode  eorb_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	B ^= t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(B);
            }
        };

        /* $1088 EORD immediate -**0- */
        public static opcode  eord_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR t;
        /*TODO*///	IMMWORD(t);
        /*TODO*///	D ^= t.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $c9 ADCB immediate ***** */
        public static opcode  adcb_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	r = B + t + (CC & CC_C);
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
        /*TODO*///	SET_H(B,t,r);
        /*TODO*///	B = r;
            }
        };

        /* $1089 ADCD immediate ***** */
        public static opcode  adcd_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR	t;
        /*TODO*///	UINT32	r;
        /*TODO*///	IMMWORD(t);
        /*TODO*///	r = D + t.w.l + (CC & CC_C);
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS16(D,t.w.l,r);
        /*TODO*///	D = r;
            }
        };

        /* $cA ORB immediate -**0- */
        public static opcode  orb_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	B |= t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(B);
            }
        };

        /* $108a ORD immediate -**0- */
        public static opcode  ord_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR t;
        /*TODO*///	IMMWORD(t);
        /*TODO*///	D |= t.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $cB ADDB immediate ***** */
        public static opcode  addb_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	IMMBYTE(t);
        /*TODO*///	r = B + t;
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
        /*TODO*///	SET_H(B,t,r);
        /*TODO*///	B = r;
            }
        };

        /* $cC LDD immediate -**0- */
        public static opcode  ldd_im = new opcode() {
            public void handler() {
                int tmp = IMMWORD();
                setDreg(tmp);
                CLR_NZV();
                SET_NZ16(tmp);
            }
        };

        /* $1086 LDW immediate -**0- */
        public static opcode  ldw_im = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR	t;
        /*TODO*///	IMMWORD(t);
        /*TODO*///	W=t.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(W);
            }
        };

        /* $cE LDU (LDS) immediate -**0- */
        public static opcode  ldu_im = new opcode() {
            public void handler() {                
                hd6309.u = IMMWORD() & 0xFFFF;
                CLR_NZV();
                SET_NZ16(hd6309.u);
            }
        };

        /* $10cE LDS immediate -**0- */
        public static opcode  lds_im = new opcode() {
            public void handler() {
                hd6309.s = IMMWORD();
                CLR_NZV();
                SET_NZ16(hd6309.s);
                hd6309.int_state |= HD6309_LDS;
            }
        };

        /* $d0 SUBB direct ?**** */
        public static opcode  subb_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = B - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
        /*TODO*///	B = r;
            }
        };

        /* $1190 SUBE direct ?**** */
        public static opcode  sube_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = E - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(E,t,r);
        /*TODO*///	E = r;
            }
        };

        /* $11d0 SUBF direct ?**** */
        public static opcode  subf_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = F - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(F,t,r);
        /*TODO*///	F = r;
            }
        };

        /* $d1 CMPB direct ?**** */
        public static opcode  cmpb_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = B - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
            }
        };

        /* $1191 CMPE direct ?**** */
        public static opcode  cmpe_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = E - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(E,t,r);
            }
        };

        /* $11D1 CMPF direct ?**** */
        public static opcode  cmpf_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = F - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(F,t,r);
            }
        };

        /* $d2 SBCB direct ?**** */
        public static opcode  sbcb_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = B - t - (CC & CC_C);
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
        /*TODO*///	B = r;
            }
        };

        /* $1092 SBCD direct ?**** */
        public static opcode  sbcd_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR	t;
        /*TODO*///	UINT32	r;
        /*TODO*///	DIRWORD(t);
        /*TODO*///	r = D - t.w.l - (CC & CC_C);
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(D,t.w.l,r);
        /*TODO*///	D = r;
            }
        };

        /* $d3 ADDD direct -**** */
        public static opcode  addd_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	DIRWORD(b);
        /*TODO*///	d = D;
        /*TODO*///	r = d + b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
        /*TODO*///	D = r;
            }
        };

        /* $109b ADDW direct -**** */
        public static opcode  addw_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	DIRWORD(b);
        /*TODO*///	d = W;
        /*TODO*///	r = d + b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
        /*TODO*///	W = r;
            }
        };

        /* $119b ADDE direct -**** */
        public static opcode  adde_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = E + t;
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(E,t,r);
        /*TODO*///	SET_H(E,t,r);
        /*TODO*///	E = r;
            }
        };

        /* $11db ADDF direct -**** */
        public static opcode  addf_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = F + t;
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(F,t,r);
        /*TODO*///	SET_H(F,t,r);
        /*TODO*///	F = r;
            }
        };

        /* $d4 ANDB direct -**0- */
        public static opcode  andb_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	B &= t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(B);
            }
        };

        /* $1094 ANDD direct -**0- */
        public static opcode  andd_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR t;
        /*TODO*///	DIRWORD(t);
        /*TODO*///	D &= t.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $d5 BITB direct -**0- */
        public static opcode  bitb_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = B & t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
            }
        };

        /* $1095 BITD direct -**0- */
        public static opcode  bitd_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR	t;
        /*TODO*///	UINT16	r;
        /*TODO*///	DIRWORD(t);
        /*TODO*///	r = B & t.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(r);
            }
        };

        /* $d6 LDB direct -**0- */
        public static opcode  ldb_di = new opcode() {
            public void handler() {
                hd6309.b = DIRBYTE();
                CLR_NZV();
                SET_NZ8(hd6309.b);
            }
        };

        /* $1196 LDE direct -**0- */
        public static opcode  lde_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	DIRBYTE(E);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(E);
            }
        };

        /* $11d6 LDF direct -**0- */
        public static opcode  ldf_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	DIRBYTE(F);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(F);
            }
        };

        /* $d7 STB direct -**0- */
        public static opcode  stb_di = new opcode() {
            public void handler() {
                CLR_NZV();
                SET_NZ8(hd6309.b);
                DIRECT();
                WM(ea, hd6309.b);
            }
        };

        /* $1197 STE direct -**0- */
        public static opcode  ste_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(E);
        /*TODO*///	DIRECT;
        /*TODO*///	WM(EAD,E);
            }
        };

        /* $11D7 STF direct -**0- */
        public static opcode  stf_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(F);
        /*TODO*///	DIRECT;
        /*TODO*///	WM(EAD,F);
            }
        };

        /* $d8 EORB direct -**0- */
        public static opcode  eorb_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	B ^= t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(B);
            }
        };

        /* $1098 EORD direct -**0- */
        public static opcode  eord_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR t;
        /*TODO*///	DIRWORD(t);
        /*TODO*///	D ^= t.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $d9 ADCB direct ***** */
        public static opcode  adcb_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = B + t + (CC & CC_C);
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
        /*TODO*///	SET_H(B,t,r);
        /*TODO*///	B = r;
            }
        };

        /* $1099 adcd direct ***** */
        public static opcode  adcd_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = D + t + (CC & CC_C);
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS16(D,t,r);
        /*TODO*///	D = r;
            }
        };

        /* $dA ORB direct -**0- */
        public static opcode  orb_di = new opcode() {
            public void handler() {
        /*TODO*///	UINT8 t;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	B |= t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(B);
                int t = DIRBYTE();
                hd6309.b = (hd6309.b | t) & 0xFF;
                CLR_NZV();
                SET_NZ8(hd6309.b);
            }
        };

        /* $109a ORD direct -**0- */
        public static opcode  ord_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR t;
        /*TODO*///	DIRWORD(t);
        /*TODO*///	D |= t.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $dB ADDB direct ***** */
        public static opcode  addb_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	DIRBYTE(t);
        /*TODO*///	r = B + t;
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
        /*TODO*///	SET_H(B,t,r);
        /*TODO*///	B = r;
            }
        };

        /* $dC LDD direct -**0- */
        public static opcode  ldd_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR t;
        /*TODO*///	DIRWORD(t);
        /*TODO*///	D=t.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $1096 LDW direct -**0- */
        public static opcode  ldw_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR t;
        /*TODO*///	DIRWORD(t);
        /*TODO*///	W=t.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(W);
            }
        };

        /* $dD STD direct -**0- */
        public static opcode  std_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
        /*TODO*///	DIRECT;
        /*TODO*///	WM16(EAD,&pD);
            }
        };

        /* $1097 STW direct -**0- */
        public static opcode  stw_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(W);
        /*TODO*///	DIRECT;
        /*TODO*///	WM16(EAD,&pW);
            }
        };

        /* $dE LDU (LDS) direct -**0- */
        public static opcode  ldu_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	DIRWORD(pU);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(U);
            }
        };

        /* $10dE LDS direct -**0- */
        public static opcode  lds_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	DIRWORD(pS);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(S);
        /*TODO*///	hd6309.int_state |= HD6309_LDS;
            }
        };

        /* $dF STU (STS) direct -**0- */
        public static opcode  stu_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(U);
        /*TODO*///	DIRECT;
        /*TODO*///	WM16(EAD,&pU);
            }
        };

        /* $10dF STS direct -**0- */
        public static opcode  sts_di = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(S);
        /*TODO*///	DIRECT;
        /*TODO*///	WM16(EAD,&pS);
            }
        };

        /* $e0 SUBB indexed ?**** */
        public static opcode  subb_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r = B - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
        /*TODO*///	B = r;
            }
        };

        /* $11a0 SUBE indexed ?**** */
        public static opcode  sube_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r = E - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(E,t,r);
        /*TODO*///	E = r;
            }
        };

        /* $11e0 SUBF indexed ?**** */
        public static opcode  subf_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r = F - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(F,t,r);
        /*TODO*///	F = r;
            }
        };

        /* $e1 CMPB indexed ?**** */
        public static opcode  cmpb_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r = B - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
            }
        };

        /* $11a1 CMPE indexed ?**** */
        public static opcode  cmpe_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r = E - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(E,t,r);
            }
        };

        /* $11e1 CMPF indexed ?**** */
        public static opcode  cmpf_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r = F - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(F,t,r);
            }
        };

        /* $e2 SBCB indexed ?**** */
        public static opcode  sbcb_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r = B - t - (CC & CC_C);
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
        /*TODO*///	B = r;
            }
        };

        /* $10a2 SBCD indexed ?**** */
        public static opcode  sbcd_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32	  t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM16(EAD);
        /*TODO*///	r = D - t - (CC & CC_C);
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(D,t,r);
        /*TODO*///	D = r;
            }
        };

        /* $e3 ADDD indexed -**** */
        public static opcode  addd_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	b.d=RM16(EAD);
        /*TODO*///	d = D;
        /*TODO*///	r = d + b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
        /*TODO*///	D = r;
            }
        };

        /* $10ab ADDW indexed -**** */
        public static opcode  addw_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	b.d=RM16(EAD);
        /*TODO*///	d = W;
        /*TODO*///	r = d + b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
        /*TODO*///	W = r;
            }
        };

        /* $11ab ADDE indexed -**** */
        public static opcode  adde_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r = E + t;
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(E,t,r);
        /*TODO*///	SET_H(E,t,r);
        /*TODO*///	E = r;
            }
        };

        /* $11eb ADDF indexed -**** */
        public static opcode  addf_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r = F + t;
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(F,t,r);
        /*TODO*///	SET_H(F,t,r);
        /*TODO*///	F = r;
            }
        };

        /* $e4 ANDB indexed -**0- */
        public static opcode  andb_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	B &= RM(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(B);
            }
        };

        /* $10a4 ANDD indexed -**0- */
        public static opcode  andd_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	D &= RM16(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $e5 BITB indexed -**0- */
        public static opcode  bitb_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	r = B & RM(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
            }
        };

        /* $10a5 BITD indexed -**0- */
        public static opcode  bitd_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	r = D & RM16(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(r);
            }
        };

        /* $e6 LDB indexed -**0- */
        public static opcode  ldb_ix = new opcode() {
            public void handler() {
                fetch_effective_address();
                hd6309.b = RM(ea);
                CLR_NZV();
                SET_NZ8(hd6309.b);
            }
        };

        /* $11a6 LDE indexed -**0- */
        public static opcode  lde_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	E = RM(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(E);
            }
        };

        /* $11e6 LDF indexed -**0- */
        public static opcode  ldf_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	F = RM(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(F);
            }
        };

        /* $e7 STB indexed -**0- */
        public static opcode  stb_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(B);
        /*TODO*///	WM(EAD,B);
            }
        };

        /* $11a7 STE indexed -**0- */
        public static opcode  ste_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(E);
        /*TODO*///	WM(EAD,E);
            }
        };

        /* $11e7 STF indexed -**0- */
        public static opcode  stf_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(F);
        /*TODO*///	WM(EAD,F);
            }
        };

        /* $e8 EORB indexed -**0- */
        public static opcode  eorb_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	B ^= RM(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(B);
            }
        };

        /* $10a8 EORD indexed -**0- */
        public static opcode  eord_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	D ^= RM16(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $e9 ADCB indexed ***** */
        public static opcode  adcb_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r = B + t + (CC & CC_C);
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
        /*TODO*///	SET_H(B,t,r);
        /*TODO*///	B = r;
            }
        };

        /* $10a9 ADCD indexed ***** */
        public static opcode  adcd_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r = D + t + (CC & CC_C);
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS16(D,t,r);
        /*TODO*///	D = r;
            }
        };

        /* $eA ORB indexed -**0- */
        public static opcode  orb_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	B |= RM(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(B);
            }
        };

        /* $10aa ORD indexed -**0- */
        public static opcode  ord_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	D |= RM16(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $eB ADDB indexed ***** */
        public static opcode  addb_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	fetch_effective_address();
        /*TODO*///	t = RM(EAD);
        /*TODO*///	r = B + t;
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
        /*TODO*///	SET_H(B,t,r);
        /*TODO*///	B = r;
            }
        };

        /* $eC LDD indexed -**0- */
        public static opcode  ldd_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	D=RM16(EAD);
        /*TODO*///	CLR_NZV; SET_NZ16(D);
            }
        };

        /* $10a6 LDW indexed -**0- */
        public static opcode  ldw_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	W=RM16(EAD);
        /*TODO*///	CLR_NZV; SET_NZ16(W);
            }
        };

        /* $eD STD indexed -**0- */
        public static opcode  std_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
        /*TODO*///	WM16(EAD,&pD);
            }
        };

        /* $10a7 STW indexed -**0- */
        public static opcode  stw_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(W);
        /*TODO*///	WM16(EAD,&pW);
            }
        };

        /* $eE LDU (LDS) indexed -**0- */
        public static opcode  ldu_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	U=RM16(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(U);
            }
        };

        /* $10eE LDS indexed -**0- */
        public static opcode  lds_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	S=RM16(EAD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(S);
        /*TODO*///	hd6309.int_state |= HD6309_LDS;
            }
        };

        /* $eF STU (STS) indexed -**0- */
        public static opcode  stu_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(U);
        /*TODO*///	WM16(EAD,&pU);
            }
        };

        /* $10eF STS indexed -**0- */
        public static opcode  sts_ix = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	fetch_effective_address();
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(S);
        /*TODO*///	WM16(EAD,&pS);
            }
        };

        /* $f0 SUBB extended ?**** */
        public static opcode  subb_ex = new opcode() {
            public void handler() {
                int t, r;
                t = EXTBYTE();
                r = (hd6309.b - t) & 0xFFFF;
                CLR_NZVC();
                SET_FLAGS8(hd6309.b, t, r);
                hd6309.b = r & 0xFF;
            }
        };

        /* $11b0 SUBE extended ?**** */
        public static opcode  sube_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = E - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(E,t,r);
        /*TODO*///	E = r;
            }
        };

        /* $11f0 SUBF extended ?**** */
        public static opcode  subf_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = F - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(F,t,r);
        /*TODO*///	F = r;
            }
        };

        /* $f1 CMPB extended ?**** */
        public static opcode  cmpb_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = B - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
            }
        };

        /* $11b1 CMPE extended ?**** */
        public static opcode  cmpe_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = E - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(E,t,r);
            }
        };

        /* $11f1 CMPF extended ?**** */
        public static opcode  cmpf_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = F - t;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(F,t,r);
            }
        };

        /* $f2 SBCB extended ?**** */
        public static opcode  sbcb_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16	  t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = B - t - (CC & CC_C);
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
        /*TODO*///	B = r;
            }
        };

        /* $10b2 SBCD extended ?**** */
        public static opcode  sbcd_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR t = {{0,}};
        /*TODO*///	UINT32 r;
        /*TODO*///
        /*TODO*///	EXTWORD(t);
        /*TODO*///	r = D - t.w.l - (CC & CC_C);
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(D,t.w.l,r);
        /*TODO*///	D = r;
            }
        };

        /* $f3 ADDD extended -**** */
        public static opcode  addd_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b = {{0,}};
        /*TODO*///	EXTWORD(b);
        /*TODO*///	d = D;
        /*TODO*///	r = d + b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
        /*TODO*///	D = r;
            }
        };

        /* $10bb ADDW extended -**** */
        public static opcode  addw_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 r,d;
        /*TODO*///	PAIR b = {{0,}};
        /*TODO*///	EXTWORD(b);
        /*TODO*///	d = W;
        /*TODO*///	r = d + b.d;
        /*TODO*///	CLR_NZVC;
        /*TODO*///	SET_FLAGS16(d,b.d,r);
        /*TODO*///	W = r;
            }
        };

        /* $11bb ADDE extended -**** */
        public static opcode  adde_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = E + t;
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(E,t,r);
        /*TODO*///	SET_H(E,t,r);
        /*TODO*///	E = r;
            }
        };

        /* $11fb ADDF extended -**** */
        public static opcode  addf_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = F + t;
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(F,t,r);
        /*TODO*///	SET_H(F,t,r);
        /*TODO*///	F = r;
            }
        };

        /* $f4 ANDB extended -**0- */
        public static opcode  andb_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	B &= t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(B);
            }
        };

        /* $10b4 ANDD extended -**0- */
        public static opcode  andd_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR t = {{0,}};
        /*TODO*///	EXTWORD(t);
        /*TODO*///	D &= t.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $f5 BITB extended -**0- */
        public static opcode  bitb_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = B & t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(r);
            }
        };

        /* $10b5 BITD extended -**0- */
        public static opcode  bitd_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR t = {{0,}};
        /*TODO*///	UINT8 r;
        /*TODO*///	EXTWORD(t);
        /*TODO*///	r = B & t.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(r);
            }
        };

        /* $f6 LDB extended -**0- */
        public static opcode  ldb_ex = new opcode() {
            public void handler() {
                hd6309.b = EXTBYTE();
                CLR_NZV();
                SET_NZ8(hd6309.b);
            }
        };

        /* $11b6 LDE extended -**0- */
        public static opcode  lde_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	EXTBYTE(E);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(E);
            }
        };

        /* $11f6 LDF extended -**0- */
        public static opcode  ldf_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	EXTBYTE(F);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(F);
            }
        };

        /* $f7 STB extended -**0- */
        public static opcode  stb_ex = new opcode() {
            public void handler() {
                CLR_NZV();
                SET_NZ8(hd6309.b);
                EXTENDED();
                WM(ea, hd6309.b);
            }
        };

        /* $11b7 STE extended -**0- */
        public static opcode  ste_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(E);
        /*TODO*///	EXTENDED;
        /*TODO*///	WM(EAD,E);
            }
        };

        /* $11f7 STF extended -**0- */
        public static opcode  stf_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(F);
        /*TODO*///	EXTENDED;
        /*TODO*///	WM(EAD,F);
            }
        };

        /* $f8 EORB extended -**0- */
        public static opcode  eorb_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	B ^= t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(B);
            }
        };

        /* $10b8 EORD extended -**0- */
        public static opcode  eord_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR t = {{0,}};
        /*TODO*///	EXTWORD(t);
        /*TODO*///	D ^= t.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $f9 ADCB extended ***** */
        public static opcode  adcb_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = B + t + (CC & CC_C);
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
        /*TODO*///	SET_H(B,t,r);
        /*TODO*///	B = r;
            }
        };

        /* $10b9 ADCD extended ***** */
        public static opcode  adcd_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT32 t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = D + t + (CC & CC_C);
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS16(D,t,r);
        /*TODO*///	D = r;
            }
        };

        /* $fA ORB extended -**0- */
        public static opcode  orb_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT8 t;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	B |= t;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(B);
            }
        };

        /* $10ba ORD extended -**0- */
        public static opcode  ord_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	PAIR t = {{0,}};
        /*TODO*///	EXTWORD(t);
        /*TODO*///	D |= t.w.l;
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ8(D);
            }
        };

        /* $fB ADDB extended ***** */
        public static opcode  addb_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	UINT16 t,r;
        /*TODO*///	EXTBYTE(t);
        /*TODO*///	r = B + t;
        /*TODO*///	CLR_HNZVC;
        /*TODO*///	SET_FLAGS8(B,t,r);
        /*TODO*///	SET_H(B,t,r);
        /*TODO*///	B = r;
            }
        };

        /* $fC LDD extended -**0- */
        public static opcode  ldd_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	EXTWORD(pD);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
            }
        };

        /* $10b6 LDW extended -**0- */
        public static opcode  ldw_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	EXTWORD(pW);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(W);
            }
        };

        /* $fD STD extended -**0- */
        public static opcode  std_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(D);
        /*TODO*///	EXTENDED;
        /*TODO*///	WM16(EAD,&pD);
            }
        };

        /* $10b7 STW extended -**0- */
        public static opcode  stw_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(W);
        /*TODO*///	EXTENDED;
        /*TODO*///	WM16(EAD,&pW);
            }
        };

        /* $fE LDU (LDS) extended -**0- */
        public static opcode  ldu_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	EXTWORD(pU);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(U);
            }
        };

        /* $10fE LDS extended -**0- */
        public static opcode  lds_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	EXTWORD(pS);
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(S);
        /*TODO*///	hd6309.int_state |= HD6309_LDS;
            }
        };

        /* $fF STU (STS) extended -**0- */
        public static opcode  stu_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(U);
        /*TODO*///	EXTENDED;
        /*TODO*///	WM16(EAD,&pU);
            }
        };

        /* $10fF STS extended -**0- */
        public static opcode  sts_ex = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///	CLR_NZV;
        /*TODO*///	SET_NZ16(S);
        /*TODO*///	EXTENDED;
        /*TODO*///	WM16(EAD,&pS);
            }
        };

        /* $10xx opcodes */
        public static opcode  pref10 = new opcode() {
            public void handler() {
                
                int ireg2 = ROP(hd6309.pc) & 0xFF;
                hd6309.pc = (hd6309.pc + 1) & 0xFFFF;
        
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
            (hd6309_page01[ireg2]).handler();
        /*TODO*///
        /*TODO*///#endif /* BIG_SWITCH */
        /*TODO*///
        	hd6309_ICount[0] -= cycle_counts_page01[ireg2];
            }
        };

        /* $11xx opcodes */
        public static opcode  pref11 = new opcode() {
            public void handler() {
                if (hd6309log != null) {
                    fclose(hd6309log);
                }
                throw new UnsupportedOperationException("Unimplemented");
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
            }
    };
    
}
