/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class hexion
{
	
	
	static data8_t *vram[2],*unkram;
	static int bankctrl,rambank,gfxrom_select;
	static struct tilemap *tilemap[2];
	
	
	
	public static VhConvertColorPromPtr hexion_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3;
	
	
			/* red component */
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* green component */
			bit0 = (color_prom.read(Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(Machine->drv->total_colors)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* blue component */
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
	
	INLINE void get_tile_info(int tile_index,data8_t *ram)
	{
		tile_index *= 4;
		SET_TILE_INFO(
				0,
				ram[tile_index] + ((ram[tile_index+1] & 0x3f) << 8),
				ram[tile_index+2] & 0x0f,
				0)
	}
	
	public static GetTileInfoPtr get_tile_info0 = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		get_tile_info(tile_index,vram[0]);
	} };
	
	public static GetTileInfoPtr get_tile_info1 = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		get_tile_info(tile_index,vram[1]);
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr hexion_vh_start = new VhStartPtr() { public int handler() 
	{
		tilemap[0] = tilemap_create(get_tile_info0,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,64,32);
		tilemap[1] = tilemap_create(get_tile_info1,tilemap_scan_rows,TILEMAP_OPAQUE,     8,8,64,32);
	
		if (!tilemap[0] || !tilemap[1])
			return 1;
	
		tilemap_set_transparent_pen(tilemap[0],0);
		tilemap_set_scrollx(tilemap[1],0,-4);
		tilemap_set_scrolly(tilemap[1],0,4);
	
		vram[0] = memory_region(REGION_CPU1) + 0x30000;
		vram[1] = vram[0] + 0x2000;
		unkram = vram[1] + 0x2000;
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr hexion_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr rom = memory_region(REGION_CPU1) + 0x10000;
	
		/* bits 0-3 select ROM bank */
		cpu_setbank(1,rom + 0x2000 * (data & 0x0f));
	
		/* does bit 6 trigger the 052591? */
		if ((data & 0x40) != 0)
		{
			int bank = unkram[0]&1;
			memset(vram[bank],unkram[1],0x2000);
			tilemap_mark_all_tiles_dirty(tilemap[bank]);
		}
	
		/* other bits unknown */
	if ((data & 0x30) != 0)
		usrintf_showmessage("bankswitch %02x",data&0xf0);
	
	//logerror("%04x: bankswitch_w %02x\n",cpu_get_pc(),data);
	} };
	
	public static ReadHandlerPtr hexion_bankedram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (gfxrom_select && offset < 0x1000)
		{
			return memory_region(REGION_GFX1)[((gfxrom_select & 0x7f) << 12) + offset];
		}
		else if (bankctrl == 0)
		{
			return vram[rambank][offset];
		}
		else if (bankctrl == 2 && offset < 0x800)
		{
			return unkram[offset];
		}
		else
		{
	//logerror("%04x: bankedram_r offset %04x, bankctrl = %02x\n",cpu_get_pc(),offset,bankctrl);
			return 0;
		}
	} };
	
	public static WriteHandlerPtr hexion_bankedram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (bankctrl == 3 && offset == 0 && (data & 0xfe) == 0)
		{
	//logerror("%04x: bankedram_w offset %04x, data %02x, bankctrl = %02x\n",cpu_get_pc(),offset,data,bankctrl);
			rambank = data & 1;
		}
		else if (bankctrl == 0)
		{
	//logerror("%04x: bankedram_w offset %04x, data %02x, bankctrl = %02x\n",cpu_get_pc(),offset,data,bankctrl);
			if (vram[rambank][offset] != data)
			{
				vram[rambank][offset] = data;
				tilemap_mark_tile_dirty(tilemap[rambank],offset/4);
			}
		}
		else if (bankctrl == 2 && offset < 0x800)
		{
	//logerror("%04x: unkram_w offset %04x, data %02x, bankctrl = %02x\n",cpu_get_pc(),offset,data,bankctrl);
			unkram[offset] = data;
		}
		else
	logerror("%04x: bankedram_w offset %04x, data %02x, bankctrl = %02x\n",cpu_get_pc(),offset,data,bankctrl);
	} };
	
	public static WriteHandlerPtr hexion_bankctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	//logerror("%04x: bankctrl_w %02x\n",cpu_get_pc(),data);
		bankctrl = data;
	} };
	
	public static WriteHandlerPtr hexion_gfxrom_select_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	//logerror("%04x: gfxrom_select_w %02x\n",cpu_get_pc(),data);
		gfxrom_select = data;
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	public static VhUpdatePtr hexion_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update(ALL_TILEMAPS);
	
		tilemap_draw(bitmap,tilemap[1],0,0);
		tilemap_draw(bitmap,tilemap[0],0,0);
	} };
}
