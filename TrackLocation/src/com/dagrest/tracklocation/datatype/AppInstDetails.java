package com.dagrest.tracklocation.datatype;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.utils.Preferences;
import com.google.gson.Gson;

import android.content.Context;

public class AppInstDetails {
	public static final String 	APP_INST_DEATILS 	= "AppInstDetails";

	private long timestamp;
	private AppInfo appInfo;
	
	public AppInstDetails(Context context) {
		saveAppInstDetails(context);
	}
	
	public long getTimestamp() {
		return timestamp;
	}
//	public void setTimestamp(long timestamp) {
//		this.timestamp = timestamp;
//	}
	public AppInfo getAppInfo() {
		return appInfo;
	}
//	public void setAppInfo(AppInfo appInfo) {
//		this.appInfo = appInfo;
//	}
	
	private void saveAppInstDetails(Context context){
		Gson gson = new Gson();
		AppInstDetails appInstDetails = getAppInstDetails(context);
		if(appInstDetails == null){
			// Create 
			this.appInfo = Controller.getAppInfo(context);
			this.timestamp = System.currentTimeMillis();
			String jsonAppInstDetailsNew = gson.toJson(this);
			if(jsonAppInstDetailsNew != null && !jsonAppInstDetailsNew.isEmpty()){
				Preferences.setPreferencesString(context, APP_INST_DEATILS, jsonAppInstDetailsNew);
			}
		} else {
			this.appInfo = appInstDetails.getAppInfo();
			this.timestamp = appInstDetails.getTimestamp();
		}
	}
	
	private AppInstDetails getAppInstDetails(Context context){
		Gson gson = new Gson();
		AppInstDetails appInstDetails = null;
		String jsonAppInstDetails = Preferences.getPreferencesString(context, APP_INST_DEATILS);
		if(jsonAppInstDetails != null && !jsonAppInstDetails.isEmpty()){
			appInstDetails = gson.fromJson(jsonAppInstDetails, AppInstDetails.class);
		}
		return appInstDetails;
	}
}
