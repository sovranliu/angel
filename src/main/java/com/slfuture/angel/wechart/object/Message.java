package com.slfuture.angel.wechart.object;

import com.slfuture.angel.wechart.event.MessageReadEvent;
import com.slfuture.angel.wechart.utility.DB;
import com.slfuture.angel.wechart.utility.Render;
import com.slfuture.carrie.base.async.Operator;
import com.slfuture.carrie.base.async.core.IOperation;
import com.slfuture.carrie.base.json.JSONBoolean;
import com.slfuture.carrie.base.json.JSONNumber;
import com.slfuture.carrie.base.json.JSONObject;
import com.slfuture.carrie.base.json.JSONString;
import com.slfuture.carrie.base.json.core.IJSON;
import com.slfuture.carrie.base.text.Text;
import com.slfuture.carrie.base.time.DateTime;
import com.slfuture.carrie.base.type.Record;
import com.slfuture.carrie.base.type.Set;
import com.slfuture.carrie.base.type.Table;
import com.slfuture.carrie.base.type.core.ICollection;
import com.slfuture.carrie.base.type.core.ILink;
import com.slfuture.carrie.utility.template.Context;
import com.slfuture.carrie.world.World;
import com.slfuture.carrie.world.annotation.Method;
import com.slfuture.carrie.world.annotation.Property;
import com.slfuture.carrie.world.relation.Condition;
import com.slfuture.carrie.world.relation.prepare.PropertyPrepare;
import com.slfuture.utility.wechart.message.Pusher;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 用户消息
 */
public class Message {
    /**
     * 数据操作
     */
    public static class DataOperation implements IOperation<Void> {
        /**
         * 执行
         */
        public static void execute() {
            try {
                String sql = "SELECT M.ID, U.OpenID, M.Type, M.UserID, M.Meta, M.AddTime FROM A_Message M JOIN A_User U ON M.UserID = U.ID WHERE M.Delivery = 1 LIMIT 1000";
                ICollection<Record> records = DB.executor().select(sql);
                for(Record record : records) {
                    String meta = record.getString("Meta");
                    if(Text.isBlank(meta)) {
                        continue;
                    }
                    String dataString = Message.get(TEMPLATE_NOTIFY, record.getInteger("ID"), record.getInteger("Type"), record.getString("Meta"), record.getString("OpenID"), record.getDateTime("AddTime"));
                    Pusher.push(dataString);
                    //
                    sql = "UPDATE A_Message SET Delivery = 2 WHERE Delivery = 1 AND ID = " + record.getInteger("ID");
                    DB.executor().alter(sql);
                }
            }
            catch (Exception ex) {
                logger.error("Message.DataOperation.execute() execute failed", ex);
            }
        }

        /**
         * 操作结束回调
         *
         * @return 操作结果
         */
        @Override
        public Void onExecute() {
            while (true) {
                try {
                    Thread.sleep(60 * 1000);
                }
                catch (InterruptedException e) {
                    logger.error("Thread.sleep(?) failed in Message.DataOperation.onExecute()", e);
                    break;
                }
                execute();
            }
            return null;
        }
    }


    /**
     * 消息类型
     */
    public final static int TYPE_APPROVAL_REJECT = 1;
    public final static int TYPE_APPROVAL_PASS = 2;
    public final static int TYPE_NEWBID = 3;
    public final static int TYPE_ACTIVITYONPAY = 4;
    public final static int TYPE_BIDBREAK = 5;
    public final static int TYPE_ACTIVITYDEAL = 6;
    public final static int TYPE_TRANSFER = 7;
    public final static int TYPE_BIDCHOOSE = 8;
    public final static int TYPE_PAY = 9;
    public final static int TYPE_REFUND = 10;
    public final static int TYPE_ACTIVITYBREAK = 11;
    public final static int TYPE_BIDDEAL = 12;
    public final static int TYPE_NEWCHART = 13;
    public final static int TYPE_NEWCOMMENT = 14;
    public final static int TYPE_NEWRESPONSE = 15;
    /**
     * 消息元数据
     */
    public final static String TEMPLATE_TITLE = "TITLE";
    public final static String TEMPLATE_DESCRIPTION = "DESCRIPTION";
    public final static String TEMPLATE_URL = "URL";
    public final static String TEMPLATE_NOTIFY = "NOTIFY";
    public final static String TEMPLATE_PAGE = "PAGE";

    /**
     * 条件集合
     */
    private static Condition CONDITION_ID = null;
    private static Condition CONDITION_USERID = null;
    private static Condition CONDITION_DELIVERY = null;
    /**
     * 日志对象
     */
    private static Logger logger = Logger.getLogger(Message.class);
    /**
     * 后台操作
     */
    private static Operator<Void> operator = null;

    static {
        CONDITION_ID = new Condition();
        CONDITION_ID.prepareSelf = new PropertyPrepare("id");
        CONDITION_USERID = new Condition();
        CONDITION_USERID.prepareSelf = new PropertyPrepare("userId");
        CONDITION_DELIVERY = new Condition();
        CONDITION_DELIVERY.prepareSelf = new PropertyPrepare("delivery");
        // 启动后台刷新
        operator = new Operator<Void>(new DataOperation());
    }

    /**
     * 消息ID
     */
    @Property
    public int id;
    /**
     * 消息类型，1：活动审批驳回,2：活动审批通过,3：新竞拍通知,4：活动确立通知,5：活动违约通知,6：发起者活动成交通知,7：收款通知,8：被选中通知,9:支付成功通知,10:退款通知,11:活动取消通知,12:竞拍者活动成交通知
     */
    @Property
    public int type;
    /**
     * 消息所属用户ID
     */
    @Property
    public int userId;
    /**
     * JSON元数据
     */
    @Property
    public String meta;
    /**
     * 消息状态，1：未读，2：已读，0：销毁
     */
    @Property
    public int status;
    /**
     * 投递状态，1：未投递，2：已投递，0：无需投递
     */
    @Property
    public int delivery;
    /**
     * 消息发生时间
     */
    @Property
    public DateTime addTime;


    /**
     * 获取消息标题
     *
     * @return 消息标题
     */
    @Method
    public String getTitle() {
        String result = get(TEMPLATE_TITLE, type, meta, null);
        if(Text.isBlank(result)) {
            return "";
        }
        return result;
    }

    /**
     * 获取消息描述文字
     *
     * @return 消息描述文字
     */
    @Method
    public String getDescription() {
        String result = get(TEMPLATE_DESCRIPTION, type, meta, null);
        if(Text.isBlank(result)) {
            return "";
        }
        return result;
    }

    /**
     * 获取消息详情页URL
     *
     * @return 消息详情页URL
     */
    @Method
    public String getURL() {
        String result = get(TEMPLATE_URL, type, meta, null);
        if(Text.isBlank(result)) {
            return "";
        }
        return result;
    }

    /**
     * 获取消息页面元素
     *
     * @return 消息页面元素
     */
    @Method
    public String getPage() {
        String result = get(TEMPLATE_PAGE, type, meta, null);
        if(Text.isBlank(result)) {
            return "";
        }
        return result;
    }

    /**
     * 获取消息描述信息
     *
     * @param template 消息模板键
     * @param type 消息类型
     * @param meta 消息内容
     * @param openId 开放ID
     * @return 消息描述信息
     */
    public String get(String template, int type, String meta, String openId) {
        return get(template, id, type, meta, openId, addTime);
    }

    /**
     * 获取消息描述信息
     *
     * @param template 消息模板键
     * @param messageId 消息ID
     * @param type 消息类型
     * @param meta 消息内容
     * @param openId 开放ID
     * @param addTime 消息创建时间
     * @return 消息描述信息
     */
    public static String get(String template, int messageId, int type, String meta, String openId, DateTime addTime) {
        if(Text.isBlank(meta)) {
            return null;
        }
        JSONObject data = JSONObject.convert(meta);
        Context context = new Context();
        context.put("messageId", messageId);
        context.put("messageTime", addTime);
        for(ILink<String, IJSON> link : data) {
            if(IJSON.JSON_TYPE_STRING == link.destination().type()) {
                JSONString jsonString = (JSONString) link.destination();
                context.put(link.origin(), jsonString.getValue());
            }
            else if(IJSON.JSON_TYPE_NUMBER == link.destination().type()) {
                JSONNumber jsonNumber = (JSONNumber) link.destination();
                if(0 == jsonNumber.doubleValue() % 1) {
                    context.put(link.origin(), jsonNumber.intValue());
                }
                else {
                    context.put(link.origin(), jsonNumber.doubleValue());
                }
            }
            else {
                context.put(link.origin(), link.destination().toString());
            }
        }
        if(null != openId) {
            context.put("openId", openId);
        }
        return Render.instance().render(template + "-" + type, context);
    }

    /**
     * 设置读取状态
     *
     * @return 操作结果
     */
    @Method
    public boolean read() {
        if(1 != status) {
            return false;
        }
        String sql = "UPDATE A_Message SET Status = 2 WHERE ID = " + id + " AND Status = 1";
        int result = DB.executor().alter(sql);
        if(1 == result) {
            World.throwEvent(this, new MessageReadEvent(id));
            return true;
        }
        return false;
    }

    /**
     * 设置投递状态
     *
     * @return 操作结果
     */
    public boolean deliver() {
        String sql = "UPDATE A_Message SET Delivery = 2 WHERE ID = " + id + " AND Delivery = 1";
        int result = DB.executor().alter(sql);
        if(1 == result) {
            return true;
        }
        return false;
    }

    /**
     * 根据指定条件查找对象
     *
     * @param condition 条件
     * @return 对象
     */
    public static Message find(Condition condition) {
        Message result = null;
        if(CONDITION_ID.equalsIgnoreTarget(condition)) {
            int id = (Integer) condition.target;
            String sql = "SELECT ID, Type, UserID, Meta, Status, AddTime FROM A_Message WHERE ID = " + id;
            Record record = DB.executor().load(sql);
            if(null == record) {
                return null;
            }
            try {
                result = record.build(Message.class);
            }
            catch (Exception e) { }
        }
        return result;
    }

    /**
     * 根据指定条件查找对象列表
     *
     * @param condition 条件
     * @return 对象列表
     */
    public static ICollection<Message> finds(Condition condition) {
        Set<Message> result = new Set<Message>();
        if(CONDITION_USERID.equalsIgnoreTarget(condition)) {
            int userId = (Integer) condition.target;
            String sql = "SELECT ID, Type, UserID, Meta, Status, AddTime FROM A_Message WHERE Status > 0 AND UserID = " + userId + " ORDER BY AddTime DESC";
            ICollection<Record> records = DB.executor().select(sql);
            for(Record record : records) {
                Message message = null;
                try {
                    message = record.build(Message.class);
                }
                catch (Exception e) { }
                if(message != null) {
                    result.add(message);
                }
            }
        }
        return result;
    }
}
