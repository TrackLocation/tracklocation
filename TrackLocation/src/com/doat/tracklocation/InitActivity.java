package com.doat.tracklocation;

import com.doat.tracklocation.controller.MainActivityController;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;

import android.os.Bundle;
import android.util.Log;

public class InitActivity extends BaseActivity {

//	private HashMap<String, ContactData> selectedAccountList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		className = this.getClass().getName();
		methodName = "onCreate";

		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);

		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		context = getApplicationContext();
//		selectedAccountList = new HashMap<String, ContactData>();

		if(mainActivityController == null){
        	mainActivityController = new MainActivityController(InitActivity.this, context, true);
        }	
		
//		Intent intent = new Intent(this, MapActivity.class);
//		startActivity(intent);
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

	@Override
	protected void onResume() {
		super.onResume();
//		finish();
	}
	
	@Override
	protected void onStop() {

    	LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_Stop] {" + className + "} -> " + methodName);

    	super.onStop();
	}
	
}
