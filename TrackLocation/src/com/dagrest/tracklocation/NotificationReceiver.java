package com.dagrest.tracklocation;

import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

public class NotificationReceiver extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//RingtoneManager ringMan = new RingtoneManager(this);
	    //ringMan.stopPreviousRingtone();
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), alert/*Uri.parse(ringTonePath)*/);
		boolean isPlaying = ringtone.isPlaying();
		ringtone.stop();
	    this.finish();
	}

}
