package com.doat.tracklocation;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.WindowManager;

public class BaseActivity extends Activity {
	protected String className;    
    protected String methodName;

	@Override
    public void onBackPressed() {
    	super.onBackPressed();
    	finish();
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
	}
}
