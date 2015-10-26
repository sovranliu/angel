package com.slfuture.angel.wechart.event;

import com.slfuture.carrie.world.IEvent;

/**
 * 投放撤销事件
 */
public class ActivityGiveupEvent implements IEvent {
    /**
     * 活动ID
     */
    public int activityId;
    /**
     * 女神ID
     */
    public int angelId;


    /**
     * 构造函数
     */
    public ActivityGiveupEvent() { }
    public ActivityGiveupEvent(int activityId, int angelId) {
        this.activityId = activityId;
        this.angelId = angelId;
    }

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    @Override
    public String name() {
        return "ActivityGiveup";
    }
}
