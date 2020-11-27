package com.tufusi.track.sdk.accessibility;

import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.tufusi.track.sdk.TufusiDataPrivate;

/**
 * Created by LeoCheung on 2020/11/26.
 *
 * @description
 */
public class TufusiDataAccessibilityDelegate extends View.AccessibilityDelegate {

    private View.AccessibilityDelegate mRealDelegate;

    public TufusiDataAccessibilityDelegate(View.AccessibilityDelegate delegate) {
        mRealDelegate = delegate;
    }

    @Override
    public void sendAccessibilityEvent(View host, int eventType) {
        if (mRealDelegate != null) {
            mRealDelegate.sendAccessibilityEvent(host, eventType);
        }

        // 如果事件类型是点击类型 埋点监控
        if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            TufusiDataPrivate.trackViewOnClick(host);
        }
    }
}