package com.dagrest.tracklocation;

import java.util.HashMap;
import java.util.List;

import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.db.DBHelper;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.db.DBManager;
import com.dagrest.tracklocation.dialog.CommonDialog;
import com.dagrest.tracklocation.dialog.IDialogOnClickAction;
import com.dagrest.tracklocation.exception.CheckPlayServicesException;
import com.dagrest.tracklocation.grid.ContactDataGridView;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.dagrest.tracklocation.utils.Utils;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static int JOIN_REQUEST = 1;  
    private BroadcastReceiver locationChangeWatcher;
    private String className;
    private String registrationId;
    private GoogleCloudMessaging gcm;
    private Context context;
    private ContactDeviceDataList contactDeviceDataList;
    private String phoneNumber;
    private String macAddress;
    private List<String> accountList;
    private String account;
    
    @SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		className = this.getClass().getName();
				
		// ======================================================================
		// Checking for all possible Internet providers
		// ======================================================================
		if(Controller.isConnectingToInternet(
				(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)) == false){
			String message = "\nYour device is not connected to internet\n\n" + 
				"The application will be closed\n\n";
			showNotificationDialog(message, "FINISH");
		}
		
		setContentView(R.layout.activity_main);
		
		context = getApplicationContext();
		
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
    		LogManager.LogInfoMsg(this.getClass().getName(), "onCreate", 
    			"No valid Google Play Services APK found.");
			//finish();
		}
		
        gcm = GoogleCloudMessaging.getInstance(this);
        registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
        if (registrationId == null || registrationId.isEmpty()) {
            // registerInBackground();
        	HashMap<String, Object> map = new HashMap<String, Object>();
        	map.put("GoogleCloudMessaging", gcm);
        	map.put("GoogleProjectNumber", getResources().getString(R.string.google_project_number));
        	map.put("Context", context);
        	Controller.registerInBackground(map);
        }
        
        account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
        if( account == null || account.isEmpty() ){
        	getCurrentAccount();
        	if( account != null && !account.isEmpty() ){
        		init();
        	} 
        } else {
        	init();
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onStart() {
		Controller.checkJoinRequestBySMS(new Object[] {context, MainActivity.this}); 
		super.onResume();
	}
		
    public void onClick(final View view) {

    	contactDeviceDataList = DBLayer.getContactDeviceDataList(null);
    	
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

		
        	Intent contactDataGridViewIntent = new Intent(this, ContactDataGridView.class);
    		startActivity(contactDataGridViewIntent);

    	// ========================================
    	// LOCATE button (CONTACT_LIST)
    	// ========================================
        } else if (view == findViewById(R.id.btnLocate)) {
    		LogManager.LogInfoMsg(this.getClass().getName(), "onClick -> Locate button", 
    			"ContactList activity started.");
    		
    		if(contactDeviceDataList != null){
	    		Intent intentContactList = new Intent(this, ContactList.class);
	    		intentContactList.putExtra(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST, 
	    			new Gson().toJson(contactDeviceDataList));
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
    	// LOCATION SHARING MANAGEMNT button
    	// ========================================
        } else if (view == findViewById(R.id.btnLocationSharing)) {
    		LogManager.LogInfoMsg(this.getClass().getName(), "onClick -> Location Sharing Management button", 
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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if(locationChangeWatcher != null){
        	unregisterReceiver(locationChangeWatcher);
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
		
		Controller.setAppInfo(context);
		
		// PHONE NUMBER
		phoneNumber = Controller.getPhoneNumber(context);
		//Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_PHONE_NUMBER, phoneNumber);
		Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER, phoneNumber);
		
		// MAC ADDRESS
		macAddress = Controller.getMacAddress(MainActivity.this);
		//Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS, macAddress);
		Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS, macAddress);
		
		DBManager.initDBManagerInstance(new DBHelper(context));
		
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
		
		// Insert into DB: owner information if doesn't exist
		ContactDeviceDataList contDevDataList = DBLayer.getContactDeviceDataList(account);
		if( contDevDataList == null || contDevDataList.getContactDeviceDataList().size() == 0){
			// add information about owner to DB 
			ContactDeviceDataList contactDeviceDataListOwner = 
				DBLayer.addContactDeviceDataList(new ContactDeviceDataList(account,
					macAddress, phoneNumber, registrationId, null));
		} else {
			Log.i(CommonConst.LOG_TAG, "Owner information already exists");
		}
		LogManager.LogInfoMsg(CommonConst.LOG_TAG, "SAVE OWNER INFO", 
				account + CommonConst.DELIMITER_COLON + macAddress + CommonConst.DELIMITER_COLON + 
				phoneNumber + CommonConst.DELIMITER_COLON + registrationId);
		
		contDevDataList = DBLayer.getContactDeviceDataList(account);
		if(contDevDataList != null){
			// get owner information from DB and save GUID to Preferences
			for (ContactDeviceData cdd : contDevDataList.getContactDeviceDataList()) {
				// Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_OWNER_GUID, cdd.getGuid());
				Preferences.setPreferencesString(context, CommonConst.PREFERENCES_OWNER_GUID, cdd.getGuid());
			}
		} else {
        	String errMsg = "Failed to save PREFERENCES_OWNER_GUID - no owner details were created";
        	Log.e(CommonConst.LOG_TAG, errMsg);
            LogManager.LogErrorMsg(className, "init", errMsg);
		}
		
		contactDeviceDataList = DBLayer.getContactDeviceDataList(null);
		
        registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
        if (registrationId.isEmpty()) {
        	String errorMessage = "\nFailed to register your application\nwith Google Cloud Message\n\n" + 
        		"Application will be closed\n\nPlease try later...\n\n";
        	// Show dialog with errorMessage and exit from application
        	showNotificationDialog(errorMessage, "FINISH");
            Log.e(CommonConst.LOG_TAG, errorMessage);
    		LogManager.LogInfoMsg(this.getClass().getName(), "onCreate", 
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
			init();
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
    
}
