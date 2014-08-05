package com.dagrest.tracklocation.utils;

import java.util.TimerTask;

import android.content.Context;
import android.util.Log;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.datatype.BroadcastCommandEnum;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.MessageDataContactDetails;
import com.dagrest.tracklocation.datatype.MessageDataLocation;
import com.dagrest.tracklocation.service.TrackLocationService;

public class MapKeepAliveTimerJob extends TimerTask {

	private Context context;
	private ContactDeviceDataList selectedContactDeviceDataList;
	
	public void setContext(Context context){
		this.context = context;
	}
	
	public void setSelectedContactDeviceDataList(
			ContactDeviceDataList selectedContactDeviceDataList) {
		this.selectedContactDeviceDataList = selectedContactDeviceDataList;
	}

	@Override
	public void run() {
		if(context != null && selectedContactDeviceDataList != null){
			
			MessageDataContactDetails contactDetails = null;
			MessageDataLocation location = null;
			
	   		Controller.sendCommand(context, selectedContactDeviceDataList, 
	   			CommandEnum.track_location_service_keep_alive, 
	   			null, contactDetails, location, 
	   			BroadcastCommandEnum.keep_alive.toString(), 
	   			Long.toString(System.currentTimeMillis()));
	    	Log.i(CommonConst.LOG_TAG, "KeepAlive command sent to trackLocationService from mapKeepAliveTimerJob");
		} else {
			// TODO: error to log 
			// Cannot start MapKeepAliveTimerJob - no context provided
			Log.e(CommonConst.LOG_TAG, "Cannot start MapKeepAliveTimerJob - no context or selectedContactDeviceDataList provided");
		}
	}
}
