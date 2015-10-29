package com.slfuture.angel.wechart.object;

import com.slfuture.angel.wechart.event.*;
import com.slfuture.angel.wechart.structure.BidInfo;
import com.slfuture.angel.wechart.structure.Chart;
import com.slfuture.angel.wechart.utility.DB;
import com.slfuture.angel.wechart.utility.Pay;
import com.slfuture.carrie.base.text.Text;
import com.slfuture.carrie.base.time.DateTime;
import com.slfuture.carrie.base.type.Record;
import com.slfuture.carrie.base.type.Set;
import com.slfuture.carrie.base.type.Table;
import com.slfuture.carrie.base.type.core.ICollection;
import com.slfuture.carrie.base.type.core.ILink;
import com.slfuture.carrie.world.IEvent;
import com.slfuture.carrie.world.IObject;
import com.slfuture.carrie.world.World;
import com.slfuture.carrie.world.annotation.Method;
import com.slfuture.carrie.world.annotation.Property;
import com.slfuture.carrie.world.relation.Condition;
import com.slfuture.utility.aliyun.OCS;
import com.slfuture.utility.wechart.pay.Payment;
import com.slfuture.utility.wechart.pay.structure.TransferResult;
import org.apache.log4j.Logger;

/**
 * 活动详情
 */
public class ActivityDetail extends Activity {
    /**
     * 审批中
     */
    public final static int STATUS_APPROVAL = 1;
    /**
     * 被驳回
     */
    public final static int STATUS_REJECT = 2;
    /**
     * 已撤销
     */
    public final static int STATUS_GIVEUP = 3;
    /**
     * 发布中
     */
    public final static int STATUS_ONLINE = 4;
    /**
     * 已取消
     */
    public final static int STATUS_CANCEL = 5;
    /**
     * 已收款
     */
    public final static int STATUS_PAY = 6;
    /**
     * 已中止
     */
    public final static int STATUS_INTERRUPT = 7;
    /**
     * 被违约
     */
    public final static int STATUS_VIOLATE = 8;
    /**
     * 已成交
     */
    public final static int STATUS_DEAL = 9;


    /**
     * 活动缓存
     */
    public static class ActivityCache extends OCS {
        /**
         * 构造函数
         */
        public ActivityCache() {
            super("Activity", PERIOD_UPDATE);
        }

        /**
         * 穿透回调
         *
         * @param key 键
         * @return 值
         */
        @Override
        public Object onMiss(String key) {
            String sql = "SELECT A.ID, Type, AngelID, A.MockName, BidID, Title, A.Photos, PrepareTime, StartTime, CityID, Region, Career, IFNULL(B.UserCount, 0) AS Popularity, Address, Content, Rules, NeedPhoto, Memo, BasicPrice, A.Status FROM A_Activity A JOIN A_User U ON A.AngelID = U.ID LEFT JOIN (SELECT ActivityID, COUNT(UserID) As UserCount FROM A_Bid WHERE Status = 1 AND ExpireTime > NOW() GROUP BY ActivityID) B ON A.ID = B.ActivityID WHERE A.ID = " + key;
            ICollection<Record> records = DB.executor().select(sql);
            for(Record record : records) {
                ActivityDetail detail = null;
                try {
                    detail = record.build(ActivityDetail.class);
                }
                catch (Exception ex) {
                    logger.error("Record.build(ActivityDetail.class) failed\n" + record, ex);
                }
                if(null == detail) {
                    continue;
                }
                detail.photoList = new Set<String>();
                String photos = record.getString("Photos");
                if(!Text.isBlank(photos)) {
                    for(String photo : photos.split(",")) {
                        if(Text.isBlank(photo)) {
                            continue;
                        }
                        detail.photoList.add(photo);
                    }
                }
                return detail;
            }
            return null;
        }
    }


    /**
     * 日志对象
     */
    private static Logger logger = Logger.getLogger(ActivityDetail.class);
    /**
     * 更新周期
     */
    public final static int PERIOD_UPDATE = 60 * 60;
    /**
     * 详情缓存
     */
    private static ActivityCache cache = null;


    /**
     * 获取缓存
     *
     * @return 缓存对象
     */
    private static ActivityCache getCache() {
        if(null == cache) {
            synchronized (ActivityDetail.class) {
                if(null == cache) {
                    cache = new ActivityCache();
                }
            }
        }
        return cache;
    }

    /**
     * 女神ID
     */
    @Property
    public int angelId;
    /**
     * 女神代号
     */
    @Property
    public String mockName;
    /**
     * 性别
     */
    @Property
    public int gender;
    /**
     * 职业
     */
    @Property
    public String career;
    /**
     * 竞拍ID
     */
    @Property
    public int bidId;
    /**
     * 封面头像
     */
    @Property
    public Set<String> photoList;
    /**
     * 预选结束时间
     */
    @Property
    public DateTime prepareTime;
    /**
     * 流行度
     */
    @Property
    public int popularity;
    /**
     * 见面地点
     */
    @Property
    public String address;
    /**
     * 活动规则
     */
    @Property
    public String rules;
    /**
     * 是否需要照片
     */
    @Property
    public boolean needPhoto;
    /**
     * 备注信息
     */
    @Property
    public String memo;
    /**
     * 底价
     */
    @Property
    public int basicPrice;
    /**
     * 慈善捐赠比例：0-100
     */
    @Property
    public int donate;
    /**
     * 活动状态，1：审核中，2：发布，3：确立，4：成交，5：取消
     */
    @Property
    public int status;


    /**
     * 获取当前最高竞拍价
     *
     * @return 当前最高竞拍价
     */
    @Method
    public int getCurrentPrice() {
        ICollection<IObject> bids = World.relatives(this, "bids", IObject.class);
        Table<Integer, Integer> countMap = new Table<Integer, Integer>();
        Table<Integer, Integer> maxMap = new Table<Integer, Integer>();
        for(IObject bid : bids) {
            int amount = (Integer) bid.property("amount");
            int step = amount / 10000;
            if(null == maxMap.get(step)) {
                maxMap.put(step, amount);
            }
            else if(maxMap.get(step) < amount) {
                maxMap.put(step, amount);
            }
            if(null == countMap.get(step)) {
                countMap.put(step, 1);
            }
            else {
                countMap.put(step, countMap.get(step) + 1);
            }
        }
        int countMaxStep = 0;
        int countMax = 0;
        for(ILink<Integer, Integer> link : countMap) {
            if(countMax < link.destination()) {
                countMax = link.destination();
                countMaxStep = link.origin();
            }
        }
        while(true) {
            if(null == countMap.get(1 + countMaxStep)) {
                break;
            }
            countMaxStep++;
        }
        Integer result = maxMap.get(countMaxStep);
        if(null == result) {
            return basicPrice;
        }
        return result;
    }

    /**
     * 审批活动
     *
     * @param result 结果
     * @param reason 拒绝原因
     */
    @Method
    public void approval(boolean result, String reason) {
        String sql = null;
        if(result) {
            sql = "UPDATE A_Activity SET Status = 4 WHERE Status = 1 AND ID = " + id;
        }
        else {
            sql = "UPDATE A_Activity SET Status = 2, Memo = '" + reason + "'  WHERE Status = 1 AND ID = " + id;
        }
        if(1 == DB.executor().alter(sql)) {
            OCS.set("Activity", String.valueOf(id), 0, null);
            World.throwEvent(this, new ActivityApprovalEvent(id, title, result, reason));
        }
    }

    /**
     * 撤销投放
     *
     * @return 操作结果
     */
    @Method
    public boolean giveup() {
        if(STATUS_APPROVAL != status && STATUS_REJECT != status) {
            return false;
        }
        String sql = "UPDATE A_Activity SET Status = " + STATUS_GIVEUP + " WHERE (Status = " + STATUS_APPROVAL + " || Status  = " + STATUS_REJECT + ") AND ID = " + id;
        int result = DB.executor().alter(sql);
        if(1 == result) {
            OCS.set("Activity", String.valueOf(id), 0, null);
            World.throwEvent(this, new ActivityGiveupEvent(id, angelId));
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * 取消活动
     *
     * @return 执行结果
     */
    @Method
    public boolean cancel() {
        if(STATUS_ONLINE != status) {
            return false;
        }
        String sql = "UPDATE A_Activity SET Status = " + STATUS_CANCEL + " WHERE Status = " + STATUS_ONLINE + " AND ID = " + id;
        int result = DB.executor().alter(sql);
        if(1 == result) {
            OCS.set("Activity", String.valueOf(id), 0, null);
            World.throwEvent(this, new ActivityCancelEvent(id, angelId));
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * 中止活动
     *
     * @param reason 原因
     * @return 操作结果
     */
    @Method
    public boolean interrupt(String reason) {
        if(STATUS_PAY != status) {
            return false;
        }
        IObject angel = World.relative(this, "angel", IObject.class);
        if(null == angel) {
            return false;
        }
        IObject bid = World.relative(this, "deal", IObject.class);
        if(null == bid) {
            return false;
        }
        //
        String sql = "UPDATE A_Activity SET Status = " + STATUS_INTERRUPT + ", Memo = '" + reason + "' WHERE Status = " + STATUS_PAY + " AND ID = " + id;
        int result = DB.executor().alter(sql);
        if(1 == result) {
            status = STATUS_INTERRUPT;
            OCS.set("Activity", String.valueOf(id), 0, null);
            World.throwEvent(this, new ActivityInterruptEvent(id, title, angelId, (String) angel.property("nickName"), startTime, reason, (Integer) bid.property("amount")));
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * 成交
     *
     * @param bidderId 竞拍者ID
     * @return 是否执行成功
     */
    @Method
    public boolean deal(int bidderId) {
        if(STATUS_PAY != status && STATUS_DEAL != status) {
            return false;
        }
        IObject bid = World.relative(this, "deal", IObject.class);
        if(null == bid) {
            logger.warn("activity deal faild, no bid\nactivityId = " + id);
            return false;
        }
        IObject bidder = World.relative(bid, "user", IObject.class);
        if(null == bidder) {
            logger.warn("activity deal faild, no bidder\nactivityId = " + id);
            return false;
        }
        if(bidderId == (Integer) bid.property("userId")) {
            if(STATUS_DEAL == status) {
                logger.info("activity repeat deal\nactivityId = " + id);
                return true;
            }
        }
        else {
            logger.warn("activity deal faild, not the winner bid\nactivityId = " + id);
            return false;
        }
        IObject angel = World.relative(this, "angel", IObject.class);
        if(null == angel) {
            logger.warn("activity deal faild, no angel\nactivityId = " + id);
            return false;
        }
        int fee = Pay.getPayAmount((Integer) bid.property("amount"), donate);
        String sql = "UPDATE A_Activity SET Status = " + STATUS_DEAL + " WHERE Status = " + STATUS_PAY + " AND ID = " + id;
        int result = DB.executor().alter(sql);
        if(1 == result) {
            if(fee > 0) {
                try {
                    TransferResult transferResult = Payment.transfer(id, (String) angel.property("name"), (String) angel.property("openId"), fee, "活动费用及酬劳", "120.55.240.239");
                    if(null == transferResult || TransferResult.CODE_SUCCESS != transferResult.code) {
                        logger.error("transfer failed: id = " + id + ", name = " + angel.property("name") + ", openId = " + angel.property("openId") + ", money = " + fee + ", result = " + transferResult);
                    }
                    else {
                        logger.info("transfer result : " + transferResult);
                    }
                }
                catch(Exception ex) {
                    logger.error("transfer failed: id = " + id + ", name = " + angel.property("name") + ", openId = " + angel.property("openId") + ", money = " + fee, ex);
                }
            }
            status = STATUS_DEAL;
            OCS.set("Activity", String.valueOf(id), 0, null);
            // 抛出事件
            World.throwEvent(this, new ActivityDealEvent(id, title, angelId, (String) angel.property("name"), mockName, bidId, bidderId, (String) bidder.property("nickName"), (Integer) bid.property("amount"), fee, content));
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * 是否可以支付
     *
     * @return 是否可以支付，0：可以支付，-1：活动已不在发布状态，-2：活动已过期
     */
    @Method
    public int canPay() {
        if(STATUS_ONLINE != status) {
            return -1;
        }
        if(startTime.compareTo(DateTime.now()) < 0) {
            return -2;
        }
        return 0;
    }

    /**
     * 尝试收款
     *
     * @param bidId 竞拍ID
     * @param amount 竞拍金额
     * @return 执行结果
     */
    @Method
    public boolean onPay(int bidId, int amount) {
        String sql = "UPDATE A_Activity SET BidID = " + bidId + ", Status = " + STATUS_PAY + " WHERE Status = " + STATUS_ONLINE + " AND ID = " + id;
        int result = DB.executor().alter(sql);
        if(1 != result) {
            return false;
        }
        this.bidId = bidId;
        this.status = STATUS_PAY;
        OCS.set("Activity", String.valueOf(id), 0, null);
        // 投递消息
        IObject bid = World.relative(this, "deal", IObject.class);
        if(null == bid) {
            return true;
        }
        IObject bidder = World.relative(bid, "user", IObject.class);
        if(null == bidder) {
            return true;
        }
        ActivityPayEvent event = new ActivityPayEvent();
        event.activityId = id;
        event.activityTitle = title;
        event.startTime = startTime;
        event.angelId = angelId;
        event.bidderNickName = (String) bidder.property("nickName");
        event.amount = (Integer) bid.property("amount");
        World.throwEvent(this, event);
        return true;
    }

    /**
     * 获取指定用户ID的竞拍者信息
     *
     * @param userId 用户ID
     * @return 竞拍者信息
     */
    @Method
    public BidInfo fetchBidder(int userId) {
        String sql = "SELECT B.ID AS BidID, B.ActivityID, B.UserID, U.NickName, B.Photo, B.ExpireTime, B.Amount, B.Status FROM A_Bid B JOIN A_User U ON B.UserID = U.ID WHERE B.ActivityID = " + id + " AND B.UserID = " + userId;
        Record record = DB.executor().load(sql);
        if(null == record) {
            return null;
        }
        return BidInfo.build(record);
    }

    /**
     * 获取竞拍者信息列表
     *
     * @param page 页索引
     * @return 竞拍者列表
     */
    @Method
    public BidInfo[] fetchBidders(int page) {
        int length = 10;
        int begin = 10 * (page - 1);
        String sql = "SELECT B.ID AS BidID, B.ActivityID, B.UserID, U.NickName, B.Photo, B.ExpireTime, B.Amount, B.Status FROM A_Bid B JOIN A_User U ON B.UserID = U.ID WHERE ActivityID = " + id + " ORDER BY B.UpdateTime DESC LIMIT " + begin + ", " + length;
        ICollection<Record> records = DB.executor().select(sql);
        if(0 == records.size()) {
            return new BidInfo[0];
        }
        BidInfo[] result = new BidInfo[records.size()];
        int i = 0;
        for(Record record : records) {
            result[i++] = BidInfo.build(record);
        }
        return result;
    }

    /**
     * 消息发送
     *
     * @param chartId 对话最大ID
     * @param userId 用户ID
     * @param content 消息内容
     * @return 消息ID
     */
    @Method
    public Chart[] chart(int chartId, int userId, String content) {
        String sql = "INSERT INTO A_Chart (ActivityID, UserID, Content, AddTime) VALUES (" + id + ", " + userId + ", '" + content + "', NOW())";
        Long result = DB.executor().insert(sql);
        if(null == result) {
            return null;
        }
        if(angelId == userId) {
            World.throwEvent(this, new AngelSpeakEvent(id, title));
        }
        else {
            World.throwEvent(this, new BidderSpeakEvent(id, title));
        }
        return listen(chartId, userId);
    }

    /**
     * 消息接受
     *
     * @param chartId 对话最大ID
     * @param listenerId 收听者ID
     * @return
     */
    @Method
    public Chart[] listen(int chartId, int listenerId) {
        String sql = "SELECT ID, UserID, Content, AddTime FROM A_Chart WHERE ActivityID = " + id + " AND ID > " + chartId + " ORDER BY AddTime ASC";
        ICollection<Record> records = DB.executor().select(sql);
        Chart[] result = new Chart[records.size()];
        int i = 0;
        for(Record record : records) {
            result[i++] = Chart.build(record);
        }
        OCS.set("Chart_Listen", id + "." + listenerId, 2, DateTime.now().toString());
        return result;
    }

    /**
     * 事件回调
     *
     * @param event 事件对象
     */
    public void onEvent(IEvent event) {
        if(event.name().equals("BidInterrupt")) {
            BidInterruptEvent bidInterruptEvent = (BidInterruptEvent) event;
            World.throwEvent(this, bidInterruptEvent);
        }
    }

    /**
     * 根据指定条件查找对象
     *
     * @param condition 条件
     * @return 对象
     */
    public static ActivityDetail find(Condition condition) {
        int activityId  = (Integer) condition.target;
        if(0 == activityId) {
            return null;
        }
        return (ActivityDetail) getCache().get(String.valueOf(activityId));
    }

    /**
     * 根据指定条件查找对象列表
     *
     * @param condition 条件
     * @return 对象列表
     */
    public static ICollection<ActivityDetail> finds(Condition condition) {
        return new Set<ActivityDetail>(find(condition));
    }
}
