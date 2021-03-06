/*********************************************************/
/*    Konami PCM controller                              */
/*********************************************************/
#ifndef __KDAC_A_H__
#define __KDAC_A_H__

#define MAX_K007232		3


struct K007232_interface
{
	int num_chips;			/* Number of chips */
	int bank[MAX_K007232];	/* memory regions */
	int volume[MAX_K007232];/* volume */
	void (*portwritehandler[MAX_K007232])(int);
};

#define K007232_VOL(LVol,LPan,RVol,RPan) ((LVol)|((LPan)<<8)|((RVol)<<16)|((RPan)<<24))



void K007232_bankswitch(int chip,UBytePtr ptr_A,UBytePtr ptr_B);

/*
  The 007232 has two channels and produces two outputs. The volume control
  is external, however to make it easier to use we handle that inside the
  emulation. You can control volume and panning: for each of the two channels
  you can set the volume of the two outputs. If panning is not required,
  then volumeB will be 0 for channel 0, and volumeA will be 0 for channel 1.
  Volume is in the range 0-255.
*/
void K007232_set_volume(int chip,int channel,int volumeA,int volumeB);

#endif
