/***************************************************************************

  Video Hardware for Championship V'ball by Paul Hampson
  Generally copied from China Gate by Paul Hampson
  "Mainly copied from Vidhrdw of Double Dragon (bootleg) & Double Dragon II"

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class vball
{
	
	int vb_scrollx_hi=0;
	UBytePtr vb_scrollx_lo;
	UBytePtr vb_videoram;
	//UBytePtr spriteram;
	UBytePtr vb_attribram;
	UBytePtr vb_fgattribram;
	int vball_gfxset;
	int vb_bgprombank=0xff;
	int vb_spprombank=0xff;
	
	public static VhStartPtr vb_vh_start = new VhStartPtr() { public int handler() 
	{
		dirtybuffer = malloc( 0x800 );
		if (dirtybuffer != 0)
		{
			memset(dirtybuffer,1, 0x800);
	
			tmpbitmap = bitmap_alloc(Machine.drv.screen_width*2,Machine.drv.screen_height*2);
	
			if (tmpbitmap != 0) return 0;
	
			free( dirtybuffer );
		}
	
		return 1;
	} };
	
	
	
	public static VhStopPtr vb_vh_stop = new VhStopPtr() { public void handler() 
	{
		bitmap_free( tmpbitmap );
		free( dirtybuffer );
	} };
	
	void vb_bgprombank_w( int bank )
	{
		int i;
	
		UBytePtr  color_prom;
	
		if (bank==vb_bgprombank) return;
	
		color_prom = memory_region(REGION_PROMS) + bank*0x80;
	
		logerror("BGPROM Bank:%x, bank offset:%x\n",bank, bank*0x80);
	
		for (i=0;i<128;i++, color_prom++)
		{
			palette_change_color(i,(color_prom.read(0)& 0x0f) << 4,(color_prom.read(0)& 0xf0) >> 0,
					       (color_prom.read(0x800)& 0x0f) << 4);
	//		logerror("\t%d: r:%d g:%d b:%d\n",i,(color_prom.read(0)& 0x0f) << 4,(color_prom.read(0)& 0xf0) >> 0,
	//				       (color_prom.read(0x800)& 0x0f) << 4);
		}
	
		vb_bgprombank=bank;
	
	}
	
	void vb_spprombank_w( int bank )
	{
	
		int i;
	
		UBytePtr  color_prom;
	
		if (bank==vb_spprombank) return;
	
		color_prom = memory_region(REGION_PROMS)+0x400 + bank*0x80;
	
		logerror("SPPROM Bank:%x, bank offset:%x\n",bank, 0x400 + bank*0x80);
	
		for (i=128;i<256;i++,color_prom++)
		{
			palette_change_color(i,(color_prom.read(0)& 0x0f) << 4,(color_prom.read(0)& 0xf0) >> 0,
					       (color_prom.read(0x800)& 0x0f) << 4);
		}
	
		vb_spprombank=bank;
	
	}
	
	public static ReadHandlerPtr vb_foreground_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return vb_videoram[offset];
	} };
	
	
	public static WriteHandlerPtr vb_foreground_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if( vb_videoram[offset] != data ){
			vb_videoram[offset] = data;
			dirtybuffer[offset] = 1;
		}
	} };
	
	
	public static ReadHandlerPtr vb_fgattrib_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return vb_fgattribram[offset];
	} };
	
	
	public static WriteHandlerPtr vb_fgattrib_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if( vb_fgattribram[offset] != data ){
			vb_fgattribram[offset] = data;
			dirtybuffer[offset] = 1;
		}
	} };
	
	
	public static ReadHandlerPtr vb_attrib_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return vb_attribram[offset];
	} };
	
	
	public static WriteHandlerPtr vb_attrib_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if( vb_attribram[offset] != data ){
			vb_attribram[offset] = data;
			dirtybuffer[offset] = 1;
		}
	} };
	
	static void vb_draw_foreground( struct osd_bitmap *bitmap )
	{
		const struct GfxElement *gfx = Machine.gfx[0];
		UBytePtr source = vb_videoram;
		UBytePtr attrib_source = vb_fgattribram;
	
		int sx,sy;
	
		for( sy=0; sy<256; sy+=8 ){
			for( sx=0; sx<256; sx+=8 ){
				int attributes = attrib_source[0];
				int tile_number = source[0] + 256*( attributes & 0x1f );
				int color = ( attributes >> 5 ) & 0x7;
				if (tile_number != 0)
					drawgfx( bitmap,gfx, tile_number + (vball_gfxset?0:8192),
					color,
					0,0, /* no flip */
					sx,sy,
					0, /* no need to clip */
					TRANSPARENCY_PEN,0);
	
				source += 1;
				attrib_source +=1;
			}
		}
	}
	
	#define DRAW_SPRITE( order, sx, sy ) drawgfx( bitmap, gfx, \
						(which+order),color,flipx,flipy,sx,sy, \
						clip,TRANSPARENCY_PEN,0);
	
	static void draw_sprites( struct osd_bitmap *bitmap )
	{
		const struct rectangle *clip = &Machine.visible_area;
		const struct GfxElement *gfx = Machine.gfx[1];
		UBytePtr src;
		int i;
	
		src = spriteram;
	
	/*	240-Y    S|X|CLR|WCH WHICH    240-X
		xxxxxxxx x|x|xxx|xxx xxxxxxxx xxxxxxxx
	*/
	
	
		for (i = 0;i < spriteram_size;i += 4)
		{
			int attr = src[i+1];
			int which = src[i+2]+((attr & 0x07)<<8);
			int sx = ((src[i+3] + 8) & 0xff) - 8;
			int sy = 240 - src[i];
			int size = (attr & 0x80) >> 7;
			int color = (attr & 0x38) >> 3;
			int flipx = ~attr & 0x40;
			int flipy = 0;
			int dy = -16;
	
			switch (size)
			{
				case 0: /* normal */
				DRAW_SPRITE(0,sx,sy);
				break;
	
				case 1: /* double y */
				DRAW_SPRITE(0,sx,sy + dy);
				DRAW_SPRITE(1,sx,sy);
				break;
			}
		}
	}
	
	#undef DRAW_SPRITE
	
	static void vb_draw_background( struct osd_bitmap *bitmap )
	{
		const struct GfxElement *gfx = Machine.gfx[0];
		UBytePtr source = videoram;
		UBytePtr attrib_source = vb_attribram;
	
		int scrollx = vb_scrollx_hi - vb_scrollx_lo[0] -4;
		int i,sx,sy;
	
		for( i=0; i < 1; i++){
			for( sy=0; sy<256; sy+=8 ){
				for( sx=0; sx<256; sx+=8 ){
					if ( dirtybuffer[source - videoram] ) {
						int attributes = attrib_source[0];
						int tile_number = source[0] + 256*( attributes & 0x1f );
						int color = ( attributes >> 5 ) & 0x7;
	
						drawgfx( tmpbitmap,gfx, tile_number + (vball_gfxset?8192:0),
						color,
						0,0, /* no flip */
						sx,sy,
						0, /* no need to clip */
						TRANSPARENCY_NONE,0);
	
						dirtybuffer[source - videoram] = 0;
	
					}
	
					if ( dirtybuffer[source + 0x400 - videoram] ) {
						int attributes = attrib_source[0x400];
						int tile_number = source[0x400] + 256*( attributes & 0x1f );
						int color = ( attributes >> 5 ) & 0x7;
	
						drawgfx( tmpbitmap,gfx, tile_number + (vball_gfxset?8192:0),
						color,
						0,0, /* no flip */
						sx+256,sy,
						0, /* no need to clip */
						TRANSPARENCY_NONE,0);
	
						dirtybuffer[source + 0x400 - videoram] = 0;
	
					}
	
	
					source += 1;
					attrib_source +=1;
				}
			}
		}
	
		copyscrollbitmap(bitmap,tmpbitmap,
				1,&scrollx,0,0,
				&Machine.visible_area,
				TRANSPARENCY_NONE,0);
	
	}
	
	
	public static VhUpdatePtr vb_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
	//	Tripping the sprite funk-tastic. :-) PaulH
	/*	static int i=0;
	
		i++;
		i%=60;
	
		vb_spprombank_w(i/15);
	*/
		if (palette_recalc())
			memset(dirtybuffer,1, 0x800);
	
		vb_draw_background( bitmap );
		draw_sprites( bitmap );
	//	vb_draw_foreground( bitmap ); /* So far just hides half the game screen... */
	} };
	
}
