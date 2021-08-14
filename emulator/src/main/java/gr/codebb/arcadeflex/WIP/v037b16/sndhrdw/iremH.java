/**
 * ported to v0.37b16
 *
 */
package gr.codebb.arcadeflex.WIP.v037b16.sndhrdw;

//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b16.mame.sndintrfH.*;
//sndhrdw imports
import static gr.codebb.arcadeflex.WIP.v037b16.sndhrdw.irem.*;

public class iremH {

    public static MachineCPU IREM_AUDIO_CPU = new MachineCPU(CPU_M6803 | CPU_AUDIO_CPU,
            6000000 / 4, /* ??? */
            irem_sound_readmem, irem_sound_writemem,
            irem_sound_readport, irem_sound_writeport,
            null, 0);

    public static MachineSound[] IREM_AUDIO = new MachineSound[]{
        new MachineSound(
        SOUND_AY8910,
        irem_ay8910_interface
        ),
        new MachineSound(
        SOUND_MSM5205,
        irem_msm5205_interface
        )
    };
}
