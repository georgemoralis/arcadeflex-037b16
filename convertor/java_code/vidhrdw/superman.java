/***************************************************************************

  vidhrdw/superman.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class superman
{
	
	size_t supes_videoram_size;
	size_t supes_attribram_size;
	data16_t *supes_videoram;
	data16_t *supes_attribram;
	
	static UINT16 tilemask;
	
	
	public static VhStartPtr superman_vh_start = new VhStartPtr() { public int handler() 
	{
		tilemask = 0x3fff;
		return 0;
	} };
	
	public static VhStartPtr ballbros_vh_start = new VhStartPtr() { public int handler() 
	{
		tilemask = 0x0fff;
		return 0;
	} };
	
	public static VhStopPtr superman_vh_stop = new VhStopPtr() { public void handler() 
	{
	} };
	
	/**************************************************************************/
	
	static void superman_update_palette(int bankbase)
	{
		UINT16 palette_map[32]; /* range of color table is 0-31 */
		int i,j,tile,color;
	
		memset (palette_map, 0, sizeof (palette_map));
	
		/* Find colors used in the background tile plane */
		for (i=0; i<0x400/2; i+=0x40/2)
		{
			for (j=i; j<(i+0x40/2); j++)
			{
				color = 0;
	
				tile = supes_videoram[0x800/2 + bankbase + j] & tilemask;
				if (tile != 0)
					color = supes_videoram[0xc00/2 + bankbase + j] >> 11;
	
				palette_map[color] |= Machine.gfx[0].pen_usage[tile];
			}
		}
	
		/* Find colors used in the sprite plane */
		for (i=(0x3fe/2); i>=0; i--)
		{
			color = 0;
	
			tile = supes_videoram[i + bankbase] & tilemask;
			if (tile != 0)
				color = supes_videoram[0x400/2 + bankbase + i] >> 11;
	
			palette_map[color] |= Machine.gfx[0].pen_usage[tile];
		}
	
		palette_init_used_colors();
	
		/* Now tell the palette system about those colors */
		for (i=0; i<32; i++)
		{
			int usage = palette_map[i];
	
			if (usage != 0)
			{
				for (j=1; j<16; j++)
					if (palette_map[i] & (1 << j))
						palette_used_colors[i * 16 + j] = PALETTE_COLOR_VISIBLE;
			}
		}
	
		/* background */
		palette_used_colors[0x1f0] = PALETTE_COLOR_VISIBLE;
	
		palette_recalc();
	}
	
	
	static void superman_draw_tilemap (struct osd_bitmap *bitmap,int bankbase,int attribfix,int cocktail)
	{
		int i,j;
		UINT32 x1, y1;
	
		/* Refresh the background tile plane */
		for (i=0; i<(0x400/2); i+=(0x40/2))
		{
			x1 = supes_attribram[0x408/2 + (i>>1)] | (attribfix &0x100);
			y1 = supes_attribram[0x400/2 + (i>>1)];
	
			attribfix >>= 1;
	
			for (j=i; j<(i + 0x40/2); j++)
			{
				int tile;
	
				tile = supes_videoram[0x800/2 + bankbase + j] & tilemask;
				if (tile != 0)
				{
					int x, y;
	
					x = ((x1 + ((j &0x1) << 4)) + 16) &0x1ff;
	
					if ( !cocktail )
						y = (265 - (y1 - ((j &0x1e) << 3))) &0xff;
					else
						y = ((y1 - ((j &0x1e) << 3)) - 7) &0xff;
	
	//				if ((x>0) && (y>0) && (x<388) && (y<272))
					{
						UINT32 flipx = supes_videoram[0x800/2 + bankbase + j] &0x8000;
						UINT32 flipy = supes_videoram[0x800/2 + bankbase + j] &0x4000;
						UINT32 color = supes_videoram[0xc00/2 + bankbase + j] >> 11;
	
						if (cocktail != 0)
						{
							flipx ^= 0x8000;
							flipy ^= 0x4000;
						}
	
						/* Some tiles are transparent, e.g. the gate, so we use TRANSPARENCY_PEN */
						drawgfx(bitmap,Machine.gfx[0],
							tile,
							color,
							flipx,flipy,
							x,y,
							&Machine.visible_area,
							TRANSPARENCY_PEN,0);
					}
				}
			}
		}
	}
	
	
	static void superman_draw_sprites (struct osd_bitmap *bitmap,int bankbase,int cocktail)
	{
		int sprite,i,x,y,color,flipx,flipy;
	
		/* Refresh the sprite plane */
		for (i=0x3fe/2; i>=0; i--)
		{
			sprite = supes_videoram[i + bankbase] & tilemask;
	
			if (sprite != 0)
			{
				x = (supes_videoram [0x400/2 + bankbase + i] + 16) &0x1ff;
	
				if (!cocktail)
					y = (250 - supes_attribram[i]) &0xff;
				else
					y = (10  + supes_attribram[i]) &0xff;
	
	//			if ((x>0) && (y>0) && (x<388) && (y<272))
				{
					flipx = supes_videoram[bankbase + i] &0x8000;
					flipy = supes_videoram[bankbase + i] &0x4000;
					color = supes_videoram[bankbase + i + 0x400/2] >> 11;
	
					if (cocktail != 0)
					{
						flipx ^= 0x8000;
						flipy ^= 0x4000;
					}
	
					drawgfx(bitmap,Machine.gfx[0],
						sprite,
						color,
						flipx,flipy,
						x,y,
						&Machine.visible_area,
						TRANSPARENCY_PEN,0);
				}
			}
		}
	}
	
	
	public static VhUpdatePtr superman_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int bankbase;
		int attribfix;
		int cocktail;
	
	#ifdef MAME_DEBUG
		static UINT8 dislayer[2];	/* Layer toggles for investigating Gigandes rd.5 */
		char buf[80];
	#endif
	
	#ifdef MAME_DEBUG
		if (keyboard_pressed_memory (KEYCODE_X))
		{
			dislayer[0] ^= 1;
			sprintf(buf,"tilemap: %01x",dislayer[0]);
			usrintf_showmessage(buf);
		}
		if (keyboard_pressed_memory (KEYCODE_C))
		{
			dislayer[1] ^= 1;
			sprintf(buf,"sprites: %01x",dislayer[1]);
			usrintf_showmessage(buf);
		}
	#endif
	
		/* set bank base */
		if (supes_attribram[ 0x602/2 ] &0x40)
			bankbase = 0x2000/2;
		else
			bankbase = 0x0;
	
		/* attribute fix */
		attribfix = ((supes_attribram[ 0x604/2 ] &0xff) << 8) |
					((supes_attribram[ 0x606/2 ] &0xff) << 16);
	
		/* cocktail mode */
		cocktail = supes_attribram[ 0x600/2 ] &0x40;
	
		/* update palette */
		superman_update_palette (bankbase);
	
		fillbitmap (bitmap,Machine.pens[0x1f0],&Machine.visible_area);
	
	#ifdef MAME_DEBUG
		if (dislayer[0]==0)
	#endif
		superman_draw_tilemap (bitmap,bankbase,attribfix,cocktail);
	
	#ifdef MAME_DEBUG
		if (dislayer[1]==0)
	#endif
		superman_draw_sprites (bitmap,bankbase,cocktail);
	} };
}
