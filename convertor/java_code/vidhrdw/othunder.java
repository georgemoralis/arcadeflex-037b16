/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class othunder
{
	
	#define TC0100SCN_GFX_NUM 1
	
	data16_t *othunder_ram;
	
	struct tempsprite
	{
		int gfx;
		int code,color;
		int flipx,flipy;
		int x,y;
		int zoomx,zoomy;
		int primask;
	};
	static struct tempsprite *spritelist;
	
	static int taito_hide_pixels;
	
	/**********************************************************/
	
	static int has_TC0110PCR(void)
	{
		const struct Memory_WriteAddress16 *mwa;
	
		/* scan the memory handlers and see if the TC0110PCR is used */
	
		mwa = Machine.drv.cpu[0].memory_write;
		if (mwa != 0)
		{
			while (!IS_MEMPORT_END(mwa))
			{
				if (!IS_MEMPORT_MARKER(mwa))
				{
					if (mwa.handler == TC0110PCR_step1_rbswap_word_w)
						return 1;
				}
				mwa++;
			}
		}
	
		return 0;
	}
	
	
	static public static VhStartPtr othunder_core_vh_start = new VhStartPtr() { public int handler() 
	{
		/* Up to $800/8 big sprites, requires 0x100 * sizeof(*spritelist)
		   Multiply this by 32 to give room for the number of small sprites,
		   which are what actually get put in the structure. */
	
		spritelist = malloc(0x2000 * sizeof(*spritelist));
		if (!spritelist)
			return 1;
	
		if (TC0100SCN_vh_start(1,TC0100SCN_GFX_NUM,taito_hide_pixels,0,0,0,0,0,0))
		{
			othunder_vh_stop();
			return 1;
		}
	
		if (has_TC0110PCR())
			if (TC0110PCR_vh_start())
			{
				othunder_vh_stop();
				return 1;
			}
	
		return 0;
	} };
	
	public static VhStartPtr othunder_vh_start = new VhStartPtr() { public int handler() 
	{
		/* There is a problem here. 4 is correct for text layer/sprite
		   alignment, but the bg layers [or one of them] are wrong */
	
		taito_hide_pixels = 4;
		return (othunder_core_vh_start());
	} };
	
	public static VhStopPtr othunder_vh_stop = new VhStopPtr() { public void handler() 
	{
		free(spritelist);
		spritelist = 0;
	
		TC0100SCN_vh_stop();
	
		if (has_TC0110PCR())
			TC0110PCR_vh_stop();
	} };
	
	
	/*********************************************************
					PALETTE
	*********************************************************/
	
	static void othunder_update_palette (void)
	{
		int i,j;
		data16_t *spritemap = (data16_t *)memory_region(REGION_USER1);
		UINT16 tile_mask = (Machine.gfx[0].total_elements) - 1;
		int map_offset,sprite_chunk,code;
		int offs,data,tilenum,color;
		unsigned short palette_map[256];
		memset (palette_map, 0, sizeof (palette_map));
	
		for (offs = (spriteram_size/2)-4;offs >=0;offs -= 4)
		{
			data = spriteram16[offs+2];
			color = (data &0xff00) >> 8;
	
			data = spriteram16[offs+3];
			tilenum = data &0x1fff;
	
			if (tilenum != 0)
			{
				map_offset = tilenum << 5;
	
				for (sprite_chunk=0;sprite_chunk<32;sprite_chunk++)
				{
					i = sprite_chunk % 4;   /* 4 chunks across */
					j = sprite_chunk / 4;   /* 8 chunks down */
	
					code = spritemap[map_offset + i + (j<<2)] &tile_mask;
	
					palette_map[color] |= Machine.gfx[0].pen_usage[code];
				}
			}
		}
	
		/* Tell MAME about the color usage */
		for (i = 0;i < 256;i++)
		{
			int usage = palette_map[i];
	
			if (usage != 0)
			{
				if (palette_map[i] & (1 << 0))
					palette_used_colors[i * 16 + 0] = PALETTE_COLOR_USED;
				for (j = 1; j < 16; j++)
					if (palette_map[i] & (1 << j))
						palette_used_colors[i * 16 + j] = PALETTE_COLOR_USED;
			}
		}
	}
	
	
	
	
	/************************************************************
				SPRITE DRAW ROUTINE
	
	It draws a series of small tiles ("chunks") together to
	create a big sprite. The spritemap rom provides the lookup
	table for this. We look up the 16x8 sprite chunks from
	the spritemap rom, creating each 64x64 sprite as follows:
	
		 0  1  2  3
		 4  5  6  7
		 8  9 10 11
		12 13 14 15
		16 17 18 19
		20 21 22 23
		24 25 26 27
		28 29 30 31
	
	The game makes heavy use of sprite zooming.
	
			***
	
	NB: unused portions of the spritemap rom contain hex FF's.
	It is a useful coding check to warn in the log if these
	are being accessed. [They can be inadvertently while
	spriteram is being tested, take no notice of that.]
	
	
			Othunder (modified table from Raine)
	
			Byte | Bit(s) | Description
			-----+76543210+-------------------------------------
			  0  |xxxxxxx.| ZoomY (0 min, 63 max - msb unused as sprites are 64x64)
			  0  |.......x| Y position (High)
			  1  |xxxxxxxx| Y position (Low)
			  2  |x.......| Sprite/BG Priority (0=sprites high)
			  2  |.x......| Flip X
			  2  |..?????.| unknown/unused ?
			  2  |.......x| X position (High)
			  3  |xxxxxxxx| X position (Low)
			  4  |xxxxxxxx| Palette bank
			  5  |?.......| unknown/unused ?
			  5  |.xxxxxxx| ZoomX (0 min, 63 max - msb unused as sprites are 64x64)
			  6  |x.......| Flip Y
			  6  |.??.....| unknown/unused ?
			  6  |...xxxxx| Sprite Tile high (2 msbs unused - 3/4 of spritemap rom empty)
			  7  |xxxxxxxx| Sprite Tile low
	
	********************************************************/
	
	
	static void othunder_draw_sprites_16x8(struct osd_bitmap *bitmap,int *primasks,int y_offs)
	{
		data16_t *spritemap = (data16_t *)memory_region(REGION_USER1);
		UINT16 tile_mask = (Machine.gfx[0].total_elements) - 1;
		int offs, data, tilenum, color, flipx, flipy;
		int x, y, priority, curx, cury;
		int sprites_flipscreen = 0;
		int zoomx, zoomy, zx, zy;
		int sprite_chunk,map_offset,code,j,k,px,py;
		int bad_chunks;
	
		/* pdrawgfx() needs us to draw sprites front to back, so we have to build a list
		   while processing sprite ram and then draw them all at the end */
		struct tempsprite *sprite_ptr = spritelist;
	
		for (offs = (spriteram_size/2)-4;offs >=0;offs -= 4)
		{
			data = spriteram16[offs+0];
			zoomy = (data & 0xfe00) >> 9;
			y = data & 0x1ff;
	
			data = spriteram16[offs+1];
			flipx = (data & 0x4000) >> 14;
			priority = (data & 0x8000) >> 15;
			x = data & 0x1ff;
	
			data = spriteram16[offs+2];
			color = (data & 0xff00) >> 8;
			zoomx = (data & 0x7f);
	
			data = spriteram16[offs+3];
			tilenum = data & 0x1fff;	// $80000 spritemap rom maps up to $2000 64x64 sprites
			flipy = (data & 0x8000) >> 15;
	
			if (!tilenum) continue;
	
			map_offset = tilenum << 5;
	
			zoomx += 1;
			zoomy += 1;
	
			y += y_offs;
	
			/* treat coords as signed */
			if (x>0x140) x -= 0x200;
			if (y>0x140) y -= 0x200;
	
			bad_chunks = 0;
	
			for (sprite_chunk=0;sprite_chunk<32;sprite_chunk++)
			{
				k = sprite_chunk % 4;   /* 4 chunks per row */
				j = sprite_chunk / 4;   /* 8 rows */
	
				px = k;
				py = j;
				if (flipx != 0)  px = 3-k;	/* pick tiles back to front for x and y flips */
				if (flipy != 0)  py = 7-j;
	
				code = spritemap[map_offset + px + (py<<2)] &tile_mask;
	
				if (code==0xffff)
				{
					bad_chunks += 1;
					continue;
				}
	
				curx = x + ((k*zoomx)/4);
				cury = y + ((j*zoomy)/8);
	
				zx= x + (((k+1)*zoomx)/4) - curx;
				zy= y + (((j+1)*zoomy)/8) - cury;
	
				if (sprites_flipscreen != 0)
				{
					/* -zx/y is there to fix zoomed sprite coords in screenflip.
					   drawgfxzoom does not know to draw from flip-side of sprites when
					   screen is flipped; so we must correct the coords ourselves. */
	
					curx = 320 - curx - zx;
					cury = 256 - cury - zy;
					flipx = !flipx;
					flipy = !flipy;
				}
	
				sprite_ptr.code = code;
				sprite_ptr.color = color;
				sprite_ptr.flipx = flipx;
				sprite_ptr.flipy = flipy;
				sprite_ptr.x = curx;
				sprite_ptr.y = cury;
				sprite_ptr.zoomx = zx << 12;
				sprite_ptr.zoomy = zy << 13;
	
				if (primasks != 0)
				{
					sprite_ptr.primask = primasks[priority];
					sprite_ptr++;
				}
				else
				{
					drawgfxzoom(bitmap,Machine.gfx[0],
							sprite_ptr.code,
							sprite_ptr.color,
							sprite_ptr.flipx,sprite_ptr.flipy,
							sprite_ptr.x,sprite_ptr.y,
							&Machine.visible_area,TRANSPARENCY_PEN,0,
							sprite_ptr.zoomx,sprite_ptr.zoomy);
				}
			}
	
			if (bad_chunks != 0)
	logerror("Sprite number %04x had %02x invalid chunks\n",tilenum,bad_chunks);
		}
	
		/* this happens only if primsks != NULL */
		while (sprite_ptr != spritelist)
		{
			sprite_ptr--;
	
			pdrawgfxzoom(bitmap,Machine.gfx[0],
					sprite_ptr.code,
					sprite_ptr.color,
					sprite_ptr.flipx,sprite_ptr.flipy,
					sprite_ptr.x,sprite_ptr.y,
					&Machine.visible_area,TRANSPARENCY_PEN,0,
					sprite_ptr.zoomx,sprite_ptr.zoomy,
					sprite_ptr.primask);
		}
	}
	
	
	/**************************************************************
					SCREEN REFRESH
	**************************************************************/
	
	public static VhUpdatePtr othunder_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int layer[3];
	
		TC0100SCN_tilemap_update();
	
		palette_init_used_colors();
		othunder_update_palette();
		palette_used_colors[0] |= PALETTE_COLOR_VISIBLE;
		palette_recalc();
	
		layer[0] = TC0100SCN_bottomlayer(0);
		layer[1] = layer[0]^1;
		layer[2] = 2;
	
		fillbitmap(priority_bitmap,0,NULL);
	
		/* Ensure screen blanked even when bottom layer not drawn due to disable bit */
		fillbitmap(bitmap, palette_transparent_pen, &Machine . visible_area);
	
		TC0100SCN_tilemap_draw(bitmap,0,layer[0],TILEMAP_IGNORE_TRANSPARENCY,1);
		TC0100SCN_tilemap_draw(bitmap,0,layer[1],0,2);
		TC0100SCN_tilemap_draw(bitmap,0,layer[2],0,4);
	
		/* Sprites can be under/over the layer below text layer */
		{
			int primasks[2] = {0xf0,0xfc};
			othunder_draw_sprites_16x8(bitmap,primasks,3);
		}
	
		/* See if we should draw artificial gun targets */
	
		if (input_port_9_word_r(0,0) & 0x1)	/* Fake DSW */
		{
			int rawx, rawy, centrex, centrey, screenx, screeny;
	
			/* calculate p1 screen co-ords by matching routine at $A932 */
			rawx = othunder_ram[0x2848/2];
			centrex = othunder_ram[0xa046/2];
			if (rawx <= centrex)
			{
				rawx = centrex - rawx;
				screenx = rawx * othunder_ram[0xa04e/2] + (((rawx * othunder_ram[0xa050/2]) & 0xffff0000) >> 16);
				screenx = 0xa0 - screenx;
				if (screenx < 0) screenx = 0;
			}
			else
			{
				if (rawx > othunder_ram[0xa028/2]) rawx = othunder_ram[0xa028/2];
				rawx -= centrex;
				screenx = rawx * othunder_ram[0xa056/2] + (((rawx * othunder_ram[0xa058/2]) & 0xffff0000) >> 16);
				screenx += 0xa0;
				if (screenx > 0x140) screenx = 0x140;
			}
			rawy = othunder_ram[0x284a/2];
			centrey = othunder_ram[0xa048/2];
			if (rawy <= centrey)
			{
				rawy = centrey - rawy;
				screeny = rawy * othunder_ram[0xa052/2] + (((rawy * othunder_ram[0xa054/2]) & 0xffff0000) >> 16);
				screeny = 0x78 - screeny;
				if (screeny < 0) screeny = 0;
			}
			else
			{
				if (rawy > othunder_ram[0xa030/2]) rawy = othunder_ram[0xa030/2];
				rawy -= centrey;
				screeny = rawy * othunder_ram[0xa05a/2] + (((rawy * othunder_ram[0xa05c/2]) & 0xffff0000) >> 16);
				screeny += 0x78;
				if (screeny > 0xf0) screeny = 0xf0;
			}
	
			// fudge y to show in centre of scope/hit sprite, note that screenx, screeny
			// were confirmed to match those stored by the game at $82732, $82734
			screeny += 2;
	
			/* player 1 */
			draw_crosshair(bitmap,screenx,screeny,&Machine.visible_area);
	
			/* calculate p2 screen co-ords by matching routine at $AA48 */
			rawx = othunder_ram[0x284c/2];
			centrex = othunder_ram[0xa04a/2];
			if (rawx <= centrex)
			{
				rawx = centrex - rawx;
				screenx = rawx * othunder_ram[0xa05e/2] + (((rawx * othunder_ram[0xa060/2]) & 0xffff0000) >> 16);
				screenx = 0xa0 - screenx;
				if (screenx < 0) screenx = 0;
			}
			else
			{
				if (rawx > othunder_ram[0xa038/2]) rawx = othunder_ram[0xa038/2];
				rawx -= centrex;
				screenx = rawx * othunder_ram[0xa066/2] + (((rawx * othunder_ram[0xa068/2]) & 0xffff0000) >> 16);
				screenx += 0xa0;
				if (screenx > 0x140) screenx = 0x140;
			}
			rawy = othunder_ram[0x284e/2];
			centrey = othunder_ram[0xa04c/2];
			if (rawy <= centrey)
			{
				rawy = centrey - rawy;
				screeny = rawy * othunder_ram[0xa062/2] + (((rawy * othunder_ram[0xa064/2]) & 0xffff0000) >> 16);
				screeny = 0x78 - screeny;
				if (screeny < 0) screeny = 0;
			}
			else
			{
				if (rawy > othunder_ram[0xa040/2]) rawy = othunder_ram[0xa040/2];
				rawy -= centrey;
				screeny = rawy * othunder_ram[0xa06a/2] + (((rawy * othunder_ram[0xa06c/2]) & 0xffff0000) >> 16);
				screeny += 0x78;
				if (screeny > 0xf0) screeny = 0xf0;
			}
	
			// fudge y to show in centre of scope/hit sprite, note that screenx, screeny
			// were confirmed to match those stored by the game at $82736, $82738
			screeny += 2;
	
			/* player 2 */
			draw_crosshair(bitmap,screenx,screeny,&Machine.visible_area);
		}
	} };
	
}
