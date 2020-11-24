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
    private static TufusiDataApi INSTANCE;
    private static Map<String, Object> mDeviceInfo;
    private String mDeviceId;

    private TufusiDataApi(Application application) {
        mDeviceId = TufusiDataPrivate.getAndroidID(application.getApplicationContext());
        mDeviceInfo = TufusiDataPrivate.getDeviceInfo(application.getApplicationContext());
        TufusiDataPrivate.registerActivityLifecycleCallbacks(application);
        TufusiDataPrivate.registerActivityStateObserver(application);
    }

    /**
     * 埋点初始化函数 内部实现单例模式 用私有构造函数初始化埋点sdk
     *
     * @param application 应用对象
     * @return 单例对象
     */
    @Keep
    public static TufusiDataApi init(Application application) {
        synchronized (LOCK) {
            if (null == INSTANCE) {
                INSTANCE = new TufusiDataApi(application);
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
}