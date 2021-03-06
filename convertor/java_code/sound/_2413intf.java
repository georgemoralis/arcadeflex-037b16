/****************************************************************

	MAME / MESS functions

****************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package sound;

public class _2413intf
{
	
	static OPLL *opll[MAX_2413];
	static int stream[MAX_2413], ym_latch[MAX_2413], num;
	
	static void YM2413_update (int ch, INT16 *buffer, int length)
	{
		while (length--) *buffer++ = OPLL_calc (opll[ch]);
	}
	
	public static ShStartPtr YM2413_sh_start = new ShStartPtr() { public int handler(MachineSound msound) 
	{
		const struct YM2413interface *intf = msound.sound_interface;
		int i;
		char buf[40];
	
		OPLL_init (intf.baseclock/2, Machine.sample_rate);
		num = intf.num;
	
		for (i=0;i<num;i++)
			{
			opll[i] = OPLL_new ();
			if (!opll[i]) return 1;
			OPLL_reset (opll[i]);
			OPLL_reset_patch (opll[i]);
	
			if (num > 1)
				sprintf (buf, "YM-2413 #%d", i);
			else
				strcpy (buf, "YM-2413");
	
			stream[i] = stream_init (buf, intf.mixing_level[i],
				Machine.sample_rate, i, YM2413_update);
			}
	
		return 0;
	} };
	
	public static ShStopPtr YM2413_sh_stop = new ShStopPtr() { public void handler() 
	{
		int i;
	
		for (i=0;i<num;i++)
		{
			OPLL_delete (opll[i]);
		}
		OPLL_close ();
	} };
	
	void YM2413_sh_reset (void)
	{
		int i;
	
		for (i=0;i<num;i++)
		{
			OPLL_reset (opll[i]);
			OPLL_reset_patch (opll[i]);
		}
	}
	
	public static WriteHandlerPtr YM2413_register_port_0_w = new WriteHandlerPtr() {public void handler(int offset, int data) { ym_latch[0] = data; } };
	public static WriteHandlerPtr YM2413_register_port_1_w = new WriteHandlerPtr() {public void handler(int offset, int data) { ym_latch[1] = data; } };
	public static WriteHandlerPtr YM2413_register_port_2_w = new WriteHandlerPtr() {public void handler(int offset, int data) { ym_latch[2] = data; } };
	public static WriteHandlerPtr YM2413_register_port_3_w = new WriteHandlerPtr() {public void handler(int offset, int data) { ym_latch[3] = data; } };
	
	static void YM2413_write_reg (int chip, int data)
	{
		OPLL_writeReg (opll[chip], ym_latch[chip], data);
		stream_update(stream[chip], chip);
	}
	
	public static WriteHandlerPtr YM2413_data_port_0_w = new WriteHandlerPtr() {public void handler(int offset, int data) { YM2413_write_reg (0, data); } };
	public static WriteHandlerPtr YM2413_data_port_1_w = new WriteHandlerPtr() {public void handler(int offset, int data) { YM2413_write_reg (1, data); } };
	public static WriteHandlerPtr YM2413_data_port_2_w = new WriteHandlerPtr() {public void handler(int offset, int data) { YM2413_write_reg (2, data); } };
	public static WriteHandlerPtr YM2413_data_port_3_w = new WriteHandlerPtr() {public void handler(int offset, int data) { YM2413_write_reg (3, data); } };
	
	WRITE16_HANDLER( YM2413_register_port_0_lsb_w ) { if (ACCESSING_LSB != 0) YM2413_register_port_0_w(offset,data & 0xff); }
	WRITE16_HANDLER( YM2413_register_port_1_lsb_w ) { if (ACCESSING_LSB != 0) YM2413_register_port_1_w(offset,data & 0xff); }
	WRITE16_HANDLER( YM2413_register_port_2_lsb_w ) { if (ACCESSING_LSB != 0) YM2413_register_port_2_w(offset,data & 0xff); }
	WRITE16_HANDLER( YM2413_register_port_3_lsb_w ) { if (ACCESSING_LSB != 0) YM2413_register_port_3_w(offset,data & 0xff); }
	WRITE16_HANDLER( YM2413_data_port_0_lsb_w ) { if (ACCESSING_LSB != 0) YM2413_data_port_0_w(offset,data & 0xff); }
	WRITE16_HANDLER( YM2413_data_port_1_lsb_w ) { if (ACCESSING_LSB != 0) YM2413_data_port_1_w(offset,data & 0xff); }
	WRITE16_HANDLER( YM2413_data_port_2_lsb_w ) { if (ACCESSING_LSB != 0) YM2413_data_port_2_w(offset,data & 0xff); }
	WRITE16_HANDLER( YM2413_data_port_3_lsb_w ) { if (ACCESSING_LSB != 0) YM2413_data_port_3_w(offset,data & 0xff); }
}
