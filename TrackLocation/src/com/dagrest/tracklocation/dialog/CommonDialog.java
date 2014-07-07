package com.dagrest.tracklocation.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

@SuppressLint("ValidFragment")
public class CommonDialog extends DialogFragment {
	
	private AlertDialog.Builder builder;
	private String dialogMessage = null;
	private String positiveButtonText = null;
	private String negativeButtonText = null;
	private IDialogOnClickAction onClickAction;
	private FragmentManager fm;
	
	public CommonDialog() {};
			
    public CommonDialog(Activity activity, IDialogOnClickAction onClickAction) {
		this.onClickAction = onClickAction;
		fm = activity.getFragmentManager();
	}

	public String getDialogMessage() {
		return dialogMessage;
	}

	public void setDialogMessage(String dialogMessage) {
		this.dialogMessage = dialogMessage;
	}

	public String getPositiveButtonText() {
		return positiveButtonText;
	}

	public void setPositiveButtonText(String positiveButtonText) {
		this.positiveButtonText = positiveButtonText;
	}

	public String getNegativeButtonText() {
		return negativeButtonText;
	}

	public void setNegativeButtonText(String negativeButtonText) {
		this.negativeButtonText = negativeButtonText;
	}

    public void showDialog() {
        this.show(fm, "tag");
    }

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

    	// Use the Builder class for convenient dialog construction
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("About");
        builder.setMessage(dialogMessage)
               .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   onClickAction.doOnPositiveButton();
                   }
               });
        	if( negativeButtonText != null ){
        		builder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   onClickAction.doOnNegativeButton();
                   }
               });
        	}
        // Create the AlertDialog object and return it 
        return builder.create();
    }
}
