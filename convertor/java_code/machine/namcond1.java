/***************************************************************************

  Namco ND-1

  machine.c

  Functions to emulate general aspects of the machine
  (RAM, ROM, interrupts, I/O ports)

***************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package machine;

public class namcond1
{
	
	/* Perform basic machine initialisation */
	
	static UINT8 coin_state;
	static UINT8 coin_count[4];
	
	public static InitMachinePtr namcond1_init_machine = new InitMachinePtr() { public void handler() 
	{
	#ifdef MAME_DEBUG
	    /*UBytePtr ROM = memory_region(REGION_CPU1);*/
	    /*unsigned int debug_trigger_addr;*/
	    /*int             i;*/
	
	#if 0
	    // debug trigger patch
	    // insert a "move.b $B0000000,D2" into the code
	    debug_trigger_addr = 0x152d4; // after ygv_init
	    ROM[debug_trigger_addr++] = 0x39;
	    ROM[debug_trigger_addr++] = 0x14;
	    ROM[debug_trigger_addr++] = 0xB0;
	    ROM[debug_trigger_addr++] = 0x00;
	    ROM[debug_trigger_addr++] = 0x00;
	    ROM[debug_trigger_addr++] = 0x00;
	#endif
	#endif
	
	    // initialise MCU states
	    coin_state = 0;
	    coin_count[0] = coin_count[1] =
	    coin_count[2] = coin_count[3] = 0;
	} };
	
	public static InterruptPtr namcond1_vb_interrupt = new InterruptPtr() { public int handler() 
	{
	    return( m68_level1_irq() );
	} };
	
	// instance of the shared ram pointer
	data16_t *namcond1_shared_ram;
	
	READ16_HANDLER( namcond1_shared_ram_r )
	{
	    static UINT8 plyr1 = 0, plyr2 = 0;
	
	    data16_t data;
		UINT8 poll_coins;
	    UINT8   current, pressed;
	
	    switch( offset )
	    {
	        case (0>>1) : // sub mailbox - sub busy
	            data = 0;    // sub not busy
	            break;
	
	        case (2>>1) : // test switch
	            // high bit is set/cleared by MCU
	            data = ( 1 << 15 ) | readinputport( 2 );
	            break;
	
	        case (4>>1) : // plyr1
	            current = readinputport( 0 );
	            pressed = plyr1 & ( plyr1 ^ current );
	            plyr1 = current;
	            data = ( ~current << 8 ) | pressed;
	            break;
	
	        case (6>>1) : // plyr2
	            current = readinputport( 1 );
	            pressed = plyr2 & ( plyr2 ^ current );
	            plyr2 = current;
	            data = ( ~current << 8 ) | pressed;
	            break;
	
		    case (0x18>>1) : // coin #1
			    data = (coin_count[0]<<8) | coin_count[1];
	            break;
	
	        case (0x1a>>1) : // coin #2
			    data = (coin_count[2]<<8) | coin_count[3];
	            break;
	
	        case (0x51>>1) : // player 1 latched
	            plyr1 = input_port_1_r( 0 );
	            data = plyr1;
	            break;
	
	        case (0x52>>1) : // player 2 latched
	            plyr2 = input_port_1_r( 0 );
	            data =  plyr2 << 8;
	            break;
	
	        case (0x580>>1) : // sub-cpu signal
	            data = 0;    // sub finished
	            break;
	
	        default :
	            data = namcond1_shared_ram[offset];
	            break;
	    }
	
	    // Is this the best place to so this? maybe not...
		poll_coins = readinputport( 3 );
		if( ( poll_coins & 0x8 ) & ~( coin_state & 0x8 ) ) coin_count[0]++;
		if( ( poll_coins & 0x4 ) & ~( coin_state & 0x4 ) ) coin_count[1]++;
		if( ( poll_coins & 0x2 ) & ~( coin_state & 0x2 ) ) coin_count[2]++;
		if( ( poll_coins & 0x1 ) & ~( coin_state & 0x1 ) ) coin_count[3]++;
		coin_state = poll_coins;
	
	    return( data );
	}
	
	// $c3ff00-$c3ffff
	READ16_HANDLER( namcond1_cuskey_r )
	{
	    switch( offset )
	    {
	        // this address returns a jump vector inside ISR2
	        // - if zero then the ISR returns without jumping
	        case (0x2e>>1):
	            return( 0x0000 );
	        case (0x30>>1):
	            return( 0x0000 );
	
	        default :
	            logerror( "offset $%X accessed from $%X\n",
	                      offset<<1, cpu_get_pc() );
	            return( 0 );
	    }
	}
	
	WRITE16_HANDLER( namcond1_shared_ram_w )
	{
	    switch( offset )
	    {
	        default :
	            COMBINE_DATA( namcond1_shared_ram + offset );
	            break;
	    }
	}
	
	/* not used at this point */
	void namcond1_nvramhandler( void *f, int state )
	{
	  UBytePtr eeprom = memory_region(REGION_CPU1);
	
	  if( f == 0 )
	    return;
	
	  if( state == 0 ) {
	    /* read eeprom contents */
	    osd_fread( f, &eeprom[0x400600], 0x90 );
	  }
	  else {
	    /* write eeprom contents */
	    osd_fwrite( f, &eeprom[0x400600], 0x90 );
	  }
	}
	
}
