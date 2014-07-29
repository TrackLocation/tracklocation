package com.dagrest.tracklocation.datatype;

public class MessageDataLocation {
	private double lat;
	private double lng;
	private double accuracy;
	private double speed;

	public MessageDataLocation(){};
	
	public MessageDataLocation(double lat, double lng, double accuracy,
			double speed) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.accuracy = accuracy;
		this.speed = speed;
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
	
}
