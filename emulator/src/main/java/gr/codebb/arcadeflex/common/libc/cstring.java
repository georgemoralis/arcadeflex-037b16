package gr.codebb.arcadeflex.common.libc;

import static common.ptr.*;

public class cstring {

    public static void memcpy(char[] dst, UBytePtr src, int size) {
        for (int i = 0; i < Math.min(size, src.memory.length); i++) {
            dst[i] = src.read(i);
        }
    }

    public static void memcpy(UBytePtr dst, char[] src, int size) {
        for (int i = 0; i < Math.min(size, src.length); i++) {
            dst.write(i, src[i]);
        }
    }
    
        public static int strstr(String x, String y)
    {
            // if x is null or if x's length is less than that of y's
            if (x == null || y.length() > x.length()) {
                    return -1;
            }

            // if y is null or is empty
            if (y == null || y.length() == 0) {
                    return 0;
            }

            for (int i = 0; i <= x.length() - y.length(); i++)
            {
                    int j;
                    for (j = 0; j < y.length(); j++) {
                            if (y.charAt(j) != x.charAt(i + j)) {
                                    break;
                            }
                    }

                    if (j == y.length()) {
                            return i;
                    }
            }

            return -1;
    }
}
