package com.slfuture.angel.wechart.event;

import com.slfuture.carrie.world.IEvent;

/**
 * 活动取消事件
 */
public class ActivityCancelEvent implements IEvent {
    /**
     * 活动ID
     */
    public int activityId;
    /**
     * 女神ID
     */
    public int angelId;
    /**
     * 竞拍ID
     */
    public int bidId;
    /**
     * 竞拍者ID
     */
    public int bidderId;


    /**
     * 构造函数
     */
    public ActivityCancelEvent() { }
    public ActivityCancelEvent(int activityId, int angelId) {
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
        return "ActivityCancel";
    }
}
