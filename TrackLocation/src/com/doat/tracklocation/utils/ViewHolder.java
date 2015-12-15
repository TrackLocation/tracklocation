package com.doat.tracklocation.utils;

import android.util.SparseArray;
import android.view.View;

public class ViewHolder
{
    private SparseArray<View> storedViews = new SparseArray<View>();

    public ViewHolder()
    {
    }
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