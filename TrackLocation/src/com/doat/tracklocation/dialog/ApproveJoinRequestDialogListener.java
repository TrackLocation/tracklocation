package com.doat.tracklocation.dialog;

import android.util.Log;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.context.ApproveJoinRequestContext;
import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.datatype.CommandData;
import com.doat.tracklocation.datatype.CommandDataBasic;
import com.doat.tracklocation.datatype.CommandEnum;
import com.doat.tracklocation.datatype.CommandKeyEnum;
import com.doat.tracklocation.datatype.CommandValueEnum;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.datatype.MessageDataLocation;
import com.doat.tracklocation.datatype.ReceivedJoinRequestData;
import com.doat.tracklocation.datatype.SMSMessage;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.exception.UnableToSendCommandException;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;

public class ApproveJoinRequestDialogListener implements
		ICommonDialogNewOnClickListener {

	private String message = null, key = null, value = null;
	private MessageDataLocation location = null;
	private String className;    
	private String methodName;
    private String logMessage;
    private ApproveJoinRequestContext approveJoinRequestContext;
    private SMSMessage smsMessage;

	public ApproveJoinRequestDialogListener(ApproveJoinRequestContext approveJoinRequestContext, SMSMessage smsMessage) {
		className = this.getClass().getName();
		methodName = "ApproveJoinRequestDialogListener";
		this.approveJoinRequestContext = approveJoinRequestContext;
		this.smsMessage = smsMessage;
	}

	@Override
	public void doOnPositiveButton(Object data) {
		methodName = "doOnPositiveButton";
		
		String mutualId = approveJoinRequestContext.getMutualId();
		
		// Send JOIN APPROVED command (CommandEnum.join_approval) with
		// information about contact approving join request sent by SMS
		CommandDataBasic commandDataBasic;
		try {
			commandDataBasic = new CommandData(
				approveJoinRequestContext.getContext(), 
				approveJoinRequestContext.getContactDeviceDataList(), 
				CommandEnum.join_approval,
				message,					// null
				approveJoinRequestContext.getSenderMessageDataContactDetails(),
				location, 					// null
				CommandKeyEnum.mutualId.toString(),
				mutualId, 
				approveJoinRequestContext.getAppInfo()
			);
			commandDataBasic.sendCommand();
		} catch (UnableToSendCommandException e) {
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
		}
		
		// Remove from RECEIVED_JOIN_REQUEST
		ReceivedJoinRequestData receivedJoinRequestData = DBLayer.getInstance().getReceivedJoinRequest(mutualId);
		if( receivedJoinRequestData == null ){
			logMessage = "Failed to get received join request data";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}else {
			logMessage = "ReceivedJoinRequestData = " + receivedJoinRequestData.toString();
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			
			// Add information to DB about contact, requesting join operation 
			ContactDeviceDataList contactDeviceDataListOwner = 
					DBLayer.getInstance().addContactDeviceDataList(
					new ContactDeviceDataList(	receivedJoinRequestData.getAccount(),
												receivedJoinRequestData.getMacAddress(), 
												receivedJoinRequestData.getPhoneNumber(), 
												receivedJoinRequestData.getRegId(), 
												null));
			if( contactDeviceDataListOwner == null ){
	        	logMessage = "Failed to save to DB details of the contcat requesting join operation";
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			}
		}
				
		// Delete join request that was handled 
		deleteReceivedJoinRequest(approveJoinRequestContext.getMutualId(), approveJoinRequestContext.getOwnerEmail());

		MessageDataContactDetails joinRequesterMessageDataContactDetails = 
			new MessageDataContactDetails(receivedJoinRequestData.getAccount(), 
				receivedJoinRequestData.getMacAddress(), 
				receivedJoinRequestData.getPhoneNumber(), 
				receivedJoinRequestData.getRegId(), 
				0);
		
		// Broadcast message to update ContactList
		Controller.broadcsatMessage(approveJoinRequestContext.getContext(), 
			joinRequesterMessageDataContactDetails, 
			BroadcastActionEnum.BROADCAST_MESSAGE.toString(), 
			"Update Contacts List", 
			CommandKeyEnum.update_contact_list.toString(), 
			CommandValueEnum.update_contact_list.toString());
	}

	@Override
	public void doOnNegativeButton(Object data) {
		methodName = "doOnNegativeButton";
		
		// Send JOIN REJECTED command (CommandEnum.join_rejected) with
		// information about contact rejecting join request sent by SMS
		CommandDataBasic commandDataBasic;
		try {
			commandDataBasic = new CommandData(
				approveJoinRequestContext.getContext(), 
				approveJoinRequestContext.getContactDeviceDataList(), 
				CommandEnum.join_rejected,
				message, 			// null
				approveJoinRequestContext.getSenderMessageDataContactDetails(), 
				location,			// null
				key,				// null
				value,				// null
				approveJoinRequestContext.getAppInfo()
			);
			commandDataBasic.sendCommand();
		} catch (UnableToSendCommandException e) {
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
		}
		
		// Remove from RECEIVED_JOIN_REQUEST
		deleteReceivedJoinRequest(approveJoinRequestContext.getMutualId(), approveJoinRequestContext.getOwnerEmail());
	}

	private void deleteReceivedJoinRequest(String mutualId, String account){
		methodName = "deleteReceivedJoinRequest";
		int count = DBLayer.getInstance().deleteReceivedJoinRequest(mutualId);
		if(count == 0){
			logMessage = "Failed to delete received join request with mutual id [" + mutualId + "] from [" + account + "] account.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		} else {
			logMessage = "Deleted received join request with mutual id [" + mutualId + "] from [" + account + "] account.";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
	}
	
	@Override
	public void doOnChooseItem(int which) {
		// TODO Auto-generated method stub

	}
}
