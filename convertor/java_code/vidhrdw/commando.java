/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class commando
{
	
	
	
	UBytePtr commando_fgvideoram,*commando_bgvideoram;
	
	static struct tilemap *fg_tilemap, *bg_tilemap;
	
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Commando has three 256x4 palette PROMs (one per gun), connected to the
	  RGB output this way:
	
	  bit 3 -- 220 ohm resistor  -- RED/GREEN/BLUE
	        -- 470 ohm resistor  -- RED/GREEN/BLUE
	        -- 1  kohm resistor  -- RED/GREEN/BLUE
	  bit 0 -- 2.2kohm resistor  -- RED/GREEN/BLUE
	
	***************************************************************************/
	
	public static VhConvertColorPromPtr commando_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3;
	
	
			bit0 = (color_prom.read(i)>> 0) & 0x01;
			bit1 = (color_prom.read(i)>> 1) & 0x01;
			bit2 = (color_prom.read(i)>> 2) & 0x01;
			bit3 = (color_prom.read(i)>> 3) & 0x01;
			palette[3*i] = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(i+Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(i+Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(i+Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(i+Machine->drv->total_colors)>> 3) & 0x01;
			palette[3*i + 1] = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(i+2*Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(i+2*Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(i+2*Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(i+2*Machine->drv->total_colors)>> 3) & 0x01;
			palette[3*i + 2] = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
		}
	} };
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoPtr get_fg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int code, color;
	
		code = commando_fgvideoram[tile_index];
		color = commando_fgvideoram[tile_index + 0x400];
		SET_TILE_INFO(
				0,
				code + ((color & 0xc0) << 2),
				color & 0x0f,
				TILE_FLIPYX((color & 0x30) >> 4))
	} };
	
	public static GetTileInfoPtr get_bg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int code, color;
	
		code = commando_bgvideoram[tile_index];
		color = commando_bgvideoram[tile_index + 0x400];
		SET_TILE_INFO(
				1,
				code + ((color & 0xc0) << 2),
				color & 0x0f,
				TILE_FLIPYX((color & 0x30) >> 4))
	} };
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr commando_vh_start = new VhStartPtr() { public int handler() 
	{
		fg_tilemap = tilemap_create(get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT, 8, 8,32,32);
		bg_tilemap = tilemap_create(get_bg_tile_info,tilemap_scan_cols,TILEMAP_OPAQUE,     16,16,32,32);
	
		if (!fg_tilemap || !bg_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap,3);
	
		return 0;
	} };
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr commando_fgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		commando_fgvideoram[offset] = data;
		tilemap_mark_tile_dirty(fg_tilemap,offset & 0x3ff);
	} };
	
	public static WriteHandlerPtr commando_bgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		commando_bgvideoram[offset] = data;
		tilemap_mark_tile_dirty(bg_tilemap,offset & 0x3ff);
	} };
	
	
	public static WriteHandlerPtr commando_scrollx_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static unsigned char scroll[2];
	
		scroll[offset] = data;
		tilemap_set_scrollx(bg_tilemap,0,scroll[0] | (scroll[1] << 8));
	} };
	
	public static WriteHandlerPtr commando_scrolly_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static unsigned char scroll[2];
	
		scroll[offset] = data;
		tilemap_set_scrolly(bg_tilemap,0,scroll[0] | (scroll[1] << 8));
	} };
	
	
	public static WriteHandlerPtr commando_c804_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* bits 0 and 1 are coin counters */
		coin_counter_w.handler(0, data & 0x01);
		coin_counter_w.handler(1, data & 0x02);
	
		/* bit 4 resets the sound CPU */
		cpu_set_reset_line(1,(data & 0x10) ? ASSERT_LINE : CLEAR_LINE);
	
		/* bit 7 flips screen */
		flip_screen_set(~data & 0x80);
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites(struct osd_bitmap *bitmap)
	{
		int offs;
	
		for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
		{
			int sx,sy,flipx,flipy,bank,attr;
	
	
			/* bit 1 of attr is not used */
			attr = buffered_spriteram.read(offs + 1);
			sx = buffered_spriteram.read(offs + 3)- ((attr & 0x01) << 8);
			sy = buffered_spriteram.read(offs + 2);
			flipx = attr & 0x04;
			flipy = attr & 0x08;
			bank = (attr & 0xc0) >> 6;
	
			if (flip_screen != 0)
			{
				sx = 240 - sx;
				sy = 240 - sy;
				flipx = !flipx;
				flipy = !flipy;
			}
	
			if (bank < 3)
				drawgfx(bitmap,Machine.gfx[2],
						buffered_spriteram.read(offs)+ 256 * bank,
						(attr & 0x30) >> 4,
						flipx,flipy,
						sx,sy,
						&Machine.visible_area,TRANSPARENCY_PEN,15);
		}
	}
	
	public static VhUpdatePtr commando_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update(ALL_TILEMAPS);
	
		tilemap_draw(bitmap,bg_tilemap,0,0);
		draw_sprites(bitmap);
		tilemap_draw(bitmap,fg_tilemap,0,0);
	} };
	
	public static VhEofCallbackPtr commando_eof_callback = new VhEofCallbackPtr() { public void handler() 
	{
		buffer_spriteram_w(0,0);
	} };
}
