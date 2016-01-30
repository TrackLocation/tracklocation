package com.doat.tracklocation.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.R;
import com.doat.tracklocation.concurrent.RegisterToGCMInBackground;
import com.doat.tracklocation.datatype.AppInfo;
import com.doat.tracklocation.datatype.BackupDataOperations;
import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.datatype.CommandKeyEnum;
import com.doat.tracklocation.datatype.CommandValueEnum;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.dialog.ChooseAccountDialog;
import com.doat.tracklocation.dialog.ICommonDialogOnClickListener;
import com.doat.tracklocation.dialog.InfoDialog;
import com.doat.tracklocation.exception.CheckPlayServicesException;
import com.doat.tracklocation.log.LogManager;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class InitAppUtils {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static void initApp(Activity mainActivity, Context context){
		String className = "InitAppController";
    	String methodName = "startMainActivityController";
    	String logMessage;
    	
    	LogManager.LogFunctionCall(className, methodName);
    	Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		String googleProjectNumber = mainActivity.getResources().getString(R.string.google_project_number);
		Preferences.setPreferencesString(context, CommonConst.GOOGLE_PROJECT_NUMBER, googleProjectNumber);

		BackupDataOperations backupData = new BackupDataOperations();

		boolean isBackUpRestoreSuccess = backupData.restore();
		if(isBackUpRestoreSuccess != true){
			logMessage = methodName + " -> Restore process failed.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
		
//		// Version check
//		AppInfo preinstalledAppInfo = Controller.getAppInfo(context);
		
		// INIT START ===================================================
		String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		if( account == null || account.isEmpty() ){
			InitAppUtils.getCurrentAccount(mainActivity, context);
		} else {
			InitAppUtils.initCont(mainActivity, context);
		}
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
    }
    
    private static boolean initCont(final Activity mainActivity, final Context context){
		String className = "InitAppController";
    	String methodName = "initCont";
    	String logMessage;
    	LogManager.LogFunctionCall(className, methodName);
    	Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		Controller.saveAppInfoToPreferences(context);
		
		// PHONE NUMBER
		String phoneNumber = InitAppUtils.getPhoneNumber(context);
		//Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_PHONE_NUMBER, phoneNumber);
		if(phoneNumber == null || phoneNumber.isEmpty()){
			phoneNumber = UUID.randomUUID().toString().replace("-", "");
		}
		Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER, phoneNumber);
		
		// MAC ADDRESS
		String macAddress = InitAppUtils.getMacAddress(mainActivity);
		//Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS, macAddress);
		Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS, macAddress);
		
		// INIT END ===================================================
		
		AppInfo installedAppInfo = Controller.getAppInfo(context);
		
		// Version check
		AppInfo preinstalledAppInfo = Controller.getAppInfo(context);
		if(preinstalledAppInfo.getVersionNumber() < installedAppInfo.getVersionNumber()){
			// reset resistrationID
			Preferences.setPreferencesString(context, CommonConst.PREFERENCES_REG_ID, "");
			Preferences.setPreferencesString(context, CommonConst.APP_INST_DETAILS, "");
		}
		
//		// Create application details during first installation 
//		// if was created already just returns the details:
//		//    - First installation's timestamp 
//		//    - ApInfo: version number and version name
//		AppInstDetails appInstDetails = new AppInstDetails(context); 
		
		// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
		try {
			Controller.checkPlayServices(context);
		} catch (CheckPlayServicesException e) {
			String errorMessage = e.getMessage();
			if(CommonConst.PLAYSERVICES_ERROR.equals(errorMessage)){
	            GooglePlayServicesUtil.getErrorDialog(e.getResultCode(), mainActivity,
	            	PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else if(CommonConst.PLAYSERVICES_DEVICE_NOT_SUPPORTED.equals(errorMessage)){
				// Show dialog with errorMessage and exit from application 
//				showNotificationDialog("\nGoogle Play Services not supported with this device.\nProgram will be closed.\n", "FINISH");
        		String title = "Warning";
        		String dialogMessage = "\nGoogle Play Services not supported with this device.\nProgram will be closed.\n";
        		new InfoDialog(mainActivity, context, title, dialogMessage, null);
			}
            Log.e(CommonConst.LOG_TAG, "No valid Google Play Services APK found.");
    		LogManager.LogInfoMsg(className, methodName, "No valid Google Play Services APK found.");
			//finish();
		}
		
		// ===============================================================
		// READ CONTACT AND DEVICE DATA FROM JSON FILE AND INSERT IT TO DB
		// ===============================================================
		// Read contact and device data from json file and insert it to DB
		String jsonStringContactDeviceData = Utils.getContactDeviceDataFromJsonFile();
		if(jsonStringContactDeviceData != null && !jsonStringContactDeviceData.isEmpty()) {
			ContactDeviceDataList contactDeviceDataList = Utils.fillContactDeviceDataListFromJSON(jsonStringContactDeviceData);
			if(contactDeviceDataList != null){
				DBLayer.getInstance().addContactDeviceDataList(contactDeviceDataList);
			}
		}
		
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(mainActivity);
        String registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
        if (registrationId == null || registrationId.isEmpty()) {
        	logMessage = "Register to Google Cloud Message.";
        	LogManager.LogInfoMsg(className, methodName, logMessage);
    		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

        	String googleProjectNumber = Preferences.getPreferencesString(context, CommonConst.GOOGLE_PROJECT_NUMBER);
        	if(googleProjectNumber == null || googleProjectNumber.isEmpty()){
				logMessage = "Google Project Number is NULL or EMPTY";
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
        	}
        	ProgressDialog waitingDialog = InitAppUtils.launchWaitingDialog(mainActivity);
    	    waitingDialog.setOnDismissListener(new OnDismissListener(){
				@Override
				public void onDismiss(DialogInterface dialog) {
					Log.i(CommonConst.LOG_TAG, "waitingDialog - onDismiss() called");
					String regID = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
					InitAppUtils.initWithRegID(mainActivity, context, regID);
				}
			});
    	    
    		Thread registerToGCMInBackgroundThread;
    		Runnable registerToGCMInBackground = new RegisterToGCMInBackground(context, mainActivity, gcm, googleProjectNumber, waitingDialog);
			try {
				registerToGCMInBackgroundThread = new Thread(registerToGCMInBackground);
				registerToGCMInBackgroundThread.start();
				// Launch waiting dialog - till registration process will be completed or failed
			} catch (IllegalThreadStateException e) {
				logMessage = "Register to GCM in background thread was started already";
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
			}
						
        } else {
        	InitAppUtils.initWithRegID(mainActivity, context, registrationId);
        }
    
        LogManager.LogFunctionExit(className, methodName);
        Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
        
//		Intent intent = new Intent(mainActivity, MapActivity.class);
//		intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK );
//		context.startActivity(intent);
        return true; // Start MapActivity - the main application activity
    }
    
	private static void getCurrentAccount(final Activity mainActivity, final Context context){		
		// CURRENT ACCOUNT
		if( Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT) == null || 
			Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT).isEmpty() ) {
			
			List<String> accountList = InitAppUtils.getAccountList(context);
			if(accountList != null && accountList.size() == 1){
				String account = accountList.get(0);
				Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT, account);
				InitAppUtils.initCont(mainActivity, context);
			} else {
        		ChooseAccountDialog chooseAccountDialog =
        				new ChooseAccountDialog(mainActivity, new ICommonDialogOnClickListener(){
        			@Override
        			public void doOnChooseItem(int which) {
        				List<String> accountList = InitAppUtils.getAccountList(context);
        				String account = accountList.get(which);
        				Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT, account);
        				InitAppUtils.initCont(mainActivity, context);
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

	private static ProgressDialog launchWaitingDialog(Activity mainActivity) {
		ProgressDialog waitingDialog = new ProgressDialog(mainActivity);
	    waitingDialog.setTitle("Registration for Google Cloud Messaging");
	    waitingDialog.setMessage("Please wait ...");
	    waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    waitingDialog.setIndeterminate(true);
	    waitingDialog.show();
	    waitingDialog.setCanceledOnTouchOutside(false);
	    
	    Log.i(CommonConst.LOG_TAG, "waitingDialog - show()");
	    return waitingDialog;
	}
	
	private static void initWithRegID(Activity mainActivity, Context context, String registrationId){
		String className = "InitAppController";
    	String methodName = "initWithRegID";
		String logMessage;
		String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		// Insert into DB: owner information if doesn't exist
		ContactDeviceDataList contDevDataList = DBLayer.getInstance().getContactDeviceDataList(account);
		if( contDevDataList == null || contDevDataList.size() == 0){
			
			String macAddress = InitAppUtils.getMacAddress(mainActivity);
			String phoneNumber = InitAppUtils.getPhoneNumber(context);
			
			// add information about owner to DB 
			ContactDeviceDataList contactDeviceDataListOwner = 
				DBLayer.getInstance().addContactDeviceDataList(new ContactDeviceDataList(account,
					macAddress, phoneNumber, registrationId, null));
			if(contactDeviceDataListOwner == null){
				logMessage = "Failed to add owner information to application's DB";
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			} else {
				MessageDataContactDetails joinRequesterMessageDataContactDetails = 
						new MessageDataContactDetails(account, 
							macAddress, 
							phoneNumber, 
							registrationId, 
							0);
					
				// Broadcast message to update ContactList
				Controller.broadcsatMessage(context, 
					joinRequesterMessageDataContactDetails, 
					BroadcastActionEnum.BROADCAST_MESSAGE.toString(), 
					"Update Contacts List", 
					CommandKeyEnum.update_contact_list.toString(), 
					CommandValueEnum.update_contact_list.toString());
				logMessage = "Owner information inserted to application's DB";
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			}
		} else {
			logMessage = "Owner information already exists in application's DB";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

			String macAddress = InitAppUtils.getMacAddress(mainActivity);
			// FIXME: Update only if registration ID in DB is differs
			long res = DBLayer.getInstance().updateRegistrationID(account, macAddress, registrationId);
			if(res <= 0){
				logMessage = "Unable to update registration ID";
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.i(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			} else {
				logMessage = "Owner registration ID has been updated";
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			}
		}
		
		String phoneNumber = InitAppUtils.getPhoneNumber(context);
		String macAddress = InitAppUtils.getMacAddress(mainActivity);
		LogManager.LogInfoMsg(className, "SAVE OWNER INFO", 
				account + CommonConst.DELIMITER_COLON + macAddress + CommonConst.DELIMITER_COLON + 
				phoneNumber + CommonConst.DELIMITER_COLON + Controller.hideRealRegID(registrationId));
		
//		contDevDataList = DBLayer.getInstance().getContactDeviceDataList(account);
//		if(contDevDataList != null){
//			// get owner information from DB and save GUID to Preferences
//			for (ContactDeviceData cdd : contDevDataList.getContactDeviceDataList()) {
//				// Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_OWNER_GUID, cdd.getGuid());
//				Preferences.setPreferencesString(context, CommonConst.PREFERENCES_OWNER_GUID, cdd.getGuid());
//			}
//		} else {
//        	String errMsg = "Failed to save PREFERENCES_OWNER_GUID - no owner details were created";
//        	Log.e(CommonConst.LOG_TAG, errMsg);
//            LogManager.LogErrorMsg(className, "init", errMsg);
//		}
//		
// 		contactDeviceDataList = DBLayer.getInstance().getContactDeviceDataList(null);
		
        registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
        if (registrationId == null || registrationId.isEmpty()) {
        	String errorMessage = "\nFailed to register your application\nwith Google Cloud Message\n\n" + 
        		"Application will be closed\n\nPlease try later...\n\n";
        	// Show dialog with errorMessage and exit from application
        	// showNotificationDialog(errorMessage, "FINISH");
            Log.e(CommonConst.LOG_TAG, errorMessage);
    		LogManager.LogInfoMsg(className, "onCreate", 
    			errorMessage);
        }
	}

	/*
	 * Get account list used on this device - email list
	 */
	public static List<String> getAccountList(Context context){
	    Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
	    AccountManager accountManager = AccountManager.get(context); 
	    //Account[] accounts = manager.getAccountsByType("com.google"); 
	    Account[] accounts = accountManager.getAccounts();
	    List<String> possibleEmails = new ArrayList<String>();

	    for (Account account : accounts) {
	        if (emailPattern.matcher(account.name).matches() && !possibleEmails.contains(account.name) &&
	        		account.type.equals("com.google")) {
        		possibleEmails.add(account.name);
	        }
	    }

	    if(!possibleEmails.isEmpty()){
	    	return possibleEmails;
	    }else
	        return null;
	}

	public static String getMacAddress(Activity activity){
		WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String macAddress = wifiInfo.getMacAddress();
		return macAddress;
	}
	
	public static String getIMEI(Activity activity){
		TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
	    String imei = tm.getDeviceId();
	    return imei;
	}
	
	public static String getPhoneNumber(Context context) {
		TelephonyManager tMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String phoneNumber = tMgr.getLine1Number();
		return phoneNumber;
	}

}
