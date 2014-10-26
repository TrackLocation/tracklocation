package com.dagrest.tracklocation.datatype;

import com.dagrest.tracklocation.Controller;

public class ContactDeviceData {
	private ContactData contactData;
	private DeviceData deviceData;
	private String phoneNumber;
	private String imei;
	private String registration_id;
	private String guid;
	private int locationSharing;
	private int tracking;
	
	public ContactDeviceData() {}
	
	public ContactDeviceData(String phoneNumber, String registration_id, String guidId) {
		this.phoneNumber = phoneNumber;
		this.registration_id = registration_id;
		if( guidId == null || guidId.isEmpty() ){
			this.guid = Controller.generateUUID();
		} else {
			this.guid = guidId;
		}
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public ContactData getContactData() {
		return contactData;
	}
	public void setContactData(ContactData contactData) {
		this.contactData = contactData;
	}
	public DeviceData getDeviceData() {
		return deviceData;
	}
	public void setDeviceData(DeviceData deviceData) {
		this.deviceData = deviceData;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public String getRegistration_id() {
		return registration_id;
	}
	public void setRegistration_id(String registration_id) {
		this.registration_id = registration_id;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public int getLocationSharing() {
		return locationSharing;
	}
	public void setLocationSharing(int locationSharing) {
		this.locationSharing = locationSharing;
	}
	public int getTracking() {
		return tracking;
	}
	public void setTracking(int tracking) {
		this.tracking = tracking;
	}
}
