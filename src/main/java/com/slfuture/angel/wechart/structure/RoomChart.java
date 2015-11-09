package com.slfuture.angel.wechart.structure;

import com.slfuture.carrie.base.time.DateTime;

/**
 * 聊天室对话
 */
public class RoomChart {
    /**
     * 对话ID
     */
    public int id;
    /**
     * 发言者用户ID
     */
    public int userId;
    /**
     * 发言者昵称
     */
    public String nickName;
    /**
     * 发言者头像
     */
    public String photo;
    /**
     * 聊天内容
     */
    public String content;
    /**
     * 对话时间
     */
    public DateTime time;


    public int id() {
        return id;
    }
    public int userId() {
        return userId;
    }
    public String nickName() {
        return nickName;
    }
    public String photo() {
        return photo;
    }
    public String content() {
        return content;
    }
    public DateTime time() {
        return time;
    }
}
