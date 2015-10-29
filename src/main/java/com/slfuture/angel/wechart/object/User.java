package com.slfuture.angel.wechart.object;

import com.slfuture.angel.wechart.event.*;
import com.slfuture.angel.wechart.utility.Credit;
import com.slfuture.angel.wechart.utility.DB;
import com.slfuture.carrie.base.text.Text;
import com.slfuture.carrie.base.time.DateTime;
import com.slfuture.carrie.base.type.Record;
import com.slfuture.carrie.base.type.Set;
import com.slfuture.carrie.base.type.Table;
import com.slfuture.carrie.base.type.core.ICollection;
import com.slfuture.carrie.base.type.core.ILink;
import com.slfuture.carrie.base.type.core.ITable;
import com.slfuture.carrie.world.IEvent;
import com.slfuture.carrie.world.IObject;
import com.slfuture.carrie.world.World;
import com.slfuture.carrie.world.annotation.Method;
import com.slfuture.carrie.world.annotation.Property;
import com.slfuture.carrie.world.relation.Condition;
import com.slfuture.carrie.world.relation.prepare.PropertyPrepare;
import com.slfuture.utility.aliyun.OCS;
import org.apache.log4j.Logger;

import java.io.Serializable;

/**
 * 用户
 */
public class User implements Serializable {
    private static final long serialVersionUID = -1;

    /**
     * 微信开放ID缓存
     */
    public static class OpenIDCache extends OCS {
        /**
         * 构造函数
         */
        public OpenIDCache() {
            super("OpenIDUser", 60 * 60);
        }

        /**
         * 穿透回调
         *
         * @param key 键
         * @return 值
         */
        @Override
        public Object onMiss(String key) {
            Record record = DB.executor().load("SELECT ID, Name, NickName, Phone, Gender, Photos, Career, Credit, OpenID, Mail, BlackList FROM A_User WHERE OpenID = '" + key + "'");
            if(null == record) {
                return null;
            }
            User result = null;
            try {
                result = record.build(User.class);
            }
            catch(Exception ex) { }
            return result;
        }
    }

    /**
     * 用户缓存
     */
    public static class IDCache extends OCS {
        /**
         * 构造函数
         */
        public IDCache() {
            super("IDUser", 60 * 60);
        }

        /**
         * 穿透回调
         *
         * @param key 键
         * @return 值
         */
        @Override
        public Object onMiss(String key) {
            Record record = DB.executor().load("SELECT ID, Name, NickName, Phone, Gender, Photos, Career, Credit, OpenID, Mail, BlackList FROM A_User WHERE ID = " + key);
            if(null == record) {
                return null;
            }
            User result = null;
            try {
                result = record.build(User.class);
            }
            catch(Exception ex) { }
            return result;
        }
    }


    /**
     * 日志对象
     */
    private static Logger logger = Logger.getLogger(User.class);
    /**
     * 条件集合
     */
    private static Condition CONDITION_ID = null;
    private static Condition CONDITION_OPENID = null;
    /**
     * 用户缓存
     */
    private static OpenIDCache openIDCache = null;
    /**
     * 用户缓存
     */
    private static IDCache idCache = null;

    static {
        CONDITION_ID = new Condition();
        CONDITION_ID.prepareSelf = new PropertyPrepare("id");
        CONDITION_OPENID = new Condition();
        CONDITION_OPENID.prepareSelf = new PropertyPrepare("openId");
    }


    /**
     * 用户ID
     */
    @Property
    public int id;
    /**
     * 用户真实姓名
     */
    @Property
    public String name;
    /**
     * 用户昵称
     */
    @Property
    public String nickName;
    /**
     * 用户手机号码
     */
    @Property
    public String phone;
    /**
     * 用户性别，1：男，2：女，0：未知
     */
    @Property
    public int gender;
    /**
     * 用户照片集合
     */
    @Property
    public String photos;
    /**
     * 职业
     */
    @Property
    public String career;
    /**
     * 信誉值
     */
    @Property
    public int credit;
    /**
     * 微信开放ID
     */
    @Property
    public String openId;
    /**
     * 邮箱
     */
    @Property
    public String mail;
    /**
     * 被黑名单本人用户集合
     */
    @Property
    public String blackList;


    /**
     * 获取用户照片数组
     *
     * @return 用户照片数组
     */
    @Method
    public String[] photoList() {
        if(Text.isBlank(photos)) {
            return new String[0];
        }
        return photos.split(",");
    }

    /**
     * 获取主头像
     *
     * @return 主头像
     */
    @Method
    public String primaryPhoto() {
        if(Text.isBlank(photos)) {
            return null;
        }
        return photos.split(",")[0];
    }

    /**
     * 被屏蔽
     *
     * @param userId 屏蔽本人的用户ID
     * @param isBlack 是否屏蔽
     *
     * @return 操作结果
     */
    public void black(int userId, Boolean isBlack) {
        String newBlackList = "";
        if(null != blackList) {
            newBlackList = blackList;
        }
        newBlackList = Text.trim(("," + newBlackList + ",").replace("," + userId + ",", ","), ",");
        if(isBlack) {
            if(0 == newBlackList.length()) {
                newBlackList = String.valueOf(userId);
            }
            else {
                newBlackList += "," + userId;
            }
        }
        blackList = newBlackList;
        String sql = "UPDATE A_User SET BlackList = '" + newBlackList + "' WHERE ID = " + id;
        if(1 == DB.executor().alter(sql)) {
            refresh();
        }
    }

    /**
     * 是否被指定用户屏蔽
     *
     * @param userId 屏蔽本人的用户ID
     * @return 是否被指定用户屏蔽
     */
    @Method
    public boolean isBlacked(int userId) {
        if(Text.isBlank(blackList)) {
            return false;
        }
        return ("," + blackList + ",").contains("," + String.valueOf(userId) + ",");
    }

    /**
     * 投放
     *
     * @param type 活动类型
     * @param title 标题
     * @param mockName 本次活动的昵称
     * @param photos 照片集
     * @param prepareTime 竞标结束时间
     * @param startTime 开始时间
     * @param cityId 城市ID
     * @param region 活动区域
     * @param address 见面地址
     * @param content 活动内容
     * @param rules 规则信息
     * @param needPhoto 是否需要竞标者出示照片
     * @param memo 备注信息
     * @param basicPrice 保底价
     * @param donate 捐赠比例
     *
     * @return 活动ID，失败返回错误码，-1：参数错误，-2：时间设置有误，-3：无资格，-4：一天最多发布10个活动，-5：内部错误
     */
    @Method
    public int launch(int type, String title, String mockName, String photos, DateTime prepareTime, DateTime startTime, int cityId, String region, String address, String content, String rules, Boolean needPhoto, String memo, int basicPrice, int donate) {
        // 参数校验
        if(Activity.TYPE_GIRL != type && Activity.TYPE_BOY != type && Activity.TYPE_FAMOUS != type) {
            return -1;
        }
        if(Text.isBlank(title) || Text.isBlank(mockName) || Text.isBlank(photos) || null == prepareTime || null == startTime || cityId <= 0 || Text.isBlank(region) || Text.isBlank(content) || null == needPhoto || basicPrice < 0 || donate < 0 || donate > 100) {
            return -1;
        }
        if(null == rules) {
            rules = "";
        }
        if(null == memo) {
            memo = "";
        }
        if(Text.isBlank(address)) {
            address = region;
        }
        if(prepareTime.compareTo(DateTime.now()) < 10 * 60) {
            return -2;
        }
        if(startTime.compareTo(prepareTime) < 0) {
            return -2;
        }
        if(startTime.compareTo(DateTime.now()) > 60 * 60 * 24 * 7) {
            return -2;
        }
        // 检查活动主资格
        if(!Credit.canLaunch(credit)) {
            return -3;
        }
        // 检查操作合法性
        String sql = "SELECT COUNT(ID) AS ActivityCount FROM A_Activity WHERE DATE(AddTime) = DATE(NOW()) AND AngelID = " + id;
        long activityCount = DB.executor().load(sql).getLong("ActivityCount");
        if(activityCount >= 10) {
            return -4;
        }
        // 执行变更
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO A_Activity (Type, AngelID, MockName, BidID, Title, Photos, PrepareTime, StartTime, CityID, Region, Address, Content, Rules, NeedPhoto, Memo, BasicPrice, Donate, Status, AddTime, UpdateTime) VALUES (");
        builder.append(type);
        builder.append(", ");
        builder.append(id);
        builder.append(", '");
        builder.append(mockName);
        builder.append("', 0, '");
        builder.append(title);
        builder.append("', '");
        builder.append(photos);
        builder.append("', '");
        builder.append(prepareTime.toString());
        builder.append("', '");
        builder.append(startTime.toString());
        builder.append("', ");
        builder.append(cityId);
        builder.append(", '");
        builder.append(region);
        builder.append("', '");
        builder.append(address);
        builder.append("', '");
        builder.append(content);
        builder.append("', '");
        builder.append(rules);
        builder.append("', ");
        builder.append(needPhoto);
        builder.append(", '");
        builder.append(memo);
        builder.append("', ");
        builder.append(basicPrice);
        builder.append(", ");
        builder.append(donate);
        builder.append(", 1, NOW(), NOW())");
        Long result = DB.executor().insert(builder.toString());
        if(null == result || 0 == result) {
            return -5;
        }
        return (int) ((long) result);
    }

    /**
     * 刷新
     *
     * @param name 真实姓名
     * @param career 职业
     * @param photos 图片
     */
    @Method
    public void update(String name, String career, String photos) {
        if(Text.isBlank(name)) {
            name = this.name;
        }
        if(null == name) {
            name = "";
        }
        //
        if(Text.isBlank(career)) {
            career = this.career;
        }
        if(null == career) {
            career = "";
        }
        //
        Set<String> set = new Set<String>();
        for(String photo : photoList()) {
            set.add(photo);
        }
        if(null != photos) {
            for(String photo : photos.split(",")) {
                if("".equals(photo)) {
                    continue;
                }
                if(!set.contains(photo)) {
                    set.add(photo);
                }
            }
        }
        String newPhotos = null;
        for(String photo : set) {
            if(null == newPhotos) {
                newPhotos = photo;
            }
            else {
                newPhotos += "," + photo;
            }
        }
        //
        String sql = "UPDATE A_User SET Name = '" + name + "', Career = '" + career + "', Photos = '" + newPhotos + "' WHERE ID = " + id;
        DB.executor().alter(sql);
        this.name = name;
        this.career = career;
        this.photos = newPhotos;
        //
        refresh();
    }

    /**
     * 竞价
     *
     * @param activityId 活动ID
     * @param amount 出价
     * @param photo 竞价者头像
     * @param expireTime 截止时间
     * @return 执行结果，-1：参数错误，-2：活动不存在，-3：竞价小于底价，-4：活动要求竞拍者必须上传照片，-5：截止时间必须比当前时间大10分钟，-6：无资格，-7：一天最多参与10次竞拍，-8：无有效竞拍，-9：内部错误，0：创建成功，1：更新成功
     */
    @Method
    public int bid(int activityId, int amount, String photo, DateTime expireTime) {
        // 参数校验
        if(activityId <= 0 || amount < 0) {
            return -1;
        }
        amount = amount * 100;
        IObject activityDetail = World.get("ActivityDetail", activityId, IObject.class);
        if(null == activityDetail) {
            return -2;
        }
        int basicPrice = (Integer) activityDetail.property("basicPrice");
        if(amount < basicPrice) {
            return -3;
        }
        boolean needPhoto = (Boolean) activityDetail.property("needPhoto");
        if(needPhoto) {
            if(Text.isBlank(photo)) {
                return -4;
            }
        }
        else {
            if("".equals(photo)) {
                photo = null;
            }
        }
        if(null != expireTime && expireTime.compareTo(DateTime.now()) <= 10 * 60 * 1000) {
            return -5;
        }
        // 检查活动主资格
        if(!Credit.canBid(credit)) {
            return -6;
        }
        // 检查操作合法性
        String sql = "SELECT COUNT(ID) AS BidCount FROM A_Bid WHERE DATE(AddTime) = DATE(NOW()) AND UserID = " + id;
        long bidCount = DB.executor().load(sql).getLong("BidCount");
        if(bidCount >= 10) {
            return -7;
        }
        // 执行变更
        String photoString = "null";
        if(null != photo) {
            photoString = "'" + photo + "'";
        }
        String expireTimeString = "null";
        if(null != expireTime) {
            expireTimeString = "'" + expireTime.toString() + "'";
        }
        sql = "SELECT * FROM A_Bid WHERE ActivityID = " + activityId + " AND UserID = " + id;
        Record record = DB.executor().load(sql);
        if(null == record) {
            sql = "INSERT INTO A_Bid (ActivityID, UserID, Amount, Photo, ExpireTime, Status, AddTime, UpdateTime) VALUES (" + activityId + ", " + id + ", " + amount + ", " + photoString  + ", " + expireTimeString + ", 1, NOW(), NOW())";
            Long result = DB.executor().insert(sql);
            if(null != result && result > 0) {
                NewBidEvent event = new NewBidEvent();
                event.activityId = activityId;
                event.activityTitle = (String) activityDetail.property("title");
                event.bidId = (int) ((long) result);
                event.bidderNickName = nickName;
                event.amount = amount;
                World.throwEvent(activityDetail, event);
                return 0;
            }
            return -9;
        }
        if(1 == record.get("Status")) {
            sql = "UPDATE A_Bid SET Amount = " + amount + ", Photo = " + photoString + ", ExpireTime = " + expireTimeString + ", Status = " + Bid.STATUS_APPROVAL + ", UpdateTime = NOW() WHERE Status IN (" + Bid.STATUS_APPROVAL + ", " + Bid.STATUS_GIVEUP + ") AND ID = " + record.getInteger("ID");
            int result = DB.executor().alter(sql);
            if(0 == result) {
                return -8;
            }
            else if(1 == result) {
                return 1;
            }
        }
        return -9;
    }

    /**
     * 发送通知消息
     *
     * @param type 消息类型
     * @param meta 元数据
     * @param delivery 是否微信投递
     */
    @Method
    public void notify(int type, ITable<String, Object> meta, int delivery) {
        String metaString = null;
        if(null == meta) {
            metaString = "{}";
        }
        else {
            metaString = "{";
            boolean sentry = false;
            for(ILink<String, Object> link : meta) {
                if(sentry) {
                    metaString += ",";
                }
                else {
                    sentry = true;
                }
                metaString += "\"" + link.origin() + "\":";
                if(null == link.destination()) {
                    metaString += "null";
                }
                else if(link.destination() instanceof Byte || link.destination() instanceof Short || link.destination() instanceof Integer || link.destination() instanceof Long || link.destination() instanceof Float || link.destination() instanceof Short || link.destination() instanceof Double) {
                    metaString += link.destination();
                }
                else {
                    metaString += "\"" + link.destination() + "\"";
                }
            }
            metaString += "}";
        }
        metaString = metaString.replace("'", "\'");
        //
        String sql = null;
        if(Message.TYPE_NEWBID == type || Message.TYPE_NEWCHART == type) {
            sql = "UPDATE A_Message SET Type = " + type + ", Meta = '" + metaString + "', Status = 1, Delivery = " + delivery + ", UpdateTime = NOW() WHERE Type = " + type + " AND Part = " + meta.get("activityId") + " AND UserID = " + id;
            if(1 == DB.executor().alter(sql)) {
                OCS.set("UserMessageCount", String.valueOf(id), 0, null);
                return;
            }
            sql = "INSERT INTO A_Message (Type, Part, UserID, Meta, Status, Delivery, AddTime, UpdateTime) VALUES (" + type + ", " + meta.get("activityId") + ", " + id + ", '" + metaString + "', 1, " + delivery + ", NOW(), NOW())";
        }
        else {
            sql = "INSERT INTO A_Message (Type, Part, UserID, Meta, Status, Delivery, AddTime, UpdateTime) VALUES (" + type + ", 0, " + id + ", '" + metaString + "', 1, " + delivery + ", NOW(), NOW())";
        }
        Long result = DB.executor().insert(sql);
        if(null == result || 0 == result) {
            logger.error("notify user failed, type = " + type + ", meta = " + meta + ", delivery = " + delivery);
        }
        OCS.set("UserMessageCount", String.valueOf(id), 0, null);
    }

    /**
     * 获取用户的消息个数
     */
    @Method
    public int messageCount() {
        Integer count = (Integer) OCS.get("UserMessageCount", String.valueOf(id));
        if(null == count) {
            count = 0;
            ICollection<IObject> messages = World.relatives(this, "messages", IObject.class);
            for(IObject message : messages) {
                if(1 == (Integer) message.property("status")) {
                    count++;
                }
            }
            OCS.set("UserMessageCount", String.valueOf(id), 60 * 10, count);
        }
        return count;
    }

    /**
     * 竞拍历史ID列表
     *
     * @return 竞拍历史列表
     */
    @Method
    public int[] bidHistory() {
        int[] history = (int[]) OCS.get("UserBidHistory", String.valueOf(id));
        if(null != history) {
            return new int[0];
        }
        ICollection<Record> records = DB.executor().select("SELECT ActivityID FROM A_Bid WHERE UserID = " + id);
        history = new int[records.size()];
        int i = 0;
        for(Record record : records) {
            history[i] = record.getInteger("ActivityID");
        }
        OCS.set("UserBidHistory", String.valueOf(id), 60 * 60, history);
        return history;
    }

    /**
     * 事件回调
     *
     * @param event 事件对象
     */
    public void onEvent(IEvent event) {
        if(event.name().equals("MessageRead")) {
            OCS.set("UserMessageCount", String.valueOf(id), 0, null);
        }
        else if(event.name().equals("ActivityGiveup")) {
            ActivityGiveupEvent activityGiveupEvent = (ActivityGiveupEvent) event;
            alterCredit(Credit.EFFECT_LAUNCH_GIVEUP);
        }
        else if(event.name().equals("ActivityCancel")) {
            ActivityCancelEvent activityCancelEvent = (ActivityCancelEvent) event;
            alterCredit(Credit.EFFECT_LAUNCH_CANCEL);
        }
        else if(event.name().equals("ActivityApproval")) {
            ActivityApprovalEvent activityApprovalEvent = (ActivityApprovalEvent) event;
            Table<String, Object> meta = new Table<String, Object>();
            meta.put("activityId", activityApprovalEvent.activityId);
            meta.put("activityTitle", activityApprovalEvent.activityTitle);
            meta.put("reason", activityApprovalEvent.reason);
            if(activityApprovalEvent.result) {
                this.notify(2, meta, 0);
            }
            else {
                this.notify(1, meta, 0);
            }
        }
        else if(event.name().equals("NewBid")) {
            NewBidEvent newBidEvent = (NewBidEvent) event;
            Table<String, Object> meta = new Table<String, Object>();
            meta.put("activityId", newBidEvent.activityId);
            meta.put("activityTitle", newBidEvent.activityTitle);
            meta.put("bidderNickName", newBidEvent.bidderNickName);
            meta.put("amount", newBidEvent.amount);
            this.notify(3, meta, 0);
        }
        else if(event.name().equals("ActivityDeal")) {
            ActivityDealEvent activityDealEvent = (ActivityDealEvent) event;
            if(activityDealEvent.angelId == id) {
                Table<String, Object> meta = new Table<String, Object>();
                meta.put("activityId", activityDealEvent.activityId);
                meta.put("activityTitle", activityDealEvent.activityTitle);
                meta.put("angelName", activityDealEvent.angelName);
                meta.put("mockName", activityDealEvent.mockName);
                meta.put("bidNickName", activityDealEvent.bidNickName);
                meta.put("amount", activityDealEvent.amount);
                meta.put("fee", activityDealEvent.fee);
                notify(Message.TYPE_ACTIVITYDEAL, meta, 0);
                //
                alterCredit(Credit.EFFECT_LAUNCH_DEAL);
                // notify(Message.TYPE_TRANSFER, meta, 1);
            }
            else if(activityDealEvent.bidderId == id) {
                Table<String, Object> meta = new Table<String, Object>();
                meta.put("activityId", activityDealEvent.activityId);
                meta.put("activityTitle", activityDealEvent.activityTitle);
                meta.put("angelName", activityDealEvent.angelName);
                meta.put("mockName", activityDealEvent.mockName);
                meta.put("bidNickName", activityDealEvent.bidNickName);
                meta.put("amount", activityDealEvent.amount);
                meta.put("content", activityDealEvent.content);
                meta.put("fee", activityDealEvent.fee);
                notify(Message.TYPE_BIDDEAL, meta, 0);
                //
                alterCredit(Credit.EFFECT_BID_DEAL);
            }
        }
        else if(event.name().equals("ActivityInterrupt")) {
            ActivityInterruptEvent activityInterruptEvent = (ActivityInterruptEvent) event;
            if(activityInterruptEvent.angelId == id) {
                alterCredit(Credit.EFFECT_LAUNCH_INTERRUPT);
            }
            else {
                Table<String, Object> meta = new Table<String, Object>();
                meta.put("activityId", activityInterruptEvent.activityId);
                meta.put("activityTitle", activityInterruptEvent.activityTitle);
                meta.put("mockName", activityInterruptEvent.mockName);
                meta.put("startTime", activityInterruptEvent.startTime);
                meta.put("amount", activityInterruptEvent.amount);
                meta.put("reason", activityInterruptEvent.reason);
                notify(Message.TYPE_ACTIVITYBREAK, meta, 1);
            }
        }
        else if(event.name().equals("BidGiveup")) {
            alterCredit(Credit.EFFECT_BID_GIVEUP);
        }
        else if(event.name().equals("BidInterrupt")) {
            BidInterruptEvent bidInterruptEvent = (BidInterruptEvent) event;
            if(bidInterruptEvent.bidderId == id) {
                alterCredit(Credit.EFFECT_BID_INTERRUPT);
            }
            else {
                Table<String, Object> meta = new Table<String, Object>();
                meta.put("activityId", bidInterruptEvent.activityId);
                meta.put("activityTitle", bidInterruptEvent.activityTitle);
                meta.put("startTime", bidInterruptEvent.startTime);
                meta.put("amount", bidInterruptEvent.amount);
                meta.put("bidderId", bidInterruptEvent.bidderId);
                meta.put("bidderNickName", bidInterruptEvent.bidderNickName);
                meta.put("reason", bidInterruptEvent.reason);
                notify(Message.TYPE_BIDBREAK, meta, 1);
            }
        }
        else if(event.name().equals("Refund")) {
            RefundEvent refundEvent = (RefundEvent) event;
            Table<String, Object> meta = new Table<String, Object>();
            meta.put("activityId", refundEvent.activityId);
            meta.put("activityTitle", refundEvent.activityTitle);
            meta.put("amount", refundEvent.amount);
            meta.put("reason", refundEvent.reason);
            notify(Message.TYPE_REFUND, meta, 1);
        }
        else if(event.name().equals("ActivityPay")) {
            ActivityPayEvent activityPayEvent = (ActivityPayEvent) event;
            if(activityPayEvent.angelId == id) {
                Table<String, Object> meta = new Table<String, Object>();
                meta.put("activityId", activityPayEvent.activityId);
                meta.put("activityTitle", activityPayEvent.activityTitle);
                meta.put("amount", activityPayEvent.amount);
                meta.put("bidderNickName", activityPayEvent.bidderNickName);
                meta.put("startTime", activityPayEvent.startTime);
                notify(Message.TYPE_ACTIVITYONPAY, meta, 1);
            }
            else {
                Table<String, Object> meta = new Table<String, Object>();
                meta.put("activityId", activityPayEvent.activityId);
                meta.put("activityTitle", activityPayEvent.activityTitle);
                meta.put("amount", activityPayEvent.amount);
                notify(Message.TYPE_PAY, meta, 1);
                //
                alterCredit(Credit.EFFECT_BID_PAY);
            }
        }
        else if(event.name().equals("BidChoose")) {
            BidChooseEvent bidChooseEvent = (BidChooseEvent) event;
            Table<String, Object> meta = new Table<String, Object>();
            meta.put("activityId", bidChooseEvent.activityId);
            meta.put("activityTitle", bidChooseEvent.activityTitle);
            meta.put("mockName", bidChooseEvent.mockName);
            meta.put("amount", bidChooseEvent.amount);
            notify(Message.TYPE_BIDCHOOSE, meta, 1);
        }
        else if(event.name().equals("AngelSpeak")) {
            AngelSpeakEvent angelSpeakEvent = (AngelSpeakEvent) event;
            if(null != OCS.get("Chart_Listen", angelSpeakEvent.activityId + "." + id)) {
                return;
            }
            Table<String, Object> meta = new Table<String, Object>();
            meta.put("activityId", angelSpeakEvent.activityId);
            meta.put("activityTitle", angelSpeakEvent.activityTitle);
            notify(Message.TYPE_NEWCHART, meta, 0);
        }
        else if(event.name().equals("BidderSpeak")) {
            BidderSpeakEvent bidderSpeakEvent = (BidderSpeakEvent) event;
            if(null != OCS.get("Chart_Listen", bidderSpeakEvent.activityId + "." + id)) {
                return;
            }
            Table<String, Object> meta = new Table<String, Object>();
            meta.put("activityId", bidderSpeakEvent.activityId);
            meta.put("activityTitle", bidderSpeakEvent.activityTitle);
            notify(Message.TYPE_NEWCHART, meta, 0);
        }
    }

    /**
     * 调整信誉值
     *
     * @param score 变动的分数值
     */
    @Method
    public void alterCredit(int score) {
        if(0 == score) {
            return;
        }
        String sql = "UPDATE A_User SET Credit = Credit + (" + score + ") WHERE id = " + id;
        DB.executor().alter(sql);
        credit += score;
        refresh();
    }

    /**
     * 刷新缓存
     */
    public void refresh() {
        OCS.set("IDUser", String.valueOf(id), 60 * 60, null);
        OCS.set("OpenIDUser", openId, 60 * 60, null);
    }

    /**
     * 根据指定条件查找对象
     *
     * @param condition 条件
     * @return 对象
     */
    public static User find(Condition condition) {
        if(CONDITION_OPENID.equalsIgnoreTarget(condition)) {
            if(null == openIDCache) {
                synchronized (User.class) {
                    if(null == openIDCache) {
                        openIDCache = new OpenIDCache();
                    }
                }
            }
            return (User) openIDCache.get((String) condition.target);
        }
        else if(CONDITION_ID.equalsIgnoreTarget(condition)) {
            if(null == idCache) {
                synchronized (User.class) {
                    if(null == idCache) {
                        idCache = new IDCache();
                    }
                }
            }
            return (User) idCache.get(String.valueOf(condition.target));
        }
        return null;
    }

    /**
     * 根据指定条件查找对象列表
     *
     * @param condition 条件
     * @return 对象列表
     */
    public static ICollection<User> finds(Condition condition) {
        return new Set<User>(find(condition));
    }
}
