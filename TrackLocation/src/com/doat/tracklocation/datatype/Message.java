package com.doat.tracklocation.datatype;

import java.util.List;

public class Message {
	private List<String> registration_ids;
	private MessageData data;
	private long time_to_live;
	
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
	public long getTime_to_live() {
		return time_to_live;
	}
	public void setTime_to_live(long time_to_live) {
		this.time_to_live = time_to_live;
	}
}
