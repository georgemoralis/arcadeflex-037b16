/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.WIP.v037b16.machine;

import static gr.codebb.arcadeflex.v037b16.cpu.z80.z80H.Z80_PC;
import gr.codebb.arcadeflex.v037b16.generic.fucPtr.ReadHandlerPtr;
import gr.codebb.arcadeflex.v037b16.generic.fucPtr.WriteHandlerPtr;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.cpu_set_reset_line;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.cpunum_get_reg;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.ASSERT_LINE;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.CLEAR_LINE;
import static gr.codebb.arcadeflex.v037b16.mame.inptport.readinputport;
import gr.codebb.arcadeflex.v037b16.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b16.mame.palette.palette_used_colors;
import static gr.codebb.arcadeflex.v037b16.mame.paletteH.PALETTE_COLOR_USED;
import static gr.codebb.arcadeflex.v056.mame.timer.timer_set;
import static gr.codebb.arcadeflex.v056.mame.timerH.TIME_NOW;
import static mame037b16.mame.Machine;
import static mame037b7.palette.palette_change_color;

public class slikshot {

    static int/*UINT8*/ u8_z80_ctrl;
    static int/*UINT8*/ u8_z80_port_val;
    static int z80_clear_to_send;

    static char nextsensor0, nextsensor1, nextsensor2, nextsensor3;
    static char sensor0, sensor1, sensor2, sensor3;

    static int/*UINT8*/ curvx, curvy = 1, curx;
    static int/*UINT8*/ lastshoot;

    /*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	sensors_to_words
/*TODO*///	 *
/*TODO*///	 *	converts from raw sensor data to
/*TODO*///	 *	the three words + byte that the
/*TODO*///	 *	Z80 sends to the main 6809
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static void sensors_to_words(UINT16 sens0, UINT16 sens1, UINT16 sens2, UINT16 sens3,
/*TODO*///								UINT16 *word1, UINT16 *word2, UINT16 *word3, UINT8 *beams)
/*TODO*///	{
/*TODO*///		/* word 1 contains the difference between the larger of sensors 2 & 3 and the smaller */
/*TODO*///		*word1 = (sens3 > sens2) ? (sens3 - sens2) : (sens2 - sens3);
/*TODO*///	
/*TODO*///		/* word 2 contains the value of the smaller of sensors 2 & 3 */
/*TODO*///		*word2 = (sens3 > sens2) ? sens2 : sens3;
/*TODO*///	
/*TODO*///		/* word 3 contains the value of sensor 0 or 1, depending on which fired */
/*TODO*///		*word3 = sens0 ? sens0 : sens1;
/*TODO*///	
/*TODO*///		/* set the beams bits */
/*TODO*///		*beams = 0;
/*TODO*///	
/*TODO*///		/* if sensor 1 fired first, set bit 0 */
/*TODO*///		if (!sens0)
/*TODO*///			*beams |= 1;
/*TODO*///	
/*TODO*///		/* if sensor 3 has the larger value, set bit 1 */
/*TODO*///		if (sens3 > sens2)
/*TODO*///			*beams |= 2;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	words_to_inters
/*TODO*///	 *
/*TODO*///	 *	converts the three words + byte
/*TODO*///	 *	data from the Z80 into the three
/*TODO*///	 *	intermediate values used in the
/*TODO*///	 *	final calculations
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static void words_to_inters(UINT16 word1, UINT16 word2, UINT16 word3, UINT8 beams,
/*TODO*///								UINT16 *inter1, UINT16 *inter2, UINT16 *inter3)
/*TODO*///	{
/*TODO*///		/* word 2 is scaled up by 0x1.6553 */
/*TODO*///		UINT16 word2mod = ((UINT64)word2 * 0x16553) >> 16;
/*TODO*///	
/*TODO*///		/* intermediate values 1 and 2 are determined based on the beams bits */
/*TODO*///		switch (beams)
/*TODO*///		{
/*TODO*///			case 0:
/*TODO*///				*inter1 = word1 + word2mod;
/*TODO*///				*inter2 = word2mod + word3;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 1:
/*TODO*///				*inter1 = word1 + word2mod + word3;
/*TODO*///				*inter2 = word2mod;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 2:
/*TODO*///				*inter1 = word2mod;
/*TODO*///				*inter2 = word1 + word2mod + word3;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 3:
/*TODO*///				*inter1 = word2mod + word3;
/*TODO*///				*inter2 = word1 + word2mod;
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* intermediate value 3 is always equal to the third word */
/*TODO*///		*inter3 = word3;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	inters_to_vels
/*TODO*///	 *
/*TODO*///	 *	converts the three intermediate
/*TODO*///	 *	values to the final velocity and
/*TODO*///	 *	X position values
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static void inters_to_vels(UINT16 inter1, UINT16 inter2, UINT16 inter3, UINT8 beams,
/*TODO*///								UINT8 *xres, UINT8 *vxres, UINT8 *vyres)
/*TODO*///	{
/*TODO*///		UINT32 _27d8, _27c2;
/*TODO*///		UINT32 vx, vy, _283a, _283e;
/*TODO*///		UINT8 vxsgn;
/*TODO*///		UINT16 xoffs = 0x0016;
/*TODO*///		UINT8 xscale = 0xe6;
/*TODO*///		UINT16 x;
/*TODO*///	
/*TODO*///		/* compute Vy */
/*TODO*///		vy = inter1 ? (0x31c28 / inter1) : 0;
/*TODO*///	
/*TODO*///		/* compute Vx */
/*TODO*///		_283a = inter2 ? (0x30f2e / inter2) : 0;
/*TODO*///		_27d8 = ((UINT64)vy * 0xfbd3) >> 16;
/*TODO*///		_27c2 = _283a - _27d8;
/*TODO*///		vxsgn = 0;
/*TODO*///		if ((INT32)_27c2 < 0)
/*TODO*///		{
/*TODO*///			vxsgn = 1;
/*TODO*///			_27c2 = _27d8 - _283a;
/*TODO*///		}
/*TODO*///		vx = ((UINT64)_27c2 * 0x58f8c) >> 16;
/*TODO*///	
/*TODO*///		/* compute X */
/*TODO*///		_27d8 = ((UINT64)(inter3 << 16) * _283a) >> 16;
/*TODO*///		_283e = ((UINT64)_27d8 * 0x4a574b) >> 16;
/*TODO*///	
/*TODO*///		/* adjust X based on the low bit of the beams */
/*TODO*///		if ((beams & 1) != 0)
/*TODO*///			x = 0x7a + (_283e >> 16) - xoffs;
/*TODO*///		else
/*TODO*///			x = 0x7a - (_283e >> 16) - xoffs;
/*TODO*///	
/*TODO*///		/* apply a constant X scale */
/*TODO*///		if (xscale != 0)
/*TODO*///			x = ((xscale * (x & 0xff)) >> 8) & 0xff;
/*TODO*///	
/*TODO*///		/* clamp if out of range */
/*TODO*///		if ((vx & 0xffff) >= 0x80)
/*TODO*///			x = 0;
/*TODO*///	
/*TODO*///		/* put the sign back in Vx */
/*TODO*///		vx &= 0xff;
/*TODO*///		if (!vxsgn)
/*TODO*///			vx = -vx;
/*TODO*///	
/*TODO*///		/* clamp VY */
/*TODO*///		if ((vy & 0xffff) > 0x7f)
/*TODO*///			vy = 0x7f;
/*TODO*///		else
/*TODO*///			vy &= 0xff;
/*TODO*///	
/*TODO*///		/* copy the results */
/*TODO*///		*xres = x;
/*TODO*///		*vxres = vx;
/*TODO*///		*vyres = vy;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	vels_to_inters
/*TODO*///	 *
/*TODO*///	 *	converts from the final velocity
/*TODO*///	 *	and X position values back to
/*TODO*///	 *	three intermediate values that
/*TODO*///	 *	will produce the desired result
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static void vels_to_inters(UINT8 x, UINT8 vx, UINT8 vy,
/*TODO*///								UINT16 *inter1, UINT16 *inter2, UINT16 *inter3, UINT8 *beams)
/*TODO*///	{
/*TODO*///		UINT32 _27d8;
/*TODO*///		UINT16 xoffs = 0x0016;
/*TODO*///		UINT8 xscale = 0xe6;
/*TODO*///		UINT8 x1, vx1, vy1;
/*TODO*///		UINT8 x2, vx2, vy2;
/*TODO*///		UINT8 diff1, diff2;
/*TODO*///		UINT16 inter2a;
/*TODO*///	
/*TODO*///		/* inter1 comes from Vy */
/*TODO*///		*inter1 = vy ? 0x31c28 / vy : 0;
/*TODO*///	
/*TODO*///		/* inter2 can be derived from Vx and Vy */
/*TODO*///		_27d8 = ((UINT64)vy * 0xfbd3) >> 16;
/*TODO*///		*inter2 = 0x30f2e / (_27d8 + ((abs((INT8)vx) << 16) / 0x58f8c));
/*TODO*///		inter2a = 0x30f2e / (_27d8 - ((abs((INT8)vx) << 16) / 0x58f8c));
/*TODO*///	
/*TODO*///		/* compute it back both ways and pick the closer */
/*TODO*///		inters_to_vels(*inter1, *inter2, 0, 0, &x1, &vx1, &vy1);
/*TODO*///		inters_to_vels(*inter1, inter2a, 0, 0, &x2, &vx2, &vy2);
/*TODO*///		diff1 = (vx > vx1) ? (vx - vx1) : (vx1 - vx);
/*TODO*///		diff2 = (vx > vx2) ? (vx - vx2) : (vx2 - vx);
/*TODO*///		if (diff2 < diff1)
/*TODO*///			*inter2 = inter2a;
/*TODO*///	
/*TODO*///		/* inter3: (beams & 1 == 1), inter3a: (beams & 1) == 0 */
/*TODO*///		if (((x << 8) / xscale) + xoffs >= 0x7a)
/*TODO*///		{
/*TODO*///			*beams = 1;
/*TODO*///			*inter3 = (((((((UINT64)(((x << 8) / xscale) + xoffs - 0x7a)) << 16) << 16) / 0x4a574b) << 16) / (0x30f2e / *inter2)) >> 16;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			*beams = 0;
/*TODO*///			*inter3 = (((((((UINT64)(((x << 8) / xscale) + xoffs - 0x7a) * -1) << 16) << 16) / 0x4a574b) << 16) / (0x30f2e / *inter2)) >> 16;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	inters_to_words
/*TODO*///	 *
/*TODO*///	 *	converts the intermediate values
/*TODO*///	 *	used in the final calculations
/*TODO*///	 *	back to the three words + byte
/*TODO*///	 *	data from the Z80
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static void inters_to_words(UINT16 inter1, UINT16 inter2, UINT16 inter3, UINT8 *beams,
/*TODO*///								UINT16 *word1, UINT16 *word2, UINT16 *word3)
/*TODO*///	{
/*TODO*///		UINT16 word2mod;
/*TODO*///	
/*TODO*///		/* intermediate value 3 is always equal to the third word */
/*TODO*///		*word3 = inter3;
/*TODO*///	
/*TODO*///		/* on input, it is expected that the low bit of beams has already been determined */
/*TODO*///		if (*beams & 1)
/*TODO*///		{
/*TODO*///			/* make sure we can do it */
/*TODO*///			if (inter3 <= inter1)
/*TODO*///			{
/*TODO*///				/* always go back via case 3 */
/*TODO*///				*beams |= 2;
/*TODO*///	
/*TODO*///				/* compute an appropriate value for the scaled version of word 2 */
/*TODO*///				word2mod = inter1 - inter3;
/*TODO*///	
/*TODO*///				/* compute the other values from that */
/*TODO*///				*word1 = inter2 - word2mod;
/*TODO*///				*word2 = ((UINT64)word2mod << 16) / 0x16553;
/*TODO*///			}
/*TODO*///			else
/*TODO*///				logerror("inters_to_words: unable to convert %04x %04x %04x %02x\n",
/*TODO*///						(UINT32)inter1, (UINT32)inter2, (UINT32)inter3, (UINT32)*beams);
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* handle the case where low bit of beams is 0 */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			/* make sure we can do it */
/*TODO*///			if (inter3 <= inter2)
/*TODO*///			{
/*TODO*///				/* always go back via case 0 */
/*TODO*///	
/*TODO*///				/* compute an appropriate value for the scaled version of word 2 */
/*TODO*///				word2mod = inter2 - inter3;
/*TODO*///	
/*TODO*///				/* compute the other values from that */
/*TODO*///				*word1 = inter1 - word2mod;
/*TODO*///				*word2 = ((UINT64)word2mod << 16) / 0x16553;
/*TODO*///			}
/*TODO*///			else
/*TODO*///				logerror("inters_to_words: unable to convert %04x %04x %04x %02x\n",
/*TODO*///						(UINT32)inter1, (UINT32)inter2, (UINT32)inter3, (UINT32)*beams);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	words_to_sensors
/*TODO*///	 *
/*TODO*///	 *	converts from the three words +
/*TODO*///	 *	byte that the Z80 sends to the
/*TODO*///	 *	main 6809 back to raw sensor data
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static void words_to_sensors(UINT16 word1, UINT16 word2, UINT16 word3, UINT8 beams,
/*TODO*///								UINT16 *sens0, UINT16 *sens1, UINT16 *sens2, UINT16 *sens3)
/*TODO*///	{
/*TODO*///		/* if bit 0 of the beams is set, sensor 1 fired first; otherwise sensor 0 fired */
/*TODO*///		if ((beams & 1) != 0)
/*TODO*///			*sens0 = 0, *sens1 = word3;
/*TODO*///		else
/*TODO*///			*sens0 = word3, *sens1 = 0;
/*TODO*///	
/*TODO*///		/* if bit 1 of the beams is set, sensor 3 had a larger value */
/*TODO*///		if ((beams & 2) != 0)
/*TODO*///			*sens3 = word2 + word1, *sens2 = word2;
/*TODO*///		else
/*TODO*///			*sens2 = word2 + word1, *sens3 = word2;
/*TODO*///	}
    /**
     * ***********************************
     *
     * compute_sensors
     *
     ************************************
     */
    static void compute_sensors() {
        throw new UnsupportedOperationException("Unsupported");
        /*TODO*///		UINT16 inter1, inter2, inter3;
/*TODO*///		UINT16 word1, word2, word3;
/*TODO*///		UINT8 beams;
/*TODO*///	
/*TODO*///		/* skip if we're not ready */
/*TODO*///		if (sensor0 != 0 || sensor1 != 0 || sensor2 != 0 || sensor3 != 0)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* reverse map the inputs */
/*TODO*///		vels_to_inters(curx, curvx, curvy, &inter1, &inter2, &inter3, &beams);
/*TODO*///		inters_to_words(inter1, inter2, inter3, &beams, &word1, &word2, &word3);
/*TODO*///		words_to_sensors(word1, word2, word3, beams, &nextsensor0, &nextsensor1, &nextsensor2, &nextsensor3);
/*TODO*///	
/*TODO*///		logerror("%15f: Sensor values: %04x %04x %04x %04x\n", timer_get_time(), nextsensor0, nextsensor1, nextsensor2, nextsensor3);
/*TODO*///
    }

    /**
     * ***********************************
     *
     * slikz80_port_r
     *
     ************************************
     */
    public static ReadHandlerPtr slikz80_port_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int result = 0;

            /* if we have nothing, return 0x03 */
            if (sensor0 == 0 && sensor1 == 0 && sensor2 == 0 && sensor3 == 0) {
                return 0x03 | (z80_clear_to_send << 7);
            }

            /* 1 bit for each sensor */
            if (sensor0 != 0) {
                result |= 1;
                sensor0--;
            }
            if (sensor1 != 0) {
                result |= 2;
                sensor1--;
            }
            if (sensor2 != 0) {
                result |= 4;
                sensor2--;
            }
            if (sensor3 != 0) {
                result |= 8;
                sensor3--;
            }
            result |= z80_clear_to_send << 7;

            return result;
        }
    };

    /**
     * ***********************************
     *
     * slikz80_port_w
     *
     ************************************
     */
    public static WriteHandlerPtr slikz80_port_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            u8_z80_port_val = data & 0xFF;
            z80_clear_to_send = 0;
        }
    };

    /**
     * ***********************************
     *
     * slikshot_z80_r
     *
     ************************************
     */
    public static ReadHandlerPtr slikshot_z80_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* allow the Z80 to send us stuff now */
            z80_clear_to_send = 1;
            timer_set(TIME_NOW, 0, null);

            return u8_z80_port_val & 0xFF;
        }
    };

    /**
     * ***********************************
     *
     * slikshot_z80_control_r
     *
     ************************************
     */
    public static ReadHandlerPtr slikshot_z80_control_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return u8_z80_ctrl & 0xFF;
        }
    };

    /**
     * ***********************************
     *
     * slikshot_z80_control_w
     *
     ************************************
     */
    public static WriteHandlerPtr slikshot_z80_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int/*UINT8*/ delta = (u8_z80_ctrl ^ data) & 0xFF;
            u8_z80_ctrl = data & 0xFF;

            /* reset the Z80 on bit 4 changing */
            if ((delta & 0x10) != 0) {
                //		logerror("%15f: Reset Z80: %02x  PC=%04x\n", timer_get_time(), data & 0x10, cpunum_get_reg(2, Z80_PC));

                /* this is a big kludge: only allow a reset if the Z80 is stopped */
 /* at its endpoint; otherwise, we never get a result from the Z80 */
                if ((data & 0x10) != 0 || cpunum_get_reg(2, Z80_PC) == 0x13a) {
                    cpu_set_reset_line(2, (data & 0x10) != 0 ? CLEAR_LINE : ASSERT_LINE);

                    /* on the rising edge, do housekeeping */
                    if ((data & 0x10) != 0) {
                        sensor0 = nextsensor0;
                        sensor1 = nextsensor1;
                        sensor2 = nextsensor2;
                        sensor3 = nextsensor3;
                        nextsensor0 = nextsensor1 = nextsensor2 = nextsensor3 = 0;
                        z80_clear_to_send = 0;
                    }
                }
            }

            /* on bit 5 going live, this looks like a clock, but the system */
 /* won't work with it configured as such */
            if ((delta & data & 0x20) != 0) {
                //		logerror("%15f: Clock edge high\n", timer_get_time());
            }
        }
    };

    /**
     * ***********************************
     *
     * slikshot_extra_draw
     *
     * render a line representing the current X crossing and the velocities
     *
     ************************************
     */
    public static void slikshot_extra_draw(osd_bitmap bitmap) {
        byte vx = (byte) readinputport(3);
        byte vy = (byte) readinputport(4);
        int/*UINT8*/ u8_xpos = readinputport(5) & 0xFF;
        int xstart, ystart, xend, yend;
        int dx, dy, absdx, absdy;
        int count, i;
        int newshoot;

        /* make sure color 256 is white for our crosshair */
        palette_change_color(256, 0xff, 0xff, 0xff);
        palette_used_colors.write(256, PALETTE_COLOR_USED);

        /* compute the updated values */
        curvx = vx & 0xFF;
        curvy = (vy < 1) ? 1 : vy & 0xFF;
        curx = u8_xpos & 0xFF;

        /* if the shoot button is pressed, fire away */
        newshoot = readinputport(2) & 1;
        if (newshoot != 0 && lastshoot == 0) {
            compute_sensors();
            //		usrintf_showmessage("V=%02x,%02x  X=%02x", curvx, curvy, curx);
        }
        lastshoot = newshoot;

        /* draw a crosshair (rotated) */
        xstart = (((int) curx - 0x60) * 0x100 / 0xd0) + 144;
        ystart = 256 - 48;
        xend = xstart + (byte) curvx;
        yend = ystart - (byte) curvy;

        /* compute line params */
        dx = xend - xstart;
        dy = yend - ystart;
        absdx = (dx < 0) ? -dx : dx;
        absdy = (dy < 0) ? -dy : dy;
        if (absdx > absdy) {
            dy = absdx != 0 ? ((dy << 16) / absdx) : 0;
            dx = (dx < 0) ? -0x10000 : 0x10000;
            count = absdx;
        } else {
            dx = absdy != 0 ? ((dx << 16) / absdy) : 0;
            dy = (dy < 0) ? -0x10000 : 0x10000;
            count = absdy;
        }

        /* scale the start points */
        xstart <<= 16;
        ystart <<= 16;

        /* draw the line */
        for (i = 0; i < count; i++) {
            int px = xstart >> 16, py = ystart >> 16;

            if (px >= 0 && px < bitmap.width
                    && py >= 0 && py < bitmap.height) {
                if (bitmap.depth == 8) {
                    bitmap.line[py].write(px, Machine.pens[256]);
                } else {
                    throw new UnsupportedOperationException("Unsupported");
                    /*TODO*///((UINT16 *)bitmap.line[py])[px] = Machine.pens[256];
                }
            }
            xstart += dx;
            ystart += dy;
        }
    }
}
