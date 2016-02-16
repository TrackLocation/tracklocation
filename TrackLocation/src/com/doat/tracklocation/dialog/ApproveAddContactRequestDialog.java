package com.doat.tracklocation.dialog;

import com.doat.tracklocation.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;

public class ApproveAddContactRequestDialog {
	
	public ApproveAddContactRequestDialog(Context context, Activity activity, String userName, String phoneNumber,
			OnClickListener positiveButtonListener, OnClickListener negativeButtonListener) {
		super();
		create(context, activity, userName, phoneNumber, positiveButtonListener, negativeButtonListener);
	}
	
	public void create(Context context, Activity activity, String userName, String phoneNumber, 
			OnClickListener positiveButtonListener,
			OnClickListener negativeButtonListener){
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    	Resources res = context.getResources();
    	builder.setTitle(R.string.approve_add_contact_dialog_title);
    	String dialogMessage = 
    		String.format(res.getString(R.string.approve_add_contact_dialog_message), userName, phoneNumber);
        builder.setMessage(dialogMessage)
               .setPositiveButton(R.string.approve_add_contact_dialog_ok, positiveButtonListener)
               .setNegativeButton(R.string.approve_add_contact_dialog_cancel, negativeButtonListener);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
	}
}
