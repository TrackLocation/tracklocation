package com.dagrest.tracklocation.utils;

import java.util.LinkedHashMap;

import com.dagrest.tracklocation.datatype.MapMarkerDetails;
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

}
