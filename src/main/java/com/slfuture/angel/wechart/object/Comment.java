package com.slfuture.angel.wechart.object;

import com.slfuture.angel.wechart.structure.CommmentInfo;
import com.slfuture.angel.wechart.utility.DB;
import com.slfuture.carrie.base.text.Text;
import com.slfuture.carrie.base.type.Record;
import com.slfuture.carrie.base.type.Set;
import com.slfuture.carrie.base.type.Table;
import com.slfuture.carrie.base.type.core.ICollection;
import com.slfuture.carrie.base.type.core.ITable;
import com.slfuture.carrie.world.IObject;
import com.slfuture.carrie.world.World;
import com.slfuture.carrie.world.annotation.Method;
import com.slfuture.carrie.world.annotation.Property;
import com.slfuture.carrie.world.relation.Condition;
import com.slfuture.carrie.world.relation.prepare.PropertyPrepare;
import com.slfuture.utility.aliyun.OCS;
import org.apache.log4j.Logger;

/**
 * 评论
 */
public class Comment {
    /**
     * 日志对象
     */
    private static Logger logger = Logger.getLogger(Comment.class);
    /**
     * 热门评论条数
     */
    public final static int COMMENT_HOT_COUNT = 2;
    /**
     * 评论单页条数
     */
    public final static int COMMENT_PAGE_SIZE = 10;


    /**
     * 条件集合
     */
    private static Condition CONDITION_ACTIVITYID = null;

    static {
        CONDITION_ACTIVITYID = new Condition();
        CONDITION_ACTIVITYID.prepareSelf = new PropertyPrepare("activityId");
    }

    /**
     * 活动ID
     */
    @Property
    public int activityId;


    /**
     * 获取最后的评价信息
     *
     * @return 最后的评价信息
     */
    @Method
    public CommmentInfo[] hotCommentInfo() {
        CommmentInfo[] result = (CommmentInfo[]) OCS.get("HotComment", String.valueOf(activityId));
        if(null != result) {
            return result;
        }
        IObject activity = World.relative(this, "activity", IObject.class);
        String sql = "SELECT C.*, U.NickName, U.Photos FROM A_Comment C, A_User U WHERE C.UserID = U.ID AND ActivityID = " + activityId + " ORDER BY C.ID DESC LIMIT " + COMMENT_HOT_COUNT;
        ICollection<Record> records = DB.executor().select(sql);
        result = new CommmentInfo[records.size()];
        int i = 0;
        for(Record record : records) {
            try {
                result[i] = record.build(CommmentInfo.class);
                if((int) (Integer) activity.property("angelId") == result[i].userId) {
                    result[i].nickName = (String) activity.property("mockName");
                    com.slfuture.carrie.base.model.Method method = new com.slfuture.carrie.base.model.Method();
                    method.name = "primaryPhoto";
                    method.parameters = new Class<?>[0];
                    result[i].photo = (String) activity.invoke(method);
                }
                else {
                    result[i].photo = Text.substring(record.getString("Photos") + ",", null, ",");
                }
                i++;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        OCS.set("HotComment", String.valueOf(activityId), 60 * 10, result);
        return result;
    }

    /**
     * 获取最后的评价信息
     *
     * @param page 页面索引
     * @return 最后的评价信息
     */
    @Method
    public CommmentInfo[] searchCommentInfo(int page) {
        IObject activity = World.relative(this, "activity", IObject.class);
        String sql = "SELECT C.*, U.NickName, U.Photos FROM A_Comment C, A_User U WHERE C.UserID = U.ID AND ActivityID = " + activityId + " ORDER BY C.ID ASC LIMIT " + ((page - 1) * COMMENT_PAGE_SIZE) + ", " + COMMENT_PAGE_SIZE;
        ICollection<Record> records = DB.executor().select(sql);
        CommmentInfo[] result = new CommmentInfo[records.size()];
        int i = 0;
        for(Record record : records) {
            try {
                result[i] = record.build(CommmentInfo.class);
                if((int) (Integer) activity.property("angelId") == result[i].userId) {
                    result[i].nickName = (String) activity.property("mockName");
                    com.slfuture.carrie.base.model.Method method = new com.slfuture.carrie.base.model.Method();
                    method.name = "primaryPhoto";
                    method.parameters = new Class<?>[0];
                    result[i].photo = (String) activity.invoke(method);
                }
                else {
                    result[i].photo = Text.substring(record.getString("Photos") + ",", null, ",");
                }
                i++;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * 评论
     *
     * @param userId 评论者
     * @param content 评论内容
     * @param anonymous 是否匿名
     */
    @Method
    public boolean submit(int userId, String content, boolean anonymous) {
        Integer count = (Integer) OCS.get("CommentSubmit", activityId + "-" + userId);
        if(null == count) {
            count = 0;
        }
        else if(count > 2) {
            return false;
        }
        String sql = "INSERT INTO A_Comment (ActivityID, UserID, Content, Anonymous, Response, AddTime, UpdateTime) VALUES (" + activityId + ", " + userId + ", '" + content + "', " + anonymous + ", NULL, NOW(), NOW())";
        DB.executor().alter(sql);
        count++;
        OCS.set("CommentSubmit", activityId + "-" + userId, 60, count);
        //
        IObject activity = World.relative(this, "activity", IObject.class);
        IObject angel = activity.relative("angel", IObject.class);
        com.slfuture.carrie.base.model.Method method = new com.slfuture.carrie.base.model.Method();
        method.name = "notify";
        method.parameters = new Class<?>[3];
        method.parameters[0] = int.class;
        method.parameters[1] = ITable.class;
        method.parameters[2] = int.class;
        ITable<String, Object> meta = new Table<String, Object>();
        meta.put("activityId", activity.property("id"));
        meta.put("activityTitle", activity.property("title"));
        angel.invoke(method, Message.TYPE_NEWCOMMENT, meta, (int) 0);
        OCS.set("HotComment", String.valueOf(activityId), 60 * 10, null);
        return true;
    }

    /**
     * 回复指定的评论
     *
     * @param commentInfoId 评论ID
     * @param content 评论内容
     */
    @Method
    public void response(int commentInfoId, String content) {
        Record record = DB.executor().load("SELECT * FROM A_Comment WHERE ID = " + commentInfoId);
        if(null == record) {
            return;
        }
        int userId = record.getInteger("UserID");
        int activityId = record.getInteger("ActivityID");
        String sql = "UPDATE A_Comment SET Response = '" + content + "' WHERE ID = " + commentInfoId;
        DB.executor().alter(sql);
        //
        IObject activityDetail = World.get("ActivityDetail", activityId, IObject.class);
        IObject user = World.get("User", userId, IObject.class);
        com.slfuture.carrie.base.model.Method method = new com.slfuture.carrie.base.model.Method();
        method.name = "notify";
        method.parameters = new Class<?>[3];
        method.parameters[0] = int.class;
        method.parameters[1] = ITable.class;
        method.parameters[2] = int.class;
        ITable<String, Object> meta = new Table<String, Object>();
        meta.put("activityId", activityDetail.property("id"));
        meta.put("activityTitle", activityDetail.property("title"));
        user.invoke(method, Message.TYPE_NEWRESPONSE, meta, (int) 0);
        OCS.set("HotComment", String.valueOf(activityId), 60 * 10, null);
    }

    /**
     * 删除指定的评论
     *
     * @param userId 活动主ID
     * @param commentInfoId 评论ID
     */
    @Method
    public void remove(int userId, int commentInfoId) {
        Record record = DB.executor().load("SELECT * FROM A_Comment WHERE ID = " + commentInfoId);
        if(null == record) {
            return;
        }
        int activityId = record.getInteger("ActivityID");
        IObject activityDetail = World.get("ActivityDetail", activityId, IObject.class);
        if(null == activityDetail || userId != (int) (Integer) activityDetail.property("angelId")) {
            return;
        }
        String sql = "DELETE FROM A_Comment WHERE ID = " + commentInfoId;
        DB.executor().alter(sql);
        OCS.set("HotComment", String.valueOf(activityId), 60 * 10, null);
    }

    /**
     * 根据指定条件查找对象
     *
     * @param condition 条件
     * @return 对象
     */
    public static Comment find(Condition condition) {
        if(CONDITION_ACTIVITYID.equalsIgnoreTarget(condition)) {
            Comment result = new Comment();
            result.activityId = (Integer) condition.target;
            return result;
        }
        else {
            return new Comment();
        }
    }

    /**
     * 根据指定条件查找对象列表
     *
     * @param condition 条件
     * @return 对象列表
     */
    public static ICollection<Comment> finds(Condition condition) {
        return new Set<Comment>(find(condition));
    }
}
