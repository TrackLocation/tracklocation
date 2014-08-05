package com.dagrest.tracklocation.datatype;

public class BroadcastData {
	private MessageDataContactDetails contactDetails;
	private MessageDataLocation location;
	
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
