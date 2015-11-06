package com.doat.tracklocation.mail;

import java.util.List;

import com.doat.tracklocation.utils.CommonConst;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class SendMail extends AsyncTask<Object, Object, Object> {
	private ProgressDialog statusDialog;
	private Activity parentActivity;

//	String fromEmail = null;
//	String fromPassword = null;
//	ArrayList<String> toEmailList = new ArrayList<String>();
//	toEmailList.add("dagrest@gmail.com");
//	String emailSubject = "RegId";
//	String emailBody = "Registration ID:\n" + Controller.getRegistrationId(getApplicationContext());
//	new SendMail(ContactConfiguration.this).execute(fromEmail,
//		fromPassword, toEmailList, emailSubject, emailBody);

	public SendMail(Activity activity) {
		parentActivity = activity;
	}

	protected void onPreExecute() {
		statusDialog = new ProgressDialog(parentActivity);
		statusDialog.setMessage("Getting ready...");
		statusDialog.setIndeterminate(false);
		statusDialog.setCancelable(false);
		statusDialog.show();
	}

	@Override
	protected String doInBackground(Object... args) {
		try {
			Log.i(CommonConst.LOG_TAG, "Preparing to send email");
			publishProgress("Processing input...");
			GMail androidEmail = new GMail(args[0].toString(),
				args[1].toString(), (List) args[2], args[3].toString(),
				args[4].toString());
			publishProgress("Preparing mail message...");
			androidEmail.createEmailMessage();
			publishProgress("Sending email...");
			androidEmail.sendEmail();
			publishProgress("Email sent.");
			Log.i(CommonConst.LOG_TAG, "Mail successfully sent.");
		} catch (Exception e) {
			publishProgress(e.getMessage());
			Log.e(CommonConst.LOG_TAG, e.getMessage(), e);
		}
		return null;
	}

	@Override
	public void onPostExecute(Object result) {
		statusDialog.dismiss();
	}
}
