package com.dagrest.tracklocation;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.dagrest.tracklocation.datatype.BroadcastCommandEnum;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Utils;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;

public class Map extends Activity implements LocationListener{

	private LocationManager locationManager;
	private LatLng lastKnownLocation;
	private LatLng latLngChanging;
	private BroadcastReceiver gcmIntentServiceChangeWatcher;
	private GoogleMap map;
	private Marker marker;
	private float zoom;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);	
		
		initGcmIntentServiceBroadcastReceiver();

		// Get a handle to the Map Fragment
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        setupLocation();
        
        zoom = 15;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
        		lastKnownLocation, zoom));
        
        marker = map.addMarker(new MarkerOptions()
//        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))
        .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
        .position(lastKnownLocation));
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
					    		String[] latLng = lanLngUpdated.split(CommonConst.DELIMITER_COMMA);
			
					    		if(latLng != null) {
						    		double lat = Double.parseDouble(latLng[0]);
						    		double lng = Double.parseDouble(latLng[1]);
						    		
						    		if(lat != 0 && lng != 0){
							    		latLngChanging = new LatLng(lat, lng);
							    		
							    		marker.remove();
							    		//marker.
							    		marker = map.addMarker(new MarkerOptions()
							            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))
							            .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
							            .position(latLngChanging));
							    		
							            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
							            		latLngChanging, zoom));
						    		}
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
    	unregisterReceiver(gcmIntentServiceChangeWatcher);
    }
}
