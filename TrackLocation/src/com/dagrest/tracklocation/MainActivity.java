package com.dagrest.tracklocation;

import java.util.List;
import java.util.UUID;

import com.dagrest.tracklocation.concurrent.CheckJoinRequestBySMS;
import com.dagrest.tracklocation.concurrent.RegisterToGCMInBackground;
import com.dagrest.tracklocation.datatype.AppInfo;
import com.dagrest.tracklocation.datatype.AppInstDetails;
import com.dagrest.tracklocation.datatype.BackupDataOperations;
import com.dagrest.tracklocation.datatype.BroadcastActionEnum;
import com.dagrest.tracklocation.datatype.BroadcastConstEnum;
import com.dagrest.tracklocation.datatype.BroadcastKeyEnum;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.NotificationBroadcastData;
import com.dagrest.tracklocation.db.DBHelper;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.db.DBManager;
import com.dagrest.tracklocation.dialog.CommonDialog;
import com.dagrest.tracklocation.dialog.IDialogOnClickAction;
import com.dagrest.tracklocation.exception.CheckPlayServicesException;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.service.GcmIntentService;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.dagrest.tracklocation.utils.Utils;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static int SLEEP_TIME = 500; // 0.5 sec
    private final static int JOIN_REQUEST = 1;  
    private BroadcastReceiver locationChangeWatcher;
    private String googleProjectNumber;
    private String className;
    private String registrationId;
    private GoogleCloudMessaging gcm;
    private Context context;
	private Thread registerToGCMInBackgroundThread;
	private Runnable registerToGCMInBackground;
	private Thread checkJoinRequestBySMSInBackgroundThread;
	private Runnable checkJoinRequestBySMSInBackground;
    private ContactDeviceDataList contactDeviceDataList;
    private String phoneNumber;
    private String macAddress;
    private List<String> accountList;
    private String account;
    private AppInstDetails appInstDetails;
    private String logMessage;
    private String methodName;
    private ProgressDialog waitingDialog;
    private BroadcastReceiver notificationBroadcastReceiver;
    public static boolean isTrackLocationRunning;
    private boolean isBringToTopRequested = false;
    
    @SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		className = this.getClass().getName();
		methodName = "onCreate";
		
		Intent i = getIntent();
		isBringToTopRequested = false;
		Bundle b = null;
		if(i != null){
			b = i.getExtras();
		}
		if(b != null){
			isBringToTopRequested = b.getBoolean(CommonConst.IS_BRING_TO_TOP);
		}
		
		// Ensure that only one instance of TrackLocation is running
		if(isTrackLocationRunning == true && isBringToTopRequested == false){
			logMessage = "Only one instance of TrackLocation can be started.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			return;
		}
		 
		context = getApplicationContext();
		DBManager.initDBManagerInstance(new DBHelper(context));
		googleProjectNumber = this.getResources().getString(R.string.google_project_number);
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
		AppInfo preinstalledAppInfo = Controller.getAppInfo(context);
		// Inside init() - application version is updated
		init();		
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
		
		setContentView(R.layout.activity_main);
				
		// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
		try {
			Controller.checkPlayServices(context);
		} catch (CheckPlayServicesException e) {
			String errorMessage = e.getMessage();
			if(CommonConst.PLAYSERVICES_ERROR.equals(errorMessage)){
	            GooglePlayServicesUtil.getErrorDialog(e.getResultCode(), this,
	            	PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else if(CommonConst.PLAYSERVICES_DEVICE_NOT_SUPPORTED.equals(errorMessage)){
				// Show dialog with errorMessage and exit from application 
				showNotificationDialog("\nGoogle Play Services not supported with this device.\nProgram will be closed.\n", "FINISH");
			}
            Log.e(CommonConst.LOG_TAG, "No valid Google Play Services APK found.");
    		LogManager.LogInfoMsg(className, methodName, 
    			"No valid Google Play Services APK found.");
			//finish();
		}
		
		initNotificationBroadcastReceiver();

		// ===============================================================
		// READ CONTACT AND DEVICE DATA FROM JSON FILE AND INSERT IT TO DB
		// ===============================================================
		// Read contact and device data from json file and insert it to DB
		String jsonStringContactDeviceData = Utils.getContactDeviceDataFromJsonFile();
		if(jsonStringContactDeviceData != null && !jsonStringContactDeviceData.isEmpty()) {
			ContactDeviceDataList contactDeviceDataList = Utils.fillContactDeviceDataListFromJSON(jsonStringContactDeviceData);
			if(contactDeviceDataList != null){
				DBLayer.addContactDeviceDataList(contactDeviceDataList);
			}
		}
		
        gcm = GoogleCloudMessaging.getInstance(this);
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
    
// 		IMPORTANT: Preparation for tracking option - start tracking autostarter service        
//		Intent trackingService = new Intent(context, TrackingAutostarter.class);
//		ComponentName componentName = context.startService(trackingService); 
//		if(componentName != null){
//			logMessage = "TrackingAutostarter is STARTED";
//			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
//		} else {
//			logMessage = "TrackingAutostarter FAILED TO START";
//			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
//		}

       
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onStart() {		

		isTrackLocationRunning = true;
		
		// Controller.checkJoinRequestBySMS(new Object[] {context, MainActivity.this}); 
		checkJoinRequestBySMSInBackground = new CheckJoinRequestBySMS(context, MainActivity.this);
		try {
			checkJoinRequestBySMSInBackgroundThread = new Thread(checkJoinRequestBySMSInBackground);
			checkJoinRequestBySMSInBackgroundThread.start();
		} catch (IllegalThreadStateException e) {
			logMessage = "Check Join request by SMS in background thread was started already";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
		}

		super.onResume();
	}
		
    public void onClick(final View view) {
    	if (view == findViewById(R.id.btnLocate) || view == findViewById(R.id.btnLocationSharing) || view == findViewById(R.id.btnTracking) ){
    		contactDeviceDataList = DBLayer.getContactDeviceDataList(null);
    	}
    	
    	// ========================================
    	// ABOUT button
    	// ========================================
        if (view == findViewById(R.id.btnAbout)) {        	
        	showAboutDialog();
        	
    	// ========================================
    	// JOIN button
    	// ========================================
        } else if (view == findViewById(R.id.btnJoin)) {
    		Intent joinContactListIntent = new Intent(this, JoinContactList.class);
    		startActivityForResult(joinContactListIntent, JOIN_REQUEST);

// 			*********************************************************************************        	
//		    // Start an activity for the user to pick a phone number from contacts
//		    Intent intent = new Intent(Intent.ACTION_PICK);
//		    intent.setType(CommonDataKinds.Phone.CONTENT_TYPE);
//		    if (intent.resolveActivity(getPackageManager()) != null) {
//		        startActivityForResult(intent, CommonConst.REQUEST_SELECT_PHONE_NUMBER);
//		    }
// 			*********************************************************************************        	
			
    	// ========================================
    	// SETTINGS button
    	// ========================================
        } else if (view == findViewById(R.id.btnSettings)) {	
    		Intent settingsIntent = new Intent(this, SettingsActivity.class);
    		startActivity(settingsIntent);

    	// ========================================
    	// LOCATE button (CONTACT_LIST)
    	// ========================================
        } else if (view == findViewById(R.id.btnLocate)) {
    		LogManager.LogInfoMsg(className, "onClick -> Locate button", 
    			"ContactList activity started.");
    		
    		if(contactDeviceDataList != null){
	    		Intent intentContactList = new Intent(this, ContactList.class);
	    		intentContactList.putExtra(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST, new Gson().toJson(contactDeviceDataList));
	    		intentContactList.putExtra(CommonConst.PREFERENCES_PHONE_ACCOUNT, account);
	    		intentContactList.putExtra(CommonConst.PREFERENCES_PHONE_MAC_ADDRESS, macAddress);
	    		intentContactList.putExtra(CommonConst.PREFERENCES_PHONE_NUMBER, phoneNumber);
	    		intentContactList.putExtra(CommonConst.PREFERENCES_REG_ID, registrationId);
	    		startActivity(intentContactList);
    		} else {
    	    	Toast.makeText(MainActivity.this, "There is no any contact.\nJoin some contact at first.", 
    	    		Toast.LENGTH_SHORT).show();
    			// TODO: to log - no joined contacts
    		}
    	// ========================================
    	// LOCATION SHARING MANAGEMENT button
    	// ========================================
        } else if (view == findViewById(R.id.btnLocationSharing)) {
    		LogManager.LogInfoMsg(className, "onClick -> Location Sharing Management button", 
    			"ContactList activity started.");
    		
    		if(contactDeviceDataList != null){
	    		Intent intentContactList = new Intent(this, LocationSharingList.class);
	    		intentContactList.putExtra(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST, 
	    			new Gson().toJson(contactDeviceDataList));
	    		startActivity(intentContactList);
    		} else {
    	    	Toast.makeText(MainActivity.this, "There is no any contact.\nJoin some contact at first.", 
    	    		Toast.LENGTH_SHORT).show();
    			// TODO: to log - no joined contacts
    		}
    	// ========================================
    	// TRACKING button (TRACKING_CONTACT_LIST)
    	// ========================================
        } else if (view == findViewById(R.id.btnTracking)) {
    		LogManager.LogInfoMsg(className, "onClick -> Tracking button", 
    			"TrackingList activity started.");
    		
    		if(contactDeviceDataList != null){
	    		Intent intentContactList = new Intent(this, TrackingList.class);
	    		intentContactList.putExtra(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST, 
	    			new Gson().toJson(contactDeviceDataList));
	    		startActivity(intentContactList);
    		} else {
    	    	Toast.makeText(MainActivity.this, "There is no any contact.\nJoin some contact at first.", 
    	    		Toast.LENGTH_SHORT).show();
    			// TODO: to log - no joined contacts
    		}
        }
    }
    
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		methodName = "onPause";

		BackupDataOperations backupData = new BackupDataOperations();
		boolean isBackUpSuccess = backupData.backUp();
		if(isBackUpSuccess != true){
			logMessage = methodName + " -> Backup process failed.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + methodName + ": " + logMessage);
		}
    }

	@Override
    protected void onDestroy() {
        super.onDestroy();
        methodName = "onDestroy";
        
		isTrackLocationRunning = false;
		
        if(locationChangeWatcher != null){
        	unregisterReceiver(locationChangeWatcher);
        }
        
        if(registerToGCMInBackgroundThread != null){
        	registerToGCMInBackgroundThread.interrupt();
        }
        
    	if(notificationBroadcastReceiver != null){
    		unregisterReceiver(notificationBroadcastReceiver);
    	}
    	
		BackupDataOperations backupData = new BackupDataOperations();
		boolean isBackUpSuccess = backupData.backUp();
		if(isBackUpSuccess != true){
			logMessage = methodName + " -> Backup process failed.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}

    }

	private void getCurrentAccount(){
		// CURRENT ACCOUNT
		if( Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT) == null || 
			Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT).isEmpty() ) {
			
			accountList = Controller.getAccountList(context);
			if(accountList != null && accountList.size() == 1){
				account = accountList.get(0);
				//Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_PHONE_ACCOUNT, account);
				Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT, account);
			} else {
				showChooseAccountDialog();
			}
		}
	}
	
	private void init(){
		account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		if( account == null || account.isEmpty() ){
			getCurrentAccount();
		}
		
		Controller.saveAppInfoToPreferences(context);
		
		// PHONE NUMBER
		phoneNumber = Controller.getPhoneNumber(context);
		//Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_PHONE_NUMBER, phoneNumber);
		if(phoneNumber == null || phoneNumber.isEmpty()){
			phoneNumber = UUID.randomUUID().toString().replace("-", "");
		}
		Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER, phoneNumber);
		
		// MAC ADDRESS
		macAddress = Controller.getMacAddress(MainActivity.this);
		//Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS, macAddress);
		Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS, macAddress);
		
		// ===============================================================
		// READ CONTACT AND DEVICE DATA FROM JSON FILE AND INSERT IT TO DB
		// ===============================================================
		// Read contact and device data from json file and insert it to DB
		String jsonStringContactDeviceData = Utils.getContactDeviceDataFromJsonFile();
		if(jsonStringContactDeviceData != null && !jsonStringContactDeviceData.isEmpty()) {
			ContactDeviceDataList contactDeviceDataList = Utils.fillContactDeviceDataListFromJSON(jsonStringContactDeviceData);
			if(contactDeviceDataList != null){
				DBLayer.addContactDeviceDataList(contactDeviceDataList);
			}
		}
	}
	
	private void initWithRegID(String registrationId){
		
		// Insert into DB: owner information if doesn't exist
		ContactDeviceDataList contDevDataList = DBLayer.getContactDeviceDataList(account);
		if( contDevDataList == null || contDevDataList.getContactDeviceDataList().size() == 0){
			// add information about owner to DB 
			ContactDeviceDataList contactDeviceDataListOwner = 
				DBLayer.addContactDeviceDataList(new ContactDeviceDataList(account,
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
		}
		
		LogManager.LogInfoMsg(className, "SAVE OWNER INFO", 
				account + CommonConst.DELIMITER_COLON + macAddress + CommonConst.DELIMITER_COLON + 
				phoneNumber + CommonConst.DELIMITER_COLON + Controller.hideRealRegID(registrationId));
		
//		contDevDataList = DBLayer.getContactDeviceDataList(account);
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
// 		contactDeviceDataList = DBLayer.getContactDeviceDataList(null);
		
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
	
	IDialogOnClickAction dialogActionsAboutDialog = new IDialogOnClickAction() {
		@Override
		public void doOnPositiveButton() {
		}
		@Override
		public void doOnNegativeButton() {
		}
		@Override
		public void setActivity(Activity activity) {
			// TODO Auto-generated method stub
		}
		@Override
		public void setContext(Context context) {
			// TODO Auto-generated method stub
		}
		@Override
		public void setParams(Object[]... objects) {
			// TODO Auto-generated method stub
		}
		@Override
		public void doOnChooseItem(int which) {
			// TODO Auto-generated method stub
		}
	};
	
	IDialogOnClickAction dialogChooseAccountDialog = new IDialogOnClickAction() {
		@Override
		public void doOnPositiveButton() {
		}
		@Override
		public void doOnNegativeButton() {
		}
		@Override
		public void setActivity(Activity activity) {
			// TODO Auto-generated method stub
		}
		@Override
		public void setContext(Context context) {
			// TODO Auto-generated method stub
		}
		@Override
		public void setParams(Object[]... objects) {
			// TODO Auto-generated method stub
		}
		@Override
		public void doOnChooseItem(int which) {
			account = accountList.get(which);
			Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT, account);
			//init();
		}
	};

	private void showAboutDialog() {
    	String dialogMessage = 
    		String.format(getResources().getString(R.string.about_dialog_text), 
    			Preferences.getPreferencesString(context, CommonConst.PREFERENCES_VERSION_NAME));
    	
		CommonDialog aboutDialog = new CommonDialog(this, dialogActionsAboutDialog);
		aboutDialog.setDialogMessage(dialogMessage);
		aboutDialog.setDialogTitle("About");
		aboutDialog.setPositiveButtonText("OK");
		aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
		aboutDialog.showDialog();
		aboutDialog.setCancelable(true);
    }
    
    private void showChooseAccountDialog() {
		CommonDialog aboutDialog = new CommonDialog(this, dialogChooseAccountDialog);
		aboutDialog.setDialogTitle("Choose current account:");
		aboutDialog.setItemsList(accountList.toArray(new String[0]));

		aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
		aboutDialog.showDialog();
		aboutDialog.setCancelable(true);
    }
    
	IDialogOnClickAction notificationDialogOnClickAction = new IDialogOnClickAction() {
		
		boolean isExit = false;
		
		@Override
		public void doOnPositiveButton() {
			if(isExit == true){
				finish();
			}
		}
		@Override
		public void doOnNegativeButton() {
			// TODO Auto-generated method stub
		}
		@Override
		public void setActivity(Activity activity) {
			// TODO Auto-generated method stub
		}
		@Override
		public void setContext(Context context) {
			// TODO Auto-generated method stub
		}
		@Override
		public void setParams(Object[]... objects) {
			if(objects[0][0] instanceof Boolean){
				isExit = (Boolean) objects[0][0];
			}
		}
		@Override
		public void doOnChooseItem(int which) {
			// TODO Auto-generated method stub
			
		}
	};
	
    //private void showGoogleServiceNotAvailable(String errorMessage) {
	private void showNotificationDialog(String errorMessage, String action) {
    	//String dialogMessage = "\nGoogle Cloud Service is not available right now.\n\nPlease try later.\n";
    	String dialogMessage = errorMessage;
    	
    	if(action != null && "FINISH".equals(action)){
    		notificationDialogOnClickAction.setParams(new Object[] {true});
    	}
		CommonDialog aboutDialog = new CommonDialog(this, notificationDialogOnClickAction);
		aboutDialog.setDialogMessage(dialogMessage);
		aboutDialog.setDialogTitle("Warning");
		aboutDialog.setPositiveButtonText("OK");
		aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
		aboutDialog.showDialog();
		aboutDialog.setCancelable(true);
    }
    
	private void launchWaitingDialog() {
		final int MAX_RETRY_TIMES = 5;
		
        waitingDialog = new ProgressDialog(this);
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
            			showNotificationDialog(errorMessage, "FINISH");
            		}
            	}
            }
        }).start();
        
	}
	
	// Initialize BROADCAST_MESSAGE broadcast receiver
	private void initNotificationBroadcastReceiver() {
		String methodName = "initNotificationBroadcastReceiver";
		LogManager.LogFunctionCall(className, methodName);
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(BroadcastActionEnum.BROADCAST_MESSAGE.toString());
	    notificationBroadcastReceiver = new BroadcastReceiver() {

	    	Gson gson = new Gson();
	    	
			@Override
			public void onReceive(Context context, Intent intent) {
				// String methodName = "onReceive";
				Bundle bundle = intent.getExtras();
	    		if(bundle != null && bundle.containsKey(BroadcastConstEnum.data.toString())){
	    			String jsonNotificationData = bundle.getString(BroadcastConstEnum.data.toString());
	    			if(jsonNotificationData == null || jsonNotificationData.isEmpty()){
	    				return;
	    			}
	    			NotificationBroadcastData broadcastData = gson.fromJson(jsonNotificationData, NotificationBroadcastData.class);
	    			if(broadcastData == null){
	    				return;
	    			}
	    			
	    			String key  = broadcastData.getKey();
	    			
    				// Notification about command: bring to top - to foreground
	    			// bring MainActivity to foreground
	    			if(BroadcastKeyEnum.join_sms.toString().equals(key)) {
	    				bringToTop(); // bring to foreground
	    			}
	    		}
			}
	    };
	    
	    registerReceiver(notificationBroadcastReceiver, intentFilter);
	    
		LogManager.LogFunctionExit(className, methodName);
	}

	private void bringToTop(){
		Intent a = new Intent(this, MainActivity.class);
		a.putExtra(CommonConst.IS_BRING_TO_TOP, true);
        a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(a);
	}
	
//	private void runDataBackup(BackupDataOperations backupData){
//		try {
//			boolean isBackUpSuccess = backupData.backUp();
//			if(isBackUpSuccess != true){
//				logMessage = "Backup process failed.";
//				LogManager.LogErrorMsg(className, methodName, logMessage);
//				Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
//			}
//		} catch (IOException e) {
//			logMessage = "Backup process failed.";
//			LogManager.LogException(e, className, methodName);
//			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
//		}
//	}
//	
//	private void runDataRestore(BackupDataOperations backupData){
//		try {
//			boolean isBackUpRestoreSuccess = backupData.restore();
//			if(isBackUpRestoreSuccess != true){
//				logMessage = "Restore process failed.";
//				LogManager.LogErrorMsg(className, methodName, logMessage);
//				Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
//			}
//		} catch (IOException e) {
//			logMessage = "Restore process failed.";
//			LogManager.LogException(e, className, methodName);
//			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
//		}
//	}

}
