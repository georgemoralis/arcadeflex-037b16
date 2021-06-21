/**
 * ported to v0.37b16
 *
 */
package mame037b16;

//mame imports
import static gr.codebb.arcadeflex.v037b16.mame.driverH.*;

import static arcadeflex037b16.debug.*;
import static arcadeflex037b7.video.osd_set_visible_area;
import static common.ptr.*;
import static gr.codebb.arcadeflex.v037b16.mame.commonH.*;
import static common.libc.cstdio.*;
import static common.libc.cstring.*;
import static common.util.*;
import static arcadeflex056.fileio.*;
import static common.libc.cstdlib.*;
import static gr.codebb.arcadeflex.v037b16.mame.common.free_memory_region;
import static gr.codebb.arcadeflex.v037b16.mame.common.memory_region;
import static gr.codebb.arcadeflex.v037b16.mame.common.memory_region_length;
import static gr.codebb.arcadeflex.v037b16.mame.common.new_memory_region;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.cputype_databus_width;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrf.cputype_endianess;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.CPU_FLAGS_MASK;
import static gr.codebb.arcadeflex.v037b16.mame.cpuintrfH.CPU_IS_LE;
import static gr.codebb.arcadeflex.v037b16.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b16.mame.osdependH.*;
import static mame037b16.mame.*;
import static mame037b16.mameH.*;

public class common {

    /**
     * *************************************************************************
     *
     * Type definitions
     *
     **************************************************************************
     */
    public static class rom_load_data {

        int warnings;/* warning count during processing */
        int errors;/* error count during processing */

        int romsloaded;/* current ROMs loaded count */
        int romstotal;/* total number of ROMs to read */
        Object file;/* current file */

        UBytePtr regionbase;/* base of current region */
        int /*UINT32*/ regionlength;/* length of current region */
        String errorbuf = "";/* accumulated errors */
 /*TODO*///	UINT8		tempbuf[65536];			/* temporary buffer */
    }





    /**
     * *************************************************************************
     *
     * Sample handling code
     *
     * This function is different from readroms() because it doesn't fail if it
     * doesn't find a file: it will load as many samples as it can find.
     *
     **************************************************************************
     */
    /*-------------------------------------------------
	read_wav_sample - read a WAV file as a sample
-------------------------------------------------*/
    static GameSample read_wav_sample(Object f) {
        long /*unsigned*/ offset = 0;
        long /*UINT32*/ length, rate, filesize, temp32;
        int /*UINT16*/ bits, temp16;
        char[] /*UINT8*/ buf = new char[32];
        GameSample result = null;


        /* read the core header and make sure it's a WAVE file */
        offset += osd_fread(f, buf, 4);
        if (offset < 4) {
            return null;
        }
        if (memcmp(buf, 0, "RIFF", 4) != 0) {
            return null;
        }

        /* get the total size */
        offset += osd_fread(f, buf, 4);
        if (offset < 8) {
            return null;
        }
        filesize = charArrayToLong(buf);

        /* read the RIFF file type and make sure it's a WAVE file */
        offset += osd_fread(f, buf, 4);
        if (offset < 12) {
            return null;
        }
        if (memcmp(buf, 0, "WAVE", 4) != 0) {
            return null;
        }


        /* seek until we find a format tag */
        while (true) {
            offset += osd_fread(f, buf, 4);
            char[] tmp = new char[buf.length];//temp creation
            System.arraycopy(buf, 0, tmp, 0, buf.length);//temp creation
            offset += osd_fread(f, buf, 4);//offset += osd_fread(f, &length, 4);
            length = charArrayToLong(buf);
            if (memcmp(tmp, 0, "fmt ", 4) == 0) {
                break;
            }

            /* seek to the next block */
            osd_fseek(f, (int) length, SEEK_CUR);
            offset += length;
            if (offset >= filesize) {
                return null;
            }
        }
        /* read the format -- make sure it is PCM */
        offset += osd_fread_lsbfirst(f, buf, 2);
        temp16 = charArrayToInt(buf);
        if (temp16 != 1) {
            return null;
        }

        /* number of channels -- only mono is supported */
        offset += osd_fread_lsbfirst(f, buf, 2);
        temp16 = charArrayToInt(buf);
        if (temp16 != 1) {
            return null;
        }

        /* sample rate */
        offset += osd_fread(f, buf, 4);
        rate = charArrayToLong(buf);

        /* bytes/second and block alignment are ignored */
        offset += osd_fread(f, buf, 6);

        /* bits/sample */
        offset += osd_fread_lsbfirst(f, buf, 2);
        bits = charArrayToInt(buf);
        if (bits != 8 && bits != 16) {
            return null;
        }


        /* seek past any extra data */
        osd_fseek(f, (int) length - 16, SEEK_CUR);
        offset += length - 16;

        /* seek until we find a data tag */
        while (true) {
            offset += osd_fread(f, buf, 4);
            char[] tmp = new char[buf.length];//temp creation
            System.arraycopy(buf, 0, tmp, 0, buf.length);//temp creation
            offset += osd_fread(f, buf, 4);//offset += osd_fread(f, &length, 4);
            length = charArrayToLong(buf);
            if (memcmp(tmp, 0, "data", 4) == 0) {
                break;
            }

            /* seek to the next block */
            osd_fseek(f, (int) length, SEEK_CUR);
            offset += length;
            if (offset >= filesize) {
                return null;
            }
        }
        /* allocate the game sample */
        result = new GameSample((int) length);
        /* fill in the sample data */
        result.length = (int) length;
        result.smpfreq = (int) rate;
        result.resolution = bits;

        /* read the data in */
        if (bits == 8) {
            osd_fread(f, result.data, (int) length);

            /* convert 8-bit data to signed samples */
            for (temp32 = 0; temp32 < length; temp32++) {
                result.data[(int) temp32] ^= 0x80;
            }
        } else {
            /* 16-bit data is fine as-is */
            osd_fread_lsbfirst(f, result.data, (int) length);
        }

        return result;
    }

    /*-------------------------------------------------
            readsamples - load all samples
    -------------------------------------------------*/
    public static GameSamples readsamples(String[] samplenames, String basename) /* V.V - avoids samples duplication */ /* if first samplename is *dir, looks for samples into "basename" first, then "dir" */ {
        int i;
        GameSamples samples = new GameSamples();
        int skipfirst = 0;

        /* if the user doesn't want to use samples, bail */
        if (options.use_samples == 0) {
            return null;
        }

        if (samplenames == null || samplenames[0] == null) {
            return null;
        }

        if (samplenames[0].charAt(0) == '*') {
            skipfirst = 1;
        }

        i = 0;
        while (samplenames[i + skipfirst] != null) {
            i++;
        }

        if (i == 0) {
            return null;
        }

        samples = new GameSamples(i);

        samples.total = i;
        for (i = 0; i < samples.total; i++) {
            samples.sample[i] = null;
        }

        for (i = 0; i < samples.total; i++) {
            Object f;

            if (samplenames[i + skipfirst].length() > 0 && samplenames[i + skipfirst].charAt(0) != '\0') {
                if ((f = osd_fopen(basename, samplenames[i + skipfirst], OSD_FILETYPE_SAMPLE, 0)) == null) {
                    if (skipfirst != 0) {
                        f = osd_fopen(samplenames[0].substring(1, samplenames[0].length())/*samplenames[0] + 1*/, samplenames[i + skipfirst], OSD_FILETYPE_SAMPLE, 0);
                    }
                }
                if (f != null) {
                    samples.sample[i] = read_wav_sample(f);
                    osd_fclose(f);
                }
            }
        }

        return samples;
    }


    /*-------------------------------------------------
            freesamples - free allocated samples
    -------------------------------------------------*/
    public static void freesamples(GameSamples samples) {
        int i;

        if (samples == null) {
            return;
        }

        for (i = 0; i < samples.total; i++) {
            samples.sample[i] = null;
        }

        samples = null;
    }



    public static void set_visible_area(int min_x, int max_x, int min_y, int max_y) {
        Machine.visible_area = new rectangle();
        Machine.visible_area.min_x = min_x;
        Machine.visible_area.max_x = max_x;
        Machine.visible_area.min_y = min_y;
        Machine.visible_area.max_y = max_y;

        /* vector games always use the whole bitmap */
        if ((Machine.drv.video_attributes & VIDEO_TYPE_VECTOR) != 0) {
            min_x = 0;
            max_x = Machine.scrbitmap.width - 1;
            min_y = 0;
            max_y = Machine.scrbitmap.height - 1;
        } else {
            int temp;

            if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
                temp = min_x;
                min_x = min_y;
                min_y = temp;
                temp = max_x;
                max_x = max_y;
                max_y = temp;
            }
            if ((Machine.orientation & ORIENTATION_FLIP_X) != 0) {
                temp = Machine.scrbitmap.width - min_x - 1;
                min_x = Machine.scrbitmap.width - max_x - 1;
                max_x = temp;
            }
            if ((Machine.orientation & ORIENTATION_FLIP_Y) != 0) {
                temp = Machine.scrbitmap.height - min_y - 1;
                min_y = Machine.scrbitmap.height - max_y - 1;
                max_y = temp;
            }
        }

        osd_set_visible_area(min_x, max_x, min_y, max_y);
        Machine.absolute_visible_area = new rectangle();
        Machine.absolute_visible_area.min_x = min_x;
        Machine.absolute_visible_area.max_x = max_x;
        Machine.absolute_visible_area.min_y = min_y;
        Machine.absolute_visible_area.max_y = max_y;
    }






    /*TODO*///void save_screen_snapshot_as(void *fp,struct osd_bitmap *bitmap)
/*TODO*///{
/*TODO*///	if (Machine->drv->video_attributes & VIDEO_TYPE_VECTOR)
/*TODO*///		png_write_bitmap(fp,bitmap);
/*TODO*///	else
/*TODO*///	{
/*TODO*///		struct osd_bitmap *copy;
/*TODO*///		int sizex, sizey, scalex, scaley;
/*TODO*///
/*TODO*///		sizex = Machine->visible_area.max_x - Machine->visible_area.min_x + 1;
/*TODO*///		sizey = Machine->visible_area.max_y - Machine->visible_area.min_y + 1;
/*TODO*///
/*TODO*///		scalex = (Machine->drv->video_attributes & VIDEO_PIXEL_ASPECT_RATIO_2_1) ? 2 : 1;
/*TODO*///		scaley = (Machine->drv->video_attributes & VIDEO_PIXEL_ASPECT_RATIO_1_2) ? 2 : 1;
/*TODO*///
/*TODO*///		copy = bitmap_alloc_depth(sizex * scalex,sizey * scaley,bitmap->depth);
/*TODO*///
/*TODO*///		if (copy)
/*TODO*///		{
/*TODO*///			int x,y,sx,sy;
/*TODO*///
/*TODO*///			sx = Machine->absolute_visible_area.min_x;
/*TODO*///			sy = Machine->absolute_visible_area.min_y;
/*TODO*///			if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///			{
/*TODO*///				int t;
/*TODO*///
/*TODO*///				t = scalex; scalex = scaley; scaley = t;
/*TODO*///			}
/*TODO*///
/*TODO*///			switch (bitmap->depth)
/*TODO*///			{
/*TODO*///			case 8:
/*TODO*///				for (y = 0;y < copy->height;y++)
/*TODO*///				{
/*TODO*///					for (x = 0;x < copy->width;x++)
/*TODO*///					{
/*TODO*///						copy->line[y][x] = bitmap->line[sy+(y/scaley)][sx +(x/scalex)];
/*TODO*///					}
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 15:
/*TODO*///			case 16:
/*TODO*///				for (y = 0;y < copy->height;y++)
/*TODO*///				{
/*TODO*///					for (x = 0;x < copy->width;x++)
/*TODO*///					{
/*TODO*///						((UINT16 *)copy->line[y])[x] = ((UINT16 *)bitmap->line[sy+(y/scaley)])[sx +(x/scalex)];
/*TODO*///					}
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 32:
/*TODO*///				for (y = 0;y < copy->height;y++)
/*TODO*///				{
/*TODO*///					for (x = 0;x < copy->width;x++)
/*TODO*///					{
/*TODO*///						((UINT32 *)copy->line[y])[x] = ((UINT32 *)bitmap->line[sy+(y/scaley)])[sx +(x/scalex)];
/*TODO*///					}
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				logerror("Unknown color depth\n");
/*TODO*///				break;
/*TODO*///			}
/*TODO*///			png_write_bitmap(fp,copy);
/*TODO*///			bitmap_free(copy);
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
    public static int snapno;

    public static void osd_save_snapshot(osd_bitmap bitmap) {
        /*TODO*///	void *fp;
/*TODO*///	char name[20];
/*TODO*///
/*TODO*///
/*TODO*///	/* avoid overwriting existing files */
/*TODO*///	/* first of all try with "gamename.png" */
/*TODO*///	sprintf(name,"%.8s", Machine->gamedrv->name);
/*TODO*///	if (osd_faccess(name,OSD_FILETYPE_SCREENSHOT))
/*TODO*///	{
/*TODO*///		do
/*TODO*///		{
/*TODO*///			/* otherwise use "nameNNNN.png" */
/*TODO*///			sprintf(name,"%.4s%04d",Machine->gamedrv->name,snapno++);
/*TODO*///		} while (osd_faccess(name, OSD_FILETYPE_SCREENSHOT));
/*TODO*///	}
/*TODO*///
/*TODO*///	if ((fp = osd_fopen(Machine->gamedrv->name, name, OSD_FILETYPE_SCREENSHOT, 1)) != NULL)
/*TODO*///	{
/*TODO*///		save_screen_snapshot_as(fp,bitmap);
/*TODO*///		osd_fclose(fp);
/*TODO*///	}
    }

    /*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
    /**
     * *************************************************************************
     *
     * ROM loading code
     *
     **************************************************************************
     */

    /*-------------------------------------------------
            rom_first_region - return pointer to first ROM
            region
    -------------------------------------------------*/
    public static RomModule[] rom_first_region(GameDriver drv) {
        return drv.rom;
    }


    /*-------------------------------------------------
            rom_next_region - return pointer to next ROM
            region
    -------------------------------------------------*/
    public static int rom_next_region(RomModule[] romp, int romp_ptr) {
        romp_ptr++;
        while (!ROMENTRY_ISREGIONEND(romp, romp_ptr)) {
            romp_ptr++;
        }
        return ROMENTRY_ISEND(romp, romp_ptr) ? -1 : romp_ptr;
    }

    /*-------------------------------------------------
            rom_first_file - return pointer to first ROM
            file
    -------------------------------------------------*/
    public static int rom_first_file(RomModule[] romp, int romp_ptr) {
        romp_ptr++;
        while (!ROMENTRY_ISFILE(romp, romp_ptr) && !ROMENTRY_ISREGIONEND(romp, romp_ptr)) {
            romp_ptr++;
        }
        return ROMENTRY_ISREGIONEND(romp, romp_ptr) ? -1 : romp_ptr;
    }

    /*-------------------------------------------------
            rom_next_file - return pointer to next ROM
            file
    -------------------------------------------------*/
    public static int rom_next_file(RomModule[] romp, int romp_ptr) {
        romp_ptr++;
        while (!ROMENTRY_ISFILE(romp, romp_ptr) && !ROMENTRY_ISREGIONEND(romp, romp_ptr)) {
            romp_ptr++;
        }
        return ROMENTRY_ISREGIONEND(romp, romp_ptr) ? -1 : romp_ptr;
    }

    /*-------------------------------------------------
	rom_first_chunk - return pointer to first ROM
	chunk
    -------------------------------------------------*/
    public static int rom_first_chunk(RomModule[] romp, int romp_ptr) {
        return (ROMENTRY_ISFILE(romp, romp_ptr)) ? romp_ptr : -1;
    }


    /*-------------------------------------------------
            rom_next_chunk - return pointer to next ROM
            chunk
    -------------------------------------------------*/
    public static int rom_next_chunk(RomModule[] romp, int romp_ptr) {
        romp_ptr++;
        return (ROMENTRY_ISCONTINUE(romp, romp_ptr)) ? romp_ptr : -1;
    }

    /*-------------------------------------------------
        debugload - log data to a file
    -------------------------------------------------*/
    static int opened;

    public static void debugload(String string, Object... arguments) {
        if (romLoadLog) {
            FILE f;

            f = fopen("romload.log", (opened++) != 0 ? "a" : "w");
            if (f != null) {
                fprintf(f, string, arguments);
                fclose(f);
            }
        }
    }

    /*-------------------------------------------------
        count_roms - counts the total number of ROMs
        that will need to be loaded
    -------------------------------------------------*/
    static int count_roms(RomModule[] romp, int romp_ptr) {
        int region, rom;
        int count = 0;

        /* loop over regions, then over files */
        for (region = romp_ptr; region != -1; region = rom_next_region(romp, region)) {
            for (rom = rom_first_file(romp, region); rom != -1; rom = rom_next_file(romp, rom)) {
                count++;
            }
        }

        /* return the total count */
        return count;
    }

    /*-------------------------------------------------
        fill_random - fills an area of memory with
        random data
    -------------------------------------------------*/
    static void fill_random(UBytePtr base, int length) {
        while ((length--) != 0) {
            base.writeinc(rand());
        }
    }

    /*-------------------------------------------------
        handle_missing_file - handles error generation
        for missing files
    -------------------------------------------------*/
    static void handle_missing_file(rom_load_data romdata, RomModule[] romp, int rom_ptr) {
        /* optional files are okay */
        if (ROM_ISOPTIONAL(romp, rom_ptr)) {
            romdata.errorbuf = romdata.errorbuf + sprintf("OPTIONAL %-12s NOT FOUND\n", ROM_GETNAME(romp, rom_ptr));
            romdata.warnings++;
        } /* no good dumps are okay */ else if (ROM_NOGOODDUMP(romp, rom_ptr)) {
            romdata.errorbuf = romdata.errorbuf + sprintf("%-12s NOT FOUND (NO GOOD DUMP KNOWN)\n", ROM_GETNAME(romp, rom_ptr));
            romdata.warnings++;
        } /* anything else is bad */ else {
            romdata.errorbuf = romdata.errorbuf + sprintf("%-12s NOT FOUND\n", ROM_GETNAME(romp, rom_ptr));
            romdata.errors++;
        }
    }

    /*-------------------------------------------------
            verify_length_and_crc - verify the length
            and CRC of a file
    -------------------------------------------------*/
    public static void verify_length_and_crc(rom_load_data romdata, String name, int/*UINT32*/ explength, int/*UINT32*/ expcrc) {
        int/*UINT32*/ actlength, actcrc;

        /* we've already complained if there is no file */
        if (romdata.file == null) {
            return;
        }

        /* get the length and CRC from the file */
        actlength = osd_fsize(romdata.file);
        actcrc = osd_fcrc(romdata.file);

        /* verify length */
        if (explength != actlength) {
            romdata.errorbuf = romdata.errorbuf + sprintf("%-12s WRONG LENGTH (expected: %08x found: %08x)\n", name, explength, actlength);
            romdata.warnings++;
        }

        /* verify CRC */
        if (expcrc != actcrc) {
            /* expected CRC == 0 means no good dump known */
            if (expcrc == 0) {
                romdata.errorbuf = romdata.errorbuf + sprintf("%-12s NO GOOD DUMP KNOWN\n", name);
            } /* inverted CRC means needs redump */ else if (expcrc == BADCRC(actcrc)) {
                romdata.errorbuf = romdata.errorbuf + sprintf("%-12s ROM NEEDS REDUMP\n", name);
            } /* otherwise, it's just bad */ else {
                romdata.errorbuf = romdata.errorbuf + sprintf("%-12s WRONG CRC (expected: %08x found: %08x)\n", name, expcrc, actcrc);
            }
            romdata.warnings++;
        }
    }

    /*-------------------------------------------------
            display_rom_load_results - display the final
            results of ROM loading
    -------------------------------------------------*/
    static int display_rom_load_results(rom_load_data romdata) {
        int region;

        /* final status display */
        osd_display_loading_rom_message(null, romdata.romsloaded, romdata.romstotal);

        /* only display if we have warnings or errors */
        if (romdata.warnings != 0 || romdata.errors != 0) {
            /* display either an error message or a warning message */
            if (romdata.errors != 0) {
                romdata.errorbuf = romdata.errorbuf + "ERROR: required files are missing, the game cannot be run.\n";
                bailing = 1;
            } else {
                romdata.errorbuf = romdata.errorbuf + "WARNING: the game might not run correctly.\n";
            }

            /* display the result */
            printf("%s", romdata.errorbuf);
            /*TODO*///
/*TODO*///		/* if we're not getting out of here, wait for a keypress */
/*TODO*///		if (!options.gui_host && !bailing)
/*TODO*///		{
/*TODO*///			int k;
/*TODO*///
/*TODO*///			/* loop until we get one */
/*TODO*///			printf ("Press any key to continue\n");
/*TODO*///			do
/*TODO*///			{
/*TODO*///				k = code_read_async();
/*TODO*///			}
/*TODO*///			while (k == CODE_NONE || k == KEYCODE_LCONTROL);
/*TODO*///
/*TODO*///			/* bail on a control + C */
/*TODO*///			if (keyboard_pressed(KEYCODE_LCONTROL) && keyboard_pressed(KEYCODE_C))
/*TODO*///				return 1;
/*TODO*///		}
        }

        /* clean up any regions */
        if (romdata.errors != 0) {
            for (region = 0; region < MAX_MEMORY_REGIONS; region++) {
                free_memory_region(region);
            }
        }

        /* return true if we had any errors */
        return (romdata.errors != 0 ? 1 : 0);
    }

    /*-------------------------------------------------
            region_post_process - post-process a region,
            byte swapping and inverting data as necessary
    -------------------------------------------------*/
    static void region_post_process(rom_load_data romdata, RomModule[] regiondata, int rom_ptr) {
        int type = ROMREGION_GETTYPE(regiondata, rom_ptr);
        int datawidth = ROMREGION_GETWIDTH(regiondata, rom_ptr) / 8;
        boolean littleendian = ROMREGION_ISLITTLEENDIAN(regiondata, rom_ptr);
        /*TODO*///	UINT8 *base;
        int i, j;

        debugload("+ datawidth=%d little=%d\n", datawidth, littleendian ? 1 : 0);

        /* if this is a CPU region, override with the CPU width and endianness */
        if (type >= REGION_CPU1 && type < REGION_CPU1 + MAX_CPU) {
            int cputype = Machine.drv.cpu[type - REGION_CPU1].cpu_type & ~CPU_FLAGS_MASK;
            if (cputype != 0) {
                datawidth = cputype_databus_width(cputype) / 8;
                littleendian = (cputype_endianess(cputype) == CPU_IS_LE);
                debugload("+ CPU region #%d: datawidth=%d little=%d\n", type - REGION_CPU1, datawidth, littleendian ? 1 : 0);
            }
        }

        /* if the region is inverted, do that now */
        if (ROMREGION_ISINVERTED(regiondata, rom_ptr)) {
            throw new UnsupportedOperationException("Unimplemented");
            /*TODO*///		debugload("+ Inverting region\n");
/*TODO*///		for (i = 0, base = romdata->regionbase; i < romdata->regionlength; i++)
/*TODO*///			*base++ ^= 0xff;
        }
        /*TODO*///
        /* swap the endianness if we need to */
        if (datawidth > 1 && !littleendian) {
            throw new UnsupportedOperationException("Unimplemented");
            /*TODO*///		debugload("+ Byte swapping region\n");
/*TODO*///		for (i = 0, base = romdata->regionbase; i < romdata->regionlength; i += datawidth)
/*TODO*///		{
/*TODO*///			UINT8 temp[8];
/*TODO*///			memcpy(temp, base, datawidth);
/*TODO*///			for (j = datawidth - 1; j >= 0; j--)
/*TODO*///				*base++ = temp[j];
/*TODO*///		}
        }
    }

    /*-------------------------------------------------
	open_rom_file - open a ROM file, searching
	up the parent and loading via CRC
    -------------------------------------------------*/
    static boolean open_rom_file(rom_load_data romdata, RomModule[] romp, int rom_ptr) {
        GameDriver drv;
        String crc;

        /* update status display */
        if (osd_display_loading_rom_message(ROM_GETNAME(romp, rom_ptr), ++romdata.romsloaded, romdata.romstotal) != 0) {
            return false;
        }

        /* first attempt reading up the chain through the parents */
        romdata.file = null;
        for (drv = Machine.gamedrv; romdata.file == null && drv != null; drv = drv.clone_of) {
            if (drv.name != null && drv.name.length() > 0) {
                romdata.file = osd_fopen(drv.name, ROM_GETNAME(romp, rom_ptr), OSD_FILETYPE_ROM, 0);
            }
        }

        /* if that failed, attempt to open via CRC */
        crc = sprintf("%08x", ROM_GETCRC(romp, rom_ptr));
        for (drv = Machine.gamedrv; romdata.file == null && drv != null; drv = drv.clone_of) {
            if (drv.name != null && drv.name.length() > 0) {
                romdata.file = osd_fopen(drv.name, crc, OSD_FILETYPE_ROM, 0);
            }
        }

        /* return the result */
        return (romdata.file != null);
    }


    /*-------------------------------------------------
            rom_fread - cheesy fread that fills with
            random data for a NULL file
    -------------------------------------------------*/
    static int rom_fread(rom_load_data romdata, UBytePtr buffer, int length) {
        /* files just pass through */
        if (romdata.file != null) {
            return osd_fread(romdata.file, buffer, length);
        } /* otherwise, fill with randomness */ else {
            fill_random(buffer, length);
        }

        return length;
    }


    /*-------------------------------------------------
            read_rom_data - read ROM data for a single
            entry
    -------------------------------------------------*/
    static int read_rom_data(rom_load_data romdata, RomModule[] romp, int rom_ptr) {
        int datashift = ROM_GETBITSHIFT(romp, rom_ptr);
        int datamask = ((1 << ROM_GETBITWIDTH(romp, rom_ptr)) - 1) << datashift;
        int numbytes = ROM_GETLENGTH(romp, rom_ptr);
        int groupsize = ROM_GETGROUPSIZE(romp, rom_ptr);
        int skip = ROM_GETSKIPCOUNT(romp, rom_ptr);
        int reversed = ROM_ISREVERSED(romp, rom_ptr) ? 1 : 0;
        int numgroups = (numbytes + groupsize - 1) / groupsize;
        UBytePtr base = new UBytePtr(romdata.regionbase, ROM_GETOFFSET(romp, rom_ptr));
        int i;

        debugload("Loading ROM data: offs=%X len=%X mask=%02X group=%d skip=%d reverse=%d\n", ROM_GETOFFSET(romp, rom_ptr), numbytes, datamask, groupsize, skip, reversed);

        /* make sure the length was an even multiple of the group size */
        if (numbytes % groupsize != 0) {
            printf("Error in RomModule definition: %s length not an even multiple of group size\n", ROM_GETNAME(romp, rom_ptr));
            return -1;
        }

        /* make sure we only fill within the region space */
        if (ROM_GETOFFSET(romp, rom_ptr) + numgroups * groupsize + (numgroups - 1) * skip > romdata.regionlength) {
            printf("Error in RomModule definition: %s out of memory region space\n", ROM_GETNAME(romp, rom_ptr));
            return -1;
        }

        /* make sure the length was valid */
        if (numbytes == 0) {
            printf("Error in RomModule definition: %s has an invalid length\n", ROM_GETNAME(romp, rom_ptr));
            return -1;
        }

        /* special case for simple loads */
        if (datamask == 0xff && (groupsize == 1 || reversed == 0) && skip == 0) {
            return rom_fread(romdata, base, numbytes);
        }
        throw new UnsupportedOperationException("Unsupported");
        /*TODO*///
/*TODO*///	/* chunky reads for complex loads */
/*TODO*///	skip += groupsize;
/*TODO*///	while (numbytes)
/*TODO*///	{
/*TODO*///		int evengroupcount = (sizeof(romdata->tempbuf) / groupsize) * groupsize;
/*TODO*///		int bytesleft = (numbytes > evengroupcount) ? evengroupcount : numbytes;
/*TODO*///		UINT8 *bufptr = romdata->tempbuf;
/*TODO*///
/*TODO*///		/* read as much as we can */
/*TODO*///		debugload("  Reading %X bytes into buffer\n", bytesleft);
/*TODO*///		if (rom_fread(romdata, romdata->tempbuf, bytesleft) != bytesleft)
/*TODO*///			return 0;
/*TODO*///		numbytes -= bytesleft;
/*TODO*///
/*TODO*///		debugload("  Copying to %08X\n", (int)base);
/*TODO*///
/*TODO*///		/* unmasked cases */
/*TODO*///		if (datamask == 0xff)
/*TODO*///		{
/*TODO*///			/* non-grouped data */
/*TODO*///			if (groupsize == 1)
/*TODO*///				for (i = 0; i < bytesleft; i++, base += skip)
/*TODO*///					*base = *bufptr++;
/*TODO*///
/*TODO*///			/* grouped data -- non-reversed case */
/*TODO*///			else if (!reversed)
/*TODO*///				while (bytesleft)
/*TODO*///				{
/*TODO*///					for (i = 0; i < groupsize && bytesleft; i++, bytesleft--)
/*TODO*///						base[i] = *bufptr++;
/*TODO*///					base += skip;
/*TODO*///				}
/*TODO*///
/*TODO*///			/* grouped data -- reversed case */
/*TODO*///			else
/*TODO*///				while (bytesleft)
/*TODO*///				{
/*TODO*///					for (i = groupsize - 1; i >= 0 && bytesleft; i--, bytesleft--)
/*TODO*///						base[i] = *bufptr++;
/*TODO*///					base += skip;
/*TODO*///				}
/*TODO*///		}
/*TODO*///
/*TODO*///		/* masked cases */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			/* non-grouped data */
/*TODO*///			if (groupsize == 1)
/*TODO*///				for (i = 0; i < bytesleft; i++, base += skip)
/*TODO*///					*base = (*base & ~datamask) | ((*bufptr++ << datashift) & datamask);
/*TODO*///
/*TODO*///			/* grouped data -- non-reversed case */
/*TODO*///			else if (!reversed)
/*TODO*///				while (bytesleft)
/*TODO*///				{
/*TODO*///					for (i = 0; i < groupsize && bytesleft; i++, bytesleft--)
/*TODO*///						base[i] = (base[i] & ~datamask) | ((*bufptr++ << datashift) & datamask);
/*TODO*///					base += skip;
/*TODO*///				}
/*TODO*///
/*TODO*///			/* grouped data -- reversed case */
/*TODO*///			else
/*TODO*///				while (bytesleft)
/*TODO*///				{
/*TODO*///					for (i = groupsize - 1; i >= 0 && bytesleft; i--, bytesleft--)
/*TODO*///						base[i] = (base[i] & ~datamask) | ((*bufptr++ << datashift) & datamask);
/*TODO*///					base += skip;
/*TODO*///				}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	debugload("  All done\n");
/*TODO*///	return ROM_GETLENGTH(romp);
    }


    /*-------------------------------------------------
            fill_rom_data - fill a region of ROM space
    -------------------------------------------------*/

 /*TODO*///static int fill_rom_data(struct rom_load_data *romdata, const struct RomModule *romp)
/*TODO*///{
/*TODO*///	UINT32 numbytes = ROM_GETLENGTH(romp);
/*TODO*///	UINT8 *base = romdata->regionbase + ROM_GETOFFSET(romp);
/*TODO*///
/*TODO*///	/* make sure we fill within the region space */
/*TODO*///	if (ROM_GETOFFSET(romp) + numbytes > romdata->regionlength)
/*TODO*///	{
/*TODO*///		printf("Error in RomModule definition: FILL out of memory region space\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* make sure the length was valid */
/*TODO*///	if (numbytes == 0)
/*TODO*///	{
/*TODO*///		printf("Error in RomModule definition: FILL has an invalid length\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* fill the data */
/*TODO*///	memset(base, ROM_GETCRC(romp) & 0xff, numbytes);
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////*-------------------------------------------------
/*TODO*///	copy_rom_data - copy a region of ROM space
/*TODO*///-------------------------------------------------*/
/*TODO*///
/*TODO*///static int copy_rom_data(struct rom_load_data *romdata, const struct RomModule *romp)
/*TODO*///{
/*TODO*///	UINT8 *base = romdata->regionbase + ROM_GETOFFSET(romp);
/*TODO*///	int srcregion = ROM_GETFLAGS(romp) >> 24;
/*TODO*///	UINT32 numbytes = ROM_GETLENGTH(romp);
/*TODO*///	UINT32 srcoffs = ROM_GETCRC(romp);
/*TODO*///	UINT8 *srcbase;
/*TODO*///
/*TODO*///	/* make sure we copy within the region space */
/*TODO*///	if (ROM_GETOFFSET(romp) + numbytes > romdata->regionlength)
/*TODO*///	{
/*TODO*///		printf("Error in RomModule definition: COPY out of target memory region space\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* make sure the length was valid */
/*TODO*///	if (numbytes == 0)
/*TODO*///	{
/*TODO*///		printf("Error in RomModule definition: COPY has an invalid length\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* make sure the source was valid */
/*TODO*///	srcbase = memory_region(srcregion);
/*TODO*///	if (!srcbase)
/*TODO*///	{
/*TODO*///		printf("Error in RomModule definition: COPY from an invalid region\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* make sure we find within the region space */
/*TODO*///	if (srcoffs + numbytes > memory_region_length(srcregion))
/*TODO*///	{
/*TODO*///		printf("Error in RomModule definition: COPY out of source memory region space\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* fill the data */
/*TODO*///	memcpy(base, srcbase + srcoffs, numbytes);
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///

    /*-------------------------------------------------
            process_rom_entries - process all ROM entries
            for a region
    -------------------------------------------------*/
    static int process_rom_entries(rom_load_data romdata, RomModule[] romp, int rom_ptr) {
        int/*UINT32*/ lastflags = 0;

        /* loop until we hit the end of this region */
        while (!ROMENTRY_ISREGIONEND(romp, rom_ptr)) {
            /* if this is a continue entry, it's invalid */
            if (ROMENTRY_ISCONTINUE(romp, rom_ptr)) {
                printf("Error in RomModule definition: ROM_CONTINUE not preceded by ROM_LOAD\n");
                if (romdata.file != null) {
                    osd_fclose(romdata.file);
                }
                romdata.file = null;
                return 0;
            }

            /* if this is a reload entry, it's invalid */
            if (ROMENTRY_ISRELOAD(romp, rom_ptr)) {
                printf("Error in RomModule definition: ROM_RELOAD not preceded by ROM_LOAD\n");
                if (romdata.file != null) {
                    osd_fclose(romdata.file);
                }
                romdata.file = null;
                return 0;
            }

            /* handle fills */
            if (ROMENTRY_ISFILL(romp, rom_ptr)) {
                throw new UnsupportedOperationException("Unimplemented");
                /*TODO*///			if (!fill_rom_data(romdata, romp++))
/*TODO*///				goto fatalerror;
            } /* handle copies */ else if (ROMENTRY_ISCOPY(romp, rom_ptr)) {
                throw new UnsupportedOperationException("Unimplemented");
                /*TODO*///			if (!copy_rom_data(romdata, romp++))
/*TODO*///				goto fatalerror;
            } /* handle files */ else if (ROMENTRY_ISFILE(romp, rom_ptr)) {
                int baserom = rom_ptr;
                int explength = 0;

                /* open the file */
                debugload("Opening ROM file: %s\n", ROM_GETNAME(romp, rom_ptr));
                if (!open_rom_file(romdata, romp, rom_ptr)) {
                    handle_missing_file(romdata, romp, rom_ptr);
                }
                /* loop until we run out of reloads */
                do {
                    /* loop until we run out of continues */
                    do {
                        int modified_romp = rom_ptr++;
                        int readresult;

                        /* handle flag inheritance */
                        if (!ROM_INHERITSFLAGS(romp, modified_romp)) {
                            lastflags = romp[modified_romp]._length & ROM_INHERITEDFLAGS;
                        } else {
                            romp[modified_romp]._length = (romp[modified_romp]._length & ~ROM_INHERITEDFLAGS) | lastflags;
                        }

                        explength += UNCOMPACT_LENGTH(romp[modified_romp]._length);

                        /* attempt to read using the modified entry */
                        readresult = read_rom_data(romdata, romp, modified_romp);
                        if (readresult == -1) {
                            if (romdata.file != null) {
                                osd_fclose(romdata.file);
                            }
                            romdata.file = null;
                            return 0;
                        }
                    } while (ROMENTRY_ISCONTINUE(romp, rom_ptr));

                    /* if this was the first use of this file, verify the length and CRC */
                    if (baserom != -1) {
                        debugload("Verifying length (%X) and CRC (%08X)\n", explength, ROM_GETCRC(romp, baserom));
                        verify_length_and_crc(romdata, ROM_GETNAME(romp, baserom), explength, ROM_GETCRC(romp, baserom));
                        debugload("Verify succeeded\n");
                    }

                    /* reseek to the start and clear the baserom so we don't reverify */
                    if (romdata.file != null) {
                        osd_fseek(romdata.file, 0, SEEK_SET);
                    }
                    baserom = -1;
                    explength = 0;
                } while (ROMENTRY_ISRELOAD(romp, rom_ptr));

                /* close the file */
                if (romdata.file != null) {
                    debugload("Closing ROM file\n");
                    osd_fclose(romdata.file);
                    romdata.file = null;
                }
            }
        }
        return 1;
        /*TODO*///
/*TODO*///	/* error case */
/*TODO*///fatalerror:
/*TODO*///	if (romdata->file)
/*TODO*///		osd_fclose(romdata->file);
/*TODO*///	romdata->file = NULL;
/*TODO*///	return 0;
    }


    /*-------------------------------------------------
	readroms - load all the ROMs for this machine
    -------------------------------------------------*/
    public static int readroms() {
        return rom_load_new(Machine.gamedrv.rom);
    }

    /*-------------------------------------------------
            rom_load_new - new, more flexible ROM
            loading system
    -------------------------------------------------*/
    static rom_load_data romdata = new rom_load_data();

    public static int rom_load_new(RomModule[] romp) {
        RomModule[] regionlist = new RomModule[REGION_MAX];
        int region;

        int regnum;
        int romp_ptr = 0;

        /* reset the region list */
        for (regnum = 0; regnum < REGION_MAX; regnum++) {
            regionlist[regnum] = null;
        }

        /* reset the romdata struct */
        romdata = new rom_load_data();
        romdata.romstotal = count_roms(romp, romp_ptr);

        /* loop until we hit the end */
        for (region = romp_ptr, regnum = 0; region != -1; region = rom_next_region(romp, region), regnum++) {
            int regiontype = ROMREGION_GETTYPE(romp, region);

            debugload("Processing region %02X (length=%X)\n", regiontype, ROMREGION_GETLENGTH(romp, region));

            /* the first entry must be a region */
            if (!ROMENTRY_ISREGION(romp, region)) {
                printf("Error: missing ROM_REGION header\n");
                return 1;
            }

            /* if sound is disabled and it's a sound-only region, skip it */
            if (Machine.sample_rate == 0 && ROMREGION_ISSOUNDONLY(romp, region)) {
                continue;
            }

            /* allocate memory for the region */
            if (new_memory_region(regiontype, ROMREGION_GETLENGTH(romp, region), ROMREGION_GETFLAGS(romp, region)) != 0) {
                printf("Error: unable to allocate memory for region %d\n", regiontype);
                return 1;
            }

            /* remember the base and length */
            romdata.regionlength = memory_region_length(regiontype);
            romdata.regionbase = memory_region(regiontype);
            debugload("Allocated %X bytes\n", romdata.regionlength);

            /* clear the region if it's requested */
            if (ROMREGION_ISERASE(romp, region)) {
                throw new UnsupportedOperationException("Unimplemented");
                /*TODO*///			memset(romdata.regionbase, ROMREGION_GETERASEVAL(region), romdata.regionlength);
            } /* or if it's sufficiently small (<= 4MB) */ else if (romdata.regionlength <= 0x400000) {
                memset(romdata.regionbase, 0, romdata.regionlength);
            }
            /* now process the entries in the region */
            if (process_rom_entries(romdata, romp, region + 1) == 0) {
                return 1;
            }
            /* add this region to the list */
            if (regiontype < REGION_MAX) {
                regionlist[regiontype] = romp[region];
            }
        }

        /* post-process the regions */
        for (regnum = 0; regnum < REGION_MAX; regnum++) {
            if (regionlist[regnum] != null) {
                debugload("Post-processing region %02X\n", regnum);
                romdata.regionlength = memory_region_length(regnum);
                romdata.regionbase = memory_region(regnum);
                region_post_process(romdata, regionlist, regnum);
            }
        }
        /* display the results and exit */
        return display_rom_load_results(romdata);
    }


    /*-------------------------------------------------
	printromlist - print list of ROMs
-------------------------------------------------*/
    public static void printromlist(RomModule[] romp, String basename) {
        int chunk;
        int rom;
        int region;
        int rom_ptr = 0;
        if (romp == null) {
            return;
        }

        printf("This is the list of the ROMs required for driver \"%s\".\n"
                + "Name              Size       Checksum\n", basename);

        for (region = rom_ptr; region != -1; region = rom_next_region(romp, region)) {
            for (rom = rom_first_file(romp, region); rom != -1; rom = rom_next_file(romp, rom)) {
                String name = ROM_GETNAME(romp, rom);
                int expchecksum = ROM_GETCRC(romp, rom);
                int length = 0;

                for (chunk = rom_first_chunk(romp, rom); chunk != -1; chunk = rom_next_chunk(romp, chunk)) {
                    length += ROM_GETLENGTH(romp, chunk);
                }

                if (expchecksum != 0) {
                    printf("%-12s  %7d bytes  %08x\n", name, length, expchecksum);
                } else {
                    printf("%-12s  %7d bytes  NO GOOD DUMP KNOWN\n", name, length);
                }
            }
        }
    }

}