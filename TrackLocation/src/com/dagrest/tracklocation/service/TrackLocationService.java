package com.dagrest.tracklocation.service;

import java.util.Date;
import java.util.List;
import java.util.Timer;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.datatype.AppInfo;
import com.dagrest.tracklocation.datatype.BroadcastActionEnum;
import com.dagrest.tracklocation.datatype.CommandData;
import com.dagrest.tracklocation.datatype.CommandDataBasic;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.CommandKeyEnum;
import com.dagrest.tracklocation.datatype.CommandValueEnum;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.JsonMessageData;
import com.dagrest.tracklocation.datatype.MessageDataContactDetails;
import com.dagrest.tracklocation.datatype.MessageDataLocation;
import com.dagrest.tracklocation.datatype.NotificationKeyEnum;
import com.dagrest.tracklocation.datatype.PushNotificationServiceStatusEnum;
import com.dagrest.tracklocation.datatype.TrackLocationServiceStatusEnum;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.TimerJob;
import com.dagrest.tracklocation.utils.Preferences;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class TrackLocationService extends Service {

	private static String className;
	private static Context context;
	private LocationManager locationManager;
	private List<String> locationProviders;
	private LocationListener locationListenerGPS = null;
	private LocationListener locationListenerNetwork = null;
	private TimerJob timerJob;
	private Timer timer;
	private long repeatPeriod;
	private long trackLocationServiceStartTime;
	private BroadcastReceiver gcmKeepAliveBroadcastReceiver;
	
	private String clientAccount;
	private String clientMacAddress;
	private String clientPhoneNumber;
	private String clientRegId;
	private int clientBatteryLevel;
	private String errorMsg;
	private AppInfo appInfo;

	@Override
	public IBinder onBind(Intent intent) {
        LogManager.LogFunctionCall(className, "onBind");
        LogManager.LogFunctionExit(className, "onBind");
		return null;
	}
	
	@Override          
    public void onCreate()          
    {             
        super.onCreate();
        className = this.getClass().getName();
        
		initBroadcastReceiver(BroadcastActionEnum.BROADCAST_LOCATION_KEEP_ALIVE.toString(), "ContactConfiguration");
        
        LogManager.LogFunctionCall(className, "onCreate");
    	Log.i(LOCATION_SERVICE, "onCreate - Start");
       
        try{
            LogManager.LogFunctionCall(className, "onCreate");
            if(context == null){
            	context = getApplicationContext();
            }
            if(locationManager == null){
            	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            }
           
            appInfo = Controller.getAppInfo(context);

            prepareTrackLocationServiceStopTimer();
            
            LogManager.LogFunctionExit(className, "onCreate");
            Log.i(LOCATION_SERVICE, "onCreate - End");
            
        } catch (Exception e) {
        	LogManager.LogException(e, className, "onCreate");
        	Log.e(LOCATION_SERVICE, "onCreate", e);
        }
        
    	// Collect client details
		context = getApplicationContext();
		clientAccount = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		clientMacAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
		clientPhoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
		clientRegId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
	}   
    
    @Override          
    public void onDestroy()           
    {                  
        super.onDestroy();
        try{
        	LogManager.LogFunctionCall(className, "onDestroy");
        	Log.i(LOCATION_SERVICE, "onDestroy - Start");
        	
        	// Stop TrackLocationServiceStopTimer
        	Log.i(CommonConst.LOG_TAG, "Stop TrackLocationService TimerJob");
        	timerJob.cancel();
        	Log.i(CommonConst.LOG_TAG, "Timer with TimerJob that stops TrackLocationService - stopped");

        	
            if(locationManager != null){
            	if( locationListenerGPS != null){
	                locationManager.removeUpdates(locationListenerGPS);
	                LogManager.LogInfoMsg(className, "onDestroy", "locationListenerGPS - Updates removed");
            	}
            	if( locationListenerNetwork != null){
	                locationManager.removeUpdates(locationListenerNetwork);
	                LogManager.LogInfoMsg(className, "onDestroy", "locationListenerNetwork - Updates removed");
            	}
            }
            
    		if(gcmKeepAliveBroadcastReceiver != null) {
    			unregisterReceiver(gcmKeepAliveBroadcastReceiver);
    		}
    		
			sendTrackLocationServiceStopped();
			
            LogManager.LogFunctionExit(className, "onDestroy");
            Log.i(LOCATION_SERVICE, "onDestroy - End");
            
        } catch (Exception e) {
            LogManager.LogException(e, className, "onDestroy");
            Log.e(LOCATION_SERVICE, "onDestroy", e);
        }
    }  

    @Override          
	public void onStart(Intent intent, int startId)           
	{                  
		try{
			LogManager.LogFunctionCall(className, "onStart");
            Log.i(LOCATION_SERVICE, "onStart - Start");
            
//            Bundle extras = intent.getExtras();
//            String regIDToReturnMessageTo = extras.getString(CommonConst.REGISTRATION_ID_TO_RETURN_MESSAGE_TO);

            // Start TrackLocationServiceStopTimer
        	Log.i(CommonConst.LOG_TAG, "Start TrackLocationService TimerJob with repeat period = " + 
        		repeatPeriod/1000/60 + " min");
            try {
				timer.schedule(timerJob, 0, repeatPeriod);
			} catch (IllegalStateException e) {
				LogManager.LogException(e, className, "onStart->timer.scheduler");
			} catch (IllegalArgumentException e) {
				LogManager.LogException(e, className, "onStart->timer.scheduler");
			}
        	Log.i(CommonConst.LOG_TAG, "Timer with TimerJob that stops TrackLocationService - started");

//              String locProvName = null; 
//              locProvName = Preferences.getPreferencesString(context, CommonConst.LOCATION_PROVIDER_NAME);
//              LogManager.LogInfoMsg(className, "onStart", "Location provider name: " + locProvName);

            requestLocation(true);
            // isLocationProviderAvailable = Preferences.getPreferencesBoolean(context, CommonConst.IS_LOCATION_PROVIDER_AVAILABLE);
            
            // TODO: The following bock is OPTIONAL :
//            if(isLocationProviderAvailable){
//            	String locationStringGPS = Preferences.getPreferencesString(context, CommonConst.LOCATION_INFO_GPS);
//            	String locationStringNETWORK = Preferences.getPreferencesString(context, CommonConst.LOCATION_INFO_NETWORK);
//      
//                if(!locationStringGPS.equals("initial")){
//                	LogManager.LogInfoMsg("LocationNotifierService", "onStart()", "locationGPS: " + locationStringGPS);
//                    //sendLocationByMail(locationStringGPS, locationProvider);
//                    // TODO: send notification
//                } else if(!locationStringNETWORK.equals("initial")){
//                    LogManager.LogInfoMsg("LocationNotifierService", "onStart()", "locationNETWORK: " + locationStringNETWORK);
//                    //sendLocationByMail(locationStringNETWORK, locationProvider);
//                    // TODO: send notification
//                }
//            }
              
            sendTrackLocationServiceStarted();
            // Notify to caller by GCM (push notification)
    		clientBatteryLevel = Controller.getBatteryLevel(context);
            MessageDataContactDetails contactDetails = new MessageDataContactDetails(clientAccount, 
                	clientMacAddress, clientPhoneNumber, clientRegId, clientBatteryLevel);
            ContactDeviceDataList contactDeviceDataToSendNotificationTo = 
            	new ContactDeviceDataList (clientAccount, clientMacAddress, clientPhoneNumber, clientRegId, null);
            // Notify caller by GCM (push notification)
            
            String msgServiceStarted = "TrackLocationService was started by [" + clientAccount + "]";
            String notificationKey = CommandKeyEnum.status.toString();
            String notificationValue = CommandValueEnum.success.toString();		

            CommandDataBasic commandDataBasic = new CommandData(
				getApplicationContext(), 
				contactDeviceDataToSendNotificationTo, 
    			CommandEnum.notification, 
    			msgServiceStarted, 
    			contactDetails, 
    			null, 					// location
    			notificationKey, 		// key
    			notificationValue,  	// value
    			appInfo
    		);
            commandDataBasic.sendCommand();

            LogManager.LogFunctionExit(className, "onStart");
            Log.i(LOCATION_SERVICE, "onStart - End");
		} catch (Exception e) {
			LogManager.LogException(e, className, "onStart");
		}
	}
	
	public void requestLocation(boolean forceGps) {
        try{
        	LogManager.LogFunctionCall(className, "requestLocation");
        	if(locationListenerGPS != null){
        		locationManager.removeUpdates(locationListenerGPS);
        	}
        	if(locationListenerNetwork != null){
        		locationManager.removeUpdates(locationListenerNetwork);
        	}
			locationManager = null;
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationProviders = locationManager.getProviders(true);
			LogManager.LogInfoMsg(className, "requestLocation", "Providers list: " + locationProviders.toString());

	        if (providerAvailable(locationProviders)) {
	        	boolean containsGPS = locationProviders.contains(LocationManager.GPS_PROVIDER);
                LogManager.LogInfoMsg(className, "requestLocation", "containsGPS: " + containsGPS);

                boolean containsNetwork = locationProviders.contains(LocationManager.NETWORK_PROVIDER);
                LogManager.LogInfoMsg(className, "requestLocation", "containsNetwork: " + containsNetwork);

                String intervalString = Preferences.getPreferencesString(context, CommonConst.LOCATION_SERVICE_INTERVAL);
                if(intervalString == null || intervalString.isEmpty()){
                	intervalString = CommonConst.LOCATION_DEFAULT_UPDATE_INTERVAL; // time in milliseconds
                }
                
                if (containsGPS && forceGps) {
                	LogManager.LogInfoMsg(className, "requestLocation", "GPS_PROVIDER selected.");
                
	            	locationListenerGPS = new LocationListenerBasic(context, this, "LocationListenerGPS", CommonConst.GPS);
	            	locationListenerNetwork = new LocationListenerBasic(context, this, "LocationListenerNetwork", CommonConst.NETWORK);

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Integer.parseInt(intervalString), 0, locationListenerGPS);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Integer.parseInt(intervalString), 0, locationListenerNetwork);
                } else if (containsNetwork) {
                	LogManager.LogInfoMsg(className, "requestLocation", "NETWORK_PROVIDER selected.");
                	
            		locationListenerNetwork = new LocationListenerBasic(context, this, "LocationListenerNetwork", CommonConst.NETWORK);

            		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Integer.parseInt(intervalString), 0, locationListenerNetwork);
                }
	        } else {
		        LogManager.LogInfoMsg(className, "requestLocation", "No location providers available.");
	        }
        LogManager.LogFunctionExit(className, "requestLocation");
        } catch (Exception e) {
        	LogManager.LogException(e, className, "requestLocation");
        }
    }
    
    private boolean providerAvailable(List<String> providers) {
        if (providers.size() < 1) {
        	return false;
        }
        return true;
    }

    private void sendTrackLocationServiceStopped(){
        // ==========================================
        // send GCM (push notification) to requester (Master)
        // that TrackLocationService is stopped
        // ==========================================
		//List<String> listRegIDs = Preferences.getPreferencesReturnToRegIDList(context); 
		List<String> listRegIDs = Controller.getPreferencesReturnToRegIDList(context);

		String time = new Date().toString(); 

		MessageDataLocation location = null;
		MessageDataContactDetails contactDetails = null;
		// Get current registration ID
//		String senderRegId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
		String jsonMessage = Controller.createJsonMessage(
			new JsonMessageData(
				listRegIDs, 
//	    		senderRegId, 
	    		CommandEnum.status_response, 
	    		null, // TODO: send device UUID in the message 
	    		contactDetails,
	    		location,
	    		null, // application info
	    		time,
	    		NotificationKeyEnum.trackLocationServiceStatus.toString(),
	    		TrackLocationServiceStatusEnum.stopped.toString()
	    	)
		);
		if(jsonMessage == null){
			errorMsg = "Failed to create JSON Message to send to recipient";
			LogManager.LogErrorMsg(className, "[Command:" + CommandEnum.status_response.toString() + "]", errorMsg);
			return;
		} else {
			// send message back with PushNotificationServiceStatusEnum.available
			Controller.sendCommand(jsonMessage);
		}
        // ==============================
        // send GCM to requester
        // ==============================
    }
    
    private void sendTrackLocationServiceStarted(){
        // ==========================================
        // send GCM (push notification) to requester (Master)
        // that TrackLocationService is started
        // ==========================================
		// List<String> listRegIDs = Preferences.getPreferencesReturnToRegIDList(context); 
		List<String> listRegIDs = Controller.getPreferencesReturnToRegIDList(context);

		String time = new Date().toString(); 

		MessageDataLocation location = null;
		MessageDataContactDetails contactDetails = null;
		// Get current registration ID
//		String senderRegId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
		String jsonMessage = Controller.createJsonMessage(
			new JsonMessageData(
				listRegIDs, 
//	    		senderRegId, 
	    		CommandEnum.status_response, 
	    		null, // TODO: send device UUID in the message 
	    		contactDetails,
	    		location,
	    		null,
	    		time,
	    		NotificationKeyEnum.trackLocationServiceStatus.toString(),
	    		TrackLocationServiceStatusEnum.started.toString()
	    	)
		);
		if(jsonMessage == null){
			errorMsg = "Failed to create JSON Message to send to recipient";
			LogManager.LogErrorMsg(className, "[Command:" + CommandEnum.status_response.toString() + "]", errorMsg);
			return;
		} else {
			// send message back with PushNotificationServiceStatusEnum.available
			Controller.sendCommand(jsonMessage);
		}
        // ==============================
        // send GCM to requester
        // ==============================
    }

    public void stopTrackLocationService(){
    	Log.i(CommonConst.LOG_TAG, "Stop TrackLocationService");
    	stopSelf();
    }

	public long getTrackLocationServiceStartTime() {
		return trackLocationServiceStartTime;
	}

	public void setTrackLocationServiceStartTime(long trackLocationServiceStartTime) {
		this.trackLocationServiceStartTime = trackLocationServiceStartTime;
	}
    
    private void initBroadcastReceiver(final String action, final String actionDescription)
    {
    	LogManager.LogFunctionCall(actionDescription, "initBroadcastReceiver");
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(action);
	    gcmKeepAliveBroadcastReceiver = new BroadcastReceiver() 
	    {
	    	@Override
    		public void onReceive(Context context, Intent intent) {
	    		LogManager.LogInfoMsg(actionDescription, "initBroadcastReceiver->onReceive", "WORK");

	    		// TODO: refactor with JSON to JAVA and back instead of string with delimiters
	    		
	    		Bundle bundle = intent.getExtras();
	    		String broadcastKepAlive = BroadcastActionEnum.KEEP_ALIVE.toString();
	    		// ===========================================
	    		// broadcast key = keep_alive
	    		// ===========================================
	    		if(bundle != null){
	    			String result = null;
	    			if(bundle.containsKey(broadcastKepAlive)){
	    				result = bundle.getString(broadcastKepAlive);
	    			}
		    		if(result != null && !result.isEmpty()){
		    			if(result.contains(BroadcastActionEnum.KEEP_ALIVE.toString())){
		    				//mStatus.setText(PushNotificationServiceStatusEnum.available.toString());
		    				String[] inputArray = result.split(CommonConst.DELIMITER_STRING); // key, value, current_time
		    				String currentTime = inputArray[1];
		    				trackLocationServiceStartTime = Long.parseLong(currentTime, 10);       
		    			}
		    		} else {
//		    			mNotification.setText(result);
		    		}
	    		}
	    	}
	    };
	    registerReceiver(gcmKeepAliveBroadcastReceiver, intentFilter);
	    LogManager.LogFunctionExit(actionDescription, "initBroadcastReceiver");
    }

    public void prepareTrackLocationServiceStopTimer(){
        timer = new Timer();
        timerJob = new TimerJob();
        timerJob.setTrackLocationServiceObject(this);
        repeatPeriod = CommonConst.REPEAT_PERIOD_DEFAULT; // 2 minutes
        trackLocationServiceStartTime = System.currentTimeMillis();
    }

}

