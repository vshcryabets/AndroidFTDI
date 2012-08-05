package com.v2soft.androidftdi;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

/**
 * 
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class FTDIDevice {
	//	#define FTDI_DEVICE_OUT_REQTYPE (USB_TYPE_VENDOR | USB_RECIP_DEVICE | USB_ENDPOINT_OUT)
	//	#define FTDI_DEVICE_IN_REQTYPE (USB_TYPE_VENDOR | USB_RECIP_DEVICE | USB_ENDPOINT_IN)
	private static final int SIOReset = 0; // reset device port
	//	#define SIO_MODEM_CTRL     1 /* Set the modem control register */
	//	#define SIO_SET_FLOW_CTRL  2 /* Set flow control register */
	private static final int SIOSetBaudRate = 3;
	//	#define SIO_SET_DATA       4 /* Set the data characteristics of the port */

	private static final int DeviceOutReq = 0x40;
	private static final int FtdiClock = 24000000;
	//------------------------------------------------------------------------------------------------
	// Enums
	//------------------------------------------------------------------------------------------------
	public enum FtdiChipType { TypeAM, TypeBM, Type2232C, TypeR, Type2232H, Type4232H};
	//------------------------------------------------------------------------------------------------
	// Private fields
	//------------------------------------------------------------------------------------------------
	private UsbDevice mDevice;
	private UsbDeviceConnection mConnection;
	private UsbManager mManager;
	private FtdiChipType mType;
	private UsbEndpoint mOut = null;
	private UsbEndpoint mIn = null;
	//	private boolean isBitbangEnabled;

	public FTDIDevice(UsbManager manager, UsbDevice device, int interfaceIdx, FtdiChipType chipType) {
		if ( manager == null ) throw new NullPointerException("UsbManager is null");
		if ( device == null ) throw new NullPointerException("Device is null");
		mManager = manager;
		mConnection = null;
		setDevice(device, interfaceIdx, chipType);
	}

	public UsbDevice getDevice() {
		return mDevice;
	}

	public void setDevice(UsbDevice mDevice, int interfaceIdx, FtdiChipType chipType) {
		if ( this.mConnection != null ) {
			closeConnection();
		}
		this.mDevice = mDevice;
		mType = chipType;
		openConnection(interfaceIdx);
	}

	private void openConnection(int interfaceIdx) {
		mConnection = mManager.openDevice(mDevice);
		final UsbInterface usbIf = mDevice.getInterface(0);
		if ( !mConnection.claimInterface(usbIf, true)) {
			throw new ExceptionInInitializerError("Can't claim interface");
		}
		resetDevice();
		clearRx();
		clearTx();
		setBaudRate(9600);
		
		for (int i = 0; i < usbIf.getEndpointCount(); i++) {
			if (usbIf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
				if (usbIf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN)
					mIn = usbIf.getEndpoint(i);
				else
					mOut = usbIf.getEndpoint(i);
			}
		}

	}

	public void setBaudRate(int baudRate) {
		if ( mConnection == null ) throw new NullPointerException("Connection is null");
		//		if ( isBitbangEnabled ) {
		//			baudRate = baudRate*4;
		//		}
		int realRate = 0x4138; // convertRate(baudRate);
		mConnection.controlTransfer(DeviceOutReq, SIOSetBaudRate, realRate, 0, null, 0, 0);
	}

	/*private int convertRate(int baudRate) {
		final int am_adjust_up[] = new int[]{0, 0, 0, 1, 0, 3, 2, 1};
		final int am_adjust_dn[] = new int[]{0, 0, 0, 1, 0, 1, 2, 3};

		int divisor = FtdiClock / baudRate;
		if ( mType == FtdiChiType.TypeAM) {
			// Round down to supported fraction (AM only)
			divisor -= am_adjust_dn[divisor & 7];			
		}
		// TODO this is code from libftdi!!!
		// Try this divisor and the one above it (because division rounds down)
		int best_divisor = 0;
		int best_baud = 0;
		int best_baud_diff = 0;
		for (int i = 0; i < 2; i++)
		{
			int try_divisor = divisor + i;
			int baud_estimate;
			int baud_diff;

			// Round up to supported divisor value
			if (try_divisor <= 8)
			{
				// Round up to minimum supported divisor
				try_divisor = 8;
			}
			else if (mType != FtdiChiType.TypeAM && try_divisor < 12)
			{
				// BM doesn't support divisors 9 through 11 inclusive
				try_divisor = 12;
			}
			else if (divisor < 16)
			{
				// AM doesn't support divisors 9 through 15 inclusive
				try_divisor = 16;
			}
			else
			{
				if (mType == FtdiChiType.TypeAM)
				{
					// Round up to supported fraction (AM only)
					try_divisor += am_adjust_up[try_divisor & 7];
					if (try_divisor > 0x1FFF8)
					{
						// Round down to maximum supported divisor value (for AM)
						try_divisor = 0x1FFF8;
					}
				}
				else
				{
					if (try_divisor > 0x1FFFF)
					{
						// Round down to maximum supported divisor value (for BM)
						try_divisor = 0x1FFFF;
					}
				}
			}
			// Get estimated baud rate (to nearest integer)
			baud_estimate = (FtdiClock + (try_divisor / 2)) / try_divisor;
			// Get absolute difference from requested baud rate
			if (baud_estimate < baudRate)
			{
				baud_diff = baudRate - baud_estimate;
			}
			else
			{
				baud_diff = baud_estimate - baudRate;
			}
			if (i == 0 || baud_diff < best_baud_diff)
			{
				// Closest to requested baud rate so far
				best_divisor = try_divisor;
				best_baud = baud_estimate;
				best_baud_diff = baud_diff;
				if (baud_diff == 0)
				{
					// Spot on! No point trying
					break;
				}
			}
		}
	    // Encode the best divisor value
	    final int frac_code[] = new int[]{0, 3, 2, 4, 1, 5, 6, 7};
	    long encoded_divisor = (best_divisor >> 3) | (frac_code[best_divisor & 7] << 14);
	    // Deal with special cases for encoded value
	    if (encoded_divisor == 1)
	    {
	        encoded_divisor = 0;    // 3000000 baud
	    }
	    else if (encoded_divisor == 0x4001)
	    {
	        encoded_divisor = 1;    // 2000000 baud (BM only)
	    }
	    // Split into "value" and "index" values
	    *value = (unsigned short)(encoded_divisor & 0xFFFF);
	    if (ftdi->type == TYPE_2232C || ftdi->type == TYPE_2232H || ftdi->type == TYPE_4232H)
	    {
	        *index = (unsigned short)(encoded_divisor >> 8);
	        *index &= 0xFF00;
	        *index |= ftdi->index;
	    }
	    else
	        *index = (unsigned short)(encoded_divisor >> 16);

	    // Return the nearest baud rate
	    return best_baud;
	}*/

	private void clearTx() {
		if ( mConnection == null ) throw new NullPointerException("Connection is null");
		mConnection.controlTransfer(DeviceOutReq, SIOReset, 2, 0, null, 0, 0);//clear Tx
	}

	private void clearRx() {
		if ( mConnection == null ) throw new NullPointerException("Connection is null");
		mConnection.controlTransfer(DeviceOutReq, SIOReset, 1, 0, null, 0, 0);//clear Rx
	}

	private void resetDevice() {
		if ( mConnection == null ) throw new NullPointerException("Connection is null");
		mConnection.controlTransfer(DeviceOutReq, SIOReset, 0, 0, null, 0, 0);
	}

	private void closeConnection() {
		if ( mConnection == null ) return;
		mConnection.close();
		mConnection = null;
	}

	/**
	 * Write data to FTDI
	 * @param data byte buffer
	 */
	public int write(byte[] data) {
		return write(data, 0, data.length);
	}

	/**
	 * Close connectio to USB device
	 */
	public void close() {
		mConnection.close();
	}

	/**
	 * Write data to FTDI
	 * @param data byte buffer
	 * @return length of data transferred (or zero) for success, or negative value for failure
	 */
	public int write(byte[] data, int offset, int length) {
		// FIXME !!! offset ignored
		return mConnection.bulkTransfer(mOut, data, length, 0);
	}

	/**
	 * 
	 * @param buffer
	 * @return length of data read (or zero) for success, or negative value for failure
	 */
	public int read(byte[] buffer) {
		return mConnection.bulkTransfer(mIn, buffer, buffer.length, 100);
	}
}
