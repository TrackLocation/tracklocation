package com.doat.tracklocation.datatype;

import android.os.Parcel;
import android.os.Parcelable;

public class DeviceData implements Parcelable{
	private String deviceName; // free text
	private DeviceTypeEnum deviceTypeEnum; // phone/computer/tablet
	private String deviceMac;

	public DeviceData() {}
	
	public DeviceData(String deviceMac) {
		this.deviceMac = deviceMac;
	}
	
	public DeviceData(Parcel in ) {
		deviceName = in.readString();
		deviceMac = in.readString();
		deviceTypeEnum = in.readParcelable(DeviceTypeEnum.class.getClassLoader());
    }
	
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
	public String getDeviceMac() {
		return deviceMac;
	}
	public void setDeviceMac(String deviceMac) {
		this.deviceMac = deviceMac;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(deviceName);	
		dest.writeString(deviceMac);
		dest.writeParcelable(deviceTypeEnum, flags);
	}
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public DeviceData createFromParcel(Parcel in ) {
            return new DeviceData( in );
        }

        public DeviceData[] newArray(int size) {
            return new DeviceData[size];
        }
    };
}
