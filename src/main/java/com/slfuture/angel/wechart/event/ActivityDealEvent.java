package com.slfuture.angel.wechart.event;

import com.slfuture.carrie.world.IEvent;

/**
 * 活动成交事件
 */
public class ActivityDealEvent implements IEvent {
    /**
     * 活动ID
     */
    public int activityId;
    /**
     * 活动标题
     */
    public String activityTitle;
    /**
     * 女神ID
     */
    public int angelId;
    /**
     * 女神姓名
     */
    public String angelName;
    /**
     * 女神代号
     */
    public String mockName;
    /**
     * 竞拍ID
     */
    public int bidId;
    /**
     * 竞拍人ID
     */
    public int bidderId;
    /**
     * 竞拍者昵称
     */
    public String bidNickName;
    /**
     * 竞拍金额
     */
    public int amount;
    /**
     * 费用
     */
    public int fee;
    /**
     * 活动内容
     */
    public String content;


    /**
     * 构造函数
     */
    public ActivityDealEvent() { }
    public ActivityDealEvent(int activityId, String activityTitle, int angelId, String angelName, String mockName, int bidId, int bidderId, String bidNickName, int amount, int fee, String content) {
        this.activityId = activityId;
        this.activityTitle = activityTitle;
        this.angelId = angelId;
        this.angelName = angelName;
        this.mockName = mockName;
        this.bidId = bidId;
        this.bidderId = bidderId;
        this.bidNickName = bidNickName;
        this.amount = amount;
        this.fee = fee;
        this.content = content;
    }

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    @Override
    public String name() {
        return "ActivityDeal";
    }
}
