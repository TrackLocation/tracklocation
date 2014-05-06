package com.dagrest.tracklocation.datatype;

public class MessageData {
	private String message;
	private String time;
	private CommandEnum command;
	private String registrationID;
	private ContactData customerData;
	private DeviceTypeEnum deviceTypeEnum;
	private DeviceData deviceData;
	private Location location;
	
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
	public ContactData getCustomerData() {
		return customerData;
	}
	public void setCustomerData(ContactData customerData) {
		this.customerData = customerData;
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
}
