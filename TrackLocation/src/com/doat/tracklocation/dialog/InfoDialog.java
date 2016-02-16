package com.doat.tracklocation.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;

public class InfoDialog extends CommonDialog {
	public InfoDialog(Activity activity, Context context,
			String title, String infoMessage,
			OnClickListener onClickListener) {
		super(activity, title, infoMessage, "OK", true, onClickListener);
	}
}
