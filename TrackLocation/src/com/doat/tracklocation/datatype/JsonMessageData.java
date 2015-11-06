package com.doat.tracklocation.datatype;

import java.util.List;

public class JsonMessageData {
	private List<String> listRegIDs; 					// registration_IDs of the contacts that command will be send to
	//private String regIDToReturnMessageTo;				// sender's registartion_ID (contact that response will be returned to)
	private CommandEnum command; 
	private String message; 							// messageString
	private MessageDataContactDetails contactDetails;	// sender's contact details
	private MessageDataLocation location;				// sender's location details
	private AppInfo appInfo;							// application info
	private String time; 								// current time - Controller.getCurrentDate()
	private String key;									// key (free pair of key/value)
	private String value;								// value (free pair of key/value)

	public JsonMessageData(List<String> listRegIDs,
			/*String regIDToReturnMessageTo,*/ CommandEnum command, String message,
			MessageDataContactDetails contactDetails,
			MessageDataLocation location, AppInfo appInfo, String time,
			String key, String value) {
		super();
		this.listRegIDs = listRegIDs;
		//this.regIDToReturnMessageTo = regIDToReturnMessageTo;
		this.command = command;
		this.message = message;
		this.contactDetails = contactDetails;
		this.location = location;
		this.appInfo = appInfo;
		this.time = time;
		this.key = key;
		this.value = value;
	}
	
	public List<String> getListRegIDs() {
		return listRegIDs;
	}
	public void setListRegIDs(List<String> listRegIDs) {
		this.listRegIDs = listRegIDs;
	}
//	public String getRegIDToReturnMessageTo() {
//		return regIDToReturnMessageTo;
//	}
//	public void setRegIDToReturnMessageTo(String regIDToReturnMessageTo) {
//		this.regIDToReturnMessageTo = regIDToReturnMessageTo;
//	}
	public CommandEnum getCommand() {
		return command;
	}
	public void setCommand(CommandEnum command) {
		this.command = command;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public MessageDataContactDetails getContactDetails() {
		return contactDetails;
	}
	public void setContactDetails(MessageDataContactDetails contactDetails) {
		this.contactDetails = contactDetails;
	}
	public MessageDataLocation getLocation() {
		return location;
	}
	public void setLocation(MessageDataLocation location) {
		this.location = location;
	}
	public AppInfo getAppInfo() {
		return appInfo;
	}
	public void setAppInfo(AppInfo appInfo) {
		this.appInfo = appInfo;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

}
