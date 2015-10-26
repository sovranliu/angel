package com.slfuture.angel.wechart.event;

import com.slfuture.carrie.base.time.DateTime;
import com.slfuture.carrie.world.IEvent;

/**
 * 活动被支付事件
 */
public class ActivityPayEvent implements IEvent {
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
     * 女神ID
     */
    public int angelId;
    /**
     * 竞拍者昵称
     */
    public String bidderNickName;
    /**
     * 金额
     */
    public int amount;


    /**
     * 构造函数
     */
    public ActivityPayEvent() { }
    public ActivityPayEvent(int activityId, String activityTitle, DateTime startTime, int angelId, String bidderNickName, int amount) {
        this.activityId = activityId;
        this.activityTitle = activityTitle;
        this.startTime = startTime;
        this.angelId = angelId;
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
        return "ActivityPay";
    }
}
