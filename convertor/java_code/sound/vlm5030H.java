#ifndef VLM5030_h
#define VLM5030_h

struct VLM5030interface
{
	int baseclock;      /* master clock (normaly 3.58MHz) */
	int volume;         /* volume                         */
	int memory_region;  /* memory region of speech rom    */
	int memory_size;    /* memory size of speech rom (0=memory region length) */
	const char **samplenames;	/* optional samples to replace emulation */
};

/* use sampling data when speech_rom == 0 */

/* set speech rom address */
void VLM5030_set_rom(void *speech_rom);

/* get BSY pin level */
/* latch contoll data */
/* set RST pin level : reset / set table address A8-A15 */
void VLM5030_RST (int pin );
/* set VCU pin level : ?? unknown */
void VLM5030_VCU(int pin );
/* set ST pin level  : set table address A0-A7 / start speech */
void VLM5030_ST(int pin );

#endif

