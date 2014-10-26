package com.dagrest.tracklocation.service;

import com.dagrest.tracklocation.datatype.CommandEnum;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;

public class LocationListenerTrackLocation extends LocationListenerBasic{
	
	public LocationListenerTrackLocation(Context context,
			TrackLocationServiceBasic service, CommandEnum command, String className,
			String locationProviderType, String objectName) {
		super(context, service, command, className, locationProviderType, objectName);
		this.className = className;
		this.service = service;
		objectName = "TrackLocation Object";
		init(context, locationProviderType);
	}

	@Override
	public void onLocationChanged(Location location) {
		super.onLocationChanged(location);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		super.onStatusChanged(provider, status, extras);
	}

	@Override
	public void onProviderEnabled(String provider) {
		super.onProviderEnabled(provider);
	}

	@Override
	public void onProviderDisabled(String provider) {
		super.onProviderDisabled(provider);
	}

}

