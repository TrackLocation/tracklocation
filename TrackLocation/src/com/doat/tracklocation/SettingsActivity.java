package com.doat.tracklocation;

import com.doat.tracklocation.R;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.WindowManager;

public class SettingsActivity extends PreferenceActivity{
	public static final String KEY_PREF_SYNC_CONN = "pref_syncConnectionType";
	
	private String className = this.getClass().getName();
	private String methodName;

	@SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);       
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        methodName = "onCreate";
        
		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);
		addPreferencesFromResource(R.xml.settings);
		// Display the fragment as the main content.
        //getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		methodName = "onDestroy";
		LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_DESTROY] {" + className + "} -> " + methodName);
	}

}
