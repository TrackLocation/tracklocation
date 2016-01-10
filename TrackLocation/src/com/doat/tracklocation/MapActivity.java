package com.doat.tracklocation;

import java.io.IOException;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.doat.tracklocation.broadcast.BroadcastReceiverMapActivity;
import com.doat.tracklocation.concurrent.TrackLocationServiceLauncher;
import com.doat.tracklocation.controller.ContactListController;
import com.doat.tracklocation.controller.MainActivityController;
import com.doat.tracklocation.controller.MapActivityController;
import com.doat.tracklocation.datatype.ActionMenuObj;
import com.doat.tracklocation.datatype.BackupDataOperations;
import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.datatype.BroadcastConstEnum;
import com.doat.tracklocation.datatype.BroadcastData;
import com.doat.tracklocation.datatype.ContactData;
import com.doat.tracklocation.datatype.ContactDeviceData;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.MapMarkerDetails;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.datatype.MessageDataLocation;
import com.doat.tracklocation.db.DBConst;
import com.doat.tracklocation.dialog.CommonDialog;
import com.doat.tracklocation.dialog.ICommonDialogOnClickListener;
import com.doat.tracklocation.dialog.InfoDialog;
import com.doat.tracklocation.exception.DefaultExceptionHandler;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.model.ContactDeviceDataListModel;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.MapUtils;
import com.doat.tracklocation.utils.Preferences;
import com.doat.tracklocation.utils.ResizeAnimation;
import com.doat.tracklocation.utils.Utils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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

public class MapActivity extends BaseActivity implements LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, OnTouchListener{
	private String logMessage;	
	private final static float DEFAULT_CAMERA_UPDATE = 15;
	private static final int ANIMATION_DURATION = 500;
	private final static int RETRY_DELAY_TO_START_TLS_MS = 20000; // [milliseconds]
	
	private LocationManager locationManager;
	private LatLng lastKnownLocation;
	private BroadcastReceiver gcmLocationUpdatedWatcher;
	private BroadcastReceiver notificationBroadcastReceiver;
	private GoogleMap map;

	private float zoom;
	private ContactDeviceDataList selectedContactDeviceDataList = new ContactDeviceDataList();
	private ContactDeviceDataList contactDeviceDataList;
	private HashMap<String, ContactData> selectedAccountList;
	private int contactsQuantity;
	private boolean isShowAllMarkersEnabled;
	private boolean isMapInMovingState = false;;
	private Context context;
	private Thread trackLocationServiceLauncherThread;
	private TrackLocationServiceLauncher trackLocationServiceLauncher;
	private boolean isPermissionDialogShown;

	private LinkedHashMap<String, MapMarkerDetails> mapMarkerDetailsList = new LinkedHashMap<String, MapMarkerDetails>();
	private MapMarkerDetails selectedMarkerDetails = null;
	
	private TextView info_preview;
	private TextView title_text;
	
	private float startY;
	protected LinearLayout map_popup_first;
	private Animation animUp;
	private Animation animDown;
	private LinearLayout map_popup_second;
	private LinearLayout layoutAccountMenu;
	private ImageButton btnMyLocation;
	
    public static volatile boolean isTrackLocationRunning; // Used in SMSReceiver.class
	
	float mLastTouchX = 0;
	float mLastTouchY = 0;
	private int mActivePointerId = -1;
	
	private DialogStatus viewStatus;
	TimerTask timerTask;
	//we are going to use a handler to be able to run in our TimerTask
	private final Handler handler = new Handler();
	private Timer timer;
	private boolean bLockMapNothOnly;	
	
	private ContactListArrayAdapter adapterFavorites;
	private ContactListArrayAdapter adapterContacts;
	private ListView lvFavorites;
	private ContactDeviceDataList favContactsDeviceDataList;
	
	private ListView lvContacts;		
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ArrayList<ActionMenuObj> mActionMenuList;
	
	private MapActivityController mapActivityController;
	
	private MainActivityController mainActivityController;

	private ContactListController contactListController;

	private LinearLayout contact_quick_info;
	private CheckBox quick_info_fav;
	private TextView quick_info_name;
    private int iContactQuickInfoWidth = -1;
	private AdView adView;

    private enum DialogStatus{
		Opened, Closed
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        getActionBar().hide();
		super.onCreate(savedInstanceState);
		className = this.getClass().getName();
		methodName = "onCreate";
    	isPermissionDialogShown = false;
		setContentView(R.layout.map);	
		
		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);

		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		isTrackLocationRunning = true;
		context = getApplicationContext();
		selectedAccountList = new HashMap<String, ContactData>();

		Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(MapActivity.this, MapActivity.class));
		final Intent intent = getIntent();
		// If activity has been restarted after crash by DefaultExceptionHandler
		// start SENDTO activity to send exception details to support team
		if(intent.getExtras() != null && intent.getExtras().containsKey(CommonConst.UNHANDLED_EXCEPTION_EXTRA)){
			String exceptionDetails = intent.getExtras().getString(CommonConst.UNHANDLED_EXCEPTION_EXTRA);
			if(exceptionDetails != null && !exceptionDetails.isEmpty()){
				String uriText =
				    "mailto:" + CommonConst.SUPPORT_MAIL +
				    "?subject=" + Uri.encode("TrackLocation unhandled exception") + 
				    "&body=" + Uri.encode(exceptionDetails);
				Uri uri = Uri.parse(uriText);
				Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
				sendIntent.setData(uri);
				sendIntent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK );
				startActivity(Intent.createChooser(sendIntent, "Please send exception details to support team."));
			}
		}

		
		if(mainActivityController == null){
        	mainActivityController = new MainActivityController(MapActivity.this, context);
        }	
		if(contactListController == null){
			contactListController = new ContactListController(this, getApplicationContext());
		}
		if(mapActivityController == null){
			mapActivityController = new MapActivityController();
		}

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
		loadActionMenu();

        loadContactQuickInfo();
		
		loadFavoritsForLocateContacts();
		

		isShowAllMarkersEnabled = true;

		MyMapFragment myMapFragment = getHandleToMapFragment();
        
        map = myMapFragment.getMap();

        setupLocation();
        
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
		
		loadBottomActionPanel();
		
		adView = (AdView)this.findViewById(R.id.adMap);
	    AdRequest adRequest = new AdRequest.Builder().build();
	    adView.loadAd(adRequest);

		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

	@Override
	protected void onStart() {	
		super.onStart();
		methodName = "onStart";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		initGcmLocationUpdatedBroadcastReceiver();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		bLockMapNothOnly = sharedPref.getBoolean("pref_map_lock", false);
		
		if(notificationBroadcastReceiver == null){
			notificationBroadcastReceiver = new BroadcastReceiverMapActivity(MapActivity.this);
		}
		initNotificationBroadcastReceiver(notificationBroadcastReceiver);
		
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	}

	@Override
	protected void onPause() {
		methodName = "onPause";

        BackupDataOperations backupData = new BackupDataOperations();
		boolean isBackUpSuccess = backupData.backUp();
		if(isBackUpSuccess != true){
			logMessage = methodName + " -> Backup process failed.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + methodName + ": " + logMessage);
		}
		adView.pause();
		super.onPause();
	}
	
	@Override
	protected void onResume() {	
		super.onResume();
		adView.resume();
	}	

    @Override
    protected void onStop() {
    	methodName = "onStop";
    	
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    	// Stop thread that checking which contacts are online
        if(contactListController != null){
        	contactListController.stopCheckWhichContactsOnLineThread();
        }

		if(mapActivityController != null){
			mapActivityController.stopKeepAliveTrackLocationService();
		}
    	String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
    	Preferences.clearPreferencesReturnToContactMap(context, account);
    	
    	Log.i(CommonConst.LOG_TAG, "Timer with mapKeepAliveTimerJob - stopped");
    	if(gcmLocationUpdatedWatcher != null){
    		unregisterReceiver(gcmLocationUpdatedWatcher);
    	}
    	
    	if(notificationBroadcastReceiver != null){
    		unregisterReceiver(notificationBroadcastReceiver);
    	}
    	if(trackLocationServiceLauncherThread != null){
    		trackLocationServiceLauncherThread.interrupt();
    	}
    	
    	isPermissionDialogShown = false;
    	
      	Thread registerToGCMInBackgroundThread = 
              	mainActivityController.getRegisterToGCMInBackgroundThread();
     	if(registerToGCMInBackgroundThread != null){
     		registerToGCMInBackgroundThread.interrupt();
     	}

     	BackupDataOperations backupData = new BackupDataOperations();
 		boolean isBackUpSuccess = backupData.backUp();
 		if(isBackUpSuccess != true){
 			logMessage = methodName + " -> Backup process failed.";
 			LogManager.LogErrorMsg(className, methodName, logMessage);
 			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
 		}
    	
		LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_Stop] {" + className + "} -> " + methodName);

    	super.onStop();
    }

    @Override
    protected void onDestroy() {
    	isTrackLocationRunning = false;
    	adView.destroy();
    	super.onDestroy();        
    }
    
	private void loadBottomActionPanel() {
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
	        		InfoDialog joinRequestDialog = new InfoDialog(MapActivity.this, context, title, dialogMessage, infoDialogOnClickListener);
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
                if (selectedMarkerDetails != null) {
                    double lat = selectedMarkerDetails.getLocationDetails().getLat();
                    double lng = selectedMarkerDetails.getLocationDetails().getLng();
                    final String uri = String.format(Locale.getDefault(), "geo:%f,%f?q=%f,%f", lat, lng, lat, lng);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                }
            }
        });
	}

	private MyMapFragment getHandleToMapFragment() {
		// Get a handle to the Map Fragment
        MyMapFragment myMapFragment = (MyMapFragment) getFragmentManager().findFragmentById(R.id.map);
        myMapFragment.setOnDragListener(new MapWrapperLayout.OnDragListener() {

            @Override
            public void onDrag(MotionEvent motionEvent) {
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
                        if (pointerIndex >= 0) {
                            final float x = MotionEventCompat.getX(motionEvent, pointerIndex);
                            final float y = MotionEventCompat.getY(motionEvent, pointerIndex);

                            // Calculate the distance moved
                            if (Math.abs(x - mLastTouchX) > 5 || Math.abs(y - mLastTouchY) > 5) {
                                if (mapMarkerDetailsList.size() > 0) {
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
		return myMapFragment;
	}	
	
	private void loadActionMenu() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		
		mDrawerList = (ListView) findViewById(R.id.left_drawer);       
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mActionMenuList = Controller.getActionMenuList(this);
        mDrawerList.setAdapter(new MenuActionListAdapter(this, mActionMenuList));
		
		/*NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
			
			@Override
			public boolean onNavigationItemSelected(MenuItem item) {
				int id = item.getItemId();
				switch (id) {
				case R.id.action_join:
					String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		        	if(account == null || account.isEmpty()){
		    	    	Toast.makeText(MapActivity.this, "Please register your application.\nPress Locate button at first.", Toast.LENGTH_SHORT).show();
		        		LogManager.LogErrorMsg(className, "onClick -> JOIN button", "Unable to join contacts - application is not registred yet.");
		        	} else {	        		
		        		Intent joinContactsListIntent = new Intent(MapActivity.this, JoinContactsListActivity.class);
		        		startActivity(joinContactsListIntent);
		        	}
					break;
				case R.id.action_location: 
					LogManager.LogInfoMsg(className, "onClick -> Location Sharing Management button", 
			    			"ContactList activity started.");
			    		    		
		    		if(contactDeviceDataList != null){
			    		Intent intentContactList = new Intent(MapActivity.this, LocationSharingListActivity.class);
			    		intentContactList.putParcelableArrayListExtra(CommonConst.CONTACT_DEVICE_DATA_LIST, contactDeviceDataList);
			    		startActivity(intentContactList);
		    		} else {
		    	    	Toast.makeText(MapActivity.this, "There is no any contact.\nJoin some contact at first.", 
		    	    		Toast.LENGTH_SHORT).show();
		        		LogManager.LogInfoMsg(className, "onClick -> LOCATION SHARING MANAGEMENT button", 
		                    "There is no any contact. Some contact must be joined at first.");
		    		}
					break;
				case R.id.action_tracking:
					LogManager.LogInfoMsg(className, "onClick -> Tracking button", 
			    			"TrackingList activity started.");
			    		    		
		    		if(contactDeviceDataList != null){
			    		Intent intentContactList = new Intent(MapActivity.this, TrackingListActivity.class);
			    		intentContactList.putParcelableArrayListExtra(CommonConst.CONTACT_DEVICE_DATA_LIST, contactDeviceDataList);
			    		startActivity(intentContactList);
		    		} else {
		    	    	Toast.makeText(MapActivity.this, "There is no any contact.\nJoin some contact at first.", 
		    	    		Toast.LENGTH_SHORT).show();
		        		LogManager.LogInfoMsg(className, "onClick -> TRACKING button", 
		                    "There is no any contact. Some contact must be joined at first.");
		    		}
					break;
				case R.id.action_settings:
					Intent settingsIntent = new Intent(MapActivity.this, SettingsActivity.class);
		    		startActivity(settingsIntent);
		    		//startActivityForResult(settingsIntent, 2);
					break;
				case R.id.action_about:
					String title = "About";
		        	String dialogMessage = String.format(getResources().getString(R.string.about_dialog_text), 
		        		Preferences.getPreferencesString(context, CommonConst.PREFERENCES_VERSION_NAME));
		        	InfoDialog dlg = new InfoDialog(MapActivity.this, context, title, dialogMessage, null);
		        	break;
				case R.id.action_contacts :
					Intent intentContactList = new Intent(MapActivity.this, ContactListActivity.class);
		    		intentContactList.putParcelableArrayListExtra(CommonConst.CONTACT_DEVICE_DATA_LIST, contactDeviceDataList);	    		
		    		startActivity(intentContactList);
					break;
				}
								
				mDrawerLayout.closeDrawer(GravityCompat.START);
		        return true;

			}
		});	*/	
		
        ImageButton btnMenu = (ImageButton) findViewById(R.id.menu_view_btn);
        btnMenu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                closeQuickContactInfo();
                if (mDrawerLayout.isActivated()) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    mDrawerLayout.openDrawer(Gravity.LEFT);
				}				
			}
		});
	}	
	
	private void loadFavoritsForLocateContacts(){
		contactDeviceDataList = ContactDeviceDataListModel.getInstance().getContactDeviceDataList(false); 
		
    	lvContacts = (ListView) findViewById(R.id.contacts_list);
	    lvFavorites = (ListView) findViewById(R.id.favorites_list);

    	ImageButton btnContacts = (ImageButton) findViewById(R.id.contacts_view_btn);
	    btnContacts.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
                closeQuickContactInfo();
		    	// Stop thread that checking which contacts are online
		        if(contactListController != null){
		        	contactListController.stopCheckWhichContactsOnLineThread();
		        }

		        if (favContactsDeviceDataList != null){
					favContactsDeviceDataList.removeAll(favContactsDeviceDataList);
					favContactsDeviceDataList = null;
					adapterFavorites.notifyDataSetChanged();
					lvFavorites.setVisibility(View.INVISIBLE);
				}
					        	
				if (lvContacts.getVisibility() == View.GONE  || lvContacts.getVisibility() == View.INVISIBLE){						
					lvContacts.setVisibility(View.VISIBLE);
				}else{					
					lvContacts.setVisibility(View.GONE);
				}
				
				// Start thread to check which contacts are online
				if(contactListController != null){
					State state = contactListController.getCheckWhichContactsOnLineThreadState();
					if(state == null || state.equals(Thread.State.TERMINATED)){
						contactListController.startCheckWhichContactsOnLineThread(contactDeviceDataList);
					}
				}
			}
		});
		
		Controller.fillContactDeviceData(MapActivity.this, contactDeviceDataList, null, null, null);
	    if(contactDeviceDataList == null){
	    	logMessage = "Unexpected state - no contacts.";
	    	LogManager.LogErrorMsg(className, methodName, logMessage);
	    	Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
	    	return;
	    }
	    			
    	adapterContacts = new ContactListArrayAdapter(this, R.layout.map_contact_item, R.id.contact, contactDeviceDataList, null, null, null);    	
        ((ContactListArrayAdapter) adapterContacts).setActiveStatusDraw(true);        
        
        ContactDeviceDataListModel.getInstance().setAdapter("adapterContacts", adapterContacts);
        
        lvContacts.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvContacts.setAdapter(adapterContacts);
	    
	    lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                closeQuickContactInfo();
                final ContactDeviceData selectedValue = (ContactDeviceData) adapterContacts.getItem(position);
                boolean isAdd = false;

                SparseBooleanArray checked = lvContacts.getCheckedItemPositions();
                ContactDeviceData cdd = contactDeviceDataList.get(position);
                if (checked.get(position)) {
                    cdd.getContactData().setContactStatus(CommonConst.CONTACT_STATUS_PENDING);
                    selectedContactDeviceDataList.add(cdd);
                    selectedAccountList.put(selectedValue.getContactData().getEmail(), selectedValue.getContactData());
                    isAdd = true;
                } else {
                    cdd.getContactData().setContactStatus(CommonConst.CONTACT_STATUS_CONNECTED);
                    selectedContactDeviceDataList.remove(cdd);
                    selectedAccountList.remove(selectedValue.getContactData().getEmail());
                    if (mapMarkerDetailsList.containsKey(selectedValue.getContactData().getEmail())) {
                        mapMarkerDetailsList.get(selectedValue.getContactData().getEmail()).getLocationCircle().remove();
                        mapMarkerDetailsList.get(selectedValue.getContactData().getEmail()).getMarker().remove();
                        mapMarkerDetailsList.remove(selectedValue.getContactData().getEmail());
                    }
                    isAdd = false;
                }
                isShowAllMarkersEnabled = true;

                ContactDeviceDataListModel.getInstance().notifyDataSetChanged();
                if (trackLocationServiceLauncherThread != null) {
                    trackLocationServiceLauncherThread.interrupt();
                }
                startTrackLocationServiceLauncher(contactDeviceDataList.get(position), isAdd);
            }
        });

        lvContacts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                closeQuickContactInfo();
                int iWidth = contact_quick_info.getLayoutParams().width;
                if (iWidth == 0){
                    iWidth = iContactQuickInfoWidth;
                }
                else{
                    iContactQuickInfoWidth = iWidth;
                }

                contact_quick_info.getLayoutParams().width = 0;
                int iParentWidth = ((LinearLayout)parent.getParent()).getLeft();
				int iQuickInfoTop = view.getTop() + (view.getHeight() - contact_quick_info.getLayoutParams().height)/2;
                ResizeAnimation anim = new ResizeAnimation(contact_quick_info, 0, contact_quick_info.getLayoutParams().height, iWidth , contact_quick_info.getLayoutParams().height);
                               ((RelativeLayout.LayoutParams) contact_quick_info.getLayoutParams()).setMargins(0, iQuickInfoTop, 0, 0);

                quick_info_fav.setChecked(contactDeviceDataList.get(position).isFavorite());
                quick_info_fav.setTag(position);
                quick_info_name.setText(contactDeviceDataList.get(position).getContactData().getNick());
                contact_quick_info.setVisibility(View.VISIBLE);
                contact_quick_info.startAnimation(anim);
                return true;
            }
        });
	    
	    //registerForContextMenu(lvContacts);
	    
	    selectedContactDeviceDataList.clear();
	    for (int i = 0; i < contactDeviceDataList.size(); i++) {
			ContactDeviceData cdData = contactDeviceDataList.get(i);
			if (cdData.isFavorite()){
				selectedContactDeviceDataList.add(cdData);				
				lvContacts.setItemChecked(i, true);				
			}
		}
	    
	    if (!selectedContactDeviceDataList.isEmpty()){
			Controller.fillContactDeviceData(MapActivity.this, selectedContactDeviceDataList, null, null, null);
			favContactsDeviceDataList = (ContactDeviceDataList) selectedContactDeviceDataList.clone();
		    if(favContactsDeviceDataList != null){
		    	adapterFavorites = new ContactListArrayAdapter(this, R.layout.map_contact_item, R.id.contact, favContactsDeviceDataList, null, null, null);
		    	ContactDeviceDataListModel.getInstance().setAdapter("adapterFovarites", adapterFavorites);
		        ((ContactListArrayAdapter) adapterFavorites).setActiveStatusDraw(true);
		        ((ContactListArrayAdapter) adapterFavorites).setSecondaryContactStatus(CommonConst.CONTACT_STATUS_PENDING);
		    
		    	lvFavorites.setAdapter(adapterFavorites);
		    	
				// Start thread to check which contacts are online
				if(contactListController != null){
					State state = contactListController.getCheckWhichContactsOnLineThreadState();
					if(state == null || state.equals(Thread.State.TERMINATED)){
						contactListController.startCheckWhichContactsOnLineThread(favContactsDeviceDataList);
					}
				}
				
	        	ContactDeviceDataListModel.getInstance().notifyDataSetChanged();
				if(trackLocationServiceLauncherThread != null){
					trackLocationServiceLauncherThread.interrupt();
				}
				startTrackLocationServiceLauncher(null, true);
		    }	    
	    }
	    else{
	    	lvFavorites.setVisibility(View.GONE);
	    	btnContacts.callOnClick();
			// Start thread to check which contacts are online
			if(contactListController != null){
				State state = contactListController.getCheckWhichContactsOnLineThreadState();
				if(state == null || state.equals(Thread.State.TERMINATED)){
					contactListController.startCheckWhichContactsOnLineThread(contactDeviceDataList);
				}
			}
	    }
	}

    private void loadContactQuickInfo() {
        if (contact_quick_info == null){
            contact_quick_info = (LinearLayout) findViewById(R.id.contact_quick_info);
            contact_quick_info.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return closeQuickContactInfo();
                }
            });
            quick_info_fav = (CheckBox)contact_quick_info.findViewById(R.id.qi_favbutton);
            quick_info_fav.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkFav = (CheckBox) v;
                    int iPosition = (Integer) checkFav.getTag();
                    
                    Map<String, Object> m = new HashMap<String, Object>();
					m.put(DBConst.CONTACT_DEVICE_IS_FAVORITE, checkFav.isChecked() ? 1 : 0);
					ContactDeviceDataListModel.getInstance().updateContactDeviceDataList(contactDeviceDataList.get(iPosition), m);
		            contactDeviceDataList.get(iPosition).setFavorite(checkFav.isChecked());
		            ContactDeviceDataListModel.getInstance().notifyDataSetChanged();                    
                }
            });
            quick_info_name = (TextView)contact_quick_info.findViewById(R.id.qi_contact);
            contact_quick_info.setVisibility(View.GONE);
        }
    }

    private boolean closeQuickContactInfo() {
        if (contact_quick_info.getVisibility() == View.VISIBLE) {
            final int iWidth = contact_quick_info.getLayoutParams().width;
            ResizeAnimation anim = new ResizeAnimation(contact_quick_info, iWidth, contact_quick_info.getLayoutParams().height, 0, contact_quick_info.getLayoutParams().height);

            anim.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    contact_quick_info.setVisibility(View.GONE);
                    contact_quick_info.getLayoutParams().width = iWidth;
                    contact_quick_info.requestLayout();
                }
            });

            contact_quick_info.startAnimation(anim);
        }
        return true;
    }

    private void startTrackLocationServiceLauncher(ContactDeviceData inContactDeviceData, boolean isAddAction){
		methodName = "startTrackLocationService";
		
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

		if(selectedContactDeviceDataList == null || selectedContactDeviceDataList.isEmpty()){
			return;
		}
		
		if(inContactDeviceData != null){
			if (isAddAction){
				selectedContactDeviceDataList.add(inContactDeviceData);
			}
			else{
				selectedContactDeviceDataList.remove(inContactDeviceData);
			}	
		}

		List<String> recipientList = new ArrayList<String>();
		//if(!selectedContactDeviceDataList.isEmpty()){
		contactsQuantity = selectedContactDeviceDataList.size();
		// Create and fill all requested accounts that should be shown on the location map
		selectedAccountList.clear();
		for (ContactDeviceData contactDeviceData : selectedContactDeviceDataList) {
			ContactData contactData = contactDeviceData.getContactData();
			if(contactData != null){
				selectedAccountList.put(contactData.getEmail(), contactData);	
				recipientList.add(contactData.getEmail());
			}
		}
		
		logMessage = "Selected accounts to get their location: " + gson.toJson(recipientList);
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		
		String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		String macAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
		String phoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
		String registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
		MessageDataContactDetails senderMessageDataContactDetails = // sender contact details
				new MessageDataContactDetails(account, macAddress, phoneNumber, registrationId, 
					Controller.getBatteryLevel(context));

		trackLocationServiceLauncher = new TrackLocationServiceLauncher(
			context,
			selectedContactDeviceDataList,
			senderMessageDataContactDetails,
			RETRY_DELAY_TO_START_TLS_MS); // retry delay in milliseconds to start TrackLocation Service for all recipients
		// ===========================================================================
		// Start TrackLocation Service for all requested recipients
		// ===========================================================================
		
		try {
			trackLocationServiceLauncherThread = new Thread(trackLocationServiceLauncher);
			//if (!selectedContactDeviceDataList.isEmpty() ){
				trackLocationServiceLauncherThread.start();
				logMessage = "Send COMMAND: START TrackLocationService in separate thread " +
					"to the following recipients: " + gson.toJson(recipientList);
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
			//}
		} catch (IllegalThreadStateException e) {
			logMessage = "Failed to Send COMMAND: START TrackLocationService in separate thread " +
				"to the following recipients: " + gson.toJson(recipientList);
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
		}
		// TODO: - Should be removed when new UI will be ready		
		String accountsListMsg = "";
		if(selectedAccountList != null && !selectedAccountList.isEmpty()){
			for (ContactData contcatData : selectedAccountList.values()) {
				String contcatName = contcatData.getNick() == null ? contcatData.getEmail() : contcatData.getNick();
				accountsListMsg = " - " + contcatName + "\n" + accountsListMsg;
			}
		} else {
			accountsListMsg  = "Not provided recipients list...";
		}
				
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
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
			Toast.makeText(MapActivity.this, getString(R.string.err_load_location),
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
		    			for (int i = 0; i < contactDeviceDataList.size(); i++) {
	    					ContactDeviceData cData = contactDeviceDataList.get(i);
	    					if (cData.getContactData().getEmail().equals(updatingAccount)){
	    						cData.getContactData().setContactStatus(CommonConst.CONTACT_STATUS_CONNECTED);
	    						ContactDeviceDataListModel.getInstance().notifyDataSetChanged();
	    						//adapterContacts.notifyDataSetChanged();
	    					}
						}
		    			
		    			
		    			MapMarkerDetails  mapMarkerDetails = createMapMarker(contactDetails, locationDetails);
		    			if (mapMarkerDetails != null){
		    				mapMarkerDetailsList.put(updatingAccount, mapMarkerDetails);
		    			}
		    			if (favContactsDeviceDataList != null){
			    			try {
			    				int indexToRemove = -1;
			    				for (int i = 0; i < favContactsDeviceDataList.size(); i++) {
			    					ContactDeviceData cData = favContactsDeviceDataList.get(i);
			    					if (cData.getContactData().getEmail().equals(updatingAccount)){
			    						indexToRemove = i;	
			    					}
								}
			    				if (indexToRemove > -1){
			    					favContactsDeviceDataList.remove(indexToRemove);
									adapterFavorites.notifyDataSetChanged();
			    				}
								
								if (favContactsDeviceDataList.isEmpty()){
									favContactsDeviceDataList = null;
									lvFavorites.setVisibility(View.INVISIBLE);									
								}														
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		    			}
		    		}
		    		
		    		mapAnimateCameraForMarkers(prevLocationDetails, updatingAccount);
	    		}	    		
    		}
	    };
	    
	    registerReceiver(gcmLocationUpdatedWatcher, intentFilter);
	    LogManager.LogFunctionExit("ContactConfiguration", "initGcmIntentServiceWatcher");
    }
	
	private void mapAnimateCameraForMarkers(MessageDataLocation prevLocationDetails, String updatingAccount) {
		if (!isMapInMovingState ){
			if(mapMarkerDetailsList.size() > 1 && isShowAllMarkersEnabled == true) {
				// put camera to show all markers
				CameraUpdate cu = MapUtils.createCameraUpdateLatLngBounds(mapMarkerDetailsList);
				map.animateCamera(cu);
				if(mapMarkerDetailsList.size() >= contactsQuantity){
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
		
		Bitmap bmpContact = null;
		if(contactDeviceData != null && contactDeviceData.getContactData() != null){
			bmpContact = contactDeviceData.getContactData().getContactPhoto() == null ? Utils.getDefaultContactBitmap(getResources()) : contactDeviceData.getContactData().getContactPhoto();
		}
		
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
    	super.onCreateContextMenu(menu, v, menuInfo);	    	    
	    menu.add(0, 0, 0, "Add to favorites");	    
    }

   @Override
   public boolean onContextItemSelected(MenuItem item) {       
       AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
       int position = info.position;
       contactDeviceDataList.get(position).setFavorite(!contactDeviceDataList.get(position).isFavorite());
       
       return true;
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

	ICommonDialogOnClickListener infoDialogOnClickListener = new ICommonDialogOnClickListener(){

		@Override
		public void doOnPositiveButton(Object data) {
			if (selectedMarkerDetails.getContactDetails() != null){
				Controller.RingDevice(context, className, selectedMarkerDetails.getContactDetails());
			}
		}

		@Override
		public void doOnNegativeButton(Object data) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void doOnChooseItem(int which) {
			// TODO Auto-generated method stub
			
		}
		
	};

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
    	//super.onBackPressed();
    	
    	CommonDialog quitDialog = new CommonDialog(this, quitListener);
    	quitDialog.setDialogMessage("Do you want to shut down TrackLocation?");
    	quitDialog.setDialogTitle("Shut Down");
    	quitDialog.setPositiveButtonText("Shut Down");
    	quitDialog.setNegativeButtonText("No");
    	quitDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
    	quitDialog.setCancelable(false);
    	quitDialog.showDialog();
    	
    	//finish();
    }

    ICommonDialogOnClickListener quitListener = new ICommonDialogOnClickListener(){
		@Override
		public void doOnPositiveButton(Object data) {
	    	finish();
		}

		@Override
		public void doOnNegativeButton(Object data) {
			// TODO Auto-generated method stub
		}

		@Override
		public void doOnChooseItem(int which) {
			// TODO Auto-generated method stub
		}
    };
    
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	setActionItem(position);
        }

		private void setActionItem(int position) {			
        	int iAction =  mActionMenuList.get(position).getKey();
            switch (iAction) {
			case 0:
				String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
	        	if(account == null || account.isEmpty()){
	    	    	Toast.makeText(MapActivity.this, "Please register your application.\nPress Locate button at first.", Toast.LENGTH_SHORT).show();
	        		LogManager.LogErrorMsg(className, "onClick -> JOIN button", "Unable to join contacts - application is not registred yet.");
	        	} else {	        		
	        		Intent joinContactsListIntent = new Intent(MapActivity.this, JoinContactsListActivity.class);
	        		startActivity(joinContactsListIntent);
	        	}
				break;
			case 1 : 
				LogManager.LogInfoMsg(className, "onClick -> Location Sharing Management button", 
		    			"ContactList activity started.");
		    		    		
	    		if(contactDeviceDataList != null){
		    		Intent intentContactList = new Intent(MapActivity.this, LocationSharingListActivity.class);
		    		intentContactList.putParcelableArrayListExtra(CommonConst.CONTACT_DEVICE_DATA_LIST, contactDeviceDataList);
		    		startActivity(intentContactList);
	    		} else {
	    	    	Toast.makeText(MapActivity.this, "There is no any contact.\nJoin some contact at first.", 
	    	    		Toast.LENGTH_SHORT).show();
	        		LogManager.LogInfoMsg(className, "onClick -> LOCATION SHARING MANAGEMENT button", 
	                    "There is no any contact. Some contact must be joined at first.");
	    		}
				break;
			case 2 :
				LogManager.LogInfoMsg(className, "onClick -> Tracking button", 
		    			"TrackingList activity started.");
		    		    		
	    		if(contactDeviceDataList != null){
		    		Intent intentContactList = new Intent(MapActivity.this, TrackingListActivity.class);
		    		intentContactList.putParcelableArrayListExtra(CommonConst.CONTACT_DEVICE_DATA_LIST, contactDeviceDataList);
		    		startActivity(intentContactList);
	    		} else {
	    	    	Toast.makeText(MapActivity.this, "There is no any contact.\nJoin some contact at first.", 
	    	    		Toast.LENGTH_SHORT).show();
	        		LogManager.LogInfoMsg(className, "onClick -> TRACKING button", 
	                    "There is no any contact. Some contact must be joined at first.");
	    		}
				break;
			case 3 :
				Intent settingsIntent = new Intent(MapActivity.this, SettingsActivity.class);
	    		startActivity(settingsIntent);
	    		//startActivityForResult(settingsIntent, 2);
				break;
			case 4:
				String title = "About";
	        	String dialogMessage = String.format(getResources().getString(R.string.about_dialog_text), 
	        		Preferences.getPreferencesString(context, CommonConst.PREFERENCES_VERSION_NAME));
	        	InfoDialog dlg = new InfoDialog(MapActivity.this, context, title, dialogMessage, null);
	        	break;
			case 5 :
				Intent intentContactList = new Intent(MapActivity.this, ContactListActivity.class);
	    		intentContactList.putParcelableArrayListExtra(CommonConst.CONTACT_DEVICE_DATA_LIST, contactDeviceDataList);	    		
	    		startActivity(intentContactList);
				break;
			}
            mDrawerLayout.closeDrawer(mDrawerList);
		}
    }  
    
    public ContactListController getContactListController() {
		return contactListController;
	}
    
	public void updateContactStatusInListView(String senderAccount){
		
		if(senderAccount != null){
			for (ContactDeviceData cdd : contactDeviceDataList) {
				if (senderAccount.equals(cdd.getContactData().getEmail())){
					if (cdd.getContactData().getContactStatus() != CommonConst.CONTACT_STATUS_CONNECTED){						
						cdd.getContactData().setContactStatus(CommonConst.CONTACT_STATUS_CONNECTED);
						adapterContacts.notifyDataSetChanged();
					}
					break;
				}
			}
			if (favContactsDeviceDataList != null){
				for (ContactDeviceData cdd : favContactsDeviceDataList) {
					if (senderAccount.equals(cdd.getContactData().getEmail())){
						if (cdd.getContactData().getContactStatus() != CommonConst.CONTACT_STATUS_CONNECTED){
							cdd.getContactData().setContactStatus(CommonConst.CONTACT_STATUS_CONNECTED);
							adapterFavorites.notifyDataSetChanged();
						}
						break;
					}
				}
			}
		}
	}

	public void showPermissionsInfoDialog(String dialogMessage){
		if(isPermissionDialogShown == false){
			String title = "Warning";
    		new InfoDialog(MapActivity.this, context, title, dialogMessage, null);
			isPermissionDialogShown = true;
		}
	}
	
	public void updateContactsList(MessageDataContactDetails contactSentJoinRequest){
		contactDeviceDataList = ContactDeviceDataListModel.getInstance().getContactDeviceDataList(true);
		adapterContacts.clear();
		adapterContacts.addAll(contactDeviceDataList);
		Controller.fillContactDeviceData(this, contactDeviceDataList, null, null, null); 
		ContactDeviceDataListModel.getInstance().notifyDataSetChanged();
		// Start thread to check which contacts are online
		if(contactListController != null){
			State state = contactListController.getCheckWhichContactsOnLineThreadState();
			if(state == null || state.equals(Thread.State.TERMINATED)){
				contactListController.startCheckWhichContactsOnLineThread(contactDeviceDataList);
			}
		}
	}
	
	// Initialize BROADCAST_MESSAGE broadcast receiver
	private void initNotificationBroadcastReceiver(BroadcastReceiver broadcastReceiver) {
		methodName = "initNotificationBroadcastReceiver";
		LogManager.LogFunctionCall(className, methodName);
		
		IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(BroadcastActionEnum.BROADCAST_MESSAGE.toString());
	    
	    registerReceiver(broadcastReceiver, intentFilter);
	    
		LogManager.LogFunctionExit(className, methodName);
	}
}