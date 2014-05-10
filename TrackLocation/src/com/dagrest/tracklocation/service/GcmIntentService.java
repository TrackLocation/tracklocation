package com.dagrest.tracklocation.service;

import java.util.ArrayList;
import java.util.Date;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.CommandTagEnum;
import com.dagrest.tracklocation.datatype.PushNotificationServiceStatusEnum;
import com.dagrest.tracklocation.http.HttpUtils;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
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
                
        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
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
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            	LogManager.LogInfoMsg(this.getClass().getName(), "onHandleIntent", 
                	"If it's a regular GCM message, do some work.");
            	LogManager.LogInfoMsg(this.getClass().getName(), "onHandleIntent", 
            		"Received: " + extras.toString());
            	if(extras.containsKey(CommandTagEnum.command.toString()) &&
            			extras.getString(CommandTagEnum.command.toString()).
            			equals(CommandEnum.start.toString())){ // COMMAND START
            		if(extras.containsKey(CommandTagEnum.interval.toString())){ // COMMAND INTERVAL
            			String intervalString = extras.getString(CommandTagEnum.interval.toString());
            			Context ctx = getApplicationContext();
                        Preferences.setPreferencesString(getApplicationContext(), 
                        	CommonConst.LOCATION_SERVICE_INTERVAL, intervalString);
            		}
            		Context context = getApplicationContext();
            		Intent trackLocationService = new Intent(context, TrackLocationService.class);
            		context.startService(trackLocationService); 
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
            			extras.getString(CommandTagEnum.command.toString()).
            			equals(CommandEnum.stop.toString())){ // COMMAND STOP 
            		Context context = getApplicationContext();
            		Intent trackLocationService = new Intent(context, TrackLocationService.class);
            		boolean result = context.stopService(trackLocationService); 
            		Log.i(LOCATION_SERVICE, "Servise stopped: " + result);
            	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
            			extras.getString(CommandTagEnum.command.toString()).
            			equals(CommandEnum.status_request.toString())){ // COMMAND STATUS_REQUEST
            		
            		
            		ArrayList<String> listRegIDs = new ArrayList<String>();
            		// get regID of the current client as a requester
            		String regIDToReturnMessageTo = extras.getString("regIDToReturnMessageTo");
            		listRegIDs.add(regIDToReturnMessageTo); // regIDs of client that are requested for location
            		if(regIDToReturnMessageTo != null){
	            		String time = new Date().toString(); 
	            		Controller controller = new Controller();
	            		String jsonMessage = controller.createJsonMessage(listRegIDs, 
	        	    		regIDToReturnMessageTo, 
	        	    		CommandEnum.status_response, 
	        	    		null, 
	        	    		time,
	        	    		null,
	        	    		PushNotificationServiceStatusEnum.available);
	            		// send message back with PushNotificationServiceStatusEnum.available
	            		//HttpUtils.sendRegistrationIdToBackend(jsonMessage);
	            		controller.sendCommand(jsonMessage);
                	} else if (extras.containsKey(CommandTagEnum.command.toString()) &&
                			extras.getString(CommandTagEnum.command.toString()).
                			equals(CommandEnum.status_response.toString())){ // COMMAND STATUS_RESPONSE
                		String pushNotificationServiceStatus = extras.getString("pushNotificationServiceStatusEnum");
                		int i = 0;
//                		ArrayList<String> listRegIDs = new ArrayList<String>();
//                		listRegIDs.add(""); // regIDs of client that are requested for location
//                		// get regID of the current client as a requester
//                		String regIDToReturnMessageTo = extras.getString("regIDToReturnMessageTo");
//                		if(regIDToReturnMessageTo != null){
//    	            		String time = new Date().toString(); 
//    	            		Controller controller = new Controller();
//    	            		String jsonMessage = controller.createJsonMessage(listRegIDs, 
//    	        	    		regIDToReturnMessageTo, 
//    	        	    		CommandEnum.status_response, 
//    	        	    		null, 
//    	        	    		time,
//    	        	    		null,
//    	        	    		PushNotificationServiceStatusEnum.available);
//    	            		// send message back with PushNotificationServiceStatusEnum.available
//    	            		//HttpUtils.sendRegistrationIdToBackend(jsonMessage);
//    	            		controller.sendCommand(jsonMessage);
                	}
            	}
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
	}
	
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
