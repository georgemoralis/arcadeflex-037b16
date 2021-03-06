package gr.codebb.arcadeflex.v037b16.generic;

import static common.ptr.*;
import static gr.codebb.arcadeflex.v037b16.mame.osdependH.*;
import static gr.codebb.arcadeflex.v037b16.mame.sndintrfH.*;

public class fucPtr {

    public static abstract interface ReadHandlerPtr {

        public abstract int handler(int offset);
    }

    public static abstract interface WriteHandlerPtr {

        public abstract void handler(int offset, int data);
    }

    public static abstract interface InitMachinePtr {

        public abstract void handler();
    }

    public static abstract interface InitDriverPtr {

        public abstract void handler();
    }

    public static abstract interface InterruptPtr {

        public abstract int handler();
    }

    public static abstract interface VhConvertColorPromPtr {

        public abstract void handler(char[] palette, char[] colortable, UBytePtr color_prom);
    }

    public static abstract interface VhEofCallbackPtr {

        public abstract void handler();
    }

    public static abstract interface VhStartPtr {

        public abstract int handler();
    }

    public static abstract interface VhStopPtr {

        public abstract void handler();
    }

    public static abstract interface VhUpdatePtr {

        public abstract void handler(osd_bitmap bitmap, int full_refresh);
    }

    public static abstract interface ShStartPtr {

        public abstract int handler(MachineSound msound);
    }

    public static abstract interface ShStopPtr {

        public abstract void handler();
    }

    public static abstract interface ShUpdatePtr {

        public abstract void handler();
    }

    public static abstract interface RomLoadPtr {

        public abstract void handler();
    }

    public static abstract interface InputPortPtr {

        public abstract void handler();
    }

    public static abstract interface nvramPtr {

        public abstract void handler(Object file, int read_or_write);
    };

    public static abstract interface WriteYmHandlerPtr {

        public abstract void handler(int linestate);
    }
}
