package com.slfuture.angel.wechart.object;

import com.slfuture.angel.wechart.utility.DB;
import com.slfuture.carrie.base.logic.grammar.WordLogicalGrammar;
import com.slfuture.carrie.base.type.Record;
import com.slfuture.carrie.base.type.Set;
import com.slfuture.carrie.base.type.core.ICollection;
import com.slfuture.carrie.world.annotation.Property;
import com.slfuture.carrie.world.relation.Condition;
import com.slfuture.carrie.world.relation.prepare.PropertyPrepare;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 城市
 */
public class City {
    /**
     * 城市缓存
     */
    private static ConcurrentHashMap<Integer, City> cache = new ConcurrentHashMap<Integer, City>();
    /**
     * 城市缓存
     */
    private static Set<City> cities = null;
    /**
     * 热门城市
     */
    private static Set<City> hotCities = null;
    /**
     * 条件集合
     */
    private static Condition CONDITION_ID = null;
    private static Condition CONDITION_HOT = null;
    private static Condition CONDITION_KEYWORD = null;
    private static Condition CONDITION_POSITION = null;

    static {
        CONDITION_ID = new Condition();
        CONDITION_ID.prepareSelf = new PropertyPrepare("id");
        CONDITION_HOT = new Condition();
        CONDITION_HOT.prepareSelf = new PropertyPrepare("hot");
        CONDITION_KEYWORD = new Condition();
        CONDITION_KEYWORD.prepareSelf = new PropertyPrepare("keyword");
        //
        Condition lngCondition = new Condition();
        lngCondition.prepareSelf = new PropertyPrepare("longitude");
        Condition latCondition = new Condition();
        latCondition.prepareSelf = new PropertyPrepare("latitude");
        CONDITION_POSITION = lngCondition;
        CONDITION_POSITION.puts(true, latCondition);
    }


    /**
     * 城市ID
     */
    @Property
    public int id;

    /**
     * 城市名称
     */
    @Property
    public String name;


    /**
     * 根据指定条件查找对象
     *
     * @param condition 条件
     * @return 对象
     */
    public static City find(Condition condition) {
        if(CONDITION_ID.equalsIgnoreTarget(condition)) {
            int cityId = (Integer) condition.target;
            City result = cache.get(cityId);
            if(null != result) {
                return result;
            }
            String sql = "SELECT ID, Name FROM A_City WHERE ID = " + cityId;
            Record record = DB.executor().load(sql);
            if(null == record) {
                return null;
            }
            try {
                result = record.build(City.class);
                cache.put(cityId, result);
            }
            catch (Exception e) { }
            return result;
        }
        else if(CONDITION_POSITION.equalsIgnoreTarget(condition)) {
            String sql = "SELECT ID, Name FROM A_City WHERE " + condition.toString(new WordLogicalGrammar());
            Record record = DB.executor().load(sql);
            if(null == record) {
                return null;
            }
            City result = null;
            try {
                result = record.build(City.class);
            }
            catch (Exception e) { }
            return result;
        }
        return null;
    }

    /**
     * 根据指定条件查找对象集合
     *
     * @param condition 条件
     * @return 对象集合
     */
    public static ICollection<City> finds(Condition condition) {
        if(null == condition) {
            if(null == cities) {
                synchronized (City.class) {
                    if(null == cities) {
                        cities = new Set<City>();
                        ICollection<Record> records = DB.executor().select("SELECT ID, Name FROM A_City LIMIT 100");
                        for(Record record : records) {
                            try {
                                cities.add(record.build(City.class));
                            }
                            catch (Exception e) { }
                        }
                    }
                }
            }
            return cities;
        }
        else if(CONDITION_HOT.equalsIgnoreTarget(condition)) {
            if(null == hotCities) {
                synchronized (City.class) {
                    if(null == hotCities) {
                        hotCities = new Set<City>();
                        ICollection<Record> records = DB.executor().select("SELECT ID, Name FROM A_City ORDER BY Hot DESC LIMIT 30");
                        for(Record record : records) {
                            try {
                                hotCities.add(record.build(City.class));
                            }
                            catch (Exception e) { }
                        }
                    }
                }
            }
            return hotCities;
        }
        else if(CONDITION_KEYWORD.equalsIgnoreTarget(condition)) {
            String keyword = (String) condition.target;
            String sql = "SELECT ID, Name FROM A_City WHERE Name like '" + keyword + "%' OR EnName like '" + keyword + "%' LIMIT 10";
            ICollection<Record> records = DB.executor().select(sql);
            Set<City> result = new Set<City>();
            for(Record record : records) {
                try {
                    result.add(record.build(City.class));
                }
                catch (Exception e) { }
            }
            return result;
        }
        return new Set<City>();
    }
}
