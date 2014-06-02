package com.dagrest.tracklocation;

import com.dagrest.tracklocation.datatype.BroadcastCommandEnum;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;

public class JoinContactList extends Activity {
	
	private SparseArray<ContactDetails> contactDetailsGroups = new SparseArray<ContactDetails>();
	private EditText inputSearch;
	private ContactDeatilsExpandableListAdapter adapter;
	private BroadcastReceiver broadcastReceiver;
	private String className = this.getClass().getName();
	ProgressDialog barProgressDialog;
	Handler updateBarHandler;
	ExpandableListView listView;
	
	public void launchBarDialog(View view) {
		        barProgressDialog = new ProgressDialog(JoinContactList.this);
		        barProgressDialog.setTitle("Fetching contacts ...");
		        barProgressDialog.setMessage("Fetch in progress ...");
		        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
		        barProgressDialog.setProgress(0);
		        barProgressDialog.setMax(Controller.getContactsNumber(JoinContactList.this));
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
	    	@Override
    		public void onReceive(Context context, Intent intent) {
	    		
    			// TODO Auto-generated method stub
	    		LogManager.LogInfoMsg(className, "initBroadcastReceiver->onReceive", "WORK");
	    		
	    		Bundle bundle = intent.getExtras();
	    		String broadcastKeyJoinNumber = BroadcastCommandEnum.join_number.toString();
	    		String broadcastKeyFetchContactsCompleted = BroadcastCommandEnum.fetch_contacts_completed.toString();
	    		// ===========================================
	    		// broadcast key: join_number or any value 
	    		// ===========================================
	    		if(bundle != null && (bundle.containsKey(broadcastKeyJoinNumber) || bundle.containsKey(broadcastKeyFetchContactsCompleted))){
	    			
		    		String result = bundle.getString(broadcastKeyJoinNumber);
		    		
		    		if(result != null && !result.isEmpty() && bundle.containsKey(broadcastKeyJoinNumber)){
		    			System.out.println("Join number: " + result);
		    			// rend join SMS command
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
