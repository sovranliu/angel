package com.slfuture.angel.wechart.object;

import com.slfuture.angel.wechart.utility.Credit;
import com.slfuture.angel.wechart.utility.DB;
import com.slfuture.angel.wechart.utility.EmojiFilter;
import com.slfuture.carrie.base.type.Set;
import com.slfuture.carrie.base.type.core.ICollection;
import com.slfuture.carrie.world.annotation.Method;
import com.slfuture.carrie.world.relation.Condition;

/**
 * 接待员
 */
public class Waiter {
    /**
     * 实例对象
     */
    private static Waiter instance = null;


    /**
     * 注册用户
     *
     * @param nickName 昵称
     * @param gender 性别
     * @param wechartHead 微信头像URL
     * @param openId 开放ID
     * @param isFans 是否粉丝
     * @param source 用户来源
     * @return 注册用户ID
     */
    @Method
    public int register(String nickName, int gender, String wechartHead, String openId, boolean isFans, String source) {
        String sql = "INSERT INTO A_User (NickName, Gender, Credit, Photos, Career, OpenID, IsFans, Source, AddTime, UpdateTime) VALUES ('" + EmojiFilter.filter(nickName) + "', " + gender + ", "  + Credit.SCORE_MID + ", '" + wechartHead + "', '', '" + openId + "', " + isFans + ", '" + source + "', NOW(), NOW())";
        Long result = DB.executor().insert(sql);
        return (int) (long) result;
    }

    /**
     * 根据指定条件查找对象
     *
     * @param condition 条件
     * @return 对象
     */
    public static Waiter find(Condition condition) {
        if(null == instance) {
            synchronized (Waiter.class) {
                if(null == instance) {
                    instance = new Waiter();
                }
            }
        }
        return instance;
    }

    /**
     * 根据指定条件查找对象列表
     *
     * @param condition 条件
     * @return 对象列表
     */
    public static ICollection<Waiter> finds(Condition condition) {
        return new Set<Waiter>(find(condition));
    }
}
