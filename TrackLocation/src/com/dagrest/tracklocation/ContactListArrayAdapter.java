package com.dagrest.tracklocation;
import java.util.HashMap;
import java.util.List;

import com.dagrest.tracklocation.datatype.PermissionsData;
import com.dagrest.tracklocation.db.DBConst;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;


public class ContactListArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final List<String> values;
	private final List<String> emailList;
	private final List<Boolean> checkBoxValues;
	private final List<String> macAddressList;
	private final int res;
	private String className;
	private String methodName;
	private String logMessage;
	
	public ContactListArrayAdapter(Context context, int resource, List<String> values, 
			List<Boolean> checkBoxValues, List<String> emailList, List<String> macAddressList) {
		super(context, resource, values);
		this.context = context;
		this.values = values;
		this.res = resource;
		this.checkBoxValues = checkBoxValues;
		this.emailList = emailList;
		this.macAddressList = macAddressList;
		className = this.getClass().getName();
	}
 
	public ContactListArrayAdapter(Context context, int resource, int textViewResourceId, 
			List<String> values, List<Boolean> checkBoxValues, List<String> emailList,
			List<String> macAddressList) {
		super(context, resource, textViewResourceId, values);
		
		this.context = context;
		this.values = values;
		this.res = resource;
		this.checkBoxValues = checkBoxValues;
		this.emailList = emailList;
		this.macAddressList = macAddressList;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
		//View rowView = inflater.inflate(R.layout.contact_list_item, parent, false);
		View rowView = inflater.inflate(res, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.contact);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.check_share_location);
		ToggleButton toggleButton = (ToggleButton) rowView.findViewById(R.id.tracking_toggle_button);
		textView.setText(values.get(position));
		
		OnCheckedChangeListener toggleButtonListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				methodName = "onCheckedChanged";
				if(isChecked){
					logMessage = "ToggleButton is checked by " + emailList.get(position);
					LogManager.LogInfoMsg(className, methodName, logMessage);
					Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
				} else {
					logMessage = "ToggleButton is unchecked " + emailList.get(position);
					LogManager.LogInfoMsg(className, methodName, logMessage);
					Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
				}
			}
		};

		// 
		if(toggleButton != null){
			toggleButton.setOnCheckedChangeListener(toggleButtonListener);
		}
		
		
		// Action on Row click 
		if (checkBox != null) {
			rowView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					CheckBox checkBox = (CheckBox) v.findViewById(R.id.check_share_location);
					if (checkBox != null) {
						int isChecked = checkBox.isChecked() == true ? 0 : 1;
						PermissionsData p = DBLayer.getPermissions(emailList.get(position));
						if (p != null) {
							DBLayer.updatePermissions(p.getEmail(), isChecked,
									p.getCommand(), p.getAdminCommand());
							java.util.Map<String, Object> m = new HashMap<String, Object>();
							m.put(DBConst.CONTACT_DEVICE_LOCATION_SHARING, (Integer)isChecked);
							DBLayer.updateTableContactDevice(p.getEmail(), macAddressList.get(position), m);
							checkBox.setChecked(checkBox.isChecked() == true ? false : true);
						}
					}
				}
			});
		}
		
		// Action on CheckBox click
		if (checkBox != null) {
			checkBox.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					CheckBox cb = (CheckBox) v;
					String t = cb.getText().toString();
					int isChecked = cb.isChecked() == true ? 1 : 0;
					PermissionsData p = DBLayer.getPermissions(emailList.get(position));
					if (p != null) {
						DBLayer.updatePermissions(p.getEmail(), isChecked,
								p.getCommand(), p.getAdminCommand());
						java.util.Map<String, Object> m = new HashMap<String, Object>();
						m.put(DBConst.CONTACT_DEVICE_LOCATION_SHARING, (Integer)isChecked);
						DBLayer.updateTableContactDevice(p.getEmail(), macAddressList.get(position), m);
					}
				}
			});
		}
		
		// Case if list with check boxes - set CheckBox marked/unmarked
		if (checkBox != null){
			//CheckBox checkBoxShareLocation = (CheckBox) rowView.findViewById(R.id.check_share_location);
			if(emailList != null){
				PermissionsData p = DBLayer.getPermissions(emailList.get(position));
				if(p != null){
					boolean isChecked = p.getIsLocationSharePermitted() == 1 ? true : false;
					checkBox.setChecked(isChecked);
				} else {
					checkBox.setChecked(false);
				}
			} else {
				checkBox.setChecked(false);
			}
		}
	
		// Change icon based on name
		String s = values.get(position);
	
		if (s.equals("dagrest")) {
			imageView.setImageResource(R.drawable.ic_launcher);
		} else if (s.equals("agrest2000")) {
			imageView.setImageResource(R.drawable.ic_launcher);
		} 
	
		return super.getView(position, rowView, parent);
	}
		
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		LayoutInflater inflater = (LayoutInflater) context
//			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
// 
//		View rowView = inflater.inflate(R.layout.contact_list_item, parent, false);
//		TextView textView = (TextView) rowView.findViewById(R.id.contact);
//		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
//		textView.setText(values.get(position));
// 
//		// Change icon based on name
//		String s = values.get(position);
// 
//		System.out.println(s);
// 
//		if (s.equals("dagrest")) {
//			imageView.setImageResource(R.drawable.ic_launcher);
//		} else if (s.equals("agrest2000")) {
//			imageView.setImageResource(R.drawable.ic_launcher);
//		} 
//		return rowView;
//	}
}
