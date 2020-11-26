package com.tufusi.track.sdk.window;

import android.app.Activity;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.tufusi.track.sdk.TufusiDataPrivate;
import com.tufusi.track.sdk.listener.WrapperAdapterViewOnItemSelectedListener;
import com.tufusi.track.sdk.listener.WrapperExpandOnChildClickListener;
import com.tufusi.track.sdk.listener.WrapperExpandOnGroupClickListener;
import com.tufusi.track.sdk.utils.ViewUtils;

import java.util.ArrayList;

/**
 * Created by LeoCheung on 2020/11/25.
 *
 * @description 封装处理分发事件埋点逻辑
 */
public class TouchEventHandler {

    public static void dispatchTouchEvent(Activity activity, MotionEvent event) {
        // 手指抬起 遍历view 开启埋点
        if (event.getAction() == MotionEvent.ACTION_UP) {
            ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
            ArrayList<View> targetViews = getTargetViews(rootView, event);

            if (targetViews.isEmpty()) {
                return;
            }

            for (View view : targetViews) {
                if (view == null) {
                    continue;
                }

                if (view instanceof AdapterView) {
                    if (view instanceof Spinner) {
                        TufusiDataPrivate.trackViewOnClick(view);

                        AdapterView.OnItemSelectedListener onItemSelectedListener = ((Spinner) view).getOnItemSelectedListener();
                        if (onItemSelectedListener != null && !(onItemSelectedListener instanceof WrapperAdapterViewOnItemSelectedListener)) {
                            ((Spinner) view).setOnItemSelectedListener(new WrapperAdapterViewOnItemSelectedListener(onItemSelectedListener));
                        }
                    } else if (view instanceof ExpandableListView) {
                        try {
                            ExpandableListView.OnGroupClickListener onGroupClickListener = TufusiDataPrivate.getExpandableOnGroupClickListener(view);
                            if (onGroupClickListener != null && !(onGroupClickListener instanceof WrapperExpandOnGroupClickListener)) {
                                ((ExpandableListView) view).setOnGroupClickListener(onGroupClickListener);
                            }

                            ExpandableListView.OnChildClickListener onChildClickListener = TufusiDataPrivate.getExpandableOnChildClickListener(view);
                            if (onChildClickListener != null && !(onChildClickListener instanceof WrapperExpandOnChildClickListener)) {
                                ((ExpandableListView) view).setOnChildClickListener(onChildClickListener);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else if (view instanceof ListView || view instanceof GridView) {
                        TufusiDataPrivate.trackAdapterViewOnClick((AdapterView<?>) view, event);
                    }
                } else {
                    TufusiDataPrivate.trackViewOnClick(view);
                }
            }
        }
    }

    /**
     * 遍历 触摸点下的 ViewGroup，并存储
     */
    private static ArrayList<View> getTargetViews(View view, MotionEvent event) {
        ArrayList<View> targetViews = new ArrayList<>();
        try {
            if (ViewUtils.isVisible(view) && ViewUtils.isContainView(view, event)) {
                if (view instanceof AdapterView) {
                    targetViews.add(view);
                    // 如果是 ViewGroup 则继续遍历添加
                    getTargetViewsInGroup((ViewGroup) view, event, targetViews);
                } else if (view.isClickable() || view instanceof SeekBar || view instanceof RatingBar) {
                    targetViews.add(view);
                } else if (view instanceof ViewGroup) {
                    getTargetViewsInGroup((ViewGroup) view, event, targetViews);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return targetViews;
    }

    private static void getTargetViewsInGroup(ViewGroup viewGroup, MotionEvent event, ArrayList<View> targetViews) {
        try {
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = viewGroup.getChildAt(i);
                ArrayList<View> childTargetViews = getTargetViews(child, event);

                if (!childTargetViews.isEmpty()) {
                    targetViews.addAll(childTargetViews);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}