package com.v2soft.androidftdi;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.v2soft.androidftdi.FTDIDevice.FtdiChipType;
import com.v2soft.ftdi.R;

/**
 * Digital voltmeter activity
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class DigitalVoltmeter extends Activity implements OnClickListener {
	protected static final String ACTION_USB_PERMISSION = "com.v2soft.android.USB";
	private static final String VID_PID = "0403:6001";
	protected static final String LOG_TAG = DigitalVoltmeter.class.getSimpleName();
	public static UsbDevice sDevice = null;
	private boolean mStop = false;
	private boolean mStopped = true;
	private FTDIDevice mDevice;
	private TextView mTxtVolts;
	private NumberFormat sFortmatter = new DecimalFormat("##0.0 V");

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_voltmeter);
		findViewById(R.id.btnStart).setOnClickListener(this);
		findViewById(R.id.btnStop).setOnClickListener(this);
		mTxtVolts = (TextView) findViewById(R.id.txtVolts);
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
		final UsbManager usbm = (UsbManager) getSystemService(USB_SERVICE);
		if (d == null)
			return; 
		mDevice = new FTDIDevice(usbm, d, 0, FtdiChipType.TypeR);
		new Thread(mLoop).start();
	}

	private Runnable mLoop = new Runnable() {
		@Override
		public void run() {
			byte[] buffer = new byte[16];
			for(;;){//this is the main loop for transferring
				if(mStop){
					mStopped = true;
					break;
				}
				int read = mDevice.read(buffer);
				if ( read == 5 ) {
					//Log.d(LOG_TAG, "R="+read+" "+getHEX(buffer, 0, read));
					if ( (buffer[2] & 0xFF) == 0xAB ) {
						final int value = ((buffer[3] << 8) & 0xFF00) | (buffer[4] & 0xFF );
						float r1 = 4700; // 4k7
						float r2 = 154000; //154k
						final double volt = ((double)value*5.0/1023.0)*r2/r1; 
						mTxtVolts.post(new Runnable() {
							@Override
							public void run() {
								mTxtVolts.setText(sFortmatter.format(volt));
							}
						});
					}
				}
			}
			mDevice.close();
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

	// =======================================================
	// OnCLick listener
	// =======================================================
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.btnStart:
			mDevice.write(new byte[]{'a','a'});
			break;
		case R.id.btnStop:
			mDevice.write(new byte[]{'a','d'});
			break;
		default:
			break;
		}
	}
}