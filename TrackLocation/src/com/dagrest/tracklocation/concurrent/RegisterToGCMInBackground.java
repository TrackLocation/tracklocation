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
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.MessageDataContactDetails;
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
	    				
	    				
	    				
//	    	    		int clientBatteryLevel = Controller.getBatteryLevel(context);
//	    	            MessageDataContactDetails messageDataContactDetails = new MessageDataContactDetails(clientAccount, 
//	    	                clientMacAddress, clientPhoneNumber, clientRegId, clientBatteryLevel);
//	    	            ContactDeviceDataList contactDeviceDataToSendNotificationTo = 
//	    	            	new ContactDeviceDataList (
//	    	            		senderMessageDataContactDetails.getAccount(), 
//	    	            		senderMessageDataContactDetails.getMacAddress(), 
//	    	            		senderMessageDataContactDetails.getPhoneNumber(), 
//	    	            		senderMessageDataContactDetails.getRegId(), 
//	    	            		null);
//	    	            // Notify caller by GCM (push notification)
//	    	            
//	    	            String msgServiceStarted = "{" + className + "} TrackLocationService was started by [" + senderMessageDataContactDetails.getAccount() + "]";
//	    	            String notificationKey = CommandKeyEnum.start_status.toString();
//	    	            String notificationValue = CommandValueEnum.success.toString();		
//
//	    	            AppInstDetails appInstDetails = new AppInstDetails(context); 
//	    	            CommandDataBasic commandDataBasic = new CommandData(
//	    					context, 
//	    					contactDeviceDataToSendNotificationTo, 
//	    	    			CommandEnum.notification, 
//	    	    			msgServiceStarted, 
//	    	    			messageDataContactDetails, 
//	    	    			null, 					// location
//	    	    			notificationKey, 		// key
//	    	    			notificationValue,  	// value
//	    	    			appInstDetails.getAppInfo()
//	    	    		);
//	    	            commandDataBasic.sendCommand();

	    				
	    				
	    				
	    				
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

}
