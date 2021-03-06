/* helper function to join two 16-bit ROMs and form a 32-bit data stream */
void konami_rom_deinterleave_2(int mem_region);
/* helper function to join four 16-bit ROMs and form a 64-bit data stream */
void konami_rom_deinterleave_4(int mem_region);


#define MAX_K007121 2
extern unsigned char K007121_ctrlram[MAX_K007121][8];

void K007121_ctrl_w(int chip,int offset,int data);
void K007121_sprites_draw(int chip,struct osd_bitmap *bitmap,
		const UBytePtr source,int base_color,int global_x_offset,int bank_base,
		UINT32 pri_mask);
void K007121_mark_sprites_colors(int chip,
		const UBytePtr source,int base_color,int bank_base);


int K007342_vh_start(int gfx_index, void (*callback)(int layer,int bank,int *code,int *color));
void K007342_tilemap_set_enable(int layer, int enable);
void K007342_tilemap_draw(struct osd_bitmap *bitmap,int num,int flags,UINT32 priority);


int K007420_vh_start(int gfxnum, void (*callback)(int *code,int *color));
void K007420_sprites_draw(struct osd_bitmap *bitmap);


/*
You don't have to decode the graphics: the vh_start() routines will do that
for you, using the plane order passed.
Of course the ROM data must be in the correct order. This is a way to ensure
that the ROM test will pass.
The konami_rom_deinterleave() function above will do the reorganization for
you in most cases (but see tmnt.c for additional bit rotations or byte
permutations which may be required).
*/
#define NORMAL_PLANE_ORDER 0,1,2,3
#define REVERSE_PLANE_ORDER 3,2,1,0


/*
The callback is passed:
- layer number (0 = FIX, 1 = A, 2 = B)
- bank (range 0-3, output of the pins CAB1 and CAB2)
- code (range 00-FF, output of the pins VC3-VC10)
  NOTE: code is in the range 0000-FFFF for X-Men, which uses extra RAM
- color (range 00-FF, output of the pins COL0-COL7)
The callback must put:
- in code the resulting tile number
- in color the resulting color index
- if necessary, put flags and/or priority for the TileMap code in the tile_info
  structure (e.g. TILE_FLIPX). Note that TILE_FLIPY is handled internally by the
  chip so it must not be set by the callback.
*/
int K052109_vh_start(int gfx_memory_region,int plane0,int plane1,int plane2,int plane3,
		void (*callback)(int layer,int bank,int *code,int *color));
/* plain 8-bit access */
READ16_HANDLER( K052109_word_r );
WRITE16_HANDLER( K052109_word_w );
READ16_HANDLER( K052109_lsb_r );
WRITE16_HANDLER( K052109_lsb_w );
void K052109_set_RMRD_line(int state);
void K052109_tilemap_draw(struct osd_bitmap *bitmap,int num,int flags,UINT32 priority);


/*
The callback is passed:
- code (range 00-1FFF, output of the pins CA5-CA17)
- color (range 00-FF, output of the pins OC0-OC7). Note that most of the
  time COL7 seems to be "shadow", but not always (e.g. Aliens).
The callback must put:
- in code the resulting sprite number
- in color the resulting color index
- if necessary, in priority the priority of the sprite wrt tilemaps
- if necessary, alter shadow to indicate whether the sprite has shadows enabled.
  shadow is preloaded with color & 0x80 so it doesn't need to be changed unless
  the game has special treatment (Aliens)
*/
int K051960_vh_start(int gfx_memory_region,int plane0,int plane1,int plane2,int plane3,
		void (*callback)(int *code,int *color,int *priority,int *shadow));
READ16_HANDLER( K051960_word_r );
WRITE16_HANDLER( K051960_word_w );
READ16_HANDLER( K051937_word_r );
WRITE16_HANDLER( K051937_word_w );
void K051960_sprites_draw(struct osd_bitmap *bitmap,int min_priority,int max_priority);

/* special handling for the chips sharing address space */


int K053245_vh_start(int gfx_memory_region,int plane0,int plane1,int plane2,int plane3,
		void (*callback)(int *code,int *color,int *priority_mask));
READ16_HANDLER( K053245_word_r );
WRITE16_HANDLER( K053245_word_w );
READ16_HANDLER( K053244_lsb_r );
WRITE16_HANDLER( K053244_lsb_w );
READ16_HANDLER( K053244_word_r );
WRITE16_HANDLER( K053244_word_w );
void K053244_bankselect(int bank);	/* used by TMNT2, Asterix and Premier Soccer for ROM testing */
void K053245_sprites_draw(struct osd_bitmap *bitmap);


int K053247_vh_start(int gfx_memory_region,int dx,int dy,int plane0,int plane1,int plane2,int plane3,
		void (*callback)(int *code,int *color,int *priority_mask));
READ16_HANDLER( K053247_word_r );
WRITE16_HANDLER( K053247_word_w );
void K053247_sprites_draw(struct osd_bitmap *bitmap);

READ16_HANDLER( K053246_word_r );
WRITE16_HANDLER( K053246_word_w );
void K053246_set_OBJCHA_line(int state);


/*
The callback is passed:
- code (range 00-FF, contents of the first tilemap RAM byte)
- color (range 00-FF, contents of the first tilemap RAM byte). Note that bit 6
  seems to be hardcoded as flip X.
The callback must put:
- in code the resulting tile number
- in color the resulting color index
- if necessary, put flags for the TileMap code in the tile_info
  structure (e.g. TILE_FLIPX)
*/
int K051316_vh_start_0(int gfx_memory_region,int bpp,
		void (*callback)(int *code,int *color));
int K051316_vh_start_1(int gfx_memory_region,int bpp,
		void (*callback)(int *code,int *color));
int K051316_vh_start_2(int gfx_memory_region,int bpp,
		void (*callback)(int *code,int *color));
void K051316_zoom_draw_0(struct osd_bitmap *bitmap,UINT32 priority);
void K051316_zoom_draw_1(struct osd_bitmap *bitmap,UINT32 priority);
void K051316_zoom_draw_2(struct osd_bitmap *bitmap,UINT32 priority);
void K051316_wraparound_enable(int chip, int status);
void K051316_set_offset(int chip, int xoffs, int yoffs);


WRITE16_HANDLER( K053251_lsb_w );
WRITE16_HANDLER( K053251_msb_w );
enum { K053251_CI0=0,K053251_CI1,K053251_CI2,K053251_CI3,K053251_CI4 };
int K053251_get_priority(int ci);
int K053251_get_palette_index(int ci);


WRITE16_HANDLER( K054000_lsb_w );
READ16_HANDLER( K054000_lsb_r );


int K054157_vh_start(int gfx_memory_region, int big, int (*scrolld)[4][2],
		     int plane0,int plane1,int plane2,int plane3,
		     void (*callback)(int, int *, int *));
READ16_HANDLER( K054157_ram_word_r );
WRITE16_HANDLER( K054157_ram_word_w );
READ16_HANDLER( K054157_ram_half_word_r );
WRITE16_HANDLER( K054157_ram_half_word_w );
READ16_HANDLER( K054157_rom_word_r );
WRITE16_HANDLER( K054157_word_w );
WRITE16_HANDLER( K054157_b_word_w );
void K054157_tilemap_draw(struct osd_bitmap *bitmap, int num, int flags, UINT32 priority);
void K054157_tilemap_draw_alpha(struct osd_bitmap *bitmap, int num, int flags, int alpha);
void K054157_mark_plane_dirty(int num);
int K054157_get_lookup(int bits);
void K054157_set_tile_bank(int bank);	/* Asterix */
