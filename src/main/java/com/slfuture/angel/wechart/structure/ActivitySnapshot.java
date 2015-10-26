package com.slfuture.angel.wechart.structure;

import com.slfuture.angel.wechart.object.Activity;

/**
 * 活动快照
 */
public class ActivitySnapshot extends Activity {
    /**
     * 职业
     */
    public String career;
    /**
     * 竞标人数
     */
    public int popularity;


    /**
     * 属性
     */
    public int id() {
        return this.id;
    }
    public int type() {
        return this.type;
    }
    public String title() {
        return this.title;
    }
    public int cityId() {
        return this.cityId;
    }
    public String region() {
        return this.region;
    }
    public String content() {
        return this.content;
    }
    public String career() {
        return this.career;
    }
    public int popularity() {
        return this.popularity;
    }
}
