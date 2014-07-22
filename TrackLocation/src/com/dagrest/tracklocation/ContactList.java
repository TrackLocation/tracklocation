package com.dagrest.tracklocation;

import java.util.ArrayList;
import java.util.List;

import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.PermissionsData;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.google.gson.Gson;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ContactList extends Activity/*ListActivity*/ {

	private String jsonStringContactDeviceDataList = null;
	private ListView lv;
	// ---------------------------------------
	// /*private EditText inputSearch; */
	// ---------------------------------------
	private ArrayAdapter<String> adapter;
	private List<Boolean> isSelected;
	private ContactDeviceDataList contactDeviceDataList;
	private ContactDeviceDataList selectedContactDeviceDataList;
	private List<String> selectedContcatList;
	private Gson gson;
	private String account;
 	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_list);
		
		Intent intent = getIntent();
		gson = new Gson();
		jsonStringContactDeviceDataList = intent.getExtras().getString(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST);
		account = intent.getExtras().getString(CommonConst.PREFERENCES_PHONE_ACCOUNT);
		contactDeviceDataList = gson.fromJson(jsonStringContactDeviceDataList, ContactDeviceDataList.class);

		// jsonStringContactDeviceData = Utils.getContactDeviceDataFromJsonFile();
		//List<String> values = Controller.fillContactListWithContactDeviceDataFromJSON(jsonStringContactDeviceDataList);
		List<String> values = Controller.fillContactListWithContactDeviceDataFromJSON(contactDeviceDataList);
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
	        adapter = new ContactListArrayAdapter(this, R.layout.contact_list_item, R.id.contact, values, null);
	        //adapter = new ArrayAdapter<String>(this, R.layout.contact_list_item, R.id.contact, values);
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

	    //ListView list = getListView();
	    lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				String selectedValue = (String) adapter.getItem(position);
				// Toast.makeText(ContactList.this, selectedValue + " is LONG_CLICKED", Toast.LENGTH_LONG).show();
				// Return true to consume the click event. In this case the
				// onListItemClick listener is not called anymore.
				return true;
			}
		});
	    
	    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

	        @Override
	        public void onItemClick(AdapterView<?> parent, final View view,
	            int position, long id) {
	        	final String selectedValue = (String) parent.getItemAtPosition(position);
	        	
	        	if(selectedContcatList == null){
	        		selectedContcatList = new ArrayList<String>();
	        	}
	        	boolean isSelectedVal = isSelected.get(position);
	        	isSelected.set(position, !isSelectedVal);
	        	if(isSelected.get(position) == false){
	        		lv.getChildAt(position).setBackgroundColor(android.R.drawable.btn_default);
	        		if(selectedContcatList.contains(selectedValue)){
	        			selectedContcatList.remove(selectedValue);
	        		}
	        	} else {
	        		lv.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.LightGrey));
	        		if(!selectedContcatList.contains(selectedValue)){
	        			selectedContcatList.add(selectedValue);
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
	}
	
//	@Override
//	protected void onListItemClick(ListView l, View v, int position, long id) {
//		//get selected items
//		String selectedValue = (String) adapter.getItem(position);
//		Toast.makeText(this, selectedValue, Toast.LENGTH_SHORT).show();
//		Intent intentContactConfig = new Intent(this, ContactConfiguration.class);
//		intentContactConfig.putExtra(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA, jsonStringContactDeviceData);
//		intentContactConfig.putExtra(CommonConst.CONTACT_LIST_SELECTED_VALUE, selectedValue);
//		startActivity(intentContactConfig);
//	}

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
        	
            selectedContactDeviceDataList = Controller.removeNonSelectedContacts(contactDeviceDataList, selectedContcatList);
        	if(selectedContactDeviceDataList != null && !selectedContactDeviceDataList.getContactDeviceDataList().isEmpty()){
        		Controller.sendCommand(getApplicationContext(), selectedContactDeviceDataList, 
        			CommandEnum.status_request, null, null);
	    		Controller.sendCommand(getApplicationContext(), selectedContactDeviceDataList, 
	    			CommandEnum.start, CommonConst.PREFERENCES_PHONE_ACCOUNT, account);
	    		Intent intentMap = new Intent(getApplicationContext(), Map.class);
	    		intentMap.putExtra(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST, 
		    			new Gson().toJson(selectedContactDeviceDataList));
	   			startActivity(intentMap);
        	} else {
        		// TODO: inform customer that no contact was selected
        	}
    	// ========================================
    	// ... button
    	// ========================================
        }
	}

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }

//	public static List<String> fillContactListWithContactDeviceDataFromJSON(String jsonStringContactDeviceData){
//		List<String> values = null;
//	    
//		ContactDeviceDataList contactDeviceDataCollection = Utils.fillContactDeviceDataListFromJSON(jsonStringContactDeviceData);
//	    if(contactDeviceDataCollection == null){
//	    	return null;
//	    }
//
//	    List<ContactDeviceData> contactDeviceDataList = contactDeviceDataCollection.getContactDeviceDataList();
//	    if(contactDeviceDataList == null){
//	    	return null;
//	    }
//	    
//	    int i = 0;
//	    values = new ArrayList<String>();
//	    for (ContactDeviceData contactDeviceData : contactDeviceDataList) {
//	    	ContactData contactData = contactDeviceData.getContactData();
//	    	if(contactData != null) {
//	    		if(contactData.getNick() != null){
//	    			values.add(contactData.getNick());
//	    		} else {
//	    			values.add("unknown");
//	    			LogManager.LogErrorMsg("ContactList", "fillListWithContactDeviceData", "Some provided username is null - check JSON input file, element :" + (i+1));
//	    		}
//	    	} else {
//	    		LogManager.LogErrorMsg("ContactList", "fillListWithContactDeviceData", "Contact Data provided incorrectly - check JSON input file, element :" + (i+1));
//	    		return null;
//	    	}
//	    	i++;
// 		}
//	    
//	    return values;
//	}
}

	