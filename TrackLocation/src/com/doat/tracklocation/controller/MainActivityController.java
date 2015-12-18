package com.doat.tracklocation.controller;

import java.util.List;
import java.util.UUID;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.R;
import com.doat.tracklocation.concurrent.RegisterToGCMInBackground;
import com.doat.tracklocation.datatype.AppInfo;
import com.doat.tracklocation.datatype.AppInstDetails;
import com.doat.tracklocation.datatype.BackupDataOperations;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.dialog.ChooseAccountDialog;
import com.doat.tracklocation.dialog.ICommonDialogNewOnClickListener;
import com.doat.tracklocation.dialog.InfoDialog;
import com.doat.tracklocation.exception.CheckPlayServicesException;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.model.MainModel;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;
import com.doat.tracklocation.utils.SMSUtils;
import com.doat.tracklocation.utils.Utils;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

public class MainActivityController {
	
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static int SLEEP_TIME = 500; // 0.5 sec
	private Activity mainActivity;
	private MainModel mainModel;

	private String className;
    private String logMessage;
    private String methodName;
	
    private GoogleCloudMessaging gcm;
    private Context context;
    private String googleProjectNumber;
    private String phoneNumber;
    private String macAddress;
    private List<String> accountList;
    private String account;
    private AppInstDetails appInstDetails;
    private String registrationId;
    private ProgressDialog waitingDialog;
    // private boolean isChooseAccountDialogOpened = false;
    private AppInfo preinstalledAppInfo;
    
	private Thread registerToGCMInBackgroundThread;
	private Runnable registerToGCMInBackground;
    
    public MainActivityController(Activity mainActivity, Context context){
    	className = this.getClass().getName();
    	methodName = "MainActivityController";
    	LogManager.LogFunctionCall(className, methodName);
    	Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

    	this.mainActivity = mainActivity;
    	this.context = context;
    	if(mainModel == null){
    		mainModel = new MainModel();
    	}
    	startMainActivityController();
    	
    	LogManager.LogFunctionExit(className, methodName);
    	Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
    }
    
    public void startMainActivityController(){
    	methodName = "startMainActivityController";
    	LogManager.LogFunctionCall(className, methodName);
    	Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		googleProjectNumber = mainActivity.getResources().getString(R.string.google_project_number);
		Preferences.setPreferencesString(context, CommonConst.GOOGLE_PROJECT_NUMBER, googleProjectNumber);

		BackupDataOperations backupData = new BackupDataOperations();
//		boolean isBackUpSuccess = backupData.backUp();
//		if(isBackUpSuccess != true){
//			logMessage = methodName + " -> Backup process failed.";
//			LogManager.LogErrorMsg(className, methodName, logMessage);
//			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
//		}
		boolean isBackUpRestoreSuccess = backupData.restore();
		if(isBackUpRestoreSuccess != true){
			logMessage = methodName + " -> Restore process failed.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
		
		// Version check
		preinstalledAppInfo = Controller.getAppInfo(context);
		
		// INIT START ===================================================
		account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		if( account == null || account.isEmpty() ){
			getCurrentAccount();
		} else {
			initCont();
		}
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
    }
    
    private void initCont(){
    	methodName = "initCont";
    	LogManager.LogFunctionCall(className, methodName);
    	Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		Controller.saveAppInfoToPreferences(context);
		
		// PHONE NUMBER
		phoneNumber = Controller.getPhoneNumber(context);
		//Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_PHONE_NUMBER, phoneNumber);
		if(phoneNumber == null || phoneNumber.isEmpty()){
			phoneNumber = UUID.randomUUID().toString().replace("-", "");
		}
		Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER, phoneNumber);
		
		// MAC ADDRESS
		macAddress = Controller.getMacAddress(mainActivity);
		//Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS, macAddress);
		Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS, macAddress);
		
		// INIT END ===================================================
		
		AppInfo installedAppInfo = Controller.getAppInfo(context);
				
		if(preinstalledAppInfo.getVersionNumber() < installedAppInfo.getVersionNumber()){
			// reset resistrationID
			Preferences.setPreferencesString(context, CommonConst.PREFERENCES_REG_ID, "");
			Preferences.setPreferencesString(context, CommonConst.APP_INST_DETAILS, "");
		}
		
		// Create application details during first installation 
		// if was created already just returns the details:
		//    - First installation's timestamp 
		//    - ApInfo: version number and version name
		appInstDetails = new AppInstDetails(context); 
		
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
		
        gcm = GoogleCloudMessaging.getInstance(mainActivity);
        registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
        if (registrationId == null || registrationId.isEmpty()) {
        	logMessage = "Register to Google Cloud Message.";
        	LogManager.LogInfoMsg(className, methodName, logMessage);
    		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

        	googleProjectNumber = Preferences.getPreferencesString(context, CommonConst.GOOGLE_PROJECT_NUMBER);
        	if(googleProjectNumber == null || googleProjectNumber.isEmpty()){
				logMessage = "Google Project Number is NULL or EMPTY";
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
        	}
        	registerToGCMInBackground = new RegisterToGCMInBackground(context, gcm, googleProjectNumber);
			try {
				registerToGCMInBackgroundThread = new Thread(registerToGCMInBackground);
				registerToGCMInBackgroundThread.start();
				// Launch waiting dialog - till registration process will be completed or failed
				launchWaitingDialog();
			} catch (IllegalThreadStateException e) {
				logMessage = "Register to GCM in background thread was started already";
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
			}
						
        } else {
        	initWithRegID(registrationId);
        }
    
		// Controller.checkJoinRequestBySMS(new Object[] {context, MainActivity.this}); 
        SMSUtils.checkJoinRequestBySMSInBackground(context, mainActivity);
 
        LogManager.LogFunctionExit(className, methodName);
        Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
    }
    
	private void getCurrentAccount(){
		// CURRENT ACCOUNT
		if( Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT) == null || 
			Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT).isEmpty() ) {
			
			accountList = Controller.getAccountList(context);
			if(accountList != null && accountList.size() == 1){
				account = accountList.get(0);
				Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT, account);
				initCont();
			} else {
        		ChooseAccountDialog chooseAccountDialog =
        				new ChooseAccountDialog(mainActivity, new ICommonDialogNewOnClickListener(){
        			@Override
        			public void doOnChooseItem(int which) {
        				account = accountList.get(which);
        				Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT, account);
        				initCont();
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

	private void launchWaitingDialog() {
		final int MAX_RETRY_TIMES = 5;
		
        waitingDialog = new ProgressDialog(mainActivity);
        waitingDialog.setTitle("Registration for Google Cloud Messaging");
        waitingDialog.setMessage("Please wait ...");
        waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //progressDialog.setProgress(0);
        //progressDialog.setMax(contactsQuantity);
        //waitingDialog.setCancelable(false);
        waitingDialog.setIndeterminate(true);
        waitingDialog.show();
        waitingDialog.setCanceledOnTouchOutside(false);
        
        new Thread(new Runnable() {
            @Override
            public void run() {
            	String regID = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
            	logMessage = "MainActivity BEFORE while in wainting dialog... regId = [" + Controller.hideRealRegID(regID) + "]";
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
				int retryTimes = 0;
            	while((regID == null || regID.isEmpty()) && retryTimes < MAX_RETRY_TIMES){
                	try {
    					Thread.sleep(SLEEP_TIME); 
    				} catch (InterruptedException e) {
    					waitingDialog.dismiss();
    					break;
    				}
                	regID = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
                	logMessage = "MainActivity INSIDE while in wainting dialog... regId = [" + Controller.hideRealRegID(regID) + "]";
    				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
    				if(regID != null && !regID.isEmpty()){
    					waitingDialog.dismiss();
    					break;
    				}
    				retryTimes++;
            	}

            	registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
            	if(registrationId != null & !registrationId.isEmpty()){
            		initWithRegID(regID);
            	} else {
            		if(registrationId == null || !registrationId.isEmpty()){
            			String errorMessage = "\nGoogle Cloud Service is not available right now.\n\n"
            				+ "Application will be closed.\n\nPlease try later.\n";
//            			showNotificationDialog(errorMessage, "FINISH");
                		String title = "Warning";
                		new InfoDialog(mainActivity, context, title, errorMessage, null);
            		}
            	}
            }
        }).start();
   
	}

	private void initWithRegID(String registrationId){
		
		// Insert into DB: owner information if doesn't exist
		ContactDeviceDataList contDevDataList = DBLayer.getInstance().getContactDeviceDataList(account);
		if( contDevDataList == null || contDevDataList.size() == 0){
			// add information about owner to DB 
			ContactDeviceDataList contactDeviceDataListOwner = 
				DBLayer.getInstance().addContactDeviceDataList(new ContactDeviceDataList(account,
					macAddress, phoneNumber, registrationId, null));
			if(contactDeviceDataListOwner == null){
				logMessage = "Failed to add owner information to application's DB";
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			}
		} else {
			logMessage = "Owner information already exists in application's DB";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
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

	public String getRegistrationId() {
		return registrationId;
	}

	public Thread getRegisterToGCMInBackgroundThread() {
		return registerToGCMInBackgroundThread;
	}
}
