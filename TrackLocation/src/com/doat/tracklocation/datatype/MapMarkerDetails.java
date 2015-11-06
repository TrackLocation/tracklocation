package com.doat.tracklocation.datatype;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;

public class MapMarkerDetails {
	private Marker marker;
	private Circle locationCircle;
	private MessageDataContactDetails contactDetails;
	private MessageDataLocation locationDetails;
	
	public MapMarkerDetails(MessageDataContactDetails contactDetails,
			MessageDataLocation locationDetails, Marker marker,
			Circle locationCircle) {
		this.marker = marker;
		this.locationCircle = locationCircle;
		this.contactDetails = contactDetails;
		this.locationDetails = locationDetails;
	}
	public Marker getMarker() {
		return marker;
	}
	public void setMarker(Marker marker) {
		this.marker = marker;
	}
	public Circle getLocationCircle() {
		return locationCircle;
	}
	public void setLocationCircle(Circle locationCircle) {
		this.locationCircle = locationCircle;
	}
	public MessageDataContactDetails getContactDetails() {
		return contactDetails;
	}
	public void setContactDetails(MessageDataContactDetails contactDetails) {
		this.contactDetails = contactDetails;
	}
	public MessageDataLocation getLocationDetails() {
		return locationDetails;
	}
	public void setLocationDetails(MessageDataLocation locationDetails) {
		this.locationDetails = locationDetails;
	}	
}
