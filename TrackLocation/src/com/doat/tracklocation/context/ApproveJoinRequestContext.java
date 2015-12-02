package com.doat.tracklocation.context;

import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.ReceivedJoinRequestData;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;

import android.content.Context;
import android.util.Log;

public class ApproveJoinRequestContext extends AppContextBase {

	private String mutualId;
	
	public ApproveJoinRequestContext(Context context, String mutualId) {
		setContext(context, mutualId);
	}

	public String getMutualId() {
		return mutualId;
	}

	public void setContext(Context context, String mutualId){
		methodName = "setContext";
		setContext(context);
		this.mutualId = mutualId;
		ReceivedJoinRequestData receivedJoinRequestData = DBLayer.getInstance().getReceivedJoinRequest(mutualId);
		if( receivedJoinRequestData == null ){
	    	logMessage = "Failed to get received join request data";
	        Log.e(CommonConst .LOG_TAG, logMessage);
	        LogManager.LogErrorMsg(className, methodName, logMessage);
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
	}

}
