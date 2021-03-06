/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class topspeed
{
	
	data16_t *topspeed_spritemap;
	
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
	
	/***************************************************************************/
	
	public static VhStartPtr topspeed_vh_start = new VhStartPtr() { public int handler() 
	{
		/* Up to $1000/8 big sprites, requires 0x200 * sizeof(*spritelist)
		   Multiply this by 128 to give room for the number of small sprites,
		   which are what actually get put in the structure. */
	
		spritelist = malloc(0x10000 * sizeof(*spritelist));
		if (!spritelist)
			return 1;
	
		/* (chips, gfxnum, x_offs, y_offs, y_invert, opaque, dblwidth) */
		if (PC080SN_vh_start(2,1,0,8,0,0,0))
		{
			free(spritelist);
			spritelist = 0;
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopPtr topspeed_vh_stop = new VhStopPtr() { public void handler() 
	{
		free(spritelist);
		spritelist = 0;
	
		PC080SN_vh_stop();
	} };
	
	
	void topspeed_update_palette(void)
	{
		int offs,map_offset,sprite_chunk,i,j;
		data16_t *spritemap = topspeed_spritemap;
		UINT16 tile_mask = (Machine.gfx[0].total_elements) - 1;
		UINT16 data,tilenum,code,color;
		UINT16 palette_map[256];
		memset (palette_map, 0, sizeof (palette_map));
	
		for (offs = (spriteram_size/2)-4;offs >=0;offs -= 4)
		{
			data = spriteram16[offs+3];
			color = (data &0xff00) >> 8;
			tilenum = data &0xff;
	
			if (tilenum != 0)
			{
				map_offset = tilenum << 7;
	
				for (sprite_chunk=0;sprite_chunk<128;sprite_chunk++)
				{
					i = sprite_chunk % 8;   /* 8 sprite chunks across */
					j = sprite_chunk / 8;   /* 16 sprite chunks down */
	
					code = spritemap[map_offset + (j<<3) + i ] &tile_mask;
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
	
	
	void topspeed_draw_sprites(struct osd_bitmap *bitmap,int *primasks,int y_offs)
	{
		int offs,map_offset,x,y,curx,cury,sprite_chunk;
		data16_t *spritemap = topspeed_spritemap;
		UINT16 data,tilenum,code,color;
		UINT8 sprites_flipscreen = 0;
		UINT8 flipx,flipy,priority,bad_chunks;
		UINT8 j,k,px,py,zx,zy,zoomx,zoomy;
		struct tempsprite *sprite_ptr = spritelist;
	
		for (offs = (spriteram_size/2)-4;offs >=0;offs -= 4)
		{
			data = spriteram16[offs+0];
			zoomy = (data & 0xfe00) >> 9;
			y = data & 0x1ff;
	
			data = spriteram16[offs+1];
			flipy = (data & 0x8000) >> 15;
			zoomx = (data & 0x7f);
	
			data = spriteram16[offs+2];
			priority = (data & 0x8000) >> 15;
			flipx = (data & 0x4000) >> 14;
	//		unknown = (data & 0x2000) >> 13;
			x = data & 0x1ff;
	
			data = spriteram16[offs+3];
			color = (data & 0xff00) >> 8;
			tilenum = data & 0xff;
	
			if (!tilenum) continue;
	
			map_offset = tilenum << 7;
	
			zoomx += 1;
			zoomy += 1;
	
			y += y_offs;
			y += (128-zoomy);
	
			/* treat coords as signed */
			if (x>0x140) x -= 0x200;
			if (y>0x140) y -= 0x200;
	
			bad_chunks = 0;
	
			for (sprite_chunk=0;sprite_chunk<128;sprite_chunk++)
			{
				k = sprite_chunk % 8;   /* 8 sprite chunks per row */
				j = sprite_chunk / 8;   /* 16 rows */
	
				px = k;
				py = j;
				if (flipx != 0)  px = 7-k;	/* pick tiles back to front for x and y flips */
				if (flipy != 0)  py = 15-j;
	
				code = spritemap[map_offset + (py<<3) + px];
	
				if (code>0x7fff)
				{
					bad_chunks += 1;
					continue;
				}
	
				curx = x + ((k*zoomx)/8);
				cury = y + ((j*zoomy)/16);
	
				zx= x + (((k+1)*zoomx)/8) - curx;
				zy= y + (((j+1)*zoomy)/16) - cury;
	
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
	
	/***************************************************************************/
	
	public static VhUpdatePtr topspeed_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		UINT8 layer[4];
	
	#ifdef MAME_DEBUG
		static UINT8 dislayer[5];
		char buf[80];
	#endif
	
	#ifdef MAME_DEBUG
		if (keyboard_pressed_memory (KEYCODE_V))
		{
			dislayer[0] ^= 1;
			sprintf(buf,"bg: %01x",dislayer[0]);
			usrintf_showmessage(buf);
		}
	
		if (keyboard_pressed_memory (KEYCODE_B))
		{
			dislayer[1] ^= 1;
			sprintf(buf,"fg: %01x",dislayer[1]);
			usrintf_showmessage(buf);
		}
	
		if (keyboard_pressed_memory (KEYCODE_N))
		{
			dislayer[2] ^= 1;
			sprintf(buf,"bg2: %01x",dislayer[2]);
			usrintf_showmessage(buf);
		}
	
		if (keyboard_pressed_memory (KEYCODE_M))
		{
			dislayer[3] ^= 1;
			sprintf(buf,"fg2: %01x",dislayer[3]);
			usrintf_showmessage(buf);
		}
	
		if (keyboard_pressed_memory (KEYCODE_C))
		{
			dislayer[4] ^= 1;
			sprintf(buf,"sprites: %01x",dislayer[4]);
			usrintf_showmessage(buf);
		}
	#endif
	
		PC080SN_tilemap_update();
	
		palette_init_used_colors();
		topspeed_update_palette();
		palette_used_colors[0] |= PALETTE_COLOR_VISIBLE;
		palette_recalc();
	
		/* Tilemap layer priority seems hardwired (the order is odd, too) */
		layer[0] = 1;
		layer[1] = 0;
		layer[2] = 1;
		layer[3] = 0;
	
		fillbitmap(priority_bitmap,0,NULL);
		fillbitmap(bitmap, palette_transparent_pen, &Machine . visible_area);
	
	#ifdef MAME_DEBUG
		if (dislayer[3]==0)
	#endif
		PC080SN_tilemap_draw(bitmap,1,layer[0],TILEMAP_IGNORE_TRANSPARENCY,1);
	
	#ifdef MAME_DEBUG
		if (dislayer[2]==0)
	#endif
		PC080SN_tilemap_draw(bitmap,1,layer[1],0,2);
	
	#ifdef MAME_DEBUG
		if (dislayer[1]==0)
	#endif
	 	PC080SN_tilemap_draw(bitmap,0,layer[2],0,4);
	
	#ifdef MAME_DEBUG
		if (dislayer[0]==0)
	#endif
		PC080SN_tilemap_draw(bitmap,0,layer[3],0,8);
	
	#ifdef MAME_DEBUG
		if (dislayer[4]==0)
	#endif
		/* Sprites are either over bottom layer or under top layer */
		/* sprite/sprite priority is from position in list, sprite/
		   tile from a control bit, hence we must use pdrawgfx */
		{
			int primasks[2] = {0xff00,0xfffc};
			topspeed_draw_sprites(bitmap,primasks,3);
		}
	} };
	
	
}
