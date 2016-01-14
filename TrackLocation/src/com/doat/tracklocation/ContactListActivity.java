package com.doat.tracklocation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

import com.doat.tracklocation.controller.ContactListController;
import com.doat.tracklocation.datatype.ContactDeviceData;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.model.ContactDeviceDataListModel;
import com.doat.tracklocation.utils.CommonConst;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class ContactListActivity extends BaseActivity {	
	private static final int EDIT_OPTION = 0;
	private static final int DELETE_OPTION = 1;

	private ListView lv;
	private ContactListArrayAdapter adapter;
	private ContactDeviceDataList contactDeviceDataList;
	private ContactListController contactListController;
	private AdView adView;
 	
    public ContactListController getContactListController() {
		return contactListController;
	}

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_list);
		className = this.getClass().getName();
		methodName = "onCreate";
		
		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);

		if(contactListController == null){
			contactListController = new ContactListController(this, getApplicationContext());
		}

		contactDeviceDataList = ContactDeviceDataListModel.getInstance().getContactDeviceDataList(false);
		
		Controller.fillContactDeviceData(ContactListActivity.this, contactDeviceDataList);
		lv = (ListView) findViewById(R.id.contact_list_view);
	    if(contactDeviceDataList != null){					
	        adapter = new ContactListArrayAdapter(this, R.layout.contact_list_item, R.id.contact, contactDeviceDataList);
	        adapter.setDrawFavorite(false);
	        ContactDeviceDataListModel.getInstance().setAdapter("contactActivityAdapter", adapter);
	    	lv.setAdapter(adapter);	    		         
	    } else {
	    	// There can be a case when data is not provided.
	    	// No contacts are joined.
	    	// Or provided incorrectly - to check JSON input file.
	    	LogManager.LogErrorMsg("ContactList", "onCreate", "Contact Data not provided "
	    			+ "- no joined contacts; or provided incorrectly - check JSON input file.");
	    	return;
	    }

	    registerForContextMenu(lv);
	    	    
	    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

	        @Override
	        public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
	        	final ContactDeviceData selectedValue = (ContactDeviceData)adapter.getItem(position);
	        	selectedValue.setFavorite(!selectedValue.isFavorite());
	        	ContactDeviceDataListModel.getInstance().notifyDataSetChanged();
	        }
	    });	    
	    
		adView = (AdView)this.findViewById(R.id.adViewContacts);
	    AdRequest adRequest = new AdRequest.Builder().build();
	    adView.loadAd(adRequest);

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.contact_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onClick(final View view) {
        if (view == findViewById(R.id.btnTrackLocation)) {        	
        	LogManager.LogFunctionCall(className, "onClick->[BUTTON:TrackLocation]");
	        finish();
        	
        	LogManager.LogFunctionExit(className, "onClick->[BUTTON:TrackLocation]");
        }
	}

	@Override
    protected void onDestroy() {
		adView.destroy();
    	LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_DESTROY] {" + className + "} -> " + methodName);
    	super.onDestroy();
    }

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);	    
	    menu.setHeaderTitle(getString(R.string.choose_operation));
	    menu.add(0, EDIT_OPTION, 0, getString(R.string.edit_menu_operation));
	    menu.add(0, DELETE_OPTION, 0, getString(R.string.delete_menu_operation));
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        switch (item.getItemId()) {
		case EDIT_OPTION:
			editContact(position);
			break;
		case DELETE_OPTION:
			removeContact(position);
			break;
		default:
			break;
        }
        
        return true;
	}
	
	private void editContact(int position) {
		final ContactDeviceData editContact = adapter.getItem(position);
		Intent contactEditIntent = new Intent(this, ContactEditActivity.class);	
		contactEditIntent.putExtra(CommonConst.JSON_STRING_CONTACT_DATA, editContact);
		contactEditIntent.putExtra(CommonConst.CONTACT_LIST_SELECTED_VALUE, position);
		startActivityForResult(contactEditIntent,2);		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // Check which request we're responding to
	        
		 if(requestCode==2){
			// Make sure the request was successful
	        if (resultCode == RESULT_OK) {	        	 			        	
	    		ContactDeviceData contactData = data.getExtras().getParcelable(CommonConst.JSON_STRING_CONTACT_DATA);  	    		
	    		int contactPosition = data.getExtras().getInt(CommonConst.CONTACT_LIST_SELECTED_VALUE);	    		
	    		adapter.remove(adapter.getItem(contactPosition));
	    		adapter.insert(contactData, contactPosition);
	    		ContactDeviceDataListModel.getInstance().notifyDataSetChanged(); 	
	    		LogManager.LogInfoMsg(className, "onActivityResult", "ContactData of " + contactData.getContactData().getNick() + " was updated");
	    		Toast.makeText(ContactListActivity.this, "The contact " + contactData.getContactData().getNick() + " was updated", Toast.LENGTH_SHORT).show();    		
	        }
		}
	}

	private void removeContact(int deletePosition){
		final ContactDeviceData deleteContact = adapter.getItem(deletePosition);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ContactListActivity.this); 
		// set title
		alertDialogBuilder.setTitle(getString(R.string.delete_menu_operation));
 
		// set dialog message
		alertDialogBuilder
			.setMessage("The contact " + deleteContact.getContactData().getNick() + " will be removed from the application")
			.setCancelable(false)
			.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					//ContactDeviceData contactDeviceData = adapter.getItem(deletePosition);
					//ContactDeviceData contactDeviceData = contactDeviceDataList.getContactDeviceDataByContactData(deleteContact.getContactData().getEmail());
					if (deleteContact != null ){
						if (DBLayer.getInstance().removeContactDataDeviceDetail(deleteContact) != -1){
							adapter.remove(deleteContact);	
							ContactDeviceDataListModel.getInstance().notifyDataSetChanged(); 
							Toast.makeText(ContactListActivity.this, "The contact " + deleteContact.getContactData().getNick() + " was removed", Toast.LENGTH_SHORT).show();
						}
					}
				}
			})
			.setNegativeButton("No",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});
 
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
	
	@Override
	protected void onPause() {
		adView.pause();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		adView.resume();
	}
}

	
