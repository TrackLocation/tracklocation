package com.dagrest.tracklocation.service;

import java.util.Timer;

import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.TimerJob;
import com.dagrest.tracklocation.utils.TrackingTimerJob;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class TrackingAutostarter extends Service{

	private String className;
	private String methodName;
	private String logMessage;
	private Timer timer;
	private TrackingTimerJob trackingTimerJob;
	private long repeatPeriod;
	private Context context;
	
	@Override
	public IBinder onBind(Intent intent) {
		className = this.getClass().getName();
		methodName = "onBind";
		logMessage = "";
		context = getApplicationContext();
		
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);

		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		className = this.getClass().getName();
		methodName = "onCreate";
		logMessage = "";
		context = getApplicationContext();
		
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);

		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		prepareTrackLocationServiceStopTimer();
        // Start TrackLocationServiceStopTimer
    	Log.i(CommonConst.LOG_TAG, "{" + className + "} Start TrackLocationService TimerJob with repeat period = " + 
    		repeatPeriod/1000/60 + " min");
        try {
        	if(timer != null){
        		timer.schedule(trackingTimerJob, 0, repeatPeriod);
        	}
		} catch (IllegalStateException e) {
//			String ecxeptionMessage = "TimerTask is scheduled already";
//			logMessage = "[EXCEPTION] {" + className + "} Failed to Start TrackLocationService TimerJob";
//			if(!ecxeptionMessage.equals(e.getMessage())){
//				LogManager.LogException(e, className, methodName);
//				Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
//			} else {
//				LogManager.LogInfoMsg(className, methodName, ecxeptionMessage);
//				Log.e(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + ecxeptionMessage);
//			}
		} catch (IllegalArgumentException e) {
//			logMessage = "[EXCEPTION] {" + className + "} Failed to Start TrackLocationService TimerJob";
//			LogManager.LogException(e, className, methodName);
//			Log.e(CommonConst.LOG_TAG, logMessage, e);
		}
	}

	@Override
	public void onDestroy() {
		logMessage = "";
		methodName = "onDestroy";
		
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);

		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		super.onDestroy();
	}

    public void prepareTrackLocationServiceStopTimer(){
        timer = new Timer();
        trackingTimerJob = new TrackingTimerJob(context);
        //trackingTimerJob.setTrackLocationServiceObject(this);
        repeatPeriod = CommonConst.REPEAT_PERIOD_DEFAULT_TRACKING_AUTOSTARTER; // 1 minutes
        //trackLocationServiceStartTime = System.currentTimeMillis();
    }

}
