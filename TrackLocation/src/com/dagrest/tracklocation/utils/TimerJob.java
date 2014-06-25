package com.dagrest.tracklocation.utils;

import java.util.TimerTask;

import android.util.Log;

import com.dagrest.tracklocation.service.TrackLocationService;

public class TimerJob extends TimerTask {

	TrackLocationService trackLocationService;
	
	public void setTrackLocationServiceObject(TrackLocationService trackLocationService){
		this.trackLocationService = trackLocationService;
	}
	
	@Override
	public void run() {
		// Get start time (keep active request time)
		long trackLocationServiceStartTime = trackLocationService.getTrackLocationServiceStartTime();
		// Get current time
		long currentTime = System.currentTimeMillis();
		
		if(currentTime - trackLocationServiceStartTime > CommonConst.REPEAT_PERIOD_DEFAULT){
			trackLocationService.stopTrackLocationService();
        	Log.i(CommonConst.LOG_TAG, "Timer with TimerJob stoped TrackLocationService");
		}
	}

}
