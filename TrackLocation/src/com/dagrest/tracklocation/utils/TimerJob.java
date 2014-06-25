package com.dagrest.tracklocation.utils;

import java.util.TimerTask;

import com.dagrest.tracklocation.service.TrackLocationService;

public class TimerJob extends TimerTask {

	TrackLocationService trackLocationService;
	
	public void setTrackLocationServiceObject(TrackLocationService trackLocationService){
		this.trackLocationService = trackLocationService;
	}
	
	@Override
	public void run() {
		// Get start time
		long trackLocationServiceStartTime = trackLocationService.getTrackLocationServiceStartTime();
		// Get current time
		long currentTime = System.currentTimeMillis();
		
		if(false){
			trackLocationService.stopTrackLocationService();
		}
	}

}
