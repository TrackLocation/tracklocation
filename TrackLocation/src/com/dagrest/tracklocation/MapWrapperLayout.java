package com.dagrest.tracklocation;

import android.content.Context;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.widget.FrameLayout;


public class MapWrapperLayout extends FrameLayout {
	public interface OnDragListener {
        public void onDrag(MotionEvent motionEvent);
        public void onDrag(DragEvent dragEvent);
    }

    private OnDragListener mOnDragListener;

    public MapWrapperLayout(Context context) {
        super(context);
    }
    
    @Override
    public boolean dispatchDragEvent(DragEvent event) {
    	boolean r = super.dispatchDragEvent(event);
        if (r && (event.getAction() == DragEvent.ACTION_DRAG_STARTED
                || event.getAction() == DragEvent.ACTION_DRAG_ENDED)){
            // If we got a start or end and the return value is true, our
            // onDragEvent wasn't called by ViewGroup.dispatchDragEvent
            // So we do it here.
        	mOnDragListener.onDrag(event);
        }
        return r;
    	/*if (mOnDragListener != null) {
            mOnDragListener.onDrag(event);
        }
    	return super.dispatchDragEvent(event);*/
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mOnDragListener != null) {
            mOnDragListener.onDrag(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setOnDragListener(OnDragListener mOnDragListener) {
        this.mOnDragListener = mOnDragListener;
    }
}
