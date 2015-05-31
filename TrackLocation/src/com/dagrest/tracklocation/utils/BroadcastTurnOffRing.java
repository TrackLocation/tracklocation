package com.dagrest.tracklocation.utils;

import com.dagrest.tracklocation.datatype.NotificationBroadcastData;

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

