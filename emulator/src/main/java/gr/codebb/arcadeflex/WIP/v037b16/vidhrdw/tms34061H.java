/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.WIP.v037b16.vidhrdw;

import common.ptr.UBytePtr;

public class tms34061H {

    /* register constants */
    public static final int TMS34061_HORENDSYNC = 0;
    public static final int TMS34061_HORENDBLNK = 1;
    public static final int TMS34061_HORSTARTBLNK = 2;
    public static final int TMS34061_HORTOTAL = 3;
    public static final int TMS34061_VERENDSYNC = 4;
    public static final int TMS34061_VERENDBLNK = 5;
    public static final int TMS34061_VERSTARTBLNK = 6;
    public static final int TMS34061_VERTOTAL = 7;
    public static final int TMS34061_DISPUPDATE = 8;
    public static final int TMS34061_DISPSTART = 9;
    public static final int TMS34061_VERINT = 10;
    public static final int TMS34061_CONTROL1 = 11;
    public static final int TMS34061_CONTROL2 = 12;
    public static final int TMS34061_STATUS = 13;
    public static final int TMS34061_XYOFFSET = 14;
    public static final int TMS34061_XYADDRESS = 15;
    public static final int TMS34061_DISPADDRESS = 16;
    public static final int TMS34061_VERCOUNTER = 17;
    public static final int TMS34061_REGCOUNT = 18;

    public static abstract interface TmsInterruptPtr {

        public abstract void handler(int state);
    }


    /* interface structure */
    public static class tms34061_interface {

        public tms34061_interface(int u8_rowshift, int vramsize, int dirtychunk, TmsInterruptPtr interrupt) {
            this.u8_rowshift = u8_rowshift;
            this.vramsize = vramsize;
            this.dirtychunk = dirtychunk;
            this.interrupt = interrupt;
        }

        public int /*UINT8*/ u8_rowshift;/* VRAM address is (row << rowshift) | col */
        public int /*UINT32*/ vramsize;/* size of video RAM */
        public int /*UINT32*/ dirtychunk;/* size of dirty chunks (must be power of 2) */
        public TmsInterruptPtr interrupt;/* interrupt gen callback */
    }


    /* display state structure */
    public static class tms34061_display {

        int/*UINT8*/ blanked;/* true if blanked */
        UBytePtr vram;/* base of VRAM */
        UBytePtr latchram;/* base of latch RAM */
        char[] dirty;/* pointer to array of dirty rows */
        char[] regs;/* pointer to array of registers */
        int dispstart;/* display start */
    }
}
