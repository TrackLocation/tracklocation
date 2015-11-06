package com.doat.tracklocation.utils;

import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.doat.tracklocation.datatype.BroadcastConstEnum;
import com.doat.tracklocation.datatype.NotificationBroadcastData;
import com.doat.tracklocation.log.LogManager;
import com.google.gson.Gson;

public class BroadcastMessage {
	private Context context;
	private String broadcastAction; // BroadcastActionEnum.BROADCAST_TURN_OFF_RING.toString()
	private String className;
    private BroadcastReceiver notificationBroadcastReceiver = null;
    private Map<String, Object> params;

	public BroadcastMessage(Context context, String broadcastAction) {
		className = this.getClass().getName();
		this.context = context;
		this.broadcastAction = broadcastAction;
	}

	public void unregisterBroadcastReceiver(){
		if(notificationBroadcastReceiver != null){
			context.unregisterReceiver(notificationBroadcastReceiver);
		}
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	// Initialize BROADCAST_TURN_OFF_RING broadcast receiver
	public void registerBroadcastReceiver() {
		String methodName = "initNotificationBroadcastReceiver";
		LogManager.LogFunctionCall(className, methodName);
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(broadcastAction);
	    
	    if(notificationBroadcastReceiver == null){
		    notificationBroadcastReceiver = new BroadcastReceiver() {
	
		    	Gson gson = new Gson();
		    	
				@Override
				public void onReceive(Context context, Intent intent) {
					// String methodName = "onReceive";
					Bundle bundle = intent.getExtras();
		    		if(bundle != null && bundle.containsKey(BroadcastConstEnum.data.toString())){
		    			String jsonNotificationData = bundle.getString(BroadcastConstEnum.data.toString());
		    			if(jsonNotificationData == null || jsonNotificationData.isEmpty()){
		    				return;
		    			}
		    			NotificationBroadcastData broadcastData = gson.fromJson(jsonNotificationData, NotificationBroadcastData.class);
		    			if(broadcastData == null){
		    				return;
		    			}
		    			
		    			onReceiveAction(broadcastData);
		    		}
				}
		    };
		    
		    context.registerReceiver(notificationBroadcastReceiver, intentFilter);
		    
			LogManager.LogFunctionExit(className, methodName);
		}
	}
	
	protected void onReceiveAction(NotificationBroadcastData broadcastData) {
		// Implement action here
	}

}
