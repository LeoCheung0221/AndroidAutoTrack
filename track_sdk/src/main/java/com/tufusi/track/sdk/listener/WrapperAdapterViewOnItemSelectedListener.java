package com.tufusi.track.sdk.listener;

import android.view.View;
import android.widget.AdapterView;

import com.tufusi.track.sdk.TufusiDataPrivate;

/**
 * Created by LeoCheung on 2020/11/24.
 *
 * @description
 */
public class WrapperAdapterViewOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

    private AdapterView.OnItemSelectedListener mOnItemSelectedListener;

    public WrapperAdapterViewOnItemSelectedListener(AdapterView.OnItemSelectedListener onItemSelectedListener) {
        mOnItemSelectedListener = onItemSelectedListener;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (mOnItemSelectedListener != null) {
            mOnItemSelectedListener.onItemSelected(parent, view, position, id);
        }

        TufusiDataPrivate.trackViewOnClick(parent);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        if (mOnItemSelectedListener != null) {
            mOnItemSelectedListener.onNothingSelected(parent);
        }
    }
}