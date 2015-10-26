package com.slfuture.angel.wechart.event;

import com.slfuture.carrie.base.time.DateTime;
import com.slfuture.carrie.world.IEvent;

/**
 * 竞拍违约事件
 */
public class BidInterruptEvent implements IEvent {
    /**
     * 活动ID
     */
    public int activityId;
    /**
     * 活动标题
     */
    public String activityTitle;
    /**
     * 活动开始时间
     */
    public DateTime startTime;
    /**
     * 活动发起者昵称
     */
    public String mockName;
    /**
     * 竞拍者ID
     */
    public int bidderId;
    /**
     * 竞拍者昵称
     */
    public String bidderNickName;
    /**
     * 竞拍价
     */
    public int amount;
    /**
     * 违约理由
     */
    public String reason;


    /**
     * 构造函数
     */
    public BidInterruptEvent() { }
    public BidInterruptEvent(int activityId, String activityTitle, DateTime startTime, String mockName, int bidderId, String bidderNickName, int amount, String reason) {
        this.activityId = activityId;
        this.activityTitle = activityTitle;
        this.startTime = startTime;
        this.mockName = mockName;
        this.bidderId = bidderId;
        this.bidderNickName = bidderNickName;
        this.amount = amount;
        this.reason = reason;
    }

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    @Override
    public String name() {
        return "BidInterrupt";
    }
}
