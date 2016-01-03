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
import android.view.KeyEvent;

public class StartAppActivity extends BaseActivity {
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
        		StartAppActivity.this.finish();
        		stoptimertask();
        		Intent mapIntent = new Intent(StartAppActivity.this, MapActivity.class);
        		startActivity(mapIntent);
        		super.onAdClosed();
        	}
		};
		
        mInterstitialAd.setAdListener(adListener);        
		
		className = this.getClass().getName();
		methodName = "onCreate";
		
		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);
		
		setContentView(R.layout.start_activity);
				
    }
	
	private void startTimer() {
    	timer = new Timer();
        //initialize the TimerTask's job
        TimerTask timerTask = new TimerTask() {
            public void run() {            	
                handler.post(new Runnable() {
                    public void run() { 
                    	;
                    }
                });
            }
        };
        
         timer.schedule(timerTask, 3000); //
    }
	
    private void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
