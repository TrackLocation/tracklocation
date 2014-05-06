package com.dagrest.tracklocation;

import com.dagrest.tracklocation.log.LogManager;

public class LocationUtils {
	/*
    private void requestLocation(boolean forceGps) {
        try{
        LogManager.LogFunctionCall("LocationNotifierService", "requestLocation()");
                locationManager.removeUpdates(locationListenerGPS);
                locationManager = null;
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationProviders = locationManager.getProviders(true);
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
                        preferences.setBoooleanSettingsValue("isLocationProviderAvailable", true);
                        //preferences.setStringSettingsValue("locationProviderName", "GPS");
                        } else if (containsNetwork) {
                        LogManager.LogInfoMsg("LocationNotifierService", "requestLocation()", "NETWORK_PROVIDER selected.");
                                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0, locationListenerNetwork);
                        preferences.setBoooleanSettingsValue("isLocationProviderAvailable", true);
                        //preferences.setStringSettingsValue("locationProviderName", "NETWORK");
                        }
                } else {
                LogManager.LogInfoMsg("LocationNotifierService", "requestLocation()", "No location providers available.");
                preferences.setBoooleanSettingsValue("isLocationProviderAvailable", false);
                }
        LogManager.LogFunctionExit("LocationNotifierService", "requestLocation()");
} catch (Exception e) {
        LogManager.LogException(e, "LocationNotifierService", "requestLocation()");
}
}
*/
}
