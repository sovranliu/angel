package com.slfuture.angel.wechart.object;

import com.slfuture.angel.wechart.structure.RoomChart;
import com.slfuture.angel.wechart.utility.DB;
import com.slfuture.carrie.base.time.DateTime;
import com.slfuture.carrie.base.type.Record;
import com.slfuture.carrie.base.type.Set;
import com.slfuture.carrie.base.type.core.ICollection;
import com.slfuture.carrie.world.IObject;
import com.slfuture.carrie.world.World;
import com.slfuture.carrie.world.annotation.Method;
import com.slfuture.carrie.world.annotation.Property;
import com.slfuture.carrie.world.relation.Condition;
import com.slfuture.carrie.world.relation.prepare.PropertyPrepare;
import com.slfuture.utility.aliyun.OCS;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 广告
 */
public class Room {
    /**
     * 条件集合
     */
    private static Condition CONDITION_CITYID = null;
    /**
     * 城市ID与房间映射
     */
    private static ConcurrentHashMap<Integer, Room> rooms = new ConcurrentHashMap<Integer, Room>();

    static {
        CONDITION_CITYID = new Condition();
        CONDITION_CITYID.prepareSelf = new PropertyPrepare("cityId");
        //
        ICollection<Record> records = DB.executor().select("SELECT * FROM A_Room");
        rooms.clear();
        for(Record record : records) {
            Room room = new Room();
            room.id = record.getInteger("ID");
            room.cityId = record.getInteger("CityID");
            rooms.put(room.cityId, room);
        }
    }

    /**
     * ID
     */
    @Property
    public int id;
    /**
     * 城市ID
     */
    @Property
    public int cityId;
    /**
     * 链接地址
     */
    public int index = 0;
    /**
     * 图片地址
     */
    public ConcurrentLinkedQueue<RoomChart> charts = new ConcurrentLinkedQueue<RoomChart>();


    /**
     * 说话
     *
     * @param chartId 会话ID
     * @param userId 用户ID
     * @param content 内容
     * @return 对话内容
     */
    @Method
    public RoomChart[] speak(int chartId, int userId, String content) {
        String info = (String) OCS.get("Room-Info", String.valueOf(userId));
        String nickName = "";
        String photo = "";
        if(null == info) {
            Condition condition = new Condition();
            condition.prepareSelf = new PropertyPrepare("id");
            condition.target = userId;
            IObject user = World.get("User", condition, IObject.class);
            if(null != user) {
                nickName = (String) user.property("nickName");
                com.slfuture.carrie.base.model.Method method = new com.slfuture.carrie.base.model.Method();
                method.name = "primaryPhoto";
                method.parameters = new Class<?>[0];
                photo = (String) user.invoke(method);
            }
        }
        else {
            int i = info.lastIndexOf(",");
            if(-1 == i) {
                nickName = info;
            }
            else {
                nickName = info.substring(0, i);
                photo = info.substring(i + 1);
            }
        }
        RoomChart roomChart = new RoomChart();
        roomChart.id = ++index;
        roomChart.userId = userId;
        roomChart.nickName = nickName;
        roomChart.photo = photo;
        roomChart.content = content;
        roomChart.time = DateTime.now();
        charts.push(roomChart);
        return listen(chartId);
    }

    /**
     * 收听
     *
     * @param chartId 会话ID
     * @return 对话内容
     */
    @Method
    public RoomChart[] listen(int chartId) {
        return null;
    }

    /**
     * 根据指定条件查找对象
     *
     * @param condition 条件
     * @return 对象
     */
    public static Room find(Condition condition) {
        if(CONDITION_CITYID.equalsIgnoreTarget(condition)) {
            int cityId = (Integer) CONDITION_CITYID.target;
            Room room = rooms.get(cityId);
            if(null == room) {
                room = new Room();
                room.cityId = cityId;
            }
            return room;
        }
        return null;
    }

    /**
     * 根据指定条件查找对象集合
     *
     * @param condition 条件
     * @return 对象集合
     */
    public static ICollection<Room> finds(Condition condition) {
        return new Set<Room>();
    }
}
