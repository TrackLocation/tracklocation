package com.dagrest.tracklocation.grid;

import com.dagrest.tracklocation.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ContactDataAdapter extends BaseAdapter {

    Context mContext;
    private String [] id = {"01","02","03"};
    private String [] name={"David", "Larisa", "Nastya"};
    private LayoutInflater mInflater;

    public ContactDataAdapter(Context context){
        mContext=context;
        mInflater = LayoutInflater.from(context);
    }
    
    @Override
	public int getCount() {
    	return id.length;
    }

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        if(convertView==null)
        {
               convertView = mInflater.inflate(R.layout.customgrid, parent,false);
               holder = new ViewHolder();
               holder.txtId=(TextView)convertView.findViewById(R.id.txtIdField);
               holder.txtId.setPadding(100, 10,10 , 10);
               holder.txtName=(TextView)convertView.findViewById(R.id.txtNameField);
               holder.txtName.setPadding(100, 10, 10, 10);
               if(position==0)
               {                             
                     convertView.setTag(holder);
               }
        }
        else
        {
               holder = (ViewHolder) convertView.getTag();
        }
        holder.txtId.setText(id[position]);
        holder.txtName.setText(name[position]);
        return convertView;
	}
	
    static class ViewHolder
    {        
           TextView txtId;        
           TextView txtName;               
    }

}
