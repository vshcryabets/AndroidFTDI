package com.v2soft.androidftdi;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

public class FTDIDevice {
//	#define FTDI_DEVICE_OUT_REQTYPE (USB_TYPE_VENDOR | USB_RECIP_DEVICE | USB_ENDPOINT_OUT)
//	#define FTDI_DEVICE_IN_REQTYPE (USB_TYPE_VENDOR | USB_RECIP_DEVICE | USB_ENDPOINT_IN)
	private static final int SIOReset = 0; // reset device port
//	#define SIO_MODEM_CTRL     1 /* Set the modem control register */
//	#define SIO_SET_FLOW_CTRL  2 /* Set flow control register */
	private static final int SIOSetBaudRate = 3;
//	#define SIO_SET_DATA       4 /* Set the data characteristics of the port */
	//------------------------------------------------------------------------------------------------
	// Enums
	//------------------------------------------------------------------------------------------------
	private enum FtdiChiType { TypeAM, TypeBM, Type2232C, TypeR, Type2232H, Type4232H};
	//------------------------------------------------------------------------------------------------
	// Private fields
	//------------------------------------------------------------------------------------------------
	private UsbDevice mDevice;
	private UsbDeviceConnection mConnection;
	private UsbManager mManager;
	private FtdiChiType mType;
	
	public FTDIDevice(UsbManager manager, UsbDevice device, int interfaceIdx) {
		if ( manager == null ) throw new NullPointerException("UsbManager is null");
		if ( device == null ) throw new NullPointerException("Device is null");
		mManager = manager;
		mConnection = null;
		setDevice(device, interfaceIdx);
	}

	public UsbDevice getDevice() {
		return mDevice;
	}

	public void setDevice(UsbDevice mDevice, int interfaceIdx) {
		if ( this.mConnection != null ) {
			closeConnection();
		}
		this.mDevice = mDevice;
		int id = mDevice.getDeviceId();
		mDevice.
//		if (dev->descriptor.bcdDevice == 0x400 || (dev->descriptor.bcdDevice == 0x200
//	            && dev->descriptor.iSerialNumber == 0))
//	        ftdi->type = TYPE_BM;
//	    else if (dev->descriptor.bcdDevice == 0x200)
//	        ftdi->type = TYPE_AM;
//	    else if (dev->descriptor.bcdDevice == 0x500)
//	        ftdi->type = TYPE_2232C;
//	    else if (dev->descriptor.bcdDevice == 0x600)
//	        ftdi->type = TYPE_R;
//	    else if (dev->descriptor.bcdDevice == 0x700)
//	        ftdi->type = TYPE_2232H;
//	    else if (dev->descriptor.bcdDevice == 0x800)
//	        ftdi->type = TYPE_4232H;		
		openConnection(interfaceIdx);
	}

	private void openConnection(int interfaceIdx) {
		mConnection = mManager.openDevice(mDevice);
		if ( !mConnection.claimInterface(mDevice.getInterface(interfaceIdx), true)) {
			throw new ExceptionInInitializerError("Can't claim interface");
		}
		resetDevice();
		clearRx();
		clearTx();
		setBaudRate(9600);
	}

	private void setBaudRate(int i) {
		if ( mConnection == null ) throw new NullPointerException("Connection is null");
		mConnection.controlTransfer(0x40, SIOSetBaudRate, 2, 0, null, 0, 0);//clear Tx
	}

	private void clearTx() {
		if ( mConnection == null ) throw new NullPointerException("Connection is null");
		mConnection.controlTransfer(0x40, SIOReset, 2, 0, null, 0, 0);//clear Tx
	}

	private void clearRx() {
		if ( mConnection == null ) throw new NullPointerException("Connection is null");
		mConnection.controlTransfer(0x40, SIOReset, 1, 0, null, 0, 0);//clear Rx
	}

	private void resetDevice() {
		if ( mConnection == null ) throw new NullPointerException("Connection is null");
		mConnection.controlTransfer(0x40, SIOReset, 0, 0, null, 0, 0);
	}

	private void closeConnection() {
		if ( mConnection == null ) return;
		mConnection.close();
		mConnection = null;
	}
}
