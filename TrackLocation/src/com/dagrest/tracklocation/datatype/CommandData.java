package com.dagrest.tracklocation.datatype;

import java.util.ArrayList;
import java.util.List;

import com.dagrest.tracklocation.log.LogManager;

import android.content.Context;
import android.util.Log;

public class CommandData extends CommandDataBasic {
	private ContactDeviceDataList contactDeviceDataList; //listRegIDs - registration_IDs of the contacts that command will be send to

	public CommandData(Context context,
			ContactDeviceDataList contactDeviceDataList, CommandEnum command,
			String message, MessageDataContactDetails senderMessageDataContactDetails,
			MessageDataLocation location, String key, String value, AppInfo appInfo) {
		super(context,
				command,
				message, senderMessageDataContactDetails,
				location, key, value, appInfo);
		this.contactDeviceDataList = contactDeviceDataList;
		initialValuesCheck();
		listAccounts = new ArrayList<String>();
		listRegIDs = new ArrayList<String>();
		prepareAccountAndRegIdLists(listAccounts, listRegIDs);
	}
		
	private void initialValuesCheck() {
		if(contactDeviceDataList == null){
			notificationMessage = "There is no recipient list defined";
			LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", notificationMessage);
			return;
		}
	}
	
	@Override
	protected void prepareAccountAndRegIdLists(List<String> listAccounts,
			List<String> listRegIDs) {
		super.prepareAccountAndRegIdLists(listAccounts, listRegIDs);
		// Collect registration_IDs of the contacts that command will be send to
		for (ContactDeviceData contactDeviceData : contactDeviceDataList.getContactDeviceDataList()) {
			ContactData contactData = contactDeviceData.getContactData();
			if(contactData != null){
				String regId = contactDeviceData.getRegistration_id();
				if(regId != null && !regId.isEmpty()){
					listRegIDs.add(contactDeviceData.getRegistration_id());
				} else {
					notificationMessage = "Empty registrationID for the following contact: " + contactData.getEmail();
					LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", notificationMessage);
					Log.e("[sendCommand:" + command.toString() + "]", notificationMessage);
				}
				listAccounts.add(contactData.getEmail());
			} else {
				LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", "Unable to get registration_ID: ContactData is null.");
				Log.e("[sendCommand:" + command.toString() + "]", "Unable to get registration_ID: contactData is null.");
			}
		}
	}

	public ContactDeviceDataList getContactDeviceDataList() {
		return contactDeviceDataList;
	}
	public void setContactDeviceDataList(ContactDeviceDataList contactDeviceDataList) {
		this.contactDeviceDataList = contactDeviceDataList;
	}
}
