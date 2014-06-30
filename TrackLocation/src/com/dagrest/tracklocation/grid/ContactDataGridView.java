package com.dagrest.tracklocation.grid;

import com.dagrest.tracklocation.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

public class ContactDataGridView extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_grid_view);
        GridView gridview = (GridView) findViewById(R.id.gridview);  
        gridview.setAdapter(new ContactDataAdapter(this));
    }
}
