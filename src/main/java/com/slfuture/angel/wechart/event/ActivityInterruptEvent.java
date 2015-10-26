package com.slfuture.angel.wechart.event;

import com.slfuture.carrie.base.time.DateTime;
import com.slfuture.carrie.world.IEvent;

/**
 * 活动中止事件
 */
public class ActivityInterruptEvent implements IEvent {
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
     * 活动发起者昵称
     */
    public String mockName;
    /**
     * 活动开始时间
     */
    public DateTime startTime;
    /**
     * 活动违约原因
     */
    public String reason;
    /**
     * 竞拍金额
     */
    public int amount;


    /**
     * 构造函数
     */
    public ActivityInterruptEvent() { }
    public ActivityInterruptEvent(int activityId, String activityTitle, int angelId, String mockName, DateTime startTime, String reason, int amount) {
        this.activityId = activityId;
        this.activityTitle = activityTitle;
        this.angelId = angelId;
        this.mockName = mockName;
        this.startTime = startTime;
        this.reason = reason;
        this.amount = amount;
    }

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    @Override
    public String name() {
        return "ActivityInterrupt";
    }
}
