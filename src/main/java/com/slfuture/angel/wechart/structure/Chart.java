package com.slfuture.angel.wechart.structure;

import com.slfuture.carrie.base.time.DateTime;
import com.slfuture.carrie.base.type.Record;

/**
 * 对话
 */
public class Chart {
    /**
     * 对话ID
     */
    private int id;
    /**
     * 用户ID
     */
    private int userId;
    /**
     * 聊天内容
     */
    private String content;
    /**
     * 对话时间
     */
    private DateTime time;


    /**
     * 构建对象
     *
     * @param record 记录对象
     * @return 对象
     */
    public static Chart build(Record record) {
        if(null == record) {
            return null;
        }
        Chart result = new Chart();
        result.setId(record.getInteger("ID"));
        result.setUserId(record.getInteger("UserID"));
        result.setContent(record.getString("Content"));
        result.setTime(record.getDateTime("AddTime"));
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
    public int userId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public DateTime time() {
        return time;
    }
    public void setTime(DateTime time) {
        this.time = time;
    }
    public String content() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}
