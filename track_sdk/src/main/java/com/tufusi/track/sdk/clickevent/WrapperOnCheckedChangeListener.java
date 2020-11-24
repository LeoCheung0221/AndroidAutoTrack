package com.tufusi.track.sdk.clickevent;

import android.widget.CompoundButton;

import com.tufusi.track.sdk.TufusiDataPrivate;

/**
 * Created by LeoCheung on 2020/11/24.
 *
 * @description
 */
public class WrapperOnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;

    public WrapperOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        mOnCheckedChangeListener = onCheckedChangeListener;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // 插入埋点
        TufusiDataPrivate.trackViewOnClick(buttonView);
    }
}