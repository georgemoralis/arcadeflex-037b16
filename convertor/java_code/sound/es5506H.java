/**********************************************************************************************
 *
 *   Ensoniq ES5505/6 driver
 *   by Aaron Giles
 *
 **********************************************************************************************/

#ifndef ES5506_H
#define ES5506_H

#define MAX_ES5505 			2

struct ES5505interface
{
	int num;                  						/* total number of chips */
	int baseclock[MAX_ES5505];						/* input clock */
	int region0[MAX_ES5505];						/* memory region where the sample ROM lives */
	int region1[MAX_ES5505];						/* memory region where the sample ROM lives */
	int mixing_level[MAX_ES5505];					/* master volume */
	void (*irq_callback[MAX_ES5505])(int state);	/* irq callback */
	UINT16 (*read_port[MAX_ES5505])(void);			/* input port read */
};


READ16_HANDLER( ES5505_data_0_r );
READ16_HANDLER( ES5505_data_1_r );
WRITE16_HANDLER( ES5505_data_0_w );
WRITE16_HANDLER( ES5505_data_1_w );




#define MAX_ES5506 			2

struct ES5506interface
{
	int num;                  						/* total number of chips */
	int baseclock[MAX_ES5506];						/* input clock */
	int region0[MAX_ES5506];						/* memory region where the sample ROM lives */
	int region1[MAX_ES5506];						/* memory region where the sample ROM lives */
	int region2[MAX_ES5506];						/* memory region where the sample ROM lives */
	int region3[MAX_ES5506];						/* memory region where the sample ROM lives */
	int mixing_level[MAX_ES5506];					/* master volume */
	void (*irq_callback[MAX_ES5506])(int state);	/* irq callback */
	UINT16 (*read_port[MAX_ES5506])(void);			/* input port read */
};



void ES5506_voice_bank_0_w(int voice, int bank);
void ES5506_voice_bank_1_w(int voice, int bank);

#endif
