package com.doat.tracklocation;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.model.ContactDeviceDataListModel;
import com.doat.tracklocation.utils.CommonConst;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.List;

public class TrackingListActivity extends BaseActivity {
	private ListView lv;
	private ContactListArrayAdapter adapter;
	private List<String> selectedContcatList;
	private AdView adView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tracking_contact_list);

		methodName = "onCreate";
		
		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);
		 
		ContactDeviceDataList contactDeviceDataList = ContactDeviceDataListModel.getInstance().getContactDeviceDataList(TrackingListActivity.this, false);

	    if(contactDeviceDataList != null){

			lv = (ListView) findViewById(R.id.tracking_contact_list_view);
			lv.setChoiceMode(ListView.CHOICE_MODE_NONE);
	        adapter = new ContactListArrayAdapter(this, R.layout.tracking_contact_list_item, R.id.contact, contactDeviceDataList);
	        ContactDeviceDataListModel.getInstance().setAdapter("TrackingAdapter", adapter);
	    	lv.setAdapter(adapter);
	    	
	    } else {
	    	// There can be a case when data is not provided.
	    	// No contacts are joined.
	    	// Or provided incorrectly - to check JSON input file.
	    	LogManager.LogErrorMsg("ContactList", "onCreate", "Contact Data not provided "
	    			+ "- no joined contacts; or provided incorrectly - check JSON input file.");
	    	return;
	    }
		    
	    /*lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	
	        @Override
	        public void onItemClick(AdapterView<?> parent, final View view,
	            int position, long id) {
	        	final ContactDeviceData selectedValue = (ContactDeviceData) parent.getItemAtPosition(position);
				SparseBooleanArray checkedArray = lv.getCheckedItemPositions();

	        	if(!checkedArray.get(position)){
	        		lv.getChildAt(position).setBackgroundColor(android.R.drawable.btn_default);
	        		if(selectedContcatList.contains(selectedValue.getContactData().getEmail())){
	        			selectedContcatList.remove(selectedValue.getContactData().getEmail());
	        		}
	        	} else {
	        		lv.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.LightGrey));
	        		if(!selectedContcatList.contains(selectedValue.getContactData().getEmail())){
	        			selectedContcatList.add(selectedValue.getContactData().getEmail());
	        		}
	        	}
	        	
	        }
	    });*/
	    adView = (AdView)this.findViewById(R.id.adTracking);
	    AdRequest adRequest = new AdRequest.Builder().build();
	    adView.loadAd(adRequest);
	
	}

//    @Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.contact_list, menu);
//		return true;
//	}

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
	
//    public void onClick(View v) {
//    	System.out.println("Test");
//        SparseBooleanArray checked = lv.getCheckedItemPositions();
//        ArrayList<String> selectedItems = new ArrayList<String>();
//        for (int i = 0; i < checked.size(); i++) {
//            // Item position in adapter
//            int position = checked.keyAt(i);
//            // Add sport if it is checked i.e.) == TRUE!
//            if (checked.valueAt(i))
//                selectedItems.add(adapter.getItem(position));
//        }
// 
//        String[] outputStrArr = new String[selectedItems.size()];
// 
//        for (int i = 0; i < selectedItems.size(); i++) {
//            outputStrArr[i] = selectedItems.get(i);
//        }
//     }
	

//	 @Override
//	  public View getView(int position, View convertView, ViewGroup parent) {
//	  
//	   ViewHolder holder = null;
//	   Log.v("ConvertView", String.valueOf(position));
//	  
//	   if (convertView == null) {
//	   LayoutInflater vi = (LayoutInflater)getSystemService(
//	     Context.LAYOUT_INFLATER_SERVICE);
//	   convertView = vi.inflate(R.layout.country_info, null);
//	  
//	   holder = new ViewHolder();
//	   holder.code = (TextView) convertView.findViewById(R.id.code);
//	   holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
//	   convertView.setTag(holder);
//	  
//	    holder.name.setOnClickListener( new View.OnClickListener() { 
//	     public void onClick(View v) { 
//	      CheckBox cb = (CheckBox) v ; 
//	      Country country = (Country) cb.getTag(); 
//	      Toast.makeText(getApplicationContext(),
//	       "Clicked on Checkbox: " + cb.getText() +
//	       " is " + cb.isChecked(),
//	       Toast.LENGTH_LONG).show();
//	      country.setSelected(cb.isChecked());
//	     } 
//	    }); 
//	   }
//	   else {
//	    holder = (ViewHolder) convertView.getTag();
//	   }
//	  
//	   Country country = countryList.get(position);
//	   holder.code.setText(" (" +  country.getCode() + ")");
//	   holder.name.setText(country.getName());
//	   holder.name.setChecked(country.isSelected());
//	   holder.name.setTag(country);
//	  
//	   return convertView;
//	  
//	  }
	 
	@Override
	protected void onDestroy() {
		adView.destroy();
		methodName = "onDestroy";
		LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_DESTROY] {" + className + "} -> " + methodName);
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