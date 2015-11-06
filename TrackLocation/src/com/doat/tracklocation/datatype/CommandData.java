package com.doat.tracklocation.datatype;

import java.util.ArrayList;
import java.util.List;

import com.doat.tracklocation.exception.UnableToSendCommandException;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;

import android.content.Context;
import android.util.Log;

public class CommandData extends CommandDataBasic {
	private ContactDeviceDataList contactDeviceDataList; //listRegIDs - registration_IDs of the contacts that command will be send to
	private String methodName;
	private String logMessage;
	
	public CommandData(Context context,
			ContactDeviceDataList contactDeviceDataList, CommandEnum command,
			String message, MessageDataContactDetails senderMessageDataContactDetails,
			MessageDataLocation location, String key, String value, AppInfo appInfo) throws UnableToSendCommandException {
		super(context,
				command,
				message, senderMessageDataContactDetails,
				location, key, value, appInfo);
		this.contactDeviceDataList = contactDeviceDataList;
		listAccounts = new ArrayList<String>();
		listRegIDs = new ArrayList<String>();
		initialValuesCheck();
		prepareAccountAndRegIdLists(listAccounts, listRegIDs);
	}
		
	private void initialValuesCheck() throws UnableToSendCommandException {
		if(contactDeviceDataList == null){
			notificationMessage = "Failed to send command: " + command + ". Message: " + message + ". Key: " + key + ". There is no recipient list defined";
			throw new UnableToSendCommandException(notificationMessage);
		} 
	}
	
	@Override
	protected void prepareAccountAndRegIdLists(List<String> listAccounts,
			List<String> listRegIDs) {
		super.prepareAccountAndRegIdLists(listAccounts, listRegIDs);
		methodName = "prepareAccountAndRegIdLists";
		// Collect registration_IDs of the contacts that command will be send to
		if(contactDeviceDataList == null){
			logMessage = "Contacts and devices data list is empty. Unable to prepare recipient list.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			return;
		}
		for (ContactDeviceData contactDeviceData : contactDeviceDataList) {
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
