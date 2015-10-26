package com.slfuture.angel.wechart.utility;

import com.slfuture.carrie.utility.config.Configuration;
import com.slfuture.carrie.utility.db.DBExecutor;
import com.slfuture.carrie.world.invoker.DBInvoker;


/**
 * 数据库
 */
public class DB {
    /**
     * 数据库执行器
     */
    private static DBExecutor executor = null;


    /**
     * 隐藏构造函数
     */
    private DB() { }

    /**
     * 获取执行器
     *
     * @return 执行器
     */
    public static DBExecutor executor() {
        if(null == executor) {
            synchronized (DB.class) {
                if(null == executor) {
                    executor = DBInvoker.instance().executor(Configuration.root().visit("/logic/db").get("name"));
                }
            }
        }
        return executor;
    }
}
