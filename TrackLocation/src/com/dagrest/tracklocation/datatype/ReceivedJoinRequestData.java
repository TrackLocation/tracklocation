package com.dagrest.tracklocation.datatype;

public class ReceivedJoinRequestData {
	private String phoneNumber;
	private String mutualId;
	private String regId;
	private String account;
	private String macAddress;
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
	public String getRegId() {
		return this.regId;
	}
	public void setRegId(String regId) {
		this.regId = regId;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public String toString() {
		String newRegId;
		if(this.regId != null && !this.regId.isEmpty()){
			newRegId = "NON-EMPTY";
		} else {
			newRegId = "EMPTY";
		}
		return "ReceivedJoinRequestData [phoneNumber=" + phoneNumber
				+ ", mutualId=" + mutualId + ", RegId=" + newRegId + ", account="
				+ account + ", macAddress=" + macAddress + ", timestamp="
				+ timestamp + "]";
	}
}
