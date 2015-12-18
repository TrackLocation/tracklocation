package com.doat.tracklocation.utils;

import java.util.List;
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
	private String logMessage;
	
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
		methodName = "run";
		
		logMessage = "MapKeepAliveTimerJob waked up. ThreadID: " + Thread.currentThread().getId();
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> ThreadID: " + Thread.currentThread().getId());

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
			
			List<String> accountList = Controller.getAccountListFromContactDeviceDataList(selectedContactDeviceDataList);
			
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
				
				logMessage = "KeepAlive command sent to trackLocationService of [" + accountList.toString() + "] from mapKeepAliveTimerJob by [" + ownerEmail + "]";
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			} catch (UnableToSendCommandException e) {
				LogManager.LogException(e, className, methodName);
				Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
			}
	   		
		} else {
			// Cannot start MapKeepAliveTimerJob - no context provided
			logMessage = "Cannot start MapKeepAliveTimerJob - no context or selectedContactDeviceDataList provided";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
}
