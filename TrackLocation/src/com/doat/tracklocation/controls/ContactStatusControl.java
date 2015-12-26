package com.doat.tracklocation.controls;

import com.doat.tracklocation.R;
import com.doat.tracklocation.utils.CommonConst;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ContactStatusControl extends FrameLayout {
	private ProgressBar progressBar;
	private Bitmap bitmap;
	private ImageView imageView;
	private boolean bStatusDrawVisible = false;
		
	private int mContactStatus = CommonConst.CONTACT_STATUS_START_CONNECT;

	public int getContactStatus() {
		return mContactStatus;
	}

	public void setContactStatus(int contactStatus) {
		if (contactStatus == CommonConst.CONTACT_STATUS_CONNECTED){
			setCompleted();
		}
		else if (contactStatus == CommonConst.CONTACT_STATUS_PENDING){
			setStartPending();
		}
		
		this.mContactStatus = contactStatus;
	}

	public ContactStatusControl(Context context) {
		super(context);
		if (!isInEditMode()){ 
			init(context,null, 0);
		}
    }

    public ContactStatusControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()){
        	init(context,attrs, 0);
        }
    }

    public ContactStatusControl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()){
        	init(context,attrs, defStyle);
        }
    }

    protected void init(Context context,AttributeSet attrs, int defStyle) {
        View v = View.inflate(context, R.layout.status_image_layout,this);
        setClipChildren(false);        
        progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        imageView = (ImageView) v.findViewById(R.id.icon);	
           
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StatusImageView);            
            setStatusDrawVisible(a.getBoolean(R.styleable.StatusImageView_siv_showStatus, false));
            a.recycle();
        }
    }
    
	public void setBitmap(Bitmap bitmap){
		this.bitmap = bitmap;
		Drawable contactPhoto = new BitmapDrawable(this.getResources(), bitmap);
		imageView.setImageDrawable(contactPhoto);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {	
		super.onSizeChanged(w, h, oldw, oldh);	
		
		//progressBar.getLayoutParams().height
		//progressBar.get
	}
	
	private void setCompleted(){	
		if (bStatusDrawVisible){
			mContactStatus = CommonConst.CONTACT_STATUS_CONNECTED;
			progressBar.setIndeterminate(false);
		}
	}
	
	private void setStartPending(){	
		if (bStatusDrawVisible){
			if (mContactStatus == CommonConst.CONTACT_STATUS_CONNECTED){
				progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.Green), android.graphics.PorterDuff.Mode.SRC_IN);									
				progressBar.setIndeterminate(true);
			}						
		}
	}
	
	public void setStatusDrawVisible(boolean bIsVisible){
		bStatusDrawVisible = bIsVisible;
		progressBar.setVisibility( bIsVisible ? View.VISIBLE : View.GONE);		
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		imageView.setEnabled(enabled);
	}
}
