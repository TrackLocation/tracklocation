package com.doat.tracklocation.service;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.doat.tracklocation.Controller;
//import com.doat.tracklocation.MainActivity;
import com.doat.tracklocation.MapActivity;
import com.doat.tracklocation.crypto.CryptoUtils;
import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.NotificationBroadcastData;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.google.gson.Gson;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private String className;
    private String logMessage;
    private String methodName;
    String smsPhoneNumber;
    
	@Override
	public void onReceive(Context context, Intent intent) {
        className = this.getClass().getName();

        methodName = "onReceive";
        LogManager.LogFunctionCall(className, methodName);
        Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

        String action = intent.getAction();
		
		if (action.equals(ACTION_SMS_RECEIVED)) {

			String smsMessageBody = "";

			SmsMessage[] msgs = getMessagesFromIntent(intent);
			if (msgs != null) {
				for (int i = 0; i < msgs.length; i++) {
					smsMessageBody += msgs[i].getMessageBody().toString();
					smsMessageBody += "\n";
					smsPhoneNumber = ((SmsMessage)msgs[i]).getOriginatingAddress();
				}
			}
			
			try {
				if(!smsMessageBody.isEmpty() && (smsMessageBody.contains(CommonConst.JOIN_SMS_PREFIX) || smsMessageBody.contains(CommonConst.OLD_JOIN_SMS_PREFIX))){
					if(smsMessageBody.contains(CommonConst.JOIN_SMS_PREFIX)){
						smsMessageBody = CryptoUtils.decodeBase64(smsMessageBody.substring(CommonConst.JOIN_SMS_PREFIX.length(), smsMessageBody.length()));
					} else if (smsMessageBody.contains(CommonConst.OLD_JOIN_SMS_PREFIX)){
						smsMessageBody = CryptoUtils.decodeBase64(smsMessageBody.substring(CommonConst.OLD_JOIN_SMS_PREFIX.length(), smsMessageBody.length()));
					}
				} else {
					LogManager.LogFunctionExit(className, methodName);
					Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
					return;
				}
			} catch (UnsupportedEncodingException e) {
				LogManager.LogException(e, className, methodName);
				Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());

				LogManager.LogFunctionExit(className, methodName);
				Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
				return;
			}
						
			String[] smsParams = smsMessageBody.split(CommonConst.DELIMITER_COMMA);
			
			if(smsParams.length > 0 && smsParams[0].equals(CommonConst.JOIN_FLAG_SMS)){
				
			    if(smsParams.length == CommonConst.JOIN_SMS_PARAMS_NUMBER){
			    	
		    	    String phoneNumberFromSMS = smsParams[3];
		    	    if(phoneNumberFromSMS == null || phoneNumberFromSMS.isEmpty()){
		    	    	phoneNumberFromSMS = smsPhoneNumber;
		    	    }
		    	    String mutualIdFromSMS = smsParams[2];
		    	    String regIdFromSMS = smsParams[1];
		    	    String accountFromSMS = smsParams[4];
		    	    String macAddressFromSMS = smsParams[5];
				
		    	    logMessage = "Handling of join request SMS received from [" + accountFromSMS + "]";
		    	    LogManager.LogInfoMsg(className, methodName, logMessage);
		    	    Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

	    	    	// Save contact details received by join requests to RECEIVED_JOIN_REQUEST table
	    			long res = DBLayer.getInstance().addReceivedJoinRequest(phoneNumberFromSMS, mutualIdFromSMS, regIdFromSMS, accountFromSMS, macAddressFromSMS);
	    			if(res == -1 || res == 0){
	    	        	logMessage = "Add received join request FAILED for phoneNumber = " + phoneNumberFromSMS;
	    	            Log.e(CommonConst.LOG_TAG, logMessage);
	    	            LogManager.LogErrorMsg(className, methodName, logMessage);
	    			}
	    		} else { 
	    			logMessage = "JOIN SMS Message has incorrect parameters number" +
	    				" - supposed to be: " + CommonConst.JOIN_SMS_PARAMS_NUMBER;
	    			LogManager.LogErrorMsg(className, methodName, logMessage);
	    			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
	    		}
    			
			    // Send a broadcast intent to update the SMS received in the activity
				Intent intentMapActivity = getIntent(context, MapActivity.class);
				Log.i(CommonConst.LOG_TAG, "intentMainActivity = " + intentMapActivity);
				if(MapActivity.isTrackLocationRunning == false){
		        	Intent mainActivity = new Intent(context, MapActivity.class);
		        	mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    		context.startActivity(mainActivity);
				} else {					
					Gson gson = new Gson();
					NotificationBroadcastData notificationBroadcastData = new NotificationBroadcastData();
					notificationBroadcastData.setMessage("message");
					notificationBroadcastData.setKey(BroadcastKeyEnum.join_sms.toString());
					notificationBroadcastData.setValue(smsMessageBody);
					String jsonNotificationBroadcastData = gson.toJson(notificationBroadcastData);
					
					// Broadcast corresponding message
					Controller.broadcastMessage(context, 
						BroadcastActionEnum.BROADCAST_MESSAGE.toString(), 
						"SMSReceiver",
						jsonNotificationBroadcastData, 
						BroadcastKeyEnum.join_sms.toString(), 
						"value");
				}
			}
		}
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	private static Intent getIntent(Context context, Class<?> cls) {
	    Intent intent = new Intent(context, cls);
	    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    return intent;
	}
	
	public boolean isRunning(Context ctx) {
		ActivityManager activityManager = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = activityManager
				.getRunningTasks(Integer.MAX_VALUE);

		for (RunningTaskInfo task : tasks) {
			if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
			return true;
		}

		return false;
	}
	
    public static SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];

        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }
}
