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
    private BroadcastReceiver locationChangeWatcher;
    private String className;
    private String registrationId;
    private Context context;
	private Thread registerToGCMInBackgroundThread;
    private ContactDeviceDataList contactDeviceDataList;
    private String phoneNumber;
    private String macAddress;
    private String account;
    private String logMessage;
    private String methodName;
    private BroadcastReceiver notificationBroadcastReceiver;
    public static boolean isTrackLocationRunning;
    private boolean isBringToTopRequested = false;
    
    MainActivityController mainActivityController;
    
    @SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		className = this.getClass().getName();
		methodName = "onCreate";
		
		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);
		
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
		 
		setContentView(R.layout.activity_main);
		
		initNotificationBroadcastReceiver();
		
		isTrackLocationRunning = true;

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
		context = getApplicationContext();
		DBManager.initDBManagerInstance(new DBHelper(context));
	}
	
    public void onClick(final View view) {
    	if (view == findViewById(R.id.btnLocate) || view == findViewById(R.id.btnLocationSharing) || view == findViewById(R.id.btnTracking) ){
    		contactDeviceDataList = DBLayer.getInstance().getContactDeviceDataList(null);
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
        			// TODO: to log - no joined contacts
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
    		
    		MainActivityController mainActivityController = new MainActivityController(this, context);
    		mainActivityController.start();
    		
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
		
		LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_DESTROY] {" + className + "} -> " + methodName);
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
	
}
