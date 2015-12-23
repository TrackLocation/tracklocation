package com.doat.tracklocation.concurrent;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.CommandData;
import com.doat.tracklocation.datatype.CommandEnum;
import com.doat.tracklocation.datatype.CommandTagEnum;
import com.doat.tracklocation.datatype.CommandValueEnum;
import com.doat.tracklocation.datatype.ContactData;
import com.doat.tracklocation.datatype.ContactDeviceData;
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

	private final static String LOCATION_UPDATE_INTERVAL = "1000"; // [millisconds]
	private static Gson gson = new Gson();
	private Context context;
	private ContactDeviceDataList selectedContactDeviceDataList;
//	private ContactDeviceDataList updatedSelectedContactDeviceDataList;
	private MessageDataContactDetails senderMessageDataContactDetails;
	private int retrySendCommandDelay; // in milliseconds
	private String className;
	private List<String> tempListAccounts;
	private String methodName;
	private String logMessage;

	public StartTrackLocationService(
        	Context context,
        	ContactDeviceDataList selectedContactDeviceDataList,
        	MessageDataContactDetails senderMessageDataContactDetails,
        	int delay) {
		this.context = context;
		this.selectedContactDeviceDataList = selectedContactDeviceDataList;
		this.senderMessageDataContactDetails = senderMessageDataContactDetails;
		this.retrySendCommandDelay = delay; // in milliseconds
		className = this.getClass().getName();
	}
	
	@Override
	public void run() {
		
		methodName = "run";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

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

        CommandData commandData = null;
		try {
			commandData = new CommandData(
					context, 
					selectedContactDeviceDataList, 
					CommandEnum.start,					// [START]	
					null, 								// message
					senderMessageDataContactDetails, 
					null,								// location
					CommandTagEnum.interval.toString(),	// key: location update interval
					LOCATION_UPDATE_INTERVAL, 			// value: location update interval = 1 second
					null 								// appInfo
			);
		} catch (UnableToSendCommandException e) {
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
			return;
		}
		
        String jsonListAccounts = gson.toJson(commandData.getListAccounts());
        // Save list of recipients' accounts list 
        Preferences.setPreferencesString(context, 
        		CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS, jsonListAccounts);

        List<String> listAccounts = null;
		while (true) {
			ContactDeviceDataList updatedSelectedContactDeviceDataList = new ContactDeviceDataList();
			jsonListAccounts = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS);
	        if((jsonListAccounts == null || jsonListAccounts.isEmpty())){
				// exit from loop - stop sending Start TrackLoccation Service command
				break;
			}
	        logMessage = "Send COMMAND: START TrackLocation Service in separate thread " +
					"to the following SAVED TO PREFERENCES RECIPIENTS: " + jsonListAccounts;
	        LogManager.LogInfoMsg(className, methodName, logMessage);
	        Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

	        listAccounts = gson.fromJson(jsonListAccounts, List.class);
			if(listAccounts == null || listAccounts.isEmpty()){
				// exit from loop - stop sending Start TrackLoccation Service command
				break;
			}
		
			for (int i = 0; i < selectedContactDeviceDataList.size(); i++) {
				ContactDeviceData contactDeviceData  = selectedContactDeviceDataList.get(i);
				if(contactDeviceData != null){
					ContactData contactData = contactDeviceData.getContactData();
					if(contactData != null){
						if(listAccounts.contains(contactData.getEmail())){
							updatedSelectedContactDeviceDataList.add(contactDeviceData);
						}
					}
				}
			}
			
			// update selectedContactDeviceDataList
			commandData.setContactDeviceDataList(updatedSelectedContactDeviceDataList);
			commandData.sendCommand();
	
			// DEBUG
			List<String> tempContacts = new ArrayList<String>();
			for (ContactDeviceData entry : updatedSelectedContactDeviceDataList) {
				tempContacts.add(entry.getContactData().getEmail());
			}
			logMessage = "START TLS: updatedSelectedContactDeviceDataList: " + gson.toJson(tempContacts);
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			logMessage = "START TLS: jsonListAccounts: " + jsonListAccounts;
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			// DEBUG
			
			try {
				Thread.sleep(retrySendCommandDelay);
			} catch (InterruptedException e) {
				logMessage = "Finish the thread of TrackLocation Service starting. ThreadID = " + 
					Thread.currentThread().getId();
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
				return;
			}
			
			jsonListAccounts = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS);
			if(jsonListAccounts != null && !jsonListAccounts.isEmpty()){
				tempListAccounts = gson.fromJson(jsonListAccounts, List.class);
				if(!tempListAccounts.isEmpty()){
			        String broadcastMessage = "Currently unavailable:\n\n";
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
        logMessage = "Reset recipients list after loop to be empty.";
        LogManager.LogInfoMsg(className, methodName, logMessage);
        Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

        String broadcastMessage = "";
        if(tempListAccounts == null && tempListAccounts.isEmpty()){
	        broadcsatMessage(context, broadcastMessage, BroadcastKeyEnum.start_status.toString(), CommandValueEnum.wait.toString());
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
			className,
			jsonNotificationBroadcastData, 
			key, // BroadcastKeyEnum.message.toString(),  
			value
		);
	}

}
