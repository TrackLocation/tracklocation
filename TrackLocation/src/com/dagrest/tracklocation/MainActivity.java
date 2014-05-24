package com.dagrest.tracklocation;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.DeviceData;
import com.dagrest.tracklocation.datatype.DeviceTypeEnum;
import com.dagrest.tracklocation.datatype.SMSMessage;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

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
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver locationChangeWatcher;
    private String className;
    private String regid;
    private GoogleCloudMessaging gcm;
    private Context context;

    @SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		className = this.getClass().getName();
		
		setContentView(R.layout.activity_main);
		
		context = getApplicationContext();

		Controller controller = new Controller();
		List<String> usernameList = controller.getUsernameList(context);
		
		DBLayer.init(context);
        String macAddress = Controller.getMacAddress(MainActivity.this);
        String imei = Controller.getIMEI(MainActivity.this);
		ContactData contactData = DBLayer.addContactData("dagrest", "David", "Agrest", "dagrest@gmail.com");
		contactData.setRegistration_id("REG_ID");
		DeviceData deviceData = DBLayer.addDeviceData(macAddress, "Galaxy S3", DeviceTypeEnum.phone);
		DBLayer.addContactDeviceData("+972544504619", contactData, deviceData, imei);
		
		ContactData contactDataNEW = DBLayer.getContactData();
		DeviceData deviceDataNEW =  DBLayer.getDeviceData();
		DBLayer.getContactDeviceDataONLY();
		
		ContactDeviceData cdd = DBLayer.getContactDeviceData();
		
		// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);

            if (regid.isEmpty()) {
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
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

//	@Override
//	protected void onResume() {
//		super.onResume();
//
//		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
//		  
//		if (checkPlayServices()){
//			Toast.makeText(getApplicationContext(), 
//				"isGooglePlayServicesAvailable SUCCESS", 
//			Toast.LENGTH_LONG).show();
//		}
//		else{
//			GooglePlayServicesUtil.getErrorDialog(resultCode, this, RQS_GooglePlayServices);
//		}
//	}
	
    public void onClick(final View view) {

    	// ========================================
    	// ABOUT button
    	// ========================================
        if (view == findViewById(R.id.send)) {
        	About dialogAbout = new About();
        	dialogAbout.show(this.getFragmentManager(), "About");
        	
    	// ========================================
    	// SEND button
    	// ========================================
        } else if (view == findViewById(R.id.send)) {

    	// ========================================
    	// CLEAR button
    	// ========================================
        } else if (view == findViewById(R.id.clear)) {

        // ========================================
    	// GET REG ID button
    	// ========================================
        } else if (view == findViewById(R.id.btnGetRegId)) {
        	String regId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
    		LogManager.LogInfoMsg(this.getClass().getName(), "onClick", 
    			"RegID: " + regId + " :RegID");
        } else if (view == findViewById(R.id.btnContactList)) {
    		LogManager.LogInfoMsg(this.getClass().getName(), "onClick", 
    			"ContactList activity started.");
    		Intent intentContactList = new Intent(this, ContactList.class);
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
                    regid = gcm.register(getResources().getString(R.string.google_project_number));
                    msg = "Device registered, registration ID=" + regid;

                    // Persist the version ID 
                    Preferences.setPreferencesInt(context, CommonConst.PROPERTY_APP_VERSION, 
                    	CommonConst.PROPERTY_APP_VERSION_VALUE);
                    // Persist the registration ID - no need to register again.
                    Preferences.setPreferencesString(context, CommonConst.PREFERENCES_REG_ID, regid);
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

