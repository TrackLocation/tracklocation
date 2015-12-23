package com.doat.tracklocation;

import java.util.ArrayList;
import java.util.List;

import com.doat.tracklocation.R;
import com.doat.tracklocation.datatype.ContactData;
import com.doat.tracklocation.datatype.ContactDeviceData;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TrackingListActivity extends BaseActivity {
	private ListView lv;
	private ArrayAdapter<ContactDeviceData> adapter;
	private List<Boolean> isSelected;
	private List<String> selectedContcatList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tracking_contact_list);

		methodName = "onCreate";
		
		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);

		ArrayList<ContactDeviceData> selectedContactDeviceDataListEx = this.getIntent().getExtras().getParcelableArrayList(CommonConst.CONTACT_DEVICE_DATA_LIST); 
		ContactDeviceDataList contactDeviceDataList = new ContactDeviceDataList();
		contactDeviceDataList.addAll(selectedContactDeviceDataListEx);
	
		List<Boolean> checkBoxesShareLocation = new ArrayList<Boolean>();
		List<String> emailList = new ArrayList<String>();
		List<String> macAddressList = new ArrayList<String>();
		Controller.fillContactDeviceData(TrackingListActivity.this, contactDeviceDataList, checkBoxesShareLocation, emailList, macAddressList);
		
	    if(contactDeviceDataList != null){
	    	// TODO: move to init isSelected list:
	    	isSelected = new ArrayList<Boolean>(contactDeviceDataList.size());
	    	for (int i = 0; i < contactDeviceDataList.size(); i++) {
	    		isSelected.add(false);
	    	}
	    	
			lv = (ListView) findViewById(R.id.tracking_contact_list_view);
			
	        adapter = new ContactListArrayAdapter(this, R.layout.tracking_contact_list_item, 
	        	R.id.contact, contactDeviceDataList, checkBoxesShareLocation, emailList, macAddressList);
	    	lv.setAdapter(adapter);
	    	
	    } else {
	    	// There can be a case when data is not provided.
	    	// No contacts are joined.
	    	// Or provided incorrectly - to check JSON input file.
	    	LogManager.LogErrorMsg("ContactList", "onCreate", "Contact Data not provided "
	    			+ "- no joined contacts; or provided incorrectly - check JSON input file.");
	    	return;
	    }
	
//	    lv.setOnItemLongClickListener(new OnItemLongClickListener() {
//			@Override
//			public boolean onItemLongClick(AdapterView<?> parent, View view,
//					int position, long id) {
//				String selectedValue = (String) adapter.getItem(position);
//				// Toast.makeText(ContactList.this, selectedValue + " is LONG_CLICKED", Toast.LENGTH_LONG).show();
//				// Return true to consume the click event. In this case the
//				// onListItemClick listener is not called anymore.
//				return true;
//			}
//		});
	    
	    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	
	        @Override
	        public void onItemClick(AdapterView<?> parent, final View view,
	            int position, long id) {
	        	final ContactData selectedValue = (ContactData) parent.getItemAtPosition(position);
	        	
	        	if(selectedContcatList == null){
	        		selectedContcatList = new ArrayList<String>();
	        	}
	        	boolean isSelectedVal = isSelected.get(position);
	        	isSelected.set(position, !isSelectedVal);
	        	if(isSelected.get(position) == false){
	        		lv.getChildAt(position).setBackgroundColor(android.R.drawable.btn_default);
	        		if(selectedContcatList.contains(selectedValue.getEmail())){
	        			selectedContcatList.remove(selectedValue.getEmail());
	        		}
	        	} else {
	        		lv.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.LightGrey));
	        		if(!selectedContcatList.contains(selectedValue.getEmail())){
	        			selectedContcatList.add(selectedValue.getEmail());
	        		}
	        	}
	        	
	        }
	    });
	    
//	    ToggleButton toggle = (ToggleButton) findViewById(R.id.tracking_toggle_button);
//	    toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//	                    Toast.makeText(getApplicationContext(), buttonView.isChecked()+"", Toast.LENGTH_SHORT).show();
//	                }
//	            });
	
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
	
//	public void onClick(final View view) {
//	}

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
		super.onDestroy();
		methodName = "onDestroy";
		LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_DESTROY] {" + className + "} -> " + methodName);
	}
}