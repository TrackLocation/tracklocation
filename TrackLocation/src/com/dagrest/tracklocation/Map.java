package com.dagrest.tracklocation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.dagrest.tracklocation.datatype.BroadcastActionEnum;
import com.dagrest.tracklocation.datatype.BroadcastConstEnum;
import com.dagrest.tracklocation.datatype.BroadcastData;
import com.dagrest.tracklocation.datatype.BroadcastKeyEnum;
import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.MessageDataContactDetails;
import com.dagrest.tracklocation.datatype.MessageDataLocation;
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
import android.util.Log;
import android.widget.Toast;

public class Map extends Activity implements LocationListener{

	// Max time of waiting dialog displaying - 30 seconds
	private final static int MAX_SHOW_TIME_WAITING_DIALOG = 30000; 
	private final static float DEFAULT_CAMERA_UPDATE = 15;
	
	private LocationManager locationManager;
	private LatLng lastKnownLocation;
	private LatLng latLngChanging;
	private BroadcastReceiver gcmLocationUpdatedWatcher;
	private GoogleMap map;
	private LinkedHashMap<String, Marker> markerMap = null;
	private LinkedHashMap<String, Circle> locationCircleMap = null;
	private float zoom;
	private ContactDeviceDataList selectedContactDeviceDataList;
	private List<String> selectedAccountList;
	//private ScaleGestureDetector detector;
	private int contactsQuantity;
	private boolean isShowAllMarkersEnabled;
	private ProgressDialog waitingDialog;
	private Gson gson;
	
	private Controller controller;
	
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

		gson = new Gson();
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String jsonStringContactDeviceDataList = null;
		if(bundle.containsKey(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST)){
			jsonStringContactDeviceDataList = intent.getExtras().getString(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA_LIST);
			selectedContactDeviceDataList = 
				gson.fromJson(jsonStringContactDeviceDataList, ContactDeviceDataList.class);
			if(selectedContactDeviceDataList != null && !selectedContactDeviceDataList.getContactDeviceDataList().isEmpty()){
				contactsQuantity = selectedContactDeviceDataList.getContactDeviceDataList().size();
				// Create and fill all requested accounts shat should be shown on the location map
				selectedAccountList = new ArrayList<String>();
				for (ContactDeviceData contactDeviceData : selectedContactDeviceDataList.getContactDeviceDataList()) {
					ContactData contactData = contactDeviceData.getContactData();
					if(contactData != null){
						selectedAccountList.add(contactData.getEmail());
					}
				}
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
        
        zoom = DEFAULT_CAMERA_UPDATE;
        if(lastKnownLocation != null){
	        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
	        		lastKnownLocation, zoom));
        }
        
//        Marker marker = map.addMarker(new MarkerOptions()
////        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))
//        .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
//        .position(lastKnownLocation));
        
        controller = new Controller();
        controller.keepAliveTrackLocationService(getApplicationContext(), selectedContactDeviceDataList, 
        	CommonConst.KEEP_ALIVE_TIMER_REQUEST_FROM_MAP_DELAY);
        
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
	    intentFilter.addAction(BroadcastActionEnum.BROADCAST_LOCATION_UPDATED.toString());
	    gcmLocationUpdatedWatcher = new BroadcastReceiver() 
	    {
	    	@Override
    		public void onReceive(Context context, Intent intent) {
	    		
	    		zoom = map.getCameraPosition().zoom;
	    		
	    		LogManager.LogInfoMsg("ContactConfiguration", "initGcmIntentServiceWatcher->onReceive", "WORK");
	    		
	    		Bundle bundle = intent.getExtras();
	    		String broadcastKeyLocationUpdated = BroadcastKeyEnum.location_updated.toString();
	    		// ===========================================
	    		// broadcast key = location_updated
	    		// ===========================================
	    		if(bundle != null && bundle.containsKey(broadcastKeyLocationUpdated)){
		    		String locationUpdatedDetails = bundle.getString(broadcastKeyLocationUpdated);
		    		
		    		if(locationUpdatedDetails != null && !locationUpdatedDetails.isEmpty()){
			    		List<String> resultList = Utils.splitLine(locationUpdatedDetails, CommonConst.DELIMITER_STRING);
			    		
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
					    		
					    		// Check locationDetails length to avoid crash in case less then 6 parameters
					    		if(locationDetails.length >= 6) {
						    		// Get account(contact) that sent its updated location on the map
						    		String updatingAccount = locationDetails[5];
						    		// Show on map only requested accounts(contacts) from Locate screen
						    		if(selectedAccountList != null && selectedAccountList.contains(updatingAccount)){
						    			// Set marker on the map
						    			Controller.setMapMarker(map, locationDetails, markerMap, locationCircleMap);
						    		}
					    		} else {
					    			// A way to try to set marker on the map if something went wrong...
					    			Controller.setMapMarker(map, locationDetails, markerMap, locationCircleMap);
					    		}
					    		
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
					    			
						    		// Check locationDetails length to avoid crash in case less then 6 parameters
						    		if(locationDetails.length >= 6) {
							    		// Get account(contact) that sent its updated location on the map
							    		String updatingAccount = locationDetails[5];
							    		// Update map's camera only for requested accounts(contacts) from Locate screen
							    		if(selectedAccountList != null && selectedAccountList.contains(updatingAccount)){

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
						    		}
					    		}
					    		if(markerMap != null && markerMap.size() == contactsQuantity){
					    			waitingDialog.dismiss();
					    		}
				    		}
			    		}
		    		}
	    		} // if(bundle != null && bundle.containsKey(broadcastKeyLocationUpdated))
	    		else if(bundle != null && bundle.containsKey(BroadcastConstEnum.data.toString())){
	    			String jsonLocationUpdatedData = bundle.getString(BroadcastConstEnum.data.toString());
	    			if(jsonLocationUpdatedData == null || jsonLocationUpdatedData.isEmpty()){
	    				return;
	    			}
	    			BroadcastData broadcastData = gson.fromJson(jsonLocationUpdatedData, BroadcastData.class);
	    			if(broadcastData == null){
	    				return;
	    			}
	    			
	    			// TODO:  Create a new function in Controller class
	    			// ================================================
	    			MessageDataContactDetails сontactDetails = broadcastData.getContactDetails();
	    			// TODO: Check that сontactDetails are not null
	    			MessageDataLocation locationDetails = broadcastData.getLocation();
	    			// TODO: Check that locationDetails are not null
	    			
	    			String updatingAccount = сontactDetails.getAccount();
	    			
		    		if(markerMap == null){
		    			markerMap = new LinkedHashMap<String, Marker>();
		    		}
		    		if(locationCircleMap == null){
		    			locationCircleMap = new LinkedHashMap<String, Circle>();
		    		}

		    		if(selectedAccountList != null && selectedAccountList.contains(updatingAccount)){
		    			// Set marker on the map
		    			Controller.setMapMarker(map, сontactDetails, locationDetails, markerMap, locationCircleMap);
		    		}

		    		
		    		
		    		
		    		// TODO: Create another function ???
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
		    			
				    		// Update map's camera only for requested accounts(contacts) from Locate screen
				    		if(selectedAccountList != null && selectedAccountList.contains(updatingAccount)){

				    			if(locationDetails != null) {
				    	    		double lat = locationDetails.getLat();
				    	    		double lng = locationDetails.getLng();
					    			latLngChanging = new LatLng(lat, lng);
					    			map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngChanging, zoom));
					    			if(markerMap.size() >= contactsQuantity){
					    				isShowAllMarkersEnabled = false;
					    			}
				    			}
				    		}
		    		}
		    		if(markerMap != null && markerMap.size() == contactsQuantity){
		    			waitingDialog.dismiss();
		    		}

		    		
		    		
		    		
		    		
	    			// ================================================
		    		
	    		}
	    		
    		}
	    };
	    
	    registerReceiver(gcmLocationUpdatedWatcher, intentFilter);
	    LogManager.LogFunctionExit("ContactConfiguration", "initGcmIntentServiceWatcher");
    }
	
    @Override
    protected void onDestroy() {
    	super.onDestroy();
//    	if(selectedContactDeviceDataList != null && !selectedContactDeviceDataList.getContactDeviceDataList().isEmpty()){
//    		Controller.sendCommand(getApplicationContext(), selectedContactDeviceDataList, 
//    			CommandEnum.stop, null, null);
//    	}
    	controller.stopKeepAliveTrackLocationService();
    	
    	Log.i(CommonConst.LOG_TAG, "Timer with mapKeepAliveTimerJob - stopped");
    	if(gcmLocationUpdatedWatcher != null){
    		unregisterReceiver(gcmLocationUpdatedWatcher);
    	}
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

