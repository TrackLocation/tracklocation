package com.dagrest.tracklocation.dialog;

import android.app.Activity;
import android.content.Context;

public interface IDialogOnClickAction {
	public void doOnPositiveButton();
	public void doOnNegativeButton();
	public void setActivity(Activity activity);
	public void setContext(Context context);
	public void setParams(Object[]...objects);
}

