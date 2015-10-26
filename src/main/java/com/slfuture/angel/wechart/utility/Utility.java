package com.slfuture.angel.wechart.utility;

import com.slfuture.carrie.base.time.DateTime;

import java.text.ParseException;

/**
 * 基础工具类
 */
public class Utility {
    /**
     * 隐藏构造函数
     */
    private Utility() {
    }

    /**
     * 判断字符串是不是日期
     *
     * @param text 字符串
     * @return 是不是日期
     */
    public static boolean isDateTime(String text) {
        if(null == text) {
            return false;
        }
        try {
            DateTime.parse(text);
        }
        catch (ParseException ex) {
            return false;
        }
        return true;
    }

    /**
     * 将字符串转换为日期对象
     *
     * @param text 字符串
     * @return 日期对象
     */
    public static DateTime parseDateTime(String text) {
        if(null == text) {
            return null;
        }
        try {
            return DateTime.parse(text);
        }
        catch (ParseException ex) {
            return null;
        }
    }
}
