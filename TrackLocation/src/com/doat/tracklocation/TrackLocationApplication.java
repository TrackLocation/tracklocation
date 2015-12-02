package com.doat.tracklocation;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;

public class TrackLocationApplication extends Application {	    
    
	private String className;    
    private String methodName;
    private Activity currentActivity;

	public static Gson gson = new Gson();
	
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
		registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());
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
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public void onActivityStarted(Activity activity) {
			// TODO Auto-generated method stub
			
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
		
		private int getPid(String packageName){
		    ActivityManager am = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		    List<ActivityManager.RunningAppProcessInfo> pids = am.getRunningAppProcesses();
		    int processid = 0;
		    for(int i = 0; i < pids.size(); i++) {
		        ActivityManager.RunningAppProcessInfo info = pids.get(i);	        
		        Log.i("PID Package",info.processName + " : " + pids.get(i) );

		        if(info.processName.equalsIgnoreCase(packageName)){
		            processid = info.pid;
		            return processid;
		        } 
		    }
		    return -1;
		}
		
		private void killApp(){
		    try {
		        int pid = getPid("com.doat.tracklocation");
		        if(pid != -1){
		        	Process.killProcess(pid);
		        } else {
		            Log.i("Not Found","App Not Found");
		        }
		    } catch (Exception e) {
		        e.printStackTrace();  // Device not rooted! 
		    }
		}
		
    }
}
