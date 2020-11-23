package com.tufusi.track.sdk;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by LeoCheung on 2020/11/23.
 *
 * @description 自定义ContentProvider 对外共享数据
 * <p>
 * 步骤：
 * 1、定义类继承 ContentProvider
 * 2、定义匹配规则 uri
 * 3、通过静态代码块添加匹配规则
 * 4、在 manifest.xml 中配置 contentProvider
 */
public class TufusiDataContentProvider extends ContentProvider {

    /**
     * APP 启动、结束、暂停 规则码
     */
    private static final int STATE_APP_STARTED = 1;
    private static final int STATE_APP_ENDED = 2;
    private static final int APP_PAUSED_TIME = 3;

    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor mEditor;

    private ContentResolver mContentResolver;

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            String packageName = getContext().getPackageName();
            uriMatcher.addURI(packageName + ".TufusiDataContentProvider", TufusiDataTable.STATE_APP_STARTED.getName(), STATE_APP_STARTED);
            uriMatcher.addURI(packageName + ".TufusiDataContentProvider", TufusiDataTable.STATE_APP_ENDED.getName(), STATE_APP_ENDED);
            uriMatcher.addURI(packageName + ".TufusiDataContentProvider", TufusiDataTable.APP_PAUSED_TIME.getName(), APP_PAUSED_TIME);

            sharedPreferences = getContext().getSharedPreferences(packageName + TufusiDataApi.class.getSimpleName(), Context.MODE_PRIVATE);
            mEditor = sharedPreferences.edit();
            mEditor.apply();
            mContentResolver = getContext().getContentResolver();
        }
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int code = uriMatcher.match(uri);
        MatrixCursor matrixCursor = null;
        switch (code) {
            case STATE_APP_STARTED:
                int appStartTag = sharedPreferences.getBoolean(TufusiDatabaseHelper.STATE_APP_STARTED, true) ? 1 : 0;
                matrixCursor = new MatrixCursor(new String[]{TufusiDatabaseHelper.STATE_APP_STARTED});
                matrixCursor.addRow(new Object[]{appStartTag});
                break;
            case STATE_APP_ENDED:
                int appEndTag = sharedPreferences.getBoolean(TufusiDatabaseHelper.STATE_APP_ENDED, true) ? 1 : 0;
                matrixCursor = new MatrixCursor(new String[]{TufusiDatabaseHelper.STATE_APP_ENDED});
                matrixCursor.addRow(new Object[]{appEndTag});
                break;
            case APP_PAUSED_TIME:
                long pausedTime = sharedPreferences.getLong(TufusiDatabaseHelper.APP_PAUSED_TIME, 0);
                matrixCursor = new MatrixCursor(new String[]{TufusiDatabaseHelper.APP_PAUSED_TIME});
                matrixCursor.addRow(new Object[]{pausedTime});
                break;
            default:
                break;
        }
        return matrixCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (values == null) {
            return uri;
        }

        int code = uriMatcher.match(uri);
        switch (code) {
            case STATE_APP_STARTED:
                boolean appStart = values.getAsBoolean(TufusiDatabaseHelper.STATE_APP_STARTED);
                mEditor.putBoolean(TufusiDatabaseHelper.STATE_APP_STARTED, appStart);
                mContentResolver.notifyChange(uri, null);
                break;
            case STATE_APP_ENDED:
                boolean appEnd = values.getAsBoolean(TufusiDatabaseHelper.STATE_APP_ENDED);
                mEditor.putBoolean(TufusiDatabaseHelper.STATE_APP_ENDED, appEnd);
                break;
            case APP_PAUSED_TIME:
                long pausedTime = values.getAsLong(TufusiDatabaseHelper.APP_PAUSED_TIME);
                mEditor.putLong(TufusiDatabaseHelper.APP_PAUSED_TIME, pausedTime);
                break;
            default:
                break;
        }
        mEditor.commit();
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}