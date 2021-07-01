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

    public static opcode asr_di = new opcode() {
        public void handler() {
            int t = DIRBYTE();
            CLR_NZC();
            m6809.cc |= (t & CC_C);
            t = ((t & 0x80) | (t >>> 1)) & 0xFF;
            SET_NZ8(t);
            WM(ea, t);
        }
    };

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

    public static opcode jmp_di = new opcode() {
        public void handler() {
            DIRECT();
            m6809.pc = ea & 0xFFFF;
            CHANGE_PC();
        }
    };

    public static opcode clr_di = new opcode() {
        public void handler() {
            DIRECT();
            WM(ea, 0);
            CLR_NZVC();
            SEZ();
        }
    };

    public static opcode nop = new opcode() {
        public void handler() {
        }
    };

    public static opcode sync = new opcode() {
        public void handler() {
            /* SYNC stops processing instructions until an interrupt request happens. */
 /* This doesn't require the corresponding interrupt to be enabled: if it */
 /* is disabled, execution continues with the next instruction. */
            m6809.int_state |= M6809_SYNC;
            /* HJB 990227 */
            CHECK_IRQ_LINES();
            /* if M6809_SYNC has not been cleared by CHECK_IRQ_LINES,
	 * stop execution until the interrupt lines change. */
            if ((m6809.int_state & M6809_SYNC) != 0) {
                if (m6809_ICount[0] > 0) {
                    m6809_ICount[0] = 0;
                }
            }
        }
    };

    public static opcode lbra = new opcode() {
        public void handler() {
            ea = IMMWORD();
            m6809.pc = ((m6809.pc + ea) & 0xFFFF);
            CHANGE_PC();

            if (ea == 0xfffd) /* EHC 980508 speed up busy loop */ {
                if (m6809_ICount[0] > 0) {
                    m6809_ICount[0] = 0;
                }
            }
        }
    };

    public static opcode lbsr = new opcode() {
        public void handler() {
            ea = IMMWORD();
            PUSHWORD(m6809.pc);
            m6809.pc = ((m6809.pc + ea) & 0xFFFF);
            CHANGE_PC();
        }
    };

    public static opcode daa = new opcode() {
        public void handler() {
            int/*UINT8*/ msn, lsn;
            int/*UINT16*/ t, cf = 0;
            msn = m6809.a & 0xf0;
            lsn = m6809.a & 0x0f;
            if (lsn > 0x09 || (m6809.cc & CC_H) != 0) {
                cf |= 0x06;
            }
            if (msn > 0x80 && lsn > 0x09) {
                cf |= 0x60;
            }
            if (msn > 0x90 || (m6809.cc & CC_C) != 0) {
                cf |= 0x60;
            }
            t = (cf + m6809.a) & 0xFFFF;
            CLR_NZV();/* keep carry from previous operation */
            SET_NZ8(/*(UINT8)*/t & 0xFF);
            SET_C8(t);
            m6809.a = (t & 0xFF);
        }
    };

    public static opcode orcc = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            m6809.cc = (m6809.cc | t) & 0xFF;
            CHECK_IRQ_LINES();
        }
    };

    public static opcode andcc = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            m6809.cc = (m6809.cc & t) & 0xFF;
            CHECK_IRQ_LINES();
        }
    };

    public static opcode sex = new opcode() {
        public void handler() {
            int t = SIGNED(m6809.b) & 0xFFFF;
            setDreg(t);
            CLR_NZV();
            SET_NZ16(t);
        }
    };

    public static opcode exg = new opcode() {
        public void handler() {
            int /*UINT16*/ t1, t2;
            int /*UINT8*/ tb;

            tb = IMMBYTE();
            if (((tb ^ (tb >>> 4)) & 0x08) != 0) /* HJB 990225: mixed 8/16 bit case? */ {
                /* transfer $ff to both registers */
                t1 = t2 = 0xff;
            } else {
                switch (tb >>> 4) {
                    case 0:
                        t1 = getDreg();
                        break;
                    case 1:
                        t1 = m6809.x;
                        break;
                    case 2:
                        t1 = m6809.y;
                        break;
                    case 3:
                        t1 = m6809.u;
                        break;
                    case 4:
                        t1 = m6809.s;
                        break;
                    case 5:
                        t1 = m6809.pc;
                        break;
                    case 8:
                        t1 = m6809.a;
                        break;
                    case 9:
                        t1 = m6809.b;
                        break;
                    case 10:
                        t1 = m6809.cc;
                        break;
                    case 11:
                        t1 = m6809.dp;
                        break;
                    default:
                        t1 = 0xff;
                }
                switch (tb & 15) {
                    case 0:
                        t2 = getDreg();
                        break;
                    case 1:
                        t2 = m6809.x;
                        break;
                    case 2:
                        t2 = m6809.y;
                        break;
                    case 3:
                        t2 = m6809.u;
                        break;
                    case 4:
                        t2 = m6809.s;
                        break;
                    case 5:
                        t2 = m6809.pc;
                        break;
                    case 8:
                        t2 = m6809.a;
                        break;
                    case 9:
                        t2 = m6809.b;
                        break;
                    case 10:
                        t2 = m6809.cc;
                        break;
                    case 11:
                        t2 = m6809.dp;
                        break;
                    default:
                        t2 = 0xff;
                }
            }
            switch (tb >>> 4) {
                case 0:
                    setDreg(t2);
                    break;
                case 1:
                    m6809.x = (t2) & 0xFFFF;
                    break;
                case 2:
                    m6809.y = (t2) & 0xFFFF;
                    break;
                case 3:
                    m6809.u = (t2) & 0xFFFF;
                    break;
                case 4:
                    m6809.s = (t2) & 0xFFFF;
                    break;
                case 5:
                    m6809.pc = (t2) & 0xFFFF;
                    CHANGE_PC();
                    break;
                case 8:
                    m6809.a = (t2) & 0xFF;
                    break;
                case 9:
                    m6809.b = (t2) & 0xFF;
                    break;
                case 10:
                    m6809.cc = (t2) & 0xFF;
                    break;
                case 11:
                    m6809.dp = (t2) & 0xFF;
                    break;
            }
            switch (tb & 15) {
                case 0:
                    setDreg(t1);
                    break;
                case 1:
                    m6809.x = (t1) & 0xFFFF;
                    break;
                case 2:
                    m6809.y = (t1) & 0xFFFF;
                    break;
                case 3:
                    m6809.u = (t1) & 0xFFFF;
                    break;
                case 4:
                    m6809.s = (t1) & 0xFFFF;
                    break;
                case 5:
                    m6809.pc = (t1) & 0xFFFF;
                    CHANGE_PC();
                    break;
                case 8:
                    m6809.a = (t1 & 0xFF);
                    break;
                case 9:
                    m6809.b = (t1 & 0xFF);
                    break;
                case 10:
                    m6809.cc = (t1 & 0xFF);
                    break;
                case 11:
                    m6809.dp = (t1 & 0xFF);
                    break;
            }
        }
    };

    public static opcode tfr = new opcode() {
        public void handler() {

            int /*UINT8*/ tb;
            int /*UINT16*/ t;

            tb = IMMBYTE();
            if (((tb ^ (tb >>> 4)) & 0x08) != 0) /* HJB 990225: mixed 8/16 bit case? */ {
                /* transfer $ff to register */
                t = 0xff;
            } else {
                switch (tb >>> 4) {
                    case 0:
                        t = getDreg();
                        break;
                    case 1:
                        t = m6809.x;
                        break;
                    case 2:
                        t = m6809.y;
                        break;
                    case 3:
                        t = m6809.u;
                        break;
                    case 4:
                        t = m6809.s;
                        break;
                    case 5:
                        t = m6809.pc;
                        break;
                    case 8:
                        t = m6809.a;
                        break;
                    case 9:
                        t = m6809.b;
                        break;
                    case 10:
                        t = m6809.cc;
                        break;
                    case 11:
                        t = m6809.dp;
                        break;
                    default:
                        t = 0xff;
                }
            }
            switch (tb & 15) {
                case 0:
                    setDreg(t);
                    break;
                case 1:
                    m6809.x = t & 0xFFFF;
                    break;
                case 2:
                    m6809.y = t & 0xFFFF;
                    break;
                case 3:
                    m6809.u = t & 0xFFFF;
                    break;
                case 4:
                    m6809.s = t & 0xFFFF;
                    break;
                case 5:
                    m6809.pc = t & 0xFFFF;
                    CHANGE_PC();
                    break;
                case 8:
                    m6809.a = (t & 0xFF);
                    break;
                case 9:
                    m6809.b = (t & 0xFF);
                    break;
                case 10:
                    m6809.cc = (t & 0xFF);
                    break;
                case 11:
                    m6809.dp = t & 0xFF;
                    break;
            }
        }
    };

    public static opcode bra = new opcode() {
        public void handler() {
            int t;
            t = IMMBYTE();
            m6809.pc = (m6809.pc + SIGNED(t)) & 0xFFFF;
            CHANGE_PC();
            /* JB 970823 - speed up busy loops */
            if (t == 0xfe) {
                if (m6809_ICount[0] > 0) {
                    m6809_ICount[0] = 0;
                }
            }
        }
    };

    public static opcode brn = new opcode() {
        public void handler() {
            int t = IMMBYTE();
        }
    };

    public static opcode lbrn = new opcode() {
        public void handler() {
            ea = IMMWORD();
        }
    };

    public static opcode bhi = new opcode() {
        public void handler() {
            BRANCH((m6809.cc & (CC_Z | CC_C)) == 0);
        }
    };

    public static opcode lbhi = new opcode() {
        public void handler() {
            LBRANCH((m6809.cc & (CC_Z | CC_C)) == 0);
        }
    };

    public static opcode bls = new opcode() {
        public void handler() {
            BRANCH((m6809.cc & (CC_Z | CC_C)) != 0);
        }
    };

    public static opcode lbls = new opcode() {
        public void handler() {
            LBRANCH((m6809.cc & (CC_Z | CC_C)) != 0);
        }
    };

    public static opcode bcc = new opcode() {
        public void handler() {

            BRANCH((m6809.cc & CC_C) == 0);
        }
    };

    public static opcode lbcc = new opcode() {
        public void handler() {
            LBRANCH((m6809.cc & CC_C) == 0);
        }
    };

    public static opcode bcs = new opcode() {
        public void handler() {
            BRANCH((m6809.cc & CC_C) != 0);
        }
    };

    public static opcode lbcs = new opcode() {
        public void handler() {
            LBRANCH((m6809.cc & CC_C) != 0);
        }
    };

    public static opcode bne = new opcode() {
        public void handler() {
            BRANCH((m6809.cc & CC_Z) == 0);
        }
    };

    public static opcode lbne = new opcode() {
        public void handler() {
            LBRANCH((m6809.cc & CC_Z) == 0);
        }
    };

    public static opcode beq = new opcode() {
        public void handler() {
            BRANCH((m6809.cc & CC_Z) != 0);
        }
    };

    public static opcode lbeq = new opcode() {
        public void handler() {
            LBRANCH((m6809.cc & CC_Z) != 0);
        }
    };

    public static opcode bvc = new opcode() {
        public void handler() {
            BRANCH((m6809.cc & CC_V) == 0);
        }
    };

    public static opcode lbvc = new opcode() {
        public void handler() {
            LBRANCH((m6809.cc & CC_V) == 0);
        }
    };

    public static opcode bvs = new opcode() {
        public void handler() {
            BRANCH((m6809.cc & CC_V) != 0);
        }
    };

    public static opcode lbvs = new opcode() {
        public void handler() {
            LBRANCH((m6809.cc & CC_V) != 0);
        }
    };

    public static opcode bpl = new opcode() {
        public void handler() {
            BRANCH((m6809.cc & CC_N) == 0);
        }
    };

    public static opcode lbpl = new opcode() {
        public void handler() {
            LBRANCH((m6809.cc & CC_N) == 0);
        }
    };

    public static opcode bmi = new opcode() {
        public void handler() {
            BRANCH((m6809.cc & CC_N) != 0);
        }
    };

    public static opcode lbmi = new opcode() {
        public void handler() {
            LBRANCH((m6809.cc & CC_N) != 0);
        }
    };

    public static opcode bge = new opcode() {
        public void handler() {
            BRANCH(NXORV() == 0);
        }
    };

    public static opcode lbge = new opcode() {
        public void handler() {
            LBRANCH(NXORV() == 0);
        }
    };

    public static opcode blt = new opcode() {
        public void handler() {
            BRANCH(NXORV() != 0);
        }
    };

    public static opcode lblt = new opcode() {
        public void handler() {
            LBRANCH(NXORV() != 0);
        }
    };

    public static opcode bgt = new opcode() {
        public void handler() {
            BRANCH(!((NXORV() != 0) || ((m6809.cc & CC_Z) != 0)));
        }
    };

    public static opcode lbgt = new opcode() {
        public void handler() {
            LBRANCH(!((NXORV() != 0) || ((m6809.cc & CC_Z) != 0)));
        }
    };

    public static opcode ble = new opcode() {
        public void handler() {
            BRANCH((NXORV() != 0 || (m6809.cc & CC_Z) != 0));
        }
    };

    public static opcode lble = new opcode() {
        public void handler() {
            LBRANCH((NXORV() != 0 || (m6809.cc & CC_Z) != 0));
        }
    };

    public static opcode leax = new opcode() {
        public void handler() {
            fetch_effective_address();
            m6809.x = ea & 0xFFFF;
            CLR_Z();
            SET_Z(m6809.x);
        }
    };

    public static opcode leay = new opcode() {
        public void handler() {
            fetch_effective_address();
            m6809.y = ea & 0xFFFF;
            CLR_Z();
            SET_Z(m6809.y);
        }
    };

    public static opcode leas = new opcode() {
        public void handler() {
            fetch_effective_address();
            m6809.s = ea & 0xFFFF;
            m6809.int_state |= M6809_LDS;
        }
    };

    public static opcode leau = new opcode() {
        public void handler() {
            fetch_effective_address();
            m6809.u = ea & 0xFFFF;
        }
    };

    public static opcode pshs = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            if ((t & 0x80) != 0) {
                PUSHWORD(m6809.pc);
                m6809_ICount[0] -= 2;
            }
            if ((t & 0x40) != 0) {
                PUSHWORD(m6809.u);
                m6809_ICount[0] -= 2;
            }
            if ((t & 0x20) != 0) {
                PUSHWORD(m6809.y);
                m6809_ICount[0] -= 2;
            }
            if ((t & 0x10) != 0) {
                PUSHWORD(m6809.x);
                m6809_ICount[0] -= 2;
            }
            if ((t & 0x08) != 0) {
                PUSHBYTE(m6809.dp);
                m6809_ICount[0] -= 1;
            }
            if ((t & 0x04) != 0) {
                PUSHBYTE(m6809.b);
                m6809_ICount[0] -= 1;
            }
            if ((t & 0x02) != 0) {
                PUSHBYTE(m6809.a);
                m6809_ICount[0] -= 1;
            }
            if ((t & 0x01) != 0) {
                PUSHBYTE(m6809.cc);
                m6809_ICount[0] -= 1;
            }
        }
    };

    public static opcode puls = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            if ((t & 0x01) != 0) {
                m6809.cc = (PULLBYTE());
                m6809_ICount[0] -= 1;
            }
            if ((t & 0x02) != 0) {
                m6809.a = (PULLBYTE());
                m6809_ICount[0] -= 1;
            }
            if ((t & 0x04) != 0) {
                m6809.b = (PULLBYTE());
                m6809_ICount[0] -= 1;
            }
            if ((t & 0x08) != 0) {
                m6809.dp = (PULLBYTE());
                m6809_ICount[0] -= 1;
            }
            if ((t & 0x10) != 0) {
                m6809.x = (PULLWORD());
                m6809_ICount[0] -= 2;
            }
            if ((t & 0x20) != 0) {
                m6809.y = (PULLWORD());
                m6809_ICount[0] -= 2;
            }
            if ((t & 0x40) != 0) {
                m6809.u = (PULLWORD());
                m6809_ICount[0] -= 2;
            }
            if ((t & 0x80) != 0) {
                m6809.pc = (PULLWORD());
                CHANGE_PC();
                m6809_ICount[0] -= 2;
            }

            /* HJB 990225: moved check after all PULLs */
            if ((t & 0x01) != 0) {
                CHECK_IRQ_LINES();
            }
        }
    };

    /* $36 PSHU inherent ----- */
    public static opcode pshu = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            if ((t & 0x80) != 0) {
                PSHUWORD(m6809.pc);
                m6809_ICount[0] -= 2;
            }
            if ((t & 0x40) != 0) {
                PSHUWORD(m6809.s);
                m6809_ICount[0] -= 2;
            }
            if ((t & 0x20) != 0) {
                PSHUWORD(m6809.y);
                m6809_ICount[0] -= 2;
            }
            if ((t & 0x10) != 0) {
                PSHUWORD(m6809.x);
                m6809_ICount[0] -= 2;
            }
            if ((t & 0x08) != 0) {
                PSHUBYTE(m6809.dp);
                m6809_ICount[0] -= 1;
            }
            if ((t & 0x04) != 0) {
                PSHUBYTE(m6809.b);
                m6809_ICount[0] -= 1;
            }
            if ((t & 0x02) != 0) {
                PSHUBYTE(m6809.a);
                m6809_ICount[0] -= 1;
            }
            if ((t & 0x01) != 0) {
                PSHUBYTE(m6809.cc);
                m6809_ICount[0] -= 1;
            }
        }
    };

    public static opcode pulu = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            if ((t & 0x01) != 0) {
                m6809.cc = (PULUBYTE());
                m6809_ICount[0] -= 1;
            }
            if ((t & 0x02) != 0) {
                m6809.a = (PULUBYTE());
                m6809_ICount[0] -= 1;
            }
            if ((t & 0x04) != 0) {
                m6809.b = (PULUBYTE());
                m6809_ICount[0] -= 1;
            }
            if ((t & 0x08) != 0) {
                m6809.dp = (PULUBYTE());
                m6809_ICount[0] -= 1;
            }
            if ((t & 0x10) != 0) {
                m6809.x = (PULUWORD());
                m6809_ICount[0] -= 2;
            }
            if ((t & 0x20) != 0) {
                m6809.y = (PULUWORD());
                m6809_ICount[0] -= 2;
            }
            if ((t & 0x40) != 0) {
                m6809.s = (PULUWORD());
                m6809_ICount[0] -= 2;
            }
            if ((t & 0x80) != 0) {
                m6809.pc = (PULUWORD());
                CHANGE_PC();
                m6809_ICount[0] -= 2;
            }

            /* HJB 990225: moved check after all PULLs */
            if ((t & 0x01) != 0) {
                CHECK_IRQ_LINES();
            }
        }
    };

    public static opcode rts = new opcode() {
        public void handler() {
            m6809.pc = PULLWORD() & 0xFFFF;
            CHANGE_PC();
        }
    };

    public static opcode abx = new opcode() {
        public void handler() {
            m6809.x = (m6809.x + m6809.b) & 0xFFFF;
        }
    };

    public static opcode rti = new opcode() {
        public void handler() {
            int t;
            m6809.cc = (PULLBYTE());
            t = m6809.cc & CC_E;
            /* HJB 990225: entire state saved? */
            if (t != 0) {
                m6809_ICount[0] -= 9;
                m6809.a = (PULLBYTE());
                m6809.b = (PULLBYTE());
                m6809.dp = (PULLBYTE());
                m6809.x = (PULLWORD());
                m6809.y = (PULLWORD());
                m6809.u = (PULLWORD());
            }
            m6809.pc = (PULLWORD());
            CHANGE_PC();
            CHECK_IRQ_LINES();
        }
    };

    public static opcode cwai = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            m6809.cc = (m6809.cc & t) & 0xFF;
            /*
         * CWAI stacks the entire machine_old state on the hardware stack,
         * then waits for an interrupt; when the interrupt is taken
         * later, the state is *not* saved again after CWAI.
             */
            m6809.cc |= CC_E;
            /* HJB 990225: save entire state */
            PUSHWORD(m6809.pc);
            PUSHWORD(m6809.u);
            PUSHWORD(m6809.y);
            PUSHWORD(m6809.x);
            PUSHBYTE(m6809.dp);
            PUSHBYTE(m6809.b);
            PUSHBYTE(m6809.a);
            PUSHBYTE(m6809.cc);
            m6809.int_state |= M6809_CWAI;
            /* HJB 990228 */
            CHECK_IRQ_LINES();
            /* HJB 990116 */
            if ((m6809.int_state & M6809_CWAI) != 0) {
                if (m6809_ICount[0] > 0) {
                    m6809_ICount[0] = 0;
                }
            }
        }
    };

    public static opcode mul = new opcode() {
        public void handler() {
            int t;
            t = ((m6809.a & 0xff) * (m6809.b & 0xff)) & 0xFFFF;
            CLR_ZC();
            SET_Z16(t);
            if ((t & 0x80) != 0) {
                SEC();
            }
            setDreg(t);
        }
    };

    public static opcode swi = new opcode() {
        public void handler() {
            m6809.cc |= CC_E;
            PUSHWORD(m6809.pc);
            PUSHWORD(m6809.u);
            PUSHWORD(m6809.y);
            PUSHWORD(m6809.x);
            PUSHBYTE(m6809.dp);
            PUSHBYTE(m6809.b);
            PUSHBYTE(m6809.a);
            PUSHBYTE(m6809.cc);
            m6809.cc |= CC_IF | CC_II;/* inhibit FIRQ and IRQ */
            m6809.pc = RM16(0xfffa);
            CHANGE_PC();
        }
    };

    public static opcode swi2 = new opcode() {
        public void handler() {
            m6809.cc |= CC_E;
            PUSHWORD(m6809.pc);
            PUSHWORD(m6809.u);
            PUSHWORD(m6809.y);
            PUSHWORD(m6809.x);
            PUSHBYTE(m6809.dp);
            PUSHBYTE(m6809.b);
            PUSHBYTE(m6809.a);
            PUSHBYTE(m6809.cc);
            m6809.pc = RM16(0xfff4);
            CHANGE_PC();
        }
    };

    public static opcode swi3 = new opcode() {
        public void handler() {
            m6809.cc |= CC_E;
            PUSHWORD(m6809.pc);
            PUSHWORD(m6809.u);
            PUSHWORD(m6809.y);
            PUSHWORD(m6809.x);
            PUSHBYTE(m6809.dp);
            PUSHBYTE(m6809.b);
            PUSHBYTE(m6809.a);
            PUSHBYTE(m6809.cc);
            m6809.pc = RM16(0xfff2);
            CHANGE_PC();
        }
    };
    public static opcode nega = new opcode() {
        public void handler() {
            int r;
            r = -m6809.a & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(0, m6809.a, r);
            m6809.a = r & 0xFF;
        }
    };

    public static opcode coma = new opcode() {
        public void handler() {
            m6809.a = ~m6809.a & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.a);
            SEC();
        }
    };

    public static opcode lsra = new opcode() {
        public void handler() {
            CLR_NZC();
            m6809.cc |= (m6809.a & CC_C);
            m6809.a = (m6809.a >>> 1) & 0xFF;
            SET_Z8(m6809.a);
        }
    };

    public static opcode rora = new opcode() {
        public void handler() {
            int r;
            r = ((m6809.cc & CC_C) << 7) & 0xFF;
            CLR_NZC();
            m6809.cc |= (m6809.a & CC_C);
            r = (r | m6809.a >>> 1) & 0xFF;
            SET_NZ8(r);
            m6809.a = r & 0xFF;
        }
    };

    public static opcode asra = new opcode() {
        public void handler() {
            CLR_NZC();
            m6809.cc |= (m6809.a & CC_C);
            m6809.a = ((m6809.a & 0x80) | (m6809.a >>> 1)) & 0xFF;
            SET_NZ8(m6809.a);

        }
    };

    public static opcode asla = new opcode() {
        public void handler() {
            int r = (m6809.a << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.a, m6809.a, r);
            m6809.a = r & 0xFF;
        }
    };

    public static opcode rola = new opcode() {
        public void handler() {
            int t, r;
            t = m6809.a & 0xFFFF;
            r = ((m6809.cc & CC_C));
            r = (r | t << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t, t, r);
            m6809.a = r & 0xFF;
        }
    };

    public static opcode deca = new opcode() {
        public void handler() {
            m6809.a = (m6809.a - 1) & 0xFF;//--A;
            CLR_NZV();
            SET_FLAGS8D(m6809.a);
        }
    };

    public static opcode inca = new opcode() {
        public void handler() {
            m6809.a = (m6809.a + 1) & 0xFF;//++A;
            CLR_NZV();
            SET_FLAGS8I(m6809.a);
        }
    };

    public static opcode tsta = new opcode() {
        public void handler() {
            CLR_NZVC();
            SET_NZ8(m6809.a);
        }
    };

    public static opcode clra = new opcode() {
        public void handler() {
            m6809.a = 0;
            CLR_NZVC();
            SEZ();
        }
    };

    public static opcode negb = new opcode() {
        public void handler() {
            int r;
            r = -m6809.b & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(0, m6809.b, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode comb = new opcode() {
        public void handler() {
            m6809.b = ~m6809.b & 0xFF;//B = ~B;
            CLR_NZV();
            SET_NZ8(m6809.b);
            SEC();
        }
    };

    public static opcode lsrb = new opcode() {
        public void handler() {
            CLR_NZC();
            m6809.cc |= (m6809.b & CC_C);
            m6809.b = (m6809.b >>> 1) & 0xFF;
            SET_Z8(m6809.b);
        }
    };

    public static opcode rorb = new opcode() {
        public void handler() {
            int r;
            r = ((m6809.cc & CC_C) << 7) & 0xFF;
            CLR_NZC();
            m6809.cc |= (m6809.b & CC_C);
            r = (r | m6809.b >>> 1) & 0xFF;
            SET_NZ8(r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode asrb = new opcode() {
        public void handler() {
            CLR_NZC();
            m6809.cc |= (m6809.b & CC_C);
            m6809.b = ((m6809.b & 0x80) | (m6809.b >>> 1)) & 0xFF;
            SET_NZ8(m6809.b);
        }
    };

    public static opcode aslb = new opcode() {
        public void handler() {
            int r = (m6809.b << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.b, m6809.b, r);
            m6809.b = r & 0xFF;

        }
    };

    public static opcode rolb = new opcode() {
        public void handler() {
            int t, r;
            t = m6809.b & 0xFFFF;
            r = m6809.cc & CC_C;
            r = (r | t << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t, t, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode decb = new opcode() {
        public void handler() {
            m6809.b = (m6809.b - 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8D(m6809.b);
        }
    };

    public static opcode incb = new opcode() {
        public void handler() {
            m6809.b = (m6809.b + 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8I(m6809.b);
        }
    };

    public static opcode tstb = new opcode() {
        public void handler() {
            CLR_NZVC();
            SET_NZ8(m6809.b);
        }
    };

    public static opcode clrb = new opcode() {
        public void handler() {
            m6809.b = 0;
            CLR_NZVC();
            SEZ();
        }
    };

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

    public static opcode asr_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            int t = RM(ea);
            CLR_NZC();
            m6809.cc |= (t & CC_C);
            t = ((t & 0x80) | (t >>> 1)) & 0xFF;
            SET_NZ8(t);
            WM(ea, t);
        }
    };

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

    public static opcode jmp_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            m6809.pc = ea & 0xFFFF;
            CHANGE_PC();
        }
    };

    public static opcode clr_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            WM(ea, 0);
            CLR_NZVC();
            SEZ();
        }
    };

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

    public static opcode asr_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE();
            CLR_NZC();
            m6809.cc |= (t & CC_C);
            t = ((t & 0x80) | (t >>> 1)) & 0xFF;
            SET_NZ8(t);
            WM(ea, t);
        }
    };

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

    public static opcode jmp_ex = new opcode() {
        public void handler() {
            EXTENDED();
            m6809.pc = ea & 0xFFFF;
            CHANGE_PC();
        }
    };

    public static opcode clr_ex = new opcode() {
        public void handler() {
            EXTENDED();
            WM(ea, 0);
            CLR_NZVC();
            SEZ();
        }
    };

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

    public static opcode cmpa_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE();
            r = (m6809.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.a, t, r);
        }
    };

    public static opcode sbca_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE();
            r = (m6809.a - t - (m6809.cc & CC_C)) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.a, t, r);
            m6809.a = r & 0xFF;
        }
    };

    public static opcode subd_im = new opcode() {
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

    public static opcode cmpd_im = new opcode() {
        public void handler() {
            int r, d;
            int b = IMMWORD();
            d = getDreg();
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode cmpu_im = new opcode() {
        public void handler() {
            int r, d;
            int b = IMMWORD();
            d = m6809.u;
            r = (d - b);
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode anda_im = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            m6809.a = (m6809.a & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.a);
        }
    };

    public static opcode bita_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE();
            r = (m6809.a & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    public static opcode lda_im = new opcode() {
        public void handler() {
            m6809.a = IMMBYTE();
            CLR_NZV();
            SET_NZ8(m6809.a);
        }
    };
    /* is this a legal instruction? */
    public static opcode sta_im = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(m6809.a);
            IMM8();
            WM(ea, m6809.a);
        }
    };

    public static opcode eora_im = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            m6809.a = (m6809.a ^ t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.a);
        }
    };

    public static opcode adca_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE();
            r = (m6809.a + t + (m6809.cc & CC_C)) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6809.a, t, r);
            SET_H(m6809.a, t, r);
            m6809.a = r & 0xFF;
        }
    };

    public static opcode ora_im = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            m6809.a = (m6809.a | t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.a);
        }
    };

    public static opcode adda_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE();
            r = (m6809.a + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6809.a, t, r);
            SET_H(m6809.a, t, r);
            m6809.a = r & 0xFF;
        }
    };

    public static opcode cmpx_im = new opcode() {
        public void handler() {
            int/*UINT32*/ r, d;
            int b = IMMWORD();
            d = m6809.x;
            r = (d - b);
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode cmpy_im = new opcode() {
        public void handler() {
            int/*UINT32*/ r, d;
            int b = IMMWORD();
            d = m6809.y;
            r = (d - b);
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode cmps_im = new opcode() {
        public void handler() {
            int r, d;
            int b = IMMWORD();
            d = m6809.s;
            r = (d - b);
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode bsr = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            PUSHWORD(m6809.pc);
            m6809.pc = (m6809.pc + SIGNED(t)) & 0xFFFF;
            CHANGE_PC();
        }
    };

    public static opcode ldx_im = new opcode() {
        public void handler() {
            m6809.x = IMMWORD();
            CLR_NZV();
            SET_NZ16(m6809.x);
        }
    };

    public static opcode ldy_im = new opcode() {
        public void handler() {
            m6809.y = IMMWORD();
            CLR_NZV();
            SET_NZ16(m6809.y);
        }
    };

    /* is this a legal instruction? */
    public static opcode stx_im = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(m6809.x);
            IMM16();
            WM16(ea, m6809.x);
        }
    };

    /* is this a legal instruction? */
    public static opcode sty_im = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(m6809.y);
            IMM16();
            WM16(ea, m6809.y);
        }
    };

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

    public static opcode cmpa_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE();
            r = (m6809.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.a, t, r);
        }
    };

    public static opcode sbca_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE();
            r = (m6809.a - t - (m6809.cc & CC_C)) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.a, t, r);
            m6809.a = r & 0xFF;
        }
    };

    public static opcode subd_di = new opcode() {
        public void handler() {
            int r, d;
            int b;
            b = DIRWORD();
            d = getDreg();
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r);
        }
    };

    public static opcode cmpd_di = new opcode() {
        public void handler() {
            int r, d;
            int b;
            b = DIRWORD();
            d = getDreg();
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode cmpu_di = new opcode() {
        public void handler() {
            int r, d;
            int b = DIRWORD();
            d = m6809.u;
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode anda_di = new opcode() {
        public void handler() {
            int t = DIRBYTE();
            m6809.a = (m6809.a & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.a);
        }
    };

    public static opcode bita_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE();
            r = (m6809.a & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    public static opcode lda_di = new opcode() {
        public void handler() {
            m6809.a = DIRBYTE();
            CLR_NZV();
            SET_NZ8(m6809.a);
        }
    };

    public static opcode sta_di = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(m6809.a);
            DIRECT();
            WM(ea, m6809.a);
        }
    };

    public static opcode eora_di = new opcode() {
        public void handler() {
            int t = DIRBYTE();
            m6809.a = (m6809.a ^ t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.a);
        }
    };

    public static opcode adca_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE();
            r = (m6809.a + t + (m6809.cc & CC_C)) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6809.a, t, r);
            SET_H(m6809.a, t, r);
            m6809.a = r & 0xFF;
        }
    };

    public static opcode ora_di = new opcode() {
        public void handler() {
            int t = DIRBYTE();
            m6809.a = (m6809.a | t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.a);
        }
    };

    public static opcode adda_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE();
            r = (m6809.a + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6809.a, t, r);
            SET_H(m6809.a, t, r);
            m6809.a = r & 0xFF;
        }
    };

    public static opcode cmpx_di = new opcode() {
        public void handler() {
            int r, d;
            int b = DIRWORD();
            d = m6809.x;
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode cmpy_di = new opcode() {
        public void handler() {
            int r, d;
            int b = DIRWORD();
            d = m6809.y;
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode cmps_di = new opcode() {
        public void handler() {
            int/*UINT32*/ r, d;
            int b = DIRWORD();
            d = m6809.s;
            r = (d - b);
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode jsr_di = new opcode() {
        public void handler() {
            DIRECT();
            PUSHWORD(m6809.pc);
            m6809.pc = ea & 0xFFFF;
            CHANGE_PC();
        }
    };

    public static opcode ldx_di = new opcode() {
        public void handler() {
            m6809.x = DIRWORD();
            CLR_NZV();
            SET_NZ16(m6809.x);
        }
    };

    public static opcode ldy_di = new opcode() {
        public void handler() {
            m6809.y = DIRWORD();
            CLR_NZV();
            SET_NZ16(m6809.y);
        }
    };

    public static opcode stx_di = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(m6809.x);
            DIRECT();
            WM16(ea, m6809.x);
        }
    };

    public static opcode sty_di = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(m6809.y);
            DIRECT();
            WM16(ea, m6809.y);
        }
    };

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

    public static opcode cmpa_ix = new opcode() {
        public void handler() {
            int t, r;
            fetch_effective_address();
            t = RM(ea);
            r = (m6809.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.a, t, r);
        }
    };

    public static opcode sbca_ix = new opcode() {
        public void handler() {
            int t, r;
            fetch_effective_address();
            t = RM(ea);
            r = (m6809.a - t - (m6809.cc & CC_C)) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.a, t, r);
            m6809.a = r & 0xFF;
        }
    };

    public static opcode subd_ix = new opcode() {
        public void handler() {
            int r, d;
            fetch_effective_address();
            int b;
            b = RM16(ea);
            d = getDreg();
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r);
        }
    };

    public static opcode cmpd_ix = new opcode() {
        public void handler() {
            int r, d;
            int b;
            fetch_effective_address();
            b = RM16(ea);
            d = getDreg();
            r = (d - b);
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode cmpu_ix = new opcode() {
        public void handler() {
            int r;
            int b;
            fetch_effective_address();
            b = RM16(ea);
            r = m6809.u - b;
            CLR_NZVC();
            SET_FLAGS16(m6809.u, b, r);
        }
    };

    public static opcode anda_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            int t = RM(ea);
            m6809.a = (m6809.a & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.a);
        }
    };

    public static opcode bita_ix = new opcode() {
        public void handler() {
            int t, r;
            fetch_effective_address();
            t = RM(ea);
            r = (m6809.a & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    public static opcode lda_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            m6809.a = RM(ea) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.a);
        }
    };

    public static opcode sta_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            CLR_NZV();
            SET_NZ8(m6809.a);
            WM(ea, m6809.a);
        }
    };

    public static opcode eora_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            int t = RM(ea);
            m6809.a = (m6809.a ^ t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.a);
        }
    };

    public static opcode adca_ix = new opcode() {
        public void handler() {
            int t, r;
            fetch_effective_address();
            t = RM(ea);
            r = (m6809.a + t + (m6809.cc & CC_C)) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6809.a, t, r);
            SET_H(m6809.a, t, r);
            m6809.a = r & 0xFF;
        }
    };

    public static opcode ora_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            int t = RM(ea);
            m6809.a = (m6809.a | t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.a);
        }
    };

    public static opcode adda_ix = new opcode() {
        public void handler() {
            int t, r;
            fetch_effective_address();
            t = RM(ea);
            r = (m6809.a + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6809.a, t, r);
            SET_H(m6809.a, t, r);
            m6809.a = r & 0xFF;
        }
    };

    public static opcode cmpx_ix = new opcode() {
        public void handler() {
            int/*UINT32*/ r, d;
            fetch_effective_address();
            int b = RM16(ea);
            d = m6809.x;
            r = (d - b);
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode cmpy_ix = new opcode() {
        public void handler() {
            int/*UINT32*/ r, d;
            fetch_effective_address();
            int b = RM16(ea);
            d = m6809.y;
            r = (d - b);
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode cmps_ix = new opcode() {
        public void handler() {
            int/*UINT32*/ r, d;
            fetch_effective_address();
            int b = RM16(ea);
            d = m6809.s;
            r = (d - b);
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode jsr_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            PUSHWORD(m6809.pc);
            m6809.pc = ea & 0xFFFF;
            CHANGE_PC();
        }
    };

    public static opcode ldx_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            m6809.x = RM16(ea) & 0xFFFF;
            CLR_NZV();
            SET_NZ16(m6809.x);
        }
    };

    public static opcode ldy_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            m6809.y = RM16(ea) & 0xFFFF;
            CLR_NZV();
            SET_NZ16(m6809.y);
        }
    };

    public static opcode stx_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            CLR_NZV();
            SET_NZ16(m6809.x);
            WM16(ea, m6809.x);
        }
    };

    public static opcode sty_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            CLR_NZV();
            SET_NZ16(m6809.y);
            WM16(ea, m6809.y);
        }
    };

    public static opcode suba_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE();
            r = (m6809.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.a, t, r);
            m6809.a = r & 0xFF;
        }
    };

    public static opcode cmpa_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE();
            r = (m6809.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.a, t, r);
        }
    };

    public static opcode sbca_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE();
            r = (m6809.a - t - (m6809.cc & CC_C)) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.a, t, r);
            m6809.a = r & 0xFF;
        }
    };

    public static opcode subd_ex = new opcode() {
        public void handler() {
            int r, d;
            int b;
            b = EXTWORD();
            d = getDreg();
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r);
        }
    };

    public static opcode cmpd_ex = new opcode() {
        public void handler() {
            int r, d;
            int b = EXTWORD();
            d = getDreg();
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode cmpu_ex = new opcode() {
        public void handler() {
            int/*UINT32*/ r, d;
            int b = EXTWORD();
            d = m6809.u;
            r = (d - b);
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode anda_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE();
            m6809.a = (m6809.a & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.a);
        }
    };

    public static opcode bita_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE();
            r = (m6809.a & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    public static opcode lda_ex = new opcode() {
        public void handler() {
            m6809.a = EXTBYTE();
            CLR_NZV();
            SET_NZ8(m6809.a);
        }
    };

    public static opcode sta_ex = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(m6809.a);
            EXTENDED();
            WM(ea, m6809.a);
        }
    };

    public static opcode eora_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE();
            m6809.a = (m6809.a ^ t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.a);
        }
    };

    public static opcode adca_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE();
            r = (m6809.a + t + (m6809.cc & CC_C)) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6809.a, t, r);
            SET_H(m6809.a, t, r);
            m6809.a = r & 0xFF;
        }
    };

    public static opcode ora_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE();
            m6809.a = (m6809.a | t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.a);
        }
    };

    public static opcode adda_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE();
            r = (m6809.a + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6809.a, t, r);
            SET_H(m6809.a, t, r);
            m6809.a = r & 0xFF;
        }
    };

    public static opcode cmpx_ex = new opcode() {
        public void handler() {
            int/*UINT32*/ r, d;
            int b = EXTWORD();
            d = m6809.x;
            r = (d - b);
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode cmpy_ex = new opcode() {
        public void handler() {
            int/*UINT32*/ r, d;
            int b = EXTWORD();
            d = m6809.y;
            r = (d - b);
            CLR_NZVC();
            SET_FLAGS16(d, b, r);;
        }
    };

    public static opcode cmps_ex = new opcode() {
        public void handler() {
            int/*UINT32*/ r, d;
            int b = EXTWORD();
            d = m6809.s;
            r = (d - b);
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    public static opcode jsr_ex = new opcode() {
        public void handler() {
            EXTENDED();
            PUSHWORD(m6809.pc);
            m6809.pc = ea & 0xFFFF;
            CHANGE_PC();
        }
    };

    public static opcode ldx_ex = new opcode() {
        public void handler() {
            m6809.x = EXTWORD();
            CLR_NZV();
            SET_NZ16(m6809.x);
        }
    };

    public static opcode ldy_ex = new opcode() {
        public void handler() {
            m6809.y = EXTWORD();
            CLR_NZV();
            SET_NZ16(m6809.y);
        }
    };

    public static opcode stx_ex = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(m6809.x);
            EXTENDED();
            WM16(ea, m6809.x);
        }
    };

    public static opcode sty_ex = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(m6809.y);
            EXTENDED();
            WM16(ea, m6809.y);
        }
    };

    public static opcode subb_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE();
            r = (m6809.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.b, t, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode cmpb_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE();
            r = (m6809.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.b, t, r);
        }
    };

    public static opcode sbcb_im = new opcode() {
        public void handler() {
            /*UINT16*/
            int t, r;
            t = IMMBYTE();
            r = (m6809.b - t - (m6809.cc & CC_C)) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.b, t, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode addd_im = new opcode() {
        public void handler() {
            int r, d;
            int b;
            b = IMMWORD();
            d = getDreg();
            r = d + b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r);
        }
    };

    public static opcode andb_im = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            m6809.b = (m6809.b & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.b);
        }
    };

    public static opcode bitb_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE();
            r = (m6809.b & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    public static opcode ldb_im = new opcode() {
        public void handler() {
            m6809.b = IMMBYTE();
            CLR_NZV();
            SET_NZ8(m6809.b);
        }
    };

    /* is this a legal instruction? */
    public static opcode stb_im = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(m6809.b);
            IMM8();
            WM(ea, m6809.b);
        }
    };

    public static opcode eorb_im = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            m6809.b = (m6809.b ^ t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.b);
        }
    };

    public static opcode adcb_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE();
            r = (m6809.b + t + (m6809.cc & CC_C)) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6809.b, t, r);
            SET_H(m6809.b, t, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode orb_im = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            m6809.b = (m6809.b | t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.b);
        }
    };

    public static opcode addb_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE();
            r = (m6809.b + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6809.b, t, r);
            SET_H(m6809.b, t, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode ldd_im = new opcode() {
        public void handler() {
            int tmp = IMMWORD();
            setDreg(tmp);
            CLR_NZV();
            SET_NZ16(tmp);
        }
    };

    /* is this a legal instruction? */
    public static opcode std_im = new opcode() {
        public void handler() {
            CLR_NZV();
            int temp = getDreg();
            SET_NZ16(temp);
            IMM16();
            WM16(ea, temp);
        }
    };

    public static opcode ldu_im = new opcode() {
        public void handler() {
            m6809.u = IMMWORD() & 0xFFFF;
            CLR_NZV();
            SET_NZ16(m6809.u);
        }
    };

    public static opcode lds_im = new opcode() {
        public void handler() {
            m6809.s = IMMWORD();
            CLR_NZV();
            SET_NZ16(m6809.s);
            m6809.int_state |= M6809_LDS;
        }
    };

    /* is this a legal instruction? */
    public static opcode stu_im = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(m6809.u);
            IMM16();
            WM16(ea, m6809.u);
        }
    };

    /* is this a legal instruction? */
    public static opcode sts_im = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(m6809.s);
            IMM16();
            WM16(ea, m6809.s);
        }
    };

    public static opcode subb_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE();
            r = (m6809.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.b, t, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode cmpb_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE();
            r = (m6809.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.b, t, r);
        }
    };

    public static opcode sbcb_di = new opcode() {
        public void handler() {
            /*UINT16*/
            int t, r;
            t = DIRBYTE();
            r = (m6809.b - t - (m6809.cc & CC_C)) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.b, t, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode addd_di = new opcode() {
        public void handler() {
            int r, d;
            int b;
            b = DIRWORD();
            d = getDreg();
            r = d + b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r);
        }
    };

    public static opcode andb_di = new opcode() {
        public void handler() {
            int t = DIRBYTE();
            m6809.b = (m6809.b & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.b);
        }
    };

    public static opcode bitb_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE();
            r = (m6809.b & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    public static opcode ldb_di = new opcode() {
        public void handler() {
            m6809.b = DIRBYTE();
            CLR_NZV();
            SET_NZ8(m6809.b);
        }
    };

    public static opcode stb_di = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(m6809.b);
            DIRECT();
            WM(ea, m6809.b);
        }
    };

    public static opcode eorb_di = new opcode() {
        public void handler() {
            int t = DIRBYTE();
            m6809.b = (m6809.b ^ t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.b);
        }
    };

    public static opcode adcb_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE();
            r = (m6809.b + t + (m6809.cc & CC_C)) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6809.b, t, r);
            SET_H(m6809.b, t, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode orb_di = new opcode() {
        public void handler() {
            int t = DIRBYTE();
            m6809.b = (m6809.b | t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.b);
        }
    };

    public static opcode addb_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE();
            r = (m6809.b + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6809.b, t, r);
            SET_H(m6809.b, t, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode ldd_di = new opcode() {
        public void handler() {
            int tmp = DIRWORD();
            setDreg(tmp);
            CLR_NZV();
            SET_NZ16(tmp);
        }
    };

    public static opcode std_di = new opcode() {
        public void handler() {
            CLR_NZV();
            int temp = getDreg();
            SET_NZ16(temp);
            DIRECT();
            WM16(ea, temp);
        }
    };

    public static opcode ldu_di = new opcode() {
        public void handler() {
            m6809.u = DIRWORD();
            CLR_NZV();
            SET_NZ16(m6809.u);
        }
    };

    public static opcode lds_di = new opcode() {
        public void handler() {
            m6809.s = DIRWORD();
            CLR_NZV();
            SET_NZ16(m6809.s);
            m6809.int_state |= M6809_LDS;
        }
    };

    public static opcode stu_di = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(m6809.u);
            DIRECT();
            WM16(ea, m6809.u);
        }
    };

    public static opcode sts_di = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(m6809.s);
            DIRECT();
            WM16(ea, m6809.s);
        }
    };
    public static opcode subb_ix = new opcode() {
        public void handler() {
            int t, r;
            fetch_effective_address();
            t = RM(ea);
            r = (m6809.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.b, t, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode cmpb_ix = new opcode() {
        public void handler() {
            int t, r;
            fetch_effective_address();
            t = RM(ea);
            r = (m6809.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.b, t, r);
        }
    };

    public static opcode sbcb_ix = new opcode() {
        public void handler() {
            /*UINT16*/
            int t, r;
            fetch_effective_address();
            t = RM(ea);
            r = (m6809.b - t - (m6809.cc & CC_C)) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.b, t, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode addd_ix = new opcode() {
        public void handler() {
            int r, d;
            int b;
            fetch_effective_address();
            b = RM16(ea);
            d = getDreg();
            r = d + b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r);
        }
    };

    public static opcode andb_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            int t = RM(ea);
            m6809.b = (m6809.b & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.b);
        }
    };

    public static opcode bitb_ix = new opcode() {
        public void handler() {
            int t, r;
            fetch_effective_address();
            t = RM(ea);
            r = (m6809.b & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    public static opcode ldb_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            m6809.b = RM(ea);
            CLR_NZV();
            SET_NZ8(m6809.b);
        }
    };

    public static opcode stb_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            CLR_NZV();
            SET_NZ8(m6809.b);
            WM(ea, m6809.b);
        }
    };

    public static opcode eorb_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            int t = RM(ea);
            m6809.b = (m6809.b ^ t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.b);
        }
    };

    public static opcode adcb_ix = new opcode() {
        public void handler() {
            int t, r;
            fetch_effective_address();
            t = RM(ea);
            r = (m6809.b + t + (m6809.cc & CC_C)) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6809.b, t, r);
            SET_H(m6809.b, t, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode orb_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            int t = RM(ea);
            m6809.b = (m6809.b | t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.b);
        }
    };

    public static opcode addb_ix = new opcode() {
        public void handler() {
            int t, r;
            fetch_effective_address();
            t = RM(ea);
            r = (m6809.b + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6809.b, t, r);
            SET_H(m6809.b, t, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode ldd_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            int tmp = RM16(ea);
            setDreg(tmp);
            CLR_NZV();
            SET_NZ16(tmp);
        }
    };

    public static opcode std_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            CLR_NZV();
            int temp = getDreg();
            SET_NZ16(temp);
            WM16(ea, temp);
        }
    };

    public static opcode ldu_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            m6809.u = RM16(ea);
            CLR_NZV();
            SET_NZ16(m6809.u);
        }
    };

    public static opcode lds_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            m6809.s = RM16(ea);
            CLR_NZV();
            SET_NZ16(m6809.s);
            m6809.int_state |= M6809_LDS;
        }
    };

    public static opcode stu_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            CLR_NZV();
            SET_NZ16(m6809.u);
            WM16(ea, m6809.u);
        }
    };

    public static opcode sts_ix = new opcode() {
        public void handler() {
            fetch_effective_address();
            CLR_NZV();
            SET_NZ16(m6809.s);
            WM16(ea, m6809.s);
        }
    };
    public static opcode subb_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE();
            r = (m6809.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.b, t, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode cmpb_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE();
            r = (m6809.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.b, t, r);
        }
    };

    public static opcode sbcb_ex = new opcode() {
        public void handler() {
            /*UINT16*/
            int t, r;
            t = EXTBYTE();
            r = (m6809.b - t - (m6809.cc & CC_C)) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6809.b, t, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode addd_ex = new opcode() {
        public void handler() {
            int r, d;
            int b;
            b = EXTWORD();
            d = getDreg();
            r = d + b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r);
        }
    };

    public static opcode andb_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE();
            m6809.b = (m6809.b & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.b);
        }
    };

    public static opcode bitb_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE();
            r = (m6809.b & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    public static opcode ldb_ex = new opcode() {
        public void handler() {
            m6809.b = EXTBYTE();
            CLR_NZV();
            SET_NZ8(m6809.b);
        }
    };

    public static opcode stb_ex = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(m6809.b);
            EXTENDED();
            WM(ea, m6809.b);
        }
    };

    public static opcode eorb_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE();
            m6809.b = (m6809.b ^ t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.b);
        }
    };

    public static opcode adcb_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE();
            r = (m6809.b + t + (m6809.cc & CC_C)) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6809.b, t, r);
            SET_H(m6809.b, t, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode orb_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE();
            m6809.b = (m6809.b | t) & 0xFF;
            CLR_NZV();
            SET_NZ8(m6809.b);
        }
    };

    public static opcode addb_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE();
            r = (m6809.b + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6809.b, t, r);
            SET_H(m6809.b, t, r);
            m6809.b = r & 0xFF;
        }
    };

    public static opcode ldd_ex = new opcode() {
        public void handler() {
            int temp = EXTWORD();
            setDreg(temp);
            CLR_NZV();
            SET_NZ16(getDreg());
        }
    };

    public static opcode std_ex = new opcode() {
        public void handler() {
            CLR_NZV();
            int temp = getDreg();
            SET_NZ16(temp);
            EXTENDED();
            WM16(ea, temp);
        }
    };

    public static opcode ldu_ex = new opcode() {
        public void handler() {
            m6809.u = EXTWORD();
            CLR_NZV();
            SET_NZ16(m6809.u);
        }
    };

    public static opcode lds_ex = new opcode() {
        public void handler() {
            m6809.s = EXTWORD();
            CLR_NZV();
            SET_NZ16(m6809.s);
            m6809.int_state |= M6809_LDS;
        }
    };

    public static opcode stu_ex = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(m6809.u);
            EXTENDED();
            WM16(ea, m6809.u);
        }
    };

    public static opcode sts_ex = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(m6809.s);
            EXTENDED();
            WM16(ea, m6809.s);
        }
    };

    public static opcode pref10 = new opcode() {
        public void handler() {
            int ireg2 = ROP(m6809.pc) & 0xFF;
            m6809.pc = (m6809.pc + 1) & 0xFFFF;
            switch (ireg2) {
                case 0x21:
                    lbrn.handler();
                    m6809_ICount[0] -= 5;
                    break;
                case 0x22:
                    lbhi.handler();
                    m6809_ICount[0] -= 5;
                    break;
                case 0x23:
                    lbls.handler();
                    m6809_ICount[0] -= 5;
                    break;
                case 0x24:
                    lbcc.handler();
                    m6809_ICount[0] -= 5;
                    break;
                case 0x25:
                    lbcs.handler();
                    m6809_ICount[0] -= 5;
                    break;
                case 0x26:
                    lbne.handler();
                    m6809_ICount[0] -= 5;
                    break;
                case 0x27:
                    lbeq.handler();
                    m6809_ICount[0] -= 5;
                    break;
                case 0x28:
                    lbvc.handler();
                    m6809_ICount[0] -= 5;
                    break;
                case 0x29:
                    lbvs.handler();
                    m6809_ICount[0] -= 5;
                    break;
                case 0x2a:
                    lbpl.handler();
                    m6809_ICount[0] -= 5;
                    break;
                case 0x2b:
                    lbmi.handler();
                    m6809_ICount[0] -= 5;
                    break;
                case 0x2c:
                    lbge.handler();
                    m6809_ICount[0] -= 5;
                    break;
                case 0x2d:
                    lblt.handler();
                    m6809_ICount[0] -= 5;
                    break;
                case 0x2e:
                    lbgt.handler();
                    m6809_ICount[0] -= 5;
                    break;
                case 0x2f:
                    lble.handler();
                    m6809_ICount[0] -= 5;
                    break;

                case 0x3f:
                    swi2.handler();
                    m6809_ICount[0] -= 20;
                    break;

                case 0x83:
                    cmpd_im.handler();
                    m6809_ICount[0] -= 5;
                    break;
                case 0x8c:
                    cmpy_im.handler();
                    m6809_ICount[0] -= 5;
                    break;
                case 0x8e:
                    ldy_im.handler();
                    m6809_ICount[0] -= 4;
                    break;
                case 0x8f:
                    sty_im.handler();
                    m6809_ICount[0] -= 4;
                    break;

                case 0x93:
                    cmpd_di.handler();
                    m6809_ICount[0] -= 7;
                    break;
                case 0x9c:
                    cmpy_di.handler();
                    m6809_ICount[0] -= 7;
                    break;
                case 0x9e:
                    ldy_di.handler();
                    m6809_ICount[0] -= 6;
                    break;
                case 0x9f:
                    sty_di.handler();
                    m6809_ICount[0] -= 6;
                    break;

                case 0xa3:
                    cmpd_ix.handler();
                    m6809_ICount[0] -= 7;
                    break;
                case 0xac:
                    cmpy_ix.handler();
                    m6809_ICount[0] -= 7;
                    break;
                case 0xae:
                    ldy_ix.handler();
                    m6809_ICount[0] -= 6;
                    break;
                case 0xaf:
                    sty_ix.handler();
                    m6809_ICount[0] -= 6;
                    break;

                case 0xb3:
                    cmpd_ex.handler();
                    m6809_ICount[0] -= 8;
                    break;
                case 0xbc:
                    cmpy_ex.handler();
                    m6809_ICount[0] -= 8;
                    break;
                case 0xbe:
                    ldy_ex.handler();
                    m6809_ICount[0] -= 7;
                    break;
                case 0xbf:
                    sty_ex.handler();
                    m6809_ICount[0] -= 7;
                    break;

                case 0xce:
                    lds_im.handler();
                    m6809_ICount[0] -= 4;
                    break;
                case 0xcf:
                    sts_im.handler();
                    m6809_ICount[0] -= 4;
                    break;

                case 0xde:
                    lds_di.handler();
                    m6809_ICount[0] -= 6;
                    break;
                case 0xdf:
                    sts_di.handler();
                    m6809_ICount[0] -= 6;
                    break;

                case 0xee:
                    lds_ix.handler();
                    m6809_ICount[0] -= 6;
                    break;
                case 0xef:
                    sts_ix.handler();
                    m6809_ICount[0] -= 6;
                    break;

                case 0xfe:
                    lds_ex.handler();
                    m6809_ICount[0] -= 7;
                    break;
                case 0xff:
                    sts_ex.handler();
                    m6809_ICount[0] -= 7;
                    break;

                default:
                    illegal.handler();
                    break;
            }
        }
    };

    public static opcode pref11 = new opcode() {
        public void handler() {
            int ireg2 = ROP(m6809.pc) & 0xFF;
            m6809.pc = (m6809.pc + 1) & 0xFFFF;
            switch (ireg2) {
                case 0x3f:
                    swi3.handler();
                    m6809_ICount[0] -= 20;
                    break;

                case 0x83:
                    cmpu_im.handler();
                    m6809_ICount[0] -= 5;
                    break;
                case 0x8c:
                    cmps_im.handler();
                    m6809_ICount[0] -= 5;
                    break;

                case 0x93:
                    cmpu_di.handler();
                    m6809_ICount[0] -= 7;
                    break;
                case 0x9c:
                    cmps_di.handler();
                    m6809_ICount[0] -= 7;
                    break;

                case 0xa3:
                    cmpu_ix.handler();
                    m6809_ICount[0] -= 7;
                    break;
                case 0xac:
                    cmps_ix.handler();
                    m6809_ICount[0] -= 7;
                    break;

                case 0xb3:
                    cmpu_ex.handler();
                    m6809_ICount[0] -= 8;
                    break;
                case 0xbc:
                    cmps_ex.handler();
                    m6809_ICount[0] -= 8;
                    break;

                default:
                    illegal.handler();
                    break;
            }
        }
    };

    public static abstract interface opcode {

        public abstract void handler();
    }
}
