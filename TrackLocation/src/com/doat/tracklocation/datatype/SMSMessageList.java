package com.doat.tracklocation.datatype;

import java.util.ArrayList;
import java.util.List;

public class SMSMessageList {
	private List<SMSMessage> smsMessageList;

	public List<SMSMessage> getSmsMessageList() {
		if(smsMessageList == null){
			smsMessageList = new ArrayList<SMSMessage>();
		}
		return smsMessageList;
	}

	public void setSmsMessageList(List<SMSMessage> smsMessageList) {
		this.smsMessageList = smsMessageList;
	}
}
