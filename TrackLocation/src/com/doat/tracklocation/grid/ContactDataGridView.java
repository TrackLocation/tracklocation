package com.doat.tracklocation.grid;

import com.doat.tracklocation.R;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;

public class ContactDataGridView extends Activity {

	private String className = this.getClass().getName();
	private String methodName;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        methodName = "onCreate";
		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);

		setContentView(R.layout.contact_grid_view);
        GridView gridview = (GridView) findViewById(R.id.gridview);  
        gridview.setAdapter(new ContactDataAdapter(this));
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		methodName = "onDestroy";
		LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_DESTROY] {" + className + "} -> " + methodName);
	}
}
