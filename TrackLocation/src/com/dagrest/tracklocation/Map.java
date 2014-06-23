package com.dagrest.tracklocation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.dagrest.tracklocation.datatype.BroadcastCommandEnum;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Utils;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;














import com.google.gson.Gson;












//import com.google.android.gms.maps.*;
//import com.google.android.gms.maps.model.*;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

public class Map extends Activity implements LocationListener{

	private LocationManager locationManager;
	private LatLng lastKnownLocation;
	private LatLng latLngChanging;
	private BroadcastReceiver gcmIntentServiceChangeWatcher;
	private GoogleMap map;
	//private Marker marker;
	private LinkedHashMap<String, Marker> markerMap = null;
	private LinkedHashMap<String, Boolean> accountsList;
	private LinkedHashMap<String, Circle> locationCircleMap = null;
	//private Circle locationCircle;
	private float zoom;
	private ContactDeviceDataList selectedContactDeviceDataList;
	private ScaleGestureDetector detector;
	private int contactsQuantity;
	private boolean isShowAllMarkersEnabled;
	ProgressDialog barProgressDialog;
	
	public void launchBarDialog() {
        barProgressDialog = new ProgressDialog(Map.this);
        barProgressDialog.setTitle("Tracking location");
        barProgressDialog.setMessage("Please wait ...");
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
        barProgressDialog.setProgress(0);
        barProgressDialog.setMax(contactsQuantity);
        barProgressDialog.show();
        
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//            	//Controller.fetchContacts(Map.this, contactDetailsGroups, barProgressDialog);
//            	if(markerMap != null && markerMap.size() >= contactsQuantity) {
//            		barProgressDialog.dismiss();
//            	}
//            	System.out.println("===============>  Test... <==================");
////            	try {
////					this.wait(1000);
////				} catch (InterruptedException e) {
////					e.printStackTrace();
////					Log.e(CommonConst.LOG_TAG, "Wait exception...", e);
////				}
//            }
//        }).start();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);	

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String jsonStringContactDeviceDataList = null;
		if(bundle.containsKey(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST)){
			jsonStringContactDeviceDataList = intent.getExtras().getString(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST);
			selectedContactDeviceDataList = 
				new Gson().fromJson(jsonStringContactDeviceDataList, ContactDeviceDataList.class);
			if(selectedContactDeviceDataList != null && !selectedContactDeviceDataList.getContactDeviceDataList().isEmpty()){
				contactsQuantity = selectedContactDeviceDataList.getContactDeviceDataList().size();
			}
			isShowAllMarkersEnabled = true;
		}

		launchBarDialog();
		initGcmIntentServiceBroadcastReceiver();

		// Get a handle to the Map Fragment
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        setupLocation();
        //selectedContactDeviceDataList
        String accountsListMsg = "Wainting for:\n";
        accountsList = new LinkedHashMap<String, Boolean>();
        for (ContactDeviceData contactDeviceData : selectedContactDeviceDataList.getContactDeviceDataList()) {
        	accountsListMsg = accountsListMsg + contactDeviceData.getContactData().getEmail() + "\n";
        	accountsList.put(contactDeviceData.getContactData().getEmail(), true);
		}
        barProgressDialog.setMessage(accountsListMsg);
        
        zoom = 15;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
        		lastKnownLocation, zoom));
        
//        Marker marker = map.addMarker(new MarkerOptions()
////        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))
//        .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
//        .position(lastKnownLocation));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	 
	private void setupLocation() {
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		// try to get our last known location
		Location location = getLastKnownLocation();
		if (location != null) {
			lastKnownLocation = new LatLng(location.getLatitude(),
					location.getLongitude());
		} else {
			Toast.makeText(Map.this, "getString(R.string.gps_connection_lost)",
					Toast.LENGTH_LONG).show();
		}

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				0,0, Map.this); 
	}
	
	private Location getLastKnownLocation() {
		Location location = null;
		final Iterator<String> locationProviders = locationManager
				.getProviders(new Criteria(), true).iterator();

		while (locationProviders.hasNext()) {
			final Location lastKnownLocation = locationManager
					.getLastKnownLocation(locationProviders.next());

			if (location == null || (lastKnownLocation != null)) {
				location = lastKnownLocation;
			}
		}
		return location;
	}
	
	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			lastKnownLocation = new LatLng(location.getLatitude(), location.getLongitude());
		}
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	private void initGcmIntentServiceBroadcastReceiver()
    {
    	LogManager.LogFunctionCall("ContactConfiguration", "initGcmIntentServiceWatcher");
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(CommonConst.BROADCAST_LOCATION_UPDATED);
	    gcmIntentServiceChangeWatcher = new BroadcastReceiver() 
	    {
	    	@Override
    		public void onReceive(Context context, Intent intent) {
	    		
	    		zoom = map.getCameraPosition().zoom;
	    		
    			// TODO Auto-generated method stub
	    		LogManager.LogInfoMsg("ContactConfiguration", "initGcmIntentServiceWatcher->onReceive", "WORK");
	    		
	    		Bundle bundle = intent.getExtras();
	    		String broadcastKeyLocationUpdated = BroadcastCommandEnum.location_updated.toString();
	    		// ===========================================
	    		// broadcast key = location_updated
	    		// ===========================================
	    		if(bundle != null && bundle.containsKey(broadcastKeyLocationUpdated)){
		    		String result = bundle.getString(broadcastKeyLocationUpdated);
		    		
		    		if(result != null && !result.isEmpty()){
			    		List<String> resultList = Utils.splitLine(result, CommonConst.DELIMITER_STRING);
			    		
			    		if(resultList != null && resultList.size() >= 2){
				    		String lanLngUpdated = resultList.get(1);
				    		
				    		if( lanLngUpdated != null && !lanLngUpdated.isEmpty() ){
					    		String[] locationDetails = lanLngUpdated.split(CommonConst.DELIMITER_COMMA);
					    		
					    		if(markerMap == null){
					    			markerMap = new LinkedHashMap<String, Marker>();
					    		}
					    		if(locationCircleMap == null){
					    			locationCircleMap = new LinkedHashMap<String, Circle>();
					    		}
					    		
					    		Controller.setMapMarker(map, locationDetails, markerMap, locationCircleMap);
					    		barProgressDialog.setProgress(markerMap.size());
					    		String accountsListMsg = "Wainting for:\n";
					    		
					    		for (Entry<String,Boolean> account : accountsList.entrySet()) {
					    			if(markerMap.containsKey(account.getKey())){
					    				account.setValue(false);
					    			} else {
					    				if(account.getValue() == true){
					    					accountsListMsg = accountsListMsg + account.getKey() + "\n";
					    				}
					    			}
								}
					    		barProgressDialog.setMessage(accountsListMsg);
					    		
					    		LatLngBounds.Builder builder = new LatLngBounds.Builder();
					    		for (LinkedHashMap.Entry<String,Marker> markerEntry : markerMap.entrySet()) {
					    			Marker m = markerEntry.getValue();
					    			if(m != null){
						    			builder.include(m.getPosition());
					    			}
					    		}
					    		LatLngBounds bounds = builder.build();
					    		
					    		if(markerMap != null && markerMap.size() > 1 && isShowAllMarkersEnabled == true) {
					    			// put camera to show all markers
						    		int padding = 50; // offset from edges of the map in pixels
						    		CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
						    		map.animateCamera(cu); // or map.moveCamera(cu); 
						    		if(markerMap.size() == contactsQuantity){
						    			isShowAllMarkersEnabled = false;
						    			barProgressDialog.dismiss();
						    		}
					    		} else {
					    			map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, zoom));
					    			barProgressDialog.dismiss();
					    		}
				    		}
			    		}
		    		}
	    		}
    		}
	    };
	    registerReceiver(gcmIntentServiceChangeWatcher, intentFilter);
	    LogManager.LogFunctionExit("ContactConfiguration", "initGcmIntentServiceWatcher");
    }
	
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if(selectedContactDeviceDataList != null && !selectedContactDeviceDataList.getContactDeviceDataList().isEmpty()){
    		Controller.sendCommand(getApplicationContext(), selectedContactDeviceDataList, CommandEnum.stop);
    	}
    	unregisterReceiver(gcmIntentServiceChangeWatcher);
    }
    
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
//        // TODO Auto-generated method stub
//        super.dispatchTouchEvent(motionEvent);
//        final int action = motionEvent.getAction();       
//        final int fingersCount = motionEvent.getPointerCount();        
//        
//        if ((action == MotionEvent.ACTION_POINTER_UP) && (fingersCount == 2)) {             
//            //onTwoFingersTap(); 
//        	System.out.println("Two fingers");
//            return true;         
//        } 
//        return true; //detector.onTouchEvent(motionEvent);     
//    }
}

