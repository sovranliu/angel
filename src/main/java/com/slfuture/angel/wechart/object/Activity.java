package com.slfuture.angel.wechart.object;

import com.slfuture.carrie.base.time.DateTime;
import com.slfuture.carrie.base.time.Period;
import com.slfuture.carrie.world.annotation.Method;
import com.slfuture.carrie.world.annotation.Property;

import java.io.Serializable;

/**
 * 活动基类
 */
public class Activity implements Serializable {
    /**
     * 女神
     */
    public final static int TYPE_GIRL = 1;
    /**
     * 男神
     */
    public final static int TYPE_BOY = 2;
    /**
     * 名人
     */
    public final static int TYPE_FAMOUS = 3;


    /**

     * 活动ID
     */
    @Property
    public int id;
    /**
     * 活动类型，1：女神，2：男神，3：名人
     */
    @Property
    public int type;
    /**
     * 活动图片列表
     */
    @Property
    public String photos;
    /**
     * 活动标题
     */
    @Property
    public String title;
    /**
     * 活动内容
     */
    @Property
    public String content;
    /**
     * 活动开始时间
     */
    @Property
    public DateTime startTime;
    /**
     * 城市ID
     */
    @Property
    public int cityId;
    /**
     * 活动区域
     */
    @Property
    public String region;


    /**
     * 获取主图
     *
     * @return 主图URL
     */
    @Method
    public String primaryPhoto() {
        if(null == photos) {
            return null;
        }
        int i = photos.indexOf(",");
        if(-1 == i) {
            return photos;
        }
        return photos.substring(0, i);
    }

    /**
     * 照片集合
     *
     * @return 照片集合
     */
    @Method
    public String[] photos() {
        return photos.split(",");
    }

    /**
     * 活动开始剩余时间
     */
    @Method
    public int remainDays() {
        return new Period(DateTime.now(), startTime).days();
    }
}
