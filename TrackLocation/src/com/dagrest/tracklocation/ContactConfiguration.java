package com.dagrest.tracklocation;

import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.DeviceData;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ContactConfiguration extends Activity {

	private TextView mUserName;
	private TextView mEmail;
	private TextView mDeviceName;
	private TextView mDeviceType;
	private TextView mStatus;
	private TextView mNotification;
	private TextView mLat;
	private TextView mLng;
	
	private String deviceStatus;
	private String notification;
	private String lat;
	private String lng;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.contact_config);
		Intent intent = getIntent();
		String jsonStringContactDeviceData = intent.getExtras().getString(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA);
		String userName = intent.getExtras().getString(CommonConst.CONTACT_LIST_SELECTED_VALUE);

		ContactDeviceDataList contactDeviceDataList = Utils.fillContactDeviceDataFromJSON(jsonStringContactDeviceData);
		if(contactDeviceDataList == null){
			return;
		}
		ContactDeviceData contactDeviceData = Utils.getContactDeviceDataByUsername(contactDeviceDataList, userName);
		if(contactDeviceData == null){
			return;
		}
		
		ContactData contactData = contactDeviceData.getContactData();
		if(contactData == null){
			return;
		}
		DeviceData deviceData = contactDeviceData.getDeviceData();
		if(deviceData == null){
			return;
		}
		
		deviceStatus = "N/A";
		notification = "N/A";
		lat = "N/A";
		lng = "N/A";
				
		mUserName = (TextView) findViewById(R.id.username);
		mEmail = (TextView) findViewById(R.id.email);
		mDeviceName = (TextView) findViewById(R.id.devicename);
		mDeviceType = (TextView) findViewById(R.id.devicetype);
		mStatus = (TextView) findViewById(R.id.status);
		mNotification = (TextView) findViewById(R.id.notification);
		mLat = (TextView) findViewById(R.id.lat);
		mLng = (TextView) findViewById(R.id.lng);
		
		mUserName.setText(contactData.getUsername());
		mEmail.setText(contactData.getEmail());
		mDeviceName.setText(deviceData.getDeviceName());
		mDeviceType.setText(deviceData.getDeviceTypeEnum().toString());
		mStatus.setText(deviceStatus);
		mNotification.setText(notification);
		mLat.setText(lat);
		mLng.setText(lng);
		
	}

}
