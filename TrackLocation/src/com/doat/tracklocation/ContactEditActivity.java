package com.doat.tracklocation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.doat.tracklocation.datatype.ContactDeviceData;
import com.doat.tracklocation.datatype.DeviceTypeEnum;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ContactEditActivity extends BaseActivity {
	private int REQUEST_CAMERA = 0;
	private int SELECT_FILE = 1;
	private EditText text_nick;
	private ContactDeviceData contactDeviceData;
	private TextView text_email;
	private EditText text_device_name;
	private ImageView iv_photo;
	private Spinner spn_device_type;
	protected String selectedDeviceTypeValue;
	private int contactPosition;    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_details);	
		final Intent intent = getIntent();
		contactPosition = intent.getExtras().getInt(CommonConst.CONTACT_LIST_SELECTED_VALUE);
		className = this.getClass().getName();
		methodName = "onCreate";
		
		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);

		contactDeviceData = intent.getExtras().getParcelable(CommonConst.JSON_STRING_CONTACT_DATA);
		
		text_email = (EditText) findViewById(R.id.fld_email);
		text_email.setText(contactDeviceData.getContactData().getEmail());
    
        text_nick = (EditText) findViewById(R.id.fld_nick); 
        text_nick.setText(contactDeviceData.getContactData().getNick());
        
        iv_photo = (ImageView) findViewById(R.id.img_contact); 
        Button btn_ChangeImage = (Button) findViewById(R.id.change_image); 
        btn_ChangeImage.setOnClickListener(imgButtonHandler); 
        
        Drawable contactPhoto = null;
        Bitmap bmp = contactDeviceData.getContactData().getContactPhoto();
        if (bmp == null){
        	bmp = Utils.getDefaultContactBitmap(getResources());
        }
        
        contactPhoto = new BitmapDrawable(this.getResources(), bmp);			
        iv_photo.setImageDrawable(contactPhoto);        
                
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
		BitmapDrawable drawable = (BitmapDrawable) iv_photo.getDrawable();
		contactDeviceData.getContactData().setContactPhoto(drawable.getBitmap());
		
		contactDeviceData.getDeviceData().setDeviceName(text_device_name.getText().toString());
		contactDeviceData.getDeviceData().setDeviceTypeEnum((DeviceTypeEnum) spn_device_type.getSelectedItem());
		if (DBLayer.getInstance().updateContactDeviceData(contactDeviceData) != -1){	
			Intent data = new Intent();
			data.putExtra(CommonConst.JSON_STRING_CONTACT_DATA, contactDeviceData);
			data.putExtra(CommonConst.CONTACT_LIST_SELECTED_VALUE, contactPosition);
	        setResult(RESULT_OK, data);				
		}
		else{
			setResult(RESULT_CANCELED);
		}
		finish();
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		methodName = "onDestroy";
		LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_DESTROY] {" + className + "} -> " + methodName);
	}
	
	View.OnClickListener imgButtonHandler = new View.OnClickListener() {

	    public void onClick(View v) {
	    	final CharSequence[] items = { "Take Photo", "Choose from Library",
            "Cancel" };

		    AlertDialog.Builder builder = new AlertDialog.Builder(ContactEditActivity.this);
		    builder.setTitle("Add Photo!");
		    builder.setItems(items, new DialogInterface.OnClickListener() {
		        @Override
		        public void onClick(DialogInterface dialog, int item) {
		            if (items[item].equals("Take Photo")) {
						Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
							startActivityForResult(takePictureIntent, REQUEST_CAMERA);
						}


		            } else if (items[item].equals("Choose from Library")) {
		                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		                intent.setType("image/*");
		                startActivityForResult(Intent.createChooser(intent, "Select File"), 1);
		            } else if (items[item].equals("Cancel")) {
		                dialog.dismiss();
		            }
		        }
		    });
		    builder.show();	    		    	
	    }
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 		
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CAMERA) {
				Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				thumbnail.compress(Bitmap.CompressFormat.PNG, 100, out);
				Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
				decoded = Utils.getResizedBitmap(decoded, iv_photo.getHeight(), iv_photo.getWidth());
				iv_photo.setImageBitmap(decoded);
			} 
			else if (requestCode == SELECT_FILE) {
				Uri selectedImageUri = data.getData();
				String[] projection = { MediaColumns.DATA };
				Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
				cursor.moveToFirst();
				String selectedImagePath = cursor.getString(column_index);
				Bitmap bm;
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				//BitmapFactory.decodeFile(selectedImagePath, options);
				final int REQUIRED_SIZE = 200;
				int scale = 1;
				while (options.outWidth / scale / 2 >= REQUIRED_SIZE && options.outHeight / scale / 2 >= REQUIRED_SIZE)
					scale *= 2;
				options.inSampleSize = scale;
				options.inJustDecodeBounds = false;
				bm = BitmapFactory.decodeFile(selectedImagePath, options);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.PNG, 100, out);
				Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
				decoded = Utils.getResizedBitmap(decoded, iv_photo.getHeight(), iv_photo.getWidth());
				iv_photo.setImageBitmap(decoded);
			}
		}		
	}
}
