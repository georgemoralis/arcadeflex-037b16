/**
 * ported to v0.37b16
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.WIP.v037b16.machine;

//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.memory.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
//to be organized
import static common.ptr.*;

public class konami {

    static /*unsigned*/ char decodebyte(char/*unsigned char*/ opcode, char address) {
        /*unsigned*/ char xormask;

        xormask = 0;
        if ((address & 0x02) != 0) {
            xormask |= 0x80;
        } else {
            xormask |= 0x20;
        }
        if ((address & 0x08) != 0) {
            xormask |= 0x08;
        } else {
            xormask |= 0x02;
        }

        return (char) ((opcode ^ xormask) & 0xFF);
    }

    static void decode(int cpu) {
        UBytePtr rom = memory_region(REGION_CPU1 + cpu);
        int diff = memory_region_length(REGION_CPU1 + cpu) / 2;
        int A;

        memory_set_opcode_base(cpu, new UBytePtr(rom, diff));

        for (A = 0; A < diff; A++) {
            rom.write(A + diff, decodebyte(rom.read(A), (char) A));
        }
    }

    public static void konami1_decode() {
        decode(0);
    }

    public static void konami1_decode_cpu2() {
        decode(1);
    }
}
