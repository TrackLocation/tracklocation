package com.doat.tracklocation;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactListArrayAdapter extends ArrayAdapter<ContactDeviceData> {
	private final Context context;
	private final int res;
	private String className;
	private String methodName;
	private String logMessage;
	private boolean bActiveStatusDraw = false;
	private boolean mDrawFavorite = true;
	private int mContactStatusPending = CommonConst.CONTACT_STATUS_START_CONNECT;
	
	public ContactListArrayAdapter(Context context, int resource, int textViewResourceId, List<ContactDeviceData> values) {
		super(context, resource, textViewResourceId, values);
		this.context = context;
		this.res = resource;
		className = this.getClass().getName();
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
      		viewHolder.statusImage.setSecondaryContactStatus(mContactStatusPending);

      		viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.check_share_location);
      		viewHolder.toggleButton = (ToggleButton) convertView.findViewById(R.id.tracking_toggle_button);
      		if (viewHolder.toggleButton != null){
	      		viewHolder.toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	    			@Override
	    			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	    				methodName = "onCheckedChanged";
	    				if(isChecked){
	    					logMessage = "ToggleButton is checked by " + contactData.getEmail();
	    					LogManager.LogInfoMsg(className, methodName, logMessage);
	    					Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
	    				} else {
	    					logMessage = "ToggleButton is unchecked " + contactData.getEmail();
	    					LogManager.LogInfoMsg(className, methodName, logMessage);
	    					Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
	    				}
	    			}
	    		});
      		}
      		if (viewHolder.checkBox != null){
				viewHolder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						PermissionsData p = DBLayer.getInstance().getPermissions(contactData.getEmail());
						if (p != null) {
							DBLayer.getInstance().updatePermissions(p.getEmail(), isChecked ? 1 : 0, p.getCommand(), p.getAdminCommand());
							Map<String, Object> m = new HashMap<String, Object>();
							m.put(DBConst.CONTACT_DEVICE_LOCATION_SHARING, isChecked ? 1 : 0);
							ContactDeviceDataListModel.getInstance().updateContactDeviceDataList(contactDeviceData, m);
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
						contactDeviceData.setFavorite(isChecked);
						Map<String, Object> m = new HashMap<String, Object>();
						m.put(DBConst.CONTACT_DEVICE_IS_FAVORITE, isChecked ? 1 : 0);
						ContactDeviceDataListModel.getInstance().updateContactDeviceDataList(contactDeviceData, m);						
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
		//viewHolder.statusImage.setEnabled(contactData.getContactStatus() != CommonConst.CONTACT_STATUS_START_CONNECT);		
		
		if (parent != null && parent instanceof ListView){
			SparseBooleanArray checked = ((ListView)parent).getCheckedItemPositions();
			if (checked != null){
				viewHolder.statusImage.setSelected(checked.get(position));
			}
		}
		
		if (viewHolder.checkBox != null) {								
			PermissionsData p = DBLayer.getInstance().getPermissions(contactData.getEmail());
			if(p != null){
				viewHolder.checkBox.setChecked(p.getIsLocationSharePermitted() == 1 ? true : false);
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

	public void setSecondaryContactStatus(int contactStatusPending) {
		mContactStatusPending = contactStatusPending;		
	}
}
