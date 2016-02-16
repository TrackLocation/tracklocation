package com.doat.tracklocation.broadcast;

import com.doat.tracklocation.BaseActivity;
import com.doat.tracklocation.TrackLocationApplication;
import com.doat.tracklocation.datatype.BroadcastConstEnum;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.NotificationBroadcastData;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.SMSUtils;
import com.google.gson.Gson;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class BroadcastReceiverBase extends BroadcastReceiver {

	protected static Gson gson = new Gson();
	protected String className;    
    protected String methodName;
    protected String logMessage;
    protected Activity activity;
    protected Handler handler;
	
	public BroadcastReceiverBase(Activity activity, Handler handler) {
		this.className = this.getClass().getName();
		this.activity = activity;
		this.handler = handler;
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
			
			// Notification about command: bring to top - to foreground
			// bring MainActivity to foreground
			if(BroadcastKeyEnum.join_sms.toString().equals(key)) {
				Activity currentActivity = ((BaseActivity)activity).getMainApp().getCurrentActivity();
//				SMSUtils.showApproveJoinRequestDialog(context, currentActivity, broadcastData); // bring to foreground
				SMSUtils.checkJoinRequestBySMSInBackground(context, currentActivity, handler);
			}
		}

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
    	
}
