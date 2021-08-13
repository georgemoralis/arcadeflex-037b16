/*
 * ported to v0.37b16
 */
package gr.codebb.arcadeflex.v037b16.drivers;

//generic imports
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b16.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b16.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b16.mame.inptportH.*;
//vidhrdw imports
import static gr.codebb.arcadeflex.v037b16.vidhrdw.dotrikun.*;
import static gr.codebb.arcadeflex.v037b16.vidhrdw.generic.*;

public class dotrikun {

    public static Memory_ReadAddress readmem[] = {
        new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_ReadAddress(0x0000, 0x3fff, MRA_ROM),
        new Memory_ReadAddress(0x8000, 0x87ff, MRA_RAM),
        new Memory_ReadAddress(MEMPORT_MARKER, 0)
    };
    public static Memory_WriteAddress writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_WriteAddress(0x0000, 0x3fff, MWA_ROM),
        new Memory_WriteAddress(0x8000, 0x87ff, dotrikun_videoram_w, videoram, videoram_size),
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };
    public static IO_ReadPort readport[] = {
        new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8), new IO_ReadPort(0x00, 0x00, input_port_0_r),
        new IO_ReadPort(MEMPORT_MARKER, 0)
    };
    public static IO_WritePort writeport[] = {
        new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8), new IO_WritePort(0x00, 0x00, dotrikun_color_w),
        new IO_WritePort(MEMPORT_MARKER, 0)
    };

    static InputPortPtr input_ports_dotrikun = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_dotrikun = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* 4 MHz */
                        readmem, writemem, readport, writeport,
                        interrupt, 1
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            256, 256, new rectangle(0, 256 - 1, 0, 192 - 1),
            null,
            2, 0,
            null,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            null,
            null,
            dotrikun_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0, null
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadPtr rom_dotrikun = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("14479a.mpr", 0x0000, 0x4000, 0xb77a50db);
            ROM_END();
        }
    };

    static RomLoadPtr rom_dotriku2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("14479.mpr", 0x0000, 0x4000, 0xa6aa7fa5);
            ROM_END();
        }
    };

    public static GameDriver driver_dotrikun = new GameDriver("1990", "dotrikun", "dotrikun.java", rom_dotrikun, null, machine_driver_dotrikun, input_ports_dotrikun, null, ROT0, "Sega", "Dottori Kun (new version)");
    public static GameDriver driver_dotriku2 = new GameDriver("1990", "dotriku2", "dotrikun.java", rom_dotriku2, driver_dotrikun, machine_driver_dotrikun, input_ports_dotrikun, null, ROT0, "Sega", "Dottori Kun (old version)");
}