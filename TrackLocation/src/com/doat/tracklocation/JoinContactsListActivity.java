package com.doat.tracklocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.doat.tracklocation.crypto.CryptoUtils;
import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.JoinRequestStatusEnum;
import com.doat.tracklocation.datatype.SentJoinRequestData;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.dialog.CommonDialog;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class JoinContactsListActivity extends FragmentActivity implements JoinContactsListFragment.OnContactsInteractionListener {
    private boolean isSearchResultView = false;
	private BroadcastReceiver broadcastReceiver;
	private String className = this.getClass().getName();	
	private boolean toSendAddJoinRequest = false;
	private AdView adView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	getActionBar().setDisplayShowHomeEnabled(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_activity_main);

        initBroadcastReceiver();
        adView = (AdView)this.findViewById(R.id.adJoinContacts);
	    AdRequest adRequest = new AdRequest.Builder().build();
	    if(adView != null){
	    	adView.loadAd(adRequest);
	    }
    }

    @Override
    public void onContactSelected(Uri contactUri) {
       Intent intent = new Intent(this, JoinContactDetailActivity.class);
       intent.setData(contactUri);
       startActivityForResult(intent, 2);       
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
    	if(requestCode==2){
			// Make sure the request was successful
	        if (resultCode == RESULT_OK) {	 
	        	if (data != null){
	        		String phoneNumber = data.getExtras().getString("JOIN_PHONE_NUMBER");
	        		String contactName = data.getExtras().getString("JOIN_CONTACT_NAME");
	        		Controller.broadcastMessage(JoinContactsListActivity.this, 
	        				BroadcastActionEnum.BROADCAST_JOIN.toString(), 
	        				"OnChildClick",
	        				null, 
	        				BroadcastKeyEnum.join_number.toString(), 
	        				contactName + CommonConst.DELIMITER_STRING + phoneNumber);
	        	}
	       }
		}
    }

    @Override
    public boolean onSearchRequested() {
        return !isSearchResultView && super.onSearchRequested();
    }

	@Override
	public void onSelectionCleared() {
		// TODO Auto-generated method stub	
	}
	
	private void initBroadcastReceiver()
    {
    	LogManager.LogFunctionCall(className, "initBroadcastReceiver");
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(BroadcastActionEnum.BROADCAST_JOIN.toString());
	    broadcastReceiver = new BroadcastReceiver() 
	    {
	    	String phoneNumber = null;
	    	String contactName = null;
	    	@Override
    		public void onReceive(final Context context, Intent intent) {
	    		
		    	// TODO Auto-generated method stub
	    		LogManager.LogInfoMsg(className, "initBroadcastReceiver->onReceive", "WORK");
	    		
	    		Bundle bundle = intent.getExtras();
	    		String broadcastKeyJoinNumber = BroadcastKeyEnum.join_number.toString();
	    		String broadcastKeyFetchContactsCompleted = BroadcastKeyEnum.fetch_contacts_completed.toString();
	    		String broadcastKeyResendJoinRequest = BroadcastKeyEnum.resend_join_request.toString();
	    		// ===========================================
	    		// broadcast key: join_number or any value 
	    		// ===========================================
	    		if(bundle != null && 
	    				(bundle.containsKey(broadcastKeyJoinNumber) || 
	    				 bundle.containsKey(broadcastKeyFetchContactsCompleted) || 
	    				 bundle.containsKey(broadcastKeyResendJoinRequest))){
	    			
		    		String namePhoneNumber = bundle.getString(broadcastKeyJoinNumber);
		    		
		    		if(namePhoneNumber != null && !namePhoneNumber.isEmpty() && bundle.containsKey(broadcastKeyJoinNumber)){
		    			
		    			String[] args = namePhoneNumber.split(CommonConst.DELIMITER_STRING);
		    			
		    			contactName = args[0];
		    			phoneNumber = args[1];
		    			
//		    			CommonDialog joinRequestDialog;
		    			SentJoinRequestData joinRequestData = DBLayer.getInstance().getSentJoinRequestByPhone(phoneNumber);
		    			if( joinRequestData == null ) {
		    				if (!toSendAddJoinRequest){
		    					String message = String.format("\nA request will be sent to %s (phone number %s)." +
		    						"\n\nDo you want to send it?\n", contactName, phoneNumber);
		    					//joinRequestDialog = joinRequestDialog(contactName, phoneNumber, message, onClickListener);
		    					Resources res = context.getResources();
		    					if(res == null){
		    						return;
		    					}
		    					new CommonDialog(JoinContactsListActivity.this, 
		    						"Add Contact", 
		    						message, 
		    						"Yes", 
		    						"No",
		    						false, // cancelable
		    						onPositiveClickListener,
		    						onPositiveClickListener    						
		    					);
		    				}
		    			} else { // join request with <phoneNumber> already exists, check the status
		    				if( joinRequestData.getStatus().equals(JoinRequestStatusEnum.SENT.toString()) ) {
		    					// Notify by dialog that join request already sent to <phoneNumber>
		    					// check if the following request should be sent again
		    					String message = String.format("\nA request has been already sent to %s, with " +
		    						"phone number %s.\n\nDo you want to send it again?\n", contactName, phoneNumber);
		    					//joinRequestDialog = joinRequestDialog(contactName, phoneNumber, message, onClickListener);
		    					new CommonDialog(JoinContactsListActivity.this, 
			    						"Add Contact", 
			    						message, 
			    						"Yes", 
			    						"No",
			    						false, // cancelable
			    						onPositiveClickListener,
			    						onNegativeClickListener    						
			    					);
		    					//toSendAddJoinRequest = joinRequestDialog.isSelectionStatus();
		    				}
		    			}
		    			if(toSendAddJoinRequest == true){
		    				sendJoinRequest(context, contactName, phoneNumber);
		    				toSendAddJoinRequest = false;
		    			}
		    		} else if(bundle.containsKey(BroadcastKeyEnum.resend_join_request.toString())){
		    			sendJoinRequest(context, contactName, phoneNumber);
		    			toSendAddJoinRequest = false;
		    		}
	    		}
    		}
	    };
	    registerReceiver(broadcastReceiver, intentFilter);
	    LogManager.LogFunctionExit(className, "initBroadcastReceiver");
    }
	
    public void sendJoinRequest(Context context, String contactName, String phoneNumber){    	
		String joinValue = "";
		try {
			joinValue = buildJoinRequestData(context, contactName, phoneNumber);
		} catch (UnsupportedEncodingException e) {
			Log.e(CommonConst.LOG_TAG, e.getMessage());			
		}
		if(!joinValue.isEmpty()){
			SmsManager smsManager = SmsManager.getDefault();
			ArrayList<String> parts = smsManager.divideMessage(joinValue);
			smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);    
			// Notify by toast that join request sent by SMS
			String msg = "Add request sent to " + contactName + " [" + phoneNumber + "] by SMS";
			Toast.makeText(JoinContactsListActivity.this, msg, Toast.LENGTH_LONG).show();						
		}
		finish();
    }
    
    private String buildJoinRequestData(Context context, String contactName, String phoneNumber) throws UnsupportedEncodingException{    	
    	String mutualId = Controller.generateUUID();
    	long res = DBLayer.getInstance().addSentJoinRequest(phoneNumber, mutualId, JoinRequestStatusEnum.SENT);
		if(res != 1){
			return "";
		}
		String registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
		if(registrationId != null && !registrationId.isEmpty()){
            String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
            String macAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
            String ownerPhoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
			return CommonConst.JOIN_SMS_PREFIX + CryptoUtils.encodeBase64(CommonConst.JOIN_FLAG_SMS + 
				CommonConst.DELIMITER_COMMA + registrationId + CommonConst.DELIMITER_COMMA +
				mutualId + CommonConst.DELIMITER_COMMA + ownerPhoneNumber + CommonConst.DELIMITER_COMMA + account +
				CommonConst.DELIMITER_COMMA + macAddress);
		}
		return "";   	
    }
    
//	private CommonDialog joinRequestDialog(String contactName, String phoneNumber, String message, ICommonDialogOnClickListener onClickListener) {
//    	String dialogMessage = String.format(message, contactName, phoneNumber) ;
//    	
//		CommonDialog aboutDialog = new CommonDialog(this, onClickListener);
//		aboutDialog.setDialogMessage(dialogMessage);
//		aboutDialog.setDialogTitle("Add Contact");
//		aboutDialog.setPositiveButtonText("Yes");
//		aboutDialog.setNegativeButtonText("No");
//		aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
//		aboutDialog.showDialog();
//		aboutDialog.setCancelable(true);
//		return aboutDialog;
//    }
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(broadcastReceiver);
	    if(adView != null){
	    	adView.destroy();
	    }
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
	    if(adView != null){
	    	adView.pause();
	    }
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	    if(adView != null){
	    	adView.resume();
	    }
	}
    
//    ICommonDialogOnClickListener onClickListener = new ICommonDialogOnClickListener(){
//
//		@Override
//		public void doOnPositiveButton(Object data) {
//			toSendAddJoinRequest = true;
//        	Controller.broadcastMessage(JoinContactsListActivity.this, 
//    			BroadcastActionEnum.BROADCAST_JOIN.toString(), 
//    			"fetchContacts",
//    			null, 
//				BroadcastKeyEnum.resend_join_request.toString(), 
//				"Resend");	
//		}
//
//		@Override
//		public void doOnNegativeButton(Object data) {
//			toSendAddJoinRequest = false;
//		}
//
//		@Override
//		public void doOnChooseItem(int which) {
//			// TODO Auto-generated method stub
//			
//		}
//		
//	};
	OnClickListener onPositiveClickListener = new OnClickListener(){
		@Override
		public void onClick(
				DialogInterface dialog,
				int which) {
			toSendAddJoinRequest = true;
        	Controller.broadcastMessage(JoinContactsListActivity.this, 
    			BroadcastActionEnum.BROADCAST_JOIN.toString(), 
    			"fetchContacts",
    			null, 
				BroadcastKeyEnum.resend_join_request.toString(), 
				"Resend"
			);	
		}		    						
	};

	OnClickListener onNegativeClickListener = new OnClickListener(){				
		@Override
		public void onClick(
				DialogInterface dialog,
				int which) {
			toSendAddJoinRequest = false;
		}		    						
	};

}
