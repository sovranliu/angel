package com.slfuture.angel.wechart.event;

import com.slfuture.carrie.world.IEvent;

/**
 * 消息被阅读事件
 */
public class AngelSpeakEvent implements IEvent {
    /**
     * 活动ID
     */
    public int activityId;
    /**
     * 活动标题
     */
    public String activityTitle;


    /**
     * 构造函数
     */
    public AngelSpeakEvent() { }
    public AngelSpeakEvent(int activityId, String activityTitle) {
        this.activityId = activityId;
        this.activityTitle = activityTitle;
    }

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    @Override
    public String name() {
        return "AngelSpeak";
    }
}
