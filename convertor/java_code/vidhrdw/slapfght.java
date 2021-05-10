/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of early Toaplan hardware.

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class slapfght
{
	
	UBytePtr slapfight_videoram;
	UBytePtr slapfight_colorram;
	size_t slapfight_videoram_size;
	UBytePtr slapfight_scrollx_lo,*slapfight_scrollx_hi,*slapfight_scrolly;
	static int flipscreen;
	
	static struct tilemap *pf1_tilemap,*fix_tilemap;
	
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Slapfight has three 256x4 palette PROMs (one per gun) all colours for all
	  outputs are mapped to the palette directly.
	
	  The palette PROMs are connected to the RGB output this way:
	
	  bit 3 -- 220 ohm resistor  -- RED/GREEN/BLUE
	        -- 470 ohm resistor  -- RED/GREEN/BLUE
	        -- 1  kohm resistor  -- RED/GREEN/BLUE
	  bit 0 -- 2.2kohm resistor  -- RED/GREEN/BLUE
	
	*/
	
	public static VhConvertColorPromPtr slapfight_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3;
	
	
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(Machine->drv->total_colors)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(2*Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(2*Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(2*Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(2*Machine->drv->total_colors)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			color_prom++;
		}
	} };
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoPtr get_pf_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 	/* For Performan only */
	{
		int tile,color;
	
		tile=videoram.read(tile_index)+ ((colorram.read(tile_index)& 0x03) << 8);
		color=(colorram.read(tile_index)>> 3) & 0x0f;
		SET_TILE_INFO(
				0,
				tile,
				color,
				0)
	} };
	
	public static GetTileInfoPtr get_pf1_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int tile,color;
	
		tile=videoram.read(tile_index)+ ((colorram.read(tile_index)& 0x0f) << 8);
		color=(colorram.read(tile_index)& 0xf0) >> 4;
	
		SET_TILE_INFO(
				1,
				tile,
				color,
				0)
	} };
	
	public static GetTileInfoPtr get_fix_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int tile,color;
	
		tile=slapfight_videoram[tile_index] + ((slapfight_colorram[tile_index] & 0x03) << 8);
		color=(slapfight_colorram[tile_index] & 0xfc) >> 2;
	
		SET_TILE_INFO(
				0,
				tile,
				color,
				0)
	} };
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr perfrman_vh_start = new VhStartPtr() { public int handler() 
	{
		pf1_tilemap = tilemap_create(get_pf_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,64,32);
	
		if (!pf1_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(pf1_tilemap,0);
	
		return 0;
	} };
	
	public static VhStartPtr slapfight_vh_start = new VhStartPtr() { public int handler() 
	{
		pf1_tilemap = tilemap_create(get_pf1_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,64,32);
		fix_tilemap = tilemap_create(get_fix_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,64,32);
	
		if (!pf1_tilemap || !fix_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(fix_tilemap,0);
	
		return 0;
	} };
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr slapfight_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		videoram.write(offset,data);
	tilemap_mark_tile_dirty(pf1_tilemap,offset);
	} };
	
	public static WriteHandlerPtr slapfight_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		colorram.write(offset,data);
	tilemap_mark_tile_dirty(pf1_tilemap,offset);
	} };
	
	public static WriteHandlerPtr slapfight_fixram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		slapfight_videoram[offset]=data;
		tilemap_mark_tile_dirty(fix_tilemap,offset);
	} };
	
	public static WriteHandlerPtr slapfight_fixcol_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		slapfight_colorram[offset]=data;
		tilemap_mark_tile_dirty(fix_tilemap,offset);
	} };
	
	public static WriteHandlerPtr slapfight_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		logerror("Writing %02x to flipscreen\n",offset);
		if (offset==0) flipscreen=1; /* Port 0x2 is flipscreen */
		else flipscreen=0; /* Port 0x3 is normal */
	} };
	
	#ifdef MAME_DEBUG
	void slapfght_log_vram(void)
	{
		if ( keyboard_pressed_memory(KEYCODE_B) )
		{
			int i;
			for (i=0; i<0x800; i++)
			{
				logerror("Offset:%03x   TileRAM:%02x   AttribRAM:%02x   SpriteRAM:%02x\n",i, videoram.read(i),colorram.read(i),spriteram.read(i));
			}
		}
	}
	#endif
	
	/***************************************************************************
	
	  Render the Sprites
	
	***************************************************************************/
	static void perfrman_draw_sprites( struct osd_bitmap *bitmap, int priority_to_display )
	{
		int offs;
	
		for (offs = 0;offs < spriteram_size;offs += 4)
		{
			int sx, sy;
	
			if ((buffered_spriteram.read(offs+2)& 0x80) == priority_to_display)
			{
				if (flipscreen != 0)
				{
					sx = 265 - buffered_spriteram.read(offs+1);
					sy = 239 - buffered_spriteram.read(offs+3);
					sy &= 0xff;
				}
				else
				{
					sx = buffered_spriteram.read(offs+1)+ 3;
					sy = buffered_spriteram.read(offs+3)- 1;
				}
				drawgfx(bitmap,Machine.gfx[1],
					buffered_spriteram.read(offs),
					((buffered_spriteram.read(offs+2)>> 1) & 3)
						+ ((buffered_spriteram.read(offs+2)<< 2) & 4)
	//					+ ((buffered_spriteram.read(offs+2)>> 2) & 8)
					,
					flipscreen, flipscreen,
					sx, sy,
					&Machine.visible_area,TRANSPARENCY_PEN,0);
			}
		}
	}
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	
	public static VhUpdatePtr perfrman_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_set_flip( pf1_tilemap, flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
		tilemap_set_scrolly( pf1_tilemap ,0 , 0 );
		if (flipscreen != 0) {
			tilemap_set_scrollx( pf1_tilemap ,0 , 264 );
		}
		else {
			tilemap_set_scrollx( pf1_tilemap ,0 , -16 );
		}
	
		tilemap_update(pf1_tilemap);
		fillbitmap(bitmap,Machine.pens[0],&Machine.visible_area);
	
		perfrman_draw_sprites(bitmap,0);
		tilemap_draw(bitmap,pf1_tilemap,0,0);
		perfrman_draw_sprites(bitmap,0x80);
	
	#ifdef MAME_DEBUG
		slapfght_log_vram();
	#endif
	} };
	
	
	public static VhUpdatePtr slapfight_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
		tilemap_set_flip(ALL_TILEMAPS,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
		if (flipscreen != 0) {
			tilemap_set_scrollx( fix_tilemap,0,296);
			tilemap_set_scrollx( pf1_tilemap,0,(*slapfight_scrollx_lo + 256 * *slapfight_scrollx_hi)+296 );
			tilemap_set_scrolly( pf1_tilemap,0, (*slapfight_scrolly)+15 );
			tilemap_set_scrolly( fix_tilemap,0, -1 ); /* Glitch in Tiger Heli otherwise */
		}
		else {
			tilemap_set_scrollx( fix_tilemap,0,0);
			tilemap_set_scrollx( pf1_tilemap,0,(*slapfight_scrollx_lo + 256 * *slapfight_scrollx_hi) );
			tilemap_set_scrolly( pf1_tilemap,0, (*slapfight_scrolly)-1 );
			tilemap_set_scrolly( fix_tilemap,0, -1 ); /* Glitch in Tiger Heli otherwise */
		}
	
		tilemap_update(ALL_TILEMAPS);
		tilemap_draw(bitmap,pf1_tilemap,0,0);
	
		/* Draw the sprites */
		for (offs = 0;offs < spriteram_size[0];offs += 4)
		{
			if (flipscreen != 0)
				drawgfx(bitmap,Machine.gfx[2],
					buffered_spriteram.read(offs)+ ((buffered_spriteram.read(offs+2)& 0xc0) << 2),
					(buffered_spriteram.read(offs+2)& 0x1e) >> 1,
					1,1,
					288-(buffered_spriteram.read(offs+1)+ ((buffered_spriteram.read(offs+2)& 0x01) << 8)) +18,240-buffered_spriteram.read(offs+3),
					&Machine.visible_area,TRANSPARENCY_PEN,0);
			else
				drawgfx(bitmap,Machine.gfx[2],
					buffered_spriteram.read(offs)+ ((buffered_spriteram.read(offs+2)& 0xc0) << 2),
					(buffered_spriteram.read(offs+2)& 0x1e) >> 1,
					0,0,
					(buffered_spriteram.read(offs+1)+ ((buffered_spriteram.read(offs+2)& 0x01) << 8)) - 13,buffered_spriteram.read(offs+3),
					&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	
		tilemap_draw(bitmap,fix_tilemap,0,0);
	
	#ifdef MAME_DEBUG
		slapfght_log_vram();
	#endif
	} };
}
