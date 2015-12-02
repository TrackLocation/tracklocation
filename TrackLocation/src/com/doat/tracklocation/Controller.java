package com.doat.tracklocation;

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
import java.util.Map;
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
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.util.SparseArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.doat.tracklocation.datatype.AppInfo;
import com.doat.tracklocation.datatype.AppInstDetails;
import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.datatype.BroadcastConstEnum;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.CommandData;
import com.doat.tracklocation.datatype.CommandDataBasic;
import com.doat.tracklocation.datatype.CommandEnum;
import com.doat.tracklocation.datatype.CommandKeyEnum;
import com.doat.tracklocation.datatype.CommandValueEnum;
import com.doat.tracklocation.datatype.ContactData;
import com.doat.tracklocation.datatype.ContactDeviceData;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.DeviceData;
import com.doat.tracklocation.datatype.JsonMessageData;
import com.doat.tracklocation.datatype.MapMarkerDetails;
import com.doat.tracklocation.datatype.Message;
import com.doat.tracklocation.datatype.MessageData;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.datatype.MessageDataLocation;
import com.doat.tracklocation.datatype.NotificationBroadcastData;
import com.doat.tracklocation.datatype.PermissionsData;
import com.doat.tracklocation.datatype.ReceivedJoinRequestData;
import com.doat.tracklocation.datatype.SMSMessage;
import com.doat.tracklocation.datatype.SMSMessageList;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.dialog.CommonDialog;
import com.doat.tracklocation.dialog.IDialogOnClickAction;
import com.doat.tracklocation.exception.CheckPlayServicesException;
import com.doat.tracklocation.exception.UnableToSendCommandException;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.MapKeepAliveTimerJob;
import com.doat.tracklocation.utils.Preferences;
import com.doat.tracklocation.utils.Utils;
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
	private static final String className = "Controller";

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
		String methodName = "broadcastMessage";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		LogManager.LogInfoMsg(className, methodName, "Action: " + action);
		LogManager.LogInfoMsg(className, methodName, "Key: " + key);
		
		Intent intent = new Intent();
		intent.setAction(action); //intent.setAction("com.dagrest.tracklocation.service.GcmIntentService.GCM_UPDATED");
		// TODO: check how to set package for sendBroadcast !!!
		//intent.setPackage(CommonConst.TRACK_LOCATION_PROJECT_PREFIX);
		//intent.setPackage("com.doat.tracklocation.*");
		intent.putExtra(key, value);
		intent.putExtra(BroadcastConstEnum.data.toString(), data);
		context.sendBroadcast(intent);
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
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
			for (ContactDeviceData contactDeviceData : contactDeviceDataList) {
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
			for (ContactDeviceData contactDeviceData : contactDeviceDataList) {
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
	
	public static Bitmap getContactPhoto(ContentResolver contentResolver, Long contactId, Boolean isRounded) {
	    Uri contactPhotoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
	    InputStream photoDataStream = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver,contactPhotoUri); // <-- always null
	    Bitmap photo = BitmapFactory.decodeStream(photoDataStream);
	    return  isRounded ? Utils.getRoundedCornerImage(photo, false) : photo;
	}
	
	public static SparseArray<ContactData> fetchContacts(Context context, SparseArray<ContactData> contactDetailsGroups,
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
        String selection = HAS_PHONE_NUMBER + " > 0 ";
        Cursor cursor = contentResolver.query(CONTENT_URI, new String[] {_ID, DISPLAY_NAME},selection, null, DISPLAY_NAME + " COLLATE LOCALIZED ASC"); 
        long endTime = System.currentTimeMillis() - startTime;
//        System.out.println("Retrieve all contacts query: " + endTime);
        
        startTime = System.currentTimeMillis();
        
        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {
        	int i = 0;
            while (cursor.moveToNext()) {            	
                barProgressDialog.incrementProgressBy(1);
                
                Long contact_id = cursor.getLong(cursor.getColumnIndex( _ID ));
                String contactName = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                if( contactName != null && !contactName.isEmpty()) {
	                
                	ContactData contactDetails = new ContactData();
                	contactDetails.setNick(contactName);
                	contactDetails.setContactPhoto(getContactPhoto(contentResolver, contact_id, false));
                    // Query and loop for every phone number of the contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI,  new String[] {Phone.NUMBER}, Phone_CONTACT_ID + " = ?", new String[] { Long.toString(contact_id) }, null);
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = PhoneNumberUtils.extractNetworkPortion(phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER)));
                        if (!contactDetails.getPhoneNumbersList().contains(phoneNumber))
                        	contactDetails.getPhoneNumbersList().add(phoneNumber);	                        	                      
                    }
                    phoneCursor.close();
                    contactDetailsGroups.append(i, contactDetails);
                    i++;
                }
            }
        }
        cursor.close();
        endTime = System.currentTimeMillis() - startTime;
//        System.out.println("Retrieve all contacts details query and save to groups: " + endTime);
        return contactDetailsGroups;
    }
	
	public static SparseArray<ContactData> fetchContactsEx(Context context, SparseArray<ContactData> contactDetailsGroups,
			ProgressDialog barProgressDialog) {
        String phoneNumber = null;        
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
        String selection = HAS_PHONE_NUMBER + " > 0 ";
        Cursor cursor = contentResolver.query(CONTENT_URI, new String[] {_ID, DISPLAY_NAME},selection, null, DISPLAY_NAME + " COLLATE LOCALIZED ASC"); 
        long endTime = System.currentTimeMillis() - startTime;
//        System.out.println("Retrieve all contacts query: " + endTime);
        
        startTime = System.currentTimeMillis();
        
        Map<String, ContactData> contactMap = new HashMap<String, ContactData>();
        
        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {
        	int i = 0;
            while (cursor.moveToNext()) {            	
                barProgressDialog.incrementProgressBy(1);
                
                Long contact_id = cursor.getLong(cursor.getColumnIndex( _ID ));
                String contactName = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                if( contactName != null && !contactName.isEmpty()) {
	                
                	ContactData contactDetails = new ContactData();
                	contactDetails.setNick(contactName);
                	contactDetails.setContactPhoto(getContactPhoto(contentResolver, contact_id, false));
                    // Query and loop for every phone number of the contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI,  new String[] {Phone.NUMBER}, Phone_CONTACT_ID + " = ?", new String[] { Long.toString(contact_id) }, null);
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = PhoneNumberUtils.extractNetworkPortion(phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER)));
                        if (!contactDetails.getPhoneNumbersList().contains(phoneNumber))
                        	contactDetails.getPhoneNumbersList().add(phoneNumber);	                        	                      
                    }
                    phoneCursor.close();
                    contactDetailsGroups.append(i, contactDetails);
                    i++;
                }
            }
        }
        cursor.close();
        endTime = System.currentTimeMillis() - startTime;
//        System.out.println("Retrieve all contacts details query and save to groups: " + endTime);
        return contactDetailsGroups;
    }

	public static List<ContactData> fillContactListWithContactDeviceData(Context context, ContactDeviceDataList contactDeviceDataCollection,
			List<Boolean> checkBoxesShareLocation, List<String> emailList, List<String> macAddressList){
		List<ContactData> values = null;
	    
		if(contactDeviceDataCollection == null){
			// TODO: error message to log
			return null;
		} 	   	    
	    
	    int i = 0;
	    values = new ArrayList<ContactData>();
	    for (ContactDeviceData contactDeviceData : contactDeviceDataCollection) {
	    	ContactData contactData = contactDeviceData.getContactData();
	  
	    	contactData.setContactPhoto(contactData.getContactPhoto() == null ? Controller.getContactPhotoByEmail(context, contactData.getEmail()) : contactData.getContactPhoto());
	    	
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
				PermissionsData p = DBLayer.getInstance().getPermissions(email);
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
		for (ContactDeviceData contactDeviceData : contactDeviceDataList) {
			ContactData contactData = contactDeviceData.getContactData();
			DeviceData deviceData = contactDeviceData.getDeviceData();
			if(contactData != null && deviceData != null) {
				for (String selectedContactID : selectedContcatList) {
					if(selectedContactID.equals(contactData.getNick()) || selectedContactID.equals(contactData.getEmail())){
						selectedContactDeviceDataList.add(contactDeviceData);
					}
				}
			}
		}
		
		return selectedContactDeviceDataList;
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
		    for (java.util.Map.Entry<String,String> entry : sendToMap.entrySet()) {
		    	entry.getKey();
		    	entry.getValue();
		    	ContactData cd = new ContactData();
		    	cd.setEmail(entry.getKey());
		    	ContactDeviceData cdd = new ContactDeviceData();
		    	cdd.setRegistration_id(entry.getValue());
		    	cdd.setContactData(cd);
		    	contactDeviceDataToSendNotificationTo.add(cdd);
			}
		}
		return contactDeviceDataToSendNotificationTo;
	}
	
	public static void broadcsatMessage(Context context, String broadcastMessage, String message, String key, String value){
		NotificationBroadcastData notificationBroadcastData = new NotificationBroadcastData();
		notificationBroadcastData.setMessage(message);
		notificationBroadcastData.setKey(key);
		notificationBroadcastData.setValue(value);
		Gson gson = new Gson();
		String jsonNotificationBroadcastData = gson.toJson(notificationBroadcastData);
		
		// Broadcast corresponding message
		Controller.broadcastMessage(context, 
			broadcastMessage, 
			"GcmIntentService",
			jsonNotificationBroadcastData, 
			key, // BroadcastKeyEnum.message.toString(),  
			value
		);
	}

/*	
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
*/	
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
		String methodName = "RingDevice";
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

        CommandDataBasic commandDataBasic;
		try {
			commandDataBasic = new CommandData(
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
		} catch (UnableToSendCommandException e) {
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
		}
	}
	
	public static Long getContactIdByEmail(Context context, String email) {
        Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI, Uri.encode(email));
        long contactId =0;

        ContentResolver contentResolver = context.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {ContactsContract.Data.CONTACT_ID }, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {

                contactLookup.moveToNext();              
                contactId = contactLookup.getLong(contactLookup.getColumnIndex(ContactsContract.Data.CONTACT_ID));

            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return contactId;
    }
	
	public static Bitmap getContactPhotoByEmail(Context context, String email) {
		Long contactId = getContactIdByEmail(context, email);
		return getContactPhoto(context.getContentResolver(), contactId, false);
	}
	
	public static MessageDataContactDetails getMessageDataContactDetails(Context context){
		String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		String macAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
		String phoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
		String registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
		return new MessageDataContactDetails(account, macAddress, phoneNumber, registrationId, 
			Controller.getBatteryLevel(context));
	}
}
