package com.dagrest.tracklocation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class CommonDialog extends DialogFragment {
	
	AlertDialog.Builder builder;
	String dialogMessage = null;
	String positiveButtonText = null;
	String negativeButtonText = null;
	
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

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

    	// Use the Builder class for convenient dialog construction
        builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(dialogMessage)
               .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // FIRE ZE MISSILES!
                   }
               });
        	if( negativeButtonText != null ){
        		builder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        	}
        // Create the AlertDialog object and return it 
        return builder.create();
    }
}
