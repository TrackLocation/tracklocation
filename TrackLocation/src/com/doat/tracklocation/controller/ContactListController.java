package com.doat.tracklocation.controller;

import java.lang.Thread.State;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.concurrent.CheckWhichContactsOnLine;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.model.ContactListModel;
import com.doat.tracklocation.utils.CommonConst;

public class ContactListController {
	private Activity contactListControllerActivity;
	private Context context;
	private ContactDeviceDataList contactDeviceDataList;
	private ContactListModel contactListModel;
	private String className;
	private String methodName;
	private String logMessage;
	private Thread checkWhichContactsOnLineThread;
	private Runnable checkWhichContactsOnLineService;
	
	public ContactListController(Activity contactListControllerActivity, Context context){
		className = this.getClass().getName();
		this.contactListControllerActivity = contactListControllerActivity;
		this.context = context;
		methodName = "ContactListController";
		
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		if(contactListModel == null){
			contactListModel = new ContactListModel();
		}
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	// Start thread to check which contacts are online
	public void checkWhichContactsOnLine(ContactDeviceDataList contactDeviceDataList){
		methodName = "checkWhichContactsOnLine";
		logMessage = "Start thread to check which contacts are online";
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		
		MessageDataContactDetails senderMessageDataContactDetails = Controller.getMessageDataContactDetails(context);
		
		checkWhichContactsOnLineService = new CheckWhichContactsOnLine(context, 
			contactDeviceDataList, senderMessageDataContactDetails); 
		
		// ===========================================================================
		// Start thread to check which contacts are online
		// ===========================================================================
		try {
			checkWhichContactsOnLineThread = new Thread(checkWhichContactsOnLineService);
			checkWhichContactsOnLineThread.start();
			LogManager.LogFunctionExit(className, methodName);
			Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
		} catch (IllegalThreadStateException e) {
			logMessage = "Thread for checking which contacts are online - has been started already.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
		}
	}
	
	// Stop thread that checking which contacts are online
	public void stopCheckWhichContactsOnLine(){
		methodName = "stopCheckWhichContactsOnLine";
		logMessage = "Stop thread that checking which contacts are online";
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		
		checkWhichContactsOnLineThread.interrupt();
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
	
	public State getThreadState(){
		if(checkWhichContactsOnLineThread != null){
			return checkWhichContactsOnLineThread.getState();
		} else {
			return null;
		}
	}
}
