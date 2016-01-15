package com.doat.tracklocation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.doat.tracklocation.datatype.ActionMenuObj;

import java.util.ArrayList;

public class MenuActionListAdapter extends BaseAdapter{
	Context mContext;
	private ArrayList<ActionMenuObj> mActionsList;

	public MenuActionListAdapter(Context context, ArrayList<ActionMenuObj> actionsList) {
		mContext = context;
		mActionsList = actionsList;
	}

	private static class ViewHolder {
		TextView textView;
		ImageView image;
	}

	@Override
	public int getCount() {
		return mActionsList.size();
	}

	@Override
	public Object getItem(int position) {
		return mActionsList.get(position);
	}

	@Override
	public long getItemId(int position) {		
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		ViewHolder viewHolder = null;
		view = convertView;
		if (view == null){
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.map_drawer_list_item, null);
			viewHolder = new ViewHolder(); 
			viewHolder.textView = (TextView) view.findViewById(android.R.id.text1);
			viewHolder.image = (ImageView) view.findViewById(R.id.icon);
			view.setTag(viewHolder);
		}
		else{
			viewHolder = (ViewHolder) view.getTag();
		}
		viewHolder.textView.setText(mActionsList.get(position).getCaption());
		if (mActionsList.get(position).getIcon() != -1){
			viewHolder.image.setImageResource(mActionsList.get(position).getIcon());
		}
		
		return view;
	}
}
