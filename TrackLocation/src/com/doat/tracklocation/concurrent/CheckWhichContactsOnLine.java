package com.doat.tracklocation.concurrent;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.doat.tracklocation.datatype.CommandData;
import com.doat.tracklocation.datatype.CommandDataBasic;
import com.doat.tracklocation.datatype.CommandEnum;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.exception.UnableToSendCommandException;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;
import com.google.gson.Gson;

public class CheckWhichContactsOnLine implements Runnable {

	private String className;
	private String methodName;
	private String logMessage;
	private final int SLEEP = 5000; // in milliseconds
	private Context context;
	private ContactDeviceDataList selectedContactDeviceDataList;
	private MessageDataContactDetails senderMessageDataContactDetails;
	
    private static Gson gson = new Gson();
	
	public CheckWhichContactsOnLine(Context context, 
			ContactDeviceDataList selectedContactDeviceDataList,
			MessageDataContactDetails senderMessageDataContactDetails) {
		super();
		className = this.getClass().getName();
		this.context = context;
		this.selectedContactDeviceDataList = selectedContactDeviceDataList;
		this.senderMessageDataContactDetails = senderMessageDataContactDetails;
	}

	@Override
	public void run() {
		methodName = "run -> ThreadID: " + Thread.currentThread().getId();
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

        if(senderMessageDataContactDetails == null){
        	logMessage = "Failed to start thread to check which contacts are online" +
        		" - no sender contact details provided.";
        	LogManager.LogWarnMsg(className, methodName, logMessage);
        	Log.e(CommonConst.LOG_TAG, "[WARN] {" + className + "} -> " + logMessage);
        	return;
        }
        
        if(selectedContactDeviceDataList == null){
        	logMessage = "Failed to start thread to check which contacts are online" +
            	" - no contacts details provided.";
        	LogManager.LogWarnMsg(className, methodName, logMessage);
        	Log.e(CommonConst.LOG_TAG, "[WARN] {" + className + "} -> " + logMessage);
        	return;
        }

		// Prepare command 
        CommandDataBasic commandDataBasic = null;
		try {
			commandDataBasic = new CommandData(
					context, 
					selectedContactDeviceDataList, 
					CommandEnum.is_online,	// [IS_ONLINE]	
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
		
        String jsonListAccounts = gson.toJson(commandDataBasic.getListAccounts());
        // Set list of recipients' accounts list 
        Preferences.setPreferencesString(context, 
        		CommonConst.PREFERENCES_SEND_IS_ONLINE_TO_ACCOUNTS, jsonListAccounts);
        logMessage = "Saved recipients: " + jsonListAccounts;
        LogManager.LogInfoMsg(className, methodName, logMessage);
        Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

        List<String> listAccounts = null;
        while(true){
			
			if(jsonListAccounts == null || jsonListAccounts.isEmpty()){
				LogManager.LogFunctionExit(className, methodName);
				Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
				return;
			}
			listAccounts = gson.fromJson(jsonListAccounts, List.class);
			if(listAccounts == null || listAccounts.isEmpty()){
				LogManager.LogFunctionExit(className, methodName);
				Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
				return;
			}

			commandDataBasic.sendCommand();

			// Send command to each contact to check if online
			logMessage = "Send command to each contact to check if online. ThreadID = " + 
				Thread.currentThread().getId();
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			
			// Sleep for delay milliseconds and continue...
			// Stop and exit the thread if interrupted
			try {
				logMessage = "Sleep " + (SLEEP / 1000) + " seconds";
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
				Thread.sleep(SLEEP);
			} catch (InterruptedException e) {
				logMessage = "Stop thread that checking which contacts are online. ThreadID = " + 
					Thread.currentThread().getId();
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
				
				// Stop and exit the thread if interrupted
				LogManager.LogFunctionExit(className, methodName);
				Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
				return;
			}
		}
	}
}
