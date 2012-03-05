/*
 * UsbManager.h
 *
 *  Created on: 5 бер. 2012
 *      Author: mrco
 */

#ifndef USBMANAGER_H_
#define USBMANAGER_H_
#include "UsbAccessory.h"
#include <vector>

class UsbManager {
public:
	UsbManager();
	virtual ~UsbManager();
	std::vector<UsbAccessory> 	getAccessoryList();
};

#endif /* USBMANAGER_H_ */
