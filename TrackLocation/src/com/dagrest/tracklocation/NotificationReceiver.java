package com.dagrest.tracklocation;

import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;

import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class NotificationReceiver extends Activity {

	private String className = this.getClass().getName();
	private String methodName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		methodName = "onCreate";

		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);

		//RingtoneManager ringMan = new RingtoneManager(this);
	    //ringMan.stopPreviousRingtone();
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), alert/*Uri.parse(ringTonePath)*/);
		boolean isPlaying = ringtone.isPlaying();
		ringtone.stop();
	    this.finish();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		methodName = "onDestroy";
		LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_DESTROY] {" + className + "} -> " + methodName);
	}

}
