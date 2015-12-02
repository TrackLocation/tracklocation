package com.doat.tracklocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.doat.tracklocation.R;
import com.doat.tracklocation.concurrent.StartTrackLocationService;
import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.datatype.BroadcastConstEnum;
import com.doat.tracklocation.datatype.BroadcastData;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.CommandKeyEnum;
import com.doat.tracklocation.datatype.CommandValueEnum;
import com.doat.tracklocation.datatype.ContactData;
import com.doat.tracklocation.datatype.ContactDeviceData;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.MapMarkerDetails;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.datatype.MessageDataLocation;
import com.doat.tracklocation.datatype.NotificationBroadcastData;
import com.doat.tracklocation.dialog.CommonDialog;
import com.doat.tracklocation.dialog.IDialogOnClickAction;
import com.doat.tracklocation.dialog.InfoDialog;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.MapUtils;
import com.doat.tracklocation.utils.Preferences;
import com.doat.tracklocation.utils.Utils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

public class MapActivity extends BaseActivity implements LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, OnTouchListener{
	private String logMessage;	
	// Max time of waiting dialog displaying - 30 seconds
	private final static int MAX_SHOW_TIME_WAITING_DIALOG = 30000; 
	private final static float DEFAULT_CAMERA_UPDATE = 15;
	//private static final int POPUP_POSITION_REFRESH_INTERVAL = 16;
	private static final int ANIMATION_DURATION = 500;
	
	private Activity mapActivity;
	private LocationManager locationManager;
	private LatLng lastKnownLocation;
	private BroadcastReceiver gcmLocationUpdatedWatcher;
	private BroadcastReceiver notificationBroadcastReceiver;
	private GoogleMap map;

	private float zoom;
	private ContactDeviceDataList selectedContactDeviceDataList;
	private HashMap<String, ContactData> selectedAccountList;
	private int contactsQuantity;
	private boolean isShowAllMarkersEnabled;
	private boolean isMapInMovingState = false;;
	private ProgressDialog waitingDialog;
	private Gson gson;
	private Context context;
	private Thread startTrackLocationServerThread;
	private Runnable startTrackLocationService;
	private boolean isPermissionDialogShown;

	private LinkedHashMap<String, MapMarkerDetails> mapMarkerDetailsList = new LinkedHashMap<String, MapMarkerDetails>();
	private MapMarkerDetails selectedMarkerDetails = null;
	
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
	private ImageButton btnMyLocation;
	
	float mLastTouchX = 0;
	float mLastTouchY = 0;
	private int mActivePointerId = -1;
	
	private DialogStatus viewStatus;
	TimerTask timerTask;
	//we are going to use a handler to be able to run in our TimerTask
	private final Handler handler = new Handler();
	private Timer timer;
	private boolean bLockMapNothOnly;
	
	private enum DialogStatus{
		Opened, Closed
	}
	
	public void launchWaitingDialog() {
        waitingDialog = new ProgressDialog(MapActivity.this);
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
            	if(mapMarkerDetailsList.size() >= contactsQuantity) {
            		waitingDialog.dismiss();
            		notificationView.setVisibility(View.INVISIBLE);
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
		className = this.getClass().getName();
		getActionBar().hide();
		methodName = "onCreate";
		mapActivity = this;
    	isPermissionDialogShown = false;
		setContentView(R.layout.map);	

		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);

		notificationView = (TextView) findViewById(R.id.textViewMap);
		notificationView.setVisibility(0); // 0 - visible / 4 - invisible
		notificationView.setText("Tracking for contacts\nPlease wait...");
		
		context = getApplicationContext();

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		gson = new Gson();
		
		ArrayList<ContactDeviceData> selectedContactDeviceDataListEx = getIntent().getExtras().getParcelableArrayList(CommonConst.CONTACT_DEVICE_DATA_LIST); 
		if(selectedContactDeviceDataListEx != null){
			selectedContactDeviceDataList = new ContactDeviceDataList();
			selectedContactDeviceDataList.addAll(selectedContactDeviceDataListEx);
			if(!selectedContactDeviceDataList.isEmpty()){
				contactsQuantity = selectedContactDeviceDataList.size();
				// Create and fill all requested accounts shat should be shown on the location map
				selectedAccountList = new HashMap<String, ContactData>();
				for (ContactDeviceData contactDeviceData : selectedContactDeviceDataList) {
					ContactData contactData = contactDeviceData.getContactData();
					if(contactData != null){
						selectedAccountList.put(contactData.getEmail(), contactData);				
					}
				}
				
				String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
				String macAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
				String phoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
				String registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
				MessageDataContactDetails senderMessageDataContactDetails = // sender contact details
						new MessageDataContactDetails(account, macAddress, phoneNumber, registrationId, 
							Controller.getBatteryLevel(context));

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
        MyMapFragment myMapFragment = (MyMapFragment) getFragmentManager().findFragmentById(R.id.map);
        myMapFragment.setOnDragListener(new MapWrapperLayout.OnDragListener() {
        	
            @Override
            public void onDrag(MotionEvent motionEvent){
            	final int action = MotionEventCompat.getActionMasked(motionEvent); 
                
                switch (action) { 
	                case MotionEvent.ACTION_DOWN: {
	                    final int pointerIndex = MotionEventCompat.getActionIndex(motionEvent); 
	                    mLastTouchX = MotionEventCompat.getX(motionEvent, pointerIndex); 
	                    mLastTouchY = MotionEventCompat.getY(motionEvent, pointerIndex); 
	                        
	                    // Save the ID of this pointer (for dragging)
	                    mActivePointerId = MotionEventCompat.getPointerId(motionEvent, 0);
	                    break;
	                }
	                        
	                case MotionEvent.ACTION_MOVE: {
	                    // Find the index of the active pointer and fetch its position
	                    final int pointerIndex = MotionEventCompat.findPointerIndex(motionEvent, mActivePointerId);  
	                    if (pointerIndex >= 0){	                   
		                    final float x = MotionEventCompat.getX(motionEvent, pointerIndex);
		                    final float y = MotionEventCompat.getY(motionEvent, pointerIndex);
		                        
		                    // Calculate the distance moved
		                    if (Math.abs(x - mLastTouchX) > 5 || Math.abs(y - mLastTouchY) > 5){
		                    	if (mapMarkerDetailsList.size() > 0){
		    						isMapInMovingState = true;
		    						startTimer();
		    					}
		                    }                    
	                    } 
	                    break;
	                }                                                            
                }       
                Log.d("ON_DRAG", String.format("ME: %s", motionEvent));                                
            }			
        });
        
        map = myMapFragment.getMap();
        

        setupLocation();

		String jsonListAccounts = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS);
        Log.i(CommonConst.LOG_TAG, "Inside loop for recipients: " + jsonListAccounts);

        String accountsListMsg = "Waiting for:\n\n";
		if(jsonListAccounts == null || jsonListAccounts.isEmpty()){
			accountsListMsg = "All contacts found.";
		} else {
			List<String> listAccounts = gson.fromJson(jsonListAccounts, List.class);
			for (String account : listAccounts) {
				accountsListMsg = accountsListMsg + selectedAccountList.get(account).getNick() + "\n";
			}
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
		
		btnMyLocation = (ImageButton) findViewById(R.id.btn_my_location);
		btnMyLocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	closeLayouUserMenu();
            	goToMyLocation();
            }
        });
		
		map_popup_first = (LinearLayout) findViewById(R.id.map_popup_first);
		
		map_popup_second = (LinearLayout) findViewById(R.id.map_popup_second);
		map_popup_second.setVisibility(View.GONE);
		
		viewStatus = DialogStatus.Closed;

		map_popup_second.setVisibility(View.GONE);
		

		
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
	        	if (selectedMarkerDetails != null){
	        		String title = "Ring to chosen contact.";
	        		String dialogMessage = "Are you sure?";
//	        		showRingConfirmationDialog(MapActivity.this, "Are you sure?", selectedMarkerDetails.getContactDetails());
	        		InfoDialog joinRequestDialog = new InfoDialog(MapActivity.this, context, title, dialogMessage, null);
	        		if(joinRequestDialog.isSelectionStatus()){
	        			Controller.RingDevice(context, className, selectedMarkerDetails.getContactDetails());
	        		}
	        	}
	        }
	    });

		ImageButton nav_btn = (ImageButton) findViewById(R.id.nav_btn);			
		nav_btn.setOnClickListener(new OnClickListener() {
			 
	        @Override
	        public void onClick(View v) {
	        	if (selectedMarkerDetails != null){
	        		double lat = selectedMarkerDetails.getLocationDetails().getLat();
	        		double lng = selectedMarkerDetails.getLocationDetails().getLng();
	        		final String uri = String.format(Locale.getDefault(), "geo:%f,%f?q=%f,%f", lat, lng, lat, lng);
	        		Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( uri ) );
	        		startActivity( intent );	
	        	}
	        }
	    });
		
        controller = new Controller();
        controller.keepAliveTrackLocationService(context, selectedContactDeviceDataList, 
        	CommonConst.KEEP_ALIVE_TIMER_REQUEST_FROM_MAP_DELAY);       
	}		
	
	@Override
	protected void onStart() {	
		super.onStart();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		bLockMapNothOnly = sharedPref.getBoolean("pref_map_lock", false);
	}
	
	private void goToMyLocation(){
		stoptimertask();
    	btnMyLocation.setVisibility(View.INVISIBLE);
    	isShowAllMarkersEnabled = true;
    	isMapInMovingState = false;
    	mapAnimateCameraForMarkers(null, "");		
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
			Toast.makeText(MapActivity.this, "getString(R.string.err_load_location)",
					Toast.LENGTH_LONG).show();
		}

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, MapActivity.this); 
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
    				// RECEIVED - some recipient received request
	    			if(BroadcastKeyEnum.start_status.toString().equals(key) && CommandValueEnum.start_track_location_service_received.toString().equals(value)){
	    				displayNotification(bundle);
	    				notificationView.setVisibility(0);
	    				notificationView.setText(broadcastData.getMessage());
	    			}

	    			// Notification about command: Start TrackLocation Service
    				// PLEASE WAIT - some recipients are not responding
	    			if(BroadcastKeyEnum.start_status.toString().equals(key) && CommandValueEnum.wait.toString().equals(value)){
	    				displayNotification(bundle);
	    				notificationView.setVisibility(0);
	    				notificationView.setText(broadcastData.getMessage());
	    			}
	    			
    				// Notification about command: Start TrackLocation Service 
    				// FAILED for some recipients
	    			if(BroadcastKeyEnum.start_status.toString().equals(key) && CommandValueEnum.error.toString().equals(value)){
	    				//showNotificationDialog(broadcastData.getMessage());
//	    				Controller.showNotificationDialog(mapActivity, broadcastData.getMessage());
	    				String title = "Warning";
	    				String dialogMessage = broadcastData.getMessage();
	    				new InfoDialog(MapActivity.this, context, title, dialogMessage, null);
	    				notificationView.setText(broadcastData.getMessage());
	    				notificationView.setVisibility(4);
	    			}
	    			
    				// Notification about command: Start TrackLocation Service 
    				// SUCCESS for some recipients
	    			if(BroadcastKeyEnum.start_status.toString().equals(key) && CommandValueEnum.success.toString().equals(value)){
	    				notificationView.setText(broadcastData.getMessage());
	    				notificationView.setVisibility(4);
	    		        String accountsListMsg = "Waiting for:\n\n";
	    				String jsonListAccounts = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_SEND_COMMAND_TO_ACCOUNTS);
	    				if(jsonListAccounts == null || jsonListAccounts.isEmpty()){
	    					accountsListMsg = "All contacts found.";
	    				} else {
	    					List<String> listAccounts = gson.fromJson(jsonListAccounts, List.class);
	    					for (String account : listAccounts) {
	    						accountsListMsg = accountsListMsg + selectedAccountList.get(account).getNick() + "\n";
	    					}
	    				}
	    				waitingDialog.setMessage(accountsListMsg);
	    			}
	    			
	    			if(CommandKeyEnum.permissions.toString().equals(key) && CommandValueEnum.not_defined.toString().equals(value)){
	    				if(isPermissionDialogShown == false){
//		    				Controller.showNotificationDialog(mapActivity, broadcastData.getMessage());
	    					String title = "Warning";
	    					String dialogMessage = broadcastData.getMessage();
	    	        		new InfoDialog(MapActivity.this, context, title, dialogMessage, null);
		    				isPermissionDialogShown = true;
	    				}
	    				waitingDialog.dismiss();
	    			}

	    			if(CommandKeyEnum.permissions.toString().equals(key) && CommandValueEnum.not_permitted.toString().equals(value)){
	    				if(isPermissionDialogShown == false){
//		    				Controller.showNotificationDialog(mapActivity, broadcastData.getMessage());
	    					String title = "Warning";
	    					String dialogMessage = broadcastData.getMessage();
	    	        		new InfoDialog(MapActivity.this, context, title, dialogMessage, null);
		    				isPermissionDialogShown = true;
	    				}
	    				waitingDialog.dismiss();
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
	    		//String broadcastKeyLocationUpdated = BroadcastKeyEnum.location_updated.toString();
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
	    			MessageDataContactDetails contactDetails = broadcastData.getContactDetails();
	    			// TODO: Check that contactDetails are not null
	    			MessageDataLocation locationDetails = broadcastData.getLocation();
	    			// TODO: Check that locationDetails are not null
	    			
	    			String updatingAccount = contactDetails.getAccount();
	    			MessageDataLocation prevLocationDetails = null; 
	    			
		    		if(selectedAccountList != null && selectedAccountList.containsKey(updatingAccount)){
		    			// Set marker on the map		    			
		    			if (mapMarkerDetailsList.containsKey(updatingAccount)){
		    				prevLocationDetails = mapMarkerDetailsList.get(updatingAccount).getLocationDetails();
		    				mapMarkerDetailsList.get(updatingAccount).getLocationCircle().remove();
		    				mapMarkerDetailsList.get(updatingAccount).getMarker().remove();
		    				mapMarkerDetailsList.remove(updatingAccount);
		    			}
		    			
		    			MapMarkerDetails  mapMarkerDetails = createMapMarker(contactDetails, locationDetails);
		    			if (mapMarkerDetails != null){
		    				mapMarkerDetailsList.put(updatingAccount, mapMarkerDetails);
		    			}	    			
		    		}
		    		
		    		mapAnimateCameraForMarkers(prevLocationDetails, updatingAccount);
		    		if(mapMarkerDetailsList != null && mapMarkerDetailsList.size() == contactsQuantity){
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
	
	private void mapAnimateCameraForMarkers(MessageDataLocation prevLocationDetails, String updatingAccount) {
		if (!isMapInMovingState && selectedAccountList.size() == mapMarkerDetailsList.size()){
			if(mapMarkerDetailsList.size() > 1 && isShowAllMarkersEnabled == true) {
				// put camera to show all markers
				CameraUpdate cu = MapUtils.createCameraUpdateLatLngBounds(mapMarkerDetailsList);
				map.animateCamera(cu);
				if(mapMarkerDetailsList.size() >= contactsQuantity){
					// put camera to show all markers
					cu = MapUtils.createCameraUpdateLatLngBounds(mapMarkerDetailsList);
					map.animateCamera(cu); // or map.moveCamera(cu); 
					isShowAllMarkersEnabled = false;
				}
			} 
			else if(mapMarkerDetailsList.size() == 1) {				
				String account = updatingAccount.isEmpty() ? selectedAccountList.keySet().toArray()[0].toString() : updatingAccount;
				MapMarkerDetails mapMarkerDetails =  mapMarkerDetailsList.get(account);
				if(mapMarkerDetails != null && mapMarkerDetails.getLocationDetails() != null) {
					if (!isShowAllMarkersEnabled && mapMarkerDetails.getLocationDetails().getSpeed() > 0 )
						isShowAllMarkersEnabled = true;
					if (isShowAllMarkersEnabled ){
			    		double lat = mapMarkerDetails.getLocationDetails().getLat();
			    		double lng = mapMarkerDetails.getLocationDetails().getLng();
			    		LatLng latLngChangingLast = new LatLng(lat, lng);
			
			    		Location prevLocation = null;
			    		float prevBearing = 0;
			    		double prevDistance = 0;
						if (prevLocationDetails != null){					
							prevLocation = new Location("prevLocation");							
							prevLocation.setLatitude(prevLocationDetails.getLat());
							prevLocation.setLongitude(prevLocationDetails.getLng());
							prevBearing = prevLocationDetails.getBearing();			
							prevDistance = prevLocationDetails.getDistance();
						}
						float bearing = 0;
						float zoomCalc = zoom;
						float tilt = 0;
						double distance = 0;
						if (mapMarkerDetails.getLocationDetails().getSpeed() > 0 && !isMapInMovingState){
							if (!bLockMapNothOnly){			
								Location currLocation = new Location("prevLocation");							
								currLocation.setLatitude(lat);
								currLocation.setLongitude(lng);
								if (prevLocation != null){
									bearing = prevLocation.bearingTo(currLocation);
									distance = MapUtils.getDistanceBetweenPoints(new LatLng(prevLocation.getLatitude(), prevLocation.getLongitude()), latLngChangingLast);
								}
								
								mapMarkerDetails.getLocationDetails().setBearing(bearing);
								mapMarkerDetails.getLocationDetails().setDistance(distance);
								if (getDifference(prevBearing, bearing) < 25 && distance != 0 && (distance - prevDistance > 50)){
									bearing = prevBearing;
									mapMarkerDetails.getLocationDetails().setBearing(bearing);
									mapMarkerDetails.getLocationDetails().setDistance(distance);
								}
								zoomCalc = DEFAULT_CAMERA_UPDATE;
								tilt = 80;
							}
							
						}
						   																								
						CameraPosition currentPlace = new CameraPosition.Builder()
				            .target(latLngChangingLast)
				            .bearing(bearing)
				            .zoom(zoomCalc)
				            .tilt(tilt)
				            .build();
						map.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
						
						if(mapMarkerDetailsList.size() >= contactsQuantity){
							isShowAllMarkersEnabled = false;
						}
					}				    		
				}
			}
		}
	}
	
	private float getDifference(float a1, float a2) {
	    return Math.min((a1-a2)<0?a1-a2+360:a1-a2, (a2-a1)<0?a2-a1+360:a2-a1);
	}
	
	private MapMarkerDetails createMapMarker(final MessageDataContactDetails contactDetails, final MessageDataLocation locationDetails) {
		if(locationDetails == null) {
			return null;
		}
		final double lat = locationDetails.getLat();
		final double lng = locationDetails.getLng();
		
		if(lat == 0 || lng == 0){
			return null;
		}
		
		LatLng latLngChanging = new LatLng(lat, lng);

		final String account = contactDetails.getAccount();
		if(account == null || account.isEmpty()) {
			return null;
		}						        	
		
		ContactDeviceData contactDeviceData = selectedContactDeviceDataList.getContactDeviceDataByContactData(contactDetails.getAccount());
		
		Bitmap bmpContact = contactDeviceData.getContactData().getContactPhoto() == null ? Utils.getDefaultContactBitmap(getResources()) : contactDeviceData.getContactData().getContactPhoto();
		
		Marker marker = map.addMarker(new MarkerOptions()
			.icon(BitmapDescriptorFactory.fromBitmap(drawMarker(bmpContact)))
			.snippet(account)
			.position(latLngChanging));
		
		double accuracy = locationDetails.getAccuracy();

		Circle locationCircle = map.addCircle(new CircleOptions().center(latLngChanging)
		            .radius(accuracy)
		            .strokeColor(Color.argb(255, 0, 153, 255))
		            .fillColor(Color.argb(30, 0, 153, 255)).strokeWidth(2));
		MapMarkerDetails mapMarkerDetails = new MapMarkerDetails(contactDetails, locationDetails, marker, locationCircle);
		updateSelectedData(mapMarkerDetails);
		return mapMarkerDetails;
	}

	private void updateSelectedData(MapMarkerDetails mapMarkerDetails) {
		if (mapMarkerDetails != null && selectedMarkerDetails != null && mapMarkerDetails.getContactDetails().getAccount().equals(selectedMarkerDetails.getContactDetails().getAccount()) && viewStatus == DialogStatus.Opened){
			String accurancy = mapMarkerDetails.getLocationDetails().getLocationProviderType().equals("gps") ? "High" : "Low";
			String snippetString = "Battery: " + String.valueOf(Math.round(mapMarkerDetails.getContactDetails().getBatteryPercentage())) + "%" +  
		        "\nAccurancy: " + accurancy;
			final double lat;
			final double lng;
			double speed = mapMarkerDetails.getLocationDetails().getSpeed();
			if(speed > 0){
				snippetString = snippetString + "\nSpeed: " + String.valueOf(Math.round(speed)) + " km/h\n";
			}
			else{
	
				Geocoder geocoder = new Geocoder(this.context, Locale.ENGLISH);
				try {
					lat = mapMarkerDetails.getLocationDetails().getLat();
	        		lng = mapMarkerDetails.getLocationDetails().getLng();
					List<Address>  addresses = geocoder.getFromLocation(lat, lng, 1);
					String address = null;
					String city = null;
					if(addresses != null && addresses.size() > 0 && addresses.get(0) != null){
						address = addresses.get(0).getAddressLine(0);
						city = addresses.get(0).getAddressLine(1);
						//String country = addresses.get(0).getAddressLine(2);
						snippetString = snippetString + "\nCity : " + city + "\nAddress: " + address;					
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			info_preview.setText(snippetString);
		}
	}
	
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }
    
    @Override
    protected void onStop() {
    	methodName = "onStop";
    	
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
    	
		LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_Stop] {" + className + "} -> " + methodName);

    	super.onStop();
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
		
		Toast.makeText(MapActivity.this, broadcastData.getMessage(), Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onMarkerClick(Marker marker) {    	
        Projection projection = map.getProjection();
        lastKnownLocation = marker.getPosition();
        Point trackedPoint = projection.toScreenLocation(lastKnownLocation);
        
        LatLng newCameraLocation = projection.fromScreenLocation(trackedPoint);
        map.animateCamera(CameraUpdateFactory.newLatLng(newCameraLocation), ANIMATION_DURATION, null);
        
        selectedMarkerDetails = mapMarkerDetailsList.get(marker.getSnippet());
        
        if (selectedMarkerDetails == null){
        	return false;
        }        	      
                
        if (viewStatus == DialogStatus.Closed){
	        layoutAccountMenu.getLayoutParams().height = map_popup_first.getLayoutParams().height;
			layoutAccountMenu.setLayoutParams(layoutAccountMenu.getLayoutParams());					
			viewStatus = DialogStatus.Opened;
			title_text.setText(marker.getSnippet()); 
	        updateSelectedData(selectedMarkerDetails);
			layoutAccountMenu.startAnimation(animUp);
        }
        else{
        	closeLayouUserMenu();
        	isShowAllMarkersEnabled = true;
        	mapAnimateCameraForMarkers(null, "");
        }
        		        
        return true;
	}

	@Override
	public void onMapClick(LatLng point) {
		
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
		int iSize = (int)Utils.convertDpToPixels(45, getResources());
		
		bitmap = Utils.getResizedBitmap(Utils.getRoundedCornerImage(bitmap, false), iSize, iSize);
		Bitmap bmp = Bitmap.createBitmap(bitmap.getWidth() + (int)(0.2 * iSize), bitmap.getHeight() + (int)(0.4  * iSize), Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);

		Bitmap bgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wpfrk);
		// modify canvas
		canvas.drawBitmap(bgBitmap, null, new Rect(0, 0, bitmap.getWidth() + (int)(0.2 * iSize), bitmap.getHeight() + (int)(0.4 * iSize)), new Paint());
		canvas.drawBitmap(bitmap, (int)(0.1 * iSize), (int)(0.1 * iSize), new Paint());
		return bmp;
	}

/*	
	private IDialogOnClickAction dialogOnClickAction = new IDialogOnClickAction(){
		MessageDataContactDetails contactDetails = null;
		
		@Override
		public void doOnPositiveButton() {
			if (contactDetails != null){
				Controller.RingDevice(context, className, contactDetails);
			}
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
			contactDetails = (MessageDataContactDetails) objects[0][0];
		}		
	};
*/
/*	
	private void showRingConfirmationDialog(Activity activity, String confirmationMessage, MessageDataContactDetails contactDetails) {
		Object[] objects = new Object[1];
		objects[0] = contactDetails;
		
		dialogOnClickAction.setParams(objects);
		CommonDialog aboutDialog = new CommonDialog(activity, dialogOnClickAction);
		aboutDialog.setDialogMessage(confirmationMessage);
		aboutDialog.setDialogTitle("Ring to chosen contact.");
		aboutDialog.setPositiveButtonText("Yes");
		aboutDialog.setNegativeButtonText("No");
		aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
		aboutDialog.showDialog();
		aboutDialog.setCancelable(true);
    }
*/	
    private void startTimer() {
    	stoptimertask();
        //set a new Timer
        timer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
         timer.schedule(timerTask, 20000); //
         btnMyLocation.setVisibility(View.VISIBLE);
    }

    private void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                    	goToMyLocation();
                    }
                });
            }
        };
    }
    
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	super.onBackPressed();
    	finish();
    }
}
