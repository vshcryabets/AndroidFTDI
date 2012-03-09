CC=avr-gcc
OBJCOPY=avr-objcopy
SRCS= 
CFLAGS=-g -mmcu=attiny13a -Os
OBJS=$(SRCS:.c=.o)
OUTS=$(SRCS:.c=.out)
.SUFFIXES: .c .o

.c.o:
	$(CC) $(CFLAGS) -c $<
.c.out: $(OBJS)
	$(CC) $(CFLAGS) -o control.out -Wl,-Map,control.map control.o
##	$(CC) $(CFLAGS) -c $<
all: $(OBJS) $(OUTS)
	$(OBJCOPY) -j .text -O ihex control.out rom.hex
	
clean:
	rm *.o *.map *.hex
	rm test
