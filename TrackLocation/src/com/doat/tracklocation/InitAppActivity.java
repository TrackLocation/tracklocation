package com.doat.tracklocation;

import java.util.List;

import com.doat.tracklocation.dialog.ChooseAccountDialog;
import com.doat.tracklocation.dialog.ICommonDialogOnClickListener;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.InitAppUtils;
import com.doat.tracklocation.utils.Preferences;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class InitAppActivity extends Activity {

	private String className;
	private String methodName;
	private Context context;
	private ProgressDialog waitingInitAppDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		className = this.getClass().getName();
		methodName = "onCreate";

		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);

		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

	@Override
	protected void onStart() {
		methodName = "onStart";
		super.onStart();
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		waitingInitAppDialog = launchWaitingInitAppDialog(InitAppActivity.this);

		context = getApplicationContext();
		if( Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT) == null || 
			Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT).isEmpty() ) {
			
			// INIT START ===================================================
			String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
			if( account == null || account.isEmpty() ){
				getCurrentAccount();
			} else {
				InitAppUtils.initApp(this, context);
			}
		} else {
			Intent intent = new Intent(InitAppActivity.this, MapActivity.class);
			intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK );
			context.startActivity(intent);
			finish();
		}

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	@Override
	protected void onStop() {
		methodName = "onStop";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_Stop] {" + className + "} -> " + methodName);

		if(waitingInitAppDialog != null){
			Log.i(CommonConst.LOG_TAG, "waitingInitAppDialog.dismiss");
			waitingInitAppDialog.dismiss();
		}
    	super.onStop();

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	private void getCurrentAccount(){		
		// CURRENT ACCOUNT
		if( Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT) == null || 
			Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT).isEmpty() ) {
			
			List<String> accountList = InitAppUtils.getAccountList(context);
			if(accountList != null && accountList.size() == 1){
				String account = accountList.get(0);
				Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT, account);
				InitAppUtils.initApp(InitAppActivity.this, context);
			} else {
        		ChooseAccountDialog chooseAccountDialog =
        				new ChooseAccountDialog(this, new ICommonDialogOnClickListener(){
        			@Override
        			public void doOnChooseItem(int which) {
        				List<String> accountList = InitAppUtils.getAccountList(context);
        				String account = accountList.get(which);
        				Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT, account);
        				InitAppUtils.initApp(InitAppActivity.this, context);
        			}

					@Override
					public void doOnPositiveButton(Object data) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void doOnNegativeButton(Object data) {
						// TODO Auto-generated method stub
						
					}
        		});
        		chooseAccountDialog.setDialogTitle("Choose current account:");
        		chooseAccountDialog.setItemsList(accountList.toArray(new String[0]));
        		chooseAccountDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
        		chooseAccountDialog.showDialog();
        		chooseAccountDialog.setCancelable(false);
     		}
		}
	}

	private static ProgressDialog launchWaitingInitAppDialog(Activity mainActivity) {
		ProgressDialog waitingDialog = new ProgressDialog(mainActivity);
	    waitingDialog.setTitle("Application intializations");
	    waitingDialog.setMessage("Please wait ...");
	    waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    waitingDialog.setIndeterminate(true);
	    waitingDialog.show();
	    waitingDialog.setCanceledOnTouchOutside(false);
	    
	    Log.i(CommonConst.LOG_TAG, "waitingInitAppDialog - show()");
	    return waitingDialog;
	}

}
