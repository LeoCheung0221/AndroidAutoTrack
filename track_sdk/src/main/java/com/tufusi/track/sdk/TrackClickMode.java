package com.tufusi.track.sdk;

/**
 * Created by LeoCheung on 2020/11/25.
 *
 * @description 介绍不同模式埋点方案
 *
 * <p>
 * {@link com.tufusi.track.sdk.TrackClickMode.CUSTOM_LISTENER}
 * 监听全局 ViewTreeObserver.OnGlobalLayoutListener 监听事件
 * 缺点：
 * ① 由于反射导致效率运行低下，会影响APP整体性能，也可能引起兼容性方面问题
 * ② Application.ActivityLifecycleCallbacks 要求 API 14+
 * ③ View.hasOnClickListeners() 要求 API 15+
 * ④ removeOnGlobalLayoutListener 要求 API 16+
 * ⑤ 无法直接支持采集游离于 Activity 之上的 View 的点击，比如 Dialog、PopupWindow 等等。
 *
 * <p>
 * {@link com.tufusi.track.sdk.TrackClickMode.WINDOW_CALLBACK}
 * 监听 WindowCallback 代理其分发事件方法实现埋点监控
 * 缺点：
 * ① 由于每次点击均需要遍历RootView，效率相对较低，且程序的整体性能较差
 * ② View.hasOnClickListeners() 要求 API 15+
 * ③ Application.ActivityLifecycleCallbacks 要求 API 14+
 * ④ 对于 Jetpack 框架 Navigation导航来说，这里面的跳转是监控不到的，很致命 ☆
 * ⑤ 无法直接支持采集游离于 Activity 之上的 View 的点击，比如 Dialog、PopupWindow 等等。
 *
 * <p>
 * {@link com.tufusi.track.sdk.TrackClickMode.ACCESSIBILITY_DELEGATE}
 * 监听 View.AccessibilityDelegate 代理performClick的发送辅助功能事件的回调实现埋点监控
 * 缺点：
 * ① Application.ActivityLifecycleCallbacks 要求 API 14+
 * ② View.hasOnClickListeners() 要求 API 15+
 * ③ removeOnGlobalLayoutListener 要求 API 16+
 * ④ 由于反射导致效率运行低下，会影响APP整体性能，也可能引起兼容性方面问题
 * ⑤ 无法采集 Dialog、PopupWindow 等游离于 Activity 之外的控件点击事件
 * ⑥ 辅助功能需要用户手动开启，在部分 Android ROM 上辅助功能可能会失效。
 *
 * <p>
 * {@link com.tufusi.track.sdk.TrackClickMode.TRANSPARENT_LAYOUT}
 * 监听 任意界面透明层点击事件的回调实现埋点监控
 * 缺点：
 * ① Application.ActivityLifecycleCallbacks 要求 API 14+
 * ② View.hasOnClickListeners() 要求 API 15+
 * ③ 无法采集 Dialog、PopupWindow 等游离于 Activity 之外的控件点击事件
 * ④ 每次点击均需遍历一次根布局View，效率低下
 *
 * <p>
 * {@link com.tufusi.track.sdk.TrackClickMode.ASPECT_J}
 * 利用AOP编程，横向切面监听点击事件回调 实现埋点监控
 * 缺点：
 * ① 无法植入第三方库进行监控
 * ② 由于自定义的切点依赖编程语言，目前无法兼容 Lambda语法表达式
 * ③ 会有兼容问题，比如：D8、Gradle 4.x等，这跟asj库有关
 */
public enum TrackClickMode {

    /**
     * 关闭点击埋点
     */
    NONE(-1),

    /**
     * 自定义监听器埋点模式
     */
    CUSTOM_LISTENER(0),

    /**
     * 监听窗口回调埋点模式
     */
    WINDOW_CALLBACK(1),

    /**
     * 检测View属性变化埋点模式
     */
    ACCESSIBILITY_DELEGATE(2),

    /**
     * 通过透明层，处理 OnTouchEvent 开启埋点
     */
    TRANSPARENT_LAYOUT(3),

    /**
     * AOP编程 AspectJ框架接入埋点
     */
    ASPECT_J(4),

    /**
     * 字节码技术埋点
     */
    ASM_TRANSFORM(5);

    TrackClickMode(int mode) {
        this.mode = mode;
    }

    private int mode;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return this.ordinal() + ":" + this.mode;
    }
}
