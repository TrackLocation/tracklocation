package com.dagrest.tracklocation;

import java.util.ArrayList;
import java.util.List;

import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Utils;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class ContactList extends ListActivity {

	private String jsonStringContactDeviceData = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		jsonStringContactDeviceData = Utils.getContactDeviceDataFromJsonFile();
		List<String> values = fillListWithContactDeviceDataFromJSON(jsonStringContactDeviceData);
	    if(values != null){
	    	setListAdapter(new ContactListArrayAdapter(this, values));
	    } else {
	    	LogManager.LogErrorMsg("ContactList", "onCreate", "Contact Data provided incorrectly - check JSON input file.");
	    }
	 }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//get selected items
		String selectedValue = (String) getListAdapter().getItem(position);
		Toast.makeText(this, selectedValue, Toast.LENGTH_SHORT).show();
		Intent intentContactConfig = new Intent(this, ContactConfiguration.class);
		intentContactConfig.putExtra(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA, jsonStringContactDeviceData);
		intentContactConfig.putExtra(CommonConst.CONTACT_LIST_SELECTED_VALUE, selectedValue);
		startActivity(intentContactConfig);
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
	
	public static List<String> fillListWithContactDeviceDataFromJSON(String jsonStringContactDeviceData){
		List<String> values = null;
	    
		ContactDeviceDataList contactDeviceDataCollection = Utils.fillContactDeviceDataFromJSON(jsonStringContactDeviceData);
	    if(contactDeviceDataCollection == null){
	    	return null;
	    }

	    List<ContactDeviceData> contactDeviceDataList = contactDeviceDataCollection.getContactDeviceDataList();
	    if(contactDeviceDataList == null){
	    	return null;
	    }
	    
	    int i = 0;
	    values = new ArrayList<String>();
	    for (ContactDeviceData contactDeviceData : contactDeviceDataList) {
	    	ContactData contactData = contactDeviceData.getContactData();
	    	if(contactData != null) {
	    		if(contactData.getUsername() != null){
	    			values.add(contactData.getUsername());
	    		} else {
	    			values.add("unknown");
	    			LogManager.LogErrorMsg("ContactList", "fillListWithContactDeviceData", "Some provided username is null - check JSON input file, element :" + (i+1));
	    		}
	    	} else {
	    		LogManager.LogErrorMsg("ContactList", "fillListWithContactDeviceData", "Contact Data provided incorrectly - check JSON input file, element :" + (i+1));
	    		return null;
	    	}
	    	i++;
 		}
	    
	    return values;
	}
}

	