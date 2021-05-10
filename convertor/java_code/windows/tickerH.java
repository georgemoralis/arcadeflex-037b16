//============================================================
//
//	ticker.h - Win32 timing code
//
//============================================================

#ifndef __WIN32_TICKER__
#define __WIN32_TICKER__

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package windows;

public class tickerH
{
	
	//============================================================
	//	TYPE DEFINITIONS
	//============================================================
	
	typedef INT64 TICKER;
	
	
	
	//============================================================
	//	MACROS
	//============================================================
	
	#define TICKS_PER_SEC ticks_per_sec
	
	
	
	//============================================================
	//	GLOBAL VARIABLES
	//============================================================
	
	extern TICKER		ticks_per_sec;
	extern TICKER		(*ticker)(void);
	
	#endif
}
