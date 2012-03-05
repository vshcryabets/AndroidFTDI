/* testlibusb.c  from LQ*/ 

#include <stdio.h>
#include <string.h>
#include <usb.h>
#include "UsbManager.h"

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
	return 0;
}
