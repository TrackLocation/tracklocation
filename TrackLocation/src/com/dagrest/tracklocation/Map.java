package com.dagrest.tracklocation;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.dagrest.tracklocation.datatype.BroadcastCommandEnum;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Utils;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;












//import com.google.android.gms.maps.*;
//import com.google.android.gms.maps.model.*;
import android.app.Activity;
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
import android.widget.Toast;

public class Map extends Activity implements LocationListener{

	private LocationManager locationManager;
	private LatLng lastKnownLocation;
	private LatLng latLngChanging;
	private BroadcastReceiver gcmIntentServiceChangeWatcher;
	private GoogleMap map;
	//private Marker marker;
	private LinkedHashMap<String, Marker> markerMap = null;
	private LinkedHashMap<String, Circle> locationCircleMap = null;
	//private Circle locationCircle;
	private float zoom;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);	

		Intent intent = getIntent();

		initGcmIntentServiceBroadcastReceiver();

		// Get a handle to the Map Fragment
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        setupLocation();
        
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
					    		
					    		setMapMarker(map, locationDetails, markerMap, locationCircleMap);

					    		map.moveCamera(CameraUpdateFactory.newLatLngZoom(
					            		lastKnownLocation, zoom));
				    		}
			    		}
		    		}
	    		}
    		}
	    };
	    registerReceiver(gcmIntentServiceChangeWatcher, intentFilter);
	    LogManager.LogFunctionExit("ContactConfiguration", "initGcmIntentServiceWatcher");
    }

	public static void setMapMarker(GoogleMap map, String[] locationDetails, 
			LinkedHashMap<String, Marker> markerMap, LinkedHashMap<String, Circle> locationCircleMap) {
		if(locationDetails != null) {
    		double lat = Double.parseDouble(locationDetails[0]);
    		double lng = Double.parseDouble(locationDetails[1]);
    		
    		if(lat != 0 && lng != 0){
				LatLng latLngChanging = new LatLng(lat, lng);

	    		String account = null;
	    		if(locationDetails.length == 6){
	    			account = locationDetails[5];
	    		}
	    		if(account == null || account.isEmpty()) {
	    			return;
	    		}
	    		
	    		if(markerMap.containsKey(account)) {
	    			markerMap.get(account).remove();
	    			markerMap.remove(account);
	    		}
	    		if(locationCircleMap.containsKey(account)) {
	    			locationCircleMap.get(account).remove();
	    			locationCircleMap.remove(account);
	    		}
	    		
				Marker marker = null;
				Circle locationCircle = null;
								
				//marker.
				marker = map.addMarker(new MarkerOptions()
		        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))
		        .snippet(account)
		        .title("Title")
		        .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
		        .position(latLngChanging));
				
				markerMap.put(account, marker);
				
				double accuracy = Double.parseDouble(locationDetails[2]);
		
				locationCircle = map.addCircle(new CircleOptions().center(latLngChanging)
				            .radius(accuracy)
				            .strokeColor(Color.argb(255, 0, 153, 255))
				            .fillColor(Color.argb(30, 0, 153, 255)).strokeWidth(2));
				locationCircleMap.put(account, locationCircle);
    		}
    	}
	}
	
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	unregisterReceiver(gcmIntentServiceChangeWatcher);
    }

}
