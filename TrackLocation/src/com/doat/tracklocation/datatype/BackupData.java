package com.doat.tracklocation.datatype;

public class BackupData {
	private ContactDeviceDataList contactDeviceDataList;
	private BackupSettings settings;
	
	public BackupData() {
	}
	
	public BackupData(ContactDeviceDataList contactDeviceDataList,
			BackupSettings settings) {
		super();
		this.contactDeviceDataList = contactDeviceDataList;
		this.settings = settings;
	}

	public ContactDeviceDataList getContactDeviceDataList() {
		return contactDeviceDataList;
	}
	public void setContactDeviceDataList(ContactDeviceDataList contactDeviceDataList) {
		this.contactDeviceDataList = contactDeviceDataList;
	}
	public BackupSettings getSettings() {
		return settings;
	}
	public void setSettings(BackupSettings settings) {
		this.settings = settings;
	}
}
