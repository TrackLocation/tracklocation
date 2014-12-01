package com.dagrest.tracklocation.datatype;

import java.util.ArrayList;
import java.util.List;

public class ContactDeviceDataList{
	private List<ContactDeviceData> contactDeviceDataList;
	
	public ContactDeviceDataList() {}
	
	public ContactDeviceDataList(String email, String deviceMac, String phoneNumber, String regId, String guidId) {
		ContactData contactData = new ContactData(email);
		DeviceData deviceData = new DeviceData(deviceMac);
		ContactDeviceData contactDeviceData = new ContactDeviceData(phoneNumber, regId, guidId);
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
					tempRegId = "NON-EMPTY";
				}
				output += tempRegId + ";";
			}
		}
		return output;
	}
	
	public ContactDeviceData getContactDeviceDataByContactData(ContactData data){
		
		for(ContactDeviceData cdData : getContactDeviceDataList()){
			if (cdData.getContactData().getEmail().equals(data.getEmail()))
				return cdData;
		}
		 
		return null;
	}

	public void remove(ContactDeviceData contactDeviceData) {
		int iCount = 0;
		for(ContactDeviceData cdData : getContactDeviceDataList()){
			if (cdData.getContactData().getEmail().equals(contactDeviceData.getContactData().getEmail())){
				getContactDeviceDataList().remove(iCount);
				return;
			}
			iCount++;
		}
		
	}
	
}
