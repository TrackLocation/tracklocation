package com.dagrest.tracklocation.datatype;

import java.util.List;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.SendMessageAsync;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
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
		
		if(senderMessageDataContactDetails == null){
			notificationMessage = "There is no sender defined";
			LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", notificationMessage);
			return;
		}
	}
	
	protected void prepareAccountAndRegIdLists(List<String> listAccounts, List<String> listRegIDs){
	}
	
	
	public void sendCommand(){
		
		LogManager.LogFunctionCall(className, "[sendCommand]");
		Log.i(CommonConst.LOG_TAG, "[sendCommand]");
		
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
				LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", notificationMessage);
				return;
			}
			
			LogManager.LogInfoMsg(className, "[sendCommand:" + command.toString() + "]", 
				"Sending command [" + command.toString() + "] as asynchonous task... ");
			
			Runnable sendMessageAsync = new SendMessageAsync(jsonMessage, context);
			new Thread(sendMessageAsync).start();
			Log.i(CommonConst.LOG_TAG, "[" + command.toString() + "] is sending asynchronously ...");
			
		} else {
			LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", 
				"Unable to send command: [" + command.toString() + "] - there is no any recipient.");
		}
		
		LogManager.LogFunctionExit(className, "[sendCommand:" + command.toString() + "]");
	}

}
