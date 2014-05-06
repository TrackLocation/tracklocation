package com.dagrest.tracklocation.datatype;

public class ContactDeviceData {
	private ContactData contactData;
	private DeviceData deviceData;
	
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
}
