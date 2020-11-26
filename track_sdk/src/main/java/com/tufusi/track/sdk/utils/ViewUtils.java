package com.tufusi.track.sdk.utils;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by LeoCheung on 2020/11/26.
 *
 * @description
 */
public class ViewUtils {

    /**
     * 视图是否可见
     */
    public static boolean isVisible(View view) {
        return view.getVisibility() == View.VISIBLE;
    }

    /**
     * 是否包含子 View
     */
    public static boolean isContainView(View view, MotionEvent event) {
        // 获取触摸点的绝对位置
        double rawX = event.getRawX();
        double rawY = event.getRawY();
        Rect rect = new Rect();
        // 获取全局范围的可视矩形区域
        view.getGlobalVisibleRect(rect);
        // 判断该区域是否包含在点击区域
        return rect.contains((int) rawX, (int) rawY);
    }

} 