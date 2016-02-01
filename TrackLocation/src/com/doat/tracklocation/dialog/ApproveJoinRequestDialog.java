package com.doat.tracklocation.dialog;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.doat.tracklocation.datatype.SMSMessage;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;

public class ApproveJoinRequestDialog extends CommonDialog {
	
	public ApproveJoinRequestDialog(Activity activity, Context context,
			ICommonDialogOnClickListener onClickListener) {
		super(activity, onClickListener);
	}

	public void showApproveJoinRequestDialog(Activity activity, Context context,
		String account, String phoneNumber, String mutualId, String regId, 
		String macAddress, SMSMessage smsMessage) {
			
		methodName = "showApproveJoinRequestDialog";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		String dialogMessage = "Approve add request from " +
			account + "\n[" + phoneNumber + "]";
	  	
		CommonDialog approveJoinRequestDialog = new CommonDialog(activity, onClickListener);
	
		approveJoinRequestDialog.setDialogMessage(dialogMessage);
		approveJoinRequestDialog.setDialogTitle("Join request approval");
		approveJoinRequestDialog.setPositiveButtonText("OK");
		approveJoinRequestDialog.setNegativeButtonText("Cancel");
		approveJoinRequestDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
		approveJoinRequestDialog.showDialog();
		approveJoinRequestDialog.setCancelable(false);

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}
}

