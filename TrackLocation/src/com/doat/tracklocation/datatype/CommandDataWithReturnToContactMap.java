package com.doat.tracklocation.datatype;

import java.util.ArrayList;
import java.util.List;

import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;

import android.content.Context;
import android.util.Log;

public class CommandDataWithReturnToContactMap extends CommandDataBasic {
//	private List<String> returnToContactList; // registration_IDs of the contacts that command will be send to
	protected java.util.Map<String, String> returnToContactMap;
	
	// returnToContactList = Controller.getPreferencesReturnToRegIDList(context);
	public CommandDataWithReturnToContactMap(Context context,
			CommandEnum command,
			String message, MessageDataContactDetails senderMessageDataContactDetails,
			MessageDataLocation location, String key, String value, AppInfo appInfo) {
		super(context,
				command,
				message, senderMessageDataContactDetails,
				location, key, value, appInfo);
		returnToContactMap = Preferences.getAccountRegIdMap(context, 
			CommonConst.PREFERENCES_LOCATION_REQUESTER_MAP__ACCOUNT_AND_REG_ID);
		Log.i(CommonConst.LOG_TAG_TLS, "returnToContactMap: " + returnToContactMap.toString());
		initialValuesCheck();
		listAccounts = new ArrayList<String>();
		listRegIDs = new ArrayList<String>();
		prepareAccountAndRegIdLists(listAccounts, listRegIDs);
	}

	public java.util.Map<String, String> getReturnToContactMap() {
		return returnToContactMap;
	}

	public void setReturnToContactMap(
			java.util.Map<String, String> returnToContactMap) {
		this.returnToContactMap = returnToContactMap;
	}

	private void initialValuesCheck() {
		if(returnToContactMap == null){
			notificationMessage = "There is no recipient map defined";
			LogManager.LogErrorMsg(className, "[sendCommand:" + command.toString() + "]", notificationMessage);
			return;
		}
	}
	
	@Override
	protected void prepareAccountAndRegIdLists(List<String> listAccounts,
			List<String> listRegIDs) {
		super.prepareAccountAndRegIdLists(listAccounts, listRegIDs);
		if(returnToContactMap != null){
			for (java.util.Map.Entry<String, String> entry : returnToContactMap.entrySet()) {
				listAccounts.add(entry.getKey());
				listRegIDs.add(entry.getValue());
			}
		}
	}
}
