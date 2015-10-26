package com.slfuture.angel.wechart.event;

import com.slfuture.carrie.world.IEvent;

/**
 * 竞拍成交事件
 */
public class BidChooseEvent implements IEvent {
    /**
     * 活动ID
     */
    public int activityId;
    /**
     * 活动标题
     */
    public String activityTitle;
    /**
     * 活动主昵称
     */
    public String mockName;
    /**
     * 竞拍金额
     */
    public int amount;


    /**
     * 构造函数
     */
    public BidChooseEvent() { }
    public BidChooseEvent(int activityId, String activityTitle, String mockName, int amount) {
        this.activityId = activityId;
        this.activityTitle = activityTitle;
        this.mockName = mockName;
        this.amount = amount;
    }

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    @Override
    public String name() {
        return "BidChoose";
    }
}
