package com.dagrest.tracklocation.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimerTask;

import android.util.Log;

import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.service.TrackLocationService;
import com.dagrest.tracklocation.service.TrackingService;

public class TimerJob extends TimerTask {

	private TrackLocationService trackLocationService = null;
	private TrackingService trackingService = null;
	private String className = this.getClass().getName();
	private String methodName;
	private String logMessage;
	
	public void setTrackLocationServiceObject(TrackLocationService trackLocationService){
		this.trackLocationService = trackLocationService;
	}
	
	public void setTrackingServiceObject(TrackingService trackingService){
		this.trackingService = trackingService;
	}

	@Override
	public void run() {
		methodName = "run";
		
		logMessage = "Timer with TimerJob is waked: " + DateUtils.getCurrentTimestampAsString();
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
				logMessage = "Timer with TimerJob stoped TrackLocationService" + 
				"\nstarted at " + trackLocationServiceStartTimeStr + 
				"\ncurrent time is " + currentTimeStr;
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			}
		}
		if(trackingService != null) {
			// Check if trackingService should be stopped
			
//			// If trackingService should be stopped - stop it
//			if(...){
//				trackingService.stopTrackingService();
//	        	Log.i(CommonConst.LOG_TAG, "Timer with TimerJob stoped TrackingService");
//			}
		}
	}

}
