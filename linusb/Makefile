CC := g++
OUT    := main
CFLAGS := -Wall -g
PKG_CONFIG=`pkg-config --libs --cflags libusb`
SOURCES=main.cpp UsbManager.cpp UsbAccessory.cpp
OBJS := $(SOURCES:%.cpp=%.o)

all:	$(OUT)

$(OUT): $(OBJS)
	$(CC) $(OBJS) $(PKG_CONFIG) -o $(OUT)

%.o : %.c
	$(CC) $(CFLAGS) $(PKG_CONFIG) -c $<
	
clean:
	rm -f $(OBJS) $(OUT)
