package com.dagrest.tracklocation.controller;

import java.util.List;
import java.util.UUID;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.R;
import com.dagrest.tracklocation.concurrent.CheckJoinRequestBySMS;
import com.dagrest.tracklocation.concurrent.RegisterToGCMInBackground;
import com.dagrest.tracklocation.datatype.AppInfo;
import com.dagrest.tracklocation.datatype.AppInstDetails;
import com.dagrest.tracklocation.datatype.BackupDataOperations;
import com.dagrest.tracklocation.datatype.CommandData;
import com.dagrest.tracklocation.datatype.CommandDataBasic;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.CommandKeyEnum;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.MessageDataContactDetails;
import com.dagrest.tracklocation.datatype.MessageDataLocation;
import com.dagrest.tracklocation.datatype.ReceivedJoinRequestData;
import com.dagrest.tracklocation.datatype.SMSMessage;
import com.dagrest.tracklocation.datatype.SMSMessageList;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.dialog.CommonDialog;
import com.dagrest.tracklocation.dialog.IDialogOnClickAction;
import com.dagrest.tracklocation.exception.CheckPlayServicesException;
import com.dagrest.tracklocation.exception.UnableToSendCommandException;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.model.MainModel;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.dagrest.tracklocation.utils.Utils;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

public class MainActivityController {
	
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static int SLEEP_TIME = 500; // 0.5 sec
	private Activity mainActivity;
	private MainModel mainModel;

	private String className;
    private String logMessage;
    private String methodName;
	
    private GoogleCloudMessaging gcm;
    private Context context;
    private String googleProjectNumber;
    private String phoneNumber;
    private String macAddress;
    private List<String> accountList;
    private String account;
    private AppInstDetails appInstDetails;
    private String registrationId;
    private ProgressDialog waitingDialog;
    // private boolean isChooseAccountDialogOpened = false;
    private AppInfo preinstalledAppInfo;
    
	private Thread registerToGCMInBackgroundThread;
	private Runnable registerToGCMInBackground;
	private Thread checkJoinRequestBySMSInBackgroundThread;
	private Runnable checkJoinRequestBySMSInBackground;
    
    public MainActivityController(Activity mainActivity, Context context){
    	className = this.getClass().getName();
    	this.mainActivity = mainActivity;
    	this.context = context;
    	if(mainModel == null){
    		mainModel = new MainModel();
    	}
    	start();
    }
    
    public void start(){
		googleProjectNumber = mainActivity.getResources().getString(R.string.google_project_number);
		Preferences.setPreferencesString(context, CommonConst.GOOGLE_PROJECT_NUMBER, googleProjectNumber);

		BackupDataOperations backupData = new BackupDataOperations();
//		boolean isBackUpSuccess = backupData.backUp();
//		if(isBackUpSuccess != true){
//			logMessage = methodName + " -> Backup process failed.";
//			LogManager.LogErrorMsg(className, methodName, logMessage);
//			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
//		}
		boolean isBackUpRestoreSuccess = backupData.restore();
		if(isBackUpRestoreSuccess != true){
			logMessage = methodName + " -> Restore process failed.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
		
		// Version check
		preinstalledAppInfo = Controller.getAppInfo(context);
		
		// INIT START ===================================================
		account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		if( account == null || account.isEmpty() ){
			getCurrentAccount();
		} else {
			initCont();
		}
		
    }
    
    private void initCont(){
		Controller.saveAppInfoToPreferences(context);
		
		// PHONE NUMBER
		phoneNumber = Controller.getPhoneNumber(context);
		//Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_PHONE_NUMBER, phoneNumber);
		if(phoneNumber == null || phoneNumber.isEmpty()){
			phoneNumber = UUID.randomUUID().toString().replace("-", "");
		}
		Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER, phoneNumber);
		
		// MAC ADDRESS
		macAddress = Controller.getMacAddress(mainActivity);
		//Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS, macAddress);
		Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS, macAddress);
		
		// INIT END ===================================================
		
		AppInfo installedAppInfo = Controller.getAppInfo(context);
				
		if(preinstalledAppInfo.getVersionNumber() < installedAppInfo.getVersionNumber()){
			// reset resistrationID
			Preferences.setPreferencesString(context, CommonConst.PREFERENCES_REG_ID, "");
			Preferences.setPreferencesString(context, CommonConst.APP_INST_DETAILS, "");
		}
		
		// Create application details during first installation 
		// if was created already just returns the details:
		//    - First installation's timestamp 
		//    - ApInfo: version number and version name
		appInstDetails = new AppInstDetails(context); 
		
		// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
		try {
			Controller.checkPlayServices(context);
		} catch (CheckPlayServicesException e) {
			String errorMessage = e.getMessage();
			if(CommonConst.PLAYSERVICES_ERROR.equals(errorMessage)){
	            GooglePlayServicesUtil.getErrorDialog(e.getResultCode(), mainActivity,
	            	PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else if(CommonConst.PLAYSERVICES_DEVICE_NOT_SUPPORTED.equals(errorMessage)){
				// Show dialog with errorMessage and exit from application 
				showNotificationDialog("\nGoogle Play Services not supported with this device.\nProgram will be closed.\n", "FINISH");
			}
            Log.e(CommonConst.LOG_TAG, "No valid Google Play Services APK found.");
    		LogManager.LogInfoMsg(className, methodName, "No valid Google Play Services APK found.");
			//finish();
		}
		
		// ===============================================================
		// READ CONTACT AND DEVICE DATA FROM JSON FILE AND INSERT IT TO DB
		// ===============================================================
		// Read contact and device data from json file and insert it to DB
		String jsonStringContactDeviceData = Utils.getContactDeviceDataFromJsonFile();
		if(jsonStringContactDeviceData != null && !jsonStringContactDeviceData.isEmpty()) {
			ContactDeviceDataList contactDeviceDataList = Utils.fillContactDeviceDataListFromJSON(jsonStringContactDeviceData);
			if(contactDeviceDataList != null){
				DBLayer.getInstance().addContactDeviceDataList(contactDeviceDataList);
			}
		}
		
        gcm = GoogleCloudMessaging.getInstance(mainActivity);
        registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
        if (registrationId == null || registrationId.isEmpty()) {
        	logMessage = "Register to Google Cloud Message.";
        	LogManager.LogInfoMsg(className, methodName, logMessage);
    		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

        	googleProjectNumber = Preferences.getPreferencesString(context, CommonConst.GOOGLE_PROJECT_NUMBER);
        	if(googleProjectNumber == null || googleProjectNumber.isEmpty()){
				logMessage = "Google Project Number is NULL or EMPTY";
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
        	}
        	registerToGCMInBackground = new RegisterToGCMInBackground(context, gcm, googleProjectNumber);
			try {
				registerToGCMInBackgroundThread = new Thread(registerToGCMInBackground);
				registerToGCMInBackgroundThread.start();
				// Launch waiting dialog - till registration process will be completed or failed
				launchWaitingDialog();
			} catch (IllegalThreadStateException e) {
				logMessage = "Register to GCM in background thread was started already";
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
			}
						
        } else {
        	initWithRegID(registrationId);
        }
    
// 		IMPORTANT: Preparation for tracking option - start tracking autostarter service        
//		Intent trackingService = new Intent(context, TrackingAutostarter.class);
//		ComponentName componentName = context.startService(trackingService); 
//		if(componentName != null){
//			logMessage = "TrackingAutostarter is STARTED";
//			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
//		} else {
//			logMessage = "TrackingAutostarter FAILED TO START";
//			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
//		}
		
		// Controller.checkJoinRequestBySMS(new Object[] {context, MainActivity.this}); 
		checkJoinRequestBySMSInBackground = new CheckJoinRequestBySMS(context, mainActivity);
		try {
			checkJoinRequestBySMSInBackgroundThread = new Thread(checkJoinRequestBySMSInBackground);
			logMessage = "Started a separate thread to check Join request by SMS";
			Log.i(CommonConst.LOG_TAG, logMessage);
			LogManager.LogInfoMsg(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> initCont()", logMessage);
			checkJoinRequestBySMSInBackgroundThread.start();
		} catch (IllegalThreadStateException e) {
			logMessage = "Check Join request by SMS in background thread was started already";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
		}
    }
    
	private void getCurrentAccount(){
		// CURRENT ACCOUNT
		if( Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT) == null || 
			Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT).isEmpty() ) {
			
			accountList = Controller.getAccountList(context);
			if(accountList != null && accountList.size() == 1){
				account = accountList.get(0);
				//Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_PHONE_ACCOUNT, account);
				Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT, account);
				initCont();
			} else {
				showChooseAccountDialog();
//				Intent intent = new Intent(context,ActivityDialog.class);
//				mainActivity.startActivity(intent);
			}
		}
	}
	
	IDialogOnClickAction dialogActionsAboutDialog = new IDialogOnClickAction() {
		@Override
		public void doOnPositiveButton() {
		}
		@Override
		public void doOnNegativeButton() {
		}
		@Override
		public void setActivity(Activity activity) {
			// TODO Auto-generated method stub
		}
		@Override
		public void setContext(Context context) {
			// TODO Auto-generated method stub
		}
		@Override
		public void setParams(Object[]... objects) {
			// TODO Auto-generated method stub
		}
		@Override
		public void doOnChooseItem(int which) {
			// TODO Auto-generated method stub
		}
	};

	IDialogOnClickAction dialogChooseAccountDialog = new IDialogOnClickAction() {
		@Override
		public void doOnPositiveButton() {
		}
		@Override
		public void doOnNegativeButton() {
		}
		@Override
		public void setActivity(Activity activity) {
			// TODO Auto-generated method stub
		}
		@Override
		public void setContext(Context context) {
			// TODO Auto-generated method stub
		}
		@Override
		public void setParams(Object[]... objects) {
			// TODO Auto-generated method stub
		}
		@Override
		public void doOnChooseItem(int which) {
			account = accountList.get(which);
			Preferences.setPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT, account);
			//init();
			initCont();
		}
	};

	IDialogOnClickAction notificationDialogOnClickAction = new IDialogOnClickAction() {
		
		boolean isExit = false;
		
		@Override
		public void doOnPositiveButton() {
			if(isExit == true){
				// finish();
				mainActivity.finish();
			}
		}
		@Override
		public void doOnNegativeButton() {
			// TODO Auto-generated method stub
		}
		@Override
		public void setActivity(Activity activity) {
			// TODO Auto-generated method stub
		}
		@Override
		public void setContext(Context context) {
			// TODO Auto-generated method stub
		}
		@Override
		public void setParams(Object[]... objects) {
			if(objects[0][0] instanceof Boolean){
				isExit = (Boolean) objects[0][0];
			}
		}
		@Override
		public void doOnChooseItem(int which) {
			// TODO Auto-generated method stub
			
		}
	};

	private void showNotificationDialog(String errorMessage, String action) {
    	//String dialogMessage = "\nGoogle Cloud Service is not available right now.\n\nPlease try later.\n";
    	String dialogMessage = errorMessage;
    	
    	if(action != null && "FINISH".equals(action)){
    		notificationDialogOnClickAction.setParams(new Object[] {true});
    	}
		CommonDialog aboutDialog = new CommonDialog(mainActivity, notificationDialogOnClickAction);
		aboutDialog.setDialogMessage(dialogMessage);
		aboutDialog.setDialogTitle("Warning");
		aboutDialog.setPositiveButtonText("OK");
		aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
		aboutDialog.showDialog();
		aboutDialog.setCancelable(true);
    }

	private void showChooseAccountDialog() {
		CommonDialog aboutDialog = new CommonDialog(mainActivity, dialogChooseAccountDialog);
		aboutDialog.setDialogTitle("Choose current account:");
		aboutDialog.setItemsList(accountList.toArray(new String[0]));

		aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
		aboutDialog.showDialog();
		aboutDialog.setCancelable(false);
    }

	private void launchWaitingDialog() {
		final int MAX_RETRY_TIMES = 5;
		
        waitingDialog = new ProgressDialog(mainActivity);
        waitingDialog.setTitle("Registration for Google Cloud Messaging");
        waitingDialog.setMessage("Please wait ...");
        waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //progressDialog.setProgress(0);
        //progressDialog.setMax(contactsQuantity);
        //waitingDialog.setCancelable(false);
        waitingDialog.setIndeterminate(true);
        waitingDialog.show();
        waitingDialog.setCanceledOnTouchOutside(false);
        
        new Thread(new Runnable() {
            @Override
            public void run() {
            	String regID = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
            	logMessage = "MainActivity BEFORE while in wainting dialog... regId = [" + Controller.hideRealRegID(regID) + "]";
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
				int retryTimes = 0;
            	while((regID == null || regID.isEmpty()) && retryTimes < MAX_RETRY_TIMES){
                	try {
    					Thread.sleep(SLEEP_TIME); 
    				} catch (InterruptedException e) {
    					waitingDialog.dismiss();
    					break;
    				}
                	regID = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
                	logMessage = "MainActivity INSIDE while in wainting dialog... regId = [" + Controller.hideRealRegID(regID) + "]";
    				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
    				if(regID != null && !regID.isEmpty()){
    					waitingDialog.dismiss();
    					break;
    				}
    				retryTimes++;
            	}

            	registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
            	if(registrationId != null & !registrationId.isEmpty()){
            		initWithRegID(regID);
            	} else {
            		if(registrationId == null || !registrationId.isEmpty()){
            			String errorMessage = "\nGoogle Cloud Service is not available right now.\n\n"
            				+ "Application will be closed.\n\nPlease try later.\n";
            			showNotificationDialog(errorMessage, "FINISH");
            		}
            	}
            }
        }).start();
   
	}

	private void initWithRegID(String registrationId){
		
		// Insert into DB: owner information if doesn't exist
		ContactDeviceDataList contDevDataList = DBLayer.getInstance().getContactDeviceDataList(account);
		if( contDevDataList == null || contDevDataList.size() == 0){
			// add information about owner to DB 
			ContactDeviceDataList contactDeviceDataListOwner = 
				DBLayer.getInstance().addContactDeviceDataList(new ContactDeviceDataList(account,
					macAddress, phoneNumber, registrationId, null));
			if(contactDeviceDataListOwner == null){
				logMessage = "Failed to add owner information to application's DB";
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			}
		} else {
			logMessage = "Owner information already exists in application's DB";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			// FIXME: Update only if registration ID in DB is differs
			long res = DBLayer.getInstance().updateRegistrationID(account, macAddress, registrationId);
			if(res <= 0){
				logMessage = "Unable to update registration ID";
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.i(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			} else {
				logMessage = "Owner registration ID has been updated";
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			}
		}
		
		LogManager.LogInfoMsg(className, "SAVE OWNER INFO", 
				account + CommonConst.DELIMITER_COLON + macAddress + CommonConst.DELIMITER_COLON + 
				phoneNumber + CommonConst.DELIMITER_COLON + Controller.hideRealRegID(registrationId));
		
//		contDevDataList = DBLayer.getInstance().getContactDeviceDataList(account);
//		if(contDevDataList != null){
//			// get owner information from DB and save GUID to Preferences
//			for (ContactDeviceData cdd : contDevDataList.getContactDeviceDataList()) {
//				// Controller.saveValueToPreferencesIfNotExist(context, CommonConst.PREFERENCES_OWNER_GUID, cdd.getGuid());
//				Preferences.setPreferencesString(context, CommonConst.PREFERENCES_OWNER_GUID, cdd.getGuid());
//			}
//		} else {
//        	String errMsg = "Failed to save PREFERENCES_OWNER_GUID - no owner details were created";
//        	Log.e(CommonConst.LOG_TAG, errMsg);
//            LogManager.LogErrorMsg(className, "init", errMsg);
//		}
//		
// 		contactDeviceDataList = DBLayer.getInstance().getContactDeviceDataList(null);
		
        registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
        if (registrationId == null || registrationId.isEmpty()) {
        	String errorMessage = "\nFailed to register your application\nwith Google Cloud Message\n\n" + 
        		"Application will be closed\n\nPlease try later...\n\n";
        	// Show dialog with errorMessage and exit from application
        	// showNotificationDialog(errorMessage, "FINISH");
            Log.e(CommonConst.LOG_TAG, errorMessage);
    		LogManager.LogInfoMsg(className, "onCreate", 
    			errorMessage);
        }
	}

	public String getRegistrationId() {
		return registrationId;
	}

	public Thread getRegisterToGCMInBackgroundThread() {
		return registerToGCMInBackgroundThread;
	}
	
  public void showApproveJoinRequestDialog(Activity activity, Context context,
			String account, String phoneNumber, String mutualId, String regId, 
			String macAddress, SMSMessage smsMessage) {
  	String dialogMessage = "Approve join request from " +
			account + "\n[" + phoneNumber + "]";
  	
  	IDialogOnClickAction approveJoinRequestDialogOnClickAction = new IDialogOnClickAction() {
			Context context;	
			String mutualId;
			String regId;
//			String email;
//			String macAddress;
			SMSMessage smsMessage;
			
          String ownerEmail;
          String ownerMacAddress;
          String ownerRegId;
          String ownerPhoneNumber;
          String message = null, key = null, value = null;
          float batteryPercentage = -1;
          AppInfo appInfo = null;

          ContactDeviceDataList contactDeviceDataList = null;
          ReceivedJoinRequestData receivedJoinRequestData = null;
			MessageDataContactDetails senderMessageDataContactDetails = null; 
			MessageDataLocation location = null;
       
			@Override
			public void doOnPositiveButton() {
				String methodName = "doOnPositiveButton";
				// Send JOIN APPROVED command (CommandEnum.join_approval) with
				// information about contact approving join request sent by SMS
//				sendApproveOnJoinRequest(context, regId, mutualId, ownerEmail, ownerRegId, ownerMacAddress, 
//					ownerPhoneNumber, CommandEnum.join_approval);
				
				if(smsMessage != null){
					saveHandledSmsDetails(context, smsMessage);
				}
				
				CommandDataBasic commandDataBasic;
				try {
					commandDataBasic = new CommandData(
						context, 
						contactDeviceDataList, 
						CommandEnum.join_approval,
						message,					// null
						senderMessageDataContactDetails,
						location, 					// null
						CommandKeyEnum.mutualId.toString(),
						mutualId, 
						appInfo
					);
					commandDataBasic.sendCommand(/*true*/);
				} catch (UnableToSendCommandException e) {
					LogManager.LogException(e, className, methodName);
					Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
				}
				
				// Remove from RECEIVED_JOIN_REQUEST
				ReceivedJoinRequestData receivedJoinRequestData = DBLayer.getInstance().getReceivedJoinRequest(mutualId);
				if( receivedJoinRequestData == null ){
		        	String errMsg = "Failed to get received join request data";
		            Log.e(CommonConst .LOG_TAG, errMsg);
		            LogManager.LogErrorMsg(className, "showApproveJoinRequestDialog", errMsg);
				}else {
					Log.i(CommonConst.LOG_TAG, "ReceivedJoinRequestData = " + receivedJoinRequestData.toString());
				}
				
				// Add information to DB about contact, requesting join operation 
				ContactDeviceDataList contactDeviceDataListOwner = 
						DBLayer.getInstance().addContactDeviceDataList(
						new ContactDeviceDataList(	receivedJoinRequestData.getAccount(),
													receivedJoinRequestData.getMacAddress(), 
													receivedJoinRequestData.getPhoneNumber(), 
													receivedJoinRequestData.getRegId(), 
													null));
				if( contactDeviceDataListOwner == null ){
		        	String errMsg = "Failed to save to DB details of the contcat requesting join operation";
		            Log.e(CommonConst .LOG_TAG, errMsg);
		            LogManager.LogErrorMsg(className, "showApproveJoinRequestDialog", errMsg);
				}
				
				// Delete join request that was handled 
				int count = DBLayer.getInstance().deleteReceivedJoinRequest(mutualId);
				if( count < 1) {
					Log.i(CommonConst.LOG_TAG, "Failed to delete recived join request with mutual id: " + mutualId + " count = " + count);
				} else {
					Log.i(CommonConst.LOG_TAG, "Deleted recived join request with mutual id: " + mutualId + " count = " + count);
				}
			}
			
			@Override
			public void doOnNegativeButton() {
				String methodName = "doOnNegativeButton";
				// Send JOIN REJECTED command (CommandEnum.join_rejected) with
				// information about contact approving join request sent by SMS
//				sendApproveOnJoinRequest(context, regId, mutualId, ownerEmail, ownerRegId, ownerMacAddress, 
//					ownerPhoneNumber, CommandEnum.join_rejected);

				if(smsMessage != null){
					saveHandledSmsDetails(context, smsMessage);
				}

				CommandDataBasic commandDataBasic;
				try {
					commandDataBasic = new CommandData(
						context, 
						contactDeviceDataList, 
						CommandEnum.join_rejected,
						message, 			// null
						senderMessageDataContactDetails, 
						location,			// null
						key,				// null
						value,				// null
						appInfo
					);
					commandDataBasic.sendCommand(/*true*/);
				} catch (UnableToSendCommandException e) {
					LogManager.LogException(e, className, methodName);
					Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
				}
				
				// Remove from RECEIVED_JOIN_REQUEST
				int count = DBLayer.getInstance().deleteReceivedJoinRequest(mutualId);
				if(count == 0){
					String errorMsg = "Failed to delete received join request from " + ownerEmail;
					LogManager.LogErrorMsg(className, "doOnNegativeButton->deleteReceivedJoinRequest", errorMsg);
				}
			}
			
			@Override
			public void setActivity(Activity activity) {
			}
			
			@Override
			public void setContext(Context context) {
				this.context = context;
				ownerEmail = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
	            ownerMacAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
	            ownerRegId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
	            ownerPhoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
				ReceivedJoinRequestData receivedJoinRequestData = DBLayer.getInstance().getReceivedJoinRequest(mutualId);
				if( receivedJoinRequestData == null ){
		        	String errMsg = "Failed to get received join request data";
		            Log.e(CommonConst .LOG_TAG, errMsg);
		            LogManager.LogErrorMsg(className, "showApproveJoinRequestDialog", errMsg);
		            return;
				}else {
					contactDeviceDataList = 
							new ContactDeviceDataList(
									receivedJoinRequestData.getAccount(), 
									receivedJoinRequestData.getMacAddress(), 
									receivedJoinRequestData.getPhoneNumber(), 
									receivedJoinRequestData.getRegId(), 
									null);
					Log.i(CommonConst.LOG_TAG, "ReceivedJoinRequestData = " + receivedJoinRequestData.toString());
				}
				senderMessageDataContactDetails = 
						new MessageDataContactDetails(ownerEmail, ownerMacAddress, ownerPhoneNumber, ownerRegId, batteryPercentage);
				appInfo = Controller.getAppInfo(context);
			}
			
			@Override
			public void setParams(Object[]... objects) {
				mutualId = objects[0][0].toString();
				regId = objects[0][1].toString();
				smsMessage = (SMSMessage)objects[0][2];
//				email = objects[0][2].toString();
//				macAddress = objects[0][3].toString();
			}

			@Override
			public void doOnChooseItem(int which) {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		Object[] objects = new Object[3];
		objects[0] = mutualId;
		objects[1] = regId;
		objects[2] = smsMessage;
		approveJoinRequestDialogOnClickAction.setParams(objects);
		approveJoinRequestDialogOnClickAction.setContext(context);
//		objects[2] = account;
//		objects[3] = macAddress;
  	
		CommonDialog aboutDialog = new CommonDialog(activity, approveJoinRequestDialogOnClickAction);
		
		aboutDialog.setDialogMessage(dialogMessage);
		aboutDialog.setDialogTitle("Join request approval");
		aboutDialog.setPositiveButtonText("OK");
		aboutDialog.setNegativeButtonText("Cancel");
		aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
		aboutDialog.showDialog();
		aboutDialog.setCancelable(false);
  }
	
  
	public void saveHandledSmsDetails(Context ctx, SMSMessage smsMessage) {
		Gson gson = new Gson();
		SMSMessageList handledSmsList = null;
		String jsonHandledSmsList = Preferences.getPreferencesString(ctx,
				CommonConst.PREFERENCES_HANDLED_SMS_LIST);
		if (jsonHandledSmsList == null || jsonHandledSmsList.isEmpty()) {
			handledSmsList = new SMSMessageList();
		} else {
			handledSmsList = gson.fromJson(jsonHandledSmsList,
					SMSMessageList.class);
		}
		if (handledSmsList != null && !isContain(handledSmsList, smsMessage)) {
			handledSmsList.getSmsMessageList().add(smsMessage);
			String jsonHandledSmsListNew = gson.toJson(handledSmsList);
			Preferences.setPreferencesString(ctx,
					CommonConst.PREFERENCES_HANDLED_SMS_LIST,
					jsonHandledSmsListNew);
		}
	}

	public boolean isContain(SMSMessageList handledSmsList,
			SMSMessage smsMessage) {
		if (handledSmsList == null) {
			return false;
		}
		if (smsMessage == null) {
			return false;
		}
		long smsMessageDateLong = Long.parseLong(smsMessage.getMessageDate());
		List<SMSMessage> list = handledSmsList.getSmsMessageList();
		if (list != null) {
			for (SMSMessage smsMessageEntity : list) {
				if (smsMessageEntity != null
						&& smsMessageEntity.getMessageDate().equals(
								smsMessage.getMessageDate())
						&& smsMessageEntity.getMessageId().equals(
								smsMessage.getMessageId())) {
					return true;
				}
				long smsMessageEntityDateLongFromList = Long
						.parseLong(smsMessageEntity.getMessageDate());
				// Check that smsMessage from list (handled one) has greater
				// timestamp than smsMessage
				// that currently checking (if handled) - consider such
				// smsMessage as handled
				if (smsMessageDateLong < smsMessageEntityDateLongFromList) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isHandledSmsDetails(Context ctx, SMSMessage smsMessage) {
		Gson gson = new Gson();
		SMSMessageList handledSmsList = null;
		if (smsMessage == null) {
			return true;
		}

		// If installation date(timestamp) of application id greater
		// then SMS date(timestamp) - consider SMS as handled
		AppInstDetails appInstDetails = new AppInstDetails(ctx);
		long appInstTimestamp = appInstDetails.getTimestamp();
		String smsMessageDate = smsMessage.getMessageDate();
		if (smsMessageDate != null && !smsMessageDate.isEmpty()) {
			long longSmsMessageTimestamp = Long.parseLong(smsMessageDate);
			if (appInstTimestamp > longSmsMessageTimestamp) {
				return true;
			}
		}

		String jsonHandledSmsList = Preferences.getPreferencesString(ctx,
				CommonConst.PREFERENCES_HANDLED_SMS_LIST);
		if (jsonHandledSmsList != null && !jsonHandledSmsList.isEmpty()) {
			handledSmsList = gson.fromJson(jsonHandledSmsList,
					SMSMessageList.class);
			if (handledSmsList != null && isContain(handledSmsList, smsMessage)) {
				return true;
			}
		}
		return false;
	}

}
