
/*
 *    Yamaha YGV608 - PVDC2 Pattern mode Video Display Controller 2
 *    - Mark McDougall
 *
 *    Notes:
 *    ======
 *
 *    This implementation is far from complete.
 *    There's enough here to emulate Namco ND-1 games.
 *    Some functionality is missing, some is incomplete.
 *    Also missing for ND-1 is rotation and scaling (cosmetic only).
 *
 *    It could also do with some optimisation for speed!
 *
 *    (Still lots of debugging info/options in here!)
 *
 *    T.B.D. (not critical to ND-1)
 *    ======
 *
 *    Rotation
 *    Scaling
 *    Split-screen scrolling by row (by column supported) (see test mode)
 *    Everything else! :)
 */

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class ygv608
{
	
	#define _ENABLE_SPRITES
	#define _ENABLE_SCROLLX
	#define _ENABLE_SCROLLY
	#define _ENABLE_SCREEN_RESIZE
	#define _nENABLE_ROTATE_ZOOM
	#define _nSHOW_VIDEO_DEBUG
	
	static YGV608 ygv608;
	
	static struct tilemap *tilemap_A = NULL;
	static struct tilemap *tilemap_B = NULL;
	static struct osd_bitmap *work_bitmap = NULL;
	
	#ifdef MAME_DEBUG
	static static void dump_block( char *name, UBytePtr block, int len );
	#endif
	
	/* interrupt generated every 1ms second */
	public static InterruptPtr ygv608_timed_interrupt = new InterruptPtr() { public int handler() 
	{
	/*
	    this is not quite generic, because we trigger a 68k interrupt
	    - if this chip is ever used by another driver, we should make
	      this more generic - or move it into the machine driver
	*/
	
	    static int timer = 0;
	
	    if( ++timer == 1000 )
	        timer = 0;
	
	    /* once every 60Hz, set the vertical border interval start flag */
	    if( ( timer % (1000/60) ) == 0 ) {
	      ygv608.ports.s.fv = 1;
	      if( ygv608.regs.s.iev )
	        return( m68_level2_irq() );
	    }
	
	    /* once every 60Hz, set the position detection flag (somewhere) */
	    if( ( timer % (1000/60) ) == 7 ) {
	      ygv608.ports.s.fp = 1;
	      if( ygv608.regs.s.iep )
	        return( m68_level2_irq() );
	    }
	
	    return( 0 );
	} };
	
	
	static UINT32 get_tile_offset( UINT32 col, UINT32 row,
	                               UINT32 numcols, UINT32 numrows )
	{
	    // this optimisation is not much good to us,
	    // since we really need row,col in the get_tile_info() routines
	    // - so just pack them into a UINT32
	
	    return( ( col << 6 ) | row );
	}
	
	static void get_tile_info_A_8( int offset )
	{
	  // extract row,col packed into offset
	  int             col = offset >> 6;
	  int             row = offset & 0x3f;
	
	  unsigned char   attr = 0;
	  int             pattern_name_base = 0;
	  int             set = ( ygv608.regs.s.md == MD_1PLANE_256COLOUR
				        ? GFX_8X8_8BIT : GFX_8X8_4BIT );
	  int             base = row >> ygv608.base_y_shift;
	
	  if( col >= ygv608.page_x ) {
	    SET_TILE_INFO( set, 0, 0, 0 );
	  }
	  else if( row >= ygv608.page_y ) {
	    SET_TILE_INFO( set, 0, 0, 0 );
	  }
	  else {
	    int sx, sy, page;
	    int i = pattern_name_base +
	      ( ( ( row << ygv608.pny_shift ) + col ) << ygv608.bits16 );
	    int j = ygv608.pattern_name_table[i];
	    if( ygv608.bits16 ) {
	      j += ((int)(ygv608.pattern_name_table[i+1] & ygv608.na8_mask )) << 8;
	      // attribute only valid in 16 color mode
	      if( set == GFX_8X8_4BIT )
	        attr = ygv608.pattern_name_table[i+1] >> 4;
	    }
	
	    /* calculate page according to scroll data */
	    /* - assuming full-screen scroll only for now... */
	    sy = (int)ygv608.scroll_data_table[0][0x00] +
	         (((int)ygv608.scroll_data_table[0][0x01] & 0x0f ) << 8);
	    sx = (int)ygv608.scroll_data_table[0][0x80] +
	         (((int)ygv608.scroll_data_table[0][0x81] & 0x0f ) << 8);
	    if( ygv608.regs.s.md == MD_2PLANE_16BIT ) {
	      page = ( ( sx + col * 8 ) % 1024 ) / 256;
	      page += ( ( ( sy + row * 8 ) % 2048 ) / 256 ) * 4;
	    }
	    else if( ygv608.regs.s.pgs ) {
	      page = ( ( sx + col * 8 ) % 2048 ) / 512;
	      page += ( ( ( sy + row * 8 ) % 2048 ) / 256 ) * 4;
	    }
	    else {
	      page = ( ( sx + col * 8 ) % 2048 ) / 256;
	      page += ( ( ( sy + row * 8 ) % 2048 ) / 512 ) * 8;
	    }
	
	    /* add page, base address to pattern name */
	    j += ( (int)ygv608.scroll_data_table[0][0xc0+page] << 10 );
	    j += ( ygv608.base_addr[0][base] << 8 );
	
	    if( j >= Machine.drv.gfxdecodeinfo[set].gfxlayout.total ) {
		logerror( "A_8X8: tilemap=%d\n", j );
	      j = 0;
	    }
	    if( ygv608.regs.s.apf != 0 ) {
	      // attribute only valid in 16 color mode
	      if( set == GFX_8X8_4BIT )
	        attr = ( j >> ( ( ygv608.regs.s.apf - 1 ) * 2 ) ) & 0x0f;
	    }
	    SET_TILE_INFO( set, j, attr & 0x0F, 0 );
	  }
	}
	
	static void get_tile_info_B_8( int offset )
	{
	  // extract row,col packed into offset
	  int             col = offset >> 6;
	  int             row = offset & 0x3f;
	
	  unsigned char   attr = 0;
	  int             pattern_name_base = ( ( ygv608.page_y << ygv608.pny_shift )
						<< ygv608.bits16 );
	  int             set = GFX_8X8_4BIT;
	  int             base = row >> ygv608.base_y_shift;
	
	  if( ygv608.regs.s.md & MD_1PLANE ) {
	    SET_TILE_INFO( set, 0, 0, 0 );
	  }
	  else if( col >= ygv608.page_x ) {
	    SET_TILE_INFO( set, 0, 0, 0 );
	  }
	  else if( row >= ygv608.page_y ) {
	    SET_TILE_INFO( set, 0, 0, 0 );
	  }
	  else {
	    int sx, sy, page;
	    int i = pattern_name_base +
	      ( ( ( row << ygv608.pny_shift ) + col ) << ygv608.bits16 );
	    int j = ygv608.pattern_name_table[i];
	    if( ygv608.bits16 ) {
	      j += ((int)(ygv608.pattern_name_table[i+1] & ygv608.na8_mask )) << 8;
	      attr = ygv608.pattern_name_table[i+1] >> 4; /*& 0x00; 0xf0;*/
	    }
	
	    /* calculate page according to scroll data */
	    /* - assuming full-screen scroll only for now... */
	    sy = (int)ygv608.scroll_data_table[1][0x00] +
	         (((int)ygv608.scroll_data_table[1][0x01] & 0x0f ) << 8);
	    sx = (int)ygv608.scroll_data_table[1][0x80] +
	         (((int)ygv608.scroll_data_table[1][0x81] & 0x0f ) << 8);
	    if( ygv608.regs.s.md == MD_2PLANE_16BIT ) {
	      page = ( ( sx + col * 8 ) % 1024 ) / 256;
	      page += ( ( ( sy + row * 8 ) % 2048 ) / 256 ) * 4;
	    }
	    else if( ygv608.regs.s.pgs ) {
	      page = ( ( sx + col * 8 ) % 2048 ) / 512;
	      page += ( ( ( sy + row * 8 ) % 2048 ) / 256 ) * 4;
	    }
	    else {
	      page = ( ( sx + col * 8 ) % 2048 ) / 256;
	      page += ( ( ( sy + row * 8 ) % 2048 ) / 512 ) * 8;
	    }
	
	    /* add page, base address to pattern name */
	    j += ( (int)ygv608.scroll_data_table[1][0xc0+page] << 10 );
	    j += ( ygv608.base_addr[1][base] << 8 );
	
	    if( j >= Machine.drv.gfxdecodeinfo[set].gfxlayout.total ) {
		logerror( "B_8X8: tilemap=%d\n", j );
	      j = 0;
	    }
	    if( ygv608.regs.s.bpf != 0 ) {
	      /* assume 16 colour mode for now... */
	      attr = ( j >> ( ( ygv608.regs.s.bpf - 1 ) * 2 ) ) & 0x0f;
	    }
	    SET_TILE_INFO( set, j, attr, 0 );
	  }
	}
	
	static void get_tile_info_A_16( int offset )
	{
	  // extract row,col packed into offset
	  int             col = offset >> 6;
	  int             row = offset & 0x3f;
	
	  unsigned char   attr = 0;
	  int             pattern_name_base = 0;
	  int             set = ( ygv608.regs.s.md == MD_1PLANE_256COLOUR
				            ? GFX_16X16_8BIT : GFX_16X16_4BIT );
	  int             base = row >> ygv608.base_y_shift;
	
	  if( col >= ygv608.page_x ) {
	    SET_TILE_INFO( set, 0, 0, 0 );
	  }
	  else if( row >= ygv608.page_y ) {
	    SET_TILE_INFO( set, 0, 0, 0 );
	  }
	  else {
	    int sx, sy, page;
	    int j;
	    int i = ( ( ( row << ygv608.pny_shift ) + col ) << ygv608.bits16 );
	    i += pattern_name_base;
	
	    j = ygv608.pattern_name_table[i];
	    if( ygv608.bits16 ) {
	      j += ((int)(ygv608.pattern_name_table[i+1] & ygv608.na8_mask )) << 8;
	      // attribute only valid in 16 color mode
	      if( set == GFX_16X16_4BIT )
	        attr = ygv608.pattern_name_table[i+1] >> 4;
	    }
	
	    /* calculate page according to scroll data */
	    /* - assuming full-screen scroll only for now... */
	    sy = (int)ygv608.scroll_data_table[0][0x00] +
	         (((int)ygv608.scroll_data_table[0][0x01] & 0x0f ) << 8);
	    sx = (int)ygv608.scroll_data_table[0][0x80] +
	         (((int)ygv608.scroll_data_table[0][0x81] & 0x0f ) << 8);
	    if( ygv608.regs.s.md == MD_2PLANE_16BIT ) {
	      page = ( ( sx + col * 16 ) % 2048 ) / 512;
	      page += ( ( sy + row * 16 ) / 512 ) * 4;
	    }
	    else if( ygv608.regs.s.pgs ) {
	      page = ( sx + col * 16 ) / 512;
	      page += ( ( sy + row * 16 ) / 1024 ) * 8;
	    }
	    else {
	      page = ( sx + col * 16 ) / 1024;
	      page += ( ( sy + row * 16 ) / 512 ) * 4;
	    }
	
	    /* add page, base address to pattern name */
	    j += ( (int)ygv608.scroll_data_table[0][0xc0+page] << 8 );
	    j += ( ygv608.base_addr[0][base] << 8 );
	
	    if( j >= Machine.drv.gfxdecodeinfo[set].gfxlayout.total ) {
		logerror( "A_16X16: tilemap=%d\n", j );
	      j = 0;
	    }
	    if( ygv608.regs.s.apf != 0 ) {
	      // attribute only valid in 16 color mode
	      if( set == GFX_16X16_4BIT )
	        attr = ( j >> ( ygv608.regs.s.apf * 2 ) ) & 0x0f;
	    }
	    SET_TILE_INFO( set, j, attr, 0 );
	  }
	}
	
	static void get_tile_info_B_16( int offset )
	{
	  // extract row,col packed into offset
	  int             col = offset >> 6;
	  int             row = offset & 0x3f;
	
	  unsigned char   attr = 0;
	  int             pattern_name_base = ( ( ygv608.page_y << ygv608.pny_shift )
						<< ygv608.bits16 );
	  int             set = GFX_16X16_4BIT;
	  int             base = row >> ygv608.base_y_shift;
	
	  if( ygv608.regs.s.md & MD_1PLANE ) {
	    SET_TILE_INFO( set, 0, 0, 0 );
	  }
	  if( col >= ygv608.page_x ) {
	    SET_TILE_INFO( set, 0, 0, 0 );
	  }
	  else if( row >= ygv608.page_y ) {
	    SET_TILE_INFO( set, 0, 0, 0 );
	  }
	  else {
	    int sx, sy, page;
	    int j;
	    int i = ( ( ( row << ygv608.pny_shift ) + col ) << ygv608.bits16 );
	    i += pattern_name_base;
	
	    j = ygv608.pattern_name_table[i];
	    if( ygv608.bits16 ) {
	      j += ((int)(ygv608.pattern_name_table[i+1] & ygv608.na8_mask )) << 8;
	      attr = ygv608.pattern_name_table[i+1] >> 4; /*& 0x00; 0xf0;*/
	    }
	
	    /* calculate page according to scroll data */
	    /* - assuming full-screen scroll only for now... */
	    sy = (int)ygv608.scroll_data_table[1][0x00] +
	         (((int)ygv608.scroll_data_table[1][0x01] & 0x0f ) << 8);
	    sx = (int)ygv608.scroll_data_table[1][0x80] +
	         (((int)ygv608.scroll_data_table[1][0x81] & 0x0f ) << 8);
	    if( ygv608.regs.s.md == MD_2PLANE_16BIT ) {
	      page = ( ( sx + col * 16 ) % 2048 ) / 512;
	      page += ( ( sy + row * 16 ) / 512 ) * 4;
	    }
	    else if( ygv608.regs.s.pgs ) {
	      page = ( sx + col * 16 ) / 512;
	      page += ( ( sy + row * 16 ) / 1024 ) * 8;
	    }
	    else {
	      page = ( sx + col * 16 ) / 1024;
	      page += ( ( sy + row * 16 ) / 512 ) * 4;
	    }
	
	    /* add page, base address to pattern name */
	    j += ( (int)ygv608.scroll_data_table[1][0xc0+page] << 8 );
	    j += ( ygv608.base_addr[1][base] << 8 );
	
	    if( j >= Machine.drv.gfxdecodeinfo[set].gfxlayout.total ) {
		logerror( "B_16X16: tilemap=%d\n", j );
	      j = 0;
	    }
	    if( ygv608.regs.s.bpf != 0 ) {
	      /* assume 16 colour mode for now... */
	      attr = ( j >> ( ygv608.regs.s.bpf * 2 ) ) & 0x0f;
	    }
	    SET_TILE_INFO( set, j, attr, 0 );
	  }
	}
	
	public static VhStartPtr ygv608_vh_start = new VhStartPtr() { public int handler() 
	{
	    // flag rebuild of the tilemaps
	    ygv608.screen_resize = 1;
	    ygv608.tilemap_resize = 1;
	
	    return 0;
	} };
	
	public static VhStopPtr ygv608_vh_stop = new VhStopPtr() { public void handler() 
	{
	    if (work_bitmap != 0)
	        bitmap_free( work_bitmap );
	} };
	
	static void draw_sprites( struct osd_bitmap *bitmap )
	{
	#ifdef _ENABLE_SPRITES
	
	  // sprites are always clipped to 512x512
	  // - regardless of the visible display dimensions
	  static struct rectangle spriteClip = { 0, 512, 0, 512 };
	
	  PSPRITE_ATTR sa;
	  int flipx = 0, flipy = 0;
	  int i;
	
	  /* ensure that sprites are enabled */
	  if( ( ygv608.regs.s.dspe == 0 ) || ygv608.regs.s.sprd )
	    return;
	
	  /* draw sprites */
	  sa = &ygv608.sprite_attribute_table.s[YGV608_MAX_SPRITES-1];
	  for( i=0; i<YGV608_MAX_SPRITES; i++, sa-- ) {
	    int code, color, sx, sy, size;
	
	    color = (int)sa.sc4;
	    sx = ( (int)sa.sx8 << 8 ) | (int)sa.sx;
	    sy = ( ( ( (int)sa.sy8 << 8 ) | (int)sa.sy ) + 1 ) & 0x1ff;
	
	    if( ygv608.regs.s.spas == SPAS_SPRITESIZE ) {
	      size = ygv608.regs.s.spa;
	      flipx = ( sa.sz & SZ_HORIZREVERSE ) != 0;
	      flipy = ( sa.sz & SZ_VERTREVERSE ) != 0;
	    }
	    else {
	      size = sa.sz;
	      flipx = ( ygv608.regs.s.spa & SZ_HORIZREVERSE ) != 0;
	      flipy = ( ygv608.regs.s.spa & SZ_VERTREVERSE ) != 0;
	    }
	
	    switch( size ) {
	
	    case SZ_8X8 :
	      code = ( (int)ygv608.regs.s.sba << 8 ) | (int)sa.sn;
	      if( ygv608.regs.s.spf != 0 )
		    color = ( code >> ( ( ygv608.regs.s.spf - 1 ) * 2 ) ) & 0x0f;
	      if( code >= Machine.drv.gfxdecodeinfo[GFX_8X8_4BIT].gfxlayout.total ) {
		    logerror( "SZ_8X8: sprite=%d\n", code );
		    code = 0;
	      }
	      drawgfx( bitmap, Machine.gfx[GFX_8X8_4BIT],
		       code,
		       color,
		       flipx,flipy,
		       sx,sy,
		       &spriteClip,TRANSPARENCY_PEN,0x00);
	      // redraw with wrap-around
	      if( sx > 512-8 )
	        drawgfx( bitmap, Machine.gfx[GFX_8X8_4BIT],
		        code,
		        color,
		        flipx,flipy,
		        sx-512,sy,
		        &spriteClip,TRANSPARENCY_PEN,0x00);
	      if( sy > 512-8 )
	        drawgfx( bitmap, Machine.gfx[GFX_8X8_4BIT],
		        code,
		        color,
		        flipx,flipy,
		        sx,sy-512,
		        &spriteClip,TRANSPARENCY_PEN,0x00);
	      // really should draw again for both wrapped!
	      // - ignore until someone thinks it's required
	      break;
	
	    case SZ_16X16 :
	      code = ( ( (int)ygv608.regs.s.sba & 0xfc ) << 6 ) | (int)sa.sn;
	      if( ygv608.regs.s.spf != 0 )
		    color = ( code >> ( ygv608.regs.s.spf * 2 ) ) & 0x0f;
	      if( code >= Machine.drv.gfxdecodeinfo[GFX_16X16_4BIT].gfxlayout.total ) {
		    logerror( "SZ_8X8: sprite=%d\n", code );
		    code = 0;
	      }
	      drawgfx( bitmap, Machine.gfx[GFX_16X16_4BIT],
		       code,
		       color,
		       flipx,flipy,
		       sx,sy,
		       &spriteClip,TRANSPARENCY_PEN,0x00);
	      // redraw with wrap-around
	      if( sx > 512-16 )
	        drawgfx( bitmap, Machine.gfx[GFX_16X16_4BIT],
		        code,
		        color,
		        flipx,flipy,
		        sx-512,sy,
		        &spriteClip,TRANSPARENCY_PEN,0x00);
	      if( sy > 512-16 )
	        drawgfx( bitmap, Machine.gfx[GFX_16X16_4BIT],
		        code,
		        color,
		        flipx,flipy,
		        sx,sy-512,
		        &spriteClip,TRANSPARENCY_PEN,0x00);
	      // really should draw again for both wrapped!
	      // - ignore until someone thinks it's required
	      break;
	
	    case SZ_32X32 :
	      code = ( ( (int)ygv608.regs.s.sba & 0xf0 ) << 4 ) | (int)sa.sn;
	      if( ygv608.regs.s.spf != 0 )
		color = ( code >> ( ( ygv608.regs.s.spf + 1 ) * 2 ) ) & 0x0f;
	      if( code >= Machine.drv.gfxdecodeinfo[GFX_32X32_4BIT].gfxlayout.total ) {
		  logerror( "SZ_32X32: sprite=%d\n", code );
		code = 0;
	      }
	      drawgfx( bitmap, Machine.gfx[GFX_32X32_4BIT],
		       code,
		       color,
		       flipx,flipy,
		       sx,sy,
		       &spriteClip,TRANSPARENCY_PEN,0x00);
	      // redraw with wrap-around
	      if( sx > 512-32 )
	        drawgfx( bitmap, Machine.gfx[GFX_32X32_4BIT],
		        code,
		        color,
		        flipx,flipy,
		        sx-512,sy,
		        &spriteClip,TRANSPARENCY_PEN,0x00);
	      if( sy > 512-32 )
	        drawgfx( bitmap, Machine.gfx[GFX_32X32_4BIT],
		        code,
		        color,
		        flipx,flipy,
		        sx,sy-512,
		        &spriteClip,TRANSPARENCY_PEN,0x00);
	      // really should draw again for both wrapped!
	      // - ignore until someone thinks it's required
	      break;
	
	    default :
	        logerror( "unsupported sprite size %d\n", size );
	      break;
	    }
	  }
	
	#endif
	}
	
	#ifdef _SHOW_VIDEO_DEBUG
	static char *mode[] = { "2PLANE_8BIT",
				"2PLANE_16BIT",
				"1PLANE_16COLORS",
				"1PLANE_256COLORS" };
	
	static char *psize[] = { "8x8", "16x16", "32x32", "64x64" };
	#endif
	
	void ygv608_vh_update( struct osd_bitmap *bitmap, int full_refresh )
	{
	#ifdef _SHOW_VIDEO_DEBUG
	    char buffer[64];
	#endif
	    int col;
	
	  if( ygv608.screen_resize ) {
	
	#ifdef _ENABLE_SCREEN_RESIZE
	    // hdw should be scaled by 16, not 8
	    // - is it something to do with double dot-clocks???
	    set_visible_area( 0, ((int)(ygv608.regs.s.hdw)<<3/*4*/)-1,
	                      0, ((int)(ygv608.regs.s.vdw)<<3)-1 );
	#endif
	
	#ifdef _ENABLE_ROTATE_ZOOM
	    if (work_bitmap != 0)
	        bitmap_free( work_bitmap );
	    work_bitmap = bitmap_alloc_depth( Machine.drv.screen_width,
	                                      Machine.drv.screen_height,
	                                      Machine.color_depth );
	#else
	    work_bitmap = bitmap;
	#endif
	
	    // reset resize flag
	    ygv608.screen_resize = 0;
	  }
	
	  if( ygv608.tilemap_resize ) {
	
	    if (tilemap_A != 0)
	    {
	        tilemap_dispose( tilemap_A );
	        tilemap_A = NULL;
	    }
	
	    if( ygv608.regs.s.pts == PTS_8X8 )
	        tilemap_A = tilemap_create( get_tile_info_A_8,
	                                    get_tile_offset,
					                    TILEMAP_TRANSPARENT,
	                                    8, 8,
					                    ygv608.page_x, ygv608.page_y );
	    else
	        tilemap_A = tilemap_create( get_tile_info_A_16,
	                                    get_tile_offset,
					                    TILEMAP_TRANSPARENT,
					                    16, 16,
					                    ygv608.page_x, ygv608.page_y );
	
	    tilemap_set_transparent_pen( tilemap_A, 0 );
	    // for NCV1 it's sufficient to scroll only columns
	    tilemap_set_scroll_cols( tilemap_A, ygv608.page_x );
	
	    if (tilemap_B != 0)
	    {
	        tilemap_dispose( tilemap_B );
	        tilemap_B = NULL;
	    }
	
	    if( ygv608.regs.s.pts == PTS_8X8 )
	        tilemap_B = tilemap_create( get_tile_info_B_8,
	                                    get_tile_offset,
					                    TILEMAP_OPAQUE,
	                                    8, 8,
					                    ygv608.page_x, ygv608.page_y );
	    else
	        tilemap_B = tilemap_create( get_tile_info_B_16,
	                                    get_tile_offset,
					                    TILEMAP_OPAQUE,
					                    16, 16,
					                    ygv608.page_x, ygv608.page_y );
	
	    // for NCV1 it's sufficient to scroll only columns
	    tilemap_set_scroll_cols( tilemap_B, ygv608.page_x );
	
	    // now clear the screen in case we change to 1-plane mode
	    fillbitmap( work_bitmap,
	                Machine.pens[0],
	                &Machine.visible_area );
	
	    // reset resize flag
	    ygv608.tilemap_resize = 0;
	  }
	
	#ifdef _ENABLE_SCROLLY
	
	  for( col=0; col<ygv608.page_x; col++ )
	  {
	    tilemap_set_scrolly( tilemap_B, col,
				   ( (int)ygv608.scroll_data_table[1][(col>>ygv608.col_shift)<<1] +
				     ( (int)ygv608.scroll_data_table[1][((col>>ygv608.col_shift)<<1)+1] << 8 ) ) );
	
	    tilemap_set_scrolly( tilemap_A, col,
				 ( (int)ygv608.scroll_data_table[0][(col>>ygv608.col_shift)<<1] +
				   ( (int)ygv608.scroll_data_table[0][((col>>ygv608.col_shift)<<1)+1] << 8 ) ) );
	  }
	
	#endif
	
	#ifdef _ENABLE_SCROLLX
	
	    tilemap_set_scrollx( tilemap_B, 0,
				   ( (int)ygv608.scroll_data_table[1][0x80] +
				     ( (int)ygv608.scroll_data_table[1][0x81] << 8 ) ) );
	
	    tilemap_set_scrollx( tilemap_A, 0,
				 ( (int)ygv608.scroll_data_table[0][0x80] +
				   ( (int)ygv608.scroll_data_table[0][0x81] << 8 ) ) );
	
	#endif
	
	  tilemap_set_enable( tilemap_A, ygv608.regs.s.dspe );
	  if( ygv608.regs.s.md & MD_1PLANE )
	    tilemap_set_enable( tilemap_B, 0 );
	  else
	    tilemap_set_enable( tilemap_B, ygv608.regs.s.dspe );
	  tilemap_mark_all_tiles_dirty( tilemap_A );
	  tilemap_mark_all_tiles_dirty( tilemap_B );
	  tilemap_update( ALL_TILEMAPS );
	
	  palette_init_used_colors();
	  // mark colours used by sprites
	  palette_recalc();
	
	  /*
	   *    now we can render the screen
	   */
	
	  tilemap_draw( work_bitmap, tilemap_B, 0, 0 );
	#ifdef _ENABLE_ROTATE_ZOOM
	  if( ygv608.regs.s.zron )
	    copyrozbitmap( bitmap, work_bitmap,
	                   ygv608.ax, // + ( Machine.visible_area.min_x << 16 ),
	                   ygv608.ay, // + ( Machine.visible_area.min_y << 16 ),
	                   ygv608.dx, ygv608.dxy, ygv608.dyx, ygv608.dy, 0,
	                   &Machine.visible_area,
	                   TRANSPARENCY_NONE, 0, 0 );
	  else
	    copybitmap( bitmap, work_bitmap, 0, 0, 0, 0,
	                &Machine.visible_area,
	                TRANSPARENCY_NONE, 0 );
	
	  // for some reason we can't use an opaque tilemap_A
	  // so use a transparent but clear the work bitmap first
	  // - look at why this is the case?!?
	  fillbitmap( work_bitmap,
	              Machine.pens[0],
	              &Machine.visible_area );
	#endif
	
	  if( ygv608.regs.s.prm == PRM_ASBDEX ||
		  ygv608.regs.s.prm == PRM_ASEBDX )
	      draw_sprites( bitmap );
	
	  tilemap_draw( work_bitmap, tilemap_A, 0, 0 );
	#ifdef _ENABLE_ROTATE_ZOOM
	  if( ygv608.regs.s.zron )
	    copyrozbitmap( bitmap, work_bitmap,
	                   ygv608.ax, // + ( Machine.visible_area.min_x << 16 ),
	                   ygv608.ay, // + ( Machine.visible_area.min_y << 16 ),
	                   ygv608.dx, ygv608.dxy, ygv608.dyx, ygv608.dy, 0,
	                   &Machine.visible_area,
	                   TRANSPARENCY_PEN, Machine.pens[0], 0 );
	  else
	    copybitmap( bitmap, work_bitmap, 0, 0, 0, 0,
	                &Machine.visible_area,
	                TRANSPARENCY_PEN, Machine.pens[0] );
	#endif
	
	  if( ygv608.regs.s.prm == PRM_SABDEX ||
	      ygv608.regs.s.prm == PRM_SEABDX )
	      draw_sprites( bitmap );
	
	
	#ifdef _SHOW_VIDEO_DEBUG
	
	  /* show screen control information */
	  ui_text( bitmap, mode[ygv608.regs.s.md], 0, 0 );
	  sprintf( buffer, "%02ux%02u", ygv608.page_x, ygv608.page_y );
	  ui_text( bitmap, buffer, 0, 16 );
	  ui_text( bitmap, psize[ygv608.regs.s.pts], 0, 32 );
	  sprintf( buffer, "A: SX:%d SY:%d",
		   (int)ygv608.scroll_data_table[0][0x80] +
		   ( ( (int)ygv608.scroll_data_table[0][0x81] & 0x0f ) << 8 ),
		   (int)ygv608.scroll_data_table[0][0x00] +
		   ( ( (int)ygv608.scroll_data_table[0][0x01] & 0x0f ) << 8 ) );
	  ui_text( bitmap, buffer, 0, 48 );
	  sprintf( buffer, "B: SX:%d SY:%d",
		   (int)ygv608.scroll_data_table[1][0x80] +
		   ( ( (int)ygv608.scroll_data_table[1][0x81] & 0x0f ) << 8 ),
		   (int)ygv608.scroll_data_table[1][0x00] +
		   ( ( (int)ygv608.scroll_data_table[1][0x01] & 0x0f ) << 8 ) );
	  ui_text( bitmap, buffer, 0, 64 );
	
	#endif
	}
	
	static static static void SetPreShortcuts( int reg, int data );
	
	READ16_HANDLER( ygv608_r )
	{
	  static int p0_state = 0;
	  static int p3_state = 0;
	  static int pattern_name_base = 0;  /* pattern name table base address */
	  int pn;
	  data16_t  data = 0;
	
	  switch( offset ) {
	
	  case 0x00:
	    switch( p0_state ) {
	
	    case 0 :
	
	      /* Are we reading from plane B? */
	      if( !( ygv608.regs.s.md & MD_1PLANE ) )
		  if( ygv608.regs.s.b_a )
		    pattern_name_base = ( ( ygv608.page_y << ygv608.pny_shift )
					                << ygv608.bits16 );
	
	      /* read character from ram */
	      pn = pattern_name_base +
		        ( ( ( ygv608.regs.s.pny << ygv608.pny_shift ) +
		            ygv608.regs.s.pnx ) <<
		            ygv608.bits16 );
	      if( pn > 4095 ) {
		    logerror( "attempt (0) to read pattern name %d\n"
			   "mode = %d, pgs = %d (%dx%d)\n"
			   "pattern_name_base = %d\n"
			   "pnx = %d, pny = %d, pny_shift = %d, bits16 = %d\n",
			   pn, ygv608.regs.s.md, ygv608.regs.s.pgs,
			   ygv608.page_x, ygv608.page_y,
			   pattern_name_base,
			   ygv608.regs.s.pnx, ygv608.regs.s.pny, ygv608.pny_shift,
			   ygv608.bits16 );
		    pn = 0;
	      }
	      data = ygv608.pattern_name_table[pn];
	      break;
	
	    case 1 :
	
	      /* read character from ram */
	      pn = pattern_name_base +
		    ( ( ( ygv608.regs.s.pny << ygv608.pny_shift ) +
		        ygv608.regs.s.pnx ) <<
		        ygv608.bits16 ) + 1;
	      if( pn > 4095 ) {
		    logerror( "attempt (1) to read pattern name %d\n"
			   "mode = %d\n"
			   "pattern_name_base = %d\n"
			   "pnx = %d, pny = %d, pny_shift = %d, bits16 = %d\n",
			   pn, ygv608.regs.s.md, pattern_name_base,
			   ygv608.regs.s.pnx, ygv608.regs.s.pny, ygv608.pny_shift,
			   ygv608.bits16 );
		    pn = 0;
	      }
	      data = ygv608.pattern_name_table[pn];
	      break;
	    }
	
	    p0_state++;
	    if( ygv608.regs.s.md == MD_2PLANE_8BIT )
	      p0_state++;
	
	    if( p0_state == 2 ) {
	
	      if( ygv608.regs.s.pnya ) {
		    if( ygv608.regs.s.pny++ == ( ygv608.page_y - 1 ) ) {
		      ygv608.regs.s.pny = 0;
		      if( ygv608.regs.s.pnx++ == ( ygv608.page_x - 1 ) ) {
		        ygv608.regs.s.pnx = 0;
		        ygv608.regs.s.b_a = ~ygv608.regs.s.b_a;
		      }
		    }
	      }
	      else {
		    if( ygv608.regs.s.pnxa ) {
		      if( ygv608.regs.s.pnx++ == ( ygv608.page_x - 1 ) ) {
		        ygv608.regs.s.pnx = 0;
		        if( ygv608.regs.s.pny++ == ( ygv608.page_y - 1 ) ) {
		          ygv608.regs.s.pny = 0;
		          ygv608.regs.s.b_a = ~ygv608.regs.s.b_a;
		        }
		      }
		    }
	      }
	      p0_state = 0;
	      pattern_name_base = 0;
	    };
	    return( data << 8 );
	    break;
	
	  case 0x01: /* P#1 - sprite data port */
	    data = ygv608.sprite_attribute_table.b[ygv608.regs.s.saa];
	    if( ygv608.regs.s.saar )
	      ygv608.regs.s.saa++;
	    return( data << 8 );
	    break;
	
	  case 0x02: /* P#2 - scroll data port */
	    data = ygv608.scroll_data_table[ygv608.regs.s.p2_b_a][ygv608.regs.s.sca];
	    if( ygv608.regs.s.scar ) {
	      ygv608.regs.s.sca++;
	      /* handle wrap to next plane */
	      if( ygv608.regs.s.sca == 0 )
	      ygv608.regs.s.p2_b_a++;
	    }
	    return( data << 8 );
	    break;
	
	  case 0x03: /* P#3 - color palette data port */
	    data = ygv608.colour_palette[ygv608.regs.s.cc][p3_state];
	    if( ++p3_state == 3 ) {
	      p3_state = 0;
	      if( ygv608.regs.s.cpar )
		    ygv608.regs.s.cc++;
	    }
	    return( data << 8 );
	    break;
	
	  case 0x04: /* P#4 - register data port */
	    data = ygv608.regs.b[ygv608.ports.s.rn];
	    if( ygv608.ports.s.rrai )
	      ygv608.ports.s.rn++;
	    return( data << 8 );
	    break;
	
	  case 0x05:
	    break;
	
	  case 0x06:
	  case 0x07:
	    return( (data16_t)(ygv608.ports.b[offset]) << 8 );
	
	  default :
	    logerror( "unknown ygv608 register (%d)\n", offset );
	    break;
	  }
	
	  return( 0 );
	}
	
	WRITE16_HANDLER( ygv608_w )
	{
	  static int p0_state = 0;
	  static int p3_state = 0;
	  static int pattern_name_base = 0;  /* pattern name table base address */
	  int pn;
	
	  data = ( data >> 8 ) & 0xff;
	
	  switch( offset ) {
	
	  case 0x00 : /* P#0 - pattern name table data port */
	
	    switch( p0_state ) {
	
	    case 0 :
	
	      /* Are we writing into plane B? */
	      if( !( ygv608.regs.s.md & MD_1PLANE ) )
		if( ygv608.regs.s.b_a )
		  pattern_name_base = ( ( ygv608.page_y << ygv608.pny_shift )
					<< ygv608.bits16 );
	
	      /* write character to ram */
	      pn = pattern_name_base +
		( ( ( ygv608.regs.s.pny << ygv608.pny_shift ) +
		    ygv608.regs.s.pnx ) <<
		  ygv608.bits16 );
	      if( pn > 4095 ) {
		  logerror( "attempt (0) to write pattern name %d\n"
			   "mode = %d, pgs = %d (%dx%d)\n"
			   "pattern_name_base = %d\n"
			   "pnx = %d, pny = %d, pny_shift = %d, bits16 = %d\n",
			   pn, ygv608.regs.s.md, ygv608.regs.s.pgs,
			   ygv608.page_x, ygv608.page_y,
			   pattern_name_base,
			   ygv608.regs.s.pnx, ygv608.regs.s.pny, ygv608.pny_shift,
			   ygv608.bits16 );
		pn = 0;
	      }
	      ygv608.pattern_name_table[pn] = data;
	      break;
	
	    case 1 :
	
	      /* write character to ram */
	      pn = pattern_name_base +
		( ( ( ygv608.regs.s.pny << ygv608.pny_shift ) +
		    ygv608.regs.s.pnx ) <<
		  ygv608.bits16 ) + 1;
	      if( pn > 4095 ) {
		  logerror( "attempt (1) to write pattern name %d\n"
			   "mode = %d\n"
			   "pattern_name_base = %d\n"
			   "pnx = %d, pny = %d, pny_shift = %d, bits16 = %d\n",
			   pn, ygv608.regs.s.md, pattern_name_base,
			   ygv608.regs.s.pnx, ygv608.regs.s.pny, ygv608.pny_shift,
			   ygv608.bits16 );
		pn = 0;
	      }
	      ygv608.pattern_name_table[pn] = data;
	      break;
	    }
	
	    p0_state++;
	    if( ygv608.regs.s.md == MD_2PLANE_8BIT )
	      p0_state++;
	
	    if( p0_state == 2 ) {
	
	      if( ygv608.regs.s.pnya ) {
		    if( ygv608.regs.s.pny++ == ( ygv608.page_y - 1 ) ) {
		      ygv608.regs.s.pny = 0;
		      if( ygv608.regs.s.pnx++ == ( ygv608.page_x - 1 ) ) {
		        ygv608.regs.s.pnx = 0;
		        ygv608.regs.s.b_a = ~ygv608.regs.s.b_a;
		      }
		    }
	      }
	      else {
		    if( ygv608.regs.s.pnxa ) {
		      if( ygv608.regs.s.pnx++ == ( ygv608.page_x - 1 ) ) {
		        ygv608.regs.s.pnx = 0;
		        if( ygv608.regs.s.pny++ == ( ygv608.page_y - 1 ) ) {
		          ygv608.regs.s.pny = 0;
		          ygv608.regs.s.b_a = ~ygv608.regs.s.b_a;
		        }
		      }
		    }
	      }
	      p0_state = 0;
	      pattern_name_base = 0;
	    };
	    break;
	
	  case 0x01 : /* P#1 - sprite data port */
	    ygv608.sprite_attribute_table.b[ygv608.regs.s.saa] = data;
	    if( ygv608.regs.s.saaw )
	      ygv608.regs.s.saa++;
	    break;
	
	  case 0x02 : /* P#2 - scroll data port */
	    ygv608.scroll_data_table[ygv608.regs.s.p2_b_a][ygv608.regs.s.sca] = data;
	    if( ygv608.regs.s.scaw ) {
	      ygv608.regs.s.sca++;
	      /* handle wrap to next plane */
	      if( ygv608.regs.s.sca == 0 )
	        ygv608.regs.s.p2_b_a++;
	    }
	    break;
	
	  case 0x03 : /* P#3 - colour palette data port */
	    ygv608.colour_palette[ygv608.regs.s.cc][p3_state] = data;
	    if( ++p3_state == 3 ) {
	      p3_state = 0;
	      palette_change_color( ygv608.regs.s.cc,
				    ygv608.colour_palette[ygv608.regs.s.cc][0] << 2,
				    ygv608.colour_palette[ygv608.regs.s.cc][1] << 2,
				    ygv608.colour_palette[ygv608.regs.s.cc][2] << 2 );
	      if( ygv608.regs.s.cpaw )
		    ygv608.regs.s.cc++;
	    }
	    break;
	
	  case 0x04 : /* P#4 - register data port */
	#if 0
	      logerror( "R#%d = $%02X\n",
		       ygv608.ports.s.rn, data );
	#endif
	    SetPreShortcuts( ygv608.ports.s.rn, data );
	    ygv608.regs.b[ygv608.ports.s.rn] = data;
	    SetPostShortcuts( ygv608.ports.s.rn );
	    if( ygv608.ports.s.rwai )
	      if( ++ygv608.ports.s.rn == 50 ) {
		    ygv608.ports.s.rn = 0;
		    logerror( "warning: rn=50\n" );
	      }
	    break;
	
	  case 0x05 : /* P#5 - register select port */
	    ygv608.ports.b[5] = data;
	    break;
	
	  case 0x06 : /* P#6 - status port */
	    /* writing a '1' resets that bit */
	    ygv608.ports.b[6] &= ~data;
	    break;
	
	  case 0x07 : /* P#7 - system control port */
	    ygv608.ports.b[7] = data;
	    if( ygv608.ports.b[7] & 0x3e )
	      HandleRomTransfers();
	    if( ygv608.ports.b[7] & 0x01 )
	      HandleYGV608Reset();
	    break;
	
	  default :
	    logerror( "unknown ygv608 register (%d)\n", offset );
	    break;
	  }
	}
	
	void HandleYGV608Reset( void )
	{
	    int i;
	
	    /* Clear ports #0-7 */
	    memset( &ygv608.ports.b[0], 0, 8 );
	
	    /* Clear registers #0-38, #47-49 */
	    memset( &ygv608.regs.b[0], 0, 39 );
	    memset( &ygv608.regs.b[47], 0, 3 );
	
	    /* Clear internal ram */
	    memset( ygv608.pattern_name_table, 0, 4096 );
	    memset( ygv608.sprite_attribute_table.b, 0,
	            YGV608_SPRITE_ATTR_TABLE_SIZE );
	    memset( ygv608.scroll_data_table, 0, 2*256 );
	    memset( ygv608.colour_palette, 0, 25*3 );
	
	    /* should set shortcuts here too */
	    for( i=0; i<50; i++ ) {
	      //SetPreShortcuts( i );
	      SetPostShortcuts( i );
	    }
	}
	
	/*
	    The YGV608 has a function to block-move data from the rom into
	    internal tables. This function is not used in NCV1, but I used
	    it for testing trojan ROM software.
	    - So leave it in!
	 */
	
	void HandleRomTransfers( void )
	{
	#if 0
	  static UBytePtr sdt = (UBytePtr )ygv608.scroll_data_table;
	  static UBytePtr sat = (UBytePtr )ygv608.sprite_attribute_table.b;
	
	  /* fudge copy from sprite data for now... */
	  UBytePtr RAM = Machine.memory_region[0];
	  int i;
	
	  int src = ( ( (int)ygv608.regs.s.tb13 << 8 ) +
		      (int)ygv608.regs.s.tb5 ) << 5;
	  int bytes = (int)ygv608.regs.s.tn4 << 4;
	
	    logerror( "Transferring data from rom...\n" );
	
	  /* pattern name table */
	  if( ygv608.ports.s.tn ) {
	  }
	
	  /* scroll table */
	  if( ygv608.ports.s.tl ) {
	
	    int dest = (int)ygv608.regs.s.sca;
	    if( ygv608.regs.s.p2_b_a )
	      dest += 0x100;
	
	    /* fudge a transfer for now... */
	    for( i=0; i<bytes; i++ ) {
	      sdt[(dest+i)%512] = RAM[src+(i^0x01)];
	
	    }
	
	    /* flag as finished */
	    ygv608.ports.s.tl = 0;
	  }
	
	  /* sprite attribute table */
	  if( ygv608.ports.s.ts ) {
	
	    int dest = (int)ygv608.regs.s.saa;
	
	    /* fudge a transfer for now... */
	    for( i=0; i<bytes; i++ ) {
	      sat[(dest+i)%256] = RAM[src+(i^0x01)];
	
	    }
	
	    /* flag as finished */
	    ygv608.ports.s.ts = 0;
	  }
	#endif
	}
	
	void nvsram( offs_t offset, data16_t data )
	{
	  static int i = 0;
	
	  data = ( data >> 8 ) & 0xff;
	
	  if (1 != 0) {
	    static char ascii[16];
	    if( i%16 == 0 )
	      logerror( "%04X: ", offset );
	    logerror( "%02X ", data );
	    ascii[i%16] = ( isprint( data ) ? data : '.' );
	    if( i%16 == 15 )
	      logerror( "| %-16.16s\n", ascii );
	  }
	
	  i++;
	}
	
	// Set any "short-cut" variables before we update the YGV608 registers
	// - these are used only in optimisation of the emulation
	
	void SetPreShortcuts( int reg, int data )
	{
	    switch( reg ) {
	
	        case 7 :
	            if( ( data & MD_MASK ) != ygv608.regs.s.md )
	                ygv608.tilemap_resize = 1;
	            break;
	
	        case 8 :
	            if( ( data & PGS_MASK ) != ygv608.regs.s.pgs )
	                ygv608.tilemap_resize = 1;
	            break;
	
	        case 9 :
	            if( ( data & PTS_MASK ) != ygv608.regs.s.pts )
	                ygv608.tilemap_resize= 1;
	            break;
	
	        case 40 :
	            if( ( data & HDW_MASK ) != ygv608.regs.s.hdw )
	                ygv608.screen_resize = 1;
	            break;
	
	        case 44 :
	            if( ( data & VDW_MASK ) != ygv608.regs.s.vdw )
	                ygv608.screen_resize = 1;
	            break;
	    }
	}
	
	// Set any "short-cut" variables after we have updated the YGV608 registers
	// - these are used only in optimisation of the emulation
	
	void SetPostShortcuts( int reg )
	{
	  int plane, addr;
	
	  switch( reg ) {
	
	  case 0 :
	    if( ygv608.regs.s.pny >= ygv608.page_y )
		logerror( "setting pny(%d) >= page_y(%d) @ $%X\n",
			 ygv608.regs.s.pny, ygv608.page_y, cpu_get_pc() );
	    ygv608.regs.s.pny &= ( ygv608.page_y - 1 );
	    break;
	
	  case 1 :
	    if( ygv608.regs.s.pnx >= ygv608.page_x )
		logerror( "setting pnx(%d) >= page_x(%d) @ $%X\n",
			 ygv608.regs.s.pnx, ygv608.page_x, cpu_get_pc() );
	    ygv608.regs.s.pnx &= ( ygv608.page_x - 1 );
	    break;
	
	  case 6 :
	#if 0
	      logerror( "SBA = $%08X\n", (int)ygv608.regs.s.sba << 13 );
	#endif
	    break;
	
	  case 7 :
	    ygv608.na8_mask = ( ygv608.regs.s.flip ? 0x03 : 0x0f );
	    /* fall thru */
	
	  case 8 :
	    ygv608.bits16 = ( ygv608.regs.s.md == MD_2PLANE_8BIT ? 0 : 1 );
	    if( ygv608.regs.s.md == MD_2PLANE_16BIT )
	      ygv608.page_x = ygv608.page_y = 32;
	    else {
	      if( ygv608.regs.s.pgs == 0 ) {
		ygv608.page_x = 64;
		ygv608.page_y = 32;
	      }
	      else {
		ygv608.page_x = 32;
		ygv608.page_y = 64;
	      }
	    }
	    ygv608.pny_shift = ( ygv608.page_x == 32 ? 5 : 6 );
	
	    /* bits to shift pattern y coordinate to extract base */
	    ygv608.base_y_shift = ( ygv608.page_y == 32 ? 2 : 3 );
	
	    break;
	
	  case 9 :
	    if( ygv608.regs.s.slv != SLV_SCREEN )
	        logerror( "SLV = %1X\n", ygv608.regs.s.slv );
	    switch( ygv608.regs.s.slv )
	    {
	        case SLV_SCREEN :
	            // always use scoll table entry #1
	            ygv608.col_shift = 8;
	            break;
	        default :
	            if( ygv608.regs.s.pts == PTS_8X8 )
	                ygv608.col_shift = ygv608.regs.s.slv - 4;
	            else
	                ygv608.col_shift = ygv608.regs.s.slv - 5;
	            if( ygv608.col_shift < 0 )
	            {
	                // we can't handle certain conditions
	                logerror( "Unhandled slv condition (pts=$%X,slv=$%X)\n",
	                          ygv608.regs.s.pts, ygv608.regs.s.slv );
	                ygv608.col_shift = 8;
	            }
	            break;
	    }
	
	    //if( ygv608.regs.s.slh != SLH_SCREEN )
	    //    logerror( "SLH = %1X\n", ygv608.regs.s.slh );
	    break;
	
	  case 11 :
	    //ShowYGV608Registers();
	    break;
	
	  case 17 : case 18 : case 19 : case 20 :
	  case 21 : case 22 : case 23 : case 24 :
	    plane = (reg-17) >> 2;
	    addr = ( (reg-17) << 1 ) & 0x07;
	    ygv608.base_addr[plane][addr] = ygv608.regs.b[reg] & 0x0f;
	    ygv608.base_addr[plane][addr+1] = ygv608.regs.b[reg] >> 4;
	    break;
	
	  case 25 : case 26 : case 27 :
	    ygv608.ax = (int)ygv608.regs.s.ax16 << 16 |
	                (int)ygv608.regs.s.ax8 << 8 |
	                (int)ygv608.regs.s.ax0;
	    ygv608.ax <<= 7;
	    if( ygv608.ax & 0x08000000 ) ygv608.ax |= 0xf8000000;   // 2s complement
	    break;
	
	  case 28 : case 29 :
	    ygv608.dx = (int)ygv608.regs.s.dx8 << 8 | (int)ygv608.regs.s.dx0;
	    ygv608.dx <<= 7;
	    if( ygv608.dx & 0x00080000 ) ygv608.dx |= 0xfff80000;   // 2s complement
	    break;
	
	  case 30 : case 31 :
	    ygv608.dxy = (int)ygv608.regs.s.dxy8 << 8 | (int)ygv608.regs.s.dxy0;
	    ygv608.dxy <<= 7;
	    if( ygv608.dxy & 0x00080000 ) ygv608.dxy |= 0xfff80000; // 2s complement
	    break;
	
	  case 32 : case 33 : case 34 :
	    ygv608.ay = (int)ygv608.regs.s.ay16 << 16 |
	                (int)ygv608.regs.s.ay8 << 8 |
	                (int)ygv608.regs.s.ay0;
	    ygv608.ay <<= 7;
	    if( ygv608.ay & 0x08000000 ) ygv608.ay |= 0xf8000000;   // 2s complement
	    break;
	
	  case 35 : case 36 :
	    ygv608.dy = (int)ygv608.regs.s.dy8 << 8 | (int)ygv608.regs.s.dy0;
	    ygv608.dy <<= 7;
	    if( ygv608.dy & 0x00080000 ) ygv608.dy |= 0xfff80000;   // 2s complement
	    break;
	
	  case 37 : case 38 :
	    ygv608.dyx = (int)ygv608.regs.s.dyx8 << 8 | (int)ygv608.regs.s.dyx0;
	    ygv608.dyx <<= 7;
	    if( ygv608.dyx & 0x00080000 ) ygv608.dyx |= 0xfff80000; // 2s complement
	    break;
	
	  case 40 : case 41 : case 42 :
	    //ShowYGV608Registers();
	    break;
	
	  }
	
	}
	
	/*
	 *      The rest of this stuff is for debugging only!
	 */
	
	#ifdef MAME_DEBUG
	
	#define nSHOW_SOURCE_MODE
	
	void dump_block( char *name, UBytePtr block, int len )
	{
	  int i;
	
	  logerror( "unsigned char %s[] = {\n", name );
	  for( i=0; i<len; i++ ) {
	    if( i%8 == 0 )
	      logerror( " " );
	    logerror( "0x%02X, ", block[i] );
	    if( i%8 == 7 )
	      logerror( "\n" );
	  }
	  logerror( "};\n" );
	}
	
	READ16_HANDLER( debug_trigger )
	{
	  static int oneshot = 0;
	
	  int i;
	  char ascii[16];
	
	  if (oneshot != 0)
	    return( 0 );
	  oneshot = 1;
	
	  ShowYGV608Registers();
	
	#ifdef SHOW_SOURCE_MODE
	
	  dump_block( "ygv608_regs",
		      (UBytePtr )ygv608.regs.b,
		      64 );
	  dump_block( "ygv608_pnt",
		      (UBytePtr )ygv608.pattern_name_table,
		      4096 );
	  dump_block( "ygv608_sat",
		      (UBytePtr )ygv608.sprite_attribute_table.b,
		      256 );
	  dump_block( "ygv608_sdt",
		      (UBytePtr )ygv608.scroll_data_table,
		      512 );
	  dump_block( "ygv608_cp",
		      (UBytePtr )ygv608.colour_palette,
		      768 );
	
	#else
	
	  /*
	   *  Dump pattern name table ram
	   */
	#if 1
	  logerror( "Pattern Name Table\n" );
	  for( i=0; i<4096; i++ ) {
	    if( i % 16 == 0 )
	      logerror( "$%04X : ", i );
	    logerror( "%02X ", ygv608.pattern_name_table[i] );
	    if( isprint( ygv608.pattern_name_table[i] ) )
	      ascii[i%16] = ygv608.pattern_name_table[i];
	    else
	      ascii[i%16] = '.';
	    if( i % 16 == 15 )
	      logerror( " | %-16.16s\n", ascii );
	  }
	  logerror( "\n" );
	#endif
	
	  /*
	   *  Dump scroll table ram
	   */
	
	  logerror( "Scroll Table\n" );
	  for( i=0; i<512; i++ ) {
	    if( i % 16 == 0 )
	      logerror( "$%04X : ", i );
	    logerror( "%02X ", ygv608.scroll_data_table[0][i] );
	    if( isprint( ygv608.scroll_data_table[0][i] ) )
	      ascii[i%16] = ygv608.scroll_data_table[0][i];
	    else
	      ascii[i%16] = '.';
	    if( i % 16 == 15 )
	      logerror( " | %-16.16s\n", ascii );
	  }
	  logerror( "\n" );
	
	#endif
	
	  return( 0 );
	}
	
	void ShowYGV608Registers( void )
	{
	  int p, b;
	
	  logerror( "YGV608 Registers\n" );
	  logerror(
	       "\tR#00: $%02X : PNYA(%d),B/A(%c),PNY(%d)\n",
		   ygv608.regs.b[0],
		   ygv608.regs.s.pnya,
		   ( ygv608.regs.s.b_a ? 'B' : 'A' ),
		   ygv608.regs.s.pny );
	
	  logerror(
	       "\tR#01: $%02X : PNXA(%d),PNX(%d)\n",
		   ygv608.regs.b[1],
		   ygv608.regs.s.pnxa,
		   ygv608.regs.s.pnx );
	
	  logerror(
	       "\tR#02: $%02X : CPAW(%d),CPAR(%d),B/A(%d),SCAW(%d),SCAR(%d),SAAW(%d),SAAR(%d)\n",
		   ygv608.regs.b[2],
		   ygv608.regs.s.cpaw,
		   ygv608.regs.s.cpar,
		   ygv608.regs.s.p2_b_a,
		   ygv608.regs.s.scaw,
		   ygv608.regs.s.scar,
		   ygv608.regs.s.saaw,
		   ygv608.regs.s.saar );
	
	  logerror(
		   "\tR#03: $%02X : SAA($%02X)\n",
		   ygv608.regs.b[3],
		   ygv608.regs.s.saa );
	
	  logerror(
		   "\tR#04: $%02X : SCA($%02X)\n",
		   ygv608.regs.b[4],
		   ygv608.regs.s.sca );
	
	  logerror(
		   "\tR#05: $%02X : CC($%02X)\n",
		   ygv608.regs.b[5],
		   ygv608.regs.s.cc );
	
	  logerror(
		   "\tR#06: $%02X : SBA($%02X)\n",
		   ygv608.regs.b[6],
		   ygv608.regs.s.sba );
	
	  logerror(
		   "\tR#07: $%02X : DSPE(%d),MD(%d),ZRON(%d),FLIP(%d),DCKM(%d)\n",
		   ygv608.regs.b[7],
		   ygv608.regs.s.dspe,
		   ygv608.regs.s.md,
		   ygv608.regs.s.zron,
		   ygv608.regs.s.flip,
		   ygv608.regs.s.dckm );
	
	  logerror(
		   "\tR#08: $%02X : HDS(%d),VDS(%d),RLRT(%d),RLSC(%d),PGS(%d)\n",
		   ygv608.regs.b[8],
		   ygv608.regs.s.hds,
		   ygv608.regs.s.vds,
		   ygv608.regs.s.rlrt,
		   ygv608.regs.s.rlsc,
		   ygv608.regs.s.pgs );
	
	  logerror(
		   "\tR#11: $%02X : CTPA(%d),CTPB(%d),PRM(%d),CBDR(%d),YSE(%d),SCM(%d)\n",
		   ygv608.regs.b[11],
		   ygv608.regs.s.ctpa,
		   ygv608.regs.s.ctpb,
		   ygv608.regs.s.prm,
		   ygv608.regs.s.cbdr,
		   ygv608.regs.s.yse,
		   ygv608.regs.s.scm );
	
	  logerror(
		   "\tR#40: $%02X : HTL9:8($%02X)=$%06X,HDW(%d)\n",
		   ygv608.regs.b[40],
		   ygv608.regs.s.htl89, (int)ygv608.regs.s.htl89 << 8,
	       ygv608.regs.s.hdw );
	
	  logerror(
		   "\tR#41: $%02X : HDSP($%02X)\n",
		   ygv608.regs.b[41],
		   ygv608.regs.s.hdsp );
	
	  logerror(
		   "\tR#42: $%02X : HTL7:0($%02X)\n",
		   ygv608.regs.b[42],
		   ygv608.regs.s.htl );
	
	  logerror(
		   "\t              HTL=$%03X\n",
		   ( (int)ygv608.regs.s.htl89 << 8 ) |
		   ( (int)ygv608.regs.s.htl ) );
	
	  logerror(
		   "\tR#47: $%02X : TB12:5($%02X) = $%06X\n",
		   ygv608.regs.b[47],
		   ygv608.regs.s.tb5, (int)ygv608.regs.s.tb5 << 5 );
	
	  logerror(
		   "\tR#48: $%02X : TB20:13($%02X) = $%06X\n",
		   ygv608.regs.b[48],
		   ygv608.regs.s.tb13, (int)ygv608.regs.s.tb13 << 13 );
	
	  logerror(
		   "\t              TB=$%06X\n",
		   ( (int)ygv608.regs.s.tb13 << 13 ) |
		   ( (int)ygv608.regs.s.tb5 << 5 ) );
	
	  logerror(
		   "\tR#49: $%02X : TN11:4($%02X) = $%04X\n",
		   ygv608.regs.b[49],
		   ygv608.regs.s.tn4, (int)ygv608.regs.s.tn4 << 4 );
	
	  logerror(
		   "ShortCuts:\n" );
	
	  for( p=0; p<2; p++ ) {
	    logerror( "\t" );
	    for( b=0; b<8; b++ ) {
	      logerror( "%02X ", ygv608.base_addr[p][b] );
	    }
	    logerror( "\n" );
	  }
	}
	
	#endif  // MAME_DEBUG
}
