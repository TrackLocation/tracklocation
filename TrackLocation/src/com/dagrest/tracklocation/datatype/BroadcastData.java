package com.dagrest.tracklocation.datatype;

public class BroadcastData {
	private MessageDataContactDetails contactDetails;
	private MessageDataLocation location;
	private String key;
	private String value;
	
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
}
