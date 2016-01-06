package com.doat.tracklocation.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;

public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {
	
	private String className;
    private final Context context;
    private final Class<?> activityClass;
    
	public DefaultExceptionHandler(Context context, Class<?> activityClass){
		className = "DefaultExceptionHandler";
        this.context = context;
        this.activityClass = activityClass;
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable exception) {
		LogManager.LogUncaughtException(exception, "DefaultExceptionHandler", "uncaughtException");
		Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + exception.getMessage());
		
		// Reopen MapActivity with extra info
		// CommonConst.UNHANDLED_EXCEPTION_EXTRA = exception's stack trace
		Intent trackLocationIntent = new Intent(context, activityClass);
		trackLocationIntent.putExtra(CommonConst.UNHANDLED_EXCEPTION_EXTRA, stackTraceToString(exception));
		trackLocationIntent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK );
		context.startActivity(trackLocationIntent);
		android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);	
	}
	
	private String stackTraceToString(Throwable exception){
		StringWriter stackTrace = new StringWriter();
		exception.printStackTrace(new PrintWriter(stackTrace));
		return exception.getMessage() + ":\n"+ stackTrace.toString();
	}
}
