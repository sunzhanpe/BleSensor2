/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sunzhanpe.blesensor2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static java.lang.Math.pow;
import static java.util.UUID.fromString;





/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
@SuppressLint("HandlerLeak")
public class DeviceControlActivity extends Activity {

	private final static String TAG = DeviceControlActivity.class.getSimpleName();

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	private TextView mConnectionState;
	private TextView mDataField;
	private String mDeviceName;
	private String mDeviceAddress;
	private ExpandableListView mGattServicesList;
	private BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
			new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private boolean mConnected = false;
	private BluetoothGattCharacteristic mNotifyCharacteristic;

	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";

	private ArrayList<Double> record_sensor = new ArrayList<Double>(); //the list used to record sensors-data for training
//	private Thread irtThread;
//	private Thread accThread;
	
//	private BluetoothGattCharacteristic fff5 ;
	private BluetoothGattCharacteristic[] irtCharacteristic = new BluetoothGattCharacteristic[3];
	private BluetoothGattCharacteristic[] accCharacteristic = new BluetoothGattCharacteristic[3];
	private BluetoothGattCharacteristic[] humCharacteristic = new BluetoothGattCharacteristic[3];
	private BluetoothGattCharacteristic[] magCharacteristic = new BluetoothGattCharacteristic[3];
	private BluetoothGattCharacteristic[] gyrCharacteristic = new BluetoothGattCharacteristic[3];
	private BluetoothGattCharacteristic[] barCharacteristic = new BluetoothGattCharacteristic[4];
	public static int flag =1;//????1,1???????????2?????????????3??????????,4??????????????(mag),
			public static UUID TEST_UUID_CONFIG = fromString("0000fff5-0000-1000-8000-00805f9b34fb");

	  public final static UUID 
	      UUID_IRT_SERV = fromString("f000aa00-0451-4000-b000-000000000000"),
	      UUID_IRT_DATA = fromString("f000aa01-0451-4000-b000-000000000000"),
	      UUID_IRT_CONF = fromString("f000aa02-0451-4000-b000-000000000000"), // 0: disable, 1: enable
	      UUID_IRT_PERI = fromString("f000aa03-0451-4000-b000-000000000000"), // Period in tens of milliseconds

	      UUID_ACC_SERV = fromString("f000aa10-0451-4000-b000-000000000000"),
	      UUID_ACC_DATA = fromString("f000aa11-0451-4000-b000-000000000000"),
	      UUID_ACC_CONF = fromString("f000aa12-0451-4000-b000-000000000000"), // 0: disable, 1: enable
	      UUID_ACC_PERI = fromString("f000aa13-0451-4000-b000-000000000000"), // Period in tens of milliseconds

	      UUID_HUM_SERV = fromString("f000aa20-0451-4000-b000-000000000000"),
	      UUID_HUM_DATA = fromString("f000aa21-0451-4000-b000-000000000000"),
	      UUID_HUM_CONF = fromString("f000aa22-0451-4000-b000-000000000000"), // 0: disable, 1: enable
	      UUID_HUM_PERI = fromString("f000aa23-0451-4000-b000-000000000000"), // Period in tens of milliseconds

	      UUID_MAG_SERV = fromString("f000aa30-0451-4000-b000-000000000000"),
	      UUID_MAG_DATA = fromString("f000aa31-0451-4000-b000-000000000000"),
	      UUID_MAG_CONF = fromString("f000aa32-0451-4000-b000-000000000000"), // 0: disable, 1: enable
	      UUID_MAG_PERI = fromString("f000aa33-0451-4000-b000-000000000000"), // Period in tens of milliseconds

	      UUID_BAR_SERV = fromString("f000aa40-0451-4000-b000-000000000000"), 
	      UUID_BAR_DATA = fromString("f000aa41-0451-4000-b000-000000000000"),
	      UUID_BAR_CONF = fromString("f000aa42-0451-4000-b000-000000000000"), // 0: disable, 1: enable
	      UUID_BAR_CALI = fromString("f000aa43-0451-4000-b000-000000000000"), // Calibration characteristic
	      UUID_BAR_PERI = fromString("f000aa44-0451-4000-b000-000000000000"), // Period in tens of milliseconds

	      UUID_GYR_SERV = fromString("f000aa50-0451-4000-b000-000000000000"), 
	      UUID_GYR_DATA = fromString("f000aa51-0451-4000-b000-000000000000"),
	      UUID_GYR_CONF = fromString("f000aa52-0451-4000-b000-000000000000"), // 0: disable, bit 0: enable x, bit 1: enable y, bit 2: enable z
	      UUID_GYR_PERI = fromString("f000aa53-0451-4000-b000-000000000000"), // Period in tens of milliseconds

	      UUID_KEY_SERV = fromString("0000ffe0-0000-1000-8000-00805f9b34fb"), 
	      UUID_KEY_DATA = fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up initialization.
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
	//                        or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
				updateConnectionState(R.string.connected);
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				mConnected = false;
				updateConnectionState(R.string.disconnected);
				invalidateOptionsMenu();
				clearUI();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				// Show all the supported services and characteristics on the user interface.
				displayGattServices(mBluetoothLeService.getSupportedGattServices());
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
			}
		}
	};

	// If a given GATT characteristic is selected, check for supported features.  This sample
	// demonstrates 'Read' and 'Notify' features.  See
	// http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
	// list of supported characteristic features.
	private final ExpandableListView.OnChildClickListener servicesListClickListner =
			new ExpandableListView.OnChildClickListener() {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
				int childPosition, long id) {
			if (mGattCharacteristics != null) {
				final BluetoothGattCharacteristic characteristic =
						mGattCharacteristics.get(groupPosition).get(childPosition);
				final int charaProp = characteristic.getProperties();
				if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
					// If there is an active notification on a characteristic, clear
					// it first so it doesn't update the data field on the user interface.
					if (mNotifyCharacteristic != null) {
						mBluetoothLeService.setCharacteristicNotification(
								mNotifyCharacteristic, false);
						mNotifyCharacteristic = null;
					}
					mBluetoothLeService.readCharacteristic(characteristic);
				}
				if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
					mNotifyCharacteristic = characteristic;
					mBluetoothLeService.setCharacteristicNotification(
							characteristic, true);
				}
				return true;
			}
			return false;
		}
	};
	
	private static Integer shortUnsignedAtOffset(byte[] c, int offset) {
	    Integer lowerByte = (int) c[offset] & 0xFF; 
	    Integer upperByte = (int) c[offset+1] & 0xFF; // // Interpret MSB as signed
	    return (upperByte << 8) + lowerByte;
	  }
	private static Integer shortSignedAtOffset(byte[] c, int offset) {
	    Integer lowerByte = (int) c[offset] & 0xFF; 
	    Integer upperByte = (int) c[offset+1]; // // Interpret MSB as signed
	    return (upperByte << 8) + lowerByte;
	  }
	
	private double extractAmbientTemperature(byte [] v) {
	      int offset = 2;
	      return shortUnsignedAtOffset(v, offset) / 128.0;
	    }
	
	private double extractTargetTemperature(byte [] v, double ambient) {
	      Integer twoByteValue = shortSignedAtOffset(v, 0);

	      double Vobj2 = twoByteValue.doubleValue();
	      Vobj2 *= 0.00000015625;

	      double Tdie = ambient + 273.15;

	      double S0 = 5.593E-14; // Calibration factor
	      double a1 = 1.75E-3;
	      double a2 = -1.678E-5;
	      double b0 = -2.94E-5;
	      double b1 = -5.7E-7;
	      double b2 = 4.63E-9;
	      double c2 = 13.4;
	      double Tref = 298.15;
	      double S = S0 * (1 + a1 * (Tdie - Tref) + a2 * pow((Tdie - Tref), 2));
	      double Vos = b0 + b1 * (Tdie - Tref) + b2 * pow((Tdie - Tref), 2);
	      double fObj = (Vobj2 - Vos) + c2 * pow((Vobj2 - Vos), 2);
	      double tObj = pow(pow(Tdie, 4) + (fObj / S), .25);

	      return tObj - 273.15;
	    }
	
	
	private void clearUI() {
		mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
		mDataField.setText(R.string.no_data);
	}
	int i = 0;
	byte buffer[] = new byte[]{(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31};
	byte buffer1[] = new byte[]{(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31};
	byte buffer2[] = new byte[]{(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31};
	byte buffer3[] = new byte[]{(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31};
	byte buffer4[] = new byte[]{(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31};
	byte buffer5[] = new byte[]{(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,
	(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31};
	
	byte buffer6[] = new byte[]{(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31};
	
	private final int[] calibration = new int[8];
	ImageView imageView ;
	Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			Log.w(TAG,"message:"+msg.what);
			if(msg.what==0){
				Log.w(TAG,"flag:"+flag);
				if(1 == flag)
				{
				if (irtCharacteristic[0] != null) {
//					mBluetoothLeService.setCharacteristicNotification(
//							mNotifyCharacteristic, false);
				mBluetoothLeService.readCharacteristic(irtCharacteristic[0]);
				 buffer = irtCharacteristic[0].getValue();
	            if (buffer != null && buffer.length > 0) {
//	                final StringBuilder stringBuilder = new StringBuilder(buffer.length);
//	                for(byte byteChar : buffer)
//	                    stringBuilder.append(String.format("%02X ", byteChar));
//	                double angle = buffer[1]/Math.sqrt(buffer[0]*buffer[0] + buffer[2] * buffer[2]);
//	                angle = -(Math.atan(angle)*180/Math.PI);
	                //angle = angle/16*Math.PI;
	             double angle,angleobj;

	                 angle = extractAmbientTemperature(buffer);
	                 
	                 angleobj =extractTargetTemperature(buffer,angle);
	              
	                
	                
	                
	                
	                
//		            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.horizontal);
		            // Getting width & height of the given image.
//		            int w = bmp.getWidth();
//		            int h = bmp.getHeight();
		            // Setting post rotate to 90
//		            Matrix mtx = new Matrix();
//		            mtx.setTranslate(w, h);
		            //mtx.setScale(1, 1);
//		            if(Double.isNaN(angle))
//		            {
//		              angle = 0;
//		            }
//		            mtx.postRotate((float)angle);
//		            // Rotating Bitmap
//		            Bitmap rotatedBMP = Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);
//		            BitmapDrawable bmd = new BitmapDrawable(rotatedBMP);
//		            imageView.setImageDrawable(bmd);
		            Spannable WordtoSpan1 = new SpannableString("环境温度："+String.format("%.2f", angle)+"度");
		            WordtoSpan1.setSpan(new AbsoluteSizeSpan(50), 0, WordtoSpan1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		            // WordtoSpan.setSpan(new ForegroundColorSpan(Color.BLUE), 15, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
		            tv1.setText(WordtoSpan1);
		            
		            Spannable WordtoSpan2 = new SpannableString("目标温度："+String.format("%.2f", angleobj)+"度");
		            WordtoSpan2.setSpan(new AbsoluteSizeSpan(50), 0, WordtoSpan2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		            
		            tv2.setText(WordtoSpan2);
		            //tv.setText("angle:"+angle+"  0:"+buffer[0]+"  1:"+buffer[1]+"  2:"+buffer[2]+"  hex:"+stringBuilder.toString());
	            }
//					mBluetoothLeService.setCharacteristicNotification(
//							mNotifyCharacteristic, true);
			}
			}
				//	((TextView) findViewById(R.id.device_address)).setText(""+i++);
				//tv.setText(""+i++);
			
//				if (fff5 != null) {
////					mBluetoothLeService.setCharacteristicNotification(
////							mNotifyCharacteristic, false);
//				mBluetoothLeService.readCharacteristic(fff5);
//				 buffer = fff5.getValue();
//	            if (buffer != null && buffer.length > 0) {
//	                final StringBuilder stringBuilder = new StringBuilder(buffer.length);
//	                for(byte byteChar : buffer)
//	                    stringBuilder.append(String.format("%02X ", byteChar));
//	                tv1.append(stringBuilder.toString());
//	            }
////					mBluetoothLeService.setCharacteristicNotification(
////							mNotifyCharacteristic, true);
//			}
				
//chuck ??????????? ??????????????????????????
			else if(2 == flag)
			{
			if(accCharacteristic[0] != null)
			{
				mBluetoothLeService.readCharacteristic(accCharacteristic[0]);
				 buffer1 = accCharacteristic[0].getValue();
	            if (buffer1 != null && buffer1.length > 0) {
	            	double accx,accy,accz;
	            	accx = (int)buffer1[0]/64.0;
	            	accy = (int)buffer1[1]/64.0;
	            	accz = (int)buffer1[2]/-64.0;
	            	Spannable WordtoSpan3 = new SpannableString("加速度x轴："+String.format("%.5f", accx)+"g"+"\n"+
	            												"y轴"+String.format("%.5f", accy)+"g"+"\n"+
	            												"z轴"+String.format("%.5f", accz)+"g");
		            WordtoSpan3.setSpan(new AbsoluteSizeSpan(50), 0, WordtoSpan3.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);		            
		            tv3.setText(WordtoSpan3);
	            }
			}
			}
			else if(3 == flag)
			{

				Log.w(TAG,"pikaqiu:"+humCharacteristic[0]);
				if(humCharacteristic[0] != null)
				{

//					Log.w(TAG,"pikaqiu");
					mBluetoothLeService.readCharacteristic(humCharacteristic[0]);
					buffer2 = humCharacteristic[0].getValue();

//					Log.w(TAG,"buffer2:"+buffer2);
					if(buffer2 != null && buffer2.length >0)
					{
						double hum;
						int a = shortUnsignedAtOffset(buffer2,2);
			            a = a- (a % 4);
			            hum = (-6f) + 125f * (a / 65535f);
			            Spannable WordtoSpan4 = new SpannableString("环境湿度："+String.format("%.2f", hum)+"%");
			            WordtoSpan4.setSpan(new AbsoluteSizeSpan(50), 0, WordtoSpan4.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			            // WordtoSpan.setSpan(new ForegroundColorSpan(Color.BLUE), 15, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
//						Log.w(TAG,"shidu:"+WordtoSpan4);

						tv4.setText(WordtoSpan4);
					}
					
				}
				
			}
			else if(4 == flag)
			{
				if(magCharacteristic[0] != null)
				{
					mBluetoothLeService.readCharacteristic(magCharacteristic[0]);
					buffer3 = magCharacteristic[0].getValue();
					if(buffer3 != null && buffer3.length >0)
					{
						float x = shortSignedAtOffset(buffer3, 0) * (2000f / 65536f) * -1;
		                float y = shortSignedAtOffset(buffer3, 2) * (2000f / 65536f) * -1;
		                float z = shortSignedAtOffset(buffer3, 4) * (2000f / 65536f);
		            	Spannable WordtoSpan5 = new SpannableString("磁力计x轴："+String.format("%.6f", x)+"\n"+
		            												"y轴"+String.format("%.6f", y)+"\n"+
		            												"z轴: "+String.format("%.6f", z));
		            	WordtoSpan5.setSpan(new AbsoluteSizeSpan(50), 0, WordtoSpan5.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);		            
			            tv5.setText(WordtoSpan5);
					}
					
				}
			}
				
			else if(5 == flag)
			{
				if(gyrCharacteristic[0] != null)
				{
					mBluetoothLeService.readCharacteristic(gyrCharacteristic[0]);
					buffer4 = gyrCharacteristic[0].getValue();
					if(buffer4 != null && buffer4.length >0)
					{
						float y = shortSignedAtOffset(buffer4, 0) * (500f / 65536f) * -1;
		                float x = shortSignedAtOffset(buffer4, 2) * (500f / 65536f);
		                float z = shortSignedAtOffset(buffer4, 4) * (500f / 65536f);
		            	Spannable WordtoSpan6 = new SpannableString("陀螺仪x轴："+String.format("%.6f", x)+"\n"+
		            												"y轴"+String.format("%.6f", y)+"\n"+
		            												"z轴: "+String.format("%.6f", z));
		            	WordtoSpan6.setSpan(new AbsoluteSizeSpan(50), 0, WordtoSpan6.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);		            
			            tv6.setText(WordtoSpan6);
					}
					
				}
			}
				
			else if(6 == flag)
			{
				if(barCharacteristic[2] != null)
				{
					mBluetoothLeService.readCharacteristic(barCharacteristic[2]);
					buffer5 = barCharacteristic[2].getValue();
					if(buffer5 != null && buffer5.length >0)
					{
						for (int i=0; i<4; ++i) 
						{
				            calibration[i] = shortUnsignedAtOffset(buffer5, i * 2);
				            calibration[i+4] = shortSignedAtOffset(buffer5, 8 + i * 2);
				        }
					}
					
				}
			}
			else if(7 == flag)
			{
				if(barCharacteristic[0] != null)
				{
					mBluetoothLeService.readCharacteristic(barCharacteristic[0]);
					buffer6 = barCharacteristic[0].getValue();
					if(buffer6 != null && buffer6.length >0)
					{
						final Integer t_r;	// Temperature raw value from sensor
				        final Integer p_r;	// Pressure raw value from sensor
				        final Double t_a; 	// Temperature actual value in unit centi degrees celsius
				        final Double S;	// Interim value in calculation
				        final Double O;	// Interim value in calculation
				        final Double p_a; 	// Pressure actual value in unit Pascal.

				        t_r = shortSignedAtOffset(buffer6, 0);
				        p_r = shortUnsignedAtOffset(buffer6, 2);

				        t_a = (100 * (calibration[0] * t_r / pow(2,8) + calibration[1] * pow(2,6))) / pow(2,16);
				        S = calibration[2] + calibration[3] * t_r / pow(2,17) + ((calibration[4] * t_r / pow(2,15)) * t_r) / pow(2,19);
				        O = calibration[5] * pow(2,14) + calibration[6] * t_r / pow(2,3) + ((calibration[7] * t_r / pow(2,15)) * t_r) / pow(2,4);
				        p_a = (S * p_r + O) / pow(2,14);
				        
				        Spannable WordtoSpan7 = new SpannableString("气压："+String.format("%.5f", p_a));
			            WordtoSpan7.setSpan(new AbsoluteSizeSpan(50), 0, WordtoSpan7.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			            // WordtoSpan.setSpan(new ForegroundColorSpan(Color.BLUE), 15, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
			            tv7.setText(WordtoSpan7);
					}
				}
			}
			}
		};
	};


	TextView tv1; //irt
	TextView tv2; //irt
	TextView tv3; //acc
	TextView tv4; //hum
	TextView tv5; //mag
	TextView tv6; //gyr
	TextView tv7; //bar
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gatt_services_characteristics);

		final Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

		// Sets up UI references.
		mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
		mGattServicesList.setOnChildClickListener(servicesListClickListner);
		mConnectionState = (TextView) findViewById(R.id.connection_state);
//		mDataField = (TextView) findViewById(R.id.data_value);

//		getActionBar().setTitle(mDeviceName);
//		getActionBar().setDisplayHomeAsUpEnabled(true);
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

		Button button3=(Button)findViewById(R.id.button3);  
		Button button2 = (Button)findViewById(R.id.button2);
		Button button1 = (Button)findViewById(R.id.button1);
		Button button4 = (Button)findViewById(R.id.button4);
		Button button5 = (Button)findViewById(R.id.button5);
		Button button6 = (Button)findViewById(R.id.button6);
		Button button7 = (Button)findViewById(R.id.button7);
		Button button8 = (Button)findViewById(R.id.button8);

		tv1=(TextView)findViewById(R.id.textView1);
		tv2=(TextView)findViewById(R.id.textView2);
		tv3=(TextView)findViewById(R.id.textView3);
		tv4=(TextView)findViewById(R.id.textView4);
		tv5=(TextView)findViewById(R.id.textView5);
		tv6=(TextView)findViewById(R.id.textView6);
		tv7=(TextView)findViewById(R.id.textView7);
		
        
//        imageView=(ImageView)findViewById(R.id.imageView1);
        
		new Thread(){
			public void run() {
				while(true){
					try {
						Thread.sleep(200);
						handler.sendEmptyMessage(0);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
		}.start();

	
///*		//?????????????????Button????  
		button1.setOnClickListener(new OnClickListener()  
		{         
			public void onClick(View v) {
				flag = 1; //1?????????????????
			}
		});   

		button2.setOnClickListener(new OnClickListener()  
		{         
			public void onClick(View v) {
				flag = 2; //2???????????????????
				Log.i("sunzhangg", String.valueOf(flag));
			}
		}); 
		button4.setOnClickListener(new OnClickListener()  
		{         
			public void onClick(View v) {
				flag = 3; //3?????????????????
			}
		});
		button5.setOnClickListener(new OnClickListener()  
		{         
			public void onClick(View v) {
				flag = 4; //4?????????????????????
			}
		});
		button6.setOnClickListener(new OnClickListener()  
		{         
			public void onClick(View v) {
				flag = 5; //5??????????????????????
			}
		}); 
		button7.setOnClickListener(new OnClickListener()  
		{         
			public void onClick(View v) {
				flag = 6; //6???????????????????
				
			}
		});
		button8.setOnClickListener(new OnClickListener()  
		{         
			public void onClick(View v) {
				flag = 7; //6???????????????????
			}
		});
		//?????????????????Button????  
		button3.setOnClickListener(new OnClickListener()  
		{         
			public void onClick(View v) {
				if(1 == flag)
				{
	//				((TextView) findViewById(R.id.device_address))
	//				.setText("accEnable" + i++);
					if (irtCharacteristic[1] != null) {
	//					mBluetoothLeService.setCharacteristicNotification(
	//							mNotifyCharacteristic, false);
					byte[] data = new byte[1];
					data[0] = 1;
					irtCharacteristic[1].setValue(data);
	
					mBluetoothLeService.writeCharacteristic(irtCharacteristic[1]);
	//					mBluetoothLeService.setCharacteristicNotification(
	//							mNotifyCharacteristic, true);
						}
					if (irtCharacteristic[2] != null) {
	//					mBluetoothLeService.setCharacteristicNotification(
	//							mNotifyCharacteristic, false);
					byte[] data = new byte[2];
					data[0] = 5;
					irtCharacteristic[2].setValue(data);
	
					mBluetoothLeService.writeCharacteristic(irtCharacteristic[2]);
	//					mBluetoothLeService.setCharacteristicNotification(
	//							mNotifyCharacteristic, true);
						}
				}
				else if(2 == flag)
				{
					if (accCharacteristic[1] != null) {
	//								mBluetoothLeService.setCharacteristicNotification(
	//										mNotifyCharacteristic, false);
					byte[] data = new byte[1];
					data[0] = 1;
					accCharacteristic[1].setValue(data);
	
					mBluetoothLeService.writeCharacteristic(accCharacteristic[1]);
	//								mBluetoothLeService.setCharacteristicNotification(
	//										mNotifyCharacteristic, true);
								}
					if (accCharacteristic[2] != null) {
	//								mBluetoothLeService.setCharacteristicNotification(
	//										mNotifyCharacteristic, false);
					byte[] data = new byte[2];
					data[0] = 10;
					accCharacteristic[2].setValue(data);
	
					mBluetoothLeService.writeCharacteristic(accCharacteristic[2]);
	//								mBluetoothLeService.setCharacteristicNotification(
	//										mNotifyCharacteristic, true);
								}
				}
				else if(3 == flag)
				{
					Log.w(TAG,humCharacteristic[1]+" "+humCharacteristic[2]);
					if (humCharacteristic[1] != null) 
					{
						
						byte[] data = new byte[1];
						data[0] = 1;
						humCharacteristic[1].setValue(data);
						mBluetoothLeService.writeCharacteristic(humCharacteristic[1]);
		
					}
					if (humCharacteristic[2] != null) 
					{
		
						byte[] data = new byte[2];
						data[0] = 10;
						humCharacteristic[2].setValue(data);
						mBluetoothLeService.writeCharacteristic(humCharacteristic[2]);
				
					}
				}
				else if(4 == flag)
				{
					if (magCharacteristic[1] != null) 
					{
						
						byte[] data = new byte[1];
						data[0] = 1;
						magCharacteristic[1].setValue(data);
						mBluetoothLeService.writeCharacteristic(magCharacteristic[1]);
		
					}
					if (magCharacteristic[2] != null) 
					{
		
						byte[] data = new byte[2];
						data[0] = 64;
						magCharacteristic[2].setValue(data);
						mBluetoothLeService.writeCharacteristic(magCharacteristic[2]);
				
					}
				}		
				else if(5 == flag)
				{
					if (gyrCharacteristic[1] != null) 
					{
						
						byte[] data = new byte[1];
						data[0] = 7;  //7 ??????????x,y,z?????????
						gyrCharacteristic[1].setValue(data);
						mBluetoothLeService.writeCharacteristic(gyrCharacteristic[1]);
		
					}
					if (gyrCharacteristic[2] != null) 
					{
		
						byte[] data = new byte[2];
						data[0] = 64;
						gyrCharacteristic[2].setValue(data);
						mBluetoothLeService.writeCharacteristic(gyrCharacteristic[2]);
				
					}
				}
				else if(6 == flag)
				{
					if (barCharacteristic[1] != null) 
					{
						
						byte[] data = new byte[1];
						data[0] = 2;  
						barCharacteristic[1].setValue(data);
						mBluetoothLeService.writeCharacteristic(barCharacteristic[1]);
		
					}
					
				}
				else if(7 == flag)
				{
					if (barCharacteristic[1] != null) 
					{
						
						byte[] data = new byte[1];
						data[0] = 1;  
						barCharacteristic[1].setValue(data);
						mBluetoothLeService.writeCharacteristic(barCharacteristic[1]);
		
					}
					if (barCharacteristic[3] != null) 
					{
		
						byte[] data = new byte[2];
						data[0] = 100;
						barCharacteristic[3].setValue(data);
						mBluetoothLeService.writeCharacteristic(barCharacteristic[3]);
				
					}
				}
				
			
			}
		});  
		


		//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
		//                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
		//        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		//        mBluetoothGatt.writeDescriptor(descriptor);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		mBluetoothLeService = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gatt_services, menu);
		if (mConnected) {
			menu.findItem(R.id.menu_connect).setVisible(false);
			menu.findItem(R.id.menu_disconnect).setVisible(true);
		} else {
			menu.findItem(R.id.menu_connect).setVisible(true);
			menu.findItem(R.id.menu_disconnect).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_connect:
			mBluetoothLeService.connect(mDeviceAddress);
			return true;
		case R.id.menu_disconnect:
			mBluetoothLeService.disconnect();
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateConnectionState(final int resourceId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mConnectionState.setText(resourceId);
			}
		});
	}

	private void displayData(String data) {
		if (data != null) {
			mDataField.setText(data);
		}
	}

	// Demonstrates how to iterate through the supported GATT Services/Characteristics.
	// In this sample, we populate the data structure that is bound to the ExpandableListView
	// on the UI.
	

	private void displayGattServices(List<BluetoothGattService> gattServices) {
		if (gattServices == null) return;
		String uuid = null;
		String unknownServiceString = getResources().getString(R.string.unknown_service);
		String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
		= new ArrayList<ArrayList<HashMap<String, String>>>();
		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
		// Loops through available GATT Services.
		for (BluetoothGattService gattService : gattServices) {
			HashMap<String, String> currentServiceData = new HashMap<String, String>();
			uuid = gattService.getUuid().toString();
			currentServiceData.put(
					LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
			currentServiceData.put(LIST_UUID, uuid);
			gattServiceData.add(currentServiceData);

			ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
					new ArrayList<HashMap<String, String>>();
			List<BluetoothGattCharacteristic> gattCharacteristics = 
					(gattService.getCharacteristics());
			ArrayList<BluetoothGattCharacteristic> charas =
					new ArrayList<BluetoothGattCharacteristic>();

			// Loops through available Characteristics.
			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				charas.add(gattCharacteristic);
				HashMap<String, String> currentCharaData = new HashMap<String, String>();
				uuid = gattCharacteristic.getUuid().toString();
//				if(uuid.equals(TEST_UUID_CONFIG.toString())){                	
//					fff5 = gattCharacteristic;
//				}
				

					if(uuid.equals(UUID_ACC_DATA.toString())){                	
						accCharacteristic[0] = gattCharacteristic;
						Log.i("sunzhan", "read out"+String.valueOf(flag));
					}
					if(uuid.equals(UUID_ACC_CONF.toString())){                	
						accCharacteristic[1] = gattCharacteristic;
					}
					if(uuid.equals(UUID_ACC_PERI.toString())){                	
						accCharacteristic[2] = gattCharacteristic;
					}

					if(uuid.equals(UUID_IRT_DATA.toString())){                	
						irtCharacteristic[0] = gattCharacteristic;
						Log.i("sunzhan", "read out"+String.valueOf(flag));
						//Log.i("sunzhan", "read out");
					}
					if(uuid.equals(UUID_IRT_CONF.toString())){                	
						irtCharacteristic[1] = gattCharacteristic;
						//Log.i("sunzhan", "read out");
					}
					if(uuid.equals(UUID_IRT_PERI.toString())){                	
						irtCharacteristic[2] = gattCharacteristic;
						//Log.i("sunzhan", "read out");
					}

					if(uuid.equals(UUID_HUM_DATA.toString())){                	
						humCharacteristic[0] = gattCharacteristic;
						Log.i("sunzhan", "read out"+String.valueOf(flag));
						//Log.i("sunzhan", "read out");
					}
					if(uuid.equals(UUID_HUM_CONF.toString())){                	
						humCharacteristic[1] = gattCharacteristic;
						//Log.i("sunzhan", "read out");
					}
					if(uuid.equals(UUID_HUM_PERI.toString())){                	
						humCharacteristic[2] = gattCharacteristic;
						//Log.i("sunzhan", "read out");
					}

					if(uuid.equals(UUID_MAG_DATA.toString())){                	
						magCharacteristic[0] = gattCharacteristic;
						Log.i("sunzhan", "read out"+String.valueOf(flag));
						//Log.i("sunzhan", "read out");
					}
					if(uuid.equals(UUID_MAG_CONF.toString())){                	
						magCharacteristic[1] = gattCharacteristic;
						//Log.i("sunzhan", "read out");
					}
					if(uuid.equals(UUID_MAG_PERI.toString())){                	
						magCharacteristic[2] = gattCharacteristic;
						//Log.i("sunzhan", "read out");
					}

					if(uuid.equals(UUID_GYR_DATA.toString())){                	
						gyrCharacteristic[0] = gattCharacteristic;
						Log.i("sunzhan", "read out"+String.valueOf(flag));
						//Log.i("sunzhan", "read out");
					}
					if(uuid.equals(UUID_GYR_CONF.toString())){                	
						gyrCharacteristic[1] = gattCharacteristic;
						//Log.i("sunzhan", "read out");
					}
					if(uuid.equals(UUID_GYR_PERI.toString())){                	
						gyrCharacteristic[2] = gattCharacteristic;
						//Log.i("sunzhan", "read out");
					}

					
					if(uuid.equals(UUID_BAR_CONF.toString())){                	
						barCharacteristic[1] = gattCharacteristic;
						//Log.i("sunzhan", "read out");
					}
					if(uuid.equals(UUID_BAR_CALI.toString())){                	
						barCharacteristic[2] = gattCharacteristic;
						//Log.i("sunzhan", "read out");
					}
					if(uuid.equals(UUID_BAR_PERI.toString())){                	
						barCharacteristic[3] = gattCharacteristic;
						//Log.i("sunzhan", "read out");
					}

					if(uuid.equals(UUID_BAR_DATA.toString())){                	
						barCharacteristic[0] = gattCharacteristic;
						Log.i("sunzhan", "read out"+String.valueOf(flag));
						//Log.i("sunzhan", "read out");
					}
					if(uuid.equals(UUID_BAR_CONF.toString())){                	
						barCharacteristic[1] = gattCharacteristic;
						//Log.i("sunzhan", "read out");
					}
					if(uuid.equals(UUID_BAR_PERI.toString())){                	
						barCharacteristic[3] = gattCharacteristic;
						//Log.i("sunzhan", "read out");
					}


				
				
				currentCharaData.put(
						LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
				currentCharaData.put(LIST_UUID, uuid);
				gattCharacteristicGroupData.add(currentCharaData);
			}
			
			
			
			
			mGattCharacteristics.add(charas);
			gattCharacteristicData.add(gattCharacteristicGroupData);

		}
		
		
			
			
	

//		SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
//				this,
//				gattServiceData,
//				android.R.layout.simple_expandable_list_item_2,
//				new String[] {LIST_NAME, LIST_UUID},
//				new int[] { android.R.id.text1, android.R.id.text2 },
//				gattCharacteristicData,
//				android.R.layout.simple_expandable_list_item_2,
//				new String[] {LIST_NAME, LIST_UUID},
//				new int[] { android.R.id.text1, android.R.id.text2 }
//				);
//		mGattServicesList.setAdapter(gattServiceAdapter);
		
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}
}
