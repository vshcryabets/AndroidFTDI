/*
 * UsbAccessory.cpp
 *
 *  Created on: 5 бер. 2012
 *      Author: mrco
 */

#include "UsbAccessory.h"

UsbAccessory::UsbAccessory() {
	// TODO Auto-generated constructor stub

}

UsbAccessory::~UsbAccessory() {
	// TODO Auto-generated destructor stub
}

// Returns a user visible description of the accessory.
std::string 	UsbAccessory::getDescription() {
	return mDescription;
}
void			UsbAccessory::setDescription(const char*value) {
	mDescription = value;
}
// Returns the manufacturer name of the accessory.
std::string 	UsbAccessory::getManufacturer() {
	return mManufacturer;
}
void			UsbAccessory::setManufacturer(const char*value) {
	mManufacturer = value;
}
// Returns the model name of the accessory.
std::string 	UsbAccessory::getModel() {
	return mModel;
}
void			UsbAccessory::setModel(const char*value) {
	mModel = value;
}
// 	Returns the unique serial number for the accessory.
std::string 	UsbAccessory::getSerial() {
	return mSerial;
}
void			UsbAccessory::setSerial(const char*value) {
	mSerial = value;
}
// 	Returns the version of the accessory.
std::string 	UsbAccessory::getVersion() {
	return mVersion;
}
void			UsbAccessory::setVersion(const char*value) {
	mVersion = value;
}

