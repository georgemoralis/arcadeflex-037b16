/**
 * ported to v0.37b16
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.v037b16.sound;

import static gr.codebb.arcadeflex.v037b16.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.sndintrfH.*;

public class Dummy_snd extends snd_interface {

    public Dummy_snd() {
        sound_num = SOUND_DUMMY;
        name = "";
    }

    @Override
    public int chips_num(MachineSound msound) {
        return 0;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int start(MachineSound msound) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void update() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
