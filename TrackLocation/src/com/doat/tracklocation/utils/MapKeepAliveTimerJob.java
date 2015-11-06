package com.doat.tracklocation.utils;

import java.util.TimerTask;

import android.content.Context;
import android.util.Log;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.datatype.AppInfo;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.CommandData;
import com.doat.tracklocation.datatype.CommandDataBasic;
import com.doat.tracklocation.datatype.CommandEnum;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.datatype.MessageDataLocation;
import com.doat.tracklocation.exception.UnableToSendCommandException;
import com.doat.tracklocation.log.LogManager;

public class MapKeepAliveTimerJob extends TimerTask {

	private Context context;
	private ContactDeviceDataList selectedContactDeviceDataList;
	private String methodName = "MapKeepAliveTimerJob";
	private String className;
	
	public void setContext(Context context){
		this.context = context;
	}
	
	public void setSelectedContactDeviceDataList(
			ContactDeviceDataList selectedContactDeviceDataList) {
		this.selectedContactDeviceDataList = selectedContactDeviceDataList;
	}

	@Override
	public void run() {
		className = this.getClass().getName();
		methodName = "MapKeepAliveTimerJob->run";
		
		if(context != null && selectedContactDeviceDataList != null){
			
			String ownerEmail = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
			String ownerMacAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
			String ownerRegId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
			String ownerPhoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
			float batteryPercentage = Controller.getBatteryLevel(context);

			MessageDataContactDetails senderMessageDataContactDetails = 
				new MessageDataContactDetails(ownerEmail, ownerMacAddress, ownerPhoneNumber, ownerRegId, batteryPercentage);
			MessageDataLocation location = null;
			AppInfo appInfo = Controller.getAppInfo(context);
			
			CommandDataBasic commandDataBasic;
			try {
				commandDataBasic = new CommandData(
					context, 
					selectedContactDeviceDataList, 
					CommandEnum.track_location_service_keep_alive, 
					null, // message
					senderMessageDataContactDetails, 
					location, 
					BroadcastKeyEnum.keep_alive.toString(), 
					Long.toString(System.currentTimeMillis()),
					appInfo
				);
				commandDataBasic.sendCommand();
			} catch (UnableToSendCommandException e) {
				LogManager.LogException(e, className, methodName);
				Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
			}
	   		
	    	Log.i(CommonConst.LOG_TAG, "KeepAlive command sent to trackLocationService from mapKeepAliveTimerJob");
		} else {
			// TODO: error to log 
			// Cannot start MapKeepAliveTimerJob - no context provided
			Log.e(CommonConst.LOG_TAG, "Cannot start MapKeepAliveTimerJob - no context or selectedContactDeviceDataList provided");
		}
	}
}
