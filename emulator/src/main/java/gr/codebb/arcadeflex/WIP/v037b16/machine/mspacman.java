/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.WIP.v037b16.machine;

//generic functions
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
//to be organized
import common.ptr.UBytePtr;

public class mspacman {

    static /*unsigned*/ char decryptd(/*unsigned*/char e) {
        /*unsigned*/ char d;

        d = (char) ((e & 0x80) >> 3);
        d |= (char) ((e & 0x40) >> 3);
        d |= (char) ((e & 0x20));
        d |= (char) ((e & 0x10) << 2);
        d |= (char) ((e & 0x08) >> 1);
        d |= (char) ((e & 0x04) >> 1);
        d |= (char) ((e & 0x02) >> 1);
        d |= (char) ((e & 0x01) << 7);

        return d;
    }

    static /*unsigned*/ int decrypta1(/*unsigned*/int e) {
        /*unsigned*/ int d;

        d = (e & 0x800);
        d |= (e & 0x400) >>> 7;
        d |= (e & 0x200) >>> 2;
        d |= (e & 0x100) << 1;
        d |= (e & 0x80) << 3;
        d |= (e & 0x40) << 2;
        d |= (e & 0x20) << 1;
        d |= (e & 0x10) << 1;
        d |= (e & 0x08) << 1;
        d |= (e & 0x04);
        d |= (e & 0x02);
        d |= (e & 0x01);

        return d;
    }

    static /*unsigned*/ int decrypta2(/*unsigned*/int e) {
        /*unsigned*/ int d;
        d = (e & 0x800);
        d |= (e & 0x400) >>> 2;
        d |= (e & 0x200) >>> 2;
        d |= (e & 0x100) >>> 3;
        d |= (e & 0x80) << 2;
        d |= (e & 0x40) << 4;
        d |= (e & 0x20) << 1;
        d |= (e & 0x10) >>> 1;
        d |= (e & 0x08) << 1;
        d |= (e & 0x04);
        d |= (e & 0x02);
        d |= (e & 0x01);

        return d;
    }

    public static void mspacman_decode() {
        int i;
        UBytePtr RAM;

        /* CPU ROMs */
        RAM = memory_region(REGION_CPU1);
        for (i = 0; i < 0x1000; i++) {
            RAM.write(0x10000 + i, RAM.read(0x0000 + i));
            RAM.write(0x11000 + i, RAM.read(0x1000 + i));
            RAM.write(0x12000 + i, RAM.read(0x2000 + i));
            RAM.write(0x1b000 + i, RAM.read(0x3000 + i));
            /*not needed but it's there*/

        }

        for (i = 0; i < 0x1000; i++) {
            RAM.write(decrypta1(i) + 0x13000, decryptd(RAM.read(0xb000 + i)));
            /*u7*/
            RAM.write(decrypta1(i) + 0x19000, decryptd(RAM.read(0x9000 + i)));
            /*u6*/
        }

        for (i = 0; i < 0x800; i++) {
            RAM.write(decrypta2(i) + 0x18000, decryptd(RAM.read(0x8000 + i)));
            /*u5*/
            RAM.write(0x18800 + i, RAM.read(0x19800 + i));
        }

        for (i = 0; i < 8; i++) {
            RAM.write(0x10410 + i, RAM.read(0x18008 + i));
            RAM.write(0x108E0 + i, RAM.read(0x181D8 + i));
            RAM.write(0x10A30 + i, RAM.read(0x18118 + i));
            RAM.write(0x10BD0 + i, RAM.read(0x180D8 + i));
            RAM.write(0x10C20 + i, RAM.read(0x18120 + i));
            RAM.write(0x10E58 + i, RAM.read(0x18168 + i));
            RAM.write(0x10EA8 + i, RAM.read(0x18198 + i));

            RAM.write(0x11000 + i, RAM.read(0x18020 + i));
            RAM.write(0x11008 + i, RAM.read(0x18010 + i));
            RAM.write(0x11288 + i, RAM.read(0x18098 + i));
            RAM.write(0x11348 + i, RAM.read(0x18048 + i));
            RAM.write(0x11688 + i, RAM.read(0x18088 + i));
            RAM.write(0x116B0 + i, RAM.read(0x18188 + i));
            RAM.write(0x116D8 + i, RAM.read(0x180C8 + i));
            RAM.write(0x116F8 + i, RAM.read(0x181C8 + i));
            RAM.write(0x119A8 + i, RAM.read(0x180A8 + i));
            RAM.write(0x119B8 + i, RAM.read(0x181A8 + i));

            RAM.write(0x12060 + i, RAM.read(0x18148 + i));
            RAM.write(0x12108 + i, RAM.read(0x18018 + i));
            RAM.write(0x121A0 + i, RAM.read(0x181A0 + i));
            RAM.write(0x12298 + i, RAM.read(0x180A0 + i));
            RAM.write(0x123E0 + i, RAM.read(0x180E8 + i));
            RAM.write(0x12418 + i, RAM.read(0x18000 + i));
            RAM.write(0x12448 + i, RAM.read(0x18058 + i));
            RAM.write(0x12470 + i, RAM.read(0x18140 + i));
            RAM.write(0x12488 + i, RAM.read(0x18080 + i));
            RAM.write(0x124B0 + i, RAM.read(0x18180 + i));
            RAM.write(0x124D8 + i, RAM.read(0x180C0 + i));
            RAM.write(0x124F8 + i, RAM.read(0x181C0 + i));
            RAM.write(0x12748 + i, RAM.read(0x18050 + i));
            RAM.write(0x12780 + i, RAM.read(0x18090 + i));
            RAM.write(0x127B8 + i, RAM.read(0x18190 + i));
            RAM.write(0x12800 + i, RAM.read(0x18028 + i));
            RAM.write(0x12B20 + i, RAM.read(0x18100 + i));
            RAM.write(0x12B30 + i, RAM.read(0x18110 + i));
            RAM.write(0x12BF0 + i, RAM.read(0x181D0 + i));
            RAM.write(0x12CC0 + i, RAM.read(0x180D0 + i));
            RAM.write(0x12CD8 + i, RAM.read(0x180E0 + i));
            RAM.write(0x12CF0 + i, RAM.read(0x181E0 + i));
            RAM.write(0x12D60 + i, RAM.read(0x18160 + i));
        }

    }

    /*These won't be used in normal operation*/
    public static ReadHandlerPtr mspacman_deactivate_rom1 = new ReadHandlerPtr() {
        public int handler(int offset) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            cpu_setbank(1, new UBytePtr(RAM, 0x00000));
            return RAM.read(offset + 0x0038);
        }
    };

    public static ReadHandlerPtr mspacman_deactivate_rom2 = new ReadHandlerPtr() {
        public int handler(int offset) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            cpu_setbank(1, new UBytePtr(RAM, 0x00000));
            return RAM.read(offset + 0x03b0);
        }
    };

    public static ReadHandlerPtr mspacman_deactivate_rom3 = new ReadHandlerPtr() {
        public int handler(int offset) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            cpu_setbank(1, new UBytePtr(RAM, 0x00000));
            return RAM.read(offset + 0x1600);
        }
    };

    public static ReadHandlerPtr mspacman_deactivate_rom4 = new ReadHandlerPtr() {
        public int handler(int offset) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            cpu_setbank(1, new UBytePtr(RAM, 0x00000));
            return RAM.read(offset + 0x2120);
        }
    };

    public static ReadHandlerPtr mspacman_deactivate_rom5 = new ReadHandlerPtr() {
        public int handler(int offset) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            cpu_setbank(1, new UBytePtr(RAM, 0x00000));
            return RAM.read(offset + 0x3ff0);
        }
    };

    public static ReadHandlerPtr mspacman_deactivate_rom6 = new ReadHandlerPtr() {
        public int handler(int offset) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            cpu_setbank(1, new UBytePtr(RAM, 0x00000));
            return RAM.read(offset + 0x8000);
        }
    };

    public static WriteHandlerPtr mspacman_activate_rom = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            if (data == 1) {
                cpu_setbank(1, new UBytePtr(RAM, 0x10000));
            }
        }
    };

}
