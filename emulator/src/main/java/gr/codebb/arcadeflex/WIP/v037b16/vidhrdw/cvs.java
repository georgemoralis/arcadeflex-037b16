/***************************************************************************

  vidhrdw\cvs.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.vidhrdw;

import common.ptr.UBytePtr;
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
import gr.codebb.arcadeflex.v037b16.mame.osdependH.osd_bitmap;
import static mame037b16.mame.Machine;
import static common.libc.cstring.*;
import static arcadeflex036.osdepend.logerror;
import common.ptr.IntPtr;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b16.mame.palette.*;
import static gr.codebb.arcadeflex.v037b16.mame.paletteH.*;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.s2636.*;
import static gr.codebb.arcadeflex.WIP.v037b16.cpu.s2650.s2650.*;
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static mame037b16.drawgfx.*;

public class cvs
{
	
	public static int MAX_STARS        = 250;
	public static int STARS_COLOR_BASE = 16;
	
	//#ifdef LSB_FIRST
	//public static int BL0 = 0;
	//public static int BL1 = 1;
	//public static int BL2 = 2;
	//public static int BL3 = 3;
	//public static int WL0 = 0;
	//public static int WL1 = 1;
	//#else
	public static int BL0 = 3;
	public static int BL1 = 2;
	public static int BL2 = 1;
	public static int BL3 = 0;
	public static int WL0 = 1;
	public static int WL1 = 0;
	//#endif
	
	public static class star
	{
		public int x,y,code;
	};
	
	static star[] stars=new star[MAX_STARS];
        static {
            for (int i = 0 ; i<MAX_STARS ; i++)
                stars[i]=new star();
        }
	static int    total_stars;
	static int[]    scroll= new int[8];
	static int    CollisionRegister=0;
	static int    stars_on=0;
	static int 	  character_mode=0;
	static int    character_page=0;
	static int    scroll_reg = 0;
	static int    stars_scroll=0;
	
	
	public static UBytePtr dirty_character=new UBytePtr();
	public static UBytePtr character_1_ram=new UBytePtr();
	public static UBytePtr character_2_ram=new UBytePtr();
	public static UBytePtr character_3_ram=new UBytePtr();
	public static UBytePtr bullet_ram=new UBytePtr();
	public static UBytePtr s2636_1_ram=new UBytePtr();
	public static UBytePtr s2636_2_ram=new UBytePtr();
	public static UBytePtr s2636_3_ram=new UBytePtr();
	
	public static osd_bitmap s2636_1_bitmap;
	public static osd_bitmap s2636_2_bitmap;
	public static osd_bitmap s2636_3_bitmap;
	public static osd_bitmap collision_bitmap;
	public static osd_bitmap collision_background;
	public static osd_bitmap scrolled_background;
	
	static char[] s2636_1_dirty=new char[4];
	static char[] s2636_2_dirty=new char[4];
	static char[] s2636_3_dirty=new char[4];
	
	static int ModeOffset[] = {223,191,255,127};
	
	/******************************************************
	 * Convert Colour prom to format for Mame Colour Map  *
	 *                                                    *
	 * There is a prom used for colour mapping and plane  *
	 * priority. This is converted to a colour table here *
	 *                                                    *
	 * colours are taken from SRAM and are programmable   *
	 ******************************************************/
        
        public static int TOTAL_COLORS(int gfxn){
            return (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity);
        }
        
	public static void COLOR(char[] colortable, int gfxn, int offs, int value){
            (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])=(char) value;
        }
	
	public static VhConvertColorPromPtr cvs_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int attr,col,map;
	
		
	
	    /* Colour Mapping Prom */
	
	    for(attr = 0;attr < 256; attr++)
	    {
	    	for(col = 0; col < 8; col++)
	        {
	          	map = color_prom.read((col * 256) + attr);
	
	            /* bits 1 and 3 are swopped over */
	
	            COLOR(colortable,0,attr*8 + col, ((map & 1) << 2) + (map & 2) + ((map & 4) >> 2));
	        }
	    }
	
	    /* Background Collision Map */
	
	    for(map=0;map<8;map++)
	    {
	    	COLOR(colortable,0,2048+map, (map & 4) >> 2);
	        COLOR(colortable,0,2056+map, (map & 2) >> 1);
	        //COLOR(colortable,0,2064+map, ((map & 2) >> 1) || ((map & 4) >> 2));
                COLOR(colortable,0,2064+map, ((map & 2) >> 1) | ((map & 4) >> 2));
	    }
	
	    /* Sprites */
	
	    for(map=0;map<8;map++)
	    {
	    	COLOR(colortable,0,map*2 + 2072, 0);
	    	COLOR(colortable,0,map*2 + 2073, 8 + map);
	    }
	
	    /* Initialise Dirty Character Array */
	
		memset(dirty_character, 0, 256);
	    memset(character_1_ram, 0, 1024);
	    memset(character_2_ram, 0, 1024);
	    memset(character_3_ram, 0, 1024);
	
	    /* Set Sprite chip offsets */
	
		s2636_x_offset = -26;
		s2636_y_offset = 3;
	
	    /* Set Scroll fixed areas */
	
	    scroll[0]=0;
	    scroll[6]=0;
	    scroll[7]=0;
	} };
	
	public static WriteHandlerPtr cvs_video_fx_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
/*TODO*///		logerror("%4x : Data Port = %2x\n",activecpu_get_pc(),data);
	
	    /* Unimplemented */
	
	    if((data & 2)!=0)   logerror("       SHADE BRIGHTER TO RIGHT\n");
	    if((data & 4)!=0)   logerror("       SCREEN ROTATE\n");
	    if((data & 8)!=0)   logerror("       SHADE BRIGHTER TO LEFT\n");
	    if((data & 64)!=0)  logerror("       SHADE BRIGHTER TO BOTTOM\n");
	    if((data & 128)!=0) logerror("       SHADE BRIGHTER TO TOP\n");
	
	    /* Implemented */
	
	    stars_on = data & 1;
/*TODO*///	    set_led_status(1,data & 16);	/* Lamp 1 */
/*TODO*///	    set_led_status(2,data & 32);	/* Lamp 2 */
	} };
	
	public static ReadHandlerPtr cvs_character_mode_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* Really a write - uses address info */
	
	    int value   = offset + 0x10;
	    int newmode = (value >> 4) & 3;
	
	    if(newmode != character_mode)
	    {
		    character_mode = newmode;
	        memset(dirtybuffer,1,videoram_size[0]);
	    }
	
	    character_page = (value << 2) & 0x300;
	
	    return 0;
	} };
	
	public static ReadHandlerPtr cvs_collision_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return CollisionRegister;
	} };
	
	public static ReadHandlerPtr cvs_collision_clear  = new ReadHandlerPtr() { public int handler(int offset)
	{
		CollisionRegister=0;
	    return 0;
	} };
	
	public static WriteHandlerPtr cvs_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		scroll_reg = 255 - data;
	
	    scroll[1]=scroll_reg;
	    scroll[2]=scroll_reg;
	    scroll[3]=scroll_reg;
	    scroll[4]=scroll_reg;
	    scroll[5]=scroll_reg;
	} };
	
	public static WriteHandlerPtr cvs_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
            if(s2650_get_flag()==0)
	    {
	    	// Colour Map
	
	        colorram_w.handler(offset,data);
	    }
	    else
	    {
	    	// Data
	
	        videoram_w.handler(offset,data);
	    }
	} };
	
	public static ReadHandlerPtr cvs_videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		if(s2650_get_flag()==0)
	    {
	    	// Colour Map
	
	        return colorram.read(offset);
	    }
	    else
	    {
	    	// Data
	
	        return videoram.read(offset);
	    }
	} };
	
	public static WriteHandlerPtr cvs_bullet_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if(s2650_get_flag()==0)
	    {
	    	// Bullet Ram
	
	        bullet_ram.write(offset, data);
	    }
	    else
	    {
	    	// Pallette Ram - Inverted ?
	
			paletteram_BBBGGGRR_w.handler((offset & 0x0f),(data ^ 0xff));
	    }
	} };
	
	public static ReadHandlerPtr cvs_bullet_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		if(s2650_get_flag()==0)
	    {
	    	// Bullet Ram
	
	        return bullet_ram.read(offset);
	    }
	    else
	    {
	    	// Pallette Ram
	
	        return (paletteram.read(offset)^ 0xff);
	    }
	} };
	
	public static WriteHandlerPtr cvs_2636_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if(s2650_get_flag()==0)
	    {
	    	// First 2636
	
	        s2636_w(s2636_1_ram,offset,data,new UBytePtr(s2636_1_dirty));
	    }
	    else
	    {
	    	// Character Ram 1
	
	        if(character_1_ram.read(character_page + offset) != data)
	        {
	        	character_1_ram.write(character_page + offset, data);
				dirty_character.write(128+((character_page + offset)>>3), 1);
	        }
		}
	} };
	
	public static ReadHandlerPtr cvs_2636_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		if(s2650_get_flag()==0)
	    {
	    	// First 2636
	
	        return s2636_1_ram.read(offset);
	    }
	    else
	    {
	    	// Character Ram 1
	
	        return character_1_ram.read(character_page + offset);
	    }
	} };
	
	public static WriteHandlerPtr cvs_2636_2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if(s2650_get_flag()==0)
	    {
	    	// Second 2636
	
	        s2636_w(s2636_2_ram,offset,data,new UBytePtr(s2636_2_dirty));
	    }
	    else
	    {
	    	// Character Ram 2
	
	        if(character_2_ram.read(character_page + offset) != data)
	        {
	        	character_2_ram.write(character_page + offset, data);
				dirty_character.write(128+((character_page + offset)>>3), 1);
	        }
	    }
	} };
	
	public static ReadHandlerPtr cvs_2636_2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		if(s2650_get_flag()==0)
	    {
	    	// Second 2636
	
	        return s2636_2_ram.read(offset);
	    }
	    else
	    {
	    	// Character Ram 2
	
	        return character_2_ram.read(character_page + offset);
	    }
	} };
	
	public static WriteHandlerPtr cvs_2636_3_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if(s2650_get_flag()==0)
	    {
	    	// Third 2636
	
	        s2636_w(s2636_3_ram,offset,data,new UBytePtr(s2636_3_dirty));
	    }
	    else
	    {
	    	// Character Ram 3
	
	        if(character_3_ram.read(character_page + offset) != data)
	        {
	        	character_3_ram.write(character_page + offset, data);
				dirty_character.write(128+((character_page + offset)>>3), 1);
	        }
	    }
	} };
	
	public static ReadHandlerPtr cvs_2636_3_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		if(s2650_get_flag()==0)
	    {
	    	// Third 2636
	
	        return s2636_3_ram.read(offset);
	    }
	    else
	    {
	    	// Character Ram 3
	
	        return character_3_ram.read(character_page + offset);
	    }
	} };
	
	public static VhStartPtr cvs_vh_start = new VhStartPtr() { public int handler() 
	{
		int generator = 0;
	    int x,y;
	
		generic_vh_start.handler();
	
		/* precalculate the star background */
	
		total_stars = 0;
	
		for (y = 255;y >= 0;y--)
		{
			for (x = 511;x >= 0;x--)
			{
				int bit1,bit2;
	
				generator <<= 1;
				bit1 = (~generator >> 17) & 1;
				bit2 = (generator >> 5) & 1;
	
				if ((bit1 ^ bit2)!=0) generator |= 1;
	
				if (((~generator >> 16) & 1)!=0 && (generator & 0xfe) == 0xfe)
				{
	            	if(((~(generator >> 12)) & 0x01)!=0 && ((~(generator >> 13)) & 0x01)!=0)
	                {
					    if (total_stars < MAX_STARS)
					    {
						    stars[total_stars].x = x;
						    stars[total_stars].y = y;
						    stars[total_stars].code = 1;
	
						    total_stars++;
					    }
	                }
				}
			}
		}
	
	    /* Need 3 bitmaps for 2636 chips */
	
		if ((s2636_1_bitmap = bitmap_alloc_depth(Machine.drv.screen_width,Machine.drv.screen_height,8)) == null)
		{
			bitmap_free(tmpbitmap);
			dirtybuffer = null;
			return 1;
		}
	
		if ((s2636_2_bitmap = bitmap_alloc_depth(Machine.drv.screen_width,Machine.drv.screen_height,8)) == null)
		{
			bitmap_free(s2636_1_bitmap);
			bitmap_free(tmpbitmap);
			dirtybuffer = null;
			return 1;
		}
	
		if ((s2636_3_bitmap = bitmap_alloc_depth(Machine.drv.screen_width,Machine.drv.screen_height,8)) == null)
		{
			bitmap_free(s2636_1_bitmap);
			bitmap_free(s2636_2_bitmap);
			bitmap_free(tmpbitmap);
			dirtybuffer = null;
			return 1;
		}
	
	    /* 3 bitmaps for collision detection */
	
		if ((collision_bitmap = bitmap_alloc_depth(Machine.drv.screen_width,Machine.drv.screen_height,8)) == null)
		{
			bitmap_free(s2636_1_bitmap);
			bitmap_free(s2636_2_bitmap);
			bitmap_free(s2636_3_bitmap);
			bitmap_free(tmpbitmap);
			dirtybuffer = null;
			return 1;
		}
	
		if ((collision_background = bitmap_alloc_depth(Machine.drv.screen_width,Machine.drv.screen_height,8)) == null)
		{
	    	bitmap_free(collision_bitmap);
			bitmap_free(s2636_1_bitmap);
			bitmap_free(s2636_2_bitmap);
			bitmap_free(s2636_3_bitmap);
			bitmap_free(tmpbitmap);
			dirtybuffer = null;
			return 1;
		}
	
		if ((scrolled_background = bitmap_alloc_depth(Machine.drv.screen_width,Machine.drv.screen_height,8)) == null)
		{
	    	bitmap_free(collision_background);
	    	bitmap_free(collision_bitmap);
			bitmap_free(s2636_1_bitmap);
			bitmap_free(s2636_2_bitmap);
			bitmap_free(s2636_3_bitmap);
			bitmap_free(tmpbitmap);
			dirtybuffer = null;
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopPtr cvs_vh_stop = new VhStopPtr() { public void handler() 
	{
		generic_vh_stop.handler();
		bitmap_free(s2636_1_bitmap);
		bitmap_free(s2636_2_bitmap);
		bitmap_free(s2636_3_bitmap);
		bitmap_free(collision_bitmap);
		bitmap_free(collision_background);
	    bitmap_free(scrolled_background);
	} };
	
	public static InterruptPtr cvs_interrupt = new InterruptPtr() { public int handler() 
	{
		stars_scroll++;
	
		cpu_irq_line_vector_w(0,0,0x03);
		cpu_set_irq_line(0,0,PULSE_LINE);
	
		return ignore_interrupt.handler();
	} };
	
	public static void plot_star(osd_bitmap bitmap, int x, int y)
	{
		if (flip_screen_x[0] != 0)
		{
			x = 255 - x;
		}
		if (flip_screen_y[0] != 0)
		{
			y = 255 - y;
		}
	
		if (read_pixel.handler(bitmap, x, y) == Machine.pens[0])
		{
			plot_pixel.handler(bitmap, x, y, Machine.pens[7]);
		}
	}
	
	public static VhUpdatePtr cvs_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
            //System.out.println("cvs_vh_screenrefresh!");
		int offs,character;
		int sx,sy;
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
	
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
	        character = videoram.read(offs);
	
			if(dirtybuffer[offs]!=0 || full_refresh!=0 || dirty_character.read(character)!=0)
			{
	            int character_bank;
	            int forecolor;
	
				dirtybuffer[offs] = 0;
	
				sx = (offs % 32) * 8;
				sy = (offs / 32) * 8;
	
	            /* Decide if RAM or ROM based character */
	
	            if(character > ModeOffset[character_mode])
	            {
	            	/* re-generate character if dirty */
	
	                if(dirty_character.read(character)==1)
	                {
	                	dirty_character.write(character,2);
			   	decodechar(Machine.gfx[1],character,new UBytePtr(character_1_ram, -1024),Machine.drv.gfxdecodeinfo[1].gfxlayout);
	                }
	
	            	character_bank=1;
	            }
	            else
	            {
	            	character_bank=0;
	            }
	
	            /* Main Screen */
	
	 			drawgfx(tmpbitmap,Machine.gfx[character_bank],
					    character,
						colorram.read(offs),
					    0,0,
					    sx,sy,
					    null,TRANSPARENCY_NONE,0);
	
	
	            /* Foreground for Collision Detection */
	
	            forecolor = 0;
	            if((colorram.read(offs)& 0x80) != 0)
	            {
					forecolor=258;
	            }
	            else
				{
					if((colorram.read(offs)& 0x03) == 3) forecolor=256;
	                else if((colorram.read(offs)& 0x01) == 0) forecolor=257;
	            }
	
	            if(forecolor != 0)
	 			    drawgfx(collision_background,Machine.gfx[character_bank],
					        character,
						    forecolor,
					        0,0,
					        sx,sy,
					        null,TRANSPARENCY_NONE,0);
			}
		}
	
	    /* Tidy up dirty character map */
	
	    for(offs=128;offs<256;offs++)
	    	if(dirty_character.read(offs)==2) dirty_character.write(offs,0);
	
	    /* Update screen - 8 regions, fixed scrolling area */
	
		copyscrollbitmap(bitmap,tmpbitmap,0,new int[]{0},8,scroll,Machine.visible_area,TRANSPARENCY_NONE,0);
		copyscrollbitmap(scrolled_background,collision_background,0,new int[]{0},8,scroll,Machine.visible_area,TRANSPARENCY_NONE,0);
	
	    /* 2636's */
	
		fillbitmap(s2636_1_bitmap,0,null);
		Update_Bitmap(s2636_1_bitmap,s2636_1_ram,new UBytePtr(s2636_1_dirty),2,collision_bitmap);
	
		fillbitmap(s2636_2_bitmap,0,null);
		Update_Bitmap(s2636_2_bitmap,s2636_2_ram,new UBytePtr(s2636_2_dirty),3,collision_bitmap);
	
		fillbitmap(s2636_3_bitmap,0,null);
		Update_Bitmap(s2636_3_bitmap,s2636_3_ram,new UBytePtr(s2636_3_dirty),4,collision_bitmap);
	
	    /* Bullet Hardware */
	
	    for (offs = 8; offs < 256; offs++ )
	    {
	        if(bullet_ram.read(offs) != 0)
	        {
	        	int ct;
	            for(ct=0;ct<4;ct++)
	            {
	            	int bx=255-7-bullet_ram.read(offs)-ct;
	
	            	/* Bullet/Object Collision */
	
	                if((CollisionRegister & 8) == 0)
	                {
	                    if ((read_pixel.handler(s2636_1_bitmap, bx, offs) != 0) ||
						    (read_pixel.handler(s2636_2_bitmap, bx, offs) != 0) ||
						    (read_pixel.handler(s2636_3_bitmap, bx, offs) != 0))
	                        CollisionRegister |= 8;
	                }
	
	            	/* Bullet/Background Collision */
	
	                if((CollisionRegister & 0x80) == 0)
	                {
						if (read_pixel.handler(scrolled_background, bx, offs) != Machine.pens[0])
	                    	CollisionRegister |= 0x80;
	                }
	
		            plot_pixel.handler(bitmap,bx,offs,Machine.pens[7]);
	            }
	        }
	    }
	
	    /* Update 2636 images */
	
		if (bitmap.depth == 16)
	    {
	        int S1,S2,S3,SB,pen;
	
	        for(sx=255;sx>7;sx--)
	        {
	        	IntPtr sp1 = new IntPtr(s2636_1_bitmap.line[sx]);
		    	IntPtr sp2 = new IntPtr(s2636_2_bitmap.line[sx]);
			IntPtr sp3 = new IntPtr(s2636_3_bitmap.line[sx]);
		        IntPtr dst = new IntPtr(bitmap.line[sx]);
			UBytePtr spb = new UBytePtr(scrolled_background.line[sx]);
	
	            for(offs=0;offs<62;offs++)
	            {
	        	 S1 = sp1.read();
                         sp1.inc();
	                 S2 = sp2.read();
                         sp2.inc();
	                 S3 = sp3.read();
                         sp3.inc();
	
	        	 pen = S1 | S2 | S3;
	
	                 if(pen!=0)
	                 {
	             	    IntPtr address = new IntPtr(dst,0);
                            if ((pen & 0xff000000)!=0) address.memory[BL3] = (char) Machine.pens[(pen >> 24) & 15];
                            if ((pen & 0x00ff0000)!=0) address.memory[BL2] = (char) Machine.pens[(pen >> 16) & 15];
                            if ((pen & 0x0000ff00)!=0) address.memory[BL1] = (char) Machine.pens[(pen >>  8) & 15];
                            if ((pen & 0x000000ff)!=0) address.memory[BL0] = (char) Machine.pens[(pen & 15)];

	                    /* Collision Detection */
	
	                    SB = 0;
                            if (spb.read(BL3) != Machine.pens[0]) SB =  0x08000000;
                            if (spb.read(BL2) != Machine.pens[0]) SB |= 0x00080000;
                            if (spb.read(BL1) != Machine.pens[0]) SB |= 0x00000800;
                            if (spb.read(BL0) != Machine.pens[0]) SB |= 0x00000008;
	
	       	            if ((S1 & S2)!=0) CollisionRegister |= 1;
	       	            if ((S2 & S3)!=0) CollisionRegister |= 2;
                            if ((S1 & S3)!=0) CollisionRegister |= 4;
	
	                    if (SB != 0)
	                    {
                                if ((S1 & SB)!=0) CollisionRegister |= 16;
                                if ((S2 & SB)!=0) CollisionRegister |= 32;
	       	                if ((S3 & SB)!=0) CollisionRegister |= 64;
	                    }
	                 }
	
	           	 dst.inc();
	                 spb.inc(4);
	            }
	        }
	    }
	    else
		{
	        for(sx=255;sx>7;sx--)
	        {
		        IntPtr sp1 = new IntPtr(s2636_1_bitmap.line[sx]);
		        IntPtr sp2 = new IntPtr(s2636_2_bitmap.line[sx]);
		        IntPtr sp3 = new IntPtr(s2636_3_bitmap.line[sx]);
                        IntPtr dst = new IntPtr(bitmap.line[sx]);
		        UBytePtr  spb = new UBytePtr(scrolled_background.line[sx]);
	
                        int S1,S2,S3,SB,pen;
	
	            for(offs=0;offs<62;offs++)
	            {
	        	 S1 = sp1.read();sp1.inc();
	                 S2 = sp2.read();sp2.inc();
	                 S3 = sp3.read();sp3.inc();
	
	        	     pen = S1 | S2 | S3;
	
	                 if(pen != 0)
	                 {
	             	    UBytePtr address = new UBytePtr(dst.readCA());
                            if ((pen & 0xff000000)!=0) address.write(BL3, Machine.pens[(pen >> 24) & 15]);
                            if ((pen & 0x00ff0000)!=0) address.write(BL2, Machine.pens[(pen >> 16) & 15]);
                            if ((pen & 0x0000ff00)!=0) address.write(BL1, Machine.pens[(pen >>  8) & 15]);
                            if ((pen & 0x000000ff)!=0) address.write(BL0, Machine.pens[(pen & 15)]);
	
	                    /* Collision Detection */
	
	                    SB = 0;
                            if (spb.read(BL3) != Machine.pens[0]) SB =  0x08000000;
                            if (spb.read(BL2) != Machine.pens[0]) SB |= 0x00080000;
                            if (spb.read(BL1) != Machine.pens[0]) SB |= 0x00000800;
                            if (spb.read(BL0) != Machine.pens[0]) SB |= 0x00000008;
	
	       	            if ((S1 & S2)!=0) CollisionRegister |= 1;
	       	            if ((S2 & S3)!=0) CollisionRegister |= 2;
                            if ((S1 & S3)!=0) CollisionRegister |= 4;
	
	                    if (SB!=0)
	                    {
	    			if ((S1 & SB)!=0) CollisionRegister |= 16;
	   			if ((S2 & SB)!=0) CollisionRegister |= 32;
	       	                if ((S3 & SB)!=0) CollisionRegister |= 64;
	                    }
                            dst.memory = address.memory;
	                 }
	
	           	 dst.inc();
	                 spb.inc(4);
	            }
	        }
	    }
	
	    /* Stars */
	
	    if(stars_on != 0)
	    {
			for (offs = 0;offs < total_stars;offs++)
			{
				int x,y;
	
	
				x = ((stars[offs].x + stars_scroll) % 512) / 2;
				y = (stars[offs].y + (stars_scroll + stars[offs].x) / 512) % 256;
	
				if (y >= Machine.visible_area.min_y &&
					y <= Machine.visible_area.max_y)
				{
					if (((y & 1) ^ ((x >> 4) & 1)) != 0)
					{
						plot_star(bitmap, x, y);
					}
				}
			}
	
	    }
	} };
}
