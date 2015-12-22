package com.doat.tracklocation.broadcast;

import com.doat.tracklocation.TrackLocationApplication;
import com.doat.tracklocation.datatype.BroadcastConstEnum;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.datatype.NotificationBroadcastData;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.service.TrackLocationService;
import com.doat.tracklocation.utils.CommonConst;
import com.google.gson.Gson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class BroadcastReceiverTrackLocationService extends BroadcastReceiver {
	private static Gson gson = new Gson();
	private String className;    
	private String methodName;
	private String logMessage;
    private TrackLocationService trackLocationService;
    private MessageDataContactDetails senderMessageDataContactDetails;

	public BroadcastReceiverTrackLocationService(TrackLocationService trackLocationService) {
		this.className = this.getClass().getName();
		this.trackLocationService = trackLocationService;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		methodName = "onReceive";
		
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

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
			
			// Restart TrackLocation Service
			// provide senderMessageDataContactDetails
			if(BroadcastKeyEnum.restart_tls.toString().equals(key)) {
				String jsonSenderMessageDataContactDetails = broadcastData.getValue();
				senderMessageDataContactDetails = gson.fromJson(jsonSenderMessageDataContactDetails, MessageDataContactDetails.class);
				trackLocationService.updateService(senderMessageDataContactDetails);
			}
		}

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

}
