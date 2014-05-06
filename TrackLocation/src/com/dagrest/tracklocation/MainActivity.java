package com.dagrest.tracklocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.dagrest.tracklocation.http.HttpUtils;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.service.ScheduledActionExecutor;
import com.dagrest.tracklocation.service.TrackLocationService;
import com.dagrest.tracklocation.utils.CommonConstants;
import com.dagrest.tracklocation.utils.Preferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	/**
     * Tag used on log messages.
     */
	private final static String LOG_TAG = "TrackLocation";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static int RQS_GooglePlayServices = 1;
    private ScheduledActionExecutor scheduledActionExecutor = null;
    private SharedPreferences preferences;
    private BroadcastReceiver locationChangeWatcher;
    private String className;
    
    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "943276333483";
    
    Intent trackLocationService;
    
    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    Context context;
    
    String regid;
//    Button btnRegId;
    TextView etRegId;
    
    private void initLocationChangeWatcherGps()
    {
    	LogManager.LogFunctionCall(className, "initLocationChangeWatcher");
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction("com.dagrest.tracklocation.service.TrackLocationService.LOCATION_UPDATED_GPS");
	    //com.dagrest.tracklocation.service.TrackLocationService.LOCATION_UPDATED_NETWORK
	    locationChangeWatcher = new BroadcastReceiver() 
	    {
	    	@Override
    		public void onReceive(Context context, Intent intent) {
    			// TODO Auto-generated method stub
	    		LogManager.LogInfoMsg(className, "initLocationChangeWatcherGps->onReceive", "WORK");
	    		mDisplay.setText("LOCATION_UPDATED_GPS: " +
	    			Preferences.getPreferencesString(context, CommonConstants.LOCATION_INFO_NETWORK));
    		}
	    };
	    registerReceiver(locationChangeWatcher, intentFilter);
	    LogManager.LogFunctionExit(className, "initLocationChangeWatcher");
    }
    
    private void initLocationChangeWatcherNetwork()
    {
    	LogManager.LogFunctionCall(className, "initLocationChangeWatcher");
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction("com.dagrest.tracklocation.service.TrackLocationService.LOCATION_UPDATED_NETWORK");
	    locationChangeWatcher = new BroadcastReceiver() 
	    {
	    	@Override
    		public void onReceive(Context context, Intent intent) {
    			// TODO Auto-generated method stub
	    		LogManager.LogInfoMsg(className, "initLocationChangeWatcherNetwork->onReceive", "WORK");
	    		mDisplay.setText("LOCATION_UPDATED_NETWORK: " +
	    			Preferences.getPreferencesString(context, CommonConstants.LOCATION_INFO_NETWORK));
    		}
	    };
	    registerReceiver(locationChangeWatcher, intentFilter);
	    LogManager.LogFunctionExit(className, "initLocationChangeWatcher");
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		className = this.getClass().getName();
		
		setContentView(R.layout.activity_main);
		mDisplay = (TextView) findViewById(R.id.display);
		
		context = getApplicationContext();
		preferences = Preferences.getGCMPreferences(context);
		initLocationChangeWatcherGps();
		initLocationChangeWatcherNetwork();
		
//        btnRegId = (Button) findViewById(R.id.btnGetRegId);
        etRegId = (TextView) findViewById(R.id.etRegId);
        
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = Preferences.getPreferencesString(context, CommonConstants.PREFERENCES_REG_ID);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.e(LOG_TAG, "No valid Google Play Services APK found.");
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
	
	@Override
	protected void onResume() {
		super.onResume();

		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		  
		if (checkPlayServices()){
			Toast.makeText(getApplicationContext(), 
				"isGooglePlayServicesAvailable SUCCESS", 
			Toast.LENGTH_LONG).show();
		}
		else{
			GooglePlayServicesUtil.getErrorDialog(resultCode, this, RQS_GooglePlayServices);
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
	            Log.e(LOG_TAG, "This device is not supported by Google Play Services.");
	            LogManager.LogErrorMsg(this.getClass().getName(), "checkPlayServices", 
	            	"This device is not supported by Google Play Services.");
	            finish();
	        }
	        return false;
	    }
	    return true;
	}
	
//    /**
//     * Stores the registration ID and the app versionCode in the application's
//     * {@code SharedPreferences}.
//     *
//     * @param context application's context.
//     * @param regId registration ID
//     */
//    private void storeRegistrationId(Context context, String regId) {
//        final SharedPreferences prefs = Preferences.getGCMPreferences(context);
//        int appVersion = getAppVersion(context);
//        Log.i(LOG_TAG, "Saving regId on app version " + appVersion);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString(PROPERTY_REG_ID, regId);
//        editor.putInt(PROPERTY_APP_VERSION, appVersion);
//        editor.commit();
//    }

//    /**
//	 * Gets the current registration ID for application on GCM service.
//	 * <p>
//	 * If result is empty, the app needs to register.
//	 *
//	 * @return registration ID, or empty string if there is no existing
//	 *         registration ID.
//	 */
//	private String getRegistrationId(Context context) {
//	    final SharedPreferences prefs = Preferences.getGCMPreferences(context);
//	    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
//	    if (registrationId.isEmpty()) {
//	        Log.i(LOG_TAG, "Registration not found.");
//	        return "";
//	    }
//	    // Check if app was updated; if so, it must clear the registration ID
//	    // since the existing regID is not guaranteed to work with the new
//	    // app version.
//	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
//	    int currentVersion = getAppVersion(context);
//	    if (registeredVersion != currentVersion) {
//	        Log.i(LOG_TAG, "App version changed.");
//	        return "";
//	    }
//	    return registrationId;
//	}

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
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    // sendRegistrationIdToBackend(regid);

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    //storeRegistrationId(context, regid);
                    Preferences.setPreferencesString(context, CommonConstants.PREFERENCES_REG_ID, regid);
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
                mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

    // Send an upstream message.
    public void onClick(final View view) {

        if (view == findViewById(R.id.send)) {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String msg = "";
//                    try {
//                        Bundle data = new Bundle();
//                        data.putString("my_message", "Hello World");
//                        data.putString("my_action", "com.dagrest.tracklocation.ECHO_NOW");
//                        String id = Integer.toString(msgId.incrementAndGet());
//                        //gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
//                        String toId = getRegistrationId(getApplicationContext());
//                        String daToId = "APA91bGkXaoVQUsJje8G0-l-uAPgWsuEfC4lrIsxPyacC52aOm4bw79pLb1MkHhl3lQ2IMCKYmc0bR5T1zna8G9H2g8KdGFvXnw4JhE3bTLjjdydPktriUQo6BLfUNKQhzqzqm4Cj2z2mtqrxNk2mt8zvNugFmJUtwkQEPQVpmhVSvyzmzk6Jo0";
//                        String laToId = "APA91bET5fuNBxEWpCN4OcKIYnPswm2P-dcKdEb4-2Kxa1rzuv8dhx36bnwECgy3Fje2tzfU2nvtFLkpj2tNryRGIwdidaQAKvKdvNupEvWBBejVE1eQYxEzpV51KXx-7Z_3CDu9SFvykNBmhZbhZ-30nd1wW-vnMjtt42uA1t0qECU6FnKBRB4";
//                        gcm.send(toId + "@gcm.googleapis.com", id, 0, data);
//                        id = Integer.toString(msgId.incrementAndGet());
//                        gcm.send(daToId + "@gcm.googleapis.com", id, 0, data);
//                        id = Integer.toString(msgId.incrementAndGet());
//                        gcm.send(laToId + "@gcm.googleapis.com", id, 0, data);
//                        
//                        //gcm.send(SENDER_ID + "@google.com", id, data);
                        msg = "Sent message";
                		sendRegistrationIdToBackend(regid);
//                    } catch (IOException ex) {
//                        msg = "Error :" + ex.getMessage();
//                        LogManager.LogErrorMsg(this.getClass().getName(), "onClick->Send", 
//                        	"Error :" + ex.getMessage());
//                    }
                    return msg;
                }

                @Override
                protected void onPostExecute(String msg) {
                    mDisplay.append(msg + "\n");
                }
            }.execute(null, null, null);
        } else if (view == findViewById(R.id.clear)) {
            mDisplay.setText("");
            
//            if( scheduledActionExecutor != null ){
//            	scheduledActionExecutor.shutdown();
//            	scheduledActionExecutor = null;
//            }
//            if(trackLocationService != null) {
//            	if(!context.stopService(trackLocationService)){
//            		LogManager.LogErrorMsg(className, "onClick", "Stop trackLocationService failed.");
//            	}
//            }
        } else if (view == findViewById(R.id.btnGetRegId)) {
        	String regId = Preferences.getPreferencesString(context, CommonConstants.PREFERENCES_REG_ID);
        	etRegId.setText(regId);
    		LogManager.LogInfoMsg(this.getClass().getName(), "onClick", 
    			"RegID: " + regId + " :RegID");
//    		if( scheduledActionExecutor == null){
//    			scheduledActionExecutor = new ScheduledActionExecutor(3);
//    		}
//    		trackLocationService = new Intent(context, TrackLocationService.class);
//    		context.startService(trackLocationService); 
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void sendRegistrationIdToBackend(String regid) {
        LogManager.LogInfoMsg(this.getClass().getName(), "sendRegistrationIdToBackend", 
            	"Before PostToGCM.post(apiKey, content)");
        //PostToGCM.post(apiKey, content);
        
        new Date().toString();
        
        String messageJSON = "{\"registration_ids\" : "
        	+ "[\"" + regid + "\"],"+
        	"\"data\" : {\"message\": \"From David\",\"time\": \"" + new Date().toString() + "\"},}";
        
        postGCM("https://android.googleapis.com/gcm/send", "AIzaSyC2YburJfQ9h12eLEn7Ar1XPK_2deytF30", messageJSON);
        
        LogManager.LogFunctionExit(this.getClass().getName(), "sendRegistrationIdToBackend");
    }

    
    private static String postGCM(String url, String serverKey, String messageJson){
    	
        int responseCode;
        String message;
        HttpPost req = new HttpPost(url);
        
        try {
			StringEntity se = new StringEntity(messageJson);
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));
			req.setEntity(se);
			List<BasicHeader> headers = new ArrayList<BasicHeader>();
			headers.add(new BasicHeader("Accept", "application/json"));
			headers.add(new BasicHeader("Authorization", "key=" + serverKey));
			HttpResponse resp = HttpUtils
					.post(url, headers, se, null/*localContext*/);
			message = resp.getStatusLine().getReasonPhrase();
			responseCode = resp.getStatusLine().getStatusCode();
			if (responseCode != 200) {
				//throw new Exception("Cloud Exception" + message);
				return null;
			} else {
				String result = EntityUtils.toString(resp.getEntity(),
						HTTP.UTF_8);
				return result;
			}
		} catch (IOException e) {
			// TODO: handle exception
		}
        return null;
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
    
}

