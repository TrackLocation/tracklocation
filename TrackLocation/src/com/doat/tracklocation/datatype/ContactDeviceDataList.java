package com.doat.tracklocation.datatype;

import java.util.ArrayList;

public class ContactDeviceDataList extends ArrayList<ContactDeviceData>{
	private static final long serialVersionUID = 7346181519310520513L;

	public ContactDeviceDataList() {}
	
	public ContactDeviceDataList(String email, String deviceMac, String phoneNumber, String regId, String guidId) {
		ContactData contactData = new ContactData(email);
		DeviceData deviceData = new DeviceData(deviceMac);
		ContactDeviceData contactDeviceData = new ContactDeviceData(phoneNumber, regId, guidId);
		contactDeviceData.setContactData(contactData);
		contactDeviceData.setDeviceData(deviceData);
		this.add(contactDeviceData);
	}

	@Override
	public String toString() {
		String output = "";
		for (ContactDeviceData contactDeviceData : this) {
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
		return output;
	}
	
	public ContactDeviceData getContactDeviceDataByContactData(String email){
		
		for(ContactDeviceData cdData : this){
			if (cdData.getContactData().getEmail().equals(email))
				return cdData;
		}
		 
		return null;
	}

	public void remove(ContactDeviceData contactDeviceData) {
		int iCount = 0;
		for(ContactDeviceData cdData : this){
			if (cdData.getContactData().getEmail().equals(contactDeviceData.getContactData().getEmail())){
				this.remove(iCount);
				return;
			}
			iCount++;
		}		
	}
}
