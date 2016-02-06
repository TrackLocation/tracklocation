package com.doat.tracklocation;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Photo;
import android.provider.ContactsContract.Data;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.telephony.PhoneNumberUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.doat.tracklocation.utils.ImageLoader;
import com.doat.tracklocation.utils.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;

public class JoinContactDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_CONTACT_URI = 
            "com.doat.tracklocation.EXTRA_CONTACT_URI";

    private static final String TAG = "ContactDetailFragment";

    private Uri mContactUri; // Stores the contact Uri for this fragment instance
    private ImageLoader mImageLoader; // Handles loading the contact image in a background thread

    private ImageView mImageView;
    private LinearLayout mDetailsLayout;
    private TextView mEmptyView;
    private TextView mContactName;

    public static JoinContactDetailFragment newInstance(Uri contactUri) {
        // Create new instance of this fragment
        final JoinContactDetailFragment fragment = new JoinContactDetailFragment();

        // Create and populate the args bundle
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_CONTACT_URI, contactUri);

        // Assign the args bundle to the new fragment
        fragment.setArguments(args);

        // Return fragment
        return fragment;
    }

    public JoinContactDetailFragment() {}

    public void setContact(Uri contactLookupUri) {
        mContactUri = contactLookupUri;

        // If the Uri contains data, load the contact's image and load contact details.
        if (contactLookupUri != null) {
            // Asynchronously loads the contact image
            mImageLoader.loadImage(mContactUri, mImageView);

            // Shows the contact photo ImageView and hides the empty view
            mImageView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);

            getLoaderManager().restartLoader(ContactDetailQuery.QUERY_ID, null, this);
            getLoaderManager().restartLoader(ContactPhoneQuery.QUERY_ID, null, this);
        } else {
            mImageView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            mDetailsLayout.removeAllViews();
            if (mContactName != null) {
                mContactName.setText("");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Let this fragment contribute menu items
        setHasOptionsMenu(true);

        mImageLoader = new ImageLoader(getActivity(), getLargestScreenDimension(), false) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return loadContactPhoto((Uri) data, getImageSize());

            }
        };

        mImageLoader.setImageFadeIn(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        final View detailView = inflater.inflate(R.layout.join_contact_detail_fragment, container, false);

        // Gets handles to view objects in the layout
        mImageView = (ImageView) detailView.findViewById(R.id.contact_image);
        mDetailsLayout = (LinearLayout) detailView.findViewById(R.id.contact_details_layout);
        mEmptyView = (TextView) detailView.findViewById(android.R.id.empty);

        return detailView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // If not being created from a previous state
        if (savedInstanceState == null) {
            // Sets the argument extra as the currently displayed contact
            setContact(getArguments() != null ? (Uri) getArguments().getParcelable(EXTRA_CONTACT_URI) : null);
        } else {
            // If being recreated from a saved state, sets the contact from the incoming
            // savedInstanceState Bundle
            setContact((Uri) savedInstanceState.getParcelable(EXTRA_CONTACT_URI));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Saves the contact Uri
        outState.putParcelable(EXTRA_CONTACT_URI, mContactUri);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // When "edit" menu option selected
            case R.id.menu_edit_contact:
                // Standard system edit contact intent
                Intent intent = new Intent(Intent.ACTION_EDIT, mContactUri);
                intent.putExtra("finishActivityOnSaveCompleted", true);

                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            // Two main queries to load the required information
            case ContactDetailQuery.QUERY_ID:
                return new CursorLoader(getActivity(), mContactUri,
                        ContactDetailQuery.PROJECTION,
                        null, null, null);
            case ContactPhoneQuery.QUERY_ID:
                final Uri uri = Uri.withAppendedPath(mContactUri, Contacts.Data.CONTENT_DIRECTORY);
                return new CursorLoader(getActivity(), uri,
                        ContactPhoneQuery.PROJECTION,
                        ContactPhoneQuery.SELECTION,
                        null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mContactUri == null) {
            return;
        }

        switch (loader.getId()) {
            case ContactDetailQuery.QUERY_ID:
                // Moves to the first row in the Cursor
                if (data.moveToFirst()) {
                    final String contactName = data.getString(ContactDetailQuery.DISPLAY_NAME);
                    getActivity().setTitle(contactName);
                }
                break;
            case ContactPhoneQuery.QUERY_ID:
                final LinearLayout.LayoutParams layoutParams =
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);

                mDetailsLayout.removeAllViews();

                // Loops through all the rows in the Cursor
                String prevPhoneNormalize = "";
                if (data.moveToFirst()) {
                    do {
                    	if (!PhoneNumberUtils.compare(prevPhoneNormalize, data.getString(ContactPhoneQuery.NUMBER))){
                    		prevPhoneNormalize = data.getString(ContactPhoneQuery.NUMBER);
	                        final LinearLayout layout = buildPhonesLayout(
	                                data.getInt(ContactPhoneQuery.TYPE),
	                                data.getString(ContactPhoneQuery.LABEL),
	                                prevPhoneNormalize);	                        
	                        mDetailsLayout.addView(layout, layoutParams);	                        	                       
                    	}
                    } while (data.moveToNext());
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private LinearLayout buildPhonesLayout(int phoneType, String phoneTypeLabel,
            final String phoneNum) {

        // Inflates the address layout
        final LinearLayout phoneLayout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.join_contact_detail_item, mDetailsLayout, false);

        // Gets handles to the view objects in the layout
        final TextView headerTextView = (TextView) phoneLayout.findViewById(R.id.contact_detail_header);
        final TextView phoneTextView = (TextView) phoneLayout.findViewById(R.id.contact_detail_item);
        final ImageButton btnJoin = (ImageButton) phoneLayout.findViewById(R.id.button_join_phone);
          if (phoneTypeLabel == null && phoneType == 0) {
            headerTextView.setVisibility(View.GONE);
            phoneTextView.setText(R.string.no_phone);
        } else {
            // Gets postal address label type
            CharSequence label = StructuredPostal.getTypeLabel(getResources(), phoneType, phoneTypeLabel);

            // Sets TextView objects in the layout
            headerTextView.setText(label);
            phoneTextView.setText(phoneNum);

            // Defines an onClickListener object for the address button
            btnJoin.setOnClickListener(new View.OnClickListener() {
                // Defines what to do when users click the address button
                @Override
                public void onClick(View view) {                	
                        Intent data = new Intent();
            			data.putExtra("JOIN_PHONE_NUMBER", phoneNum);
            			data.putExtra("JOIN_CONTACT_NAME", getActivity().getTitle());
                        getActivity().setResult(Activity.RESULT_OK, data);
                        getActivity().finish();
                }
            });

        }
        return phoneLayout;
    }

    private int getLargestScreenDimension() {
        final DisplayMetrics displayMetrics = new DisplayMetrics();

        // Retrieves a displayMetrics object for the device's default display
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        // Returns the larger of the two values
        return height > width ? height : width;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Bitmap loadContactPhoto(Uri contactUri, int imageSize) {

        if (!isAdded() || getActivity() == null) {
            return null;
        }

        // Instantiates a ContentResolver for retrieving the Uri of the image
        final ContentResolver contentResolver = getActivity().getContentResolver();

        // Instantiates an AssetFileDescriptor. Given a content Uri pointing to an image file, the
        // ContentResolver can return an AssetFileDescriptor for the file.
        AssetFileDescriptor afd = null;

        if (Utils.hasICS()) {
            // On platforms running Android 4.0 (API version 14) and later, a high resolution image
            // is available from Photo.DISPLAY_PHOTO.
            try {
                // Constructs the content Uri for the image
                Uri displayImageUri = Uri.withAppendedPath(contactUri, Photo.DISPLAY_PHOTO);

                // Retrieves an AssetFileDescriptor from the Contacts Provider, using the
                // constructed Uri
                afd = contentResolver.openAssetFileDescriptor(displayImageUri, "r");
                // If the file exists
                if (afd != null) {
                    // Reads and decodes the file to a Bitmap and scales it to the desired size
                    return ImageLoader.decodeSampledBitmapFromDescriptor(
                            afd.getFileDescriptor(), imageSize, imageSize);
                }
            } catch (FileNotFoundException e) {
                // Catches file not found exceptions
                if (BuildConfig.DEBUG) {
                    // Log debug message, this is not an error message as this exception is thrown
                    // when a contact is legitimately missing a contact photo (which will be quite
                    // frequently in a long contacts list).
                    Log.d(TAG, "Contact photo not found for contact " + contactUri.toString()
                            + ": " + e.toString());
                }
            } finally {
                // Once the decode is complete, this closes the file. You must do this each time
                // you access an AssetFileDescriptor; otherwise, every image load you do will open
                // a new descriptor.
                if (afd != null) {
                    try {
                        afd.close();
                    } catch (IOException e) {
                        // Closing a file descriptor might cause an IOException if the file is
                        // already closed. Nothing extra is needed to handle this.
                    }
                }
            }
        }

        // If the platform version is less than Android 4.0 (API Level 14), use the only available
        // image URI, which points to a normal-sized image.
        try {
            // Constructs the image Uri from the contact Uri and the directory twig from the
            // Contacts.Photo table
            Uri imageUri = Uri.withAppendedPath(contactUri, Photo.CONTENT_DIRECTORY);

            // Retrieves an AssetFileDescriptor from the Contacts Provider, using the constructed
            // Uri
            afd = getActivity().getContentResolver().openAssetFileDescriptor(imageUri, "r");

            // If the file exists
            if (afd != null) {
                // Reads the image from the file, decodes it, and scales it to the available screen
                // area
                return ImageLoader.decodeSampledBitmapFromDescriptor(
                        afd.getFileDescriptor(), imageSize, imageSize);
            }
        } catch (FileNotFoundException e) {
            // Catches file not found exceptions
            if (BuildConfig.DEBUG) {
                // Log debug message, this is not an error message as this exception is thrown
                // when a contact is legitimately missing a contact photo (which will be quite
                // frequently in a long contacts list).
                Log.d(TAG, "Contact photo not found for contact " + contactUri.toString()
                        + ": " + e.toString());
            }
        } finally {
            // Once the decode is complete, this closes the file. You must do this each time you
            // access an AssetFileDescriptor; otherwise, every image load you do will open a new
            // descriptor.
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {
                    // Closing a file descriptor might cause an IOException if the file is
                    // already closed. Ignore this.
                }
            }
        }

        // If none of the case selectors match, returns null.
        return null;
    }

    /**
     * This interface defines constants used by contact retrieval queries.
     */
    public interface ContactDetailQuery {
        // A unique query ID to distinguish queries being run by the
        // LoaderManager.
        final static int QUERY_ID = 1;

        // The query projection (columns to fetch from the provider)
        @SuppressLint("InlinedApi")
        final static String[] PROJECTION = {
                Contacts._ID,
                Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME,
        };

        // The query column numbers which map to each value in the projection
        final static int ID = 0;
        final static int DISPLAY_NAME = 1;
    }

    /**
     * This interface defines constants used by address retrieval queries.
     */
    public interface ContactPhoneQuery {
        final static int QUERY_ID = 2;
        
        final static String[] PROJECTION = {        		
            Phone._ID,
            Phone.NUMBER,
            Phone.TYPE,
            Phone.LABEL
        };

        final static String SELECTION = Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'";

        // The query column numbers which map to each value in the projection
        final static int ID = 0;
        final static int NUMBER = 1;
        final static int TYPE = 2;
        final static int LABEL = 3;
    }
}
