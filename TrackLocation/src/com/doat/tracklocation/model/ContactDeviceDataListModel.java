package com.doat.tracklocation.model;

import android.content.Context;

import com.doat.tracklocation.ContactListArrayAdapter;
import com.doat.tracklocation.Controller;
import com.doat.tracklocation.datatype.ContactData;
import com.doat.tracklocation.datatype.ContactDeviceData;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.db.DBLayer;

import java.util.HashMap;
import java.util.Map;

public class ContactDeviceDataListModel {
	private static ContactDeviceDataListModel contactDeviceDataListModel;
	private ContactDeviceDataList contactDeviceDataList;
	private HashMap<String, ContactListArrayAdapter> adapterMap = new HashMap<String, ContactListArrayAdapter>();
	
	//Single instance 
	public static ContactDeviceDataListModel getInstance(){
		if (contactDeviceDataListModel == null){
			contactDeviceDataListModel = new ContactDeviceDataListModel();
		}
		return contactDeviceDataListModel;
	}
	
	public ContactDeviceDataListModel(){
		
	}
	
	public ContactDeviceDataList getContactDeviceDataList(Context context, boolean bForcereload) {
		if (contactDeviceDataList == null || bForcereload){
			contactDeviceDataList = DBLayer.getInstance().getContactDeviceDataList(null);
			for (ContactDeviceData contactDeviceData : contactDeviceDataList) {
				ContactData contactData = contactDeviceData.getContactData();
				contactData.setContactPhoto(contactData.getContactPhoto() == null ? Controller.getContactPhotoByEmail(context, contactData.getEmail()) : contactData.getContactPhoto());
			}

		}
		return contactDeviceDataList;
	}
	
	public ContactDeviceData getContactDeviceData(String account) {		
		return DBLayer.getInstance().getContactDeviceData(account);
	}
	
	public void setAdapter(String key, ContactListArrayAdapter adapter){		
		adapterMap.put(key, adapter);
	}
	
	public ContactListArrayAdapter getAdapter(String key){
		return adapterMap.get(key);
	}
	
	public void notifyDataSetChanged(){
		for (String key : adapterMap.keySet()) {
			ContactListArrayAdapter adapter = adapterMap.get(key);
			if (adapter != null){
				adapter.notifyDataSetChanged();
			}
		} 
	}
	
	public long updateContactDeviceDataList(ContactDeviceData contactDeviceData, Map<String, Object> mapData){
		return DBLayer.getInstance().updateTableContactDevice(contactDeviceData.getContactData().getEmail(), contactDeviceData.getDeviceData().getDeviceMac(), mapData);
	}

}
