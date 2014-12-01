package com.dagrest.tracklocation;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.UUID;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.util.SparseArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dagrest.tracklocation.datatype.AppInfo;
import com.dagrest.tracklocation.datatype.AppInstDetails;
import com.dagrest.tracklocation.datatype.BroadcastActionEnum;
import com.dagrest.tracklocation.datatype.BroadcastConstEnum;
import com.dagrest.tracklocation.datatype.BroadcastKeyEnum;
import com.dagrest.tracklocation.datatype.CommandData;
import com.dagrest.tracklocation.datatype.CommandDataBasic;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.CommandKeyEnum;
import com.dagrest.tracklocation.datatype.CommandValueEnum;
import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.DeviceData;
import com.dagrest.tracklocation.datatype.JsonMessageData;
import com.dagrest.tracklocation.datatype.Message;
import com.dagrest.tracklocation.datatype.MessageData;
import com.dagrest.tracklocation.datatype.MessageDataContactDetails;
import com.dagrest.tracklocation.datatype.MessageDataLocation;
import com.dagrest.tracklocation.datatype.NotificationBroadcastData;
import com.dagrest.tracklocation.datatype.PermissionsData;
import com.dagrest.tracklocation.datatype.ReceivedJoinRequestData;
import com.dagrest.tracklocation.datatype.SMSMessage;
import com.dagrest.tracklocation.datatype.SMSMessageList;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.dialog.CommonDialog;
import com.dagrest.tracklocation.dialog.IDialogOnClickAction;
import com.dagrest.tracklocation.exception.CheckPlayServicesException;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.MapKeepAliveTimerJob;
import com.dagrest.tracklocation.utils.Preferences;
import com.dagrest.tracklocation.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

public class Controller {

	private final static String CLASS_NAME = "com.dagrest.tracklocation.Controller";
	
	private Timer timer;
	private MapKeepAliveTimerJob mapKeepAliveTimerJob;

	public void keepAliveTrackLocationService(Context context, ContactDeviceDataList selectedContactDeviceDataList, long startDelay){
        timer = new Timer();
        mapKeepAliveTimerJob = new MapKeepAliveTimerJob();
        mapKeepAliveTimerJob.setContext(context);
        mapKeepAliveTimerJob.setSelectedContactDeviceDataList(selectedContactDeviceDataList);
    	Log.i(CommonConst.LOG_TAG, "Start KeepAliveTrackLocationService TimerJob with repeat period = " + 
        		(CommonConst.REPEAT_PERIOD_DEFAULT / 2 + 700)/1000/60 + " min");
        timer.schedule(mapKeepAliveTimerJob, startDelay, 
        	CommonConst.REPEAT_PERIOD_DEFAULT / 2 + 700);
    	Log.i(CommonConst.LOG_TAG, "Timer with mapKeepAliveTimerJob - started");
	}
	
	public void stopKeepAliveTrackLocationService(){
		timer.cancel();
	}
	
	// =======================
	// STATIC FUNCTIONS:
	// =======================
	
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    @SuppressWarnings({"unchecked" })
	public static void OLDregisterInBackground(HashMap<String, Object> params) {
        new AsyncTask<HashMap<String, Object>, Void, String>() {
            @Override
            protected String doInBackground(HashMap<String, Object>... params) {
                String msg = "";
                Context context = null;
                String registrationId = null;
                GoogleCloudMessaging gcm = null;
                String googleProjectNumber = null;
                if(params != null){
                	HashMap<String, Object> mapParams = params[0];
                	if(mapParams.get("Context") instanceof Context){
                		context = (Context) mapParams.get("Context");
                	} else { 
                		// TODO: Error message
                	}
                	if(mapParams.get("GoogleCloudMessaging") instanceof GoogleCloudMessaging){
                		gcm = (GoogleCloudMessaging) mapParams.get("GoogleCloudMessaging");
                	} else {
                		// TODO: Error message
                	}
                	if(mapParams.get("GoogleProjectNumber") instanceof String){
                		googleProjectNumber = (String) mapParams.get("GoogleProjectNumber");
                	} else {
                		// TODO: Error message
                	}
                } else {
                	// TODO: Error message: incorrect parameters
                	return "Error" + msg;
                }
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    registrationId = gcm.register(googleProjectNumber);
                    msg = "Device registered, registration ID=" + registrationId;

                    // Persist the registration ID - no need to register again.
                    Preferences.setPreferencesString(context, CommonConst.PREFERENCES_REG_ID, registrationId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                	String errMsg = "Exception caught: " + ex.getMessage();
                	Log.e(CommonConst.LOG_TAG, errMsg, ex);
                    LogManager.LogErrorMsg(CLASS_NAME, "registerInBackground->doInBackground", errMsg);
                    
					registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
					if(registrationId == null || registrationId.isEmpty()){
						// showGoogleServiceNotAvailable();
						// TODO: Error: message Google Service Not Available
					}

                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            	// TODO: do some work here...
            }
        }.execute(params, null, null);
    }

    /**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
     * @throws CheckPlayServicesException 
	 */
	public static void checkPlayServices(Context context) throws CheckPlayServicesException {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	        	// The following dialog should be shown in calling Activity
	            //GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
	            //	PLAY_SERVICES_RESOLUTION_REQUEST).show();
	            Log.e(CommonConst.LOG_TAG, "User recoverable error: " + resultCode);
	            LogManager.LogErrorMsg(CLASS_NAME, "checkPlayServices", 
	            	"User recoverable error: " + resultCode);
	            throw new CheckPlayServicesException(CommonConst.PLAYSERVICES_ERROR, resultCode);
	        } else {
	            Log.e(CommonConst.LOG_TAG, "Google Play Services not supported with this device.");
	            LogManager.LogErrorMsg(CLASS_NAME, "checkPlayServices", 
	            	"Google Play Services not supported with this device.");
	            // finish();
	            throw new CheckPlayServicesException(CommonConst.PLAYSERVICES_DEVICE_NOT_SUPPORTED);
	        }
	    }
	}

	public static String generateUUID(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	public static String createJsonMessage(JsonMessageData jsonMessageData){	

		List<String> listRegIDs; 					// registration_IDs of the contacts that command will be send to
		String regIDToReturnMessageTo;				// sender's registartion_ID (contact that response will be returned to)
		CommandEnum command; 
		String messageString; 						// message
		MessageDataContactDetails contactDetails;	// sender's contact details
		MessageDataLocation location;				// sender's location details
		AppInfo appInfo;							// application info
		String time; 								// current time - Controller.getCurrentDate()
		String key;									// key (free pair of key/value)
		String value;								// value (free pair of key/value)
		String errorMsg;
		
		LogManager.LogFunctionCall(CLASS_NAME, "[createJsonMessage]");

		time = Controller.getCurrentDate();
		
		if(jsonMessageData == null){
			errorMsg = "There is no JSON Message Data to create JSON Message";
			LogManager.LogErrorMsg(CLASS_NAME, "[sendCommand:NO_DATA_FOR_JSON_MESSAGE]", errorMsg);
			return null;
		}
		
		command = jsonMessageData.getCommand();
		if(command == null){
			errorMsg = "Command is undefined";
			LogManager.LogErrorMsg(CLASS_NAME, "[sendCommand:UNDEFINED_COMMAND]", errorMsg);
			return null;
		}
		
		LogManager.LogFunctionCall(CLASS_NAME, "[createJsonMessage:" + command.toString() + "]");
		
		listRegIDs = jsonMessageData.getListRegIDs();
		if(listRegIDs == null){
			errorMsg = "There is no recipient list defined";
			LogManager.LogErrorMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", errorMsg);
			return null;
		}

		contactDetails = jsonMessageData.getContactDetails();
		if(contactDetails == null){
			errorMsg = "There is no sender defined";
			LogManager.LogErrorMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", errorMsg);
			return null;
		}
		
		regIDToReturnMessageTo = contactDetails.getRegId();
		if(regIDToReturnMessageTo == null || regIDToReturnMessageTo.isEmpty()){
			errorMsg = "There is no sender defined";
			LogManager.LogErrorMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", errorMsg);
			return null;
		}
		
		location = jsonMessageData.getLocation();
		messageString = jsonMessageData.getMessage();
		appInfo = jsonMessageData.getAppInfo();
		key = jsonMessageData.getKey();
		value = jsonMessageData.getValue();
		
		String jsonMessage = null;
    	
        Gson gson = new Gson();
    	
        MessageData messageData = new MessageData();
        messageData.setMessage(messageString);
        messageData.setTime(time);
        messageData.setCommand(command);
        messageData.setRegIDToReturnMessageTo(regIDToReturnMessageTo);
        messageData.setKey(key);
        messageData.setValue(value);
        messageData.setLocation(location);
        messageData.setContactDetails(contactDetails);
        messageData.setAppInfo(appInfo);
        
        // time_to_live: from 0 to 2,419,200 seconds 
        // 0 means messages that can't be delivered immediately will be discarded. 
        // However, because such messages are never stored, this provides the best 
        // latency for sending notifications.
        long time_to_live = 0;
        
        Message message = new Message();
        message.setData(messageData); 
        message.setRegistrationIDs(listRegIDs);
        message.setTime_to_live(time_to_live);

        jsonMessage = gson.toJson(message);
        
//        String infoMessage = "JSON Message: " + jsonMessage;
//        LogManager.LogInfoMsg(CLASS_NAME, "[createJsonMessage:" + command.toString() + "]", infoMessage);
    	
		LogManager.LogFunctionExit(CLASS_NAME, "[createJsonMessage:" + command.toString() + "]");

    	return jsonMessage;
    }

	/**
	 * Gets the current registration ID for application on GCM service.
	 * 
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	public static String getRegistrationId(Context context) {
	    final SharedPreferences prefs = Preferences.getGCMPreferences(context);
	    String registrationId = prefs.getString(CommonConst.PREFERENCES_REG_ID/*PROPERTY_REG_ID*/, "");
	    if (registrationId.isEmpty()) {
	        Log.i(CommonConst.LOG_TAG, "Registration not found.");
	        return "";
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    //int registeredVersion = prefs.getInt(CommonConst.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    //int currentVersion = getAppVersion(context);
	    //if (registeredVersion != currentVersion) {
	    //    Log.i(CommonConst.LOG_TAG, "App version changed.");
	    //    LogManager.LogErrorMsg(CLASS_NAME, "getRegistrationId", "App version changed.");
	    //    // TODO: To check if REG ID should be renewed - HOW? WHEN?
	    //    // return "";
	    //}
	    return registrationId;
	}

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

	public static void saveAppInfoToPreferences(Context context){
		PackageInfo pinfo;
		try {
			pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			int versionNumber = pinfo.versionCode;
			Preferences.setPreferencesInt(context, CommonConst.PREFERENCES_VERSION_NUMBER, versionNumber);
			String versionName = pinfo.versionName;
			Preferences.setPreferencesString(context, CommonConst.PREFERENCES_VERSION_NAME, versionName);
		} catch (NameNotFoundException e) {
			Log.e(CommonConst.LOG_TAG, "NameNotFoundException in getAppInfo: " + e.getMessage());
        	LogManager.LogException(e, CLASS_NAME, "getAppInfo");
		}
	}
	
	public static AppInfo getAppInfo(Context context){
		
		String className = CommonConst.TRACK_LOCATION_PROJECT_PREFIX + ".Controller";
		String methodName = "getAppInfo";
		
		AppInfo appInfo = null;
		
		if(context != null){
			int versionNumber = Preferences.getPreferencesInt(context, CommonConst.PREFERENCES_VERSION_NUMBER);
			String versionName = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_VERSION_NAME);
			appInfo = new AppInfo(versionNumber, versionName);
		} else {
			String logMessage = "Unable to get AppInfo. Context parameter is null.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
		return appInfo;
	}

	public static String getCurrentDate(){
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.US);
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}

	// actionDescription - only for logging
	public static void broadcastMessage(Context context, 
		String action, 
		String actionDescription,
		String data,
		String key, 
		String value)
	{
		LogManager.LogFunctionCall(actionDescription, "broadcastMessage");
		Intent intent = new Intent();
		intent.setAction(action); //intent.setAction("com.dagrest.tracklocation.service.GcmIntentService.GCM_UPDATED");
		intent.putExtra(key, value);
		intent.putExtra(BroadcastConstEnum.data.toString(), data);
		context.sendBroadcast(intent);
		LogManager.LogFunctionExit(actionDescription, "broadcastMessage");
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
	
	public static SMSMessageList fetchInboxSms(Activity activity, int type) {
        SMSMessageList smsInbox = new SMSMessageList();

        Uri uriSms = Uri.parse(CommonConst.SMS_URI);

        Cursor cursor = activity.getContentResolver()
                .query(uriSms,
                        new String[] { "_id", "address", "date", "body", "type", "read" }, "type=" + type, null,
                        "date" + " COLLATE LOCALIZED ASC");
        if (cursor != null) {
            cursor.moveToLast();
            if (cursor.getCount() > 0) {

                do {
//                    String date =  cursor.getString(cursor.getColumnIndex("date"));
//                    Long timestamp = Long.parseLong(date);    
//                    Calendar calendar = Calendar.getInstance();
//                    calendar.setTimeInMillis(timestamp);
//                    DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.SSS");
                    SMSMessage message = new SMSMessage();
                    message.setMessageId(cursor.getString(cursor.getColumnIndex("_id")));
                    message.setMessageNumber(cursor.getString(cursor.getColumnIndex("address")));
                    message.setMessageContent(cursor.getString(cursor.getColumnIndex("body")));
//                  message.setMessageDate(formatter.format(calendar.getTime()));
                    message.setMessageDate(cursor.getString(cursor.getColumnIndex("date")));
                    smsInbox.getSmsMessageList().add(message);
                } while (cursor.moveToPrevious());
                
            }
        }
        return smsInbox;
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

//	public static void saveValueToPreferencesIfNotExist(Context context, String valueName, String value){
//		String tmpValue = Preferences.getPreferencesString(context, valueName);
//		if(tmpValue == null || tmpValue.isEmpty()){
//			Preferences.setPreferencesString(context, valueName, value);
//		}
//	}
	
	public static String getNickNameFromEmail(String account){
		String nickName = null;
		if(account != null && !account.isEmpty() && account.contains(CommonConst.DELIMITER_AT)){
			String[] accountParts = account.split(CommonConst.DELIMITER_AT);
			if(accountParts != null){
				nickName = accountParts[0];
			}
		}
		return nickName;
	}
	
	public static String getNickName(Context context){
		String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		String nickName = getNickNameFromEmail(account);
		return nickName;
	}

	public static List<String> getNickListFromContactDeviceDataList(ContactDeviceDataList contactDeviceDataList){
		List<String> nickList = null;
		if(contactDeviceDataList != null){
			nickList = new ArrayList<String>();
			for (ContactDeviceData contactDeviceData : contactDeviceDataList.getContactDeviceDataList()) {
				if(contactDeviceData != null){
					ContactData contactData = contactDeviceData.getContactData();
					if(contactData != null){
						String nick = contactData.getNick();
						if(nick != null && !nick.isEmpty()){
							nickList.add(nick);
						}
					}
				}
			}
		}
		return nickList;
	}
	
	public static List<String> getAccountListFromContactDeviceDataList(ContactDeviceDataList contactDeviceDataList){
		List<String> accountList = null;
		if(contactDeviceDataList != null){
			accountList = new ArrayList<String>();
			for (ContactDeviceData contactDeviceData : contactDeviceDataList.getContactDeviceDataList()) {
				if(contactDeviceData != null){
					ContactData contactData = contactDeviceData.getContactData();
					if(contactData != null){
						String account = contactData.getEmail();
						if(account != null && !account.isEmpty()){
							accountList.add(account);
						}
					}
				}
			}
		}
		return accountList;
	}
	
	public static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
	}
	
	public static int getContactsNumber(Context context){
		Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null); 
		return cursor.getCount();
	}
	
	public static Bitmap getContactPhoto(ContentResolver contentResolver, Long contactId) {
	    Uri contactPhotoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
	    InputStream photoDataStream = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver,contactPhotoUri); // <-- always null
	    Bitmap photo = BitmapFactory.decodeStream(photoDataStream);
	    return  Utils.getRoundedCornerImage(photo);
	}
	
	public static SparseArray<ContactDetails> fetchContacts(Context context, SparseArray<ContactDetails> contactDetailsGroups,
			ProgressDialog barProgressDialog) {
        String phoneNumber = null;
        //SparseArray<ContactDetails> contactDetailsGroups = null;
        contactDetailsGroups.clear();
        // String email = null;
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        // Uri EmailCONTENT_URI =  ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        // String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        // String DATA = ContactsContract.CommonDataKinds.Email.DATA;
        ContentResolver contentResolver = context.getContentResolver();
        long startTime = System.currentTimeMillis();
        Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, DISPLAY_NAME + " COLLATE LOCALIZED ASC"); 
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("Retrieve all contacts query: " + endTime);
        
        startTime = System.currentTimeMillis();
        
        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {
        	int i = 0;
            while (cursor.moveToNext()) {
            	//barProgressDialog.incrementProgressBy(2);
                barProgressDialog.incrementProgressBy(1);

            	//System.out.println("Element: " + (i + 1));
                
                Long contact_id = cursor.getLong(cursor.getColumnIndex( _ID ));
                String contactName = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                if( contactName != null && !contactName.isEmpty()) {
	                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));
	                if (hasPhoneNumber > 0) {
	                	ContactDetails contactDetails = new ContactDetails();
	                	contactDetails.setContactName(contactName);
	                	contactDetails.setContactPhoto(getContactPhoto(contentResolver, contact_id));
	                    // Query and loop for every phone number of the contact
	                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { Long.toString(contact_id) }, null);
	                    while (phoneCursor.moveToNext()) {
	                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
	                        contactDetails.getPhoneNumbersList().add(phoneNumber);
	                    }
	                    phoneCursor.close();
	                    contactDetailsGroups.append(i, contactDetails);
	                    i++;
	                }
                }
            }
        }
        cursor.close();
        endTime = System.currentTimeMillis() - startTime;
        System.out.println("Retrieve all contacts details query and save to groups: " + endTime);
        return contactDetailsGroups;
    }

    public static void saveHandledSmsDetails(Context ctx, SMSMessage smsMessage){
    	Gson gson = new Gson();
	    SMSMessageList handledSmsList = null;
	    String jsonHandledSmsList = 
	    	Preferences.getPreferencesString(ctx, CommonConst.PREFERENCES_HANDLED_SMS_LIST);
	    if(jsonHandledSmsList == null || jsonHandledSmsList.isEmpty()){
	    	handledSmsList = new SMSMessageList();
	    } else {
	    	handledSmsList = gson.fromJson(jsonHandledSmsList, SMSMessageList.class);
	    }
    	if(handledSmsList != null && !Controller.isContain(handledSmsList,smsMessage)){
    		handledSmsList.getSmsMessageList().add(smsMessage);
    		String jsonHandledSmsListNew = gson.toJson(handledSmsList);
    		Preferences.setPreferencesString(ctx, 
    			CommonConst.PREFERENCES_HANDLED_SMS_LIST, jsonHandledSmsListNew);
    	}
    }
    
    private static boolean isContain(SMSMessageList handledSmsList, SMSMessage smsMessage){
    	if(handledSmsList == null){
    		return false;
    	}
    	if(smsMessage == null){
    		return false;
    	}
    	long smsMessageDateLong = Long.parseLong(smsMessage.getMessageDate());
    	List<SMSMessage> list = handledSmsList.getSmsMessageList();
    	if(list != null){
        	for (SMSMessage smsMessageEntity : list) {
    			if( smsMessageEntity != null &&
    				smsMessageEntity.getMessageDate().equals(smsMessage.getMessageDate()) &&
    				smsMessageEntity.getMessageId().equals(smsMessage.getMessageId())){
					return true;
    			}
    			long smsMessageEntityDateLongFromList = Long.parseLong(smsMessageEntity.getMessageDate());
    			// Check that smsMessage from list (handled one) has greater timestamp than smsMessage
    			// that currently checking (if handled) - consider such smsMessage as handled
    			if(smsMessageDateLong < smsMessageEntityDateLongFromList){
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    public static boolean isHandledSmsDetails(Context ctx, SMSMessage smsMessage){
    	Gson gson = new Gson();
	    SMSMessageList handledSmsList = null;
	    if(smsMessage == null){
	    	return true;
	    }
	    
	    // If installation date(timestamp) of application id greater 
	    // then SMS date(timestamp) - consider SMS as handled
		AppInstDetails appInstDetails = new AppInstDetails(ctx);
		long appInstTimestamp = appInstDetails.getTimestamp();
		String smsMessageDate = smsMessage.getMessageDate();
		if(smsMessageDate != null && !smsMessageDate.isEmpty()){
			long longSmsMessageTimestamp = Long.parseLong(smsMessageDate);
			if(appInstTimestamp > longSmsMessageTimestamp){
				return true;
			}
		}

	    String jsonHandledSmsList = 
	    	Preferences.getPreferencesString(ctx, CommonConst.PREFERENCES_HANDLED_SMS_LIST);
	    if(jsonHandledSmsList != null && !jsonHandledSmsList.isEmpty()){
	    	handledSmsList = gson.fromJson(jsonHandledSmsList, SMSMessageList.class);
	    	if(handledSmsList != null && Controller.isContain(handledSmsList, smsMessage)){
	    		return true;
	    	}
	    }
    	return false;
    }
    
	public static void showApproveJoinRequestDialog(Activity activity, Context context,
			String account, String phoneNumber, String mutualId, String regId, String macAddress) {
    	String dialogMessage = "Approve join request from " +
			account + "\n[" + phoneNumber + "]";
    	
    	IDialogOnClickAction approveJoinRequestDialogOnClickAction = new IDialogOnClickAction() {
			Context context;	
			String mutualId;
			String regId;
//			String email;
//			String macAddress;
			
            String ownerEmail;
            String ownerMacAddress;
            String ownerRegId;
            String ownerPhoneNumber;
            String message = null, key = null, value = null;
            float batteryPercentage = -1;
            AppInfo appInfo = null;

            ContactDeviceDataList contactDeviceDataList = null;
            ReceivedJoinRequestData receivedJoinRequestData = null;
			MessageDataContactDetails senderMessageDataContactDetails = null; 
			MessageDataLocation location = null;
         
			@Override
			public void doOnPositiveButton() {
				
				// Send JOIN APPROVED command (CommandEnum.join_approval) with
				// information about contact approving join request sent by SMS
//				sendApproveOnJoinRequest(context, regId, mutualId, ownerEmail, ownerRegId, ownerMacAddress, 
//					ownerPhoneNumber, CommandEnum.join_approval);
				
				CommandDataBasic commandDataBasic = new CommandData(
					context, 
        			contactDeviceDataList, 
        			CommandEnum.join_approval,
        			message,					// null
        			senderMessageDataContactDetails,
        			location, 					// null
        			CommandKeyEnum.mutualId.toString(),
        			mutualId, 
        			appInfo
				);
				commandDataBasic.sendCommand(/*true*/);
				
				// Remove from RECEIVED_JOIN_REQUEST
				ReceivedJoinRequestData receivedJoinRequestData = DBLayer.getReceivedJoinRequest(mutualId);
				if( receivedJoinRequestData == null ){
		        	String errMsg = "Failed to get received join request data";
		            Log.e(CommonConst .LOG_TAG, errMsg);
		            LogManager.LogErrorMsg(CLASS_NAME, "showApproveJoinRequestDialog", errMsg);
				}else {
					Log.i(CommonConst.LOG_TAG, "ReceivedJoinRequestData = " + receivedJoinRequestData.toString());
				}
				
				// Add information to DB about contact, requesting join operation 
				ContactDeviceDataList contactDeviceDataListOwner = 
					DBLayer.addContactDeviceDataList(
						new ContactDeviceDataList(	receivedJoinRequestData.getAccount(),
													receivedJoinRequestData.getMacAddress(), 
													receivedJoinRequestData.getPhoneNumber(), 
													receivedJoinRequestData.getRegId(), 
													null));
				if( contactDeviceDataListOwner == null ){
		        	String errMsg = "Failed to save to DB details of the contcat requesting join operation";
		            Log.e(CommonConst .LOG_TAG, errMsg);
		            LogManager.LogErrorMsg(CLASS_NAME, "showApproveJoinRequestDialog", errMsg);
				}
				
				// Delete join request that was handled 
				int count = DBLayer.deleteReceivedJoinRequest(mutualId);
				if( count < 1) {
					Log.i(CommonConst.LOG_TAG, "Failed to delete recived join request with mutual id: " + mutualId + " count = " + count);
				} else {
					Log.i(CommonConst.LOG_TAG, "Deleted recived join request with mutual id: " + mutualId + " count = " + count);
				}
			}
			
			@Override
			public void doOnNegativeButton() {

				// Send JOIN REJECTED command (CommandEnum.join_rejected) with
				// information about contact approving join request sent by SMS
//				sendApproveOnJoinRequest(context, regId, mutualId, ownerEmail, ownerRegId, ownerMacAddress, 
//					ownerPhoneNumber, CommandEnum.join_rejected);
				CommandDataBasic commandDataBasic = new CommandData(
					context, 
        			contactDeviceDataList, 
        			CommandEnum.join_rejected,
        			message, 			// null
        			senderMessageDataContactDetails, 
        			location,			// null
        			key,				// null
        			value,				// null
        			appInfo
				);
				commandDataBasic.sendCommand(/*true*/);
				
				// Remove from RECEIVED_JOIN_REQUEST
				int count = DBLayer.deleteReceivedJoinRequest(mutualId);
				if(count == 0){
					String errorMsg = "Failed to delete received join request from " + ownerEmail;
					LogManager.LogErrorMsg(CLASS_NAME, "doOnNegativeButton->deleteReceivedJoinRequest", errorMsg);
				}
			}
			
			@Override
			public void setActivity(Activity activity) {
			}
			
			@Override
			public void setContext(Context context) {
				this.context = context;
				ownerEmail = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
	            ownerMacAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
	            ownerRegId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
	            ownerPhoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
				ReceivedJoinRequestData receivedJoinRequestData = DBLayer.getReceivedJoinRequest(mutualId);
				if( receivedJoinRequestData == null ){
		        	String errMsg = "Failed to get received join request data";
		            Log.e(CommonConst .LOG_TAG, errMsg);
		            LogManager.LogErrorMsg(CLASS_NAME, "showApproveJoinRequestDialog", errMsg);
		            return;
				}else {
					contactDeviceDataList = 
							new ContactDeviceDataList(
									receivedJoinRequestData.getAccount(), 
									receivedJoinRequestData.getMacAddress(), 
									receivedJoinRequestData.getPhoneNumber(), 
									receivedJoinRequestData.getRegId(), 
									null);
					Log.i(CommonConst.LOG_TAG, "ReceivedJoinRequestData = " + receivedJoinRequestData.toString());
				}
				senderMessageDataContactDetails = 
						new MessageDataContactDetails(ownerEmail, ownerMacAddress, ownerPhoneNumber, ownerRegId, batteryPercentage);
				appInfo = Controller.getAppInfo(context);
			}
			
			@Override
			public void setParams(Object[]... objects) {
				mutualId = objects[0][0].toString();
				regId = objects[0][1].toString();
//				email = objects[0][2].toString();
//				macAddress = objects[0][3].toString();
			}

			@Override
			public void doOnChooseItem(int which) {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		Object[] objects = new Object[2];
		objects[0] = mutualId;
		objects[1] = regId;
		approveJoinRequestDialogOnClickAction.setParams(objects);
		approveJoinRequestDialogOnClickAction.setContext(context);
//		objects[2] = account;
//		objects[3] = macAddress;
    	
 		CommonDialog aboutDialog = new CommonDialog(activity, approveJoinRequestDialogOnClickAction);
		
		aboutDialog.setDialogMessage(dialogMessage);
		aboutDialog.setDialogTitle("Join request approval");
		aboutDialog.setPositiveButtonText("OK");
		aboutDialog.setNegativeButtonText("Cancel");
		aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
		aboutDialog.showDialog();
		aboutDialog.setCancelable(true);
    }
	
	public static List<ContactData> fillContactListWithContactDeviceDataFromJSON(ContactDeviceDataList contactDeviceDataCollection,
			List<Boolean> checkBoxesShareLocation, List<String> emailList, List<String> macAddressList){
		List<ContactData> values = null;
	    
		if(contactDeviceDataCollection == null){
			// TODO: error message to log
			return null;
		}
 	    List<ContactDeviceData> contactDeviceDataList = contactDeviceDataCollection.getContactDeviceDataList();
	    if(contactDeviceDataList == null){
			// TODO: error message to log
	    	return null;
	    }
	    
	    int i = 0;
	    values = new ArrayList<ContactData>();
	    for (ContactDeviceData contactDeviceData : contactDeviceDataList) {
	    	ContactData contactData = contactDeviceData.getContactData();
	    	DeviceData deviceData = contactDeviceData.getDeviceData();
	    	if(contactData != null && deviceData != null) {
	    		String nick = contactData.getNick();
	    		if(nick != null && !nick.isEmpty()){
	    			values.add(contactData);
	    			if(checkBoxesShareLocation != null){
	    				checkBoxesShareLocation.add(isLocationSharingEnabled(contactData));
	    			}
	    			if(emailList != null){
	    				emailList.add(contactData.getEmail());
	    			}
	    			if(macAddressList != null){
	    				macAddressList.add(deviceData.getDeviceMac());
	    			}
	    		} 
	    		else{
	    			String email = contactData.getEmail();
	    			if(email != null && !email.isEmpty()) {
	    				values.add(contactData);
		    			if(checkBoxesShareLocation != null){
		    				checkBoxesShareLocation.add(isLocationSharingEnabled(contactData));
		    			}
		    			if(emailList != null){
		    				emailList.add(contactData.getEmail());
		    			}
	    			} else {
		    			values.add(new ContactData());
		    			if(checkBoxesShareLocation != null){
		    				checkBoxesShareLocation.add(false);
		    			}
		    			if(emailList != null){
		    				emailList.add("unknown@unknown.com");
		    			}
		    			LogManager.LogErrorMsg("ContactList", "fillListWithContactDeviceData", "Some provided username is null - check JSON input file, element :" + (i+1));
	    			}
	    			String macAddress = deviceData.getDeviceMac();
	    			if(macAddress != null && !macAddress.isEmpty()) {
	    				values.add(new ContactData(macAddress));
		    			if(macAddressList != null){
		    				macAddressList.add(contactData.getEmail());
		    			}
	    			} else {
		    			values.add(new ContactData());
		    			if(macAddressList != null){
		    				macAddressList.add(contactData.getEmail());
		    			}
		    			LogManager.LogErrorMsg("ContactList", "fillListWithContactDeviceData", "Some provided macAddress is null - check JSON input file, element :" + (i+1));
	    			}
	    		}

	    	} else {
	    		LogManager.LogErrorMsg("ContactList", "fillListWithContactDeviceData", "Contact Data provided incorrectly - check JSON input file, element :" + (i+1));
	    		return null;
	    	}
	    	i++;
		}
	    
	    return values;
	}
	
	private static boolean isLocationSharingEnabled(ContactData contactData){
    	if(contactData != null) {
			String email = contactData.getEmail();
			if(email != null && !email.isEmpty()) {
				PermissionsData p = DBLayer.getPermissions(email);
				return p.getIsLocationSharePermitted() == 1 ? true : false;
			} else {
				return false;
			}
    	} else {
    		return false;
    	}
	}
	
	public static List<Boolean> fillShareLocationListWithContactDeviceDataFromJSON(ContactDeviceDataList contactDeviceDataList, 
			List<String> values){
		List<Boolean> valuesCheckBoxesShareLocation = null;
		
		valuesCheckBoxesShareLocation = new ArrayList<Boolean>();
		valuesCheckBoxesShareLocation.add(true);
		valuesCheckBoxesShareLocation.add(false);
		valuesCheckBoxesShareLocation.add(true);
		
		return valuesCheckBoxesShareLocation;
	}

	public static ContactDeviceDataList removeNonSelectedContacts(ContactDeviceDataList contactDeviceDataList, 
		List<String> selectedContcatList){
		
		if(contactDeviceDataList == null || selectedContcatList == null || selectedContcatList.size() == 0){
			// TODO: Error to log - no selected users were passed...
			return null;
		}
		ContactDeviceDataList selectedContactDeviceDataList = new ContactDeviceDataList();
		// remove extra contacts in contactDeviceDataList
		for (ContactDeviceData contactDeviceData : contactDeviceDataList.getContactDeviceDataList()) {
			ContactData contactData = contactDeviceData.getContactData();
			DeviceData deviceData = contactDeviceData.getDeviceData();
			if(contactData != null && deviceData != null) {
				for (String selectedContactID : selectedContcatList) {
					if(selectedContactID.equals(contactData.getNick()) || selectedContactID.equals(contactData.getEmail())){
						selectedContactDeviceDataList.getContactDeviceDataList().add(contactDeviceData);
					}
				}
			}
		}
		
		return selectedContactDeviceDataList;
	}

	public static CameraUpdate createCameraUpdateLatLngBounds(LinkedHashMap<String, Marker> markerMap) {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (LinkedHashMap.Entry<String,Marker> markerEntry : markerMap.entrySet()) {
			Marker m = markerEntry.getValue();
			if(m != null){
    			builder.include(m.getPosition());
			}
		}
		LatLngBounds bounds = builder.build();
		int padding = 50; // offset from edges of the map in pixels
		return  CameraUpdateFactory.newLatLngBounds(bounds, padding);
	}
	
	public static int getBatteryLevel(Context context){
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		//int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		//float batteryPct = level / (float)scale;
		return level;
	}
	
	// ======================================================================
	// Example to get ConnectivityManager:
	// ======================================================================
	// ConnectivityManager connectivity = 
	//	 (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	// Checking for all possible Internet providers
	// ======================================================================
	public static boolean isConnectingToInternet(
			ConnectivityManager connectivity) {

		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
			}
		}
		return false;
	}
	
	public static List<String> getPreferencesReturnToRegIDList(Context context){
		List<String> listRegIDs = null;
		java.util.Map<String, String> returnToContactMap = Preferences.getPreferencesReturnToContactMap(context);
		if(returnToContactMap != null){
			listRegIDs = new ArrayList<String>();
			for (java.util.Map.Entry<String, String> entry : returnToContactMap.entrySet()) {
				listRegIDs.add(entry.getValue());
			}
		}
		return listRegIDs;
	}
	
	public static ContactDeviceDataList getPreferencesContactDeviceDataListToSendCommandTo(Context context){
		ContactDeviceDataList contactDeviceDataToSendNotificationTo = null;
		java.util.Map<String, String> sendToMap = Preferences.getPreferencesReturnToContactMap(context);
		if(sendToMap != null && !sendToMap.isEmpty()){
		    contactDeviceDataToSendNotificationTo = new ContactDeviceDataList();
		    List<ContactDeviceData> contactDeviceDataList = contactDeviceDataToSendNotificationTo.getContactDeviceDataList();
		    for (java.util.Map.Entry<String,String> entry : sendToMap.entrySet()) {
		    	entry.getKey();
		    	entry.getValue();
		    	ContactData cd = new ContactData();
		    	cd.setEmail(entry.getKey());
		    	ContactDeviceData cdd = new ContactDeviceData();
		    	cdd.setRegistration_id(entry.getValue());
		    	cdd.setContactData(cd);
		    	contactDeviceDataList.add(cdd);
			}
		}
		return contactDeviceDataToSendNotificationTo;
	}
	
	public static void broadcsatMessage(Context context, String message, String key, String value){
		NotificationBroadcastData notificationBroadcastData = new NotificationBroadcastData();
		notificationBroadcastData.setMessage(message);
		notificationBroadcastData.setKey(key);
		notificationBroadcastData.setValue(value);
		Gson gson = new Gson();
		String jsonNotificationBroadcastData = gson.toJson(notificationBroadcastData);
		
		// Broadcast corresponding message
		Controller.broadcastMessage(context, 
			BroadcastActionEnum.BROADCAST_MESSAGE.toString(), 
			"GcmIntentService",
			jsonNotificationBroadcastData, 
			key, // BroadcastKeyEnum.message.toString(),  
			value
		);
	}

	public static void showNotificationDialog(Activity activity, String errorMessage) {
		IDialogOnClickAction dialogOnClickAction = null;
		CommonDialog aboutDialog = new CommonDialog(activity, dialogOnClickAction);
		aboutDialog.setDialogMessage(errorMessage);
		aboutDialog.setDialogTitle("Warning");
		aboutDialog.setPositiveButtonText("OK");
		aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
		aboutDialog.showDialog();
		aboutDialog.setCancelable(true);
    }
	
	public static void removeSenderAccountFromSendCommandList(Context context, 
			String jsonListAccounts, String senderAccount){
        String methodName = "showNotificationDialog";
        Gson gson = new Gson();
        @SuppressWarnings("unchecked")
		List<String> listAccounts = gson.fromJson(jsonListAccounts, List.class);
        if(listAccounts != null && listAccounts.contains(senderAccount)){
        	listAccounts.remove(senderAccount);
        	jsonListAccounts = gson.toJson(listAccounts);
        	if(jsonListAccounts == null){
        		jsonListAccounts = "";
        	}	        
        	Preferences.setPreferencesString(context, 
	        		CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS, jsonListAccounts);
        	String logMessage = "Updated recipients accounts list: [" + jsonListAccounts + "]";
    		LogManager.LogInfoMsg(CLASS_NAME, methodName, logMessage);
    		Log.i(CommonConst.LOG_TAG, "[INFO] {" + CLASS_NAME + "} -> " + methodName + ": "+ logMessage);
        }
	}
	public static String hideRealRegID(String regID){
		return (regID == null || regID.isEmpty()) ? "EMPTY" : "NON-EMPTY";
	}
	
	public static void RingDevice(Context context, String className, MessageDataContactDetails contactDetails){
		String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		String macAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
		String phoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
		String registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
        MessageDataContactDetails messageDataContactDetails = 
        	new MessageDataContactDetails(
        		account, 
        		macAddress, 
        		phoneNumber, 
        		registrationId,
        		0);
        ContactDeviceDataList contactDeviceDataToSendNotificationTo = 
        	new ContactDeviceDataList (
        		contactDetails.getAccount(), 
        		contactDetails.getMacAddress(), 
        		contactDetails.getPhoneNumber(), 
        		contactDetails.getRegId(), 
        		null);
        // Notify caller by GCM (push notification)
        
        String msgServiceStarted = "{" + className + "} RingDevice was called by [" + account + "]";
        String notificationKey = CommandKeyEnum.start_tracking_status.toString();
        String notificationValue = CommandValueEnum.success.toString();		

        CommandDataBasic commandDataBasic = new CommandData(
        	context, 
			contactDeviceDataToSendNotificationTo, 
			CommandEnum.ring_device, 
			msgServiceStarted, 
			messageDataContactDetails, 
			null, 					// location
			notificationKey, 		// key
			notificationValue,  	// value
			null
		);
        commandDataBasic.sendCommand();
	}

}
	
//    public static void checkGcmStatus(Context context, ContactData contactData, ContactDeviceData contactDeviceData){
//		String regIDToReturnMessageTo = Controller.getRegistrationId(context);
//		List<String> listRegIDs = new ArrayList<String>();
//		if(contactData != null){
//			listRegIDs.add(contactDeviceData.getRegistration_id());
//		} else {
//			LogManager.LogErrorMsg("Controller", "checkGcmStatus", "Unable to get registration_ID: contactData is null.");
//		}
//		if(listRegIDs.size() > 0){
//			String jsonMessage = Controller.createJsonMessage(listRegIDs, 
//		    		regIDToReturnMessageTo, 
//		    		CommandEnum.status_request, 
//		    		"", // messageString, 
//		    		Controller.getCurrentDate(), // time,
//		    		null, //NotificationCommandEnum.pushNotificationServiceStatus.toString(),
//		    		null //PushNotificationServiceStatusEnum.available.toString()
//					);
//			Controller.sendCommand(jsonMessage);
//		} else {
//			// TODO: error to log! Unable to send command: checkGcmStatus
//		}
//    }
//
//    public static void startTrackLocationService(Context context, ContactDeviceData contactDeviceData){
//		String regIDToReturnMessageTo = Controller.getRegistrationId(context);
//		List<String> listRegIDs = new ArrayList<String>();
//		listRegIDs.add(contactDeviceData.getRegistration_id());
//		if(listRegIDs.size() > 0){
//			String jsonMessage = Controller.createJsonMessage(listRegIDs, 
//		    		regIDToReturnMessageTo, 
//		    		CommandEnum.start, 
//		    		"", // messageString, 
//		    		Controller.getCurrentDate(), // time,
//		    		null, //NotificationCommandEnum.pushNotificationServiceStatus.toString(),
//		    		null //PushNotificationServiceStatusEnum.available.toString()
//					);
//			Controller.sendCommand(jsonMessage);
//		} else {
//			// TODO: error to log! Unable to send command: startTrackLocationService
//		}
//    }
//
//    public static void stopTrackLocationService(Context context, ContactDeviceData contactDeviceData){
//		String regIDToReturnMessageToStop = Controller.getRegistrationId(context);
//		List<String> listRegIDsStop = new ArrayList<String>();
//		listRegIDsStop.add(contactDeviceData.getRegistration_id());
//		
//		if(listRegIDsStop.size() > 0){
//			String jsonMessage = Controller.createJsonMessage(listRegIDsStop, 
//		    		regIDToReturnMessageToStop, 
//		    		CommandEnum.stop, 
//		    		"", // messageString, 
//		    		Controller.getCurrentDate(), // time,
//		    		null, // key
//		    		null // value
//					);
//			Controller.sendCommand(jsonMessage);
//		} else {
//			// TODO: error to log! Unable to send command: stopTrackLocationService
//		}
//    }

//  TODO: TO DELETE:	
//	public static String getAccountListFromPreferences(Context context){
//		final SharedPreferences prefs = Preferences.getGCMPreferences(context);
//		String account = prefs.getString(CommonConst.PREFERENCES_PHONE_ACCOUNT, null);
//		return account;
//	}
//
//	public static String getPhoneNumberFromPreferences(){
//		String phoneNumber = null;
//		return phoneNumber;
//	}
//	
//	public static String getMacAddressFromPreferences(){
//		String macAddress = null;
//		return macAddress;
//	}

/*
public static String createJsonMessage(
		List<String> listRegIDs, 					// List of contact's regIDs to send message to
		String regIDToReturnMessageTo,				// regID of contact to return message to 
		CommandEnum command, 						// command
		String messageString,						// message	
		MessageDataContactDetails contactDetails, 	// account, macAddress, phoneNumber, regId, batteryPercentage
		MessageDataLocation location,				// latitude, longitude, accuracy, speed
		AppInfo appInfo,							// application info version number, version name and so on...
		String time,								// time
		String key, 								// key
		String value){								// value
	
	LogManager.LogFunctionCall(CLASS_NAME, "[createJsonMessage:" + command.toString() + "]");
	
	String jsonMessage = null;
	
    Gson gson = new Gson();
	
    MessageData messageData = new MessageData();
    messageData.setMessage(messageString);
    messageData.setTime(time);
    messageData.setCommand(command);
    messageData.setRegIDToReturnMessageTo(regIDToReturnMessageTo);
    messageData.setKey(key);
    messageData.setValue(value);
    messageData.setLocation(location);
    messageData.setContactDetails(contactDetails);
    
    Message message = new Message();
    message.setData(messageData); 
    message.setRegistrationIDs(listRegIDs);

    jsonMessage = gson.toJson(message);
    
//    String infoMessage = "JSON Message: " + jsonMessage;
//    LogManager.LogInfoMsg(CLASS_NAME, "[createJsonMessage:" + command.toString() + "]", infoMessage);
	
	LogManager.LogFunctionExit(CLASS_NAME, "[createJsonMessage:" + command.toString() + "]");

	return jsonMessage;
}
*/    

/*
public static void sendCommand(final String jsonMessage, final Context context){ 
	
	Gson gson = new Gson();
	String result;
	
	if(jsonMessage != null){
		String account = "";
		String macAddress = "";
		Message m = gson.fromJson(jsonMessage, Message.class);
		MessageData messageData = m.getData();
		CommandEnum cmd = null;
		if(messageData != null){
			MessageDataContactDetails contactDetails = messageData.getContactDetails();
			macAddress = contactDetails.getMacAddress();
			account = contactDetails.getAccount();
			cmd = messageData.getCommand();
			Log.w(CommonConst.LOG_TAG, cmd.toString());
		}
		
		if((context != null && CommandEnum.notification.equals(cmd)) ||
		   (context != null && CommandEnum.start.equals(cmd))){
            String sendCommandRetryDestination = account+macAddress;
    		for (int i = 0; i < 5; i++) {
            	
    			//result = HttpUtils.sendMessageToBackend(jsonMessage);
    			Log.i(CommonConst.LOG_TAG, "sendMessageToBackendAsync");
    			result = HttpUtils.sendMessageToBackendAsync(jsonMessage);
            	
    			int resCode = Preferences.getPreferencesInt(context, sendCommandRetryDestination);
                Log.i(CommonConst.LOG_TAG, "[" + cmd + "] command in BACKGROUND");
                Log.i(CommonConst.LOG_TAG, "Save to " + sendCommandRetryDestination);
                Log.i(CommonConst.LOG_TAG, "[RETRY BACKGROIND] " + 
                    	Preferences.getPreferencesInt(context, sendCommandRetryDestination));
            	if (resCode == 0){
            		Log.i(CommonConst.LOG_TAG, "BREAK: " + i);
            		break;
            	}
            	LogManager.LogInfoMsg(CLASS_NAME, "HttpUtils.sendMessageToBackend", jsonMessage);
                try
                {
                	LogManager.LogInfoMsg(CLASS_NAME, "HttpUtils.sendMessageToBackend", "Wait - 3 sec");
                	Log.i(CommonConst.LOG_TAG, i + ": Sleep 10 sec => HttpUtils.sendMessageToBackend");
                    Thread.sleep(10000);
                }
                catch(Exception e)
                {
                	LogManager.LogException(e, CLASS_NAME, "HttpUtils.sendMessageToBackend");
                	Log.i(CommonConst.LOG_TAG, "Save to " + sendCommandRetryDestination);
                	Log.e(CommonConst.LOG_TAG, "Exception: Sleep 10 sec => HttpUtils.sendMessageToBackend");
                }
            	resCode = Preferences.getPreferencesInt(context, sendCommandRetryDestination);
            	if (resCode == 0){
            		Log.i(CommonConst.LOG_TAG, "BREAK: " + i);
            		break;
            	}
			} 
		}else {
			Log.i(CommonConst.LOG_TAG, "sendMessageToBackend");
			result = HttpUtils.sendMessageToBackend(jsonMessage);
		}
	}
}
*/


//@SuppressWarnings("unchecked")
//// params:
////	  Context context,
////	  ContactDeviceDataList selectedContactDeviceDataList, 
////	  MessageDataContactDetails contactDetails,
//public static void startTrackLocationService(HashMap<String, Object> params, final int retryTimes) {
//    new AsyncTask<java.util.Map<String, Object>, Void, String>() {
//        @Override
//        protected String doInBackground(java.util.Map<String, Object>... params) {
//        	String result = null;
//        	Gson gson = new Gson();
//        	Context context = null;
//        	ContactDeviceDataList selectedContactDeviceDataList = null;
//        	MessageDataContactDetails contactDetails = null;
//        	Log.i("Thread", Thread.currentThread().getId() + "");
//        	
//            if(params != null){
//            	Map<String, Object> mapParams = params[0];
//            	if(mapParams.get("Context") instanceof Context){
//            		context = (Context) mapParams.get("Context");
//            	} else { 
//            		// TODO: Error message
//            	}
//            	if(mapParams.get("SelectedContactDeviceDataList") instanceof ContactDeviceDataList){
//            		selectedContactDeviceDataList = (ContactDeviceDataList) mapParams.get("SelectedContactDeviceDataList");
//            	} else {
//            		// TODO: Error message
//            	}
//            	if(mapParams.get("ContactDetails") instanceof MessageDataContactDetails){
//            		contactDetails = (MessageDataContactDetails) mapParams.get("ContactDetails");
//            	} else {
//            		// TODO: Error message
//            	}
//            } else {
//            	// TODO: Error message: incorrect parameters
//            	return "Error" + "";
//            }
//
//			// ===========================================================
//			// ===  START  ===============================================
//			// ===========================================================
//			// Send START command (GCM) to selected contacts to start on their side TrackLocationService
//			// to notify their location
//			CommandDataBasic commandDataBasic = new CommandData(
//				context, 
//				selectedContactDeviceDataList, 
//				CommandEnum.start,	// [START]	
//				null, 				// message
//				contactDetails, 
//				null,				// location
//				null, 				// key
//				null, 				// value,
//				null 				// appInfo
//			);
//			
//	        int sendCommandRetry = 1;
//	        if(contactDetails == null){
//	        	return result;
//	        }
//	        String currentAccount = contactDetails.getAccount();
//	        String sendCommandRetryDestination = currentAccount + contactDetails.getMacAddress();
//	        Preferences.setPreferencesInt(context, sendCommandRetryDestination, sendCommandRetry);
//	        Log.i(CommonConst.LOG_TAG, "[RETRY] " + 
//	        	Preferences.getPreferencesInt(context, sendCommandRetryDestination));
//	        
//	        String jsonListAccounts = gson.toJson(commandDataBasic.getListAccounts());
//	        // Set list of recipients' accounts list 
//	        Preferences.setPreferencesString(context, 
//	        		CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS, jsonListAccounts);
//	        Log.i(CommonConst.LOG_TAG, "Saved destinations: " + jsonListAccounts);
//	        List<String> listAccounts = null;
//			for (int i = 0; i < retryTimes; i++) {
//				// TODO: Check that loop should run 
//				// Send [START] TrackLocation Service in Async way
//				jsonListAccounts = Preferences.getPreferencesString(context, 
//		        		CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS);
//		        Log.i(CommonConst.LOG_TAG, "Inside loop destinations: " + jsonListAccounts);
//
//				if(jsonListAccounts == null || jsonListAccounts.isEmpty()){
//					break;
//				}
//				listAccounts = gson.fromJson(jsonListAccounts, List.class);
//				if(listAccounts == null){
//					break;
//				}
//				if(!listAccounts.contains(currentAccount)){
//					break;
//				}
//				
//				Log.i(CommonConst.LOG_TAG, "Loop: " + (i+1) + " [START handler before] startTrackLocationService");
//        		Log.i(CommonConst.LOG_TAG, "Loop: " + (i+1) + " [START ThreadId]: " + Thread.currentThread().getId());
//				commandDataBasic.sendCommand(/*true*/);
//				Log.i(CommonConst.LOG_TAG, "Loop: " + (i+1) + " [START] command sent from startTrackLocationService");
//        		Log.i(CommonConst.LOG_TAG, "Loop: " + (i+1) + " [START handler after] startTrackLocationService");
//				try {
//					Thread.sleep(10000 * 6 * 3); // 3 min
//				} catch (InterruptedException e) {
//					// Do nothing
//					// TODO: To log - info about crash
//				}
//			}
//
//			// Reset list of recipients' accounts list to be empty
//	        Preferences.setPreferencesString(context, 
//	        		CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS, "");
//	        Log.i(CommonConst.LOG_TAG, "After loop destinations: " + jsonListAccounts);
//	        
//			// TODO: fix return value
//	    	return result;
//	    }
//	
//	    @Override
//	    protected void onPostExecute(String msg) {
//	    	// TODO: fix return value
//	    }
//	
//	}.execute(params, null, null);
//	
//	
//}

/*
//
// Send command to request contact by GCM (Google Cloud Message - push notifictation)
// 
// public static void sendCommand(final String jsonMessage, final Context context){ 
@SuppressWarnings("unchecked")
public static void sendCommand(java.util.Map<String, Object> params){ 	
	
	Log.i("ThreadMain", Thread.currentThread().getId() + "");
	// AsyncTask <TypeOfVarArgParams , ProgressValue , ResultValue>
    new AsyncTask<java.util.Map<String, Object>, Void, String>() {
        @Override
        protected String doInBackground(java.util.Map<String, Object>... params) {
        	String result = null;
        	Gson gson = new Gson();
        	Context context = null;
        	String jsonMessage = null;
        	Log.i("Thread", Thread.currentThread().getId() + "");
        	
            if(params != null){
            	Map<String, Object> mapParams = params[0];
            	if(mapParams.get("Context") instanceof Context){
            		context = (Context) mapParams.get("Context");
            	} else { 
            		// TODO: Error message
            	}
            	if(mapParams.get("JsonMessage") instanceof String){
            		jsonMessage = (String) mapParams.get("JsonMessage");
            	} else {
            		// TODO: Error message
            	}
            } else {
            	// TODO: Error message: incorrect parameters
            	return "Error" + "";
            }
        	
        	if(jsonMessage != null){
        		String account = "";
        		String macAddress = "";
        		Message m = gson.fromJson(jsonMessage, Message.class);
        		MessageData messageData = m.getData();
        		CommandEnum cmd = null;
        		if(messageData != null){
        			MessageDataContactDetails contactDetails = messageData.getContactDetails();
        			macAddress = contactDetails.getMacAddress();
        			account = contactDetails.getAccount();
        			cmd = messageData.getCommand();
        			Log.w(CommonConst.LOG_TAG, cmd.toString());
        		}
        		
        		if((context != null && CommandEnum.notification.equals(cmd)) ||
        		   (context != null && CommandEnum.start.equals(cmd))){
	                String sendCommandRetryDestination = account+macAddress;
	        		for (int i = 0; i < 5; i++) {
		            	
	        			result = HttpUtils.sendMessageToBackend(jsonMessage);
		            	
	        			int resCode = Preferences.getPreferencesInt(context, sendCommandRetryDestination);
		                Log.i(CommonConst.LOG_TAG, "[" + cmd + "] command in BACKGROUND");
		                Log.i(CommonConst.LOG_TAG, "Save to " + sendCommandRetryDestination);
		                Log.i(CommonConst.LOG_TAG, "[RETRY BACKGROIND] " + 
		                    	Preferences.getPreferencesInt(context, sendCommandRetryDestination));
		            	if (resCode == 0){
		            		Log.i(CommonConst.LOG_TAG, "BREAK: " + i);
		            		break;
		            	}
		            	LogManager.LogInfoMsg(CLASS_NAME, "HttpUtils.sendMessageToBackend", jsonMessage);
		                try
		                {
		                	LogManager.LogInfoMsg(CLASS_NAME, "HttpUtils.sendMessageToBackend", "Wait - 3 sec");
		                	Log.i(CommonConst.LOG_TAG, i + ": Wait - 3 sec => HttpUtils.sendMessageToBackend");
		                    Thread.sleep(10000);
		                }
		                catch(Exception e)
		                {
		                	LogManager.LogException(e, CLASS_NAME, "HttpUtils.sendMessageToBackend");
		                	Log.i(CommonConst.LOG_TAG, "Save to " + sendCommandRetryDestination);
		                	Log.e(CommonConst.LOG_TAG, "Exception: Wait - 3 sec => HttpUtils.sendMessageToBackend");
		                }
		            	resCode = Preferences.getPreferencesInt(context, sendCommandRetryDestination);
		            	if (resCode == 0){
		            		Log.i(CommonConst.LOG_TAG, "BREAK: " + i);
		            		break;
		            	}
					} 
        		}else {
					result = HttpUtils.sendMessageToBackend(jsonMessage);
        		}
        	}
        	// TODO: fix return value
        	return result;
        }

        @Override
        protected void onPostExecute(String msg) {
        	// TODO: fix return value
        }

    }.execute(params, null, null);
}
*/

/*
// TODO: Throw exception id sendCommand failed
// TODO: Implement return value
public static void sendCommand(CommandData commandData){
	Context context; 
	ContactDeviceDataList contactDeviceDataList; 
	CommandEnum command;
	String message; 
	MessageDataContactDetails contactDetails; 
	MessageDataLocation location; 
	String key; 
	String value;
	AppInfo appInfo;
	String errorMsg = null;
	
	LogManager.LogFunctionCall(CLASS_NAME, "[sendCommand]");

	if(commandData == null) {
		errorMsg = "There is no data to send";
		LogManager.LogErrorMsg(CLASS_NAME, "[sendCommand:NO_DATA_TO_SEND]", errorMsg);
		return;
	}
	
	command = commandData.getCommand();
	if(command == null){
		errorMsg = "Command is undefined";
		LogManager.LogErrorMsg(CLASS_NAME, "[sendCommand:UNDEFINED_COMMAND]", errorMsg);
		return;
	}
	
	LogManager.LogFunctionCall(CLASS_NAME, "[sendCommand:" + command.toString() + "]");

	context = commandData.getContext();
	if(context == null){
		errorMsg = "Context is undefined";
		LogManager.LogErrorMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", errorMsg);
		return;
	}
	
	contactDeviceDataList = commandData.getContactDeviceDataList();
	if(contactDeviceDataList == null){
		errorMsg = "There is no recipient list defined";
		LogManager.LogErrorMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", errorMsg);
		return;
	}
	
	contactDetails = commandData.getContactDetails();
	if(contactDetails == null){
		errorMsg = "There is no sender defined";
		LogManager.LogErrorMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", errorMsg);
		return;
	}
	
	// Not mandatory variables for each sendCommand 
	location = commandData.getLocation();
	message = commandData.getMessage();
	key = commandData.getKey();
	value = commandData.getValue();
	appInfo = commandData.getAppInfo();

	// Original SendCommand started here
	String infoMessage;
	
//	String regIDToReturnMessageTo = Controller.getRegistrationId(context);
//	if(regIDToReturnMessageTo == null || regIDToReturnMessageTo.isEmpty()){
//		errorMsg = "Check if app was updated; if so, it must clear the registration ID" + 
//			"since the existing regID is not guaranteed to work with the new" + 
//			"app version.";
//		LogManager.LogErrorMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", errorMsg);
//		return;
//	}

	List<String> listRegIDs = new ArrayList<String>();
	List<String> listAccounts = new ArrayList<String>();
	
	// Collect registration_IDs of the contacts that command will be send to
	for (ContactDeviceData contactDeviceData : contactDeviceDataList.getContactDeviceDataList()) {
		ContactData contactData = contactDeviceData.getContactData();
		if(contactData != null){
			String regId = contactDeviceData.getRegistration_id();
			if(regId != null && !regId.isEmpty()){
				listRegIDs.add(contactDeviceData.getRegistration_id());
			} else {
				errorMsg = "Empty registrationID for the following contact: " + contactData.getEmail();
				LogManager.LogErrorMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", errorMsg);
				Log.e("[sendCommand:" + command.toString() + "]", errorMsg);
			}
			listAccounts.add(contactData.getEmail());
		} else {
			LogManager.LogErrorMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", "Unable to get registration_ID: ContactData is null.");
			Log.e("[sendCommand:" + command.toString() + "]", "Unable to get registration_ID: contactData is null.");
		}
		
	}
	
	Gson gson = new Gson();
	infoMessage = 	"Sending command [" + command.toString() + "] to the following recipients: " +
					gson.toJson(listAccounts);
	LogManager.LogInfoMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", infoMessage);
	
	JsonMessageData jsonMessageData = new JsonMessageData(
			listRegIDs, 				// registration_IDs of the contacts that command will be send to
    		//regIDToReturnMessageTo, 	// sender's registartion_ID (contact that response will be returned to)
    		command, 
    		message, 					// messageString
    		contactDetails,				// sender's contact details
    		location,					// sender's location details
    		appInfo,					// application info
    		Controller.getCurrentDate(),// current time
    		key, 						// key (free pair of key/value)
    		value 						// value (free pair of key/value)
			);
	
	if(listRegIDs.size() > 0){
		String jsonMessage = Controller.createJsonMessage(jsonMessageData);
//				listRegIDs, 				// registration_IDs of the contacts that command will be send to
//	    		regIDToReturnMessageTo, 	// sender's registartion_ID (contact that response will be returned to)
//	    		command, 
//	    		message, 					// messageString
//	    		contactDetails,				// sender's contact details
//	    		location,					// sender's location details
//	    		appInfo,					// application info
//	    		Controller.getCurrentDate(),// current time
//	    		key, 						// key (free pair of key/value)
//	    		value 						// value (free pair of key/value)
//				);
		
		if(jsonMessage == null){
			errorMsg = "Failed to create JSON Message to send to recipient";
			LogManager.LogErrorMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", errorMsg);
			return;
		}
		
		LogManager.LogInfoMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", 
			"Sending command [" + command.toString() + "] as asynchonous task... ");
//		LogManager.LogInfoMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", 
//			"JSON message: " + jsonMessage);
		Controller.sendCommand(jsonMessage);
	} else {
		LogManager.LogErrorMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", 
			"Unable to send command: [" + command.toString() + "] - there is no any recipient.");
	}
	
	LogManager.LogFunctionExit(CLASS_NAME, "[sendCommand:" + command.toString() + "]");
}
*/	
// TODO: Implement return value
/*	
public static void sendCommand(
		Context context, 
		ContactDeviceDataList contactDeviceDataList, 
		CommandEnum command, 
		String message, 
		MessageDataContactDetails contactDetails, 
		MessageDataLocation location, 
		AppInfo appInfo,
		String key, 
		String value){
	
	String infoMessage;
	
	LogManager.LogFunctionCall(CLASS_NAME, "[sendCommand:" + command.toString() + "]");
	
	String regIDToReturnMessageTo = Controller.getRegistrationId(context);
	List<String> listRegIDs = new ArrayList<String>();
	List<String> listAccounts = new ArrayList<String>();
	
	// Collect registration_IDs of the contacts that command will be send to
	for (ContactDeviceData contactDeviceData : contactDeviceDataList.getContactDeviceDataList()) {
		ContactData contactData = contactDeviceData.getContactData();
		if(contactData != null){
			listRegIDs.add(contactDeviceData.getRegistration_id());
			listAccounts.add(contactData.getEmail());
		} else {
			LogManager.LogErrorMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", "Unable to get registration_ID: ContactData is null.");
			Log.e("[sendCommand:" + command.toString() + "]", "Unable to get registration_ID: contactData is null.");
		}
		
	}
	
	Gson gson = new Gson();
	infoMessage = 	"Sending command [" + command.toString() + "] to the following recipients: " +
					gson.toJson(listAccounts);
	LogManager.LogInfoMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", infoMessage);
	
	if(listRegIDs.size() > 0){
		String jsonMessage = Controller.createJsonMessage(
				listRegIDs, 				// registration_IDs of the contacts that command will be send to
	    		regIDToReturnMessageTo, 	// sender's registartion_ID (contact that response will be returned to)
	    		command, 
	    		message, 					// messageString
	    		contactDetails,				// sender's contact details
	    		location,					// sender's location details
	    		appInfo,					// application info
	    		Controller.getCurrentDate(),// current time
	    		key, 						// key (free pair of key/value)
	    		value 						// value (free pair of key/value)
				);
		LogManager.LogInfoMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", 
			"Sending command [" + command.toString() + "] as asynchonous task... ");
//		LogManager.LogInfoMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", 
//			"JSON message: " + jsonMessage);
		Controller.sendCommand(jsonMessage);
	} else {
		LogManager.LogErrorMsg(CLASS_NAME, "[sendCommand:" + command.toString() + "]", 
			"Unable to send command: [" + command.toString() + "] - there is no any recipient.");
	}
	
	LogManager.LogFunctionExit(CLASS_NAME, "[sendCommand:" + command.toString() + "]");
}
*/	

/*
// TODO: Remove regId - replaced by ownerRegId
public static void sendApproveOnJoinRequest(Context context, String regId, String ownerMutualId, 
		String ownerEmail, String ownerRegId, String ownerMacAddress, String ownerPhoneNumber, 
		CommandEnum command){
	
	String regIDToReturnMessageTo = regId;
	List<String> listRegIDs = new ArrayList<String>();
	// listRegIDs.add(regId);
	String time = Controller.getCurrentDate();
	String messageString = ownerEmail + CommonConst.DELIMITER_COMMA + ownerRegId + CommonConst.DELIMITER_COMMA + 
			ownerPhoneNumber + CommonConst.DELIMITER_COMMA + ownerMacAddress;
	listRegIDs.add(ownerRegId);
	float batteryPercentage = -1;
	MessageDataContactDetails contactDetails = 
		new MessageDataContactDetails(ownerEmail, ownerMacAddress, ownerPhoneNumber, ownerRegId, batteryPercentage);
	MessageDataLocation location = null;
	
	// TODO: remove regIDToReturnMessageTo from function signature - remove it to function itself
	String jsonMessage = createJsonMessage(new JsonMessageData(
			listRegIDs, 
    		regIDToReturnMessageTo, 
    		command, // CommandEnum.join_approval, 
    		messageString, // email, regId, phoneNumber, macAddress
    		contactDetails,
    		location,
    		null, // application info
    		time,
    		NotificationKeyEnum.joinRequestApprovalMutualId.toString(), 
    		ownerMutualId
			));
	sendCommand(jsonMessage);
//	if(CommandEnum.join_approval.toString().equals(command)){
//		// add contact to the following tables:
//		// TABLE_CONTACT_DEVICE
//		// TABLE_CONTACT
//		// TABLE_DEVICE
//		ContactData contactData = DBLayer.addContactData(null, null, null, email);
//		DeviceData deviceData = DBLayer.addDeviceData("macAddress", null, DeviceTypeEnum.unknown);
//		ContactDeviceData contactDeviceData = new ContactDeviceData();
//		contactDeviceData.setContactData(contactData);
//		contactDeviceData.setDeviceData(deviceData);
//		ContactDeviceDataList contactDeviceDataList = new ContactDeviceDataList();
//		contactDeviceDataList.getContactDeviceDataList().add(contactDeviceData);
//		DBLayer.addContactDeviceDataList(contactDeviceDataList);
//	}
}
*/


//public static List<String> fillContactListWithContactDeviceDataFromJSON(String jsonStringContactDeviceData){
//	List<String> values = null;
//    
//	ContactDeviceDataList contactDeviceDataCollection = Utils.fillContactDeviceDataListFromJSON(jsonStringContactDeviceData);
//    if(contactDeviceDataCollection == null){
//    	return null;
//    }
//
//    values = fillContactListWithContactDeviceDataFromJSON(contactDeviceDataCollection, checkBoxesShareLocation);
//    return values;
//}

///*
// * Check if received SMS with JOIN REQUEST
// * 
// * If join request approved:
// *  1 - Save "requester" contact details into DB (CONTACT_DATA, DEVICE_DATA, CONTACT_DEVICE_DATA tables)
// *  2 - Send approve command to "requester" contact by GCM (Google Cloud Message - push notifictation)
// *      with owner contact details
// *      
// * Input parematers:
// * 		Object array {Activity, Context}
// */
//public static void checkJoinRequestBySMS(Object[] objects) { 
//	// AsyncTask <TypeOfVarArgParams , ProgressValue , ResultValue>
//	
//    new AsyncTask<Object[], Void, String>() {
//	
//    	@Override
//        protected String doInBackground(Object[]... objects) {
//    		Gson gson = new Gson();
//    	    // Read SMS messages from inbox
//    		Context ctx = null;
//    		if(objects[0][0] != null){
//    			ctx = (Context)objects[0][0];
//    		} else {
//    			// TODO: handle case if input is invalid - expected Context type
//    		}
//    		
//    		Activity activity = null;
//    		if(objects[0][1] != null){
//    			activity = (Activity)objects[0][1];
//    		} else {
//    			// TODO: handle case if input is invalid - expected Context type
//    		}
//    		
//    		// Fetch all SMS 
//    	    SMSMessageList smsList = fetchInboxSms(activity, 1);
//    	    if(smsList != null && smsList.getSmsMessageList() != null){
//    	    	for (SMSMessage smsMessage : smsList.getSmsMessageList()) {
//    	    		//String n = smsMessage.getMessageNumber();
//    	    		// Check if there SMS with JOIN REQUEST from TrackLocation application
//					if(smsMessage != null && smsMessage.getMessageContent().contains(CommonConst.JOIN_FLAG_SMS)){
//			    	    String smsMsg = smsMessage.getMessageContent();
//			    	    String smsId = smsMessage.getMessageId();
//			    	    String smsPhoneNumber = smsMessage.getMessageNumber();
//			    	    String smsDate = smsMessage.getMessageDate();
//			    	    
//			    	    if(Controller.isHandledSmsDetails(ctx, smsMessage)){
//			    	    	continue;
//			    	    }
//			    	    
//			    	    String[] smsParams = smsMsg.split(CommonConst.DELIMITER_COMMA);
//			    	    
//			    	    if(smsParams.length >= 4){
//			    	    	
//				    	    String phoneNumberFromSMS = smsParams[3];
//				    	    if(phoneNumberFromSMS == null || phoneNumberFromSMS.isEmpty()){
//				    	    	phoneNumberFromSMS = smsPhoneNumber;
//				    	    }
//				    	    String mutualIdFromSMS = smsParams[2];
//				    	    String regIdFromSMS = smsParams[1];
//				    	    String accountFromSMS = smsParams[4];
//				    	    String macAddressFromSMS = smsParams[5];
//				    	    
//				    	    if(phoneNumberFromSMS != null && !phoneNumberFromSMS.isEmpty() &&
//				    	    	mutualIdFromSMS != null && macAddressFromSMS != null && !mutualIdFromSMS.isEmpty() &&
//				    	    	regIdFromSMS != null && !regIdFromSMS.isEmpty() && !macAddressFromSMS.isEmpty()){
//				    	    	
//				    	    	// Save contact details received by join requests to RECEIVED_JOIN_REQUEST table
//				    			long res = DBLayer.addReceivedJoinRequest(phoneNumberFromSMS, mutualIdFromSMS, regIdFromSMS, accountFromSMS, macAddressFromSMS);
//				    			if(res != 1){
//				    	        	String errMsg = "Add received join request FAILED for phoneNumber = " + phoneNumberFromSMS;
//				    	            Log.e(CommonConst.LOG_TAG, errMsg);
//				    	            LogManager.LogErrorMsg(CLASS_NAME, "checkJoinRequestBySMS", errMsg);
//				    			} else {
//				    				// TODO: delete SMS that was handled
//				    				String uriSms = Uri.parse(CommonConst.SMS_URI) + "/" + smsId;
//				    				//int count = activity.getContentResolver().delete(Uri.parse(uriSms), 
//				    				//	"date=?", new String[] { smsDate });
//				    				int count = 0;
//				    				if(count != 1){
//				    					// Log that join SMS request has not been removed
//					    	        	String errMsg = "Failed to delete join request SMS";
//					    	            Log.e(CommonConst.LOG_TAG, errMsg);
//					    	            LogManager.LogErrorMsg(CLASS_NAME, "checkJoinRequestBySMS", errMsg);
//					    	            
//					    	            Controller.saveHandledSmsDetails(ctx, smsMessage);
//					    	            
////					    	            List handledSmsList = null;
////					    	            String jsonHandledSmsList = 
////					    	            	Preferences.getPreferencesString(ctx, CommonConst.PREFERENCES_HANDLED_SMS_LIST);
////					    	            if(jsonHandledSmsList == null || jsonHandledSmsList.isEmpty()){
////					    	            	handledSmsList = new ArrayList();
////					    	            } else {
////					    	            	handledSmsList = gson.fromJson(jsonHandledSmsList, List.class);
////					    	            	if(handledSmsList != null && !handledSmsList.contains(smsId)){
////					    	            		handledSmsList.add(smsId);
////					    	            		String jsonHandledSmsListNew = gson.toJson(handledSmsList);
////					    	            		Preferences.setPreferencesString(ctx, 
////					    	            			CommonConst.PREFERENCES_HANDLED_SMS_LIST, jsonHandledSmsListNew);
////					    	            	}
////					    	            }
//				    				}
//				    	    	    // Check that join request approved and send back by
//				    	    	    // push notification (GCM) owner contact details
//				    				showApproveJoinRequestDialog(activity, ctx, accountFromSMS, phoneNumberFromSMS, mutualIdFromSMS, regIdFromSMS, macAddressFromSMS);
//				    			}
//				    	    } else {
//			    	        	String errMsg = "No NULL or empty parameters accepted for mutualId , regId, " + 
//					    	    	"macAddress and phoneNumber.";
//			    	            Log.e(CommonConst.LOG_TAG, errMsg);
//			    	            LogManager.LogErrorMsg(CLASS_NAME, "checkJoinRequestBySMS", errMsg);
//				    	    	if(phoneNumberFromSMS != null && !phoneNumberFromSMS.isEmpty()){
//				    	        	errMsg = "phoneNumber is null or empty";
//				    	            Log.e(CommonConst.LOG_TAG, errMsg);
//				    	            LogManager.LogErrorMsg(CLASS_NAME, "checkJoinRequestBySMS", errMsg);
//				    	    	}
//				    	    	if(mutualIdFromSMS != null && !mutualIdFromSMS.isEmpty()){
//				    	        	errMsg = "mutualId is null or empty";
//				    	            Log.e(CommonConst.LOG_TAG, errMsg);
//				    	            LogManager.LogErrorMsg(CLASS_NAME, "checkJoinRequestBySMS", errMsg);
//				    	    	}
//				    	    	if(regIdFromSMS != null && !regIdFromSMS.isEmpty()){
//				    	        	errMsg = "regId is null or empty";
//				    	            Log.e(CommonConst.LOG_TAG, errMsg);
//				    	            LogManager.LogErrorMsg(CLASS_NAME, "checkJoinRequestBySMS", errMsg);
//				    	    	}
//				    	    	if(macAddressFromSMS != null && !macAddressFromSMS.isEmpty()){
//				    	        	errMsg = "macAddress is null or empty";
//				    	            Log.e(CommonConst.LOG_TAG, errMsg);
//				    	            LogManager.LogErrorMsg(CLASS_NAME, "checkJoinRequestBySMS", errMsg);
//				    	    	}
//				    	    }
//			    	    }
//					}
//				}
//    	    }
//    	    return "";
//        }
//
//        @Override
//        protected void onPostExecute(String msg) {
//        	// TODO: fix return value
//        }
//    }.execute(objects);
//}

