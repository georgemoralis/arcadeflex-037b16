/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class simpsons
{
	
	static int bg_colorbase,sprite_colorbase,layer_colorbase[3];
	UBytePtr simpsons_xtraram;
	static int layerpri[3];
	
	
	/***************************************************************************
	
	  Callbacks for the K052109
	
	***************************************************************************/
	
	public static K052109_callbackProcPtr tile_callback = new K052109_callbackProcPtr() { public void handler(int layer,int bank,int[] code,int[] color) 
	{
		*code |= ((*color & 0x3f) << 8) | (bank << 14);
		*color = layer_colorbase[layer] + ((*color & 0xc0) >> 6);
	} };
	
	
	/***************************************************************************
	
	  Callbacks for the K053247
	
	***************************************************************************/
	
	static void sprite_callback(int *code,int *color,int *priority_mask)
	{
		int pri = (*color & 0x0f80) >> 6;	/* ??????? */
		if (pri <= layerpri[2])								*priority_mask = 0;
		else if (pri > layerpri[2] && pri <= layerpri[1])	*priority_mask = 0xf0;
		else if (pri > layerpri[1] && pri <= layerpri[0])	*priority_mask = 0xf0|0xcc;
		else 												*priority_mask = 0xf0|0xcc|0xaa;
	
		*color = sprite_colorbase + (*color & 0x001f);
	}
	
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr simpsons_vh_start = new VhStartPtr() { public int handler() 
	{
		if (K052109_vh_start(REGION_GFX1,NORMAL_PLANE_ORDER,tile_callback))
			return 1;
		if (K053247_vh_start(REGION_GFX2,53,23,NORMAL_PLANE_ORDER,sprite_callback))
		{
			K052109_vh_stop();
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopPtr simpsons_vh_stop = new VhStopPtr() { public void handler() 
	{
		K052109_vh_stop();
		K053247_vh_stop();
	} };
	
	/***************************************************************************
	
	  Extra video banking
	
	***************************************************************************/
	
	public static ReadHandlerPtr simpsons_K052109_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K052109_r(offset + 0x2000);
	} };
	
	public static WriteHandlerPtr simpsons_K052109_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		K052109_w(offset + 0x2000,data);
	} };
	
	public static ReadHandlerPtr simpsons_K053247_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (offset < 0x1000) return K053247_r(offset);
		else return simpsons_xtraram[offset - 0x1000];
	} };
	
	public static WriteHandlerPtr simpsons_K053247_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset < 0x1000) K053247_w(offset,data);
		else simpsons_xtraram[offset - 0x1000] = data;
	} };
	
	void simpsons_video_banking(int bank)
	{
		if ((bank & 1) != 0)
		{
			memory_set_bankhandler_r(3,0,paletteram_r);
			memory_set_bankhandler_w(3,0,paletteram_xBBBBBGGGGGRRRRR_swap_w);
		}
		else
		{
			memory_set_bankhandler_r(3,0,K052109_r);
			memory_set_bankhandler_w(3,0,K052109_w);
		}
	
		if ((bank & 2) != 0)
		{
			memory_set_bankhandler_r(4,0,simpsons_K053247_r);
			memory_set_bankhandler_w(4,0,simpsons_K053247_w);
		}
		else
		{
			memory_set_bankhandler_r(4,0,simpsons_K052109_r);
			memory_set_bankhandler_w(4,0,simpsons_K052109_w);
		}
	}
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	/* useful function to sort the three tile layers by priority order */
	static void sortlayers(int *layer,int *pri)
	{
	#define SWAP(a,b) \
		if (pri[a] < pri[b]) \
		{ \
			int t; \
			t = pri[a]; pri[a] = pri[b]; pri[b] = t; \
			t = layer[a]; layer[a] = layer[b]; layer[b] = t; \
		}
	
		SWAP(0,1)
		SWAP(0,2)
		SWAP(1,2)
	}
	
	public static VhUpdatePtr simpsons_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int layer[3];
	
	
		bg_colorbase       = K053251_get_palette_index(K053251_CI0);
		sprite_colorbase   = K053251_get_palette_index(K053251_CI1);
		layer_colorbase[0] = K053251_get_palette_index(K053251_CI2);
		layer_colorbase[1] = K053251_get_palette_index(K053251_CI3);
		layer_colorbase[2] = K053251_get_palette_index(K053251_CI4);
	
		K052109_tilemap_update();
	
		palette_init_used_colors();
		K053247_mark_sprites_colors();
		if (palette_used_colors != 0)
			palette_used_colors[16 * bg_colorbase] |= PALETTE_COLOR_VISIBLE;
		palette_recalc();
	
		layer[0] = 0;
		layerpri[0] = K053251_get_priority(K053251_CI2);
		layer[1] = 1;
		layerpri[1] = K053251_get_priority(K053251_CI3);
		layer[2] = 2;
		layerpri[2] = K053251_get_priority(K053251_CI4);
	
		sortlayers(layer,layerpri);
	
		fillbitmap(priority_bitmap,0,NULL);
		fillbitmap(bitmap,Machine.pens[16 * bg_colorbase],&Machine.visible_area);
		K052109_tilemap_draw(bitmap,layer[0],0,1);
		K052109_tilemap_draw(bitmap,layer[1],0,2);
		K052109_tilemap_draw(bitmap,layer[2],0,4);
	
		K053247_sprites_draw(bitmap);
	} };
}
