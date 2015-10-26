package com.slfuture.angel.wechart.event;

import com.slfuture.carrie.world.IEvent;

/**
 * 活动审批事件
 */
public class ActivityApprovalEvent implements IEvent {
    /**
     * 活动ID
     */
    public int activityId;
    /**
     * 活动标题
     */
    public String activityTitle;
    /**
     * 结果
     */
    public boolean result;
    /**
     * 拒绝原因
     */
    public String reason;


    /**
     * 构造函数
     */
    public ActivityApprovalEvent() { }
    public ActivityApprovalEvent(int activityId, String activityTitle, boolean result, String reason) {
        this.activityId = activityId;
        this.activityTitle = activityTitle;
        this.result = result;
        this.reason = reason;
    }

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    @Override
    public String name() {
        return "ActivityApproval";
    }
}
