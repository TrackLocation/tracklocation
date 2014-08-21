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
	private MessageDataContactDetails contactDetails;
	private int retryTimes;
	private int delay;
	private String className;

	public StartTrackLocationService(
        	Context context,
        	ContactDeviceDataList selectedContactDeviceDataList,
        	MessageDataContactDetails contactDetails,
        	int retryTimes,
        	int delay) {
		this.context = context;
		this.selectedContactDeviceDataList = selectedContactDeviceDataList;
		this.contactDetails = contactDetails;
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
				contactDetails, 
				null,				// location
				null, 				// key
				null, 				// value,
				null 				// appInfo
		);
			
        if(contactDetails == null){
        	return;
        }
        
        if(selectedContactDeviceDataList == null){
        	return;
        }
        
        String currentAccount = contactDetails.getAccount();
        
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
			
    		Log.i(CommonConst.LOG_TAG, className + ": Loop: " + (i+1) + " [START] command, threadId: " + Thread.currentThread().getId());
			commandDataBasic.sendCommand();
			Log.i(CommonConst.LOG_TAG, className + ": Loop: " + (i+1) + " [START] command sent to recipients: " + jsonListAccounts);
			
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// Do nothing
				// TODO: To log - info about crash
			}
		}

		// Reset list of recipients' accounts list to be empty
        Preferences.setPreferencesString(context, 
        		CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS, "");
        Log.i(CommonConst.LOG_TAG, className + ": Recipients list after loop: " + jsonListAccounts);
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

