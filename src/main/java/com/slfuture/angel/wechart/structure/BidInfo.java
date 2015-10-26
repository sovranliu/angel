package com.slfuture.angel.wechart.structure;

import com.slfuture.carrie.base.time.DateTime;
import com.slfuture.carrie.base.type.Record;

/**
 * 竞拍者
 */
public class BidInfo {
    /**
     * 竞拍者状态
     */
    public final static int STATUS_WAIT = 1;
    public final static int STATUS_SELECTED = 2;
    public final static int STATUS_READ = 3;
    public final static int STATUS_CANCEL = 4;

    /**
     * 竞拍ID
     */
    private int id;
    /**
     * 活动ID
     */
    private int activityId;
    /**
     * 竞拍者用户ID
     */
    private int userId;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 头像URL
     */
    private String photo;
    /**
     * 截止时间
     */
    private DateTime expireTime;
    /**
     * 竞价金额
     */
    private int amount;
    /**
     * 状态
     */
    private int status;


    /**
     * 构建竞拍者对象
     *
     * @param record 记录对象
     * @return 竞拍者对象
     */
    public static BidInfo build(Record record) {
        if(null == record) {
            return null;
        }
        BidInfo result = new BidInfo();
        result.setId(record.getInteger("BidID"));
        result.setActivityId(record.getInteger("ActivityID"));
        result.setUserId(record.getInteger("UserID"));
        result.setNickName(record.getString("NickName"));
        result.setPhoto(record.getString("Photo"));
        result.setExpireTime(record.getDateTime("ExpireTime"));
        result.setAmount(record.getInteger("Amount"));
        result.setStatus(record.getInteger("Status"));
        return result;
    }

    /**
     * 属性
     */
    public int id() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int activityId() {
        return activityId;
    }
    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }
    public int userId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String nickName() {
        return nickName;
    }
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    public String photo() {
        return photo;
    }
    public void setPhoto(String photo) {
        this.photo = photo;
    }
    public DateTime expireTime() {
        return expireTime;
    }
    public void setExpireTime(DateTime expireTime) {
        this.expireTime = expireTime;
    }
    public int amount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public int status() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
}
