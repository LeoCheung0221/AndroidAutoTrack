package com.tufusi.track.sdk.transparent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tufusi.track.sdk.TufusiDataPrivate;
import com.tufusi.track.sdk.utils.ViewUtils;

/**
 * Created by LeoCheung on 2020/11/26.
 *
 * @description
 */
public class WrapperAppClickOverlayLayout extends FrameLayout {

    public WrapperAppClickOverlayLayout(@NonNull Context context) {
        this(context, null);
    }

    public WrapperAppClickOverlayLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WrapperAppClickOverlayLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if (event != null) {
                int action = event.getAction() & MotionEvent.ACTION_MASK;
                if (action == MotionEvent.ACTION_DOWN) {
                    View view = getTargetView((ViewGroup) getRootView(), event);
                    if (view != null) {
                        if (view instanceof AdapterView) {
                            TufusiDataPrivate.trackAdapterViewOnClick((AdapterView<?>) view, event);
                        } else {
                            TufusiDataPrivate.trackViewOnClick(view);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return super.onTouchEvent(event);
    }

    private View getTargetView(ViewGroup viewGroup, MotionEvent event) {
        if (viewGroup == null) {
            return null;
        }

        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = viewGroup.getChildAt(i);
            if (!child.isShown()) {
                continue;
            }
            if (ViewUtils.isContainView(child, event)) {
                if (child.hasOnClickListeners()
                        || child instanceof CompoundButton
                        || child instanceof SeekBar
                        || child instanceof RatingBar) {
                    return child;
                } else if (TufusiDataPrivate.matchDiffViewClickListener(child)) {
                }
            }

            if (child instanceof ViewGroup) {
                View targetView = getTargetView((ViewGroup) child, event);
                if (targetView != null) {
                    return targetView;
                }
            }
        }
        return null;
    }
}