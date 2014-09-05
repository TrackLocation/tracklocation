package com.dagrest.tracklocation.concurrent;

import java.io.IOException;

import android.content.Context;
import android.util.Log;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.datatype.AppInstDetails;
import com.dagrest.tracklocation.datatype.CommandData;
import com.dagrest.tracklocation.datatype.CommandDataBasic;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.CommandKeyEnum;
import com.dagrest.tracklocation.datatype.CommandValueEnum;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.MessageDataContactDetails;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class RegisterToGCMInBackground implements Runnable {
	private final static int MAX_REGISTARTION_RETRY_TIMES = 5;
	private final static int SLEEP_TIME = 1000;
	private GoogleCloudMessaging gcm;
	private Context context;
	private String googleProjectNumber; 
	private String registrationId;
	private String className; // className = this.getClass().getName();
	private String logMessage;
	private String methodName;
	
	private String clientAccount;
	private String clientMacAddress;
	private String clientPhoneNumber;
	private String clientRegId;
	private int clientBatteryLevel;

	
	public RegisterToGCMInBackground(Context context, GoogleCloudMessaging gcm, 
			String googleProjectNumber) {
		super();
		methodName = "RegisterToGCMInBackground";
		this.gcm = gcm;
		this.context = context;
		this.googleProjectNumber = googleProjectNumber;
		className = this.getClass().getName();
	}

	@Override
	public void run() {
		methodName = "run";
        for(int i = 0; i < MAX_REGISTARTION_RETRY_TIMES; i++){
	        try {
	            if (gcm == null) {
	                gcm = GoogleCloudMessaging.getInstance(context);
	            }
	             	registrationId = gcm.register(googleProjectNumber);
	            	if(registrationId != null && !registrationId.isEmpty()){
	                    // Persist the registration ID - no need to register again.
	                    Preferences.setPreferencesString(context, CommonConst.PREFERENCES_REG_ID, registrationId);
						logMessage = "Retry " + (i+1) + ". Successfully finished a Google Cloud Message Registration. ThreadID = " + 
	    					Thread.currentThread().getId();
	    				LogManager.LogInfoMsg(className, methodName, logMessage);
	    				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
	    				
	    				// ============================================================================
	    				// Update a new registratioID for all known accounts - START BLOCK
	    				// ============================================================================
	    				
	    				String sendToAccount = null;
	    				String sendToMacAddress = null;
	    				String sendToPhoneNumber = null;
	    				String sendToRegId = null;
	    				
	    				initClientDetails();
	    				ContactDeviceDataList contDevDataList = DBLayer.getContactDeviceDataList(null);
	    				if(contDevDataList != null){
	    					// get owner information from DB and save GUID to Preferences
	    					for (ContactDeviceData cdd : contDevDataList.getContactDeviceDataList()) {
	    						if(cdd != null && cdd.getContactData() != null){
	    							sendToAccount = cdd.getContactData().getEmail();
	    						} else {
	    							logMessage = "Unable to send " + CommandEnum.update_reg_id.toString() + 
	    								" command - no account";
	    							LogManager.LogErrorMsg(className, methodName, logMessage);
	    							Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
	    							continue;
	    						}
	    						if(cdd != null && cdd.getDeviceData() != null){
	    							sendToMacAddress = cdd.getDeviceData().getDeviceMac();
	    						} else {
	    							logMessage = "Unable to send " + CommandEnum.update_reg_id.toString() + 
		    							" command - no mac address";
		    						LogManager.LogErrorMsg(className, methodName, logMessage);
		    						Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
	    							continue;
	    						}
	    						sendToPhoneNumber = cdd.getPhoneNumber();
	    						sendToRegId = cdd.getRegistration_id();
	    						if(sendToRegId == null || sendToRegId.isEmpty()){
	    							logMessage = "Unable to send " + CommandEnum.update_reg_id.toString() + 
			    						" command - registrationID is null or EMPTY";
			    					LogManager.LogErrorMsg(className, methodName, logMessage);
			    					Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		    						continue;
	    						}
	    						
	    	    	    		clientBatteryLevel = Controller.getBatteryLevel(context);
	    	    	            MessageDataContactDetails messageDataContactDetails = new MessageDataContactDetails(clientAccount, 
	    	    	                clientMacAddress, clientPhoneNumber, clientRegId, clientBatteryLevel);
	    	    	            ContactDeviceDataList contactDeviceDataToSendNotificationTo = 
	    	    	            	new ContactDeviceDataList (
	    	    	            		sendToAccount, 
	    	    	            		sendToMacAddress, 
	    	    	            		sendToPhoneNumber, 
	    	    	            		sendToRegId, 
	    	    	            		null);
	    	    	            // Notify caller by GCM (push notification)
	    	    	            
	    	    	            String commandMessage = "Update registartionID of [" + clientAccount + "]";
	    	    	            String notificationKey = CommandKeyEnum.updated_reg_id.toString();
	    	    	            String notificationValue = registrationId;		

	    	    	            AppInstDetails appInstDetails = new AppInstDetails(context); 
	    	    	            CommandDataBasic commandDataBasic = new CommandData(
	    	    					context, 
	    	    					contactDeviceDataToSendNotificationTo, 
	    	    	    			CommandEnum.update_reg_id, 
	    	    	    			commandMessage, 
	    	    	    			messageDataContactDetails, 
	    	    	    			null, 					// location
	    	    	    			notificationKey, 		// key
	    	    	    			notificationValue,  	// value
	    	    	    			appInstDetails.getAppInfo()
	    	    	    		);
	    	    	            commandDataBasic.sendCommand();

	    					}
	    				} else {
	    		        	String errMsg = "Failed to send updated registartionID to the following account: " + sendToAccount;
	    		        	Log.e(CommonConst.LOG_TAG, errMsg);
	    		            LogManager.LogErrorMsg(className, methodName, errMsg);
	    				}
	    				
	    				// ============================================================================
	    				// Update a new registratioID for all known accounts - END BLOCK
	    				// ============================================================================
	    				
	    				break;
	            	} else {
	            		// SLEEP START - SLEEP_TIME in milliseconds
	    				try {
	    					Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> Sleep: " + SLEEP_TIME / 1000 + " sec");
	    					Thread.sleep(SLEEP_TIME);
	    				} catch (InterruptedException e) {
	    					//if(exitNow == true){
	    					logMessage = "Failed the thread with loop for Google Cloud Message Registration. ThreadID = " + 
	    						Thread.currentThread().getId();
	    					LogManager.LogInfoMsg(className, methodName, logMessage);
	    					Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
	    					break;
	    					//}
	    				}
	    				// SLEEP END - SLEEP_TIME in milliseconds
	            	}
	            	// msg = "Device registered, registration ID: " + registrationId;
	        } catch (IOException ex) {
	            // msg = "Error :" + ex.getMessage();
	            // If there is an error, don't just keep trying to register.
	            // Require the user to click a button again, or perform
	            // exponential back-off.
	        	logMessage = "Exception caught: " + ex.getMessage();
	        	Log.e(CommonConst.LOG_TAG, logMessage, ex);
	            LogManager.LogErrorMsg(className, methodName, logMessage);
	        }
        }
	}
	
	private void initClientDetails(){
		clientAccount = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		clientMacAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
		clientPhoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
		clientRegId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
	}
}
