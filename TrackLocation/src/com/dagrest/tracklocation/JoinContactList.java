package com.dagrest.tracklocation;

import java.util.ArrayList;

import com.dagrest.tracklocation.datatype.BroadcastCommandEnum;
import com.dagrest.tracklocation.datatype.JoinRequestData;
import com.dagrest.tracklocation.datatype.JoinRequestStatusEnum;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.util.SparseArray;
import android.view.KeyEvent;
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
	ProgressDialog barProgressDialog;
	Handler updateBarHandler;
	ExpandableListView listView;
	private boolean toSendAddJoinRequest = false;
	
	public void launchBarDialog(View view) {
		        barProgressDialog = new ProgressDialog(JoinContactList.this);
		        barProgressDialog.setTitle("Fetching contacts");
		        barProgressDialog.setMessage("Please wait ...");
		        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
		        barProgressDialog.setProgress(0);
		        barProgressDialog.setMax(Controller.getContactsNumber(JoinContactList.this));
		        barProgressDialog.setCancelable(false);
		        //barProgressDialog.setIndeterminate(true);
		        barProgressDialog.show();
		        
		        new Thread(new Runnable() {
		            @Override
		            public void run() {
		                try {
		                	Controller.fetchContacts(JoinContactList.this, contactDetailsGroups, barProgressDialog);
		                	barProgressDialog.dismiss();

		                	Controller.broadcastMessage(JoinContactList.this, CommonConst.BROADCAST_JOIN, "fetchContacts", 
		        					BroadcastCommandEnum.fetch_contacts_completed.toString(), 
		        					"Completed");
		                } catch (Exception e) {
		                	System.out.println("Exception IMPORTANT: " + e.getMessage());
		                }
		            }
		        }).start();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.join_contact_list);
		
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
	    intentFilter.addAction(CommonConst.BROADCAST_JOIN);
	    broadcastReceiver = new BroadcastReceiver() 
	    {
	    	String phoneNumber = null;
	    	String contactName = null;
	    	String mutualId = null;
	    	
	    	@Override
    		public void onReceive(final Context context, Intent intent) {
	    		
		    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	    		
		    	    @Override
		    	    public void onClick(DialogInterface dialog, int which) {
		    	        switch (which){
		    	        case DialogInterface.BUTTON_POSITIVE:
		    				long res = DBLayer.addJoinRequest(phoneNumber, mutualId, JoinRequestStatusEnum.SENT);
			    			if(res != 1){
			    				// TODO: Notify that add to DB failed...
			    			}
			    			String registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
			    			if(registrationId != null && !registrationId.isEmpty()){
					        	// Send SMS with registration details: 
					        	// phoneNumber and registartionId (mutual ID - optional) 
					        	SmsManager smsManager = SmsManager.getDefault();
			                    String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
			                    String ownerGuid = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_OWNER_GUID);
								ArrayList<String> parts = smsManager.divideMessage(CommonConst.JOIN_FLAG_SMS + 
									CommonConst.DELIMITER_COMMA + registrationId + CommonConst.DELIMITER_COMMA +
									mutualId + CommonConst.DELIMITER_COMMA + phoneNumber + CommonConst.DELIMITER_COMMA + account + 
									CommonConst.DELIMITER_COMMA + ownerGuid);
								smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);    
								// Notify by toast that join request sent by SMS
								String msg = "Join request sent to " + contactName + " [" + phoneNumber + "] by SMS";
								Toast.makeText(JoinContactList.this, msg, Toast.LENGTH_SHORT).show();
			    			}
			    			finish();
		    	            break;

		    	        case DialogInterface.BUTTON_NEGATIVE:
		    	        	toSendAddJoinRequest = false;
		    	            break;
		    	        }
		    	    }
		    	};

		    	// TODO Auto-generated method stub
	    		LogManager.LogInfoMsg(className, "initBroadcastReceiver->onReceive", "WORK");
	    		
	    		Bundle bundle = intent.getExtras();
	    		String broadcastKeyJoinNumber = BroadcastCommandEnum.join_number.toString();
	    		String broadcastKeyFetchContactsCompleted = BroadcastCommandEnum.fetch_contacts_completed.toString();
	    		// ===========================================
	    		// broadcast key: join_number or any value 
	    		// ===========================================
	    		if(bundle != null && (bundle.containsKey(broadcastKeyJoinNumber) || bundle.containsKey(broadcastKeyFetchContactsCompleted))){
	    			
		    		String namePhoneNumber = bundle.getString(broadcastKeyJoinNumber);
		    		
		    		if(namePhoneNumber != null && !namePhoneNumber.isEmpty() && bundle.containsKey(broadcastKeyJoinNumber)){
		    			System.out.println("Join number: " + namePhoneNumber);
		    			
		    			String[] args = namePhoneNumber.split(CommonConst.DELIMITER_STRING);
		    			
		    			contactName = args[0];
		    			phoneNumber = args[1];
		    			
		    			// rend join SMS command
		    			String mutualId = Controller.generateUUID();
		    			long res = -1;
		    			JoinRequestData joinRequestData = DBLayer.getJoinRequest(phoneNumber);
		    			if( joinRequestData == null ) { 
		    				toSendAddJoinRequest = true;
		    			} else { // join request with <phoneNumber> already exists, check the status
		    				if( joinRequestData.getStatus().equals(JoinRequestStatusEnum.SENT.toString()) ) {
		    					// TODO: notify by dialog that join request already sent to <phoneNumber>
		    					// check if the following request should be sent again
		    					// DIALOG FUNC
		    					
		    					AlertDialog.Builder builder = new AlertDialog.Builder(JoinContactList.this);
		    					builder.setTitle("Title")
		    						.setMessage("Are you sure?")
		    					    .setNegativeButton("No", dialogClickListener)
		    						.setPositiveButton("Yes", dialogClickListener)
		    					    .show();
		    					
//		    		        	CommonDialog dialog = new CommonDialog();
//		    		        	dialog.setPositiveButtonText("Yes");
//		    		        	dialog.setNegativeButtonText("No");
//		    		        	dialog.setDialogMessage(contactName + " [" + phoneNumber + "] has been joined already.\n" + 
//		    		        		"Do you want to sent join request again?");
//		    		        	dialog.show(JoinContactList.this.getFragmentManager(), "Join request");
		    		        	
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
		    				res = DBLayer.addJoinRequest(phoneNumber, mutualId, JoinRequestStatusEnum.SENT);
			    			if(res != 1){
			    				// TODO: Notify that add to DB failed...
			    			}
			    			String registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
			    			if(registrationId != null && !registrationId.isEmpty()){
					        	// Send SMS with registration details: 
					        	// phoneNumber and registartionId (mutual ID - optional) 
					        	SmsManager smsManager = SmsManager.getDefault();
			                    String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
								ArrayList<String> parts = smsManager.divideMessage(CommonConst.JOIN_FLAG_SMS + 
									CommonConst.DELIMITER_COMMA + registrationId + CommonConst.DELIMITER_COMMA +
									mutualId + CommonConst.DELIMITER_COMMA + phoneNumber + CommonConst.DELIMITER_COMMA + account);
								smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);    
								// Notify by toast that join request sent by SMS
								String msg = "Join request sent to " + contactName + " [" + phoneNumber + "] by SMS";
								Toast.makeText(JoinContactList.this, msg, Toast.LENGTH_SHORT).show();
			    			}
			    			finish();
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
    }
}

	