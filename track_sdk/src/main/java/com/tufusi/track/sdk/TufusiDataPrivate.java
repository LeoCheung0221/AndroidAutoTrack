package com.tufusi.track.sdk;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by LeoCheung on 2020/11/3.
 *
 * @description
 */
class TufusiDataPrivate {

    private static final long SESSION_INTERVAL_TIME = 30 * 1000;

    /**
     * 需要忽略跟踪的Activity集合
     */
    private static List<String> mIgnoredActivities;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" + ".SSS", Locale.CHINA);
    private static TufusiDatabaseHelper mDatabaseHelper;
    private static CountDownTimer countDownTimer;

    static {
        mIgnoredActivities = new ArrayList<>();
    }

    private static WeakReference<Activity> mCurrentActivity;

    /**
     * 添加需忽略跟踪的Activity
     */
    public static void ignoreAutoTrackActivity(Class<?> activity) {
        if (activity == null) {
            return;
        }
        mIgnoredActivities.add(activity.getCanonicalName());
    }

    /**
     * 移除指定的Activity
     */
    public static void removeAutoTrackActivity(Class<?> activity) {
        if (activity == null) {
            return;
        }
        mIgnoredActivities.remove(activity.getCanonicalName());
    }

    /**
     * 获取Android设备ID
     *
     * @param context 上下文环境
     */
    @SuppressLint("HardwareIds")
    public static String getAndroidID(Context context) {
        String androidID = "";
        try {
            androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return androidID;
    }

    /**
     * 获取设备信息
     * 当前 Android SDK版本号、手机系统、厂商信息、手机型号、APP版本信息、屏幕宽高信息
     *
     * @return 返回不可修改的指定映射
     */
    public static Map<String, Object> getDeviceInfo(Context context) {
        Map<String, Object> deviceInfo = new HashMap<>();
        {
            deviceInfo.put("$lib", "Android");
            deviceInfo.put("$lib_version", TufusiDataApi.SDK_VERSION);
            deviceInfo.put("$os", "Android");
            deviceInfo.put("$os_version", Build.VERSION.RELEASE == null ? "UNKNOWN" : Build.VERSION.RELEASE);
            deviceInfo.put("$manufacturer", Build.MANUFACTURER == null ? "UNKNOWN" : Build.MANUFACTURER);
            deviceInfo.put("$model", TextUtils.isEmpty(Build.MODEL) ? "UNKNOWN" : Build.MODEL.trim());

            try {
                final PackageManager manager = context.getPackageManager();
                final PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
                deviceInfo.put("$app_version", packageInfo.versionName);

                int labelRes = packageInfo.applicationInfo.labelRes;
                deviceInfo.put("$app_name", context.getResources().getString(labelRes));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            deviceInfo.put("$screen_width", displayMetrics.widthPixels);
            deviceInfo.put("$screen_height", displayMetrics.heightPixels);

            return Collections.unmodifiableMap(deviceInfo);
        }
    }

    /**
     * 注册 Application.ActivityLifecycleCallbacks 以此绑定应用生命周期
     *
     * @param application 整个应用对象
     */
    public static void registerActivityLifecycleCallbacks(Application application) {
        mDatabaseHelper = new TufusiDatabaseHelper(application.getApplicationContext(), application.getPackageName());

        // 计时器 如果计时器终止 则触发统计事件 跟踪关闭，每十秒触发一次，30秒之后结束计时器
        countDownTimer = new CountDownTimer(SESSION_INTERVAL_TIME, 10 * 100) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (mCurrentActivity != null) {
                    // 在此埋点跟踪 AppEnd
                    trackAppEnd(mCurrentActivity.get());
                }
            }
        };

        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                // 设置 App Start 状态为 true
                mDatabaseHelper.commitAppStartEvent(true);

                long timeDiff = System.currentTimeMillis() - mDatabaseHelper.getAppPausedTime();
                // 如果暂停时长超过自定义间隔时长，则认为是满足 跟踪 AppEnd 事件条件
                // 一旦跳转新页面，即使前一个界面未被finish掉，由于间隔时间太短也不会命中此处
                if (timeDiff > SESSION_INTERVAL_TIME) {
                    // 获取 AppEnd 状态，如果不是结束状态，只要没有进入后台均不会命中此处
                    if (!mDatabaseHelper.getAppEndEventState()) {
                        trackAppEnd(activity);
                    }
                }

                // 如果获取到的 AppEnd 状态是已结束，则重新开启，App 只要在前台均会命中此处
                if (mDatabaseHelper.getAppEndEventState()) {
                    // 一旦进入，即设置结束状态false,从而不会再次进入这里
                    mDatabaseHelper.commitAppEndEventState(false);
                    trackAppStart(activity);
                }
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                // 在此埋点跟踪 AppViewScreen
                trackAppViewScreen(activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                mCurrentActivity = new WeakReference<>(activity);
                countDownTimer.start();
                mDatabaseHelper.commitAppPausedTime(System.currentTimeMillis());
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });
    }

    /**
     * 注册 App Start监听
     * <p>
     * registerContentObserver(uri, notifyForDescendants)： 为指定的Uri注册一个ContentObserver派生类实例，当给定的Uri发生改变时，回调该实例对象去处理。
     * uri                    需要观察的Uri(需要在UriMatcher里注册，否则该Uri也没有意义了)
     * notifyForDescendants   为false 表示精确匹配，即只匹配该Uri；为true 表示可以同时匹配其派生的Uri
     * observer               ContentObserver的派生类实例
     *
     * <p>
     * ContentObserver - 内容观察者
     * 目的：观察（捕捉）特定 Uri 引起的数据库的变化，继而可以做相应的处理，比较类似于数据库技术中的触发器
     *
     * @param application 整个应用对象
     */
    public static void registerActivityStateObserver(Application application) {
        application.getContentResolver().registerContentObserver(mDatabaseHelper.getAppStartUri(),
                false,
                new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        // 如果数据库检索到的 app_start_uri 状态不变，则取消计时器，否则计时不准
                        if (mDatabaseHelper.getAppStartUri().equals(uri)) {
                            countDownTimer.cancel();
                        }
                    }
                });
    }

    /**
     * Track 页面浏览事件
     *
     * @param activity 页面上下文环境
     */
    @Keep
    private static void trackAppViewScreen(Activity activity) {
        try {
            if (activity == null) {
                return;
            }
            if (mIgnoredActivities.contains(activity.getClass().getCanonicalName())) {
                return;
            }

            JSONObject properties = new JSONObject();
            properties.put("$activity", activity.getClass().getCanonicalName());
            properties.put("title", getActivityTitle(activity));
            TufusiDataApi.getInstance().track("$AppViewScreen", properties);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取页面标题
     */
    @SuppressWarnings("all")
    private static String getActivityTitle(Activity activity) {
        String activityTitle = "";
        if (activity == null || TextUtils.isEmpty(activity.getTitle())) {
            return null;
        }

        try {
            activityTitle = activity.getTitle().toString();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                String toolbarTitle = getToolbarTitle(activity);
                if (!TextUtils.isEmpty(toolbarTitle)) {
                    activityTitle = toolbarTitle;
                }
            }

            if (TextUtils.isEmpty(activityTitle)) {
                PackageManager packageManager = activity.getPackageManager();
                if (packageManager != null) {
                    ActivityInfo activityInfo = packageManager.getActivityInfo(activity.getComponentName(), 0);
                    activityTitle = activityInfo.loadLabel(packageManager).toString();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return activityTitle;
    }

    @TargetApi(11)
    private static String getToolbarTitle(Activity activity) {
        try {
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                if (!TextUtils.isEmpty(actionBar.getTitle())) {
                    return actionBar.getTitle().toString();
                }
            } else {
                if (activity instanceof AppCompatActivity) {
                    AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
                    androidx.appcompat.app.ActionBar supportActionBar = appCompatActivity.getSupportActionBar();
                    if (supportActionBar != null) {
                        if (supportActionBar.getTitle() != null) {
                            return supportActionBar.getTitle().toString();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 跟踪 APP Activity 启动事件
     *
     * @param activity 当前activity
     */
    private static void trackAppStart(Activity activity) {
        try {
            if (activity == null) {
                return;
            }

            JSONObject properties = new JSONObject();
            properties.put("$activity", activity.getClass().getCanonicalName());
            properties.put("$title", getActivityTitle(activity));
            TufusiDataApi.getInstance().track("$AppStart", properties);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 跟踪 APP Activity 结束事件
     *
     * @param activity 当前activity
     */
    private static void trackAppEnd(Activity activity) {
        try {
            if (activity == null) {
                return;
            }

            JSONObject properties = new JSONObject();
            properties.put("$activity", activity.getClass().getCanonicalName());
            properties.put("$title", getActivityTitle(activity));
            TufusiDataApi.getInstance().track("$AppEnd", properties);
            mDatabaseHelper.commitAppEndEventState(true);
            mCurrentActivity = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 合并JSONObject,规整 Date
     */
    public static void mergeJSONObject(JSONObject source, JSONObject dest) throws JSONException {
        Iterator<String> superPropertiesIterator = source.keys();
        while (superPropertiesIterator.hasNext()) {
            String key = superPropertiesIterator.next();
            Object value = source.get(key);
            if (value instanceof Date) {
                synchronized (DATE_FORMAT) {
                    dest.put(key, DATE_FORMAT.format(value));
                }
            } else {
                dest.put(key, value);
            }
        }
    }

    /**
     * 格式化json字符串，为的是便于日志打印查看
     */
    public static String formatJson(String jsonStr) {
        try {
            if (null == jsonStr || "".equals(jsonStr)) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            // 最末尾字符
            char last;
            // 当前字符
            char current = '\0';
            // 缩进位数
            int indent = 0;
            // 是否包含引号
            boolean isInQuotationMarks = false;

            for (int i = 0; i < jsonStr.length(); i++) {
                last = current;
                current = jsonStr.charAt(i);
                switch (current) {
                    case '"':
                        if (last != '\\') {
                            isInQuotationMarks = !isInQuotationMarks;
                        }
                        sb.append(current);
                        break;
                    case '{':
                    case '[':
                        sb.append(current);
                        if (!isInQuotationMarks) {
                            sb.append('\n');
                            indent++;
                            addIndentBlank(sb, indent);
                        }
                        break;
                    case '}':
                    case ']':
                        if (!isInQuotationMarks) {
                            sb.append('\n');
                            indent--;
                            addIndentBlank(sb, indent);
                        }
                        sb.append(current);
                        break;
                    case ',':
                        sb.append(current);
                        if (last != '\\' && !isInQuotationMarks) {
                            sb.append('\n');
                            addIndentBlank(sb, indent);
                        }
                        break;
                    default:
                        sb.append(current);
                        break;
                }
            }

            return sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * 添加缩进空格位置
     */
    private static void addIndentBlank(StringBuilder sb, int indent) {
        try {
            for (int i = 0; i < indent; i++) {
                sb.append('\t');
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void removeIgnoredActivity(Class<?> activity) {
        if (activity == null) {
            return;
        }

        if (mIgnoredActivities.contains(activity.getCanonicalName())) {
            mIgnoredActivities.remove(activity.getCanonicalName());
        }
    }
}