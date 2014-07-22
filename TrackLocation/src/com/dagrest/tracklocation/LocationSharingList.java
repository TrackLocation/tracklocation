package com.dagrest.tracklocation;

import java.util.ArrayList;
import java.util.List;

import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.google.gson.Gson;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;

public class LocationSharingList extends Activity {

	private String jsonStringContactDeviceDataList = null;
	private ListView lv;
	private ArrayAdapter<String> adapter;
	private List<Boolean> isSelected;
	private Gson gson;
	private ContactDeviceDataList contactDeviceDataList;
	private ContactDeviceDataList selectedContactDeviceDataList;
	private List<String> selectedContcatList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_sharing_list);

		Intent intent = getIntent();
		gson = new Gson();
		jsonStringContactDeviceDataList = intent.getExtras().getString(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST);
		//account = intent.getExtras().getString(CommonConst.PREFERENCES_PHONE_ACCOUNT);
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
	    	
			lv = (ListView) findViewById(R.id.location_sharing_list_view);
			
	        adapter = new ContactListArrayAdapter(this, R.layout.location_sharing_list_item, R.id.contact, values);
	    	lv.setAdapter(adapter);
	    	
	    } else {
	    	// There can be a case when data is not provided.
	    	// No contacts are joined.
	    	// Or provided incorrectly - to check JSON input file.
	    	LogManager.LogErrorMsg("ContactList", "onCreate", "Contact Data not provided "
	    			+ "- no joined contacts; or provided incorrectly - check JSON input file.");
	    	return;
	    }
	
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
	}

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }
}
