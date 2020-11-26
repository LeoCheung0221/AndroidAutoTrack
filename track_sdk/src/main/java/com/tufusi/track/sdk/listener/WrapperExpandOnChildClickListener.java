package com.tufusi.track.sdk.listener;

import android.view.View;
import android.widget.ExpandableListView;

import com.tufusi.track.sdk.TufusiDataPrivate;

/**
 * Created by LeoCheung on 2020/11/24.
 *
 * @description
 */
public class WrapperExpandOnChildClickListener implements ExpandableListView.OnChildClickListener {

    private ExpandableListView.OnChildClickListener mOnChildClickListener;

    public WrapperExpandOnChildClickListener(ExpandableListView.OnChildClickListener onChildClickListener) {
        mOnChildClickListener = onChildClickListener;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        TufusiDataPrivate.trackAdapterViewOnClick(parent, v, groupPosition, childPosition);

        if (mOnChildClickListener != null) {
            mOnChildClickListener.onChildClick(parent, v, groupPosition, childPosition, id);
        }
        return false;
    }
}