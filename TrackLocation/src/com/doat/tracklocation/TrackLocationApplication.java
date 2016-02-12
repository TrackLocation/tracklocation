package com.doat.tracklocation;

import java.util.Timer;
import java.util.TimerTask;

import com.doat.tracklocation.datatype.AppInfo;
import com.doat.tracklocation.db.DBHelper;
import com.doat.tracklocation.db.DBManager;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Utils;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class TrackLocationApplication extends Application {	    
    
	private String className;    
	private String methodName;
	private String logMessage;
	private Activity currentActivity;
	private static Context context;	

	public static Gson gson = new Gson();
	
    public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public static Context getContext() {
		return context;
	}

	public Activity getCurrentActivity() {
		return currentActivity;
	}

	public void setCurrentActivity(Activity currentActivity) {
		this.currentActivity = currentActivity;
	}

	@Override
    public void onCreate() {
    	// TODO Auto-generated method stub
    	super.onCreate();
       	
		className = this.getClass().getName();
		context = getApplicationContext();
		
		Controller.saveAppInfoToPreferences(context);

		// Setup handler for uncaught exceptions.
		Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException (Thread thread, Throwable e)
			{
			  handleUncaughtException (thread, e);
			}
		});
	    
		DBManager.initDBManagerInstance(new DBHelper(context));		
		registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());
    }
	
	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}
	
    
    private final class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {
    	private Timer mActivityTransitionTimer;
        private TimerTask mActivityTransitionTimerTask;
        
        private Timer mAppDownTimer;
        private TimerTask mAppDownTimerTask;
        
        private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;
        private final long MAX_APP_BACKGROUND_TIME_MS = 60000;
        
        public boolean wasInBackground;
    	
    	public void startActivityTransitionTimer() {
    	    this.mActivityTransitionTimer = new Timer();
    	    this.mActivityTransitionTimerTask = new TimerTask() {
    	        public void run() {
    	        	wasInBackground = true;
    	        	startAppDownTimer();
    	        }
    	    };

    	    this.mActivityTransitionTimer.schedule(mActivityTransitionTimerTask, MAX_ACTIVITY_TRANSITION_TIME_MS);
    	}

    	public void stopActivityTransitionTimer() {
    	    if (this.mActivityTransitionTimerTask != null) {
    	        this.mActivityTransitionTimerTask.cancel();
    	    }

    	    if (this.mActivityTransitionTimer != null) {
    	        this.mActivityTransitionTimer.cancel();
    	    }
    	}
    	
    	public void startAppDownTimer() {
    	    this.mAppDownTimer = new Timer();
    	    this.mAppDownTimerTask = new TimerTask() {
    	        public void run() {    	        	
    	        	NotificationManager notifManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	        	notifManager.cancelAll();
    	        	//killApp();  
    	        	System.runFinalization();
    	        	System.exit(0);
    	        }
    	    };

    	    this.mAppDownTimer.schedule(mAppDownTimerTask, MAX_APP_BACKGROUND_TIME_MS);
    	}

    	public void stopAppDownTimer() {
    	    if (this.mAppDownTimerTask != null) {
    	        this.mAppDownTimerTask.cancel();
    	    }

    	    if (this.mAppDownTimer != null) {
    	        this.mAppDownTimer.cancel();
    	    }
    	}

		@Override
		public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
			currentActivity = activity;
		}
	
		@Override
		public void onActivityStarted(Activity activity) {
			currentActivity = activity;
		}
	
		@Override
		public void onActivityResumed(Activity activity) {	
			currentActivity = activity;
			if (wasInBackground){
				stopAppDownTimer();
			}
	        stopActivityTransitionTimer();			
		}
	
		@Override
		public void onActivityPaused(Activity activity) {			
			startActivityTransitionTimer();			
		}
	
		@Override
		public void onActivityStopped(Activity activity) {
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public void onActivityDestroyed(Activity activity) {
			// TODO Auto-generated method stub
			
		}
		
    }

	public void handleUncaughtException (Thread thread, Throwable e)
	{
		methodName = "handleUncaughtException";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		AppInfo appInfo = Controller.getAppInfo(context);
		String versionName = "";
		int versionNumber = 0;
		if(appInfo != null){
			versionName = appInfo.getVersionName();
			versionNumber = appInfo.getVersionNumber();
		}

		String uriText =
		"mailto:" + CommonConst.SUPPORT_MAIL +
			"?subject=" + Uri.encode(CommonConst.NEW_APP_NAME + " - unhandled exception report. ") +
			"&body=" + "VesionNumber: " + versionNumber + 
			" Version name: " + versionName + "\n" +
			Uri.encode(Utils.stackTraceToString(e));
		Uri uri = Uri.parse(uriText);
		        
		logMessage = uriText;
		LogManager.LogException(e, className, methodName);
		Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);

		Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
		sendIntent.setData(uri);
        Intent emailChooser = Intent.createChooser(sendIntent, CommonConst.NEW_APP_NAME + " - unhandled exception occurred! Please send error report to support team.");
        emailChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try{
			startActivity(emailChooser);
		} catch (Throwable t){
			logMessage = "Unable to start SendTo activity to send an exception report.";
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
			System.exit(1); 
		}
		logMessage = "Kill " + CommonConst.NEW_APP_NAME + " - unhandled exception has occured.";
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		LogManager.LogInfoMsg(className, methodName, logMessage);

		//android.os.Process.killProcess(android.os.Process.myPid());

		System.exit(1); // kill the crashed application
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
}
