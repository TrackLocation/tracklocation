package com.dagrest.tracklocation.datatype;

public class MessageData {
	private String message;
	private String time;
	private CommandEnum command;
	private String registrationID;
	private ContactData contactData;
	private DeviceTypeEnum deviceTypeEnum;
	private DeviceData deviceData;
	private Location location;
	// registration ID - to return answer to this regID 
	// in case of "status" command
	private String regIDToReturnMessageTo;
//	private TrackLocationServiceStatusEnum trackLocationServiceStatusEnum;
//	private PushNotificationServiceStatusEnum pushNotificationServiceStatusEnum;
	private String key;
	private String value;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public CommandEnum getCommand() {
		return command;
	}
	public void setCommand(CommandEnum command) {
		this.command = command;
	}
	public String getRegistrationID() {
		return registrationID;
	}
	public void setRegistrationID(String registrationID) {
		this.registrationID = registrationID;
	}
	public ContactData getContactData() {
		return contactData;
	}
	public void setContactData(ContactData contactData) {
		this.contactData = contactData;
	}
	public DeviceTypeEnum getDeviceTypeEnum() {
		return deviceTypeEnum;
	}
	public void setDeviceTypeEnum(DeviceTypeEnum deviceTypeEnum) {
		this.deviceTypeEnum = deviceTypeEnum;
	}
	public DeviceData getDeviceData() {
		return deviceData;
	}
	public void setDeviceData(DeviceData deviceData) {
		this.deviceData = deviceData;
	}
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	public String getRegIDToReturnMessageTo() {
		return regIDToReturnMessageTo;
	}
	public void setRegIDToReturnMessageTo(String regIDToReturnMessageTo) {
		this.regIDToReturnMessageTo = regIDToReturnMessageTo;
	}
//	public TrackLocationServiceStatusEnum getTrackLocationServiceStatusEnum() {
//		return trackLocationServiceStatusEnum;
//	}
//	public void setTrackLocationServiceStatusEnum(
//			TrackLocationServiceStatusEnum trackLocationServiceStatusEnum) {
//		this.trackLocationServiceStatusEnum = trackLocationServiceStatusEnum;
//	}
//	public PushNotificationServiceStatusEnum getPushNotificationServiceStatusEnum() {
//		return pushNotificationServiceStatusEnum;
//	}
//	public void setPushNotificationServiceStatusEnum(
//			PushNotificationServiceStatusEnum pushNotificationServiceStatusEnum) {
//		this.pushNotificationServiceStatusEnum = pushNotificationServiceStatusEnum;
//	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
