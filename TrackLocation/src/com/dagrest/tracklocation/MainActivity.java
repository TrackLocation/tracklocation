package com.dagrest.tracklocation;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.DeviceData;
import com.dagrest.tracklocation.datatype.DeviceTypeEnum;
import com.dagrest.tracklocation.datatype.SMSMessage;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.dagrest.tracklocation.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver locationChangeWatcher;
    private String className;
    private String registrationId;
    private GoogleCloudMessaging gcm;
    private Context context;
    private ContactDeviceDataList contactDeviceDataList;
    private String phoneNumber;
    private String macAddress;
    private String account;
    private SQLiteDatabase db;

    @SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		className = this.getClass().getName();
		
		setContentView(R.layout.activity_main);
		
		context = getApplicationContext();

		// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);

            if (registrationId.isEmpty()) {
                registerInBackground();
            }
            
//    		SmsManager smsManager = SmsManager.getDefault();
//    		regid = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
//    		smsManager.sendTextMessage("+972544504619", null, "\"" + regid + "\"", null, null);

//			// Send SMS message (multipart text message)            	
//            regid = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
//            
//            SmsManager smsManager = SmsManager.getDefault();
//            ArrayList<String> parts = smsManager.divideMessage("GUID" + "," + "dagrest@gmail.com" + "," + regid);
//            smsManager.sendMultipartTextMessage("+972544504619", null, parts, null, null);     		
    		
//            // Read SMS messages from inbox
//            List<SMSMessage> smsList= controller.fetchInboxSms(MainActivity.this, 1);
//            String s = smsList.get(0).messageContent;
//            System.out.println(smsList.get(0).messageContent);
    		
        } else {
            Log.e(CommonConst.LOG_TAG, "No valid Google Play Services APK found.");
    		LogManager.LogInfoMsg(this.getClass().getName(), "onCreate", 
    			"No valid Google Play Services APK found.");
        }

		DBLayer.init(context);
        init();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onStart() {
		super.onResume();
	}
		
	
	private void init(){
		// CURRENT ACCOUNT
		List<String> accountList = Controller.getAccountList(context);
		if(accountList != null && accountList.size() == 1){
			account = accountList.get(0);
			Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_PHONE_ACCOUNT, account);
		} else {
			// TODO: ask phone number from customer
			Toast.makeText(MainActivity.this,
					"Detected more that one ACCOUNT!",
					Toast.LENGTH_LONG).show();
			finish();
		}

		// PHONE NUMBER
		phoneNumber = Controller.getPhoneNumber(context);
		Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_PHONE_NUMBER, phoneNumber);
		
		// MAC ADDRESS
		macAddress = Controller.getMacAddress(MainActivity.this);
		Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS, phoneNumber);
		
		// Read contact and device data from json file and insert it to DB
		String jsonStringContactDeviceData = Utils.getContactDeviceDataFromJsonFile();
		if(jsonStringContactDeviceData != null && !jsonStringContactDeviceData.isEmpty()) {
			ContactDeviceDataList contactDeviceDataList = Utils.fillContactDeviceDataListFromJSON(jsonStringContactDeviceData);
			if(contactDeviceDataList != null){
				DBLayer.addContactDeviceDataList(contactDeviceDataList);
			}
		}
		
		contactDeviceDataList = DBLayer.getContactDeviceDataList();
	}
	
//	@Override
//	protected void onResume() {
//		super.onResume();
//
//		DBLayer.init(context);
//
//		ContactData c = DBLayer.getContactData();
//		DeviceData d = DBLayer.getDeviceData();
//		ContactDeviceData cd = DBLayer.getContactDeviceData();
//		ContactDeviceDataList contactDeviceDataList = DBLayer.getContactDeviceDataList();
//		boolean isEmail = DBLayer.isContactWithEmailExist("dagrest@gmail.com");
//		boolean isNick = DBLayer.isContactWithNickExist("dagrest");
//		boolean isMac = DBLayer.isDeviceWithMacAddressExist("88:32:9B:01:26:DD");
//		boolean isContactDevice = DBLayer.isContactDeviceExist("+972544504619", "dagrest@gmail.com", "88:32:9B:01:26:DD");
//		
//		ContactDeviceDataList contactDeviceDataList = DBLayer.getContactDeviceDataList();
//		boolean isEmail = DBLayer.isContactWithEmailExist("dagrest@gmail.com");
//		boolean isNick = DBLayer.isContactWithNickExist("dagrest");
//		boolean isMac = DBLayer.isDeviceWithMacAddressExist("88:32:9B:01:26:DD");
//		boolean isContactDevice = DBLayer.isContactDeviceExist("+972544504619", "dagrest@gmail.com", "88:32:9B:01:26:DD");
//	
//	    String macAddress = Controller.getMacAddress(MainActivity.this);
//	    String imei = Controller.getIMEI(MainActivity.this);
//		ContactData contactData = DBLayer.addContactData("dagrest", "David", "Agrest", "dagrest@gmail.com");
//		//contactData.setRegistration_id("REG_ID");
//		DeviceData deviceData = DBLayer.addDeviceData(macAddress, "Galaxy S3", DeviceTypeEnum.phone);
//		DBLayer.addContactDeviceData("+972544504619", contactData, deviceData, imei, "REG_ID");
//		
//		ContactData contactDataNEW = DBLayer.getContactData();
//		DeviceData deviceDataNEW =  DBLayer.getDeviceData();
//		ContactDeviceData cddOnly = DBLayer.getContactDeviceDataONLY();
//		
//		ContactDeviceData cdd = DBLayer.getContactDeviceData();
//		Gson gson = new Gson();
//		ContactDeviceDataList contactDeviceDataList = new ContactDeviceDataList();
//		contactDeviceDataList.getContactDeviceDataList().add(cdd);
//		String gsonString = gson.toJson(contactDeviceDataList);
//
//		ContactDeviceDataList contactDeviceDataListNEW = null;
//		try {
//			contactDeviceDataListNEW = gson.fromJson(gsonString, ContactDeviceDataList.class);
//			int a = 0;
//		} catch (JsonSyntaxException e) {
//    		LogManager.LogException(e, "Utils", "fillContactDeviceDataFromJSON");
//			int s = 0;
//		} catch (Exception e) {
//			String s = e.getMessage();
//		}
//		
////		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
////		  
////		if (checkPlayServices()){
////			Toast.makeText(getApplicationContext(), 
////				"isGooglePlayServicesAvailable SUCCESS", 
////			Toast.LENGTH_LONG).show();
////		}
////		else{
////			GooglePlayServicesUtil.getErrorDialog(resultCode, this, RQS_GooglePlayServices);
////		}
//	}
	
    public void onClick(final View view) {

    	// ========================================
    	// ABOUT button
    	// ========================================
        if (view == findViewById(R.id.btnAbout)) {
        	About dialogAbout = new About();
        	dialogAbout.show(this.getFragmentManager(), "About");
        	
    	// ========================================
    	// JOIN button
    	// ========================================
        } else if (view == findViewById(R.id.btnJoin)) {
        	String mutualId = Controller.generateUUID();
// IMPORTANT !!!			// TODO: INSERT PHONE NUMBER and MUTUAL_ID to TABLE TABLE_JOIN_REQUEST
			// TODO: Request phone number by UI dialog - might be from contacts list (phone book)

			DBLayer.addRequest(phoneNumber, mutualId);
			boolean testRes = DBLayer.isPhoneInJoinRequestTable(phoneNumber);
			
        	// Send SMS with registration details: 
        	// phoneNumber and registartionId (mutual ID - optional) 
        	SmsManager smsManager = SmsManager.getDefault();
			ArrayList<String> parts = smsManager.divideMessage(CommonConst.JOIN_FLAG_SMS + 
					CommonConst.DELIMITER_COMMA + registrationId + CommonConst.DELIMITER_COMMA +
					mutualId);
//			smsManager.sendMultipartTextMessage("+972544504619", null, parts, null, null);    
			

    	// ========================================
    	// CLEAR button
    	// ========================================
        } else if (view == findViewById(R.id.clear)) {

//        // ========================================
//    	// GET REG ID button
//    	// ========================================
//        } else if (view == findViewById(R.id.btnGetRegId)) {
//        	String regId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
//    		LogManager.LogInfoMsg(this.getClass().getName(), "onClick", 
//    			"RegID: " + regId + " :RegID");
    	// ========================================
    	// LOCATE button
    	// ========================================
        } else if (view == findViewById(R.id.btnLocate)) {
    		LogManager.LogInfoMsg(this.getClass().getName(), "onClick -> Locate button", 
    			"ContactList activity started.");
    		Intent intentContactList = new Intent(this, ContactList.class);
    		intentContactList.putExtra(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA, 
    			new Gson().toJson(contactDeviceDataList));
    		startActivity(intentContactList);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if(locationChangeWatcher != null){
        	unregisterReceiver(locationChangeWatcher);
        }
    }

	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	            	PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.e(CommonConst.LOG_TAG, "This device is not supported by Google Play Services.");
	            LogManager.LogErrorMsg(this.getClass().getName(), "checkPlayServices", 
	            	"This device is not supported by Google Play Services.");
	            finish();
	        }
	        return false;
	    }
	    return true;
	}
	
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    registrationId = gcm.register(getResources().getString(R.string.google_project_number));
                    msg = "Device registered, registration ID=" + registrationId;

                    // Persist the version ID 
                    Preferences.setPreferencesInt(context, CommonConst.PROPERTY_APP_VERSION, 
                    	CommonConst.PROPERTY_APP_VERSION_VALUE);
                    // Persist the registration ID - no need to register again.
                    Preferences.setPreferencesString(context, CommonConst.PREFERENCES_REG_ID, registrationId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            	// TODO: do some work here...
            }
        }.execute(null, null, null);
    }

   	// Checking for all possible internet providers
    public boolean isConnectingToInternet(){
         
        ConnectivityManager connectivity = 
                             (ConnectivityManager) getSystemService(
                              Context.CONNECTIVITY_SERVICE);
          if (connectivity != null)
          {
              NetworkInfo[] info = connectivity.getAllNetworkInfo();
              if (info != null)
                  for (int i = 0; i < info.length; i++)
                      if (info[i].getState() == NetworkInfo.State.CONNECTED)
                      {
                          return true;
                      }
  
          }
          return false;
    }

}


