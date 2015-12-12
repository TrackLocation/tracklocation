package com.doat.tracklocation.service;

import java.util.List;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.datatype.AppInfo;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

public class TrackLocationServiceBasic extends Service {

	protected static String className;
	protected static Context context;
	protected LocationManager locationManager;
	protected List<String> locationProviders;
	protected LocationListener locationListenerGPS = null;
	protected LocationListener locationListenerNetwork = null;
	protected String clientAccount;
	protected String clientMacAddress;
	protected String clientPhoneNumber;
	protected String clientRegId;
	protected int clientBatteryLevel;
	protected String logMessage;
	protected AppInfo appInfo;
	protected String methodName;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override          
    public void onCreate()          
    {             
        super.onCreate();
        methodName = "onCreate";
        className = this.getClass().getName();
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
                
		try{
            LogManager.LogFunctionCall(className, "onCreate");
            if(context == null){
            	context = getApplicationContext();
            }
            if(locationManager == null){
            	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            }
           
            appInfo = Controller.getAppInfo(context);

        	// Collect client details
    		context = getApplicationContext();
    		clientAccount = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
    		clientMacAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
    		clientPhoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
    		clientRegId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
    		
    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
    		
        } catch (Exception e) {
        	LogManager.LogException(e, className, methodName);
        	Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + methodName, e);
        }
	}   
    
    @Override          
    public void onDestroy()           
    {                  
        super.onDestroy();
        try{
        	LogManager.LogFunctionCall(className, "onDestroy");
        	Log.i(CommonConst.LOG_TAG, "[INFO]  {" + className + "} -> onDestroy - Start");
        	
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
            
            LogManager.LogFunctionExit(className, "onDestroy");
            Log.i(CommonConst.LOG_TAG, "onDestroy - End");
            
        } catch (Exception e) {
            LogManager.LogException(e, className, "onDestroy");
            Log.e(CommonConst.LOG_TAG, "onDestroy", e);
        }
    }  

    @Override          
	public void onStart(Intent intent, int startId)           
	{                  
    	// INMPLEMENTAT IN CERTAIN IMPLEMENTATION OF TRACK_LOCATION_SERVICE...		
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
        	if(locationManager == null){
        		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        	}
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
                
                String objectName = TrackLocationServiceBasic.className.toString();
                if (containsGPS && forceGps) {
                	LogManager.LogInfoMsg(className, "requestLocation", "GPS_PROVIDER selected.");
                
	            	locationListenerGPS = new LocationListenerBasic(context, this, "LocationListenerGPS", CommonConst.GPS, objectName);
	            	locationListenerNetwork = new LocationListenerBasic(context, this, "LocationListenerNetwork", CommonConst.NETWORK, objectName);

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Integer.parseInt(intervalString), 0, locationListenerGPS);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Integer.parseInt(intervalString), 0, locationListenerNetwork);
                } else if (containsNetwork) {
                	LogManager.LogInfoMsg(className, "requestLocation", "NETWORK_PROVIDER selected.");
                	
            		locationListenerNetwork = new LocationListenerBasic(context, this, "LocationListenerNetwork", CommonConst.NETWORK, objectName);

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
    
    protected boolean providerAvailable(List<String> providers) {
        if (providers.size() < 1) {
        	return false;
        }
        return true;
    }
}
