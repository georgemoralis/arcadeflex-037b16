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



 


 


 

}
