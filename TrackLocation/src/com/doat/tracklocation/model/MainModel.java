package com.doat.tracklocation.model;

import com.doat.tracklocation.datatype.ContactDeviceDataList;

import android.location.Location;

public class MainModel {
	
	private Location location;
	
    private ContactDeviceDataList contactDeviceDataList;
    
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	public ContactDeviceDataList getContactDeviceDataList() {
		return contactDeviceDataList;
	}
	public void setContactDeviceDataList(ContactDeviceDataList contactDeviceDataList) {
		this.contactDeviceDataList = contactDeviceDataList;
	}
}
