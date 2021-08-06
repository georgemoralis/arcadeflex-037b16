package gr.codebb.arcadeflex.v037b16.machine;

public class _7474H {

    public static final int MAX_TTL7474 = 4;

    public static abstract interface sh_7474_callbackPtr {

        public abstract void handler();
    }

    /* The interface structure */
    public static class TTL7474_interface {

        public static sh_7474_callbackPtr output_changed_cb;

        public TTL7474_interface(sh_7474_callbackPtr sh_7474_callback) {
            output_changed_cb = sh_7474_callback;
        }
    }
}
