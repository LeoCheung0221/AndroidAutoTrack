package com.tufusi.track.sdk.listener;

import android.widget.RatingBar;

import com.tufusi.track.sdk.TufusiDataPrivate;

/**
 * Created by LeoCheung on 2020/11/24.
 *
 * @description
 */
public class WrapperOnRatingBarChangeListener implements RatingBar.OnRatingBarChangeListener {

    private RatingBar.OnRatingBarChangeListener mOnRatingBarChangeListener;

    public WrapperOnRatingBarChangeListener(RatingBar.OnRatingBarChangeListener onRatingBarChangeListener) {
        mOnRatingBarChangeListener = onRatingBarChangeListener;
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        try {
            if (mOnRatingBarChangeListener != null) {
                mOnRatingBarChangeListener.onRatingChanged(ratingBar, rating, fromUser);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        TufusiDataPrivate.trackViewOnClick(ratingBar);
    }
}