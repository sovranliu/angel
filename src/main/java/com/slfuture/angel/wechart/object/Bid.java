package com.slfuture.angel.wechart.object;

import com.slfuture.angel.wechart.event.*;
import com.slfuture.angel.wechart.structure.OrderResult;
import com.slfuture.angel.wechart.utility.DB;
import com.slfuture.carrie.base.time.DateTime;
import com.slfuture.carrie.base.type.Record;
import com.slfuture.carrie.base.type.Set;
import com.slfuture.carrie.base.type.core.ICollection;
import com.slfuture.carrie.world.IEvent;
import com.slfuture.carrie.world.IObject;
import com.slfuture.carrie.world.World;
import com.slfuture.carrie.world.annotation.Method;
import com.slfuture.carrie.world.annotation.Property;
import com.slfuture.carrie.world.relation.Condition;
import com.slfuture.carrie.world.relation.prepare.PropertyPrepare;
import com.slfuture.utility.wechart.pay.Payment;
import com.slfuture.utility.wechart.pay.structure.RefundResult;
import com.slfuture.utility.wechart.pay.structure.UnifiedOrderResult;
import org.apache.log4j.Logger;

import java.io.Serializable;

/**
 * 竞拍
 */
public class Bid implements Serializable {
    /**
     * 竞拍中
     */
    public final static int STATUS_APPROVAL = 1;
    /**
     * 已撤销
     */
    public final static int STATUS_GIVEUP = 2;
    /**
     * 已中标
     */
    public final static int STATUS_CHOOSE = 3;
    /**
     * 已确认
     */
    public final static int STATUS_CONFIRM = 4;
    /**
     * 已取消
     */
    public final static int STATUS_CANCEL = 5;
    /**
     * 已支付
     */
    public final static int STATUS_PAY = 6;
    /**
     * 已撤销
     */
    public final static int STATUS_REFUND = 7;

    /**
     * 日志对象
     */
    private static Logger logger = Logger.getLogger(Bid.class);


    /**
     * 条件集合
     */
    private static Condition CONDITION_ID = null;
    private static Condition CONDITION_ACTIVITYID = null;
    private static Condition CONDITION_USERID = null;
    private static Condition CONDITION_ACTIVITYIDBYORDER = null;
    private static Condition CONDITION_ACTIVITYIDUSERID = null;

    static {
        CONDITION_ID = new Condition();
        CONDITION_ID.prepareSelf = new PropertyPrepare("id");
        CONDITION_ACTIVITYID = new Condition();
        CONDITION_ACTIVITYID.prepareSelf = new PropertyPrepare("activityId");
        CONDITION_USERID = new Condition();
        CONDITION_USERID.prepareSelf = new PropertyPrepare("userId");
        Condition condition = new Condition();
        condition.prepareSelf = new PropertyPrepare("order");
        CONDITION_ACTIVITYIDBYORDER = new Condition();
        CONDITION_ACTIVITYIDBYORDER.prepareSelf = new PropertyPrepare("activityId");
        CONDITION_ACTIVITYIDBYORDER.put(true, condition);
        condition = new Condition();
        condition.prepareSelf = new PropertyPrepare("userId");
        CONDITION_ACTIVITYIDUSERID = new Condition();
        CONDITION_ACTIVITYIDUSERID.prepareSelf = new PropertyPrepare("activityId");
        CONDITION_ACTIVITYIDUSERID.put(true, condition);
    }


    /**
     * 竞拍ID
     */
    @Property
    public int id;
    /**
     * 活动ID
     */
    @Property
    public int activityId;
    /**
     * 用户ID
     */
    @Property
    public int userId;
    /**
     * 竞拍价
     */
    @Property
    public int amount;
    /**
     * 照片
     */
    @Property
    public String photo;
    /**
     * 截止时间
     */
    @Property
    public DateTime expireTime;
    /**
     * 状态，1：投票，2：被选中，3：已打开，4：已取消，5：已放弃
     */
    @Property
    public int status;
    /**
     * 竞标者信誉度
     */
    private int credit = -1;


    /**
     * 获取竞标者信誉度
     */
    @Method
    public int getCredit() {
        if(-1 != credit) {
            return credit;
        }
        IObject user = World.relative(this, "user", IObject.class);
        if(null == user) {
            return 0;
        }
        return (Integer) user.property("credit");
    }

    /**
     * 撤销竞拍
     *
     * @return 操作结果
     */
    @Method
    public boolean giveup() {
        if(STATUS_APPROVAL != status) {
            return false;
        }
        String sql = "UPDATE A_Bid SET Status = " + STATUS_GIVEUP + " WHERE Status = " + STATUS_APPROVAL + " AND ID = " + id;
        int result = DB.executor().alter(sql);
        if(1 == result) {
            World.throwEvent(this, new BidGiveupEvent(id, activityId, userId));
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * 被选中
     *
     * @return 操作结果，0：成功，-1：竞标已撤销，-2：状态不正确，-3：内部错误
     */
    @Method
    public int choose() {
        if(STATUS_GIVEUP == status) {
            return -1;
        }
        else if(STATUS_APPROVAL != status) {
            return -2;
        }
        IObject activityDetail = World.relative(this, "activity", IObject.class);
        if(null == activityDetail) {
            return -3;
        }
        IObject angel = World.relative(activityDetail, "angel", IObject.class);
        if(null == angel) {
            return -3;
        }
        String sql = "UPDATE A_Bid SET Status = " + STATUS_CHOOSE + " WHERE Status = " + STATUS_APPROVAL + " AND ID = " + id;
        int result = DB.executor().alter(sql);
        if(1 != result) {
            return -3;
        }
        World.throwEvent(this, new BidChooseEvent(activityId, (String) activityDetail.property("title"), (String) activityDetail.property("mockName"), amount));
        return 0;
    }

    /**
     * 确认中标
     */
    @Method
    public void confirm() {
        if(STATUS_CHOOSE != status) {
            return;
        }
        String sql = "UPDATE A_Bid SET Status = " + STATUS_CONFIRM + " WHERE ID = " + id + " AND Status = " + STATUS_CHOOSE;
        DB.executor().alter(sql);
    }

    /**
     * 取消活动
     *
     * @return 执行结果，0：成功，-1：活动不在发布状态无法取消，-2：重复操作，-3：操作失败
     */
    @Method
    public boolean cancel() {
        if(STATUS_CHOOSE != status && STATUS_CONFIRM != status) {
            return false;
        }
        String sql = "UPDATE A_Bid SET Status = " + STATUS_CANCEL + " WHERE Status IN (" + STATUS_CHOOSE + ", " + STATUS_CONFIRM + ") AND ID = " + id;
        int result = DB.executor().alter(sql);
        if(1 == result) {
            World.throwEvent(this, new BidCancelEvent(id, activityId, userId));
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * 是否可以支付
     *
     * @return 操作结果，-1：活动已经不在发布状态，-2：活动无法支付
     */
    @Method
    public int canPay() {
        if(STATUS_CHOOSE != status && STATUS_CONFIRM != status) {
            logger.error("Bid.canPay(" + id + ", " + activityId + ", " + userId + ", " + amount + ", ?, ?, " + status + ", " + credit + ") failed, bid can not pay");
            return -2;
        }
        IObject activityDetail = World.get("ActivityDetail", activityId, IObject.class);
        if(null == activityDetail) {
            logger.error("Bid.canPay(" + id + ", " + activityId + ", " + userId + ", " + amount + ", ?, ?, " + status + ", " + credit + ") failed, activity not found");
            return -2;
        }
        if(ActivityDetail.STATUS_ONLINE != (Integer) activityDetail.property("status")) {
            logger.error("Bid.canPay(" + id + ", " + activityId + ", " + userId + ", " + amount + ", ?, ?, " + status + ", " + credit + ") failed, activity not online");
            return -1;
        }
        return 0;
    }

    /**
     * 下单准备支付
     *
     * @param ip 客户端IP
     * @return 操作结果，-1：活动已经不在发布状态，-2：活动无法支付
     */
    @Method
    public OrderResult order(String ip) {
        IObject bidder = World.relative(this, "user", IObject.class);
        if(null == bidder) {
            return null;
        }
        UnifiedOrderResult unifiedOrderResult = null;
        try {
            unifiedOrderResult = Payment.unifiedOrder(activityId, amount, (String) bidder.property("openId"), ip);
        }
        catch (Exception ex) {
            logger.error("call Payment.unifiedOrder(" + activityId + ", " + amount + ", " + (String) bidder.property("openId") + ", " + ip + ") in Bid.order(" + ip + ") failed");
            return null;
        }
        OrderResult result = new OrderResult();
        result.setCode(unifiedOrderResult.code);
        result.setTradeNo(unifiedOrderResult.tradeNo);
        result.setPrepayId(unifiedOrderResult.prepayId);
        result.setNonceString(unifiedOrderResult.nonceString);
        result.setSignType(unifiedOrderResult.signType);
        result.setTimestamp(unifiedOrderResult.timestamp);
        result.setSignature(unifiedOrderResult.signature);
        return result;
    }

    /**
     * 收款
     *
     * @param amount 支付金额
     * @return 支付结果，0：支付成功，-1：竞标状态不正确，-2：数据状态不正确，-3：活动不存在，-4：支付冲突，-5：内部错误
     */
    @Method
    public int onPay(int amount) {
        logger.info("Bid.pay(" + id + ", " + activityId + ", " + userId + ", " + amount + ", ?, ?, " + status + ", " + credit +  ")");
        if(amount != this.amount) {
            return -5;
        }
        if(STATUS_CHOOSE != status && STATUS_CONFIRM != status && STATUS_CANCEL != status) {
            logger.error("Bid.pay(" + id + ", " + activityId + ", " + userId + ", " + amount + ", ?, ?, " + status + ", " + credit + ") failed, status = " + status);
            refund("竞标状态不正确");
            return -1;
        }
        IObject activityDetail = World.get("ActivityDetail", activityId, IObject.class);
        if(null == activityDetail) {
            logger.error("Bid.pay(" + id + ", " + activityId + ", " + userId + ", " + amount + ", ?, ?, " + status + ", " + credit + ") failed, activity not found");
            refund("活动不存在");
            return -3;
        }
        IObject bidder = World.relative(this, "user", IObject.class);
        if(null == bidder) {
            logger.error("Bid.interrupt(" + id + ", " + activityId + ", " + userId + ", " + amount + ", ?, ?, " + status + ", " + credit + ") failed, bidder not found");
            refund("竞拍者查找失败");
            return -3;
        }
        int result = DB.executor().alter("UPDATE A_Bid SET Status = " + STATUS_PAY + " WHERE Status IN(" + STATUS_CHOOSE + ", " + STATUS_CONFIRM + ", " + STATUS_CANCEL + ") AND ID = " + id);
        if(1 != result) {
            logger.error("Bid.pay(" + id + ", " + activityId + ", " + userId + ", " + amount + ", ?, ?, " + status + ", " + credit + ") failed, result = " + result);
            refund("数据状态不正确");
            return -2;
        }
        status = STATUS_PAY;
        logger.info("Bid.pay(" + id + ", " + activityId + ", " + userId + ", " + amount + ", ?, ?, " + status + ", " + credit +  ") success");
        //
        com.slfuture.carrie.base.model.Method method = new com.slfuture.carrie.base.model.Method();
        method.name = "onPay";
        method.parameters = new Class<?>[2];
        method.parameters[0] = int.class;
        method.parameters[1] = int.class;
        Boolean onPayResult = (Boolean) activityDetail.invoke(method, id, amount);
        if(onPayResult) {
            ActivityPayEvent event = new ActivityPayEvent();
            event.activityId = activityId;
            event.activityTitle = (String) activityDetail.property("title");
            event.startTime = (DateTime) activityDetail.property("startTime");
            event.angelId = (Integer) activityDetail.property("angelId");
            event.bidderNickName = (String) bidder.property("nickName");
            event.amount = amount;
            World.throwEvent(this, event);
            return 0;
        }
        // 支付冲突，执行退款
        refund("活动被其他中标者抢先支付");
        return -4;
    }

    /**
     * 违约
     *
     * @param reason 违约原因
     * @return 操作结果，-1：未支付不可退款，-2：活动不存在，-3：活动不是当前用户竞的标，-4：活动未支付，-5：退款失败
     */
    @Method
    public int interrupt(String reason) {
        if(STATUS_PAY != status) {
            logger.error("Bid.interrupt(" + id + ", " + activityId + ", " + userId + ", " + amount + ", ?, ?, " + status + ", " + credit + ") failed, bid not pay");
            return -1;
        }
        IObject activityDetail = World.get("ActivityDetail", activityId, IObject.class);
        if(null == activityDetail) {
            logger.error("Bid.interrupt(" + id + ", " + activityId + ", " + userId + ", " + amount + ", ?, ?, " + status + ", " + credit + ") failed, activity not found");
            return -2;
        }
        if(id != (Integer) activityDetail.property("bidId")) {
            logger.error("Bid.interrupt(" + id + ", " + activityId + ", " + userId + ", " + amount + ", ?, ?, " + status + ", " + credit + ") failed, activity not bid by this user");
            return -3;
        }
        if(ActivityDetail.STATUS_PAY != (Integer) activityDetail.property("status")) {
            logger.error("Bid.interrupt(" + id + ", " + activityId + ", " + userId + ", " + amount + ", ?, ?, " + status + ", " + credit + ") failed, activity not pay");
            return -4;
        }
        IObject angel = World.relative(activityDetail, "angel", IObject.class);
        if(null == angel) {
            logger.error("Bid.interrupt(" + id + ", " + activityId + ", " + userId + ", " + amount + ", ?, ?, " + status + ", " + credit + ") failed, angel not found");
            return -2;
        }
        IObject bidder = World.relative(this, "user", IObject.class);
        if(null == bidder) {
            logger.error("Bid.interrupt(" + id + ", " + activityId + ", " + userId + ", " + amount + ", ?, ?, " + status + ", " + credit + ") failed, bidder not found");
            return -2;
        }
        String sql = "UPDATE A_Activity SET Status = " + ActivityDetail.STATUS_VIOLATE + ", Memo = '" + reason + "' WHERE Status = " + ActivityDetail.STATUS_PAY + " AND ID = " + activityId;
        int result = DB.executor().alter(sql);
        if(1 != result) {
            return -4;
        }
        World.throwEvent(this, new BidInterruptEvent(activityId, (String) activityDetail.property("title"), (DateTime) activityDetail.property("startTime"), (String) angel.property("nickName"), userId, (String) bidder.property("nickName"), amount, reason));
        if(refund("竞拍者主动退款") >= 0) {
            return 0;
        }
        return -5;
    }

    /**
     * 退款
     *
     * @param reason 原因
     * @return 执行结果，-1：不是支付状态，-2：数据不是支付状态，-3：内部错误
     */
    @Method
    public int refund(String reason) {
        logger.info("Bid.refund(" + id + ", " + activityId + ", " + userId + ", " + amount + ", ?, ?, " + status + ", " + credit +  ")");
        if(STATUS_PAY != status) {
            return -1;
        }
        IObject activity = World.relative(this, "activity", IObject.class);
        if(null == activity) {
            return -3;
        }
        String sql = "UPDATE A_Bid SET Status = " + STATUS_REFUND + " WHERE Status = " + STATUS_PAY + " AND ID = " + id;
        int result = DB.executor().alter(sql);
        if(1 != result) {
            return -2;
        }
        // 调用退款接口
        IObject user = World.relative(this, "user", IObject.class);
        if(null == user) {
            logger.error("user not found in refund");
            return -3;
        }
        try {
            RefundResult refundResult = Payment.refund(activityId, (String) user.property("openId"), amount);
            if(RefundResult.CODE_SUCCESS == refundResult.code) {
                World.throwEvent(this, new RefundEvent(activityId, (String) activity.property("title"), amount, id, userId, reason));
                return 0;
            }
            else {
                logger.error("call Payment.refund(" + activityId + ", " + user.property("openId") + ", " + amount + ") failed, code = " + refundResult.code);
                return -3;
            }
        }
        catch(Exception ex) {
            logger.error("call Payment.refund(" + activityId + ", " + user.property("openId") + ", " + amount + ") failed");
            return -3;
        }
    }

    /**
     * 事件回调
     *
     * @param event 事件对象
     */
    public void onEvent(IEvent event) {
        if(event.name().equals("ActivityInterrupt")) {
            ActivityInterruptEvent activityInterruptEvent = (ActivityInterruptEvent) event;
            refund("活动主取消了活动");
            //
            World.throwEvent(this, activityInterruptEvent);
        }
        else if(event.name().equals("AngelSpeak")) {
            AngelSpeakEvent angelSpeakEvent = (AngelSpeakEvent) event;
            World.throwEvent(this, angelSpeakEvent);
        }
    }

    /**
     * 根据指定条件查找对象
     *
     * @param condition 条件
     * @return 对象
     */
    public static Bid find(Condition condition) {
        if(CONDITION_ID.equalsIgnoreTarget(condition)) {
            int bidId = (Integer) condition.target;
            String sql = "SELECT ID, ActivityID, UserID, Amount, Photo, ExpireTime, Status FROM A_Bid WHERE ID = " + bidId;
            Record record = DB.executor().load(sql);
            if(null == record) {
                return null;
            }
            try {
                return record.build(Bid.class);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else if(CONDITION_ACTIVITYIDUSERID.equalsIgnoreTarget(condition)) {
            int activityId = (Integer) condition.target;
            int bidderId = (Integer) condition.get(true).target;
            String sql = "SELECT ID, ActivityID, UserID, Amount, Photo, ExpireTime, Status FROM A_Bid WHERE ActivityID = " + activityId + " AND UserID = " + bidderId;
            Record record = DB.executor().load(sql);
            if(null == record) {
                return null;
            }
            try {
                return record.build(Bid.class);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * 根据指定条件查找对象列表
     *
     * @param condition 条件
     * @return 对象列表
     */
    public static ICollection<Bid> finds(Condition condition) {
        Set<Bid> result = new Set<Bid>();
        if(CONDITION_ACTIVITYID.equalsIgnoreTarget(condition)) {
            int activityId = (Integer) condition.target;
            String sql = "SELECT ID, ActivityID, UserID, Amount, Photo, ExpireTime, Status FROM A_Bid WHERE ActivityID = " + activityId + " ORDER BY UpdateTime DESC";
            ICollection<Record> records = DB.executor().select(sql);
            for(Record record : records) {
                Bid bid = null;
                try {
                    bid = record.build(Bid.class);
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if(bid != null) {
                    result.add(bid);
                }
            }
        }
        else if(CONDITION_USERID.equalsIgnoreTarget(condition)) {
            int userId = (Integer) condition.target;
            String sql = "SELECT ID, ActivityID, UserID, Amount, Photo, ExpireTime, Status FROM A_Bid WHERE UserID = " + userId + " ORDER BY UpdateTime DESC";
            ICollection<Record> records = DB.executor().select(sql);
            for(Record record : records) {
                Bid bid = null;
                try {
                    bid = record.build(Bid.class);
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if(bid != null) {
                    result.add(bid);
                }
            }
        }
        else if(CONDITION_ACTIVITYIDBYORDER.equalsIgnoreTarget(condition)) {
            int activityId = (Integer) condition.target;
            int order = (Integer) condition.get(true).target;
            // 1 = 竞拍时间，2 = 出价，3 = 信誉度
            String sql = "SELECT B.ID, B.ActivityID, B.UserID, B.Amount, B.Photo, B.ExpireTime, U.Credit, B.Status FROM A_Bid B JOIN A_User U ON B.UserID = U.ID WHERE B.ActivityID = " + activityId + " ORDER BY B.AddTime DESC";
            if (2 == order) {
                sql = "SELECT B.ID, B.ActivityID, B.UserID, B.Amount, B.Photo, B.ExpireTime, U.Credit, B.Status FROM A_Bid B JOIN A_User U ON B.UserID = U.ID WHERE B.ActivityID = " + activityId + " ORDER BY B.Amount DESC";
            }
            else if (3 == order) {
                sql = "SELECT B.ID, B.ActivityID, B.UserID, B.Amount, B.Photo, B.ExpireTime, U.Credit, B.Status FROM A_Bid B JOIN A_User U ON B.UserID = U.ID WHERE B.ActivityID = " + activityId + " ORDER BY U.Credit DESC";
            }
            ICollection<Record> records = DB.executor().select(sql);
            for(Record record : records) {
                Bid bid = null;
                try {
                    bid = record.build(Bid.class);
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if(bid != null) {
                    result.add(bid);
                }
            }
        }
        return result;
    }
}
