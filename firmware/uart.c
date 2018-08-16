#include "uart.h"

static volatile uint8_t usartRxBuf = 0;


void initUART(uint32_t initBR){
	#define USART_BRITRATE_CALC(br) 	( (uint16_t)( (F_CPU/(16UL*(br))) - 1 ) )
	uint16_t br = USART_BRITRATE_CALC(initBR);
	UBRR0H = br >> 8;
	UBRR0L = br & 0xFF;
 	
	UCSR0B=(1<<RXCIE0)|(1<<RXEN0)|(1<<TXEN0); 	//разр. прерыв при приеме, разр приема, разр передачи.
	UCSR0C=(1<<UCSZ01)|(1<<UCSZ00);				//размер слова 8 разрядов
}

void sendByteToUART(uint8_t b){
	while(!(UCSR0A & (1<<UDRE0)));
	UDR0 = b;  
}

uint8_t getByteOfUART(void){
	uint8_t tmp;
	// ATOMIC_BLOCK(ATOMIC_FORCEON){
	ATOMIC_BLOCK(ATOMIC_RESTORESTATE){
		tmp = usartRxBuf;
		usartRxBuf = 0;
	}
	return tmp;  
}

ISR(USART_RX_vect){ 
   usartRxBuf = UDR0;  
} 