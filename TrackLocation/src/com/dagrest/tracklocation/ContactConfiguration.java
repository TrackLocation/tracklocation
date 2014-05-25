package com.dagrest.tracklocation;

import java.util.ArrayList;
import java.util.List;

import com.dagrest.tracklocation.datatype.BroadcastCommandEnum;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.DeviceData;
import com.dagrest.tracklocation.datatype.NotificationCommandEnum;
import com.dagrest.tracklocation.datatype.PushNotificationServiceStatusEnum;
import com.dagrest.tracklocation.datatype.TrackLocationServiceStatusEnum;
import com.dagrest.tracklocation.http.HttpUtils;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.service.TrackLocationService;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.dagrest.tracklocation.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ContactConfiguration extends Activity {

	private static final String DEVICE_STATUS_DEFAULT = "N/A";
	private static final String NOTIFICATION_DEFAULT = "N/A";
	private static final String LAT_DEFAULT = "N/A";
	private static final String LNG_DEFAULT = "N/A";
	
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

	private ContactDeviceDataList contactDeviceDataList;
	private ContactDeviceData contactDeviceData;
	private ContactData contactData;
	private DeviceData deviceData;
	private String className;
	private Controller controller;
	private String jsonMessage;

	private BroadcastReceiver gcmIntentServiceChangeWatcher;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		className = this.getClass().getName();
		setContentView(R.layout.contact_config);
		initBroadcastReceiver(CommonConst.BROADCAST_LOCATION_UPDATED, "ContactConfiguration");

		Intent intent = getIntent();
		String jsonStringContactDeviceData = intent.getExtras().getString(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA);
		String userName = intent.getExtras().getString(CommonConst.CONTACT_LIST_SELECTED_VALUE);

    	controller = new Controller();
    	jsonMessage = "";

    	contactDeviceDataList = Utils.fillContactDeviceDataListFromJSON(jsonStringContactDeviceData);
		if(contactDeviceDataList == null){
			return;
		}
		contactDeviceData = Utils.getContactDeviceDataByUsername(contactDeviceDataList, userName);
		if(contactDeviceData == null){
			return;
		}
		
		contactData = contactDeviceData.getContactData();
		if(contactData == null){
			return;
		}
		deviceData = contactDeviceData.getDeviceData();
		if(deviceData == null){
			return;
		}
		
		mUserName = (TextView) findViewById(R.id.username);
		mEmail = (TextView) findViewById(R.id.email);
		mDeviceName = (TextView) findViewById(R.id.devicename);
		mDeviceType = (TextView) findViewById(R.id.devicetype);
		mStatus = (TextView) findViewById(R.id.status);
		mNotification = (TextView) findViewById(R.id.notification);
		mLat = (TextView) findViewById(R.id.lat);
		mLng = (TextView) findViewById(R.id.lng);

		deviceStatus = mStatus.getText().toString();
		notification = mNotification.getText().toString();
		lat = mLat.getText().toString();
		lng = mLng.getText().toString();

		if(deviceStatus == null || deviceStatus.isEmpty()){
			mStatus.setText(DEVICE_STATUS_DEFAULT);
		}
//		} else {
//			mStatus.setText(deviceStatus);
//		}
		
		if(notification == null || notification.isEmpty()){
			mNotification.setText(NOTIFICATION_DEFAULT);
		}
//		} else {
//			mNotification.setText(notification);
//		}
		
		if(lat == null || lat.isEmpty()){
			mLat.setText(LAT_DEFAULT);
		}
//		} else {
//			mLat.setText(lat);
//		}

		if(lng == null || lng.isEmpty()){
			mLng.setText(LNG_DEFAULT);
		}
//		} else {
//			mLng.setText(lng);
//		}
		
		mUserName.setText(contactData.getNick());
		mEmail.setText(contactData.getEmail());
		mDeviceName.setText(deviceData.getDeviceName());
		mDeviceType.setText(deviceData.getDeviceTypeEnum().toString());
		
	}

    public void onClick(final View view) {

    	switch(view.getId()) {
        	case R.id.check_status:
        		checkGcmStatus();
        		break;
        	case R.id.start:
        		startTrackLocationService();
        		break;
        	case R.id.stop:
        		stopTrackLocationService();
        		break;
        	case R.id.show_map:
        		Intent intent = new Intent(getApplicationContext(), Map.class);
//        		intent.putExtra("originalTextToSearch", originalTextToSearch);
//        		intent.putExtra("searchedLocation", textSearchResult);
//        		intent.putExtra("isFavorite", isFavorite);
       			startActivity(intent);
       			break;
    	}
    	
    }
    
    private void initBroadcastReceiver(final String action, final String actionDescription)
    {
    	LogManager.LogFunctionCall(actionDescription, "initBroadcastReceiver");
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(action);
	    gcmIntentServiceChangeWatcher = new BroadcastReceiver() 
	    {
	    	@Override
    		public void onReceive(Context context, Intent intent) {
	    		LogManager.LogInfoMsg(actionDescription, "initBroadcastReceiver->onReceive", "WORK");

	    		// TODO: refactor with JSON to JAVA and back instead of string with delimiters
	    		
	    		Bundle bundle = intent.getExtras();
	    		String broadcastKeyLocationUpdated = BroadcastCommandEnum.location_updated.toString();
	    		String broadcastKeyGcmStatus = BroadcastCommandEnum.gcm_status.toString();
	    		// ===========================================
	    		// broadcast key = location_updated
	    		// ===========================================
	    		if(bundle != null){
	    			String result = null;
	    			if(bundle.containsKey(broadcastKeyLocationUpdated)){
	    				result = bundle.getString(broadcastKeyLocationUpdated);
	    			}
	    			if(bundle.containsKey(broadcastKeyGcmStatus)){
	    				result = bundle.getString(broadcastKeyGcmStatus);
	    			}
		    		if(result != null && !result.isEmpty()){
			    		mNotification.setText(result);
			    		
		    			if(result.contains(PushNotificationServiceStatusEnum.available.toString())){
		    				mStatus.setText(PushNotificationServiceStatusEnum.available.toString());
		    			}
		    		} else {
		    			mNotification.setText(result);
		    		}
	    		}
	    	}
	    };
	    
	    registerReceiver(gcmIntentServiceChangeWatcher, intentFilter);
	    LogManager.LogFunctionExit(actionDescription, "initBroadcastReceiver");
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	

	    Context context = getApplicationContext();
		Intent trackLocationService = new Intent(context, TrackLocationService.class);
		context.stopService(trackLocationService); 

		stopTrackLocationService();
		
		if(gcmIntentServiceChangeWatcher != null) {
			unregisterReceiver(gcmIntentServiceChangeWatcher);
		}
    }

    private void checkGcmStatus(){
		String regIDToReturnMessageTo = Controller.getRegistrationId(getApplicationContext());
		List<String> listRegIDs = new ArrayList<String>();
		if(contactData != null){
			listRegIDs.add(contactDeviceData.getRegistration_id());
		} else {
			LogManager.LogErrorMsg(className, "checkGcmStatus", "Unable to get registration_ID: contactData is null.");
		}
		String time = "";
		String messageString = "";
		jsonMessage = controller.createJsonMessage(listRegIDs, 
	    		regIDToReturnMessageTo, 
	    		CommandEnum.status_request, 
	    		"", // messageString, 
	    		Controller.getCurrentDate(), // time,
	    		null, //NotificationCommandEnum.pushNotificationServiceStatus.toString(),
	    		null //PushNotificationServiceStatusEnum.available.toString()
				);
		controller.sendCommand(jsonMessage);
    }
    
    private void startTrackLocationService(){
		String regIDToReturnMessageTo = Controller.getRegistrationId(getApplicationContext());
		List<String> listRegIDs = new ArrayList<String>();
		listRegIDs.add(contactDeviceData.getRegistration_id());
		String time = "";
		String messageString = "";
		jsonMessage = controller.createJsonMessage(listRegIDs, 
	    		regIDToReturnMessageTo, 
	    		CommandEnum.start, 
	    		"", // messageString, 
	    		Controller.getCurrentDate(), // time,
	    		null, //NotificationCommandEnum.pushNotificationServiceStatus.toString(),
	    		null //PushNotificationServiceStatusEnum.available.toString()
				);
		controller.sendCommand(jsonMessage);
    }

    private void stopTrackLocationService(){
		String regIDToReturnMessageToStop = Controller.getRegistrationId(getApplicationContext());
		List<String> listRegIDsStop = new ArrayList<String>();
		listRegIDsStop.add(contactDeviceData.getRegistration_id());
		jsonMessage = controller.createJsonMessage(listRegIDsStop, 
	    		regIDToReturnMessageToStop, 
	    		CommandEnum.stop, 
	    		"", // messageString, 
	    		Controller.getCurrentDate(), // time,
	    		null, // key
	    		null // value
				);
		controller.sendCommand(jsonMessage);
    }
}


