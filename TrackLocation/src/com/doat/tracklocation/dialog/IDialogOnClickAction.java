package com.doat.tracklocation.dialog;

import android.app.Activity;
import android.content.Context;

public interface IDialogOnClickAction {
	public void doOnPositiveButton();
	public void doOnNegativeButton();
	public void doOnChooseItem(int which);
	public void setActivity(Activity activity);
	public void setContext(Context context);
	public void setParams(Object[]...objects);
}

