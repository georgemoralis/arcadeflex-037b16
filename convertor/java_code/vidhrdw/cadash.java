/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class cadash
{
	
	#define TC0100SCN_GFX_NUM 1
	
	
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
	
	static UINT16 sprite_ctrl = 0;
	static UINT16 sprites_flipscreen = 0;
	
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
					if (mwa.handler == TC0110PCR_step1_4bpg_word_w)
						return 1;
				}
				mwa++;
			}
		}
	
		return 0;
	}
	
	
	public static VhStartPtr cadash_core_vh_start = new VhStartPtr() { public int handler() 
	{
		spritelist = malloc(0x100 * sizeof(*spritelist));
		if (!spritelist)
			return 1;
	
		if (TC0100SCN_vh_start(1,TC0100SCN_GFX_NUM,taito_hide_pixels,0,0,0,0,0,0))
		{
			cadash_vh_stop();
			return 1;
		}
	
		if (has_TC0110PCR())
			if (TC0110PCR_vh_start())
			{
				cadash_vh_stop();
				return 1;
			}
	
		return 0;
	} };
	
	public static VhStartPtr cadash_vh_start = new VhStartPtr() { public int handler() 
	{
		taito_hide_pixels = 0;
		return (cadash_core_vh_start());
	} };
	
	public static VhStopPtr cadash_vh_stop = new VhStopPtr() { public void handler() 
	{
		free(spritelist);
		spritelist = 0;
	
		TC0100SCN_vh_stop();
	
		if (has_TC0110PCR())
			TC0110PCR_vh_stop();
	} };
	
	
	/********************************************************
	          SPRITE READ AND WRITE HANDLERS
	********************************************************/
	
	WRITE16_HANDLER( cadash_spritectrl_w )
	{
		sprite_ctrl = data;
	}
	
	WRITE16_HANDLER( cadash_spriteflip_w )
	{
		sprites_flipscreen = data;
	}
	
	
	/*********************************************************
					PALETTE
	*********************************************************/
	
	public static ShUpdatePtr cadash_update_palette = new ShUpdatePtr() { public void handler() 
	{
		int i,j,offs;
		UINT8 color, sprite_colbank = (sprite_ctrl & 0x3c) << 2;
		UINT16 data, tilenum, palette_map[256];
		memset (palette_map, 0, sizeof (palette_map));
	
		for (offs = (spriteram_size/2)-4;offs >=0;offs -= 4)
		{
			data = buffered_spriteram16[offs+0];
			color = (data &0x000f) | sprite_colbank;
	
			data = buffered_spriteram16[offs+2];
			tilenum = data &0x1fff;
	
			if (tilenum != 0)
			{
				palette_map[color] |= Machine.gfx[0].pen_usage[tilenum];
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
	} };
	
	
	
	
	/************************************************************
				SPRITE DRAW ROUTINE
	
			(information from Raine)
	
			OBJECT RAM
			----------
	
			- 8 bytes/sprite
			- 256 sprites (0x800 bytes)
			- First sprite has *highest* priority
	
			-----+--------+-------------------------
			Byte | Bit(s) | Use
			-----+76543210+-------------------------
			  0  |.x......| Flip Y Axis
			  0  |x.......| Flip X Axis
			  1  |....xxxx| Colour Bank
			  2  |.......x| Sprite Y
			  3  |xxxxxxxx| Sprite Y
			  4  |...xxxxx| Sprite Tile
			  5  |xxxxxxxx| Sprite Tile
			  6  |.......x| Sprite X
			  7  |xxxxxxxx| Sprite X
			-----+--------+-------------------------
	
			SPRITE CONTROL
			--------------
	
			-----+--------+-------------------------
			Byte | Bit(s) | Use
			-----+76543210+-------------------------
			  0  |.......x| ?
			  0  |......x.| Write Acknowledge?
			  0  |..xxxx..| Colour Bank Offset
			  0  |xx......| Unused
			  1  |...xxxxx| Unused
			  1  |..x.....| BG1:Sprite Priority
			  1  |.x......| Priority?
			  1  |x.......| Unused
			-----+--------+-------------------------
	
	********************************************************/
	
	
	static void cadash_draw_sprites(struct osd_bitmap *bitmap,int *primasks,int y_offs)
	{
		int offs, flipx, flipy;
		int x, y, curx, cury;
		int priority = (sprite_ctrl & 0x2000) >> 13;	/* 1 = sprites under top bg layer */
		UINT8 color, sprite_colbank = (sprite_ctrl & 0x3c) << 2;
		UINT16 data, tilenum, code;
	
		/* pdrawgfx() needs us to draw sprites front to back, so we have to build a list
		   while processing sprite ram and then draw them all at the end */
		struct tempsprite *sprite_ptr = spritelist;
	
		for (offs = (spriteram_size/2)-4;offs >=0;offs -= 4)
		{
			data = buffered_spriteram16[offs+0];
			flipy = (data & 0x8000) >> 15;
			flipx = (data & 0x4000) >> 14;
			color = (data & 0x000f) | sprite_colbank;
	
			data = buffered_spriteram16[offs+1];
			y = data & 0x1ff;   // correct mask?
	
			data = buffered_spriteram16[offs+2];
			tilenum = data & 0x1fff;
	
			data = buffered_spriteram16[offs+3];
			x = data & 0x1ff;   // correct mask?
	
			if (!tilenum) continue;
	
			y += y_offs;
	
			/* treat coords as signed */
			if (x>0x140) x -= 0x200;
			if (y>0x140) y -= 0x200;
	
			code = tilenum;
			curx = x;
			cury = y;
	
			if ((sprites_flipscreen &1) == 0)
			{
				curx = 320 - curx - 16;
				cury = 256 - cury;
				flipx = !flipx;
				flipy = !flipy;
			}
	
			sprite_ptr.code = code;
			sprite_ptr.color = color;
			sprite_ptr.flipx = flipx;
			sprite_ptr.flipy = flipy;
			sprite_ptr.x = curx;
			sprite_ptr.y = cury;
	
			if (primasks != 0)
			{
				sprite_ptr.primask = primasks[priority];
				sprite_ptr++;
			}
			else
			{
				drawgfx(bitmap,Machine.gfx[0],
						sprite_ptr.code,
						sprite_ptr.color,
						sprite_ptr.flipx,sprite_ptr.flipy,
						sprite_ptr.x,sprite_ptr.y,
						&Machine.visible_area,TRANSPARENCY_PEN,0);
			}
	
		}
	
		/* this happens only if primsks != NULL */
		while (sprite_ptr != spritelist)
		{
			sprite_ptr--;
	
			pdrawgfx(bitmap,Machine.gfx[0],
					sprite_ptr.code,
					sprite_ptr.color,
					sprite_ptr.flipx,sprite_ptr.flipy,
					sprite_ptr.x,sprite_ptr.y,
					&Machine.visible_area,TRANSPARENCY_PEN,0,
					sprite_ptr.primask);
		}
	}
	
	
	/**************************************************************
					SCREEN REFRESH
	**************************************************************/
	
	public static VhUpdatePtr cadash_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		UINT8 layer[3];
	
		TC0100SCN_tilemap_update();
	
		palette_init_used_colors();
		cadash_update_palette();
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
			cadash_draw_sprites(bitmap,primasks,8);
		}
	
	#if 0
		{
			char buf[80];
	
			sprintf(buf,"sprite_ctrl: %04x",sprite_ctrl);
			usrintf_showmessage(buf);
		}
	#endif
	} };
	
}
