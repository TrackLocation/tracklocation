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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class BroadcastReceiverMapActivity extends BroadcastReceiverBase {

	public BroadcastReceiverMapActivity(Activity activity) {
		super(activity);
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
				String jsonListAccounts = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS);
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
//    					notificationView.setText("Tracking for contacts:\n" +
//    							accountsListMsg +
//    							"\nPlease wait...");
//	    				notificationView.setVisibility(0);
					} else {
//						notificationView.setVisibility(4);
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
    				logMessage = "TrackLocation Service has been strated on [" + senderAccount + "].";
    				LogManager.LogInfoMsg(className, methodName, logMessage);
    				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

    				// TODO: - Should be removed when new UI will be ready		
    				accountsListMsg = "";
					if(listAccounts != null && !listAccounts.isEmpty()){
    					for (String account : listAccounts) {
    						accountsListMsg = " - " + account + "\n" + accountsListMsg;
    					}
//    					notificationView.setText("Tracking for contacts:\n" +
//    							accountsListMsg +
//    							"\nPlease wait...");
//	    				notificationView.setVisibility(0);
					} else {
//						notificationView.setVisibility(4);
					}
					break;
				default:
					break;
				}
			}
				    			
			if( (CommandKeyEnum.permissions.toString().equals(key) && CommandValueEnum.not_defined.toString().equals(value)) ||
				(CommandKeyEnum.permissions.toString().equals(key) && CommandValueEnum.not_permitted.toString().equals(value)) ){
				((MapActivity) activity).showPermissionsInfoDialog(broadcastData.getMessage());
			}

			if(CommandKeyEnum.online_status.toString().equals(key)) {
				if(CommandValueEnum.online.toString().equals(value)){
					
					MessageDataContactDetails contactDetails = broadcastData.getContactDetails();
					if(contactDetails != null){
						String senderAccount = contactDetails.getAccount();
						// update listView - notifyDataSetChanged
						((MapActivity) this.activity).updateContactStatusInListView(senderAccount);
					}

//					ListView lv = (ListView) activity.findViewById(this.contactsListId);
//					ArrayAdapter<ContactDeviceData> adapter = (ArrayAdapter<ContactDeviceData>) lv.getAdapter();
//					MessageDataContactDetails contactDetails = broadcastData.getContactDetails();
//					if(contactDetails != null){
//						String senderAccount = contactDetails.getAccount();
//						if(adapter != null && senderAccount != null){
//							int count = adapter.getCount();
//							for (int i = 0; i < count; i++) {
//								ContactDeviceData item = adapter.getItem(i);
//								if(senderAccount.equals(item.getContactData().getEmail())){
//									item.getContactData().setContactStatus(CommonConst.CONTACT_STATUS_CONNECTED);
//									adapter.notifyDataSetChanged();
//								}
//							}
//						}
//					}
				}
			}
			
//			if(CommandKeyEnum.update_contact_list.toString().equals(key)) {
//				
//				ListView lv = (ListView) activity.findViewById (R.id.contact_list_view);
//				ArrayAdapter<ContactDeviceData> adapter = (ArrayAdapter<ContactDeviceData>) lv.getAdapter();
//
//				// Get all joined contacts from DB				
//				ContactDeviceDataList contactDeviceDataList = DBLayer.getInstance().getContactDeviceDataList(null);
//				Controller.fillContactDeviceData(activity, contactDeviceDataList, null, null, null);
//
//				// Get details of contact that sent join request by SMS from broadcast
//				MessageDataContactDetails contactSentJoinRequest = broadcastData.getContactDetails();
//				
//				for (ContactDeviceData jonedContact : contactDeviceDataList) {
//					if(jonedContact.getContactData().getEmail().equals(contactSentJoinRequest.getAccount())){
//						int position = adapter.getPosition(jonedContact);
//						// if joined contact still not shown in Contact List - add it and show it
//						if(position < 0){
//							adapter.add(jonedContact);
//							adapter.notifyDataSetChanged();
//						}
//						break;
//					}
//				}
//			}
		}

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

//	private void displayNotification(Bundle bundle){
//		String jsonNotificationData = bundle.getString(BroadcastConstEnum.data.toString());
//		if(jsonNotificationData == null || jsonNotificationData.isEmpty()){
//			return;
//		}
//		NotificationBroadcastData broadcastData = gson.fromJson(jsonNotificationData, NotificationBroadcastData.class);
//		if(broadcastData == null){
//			return;
//		}
//		
//		Toast.makeText(activity, broadcastData.getMessage(), Toast.LENGTH_LONG).show();
//	}

}
