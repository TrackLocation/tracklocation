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

	@Override
	public String toString() {
		String output = "";
		if(contactDeviceDataList != null){
			for (ContactDeviceData contactDeviceData : contactDeviceDataList) {
				ContactData contactData = contactDeviceData.getContactData();
				if(contactData != null){
					output += contactData.getEmail() + ";";
					output += contactData.getFirstName() + ";";
					output += contactData.getLastName() + ";";
					output += contactData.getNick() + ";";
				}
				DeviceData deviceData = contactDeviceData.getDeviceData();
				if(deviceData != null){
					deviceData.getDeviceMac(); // do not output to log
				}
				contactDeviceData.getPhoneNumber();
				String regId = contactDeviceData.getRegistration_id();
				String tempRegId;
				if(regId == null || regId.isEmpty()){
					tempRegId = "EMPTY";
				} else {
					tempRegId = "***";
				}
				output += tempRegId + ";";
			}
		}
		return output;
	}
	
	
}
