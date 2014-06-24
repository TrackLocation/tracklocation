package com.dagrest.tracklocation;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import com.dagrest.tracklocation.datatype.BroadcastCommandEnum;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Utils;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class Map extends Activity implements LocationListener{

	// Max time of waiting dialog displaying - 30 seconds
	private final static int MAX_SHOW_TIME_WAITING_DIALOG = 30000; 
	
	private LocationManager locationManager;
	private LatLng lastKnownLocation;
	private LatLng latLngChanging;
	private BroadcastReceiver gcmLocationUpdatedWatcher;
	private GoogleMap map;
	private LinkedHashMap<String, Marker> markerMap = null;
	private LinkedHashMap<String, Circle> locationCircleMap = null;
	private float zoom;
	private ContactDeviceDataList selectedContactDeviceDataList;
	//private ScaleGestureDetector detector;
	private int contactsQuantity;
	private boolean isShowAllMarkersEnabled;
	ProgressDialog waitingDialog;
	
	public void launchWaitingDialog() {
        waitingDialog = new ProgressDialog(Map.this);
        waitingDialog.setTitle("Tracking location");
        waitingDialog.setMessage("Please wait ...");
        waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //progressDialog.setProgress(0);
        //progressDialog.setMax(contactsQuantity);
        waitingDialog.setCancelable(false);
        waitingDialog.setIndeterminate(true);
        waitingDialog.show();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
            	if(markerMap != null && markerMap.size() >= contactsQuantity) {
            		waitingDialog.dismiss();
            	}
            	try {
					Thread.sleep(MAX_SHOW_TIME_WAITING_DIALOG); 
					waitingDialog.dismiss();
				} catch (InterruptedException e) {
					waitingDialog.dismiss();
				}
            }
        }).start();
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
		}
		isShowAllMarkersEnabled = true;

		launchWaitingDialog();
		
		initGcmLocationUpdatedBroadcastReceiver();

		// Get a handle to the Map Fragment
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        setupLocation();

        String accountsListMsg = "Waiting for:\n\n";
        for (ContactDeviceData contactDeviceData : selectedContactDeviceDataList.getContactDeviceDataList()) {
        	accountsListMsg = accountsListMsg + contactDeviceData.getContactData().getEmail() + "\n";
		}
        waitingDialog.setMessage(accountsListMsg);
        
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

	private void initGcmLocationUpdatedBroadcastReceiver()
    {
    	LogManager.LogFunctionCall("ContactConfiguration", "initGcmIntentServiceWatcher");
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(CommonConst.BROADCAST_LOCATION_UPDATED);
	    gcmLocationUpdatedWatcher = new BroadcastReceiver() 
	    {
	    	@Override
    		public void onReceive(Context context, Intent intent) {
	    		
	    		zoom = map.getCameraPosition().zoom;
	    		
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
					    		
					    		if(markerMap != null && markerMap.size() > 1 && isShowAllMarkersEnabled == true) {
					    			// put camera to show all markers
					    			CameraUpdate cu = Controller.createCameraUpdateLatLngBounds(markerMap);
						    		map.animateCamera(cu); // or map.moveCamera(cu); 
						    		if(markerMap.size() >= contactsQuantity){
						    			// put camera to show all markers
						    			cu = Controller.createCameraUpdateLatLngBounds(markerMap);
							    		map.animateCamera(cu); // or map.moveCamera(cu); 
						    			isShowAllMarkersEnabled = false;
						    		}
					    		} else if(markerMap != null && markerMap.size() == 1 && isShowAllMarkersEnabled == true) {
					    			if(locationDetails != null) {
					    	    		double lat = Double.parseDouble(locationDetails[0]);
					    	    		double lng = Double.parseDouble(locationDetails[1]);
						    			latLngChanging = new LatLng(lat, lng);
						    			map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngChanging, zoom));
						    			if(markerMap.size() >= contactsQuantity){
						    				isShowAllMarkersEnabled = false;
						    			}
					    			}
					    		}
					    		if(markerMap != null && markerMap.size() == contactsQuantity){
					    			waitingDialog.dismiss();
					    		}
				    		}
			    		}
		    		}
	    		}
    		}
	    };
	    registerReceiver(gcmLocationUpdatedWatcher, intentFilter);
	    LogManager.LogFunctionExit("ContactConfiguration", "initGcmIntentServiceWatcher");
    }
	
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if(selectedContactDeviceDataList != null && !selectedContactDeviceDataList.getContactDeviceDataList().isEmpty()){
    		Controller.sendCommand(getApplicationContext(), selectedContactDeviceDataList, CommandEnum.stop);
    	}
    	unregisterReceiver(gcmLocationUpdatedWatcher);
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

