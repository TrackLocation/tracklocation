package com.dagrest.tracklocation;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static int RQS_GooglePlayServices = 1;
    private BroadcastReceiver locationChangeWatcher;
    private String className;
    private String regid;
    private GoogleCloudMessaging gcm;
    private Context context;

//  TextView mDisplay;
//  TextView etRegId;
    
    @SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		className = this.getClass().getName();
		
		setContentView(R.layout.activity_main);
//		mDisplay = (TextView) findViewById(R.id.display);
		
		context = getApplicationContext();
//		preferences = Preferences.getGCMPreferences(context);
//		initLocationChangeWatcherGps();
//		initLocationChangeWatcherNetwork();
		
////        btnRegId = (Button) findViewById(R.id.btnGetRegId);
//        etRegId = (TextView) findViewById(R.id.etRegId);
        
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);

            if (regid.isEmpty()) {
                registerInBackground();
            }
            
        } else {
            Log.e(CommonConst.LOG_TAG, "No valid Google Play Services APK found.");
    		LogManager.LogInfoMsg(this.getClass().getName(), "onCreate", 
    			"No valid Google Play Services APK found.");
        }
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
//	@Override
//	protected void onResume() {
//		super.onResume();
//
//		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
//		  
//		if (checkPlayServices()){
//			Toast.makeText(getApplicationContext(), 
//				"isGooglePlayServicesAvailable SUCCESS", 
//			Toast.LENGTH_LONG).show();
//		}
//		else{
//			GooglePlayServicesUtil.getErrorDialog(resultCode, this, RQS_GooglePlayServices);
//		}
//	}
	
    public void onClick(final View view) {

    	// ========================================
    	// ABOUT button
    	// ========================================
        if (view == findViewById(R.id.send)) {
        	About dialogAbout = new About();
        	dialogAbout.show(this.getFragmentManager(), "About");
        	
    	// ========================================
    	// SEND button
    	// ========================================
        } else if (view == findViewById(R.id.send)) {
//            new AsyncTask<Void, Void, String>() {
//                @Override
//                protected String doInBackground(Void... params) {
//                    String msg = "";
//                    msg = "Sent message";
//            		sendRegistrationIdToBackend(regid);
//                    return msg;
//                }
//
//                @Override
//                protected void onPostExecute(String msg) {
//                    mDisplay.append(msg + "\n");
//                }
//            }.execute(null, null, null);
    	// ========================================
    	// CLEAR button
    	// ========================================
        } else if (view == findViewById(R.id.clear)) {
//            mDisplay.setText("");
//            
////            if( scheduledActionExecutor != null ){
////            	scheduledActionExecutor.shutdown();
////            	scheduledActionExecutor = null;
////            }
//            if(trackLocationService != null) {
//            	if(!context.stopService(trackLocationService)){
//            		LogManager.LogErrorMsg(className, "onClick", "Stop trackLocationService failed.");
//            		Log.i(CommonConst.LOG_TAG, "Stop trackLocationService failed.");
//            	}
//            }
    	// ========================================
    	// GET REG ID button
    	// ========================================
        } else if (view == findViewById(R.id.btnGetRegId)) {
        	String regId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
//        	etRegId.setText(regId);
    		LogManager.LogInfoMsg(this.getClass().getName(), "onClick", 
    			"RegID: " + regId + " :RegID");
//    		if( scheduledActionExecutor == null){
//    			scheduledActionExecutor = new ScheduledActionExecutor(3);
//    		}
//    		trackLocationService = new Intent(context, TrackLocationService.class);
//    		context.startService(trackLocationService); 
        } else if (view == findViewById(R.id.btnContactList)) {
    		LogManager.LogInfoMsg(this.getClass().getName(), "onClick", 
    			"ContactList activity started.");
    		Intent intentContactList = new Intent(this, ContactList.class);
    		startActivity(intentContactList);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if(locationChangeWatcher != null){
        	unregisterReceiver(locationChangeWatcher);
        }
    }

	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	            	PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.e(CommonConst.LOG_TAG, "This device is not supported by Google Play Services.");
	            LogManager.LogErrorMsg(this.getClass().getName(), "checkPlayServices", 
	            	"This device is not supported by Google Play Services.");
	            finish();
	        }
	        return false;
	    }
	    return true;
	}
	
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(getResources().getString(R.string.google_project_number));
                    msg = "Device registered, registration ID=" + regid;

                    // Persist the version ID 
                    Preferences.setPreferencesInt(context, CommonConst.PROPERTY_APP_VERSION, 
                    	CommonConst.PROPERTY_APP_VERSION_VALUE);
                    // Persist the registration ID - no need to register again.
                    Preferences.setPreferencesString(context, CommonConst.PREFERENCES_REG_ID, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
//                mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

   	// Checking for all possible internet providers
    public boolean isConnectingToInternet(){
         
        ConnectivityManager connectivity = 
                             (ConnectivityManager) getSystemService(
                              Context.CONNECTIVITY_SERVICE);
          if (connectivity != null)
          {
              NetworkInfo[] info = connectivity.getAllNetworkInfo();
              if (info != null)
                  for (int i = 0; i < info.length; i++)
                      if (info[i].getState() == NetworkInfo.State.CONNECTED)
                      {
                          return true;
                      }
  
          }
          return false;
    }

//    private void initLocationChangeWatcherGps()
//    {
//    	LogManager.LogFunctionCall(className, "initLocationChangeWatcher");
//	    IntentFilter intentFilter = new IntentFilter();
//	    intentFilter.addAction("com.dagrest.tracklocation.service.TrackLocationService.LOCATION_UPDATED_GPS");
//	    //com.dagrest.tracklocation.service.TrackLocationService.LOCATION_UPDATED_NETWORK
//	    locationChangeWatcher = new BroadcastReceiver() 
//	    {
//	    	@Override
//    		public void onReceive(Context context, Intent intent) {
//    			// TODO Auto-generated method stub
//	    		LogManager.LogInfoMsg(className, "initLocationChangeWatcherGps->onReceive", "WORK");
////	    		mDisplay.setText("LOCATION_UPDATED_GPS: " +
////	    			Preferences.getPreferencesString(context, CommonConst.LOCATION_INFO_NETWORK));
//    		}
//	    };
//	    registerReceiver(locationChangeWatcher, intentFilter);
//	    LogManager.LogFunctionExit(className, "initLocationChangeWatcher");
//    }
//    
//    private void initLocationChangeWatcherNetwork()
//    {
//    	LogManager.LogFunctionCall(className, "initLocationChangeWatcher");
//	    IntentFilter intentFilter = new IntentFilter();
//	    intentFilter.addAction("com.dagrest.tracklocation.service.TrackLocationService.LOCATION_UPDATED_NETWORK");
//	    locationChangeWatcher = new BroadcastReceiver() 
//	    {
//	    	@Override
//    		public void onReceive(Context context, Intent intent) {
//    			// TODO Auto-generated method stub
//	    		LogManager.LogInfoMsg(className, "initLocationChangeWatcherNetwork->onReceive", "WORK");
////	    		mDisplay.setText("LOCATION_UPDATED_NETWORK: " +
////	    			Preferences.getPreferencesString(context, CommonConst.LOCATION_INFO_NETWORK));
//    		}
//	    };
//	    registerReceiver(locationChangeWatcher, intentFilter);
//	    LogManager.LogFunctionExit(className, "initLocationChangeWatcher");
//    }


}

