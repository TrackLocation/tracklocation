package com.dagrest.tracklocation.datatype;

import java.util.ArrayList;
import java.util.List;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.SendMessageAsync;
import com.dagrest.tracklocation.http.HttpUtils;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.google.gson.Gson;

import android.content.Context;
import android.util.Log;

public class CommandDataBasic {
	protected Context context; 
	protected CommandEnum command;
	protected String message;
	protected MessageDataContactDetails contactDetails; // regIDToReturnMessageTo
	protected MessageDataLocation location;
	protected String key;
	protected String value;
	protected AppInfo appInfo;
	protected List<String> listRegIDs;
	protected List<String> listAccounts;
	protected String className;
	protected String notificationMessage;
	
	protected CommandDataBasic() {
		className = this.getClass().getName();
	}
	
	public CommandDataBasic(Context context,
			CommandEnum command,
			String message, MessageDataContactDetails contactDetails,
			MessageDataLocation location, String key, String value, AppInfo appInfo) {
		super();
		className = this.getClass().getName();
		this.context = context;
		this.command = command;
		this.message = message;
		this.contactDetails = contactDetails;
		this.location = location;
		this.key = key;
		this.value = value;
		this.appInfo = appInfo;
		
		initialValuesCheck();
	}
	
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public CommandEnum getCommand() {
		return command;
	}
	public void setCommand(CommandEnum command) {
		this.command = command;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public MessageDataContactDetails getContactDetails() {
		return contactDetails;
	}
	public void setContactDetails(MessageDataContactDetails contactDetails) {
		this.contactDetails = contactDetails;
	}
	public MessageDataLocation getLocation() {
		return location;
	}
	public void setLocation(MessageDataLocation location) {
		this.location = location;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public AppInfo getAppInfo() {
		return appInfo;
	}
	public void setAppInfo(AppInfo appInfo) {
		this.appInfo = appInfo;
	}	
	public List<String> getListAccounts() {
		return listAccounts;
	}

	protected void initialValuesCheck(){
		if(command == null){
			notificationMessage = "Command is undefined";
			LogManager.LogErrorMsg(className, "[sendCommand:UNDEFINED_COMMAND]", notificationMessage);
			return;
		}
		
		LogManager.LogFunctionCall(className, "[sendCommand:" + command.toString() + "]");

		if(context == null){
			notificationMessage = "Context is undefined";
			LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", notificationMessage);
			return;
		}
		
//		contactDeviceDataList = commandData.getContactDeviceDataList();
//		if(contactDeviceDataList == null){
//			notificationMessage = "There is no recipient list defined";
//			LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", notificationMessage);
//			return;
//		}
		
		if(contactDetails == null){
			notificationMessage = "There is no sender defined";
			LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", notificationMessage);
			return;
		}
	}
	
	protected void prepareAccountAndRegIdLists(List<String> listAccounts, List<String> listRegIDs){
//		// Collect registration_IDs of the contacts that command will be send to
//		for (ContactDeviceData contactDeviceData : contactDeviceDataList.getContactDeviceDataList()) {
//			ContactData contactData = contactDeviceData.getContactData();
//			if(contactData != null){
//				String regId = contactDeviceData.getRegistration_id();
//				if(regId != null && !regId.isEmpty()){
//					listRegIDs.add(contactDeviceData.getRegistration_id());
//				} else {
//					notificationMessage = "Empty registrationID for the following contact: " + contactData.getEmail();
//					LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", notificationMessage);
//					Log.e("[sendCommand:" + command.toString() + "]", notificationMessage);
//				}
//				listAccounts.add(contactData.getEmail());
//			} else {
//				LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", "Unable to get registration_ID: ContactData is null.");
//				Log.e("[sendCommand:" + command.toString() + "]", "Unable to get registration_ID: contactData is null.");
//			}
//			
//		}
	}
	
//	@SuppressWarnings("unchecked")
//	public void sendCommandInBackground(HashMap<String, Object> params) {
//        new AsyncTask<HashMap<String, Object>, Void, String>() {
//            @Override
//            protected String doInBackground(HashMap<String, Object>... params) {
//                String msg = "";
////                Context context = null;
////                String registrationId = null;
////                GoogleCloudMessaging gcm = null;
////                String googleProjectNumber = null;
////                if(params != null){
////                	HashMap<String, Object> mapParams = params[0];
////                	if(mapParams.get("Context") instanceof Context){
////                		context = (Context) mapParams.get("Context");
////                	} else { 
////                		// TODO: Error message
////                	}
////                	if(mapParams.get("GoogleCloudMessaging") instanceof GoogleCloudMessaging){
////                		gcm = (GoogleCloudMessaging) mapParams.get("GoogleCloudMessaging");
////                	} else {
////                		// TODO: Error message
////                	}
////                	if(mapParams.get("GoogleProjectNumber") instanceof String){
////                		googleProjectNumber = (String) mapParams.get("GoogleProjectNumber");
////                	} else {
////                		// TODO: Error message
////                	}
////                } else {
////                	// TODO: Error message: incorrect parameters
////                	return "Error" + msg;
////                }
//
//                
//        		LogManager.LogFunctionCall(className, "[sendCommand]");
//
//        		initialValuesCheck();
//        		
//        		listAccounts = new ArrayList<String>();
//        		listRegIDs = new ArrayList<String>();
//        		prepareAccountAndRegIdLists(listAccounts, listRegIDs);
//        		
//        		Gson gson = new Gson();
//        		notificationMessage = 	"Sending command [" + command.toString() + "] to the following recipients: " +
//        						gson.toJson(listAccounts);
//        		LogManager.LogInfoMsg(className, "[sendCommand:" + command.toString() + "]", notificationMessage);
//        		
//        		JsonMessageData jsonMessageData = new JsonMessageData(
//        				listRegIDs, 				// registration_IDs of the contacts that command will be send to
//        	    		//regIDToReturnMessageTo, 	// sender's registartion_ID (contact that response will be returned to)
//        	    		command, 
//        	    		message, 					// messageString
//        	    		contactDetails,				// sender's contact details
//        	    		location,					// sender's location details
//        	    		appInfo,					// application info
//        	    		Controller.getCurrentDate(),// current time
//        	    		key, 						// key (free pair of key/value)
//        	    		value						// value (free pair of key/value)
//        				);
//        		
//        		if(listRegIDs.size() > 0){
//        			String jsonMessage = Controller.createJsonMessage(jsonMessageData);
//        			
//        			if(jsonMessage == null){
//        				notificationMessage = "Failed to create JSON Message to send to recipient";
//        				LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", notificationMessage);
//// -------        				return;
//        			}
//        			
//        			LogManager.LogInfoMsg(className, "[sendCommand:" + command.toString() + "]", 
//        				"Sending command [" + command.toString() + "] as asynchonous task... ");
//        			Controller.sendCommand(jsonMessage, context);
//        		} else {
//        			LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", 
//        				"Unable to send command: [" + command.toString() + "] - there is no any recipient.");
//        		}
//        		
//        		LogManager.LogFunctionExit(className, "[sendCommand:" + command.toString() + "]");
//        		
//        		return msg;
//            }
//
//            @Override
//            protected void onPostExecute(String msg) {
//            	// TODO: do some work here...
//            }
//        }.execute(params, null, null);
//    }

	
	public void sendCommand(){
		
		LogManager.LogFunctionCall(className, "[sendCommand]");
		Log.i(CommonConst.LOG_TAG, "[sendCommand]");

		
//		String regIDToReturnMessageTo = Controller.getRegistrationId(context);
//		if(regIDToReturnMessageTo == null || regIDToReturnMessageTo.isEmpty()){
//			errorMsg = "Check if app was updated; if so, it must clear the registration ID" + 
//				"since the existing regID is not guaranteed to work with the new" + 
//				"app version.";
//			LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", errorMsg);
//			return;
//		}

//		// Collect registration_IDs of the contacts that command will be send to
//		for (ContactDeviceData contactDeviceData : contactDeviceDataList.getContactDeviceDataList()) {
//			ContactData contactData = contactDeviceData.getContactData();
//			if(contactData != null){
//				String regId = contactDeviceData.getRegistration_id();
//				if(regId != null && !regId.isEmpty()){
//					listRegIDs.add(contactDeviceData.getRegistration_id());
//				} else {
//					errorMsg = "Empty registrationID for the following contact: " + contactData.getEmail();
//					LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", errorMsg);
//					Log.e("[sendCommand:" + command.toString() + "]", errorMsg);
//				}
//				listAccounts.add(contactData.getEmail());
//			} else {
//				LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", "Unable to get registration_ID: ContactData is null.");
//				Log.e("[sendCommand:" + command.toString() + "]", "Unable to get registration_ID: contactData is null.");
//			}
//			
//		}
		
		Gson gson = new Gson();
		notificationMessage = 	"Sending command [" + command.toString() + "] to the following recipients: " +
						gson.toJson(listAccounts);
		LogManager.LogInfoMsg(className, "[sendCommand:" + command.toString() + "]", notificationMessage);
		Log.i(CommonConst.LOG_TAG, "[sendCommand:" + command.toString() + "]");
		
		JsonMessageData jsonMessageData = new JsonMessageData(
				listRegIDs, 				// registration_IDs of the contacts that command will be send to
	    		//regIDToReturnMessageTo, 	// sender's registartion_ID (contact that response will be returned to)
	    		command, 
	    		message, 					// messageString
	    		contactDetails,				// sender's contact details
	    		location,					// sender's location details
	    		appInfo,					// application info
	    		Controller.getCurrentDate(),// current time
	    		key, 						// key (free pair of key/value)
	    		value						// value (free pair of key/value)
				);
		
		if(listRegIDs.size() > 0){
			String jsonMessage = Controller.createJsonMessage(jsonMessageData);
//					listRegIDs, 				// registration_IDs of the contacts that command will be send to
//		    		regIDToReturnMessageTo, 	// sender's registartion_ID (contact that response will be returned to)
//		    		command, 
//		    		message, 					// messageString
//		    		contactDetails,				// sender's contact details
//		    		location,					// sender's location details
//		    		appInfo,					// application info
//		    		Controller.getCurrentDate(),// current time
//		    		key, 						// key (free pair of key/value)
//		    		value 						// value (free pair of key/value)
//					);
			
			if(jsonMessage == null){
				notificationMessage = "Failed to create JSON Message to send to recipient";
				LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", notificationMessage);
				return;
			}
			
			LogManager.LogInfoMsg(className, "[sendCommand:" + command.toString() + "]", 
				"Sending command [" + command.toString() + "] as asynchonous task... ");
//			LogManager.LogInfoMsg(className, "[sendCommand:" + command.toString() + "]", 
//				"JSON message: " + jsonMessage);
//			java.util.Map<String,Object> params = new HashMap<String,Object>();
//			params.put("JsonMessage", jsonMessage);
//			params.put("Context", context);
//			if(isRetry == false){
//				// Controller.sendCommand(jsonMessage, null);
//				Controller.sendCommand(params);
//			} else {
//				// Controller.sendCommand(jsonMessage, context);
//				Controller.sendCommand(params);
//			}
			
			Runnable sendMessageAsync = new SendMessageAsync(jsonMessage);
			new Thread(sendMessageAsync).start();
			Log.i(CommonConst.LOG_TAG, "[" + command.toString() + "] is sending asynchronously ...");
			
		} else {
			LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", 
				"Unable to send command: [" + command.toString() + "] - there is no any recipient.");
		}
		
		LogManager.LogFunctionExit(className, "[sendCommand:" + command.toString() + "]");
	}

}
