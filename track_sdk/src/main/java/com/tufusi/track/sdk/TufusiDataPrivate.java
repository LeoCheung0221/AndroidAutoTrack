package com.tufusi.track.sdk;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;

import com.tufusi.track.sdk.listener.WrapperAdapterViewOnItemClickListener;
import com.tufusi.track.sdk.listener.WrapperAdapterViewOnItemSelectedListener;
import com.tufusi.track.sdk.listener.WrapperExpandOnChildClickListener;
import com.tufusi.track.sdk.listener.WrapperExpandOnGroupClickListener;
import com.tufusi.track.sdk.listener.WrapperOnRatingBarChangeListener;
import com.tufusi.track.sdk.listener.WrapperRadioGroupOnCheckedChangeListener;
import com.tufusi.track.sdk.listener.WrapperOnCheckedChangeListener;
import com.tufusi.track.sdk.listener.WrapperOnClickListener;
import com.tufusi.track.sdk.listener.WrapperOnSeekBarChangeListener;
import com.tufusi.track.sdk.lifecycle.TufusiDatabaseHelper;
import com.tufusi.track.sdk.window.TouchEventHandler;
import com.tufusi.track.sdk.window.WrapperWindowCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
 * @description 数据采集统一处理类
 */
public class TufusiDataPrivate {

    private static final long SESSION_INTERVAL_TIME = 30 * 1000;

    /**
     * 需要忽略跟踪的Activity集合
     */
    private static List<String> mIgnoredActivities;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" + ".SSS", Locale.CHINA);
    private static TufusiDatabaseHelper mDatabaseHelper;
    private static CountDownTimer countDownTimer;
    // 通过 ViewTree观察者对象全局监听点击事件
    private static ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;
    private static TrackClickMode mode;

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
     * 获取activity界面内的根布局
     *
     * @param activity    当前页面对象
     * @param isDecorView 是否是装饰布局
     */
    private static ViewGroup getRootViewFromActivity(Activity activity, boolean isDecorView) {
        if (isDecorView) {
            return (ViewGroup) activity.getWindow().getDecorView();
        } else {
            return activity.findViewById(android.R.id.content);
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
            public void onActivityCreated(@NonNull final Activity activity, @Nullable Bundle savedInstanceState) {
                Log.e("onActivityCreated: 当前模式", TufusiDataApi.getInstance().getTrackClickMode(mode));
                if (mode == TrackClickMode.CUSTOM_LISTENER) {
                    setGlobalListener(activity);
                } else if (mode == TrackClickMode.WINDOW_CALLBACK) {
                    setWindowCallback(activity);
                }
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
                if (mode == TrackClickMode.CUSTOM_LISTENER) {
                    // 在此注册全局点击监听事件 AppClick
                    ViewGroup rootView = getRootViewFromActivity(activity, true);
                    rootView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
                }
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                // 在此统计暂停时长
                mCurrentActivity = new WeakReference<>(activity);
                countDownTimer.start();
                mDatabaseHelper.commitAppPausedTime(System.currentTimeMillis());
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                if (mode == TrackClickMode.CUSTOM_LISTENER) {
                    // 在此移除全局监听点击事件
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                        ViewGroup rootView = getRootViewFromActivity(activity, true);
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
                    }
                }
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });
    }

    private static void setWindowCallback(Activity activity) {
        // 在创建时期监听窗口回调
        Window window = activity.getWindow();
        Window.Callback callback = window.getCallback();
        window.setCallback(new WrapperWindowCallback(activity, callback));
    }

    private static void setGlobalListener(final Activity activity) {
        // 在创建时期开启全局监听
        final ViewGroup rootView = getRootViewFromActivity(activity, true);
        onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 委托监听点击事件
                delegateViewsOnClickListener(activity, rootView);
            }
        };
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
     * 跟踪 APP Activity 启动事件
     *
     * @param activity 当前activity
     */
    @Keep
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
    @Keep
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
     * 跟踪点击事件埋点处理
     *
     * @param view 当前点击view
     */
    @Keep
    public static void trackViewOnClick(View view) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("$element_type", view.getClass().getCanonicalName());
            jsonObject.put("$element_id", getViewId(view));
            jsonObject.put("$element_content", getElementContent(view));

            Activity activity = getActivityFromView(view);
            if (activity != null) {
                jsonObject.put("$activity", activity.getClass().getCanonicalName());
            }

            TufusiDataApi.getInstance().track("$AppClick", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 跟踪点击事件埋点处理
     */
    public static void trackAdapterViewOnClick(AdapterView<?> view, MotionEvent event) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("$element_type", view.getClass().getCanonicalName());
            jsonObject.put("$element_id", getViewId(view));

            int count = view.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = view.getChildAt(i);
                if (TouchEventHandler.isContainView(child, event)) {
                    jsonObject.put("$element_position", String.valueOf(i));
                    jsonObject.put("$element_content", traverseViewContent(new StringBuilder(), child));
                    break;
                }
            }

            Activity activity = getActivityFromView(view);
            if (activity != null) {
                jsonObject.put("$activity", activity.getClass().getCanonicalName());
            }
            TufusiDataApi.getInstance().track("$AppClick", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 跟踪点击事件埋点处理
     *
     * @param parent   父 view
     * @param child    子 view
     * @param position view点击位置
     */
    public static void trackAdapterViewOnClick(AdapterView<?> parent, View child, int position) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("$element_type", parent.getClass().getCanonicalName());
            jsonObject.put("$element_id", getViewId(parent));
            jsonObject.put("$element_position", String.format(Locale.CHINA, "%d", position));

            StringBuilder stringBuilder = new StringBuilder();
            String content = traverseViewContent(stringBuilder, child);
            if (!TextUtils.isEmpty(content)) {
                jsonObject.put("$element_content", content);
            }

            Activity activity = getActivityFromView(parent);
            if (activity != null) {
                jsonObject.put("$activity", activity.getClass().getCanonicalName());
            }

            TufusiDataApi.getInstance().track("$AppClick", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 跟踪点击事件埋点处理
     *
     * @param parent        父 view
     * @param child         子 view
     * @param groupPosition 父view点击位置
     * @param childPosition 子view点击位置
     */
    @Keep
    public static void trackAdapterViewOnClick(AdapterView<?> parent, View child, int groupPosition, int childPosition) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("$element_type", parent.getClass().getCanonicalName());
            jsonObject.put("$element_id", getViewId(parent));

            if (childPosition > -1) {
                jsonObject.put("$element_position", String.format(Locale.CHINA, "%d:%d", groupPosition, childPosition));
            } else {
                jsonObject.put("$element_position", String.format(Locale.CHINA, "%d", groupPosition));
            }

            StringBuilder stringBuilder = new StringBuilder();
            String content = traverseViewContent(stringBuilder, child);
            if (!TextUtils.isEmpty(content)) {
                jsonObject.put("$element_content", content);
            }

            Activity activity = getActivityFromView(parent);
            if (activity != null) {
                jsonObject.put("$activity", activity.getClass().getCanonicalName());
            }

            TufusiDataApi.getInstance().track("$AppClick", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取 View 上显示的文本
     *
     * @param view 当前点击 view
     * @return 返回显示文本
     */
    private static String getElementContent(View view) {
        if (view == null) {
            return null;
        }

        String text = null;
        if (view instanceof ImageView) {
            text = view.getContentDescription().toString();
        } else if (view instanceof Button) {
            text = ((Button) view).getText().toString();
        } else if (view instanceof ActionMenuItemView) {
            text = ((ActionMenuItemView) view).getText().toString();
        } else if (view instanceof TextView) {
            text = ((TextView) view).getText().toString();
        } else if (view instanceof RadioGroup) {
            try {
                RadioGroup radioGroup = (RadioGroup) view;
                Activity activity = getActivityFromView(view);
                if (activity != null) {
                    int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                    RadioButton radioButton = activity.findViewById(checkedRadioButtonId);
                    if (radioButton != null) {
                        text = radioButton.getText().toString();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (view instanceof RatingBar) {
            text = String.valueOf(((RatingBar) view).getRating());
        } else if (view instanceof SeekBar) {
            text = String.valueOf(((SeekBar) view).getProgress());
        } else if (view instanceof ViewGroup) {
            text = traverseViewContent(new StringBuilder(), view);
        }
        return text;
    }

    /**
     * 遍历视图内容并返回
     *
     * @param stringBuilder 字符串构建器
     * @param root          遍历视图对象
     * @return 返回遍历拼接内容
     */
    private static String traverseViewContent(StringBuilder stringBuilder, View root) {
        try {
            if (root == null) {
                return stringBuilder.toString();
            }

            if (root instanceof ViewGroup) {
                int childCount = ((ViewGroup) root).getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = ((ViewGroup) root).getChildAt(i);

                    if (child.getVisibility() != View.VISIBLE) {
                        continue;
                    }
                    if (child instanceof ViewGroup) {
                        traverseViewContent(stringBuilder, child);
                    } else {
                        String content = getElementContent(child);
                        if (!TextUtils.isEmpty(content)) {
                            stringBuilder.append(content);
                        }
                    }
                }
            } else {
                stringBuilder.append(getElementContent(root));
            }
            return stringBuilder.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return stringBuilder.toString();
        }
    }

    /**
     * 获取View 所属 Activity
     *
     * @param view 附属activity的view
     * @return 返回所属Activity
     */
    private static Activity getActivityFromView(View view) {
        Activity activity = null;
        if (view == null) {
            return null;
        }

        try {
            Context context = view.getContext();
            if (context != null) {
                if (context instanceof Activity) {
                    activity = (Activity) context;
                } else if (context instanceof ContextWrapper) {
                    while (!(context instanceof Activity) && context instanceof ContextWrapper) {
                        // 往父类遍历上下文环境，找寻最终依附的上下文环境对象
                        context = ((ContextWrapper) context).getBaseContext();
                    }
                    if (context instanceof Activity) {
                        activity = (Activity) context;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return activity;
    }

    /**
     * 获取 View 的 android:id 对应的字符串
     *
     * @param view 点击view
     * @return id字符串
     */
    private static String getViewId(View view) {
        String idString = null;
        try {
            if (view.getId() != View.NO_ID) {
                idString = view.getContext().getResources().getResourceEntryName(view.getId());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return idString;
    }

    /**
     * 委托监听view点击事件监听
     *
     * @param context 当前页面对象
     * @param view    根布局，用于区分是否是activity/fragment还是dialog
     */
    protected static void delegateViewsOnClickListener(Context context, View view) {
        if (context == null || view == null) {
            return;
        }

        if (view instanceof AdapterView) {
            if (view instanceof Spinner) {
                AdapterView.OnItemSelectedListener onItemSelectedListener =
                        ((Spinner) view).getOnItemSelectedListener();

                if (onItemSelectedListener != null && !(onItemSelectedListener instanceof WrapperAdapterViewOnItemSelectedListener)) {
                    ((Spinner) view).setOnItemSelectedListener(new WrapperAdapterViewOnItemSelectedListener(onItemSelectedListener));
                }
            } else if (view instanceof ExpandableListView) {
                ExpandableListView.OnGroupClickListener onGroupClickListener =
                        getExpandableOnGroupClickListener(view);
                if (onGroupClickListener != null && !(onGroupClickListener instanceof WrapperExpandOnGroupClickListener)) {
                    ((ExpandableListView) view).setOnGroupClickListener(new WrapperExpandOnGroupClickListener(onGroupClickListener));
                }

                ExpandableListView.OnChildClickListener onChildClickListener =
                        getExpandableOnChildClickListener(view);
                if (onChildClickListener != null && !(onChildClickListener instanceof WrapperExpandOnChildClickListener)) {
                    ((ExpandableListView) view).setOnChildClickListener(new WrapperExpandOnChildClickListener(onChildClickListener));
                }
            } else if (view instanceof ListView || view instanceof GridView) {
                AdapterView.OnItemClickListener onItemClickListener =
                        ((AbsListView) view).getOnItemClickListener();

                if (onItemClickListener != null && !(onItemClickListener instanceof WrapperAdapterViewOnItemClickListener)) {
                    ((AbsListView) view).setOnItemClickListener(new WrapperAdapterViewOnItemClickListener(onItemClickListener));
                }
            }
        } else {
            // 获取当前 View 设置的 OnClickListener，交由我们自己来封装处理埋点
            final View.OnClickListener listener = getOnClickListener(view);

            // 判断设置的监听器类型，如果是自定义的 WrapperOnClickListener，说明已经被hook过，此处要防止重复hook
            if (listener != null && !(listener instanceof WrapperOnClickListener)) {
                view.setOnClickListener(new WrapperOnClickListener(listener));

            } else if (view instanceof CompoundButton) {
                CompoundButton.OnCheckedChangeListener onCheckedChangeListener
                        = getOnCheckedChangeListener(view);

                if (onCheckedChangeListener != null && !(onCheckedChangeListener instanceof WrapperOnCheckedChangeListener)) {
                    ((CompoundButton) view).setOnCheckedChangeListener(new WrapperOnCheckedChangeListener(onCheckedChangeListener));
                }
            } else if (view instanceof RadioGroup) {
                RadioGroup.OnCheckedChangeListener onCheckedChangeListener =
                        getRadioGroupOnCheckedChangeListener(view);

                if (onCheckedChangeListener != null && !(onCheckedChangeListener instanceof WrapperRadioGroupOnCheckedChangeListener)) {
                    ((RadioGroup) view).setOnCheckedChangeListener(new WrapperRadioGroupOnCheckedChangeListener(onCheckedChangeListener));
                }
            } else if (view instanceof RatingBar) {
                RatingBar.OnRatingBarChangeListener onRatingBarChangeListener =
                        getOnRatingBarChangeListener(view);

                if (onRatingBarChangeListener != null && !(onRatingBarChangeListener instanceof WrapperOnRatingBarChangeListener)) {
                    ((RatingBar) view).setOnRatingBarChangeListener(new WrapperOnRatingBarChangeListener(onRatingBarChangeListener));
                }
            } else if (view instanceof SeekBar) {
                SeekBar.OnSeekBarChangeListener onSeekBarChangeListener =
                        getOnSeekBarChangeListener(view);

                if (onSeekBarChangeListener != null && !(onSeekBarChangeListener instanceof WrapperOnSeekBarChangeListener)) {
                    ((SeekBar) view).setOnSeekBarChangeListener(new WrapperOnSeekBarChangeListener(onSeekBarChangeListener));
                }
            }
        }

        // 如果是 ViewGroup ，需要递归遍历子 View，并 hook
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            if (childCount > 0) {
                for (int i = 0; i < childCount; i++) {
                    View child = viewGroup.getChildAt(i);
                    delegateViewsOnClickListener(context, child);
                }
            }
        }
    }

    /**
     * 获取 ExpandableListView 设置的 OnGroupClickListener
     *
     * @param view 指定的view
     * @return 返回监听器事件
     */
    public static ExpandableListView.OnGroupClickListener getExpandableOnGroupClickListener(View view) {
        try {
            Class<?> expandClazz = Class.forName("android.widget.ExpandableListView");
            Field mOnGroupClickListener = expandClazz.getDeclaredField("mOnGroupClickListener");
            if (!mOnGroupClickListener.isAccessible()) {
                mOnGroupClickListener.setAccessible(true);
            }
            return (ExpandableListView.OnGroupClickListener) mOnGroupClickListener.get(view);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 ExpandableListView 设置的 OnChildClickListener
     *
     * @param view 指定的view
     * @return 返回监听器事件
     */
    public static ExpandableListView.OnChildClickListener getExpandableOnChildClickListener(View view) {
        try {
            Class<?> expandClazz = Class.forName("android.widget.ExpandableListView");
            Field mOnChildClickListener = expandClazz.getDeclaredField("mOnChildClickListener");
            if (!mOnChildClickListener.isAccessible()) {
                mOnChildClickListener.setAccessible(true);
            }
            return (ExpandableListView.OnChildClickListener) mOnChildClickListener.get(view);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 RatingBar 设置的 OnRatingBarChangeListener
     *
     * @param view 指定的view
     * @return 返回监听器事件
     */
    private static RatingBar.OnRatingBarChangeListener getOnRatingBarChangeListener(View view) {
        try {
            Class<?> ratingBarClazz = Class.forName("android.widget.RatingBar");
            Field mOnRatingBarChangeListener = ratingBarClazz.getDeclaredField("mOnRatingBarChangeListener");
            if (!mOnRatingBarChangeListener.isAccessible()) {
                mOnRatingBarChangeListener.setAccessible(true);
            }
            return (RatingBar.OnRatingBarChangeListener) mOnRatingBarChangeListener.get(view);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 SeekBar 设置的 OnSeekBarChangeListener
     *
     * @param view 指定的view
     * @return 返回监听器事件
     */
    private static SeekBar.OnSeekBarChangeListener getOnSeekBarChangeListener(View view) {
        try {
            Class<?> seekClazz = Class.forName("android.widget.SeekBar");
            Field mOnSeekBarChangeListener = seekClazz.getDeclaredField("mOnSeekBarChangeListener");
            if (!mOnSeekBarChangeListener.isAccessible()) {
                mOnSeekBarChangeListener.setAccessible(true);
            }
            return (SeekBar.OnSeekBarChangeListener) mOnSeekBarChangeListener.get(view);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 RadioGroup 设置的 OnCheckedChangeListener
     *
     * @param view 指定的view
     * @return 返回监听器事件
     */
    private static RadioGroup.OnCheckedChangeListener getRadioGroupOnCheckedChangeListener(View view) {
        try {
            Class<?> radioClazz = Class.forName("android.widget.RadioGroup");
            Field mOnCheckedChangeListener = radioClazz.getDeclaredField("mOnCheckedChangeListener");
            if (!mOnCheckedChangeListener.isAccessible()) {
                mOnCheckedChangeListener.setAccessible(true);
            }
            return (RadioGroup.OnCheckedChangeListener) mOnCheckedChangeListener.get(view);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 CheckBox 设置的 OnCheckedChangeListener
     *
     * @param view 指定的view
     * @return 监听器事件
     */
    private static CompoundButton.OnCheckedChangeListener getOnCheckedChangeListener(View view) {
        try {
            Class<?> buttonClazz = Class.forName("android.widget.CompoundButton");
            Field mOnCheckedChangeListenerField = buttonClazz.getDeclaredField("mOnCheckedChangeListener");
            if (!mOnCheckedChangeListenerField.isAccessible()) {
                mOnCheckedChangeListenerField.setAccessible(true);
            }
            return (CompoundButton.OnCheckedChangeListener) mOnCheckedChangeListenerField.get(view);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前View设置的点击监听器
     *
     * @param view 指定 view
     */
    @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
    @TargetApi(15)
    private static View.OnClickListener getOnClickListener(View view) {
        boolean hasOnClick = view.hasOnClickListeners();
        if (hasOnClick) {
            try {
                // 通过反射拿取View类，获取申明的 getListenerInfo Method，继而拿到 mOnClickListener 字段
                Class<?> viewClazz = Class.forName("android.view.View");
                // 反射获取方法 ListenerInfo getListenerInfo(){}
                Method listenerInfoMethod = viewClazz.getDeclaredMethod("getListenerInfo");
                // 开启方法的安全访问权限，其实这里可以直接开启，并不是public就一定可以直接访问，有时也必须强制开启
                if (!listenerInfoMethod.isAccessible()) {
                    // 取消 Java 的权限控制检查，所以即便是 public方法，其accessible属性默认也是 false
                    listenerInfoMethod.setAccessible(true);
                }
                // 反射委托方法，这里第一个参数传递当前方法需设置的执行对象，由于是无参方法，所以不需要传递参数
                // 获取 ListenerInfo 对象
                Object listenerInfoObj = listenerInfoMethod.invoke(view);
                // 获取静态内部类 ListenerInfo
                Class<?> listenerInfoClazz = Class.forName("android.view.View$ListenerInfo");
                // 获取设置的点击事件监听器字段
                Field onClickListenerField = listenerInfoClazz.getDeclaredField("mOnClickListener");
                if (!onClickListenerField.isAccessible()) {
                    onClickListenerField.setAccessible(true);
                }
                // 返回事件监听器：反射监听器信息对象拿取
                return (View.OnClickListener) onClickListenerField.get(listenerInfoObj);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return null;
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

    public static void setTrackClickMode(TrackClickMode mode) {
        TufusiDataPrivate.mode = mode;
    }

}