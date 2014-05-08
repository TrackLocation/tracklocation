package com.dagrest.tracklocation;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class ContactListArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;
 
	public ContactListArrayAdapter(Context context, String[] values) {
		super(context, R.layout.contact_list, values);
		this.context = context;
		this.values = values;
	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View rowView = inflater.inflate(R.layout.contact_list, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.contact);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		textView.setText(values[position]);
 
		// Change icon based on name
		String s = values[position];
 
		System.out.println(s);
 
		if (s.equals("dagrest")) {
			imageView.setImageResource(R.drawable.ic_launcher);
		} else if (s.equals("agrest2000")) {
			imageView.setImageResource(R.drawable.ic_launcher);
		} 
		return rowView;
	}
}
