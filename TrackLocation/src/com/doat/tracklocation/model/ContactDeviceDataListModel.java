package com.doat.tracklocation.model;

import java.util.HashMap;

import com.doat.tracklocation.ContactListArrayAdapter;
import com.doat.tracklocation.datatype.ContactDeviceData;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.db.DBLayer;

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
	
	public ContactDeviceDataList getContactDeviceDataList(boolean bForcereload) {
		if (contactDeviceDataList == null || bForcereload){
			contactDeviceDataList = DBLayer.getInstance().getContactDeviceDataList(null);
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

}
