/*********************************************************

	Konami 053260 PCM Sound Chip

*********************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.sound;

import common.ptr.UBytePtr;
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b16.mame.memory.*;
import static gr.codebb.arcadeflex.v037b16.mame.memoryH.*;
import static gr.codebb.arcadeflex.v056.mame.timer.*;
import static gr.codebb.arcadeflex.v056.mame.timerH.*;
import static mame037b16.mame.Machine;
import static mame056.sound.streams.*;
import static arcadeflex036.osdepend.logerror;
import common.ptr.ShortPtr;
import gr.codebb.arcadeflex.WIP.v037b16.sound.k053260H;
import static gr.codebb.arcadeflex.WIP.v037b16.sound.k053260H.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.sndintrf.*;
import gr.codebb.arcadeflex.v037b16.mame.sndintrfH;
import static gr.codebb.arcadeflex.v037b16.mame.sndintrfH.*;
import static common.libc.cstdio.sprintf;

public class k053260 extends snd_interface {

    public k053260() {
        this.name = "053260";
        this.sound_num = SOUND_K053260;
    }
	
/*TODO*///	#define LOG 0

        public static final int BASE_SHIFT	= 16;

    @Override
    public int chips_num(sndintrfH.MachineSound msound) {
        return ((K053260_interface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(sndintrfH.MachineSound msound) {
        return ((K053260_interface) msound.sound_interface).clock[0];
    }

    @Override
    public int start(sndintrfH.MachineSound msound) {
        return K053260_sh_start.handler(msound);
    }

    @Override
    public void stop() {
        K053260_sh_stop.handler();
    }

    @Override
    public void update() {
        // nothing to do
    }

    @Override
    public void reset() {
        // nothing to do
    }
	
	public static class K053260_channel_def {
		long		rate;
		long		size;
		long		start;
		long		bank;
		long		volume;
		int					play;
		long		pan;
		long		pos;
		int					loop;
		int					ppcm; /* packed PCM ( 4 bit signed ) */
		int					ppcm_data;
	};
	
	public static class K053260_chip_def {
		int								channel;
		int								mode;
		int[]								regs= new int[0x30];
		UBytePtr rom;
		int								rom_size;
		timer_entry							timer; /* SH1 int timer */
		long[]					delta_table;
		K053260_channel_def[]		channels= new K053260_channel_def[4];
	};
	
	public static K053260_chip_def[] K053260_chip;

	/* local copy of the interface */
	static K053260_interface	intf;

	static void InitDeltaTable( int chip ) {
		int		i;
		double	base = ( double )Machine.sample_rate;
		double	max = (double)( intf.clock[chip] ); /* Hz */
		long val;
	
		for( i = 0; i < 0x1000; i++ ) {
			double v = ( double )( 0x1000 - i );
			double target = max / v;
			double fixed = ( double )( 1 << BASE_SHIFT );
	
			if ( target!=0 && base!=0 ) {
				target = fixed / ( base / target );
				val = (long) target;
				if ( val == 0 )
					val = 1;
			} else
				val = 1;
	
			K053260_chip[chip].delta_table[i] = val;
		}
	}
	
	static void K053260_reset( int chip ) {
		int i;
	
		for( i = 0; i < 4; i++ ) {
                        if (K053260_chip[chip] == null)
                            K053260_chip[chip] = new K053260_chip_def();
                        
                        if (K053260_chip[chip].channels[i] == null)
                            K053260_chip[chip].channels[i] = new K053260_channel_def();
                        
			K053260_chip[chip].channels[i].rate = 0;
			K053260_chip[chip].channels[i].size = 0;
			K053260_chip[chip].channels[i].start = 0;
			K053260_chip[chip].channels[i].bank = 0;
			K053260_chip[chip].channels[i].volume = 0;
			K053260_chip[chip].channels[i].play = 0;
			K053260_chip[chip].channels[i].pan = 0;
			K053260_chip[chip].channels[i].pos = 0;
			K053260_chip[chip].channels[i].loop = 0;
			K053260_chip[chip].channels[i].ppcm = 0;
			K053260_chip[chip].channels[i].ppcm_data = 0;
		}
	}
	
/*TODO*///	INLINE int limit( int val, int max, int min ) {
/*TODO*///		if ( val > max )
/*TODO*///			val = max;
/*TODO*///		else if ( val < min )
/*TODO*///			val = min;
/*TODO*///	
/*TODO*///		return val;
/*TODO*///	}

        public static final int MAXOUT = 0x7fff;
        public static final int MINOUT = -0x8000;
	
	public static StreamInitMultiPtr K053260_update = new StreamInitMultiPtr() {
            public void handler(int param, ShortPtr[] buffer, int length) {
		int dpcmcnv[] = { 0, 1, 4, 9, 16, 25, 36, 49, -64, -49, -36, -25, -16, -9, -4, -1 };
	
		int i, j;
                int[] lvol=new int[4], rvol=new int[4], play=new int[4], loop=new int[4], ppcm_data=new int[4], ppcm=new int[4];
		UBytePtr[] rom=new UBytePtr[4];
		int[] delta=new int[4], end=new int[4], pos=new int[4];
		int dataL, dataR;
		char d;
		//struct K053260_chip_def *ic = &K053260_chip[param];
	
		/* precache some values */
		for ( i = 0; i < 4; i++ ) {
                        int _posi=(int)(K053260_chip[param].channels[i].start) + (int)(( K053260_chip[param].channels[i].bank << 16 ));
			rom[i]= new UBytePtr(K053260_chip[param].rom, _posi);
			delta[i] = (int) (K053260_chip[param].delta_table[(int)K053260_chip[param].channels[i].rate]);
			lvol[i] = (int) (K053260_chip[param].channels[i].volume * K053260_chip[param].channels[i].pan);
			rvol[i] = (int) (K053260_chip[param].channels[i].volume * ( 8 - K053260_chip[param].channels[i].pan ));
			end[i] = (int) K053260_chip[param].channels[i].size;
			pos[i] = (int) K053260_chip[param].channels[i].pos;
			play[i] = K053260_chip[param].channels[i].play;
			loop[i] = K053260_chip[param].channels[i].loop;
			ppcm[i] = K053260_chip[param].channels[i].ppcm;
			ppcm_data[i] = K053260_chip[param].channels[i].ppcm_data;
			if ( ppcm[i] != 0 )
				delta[i] /= 2;
		}
	
			for ( j = 0; j < length; j++ ) {
	
				dataL = dataR = 0;
	
				for ( i = 0; i < 4; i++ ) {
					/* see if the voice is on */
					if ( play[i] != 0 ) {
						/* see if we're done */
						if ( ( pos[i] >> BASE_SHIFT ) >= end[i] ) {
	
							ppcm_data[i] = 0;
	
							if ( loop[i] != 0 )
								pos[i] = 0;
							else {
								play[i] = 0;
								continue;
							}
						}
	
						if ( ppcm[i] != 0 ) { /* Packed PCM */
							/* we only update the signal if we're starting or a real sound sample has gone by */
							/* this is all due to the dynamic sample rate convertion */
							if ( pos[i] == 0 || ( ( pos[i] ^ ( pos[i] - delta[i] ) ) & 0x8000 ) == 0x8000 ) {
								int newdata;
								if (( pos[i] & 0x8000 ) != 0)
									newdata = rom[i].read(pos[i] >> BASE_SHIFT) & 0x0f;
								else
									newdata = ( ( rom[i].read(pos[i] >> BASE_SHIFT) ) >> 4 ) & 0x0f;
	
								ppcm_data[i] = (int) (( ( ppcm_data[i] * 62 ) >> 6 ) + dpcmcnv[newdata]);
	
								if ( ppcm_data[i] > 127 )
									ppcm_data[i] = 127;
								else
									if ( ppcm_data[i] < -128 )
										ppcm_data[i] = -128;
							}
	
							d = (char) ppcm_data[i];
	
							pos[i] += delta[i];
						} else { /* PCM */
							d = rom[i].read(pos[i] >> BASE_SHIFT);
	
							pos[i] += delta[i];
						}
	
						if (( K053260_chip[param].mode & 2 ) != 0) {
							dataL += ( d * lvol[i] ) >> 2;
							dataR += ( d * rvol[i] ) >> 2;
						}
					}
				}
	
				//buffer[1][j] = limit(dataL, MAXOUT, MINOUT);
                //buffer[0][j] = limit(dataR, MAXOUT, MINOUT);
                if (dataL > MAXOUT) {
                    dataL = MAXOUT;
                } else if (dataL < MINOUT) {
                    dataL = MINOUT;
                }
                if (dataR > MAXOUT) {
                    dataR = MAXOUT;
                } else if (dataR < MINOUT) {
                    dataR = MINOUT;
                }

                buffer[1].write(j, (short) dataL);
                buffer[0].write(j, (short) dataR);
			}
	
		/* update the regs now */
		for ( i = 0; i < 4; i++ ) {
			K053260_chip[param].channels[i].pos = pos[i];
			K053260_chip[param].channels[i].play = play[i];
			K053260_chip[param].channels[i].ppcm_data = ppcm_data[i];
		}
            }
        };
	
	public static ShStartPtr K053260_sh_start = new ShStartPtr() { public int handler(MachineSound msound)  {
		String[] names = new String[2];
		String[] ch_names = new String[2];
                
                for (int _i=0 ; _i<2 ; _i++){
                    names[_i]="";
                    ch_names[_i]="";
                }
                
		int i, ics;
	
		/* Initialize our chip structure */
		intf = (k053260H.K053260_interface) msound.sound_interface;
                
		if ( intf.num > MAX_053260 )
			return -1;
	
		K053260_chip = new K053260_chip_def[intf.num];
                
                for (int _i=0 ; _i<intf.num ; _i++)
                    K053260_chip[_i] = new K053260_chip_def();
	
		if ( K053260_chip == null )
			return -1;
	
		for( ics = 0; ics < intf.num; ics++ ) {
			//struct K053260_chip_def *ic = &K053260_chip[ics];
	
			K053260_chip[ics].mode = 0;
			K053260_chip[ics].rom = new UBytePtr(memory_region(intf.region[ics]));
			K053260_chip[ics].rom_size = memory_region_length(intf.region[ics]) - 1;
	
			K053260_reset( ics );
	
			for ( i = 0; i < 0x30; i++ )
				K053260_chip[ics].regs[i] = 0;
	
			K053260_chip[ics].delta_table = new long[ 0x1000 ];
	
			if ( K053260_chip[ics].delta_table == null )
				return -1;
	
			for ( i = 0; i < 2; i++ ) {
				names[i] = ch_names[i];
				sprintf(ch_names[i],"%s #%d Ch %d",sound_name(msound),ics,i);
			}
	
			K053260_chip[ics].channel = stream_init_multi( 2, names, intf.mixing_level[ics], Machine.sample_rate, ics, K053260_update );
	
			InitDeltaTable( ics );
	
			/* setup SH1 timer if necessary */
			if ( intf.irq!= null && intf.irq[ics] != null )
				K053260_chip[ics].timer = timer_pulse( TIME_IN_HZ( ( intf.clock[ics] / 32 ) ), 0, intf.irq[ics] );
			else
				K053260_chip[ics].timer = null;
		}
	
	    return 0;
	} };
	
	public static ShStopPtr K053260_sh_stop = new ShStopPtr() { public void handler()  {
		int ics;
	
		if (K053260_chip != null) {
			for( ics = 0; ics < intf.num; ics++ ) {
				//struct K053260_chip_def *ic = &K053260_chip[ics];
	
				//if ( K053260_chip[ics].delta_table != null )
				//	free( ic.delta_table );
	
				K053260_chip[ics].delta_table = null;
	
				if ( K053260_chip[ics].timer != null )
					timer_remove( K053260_chip[ics].timer );
	
				K053260_chip[ics].timer = null;
			}
	
			K053260_chip = null;
		}
	} };
	
	public static void check_bounds( int chip, int channel ) {
		//struct K053260_chip_def *ic = &K053260_chip[chip];
	
		int channel_start = (int) (( K053260_chip[chip].channels[channel].bank << 16 ) + K053260_chip[chip].channels[channel].start);
		int channel_end = (int) (channel_start + K053260_chip[chip].channels[channel].size - 1);
	
		if ( channel_start > K053260_chip[chip].rom_size ) {
			logerror("K53260: Attempting to start playing past the end of the rom ( start = %06x, end = %06x ).\n", channel_start, channel_end );
	
			K053260_chip[chip].channels[channel].play = 0;
	
			return;
		}
	
		if ( channel_end > K053260_chip[chip].rom_size ) {
			logerror("K53260: Attempting to play past the end of the rom ( start = %06x, end = %06x ).\n", channel_start, channel_end );
	
			K053260_chip[chip].channels[channel].size = K053260_chip[chip].rom_size - channel_start;
		}
/*TODO*///	#if LOG
/*TODO*///		logerror("K053260: Sample Start = %06x, Sample End = %06x, Sample rate = %04lx, PPCM = %s\n", channel_start, channel_end, ic.channels[channel].rate, ic.channels[channel].ppcm ? "yes" : "no" );
/*TODO*///	#endif
	}
	
	public static void K053260_write( int chip, int offset, int data )
	{
		int i, t;
		int r = offset;
		int v = data;
	
		//struct K053260_chip_def *ic = &K053260_chip[chip];
	
		if ( r > 0x2f ) {
			logerror("K053260: Writing past registers\n" );
			return;
		}
	
		if ( Machine.sample_rate != 0 )
			stream_update( K053260_chip[chip].channel, 0 );
	
		/* before we update the regs, we need to check for a latched reg */
		if ( r == 0x28 ) {
			t = K053260_chip[chip].regs[r] ^ v;
	
			for ( i = 0; i < 4; i++ ) {
				if (( t & ( 1 << i ) ) != 0) {
					if (( v & ( 1 << i ) ) != 0){
						K053260_chip[chip].channels[i].play = 1;
						K053260_chip[chip].channels[i].pos = 0;
						K053260_chip[chip].channels[i].ppcm_data = 0;
						check_bounds( chip, i );
					} else
						K053260_chip[chip].channels[i].play = 0;
				}
			}
	
			K053260_chip[chip].regs[r] = v;
			return;
		}
	
		/* update regs */
		K053260_chip[chip].regs[r] = v;
	
		/* communication registers */
		if ( r < 8 )
			return;
	
		/* channel setup */
		if ( r < 0x28 ) {
			int channel = ( r - 8 ) / 8;
	
			switch ( ( r - 8 ) & 0x07 ) {
				case 0: /* sample rate low */
					K053260_chip[chip].channels[channel].rate &= 0x0f00;
					K053260_chip[chip].channels[channel].rate |= v;
				break;
	
				case 1: /* sample rate high */
					K053260_chip[chip].channels[channel].rate &= 0x00ff;
					K053260_chip[chip].channels[channel].rate |= ( v & 0x0f ) << 8;
				break;
	
				case 2: /* size low */
					K053260_chip[chip].channels[channel].size &= 0xff00;
					K053260_chip[chip].channels[channel].size |= v;
				break;
	
				case 3: /* size high */
					K053260_chip[chip].channels[channel].size &= 0x00ff;
					K053260_chip[chip].channels[channel].size |= v << 8;
				break;
	
				case 4: /* start low */
					K053260_chip[chip].channels[channel].start &= 0xff00;
					K053260_chip[chip].channels[channel].start |= v;
				break;
	
				case 5: /* start high */
					K053260_chip[chip].channels[channel].start &= 0x00ff;
					K053260_chip[chip].channels[channel].start |= v << 8;
				break;
	
				case 6: /* bank */
					K053260_chip[chip].channels[channel].bank = v & 0xff;
				break;
	
				case 7: /* volume is 7 bits. Convert to 8 bits now. */
					K053260_chip[chip].channels[channel].volume = ( ( v & 0x7f ) << 1 ) | ( v & 1 );
				break;
			}
	
			return;
		}
	
		switch( r ) {
			case 0x2a: /* loop, ppcm */
				for ( i = 0; i < 4; i++ )
					K053260_chip[chip].channels[i].loop = ( v & ( 1 << i ) ) != 0 ? 1:0;
	
				for ( i = 4; i < 8; i++ )
					K053260_chip[chip].channels[i-4].ppcm = ( v & ( 1 << i ) ) != 0 ? 1:0;
			break;
	
			case 0x2c: /* pan */
				K053260_chip[chip].channels[0].pan = v & 7;
				K053260_chip[chip].channels[1].pan = ( v >> 3 ) & 7;
			break;
	
			case 0x2d: /* more pan */
				K053260_chip[chip].channels[2].pan = v & 7;
				K053260_chip[chip].channels[3].pan = ( v >> 3 ) & 7;
			break;
	
			case 0x2f: /* control */
				K053260_chip[chip].mode = v & 7;
				/* bit 0 = read ROM */
				/* bit 1 = enable sound output */
				/* bit 2 = unknown */
			break;
		}
	}
	
	public static int K053260_read( int chip, int offset )
	{
		//struct K053260_chip_def *ic = &K053260_chip[chip];
	
		switch ( offset ) {
			case 0x29: /* channel status */
				{
					int i, status = 0;
	
					for ( i = 0; i < 4; i++ )
						status |= K053260_chip[chip].channels[i].play << i;
	
					return status;
				}
			//break;
	
			case 0x2e: /* read rom */
				if (( K053260_chip[chip].mode & 1 ) != 0){
					long offs = K053260_chip[chip].channels[0].start + ( K053260_chip[chip].channels[0].pos >> BASE_SHIFT ) + ( K053260_chip[chip].channels[0].bank << 16 );
	
					K053260_chip[chip].channels[0].pos += ( 1 << 16 );
	
					if ( offs > K053260_chip[chip].rom_size ) {
						logerror("%06x: K53260: Attempting to read past rom size in rom Read Mode (offs = %06x, size = %06x).\n",cpu_get_pc(),offs,K053260_chip[chip].rom_size );
	
						return 0;
					}
	
					return K053260_chip[chip].rom.read((int) offs);
				}
			break;
		}
	
		return K053260_chip[chip].regs[offset];
	}
	
	/**************************************************************************************************/
	/* Accesors */
	
	public static ReadHandlerPtr K053260_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K053260_read( 0, offset );
	} };
	
	public static WriteHandlerPtr K053260_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		K053260_write( 0, offset, data );
	} };
	
/*TODO*///	public static ReadHandlerPtr K053260_1_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return K053260_read( 1, offset );
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr K053260_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		K053260_write( 1, offset, data );
/*TODO*///	} };
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( K053260_0_lsb_w )
/*TODO*///	{
/*TODO*///		if (ACCESSING_LSB != 0)
/*TODO*///			K053260_0_w (offset, data & 0xff);
/*TODO*///	}
/*TODO*///	
/*TODO*///	READ16_HANDLER( K053260_0_lsb_r )
/*TODO*///	{
/*TODO*///		return K053260_0_r(offset);
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( K053260_1_lsb_w )
/*TODO*///	{
/*TODO*///		if (ACCESSING_LSB != 0)
/*TODO*///			K053260_1_w (offset, data & 0xff);
/*TODO*///	}
/*TODO*///	
/*TODO*///	READ16_HANDLER( K053260_1_lsb_r )
/*TODO*///	{
/*TODO*///		return K053260_1_r(offset);
/*TODO*///	}
}
