package com.doat.tracklocation.datatype;

import java.util.List;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.concurrent.SendMessageAsync;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;
import com.google.gson.Gson;

import android.content.Context;
import android.util.Log;

public class CommandDataBasic {
	protected Context context; 
	protected CommandEnum command;
	protected String message;
	protected MessageDataContactDetails senderMessageDataContactDetails; // regIDToReturnMessageTo
	protected MessageDataLocation location;
	protected String key;
	protected String value;
	protected AppInfo appInfo;
	protected List<String> listRegIDs;
	protected List<String> listAccounts;
	protected String className;
	protected String notificationMessage;
	protected String methodName;
	protected String logMessage;
	
	protected CommandDataBasic() {
		className = this.getClass().getName();
	}
	
	public CommandDataBasic(Context context,
			CommandEnum command,
			String message, MessageDataContactDetails senderMessageDataContactDetails,
			MessageDataLocation location, String key, String value, AppInfo appInfo) {
		super();
		className = this.getClass().getName();
		this.context = context;
		this.command = command;
		this.message = message;
		this.senderMessageDataContactDetails = senderMessageDataContactDetails;
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
	public MessageDataContactDetails getSenderMessageDataContactDetails() {
		return senderMessageDataContactDetails;
	}
	public void setSenderMessageDataContactDetails(
			MessageDataContactDetails senderMessageDataContactDetails) {
		this.senderMessageDataContactDetails = senderMessageDataContactDetails;
	}

	private void initialValuesCheck(){
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
		
		if(senderMessageDataContactDetails == null){
			notificationMessage = "There is no sender defined";
			LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", notificationMessage);
			return;
		}
	}
	
	protected void prepareAccountAndRegIdLists(List<String> listAccounts, List<String> listRegIDs){
		methodName = "prepareAccountAndRegIdLists";
		logMessage = "Accounts list or RegistartionIDs list is empty.";
		if(listAccounts == null || listRegIDs == null){
			LogManager.LogException(new Exception(logMessage), className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
		}
		listAccounts.clear();
		listRegIDs.clear();
	}
	
	
	public void sendCommand(){
		
		methodName = "sendCommand";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);

		Gson gson = new Gson();
		notificationMessage = 	"Sending command [" + command.toString() + "] to the following recipients: " +
			gson.toJson(listAccounts) + " from [" + senderMessageDataContactDetails.getAccount() + "]";
		LogManager.LogInfoMsg(className, methodName, notificationMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + notificationMessage);
		
		// update Registartion ID for senderMessageDataContactDetails
		if(senderMessageDataContactDetails.getRegId() == null ||
			senderMessageDataContactDetails.getRegId().isEmpty()){
			notificationMessage = "Set Registartion ID for sender: [" + senderMessageDataContactDetails.getAccount() + "]";
			LogManager.LogInfoMsg(className, methodName, notificationMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + notificationMessage);
			senderMessageDataContactDetails.setRegId(Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID));
		}

		JsonMessageData jsonMessageData = new JsonMessageData(
				listRegIDs, 				// registration_IDs of the contacts that command will be send to
	    		//regIDToReturnMessageTo, 	// sender's registartion_ID (contact that response will be returned to)
	    		command, 
	    		message, 					// messageString
	    		senderMessageDataContactDetails,				// sender's contact details
	    		location,					// sender's location details
	    		appInfo,					// application info
	    		Controller.getCurrentDate(),// current time
	    		key, 						// key (free pair of key/value)
	    		value						// value (free pair of key/value)
				);
		
		if(listRegIDs.size() > 0){
			String jsonMessage = Controller.createJsonMessage(jsonMessageData);
			
			if(jsonMessage == null){
				notificationMessage = "Failed to create JSON Message to send to recipient";
				LogManager.LogErrorMsg(className, methodName, notificationMessage);
				Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + notificationMessage);
				return;
			}
			
			logMessage = "Sending command [" + command.toString() + "] as asynchonous task... ";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			
			Runnable sendMessageAsync = new SendMessageAsync(jsonMessage, context);
			new Thread(sendMessageAsync).start();
			
			logMessage = "[" + command.toString() + "] is sending asynchronously ...";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			
		} else {
			logMessage = "Unable to send command: [" + command.toString() + "] - there is no any recipient.";
			LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", 
				logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

}
