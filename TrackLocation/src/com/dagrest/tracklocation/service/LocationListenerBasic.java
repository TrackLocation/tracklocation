package com.dagrest.tracklocation.service;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.datatype.CommandDataBasic;
import com.dagrest.tracklocation.datatype.CommandDataWithReturnToContactList;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.MessageDataContactDetails;
import com.dagrest.tracklocation.datatype.MessageDataLocation;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.dagrest.tracklocation.utils.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class LocationListenerBasic implements LocationListener{

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
        	
            LogManager.LogFunctionCall(className, CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
            	locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged");
            Log.i(CommonConst.LOG_TAG, "Entrance " + CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
            	locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged");
            
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

            // TODO: check if the next key,value is needed...
            // Preferences.setPreferencesString(context, CommonConst.LOCATION_PROVIDER_NAME, locationProviderName);
            
            // Create string = "latitude,longitude,accuracy,speed,time" if a location is provided
            String locationInfo = latitude + CommonConst.DELIMITER_COMMA + 
            	longitude + CommonConst.DELIMITER_COMMA + 
            	accuracy + CommonConst.DELIMITER_COMMA + 
            	speed + CommonConst.DELIMITER_COMMA + 
            	Utils.getCurrentTime() + CommonConst.DELIMITER_COMMA +
            	account;
                    
            LogManager.LogInfoMsg(className, CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
            	locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged", 
            	CommonConst.LOCATION_INFO_ + locationProviderType + CommonConst.DELIMITER_COLON + locationInfo);
            
            
			MessageDataContactDetails contactDetails = 
    			new MessageDataContactDetails(account, macAddress, phoneNumber, regId, 
    					Controller.getBatteryLevel(context));
			MessageDataLocation locationDetails = 
				new MessageDataLocation(latitude, longitude, accuracy, speed, locationProviderName);
			CommandDataBasic commandData = new CommandDataWithReturnToContactList(
					context, 
        			CommandEnum.location,
        			null, // message,
        			contactDetails, 
        			locationDetails,			
        			null, // key
        			null, // value
        			Controller.getAppInfo(context)
			);
			commandData.sendCommand();
            
            
            
//            // ==========================================
//            // send GCM (push notification) to requester
//            // ==========================================
//			// List<String> listRegIDs = Preferences.getPreferencesReturnToRegIDList(context); 
//			List<String> listRegIDs = Controller.getPreferencesReturnToRegIDList(context);
//
//			String time = new Date().toString(); 
//
//    		MessageDataContactDetails messageDataContactDetails = 
//    			new MessageDataContactDetails(account, macAddress, phoneNumber, regId, batteryLevel);
//       		MessageDataLocation messageDataLocation = 
//       			new MessageDataLocation(latitude, longitude, accuracy, speed, locationProviderName);
//       		
//			// Get current registration ID
////    		String senderRegId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
//    		String jsonMessage = Controller.createJsonMessage(
//    			new JsonMessageData(
//	    			listRegIDs, 
////		    		senderRegId, 
//		    		CommandEnum.location, 
//		    		null, // TODO: send device UUID in the message 
//		    		messageDataContactDetails,
//		    		messageDataLocation,
//		    		null, // application info
//		    		time,
//		    		locationProviderType, // key
//		    		locationInfo// value
//		    	)
//    		);
//			if(jsonMessage == null){
//				errorMsg = "Failed to create JSON Message to send to recipient";
//				LogManager.LogErrorMsg(className, "[Command:" + CommandEnum.location.toString() + "]", errorMsg);
//				return;
//			} else {
//	    		// send message back with PushNotificationServiceStatusEnum.available
//	    		Controller.sendCommand(jsonMessage);
//			}
//            // ==============================
//            // send GCM to requester
//            // ==============================
    		
    		// For very OLD version
            //sendLocationByMail(latlong);

            LogManager.LogFunctionExit(className, CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
                locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged");
            Log.i(CommonConst.LOG_TAG, "Exit " + CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
                locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged");

        } catch (Exception e) {
                LogManager.LogException(e, className, CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
                    locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged");
                Log.e(CommonConst.LOG_TAG, "Exception " + CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
                    locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged", e);
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

