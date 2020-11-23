package com.tufusi.track.sdk;

/**
 * Created by LeoCheung on 2020/11/23.
 *
 * @description 采集数据表 - 枚举对象
 */
enum TufusiDataTable {

    /**
     * APP 已启动状态
     */
    STATE_APP_STARTED("app_started_state"),

    /**
     * APP 暂停时间
     */
    APP_PAUSED_TIME("app_paused_time"),

    /**
     * APP 已结束状态
     */
    STATE_APP_ENDED("app_ended_state");

    private String name;

    TufusiDataTable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}