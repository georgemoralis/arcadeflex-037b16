package gr.codebb.arcadeflex.WIP.v037b16.sound;

import static gr.codebb.arcadeflex.v056.mame.timer.*;


/**
 * *******************************************************
 *
 * Konami 053260 PCM/ADPCM Sound Chip
 *
 ********************************************************
 */

public class k053260H {

    public static final int MAX_053260 = 2;

    public static class K053260_interface {
        
        public K053260_interface(int num, int[] clock, int[] region, int[][] mixing_level, timer_callback[] irq) {
            this.num = num;
            this.clock = clock;
            this.region = region;
            this.mixing_level = mixing_level;
            this.irq = irq;
        }
        
        int num;
        
        int[] clock;					/* clock */

        int[] region;					/* memory region of sample ROM(s) */

        int[][] mixing_level=new int [MAX_053260][2];		/* volume */

        timer_callback[] irq;	/* called on SH1 complete cycle ( clock / 32 ) */

    };
}
