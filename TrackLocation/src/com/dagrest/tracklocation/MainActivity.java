package com.dagrest.tracklocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.JoinRequestData;
import com.dagrest.tracklocation.db.DBHelper;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.db.DBManager;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.dagrest.tracklocation.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
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
            
        } else {
            Log.e(CommonConst.LOG_TAG, "No valid Google Play Services APK found.");
    		LogManager.LogInfoMsg(this.getClass().getName(), "onCreate", 
    			"No valid Google Play Services APK found.");
        }

		//DBLayer.init(context);
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
		Controller.checkJoinRequestBySMS(new Object[] {context, MainActivity.this}); 
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
		
		DBManager.initDBManagerInstance(new DBHelper(context));
		
		// Read contact and device data from json file and insert it to DB
		String jsonStringContactDeviceData = Utils.getContactDeviceDataFromJsonFile();
		if(jsonStringContactDeviceData != null && !jsonStringContactDeviceData.isEmpty()) {
			ContactDeviceDataList contactDeviceDataList = Utils.fillContactDeviceDataListFromJSON(jsonStringContactDeviceData);
			if(contactDeviceDataList != null){
				DBLayer.addContactDeviceDataList(contactDeviceDataList);
			}
		}
		
        //Put up the Yes/No message box
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder
    	.setTitle("Join request")
    	.setMessage("Approve join request from David Agrest, phone: +972 (54) 4504619 ?")
    	.setIcon(android.R.drawable.ic_dialog_alert)
    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {			      	
    	    	//Yes button clicked, do something
    	    	Toast.makeText(MainActivity.this, "Yes button pressed", 
                               Toast.LENGTH_SHORT).show();
    	    }
    	})
    	.setNegativeButton("No", null)						//Do nothing on no
    	.show();
		
		contactDeviceDataList = DBLayer.getContactDeviceDataList();
	}
	
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

    		Intent joinContactListIntent = new Intent(this, JoinContactList.class);
    		startActivity(joinContactListIntent);

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
        	
        // ========================================
    	// GET REG ID button
    	// ========================================
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CommonConst.REQUEST_SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
            // Get the URI and query the content provider for the phone number
            Uri contactUri = data.getData();
            String[] projection = new String[]{CommonDataKinds.Phone.NUMBER};
            Cursor cursor = getContentResolver().query(contactUri, projection,
                    null, null, null);
            // If the cursor returned is valid, get the phone number
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER);
                String phoneNumberToJoin = cursor.getString(numberIndex);

            	String mutualId = Controller.generateUUID();

//            	
// IMPORTANT !!!			// TODO: INSERT PHONE NUMBER and MUTUAL_ID to TABLE TABLE_JOIN_REQUEST
//             			// TODO: Request phone number by UI dialog - might be from contacts list (phone book)
//
     			long res = DBLayer.addJoinRequest(phoneNumberToJoin, mutualId);
     			JoinRequestData joinRequestData = DBLayer.getJoinRequest(phoneNumberToJoin);

                // TODO: log number that join request was send to
                // TODO: remove all incorrect symbols from number except digits and "+" sign	
    			if(registrationId != null && !registrationId.isEmpty()){
    	        	// Send SMS with registration details: 
    	        	// phoneNumber and registartionId (mutual ID - optional) 
    	        	SmsManager smsManager = SmsManager.getDefault();
    				ArrayList<String> parts = smsManager.divideMessage(CommonConst.JOIN_FLAG_SMS + 
    						CommonConst.DELIMITER_COMMA + registrationId + CommonConst.DELIMITER_COMMA +
    						mutualId);
    				//smsManager.sendMultipartTextMessage(phoneNumberToJoin, null, parts, null, null);    
    			}
            }
        }
    }

    private void createDialog(String text){
		// get prompts.xml view
		LayoutInflater li = LayoutInflater.from(MainActivity.this);
		View promptsView = li.inflate(R.layout.dialog, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				MainActivity.this);

		// set prompts.xml to alertdialog builder
		alertDialogBuilder.setView(promptsView);

		final EditText userInput = (EditText) promptsView
				.findViewById(R.id.editTextDialogUserInput);

		userInput.setText(text);
		
		// set dialog message
		alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton("OK",
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) {
				// get user input and set it to result
				// edit text
//				result.setText(userInput.getText());
			    }
			  })
			.setNegativeButton("Cancel",
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			    }
			  });

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
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

//	@Override
//	protected void onResume() {
//		super.onResume();
//
//	SmsManager smsManager = SmsManager.getDefault();
//	regid = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
//	smsManager.sendTextMessage("+972544504619", null, "\"" + regid + "\"", null, null);

//	// Send SMS message (multipart text message)            	
//    regid = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
//    
//    SmsManager smsManager = SmsManager.getDefault();
//    ArrayList<String> parts = smsManager.divideMessage("GUID" + "," + "dagrest@gmail.com" + "," + regid);
//    smsManager.sendMultipartTextMessage("+972544504619", null, parts, null, null);     		
	
//    // Read SMS messages from inbox
//    List<SMSMessage> smsList= controller.fetchInboxSms(MainActivity.this, 1);
//    String s = smsList.get(0).messageContent;
//    System.out.println(smsList.get(0).messageContent);
	
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
	
    
}


