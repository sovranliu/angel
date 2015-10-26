package com.slfuture.angel.wechart.event;

import com.slfuture.carrie.world.IEvent;

/**
 * 消息被阅读事件
 */
public class MessageReadEvent implements IEvent {
    /**
     * 消息ID
     */
    public int messageId;


    /**
     * 构造函数
     */
    public MessageReadEvent() { }
    public MessageReadEvent(int messageId) {
        this.messageId = messageId;
    }

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    @Override
    public String name() {
        return "MessageRead";
    }
}
