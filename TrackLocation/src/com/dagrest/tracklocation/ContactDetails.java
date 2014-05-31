package com.dagrest.tracklocation;

import java.util.ArrayList;
import java.util.List;

public class ContactDetails {

	private String contactName;
	private final List<String> phoneNumbersList = new ArrayList<String>();
	
	public ContactDetails() {}
	
	public ContactDetails(String string) {
		this.contactName = string;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public List<String> getPhoneNumbersList() {
		return phoneNumbersList;
	}

} 
