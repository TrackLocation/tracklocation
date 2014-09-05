package com.dagrest.tracklocation.service;

import java.util.List;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.MainActivity;
import com.dagrest.tracklocation.datatype.BroadcastActionEnum;
import com.dagrest.tracklocation.datatype.BroadcastKeyEnum;
import com.dagrest.tracklocation.datatype.MessageDataContactDetails;
import com.dagrest.tracklocation.datatype.NotificationBroadcastData;
import com.dagrest.tracklocation.grid.ContactDataGridView;
import com.dagrest.tracklocation.utils.CommonConst;
import com.google.gson.Gson;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {
    private final String DEBUG_TAG = getClass().getSimpleName().toString();
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private Context context;
    private Intent intent;
    
	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
        this.intent = intent;

		String action = intent.getAction();
		
		if (action.equals(ACTION_SMS_RECEIVED)) {

			String address, str = "";
			int contactId = -1;

			SmsMessage[] msgs = getMessagesFromIntent(intent);
			if (msgs != null) {
				for (int i = 0; i < msgs.length; i++) {
					address = msgs[i].getOriginatingAddress();
//					contactId = ContactsUtils.getContactId(context, address,
//							"address");
					str += msgs[i].getMessageBody().toString();
					str += "\n";
				}
			}

//			if (contactId != -1) {
//				showNotification(contactId, str);
//			}
			

			if(str.contains(CommonConst.JOIN_FLAG_SMS)){
//				// ---send a broadcast intent to update the SMS received in the
//				// activity---
//				Intent broadcastIntent = new Intent();
//				broadcastIntent.setAction("SMS_RECEIVED_ACTION");
//				broadcastIntent.putExtra("sms", str);
//				context.sendBroadcast(broadcastIntent);
				Intent intentMainActivity = getIntent(context, MainActivity.class);
				Log.i(CommonConst.LOG_TAG, "intentMainActivity = " + intentMainActivity);
				if(MainActivity.isTrackLocationRunning == false){
		        	Intent mainActivity = new Intent(context, MainActivity.class);
		        	mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    		context.startActivity(mainActivity);
				} else {					
					Gson gson = new Gson();
					NotificationBroadcastData notificationBroadcastData = new NotificationBroadcastData();
					notificationBroadcastData.setMessage("message");
					notificationBroadcastData.setKey(BroadcastKeyEnum.join_sms.toString());
					notificationBroadcastData.setValue("value");
					String jsonNotificationBroadcastData = gson.toJson(notificationBroadcastData);
					
					MessageDataContactDetails mdcd = null;
			
					// Broadcast corresponding message
					Controller.broadcastMessage(context, 
						BroadcastActionEnum.BROADCAST_MESSAGE.toString(), 
						"SMSReceiver",
						jsonNotificationBroadcastData, 
						BroadcastKeyEnum.join_sms.toString(), 
						"value");
					
					
//		    		//Intent i = new Intent(context, MainActivity.class);
////					intentMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////		    		intentMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//					intentMainActivity.setAction(Intent.ACTION_MAIN);
//					intentMainActivity.addCategory(Intent.CATEGORY_LAUNCHER);
//					context.startActivity(intentMainActivity);
				}
			}
		}
	}
	
	private static Intent getIntent(Context context, Class<?> cls) {
	    Intent intent = new Intent(context, cls);
	    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    return intent;
	}
	
	  public boolean isRunning(Context ctx) {
	        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
	        List<RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

	        for (RunningTaskInfo task : tasks) {
	            if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName())) 
	                return true;                                  
	        }

	        return false;
	    }

	
    public static SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];

        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }
}
