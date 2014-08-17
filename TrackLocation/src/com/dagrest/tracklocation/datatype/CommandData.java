package com.dagrest.tracklocation.datatype;

import java.util.List;

import com.dagrest.tracklocation.log.LogManager;

import android.content.Context;
import android.util.Log;

public class CommandData extends CommandDataBasic {
//	private Context context; 
	private ContactDeviceDataList contactDeviceDataList; //listRegIDs - registration_IDs of the contacts that command will be send to
//	private CommandEnum command;
//	private String message;
//	private MessageDataContactDetails contactDetails; // regIDToReturnMessageTo
//	private MessageDataLocation location;
//	private String key;
//	private String value;
//	private AppInfo appInfo;
	
//	public CommandData(Context context,
//			ContactDeviceDataList contactDeviceDataList, CommandEnum command,
//			String message, MessageDataContactDetails contactDetails,
//			MessageDataLocation location, String key, String value, AppInfo appInfo) {
//		super();
//		this.context = context;
//		this.contactDeviceDataList = contactDeviceDataList;
//		this.command = command;
//		this.message = message;
//		this.contactDetails = contactDetails;
//		this.location = location;
//		this.key = key;
//		this.value = value;
//		this.appInfo = appInfo;
//	}

	public CommandData(Context context,
			ContactDeviceDataList contactDeviceDataList, CommandEnum command,
			String message, MessageDataContactDetails contactDetails,
			MessageDataLocation location, String key, String value, AppInfo appInfo) {
		super(context,
				command,
				message, contactDetails,
				location, key, value, appInfo);
		this.contactDeviceDataList = contactDeviceDataList;
	}
		
	@Override
	protected void initialValuesCheck() {
		super.initialValuesCheck();
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

	//	public Context getContext() {
//		return context;
//	}
//	public void setContext(Context context) {
//		this.context = context;
//	}
	public ContactDeviceDataList getContactDeviceDataList() {
		return contactDeviceDataList;
	}
	public void setContactDeviceDataList(ContactDeviceDataList contactDeviceDataList) {
		this.contactDeviceDataList = contactDeviceDataList;
	}
//	public CommandEnum getCommand() {
//		return command;
//	}
//	public void setCommand(CommandEnum command) {
//		this.command = command;
//	}
//	public String getMessage() {
//		return message;
//	}
//	public void setMessage(String message) {
//		this.message = message;
//	}
//	public MessageDataContactDetails getContactDetails() {
//		return contactDetails;
//	}
//	public void setContactDetails(MessageDataContactDetails contactDetails) {
//		this.contactDetails = contactDetails;
//	}
//	public MessageDataLocation getLocation() {
//		return location;
//	}
//	public void setLocation(MessageDataLocation location) {
//		this.location = location;
//	}
//	public String getKey() {
//		return key;
//	}
//	public void setKey(String key) {
//		this.key = key;
//	}
//	public String getValue() {
//		return value;
//	}
//	public void setValue(String value) {
//		this.value = value;
//	}
//	public AppInfo getAppInfo() {
//		return appInfo;
//	}
//	public void setAppInfo(AppInfo appInfo) {
//		this.appInfo = appInfo;
//	}	
}
