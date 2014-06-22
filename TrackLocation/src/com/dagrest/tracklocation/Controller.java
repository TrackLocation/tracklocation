package com.dagrest.tracklocation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.util.SparseArray;

import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.DeviceData;
import com.dagrest.tracklocation.datatype.Message;
import com.dagrest.tracklocation.datatype.MessageData;
import com.dagrest.tracklocation.datatype.SMSMessage;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.http.HttpUtils;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.dagrest.tracklocation.utils.Utils;
import com.google.gson.Gson;

public class Controller {

	public static String generateUUID(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	public static String createJsonMessage(List<String> listRegIDs, 
    		String regIDToReturnMessageTo, 
    		CommandEnum command, 
    		String messageString, 
    		String time,
//    		TrackLocationServiceStatusEnum trackLocationServiceStatus,
//    		PushNotificationServiceStatusEnum pushNotificationServiceStatus,
    		String key, 
    		String value){
    	
    	String jsonMessage = null;
    	
        Gson gson = new Gson();
    	
        MessageData messageData = new MessageData();
        messageData.setMessage(messageString);
        messageData.setTime(time);
        messageData.setCommand(command);
        messageData.setRegIDToReturnMessageTo(regIDToReturnMessageTo);
//        messageData.setTrackLocationServiceStatusEnum(trackLocationServiceStatus);
//        messageData.setPushNotificationServiceStatusEnum(pushNotificationServiceStatus);
        messageData.setKey(key);
        messageData.setValue(value);
        
        Message message = new Message();
        message.setData(messageData); 
        message.setRegistrationIDs(listRegIDs);

        jsonMessage = gson.toJson(message);
    	
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
	    int registeredVersion = prefs.getInt(CommonConst.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(context);
	    if (registeredVersion != currentVersion) {
	        Log.i(CommonConst.LOG_TAG, "App version changed.");
	        return "";
	    }
	    return registrationId;
	}

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /*
     * Send command to request contact by GCM (Google Cloud Message - push notifictation)
     */
    public static void sendCommand(final String jsonMessage){ 
    	// AsyncTask <TypeOfVarArgParams , ProgressValue , ResultValue>
	    new AsyncTask<Void, Void, String>() {
	        @Override
	        protected String doInBackground(Void... params) {
//	            try {
	        		//sendRegistrationIdToBackend(regid);
	            	String result = HttpUtils.sendRegistrationIdToBackend(jsonMessage);
	            	// TODO: fix return value
	            	return result;
//	            } catch (Exception e) {
//	//                LogManager.LogErrorMsg(this.getClass().getName(), "onClick->Send", 
//	//                	"Error :" + ex.getMessage());
//	            	// TODO: fix return value
//	            	return "Exception" + e.getMessage();
//	            }
	        }
	
	        @Override
	        protected void onPostExecute(String msg) {
	        	// TODO: fix return value
	        }
	    }.execute(null, null, null);
    }

	public static String getCurrentDate(){
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.US);
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}

	// actionDescription - only for logging
	public static void broadcastMessage(Context context, String action, String actionDescription, String key, String value)
	{
		LogManager.LogFunctionCall(actionDescription, "broadcastMessage");
		Intent intent = new Intent();
		intent.setAction(action); //intent.setAction("com.dagrest.tracklocation.service.GcmIntentService.GCM_UPDATED");
		intent.putExtra(key, value);
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
	
	public static ArrayList<SMSMessage> fetchInboxSms(Activity activity, int type) {
        ArrayList<SMSMessage> smsInbox = new ArrayList<SMSMessage>();

        Uri uriSms = Uri.parse(CommonConst.SMS_URI);

        Cursor cursor = activity.getContentResolver()
                .query(uriSms,
                        new String[] { "_id", "address", "date", "body", "type", "read" }, "type=" + type, null,
                        "date" + " COLLATE LOCALIZED ASC");
        if (cursor != null) {
            cursor.moveToLast();
            if (cursor.getCount() > 0) {

                do {
                    String date =  cursor.getString(cursor.getColumnIndex("date"));
                    Long timestamp = Long.parseLong(date);    
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(timestamp);
                    DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.SSS");
                    SMSMessage message = new SMSMessage();
                    message.setMessageId(cursor.getString(cursor.getColumnIndex("_id")));
                    message.setMessageNumber(cursor.getString(cursor.getColumnIndex("address")));
                    message.setMessageContent(cursor.getString(cursor.getColumnIndex("body")));
                    message.setMessageDate(formatter.format(calendar.getTime()));
                    smsInbox.add(message);
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

	public static void saveValueToPreferencesIfNotExist(Context context, String valueName, String value){
		String tmpValue = Preferences.getPreferencesString(context, valueName);
		if(tmpValue == null || tmpValue.isEmpty()){
			Preferences.setPreferencesString(context, valueName, value);
		}
	}
	
	public static String getNickName(Context context){
		String nickName = null;
		String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		if(account != null && !account.isEmpty() && account.contains(CommonConst.DELIMITER_AT)){
			String[] accountParts = account.split(CommonConst.DELIMITER_AT);
			if(accountParts != null){
				nickName = accountParts[0];
			}
		}
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
	
	public static SparseArray<ContactDetails> fetchContacts(Context context, SparseArray<ContactDetails> contactDetailsGroups,
			ProgressDialog barProgressDialog) {
        String phoneNumber = null;
        ContactDetails contactDetails = null;
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
        Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null); 
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("Retrieve all contacts query: " + endTime);
        
        startTime = System.currentTimeMillis();
        
        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {
//        	contactDetailsGroups = new SparseArray<ContactDetails>();
        	int i = 0;
            while (cursor.moveToNext()) {
            	//barProgressDialog.incrementProgressBy(2);
                barProgressDialog.incrementProgressBy(1);

            	//System.out.println("Element: " + (i + 1));
            	contactDetails = new ContactDetails();
                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String contactName = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                if( contactName != null && !contactName.isEmpty()) {
	                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));
	                if (hasPhoneNumber > 0) {
	                	contactDetails.setContactName(contactName);
	                    // Query and loop for every phone number of the contact
	                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);
	                    while (phoneCursor.moveToNext()) {
	                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
	                        contactDetails.getPhoneNumbersList().add(phoneNumber);
	                    }
	                    phoneCursor.close();
	                    contactDetailsGroups.append(i, contactDetails);
	                    i++;
	//                    // Query and loop for every email of the contact
	//                    Cursor emailCursor = contentResolver.query(EmailCONTENT_URI,    null, EmailCONTACT_ID+ " = ?", new String[] { contact_id }, null);
	//                    while (emailCursor.moveToNext()) {
	//                        email = emailCursor.getString(emailCursor.getColumnIndex(DATA));
	//                        output.append("\nEmail:" + email);
	//                    }
	//                    emailCursor.close();
	                }
                }
            }
        }
        cursor.close();
        endTime = System.currentTimeMillis() - startTime;
        System.out.println("Retrieve all contacts details query and save to groups: " + endTime);
        return contactDetailsGroups;
    }

    /*
     * Send command to request contact by GCM (Google Cloud Message - push notifictation)
     * Input parematers:
     * 		Object array {Activity, Context}
     */
    public static void checkJoinRequestBySMS(Object[] objects) { 
    	// AsyncTask <TypeOfVarArgParams , ProgressValue , ResultValue>
    	
	    new AsyncTask<Object[], Void, String>() {
   	
	    	@Override
	        protected String doInBackground(Object[]... objects) {
	    	    // Read SMS messages from inbox
	    		Context ctx = null;
	    		if(objects[0][0] != null){
	    			ctx = (Context)objects[0][0];
	    		} else {
	    			// TODO: handle case if input is invalid - expected Context type
	    		}
	    		
	    		Activity activity = null;
	    		if(objects[0][1] != null){
	    			activity = (Activity)objects[0][1];
	    		} else {
	    			// TODO: handle case if input is invalid - expected Context type
	    		}
	    		
	    	    List<SMSMessage> smsList = fetchInboxSms(activity, 1);
	    	    if(smsList != null){
	    	    	for (SMSMessage smsMessage : smsList) {
	    	    		//String n = smsMessage.getMessageNumber();
						if(smsMessage != null && smsMessage.getMessageContent().contains(CommonConst.JOIN_FLAG_SMS)){
				    	    String smsMsg = smsMessage.getMessageContent();
				    	    String smsId = smsMessage.getMessageId();
				    	    System.out.println("JOIN SMS: " + smsMsg);
				    	    String[] smsParams = smsMsg.split(CommonConst.DELIMITER_COMMA);
				    	    // TODO: save all received join requests to RECEIVED_JOIN_REQUEST table
				    	    if(smsParams.length >= 4){
					    	    String phoneNumber = smsParams[3];
					    	    String mutualId = smsParams[2];
					    	    String regId = smsParams[1];
					    	    String account = smsParams[4];
					    	    if(phoneNumber != null && !phoneNumber.isEmpty() &&
					    	    	mutualId != null && !mutualId.isEmpty() &&
					    	    	regId != null && !regId.isEmpty() ){
					    			long res = DBLayer.addReceivedJoinRequest(phoneNumber, mutualId, regId, account);
					    			if(res != 1){
					    				// TODO: Notify that add ReceivedJoinRequest to DB failed...
					    				System.out.println("addReceivedJoinRequest FAILED for phoneNumber = " + phoneNumber);
					    			} else {
					    				// TODO: delete SMS that was handled
					    				String uriSms = Uri.parse(CommonConst.SMS_URI) + "/" + smsId;
					    				int count = activity.getContentResolver().delete(Uri.parse(uriSms), null, null);
					    				if(count != 1){
					    					// Log that join SMS request has not been removed
					    				}
					    	    	    // TODO: Check that join request approved and send back
					    	    	    // push notification with newly connected contact details
					    	    	    sendApproveOnJoinRequest(ctx);
					    			}
					    	    } else {
					    	    	// TODO: notify error ??? 
					    	    	System.out.println("No NULL or empty parameters accepted for mutualId , regId and phoneNumber.");
					    	    	if(phoneNumber != null && !phoneNumber.isEmpty()){
					    	    		System.out.println("phoneNumber is null or empty");
					    	    	}
					    	    	if(mutualId != null && !mutualId.isEmpty()){
					    	    		System.out.println("mutualId is null or empty");
					    	    	}
					    	    	if(regId != null && !regId.isEmpty()){
					    	    		System.out.println("regId is null or empty");
					    	    	}
					    	    }
				    	    }
						}
					}
	    	    }
	    	    return "";
	        }
	
	        @Override
	        protected void onPostExecute(String msg) {
	        	// TODO: fix return value
	        }
	    }.execute(objects);
    }

	public static void sendApproveOnJoinRequest(Context context){
		String regIDToReturnMessageTo = Controller.getRegistrationId(context);
		List<String> listRegIDs = new ArrayList<String>();
		listRegIDs.add(regIDToReturnMessageTo);
		String time = "";
		String messageString = "";
		String jsonMessage = createJsonMessage(listRegIDs, 
	    		regIDToReturnMessageTo, 
	    		CommandEnum.join_approval, 
	    		"", // messageString, 
	    		Controller.getCurrentDate(), // time,
	    		null, //NotificationCommandEnum.pushNotificationServiceStatus.toString(),
	    		null //PushNotificationServiceStatusEnum.available.toString()
				);
		sendCommand(jsonMessage);
	}
	
	public static List<String> fillContactListWithContactDeviceDataFromJSON(String jsonStringContactDeviceData){
		List<String> values = null;
	    
		ContactDeviceDataList contactDeviceDataCollection = Utils.fillContactDeviceDataListFromJSON(jsonStringContactDeviceData);
	    if(contactDeviceDataCollection == null){
	    	return null;
	    }
	
	    values = fillContactListWithContactDeviceDataFromJSON(contactDeviceDataCollection);
	    return values;
	}
	
	public static List<String> fillContactListWithContactDeviceDataFromJSON(ContactDeviceDataList contactDeviceDataCollection){
		List<String> values = null;
	    
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
	    values = new ArrayList<String>();
	    for (ContactDeviceData contactDeviceData : contactDeviceDataList) {
	    	ContactData contactData = contactDeviceData.getContactData();
	    	if(contactData != null) {
	    		String nick = contactData.getNick();
	    		if(nick != null && !nick.isEmpty()){
	    			values.add(contactData.getNick());
	    		} else {
	    			String email = contactData.getEmail();
	    			if(email != null && !email.isEmpty()) {
	    				values.add(email);
	    			} else {
		    			values.add("unknown");
		    			LogManager.LogErrorMsg("ContactList", "fillListWithContactDeviceData", "Some provided username is null - check JSON input file, element :" + (i+1));
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

	public static void sendCommand(Context context, ContactDeviceDataList contactDeviceDataList, CommandEnum command){
		String regIDToReturnMessageTo = Controller.getRegistrationId(context);
		List<String> listRegIDs = new ArrayList<String>();
		
		for (ContactDeviceData contactDeviceData : contactDeviceDataList.getContactDeviceDataList()) {
			ContactData contactData = contactDeviceData.getContactData();
			if(contactData != null){
				listRegIDs.add(contactDeviceData.getRegistration_id());
			} else {
				LogManager.LogErrorMsg("Controller", "checkGcmStatus", "Unable to get registration_ID: contactData is null.");
				Log.e("checkGcmStatus", "Unable to get registration_ID: contactData is null.");
			}
			
		}
		
		if(listRegIDs.size() > 0){
			String jsonMessage = Controller.createJsonMessage(listRegIDs, 
		    		regIDToReturnMessageTo, 
		    		command, 
		    		"", // messageString, 
		    		Controller.getCurrentDate(), // time,
		    		null, //NotificationCommandEnum.pushNotificationServiceStatus.toString(),
		    		null //PushNotificationServiceStatusEnum.available.toString()
					);
			Controller.sendCommand(jsonMessage);
		} else {
			// TODO: error to log! Unable to send command: checkGcmStatus
		}
	}
	
	public static ContactDeviceDataList removeNonSelectedContacts(ContactDeviceDataList contactDeviceDataList, 
		List<String> selectedContcatList){
		
		if(selectedContcatList.size() == 0){
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
}

