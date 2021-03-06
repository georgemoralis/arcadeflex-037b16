/***************************************************************************

  namcond1.h

  Common functions & declarations for the Namco ND-1 driver

***************************************************************************/

/* VIDHRDW */

#define GFX_8X8_4BIT    0
#define GFX_16X16_4BIT  1
#define GFX_32X32_4BIT  2
#define GFX_8X8_8BIT    3
#define GFX_16X16_8BIT  4

extern void nvsram( offs_t offset, data16_t data );

/* MACHINE */

extern unsigned short int *namcond1_shared_ram;

extern READ16_HANDLER( namcond1_shared_ram_r );
extern READ16_HANDLER( namcond1_cuskey_r );
extern WRITE16_HANDLER( namcond1_shared_ram_w );

void namcond1_nvramhandler( void *f, int state );
void namcond1_init_machine(void);
int namcond1_vb_interrupt( void );

/* VIDHRDW */

// to be removed
extern READ16_HANDLER( debug_trigger );

