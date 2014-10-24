package com.dagrest.tracklocation;

import com.dagrest.tracklocation.datatype.BroadcastActionEnum;
import com.dagrest.tracklocation.datatype.BroadcastKeyEnum;
import com.dagrest.tracklocation.utils.CommonConst;

import android.app.Activity;
import android.content.Context;
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
//import android.widget.Toast;

public class ContactDeatilsExpandableListAdapter extends BaseExpandableListAdapter {

	private SparseArray<ContactDetails> contactDetailsGroups = null;
	private SparseArray<ContactDetails> contactDetailsGroupsOriginal = null;
	private LayoutInflater inflater;
	private Activity activity;
	private int groupPositionCurrent = -1;

	public ContactDeatilsExpandableListAdapter(Activity act,
			SparseArray<ContactDetails> contactDetailsGroups) {
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
		TextView text = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listrow_details, null);
		}
		text = (TextView) convertView.findViewById(R.id.textView1);
		text.setText(groupPhone);
		setGroupPositionCurrent(groupPosition);
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Toast.makeText(activity, children, Toast.LENGTH_SHORT).show();
				ContactDetails contactDetails = (ContactDetails) getGroup(getGroupPositionCurrent());
				String contactName = null;
				if(contactDetails != null && !contactDetails.getContactName().isEmpty()){
					contactName = contactDetails.getContactName();
				}
        		Controller.broadcastMessage(activity, 
        			BroadcastActionEnum.BROADCAST_JOIN.toString(), 
        			"OnChildClick",
        			null, 
					BroadcastKeyEnum.join_number.toString(), 
					contactName + CommonConst.DELIMITER_STRING + groupPhone);
			}
		});
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return contactDetailsGroups.get(groupPosition).getPhoneNumbersList()
				.size();
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
		ContactDetails group = (ContactDetails) getGroup(groupPosition);
		((CheckedTextView) convertView).setText(group.getContactName());
		//Add person image into the Join List 
		Bitmap bmp = group.getContactPhoto();
		if (bmp == null){
			bmp = BitmapFactory.decodeResource(convertView.getResources(), R.drawable.ic_launcher);
		}
		Drawable contactPhoto = new BitmapDrawable(convertView.getResources(), bmp);
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

		String contcatNameLowerCase = contcatName.toLowerCase();
		SparseArray<ContactDetails> contactDetailsGroupsNew = new SparseArray<ContactDetails>();

		if (contcatNameLowerCase == null || contcatNameLowerCase.isEmpty()) {
			contactDetailsGroups = contactDetailsGroupsOriginal.clone();
		} else {
			int j = 0;
			for (int i = 0; i < contactDetailsGroupsOriginal.size(); i++) {

				if (contactDetailsGroupsOriginal.get(i).getContactName().toLowerCase().contains(contcatNameLowerCase)) {
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
