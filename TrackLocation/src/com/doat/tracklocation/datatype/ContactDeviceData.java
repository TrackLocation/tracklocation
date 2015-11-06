package com.doat.tracklocation.datatype;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.doat.tracklocation.Controller;

public class ContactDeviceData implements Parcelable{
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
	
	public ContactDeviceData(Parcel in ) {
		phoneNumber = in.readString();
		imei = in.readString();
		registration_id = in.readString();
		guid = in.readString();
		locationSharing = in.readInt();
		tracking = in.readInt();
		contactData = in.readParcelable(ContactData.class.getClassLoader());
		deviceData = in.readParcelable(DeviceData.class.getClassLoader());
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

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(phoneNumber);
		dest.writeString(imei);
		dest.writeString(registration_id);
		dest.writeString(guid);
		dest.writeInt(locationSharing);
		dest.writeInt(tracking);
		dest.writeParcelable(contactData, flags);
		dest.writeParcelable(deviceData, flags);
	}
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ContactDeviceData createFromParcel(Parcel in ) {
            return new ContactDeviceData( in );
        }

        public ContactDeviceData[] newArray(int size) {
            return new ContactDeviceData[size];
        }
    };
}
