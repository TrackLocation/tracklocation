package com.dagrest.tracklocation.datatype;

import java.util.ArrayList;
import java.util.List;

public class ContactDeviceDataList {
	private List<ContactDeviceData> contactDeviceDataList;
	
	public ContactDeviceDataList() {}
	
	public ContactDeviceDataList(String email, String deviceMac, String phoneNumber, 
			String regId, String guidId) {
		ContactData contactData = new ContactData(email);
		DeviceData deviceData = new DeviceData(deviceMac);
		ContactDeviceData contactDeviceData = new ContactDeviceData(phoneNumber, 
			regId, guidId);
		contactDeviceData.setContactData(contactData);
		contactDeviceData.setDeviceData(deviceData);
		getContactDeviceDataList().add(contactDeviceData);
	}

	public List<ContactDeviceData> getContactDeviceDataList() {
		if( contactDeviceDataList == null){
			contactDeviceDataList = new ArrayList<ContactDeviceData>();
		} 
		return contactDeviceDataList;
	}
}
