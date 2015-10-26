package com.slfuture.angel.wechart.object;

import com.slfuture.angel.wechart.utility.DB;
import com.slfuture.carrie.base.type.Record;
import com.slfuture.carrie.base.type.Set;
import com.slfuture.carrie.base.type.core.ICollection;
import com.slfuture.carrie.world.annotation.Method;
import com.slfuture.carrie.world.relation.Condition;

/**
 * 区域
 */
public class Region {
    /**
     * 单件实例
     */
    private static Region instance = null;
    /**
     * 热门城市集合
     */
    private static ICollection<City> hotCitys = null;


    /**
     * 根据指定的关键词搜索城市集合
     *
     * @param keyword 关键词
     * @return 城市集合
     */
    @Method
    public ICollection<City> search(String keyword) {
        String sql = "SELECT ID, Name FROM A_City WHERE Name like '" + keyword + "%' OR EnName like '\" + keyword + \"%' LIMIT 10";
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

    /**
     * 根据经纬度搜索城市
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 命中率最高的城市
     */
    @Method
    public City search(double lng, double lat) {
        String sql = "SELECT ID, Name FROM A_City ORDER BY ABS(Longitude - " + lng + ") +  ABS(Latitude - " + lat + ") ASC LIMIT 1";
        Record record = DB.executor().load(sql);
        try {
            return record.build(City.class);
        }
        catch (Exception e) { }
        return null;
    }

    /**
     * 获取热门城市集合
     *
     * @return 城市集合
     */
    @Method
    public ICollection<City> hot() {
        if(null != hotCitys) {
            return hotCitys;
        }
        synchronized (Region.class) {
            if(null != hotCitys) {
                return hotCitys;
            }
            Set<City> citys = new Set<City>();
            String sql = "SELECT ID, Name FROM A_City LIMIT 30";
            ICollection<Record> recordSet = DB.executor().select(sql);
            for(Record record : recordSet) {
                try {
                    City city = record.build(City.class);
                    if(null != city) {
                        citys.add(city);
                    }
                }
                catch (Exception e) { }
            }
            hotCitys = citys;
            return hotCitys;
        }
    }

    /**
     * 根据指定条件查找对象
     *
     * @param condition 条件
     * @return 对象
     */
    public static Region find(Condition condition) {
        if(null == instance) {
            synchronized (Region.class) {
                if(null == instance) {
                    instance = new Region();
                }
            }
        }
        return instance;
    }

    /**
     * 根据指定条件查找对象集合
     *
     * @param condition 条件
     * @return 对象集合
     */
    public static ICollection<Region> finds(Condition condition) {
        return new Set<Region>(find(null));
    }
}
