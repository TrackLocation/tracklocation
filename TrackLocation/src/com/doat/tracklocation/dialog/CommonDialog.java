package com.doat.tracklocation.dialog;

import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class CommonDialog extends DialogFragment {
	
	protected AlertDialog.Builder builder;
	protected String dialogMessage = null;
	protected String dialogTitle = null;
	protected String positiveButtonText = null;
	protected String negativeButtonText = null;
	protected ICommonDialogOnClickListener onClickListener;
	protected ICommonDialogOnClickListener onClickListenerDefault;
	protected FragmentManager fm;
	protected String[] itemsList;
	protected String className;    
    protected String methodName;
    protected String logMessage;
    protected Object data;
    protected boolean selectionStatus; // positive or negative button chosen

	public CommonDialog(Activity activity, ICommonDialogOnClickListener onClickListener) {
		
		className = this.getClass().getName();
		methodName = "CommonDialogNew";
		selectionStatus = false; // by default negative button chosen
		
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		if(onClickListener != null){
			this.onClickListener = onClickListener;
		} else {
			this.onClickListener = getDialogOnClickActionDefault();
		}
		fm = activity.getFragmentManager();
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

    public String getDialogMessage() {
		return dialogMessage;
	}

	public void setDialogMessage(String dialogMessage) {
		this.dialogMessage = dialogMessage;
	}

	public String getDialogTitle() {
		return dialogTitle;
	}

	public void setDialogTitle(String dialogTitle) {
		this.dialogTitle = dialogTitle;
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

    public void closeDialog() {
        this.closeDialog();
    }

    public boolean isSelectionStatus() {
		return selectionStatus;
	}

	public String[] getItemsList() {
		return itemsList;
	}

	public void setItemsList(String[] itemsList) {
		this.itemsList = itemsList;
	}

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		methodName = "onCreateDialog";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		// Use the Builder class for convenient dialog construction
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(dialogTitle);
        builder.setMessage(dialogMessage)
               .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   onClickListener.doOnPositiveButton(data);
                   }
               });
        
    	if( negativeButtonText != null ){
    		builder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
            	   onClickListener.doOnNegativeButton(data);
               }
           });
    	}
        
    	if( itemsList != null ){
	        builder.setItems(itemsList, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					onClickListener.doOnChooseItem(which);
				}
			});
    	}
        	
    	LogManager.LogFunctionExit(className, methodName);
    	Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
        // Create the AlertDialog object and return it 
        return builder.create();
    }

    private ICommonDialogOnClickListener getDialogOnClickActionDefault(){
    	onClickListenerDefault = new ICommonDialogOnClickListener() {
			@Override
			public void doOnChooseItem(int which) {
				// TODO Auto-generated method stub
			}
			@Override
			public void doOnPositiveButton(Object data) {
				selectionStatus = true;
			}
			@Override
			public void doOnNegativeButton(Object data) {
				selectionStatus = false;
			}
		};
		return onClickListenerDefault;
    }

}
