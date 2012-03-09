/*
 * UsbControllerActivity.java
 * This file is part of UsbController
 *
 * Copyright (C) 2012 - Manuel Di Cerbo
 *
 * UsbController is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * UsbController is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UsbController. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.serverbox.android.usbcontroller;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;


/**
 * (c) Neuxs-Computing GmbH Switzerland
 * @author Manuel Di Cerbo, 02.02.2012
 *
 */
public class UsbControllerActivity extends Activity {
	/** Called when the activity is first created. */
	private static final int VID = 0x0403;
	private static final int PID = 0x6001;//I believe it is 0x0000 for the Arduino Megas
	private static UsbController sUsbController;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if(sUsbController == null){
	        sUsbController = new UsbController(this, mConnectionHandler, VID, PID);
        }
        ((SeekBar)(findViewById(R.id.seekBar1))).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					if(sUsbController != null){
						sUsbController.send((byte)(progress&0xFF));
					}
				}
			}
		});
        ((Button)findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(sUsbController == null)
					sUsbController = new UsbController(UsbControllerActivity.this, mConnectionHandler, VID, PID);
				else{
					sUsbController.stop();
					sUsbController = new UsbController(UsbControllerActivity.this, mConnectionHandler, VID, PID);
				}
			}
		});
        
    }

	private final IUsbConnectionHandler mConnectionHandler = new IUsbConnectionHandler() {
		@Override
		public void onUsbStopped() {
			L.e("Usb stopped!");
		}
		
		@Override
		public void onErrorLooperRunningAlready() {
			L.e("Looper already running!");
		}
		
		@Override
		public void onDeviceNotFound() {
			if(sUsbController != null){
				sUsbController.stop();
				sUsbController = null;
			}
		}
	};
}