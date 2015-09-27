package com.dagrest.tracklocation;

import com.dagrest.tracklocation.controller.MainActivityController;
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
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.model.MainModel;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.google.gson.Gson;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final static int JOIN_REQUEST = 1;  
    protected String className;
    protected String logMessage;
    protected String methodName;
    protected Context context;
    protected MainActivityController mainActivityController;
    protected MainModel mainModel;
    private BroadcastReceiver notificationBroadcastReceiver;
    
    public static volatile boolean isTrackLocationRunning; // Used in SMSReceiver.class

    
    public MainActivityController getMainActivityController() {
		return mainActivityController;
	}

	@SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		className = this.getClass().getName();
		methodName = "onCreate";
		
		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);
		
		setContentView(R.layout.activity_main);
				
		initNotificationBroadcastReceiver();
		
		isTrackLocationRunning = true;
    }

	@Override
	protected void onResume() {
		super.onResume();
		context = getApplicationContext();
		DBManager.initDBManagerInstance(new DBHelper(context));
		if(mainModel == null){
			mainModel = new MainModel();
		}
		if(mainActivityController == null){
			mainActivityController = new MainActivityController(this, context);
		}
	}

    @Override
	protected void onPause() {
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
		
     	Thread registerToGCMInBackgroundThread = 
     		mainActivityController.getRegisterToGCMInBackgroundThread();
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
		
		LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_DESTROY] {" + className + "} -> " + methodName);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
    public void onClick(final View view) {
    	if (view == findViewById(R.id.btnLocate) || view == findViewById(R.id.btnLocationSharing) || view == findViewById(R.id.btnTracking) ){
    		mainModel.setContactDeviceDataList(DBLayer.getInstance().getContactDeviceDataList(null));
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
        	String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
        	if(account == null || account.isEmpty()){
    	    	Toast.makeText(MainActivity.this, "Please register your application.\nPress Locate button at first.", 
        	    		Toast.LENGTH_SHORT).show();
        		LogManager.LogErrorMsg(className, "onClick -> JOIN button", 
            		"Unable to join contacts - application is not registred yet.");
        	} else {
        		Intent joinContactListIntent = new Intent(this, JoinContactList.class);
        		startActivityForResult(joinContactListIntent, JOIN_REQUEST);
        	}

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
    		
    		if(mainModel.getContactDeviceDataList() != null){
	    		Intent intentContactList = new Intent(this, ContactList.class);
//	    		intentContactList.putExtra(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST, new Gson().toJson(contactDeviceDataList));
//	    		intentContactList.putExtra(CommonConst.PREFERENCES_PHONE_ACCOUNT, account);
//	    		intentContactList.putExtra(CommonConst.PREFERENCES_PHONE_MAC_ADDRESS, macAddress);
//	    		intentContactList.putExtra(CommonConst.PREFERENCES_PHONE_NUMBER, phoneNumber);
//	    		intentContactList.putExtra(CommonConst.PREFERENCES_REG_ID, mainActivityController.getRegistrationId());
	    		startActivity(intentContactList);
    		} else {
    	    	Toast.makeText(MainActivity.this, "There is no any contact.\nJoin some contact at first.", 
    	    		Toast.LENGTH_SHORT).show();
        		LogManager.LogInfoMsg(className, "onClick -> LOCATE button", 
                	"There is no any contact. Some contact must be joined at first.");
    		}
    	// ========================================
    	// LOCATION SHARING MANAGEMENT button
    	// ========================================
        } else if (view == findViewById(R.id.btnLocationSharing)) {
    		LogManager.LogInfoMsg(className, "onClick -> Location Sharing Management button", 
    			"ContactList activity started.");
    		
    		ContactDeviceDataList contactDeviceDataList = mainModel.getContactDeviceDataList();
    		if(contactDeviceDataList != null){
	    		Intent intentContactList = new Intent(this, LocationSharingList.class);
	    		intentContactList.putExtra(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST, 
	    			new Gson().toJson(contactDeviceDataList));
	    		startActivity(intentContactList);
    		} else {
    	    	Toast.makeText(MainActivity.this, "There is no any contact.\nJoin some contact at first.", 
    	    		Toast.LENGTH_SHORT).show();
        		LogManager.LogInfoMsg(className, "onClick -> LOCATION SHARING MANAGEMENT button", 
                    "There is no any contact. Some contact must be joined at first.");
    		}
    	// ========================================
    	// TRACKING button (TRACKING_CONTACT_LIST)
    	// ========================================
        } else if (view == findViewById(R.id.btnTracking)) {
    		LogManager.LogInfoMsg(className, "onClick -> Tracking button", 
    			"TrackingList activity started.");
    		
    		ContactDeviceDataList contactDeviceDataList = mainModel.getContactDeviceDataList();
    		if(contactDeviceDataList != null){
	    		Intent intentContactList = new Intent(this, TrackingList.class);
	    		intentContactList.putExtra(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST, 
	    			new Gson().toJson(contactDeviceDataList));
	    		startActivity(intentContactList);
    		} else {
    	    	Toast.makeText(MainActivity.this, "There is no any contact.\nJoin some contact at first.", 
    	    		Toast.LENGTH_SHORT).show();
        		LogManager.LogInfoMsg(className, "onClick -> TRACKING button", 
                    "There is no any contact. Some contact must be joined at first.");
    		}
        }
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
	    				showApproveJoinRequestDialog(broadcastData); // bring to foreground
	    			}
	    		}
			}
	    };
	    
	    registerReceiver(notificationBroadcastReceiver, intentFilter);
	    
		LogManager.LogFunctionExit(className, methodName);
	}

	private void showApproveJoinRequestDialog(NotificationBroadcastData broadcastData){
		methodName = "showApproveJoinRequestDialog";
		
		//  SMS Message values:
		//	[0] - smsMessageKey - "JOIN_TRACK_LOCATION"
		//	[1] - regIdFromSMS
		//	[2] - mutualIdFromSMS
		//	[3] - phoneNumberFromSMS
		//	[4] - accountFromSMS
		//	[5] - macAddressFromSMS
		String smsMessage = broadcastData.getValue();
		
//		List<String> listSmsVals = new ArrayList<String>();
//		StringTokenizer st = new StringTokenizer(smsMessage, ",");
//		while(st.hasMoreElements()){
//			listSmsVals.add(st.nextToken().trim());
//		}
		String smsVals[] = smsMessage.split(",");
		if(smsVals.length == CommonConst.JOIN_SMS_PARAMS_NUMBER){ // should be exactly 6 values
			//Controller.showApproveJoinRequestDialog(this, 
			mainActivityController.showApproveJoinRequestDialog(this,
				context, 
				smsVals[4].trim(), 	// accountFromSMS
				smsVals[3].trim(), 	// phoneNumberFromSMS
				smsVals[2].trim(), 	// mutualIdFromSMS 
				smsVals[1].trim(), 	// regIdFromSMS
				smsVals[5].trim(),	// macAddressFromSMS
				null
			);
		} else {
			logMessage = "JOIN SMS Message has incorrect parameters number" +
				" - supposed to be: " + CommonConst.JOIN_SMS_PARAMS_NUMBER;
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		
			logMessage = methodName + " -> Backup process failed.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
	}
	
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
    
	IDialogOnClickAction dialogActionsAboutDialog = new IDialogOnClickAction() {
		@Override
		public void doOnPositiveButton() {
		}
		@Override
		public void doOnNegativeButton() {
		}
		@Override
		public void setActivity(Activity activity) {
		}
		@Override
		public void setContext(Context context) {
		}
		@Override
		public void setParams(Object[]... objects) {
		}
		@Override
		public void doOnChooseItem(int which) {
		}
	};
	
}
