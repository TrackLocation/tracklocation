package com.doat.tracklocation;

import java.util.Timer;
import java.util.TimerTask;

import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class MainActivity extends BaseActivity {
	private InterstitialAd mInterstitialAd;
	private Handler handler = new Handler();
	private Timer timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Create the InterstitialAd and set the adUnitId.
        mInterstitialAd = new InterstitialAd(this);
        // Defined in res/values/strings.xml
        mInterstitialAd.setAdUnitId("ca-app-pub-6783162973293781/6666323357");
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
        AdListener adListener = new AdListener() {
        	@Override
        	public void onAdLoaded() {        		
        		super.onAdLoaded();
        		mInterstitialAd.show();
        		startTimer();
        	}
        	
        	@Override
        	public void onAdClosed() {        		
        		super.onAdClosed();
        		stoptimertask();
        		Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
        		startActivity(mapIntent);
        		MainActivity.this.finish();
        	}
		};
		
        mInterstitialAd.setAdListener(adListener);        

		
		className = this.getClass().getName();
		methodName = "onCreate";
		
		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);
		
		setContentView(R.layout.activity_main);
				
    }
	
	private void startTimer() {
    	timer = new Timer();
        //initialize the TimerTask's job
        TimerTask timerTask = new TimerTask() {
            public void run() {            	
                handler.post(new Runnable() {
                    public void run() {
                    	mInterstitialAd = null;
                    }
                });
            }
        };
        
         timer.schedule(timerTask, 5000); //
    }
	
    private void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

//
//	@Override
//	protected void onStart() {
//		super.onStart();
//		if(broadcastReceiver == null){
//			broadcastReceiver = new BroadcastReceiverBase(MainActivity.this);
//		}
//		initNotificationBroadcastReceiver(broadcastReceiver);
//	}
//
//	@Override
//	protected void onResume() {
//		super.onResume();
//		DBManager.initDBManagerInstance(new DBHelper(context));
//		if(mainModel == null){
//			mainModel = new MainModel();
//		}
//		if(mainActivityController == null){
//			mainActivityController = new MainActivityController(this, context);
//		}		
//	}
//
//	@Override
//	protected void onPause() {
//		super.onPause();
//		methodName = "onPause";
//
//        BackupDataOperations backupData = new BackupDataOperations();
//		boolean isBackUpSuccess = backupData.backUp();
//		if(isBackUpSuccess != true){
//			logMessage = methodName + " -> Backup process failed.";
//			LogManager.LogErrorMsg(className, methodName, logMessage);
//			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + methodName + ": " + logMessage);
//		}
//    }
//
//    @Override
//	protected void onStop() {
//		super.onStop();
//		
//        if(broadcastReceiver != null){
//    		unregisterReceiver(broadcastReceiver);
//    	}
//        
//     	Thread registerToGCMInBackgroundThread = 
//         	mainActivityController.getRegisterToGCMInBackgroundThread();
//    	if(registerToGCMInBackgroundThread != null){
//    		registerToGCMInBackgroundThread.interrupt();
//    	}
//
//		BackupDataOperations backupData = new BackupDataOperations();
//		boolean isBackUpSuccess = backupData.backUp();
//		if(isBackUpSuccess != true){
//			logMessage = methodName + " -> Backup process failed.";
//			LogManager.LogErrorMsg(className, methodName, logMessage);
//			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
//		}
//	}
//
//	@Override
//    protected void onDestroy() {
//        super.onDestroy();
//        methodName = "onDestroy";
//        
//        isTrackLocationRunning = false;
//		
//		
//		LogManager.LogActivityDestroy(className, methodName);
//		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_DESTROY] {" + className + "} -> " + methodName);
//    }
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//	
//    public void onClick(final View view) {
//    	if (view == findViewById(R.id.btnLocate) || view == findViewById(R.id.btnLocationSharing) || view == findViewById(R.id.btnTracking) ){
//    		mainModel.setContactDeviceDataList(DBLayer.getInstance().getContactDeviceDataList(null));
//    	}
//    	
//    	// ========================================
//    	// ABOUT button
//    	// ========================================
//        if (view == findViewById(R.id.btnAbout)) {        	
////        	showAboutDialog();
//        	String title = "About";
//        	String dialogMessage = String.format(getResources().getString(R.string.about_dialog_text), 
//        		Preferences.getPreferencesString(context, CommonConst.PREFERENCES_VERSION_NAME));
//        	new InfoDialog(this, context, title, dialogMessage, null);
//        	
//    	// ========================================
//    	// JOIN button
//    	// ========================================
//        } else if (view == findViewById(R.id.btnJoin)) {
//        	String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
//        	if(account == null || account.isEmpty()){
//    	    	Toast.makeText(MainActivity.this, "Please register your application.\nPress Locate button at first.", 
//        	    		Toast.LENGTH_SHORT).show();
//        		LogManager.LogErrorMsg(className, "onClick -> JOIN button", 
//            		"Unable to join contacts - application is not registred yet.");
//        	} else {
//        		Intent joinContactsListIntent = new Intent(this, JoinContactsListActivity.class);
//        		//startActivity(joinContactsListIntent);
//        		startActivityForResult(joinContactsListIntent, JOIN_REQUEST);
//        	}
//
//// 			*********************************************************************************        	
////		    // Start an activity for the user to pick a phone number from contacts
////		    Intent intent = new Intent(Intent.ACTION_PICK);
////		    intent.setType(CommonDataKinds.Phone.CONTENT_TYPE);
////		    if (intent.resolveActivity(getPackageManager()) != null) {
////		        startActivityForResult(intent, CommonConst.REQUEST_SELECT_PHONE_NUMBER);
////		    }
//// 			*********************************************************************************        	
//			
//    	// ========================================
//    	// SETTINGS button
//    	// ========================================
//        } else if (view == findViewById(R.id.btnSettings)) {	
//    		Intent settingsIntent = new Intent(this, SettingsActivity.class);
//    		startActivityForResult(settingsIntent, 2); 
//
//    	// ========================================
//    	// LOCATE button (CONTACT_LIST)
//    	// ========================================
//        } else if (view == findViewById(R.id.btnLocate)) {
//    		LogManager.LogInfoMsg(className, "onClick -> Locate button", 
//    			"ContactList activity started.");
//    		
//    		if(mainModel.getContactDeviceDataList() != null){
//	    		Intent intentContactList = new Intent(this, ContactListActivity.class);
//	    		intentContactList.putParcelableArrayListExtra(CommonConst.CONTACT_DEVICE_DATA_LIST, mainModel.getContactDeviceDataList());
//	    		startActivity(intentContactList);
//    		} else {
//    	    	Toast.makeText(MainActivity.this, "There is no any contact.\nJoin some contact at first.", 
//    	    		Toast.LENGTH_SHORT).show();
//        		LogManager.LogInfoMsg(className, "onClick -> LOCATE button", 
//                	"There is no any contact. Some contact must be joined at first.");
//    		}
//    	// ========================================
//    	// LOCATION SHARING MANAGEMENT button
//    	// ========================================
//        } else if (view == findViewById(R.id.btnLocationSharing)) {
//    		LogManager.LogInfoMsg(className, "onClick -> Location Sharing Management button", 
//    			"ContactList activity started.");
//    		    		
//    		if(mainModel.getContactDeviceDataList() != null){
//	    		Intent intentContactList = new Intent(this, LocationSharingListActivity.class);
//	    		intentContactList.putParcelableArrayListExtra(CommonConst.CONTACT_DEVICE_DATA_LIST, mainModel.getContactDeviceDataList());
//	    		startActivity(intentContactList);
//    		} else {
//    	    	Toast.makeText(MainActivity.this, "There is no any contact.\nJoin some contact at first.", 
//    	    		Toast.LENGTH_SHORT).show();
//        		LogManager.LogInfoMsg(className, "onClick -> LOCATION SHARING MANAGEMENT button", 
//                    "There is no any contact. Some contact must be joined at first.");
//    		}
//    	// ========================================
//    	// TRACKING button (TRACKING_CONTACT_LIST)
//    	// ========================================
//        } else if (view == findViewById(R.id.btnTracking)) {
//    		LogManager.LogInfoMsg(className, "onClick -> Tracking button", 
//    			"TrackingList activity started.");
//    		    		
//    		if(mainModel.getContactDeviceDataList() != null){
//	    		Intent intentContactList = new Intent(this, TrackingListActivity.class);
//	    		intentContactList.putParcelableArrayListExtra(CommonConst.CONTACT_DEVICE_DATA_LIST, mainModel.getContactDeviceDataList());
//	    		startActivity(intentContactList);
//    		} else {
//    	    	Toast.makeText(MainActivity.this, "There is no any contact.\nJoin some contact at first.", 
//    	    		Toast.LENGTH_SHORT).show();
//        		LogManager.LogInfoMsg(className, "onClick -> TRACKING button", 
//                    "There is no any contact. Some contact must be joined at first.");
//    		}
//        }
//    }
//    
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		if(requestCode==2){
//			// Make sure the request was successful
//	        if (resultCode == RESULT_OK) {	 
//	        	if (data != null && data.getExtras().getBoolean(CommonConst.THEME_CHANGED)){
//	        		Intent i = new Intent(this, MainActivity.class);
//	                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
//	                startActivity(i);
//	        	}
//	       }
//		}
//	}
}
