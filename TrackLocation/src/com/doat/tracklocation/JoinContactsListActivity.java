package com.doat.tracklocation;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.doat.tracklocation.crypto.CryptoUtils;
import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.JoinRequestStatusEnum;
import com.doat.tracklocation.datatype.SentJoinRequestData;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.dialog.CommonDialogNew;
import com.doat.tracklocation.dialog.ICommonDialogNewOnClickListener;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;


public class JoinContactsListActivity extends FragmentActivity implements JoinContactsListFragment.OnContactsInteractionListener {
    private boolean isSearchResultView = false;
	private BroadcastReceiver broadcastReceiver;
	private String className = this.getClass().getName();	
	private boolean toSendAddJoinRequest = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	getActionBar().setDisplayShowHomeEnabled(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_activity_main);

        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            String searchQuery = getIntent().getStringExtra(SearchManager.QUERY);
            JoinContactsListFragment mContactsListFragment = (JoinContactsListFragment)
                    getSupportFragmentManager().findFragmentById(R.id.contact_list);

            isSearchResultView = true;
            mContactsListFragment.setSearchQuery(searchQuery);

            // Set special title for search results
            String title = getString(R.string.contacts_list_search_results_title, searchQuery);
            setTitle(title);
        }
        initBroadcastReceiver();
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
        // Don't allow another search if this activity instance is already showing
        // search results. Only used pre-HC.
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
//		    			System.out.println("Join number: " + namePhoneNumber);
		    			
		    			String[] args = namePhoneNumber.split(CommonConst.DELIMITER_STRING);
		    			
		    			contactName = args[0];
		    			phoneNumber = args[1];
		    			
		    			CommonDialogNew joinRequestDialog;
		    			SentJoinRequestData joinRequestData = DBLayer.getInstance().getSentJoinRequestByPhone(phoneNumber);
		    			if( joinRequestData == null ) {
		    				if (!toSendAddJoinRequest){
		    					String message = "\nJoin request will be sent to %s ( phone number %s) .\n\nDo you want to send it?\n";
		    					joinRequestDialog = joinRequestDialog(contactName, phoneNumber, message, onClickListener);
		    				}
		    			} else { // join request with <phoneNumber> already exists, check the status
		    				if( joinRequestData.getStatus().equals(JoinRequestStatusEnum.SENT.toString()) ) {
		    					// Notify by dialog that join request already sent to <phoneNumber>
		    					// check if the following request should be sent again
		    					String message = "\nJoin request has been already sent to %s, with phone number %s.\n\nDo you want to send it again?\n";
		    					joinRequestDialog = joinRequestDialog(contactName, phoneNumber, message, onClickListener);
		    					toSendAddJoinRequest = joinRequestDialog.isSelectionStatus();
		    				} else if( toSendAddJoinRequest ) {
//		    					// TODO: notify by dialog that join request already sent to <phoneNumber> and accepted
//		    					// check if the following request should be sent again
//		    					// DIALOG FUNC
//		    					
//		    					// in case if request should be sent again
//			    				toSendAddJoinRequest = true;
		    				} else if( !toSendAddJoinRequest ) {
//		    					// TODO: notify by dialog that join request already sent to <phoneNumber> but declined
//		    					// check if the following request should be sent again
//		    					// DIALOG FUNC
//		    					
//		    					// in case if request should be sent again
//			    				toSendAddJoinRequest = true;
		    				}
		    			}
		    			if(toSendAddJoinRequest == true){
		    				sendJoinRequest(context, contactName, phoneNumber);
		    				toSendAddJoinRequest = false;
		    			}
		    			
		    			// TODO: fix the following code - use STATUS instead of Mutual_ID
//		    			if(joinRequestData.getMutualId().equals(CommonConst.JOIN_COMPLETED)){
//		    				// TODO: notify that this contact has been joined
//		    				String msg = contactName + " [" + phoneNumber + "] has been joined already ";
//		    				Toast.makeText(JoinContactList.this, msg, Toast.LENGTH_SHORT).show();
//		    				finish();
//		    			}

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
			String msg = "Join request sent to " + contactName + " [" + phoneNumber + "] by SMS";
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
    
	private CommonDialogNew joinRequestDialog(String contactName, String phoneNumber, String message, ICommonDialogNewOnClickListener onClickListener) {
    	String dialogMessage = String.format(message, contactName, phoneNumber) ;
    	
		CommonDialogNew aboutDialog = new CommonDialogNew(this, onClickListener);
		aboutDialog.setDialogMessage(dialogMessage);
		aboutDialog.setDialogTitle("Join contact");
		aboutDialog.setPositiveButtonText("Yes");
		aboutDialog.setNegativeButtonText("No");
		aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
		aboutDialog.showDialog();
		aboutDialog.setCancelable(true);
		return aboutDialog;
    }
	
	protected void onDestroy() {
		unregisterReceiver(broadcastReceiver);
		super.onDestroy();
	}
    
    ICommonDialogNewOnClickListener onClickListener = new ICommonDialogNewOnClickListener(){

		@Override
		public void doOnPositiveButton(Object data) {
			toSendAddJoinRequest = true;
        	Controller.broadcastMessage(JoinContactsListActivity.this, 
    			BroadcastActionEnum.BROADCAST_JOIN.toString(), 
    			"fetchContacts",
    			null, 
				BroadcastKeyEnum.resend_join_request.toString(), 
				"Resend");	
		}

		@Override
		public void doOnNegativeButton(Object data) {
			toSendAddJoinRequest = false;
		}

		@Override
		public void doOnChooseItem(int which) {
			// TODO Auto-generated method stub
			
		}
		
	};
}
