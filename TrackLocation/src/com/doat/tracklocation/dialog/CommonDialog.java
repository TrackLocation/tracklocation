package com.doat.tracklocation.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class CommonDialog {
	
	protected Activity activity;
	protected String title;
	protected String message;
	protected String positiveButtonName;
	protected String negativeButtonName;
	protected boolean cancelable;
	protected OnClickListener positiveButtonListener;
	protected OnClickListener negativeButtonListener;
	protected CharSequence[] itemsList;
	protected int checkedItem;
	protected OnClickListener choiseListener;
	
	public CommonDialog(Activity activity, String title, String message, 
			String positiveButtonName, String negativeButtonName, boolean cancelable,
			OnClickListener positiveButtonListener, OnClickListener negativeButtonListener) {
		super();
		
		this.activity = activity;
		this.title = title;
		this.message = message;
		this.positiveButtonName = positiveButtonName;
		this.negativeButtonName = negativeButtonName;
		this.cancelable = cancelable;
		this.positiveButtonListener = positiveButtonListener;
		this.negativeButtonListener = negativeButtonListener;
		showTwoButtons();
	}
	
	public CommonDialog(Activity activity, String title, String message, 
			String positiveButtonName, boolean cancelable,
			OnClickListener positiveButtonListener) {
		super();
		
		this.activity = activity;
		this.title = title;
		this.message = message;
		this.positiveButtonName = positiveButtonName;
		this.cancelable = cancelable;
		if(positiveButtonListener != null){
			this.positiveButtonListener = positiveButtonListener;
		} else {
			this.positiveButtonListener = defaultPositiveButtonListener;
		}
		showOneButton();
	}

	public CommonDialog(Activity activity, String title, 
			CharSequence[] itemsList, boolean cancelable,
			OnClickListener choiseListener) {
		super();
		
		this.activity = activity;
		this.title = title;
		this.itemsList = itemsList;
		this.cancelable = cancelable;
		this.choiseListener = choiseListener;
		showChoise();
	}

	protected void showTwoButtons(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title).setMessage(message).setCancelable(cancelable)
        	.setPositiveButton(positiveButtonName, positiveButtonListener)
        	.setNegativeButton(negativeButtonName, negativeButtonListener);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
	}

	protected void showOneButton(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title).setMessage(message).setCancelable(cancelable)
        	.setPositiveButton(positiveButtonName, positiveButtonListener);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
	}

	protected void showChoise(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title).setMessage(message).setCancelable(cancelable)
        	.setSingleChoiceItems(itemsList, checkedItem, choiseListener);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
	}

	OnClickListener defaultPositiveButtonListener = new OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
		}
	};
	
}
