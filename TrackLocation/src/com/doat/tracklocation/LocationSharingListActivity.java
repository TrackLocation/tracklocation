package com.doat.tracklocation;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.doat.tracklocation.datatype.ContactDeviceData;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.PermissionsData;
import com.doat.tracklocation.db.DBConst;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.model.ContactDeviceDataListModel;
import com.doat.tracklocation.utils.CommonConst;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.HashMap;
import java.util.Map;

public class LocationSharingListActivity extends BaseActivity {

	private ListView lv;
	private ContactListArrayAdapter adapter;
	private AdView adView;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_sharing_list);
		methodName = "onCreate";
		
		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);

		ContactDeviceDataList contactDeviceDataList = ContactDeviceDataListModel.getInstance().getContactDeviceDataList(false);

		Controller.fillContactDeviceData(LocationSharingListActivity.this, contactDeviceDataList);
		
	    if(contactDeviceDataList != null){	    	
			lv = (ListView) findViewById(R.id.location_sharing_list_view);
			
	        adapter = new ContactListArrayAdapter(this, R.layout.location_sharing_list_item, R.id.contact, contactDeviceDataList);
	        ContactDeviceDataListModel.getInstance().setAdapter("locationSharingAdapter", adapter);
	    	lv.setAdapter(adapter);
	    	
	    } else {
	    	// There can be a case when data is not provided.
	    	// No contacts are joined.
	    	// Or provided incorrectly - to check JSON input file.
	    	LogManager.LogErrorMsg("ContactList", "onCreate", "Contact Data not provided "
	    			+ "- no joined contacts; or provided incorrectly - check JSON input file.");
	    	return;
	    }

	    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

	        @Override
	        public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				final ContactDeviceData contactDeviceData = (ContactDeviceData)adapter.getItem(position);
				PermissionsData p = DBLayer.getInstance().getPermissions(contactDeviceData.getContactData().getEmail());
				if (p != null) {
					int iPerm = p.getIsLocationSharePermitted() == 1 ? 0 : 1; //Switch permit
					DBLayer.getInstance().updatePermissions(p.getEmail(), iPerm , p.getCommand(), p.getAdminCommand());
					Map<String, Object> m = new HashMap<String, Object>();
					m.put(DBConst.CONTACT_DEVICE_LOCATION_SHARING, iPerm);
					ContactDeviceDataListModel.getInstance().updateContactDeviceDataList(contactDeviceData, m);
					ContactDeviceDataListModel.getInstance().notifyDataSetChanged();
				}
	        }
	    });

	    adView = (AdView)this.findViewById(R.id.adLocation);
	    AdRequest adRequest = new AdRequest.Builder().build();
	    adView.loadAd(adRequest);
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

	@Override
    protected void onDestroy() {
    	methodName = "onDestroy";
		LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_DESTROY] {" + className + "} -> " + methodName);
		adView.destroy();
    	super.onDestroy();
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
