package com.doat.tracklocation.service;

import java.util.List;
import java.util.Timer;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.datatype.BroadcastConstEnum;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.CommandData;
import com.doat.tracklocation.datatype.CommandDataBasic;
import com.doat.tracklocation.datatype.CommandEnum;
import com.doat.tracklocation.datatype.CommandKeyEnum;
import com.doat.tracklocation.datatype.CommandValueEnum;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.datatype.NotificationBroadcastData;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;
import com.doat.tracklocation.utils.TimerJob;
import com.google.gson.Gson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

// This service notifies location while tracking contact/s on map ("Locate" button)
public class TrackLocationService extends TrackLocationServiceBasic {

	protected TimerJob timerJob;
	protected Timer timer;
	protected long repeatPeriod;
	protected long trackLocationServiceStartTime;
	protected String trackLocationKeepAliveRequester;
	protected BroadcastReceiver gcmKeepAliveBroadcastReceiver;

	@Override
	public IBinder onBind(Intent intent) {
		super.onBind(intent);
		return null;
	}
	
	@Override          
    public void onCreate()          
    {             
        super.onCreate();
        methodName = "onCreate";
        className = this.getClass().getName();
        timer = null;
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
        
		
		initBroadcastReceiver(BroadcastActionEnum.BROADCAST_LOCATION_KEEP_ALIVE.toString(), "ContactConfiguration");
        
        prepareTrackLocationServiceStopTimer();
	}   
    
    @Override          
    public void onDestroy()           
    {                  
        super.onDestroy();
    	LogManager.LogFunctionCall(className, "onDestroy");
    	Log.i(CommonConst.LOG_TAG, "[INFO]  {" + className + "} -> onDestroy - Start");

    	// Stop TrackLocationServiceStopTimer
    	Log.i(CommonConst.LOG_TAG, "[INFO]  {" + className + "} -> Stop TrackLocationService TimerJob");
    	timerJob.cancel();
    	Log.i(CommonConst.LOG_TAG, "[INFO]  {" + className + "} -> Timer with TimerJob that stops TrackLocationService - stopped");
    }  

    @Override          
	public void onStart(Intent intent, int startId)           
	{             
    	super.onStart(intent, startId);
    	methodName = "onStart";
		try{
			LogManager.LogFunctionCall(className, "onStart");
            Log.i(CommonConst.LOG_TAG, "{" + className + "} onStart - Start");
            
            Gson gson = new Gson();
            if(intent == null){
            	logMessage = "TrackLocation service failed to start.";
        		LogManager.LogErrorMsg(className, methodName, logMessage);
        		Log.i(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
            	return;
            }
            Bundle extras = intent.getExtras();
            String jsonSenderMessageDataContactDetails = null;
            MessageDataContactDetails senderMessageDataContactDetails = null;
    		if(extras.containsKey(CommonConst.START_CMD_SENDER_MESSAGE_DATA_CONTACT_DETAILS)){
    			jsonSenderMessageDataContactDetails = extras.getString(CommonConst.START_CMD_SENDER_MESSAGE_DATA_CONTACT_DETAILS);
	            senderMessageDataContactDetails = 
	            	gson.fromJson(jsonSenderMessageDataContactDetails, MessageDataContactDetails.class);
	            if(senderMessageDataContactDetails != null){
	            	trackLocationKeepAliveRequester = senderMessageDataContactDetails.getAccount();
	            	logMessage = "TrackLocation service has been started by [" + trackLocationKeepAliveRequester + "]";
	        		LogManager.LogInfoMsg(className, methodName, logMessage);
	        		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
	            }
    		}
            
            // Start TrackLocationServiceStopTimer
        	Log.i(CommonConst.LOG_TAG, "{" + className + "} Start TrackLocationService TimerJob with repeat period = " + 
        		repeatPeriod/1000/60 + " min");
            try {
            	if(timer != null){
            		timer.schedule(timerJob, 0, repeatPeriod);
            	}
			} catch (IllegalStateException e) {
				String ecxeptionMessage = "TimerTask is scheduled already";
				logMessage = "[EXCEPTION] {" + className + "} Failed to Start TrackLocationService TimerJob";
				if(!ecxeptionMessage.equals(e.getMessage())){
					LogManager.LogException(e, className, methodName);
					Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
				} else {
					LogManager.LogInfoMsg(className, methodName, ecxeptionMessage);
					Log.e(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + ecxeptionMessage);
				}
			} catch (IllegalArgumentException e) {
				logMessage = "[EXCEPTION] {" + className + "} Failed to Start TrackLocationService TimerJob";
				LogManager.LogException(e, className, methodName);
				Log.e(CommonConst.LOG_TAG, logMessage, e);
			}
    		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> Timer with TimerJob that stops TrackLocationService - started");

            requestLocation(true);

            // Notify to caller by GCM (push notification) - TrackLocationServiceStarted
    		clientBatteryLevel = Controller.getBatteryLevel(context);
            MessageDataContactDetails messageDataContactDetails = new MessageDataContactDetails(clientAccount, 
                clientMacAddress, clientPhoneNumber, clientRegId, clientBatteryLevel);
            ContactDeviceDataList contactDeviceDataToSendNotificationTo = 
            	new ContactDeviceDataList (
            		senderMessageDataContactDetails.getAccount(), 
            		senderMessageDataContactDetails.getMacAddress(), 
            		senderMessageDataContactDetails.getPhoneNumber(), 
            		senderMessageDataContactDetails.getRegId(), 
            		null);
            
            String msgServiceStarted = "{" + className + "} TrackLocationService was started by [" + senderMessageDataContactDetails.getAccount() + "]";
            String notificationKey = CommandKeyEnum.start_status.toString();
            String notificationValue = CommandValueEnum.success.toString();		

            CommandDataBasic commandDataBasic = new CommandData(
				getApplicationContext(), 
				contactDeviceDataToSendNotificationTo, 
    			CommandEnum.notification, 
    			msgServiceStarted, 
    			messageDataContactDetails, 
    			null, 					// location
    			notificationKey, 		// key
    			notificationValue,  	// value
    			appInfo
    		);
            commandDataBasic.sendCommand();
            
            Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} TrackLocationService - send NOTIFICATION Command performed");

            LogManager.LogFunctionExit(className, "onStart");
            Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} onStart - End");
		} catch (Exception e) {
			LogManager.LogException(e, className, "onStart");
		}
	}

    protected boolean providerAvailable(List<String> providers) {
        if (providers.size() < 1) {
        	return false;
        }
        return true;
    }

    public void stopTrackLocationService(){
    	methodName = "stopTrackLocationService";
    	Log.i(CommonConst.LOG_TAG, "Stop TrackLocationService");
	    unregisterReceiver(gcmKeepAliveBroadcastReceiver);
    	stopSelf();
    	Preferences.clearPreferencesReturnToContactMap(context);
    	logMessage = "Track Location Service has been stopped.";
    	LogManager.LogInfoMsg(className, methodName, logMessage);
    	Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
    }

	public long getTrackLocationServiceStartTime() {
		return trackLocationServiceStartTime;
	}

	public void setTrackLocationServiceStartTime(long trackLocationServiceStartTime) {
		this.trackLocationServiceStartTime = trackLocationServiceStartTime;
	}
    
    protected void initBroadcastReceiver(final String action, final String actionDescription)
    {
    	methodName = "initBroadcastReceiver";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
		
		logMessage = "Broadcast action: " + action + ". Broadcast action description: " + actionDescription;
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(action);
	    gcmKeepAliveBroadcastReceiver = new BroadcastReceiver() 
	    {
	    	@Override
    		public void onReceive(Context context, Intent intent) {
	    		methodName = "BroadcastReceiver->onReceive";
	    		LogManager.LogFunctionCall(className, methodName);
	    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
	    		
	    		Gson gson = new Gson();
	    		Bundle bundle = intent.getExtras();
	    		// ===========================================
	    		// broadcast key = keep_alive
	    		// ===========================================
	    		if(bundle != null  && bundle.containsKey(BroadcastConstEnum.data.toString())){
	    			String jsonNotificationData = bundle.getString(BroadcastConstEnum.data.toString());
	    			if(jsonNotificationData == null || jsonNotificationData.isEmpty()){
	    				return;
	    			}
	    			NotificationBroadcastData broadcastData = 
	    				gson.fromJson(jsonNotificationData, NotificationBroadcastData.class);
	    			if(broadcastData == null){
	    				return;
	    			}
	    			
	    			String key = broadcastData.getKey();
	    			
	    			String currentTime = broadcastData.getValue();
	    			if(currentTime == null || currentTime.isEmpty()){
	    				logMessage = "Keep alive delay is empty.";
	    				LogManager.LogErrorMsg(className, methodName, logMessage);
	    				Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
	    				return;
	    			}
	    			MessageDataContactDetails messageDataContactDetails = broadcastData.getContactDetails();
	    			String accountRequestedKeepAlive = messageDataContactDetails.getAccount();

	    			logMessage = "Broadcast action key: " + key + " in: " + 
		    			(Long.parseLong(currentTime, 10) - System.currentTimeMillis()) + " sec. " +
		    			"Requested by [" + accountRequestedKeepAlive + "]";
		    		LogManager.LogInfoMsg(className, methodName, logMessage);
		    		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

	    			if(BroadcastKeyEnum.keep_alive.toString().equals(key)){
		    			logMessage = "Broadcast action key: " + key +
				    		"Requested by [" + accountRequestedKeepAlive + "]";
		    			LogManager.LogInfoMsg(className, methodName, logMessage);
		    			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		    			trackLocationServiceStartTime = Long.parseLong(currentTime, 10);   
		    			trackLocationKeepAliveRequester = accountRequestedKeepAlive;
		   			}
	    		}
	    		LogManager.LogFunctionExit(className, methodName);
	    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	    	}
	    };
	    registerReceiver(gcmKeepAliveBroadcastReceiver, intentFilter);
	    
		logMessage = "Broadcast action: " + action + ". Broadcast action description: " + actionDescription;
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
	    
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
    }

    public void prepareTrackLocationServiceStopTimer(){
        timer = new Timer();
        timerJob = new TimerJob();
        timerJob.setTrackLocationServiceObject(this);
        repeatPeriod = CommonConst.REPEAT_PERIOD_DEFAULT; // 2 minutes
        trackLocationServiceStartTime = System.currentTimeMillis();
    }

}

