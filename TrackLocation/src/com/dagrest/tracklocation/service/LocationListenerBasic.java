package com.dagrest.tracklocation.service;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.datatype.CommandDataBasic;
import com.dagrest.tracklocation.datatype.CommandDataWithReturnToContactMap;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.MessageDataContactDetails;
import com.dagrest.tracklocation.datatype.MessageDataLocation;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class LocationListenerBasic implements LocationListener{

	protected String methodName;
	protected String logMessage;
	protected String className; // LocationListenerBasic
	protected String locationProviderType; // CommonConst.GPS = GPS, CommonConst.NETWORK = NETWORK...
	protected Context context;
	protected TrackLocationServiceBasic service;
	protected SharedPreferences prefs;
	protected String account;
	protected String macAddress;
	protected String phoneNumber;
	protected String regId;
	protected int batteryLevel;
//	object that created this certain listener - 
//	TrackLocationService - service that notifies location while tracking on map ("Locate" button)
//	TrackingService - service that notifies location automatically once in certain period of time ("Tracking" button)
	protected String objectName; 
	protected CommandEnum command;
	
	protected void init(Context context, String locationProviderType){
		this.locationProviderType = locationProviderType;
		this.context = context;
	    this.prefs = Preferences.getGCMPreferences(context);
		this.account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		this.macAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
		this.phoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
		this.regId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
		this.batteryLevel = Controller.getBatteryLevel(context);
	}
	
	public LocationListenerBasic(
			Context context, 
			TrackLocationServiceBasic service, 
			CommandEnum command,
			String className, 
			String locationProviderType, 
			String objectName) {
		this.className = className;
		this.service = service;
		this.command = command;
		this.objectName = objectName;
		init(context, locationProviderType);
	}

	@Override
	public void onLocationChanged(Location location) {
        try{
        	
        	methodName = "onLocationChanged" + " by " + objectName;
    		LogManager.LogFunctionCall(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
            
            // Fill location parameters
            double latitude = 0, longitude = 0;
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            if(latitude == 0 || longitude == 0){
            	return;
            }
            float accuracy = location.getAccuracy();
            String locationProviderName = location.getProvider();
            float speed = location.getSpeed();
            batteryLevel = Controller.getBatteryLevel(context);

            // TODO: check if the next key,value is needed...
            // Preferences.setPreferencesString(context, CommonConst.LOCATION_PROVIDER_NAME, locationProviderName);
            
            logMessage = "Provider name: " + locationProviderName + 
            	", Latitude: " + latitude + ", Longitude: " + longitude + 
            	", Accuracy: " + accuracy + ", Speed: " + speed + 
            	", Battery level: " + batteryLevel;        
    		LogManager.LogInfoMsg(className, methodName, logMessage);
            
			MessageDataContactDetails senderMessageDataContactDetails = 
    			new MessageDataContactDetails(account, macAddress, phoneNumber, regId, batteryLevel);
			MessageDataLocation locationDetails = 
				new MessageDataLocation(latitude, longitude, accuracy, speed, locationProviderName);
			
//        	if(trackLocationService != null){
//        		command = CommandEnum.location;
//        	}
//        	if (trackingService != null) {
//        		command = CommandEnum.tracking_location;
//        	}
        	
//    		ContactDeviceDataList contactDeviceDataToSendNotificationTo = 
//        			Controller.getPreferencesContactDeviceDataListToSendCommandTo(context);
//			// ==========================================
//			// send GCM (push notification) to requester
//			// ==========================================
//			CommandDataBasic commandData = new CommandData(
//					context, 
//					contactDeviceDataToSendNotificationTo,
//        			command,
//        			null, // message,
//        			senderMessageDataContactDetails, 
//        			locationDetails,			
//        			null, // key
//        			null, // value
//        			Controller.getAppInfo(context)
//			);
//			commandData.sendCommand();
            			
			CommandDataBasic commandData = new CommandDataWithReturnToContactMap(
					context, 
        			command,
        			null, // message,
        			senderMessageDataContactDetails, 
        			locationDetails,			
        			null, // key
        			null, // value
        			Controller.getAppInfo(context)
			);
			commandData.sendCommand();
			
    		// For very OLD version
            //sendLocationByMail(...);

			LogManager.LogFunctionExit(className, methodName + CommonConst.DELIMITER_ARROW + 
	                locationProviderType);
			Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName 
					+ CommonConst.DELIMITER_ARROW + locationProviderType);
        } catch (Exception e) {
        	logMessage = "Location provider type: " + locationProviderType;
    		LogManager.LogException(e, "[EXCEPTION] {" + className + "} -> " + methodName, logMessage);
    		Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
        }      
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		methodName = "onStatusChanged" + " by " + objectName;
		LogManager.LogFunctionCall(className, methodName);
		
		LogManager.LogInfoMsg(provider, className, methodName);
		service.requestLocation(true);
//		if(trackLocationService != null){
//			trackLocationService.requestLocation(true);
//		} 
//		if(trackingService != null) {
//			trackingService.requestLocation(true);
//		}
		
		LogManager.LogFunctionExit(className, methodName);
	}

	@Override
	public void onProviderEnabled(String provider) {
		methodName = "onProviderEnabled" + " by " + objectName;
		service.requestLocation(true);
//		if(trackLocationService != null){
//			trackLocationService.requestLocation(true);
//		}
//		if(trackingService != null) {
//			trackingService.requestLocation(true);
//		}
		Log.i(CommonConst.LOG_TAG, methodName);
		LogManager.LogInfoMsg(provider, className, methodName);
	}

	@Override
	public void onProviderDisabled(String provider) {
		methodName = "onProviderDisabled" + " by " + objectName;
		service.requestLocation(true);
//		if(trackLocationService != null){
//			trackLocationService.requestLocation(true);
//		}
//		if(trackingService != null){
//			trackLocationService.requestLocation(true);
//		}
		Log.i(CommonConst.LOG_TAG, methodName);
		LogManager.LogInfoMsg(provider, className, methodName);
	}

}

