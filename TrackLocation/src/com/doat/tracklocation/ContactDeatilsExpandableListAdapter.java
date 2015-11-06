package com.doat.tracklocation;

import java.util.Locale;

import com.doat.tracklocation.R;
import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.ContactData;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Utils;
import com.doat.tracklocation.utils.ViewHolder;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class ContactDeatilsExpandableListAdapter extends BaseExpandableListAdapter {

	private SparseArray<ContactData> contactDetailsGroups = null;
	private SparseArray<ContactData> contactDetailsGroupsOriginal = null;
	private LayoutInflater inflater;
	private Activity activity;
	private int groupPositionCurrent = -1;

	public ContactDeatilsExpandableListAdapter(Activity act, SparseArray<ContactData> contactDetailsGroups) {
		activity = act;
		this.contactDetailsGroups = contactDetailsGroups.clone();
		this.contactDetailsGroupsOriginal = contactDetailsGroups.clone();
		inflater = act.getLayoutInflater();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return contactDetailsGroups.get(groupPosition).getPhoneNumbersList()
				.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		final String groupPhone = (String) getChild(groupPosition, childPosition);
		
		View row = convertView;
		if (row == null){
			row = inflater.inflate(R.layout.listrow_details, null);
			TextView text = (TextView) row.findViewById(R.id.textView1);
			text.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_contact_phone_black_24dp, 0, 0, 0);
			//View photo = row.findViewById(R.id.ic_launcher);
			ViewHolder holder = new ViewHolder();
            holder.addView(text);
            //holder.addView(photo);
            row.setTag(holder);
		}
		
		ViewHolder holder = (ViewHolder) row.getTag();
        TextView text = (TextView) holder.getView(R.id.textView1);        
		text.setText(groupPhone);
		setGroupPositionCurrent(groupPosition);
		row.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				ContactData contactDetails = (ContactData) getGroup(getGroupPositionCurrent());
				String contactName = null;
				if(contactDetails != null && !contactDetails.getNick().isEmpty()){
					contactName = contactDetails.getNick();
				}
        		Controller.broadcastMessage(activity, 
        			BroadcastActionEnum.BROADCAST_JOIN.toString(), 
        			"OnChildClick",
        			null, 
					BroadcastKeyEnum.join_number.toString(), 
					contactName + CommonConst.DELIMITER_STRING + groupPhone);
			}
		});
		return row;
	}
	
	@Override
	public int getChildrenCount(int groupPosition) {
		return contactDetailsGroups.get(groupPosition).getPhoneNumbersList().size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return contactDetailsGroups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return contactDetailsGroups.size();
	}

	@Override
	public void onGroupCollapsed(int groupPosition) {
		super.onGroupCollapsed(groupPosition);
	}

	@Override
	public void onGroupExpanded(int groupPosition) {
		super.onGroupExpanded(groupPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listrow_group, null);
		}
		ContactData group = (ContactData) getGroup(groupPosition);
		((CheckedTextView) convertView).setText(group.getNick());
		//Add person image into the Join List 
		Bitmap bmp = group.getContactPhoto();
		if (bmp == null){
			bmp = BitmapFactory.decodeResource(convertView.getResources(), R.drawable.ic_launcher);
		}
		Drawable contactPhoto = new BitmapDrawable(convertView.getResources(), Utils.getRoundedCornerImage(bmp));
		contactPhoto.setBounds( 0, 0, 120, 120 );
		((CheckedTextView) convertView).setCompoundDrawables(null,null,contactPhoto, null);
		
		((CheckedTextView) convertView).setChecked(isExpanded);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	public void filterData(final String contcatName) {

		String contcatNameLowerCase = contcatName.toLowerCase(Locale.getDefault());
		SparseArray<ContactData> contactDetailsGroupsNew = new SparseArray<ContactData>();

		if (contcatNameLowerCase == null || contcatNameLowerCase.isEmpty()) {
			contactDetailsGroups = contactDetailsGroupsOriginal.clone();
		} else {
			int j = 0;
			for (int i = 0; i < contactDetailsGroupsOriginal.size(); i++) {

				if (contactDetailsGroupsOriginal.get(i).getNick().toLowerCase().contains(contcatNameLowerCase)) {
					contactDetailsGroupsNew.append(j,
							contactDetailsGroupsOriginal.get(i));
					j++;
				}
			}
			if (contactDetailsGroupsNew.size() > 0) {
				contactDetailsGroups = contactDetailsGroupsNew.clone();
			} else {
				contactDetailsGroups.clear();
			}

		}

		notifyDataSetChanged();
	}

	public int getGroupPositionCurrent() {
		return groupPositionCurrent;
	}

	public void setGroupPositionCurrent(int groupPositionCurrent) {
		this.groupPositionCurrent = groupPositionCurrent;
	}
} 
