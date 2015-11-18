package com.doat.tracklocation;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

public class BaseActivity extends Activity {
	protected String className;    
    protected String methodName;
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
	    actionBar.setHomeButtonEnabled(false);	    
	    actionBar.setDisplayShowHomeEnabled(false);
	    actionBar.setDisplayShowTitleEnabled(true);
	}
}
