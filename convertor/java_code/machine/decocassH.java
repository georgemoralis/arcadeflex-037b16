/* set to 1 to display tape time offset */
#define TAPE_UI_DISPLAY 0

#ifdef MAME_DEBUG
#define LOGLEVEL  0
#define LOG(n,x)  if (LOGLEVEL >= n) logerror x
#else
#define LOG(n,x)
#endif

extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern int tape_dir;
extern int tape_speed;
extern double tape_time0;
extern void *tape_timer;

extern extern extern extern extern extern extern extern extern 
extern extern 
extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern 
extern extern extern extern 
/* from drivers/decocass.c */
extern 
/* from vidhrdw/decocass.c */
extern extern extern extern extern extern extern extern extern extern extern 
extern extern extern extern extern extern extern extern extern extern extern extern 
extern extern extern 
extern UBytePtr decocass_charram;
extern UBytePtr decocass_fgvideoram;
extern UBytePtr decocass_colorram;
extern UBytePtr decocass_bgvideoram;
extern UBytePtr decocass_tileram;
extern UBytePtr decocass_objectram;
extern size_t decocass_fgvideoram_size;
extern size_t decocass_colorram_size;
extern size_t decocass_bgvideoram_size;
extern size_t decocass_tileram_size;
extern size_t decocass_objectram_size;

