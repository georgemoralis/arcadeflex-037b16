package gr.codebb.arcadeflex.v037b16.sound;

import common.ptr.UBytePtr;
import static gr.codebb.arcadeflex.v037b16.sound.k007232.KDAC_A_PCM_MAX;

public class kdacApcm {

    public kdacApcm() {
        vol[0] = new int[2];
        vol[1] = new int[2];
    }
    public /*unsigned char*/ int[][] vol = new int[KDAC_A_PCM_MAX][];/* volume for the left and right channel */

    public /*unsigned int*/ long[] addr = new long[KDAC_A_PCM_MAX];
    public /*unsigned int*/ long[] start = new long[KDAC_A_PCM_MAX];
    public /*unsigned int*/ long[] step = new long[KDAC_A_PCM_MAX];
    public int[] play = new int[KDAC_A_PCM_MAX];
    public int[] loop = new int[KDAC_A_PCM_MAX];
    public /*unsigned char*/ int[] wreg = new int[0x10];/* write data */

    public UBytePtr[] pcmbuf = new UBytePtr[2];/* Channel A & B pointers */


}
