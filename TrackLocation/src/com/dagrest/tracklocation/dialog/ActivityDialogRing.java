package com.dagrest.tracklocation.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.R;
import com.dagrest.tracklocation.datatype.BroadcastActionEnum;
import com.dagrest.tracklocation.datatype.BroadcastKeyEnum;
import com.dagrest.tracklocation.utils.CommonConst;
import com.google.gson.Gson;

public class ActivityDialogRing extends Activity {

	private static final int TEXT_ID = 1;
	private static final int BTN_FIRST_ID = 2;
	private static final int BTN_SECOND_ID = 3;
	
	private Button btnFirst = null;
	private Button btnSecond = null;
	private Context context;
	
	private String senderAccount;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		context = this.getApplicationContext();
		
		Intent intent = getIntent();
		senderAccount = intent.getExtras().getString(CommonConst.PREFERENCES_PHONE_ACCOUNT);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setFinishOnTouchOutside(false); 

		setContentView(R.layout.activity_dialog);
		
		// Text Layout
		RelativeLayout textLayout = (RelativeLayout) findViewById(R.id.text_layout);
    
	    // Text View
	    TextView txtView = new TextView(this);
	    String dialogMsg = String.format(getResources().getString(R.string.ring_dialog_message), 
	    		senderAccount).toString();
	    txtView.setText(dialogMsg);
	    txtView.setTextSize(18);
	    txtView.setId(TEXT_ID);
	    txtView.setTextColor(Color.BLACK);
	    // add text view (txtView) to the layout (textLayout)
	    textLayout.addView(txtView);
	    
		// Buttons Layout:
	    RelativeLayout buttonsLayout = (RelativeLayout) findViewById(R.id.buttons_layout);
	    RelativeLayout.LayoutParams buttonsLayoutParams = new RelativeLayout.LayoutParams(
	    		LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	    buttonsLayoutParams.setMargins(0, 30, 0, 0);
	    buttonsLayoutParams.addRule(RelativeLayout.BELOW, R.id.text_layout);
	    buttonsLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    buttonsLayout.setLayoutParams(buttonsLayoutParams);

	    LinearLayout buttonsLinearLayout = new LinearLayout(context);
	    LinearLayout.LayoutParams buttonsLinearLayoutParams = new LinearLayout.LayoutParams(
	    		LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	    buttonsLinearLayout.setLayoutParams(buttonsLinearLayoutParams);
	    buttonsLayout.addView(buttonsLinearLayout);
	    
	    // Set the properties for button: First
	    btnFirst = new Button(this);
	    btnFirst.setText(getResources().getString(R.string.ring_dialog_btn_first_name)); 
	    btnFirst.setId(BTN_FIRST_ID);
	    LinearLayout.LayoutParams paramsBtnFirst = new LinearLayout.LayoutParams(
	    		LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
	    btnFirst.setLayoutParams(paramsBtnFirst);
	    //add button to the layout
	    buttonsLinearLayout.addView(btnFirst, paramsBtnFirst);

	    // Set the properties for button: Second
	    btnSecond = new Button(this);
	    btnSecond.setText(getResources().getString(R.string.ring_dialog_btn_second_name));
	    btnSecond.setId(BTN_SECOND_ID);
	    btnSecond.setEnabled(false);
	    LinearLayout.LayoutParams paramsBtnSecond = new LinearLayout.LayoutParams(
	    		LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
	    paramsBtnSecond.setMargins(20, 0, 0, 0);
	    btnSecond.setLayoutParams(paramsBtnSecond);
	    //add button to the layout
	    buttonsLinearLayout.addView(btnSecond, paramsBtnSecond);

	    final Button buttonFirst = (Button) findViewById(BTN_FIRST_ID);
	    buttonFirst.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// send broadcast to turn off the signal
				Controller.broadcsatMessage(context, BroadcastActionEnum.BROADCAST_TURN_OFF_RING.toString(), 
						"Turn Off the Ring signal" + " by " + senderAccount, 
						BroadcastKeyEnum.turn_off.toString(), "value");
				btnSecond.setEnabled(true);
				btnFirst.setEnabled(false);
			}
		});

		final Button buttonSecond = (Button) findViewById(BTN_SECOND_ID);
		buttonSecond.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		// Only BTN_SECOND_ID can close the dialog/activity
	}

}
