package gr.codebb.arcadeflex.common.libc;

import static common.ptr.*;

public class cstring {

    public static void memcpy(char[] dst, UBytePtr src, int size) {
        for (int i = 0; i < Math.min(size, src.memory.length); i++) {
            dst[i] = src.read(i);
        }
    }
}
