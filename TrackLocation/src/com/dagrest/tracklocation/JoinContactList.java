package com.dagrest.tracklocation;

import java.util.ArrayList;

import com.dagrest.tracklocation.datatype.BroadcastActionEnum;
import com.dagrest.tracklocation.datatype.BroadcastKeyEnum;
import com.dagrest.tracklocation.datatype.JoinRequestStatusEnum;
import com.dagrest.tracklocation.datatype.SentJoinRequestData;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.dialog.CommonDialog;
import com.dagrest.tracklocation.dialog.IDialogOnClickAction;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

public class JoinContactList extends Activity {
	
	private SparseArray<ContactDetails> contactDetailsGroups = new SparseArray<ContactDetails>();
	private EditText inputSearch;
	private ContactDeatilsExpandableListAdapter adapter;
	private BroadcastReceiver broadcastReceiver;
	private String className = this.getClass().getName();
	private String methodName;
	ProgressDialog barProgressDialog;
	Handler updateBarHandler;
	ExpandableListView listView;
	private boolean toSendAddJoinRequest = false;
	
	public void launchBarDialog(View view) {
		methodName = "launchBarDialog";
		barProgressDialog = new ProgressDialog(JoinContactList.this);
		barProgressDialog.setTitle("Fetching contacts");
		barProgressDialog.setMessage("Please wait ...");
		barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		barProgressDialog.setProgress(0);
		barProgressDialog.setMax(Controller.getContactsNumber(JoinContactList.this));
		barProgressDialog.setCancelable(false);
		//barProgressDialog.setIndeterminate(true);
		barProgressDialog.show();
		
		new Thread(new Runnable() {
		    @Override
		    public void run() {
		    	String methodName = "launchBarDialog->Thread->Runnable->run";
		        try {
		        	Controller.fetchContacts(JoinContactList.this, contactDetailsGroups, barProgressDialog);
		        	barProgressDialog.dismiss();
		
		        	Controller.broadcastMessage(JoinContactList.this, 
		        			BroadcastActionEnum.BROADCAST_JOIN.toString(), 
		        			"fetchContacts",
		        			null,
							BroadcastKeyEnum.fetch_contacts_completed.toString(), 
							"Completed");
		        } catch (Exception e) {
		    		LogManager.LogException(e, className, methodName);
		    		Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
		        }
		    }
		}).start();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.join_contact_list);
		
		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);

		updateBarHandler = new Handler();

		initBroadcastReceiver();
		
		listView = (ExpandableListView) findViewById(R.id.listView);
        
		//	    Controller.fetchContacts(JoinContactList.this, contactDetailsGroups, barProgressDialog);
		//		adapter = new ContactDeatilsExpandableListAdapter(this, contactDetailsGroups);
		//	    listView.setAdapter(adapter);

	    launchBarDialog(listView);
        
	    inputSearch = (EditText) findViewById(R.id.find_join_contact);
        // Enabling Search Filter
        inputSearch.addTextChangedListener(new TextWatcher() {
             
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
            	JoinContactList.this.adapter.filterData(cs.toString());

            }
             
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                    int arg3) {
                // TODO Auto-generated method stub
                 
            }
             
            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub                          
            }
        });
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
	    	String mutualId = null;
	    	
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
		    			
		    			// send join SMS command
		    			long res = -1;
		    			SentJoinRequestData joinRequestData = DBLayer.getInstance().getSentJoinRequestByPhone(phoneNumber);
		    			if( joinRequestData == null ) { 
		    				toSendAddJoinRequest = true;
		    			} else { // join request with <phoneNumber> already exists, check the status
		    				if( joinRequestData.getStatus().equals(JoinRequestStatusEnum.SENT.toString()) ) {
		    					// Notify by dialog that join request already sent to <phoneNumber>
		    					// check if the following request should be sent again
		    					joinRequestDialog(contactName, phoneNumber);
		    		        	
		    				} else if( joinRequestData.getStatus().equals(JoinRequestStatusEnum.ACCEPTED.toString()) ) {
		    					// TODO: notify by dialog that join request already sent to <phoneNumber> and accepted
		    					// check if the following request should be sent again
		    					// DIALOG FUNC
		    					
		    					// in case if request should be sent again
			    				toSendAddJoinRequest = true;
		    				} else if( joinRequestData.getStatus().equals(JoinRequestStatusEnum.DECLINED.toString()) ) {
		    					// TODO: notify by dialog that join request already sent to <phoneNumber> but declined
		    					// check if the following request should be sent again
		    					// DIALOG FUNC
		    					
		    					// in case if request should be sent again
			    				toSendAddJoinRequest = true;
		    				}
		    			}
		    			if(toSendAddJoinRequest == true){
		    				sendJoinRequest(context, contactName, phoneNumber);
		    			}
		    			
		    			// TODO: fix the following code - use STATUS instead of Mutual_ID
//		    			if(joinRequestData.getMutualId().equals(CommonConst.JOIN_COMPLETED)){
//		    				// TODO: notify that this contact has been joined
//		    				String msg = contactName + " [" + phoneNumber + "] has been joined already ";
//		    				Toast.makeText(JoinContactList.this, msg, Toast.LENGTH_SHORT).show();
//		    				finish();
//		    			}

		    		} else if(bundle.containsKey(broadcastKeyFetchContactsCompleted)){
		    			// update expandable list of the device contacts
		    	        adapter = new ContactDeatilsExpandableListAdapter(JoinContactList.this, contactDetailsGroups);
		    		    listView.setAdapter(adapter);
		    		} else if(bundle.containsKey(BroadcastKeyEnum.resend_join_request.toString())){
		    			sendJoinRequest(context, contactName, phoneNumber);
		    		}
	    		}
    		}
	    };
	    registerReceiver(broadcastReceiver, intentFilter);
	    LogManager.LogFunctionExit(className, "initBroadcastReceiver");
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	unregisterReceiver(broadcastReceiver);
    	methodName = "onDestroy";
		LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_DESTROY] {" + className + "} -> " + methodName);
    }
    
    public void sendJoinRequest(Context context, String contactName, String phoneNumber){
    	String mutualId = Controller.generateUUID();
    	long res = DBLayer.getInstance().addSentJoinRequest(phoneNumber, mutualId, JoinRequestStatusEnum.SENT);
		if(res != 1){
			// TODO: Notify that add to DB failed...
		}
		String registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
		if(registrationId != null && !registrationId.isEmpty()){
        	// Send SMS with registration details: 
        	// phoneNumber and registartionId (mutual ID - optional) 
        	SmsManager smsManager = SmsManager.getDefault();
            String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
            String macAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
            String ownerPhoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
			ArrayList<String> parts = smsManager.divideMessage(CommonConst.JOIN_FLAG_SMS + 
				CommonConst.DELIMITER_COMMA + registrationId + CommonConst.DELIMITER_COMMA +
				mutualId + CommonConst.DELIMITER_COMMA + ownerPhoneNumber + CommonConst.DELIMITER_COMMA + account +
				CommonConst.DELIMITER_COMMA + macAddress);
			smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);    
			// Notify by toast that join request sent by SMS
			String msg = "Join request sent to " + contactName + " [" + phoneNumber + "] by SMS";
			Toast.makeText(JoinContactList.this, msg, Toast.LENGTH_LONG).show();
		}
		finish();
    }
    
	IDialogOnClickAction joinRequest = new IDialogOnClickAction() {
		@Override
		public void doOnPositiveButton() {
			toSendAddJoinRequest = true;
        	Controller.broadcastMessage(JoinContactList.this, 
        			BroadcastActionEnum.BROADCAST_JOIN.toString(), 
        			"fetchContacts",
        			null, 
					BroadcastKeyEnum.resend_join_request.toString(), 
					"Resend");
			//sendJoinRequest(context, contactName, phoneNumber, mutualId);
		}
		@Override
		public void doOnNegativeButton() {
			toSendAddJoinRequest = false;
		}
		@Override
		public void setActivity(Activity activity) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void setContext(Context context) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void setParams(Object[]... objects) {
			// TODO Auto-generated method stub
		}
		@Override
		public void doOnChooseItem(int which) {
			// TODO Auto-generated method stub
			
		}
	};
	
    private void joinRequestDialog(String contactName, String phoneNumber) {
    	String dialogMessage = "\nJoin request has been already sent to " + contactName + 
    		", with phone number " + phoneNumber + ".\n\nDo you want to send it again?\n";
    	
		CommonDialog aboutDialog = new CommonDialog(this, joinRequest);
		aboutDialog.setDialogMessage(dialogMessage);
		aboutDialog.setDialogTitle("Join contact");
		aboutDialog.setPositiveButtonText("Yes");
		aboutDialog.setNegativeButtonText("No");
		aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
		aboutDialog.showDialog();
		aboutDialog.setCancelable(true);
    }
}

	