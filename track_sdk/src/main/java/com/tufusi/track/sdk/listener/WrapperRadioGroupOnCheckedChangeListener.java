package com.tufusi.track.sdk.listener;

import android.widget.RadioGroup;

import com.tufusi.track.sdk.TufusiDataPrivate;

/**
 * Created by LeoCheung on 2020/11/24.
 *
 * @description
 */
public class WrapperRadioGroupOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {

    private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener;

    public WrapperRadioGroupOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener onCheckedChangeListener) {
        mOnCheckedChangeListener = onCheckedChangeListener;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        try {
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(group, checkedId);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        TufusiDataPrivate.trackViewOnClick(group);
    }
}