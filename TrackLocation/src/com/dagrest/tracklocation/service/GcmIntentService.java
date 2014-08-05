package com.dagrest.tracklocation.service;

import java.util.Date;
import java.util.List;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.datatype.BroadcastCommandEnum;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.CommandTagEnum;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.MessageDataContactDetails;
import com.dagrest.tracklocation.datatype.MessageDataLocation;
import com.dagrest.tracklocation.datatype.NotificationKeyEnum;
import com.dagrest.tracklocation.datatype.PermissionsData;
import com.dagrest.tracklocation.datatype.PushNotificationServiceStatusEnum;
import com.dagrest.tracklocation.datatype.SentJoinRequestData;
import com.dagrest.tracklocation.db.DBLayer;
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

	public static final String CLASS_NAME = "GcmIntentService";
	
	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        LogManager.LogInfoMsg(this.getClass().getName(), "onHandleIntent", 
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
        		LogManager.LogErrorMsg(this.getClass().getName(), "onHandleIntent", 
        			"Send error: " + extras.toString());
        		
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                //sendNotification("Deleted messages on server: " +
                //        extras.toString());
        		LogManager.LogErrorMsg(this.getClass().getName(), "onHandleIntent", 
        			"Deleted messages on server: " + extras.toString());
        		
        	// ============================================
            // If it's a regular GCM message, do some work.
        	// ============================================
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            	LogManager.LogInfoMsg(this.getClass().getName(), "onHandleIntent", 
                	"It is a regular GCM message");
            	LogManager.LogInfoMsg(this.getClass().getName(), "onHandleIntent", 
            		"Received: " + extras.toString());
            	
            	// ============================================
            	// COMMANDS TO RUN ON CLIENT SIDE
            	// ============================================
            	
            	// Collect client details
        		Context clientContext = getApplicationContext();
        		String clientAccount = Preferences.getPreferencesString(clientContext, CommonConst.PREFERENCES_PHONE_ACCOUNT);
        		String clientMacAddress = Preferences.getPreferencesString(clientContext, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
        		String clientPhoneNumber = Preferences.getPreferencesString(clientContext, CommonConst.PREFERENCES_PHONE_NUMBER);
        		int clientBatteryLevel = Controller.getBatteryLevel(clientContext);

            	// ============================================
                // COMMAND: 	start - Start Track Location Service on client
            	// CLIENT SIDE	
            	// command received via GCM from Master
            	// to Slave - in order to start location
            	// service on Slave
            	// ============================================
            	if(extras.containsKey(CommandTagEnum.command.toString()) &&
            			extras.getString(CommandTagEnum.command.toString()).
            			equals(CommandEnum.start.toString())){ // COMMAND START
            		
            		String regIdToReturnMessageTo = extras.getString(CommonConst.REGISTRATION_ID_TO_RETURN_MESSAGE_TO);
            		String value = extras.getString("value"); // account = email
            		if( value == null || value.isEmpty() ) {
                    	String errMsg = "Account (email) was not delivered to start TrackLocationService";
                        Log.e(CommonConst.LOG_TAG, errMsg);
                        LogManager.LogErrorMsg(CLASS_NAME, "GcmIntentService->onHandleIntent->[COMMAND:start]", errMsg);
                        
                        MessageDataContactDetails contactDetails = new MessageDataContactDetails(clientAccount, 
                        	clientMacAddress, clientPhoneNumber, null, clientBatteryLevel);
                        ContactDeviceDataList contactDeviceDataToSendNotificationTo = 
                        	new ContactDeviceDataList (clientAccount, clientMacAddress, clientPhoneNumber, regIdToReturnMessageTo, null);
                        // Notify caller by GCM (push notification)
        	    		Controller.sendCommand(getApplicationContext(), contactDeviceDataToSendNotificationTo, 
        		    			CommandEnum.notification, errMsg, contactDetails, null, null, null);

            			return;
            		}
            		
                	PermissionsData permissionsData = DBLayer.getPermissions(value);
                	if( permissionsData == null){
                		// TODO: Show error - no permission for "account" to invoke TrackLocation
                    	String errMsg = "No permissions defind for account: " + value + 
                    		". Not permitted to share location";
                        Log.e(CommonConst.LOG_TAG, errMsg);
                        LogManager.LogErrorMsg(CLASS_NAME, "GcmIntentService->onHandleIntent->[COMMAND:start]", errMsg);
                        
                        // Notify to caller by GCM (push notification)
                        MessageDataContactDetails contactDetails = new MessageDataContactDetails(clientAccount, 
                            	clientMacAddress, clientPhoneNumber, null, clientBatteryLevel);
                        ContactDeviceDataList contactDeviceDataToSendNotificationTo = 
                        	new ContactDeviceDataList (clientAccount, clientMacAddress, clientPhoneNumber, regIdToReturnMessageTo, null);
                        // Notify caller by GCM (push notification)
        	    		Controller.sendCommand(getApplicationContext(), contactDeviceDataToSendNotificationTo, 
        		    			CommandEnum.notification, errMsg, contactDetails, null, null, null);
        	    		
        	    		return;
                	}
                	
                	int isLocationSharingPermitted = permissionsData.getIsLocationSharePermitted();
                	if( isLocationSharingPermitted != 1 ){
                		// TODO: Show error - no permission to invoke TrackLocation
                    	String errMsg = "Not permitted to share location to " + value;
                        Log.e(CommonConst.LOG_TAG, errMsg);
                        LogManager.LogErrorMsg(CLASS_NAME, "GcmIntentService->onHandleIntent->[COMMAND:start]", errMsg);
                        
                        // Notify to caller by GCM (push notification)
                        MessageDataContactDetails contactDetails = new MessageDataContactDetails(clientAccount, 
                            	clientMacAddress, clientPhoneNumber, null, clientBatteryLevel);
                        ContactDeviceDataList contactDeviceDataToSendNotificationTo = 
                        	new ContactDeviceDataList (clientAccount, clientMacAddress, clientPhoneNumber, regIdToReturnMessageTo, null);
                        // Notify caller by GCM (push notification)
        	    		Controller.sendCommand(getApplicationContext(), contactDeviceDataToSendNotificationTo, 
        		    			CommandEnum.notification, errMsg, contactDetails, null, null, null);

        	    		return;
                	}
            		
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
            		
            		// TODO
            		// TODO
            		// TODO create table to contain regIds to sent notification messages to...
            		// TODO
            		// TODO
            		
//            		// get regID of the current client as a requester
//            		String regIDToReturnMessageTo = extras.getString("regIDToReturnMessageTo");
//            		if(regIDToReturnMessageTo != null){
//             			
//            			// update (insert/add) regIds to list of contacts that will be notified
//            			Preferences.setPreferencesReturnToRegIDList(clientContext, 
//            				CommonConst.PREFERENCES_RETURN_TO_REG_ID_LIST, regIDToReturnMessageTo); 
//            		}
            		
            		// Start location service to get current location
            		Intent trackLocationService = new Intent(clientContext, TrackLocationService.class);
            		trackLocationService.putExtra(CommonConst.REGISTRATION_ID_TO_RETURN_MESSAGE_TO, 
            				regIdToReturnMessageTo);
            		ComponentName componentName = clientContext.startService(trackLocationService); 
            		if(componentName != null){
            			// TODO: Notify that TrackLoactionService was started
                        // Notify to caller by GCM (push notification)
            			String msgServiceStarted = "TrackLoactionService was started for " + clientAccount;
                        MessageDataContactDetails contactDetails = new MessageDataContactDetails(clientAccount, 
                            	clientMacAddress, clientPhoneNumber, null, clientBatteryLevel);
                        ContactDeviceDataList contactDeviceDataToSendNotificationTo = 
                        	new ContactDeviceDataList (clientAccount, clientMacAddress, clientPhoneNumber, regIdToReturnMessageTo, null);
                        // Notify caller by GCM (push notification)
        	    		Controller.sendCommand(getApplicationContext(), contactDeviceDataToSendNotificationTo, 
        		    			CommandEnum.notification, msgServiceStarted, contactDetails, null, null, null);
            		} else {
            			// TODO: Notify that TrackLoactionService was not started
            		}
           	
//        		// ============================================
//            	// COMMAND: 	stop
//            	// CLIENT SIDE	
//            	// command received via GCM from Master
//            	// to Slave - in order to stop location
//            	// service on Slave
//            	// ============================================
//            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
//            			extras.getString(CommandTagEnum.command.toString()).
//            			equals(CommandEnum.stop.toString())){ // COMMAND STOP 
//            		Context context = getApplicationContext();
//            		Intent trackLocationService = new Intent(context, TrackLocationService.class);
//            		boolean result = context.stopService(trackLocationService); 
//            		Log.i(LOCATION_SERVICE, "Servise stopped: " + result);
            		
        		// ============================================
                // COMMAND: 	status_request
            	// CLIENT SIDE	
            	// command received via GCM from Master
            	// to Slave - in order to check that GCM Service
            	// is registered on Slave.
            	// Send via GCM command status_response to Master
            	// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
            			extras.getString(CommandTagEnum.command.toString()).
            			equals(CommandEnum.status_request.toString())){ // COMMAND STATUS_REQUEST
            		
            		
            		List<String> listRegIDs = null; //new ArrayList<String>();
            		// get regID of the current client as a requester
            		String regIDToReturnMessageTo = extras.getString("regIDToReturnMessageTo");
            		if(regIDToReturnMessageTo != null){
             			
            			// update (insert/add) regIds to list of contacts that will be notified
            			listRegIDs = Preferences.setPreferencesReturnToRegIDList(clientContext, 
            				CommonConst.PREFERENCES_RETURN_TO_REG_ID_LIST, regIDToReturnMessageTo); 
             			//  listRegIDs = Utils.splitLine(
            			//	   Preferences.getPreferencesString(getApplicationContext(), CommonConst.PREFERENCES_RETURN_TO_REG_ID_LIST), 
            			//	   CommonConst.DELIMITER_STRING);
           			
	            		String time = new Date().toString(); 
	            			            		
	            		MessageDataContactDetails contactDetails = new MessageDataContactDetails(
	            			clientAccount, clientMacAddress, clientPhoneNumber, null, clientBatteryLevel);
	            		MessageDataLocation location = null;

	            		String jsonMessage = Controller.createJsonMessage(listRegIDs, 
	        	    		regIDToReturnMessageTo, 
	        	    		CommandEnum.status_response, 
	        	    		null, 
	        	    		contactDetails,
	        	    		location,
	        	    		time,
	        	    		NotificationKeyEnum.pushNotificationServiceStatus.toString(),
	        	    		PushNotificationServiceStatusEnum.available.toString());
	            		// send message back with PushNotificationServiceStatusEnum.available
	            		Controller.sendCommand(jsonMessage);
	            		
                	} 
 
        		// ============================================
                // COMMAND: 	status_response
            	// notification from Slave to Master via GCM
            	// and broadcast to related UI consumers on Master	
            	// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.status_response.toString())){ // COMMAND STATUS_RESPONSE
            		
                		String key = extras.getString("key");
                		String value = extras.getString("value");
                		String currentDateTime = Controller.getCurrentDate();
                		
//						broadcastLocationUpdatedGps(key + CommonConst.DELIMITER_STRING +
//							value + CommonConst.DELIMITER_STRING + currentDateTime);
                		if(value != null && !value.isEmpty()) {
							Controller.broadcastMessage(GcmIntentService.this, CommonConst.BROADCAST_LOCATION_UPDATED, "GcmIntentService",
								null, null,  
								BroadcastCommandEnum.gcm_status.toString(),  
								key + CommonConst.DELIMITER_STRING + value + CommonConst.DELIMITER_STRING + currentDateTime);
                		} else {
                			Controller.broadcastMessage(GcmIntentService.this, CommonConst.BROADCAST_LOCATION_UPDATED, "GcmIntentService",
                				null, null,	
								BroadcastCommandEnum.gcm_status.toString(),  
								"");
                		}
        		// ============================================
                // COMMAND: 	location
                // notification from Slave to Master and
                // broadcast to related UI consumers on Master 		
            	// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.location.toString())){ // COMMAND LOCATION
            		
                		String key = extras.getString("key");
                		String value = extras.getString("value");
                		String currentDateTime = Controller.getCurrentDate();
                		
                		String jsonContactDetails = extras.getString("contactDetails");
                		String jsonLocation = extras.getString("location");
                		
                		Controller.broadcastMessage(GcmIntentService.this, CommonConst.BROADCAST_LOCATION_UPDATED, "GcmIntentService",
                			jsonContactDetails, jsonLocation,	
							BroadcastCommandEnum.location_updated.toString(), 
							key + CommonConst.DELIMITER_STRING + value + CommonConst.DELIMITER_STRING + currentDateTime);
        		// ============================================
                // COMMAND: 	join_approval
            	// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.join_approval.toString())){ // COMMAND JOIN_APPROVAL
            		
            			String message = extras.getString("message"); // email, regId, phoneNumber
            			
            			String messageArray[] = message.split(CommonConst.DELIMITER_COMMA);
            			String email = null;
            			String registrationId = null;
            			String phoneNumber = null;
            			String macAddress = null;
            			if(messageArray.length == 4){
	            			email = messageArray[0];
	            			registrationId = messageArray[1];
	            			phoneNumber = messageArray[2];
	            			macAddress = messageArray[3];
            			} else {
            				// Join approval request failed
            	        	String errMsg = "Join approval request failed - not all contact details were provided.";
            	            Log.e(CommonConst.LOG_TAG, errMsg);
            	            LogManager.LogErrorMsg(CLASS_NAME, "GcmIntentService->OnHandleEvent->[COMMAND:join_approval]", errMsg);
             			}
                		String key = extras.getString("key");
                		String mutualId = extras.getString("value"); // mutualId
                		String currentDateTime = Controller.getCurrentDate();
                		
//                		// Insert into TABLE_PERMISSIONS account(email) according to mutualId
//                		long countAdded = DBLayer.addPermissions(email, 0, 0, 0);
//                		PermissionsData permissionData = DBLayer.getPermissions(email);
                		
                		SentJoinRequestData sentJoinRequestData = DBLayer.getSentJoinRequestByMutualId(mutualId);
        				if( sentJoinRequestData == null ){
        		        	String errMsg = "Failed to get sent join request data";
        		            Log.e(CommonConst .LOG_TAG, errMsg);
        		            LogManager.LogErrorMsg(CLASS_NAME, "GcmIntentService->OnHandleEvent->[COMMAND:join_approval]", errMsg);
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
                				macAddress, phoneNumber, registrationId, null));
                		
        		// ============================================
                // COMMAND: 	track_location_service_keep_alive
            	// command received via GCM from Master
            	// to Slave - in order to keep alive location
            	// service on Slave 
                // (update start time on Timer that stops Track Location Service )
            	// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.track_location_service_keep_alive.toString())){ // COMMAND TRACK_LOCATION_SERVICE_KEEP_ALIVE
            		
            		String key = extras.getString("key");
            		String value = extras.getString("value");
            		
					Controller.broadcastMessage(GcmIntentService.this, CommonConst.BROADCAST_LOCATION_KEEP_ALIVE, "GcmIntentService",
						null, null,	
						BroadcastCommandEnum.keep_alive.toString(),  
						key + CommonConst.DELIMITER_STRING + value);
					
        		// ============================================
                // COMMAND: 	notification
            	// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.notification.toString())){ // COMMAND NOTIFICATION
            		
            		String msg = extras.getString("message");
            		if(msg != null || !msg.isEmpty()){
                		LogManager.LogInfoMsg(CLASS_NAME, "GcmIntentService->OnHandleEvent->[COMMAND:notification]", msg);
            		}
            		
            		// Broadcast corresponding message
					Controller.broadcastMessage(GcmIntentService.this, CommonConst.BROADCAST_MESSAGE, "GcmIntentService",
						null, null,
						BroadcastCommandEnum.message.toString(),  
						msg);
            	} 
        	
            } // if (GoogleCloudMessaging
            
        } // if (extras.isEmpty())...
        
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
	} // onHandleIntent(...
	
}
