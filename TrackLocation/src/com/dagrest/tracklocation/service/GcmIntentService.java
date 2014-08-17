package com.dagrest.tracklocation.service;

import java.util.HashMap;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.R;
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
import com.dagrest.tracklocation.exception.CheckPlayServicesException;
import com.dagrest.tracklocation.exception.NoSentContactFromAccountException;
import com.dagrest.tracklocation.exception.NoSentContactFromException;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class GcmIntentService extends IntentService {

	private Gson gson = new Gson();
	private AppInfo appInfo = null;
	private String errorMsg;
	private Context clientContext;
	private String registrationId;
	
	private String clientAccount;
	private String clientMacAddress;
	private String clientPhoneNumber;
	private String clientRegId;
	private int clientBatteryLevel;
	private GoogleCloudMessaging gcm;
	private String className;
	
	private Context context;
	
	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		className = this.getClass().getName();
		LogManager.LogFunctionCall(className, "onHandleIntent");
		
		clientContext = getApplicationContext();
		context = getApplicationContext();
		appInfo = Controller.getAppInfo(clientContext);
		
		// The code below is taken from MainActivity function
		// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
		checkDeviceForGooglePlayServices();
		
		getClientRegistartionId();

    	// Collect client details
        initClientDetails();

		DBManager.initDBManagerInstance(new DBHelper(getApplicationContext()));
		
        Bundle extras = intent.getExtras();
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        LogManager.LogInfoMsg(className, "onHandleIntent", 
            	"messageType :" + messageType);
                
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
            		
            		commandStartTrackLocationService(extras);
    	    		
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
//            		commandStopTrackLocationService(extras);
            		
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
            		
            		commandStatusRequest(extras);
 
                // ============================================
                // ====  COMMAND: STATUS_RESPONSE  ============
        		// ============================================
            	// notification from Slave to Master via GCM
            	// and broadcast to related UI consumers on Master	
            	// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.status_response.toString())){ // COMMAND STATUS_RESPONSE
            		
            		commandStatusResponse(extras);	
                		
                // ============================================
                // ====  COMMAND: LOCATION_UPDATE  ============
                // ============================================
                // notification from Slave to Master and
                // broadcast to related UI consumers on Master 		
            	// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.location.toString())){ // COMMAND LOCATION
            		
            		commandLocationUpdare(extras);

                // ============================================
                // ====  COMMAND: JOIN_APPROVAL  ==============
                // ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.join_approval.toString())){ // COMMAND JOIN_APPROVAL
            		
            		commandJoinApproval(extras);            		
                		
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
            		
            		commandTrackLocationKeepAlive(extras);
					
	            // ============================================
	            // ====  COMMAND: NOTIFICATION  ===============
        		// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.notification.toString())){ // COMMAND NOTIFICATION
            		
            		commandNotification(extras);
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
		try {
			Controller.checkPlayServices(clientContext);
		} catch (CheckPlayServicesException e) {
			String errorMessage = e.getMessage();
			if(CommonConst.PLAYSERVICES_ERROR.equals(errorMessage)){
	            //GooglePlayServicesUtil.getErrorDialog(e.getResultCode(), this,
	            //	PLAY_SERVICES_RESOLUTION_REQUEST).show();
	            Log.e(CommonConst.LOG_TAG, errorMessage + " .Error code: " + e.getResultCode());
	    		LogManager.LogInfoMsg(className, "onCreate", 
	    			errorMessage + " .Error code: " + e.getResultCode());
			} else if(CommonConst.PLAYSERVICES_DEVICE_NOT_SUPPORTED.equals(errorMessage)){
				// Show dialog with errorMessage
				// showNotificationDialog(errorMessage);
				String errorMsg = "Current device is not supported by Google Play Services ";
	            Log.e(CommonConst.LOG_TAG, errorMsg);
	    		LogManager.LogInfoMsg(className, "onCreate", 
	    			errorMsg);
			}
            Log.e(CommonConst.LOG_TAG, "No valid Google Play Services APK found.");
    		LogManager.LogInfoMsg(className, "onCreate", 
    			"No valid Google Play Services APK found.");
			//finish();
		}
	}
	
	private void getClientRegistartionId(){
		gcm = GoogleCloudMessaging.getInstance(this);
        registrationId = Preferences.getPreferencesString(clientContext, CommonConst.PREFERENCES_REG_ID);
        if (registrationId == null || registrationId.isEmpty()) {
            // registerInBackground();
        	HashMap<String, Object> map = new HashMap<String, Object>();
        	map.put("GoogleCloudMessaging", gcm);
        	map.put("GoogleProjectNumber", getResources().getString(R.string.google_project_number));
        	map.put("Context", clientContext);
        	Controller.registerInBackground(map);
        }
	}
	
	private MessageDataContactDetails initCommand(Bundle extras) 
			throws NoSentContactFromException, NoSentContactFromAccountException{
		
		String jsonContactDetailsSentFrom = extras.getString("contactDetails");
		
		MessageDataContactDetails contactDetailsSentFrom = 
			gson.fromJson(jsonContactDetailsSentFrom, MessageDataContactDetails.class);
		if(contactDetailsSentFrom == null){
			errorMsg = "No SentFrom contact details. Cannot start TrackLocation Service.";
            Log.e(CommonConst.LOG_TAG, errorMsg);
            LogManager.LogErrorMsg(className, "GcmIntentService->onHandleIntent->[COMMAND:start]", errorMsg);
            throw new NoSentContactFromException("");
		}
		
		String accountCommandSentFrom = null;
		accountCommandSentFrom = contactDetailsSentFrom.getAccount();
		if(accountCommandSentFrom == null){
			errorMsg = "No SentFrom contact details - account/email. Cannot start TrackLocation Service.";
            Log.e(CommonConst.LOG_TAG, errorMsg);
            LogManager.LogErrorMsg(className, "GcmIntentService->onHandleIntent->[COMMAND:start]", errorMsg);
            throw new NoSentContactFromAccountException("");
		}
		
		// update (insert/add) regIds to list of contacts that will be notified
		Preferences.setPreferencesReturnToContactMap(clientContext, 
			accountCommandSentFrom, contactDetailsSentFrom.getRegId());
		
		return contactDetailsSentFrom;
	}
	
	private void commandStartTrackLocationService(Bundle extras){
		
		MessageDataContactDetails contactDetailsSentFrom;
		String accountCommandSentFrom;
		
		try {
			contactDetailsSentFrom = initCommand(extras);
			accountCommandSentFrom = contactDetailsSentFrom.getAccount();
		} catch (NoSentContactFromException e) {
			errorMsg = "Failed to start TrackLocationService - NoSentContactFrom details";
			LogManager.LogErrorMsg(className, "commandStartTrackLocationService", errorMsg);
			Log.e(CommonConst.LOG_TAG, errorMsg);
			return;
		} catch (NoSentContactFromAccountException e) {
			errorMsg = "Failed to start TrackLocationService - NoSentContactFromAccount details";
			LogManager.LogErrorMsg(className, "commandStartTrackLocationService", errorMsg);
			Log.e(CommonConst.LOG_TAG, errorMsg);
			return;
		}
		
    	PermissionsData permissionsData = DBLayer.getPermissions(accountCommandSentFrom);
    	if( permissionsData == null){
    		// TODO: Show error - no permission for "account" to invoke TrackLocation
   		
        	errorMsg = "No permissions defind for account: " + accountCommandSentFrom + 
        		". Not permitted to share location.";
        	// "Existing permissions >>> " +
        	//	gson.toJson(DBLayer.getPermissionsList(null)) + " <<< ";
            Log.e(CommonConst.LOG_TAG, errorMsg);
            LogManager.LogErrorMsg(className, "GcmIntentService->onHandleIntent->[COMMAND:start]", errorMsg);
            
            MessageDataContactDetails contactDetails = new MessageDataContactDetails(clientAccount, 
                	clientMacAddress, clientPhoneNumber, clientRegId, clientBatteryLevel);
            ContactDeviceDataList contactDeviceDataToSendNotificationTo = 
            	new ContactDeviceDataList (
            		contactDetailsSentFrom.getAccount(), 
            		contactDetailsSentFrom.getMacAddress(), 
            		contactDetailsSentFrom.getPhoneNumber(), 
            		contactDetailsSentFrom.getRegId(), 
            		null);
            // Notify caller by GCM (push notification)
            CommandDataBasic commandDataBasic = new CommandData(
				getApplicationContext(), 
				contactDeviceDataToSendNotificationTo, 
    			CommandEnum.notification, 
    			errorMsg, 
    			contactDetails, 
    			null, // location
    			null, // key
    			null, // value
    			appInfo
    		);
            commandDataBasic.sendCommand();
    		
    		return;
    	}
    	
    	int isLocationSharingPermitted = permissionsData.getIsLocationSharePermitted();
    	if( isLocationSharingPermitted != 1 ){
    		// TODO: Show error - no permission to invoke TrackLocation
    		errorMsg = "Not permitted to share location to " + accountCommandSentFrom;
            Log.e(CommonConst.LOG_TAG, errorMsg);
            LogManager.LogErrorMsg(className, "GcmIntentService->onHandleIntent->[COMMAND:start]", errorMsg);
            
            // Notify to caller by GCM (push notification)
            MessageDataContactDetails contactDetails = new MessageDataContactDetails(clientAccount, 
                	clientMacAddress, clientPhoneNumber, clientRegId, clientBatteryLevel);
            ContactDeviceDataList contactDeviceDataToSendNotificationTo = 
            	new ContactDeviceDataList (
                		contactDetailsSentFrom.getAccount(), 
                		contactDetailsSentFrom.getMacAddress(), 
                		contactDetailsSentFrom.getPhoneNumber(), 
                		contactDetailsSentFrom.getRegId(), 
            			null);
            // Notify caller by GCM (push notification)
            CommandDataBasic commandDataBasic = new CommandData(
				getApplicationContext(), 
				contactDeviceDataToSendNotificationTo, 
    			CommandEnum.notification, 
    			errorMsg, 
    			contactDetails, 
    			null, // location
    			null, // key
    			null, // value
    			appInfo
    		);
            commandDataBasic.sendCommand();

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
		
		// Start location service to get current location
		Intent trackLocationService = new Intent(clientContext, TrackLocationService.class);
		trackLocationService.putExtra(CommonConst.REGISTRATION_ID_TO_RETURN_MESSAGE_TO, 
			contactDetailsSentFrom.getRegId());
		
        MessageDataContactDetails contactDetails = new MessageDataContactDetails(clientAccount, 
            clientMacAddress, clientPhoneNumber, clientRegId, clientBatteryLevel);
        ContactDeviceDataList contactDeviceDataToSendNotificationTo = 
        	new ContactDeviceDataList (
            	contactDetailsSentFrom.getAccount(), 
            	contactDetailsSentFrom.getMacAddress(), 
            	contactDetailsSentFrom.getPhoneNumber(), 
            	contactDetailsSentFrom.getRegId(), 
        		null);
        String msgServiceStarted;
        String notificationKey;
        String notificationValue;
		ComponentName componentName = clientContext.startService(trackLocationService); 
		if(componentName != null){
			// Notify that TrackLoactionService was started - by GCM (push notification)
			msgServiceStarted = "TrackLocationService was started on " + clientAccount;
			notificationKey = CommandKeyEnum.status.toString();
			notificationValue = CommandValueEnum.success.toString();		
		} else {
			// Notify that TrackLoactionService was not started - by GCM (push notification)
			msgServiceStarted = "Failed to start TrackLocationService on " + clientAccount;
			notificationKey = CommandKeyEnum.status.toString();
			notificationValue = CommandValueEnum.error.toString();		
		}
        // Notify caller by GCM (push notification)
		CommandDataBasic commandDataBasic = new CommandData(
			getApplicationContext(), 
			contactDeviceDataToSendNotificationTo, 
			CommandEnum.notification, 
			msgServiceStarted, 
			contactDetails, 
			null, 					// location
			notificationKey, 
			notificationValue,
			appInfo
		);
		commandDataBasic.sendCommand();
	}
	
	private void commandStopTrackLocationService(Bundle extras){
		Intent trackLocationService = new Intent(context, TrackLocationService.class);
		boolean result = context.stopService(trackLocationService); 
		Log.i(LOCATION_SERVICE, "Servise stopped: " + result);
	}
	
	private void commandStatusRequest(Bundle extras){
		
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

        MessageDataContactDetails contactDetails = new MessageDataContactDetails(clientAccount, 
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
			errorMsg, 
			contactDetails, 
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
	}
	
	private void commandStatusResponse(Bundle extras){
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
	}
	
	private void commandLocationUpdare(Bundle extras){
		String key = extras.getString(CommandTagEnum.key.toString());
		String value = extras.getString(CommandTagEnum.value.toString());
		String currentDateTime = Controller.getCurrentDate();
		
		String jsonContactDetails = extras.getString("contactDetails");
		String jsonLocation = extras.getString("location");
		MessageDataContactDetails contactDetails = 
			gson.fromJson(jsonContactDetails, MessageDataContactDetails.class);
		MessageDataLocation location = 
			gson.fromJson(jsonLocation, MessageDataLocation.class);
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
	}
	
	private void commandJoinApproval(Bundle extras){
		String message = extras.getString("message"); // email, regId, phoneNumber
		
		String messageArray[] = message.split(CommonConst.DELIMITER_COMMA);
		String email = null;
		String registrationIdJoinApproval = null;
		String phoneNumber = null;
		String macAddress = null;
		if(messageArray.length == 4){
			email = messageArray[0];
			registrationIdJoinApproval = messageArray[1];
			phoneNumber = messageArray[2];
			macAddress = messageArray[3];
		} else {
			// Join approval request failed
			errorMsg = "Join approval request failed - not all contact details were provided.";
            Log.e(CommonConst.LOG_TAG, errorMsg);
            LogManager.LogErrorMsg(className, "GcmIntentService->OnHandleEvent->[COMMAND:join_approval]", errorMsg);
			}
		String key = extras.getString(CommandTagEnum.key.toString());
		String mutualId = extras.getString(CommandTagEnum.value.toString()); // mutualId
		String currentDateTime = Controller.getCurrentDate();
		
//		// Insert into TABLE_PERMISSIONS account(email) according to mutualId
//		long countAdded = DBLayer.addPermissions(email, 0, 0, 0);
//		PermissionsData permissionData = DBLayer.getPermissions(email);
		
		SentJoinRequestData sentJoinRequestData = DBLayer.getSentJoinRequestByMutualId(mutualId);
		if( sentJoinRequestData == null ){
			errorMsg = "Failed to get sent join request data";
            Log.e(CommonConst .LOG_TAG, errorMsg);
            LogManager.LogErrorMsg(className, "GcmIntentService->OnHandleEvent->[COMMAND:join_approval]", errorMsg);
		}else {
			Log.i(CommonConst.LOG_TAG, "ReceivedJoinRequestData = " + sentJoinRequestData.toString());
		}
		
		// Remove join request from TABLE_SEND_JOIN_REQUEST according to "mutualId"
		int count = DBLayer.deleteSentJoinRequest(mutualId);
		if( count < 1) {
			Log.i(CommonConst.LOG_TAG, "Failed to delete sent join request with mutual id: " + mutualId + " count = " + count);
		} else {
			Log.i(CommonConst.LOG_TAG, "Deleted sent join request with mutual id: " + mutualId + " count = " + count);
		}
		
		sentJoinRequestData = DBLayer.getSentJoinRequestByMutualId(mutualId);
		if( sentJoinRequestData != null){
			Log.i(CommonConst.LOG_TAG, "Failed to delete sent join request with mutual id: " + mutualId + " count = " + count);
		}
		
		// Add contact details to CondtactDeviceData table
		DBLayer.addContactDeviceDataList(new ContactDeviceDataList(email,
				macAddress, phoneNumber, registrationIdJoinApproval, null));
	}
	
	private void commandTrackLocationKeepAlive(Bundle extras){
		String key = extras.getString(CommandTagEnum.key.toString());
		String value = extras.getString(CommandTagEnum.value.toString());
		
		Controller.broadcastMessage(GcmIntentService.this, 
			BroadcastActionEnum.BROADCAST_LOCATION_KEEP_ALIVE.toString(),
			"GcmIntentService",
			null, 
			BroadcastKeyEnum.keep_alive.toString(),  
			key + CommonConst.DELIMITER_STRING + value);
	}
	
	private void commandNotification(Bundle extras){
		LogManager.LogFunctionCall(className, "GcmIntentService->OnHandleEvent->[COMMAND:notification]");
		String msg = extras.getString("message");
		if(msg != null && !msg.isEmpty()){
    		LogManager.LogInfoMsg(className, "GcmIntentService->OnHandleEvent->[COMMAND:notification]", msg);
		}
		
		String key = extras.getString(CommandTagEnum.key.toString());
		String value = extras.getString(CommandTagEnum.value.toString());
		NotificationBroadcastData notificationBroadcastData = new NotificationBroadcastData();
		notificationBroadcastData.setMessage(extras.getString("message"));
		notificationBroadcastData.setKey(key);
		notificationBroadcastData.setValue(value);
		String jsonNotificationBroadcastData = gson.toJson(notificationBroadcastData);
		
		// Broadcast corresponding message
		Controller.broadcastMessage(GcmIntentService.this, 
			BroadcastActionEnum.BROADCAST_MESSAGE.toString(), 
			"GcmIntentService",
			jsonNotificationBroadcastData, 
			key, // BroadcastKeyEnum.message.toString(),  
			value);
		LogManager.LogFunctionExit(className, "GcmIntentService->OnHandleEvent->[COMMAND:notification]");
	}
}
