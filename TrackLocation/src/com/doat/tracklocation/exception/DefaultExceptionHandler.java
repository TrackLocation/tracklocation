package com.doat.tracklocation.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.doat.tracklocation.TrackLocationApplication;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;

public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {
	
	private String className;
	private Activity activity;
	private static final String SUPPORT_MAIL = "track.and.location@gmail.com";
	
	public DefaultExceptionHandler(){
		className = "DefaultExceptionHandler";
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable exception) {
		LogManager.LogUncaughtException(exception, "DefaultExceptionHandler", "uncaughtException");
		Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + exception.getMessage());
		
    	activity = ((TrackLocationApplication) TrackLocationApplication.getContext()).getCurrentActivity();

		String uriText =
		    "mailto:" + SUPPORT_MAIL +
		    "?subject=" + Uri.encode("TrackLocation unhandled exception") + 
		    "&body=" + Uri.encode(stackTraceToString(exception));
		Uri uri = Uri.parse(uriText);
		Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
		sendIntent.setData(uri);
		activity.startActivity(Intent.createChooser(sendIntent, "Please send exception details to support mail")); 
		
	}
	
	private String stackTraceToString(Throwable exception){
		StringWriter stackTrace = new StringWriter();
		exception.printStackTrace(new PrintWriter(stackTrace));
		return exception.getMessage() + ":\n"+ stackTrace.toString();
	}
}
