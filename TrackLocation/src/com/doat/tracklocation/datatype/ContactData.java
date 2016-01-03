package com.doat.tracklocation.datatype;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.utils.CommonConst;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ContactData implements Parcelable{
	private String nick; // free text
	private String email;
	@JsonIgnore
	private transient Bitmap contactPhoto;
	@JsonIgnore
	private transient int contactStatus = CommonConst.CONTACT_STATUS_START_CONNECT;
	private final List<String> phoneNumbersList = new ArrayList<String>();
	
	public ContactData() {}
	
	public ContactData(String email) {
		this.email = email;
	}
	
	public ContactData(Parcel in ) {
		nick = in.readString();
		email = in.readString();
		contactPhoto = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
    }
	
	public String getNick() {
		if (nick == null || nick.isEmpty()){
			if (email == null || email.isEmpty()){
//				nick = "unknown";
//				email = "unknown@unknown.com";
			}
			else{
				nick = Controller.getNickNameFromEmail(email);	
			}
		}
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
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

	public int getContactStatus() {
		return contactStatus;
	}

	public void setContactStatus(int contactStatus) {
		this.contactStatus = contactStatus;
	}

	@Override
	public int describeContents() {	
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(nick);
        dest.writeString(email);
        dest.writeValue(contactPhoto);	
	}
	
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ContactData createFromParcel(Parcel in ) {
            return new ContactData( in );
        }

        public ContactData[] newArray(int size) {
            return new ContactData[size];
        }
    };
}
