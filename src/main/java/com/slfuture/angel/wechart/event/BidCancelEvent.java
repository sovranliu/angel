package com.slfuture.angel.wechart.event;

import com.slfuture.carrie.world.IEvent;

/**
 * 竞拍取消事件
 */
public class BidCancelEvent implements IEvent {
    /**
     * 竞拍ID
     */
    public int bidId;
    /**
     * 活动ID
     */
    public int activityId;
    /**
     * 竞拍者ID
     */
    public int userId;


    /**
     * 构造函数
     */
    public BidCancelEvent() { }
    public BidCancelEvent(int bidId, int activityId, int userId) {
        this.bidId = bidId;
        this.activityId = activityId;
        this.userId = userId;
    }

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    @Override
    public String name() {
        return "BidCancel";
    }
}
