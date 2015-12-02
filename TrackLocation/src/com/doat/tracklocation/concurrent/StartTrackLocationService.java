package com.doat.tracklocation.concurrent;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.CommandData;
import com.doat.tracklocation.datatype.CommandDataBasic;
import com.doat.tracklocation.datatype.CommandEnum;
import com.doat.tracklocation.datatype.CommandValueEnum;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.datatype.NotificationBroadcastData;
import com.doat.tracklocation.exception.UnableToSendCommandException;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;
import com.google.gson.Gson;

/**
 * 
 * Send GCM command to start Track Location Service
 * 
 * @author dagrest@gmail.com
 *
 */
public class StartTrackLocationService implements Runnable {

	private final static int DELAY_IN_MS = 1000;
	
	private Context context;
	private ContactDeviceDataList selectedContactDeviceDataList;
	private MessageDataContactDetails senderMessageDataContactDetails;
	private int retryTimes;
	private int delay;
	private String className;
	private List<String> tempListAccounts;
	private volatile boolean exitNow;
	private String methodName;
	private String logMessage;

	public StartTrackLocationService(
        	Context context,
        	ContactDeviceDataList selectedContactDeviceDataList,
        	MessageDataContactDetails senderMessageDataContactDetails,
        	int retryTimes,
        	int delay) {
		exitNow = false;
		this.context = context;
		this.selectedContactDeviceDataList = selectedContactDeviceDataList;
		this.senderMessageDataContactDetails = senderMessageDataContactDetails;
		this.retryTimes = retryTimes;
		this.delay = delay; // in milliseconds
		className = this.getClass().getName();
	}
	
	public boolean isExitNow() {
		return exitNow;
	}

	public void setExitNow(boolean exitNow) {
		this.exitNow = exitNow;
	}

	@Override
	public void run() {
		
		methodName = "Create a new thread [ID = " + Thread.currentThread().getId() + 
			"] with loop for sending of the command to start TrackLocationService -> run()";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
				
        if(senderMessageDataContactDetails == null){
        	logMessage = "Failed to send GCM command to start Track Location Service" +
        		" - no sender contact details provided.";
        	LogManager.LogErrorMsg(className, methodName, logMessage);
        	Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
        	return;
        }
        
        if(selectedContactDeviceDataList == null){
        	logMessage = "Failed to send GCM command to start Track Location Service" +
            	" - no selected contact details provided.";
        	LogManager.LogErrorMsg(className, methodName, logMessage);
        	Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
        	return;
        }

        CommandDataBasic commandDataBasic = null;
		try {
			commandDataBasic = new CommandData(
					context, 
					selectedContactDeviceDataList, 
					CommandEnum.start,	// [START]	
					null, 				// message
					senderMessageDataContactDetails, 
					null,				// location
					null, 				// key
					null, 				// value,
					null 				// appInfo
			);
		} catch (UnableToSendCommandException e) {
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
			return;
		}
			        
        // String currentAccount = senderMessageDataContactDetails.getAccount();
        
        Gson gson = new Gson();
        String jsonListAccounts = gson.toJson(commandDataBasic.getListAccounts());
        // Set list of recipients' accounts list 
        Preferences.setPreferencesString(context, 
        		CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS, jsonListAccounts);
        Log.i(CommonConst.LOG_TAG, "Saved recipients: " + jsonListAccounts);
        List<String> listAccounts = null;
		for (int i = 0; i < retryTimes; i++) {
			jsonListAccounts = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS);
	        Log.i(CommonConst.LOG_TAG, "Inside loop for recipients: " + jsonListAccounts);

			if(jsonListAccounts == null || jsonListAccounts.isEmpty()){
				break;
			}
			listAccounts = gson.fromJson(jsonListAccounts, List.class);
			if(listAccounts == null || listAccounts.isEmpty()){
				break;
			}

			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> LOOP: " + (i+1) + 
				" ThreadID: " + Thread.currentThread().getId());
			commandDataBasic.sendCommand();
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> LOOP: " + (i+1) + 
				" [START] command sent to recipients: " + jsonListAccounts);
			
			for(int j = 0; j < (delay / 1000); j++){
				try {
					Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> Sleep: " + DELAY_IN_MS / 1000 + " sec");
					Thread.sleep(DELAY_IN_MS);
				} catch (InterruptedException e) {
					//if(exitNow == true){
					logMessage = "Finish the thread with loop for starting of TrackLocation Service. ThreadID = " + 
						Thread.currentThread().getId();
					LogManager.LogInfoMsg(className, methodName, logMessage);
					Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
					return;
					//}
				}
			}
			
			jsonListAccounts = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS);
			if(jsonListAccounts != null && !jsonListAccounts.isEmpty()){
				tempListAccounts = gson.fromJson(jsonListAccounts, List.class);
				if(!tempListAccounts.isEmpty()){
			        String broadcastMessage = "Retry " + (i+1) + " from " + retryTimes + 
			        	":\nCurrently unavailable:\n\n";
			        for (String currentAccount : tempListAccounts) {
			        	broadcastMessage = broadcastMessage + currentAccount + "\n";
					}
			        broadcastMessage += "\nPlease wait...";
				    broadcsatMessage(context, broadcastMessage, BroadcastKeyEnum.start_status.toString(), CommandValueEnum.wait.toString());
				}
			}

		}

		// Reset list of recipients' accounts list to be empty
        Preferences.setPreferencesString(context, CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS, "");
        Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> : Recipients list after loop: " + jsonListAccounts);
        tempListAccounts = gson.fromJson(jsonListAccounts, List.class);
        String broadcastMessage = "\nCurrently unavailable:\n\n";
        for (String currentAccount : tempListAccounts) {
        	broadcastMessage = broadcastMessage + currentAccount + "\n";
		}
        broadcastMessage += "\n";
        if(tempListAccounts != null && !tempListAccounts.isEmpty()){
	        broadcsatMessage(context, broadcastMessage, BroadcastKeyEnum.start_status.toString(), CommandValueEnum.error.toString());
        } else {
	        broadcsatMessage(context, broadcastMessage, BroadcastKeyEnum.start_status.toString(), CommandValueEnum.success.toString());
        }
	}

	private void broadcsatMessage(Context context, String message, String key, String value){
		NotificationBroadcastData notificationBroadcastData = new NotificationBroadcastData();
		notificationBroadcastData.setMessage(message);
		notificationBroadcastData.setKey(key);
		notificationBroadcastData.setValue(value);
		Gson gson = new Gson();
		String jsonNotificationBroadcastData = gson.toJson(notificationBroadcastData);
		
		// Broadcast corresponding message
		Controller.broadcastMessage(context, 
			BroadcastActionEnum.BROADCAST_MESSAGE.toString(), 
			"GcmIntentService",
			jsonNotificationBroadcastData, 
			key, // BroadcastKeyEnum.message.toString(),  
			value
		);
	}

}

//Way to run the function:
//Runnable startTrackLocationService = new StartTrackLocationService(
//	context,
//	selectedContactDeviceDataList,
//	contactDetails,
//	retryTimes,
//	delay);
//new Thread(startTrackLocationService).start();

