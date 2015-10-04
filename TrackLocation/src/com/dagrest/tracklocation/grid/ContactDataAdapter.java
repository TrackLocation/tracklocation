package com.dagrest.tracklocation.grid;

import java.util.List;

import com.dagrest.tracklocation.R;
import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.DeviceData;
import com.dagrest.tracklocation.db.DBLayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ContactDataAdapter extends BaseAdapter {

    Context mContext;
    private String [] phoneArray;
    private String [] accountArray;
    private String [] macAddressArray;
    private LayoutInflater mInflater;

    public ContactDataAdapter(Context context){
        mContext=context;
        mInflater = LayoutInflater.from(context);
        
		ContactDeviceDataList contDevDataList = DBLayer.getInstance().getContactDeviceDataList(null);
		
		int contactDeviceQuantity = contDevDataList.size();
		if( contactDeviceQuantity > 0){
		
			phoneArray = new String[contactDeviceQuantity];
			accountArray = new String[contactDeviceQuantity];
			macAddressArray = new String[contactDeviceQuantity];
			
			int i = 0;
			for (ContactDeviceData contactDeviceData : contDevDataList) {	
				String phoneNumber = contactDeviceData.getPhoneNumber();
				phoneArray[i] = contactDeviceData.getPhoneNumber();
				ContactData contactData = contactDeviceData.getContactData();
				DeviceData deviceData = contactDeviceData.getDeviceData();
				String account = null;
				if( contactData != null ){
					account = contactData.getEmail();
					accountArray[i] = contactData.getEmail();
				}
				String macAddress = null;
				if( deviceData != null ){
					macAddress = deviceData.getDeviceMac();
					macAddressArray[i] = deviceData.getDeviceMac();
				}
				i++;
			}
		}
    }
    
    @Override
	public int getCount() {
    	return phoneArray.length;
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
			convertView = mInflater.inflate(R.layout.contact_grid, parent,false);
			
			holder = new ViewHolder();
			holder.txtPhone=(TextView)convertView.findViewById(R.id.txtPhone);
			holder.txtPhone.setPadding(20, 10,10 , 10);
			holder.txtEmail=(TextView)convertView.findViewById(R.id.txtEmail);
			holder.txtEmail.setPadding(20, 10, 10, 10);
			holder.txtMacAddress=(TextView)convertView.findViewById(R.id.txtMacAddress);
			holder.txtMacAddress.setPadding(20, 10, 10, 10);
			if(position==0)
			{                             
			      convertView.setTag(holder);
			}
        }
        else
        {
               holder = (ViewHolder) convertView.getTag();
        }
        holder.txtPhone.setText(phoneArray[position]);
        holder.txtEmail.setText(accountArray[position]);
        holder.txtMacAddress.setText(macAddressArray[position]);
        return convertView;
	}
	
    static class ViewHolder
    {        
		TextView txtPhone;        
		TextView txtEmail;      
		TextView txtMacAddress;
    }

}
