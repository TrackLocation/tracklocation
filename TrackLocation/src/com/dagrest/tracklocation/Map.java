package com.dagrest.tracklocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.dagrest.tracklocation.concurrent.StartTrackLocationService;
import com.dagrest.tracklocation.datatype.BroadcastActionEnum;
import com.dagrest.tracklocation.datatype.BroadcastConstEnum;
import com.dagrest.tracklocation.datatype.BroadcastData;
import com.dagrest.tracklocation.datatype.BroadcastKeyEnum;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.CommandKeyEnum;
import com.dagrest.tracklocation.datatype.CommandTagEnum;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.gson.Gson;

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
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class Map extends Activity implements LocationListener{

	// Max time of waiting dialog displaying - 30 seconds
	private final static int MAX_SHOW_TIME_WAITING_DIALOG = 30000; 
	private final static float DEFAULT_CAMERA_UPDATE = 15;
	
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
	
	private TextView notificationView;
	
	private Controller controller;
	
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
	    			MessageDataContactDetails contactDetails = broadcastData.getContactDetails();
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
		    			final View infoView = getLayoutInflater().inflate(R.layout.map_info_window, null);
		    			Controller.setMapMarker(map, contactDetails, locationDetails, markerMap, locationCircleMap,infoView);
		    			
		    			//Controller.setMapMarker(map, contactDetails, locationDetails, markerMap, locationCircleMap,null);
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
	
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//    	if(selectedContactDeviceDataList != null && !selectedContactDeviceDataList.getContactDeviceDataList().isEmpty()){
//    		Controller.sendCommand(context, selectedContactDeviceDataList, 
//    			CommandEnum.stop, null, null);
//    	}
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

//IDialogOnClickAction notificationDialogOnClickAction = new IDialogOnClickAction() {
//
//	@Override
//	public void doOnPositiveButton() {
//	}
//	@Override
//	public void doOnNegativeButton() {
//		// TODO Auto-generated method stub
//	}
//	@Override
//	public void setActivity(Activity activity) {
//		// TODO Auto-generated method stub
//	}
//	@Override
//	public void setContext(Context context) {
//		// TODO Auto-generated method stub
//	}
//	@Override
//	public void setParams(Object[]... objects) {
//		// TODO Auto-generated method stub
//	}
//	@Override
//	public void doOnChooseItem(int which) {
//		// TODO Auto-generated method stub
//	}
//};
//
////private void showGoogleServiceNotAvailable(String errorMessage) {
//private CommonDialog showNotificationDialog(String errorMessage) {
//	//String dialogMessage = "\nGoogle Cloud Service is not available right now.\n\nPlease try later.\n";
//	String dialogMessage = errorMessage;
//	
//	CommonDialog aboutDialog = new CommonDialog(this, notificationDialogOnClickAction);
//	aboutDialog.setDialogMessage(dialogMessage);
//	aboutDialog.setDialogTitle("Warning");
//	aboutDialog.setPositiveButtonText("OK");
//	aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
//	aboutDialog.showDialog();
//	aboutDialog.setCancelable(true);
//	return aboutDialog;
//}

