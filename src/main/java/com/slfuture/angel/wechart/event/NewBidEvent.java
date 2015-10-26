package com.slfuture.angel.wechart.event;

import com.slfuture.carrie.world.IEvent;

/**
 * 竞拍取消事件
 */
public class NewBidEvent implements IEvent {
    /**
     * 活动ID
     */
    public int activityId;
    /**
     * 活动标题
     */
    public String activityTitle;
    /**
     * 竞拍ID
     */
    public int bidId;
    /**
     * 竞拍者昵称
     */
    public String bidderNickName;
    /**
     * 竞拍金额
     */
    public int amount;


    /**
     * 构造函数
     */
    public NewBidEvent() { }
    public NewBidEvent(int activityId, String activityTitle, int bidId, String bidderNickName, int amount) {
        this.activityId = activityId;
        this.activityTitle = activityTitle;
        this.bidId = bidId;
        this.bidderNickName = bidderNickName;
        this.amount = amount;
    }

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    @Override
    public String name() {
        return "NewBid";
    }
}
