/*
 * ported to v0.37b16
 * ported to v0.37b7
 */
package gr.codebb.arcadeflex.WIP.v037b16.machine;

//generic imports 
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//common imports
import static common.libc.cstdio.*;
//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
//machine imports
import static gr.codebb.arcadeflex.WIP.v037b16.machine._6821piaH.*;

public class _6821pia {

    public static FILE pia6821log = null;//fopen("pia6821.log", "wa");  //for debug purposes

    /**
     * ***************** internal PIA data structure ******************
     */
    public static class pia6821 {

        pia6821_interface intf;
        int /*UINT8*/ u8_addr;
        int /*UINT8*/ u8_in_a;
        int /*UINT8*/ u8_in_ca1;
        int /*UINT8*/ u8_in_ca2;
        int /*UINT8*/ u8_out_a;
        int /*UINT8*/ u8_out_ca2;
        int /*UINT8*/ u8_ddr_a;
        int /*UINT8*/ u8_ctl_a;
        int /*UINT8*/ u8_irq_a1;
        int /*UINT8*/ u8_irq_a2;
        int /*UINT8*/ u8_irq_a_state;
        int /*UINT8*/ u8_in_b;
        int /*UINT8*/ u8_in_cb1;
        int /*UINT8*/ u8_in_cb2;
        int /*UINT8*/ u8_out_b;
        int /*UINT8*/ u8_out_cb2;
        int /*UINT8*/ u8_ddr_b;
        int /*UINT8*/ u8_ctl_b;
        int /*UINT8*/ u8_irq_b1;
        int /*UINT8*/ u8_irq_b2;
        int /*UINT8*/ u8_irq_b_state;

        public static pia6821[] create(int n) {
            pia6821[] a = new pia6821[n];
            for (int k = 0; k < n; k++) {
                a[k] = new pia6821();
            }
            return a;
        }
    }

    /**
     * ***************** convenince macros and defines ******************
     */
    public static final int PIA_IRQ1 = 0x80;
    public static final int PIA_IRQ2 = 0x40;

    static boolean IRQ1_ENABLED(int c) {
        return (c & 0x01) != 0;
    }

    static boolean IRQ1_DISABLED(int c) {
        return (c & 0x01) == 0;
    }

    static boolean C1_LOW_TO_HIGH(int c) {
        return (c & 0x02) != 0;
    }

    static boolean C1_HIGH_TO_LOW(int c) {
        return (c & 0x02) == 0;
    }

    static boolean OUTPUT_SELECTED(int c) {
        return (c & 0x04) != 0;
    }

    static boolean DDR_SELECTED(int c) {
        return ((c & 0x04)) == 0;
    }

    static boolean IRQ2_ENABLED(int c) {
        return (c & 0x08) != 0;
    }

    static boolean IRQ2_DISABLED(int c) {
        return (c & 0x08) == 0;
    }

    static boolean STROBE_E_RESET(int c) {
        return (c & 0x08) != 0;
    }

    static boolean STROBE_C1_RESET(int c) {
        return (c & 0x08) == 0;
    }

    static boolean SET_C2(int c) {
        return (c & 0x08) != 0;
    }

    static boolean RESET_C2(int c) {
        return ((c & 0x08)) == 0;
    }

    static boolean C2_LOW_TO_HIGH(int c) {
        return (c & 0x10) != 0;
    }

    static boolean C2_HIGH_TO_LOW(int c) {
        return (c & 0x10) == 0;
    }

    static boolean C2_SET_MODE(int c) {
        return (c & 0x10) != 0;
    }

    static boolean C2_STROBE_MODE(int c) {
        return ((c & 0x10) == 0);
    }

    static boolean C2_OUTPUT(int c) {
        return (c & 0x20) != 0;
    }

    static boolean C2_INPUT(int c) {
        return ((c & 0x20) == 0);
    }

    /**
     * ***************** static variables ******************
     */
    static pia6821[] pia = pia6821.create(MAX_PIA);

    static int swizzle_address[] = {0, 2, 1, 3};

    /**
     * ***************** stave state ******************
     */
    /*
    static void pia_postload(int which)
    {
            struct pia6821 *p = pia + which;
            update_6821_interrupts(p);
            if (p->intf->out_a_func && p->ddr_a) p->intf->out_a_func(0, p->out_a & p->ddr_a);
            if (p->intf->out_b_func && p->ddr_b) p->intf->out_b_func(0, p->out_b & p->ddr_b);
            if (p->intf->out_ca2_func) p->intf->out_ca2_func(0, p->out_ca2);
            if (p->intf->out_cb2_func) p->intf->out_cb2_func(0, p->out_cb2);
    }

    static void pia_postload_0(void)
    {
            pia_postload(0);
    }

    static void pia_postload_1(void)
    {
            pia_postload(1);
    }

    static void pia_postload_2(void)
    {
            pia_postload(2);
    }

    static void pia_postload_3(void)
    {
            pia_postload(3);
    }

    static void pia_postload_4(void)
    {
            pia_postload(4);
    }

    static void pia_postload_5(void)
    {
            pia_postload(5);
    }

    static void pia_postload_6(void)
    {
            pia_postload(6);
    }

    static void pia_postload_7(void)
    {
            pia_postload(7);
    }

    static void (*pia_postload_funcs[MAX_PIA])(void) =
    {
            pia_postload_0,
            pia_postload_1,
            pia_postload_2,
            pia_postload_3,
            pia_postload_4,
            pia_postload_5,
            pia_postload_6,
            pia_postload_7
    };
     */
    /**
     * ***************** un-configuration ******************
     */
    public static void pia_unconfig() {
        for (int i = 0; i < MAX_PIA; i++) {
            pia[i] = new pia6821();
        }
    }

    /**
     * ***************** configuration ******************
     */
    public static void pia_config(int which, int addressing, pia6821_interface intf) {
        if (which >= MAX_PIA) {
            return;
        }
        pia[which].intf = intf;
        pia[which].u8_addr = addressing & 0xFF;
        /*
	state_save_register_UINT8("6821pia", which, "in_a",		&pia[which].in_a, 1);
	state_save_register_UINT8("6821pia", which, "in_ca1",	&pia[which].in_ca1, 1);
	state_save_register_UINT8("6821pia", which, "in_ca2",	&pia[which].in_ca2, 1);
	state_save_register_UINT8("6821pia", which, "out_a",	&pia[which].out_a, 1);
	state_save_register_UINT8("6821pia", which, "out_ca2",	&pia[which].out_ca2, 1);
	state_save_register_UINT8("6821pia", which, "ddr_a",	&pia[which].ddr_a, 1);
	state_save_register_UINT8("6821pia", which, "ctl_a",	&pia[which].ctl_a, 1);
	state_save_register_UINT8("6821pia", which, "irq_a1",	&pia[which].irq_a1, 1);
	state_save_register_UINT8("6821pia", which, "irq_a2",	&pia[which].irq_a2, 1);
	state_save_register_UINT8("6821pia", which, "in_b",		&pia[which].in_b, 1);
	state_save_register_UINT8("6821pia", which, "in_cb1",	&pia[which].in_cb1, 1);
	state_save_register_UINT8("6821pia", which, "in_cb2",	&pia[which].in_cb2, 1);
	state_save_register_UINT8("6821pia", which, "out_b",	&pia[which].out_b, 1);
	state_save_register_UINT8("6821pia", which, "out_cb2",	&pia[which].out_cb2, 1);
	state_save_register_UINT8("6821pia", which, "ddr_b",	&pia[which].ddr_b, 1);
	state_save_register_UINT8("6821pia", which, "ctl_b",	&pia[which].ctl_b, 1);
	state_save_register_UINT8("6821pia", which, "irq_b1",	&pia[which].irq_b1, 1);
	state_save_register_UINT8("6821pia", which, "irq_b2",	&pia[which].irq_b2, 1);
	state_save_register_func_postload(pia_postload_funcs[which]);*/
    }

    /**
     * ***************** reset ******************
     */
    public static void pia_reset() {
        int i;

        /* zap each structure, preserving the interface and swizzle */
        for (i = 0; i < MAX_PIA; i++) {
            pia6821_interface intf = pia[i].intf;
            int addr = pia[i].u8_addr & 0xFF;

            pia[i] = new pia6821();

            pia[i].intf = intf;
            pia[i].u8_addr = addr & 0xFF;
        }
    }

    /**
     * ***************** wire-OR for all interrupt handlers ******************
     */
    static void update_shared_irq_handler(IrqfuncPtr irq_func) {
        int i;

        /* search all PIAs for this same IRQ function */
        for (i = 0; i < MAX_PIA; i++) {
            if (pia[i].intf != null) {
                /* check IRQ A */
                if (pia[i].intf.irq_a_func == irq_func && pia[i].u8_irq_a_state != 0) {
                    irq_func.handler(1);
                    return;
                }

                /* check IRQ B */
                if (pia[i].intf.irq_b_func == irq_func && pia[i].u8_irq_b_state != 0) {
                    irq_func.handler(1);
                    return;
                }
            }
        }

        /* if we found nothing, the state is off */
        irq_func.handler(0);
    }

    /**
     * ***************** external interrupt check ******************
     */
    public static void update_6821_interrupts(pia6821 p) {
        int new_state;

        /* start with IRQ A */
        new_state = 0;
        if ((p.u8_irq_a1 != 0 && IRQ1_ENABLED(p.u8_ctl_a)) || (p.u8_irq_a2 != 0 && IRQ2_ENABLED(p.u8_ctl_a))) {
            new_state = 1;
        }
        if (new_state != p.u8_irq_a_state) {
            p.u8_irq_a_state = new_state & 0xFF;
            if (p.intf.irq_a_func != null) {
                update_shared_irq_handler(p.intf.irq_a_func);
            }
        }

        /* then do IRQ B */
        new_state = 0;
        if ((p.u8_irq_b1 != 0 && IRQ1_ENABLED(p.u8_ctl_b)) || (p.u8_irq_b2 != 0 && IRQ2_ENABLED(p.u8_ctl_b))) {
            new_state = 1;
        }
        if (new_state != p.u8_irq_b_state) {
            p.u8_irq_b_state = new_state & 0xFF;
            if (p.intf.irq_b_func != null) {
                update_shared_irq_handler(p.intf.irq_b_func);
            }
        }
    }

    /**
     * ***************** CPU interface for PIA read ******************
     */
    static int pia_read(int which, int offset) {
        int val = 0;

        /* adjust offset for 16-bit and ordering */
        offset &= 3;
        if ((pia[which].u8_addr & PIA_ALTERNATE_ORDERING) != 0) {
            offset = swizzle_address[offset];
        }

        switch (offset) {
            /**
             * ***************** port A output/DDR read ******************
             */
            case PIA_DDRA:

                /* read output register */
                if (OUTPUT_SELECTED(pia[which].u8_ctl_a)) {
                    /* update the input */
                    if (pia[which].intf.in_a_func != null) {
                        pia[which].u8_in_a = pia[which].intf.in_a_func.handler(0) & 0xFF;
                    }

                    /* combine input and output values */
                    val = (pia[which].u8_out_a & pia[which].u8_ddr_a) + (pia[which].u8_in_a & ~pia[which].u8_ddr_a);

                    /* IRQ flags implicitly cleared by a read */
                    pia[which].u8_irq_a1 = pia[which].u8_irq_a2 = 0;
                    update_6821_interrupts(pia[which]);

                    /* CA2 is configured as output and in read strobe mode */
                    if (C2_OUTPUT(pia[which].u8_ctl_a) && C2_STROBE_MODE(pia[which].u8_ctl_a)) {
                        /* this will cause a transition low; call the output function if we're currently high */
                        if (pia[which].u8_out_ca2 != 0) {
                            if (pia[which].intf.out_ca2_func != null) {
                                pia[which].intf.out_ca2_func.handler(0, 0);
                            }
                        }

                        pia[which].u8_out_ca2 = 0;

                        /* if the CA2 strobe is cleared by the E, reset it right away */
                        if (STROBE_E_RESET(pia[which].u8_ctl_a)) {
                            if (pia[which].intf.out_ca2_func != null) {
                                pia[which].intf.out_ca2_func.handler(0, 1);
                            }
                            pia[which].u8_out_ca2 = 1;
                        }
                    }
                    if (pia6821log != null) {
                        fprintf(pia6821log, "%04x: PIA%d read port A = %02X\n", cpu_getpreviouspc(), which, val);
                    }
                } /* read DDR register */ else {
                    val = pia[which].u8_ddr_a;
                    if (pia6821log != null) {
                        fprintf(pia6821log, "%04x: PIA%d read DDR A = %02X\n", cpu_getpreviouspc(), which, val);
                    }
                }
                break;

            /**
             * ***************** port B output/DDR read ******************
             */
            case PIA_DDRB:

                /* read output register */
                if (OUTPUT_SELECTED(pia[which].u8_ctl_b)) {
                    /* update the input */
                    if (pia[which].intf.in_b_func != null) {
                        pia[which].u8_in_b = pia[which].intf.in_b_func.handler(0) & 0xFF;
                    }

                    /* combine input and output values */
                    val = (pia[which].u8_out_b & pia[which].u8_ddr_b) + (pia[which].u8_in_b & ~pia[which].u8_ddr_b);

                    /* IRQ flags implicitly cleared by a read */
                    pia[which].u8_irq_b1 = pia[which].u8_irq_b2 = 0;
                    update_6821_interrupts(pia[which]);
                    if (pia6821log != null) {
                        fprintf(pia6821log, "%04x: PIA%d read port B = %02X\n", cpu_getpreviouspc(), which, val);
                    }
                } /* read DDR register */ else {
                    val = pia[which].u8_ddr_b;
                    if (pia6821log != null) {
                        fprintf(pia6821log, "%04x: PIA%d read DDR B = %02X\n", cpu_getpreviouspc(), which, val);
                    }
                }
                break;

            /**
             * ***************** port A control read ******************
             */
            case PIA_CTLA:

                /* Update CA1 & CA2 if callback exists, these in turn may update IRQ's */
                if (pia[which].intf.in_ca1_func != null) {
                    pia_set_input_ca1(which, pia[which].intf.in_ca1_func.handler(0));
                }
                if (pia[which].intf.in_ca2_func != null) {
                    pia_set_input_ca2(which, pia[which].intf.in_ca2_func.handler(0));
                }

                /* read control register */
                val = pia[which].u8_ctl_a;

                /* set the IRQ flags if we have pending IRQs */
                if (pia[which].u8_irq_a1 != 0) {
                    val |= PIA_IRQ1;
                }
                if (pia[which].u8_irq_a2 != 0 && C2_INPUT(pia[which].u8_ctl_a)) {
                    val |= PIA_IRQ2;
                }

                if (pia6821log != null) {
                    fprintf(pia6821log, "%04x: PIA%d read control A = %02X\n", cpu_getpreviouspc(), which, val);
                }
                break;

            /**
             * ***************** port B control read ******************
             */
            case PIA_CTLB:

                /* Update CB1 & CB2 if callback exists, these in turn may update IRQ's */
                if (pia[which].intf.in_cb1_func != null) {
                    pia_set_input_cb1(which, pia[which].intf.in_cb1_func.handler(0));
                }
                if (pia[which].intf.in_cb2_func != null) {
                    pia_set_input_cb2(which, pia[which].intf.in_cb2_func.handler(0));
                }

                /* read control register */
                val = pia[which].u8_ctl_b;

                /* set the IRQ flags if we have pending IRQs */
                if (pia[which].u8_irq_b1 != 0) {
                    val |= PIA_IRQ1;
                }
                if (pia[which].u8_irq_b2 != 0 && C2_INPUT(pia[which].u8_ctl_b)) {
                    val |= PIA_IRQ2;
                }

                if (pia6821log != null) {
                    fprintf(pia6821log, "%04x: PIA%d read control B = %02X\n", cpu_getpreviouspc(), which, val);
                }
                break;
        }

        return val;
    }

    /**
     * ***************** CPU interface for PIA write ******************
     */
    static void pia_write(int which, int offset, int data) {

        /* adjust offset for 16-bit and ordering */
        offset &= 3;
        if ((pia[which].u8_addr & PIA_ALTERNATE_ORDERING) != 0) {
            offset = swizzle_address[offset];
        }

        switch (offset) {
            /**
             * ***************** port A output/DDR write ******************
             */
            case PIA_DDRA:

                /* write output register */
                if (OUTPUT_SELECTED(pia[which].u8_ctl_a)) {
                    if (pia6821log != null) {
                        fprintf(pia6821log, "%04x: PIA%d port A write = %02X\n", cpu_getpreviouspc(), which, data);
                    }

                    /* update the output value */
                    pia[which].u8_out_a = data & 0xFF;/* & pia[which].ddr_a; */ /* NS990130 - don't mask now, DDR could change later */

 /* send it to the output function */
                    if (pia[which].intf.out_a_func != null && pia[which].u8_ddr_a != 0) {
                        pia[which].intf.out_a_func.handler(0, pia[which].u8_out_a & pia[which].u8_ddr_a);
                    }/* NS990130 */
                } /* write DDR register */ else {
                    if (pia6821log != null) {
                        fprintf(pia6821log, "%04x: PIA%d DDR A write = %02X\n", cpu_getpreviouspc(), which, data);
                    }

                    if (pia[which].u8_ddr_a != data) {
                        /* NS990130 - if DDR changed, call the callback again */
                        pia[which].u8_ddr_a = data & 0xFF;

                        /* send it to the output function */
                        if (pia[which].intf.out_a_func != null && pia[which].u8_ddr_a != 0) {
                            pia[which].intf.out_a_func.handler(0, pia[which].u8_out_a & pia[which].u8_ddr_a);
                        }
                    }
                }
                break;

            /**
             * ***************** port B output/DDR write ******************
             */
            case PIA_DDRB:

                /* write output register */
                if (OUTPUT_SELECTED(pia[which].u8_ctl_b)) {
                    if (pia6821log != null) {
                        fprintf(pia6821log, "%04x: PIA%d port B write = %02X\n", cpu_getpreviouspc(), which, data);
                    }

                    /* update the output value */
                    pia[which].u8_out_b = data & 0xFF;/* & pia[which].ddr_b */ /* NS990130 - don't mask now, DDR could change later */

 /* send it to the output function */
                    if (pia[which].intf.out_b_func != null && pia[which].u8_ddr_b != 0) {
                        pia[which].intf.out_b_func.handler(0, pia[which].u8_out_b & pia[which].u8_ddr_b);
                    }
                    /* NS990130 */

 /* CB2 is configured as output and in write strobe mode */
                    if (C2_OUTPUT(pia[which].u8_ctl_b) && C2_STROBE_MODE(pia[which].u8_ctl_b)) {
                        /* this will cause a transition low; call the output function if we're currently high */
                        if (pia[which].u8_out_cb2 != 0) {
                            if (pia[which].intf.out_cb2_func != null) {
                                pia[which].intf.out_cb2_func.handler(0, 0);
                            }
                        }
                        pia[which].u8_out_cb2 = 0;

                        /* if the CB2 strobe is cleared by the E, reset it right away */
                        if (STROBE_E_RESET(pia[which].u8_ctl_b)) {
                            if (pia[which].intf.out_cb2_func != null) {
                                pia[which].intf.out_cb2_func.handler(0, 1);
                            }
                            pia[which].u8_out_cb2 = 1;
                        }
                    }
                } /* write DDR register */ else {
                    if (pia6821log != null) {
                        fprintf(pia6821log, "%04x: PIA%d DDR B write = %02X\n", cpu_getpreviouspc(), which, data);
                    }

                    if (pia[which].u8_ddr_b != data) {
                        /* NS990130 - if DDR changed, call the callback again */
                        pia[which].u8_ddr_b = data & 0xFF;

                        /* send it to the output function */
                        if (pia[which].intf.out_b_func != null && pia[which].u8_ddr_b != 0) {
                            pia[which].intf.out_b_func.handler(0, pia[which].u8_out_b & pia[which].u8_ddr_b);
                        }
                    }
                }
                break;

            /**
             * ***************** port A control write ******************
             */
            case PIA_CTLA:

                /* Bit 7 and 6 read only - PD 16/01/00 */
                data &= 0x3f;

                if (pia6821log != null) {
                    fprintf(pia6821log, "%04x: PIA%d control A write = %02X\n", cpu_getpreviouspc(), which, data);
                }

                /* CA2 is configured as output and in set/reset mode */
 /* 10/22/98 - MAB/FMP - any C2_OUTPUT should affect CA2 */
                //			if (C2_OUTPUT(data) && C2_SET_MODE(data))
                if (C2_OUTPUT(data)) {
                    /* determine the new value */
                    int temp = SET_C2(data) ? 1 : 0;

                    /* if this creates a transition, call the CA2 output function */
                    if ((pia[which].u8_out_ca2 ^ temp) != 0) {
                        if (pia[which].intf.out_ca2_func != null) {
                            pia[which].intf.out_ca2_func.handler(0, temp);
                        }
                    }

                    /* set the new value */
                    pia[which].u8_out_ca2 = temp & 0xFF;
                }

                /* update the control register */
                pia[which].u8_ctl_a = data & 0xFF;

                /* update externals */
                update_6821_interrupts(pia[which]);
                break;

            /**
             * ***************** port B control write ******************
             */
            case PIA_CTLB:

                /* Bit 7 and 6 read only - PD 16/01/00 */
                data &= 0x3f;

                if (pia6821log != null) {
                    fprintf(pia6821log, "%04x: PIA%d control B write = %02X\n", cpu_getpreviouspc(), which, data);
                }

                /* CB2 is configured as output and in set/reset mode */
 /* 10/22/98 - MAB/FMP - any C2_OUTPUT should affect CB2 */
                //			if (C2_OUTPUT(data) && C2_SET_MODE(data))
                if (C2_OUTPUT(data)) {
                    /* determine the new value */
                    int temp = SET_C2(data) ? 1 : 0;

                    /* if this creates a transition, call the CA2 output function */
                    if ((pia[which].u8_out_cb2 ^ temp) != 0) {
                        if (pia[which].intf.out_cb2_func != null) {
                            pia[which].intf.out_cb2_func.handler(0, temp);
                        }
                    }

                    /* set the new value */
                    pia[which].u8_out_cb2 = temp & 0xFF;
                }

                /* update the control register */
                pia[which].u8_ctl_b = data & 0xFF;

                /* update externals */
                update_6821_interrupts(pia[which]);
                break;
        }
    }

    /**
     * ***************** interface setting PIA port A input ******************
     */
    static void pia_set_input_a(int which, int data) {
        /* set the input, what could be easier? */
        pia[which].u8_in_a = data & 0xFF;
    }

    /**
     * ***************** interface setting PIA port CA1 input
     * ******************
     */
    static void pia_set_input_ca1(int which, int data) {
        /* limit the data to 0 or 1 */
        data = data != 0 ? 1 : 0;

        /* the new state has caused a transition */
        if ((pia[which].u8_in_ca1 ^ data) != 0) {
            /* handle the active transition */
            if ((data != 0 && C1_LOW_TO_HIGH(pia[which].u8_ctl_a)) || (data == 0 && C1_HIGH_TO_LOW(pia[which].u8_ctl_a))) {
                /* mark the IRQ */
                pia[which].u8_irq_a1 = 1;

                /* update externals */
                update_6821_interrupts(pia[which]);

                /* CA2 is configured as output and in read strobe mode and cleared by a CA1 transition */
                if (C2_OUTPUT(pia[which].u8_ctl_a) && C2_STROBE_MODE(pia[which].u8_ctl_a) && STROBE_C1_RESET(pia[which].u8_ctl_a)) {
                    /* call the CA2 output function */
                    if (pia[which].u8_out_ca2 == 0) {
                        if (pia[which].intf.out_ca2_func != null) {
                            pia[which].intf.out_ca2_func.handler(0, 1);
                        }
                    }

                    /* clear CA2 */
                    pia[which].u8_out_ca2 = 1;
                }
            }
        }

        /* set the new value for CA1 */
        pia[which].u8_in_ca1 = data & 0xFF;
    }

    /**
     * ***************** interface setting PIA port CA2 input
     * ******************
     */
    static void pia_set_input_ca2(int which, int data) {
        /* limit the data to 0 or 1 */
        data = data != 0 ? 1 : 0;

        /* CA2 is in input mode */
        if (C2_INPUT(pia[which].u8_ctl_a)) {
            /* the new state has caused a transition */
            if ((pia[which].u8_in_ca2 ^ data) != 0) {
                /* handle the active transition */
                if ((data != 0 && C2_LOW_TO_HIGH(pia[which].u8_ctl_a)) || (data == 0 && C2_HIGH_TO_LOW(pia[which].u8_ctl_a))) {
                    /* mark the IRQ */
                    pia[which].u8_irq_a2 = 1;

                    /* update externals */
                    update_6821_interrupts(pia[which]);
                }
            }
        }

        /* set the new value for CA2 */
        pia[which].u8_in_ca2 = data & 0xFF;
    }

    /**
     * ***************** interface setting PIA port B input ******************
     */
    static void pia_set_input_b(int which, int data) {
        /* set the input, what could be easier? */
        pia[which].u8_in_b = data & 0xFF;
    }

    /**
     * ***************** interface setting PIA port CB1 input
     * ******************
     */
    static void pia_set_input_cb1(int which, int data) {
        /* limit the data to 0 or 1 */
        data = data != 0 ? 1 : 0;

        /* the new state has caused a transition */
        if ((pia[which].u8_in_cb1 ^ data) != 0) {
            /* handle the active transition */
            if ((data != 0 && C1_LOW_TO_HIGH(pia[which].u8_ctl_b)) || (data == 0 && C1_HIGH_TO_LOW(pia[which].u8_ctl_b))) {
                /* mark the IRQ */
                pia[which].u8_irq_b1 = 1;

                /* update externals */
                update_6821_interrupts(pia[which]);

                /* CB2 is configured as output and in write strobe mode and cleared by a CA1 transition */
                if (C2_OUTPUT(pia[which].u8_ctl_b) && C2_STROBE_MODE(pia[which].u8_ctl_b) && STROBE_C1_RESET(pia[which].u8_ctl_b)) {
                    /* the IRQ1 flag must have also been cleared */
                    if (pia[which].u8_irq_b1 == 0) {
                        /* call the CB2 output function */
                        if (pia[which].u8_out_cb2 == 0) {
                            if (pia[which].intf.out_cb2_func != null) {
                                pia[which].intf.out_cb2_func.handler(0, 1);
                            }
                        }

                        /* clear CB2 */
                        pia[which].u8_out_cb2 = 1;
                    }
                }
            }
        }

        /* set the new value for CB1 */
        pia[which].u8_in_cb1 = data & 0xFF;
    }

    /**
     * ***************** interface setting PIA port CB2 input
     * ******************
     */
    static void pia_set_input_cb2(int which, int data) {
        /* limit the data to 0 or 1 */
        data = data != 0 ? 1 : 0;

        /* CB2 is in input mode */
        if (C2_INPUT(pia[which].u8_ctl_b)) {
            /* the new state has caused a transition */
            if ((pia[which].u8_in_cb2 ^ data) != 0) {
                /* handle the active transition */
                if ((data != 0 && C2_LOW_TO_HIGH(pia[which].u8_ctl_b)) || (data == 0 && C2_HIGH_TO_LOW(pia[which].u8_ctl_b))) {
                    /* mark the IRQ */
                    pia[which].u8_irq_b2 = 1;

                    /* update externals */
                    update_6821_interrupts(pia[which]);
                }
            }
        }

        /* set the new value for CA2 */
        pia[which].u8_in_cb2 = data & 0xFF;
    }

    /**
     * ***************** Standard 8-bit CPU interfaces, D0-D7
     * ******************
     */
    public static ReadHandlerPtr pia_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia_read(0, offset);
        }
    };
    public static ReadHandlerPtr pia_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia_read(1, offset);
        }
    };
    public static ReadHandlerPtr pia_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia_read(2, offset);
        }
    };
    public static ReadHandlerPtr pia_3_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia_read(3, offset);
        }
    };
    public static ReadHandlerPtr pia_4_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia_read(4, offset);
        }
    };
    public static ReadHandlerPtr pia_5_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia_read(5, offset);
        }
    };
    public static ReadHandlerPtr pia_6_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia_read(6, offset);
        }
    };
    public static ReadHandlerPtr pia_7_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia_read(7, offset);
        }
    };

    public static WriteHandlerPtr pia_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_write(0, offset, data);
        }
    };
    public static WriteHandlerPtr pia_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_write(1, offset, data);
        }
    };
    public static WriteHandlerPtr pia_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_write(2, offset, data);
        }
    };
    public static WriteHandlerPtr pia_3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_write(3, offset, data);
        }
    };
    public static WriteHandlerPtr pia_4_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_write(4, offset, data);
        }
    };
    public static WriteHandlerPtr pia_5_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_write(5, offset, data);
        }
    };
    public static WriteHandlerPtr pia_6_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_write(6, offset, data);
        }
    };
    public static WriteHandlerPtr pia_7_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_write(7, offset, data);
        }
    };

    /*TODO*////******************* Standard 16-bit CPU interfaces, D0-D7 *******************/
/*TODO*///
/*TODO*///READ16_HANDLER( pia_0_lsb_r ) { return pia_read(0, offset); }
/*TODO*///READ16_HANDLER( pia_1_lsb_r ) { return pia_read(1, offset); }
/*TODO*///READ16_HANDLER( pia_2_lsb_r ) { return pia_read(2, offset); }
/*TODO*///READ16_HANDLER( pia_3_lsb_r ) { return pia_read(3, offset); }
/*TODO*///READ16_HANDLER( pia_4_lsb_r ) { return pia_read(4, offset); }
/*TODO*///READ16_HANDLER( pia_5_lsb_r ) { return pia_read(5, offset); }
/*TODO*///READ16_HANDLER( pia_6_lsb_r ) { return pia_read(6, offset); }
/*TODO*///READ16_HANDLER( pia_7_lsb_r ) { return pia_read(7, offset); }
/*TODO*///
/*TODO*///WRITE16_HANDLER( pia_0_lsb_w ) { if (ACCESSING_LSB) pia_write(0, offset, data & 0xff); }
/*TODO*///WRITE16_HANDLER( pia_1_lsb_w ) { if (ACCESSING_LSB) pia_write(1, offset, data & 0xff); }
/*TODO*///WRITE16_HANDLER( pia_2_lsb_w ) { if (ACCESSING_LSB) pia_write(2, offset, data & 0xff); }
/*TODO*///WRITE16_HANDLER( pia_3_lsb_w ) { if (ACCESSING_LSB) pia_write(3, offset, data & 0xff); }
/*TODO*///WRITE16_HANDLER( pia_4_lsb_w ) { if (ACCESSING_LSB) pia_write(4, offset, data & 0xff); }
/*TODO*///WRITE16_HANDLER( pia_5_lsb_w ) { if (ACCESSING_LSB) pia_write(5, offset, data & 0xff); }
/*TODO*///WRITE16_HANDLER( pia_6_lsb_w ) { if (ACCESSING_LSB) pia_write(6, offset, data & 0xff); }
/*TODO*///WRITE16_HANDLER( pia_7_lsb_w ) { if (ACCESSING_LSB) pia_write(7, offset, data & 0xff); }
/*TODO*///
/*TODO*////******************* Standard 16-bit CPU interfaces, D8-D15 *******************/
/*TODO*///
/*TODO*///READ16_HANDLER( pia_0_msb_r ) { return pia_read(0, offset) << 8; }
/*TODO*///READ16_HANDLER( pia_1_msb_r ) { return pia_read(1, offset) << 8; }
/*TODO*///READ16_HANDLER( pia_2_msb_r ) { return pia_read(2, offset) << 8; }
/*TODO*///READ16_HANDLER( pia_3_msb_r ) { return pia_read(3, offset) << 8; }
/*TODO*///READ16_HANDLER( pia_4_msb_r ) { return pia_read(4, offset) << 8; }
/*TODO*///READ16_HANDLER( pia_5_msb_r ) { return pia_read(5, offset) << 8; }
/*TODO*///READ16_HANDLER( pia_6_msb_r ) { return pia_read(6, offset) << 8; }
/*TODO*///READ16_HANDLER( pia_7_msb_r ) { return pia_read(7, offset) << 8; }
/*TODO*///
/*TODO*///WRITE16_HANDLER( pia_0_msb_w ) { if (ACCESSING_MSB) pia_write(0, offset, data >> 8); }
/*TODO*///WRITE16_HANDLER( pia_1_msb_w ) { if (ACCESSING_MSB) pia_write(1, offset, data >> 8); }
/*TODO*///WRITE16_HANDLER( pia_2_msb_w ) { if (ACCESSING_MSB) pia_write(2, offset, data >> 8); }
/*TODO*///WRITE16_HANDLER( pia_3_msb_w ) { if (ACCESSING_MSB) pia_write(3, offset, data >> 8); }
/*TODO*///WRITE16_HANDLER( pia_4_msb_w ) { if (ACCESSING_MSB) pia_write(4, offset, data >> 8); }
/*TODO*///WRITE16_HANDLER( pia_5_msb_w ) { if (ACCESSING_MSB) pia_write(5, offset, data >> 8); }
/*TODO*///WRITE16_HANDLER( pia_6_msb_w ) { if (ACCESSING_MSB) pia_write(6, offset, data >> 8); }
/*TODO*///WRITE16_HANDLER( pia_7_msb_w ) { if (ACCESSING_MSB) pia_write(7, offset, data >> 8); }
/*TODO*///
    /**
     * ***************** 8-bit A/B port interfaces ******************
     */
    public static WriteHandlerPtr pia_0_porta_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_a(0, data);
        }
    };
    public static WriteHandlerPtr pia_1_porta_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_a(1, data);
        }
    };
    public static WriteHandlerPtr pia_2_porta_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_a(2, data);
        }
    };
    public static WriteHandlerPtr pia_3_porta_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_a(3, data);
        }
    };
    public static WriteHandlerPtr pia_4_porta_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_a(4, data);
        }
    };
    public static WriteHandlerPtr pia_5_porta_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_a(5, data);
        }
    };
    public static WriteHandlerPtr pia_6_porta_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_a(6, data);
        }
    };
    public static WriteHandlerPtr pia_7_porta_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_a(7, data);
        }
    };

    public static WriteHandlerPtr pia_0_portb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_b(0, data);
        }
    };
    public static WriteHandlerPtr pia_1_portb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_b(1, data);
        }
    };
    public static WriteHandlerPtr pia_2_portb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_b(2, data);
        }
    };
    public static WriteHandlerPtr pia_3_portb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_b(3, data);
        }
    };
    public static WriteHandlerPtr pia_4_portb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_b(4, data);
        }
    };
    public static WriteHandlerPtr pia_5_portb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_b(5, data);
        }
    };
    public static WriteHandlerPtr pia_6_portb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_b(6, data);
        }
    };
    public static WriteHandlerPtr pia_7_portb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_b(7, data);
        }
    };

    public static ReadHandlerPtr pia_0_porta_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[0].u8_in_a & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_1_porta_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[1].u8_in_a & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_2_porta_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[2].u8_in_a & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_3_porta_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[3].u8_in_a & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_4_porta_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[4].u8_in_a & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_5_porta_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[5].u8_in_a & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_6_porta_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[6].u8_in_a & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_7_porta_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[7].u8_in_a & 0xFF;
        }
    };

    public static ReadHandlerPtr pia_0_portb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[0].u8_in_b & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_1_portb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[1].u8_in_b & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_2_portb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[2].u8_in_b & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_3_portb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[3].u8_in_b & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_4_portb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[4].u8_in_b & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_5_portb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[5].u8_in_b & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_6_portb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[6].u8_in_b & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_7_portb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[7].u8_in_b & 0xFF;
        }
    };

    /**
     * ***************** 1-bit CA1/CA2/CB1/CB2 port interfaces
     * ******************
     */
    public static WriteHandlerPtr pia_0_ca1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_ca1(0, data);
        }
    };
    public static WriteHandlerPtr pia_1_ca1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_ca1(1, data);
        }
    };
    public static WriteHandlerPtr pia_2_ca1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_ca1(2, data);
        }
    };
    public static WriteHandlerPtr pia_3_ca1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_ca1(3, data);
        }
    };
    public static WriteHandlerPtr pia_4_ca1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_ca1(4, data);
        }
    };
    public static WriteHandlerPtr pia_5_ca1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_ca1(5, data);
        }
    };
    public static WriteHandlerPtr pia_6_ca1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_ca1(6, data);
        }
    };
    public static WriteHandlerPtr pia_7_ca1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_ca1(7, data);
        }
    };
    public static WriteHandlerPtr pia_0_ca2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_ca2(0, data);
        }
    };
    public static WriteHandlerPtr pia_1_ca2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_ca2(1, data);
        }
    };
    public static WriteHandlerPtr pia_2_ca2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_ca2(2, data);
        }
    };
    public static WriteHandlerPtr pia_3_ca2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_ca2(3, data);
        }
    };
    public static WriteHandlerPtr pia_4_ca2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_ca2(4, data);
        }
    };
    public static WriteHandlerPtr pia_5_ca2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_ca2(5, data);
        }
    };
    public static WriteHandlerPtr pia_6_ca2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_ca2(6, data);
        }
    };
    public static WriteHandlerPtr pia_7_ca2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_ca2(7, data);
        }
    };

    public static WriteHandlerPtr pia_0_cb1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_cb1(0, data);
        }
    };
    public static WriteHandlerPtr pia_1_cb1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_cb1(1, data);
        }
    };
    public static WriteHandlerPtr pia_2_cb1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_cb1(2, data);
        }
    };
    public static WriteHandlerPtr pia_3_cb1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_cb1(3, data);
        }
    };
    public static WriteHandlerPtr pia_4_cb1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_cb1(4, data);
        }
    };
    public static WriteHandlerPtr pia_5_cb1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_cb1(5, data);
        }
    };
    public static WriteHandlerPtr pia_6_cb1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_cb1(6, data);
        }
    };
    public static WriteHandlerPtr pia_7_cb1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_cb1(7, data);
        }
    };
    public static WriteHandlerPtr pia_0_cb2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_cb2(0, data);
        }
    };
    public static WriteHandlerPtr pia_1_cb2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_cb2(1, data);
        }
    };
    public static WriteHandlerPtr pia_2_cb2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_cb2(2, data);
        }
    };
    public static WriteHandlerPtr pia_3_cb2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_cb2(3, data);
        }
    };
    public static WriteHandlerPtr pia_4_cb2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_cb2(4, data);
        }
    };
    public static WriteHandlerPtr pia_5_cb2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_cb2(5, data);
        }
    };
    public static WriteHandlerPtr pia_6_cb2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_cb2(6, data);
        }
    };
    public static WriteHandlerPtr pia_7_cb2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            pia_set_input_cb2(7, data);
        }
    };

    public static ReadHandlerPtr pia_0_ca1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[0].u8_in_ca1 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_1_ca1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[1].u8_in_ca1 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_2_ca1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[2].u8_in_ca1 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_3_ca1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[3].u8_in_ca1 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_4_ca1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[4].u8_in_ca1 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_5_ca1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[5].u8_in_ca1 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_6_ca1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[6].u8_in_ca1 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_7_ca1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[7].u8_in_ca1 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_0_ca2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[0].u8_in_ca2 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_1_ca2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[1].u8_in_ca2 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_2_ca2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[2].u8_in_ca2 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_3_ca2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[3].u8_in_ca2 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_4_ca2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[4].u8_in_ca2 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_5_ca2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[5].u8_in_ca2 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_6_ca2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[6].u8_in_ca2 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_7_ca2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[7].u8_in_ca2 & 0xFF;
        }
    };

    public static ReadHandlerPtr pia_0_cb1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[0].u8_in_cb1 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_1_cb1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[1].u8_in_cb1 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_2_cb1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[2].u8_in_cb1 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_3_cb1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[3].u8_in_cb1 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_4_cb1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[4].u8_in_cb1 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_5_cb1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[5].u8_in_cb1 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_6_cb1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[6].u8_in_cb1 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_7_cb1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[7].u8_in_cb1 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_0_cb2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[0].u8_in_cb2 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_1_cb2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[1].u8_in_cb2 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_2_cb2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[2].u8_in_cb2 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_3_cb2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[3].u8_in_cb2 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_4_cb2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[4].u8_in_cb2 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_5_cb2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[5].u8_in_cb2 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_6_cb2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[6].u8_in_cb2 & 0xFF;
        }
    };
    public static ReadHandlerPtr pia_7_cb2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return pia[7].u8_in_cb2 & 0xFF;
        }
    };
}
