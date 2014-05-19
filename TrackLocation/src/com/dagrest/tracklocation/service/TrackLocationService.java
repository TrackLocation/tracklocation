package com.dagrest.tracklocation.service;

import java.util.Date;
import java.util.List;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.NotificationCommandEnum;
import com.dagrest.tracklocation.datatype.PushNotificationServiceStatusEnum;
import com.dagrest.tracklocation.datatype.TrackLocationServiceStatusEnum;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

public class TrackLocationService extends Service {

	private static String className;
	private static Context context;
	private LocationManager locationManager;
	private List<String> locationProviders;
	private Boolean isLocationProviderAvailable;
	private LocationListener locationListenerGPS = null;
	private LocationListener locationListenerNetwork = null;
	
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
        
        isLocationProviderAvailable = false;
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
            
            LogManager.LogFunctionExit(className, "onCreate");
            Log.i(LOCATION_SERVICE, "onCreate - End");
            
        } catch (Exception e) {
        	LogManager.LogException(e, className, "onCreate");
        	Log.e(LOCATION_SERVICE, "onCreate", e);
        }
	}   
    
    @Override          
    public void onDestroy()           
    {                  
        super.onDestroy();
        try{
        	LogManager.LogFunctionCall(className, "onDestroy");
        	Log.i(LOCATION_SERVICE, "onDestroy - Start");
        	
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
            
            LogManager.LogFunctionExit(className, "onStart");
            Log.i(LOCATION_SERVICE, "onStart - End");
		} catch (Exception e) {
			LogManager.LogException(e, className, "onStart");
			LogManager.LogInfoMsg(className, "onStart", e.toString());
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
                    isLocationProviderAvailable = true; // Preferences.setPreferencesBooolean(context, CommonConst.IS_LOCATION_PROVIDER_AVAILABLE, true);
                    //preferences.setStringSettingsValue("locationProviderName", "GPS");
                } else if (containsNetwork) {
                	LogManager.LogInfoMsg(className, "requestLocation", "NETWORK_PROVIDER selected.");
                	
            		locationListenerNetwork = new LocationListenerBasic(context, this, "LocationListenerNetwork", CommonConst.NETWORK);

            		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Integer.parseInt(intervalString), 0, locationListenerNetwork);
            		isLocationProviderAvailable = true; // Preferences.setPreferencesBooolean(context, CommonConst.IS_LOCATION_PROVIDER_AVAILABLE, true);
                    //preferences.setStringSettingsValue("locationProviderName", "NETWORK");
                }
	        } else {
		        LogManager.LogInfoMsg(className, "requestLocation", "No location providers available.");
		        isLocationProviderAvailable = false; // Preferences.setPreferencesBooolean(context, CommonConst.IS_LOCATION_PROVIDER_AVAILABLE, false);
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
		List<String> listRegIDs = Preferences.getPreferencesReturnToRegIDList(context, 
				CommonConst.PREFERENCES_RETURN_TO_REG_ID_LIST); 

		String time = new Date().toString(); 
		Controller controller = new Controller();

		// Get current registration ID
		String senderRegId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
		String jsonMessage = controller.createJsonMessage(listRegIDs, 
    		senderRegId, 
    		CommandEnum.status_response, 
    		null, // TODO: send device UUID in the message 
    		time,
    		NotificationCommandEnum.trackLocationServiceStatus.toString(),
    		TrackLocationServiceStatusEnum.stopped.toString());
		// send message back with PushNotificationServiceStatusEnum.available
		controller.sendCommand(jsonMessage);
        // ==============================
        // send GCM to requester
        // ==============================
    }
    
    private void sendTrackLocationServiceStarted(){
        // ==========================================
        // send GCM (push notification) to requester (Master)
        // that TrackLocationService is started
        // ==========================================
		List<String> listRegIDs = Preferences.getPreferencesReturnToRegIDList(context, 
				CommonConst.PREFERENCES_RETURN_TO_REG_ID_LIST); 

		String time = new Date().toString(); 
		Controller controller = new Controller();

		// Get current registration ID
		String senderRegId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
		String jsonMessage = controller.createJsonMessage(listRegIDs, 
    		senderRegId, 
    		CommandEnum.status_response, 
    		null, // TODO: send device UUID in the message 
    		time,
    		NotificationCommandEnum.trackLocationServiceStatus.toString(),
    		TrackLocationServiceStatusEnum.started.toString());
		// send message back with PushNotificationServiceStatusEnum.available
		controller.sendCommand(jsonMessage);
        // ==============================
        // send GCM to requester
        // ==============================
    }

}

