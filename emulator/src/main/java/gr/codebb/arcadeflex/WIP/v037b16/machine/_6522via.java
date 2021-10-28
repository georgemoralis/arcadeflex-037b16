/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.WIP.v037b16.machine;

import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v056.mame.timer.*;
import static mame037b16.mame.Machine;
import static gr.codebb.arcadeflex.WIP.v037b16.machine._6522viaH.*;

public class _6522via {

    /**
     * ***************** internal VIA data structure ******************
     */
    public static class via6522 {

        via6522_interface intf;

        int/*UINT8*/ u8_in_a;
        int/*UINT8*/ u8_in_ca1;
        int/*UINT8*/ u8_in_ca2;
        int/*UINT8*/ u8_out_a;
        int/*UINT8*/ u8_out_ca2;
        int/*UINT8*/ u8_ddr_a;

        int/*UINT8*/ u8_in_b;
        int/*UINT8*/ u8_in_cb1;
        int/*UINT8*/ u8_in_cb2;
        int/*UINT8*/ out_b;
        int/*UINT8*/ out_cb2;
        int/*UINT8*/ ddr_b;

        int/*UINT8*/ t1cl;
        int/*UINT8*/ t1ch;
        int/*UINT8*/ t1ll;
        int/*UINT8*/ t1lh;
        int/*UINT8*/ t2cl;
        int/*UINT8*/ t2ch;
        int/*UINT8*/ t2ll;
        int/*UINT8*/ t2lh;

        int/*UINT8*/ sr;
        int/*UINT8*/ pcr;
        int/*UINT8*/ acr;
        int/*UINT8*/ ier;
        int/*UINT8*/ ifr;

        Object t1;
        double time1;
        Object t2;
        double time2;

        double cycles_to_sec;
        double sec_to_cycles;

        public static via6522[] create(int n) {
            via6522[] a = new via6522[n];
            for (int k = 0; k < n; k++) {
                a[k] = new via6522();
            }
            return a;
        }
    }

    /**
     * ***************** convenince macros and defines ******************
     */
    public static double V_CYCLES_TO_TIME(via6522 v, int c) {
        return ((double) (c) * v.cycles_to_sec);
    }

    public static int V_TIME_TO_CYCLES(via6522 v, double t) {
        return ((int) ((t) * v.sec_to_cycles));
    }

    /* Macros for PCR */
    public static boolean CA1_LOW_TO_HIGH(int c) {
        return (c & 0x01) != 0;
    }

    public static boolean CA1_HIGH_TO_LOW(int c) {
        return ((c & 0x01)) == 0;
    }

    public static boolean CB1_LOW_TO_HIGH(int c) {
        return (c & 0x10) != 0;
    }

    public static boolean CB1_HIGH_TO_LOW(int c) {
        return ((c & 0x10)) == 0;
    }

    public static boolean CA2_INPUT(int c) {
        return ((c & 0x08)) == 0;
    }

    public static boolean CA2_LOW_TO_HIGH(int c) {
        return ((c & 0x0c) == 0x04);
    }

    public static boolean CA2_HIGH_TO_LOW(int c) {
        return ((c & 0x0c) == 0x00);
    }

    public static boolean CA2_IND_IRQ(int c) {
        return ((c & 0x0a) == 0x02);
    }

    public static boolean CA2_OUTPUT(int c) {
        return (c & 0x08) != 0;
    }

    public static boolean CA2_AUTO_HS(int c) {
        return ((c & 0x0c) == 0x08);
    }

    public static boolean CA2_HS_OUTPUT(int c) {
        return ((c & 0x0e) == 0x08);
    }

    public static boolean CA2_PULSE_OUTPUT(int c) {
        return ((c & 0x0e) == 0x0a);
    }

    public static boolean CA2_FIX_OUTPUT(int c) {
        return ((c & 0x0c) == 0x0c);
    }

    public static boolean CA2_OUTPUT_LEVEL(int c) {
        return ((c & 0x02) >> 1) != 0;
    }

    public static boolean CB2_INPUT(int c) {
        return ((c & 0x80) == 0);
    }

    public static boolean CB2_LOW_TO_HIGH(int c) {
        return ((c & 0xc0) == 0x40);
    }

    public static boolean CB2_HIGH_TO_LOW(int c) {
        return ((c & 0xc0) == 0x00);
    }

    public static boolean CB2_IND_IRQ(int c) {
        return ((c & 0xa0) == 0x20);
    }

    public static boolean CB2_OUTPUT(int c) {
        return (c & 0x80) != 0;
    }

    public static boolean CB2_AUTO_HS(int c) {
        return ((c & 0xc0) == 0x80);
    }

    public static boolean CB2_HS_OUTPUT(int c) {
        return ((c & 0xe0) == 0x80);
    }

    public static boolean CB2_PULSE_OUTPUT(int c) {
        return ((c & 0xe0) == 0xa0);
    }

    public static boolean CB2_FIX_OUTPUT(int c) {
        return ((c & 0xc0) == 0xc0);
    }

    public static boolean CB2_OUTPUT_LEVEL(int c) {
        return ((c & 0x20) >> 5) != 0;
    }

    /* Macros for ACR */
    public static boolean PA_LATCH_ENABLE(int c) {
        return (c & 0x01) != 0;
    }

    public static boolean PB_LATCH_ENABLE(int c) {
        return (c & 0x02) != 0;
    }

    public static boolean SR_DISABLED(int c) {
        return ((c & 0x1c)) == 0;
    }

    public static boolean SI_T2_CONTROL(int c) {
        return ((c & 0x1c) == 0x04);
    }

    public static boolean SI_O2_CONTROL(int c) {
        return ((c & 0x1c) == 0x08);
    }

    public static boolean SI_EXT_CONTROL(int c) {
        return ((c & 0x1c) == 0x0c);
    }

    public static boolean SO_T2_RATE(int c) {
        return ((c & 0x1c) == 0x10);
    }

    public static boolean SO_T2_CONTROL(int c) {
        return ((c & 0x1c) == 0x14);
    }

    public static boolean SO_O2_CONTROL(int c) {
        return ((c & 0x1c) == 0x18);
    }

    public static boolean SO_EXT_CONTROL(int c) {
        return ((c & 0x1c) == 0x1c);
    }

    public static boolean T1_SET_PB7(int c) {
        return (c & 0x80) != 0;
    }

    public static boolean T1_CONTINUOUS(int c) {
        return (c & 0x40) != 0;
    }

    public static boolean T2_COUNT_PB6(int c) {
        return (c & 0x20) != 0;
    }

    /* Interrupt flags */
    public static final int INT_CA2 = 0x01;
    public static final int INT_CA1 = 0x02;
    public static final int INT_SR = 0x04;
    public static final int INT_CB2 = 0x08;
    public static final int INT_CB1 = 0x10;
    public static final int INT_T2 = 0x20;
    public static final int INT_T1 = 0x40;
    public static final int INT_ANY = 0x80;

    public static void CLR_PA_INT(via6522 v) {
        via_clear_int(v, INT_CA1 | ((!CA2_IND_IRQ(v.pcr)) ? INT_CA2 : 0));
    }

    public static void CLR_PB_INT(via6522 v) {
        via_clear_int(v, INT_CB1 | ((!CB2_IND_IRQ(v.pcr)) ? INT_CB2 : 0));
    }

    public static final int IFR_DELAY = 3;

    public static int TIMER1_VALUE(via6522 v) {
        return (v.t1ll + (v.t1lh << 8));
    }

    public static int TIMER2_VALUE(via6522 v) {
        return (v.t2ll + (v.t2lh << 8));
    }

    /**
     * ***************** static variables ******************
     */
    static via6522[] via = via6522.create(MAX_VIA);

    /**
     * ***************** configuration ******************
     */
    public static void via_set_clock(int which, int clock) {
        via[which].sec_to_cycles = clock;
        via[which].cycles_to_sec = 1.0 / via[which].sec_to_cycles;
    }

    public static void via_config(int which, via6522_interface intf) {
        if (which >= MAX_VIA) {
            return;
        }
        via[which].intf = intf;
        via[which].t1ll = 0xf3;
        /* via at 0x9110 in vic20 show these values */
        via[which].t1lh = 0xb5;
        /* ports are not written by kernel! */
        via[which].t2ll = 0xff;
        /* taken from vice */
        via[which].t2lh = 0xff;
        via[which].time2 = via[which].time1 = timer_get_time();

        /* Default clock is from CPU1 */
        via_set_clock(which, Machine.drv.cpu[0].cpu_clock);
    }

    /**
     * ***************** reset ******************
     */
    public static void via_reset() {
        int i;
        via6522 v = new via6522();

        for (i = 0; i < MAX_VIA; i++) {
            v.intf = via[i].intf;
            v.t1ll = via[i].t1ll;
            v.t1lh = via[i].t1lh;
            v.t2ll = via[i].t2ll;
            v.t2lh = via[i].t2lh;
            v.time1 = via[i].time1;
            v.time2 = via[i].time2;
            v.sec_to_cycles = via[i].sec_to_cycles;
            v.cycles_to_sec = via[i].cycles_to_sec;

            via[i] = v;
        }
    }

    /**
     * ***************** external interrupt check ******************
     */
    static void via_set_int(via6522 v, int data) {
        v.ifr |= data;

        if (v.ier != 0 & v.ifr != 0) {
            v.ifr |= INT_ANY;
            if (v.intf.irq_func != null) {
                (v.intf.irq_func).handler(ASSERT_LINE);
            }
        }
    }

    static void via_clear_int(via6522 v, int data) {
        v.ifr = (v.ifr & ~data) & 0x7f;

        if (v.ifr != 0 & v.ier != 0) {
            v.ifr |= INT_ANY;
        } else if (v.intf.irq_func != null) {
            (v.intf.irq_func).handler(CLEAR_LINE);
        }
    }

    /**
     * ***************** Timer timeouts ************************
     */
    public static timer_callback via_t1_timeout = new timer_callback() {
        public void handler(int which) {
            via6522 v = via[which];

            if (T1_CONTINUOUS(v.acr)) {
                if (T1_SET_PB7(v.acr)) {
                    v.out_b ^= 0x80;
                }
                timer_reset(v.t1, V_CYCLES_TO_TIME(v, TIMER1_VALUE(v) + IFR_DELAY));
            } else {
                if (T1_SET_PB7(v.acr)) {
                    v.out_b |= 0x80;
                }
                v.t1 = 0;
                v.time1 = timer_get_time();
            }
            if (v.intf.out_b_func != null && v.ddr_b != 0) {
                v.intf.out_b_func.handler(0, v.out_b & v.ddr_b);
            }

            if ((v.ifr & INT_T1) == 0) {
                via_set_int(v, INT_T1);
            }
        }
    };

    public static timer_callback via_t2_timeout = new timer_callback() {
        public void handler(int which) {
            via6522 v = via[which];

            if (v.intf.t2_callback != null) {
                v.intf.t2_callback.handler(timer_timeelapsed(v.t2));
            }

            v.t2 = 0;
            v.time2 = timer_get_time();

            if ((v.ifr & INT_T2) == 0) {
                via_set_int(v, INT_T2);
            }
        }
    };

    /**
     * ***************** CPU interface for VIA read ******************
     */
    public static int via_read(int which, int offset) {
        via6522 v = via[which];
        int val = 0;

        offset &= 0xf;

        switch (offset) {
            case VIA_PB:
                /* update the input */
                if (!PB_LATCH_ENABLE(v.acr)) {
                    if (v.intf.in_b_func != null) {
                        v.u8_in_b = v.intf.in_b_func.handler(0) & 0xFF;
                    }
                }

                CLR_PB_INT(v);

                /* combine input and output values, hold DDRB bit 7 high if T1_SET_PB7 */
                if (T1_SET_PB7(v.acr)) {
                    val = (v.out_b & (v.ddr_b | 0x80)) | (v.u8_in_b & ~(v.ddr_b | 0x80));
                } else {
                    val = (v.out_b & v.ddr_b) + (v.u8_in_b & ~v.ddr_b);
                }
                break;

            case VIA_PA:
                /* update the input */
                if (!PA_LATCH_ENABLE(v.acr)) {
                    if (v.intf.in_a_func != null) {
                        v.u8_in_a = v.intf.in_a_func.handler(0) & 0xFF;
                    }
                }

                /* combine input and output values */
                val = (v.u8_out_a & v.u8_ddr_a) + (v.u8_in_a & ~v.u8_ddr_a);

                CLR_PA_INT(v);

                /* If CA2 is configured as output and in pulse or handshake mode,
			   CA2 is set now */
                if (CA2_AUTO_HS(v.pcr)) {
                    if (v.u8_out_ca2 != 0) {
                        /* set CA2 */
                        v.u8_out_ca2 = 0;

                        /* call the CA2 output function */
                        if (v.intf.out_ca2_func != null) {
                            v.intf.out_ca2_func.handler(0, 0);
                        }
                    }
                }

                break;

            case VIA_PANH:
                /* update the input */
                if (!PA_LATCH_ENABLE(v.acr)) {
                    if (v.intf.in_a_func != null) {
                        v.u8_in_a = v.intf.in_a_func.handler(0) & 0xFF;
                    }
                }

                /* combine input and output values */
                val = (v.u8_out_a & v.u8_ddr_a) + (v.u8_in_a & ~v.u8_ddr_a);
                break;

            case VIA_DDRB:
                val = v.ddr_b;
                break;

            case VIA_DDRA:
                val = v.u8_ddr_a & 0xFF;
                break;

            case VIA_T1CL:
                via_clear_int(v, INT_T1);
                if (v.t1 != null) {
                    val = V_TIME_TO_CYCLES(v, timer_timeleft(v.t1)) & 0xff;
                } else {
                    if (T1_CONTINUOUS(v.acr)) {
                        val = (TIMER1_VALUE(v)
                                - (V_TIME_TO_CYCLES(v, timer_get_time() - v.time1)
                                % TIMER1_VALUE(v)) - 1) & 0xff;
                    } else {
                        val = (0x10000
                                - (V_TIME_TO_CYCLES(v, timer_get_time() - v.time1) & 0xffff)
                                - 1) & 0xff;
                    }
                }
                break;

            case VIA_T1CH:
                if (v.t1 != null) {
                    val = V_TIME_TO_CYCLES(v, timer_timeleft(v.t1)) >> 8;
                } else {
                    if (T1_CONTINUOUS(v.acr)) {
                        val = (TIMER1_VALUE(v)
                                - (V_TIME_TO_CYCLES(v, timer_get_time() - v.time1)
                                % TIMER1_VALUE(v)) - 1) >> 8;
                    } else {
                        val = (0x10000
                                - (V_TIME_TO_CYCLES(v, timer_get_time() - v.time1) & 0xffff)
                                - 1) >> 8;
                    }
                }
                break;

            case VIA_T1LL:
                val = v.t1ll;
                break;

            case VIA_T1LH:
                val = v.t1lh;
                break;

            case VIA_T2CL:
                via_clear_int(v, INT_T2);
                if (v.t2 != null) {
                    val = V_TIME_TO_CYCLES(v, timer_timeleft(v.t2)) & 0xff;
                } else {
                    if (T2_COUNT_PB6(v.acr)) {
                        val = v.t2cl;
                    } else {
                        val = (0x10000
                                - (V_TIME_TO_CYCLES(v, timer_get_time() - v.time2) & 0xffff)
                                - 1) & 0xff;
                    }
                }
                break;

            case VIA_T2CH:
                if (v.t2 != null) {
                    val = V_TIME_TO_CYCLES(v, timer_timeleft(v.t2)) >> 8;
                } else {
                    if (T2_COUNT_PB6(v.acr)) {
                        val = v.t2ch;
                    } else {
                        val = (0x10000
                                - (V_TIME_TO_CYCLES(v, timer_get_time() - v.time2) & 0xffff)
                                - 1) >> 8;
                    }
                }
                break;

            case VIA_SR:
                val = v.sr;
                break;

            case VIA_PCR:
                val = v.pcr;
                break;

            case VIA_ACR:
                val = v.acr;
                break;

            case VIA_IER:
                val = v.ier | 0x80;
                break;

            case VIA_IFR:
                val = v.ifr;
                break;
        }
        return val;
    }

    /**
     * ***************** CPU interface for VIA write ******************
     */
    public static void via_write(int which, int offset, int data) {
        via6522 v = via[which];

        offset &= 0x0f;
        switch (offset) {
            case VIA_PB:
                if (T1_SET_PB7(v.acr)) {
                    v.out_b = (v.out_b & 0x80) | (data & 0x7f);
                } else {
                    v.out_b = data;
                }

                if (v.intf.out_b_func != null && v.ddr_b != 0) {
                    v.intf.out_b_func.handler(0, v.out_b & v.ddr_b);
                }

                CLR_PB_INT(v);

                /* If CB2 is configured as output and in pulse or handshake mode,
			   CB2 is set now */
                if (CB2_AUTO_HS(v.pcr)) {
                    if (v.out_cb2 != 0) {
                        /* set CB2 */
                        v.out_cb2 = 0;

                        /* call the CB2 output function */
                        if (v.intf.out_cb2_func != null) {
                            v.intf.out_cb2_func.handler(0, 0);
                        }
                    }
                }
                break;

            case VIA_PA:
                v.u8_out_a = data & 0xFF;
                if (v.intf.out_a_func != null && v.u8_ddr_a != 0) {
                    v.intf.out_a_func.handler(0, v.u8_out_a & v.u8_ddr_a);
                }

                CLR_PA_INT(v);

                /* If CA2 is configured as output and in pulse or handshake mode,
			   CA2 is set now */
                if (CA2_AUTO_HS(v.pcr)) {
                    if (v.u8_out_ca2 != 0) {
                        /* set CA2 */
                        v.u8_out_ca2 = 0;

                        /* call the CA2 output function */
                        if (v.intf.out_ca2_func != null) {
                            v.intf.out_ca2_func.handler(0, 0);
                        }
                    }
                }

                break;

            case VIA_PANH:
                v.u8_out_a = data & 0xFF;
                if (v.intf.out_a_func != null && v.u8_ddr_a != 0) {
                    v.intf.out_a_func.handler(0, v.u8_out_a & v.u8_ddr_a);
                }
                break;

            case VIA_DDRB:
                /* EHC 03/04/2000 - If data direction changed, present output on the lines */
                if (data != v.ddr_b) {
                    v.ddr_b = data;

                    if (v.intf.out_b_func != null && v.ddr_b != 0) {
                        v.intf.out_b_func.handler(0, v.out_b & v.ddr_b);
                    }
                }
                break;

            case VIA_DDRA:
                /* EHC 03/04/2000 - If data direction changed, present output on the lines */
                if (data != v.u8_ddr_a) {
                    v.u8_ddr_a = data & 0xFF;

                    if (v.intf.out_a_func != null && v.u8_ddr_a != 0) {
                        v.intf.out_a_func.handler(0, v.u8_out_a & v.u8_ddr_a);
                    }
                }
                break;

            case VIA_T1CL:
            case VIA_T1LL:
                v.t1ll = data;
                break;

            case VIA_T1LH:
                v.t1lh = data;
                via_clear_int(v, INT_T1);
                break;

            case VIA_T1CH:
                v.t1ch = v.t1lh = data;
                v.t1cl = v.t1ll;

                via_clear_int(v, INT_T1);

                if (T1_SET_PB7(v.acr)) {
                    v.out_b &= 0x7f;
                    if (v.intf.out_b_func != null && v.ddr_b != 0) {
                        v.intf.out_b_func.handler(0, v.out_b & v.ddr_b);
                    }
                }
                if (v.t1 != null) {
                    timer_reset(v.t1, V_CYCLES_TO_TIME(v, TIMER1_VALUE(v) + IFR_DELAY));
                } else {
                    v.t1 = timer_set(V_CYCLES_TO_TIME(v, TIMER1_VALUE(v) + IFR_DELAY), which, via_t1_timeout);
                }
                break;

            case VIA_T2CL:
                v.t2ll = data;
                break;

            case VIA_T2CH:
                v.t2ch = v.t2lh = data;
                v.t2cl = v.t2ll;

                via_clear_int(v, INT_T2);

                if (!T2_COUNT_PB6(v.acr)) {
                    if (v.t2 != null) {
                        if (v.intf.t2_callback != null) {
                            v.intf.t2_callback.handler(timer_timeelapsed(v.t2));
                        }
                        timer_reset(v.t2, V_CYCLES_TO_TIME(v, TIMER2_VALUE(v) + IFR_DELAY));
                    } else {
                        v.t2 = timer_set(V_CYCLES_TO_TIME(v, TIMER2_VALUE(v) + IFR_DELAY),
                                which, via_t2_timeout);
                    }
                } else {
                    v.time2 = timer_get_time();
                }
                break;

            case VIA_SR:
                v.sr = data;
                if (v.intf.out_shift_func != null && SO_O2_CONTROL(v.acr)) {
                    v.intf.out_shift_func.handler(data);
                }
                /* kludge for Mac Plus (and 128k, 512k, 512ke) : */
                if (v.intf.out_shift_func2 != null && SO_EXT_CONTROL(v.acr)) {
                    v.intf.out_shift_func2.handler(data);
                    via_set_int(v, INT_SR);
                }
                break;

            case VIA_PCR:
                v.pcr = data;

                if (CA2_FIX_OUTPUT(data) && (CA2_OUTPUT_LEVEL(data) ? 1 : 0 ^ v.u8_out_ca2) != 0) {
                    v.u8_out_ca2 = CA2_OUTPUT_LEVEL(data) ? 1 : 0;
                    if (v.intf.out_ca2_func != null) {
                        v.intf.out_ca2_func.handler(0, v.u8_out_ca2);
                    }
                }

                if (CB2_FIX_OUTPUT(data) && (CB2_OUTPUT_LEVEL(data) ? 1 : 0 ^ v.out_cb2) != 0) {
                    v.out_cb2 = CB2_OUTPUT_LEVEL(data) ? 1 : 0;
                    if (v.intf.out_cb2_func != null) {
                        v.intf.out_cb2_func.handler(0, v.out_cb2);
                    }
                }
                break;

            case VIA_ACR:
                v.acr = data;
                if (T1_SET_PB7(v.acr)) {
                    if (v.t1 != null) {
                        v.out_b &= ~0x80;
                    } else {
                        v.out_b |= 0x80;
                    }

                    if (v.intf.out_b_func != null && v.ddr_b != 0) {
                        v.intf.out_b_func.handler(0, v.out_b & v.ddr_b);
                    }
                }
                if (T1_CONTINUOUS(data)) {
                    if (v.t1 != null) {
                        timer_reset(v.t1, V_CYCLES_TO_TIME(v, TIMER1_VALUE(v) + IFR_DELAY));
                    } else {
                        v.t1 = timer_set(V_CYCLES_TO_TIME(v, TIMER1_VALUE(v) + IFR_DELAY), which, via_t1_timeout);
                    }
                }
                /* kludge for Mac Plus (and 128k, 512k, 512ke) : */
                if (v.intf.si_ready_func != null && SI_EXT_CONTROL(data)) {
                    v.intf.si_ready_func.handler();
                }
                break;

            case VIA_IER:
                if ((data & 0x80) != 0) {
                    v.ier |= data & 0x7f;
                } else {
                    v.ier &= ~(data & 0x7f);
                }

                if ((v.ifr & INT_ANY) != 0) {
                    if (((v.ifr & v.ier) & 0x7f) == 0) {
                        v.ifr &= ~INT_ANY;
                        if (v.intf.irq_func != null) {
                            (v.intf.irq_func).handler(CLEAR_LINE);
                        }
                    }
                } else {
                    if (((v.ier & v.ifr) & 0x7f) != 0) {
                        v.ifr |= INT_ANY;
                        if (v.intf.irq_func != null) {
                            (v.intf.irq_func).handler(ASSERT_LINE);
                        }
                    }
                }
                break;

            case VIA_IFR:
                if ((data & INT_ANY) != 0) {
                    data = 0x7f;
                }
                via_clear_int(v, data);
                break;
        }
    }

    /**
     * ***************** interface setting VIA port A input ******************
     */
    public static void via_set_input_a(int which, int data) {
        via6522 v = via[which];

        /* set the input, what could be easier? */
        v.u8_in_a = data & 0xFF;
    }

    /**
     * ***************** interface setting VIA port CA1 input
     * ******************
     */
    public static void via_set_input_ca1(int which, int data) {
        via6522 v = via[which];

        /* limit the data to 0 or 1 */
        data = data != 0 ? 1 : 0;

        /* handle the active transition */
        if (data != v.u8_in_ca1) {
            if ((CA1_LOW_TO_HIGH(v.pcr) && data != 0) || (CA1_HIGH_TO_LOW(v.pcr) && data == 0)) {
                if (PA_LATCH_ENABLE(v.acr)) {
                    if (v.intf.in_a_func != null) {
                        v.u8_in_a = v.intf.in_a_func.handler(0) & 0xFF;
                    }
                }
                via_set_int(v, INT_CA1);

                /* CA2 is configured as output and in pulse or handshake mode,
				   CA2 is cleared now */
                if (CA2_AUTO_HS(v.pcr)) {
                    if (v.u8_out_ca2 == 0) {
                        /* clear CA2 */
                        v.u8_out_ca2 = 1;

                        /* call the CA2 output function */
                        if (v.intf.out_ca2_func != null) {
                            v.intf.out_ca2_func.handler(0, 1);
                        }
                    }
                }
            }
            v.u8_in_ca1 = data & 0xFF;
        }
    }

    /**
     * ***************** interface setting VIA port CA2 input
     * ******************
     */
    public static void via_set_input_ca2(int which, int data) {
        via6522 v = via[which];

        /* limit the data to 0 or 1 */
        data = data != 0 ? 1 : 0;

        /* CA2 is in input mode */
        if (CA2_INPUT(v.pcr)) {
            /* the new state has caused a transition */
            if (v.u8_in_ca2 != data) {
                /* handle the active transition */
                if ((data != 0 && CA2_LOW_TO_HIGH(v.pcr)) || (data == 0 && CA2_HIGH_TO_LOW(v.pcr))) {
                    /* mark the IRQ */
                    via_set_int(v, INT_CA2);
                }
                /* set the new value for CA2 */
                v.u8_in_ca2 = data & 0xFF;
            }
        }

    }

    /**
     * ***************** interface setting VIA port B input ******************
     */
    public static void via_set_input_b(int which, int data) {
        via6522 v = via[which];

        /* set the input, what could be easier? */
        v.u8_in_b = data & 0xFF;
    }

    /**
     * ***************** interface setting VIA port CB1 input
     * ******************
     */
    public static void via_set_input_cb1(int which, int data) {
        via6522 v = via[which];

        /* limit the data to 0 or 1 */
        data = data != 0 ? 1 : 0;

        /* handle the active transition */
        if (data != v.u8_in_cb1) {
            if ((CB1_LOW_TO_HIGH(v.pcr) && data != 0) || (CB1_HIGH_TO_LOW(v.pcr) && data == 0)) {
                if (PB_LATCH_ENABLE(v.acr)) {
                    if (v.intf.in_b_func != null) {
                        v.u8_in_b = v.intf.in_b_func.handler(0) & 0xFF;
                    }
                }
                via_set_int(v, INT_CB1);

                /* CB2 is configured as output and in pulse or handshake mode,
				   CB2 is cleared now */
                if (CB2_AUTO_HS(v.pcr)) {
                    if (v.out_cb2 == 0) {
                        /* clear CB2 */
                        v.out_cb2 = 1;

                        /* call the CB2 output function */
                        if (v.intf.out_cb2_func != null) {
                            v.intf.out_cb2_func.handler(0, 1);
                        }
                    }
                }
            }
            v.u8_in_cb1 = data & 0xFF;
        }
    }

    /**
     * ***************** interface setting VIA port CB2 input
     * ******************
     */
    public static void via_set_input_cb2(int which, int data) {
        via6522 v = via[which];

        /* limit the data to 0 or 1 */
        data = data != 0 ? 1 : 0;

        /* CB2 is in input mode */
        if (CB2_INPUT(v.pcr)) {
            /* the new state has caused a transition */
            if (v.u8_in_cb2 != data) {
                /* handle the active transition */
                if ((data != 0 && CB2_LOW_TO_HIGH(v.pcr)) || (data == 0 && CB2_HIGH_TO_LOW(v.pcr))) {
                    /* mark the IRQ */
                    via_set_int(v, INT_CB2);
                }
                /* set the new value for CB2 */
                v.u8_in_cb2 = data&0xFF;
            }
        }
    }

    /**
     * ***************** interface to shift data into VIA
     * **********************
     */
    /* kludge for Mac Plus (and 128k, 512k, 512ke) : */
    public static void via_set_input_si(int which, int data) {
        via6522 v = via[which];

        via_set_int(v, INT_SR);
        v.sr = data;
    }

    /**
     * ***************** Standard 8-bit CPU interfaces, D0-D7
     * ******************
     */
    public static ReadHandlerPtr via_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via_read(0, offset);
        }
    };
    public static ReadHandlerPtr via_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via_read(1, offset);
        }
    };
    public static ReadHandlerPtr via_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via_read(2, offset);
        }
    };
    public static ReadHandlerPtr via_3_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via_read(3, offset);
        }
    };
    public static ReadHandlerPtr via_4_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via_read(4, offset);
        }
    };
    public static ReadHandlerPtr via_5_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via_read(5, offset);
        }
    };
    public static ReadHandlerPtr via_6_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via_read(6, offset);
        }
    };
    public static ReadHandlerPtr via_7_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via_read(7, offset);
        }
    };

    public static WriteHandlerPtr via_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_write(0, offset, data);
        }
    };
    public static WriteHandlerPtr via_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_write(1, offset, data);
        }
    };
    public static WriteHandlerPtr via_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_write(2, offset, data);
        }
    };
    public static WriteHandlerPtr via_3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_write(3, offset, data);
        }
    };
    public static WriteHandlerPtr via_4_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_write(4, offset, data);
        }
    };
    public static WriteHandlerPtr via_5_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_write(5, offset, data);
        }
    };
    public static WriteHandlerPtr via_6_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_write(6, offset, data);
        }
    };
    public static WriteHandlerPtr via_7_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_write(7, offset, data);
        }
    };

    /**
     * ***************** 8-bit A/B port interfaces ******************
     */
    public static WriteHandlerPtr via_0_porta_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_a(0, data);
        }
    };
    public static WriteHandlerPtr via_1_porta_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_a(1, data);
        }
    };
    public static WriteHandlerPtr via_2_porta_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_a(2, data);
        }
    };
    public static WriteHandlerPtr via_3_porta_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_a(3, data);
        }
    };
    public static WriteHandlerPtr via_4_porta_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_a(4, data);
        }
    };
    public static WriteHandlerPtr via_5_porta_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_a(5, data);
        }
    };
    public static WriteHandlerPtr via_6_porta_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_a(6, data);
        }
    };
    public static WriteHandlerPtr via_7_porta_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_a(7, data);
        }
    };

    public static WriteHandlerPtr via_0_portb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_b(0, data);
        }
    };
    public static WriteHandlerPtr via_1_portb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_b(1, data);
        }
    };
    public static WriteHandlerPtr via_2_portb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_b(2, data);
        }
    };
    public static WriteHandlerPtr via_3_portb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_b(3, data);
        }
    };
    public static WriteHandlerPtr via_4_portb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_b(4, data);
        }
    };
    public static WriteHandlerPtr via_5_portb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_b(5, data);
        }
    };
    public static WriteHandlerPtr via_6_portb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_b(6, data);
        }
    };
    public static WriteHandlerPtr via_7_portb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_b(7, data);
        }
    };

    public static ReadHandlerPtr via_0_porta_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[0].u8_in_a & 0xFF;
        }
    };
    public static ReadHandlerPtr via_1_porta_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[1].u8_in_a & 0xFF;
        }
    };
    public static ReadHandlerPtr via_2_porta_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[2].u8_in_a & 0xFF;
        }
    };
    public static ReadHandlerPtr via_3_porta_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[3].u8_in_a & 0xFF;
        }
    };
    public static ReadHandlerPtr via_4_porta_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[4].u8_in_a & 0xFF;
        }
    };
    public static ReadHandlerPtr via_5_porta_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[5].u8_in_a & 0xFF;
        }
    };
    public static ReadHandlerPtr via_6_porta_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[6].u8_in_a & 0xFF;
        }
    };
    public static ReadHandlerPtr via_7_porta_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[7].u8_in_a & 0xFF;
        }
    };

    public static ReadHandlerPtr via_0_portb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[0].u8_in_b & 0xFF;
        }
    };
    public static ReadHandlerPtr via_1_portb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[1].u8_in_b & 0xFF;
        }
    };
    public static ReadHandlerPtr via_2_portb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[2].u8_in_b & 0xFF;
        }
    };
    public static ReadHandlerPtr via_3_portb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[3].u8_in_b & 0xFF;
        }
    };
    public static ReadHandlerPtr via_4_portb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[4].u8_in_b & 0xFF;
        }
    };
    public static ReadHandlerPtr via_5_portb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[5].u8_in_b & 0xFF;
        }
    };
    public static ReadHandlerPtr via_6_portb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[6].u8_in_b & 0xFF;
        }
    };
    public static ReadHandlerPtr via_7_portb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[7].u8_in_b & 0xFF;
        }
    };

    /**
     * ***************** 1-bit CA1/CA2/CB1/CB2 port interfaces
     * ******************
     */
    public static WriteHandlerPtr via_0_ca1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_ca1(0, data);
        }
    };
    public static WriteHandlerPtr via_1_ca1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_ca1(1, data);
        }
    };
    public static WriteHandlerPtr via_2_ca1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_ca1(2, data);
        }
    };
    public static WriteHandlerPtr via_3_ca1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_ca1(3, data);
        }
    };
    public static WriteHandlerPtr via_4_ca1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_ca1(4, data);
        }
    };
    public static WriteHandlerPtr via_5_ca1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_ca1(5, data);
        }
    };
    public static WriteHandlerPtr via_6_ca1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_ca1(6, data);
        }
    };
    public static WriteHandlerPtr via_7_ca1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_ca1(7, data);
        }
    };
    public static WriteHandlerPtr via_0_ca2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_ca2(0, data);
        }
    };
    public static WriteHandlerPtr via_1_ca2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_ca2(1, data);
        }
    };
    public static WriteHandlerPtr via_2_ca2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_ca2(2, data);
        }
    };
    public static WriteHandlerPtr via_3_ca2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_ca2(3, data);
        }
    };
    public static WriteHandlerPtr via_4_ca2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_ca2(4, data);
        }
    };
    public static WriteHandlerPtr via_5_ca2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_ca2(5, data);
        }
    };
    public static WriteHandlerPtr via_6_ca2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_ca2(6, data);
        }
    };
    public static WriteHandlerPtr via_7_ca2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_ca2(7, data);
        }
    };

    public static WriteHandlerPtr via_0_cb1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_cb1(0, data);
        }
    };
    public static WriteHandlerPtr via_1_cb1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_cb1(1, data);
        }
    };
    public static WriteHandlerPtr via_2_cb1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_cb1(2, data);
        }
    };
    public static WriteHandlerPtr via_3_cb1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_cb1(3, data);
        }
    };
    public static WriteHandlerPtr via_4_cb1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_cb1(4, data);
        }
    };
    public static WriteHandlerPtr via_5_cb1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_cb1(5, data);
        }
    };
    public static WriteHandlerPtr via_6_cb1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_cb1(6, data);
        }
    };
    public static WriteHandlerPtr via_7_cb1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_cb1(7, data);
        }
    };
    public static WriteHandlerPtr via_0_cb2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_cb2(0, data);
        }
    };
    public static WriteHandlerPtr via_1_cb2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_cb2(1, data);
        }
    };
    public static WriteHandlerPtr via_2_cb2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_cb2(2, data);
        }
    };
    public static WriteHandlerPtr via_3_cb2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_cb2(3, data);
        }
    };
    public static WriteHandlerPtr via_4_cb2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_cb2(4, data);
        }
    };
    public static WriteHandlerPtr via_5_cb2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_cb2(5, data);
        }
    };
    public static WriteHandlerPtr via_6_cb2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_cb2(6, data);
        }
    };
    public static WriteHandlerPtr via_7_cb2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            via_set_input_cb2(7, data);
        }
    };

    public static ReadHandlerPtr via_0_ca1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[0].u8_in_ca1 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_1_ca1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[1].u8_in_ca1 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_2_ca1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[2].u8_in_ca1 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_3_ca1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[3].u8_in_ca1 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_4_ca1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[4].u8_in_ca1 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_5_ca1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[5].u8_in_ca1 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_6_ca1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[6].u8_in_ca1 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_7_ca1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[7].u8_in_ca1 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_0_ca2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[0].u8_in_ca2 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_1_ca2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[1].u8_in_ca2 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_2_ca2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[2].u8_in_ca2 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_3_ca2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[3].u8_in_ca2 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_4_ca2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[4].u8_in_ca2 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_5_ca2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[5].u8_in_ca2 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_6_ca2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[6].u8_in_ca2 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_7_ca2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[7].u8_in_ca2 & 0xFF;
        }
    };

    public static ReadHandlerPtr via_0_cb1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[0].u8_in_cb1 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_1_cb1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[1].u8_in_cb1 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_2_cb1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[2].u8_in_cb1 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_3_cb1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[3].u8_in_cb1 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_4_cb1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[4].u8_in_cb1 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_5_cb1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[5].u8_in_cb1 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_6_cb1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[6].u8_in_cb1 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_7_cb1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[7].u8_in_cb1 & 0xFF;
        }
    };
    public static ReadHandlerPtr via_0_cb2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[0].u8_in_cb2&0xFF;
        }
    };
    public static ReadHandlerPtr via_1_cb2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[1].u8_in_cb2&0xFF;
        }
    };
    public static ReadHandlerPtr via_2_cb2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[2].u8_in_cb2&0xFF;
        }
    };
    public static ReadHandlerPtr via_3_cb2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[3].u8_in_cb2&0xFF;
        }
    };
    public static ReadHandlerPtr via_4_cb2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[4].u8_in_cb2&0xFF;
        }
    };
    public static ReadHandlerPtr via_5_cb2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[5].u8_in_cb2&0xFF;
        }
    };
    public static ReadHandlerPtr via_6_cb2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[6].u8_in_cb2&0xFF;
        }
    };
    public static ReadHandlerPtr via_7_cb2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return via[7].u8_in_cb2&0xFF;
        }
    };
}
