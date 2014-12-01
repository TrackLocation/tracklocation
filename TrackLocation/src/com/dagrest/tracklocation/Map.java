package com.dagrest.tracklocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import com.dagrest.tracklocation.concurrent.StartTrackLocationService;
import com.dagrest.tracklocation.datatype.BroadcastActionEnum;
import com.dagrest.tracklocation.datatype.BroadcastConstEnum;
import com.dagrest.tracklocation.datatype.BroadcastData;
import com.dagrest.tracklocation.datatype.BroadcastKeyEnum;
import com.dagrest.tracklocation.datatype.CommandKeyEnum;
import com.dagrest.tracklocation.datatype.CommandValueEnum;
import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.MessageDataContactDetails;
import com.dagrest.tracklocation.datatype.MessageDataLocation;
import com.dagrest.tracklocation.datatype.NotificationBroadcastData;
import com.dagrest.tracklocation.dialog.CommonDialog;
import com.dagrest.tracklocation.dialog.IDialogOnClickAction;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.dagrest.tracklocation.utils.Utils;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.gson.Gson;
import com.dagrest.tracklocation.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Map extends Activity implements LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, OnTouchListener{

	// Max time of waiting dialog displaying - 30 seconds
	private final static int MAX_SHOW_TIME_WAITING_DIALOG = 30000; 
	private final static float DEFAULT_CAMERA_UPDATE = 15;
	private static final int POPUP_POSITION_REFRESH_INTERVAL = 16;
	private static final int ANIMATION_DURATION = 500;
	
	private String className;
	private String methodName;
	private String logMessage;
	
	private Activity mapActivity;
	private LocationManager locationManager;
	private LatLng lastKnownLocation;
	private LatLng latLngChanging;
	private BroadcastReceiver gcmLocationUpdatedWatcher;
	private BroadcastReceiver notificationBroadcastReceiver;
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
	private Context context;
	private Thread startTrackLocationServerThread;
	private Runnable startTrackLocationService;
	private boolean isPermissionDialogShown;
	private MessageDataContactDetails contactDetails;
	
	private TextView notificationView;
	
	private Controller controller;
	
	private TextView info_preview;
	private TextView title_text;
	
	private float startY;
	protected LinearLayout map_popup_first;
	private Animation animUp;
	private Animation animDown;
	private LinearLayout map_popup_second;
	private LinearLayout layoutAccountMenu;
	
	private DialogStatus viewStatus;
	
	private enum DialogStatus{
		Opened, Closed
	}
	
	public void launchWaitingDialog() {
        waitingDialog = new ProgressDialog(Map.this);
        waitingDialog.setTitle("Tracking location");
        waitingDialog.setMessage("Please wait ...");
        waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //progressDialog.setProgress(0);
        //progressDialog.setMax(contactsQuantity);
        //waitingDialog.setCancelable(false);
        waitingDialog.setIndeterminate(true);
        waitingDialog.show();
        waitingDialog.setCanceledOnTouchOutside(false);
        
        new Thread(new Runnable() {
            @Override
            public void run() {
            	if(markerMap != null && markerMap.size() >= contactsQuantity) {
            		waitingDialog.dismiss();
            		notificationView.setVisibility(4);
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
		getActionBar().hide();
		className = this.getClass().getName();
		methodName = "onCreate";
		mapActivity = this;
    	isPermissionDialogShown = false;
		setContentView(R.layout.map);	
		
		notificationView = (TextView) findViewById(R.id.textViewMap);
		notificationView.setVisibility(0); // 0 - visible / 4 - invisible
		notificationView.setText("Tracking for contacts\nPlease wait...");
		
		context = getApplicationContext();

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
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
				
				String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
				String macAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
				String phoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
				String registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
				MessageDataContactDetails senderMessageDataContactDetails = // sender contact details
						new MessageDataContactDetails(account, macAddress, phoneNumber, registrationId, 
							Controller.getBatteryLevel(context));
//				HashMap<String, Object> params = new HashMap<String, Object>();
//				params.put(CommonConst.START_CMD_CONTEXT, context);
//				params.put(CommonConst.START_CMD_SELECTED_CONTACT_DEVICE_DATA_LIST, selectedContactDeviceDataList);
//				params.put(CommonConst.START_CMD_SENDER_MESSAGE_DATA_CONTACT_DETAILS, senderMessageDataContactDetails); 
				final int retryTimes = 5;
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> BEGIN of LOOP: CMD START TrackLocationService");
				LogManager.LogInfoMsg(className, methodName, "BEGIN of LOOP: CMD START TrackLocationService");
				startTrackLocationService = new StartTrackLocationService(
					context,
					selectedContactDeviceDataList,
					senderMessageDataContactDetails,
					retryTimes,
					20000); // delay in milliseconds
				// ===========================================================================
				// Start TrackLocation Service for all requested recipients
				// ===========================================================================
				try {
					startTrackLocationServerThread = new Thread(startTrackLocationService);//.start();
					startTrackLocationServerThread.start();
				} catch (IllegalThreadStateException e) {
					logMessage = "LOOP: CMD START TrackLocationService was started already";
					LogManager.LogErrorMsg(className, methodName, logMessage);
					Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
				}
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> FINISH of LOOP: CMD START TrackLocationService");
				LogManager.LogInfoMsg(className, methodName, "FINISH of LOOP: CMD START TrackLocationService");
			}
		}
		isShowAllMarkersEnabled = true;

		launchWaitingDialog();
		
		initGcmLocationUpdatedBroadcastReceiver();
		initNotificationBroadcastReceiver();

		// Get a handle to the Map Fragment
		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map = mapFragment.getMap();

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
        
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);        

        map.clear();                
        
    	title_text = (TextView) findViewById(R.id.title_text);
    	
        info_preview = (TextView) findViewById(R.id.info_preview); 
        
        animUp = AnimationUtils.loadAnimation(this, R.anim.anim_up);
	    animDown = AnimationUtils.loadAnimation(this, R.anim.anim_down);
	    viewStatus = DialogStatus.Closed;
		
		layoutAccountMenu = (LinearLayout) findViewById(R.id.layoutAccountMenu);		
		layoutAccountMenu.getLayoutParams().height = 0;
		layoutAccountMenu.setLayoutParams(layoutAccountMenu.getLayoutParams());
		layoutAccountMenu.setOnTouchListener(this);
		
		map_popup_first = (LinearLayout) findViewById(R.id.map_popup_first);
		
		map_popup_second = (LinearLayout) findViewById(R.id.map_popup_second);
		map_popup_second.setVisibility(View.GONE);
		
		viewStatus = DialogStatus.Closed;

		map_popup_second.setVisibility(View.GONE);
        controller = new Controller();
        controller.keepAliveTrackLocationService(context, selectedContactDeviceDataList, 
        	CommonConst.KEEP_ALIVE_TIMER_REQUEST_FROM_MAP_DELAY);
        
	}		
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	 
	private void setupLocation() {
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);
		locationManager = (LocationManager) this.context.getSystemService(LOCATION_SERVICE);
		
		// try to get our last known location
		Location location = getLastKnownLocation();
		if (location != null) {
			lastKnownLocation = new LatLng(location.getLatitude(),
					location.getLongitude());
		} 
		else {
			Toast.makeText(Map.this, "getString(R.string.err_load_location)",
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

	// Initialize BROADCAST_MESSAGE broadcast receiver
	private void initNotificationBroadcastReceiver() {
		String methodName = "initNotificationBroadcastReceiver";
		LogManager.LogFunctionCall(className, methodName);
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(BroadcastActionEnum.BROADCAST_MESSAGE.toString());
	    notificationBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle bundle = intent.getExtras();
	    		if(bundle != null && bundle.containsKey(BroadcastConstEnum.data.toString())){
	    			String jsonNotificationData = bundle.getString(BroadcastConstEnum.data.toString());
	    			if(jsonNotificationData == null || jsonNotificationData.isEmpty()){
	    				return;
	    			}
	    			NotificationBroadcastData broadcastData = gson.fromJson(jsonNotificationData, NotificationBroadcastData.class);
	    			if(broadcastData == null){
	    				return;
	    			}
	    			
	    			String key  = broadcastData.getKey();
	    			String value = broadcastData.getValue();
	    			
    				// Notification about command: Start TrackLocation Service
    				// PLEASE WAIT - some recipients are not responding
	    			if(BroadcastKeyEnum.start_status.toString().equals(key) && 
	    					CommandValueEnum.wait.toString().equals(value)){
	    				displayNotification(bundle);
	    				notificationView.setVisibility(0);
	    				notificationView.setText(broadcastData.getMessage());
	    			}
	    			
    				// Notification about command: Start TrackLocation Service 
    				// FAILED for some recipients
	    			if(BroadcastKeyEnum.start_status.toString().equals(key) && 
	    					CommandValueEnum.error.toString().equals(value)){
	    				//showNotificationDialog(broadcastData.getMessage());
	    				Controller.showNotificationDialog(mapActivity, broadcastData.getMessage());
	    				notificationView.setText(broadcastData.getMessage());
	    				notificationView.setVisibility(4);
	    			}
	    			
    				// Notification about command: Start TrackLocation Service 
    				// FAILED for some recipients
	    			if(BroadcastKeyEnum.start_status.toString().equals(key) && 
	    					CommandValueEnum.success.toString().equals(value)){
	    				notificationView.setText(broadcastData.getMessage());
	    				notificationView.setVisibility(4);
	    			}
	    			
	    			if(CommandKeyEnum.permissions.toString().equals(key) && 
	    					CommandValueEnum.not_defined.toString().equals(value)){
	    				if(isPermissionDialogShown == false){
		    				Controller.showNotificationDialog(mapActivity, broadcastData.getMessage());
		    				isPermissionDialogShown = true;
	    				}
	    				waitingDialog.dismiss();
//	    				notificationView.setVisibility(4);
//	    		    	if(startTrackLocationServerThread != null){
//	    		    		startTrackLocationServerThread.interrupt();
//	    		    	}
	    			}

	    			if(CommandKeyEnum.permissions.toString().equals(key) && 
	    					CommandValueEnum.not_permitted.toString().equals(value)){
	    				if(isPermissionDialogShown == false){
		    				Controller.showNotificationDialog(mapActivity, broadcastData.getMessage());
		    				isPermissionDialogShown = true;
	    				}
	    				waitingDialog.dismiss();
//	    				notificationView.setVisibility(4);
//	    		    	if(startTrackLocationServerThread != null){
//	    		    		startTrackLocationServerThread.interrupt();
//	    		    	}
	    			}
	    		}
			}
	    };
	    
	    registerReceiver(notificationBroadcastReceiver, intentFilter);
	    
		LogManager.LogFunctionExit(className, methodName);
	}
	
	private void initGcmLocationUpdatedBroadcastReceiver() {
		
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

	    		if(bundle != null && bundle.containsKey(BroadcastConstEnum.data.toString())){
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
	    			contactDetails = broadcastData.getContactDetails();
	    			// TODO: Check that contactDetails are not null
	    			MessageDataLocation locationDetails = broadcastData.getLocation();
	    			// TODO: Check that locationDetails are not null
	    			
	    			String updatingAccount = contactDetails.getAccount();
	    			
		    		if(markerMap == null){
		    			markerMap = new LinkedHashMap<String, Marker>();
		    		}
		    		if(locationCircleMap == null){
		    			locationCircleMap = new LinkedHashMap<String, Circle>();
		    		}

		    		if(selectedAccountList != null && selectedAccountList.contains(updatingAccount)){
		    			// Set marker on the map
		    			//final View infoView = getLayoutInflater().inflate(R.layout.map_info_window, null);
		    			createMapMarker(map, contactDetails, locationDetails, markerMap, locationCircleMap);
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
		    			notificationView.setVisibility(4);
		    		}
	    			// ================================================		    		
	    		}	    		
    		}
	    };
	    
	    registerReceiver(gcmLocationUpdatedWatcher, intentFilter);
	    LogManager.LogFunctionExit("ContactConfiguration", "initGcmIntentServiceWatcher");
    }
	
	private void createMapMarker(GoogleMap map, 
			final MessageDataContactDetails contactDetails, 
			final MessageDataLocation locationDetails, 
			LinkedHashMap<String, 
			Marker> markerMap, 
			LinkedHashMap<String, 
			Circle> locationCircleMap) {
		if(locationDetails != null) {
    		final double lat = locationDetails.getLat();
    		final double lng = locationDetails.getLng();
    		
    		if(lat != 0 && lng != 0){
				LatLng latLngChanging = new LatLng(lat, lng);

				final String account = contactDetails.getAccount();
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
				String accurancy = locationDetails.getLocationProviderType().equals("gps") ? "High" : "Low";
				String snippetString = "Battery: " + String.valueOf(Math.round(contactDetails.getBatteryPercentage())) + "%" +  
			        "\nAccurancy: " + accurancy;
				double speed = locationDetails.getSpeed();
				if(speed > 0){
					snippetString = snippetString + "\nSpeed: " + String.valueOf(Math.round(speed)) + " km/h";
				}
				else{

					Geocoder geocoder = new Geocoder(this.context, Locale.ENGLISH);
					try {
						List<Address>  addresses = geocoder.getFromLocation(lat, lng, 1);
						String address = addresses.get(0).getAddressLine(0);
						String city = addresses.get(0).getAddressLine(1);
						//String country = addresses.get(0).getAddressLine(2);
						snippetString = snippetString + "\nCity : " + city + "\nAddress: " + address;					
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				Button callBtn = (Button) findViewById(R.id.call_btn);			
				callBtn.setOnClickListener(new OnClickListener() {

			        @Override
			        public void onClick(View v) {		
			        	Intent intent = new Intent(Intent.ACTION_DIAL);
			        	//Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "0544504619"));
				    	startActivity( intent );			        	 
			        }
			    });
				
				Button messageBtn = (Button) findViewById(R.id.message_btn);
				messageBtn.setOnClickListener(new OnClickListener() {

			        @Override
			        public void onClick(View v) {
					    PackageManager pm = getPackageManager();
					    Intent sendIntent = new Intent(Intent.ACTION_SEND);     
					    sendIntent.setType("text/plain");
				    
					    List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
					    List<Intent> intentList = new ArrayList<Intent>();        
					    for (int i = 0; i < resInfo.size(); i++) {
					        // Extract the label, append it, and repackage it in a LabeledIntent
					        ResolveInfo ri = resInfo.get(i);
					        String packageName = ri.activityInfo.packageName;

					        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
					        intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
					        intent.setType("text/plain"); // put here your mime type			       
			   				if (packageName.equals("com.whatsapp")) {			    	            
			   					intent.setPackage(packageName);
			   					intentList.add(intent);
			    	        }
			   				else if (packageName.equals("com.viber.voip")) {			    	            
			   					intent.setPackage(packageName);
			   					intentList.add(intent);
			    	        }else if (packageName.equals("com.android.mms")) {			    	            
			    	        	intent.setPackage(packageName);
			    	        	intentList.add(intent);
			    	        }
			    	        else if (packageName.equals("ru.ok.android")) {			    	            
			    	        	intent.setPackage(packageName);
			    	        	intentList.add(intent);
			    	        }
			    	        else if (packageName.equals("com.skype.raider")) {			    	            
			    	        	intent.setPackage(packageName);
			    	        	intentList.add(intent);
			    	        }
			    	        else if (packageName.equals("com.google.android.gm")) {			    	            
			    	        	intent.setPackage(packageName);
			    	        	intentList.add(intent);
			    	        }
			    	        else if (packageName.equals("com.facebook.orca")) {			    	            
			    	        	intent.setPackage(packageName);
			    	        	intentList.add(intent);
			    	        }   
					    }

					    // convert intentList to array
					    Intent openInChooser = Intent.createChooser(intentList.remove(0),"Message option choose");
					    Intent[] extraIntents = intentList.toArray( new Intent[ intentList.size() ]);

					    openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
					    startActivity(openInChooser);	          
			        }
			    });
				
				Button ringBtn = (Button) findViewById(R.id.ring_btn);			
				ringBtn.setOnClickListener(new OnClickListener() {

			        @Override
			        public void onClick(View v) {		
			        	showRingConfirmationDialog(Map.this, "Are you sure?");
			        }
			    });

				ImageButton nav_btn = (ImageButton) findViewById(R.id.nav_btn);			
				nav_btn.setOnClickListener(new OnClickListener() {
					 
			        @Override
			        public void onClick(View v) {
			        	Location loc = getLastKnownLocation();
				    	   final String uri = String.format(Locale.getDefault(), "geo:%f,%f?q=%f,%f", lat, lng, lat, lng);
				    	   Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( uri ) );
				    	   startActivity( intent );	
			        }
			    });
				//marker.
				
				Bitmap.Config conf = Bitmap.Config.ARGB_8888;
				Bitmap bmp = Bitmap.createBitmap(80, 80, conf);
				Canvas canvas1 = new Canvas(bmp);

				// paint defines the text color,
				// stroke width, size
				Paint color = new Paint();
				color.setTextSize(35);
				color.setColor(Color.BLACK);

				//modify canvas
				canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),	R.drawable.wpfrk), 0,0, color);
				canvas1.drawText("User Name!", 30, 40, color);
				
				
				marker = map.addMarker(new MarkerOptions()
					//TODO add user profile image
					.icon(BitmapDescriptorFactory.fromBitmap(drawMarker(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))))

					.snippet(snippetString)
					.title(account)
					//.anchor(0.0f, 1.0f) // Anchors the marker on the bottom
										// left
					.position(latLngChanging));
				
				markerMap.put(account, marker);
				
				double accuracy = locationDetails.getAccuracy();
		
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
    	
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    	controller.stopKeepAliveTrackLocationService();
    	String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
    	Preferences.clearPreferencesReturnToContactMap(context, account);
    	
    	Log.i(CommonConst.LOG_TAG, "Timer with mapKeepAliveTimerJob - stopped");
    	if(gcmLocationUpdatedWatcher != null){
    		unregisterReceiver(gcmLocationUpdatedWatcher);
    	}
    	if(notificationBroadcastReceiver != null){
    		unregisterReceiver(notificationBroadcastReceiver);
    	}
    	if(startTrackLocationServerThread != null){
    		startTrackLocationServerThread.interrupt();
    	}
    	
    	isPermissionDialogShown = false;
    }

	private void displayNotification(Bundle bundle){
		String jsonNotificationData = bundle.getString(BroadcastConstEnum.data.toString());
		if(jsonNotificationData == null || jsonNotificationData.isEmpty()){
			return;
		}
		NotificationBroadcastData broadcastData = gson.fromJson(jsonNotificationData, NotificationBroadcastData.class);
		if(broadcastData == null){
			return;
		}
		
		Toast.makeText(Map.this, broadcastData.getMessage(), Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onMarkerClick(Marker marker) {    	
        Projection projection = map.getProjection();
        lastKnownLocation = marker.getPosition();
        Point trackedPoint = projection.toScreenLocation(lastKnownLocation);
        
        LatLng newCameraLocation = projection.fromScreenLocation(trackedPoint);
        map.animateCamera(CameraUpdateFactory.newLatLng(newCameraLocation), ANIMATION_DURATION, null);
        if (viewStatus == DialogStatus.Closed){
	        layoutAccountMenu.getLayoutParams().height = map_popup_first.getLayoutParams().height;
			layoutAccountMenu.setLayoutParams(layoutAccountMenu.getLayoutParams());					
			viewStatus = DialogStatus.Opened;
			
			layoutAccountMenu.startAnimation(animUp);
        }
        title_text.setText(marker.getTitle());          
        info_preview.setText(marker.getSnippet());
		        
        return true;
	}

	@Override
	public void onMapClick(LatLng point) {
		closeLayouUserMenu();
	}

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
  
    }

   @Override
   public boolean onContextItemSelected(MenuItem item) {
       return super.onContextItemSelected(item);
   }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return touchEventAnalyzing(event);
	}

	private boolean touchEventAnalyzing(MotionEvent event) {
		switch (event.getAction()) {
	        case MotionEvent.ACTION_DOWN : {
	            startY = event.getY();
	            break ;           
	        }
	        case MotionEvent.ACTION_UP: {
	            float endY = event.getY();
	             
	            if (endY < startY) {
	            	if (viewStatus == DialogStatus.Opened){
		                System.out.println("Move UP");	                
		                layoutAccountMenu.getLayoutParams().height = map_popup_first.getLayoutParams().height + map_popup_second.getLayoutParams().height;
						layoutAccountMenu.setLayoutParams(layoutAccountMenu.getLayoutParams());
		                Animation animUpEx = new TranslateAnimation(0, 0, layoutAccountMenu.getLayoutParams().height, 0 );
		                animUpEx.setDuration(400);		                
		                map_popup_second.setVisibility(View.VISIBLE);
		                layoutAccountMenu.startAnimation(animUpEx);		                
	            	}	                
	            }
	            else {
	            	closeLayouUserMenu();
	            }    
	        }     
		}
		return true;
	}

	private void closeLayouUserMenu() {
		if (layoutAccountMenu.getVisibility() == View.VISIBLE){
			animDown.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationRepeat(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                	map_popup_second.setVisibility(View.GONE);
	            	layoutAccountMenu.getLayoutParams().height = 0;
	        		layoutAccountMenu.setLayoutParams(layoutAccountMenu.getLayoutParams());
	        		viewStatus =DialogStatus.Closed;
                }
            });
			layoutAccountMenu.startAnimation(animDown);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return touchEventAnalyzing(event);
	}
	
	private Bitmap drawMarker(Bitmap bitmap) {		
		bitmap = Utils.getResizedBitmap(Utils.getRoundedCornerImage(bitmap), 50, 60);
		Bitmap bmp = Bitmap.createBitmap(bitmap.getWidth() + 20, bitmap.getHeight() + 40, Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);

		Paint color = new Paint();
		color.setTextSize(35);
		color.setColor(Color.BLACK);

		Bitmap bgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wpfrk);
		// modify canvas
		canvas.drawBitmap(bgBitmap, null, new Rect(0, 0, bitmap.getWidth() + 20, bitmap.getHeight() + 40), color);
		canvas.drawBitmap(bitmap, 10, 10, color);
		return bmp;
	}
	
	private IDialogOnClickAction dialogOnClickAction = new IDialogOnClickAction(){

		@Override
		public void doOnPositiveButton() {
			Controller.RingDevice(context, className, contactDetails);
		}

		@Override
		public void doOnNegativeButton() {
		}

		@Override
		public void doOnChooseItem(int which) {
		}

		@Override
		public void setActivity(Activity activity) {
		}

		@Override
		public void setContext(Context context) {
		}

		@Override
		public void setParams(Object[]... objects) {
		}		
	};
	
	private void showRingConfirmationDialog(Activity activity, String confirmationMessage) {
		CommonDialog aboutDialog = new CommonDialog(activity, dialogOnClickAction);
		aboutDialog.setDialogMessage(confirmationMessage);
		aboutDialog.setDialogTitle("Ring to chosen contact.");
		aboutDialog.setPositiveButtonText("Yes");
		aboutDialog.setNegativeButtonText("No");
		aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
		aboutDialog.showDialog();
		aboutDialog.setCancelable(true);
    }
	
}

