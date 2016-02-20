package com.doat.tracklocation.broadcast;

import java.util.List;

import com.doat.tracklocation.MapActivity;
import com.doat.tracklocation.TrackLocationApplication;
import com.doat.tracklocation.datatype.BroadcastConstEnum;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.CommandKeyEnum;
import com.doat.tracklocation.datatype.CommandValueEnum;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.datatype.NotificationBroadcastData;
import com.doat.tracklocation.dialog.InfoDialog;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class BroadcastReceiverMapActivity extends BroadcastReceiverBase {

	public BroadcastReceiverMapActivity(Activity activity, Handler handler) {
		super(activity, handler);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		methodName = "onReceive";
		
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		logMessage = "[" + methodName + "] started from [" + activity + "] activity";
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		Bundle bundle = intent.getExtras();
		if(bundle != null && bundle.containsKey(BroadcastConstEnum.data.toString())){
			String jsonNotificationData = bundle.getString(BroadcastConstEnum.data.toString());
			if(jsonNotificationData == null || jsonNotificationData.isEmpty()){
				logMessage = "The received Broadcast Data is null or empty.";
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
				return;
			}
			NotificationBroadcastData broadcastData = TrackLocationApplication.gson.fromJson(jsonNotificationData, NotificationBroadcastData.class);
			if(broadcastData == null){
				logMessage = "Unable to decode the received Broadcast Data.";
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
				return;
			}
			
			String key = broadcastData.getKey();
			String value = broadcastData.getValue();
			
			
			// Notification about command: Start TrackLocation Service
			// RECEIVED - some recipient received request
			if(BroadcastKeyEnum.start_status.toString().equals(key)){
				String jsonListAccounts = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_SEND_START_COMMAND_TO_ACCOUNTS);
				List<String> listAccounts = gson.fromJson(jsonListAccounts, List.class);
				String accountsListMsg = "";
				switch (CommandValueEnum.getValue(value)) {
				case start_track_location_service_received:
				case wait:
					// Notification about command: Start TrackLocation Service
    				// PLEASE WAIT - some recipients are not responding
    				// displayNotification(bundle);		    				
					// TODO: - Should be removed when new UI will be ready		
					accountsListMsg = "";
					if(listAccounts != null && !listAccounts.isEmpty()){
    					for (String account : listAccounts) {
    						accountsListMsg = " - " + account + "\n" + accountsListMsg;
    					}
					}
					break;
				case error:
    				// Notification about command: Start TrackLocation Service 
    				// FAILED for some recipients
					String title = "Warning";
    				String dialogMessage = broadcastData.getMessage();
    				new InfoDialog(activity, context, title, dialogMessage, null);	
					break;						
				case success:
					// Notification about command: Start TrackLocation Service 
    				// SUCCESS for some recipients
					String senderAccount  = broadcastData.getMessage();
    				logMessage = "MapLocationShare Service has been strated on [" + senderAccount + "].";
    				LogManager.LogInfoMsg(className, methodName, logMessage);
    				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

    				// TODO: - Should be removed when new UI will be ready		
    				accountsListMsg = "";
					if(listAccounts != null && !listAccounts.isEmpty()){
    					for (String account : listAccounts) {
    						accountsListMsg = " - " + account + "\n" + accountsListMsg;
    					}
					}
					break;
				default:
					break;
				}
			}
				    			
			if( (CommandKeyEnum.permissions.toString().equals(key) && CommandValueEnum.not_defined.toString().equals(value)) ||
				(CommandKeyEnum.permissions.toString().equals(key) && CommandValueEnum.not_permitted.toString().equals(value)) ){
				((MapActivity) activity).showPermissionsInfoDialog(broadcastData);
			}

			if(CommandKeyEnum.online_status.toString().equals(key)) {
				if(CommandValueEnum.online.toString().equals(value)){
					
					MessageDataContactDetails contactDetails = broadcastData.getContactDetails();
					if(contactDetails != null){
						String senderAccount = contactDetails.getAccount();
						// update listView - notifyDataSetChanged
						((MapActivity) this.activity).updateContactStatusInListView(senderAccount);
					}
				}
			}
			
			if(CommandKeyEnum.update_contact_list.toString().equals(key)) {
				// Get details of contact that sent join request by SMS from broadcast
				MessageDataContactDetails contactSentJoinRequest = broadcastData.getContactDetails();
				((MapActivity) this.activity).updateContactsList(contactSentJoinRequest);
			}
			
			if(BroadcastKeyEnum.register_to_gcm.toString().equals(key) && 
					CommonConst.FAILED.equals(value)){
				new InfoDialog(activity, context,
					"GCM Registration Error", 
					"\nGoogle Cloud Service is not available right now.\n\n"
						+ "Application will be closed.\n\nPlease try later.\n",
					new OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
						}
					}
				);
			}
		}

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

}
