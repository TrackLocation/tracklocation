package com.doat.tracklocation.concurrent;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.R;
import com.doat.tracklocation.context.ApproveJoinRequestContext;
import com.doat.tracklocation.crypto.CryptoUtils;
import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.datatype.CommandData;
import com.doat.tracklocation.datatype.CommandDataBasic;
import com.doat.tracklocation.datatype.CommandEnum;
import com.doat.tracklocation.datatype.CommandKeyEnum;
import com.doat.tracklocation.datatype.CommandValueEnum;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.datatype.MessageDataLocation;
import com.doat.tracklocation.datatype.ReceivedJoinRequestData;
import com.doat.tracklocation.datatype.SMSMessage;
import com.doat.tracklocation.datatype.SMSMessageList;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.dialog.ApproveAddContactRequestDialog;
import com.doat.tracklocation.dialog.CommonDialog;
import com.doat.tracklocation.exception.UnableToSendCommandException;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.SMSUtils;
import com.doat.tracklocation.utils.Utils;

public class CheckJoinRequestBySMS implements Runnable {

	private String className; // className = this.getClass().getName();
	private String logMessage;
	private String methodName;
	private Context ctx;
	private Activity activity;
	private long threadId;
	private Handler handler;
	
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
	public CheckJoinRequestBySMS(Context context, Activity activity, Handler handler) {
		super();
		this.ctx = context;
		this.activity = activity;
		this.handler = handler;
		className = this.getClass().getName();
		threadId = Thread.currentThread().getId();
		methodName = "CheckJoinRequestBySMS" + " -> ThreadID: " + threadId;
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG_SMS, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG_SMS, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

	public void handleSms(Context ctx, Activity activity, SMSMessage smsMessage, Handler handler){
		threadId = Thread.currentThread().getId();
		methodName = "handleSms" + " -> ThreadID: " + threadId;
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG_SMS, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
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
    	            Log.e(CommonConst.LOG_TAG_SMS, logMessage);
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
	    	        //    Log.e(CommonConst.LOG_TAG_SMS, logMessage);
	    	        //    LogManager.LogErrorMsg(className, "checkJoinRequestBySMS", logMessage);
	    	        //    
	    	        //    ((MainActivity) activity).getMainActivityController().saveHandledSmsDetails(ctx, smsMessage);
    				//}
    				
    				// Show Approve Join Request Dialog only if called by initCont (init process)
    				// DO not show if called by broadcast invoked by SMSReceiver
    	    	    // Check that join request approved and send back by
    	    	    // push notification (GCM) owner contact details
    				logMessage = "Show 'ApproveJoinRequestDialog' from thread...";
    				Log.i(CommonConst.LOG_TAG_SMS, logMessage);
    				LogManager.LogInfoMsg(className, methodName, logMessage);

//    				ApproveJoinRequestContext approveJoinRequestContext = 
//    					new ApproveJoinRequestContext(ctx, mutualIdFromSMS);

// OLD STYLE DIALOG - replaced by: ApproveAddContactRequestDialog			
//					ApproveJoinRequestDialogListener approveJoinRequestDialogListener = 
//						new ApproveJoinRequestDialogListener(approveJoinRequestContext, smsMessage);
//    					
//					ApproveJoinRequestDialog approveJoinRequestDialog = 
//						new ApproveJoinRequestDialog(activity, ctx, approveJoinRequestDialogListener);
//					approveJoinRequestDialog.showApproveJoinRequestDialog(
//						activity, 
//						ctx, 
//						accountFromSMS,
//						phoneNumberFromSMS, 
//						mutualIdFromSMS,
//						regIdFromSMS,
//						macAddressFromSMS,
//						smsMessage);
					
//    				ApproveJoinRequestContext approveJoinRequestContext = 
//    						new ApproveJoinRequestContext(ctx, mutualIdFromSMS);
//					AddContactDialogPositiveOnClickListener addContactDialogPositiveOnClickListener = 
//						new AddContactDialogPositiveOnClickListener(approveJoinRequestContext);
//					AddContactDialogNegativeOnClickListener addContactDialogNegativeOnClickListener = 
//						new AddContactDialogNegativeOnClickListener(approveJoinRequestContext);
//					ApproveAddContactRequestDialog approveAddContactRequestDialog = 
//						new ApproveAddContactRequestDialog(ctx, accountFromSMS, phoneNumberFromSMS, 
//							addContactDialogPositiveOnClickListener, addContactDialogNegativeOnClickListener);
    				RunnableDialog runnableDialog = new RunnableDialog(ctx, activity, 
    					mutualIdFromSMS, accountFromSMS, phoneNumberFromSMS);
    				handler.post(runnableDialog);
//					handler.post(new Runnable() { // This thread runs in the UI
//	                    @Override
//	                    public void run() {
//	                        //progress.setProgress("anything"); // Update the UI
//	        				ApproveJoinRequestContext approveJoinRequestContext = 
//	            					new ApproveJoinRequestContext(ctx, mutualIdFromSMS);
//	    					AddContactDialogPositiveOnClickListener addContactDialogPositiveOnClickListener = 
//	    							new AddContactDialogPositiveOnClickListener(approveJoinRequestContext);
//	    					AddContactDialogNegativeOnClickListener addContactDialogNegativeOnClickListener = 
//	    							new AddContactDialogNegativeOnClickListener(approveJoinRequestContext);
//	    					ApproveAddContactRequestDialog approveAddContactRequestDialog = 
//							new ApproveAddContactRequestDialog(ctx, accountFromSMS, phoneNumberFromSMS, 
//								addContactDialogPositiveOnClickListener, addContactDialogNegativeOnClickListener);
//	                    }
//	                });					
				}
    	    } else {
	        	logMessage = "No NULL or empty parameters accepted for mutualId , regId, " + 
	    	    	"macAddress and phoneNumber.";
	            Log.e(CommonConst.LOG_TAG_SMS, logMessage);
	            LogManager.LogErrorMsg(className, methodName, logMessage);
    	    	if(phoneNumberFromSMS != null && !phoneNumberFromSMS.isEmpty()){
    	        	logMessage = "phoneNumber is null or empty";
    	            Log.e(CommonConst.LOG_TAG_SMS, logMessage);
    	            LogManager.LogErrorMsg(className, methodName, logMessage);
    	    	}
    	    	if(mutualIdFromSMS != null && !mutualIdFromSMS.isEmpty()){
    	        	logMessage = "mutualId is null or empty";
    	            Log.e(CommonConst.LOG_TAG_SMS, logMessage);
    	            LogManager.LogErrorMsg(className, methodName, logMessage);
    	    	}
    	    	if(regIdFromSMS != null && !regIdFromSMS.isEmpty()){
    	        	logMessage = "regId is null or empty";
    	            Log.e(CommonConst.LOG_TAG_SMS, logMessage);
    	            LogManager.LogErrorMsg(className, methodName, logMessage);
    	    	}
    	    	if(macAddressFromSMS != null && !macAddressFromSMS.isEmpty()){
    	        	logMessage = "macAddress is null or empty";
    	            Log.e(CommonConst.LOG_TAG_SMS, logMessage);
    	            LogManager.LogErrorMsg(className, methodName, logMessage);
    	    	}
    	    }
		} else { 
			logMessage = "JOIN SMS Message has incorrect parameters number" +
				" - supposed to be: " + CommonConst.JOIN_SMS_PARAMS_NUMBER;
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG_SMS, "[ERROR] {" + className + "} -> " + logMessage);
		}
	    
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG_SMS, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	@Override
	public void run() {
		threadId = Thread.currentThread().getId();
		methodName = "run" + " -> ThreadID: " + threadId;
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG_SMS, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
	    // Read SMS messages from inbox
		// Fetch all SMS 
	    SMSMessageList smsList = Controller.fetchInboxSms(activity, 1);
	    if(smsList != null && smsList.getSmsMessageList() != null){
	    	boolean isFirstAppStartWithOneContact = Utils.isFirstAppStart(ctx) && Utils.isOnlyOneContact(ctx); 
	    	SMSMessage newestSmsMessageOnFirstAppStart = new SMSMessage();
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
						
						Log.i(CommonConst.LOG_TAG_SMS, "Check if there SMS with JOIN REQUEST from TrackLocation application");
						
						// Check if there SMS with JOIN REQUEST from TrackLocation application
						if(smsMessageContent != null && (smsMessageContent.contains(CommonConst.JOIN_FLAG_SMS))){
							
							if(isFirstAppStartWithOneContact){
								// Get the newest Add Contact SMS request and add contact
								
								String smsMessageDate = smsMessage.getMessageDate();
								
								// Debug info ONLY:
								// Log all SMS from application on the phone
								Long nextSMStimestamp = Long.parseLong(smsMessageDate);
								Date date = new Date(nextSMStimestamp);
								Log.i(CommonConst.LOG_TAG_SMS, "Next SMS date: " + date.toString() + 
									" : " + smsMessage.getMessageNumber());
								
								if(smsMessageDate == null || smsMessageDate.isEmpty()){
									continue;
								}
								
								if(newestSmsMessageOnFirstAppStart.getMessageDate() == null){
									newestSmsMessageOnFirstAppStart = smsMessage;
								} else if (Long.parseLong(smsMessageDate) > Long.parseLong(newestSmsMessageOnFirstAppStart.getMessageDate())) {
									newestSmsMessageOnFirstAppStart = smsMessage;
								}
								
							} else {
					    	    if(SMSUtils.isHandledSmsDetails(ctx, smsMessage)){
					    	    	Log.i(CommonConst.LOG_TAG_SMS, "Already handled");
					    	    	continue;
					    	    }
					    	    
					    	    // save the SMS as handled
					    		if(smsMessage != null){
					    			Log.i(CommonConst.LOG_TAG_SMS, "save the SMS as handled");
					    			SMSUtils.saveHandledSmsDetails(ctx, smsMessage);
					    		}

				    			handleSms(ctx, activity, smsMessage, handler);
							}
						}
					} else {
						continue;
					}
				} catch (UnsupportedEncodingException e) {
					LogManager.LogException(e, className, methodName);
					Log.e(CommonConst.LOG_TAG_SMS, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
				}
			}
	    	if(isFirstAppStartWithOneContact){
	    		Log.i(CommonConst.LOG_TAG_SMS, "save the SMS as handled");
	    		if(newestSmsMessageOnFirstAppStart.getMessageId() != null){
	    			SMSUtils.saveHandledSmsDetails(ctx, newestSmsMessageOnFirstAppStart);	    		
	    			handleSms(ctx, activity, newestSmsMessageOnFirstAppStart, handler);
	    			
					// Debug info ONLY:
					// Log a newest SMS from application on the phone
					Long nextSMStimestamp = Long.parseLong(newestSmsMessageOnFirstAppStart.getMessageDate());
					Date date = new Date(nextSMStimestamp);
					Log.i(CommonConst.LOG_TAG_SMS, "!!! Next SMS date: " + date.toString() + 
						" : " + newestSmsMessageOnFirstAppStart.getMessageNumber());

	    		}
	    	}
	    }
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG_SMS, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
}

class RunnableDialog implements Runnable {

	private Context context;
	private String mutualIdFromSMS;
	private String accountFromSMS;
	private String phoneNumberFromSMS;
	private Activity activity;
	
	public RunnableDialog(Context context, Activity activity, String mutualIdFromSMS,
			String accountFromSMS, String phoneNumberFromSMS) {
		super();
		this.context = context;
		this.activity = activity;
		this.mutualIdFromSMS = mutualIdFromSMS;
		this.accountFromSMS = accountFromSMS;
		this.phoneNumberFromSMS = phoneNumberFromSMS;
	}

	@Override
	public void run() {
		ApproveJoinRequestContext approveJoinRequestContext = 
				new ApproveJoinRequestContext(context, mutualIdFromSMS);
		AddContactDialogPositiveOnClickListener addContactDialogPositiveOnClickListener = 
				new AddContactDialogPositiveOnClickListener(approveJoinRequestContext);
		AddContactDialogNegativeOnClickListener addContactDialogNegativeOnClickListener = 
				new AddContactDialogNegativeOnClickListener(approveJoinRequestContext);
//		ApproveAddContactRequestDialog approveAddContactRequestDialog = 
//		new ApproveAddContactRequestDialog(context, activity, accountFromSMS, phoneNumberFromSMS, 
//			addContactDialogPositiveOnClickListener, addContactDialogNegativeOnClickListener);
		Resources res = context.getResources();
		if(res == null){
			return;
		}
		new CommonDialog(activity, 
			res.getString(R.string.approve_add_contact_dialog_title), 
			String.format(res.getString(R.string.approve_add_contact_dialog_message), 
			    accountFromSMS, phoneNumberFromSMS), 
			res.getString(R.string.approve_add_contact_dialog_ok), 
			res.getString(R.string.approve_add_contact_dialog_cancel),
			false, // cancelable
			addContactDialogPositiveOnClickListener, 
			addContactDialogNegativeOnClickListener);
	}
	
}

class AddContactDialogPositiveOnClickListener implements OnClickListener {
	private String methodName;
	private String className = this.getClass().getName();
	private String logMessage;
	private ApproveJoinRequestContext approveJoinRequestContext;
	private String message = null; // , key = null, value = null;
	private MessageDataLocation location = null;
	
	public AddContactDialogPositiveOnClickListener(
			ApproveJoinRequestContext approveJoinRequestContext) {
		super();
		this.approveJoinRequestContext = approveJoinRequestContext;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		methodName = "onClick";
		
		String mutualId = approveJoinRequestContext.getMutualId();
		
		// Send JOIN APPROVED command (CommandEnum.join_approval) with
		// information about contact approving join request sent by SMS
		CommandDataBasic commandDataBasic;
		try {
			commandDataBasic = new CommandData(
				approveJoinRequestContext.getContext(), 
				approveJoinRequestContext.getContactDeviceDataList(), 
				CommandEnum.join_approval,
				message,					// null
				approveJoinRequestContext.getSenderMessageDataContactDetails(),
				location, 					// null
				CommandKeyEnum.mutualId.toString(),
				mutualId, 
				approveJoinRequestContext.getAppInfo()
			);
			commandDataBasic.sendCommand();
		} catch (UnableToSendCommandException e) {
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
		}
		
		// Remove from RECEIVED_JOIN_REQUEST
		ReceivedJoinRequestData receivedJoinRequestData = DBLayer.getInstance().getReceivedJoinRequest(mutualId);
		if( receivedJoinRequestData == null ){
			logMessage = "Failed to get received join request data";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			return;
		}else {
			logMessage = "ReceivedJoinRequestData = " + receivedJoinRequestData.toString();
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			
			// Add information to DB about contact, requesting join operation 
			ContactDeviceDataList contactDeviceDataListOwner = 
					DBLayer.getInstance().addContactDeviceDataList(
					new ContactDeviceDataList(	receivedJoinRequestData.getAccount(),
												receivedJoinRequestData.getMacAddress(), 
												receivedJoinRequestData.getPhoneNumber(), 
												receivedJoinRequestData.getRegId(), 
												null));
			if( contactDeviceDataListOwner == null ){
	        	logMessage = "Failed to save to DB details of the contcat requesting join operation";
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			}
		}
				
		// Delete join request that was handled 
		deleteReceivedJoinRequest(approveJoinRequestContext.getMutualId(), approveJoinRequestContext.getOwnerEmail());

		MessageDataContactDetails joinRequesterMessageDataContactDetails = 
			new MessageDataContactDetails(receivedJoinRequestData.getAccount(), 
				receivedJoinRequestData.getMacAddress(), 
				receivedJoinRequestData.getPhoneNumber(), 
				receivedJoinRequestData.getRegId(), 
				0);
		
		// Broadcast message to update ContactList
		Controller.broadcsatMessage(approveJoinRequestContext.getContext(), 
			joinRequesterMessageDataContactDetails, 
			BroadcastActionEnum.BROADCAST_MESSAGE.toString(), 
			"Update Contacts List", 
			CommandKeyEnum.update_contact_list.toString(), 
			CommandValueEnum.update_contact_list.toString());
	}
	
	private void deleteReceivedJoinRequest(String mutualId, String account){
		methodName = "deleteReceivedJoinRequest";
		int count = DBLayer.getInstance().deleteReceivedJoinRequest(mutualId);
		if(count == 0){
			logMessage = "Failed to delete received join request with mutual id [" + mutualId + "] from [" + account + "] account.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		} else {
			logMessage = "Deleted received join request with mutual id [" + mutualId + "] from [" + account + "] account.";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
	}
	
}

class AddContactDialogNegativeOnClickListener implements OnClickListener {
	private String methodName;
	private String className = this.getClass().getName();
	private String logMessage;
	private ApproveJoinRequestContext approveJoinRequestContext;
	private String message = null, key = null, value = null;
	private MessageDataLocation location = null;

	public AddContactDialogNegativeOnClickListener(
			ApproveJoinRequestContext approveJoinRequestContext) {
		super();
		this.approveJoinRequestContext = approveJoinRequestContext;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		methodName = "onClick";
		
		// Send JOIN REJECTED command (CommandEnum.join_rejected) with
		// information about contact rejecting join request sent by SMS
		CommandDataBasic commandDataBasic;
		try {
			commandDataBasic = new CommandData(
				approveJoinRequestContext.getContext(), 
				approveJoinRequestContext.getContactDeviceDataList(), 
				CommandEnum.join_rejected,
				message, 			// null
				approveJoinRequestContext.getSenderMessageDataContactDetails(), 
				location,			// null
				key,				// null
				value,				// null
				approveJoinRequestContext.getAppInfo()
			);
			commandDataBasic.sendCommand();
		} catch (UnableToSendCommandException e) {
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
		}
		
		// Remove from RECEIVED_JOIN_REQUEST
		deleteReceivedAddRequest(approveJoinRequestContext.getMutualId(), approveJoinRequestContext.getOwnerEmail());
	}

	private void deleteReceivedAddRequest(String mutualId, String account){
		methodName = "deleteReceivedJoinRequest";
		int count = DBLayer.getInstance().deleteReceivedJoinRequest(mutualId);
		if(count == 0){
			logMessage = "Failed to delete received join request with mutual id [" + mutualId + "] from [" + account + "] account.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		} else {
			logMessage = "Deleted received join request with mutual id [" + mutualId + "] from [" + account + "] account.";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
	}

}

