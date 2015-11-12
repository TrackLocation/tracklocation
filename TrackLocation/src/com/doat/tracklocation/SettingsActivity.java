package com.doat.tracklocation;

import com.doat.tracklocation.R;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	public static final String KEY_PREF_SYNC_CONN = "pref_syncConnectionType";
	
	private String className = this.getClass().getName();
	private String methodName;
	private Boolean bThemeChanged = false;

	@SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String theameName = sharedPref.getString("pref_theame", "0x7f070006");		
		setTheme(Integer.decode(theameName));
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        super.onCreate(savedInstanceState);        
        
        methodName = "onCreate";
        
		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);
		addPreferencesFromResource(R.xml.settings);
		initSummary(getPreferenceScreen());
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
	
	@Override
	protected void onResume() {
	    super.onResume();		    
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	

	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	    /*Intent data = new Intent();
		data.putExtra(CommonConst.THEME_CHANGED, bThemeChanged);		
		setResult(RESULT_OK, data);		
		finish();*/
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("pref_theame")){
			String valueStr = "";
			Preference connectionPref = findPreference(key);	
			if (connectionPref instanceof ListPreference) {
		        ListPreference listPref = (ListPreference) connectionPref;
		        connectionPref.setSummary(listPref.getEntry());
		        valueStr = listPref.getValue();
		    }			
			Intent data = new Intent();
			data.putExtra(CommonConst.THEME_CHANGED, true);		
			setResult(RESULT_OK, data);		
			finish();			
		}		
	}
	
	private void updatePrefSummary(Preference p) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());
        }        
    }
	
	private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }
}
