#include <avr/io.h>
#include <util/atomic.h>


// List of baud rates.
// Used as parametr in initUSART function. 
#define 	BAUDE_RATE_1200		1200
#define 	BAUDE_RATE_4800		4800
#define 	BAUDE_RATE_9600		9600
#define 	BAUDE_RATE_19200	19200
#define 	BAUDE_RATE_38400	38400
#define 	BAUDE_RATE_115200	115200
#define 	BAUDE_RATE_256000	256000


void initUART(uint32_t initBR);

void sendByteToUART(uint8_t b);

uint8_t getByteOfUART(void);