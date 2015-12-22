package com.doat.tracklocation.service;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.datatype.CommandDataBasic;
import com.doat.tracklocation.datatype.CommandDataWithReturnToContactMap;
import com.doat.tracklocation.datatype.CommandEnum;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.datatype.MessageDataLocation;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;
import com.doat.tracklocation.utils.Utils;
import com.google.gson.Gson;

public class TrackLocationServiceLocationListener implements ILocationListener {

	private static Gson gson = new Gson();
	private final String className = this.getClass().getName();
	private String logMessage;
	private String methodName;
	private Context context;
	private MessageDataContactDetails senderMessageDataContactDetails;
	private final static int MAX_LOCATION_LIST_SIZE = 3;
	// List of locations - needed to decide which location to send GPS or NETWORK
	private List<MessageDataLocation> locationList;
	
	public TrackLocationServiceLocationListener(Context context) {
		this.context = context;
		locationList = new ArrayList<MessageDataLocation>();
		senderMessageDataContactDetails = Utils.initLocalRecipientData(context);
	}

	@Override
	public void onLocationChanged(MessageDataLocation locationDetails) {
		
		methodName = "onLocationChanged"; 
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
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
		
		addLocationList(locationDetails);
		String providerName = locationDetails.getLocationProviderType();

		String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		int batteryLevel = Controller.getBatteryLevel(context);
		List<String> recitientsList = commandData.getListAccounts();
		String recitientsListString = "";
		if(recitientsList != null && !recitientsList.isEmpty()){
			recitientsListString = gson.toJson(recitientsList);
		}
        logMessage = "Location info:\nProvider name: " + locationDetails.getLocationProviderType() + 
            	", Latitude: " + locationDetails.getLat() + ", Longitude: " + locationDetails.getLng() + 
            	", Accuracy: " + locationDetails.getAccuracy() + ", Speed: " + locationDetails.getSpeed() + 
            	", Battery level: " + batteryLevel +
            	"\nhas been sent to the following recipients: " + recitientsListString +
            	"\nby [" + account + "]";        

        if(locationList.isEmpty() || // If current location is a first one - send it
        	"gps".equalsIgnoreCase(providerName) || // If current location from GPS provider - send it
        	// If current location from NETWORK provider and there were no any GPS provider location before - send it
			(isGPSLocationUnavailable() == true && "network".equalsIgnoreCase(providerName))){ 
			commandData.sendCommand();
		} else {
			String logMessageTemp = "Current loaction will not be send:\n" + logMessage;
			LogManager.LogInfoMsg(className, methodName, logMessageTemp);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessageTemp);
		}
		
        LogManager.LogInfoMsg(className, methodName, logMessage);
        Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
        
        LogManager.LogFunctionExit(className, methodName);
        Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

	private boolean isGPSLocationUnavailable(){
		for (MessageDataLocation messageDataLocation : locationList) {
			if("gps".equalsIgnoreCase(messageDataLocation.getLocationProviderType())){
				return false;
			}
		}
		return true;
	}
	
	private void addLocationList(MessageDataLocation locationDetails){
		if(locationList.size() == MAX_LOCATION_LIST_SIZE){
			locationList.remove(0);
		}
		locationList.add(locationDetails);
	}
	
	@Override
	public void onStatusChanged(MessageDataLocation locationDetails) {
		methodName = "onStatusChanged";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		// TODO Auto-generated method stub
		logMessage = "Location provider = " + locationDetails.getLocationProviderType();
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		
        LogManager.LogFunctionExit(className, methodName);
        Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

	@Override
	public void onProviderEnabled(MessageDataLocation locationDetails) {
		methodName = "onProviderEnabled";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		// TODO Auto-generated method stub
		logMessage = "Location provider = " + locationDetails.getLocationProviderType();
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		
        LogManager.LogFunctionExit(className, methodName);
        Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

	@Override
	public void onProviderDisabled(MessageDataLocation locationDetails) {
		methodName = "onProviderDisabled";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		// TODO Auto-generated method stub
		logMessage = "Location provider = " + locationDetails.getLocationProviderType();
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		
        LogManager.LogFunctionExit(className, methodName);
        Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

}
