package com.dagrest.tracklocation.json;

import java.util.List;

public class Message {
	private List<String> registrationIDs;
	private MessageData data;
	
	public List<String> getRegistrationIDs() {
		return registrationIDs;
	}
	public void setRegistrationIDs(List<String> registrationIDs) {
		this.registrationIDs = registrationIDs;
	}
	public MessageData getData() {
		return data;
	}
	public void setData(MessageData data) {
		this.data = data;
	}
}
