/*
 * ported to v0.37b16
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.v037b16.cpu.m6800;

//generic imports
//cpu imports
import static gr.codebb.arcadeflex.v037b16.cpu.m6800.m6800Η.*;
import static gr.codebb.arcadeflex.v037b16.cpu.m6800.m6800tbl.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;

public class m6808 extends m6800 {

    public m6808() {
        cpu_num = CPU_M6808;
        num_irqs = 1;
        default_vector = 0;
        overclock = 1.0;
        no_int = M6800_INT_NONE;
        irq_int = M6800_INT_IRQ;
        nmi_int = M6800_INT_NMI;
        databus_width = 8;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_BE;
        align_unit = 1;
        max_inst_len = 4;
        pgm_memory_base = 0;
        icount = m6800_ICount;
        m6800_ICount[0] = 50000;
    }

    public String cpu_info(Object context, int regnum) {
        switch (regnum) {
            case CPU_INFO_NAME:
                return "M6808";
        }
        return super.cpu_info(context, regnum);
    }

    @Override
    public void init() {
        m6800.insn = m6800_insn;
        m6800.cycles = cycles_6800;
        /*TODO*///state_register("m6808");
    }
}
