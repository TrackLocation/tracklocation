package com.doat.tracklocation.service;

import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        String methodName = "onReceive";
        String className = this.getClass().getName();
        String logMessage = "";
        
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);

		logMessage = "Intent action: " + intent.getAction();
    	LogManager.LogInfoMsg(className, methodName, logMessage);
    	Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

    	if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
        	logMessage = "StartUpBootReceiver BOOT_COMPLETED";
        	LogManager.LogInfoMsg(className, methodName, logMessage);
        	Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
        }

    	// Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        // Start the service, keeping the device awake while it is launching.
        ComponentName componentName = startWakefulService(context, (intent.setComponent(comp)));
		logMessage = "StartWakefulService: " + componentName.toString();
		LogManager.LogInfoMsg(className, "onHandleIntent", logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
        setResultCode(Activity.RESULT_OK);
        
        LogManager.LogFunctionExit(className, methodName);
        Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

}
