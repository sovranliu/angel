package com.slfuture.angel.wechart.event;

import com.slfuture.carrie.world.IEvent;

/**
 * 退款事件
 */
public class RefundEvent implements IEvent {
    /**
     * 活动ID
     */
    public int activityId;
    /**
     * 活动标题
     */
    public String activityTitle;
    /**
     * 金额
     */
    public int amount;
    /**
     * 竞拍ID
     */
    public int bidId;
    /**
     * 竞拍者ID
     */
    public int bidderId;
    /**
     * 退款原因
     */
    public String reason;


    /**
     * 构造函数
     */
    public RefundEvent() { }
    public RefundEvent(int activityId, String activityTitle, int amount, int bidId, int bidderId, String reason) {
        this.activityId = activityId;
        this.activityTitle = activityTitle;
        this.amount = amount;
        this.bidId = bidId;
        this.bidderId = bidderId;
        this.reason = reason;
    }

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    @Override
    public String name() {
        return "Refund";
    }
}
