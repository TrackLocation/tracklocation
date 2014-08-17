package com.dagrest.tracklocation;

import java.util.ArrayList;
import java.util.List;

import com.dagrest.tracklocation.datatype.AppInfo;
import com.dagrest.tracklocation.datatype.BroadcastActionEnum;
import com.dagrest.tracklocation.datatype.BroadcastKeyEnum;
import com.dagrest.tracklocation.datatype.CommandData;
import com.dagrest.tracklocation.datatype.CommandDataBasic;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.DeviceData;
import com.dagrest.tracklocation.datatype.MessageDataContactDetails;
import com.dagrest.tracklocation.datatype.MessageDataLocation;
import com.dagrest.tracklocation.datatype.PushNotificationServiceStatusEnum;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.service.TrackLocationService;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ContactConfiguration extends Activity {

	private static final String DEVICE_STATUS_DEFAULT = "N/A";
	private static final String NOTIFICATION_DEFAULT = "N/A";
	
	private TextView mUserName;
	private TextView mEmail;
	private TextView mDeviceName;
	private TextView mDeviceType;
	private TextView mStatus;
	private TextView mNotification;
	
	private String deviceStatus;
	private String notification;

	private ContactDeviceDataList contactDeviceDataList;
	private ContactDeviceDataList selectedContactDeviceDataList;
	private ContactDeviceData contactDeviceData;
	private ContactData contactData;
	private DeviceData deviceData;
	private String className;

	private BroadcastReceiver gcmIntentServiceChangeWatcher;
	
	private Controller controller;
	
	private	AppInfo appInfo;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		className = this.getClass().getName();
		setContentView(R.layout.contact_config);
		initBroadcastReceiver(BroadcastActionEnum.BROADCAST_LOCATION_UPDATED.toString(), "ContactConfiguration");

		Intent intent = getIntent();
		String jsonStringContactDeviceDataList = intent.getExtras().getString(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST);
		String selectedContactID = intent.getExtras().getString(CommonConst.CONTACT_LIST_SELECTED_VALUE);

    	contactDeviceDataList = Utils.fillContactDeviceDataListFromJSON(jsonStringContactDeviceDataList);
		if(contactDeviceDataList == null){
			// TODO: error message to log
			return;
		}
		
		List<String> selectedContcatList = new ArrayList<String>();
		selectedContcatList.add(selectedContactID);
		selectedContactDeviceDataList = Controller.removeNonSelectedContacts(contactDeviceDataList, selectedContcatList);
		
		contactDeviceData = Utils.getContactDeviceDataByUsername(contactDeviceDataList, selectedContactID);
		if(contactDeviceData == null){
			// TODO: error message to log
			return;
		}
		
		contactData = contactDeviceData.getContactData();
		if(contactData == null){
			// TODO: error message to log
			return;
		}
		deviceData = contactDeviceData.getDeviceData();
		if(deviceData == null){
			// TODO: error message to log
			return;
		}
		
		mUserName = (TextView) findViewById(R.id.username);
		mEmail = (TextView) findViewById(R.id.email);
		mDeviceName = (TextView) findViewById(R.id.devicename);
		mDeviceType = (TextView) findViewById(R.id.devicetype);
		mStatus = (TextView) findViewById(R.id.status);
		mNotification = (TextView) findViewById(R.id.notification);

		deviceStatus = mStatus.getText().toString();
		notification = mNotification.getText().toString();

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
		
		mUserName.setText(contactData.getNick());
		mEmail.setText(contactData.getEmail());
		mDeviceName.setText(deviceData.getDeviceName());
		mDeviceType.setText(deviceData.getDeviceTypeEnum().toString());
		
	}

    public void onClick(final View view) {

		MessageDataContactDetails contactDetails = null;
		MessageDataLocation location = null;
		String message = null, key = null, value = null;

    	controller = new Controller();
    	switch(view.getId()) {
        	case R.id.check_status:
        		//Controller.checkGcmStatus(getApplicationContext(), contactData, contactDeviceData);
//        		Controller.sendCommand(getApplicationContext(), selectedContactDeviceDataList, 
//        			CommandEnum.status_request, null, contactDetails, location, null, null, null);
        		CommandDataBasic commandDataBasic = new CommandData(
        			getApplicationContext(), 
        			contactDeviceDataList, 
        			CommandEnum.status_request,
        			message,
        			contactDetails, 
        			location,
        			key,
        			value,
        			appInfo
        		);
        		commandDataBasic.sendCommand();
        		break;
        	case R.id.start:
        		//Controller.startTrackLocationService(getApplicationContext(), contactDeviceData);
//        		Controller.sendCommand(getApplicationContext(), selectedContactDeviceDataList, 
//        			CommandEnum.status_request, null, contactDetails, location, null, null, null);
//        		Controller.sendCommand(getApplicationContext(), selectedContactDeviceDataList, 
//        			CommandEnum.start, null, contactDetails, location, null, null, null);
        		commandDataBasic = new CommandData(
        			getApplicationContext(), 
        			contactDeviceDataList, 
        			CommandEnum.status_request,
        			message,
        			contactDetails, 
        			location,
        			key,
        			value,
        			appInfo
        		);
        		commandDataBasic.sendCommand();
        		
        		commandDataBasic = new CommandData(
        			getApplicationContext(), 
        			contactDeviceDataList, 
        			CommandEnum.start,
        			message,
        			contactDetails, 
        			location,
        			key,
        			value,
        			appInfo
        		);
        		commandDataBasic.sendCommand();
        		
        		controller.keepAliveTrackLocationService(getApplicationContext(), selectedContactDeviceDataList, 500);
        		break;
        	case R.id.stop:
        		//Controller.stopTrackLocationService(getApplicationContext(), contactDeviceData);
//        		Controller.sendCommand(getApplicationContext(), selectedContactDeviceDataList, 
//        			CommandEnum.stop, null, null);
        		controller.stopKeepAliveTrackLocationService();
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
	    		String broadcastKeyLocationUpdated = BroadcastKeyEnum.location_updated.toString();
	    		String broadcastKeyGcmStatus = BroadcastKeyEnum.gcm_status.toString();
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

		MessageDataContactDetails contactDetails = null;
		MessageDataLocation location = null;
		String message = null, key = null, value = null;

		//Controller.stopTrackLocationService(context, contactDeviceData);
//		Controller.sendCommand(getApplicationContext(), contactDeviceDataList, 
//			CommandEnum.stop, null, contactDetails, location, null, null, null);
		CommandDataBasic commandDataBasic = new CommandData(
			getApplicationContext(), 
			contactDeviceDataList, 
			CommandEnum.stop,
			message,
			contactDetails, 
			location,
			key,
			value,
			appInfo
		);
		commandDataBasic.sendCommand();
		
		if(gcmIntentServiceChangeWatcher != null) {
			unregisterReceiver(gcmIntentServiceChangeWatcher);
		}
    }
}


