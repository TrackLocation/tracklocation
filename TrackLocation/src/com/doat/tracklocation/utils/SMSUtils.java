package com.doat.tracklocation.utils;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.doat.tracklocation.concurrent.CheckJoinRequestBySMS;
import com.doat.tracklocation.context.ApproveJoinRequestContext;
import com.doat.tracklocation.datatype.AppInstDetails;
import com.doat.tracklocation.datatype.NotificationBroadcastData;
import com.doat.tracklocation.datatype.SMSMessage;
import com.doat.tracklocation.datatype.SMSMessageList;
import com.doat.tracklocation.dialog.ApproveJoinRequestDialog;
import com.doat.tracklocation.dialog.ApproveJoinRequestDialogListener;
import com.doat.tracklocation.log.LogManager;
import com.google.gson.Gson;

public class SMSUtils {
	
	public static final String className = "com.doat.tracklocation.utils.SMSUtils";
	
	public static void checkJoinRequestBySMSInBackground(Context context, Activity activity, boolean isBySmsReceiver){
		
		Thread checkJoinRequestBySMSInBackgroundThread;
		Runnable checkJoinRequestBySMSInBackground;
		String methodName = "checkJoinRequestBySMSInBackground";
		String logMessage;
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		checkJoinRequestBySMSInBackground = new CheckJoinRequestBySMS(context, activity);
		try {
			checkJoinRequestBySMSInBackgroundThread = new Thread(checkJoinRequestBySMSInBackground);
			logMessage = "Started a separate thread to check Join request by SMS";
			Log.i(CommonConst.LOG_TAG, logMessage);
			LogManager.LogInfoMsg(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> initCont()", logMessage);
			checkJoinRequestBySMSInBackgroundThread.start();
		} catch (IllegalThreadStateException e) {
			logMessage = "Check Join request by SMS in background thread was started already";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
		}

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	public static void saveHandledSmsDetails(Context ctx, SMSMessage smsMessage) {
		String methodName = "saveHandledSmsDetails";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		Gson gson = new Gson();
		SMSMessageList handledSmsList = null;
		String jsonHandledSmsList = Preferences.getPreferencesString(ctx,
				CommonConst.PREFERENCES_HANDLED_SMS_LIST);
		if (jsonHandledSmsList == null || jsonHandledSmsList.isEmpty()) {
			handledSmsList = new SMSMessageList();
		} else {
			handledSmsList = gson.fromJson(jsonHandledSmsList,
					SMSMessageList.class);
		}
		if (handledSmsList != null && !isContain(handledSmsList, smsMessage)) {
			handledSmsList.getSmsMessageList().add(smsMessage);
			String jsonHandledSmsListNew = gson.toJson(handledSmsList);
			Preferences.setPreferencesString(ctx,
					CommonConst.PREFERENCES_HANDLED_SMS_LIST,
					jsonHandledSmsListNew);
			String logMessage = "Save SMS as handled.";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		}
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

	public static boolean isContain(SMSMessageList handledSmsList,
			SMSMessage smsMessage) {
		if (handledSmsList == null) {
			return false;
		}
		if (smsMessage == null) {
			return false;
		}
		long smsMessageDateLong = Long.parseLong(smsMessage.getMessageDate());
		List<SMSMessage> list = handledSmsList.getSmsMessageList();
		if (list != null) {
			for (SMSMessage smsMessageEntity : list) {
				if (smsMessageEntity != null
						&& smsMessageEntity.getMessageDate().equals(
								smsMessage.getMessageDate())
						&& smsMessageEntity.getMessageId().equals(
								smsMessage.getMessageId())) {
					return true;
				}
				long smsMessageEntityDateLongFromList = Long
						.parseLong(smsMessageEntity.getMessageDate());
				// Check that smsMessage from list (handled one) has greater
				// timestamp than smsMessage
				// that currently checking (if handled) - consider such
				// smsMessage as handled
				if (smsMessageDateLong < smsMessageEntityDateLongFromList) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isHandledSmsDetails(Context ctx, SMSMessage smsMessage) {
		Gson gson = new Gson();
		SMSMessageList handledSmsList = null;
		if (smsMessage == null) {
			return true;
		}

		// If installation date(timestamp) of application id greater
		// then SMS date(timestamp) - consider SMS as handled
		AppInstDetails appInstDetails = new AppInstDetails(ctx);
		long appInstTimestamp = appInstDetails.getTimestamp();
		String smsMessageDate = smsMessage.getMessageDate();
		if (smsMessageDate != null && !smsMessageDate.isEmpty()) {
			long longSmsMessageTimestamp = Long.parseLong(smsMessageDate);
			if (appInstTimestamp > longSmsMessageTimestamp) {
				return true;
			}
		}

		String jsonHandledSmsList = Preferences.getPreferencesString(ctx,
				CommonConst.PREFERENCES_HANDLED_SMS_LIST);
		if (jsonHandledSmsList != null && !jsonHandledSmsList.isEmpty()) {
			handledSmsList = gson.fromJson(jsonHandledSmsList,
					SMSMessageList.class);
			if (handledSmsList != null && isContain(handledSmsList, smsMessage)) {
				return true;
			}
		}
		return false;
	}

//	public static void showApproveJoinRequestDialog(Context context, Activity activity, 
//			NotificationBroadcastData broadcastData){
//		String methodName = "showApproveJoinRequestDialog";
//		String logMessage;
//		LogManager.LogFunctionCall(className, methodName);
//		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
//		
//		//  SMS Message values:
//		//	[0] - smsMessageKey - "JOIN_TRACK_LOCATION"
//		//	[1] - regIdFromSMS
//		//	[2] - mutualIdFromSMS
//		//	[3] - phoneNumberFromSMS
//		//	[4] - accountFromSMS
//		//	[5] - macAddressFromSMS
//		String smsMessageText = broadcastData.getValue();
//		
//		String smsVals[] = smsMessageText.split(",");
//		if(smsVals.length == CommonConst.JOIN_SMS_PARAMS_NUMBER){ // should be exactly 6 values
//			
//			ApproveJoinRequestContext approveJoinRequestContext = 
//				new ApproveJoinRequestContext(context, smsVals[2].trim());
//			
//			ApproveJoinRequestDialogListener approveJoinRequestDialogListener = 
//				new ApproveJoinRequestDialogListener(approveJoinRequestContext, null);
//			
//			ApproveJoinRequestDialog approveJoinRequestDialog = 
//				new ApproveJoinRequestDialog(activity, context, approveJoinRequestDialogListener);
//			approveJoinRequestDialog.showApproveJoinRequestDialog(
//					activity, 
//					context, 
//					smsVals[4].trim(), 	// accountFromSMS
//					smsVals[3].trim(), 	// phoneNumberFromSMS 
//					smsVals[2].trim(), 	// mutualIdFromSMS  
//					smsVals[1].trim(), 	// regIdFromSMS 
//					smsVals[5].trim(),	// macAddressFromSMS 
//					null);
//		} else {
//			logMessage = "JOIN SMS Message has incorrect parameters number" +
//				" - supposed to be: " + CommonConst.JOIN_SMS_PARAMS_NUMBER;
//			LogManager.LogErrorMsg(className, methodName, logMessage);
//			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
//		
//			logMessage = methodName + " -> Backup process failed.";
//			LogManager.LogErrorMsg(className, methodName, logMessage);
//			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
//		}
//		
//		LogManager.LogFunctionExit(className, methodName);
//		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
//	}

}
