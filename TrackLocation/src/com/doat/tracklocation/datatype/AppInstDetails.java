package com.doat.tracklocation.datatype;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;
import com.google.gson.Gson;

import android.content.Context;

public class AppInstDetails {

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
		if(appInstDetails == null || 
			(appInstDetails != null && appInstDetails.getAppInfo() != null && 
			 (appInstDetails.getAppInfo().getVersionName() == null || 
			  appInstDetails.getAppInfo().getVersionName().isEmpty()))){
			// Create 
			this.appInfo = Controller.getAppInfo(context);
			this.timestamp = System.currentTimeMillis();
			String jsonAppInstDetailsNew = gson.toJson(this);
			if(jsonAppInstDetailsNew != null && !jsonAppInstDetailsNew.isEmpty()){
				Preferences.setPreferencesString(context, CommonConst.APP_INST_DETAILS, jsonAppInstDetailsNew);
			}
		} else {
			this.appInfo = appInstDetails.getAppInfo();
			this.timestamp = appInstDetails.getTimestamp();
		}
	}
	
	private AppInstDetails getAppInstDetails(Context context){
		Gson gson = new Gson();
		AppInstDetails appInstDetails = null;
		String jsonAppInstDetails = Preferences.getPreferencesString(context, CommonConst.APP_INST_DETAILS);
		if(jsonAppInstDetails != null && !jsonAppInstDetails.isEmpty()){
			appInstDetails = gson.fromJson(jsonAppInstDetails, AppInstDetails.class);
		}
		return appInstDetails;
	}
}
