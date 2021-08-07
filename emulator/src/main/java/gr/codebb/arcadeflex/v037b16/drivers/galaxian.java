/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b16.drivers;

//generic imports
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//cpu imports
import static gr.codebb.arcadeflex.v037b16.cpu.z80.z80H.*;
//drivers imports
import static gr.codebb.arcadeflex.v037b16.drivers.pacman.*;
import static gr.codebb.arcadeflex.v037b16.drivers.scramble.*;
//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b16.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
import static gr.codebb.arcadeflex.v037b16.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b16.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b16.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.memory.*;
import static gr.codebb.arcadeflex.v037b16.mame.sndintrf.*;
//machine imports
import static gr.codebb.arcadeflex.v037b16.machine.scramble.*;
//sndhrdw imports
import static gr.codebb.arcadeflex.v037b16.sndhrdw.galaxian.*;
//sound imports
import static gr.codebb.arcadeflex.v037b16.sound.ay8910.*;
import static gr.codebb.arcadeflex.v037b16.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v037b16.sound.dac.*;
import static gr.codebb.arcadeflex.v037b16.sound.dacH.*;
//vidhrdw imports
import static gr.codebb.arcadeflex.v037b16.vidhrdw.galaxian.*;
//to be organized
import static mame037b16.mame.*;
import static arcadeflex036.osdepend.logerror;
import static common.libc.cstdlib.rand;
import common.ptr.UBytePtr;

public class galaxian {

    public static WriteHandlerPtr galaxian_coin_lockout_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            coin_lockout_global_w(~data & 1);
        }
    };

    public static WriteHandlerPtr galaxian_leds_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            set_led_status(offset, data & 1);
        }
    };

    static int kingball_speech_dip;

    /* Hack? If $b003 is high, we'll check our "fake" speech dipswitch (marked as SLAM) */
    public static ReadHandlerPtr kingball_IN0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (kingball_speech_dip != 0) {
                return (readinputport(0) & ~0x40) | ((readinputport(3) & 0x01) << 6);
            } else {
                return readinputport(0);
            }
        }
    };

    public static ReadHandlerPtr kingball_IN1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* bit 5 is the NOISE line from the sound circuit.  The code just verifies
		   that it's working, doesn't actually use return value, so we can just use
		   rand() */

            return (readinputport(1) & ~0x20) | (rand() & 0x20);
        }
    };

    public static WriteHandlerPtr kingball_speech_dip_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            kingball_speech_dip = data;
        }
    };

    static int kingball_sound;

    public static WriteHandlerPtr kingball_sound1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            kingball_sound = (kingball_sound & ~0x01) | data;
        }
    };

    public static WriteHandlerPtr kingball_sound2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            kingball_sound = (kingball_sound & ~0x02) | (data << 1);
            soundlatch_w.handler(0, kingball_sound | 0xf0);
        }
    };

    public static ReadHandlerPtr jumpbug_protection_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (offset) {
                case 0x0114:
                    return 0x4f;
                case 0x0118:
                    return 0xd3;
                case 0x0214:
                    return 0xcf;
                case 0x0235:
                    return 0x02;
                case 0x0311:
                    return 0x00;
                /* not checked */
                default:
                    logerror("Unknown protection read. Offset: %04X  PC=%04X\n", 0xb000 + offset, cpu_get_pc());
            }

            return 0;
        }
    };

    public static ReadHandlerPtr checkmaj_protection_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (cpu_get_pc()) {
                case 0x0f15:
                    return 0xf5;
                case 0x0f8f:
                    return 0x7c;
                case 0x10b3:
                    return 0x7c;
                case 0x10e0:
                    return 0x00;
                case 0x10f1:
                    return 0xaa;
                case 0x1402:
                    return 0xaa;
                default:
                    logerror("Unknown protection read. PC=%04X\n", cpu_get_pc());
            }

            return 0;
        }
    };

    /* Send sound data to the sound cpu and cause an nmi */
    public static WriteHandlerPtr checkman_sound_command_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(0, data);
            cpu_cause_interrupt(1, Z80_NMI_INT);
        }
    };

    public static WriteHandlerPtr galaxian_coin_counter_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            coin_counter_w(offset, data & 0x01);
        }
    };

    public static Memory_ReadAddress galaxian_readmem[] = {
        new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_ReadAddress(0x0000, 0x3fff, MRA_ROM),
        new Memory_ReadAddress(0x4000, 0x47ff, MRA_RAM),
        new Memory_ReadAddress(0x5000, 0x53ff, MRA_RAM),
        new Memory_ReadAddress(0x5400, 0x57ff, galaxian_videoram_r),
        new Memory_ReadAddress(0x5800, 0x58ff, MRA_RAM),
        new Memory_ReadAddress(0x6000, 0x6000, input_port_0_r),
        new Memory_ReadAddress(0x6800, 0x6800, input_port_1_r),
        new Memory_ReadAddress(0x7000, 0x7000, input_port_2_r),
        new Memory_ReadAddress(0x7800, 0x78ff, watchdog_reset_r),
        new Memory_ReadAddress(MEMPORT_MARKER, 0)
    };
    public static Memory_WriteAddress galaxian_writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_WriteAddress(0x0000, 0x3fff, MWA_ROM),
        new Memory_WriteAddress(0x4000, 0x47ff, MWA_RAM),
        new Memory_WriteAddress(0x5000, 0x53ff, MWA_RAM, galaxian_videoram),
        new Memory_WriteAddress(0x5800, 0x583f, MWA_RAM, galaxian_attributesram),
        new Memory_WriteAddress(0x5840, 0x585f, MWA_RAM, galaxian_spriteram, galaxian_spriteram_size),
        new Memory_WriteAddress(0x5860, 0x587f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
        new Memory_WriteAddress(0x5880, 0x58ff, MWA_RAM),
        new Memory_WriteAddress(0x6000, 0x6001, galaxian_leds_w),
        new Memory_WriteAddress(0x6002, 0x6002, galaxian_coin_lockout_w),
        new Memory_WriteAddress(0x6003, 0x6003, galaxian_coin_counter_w),
        new Memory_WriteAddress(0x6004, 0x6007, galaxian_lfo_freq_w),
        new Memory_WriteAddress(0x6800, 0x6802, galaxian_background_enable_w),
        new Memory_WriteAddress(0x6803, 0x6803, galaxian_noise_enable_w),
        new Memory_WriteAddress(0x6805, 0x6805, galaxian_shoot_enable_w),
        new Memory_WriteAddress(0x6806, 0x6807, galaxian_vol_w),
        new Memory_WriteAddress(0x7001, 0x7001, interrupt_enable_w),
        new Memory_WriteAddress(0x7004, 0x7004, galaxian_stars_enable_w),
        new Memory_WriteAddress(0x7006, 0x7006, galaxian_flip_screen_x_w),
        new Memory_WriteAddress(0x7007, 0x7007, galaxian_flip_screen_y_w),
        new Memory_WriteAddress(0x7800, 0x7800, galaxian_pitch_w),
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };

    public static Memory_ReadAddress mooncrst_readmem[] = {
        new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_ReadAddress(0x0000, 0x3fff, MRA_ROM),
        new Memory_ReadAddress(0x8000, 0x83ff, MRA_RAM),
        new Memory_ReadAddress(0x9000, 0x93ff, MRA_RAM),
        new Memory_ReadAddress(0x9400, 0x97ff, galaxian_videoram_r),
        new Memory_ReadAddress(0x9800, 0x98ff, MRA_RAM),
        new Memory_ReadAddress(0xa000, 0xa000, input_port_0_r),
        new Memory_ReadAddress(0xa800, 0xa800, input_port_1_r),
        new Memory_ReadAddress(0xb000, 0xb000, input_port_2_r),
        new Memory_ReadAddress(0xb800, 0xb800, watchdog_reset_r),
        new Memory_ReadAddress(MEMPORT_MARKER, 0)
    };
    public static Memory_WriteAddress mooncrst_writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_WriteAddress(0x0000, 0x3fff, MWA_ROM),
        new Memory_WriteAddress(0x8000, 0x83ff, MWA_RAM),
        new Memory_WriteAddress(0x9000, 0x93ff, MWA_RAM, galaxian_videoram),
        new Memory_WriteAddress(0x9800, 0x983f, MWA_RAM, galaxian_attributesram),
        new Memory_WriteAddress(0x9840, 0x985f, MWA_RAM, galaxian_spriteram, galaxian_spriteram_size),
        new Memory_WriteAddress(0x9860, 0x987f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
        new Memory_WriteAddress(0x9880, 0x98ff, MWA_RAM),
        new Memory_WriteAddress(0xa000, 0xa002, mooncrst_gfxextend_w),
        new Memory_WriteAddress(0xa003, 0xa003, galaxian_coin_counter_w),
        new Memory_WriteAddress(0xa004, 0xa007, galaxian_lfo_freq_w),
        new Memory_WriteAddress(0xa800, 0xa802, galaxian_background_enable_w),
        new Memory_WriteAddress(0xa803, 0xa803, galaxian_noise_enable_w),
        new Memory_WriteAddress(0xa805, 0xa805, galaxian_shoot_enable_w),
        new Memory_WriteAddress(0xa806, 0xa807, galaxian_vol_w),
        new Memory_WriteAddress(0xb000, 0xb000, interrupt_enable_w),
        new Memory_WriteAddress(0xb004, 0xb004, galaxian_stars_enable_w),
        new Memory_WriteAddress(0xb006, 0xb006, galaxian_flip_screen_x_w),
        new Memory_WriteAddress(0xb007, 0xb007, galaxian_flip_screen_y_w),
        new Memory_WriteAddress(0xb800, 0xb800, galaxian_pitch_w),
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };

    public static Memory_ReadAddress scramblb_readmem[] = {
        new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_ReadAddress(0x0000, 0x3fff, MRA_ROM),
        new Memory_ReadAddress(0x4000, 0x4bff, MRA_RAM),
        new Memory_ReadAddress(0x5000, 0x50ff, MRA_RAM),
        new Memory_ReadAddress(0x6000, 0x6000, input_port_0_r),
        new Memory_ReadAddress(0x6800, 0x6800, input_port_1_r),
        new Memory_ReadAddress(0x7000, 0x7000, input_port_2_r),
        new Memory_ReadAddress(0x7800, 0x7800, watchdog_reset_r),
        new Memory_ReadAddress(0x8102, 0x8102, scramblb_protection_1_r),
        new Memory_ReadAddress(0x8202, 0x8202, scramblb_protection_2_r),
        new Memory_ReadAddress(MEMPORT_MARKER, 0)
    };
    public static Memory_WriteAddress scramblb_writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_WriteAddress(0x0000, 0x3fff, MWA_ROM),
        new Memory_WriteAddress(0x4000, 0x47ff, MWA_RAM),
        new Memory_WriteAddress(0x4800, 0x4bff, MWA_RAM, galaxian_videoram),
        new Memory_WriteAddress(0x5000, 0x503f, MWA_RAM, galaxian_attributesram),
        new Memory_WriteAddress(0x5040, 0x505f, MWA_RAM, galaxian_spriteram, galaxian_spriteram_size),
        new Memory_WriteAddress(0x5060, 0x507f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
        new Memory_WriteAddress(0x5080, 0x50ff, MWA_RAM),
        new Memory_WriteAddress(0x6000, 0x6001, MWA_NOP), /* sound triggers */
        new Memory_WriteAddress(0x6003, 0x6003, galaxian_coin_counter_w),
        new Memory_WriteAddress(0x6004, 0x6007, galaxian_lfo_freq_w),
        new Memory_WriteAddress(0x6800, 0x6802, galaxian_background_enable_w),
        new Memory_WriteAddress(0x6803, 0x6803, galaxian_noise_enable_w),
        new Memory_WriteAddress(0x6805, 0x6805, galaxian_shoot_enable_w),
        new Memory_WriteAddress(0x6806, 0x6807, galaxian_vol_w),
        new Memory_WriteAddress(0x7001, 0x7001, interrupt_enable_w),
        new Memory_WriteAddress(0x7002, 0x7002, galaxian_coin_counter_w),
        new Memory_WriteAddress(0x7003, 0x7003, scramble_background_blue_w),
        new Memory_WriteAddress(0x7004, 0x7004, galaxian_stars_enable_w),
        new Memory_WriteAddress(0x7006, 0x7006, galaxian_flip_screen_x_w),
        new Memory_WriteAddress(0x7007, 0x7007, galaxian_flip_screen_y_w),
        new Memory_WriteAddress(0x7800, 0x7800, galaxian_pitch_w),
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };

    public static Memory_ReadAddress jumpbug_readmem[] = {
        new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_ReadAddress(0x0000, 0x3fff, MRA_ROM),
        new Memory_ReadAddress(0x4000, 0x4bff, MRA_RAM),
        new Memory_ReadAddress(0x4c00, 0x4fff, galaxian_videoram_r),
        new Memory_ReadAddress(0x5000, 0x50ff, MRA_RAM),
        new Memory_ReadAddress(0x6000, 0x6000, input_port_0_r),
        new Memory_ReadAddress(0x6800, 0x6800, input_port_1_r),
        new Memory_ReadAddress(0x7000, 0x7000, input_port_2_r),
        new Memory_ReadAddress(0x8000, 0xafff, MRA_ROM),
        new Memory_ReadAddress(0xb000, 0xbfff, jumpbug_protection_r),
        new Memory_ReadAddress(0xfff0, 0xffff, MRA_RAM),
        new Memory_ReadAddress(MEMPORT_MARKER, 0)
    };
    public static Memory_WriteAddress jumpbug_writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_WriteAddress(0x0000, 0x3fff, MWA_ROM),
        new Memory_WriteAddress(0x4000, 0x47ff, MWA_RAM),
        new Memory_WriteAddress(0x4800, 0x4bff, MWA_RAM, galaxian_videoram),
        new Memory_WriteAddress(0x4c00, 0x4fff, galaxian_videoram_w),
        new Memory_WriteAddress(0x5000, 0x503f, MWA_RAM, galaxian_attributesram),
        new Memory_WriteAddress(0x5040, 0x505f, MWA_RAM, galaxian_spriteram, galaxian_spriteram_size),
        new Memory_WriteAddress(0x5060, 0x507f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
        new Memory_WriteAddress(0x5080, 0x50ff, MWA_RAM),
        new Memory_WriteAddress(0x5800, 0x5800, AY8910_write_port_0_w),
        new Memory_WriteAddress(0x5900, 0x5900, AY8910_control_port_0_w),
        new Memory_WriteAddress(0x6002, 0x6006, jumpbug_gfxbank_w),
        new Memory_WriteAddress(0x7001, 0x7001, interrupt_enable_w),
        new Memory_WriteAddress(0x7002, 0x7002, galaxian_coin_counter_w),
        new Memory_WriteAddress(0x7004, 0x7004, galaxian_stars_enable_w),
        new Memory_WriteAddress(0x7006, 0x7006, galaxian_flip_screen_x_w),
        new Memory_WriteAddress(0x7007, 0x7007, galaxian_flip_screen_y_w),
        new Memory_WriteAddress(0x8000, 0xafff, MWA_ROM),
        new Memory_WriteAddress(0xfff0, 0xffff, MWA_RAM),
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };

    public static Memory_WriteAddress checkman_writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_WriteAddress(0x0000, 0x3fff, MWA_ROM),
        new Memory_WriteAddress(0x8000, 0x83ff, MWA_RAM),
        new Memory_WriteAddress(0x9000, 0x93ff, MWA_RAM, galaxian_videoram),
        new Memory_WriteAddress(0x9800, 0x983f, MWA_RAM, galaxian_attributesram),
        new Memory_WriteAddress(0x9840, 0x985f, MWA_RAM, galaxian_spriteram, galaxian_spriteram_size),
        new Memory_WriteAddress(0x9860, 0x987f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
        new Memory_WriteAddress(0x9880, 0x98ff, MWA_RAM),
        new Memory_WriteAddress(0xa004, 0xa007, galaxian_lfo_freq_w),
        new Memory_WriteAddress(0xa800, 0xa802, galaxian_background_enable_w),
        new Memory_WriteAddress(0xa803, 0xa803, galaxian_noise_enable_w),
        new Memory_WriteAddress(0xa805, 0xa805, galaxian_shoot_enable_w),
        new Memory_WriteAddress(0xa806, 0xa807, galaxian_vol_w),
        new Memory_WriteAddress(0xb001, 0xb001, interrupt_enable_w),
        new Memory_WriteAddress(0xb004, 0xb004, galaxian_stars_enable_w),
        new Memory_WriteAddress(0xb006, 0xb006, galaxian_flip_screen_x_w),
        new Memory_WriteAddress(0xb007, 0xb007, galaxian_flip_screen_y_w),
        new Memory_WriteAddress(0xb800, 0xb800, galaxian_pitch_w),
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };

    public static Memory_WriteAddress checkmaj_writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_WriteAddress(0x0000, 0x3fff, MWA_ROM),
        new Memory_WriteAddress(0x4000, 0x47ff, MWA_RAM),
        new Memory_WriteAddress(0x5000, 0x53ff, MWA_RAM, galaxian_videoram),
        new Memory_WriteAddress(0x5800, 0x583f, MWA_RAM, galaxian_attributesram),
        new Memory_WriteAddress(0x5840, 0x585f, MWA_RAM, galaxian_spriteram, galaxian_spriteram_size),
        new Memory_WriteAddress(0x5860, 0x587f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
        new Memory_WriteAddress(0x5880, 0x58ff, MWA_RAM),
        new Memory_WriteAddress(0x7001, 0x7001, interrupt_enable_w),
        new Memory_WriteAddress(0x7006, 0x7006, galaxian_flip_screen_x_w),
        new Memory_WriteAddress(0x7007, 0x7007, galaxian_flip_screen_y_w),
        new Memory_WriteAddress(0x7800, 0x7800, checkman_sound_command_w),
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };

    public static IO_WritePort checkman_writeport[] = {
        new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8), new IO_WritePort(0, 0, checkman_sound_command_w),
        new IO_WritePort(MEMPORT_MARKER, 0)
    };

    public static Memory_ReadAddress checkman_sound_readmem[] = {
        new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_ReadAddress(0x0000, 0x0fff, MRA_ROM),
        new Memory_ReadAddress(0x2000, 0x23ff, MRA_RAM),
        new Memory_ReadAddress(MEMPORT_MARKER, 0)
    };
    public static Memory_WriteAddress checkman_sound_writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_WriteAddress(0x0000, 0x0fff, MWA_ROM),
        new Memory_WriteAddress(0x2000, 0x23ff, MWA_RAM),
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };

    public static IO_ReadPort checkman_sound_readport[] = {
        new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8), new IO_ReadPort(0x03, 0x03, soundlatch_r),
        new IO_ReadPort(0x06, 0x06, AY8910_read_port_0_r),
        new IO_ReadPort(MEMPORT_MARKER, 0)
    };
    public static IO_WritePort checkman_sound_writeport[] = {
        new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8), new IO_WritePort(0x04, 0x04, AY8910_control_port_0_w),
        new IO_WritePort(0x05, 0x05, AY8910_write_port_0_w),
        new IO_WritePort(MEMPORT_MARKER, 0)
    };

    public static Memory_ReadAddress checkmaj_sound_readmem[] = {
        new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_ReadAddress(0x0000, 0x0fff, MRA_ROM),
        new Memory_ReadAddress(0x8000, 0x81ff, MRA_RAM),
        new Memory_ReadAddress(0xa002, 0xa002, AY8910_read_port_0_r),
        new Memory_ReadAddress(MEMPORT_MARKER, 0)
    };
    public static Memory_WriteAddress checkmaj_sound_writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_WriteAddress(0x0000, 0x0fff, MWA_ROM),
        new Memory_WriteAddress(0x8000, 0x81ff, MWA_RAM),
        new Memory_WriteAddress(0xa000, 0xa000, AY8910_control_port_0_w),
        new Memory_WriteAddress(0xa001, 0xa001, AY8910_write_port_0_w),
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };

    public static Memory_WriteAddress kingball_writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_WriteAddress(0x0000, 0x2fff, MWA_ROM),
        new Memory_WriteAddress(0x8000, 0x83ff, MWA_RAM),
        new Memory_WriteAddress(0x9000, 0x93ff, MWA_RAM, galaxian_videoram),
        new Memory_WriteAddress(0x9800, 0x983f, MWA_RAM, galaxian_attributesram),
        new Memory_WriteAddress(0x9840, 0x985f, MWA_RAM, galaxian_spriteram, galaxian_spriteram_size),
        new Memory_WriteAddress(0x9860, 0x987f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
        new Memory_WriteAddress(0x9880, 0x98ff, MWA_RAM),
        new Memory_WriteAddress(0xa000, 0xa001, galaxian_leds_w),
        new Memory_WriteAddress(0xa002, 0xa002, galaxian_coin_lockout_w),
        new Memory_WriteAddress(0xa003, 0xa003, galaxian_coin_counter_w),
        new Memory_WriteAddress(0xa004, 0xa007, galaxian_lfo_freq_w),
        new Memory_WriteAddress(0xa800, 0xa802, galaxian_background_enable_w),
        new Memory_WriteAddress(0xa803, 0xa803, galaxian_noise_enable_w),
        new Memory_WriteAddress(0xa805, 0xa805, galaxian_shoot_enable_w),
        new Memory_WriteAddress(0xa806, 0xa807, galaxian_vol_w), //
        new Memory_WriteAddress(0xb000, 0xb000, kingball_sound1_w),
        new Memory_WriteAddress(0xb001, 0xb001, interrupt_enable_w),
        new Memory_WriteAddress(0xb002, 0xb002, kingball_sound2_w),
        new Memory_WriteAddress(0xb003, 0xb003, kingball_speech_dip_w),
        new Memory_WriteAddress(0xb004, 0xb004, MWA_NOP), /* noise generator enable */
        new Memory_WriteAddress(0xb006, 0xb006, galaxian_flip_screen_x_w),
        new Memory_WriteAddress(0xb007, 0xb007, galaxian_flip_screen_y_w),
        new Memory_WriteAddress(0xb800, 0xb800, galaxian_pitch_w),
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };
    public static Memory_ReadAddress kingball_sound_readmem[] = {
        new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_ReadAddress(0x0000, 0x1fff, MRA_ROM),
        new Memory_ReadAddress(MEMPORT_MARKER, 0)
    };
    public static Memory_WriteAddress kingball_sound_writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_WriteAddress(0x0000, 0x1fff, MWA_ROM),
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };
    public static IO_ReadPort kingball_sound_readport[] = {
        new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8), new IO_ReadPort(0x00, 0x00, soundlatch_r),
        new IO_ReadPort(MEMPORT_MARKER, 0)
    };
    public static IO_WritePort kingball_sound_writeport[] = {
        new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8), new IO_WritePort(0x00, 0x00, DAC_0_data_w),
        new IO_WritePort(MEMPORT_MARKER, 0)
    };

    /* Zig Zag can swap ROMs 2 and 3 as a form of copy protection */
    public static WriteHandlerPtr zigzag_sillyprotection_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            if (data != 0) {
                /* swap ROM 2 and 3! */
                cpu_setbank(1, new UBytePtr(RAM, 0x3000));
                cpu_setbank(2, new UBytePtr(RAM, 0x2000));
            } else {
                cpu_setbank(1, new UBytePtr(RAM, 0x2000));
                cpu_setbank(2, new UBytePtr(RAM, 0x3000));
            }
        }
    };

    /* but the way the 8910 is hooked up is even sillier! */
    static int latch;

    public static WriteHandlerPtr zigzag_8910_latch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            latch = offset;
        }
    };

    public static WriteHandlerPtr zigzag_8910_data_trigger_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            AY8910_write_port_0_w.handler(0, latch);
        }
    };

    public static WriteHandlerPtr zigzag_8910_control_trigger_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            AY8910_control_port_0_w.handler(0, latch);
        }
    };

    public static Memory_ReadAddress zigzag_readmem[] = {
        new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_ReadAddress(0x0000, 0x1fff, MRA_ROM),
        new Memory_ReadAddress(0x2000, 0x2fff, MRA_BANK1),
        new Memory_ReadAddress(0x3000, 0x3fff, MRA_BANK2),
        new Memory_ReadAddress(0x4000, 0x47ff, MRA_RAM),
        new Memory_ReadAddress(0x5000, 0x53ff, MRA_RAM),
        new Memory_ReadAddress(0x5800, 0x58ff, MRA_RAM),
        new Memory_ReadAddress(0x6000, 0x6000, input_port_0_r),
        new Memory_ReadAddress(0x6800, 0x6800, input_port_1_r),
        new Memory_ReadAddress(0x7000, 0x7000, input_port_2_r),
        new Memory_ReadAddress(0x7800, 0x7800, watchdog_reset_r),
        new Memory_ReadAddress(MEMPORT_MARKER, 0)
    };
    public static Memory_WriteAddress zigzag_writemem[] = {
        new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8), new Memory_WriteAddress(0x0000, 0x3fff, MWA_ROM),
        new Memory_WriteAddress(0x4000, 0x47ff, MWA_RAM),
        new Memory_WriteAddress(0x4800, 0x4800, MWA_NOP), /* part of the 8910 interface */
        new Memory_WriteAddress(0x4801, 0x4801, zigzag_8910_data_trigger_w),
        new Memory_WriteAddress(0x4803, 0x4803, zigzag_8910_control_trigger_w),
        new Memory_WriteAddress(0x4900, 0x49ff, zigzag_8910_latch_w),
        new Memory_WriteAddress(0x4a00, 0x4a00, MWA_NOP), /* part of the 8910 interface */
        new Memory_WriteAddress(0x5000, 0x53ff, MWA_RAM, galaxian_videoram),
        new Memory_WriteAddress(0x5800, 0x583f, MWA_RAM, galaxian_attributesram),
        new Memory_WriteAddress(0x5840, 0x587f, MWA_RAM, galaxian_spriteram, galaxian_spriteram_size), /* no bulletsram, all sprites */
        new Memory_WriteAddress(0x5880, 0x58ff, MWA_RAM),
        new Memory_WriteAddress(0x7001, 0x7001, interrupt_enable_w),
        new Memory_WriteAddress(0x7002, 0x7002, zigzag_sillyprotection_w),
        new Memory_WriteAddress(0x7006, 0x7006, galaxian_flip_screen_x_w),
        new Memory_WriteAddress(0x7007, 0x7007, galaxian_flip_screen_y_w),
        new Memory_WriteAddress(MEMPORT_MARKER, 0)
    };

    static InputPortPtr input_ports_galaxian = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_SERVICE(0x40, IP_ACTIVE_HIGH);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_SERVICE1);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xc0, DEF_STR("Free_Play"));

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "7000");
            PORT_DIPSETTING(0x01, "10000");
            PORT_DIPSETTING(0x02, "12000");
            PORT_DIPSETTING(0x03, "20000");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x04, "3");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_superg = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_SERVICE(0x40, IP_ACTIVE_HIGH);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_SERVICE1);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xc0, DEF_STR("Free_Play"));

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x01, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x01, "4000");
            PORT_DIPSETTING(0x02, "5000");
            PORT_DIPSETTING(0x03, "7000");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x04, "5");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_gteikokb = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0xe0, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_5C"));

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "7000");
            PORT_DIPSETTING(0x01, "None");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x04, "5");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_zerotime = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_SERVICE(0x40, IP_ACTIVE_HIGH);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_SERVICE1);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */
            PORT_DIPNAME(0xc0, 0x40, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x40, "A 1C/1C  B 1C/2C ");
            PORT_DIPSETTING(0xc0, "A 1C/1C  B 1C/3C ");
            PORT_DIPSETTING(0x00, "A 1C/2C  B 1C/4C ");
            PORT_DIPSETTING(0x80, "A 1C/2C  B 1C/5C ");
            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x03, "6000");
            PORT_DIPSETTING(0x02, "7000");
            PORT_DIPSETTING(0x01, "9000");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x04, "5");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            /* used */
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_pisces = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
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
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x40, "4");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "10000");
            PORT_DIPSETTING(0x01, "20000");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, "A 2C/1C  B 1C/2C 2C/5C");
            PORT_DIPSETTING(0x00, "A 1C/1C  B 1C/5C");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x04, "Hard");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_batman2 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_5C"));

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "30000");
            PORT_DIPSETTING(0x01, "50000");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x04, "5");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_warofbug = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_SERVICE1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("Free_Play"));
            /* 0x80 gives 2 Coins/1 Credit */

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x02, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x01, "2");
            PORT_DIPSETTING(0x02, "3");
            PORT_DIPSETTING(0x03, "4");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "500000");
            PORT_DIPSETTING(0x00, "750000");
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_redufo = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_SERVICE(0x40, IP_ACTIVE_HIGH);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_SERVICE1);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x40, "A 2C/1C  B 1C/3C");
            PORT_DIPSETTING(0x00, "A 1C/1C  B 1C/6C");
            PORT_DIPSETTING(0x80, "A 1C/2C  B 1C/12C");
            PORT_DIPSETTING(0xc0, DEF_STR("Free_Play"));

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x01, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x01, "4000");
            PORT_DIPSETTING(0x02, "5000");
            PORT_DIPSETTING(0x03, "7000");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x04, "5");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_exodus = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_SERVICE(0x40, IP_ACTIVE_HIGH);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_SERVICE1);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_5C"));

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "7000");
            PORT_DIPSETTING(0x01, "None");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x04, "5");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_pacmanbl = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_5C"));

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "15000");
            PORT_DIPSETTING(0x01, "20000");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x04, "5");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_devilfsg = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_5C"));

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "10000");
            PORT_DIPSETTING(0x01, "15000");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            /* Probably unused */
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "4");
            PORT_DIPSETTING(0x04, "5");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_zigzag = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xc0, DEF_STR("Free_Play"));

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x02, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "10000 60000");
            PORT_DIPSETTING(0x04, "20000 60000");
            PORT_DIPSETTING(0x08, "30000 60000");
            PORT_DIPSETTING(0x0c, "40000 60000");
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_mooncrgx = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0xe0, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_5C"));

            PORT_START();
            /* DSW */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x01, "30000");
            PORT_DIPSETTING(0x00, "50000");
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            /* probably unused */
 /*PORT_DIPNAME( 0x04, 0x00, "Language" );   This version is always in English */
 /* Code has been commented out at 0x2f4b */
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            /* probably unused */
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_scramblb = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x40, DEF_STR("Cocktail"));
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_START();
            /* IN2 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_4C"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x04, "4");
            PORT_DIPSETTING(0x08, "5");
            PORT_BITX(0, 0x0c, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "255", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            /* probably unused */
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            /* probably unused */
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            /* probably unused */
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            /* probably unused */
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_jumpbug = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_DIPNAME(0x40, 0x00, "Difficulty ?");
            PORT_DIPSETTING(0x00, "Hard?");
            PORT_DIPSETTING(0x40, "Easy?");
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPSETTING(0x03, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x04, "2/1 2/1");
            PORT_DIPSETTING(0x08, "2/1 1/3");
            PORT_DIPSETTING(0x00, "1/1 1/1");
            PORT_DIPSETTING(0x0c, "1/1 1/6");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_levers = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            /* probably unused */
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            /* probably unused */
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            /* used */
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            /* probably unused */
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            /* probably unused */
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            /* probably unused */
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            /* probably unused */
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_azurian = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            /* used */
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x80, "5");
            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "5000");
            PORT_DIPSETTING(0x02, "7000");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            /* used */
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_orbitron = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, "A 2C/1C  B 1C/3C");
            PORT_DIPSETTING(0x40, "A 1C/1C  B 1C/6C");
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x04, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_checkmaj = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);/* p2 tiles right */
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);/* p2 tiles left */
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, "A 1/1 B 1/6");
            PORT_DIPSETTING(0x40, "A 2/1 B 1/3");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));

            PORT_START();
            /* DSW */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "100000");
            PORT_DIPSETTING(0x04, "200000");
            PORT_DIPNAME(0x08, 0x00, "Difficulty Increases At Level");
            PORT_DIPSETTING(0x08, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON2);/* p1 tiles right */
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1);/* p1 tiles left */
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_swarm = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_SERVICE(0x40, IP_ACTIVE_HIGH);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_SERVICE1);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xc0, DEF_STR("Free_Play"));

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x01, "10000");
            PORT_DIPSETTING(0x02, "20000");
            PORT_DIPSETTING(0x03, "40000");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x04, "4");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_streakng = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0xc0, 0x40, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x40, "10000");
            PORT_DIPSETTING(0x80, "15000");
            PORT_DIPSETTING(0xc0, "20000");
            PORT_DIPSETTING(0x00, "None");
            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x03, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x0c, 0x04, DEF_STR("Lives"));
            PORT_DIPSETTING(0x0c, "1");
            PORT_DIPSETTING(0x08, "2");
            PORT_DIPSETTING(0x04, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_blkhole = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_SERVICE1);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0xc0, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_3C"));

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            /* Bonus Life? */
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x02, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_mooncrst = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* "reset" on schematics */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_SERVICE1);/* works only in the Gremlin version */

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "30000");
            PORT_DIPSETTING(0x40, "50000");
            PORT_DIPNAME(0x80, 0x80, "Language");
            PORT_DIPSETTING(0x80, "English");
            PORT_DIPSETTING(0x00, "Japanese");
            PORT_START();
            /* DSW */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x03, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("Free_Play"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_eagle = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* "reset" on schematics */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "30000");
            PORT_DIPSETTING(0x40, "50000");
            PORT_DIPNAME(0x80, 0x80, "Language");
            PORT_DIPSETTING(0x80, "English");
            PORT_DIPSETTING(0x00, "Japanese");
            PORT_START();
            /* DSW */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("Free_Play"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_eagle2 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* "reset" on schematics */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "30000");
            PORT_DIPSETTING(0x40, "50000");
            PORT_DIPNAME(0x80, 0x80, "Language");
            PORT_DIPSETTING(0x80, "English");
            PORT_DIPSETTING(0x00, "Japanese");
            PORT_START();
            /* DSW */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x03, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x0c, 0x00, "Game Type");
            PORT_DIPSETTING(0x00, "Normal 1?");
            PORT_DIPSETTING(0x04, "Normal 2?");
            PORT_DIPSETTING(0x08, "Normal 3?");
            PORT_DIPSETTING(0x0c, DEF_STR("Free_Play"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_moonqsr = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* "reset" on schematics */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_SERVICE1);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x40, "Medium");
            PORT_DIPSETTING(0x80, "Hard");
            PORT_DIPSETTING(0xc0, "Hardest");
            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x03, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("Free_Play"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_checkman = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);/* p2 tiles right */
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP);
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);/* also p1 tiles left */
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);/* also p1 tiles right */
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);/* p2 tiles left */
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_COCKTAIL);
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, "A 1/1 B 1/6");
            PORT_DIPSETTING(0x40, "A 2/1 B 1/3");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));

            PORT_START();
            /* DSW */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "100000");
            PORT_DIPSETTING(0x04, "200000");
            PORT_DIPNAME(0x08, 0x00, "Difficulty Increases At Level");
            PORT_DIPSETTING(0x08, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_moonal2 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_SERVICE(0x40, IP_ACTIVE_HIGH);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_SERVICE1);/* works only in the Gremlin version */

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xc0, DEF_STR("Free_Play"));

            PORT_START();
            /* DSW */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x01, "4000");
            PORT_DIPSETTING(0x02, "5000");
            PORT_DIPSETTING(0x03, "7000");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x04, "5");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_kingball = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_SERVICE(0x40, IP_ACTIVE_HIGH);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_SERVICE1);
            /* Hack? - possibly multiplexed via writes to $b003 */
            //PORT_DIPNAME( 0x80, 0x80, "Speech" );	//PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
            //PORT_DIPSETTING(    0x80, DEF_STR( "On") );

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_SPECIAL);/* NOISE line */
            PORT_DIPNAME(0xc0, 0x40, DEF_STR("Coinage"));
            PORT_DIPSETTING(0xc0, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));

            PORT_START();
            /* DSW */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "10000");
            PORT_DIPSETTING(0x01, "12000");
            PORT_DIPSETTING(0x02, "15000");
            PORT_DIPSETTING(0x03, "None");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x04, "3");
            PORT_DIPNAME(0xf8, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0xf8, DEF_STR("On"));

            PORT_START();
            /* IN3 (fake) */
 /* Hack? - possibly multiplexed via writes to $b003 - marked as SLAM */
            PORT_DIPNAME(0x01, 0x01, "Speech");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout galaxian_charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            RGN_FRAC(1, 2),
            2, /* 2 bits per pixel */
            new int[]{RGN_FRAC(0, 2), RGN_FRAC(1, 2)}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );
    static GfxLayout galaxian_spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            RGN_FRAC(1, 2),
            2, /* 2 bits per pixel */
            new int[]{RGN_FRAC(0, 2), RGN_FRAC(1, 2)}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 8 * 8 + 4, 8 * 8 + 5, 8 * 8 + 6, 8 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                16 * 8, 17 * 8, 18 * 8, 19 * 8, 20 * 8, 21 * 8, 22 * 8, 23 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxLayout pacmanbl_charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 256 * 8 * 8}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );
    static GfxLayout pacmanbl_spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            64, /* 64 sprites */
            2, /* 2 bits per pixel */
            new int[]{0, 64 * 16 * 16}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 8 * 8 + 4, 8 * 8 + 5, 8 * 8 + 6, 8 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                16 * 8, 17 * 8, 18 * 8, 19 * 8, 20 * 8, 21 * 8, 22 * 8, 23 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxDecodeInfo galaxian_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x0000, galaxian_charlayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0x0000, galaxian_spritelayout, 0, 8),
                new GfxDecodeInfo(-1) /* end of array */};

    /* separate character and sprite ROMs */
    static GfxDecodeInfo pacmanbl_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x0000, pacmanbl_charlayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0x1000, pacmanbl_spritelayout, 0, 8),
                new GfxDecodeInfo(-1) /* end of array */};

    static CustomSound_interface custom_interface = new CustomSound_interface(
            galaxian_sh_start,
            galaxian_sh_stop,
            galaxian_sh_update
    );

    static AY8910interface jumpbug_ay8910_interface = new AY8910interface(
            1, /* 1 chip */
            1789750, /* 1.78975 MHz? */
            new int[]{50},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    static AY8910interface checkmaj_ay8910_interface = new AY8910interface(
            1, /* 1 chip */
            1620000, /* 1.62 MHz? (Used the same as Moon Cresta) */
            new int[]{50},
            new ReadHandlerPtr[]{soundlatch_r},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    static DACinterface kingball_dac_interface = new DACinterface(
            1,
            new int[]{100}
    );

    static MachineDriver machine_driver_galaxian = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        galaxian_readmem, galaxian_writemem, null, null,
                        nmi_interrupt, 1
                )
            },
            16000.0 / 132 / 2, 2500, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            galaxian_gfxdecodeinfo,
            32 + 2 + 64 + 1, 8 * 4, /* 32 for the characters, 2 for the bullets, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            galaxian_vh_start,
            null,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        custom_interface
                )
            }
    );

    static MachineDriver machine_driver_pisces = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        galaxian_readmem, galaxian_writemem, null, null,
                        nmi_interrupt, 1
                )
            },
            16000.0 / 132 / 2, 2500, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            galaxian_gfxdecodeinfo,
            32 + 2 + 64 + 1, 8 * 4, /* 32 for the characters, 2 for the bullets, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            pisces_vh_start,
            null,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        custom_interface
                )
            }
    );

    static MachineDriver machine_driver_batman2 = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        galaxian_readmem, galaxian_writemem, null, null,
                        nmi_interrupt, 1
                )
            },
            16000.0 / 132 / 2, 2500, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            galaxian_gfxdecodeinfo,
            32 + 2 + 64 + 1, 8 * 4, /* 32 for the characters, 2 for the bullets, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            batman2_vh_start,
            null,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        custom_interface
                )
            }
    );

    static MachineDriver machine_driver_mooncrgx = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        galaxian_readmem, galaxian_writemem, null, null,
                        nmi_interrupt, 1
                )
            },
            16000.0 / 132 / 2, 2500, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            galaxian_gfxdecodeinfo,
            32 + 2 + 64 + 1, 8 * 4, /* 32 for the characters, 2 for the bullets, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            mooncrst_vh_start,
            null,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        custom_interface
                )
            }
    );

    static MachineDriver machine_driver_pacmanbl = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        galaxian_readmem, galaxian_writemem, null, null,
                        nmi_interrupt, 1
                )
            },
            16000.0 / 132 / 2, 2500, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            pacmanbl_gfxdecodeinfo,
            32 + 2 + 64 + 1, 8 * 4, /* 32 for the characters, 2 for the bullets, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            galaxian_vh_start,
            null,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        custom_interface
                )
            }
    );

    static MachineDriver machine_driver_devilfsg = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        galaxian_readmem, galaxian_writemem, null, null,
                        interrupt, 1
                )
            },
            16000.0 / 132 / 2, 2500, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            pacmanbl_gfxdecodeinfo,
            32 + 2 + 64 + 1, 8 * 4, /* 32 for the characters, 2 for the bullets, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            galaxian_vh_start,
            null,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        custom_interface
                )
            }
    );

    static MachineDriver machine_driver_scramblb = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        scramblb_readmem, scramblb_writemem, null, null,
                        nmi_interrupt, 1
                )
            },
            16000.0 / 132 / 2, 2500, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            galaxian_gfxdecodeinfo,
            32 + 2 + 64 + 1, 8 * 4, /* 32 for the characters, 2 for the bullets, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            scramble_vh_start,
            null,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        custom_interface
                )
            }
    );

    static MachineDriver machine_driver_mooncrst = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        mooncrst_readmem, mooncrst_writemem, null, null,
                        nmi_interrupt, 1
                )
            },
            16000.0 / 132 / 2, 2500, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            galaxian_gfxdecodeinfo,
            32 + 2 + 64 + 1, 8 * 4, /* 32 for the characters, 2 for the bullets, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            mooncrst_vh_start,
            null,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        custom_interface
                )
            }
    );

    static MachineDriver machine_driver_moonqsr = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        mooncrst_readmem, mooncrst_writemem, null, null,
                        nmi_interrupt, 1
                )
            },
            16000.0 / 132 / 2, 2500, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            galaxian_gfxdecodeinfo,
            32 + 2 + 64 + 1, 8 * 4, /* 32 for the characters, 2 for the bullets, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            moonqsr_vh_start,
            null,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        custom_interface
                )
            }
    );

    static MachineDriver machine_driver_zigzag = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        zigzag_readmem, zigzag_writemem, null, null,
                        nmi_interrupt, 1
                )
            },
            16000.0 / 132 / 2, 2500, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            pacmanbl_gfxdecodeinfo,
            32 + 2 + 64 + 1, 8 * 4, /* 32 for the characters, 2 for the bullets, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            galaxian_plain_vh_start,
            null,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        jumpbug_ay8910_interface
                )
            }
    );

    static MachineDriver machine_driver_jumpbug = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3072000, /* 3.072 MHz */
                        jumpbug_readmem, jumpbug_writemem, null, null,
                        nmi_interrupt, 1
                )
            },
            16000.0 / 132 / 2, 2500, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            galaxian_gfxdecodeinfo,
            32 + 2 + 64 + 1, 8 * 4, /* 32 for the characters, 2 for the bullets, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            jumpbug_vh_start,
            null,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        jumpbug_ay8910_interface
                )
            }
    );

    static MachineDriver machine_driver_checkman = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        mooncrst_readmem, checkman_writemem, null, checkman_writeport,
                        nmi_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        1620000, /* 1.62 MHz */
                        checkman_sound_readmem, checkman_sound_writemem,
                        checkman_sound_readport, checkman_sound_writeport,
                        interrupt, 1 /* NMIs are triggered by the main CPU */
                )
            },
            16000.0 / 132 / 2, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            galaxian_gfxdecodeinfo,
            32 + 2 + 64 + 1, 8 * 4, /* 32 for the characters, 2 for the bullets, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            mooncrst_vh_start,
            null,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        custom_interface
                ),
                new MachineSound(
                        SOUND_AY8910,
                        jumpbug_ay8910_interface
                )
            }
    );

    static MachineDriver machine_driver_checkmaj = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        galaxian_readmem, checkmaj_writemem, null, null,
                        nmi_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        1620000, /* 1.62 MHz? (used the same as Moon Cresta) */
                        checkmaj_sound_readmem, checkmaj_sound_writemem, null, null,
                        interrupt, 32 /* NMIs are triggered by the main CPU */
                )
            },
            16000.0 / 132 / 2, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            galaxian_gfxdecodeinfo,
            32 + 2 + 64 + 1, 8 * 4, /* 32 for the characters, 2 for the bullets, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            galaxian_vh_start,
            null,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        checkmaj_ay8910_interface
                )
            }
    );

    static MachineDriver machine_driver_kingball = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        mooncrst_readmem, kingball_writemem, null, null,
                        nmi_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        5000000 / 2, /* 2.5 MHz */
                        kingball_sound_readmem, kingball_sound_writemem,
                        kingball_sound_readport, kingball_sound_writeport,
                        ignore_interrupt, 1
                )
            },
            16000.0 / 132 / 2, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            galaxian_gfxdecodeinfo,
            32 + 2 + 64 + 1, 8 * 4, /* 32 for the characters, 2 for the bullets, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            galaxian_vh_start,
            null,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        custom_interface
                ),
                new MachineSound(
                        SOUND_DAC,
                        kingball_dac_interface
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
    static RomLoadPtr rom_galaxian = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("galmidw.u", 0x0000, 0x0800, 0x745e2d61);
            ROM_LOAD("galmidw.v", 0x0800, 0x0800, 0x9c999a40);
            ROM_LOAD("galmidw.w", 0x1000, 0x0800, 0xb5894925);
            ROM_LOAD("galmidw.y", 0x1800, 0x0800, 0x6b3ca10b);
            ROM_LOAD("7l", 0x2000, 0x0800, 0x1b933207);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("1h.bin", 0x0000, 0x0800, 0x39fb43a4);
            ROM_LOAD("1k.bin", 0x0800, 0x0800, 0x7e3f56a2);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("6l.bpr", 0x0000, 0x0020, 0xc3ac9467);
            ROM_END();
        }
    };

    static RomLoadPtr rom_galaxiaj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("7f.bin", 0x0000, 0x1000, 0x4335b1de);
            ROM_LOAD("7j.bin", 0x1000, 0x1000, 0x4e6f66a1);
            ROM_LOAD("7l.bin", 0x2000, 0x0800, 0x5341d75a);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("1h.bin", 0x0000, 0x0800, 0x39fb43a4);
            ROM_LOAD("1k.bin", 0x0800, 0x0800, 0x7e3f56a2);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("6l.bpr", 0x0000, 0x0020, 0xc3ac9467);
            ROM_END();
        }
    };

    static RomLoadPtr rom_galmidw = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("galmidw.u", 0x0000, 0x0800, 0x745e2d61);
            ROM_LOAD("galmidw.v", 0x0800, 0x0800, 0x9c999a40);
            ROM_LOAD("galmidw.w", 0x1000, 0x0800, 0xb5894925);
            ROM_LOAD("galmidw.y", 0x1800, 0x0800, 0x6b3ca10b);
            ROM_LOAD("galmidw.z", 0x2000, 0x0800, 0xcb24f797);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("galmidw.1j", 0x0000, 0x0800, 0x84decf98);
            ROM_LOAD("galmidw.1k", 0x0800, 0x0800, 0xc31ada9e);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("6l.bpr", 0x0000, 0x0020, 0xc3ac9467);
            ROM_END();
        }
    };

    static RomLoadPtr rom_superg = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("7f.bin", 0x0000, 0x1000, 0x4335b1de);
            ROM_LOAD("superg.w", 0x1000, 0x0800, 0xddeabdae);
            ROM_LOAD("superg.y", 0x1800, 0x0800, 0x9463f753);
            ROM_LOAD("superg.z", 0x2000, 0x0800, 0xe6312e35);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("galmidw.1j", 0x0000, 0x0800, 0x84decf98);
            ROM_LOAD("galmidw.1k", 0x0800, 0x0800, 0xc31ada9e);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("6l.bpr", 0x0000, 0x0020, 0xc3ac9467);
            ROM_END();
        }
    };

    static RomLoadPtr rom_galapx = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("galx.u", 0x0000, 0x0800, 0x79e4007d);
            ROM_LOAD("galx.v", 0x0800, 0x0800, 0xbc16064e);
            ROM_LOAD("galx.w", 0x1000, 0x0800, 0x72d2d3ee);
            ROM_LOAD("galx.y", 0x1800, 0x0800, 0xafe397f3);
            ROM_LOAD("galx.z", 0x2000, 0x0800, 0x778c0d3c);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("galx.1h", 0x0000, 0x0800, 0xe8810654);
            ROM_LOAD("galx.1k", 0x0800, 0x0800, 0xcbe84a76);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("6l.bpr", 0x0000, 0x0020, 0xc3ac9467);
            ROM_END();
        }
    };

    static RomLoadPtr rom_galap1 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("7f.bin", 0x0000, 0x1000, 0x4335b1de);
            ROM_LOAD("galx_1_3.rom", 0x1000, 0x0800, 0x4c7031c0);
            ROM_LOAD("galx_1_4.rom", 0x1800, 0x0800, 0xe71e1d9e);
            ROM_LOAD("galx_1_5.rom", 0x2000, 0x0800, 0x6e65a3b2);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("galmidw.1j", 0x0000, 0x0800, 0x84decf98);
            ROM_LOAD("galmidw.1k", 0x0800, 0x0800, 0xc31ada9e);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("6l.bpr", 0x0000, 0x0020, 0xc3ac9467);
            ROM_END();
        }
    };

    static RomLoadPtr rom_galap4 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("galnamco.u", 0x0000, 0x0800, 0xacfde501);
            ROM_LOAD("galnamco.v", 0x0800, 0x0800, 0x65cf3c77);
            ROM_LOAD("galnamco.w", 0x1000, 0x0800, 0x9eef9ae6);
            ROM_LOAD("galnamco.y", 0x1800, 0x0800, 0x56a5ddd1);
            ROM_LOAD("galnamco.z", 0x2000, 0x0800, 0xf4bc7262);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("galx_4c1.rom", 0x0000, 0x0800, 0xd5e88ab4);
            ROM_LOAD("galx_4c2.rom", 0x0800, 0x0800, 0xa57b83e4);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("6l.bpr", 0x0000, 0x0020, 0xc3ac9467);
            ROM_END();
        }
    };

    static RomLoadPtr rom_galturbo = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("galturbo.u", 0x0000, 0x0800, 0xe8f3aa67);
            ROM_LOAD("galx.v", 0x0800, 0x0800, 0xbc16064e);
            ROM_LOAD("superg.w", 0x1000, 0x0800, 0xddeabdae);
            ROM_LOAD("galturbo.y", 0x1800, 0x0800, 0xa44f450f);
            ROM_LOAD("galturbo.z", 0x2000, 0x0800, 0x3247f3d4);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("galturbo.1h", 0x0000, 0x0800, 0xa713fd1a);
            ROM_LOAD("galturbo.1k", 0x0800, 0x0800, 0x28511790);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("6l.bpr", 0x0000, 0x0020, 0xc3ac9467);
            ROM_END();
        }
    };

    static RomLoadPtr rom_swarm = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("swarm1.bin", 0x0000, 0x0800, 0x21eba3d0);
            ROM_LOAD("swarm2.bin", 0x0800, 0x0800, 0xf3a436cd);
            ROM_LOAD("swarm3.bin", 0x1000, 0x0800, 0x2915e38b);
            ROM_LOAD("swarm4.bin", 0x1800, 0x0800, 0x8bbbf486);
            ROM_LOAD("swarm5.bin", 0x2000, 0x0800, 0xf1b1987e);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("swarma.bin", 0x0000, 0x0800, 0xef8657bb);
            ROM_LOAD("swarmb.bin", 0x0800, 0x0800, 0x60c4bd31);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("6l.bpr", 0x0000, 0x0020, 0xc3ac9467);
            ROM_END();
        }
    };

    static RomLoadPtr rom_zerotime = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("zt-p01c.016", 0x0000, 0x0800, 0x90a2bc61);
            ROM_LOAD("zt-2.016", 0x0800, 0x0800, 0xa433067e);
            ROM_LOAD("zt-3.016", 0x1000, 0x0800, 0xaaf038d4);
            ROM_LOAD("zt-4.016", 0x1800, 0x0800, 0x786d690a);
            ROM_LOAD("zt-5.016", 0x2000, 0x0800, 0xaf9260d7);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("ztc-2.016", 0x0000, 0x0800, 0x1b13ca05);
            ROM_LOAD("ztc-1.016", 0x0800, 0x0800, 0x5cd7df03);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("6l.bpr", 0x0000, 0x0020, 0xc3ac9467);
            ROM_END();
        }
    };

    static RomLoadPtr rom_pisces = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("pisces.a1", 0x0000, 0x0800, 0x856b8e1f);
            ROM_LOAD("pisces.a2", 0x0800, 0x0800, 0x055f9762);
            ROM_LOAD("pisces.b2", 0x1000, 0x0800, 0x5540f2e4);
            ROM_LOAD("pisces.c1", 0x1800, 0x0800, 0x44aaf525);
            ROM_LOAD("pisces.d1", 0x2000, 0x0800, 0xfade512b);
            ROM_LOAD("pisces.e2", 0x2800, 0x0800, 0x5ab2822f);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("pisces.1j", 0x0000, 0x1000, 0x2dba9e0e);
            ROM_LOAD("pisces.1k", 0x1000, 0x1000, 0xcdc5aa26);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("6331-1j.86", 0x0000, 0x0020, 0x24652bc4);/* very close to Galaxian */
            ROM_END();
        }
    };

    static RomLoadPtr rom_uniwars = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("f07_1a.bin", 0x0000, 0x0800, 0xd975af10);
            ROM_LOAD("h07_2a.bin", 0x0800, 0x0800, 0xb2ed14c3);
            ROM_LOAD("k07_3a.bin", 0x1000, 0x0800, 0x945f4160);
            ROM_LOAD("m07_4a.bin", 0x1800, 0x0800, 0xddc80bc5);
            ROM_LOAD("d08p_5a.bin", 0x2000, 0x0800, 0x62354351);
            ROM_LOAD("gg6", 0x2800, 0x0800, 0x270a3f4d);
            ROM_LOAD("m08p_7a.bin", 0x3000, 0x0800, 0xc9245346);
            ROM_LOAD("n08p_8a.bin", 0x3800, 0x0800, 0x797d45c7);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("egg10", 0x0000, 0x0800, 0x012941e0);
            ROM_LOAD("h01_2.bin", 0x0800, 0x0800, 0xc26132af);
            ROM_LOAD("egg9", 0x1000, 0x0800, 0xfc8b58fd);
            ROM_LOAD("k01_2.bin", 0x1800, 0x0800, 0xdcc2b33b);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("uniwars.clr", 0x0000, 0x0020, 0x25c79518);
            ROM_END();
        }
    };

    static RomLoadPtr rom_gteikoku = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("f07_1a.bin", 0x0000, 0x0800, 0xd975af10);
            ROM_LOAD("h07_2a.bin", 0x0800, 0x0800, 0xb2ed14c3);
            ROM_LOAD("k07_3a.bin", 0x1000, 0x0800, 0x945f4160);
            ROM_LOAD("m07_4a.bin", 0x1800, 0x0800, 0xddc80bc5);
            ROM_LOAD("d08p_5a.bin", 0x2000, 0x0800, 0x62354351);
            ROM_LOAD("e08p_6a.bin", 0x2800, 0x0800, 0xd915a389);
            ROM_LOAD("m08p_7a.bin", 0x3000, 0x0800, 0xc9245346);
            ROM_LOAD("n08p_8a.bin", 0x3800, 0x0800, 0x797d45c7);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("h01_1.bin", 0x0000, 0x0800, 0x8313c959);
            ROM_LOAD("h01_2.bin", 0x0800, 0x0800, 0xc26132af);
            ROM_LOAD("k01_1.bin", 0x1000, 0x0800, 0xc9d4537e);
            ROM_LOAD("k01_2.bin", 0x1800, 0x0800, 0xdcc2b33b);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_gteikokb = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("1.bin", 0x0000, 0x0800, 0xbf00252f);
            ROM_LOAD("2.bin", 0x0800, 0x0800, 0xf712b7d5);
            ROM_LOAD("k07_3a.bin", 0x1000, 0x0800, 0x945f4160);
            ROM_LOAD("4.bin", 0x1800, 0x0800, 0x808a39a8);
            ROM_LOAD("5.bin", 0x2000, 0x0800, 0x36fe6e67);
            ROM_LOAD("6.bin", 0x2800, 0x0800, 0xc5ea67e8);
            ROM_LOAD("m08p_7a.bin", 0x3000, 0x0800, 0xc9245346);
            ROM_LOAD("8.bin", 0x3800, 0x0800, 0x28df3229);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("h01_1.bin", 0x0000, 0x0800, 0x8313c959);
            ROM_LOAD("h01_2.bin", 0x0800, 0x0800, 0xc26132af);
            ROM_LOAD("k01_1.bin", 0x1000, 0x0800, 0xc9d4537e);
            ROM_LOAD("k01_2.bin", 0x1800, 0x0800, 0xdcc2b33b);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_spacbatt = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("f07_1a.bin", 0x0000, 0x0800, 0xd975af10);
            ROM_LOAD("h07_2a.bin", 0x0800, 0x0800, 0xb2ed14c3);
            ROM_LOAD("sb.3", 0x1000, 0x0800, 0xc25ce4c1);
            ROM_LOAD("sb.4", 0x1800, 0x0800, 0x8229835c);
            ROM_LOAD("sb.5", 0x2000, 0x0800, 0xf51ef930);
            ROM_LOAD("e08p_6a.bin", 0x2800, 0x0800, 0xd915a389);
            ROM_LOAD("m08p_7a.bin", 0x3000, 0x0800, 0xc9245346);
            ROM_LOAD("sb.8", 0x3800, 0x0800, 0xe59ff1ae);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("h01_1.bin", 0x0000, 0x0800, 0x8313c959);
            ROM_LOAD("h01_2.bin", 0x0800, 0x0800, 0xc26132af);
            ROM_LOAD("k01_1.bin", 0x1000, 0x0800, 0xc9d4537e);
            ROM_LOAD("k01_2.bin", 0x1800, 0x0800, 0xdcc2b33b);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_batman2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("01.bin", 0x0000, 0x0800, 0x150fbca5);
            ROM_LOAD("02.bin", 0x0800, 0x0800, 0xb1624fd0);
            ROM_LOAD("03.bin", 0x1000, 0x0800, 0x93774188);
            ROM_LOAD("04.bin", 0x1800, 0x0800, 0x8a94ec6c);
            ROM_LOAD("05.bin", 0x2000, 0x0800, 0xa3669461);
            ROM_LOAD("06.bin", 0x2800, 0x0800, 0xfa1efbfe);
            ROM_LOAD("07.bin", 0x3000, 0x0800, 0x9b77debd);
            ROM_LOAD("08.bin", 0x3800, 0x0800, 0x6466177e);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("09.bin", 0x0000, 0x0800, 0x1a657b1f);
            ROM_LOAD("11.bin", 0x0800, 0x0800, 0x7a2b48e5);
            ROM_LOAD("10.bin", 0x1000, 0x0800, 0x9b570016);
            ROM_LOAD("12.bin", 0x1800, 0x0800, 0x73956244);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_warofbug = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("warofbug.u", 0x0000, 0x0800, 0xb8dfb7e3);
            ROM_LOAD("warofbug.v", 0x0800, 0x0800, 0xfd8854e0);
            ROM_LOAD("warofbug.w", 0x1000, 0x0800, 0x4495aa14);
            ROM_LOAD("warofbug.y", 0x1800, 0x0800, 0xc14a541f);
            ROM_LOAD("warofbug.z", 0x2000, 0x0800, 0xc167fe55);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("warofbug.1k", 0x0000, 0x0800, 0x8100fa85);
            ROM_LOAD("warofbug.1j", 0x0800, 0x0800, 0xd1220ae9);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("warofbug.clr", 0x0000, 0x0020, 0x8688e64b);
            ROM_END();
        }
    };

    static RomLoadPtr rom_redufo = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("ru1a", 0x0000, 0x0800, 0x5a8e4f37);
            ROM_LOAD("ru2a", 0x0800, 0x0800, 0xc624f52d);
            ROM_LOAD("ru3a", 0x1000, 0x0800, 0xe1030d1c);
            ROM_LOAD("ru4a", 0x1800, 0x0800, 0x7692069e);
            ROM_LOAD("ru5a", 0x2000, 0x0800, 0xcb648ff3);
            ROM_LOAD("ru6a", 0x2800, 0x0800, 0xe1a9f58e);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("ruhja", 0x0000, 0x0800, 0x8a422b0d);
            ROM_LOAD("rukla", 0x0800, 0x0800, 0x1eb84cb1);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("6l.bpr", 0x0000, 0x0020, 0xc3ac9467);
            ROM_END();
        }
    };

    static RomLoadPtr rom_exodus = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("exodus1.bin", 0x0000, 0x0800, 0x5dfe65e1);
            ROM_LOAD("exodus2.bin", 0x0800, 0x0800, 0x6559222f);
            ROM_LOAD("exodus3.bin", 0x1000, 0x0800, 0xbf7030e8);
            ROM_LOAD("exodus4.bin", 0x1800, 0x0800, 0x3607909e);
            ROM_LOAD("exodus9.bin", 0x2000, 0x0800, 0x994a90c4);
            ROM_LOAD("exodus10.bin", 0x2800, 0x0800, 0xfbd11187);
            ROM_LOAD("exodus11.bin", 0x3000, 0x0800, 0xfd07d811);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("exodus5.bin", 0x0000, 0x0800, 0xb34c7cb4);
            ROM_LOAD("exodus6.bin", 0x0800, 0x0800, 0x50a2d447);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_streakng = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("sk1", 0x0000, 0x1000, 0xc8866ccb);
            ROM_LOAD("sk2", 0x1000, 0x1000, 0x7caea29b);
            ROM_LOAD("sk3", 0x2000, 0x1000, 0x7b4bfa76);
            ROM_LOAD("sk4", 0x3000, 0x1000, 0x056fc921);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("sk5", 0x0000, 0x1000, 0xd27f1e0c);
            ROM_LOAD("sk6", 0x1000, 0x1000, 0xa7089588);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("sk.bpr", 0x0000, 0x0020, 0xbce79607);
            ROM_END();
        }
    };

    static RomLoadPtr rom_pacmanbl = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("blpac1b", 0x0000, 0x0800, 0x6718df42);
            ROM_LOAD("blpac2b", 0x0800, 0x0800, 0x33be3648);
            ROM_LOAD("blpac3b", 0x1000, 0x0800, 0xf98c0ceb);
            ROM_LOAD("blpac4b", 0x1800, 0x0800, 0xa9cd0082);
            ROM_LOAD("blpac5b", 0x2000, 0x0800, 0x6d475afc);
            ROM_LOAD("blpac6b", 0x2800, 0x0800, 0xcbe863d3);
            ROM_LOAD("blpac7b", 0x3000, 0x0800, 0x7daef758);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("blpac12b", 0x0000, 0x0800, 0xb2ed320b);
            ROM_LOAD("blpac11b", 0x0800, 0x0800, 0xab88b2c4);
            ROM_LOAD("blpac10b", 0x1000, 0x0800, 0x44a45b72);
            ROM_LOAD("blpac9b", 0x1800, 0x0800, 0xfa84659f);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("blpaccp", 0x0000, 0x0020, 0x24652bc4);/* same as pisces */
            ROM_END();
        }
    };

    static RomLoadPtr rom_ghostmun = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("pac1.bin", 0x0000, 0x1000, 0x19338c70);
            ROM_LOAD("pac2.bin", 0x1000, 0x1000, 0x18db074d);
            ROM_LOAD("pac3.bin", 0x2000, 0x1000, 0xabb98b1d);
            ROM_LOAD("pac4.bin", 0x3000, 0x1000, 0x2403c78e);
            ROM_REGION(0x4000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("blpac12b", 0x0000, 0x0800, 0xb2ed320b);
            ROM_LOAD("blpac11b", 0x0800, 0x0800, 0xab88b2c4);
            ROM_LOAD("blpac10b", 0x1000, 0x0800, 0x44a45b72);
            ROM_LOAD("blpac9b", 0x1800, 0x0800, 0xfa84659f);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("ghostmun.clr", 0x0000, 0x0020, 0x8dca99ec);
            ROM_END();
        }
    };

    static RomLoadPtr rom_devilfsg = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);
            /* 64k for code */
            ROM_LOAD("dfish1.7f", 0x2000, 0x0800, 0x2ab19698);
            ROM_CONTINUE(0x0000, 0x0800);
            ROM_LOAD("dfish2.7h", 0x2800, 0x0800, 0x4e77f097);
            ROM_CONTINUE(0x0800, 0x0800);
            ROM_LOAD("dfish3.7k", 0x3000, 0x0800, 0x3f16a4c6);
            ROM_CONTINUE(0x1000, 0x0800);
            ROM_LOAD("dfish4.7m", 0x3800, 0x0800, 0x11fc7e59);
            ROM_CONTINUE(0x1800, 0x0800);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("dfish5.1h", 0x1000, 0x0800, 0xace6e31f);
            ROM_CONTINUE(0x0000, 0x0800);
            ROM_LOAD("dfish6.1k", 0x1800, 0x0800, 0xd7a6c4c4);
            ROM_CONTINUE(0x0800, 0x0800);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("82s123.6e", 0x0000, 0x0020, 0x4e3caeab);
            ROM_END();
        }
    };

    static RomLoadPtr rom_zigzag = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("zz_d1.bin", 0x0000, 0x1000, 0x8cc08d81);
            ROM_LOAD("zz_d2.bin", 0x1000, 0x1000, 0x326d8d45);
            ROM_LOAD("zz_d4.bin", 0x2000, 0x1000, 0xa94ed92a);
            ROM_LOAD("zz_d3.bin", 0x3000, 0x1000, 0xce5e7a00);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("zz_6_h1.bin", 0x0000, 0x0800, 0x780c162a);
            ROM_CONTINUE(0x1000, 0x0800);
            ROM_LOAD("zz_5.bin", 0x0800, 0x0800, 0xf3cdfec5);
            ROM_CONTINUE(0x1800, 0x0800);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("zzbp_e9.bin", 0x0000, 0x0020, 0xaa486dd0);
            ROM_END();
        }
    };

    static RomLoadPtr rom_zigzag2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("z1", 0x0000, 0x1000, 0x4c28349a);
            ROM_LOAD("zz_d2.bin", 0x1000, 0x1000, 0x326d8d45);
            ROM_LOAD("zz_d4.bin", 0x2000, 0x1000, 0xa94ed92a);
            ROM_LOAD("zz_d3.bin", 0x3000, 0x1000, 0xce5e7a00);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("zz_6_h1.bin", 0x0000, 0x0800, 0x780c162a);
            ROM_CONTINUE(0x1000, 0x0800);
            ROM_LOAD("zz_5.bin", 0x0800, 0x0800, 0xf3cdfec5);
            ROM_CONTINUE(0x1800, 0x0800);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("zzbp_e9.bin", 0x0000, 0x0020, 0xaa486dd0);
            ROM_END();
        }
    };

    static RomLoadPtr rom_mooncrgx = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("1", 0x0000, 0x0800, 0x84cf420b);
            ROM_LOAD("2", 0x0800, 0x0800, 0x4c2a61a1);
            ROM_LOAD("3", 0x1000, 0x0800, 0x1962523a);
            ROM_LOAD("4", 0x1800, 0x0800, 0x75dca896);
            ROM_LOAD("5", 0x2000, 0x0800, 0x32483039);
            ROM_LOAD("6", 0x2800, 0x0800, 0x43f2ab89);
            ROM_LOAD("7", 0x3000, 0x0800, 0x1e9c168c);
            ROM_LOAD("8", 0x3800, 0x0800, 0x5e09da94);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("1h_1_10.bin", 0x0000, 0x0800, 0x528da705);
            ROM_LOAD("12.chr", 0x0800, 0x0800, 0x5a4b17ea);
            ROM_LOAD("9.chr", 0x1000, 0x0800, 0x70df525c);
            ROM_LOAD("11.chr", 0x1800, 0x0800, 0xe0edccbd);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_scramblb = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("scramble.1k", 0x0000, 0x0800, 0x9e025c4a);
            ROM_LOAD("scramble.2k", 0x0800, 0x0800, 0x306f783e);
            ROM_LOAD("scramble.3k", 0x1000, 0x0800, 0x0500b701);
            ROM_LOAD("scramble.4k", 0x1800, 0x0800, 0xdd380a22);
            ROM_LOAD("scramble.5k", 0x2000, 0x0800, 0xdf0b9648);
            ROM_LOAD("scramble.1j", 0x2800, 0x0800, 0xb8c07b3c);
            ROM_LOAD("scramble.2j", 0x3000, 0x0800, 0x88ac07a0);
            ROM_LOAD("scramble.3j", 0x3800, 0x0800, 0xc67d57ca);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("5f.k", 0x0000, 0x0800, 0x4708845b);
            ROM_LOAD("5h.k", 0x0800, 0x0800, 0x11fd2887);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("82s123.6e", 0x0000, 0x0020, 0x4e3caeab);
            ROM_END();
        }
    };

    static RomLoadPtr rom_jumpbug = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("jb1", 0x0000, 0x1000, 0x415aa1b7);
            ROM_LOAD("jb2", 0x1000, 0x1000, 0xb1c27510);
            ROM_LOAD("jb3", 0x2000, 0x1000, 0x97c24be2);
            ROM_LOAD("jb4", 0x3000, 0x1000, 0x66751d12);
            ROM_LOAD("jb5", 0x8000, 0x1000, 0xe2d66faf);
            ROM_LOAD("jb6", 0x9000, 0x1000, 0x49e0bdfd);
            ROM_LOAD("jb7", 0xa000, 0x0800, 0x83d71302);
            ROM_REGION(0x3000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("jbl", 0x0000, 0x0800, 0x9a091b0a);
            ROM_LOAD("jbm", 0x0800, 0x0800, 0x8a0fc082);
            ROM_LOAD("jbn", 0x1000, 0x0800, 0x155186e0);
            ROM_LOAD("jbi", 0x1800, 0x0800, 0x7749b111);
            ROM_LOAD("jbj", 0x2000, 0x0800, 0x06e8d7df);
            ROM_LOAD("jbk", 0x2800, 0x0800, 0xb8dbddf3);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_jumpbugb = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("jb1", 0x0000, 0x1000, 0x415aa1b7);
            ROM_LOAD("jb2", 0x1000, 0x1000, 0xb1c27510);
            ROM_LOAD("jb3b", 0x2000, 0x1000, 0xcb8b8a0f);
            ROM_LOAD("jb4", 0x3000, 0x1000, 0x66751d12);
            ROM_LOAD("jb5b", 0x8000, 0x1000, 0x7553b5e2);
            ROM_LOAD("jb6b", 0x9000, 0x1000, 0x47be9843);
            ROM_LOAD("jb7b", 0xa000, 0x0800, 0x460aed61);
            ROM_REGION(0x3000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("jbl", 0x0000, 0x0800, 0x9a091b0a);
            ROM_LOAD("jbm", 0x0800, 0x0800, 0x8a0fc082);
            ROM_LOAD("jbn", 0x1000, 0x0800, 0x155186e0);
            ROM_LOAD("jbi", 0x1800, 0x0800, 0x7749b111);
            ROM_LOAD("jbj", 0x2000, 0x0800, 0x06e8d7df);
            ROM_LOAD("jbk", 0x2800, 0x0800, 0xb8dbddf3);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_levers = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);
            /* 64k for code */
            ROM_LOAD("g96059.a8", 0x0000, 0x1000, 0x9550627a);
            ROM_LOAD("g96060.d8", 0x2000, 0x1000, 0x5ac64646);
            ROM_LOAD("g96061.e8", 0x3000, 0x1000, 0x9db8e520);
            ROM_LOAD("g96062.h8", 0x8000, 0x1000, 0x7c8e8b3a);
            ROM_LOAD("g96063.j8", 0x9000, 0x1000, 0xfa61e793);
            ROM_LOAD("g96064.l8", 0xa000, 0x1000, 0xf797f389);
            ROM_REGION(0x3000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("g95948.n1", 0x0000, 0x0800, 0xd8a0c692);
            /*0x0800- 0x0fff empty */
            ROM_LOAD("g95949.s1", 0x1000, 0x0800, 0x3660a552);
            ROM_LOAD("g95946.j1", 0x1800, 0x0800, 0x73b61b2d);
            /*0x2000- 0x27ff empty */
            ROM_LOAD("g95947.m1", 0x2800, 0x0800, 0x72ff67e2);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("g960lev.clr", 0x0000, 0x0020, 0x01febbbe);
            ROM_END();
        }
    };

    static RomLoadPtr rom_azurian = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("pgm.1", 0x0000, 0x1000, 0x17a0fca7);
            ROM_LOAD("pgm.2", 0x1000, 0x1000, 0x14659848);
            ROM_LOAD("pgm.3", 0x2000, 0x1000, 0x8f60fb97);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("gfx.1", 0x0000, 0x0800, 0xf5afb803);
            ROM_LOAD("gfx.2", 0x0800, 0x0800, 0xae96e5d1);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_orbitron = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("orbitron.3", 0x0600, 0x0200, 0x419f9c9b);
            ROM_CONTINUE(0x0400, 0x0200);
            ROM_CONTINUE(0x0200, 0x0200);
            ROM_CONTINUE(0x0000, 0x0200);
            ROM_LOAD("orbitron.4", 0x0e00, 0x0200, 0x44ad56ac);
            ROM_CONTINUE(0x0c00, 0x0200);
            ROM_CONTINUE(0x0a00, 0x0200);
            ROM_CONTINUE(0x0800, 0x0200);
            ROM_LOAD("orbitron.1", 0x1600, 0x0200, 0xda3f5168);
            ROM_CONTINUE(0x1400, 0x0200);
            ROM_CONTINUE(0x1200, 0x0200);
            ROM_CONTINUE(0x1000, 0x0200);
            ROM_LOAD("orbitron.2", 0x1e00, 0x0200, 0xa3b813fc);
            ROM_CONTINUE(0x1c00, 0x0200);
            ROM_CONTINUE(0x1a00, 0x0200);
            ROM_CONTINUE(0x1800, 0x0200);
            ROM_LOAD("orbitron.5", 0x2000, 0x0800, 0x20cd8bb8);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("orbitron.6", 0x0000, 0x0800, 0x2c91b83f);
            ROM_LOAD("orbitron.7", 0x0800, 0x0800, 0x46f4cca4);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_checkman = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("cm1", 0x0000, 0x0800, 0xe8cbdd28);
            ROM_LOAD("cm2", 0x0800, 0x0800, 0xb8432d4d);
            ROM_LOAD("cm3", 0x1000, 0x0800, 0x15a97f61);
            ROM_LOAD("cm4", 0x1800, 0x0800, 0x8c12ecc0);
            ROM_LOAD("cm5", 0x2000, 0x0800, 0x2352cfd6);
            ROM_REGION(0x10000, REGION_CPU2, 0);/* 64k for sound code */
            ROM_LOAD("cm13", 0x0000, 0x0800, 0x0b09a3e8);
            ROM_LOAD("cm14", 0x0800, 0x0800, 0x47f043be);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("cm11", 0x0000, 0x0800, 0x8d1bcca0);
            ROM_RELOAD(0x0800, 0x0800);
            ROM_LOAD("cm9", 0x1000, 0x0800, 0x3cd5c751);
            ROM_RELOAD(0x1800, 0x0800);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("checkman.clr", 0x0000, 0x0020, 0x57a45057);
            ROM_END();
        }
    };

    static RomLoadPtr rom_checkmaj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("cm_1.bin", 0x0000, 0x1000, 0x456a118f);
            ROM_LOAD("cm_2.bin", 0x1000, 0x1000, 0x146b2c44);
            ROM_LOAD("cm_3.bin", 0x2000, 0x0800, 0x73e1c945);
            ROM_REGION(0x10000, REGION_CPU2, 0);/* 64k for sound code */
            ROM_LOAD("cm_4.bin", 0x0000, 0x1000, 0x923cffa1);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("cm_6.bin", 0x0000, 0x0800, 0x476a7cc3);
            ROM_LOAD("cm_5.bin", 0x0800, 0x0800, 0xb3df2b5f);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("checkman.clr", 0x0000, 0x0020, 0x57a45057);
            ROM_END();
        }
    };

    static RomLoadPtr rom_blkhole = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("bh1", 0x0000, 0x0800, 0x64998819);
            ROM_LOAD("bh2", 0x0800, 0x0800, 0x26f26ce4);
            ROM_LOAD("bh3", 0x1000, 0x0800, 0x3418bc45);
            ROM_LOAD("bh4", 0x1800, 0x0800, 0x735ff481);
            ROM_LOAD("bh5", 0x2000, 0x0800, 0x3f657be9);
            ROM_LOAD("bh6", 0x2800, 0x0800, 0xa057ab35);
            ROM_REGION(0x1000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("bh7", 0x0000, 0x0800, 0x975ba821);
            ROM_LOAD("bh8", 0x0800, 0x0800, 0x03d11020);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("6l.bpr", 0x0000, 0x0020, 0xc3ac9467);
            ROM_END();
        }
    };

    static RomLoadPtr rom_mooncrst = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("mc1", 0x0000, 0x0800, 0x7d954a7a);
            ROM_LOAD("mc2", 0x0800, 0x0800, 0x44bb7cfa);
            ROM_LOAD("mc3", 0x1000, 0x0800, 0x9c412104);
            ROM_LOAD("mc4", 0x1800, 0x0800, 0x7e9b1ab5);
            ROM_LOAD("mc5", 0x2000, 0x0800, 0x16c759af);
            ROM_LOAD("mc6", 0x2800, 0x0800, 0x69bcafdb);
            ROM_LOAD("mc7", 0x3000, 0x0800, 0xb50dbc46);
            ROM_LOAD("mc8", 0x3800, 0x0800, 0x18ca312b);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("mcs_b", 0x0000, 0x0800, 0xfb0f1f81);
            ROM_LOAD("mcs_d", 0x0800, 0x0800, 0x13932a15);
            ROM_LOAD("mcs_a", 0x1000, 0x0800, 0x631ebb5a);
            ROM_LOAD("mcs_c", 0x1800, 0x0800, 0x24cfd145);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_skybase = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("skybase.9a", 0x0000, 0x1000, 0x7d954a7a);
            ROM_LOAD("skybase.8a", 0x1000, 0x1000, 0x7d954a7a);
            ROM_LOAD("skybase.7a", 0x2000, 0x1000, 0x7d954a7a);
            ROM_LOAD("skybase.6a", 0x3000, 0x1000, 0x7d954a7a);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("mcs_b", 0x0000, 0x0800, 0xfb0f1f81);
            ROM_LOAD("mcs_d", 0x0800, 0x0800, 0x13932a15);
            ROM_LOAD("mcs_a", 0x1000, 0x0800, 0x631ebb5a);
            ROM_LOAD("mcs_c", 0x1800, 0x0800, 0x24cfd145);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_mooncrsg = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("epr194", 0x0000, 0x0800, 0x0e5582b1);
            ROM_LOAD("epr195", 0x0800, 0x0800, 0x12cb201b);
            ROM_LOAD("epr196", 0x1000, 0x0800, 0x18255614);
            ROM_LOAD("epr197", 0x1800, 0x0800, 0x05ac1466);
            ROM_LOAD("epr198", 0x2000, 0x0800, 0xc28a2e8f);
            ROM_LOAD("epr199", 0x2800, 0x0800, 0x5a4571de);
            ROM_LOAD("epr200", 0x3000, 0x0800, 0xb7c85bf1);
            ROM_LOAD("epr201", 0x3800, 0x0800, 0x2caba07f);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("epr203", 0x0000, 0x0800, 0xbe26b561);
            ROM_LOAD("mcs_d", 0x0800, 0x0800, 0x13932a15);
            ROM_LOAD("epr202", 0x1000, 0x0800, 0x26c7e800);
            ROM_LOAD("mcs_c", 0x1800, 0x0800, 0x24cfd145);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_smooncrs = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("927", 0x0000, 0x0800, 0x55c5b994);
            ROM_LOAD("928a", 0x0800, 0x0800, 0x77ae26d3);
            ROM_LOAD("929", 0x1000, 0x0800, 0x716eaa10);
            ROM_LOAD("930", 0x1800, 0x0800, 0xcea864f2);
            ROM_LOAD("931", 0x2000, 0x0800, 0x702c5f51);
            ROM_LOAD("932a", 0x2800, 0x0800, 0xe6a2039f);
            ROM_LOAD("933", 0x3000, 0x0800, 0x73783cee);
            ROM_LOAD("934", 0x3800, 0x0800, 0xc1a14aa2);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("epr203", 0x0000, 0x0800, 0xbe26b561);
            ROM_LOAD("mcs_d", 0x0800, 0x0800, 0x13932a15);
            ROM_LOAD("epr202", 0x1000, 0x0800, 0x26c7e800);
            ROM_LOAD("mcs_c", 0x1800, 0x0800, 0x24cfd145);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_mooncrsb = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("bepr194", 0x0000, 0x0800, 0x6a23ec6d);
            ROM_LOAD("bepr195", 0x0800, 0x0800, 0xee262ff2);
            ROM_LOAD("f03.bin", 0x1000, 0x0800, 0x29a2b0ab);
            ROM_LOAD("f04.bin", 0x1800, 0x0800, 0x4c6a5a6d);
            ROM_LOAD("e5", 0x2000, 0x0800, 0x06d378a6);
            ROM_LOAD("bepr199", 0x2800, 0x0800, 0x6e84a927);
            ROM_LOAD("e7", 0x3000, 0x0800, 0xb45af1e8);
            ROM_LOAD("bepr201", 0x3800, 0x0800, 0x66da55d5);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("epr203", 0x0000, 0x0800, 0xbe26b561);
            ROM_LOAD("mcs_d", 0x0800, 0x0800, 0x13932a15);
            ROM_LOAD("epr202", 0x1000, 0x0800, 0x26c7e800);
            ROM_LOAD("mcs_c", 0x1800, 0x0800, 0x24cfd145);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_mooncrs2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("f8.bin", 0x0000, 0x0800, 0xd36003e5);
            ROM_LOAD("bepr195", 0x0800, 0x0800, 0xee262ff2);
            ROM_LOAD("f03.bin", 0x1000, 0x0800, 0x29a2b0ab);
            ROM_LOAD("f04.bin", 0x1800, 0x0800, 0x4c6a5a6d);
            ROM_LOAD("e5", 0x2000, 0x0800, 0x06d378a6);
            ROM_LOAD("bepr199", 0x2800, 0x0800, 0x6e84a927);
            ROM_LOAD("e7", 0x3000, 0x0800, 0xb45af1e8);
            ROM_LOAD("m7.bin", 0x3800, 0x0800, 0x957ee078);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("1h_1_10.bin", 0x0000, 0x0800, 0x528da705);
            ROM_LOAD("12.chr", 0x0800, 0x0200, 0x5a4b17ea);
            ROM_CONTINUE(0x0c00, 0x0200);/* this version of the gfx ROMs has two */
            ROM_CONTINUE(0x0a00, 0x0200);/* groups of 16 sprites swapped */
            ROM_CONTINUE(0x0e00, 0x0200);
            ROM_LOAD("1k_1_11.bin", 0x1000, 0x0800, 0x4e79ff6b);
            ROM_LOAD("11.chr", 0x1800, 0x0200, 0xe0edccbd);
            ROM_CONTINUE(0x1c00, 0x0200);
            ROM_CONTINUE(0x1a00, 0x0200);
            ROM_CONTINUE(0x1e00, 0x0200);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_fantazia = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("f01.bin", 0x0000, 0x0800, 0xd3e23863);
            ROM_LOAD("f02.bin", 0x0800, 0x0800, 0x63fa4149);
            ROM_LOAD("f03.bin", 0x1000, 0x0800, 0x29a2b0ab);
            ROM_LOAD("f04.bin", 0x1800, 0x0800, 0x4c6a5a6d);
            ROM_LOAD("f09.bin", 0x2000, 0x0800, 0x75fd5ca1);
            ROM_LOAD("f10.bin", 0x2800, 0x0800, 0xe4da2dd4);
            ROM_LOAD("f11.bin", 0x3000, 0x0800, 0x42869646);
            ROM_LOAD("f12.bin", 0x3800, 0x0800, 0xa48d7fb0);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("1h_1_10.bin", 0x0000, 0x0800, 0x528da705);
            ROM_LOAD("mcs_d", 0x0800, 0x0800, 0x13932a15);
            ROM_LOAD("1k_1_11.bin", 0x1000, 0x0800, 0x4e79ff6b);
            ROM_LOAD("mcs_c", 0x1800, 0x0800, 0x24cfd145);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            /* this PROM was bad (bit 3 always set). I tried to "fix" it to get more reasonable */
 /* colors, but it should not be considered correct. It's a bootleg anyway. */
            ROM_LOAD("6l_prom.bin", 0x0000, 0x0020, BADCRC(0xf5381d3e));
            ROM_END();
        }
    };

    static RomLoadPtr rom_eagle = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("e1", 0x0000, 0x0800, 0x224c9526);
            ROM_LOAD("e2", 0x0800, 0x0800, 0xcc538ebd);
            ROM_LOAD("f03.bin", 0x1000, 0x0800, 0x29a2b0ab);
            ROM_LOAD("f04.bin", 0x1800, 0x0800, 0x4c6a5a6d);
            ROM_LOAD("e5", 0x2000, 0x0800, 0x06d378a6);
            ROM_LOAD("e6", 0x2800, 0x0800, 0x0dea20d5);
            ROM_LOAD("e7", 0x3000, 0x0800, 0xb45af1e8);
            ROM_LOAD("e8", 0x3800, 0x0800, 0xc437a876);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("e10", 0x0000, 0x0800, 0x40ce58bf);
            ROM_LOAD("e12", 0x0800, 0x0200, 0x628fdeed);
            ROM_CONTINUE(0x0c00, 0x0200);/* this version of the gfx ROMs has two */
            ROM_CONTINUE(0x0a00, 0x0200);/* groups of 16 sprites swapped */
            ROM_CONTINUE(0x0e00, 0x0200);
            ROM_LOAD("e9", 0x1000, 0x0800, 0xba664099);
            ROM_LOAD("e11", 0x1800, 0x0200, 0xee4ec5fd);
            ROM_CONTINUE(0x1c00, 0x0200);
            ROM_CONTINUE(0x1a00, 0x0200);
            ROM_CONTINUE(0x1e00, 0x0200);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_eagle2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("e1.7f", 0x0000, 0x0800, 0x45aab7a3);
            ROM_LOAD("e2", 0x0800, 0x0800, 0xcc538ebd);
            ROM_LOAD("f03.bin", 0x1000, 0x0800, 0x29a2b0ab);
            ROM_LOAD("f04.bin", 0x1800, 0x0800, 0x4c6a5a6d);
            ROM_LOAD("e5", 0x2000, 0x0800, 0x06d378a6);
            ROM_LOAD("e6.6", 0x2800, 0x0800, 0x9f09f8c6);
            ROM_LOAD("e7", 0x3000, 0x0800, 0xb45af1e8);
            ROM_LOAD("e8", 0x3800, 0x0800, 0xc437a876);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("e10.2", 0x0000, 0x0800, 0x25b38ebd);
            ROM_LOAD("e12", 0x0800, 0x0200, 0x628fdeed);
            ROM_CONTINUE(0x0c00, 0x0200);/* this version of the gfx ROMs has two */
            ROM_CONTINUE(0x0a00, 0x0200);/* groups of 16 sprites swapped */
            ROM_CONTINUE(0x0e00, 0x0200);
            ROM_LOAD("e9", 0x1000, 0x0800, 0xba664099);
            ROM_LOAD("e11", 0x1800, 0x0200, 0xee4ec5fd);
            ROM_CONTINUE(0x1c00, 0x0200);
            ROM_CONTINUE(0x1a00, 0x0200);
            ROM_CONTINUE(0x1e00, 0x0200);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("l06_prom.bin", 0x0000, 0x0020, 0x6a0c7d87);
            ROM_END();
        }
    };

    static RomLoadPtr rom_moonqsr = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1, 0);/* 64k for code + 64k for decrypted opcodes */
            ROM_LOAD("mq1", 0x0000, 0x0800, 0x132c13ec);
            ROM_LOAD("mq2", 0x0800, 0x0800, 0xc8eb74f1);
            ROM_LOAD("mq3", 0x1000, 0x0800, 0x33965a89);
            ROM_LOAD("mq4", 0x1800, 0x0800, 0xa3861d17);
            ROM_LOAD("mq5", 0x2000, 0x0800, 0x8bcf9c67);
            ROM_LOAD("mq6", 0x2800, 0x0800, 0x5750cda9);
            ROM_LOAD("mq7", 0x3000, 0x0800, 0x78d7fe5b);
            ROM_LOAD("mq8", 0x3800, 0x0800, 0x4919eed5);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("mqb", 0x0000, 0x0800, 0xb55ec806);
            ROM_LOAD("mqd", 0x0800, 0x0800, 0x9e7d0e13);
            ROM_LOAD("mqa", 0x1000, 0x0800, 0x66eee0db);
            ROM_LOAD("mqc", 0x1800, 0x0800, 0xa6db5b0d);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("vid_e6.bin", 0x0000, 0x0020, 0x0b878b54);
            ROM_END();
        }
    };

    static RomLoadPtr rom_moonal2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("ali1", 0x0000, 0x0400, 0x0dcecab4);
            ROM_LOAD("ali2", 0x0400, 0x0400, 0xc6ee75a7);
            ROM_LOAD("ali3", 0x0800, 0x0400, 0xcd1be7e9);
            ROM_LOAD("ali4", 0x0c00, 0x0400, 0x83b03f08);
            ROM_LOAD("ali5", 0x1000, 0x0400, 0x6f3cf61d);
            ROM_LOAD("ali6", 0x1400, 0x0400, 0xe169d432);
            ROM_LOAD("ali7", 0x1800, 0x0400, 0x41f64b73);
            ROM_LOAD("ali8", 0x1c00, 0x0400, 0xf72ee876);
            ROM_LOAD("ali9", 0x2000, 0x0400, 0xb7fb763c);
            ROM_LOAD("ali10", 0x2400, 0x0400, 0xb1059179);
            ROM_LOAD("ali11", 0x2800, 0x0400, 0x9e79a1c6);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("ali13.1h", 0x0000, 0x0800, 0xa1287bf6);
            ROM_RELOAD(0x0800, 0x0800);
            ROM_LOAD("ali12.1k", 0x1000, 0x0800, 0x528f1481);
            ROM_RELOAD(0x1800, 0x0800);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("6l.bpr", 0x0000, 0x0020, 0xc3ac9467);
            ROM_END();
        }
    };

    static RomLoadPtr rom_moonal2b = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("ali1", 0x0000, 0x0400, 0x0dcecab4);
            ROM_LOAD("ali2", 0x0400, 0x0400, 0xc6ee75a7);
            ROM_LOAD("md-2", 0x0800, 0x0800, 0x8318b187);
            ROM_LOAD("ali5", 0x1000, 0x0400, 0x6f3cf61d);
            ROM_LOAD("ali6", 0x1400, 0x0400, 0xe169d432);
            ROM_LOAD("ali7", 0x1800, 0x0400, 0x41f64b73);
            ROM_LOAD("ali8", 0x1c00, 0x0400, 0xf72ee876);
            ROM_LOAD("ali9", 0x2000, 0x0400, 0xb7fb763c);
            ROM_LOAD("ali10", 0x2400, 0x0400, 0xb1059179);
            ROM_LOAD("md-6", 0x2800, 0x0800, 0x9cc973e0);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("ali13.1h", 0x0000, 0x0800, 0xa1287bf6);
            ROM_RELOAD(0x0800, 0x0800);
            ROM_LOAD("ali12.1k", 0x1000, 0x0800, 0x528f1481);
            ROM_RELOAD(0x1800, 0x0800);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("6l.bpr", 0x0000, 0x0020, 0xc3ac9467);
            ROM_END();
        }
    };

    static RomLoadPtr rom_kingball = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("prg1.7f", 0x0000, 0x1000, 0x6cb49046);
            ROM_LOAD("prg2.7j", 0x1000, 0x1000, 0xc223b416);
            ROM_LOAD("prg3.7l", 0x2000, 0x0800, 0x453634c0);
            ROM_REGION(0x10000, REGION_CPU2, 0);/* 64k for sound code */
            ROM_LOAD("kbe1.ic4", 0x0000, 0x0800, 0x5be2c80a);
            ROM_LOAD("kbe2.ic5", 0x0800, 0x0800, 0xbb59e965);
            ROM_LOAD("kbe3.ic6", 0x1000, 0x0800, 0x1c94dd31);
            ROM_LOAD("kbe2.ic7", 0x1800, 0x0800, 0xbb59e965);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("chg1.1h", 0x0000, 0x0800, 0x9cd550e7);
            ROM_RELOAD(0x0800, 0x0800);
            ROM_LOAD("chg2.1k", 0x1000, 0x0800, 0xa206757d);
            ROM_RELOAD(0x1800, 0x0800);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("kb2-1", 0x0000, 0x0020, 0x15dd5b16);
            ROM_END();
        }
    };

    static RomLoadPtr rom_kingbalj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1, 0);/* 64k for code */
            ROM_LOAD("prg1.7f", 0x0000, 0x1000, 0x6cb49046);
            ROM_LOAD("prg2.7j", 0x1000, 0x1000, 0xc223b416);
            ROM_LOAD("prg3.7l", 0x2000, 0x0800, 0x453634c0);
            ROM_REGION(0x10000, REGION_CPU2, 0);/* 64k for sound code */
            ROM_LOAD("kbj1.ic4", 0x0000, 0x0800, 0xba16beb7);
            ROM_LOAD("kbj2.ic5", 0x0800, 0x0800, 0x56686a63);
            ROM_LOAD("kbj3.ic6", 0x1000, 0x0800, 0xfbc570a5);
            ROM_LOAD("kbj2.ic7", 0x1800, 0x0800, 0x56686a63);
            ROM_REGION(0x2000, REGION_GFX1, ROMREGION_DISPOSE);
            ROM_LOAD("chg1.1h", 0x0000, 0x0800, 0x9cd550e7);
            ROM_RELOAD(0x0800, 0x0800);
            ROM_LOAD("chg2.1k", 0x1000, 0x0800, 0xa206757d);
            ROM_RELOAD(0x1800, 0x0800);
            ROM_REGION(0x0020, REGION_PROMS, 0);
            ROM_LOAD("kb2-1", 0x0000, 0x0020, 0x15dd5b16);
            ROM_END();
        }
    };

    public static InitDriverPtr init_pisces = new InitDriverPtr() {
        public void handler() {
            /* the coin lockout was replaced */
            install_mem_write_handler(0, 0x6002, 0x6002, pisces_gfxbank_w);
        }
    };

    public static InitDriverPtr init_checkmaj = new InitDriverPtr() {
        public void handler() {
            /* for the title screen */
            install_mem_read_handler(0, 0x3800, 0x3800, checkmaj_protection_r);
        }
    };

    public static InitDriverPtr init_kingball = new InitDriverPtr() {
        public void handler() {
            install_mem_read_handler(0, 0xa000, 0xa000, kingball_IN0_r);
            install_mem_read_handler(0, 0xa800, 0xa800, kingball_IN1_r);
        }
    };

    static int/*unsigned char*/ decode(int data, int addr) {
        int res;

        res = data;
        if ((data & 0x02) != 0) {
            res ^= 0x40;
        }
        if ((data & 0x20) != 0) {
            res ^= 0x04;
        }
        if ((addr & 1) == 0) {
            res = (res & 0xbb) | ((res & 0x40) >> 4) | ((res & 0x04) << 4);
        }
        return res & 0xFF;
    }

    public static InitDriverPtr init_mooncrst = new InitDriverPtr() {
        public void handler() {
            int A;
            UBytePtr rom = memory_region(REGION_CPU1);

            for (A = 0; A < 0x10000; A++) {
                rom.write(A, decode(rom.read(A), A));
            }
        }
    };

    public static InitDriverPtr init_mooncrgx = new InitDriverPtr() {
        public void handler() {
            init_mooncrst.handler();

            install_mem_write_handler(0, 0x6000, 0x6002, mooncrgx_gfxextend_w);
        }
    };

    public static InitDriverPtr init_moonal2 = new InitDriverPtr() {
        public void handler() {
            install_mem_write_handler(0, 0xa000, 0xa002, MWA_NOP);
        }
    };

    public static InitDriverPtr init_moonqsr = new InitDriverPtr() {
        public void handler() {
            int A;
            UBytePtr rom = memory_region(REGION_CPU1);
            int diff = memory_region_length(REGION_CPU1) / 2;

            memory_set_opcode_base(0, new UBytePtr(rom, diff));

            for (A = 0; A < 0x10000; A++) {
                rom.write(A + diff, decode(rom.read(A), A));
            }
        }
    };

    public static InitDriverPtr init_checkman = new InitDriverPtr() {
        public void handler() {
            int A;
            int data_xor = 0;
            UBytePtr rom = memory_region(REGION_CPU1);

            for (A = 0; A < 0x2800; A++) {
                switch (A & 0x07) {
                    case 0:
                        data_xor = (rom.read(A) & 0x40) >> 6;
                        break;
                    case 1:
                        data_xor = (rom.read(A) & 0x20) >> 4;
                        break;
                    case 2:
                        data_xor = ((rom.read(A) & 0x10) >> 2) | ((rom.read(A) & 0x40) >> 5);
                        break;
                    case 3:
                        data_xor = ((rom.read(A) & 0x04) << 2) | ((rom.read(A) & 0x20) >> 5);
                        break;
                    case 4:
                        data_xor = ((rom.read(A) & 0x10) << 2) | ((rom.read(A) & 0x02) << 4);
                        break;
                    case 5:
                        data_xor = ((rom.read(A) & 0x01) << 6) | ((rom.read(A) & 0x04) << 3);
                        break;
                    case 6:
                        data_xor = (rom.read(A) & 0x01) << 2;
                        break;
                    case 7:
                        data_xor = (rom.read(A) & 0x02) << 3;
                        break;
                }
                rom.xor(A, data_xor);
            }
        }
    };

    public static GameDriver driver_galaxian = new GameDriver("1979", "galaxian", "galaxian.java", rom_galaxian, null, machine_driver_galaxian, input_ports_galaxian, null, ROT90, "Namco", "Galaxian (Namco set 1)");
    public static GameDriver driver_galaxiaj = new GameDriver("1979", "galaxiaj", "galaxian.java", rom_galaxiaj, driver_galaxian, machine_driver_galaxian, input_ports_superg, null, ROT90, "Namco", "Galaxian (Namco set 2)");
    public static GameDriver driver_galmidw = new GameDriver("1979", "galmidw", "galaxian.java", rom_galmidw, driver_galaxian, machine_driver_galaxian, input_ports_galaxian, null, ROT90, "[Namco] (Midway license)", "Galaxian (Midway)");
    public static GameDriver driver_superg = new GameDriver("1979", "superg", "galaxian.java", rom_superg, driver_galaxian, machine_driver_galaxian, input_ports_superg, null, ROT90, "hack", "Super Galaxians");
    public static GameDriver driver_galapx = new GameDriver("1979", "galapx", "galaxian.java", rom_galapx, driver_galaxian, machine_driver_galaxian, input_ports_superg, null, ROT90, "hack", "Galaxian Part X");
    public static GameDriver driver_galap1 = new GameDriver("1979", "galap1", "galaxian.java", rom_galap1, driver_galaxian, machine_driver_galaxian, input_ports_superg, null, ROT90, "hack", "Space Invaders Galactica");
    public static GameDriver driver_galap4 = new GameDriver("1979", "galap4", "galaxian.java", rom_galap4, driver_galaxian, machine_driver_galaxian, input_ports_superg, null, ROT90, "hack", "Galaxian Part 4");
    public static GameDriver driver_galturbo = new GameDriver("1979", "galturbo", "galaxian.java", rom_galturbo, driver_galaxian, machine_driver_galaxian, input_ports_superg, null, ROT90, "hack", "Galaxian Turbo");
    public static GameDriver driver_swarm = new GameDriver("1979", "swarm", "galaxian.java", rom_swarm, driver_galaxian, machine_driver_galaxian, input_ports_swarm, null, ROT90, "hack", "Swarm");
    public static GameDriver driver_zerotime = new GameDriver("1979", "zerotime", "galaxian.java", rom_zerotime, driver_galaxian, machine_driver_galaxian, input_ports_zerotime, null, ROT90, "Petaco S.A.", "Zero Time");
    public static GameDriver driver_pisces = new GameDriver("19??", "pisces", "galaxian.java", rom_pisces, null, machine_driver_pisces, input_ports_pisces, init_pisces, ROT90, "<unknown>", "Pisces");
    public static GameDriver driver_uniwars = new GameDriver("1980", "uniwars", "galaxian.java", rom_uniwars, null, machine_driver_pisces, input_ports_superg, init_pisces, ROT90, "Irem", "UniWar S");
    public static GameDriver driver_gteikoku = new GameDriver("1980", "gteikoku", "galaxian.java", rom_gteikoku, driver_uniwars, machine_driver_pisces, input_ports_superg, init_pisces, ROT90, "Irem", "Gingateikoku No Gyakushu");
    public static GameDriver driver_gteikokb = new GameDriver("1980", "gteikokb", "galaxian.java", rom_gteikokb, driver_uniwars, machine_driver_pisces, input_ports_gteikokb, init_pisces, ROT270, "bootleg", "Gingateikoku No Gyakushu (bootleg)");
    public static GameDriver driver_spacbatt = new GameDriver("1980", "spacbatt", "galaxian.java", rom_spacbatt, driver_uniwars, machine_driver_pisces, input_ports_superg, init_pisces, ROT90, "bootleg", "Space Battle");
    /*TODO*///    public static GameDriver driver_batman2 = new GameDriver("1980", "batman2", "galaxian.java", rom_batman2, driver_phoenix, machine_driver_batman2, input_ports_batman2, init_pisces, ROT270, "bootleg", "Batman Part 2");
    public static GameDriver driver_warofbug = new GameDriver("1981", "warofbug", "galaxian.java", rom_warofbug, null, machine_driver_galaxian, input_ports_warofbug, init_pisces, ROT90, "Armenia", "War of the Bugs or Monsterous Manouvers in a Mushroom Maze");
    public static GameDriver driver_redufo = new GameDriver("19??", "redufo", "galaxian.java", rom_redufo, null, machine_driver_galaxian, input_ports_redufo, init_pisces, ROT90, "bootleg", "Defend the Terra Attack on the Red UFO (bootleg)");
    public static GameDriver driver_exodus = new GameDriver("19??", "exodus", "galaxian.java", rom_exodus, driver_redufo, machine_driver_galaxian, input_ports_exodus, init_pisces, ROT90, "Subelectro", "Exodus (bootleg?)");
    public static GameDriver driver_streakng = new GameDriver("1980", "streakng", "galaxian.java", rom_streakng, null, machine_driver_pacmanbl, input_ports_streakng, null, ROT90, "Shoei", "Streaking", GAME_IMPERFECT_COLORS);
    public static GameDriver driver_ghostmun = new GameDriver("1981", "ghostmun", "galaxian.java", rom_ghostmun, driver_pacman, machine_driver_pacmanbl, input_ports_streakng, null, ROT90, "bootleg", "Ghost Muncher", GAME_IMPERFECT_COLORS);
    public static GameDriver driver_pacmanbl = new GameDriver("1981", "pacmanbl", "galaxian.java", rom_pacmanbl, driver_pacman, machine_driver_pacmanbl, input_ports_pacmanbl, init_pisces, ROT270, "bootleg", "Pac-Man (bootleg on Galaxian hardware)");
    public static GameDriver driver_devilfsg = new GameDriver("1984", "devilfsg", "galaxian.java", rom_devilfsg, driver_devilfsh, machine_driver_devilfsg, input_ports_devilfsg, null, ROT270, "Vision / Artic", "Devil Fish (Galaxian hardware, bootleg?)");
    public static GameDriver driver_zigzag = new GameDriver("1982", "zigzag", "galaxian.java", rom_zigzag, null, machine_driver_zigzag, input_ports_zigzag, null, ROT90, "LAX", "Zig Zag (Galaxian hardware, set 1)");
    public static GameDriver driver_zigzag2 = new GameDriver("1982", "zigzag2", "galaxian.java", rom_zigzag2, driver_zigzag, machine_driver_zigzag, input_ports_zigzag, null, ROT90, "LAX", "Zig Zag (Galaxian hardware, set 2)");
    public static GameDriver driver_scramblb = new GameDriver("1981", "scramblb", "galaxian.java", rom_scramblb, driver_scramble, machine_driver_scramblb, input_ports_scramblb, null, ROT90, "bootleg", "Scramble (bootleg on Galaxian hardware)");
    public static GameDriver driver_jumpbug = new GameDriver("1981", "jumpbug", "galaxian.java", rom_jumpbug, null, machine_driver_jumpbug, input_ports_jumpbug, null, ROT90, "Rock-ola", "Jump Bug");
    public static GameDriver driver_jumpbugb = new GameDriver("1981", "jumpbugb", "galaxian.java", rom_jumpbugb, driver_jumpbug, machine_driver_jumpbug, input_ports_jumpbug, null, ROT90, "bootleg", "Jump Bug (bootleg)");
    public static GameDriver driver_levers = new GameDriver("1983", "levers", "galaxian.java", rom_levers, null, machine_driver_jumpbug, input_ports_levers, null, ROT90, "Rock-ola", "Levers");
    public static GameDriver driver_azurian = new GameDriver("1982", "azurian", "galaxian.java", rom_azurian, null, machine_driver_galaxian, input_ports_azurian, init_pisces, ROT90, "Rait Electronics Ltd", "Azurian Attack");
    public static GameDriver driver_orbitron = new GameDriver("19??", "orbitron", "galaxian.java", rom_orbitron, null, machine_driver_galaxian, input_ports_orbitron, init_pisces, ROT270, "Signatron USA", "Orbitron");
    public static GameDriver driver_checkman = new GameDriver("1982", "checkman", "galaxian.java", rom_checkman, null, machine_driver_checkman, input_ports_checkman, init_checkman, ROT90, "Zilec-Zenitone", "Checkman");
    public static GameDriver driver_checkmaj = new GameDriver("1982", "checkmaj", "galaxian.java", rom_checkmaj, driver_checkman, machine_driver_checkmaj, input_ports_checkmaj, init_checkmaj, ROT90, "Jaleco", "Checkman (Japan)");
    public static GameDriver driver_blkhole = new GameDriver("19??", "blkhole", "galaxian.java", rom_blkhole, null, machine_driver_galaxian, input_ports_blkhole, null, ROT90, "TDS", "Black Hole");
    public static GameDriver driver_mooncrst = new GameDriver("1980", "mooncrst", "galaxian.java", rom_mooncrst, null, machine_driver_mooncrst, input_ports_mooncrst, init_mooncrst, ROT90, "Nichibutsu", "Moon Cresta (Nichibutsu)");
    public static GameDriver driver_mooncrsg = new GameDriver("1980", "mooncrsg", "galaxian.java", rom_mooncrsg, driver_mooncrst, machine_driver_mooncrst, input_ports_mooncrst, null, ROT90, "Gremlin", "Moon Cresta (Gremlin)");
    public static GameDriver driver_smooncrs = new GameDriver("1980?", "smooncrs", "galaxian.java", rom_smooncrs, driver_mooncrst, machine_driver_mooncrst, input_ports_mooncrst, null, ROT90, "Gremlin", "Super Moon Cresta");
    public static GameDriver driver_mooncrsb = new GameDriver("1980", "mooncrsb", "galaxian.java", rom_mooncrsb, driver_mooncrst, machine_driver_mooncrst, input_ports_mooncrst, null, ROT90, "bootleg", "Moon Cresta (bootleg set 1)");
    public static GameDriver driver_mooncrs2 = new GameDriver("1980", "mooncrs2", "galaxian.java", rom_mooncrs2, driver_mooncrst, machine_driver_mooncrst, input_ports_mooncrst, null, ROT90, "Nichibutsu", "Moon Cresta (bootleg set 2)");
    public static GameDriver driver_fantazia = new GameDriver("1980", "fantazia", "galaxian.java", rom_fantazia, driver_mooncrst, machine_driver_mooncrst, input_ports_mooncrst, null, ROT90, "bootleg", "Fantazia", GAME_IMPERFECT_COLORS);
    public static GameDriver driver_eagle = new GameDriver("1980", "eagle", "galaxian.java", rom_eagle, driver_mooncrst, machine_driver_mooncrst, input_ports_eagle, null, ROT90, "Centuri", "Eagle (set 1)");
    public static GameDriver driver_eagle2 = new GameDriver("1980", "eagle2", "galaxian.java", rom_eagle2, driver_mooncrst, machine_driver_mooncrst, input_ports_eagle2, null, ROT90, "Centuri", "Eagle (set 2)");
    public static GameDriver driver_mooncrgx = new GameDriver("1980", "mooncrgx", "galaxian.java", rom_mooncrgx, driver_mooncrst, machine_driver_mooncrgx, input_ports_mooncrgx, init_mooncrgx, ROT270, "bootleg", "Moon Cresta (bootleg on Galaxian hardware)");
    public static GameDriver driver_moonqsr = new GameDriver("1980", "moonqsr", "galaxian.java", rom_moonqsr, null, machine_driver_moonqsr, input_ports_moonqsr, init_moonqsr, ROT90, "Nichibutsu", "Moon Quasar");
    public static GameDriver driver_moonal2 = new GameDriver("1980", "moonal2", "galaxian.java", rom_moonal2, null, machine_driver_mooncrst, input_ports_moonal2, init_moonal2, ROT90, "Nichibutsu", "Moon Alien Part 2");
    public static GameDriver driver_moonal2b = new GameDriver("1980", "moonal2b", "galaxian.java", rom_moonal2b, driver_moonal2, machine_driver_mooncrst, input_ports_moonal2, init_moonal2, ROT90, "Nichibutsu", "Moon Alien Part 2 (older version)");
    public static GameDriver driver_kingball = new GameDriver("1980", "kingball", "galaxian.java", rom_kingball, null, machine_driver_kingball, input_ports_kingball, init_kingball, ROT90, "Namco", "King & Balloon (US)");
    public static GameDriver driver_kingbalj = new GameDriver("1980", "kingbalj", "galaxian.java", rom_kingbalj, driver_kingball, machine_driver_kingball, input_ports_kingball, init_kingball, ROT90, "Namco", "King & Balloon (Japan)");
}
