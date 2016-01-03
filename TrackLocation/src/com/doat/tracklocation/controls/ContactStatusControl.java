package com.doat.tracklocation.controls;

import com.doat.tracklocation.R;
import com.doat.tracklocation.utils.CommonConst;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ContactStatusControl extends FrameLayout {
	private ProgressBar progressBar;
	private Bitmap bitmap;
	private ImageView imageView;
	private ImageView favView;
	private boolean bStatusDrawVisible = false;
		
	private int mContactStatus = CommonConst.CONTACT_STATUS_START_CONNECT;
	private int mSecondaryContactStatus = CommonConst.CONTACT_STATUS_START_CONNECT;
	private boolean mIsFavorite = false;
	private boolean mDrawFavorite = true;
	private AlphaAnimation imgAnimation;

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
        
        favView = (ImageView) v.findViewById(R.id.favorite_image);
           
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StatusImageView);            
            setStatusDrawVisible(a.getBoolean(R.styleable.StatusImageView_siv_showStatus, false));
            a.recycle();
        }
        
        imgAnimation = new AlphaAnimation(1.0f, 0.3f);
		imgAnimation.setDuration(1000);
		imgAnimation.setRepeatMode(Animation.REVERSE);
		imgAnimation.setRepeatCount(Animation.INFINITE);
		imgAnimation.setFillAfter(true);
    }
    
	public void setBitmap(Bitmap bitmap){
		this.bitmap = bitmap;
		Drawable normalDrawable = new BitmapDrawable(this.getResources(), bitmap);
		imageView.setImageDrawable(normalDrawable);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {	
		super.onSizeChanged(w, h, oldw, oldh);	
		favView.setTop((int)(this.getHeight() *2 / 3));
		favView.setLeft((int)(this.getWidth() *2 / 3));
	}
	
	private void setCompleted(){	
		if (bStatusDrawVisible){
			if (this.mSecondaryContactStatus == CommonConst.CONTACT_STATUS_PENDING){
				mSecondaryContactStatus = CommonConst.CONTACT_STATUS_START_CONNECT;
				mContactStatus = CommonConst.CONTACT_STATUS_CONNECTED;
				setStartPending();
				return;
			}
				
			if (mContactStatus == CommonConst.CONTACT_STATUS_PENDING){
				imageView.clearAnimation();
			}
			else{
				progressBar.setIndeterminate(false);
			}
			mContactStatus = CommonConst.CONTACT_STATUS_CONNECTED;
			progressBar.setIndeterminate(false);
		}
	}
	
	private void setStartPending(){	
		if (bStatusDrawVisible){
			if (mContactStatus == CommonConst.CONTACT_STATUS_CONNECTED){				
				imageView.startAnimation(imgAnimation);
				mContactStatus = CommonConst.CONTACT_STATUS_PENDING;
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
	
	public void setFavorite(boolean value){
		mIsFavorite = value;
		if (mDrawFavorite){
			favView.setVisibility(mIsFavorite ? View.VISIBLE : View.GONE);
		}
	}
	
	public boolean isFavorite(){		
		return mIsFavorite;
	}
	
	public void setDrawFavorite(boolean value){		
		mDrawFavorite  = value;		
	}
	
	public void setSelected(boolean bSelected){
		imageView.setAlpha(bSelected ? 0.5f : 1f);
	}

	public void setSecondaryContactStatus(int mSecondaryContactStatus) {
		this.mSecondaryContactStatus = mSecondaryContactStatus;
	}
}
