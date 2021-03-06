/*
 * ported to v0.37b16
 * ported to v0.37b7
 */
package gr.codebb.arcadeflex.v037b16.drivers;

//generic functions
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//cpu imports
import static gr.codebb.arcadeflex.v037b16.cpu.m6800.hd63701.*;
import static gr.codebb.arcadeflex.v037b16.cpu.m6800.m6800Η.*;
import static gr.codebb.arcadeflex.v037b16.cpu.m6809.m6809H.*;
//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b16.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b16.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b16.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b16.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
//vidhrdw imports
import static gr.codebb.arcadeflex.v037b16.vidhrdw.skykid.*;
//to be organized
import common.ptr.UBytePtr;
import static mame056.sound.namco.namco_soundregs;
import static mame056.sound.namco.namco_wavedata;
import static mame056.sound.namco.namcos1_sound_w;
import static mame056.sound.namco.namcos1_wavedata_r;
import static mame056.sound.namco.namcos1_wavedata_w;
import mame056.sound.namcoH.namco_interface;

public class skykid {

    static UBytePtr sharedram = new UBytePtr();

    static int irq_disabled = 1;
    static int inputport_selected;

    public static InterruptPtr skykid_interrupt = new InterruptPtr() {
        public int handler() {
            if (irq_disabled == 0) {
                return M6809_INT_IRQ;
            } else {
                return ignore_interrupt.handler();
            }
        }
    };

    public static WriteHandlerPtr skykid_irq_ctrl_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            irq_disabled = offset;
        }
    };

    public static WriteHandlerPtr inputport_select_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0xe0) == 0x60) {
                inputport_selected = data & 0x07;
            } else if ((data & 0xe0) == 0xc0) {
                coin_lockout_global_w(~data & 1);
                coin_counter_w(0, data & 2);
                coin_counter_w(1, data & 4);
            }
        }
    };

    public static int reverse_bitstrm(int data) {
        return ((data & 0x01) << 4) | ((data & 0x02) << 2) | (data & 0x04)
                | ((data & 0x08) >> 2) | ((data & 0x10) >> 4);
    }

    public static ReadHandlerPtr inputport_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int data = 0;

            switch (inputport_selected) {
                case 0x00:
                    /* DSW B (bits 0-4) */
                    data = ~(reverse_bitstrm(readinputport(1) & 0x1f));
                    break;
                case 0x01:
                    /* DSW B (bits 5-7), DSW A (bits 0-1) */
                    data = ~(reverse_bitstrm((((readinputport(1) & 0xe0) >> 5) | ((readinputport(0) & 0x03) << 3))));
                    break;
                case 0x02:
                    /* DSW A (bits 2-6) */
                    data = ~(reverse_bitstrm(((readinputport(0) & 0x7c) >> 2)));
                    break;
                case 0x03:
                    /* DSW A (bit 7), DSW C (bits 0-3) */
                    data = ~(reverse_bitstrm((((readinputport(0) & 0x80) >> 7) | ((readinputport(2) & 0x0f) << 1))));
                    break;
                case 0x04:
                    /* coins, start */
                    data = ~(readinputport(3));
                    break;
                case 0x05:
                    /* 2P controls */
                    data = ~(readinputport(5));
                    break;
                case 0x06:
                    /* 1P controls */
                    data = ~(readinputport(4));
                    break;
                default:
                    data = 0xff;
            }

            return data;
        }
    };

    public static WriteHandlerPtr skykid_led_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*TODO*///		set_led_status(0,data & 0x08);
/*TODO*///		set_led_status(1,data & 0x10);
        }
    };

    public static WriteHandlerPtr skykid_halt_mcu_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 0) {
                cpu_set_reset_line(1, PULSE_LINE);
                cpu_set_halt_line(1, CLEAR_LINE);
            } else {
                cpu_set_halt_line(1, ASSERT_LINE);
            }
        }
    };

    public static ReadHandlerPtr skykid_sharedram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return sharedram.read(offset);
        }
    };
    public static WriteHandlerPtr skykid_sharedram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            sharedram.write(offset, data);
        }
    };

    public static WriteHandlerPtr skykid_bankswitch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bankaddress;
            UBytePtr RAM = new UBytePtr(memory_region(REGION_CPU1));

            bankaddress = 0x10000 + (offset != 0 ? 0 : 0x2000);
            cpu_setbank(1, new UBytePtr(RAM, bankaddress));
        }
    };

    public static Memory_ReadAddress skykid_readmem[] = {
        new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_ReadAddress(0x0000, 0x1fff, MRA_BANK1), /* banked ROM */
        new Memory_ReadAddress(0x2000, 0x2fff, skykid_videoram_r), /* Video RAM (background) */
        new Memory_ReadAddress(0x4000, 0x47ff, MRA_RAM), /* video RAM (text layer) */
        new Memory_ReadAddress(0x4800, 0x5fff, MRA_RAM), /* RAM + Sprite RAM */
        new Memory_ReadAddress(0x6800, 0x68ff, namcos1_wavedata_r), /* PSG device, shared RAM */
        new Memory_ReadAddress(0x6800, 0x6bff, skykid_sharedram_r), /* shared RAM with the MCU */
        new Memory_ReadAddress(0x7800, 0x7800, watchdog_reset_r), /* watchdog reset */
        new Memory_ReadAddress(0x8000, 0xffff, MRA_ROM), /* ROM */
        new Memory_ReadAddress(MEMPORT_MARKER, 0)
    };
    public static Memory_WriteAddress skykid_writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_WriteAddress(0x0000, 0x1fff, MWA_ROM), /* banked ROM */
        new Memory_WriteAddress(0x2000, 0x2fff, skykid_videoram_w, skykid_videoram),/* Video RAM (background) */
        new Memory_WriteAddress(0x4000, 0x47ff, MWA_RAM, skykid_textram),/* video RAM (text layer) */
        new Memory_WriteAddress(0x4800, 0x5fff, MWA_RAM), /* RAM + Sprite RAM */
        new Memory_WriteAddress(0x6000, 0x60ff, skykid_scroll_y_w), /* Y scroll register map */
        new Memory_WriteAddress(0x6200, 0x63ff, skykid_scroll_x_w), /* X scroll register map */
        new Memory_WriteAddress(0x6800, 0x68ff, namcos1_wavedata_w, namco_wavedata),/* PSG device, shared RAM */
        new Memory_WriteAddress(0x6800, 0x6bff, skykid_sharedram_w, sharedram), /* shared RAM with the MCU */
        new Memory_WriteAddress(0x7000, 0x7800, skykid_irq_ctrl_w), /* IRQ control */
        new Memory_WriteAddress(0x8000, 0x8800, skykid_halt_mcu_w), /* MCU control */
        new Memory_WriteAddress(0x9000, 0x9800, skykid_bankswitch_w), /* Bankswitch control */
        new Memory_WriteAddress(0xa000, 0xa001, skykid_flipscreen_w), /* flip screen */
        new Memory_WriteAddress(0x8000, 0xffff, MWA_ROM), /* ROM */
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };
    public static Memory_ReadAddress mcu_readmem[] = {
        new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_ReadAddress(0x0000, 0x001f, hd63701_internal_registers_r),/* internal registers */
        new Memory_ReadAddress(0x0080, 0x00ff, MRA_RAM), /* built in RAM */
        new Memory_ReadAddress(0x1000, 0x10ff, namcos1_wavedata_r), /* PSG device, shared RAM */
        new Memory_ReadAddress(0x1100, 0x113f, MRA_RAM), /* PSG device */
        new Memory_ReadAddress(0x1000, 0x13ff, skykid_sharedram_r), /* shared RAM with the 6809 */
        new Memory_ReadAddress(0x8000, 0xbfff, MRA_ROM), /* MCU external ROM */
        new Memory_ReadAddress(0xc000, 0xc800, MRA_RAM), /* RAM */
        new Memory_ReadAddress(0xf000, 0xffff, MRA_ROM), /* MCU internal ROM */
        new Memory_ReadAddress(MEMPORT_MARKER, 0)
    };
    public static Memory_WriteAddress mcu_writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_WriteAddress(0x0000, 0x001f, hd63701_internal_registers_w),/* internal registers */
        new Memory_WriteAddress(0x0080, 0x00ff, MWA_RAM), /* built in RAM */
        new Memory_WriteAddress(0x1000, 0x10ff, namcos1_wavedata_w), /* PSG device, shared RAM */
        new Memory_WriteAddress(0x1100, 0x113f, namcos1_sound_w, namco_soundregs),/* PSG device */
        new Memory_WriteAddress(0x1000, 0x13ff, skykid_sharedram_w), /* shared RAM with the 6809 */
        new Memory_WriteAddress(0x2000, 0x2000, MWA_NOP), /* ??? */
        new Memory_WriteAddress(0x4000, 0x4000, MWA_NOP), /* ??? */
        new Memory_WriteAddress(0x6000, 0x6000, MWA_NOP), /* ??? */
        new Memory_WriteAddress(0x8000, 0xbfff, MWA_ROM), /* MCU external ROM */
        new Memory_WriteAddress(0xc000, 0xc800, MWA_RAM), /* RAM */
        new Memory_WriteAddress(0xf000, 0xffff, MWA_ROM), /* MCU internal ROM */
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };

    public static ReadHandlerPtr readFF = new ReadHandlerPtr() {
        public int handler(int offset) {
            return 0xff;
        }
    };

    public static IO_ReadPort mcu_readport[] = {
        new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8), new IO_ReadPort(HD63701_PORT1, HD63701_PORT1, inputport_r), /* input ports read */
        new IO_ReadPort(HD63701_PORT2, HD63701_PORT2, readFF), /* leds won't work otherwise */
        new IO_ReadPort(MEMPORT_MARKER, 0)
    };
    public static IO_WritePort mcu_writeport[] = {
        new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8), new IO_WritePort(HD63701_PORT1, HD63701_PORT1, inputport_select_w), /* input port select */
        new IO_WritePort(HD63701_PORT2, HD63701_PORT2, skykid_led_w), /* lamps */
        new IO_WritePort(MEMPORT_MARKER, 0)
    };

    static InputPortPtr input_ports_skykid = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* DSW A */
            PORT_SERVICE(0x01, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x06, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x06, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, "Round Skip");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, "Freeze screen");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0xc0, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_2C"));

            PORT_START();
            /* DSW B */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "2");
            PORT_DIPSETTING(0x02, "1");
            PORT_DIPSETTING(0x03, "5");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "30k, 90k");
            PORT_DIPSETTING(0x04, "20k, 80k");
            PORT_DIPSETTING(0x08, "30k every 90k");
            PORT_DIPSETTING(0x0c, "20k every 80k");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));

            PORT_START();
            /* DSW C */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* IN 0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_SERVICE1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* IN 1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* IN 2 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_drgnbstr = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* DSW A */
            PORT_SERVICE(0x01, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x06, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x06, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, "Round Skip");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, "Freeze screen");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0xc0, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_2C"));

            PORT_START();
            /* DSW B */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* DSW C */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x04, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* IN 0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_SERVICE1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* IN 1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* IN 2 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout text_layout = new GfxLayout(
            8, 8, /* 8*8 characters */
            512, /* 512 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the bitplanes are packed in the same byte */
            new int[]{8 * 8, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 0, 1, 2, 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            16 * 8 /* every char takes 16 consecutive bytes */
    );

    static GfxLayout tile_layout = new GfxLayout(
            8, 8, /* 8*8 characters */
            512, /* 512 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 4},
            new int[]{0, 1, 2, 3, 8 + 0, 8 + 1, 8 + 2, 8 + 3},
            new int[]{0 * 8, 2 * 8, 4 * 8, 6 * 8, 8 * 8, 10 * 8, 12 * 8, 14 * 8},
            16 * 8 /* every char takes 16 consecutive bytes */
    );

    static GfxLayout sprite_layout1 = new GfxLayout(
            16, 16, /* 16*16 sprites */
            128, /* 128 sprites */
            3, /* 3 bits per pixel */
            new int[]{0x4000 * 8 + 4, 0, 4},
            new int[]{0, 1, 2, 3, 8 * 8, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 24 * 8 + 0, 24 * 8 + 1, 24 * 8 + 2, 24 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8},
            64 * 8 /* every sprite takes 64 bytes */
    );

    static GfxLayout sprite_layout2 = new GfxLayout(
            16, 16, /* 16*16 sprites */
            128, /* 128 sprites */
            3, /* 3 bits per pixel */
            new int[]{0x4000 * 8, 0x2000 * 8, 0x2000 * 8 + 4},
            new int[]{0, 1, 2, 3, 8 * 8, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 24 * 8 + 0, 24 * 8 + 1, 24 * 8 + 2, 24 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8},
            64 * 8 /* every sprite takes 64 bytes */
    );

    static GfxLayout sprite_layout3 = new GfxLayout(
            16, 16, /* 16*16 sprites */
            128, /* 128 sprites */
            3, /* 3 bits per pixel */
            new int[]{0x8000 * 8, 0x6000 * 8, 0x6000 * 8 + 4},
            new int[]{0, 1, 2, 3, 8 * 8, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 24 * 8 + 0, 24 * 8 + 1, 24 * 8 + 2, 24 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8},
            64 * 8 /* every sprite takes 64 bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, text_layout, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0, tile_layout, 64 * 4, 128),
                new GfxDecodeInfo(REGION_GFX3, 0, sprite_layout1, 64 * 4 + 128 * 4, 64),
                new GfxDecodeInfo(REGION_GFX3, 0, sprite_layout2, 64 * 4 + 128 * 4, 64),
                new GfxDecodeInfo(REGION_GFX3, 0, sprite_layout3, 64 * 4 + 128 * 4, 64),
                new GfxDecodeInfo(-1)
            };

    static namco_interface namco_interface = new namco_interface(
            49152000 / 2048, /* 24000 Hz */
            8, /* number of voices */
            100, /* playback volume */
            -1, /* memory region */
            0 /* stereo */
    );

    static MachineDriver machine_driver_skykid = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        49152000 / 32, /* ??? */
                        skykid_readmem, skykid_writemem, null, null,
                        skykid_interrupt, 1
                ),
                new MachineCPU(
                        CPU_HD63701, /* or compatible 6808 with extra instructions */
                        49152000 / 32, /* ??? */
                        mcu_readmem, mcu_writemem, mcu_readport, mcu_writeport,
                        interrupt, 1
                )
            },
            60.606060, DEFAULT_REAL_60HZ_VBLANK_DURATION,
            100, /* we need heavy synch */
            null,
            /* video hardware */
            36 * 8, 28 * 8, new rectangle(0 * 8, 36 * 8 - 1, 0 * 8, 28 * 8 - 1),
            gfxdecodeinfo,
            256, 64 * 4 + 128 * 4 + 64 * 8,
            skykid_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            skykid_vh_start,
            null,
            skykid_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_NAMCO,
                        namco_interface
                )
            }
    );

    static RomLoadPtr rom_skykid = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x14000, REGION_CPU1, 0);/* 6809 code */
            ROM_LOAD("sk2-6c.bin", 0x08000, 0x4000, 0xea8a5822);
            ROM_LOAD("sk1-6b.bin", 0x0c000, 0x4000, 0x7abe6c6c);
            ROM_LOAD("sk3-6d.bin", 0x10000, 0x4000, 0x314b8765);/* banked ROM */

            ROM_REGION(0x10000, REGION_CPU2, 0);/* MCU code */
            ROM_LOAD("sk4-3c.bin", 0x8000, 0x2000, 0xa460d0e0);/* subprogram for the MCU */
            ROM_LOAD("sk1-mcu.bin", 0xf000, 0x1000, 0x6ef08fb3);/* MCU internal code */
 /* Using Pacland code (probably similar) */

            ROM_REGION(0x02000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("sk6-6l.bin", 0x00000, 0x2000, 0x58b731b9);/* chars */

            ROM_REGION(0x02000, REGION_GFX2, ROMREGION_DISPOSE);
            ROM_LOAD("sk5-7e.bin", 0x00000, 0x2000, 0xc33a498e);
            ROM_REGION(0x0a000, REGION_GFX3, ROMREGION_DISPOSE);
            ROM_LOAD("sk9-10n.bin", 0x00000, 0x4000, 0x44bb7375);/* sprites */
            ROM_LOAD("sk7-10m.bin", 0x04000, 0x4000, 0x3454671d);
            /* empty space to decode the sprites as 3bpp */

            ROM_REGION(0x0700, REGION_PROMS, 0);
            ROM_LOAD("sk1-2n.bin", 0x0000, 0x0100, 0x0218e726);/* red component */
            ROM_LOAD("sk2-2p.bin", 0x0100, 0x0100, 0xfc0d5b85);/* green component */
            ROM_LOAD("sk3-2r.bin", 0x0200, 0x0100, 0xd06b620b);/* blue component */
            ROM_LOAD("sk-5n.bin", 0x0300, 0x0200, 0xc697ac72);/* tiles lookup table */
            ROM_LOAD("sk-6n.bin", 0x0500, 0x0200, 0x161514a4);/* sprites lookup table */
            ROM_END();
        }
    };

    static RomLoadPtr rom_skykidb = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x14000, REGION_CPU1, 0);/* 6809 code */
            ROM_LOAD("sk2-6c.bin", 0x08000, 0x4000, 0xea8a5822);
            ROM_LOAD("sk1", 0x0c000, 0x4000, 0x070a49d4);
            ROM_LOAD("sk3-6d.bin", 0x10000, 0x4000, 0x314b8765);/* banked ROM */

            ROM_REGION(0x10000, REGION_CPU2, 0);/* MCU code */
            ROM_LOAD("sk4-3c.bin", 0x8000, 0x2000, 0xa460d0e0);/* subprogram for the MCU */
            ROM_LOAD("sk1-mcu.bin", 0xf000, 0x1000, 0x6ef08fb3);/* MCU internal code */
 /* Using Pacland code (probably similar) */

            ROM_REGION(0x02000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("sk6-6l.bin", 0x00000, 0x2000, 0x58b731b9);/* chars */

            ROM_REGION(0x02000, REGION_GFX2, ROMREGION_DISPOSE);
            ROM_LOAD("sk5-7e.bin", 0x00000, 0x2000, 0xc33a498e);
            ROM_REGION(0x0a000, REGION_GFX3, ROMREGION_DISPOSE);
            ROM_LOAD("sk9-10n.bin", 0x00000, 0x4000, 0x44bb7375);/* sprites */
            ROM_LOAD("sk7-10m.bin", 0x04000, 0x4000, 0x3454671d);
            /* empty space to decode the sprites as 3bpp */

            ROM_REGION(0x0700, REGION_PROMS, 0);
            ROM_LOAD("sk1-2n.bin", 0x0000, 0x0100, 0x0218e726);/* red component */
            ROM_LOAD("sk2-2p.bin", 0x0100, 0x0100, 0xfc0d5b85);/* green component */
            ROM_LOAD("sk3-2r.bin", 0x0200, 0x0100, 0xd06b620b);/* blue component */
            ROM_LOAD("sk-5n.bin", 0x0300, 0x0200, 0xc697ac72);/* tiles lookup table */
            ROM_LOAD("sk-6n.bin", 0x0500, 0x0200, 0x161514a4);/* sprites lookup table */
            ROM_END();
        }
    };

    static RomLoadPtr rom_drgnbstr = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x14000, REGION_CPU1, 0);/* 6809 code */
            ROM_LOAD("6c.bin", 0x08000, 0x04000, 0x0f11cd17);
            ROM_LOAD("6b.bin", 0x0c000, 0x04000, 0x1c7c1821);
            ROM_LOAD("6d.bin", 0x10000, 0x04000, 0x6da169ae);/* banked ROM */

            ROM_REGION(0x10000, REGION_CPU2, 0);/* MCU code */
            ROM_LOAD("3c.bin", 0x8000, 0x02000, 0x8a0b1fc1);/* subprogram for the MCU */
            ROM_LOAD("pl1-mcu.bin", 0xf000, 0x01000, 0x6ef08fb3);/* The MCU internal code is missing */
 /* Using Pacland code (probably similar) */

            ROM_REGION(0x02000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("6l.bin", 0x00000, 0x2000, 0xc080b66c);/* tiles */

            ROM_REGION(0x02000, REGION_GFX2, ROMREGION_DISPOSE);
            ROM_LOAD("7e.bin", 0x00000, 0x2000, 0x28129aed);
            ROM_REGION(0x0a000, REGION_GFX3, ROMREGION_DISPOSE);
            ROM_LOAD("10n.bin", 0x00000, 0x4000, 0x11942c61);/* sprites */
            ROM_LOAD("10m.bin", 0x04000, 0x4000, 0xcc130fe2);
            /* empty space to decode the sprites as 3bpp */

            ROM_REGION(0x0700, REGION_PROMS, 0);
            ROM_LOAD("2n.bin", 0x00000, 0x0100, 0x3f8cce97);/* red component */
            ROM_LOAD("2p.bin", 0x00100, 0x0100, 0xafe32436);/* green component */
            ROM_LOAD("2r.bin", 0x00200, 0x0100, 0xc95ff576);/* blue component */
            ROM_LOAD("db1-4.5n", 0x00300, 0x0200, 0xb2180c21);/* tiles lookup table */
            ROM_LOAD("db1-5.6n", 0x00500, 0x0200, 0x5e2b3f74);/* sprites lookup table */
            ROM_END();
        }
    };

    public static GameDriver driver_skykid = new GameDriver("1985", "skykid", "skykid.java", rom_skykid, null, machine_driver_skykid, input_ports_skykid, null, ROT0, "Namco", "Sky Kid (set 1)");
    public static GameDriver driver_skykidb = new GameDriver("1985", "skykidb", "skykid.java", rom_skykidb, driver_skykid, machine_driver_skykid, input_ports_skykid, null, ROT0, "Namco", "Sky Kid (set 2)");
    public static GameDriver driver_drgnbstr = new GameDriver("1984", "drgnbstr", "skykid.java", rom_drgnbstr, null, machine_driver_skykid, input_ports_drgnbstr, null, ROT0, "Namco", "Dragon Buster");
}
