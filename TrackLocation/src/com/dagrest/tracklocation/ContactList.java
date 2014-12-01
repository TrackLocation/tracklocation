package com.dagrest.tracklocation;

import java.util.ArrayList;
import java.util.List;








import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
//import com.dagrest.tracklocation.datatype.AppInfo;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.dialog.CommonDialog;
import com.dagrest.tracklocation.dialog.IDialogOnClickAction;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ContactList extends Activity {

	private static final int EDIT_OPTION = 0;
	private static final int DELETE_OPTION = 1;
	private String jsonStringContactDeviceDataList = null;
	private ListView lv;
	// ---------------------------------------
	// /*private EditText inputSearch; */
	// ---------------------------------------
	private ArrayAdapter<ContactData> adapter;
	private List<Boolean> isSelected;
	private ContactDeviceDataList contactDeviceDataList;
	private ContactDeviceDataList selectedContactDeviceDataList;
	private List<String> selectedContcatList;
	private Gson gson;
	private String className;
	private Context context;
	List<ContactData> values;
 	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_list);
		className = this.getClass().getName();
		context = getApplicationContext();
		
		
		Intent intent = getIntent();
		gson = new Gson();
		jsonStringContactDeviceDataList = intent.getExtras().getString(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST);
		contactDeviceDataList = gson.fromJson(jsonStringContactDeviceDataList, ContactDeviceDataList.class);

		values = Controller.fillContactListWithContactDeviceDataFromJSON(contactDeviceDataList, null, null, null);
	    if(values != null){
	    	// TODO: move to init isSelected list:
	    	isSelected = new ArrayList<Boolean>(values.size());
	    	for (int i = 0; i < values.size(); i++) {
	    		isSelected.add(false);
	    	}
	    	
			lv = (ListView) findViewById(R.id.contact_list_view);
			
			// ---------------------------------------
			/*
			// Create search edit text
	        // inputSearch = (EditText) findViewById(R.id.find_contact);
			*/
			// ---------------------------------------

//	    	View header = getLayoutInflater().inflate(R.layout.find_contacts_header, null);
//	    	ListView listView = getListView();
//	    	listView.addHeaderView(header);
	        adapter = new ContactListArrayAdapter(this, R.layout.contact_list_item, R.id.contact, values, null, null, null);
	    	lv.setAdapter(adapter);
	    	
			// ---------------------------------------
	    	/*
	        //
	        // Enabling Search Filter
	        //
	        inputSearch.addTextChangedListener(new TextWatcher() {
	             
	            @Override
	            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
	                // When user changed the Text
	            	ContactList.this.adapter.getFilter().filter(cs);
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
	        */
			// ---------------------------------------
	         
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
	        	final ContactData selectedValue = (ContactData)adapter.getItem(position);
	        	
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
	        	
	        	// TODO: move the following code to a separate function:
	        	/*
				Toast.makeText(ContactList.this, selectedValue, Toast.LENGTH_SHORT).show();
				Intent intentContactConfig = new Intent(ContactList.this, ContactConfiguration.class);
				intentContactConfig.putExtra(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST, jsonStringContactDeviceDataList);
				intentContactConfig.putExtra(CommonConst.CONTACT_LIST_SELECTED_VALUE, selectedValue);
				startActivity(intentContactConfig);
				*/
	        }

	    });
	    
	    lv.setOnScrollListener(new OnScrollListener(){
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
	    });
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
	
	public void onClick(final View view) {
		
    	// ========================================
    	// TrackLocation button
    	// ========================================
        if (view == findViewById(R.id.btnTrackLocation)) {
        	
        	LogManager.LogFunctionCall(className, "onClick->[BUTTON:TrackLocation]");
        	
            selectedContactDeviceDataList = Controller.removeNonSelectedContacts(contactDeviceDataList, selectedContcatList);
        	if(selectedContactDeviceDataList != null && !selectedContactDeviceDataList.getContactDeviceDataList().isEmpty()){
        		
        		LogManager.LogInfoMsg(className, "onClick->[BUTTON:TrackLocation]", "Track location of " + selectedContactDeviceDataList.toString());
        		
				// Catch ecxeption - version changed, so RegID is empty
				// Show pop up message - reinstall app/update regID.
				
	    		// Start Map activity to see locations of selected contacts
	    		Intent intentMap = new Intent(context, Map.class);
	    		// Pass to Map activity list of selected contacts to get their location
	    		intentMap.putExtra(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST, new Gson().toJson(selectedContactDeviceDataList));
	   			startActivity(intentMap);
        	} else {
        		// TODO: Inform customer that no contact was selected by pop-up dialog
        		String title = "No contacs selected";
        		String dialogMessage = "\nSelect at least one contact to locate it\n\n";
        		showNotificationDialog(title, dialogMessage);
        	}
        	
        	LogManager.LogFunctionExit(className, "onClick->[BUTTON:TrackLocation]");

    	// ========================================
    	// ... button
    	// ========================================
        }
	}

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }

	private void showNotificationDialog(String title, String errorMessage) {
    	String dialogMessage = errorMessage;
    	
		CommonDialog aboutDialog = new CommonDialog(this, notificationDialogOnClickAction);
		aboutDialog.setDialogMessage(dialogMessage);
		aboutDialog.setDialogTitle(title);
		aboutDialog.setPositiveButtonText("OK");
		aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
		aboutDialog.showDialog();
		aboutDialog.setCancelable(true);
    }

	IDialogOnClickAction notificationDialogOnClickAction = new IDialogOnClickAction() {
		
		@Override
		public void doOnPositiveButton() {
			// TODO Auto-generated method stub
		}
		@Override
		public void doOnNegativeButton() {
			// TODO Auto-generated method stub
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
			
			break;
		case DELETE_OPTION:
			removeContact(position);
			break;
		default:
			break;
        }
        
        return true;
	}
	
	private void removeContact(int deletePosition){
		final ContactData deleteContact = adapter.getItem(deletePosition);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ContactList.this); 
		// set title
		alertDialogBuilder.setTitle(getString(R.string.delete_menu_operation));
 
			// set dialog message
		alertDialogBuilder
			.setMessage("The contact " + deleteContact.getNick() + " will be removed from the application")
			.setCancelable(false)
			.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					ContactDeviceData contactDeviceData = contactDeviceDataList.getContactDeviceDataByContactData(deleteContact);
					if (contactDeviceData != null ){
						if (DBLayer.removeContactDataDeviceDetail(contactDeviceData) != -1){
							values.remove(deleteContact);
							adapter.notifyDataSetChanged(); 
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

	