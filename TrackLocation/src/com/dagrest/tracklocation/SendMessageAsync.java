package com.dagrest.tracklocation;

import android.util.Log;

import com.dagrest.tracklocation.http.HttpUtils;
import com.dagrest.tracklocation.utils.CommonConst;

public class SendMessageAsync implements Runnable {
	
	private String jsonMessage;
	
	public SendMessageAsync(String jsonMessage) {
		this.jsonMessage = jsonMessage;
	}

	@Override
	public void run() {
		try {
			HttpUtils.sendMessageToBackend(jsonMessage);
		} catch (Exception e) {
			Log.e(CommonConst.LOG_TAG, e.getMessage());
		}	
	}

}

// Way to run the function:
// Runnable sendMessageAsync = new SendMessageAsync(jsonMessage);
// new Thread(sendMessageAsync).start();
