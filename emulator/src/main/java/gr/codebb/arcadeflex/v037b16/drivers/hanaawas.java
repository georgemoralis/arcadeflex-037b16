/**
 * ported to v0.37b16
 * ported to v0.37b7
 */
package gr.codebb.arcadeflex.v037b16.drivers;

//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b16.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
//sound imports
import static gr.codebb.arcadeflex.v037b16.sound.ay8910.*;
import static gr.codebb.arcadeflex.v037b16.sound.ay8910H.*;
//vidhrdw imports
import static gr.codebb.arcadeflex.v037b16.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b16.vidhrdw.hanaawas.*;
//to be organized
import static arcadeflex037b16.fucPtr.*;
import static mame056.inptport.*;
import static mame056.inptportH.*;
import static mame056.sndintrfH.*;

public class hanaawas {

    public static ReadHandlerPtr hanaawas_input_port_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int i, ordinal = 0;
            char buttons;

            /* as to which player's jeys are read are probably selected via port 0, but
		   it's not obvious to me how */
            buttons = (char) readinputport(2);

            /* map button pressed into 1-10 range */
            for (i = 0; i < 10; i++) {
                if ((buttons & (1 << i)) != 0) {
                    ordinal = (i + 1);
                    break;
                }
            }

            return (input_port_0_r.handler(0) & 0xf0) | ordinal;
        }
    };

    public static Memory_ReadAddress readmem[] = {
        new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_ReadAddress(0x0000, 0x2fff, MRA_ROM),
        new Memory_ReadAddress(0x4000, 0x4fff, MRA_ROM),
        new Memory_ReadAddress(0x6000, 0x6fff, MRA_ROM),
        new Memory_ReadAddress(0x8000, 0x8bff, MRA_RAM),
        new Memory_ReadAddress(MEMPORT_MARKER, 0)
    };
    public static Memory_WriteAddress writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_WriteAddress(0x0000, 0x2fff, MWA_ROM),
        new Memory_WriteAddress(0x4000, 0x4fff, MWA_ROM),
        new Memory_WriteAddress(0x6000, 0x6fff, MWA_ROM),
        new Memory_WriteAddress(0x8000, 0x83ff, videoram_w, videoram, videoram_size),
        new Memory_WriteAddress(0x8400, 0x87ff, hanaawas_colorram_w, colorram),
        new Memory_WriteAddress(0x8800, 0x8bff, MWA_RAM),
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };
    public static IO_ReadPort readport[] = {
        new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8), new IO_ReadPort(0x00, 0x00, hanaawas_input_port_0_r),
        new IO_ReadPort(0x10, 0x10, AY8910_read_port_0_r),
        new IO_ReadPort(MEMPORT_MARKER, 0)
    };
    public static IO_WritePort writeport[] = {
        new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8), new IO_WritePort(0x10, 0x10, AY8910_control_port_0_w),
        new IO_WritePort(0x11, 0x11, AY8910_write_port_0_w),
        new IO_WritePort(MEMPORT_MARKER, 0)
    };

    static InputPortPtr input_ports_hanaawas = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x0f, IP_ACTIVE_HIGH, IPT_SPECIAL);
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_HIGH, IPT_COIN1, 1);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x06, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x02, "1.5");
            PORT_DIPSETTING(0x04, "2");
            PORT_DIPSETTING(0x06, "2.5");
            PORT_DIPNAME(0x18, 0x10, "Key Time-Out");
            PORT_DIPSETTING(0x00, "15 sec");
            PORT_DIPSETTING(0x08, "20 sec");
            PORT_DIPSETTING(0x10, "25 sec");
            PORT_DIPSETTING(0x18, "30 sec");
            PORT_DIPNAME(0x20, 0x00, "Time Per Coin");
            PORT_DIPSETTING(0x20, "50");
            PORT_DIPSETTING(0x00, "100");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x40, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_2C"));

            /* fake port.  The button depressed gets converted to an integer in the 1-10 range */
            PORT_START();
            /* IN2 */
            PORT_BIT(0x001, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x001, IP_ACTIVE_HIGH, IPT_START1);
            /* same as button 1 */
            PORT_BIT(0x002, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x002, IP_ACTIVE_HIGH, IPT_START2);
            /* same as button 2 */
            PORT_BIT(0x004, IP_ACTIVE_HIGH, IPT_BUTTON3);
            PORT_BIT(0x008, IP_ACTIVE_HIGH, IPT_BUTTON4);
            PORT_BIT(0x010, IP_ACTIVE_HIGH, IPT_BUTTON5);
            PORT_BIT(0x020, IP_ACTIVE_HIGH, IPT_BUTTON6);
            PORT_BIT(0x040, IP_ACTIVE_HIGH, IPT_BUTTON7);
            PORT_BIT(0x080, IP_ACTIVE_HIGH, IPT_BUTTON8);
            PORT_BIT(0x100, IP_ACTIVE_HIGH, IPT_BUTTON9);
            PORT_BIT(0x200, IP_ACTIVE_HIGH, IPT_BUTTON10);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout_1bpp = new GfxLayout(
            8, 8, /* 8*8 chars */
            512, /* 512 characters */
            3, /* 3 bits per pixel */
            new int[]{0x2000 * 8 + 4, 0x2000 * 8 + 4, 0x2000 * 8 + 4}, /* bitplanes */
            new int[]{8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 0, 1, 2, 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 16 /* every char takes 16 consecutive bytes */
    );

    static GfxLayout charlayout_3bpp = new GfxLayout(
            8, 8, /* 8*8 chars */
            512, /* 512 characters */
            3, /* 3 bits per pixel */
            new int[]{0x2000 * 8, 0, 4}, /* bitplanes */
            new int[]{8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 0, 1, 2, 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 16 /* every char takes 16 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout_1bpp, 0, 32),
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout_3bpp, 0, 32),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            1, /* 1 chip */
            18432000 / 12, /* 1.5 MHz ? */
            new int[]{50},
            new ReadHandlerPtr[]{input_port_1_r},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{hanaawas_portB_w}
    );

    static MachineDriver machine_driver_hanaawas = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz ??? */
                        readmem, writemem, readport, writeport,
                        interrupt, 1
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(1 * 8, 31 * 8 - 1, 0 * 8, 32 * 8 - 1),
            gfxdecodeinfo,
            16, 32 * 8,
            hanaawas_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            hanaawas_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                )
            }
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadPtr rom_hanaawas = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);
            /* 64k for code */
            ROM_LOAD("1.1e", 0x0000, 0x2000, 0x618dc1e3);
            ROM_LOAD("2.3e", 0x2000, 0x1000, 0x5091b67f);
            ROM_LOAD("3.4e", 0x4000, 0x1000, 0xdcb65067);
            ROM_LOAD("4.6e", 0x6000, 0x1000, 0x24bee0dc);
            ROM_REGION(0x4000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("5.9a", 0x0000, 0x1000, 0x304ae219);
            ROM_LOAD("6.10a", 0x1000, 0x1000, 0x765a4e5f);
            ROM_LOAD("7.12a", 0x2000, 0x1000, 0x5245af2d);
            ROM_LOAD("8.13a", 0x3000, 0x1000, 0x3356ddce);
            ROM_REGION(0x0220, REGION_PROMS, 0);
            ROM_LOAD("13j.bpr", 0x0000, 0x0020, 0x99300d85);/* color PROM */
            ROM_LOAD("2a.bpr", 0x0020, 0x0100, 0xe26f21a2);/* lookup table */
            ROM_LOAD("6g.bpr", 0x0120, 0x0100, 0x4d94fed5);/* I don't know what this is */
            ROM_END();
        }
    };

    public static GameDriver driver_hanaawas = new GameDriver("1982", "hanaawas", "hanaawas.java", rom_hanaawas, null, machine_driver_hanaawas, input_ports_hanaawas, null, ROT0, "Seta", "Hana Awase (Flower Matching)");
}
