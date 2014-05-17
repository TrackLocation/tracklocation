package com.dagrest.tracklocation.datatype;

public class DeviceData {
	private String deviceName; // free text
	private DeviceTypeEnum deviceTypeEnum; // phone/computer/tablet
	private String imei;
	private String deviceNumber;
	private String guid;

	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public DeviceTypeEnum getDeviceTypeEnum() {
		return deviceTypeEnum;
	}
	public void setDeviceTypeEnum(DeviceTypeEnum deviceTypeEnum) {
		this.deviceTypeEnum = deviceTypeEnum;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public String getDeviceNumber() {
		return deviceNumber;
	}
	public void setDeviceNumber(String deviceNumber) {
		this.deviceNumber = deviceNumber;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
}
