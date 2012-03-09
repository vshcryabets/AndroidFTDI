/*
 * UsbControllerActivity.java
 * This file is part of UsbController
 *
 * Copyright (C) 2012 - Manuel Di Cerbo
 *
 * UsbController is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * UsbController is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UsbController. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * main.c
 *  (c) Nexus-Computing GmbH Switzerland
 *  Created on: Feb 02, 2012
 *      Author: Manuel Di Cerbo
 */
#include <stdio.h>
#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>

#define LED PB5 // LED is on Pin 13 or Pin 5 of Port B
/*
 * UART-Initialization from www.mikrocontroller.net
 * Hint: They are awesome! :-)
 */

#ifndef F_CPU
#warning "F_CPU was not defined, defining it now as 16000000"
#define F_CPU 16000000UL
#endif

#define BAUD 9600UL      // baud rate
// Calculations
#define UBRR_VAL ((F_CPU+BAUD*8)/(BAUD*16)-1)   // smart rounding
#define BAUD_REAL (F_CPU/(16*(UBRR_VAL+1)))     // real baud rate
#define BAUD_ERROR ((BAUD_REAL*1000)/BAUD) // error in parts per mill, 1000 = no error
#if ((BAUD_ERROR<990) || (BAUD_ERROR>1010))
#error Error in baud rate greater than 1%!
#endif

void uart_init(void) {
	UBRR0H = UBRR_VAL >> 8;
	UBRR0L = UBRR_VAL & 0xFF;

	UCSR0C = (0 << UMSEL01) | (0 << UMSEL00) | (1 << UCSZ01) | (1 << UCSZ00); // asynchron 8N1
	UCSR0B |= (1 << RXEN0); // enable UART RX
	UCSR0B |= (1 << TXEN0); // enable UART TX
	UCSR0B |= (1 << RXCIE0); //interrupt enable
}

/* Receive symbol, not necessary for this example, using interrupt instead*/
uint8_t uart_getc(void) {
	while (!(UCSR0A & (1 << RXC0)))
		// wait until symbol is ready
		;
	return UDR0; // return symbol
}

uint8_t uart_putc(unsigned char data) {
	/* Wait for empty transmit buffer */
	while (!(UCSR0A & (1 << UDRE0)))
		;
	/* Put data into buffer, sends the data */
	UDR0 = data;
	return 0;
}


void initIO(void) {
	DDRD |= (1 << DDD3);
	DDRB = 0xff; //all out
}


volatile uint8_t data = 10;

int main(void) {
	initIO();
	uart_init();
	sei();

	uint8_t i = 0;
	volatile uint8_t pause;
	for(;;) {
		pause = data;
		PORTB |= (1 << LED);
		for(i = 0; i < pause; i++)
			_delay_us(10);
		PORTB &= ~(1 << LED);
		for(i = 0; i < 255-pause; i++)
			_delay_us(10);
	}
	return 0; // never reached
}

ISR(USART_RX_vect) {//attention to the name and argument here, won't work otherwise
	data = UDR0;//UDR0 needs to be read
}
