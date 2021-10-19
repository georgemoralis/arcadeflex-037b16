/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.WIP.v037b16.drivers;

//generic functions
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b16.mame.inptport.*;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.itech8.*;
import static gr.codebb.arcadeflex.v037b16.cpu.m6809.m6809H.M6809_FIRQ_LINE;
import static gr.codebb.arcadeflex.v037b16.cpu.m6809.m6809H.M6809_IRQ_LINE;
import static mame037b16.mame.Machine;
import static arcadeflex036.osdepend.logerror;
import static arcadeflex056.fileio.osd_fread;
import static arcadeflex056.fileio.osd_fwrite;
import static common.libc.cstdlib.rand;
import common.ptr.UBytePtr;
import static gr.codebb.arcadeflex.WIP.v037b16.machine._6821pia.pia_config;
import static gr.codebb.arcadeflex.WIP.v037b16.machine._6821pia.pia_reset;
import static gr.codebb.arcadeflex.WIP.v037b16.machine._6821pia.pia_unconfig;
import static gr.codebb.arcadeflex.WIP.v037b16.machine._6821piaH.PIA_STANDARD_ORDERING;
import gr.codebb.arcadeflex.WIP.v037b16.machine._6821piaH.pia6821_interface;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.tms34061.tms34061_latch_w;
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import gr.codebb.arcadeflex.v037b16.mame.drawgfxH.rectangle;
import gr.codebb.arcadeflex.v037b16.mame.driverH.GameDriver;
import gr.codebb.arcadeflex.v037b16.mame.driverH.MachineDriver;
import static gr.codebb.arcadeflex.v037b16.mame.driverH.ROT0;
import static gr.codebb.arcadeflex.v037b16.mame.driverH.ROT270;
import static gr.codebb.arcadeflex.v037b16.mame.driverH.VIDEO_MODIFIES_PALETTE;
import static gr.codebb.arcadeflex.v037b16.mame.driverH.VIDEO_TYPE_RASTER;
import static gr.codebb.arcadeflex.v037b16.mame.driverH.VIDEO_UPDATE_BEFORE_VBLANK;
import static gr.codebb.arcadeflex.v037b16.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b16.mame.inputH.*;
import gr.codebb.arcadeflex.v037b16.mame.sndintrfH.MachineSound;
import static gr.codebb.arcadeflex.v037b16.mame.sndintrfH.SOUND_OKIM6295;
import static gr.codebb.arcadeflex.v037b16.mame.sndintrfH.SOUND_YM2203;
import static gr.codebb.arcadeflex.v037b16.sound._2203intf.*;
import static gr.codebb.arcadeflex.v037b16.sound._2203intfH.YM2203_VOL;
import gr.codebb.arcadeflex.v037b16.sound._2203intfH.YM2203interface;
import gr.codebb.arcadeflex.v037b16.sound._3812intfH.YM3812interface;
import static gr.codebb.arcadeflex.v037b16.sound.okim6295.*;
import gr.codebb.arcadeflex.v037b16.sound.okim6295H.OKIM6295interface;
import static gr.codebb.arcadeflex.v056.machine.ticket.ticket_dispenser_init;
import static gr.codebb.arcadeflex.v056.machine.ticket.ticket_dispenser_r;
import static gr.codebb.arcadeflex.v056.machine.ticket.ticket_dispenser_w;
import static gr.codebb.arcadeflex.v056.machine.ticketH.TICKET_MOTOR_ACTIVE_HIGH;
import static gr.codebb.arcadeflex.v056.machine.ticketH.TICKET_STATUS_ACTIVE_LOW;
import gr.codebb.arcadeflex.v056.mame.timer.timer_callback;
import static gr.codebb.arcadeflex.v056.mame.timer.timer_set;
import static gr.codebb.arcadeflex.v056.mame.timerH.TIME_NOW;

public class itech8 {

    public static final int FULL_LOGGING = 0;

    public static final int CLOCK_8MHz = (8000000);
    public static final int CLOCK_12MHz = (12000000);

    /**
     * ***********************************
     *
     * Static data
     *
     ************************************
     */
    static int/*UINT8*/ blitter_int;
    static int/*UINT8*/ tms34061_int;
    static int/*UINT8*/ periodic_int;

    static int/*data8_t*/ u8_sound_data;

    static int/*data8_t*/ u8_pia_porta_data;
    static int/*data8_t*/ u8_pia_portb_data;

    /*TODO*///	
/*TODO*///	static data8_t *via6522;
/*TODO*///	static data16_t via6522_timer_count[2];
/*TODO*///	static void *via6522_timer[2];
/*TODO*///	static data8_t via6522_int_state;
/*TODO*///	
    static UBytePtr main_ram = new UBytePtr();
    static int[] main_ram_size = new int[1];

    /**
     * ***********************************
     *
     * Interrupt handling
     *
     ************************************
     */
    public static void itech8_update_interrupts(int periodic, int tms34061, int blitter) {
        /* update the states */
        if (periodic != -1) {
            periodic_int = periodic;
        }
        if (tms34061 != -1) {
            tms34061_int = tms34061;
        }
        if (blitter != -1) {
            blitter_int = blitter;
        }

        /* handle the 6809 case */
        if ((Machine.drv.cpu[0].cpu_type & ~CPU_FLAGS_MASK) == CPU_M6809) {
            /* just modify lines that have changed */
            if (periodic != -1) {
                cpu_set_nmi_line(0, periodic != 0 ? ASSERT_LINE : CLEAR_LINE);
            }
            if (tms34061 != -1) {
                cpu_set_irq_line(0, M6809_IRQ_LINE, tms34061 != 0 ? ASSERT_LINE : CLEAR_LINE);
            }
            if (blitter != -1) {
                cpu_set_irq_line(0, M6809_FIRQ_LINE, blitter != 0 ? ASSERT_LINE : CLEAR_LINE);
            }
        } /* handle the 68000 case */ else {
            int level = 0;

            /* determine which level is active */
            if (blitter_int != 0) {
                level = 2;
            }
            if (periodic_int != 0) {
                level = 3;
            }

            /* update it */
            if (level != 0) {
                cpu_set_irq_line(0, level, ASSERT_LINE);
            } else {
                cpu_set_irq_line(0, 7, CLEAR_LINE);
            }
        }
    }

    /**
     * ***********************************
     *
     * Interrupt generation
     *
     ************************************
     */
    public static InterruptPtr generate_nmi = new InterruptPtr() {
        public int handler() {
            /* signal the NMI */
            itech8_update_interrupts(1, -1, -1);
            itech8_update_interrupts(0, -1, -1);

            if (FULL_LOGGING != 0) {
                logerror("------------ VBLANK (%d) --------------\n", cpu_getscanline());
            }
            return ignore_interrupt.handler();
        }
    };

    public static WriteHandlerPtr nmi_ack_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* doesn't seem to hold for every game (e.g., hstennis) */
 /*	cpu_set_nmi_line(0, CLEAR_LINE);*/
        }
    };

    static WriteYmHandlerPtr generate_sound_irq = new WriteYmHandlerPtr() {
        @Override
        public void handler(int state) {
            cpu_set_irq_line(1, M6809_FIRQ_LINE, state != 0 ? ASSERT_LINE : CLEAR_LINE);
        }
    };

    /**
     * ***********************************
     *
     * Machine initialization
     *
     ************************************
     */
    public static InitMachinePtr init_machine = new InitMachinePtr() {
        public void handler() {
            /* make sure bank 0 is selected */
            if ((Machine.drv.cpu[0].cpu_type & ~CPU_FLAGS_MASK) == CPU_M6809) {
                cpu_setbank(1, new UBytePtr(memory_region(REGION_CPU1), 0x4000));
            }

            /* reset the PIA (if used) */
            pia_unconfig();
            pia_config(0, PIA_STANDARD_ORDERING, pia_interface);
            pia_reset();
            	
            /*TODO*///		/* reset the VIA chip (if used) */
/*TODO*///		via6522_timer_count[0] = via6522_timer_count[1] = 0;
/*TODO*///		via6522_timer[0] = via6522_timer[1] = 0;
/*TODO*///		via6522_int_state = 0;
/*TODO*///	
            /* reset the ticket dispenser */
            ticket_dispenser_init(200, TICKET_MOTOR_ACTIVE_HIGH, TICKET_STATUS_ACTIVE_LOW);
        }
    };

    /**
     * ***********************************
     *
     * Bank switching
     *
     ************************************
     */
    public static WriteHandlerPtr blitter_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bit 0x20 on address 7 controls CPU banking */
            if (offset / 2 == 7) {
                cpu_setbank(1, new UBytePtr(memory_region(REGION_CPU1), 0x4000 + 0xc000 * ((data >> 5) & 1)));
            }

            /* the rest is handled by the video hardware */
            itech8_blitter_w.handler(offset, data);
        }
    };

    public static WriteHandlerPtr rimrockn_bank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* banking is controlled here instead of by the blitter output */
            cpu_setbank(1, new UBytePtr(memory_region(REGION_CPU1), 0x4000 + 0xc000 * (data & 3)));
        }
    };

    /**
     * ***********************************
     *
     * Input handling
     *
     ************************************
     */
    public static ReadHandlerPtr special_port0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int result = readinputport(0);
            result = (result & 0xfe) | (u8_pia_portb_data & 0x01);
            return result;
        }
    };

    /**
     * ***********************************
     *
     * 6821 PIA handling
     *
     ************************************
     */
    public static WriteHandlerPtr pia_porta_out = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            logerror("PIA port A write = %02x\n", data);
            u8_pia_porta_data = data & 0xFF;
        }
    };

    public static WriteHandlerPtr pia_portb_out = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            logerror("PIA port B write = %02x\n", data);

            /* bit 0 provides feedback to the main CPU */
 /* bit 4 controls the ticket dispenser */
 /* bit 5 controls the coin counter */
 /* bit 6 controls the diagnostic sound LED */
            u8_pia_portb_data = data & 0xFF;
            ticket_dispenser_w.handler(0, (data & 0x10) << 3);
            coin_counter_w(0, (data & 0x20) >> 5);
        }
    };
    /**
     * ***********************************
     *
     * 6821 PIA interface
     *
     ************************************
     */

    public static pia6821_interface pia_interface = new pia6821_interface(
            null, ticket_dispenser_r, null, null, null, null, /* PIA inputs: A, B, CA1, CB1, CA2, CB2 */
            pia_porta_out, pia_portb_out, null, null, /* PIA outputs: A, B, CA2, CB2 */
            null, null /* PIA IRQs: A, B */
    );

    public static WriteHandlerPtr ym2203_portb_out = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            logerror("YM2203 port B write = %02x\n", data);

            /* bit 0 provides feedback to the main CPU */
 /* bit 5 controls the coin counter */
 /* bit 6 controls the diagnostic sound LED */
 /* bit 7 controls the ticket dispenser */
            u8_pia_portb_data = data & 0xFF;
            ticket_dispenser_w.handler(0, data & 0x80);
            coin_counter_w(0, (data & 0x20) >> 5);
        }
    };

    /**
     * ***********************************
     *
     * Sound communication
     *
     ************************************
     */
    public static timer_callback delayed_sound_data_w = new timer_callback() {
        public void handler(int data) {
            u8_sound_data = data & 0xFF;
            cpu_set_irq_line(1, M6809_IRQ_LINE, ASSERT_LINE);
        }
    };

    public static WriteHandlerPtr sound_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            timer_set(TIME_NOW, data, delayed_sound_data_w);
        }
    };

    public static WriteHandlerPtr gtg2_sound_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* on the later GTG2 board, they swizzle the data lines */
            data = ((data & 0x80) >> 7)
                    | ((data & 0x5d) << 1)
                    | ((data & 0x20) >> 3)
                    | ((data & 0x02) << 5);
            timer_set(TIME_NOW, data, delayed_sound_data_w);
        }
    };

    public static ReadHandlerPtr sound_data_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            cpu_set_irq_line(1, M6809_IRQ_LINE, CLEAR_LINE);
            return u8_sound_data & 0xFF;
        }
    };
    /*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Sound 6522 VIA handling
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	INLINE void update_via_int(void)
/*TODO*///	{
/*TODO*///		/* if interrupts are enabled and one is pending, set the line */
/*TODO*///		if ((via6522[14] & 0x80) && (via6522_int_state & via6522[14]))
/*TODO*///			cpu_set_irq_line(1, M6809_FIRQ_LINE, ASSERT_LINE);
/*TODO*///		else
/*TODO*///			cpu_set_irq_line(1, M6809_FIRQ_LINE, CLEAR_LINE);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static void via6522_timer_callback(int which)
/*TODO*///	{
/*TODO*///		via6522_int_state |= 0x40 >> which;
/*TODO*///		update_via_int();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr via6522_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		/* update the data */
/*TODO*///		via6522[offset] = data;
/*TODO*///	
/*TODO*///		/* switch off the offset */
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			case 0:		/* write to port B */
/*TODO*///				pia_portb_out(0, data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 5:		/* write into high order timer 1 */
/*TODO*///				via6522_timer_count[0] = (via6522[5] << 8) | via6522[4];
/*TODO*///				if (via6522_timer[0])
/*TODO*///					timer_remove(via6522_timer[0]);
/*TODO*///				via6522_timer[0] = timer_pulse(TIME_IN_HZ(CLOCK_8MHz/4) * (double)via6522_timer_count[0], 0, via6522_timer_callback);
/*TODO*///	
/*TODO*///				via6522_int_state &= ~0x40;
/*TODO*///				update_via_int();
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 13:	/* write interrupt flag register */
/*TODO*///				via6522_int_state &= ~data;
/*TODO*///				update_via_int();
/*TODO*///				break;
/*TODO*///	
/*TODO*///			default:	/* log everything else */
/*TODO*///				if (FULL_LOGGING != 0) logerror("VIA write(%02x) = %02x\n", offset, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr via6522_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int result = 0;
/*TODO*///	
/*TODO*///		/* switch off the offset */
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			case 4:		/* read low order timer 1 */
/*TODO*///				via6522_int_state &= ~0x40;
/*TODO*///				update_via_int();
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 13:	/* interrupt flag register */
/*TODO*///				result = via6522_int_state & 0x7f;
/*TODO*///				if (via6522_int_state & via6522[14]) result |= 0x80;
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	
/*TODO*///		if (FULL_LOGGING != 0) logerror("VIA read(%02x) = %02x\n", offset, result);
/*TODO*///		return result;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	16-bit memory shunts
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static READ16_HANDLER( blitter16_r )
/*TODO*///	{
/*TODO*///		return (itech8_blitter_r(offset * 2 + 0) << 8) + itech8_blitter_r(offset * 2 + 1);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static READ16_HANDLER( tms34061_16_r )
/*TODO*///	{
/*TODO*///		/* since multiple XY accesses can move the pointer multiple times, we have to */
/*TODO*///		/* be careful to only perform one read per access here; fortunately, the low */
/*TODO*///		/* bit doesn't matter in XY addressing mode */
/*TODO*///		if ((offset & 0x700) == 0x100)
/*TODO*///		{
/*TODO*///			int result = itech8_tms34061_r(offset * 2);
/*TODO*///			return (result << 8) | result;
/*TODO*///		}
/*TODO*///		else
/*TODO*///			return (itech8_tms34061_r(offset * 2 + 0) << 8) + itech8_tms34061_r(offset * 2 + 1);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static WRITE16_HANDLER( sound_data16_w )
/*TODO*///	{
/*TODO*///		if (ACCESSING_MSB != 0)
/*TODO*///			sound_data_w(0, data >> 8);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static WRITE16_HANDLER( grom_bank16_w )
/*TODO*///	{
/*TODO*///		if (ACCESSING_MSB != 0)
/*TODO*///			*itech8_grom_bank = data >> 8;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static WRITE16_HANDLER( display_page16_w )
/*TODO*///	{
/*TODO*///		if (ACCESSING_MSB != 0)
/*TODO*///			*itech8_display_page = ~data >> 8;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static WRITE16_HANDLER( tms34061_latch16_w )
/*TODO*///	{
/*TODO*///		if (ACCESSING_MSB != 0)
/*TODO*///			tms34061_latch_w(0, data >> 8);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static WRITE16_HANDLER( blitter16_w )
/*TODO*///	{
/*TODO*///		if (ACCESSING_MSB != 0)
/*TODO*///			itech8_blitter_w(offset * 2 + 0, data >> 8);
/*TODO*///		if (ACCESSING_LSB != 0)
/*TODO*///			itech8_blitter_w(offset * 2 + 1, data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static WRITE16_HANDLER( palette_addr16_w )
/*TODO*///	{
/*TODO*///		if (ACCESSING_MSB != 0)
/*TODO*///			itech8_palette_address_w(0, data >> 8);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static WRITE16_HANDLER( palette_data16_w )
/*TODO*///	{
/*TODO*///		if (ACCESSING_MSB != 0)
/*TODO*///			itech8_palette_data_w(0, data >> 8);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static WRITE16_HANDLER( tms34061_16_w )
/*TODO*///	{
/*TODO*///		if (ACCESSING_MSB != 0)
/*TODO*///			itech8_tms34061_w(offset * 2 + 0, data >> 8);
/*TODO*///		else if (ACCESSING_LSB != 0)
/*TODO*///			itech8_tms34061_w(offset * 2 + 1, data);
/*TODO*///	}

    /**
     * ***********************************
     *
     * NVRAM read/write
     *
     ************************************
     */
    public static nvramPtr nvram_handler = new nvramPtr() {
        public void handler(Object file, int read_or_write) {
            int i;

            if (read_or_write != 0) {
                osd_fwrite(file, main_ram, main_ram_size[0]);
            } else if (file != null) {
                osd_fread(file, main_ram, main_ram_size[0]);
            } else {
                for (i = 0; i < main_ram_size[0]; i++) {
                    main_ram.write(i, rand());
                }
            }
        }
    };

    /**
     * ***********************************
     *
     * Main CPU memory handlers
     *
     ************************************
     */
    /*------ common layout with TMS34061 at 0000 ------*/
    public static Memory_ReadAddress tmslo_readmem[] = {
        new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
        new Memory_ReadAddress(0x0000, 0x0fff, itech8_tms34061_r),
        new Memory_ReadAddress(0x1140, 0x1140, special_port0_r),
        new Memory_ReadAddress(0x1160, 0x1160, input_port_1_r),
        new Memory_ReadAddress(0x1180, 0x1180, input_port_2_r),
        new Memory_ReadAddress(0x11c0, 0x11d7, itech8_blitter_r),
        new Memory_ReadAddress(0x11d8, 0x11d9, input_port_3_r),
        new Memory_ReadAddress(0x11da, 0x11db, input_port_4_r),
        new Memory_ReadAddress(0x11dc, 0x11dd, input_port_5_r),
        new Memory_ReadAddress(0x11de, 0x11df, input_port_6_r),
        new Memory_ReadAddress(0x2000, 0x3fff, MRA_RAM),
        new Memory_ReadAddress(0x4000, 0xffff, MRA_BANK1),
        new Memory_ReadAddress(MEMPORT_MARKER, 0)
    };

    public static Memory_WriteAddress tmslo_writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
        new Memory_WriteAddress(0x0000, 0x0fff, itech8_tms34061_w),
        new Memory_WriteAddress(0x1100, 0x1100, MWA_NOP),
        new Memory_WriteAddress(0x1120, 0x1120, sound_data_w),
        new Memory_WriteAddress(0x1140, 0x1140, MWA_RAM, itech8_grom_bank),
        new Memory_WriteAddress(0x1160, 0x1160, MWA_RAM, itech8_display_page),
        new Memory_WriteAddress(0x1180, 0x1180, tms34061_latch_w),
        new Memory_WriteAddress(0x11a0, 0x11a0, nmi_ack_w),
        new Memory_WriteAddress(0x11c0, 0x11df, blitter_w),
        new Memory_WriteAddress(0x11e0, 0x11e0, itech8_palette_address_w),
        new Memory_WriteAddress(0x11e2, 0x11e3, itech8_palette_data_w),
        new Memory_WriteAddress(0x2000, 0x3fff, MWA_RAM, main_ram, main_ram_size),
        new Memory_WriteAddress(0x4000, 0xffff, MWA_ROM),
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };

    /*------ common layout with TMS34061 at 1000 ------*/
    public static Memory_ReadAddress tmshi_readmem[] = {
        new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
        new Memory_ReadAddress(0x1000, 0x1fff, itech8_tms34061_r),
        new Memory_ReadAddress(0x0140, 0x0140, special_port0_r),
        new Memory_ReadAddress(0x0160, 0x0160, input_port_1_r),
        new Memory_ReadAddress(0x0180, 0x0180, input_port_2_r),
        new Memory_ReadAddress(0x01c0, 0x01d7, itech8_blitter_r),
        new Memory_ReadAddress(0x01d8, 0x01d9, input_port_3_r),
        new Memory_ReadAddress(0x01da, 0x01db, input_port_4_r),
        new Memory_ReadAddress(0x01dc, 0x01dd, input_port_5_r),
        new Memory_ReadAddress(0x01de, 0x01df, input_port_6_r),
        new Memory_ReadAddress(0x2000, 0x3fff, MRA_RAM),
        new Memory_ReadAddress(0x4000, 0xffff, MRA_BANK1),
        new Memory_ReadAddress(MEMPORT_MARKER, 0)
    };

    public static Memory_WriteAddress tmshi_writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
        new Memory_WriteAddress(0x1000, 0x1fff, itech8_tms34061_w),
        new Memory_WriteAddress(0x0100, 0x0100, MWA_NOP),
        new Memory_WriteAddress(0x0120, 0x0120, sound_data_w),
        new Memory_WriteAddress(0x0140, 0x0140, MWA_RAM, itech8_grom_bank),
        new Memory_WriteAddress(0x0160, 0x0160, MWA_RAM, itech8_display_page),
        new Memory_WriteAddress(0x0180, 0x0180, tms34061_latch_w),
        new Memory_WriteAddress(0x01a0, 0x01a0, nmi_ack_w),
        new Memory_WriteAddress(0x01c0, 0x01df, blitter_w),
        new Memory_WriteAddress(0x01e0, 0x01e0, itech8_palette_address_w),
        new Memory_WriteAddress(0x01e2, 0x01e3, itech8_palette_data_w),
        new Memory_WriteAddress(0x2000, 0x3fff, MWA_RAM, main_ram, main_ram_size),
        new Memory_WriteAddress(0x4000, 0xffff, MWA_ROM),
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };
    /*TODO*///	
/*TODO*///	/*------ Golden Tee Golf II 1992 layout ------*/
/*TODO*///	public static Memory_ReadAddress gtg2_readmem[]={
/*TODO*///		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x1000, 0x1fff, itech8_tms34061_r ),
/*TODO*///		new Memory_ReadAddress( 0x0100, 0x0100, input_port_0_r ),
/*TODO*///		new Memory_ReadAddress( 0x0120, 0x0120, input_port_1_r ),
/*TODO*///		new Memory_ReadAddress( 0x0140, 0x0140, input_port_2_r ),
/*TODO*///		new Memory_ReadAddress( 0x0180, 0x0197, itech8_blitter_r ),
/*TODO*///		new Memory_ReadAddress( 0x0198, 0x0199, input_port_3_r ),
/*TODO*///		new Memory_ReadAddress( 0x019a, 0x019b, input_port_4_r ),
/*TODO*///		new Memory_ReadAddress( 0x019c, 0x019d, input_port_5_r ),
/*TODO*///		new Memory_ReadAddress( 0x019e, 0x019f, input_port_6_r ),
/*TODO*///		new Memory_ReadAddress( 0x2000, 0x3fff, MRA_RAM ),
/*TODO*///		new Memory_ReadAddress( 0x4000, 0xffff, MRA_BANK1 ),
/*TODO*///		new Memory_ReadAddress(MEMPORT_MARKER, 0)
/*TODO*///	};
/*TODO*///	
/*TODO*///	public static Memory_WriteAddress gtg2_writemem[]={
/*TODO*///		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x1000, 0x1fff, itech8_tms34061_w ),
/*TODO*///		new Memory_WriteAddress( 0x01c0, 0x01c0, gtg2_sound_data_w ),
/*TODO*///		new Memory_WriteAddress( 0x0160, 0x0160, MWA_RAM, &itech8_grom_bank ),
/*TODO*///		new Memory_WriteAddress( 0x0120, 0x0120, MWA_RAM, &itech8_display_page ),
/*TODO*///		new Memory_WriteAddress( 0x01e0, 0x01e0, tms34061_latch_w ),
/*TODO*///		new Memory_WriteAddress( 0x0100, 0x0100, nmi_ack_w ),
/*TODO*///		new Memory_WriteAddress( 0x0180, 0x019f, blitter_w ),
/*TODO*///		new Memory_WriteAddress( 0x0140, 0x0140, itech8_palette_address_w ),
/*TODO*///		new Memory_WriteAddress( 0x0142, 0x0143, itech8_palette_data_w ),
/*TODO*///		new Memory_WriteAddress( 0x2000, 0x3fff, MWA_RAM, &main_ram, &main_ram_size ),
/*TODO*///		new Memory_WriteAddress( 0x4000, 0xffff, MWA_ROM ),
/*TODO*///		new Memory_WriteAddress(MEMPORT_MARKER, 0)
/*TODO*///	};
/*TODO*///	
/*TODO*///	/*------ Ninja Clowns layout ------*/
/*TODO*///	static MEMORY_READ16_START( ninclown_readmem )
/*TODO*///		{ 0x000000, 0x003fff, MRA16_RAM },
/*TODO*///		{ 0x004000, 0x07ffff, MRA16_ROM },
/*TODO*///		{ 0x100100, 0x100101, input_port_0_word_r },
/*TODO*///		{ 0x100180, 0x100181, input_port_1_word_r },
/*TODO*///		{ 0x100280, 0x100281, input_port_2_word_r },
/*TODO*///		{ 0x100300, 0x10031f, blitter16_r },
/*TODO*///		{ 0x110000, 0x110fff, tms34061_16_r },
/*TODO*///	MEMORY_END
/*TODO*///	
/*TODO*///	
/*TODO*///	static MEMORY_WRITE16_START( ninclown_writemem )
/*TODO*///		{ 0x000000, 0x00007f, MWA16_RAM },
/*TODO*///		{ 0x000080, 0x003fff, MWA16_RAM, (data16_t **)&main_ram, &main_ram_size },
/*TODO*///		{ 0x004000, 0x07ffff, MWA16_ROM },
/*TODO*///		{ 0x100080, 0x100081, sound_data16_w },
/*TODO*///		{ 0x100100, 0x100101, grom_bank16_w, (data16_t **)&itech8_grom_bank },
/*TODO*///		{ 0x100180, 0x100181, display_page16_w, (data16_t **)&itech8_display_page },
/*TODO*///		{ 0x100240, 0x100241, tms34061_latch16_w },
/*TODO*///		{ 0x100280, 0x100281, MWA16_NOP },
/*TODO*///		{ 0x100300, 0x10031f, blitter16_w },
/*TODO*///		{ 0x100380, 0x100381, palette_addr16_w },
/*TODO*///		{ 0x1003a0, 0x1003a1, palette_data16_w },
/*TODO*///		{ 0x110000, 0x110fff, tms34061_16_w },
/*TODO*///	MEMORY_END
/*TODO*///	
/*TODO*///	

    /**
     * ***********************************
     *
     * Sound CPU memory handlers
     *
     ************************************
     */
    /*------ YM2203-based sound board ------*/
    public static Memory_ReadAddress sound2203_readmem[] = {
        new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
        new Memory_ReadAddress(0x1000, 0x1000, sound_data_r),
        new Memory_ReadAddress(0x2000, 0x2000, YM2203_status_port_0_r),
        new Memory_ReadAddress(0x2002, 0x2002, YM2203_status_port_0_r),
        new Memory_ReadAddress(0x3000, 0x37ff, MRA_RAM),
        new Memory_ReadAddress(0x4000, 0x4000, OKIM6295_status_0_r),
        new Memory_ReadAddress(0x8000, 0xffff, MRA_ROM),
        new Memory_ReadAddress(MEMPORT_MARKER, 0)
    };

    public static Memory_WriteAddress sound2203_writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
        new Memory_WriteAddress(0x0000, 0x0000, MWA_NOP),
        new Memory_WriteAddress(0x2000, 0x2000, YM2203_control_port_0_w),
        new Memory_WriteAddress(0x2001, 0x2001, YM2203_write_port_0_w),
        new Memory_WriteAddress(0x2002, 0x2002, YM2203_control_port_0_w),
        new Memory_WriteAddress(0x2003, 0x2003, YM2203_write_port_0_w),
        new Memory_WriteAddress(0x3000, 0x37ff, MWA_RAM),
        new Memory_WriteAddress(0x4000, 0x4000, OKIM6295_data_0_w),
        new Memory_WriteAddress(0x8000, 0xffff, MWA_ROM),
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };

    /*TODO*///	
/*TODO*///	/*------ YM3812-based sound board ------*/
/*TODO*///	public static Memory_ReadAddress sound3812_readmem[]={
/*TODO*///		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x1000, 0x1000, sound_data_r ),
/*TODO*///		new Memory_ReadAddress( 0x2000, 0x2000, YM3812_status_port_0_r ),
/*TODO*///		new Memory_ReadAddress( 0x3000, 0x37ff, MRA_RAM ),
/*TODO*///		new Memory_ReadAddress( 0x4000, 0x4000, OKIM6295_status_0_r ),
/*TODO*///		new Memory_ReadAddress( 0x5000, 0x5003, pia_0_r ),
/*TODO*///		new Memory_ReadAddress( 0x8000, 0xffff, MRA_ROM ),
/*TODO*///		new Memory_ReadAddress(MEMPORT_MARKER, 0)
/*TODO*///	};
/*TODO*///	
/*TODO*///	public static Memory_WriteAddress sound3812_writemem[]={
/*TODO*///		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0x0000, MWA_NOP ),
/*TODO*///		new Memory_WriteAddress( 0x2000, 0x2000, YM3812_control_port_0_w ),
/*TODO*///		new Memory_WriteAddress( 0x2001, 0x2001, YM3812_write_port_0_w ),
/*TODO*///		new Memory_WriteAddress( 0x3000, 0x37ff, MWA_RAM ),
/*TODO*///		new Memory_WriteAddress( 0x4000, 0x4000, OKIM6295_data_0_w ),
/*TODO*///		new Memory_WriteAddress( 0x5000, 0x5003, pia_0_w ),
/*TODO*///		new Memory_WriteAddress( 0x8000, 0xffff, MWA_ROM ),
/*TODO*///		new Memory_WriteAddress(MEMPORT_MARKER, 0)
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Other CPU memory handlers
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	public static Memory_ReadAddress slikz80_readmem[]={
/*TODO*///		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_ReadAddress( 0x0000, 0x7ff, MRA_ROM ),
/*TODO*///		new Memory_ReadAddress(MEMPORT_MARKER, 0)
/*TODO*///	};
/*TODO*///	
/*TODO*///	public static Memory_WriteAddress slikz80_writemem[]={
/*TODO*///		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),	new Memory_WriteAddress( 0x0000, 0x7f, MWA_ROM ),
/*TODO*///		new Memory_WriteAddress(MEMPORT_MARKER, 0)
/*TODO*///	};
/*TODO*///	
/*TODO*///	public static IO_ReadPort slikz80_readport[]={
/*TODO*///		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),	new IO_ReadPort( 0x00, 0x00, slikz80_port_r ),
/*TODO*///	MEMORY_END
/*TODO*///	
/*TODO*///	
/*TODO*///	public static IO_WritePort slikz80_writeport[]={
/*TODO*///		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),	new IO_WritePort( 0x00, 0x00, slikz80_port_w ),
/*TODO*///	MEMORY_END
/*TODO*///	
/*TODO*///	
    /**
     * ***********************************
     *
     * Port definitions
     *
     ************************************
     */
    public static void PORT_SERVICE_NO_TOGGLE(int mask, int _default) {
        PORT_BITX(mask, mask & _default, IPT_SERVICE1, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);
    }

    public static void UNUSED_ANALOG() {
        PORT_START();
        PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNUSED);
    }

    static InputPortPtr input_ports_stratab = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 40 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_SPECIAL);/* input from sound board */
            PORT_BIT(0x06, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x08, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_BIT(0x70, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_SERVICE_NO_TOGGLE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* 60 */
            PORT_BIT_NAME(0x01, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 | IPF_COCKTAIL, "P2 Right Hook");
            PORT_BIT_NAME(0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 | IPF_COCKTAIL, "P2 Left Hook");
            PORT_BIT_NAME(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1, "P1 Right Hook");
            PORT_BIT_NAME(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1, "P1 Left Hook");
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_START();
            /* 80 */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_START();
            /* analog C */
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_Y | IPF_PLAYER1 | IPF_CENTER, 25, 32, 0x80, 0x7f);
            PORT_START();
            /* analog D */
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_X | IPF_PLAYER1 | IPF_REVERSE | IPF_CENTER, 25, 32, 0x80, 0x7f);
            PORT_START();
            /* analog E */
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_Y | IPF_PLAYER2 | IPF_COCKTAIL | IPF_CENTER, 25, 32, 0x80, 0x7f);
            PORT_START();
            /* analog F */
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_X | IPF_PLAYER2 | IPF_COCKTAIL | IPF_REVERSE | IPF_CENTER, 25, 32, 0x80, 0x7f);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_sstrike = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 40 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_SPECIAL);/* input from sound board */
            PORT_BIT(0x7e, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_SERVICE_NO_TOGGLE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* 60 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN4);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_SPECIAL);
            PORT_BIT_NAME(0x08, IP_ACTIVE_LOW, IPT_BUTTON1, "Left Hook");
            PORT_BIT_NAME(0x10, IP_ACTIVE_LOW, IPT_BUTTON2, "Right Hook");
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_START();
            /* 80 */
            PORT_BIT_NAME(0x01, IP_ACTIVE_HIGH, IPT_BUTTON3, "Roll");

            PORT_START();
            /* analog C */
            PORT_ANALOG(0xff, 0x00, IPT_AD_STICK_X | IPF_PLAYER1, 50, 32, 0x80, 0x7f);
            PORT_START();
            /* analog D */
            PORT_ANALOG(0xff, 0x00, IPT_AD_STICK_Y | IPF_PLAYER1 | IPF_REVERSE, 50, 32, 0x80, 0x7f);
            PORT_START();
            /* analog E */
            PORT_ANALOG(0xff, 0x60, IPT_PADDLE | IPF_PLAYER2, 100, 1, 0x28, 0x98);
            UNUSED_ANALOG();
            /* analog F */
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_wfortune = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 40 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_SPECIAL);/* input from sound board */
            PORT_BIT(0x06, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x08, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_BIT(0x70, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_SERVICE_NO_TOGGLE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* 60 */
            PORT_BIT(0x07, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT_NAME(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3, "Blue Player");
            PORT_BIT_NAME(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2, "Yellow Player");
            PORT_BIT_NAME(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1, "Red Player");
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_START();
            /* 80 */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNUSED);
            UNUSED_ANALOG();
            /* analog C */

            PORT_START();
            /* analog D */
            PORT_ANALOG(0xff, 0x80, IPT_DIAL | IPF_PLAYER1, 75, 10, 0x00, 0xff);
            UNUSED_ANALOG();
            /* analog E */

            PORT_START();
            /* analog F */
            PORT_ANALOG(0xff, 0x80, IPT_DIAL | IPF_PLAYER2 | IPF_COCKTAIL, 75, 10, 0x00, 0xff);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_gtg = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 40 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_SPECIAL);/* input from sound board */
            PORT_BIT(0x06, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x08, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_BIT(0x70, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_SERVICE_NO_TOGGLE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* 60 */
 /* it is still unknown how the second player inputs are muxed in */
 /* currently we map both sets of controls to the same inputs */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1);
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 | IPF_COCKTAIL);
            PORT_BIT_NAME(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1, "P2 Swing");
            PORT_BIT_NAME(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 | IPF_COCKTAIL, "P2 Swing");
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_START();
            /* 80 */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNUSED);
            UNUSED_ANALOG();
            /* analog C */
            UNUSED_ANALOG();
            /* analog D */
            UNUSED_ANALOG();
            /* analog E */
            UNUSED_ANALOG();
            /* analog F */
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_gtg2 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 40 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_SERVICE_NO_TOGGLE(0x02, IP_ACTIVE_LOW);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x30, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x40, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_START();
            /* 60 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT_NAME(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1, "P1 Face Right");
            PORT_BIT_NAME(0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1, "P1 Face Left");
            PORT_BIT(0x78, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);
            PORT_START();
            /* 80 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT_NAME(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 | IPF_COCKTAIL, "P2 Face Right");
            PORT_BIT_NAME(0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 | IPF_COCKTAIL, "P2 Face Left");
            PORT_BIT(0x78, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);
            PORT_START();
            /* analog C */
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_X | IPF_PLAYER1 | IPF_CENTER, 25, 32, 0x80, 0x7f);
            PORT_START();
            /* analog D */
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_Y | IPF_PLAYER1 | IPF_REVERSE | IPF_CENTER, 25, 32, 0x80, 0x7f);
            PORT_START();
            /* analog E */
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_X | IPF_PLAYER2 | IPF_COCKTAIL | IPF_CENTER, 25, 32, 0x80, 0x7f);
            PORT_START();
            /* analog F */
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_Y | IPF_PLAYER2 | IPF_COCKTAIL | IPF_REVERSE | IPF_CENTER, 25, 32, 0x80, 0x7f);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_gtg2t = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 40 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_SPECIAL);/* input from sound board */
            PORT_BIT(0x06, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x08, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_BIT(0x70, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_SERVICE_NO_TOGGLE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* 60 */
            PORT_BIT_NAME(0x01, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 | IPF_COCKTAIL, "P2 Face Right");
            PORT_BIT_NAME(0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 | IPF_COCKTAIL, "P2 Face Left");
            PORT_BIT_NAME(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1, "P1 Face Right");
            PORT_BIT_NAME(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1, "P1 Face Left");
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_START();
            /* 80 */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_START();
            /* analog C */
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_X | IPF_PLAYER1 | IPF_CENTER, 25, 32, 0x80, 0x7f);
            PORT_START();
            /* analog D */
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_Y | IPF_PLAYER1 | IPF_REVERSE | IPF_CENTER, 25, 32, 0x80, 0x7f);
            PORT_START();
            /* analog E */
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_X | IPF_PLAYER2 | IPF_COCKTAIL | IPF_CENTER, 25, 32, 0x80, 0x7f);
            PORT_START();
            /* analog F */
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_Y | IPF_PLAYER2 | IPF_COCKTAIL | IPF_REVERSE | IPF_CENTER, 25, 32, 0x80, 0x7f);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_slikshot = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 40 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_SPECIAL);/* input from sound board */
            PORT_BIT(0x7e, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_SERVICE_NO_TOGGLE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* 60 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN4);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_SPECIAL);
            PORT_BIT_NAME(0x08, IP_ACTIVE_LOW, IPT_BUTTON2, "Yellow");
            PORT_BIT_NAME(0x10, IP_ACTIVE_LOW, IPT_BUTTON3, "Red");
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT_NAME(0x40, IP_ACTIVE_LOW, IPT_BUTTON1, "Green");
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_START();
            /* 80 */
            PORT_BIT_NAME(0x01, IP_ACTIVE_HIGH, IPT_BUTTON4, "Shoot");

            PORT_START();
            /* analog C */
            PORT_ANALOG(0xff, 0x00, IPT_AD_STICK_X | IPF_PLAYER1, 50, 32, 0x80, 0x7f);
            PORT_START();
            /* analog D */
            PORT_ANALOG(0xff, 0x00, IPT_AD_STICK_Y | IPF_PLAYER1 | IPF_REVERSE, 50, 32, 0x80, 0x7f);
            PORT_START();
            /* analog E */
            PORT_ANALOG(0xff, 0x60, IPT_PADDLE | IPF_PLAYER2, 100, 1, 0x28, 0x98);
            UNUSED_ANALOG();
            /* analog F */
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_arlingtn = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 40 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_SPECIAL);/* input from sound board */
            PORT_BIT(0x06, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            /* see code at e23c */
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0x70, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_SERVICE_NO_TOGGLE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* 60 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT_NAME(0x04, IP_ACTIVE_LOW, IPT_BUTTON2, "Place");
            PORT_BIT_NAME(0x08, IP_ACTIVE_LOW, IPT_BUTTON1, "Win");
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_START();
            /* 80 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN4);
            PORT_BIT(0x06, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT_NAME(0x08, IP_ACTIVE_LOW, IPT_BUTTON3, "Show");
            PORT_BIT_NAME(0x10, IP_ACTIVE_LOW, IPT_START1, "Start Race");
            PORT_BIT_NAME(0x20, IP_ACTIVE_LOW, IPT_BUTTON4, "Collect");
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);
            UNUSED_ANALOG();
            /* analog C */
            UNUSED_ANALOG();
            /* analog D */
            UNUSED_ANALOG();
            /* analog E */
            UNUSED_ANALOG();
            /* analog F */
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_neckneck = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 40 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_SPECIAL);/* input from sound board */
            PORT_BIT(0x06, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            /* see code at e23c */
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0x70, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_SERVICE_NO_TOGGLE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* 60 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT_NAME(0x04, IP_ACTIVE_LOW, IPT_BUTTON3, "Horse 3");
            PORT_BIT_NAME(0x08, IP_ACTIVE_LOW, IPT_BUTTON2, "Horse 2");
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT_NAME(0x20, IP_ACTIVE_LOW, IPT_BUTTON1, "Horse 1");
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_START();
            /* 80 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN4);
            PORT_BIT(0x06, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT_NAME(0x08, IP_ACTIVE_LOW, IPT_BUTTON4, "Horse 4");
            PORT_BIT_NAME(0x10, IP_ACTIVE_LOW, IPT_BUTTON6, "Horse 6");
            PORT_BIT_NAME(0x20, IP_ACTIVE_LOW, IPT_BUTTON5, "Horse 5");
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);
            UNUSED_ANALOG();
            /* analog C */
            UNUSED_ANALOG();
            /* analog D */
            UNUSED_ANALOG();
            /* analog E */
            UNUSED_ANALOG();
            /* analog F */
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_peggle = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 40 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_SPECIAL);/* input from sound board */
            PORT_BIT(0x7e, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_SERVICE_NO_TOGGLE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* 60 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1);
            PORT_BIT(0x30, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_START();
            /* 80 */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNUSED);
            UNUSED_ANALOG();
            /* analog C */
            UNUSED_ANALOG();
            /* analog D */
            UNUSED_ANALOG();
            /* analog E */
            UNUSED_ANALOG();
            /* analog F */
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_pegglet = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 40 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_SPECIAL);/* input from sound board */
            PORT_BIT(0x7e, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_SERVICE_NO_TOGGLE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* 60 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x3e, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_START();
            /* 80 */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNUSED);
            UNUSED_ANALOG();
            /* analog C */

            PORT_START();
            /* analog D */
            PORT_ANALOG(0xff, 0x00, IPT_DIAL | IPF_PLAYER1 | IPF_CENTER, 50, 10, 0x80, 0x7f);
            UNUSED_ANALOG();
            /* analog E */
            UNUSED_ANALOG();
            /* analog F */
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_hstennis = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 40 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_SPECIAL);/* input from sound board */
            PORT_BIT(0x06, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x08, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            /* see code at fbb5 */
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_BIT(0x60, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_SERVICE_NO_TOGGLE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* 60 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT_NAME(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1, "P2 Soft");
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1);
            PORT_BIT_NAME(0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1, "P1 Hard");
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_START();
            /* 80 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN4);
            PORT_BIT_NAME(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2, "P2 Soft");
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2);
            PORT_BIT_NAME(0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2, "P2 Hard");
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);
            UNUSED_ANALOG();
            /* analog C */
            UNUSED_ANALOG();
            /* analog D */
            UNUSED_ANALOG();
            /* analog E */
            UNUSED_ANALOG();
            /* analog F */
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_rimrockn = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 40 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_SPECIAL);/* input from sound board */
            PORT_BIT(0xfe, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_START();
            /* 60 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_SERVICE1);
            PORT_SERVICE_NO_TOGGLE(0x02, IP_ACTIVE_LOW);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN4);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_START();
            /* 80 */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_START();
            /* special 161 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT_NAME(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1, "P1 Pass");
            PORT_BIT_NAME(0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1, "P1 Shoot");
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);
            PORT_START();
            /* special 162 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT_NAME(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2, "P2 Pass");
            PORT_BIT_NAME(0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2, "P2 Shoot");
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);
            PORT_START();
            /* special 163 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT_NAME(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3, "P3 Pass");
            PORT_BIT_NAME(0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3, "P3 Shoot");
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER3);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START3);
            PORT_START();
            /* special 164 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT_NAME(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4, "P4 Pass");
            PORT_BIT_NAME(0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4, "P4 Shoot");
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER4);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER4);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER4);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER4);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START4);
            PORT_START();
            /* special 165 */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_ninclown = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 40 */
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_SERVICE1);
            PORT_SERVICE_NO_TOGGLE(0x0200, IP_ACTIVE_LOW);
            PORT_BIT(0x0c00, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0xc000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_START();
            /* 60 */
            PORT_BIT_NAME(0x0100, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1, "P1 Throw");
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1);
            PORT_BIT_NAME(0x4000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1, "P1 Kick");
            PORT_BIT_NAME(0x8000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1, "P1 Punch");

            PORT_START();
            /* 80 */
            PORT_BIT_NAME(0x0100, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2, "P2 Throw");
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2);
            PORT_BIT_NAME(0x4000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2, "P2 Kick");
            PORT_BIT_NAME(0x8000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2, "P2 Punch");

            UNUSED_ANALOG();
            /* analog C */
            UNUSED_ANALOG();
            /* analog D */
            UNUSED_ANALOG();
            /* analog E */
            UNUSED_ANALOG();
            /* analog F */
            INPUT_PORTS_END();
        }
    };

    /**
     * ***********************************
     *
     * Sound definitions
     *
     ************************************
     */
    static YM2203interface ym2203_interface = new YM2203interface(
            1,
            CLOCK_8MHz / 2,
            new int[]{YM2203_VOL(75, 7)},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{ym2203_portb_out},
            new WriteYmHandlerPtr[]{generate_sound_irq}
    );

    static YM3812interface ym3812_interface = new YM3812interface(
            1,
            CLOCK_8MHz / 2,
            new int[]{75},
            new WriteYmHandlerPtr[]{generate_sound_irq}
    );

    static OKIM6295interface oki6295_interface_low = new OKIM6295interface(
            1,
            new int[]{CLOCK_8MHz / 8 / 165},
            new int[]{REGION_SOUND1},
            new int[]{75}
    );

    static OKIM6295interface oki6295_interface_high = new OKIM6295interface(
            1,
            new int[]{CLOCK_8MHz / 8 / 128},
            new int[]{REGION_SOUND1},
            new int[]{75}
    );

    /**
     * ***********************************
     *
     * Machine driver
     *
     ************************************
     */
    /*TODO*///	
/*TODO*///	#define ITECH_DRIVER(NAME, CPUTYPE, CPUCLOCK, MAINMEM, YMTYPE, OKISPEED, XMIN, XMAX)	
/*TODO*///	static MachineDriver machine_driver_##NAME = new MachineDriver
/*TODO*///	(																				
/*TODO*///		/* basic machine hardware */												
/*TODO*///		new MachineCPU[] {																			
/*TODO*///			new MachineCPU(																		
/*TODO*///				CPU_##CPUTYPE,														
/*TODO*///				CPUCLOCK,															
/*TODO*///				MAINMEM##_readmem,MAINMEM##_writemem,null,null,							
/*TODO*///				generate_nmi,1														
/*TODO*///			),																		
/*TODO*///			new MachineCPU(																		
/*TODO*///				CPU_M6809,															
/*TODO*///				CLOCK_8MHz/4,														
/*TODO*///				sound##YMTYPE##_readmem,sound##YMTYPE##_writemem,null,null,				
/*TODO*///				ignore_interrupt,1													
/*TODO*///			)																		
/*TODO*///		},																			
/*TODO*///		60,(int)(((263. - 240.) / 263.) * 1000000. / 60.),							
/*TODO*///		1,																			
/*TODO*///		init_machine,																
/*TODO*///																					
/*TODO*///		/* video hardware */														
/*TODO*///		512, 263, new rectangle( XMIN, XMAX, 0, 239 ),											
/*TODO*///		null,																			
/*TODO*///		256,256,																	
/*TODO*///		null,																			
/*TODO*///																					
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,	
/*TODO*///		null,																			
/*TODO*///		itech8_vh_start,															
/*TODO*///		itech8_vh_stop,																
/*TODO*///		itech8_vh_screenrefresh,													
/*TODO*///																					
/*TODO*///		/* sound hardware */														
/*TODO*///		0,0,0,0,																	
/*TODO*///		new MachineSound[] {																			
/*TODO*///			new MachineSound( SOUND_YM##YMTYPE, ym##YMTYPE##_interface ),							
/*TODO*///			new MachineSound( SOUND_OKIM6295, oki6295_interface_##OKISPEED ),						
/*TODO*///		},																			
/*TODO*///		nvram_handler																
/*TODO*///	)
/*TODO*///	
/*TODO*///	
/*TODO*///	/*           NAME,      CPU,    CPUCLOCK,      MAINMEM,  YMTYPE, OKISPEED, XMIN, XMAX) */
/*TODO*///	ITECH_DRIVER(tmslo2203, M6809,  CLOCK_8MHz/4,  tmslo,    2203,   high,     0,    255);
    static MachineDriver machine_driver_tmshi2203 = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        CLOCK_8MHz / 4,
                        tmshi_readmem, tmshi_writemem, null, null,
                        generate_nmi, 1
                ),
                new MachineCPU(
                        CPU_M6809,
                        CLOCK_8MHz / 4,
                        sound2203_readmem, sound2203_writemem, null, null,
                        ignore_interrupt, 1
                )
            },
            60, (int) (((263. - 240.) / 263.) * 1000000. / 60.),
            1,
            init_machine,
            /* video hardware */
            512, 263, new rectangle(0, 255, 0, 239),
            null,
            256, 256,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
            null,
            itech8_vh_start,
            itech8_vh_stop,
            itech8_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(SOUND_YM2203, ym2203_interface),
                new MachineSound(SOUND_OKIM6295, oki6295_interface_high)
            },
            nvram_handler
    );
    /*TODO*///	ITECH_DRIVER(gtg2,      M6809,  CLOCK_8MHz/4,  gtg2,     3812,   high,     0,    255);
/*TODO*///	ITECH_DRIVER(peggle,    M6809,  CLOCK_8MHz/4,  tmslo,    3812,   high,     18,   367);
/*TODO*///	ITECH_DRIVER(arlingtn,  M6809,  CLOCK_8MHz/4,  tmshi,    3812,   low,      16,   389);
/*TODO*///	ITECH_DRIVER(neckneck,  M6809,  CLOCK_8MHz/4,  tmslo,    3812,   high,     8,    375);
/*TODO*///	ITECH_DRIVER(hstennis,  M6809,  CLOCK_8MHz/4,  tmshi,    3812,   high,     0,    375);
/*TODO*///	ITECH_DRIVER(rimrockn,  M6809,  CLOCK_12MHz/4, tmshi,    3812,   high,     24,   375);
/*TODO*///	ITECH_DRIVER(ninclown,  M68000, CLOCK_12MHz,   ninclown, 3812,   high,     64,   423);
/*TODO*///	
/*TODO*///	
/*TODO*///	static MachineDriver machine_driver_slikshot = new MachineDriver
/*TODO*///	(
/*TODO*///		/* basic machine hardware */
/*TODO*///		new MachineCPU[] {
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M6809,
/*TODO*///				CLOCK_8MHz/4,
/*TODO*///				tmshi_readmem,tmshi_writemem,null,null,
/*TODO*///				generate_nmi,1
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M6809,
/*TODO*///				CLOCK_8MHz/4,
/*TODO*///				sound2203_readmem,sound2203_writemem,null,null,
/*TODO*///				ignore_interrupt,1
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_Z80,
/*TODO*///				CLOCK_8MHz/2,
/*TODO*///				slikz80_readmem,slikz80_writemem,slikz80_readport,slikz80_writeport,
/*TODO*///				ignore_interrupt,1
/*TODO*///			)
/*TODO*///		},
/*TODO*///		60,(int)(((263. - 240.) / 263.) * 1000000. / 60.),
/*TODO*///		1,
/*TODO*///		init_machine,
/*TODO*///	
/*TODO*///		/* video hardware */
/*TODO*///		512, 263, new rectangle( 0, 255, 0, 239 ),
/*TODO*///		null,
/*TODO*///		257,257,
/*TODO*///		null,
/*TODO*///	
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
/*TODO*///		null,
/*TODO*///		slikshot_vh_start,
/*TODO*///		itech8_vh_stop,
/*TODO*///		itech8_vh_screenrefresh,
/*TODO*///	
/*TODO*///		/* sound hardware */
/*TODO*///		0,0,0,0,
/*TODO*///		new MachineSound[] {
/*TODO*///			new MachineSound( SOUND_YM2203, ym2203_interface ),
/*TODO*///			new MachineSound( SOUND_OKIM6295, oki6295_interface_high ),
/*TODO*///		},
/*TODO*///		nvram_handler
/*TODO*///	);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	static MachineDriver machine_driver_sstrike = new MachineDriver
/*TODO*///	(
/*TODO*///		/* basic machine hardware */
/*TODO*///		new MachineCPU[] {
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M6809,
/*TODO*///				CLOCK_8MHz/4,
/*TODO*///				tmslo_readmem,tmslo_writemem,null,null,
/*TODO*///				generate_nmi,1
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M6809,
/*TODO*///				CLOCK_8MHz/4,
/*TODO*///				sound2203_readmem,sound2203_writemem,null,null,
/*TODO*///				ignore_interrupt,1
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_Z80,
/*TODO*///				CLOCK_8MHz/2,
/*TODO*///				slikz80_readmem,slikz80_writemem,slikz80_readport,slikz80_writeport,
/*TODO*///				ignore_interrupt,1
/*TODO*///			)
/*TODO*///		},
/*TODO*///		60,(int)(((263. - 240.) / 263.) * 1000000. / 60.),
/*TODO*///		1,
/*TODO*///		init_machine,
/*TODO*///	
/*TODO*///		/* video hardware */
/*TODO*///		512, 263, new rectangle( 0, 255, 0, 239 ),
/*TODO*///		null,
/*TODO*///		257,257,
/*TODO*///		null,
/*TODO*///	
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
/*TODO*///		null,
/*TODO*///		slikshot_vh_start,
/*TODO*///		itech8_vh_stop,
/*TODO*///		itech8_vh_screenrefresh,
/*TODO*///	
/*TODO*///		/* sound hardware */
/*TODO*///		0,0,0,0,
/*TODO*///		new MachineSound[] {
/*TODO*///			new MachineSound( SOUND_YM2203, ym2203_interface ),
/*TODO*///			new MachineSound( SOUND_OKIM6295, oki6295_interface_high ),
/*TODO*///		},
/*TODO*///		nvram_handler
/*TODO*///	);
    /**
     * ***********************************
     *
     * ROM definitions
     *
     ************************************
     */
    static RomLoadPtr rom_stratab = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1, 0);
            ROM_LOAD("sbprogv3.bin", 0x08000, 0x8000, 0xa5ae728f);
            ROM_COPY(REGION_CPU1, 0x8000, 0x14000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("sbsnds.bin", 0x08000, 0x8000, 0xb36c8f0a);
            ROM_REGION(0xc0000, REGION_GFX1, 0);
            ROM_LOAD("grom0.bin", 0x00000, 0x20000, 0xa915b0bd);
            ROM_LOAD("grom1.bin", 0x20000, 0x20000, 0x340c661f);
            ROM_LOAD("grom2.bin", 0x40000, 0x20000, 0x5df9f1cf);
            ROM_REGION(0x20000, REGION_SOUND1, 0);
            ROM_LOAD("srom0.bin", 0x00000, 0x20000, 0x6ff390b9);
            ROM_END();
        }
    };

    static RomLoadPtr rom_wfortune = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1, 0);
            ROM_LOAD("wofpgm", 0x04000, 0x4000, 0xbd984654);
            ROM_CONTINUE(0x10000, 0xc000);
            ROM_COPY(REGION_CPU1, 0x14000, 0x8000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("wofsnd", 0x08000, 0x8000, 0x0a6aa5dc);
            ROM_REGION(0xc0000, REGION_GFX1, 0);
            ROM_LOAD("wofgrom0", 0x00000, 0x10000, 0x9a157b2c);
            ROM_LOAD("wofgrom1", 0x10000, 0x10000, 0x5064739b);
            ROM_LOAD("wofgrom2", 0x20000, 0x10000, 0x3d393b2b);
            ROM_LOAD("wofgrom3", 0x30000, 0x10000, 0x117a2ce9);
            ROM_REGION(0x20000, REGION_SOUND1, 0);
            ROM_LOAD("wofsbom0", 0x00000, 0x20000, 0x5c28c3fe);
            ROM_END();
        }
    };

    static RomLoadPtr rom_wfortuna = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1, 0);
            ROM_LOAD("wofpgmr1.bin", 0x04000, 0x4000, 0xc3d3eb21);
            ROM_CONTINUE(0x10000, 0xc000);
            ROM_COPY(REGION_CPU1, 0x14000, 0x8000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("wofsnd", 0x08000, 0x8000, 0x0a6aa5dc);
            ROM_REGION(0xc0000, REGION_GFX1, 0);
            ROM_LOAD("wofgrom0", 0x00000, 0x10000, 0x9a157b2c);
            ROM_LOAD("wofgrom1", 0x10000, 0x10000, 0x5064739b);
            ROM_LOAD("wofgrom2", 0x20000, 0x10000, 0x3d393b2b);
            ROM_LOAD("wofgrom3", 0x30000, 0x10000, 0x117a2ce9);
            ROM_REGION(0x20000, REGION_SOUND1, 0);
            ROM_LOAD("wofsbom0", 0x00000, 0x20000, 0x5c28c3fe);
            ROM_END();
        }
    };

    static RomLoadPtr rom_gtg = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1, 0);
            ROM_LOAD("u5.bin", 0x04000, 0x4000, 0x61984272);
            ROM_CONTINUE(0x10000, 0xc000);
            ROM_COPY(REGION_CPU1, 0x14000, 0x8000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("u27.bin", 0x08000, 0x8000, 0x358d2440);
            ROM_REGION(0xc0000, REGION_GFX1, 0);
            ROM_LOAD("grom0.bin", 0x00000, 0x20000, 0xa29c688a);
            ROM_LOAD("grom1.bin", 0x20000, 0x20000, 0xb52a23f6);
            ROM_LOAD("grom2.bin", 0x40000, 0x20000, 0x9b8e3a61);
            ROM_LOAD("grom3.bin", 0x60000, 0x20000, 0xb6e9fb15);
            ROM_LOAD("grom4.bin", 0x80000, 0x20000, 0xfaa16729);
            ROM_LOAD("grom5.bin", 0xa0000, 0x20000, 0x5b393314);
            ROM_REGION(0x20000, REGION_SOUND1, 0);
            ROM_LOAD("srom0.bin", 0x00000, 0x20000, 0x1cccbfdf);
            ROM_END();
        }
    };

    static RomLoadPtr rom_slikshot = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1, 0);
            ROM_LOAD("pgm20.u5", 0x04000, 0x4000, 0x370a00eb);
            ROM_CONTINUE(0x10000, 0xc000);
            ROM_COPY(REGION_CPU1, 0x14000, 0x8000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("u27.bin", 0x08000, 0x8000, 0xa96ce0f7);
            ROM_REGION(0x10000, REGION_CPU3, 0);
            ROM_LOAD("u53.bin", 0x00000, 0x0800, 0x04b85918);
            ROM_CONTINUE(0x00000, 0x0800);
            ROM_CONTINUE(0x00000, 0x0800);
            ROM_CONTINUE(0x00000, 0x0800);
            ROM_REGION(0xc0000, REGION_GFX1, 0);
            ROM_LOAD("grom0.bin", 0x00000, 0x20000, 0xe60c2804);
            ROM_LOAD("grom1.bin", 0x20000, 0x20000, 0xd764d542);
            ROM_REGION(0x10000, REGION_SOUND1, 0);
            ROM_LOAD("srom0.bin", 0x00000, 0x10000, 0x4b075f5e);
            ROM_END();
        }
    };

    static RomLoadPtr rom_sliksh17 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1, 0);
            ROM_LOAD("u5.bin", 0x04000, 0x4000, 0x09d70554);
            ROM_CONTINUE(0x10000, 0xc000);
            ROM_COPY(REGION_CPU1, 0x14000, 0x8000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("u27.bin", 0x08000, 0x8000, 0xa96ce0f7);
            ROM_REGION(0x10000, REGION_CPU3, 0);
            ROM_LOAD("u53.bin", 0x00000, 0x0800, 0x04b85918);
            ROM_CONTINUE(0x00000, 0x0800);
            ROM_CONTINUE(0x00000, 0x0800);
            ROM_CONTINUE(0x00000, 0x0800);
            ROM_REGION(0xc0000, REGION_GFX1, 0);
            ROM_LOAD("grom0.bin", 0x00000, 0x20000, 0xe60c2804);
            ROM_LOAD("grom1.bin", 0x20000, 0x20000, 0xd764d542);
            ROM_REGION(0x10000, REGION_SOUND1, 0);
            ROM_LOAD("srom0.bin", 0x00000, 0x10000, 0x4b075f5e);
            ROM_END();
        }
    };

    static RomLoadPtr rom_sstrike = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1, 0);
            ROM_LOAD("sstrku5.bin", 0x08000, 0x8000, 0xaf00cddf);
            ROM_COPY(REGION_CPU1, 0x8000, 0x14000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("sstrku27.bin", 0x08000, 0x8000, 0xefab7252);
            ROM_REGION(0x10000, REGION_CPU3, 0);
            ROM_LOAD("spstku53.bin", 0x00000, 0x0800, 0x04b85918);
            ROM_CONTINUE(0x00000, 0x0800);
            ROM_CONTINUE(0x00000, 0x0800);
            ROM_CONTINUE(0x00000, 0x0800);
            ROM_REGION(0xc0000, REGION_GFX1, 0);
            ROM_LOAD("sstgrom0.bin", 0x00000, 0x20000, 0x9cfb9849);
            ROM_LOAD("sstgrom1.bin", 0x20000, 0x20000, 0xd9ea14e1);
            ROM_LOAD("sstgrom2.bin", 0x40000, 0x20000, 0xdcd97bf7);
            ROM_REGION(0x20000, REGION_SOUND1, 0);
            ROM_LOAD("sstsrom0.bin", 0x00000, 0x20000, 0x6ff390b9);
            ROM_END();
        }
    };

    static RomLoadPtr rom_gtg2 = new RomLoadPtr() {
        public void handler() {
            /* banks are loaded in the opposite order from the others, */
            ROM_REGION(0x1c000, REGION_CPU1, 0);
            ROM_LOAD("u5.2", 0x10000, 0x4000, 0x4a61580f);
            ROM_CONTINUE(0x04000, 0xc000);
            ROM_COPY(REGION_CPU1, 0x8000, 0x14000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("u27.2", 0x08000, 0x8000, 0x55734876);
            ROM_REGION(0xc0000, REGION_GFX1, 0);
            ROM_LOAD("grom0.bin", 0x00000, 0x20000, 0xa29c688a);
            ROM_LOAD("grom1.bin", 0x20000, 0x20000, 0xa4182776);
            ROM_LOAD("grom2.bin", 0x40000, 0x20000, 0x0580bb99);
            ROM_LOAD("grom3.bin", 0x60000, 0x20000, 0x89edb624);
            ROM_LOAD("grom4.bin", 0x80000, 0x20000, 0xf6557950);
            ROM_LOAD("grom5.bin", 0xa0000, 0x20000, 0xa680ce6a);
            ROM_REGION(0x20000, REGION_SOUND1, 0);
            ROM_LOAD("vr-srom0", 0x00000, 0x20000, 0x4dd4db42);
            ROM_END();
        }
    };

    static RomLoadPtr rom_gtg2t = new RomLoadPtr() {
        public void handler() {
            /* banks are loaded in the opposite order from the others, */
            ROM_REGION(0x1c000, REGION_CPU1, 0);
            ROM_LOAD("u5", 0x10000, 0x4000, 0xc7b3a9f3);
            ROM_CONTINUE(0x04000, 0xc000);
            ROM_COPY(REGION_CPU1, 0x8000, 0x14000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("u27.bin", 0x08000, 0x8000, 0xdd2a5905);
            ROM_REGION(0xc0000, REGION_GFX1, 0);
            ROM_LOAD("grom0.bin", 0x00000, 0x20000, 0xa29c688a);
            ROM_LOAD("grom1.bin", 0x20000, 0x20000, 0xa4182776);
            ROM_LOAD("grom2.bin", 0x40000, 0x20000, 0x0580bb99);
            ROM_LOAD("grom3.bin", 0x60000, 0x20000, 0x89edb624);
            ROM_LOAD("grom4.bin", 0x80000, 0x20000, 0xf6557950);
            ROM_LOAD("grom5.bin", 0xa0000, 0x20000, 0xa680ce6a);
            ROM_REGION(0x20000, REGION_SOUND1, 0);
            ROM_LOAD("vr-srom0", 0x00000, 0x20000, 0x4dd4db42);
            ROM_END();
        }
    };

    static RomLoadPtr rom_gtg2j = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1, 0);
            ROM_LOAD("u5.bin", 0x04000, 0x4000, 0x9c95ceaa);
            ROM_CONTINUE(0x10000, 0xc000);
            ROM_COPY(REGION_CPU1, 0x14000, 0x8000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("u27.bin", 0x08000, 0x8000, 0xdd2a5905);
            ROM_REGION(0xc0000, REGION_GFX1, 0);
            ROM_LOAD("grom0.bin", 0x00000, 0x20000, 0xa29c688a);
            ROM_LOAD("grom1.bin", 0x20000, 0x20000, 0xa4182776);
            ROM_LOAD("grom2.bin", 0x40000, 0x20000, 0x0580bb99);
            ROM_LOAD("grom3.bin", 0x60000, 0x20000, 0x89edb624);
            ROM_LOAD("grom4.bin", 0x80000, 0x20000, 0xf6557950);
            ROM_LOAD("grom5.bin", 0xa0000, 0x20000, 0xa680ce6a);
            ROM_REGION(0x20000, REGION_SOUND1, 0);
            ROM_LOAD("srom0.bin", 0x00000, 0x20000, 0x1cccbfdf);
            ROM_END();
        }
    };

    static RomLoadPtr rom_arlingtn = new RomLoadPtr() {
        public void handler() {
            /* banks are loaded in the opposite order from the others, */
            ROM_REGION(0x1c000, REGION_CPU1, 0);
            ROM_LOAD("ahrd121.bin", 0x10000, 0x4000, 0x00aae02e);
            ROM_CONTINUE(0x04000, 0xc000);
            ROM_COPY(REGION_CPU1, 0x8000, 0x14000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("ahrsnd11.bin", 0x08000, 0x8000, 0xdec57dca);
            ROM_REGION(0xc0000, REGION_GFX1, 0);
            ROM_LOAD("grom0.bin", 0x00000, 0x20000, 0x5ef57fe5);
            ROM_LOAD("grom1.bin", 0x20000, 0x20000, 0x6aca95c0);
            ROM_LOAD("grom2.bin", 0x40000, 0x10000, 0x6d6fde1b);
            ROM_REGION(0x40000, REGION_SOUND1, 0);
            ROM_LOAD("srom0.bin", 0x00000, 0x40000, 0x56087f81);
            ROM_END();
        }
    };

    static RomLoadPtr rom_neckneck = new RomLoadPtr() {
        public void handler() {
            /* banks are loaded in the opposite order from the others, */
            ROM_REGION(0x1c000, REGION_CPU1, 0);
            ROM_LOAD("nn_prg12.u5", 0x04000, 0x4000, 0x8e51734a);
            ROM_CONTINUE(0x10000, 0xc000);
            ROM_COPY(REGION_CPU1, 0x14000, 0x8000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("nn_snd10.u27", 0x08000, 0x8000, 0x74771b2f);
            ROM_REGION(0xc0000, REGION_GFX1, 0);
            ROM_LOAD("nn_grom0.bin", 0x00000, 0x20000, 0x064d1464);
            ROM_LOAD("nn_grom1.bin", 0x20000, 0x20000, 0x622d9a0b);
            ROM_LOAD("nn_grom2.bin", 0x40000, 0x20000, 0xe7eb4020);
            ROM_LOAD("nn_grom3.bin", 0x60000, 0x20000, 0x765c8593);
            ROM_REGION(0x40000, REGION_SOUND1, 0);
            ROM_LOAD("nn_srom0.bin", 0x00000, 0x40000, 0x33687201);
            ROM_END();
        }
    };

    static RomLoadPtr rom_peggle = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1, 0);
            ROM_LOAD("j-stick.u5", 0x04000, 0x4000, 0x140d5a9c);
            ROM_CONTINUE(0x10000, 0xc000);
            ROM_COPY(REGION_CPU1, 0x14000, 0x8000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("sound.u27", 0x08000, 0x8000, 0xb99beb70);
            ROM_REGION(0xc0000, REGION_GFX1, 0);
            ROM_LOAD("grom0.bin", 0x00000, 0x20000, 0x5c02348d);
            ROM_LOAD("grom1.bin", 0x20000, 0x20000, 0x85a7a3a2);
            ROM_LOAD("grom2.bin", 0x40000, 0x20000, 0xbfe11f18);
            ROM_REGION(0x20000, REGION_SOUND1, 0);
            ROM_LOAD("srom0", 0x00000, 0x20000, 0x001846ea);
            ROM_END();
        }
    };

    static RomLoadPtr rom_pegglet = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1, 0);
            ROM_LOAD("trakball.u5", 0x04000, 0x4000, 0xd2694868);
            ROM_CONTINUE(0x10000, 0xc000);
            ROM_COPY(REGION_CPU1, 0x14000, 0x8000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("sound.u27", 0x08000, 0x8000, 0xb99beb70);
            ROM_REGION(0xc0000, REGION_GFX1, 0);
            ROM_LOAD("grom0.bin", 0x00000, 0x20000, 0x5c02348d);
            ROM_LOAD("grom1.bin", 0x20000, 0x20000, 0x85a7a3a2);
            ROM_LOAD("grom2.bin", 0x40000, 0x20000, 0xbfe11f18);
            ROM_REGION(0x20000, REGION_SOUND1, 0);
            ROM_LOAD("srom0", 0x00000, 0x20000, 0x001846ea);
            ROM_END();
        }
    };

    static RomLoadPtr rom_hstennis = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1, 0);
            ROM_LOAD("ten_v1_1.bin", 0x04000, 0x4000, 0xfaffab5c);
            ROM_CONTINUE(0x10000, 0xc000);
            ROM_COPY(REGION_CPU1, 0x14000, 0x8000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("tensd_v1.bin", 0x08000, 0x8000, 0xf034a694);
            ROM_REGION(0xc0000, REGION_GFX1, 0);
            ROM_LOAD("grom0.bin", 0x00000, 0x20000, 0x1e69ebae);
            ROM_LOAD("grom1.bin", 0x20000, 0x20000, 0x4e6a22d5);
            ROM_LOAD("grom2.bin", 0x40000, 0x20000, 0xc0b643a9);
            ROM_LOAD("grom3.bin", 0x60000, 0x20000, 0x54afb456);
            ROM_LOAD("grom4.bin", 0x80000, 0x20000, 0xee09d645);
            ROM_REGION(0x20000, REGION_SOUND1, 0);
            ROM_LOAD("srom0.bin", 0x00000, 0x20000, 0xd9ce58c3);
            ROM_END();
        }
    };

    static RomLoadPtr rom_rimrockn = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x34000, REGION_CPU1, 0);
            ROM_LOAD("u5-2_2", 0x04000, 0x4000, 0x97777683);
            ROM_CONTINUE(0x10000, 0x4000);
            ROM_CONTINUE(0x1c000, 0x4000);
            ROM_CONTINUE(0x28000, 0xc000);
            ROM_CONTINUE(0x2c000, 0x8000);
            ROM_COPY(REGION_CPU1, 0x2c000, 0x08000, 0x8000);
            ROM_COPY(REGION_CPU1, 0x2c000, 0x14000, 0x8000);
            ROM_COPY(REGION_CPU1, 0x2c000, 0x20000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("u27", 0x08000, 0x8000, 0x59f87f0e);
            ROM_REGION(0x100000, REGION_GFX1, 0);
            ROM_LOAD("grom00", 0x00000, 0x40000, 0x3eacbad9);
            ROM_LOAD("grom01", 0x40000, 0x40000, 0x864cc269);
            ROM_LOAD("grom02-2.st2", 0x80000, 0x40000, 0x47904233);
            ROM_LOAD("grom03-2.st2", 0xc0000, 0x40000, 0xf005f118);
            ROM_REGION(0x40000, REGION_SOUND1, 0);
            ROM_LOAD("srom0", 0x00000, 0x40000, 0x7ad42be0);
            ROM_END();
        }
    };

    static RomLoadPtr rom_rimrck20 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x34000, REGION_CPU1, 0);
            ROM_LOAD("rrb.bin", 0x04000, 0x4000, 0x7e9d5545);
            ROM_CONTINUE(0x10000, 0x4000);
            ROM_CONTINUE(0x1c000, 0x4000);
            ROM_CONTINUE(0x28000, 0xc000);
            ROM_CONTINUE(0x2c000, 0x8000);
            ROM_COPY(REGION_CPU1, 0x2c000, 0x08000, 0x8000);
            ROM_COPY(REGION_CPU1, 0x2c000, 0x14000, 0x8000);
            ROM_COPY(REGION_CPU1, 0x2c000, 0x20000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("u27", 0x08000, 0x8000, 0x59f87f0e);
            ROM_REGION(0x100000, REGION_GFX1, 0);
            ROM_LOAD("grom00", 0x00000, 0x40000, 0x3eacbad9);
            ROM_LOAD("grom01", 0x40000, 0x40000, 0x864cc269);
            ROM_LOAD("grom02-2.st2", 0x80000, 0x40000, 0x47904233);
            ROM_LOAD("grom03-2.st2", 0xc0000, 0x40000, 0xf005f118);
            ROM_REGION(0x40000, REGION_SOUND1, 0);
            ROM_LOAD("srom0", 0x00000, 0x40000, 0x7ad42be0);
            ROM_END();
        }
    };

    static RomLoadPtr rom_rimrck16 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x34000, REGION_CPU1, 0);
            ROM_LOAD("rrbbv16.u5", 0x04000, 0x4000, 0x999cd502);
            ROM_CONTINUE(0x10000, 0x4000);
            ROM_CONTINUE(0x1c000, 0x4000);
            ROM_CONTINUE(0x28000, 0xc000);
            ROM_CONTINUE(0x2c000, 0x8000);
            ROM_COPY(REGION_CPU1, 0x2c000, 0x08000, 0x8000);
            ROM_COPY(REGION_CPU1, 0x2c000, 0x14000, 0x8000);
            ROM_COPY(REGION_CPU1, 0x2c000, 0x20000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("u27", 0x08000, 0x8000, 0x59f87f0e);
            ROM_REGION(0x100000, REGION_GFX1, 0);
            ROM_LOAD("grom00", 0x00000, 0x40000, 0x3eacbad9);
            ROM_LOAD("grom01", 0x40000, 0x40000, 0x864cc269);
            ROM_LOAD("grom02", 0x80000, 0x40000, 0x34e567d5);
            ROM_LOAD("grom03", 0xc0000, 0x40000, 0xfd18045d);
            ROM_REGION(0x40000, REGION_SOUND1, 0);
            ROM_LOAD("srom0", 0x00000, 0x40000, 0x7ad42be0);
            ROM_END();
        }
    };

    static RomLoadPtr rom_rimrck12 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x34000, REGION_CPU1, 0);
            ROM_LOAD("rrbbv12.u5", 0x04000, 0x4000, 0x661761a6);
            ROM_CONTINUE(0x10000, 0x4000);
            ROM_CONTINUE(0x1c000, 0x4000);
            ROM_CONTINUE(0x28000, 0xc000);
            ROM_CONTINUE(0x2c000, 0x8000);
            ROM_COPY(REGION_CPU1, 0x2c000, 0x08000, 0x8000);
            ROM_COPY(REGION_CPU1, 0x2c000, 0x14000, 0x8000);
            ROM_COPY(REGION_CPU1, 0x2c000, 0x20000, 0x8000);

            ROM_REGION(0x10000, REGION_CPU2, 0);
            ROM_LOAD("rrbsndv1.u27", 0x08000, 0x8000, 0x8eda5f53);
            ROM_REGION(0x100000, REGION_GFX1, 0);
            ROM_LOAD("grom00", 0x00000, 0x40000, 0x3eacbad9);
            ROM_LOAD("grom01", 0x40000, 0x40000, 0x864cc269);
            ROM_LOAD("grom02", 0x80000, 0x40000, 0x34e567d5);
            ROM_LOAD("grom03", 0xc0000, 0x40000, 0xfd18045d);
            ROM_REGION(0x40000, REGION_SOUND1, 0);
            ROM_LOAD("srom0", 0x00000, 0x40000, 0x7ad42be0);
            ROM_END();
        }
    };

    /*TODO*///	static RomLoadPtr rom_ninclown = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x80000, REGION_CPU1, 0 );	ROM_LOAD16_BYTE( "prog1", 0x00000, 0x20000, 0xfabfdcd2 )
/*TODO*///		ROM_LOAD16_BYTE( "prog0", 0x00001, 0x20000, 0xeca63db5 )
/*TODO*///		ROM_COPY(    REGION_CPU1, 0x08000, 0x40000, 0x38000 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2, 0 );	ROM_LOAD( "nc-snd", 0x08000, 0x8000, 0xf9d5b4e1 );
/*TODO*///		ROM_REGION( 0x180000, REGION_GFX1, 0 );	ROM_LOAD( "nc-grom0", 0x000000, 0x40000, 0x532f7bff );	ROM_LOAD( "nc-grom1", 0x040000, 0x40000, 0x45640d4a );	ROM_LOAD( "nc-grom2", 0x080000, 0x40000, 0xc8281d06 );	ROM_LOAD( "nc-grom3", 0x0c0000, 0x40000, 0x2a6d33ac );	ROM_LOAD( "nc-grom4", 0x100000, 0x40000, 0x910876ba );	ROM_LOAD( "nc-grom5", 0x140000, 0x40000, 0x2533279b );
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1, 0 );	ROM_LOAD( "srom0.bin", 0x00000, 0x40000, 0xf6b501e1 );ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Driver-specific init
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static public static InitDriverPtr init_viasound = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		/* some games with a YM3812 use a VIA(6522) for timing and communication */
/*TODO*///		install_mem_read_handler (1, 0x5000, 0x500f, via6522_r);
/*TODO*///		via6522 = install_mem_write_handler(1, 0x5000, 0x500f, via6522_w);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	static public static InitDriverPtr init_slikshot = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		install_mem_read_handler (0, 0x0180, 0x0180, slikshot_z80_r);
/*TODO*///		install_mem_read_handler (0, 0x01cf, 0x01cf, slikshot_z80_control_r);
/*TODO*///		install_mem_write_handler(0, 0x01cf, 0x01cf, slikshot_z80_control_w);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	static public static InitDriverPtr init_sstrike = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		install_mem_read_handler (0, 0x1180, 0x1180, slikshot_z80_r);
/*TODO*///		install_mem_read_handler (0, 0x11cf, 0x11cf, slikshot_z80_control_r);
/*TODO*///		install_mem_write_handler(0, 0x11cf, 0x11cf, slikshot_z80_control_w);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	static public static InitDriverPtr init_rimrockn = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		/* additional input ports */
/*TODO*///		install_mem_read_handler (0, 0x0161, 0x0161, input_port_3_r);
/*TODO*///		install_mem_read_handler (0, 0x0162, 0x0162, input_port_4_r);
/*TODO*///		install_mem_read_handler (0, 0x0163, 0x0163, input_port_5_r);
/*TODO*///		install_mem_read_handler (0, 0x0164, 0x0164, input_port_6_r);
/*TODO*///		install_mem_read_handler (0, 0x0165, 0x0165, input_port_7_r);
/*TODO*///	
/*TODO*///		/* different banking mechanism (disable the old one) */
/*TODO*///		install_mem_write_handler(0, 0x01a0, 0x01a0, rimrockn_bank_w);
/*TODO*///		install_mem_write_handler(0, 0x01c0, 0x01df, itech8_blitter_w);
/*TODO*///	
/*TODO*///		/* VIA-based sound timing */
/*TODO*///		init_viasound();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Game drivers
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
	public static GameDriver driver_wfortune	   = new GameDriver("1989"	,"wfortune"	,"itech8.java"	,rom_wfortune,null	,machine_driver_tmshi2203	,input_ports_wfortune	,null	,ROT0	,	"GameTek", "Wheel Of Fortune" );
	public static GameDriver driver_wfortuna	   = new GameDriver("1989"	,"wfortuna"	,"itech8.java"	,rom_wfortuna,driver_wfortune	,machine_driver_tmshi2203	,input_ports_wfortune	,null	,ROT0	,	"GameTek", "Wheel Of Fortune (alternate)" );
	public static GameDriver driver_stratab	   = new GameDriver("1990"	,"stratab"	,"itech8.java"	,rom_stratab,null	,machine_driver_tmshi2203	,input_ports_stratab	,null	,ROT270	,	"Strata/Incredible Technologies", "Strata Bowling" );
/*TODO*///	public static GameDriver driver_sstrike	   = new GameDriver("1990"	,"sstrike"	,"itech8.java"	,rom_sstrike,null	,machine_driver_sstrike	,input_ports_sstrike	,init_sstrike	,ROT270	,	"Strata/Incredible Technologies", "Super Strike Bowling", GAME_NOT_WORKING )
	public static GameDriver driver_gtg	   = new GameDriver("1990"	,"gtg"	,"itech8.java"	,rom_gtg,null	,machine_driver_tmshi2203	,input_ports_gtg	,null	,ROT0	,	"Strata/Incredible Technologies", "Golden Tee Golf" );
/*TODO*///	public static GameDriver driver_slikshot	   = new GameDriver("1990"	,"slikshot"	,"itech8.java"	,rom_slikshot,null	,machine_driver_slikshot	,input_ports_slikshot	,init_slikshot	,ROT90	,	"Grand Products/Incredible Technologies", "Slick Shot (V2.2)" )
/*TODO*///	public static GameDriver driver_sliksh17	   = new GameDriver("1990"	,"sliksh17"	,"itech8.java"	,rom_sliksh17,driver_slikshot	,machine_driver_slikshot	,input_ports_slikshot	,init_slikshot	,ROT90	,	"Grand Products/Incredible Technologies", "Slick Shot (V1.7)" )
/*TODO*///	public static GameDriver driver_hstennis	   = new GameDriver("1990"	,"hstennis"	,"itech8.java"	,rom_hstennis,null	,machine_driver_hstennis	,input_ports_hstennis	,null	,ROT90	,	"Strata/Incredible Technologies", "Hot Shots Tennis" )
/*TODO*///	public static GameDriver driver_arlingtn	   = new GameDriver("1991"	,"arlingtn"	,"itech8.java"	,rom_arlingtn,null	,machine_driver_arlingtn	,input_ports_arlingtn	,null	,ROT0	,	"Strata/Incredible Technologies", "Arlington Horse Racing" )
/*TODO*///	public static GameDriver driver_peggle	   = new GameDriver("1991"	,"peggle"	,"itech8.java"	,rom_peggle,null	,machine_driver_peggle	,input_ports_peggle	,null	,ROT90	,	"Strata/Incredible Technologies", "Peggle (Joystick)" )
/*TODO*///	public static GameDriver driver_pegglet	   = new GameDriver("1991"	,"pegglet"	,"itech8.java"	,rom_pegglet,driver_peggle	,machine_driver_peggle	,input_ports_pegglet	,null	,ROT90	,	"Strata/Incredible Technologies", "Peggle (Trackball)" )
/*TODO*///	public static GameDriver driver_rimrockn	   = new GameDriver("1991"	,"rimrockn"	,"itech8.java"	,rom_rimrockn,null	,machine_driver_rimrockn	,input_ports_rimrockn	,init_rimrockn	,ROT0	,	"Strata/Incredible Technologies", "Rim Rockin' Basketball (V2.2)" )
/*TODO*///	public static GameDriver driver_rimrck20	   = new GameDriver("1991"	,"rimrck20"	,"itech8.java"	,rom_rimrck20,driver_rimrockn	,machine_driver_rimrockn	,input_ports_rimrockn	,init_rimrockn	,ROT0	,	"Strata/Incredible Technologies", "Rim Rockin' Basketball (V2.0)" )
/*TODO*///	public static GameDriver driver_rimrck16	   = new GameDriver("1991"	,"rimrck16"	,"itech8.java"	,rom_rimrck16,driver_rimrockn	,machine_driver_rimrockn	,input_ports_rimrockn	,init_rimrockn	,ROT0	,	"Strata/Incredible Technologies", "Rim Rockin' Basketball (V1.6)" )
/*TODO*///	public static GameDriver driver_rimrck12	   = new GameDriver("1991"	,"rimrck12"	,"itech8.java"	,rom_rimrck12,driver_rimrockn	,machine_driver_rimrockn	,input_ports_rimrockn	,init_rimrockn	,ROT0	,	"Strata/Incredible Technologies", "Rim Rockin' Basketball (V1.2)" )
/*TODO*///	public static GameDriver driver_ninclown	   = new GameDriver("1991"	,"ninclown"	,"itech8.java"	,rom_ninclown,null	,machine_driver_ninclown	,input_ports_ninclown	,init_viasound	,ROT0	,	"Strata/Incredible Technologies", "Ninja Clowns" )
/*TODO*///	public static GameDriver driver_gtg2	   = new GameDriver("1992"	,"gtg2"	,"itech8.java"	,rom_gtg2,null	,machine_driver_gtg2	,input_ports_gtg2	,init_viasound	,ROT0	,	"Strata/Incredible Technologies", "Golden Tee Golf II (Trackball, V2.2)" )
/*TODO*///	public static GameDriver driver_gtg2t	   = new GameDriver("1989"	,"gtg2t"	,"itech8.java"	,rom_gtg2t,driver_gtg2	,machine_driver_tmshi2203	,input_ports_gtg2t	,null	,ROT0	,	"Strata/Incredible Technologies", "Golden Tee Golf II (Trackball, V1.1)" )
/*TODO*///	public static GameDriver driver_gtg2j	   = new GameDriver("1991"	,"gtg2j"	,"itech8.java"	,rom_gtg2j,driver_gtg2	,machine_driver_tmslo2203	,input_ports_gtg	,null	,ROT0	,	"Strata/Incredible Technologies", "Golden Tee Golf II (Joystick, V1.0)" )
/*TODO*///	public static GameDriver driver_neckneck	   = new GameDriver("1992"	,"neckneck"	,"itech8.java"	,rom_neckneck,null	,machine_driver_neckneck	,input_ports_neckneck	,null	,ROT0	,	"Bundra Games/Incredible Technologies", "Neck-n-Neck" )
}
