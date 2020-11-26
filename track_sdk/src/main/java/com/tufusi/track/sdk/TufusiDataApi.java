package com.tufusi.track.sdk;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.ViewTreeObserver;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import org.json.JSONObject;

import java.util.Map;

import static com.tufusi.track.sdk.TrackClickMode.CUSTOM_LISTENER;

/**
 * Created by LeoCheung on 2020/11/3.
 *
 * @description 数据采集对外展示接口类
 */
@Keep
public class TufusiDataApi {

    private static final String TAG = TufusiDataApi.class.getSimpleName();

    public static final String SDK_VERSION = "1.0.0";
    private static final Object LOCK = new Object();
    private static final String APP_CLICK = "$AppClick";
    private static final String TRACK_CLICK_MODE_1 = "代理 View.OnClickListener";
    private static final String TRACK_CLICK_MODE_2 = "代理 Window.Callback";
    private static final String TRACK_CLICK_MODE_3 = "代理 View.AccessibilityDelegate";
    private static final String TRACK_CLICK_MODE_4 = "透明层代理";

    private static TufusiDataApi INSTANCE;
    private static Map<String, Object> mDeviceInfo;
    private static TrackClickMode mode;
    private String mDeviceId;

    private TufusiDataApi(Application application, TrackClickMode mode) {
        TufusiDataPrivate.setTrackClickMode(mode);

        mDeviceId = TufusiDataPrivate.getAndroidID(application.getApplicationContext());
        mDeviceInfo = TufusiDataPrivate.getDeviceInfo(application.getApplicationContext());

        TufusiDataPrivate.registerActivityLifecycleCallbacks(application);
        TufusiDataPrivate.registerActivityStateObserver(application);
    }

    /**
     * 埋点初始化函数 内部实现单例模式 用私有构造函数初始化埋点sdk
     *
     * @param application 应用对象
     * @param mode        点击事件埋点模式
     * @return 单例对象
     */
    @Keep
    public static TufusiDataApi init(Application application, TrackClickMode mode) {
        TufusiDataApi.mode = mode;
        synchronized (LOCK) {
            if (null == INSTANCE) {
                INSTANCE = new TufusiDataApi(application, mode);
            }
        }
        return INSTANCE;
    }

    /**
     * 静态方法，获取埋点sdk实体对象
     *
     * @return 返回实体对象
     */
    public static TufusiDataApi getInstance() {
        return INSTANCE;
    }

    /**
     * 埋点跟踪
     *
     * @param option     跟踪手段 $AppViewScreen
     * @param properties 采集信息：事件自定义属性
     */
    public void track(@NonNull String option, @Nullable JSONObject properties) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("event", option);
            if (option.equals(APP_CLICK)) {
                jsonObject.put("track_mode", getTrackClickMode(TufusiDataApi.mode));
            }
            jsonObject.put("device_id", mDeviceId);

            JSONObject sendProperties = new JSONObject(mDeviceInfo);
            if (properties != null) {
                TufusiDataPrivate.mergeJSONObject(properties, sendProperties);
            }

            jsonObject.put("properties", sendProperties);
            jsonObject.put("time", System.currentTimeMillis());

            Log.i(TAG, TufusiDataPrivate.formatJson(jsonObject.toString()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取当前点击事件埋点方案
     *
     * @param mode 埋点模式
     * @return 埋点方案名称
     */
    public String getTrackClickMode(TrackClickMode mode) {
        switch (mode) {
            case CUSTOM_LISTENER:
                return TRACK_CLICK_MODE_1;
            case WINDOW_CALLBACK:
                return TRACK_CLICK_MODE_2;
            case ACCESSIBILITY_DELEGATE:
                return TRACK_CLICK_MODE_3;
            case TRANSPARENT_LAYOUT:
                return TRACK_CLICK_MODE_4;
            default:
                return "";
        }
    }

    /**
     * 埋点 Dialog 点击操作
     */
    public void trackDialog(@NonNull final Activity activity, @NonNull final Dialog dialog) {
        if (dialog.getWindow() != null) {
            dialog.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            TufusiDataPrivate.delegateViewsOnClickListener(activity, dialog.getWindow().getDecorView());
                        }
                    }
            );
        }
    }

    /**
     * 恢复采集指定 Activity 的页面浏览事件
     *
     * @param activity 指定页面
     */
    public void removeIgnoredActivity(Class<?> activity) {
        TufusiDataPrivate.removeIgnoredActivity(activity);
    }

    public void setTrackClickMode(TrackClickMode clickMode) {
        mode = clickMode;
    }
}