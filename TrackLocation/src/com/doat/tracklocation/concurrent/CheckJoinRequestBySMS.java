package com.doat.tracklocation.concurrent;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.context.ApproveJoinRequestContext;
import com.doat.tracklocation.crypto.CryptoUtils;
import com.doat.tracklocation.datatype.SMSMessage;
import com.doat.tracklocation.datatype.SMSMessageList;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.dialog.ApproveJoinRequestDialog;
import com.doat.tracklocation.dialog.ApproveJoinRequestDialogListener;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.SMSUtils;

public class CheckJoinRequestBySMS implements Runnable {

	private String className; // className = this.getClass().getName();
	private String logMessage;
	private String methodName;
	private Context ctx;
	private Activity activity;
	private long threadId;
	
    /*
     * Check if received SMS with JOIN REQUEST
     * 
     * If join request approved:
     *  1 - Save "requester" contact details into DB (CONTACT_DATA, DEVICE_DATA, CONTACT_DEVICE_DATA tables)
     *  2 - Send approve command to "requester" contact by GCM (Google Cloud Message - push notifictation)
     *      with owner contact details
     *      
     * Input parematers:
     * 		Object array {Activity, Context}
     */
	public CheckJoinRequestBySMS(Context context, Activity activity) {
		super();
		this.ctx = context;
		this.activity = activity;
		className = this.getClass().getName();
		threadId = Thread.currentThread().getId();
		methodName = "CheckJoinRequestBySMS" + " -> ThreadID: " + threadId;
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

	public void handleSms(Context ctx, SMSMessage smsMessage){
		threadId = Thread.currentThread().getId();
		methodName = "handleSms" + " -> ThreadID: " + threadId;
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
	    String smsPhoneNumber = smsMessage.getMessageNumber();
	    
	    String smsMsg = smsMessage.getMessageContent();
	    String[] smsParams = smsMsg.split(CommonConst.DELIMITER_COMMA);
	    
	    if(smsParams.length == CommonConst.JOIN_SMS_PARAMS_NUMBER){
	    	
    	    String phoneNumberFromSMS = smsParams[3];
    	    if(phoneNumberFromSMS == null || phoneNumberFromSMS.isEmpty()){
    	    	phoneNumberFromSMS = smsPhoneNumber;
    	    }
    	    String mutualIdFromSMS = smsParams[2];
    	    String regIdFromSMS = smsParams[1];
    	    String accountFromSMS = smsParams[4];
    	    String macAddressFromSMS = smsParams[5];
    	    
    	    if(phoneNumberFromSMS != null && !phoneNumberFromSMS.isEmpty() &&
    	    	mutualIdFromSMS != null && macAddressFromSMS != null && !mutualIdFromSMS.isEmpty() &&
    	    	regIdFromSMS != null && !regIdFromSMS.isEmpty() && !macAddressFromSMS.isEmpty()){
    	    	
    	    	// Save contact details received by join requests to RECEIVED_JOIN_REQUEST table
    			long res = DBLayer.getInstance().addReceivedJoinRequest(phoneNumberFromSMS, mutualIdFromSMS, regIdFromSMS, accountFromSMS, macAddressFromSMS);
    			if(res == -1 || res == 0){
    	        	logMessage = "Add received join request FAILED for phoneNumber = " + phoneNumberFromSMS;
    	            Log.e(CommonConst.LOG_TAG, logMessage);
    	            LogManager.LogErrorMsg(className, methodName, logMessage);
    			} else {
    				// Starting Android 4.4 - only default SMS application can delete SMS
    				// Delete SMS that was handled - deprecated action
    				// String uriSms = Uri.parse(CommonConst.SMS_URI) + "/" + smsId;
    				// int count = activity.getContentResolver().delete(Uri.parse(uriSms), 
    				//	"date=?", new String[] { smsDate });
    				// if(count != 1){
    				//	  // Log that join SMS request has not been removed
	    	        //	  logMessage = "Failed to delete join request SMS";
	    	        //    Log.e(CommonConst.LOG_TAG, logMessage);
	    	        //    LogManager.LogErrorMsg(className, "checkJoinRequestBySMS", logMessage);
	    	        //    
	    	        //    ((MainActivity) activity).getMainActivityController().saveHandledSmsDetails(ctx, smsMessage);
    				//}
    				
    				// Show Approve Join Request Dialog only if called by initCont (init process)
    				// DO not show if called by broadcast invoked by SMSReceiver
    	    	    // Check that join request approved and send back by
    	    	    // push notification (GCM) owner contact details
    				logMessage = "Show 'ApproveJoinRequestDialog' from thread...";
    				Log.i(CommonConst.LOG_TAG, logMessage);
    				LogManager.LogInfoMsg(className, methodName, logMessage);

    				ApproveJoinRequestContext approveJoinRequestContext = 
    					new ApproveJoinRequestContext(ctx, mutualIdFromSMS);
    					
					ApproveJoinRequestDialogListener approveJoinRequestDialogListener = 
						new ApproveJoinRequestDialogListener(approveJoinRequestContext, smsMessage);
    					
					ApproveJoinRequestDialog approveJoinRequestDialog = 
						new ApproveJoinRequestDialog(activity, ctx, approveJoinRequestDialogListener);
					approveJoinRequestDialog.showApproveJoinRequestDialog(
						activity, 
						ctx, 
						accountFromSMS,
						phoneNumberFromSMS, 
						mutualIdFromSMS,
						regIdFromSMS,
						macAddressFromSMS,
						smsMessage);
				}
    	    } else {
	        	logMessage = "No NULL or empty parameters accepted for mutualId , regId, " + 
	    	    	"macAddress and phoneNumber.";
	            Log.e(CommonConst.LOG_TAG, logMessage);
	            LogManager.LogErrorMsg(className, methodName, logMessage);
    	    	if(phoneNumberFromSMS != null && !phoneNumberFromSMS.isEmpty()){
    	        	logMessage = "phoneNumber is null or empty";
    	            Log.e(CommonConst.LOG_TAG, logMessage);
    	            LogManager.LogErrorMsg(className, methodName, logMessage);
    	    	}
    	    	if(mutualIdFromSMS != null && !mutualIdFromSMS.isEmpty()){
    	        	logMessage = "mutualId is null or empty";
    	            Log.e(CommonConst.LOG_TAG, logMessage);
    	            LogManager.LogErrorMsg(className, methodName, logMessage);
    	    	}
    	    	if(regIdFromSMS != null && !regIdFromSMS.isEmpty()){
    	        	logMessage = "regId is null or empty";
    	            Log.e(CommonConst.LOG_TAG, logMessage);
    	            LogManager.LogErrorMsg(className, methodName, logMessage);
    	    	}
    	    	if(macAddressFromSMS != null && !macAddressFromSMS.isEmpty()){
    	        	logMessage = "macAddress is null or empty";
    	            Log.e(CommonConst.LOG_TAG, logMessage);
    	            LogManager.LogErrorMsg(className, methodName, logMessage);
    	    	}
    	    }
		} else { 
			logMessage = "JOIN SMS Message has incorrect parameters number" +
				" - supposed to be: " + CommonConst.JOIN_SMS_PARAMS_NUMBER;
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
	    
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	@Override
	public void run() {
		threadId = Thread.currentThread().getId();
		methodName = "run" + " -> ThreadID: " + threadId;
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
	    // Read SMS messages from inbox
		// Fetch all SMS 
	    SMSMessageList smsList = Controller.fetchInboxSms(activity, 1);
	    if(smsList != null && smsList.getSmsMessageList() != null){
	    	for (SMSMessage smsMessage : smsList.getSmsMessageList()) {
	    		
	    		String smsMessageContent = smsMessage.getMessageContent();
	    		try {
					if(!smsMessageContent.isEmpty() && (smsMessageContent.contains(CommonConst.JOIN_SMS_PREFIX) || smsMessageContent.contains(CommonConst.OLD_JOIN_SMS_PREFIX))){
						if(smsMessageContent.contains(CommonConst.JOIN_SMS_PREFIX)){
							smsMessageContent = CryptoUtils.decodeBase64(smsMessageContent.substring(CommonConst.JOIN_SMS_PREFIX.length(), smsMessageContent.length()));
						} else if (smsMessageContent.contains(CommonConst.OLD_JOIN_SMS_PREFIX)){
							smsMessageContent = CryptoUtils.decodeBase64(smsMessageContent.substring(CommonConst.OLD_JOIN_SMS_PREFIX.length(), smsMessageContent.length()));
						}
						smsMessage.setMessageContent(smsMessageContent);
						
						// Check if there SMS with JOIN REQUEST from TrackLocation application
						if(smsMessageContent != null && (smsMessageContent.contains(CommonConst.JOIN_SMS_PREFIX) || smsMessageContent.contains(CommonConst.OLD_JOIN_SMS_PREFIX))){
				    	    if(SMSUtils.isHandledSmsDetails(ctx, smsMessage)){
				    	    	continue;
				    	    }
				    	    
				    	    // save the SMS as handled
				    		if(smsMessage != null){
				    			SMSUtils.saveHandledSmsDetails(ctx, smsMessage);
				    		}

			    			handleSms(ctx, smsMessage);
						}
					} else {
						continue;
					}
				} catch (UnsupportedEncodingException e) {
					LogManager.LogException(e, className, methodName);
					Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
				}
			}
	    }
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
}
