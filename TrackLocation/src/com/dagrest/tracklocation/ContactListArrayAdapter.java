package com.dagrest.tracklocation;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class ContactListArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final List<String> values;
	private final int res;
	
	public ContactListArrayAdapter(Context context, int resource, List<String> values) {
		super(context, resource, values);
		this.context = context;
		this.values = values;
		this.res = resource;
	}
 
	public ContactListArrayAdapter(Context context, int resource, int textViewResourceId, List<String> values) {
		super(context, resource, textViewResourceId, values);
		
		this.context = context;
		this.values = values;
		this.res = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
		//View rowView = inflater.inflate(R.layout.contact_list_item, parent, false);
		View rowView = inflater.inflate(res, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.contact);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		textView.setText(values.get(position));
	
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
