package com.doat.tracklocation.concurrent;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.doat.tracklocation.http.HttpUtils;
import com.doat.tracklocation.utils.CommonConst;

public class SendMessageAsync implements Runnable {
	
	private String jsonMessage;
	private Context context;
	
	public SendMessageAsync(String jsonMessage, Context context) {
		this.jsonMessage = jsonMessage;
		this.context = context;
	}

	@Override
	public void run() {
		try {
			HttpUtils.sendMessageToBackend(jsonMessage, context);
		} catch (Exception e) {
			Log.e(CommonConst.LOG_TAG, e.getMessage(), e);
		}	
	}

}

// Way to run the function:
// Runnable sendMessageAsync = new SendMessageAsync(jsonMessage);
// new Thread(sendMessageAsync).start();
