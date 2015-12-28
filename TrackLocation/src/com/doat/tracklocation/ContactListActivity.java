package com.doat.tracklocation;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;

import com.doat.tracklocation.R;
import com.doat.tracklocation.broadcast.BroadcastReceiverContactListActivity;
import com.doat.tracklocation.controller.ContactListController;
import com.doat.tracklocation.datatype.ContactData;
import com.doat.tracklocation.datatype.ContactDeviceData;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.dialog.InfoDialog;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ContactListActivity extends BaseActivity {	
	private static final int EDIT_OPTION = 0;
	private static final int DELETE_OPTION = 1;

	private ListView lv;
	// ---------------------------------------
	// /*private EditText inputSearch; */
	// ---------------------------------------
	private ArrayAdapter<ContactDeviceData> adapter;
	//private List<Boolean> isSelected;
	private ContactDeviceDataList contactDeviceDataList;
	//private List<String> selectedContcatList;
	private ContactListController contactListController;

	List<ContactData> values;
 	
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

		ArrayList<ContactDeviceData> selectedContactDeviceDataListEx = this.getIntent().getExtras().getParcelableArrayList(CommonConst.CONTACT_DEVICE_DATA_LIST); 
		contactDeviceDataList = new ContactDeviceDataList();
		contactDeviceDataList.addAll(selectedContactDeviceDataListEx);		
		
		Controller.fillContactDeviceData(ContactListActivity.this, contactDeviceDataList, null, null, null);
	    if(contactDeviceDataList != null){
	    	// TODO: move to init isSelected list:
	    	/*isSelected = new ArrayList<Boolean>(values.size());
	    	for (int i = 0; i < values.size(); i++) {
	    		isSelected.add(false);
	    	}*/
	    	
			lv = (ListView) findViewById(R.id.contact_list_view);			
	        adapter = new ContactListArrayAdapter(this, R.layout.contact_list_item, R.id.contact, contactDeviceDataList, null, null, null);
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

	    	    
/*	    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

	        @Override
	        public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
	        	final ContactData selectedValue = ((ContactDeviceData)adapter.getItem(position)).getContactData();
	        	
	        	if(selectedContcatList == null){
	        		selectedContcatList = new ArrayList<String>();
	        	}
	        	boolean isSelectedVal = isSelected.get(position);
	        	isSelected.set(position, !isSelectedVal);
	        	
	        	int visiblePosition = position - lv.getFirstVisiblePosition();
	        	if(isSelected.get(position) == false){
	        		if(lv.getChildAt(visiblePosition) != null){
		        		lv.getChildAt(visiblePosition).setBackgroundColor(android.R.drawable.btn_default);
		        		if(selectedContcatList.contains(selectedValue.getNick())){
		        			selectedContcatList.remove(selectedValue.getNick());
		        		}
	        		}
	        	} else {
	        		if(lv.getChildAt(visiblePosition) != null){
		        		lv.getChildAt(visiblePosition).setBackgroundColor(getResources().getColor(R.color.LightGrey));
		        		if(!selectedContcatList.contains(selectedValue.getNick())){
		        			selectedContcatList.add(selectedValue.getNick());
		        		}
	        		}
	        	}	        
	        }

	    });*/
	    
	    /*lv.setOnScrollListener(new OnScrollListener(){
	        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	          // TODO Auto-generated method stub
	        }
	        
	        public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(scrollState == 0) 
	        	{
					if (view.getId() == lv.getId()) {
						int firstVisiblePosition = lv.getFirstVisiblePosition();
						int lastVisiblePosition = lv.getLastVisiblePosition();
						for (int i = firstVisiblePosition; i < lastVisiblePosition; i++) {
							int isSelectedIndex = i + firstVisiblePosition;
							if(isSelectedIndex >= isSelected.size()){
								continue;
							}
				        	if(isSelected.get(isSelectedIndex) == false){
				        		if(lv.getChildAt(i) != null){
					        		lv.getChildAt(i).setBackgroundColor(android.R.drawable.btn_default);
				        		}
				        	} else {
				        		if(lv.getChildAt(i) != null){
					        		lv.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.LightGrey));
				        		}
				        	}
						}
					}
	        	}
			}
	    });*/

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
/*	public void onClick(final View view) {
        if (view == findViewById(R.id.btnTrackLocation)) {
        	
        	LogManager.LogFunctionCall(className, "onClick->[BUTTON:TrackLocation]");
        	
        	ContactDeviceDataList selectedContactDeviceDataList = Controller.removeNonSelectedContacts(contactDeviceDataList, selectedContcatList);
        	if(selectedContactDeviceDataList != null && !selectedContactDeviceDataList.isEmpty()){
        		
        		LogManager.LogInfoMsg(className, "onClick->[BUTTON:TrackLocation]", "Track location of " + selectedContactDeviceDataList.toString());
        		
				// Catch ecxeption - version changed, so RegID is empty
				// Show pop up message - reinstall app/update regID.
				
            	// Stop thread that checking which contacts are online
                if(contactListController != null){
                	contactListController.stopCheckWhichContactsOnLineThread();
                }

                // Start Map activity to see locations of selected contacts
	    		Intent intentMap = new Intent(this, MapActivity.class);
	    		// Pass to Map activity list of selected contacts to get their location	    		
	    		intentMap.putParcelableArrayListExtra(CommonConst.CONTACT_DEVICE_DATA_LIST, selectedContactDeviceDataList);
	   			startActivity(intentMap);
        	} else {
        		// TODO: Inform customer that no contact was selected by pop-up dialog
        		String title = "No contacs selected";
        		String dialogMessage = "\nSelect at least one contact to locate it\n\n";
        		new InfoDialog(this, context, title, dialogMessage, null);
        	}
        	
        	LogManager.LogFunctionExit(className, "onClick->[BUTTON:TrackLocation]");

        }
	}*/

 	@Override
	protected void onStart() {
		super.onStart();
		/*if(broadcastReceiver == null){
			broadcastReceiver = new BroadcastReceiverContactListActivity(ContactListActivity.this, R.id.contact_list_view);
		}
		initNotificationBroadcastReceiver(broadcastReceiver);
		
		// Start thread to check which contacts are online
		if(contactListController != null){
			State state = contactListController.getThreadState();
			contactListController.startCheckWhichContactsOnLineThread(contactDeviceDataList);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
/*        if(broadcastReceiver != null){
    		unregisterReceiver(broadcastReceiver);
    	}
    	// Stop thread that checking which contacts are online
        if(contactListController != null){
        	contactListController.stopCheckWhichContactsOnLineThread();
        }
*/	}

	@Override
    protected void onDestroy() {
    	super.onDestroy();

    	LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_DESTROY] {" + className + "} -> " + methodName);
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
	    		adapter.notifyDataSetChanged(); 	
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
					ContactDeviceData contactDeviceData = contactDeviceDataList.getContactDeviceDataByContactData(deleteContact.getContactData().getEmail());
					if (contactDeviceData != null ){
						if (DBLayer.getInstance().removeContactDataDeviceDetail(contactDeviceData) != -1){
							values.remove(deleteContact);
							adapter.notifyDataSetChanged(); 
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
}

	
