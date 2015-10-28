package com.dagrest.tracklocation.utils;

import android.util.SparseArray;
import android.view.View;

public class ViewHolder
{
    private SparseArray<View> storedViews = new SparseArray<View>();

    public ViewHolder()
    {
    }

    /**
     * 
     * @param view
     *            The view to add; to reference this view later, simply refer to its id.
     * @return This instance to allow for chaining.
     */
    public ViewHolder addView(View view)
    {
        int id = view.getId();
        storedViews.put(id, view);
        return this;
    }

    public View getView(int id)
    {
        return storedViews.get(id);
    }
}