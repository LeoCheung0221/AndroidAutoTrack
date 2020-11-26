package com.tufusi.track.sdk.listener;

import android.view.View;

import com.tufusi.track.sdk.TufusiDataPrivate;

/**
 * Created by LeoCheung on 2020/11/24.
 *
 * @description
 */
public class WrapperOnClickListener implements View.OnClickListener {

    private View.OnClickListener mListener;

    public WrapperOnClickListener(View.OnClickListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View v) {
        // 植入原有的点击事件监听逻辑处理
        try {
            if (mListener != null) {
                mListener.onClick(v);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // 植入埋点代码
        TufusiDataPrivate.trackViewOnClick(v);
    }
}