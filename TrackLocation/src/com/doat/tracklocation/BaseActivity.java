package com.doat.tracklocation;

import com.doat.tracklocation.broadcast.BroadcastReceiverBase;
import com.doat.tracklocation.controller.MainActivityController;
import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.google.gson.Gson;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class BaseActivity extends Activity {
	protected final static Gson gson = new Gson();
	protected String className;    
    protected String methodName;
    protected Context context;
    protected String logMessage;
    protected MainActivityController mainActivityController;
	
    public static volatile boolean isTrackLocationRunning; // Used in SMSReceiver.class
	
	public TrackLocationApplication getMainApp() {
		return (TrackLocationApplication) getApplication();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActionBar actionBar = getActionBar();
	    actionBar.setHomeButtonEnabled(false);	    
	    actionBar.setDisplayShowHomeEnabled(false);
	    actionBar.setDisplayShowTitleEnabled(true);
		super.onCreate(savedInstanceState);
		className = this.getClass().getName();	
		context = getApplicationContext();
		
		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);
	}

	// Initialize BROADCAST_MESSAGE broadcast receiver
	protected void initNotificationBroadcastReceiver(BroadcastReceiverBase broadcastReceiver) {
		methodName = "initNotificationBroadcastReceiver";
		LogManager.LogFunctionCall(className, methodName);
		
		IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(BroadcastActionEnum.BROADCAST_MESSAGE.toString());
	    
	    registerReceiver(broadcastReceiver, intentFilter);
	    
		LogManager.LogFunctionExit(className, methodName);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_DESTROY] {" + className + "} -> " + methodName);
	}
}
