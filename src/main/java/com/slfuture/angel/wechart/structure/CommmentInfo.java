package com.slfuture.angel.wechart.structure;

import com.slfuture.carrie.base.text.Text;
import com.slfuture.carrie.base.time.DateTime;

import java.io.Serializable;

/**
 * 评论信息
 */
public class CommmentInfo implements Serializable {
    /**
     * 评论信息ID
     */
    public int id;
    /**
     * 评论用户ID
     */
    public int userId;
    /**
     * 评论用户昵称
     */
    public String nickName;
    /**
     * 是否匿名
     */
    public boolean anonymous;
    /**
     * 评论用户头像
     */
    public String photo;
    /**
     * 评论信息内容
     */
    public String content;
    /**
     * 评论回复内容
     */
    public String response;
    /**
     * 评论时间
     */
    public DateTime addTime;
    /**
     * 评论回复时间
     */
    public DateTime updateTime;


    public int id() {
        return id;
    }
    public int userId() {
        return userId;
    }
    public String content() {
        return content;
    }
    public String nickName() {
        if(anonymous) {
            return "匿名";
        }
        else {
            return nickName;
        }
    }
    public String photo() {
        if(anonymous) {
            return null;
        }
        if(Text.isBlank(photo)) {
            return null;
        }
        else {
            return photo;
        }
    }
    public String response() {
        return response;
    }
    public DateTime addTime() {
        return addTime;
    }
    public DateTime updateTime() {
        return updateTime;
    }

    public String submitTime() {
        if(null == addTime) {
            return null;
        }
        return addTime.toString("MM-dd HH:mm");
    }
    public String responseTime() {
        if(null == updateTime) {
            return null;
        }
        return updateTime.toString("MM-dd HH:mm");
    }
    public boolean hasResponse() {
        return !Text.isBlank(response);
    }
}
