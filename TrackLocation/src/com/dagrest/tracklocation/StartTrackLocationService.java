package com.dagrest.tracklocation;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.dagrest.tracklocation.datatype.CommandData;
import com.dagrest.tracklocation.datatype.CommandDataBasic;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.MessageDataContactDetails;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.google.gson.Gson;

public class StartTrackLocationService  implements Runnable {

	private Context context;
	private ContactDeviceDataList selectedContactDeviceDataList;
	private MessageDataContactDetails senderMessageDataContactDetails;
	private int retryTimes;
	private int delay;
	private String className;

	public StartTrackLocationService(
        	Context context,
        	ContactDeviceDataList selectedContactDeviceDataList,
        	MessageDataContactDetails senderMessageDataContactDetails,
        	int retryTimes,
        	int delay) {
		this.context = context;
		this.selectedContactDeviceDataList = selectedContactDeviceDataList;
		this.senderMessageDataContactDetails = senderMessageDataContactDetails;
		this.retryTimes = retryTimes;
		this.delay = delay; // in milliseconds
		className = this.getClass().getName();
	}
	
	@Override
	public void run() {
		CommandDataBasic commandDataBasic = new CommandData(
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
			
        if(senderMessageDataContactDetails == null){
        	return;
        }
        
        if(selectedContactDeviceDataList == null){
        	return;
        }
        
        // String currentAccount = senderMessageDataContactDetails.getAccount();
        
        Gson gson = new Gson();
        String jsonListAccounts = gson.toJson(commandDataBasic.getListAccounts());
        // Set list of recipients' accounts list 
        Preferences.setPreferencesString(context, 
        		CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS, jsonListAccounts);
        Log.i(CommonConst.LOG_TAG, "Saved destinations: " + jsonListAccounts);
        List<String> listAccounts = null;
		for (int i = 0; i < retryTimes; i++) {
			jsonListAccounts = Preferences.getPreferencesString(context, 
	        		CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS);
	        Log.i(CommonConst.LOG_TAG, "Inside loop destinations: " + jsonListAccounts);

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
				" [START] command sent to recipients: [" + jsonListAccounts + "]");
			
			try {
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> Sleep: " + delay / 1000 + " sec");
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// Do nothing
				// TODO: To log - info about crash
			}
		}

		// Reset list of recipients' accounts list to be empty
        Preferences.setPreferencesString(context, 
        		CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS, "");
        Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> : Recipients list after loop: [" + jsonListAccounts + "]");
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

