/*************************************************************************

	Driver for Williams/Midway T-unit games.

**************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class wmstunit
{
	
	
	
	/* compile-time options */
	#define FAST_DMA			1		/* DMAs complete immediately; reduces number of CPU switches */
	#define LOG_DMA				0		/* DMAs are logged if the 'L' key is pressed */
	
	
	/* constants for the  DMA chip */
	enum
	{
		DMA_LRSKIP = 0,
		DMA_COMMAND,
		DMA_OFFSETLO,
		DMA_OFFSETHI,
		DMA_XSTART,
		DMA_YSTART,
		DMA_WIDTH,
		DMA_HEIGHT,
		DMA_PALETTE,
		DMA_COLOR,
		DMA_SCALE_X,
		DMA_SCALE_Y,
		DMA_TOPCLIP,
		DMA_BOTCLIP,
		DMA_UNKNOWN_E,	/* MK1/2 never write here; NBA only writes 0 */
		DMA_CONTROL,
		DMA_LEFTCLIP,	/* pseudo-register */
		DMA_RIGHTCLIP	/* pseudo-register */
	};
	
	
	
	/* graphics-related variables */
	extern UINT8 *	wms_gfx_rom;
	extern size_t	wms_gfx_rom_size;
	       UINT8	wms_gfx_rom_large;
	static data16_t	wms_control;
	static UINT8	wms_using_34020;
	
	/* videoram-related variables */
	static UINT32 	gfxbank_offset[2];
	static UINT16 *	local_videoram;
	static UINT8	videobank_select;
	
	/* DMA-related variables */
	static data16_t	dma_register[18];
	static struct
	{
		UINT32		offset;			/* source offset, in bits */
		INT32 		rowbits;		/* source bits to skip each row */
		INT32 		xpos;			/* x position, clipped */
		INT32		ypos;			/* y position, clipped */
		INT32		width;			/* horizontal pixel count */
		INT32		height;			/* vertical pixel count */
		UINT16		palette;		/* palette base */
		UINT16		color;			/* current foreground color with palette */
	
		UINT8		yflip;			/* yflip? */
		UINT8		bpp;			/* bits per pixel */
		UINT8		preskip;		/* preskip scale */
		UINT8		postskip;		/* postskip scale */
		INT32		topclip;		/* top clipping scanline */
		INT32		botclip;		/* bottom clipping scanline */
		INT32		leftclip;		/* left clipping column */
		INT32		rightclip;		/* right clipping column */
		INT32		startskip;		/* pixels to skip at start */
		INT32		endskip;		/* pixels to skip at end */
		UINT16		xstep;			/* 8.8 fixed number scale x factor */
		UINT16		ystep;			/* 8.8 fixed number scale y factor */
	} dma_state;
	
	
	
	/* prototypes */
	
	
	/* macros */
	#define TMS_SET_IRQ_LINE(x)				\
		if (wms_using_34020 != 0) 				\
			tms34020_set_irq_line(0, x); 	\
		else 								\
			tms34010_set_irq_line(0, x);	\
	
	
	
	/*************************************
	 *
	 *	Video startup
	 *
	 *************************************/
	
	public static VhStartPtr wms_tunit_vh_start = new VhStartPtr() { public int handler() 
	{
		/* allocate memory */
		local_videoram = malloc(0x100000);
	
		/* handle failure */
		if (!local_videoram)
		{
			wms_tunit_vh_stop();
			return 1;
		}
	
		/* reset all the globals */
		gfxbank_offset[0] = 0x000000;
		gfxbank_offset[1] = 0x400000;
	
		memset(dma_register, 0, sizeof(dma_register));
		memset(&dma_state, 0, sizeof(dma_state));
	
		wms_using_34020 = 0;
		return 0;
	} };
	
	
	public static VhStartPtr wms_wolfu_vh_start = new VhStartPtr() { public int handler() 
	{
		int result = wms_tunit_vh_start();
		wms_gfx_rom_large = 1;
		return result;
	} };
	
	
	public static VhStartPtr wms_revx_vh_start = new VhStartPtr() { public int handler() 
	{
		int result = wms_tunit_vh_start();
		wms_gfx_rom_large = 1;
		wms_using_34020 = 1;
		videobank_select = 1;
		return result;
	} };
	
	
	
	/*************************************
	 *
	 *	Video shutdown
	 *
	 *************************************/
	
	public static VhStopPtr wms_tunit_vh_stop = new VhStopPtr() { public void handler() 
	{
		if (local_videoram != 0)
			free(local_videoram);
		local_videoram = NULL;
	} };
	
	
	
	/*************************************
	 *
	 *	Banked graphics ROM access
	 *
	 *************************************/
	
	READ16_HANDLER( wms_tunit_gfxrom_r )
	{
		UINT8 *base = &wms_gfx_rom[gfxbank_offset[(offset >> 21) & 1]];
		offset = (offset & 0x01fffff) * 2;
		return base[offset] | (base[offset + 1] << 8);
	}
	
	
	READ16_HANDLER( wms_wolfu_gfxrom_r )
	{
		UINT8 *base = &wms_gfx_rom[gfxbank_offset[0]];
		offset *= 2;
		return base[offset] | (base[offset + 1] << 8);
	}
	
	
	
	/*************************************
	 *
	 *	Video RAM read/write
	 *
	 *************************************/
	
	WRITE16_HANDLER( wms_tunit_vram_w )
	{
		offset *= 2;
		if (videobank_select != 0)
		{
			if (ACCESSING_LSB != 0)
				local_videoram[offset] = (data & 0xff) | ((dma_register[DMA_PALETTE] & 0xff) << 8);
			if (ACCESSING_MSB != 0)
				local_videoram[offset + 1] = ((data >> 8) & 0xff) | (dma_register[DMA_PALETTE] & 0xff00);
		}
		else
		{
			if (ACCESSING_LSB != 0)
				local_videoram[offset] = (local_videoram[offset] & 0xff) | ((data & 0xff) << 8);
			if (ACCESSING_MSB != 0)
				local_videoram[offset + 1] = (local_videoram[offset + 1] & 0xff) | (data & 0xff00);
		}
	}
	
	
	WRITE16_HANDLER( wms_tunit_vram_data_w )
	{
		offset *= 2;
		if (ACCESSING_LSB != 0)
			local_videoram[offset] = (data & 0xff) | ((dma_register[DMA_PALETTE] & 0xff) << 8);
		if (ACCESSING_MSB != 0)
			local_videoram[offset + 1] = ((data >> 8) & 0xff) | (dma_register[DMA_PALETTE] & 0xff00);
	}
	
	
	WRITE16_HANDLER( wms_tunit_vram_color_w )
	{
		offset *= 2;
		if (ACCESSING_LSB != 0)
			local_videoram[offset] = (local_videoram[offset] & 0xff) | ((data & 0xff) << 8);
		if (ACCESSING_MSB != 0)
			local_videoram[offset + 1] = (local_videoram[offset + 1] & 0xff) | (data & 0xff00);
	}
	
	
	READ16_HANDLER( wms_tunit_vram_r )
	{
		offset *= 2;
		if (videobank_select != 0)
			return (local_videoram[offset] & 0x00ff) | (local_videoram[offset + 1] << 8);
		else
			return (local_videoram[offset] >> 8) | (local_videoram[offset + 1] & 0xff00);
	}
	
	
	READ16_HANDLER( wms_tunit_vram_data_r )
	{
		offset *= 2;
		return (local_videoram[offset] & 0x00ff) | (local_videoram[offset + 1] << 8);
	}
	
	
	READ16_HANDLER( wms_tunit_vram_color_r )
	{
		offset *= 2;
		return (local_videoram[offset] >> 8) | (local_videoram[offset + 1] & 0xff00);
	}
	
	
	
	/*************************************
	 *
	 *	Shift register read/write
	 *
	 *************************************/
	
	void wms_tunit_to_shiftreg(UINT32 address, UINT16 *shiftreg)
	{
		memcpy(shiftreg, &local_videoram[address >> 3], 2 * 512 * sizeof(UINT16));
	}
	
	
	void wms_tunit_from_shiftreg(UINT32 address, UINT16 *shiftreg)
	{
		memcpy(&local_videoram[address >> 3], shiftreg, 2 * 512 * sizeof(UINT16));
	}
	
	
	
	/*************************************
	 *
	 *	Control register
	 *
	 *************************************/
	
	WRITE16_HANDLER( wms_tunit_control_w )
	{
		/*
			other important bits:
				bit 2 (0x0004) is toggled periodically
		*/
		logerror("T-unit control = %04X\n", data);
	
		COMBINE_DATA(&wms_control);
	
		/* gfx bank select is bit 7 */
		if (!(wms_control & 0x0080) || !wms_gfx_rom_large)
			gfxbank_offset[0] = 0x000000;
		else
			gfxbank_offset[0] = 0x800000;
	
		/* video bank select is bit 5 */
		videobank_select = (wms_control >> 5) & 1;
	}
	
	
	WRITE16_HANDLER( wms_wolfu_control_w )
	{
		/*
			other important bits:
				bit 2 (0x0004) is toggled periodically
		*/
		logerror("Wolf-unit control = %04X\n", data);
	
		COMBINE_DATA(&wms_control);
	
		/* gfx bank select is bits 8-9 */
		gfxbank_offset[0] = 0x800000 * ((wms_control >> 8) & 3);
	
		/* video bank select is unknown */
		videobank_select = (wms_control >> 11) & 1;
	}
	
	
	READ16_HANDLER( wms_wolfu_control_r )
	{
		return wms_control;
	}
	
	
	
	/*************************************
	 *
	 *	Palette handlers
	 *
	 *************************************/
	
	WRITE16_HANDLER( wms_tunit_paletteram_w )
	{
		int newword, r, g, b;
	
		COMBINE_DATA(&paletteram16[offset]);
		newword = paletteram16[offset];
	
		r = (newword >> 10) & 0x1f;
		g = (newword >>  5) & 0x1f;
		b = (newword      ) & 0x1f;
	
		r = (r << 3) | (r >> 2);
		g = (g << 3) | (g >> 2);
		b = (b << 3) | (b >> 2);
	
		palette_change_color(offset, r, g, b);
	}
	
	
	
	/*************************************
	 *
	 *	DMA drawing routines
	 *
	 *************************************/
	
	/*** constant definitions ***/
	#define	PIXEL_SKIP		0
	#define PIXEL_COLOR		1
	#define PIXEL_COPY		2
	
	#define XFLIP_NO		0
	#define XFLIP_YES		1
	
	#define SKIP_NO			0
	#define SKIP_YES		1
	
	#define SCALE_NO		0
	#define SCALE_YES		1
	
	
	typedef void (*dma_draw_func)(void);
	
	
	/*** fast pixel extractors ***/
	#if !defined(ALIGN_SHORTS) && defined(LSB_FIRST)
	#define EXTRACTGEN(m)	((*(UINT16 *)&base[o >> 3] >> (o & 7)) & (m))
	#elif defined(powerc)
	#define EXTRACTGEN(m)	((__lhbrx(base, o >> 3) >> (o & 7)) & (m))
	#else
	#define EXTRACTGEN(m)	(((base[o >> 3] | (base[(o >> 3) + 1] << 8)) >> (o & 7)) & (m))
	#endif
	
	/*** core blitter routine macro ***/
	#define DMA_DRAW_FUNC_BODY(name, bitsperpixel, extractor, xflip, skip, scale, zero, nonzero) \
	{																				\
		int height = dma_state.height << 8;											\
		UINT8 *base = wms_gfx_rom;													\
		UINT32 offset = dma_state.offset;											\
		UINT16 pal = dma_state.palette;												\
		UINT16 color = pal | dma_state.color;										\
		int sy = dma_state.ypos, iy = 0, ty;										\
		int bpp = bitsperpixel;														\
		int mask = (1 << bpp) - 1;													\
		int xstep = scale ? dma_state.xstep : 0x100;								\
																					\
		/* loop over the height */													\
		while (iy < height)															\
		{																			\
			int startskip = dma_state.startskip << 8;								\
			int endskip = dma_state.endskip << 8;									\
			int width = dma_state.width << 8;										\
			int sx = dma_state.xpos, ix = 0, tx;									\
			UINT32 o = offset;														\
			int pre, post;															\
			UINT16 *d;																\
																					\
			/* handle skipping */													\
			if (skip != 0)																\
			{																		\
				UINT8 value = EXTRACTGEN(0xff);										\
				o += 8;																\
																					\
				/* adjust for preskip */											\
				pre = (value & 0x0f) << (dma_state.preskip + 8);					\
				tx = pre / xstep;													\
				xflip ? (sx -= tx) : (sx += tx);									\
				ix += tx * xstep;													\
																					\
				/* adjust for postskip */											\
				post = ((value >> 4) & 0x0f) << (dma_state.postskip + 8);			\
				width -= post;														\
				endskip -= post;													\
			}																		\
																					\
			/* handle Y clipping */													\
			if (sy < dma_state.topclip || sy > dma_state.botclip)					\
				goto clipy;															\
																					\
			/* handle left clip */													\
			if (!xflip && sx < 0)													\
			{																		\
				tx = -sx * xstep;													\
				ix += tx;															\
				o += (tx >> 8) * bpp;												\
				sx = 0;																\
			}																		\
			else if (xflip && sx >= 512)											\
			{																		\
				tx = (sx - 511) * xstep;											\
				ix += tx;															\
				o += (tx >> 8) * bpp;												\
				sx = 511;															\
			}																		\
																					\
			/* handle start skip */													\
			if (ix < startskip)														\
			{																		\
				tx = ((startskip - ix) / xstep) * xstep;							\
				ix += tx;															\
				o += (tx >> 8) * bpp;												\
			}																		\
																					\
			/* handle end skip */													\
			if ((width >> 8) > dma_state.width - dma_state.endskip)					\
				width = (dma_state.width - dma_state.endskip) << 8;					\
																					\
			/* determine destination pointer */										\
			d = &local_videoram[sy * 512 + sx];										\
																					\
			/* loop until we draw the entire width */								\
			while (ix < width && ((!xflip && sx < 512) || (xflip && sx >= 0)))		\
			{																		\
				/* special case similar handling of zero/non-zero */				\
				if (zero == nonzero)												\
				{																	\
					if (zero == PIXEL_COLOR)										\
						*d = color;													\
					else if (zero == PIXEL_COPY)									\
						*d = (extractor(mask)) | pal;								\
				}																	\
																					\
				/* otherwise, read the pixel and look */							\
				else																\
				{																	\
					int pixel = (extractor(mask));									\
																					\
					/* non-zero pixel case */										\
					if (pixel != 0)														\
					{																\
						if (nonzero == PIXEL_COLOR)									\
							*d = color;												\
						else if (nonzero == PIXEL_COPY)								\
							*d = pixel | pal;										\
					}																\
																					\
					/* zero pixel case */											\
					else															\
					{																\
						if (zero == PIXEL_COLOR)									\
							*d = color;												\
						else if (zero == PIXEL_COPY)								\
							*d = pal;												\
					}																\
				}																	\
																					\
				/* update pointers */												\
				if (xflip != 0) 															\
					d--, sx--; 														\
				else 																\
					d++, sx++;														\
																					\
				/* advance to the next pixel */										\
				if (!scale)															\
				{																	\
					ix += 0x100;													\
					o += bpp;														\
				}																	\
				else																\
				{																	\
					tx = ix >> 8;													\
					ix += xstep;													\
					tx = (ix >> 8) - tx;											\
					o += bpp * tx;													\
				}																	\
			}																		\
																					\
		clipy:																		\
			/* advance to the next row */											\
			dma_state.yflip ? sy-- : sy++;											\
			if (!scale)																\
			{																		\
				iy += 0x100;														\
				width = dma_state.width;											\
				if (skip != 0)															\
				{																	\
					offset += 8;													\
					width -= (pre + post) >> 8;										\
					if (width > 0) offset += width * bpp;							\
				}																	\
				else																\
					offset += width * bpp;											\
			}																		\
			else																	\
			{																		\
				ty = iy >> 8;														\
				iy += dma_state.ystep;												\
				ty = (iy >> 8) - ty;												\
				if (!skip)															\
					offset += ty * dma_state.width * bpp;							\
				else if (ty--)														\
				{																	\
					o = offset + 8;													\
					width = dma_state.width - ((pre + post) >> 8);					\
					if (width > 0) o += width * bpp;								\
					while (ty--)													\
					{																\
						UINT8 value = EXTRACTGEN(0xff);								\
						o += 8;														\
						pre = (value & 0x0f) << dma_state.preskip;					\
						post = ((value >> 4) & 0x0f) << dma_state.postskip;			\
						width = dma_state.width - pre - post;						\
						if (width > 0) o += width * bpp;							\
					}																\
					offset = o;														\
				}																	\
			}																		\
		}																			\
	}
	
	
	/*** slightly simplified one for most blitters ***/
	#define DMA_DRAW_FUNC(name, bpp, extract, xflip, skip, scale, zero, nonzero)	\
	static void name(void)															\
	{																				\
		DMA_DRAW_FUNC_BODY(name, bpp, extract, xflip, skip, scale, zero, nonzero)	\
	}
	
	/*** empty blitter ***/
	static void dma_draw_none(void)
	{
	}
	
	/*** super macro for declaring an entire blitter family ***/
	#define DECLARE_BLITTER_SET(prefix, bpp, extract, skip, scale)										\
	DMA_DRAW_FUNC(prefix##_p0,      bpp, extract, XFLIP_NO,  skip, scale, PIXEL_COPY,  PIXEL_SKIP)		\
	DMA_DRAW_FUNC(prefix##_p1,      bpp, extract, XFLIP_NO,  skip, scale, PIXEL_SKIP,  PIXEL_COPY)		\
	DMA_DRAW_FUNC(prefix##_c0,      bpp, extract, XFLIP_NO,  skip, scale, PIXEL_COLOR, PIXEL_SKIP)		\
	DMA_DRAW_FUNC(prefix##_c1,      bpp, extract, XFLIP_NO,  skip, scale, PIXEL_SKIP,  PIXEL_COLOR)		\
	DMA_DRAW_FUNC(prefix##_p0p1,    bpp, extract, XFLIP_NO,  skip, scale, PIXEL_COPY,  PIXEL_COPY)		\
	DMA_DRAW_FUNC(prefix##_c0c1,    bpp, extract, XFLIP_NO,  skip, scale, PIXEL_COLOR, PIXEL_COLOR)		\
	DMA_DRAW_FUNC(prefix##_c0p1,    bpp, extract, XFLIP_NO,  skip, scale, PIXEL_COLOR, PIXEL_COPY)		\
	DMA_DRAW_FUNC(prefix##_p0c1,    bpp, extract, XFLIP_NO,  skip, scale, PIXEL_COPY,  PIXEL_COLOR)		\
																										\
	DMA_DRAW_FUNC(prefix##_p0_xf,   bpp, extract, XFLIP_YES, skip, scale, PIXEL_COPY,  PIXEL_SKIP)		\
	DMA_DRAW_FUNC(prefix##_p1_xf,   bpp, extract, XFLIP_YES, skip, scale, PIXEL_SKIP,  PIXEL_COPY)		\
	DMA_DRAW_FUNC(prefix##_c0_xf,   bpp, extract, XFLIP_YES, skip, scale, PIXEL_COLOR, PIXEL_SKIP)		\
	DMA_DRAW_FUNC(prefix##_c1_xf,   bpp, extract, XFLIP_YES, skip, scale, PIXEL_SKIP,  PIXEL_COLOR)		\
	DMA_DRAW_FUNC(prefix##_p0p1_xf, bpp, extract, XFLIP_YES, skip, scale, PIXEL_COPY,  PIXEL_COPY)		\
	DMA_DRAW_FUNC(prefix##_c0c1_xf, bpp, extract, XFLIP_YES, skip, scale, PIXEL_COLOR, PIXEL_COLOR)		\
	DMA_DRAW_FUNC(prefix##_c0p1_xf, bpp, extract, XFLIP_YES, skip, scale, PIXEL_COLOR, PIXEL_COPY)		\
	DMA_DRAW_FUNC(prefix##_p0c1_xf, bpp, extract, XFLIP_YES, skip, scale, PIXEL_COPY,  PIXEL_COLOR)		\
																												\
	static dma_draw_func prefix[32] =																			\
	{																											\
	/*	B0:N / B1:N			B0:Y / B1:N			B0:N / B1:Y			B0:Y / B1:Y */								\
		dma_draw_none,		prefix##_p0,		prefix##_p1,		prefix##_p0p1,		/* no color */ 			\
		prefix##_c0,		prefix##_c0,		prefix##_c0p1,		prefix##_c0p1,		/* color 0 pixels */ 	\
		prefix##_c1,		prefix##_p0c1,		prefix##_c1,		prefix##_p0c1,		/* color non-0 pixels */\
		prefix##_c0c1,		prefix##_c0c1,		prefix##_c0c1,		prefix##_c0c1,		/* fill */ 				\
																												\
		dma_draw_none,		prefix##_p0_xf,		prefix##_p1_xf,		prefix##_p0p1_xf,	/* no color */ 			\
		prefix##_c0_xf,		prefix##_c0_xf,		prefix##_c0p1_xf,	prefix##_c0p1_xf,	/* color 0 pixels */ 	\
		prefix##_c1_xf,		prefix##_p0c1_xf,	prefix##_c1_xf,		prefix##_p0c1_xf,	/* color non-0 pixels */\
		prefix##_c0c1_xf,	prefix##_c0c1_xf,	prefix##_c0c1_xf,	prefix##_c0c1_xf	/* fill */ 				\
	};
	
	
	/* allow for custom blitters */
	#ifdef WMS_TUNIT_CUSTOM_BLITTERS
	#endif
	
	
	/*** blitter family declarations ***/
	DECLARE_BLITTER_SET(dma_draw_skip_scale,       dma_state.bpp, EXTRACTGEN,   SKIP_YES, SCALE_YES)
	DECLARE_BLITTER_SET(dma_draw_noskip_scale,     dma_state.bpp, EXTRACTGEN,   SKIP_NO,  SCALE_YES)
	DECLARE_BLITTER_SET(dma_draw_skip_noscale,     dma_state.bpp, EXTRACTGEN,   SKIP_YES, SCALE_NO)
	DECLARE_BLITTER_SET(dma_draw_noskip_noscale,   dma_state.bpp, EXTRACTGEN,   SKIP_NO,  SCALE_NO)
	
	
	
	/*************************************
	 *
	 *	DMA finished callback
	 *
	 *************************************/
	
	static int temp_irq_callback(int irqline)
	{
		TMS_SET_IRQ_LINE(CLEAR_LINE);
		return 0;
	}
	
	
	static void dma_callback(int is_in_34010_context)
	{
		dma_register[DMA_COMMAND] &= ~0x8000; /* tell the cpu we're done */
		if (is_in_34010_context != 0)
		{
			if (wms_using_34020 != 0)
				tms34020_set_irq_callback(temp_irq_callback);
			else
				tms34010_set_irq_callback(temp_irq_callback);
			TMS_SET_IRQ_LINE(ASSERT_LINE);
		}
		else
			cpu_cause_interrupt(0,TMS34010_INT1);
	}
	
	
	
	/*************************************
	 *
	 *	DMA reader
	 *
	 *************************************/
	
	READ16_HANDLER( wms_tunit_dma_r )
	{
		return dma_register[offset];
	}
	
	
	
	/*************************************
	 *
	 *	DMA write handler
	 *
	 *************************************/
	
	/*
	 * DMA registers
	 * ------------------
	 *
	 *  Register | Bit              | Use
	 * ----------+-FEDCBA9876543210-+------------
	 *     0     | xxxxxxxx-------- | pixels to drop at the start of each row
	 *           | --------xxxxxxxx | pixels to drop at the end of each row
	 *     1     | x--------------- | trigger write (or clear if zero)
	 *           | -421------------ | image bpp (0=8)
	 *           | ----84---------- | post skip size = (1<<x)
	 *           | ------21-------- | pre skip size = (1<<x)
	 *           | --------8------- | pre/post skip enable
	 *           | ---------4------ | pixel skip control
	 *           | ----------2----- | flip y
	 *           | -----------1---- | flip x
	 *           | ------------8--- | blit nonzero pixels as color
	 *           | -------------4-- | blit zero pixels as color
	 *           | --------------2- | blit nonzero pixels
	 *           | ---------------1 | blit zero pixels
	 *     2     | xxxxxxxxxxxxxxxx | source address low word
	 *     3     | xxxxxxxxxxxxxxxx | source address high word
	 *     4     | xxxxxxxxxxxxxxxx | detination x
	 *     5     | xxxxxxxxxxxxxxxx | destination y
	 *     6     | xxxxxxxxxxxxxxxx | image columns
	 *     7     | xxxxxxxxxxxxxxxx | image rows
	 *     8     | xxxxxxxxxxxxxxxx | palette
	 *     9     | xxxxxxxxxxxxxxxx | color
	 *    10     | xxxxxxxxxxxxxxxx | scale x
	 *    11     | xxxxxxxxxxxxxxxx | scale y
	 *    12     | xxxxxxxxxxxxxxxx | top clip
	 *    13     | xxxxxxxxxxxxxxxx | bottom clip
	 */
	
	WRITE16_HANDLER( wms_tunit_dma_w )
	{
		static const UINT8 register_map[2][16] =
		{
			{ 0,1,2,3,4,5,6,7,8,9,10,11,16,17,14,15 },
			{ 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15 }
		};
		int regbank = (dma_register[DMA_CONTROL] >> 5) & 1;
		int command, bpp, regnum;
		UINT32 gfxoffset;
	
		/* blend with the current register contents */
		regnum = register_map[regbank][offset];
		COMBINE_DATA(&dma_register[regnum]);
	
	#if LOG_DMA
		if (keyboard_pressed(KEYCODE_L))
			logerror("%08X:DMA %d = %04X\n", cpu_get_pc(), regnum, data);
	#endif
	
		/* only writes to DMA_COMMAND actually cause actions */
		if (regnum != DMA_COMMAND)
			return;
	
		/* high bit triggers action */
		command = dma_register[DMA_COMMAND];
		if (!(command & 0x8000))
		{
			TMS_SET_IRQ_LINE(CLEAR_LINE);
			return;
		}
	
	#if LOG_DMA
		if (keyboard_pressed(KEYCODE_L))
		{
			logerror("DMA command %04X: (bpp=%d skip=%d xflip=%d yflip=%d preskip=%d postskip=%d)\n",
					command, (command >> 12) & 7, (command >> 7) & 1, (command >> 4) & 1, (command >> 5) & 1, (command >> 8) & 3, (command >> 10) & 3);
			logerror("  offset=%08X pos=(%d,%d) w=%d h=%d clip=(%d,%d)\n",
					dma_register[DMA_OFFSETLO] | (dma_register[DMA_OFFSETHI] << 16),
					(INT16)dma_register[DMA_XSTART], (INT16)dma_register[DMA_YSTART],
					dma_register[DMA_WIDTH], dma_register[DMA_HEIGHT],
					dma_register[DMA_TOPCLIP], dma_register[DMA_BOTCLIP]);
			logerror("  palette=%04X color=%04X lskip=%02X rskip=%02X xstep=%04X ystep=%04X unkE=%04X unkF=%04X\n",
					dma_register[DMA_PALETTE], dma_register[DMA_COLOR],
					dma_register[DMA_LRSKIP] >> 8, dma_register[DMA_LRSKIP] & 0xff,
					dma_register[DMA_SCALE_X], dma_register[DMA_SCALE_Y], dma_register[DMA_UNKNOWN_E],
					dma_register[DMA_CONTROL]);
			logerror("----\n");
		}
	#endif
	
		profiler_mark(PROFILER_USER1);
	
		/* determine bpp */
		bpp = (command >> 12) & 7;
	
		/* fill in the basic data */
		dma_state.xpos = (INT16)dma_register[DMA_XSTART];
		dma_state.ypos = (INT16)dma_register[DMA_YSTART];
		dma_state.width = dma_register[DMA_WIDTH];
		dma_state.height = dma_register[DMA_HEIGHT];
		dma_state.palette = dma_register[DMA_PALETTE] & 0x7f00;
		dma_state.color = dma_register[DMA_COLOR] & 0xff;
	
		/* fill in the rev 2 data */
		dma_state.yflip = (command & 0x20) >> 5;
		dma_state.bpp = bpp ? bpp : 8;
		dma_state.preskip = (command >> 8) & 3;
		dma_state.postskip = (command >> 10) & 3;
		dma_state.topclip = (INT16)dma_register[DMA_TOPCLIP];
		dma_state.botclip = (INT16)dma_register[DMA_BOTCLIP];
		dma_state.leftclip = (INT16)dma_register[DMA_LEFTCLIP];
		dma_state.rightclip = (INT16)dma_register[DMA_RIGHTCLIP];
		dma_state.xstep = dma_register[DMA_SCALE_X] ? dma_register[DMA_SCALE_X] : 0x100;
		dma_state.ystep = dma_register[DMA_SCALE_Y] ? dma_register[DMA_SCALE_Y] : 0x100;
	
		/* clip the clippers */
		if (dma_state.topclip < 0) dma_state.topclip = 0;
		if (dma_state.botclip > 512 || dma_state.botclip == 0) dma_state.botclip = 512;
		if (dma_state.leftclip < 0) dma_state.leftclip = 0;
		if (dma_state.rightclip > 512) dma_state.rightclip = 512;
	
		/* determine the offset */
		gfxoffset = dma_register[DMA_OFFSETLO] | (dma_register[DMA_OFFSETHI] << 16);
	
		/* special case: drawing mode C doesn't need to know about any pixel data */
		if ((command & 0x0f) == 0x0c)
			gfxoffset = 0;
	
		/* determine the location */
		if (!wms_gfx_rom_large && gfxoffset >= 0x2000000)
			gfxoffset -= 0x2000000;
		if (gfxoffset >= 0xf8000000)
			gfxoffset -= 0xf8000000;
		if (gfxoffset < 0x10000000)
			dma_state.offset = gfxoffset;
		else
		{
			logerror("DMA source out of range: %08X\n", gfxoffset);
			goto skipdma;
		}
	
		/* there seems to be two types of behavior for the DMA chip */
		/* for MK1 and MK2, the upper byte of the LRSKIP is the     */
		/* starting skip value, and the lower byte is the ending    */
		/* skip value; for the NBA Jam, Hangtime, and Open Ice, the */
		/* full word seems to be the starting skip value.           */
		if ((command & 0x40) != 0)
		{
			dma_state.startskip = dma_register[DMA_LRSKIP] & 0xff;
			dma_state.endskip = dma_register[DMA_LRSKIP] >> 8;
		}
		else
		{
			dma_state.startskip = 0;
			dma_state.endskip = dma_register[DMA_LRSKIP];
		}
	
		/* then draw */
		if (dma_state.xstep == 0x100 && dma_state.ystep == 0x100)
		{
			if ((command & 0x80) != 0)
				(*dma_draw_skip_noscale[command & 0x1f])();
			else
				(*dma_draw_noskip_noscale[command & 0x1f])();
		}
		else
		{
			if ((command & 0x80) != 0)
				(*dma_draw_skip_scale[command & 0x1f])();
			else
				(*dma_draw_noskip_scale[command & 0x1f])();
		}
	
		/* signal we're done */
	skipdma:
	
		/* special case for Open Ice: use a timer for command 0x8000, which is */
		/* used to initiate the DMA. What they do is start the DMA, *then* set */
		/* up the memory for it, which means that there must be some non-zero  */
		/* delay that gives them enough time to build up the DMA command list  */
		if (FAST_DMA != 0)
		{
			if (command != 0x8000)
				dma_callback(1);
			else
			{
				TMS_SET_IRQ_LINE(CLEAR_LINE);
				timer_set(TIME_IN_NSEC(41 * dma_state.width * dma_state.height * 4), 0, dma_callback);
			}
		}
		else
		{
			TMS_SET_IRQ_LINE(CLEAR_LINE);
			timer_set(TIME_IN_NSEC(41 * dma_state.width * dma_state.height), 0, dma_callback);
		}
	
		profiler_mark(PROFILER_END);
	}
	
	
	
	/*************************************
	 *
	 *	34010 display address callback
	 *
	 *************************************/
	
	void wms_tunit_display_addr_changed(UINT32 offs, int rowbytes, int scanline)
	{
	}
	
	
	
	/*************************************
	 *
	 *	Core refresh routine
	 *
	 *************************************/
	
	public static VhUpdatePtr wms_tunit_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int v, width, xoffs;
		UINT32 offset;
	
		// recompute the palette
		palette_recalc();
	
		/* determine the base of the videoram */
		if (wms_using_34020 != 0)
			offset = (tms34020_get_DPYSTRT(0) >> 5) & 0x3ffff;
		else
			offset = ((~tms34010_get_DPYSTRT(0) & 0x1ff0) << 5) & 0x3ffff;
		logerror("Screen offset = %06X\n", offset);
	
		/* determine how many pixels to copy */
		xoffs = Machine.visible_area.min_x;
		width = Machine.visible_area.max_x - xoffs + 1;
	
		/* adjust the offset */
		offset += xoffs;
		offset += 512 * Machine.visible_area.min_y;
		offset &= 0x3ffff;
	
		/* loop over rows */
		for (v = Machine.visible_area.min_y; v <= Machine.visible_area.max_y; v++)
		{
			draw_scanline16(bitmap, xoffs, v, width, &local_videoram[offset], Machine.pens, -1);
			offset = (offset + 512) & 0x3ffff;
		}
	} };
}
