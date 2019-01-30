package com.ly.neuter.core.utils;

import android.util.SparseArray;
import android.view.View;

/**
 * 通用ViewHolder工具类
 * Created by fzJiang on 2016-5-9.
 */
public class ViewHolder {


    @SuppressWarnings("unchecked")
    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);
        }

        View childView = viewHolder.get(id);

        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }
}