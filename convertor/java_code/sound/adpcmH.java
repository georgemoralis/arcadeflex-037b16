#ifndef ADPCM_H
#define ADPCM_H

#define MAX_ADPCM 8


/* a generic ADPCM interface, for unknown chips */

struct ADPCMinterface
{
	int num;			       /* total number of ADPCM decoders in the machine */
	int frequency;             /* playback frequency */
	int region;                /* memory region where the samples come from */
	int mixing_level[MAX_ADPCM];     /* master volume */
};


void ADPCM_play(int num, int offset, int length);
void ADPCM_setvol(int num, int vol);
void ADPCM_stop(int num);
int ADPCM_playing(int num);


/* an interface for the OKIM6295 and similar chips */

#define MAX_OKIM6295 			2

/*
  Note about the playback frequency: the external clock is internally divided,
  depending on pin 7, by 132 (high) or 165 (low). This isn't handled by the
  emulation, so you have to provide the didvided internal clock instead of the
  external clock.
*/
struct OKIM6295interface
{
	int num;                  		/* total number of chips */
	int frequency[MAX_OKIM6295];	/* playback frequency */
	int region[MAX_OKIM6295];		/* memory region where the sample ROM lives */
	int mixing_level[MAX_OKIM6295];	/* master volume */
};

void OKIM6295_set_bank_base(int which, int base);
void OKIM6295_set_frequency(int which, int frequency);

READ16_HANDLER( OKIM6295_status_0_lsb_r );
READ16_HANDLER( OKIM6295_status_1_lsb_r );
READ16_HANDLER( OKIM6295_status_0_msb_r );
READ16_HANDLER( OKIM6295_status_1_msb_r );
WRITE16_HANDLER( OKIM6295_data_0_lsb_w );
WRITE16_HANDLER( OKIM6295_data_1_lsb_w );
WRITE16_HANDLER( OKIM6295_data_0_msb_w );
WRITE16_HANDLER( OKIM6295_data_1_msb_w );

#endif
