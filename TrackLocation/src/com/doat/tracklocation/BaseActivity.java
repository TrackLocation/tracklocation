package com.doat.tracklocation;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.WindowManager;

public class BaseActivity extends Activity {
	protected String className;    
    protected String methodName;
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		/*SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		int theameId = Integer.parseInt(sharedPref.getString("pref_theame", "0"));
		switch (theameId) {
		case 1:
			theameId = R.style.AppTheme_Material_Light;
			break;
		case 2 : 
			theameId = R.style.AppTheme_Material_Light_Blue;
			break;
		case 3 : 
			theameId = R.style.AppTheme_Material_Dark;
			break;
		default:
			theameId = R.style.AppTheme;
			break;
		}
		
		getApplication().setTheme(theameId);*/
		
		super.onCreate(savedInstanceState);
		/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		}		*/
		//setTheme(theameId);
		
		//getActionBar().setDisplayShowHomeEnabled(true);
		
		//getActionBar().setIcon(R.drawable.main_icon_96);
	}
}
