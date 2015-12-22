package com.doat.tracklocation.service;

import com.doat.tracklocation.datatype.MessageDataLocation;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.google.gson.Gson;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class LocationListenerBasic implements LocationListener{

	protected static Gson gson = new Gson();
	protected String methodName;
	protected String logMessage;
	protected String className; // LocationListenerBasic
	protected String locationProviderType; // CommonConst.GPS = GPS, CommonConst.NETWORK = NETWORK...
	protected Context context;
	protected TrackLocationServiceBasic service;
//	protected SharedPreferences prefs;
//	protected String account;
//	protected String macAddress;
//	protected String phoneNumber;
//	protected String regId;
//	protected int batteryLevel;
	protected String objectName; 
	protected ILocationListener locationListener;
	
//	protected void init(Context context, String locationProviderType){
//		this.locationProviderType = locationProviderType;
//		this.context = context;
//	    this.prefs = Preferences.getGCMPreferences(context);
//		this.account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
//		this.macAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
//		this.phoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
//		this.regId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
//		this.batteryLevel = Controller.getBatteryLevel(context);
//	}
	
	public LocationListenerBasic(
			Context context, 
			TrackLocationServiceBasic service, 
			String className, 
			String locationProviderType, 
			String objectName,
			ILocationListener locationListener) {
		this.className = className;
		this.service = service;
		this.objectName = objectName;
		this.locationProviderType = locationProviderType;
//		init(context, locationProviderType);
		this.locationListener = locationListener;
	}

	@Override
	public void onLocationChanged(Location location) {
		
		methodName = "onLocationChanged" + " by " + objectName;
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		try{
            // Fill location parameters
            double latitude = 0, longitude = 0;
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            if(latitude == 0 || longitude == 0){
            	return;
            }
            float accuracy = location.getAccuracy();
            String locationProviderName = location.getProvider();
            double speed = location.getSpeed() * 3.6; // 3600/1000 - speed converted from m/s to km/h
//            batteryLevel = Controller.getBatteryLevel(context);

//			MessageDataContactDetails senderMessageDataContactDetails = 
//    			new MessageDataContactDetails(account, macAddress, phoneNumber, regId, batteryLevel);
			MessageDataLocation locationDetails = 
				new MessageDataLocation(latitude, longitude, accuracy, speed, locationProviderName);
			
			locationListener.onLocationChanged(locationDetails);
			
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
/*            			
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

			List<String> recitientsList = commandData.getListAccounts();
			String recitientsListString = "";
			if(recitientsList != null && !recitientsList.isEmpty()){
				recitientsListString = gson.toJson(recitientsList);
			}

            logMessage = "Location info:\nProvider name: " + locationProviderName + 
                	", Latitude: " + latitude + ", Longitude: " + longitude + 
                	", Accuracy: " + accuracy + ", Speed: " + speed + 
                	", Battery level: " + batteryLevel +
                	"\nhas been sent to the following recipients: " + recitientsListString +
                	"\nby [" + account + "]";        
            LogManager.LogInfoMsg(className, methodName, logMessage);
            Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
*/
			LogManager.LogFunctionExit(className, methodName + CommonConst.DELIMITER_ARROW + 
	                locationProviderType);
			Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName 
					+ CommonConst.DELIMITER_ARROW + locationProviderType);
        } catch (Exception e) {
        	logMessage = "Location provider type: " + locationProviderType;
    		LogManager.LogException(e, "[EXCEPTION] {" + className + "} -> " + methodName, logMessage);
    		Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
        } finally {
    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
        }
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		methodName = "onStatusChanged" + " by " + objectName;
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
//		service.requestLocation(true);
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

	@Override
	public void onProviderEnabled(String provider) {
		methodName = "onProviderEnabled" + " by " + objectName;
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
//		service.requestLocation(true);

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

	@Override
	public void onProviderDisabled(String provider) {
		methodName = "onProviderDisabled" + " by " + objectName;
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
//		service.requestLocation(true);

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

}

