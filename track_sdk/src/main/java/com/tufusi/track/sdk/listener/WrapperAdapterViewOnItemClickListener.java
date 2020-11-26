package com.tufusi.track.sdk.listener;

import android.view.View;
import android.widget.AdapterView;

import com.tufusi.track.sdk.TufusiDataPrivate;

/**
 * Created by LeoCheung on 2020/11/24.
 *
 * @description
 */
public class WrapperAdapterViewOnItemClickListener implements AdapterView.OnItemClickListener {

    private AdapterView.OnItemClickListener mOnItemClickListener;

    public WrapperAdapterViewOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(parent, view, position, id);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        TufusiDataPrivate.trackAdapterViewOnClick(parent, view, position);
    }
}