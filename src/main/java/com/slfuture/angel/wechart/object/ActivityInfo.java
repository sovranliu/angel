package com.slfuture.angel.wechart.object;

import com.slfuture.angel.wechart.utility.DB;
import com.slfuture.carrie.base.type.Record;
import com.slfuture.carrie.base.type.Set;
import com.slfuture.carrie.base.type.core.ICollection;
import com.slfuture.carrie.world.annotation.Property;
import com.slfuture.carrie.world.relation.Condition;
import com.slfuture.carrie.world.relation.prepare.PropertyPrepare;

/**
 * 活动信息
 */
public class ActivityInfo extends Activity {
    /**
     * 条件集合
     */
    private static Condition CONDITION_ANGELID = null;

    static {
        CONDITION_ANGELID = new Condition();
        CONDITION_ANGELID.prepareSelf = new PropertyPrepare("angelId");
    }

    /**
     * 活动主ID
     */
    @Property
    public int angelId;


    /**
     * 根据指定条件查找对象
     *
     * @param condition 条件
     * @return 对象
     */
    public static ActivityInfo find(Condition condition) {
        return null;
    }

    /**
     * 根据指定条件查找对象列表
     *
     * @param condition 条件
     * @return 对象列表
     */
    public static ICollection<ActivityInfo> finds(Condition condition) {
        Set<ActivityInfo> result = new Set<ActivityInfo>();
        if(CONDITION_ANGELID.equalsIgnoreTarget(condition)) {
            int angelId = (Integer) condition.target;
            String sql = "SELECT ID, Type, AngelID, Photos, Title, Content, StartTime, CityID, Region FROM A_Activity WHERE AngelID = " + angelId + " ORDER BY AddTime DESC";
            ICollection<Record> records = DB.executor().select(sql);
            for(Record record : records) {
                ActivityInfo activityInfo = null;
                try {
                    activityInfo = record.build(ActivityInfo.class);
                }
                catch (Exception e) { }
                if(activityInfo != null) {
                    result.add(activityInfo);
                }
            }
        }
        return result;
    }
}
