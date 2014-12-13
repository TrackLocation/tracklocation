package com.dagrest.tracklocation;

import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.DeviceTypeEnum;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.utils.CommonConst;
import com.google.gson.Gson;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ContactEdit extends Activity {
	private EditText text_nick;
	private ContactDeviceData contactDeviceData;
	private EditText text_first_name;
	private EditText text_last_name;
	private TextView text_email;
	private EditText text_device_name;
	private Spinner spn_device_type;
	protected String selectedDeviceTypeValue;
	private int contactPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_details);	
		final Intent intent = getIntent();
		contactPosition = intent.getExtras().getInt(CommonConst.CONTACT_LIST_SELECTED_VALUE);
		
		Gson gson = new Gson();
		String jsonStringContactData = intent.getExtras().getString(CommonConst.JSON_STRING_CONTACT_DATA);
		ContactData contactData = gson.fromJson(jsonStringContactData, ContactData.class);
		
		contactDeviceData = DBLayer.getInstance().getContactDeviceDataCursor(contactData.getEmail());
		
		text_email = (EditText) findViewById(R.id.fld_email);
		text_email.setText(contactDeviceData.getContactData().getEmail());
    
        text_nick = (EditText) findViewById(R.id.fld_nick); 
        text_nick.setText(contactDeviceData.getContactData().getNick());
        
        text_first_name = (EditText) findViewById(R.id.fld_first_name); 
        text_first_name.setText(contactDeviceData.getContactData().getFirstName());
        
        text_last_name = (EditText) findViewById(R.id.fld_last_name); 
        text_last_name.setText(contactDeviceData.getContactData().getLastName());        
        
        text_device_name = (EditText) findViewById(R.id.fld_devicename); 
        text_device_name.setText(contactDeviceData.getDeviceData().getDeviceName()); 
        
        spn_device_type = (Spinner) findViewById(R.id.devicetype);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<DeviceTypeEnum> adapter = new ArrayAdapter<DeviceTypeEnum>(this, android.R.layout.simple_spinner_item, DeviceTypeEnum.values());
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	     // Apply the adapter to the spinner
        spn_device_type.setAdapter(adapter);            
        int pos = adapter.getPosition( contactDeviceData.getDeviceData().getDeviceTypeEnum());
        spn_device_type.setSelection(pos);     
	}
	
	public void submitUpdateResult(View V)
    {
		contactDeviceData.getContactData().setNick(text_nick.getText().toString());
		contactDeviceData.getContactData().setFirstName(text_first_name.getText().toString());
		contactDeviceData.getContactData().setLastName(text_last_name.getText().toString());
		contactDeviceData.getDeviceData().setDeviceName(text_device_name.getText().toString());
		contactDeviceData.getDeviceData().setDeviceTypeEnum((DeviceTypeEnum) spn_device_type.getSelectedItem());
		if (DBLayer.getInstance().updateContactDeviceData(contactDeviceData) != -1){	
			Intent data = new Intent();
			data.putExtra(CommonConst.JSON_STRING_CONTACT_DATA, new Gson().toJson(contactDeviceData.getContactData()));
			data.putExtra(CommonConst.CONTACT_LIST_SELECTED_VALUE, contactPosition);
	        setResult(RESULT_OK, data);				
		}
		else{
			setResult(RESULT_CANCELED);
		}
		finish();
    }
	
	
	
}
