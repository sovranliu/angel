package com.slfuture.angel.wechart.utility;

import com.slfuture.utility.aliyun.OCS;

/**
 * 缓存
 */
public class Cache {
    /**
     * 隐藏构造函数
     */
    private Cache() { }

    /**
     * 获取缓存数据
     *
     * @param category 分类
     * @param key 键
     * @return 缓存数据
     */
    public static Object get(String category, String key) {
        return OCS.get(category, key);
    }

    /**
     * 获取缓存数据
     *
     * @param category 分类
     * @param key 键
     * @param period 缓存时长
     * @param value 缓存数据
     */
    public static void set(String category, String key, int period, Object value) {
        OCS.set(category, key, period, value);
    }
}
