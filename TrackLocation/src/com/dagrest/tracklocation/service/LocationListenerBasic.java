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
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class LocationListenerBasic implements LocationListener{

	private String methodName;
	private String logMessage;
	private String className; // LocationListenerBasic
	private String locationProviderType; // CommonConst.GPS = GPS, CommonConst.NETWORK = NETWORK...
	private Context context;
	private TrackLocationService trackLocationService;
	private final SharedPreferences prefs;
	private String account;
	private String macAddress;
	private String phoneNumber;
	private String regId;
	private int batteryLevel;
	
	public LocationListenerBasic(Context context, TrackLocationService trackLocationService, String className, String locationProviderType) {
		this.className = className;
		this.locationProviderType = locationProviderType;
		this.context = context;
		this.trackLocationService = trackLocationService;
	    this.prefs = Preferences.getGCMPreferences(context);
		this.account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		this.macAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
		this.phoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
		this.regId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
		this.batteryLevel = Controller.getBatteryLevel(context);
	}

	@Override
	public void onLocationChanged(Location location) {
        try{
        	
        	methodName = "onLocationChanged";
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
			
//    		ContactDeviceDataList contactDeviceDataToSendNotificationTo = 
//        			Controller.getPreferencesContactDeviceDataListToSendCommandTo(context);
//			// ==========================================
//			// send GCM (push notification) to requester
//			// ==========================================
//			CommandDataBasic commandData = new CommandData(
//					context, 
//					contactDeviceDataToSendNotificationTo,
//        			CommandEnum.location,
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
        			CommandEnum.location,
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
		LogManager.LogFunctionCall("LocationListenerBasic", "onStatusChanged");
		
		LogManager.LogInfoMsg(provider, "LocationListenerBasic", "onStatusChanged");
		trackLocationService.requestLocation(true);
		
		LogManager.LogFunctionExit("LocationListenerBasic", "onStatusChanged");
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		trackLocationService.requestLocation(true);
		Log.i(CommonConst.LOG_TAG, "onProviderEnabled");
		LogManager.LogInfoMsg(provider, "LocationListenerBasic", "onProviderEnabled");
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		trackLocationService.requestLocation(true);
		Log.i(CommonConst.LOG_TAG, "onProviderDisabled");
		LogManager.LogInfoMsg(provider, "LocationListenerBasic", "onProviderDisabled");
	}

}

