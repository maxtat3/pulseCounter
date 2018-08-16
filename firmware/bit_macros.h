#ifndef BIT_MACROS_H
#define BIT_MACROS_H


// DDRxn 	PORTx	I/Output 	Comment
// 0 		0 		I (Input) 	Вход. Высокоимпендансный вход. 
// 0 		1 		I (Input) 	Вход. Подтянуто внутренне сопротивление.
// 1 		0 		O (Output)	Выход. На выходе низкий уровень.
// 1 		1 		O (Output)	Выход. На выходе высокий уровень.

#define		high(reg, bit)		reg |= (1<<bit)			// Set bit in register
#define		low(reg, bit)		reg &= (~(1<<bit))		// Clear bit in register
#define		isHigh(reg, bit)	((reg & (1<<bit)) != 0)		// Check is set bit in register
#define		isLow(reg, bit)		((reg & (1<<bit)) == 0)		// Check is clear bit in register


#endif //BIT_MACROS_H