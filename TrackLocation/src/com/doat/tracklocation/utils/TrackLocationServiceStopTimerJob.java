package com.doat.tracklocation.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimerTask;

import android.util.Log;

import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.service.TrackLocationService;

public class TrackLocationServiceStopTimerJob extends TimerTask {

	private TrackLocationService trackLocationService = null;
	private String className = this.getClass().getName();
	private String methodName;
	private String logMessage;
	
	public void setTrackLocationServiceObject(TrackLocationService trackLocationService){
		methodName = "setTrackLocationServiceObject";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		this.trackLocationService = trackLocationService;
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	@Override
	public void run() {
		methodName = "run";
		
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		logMessage = "Timer with TimerJob is waked up: " + DateUtils.getCurrentTimestampAsString() +
			" -> ThreadID: " + Thread.currentThread().getId();
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		
		if(trackLocationService != null) {
			logMessage = "Track Location Service is running";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			
			// Get start time (keep active request time)
			long trackLocationServiceStartTime = trackLocationService.getTrackLocationServiceStartTime();
			// Get current time
			long currentTime = System.currentTimeMillis();
			
			Calendar trackLocationServiceStartTimeCal = GregorianCalendar.getInstance();
			trackLocationServiceStartTimeCal.setTimeInMillis(trackLocationServiceStartTime);
			String trackLocationServiceStartTimeStr = DateUtils.calendarToTimestampString(trackLocationServiceStartTimeCal);
			
			Calendar currentTimeCal = GregorianCalendar.getInstance();
			currentTimeCal.setTimeInMillis(currentTime);
			String currentTimeStr = DateUtils.calendarToTimestampString(currentTimeCal);

			logMessage = "TrackLocationService" + 
			"\nstarted at " + trackLocationServiceStartTimeStr + 
			"\ncurrent time is " + currentTimeStr;

			if(currentTime - trackLocationServiceStartTime > CommonConst.REPEAT_PERIOD_DEFAULT){				
				trackLocationService.stopTrackLocationService();
				logMessage = "TrackLocationService STOPPED by Timer with TimerJob " + 
				"\nstarted at " + trackLocationServiceStartTimeStr + 
				"\ncurrent time is " + currentTimeStr;
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			}
		} else {
			logMessage = "Track Location Service is not running";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		}
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
}
