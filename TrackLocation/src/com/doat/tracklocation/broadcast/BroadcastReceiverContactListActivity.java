package com.doat.tracklocation.broadcast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.R;
import com.doat.tracklocation.TrackLocationApplication;
import com.doat.tracklocation.datatype.BroadcastConstEnum;
import com.doat.tracklocation.datatype.CommandKeyEnum;
import com.doat.tracklocation.datatype.CommandValueEnum;
import com.doat.tracklocation.datatype.ContactDeviceData;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.datatype.NotificationBroadcastData;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;

public class BroadcastReceiverContactListActivity extends BroadcastReceiverBase {
	private int contactsListId;
	public BroadcastReceiverContactListActivity(Activity activity, int contactsListId) {
		super(activity);
		this.contactsListId = contactsListId;
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
			
			
			if(CommandKeyEnum.online_status.toString().equals(key)) {
				String value = broadcastData.getValue();
				if(CommandValueEnum.online.toString().equals(value)){
					//R.id.contact_list_view
					
					ListView lv = (ListView) activity.findViewById(this.contactsListId);
					ArrayAdapter<ContactDeviceData> adapter = (ArrayAdapter<ContactDeviceData>) lv.getAdapter();
					MessageDataContactDetails contactDetails = broadcastData.getContactDetails();
					if(contactDetails != null){
						String senderAccount = contactDetails.getAccount();
						if(adapter != null && senderAccount != null){
							int count = adapter.getCount();
							for (int i = 0; i < count; i++) {
								ContactDeviceData item = adapter.getItem(i);
								if(senderAccount.equals(item.getContactData().getEmail())){
									item.getContactData().setContactStatus(CommonConst.CONTACT_STATUS_CONNECTED);
									adapter.notifyDataSetChanged();
								}
							}
						}
					}
				}
			}
			
			if(CommandKeyEnum.update_contact_list.toString().equals(key)) {
				
				ListView lv = (ListView) activity.findViewById (R.id.contact_list_view);
				ArrayAdapter<ContactDeviceData> adapter = (ArrayAdapter<ContactDeviceData>) lv.getAdapter();

				// Get all joined contacts from DB				
				ContactDeviceDataList contactDeviceDataList = DBLayer.getInstance().getContactDeviceDataList(null);
				Controller.fillContactDeviceData(activity, contactDeviceDataList);

				// Get details of contact that sent join request by SMS from broadcast
				MessageDataContactDetails contactSentJoinRequest = broadcastData.getContactDetails();
				
				for (ContactDeviceData jonedContact : contactDeviceDataList) {
					if(jonedContact.getContactData().getEmail().equals(contactSentJoinRequest.getAccount())){
						int position = adapter.getPosition(jonedContact);
						// if joined contact still not shown in Contact List - add it and show it
						if(position < 0){
							adapter.add(jonedContact);
							adapter.notifyDataSetChanged();
						}
						break;
					}
				}
			}
		}

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
}
