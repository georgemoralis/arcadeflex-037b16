/*
 * ported to v0.37b16
 * 
 */
package gr.codebb.arcadeflex.v037b16.mame;

public class drawgfxH {
/*TODO*///#define MAX_GFX_PLANES 8
/*TODO*///#define MAX_GFX_SIZE 64
/*TODO*///
/*TODO*///#define RGN_FRAC(num,den) (0x80000000 | (((num) & 0x0f) << 27) | (((den) & 0x0f) << 23))
/*TODO*///#define IS_FRAC(offset) ((offset) & 0x80000000)
/*TODO*///#define FRAC_NUM(offset) (((offset) >> 27) & 0x0f)
/*TODO*///#define FRAC_DEN(offset) (((offset) >> 23) & 0x0f)
/*TODO*///#define FRAC_OFFSET(offset) ((offset) & 0x007fffff)
/*TODO*///
/*TODO*///#define STEP4(START,STEP)  (START),(START)+1*(STEP),(START)+2*(STEP),(START)+3*(STEP)
/*TODO*///#define STEP8(START,STEP)  STEP4(START,STEP),STEP4((START)+4*(STEP),STEP)
/*TODO*///#define STEP16(START,STEP) STEP8(START,STEP),STEP8((START)+8*(STEP),STEP)
/*TODO*///
/*TODO*///
/*TODO*///struct GfxLayout
/*TODO*///{
/*TODO*///	UINT16 width,height; /* width and height (in pixels) of chars/sprites */
/*TODO*///	UINT32 total; /* total numer of chars/sprites in the rom */
/*TODO*///	UINT16 planes; /* number of bitplanes */
/*TODO*///	UINT32 planeoffset[MAX_GFX_PLANES]; /* start of every bitplane (in bits) */
/*TODO*///	UINT32 xoffset[MAX_GFX_SIZE]; /* position of the bit corresponding to the pixel */
/*TODO*///	UINT32 yoffset[MAX_GFX_SIZE]; /* of the given coordinates */
/*TODO*///	UINT16 charincrement; /* distance between two consecutive characters/sprites (in bits) */
/*TODO*///};
/*TODO*///
/*TODO*///#define GFX_RAW 0x12345678
/*TODO*///
/*TODO*///
/*TODO*///struct GfxElement
/*TODO*///{
/*TODO*///	int width,height;
/*TODO*///
/*TODO*///	unsigned int total_elements;	/* total number of characters/sprites */
/*TODO*///	int color_granularity;	/* number of colors for each color code */
/*TODO*///							/* (for example, 4 for 2 bitplanes gfx) */
/*TODO*///	UINT32 *colortable;	/* map color codes to screen pens */
/*TODO*///	int total_colors;
/*TODO*///	UINT32 *pen_usage;	/* an array of total_elements entries. */
/*TODO*///						/* It is a table of the pens each character uses */
/*TODO*///						/* (bit 0 = pen 0, and so on). This is used by */
/*TODO*///						/* drawgfgx() to do optimizations like skipping */
/*TODO*///						/* drawing of a totally transparent character */
/*TODO*///	UINT8 *gfxdata;		/* pixel data */
/*TODO*///	int line_modulo;	/* amount to add to get to the next line (usually = width) */
/*TODO*///	int char_modulo;	/* = line_modulo * height */
/*TODO*///	int flags;
/*TODO*///};
/*TODO*///
/*TODO*///#define GFX_PACKED				1	/* two 4bpp pixels are packed in one byte of gfxdata */
/*TODO*///#define GFX_SWAPXY				2	/* characters are mirrored along the top-left/bottom-right diagonal */
/*TODO*///#define GFX_DONT_FREE_GFXDATA	4	/* gfxdata was not malloc()ed, so don't free it on exit */
/*TODO*///
/*TODO*///
/*TODO*///struct GfxDecodeInfo
/*TODO*///{
/*TODO*///	int memory_region;	/* memory region where the data resides (usually 1) */
/*TODO*///						/* -1 marks the end of the array */
/*TODO*///	int start;	/* beginning of data to decode */
/*TODO*///	struct GfxLayout *gfxlayout;
/*TODO*///	int color_codes_start;	/* offset in the color lookup table where color codes start */
/*TODO*///	int total_color_codes;	/* total number of color codes */
/*TODO*///};
/*TODO*///
/*TODO*///
/*TODO*///struct rectangle
/*TODO*///{
/*TODO*///	int min_x,max_x;
/*TODO*///	int min_y,max_y;
/*TODO*///};
/*TODO*///
/*TODO*///struct _alpha_cache {
/*TODO*///	const UINT8 *alphas;
/*TODO*///	const UINT8 *alphad;
/*TODO*///	UINT8 alpha[0x101][0x100];
/*TODO*///};
/*TODO*///
/*TODO*///enum
/*TODO*///{
/*TODO*///	TRANSPARENCY_NONE,			/* opaque with remapping */
/*TODO*///	TRANSPARENCY_NONE_RAW,		/* opaque with no remapping */
/*TODO*///	TRANSPARENCY_PEN,			/* single pen transparency with remapping */
/*TODO*///	TRANSPARENCY_PEN_RAW,		/* single pen transparency with no remapping */
/*TODO*///	TRANSPARENCY_PENS,			/* multiple pen transparency with remapping */
/*TODO*///	TRANSPARENCY_PENS_RAW,		/* multiple pen transparency with no remapping */
/*TODO*///	TRANSPARENCY_COLOR,			/* single remapped pen transparency with remapping */
/*TODO*///	TRANSPARENCY_PEN_TABLE,		/* special pen remapping modes (see DRAWMODE_xxx below) with remapping */
/*TODO*///	TRANSPARENCY_PEN_TABLE_RAW,	/* special pen remapping modes (see DRAWMODE_xxx below) with no remapping */
/*TODO*///	TRANSPARENCY_BLEND,			/* blend two bitmaps, shifting the source and ORing to the dest with remapping */
/*TODO*///	TRANSPARENCY_BLEND_RAW,		/* blend two bitmaps, shifting the source and ORing to the dest with no remapping */
/*TODO*///	TRANSPARENCY_ALPHAONE,		/* single pen transparency, single pen alpha */
/*TODO*///	TRANSPARENCY_ALPHA,			/* single pen transparency, other pens alpha */
/*TODO*///
/*TODO*///	TRANSPARENCY_MODES			/* total number of modes; must be last */
/*TODO*///};
/*TODO*///
/*TODO*///enum
/*TODO*///{
/*TODO*///	DRAWMODE_NONE,
/*TODO*///	DRAWMODE_SOURCE,
/*TODO*///	DRAWMODE_SHADOW
/*TODO*///};
/*TODO*///
/*TODO*///
/*TODO*///INLINE void alpha_set_level(int level) {
/*TODO*///	if(level == 0)
/*TODO*///		level = -1;
/*TODO*///	alpha_cache.alphas = alpha_cache.alpha[level+1];
/*TODO*///	alpha_cache.alphad = alpha_cache.alpha[255-level];
/*TODO*///}
/*TODO*///
/*TODO*///INLINE UINT32 alpha_blend16( UINT32 d, UINT32 s )
/*TODO*///{
/*TODO*///	const UINT8 *alphas = alpha_cache.alphas;
/*TODO*///	const UINT8 *alphad = alpha_cache.alphad;
/*TODO*///	return (alphas[s & 0x1f] | (alphas[(s>>5) & 0x1f] << 5) | (alphas[(s>>10) & 0x1f] << 10))
/*TODO*///		+ (alphad[d & 0x1f] | (alphad[(d>>5) & 0x1f] << 5) | (alphad[(d>>10) & 0x1f] << 10));
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE UINT32 alpha_blend32( UINT32 d, UINT32 s )
/*TODO*///{
/*TODO*///	const UINT8 *alphas = alpha_cache.alphas;
/*TODO*///	const UINT8 *alphad = alpha_cache.alphad;
/*TODO*///	return (alphas[s & 0xff] | (alphas[(s>>8) & 0xff] << 8) | (alphas[(s>>16) & 0xff] << 16))
/*TODO*///		+ (alphad[d & 0xff] | (alphad[(d>>8) & 0xff] << 8) | (alphad[(d>>16) & 0xff] << 16));
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///    
}
