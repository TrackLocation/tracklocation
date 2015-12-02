package com.doat.tracklocation;

import com.doat.tracklocation.datatype.BroadcastConstEnum;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.NotificationBroadcastData;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.SMSUtils;
import com.google.android.gms.drive.internal.GetMetadataRequest;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class BroadcastReceiverBase extends BroadcastReceiver {

	protected String className;    
    protected String methodName;
    protected String logMessage;
    protected Activity activity;
	
	public BroadcastReceiverBase(Activity activity) {
		this.activity = activity;
		((BaseActivity)activity).getMainApp().getCurrentActivity();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
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
				return;
			}
			NotificationBroadcastData broadcastData = TrackLocationApplication.gson.fromJson(jsonNotificationData, NotificationBroadcastData.class);
			if(broadcastData == null){
				return;
			}
			
			String key = broadcastData.getKey();
			
			// Notification about command: bring to top - to foreground
			// bring MainActivity to foreground
			if(BroadcastKeyEnum.join_sms.toString().equals(key)) {
				Activity currentActivity = ((BaseActivity)activity).getMainApp().getCurrentActivity();
//				SMSUtils.showApproveJoinRequestDialog(context, currentActivity, broadcastData); // bring to foreground
				SMSUtils.checkJoinRequestBySMSInBackground(context, currentActivity, true);
			}
		}

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
    	
}
