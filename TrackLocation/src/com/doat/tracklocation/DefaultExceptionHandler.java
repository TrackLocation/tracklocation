package com.doat.tracklocation;

import android.app.Activity;
import android.util.Log;

import com.doat.tracklocation.dialog.ICommonDialogOnClickListener;
import com.doat.tracklocation.dialog.InfoDialog;
import com.doat.tracklocation.log.LogHelper;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;

public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {
	
	private String className;
	private Activity activity;
	
	public DefaultExceptionHandler(){
		className = "DefaultExceptionHandler";
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable exception) {
		LogManager.LogUncaughtException(exception, "DefaultExceptionHandler", "uncaughtException");
		Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + exception.getMessage());
		
    	String title = "Exception";
		String errorMessage = "Unhandled exception catched.\n"
				+ "Application will be closed.\n\nSee: sdcard\\TrackLocation\\TrackLocation.log\n";
    	activity = ((TrackLocationApplication) TrackLocationApplication.getContext()).getCurrentActivity();
    	new InfoDialog(activity, TrackLocationApplication.getContext(), title, errorMessage, new ICommonDialogOnClickListener(){

			@Override
			public void doOnPositiveButton(Object data) {
		    	activity.finish();
			}

			@Override
			public void doOnNegativeButton(Object data) {
			}

			@Override
			public void doOnChooseItem(int which) {
			}
    	});
	}
}
