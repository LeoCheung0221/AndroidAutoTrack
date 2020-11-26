package com.tufusi.track.sdk.listener;

import android.view.View;
import android.widget.ExpandableListView;

import com.tufusi.track.sdk.TufusiDataPrivate;

/**
 * Created by LeoCheung on 2020/11/24.
 *
 * @description
 */
public class WrapperExpandOnGroupClickListener implements ExpandableListView.OnGroupClickListener {

    private ExpandableListView.OnGroupClickListener mOnGroupClickListener;

    public WrapperExpandOnGroupClickListener(ExpandableListView.OnGroupClickListener onGroupClickListener) {
        mOnGroupClickListener = onGroupClickListener;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        TufusiDataPrivate.trackAdapterViewOnClick(parent, v, groupPosition, -1);

        if (mOnGroupClickListener != null) {
            mOnGroupClickListener.onGroupClick(parent, v, groupPosition, id);
        }
        return false;
    }
}