package com.doat.tracklocation;

import com.doat.tracklocation.datatype.ContactData;
import com.doat.tracklocation.utils.CommonConst;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

public class FetchDeviceContacts extends AsyncTask<Object, Object, SparseArray<ContactData>> {

	@Override
	protected SparseArray<ContactData> doInBackground(Object... params) {
		return null;
	}
/*	private ProgressDialog statusDialog;
	private Activity parentActivity;
	private Context context;
	SparseArray<ContactDetails> contactDetailsGroups = null;

	public FetchDeviceContacts(Activity activity, Context context) {
		parentActivity = activity;
		this.context = context;
	}

	protected void onPreExecute() {
		statusDialog = new ProgressDialog(parentActivity);
		statusDialog.setMessage("Getting ready...");
		statusDialog.setIndeterminate(false);
		statusDialog.setCancelable(false);
		statusDialog.show();
	}

	@Override
	protected SparseArray<ContactDetails> doInBackground(Object... args) {
		try {
			Log.i(CommonConst.LOG_TAG, "Preparing to send email");
			publishProgress("Processing input...");
			contactDetailsGroups = Controller.fetchContacts(context);
			publishProgress("Preparing mail message...");
			publishProgress("Sending email...");
			publishProgress("Email sent.");
			Log.i(CommonConst.LOG_TAG, "Mail successfully sent.");
		} catch (Exception e) {
			publishProgress(e.getMessage());
			Log.e(CommonConst.LOG_TAG, e.getMessage(), e);
		}
		return contactDetailsGroups;
	}

	@Override
	public void onPostExecute(SparseArray<ContactDetails> result) {
		statusDialog.dismiss();
	}

	public SparseArray<ContactDetails> getContactDetailsGroups() {
		return contactDetailsGroups;
	}
	*/
}
