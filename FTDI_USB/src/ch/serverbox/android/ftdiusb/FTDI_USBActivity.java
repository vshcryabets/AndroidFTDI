/*
 * This file is part of Android FTDI Serial
 *
 * Copyright (C) 2011 - Manuel Di Cerbo, Nexus-Computing GmbH
 *
 * Android FTDI Serial is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Android FTDI Serial is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Android FTDI Serial; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, 
 * Boston, MA  02110-1301  USA
 */
 
 /* 
  * Thanks to the libftdi project http://www.intra2net.com/en/developer/libftdi/
  */
package ch.serverbox.android.ftdiusb;

import java.util.HashMap;
import java.util.Iterator;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;

//0403:6001 FTDI Serial
//0x81 EP IN
//0x02 EP OUT
/*
 * 	handle.controlMsg(requestType = 0x40, request = 0, value = 0, index = 0, buffer = 0, timeout = 0)#reset
	handle.controlMsg(requestType = 0x40, request = 0, value = 1, index = 0, buffer = 0, timeout = 0)#reset
	handle.controlMsg(requestType = 0x40, request = 0, value = 2, index = 0, buffer = 0, timeout = 0)#reset
	handle.controlMsg(requestType = 0x40, request = 0x03, value = 0x4138, index = 0, buffer = 0, timeout = 0)#9600 baudrate
 */
public class FTDI_USBActivity extends Activity {
    protected static final String ACTION_USB_PERMISSION = "ch.serverbox.android.USB";
	private static final String VID_PID = "0403:6001";
    public static UsbDevice sDevice = null;
	private static FTDI_USBActivity sActivityContext;
	private boolean mStop = false;
	private boolean mStopped = true;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		sActivityContext = this;
		setContentView(R.layout.main);
    }
    
    @Override
    protected void onDestroy() {
    	sActivityContext = null;
    	super.onDestroy();
    }
    
    @Override
    protected void onStart() {
    	mStop = false;
    	if(mStopped)
    		enumerate();
    	super.onStart();
    }
    
    @Override
    protected void onStop() {
    	mStop = true;
    	unregisterReceiver(mPermissionReceiver);
    	super.onStop();
    }
    
    
	private final BroadcastReceiver mPermissionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
				if (!intent.getBooleanExtra(
						UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
					e("Permission not granted :(");
				} else {
					l("Permission granted");
					UsbDevice dev = (UsbDevice) intent
					.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (dev != null) {
						if (String.format("%04X:%04X", dev.getVendorId(),
								dev.getProductId()).equals(VID_PID)) {
							mainloop(dev);//has new thread
						}
					} else {
						e("device not present!");
					}
				}
			}
		}
	};
	
	private void enumerate() {
		l("enumerating");
		UsbManager usbman = (UsbManager) getSystemService(USB_SERVICE);

		HashMap<String, UsbDevice> devlist = usbman.getDeviceList();
		Iterator<UsbDevice> deviter = devlist.values().iterator();
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(
				ACTION_USB_PERMISSION), 0);

		while (deviter.hasNext()) {
			UsbDevice d = deviter.next();
			l("Found device: "
					+ String.format("%04X:%04X", d.getVendorId(),
							d.getProductId()));
			if (String.format("%04X:%04X", d.getVendorId(), d.getProductId())
					.equals(VID_PID)) {
				// we need to upload the hex file, first request permission
				l("Device under: " + d.getDeviceName());
				registerReceiver(mPermissionReceiver, new IntentFilter(
						ACTION_USB_PERMISSION));
				if (!usbman.hasPermission(d))
					usbman.requestPermission(d, pi);
				else
					mainloop(d);
				break;
			}
		}
		l("no more devices found");
	}
	
	private void mainloop(UsbDevice d) {
		sDevice = d;// not really nice...
		new Thread(mLoop).start();
	}

	private Runnable mLoop = new Runnable() {


		@Override
		public void run() {
			UsbDevice dev = sDevice;
			if (dev == null)
				return; 
			UsbManager usbm = (UsbManager) getSystemService(USB_SERVICE);
			UsbDeviceConnection conn = usbm.openDevice(dev);
			l("Interface Count: "+dev.getInterfaceCount());
			l("Using "+String.format("%04X:%04X", sDevice.getVendorId(), sDevice.getProductId()));

			if(!conn.claimInterface(dev.getInterface(0), true)) 
				return;
			
			conn.controlTransfer(0x40, 0, 0, 0, null, 0, 0);//reset
			conn.controlTransfer(0x40, 0, 1, 0, null, 0, 0);//clear Rx
			conn.controlTransfer(0x40, 0, 2, 0, null, 0, 0);//clear Tx
			conn.controlTransfer(0x40, 0x03, 0x4138, 0, null, 0, 0);//baudrate 9600

			UsbEndpoint epIN = null;
			UsbEndpoint epOUT = null;
			
			short counter = 0;
			
			UsbInterface usbIf = dev.getInterface(0);				
			for(int i = 0; i < usbIf.getEndpointCount(); i++){
				l("EP: "+String.format("0x%02X", usbIf.getEndpoint(i).getAddress()));
				if(usbIf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK){
					l("Bulk Endpoint");
					if(usbIf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN)
						epIN = usbIf.getEndpoint(i);
					else
						epOUT = usbIf.getEndpoint(i);
				}else{
					l("Not Bulk");
				}
			}
			
			for(;;){//this is the main loop for transferring		
				conn.bulkTransfer(epOUT, new byte[]{(byte) (counter&0xFF)}, 1, 0);
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if(mStop){
					mStopped = true;
					return;
				}
				l("sent "+counter);
				counter++;
				if ( counter > 255 ) {
					counter = 0;
				}
			}
		}
	};
	
	private static void l(Object s) {
		Log.d("FTDI_USB", ">==< " + s.toString() + " >==<");
	}

	private static void e(Object s) {
		Log.e("FTDI_USB", ">==< " + s.toString() + " >==<");
	}
}