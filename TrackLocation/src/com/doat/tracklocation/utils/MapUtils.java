package com.doat.tracklocation.utils;

import java.util.LinkedHashMap;

import com.doat.tracklocation.datatype.MapMarkerDetails;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

public class MapUtils {
	public static CameraUpdate createCameraUpdateLatLngBounds(LinkedHashMap<String, MapMarkerDetails> markerMap) {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (LinkedHashMap.Entry<String, MapMarkerDetails> markerEntry : markerMap.entrySet()) {
			Marker m = markerEntry.getValue().getMarker();
			if(m != null){
    			builder.include(m.getPosition());
			}
		}
		LatLngBounds bounds = builder.build();
		int padding = 150; // offset from edges of the map in pixels
		return  CameraUpdateFactory.newLatLngBounds(bounds, padding);
	}
	
	public static float getRotationAngle(LatLng secondLastLatLng, LatLng lastLatLng)
	{
	    double x1 = secondLastLatLng.latitude;
	    double y1 = secondLastLatLng.longitude;
	    double x2 = lastLatLng.latitude;
	    double y2 = lastLatLng.longitude;

	    float xDiff = (float) (x2 - x1);
	    float yDiff = (float) (y2 - y1);

	    return (float) (Math.atan2(yDiff, xDiff) * 180.0 / Math.PI);
	}
	
	public static double getDistanceBetweenPoints(LatLng secondLastLatLng, LatLng lastLatLng){
		double R = 6378137; // Earth’s mean radius in meter
		double dLat = rad(lastLatLng.latitude - secondLastLatLng.latitude);
		double dLong = rad(lastLatLng.longitude - secondLastLatLng.longitude);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(rad(secondLastLatLng.latitude)) * Math.cos(rad(lastLatLng.latitude)) *
				Math.sin(dLong / 2) * Math.sin(dLong / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return R * c; // returns the distance in meter		 
	}
	
	private static double rad(double x) {
		return x * Math.PI / 180;
	}

}
