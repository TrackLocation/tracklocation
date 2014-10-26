package com.dagrest.tracklocation.service;

import java.util.HashMap;
import java.util.List;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.R;
import com.dagrest.tracklocation.concurrent.RegisterToGCMInBackground;
import com.dagrest.tracklocation.datatype.AppInfo;
import com.dagrest.tracklocation.datatype.BroadcastActionEnum;
import com.dagrest.tracklocation.datatype.BroadcastData;
import com.dagrest.tracklocation.datatype.BroadcastKeyEnum;
import com.dagrest.tracklocation.datatype.CommandData;
import com.dagrest.tracklocation.datatype.CommandDataBasic;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.CommandKeyEnum;
import com.dagrest.tracklocation.datatype.CommandTagEnum;
import com.dagrest.tracklocation.datatype.CommandValueEnum;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.MessageDataContactDetails;
import com.dagrest.tracklocation.datatype.MessageDataLocation;
import com.dagrest.tracklocation.datatype.NotificationBroadcastData;
import com.dagrest.tracklocation.datatype.NotificationKeyEnum;
import com.dagrest.tracklocation.datatype.PermissionsData;
import com.dagrest.tracklocation.datatype.PushNotificationServiceStatusEnum;
import com.dagrest.tracklocation.datatype.SentJoinRequestData;
import com.dagrest.tracklocation.db.DBHelper;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.db.DBManager;
import com.dagrest.tracklocation.dialog.CommonDialog;
import com.dagrest.tracklocation.dialog.IDialogOnClickAction;
import com.dagrest.tracklocation.exception.CheckPlayServicesException;
import com.dagrest.tracklocation.exception.NoSentContactFromAccountException;
import com.dagrest.tracklocation.exception.NoSentContactFromException;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class GcmIntentService extends IntentService {

	private Gson gson = new Gson();
	private AppInfo appInfo = null;
	private String logMessage;
	private Context clientContext;
	private String registrationId;
	
	private String clientAccount;
	private String clientMacAddress;
	private String clientPhoneNumber;
	private String clientRegId;
	private int clientBatteryLevel;
	private GoogleCloudMessaging gcm;
	private String className;
	private String googleProjectNumber;
	
	private Context context;
	
	public GcmIntentService() {
		super("GcmIntentService");
		Log.i(CommonConst.LOG_TAG, "[INFO] {" +this.getClass().getName() + "} -> " + "GcmIntentService()");
		LogManager.LogFunctionCall(className, "GcmIntentService()");
	}

	
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i(CommonConst.LOG_TAG, "[INFO] {" +this.getClass().getName() + "} -> " + "onCreate()");
		LogManager.LogFunctionCall(className, "onCreate()");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		Log.i(CommonConst.LOG_TAG, "[INFO] {" +this.getClass().getName() + "} -> " + "onHandleIntent(Intent intent)");
		LogManager.LogFunctionCall(className, "onHandleIntent(Intent intent)");
		
		className = this.getClass().getName();
		LogManager.LogFunctionCall(className, "onHandleIntent");
		
		clientContext = getApplicationContext();
		context = getApplicationContext();
		appInfo = Controller.getAppInfo(clientContext);
		
		// The code below is taken from MainActivity function
		// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
		checkDeviceForGooglePlayServices();
		
		DBManager.initDBManagerInstance(new DBHelper(getApplicationContext()));
		googleProjectNumber = this.getResources().getString(R.string.google_project_number);
		Preferences.setPreferencesString(context, CommonConst.GOOGLE_PROJECT_NUMBER, googleProjectNumber);
		
		getClientRegistartionId();

    	// Collect client details
        initClientDetails();

        Bundle extras = intent.getExtras();
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        // LogManager.LogInfoMsg(className, "onHandleIntent", 
        //     	"messageType :" + messageType);
                
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
            	LogManager.LogInfoMsg(className, "onHandleIntent", 
                	"It is a regular GCM message");
            	
            	// ============================================
            	// COMMANDS TO RUN ON CLIENT SIDE
            	// ============================================
            	
            	// ============================================
            	// ====  COMMAND: START  ======================
            	// ============================================
                // COMMAND: Start Track Location Service on client
            	// CLIENT SIDE	
            	// command received via GCM from Master
            	// to Slave - in order to start location
            	// service on Slave
            	// ============================================
            	if(extras.containsKey(CommandTagEnum.command.toString()) &&
            			extras.getString(CommandTagEnum.command.toString()).
            			equals(CommandEnum.start.toString())){ // COMMAND START
            		
            		logMessage = "Catched push notification message (GCM): [START TrackLocation Service]";
        			LogManager.LogInfoMsg(className, "Start Track Location Service", logMessage);
        			Log.i(CommonConst.LOG_TAG, "[INFO] {" +className + "} -> " + logMessage);
        			Log.i(CommonConst.LOG_TAG, "[INFO] {" +className + "} -> ThreadID: " + Thread.currentThread().getId());
            		
        			handleCommandStartTrackLocationService(extras);
    	    		
//             	// ============================================
//             	// ====  COMMAND: STOP  =======================
//        		// ============================================
//            	// CLIENT SIDE	
//            	// command received via GCM from Master
//            	// to Slave - in order to stop location
//            	// service on Slave
//            	// ============================================
//            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
//            			extras.getString(CommandTagEnum.command.toString()).
//            			equals(CommandEnum.stop.toString())){ // COMMAND STOP 
//            		
//            		errorMsg = "Catched push notification message (GCM): [STOP ]";
//        			LogManager.LogInfoMsg(className, "Stop ", errorMsg);
//        			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + errorMsg);
//        			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> ThreadID: " + Thread.currentThread().getId());
//
//            		commandStopTrackLocationService(extras);
            		
            	// ============================================
            	// ====  COMMAND: TRACKING  ===================
            	// ============================================
                // COMMAND: Start Tracking on client
            	// CLIENT SIDE	
            	// command received via GCM from Master
            	// to Slave - in order to start tracking 
        		// service on Slave and 
        		// notify about location every certain
        		// period of time
            	// ============================================
            	} else if(extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.tracking.toString())){ // COMMAND TRACKING
                		
                		logMessage = "Catched push notification message (GCM): [START Tracking Service]";
            			LogManager.LogInfoMsg(className, "Start Tracking Service", logMessage);
            			Log.i(CommonConst.LOG_TAG, "[INFO] {" +className + "} -> " + logMessage);
            			Log.i(CommonConst.LOG_TAG, "[INFO] {" +className + "} -> ThreadID: " + Thread.currentThread().getId());
                		
            			handleCommandStartTrackingService(extras);
            			
                // ============================================
                // ====  COMMAND: STATUS_REQUEST  =============
        		// ============================================
            	// CLIENT SIDE	
            	// command received via GCM from Master
            	// to Slave - in order to check that GCM Service
            	// is registered on Slave.
            	// Send via GCM command status_response to Master
            	// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
            			extras.getString(CommandTagEnum.command.toString()).
            			equals(CommandEnum.status_request.toString())){ // COMMAND STATUS_REQUEST
            		
            		logMessage = "Catched push notification message (GCM): [STATUS_REQUEST]";
        			LogManager.LogInfoMsg(className, "Status Request", logMessage);
        			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
        			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> ThreadID: " + Thread.currentThread().getId());
            		
            		handleCommandStatusRequest(extras);
 
                // ============================================
                // ====  COMMAND: STATUS_RESPONSE  ============
        		// ============================================
            	// notification from Slave to Master via GCM
            	// and broadcast to related UI consumers on Master	
            	// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.status_response.toString())){ // COMMAND STATUS_RESPONSE
            		
            		logMessage = "Catched push notification message (GCM): [STATUS_REQUEST]";
        			LogManager.LogInfoMsg(className, "Status Response", logMessage);
        			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
        			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> ThreadID: " + Thread.currentThread().getId());
        			
            		handleCommandStatusResponse(extras);	
                		
                // ============================================
                // ====  COMMAND: LOCATION_UPDATE  ============
                // ============================================
                // notification from Slave to Master and
                // broadcast to related UI consumers on Master 		
            	// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.location.toString())){ // COMMAND LOCATION
            		
            		logMessage = "Catched push notification message (GCM): [LOCATION_UPDATE]";
        			LogManager.LogInfoMsg(className, "Location Update", logMessage);
        			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
        			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> ThreadID: " + Thread.currentThread().getId());

            		handleCommandLocationUpdate(extras);

                // ============================================
                // ====  COMMAND: JOIN_APPROVAL  ==============
                // ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.join_approval.toString())){ // COMMAND JOIN_APPROVAL
            		
            		logMessage = "Catched push notification message (GCM): [JOIN_APPROVAL]";
        			LogManager.LogInfoMsg(className, "Join Approval", logMessage);
        			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
        			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> ThreadID: " + Thread.currentThread().getId());
            		
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
            		
            		logMessage = "Catched push notification message (GCM): [TRACK_LOCATION_SERVICE_KEEP_ALIVE]";
        			LogManager.LogInfoMsg(className, "TrackLocation Service Keep Alive", logMessage);
        			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
        			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> ThreadID: " + Thread.currentThread().getId());

        			handleCommandTrackLocationKeepAlive(extras);
					
    	        // ============================================
    	        // ====  COMMAND: UPDATE_REGISTARTION_ID  =====
        		// ============================================
                } else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                    		extras.getString(CommandTagEnum.command.toString()).
                    		equals(CommandEnum.update_reg_id.toString())){ // COMMAND UPDATE_REGISTARTION_ID
                		
                	logMessage = "Catched push notification message (GCM): [UPDATE_REGISTARTION_ID]";
            		LogManager.LogInfoMsg(className, "Update registration ID", logMessage);
            		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
            		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> ThreadID: " + Thread.currentThread().getId());

            		handleCommandUpdateRegistartionID(extras);

            	// ============================================
	            // ====  COMMAND: NOTIFICATION  ===============
        		// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.notification.toString())){ // COMMAND NOTIFICATION
            		
            		logMessage = "Catched push notification message (GCM): [NOTIFICATION]";
        			LogManager.LogInfoMsg(className, "Notification", logMessage);
        			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
        			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> ThreadID: " + Thread.currentThread().getId());

        			handleCommandNotification(extras);
            	} 
        	
            } // if (GoogleCloudMessaging
            
        } // if (extras.isEmpty())...
        
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
        
        LogManager.LogFunctionExit(className, "onHandleIntent");
        
	} // onHandleIntent(...
	
	private void initClientDetails(){
		clientAccount = Preferences.getPreferencesString(clientContext, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		clientMacAddress = Preferences.getPreferencesString(clientContext, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
		clientPhoneNumber = Preferences.getPreferencesString(clientContext, CommonConst.PREFERENCES_PHONE_NUMBER);
		clientRegId = Preferences.getPreferencesString(clientContext, CommonConst.PREFERENCES_REG_ID);
		clientBatteryLevel = Controller.getBatteryLevel(clientContext);
	}
	
	// The code below is taken from MainActivity function
	// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
	private void checkDeviceForGooglePlayServices(){
		
		String methodName = "checkDeviceForGooglePlayServices";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
		
		try {
			Controller.checkPlayServices(clientContext);
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
    		
			LogManager.LogFunctionExit(className, methodName);
			Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
		}
	}
	
	private void getClientRegistartionId(){
		
		String methodName = "getClientRegistartionId";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
    	Thread registerToGCMInBackgroundThread;
    	Runnable registerToGCMInBackground;

		gcm = GoogleCloudMessaging.getInstance(this);
        registrationId = Preferences.getPreferencesString(clientContext, CommonConst.PREFERENCES_REG_ID);
        if (registrationId == null || registrationId.isEmpty()) {
            // registerInBackground();
        	HashMap<String, Object> map = new HashMap<String, Object>();
        	map.put("GoogleCloudMessaging", gcm);
        	map.put("GoogleProjectNumber", getResources().getString(R.string.google_project_number));
        	map.put("Context", clientContext);
        	
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
		Preferences.setPreferencesReturnToContactMap(clientContext, 
			accountCommandSentFrom, contactDetailsSentFrom.getRegId());
		
		return contactDetailsSentFrom;
	}
	
	private void handleCommandStartTrackLocationService(Bundle extras){
		
		String methodName = "handleCommandStartTrackLocationService";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
		
		MessageDataContactDetails senderMessageDataContactDetails;
		String senderAccount;
		
		try {
			senderMessageDataContactDetails = initCommand(extras);
			senderAccount = senderMessageDataContactDetails.getAccount();
		} catch (NoSentContactFromException e) {
			logMessage = "Failed to start TrackLocationService - NoSentContactFrom details";
			LogManager.LogException(e, logMessage, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
			return;
		} catch (NoSentContactFromAccountException e) {
			logMessage = "Failed to start TrackLocationService - NoSentContactFromAccount details";
			LogManager.LogException(e, logMessage, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
			return;
		}
		
		MessageDataContactDetails messageDataContactDetails = new MessageDataContactDetails(
			clientAccount, clientMacAddress, clientPhoneNumber,
			clientRegId, clientBatteryLevel);
		ContactDeviceDataList contactDeviceDataToSendNotificationTo = new ContactDeviceDataList(
			senderMessageDataContactDetails.getAccount(),
			senderMessageDataContactDetails.getMacAddress(),
			senderMessageDataContactDetails.getPhoneNumber(),
			senderMessageDataContactDetails.getRegId(), null);

		if(isPermissionToGetLocation(senderAccount, contactDeviceDataToSendNotificationTo, 
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
		if(extras.containsKey(CommandTagEnum.interval.toString())){ // PARAMETER INTERVAL
			String intervalString = extras.getString(CommandTagEnum.interval.toString());
            Preferences.setPreferencesString(getApplicationContext(), 
            	CommonConst.LOCATION_SERVICE_INTERVAL, intervalString);
		}
		
        String msgServiceStarted;
        String notificationKey;
        String notificationValue;
        
		// ===========================================================
		// Start TrackLocation service to get current location
		// ===========================================================
		Intent trackLocationService = new Intent(clientContext, TrackLocationService.class);
		String jsonSenderMessageDataContactDetails = gson.toJson(senderMessageDataContactDetails);
		//String jsonContactDetailsSentFrom = gson.toJson(contactDetailsSentFrom, MessageDataContactDetails.class);
		trackLocationService.putExtra(CommonConst.START_CMD_SENDER_MESSAGE_DATA_CONTACT_DETAILS, jsonSenderMessageDataContactDetails);
//		trackLocationService.putExtra(CommonConst.REGISTRATION_ID_TO_RETURN_MESSAGE_TO, 
//				contactDetailsSentFrom.getRegId());
		ComponentName componentName = clientContext.startService(trackLocationService); 
		if(componentName != null){
			// Notify that TrackLoactionService was started - by GCM (push notification)
			msgServiceStarted = "TrackLocationService is starting by [" + clientAccount + "]";
			notificationKey = CommandKeyEnum.starting_status.toString();
			notificationValue = CommandValueEnum.success.toString();
			LogManager.LogInfoMsg(className, methodName, msgServiceStarted);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> ThreadID: " + msgServiceStarted);
		} else {
			// Notify that TrackLoactionService was not started - by GCM (push notification)
			msgServiceStarted = "Failed to start TrackLocationService on [" + clientAccount + "]";
			notificationKey = CommandKeyEnum.starting_status.toString();
			notificationValue = CommandValueEnum.error.toString();		
			LogManager.LogErrorMsg(className, methodName, msgServiceStarted);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + msgServiceStarted);
		}
		
		// ===========================================================
		// ===  NOTIFICATION: START COMMAND STATUS  ==================
		// ===========================================================
		// Notify caller by GCM (push notification)
		CommandDataBasic commandDataBasic = new CommandData(
			getApplicationContext(), 
			contactDeviceDataToSendNotificationTo, 
			CommandEnum.notification, 
			msgServiceStarted, 
			messageDataContactDetails, 
			null, 					// location
			notificationKey, 
			notificationValue,
			appInfo
		);
		commandDataBasic.sendCommand();
		
        LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	private void handleCommandStopTrackLocationService(Bundle extras){
		
		String methodName = "handleCommandStopTrackLocationService";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL]  {" + className + "} -> " + methodName);

		Intent trackLocationService = new Intent(context, TrackLocationService.class);
		boolean result = context.stopService(trackLocationService); 
		
		LogManager.LogInfoMsg(className, methodName, "Servise stopped: " + result);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + "Servise stopped: " + result);

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	private void handleCommandStatusRequest(Bundle extras){
		
		String methodName = "handleCommandStatusRequest";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);

		MessageDataContactDetails contactDetailsSentFrom;
		String accountCommandSentFrom;
		
		try {
			contactDetailsSentFrom = initCommand(extras);
			accountCommandSentFrom = contactDetailsSentFrom.getAccount();
		} catch (NoSentContactFromException e) {
			return;
		} catch (NoSentContactFromAccountException e) {
			return;
		}

        MessageDataContactDetails senderMessageDataContactDetails = new MessageDataContactDetails(clientAccount, 
            	clientMacAddress, clientPhoneNumber, clientRegId, clientBatteryLevel);
        ContactDeviceDataList contactDeviceDataToSendNotificationTo = 
        	new ContactDeviceDataList (
        		accountCommandSentFrom, 
        		contactDetailsSentFrom.getMacAddress(), 
        		contactDetailsSentFrom.getPhoneNumber(), 
        		contactDetailsSentFrom.getRegId(), 
        		null);
        // Notify caller by GCM (push notification)
        CommandDataBasic commandDataBasic = new CommandData(
			getApplicationContext(), 
			contactDeviceDataToSendNotificationTo, 
			CommandEnum.status_response, 
			logMessage, 
			senderMessageDataContactDetails, 
			null, // location
    		NotificationKeyEnum.pushNotificationServiceStatus.toString(),
    		PushNotificationServiceStatusEnum.available.toString(),
			appInfo
		);
        commandDataBasic.sendCommand();

/*		
		List<String> listRegIDs = null; //new ArrayList<String>();
		// get regID of the current client as a requester
//		String regIDToReturnMessageTo = extras.getString(CommonConst.REGISTRATION_ID_TO_RETURN_MESSAGE_TO);
//		if(regIDToReturnMessageTo != null){
    	if(clientRegId != null){
			            			
//			// update (insert/add) regIds to list of contacts that will be notified
//			listRegIDs = Preferences.setPreferencesReturnToRegIDList(clientContext, 
//				CommonConst.PREFERENCES_RETURN_TO_REG_ID_LIST, clientRegId); 
 			//  listRegIDs = Utils.splitLine(
			//	   Preferences.getPreferencesString(getApplicationContext(), CommonConst.PREFERENCES_RETURN_TO_REG_ID_LIST), 
			//	   CommonConst.DELIMITER_STRING);
			
    		String time = new Date().toString(); 
    			            		
    		MessageDataContactDetails contactDetails = new MessageDataContactDetails(
    			clientAccount, clientMacAddress, clientPhoneNumber, clientRegId, clientBatteryLevel);
    		MessageDataLocation location = null;

    		String jsonMessage = Controller.createJsonMessage(
    			new JsonMessageData(
    				listRegIDs, 
    	    		//regIDToReturnMessageTo, 
    	    		CommandEnum.status_response, 
    	    		null, 
    	    		contactDetails,
    	    		location,
    	    		appInfo,
    	    		time,
    	    		NotificationKeyEnum.pushNotificationServiceStatus.toString(),
    	    		PushNotificationServiceStatusEnum.available.toString()
    	    	)
    		);
			if(jsonMessage == null){
				errorMsg = "Failed to create JSON Message to send to recipient";
				LogManager.LogErrorMsg(className, "[Command:" + CommandEnum.status_request.toString() + "]", errorMsg);
				return;
			} else {
        		// send message back with PushNotificationServiceStatusEnum.available
        		Controller.sendCommand(jsonMessage);
			}
    	} 
*/    	
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	private void handleCommandStatusResponse(Bundle extras){
		
		String methodName = "handleCommandStatusResponse";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
		
		String key = extras.getString(CommandTagEnum.key.toString());
		String value = extras.getString(CommandTagEnum.value.toString());
		String currentDateTime = Controller.getCurrentDate();
		
//		broadcastLocationUpdatedGps(key + CommonConst.DELIMITER_STRING +
//			value + CommonConst.DELIMITER_STRING + currentDateTime);
		if(value != null && !value.isEmpty()) {
			Controller.broadcastMessage(GcmIntentService.this, 
				BroadcastActionEnum.BROADCAST_LOCATION_UPDATED.toString(), 
				"GcmIntentService",
				null, 
				BroadcastKeyEnum.gcm_status.toString(),  
				key + CommonConst.DELIMITER_STRING + value + CommonConst.DELIMITER_STRING + currentDateTime);
		} else {
			Controller.broadcastMessage(GcmIntentService.this,
				BroadcastActionEnum.BROADCAST_LOCATION_UPDATED.toString(), 
				"GcmIntentService",
				null, 
				BroadcastKeyEnum.gcm_status.toString(),  
				"");
		}
		
		logMessage = "StatusResponse key = " + key;
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		logMessage = "StatusResponse value = " + value;
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	private void handleCommandLocationUpdate(Bundle extras){

		String methodName = "handleCommandLocationUpdate";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);

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
		String jsonLocation = extras.getString("location");

		MessageDataContactDetails contactDetails = 
			gson.fromJson(jsonContactDetails, MessageDataContactDetails.class);
		MessageDataLocation location = 
			gson.fromJson(jsonLocation, MessageDataLocation.class);

		logMessage = "Location of [" + contactDetails.getAccount() + "]: " + jsonLocation;
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		BroadcastData broadcastData = new BroadcastData();
		broadcastData.setContactDetails(contactDetails);
		broadcastData.setLocation(location);
		String jsonBroadcastData = gson.toJson(broadcastData);
		
		Controller.broadcastMessage(GcmIntentService.this, 
			BroadcastActionEnum.BROADCAST_LOCATION_UPDATED.toString(), 
			"GcmIntentService",
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
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
		
		String jsonContactDetailsSentFrom = extras.getString(CommandTagEnum.contactDetails.toString());
		
		MessageDataContactDetails contactDetailsSentFrom = 
			gson.fromJson(jsonContactDetailsSentFrom, MessageDataContactDetails.class);
		if(contactDetailsSentFrom == null){
			logMessage = "Join approval request failed. No SentFrom contact details.";
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "." + methodName + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
            return;
		}
		
		String accountCommandSentFrom = null;
		accountCommandSentFrom = contactDetailsSentFrom.getAccount();
		if(accountCommandSentFrom == null){
			logMessage = "Join approval request failed. No account/email.";
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "." + methodName + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
            return;
		}

		String registrationIdJoinApproval = null;
		registrationIdJoinApproval = contactDetailsSentFrom.getRegId();
		if(registrationIdJoinApproval == null){
			logMessage = "Join approval request failed. No registrationID.";
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "." + methodName + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
            return;
		}
		
		String phoneNumber = contactDetailsSentFrom.getPhoneNumber();
		String macAddress = contactDetailsSentFrom.getMacAddress();

		String key = extras.getString(CommandTagEnum.key.toString());
		String mutualId = null;
		if(CommandKeyEnum.mutualId.toString().equals(key)){
			mutualId = extras.getString(CommandTagEnum.value.toString()); // mutualId
		}
		String currentDateTime = Controller.getCurrentDate();
		
//		// Insert into TABLE_PERMISSIONS account(email) according to mutualId
//		long countAdded = DBLayer.addPermissions(email, 0, 0, 0);
//		PermissionsData permissionData = DBLayer.getPermissions(email);
		
		if(mutualId != null){
			SentJoinRequestData sentJoinRequestData = DBLayer.getSentJoinRequestByMutualId(mutualId);
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
			int count = DBLayer.deleteSentJoinRequest(mutualId);
			if( count < 1) {
				logMessage = "Failed to delete sent join request with mutual id: " + mutualId + " count = " + count;
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.w(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			} else {
				logMessage = "Deleted sent join request with mutual id: " + mutualId + " count = " + count;
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.w(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			}
			
			sentJoinRequestData = DBLayer.getSentJoinRequestByMutualId(mutualId);
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
		DBLayer.addContactDeviceDataList(new ContactDeviceDataList(accountCommandSentFrom,
				macAddress, phoneNumber, registrationIdJoinApproval, null));
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	private void handleCommandTrackLocationKeepAlive(Bundle extras){

		String methodName = "handleCommandTrackLocationKeepAlive";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
		
		String key = extras.getString(CommandTagEnum.key.toString());
		String value = extras.getString(CommandTagEnum.value.toString());
		
		String jsonContactDetails = extras.getString(CommandTagEnum.contactDetails.toString());
		MessageDataContactDetails contactDetails = 
				gson.fromJson(jsonContactDetails, MessageDataContactDetails.class);
		MessageDataLocation location = null;
		BroadcastData broadcastData = new BroadcastData();
		broadcastData.setContactDetails(contactDetails);
		broadcastData.setLocation(location);
		broadcastData.setKey(BroadcastKeyEnum.keep_alive.toString());
		broadcastData.setValue(value);
		String jsonBroadcastData = gson.toJson(broadcastData);

		Controller.broadcastMessage(GcmIntentService.this, 
			BroadcastActionEnum.BROADCAST_LOCATION_KEEP_ALIVE.toString(),
			"GcmIntentService",
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
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
		
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
		MessageDataContactDetails contactDetails = gson.fromJson(jsonContactDetails, MessageDataContactDetails.class);
		String senderAccount = null;
		if(contactDetails == null){
			logMessage = "[contactDetails]: not defined";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			return;
		} else {
			senderAccount = contactDetails.getAccount();
		}
		if(senderAccount == null || senderAccount.isEmpty()){
			logMessage = "[senderAccount]: not defined";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			return;
		}		
		
		logMessage = "Sent by [" + senderAccount + "]";
		LogManager.LogInfoMsg(className, methodName, "");
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);

		String jsonListAccounts = Preferences.getPreferencesString(clientContext, 
        		CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS);
		logMessage = "Recipients accounts list: [" + jsonListAccounts + "]";
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		// ============================================================
		// TrackLocation Service started - notification received
		// ============================================================
		if(CommandKeyEnum.start_status.toString().equals(key) && 
				CommandValueEnum.success.toString().equals(value)){
			
			Controller.removeSenderAccountFromSendCommandList(context, 
				jsonListAccounts, senderAccount);

			// TODO: For test only:
			jsonListAccounts = Preferences.getPreferencesString(clientContext, 
	        	CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS);
			
//			// TODO: Broadcast - the following recipients are not available
//			if(jsonListAccounts != null && !jsonListAccounts.isEmpty()){
//				String broadcastMessage = "The following recipients are unavailable: " + jsonListAccounts;
//				broadcsatMessage(broadcastMessage, key, value);
//			}
			
		// ============================================================
		//  - notification received
		// ============================================================
		} else if (CommandKeyEnum.permissions.toString().equals(key) && 
				CommandValueEnum.not_defined.toString().equals(value)){
			Controller.removeSenderAccountFromSendCommandList(context, 
					jsonListAccounts, senderAccount);
			Controller.broadcsatMessage(context, msg + " by " + senderAccount, key, value);
		} else if (CommandKeyEnum.permissions.toString().equals(key) && 
				CommandValueEnum.not_permitted.toString().equals(value)){
			Controller.removeSenderAccountFromSendCommandList(context, 
					jsonListAccounts, senderAccount);
			Controller.broadcsatMessage(context, msg + " by " + senderAccount, key, value);
		}

// TODO: Delete from here - not needed - ONLY as example of BROADCAST:
//		
//		NotificationBroadcastData notificationBroadcastData = new NotificationBroadcastData();
//		notificationBroadcastData.setMessage(extras.getString("message"));
//		notificationBroadcastData.setKey(key);
//		notificationBroadcastData.setValue(value);
//		String jsonNotificationBroadcastData = gson.toJson(notificationBroadcastData);
//		
//		MessageDataContactDetails mdcd = 
//			gson.fromJson(extras.getString(CommandTagEnum.contactDetails.toString()), MessageDataContactDetails.class);
//
//		// Broadcast corresponding message
//		Controller.broadcastMessage(GcmIntentService.this, 
//			BroadcastActionEnum.BROADCAST_MESSAGE.toString(), 
//			"GcmIntentService",
//			jsonNotificationBroadcastData, 
//			key, // BroadcastKeyEnum.message.toString(),  
//			value);
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	private void handleCommandUpdateRegistartionID(Bundle extras){
		String methodName = "handleCommandUpdateRegistartionID";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
		
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
		MessageDataContactDetails contactDetails = gson.fromJson(jsonContactDetails, MessageDataContactDetails.class);
		String senderAccount = null;
		String senderMacAddress = null;
		if(contactDetails == null){
			logMessage = "Failed to update RegistartionID - [contactDetails]: not defined";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			return;
		} else {
			senderAccount = contactDetails.getAccount();
			senderMacAddress = contactDetails.getMacAddress();
		}
		if(senderAccount == null || senderAccount.isEmpty()){
			logMessage = "Failed to update RegistartionID - [senderAccount]: not defined";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			return;
		}		
		if(senderMacAddress == null || senderMacAddress.isEmpty()){
			logMessage = "Failed to update RegistartionID - [senderMacAddress]: not defined";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			return;
		}		
		
		logMessage = "Sent by [" + senderAccount + "]";
		LogManager.LogInfoMsg(className, methodName, "");
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);

		// Update registrationID - sent by contact,
		// that registered its application against Google Cloud Message
		// Input parameters: value(as RegistrationID), senderAccount, senderMacAddress
		long result = DBLayer.updateRegistrationID(senderAccount, senderMacAddress, value);
		if(result == -1){
			logMessage = "Failed to update RegistartionID";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
	}
	
	private void handleCommandStartTrackingService(Bundle extras){
		String methodName = "handleCommandStartTrackingService";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
		
		MessageDataContactDetails senderMessageDataContactDetails;
		String senderAccount;
		
		try {
			senderMessageDataContactDetails = initCommand(extras);
			senderAccount = senderMessageDataContactDetails.getAccount();
		} catch (NoSentContactFromException e) {
			logMessage = "Failed to start TrackLocationService - NoSentContactFrom details";
			LogManager.LogException(e, logMessage, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
			return;
		} catch (NoSentContactFromAccountException e) {
			logMessage = "Failed to start TrackLocationService - NoSentContactFromAccount details";
			LogManager.LogException(e, logMessage, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
			return;
		}
		
		MessageDataContactDetails messageDataContactDetails = new MessageDataContactDetails(
			clientAccount, clientMacAddress, clientPhoneNumber,
			clientRegId, clientBatteryLevel);
		ContactDeviceDataList contactDeviceDataToSendNotificationTo = new ContactDeviceDataList(
			senderMessageDataContactDetails.getAccount(),
			senderMessageDataContactDetails.getMacAddress(),
			senderMessageDataContactDetails.getPhoneNumber(),
			senderMessageDataContactDetails.getRegId(), null);

		if(isPermissionToGetLocation(senderAccount, contactDeviceDataToSendNotificationTo, 
				messageDataContactDetails, methodName) == false){
			return;
		}
	}
	
	private boolean isPermissionToGetLocation(String senderAccount, 
			ContactDeviceDataList contactDeviceDataToSendNotificationTo, 
			MessageDataContactDetails messageDataContactDetails,
			String methodName){
		PermissionsData permissionsData = DBLayer.getPermissions(senderAccount);
    	if( permissionsData == null){
    		// Show error - no permission for "account" to invoke TrackLocation
        	logMessage = "No permissions defind for account: " + senderAccount + 
        		". Not permitted to share location.";
        	// "Existing permissions >>> " +
        	//	gson.toJson(DBLayer.getPermissionsList(null)) + " <<< ";
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
            
			// ===========================================================
			// ===  NOTIFICATION: NO PERMISSIONS DEFINED FOR ACCOUNT  ====
			// ===========================================================
            // Notify caller by GCM (push notification)
            CommandDataBasic commandDataBasic = new CommandData(
				getApplicationContext(), 
				contactDeviceDataToSendNotificationTo, 
    			CommandEnum.notification, 
    			logMessage, 
    			messageDataContactDetails, 
    			null, // location
    			CommandKeyEnum.permissions.toString(), // key
    			CommandValueEnum.not_defined.toString(), // value
    			appInfo
    		);
            commandDataBasic.sendCommand();
            
    		return false;
    	}
    	
    	int isLocationSharingPermitted = permissionsData.getIsLocationSharePermitted();
    	if( isLocationSharingPermitted != 1 ){
    		// TODO: Show error - no permission to invoke TrackLocation
    		logMessage = "Not permitted to share location to " + senderAccount;
            Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
            LogManager.LogErrorMsg(className, methodName, logMessage);
            
			// ===========================================================
			// ===  NOTIFICATION: NOT PERMITTED TO SHARE FOR ACCOUNT  ====
			// ===========================================================
            // Notify caller by GCM (push notification)
            CommandDataBasic commandDataBasic = new CommandData(
				getApplicationContext(), 
				contactDeviceDataToSendNotificationTo, 
    			CommandEnum.notification, 
    			logMessage, 
    			messageDataContactDetails, 
    			null, // location
    			CommandKeyEnum.permissions.toString(), // key
    			CommandValueEnum.not_permitted.toString(), // value
    			appInfo
    		);
            commandDataBasic.sendCommand();

    		return false;
    	}
    	return true;
	}
	
//	private static void broadcsatMessage(Context context, String message, String key, String value){
//		NotificationBroadcastData notificationBroadcastData = new NotificationBroadcastData();
//		notificationBroadcastData.setMessage(message);
//		notificationBroadcastData.setKey(key);
//		notificationBroadcastData.setValue(value);
//		Gson gson = new Gson();
//		String jsonNotificationBroadcastData = gson.toJson(notificationBroadcastData);
//		
////		MessageDataContactDetails mdcd = 
////			gson.fromJson(extras.getString(CommandTagEnum.contactDetails.toString()), MessageDataContactDetails.class);
//
//		// Broadcast corresponding message
//		Controller.broadcastMessage(context, 
//			BroadcastActionEnum.BROADCAST_MESSAGE.toString(), 
//			"GcmIntentService",
//			jsonNotificationBroadcastData, 
//			key, // BroadcastKeyEnum.message.toString(),  
//			value
//		);
//	}
}
