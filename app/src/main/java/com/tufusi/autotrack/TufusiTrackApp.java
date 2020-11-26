package com.tufusi.autotrack;

import android.app.Application;

import com.tufusi.track.sdk.TrackClickMode;
import com.tufusi.track.sdk.TufusiDataApi;

/**
 * Created by LeoCheung on 2020/11/3.
 *
 * @description
 */
public class TufusiTrackApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initTufusiDataApi();
    }

    /**
     * 初始化埋点sdk
     */
    private void initTufusiDataApi() {
        TufusiDataApi.init(this, TrackClickMode.TRANSPARENT_LAYOUT);
    }
}