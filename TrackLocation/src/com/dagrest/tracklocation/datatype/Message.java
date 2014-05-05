package com.dagrest.tracklocation.datatype;

import java.util.List;

public class Message {
	private List<String> registration_ids;
	private MessageData data;
	
	public List<String> getRegistrationIDs() {
		return registration_ids;
	}
	public void setRegistrationIDs(List<String> registration_ids) {
		this.registration_ids = registration_ids;
	}
	public MessageData getData() {
		return data;
	}
	public void setData(MessageData data) {
		this.data = data;
	}
}
