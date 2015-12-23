package com.doat.tracklocation.dialog;

import com.doat.tracklocation.utils.CommonConst;

import android.app.Activity;
import android.content.Context;

public class InfoDialog extends CommonDialog {

	public InfoDialog(Activity activity, Context context,
			String title, String infoMessage,
			ICommonDialogOnClickListener onClickListener) {
		super(activity, onClickListener);
		showInfoDialog(activity, context, title, infoMessage);
	}

	private void showInfoDialog(Activity activity, Context context,
			String title, String infoMessage){
		setDialogMessage(infoMessage);
		setDialogTitle(title);
		setPositiveButtonText("OK");
		setStyle(CommonConst.STYLE_NORMAL, 0);
		showDialog();
		setCancelable(true);
	}
	
}
