#include <avr/io.h>
#include <util/delay.h>
#include <avr/interrupt.h>
#include <stdbool.h>
#include "bit_macros.h"
#include "uart.h"


/* нормально открытый геркон */
#define		GERKON_SENSOR_PORT		PORTB
#define		GERKON_SENSOR_DDR		DDRB
#define		GERKON_SENSOR_PIN		PINB
#define		GERKON_SENSOR 			2

/* Logging led */
#define		STATE_LED_PORT		PORTB
#define		STATE_LED_DDR		DDRB
#define 	STATE_LED 			5

#define		REQUEST_INIT_DEVICE		0x31
#define		RESPONSE_INIT_DEVICE	0x70
#define		CMD_INCREMENT_COUNTER	0x55

bool isConnected = false;
uint8_t rxByte = 0;


void initIO(void);
void initExtInt0(void);
void blinkLogLed(void);



ISR(INT0_vect){
	// start measuring
	// if (btnStateFlag == false){
	// 	btnStateFlag = true;
	// 	pcCommand = DO_START_SM;
	// // stop measuring
	// } else if (btnStateFlag == true){
	// 	btnStateFlag = false;
	// 	pcCommand = DO_STOP_SM;
	// }

	

	// if(isConnected) 

	// _delay_ms(150); // антидребизг
	sendByteToUART(CMD_INCREMENT_COUNTER);

	// high(STATE_LED_PORT, STATE_LED);
	// _delay_ms(10);
	// low(STATE_LED_PORT, STATE_LED);


}


int main(void){
	cli();
	initIO();
	initExtInt0();
	initUART(BAUDE_RATE_9600);
	sei();


	while(1){
		rxByte = getByteOfUART();

		if (rxByte == REQUEST_INIT_DEVICE){
			// blinkLogLed();
			sendByteToUART(RESPONSE_INIT_DEVICE);
			isConnected = true;
		}

		// alternate ISR
		// if (isHigh(EIFR, INTF0)){
		// 	sendByteToUART(CMD_INCREMENT_COUNTER);
		// 	high(EIFR, INTF0);
		// 	_delay_ms(50);
		// }


	}

}

void initIO(void){
	/* Настриваем на ВЫХОД порт для подключения logging led */
	high(STATE_LED_DDR, STATE_LED);

	/* Input for gerkon */
	// low(GERKON_SENSOR_DDR, GERKON_SENSOR);
	// /* Pull-up resistance is enable */
	// high(GERKON_SENSOR_PORT, GERKON_SENSOR);
}

// Настройка прерывания от внешнего источника (геркона)
void initExtInt0(void){
	// срабатывание по низкому уровню на выводе INT0

	// high(EICRA, ISC00);
	// low(EICRA, ISC01);

	high(EICRA, ISC00);
	high(EICRA, ISC01);

	// EICRA = (1<<ISC01)|(1<<ISC00);
	// резрещаем внешние прерывания
	high(EIMSK, INT0);
}

void blinkLogLed(void){
	high(STATE_LED_PORT, STATE_LED);
	_delay_ms(1000);
	low(STATE_LED_PORT, STATE_LED);
}