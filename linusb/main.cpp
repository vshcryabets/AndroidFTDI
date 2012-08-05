/* testlibusb.c  from LQ*/ 

#include <stdio.h>
#include <string.h>
#include <usb.h>
#include "UsbManager.h"

/* Definitions for flow control */
#define SIO_RESET          0 /* Reset the port */
#define SIO_MODEM_CTRL     1 /* Set the modem control register */
#define SIO_SET_FLOW_CTRL  2 /* Set flow control register */
#define SIO_SET_BAUD_RATE  3 /* Set baud rate */
#define SIO_SET_DATA       4 /* Set the data characteristics of the port */
/* Requests */
#define SIO_RESET_REQUEST             SIO_RESET
#define SIO_SET_BAUDRATE_REQUEST      SIO_SET_BAUD_RATE
#define SIO_SET_DATA_REQUEST          SIO_SET_DATA
#define SIO_SET_FLOW_CTRL_REQUEST     SIO_SET_FLOW_CTRL
#define SIO_SET_MODEM_CTRL_REQUEST    SIO_MODEM_CTRL
#define SIO_POLL_MODEM_STATUS_REQUEST 0x05
#define SIO_SET_EVENT_CHAR_REQUEST    0x06
#define SIO_SET_ERROR_CHAR_REQUEST    0x07
#define SIO_SET_LATENCY_TIMER_REQUEST 0x09
#define SIO_GET_LATENCY_TIMER_REQUEST 0x0A
#define SIO_SET_BITMODE_REQUEST       0x0B
#define SIO_READ_PINS_REQUEST         0x0C
#define SIO_READ_EEPROM_REQUEST       0x90
#define SIO_WRITE_EEPROM_REQUEST      0x91
#define SIO_ERASE_EEPROM_REQUEST      0x92
#define FTDI_DEVICE_OUT_REQTYPE (USB_TYPE_VENDOR | USB_RECIP_DEVICE | USB_ENDPOINT_OUT)
#define SIO_RESET_SIO 0

struct usb_device* findFTDevice() {
	struct usb_bus *bus;
	struct usb_device *dev;
	int count = 0;
	for (bus = usb_get_busses(); bus; bus = bus->next)
	{
		for (dev = bus->devices; dev; dev = dev->next)
		{
			if (dev->descriptor.idVendor == 0x0403
					&& dev->descriptor.idProduct == 0x6001)
			{
				return dev;
			}
		}
	}
	return NULL;
}

/**
    Resets the ftdi device.

    \param ftdi pointer to ftdi_context

    \retval  0: all fine
    \retval -1: FTDI reset failed
    \retval -2: USB device unavailable
*/
int ftdi_usb_reset(struct usb_dev_handle *dev)
{
    if ( dev == NULL)
        printf("ERRO:USB device unavailable\n");

    if (usb_control_msg(dev, FTDI_DEVICE_OUT_REQTYPE,
                        SIO_RESET_REQUEST, SIO_RESET_SIO,
                        0, NULL, 0, 5000) != 0)
        printf("ERRO:FTDI reset failed\n");
    return 0;
}

void openFTDevice(struct usb_device* usb_dev) {
	struct usb_dev_handle* handle;

    if (!( handle = usb_open(usb_dev)))
    	return;

	if (usb_claim_interface(handle, 0) != 0) {
		printf("unable to claim usb device. Make sure the default FTDI driver is not in use");
		return;
	}

	if (ftdi_usb_reset (handle) != 0)
	{
//		ftdi_usb_close_internal (ftdi);
		printf("ftdi_usb_reset failed\n");
		return;
	}

	// Try to guess chip type
	// Bug in the BM type chips: bcdDevice is 0x200 for serial == 0
	if ( usb_dev->descriptor.bcdDevice == 0x400 || (usb_dev->descriptor.bcdDevice == 0x200
			&& usb_dev->descriptor.iSerialNumber == 0)) {
		printf("TYPE_BM");
	}
	else if (usb_dev->descriptor.bcdDevice == 0x200)
		printf("TYPE_AM");
	else if (usb_dev->descriptor.bcdDevice == 0x500)
		printf("TYPE_2232C");
	else if (usb_dev->descriptor.bcdDevice == 0x600)
		printf("TYPE_R");
	else if (usb_dev->descriptor.bcdDevice == 0x700)
		printf("TYPE_2232H");
	else if (usb_dev->descriptor.bcdDevice == 0x800)
		printf("TYPE_4232H");

//	// Set default interface on dual/quad type chips
//	switch(ftdi->type)
//	{
//	case TYPE_2232C:
//	case TYPE_2232H:
//	case TYPE_4232H:
//		if (!ftdi->index)
//			ftdi->index = INTERFACE_A;
//		break;
//	default:
//		break;
//	}

	// Determine maximum packet size
//	ftdi->max_packet_size = _ftdi_determine_max_packet_size(ftdi, dev);
//
//	if (ftdi_set_baudrate (ftdi, 9600) != 0)
//	{
//		ftdi_usb_close_internal (ftdi);
//		ftdi_error_return(-7, "set baudrate failed");
//	}
//
//	ftdi_error_return(0, "all fine");
}

int main(void) 
{ 
	UsbManager manager;
	std::vector<UsbAccessory> devices = manager.getAccessoryList();
	for (std::vector<UsbAccessory>::iterator i = devices.begin();
			i != devices.end();
			++i)
	{
		printf("Description: %s\n", (*i).getDescription().c_str());
		printf("Manufacturer: %s\n", (*i).getManufacturer().c_str());
		printf("Model: %s\n", (*i).getModel().c_str());
		printf("Serial: %s\n", (*i).getSerial().c_str());
		printf("Version: %s\n", (*i).getVersion().c_str());

	}

	struct usb_device *dev = findFTDevice();
	if ( dev != NULL )
		openFTDevice(dev);
	printf("Dev=%p\n",dev);
	return 0;
}
