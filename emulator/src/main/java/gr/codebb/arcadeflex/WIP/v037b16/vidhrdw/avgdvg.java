/*
 * avgdvg.c: Atari DVG and AVG simulators
 *
 * Copyright 1991, 1992, 1996 Eric Smith
 *
 * Modified for the MAME project 1997 by
 * Brad Oliver, Bernd Wiebelt, Aaron Giles, Andrew Caldwell
 *
 * 971108 Disabled vector timing routines, introduced an ugly (but fast!)
 *        busy flag hack instead. BW
 * 980202 New anti aliasing code by Andrew Caldwell (.ac)
 * 980206 New (cleaner) busy flag handling.
 *        Moved LBO's buffered point into generic vector code. BW
 * 980212 Introduced timing code based on Aaron timer routines. BW
 * 980318 Better color handling, Bzone and MHavoc clipping. BW
 *
 * Battlezone uses a red overlay for the top of the screen and a green one
 * for the rest. There is a circuit to clip color 0 lines extending to the
 * red zone. This is emulated now. Thanks to Neil Bradley for the info. BW
 *
 * Frame and interrupt rates (Neil Bradley) BW
 * ~60 fps/4.0ms: Asteroid, Asteroid Deluxe
 * ~40 fps/4.0ms: Lunar Lander
 * ~40 fps/4.1ms: Battle Zone
 * ~45 fps/5.4ms: Space Duel, Red Baron
 * ~30 fps/5.4ms: StarWars
 *
 * Games with self adjusting framerate
 *
 * 4.1ms: Black Widow, Gravitar
 * 4.1ms: Tempest
 * Major Havoc
 * Quantum
 *
 * TODO: accurate vector timing (need timing diagramm)
 */

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.WIP.v037b16.vidhrdw;

import common.ptr.UBytePtr;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.vector.*;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.avgdvgH.*;
import static gr.codebb.arcadeflex.v037b16.generic.fucPtr.*;
import static gr.codebb.arcadeflex.WIP.v037b16.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v056.mame.timer.*;
import static gr.codebb.arcadeflex.v056.mame.timerH.*;
import static arcadeflex036.osdepend.logerror;
import static common.libc.cstdlib.rand;
import static gr.codebb.arcadeflex.v037b16.mame.common.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static java.lang.Math.abs;
import static mame037b16.mame.Machine;

public class avgdvg
{
	
	static int VEC_SHIFT = 16;	/* fixed for the moment */
	static int BRIGHTNESS = 12;   /* for maximum brightness, use 16! */
	
	
	/* the screen is red above this Y coordinate */
        static final int BZONE_TOP = 0x0050;
	
        static void BZONE_CLIP() {
		vector_add_clip (xmin<<VEC_SHIFT, BZONE_TOP<<VEC_SHIFT, 
						xmax<<VEC_SHIFT, ymax<<VEC_SHIFT);
        }
        
	static void BZONE_NOCLIP() {
		vector_add_clip (xmin<<VEC_SHIFT, ymin <<VEC_SHIFT, 
						xmax<<VEC_SHIFT, ymax<<VEC_SHIFT);
        }
	
	static final int MHAVOC_YWINDOW = 0x0048;
	static void MHAVOC_CLIP() {
		vector_add_clip (xmin<<VEC_SHIFT, MHAVOC_YWINDOW<<VEC_SHIFT, 
						xmax<<VEC_SHIFT, ymax<<VEC_SHIFT);
        }
        
	static void MHAVOC_NOCLIP() {
		vector_add_clip (xmin<<VEC_SHIFT, ymin <<VEC_SHIFT, 
						xmax<<VEC_SHIFT, ymax<<VEC_SHIFT);
        }

	static int vectorEngine = USE_DVG;
	static int flipword = 0; /* little/big endian issues */
	static int busy = 0;     /* vector engine busy? */
        static int[] colorram = new int[16]; /* colorram entries */
	
	/* These hold the X/Y coordinates the vector engine uses */
	static int width; static int height;
	static int xcenter; static int ycenter;
	static int xmin; static int xmax;
	static int ymin; static int ymax;
	
	
	static int vector_updates; /* avgdvg_go_w()'s per Mame frame, should be 1 */
	
	static int vg_step = 0;    /* single step the vector generator */
	static int total_length;   /* length of all lines drawn in a frame */
	
	public static final int MAXSTACK = 8; 	/* Tempest needs more than 4     BW 210797 */
	
	/* AVG commands */
	public static final int VCTR = 0;
	public static final int HALT = 1;
	public static final int SVEC = 2;
	public static final int STAT = 3;
	public static final int CNTR = 4;
	public static final int JSRL = 5;
	public static final int RTSL = 6;
	public static final int JMPL = 7;
	public static final int SCAL = 8;
	
	/* DVG commands */
        static final int DVCTR = 0x01;
	static final int DLABS = 0x0a;
        static final int DHALT = 0x0b;
        static final int DJSRL = 0x0c;
        static final int DRTSL = 0x0d;
        static final int DJMPL = 0x0e;
        static final int DSVEC = 0x0f;

	static int twos_comp_val(int num, int bits){ return ((num&(1<<(bits-1)))!=0?(num|~((1<<bits)-1)):(num&((1<<bits)-1))); }
/*TODO*///	
/*TODO*///	char *avg_mnem[] = { "vctr", "halt", "svec", "stat", "cntr",
/*TODO*///				 "jsrl", "rtsl", "jmpl", "scal" };
/*TODO*///	
/*TODO*///	char *dvg_mnem[] = { "????", "vct1", "vct2", "vct3",
/*TODO*///			     "vct4", "vct5", "vct6", "vct7",
/*TODO*///			     "vct8", "vct9", "labs", "halt",
/*TODO*///			     "jsrl", "rtsl", "jmpl", "svec" };
	
	/* ASG 971210 -- added banks and modified the read macros to use them */
	static int BANK_BITS = 13;
	static int BANK_SIZE(){ return (1<<BANK_BITS); }
	static int NUM_BANKS(){ return (0x4000/BANK_SIZE()); }
        static int  VECTORRAM(int offset){ return (vectorbank[(offset)>>BANK_BITS].read((offset)&(BANK_SIZE()-1))); }
        static UBytePtr[] vectorbank=new UBytePtr[NUM_BANKS()];
	
	static int  map_addr(int n){ return (((n)<<1)); }
        static int  memrdwd(int offset){ return (VECTORRAM(offset) | (VECTORRAM(offset+1)<<8)); }
	/* The AVG used by Star Wars reads the bytes in the opposite order */
	static int  memrdwd_flip(int offset){ return (VECTORRAM(offset+1) | (VECTORRAM(offset)<<8)); }
	
	
	static void vector_timer (int deltax, int deltay)
	{
		deltax = abs (deltax);
		deltay = abs (deltay);
		if (deltax > deltay)
			total_length += deltax >> VEC_SHIFT;
		else
			total_length += deltay >> VEC_SHIFT;
	}
	
	static void dvg_vector_timer (int scale)
	{
		total_length += scale;
	}
	
	static void dvg_generate_vector_list()
	{
		int pc;
		int sp;
		int[] stack = new int[MAXSTACK];
	
		int scale;
		int statz;
	
		int currentx, currenty;
	
		int done = 0;
	
		int firstwd;
		int secondwd = 0; /* Initialize to tease the compiler */
		int opcode;
	
		int x, y;
		int z, temp;
		int a;
	
		int deltax, deltay;
	
		vector_clear_list();
		pc = 0;
		sp = 0;
		scale = 0;
		statz = 0;
	
		currentx = 0;
		currenty = 0;
	
		while (done==0)
		{
	
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///			if (vg_step != 0)
/*TODO*///			{
/*TODO*///		  		logerror("Current beam position: (%d, %d)\n",
/*TODO*///					currentx, currenty);
/*TODO*///		  		getchar();
/*TODO*///			}
/*TODO*///	#endif
	
			firstwd = memrdwd (map_addr (pc));
			opcode = firstwd >> 12;
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///			logerror("%4x: %4x ", map_addr (pc), firstwd);
/*TODO*///	#endif
			pc++;
			if ((opcode >= 0 /* DVCTR */) && (opcode <= DLABS))
			{
				secondwd = memrdwd (map_addr (pc));
				pc++;
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///				logerror("%s ", dvg_mnem [opcode]);
/*TODO*///				logerror("%4x  ", secondwd);
/*TODO*///	#endif
			}
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///			else logerror("Illegal opcode ");
/*TODO*///	#endif
	
			switch (opcode)
			{
				case 0:
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///		 			logerror("Error: DVG opcode 0!  Addr %4x Instr %4x %4x\n", map_addr (pc-2), firstwd, secondwd);
/*TODO*///					done = 1;
/*TODO*///					break;
/*TODO*///	#endif
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
				case 8:
				case 9:
		  			y = firstwd & 0x03ff;
					if ((firstwd & 0x400) != 0)
						y=-y;
					x = secondwd & 0x3ff;
					if ((secondwd & 0x400) != 0)
						x=-x;
					z = secondwd >> 12;
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///					logerror("(%d,%d) z: %d scal: %d", x, y, z, opcode);
/*TODO*///	#endif
		  			temp = ((scale + opcode) & 0x0f);
		  			if (temp > 9)
						temp = -1;
		  			deltax = (x << VEC_SHIFT) >> (9-temp);		/* ASG 080497 */
					deltay = (y << VEC_SHIFT) >> (9-temp);		/* ASG 080497 */
		  			currentx += deltax;
					currenty -= deltay;
					dvg_vector_timer(temp);
	
					/* ASG 080497, .ac JAN2498 - V.V */
					if (translucency != 0)
						z = z * BRIGHTNESS;
					else
						if (z != 0) z = (z << 4) | 0x0f;
					vector_add_point (currentx, currenty, colorram[1], z);
	
					break;
	
				case DLABS:
					x = twos_comp_val (secondwd, 12);
					y = twos_comp_val (firstwd, 12);
		  			scale = (secondwd >> 12);
					currentx = ((x-xmin) << VEC_SHIFT);		/* ASG 080497 */
					currenty = ((ymax-y) << VEC_SHIFT);		/* ASG 080497 */
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///					logerror("(%d,%d) scal: %d", x, y, secondwd >> 12);
/*TODO*///	#endif
					break;
	
				case DHALT:
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///					if ((firstwd & 0x0fff) != 0)
/*TODO*///	      				logerror("(%d?)", firstwd & 0x0fff);
/*TODO*///	#endif
					done = 1;
					break;
	
				case DJSRL:
					a = firstwd & 0x0fff;
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///					logerror("%4x", map_addr(a));
/*TODO*///	#endif
					stack [sp] = pc;
					if (sp == (MAXSTACK - 1))
		    			{
						logerror("\n*** Vector generator stack overflow! ***\n");
						done = 1;
						sp = 0;
					}
					else
						sp++;
					pc = a;
					break;
	
				case DRTSL:
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///					if ((firstwd & 0x0fff) != 0)
/*TODO*///						 logerror("(%d?)", firstwd & 0x0fff);
/*TODO*///	#endif
					if (sp == 0)
		    			{
						logerror("\n*** Vector generator stack underflow! ***\n");
						done = 1;
						sp = MAXSTACK - 1;
					}
					else
						sp--;
					pc = stack [sp];
					break;
	
				case DJMPL:
					a = firstwd & 0x0fff;
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///					logerror("%4x", map_addr(a));
/*TODO*///	#endif
					pc = a;
					break;
	
				case DSVEC:
					y = firstwd & 0x0300;
					if ((firstwd & 0x0400) != 0)
						y = -y;
					x = (firstwd & 0x03) << 8;
					if ((firstwd & 0x04) != 0)
						x = -x;
					z = (firstwd >> 4) & 0x0f;
					temp = 2 + ((firstwd >> 2) & 0x02) + ((firstwd >>11) & 0x01);
		  			temp = ((scale + temp) & 0x0f);
					if (temp > 9)
						temp = -1;
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///					logerror("(%d,%d) z: %d scal: %d", x, y, z, temp);
/*TODO*///	#endif
	
					deltax = (x << VEC_SHIFT) >> (9-temp);	/* ASG 080497 */
					deltay = (y << VEC_SHIFT) >> (9-temp);	/* ASG 080497 */
		  			currentx += deltax;
					currenty -= deltay;
					dvg_vector_timer(temp);
	
					/* ASG 080497, .ac JAN2498 */
					if (translucency != 0)
						z = z * BRIGHTNESS;
					else
						if (z != 0) z = (z << 4) | 0x0f;
					vector_add_point (currentx, currenty, colorram[1], z);
					break;
	
				default:
					logerror("Unknown DVG opcode found\n");
					done = 1;
			}
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///	      		logerror("\n");
/*TODO*///	#endif
		}
	}
	
	/*
	Atari Analog Vector Generator Instruction Set
	
	Compiled from Atari schematics and specifications
	Eric Smith  7/2/92
	---------------------------------------------
	
	NOTE: The vector generator is little-endian.  The instructions are 16 bit
	      words, which need to be stored with the least significant byte in the
	      lower (even) address.  They are shown here with the MSB on the left.
	
	The stack allows four levels of subroutine calls in the TTL version, but only
	three levels in the gate array version.
	
	inst  bit pattern          description
	----  -------------------  -------------------
	VCTR  000- yyyy yyyy yyyy  normal vector
	      zzz- xxxx xxxx xxxx
	HALT  001- ---- ---- ----  halt - does CNTR also on newer hardware
	SVEC  010y yyyy zzzx xxxx  short vector - don't use zero length
	STAT  0110 ---- zzzz cccc  status
	SCAL  0111 -bbb llll llll  scaling
	CNTR  100- ---- dddd dddd  center
	JSRL  101a aaaa aaaa aaaa  jump to subroutine
	RTSL  110- ---- ---- ----  return
	JMPL  111a aaaa aaaa aaaa  jump
	
	-     unused bits
	x, y  relative x and y coordinates in two's complement (5 or 13 bit,
	      5 bit quantities are scaled by 2, so x=1 is really a length 2 vector.
	z     intensity, 0 = blank, 1 means use z from STAT instruction,  2-7 are
	      doubled for actual range of 4-14
	c     color
	b     binary scaling, multiplies all lengths by 2**(1-b), 0 is double size,
	      1 is normal, 2 is half, 3 is 1/4, etc.
	l     linear scaling, multiplies all lengths by 1-l/256, don't exceed $80
	d     delay time, use $40
	a     address (word address relative to base of vector memory)
	
	Notes:
	
	Quantum:
	        the VCTR instruction has a four bit Z field, that is not
	        doubled.  The value 2 means use Z from STAT instruction.
	
	        the SVEC instruction can't be used
	
	Major Havoc:
	        SCAL bit 11 is used for setting a Y axis window.
	
	        STAT bit 11 is used to enable "sparkle" color.
	        STAT bit 10 inverts the X axis of vectors.
	        STAT bits 9 and 8 are the Vector ROM bank select.
	
	Star Wars:
	        STAT bits 10, 9, and 8 are used directly for R, G, and B.
	*/
	
	static void avg_generate_vector_list ()
	{
	
		int pc;
		int sp;
		int[] stack = new int[MAXSTACK];
	
		int scale;
		int statz   = 0;
		int sparkle = 0;
		int xflip   = 0;
	
		int color   = 0;
		int bz_col  = -1; /* Battle Zone color selection */
		int ywindow = -1; /* Major Havoc Y-Window */
	
		int currentx, currenty;
		int done    = 0;
	
		int firstwd, secondwd;
		int opcode;
	
		int x, y, z=0, b, l, d, a;
	
		int deltax, deltay;
	
	
		pc = 0;
		sp = 0;
		statz = 0;
		color = 0;
	
		if (flipword != 0)
		{
			firstwd = memrdwd_flip (map_addr (pc));
			secondwd = memrdwd_flip (map_addr (pc+1));
		}
		else
		{
			firstwd = memrdwd (map_addr (pc));
			secondwd = memrdwd (map_addr (pc+1));
		}
		if ((firstwd == 0) && (secondwd == 0))
		{
			logerror("VGO with zeroed vector memory\n");
			return;
		}
	
		/* kludge to bypass Major Havoc's empty frames. BW 980216 */
		if (vectorEngine == USE_AVG_MHAVOC && firstwd == 0xafe2)
			return;
	
		scale = 0;          /* ASG 080497 */
		currentx = xcenter; /* ASG 080497 */ /*.ac JAN2498 */
		currenty = ycenter; /* ASG 080497 */ /*.ac JAN2498 */
	
		vector_clear_list();
	
		while (done==0)
		{
	
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///			if (vg_step != 0) getchar();
/*TODO*///	#endif
	
			if (flipword != 0) firstwd = memrdwd_flip (map_addr (pc));
			else          firstwd = memrdwd      (map_addr (pc));
	
			opcode = firstwd >> 13;
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///			logerror("%4x: %4x ", map_addr (pc), firstwd);
/*TODO*///	#endif
			pc++;
			if (opcode == VCTR)
			{
				if (flipword != 0) secondwd = memrdwd_flip (map_addr (pc));
				else          secondwd = memrdwd      (map_addr (pc));
				pc++;
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///				logerror("%4x  ", secondwd);
/*TODO*///	#endif
			}
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///			else logerror("      ");
/*TODO*///	#endif
	
			if ((opcode == STAT) && ((firstwd & 0x1000) != 0))
				opcode = SCAL;
	
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///			logerror("%s ", avg_mnem [opcode]);
/*TODO*///	#endif
	
			switch (opcode)
			{
				case VCTR:
	
					if (vectorEngine == USE_AVG_QUANTUM)
					{
						x = twos_comp_val (secondwd, 12);
						y = twos_comp_val (firstwd, 12);
					}
					else
					{
						/* These work for all other games. */
						x = twos_comp_val (secondwd, 13);
						y = twos_comp_val (firstwd, 13);
					}
					z = (secondwd >> 12) & ~0x01;
	
					/* z is the maximum DAC output, and      */
					/* the 8 bit value from STAT does some   */
					/* fine tuning. STATs of 128 should give */
					/* highest intensity. */
					if (vectorEngine == USE_AVG_SWARS)
					{
						if (translucency != 0)
							z = (statz * z) / 12;
						else
							z = (statz * z) >> 3;
						if (z > 0xff)
							z = 0xff;
					}
					else
					{
						if (z == 2)
							z = statz;
							if (translucency != 0)
								z = z * BRIGHTNESS;
							else
								if (z != 0) z = (z << 4) | 0x1f;
					}
	
					deltax = x * scale;
					if (xflip != 0) deltax = -deltax;
	
					deltay = y * scale;
					currentx += deltax;
					currenty -= deltay;
					vector_timer(deltax, deltay);
	
					if (sparkle != 0)
					{
						color = rand() & 0x07;
					}
	
					if ((vectorEngine == USE_AVG_BZONE) && (bz_col != 0))
					{
						if (currenty < (BZONE_TOP<<16))
							color = 4;
						else
							color = 2;
					}
	
					vector_add_point (currentx, currenty, colorram[color], z);
	
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///					logerror("VCTR x:%d y:%d z:%d statz:%d", x, y, z, statz);
/*TODO*///	#endif
					break;
	
				case SVEC:
					x = twos_comp_val (firstwd, 5) << 1;
					y = twos_comp_val (firstwd >> 8, 5) << 1;
					z = ((firstwd >> 4) & 0x0e);
	
					if (vectorEngine == USE_AVG_SWARS)
					{
						if (translucency != 0)
							z = (statz * z) / 12;
						else
							z = (statz * z) >> 3;
						if (z > 0xff) z = 0xff;
					}
					else
					{
						if (z == 2)
							z = statz;
							if (translucency != 0)
								z = z * BRIGHTNESS;
							else
								if (z != 0) z = (z << 4) | 0x1f;
					}
	
					deltax = x * scale;
					if (xflip != 0) deltax = -deltax;
	
					deltay = y * scale;
					currentx += deltax;
					currenty -= deltay;
					vector_timer(deltax, deltay);
	
					if (sparkle != 0)
					{
						color = rand() & 0x07;
					}
	
					vector_add_point (currentx, currenty, colorram[color], z);
	
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///					logerror("SVEC x:%d y:%d z:%d statz:%d", x, y, z, statz);
/*TODO*///	#endif
					break;
	
				case STAT:
					if (vectorEngine == USE_AVG_SWARS)
					{
						/* color code 0-7 stored in top 3 bits of `color' */
						color=(char)((firstwd & 0x0700)>>8);
						statz = (firstwd) & 0xff;
					}
					else
					{
						color = (firstwd) & 0x000f;
						statz = (firstwd >> 4) & 0x000f;
						if (vectorEngine == USE_AVG_TEMPEST)
									sparkle = (firstwd & 0x0800)!=0?0:1;
						if (vectorEngine == USE_AVG_MHAVOC)
						{
							sparkle = (firstwd & 0x0800);
							xflip = firstwd & 0x0400;
							/* Bank switch the vector ROM for Major Havoc */
							vectorbank[1] = new UBytePtr(memory_region(REGION_CPU1), 0x18000 + ((firstwd & 0x300) >> 8) * 0x2000);
						}
						if (vectorEngine == USE_AVG_BZONE)
						{
							bz_col = color;
							if (color == 0)
							{
								BZONE_CLIP();
								color = 2;
							}
							else
							{
								BZONE_NOCLIP();
							}
						}
					}
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///					logerror("STAT: statz: %d color: %d", statz, color);
/*TODO*///					if (xflip || sparkle)
/*TODO*///						logerror("xflip: %02x  sparkle: %02x\n", xflip, sparkle);
/*TODO*///	#endif
	
					break;
	
				case SCAL:
					b = ((firstwd >> 8) & 0x07)+8;
					l = (~firstwd) & 0xff;
					scale = (l << VEC_SHIFT) >> b;		/* ASG 080497 */
	
					/* Y-Window toggle for Major Havoc BW 980318 */
					if (vectorEngine == USE_AVG_MHAVOC)
					{
						if ((firstwd & 0x0800) != 0)
						{
							logerror("CLIP %d\n", firstwd & 0x0800);
							if (ywindow == 0)
							{
								ywindow = 1;
								MHAVOC_CLIP();
							}
							else
							{
								ywindow = 0;
								MHAVOC_NOCLIP();
							}
						}
					}
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///					logerror("bin: %d, lin: ", b);
/*TODO*///					if (l > 0x80)
/*TODO*///						logerror("(%d?)", l);
/*TODO*///					else
/*TODO*///						logerror("%d", l);
/*TODO*///					logerror(" scale: %f", (scale/(float)(1<<VEC_SHIFT)));
/*TODO*///	#endif
					break;
	
				case CNTR:
					d = firstwd & 0xff;
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///					if (d != 0x40) logerror("%d", d);
/*TODO*///	#endif
					currentx = xcenter ;  /* ASG 080497 */ /*.ac JAN2498 */
					currenty = ycenter ;  /* ASG 080497 */ /*.ac JAN2498 */
					vector_add_point (currentx, currenty, 0, 0);
					break;
	
				case RTSL:
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///					if ((firstwd & 0x1fff) != 0)
/*TODO*///						logerror("(%d?)", firstwd & 0x1fff);
/*TODO*///	#endif
					if (sp == 0)
					{
						logerror("\n*** Vector generator stack underflow! ***\n");
						done = 1;
						sp = MAXSTACK - 1;
					}
					else
						sp--;
	
					pc = stack [sp];
					break;
	
				case HALT:
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///					if ((firstwd & 0x1fff) != 0)
/*TODO*///						logerror("(%d?)", firstwd & 0x1fff);
/*TODO*///	#endif
					done = 1;
					break;
	
				case JMPL:
					a = firstwd & 0x1fff;
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///					logerror("%4x", map_addr(a));
/*TODO*///	#endif
					/* if a = 0x0000, treat as HALT */
					if (a == 0x0000)
						done = 1;
					else
						pc = a;
					break;
	
				case JSRL:
					a = firstwd & 0x1fff;
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///					logerror("%4x", map_addr(a));
/*TODO*///	#endif
					/* if a = 0x0000, treat as HALT */
					if (a == 0x0000)
						done = 1;
					else
					{
						stack [sp] = pc;
						if (sp == (MAXSTACK - 1))
						{
							logerror("\n*** Vector generator stack overflow! ***\n");
							done = 1;
							sp = 0;
						}
						else
							sp++;
	
						pc = a;
					}
					break;
	
				default:
					logerror("internal error\n");
			}
/*TODO*///	#ifdef VG_DEBUG
/*TODO*///			logerror("\n");
/*TODO*///	#endif
		}
	}
	
	
	public static int avgdvg_done ()
	{
		if (busy != 0)
			return 0;
		else
			return 1;
	}
	
	static timer_callback avgdvg_clr_busy = new timer_callback() {
            @Override
            public void handler(int dummy) {
                busy = 0;
            }
        };
        
	public static WriteHandlerPtr avgdvg_go_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (busy != 0)
			return;
	
		vector_updates++;
		total_length = 1;
		busy = 1;
	
		if (vectorEngine == USE_DVG)
		{
			dvg_generate_vector_list();
			timer_set (TIME_IN_NSEC(4500) * total_length, 1, avgdvg_clr_busy);
		}
		else
		{
			avg_generate_vector_list();
			if (total_length > 1)
				timer_set (TIME_IN_NSEC(1500) * total_length, 1, avgdvg_clr_busy);
			/* this is for Major Havoc */
			else
			{
				vector_updates--;
				busy = 0;
			}
		}
	} };
	
/*TODO*///	WRITE16_HANDLER( avgdvg_go_word_w )
/*TODO*///	{
/*TODO*///		avgdvg_go_w(offset, data);
/*TODO*///	}
	
	public static WriteHandlerPtr avgdvg_reset_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		avgdvg_clr_busy.handler(0);
	} };
	
/*TODO*///	WRITE16_HANDLER( avgdvg_reset_word_w )
/*TODO*///	{
/*TODO*///		avgdvg_clr_busy(0);
/*TODO*///	}
	
	public static int avgdvg_init (int vgType)
	{
		int i;
	
		if (vectorram_size == null)
		{
			logerror("Error: vectorram_size not initialized\n");
			return 1;
		}
	
		/* ASG 971210 -- initialize the pages */
		for (i = 0; i < NUM_BANKS(); i++)
			vectorbank[i] = new UBytePtr(vectorram, (i<<BANK_BITS));
		if (vgType == USE_AVG_MHAVOC)
			vectorbank[1] = new UBytePtr(memory_region(REGION_CPU1), 0x18000);
	
		vectorEngine = vgType;
		if ((vectorEngine<AVGDVG_MIN) || (vectorEngine>AVGDVG_MAX))
		{
			logerror("Error: unknown Atari Vector Game Type\n");
			return 1;
		}
	
		if (vectorEngine==USE_AVG_SWARS)
			flipword=1;
/*TODO*///	#ifndef LSB_FIRST
/*TODO*///		else if (vectorEngine==USE_AVG_QUANTUM)
/*TODO*///			flipword=1;
/*TODO*///	#endif
		else
			flipword=0;
	
		vg_step = 0;
	
		busy = 0;
	
		xmin=Machine.visible_area.min_x;
		ymin=Machine.visible_area.min_y;
		xmax=Machine.visible_area.max_x;
		ymax=Machine.visible_area.max_y;
		width=xmax-xmin;
		height=ymax-ymin;
	
		xcenter=((xmax+xmin)/2) << VEC_SHIFT; /*.ac JAN2498 */
		ycenter=((ymax+ymin)/2) << VEC_SHIFT; /*.ac JAN2498 */
	
		vector_set_shift (VEC_SHIFT);
	
		if (vector_vh_start.handler() != 0)
			return 1;
	
		return 0;
	}
	
	/*
	 * These functions initialise the colors for all atari games.
	 */
	
	static int RED   = 0x04;
	static int GREEN = 0x02;
	static int BLUE  = 0x01;
	static int WHITE = RED|GREEN|BLUE;
	
	static void shade_fill (char[] palette, int rgb, int start_index, int end_index, int start_inten, int end_inten)
	{
		int i, inten, index_range, inten_range;
	
		index_range = end_index-start_index;
		inten_range = end_inten-start_inten;
		for (i = start_index; i <= end_index; i++)
		{
			inten = start_inten + (inten_range) * (i-start_index) / (index_range);
			palette[3*i  ] = (char) ((rgb & RED  )!=0? inten : 0);
			palette[3*i+1] = (char) ((rgb & GREEN)!=0? inten : 0);
			palette[3*i+2] = (char) ((rgb & BLUE )!=0? inten : 0);
		}
	}

        public static final int VEC_PAL_WHITE       = 1;
        public static final int VEC_PAL_AQUA        = 2;
        public static final int VEC_PAL_BZONE       = 3;
        public static final int VEC_PAL_MULTI       = 4;
	public static final int VEC_PAL_SWARS       = 5;
        public static final int VEC_PAL_ASTDELUX    = 6;
	
	/* Helper function to construct the color palette for the Atari vector
	 * games. DO NOT reference this function from the Gamedriver or
	 * MachineDriver. Use "avg_init_palette_XXXXX" instead. */
	static void avg_init_palette (int paltype, char[] palette, char[] colortable, UBytePtr color_prom)
	{
		int i,j,k;
	
		int trcl1[] = { 0,0,2,2,1,1 };
		int trcl2[] = { 1,2,0,1,0,2 };
		int trcl3[] = { 2,1,1,0,2,0 };
	
		/* initialize the first 8 colors with the basic colors */
		/* Only these are selected by writes to the colorram. */
		for (i = 0; i < 8; i++)
		{
			palette[3*i  ] = (char) ((i & RED  )!=0 ? 0xff : 0);
			palette[3*i+1] = (char) ((i & GREEN)!=0 ? 0xff : 0);
			palette[3*i+2] = (char) ((i & BLUE )!=0 ? 0xff : 0);
		}
	
		/* initialize the colorram */
		for (i = 0; i < 16; i++)
			colorram[i] =i & 0x07;

		/* fill the rest of the 256 color entries depending on the game */
		switch (paltype)
		{
			/* Black and White vector colors (Asteroids,Omega Race) .ac JAN2498 */
			case  VEC_PAL_WHITE:
				shade_fill (palette, RED|GREEN|BLUE, 8, 128+8, 0, 255);
				colorram[1] =7; /* BW games use only color 1 (== white) */
				break;
	
			/* Monochrome Aqua colors (Asteroids Deluxe,Red Baron) .ac JAN2498 */
			case  VEC_PAL_ASTDELUX:
				/* Use backdrop if present MLR OCT0598 */
/*TODO*///				backdrop_load("astdelux.png", 32, Machine.drv.total_colors-32);
/*TODO*///				if (artwork_backdrop!=null)
/*TODO*///				{
/*TODO*///					shade_fill (palette, GREEN|BLUE, 8, 23, 1, 254);
/*TODO*///					/* Some more anti-aliasing colors. */
/*TODO*///					shade_fill (palette, GREEN|BLUE, 24, 31, 1, 254);
/*TODO*///					for (i=0; i<8; i++)
/*TODO*///						palette[(24+i)*3]=80;
/*TODO*///					memcpy (palette+3*artwork_backdrop.start_pen, artwork_backdrop.orig_palette,
/*TODO*///						3*artwork_backdrop.num_pens_used);
/*TODO*///				}
/*TODO*///				else
					shade_fill (palette, GREEN|BLUE, 8, 128+8, 1, 254);
				colorram[1] =3; /* for Asteroids */
				break;
	
			case  VEC_PAL_AQUA:
				shade_fill (palette, GREEN|BLUE, 8, 128+8, 1, 254);
				colorram[0] =3; /* for Red Baron */
				break;
	
			/* Monochrome Green/Red vector colors (Battlezone) .ac JAN2498 */
			case  VEC_PAL_BZONE:
				shade_fill (palette, RED  ,  8, 23, 1, 254);
				shade_fill (palette, GREEN, 24, 31, 1, 254);
				shade_fill (palette, WHITE, 32, 47, 1, 254);
				/* Use backdrop if present MLR OCT0598 */
/*TODO*///				backdrop_load("bzone.png", 48, Machine.drv.total_colors-48);
/*TODO*///				if (artwork_backdrop!=null)
/*TODO*///					memcpy (palette+3*artwork_backdrop.start_pen, artwork_backdrop.orig_palette, 3*artwork_backdrop.num_pens_used);
				break;
	
			/* Colored games (Major Havoc, Star Wars, Tempest) .ac JAN2498 */
			case  VEC_PAL_MULTI:
			case  VEC_PAL_SWARS:
				/* put in 40 shades for red, blue and magenta */
				shade_fill (palette, RED       ,   8,  47, 10, 250);
				shade_fill (palette, BLUE      ,  48,  87, 10, 250);
				shade_fill (palette, RED|BLUE  ,  88, 127, 10, 250);
	
				/* put in 20 shades for yellow and green */
				shade_fill (palette, GREEN     , 128, 147, 10, 250);
				shade_fill (palette, RED|GREEN , 148, 167, 10, 250);
	
				/* and 14 shades for cyan and white */
				shade_fill (palette, BLUE|GREEN, 168, 181, 10, 250);
				shade_fill (palette, WHITE     , 182, 194, 10, 250);
	
				/* Fill in unused gaps with more anti-aliasing colors. */
				/* There are 60 slots available.           .ac JAN2498 */
				i=195;
				for (j=0; j<6; j++)
				{
					for (k=7; k<=16; k++)
					{
						palette[3*i+trcl1[j]] = (char) (((256*k)/16)-1);
						palette[3*i+trcl2[j]] = (char) (((128*k)/16)-1);
						palette[3*i+trcl3[j]] = 0;
						i++;
					}
				}
				break;
			default:
				logerror("Wrong palette type in avgdvg.c");
				break;
		}
	}
	
/*TODO*///	/* A macro for the palette_init functions */
/*TODO*///	#define VEC_PAL_INIT(name, paltype) \
/*TODO*///	public static VhConvertColorPromPtr avg_init_palette_##name = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom)  \
/*TODO*///	{ avg_init_palette (paltype, palette, colortable, color_prom); } };
/*TODO*///	
/*TODO*///	/* The functions referenced from gamedriver */
/*TODO*///	VEC_PAL_INIT(white,    VEC_PAL_WHITE)
/*TODO*///	VEC_PAL_INIT(aqua ,    VEC_PAL_AQUA )
/*TODO*///	VEC_PAL_INIT(bzone,    VEC_PAL_BZONE)
/*TODO*///	VEC_PAL_INIT(multi,    VEC_PAL_MULTI)
/*TODO*///	VEC_PAL_INIT(swars,    VEC_PAL_SWARS)
        	
	/* A macro for the palette_init functions */
	//#define VEC_PAL_INIT(name, paltype) \
	public static VhConvertColorPromPtr avg_init_palette_swars = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom)
	{ avg_init_palette (VEC_PAL_SWARS, palette, colortable, color_prom); } };
	
/*TODO*///	VEC_PAL_INIT(astdelux, VEC_PAL_ASTDELUX )
/*TODO*///	
/*TODO*///	
/*TODO*///	/* If you want to use the next two functions, please make sure that you have
/*TODO*///	 * a fake GfxLayout, otherwise you'll crash */
/*TODO*///	public static WriteHandlerPtr colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		colorram.write(offset & 0x0f,data & 0x0f);
/*TODO*///} };
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 * Tempest, Major Havoc and Quantum select colors via a 16 byte colorram.
/*TODO*///	 * What's more, they have a different ordering of the rgbi bits than the other
/*TODO*///	 * color avg games.
/*TODO*///	 * We need translation tables.
/*TODO*///	 */
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr tempest_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///	#if 0 /* with low intensity bit */
/*TODO*///		static const int trans[]= { 7, 15, 3, 11, 6, 14, 2, 10, 5, 13, 1,  9, 4, 12, 0,  8 };
/*TODO*///	#else /* high intensity */
/*TODO*///		static const int trans[]= { 7,  7, 3,  3, 6,  6, 2,  2, 5,  5, 1,  1, 4,  4, 0,  0 };
/*TODO*///	#endif
/*TODO*///		colorram_w (offset, trans[data & 0x0f]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr mhavoc_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///	#if 0 /* with low intensity bit */
/*TODO*///		static const int trans[]= { 7, 6, 5, 4, 15, 14, 13, 12, 3, 2, 1, 0, 11, 10, 9, 8 };
/*TODO*///	#else /* high intensity */
/*TODO*///		static const int trans[]= { 7, 6, 5, 4,  7,  6,  5,  4, 3, 2, 1, 0,  3,  2, 1, 0 };
/*TODO*///	#endif
/*TODO*///		logerror("colorram: %02x: %02x\n", offset, data);
/*TODO*///		colorram_w (offset , trans[data & 0x0f]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( quantum_colorram_w )
/*TODO*///	{
/*TODO*///	/* Notes on colors:
/*TODO*///	offset:				color:			color (game):
/*TODO*///	0 - score, some text		0 - black?
/*TODO*///	1 - nothing?			1 - blue
/*TODO*///	2 - nothing?			2 - green
/*TODO*///	3 - Quantum, streaks		3 - cyan
/*TODO*///	4 - text/part 1 player		4 - red
/*TODO*///	5 - part 2 of player		5 - purple
/*TODO*///	6 - nothing?			6 - yellow
/*TODO*///	7 - part 3 of player		7 - white
/*TODO*///	8 - stars			8 - black
/*TODO*///	9 - nothing?			9 - blue
/*TODO*///	10 - nothing?			10 - green
/*TODO*///	11 - some text, 1up, like 3	11 - cyan
/*TODO*///	12 - some text, like 4
/*TODO*///	13 - nothing?			13 - purple
/*TODO*///	14 - nothing?
/*TODO*///	15 - nothing?
/*TODO*///	
/*TODO*///	1up should be blue
/*TODO*///	score should be red
/*TODO*///	high score - white? yellow?
/*TODO*///	level # - green
/*TODO*///	*/
/*TODO*///	
/*TODO*///		if (ACCESSING_LSB != 0)
/*TODO*///		{
/*TODO*///			static const int trans[]= { 7/*white*/, 0, 3, 1/*blue*/, 2/*green*/, 5, 6, 4/*red*/,
/*TODO*///				       7/*white*/, 0, 3, 1/*blue*/, 2/*green*/, 5, 6, 4/*red*/};
/*TODO*///	
/*TODO*///			colorram_w(offset, trans[data & 0x0f]);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///	  Draw the game screen in the given osd_bitmap.
/*TODO*///	  Do NOT call osd_update_display() from this function, it will be called by
/*TODO*///	  the main emulation engine.
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	int dvg_start(void)
/*TODO*///	{
/*TODO*///		return avgdvg_init (USE_DVG);
/*TODO*///	}
/*TODO*///	
/*TODO*///	int avg_start(void)
/*TODO*///	{
/*TODO*///		return avgdvg_init (USE_AVG);
/*TODO*///	}
	
	public static VhStartPtr avg_start_starwars = new VhStartPtr() {
            @Override
            public int handler() {
                return avgdvg_init (USE_AVG_SWARS);
            }
        };
        
/*TODO*///	int avg_start_tempest(void)
/*TODO*///	{
/*TODO*///		return avgdvg_init (USE_AVG_TEMPEST);
/*TODO*///	}
/*TODO*///	
/*TODO*///	int avg_start_mhavoc(void)
/*TODO*///	{
/*TODO*///		return avgdvg_init (USE_AVG_MHAVOC);
/*TODO*///	}
/*TODO*///	
/*TODO*///	int avg_start_bzone(void)
/*TODO*///	{
/*TODO*///		return avgdvg_init (USE_AVG_BZONE);
/*TODO*///	}
/*TODO*///	
/*TODO*///	int avg_start_quantum(void)
/*TODO*///	{
/*TODO*///		return avgdvg_init (USE_AVG_QUANTUM);
/*TODO*///	}
/*TODO*///	
/*TODO*///	int avg_start_redbaron(void)
/*TODO*///	{
/*TODO*///		return avgdvg_init (USE_AVG_RBARON);
/*TODO*///	}
	
	public static VhStopPtr avg_stop = new VhStopPtr() {
            @Override
            public void handler() {
                busy = 0;
		vector_clear_list();
	
		vector_vh_stop.handler();
            }
        };
	
/*TODO*///	void dvg_stop(void)
/*TODO*///	{
/*TODO*///		busy = 0;
/*TODO*///		vector_clear_list();
/*TODO*///	
/*TODO*///		vector_vh_stop();
/*TODO*///	}
	
}
