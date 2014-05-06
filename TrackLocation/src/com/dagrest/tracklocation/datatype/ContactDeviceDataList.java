package com.dagrest.tracklocation.datatype;

import java.util.ArrayList;
import java.util.List;

public class ContactDeviceDataList {
	private List<ContactDeviceData> contactDeviceDataList;

	public List<ContactDeviceData> getCustomerDataFromFileList() {
		if( contactDeviceDataList == null){
			contactDeviceDataList = new ArrayList<ContactDeviceData>();
		} 
		return contactDeviceDataList;
	}
}
