package com.doat.tracklocation.context;

import android.content.Context;
import android.util.Log;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.datatype.AppInfo;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;

public class AppContextBase implements IAppContext{
	protected Context context;
	protected String ownerEmail;
	protected String ownerMacAddress;
	protected String ownerRegId;
	protected String ownerPhoneNumber;
	protected ContactDeviceDataList contactDeviceDataList;
	protected AppInfo appInfo;
	protected MessageDataContactDetails senderMessageDataContactDetails;
	float batteryPercentage = -1;
	protected String className;    
    protected String methodName;
    protected String logMessage;

    
	public AppContextBase() {
		className = this.getClass().getName();
		methodName = "AppContextBase";
	}
 
	public Context getContext() {
		return context;
	}

	public String getOwnerEmail() {
		return ownerEmail;
	}

	public String getOwnerMacAddress() {
		return ownerMacAddress;
	}

	public String getOwnerRegId() {
		return ownerRegId;
	}

	public String getOwnerPhoneNumber() {
		return ownerPhoneNumber;
	}

	public ContactDeviceDataList getContactDeviceDataList() {
		return contactDeviceDataList;
	}

	public AppInfo getAppInfo() {
		return appInfo;
	}

	public MessageDataContactDetails getSenderMessageDataContactDetails() {
		return senderMessageDataContactDetails;
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
		ownerEmail = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
        ownerMacAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
        ownerRegId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
        ownerPhoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
		appInfo = Controller.getAppInfo(context);
		senderMessageDataContactDetails = 
			new MessageDataContactDetails(ownerEmail, ownerMacAddress, ownerPhoneNumber, ownerRegId, -1);
	}

}
