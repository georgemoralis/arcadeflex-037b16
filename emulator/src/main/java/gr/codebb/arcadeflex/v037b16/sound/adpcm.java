/*
 * ported to v0.37b16
 * ported to v0.37b7 
 * ported to v0.36 
 */
package gr.codebb.arcadeflex.v037b16.sound;

import static arcadeflex036.osdepend.logerror;
import static common.libc.cstdio.sprintf;
import common.ptr.ShortPtr;
import common.ptr.UBytePtr;
import static gr.codebb.arcadeflex.v037b16.mame.common.memory_region;
import gr.codebb.arcadeflex.v037b16.mame.sndintrf.snd_interface;
import static gr.codebb.arcadeflex.v037b16.mame.sndintrf.sound_name;
import gr.codebb.arcadeflex.v037b16.mame.sndintrfH.MachineSound;
import static gr.codebb.arcadeflex.v037b16.mame.sndintrfH.SOUND_ADPCM;
import gr.codebb.arcadeflex.v037b16.sound.adpcmH.ADPCMinterface;
import static gr.codebb.arcadeflex.v037b16.sound.adpcmH.MAX_ADPCM;
import static mame037b16.mame.Machine;
import mame056.sound.streams.StreamInitPtr;
import static mame056.sound.streams.stream_init;
import static mame056.sound.streams.stream_update;

public class adpcm extends snd_interface {

    public static final int MAX_SAMPLE_CHUNK = 10000;

    public static final int FRAC_BITS = 14;
    public static final int FRAC_ONE = (1 << FRAC_BITS);
    public static final int FRAC_MASK = (FRAC_ONE - 1);

    /* struct describing a single playing ADPCM voice */
    public static class ADPCMVoice {

        int stream;
        /* which stream are we playing on? */

        byte playing;
        /* 1 if we are actively playing */

        UBytePtr region_base;
        /* pointer to the base of the region */

        UBytePtr _base;
        /* pointer to the base memory location */

        int/*UINT32*/ sample;
        /* current sample number */

        int/*UINT32*/ count;
        /* total samples to play */

        int/*UINT32*/ signal;
        /* current ADPCM signal */

        int/*UINT32*/ step;
        /* current ADPCM step */

        int/*UINT32*/ volume;
        /* output volume */

        short last_sample;
        /* last sample output */

        short curr_sample;
        /* current sample target */

        int/*UINT32*/ source_step;
        /* step value for frequency conversion */

        int/*UINT32*/ source_pos;
        /* current fractional position */

    };
    /* array of ADPCM voices */
    static int/*UINT8*/ num_voices;
    static ADPCMVoice[] adpcm = new ADPCMVoice[MAX_ADPCM];
    /* step size index shift table */
    static int[] index_shift = {-1, -1, -1, -1, 2, 4, 6, 8};
    /* lookup table for the precomputed difference */
    static int[] diff_lookup = new int[49 * 16];
    /* volume lookup table */
    static /*UINT32*/ int[] volume_table = new int[16];

    public adpcm() {
        this.sound_num = SOUND_ADPCM;
        this.name = "ADPCM";
        for (int i = 0; i < MAX_ADPCM; i++) {
            adpcm[i] = new ADPCMVoice();
        }
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((ADPCMinterface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return 0;//NO functionality expected
    }

    @Override
    public void reset() {
        //NO functionality expected
    }

    /**
     * ********************************************************************************************
     *
     * compute_tables -- compute the difference tables
     *
     **********************************************************************************************
     */
    static void compute_tables() {
        /* nibble to bit map */
        int[][] nbl2bit
                = {
                    new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 1}, new int[]{1, 0, 1, 0}, new int[]{1, 0, 1, 1},
                    new int[]{1, 1, 0, 0}, new int[]{1, 1, 0, 1}, new int[]{1, 1, 1, 0}, new int[]{1, 1, 1, 1},
                    new int[]{-1, 0, 0, 0}, new int[]{-1, 0, 0, 1}, new int[]{-1, 0, 1, 0}, new int[]{-1, 0, 1, 1},
                    new int[]{-1, 1, 0, 0}, new int[]{-1, 1, 0, 1}, new int[]{-1, 1, 1, 0}, new int[]{-1, 1, 1, 1}
                };
        /* loop over all possible steps */
        for (int step = 0; step <= 48; step++) {
            /* compute the step value */
            int stepval = (int) Math.floor(16.0 * Math.pow(11.0 / 10.0, (double) step));

            /* loop over all nibbles and compute the difference */
            for (int nib = 0; nib < 16; nib++) {
                diff_lookup[step * 16 + nib] = nbl2bit[nib][0]
                        * (stepval * nbl2bit[nib][1]
                        + stepval / 2 * nbl2bit[nib][2]
                        + stepval / 4 * nbl2bit[nib][3]
                        + stepval / 8);
            }
        }
        /* generate the OKI6295 volume table */
        for (int step = 0; step < 16; step++) {
            double out = 256.0;
            int vol = step;

            /* 3dB per step */
            while (vol-- > 0) {
                out /= 1.412537545;
                /* = 10 ^ (3/20) = 3dB */
            }
            volume_table[step] = (/*UINT32*/int) out;
        }
    }

    /**
     * ********************************************************************************************
     *
     * generate_adpcm -- general ADPCM decoding routine
     *
     **********************************************************************************************
     */
    static void generate_adpcm(ADPCMVoice voice, ShortPtr buffer, int samples) {
        /* if this voice is active */
        if (voice.playing != 0) {
            UBytePtr _base = voice._base;
            int sample = (int) voice.sample;
            int signal = (int) voice.signal;
            int count = (int) voice.count;
            int step = (int) voice.step;
            int val;
            /* loop while we still have samples to generate */
            while (samples != 0) {
                /* compute the new amplitude and update the current step */
                val = _base.read(sample / 2) >> (((sample & 1) << 2) ^ 4);
                signal += diff_lookup[step * 16 + (val & 15)];

                /* clamp to the maximum */
                if (signal > 2047) {
                    signal = 2047;
                } else if (signal < -2048) {
                    signal = -2048;
                }

                /* adjust the step size and clamp */
                step += index_shift[val & 7];
                if (step > 48) {
                    step = 48;
                } else if (step < 0) {
                    step = 0;
                }
                /* output to the buffer, scaling by the volume */
                buffer.write(0, (short) (signal * voice.volume / 16));
                buffer.offset += 2;
                samples--;

                /* next! */
                if (++sample > count) {
                    voice.playing = 0;
                    break;
                }
            }
            /* update the parameters */
            voice.sample = sample;
            voice.signal = signal;
            voice.step = step;
        }

        /* fill the rest with silence */
        while (samples-- != 0) {
            buffer.write(0, (short) 0);
            buffer.offset += 2;
        }
    }

    /**
     * ********************************************************************************************
     *
     * adpcm_update -- update the sound chip so that it is in sync with CPU
     * execution
     *
     **********************************************************************************************
     */
    public static StreamInitPtr adpcm_update = new StreamInitPtr() {
        public void handler(int num, ShortPtr buffer, int length) {
            ADPCMVoice voice = adpcm[num];
            ShortPtr sample_data = new ShortPtr(MAX_SAMPLE_CHUNK * 2), curr_data = new ShortPtr(sample_data);
            short prev = voice.last_sample, curr = voice.curr_sample;
            int/*UINT32*/ final_pos;
            int/*UINT32*/ new_samples;
            /* finish off the current sample */
            if (voice.source_pos > 0) {
                /* interpolate */
                while (length > 0 && voice.source_pos < FRAC_ONE) {
                    buffer.write(0, (short) ((((int) prev * (FRAC_ONE - voice.source_pos)) + ((int) curr * voice.source_pos)) >> FRAC_BITS));
                    buffer.offset += 2;
                    voice.source_pos += voice.source_step;
                    length--;
                }

                /* if we're over, continue; otherwise, we're done */
                if (voice.source_pos >= FRAC_ONE) {
                    voice.source_pos -= FRAC_ONE;
                } else {
                    return;
                }
            }
            /* compute how many new samples we need */
            final_pos = (int) (voice.source_pos + length * voice.source_step);
            new_samples = (final_pos + FRAC_ONE - 1) >> FRAC_BITS;
            if (new_samples > MAX_SAMPLE_CHUNK) {
                new_samples = MAX_SAMPLE_CHUNK;
            }

            /* generate them into our buffer */
            generate_adpcm(voice, sample_data, (int) new_samples);
            prev = curr;
            curr = (short) curr_data.read(0);
            curr_data.offset += 2;

            /* then sample-rate convert with linear interpolation */
            while (length > 0) {
                /* interpolate */
                while (length > 0 && voice.source_pos < FRAC_ONE) {
                    buffer.write(0, (short) ((((int) prev * (FRAC_ONE - voice.source_pos)) + ((int) curr * voice.source_pos)) >> FRAC_BITS));
                    buffer.offset += 2;
                    voice.source_pos += voice.source_step;
                    length--;
                }

                /* if we're over, grab the next samples */
                if (voice.source_pos >= FRAC_ONE) {
                    voice.source_pos -= FRAC_ONE;
                    prev = curr;
                    curr = (short) curr_data.read(0);
                    curr_data.offset += 2;
                }
            }

            /* remember the last samples */
            voice.last_sample = prev;
            voice.curr_sample = curr;
        }
    };

    /*TODO*////**********************************************************************************************
/*TODO*///
/*TODO*///     state save support for MAME
/*TODO*///
/*TODO*///***********************************************************************************************/
/*TODO*///
/*TODO*///static UINT32 voice_base_offset[MAX_ADPCM]; /*we cannot save the pointer - this is a workaround*/
/*TODO*///static void adpcm_state_save_base_store (void)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	struct ADPCMVoice *voice;
/*TODO*///
/*TODO*///	for (i=0; i<num_voices; i++)
/*TODO*///	{
/*TODO*///		voice = &adpcm[i];
/*TODO*///		voice_base_offset[i] = voice->base - voice->region_base;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void adpcm_state_save_base_refresh (void)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	struct ADPCMVoice *voice;
/*TODO*///
/*TODO*///	for (i=0; i<num_voices; i++)
/*TODO*///	{
/*TODO*///		voice = &adpcm[i];
/*TODO*///		voice->base = &voice->region_base[ voice_base_offset[i] ];
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void adpcm_state_save_register( void )
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	char buf[20];
/*TODO*///	struct ADPCMVoice *voice;
/*TODO*///
/*TODO*///
/*TODO*///	sprintf(buf,"ADPCM");
/*TODO*///
/*TODO*///	for (i=0; i<num_voices; i++)
/*TODO*///	{
/*TODO*///		voice = &adpcm[i];
/*TODO*///
/*TODO*///		state_save_register_UINT8  (buf, i, "playing", &voice->playing, 1);
/*TODO*///		state_save_register_UINT32 (buf, i, "base_offset" , &voice_base_offset[i],  1);
/*TODO*///		state_save_register_UINT32 (buf, i, "sample" , &voice->sample,  1);
/*TODO*///		state_save_register_UINT32 (buf, i, "count"  , &voice->count,   1);
/*TODO*///		state_save_register_UINT32 (buf, i, "signal" , &voice->signal,  1);
/*TODO*///		state_save_register_UINT32 (buf, i, "step"   , &voice->step,    1);
/*TODO*///		state_save_register_UINT32 (buf, i, "volume" , &voice->volume,  1);
/*TODO*///
/*TODO*///		state_save_register_INT16  (buf, i, "last_sample", &voice->last_sample, 1);
/*TODO*///		state_save_register_INT16  (buf, i, "curr_sample", &voice->curr_sample, 1);
/*TODO*///		state_save_register_UINT32 (buf, i, "source_step", &voice->source_step, 1);
/*TODO*///		state_save_register_UINT32 (buf, i, "source_pos" , &voice->source_pos,  1);
/*TODO*///	}
/*TODO*///	state_save_register_func_presave(adpcm_state_save_base_store);
/*TODO*///	state_save_register_func_postload(adpcm_state_save_base_refresh);
/*TODO*///}
    /**
     * ********************************************************************************************
     *
     * ADPCM_sh_start -- start emulation of several ADPCM output streams
     *
     **********************************************************************************************
     */
    @Override
    public int start(MachineSound msound) {
        ADPCMinterface intf = (ADPCMinterface) msound.sound_interface;
        String stream_name;

        /* reset the ADPCM system */
        num_voices = intf.num;
        compute_tables();

        /* initialize the voices */
        //memset(adpcm, 0, sizeof(adpcm));
        for (int i = 0; i < num_voices; i++) {
            /* generate the name and create the stream */
            stream_name = sprintf("%s #%d", sound_name(msound), i);
            adpcm[i].stream = stream_init(stream_name, intf.mixing_level[i], Machine.sample_rate, i, adpcm_update);
            if (adpcm[i].stream == -1) {
                return 1;
            }

            /* initialize the rest of the structure */
            adpcm[i].region_base = memory_region(intf.region);
            adpcm[i].volume = 255;
            adpcm[i].signal = -2;
            if (Machine.sample_rate != 0) {
                adpcm[i].source_step = (/*UINT32*/int) ((double) intf.frequency * (double) FRAC_ONE / (double) Machine.sample_rate);
            }
        }
        /*TODO*///	adpcm_state_save_register();

        /* success */
        return 0;
    }

    /**
     * ********************************************************************************************
     *
     * ADPCM_sh_stop -- stop emulation of several ADPCM output streams
     *
     **********************************************************************************************
     */
    @Override
    public void stop() {
        //NO functionality expected
    }

    /**
     * ********************************************************************************************
     *
     * ADPCM_sh_update -- update ADPCM streams
     *
     **********************************************************************************************
     */
    @Override
    public void update() {
        //NO functionality expected
    }

    /**
     * ********************************************************************************************
     *
     * ADPCM_play -- play data from a specific offset for a specific length
     *
     **********************************************************************************************
     */
    public static void ADPCM_play(int num, int offset, int length) {
        ADPCMVoice voice = adpcm[num];

        /* bail if we're not playing anything */
        if (Machine.sample_rate == 0) {
            return;
        }

        /* range check the numbers */
        if (num >= num_voices) {
            logerror("error: ADPCM_trigger() called with channel = %d, but only %d channels allocated\n", num, num_voices);
            return;
        }

        /* update the ADPCM voice */
        stream_update(voice.stream, 0);

        /* set up the voice to play this sample */
        voice.playing = 1;
        voice._base = new UBytePtr(voice.region_base, offset);
        voice.sample = 0;
        voice.count = length;

        /* also reset the ADPCM parameters */
        voice.signal = -2;
        voice.step = 0;
    }

    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////**********************************************************************************************
    /*TODO*///
    /*TODO*///     ADPCM_stop -- stop playback on an ADPCM data channel
    /*TODO*///
    /*TODO*///***********************************************************************************************/
    /*TODO*///
    /*TODO*///void ADPCM_stop(int num)
    /*TODO*///{
    /*TODO*///	struct ADPCMVoice *voice = &adpcm[num];
    /*TODO*///
    /*TODO*///	/* bail if we're not playing anything */
    /*TODO*///	if (Machine->sample_rate == 0)
    /*TODO*///		return;
    /*TODO*///
    /*TODO*///	/* range check the numbers */
    /*TODO*///	if (num >= num_voices)
    /*TODO*///	{
    /*TODO*///		if (errorlog) fprintf(errorlog,"error: ADPCM_stop() called with channel = %d, but only %d channels allocated\n", num, num_voices);
    /*TODO*///		return;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* update the ADPCM voice */
    /*TODO*///	stream_update(voice->stream, 0);
    /*TODO*///
    /*TODO*///	/* stop playback */
    /*TODO*///	voice->playing = 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /**
     * ********************************************************************************************
     *
     * ADPCM_setvol -- change volume on an ADPCM data channel
     *
     **********************************************************************************************
     */
    public static void ADPCM_setvol(int num, int vol) {
        ADPCMVoice voice = adpcm[num];

        /* bail if we're not playing anything */
        if (Machine.sample_rate == 0) {
            return;
        }

        /* range check the numbers */
        if (num >= num_voices) {
            logerror("error: ADPCM_setvol() called with channel = %d, but only %d channels allocated\n", num, num_voices);
            return;
        }

        /* update the ADPCM voice */
        stream_update(voice.stream, 0);
        voice.volume = vol;
    }

    /**
     * ********************************************************************************************
     *
     * ADPCM_playing -- returns true if an ADPCM data channel is still playing
     *
     **********************************************************************************************
     */
    public static int ADPCM_playing(int num) {
        ADPCMVoice voice = adpcm[num];

        /* bail if we're not playing anything */
        if (Machine.sample_rate == 0) {
            return 0;
        }

        /* range check the numbers */
        if (num >= num_voices) {
            logerror("error: ADPCM_playing() called with channel = %d, but only %d channels allocated\n", num, num_voices);
            return 0;
        }

        /* update the ADPCM voice */
        stream_update(voice.stream, 0);
        return voice.playing;
    }

}
