package com.dagrest.tracklocation;

import java.util.List;

import com.dagrest.tracklocation.datatype.BroadcastCommandEnum;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.widget.EditText;
import android.widget.ExpandableListView;

public class JoinContactList extends Activity {
	
	private SparseArray<ContactDetails> contactDetailsGroups = new SparseArray<ContactDetails>();
	private EditText inputSearch;
	private ContactDeatilsExpandableListAdapter adapter;
	private ProgressDialog statusDialog;
	private Context context;
	private BroadcastReceiver broadcastReceiver;
	private String className = this.getClass().getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.join_contact_list);
		
		initBroadcastReceiver();
		
		contactDetailsGroups = Controller.fetchContacts(JoinContactList.this);
		
	    ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
	    adapter = new ContactDeatilsExpandableListAdapter(this, contactDetailsGroups);
	    listView.setAdapter(adapter);

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

//	protected void onPreExecute() {
//		statusDialog = new ProgressDialog(JoinContactList.this);
//		statusDialog.setMessage("Getting ready...");
//		statusDialog.setIndeterminate(false);
//		statusDialog.setCancelable(false);
//		statusDialog.show();
//	}
//
//	public void onPostExecute() {
//		statusDialog.dismiss();
//	}

    private void showProgressDialog(ProgressDialog d) {
        new AsyncTask<ProgressDialog, Void, String>() {
        	
        	private ProgressDialog statusDlg;
        	
            @Override
            protected String doInBackground(ProgressDialog... params) {
        		try {
         			//publishProgress("Processing input...");
        			//publishProgress("Preparing mail message...");
        			//publishProgress("Sending email...");
        			//publishProgress("Email sent.");
        		} catch (Exception e) {
        			//publishProgress(e.getMessage());
        		}
        		return null;
            }

            protected void onPreExecute() {
            	statusDlg = new ProgressDialog(context);
            	statusDlg.setMessage("Getting ready...");
            	statusDlg.setIndeterminate(false);
            	statusDlg.setCancelable(false);
            	statusDlg.show();
            };
            
            @Override
            protected void onPostExecute(String msg) {
            //	statusDialog.dismiss();
            }
        }.execute(d, null, null);
    }

	private void initBroadcastReceiver()
    {
    	LogManager.LogFunctionCall(className, "initBroadcastReceiver");
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(CommonConst.BROADCAST_JOIN_NUMBER);
	    broadcastReceiver = new BroadcastReceiver() 
	    {
	    	@Override
    		public void onReceive(Context context, Intent intent) {
	    		
    			// TODO Auto-generated method stub
	    		LogManager.LogInfoMsg(className, "initBroadcastReceiver->onReceive", "WORK");
	    		
	    		Bundle bundle = intent.getExtras();
	    		String broadcastKeyJoinNumber = BroadcastCommandEnum.join_number.toString();
	    		// ===========================================
	    		// broadcast key = join_number
	    		// ===========================================
	    		if(bundle != null && bundle.containsKey(broadcastKeyJoinNumber)){
		    		String result = bundle.getString(broadcastKeyJoinNumber);
		    		
		    		if(result != null && !result.isEmpty()){
		    			System.out.println("Join number: " + result);
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
