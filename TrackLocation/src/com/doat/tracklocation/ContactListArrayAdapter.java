package com.doat.tracklocation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.doat.tracklocation.R;
import com.doat.tracklocation.controls.ContactStatusControl;
import com.doat.tracklocation.datatype.ContactData;
import com.doat.tracklocation.datatype.ContactDeviceData;
import com.doat.tracklocation.datatype.PermissionsData;
import com.doat.tracklocation.db.DBConst;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.model.ContactDeviceDataListModel;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;


public class ContactListArrayAdapter extends ArrayAdapter<ContactDeviceData> {
	private final Context context;
	private final List<String> emailList;
	private final List<String> macAddressList;
	private final int res;
	private String className;
	private String methodName;
	private String logMessage;
	private boolean bActiveStatusDraw = false;
	private boolean mDrawFavorite = true;
	
	public ContactListArrayAdapter(Context context, int resource, List<ContactDeviceData> values, 
			List<Boolean> checkBoxValues, List<String> emailList, List<String> macAddressList) {
		super(context, resource, values);
		this.context = context;
		this.res = resource;
		this.emailList = emailList;
		this.macAddressList = macAddressList;
		className = this.getClass().getName();
	}
 
	public ContactListArrayAdapter(Context context, int resource, int textViewResourceId, 
			List<ContactDeviceData> values, List<Boolean> checkBoxValues, List<String> emailList,
			List<String> macAddressList) {
		super(context, resource, textViewResourceId, values);
		
		this.context = context;
		this.res = resource;
		this.emailList = emailList;
		this.macAddressList = macAddressList;
	}
	
    private static class ViewHolder {
        TextView textView;
        ContactStatusControl statusImage;
        CheckBox checkBox;
        CheckBox favButton;
        ToggleButton toggleButton; 
    }
    
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ContactDeviceData contactDeviceData = (ContactDeviceData)getItem(position); 
		final ContactData contactData = contactDeviceData.getContactData();    
		final ViewHolder viewHolder; // view lookup cache stored in tag
		if (convertView == null) {
			viewHolder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	      	
			convertView = inflater.inflate(res, parent, false);
      		viewHolder.textView = (TextView) convertView.findViewById(R.id.contact);
      		//viewHolder.contact_add_panel = (RelativeLayout) convertView.findViewById(R.id.contact_add_panel);
      		viewHolder.statusImage = (ContactStatusControl) convertView.findViewById(R.id.status_image_ctrl);
      		viewHolder.statusImage.setStatusDrawVisible(this.bActiveStatusDraw);
      		viewHolder.statusImage.setDrawFavorite(mDrawFavorite);      		
      		
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
								DBLayer.getInstance().updatePermissions(p.getEmail(), isChecked, p.getCommand(), p.getAdminCommand());
								Map<String, Object> m = new HashMap<String, Object>();
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
							DBLayer.getInstance().updatePermissions(p.getEmail(), isChecked, p.getCommand(), p.getAdminCommand());
							Map<String, Object> m = new HashMap<String, Object>();
							m.put(DBConst.CONTACT_DEVICE_LOCATION_SHARING, (Integer)isChecked);
							DBLayer.getInstance().updateTableContactDevice(p.getEmail(), macAddressList.get(position), m);
							ContactDeviceDataListModel.getInstance().notifyDataSetChanged();							
						}
					}
				});
      		}
      		viewHolder.favButton = (CheckBox) convertView.findViewById(R.id.favbutton);
      		if (viewHolder.favButton != null){
      			viewHolder.favButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {						
						Map<String, Object> m = new HashMap<String, Object>();
						m.put(DBConst.CONTACT_DEVICE_IS_FAVORITE, isChecked ? 1 : 0);
						DBLayer.getInstance().updateTableContactDevice(contactData.getEmail(), contactDeviceData.getDeviceData().getDeviceMac(), m);
						contactDeviceData.setFavorite(isChecked);
						ContactDeviceDataListModel.getInstance().notifyDataSetChanged();						
					}
				});
      		}
			
			convertView.setTag(viewHolder);
		} 
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if (viewHolder.textView != null){
			viewHolder.textView.setText(contactData.getNick());
		}
		
		Bitmap bmp = contactData.getContactPhoto();
		if (bmp == null){			
			bmp = Utils.getDefaultContactBitmap(convertView.getResources());
		}
		else{
			bmp = Utils.getRoundedCornerImage(bmp, false);
		}
				
		viewHolder.statusImage.setBitmap(bmp);
		viewHolder.statusImage.setContactStatus(contactData.getContactStatus());
		viewHolder.statusImage.setFavorite(contactDeviceData.isFavorite());
		if (contactData.getContactStatus() == CommonConst.CONTACT_STATUS_START_CONNECT){
			viewHolder.statusImage.setEnabled(false);
		}
		
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
		if (viewHolder.favButton != null){
			viewHolder.favButton.setChecked(((ContactDeviceData)getItem(position)).isFavorite());		
		}
		return convertView;
	}

	public void setActiveStatusDraw(boolean bActiveStatusDraw) {
		this.bActiveStatusDraw = bActiveStatusDraw;
	}
	
	@Override
	public boolean isEnabled(int position) {
		if (bActiveStatusDraw){
			return ((ContactDeviceData)getItem(position)).getContactData().getContactStatus() != CommonConst.CONTACT_STATUS_START_CONNECT;
		}
		else{
			return super.isEnabled(position);
		}
	}

	public void setDrawFavorite(boolean b) {
		mDrawFavorite  = b;
		
	}
}
