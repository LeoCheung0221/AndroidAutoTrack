package com.tufusi.track.sdk;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by LeoCheung on 2020/11/23.
 *
 * @description 数据采集数据库辅助类
 */
class TufusiDatabaseHelper {

    private static final String TUFUSI_DATA_CONTENT_PROVIDER = ".TufusiDataContentProvider/";

    public static final String STATE_APP_STARTED = "$app_started";
    public static final String STATE_APP_ENDED = "$app_ended";
    public static final String APP_PAUSED_TIME = "$app_paused_time";

    private Uri mAppStartState;
    private Uri mAppEndState;
    private Uri mAppPausedTime;

    /**
     * 内容处理对象
     * 调用 ContentProvider 提供的接口，对 ContentProvider 中的数据进行添加、删除、修改和查询操作
     */
    private ContentResolver mContentResolver;

    TufusiDatabaseHelper(Context context, String packageName) {
        mContentResolver = context.getContentResolver();
        mAppStartState = Uri.parse("content://" + packageName + TUFUSI_DATA_CONTENT_PROVIDER + TufusiDataTable.STATE_APP_STARTED.getName());
        mAppEndState = Uri.parse("content://" + packageName + TUFUSI_DATA_CONTENT_PROVIDER + TufusiDataTable.STATE_APP_ENDED.getName());
        mAppPausedTime = Uri.parse("content://" + packageName + TUFUSI_DATA_CONTENT_PROVIDER + TufusiDataTable.APP_PAUSED_TIME.getName());
    }

    /**
     * 提交APP Activity 暂停时间戳，并存储值
     *
     * @param pausedTime 当前暂停时间戳
     */
    public void commitAppPausedTime(long pausedTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(APP_PAUSED_TIME, pausedTime);
        mContentResolver.insert(mAppPausedTime, contentValues);
    }

    /**
     * 提交 APP Activity 结束事件标记状态
     *
     * @param appEndState Activity 结束标记
     */
    public void commitAppEndEventState(boolean appEndState) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(STATE_APP_ENDED, appEndState);
        mContentResolver.insert(mAppEndState, contentValues);
    }

    /**
     * 获取App Activity 结束标记
     *
     * @return Activity结束状态
     */
    public boolean getAppEndEventState() {
        boolean state = true;
        Cursor cursor = mContentResolver.query(mAppEndState, new String[]{STATE_APP_ENDED},
                null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                state = cursor.getInt(0) > 0;
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        return state;
    }

    /**
     * 提交 APP 启动开始事件
     *
     * @param appStartState Activity 启动标记
     */
    public void commitAppStartEvent(boolean appStartState) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(STATE_APP_STARTED, appStartState);
        mContentResolver.insert(mAppStartState, contentValues);
    }

    /**
     * 返回 Activity 暂停时间
     *
     * @return Activity 暂停时长
     */
    public long getAppPausedTime() {
        long pausedTime = 0;
        Cursor cursor = mContentResolver.query(mAppPausedTime, new String[]{APP_PAUSED_TIME},
                null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                pausedTime = cursor.getLong(0);
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        return pausedTime;
    }

    public Uri getAppStartUri() {
        return mAppStartState;
    }
}