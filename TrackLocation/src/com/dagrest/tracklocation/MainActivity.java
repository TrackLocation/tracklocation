package com.dagrest.tracklocation;

import com.dagrest.tracklocation.log.LogManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity {

	final int RQS_GooglePlayServices = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		// my 2 cents + 2 cents
		
		System.out.println("Started");
		LogManager.LogInfoMsg(this.getClass().getName(), "onCreate", "NEW Test message from onCreate method");
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		  
		  if (resultCode == ConnectionResult.SUCCESS){
		   Toast.makeText(getApplicationContext(), 
		     "isGooglePlayServicesAvailable SUCCESS", 
		     Toast.LENGTH_LONG).show();
		  }else{
		   GooglePlayServicesUtil.getErrorDialog(resultCode, this, RQS_GooglePlayServices);
		  }
	}
	
}
