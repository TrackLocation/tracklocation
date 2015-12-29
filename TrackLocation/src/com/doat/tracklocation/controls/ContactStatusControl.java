package com.doat.tracklocation.controls;

import com.doat.tracklocation.R;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.support.v4.graphics.drawable.DrawableCompat;

public class ContactStatusControl extends FrameLayout {
	private ProgressBar progressBar;
	private Bitmap bitmap;
	private ImageView imageView;
	private ImageView favView;
	private boolean bStatusDrawVisible = false;
		
	private int mContactStatus = CommonConst.CONTACT_STATUS_START_CONNECT;
	private boolean mIsFavorite = false;
	private boolean mDrawFavorite = true;

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
    }
    
	public void setBitmap(Bitmap bitmap){
		this.bitmap = bitmap;
		Drawable normalDrawable = new BitmapDrawable(this.getResources(), bitmap);
		/*	Drawable wrappedDrawable = DrawableCompat.wrap(normalDrawable);
	
		DrawableCompat.setTintList(wrappedDrawable, getResources().getColorStateList(R.drawable.drawable_selector));		
		DrawableCompat.setTintMode(wrappedDrawable, Mode.SRC_IN);
		imageView.setImageDrawable(wrappedDrawable);*/
		//Drawable contactPhoto = new BitmapDrawable(this.getResources(), bitmap);
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
			mContactStatus = CommonConst.CONTACT_STATUS_CONNECTED;
			progressBar.setIndeterminate(false);
		}
	}
	
	private void setStartPending(){	
		if (bStatusDrawVisible){
			if (mContactStatus == CommonConst.CONTACT_STATUS_CONNECTED){
				progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.Orange), android.graphics.PorterDuff.Mode.SRC_IN);									
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
		/*Drawable contactPhoto = new BitmapDrawable(this.getResources(), bitmap);
		Bitmap disBitmap = Utils.changeBitmapColor(bitmap);
	    Drawable icon = enabled ? contactPhoto : new BitmapDrawable(this.getResources(), disBitmap);	    
		imageView.setImageDrawable(icon);*/

	}
	
/*	private static Drawable convertDrawableToGrayScale(Drawable drawable) {
	    if (drawable == null) {
	        return null;
	    }
	    Drawable res = drawable.mutate();
	    res.setColorFilter(Color.GRAY, Mode.SRC_OVER);
	    return res;
	}
*/	
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
}
