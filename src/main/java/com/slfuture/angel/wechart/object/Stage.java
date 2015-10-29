package com.slfuture.angel.wechart.object;

import com.slfuture.angel.wechart.structure.ActivitySnapshot;
import com.slfuture.angel.wechart.utility.DB;
import com.slfuture.carrie.base.async.Operator;
import com.slfuture.carrie.base.async.core.IOperation;
import com.slfuture.carrie.base.type.Record;
import com.slfuture.carrie.base.type.Set;
import com.slfuture.carrie.base.type.core.ICollection;
import com.slfuture.carrie.world.annotation.Method;
import com.slfuture.carrie.world.relation.Condition;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 秀台
 */
public class Stage {
    /**
     * 数据操作
     */
    public static class DataOperation implements IOperation<Void> {
        /**
         * 执行
         */
        public static void execute() {
            try {
                logger.debug("DataOperation.execute() execute");
                ConcurrentHashMap<String, ActivitySnapshot[]> newStageMap = new ConcurrentHashMap<String, ActivitySnapshot[]>();
                String sql = "SELECT CityID, Type FROM A_Activity WHERE Status = 4 AND PrepareTime > NOW() GROUP BY CityID, Type";
                ICollection<Record> recordSet = DB.executor().select(sql);
                for(Record record : recordSet) {
                    int cityId = record.getInteger("CityID", 0);
                    int type = record.getInteger("Type", 0);
                    //
                    sql = "SELECT A.ID, A.Title, A.Photos, A.Content, A.StartTime, A.Region, U.Career, IFNULL(UC.UserCount, 0) AS BidderCount FROM A_Activity A JOIN A_User U  ON A.AngelID = U.ID LEFT JOIN (SELECT ActivityID, COUNT(UserID) As UserCount FROM A_Bid WHERE Status IN (1, 3, 4, 6) AND (ExpireTime IS NULL OR ExpireTime > NOW()) GROUP BY ActivityID) UC ON A.ID = UC.ActivityID WHERE A.Type = " + type + " AND A.CityID = " + cityId + " AND A.Status = 4 AND A.PrepareTime > NOW() ORDER BY A.ID DESC LIMIT 10000";
                    ICollection<Record> snapshotRecordSet = DB.executor().select(sql);
                    int size = snapshotRecordSet.size();
                    ActivitySnapshot[] snapshots = new ActivitySnapshot[size];
                    int i = 0;
                    for(Record snapshotRecord : snapshotRecordSet) {
                        snapshots[i] = new ActivitySnapshot();
                        snapshots[i].id = snapshotRecord.getInteger("ID", 0);
                        snapshots[i].type = type;
                        snapshots[i].photos = snapshotRecord.getString("Photos");
                        snapshots[i].title = snapshotRecord.getString("Title");
                        snapshots[i].content = snapshotRecord.getString("Content");
                        snapshots[i].startTime = snapshotRecord.getDateTime("StartTime");
                        snapshots[i].cityId = cityId;
                        snapshots[i].region = snapshotRecord.getString("Region");
                        snapshots[i].career = snapshotRecord.getString("Career");
                        snapshots[i].popularity = (int) snapshotRecord.getLong("BidderCount", 0);
                        i++;
                    }
                    newStageMap.put(makeKey(cityId, type), snapshots);
                }
                stageMap = newStageMap;
                logger.debug("DataOperation.execute() finished");
            }
            catch(Exception ex) {
                logger.error("DataOperation.execute() execute failed", ex);
            }
        }

        /**
         * 操作结束回调
         *
         * @return 操作结果
         */
        @Override
        public Void onExecute() {
            while(true) {
                execute();
                try {
                    Thread.sleep(60000);
                }
                catch (InterruptedException e) {
                    logger.error("Thread.sleep(?) failed in Stage.DataOperation.onExecute()", e);
                    break;
                }
            }
            return null;
        }
    }


    /**
     * 日志对象
     */
    private static Logger logger = Logger.getLogger(Stage.class);
    /**
     * 实例对象
     */
    private static Stage instance = null;
    /**
     * 后台操作
     */
    private Operator<Void> operator = null;
    /**
     * 舞台映射
     */
    private static ConcurrentHashMap<String, ActivitySnapshot[]> stageMap = new ConcurrentHashMap<String, ActivitySnapshot[]>();


    /**
     * 搜索指定城市和分类的活动个数
     *
     * @param cityId 城市ID
     * @param type 分类ID
     * @return 活动个数
     */
    @Method
    public int getActivityCount(int cityId, int type) {
        String key = makeKey(cityId, type);
        ActivitySnapshot[] snapshots = stageMap.get(key);
        if(null == snapshots) {
            return 0;
        }
        return snapshots.length;
    }

    /**
     * 搜索指定城市和分类的活动快照
     *
     * @param cityId 城市ID
     * @param type 分类ID
     * @param index 起始索引
     * @param size 尺寸
     * @return 活动快照数组
     */
    @Method
    public ActivitySnapshot[] search(int cityId, int type, int index, int size) {
        ArrayList<ActivitySnapshot> list = new ArrayList<ActivitySnapshot>();
        String key = makeKey(cityId, type);
        ActivitySnapshot[] snapshots = stageMap.get(key);
        if(null == snapshots || 0 == snapshots.length) {
            return new ActivitySnapshot[0];
        }
        if(index >= snapshots.length) {
            return new ActivitySnapshot[0];
        }
        for(int i = 0; i < size && index < snapshots.length; i++) {
            list.add(snapshots[index++]);
        }
        return list.toArray(new ActivitySnapshot[0]);
    }

    /**
     * 获取活动缓存键
     *
     * @param cityId 城市ID
     * @param type 分类ID
     * @return 活动缓存键
     */
    private static String makeKey(int cityId, int type) {
        return cityId + "-" + type;
    }

    /**
     * 根据指定条件查找对象
     *
     * @param condition 条件
     * @return 对象
     */
    public static Stage find(Condition condition) {
        if(null == instance) {
            synchronized (Stage.class) {
                if(null == instance) {
                    instance = new Stage();
                    DataOperation.execute();
                    // 启动后台刷新
                    instance.operator = new Operator<Void>(new DataOperation());
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
    public static ICollection<Stage> finds(Condition condition) {
        return new Set<Stage>(find(condition));
    }
}
