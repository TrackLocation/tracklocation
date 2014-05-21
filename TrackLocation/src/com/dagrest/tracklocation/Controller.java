package com.dagrest.tracklocation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Patterns;

import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.Message;
import com.dagrest.tracklocation.datatype.MessageData;
import com.dagrest.tracklocation.http.HttpUtils;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.google.gson.Gson;

public class Controller {

	public static String generateUUID(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	public String createJsonMessage(List<String> listRegIDs, 
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
    public void sendCommand(final String jsonMessage){ 
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
	        	int i = 0;
	        }
	    }.execute(null, null, null);
    }

	public static String getCurrentDate(){
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.US);
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}

	public List<String> getUsernameList(Context context){
	    Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
	    AccountManager accountManager = AccountManager.get(context); 
	    //Account[] accounts = manager.getAccountsByType("com.google"); 
	    Account[] accounts = accountManager.getAccounts();
	    List<String> possibleEmails = new ArrayList<String>();

	    for (Account account : accounts) {
	        if (emailPattern.matcher(account.name).matches() && !possibleEmails.contains(account.name)) {
        		possibleEmails.add(account.name);
	        }
	    }

	    if(!possibleEmails.isEmpty()){
	    	return possibleEmails;
	    }else
	        return null;
	}
}
