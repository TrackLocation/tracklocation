package com.doat.tracklocation.controls;

import com.doat.tracklocation.R;
import com.doat.tracklocation.utils.Utils;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class StatusImage extends FrameLayout {
	private ProgressBar progressBar;
	private Bitmap bitmap;
	private ImageView imageView;
	private boolean bStatusDrawVisible = false;

	public StatusImage(Context context) {
		super(context);
		if (!isInEditMode()){ 
			init(context,null, 0);
		}
    }

    public StatusImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()){
        	init(context,attrs, 0);
        }
    }

    public StatusImage(Context context, AttributeSet attrs, int defStyle) {
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
	
	public void setCompleted(int contactStatus){	
		if (bStatusDrawVisible){
			progressBar.setIndeterminate(false);
		}
	}
	
	public void setStatusDrawVisible(boolean bIsVisible){
		bStatusDrawVisible = bIsVisible;
		progressBar.setVisibility( bIsVisible ? View.VISIBLE : View.GONE);		
	}
}
