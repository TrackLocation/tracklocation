package com.dagrest.tracklocation;
import java.util.List;

import com.dagrest.tracklocation.datatype.PermissionsData;
import com.dagrest.tracklocation.db.DBLayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


public class ContactListArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final List<String> values;
	private final List<String> emailList;
	private final List<Boolean> checkBoxValues;
	private final int res;
	
	public ContactListArrayAdapter(Context context, int resource, List<String> values, 
			List<Boolean> checkBoxValues, List<String> emailList) {
		super(context, resource, values);
		this.context = context;
		this.values = values;
		this.res = resource;
		this.checkBoxValues = checkBoxValues;
		this.emailList = emailList;
	}
 
	public ContactListArrayAdapter(Context context, int resource, int textViewResourceId, 
			List<String> values, List<Boolean> checkBoxValues, List<String> emailList) {
		super(context, resource, textViewResourceId, values);
		
		this.context = context;
		this.values = values;
		this.res = resource;
		this.checkBoxValues = checkBoxValues;
		this.emailList = emailList;
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
		textView.setText(values.get(position));
		
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
					}
				}
			});
		}
		
		// Case if list with check boxes
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
