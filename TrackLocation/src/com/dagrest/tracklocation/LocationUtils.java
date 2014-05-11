//package com.dagrest.tracklocation;
/*
import java.util.List;

import android.content.Context;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.dagrest.tracklocation.log.LogManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
	
//extends FragmentActivity implements
//GooglePlayServicesClient.ConnectionCallbacks,
//GooglePlayServicesClient.OnConnectionFailedListener {
//...
//// Global variable to hold the current location
//Location mCurrentLocation;
//...
//mCurrentLocation = mLocationClient.getLastLocation();
//...
//}
public class LocationUtils extends FragmentActivity implements 
		GooglePlayServicesClient.ConnectionCallbacks, 
		GooglePlayServicesClient.OnConnectionFailedListener {
	
	private LocationManager locationManager;
	
	public LocationManager getLocationMager(){
        if(locationManager == null){
        	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        return locationManager;
	}
	
    private void requestLocation(boolean forceGps) {
	    try{
	    	LogManager.LogFunctionCall("LocationNotifierService", "requestLocation()");
            //locationManager.removeUpdates(locationListenerGPS);
            locationManager = null;
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            List<String> locationProviders = locationManager.getProviders(true);
            
            
            LocationProvider lpGps = locationManager.getProvider(LocationManager.GPS_PROVIDER);
            LocationProvider lpNetwork = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);
            
            LogManager.LogInfoMsg("LocationNotifierService", "requestLocation()", "Providers list: " + locationProviders.toString());

            if (providerAvailable(locationProviders)) {
                    boolean containsGPS = locationProviders.contains(LocationManager.GPS_PROVIDER);
                    LogManager.LogInfoMsg("LocationNotifierService", "requestLocation()", "containsGPS: " + containsGPS);

                    boolean containsNetwork = locationProviders.contains(LocationManager.NETWORK_PROVIDER);
                    LogManager.LogInfoMsg("LocationNotifierService", "requestLocation()", "containsNetwork: " + containsNetwork);

                    if (containsGPS && forceGps) {
                    LogManager.LogInfoMsg("LocationNotifierService", "requestLocation()", "GPS_PROVIDER selected.");
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListenerGPS);
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0, locationListenerNetwork);
                    ////preferences.setBoooleanSettingsValue("isLocationProviderAvailable", true);
                    //preferences.setStringSettingsValue("locationProviderName", "GPS");
                    } else if (containsNetwork) {
                    LogManager.LogInfoMsg("LocationNotifierService", "requestLocation()", "NETWORK_PROVIDER selected.");
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0, locationListenerNetwork);
                    ////preferences.setBoooleanSettingsValue("isLocationProviderAvailable", true);
                    //preferences.setStringSettingsValue("locationProviderName", "NETWORK");
                    }
            } else {
            LogManager.LogInfoMsg("LocationNotifierService", "requestLocation()", "No location providers available.");
            ////preferences.setBoooleanSettingsValue("isLocationProviderAvailable", false);
            }
            LogManager.LogFunctionExit("LocationNotifierService", "requestLocation()");
		} catch (Exception e) {
		        LogManager.LogException(e, "LocationNotifierService", "requestLocation()");
		}
    }

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

}
*/
