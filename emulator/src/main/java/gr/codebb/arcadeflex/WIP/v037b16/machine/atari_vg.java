/**
 * ported to v0.37b16
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.WIP.v037b16.machine;

//generic imports
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
//to be organized
import static arcadeflex036.osdepend.logerror;
import static arcadeflex056.fileio.osd_fread;
import static arcadeflex056.fileio.osd_fwrite;
import static common.libc.cstring.memset;

public class atari_vg {

    public static final int EAROM_SIZE = 0x40;

    static int earom_offset;
    static int earom_data;
    static char[] earom = new char[EAROM_SIZE];

    public static ReadHandlerPtr atari_vg_earom_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            logerror("read earom: %02x(%02x):%02x\n", earom_offset, offset, earom_data);
            return (earom_data);
        }
    };

    public static WriteHandlerPtr atari_vg_earom_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            logerror("write earom: %02x:%02x\n", offset, data);
            earom_offset = offset;
            earom_data = data;
        }
    };

    /* 0,8 and 14 get written to this location, too.
	 * Don't know what they do exactly
     */
    public static WriteHandlerPtr atari_vg_earom_ctrl_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            logerror("earom ctrl: %02x:%02x\n", offset, data);
            /*
			0x01 = clock
			0x02 = set data latch? - writes only (not always)
			0x04 = write mode? - writes only
			0x08 = set addr latch?
             */
            if ((data & 0x01) != 0) {
                earom_data = earom[earom_offset];
            }
            if ((data & 0x0c) == 0x0c) {
                earom[earom_offset] = (char) (earom_data & 0xFF);
                logerror("    written %02x:%02x\n", earom_offset, earom_data);
            }
        }
    };

    public static nvramPtr atari_vg_earom_handler = new nvramPtr() {
        public void handler(Object file, int read_or_write) {
            if (read_or_write != 0) {
                osd_fwrite(file, earom, EAROM_SIZE);
            } else {
                if (file != null) {
                    osd_fread(file, earom, EAROM_SIZE);
                } else {
                    memset(earom, 0, EAROM_SIZE);
                }
            }
        }
    };
}
