/**
 * 
 */
package com.doat.tracklocation;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;
import com.doat.tracklocation.crypto.CryptoUtils;
import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.JoinRequestStatusEnum;
import com.doat.tracklocation.datatype.SentJoinRequestData;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.dialog.CommonDialogNew;
import com.doat.tracklocation.dialog.ICommonDialogNewOnClickListener;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;
import com.doat.tracklocation.utils.Utils;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.app.SearchManager;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CursorTreeAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class JoinContactListActivity extends ExpandableListActivity {
	private BroadcastReceiver broadcastReceiver;
	private String className = this.getClass().getName();
	private String methodName;	
	private boolean toSendAddJoinRequest = false;
	private int groupPositionCurrent = -1;
	
    private static final String[] CONTACTS_PROJECTION = new String[] {    	
        Contacts._ID, 
        Contacts.DISPLAY_NAME
    };
    
    private static final int GROUP_ID_COLUMN_INDEX = 0;

    private static final String[] PHONE_NUMBER_PROJECTION = new String[] {
            Phone._ID,
            Phone.NUMBER
    };
    
    private String contactsOrder = Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";    
    
    private Uri contactUri = Contacts.CONTENT_URI;
    private String selection = Contacts.HAS_PHONE_NUMBER + "=1"; 
    private static String filterSelection = "";
    private static Boolean inFilterMode = false;

    private static final int TOKEN_GROUP = 0;
    private static final int TOKEN_CHILD = 1;

    private static final class QueryHandler extends AsyncQueryHandler {
        private CursorTreeAdapter mAdapter;        

        public QueryHandler(Context context, CursorTreeAdapter adapter) {
            super(context.getContentResolver());
            this.mAdapter = adapter;            
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
            case TOKEN_GROUP:
            	inFilterMode = !filterSelection.isEmpty() && cursor.getCount() == 0; 
            	if (inFilterMode){
            		MatrixCursor matrixCursor = new MatrixCursor(new String[] { Contacts._ID, Contacts.DISPLAY_NAME });
            		matrixCursor.addRow(new Object[] { 100000000, filterSelection});            		            		
            		cursor = matrixCursor;
            	}
        		mAdapter.setGroupCursor(cursor);
        		if (inFilterMode){
        			MatrixCursor matrixCursor = new MatrixCursor(new String[] { Phone._ID, Phone.NUMBER });
            		matrixCursor.addRow(new Object[] { 100000000, filterSelection});            		            		
            		cursor = matrixCursor;
        			this.onQueryComplete(TOKEN_CHILD, 0, cursor);
        		}
                break;

            case TOKEN_CHILD:
                int groupPosition = (Integer) cookie;
                mAdapter.setChildrenCursor(groupPosition, cursor);
                break;
            }
        }
    }

    public class MyExpandableListAdapter extends SimpleCursorTreeAdapter {

        // Note that the constructor does not take a Cursor. This is done to avoid querying the 
        // database on the main thread.
        public MyExpandableListAdapter(Context context, int groupLayout,
                int childLayout, String[] groupFrom, int[] groupTo, String[] childrenFrom,
                int[] childrenTo) {
        	
            super(context, null, groupLayout, groupFrom, groupTo, childLayout, childrenFrom, childrenTo);
        }                         
        
        @Override
        protected void bindGroupView(View view, Context context, Cursor cursor,
        		boolean isExpanded) {
        	
        	super.bindGroupView(view, context, cursor, isExpanded);       
        	
        	long _id = cursor.getInt(0);
        	ImageView imageView =  (ImageView) view.findViewById(R.id.icon);
        	
        	Bitmap bmp = getContactPhoto(getContentResolver(), _id, false);
        	if (bmp == null){        	
        		bmp = Utils.getDefaultContactBitmap(getResources());
        	}
        	
        	bmp = Utils.getRoundedCornerImage(bmp, false);
        	
        	Drawable contactPhoto = new BitmapDrawable(getResources(), bmp);    		
    		imageView.setImageDrawable(contactPhoto);
    		if (inFilterMode){    			
    			getExpandableListView().expandGroup(cursor.getPosition());    			
    		}
        }
              
        private Bitmap getContactPhoto(ContentResolver contentResolver, Long contactId, Boolean isRounded) {
    	    Uri contactPhotoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
    	    InputStream photoDataStream = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver,contactPhotoUri); // <-- always null
    	    return BitmapFactory.decodeStream(photoDataStream);    	 
    	}       

        @Override
        protected Cursor getChildrenCursor(Cursor groupCursor) {
            mQueryHandler.startQuery(TOKEN_CHILD, groupCursor.getPosition(), ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
            		PHONE_NUMBER_PROJECTION, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { Long.toString(groupCursor.getLong(GROUP_ID_COLUMN_INDEX)) }, null);
                        
            return null;
        }       
        
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        	View row = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
        	setGroupPositionCurrent(groupPosition);  
        	TextView text = (TextView) row.findViewById(R.id.textView1);
        	int iPic = R.drawable.ic_contact_phone_black_24dp;        	
        	Bitmap bmp = BitmapFactory.decodeResource(getResources(), iPic);
        	bmp = Utils.changeBitmapColor(bmp);
        	Drawable drawable = Utils.covertBitmapToDrawable(getBaseContext(), bmp);
        	drawable.setBounds(0,0,bmp.getWidth(),bmp.getHeight());
			text.setCompoundDrawables(drawable,null,null, null);
        	
        	row.setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				final View view = v;
    				Cursor parent = getGroup(getGroupPositionCurrent());    		
    				final String contactName = parent.getString(1);
    				if (inFilterMode && !Patterns.PHONE.matcher(filterSelection).matches()){
    					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(JoinContactListActivity.this); 
        				alertDialogBuilder.setTitle("Join contact");
        	
        				alertDialogBuilder
        					.setMessage("The " + filterSelection + " is not valid a phone number")
        					.setCancelable(false)        					
        					.setNegativeButton("Ok",new DialogInterface.OnClickListener() {
        						public void onClick(DialogInterface dialog,int id) {
        							dialog.cancel();
        						}
        					});
                		
                		AlertDialog alertDialog = alertDialogBuilder.create();
        				alertDialog.show();
    				}
    				else{
    					sendBroadcastToJoin(view, contactName);
    				}    				
    			}
    		});
        	
        	return row;
        }     
    }
    
    private void sendBroadcastToJoin(final View view, final String contactName) {
		TextView tv = (TextView) view.findViewById(R.id.textView1);
		String valToJoin  =  tv.getText().toString();
		Controller.broadcastMessage(JoinContactListActivity.this, 
			BroadcastActionEnum.BROADCAST_JOIN.toString(), 
			"OnChildClick",
			null, 
			BroadcastKeyEnum.join_number.toString(), 
			contactName + CommonConst.DELIMITER_STRING + valToJoin);
	}

    private QueryHandler mQueryHandler;
    private CursorTreeAdapter mAdapter;   
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	ActionBar actionBar = getActionBar();
	    actionBar.setHomeButtonEnabled(false);	    
	    actionBar.setDisplayShowHomeEnabled(false);
	    actionBar.setDisplayShowTitleEnabled(false);
	    
        super.onCreate(savedInstanceState);
        
        mAdapter = new MyExpandableListAdapter(
                this,  
                R.layout.join_row_group,
                R.layout.listrow_details,
                new String[] { Contacts.DISPLAY_NAME }, // Name for group layouts
                new int[] { R.id.contact },
                new String[] { Phone.NUMBER }, // Number for child layouts
                new int[] { R.id.textView1 });

        setListAdapter(mAdapter);

        mQueryHandler = new QueryHandler(this, mAdapter);
        
        mQueryHandler.startQuery(TOKEN_GROUP, null, contactUri, CONTACTS_PROJECTION, selection, null, contactsOrder);
        LogManager.LogActivityCreate(className, methodName);		
		initBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.changeCursor(null);
        mAdapter = null;
        unregisterReceiver(broadcastReceiver);
    	methodName = "onDestroy";
		LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_DESTROY] {" + className + "} -> " + methodName);
    }
    
	private void initBroadcastReceiver()
    {
    	LogManager.LogFunctionCall(className, "initBroadcastReceiver");
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(BroadcastActionEnum.BROADCAST_JOIN.toString());
	    broadcastReceiver = new BroadcastReceiver() 
	    {
	    	String phoneNumber = null;
	    	String contactName = null;
	    	@Override
    		public void onReceive(final Context context, Intent intent) {
	    		
		    	// TODO Auto-generated method stub
	    		LogManager.LogInfoMsg(className, "initBroadcastReceiver->onReceive", "WORK");
	    		
	    		Bundle bundle = intent.getExtras();
	    		String broadcastKeyJoinNumber = BroadcastKeyEnum.join_number.toString();
	    		String broadcastKeyFetchContactsCompleted = BroadcastKeyEnum.fetch_contacts_completed.toString();
	    		String broadcastKeyResendJoinRequest = BroadcastKeyEnum.resend_join_request.toString();
	    		// ===========================================
	    		// broadcast key: join_number or any value 
	    		// ===========================================
	    		if(bundle != null && 
	    				(bundle.containsKey(broadcastKeyJoinNumber) || 
	    				 bundle.containsKey(broadcastKeyFetchContactsCompleted) || 
	    				 bundle.containsKey(broadcastKeyResendJoinRequest))){
	    			
		    		String namePhoneNumber = bundle.getString(broadcastKeyJoinNumber);
		    		
		    		if(namePhoneNumber != null && !namePhoneNumber.isEmpty() && bundle.containsKey(broadcastKeyJoinNumber)){
//		    			System.out.println("Join number: " + namePhoneNumber);
		    			
		    			String[] args = namePhoneNumber.split(CommonConst.DELIMITER_STRING);
		    			
		    			contactName = args[0];
		    			phoneNumber = args[1];
		    			
		    			CommonDialogNew joinRequestDialog;
		    			SentJoinRequestData joinRequestData = DBLayer.getInstance().getSentJoinRequestByPhone(phoneNumber);
		    			if( joinRequestData == null ) {
		    				if (!toSendAddJoinRequest){
		    					String message = "\nJoin request will be sent to %s ( phone number %s) .\n\nDo you want to send it?\n";
		    					joinRequestDialog = joinRequestDialog(contactName, phoneNumber, message, onClickListener);
		    				}
		    			} else { // join request with <phoneNumber> already exists, check the status
		    				if( joinRequestData.getStatus().equals(JoinRequestStatusEnum.SENT.toString()) ) {
		    					// Notify by dialog that join request already sent to <phoneNumber>
		    					// check if the following request should be sent again
		    					String message = "\nJoin request has been already sent to %s, with phone number %s.\n\nDo you want to send it again?\n";
		    					joinRequestDialog = joinRequestDialog(contactName, phoneNumber, message, onClickListener);
		    					toSendAddJoinRequest = joinRequestDialog.isSelectionStatus();
		    				} else if( toSendAddJoinRequest ) {
//		    					// TODO: notify by dialog that join request already sent to <phoneNumber> and accepted
//		    					// check if the following request should be sent again
//		    					// DIALOG FUNC
//		    					
//		    					// in case if request should be sent again
//			    				toSendAddJoinRequest = true;
		    				} else if( !toSendAddJoinRequest ) {
//		    					// TODO: notify by dialog that join request already sent to <phoneNumber> but declined
//		    					// check if the following request should be sent again
//		    					// DIALOG FUNC
//		    					
//		    					// in case if request should be sent again
//			    				toSendAddJoinRequest = true;
		    				}
		    			}
		    			if(toSendAddJoinRequest == true){
		    				sendJoinRequest(context, contactName, phoneNumber);
		    				toSendAddJoinRequest = false;
		    			}
		    			
		    			// TODO: fix the following code - use STATUS instead of Mutual_ID
//		    			if(joinRequestData.getMutualId().equals(CommonConst.JOIN_COMPLETED)){
//		    				// TODO: notify that this contact has been joined
//		    				String msg = contactName + " [" + phoneNumber + "] has been joined already ";
//		    				Toast.makeText(JoinContactList.this, msg, Toast.LENGTH_SHORT).show();
//		    				finish();
//		    			}

		    		} else if(bundle.containsKey(BroadcastKeyEnum.resend_join_request.toString())){
		    			sendJoinRequest(context, contactName, phoneNumber);
		    			toSendAddJoinRequest = false;
		    		}
	    		}
    		}
	    };
	    registerReceiver(broadcastReceiver, intentFilter);
	    LogManager.LogFunctionExit(className, "initBroadcastReceiver");
    }
	
    public void sendJoinRequest(Context context, String contactName, String phoneNumber){    	
		String joinValue = "";
		try {
			joinValue = buildJoinRequestData(context, contactName, phoneNumber);
		} catch (UnsupportedEncodingException e) {
			Log.e(CommonConst.LOG_TAG, e.getMessage());			
		}
		if(!joinValue.isEmpty()){
			SmsManager smsManager = SmsManager.getDefault();
			ArrayList<String> parts = smsManager.divideMessage(joinValue);
			smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);    
			// Notify by toast that join request sent by SMS
			String msg = "Join request sent to " + contactName + " [" + phoneNumber + "] by SMS";
			Toast.makeText(JoinContactListActivity.this, msg, Toast.LENGTH_LONG).show();						
		}
		finish();
    }
    
    private String buildJoinRequestData(Context context, String contactName, String phoneNumber) throws UnsupportedEncodingException{    	
    	String mutualId = Controller.generateUUID();
    	long res = DBLayer.getInstance().addSentJoinRequest(phoneNumber, mutualId, JoinRequestStatusEnum.SENT);
		if(res != 1){
			return "";
		}
		String registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
		if(registrationId != null && !registrationId.isEmpty()){
            String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
            String macAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
            String ownerPhoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
			return CryptoUtils.encodeBase64(CommonConst.JOIN_FLAG_SMS + 
				CommonConst.DELIMITER_COMMA + registrationId + CommonConst.DELIMITER_COMMA +
				mutualId + CommonConst.DELIMITER_COMMA + ownerPhoneNumber + CommonConst.DELIMITER_COMMA + account +
				CommonConst.DELIMITER_COMMA + macAddress);
		}
		return "";   	
    }

	ICommonDialogNewOnClickListener onClickListener = new ICommonDialogNewOnClickListener(){

		@Override
		public void doOnPositiveButton(Object data) {
			toSendAddJoinRequest = true;
        	Controller.broadcastMessage(JoinContactListActivity.this, 
    			BroadcastActionEnum.BROADCAST_JOIN.toString(), 
    			"fetchContacts",
    			null, 
				BroadcastKeyEnum.resend_join_request.toString(), 
				"Resend");	
		}

		@Override
		public void doOnNegativeButton(Object data) {
			toSendAddJoinRequest = false;
		}

		@Override
		public void doOnChooseItem(int which) {
			// TODO Auto-generated method stub
			
		}
		
	};

/*    
	IDialogOnClickAction joinRequest = new IDialogOnClickAction() {
		@Override
		public void doOnPositiveButton() {
			toSendAddJoinRequest = true;
        	Controller.broadcastMessage(JoinContactListActivity.this, 
        			BroadcastActionEnum.BROADCAST_JOIN.toString(), 
        			"fetchContacts",
        			null, 
					BroadcastKeyEnum.resend_join_request.toString(), 
					"Resend");	
		}
		@Override
		public void doOnNegativeButton() {
			toSendAddJoinRequest = false;
		}
		@Override
		public void setActivity(Activity activity) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void setContext(Context context) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void setParams(Object[]... objects) {
			// TODO Auto-generated method stub
		}
		@Override
		public void doOnChooseItem(int which) {
			// TODO Auto-generated method stub			
		}
	};
*/	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
	    // Inflate the menu; this adds items to the action bar if it is present.
	    getMenuInflater().inflate(R.menu.search_menu, menu);
	 
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
	    if (searchView != null )
	    {
	    	searchView.setSearchableInfo( searchManager.getSearchableInfo(getComponentName()) );
	    	searchView.setIconifiedByDefault(false);

	        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener()
	        {
	            public boolean onQueryTextChange(String newText)
	            {	            	
	            	filterData(newText);
	                return true;
	            }

				@Override
				public boolean onQueryTextSubmit(String query) {
					// TODO Auto-generated method stub
					return false;
				}	           
	        };

	        searchView.setOnQueryTextListener(queryTextListener);
	    }
	 
	    return true;
	}
	
	private CommonDialogNew joinRequestDialog(String contactName, String phoneNumber, String message, ICommonDialogNewOnClickListener onClickListener) {
    	String dialogMessage = String.format(message, contactName, phoneNumber) ;
    	
		CommonDialogNew aboutDialog = new CommonDialogNew(this, onClickListener);
		aboutDialog.setDialogMessage(dialogMessage);
		aboutDialog.setDialogTitle("Join contact");
		aboutDialog.setPositiveButtonText("Yes");
		aboutDialog.setNegativeButtonText("No");
		aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
		aboutDialog.showDialog();
		aboutDialog.setCancelable(true);
		return aboutDialog;
    }
	
	public int getGroupPositionCurrent() {
		return groupPositionCurrent;
	}

	public void setGroupPositionCurrent(int groupPositionCurrent) {
		this.groupPositionCurrent = groupPositionCurrent;
	}
	
	public void filterData(final String contcatName) {		
		String contcatNameLowerCase = contcatName.toLowerCase(Locale.getDefault());
		filterSelection = contcatName;
				
		String selFilter = selection;
		if (!contcatNameLowerCase.isEmpty()){
			selFilter = Contacts.DISPLAY_NAME + " LIKE '%" + contcatNameLowerCase + "%'";
		}
			
		mQueryHandler.startQuery(TOKEN_GROUP, null, contactUri, CONTACTS_PROJECTION, selFilter, null, contactsOrder);
			
	}
}