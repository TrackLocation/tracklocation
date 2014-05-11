package com.dagrest.tracklocation.service;

import java.util.Date;
import java.util.List;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.CommandTagEnum;
import com.dagrest.tracklocation.datatype.NotificationCommandEnum;
import com.dagrest.tracklocation.datatype.PushNotificationServiceStatusEnum;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.dagrest.tracklocation.utils.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class GcmIntentService extends IntentService {

	public GcmIntentService() {
		super("GcmIntentService");
//		context = getApplicationContext();
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
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
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
                	"If it's a regular GCM message, do some work.");
            	LogManager.LogInfoMsg(this.getClass().getName(), "onHandleIntent", 
            		"Received: " + extras.toString());
            	
            	// ============================================
                // COMMAND: 	start
            	// ============================================
            	if(extras.containsKey(CommandTagEnum.command.toString()) &&
            			extras.getString(CommandTagEnum.command.toString()).
            			equals(CommandEnum.start.toString())){ // COMMAND START
                	// ============================================
                    // COMMAND: 	start
                	// PARAMETER: 	interval
                	// ============================================
            		if(extras.containsKey(CommandTagEnum.interval.toString())){ // COMMAND INTERVAL
            			String intervalString = extras.getString(CommandTagEnum.interval.toString());
            			//Context ctx = getApplicationContext();
                        Preferences.setPreferencesString(getApplicationContext(), 
                        	CommonConst.LOCATION_SERVICE_INTERVAL, intervalString);
            		}
//            		Context context = getApplicationContext();
//            		Intent trackLocationService = new Intent(context, TrackLocationService.class);
//            		context.startService(trackLocationService);
            	
        		// ============================================
                // COMMAND: 	stop
            	// PARAMETER: 	interval
            	// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
            			extras.getString(CommandTagEnum.command.toString()).
            			equals(CommandEnum.stop.toString())){ // COMMAND STOP 
            		Context context = getApplicationContext();
            		Intent trackLocationService = new Intent(context, TrackLocationService.class);
            		boolean result = context.stopService(trackLocationService); 
            		Log.i(LOCATION_SERVICE, "Servise stopped: " + result);
            		
        		// ============================================
                // COMMAND: 	status_request
            	// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
            			extras.getString(CommandTagEnum.command.toString()).
            			equals(CommandEnum.status_request.toString())){ // COMMAND STATUS_REQUEST
            		
            		
            		List<String> listRegIDs = null; //new ArrayList<String>();
            		// get regID of the current client as a requester
            		String regIDToReturnMessageTo = extras.getString("regIDToReturnMessageTo");
            		if(regIDToReturnMessageTo != null){
             			
            			// update (insert/add) regIds to list of contacts that will be notified
            			listRegIDs = Preferences.setPreferencesReturnToRegIDList(getApplicationContext(), 
            				CommonConst.PREFERENCES_RETURN_TO_REG_ID_LIST, regIDToReturnMessageTo); 
             			//  listRegIDs = Utils.splitLine(
            			//	   Preferences.getPreferencesString(getApplicationContext(), CommonConst.PREFERENCES_RETURN_TO_REG_ID_LIST), 
            			//	   CommonConst.DELIMITER_STRING);
           			
	            		String time = new Date().toString(); 
	            		Controller controller = new Controller();

	            		String jsonMessage = controller.createJsonMessage(listRegIDs, 
	        	    		regIDToReturnMessageTo, 
	        	    		CommandEnum.status_response, 
	        	    		null, 
	        	    		time,
	        	    		NotificationCommandEnum.pushNotificationServiceStatus.toString(),
	        	    		PushNotificationServiceStatusEnum.available.toString());
	            		// send message back with PushNotificationServiceStatusEnum.available
	            		controller.sendCommand(jsonMessage);
	            		
                		// Start location service to get current location
                		Context context = getApplicationContext();
                		Intent trackLocationService = new Intent(context, TrackLocationService.class);
                		context.startService(trackLocationService); 

                	} 
 
        		// ============================================
                // COMMAND: 	status_response
            	// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.status_response.toString())){ // COMMAND STATUS_RESPONSE
                		String key = extras.getString("key");
                		String value = extras.getString("value");
                		String currentDateTime = Controller.getCurrentDate();
                		
						broadcastLocationUpdatedGps(key + CommonConst.DELIMITER_STRING +
							value + CommonConst.DELIMITER_STRING + currentDateTime);
            	
        		// ============================================
                // COMMAND: 	location
            	// ============================================
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.location.toString())){ // COMMAND LOCATION
                		String key = extras.getString("key");
                		String value = extras.getString("value");
                		String currentDateTime = Controller.getCurrentDate();
                		
						broadcastLocationUpdatedGps(key + CommonConst.DELIMITER_STRING +
							value + CommonConst.DELIMITER_STRING + currentDateTime);
            	}
            	
            } // if (GoogleCloudMessaging
            
        } // if (extras.isEmpty())...
        
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
	} // onHandleIntent(...
	
	public void broadcastLocationUpdatedGps(String value)
	{
		LogManager.LogFunctionCall("GcmIntentService", "broadcastLocationUpdatedGps");
		Intent intent = new Intent();
		intent.setAction("com.dagrest.tracklocation.service.GcmIntentService.GCM_UPDATED");
		intent.putExtra("updated", value);
		sendBroadcast(intent);
		LogManager.LogFunctionExit("GcmIntentService", "broadcastLocationUpdatedGps");
	}

}
