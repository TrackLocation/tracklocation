package com.doat.tracklocation.utils;

import com.doat.tracklocation.datatype.NotificationBroadcastData;

import android.content.Context;

public class BroadcastTurnOffRing extends BroadcastMessage {

	public BroadcastTurnOffRing(Context context, String broadcastAction) {
		super(context, broadcastAction);
	}
	
	@Override
	protected void onReceiveAction(NotificationBroadcastData broadcastData) {
		super.onReceiveAction(broadcastData);
	}

}

