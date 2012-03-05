/*
 * UsbAccessory.h
 *
 *  Created on: 5 бер. 2012
 *      Author: mrco
 */

#ifndef USBACCESSORY_H_
#define USBACCESSORY_H_
#include <string>

class UsbAccessory {
private:
	std::string mDescription;
	std::string mManufacturer;
	std::string mModel;
	std::string mSerial;
	std::string mVersion;
public:
	UsbAccessory();
	virtual ~UsbAccessory();
	// Returns a user visible description of the accessory.
	std::string 	getDescription();
	void			setDescription(const char*value);
	// Returns the manufacturer name of the accessory.
	std::string 	getManufacturer();
	void			setManufacturer(const char*value);
	// Returns the model name of the accessory.
	std::string 	getModel();
	void			setModel(const char*value);
	// 	Returns the unique serial number for the accessory.
	std::string 	getSerial();
	void			setSerial(const char*value);
	// 	Returns the version of the accessory.
	std::string 	getVersion();
	void			setVersion(const char*value);
};

#endif /* USBACCESSORY_H_ */
