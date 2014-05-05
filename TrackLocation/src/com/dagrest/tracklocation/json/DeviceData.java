package com.dagrest.tracklocation.json;

public class DeviceData {
	private String deviceName; // free text
	private DeviceTypeEnum deviceTypeEnum; // phone/computer/tablet
	
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
}
