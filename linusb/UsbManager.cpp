/*
 * UsbManager.cpp
 *
 *  Created on: 5 бер. 2012
 *      Author: mrco
 */

#include "UsbManager.h"
#include <usb.h>
#include <vector>

UsbManager::UsbManager() {
	usb_init();
}

UsbManager::~UsbManager() {
	// TODO Auto-generated destructor stub
}

std::vector<UsbAccessory> 	UsbManager::getAccessoryList() {
	struct usb_bus *bus;
	struct usb_device *dev;
	usb_find_busses();
	usb_find_devices();
//    if (usb_find_busses() < 0)
//        printf("usb_find_busses() failed\n");
//    if (usb_find_devices() < 0)
//        printf("usb_find_devices() failed\n");

	std::vector<UsbAccessory> result;
	for (bus = usb_busses; bus; bus = bus->next) {
		for (dev = bus->devices; dev; dev = dev->next) {
			int ret, i;
			char string[256];
			usb_dev_handle *udev;
			UsbAccessory accessory;

			//			printf("%s/%s %04X/%04X\n", bus->dirname, dev->filename,
			//					dev->descriptor.idVendor, dev->descriptor.idProduct);

			udev = usb_open(dev);
			if (udev) {
				if (dev->descriptor.iManufacturer) {
					ret = usb_get_string_simple(udev, dev->descriptor.iManufacturer, string, sizeof(string));
					if (ret > 0)
						accessory.setManufacturer(string);
					else
						accessory.setManufacturer("unknown");
				}

				if (dev->descriptor.iProduct) {
					ret = usb_get_string_simple(udev, dev->descriptor.iProduct, string, sizeof(string));
					if (ret > 0)
						accessory.setModel(string);
					else
						accessory.setModel("unknown");
				}

				if (dev->descriptor.iSerialNumber) {
					ret = usb_get_string_simple(udev, dev->descriptor.iSerialNumber, string, sizeof(string));
					if (ret > 0)
						accessory.setSerial(string);
					else
						accessory.setSerial("00");
				}

				usb_close (udev);
			}

			if (!dev->config) {
				accessory.setDescription(string);
				continue;
			}

			//			for (i = 0; i < dev->descriptor.bNumConfigurations; i++)
			//				print_configuration(&dev->config[i]);
			result.push_back(accessory);
		}

	}
	return result;
}
