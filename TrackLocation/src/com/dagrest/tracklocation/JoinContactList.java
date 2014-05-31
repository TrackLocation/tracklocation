package com.dagrest.tracklocation;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.join_contact_list);
		
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

}
