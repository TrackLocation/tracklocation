package com.doat.tracklocation.service;

import com.doat.tracklocation.datatype.MessageDataLocation;

public interface ILocationListener {
	public void onLocationChanged(MessageDataLocation locationDetails);
	public void onStatusChanged(MessageDataLocation locationDetails);
	public void onProviderEnabled(MessageDataLocation locationDetails);
	public void onProviderDisabled(MessageDataLocation locationDetails);
}
