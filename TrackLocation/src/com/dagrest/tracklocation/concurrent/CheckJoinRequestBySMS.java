package com.dagrest.tracklocation.concurrent;

import android.app.Activity;
import android.content.Context;
// import android.net.Uri;
import android.util.Log;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.datatype.SMSMessage;
import com.dagrest.tracklocation.datatype.SMSMessageList;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
//import com.google.gson.Gson;

public class CheckJoinRequestBySMS implements Runnable {

	private String className; // className = this.getClass().getName();
	private String logMessage;
	private String methodName;
	private Context ctx;
	private Activity activity;
	
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
		this.activity =activity;
		className = this.getClass().getName();
	}

	@Override
	public void run() {
		methodName = "run";
		//Gson gson = new Gson();
	    // Read SMS messages from inbox
		// Fetch all SMS 
	    SMSMessageList smsList = Controller.fetchInboxSms(activity, 1);
	    if(smsList != null && smsList.getSmsMessageList() != null){
	    	for (SMSMessage smsMessage : smsList.getSmsMessageList()) {
	    		//String n = smsMessage.getMessageNumber();
	    		// Check if there SMS with JOIN REQUEST from TrackLocation application
				if(smsMessage != null && smsMessage.getMessageContent().contains(CommonConst.JOIN_FLAG_SMS)){
		    	    String smsMsg = smsMessage.getMessageContent();
		    	    // String smsId = smsMessage.getMessageId();
		    	    String smsPhoneNumber = smsMessage.getMessageNumber();
		    	    //String smsDate = smsMessage.getMessageDate();
		    	    
		    	    if(Controller.isHandledSmsDetails(ctx, smsMessage)){
		    	    	continue;
		    	    }
		    	    
		    	    String[] smsParams = smsMsg.split(CommonConst.DELIMITER_COMMA);
		    	    
		    	    if(smsParams.length >= 4){
		    	    	
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
			    			long res = DBLayer.addReceivedJoinRequest(phoneNumberFromSMS, mutualIdFromSMS, regIdFromSMS, accountFromSMS, macAddressFromSMS);
			    			if(res != 1){
			    	        	logMessage = "Add received join request FAILED for phoneNumber = " + phoneNumberFromSMS;
			    	            Log.e(CommonConst.LOG_TAG, logMessage);
			    	            LogManager.LogErrorMsg(className, methodName, logMessage);
			    			} else {
			    				// Starting Android 4.4 - only default SMS application can delete SMS
			    				// Delete SMS that was handled - deprecated action
			    				// String uriSms = Uri.parse(CommonConst.SMS_URI) + "/" + smsId;
			    				// int count = activity.getContentResolver().delete(Uri.parse(uriSms), 
			    				//	"date=?", new String[] { smsDate });
			    				int count = 0;
			    				if(count != 1){
			    					// Log that join SMS request has not been removed
				    	        	logMessage = "Failed to delete join request SMS";
				    	            Log.e(CommonConst.LOG_TAG, logMessage);
				    	            LogManager.LogErrorMsg(className, "checkJoinRequestBySMS", logMessage);
				    	            
				    	            Controller.saveHandledSmsDetails(ctx, smsMessage);
				    	            
//					    	            List handledSmsList = null;
//					    	            String jsonHandledSmsList = 
//					    	            	Preferences.getPreferencesString(ctx, CommonConst.PREFERENCES_HANDLED_SMS_LIST);
//					    	            if(jsonHandledSmsList == null || jsonHandledSmsList.isEmpty()){
//					    	            	handledSmsList = new ArrayList();
//					    	            } else {
//					    	            	handledSmsList = gson.fromJson(jsonHandledSmsList, List.class);
//					    	            	if(handledSmsList != null && !handledSmsList.contains(smsId)){
//					    	            		handledSmsList.add(smsId);
//					    	            		String jsonHandledSmsListNew = gson.toJson(handledSmsList);
//					    	            		Preferences.setPreferencesString(ctx, 
//					    	            			CommonConst.PREFERENCES_HANDLED_SMS_LIST, jsonHandledSmsListNew);
//					    	            	}
//					    	            }
			    				}
			    	    	    // Check that join request approved and send back by
			    	    	    // push notification (GCM) owner contact details
			    				Controller.showApproveJoinRequestDialog(activity, ctx, accountFromSMS, phoneNumberFromSMS, mutualIdFromSMS, regIdFromSMS, macAddressFromSMS);
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
		    	    }
				}
			}
	    }
	}
}
