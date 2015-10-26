package com.slfuture.angel.wechart.utility;

/**
 * 信誉体系
 */
public class Credit {
    /**
     * 信誉等级
     */
    public final static String LEVEL_VERYLOW = "很低";
    public final static String LEVEL_LOW = "较低";
    public final static String LEVEL_MIDLOW = "中下";
    public final static String LEVEL_MID = "中等";
    public final static String LEVEL_MIDHIGH = "中上";
    public final static String LEVEL_HIGH = "较高";
    public final static String LEVEL_VERYHIGH = "很高";
    /**
     * 信誉分数
     */
    public final static int SCORE_MID = 60;
    /**
     * 操作信誉影响
     */
    public final static int EFFECT_LAUNCH_GIVEUP = -2;
    public final static int EFFECT_LAUNCH_CANCEL = -5;
    public final static int EFFECT_LAUNCH_INTERRUPT = -10;
    public final static int EFFECT_LAUNCH_DEAL = +9;
    public final static int EFFECT_BID_GIVEUP = -4;
    public final static int EFFECT_BID_CANCEL = -8;
    public final static int EFFECT_BID_INTERRUPT = -10;
    public final static int EFFECT_BID_PAY = +5;
    public final static int EFFECT_BID_DEAL = +9;
    public final static int EFFECT_USER_VISIT = +2;


    /**
     * 获取信誉等级
     *
     * @param credit 信誉值
     * @return 信誉等级
     */
    public static String getLevel(int credit) {
        if(credit < 25) {
            return LEVEL_VERYLOW;
        }
        else if(credit < 35) {
            return LEVEL_LOW;
        }
        else if(credit < 45) {
            return LEVEL_MIDLOW;
        }
        else if(credit < 55) {
            return LEVEL_MID;
        }
        else if(credit < 65) {
            return LEVEL_MIDHIGH;
        }
        else if(credit < 75) {
            return LEVEL_HIGH;
        }
        else {
            return LEVEL_VERYHIGH;
        }
    }

    /**
     * 判断指定信誉值是否可执行指定操作
     */
    public static boolean canVisit(int credit) {
        return credit >= 20;
    }
    public static boolean canBid(int credit) {
        return credit >= 30;
    }
    public static boolean canLaunch(int credit) {
        return credit >= 40;
    }
}
