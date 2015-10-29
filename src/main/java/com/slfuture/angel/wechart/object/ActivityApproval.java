package com.slfuture.angel.wechart.object;

import com.slfuture.angel.wechart.utility.DB;
import com.slfuture.carrie.base.time.DateTime;
import com.slfuture.carrie.base.type.Record;
import com.slfuture.carrie.base.type.Set;
import com.slfuture.carrie.base.type.core.ICollection;
import com.slfuture.carrie.world.annotation.Property;
import com.slfuture.carrie.world.relation.Condition;

/**
 * 活动申请
 */
public class ActivityApproval extends Activity {
    /**
     * 活动竞拍截止时间
     */
    @Property
    public DateTime prepareTime;
    /**
     * 活动详细地址
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
     * 捐赠比例
     */
    @Property
    public int donate;

    /**
     * 用户真实姓名
     */
    @Property
    public String name;
    /**
     * 用户性别
     */
    @Property
    public int gender;
    /**
     * 用户手机号码
     */
    @Property
    public String phone;
    /**
     * 用户昵称
     */
    @Property
    public String nickName;
    /**
     * 用户代号
     */
    @Property
    public String mockName;
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


    public int id() {
        return id;
    }
    public int type() {
        return type;
    }
    public String title() {
        return title;
    }

    public String content() {
        return content;
    }

    public DateTime startTime() {
        return startTime;
    }

    public int cityId() {
        return cityId;
    }

    public String region() {
        return region;
    }
    public DateTime prepareTime() {
        return prepareTime;
    }

    public String address() {
        return address;
    }

    public String rules() {
        return rules;
    }

    public boolean isNeedPhoto() {
        return needPhoto;
    }

    public String memo() {
        return memo;
    }

    public int basicPrice() {
        return basicPrice;
    }

    public int donate() {
        return donate;
    }

    public String name() {
        return name;
    }

    public int gender() {
        return gender;
    }

    public String phone() {
        return phone;
    }

    public String nickName() {
        return nickName;
    }

    public String mockName() {
        return mockName;
    }

    public String career() {
        return career;
    }

    public int credit() {
        return credit;
    }

    /**
     * 根据指定条件查找对象
     *
     * @param condition 条件
     * @return 对象
     */
    public static ActivityApproval find(Condition condition) {
        String sql = "SELECT A.*, U.*,  A.ID AS ID, U.ID AS AngelID, A.Photos AS Photos FROM A_Activity A JOIN A_User U ON A.AngelID = U.ID WHERE A.Status = 1 ORDER BY A.ID ASC LIMIT 1";
        Record record = DB.executor().load(sql);
        if(null == record) {
            return null;
        }
        ActivityApproval result = null;
        try {
            result = record.build(ActivityApproval.class);
        }
        catch (Exception e) { }
        return result;
    }

    /**
     * 根据指定条件查找对象列表
     *
     * @param condition 条件
     * @return 对象列表
     */
    public static ICollection<ActivityApproval> finds(Condition condition) {
        return new Set<ActivityApproval>();
    }
}
