package com.doat.tracklocation.service;

import java.util.HashMap;

import com.doat.tracklocation.R;
import com.doat.tracklocation.Controller;
import com.doat.tracklocation.concurrent.RegisterToGCMInBackground;
import com.doat.tracklocation.datatype.AppInfo;
import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.datatype.BroadcastConstEnum;
import com.doat.tracklocation.datatype.BroadcastData;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.CommandData;
import com.doat.tracklocation.datatype.CommandDataBasic;
import com.doat.tracklocation.datatype.CommandEnum;
import com.doat.tracklocation.datatype.CommandKeyEnum;
import com.doat.tracklocation.datatype.CommandTagEnum;
import com.doat.tracklocation.datatype.CommandValueEnum;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.datatype.MessageDataLocation;
import com.doat.tracklocation.datatype.NotificationBroadcastData;
import com.doat.tracklocation.datatype.PermissionsData;
import com.doat.tracklocation.datatype.SentJoinRequestData;
import com.doat.tracklocation.db.DBHelper;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.db.DBManager;
import com.doat.tracklocation.dialog.ActivityDialogRing;
import com.doat.tracklocation.exception.CheckPlayServicesException;
import com.doat.tracklocation.exception.NoSentContactFromAccountException;
import com.doat.tracklocation.exception.NoSentContactFromException;
import com.doat.tracklocation.exception.UnableToSendCommandException;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;
import com.doat.tracklocation.utils.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class GcmIntentService extends IntentService {

	private Gson gson = new Gson();
	private AppInfo appInfo = null;
	private String logMessage;
	private String registrationId;
	
	MessageDataContactDetails messageDataContactDetails;
	
	private GoogleCloudMessaging gcm;
	private String className;
	private String googleProjectNumber;
	private String methodName;
	
	private Context context;
	
	boolean isRinging = false;
	
    private BroadcastReceiver notificationBroadcastReceiver = null;

    public GcmIntentService() {
		super("GcmIntentService");
		className = this.getClass().getName();
		methodName = "GcmIntentService";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		methodName = "onCreate";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		methodName = "onHandleIntent [thread: " + Thread.currentThread().getId() + "]";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		context = getApplicationContext();
		appInfo = Controller.getAppInfo(context);
		
		// The code below is taken from MainActivity function
		// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
		checkDeviceForGooglePlayServices();
		
		DBManager.initDBManagerInstance(new DBHelper(getApplicationContext()));
		googleProjectNumber = this.getResources().getString(R.string.google_project_number);
		Preferences.setPreferencesString(context, CommonConst.GOOGLE_PROJECT_NUMBER, googleProjectNumber);
		
		getClientRegistartionId();

    	// Collect client details
		messageDataContactDetails = initClientDetails();

		initNotificationBroadcastReceiver();

        Bundle extras = intent.getExtras();
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);
		
		logMessage = "Received message type: " + messageType;
		LogManager.LogInfoMsg(className, "onHandleIntent", logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
                
        if (!extras.isEmpty()) {  
            // ============================================
            // Filter messages based on message type. Since it is likely that GCM
            // will be extended in the future with new message types, just ignore
            // any message types you're not interested in, or that you don't
            // recognize.
            // ============================================
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //sendNotification("Send error: " + extras.toString());
            	
            	// IMPORTANT! - DO NOT LOG extars - it can contain RegistrationID
        		//LogManager.LogErrorMsg(className, "onHandleIntent", 
        		//	"Send error: " + extras.toString());
        		
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
            	//sendNotification("Deleted messages on server: " +
                //        extras.toString());
            	
            	// IMPORTANT! - DO NOT LOG extars - it can contain RegistrationID
        		// LogManager.LogErrorMsg(className, "onHandleIntent", 
        		//	"Deleted messages on server: " + extras.toString());
        		
        	// ============================================
            // If it's a regular GCM message, do some work.
        	// ============================================
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            	logMessage = "It is a regular GCM message";
            	LogManager.LogInfoMsg(className, methodName, logMessage);
            	Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
            	
            	// ============================================
            	// COMMANDS TO RUN ON CLIENT SIDE
            	// ============================================
            	
            	if(extras.containsKey(CommandTagEnum.command.toString())){
            		logMessage = "GCM COMMAND: " + extras.getString(CommandTagEnum.command.toString());
            		LogManager.LogInfoMsg(className, methodName, logMessage);
            		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
            	}
            
            	// ============================================
            	// ====  COMMAND: IS_ONLINE  ==================
            	// ============================================
                // COMMAND: Check is contact online
            	// CLIENT SIDE	
            	// command received via GCM from Master
            	// to Slave - in order to confirm online status
            	// ============================================
            	if(extras.containsKey(CommandTagEnum.command.toString()) &&
            			extras.getString(CommandTagEnum.command.toString()).
            			equals(CommandEnum.is_online.toString())){ // COMMAND IS_ONLINE
            		
        			handleCommandIsOnline(extras);
    	    		
            	// ============================================
            	// ====  COMMAND: START  ======================
            	// ============================================
                // COMMAND: Start Track Location Service on client
            	// CLIENT SIDE	
            	// command received via GCM from Master
            	// to Slave - in order to start location
            	// service on Slave
            	// ============================================
            	} else if(extras.containsKey(CommandTagEnum.command.toString()) &&
            			extras.getString(CommandTagEnum.command.toString()).
            			equals(CommandEnum.start.toString())){ // COMMAND START
            		
        			handleCommandStartTrackLocationService(extras);
    	    		                		
                // ============================================
                // ====  COMMAND: LOCATION_UPDATE  ============
                // ============================================
                // notification from Slave to Master and
                // broadcast to related UI consumers on Master 		
            	// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.location.toString())){ // COMMAND LOCATION
            		
            		handleCommandLocationUpdate(extras);

                // ============================================
                // ====  COMMAND: JOIN_APPROVAL  ==============
                // ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.join_approval.toString())){ // COMMAND JOIN_APPROVAL
            		
            		handleCommandJoinApproval(extras);            		
                		
                // ======================================================
                // ====  COMMAND: TRACK_LOCATION_SERVICE_KEEP_ALIVE  ====
        		// ======================================================
            	// command received via GCM from Master
            	// to Slave - in order to keep alive location
            	// service on Slave 
                // (update start time on Timer that stops Track Location Service )
            	// ======================================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.track_location_service_keep_alive.toString())){ // COMMAND TRACK_LOCATION_SERVICE_KEEP_ALIVE
            		
        			handleCommandTrackLocationKeepAlive(extras);
					
    	        // ============================================
    	        // ====  COMMAND: UPDATE_REGISTARTION_ID  =====
        		// ============================================
                } else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                    		extras.getString(CommandTagEnum.command.toString()).
                    		equals(CommandEnum.update_reg_id.toString())){ // COMMAND UPDATE_REGISTARTION_ID
                		
            		handleCommandUpdateRegistartionID(extras);

            	// ============================================
	            // ====  COMMAND: NOTIFICATION  ===============
        		// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.notification.toString())){ // COMMAND NOTIFICATION
            		
        			handleCommandNotification(extras);
            	// ============================================
	            // ====  COMMAND: RING_DEVICE  ===============
        		// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.ring_device.toString())){ // COMMAND RING_DEVICE
            		
        			handleCommandRingDevice(extras);
            	} 
        	
            } else { // if (GoogleCloudMessaging
        		logMessage = "Unknown GCM type - NOTHING DONE.";
        		LogManager.LogInfoMsg(className, "onHandleIntent", logMessage);
        		Log.w(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
            }
        } else {// if (extras.isEmpty())...
    		logMessage = "Bundle extras is EMPTY.";
    		LogManager.LogInfoMsg(className, "onHandleIntent", logMessage);
    		Log.w(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
        }
        
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
        
        LogManager.LogFunctionExit(className, methodName);
        Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
        
	} // onHandleIntent(...
	
	private MessageDataContactDetails initClientDetails(){
		MessageDataContactDetails messageDataContactDetails = new MessageDataContactDetails(
				Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT), 
				Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS), 
				Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER), 
				Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID), 
				Controller.getBatteryLevel(context)
				);
		return messageDataContactDetails;
	}
	
	// The code below is taken from MainActivity function
	// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
	private void checkDeviceForGooglePlayServices(){
		
		String methodName = "checkDeviceForGooglePlayServices";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		try {
			Controller.checkPlayServices(context);
			LogManager.LogFunctionExit(className, methodName);
			Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
		} catch (CheckPlayServicesException e) {
			String errorMessage = e.getMessage();
			if(CommonConst.PLAYSERVICES_ERROR.equals(errorMessage)){
	            //GooglePlayServicesUtil.getErrorDialog(e.getResultCode(), this,
	            //	PLAY_SERVICES_RESOLUTION_REQUEST).show();
				logMessage = errorMessage + " .Error code: " + e.getResultCode();
				LogManager.LogException(e, logMessage, methodName);
				Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
			} else if(CommonConst.PLAYSERVICES_DEVICE_NOT_SUPPORTED.equals(errorMessage)){
				// Show dialog with errorMessage
				// showNotificationDialog(errorMessage);
				logMessage = "Current device is not supported by Google Play Services ";
				LogManager.LogException(e, logMessage, methodName);
				Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
			}
			logMessage = "No valid Google Play Services APK found.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			//finish();
		}
	}
	
	private void getClientRegistartionId(){
		
		String methodName = "getClientRegistartionId";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
    	Thread registerToGCMInBackgroundThread;
    	Runnable registerToGCMInBackground;

		gcm = GoogleCloudMessaging.getInstance(this);
        registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
        if (registrationId == null || registrationId.isEmpty()) {
            // registerInBackground();
        	HashMap<String, Object> map = new HashMap<String, Object>();
        	map.put("GoogleCloudMessaging", gcm);
        	map.put("GoogleProjectNumber", getResources().getString(R.string.google_project_number));
        	map.put("Context", context);
        	
        	// Controller.registerInBackground(map);
        	String googleProjectNumber = Preferences.getPreferencesString(context, CommonConst.GOOGLE_PROJECT_NUMBER);
        	registerToGCMInBackground = new RegisterToGCMInBackground(context, gcm, googleProjectNumber);
			try {
				registerToGCMInBackgroundThread = new Thread(registerToGCMInBackground);
				registerToGCMInBackgroundThread.start();
				// Launch waiting dialog - till registration process will be completed or failed
				// launchWaitingDialog();
			} catch (IllegalThreadStateException e) {
				logMessage = "Register to GCM in background thread was started already";
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
			}

        }
        
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	private MessageDataContactDetails initCommand(Bundle extras) 
			throws NoSentContactFromException, NoSentContactFromAccountException{
		
		String jsonContactDetailsSentFrom = extras.getString(CommandTagEnum.contactDetails.toString());
		
		MessageDataContactDetails contactDetailsSentFrom = 
			gson.fromJson(jsonContactDetailsSentFrom, MessageDataContactDetails.class);
		if(contactDetailsSentFrom == null){
			logMessage = "No SentFrom contact details. Cannot start TrackLocation Service.";
            Log.e(CommonConst.LOG_TAG, logMessage);
            LogManager.LogErrorMsg(className, "GcmIntentService->onHandleIntent->[COMMAND:start]", logMessage);
            throw new NoSentContactFromException("");
		}
		
		String accountCommandSentFrom = null;
		accountCommandSentFrom = contactDetailsSentFrom.getAccount();
		if(accountCommandSentFrom == null){
			logMessage = "No SentFrom contact details - account/email. Cannot start TrackLocation Service.";
            Log.e(CommonConst.LOG_TAG, logMessage);
            LogManager.LogErrorMsg(className, "GcmIntentService->onHandleIntent->[COMMAND:start]", logMessage);
            throw new NoSentContactFromAccountException("");
		}
		
		// update (insert/add) regIds to list of contacts that will be notified
		Preferences.setPreferencesReturnToContactMap(context, 
			accountCommandSentFrom, contactDetailsSentFrom.getRegId());
		
		return contactDetailsSentFrom;
	}
	
	private void handleCommandIsOnline(Bundle extras){
		String methodName = "handleCommandIsOnline";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
	
		MessageDataContactDetails senderMessageDataContactDetails;
		String senderAccount;

		try {
			senderMessageDataContactDetails = initCommand(extras);
			senderAccount = senderMessageDataContactDetails.getAccount();
    		logMessage = "Catched push notification message (GCM): [IS_ONLINE]" +
    			"from [" + senderAccount + "]";
    		LogManager.LogInfoMsg(className, methodName, logMessage);
    		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		} catch (NoSentContactFromException e) {
			logMessage = "Catched push notification message (GCM): [IS_ONLINE]" +
	    		"Failed to check online status - NoSentContactFrom details";
			LogManager.LogException(e, logMessage, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
			return;
		} catch (NoSentContactFromAccountException e) {
			logMessage = "Catched push notification message (GCM): [IS_ONLINE]" +
				"Failed to check online status - NoSentContactFromAccount details";
			LogManager.LogException(e, logMessage, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
			return;
		}

		// ===========================================================
		// ===  NOTIFICATION: ONLINE STATUS			 			  ====
		// ===========================================================
        // Notify caller by GCM (push notification)
		sendNotificationCommand(
			senderMessageDataContactDetails,
			"Online status received from '" + senderAccount + "'", 
			CommandKeyEnum.online_status.toString(), 
			CommandValueEnum.online.toString());

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	private void handleCommandStartTrackLocationService(Bundle extras){
		
		String methodName = "handleCommandStartTrackLocationService";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		MessageDataContactDetails senderMessageDataContactDetails;
		String senderAccount;
		
		try {
			senderMessageDataContactDetails = initCommand(extras);
			senderAccount = senderMessageDataContactDetails.getAccount();
    		logMessage = "Catched push notification message (GCM): [START TrackLocation Service] " + 
    			"from [" + senderAccount + "]";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		} catch (NoSentContactFromException e) {
			logMessage = "Catched push notification message (GCM): [START TrackLocation Service]. " + 
				"Failed to start TrackLocationService - NoSentContactFrom details";
			LogManager.LogException(e, logMessage, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
			return;
		} catch (NoSentContactFromAccountException e) {
			logMessage = "Catched push notification message (GCM): [START TrackLocation Service]. " + 
				"Failed to start TrackLocationService - NoSentContactFromAccount details";
			LogManager.LogException(e, logMessage, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
			return;
		}
		
		// ===========================================================
		// ===  NOTIFICATION: RECEIVED "START" COMMAND 			  ====
		// ===  TO START TRACK LOCATION SERVICE					  ==== 
		// ===  FOR ACCOUNT  									  ====
		// ===========================================================
        // Notify caller by GCM (push notification)

		sendNotificationCommand(
			senderMessageDataContactDetails,
			"Start Track Location Service command received by '" + senderAccount + "'", 
			CommandKeyEnum.start_status.toString(), 
			CommandValueEnum.start_track_location_service_received.toString());
		
		if(isPermissionToGetLocation(senderMessageDataContactDetails,
				messageDataContactDetails, methodName) == false){
			return;
		}
		
    	// ============================================
    	// ====  COMMAND: START + INTERVAL PARAM  =====
    	// ============================================
        // COMMAND: 	start - with parameter:
    	// PARAMETER: 	interval
    	// CLIENT SIDE	
    	// ============================================
		if(extras.containsKey(CommandTagEnum.key.toString()) && 
				extras.getString(CommandTagEnum.key.toString()).
    			equals(CommandTagEnum.interval.toString())){ // PARAMETER INTERVAL
			String intervalString = extras.getString(CommandTagEnum.value.toString());
			if(intervalString != null && !intervalString.isEmpty()){
	            Preferences.setPreferencesString(getApplicationContext(), 
	            	CommonConst.LOCATION_SERVICE_INTERVAL, intervalString);
			}
		}

		logMessage = "Location update interval: " + Preferences.getPreferencesString(getApplicationContext(), 
            CommonConst.LOCATION_SERVICE_INTERVAL);
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

        String notificationKey = "";
        String notificationValue = "";
        boolean isTrackLocationServiceRunning = Utils.isServiceRunning(context, TrackLocationService.class);
        if(isTrackLocationServiceRunning == true){
        	logMessage = "TrackLocationService is already started";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> ThreadID: " + logMessage);
			// ===========================================================
			// ===  NOTIFICATION: START COMMAND STATUS - success  ========
			// ===========================================================
			// Notify caller by GCM (push notification)
			notificationKey = CommandKeyEnum.start_status.toString();
			notificationValue = CommandValueEnum.success.toString();
			sendNotificationCommand(
					senderMessageDataContactDetails,
					logMessage, 
					notificationKey, 
					notificationValue);
			
			
			MessageDataLocation location = null;
			BroadcastData broadcastData = new BroadcastData();
			broadcastData.setContactDetails(senderMessageDataContactDetails);
			broadcastData.setLocation(location);
			broadcastData.setKey(BroadcastKeyEnum.restart_tls.toString());
			broadcastData.setValue(gson.toJson(senderMessageDataContactDetails, MessageDataContactDetails.class));
			String jsonBroadcastData = gson.toJson(broadcastData);

			Controller.broadcastMessage(GcmIntentService.this, 
				BroadcastActionEnum.BROADCAST_MESSAGE.toString(),
				"GcmIntentServicefrom from [" + senderMessageDataContactDetails.getAccount() + "]",
				jsonBroadcastData, 
				BroadcastKeyEnum.restart_tls.toString(),  
				"value");

	    	logMessage = "TrackLocation service got restart request from [" + senderMessageDataContactDetails.getAccount() + "]";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

			
			return;
        }

        // ===========================================================
		// Start TrackLocation service to get current location
		// ===========================================================
        
        logMessage = "Starting of TrackLocationService";
        LogManager.LogInfoMsg(className, methodName, logMessage);
        Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
        
		Intent trackLocationService = new Intent(context, TrackLocationService.class);
		String jsonSenderMessageDataContactDetails = gson.toJson(senderMessageDataContactDetails);
		//String jsonContactDetailsSentFrom = gson.toJson(contactDetailsSentFrom, MessageDataContactDetails.class);
		trackLocationService.putExtra(CommonConst.START_CMD_SENDER_MESSAGE_DATA_CONTACT_DETAILS, jsonSenderMessageDataContactDetails);
		ComponentName componentName = context.startService(trackLocationService); 
		notificationKey = CommandKeyEnum.start_status.toString();
		notificationValue = CommandValueEnum.success.toString();
		if(componentName == null){
			// Notify that TrackLoactionService was not started - by GCM (push notification)
			logMessage = "Failed to start TrackLocationService on [" + messageDataContactDetails.getAccount() + "]";
			notificationKey = CommandKeyEnum.start_status.toString();
			notificationValue = CommandValueEnum.error.toString();		
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
		
		// ===========================================================
		// ===  NOTIFICATION: START COMMAND STATUS                 ===
		// ===========================================================
		// Notify caller by GCM (push notification)
		
		sendNotificationCommand(
			senderMessageDataContactDetails,
			logMessage, 
			notificationKey, 
			notificationValue);
		
        LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	
	private void sendNotificationCommand(
			MessageDataContactDetails senderMessageDataContactDetails,
			String notificationMessage, 
			String notificationKey, 
			String notificationValue){
		
		String methodName = "sendNotificationCommand";
		
		ContactDeviceDataList contactDeviceDataToSendNotificationTo = new ContactDeviceDataList(
				senderMessageDataContactDetails.getAccount(),
				senderMessageDataContactDetails.getMacAddress(),
				senderMessageDataContactDetails.getPhoneNumber(),
				senderMessageDataContactDetails.getRegId(), null);

		// ===========================================================
		// ===  NOTIFICATION: START COMMAND STATUS  ==================
		// ===========================================================
		// Notify caller by GCM (push notification)
		CommandDataBasic commandDataBasic;
		try {
			commandDataBasic = new CommandData(
				getApplicationContext(), 
				contactDeviceDataToSendNotificationTo, 
				CommandEnum.notification, 
				notificationMessage, 
				messageDataContactDetails, 
				null, 					// location
				notificationKey, 
				notificationValue,
				appInfo
			);
			commandDataBasic.sendCommand();
		} catch (UnableToSendCommandException e) {
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
		}
	}
	
	private void handleCommandLocationUpdate(Bundle extras){

		String methodName = "handleCommandLocationUpdate";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		String key = extras.getString(CommandTagEnum.key.toString());
		String value = extras.getString(CommandTagEnum.value.toString());
		String currentDateTime = Controller.getCurrentDate();
		
		logMessage = "StatusResponse key = " + key;
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		logMessage = "StatusResponse value = " + value;
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		String jsonContactDetails = extras.getString(CommandTagEnum.contactDetails.toString());
		if(jsonContactDetails == null){
			logMessage = "Command LocationUpdate failed. No SentFrom contact details in extras.";
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "." + methodName + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
            return;
		}
		
		String jsonLocation = extras.getString("location");

		MessageDataContactDetails contactDetails = 
			gson.fromJson(jsonContactDetails, MessageDataContactDetails.class);
		if(contactDetails == null){
			logMessage = "Command LocationUpdate failed. Unable to retrieve sender's contact details...";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			LogManager.LogFunctionExit(className, methodName);
			Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
			return;
		}
		MessageDataLocation location = 
			gson.fromJson(jsonLocation, MessageDataLocation.class);
		if(location == null){
			logMessage = "Command LocationUpdate failed. Unable to retrieve sender's location details...";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			LogManager.LogFunctionExit(className, methodName);
			Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
			return;
		}
		
		logMessage = "Catched push notification message (GCM): [LOCATION_UPDATE]" +
			"from [" + contactDetails.getAccount() + "]";
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		logMessage = "Location of [" + contactDetails.getAccount() + "]: " + jsonLocation;
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		BroadcastData broadcastData = new BroadcastData();
		broadcastData.setContactDetails(contactDetails);
		broadcastData.setLocation(location);
		String jsonBroadcastData = gson.toJson(broadcastData);
		
		Controller.broadcastMessage(GcmIntentService.this, 
			BroadcastActionEnum.BROADCAST_LOCATION_UPDATED.toString(), 
			"GcmIntentService from [" + contactDetails.getAccount() + "]",
			jsonBroadcastData,	
			null, //BroadcastKeyEnum.location_updated.toString(), 
			key + CommonConst.DELIMITER_STRING + value + CommonConst.DELIMITER_STRING + currentDateTime);
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
//	time=21:17:23 24/08/2014, collapse_key=do_not_collapse, 
//	appInfo={"versionName":"0.0.1","versionNumber":2}, 
//	regIDToReturnMessageTo=APA91bEBpGwPmyVlQ08ab9WNsza8XeRywpXVyTcy6NCZckAiq53rMDcqxfhO1meQEhEzcB7C7KKiVtvTBngal_obixquq5jZkfUE4AiYCEmhQ735B8guHLTBf4tmqwGoelXWLQHp7jrBvPdowFBpk6Je-LTALHaXBpANT7GqL6ToBvmKceGJ0BI, 
//	command=join_approval, android.support.content.wakelockid=1, 
//	contactDetails={"phoneNumber":"","account":"agrest2000@gmail.com","macAddress":"10:68:3f:44:7a:98","batteryPercentage":-1.0,"regId":"APA91bEBpGwPmyVlQ08ab9WNsza8XeRywpXVyTcy6NCZckAiq53rMDcqxfhO1meQEhEzcB7C7KKiVtvTBngal_obixquq5jZkfUE4AiYCEmhQ735B8guHLTBf4tmqwGoelXWLQHp7jrBvPdowFBpk6Je-LTALHaXBpANT7GqL6ToBvmKceGJ0BI"}, 
//	from=943276333483}]
	
	private void handleCommandJoinApproval(Bundle extras) {
		
		String methodName = "handleCommandJoinApproval";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		String jsonContactDetailsSentFrom = extras.getString(CommandTagEnum.contactDetails.toString());
		if(jsonContactDetailsSentFrom == null){
			logMessage = "Join approval request failed. No SentFrom contact details in extras.";
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "." + methodName + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
            return;
		}
		
		MessageDataContactDetails contactDetailsSentFrom = gson.fromJson(jsonContactDetailsSentFrom, MessageDataContactDetails.class);
		if(contactDetailsSentFrom == null){
			logMessage = "Join approval request failed. No SentFrom contact details.";
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "." + methodName + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
            return;
		}
		
		String accountCommandSentFrom = null;
		accountCommandSentFrom = contactDetailsSentFrom.getAccount();
		if(accountCommandSentFrom == null){
			logMessage = "Join approval request failed. No account/email.";
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "." + methodName + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
            return;
		}

		logMessage = "Catched push notification message (GCM): [JOIN_APPROVAL]" +
			"from [" + accountCommandSentFrom + "]";
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		String registrationIdJoinApproval = null;
		registrationIdJoinApproval = contactDetailsSentFrom.getRegId();
		if(registrationIdJoinApproval == null){
			logMessage = "Join approval request failed. No registrationID.";
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "." + methodName + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
            return;
		}
		
		String phoneNumber = contactDetailsSentFrom.getPhoneNumber();
		String macAddress = contactDetailsSentFrom.getMacAddress();

		String key = extras.getString(CommandTagEnum.key.toString());
		String mutualId = null;
		if(CommandKeyEnum.mutualId.toString().equals(key)){
			mutualId = extras.getString(CommandTagEnum.value.toString()); // mutualId
		}

//		// Insert into TABLE_PERMISSIONS account(email) according to mutualId
//		long countAdded = DBLayer.addPermissions(email, 0, 0, 0);
//		PermissionsData permissionData = DBLayer.getPermissions(email);
		
		if(mutualId != null){
			SentJoinRequestData sentJoinRequestData = DBLayer.getInstance().getSentJoinRequestByMutualId(mutualId);
			if( sentJoinRequestData == null ){
				logMessage = "Failed to get sent join request data";
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			}else {
				// TODO: Check which data id possible to show in log - from sentJoinRequestData
				logMessage = "ReceivedJoinRequestData = " +  "check which data is possible to log"; // sentJoinRequestData;
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			}

			// Remove join request from TABLE_SEND_JOIN_REQUEST according to "mutualId"
			int count = DBLayer.getInstance().deleteSentJoinRequest(mutualId);
			if( count < 1) {
				logMessage = "Failed to delete sent join request with mutual id: " + mutualId + " count = " + count;
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.w(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			} else {
				logMessage = "Deleted sent join request with mutual id: " + mutualId + " count = " + count;
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.w(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			}
			
			sentJoinRequestData = DBLayer.getInstance().getSentJoinRequestByMutualId(mutualId);
			if( sentJoinRequestData != null){
				logMessage = "Failed to delete sent join request with mutual id: " + mutualId;
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.w(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			}
		} else {
			logMessage = "Failed to delete sent join request with mutual id: " + mutualId;
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.w(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		}
		
		// Add contact details to CondtactDeviceData table
		DBLayer.getInstance().addContactDeviceDataList(new ContactDeviceDataList(accountCommandSentFrom,
				macAddress, phoneNumber, registrationIdJoinApproval, null));
		
		// Broadcast message to update ContactList
		Controller.broadcsatMessage(context, messageDataContactDetails, BroadcastActionEnum.BROADCAST_MESSAGE.toString(), 
			"Update Contacts List", CommandKeyEnum.update_contact_list.toString(), CommandValueEnum.update_contact_list.toString());
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	private void handleCommandTrackLocationKeepAlive(Bundle extras){

		String methodName = "handleCommandTrackLocationKeepAlive";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		String value = extras.getString(CommandTagEnum.value.toString());
		
		String jsonContactDetails = extras.getString(CommandTagEnum.contactDetails.toString());
		if(jsonContactDetails == null){
			logMessage = "Command TrackLocationKeepAlive failed. No SentFrom contact details in extras.";
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "." + methodName + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
            return;
		}
		
		MessageDataContactDetails contactDetails = 
				gson.fromJson(jsonContactDetails, MessageDataContactDetails.class);
		if(contactDetails == null){
			logMessage = "Command TrackLocationKeepAlive failed. No SentFrom contact details.";
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "." + methodName + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
            return;
		}

		logMessage = "Catched push notification message (GCM): [TRACK_LOCATION_SERVICE_KEEP_ALIVE]" +
				"from [" + contactDetails.getAccount() + "]";
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		MessageDataLocation location = null;
		BroadcastData broadcastData = new BroadcastData();
		broadcastData.setContactDetails(contactDetails);
		broadcastData.setLocation(location);
		broadcastData.setKey(BroadcastKeyEnum.keep_alive.toString());
		broadcastData.setValue(value);
		String jsonBroadcastData = gson.toJson(broadcastData);

		Controller.broadcastMessage(GcmIntentService.this, 
			BroadcastActionEnum.BROADCAST_LOCATION_KEEP_ALIVE.toString(),
			"GcmIntentServicefrom from [" + contactDetails.getAccount() + "]",
			jsonBroadcastData, 
			BroadcastKeyEnum.keep_alive.toString(),  
			value);

    	logMessage = "TrackLocation service got keep alive request from [" + contactDetails.getAccount() + "]";
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	private void handleCommandNotification(Bundle extras){
		
		String methodName = "handleCommandNotification";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		String msg = extras.getString(CommandTagEnum.message.toString());
		if(msg != null && !msg.isEmpty()){
			logMessage = "message: " + msg;
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		}
		
		String key = extras.getString(CommandTagEnum.key.toString());
		logMessage = "[KEY]: " + key;
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		String value = extras.getString(CommandTagEnum.value.toString());
		logMessage = "[VALUE]: " + value;
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		
		String jsonContactDetails = extras.getString(CommandTagEnum.contactDetails.toString());
		if(jsonContactDetails == null){
			logMessage = "Command Notification failed. No SentFrom contact details in extras.";
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "." + methodName + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
            return;
		}
		MessageDataContactDetails contactDetails = gson.fromJson(jsonContactDetails, MessageDataContactDetails.class);
		String senderAccount = null;
		if(contactDetails == null){
			logMessage = "Command Notification failed. No SentFrom contact details in extras.";
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "." + methodName + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
            return;
		} else {
			senderAccount = contactDetails.getAccount();
			logMessage = "Catched push notification message (GCM): [NOTIFICATION]" +
				"from [" + senderAccount + "]";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		}
		
		if(senderAccount == null || senderAccount.isEmpty()){
			logMessage = "[senderAccount]: not defined";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			LogManager.LogFunctionExit(className, methodName);
			Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
			return;
		}		
		
		logMessage = "Sent by [" + senderAccount + "]";
		LogManager.LogInfoMsg(className, methodName, "");
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		// ============================================================
		// Contact is online - notification received
		// ============================================================
		if(CommandKeyEnum.online_status.toString().equals(key) && 
				CommandValueEnum.online.toString().equals(value)){
			
			String jsonListAccounts = Preferences.getPreferencesString(context, 
	        		CommonConst.PREFERENCES_SEND_IS_ONLINE_TO_ACCOUNTS);
			logMessage = "Catched push notification message (GCM): [NOTIFICATION] - ONLINE_STATUS." +
				"Recipients accounts list: " + jsonListAccounts;
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

			if(jsonListAccounts != null && !jsonListAccounts.isEmpty()){
				Controller.removeAccountFromList(context, 
					CommonConst.PREFERENCES_SEND_IS_ONLINE_TO_ACCOUNTS, jsonListAccounts, senderAccount);
				logMessage = "Removed sender: " + senderAccount;
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
	
				// TODO: For test only:
				jsonListAccounts = Preferences.getPreferencesString(context, 
		        	CommonConst.PREFERENCES_SEND_IS_ONLINE_TO_ACCOUNTS);
			
				// TODO: Broadcast - the following recipients are not available
				if(jsonListAccounts != null && !jsonListAccounts.isEmpty()){
					Controller.broadcsatMessage(context, contactDetails, BroadcastActionEnum.BROADCAST_MESSAGE.toString(), 
						msg + " by " + senderAccount, key, value);
				}
			} else {
				logMessage = "\"Check Which Contact Is Online thread\" is not stopped!";
				LogManager.LogWarnMsg(className, methodName, logMessage);
				Log.w(CommonConst.LOG_TAG, "[WARN] {" + className + "} -> " + logMessage);
			}
			
		// ============================================================
		// TrackLocation Service started - notification received
		// ============================================================
		} else if(CommandKeyEnum.start_status.toString().equals(key) && 
				CommandValueEnum.success.toString().equals(value)){
			
			logMessage = "Catched push notification message (GCM): [NOTIFICATION] - START_STATUS: SUCCESS." +
				"From recipient account[" + senderAccount + "]";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

			String jsonListAccounts = Preferences.getPreferencesString(context, 
	        	CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS);
			if(jsonListAccounts != null && !jsonListAccounts.isEmpty()){
				Controller.removeAccountFromList(context, 
					CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS, jsonListAccounts, senderAccount);
	
				// TODO: For test only:
				jsonListAccounts = Preferences.getPreferencesString(context, 
		        	CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS);
				// TODO: Broadcast - the following recipients are not available
				if(jsonListAccounts != null && !jsonListAccounts.isEmpty()){
					Controller.broadcsatMessage(context, contactDetails, BroadcastActionEnum.BROADCAST_MESSAGE.toString(), 
						senderAccount, key, value);
				}
			} else {
				logMessage = "\"Start TrackLocation Service thread\" is not stopped!";
				LogManager.LogWarnMsg(className, methodName, logMessage);
				Log.w(CommonConst.LOG_TAG, "[WARN] {" + className + "} -> " + logMessage);
			}
			
		// ============================================================
		// Permissions - notification received
		// ============================================================
		} else if (CommandKeyEnum.permissions.toString().equals(key) && 
				CommandValueEnum.not_defined.toString().equals(value)){
			String jsonListAccounts = Preferences.getPreferencesString(context, 
	        		CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS);
			logMessage = "Catched push notification message (GCM): [NOTIFICATION] - PERMISSIONS: NOT_DEFINED." +
				"Recipients accounts list: [" + jsonListAccounts + "]";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

			if(jsonListAccounts != null && !jsonListAccounts.isEmpty()){
				Controller.removeAccountFromList(context, 
					CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS, jsonListAccounts, senderAccount);
				Controller.broadcsatMessage(context, BroadcastActionEnum.BROADCAST_MESSAGE.toString(), msg + " by " + senderAccount, key, value);
			} else {
				logMessage = "\"Check Permissions Not Defined thread\" is not stopped!";
				LogManager.LogWarnMsg(className, methodName, logMessage);
				Log.w(CommonConst.LOG_TAG, "[WARN] {" + className + "} -> " + logMessage);
			}
		} else if (CommandKeyEnum.permissions.toString().equals(key) && 
				CommandValueEnum.not_permitted.toString().equals(value)){
			String jsonListAccounts = Preferences.getPreferencesString(context, 
	        		CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS);
			logMessage = "Catched push notification message (GCM): [NOTIFICATION] - PERMISSIONS: NOT_PERMITTED." +
				"Recipients accounts list: " + jsonListAccounts;
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

			if(jsonListAccounts != null && !jsonListAccounts.isEmpty()){
				Controller.removeAccountFromList(context, 
					CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS, jsonListAccounts, senderAccount);
				Controller.broadcsatMessage(context, BroadcastActionEnum.BROADCAST_MESSAGE.toString(), msg + " by " + senderAccount, key, value);
			} else {
				logMessage = "\"Check Permissions Not Permitted thread\" is not stopped!";
				LogManager.LogWarnMsg(className, methodName, logMessage);
				Log.w(CommonConst.LOG_TAG, "[WARN] {" + className + "} -> " + logMessage);
			}
		} else if (CommandKeyEnum.start_status.toString().equals(key) && 
				CommandValueEnum.start_track_location_service_received.toString().equals(value)){

			String jsonListAccounts = Preferences.getPreferencesString(context, 
	        		CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS);
			logMessage = "Catched push notification message (GCM): [NOTIFICATION] - START_STATUS: START_TRACK_LOCATION_SERVICE_RECEIVED " +
				"Recipients accounts list: " + jsonListAccounts;
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
				
			Controller.broadcsatMessage(context, BroadcastActionEnum.BROADCAST_MESSAGE.toString(), msg + " by " + senderAccount, key, value);
		}

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	private void handleCommandUpdateRegistartionID(Bundle extras){
		String methodName = "handleCommandUpdateRegistartionID";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		String msg = extras.getString(CommandTagEnum.message.toString());
		if(msg != null && !msg.isEmpty()){
			logMessage = "message: " + msg;
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		}
		
		String key = extras.getString(CommandTagEnum.key.toString());
		logMessage = "[KEY]: " + key;
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		String value = extras.getString(CommandTagEnum.value.toString());
		logMessage = "[VALUE]: " + "RegistrationID = " + Controller.hideRealRegID(value);
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		
		String jsonContactDetails = extras.getString(CommandTagEnum.contactDetails.toString());
		if(jsonContactDetails == null){
			logMessage = "Command UpdateRegistartionID failed. No SentFrom contact details in extras.";
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "." + methodName + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
            return;
		}
		
		MessageDataContactDetails contactDetails = gson.fromJson(jsonContactDetails, MessageDataContactDetails.class);

		String senderAccount = null;
		String senderMacAddress = null;
		if(contactDetails == null){
			logMessage = "Command UpdateRegistartionID failed. No SentFrom contact details.";
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "." + methodName + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
            return;
		} else {
			senderAccount = contactDetails.getAccount();
    		logMessage = "Catched push notification message (GCM): [UPDATE_REGISTARTION_ID]" +
    			"from [" + senderAccount + "]";
    		LogManager.LogInfoMsg(className, methodName, logMessage);
    		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			senderMacAddress = contactDetails.getMacAddress();
		}
		if(senderAccount == null || senderAccount.isEmpty()){
			logMessage = "Command UpdateRegistartionID failed - [senderAccount]: not defined";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
			return;
		}		
		if(senderMacAddress == null || senderMacAddress.isEmpty()){
			logMessage = "Command UpdateRegistartionID failed - [senderMacAddress]: not defined";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
			return;
		}		
		
		// Update registrationID - sent by contact,
		// that registered its application against Google Cloud Message
		// Input parameters: value(as RegistrationID), senderAccount, senderMacAddress
		long result = DBLayer.getInstance().updateRegistrationID(senderAccount, senderMacAddress, value);
		if(result == -1){
			logMessage = "Failed to update RegistartionID";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
	}
	
	private void handleCommandRingDevice(Bundle extras){
		String methodName = "handleCommandRingDevice";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
		
		String jsonContactDetailsSentFrom = extras.getString(CommandTagEnum.contactDetails.toString());
		if(jsonContactDetailsSentFrom == null){
			logMessage = "Command RingDevice failed. No SentFrom contact details in extras.";
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "." + methodName + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
            return;
		}
		
		MessageDataContactDetails contactDetailsSentFrom = 
			gson.fromJson(jsonContactDetailsSentFrom, MessageDataContactDetails.class);
		if(contactDetailsSentFrom == null){
			logMessage = "Command RingDevice failed. No SentFrom contact details.";
	        Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "." + methodName + "} -> " + logMessage);
	        LogManager.LogErrorMsg(className, methodName, logMessage);
			LogManager.LogFunctionExit(className, methodName);
			Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	        return;
		}
		
		String accountCommandSentFrom = contactDetailsSentFrom.getAccount();
		logMessage = "Catched push notification message (GCM): [RING_DEVICE]" +
				"from [" + accountCommandSentFrom + "]";
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		/* TODO: continue development - DAVID		
		
		Intent intent = new Intent(this, NotificationReceiver.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

		// build notification
		// the addAction re-use the same intent to keep the example short
		Notification n  = new Notification.Builder(this)
		        .setContentTitle("Ring from " + accountCommandSentFrom)
		        .setContentText("Subject")
		        .setSmallIcon(R.drawable.main_icon_96)
		        .setContentIntent(pIntent)
		        .setAutoCancel(true)
		        .addAction(R.drawable.main_icon_96, "Call", pIntent)
		        .build();
		    
		  
		NotificationManager notificationManager = 
		  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		notificationManager.notify(0, n); 
*/		
		
		Intent intentRingDialog = new Intent(getBaseContext(),ActivityDialogRing.class);
		intentRingDialog.putExtra(CommonConst.PREFERENCES_PHONE_ACCOUNT, accountCommandSentFrom);
		intentRingDialog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intentRingDialog);

		ringDevice();

		// send broadcast to finish the ActivityDialogRing
		Controller.broadcsatMessage(context, BroadcastActionEnum.BROADCAST_FINISH_ACITIVTY_DIALOG_RING.toString(), 
				"Turn Off the Ring signal" + " by " + messageDataContactDetails.getAccount(), 
				BroadcastKeyEnum.finish.toString(), CommonConst.NOBODY_RESPONDED);

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	private void ringDevice(){
		String methodName = "ringDevice";
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		// Get max volume for device
		int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
		// Get current volume for device
		int originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
		if(originalVolume == 0){
			audioManager.setStreamVolume(AudioManager.STREAM_RING, 1, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		}
		
		// Get URI of default ringtone
		Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		//Get system default ring tone
		Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringUri /*Uri.parse(ringTonePath)*/);
		
		if(ringtone != null){
		    ringtone.setStreamType(AudioManager.STREAM_RING);
		    ringtone.play();
		    isRinging = true;
		}
		
		int currVoulme = 0;
		if(isRinging == true){
			try {
				// play 5 seconds and increase volume until max volume achieved
				currVoulme = originalVolume;
				int ringTimeWithMaxVolume = 0;
				while(currVoulme <= maxVolume && isRinging == true){
					int secondsCounter = 0;
					while(secondsCounter <= 5 && isRinging == true){ // 5 seconds - waiting loop
						Thread.sleep(1000);
						secondsCounter++;
					}
					// Increase ring volume
					audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_RAISE, 
						AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
					currVoulme = audioManager.getStreamVolume(AudioManager.STREAM_RING);
					Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + methodName + 
						" MaxVolume = " + maxVolume + "; CurrVoulme = " + currVoulme);
					if(currVoulme >= maxVolume && ringTimeWithMaxVolume >= CommonConst.MAX_RINGTIME_WITH_MAX_VOLUME/* * 3 */){
						// send broadcast to finish the ActivityDialogRing
						Controller.broadcsatMessage(context, BroadcastActionEnum.BROADCAST_FINISH_ACITIVTY_DIALOG_RING.toString(), 
								"Turn Off the Ring signal" + " by " + messageDataContactDetails.getAccount(), 
								BroadcastKeyEnum.finish.toString(), CommonConst.NOBODY_RESPONDED);
						break; // stop Ringtone signal

					} else {
						ringTimeWithMaxVolume++;
					}
				}
			} catch (InterruptedException e) {
				ringtone.stop();
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + methodName + 
						" Ringtone stopped.");			
				isRinging = false;
			} 
			ringtone.stop();
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + methodName + 
					" Ringtone stopped.");			
			isRinging = false;
			// Set original volume
			audioManager.setStreamVolume(AudioManager.STREAM_RING, originalVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		}
	}
	
	private boolean isPermissionToGetLocation(
			MessageDataContactDetails senderMessageDataContactDetails, 
			MessageDataContactDetails messageDataContactDetails,
			String methodName){
		
		methodName = "isPermissionToGetLocation";
		
		if(senderMessageDataContactDetails == null){
			logMessage = "Unable to check permissions: SenderMessageDataContactDetails is null";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			return false;
		}
		
		PermissionsData permissionsData = 
			DBLayer.getInstance().getPermissions(senderMessageDataContactDetails.getAccount());
    	if( permissionsData == null){
    		// Show error - no permission for "account" to invoke TrackLocation
        	logMessage = "No permissions defind for account: " + 
        		senderMessageDataContactDetails.getAccount() + 
        		". Not permitted to share location.";
        	// "Existing permissions >>> " +
        	//	gson.toJson(DBLayer.getPermissionsList(null)) + " <<< ";
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
            
    		// ===========================================================
			// ===  NOTIFICATION: NO PERMISSIONS DEFINED FOR ACCOUNT  ====
			// ===========================================================
            // Notify caller by GCM (push notification)
            
    		sendNotificationCommand(
				senderMessageDataContactDetails,
				logMessage, 
				CommandKeyEnum.permissions.toString(), 
				CommandValueEnum.not_defined.toString());

    		return false;
    	}
    	
    	int isLocationSharingPermitted = permissionsData.getIsLocationSharePermitted();
    	if( isLocationSharingPermitted != 1 ){
    		// TODO: Show error - no permission to invoke TrackLocation
    		logMessage =
    			"Not permitted to share location to " + senderMessageDataContactDetails.getAccount();
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
            
    		// ===========================================================
			// ===  NOTIFICATION: NOT PERMITTED TO SHARE FOR ACCOUNT  ====
			// ===========================================================
            // Notify caller by GCM (push notification)

    		sendNotificationCommand(
				senderMessageDataContactDetails,
				logMessage, 
				CommandKeyEnum.permissions.toString(), 
				CommandValueEnum.not_permitted.toString());

    		return false;
    	}
    	return true;
	}

	// Initialize BROADCAST_TURN_OFF_RING broadcast receiver
	private void initNotificationBroadcastReceiver() {
		String methodName = "initNotificationBroadcastReceiver";
		LogManager.LogFunctionCall(className, methodName);
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(BroadcastActionEnum.BROADCAST_TURN_OFF_RING.toString());
	    if(notificationBroadcastReceiver != null){
	    	LogManager.LogFunctionExit(className, methodName);
	    	return;
	    }
	    notificationBroadcastReceiver = new BroadcastReceiver() {

	    	Gson gson = new Gson();
	    	
			@Override
			public void onReceive(Context context, Intent intent) {
				// String methodName = "onReceive";
				Bundle bundle = intent.getExtras();
	    		if(bundle != null && bundle.containsKey(BroadcastConstEnum.data.toString())){
	    			String jsonNotificationData = bundle.getString(BroadcastConstEnum.data.toString());
	    			if(jsonNotificationData == null || jsonNotificationData.isEmpty()){
	    				return;
	    			}
	    			NotificationBroadcastData broadcastData = gson.fromJson(jsonNotificationData, NotificationBroadcastData.class);
	    			if(broadcastData == null){
	    				return;
	    			}
	    			
	    			String key  = broadcastData.getKey();
	    			
    				// Notification about command: turn off the Ring signal
	    			if(BroadcastKeyEnum.turn_off.toString().equals(key)) {
	    				isRinging = false;
	    			}
	    		}
			}
	    };
	    
	    registerReceiver(notificationBroadcastReceiver, intentFilter);
	    
		LogManager.LogFunctionExit(className, methodName);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
    	if(notificationBroadcastReceiver != null){
    		unregisterReceiver(notificationBroadcastReceiver);
    	}
	}
}
