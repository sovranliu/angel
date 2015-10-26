package com.slfuture.angel.wechart.object;

import com.slfuture.angel.wechart.utility.DB;
import com.slfuture.carrie.base.type.Record;
import com.slfuture.carrie.base.type.Set;
import com.slfuture.carrie.base.type.core.ICollection;
import com.slfuture.carrie.base.type.core.ITable;
import com.slfuture.carrie.utility.cache.IntervalCache;
import com.slfuture.carrie.world.annotation.Property;
import com.slfuture.carrie.world.relation.Condition;
import com.slfuture.carrie.world.relation.prepare.PropertyPrepare;

/**
 * 广告
 */
public class Ad {
    /**
     * 条件集合
     */
    private static Condition CONDITION_ID = null;
    /**
     * 缓存
     */
    private static IntervalCache<Integer, Ad> cached = null;
    /**
     * 集合
     */
    private static ICollection<Ad> adSet = new Set<Ad>();

    static {
        CONDITION_ID = new Condition();
        CONDITION_ID.prepareSelf = new PropertyPrepare("id");
        cached = new IntervalCache<Integer, Ad>(100 * 1000) {
            /**
             * 刷新回调
             *
             * @param map 待刷新缓存
             * @return 是否刷新成功
             */
            @Override
            public boolean onFresh(ITable<Integer, Ad> map) {
                ICollection<Record> records = DB.executor().select("SELECT ID, Image, Link FROM A_Ad WHERE Status = 1 AND ExpireTime > NOW()");
                Set<Ad> set = new Set<Ad>();
                for(Record record : records) {
                    try {
                        Ad ad = record.build(Ad.class);
                        if(null == ad) {
                            continue;
                        }
                        set.add(ad);
                        map.put(ad.id, ad);
                    }
                    catch (Exception e) {
                        return false;
                    }
                }
                adSet = set;
                return true;
            }
        };
    }

    /**
     * ID
     */
    @Property
    public int id;
    /**
     * 图片地址
     */
    @Property
    public String image;
    /**
     * 链接地址
     */
    @Property
    public String link;


    /**
     * 根据指定条件查找对象
     *
     * @param condition 条件
     * @return 对象
     */
    public static Ad find(Condition condition) {
        if(CONDITION_ID.equalsIgnoreTarget(condition)) {
            return cached.get((Integer) condition.target);
        }
        return null;
    }

    /**
     * 根据指定条件查找对象集合
     *
     * @param condition 条件
     * @return 对象集合
     */
    public static ICollection<Ad> finds(Condition condition) {
        return adSet;
    }
}
