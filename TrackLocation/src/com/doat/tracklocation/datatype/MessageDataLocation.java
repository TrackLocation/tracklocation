package com.doat.tracklocation.datatype;

public class MessageDataLocation {
	private double lat;
	private double lng;
	private double accuracy;
	private double speed;
	private String locationProviderType;
	private float bearing = 0;

	public void setBearing(float bearing) {
		this.bearing = bearing;
	}
	
	public float getBearing(){
		return bearing;
	}

	public MessageDataLocation(){};
	
	public MessageDataLocation(double lat, double lng, double accuracy,
			double speed, String locationProviderType) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.accuracy = accuracy;
		this.speed = speed;
		this.locationProviderType = locationProviderType;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public String getLocationProviderType() {
		return locationProviderType;
	}
	public void setLocationProviderType(String locationProviderType) {
		this.locationProviderType = locationProviderType;
	}
}
