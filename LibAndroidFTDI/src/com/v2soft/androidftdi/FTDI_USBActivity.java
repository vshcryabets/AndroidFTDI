package com.v2soft.androidftdi;

import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;

import com.v2soft.androidftdi.FTDIDevice.FtdiChipType;
import com.v2soft.ftdi.R;

/**
 * 
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class FTDI_USBActivity extends Activity {
	protected static final String ACTION_USB_PERMISSION = "ch.serverbox.android.USB";
	private static final String VID_PID = "0403:6001";
	protected static final String LOG_TAG = FTDI_USBActivity.class.getSimpleName();
	public static UsbDevice sDevice = null;
	private boolean mStop = false;
	private boolean mStopped = true;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
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
							FTDIDevice device = new FTDIDevice((UsbManager) getSystemService(Context.USB_SERVICE), 
									dev, 0, FtdiChipType.TypeR);
							device.setBaudRate(9600);
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
			final UsbManager usbm = (UsbManager) getSystemService(USB_SERVICE);
			if (sDevice == null)
				return; 
			FTDIDevice device = new FTDIDevice(usbm, sDevice, 0, FtdiChipType.TypeR);
			short counter = 0;
			byte[] buffer = new byte[32];
			for(;;){//this is the main loop for transferring
				buffer[0] = (byte) (counter&0xFF);
				device.write(buffer, 0, 1);
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if(mStop){
					mStopped = true;
					break;
				}
				counter++;
				if ( counter > 255 ) {
					counter = 0;
				}
				int read = device.read(buffer);
				if ( read > 0 ) {
					Log.d(LOG_TAG, "R="+read+" "+getHEX(buffer, 0, read));
				}
			}
			device.close();
		}
	};
	
    /**
     * Returns byte array in HEX format
     * @param bytes - input bytes array
     * @return
     */
    public static String getHEX(byte[] bytes, int offset, int length) {
        char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[length * 2];
        int v;
        for ( int j = offset; j <length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v/16];
            hexChars[j*2 + 1] = hexArray[v%16];
        }
        return new String(hexChars);
    }

	private static void l(Object s) {
		Log.d(LOG_TAG, ">==< " + s.toString() + " >==<");
	}

	private static void e(Object s) {
		Log.e(LOG_TAG, ">==< " + s.toString() + " >==<");
	}
}