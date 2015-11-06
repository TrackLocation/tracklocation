package com.doat.tracklocation.datatype;

public class MessageDataContactDetails {
	private String account;
	private String macAddress;
	private String phoneNumber;
	private String regId;
	private float batteryPercentage;
	
	public MessageDataContactDetails() {};
			
	public MessageDataContactDetails(String account, String macAddress,
			String phoneNumber, String regId, float batteryPercentage) {
		super();
		this.account = account;
		this.macAddress = macAddress;
		this.phoneNumber = phoneNumber;
		this.regId = regId;
		this.batteryPercentage = batteryPercentage;
	}
	
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public float getBatteryPercentage() {
		return batteryPercentage;
	}
	public void setBatteryPercentage(float batteryPercentage) {
		this.batteryPercentage = batteryPercentage;
	}
	public String getRegId() {
		return regId;
	}
	public void setRegId(String regId) {
		this.regId = regId;
	}
}
