/**
 * 
 */
package com.doat.tracklocation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.ContactData;
import com.doat.tracklocation.datatype.JoinRequestStatusEnum;
import com.doat.tracklocation.datatype.SentJoinRequestData;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.dialog.CommonDialog;
import com.doat.tracklocation.dialog.IDialogOnClickAction;
import com.doat.tracklocation.dialog.InfoDialog;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;
import com.doat.tracklocation.utils.Utils;

import android.app.ExpandableListActivity;
import android.app.SearchManager;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class JoinContactsExpandableList extends ExpandableListActivity {
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
                mAdapter.setGroupCursor(cursor);
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
        	// TODO Auto-generated method stub
        	super.bindGroupView(view, context, cursor, isExpanded);
        	
        	long _id = cursor.getInt(0);
        	ImageView imageView =  (ImageView) view.findViewById(R.id.icon);
        	
        	Bitmap bmp = getContactPhoto(getContentResolver(), _id, false);
        	if (bmp == null){        	
        		bmp = Utils.getDefaultContactBitmap(getResources());
        	}
        	
        	bmp = Utils.getRoundedCornerImage(bmp, false);
        	
        	Drawable contactPhoto = new BitmapDrawable(getResources(), bmp);
    		contactPhoto.setBounds( 0, 0, 120, 120 );
    		imageView.setImageDrawable(contactPhoto);
        }
        
        private Bitmap getContactPhoto(ContentResolver contentResolver, Long contactId, Boolean isRounded) {
    	    Uri contactPhotoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
    	    InputStream photoDataStream = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver,contactPhotoUri); // <-- always null
    	    return BitmapFactory.decodeStream(photoDataStream);    	 
    	}

        @Override
        protected Cursor getChildrenCursor(Cursor groupCursor) {
            // Given the group, we return a cursor for all the children within that group 

            // Return a cursor that points to this contact's phone numbers
            Uri.Builder builder = Contacts.CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, groupCursor.getLong(GROUP_ID_COLUMN_INDEX));
            builder.appendEncodedPath(Contacts.Data.CONTENT_DIRECTORY);
            Uri phoneNumbersUri = builder.build();

            mQueryHandler.startQuery(TOKEN_CHILD, groupCursor.getPosition(), phoneNumbersUri, 
                    PHONE_NUMBER_PROJECTION, Phone.MIMETYPE + "=?", 
                    new String[] { Phone.CONTENT_ITEM_TYPE }, null);

            return null;
        }
        
        @Override
        protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {        
        	super.bindChildView(view, context, cursor, isLastChild);       
        	TextView text = (TextView) view.findViewById(android.R.id.text1);
			text.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_contact_phone_black_24dp, 0, 0, 0);
        }        
        
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        	View row = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
        	setGroupPositionCurrent(groupPosition);   
        	row.setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				Cursor parent = getGroup(getGroupPositionCurrent());    		
    				String contactName = parent.getString(1);    		
    				TextView tv = (TextView) v.findViewById(android.R.id.text1);
    				String groupPhone  =  tv.getText().toString();
            		Controller.broadcastMessage(JoinContactsExpandableList.this, 
            			BroadcastActionEnum.BROADCAST_JOIN.toString(), 
            			"OnChildClick",
            			null, 
    					BroadcastKeyEnum.join_number.toString(), 
    					contactName + CommonConst.DELIMITER_STRING + groupPhone);
    			}
    		});
        	
        	return row;
        }     
    }

    private QueryHandler mQueryHandler;
    private CursorTreeAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up our adapter
        mAdapter = new MyExpandableListAdapter(
                this,  
                R.layout.join_row_group,
                android.R.layout.simple_expandable_list_item_1,
                new String[] { Contacts.DISPLAY_NAME }, // Name for group layouts
                new int[] { R.id.contact },
                new String[] { Phone.NUMBER }, // Number for child layouts
                new int[] { android.R.id.text1 });

        setListAdapter(mAdapter);

        mQueryHandler = new QueryHandler(this, mAdapter);

        // Query for people
        mQueryHandler.startQuery(TOKEN_GROUP, null, Contacts.CONTENT_URI, CONTACTS_PROJECTION, 
                Contacts.HAS_PHONE_NUMBER + "=1", null, Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);	

		initBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Null out the group cursor. This will cause the group cursor and all of the child cursors
        // to be closed.
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
		    			
		    			SentJoinRequestData joinRequestData = DBLayer.getInstance().getSentJoinRequestByPhone(phoneNumber);
		    			InfoDialog joinRequestDialog = null;
		    			if( joinRequestData == null ) { 
		    				toSendAddJoinRequest = true;
		    			} else { // join request with <phoneNumber> already exists, check the status
		    				if( joinRequestData.getStatus().equals(JoinRequestStatusEnum.SENT.toString()) ) {
		    					// Notify by dialog that join request already sent to <phoneNumber>
		    					// check if the following request should be sent again
//		    					joinRequestDialog(contactName, phoneNumber);
		    	        		String title = "Join contact";
		    	        		String dialogMessage = "\nJoin request has been already sent to " + contactName + 
		    			        	", with phone number " + phoneNumber + ".\n\nDo you want to send it again?\n";
		    	        		joinRequestDialog = new InfoDialog(JoinContactsExpandableList.this, context, title, dialogMessage, null);
		    	        		
		    	        		toSendAddJoinRequest = joinRequestDialog.isSelectionStatus();
		    				} else if(toSendAddJoinRequest) {
		    					// TODO: notify by dialog that join request already sent to <phoneNumber> and accepted
		    					// check if the following request should be sent again
		    					// DIALOG FUNC
		    					
		    					// in case if request should be sent again
			    				toSendAddJoinRequest = true;
		    				} else if(!toSendAddJoinRequest) {
		    					// TODO: notify by dialog that join request already sent to <phoneNumber> but declined
		    					// check if the following request should be sent again
		    					// DIALOG FUNC
		    					
		    					// in case if request should be sent again
			    				toSendAddJoinRequest = false;
		    				}
		    			}
		    			if(toSendAddJoinRequest == true){
		    				sendJoinRequest(context, contactName, phoneNumber);
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
		    		}
	    		}
    		}
	    };
	    registerReceiver(broadcastReceiver, intentFilter);
	    LogManager.LogFunctionExit(className, "initBroadcastReceiver");
    }
	
    public void sendJoinRequest(Context context, String contactName, String phoneNumber){
    	String mutualId = Controller.generateUUID();
    	long res = DBLayer.getInstance().addSentJoinRequest(phoneNumber, mutualId, JoinRequestStatusEnum.SENT);
		if(res != 1){
			// TODO: Notify that add to DB failed...
		}
		String registrationId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
		if(registrationId != null && !registrationId.isEmpty()){
        	// Send SMS with registration details: 
        	// phoneNumber and registartionId (mutual ID - optional) 
        	SmsManager smsManager = SmsManager.getDefault();
            String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
            String macAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
            String ownerPhoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
			ArrayList<String> parts = smsManager.divideMessage(CommonConst.JOIN_FLAG_SMS + 
				CommonConst.DELIMITER_COMMA + registrationId + CommonConst.DELIMITER_COMMA +
				mutualId + CommonConst.DELIMITER_COMMA + ownerPhoneNumber + CommonConst.DELIMITER_COMMA + account +
				CommonConst.DELIMITER_COMMA + macAddress);
			smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);    
			// Notify by toast that join request sent by SMS
			String msg = "Join request sent to " + contactName + " [" + phoneNumber + "] by SMS";
			Toast.makeText(JoinContactsExpandableList.this, msg, Toast.LENGTH_LONG).show();
		}
		finish();
    }

/*    
	IDialogOnClickAction joinRequest = new IDialogOnClickAction() {
		@Override
		public void doOnPositiveButton() {
			toSendAddJoinRequest = true;
        	Controller.broadcastMessage(JoinContactsExpandableList.this, 
        			BroadcastActionEnum.BROADCAST_JOIN.toString(), 
        			"fetchContacts",
        			null, 
					BroadcastKeyEnum.resend_join_request.toString(), 
					"Resend");
			//sendJoinRequest(context, contactName, phoneNumber, mutualId);
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

/*	
	private void joinRequestDialog(String contactName, String phoneNumber) {
    	String dialogMessage = "\nJoin request has been already sent to " + contactName + 
    		", with phone number " + phoneNumber + ".\n\nDo you want to send it again?\n";
    	
		CommonDialog aboutDialog = new CommonDialog(this, joinRequest);
		aboutDialog.setDialogMessage(dialogMessage);
		aboutDialog.setDialogTitle("Join contact");
		aboutDialog.setPositiveButtonText("Yes");
		aboutDialog.setNegativeButtonText("No");
		aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
		aboutDialog.showDialog();
		aboutDialog.setCancelable(true);
    }
*/	
	public int getGroupPositionCurrent() {
		return groupPositionCurrent;
	}

	public void setGroupPositionCurrent(int groupPositionCurrent) {
		this.groupPositionCurrent = groupPositionCurrent;
	}
	
	public void filterData(final String contcatName) {
		String contcatNameLowerCase = contcatName.toLowerCase(Locale.getDefault());
		
		String selection = Contacts.HAS_PHONE_NUMBER + " = 1 ";
		
		if (!contcatNameLowerCase.isEmpty()){
			selection += "AND " + Contacts.DISPLAY_NAME + " LIKE '%" + contcatNameLowerCase + "%'";
		}
			
		mQueryHandler.startQuery(TOKEN_GROUP, null, Contacts.CONTENT_URI, CONTACTS_PROJECTION, 
				selection,
                null, Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
		
	}
}
