package com.dagrest.tracklocation.datatype;

public class JoinRequestData {
	private String phoneNumber;
	private String mutualId;
	private String timestamp;
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getMutualId() {
		return mutualId;
	}
	public void setMutualId(String mutualId) {
		this.mutualId = mutualId;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
}
