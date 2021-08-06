/**
 * ported to v0.37b16
 */
package gr.codebb.arcadeflex.v037b16.machine;

//generic imports
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;

public class _8255ppiH {

    public static int MAX_8255 = 4;

    public static class ppi8255_interface {

        public int num;/* number of PPIs to emulate */
        public ReadHandlerPtr[] portAread;
        public ReadHandlerPtr[] portBread;
        public ReadHandlerPtr[] portCread;
        public WriteHandlerPtr[] portAwrite;
        public WriteHandlerPtr[] portBwrite;
        public WriteHandlerPtr[] portCwrite;

        public ppi8255_interface(int i, ReadHandlerPtr[] ppi_porta_r, ReadHandlerPtr[] ppi_portb_r, ReadHandlerPtr[] ppi_portc_r, WriteHandlerPtr[] ppi_porta_w, WriteHandlerPtr[] ppi_portb_w, WriteHandlerPtr[] ppi_portc_w) {
            num = i;

            portAread = ppi_porta_r;
            portBread = ppi_portb_r;
            portCread = ppi_portc_r;

            portAwrite = ppi_porta_w;
            portBwrite = ppi_portb_w;
            portCwrite = ppi_portc_w;
        }

        public ppi8255_interface(int i, ReadHandlerPtr ppi_porta_r, ReadHandlerPtr ppi_portb_r, ReadHandlerPtr ppi_portc_r, WriteHandlerPtr ppi_porta_w, WriteHandlerPtr ppi_portb_w, WriteHandlerPtr ppi_portc_w) {
            num = i;

            portAread = new ReadHandlerPtr[]{ppi_porta_r};
            portBread = new ReadHandlerPtr[]{ppi_portb_r};
            portCread = new ReadHandlerPtr[]{ppi_portc_r};

            portAwrite = new WriteHandlerPtr[]{ppi_porta_w};
            portBwrite = new WriteHandlerPtr[]{ppi_portb_w};
            portCwrite = new WriteHandlerPtr[]{ppi_portc_w};
        }
    }
}
