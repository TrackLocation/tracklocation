package com.dagrest.tracklocation.service;

import com.dagrest.tracklocation.log.LogManager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;

public class TrackLocationService extends Service {

	private boolean started = false;
	private int getLocationInterval = 0;
	
	private static String className;
	private static Context context;
	private LocationManager locationManager;
	private PowerManager.WakeLock wl;
	
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
        
        try{
            LogManager.LogFunctionCall(className, "onCreate");
            if(context == null){
            	context = getApplicationContext();
            }
//          if(sharedPreferences == null){
//          	sharedPreferences = context.getSharedPreferences(PREFS_NAME, 0);
//          }
//          if(preferences == null){
//              preferences = new Preferences(sharedPreferences);
//          }
            if(locationManager == null){
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            }
//          toReleaseWakeLock = false;
            LogManager.LogFunctionExit(className, "onCreate");
        } catch (Exception e) {
        	LogManager.LogException(e, className, "onCreate");
        }
   }   
    
    @Override          
    public void onDestroy()           
    {                  
        super.onDestroy();
        try{
        	LogManager.LogFunctionCall(className, "onDestroy");
        	//LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if(locationManager != null){
                locationManager.removeUpdates(locationListenerGPS);
                LogManager.LogInfoMsg(className, "onDestroy", "locationListenerGPS - Updates removed");
                locationManager.removeUpdates(locationListenerNetwork);
                LogManager.LogInfoMsg(className, "onDestroy", "locationListenerNetwork - Updates removed");
            }
            if(wl != null){
                wl.release();
                LogManager.LogInfoMsg(className, "onDestroy", "WAKE LOCK - HAS BEEN REMOVED.");
            }
            LogManager.LogFunctionExit(className, "onDestroy");
        } catch (Exception e) {
            LogManager.LogException(e, className, "onDestroy");
        }
    }  
    
    // Define a listener that responds to location updates
    LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
        	/*
	        try{
        	 
                LogManager.LogFunctionCall("LocationListener", "onLocationChanged()");

                preferences.setStringSettingsValue("locationProviderName", "GPS");

                double latitude = 0, longitude = 0;
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                if(latitude == 0 || longitude == 0){
                        return;
                }

                Float distanceToTarget = location.distanceTo(Utils.getTargetLocation(preferences))/1000;
                accuracy = location.getAccuracy();
                locationProvider = location.getProvider();
                speed = location.getSpeed();

                preferences.setStringSettingsValue("locationProviderName", locationProvider);
                
                //sets latitude/longitude when a location is provided
                latlong = location.getLatitude() + "," + location.getLongitude() + DELIMITER + 
                	Utils.getCurrentTime() + DELIMITER + distanceToTarget;
                        
                LogManager.LogInfoMsg("locationListenerGPS", "onLocationChanged()", "@@@NEW_LOCATION_GPS: " + latlong);
                preferences.setStringSettingsValue("locationStringGPS", latlong);
        
                //sendLocationByMail(latlong);

                String laDeviceId = preferences.getStringSettingsValue("laDeviceId", "004999010640000");
                String deviceUid = null;
                if(wl != null && wl.isHeld()){
                    LogManager.LogInfoMsg("locationListenerGPS", "onLocationChanged()", "WAKE LOCK - READY TO BE RELEASED.");
                    if(!laDeviceId.equals(preferences.getStringSettingsValue("deviceUid", deviceUid))){
                            toReleaseWakeLock = true;
                    }
                    LogManager.LogInfoMsg("locationListenerGPS", "onLocationChanged()", "WAKE LOCK isHeld: " + wl.isHeld());
//                  wl.release();
//                  LogManager.LogInfoMsg("locationListenerGPS", "onLocationChanged()", "WAKE LOCK - HAS BEEN RELEASED.");
                }
                LogManager.LogFunctionExit("locationListenerGPS", "onLocationChanged()");
	        } catch (Exception e) {
	                LogManager.LogException(e, "locationListenerGPS", "onLocationChanged()");
	        }      
	        */   
        }

	    public void onStatusChanged(String provider, int status, Bundle extras) {}
	    public void onProviderEnabled(String provider) {}
	    public void onProviderDisabled(String provider) {}
    };

    // Define a listener that responds to location updates
	LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
/*
			try{
                LogManager.LogFunctionCall("locationListenerNetwork", "onLocationChanged()");
                preferences.setStringSettingsValue("locationProviderName", "NETWORK");
                
                double latitude = 0, longitude = 0;
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                if(latitude == 0 || longitude == 0){
                        return;
                }

                Float distanceToTarget = location.distanceTo(Utils.getTargetLocation(preferences))/1000;
                accuracy = location.getAccuracy();
                locationProvider = location.getProvider();
                speed = location.getSpeed();
                
                preferences.setStringSettingsValue("locationProviderName", locationProvider);

                //sets latitude/longitude when a location is provided
                latlong = location.getLatitude() + "," + location.getLongitude() + DELIMITER + 
                	Utils.getCurrentTime() + DELIMITER + distanceToTarget;
                        
                LogManager.LogInfoMsg("locationListenerNetwork", "onLocationChanged()", "@@@NEW_LOCATION_NETWORK: " + latlong);
                preferences.setStringSettingsValue("locationStringNETWORK", latlong);
       
                if(wl != null){
                	LogManager.LogInfoMsg("locationListenerNetwork", "onLocationChanged()", "WAKE LOCK isHeld: " + wl.isHeld());
                }
                LogManager.LogFunctionExit("locationListenerNetwork", "onLocationChanged()");
            } catch (Exception e) {
                    LogManager.LogException(e, "locationListenerNetwork", "onLocationChanged()");
            }
*/
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
      };
}
