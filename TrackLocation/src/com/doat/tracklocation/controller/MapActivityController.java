package com.doat.tracklocation.controller;

import java.util.Timer;

import android.content.Context;
import android.util.Log;

import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.MapKeepAliveTimerJob;

public class MapActivityController {

	private final static long SEND_KEEP_ALIVE_INTERAVL = CommonConst.REPEAT_PERIOD_DEFAULT / 2 + 700;
	
	private String className;    
	private String methodName;
	private String logMessage;
    
	private Timer mapKeepAliveTimer;
	private MapKeepAliveTimerJob mapKeepAliveTimerJob;

	public MapActivityController() {
		className = this.getClass().getName();
	}

	public void keepAliveTrackLocationService(Context context, ContactDeviceDataList selectedContactDeviceDataList, long startDelay){
		methodName = "keepAliveTrackLocationService";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		mapKeepAliveTimer = new Timer();
		mapKeepAliveTimerJob = new MapKeepAliveTimerJob();
		mapKeepAliveTimerJob.setContext(context);
		mapKeepAliveTimerJob.setSelectedContactDeviceDataList(selectedContactDeviceDataList);

		logMessage = "Starting KeepAliveTrackLocationService TimerJob with repeat period = " + 
				SEND_KEEP_ALIVE_INTERAVL/1000 + " sec.";
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		try {
			mapKeepAliveTimer.schedule(mapKeepAliveTimerJob, startDelay, SEND_KEEP_ALIVE_INTERAVL);
		} catch (IllegalArgumentException e) {
			logMessage = e.getMessage();
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
		} catch (IllegalStateException e) {
			logMessage = e.getMessage();
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
		}
		
		logMessage = "Started KeepAliveTrackLocationService TimerJob with repeat period = " + 
				SEND_KEEP_ALIVE_INTERAVL/1000 + " sec.";
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	public void stopKeepAliveTrackLocationService(){
		methodName = "stopKeepAliveTrackLocationService";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		mapKeepAliveTimer.cancel();
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

}
