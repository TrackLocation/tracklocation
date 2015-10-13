package com.dagrest.tracklocation;
import java.util.HashMap;
import java.util.List;

import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.PermissionsData;
import com.dagrest.tracklocation.db.DBConst;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;


public class ContactListArrayAdapter extends ArrayAdapter<ContactData> {
	private final Context context;
	private final List<String> emailList;
	private final List<String> macAddressList;
	private final int res;
	private String className;
	private String methodName;
	private String logMessage;
	
	public ContactListArrayAdapter(Context context, int resource, List<ContactData> values, 
			List<Boolean> checkBoxValues, List<String> emailList, List<String> macAddressList) {
		super(context, resource, values);
		this.context = context;
		this.res = resource;
		this.emailList = emailList;
		this.macAddressList = macAddressList;
		className = this.getClass().getName();
	}
 
	public ContactListArrayAdapter(Context context, int resource, int textViewResourceId, 
			List<ContactData> values, List<Boolean> checkBoxValues, List<String> emailList,
			List<String> macAddressList) {
		super(context, resource, textViewResourceId, values);
		
		this.context = context;
		this.res = resource;
		this.emailList = emailList;
		this.macAddressList = macAddressList;
	}
	
	// View lookup cache
    private static class ViewHolder {
        TextView textView;
        ImageView imageView;
        CheckBox checkBox;
        ToggleButton toggleButton; 
    }
    
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// Get the data item for this position
		ContactData contactData = getItem(position);    
	       // Check if an existing view is being reused, otherwise inflate the view
		final ViewHolder viewHolder; // view lookup cache stored in tag
		if (convertView == null) {
			viewHolder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	      	
			convertView = inflater.inflate(res, parent, false);
      		viewHolder.textView = (TextView) convertView.findViewById(R.id.contact);
      		viewHolder.imageView = (ImageView) convertView.findViewById(R.id.icon);
      		viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.check_share_location);
      		viewHolder.toggleButton = (ToggleButton) convertView.findViewById(R.id.tracking_toggle_button);
      		if (viewHolder.toggleButton != null){
	      		viewHolder.toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
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
	    		});
      		}
      		if (viewHolder.checkBox != null){
	      		convertView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {						
						if (viewHolder.checkBox != null) {
							int isChecked = viewHolder.checkBox.isChecked() == true ? 0 : 1;
							PermissionsData p = DBLayer.getInstance().getPermissions(emailList.get(position));
							if (p != null) {
								DBLayer.getInstance().updatePermissions(p.getEmail(), isChecked,
										p.getCommand(), p.getAdminCommand());
								java.util.Map<String, Object> m = new HashMap<String, Object>();
								m.put(DBConst.CONTACT_DEVICE_LOCATION_SHARING, (Integer)isChecked);
								DBLayer.getInstance().updateTableContactDevice(p.getEmail(), macAddressList.get(position), m);
								viewHolder.checkBox.setChecked(viewHolder.checkBox.isChecked() == true ? false : true);
							}
						}
					}
				});
      		
				viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						CheckBox cb = (CheckBox) v;
						cb.getText().toString();
						int isChecked = cb.isChecked() == true ? 1 : 0;
						PermissionsData p = DBLayer.getInstance().getPermissions(emailList.get(position));
						if (p != null) {
							DBLayer.getInstance().updatePermissions(p.getEmail(), isChecked,
									p.getCommand(), p.getAdminCommand());
							java.util.Map<String, Object> m = new HashMap<String, Object>();
							m.put(DBConst.CONTACT_DEVICE_LOCATION_SHARING, (Integer)isChecked);
							DBLayer.getInstance().updateTableContactDevice(p.getEmail(), macAddressList.get(position), m);
						}
					}
				});
      		}
			
			convertView.setTag(viewHolder);
		} 
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
	
		viewHolder.textView.setText(contactData.getNick());
		
		Bitmap bmp = contactData.getContactPhoto();
		if (bmp == null){
			bmp = BitmapFactory.decodeResource(convertView.getResources(), R.drawable.ic_launcher);
		}
		Drawable contactPhoto = new BitmapDrawable(convertView.getResources(), Utils.getRoundedCornerImage(bmp));
		contactPhoto.setBounds( 0, 0, 120, 120 );
		viewHolder.imageView.setImageDrawable(contactPhoto);

				// Action on Row click 
		if (viewHolder.checkBox != null) {								
			if(emailList != null){
				PermissionsData p = DBLayer.getInstance().getPermissions(emailList.get(position));
				if(p != null){
					boolean isChecked = p.getIsLocationSharePermitted() == 1 ? true : false;
					viewHolder.checkBox.setChecked(isChecked);
				} else {
					viewHolder.checkBox.setChecked(false);
				}
			} else {
				viewHolder.checkBox.setChecked(false);
			}
		}
		return convertView;
	}
}
