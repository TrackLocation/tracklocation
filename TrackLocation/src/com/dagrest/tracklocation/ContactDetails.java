package com.dagrest.tracklocation;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

public class ContactDetails {

	private String contactName;
	private Bitmap contactPhoto;
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

	public Bitmap getContactPhoto() {
		return contactPhoto;
	}

	public void setContactPhoto(Bitmap contactPhoto) {
		this.contactPhoto = contactPhoto;
	}

} 
