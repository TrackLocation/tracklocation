package com.dagrest.tracklocation.utils;

import java.util.TimerTask;

import android.util.Log;

import com.dagrest.tracklocation.service.TrackLocationService;
import com.dagrest.tracklocation.service.TrackingService;

public class TimerJob extends TimerTask {

	TrackLocationService trackLocationService = null;
	TrackingService trackingService = null;
	
	public void setTrackLocationServiceObject(TrackLocationService trackLocationService){
		this.trackLocationService = trackLocationService;
	}
	
	public void setTrackingServiceObject(TrackingService trackingService){
		this.trackingService = trackingService;
	}

	@Override
	public void run() {
		if(trackLocationService != null) {
			// Get start time (keep active request time)
			long trackLocationServiceStartTime = trackLocationService.getTrackLocationServiceStartTime();
			// Get current time
			long currentTime = System.currentTimeMillis();
			
			if(currentTime - trackLocationServiceStartTime > CommonConst.REPEAT_PERIOD_DEFAULT){
				trackLocationService.stopTrackLocationService();
	        	Log.i(CommonConst.LOG_TAG, "Timer with TimerJob stoped TrackLocationService");
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
