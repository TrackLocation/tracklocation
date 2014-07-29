package com.dagrest.tracklocation.datatype;

public class MessageData {
	private String message;
	private String time;
	private CommandEnum command;
	private String registrationID;
	private DeviceTypeEnum deviceTypeEnum;
	// registration ID - to return answer to this regID 
	// in case of "status" command
	private String regIDToReturnMessageTo;
	private String key;
	private String value;
	
	private MessageDataLocation location;
	private MessageDataContactDetails contactDetails;
	
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
	public DeviceTypeEnum getDeviceTypeEnum() {
		return deviceTypeEnum;
	}
	public void setDeviceTypeEnum(DeviceTypeEnum deviceTypeEnum) {
		this.deviceTypeEnum = deviceTypeEnum;
	}
	public String getRegIDToReturnMessageTo() {
		return regIDToReturnMessageTo;
	}
	public void setRegIDToReturnMessageTo(String regIDToReturnMessageTo) {
		this.regIDToReturnMessageTo = regIDToReturnMessageTo;
	}
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
	public MessageDataLocation getLocation() {
		return location;
	}
	public void setLocation(MessageDataLocation location) {
		this.location = location;
	}
	public MessageDataContactDetails getContactDetails() {
		return contactDetails;
	}
	public void setContactDetails(MessageDataContactDetails contactDetails) {
		this.contactDetails = contactDetails;
	}
	
}
