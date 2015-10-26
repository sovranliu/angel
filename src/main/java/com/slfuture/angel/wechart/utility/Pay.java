package com.slfuture.angel.wechart.utility;

import com.slfuture.carrie.base.async.PipeLine;
import com.slfuture.carrie.base.async.core.IOperation;
import com.slfuture.carrie.base.model.Method;
import com.slfuture.carrie.world.IObject;
import com.slfuture.carrie.world.World;
import com.slfuture.carrie.world.relation.Condition;
import com.slfuture.carrie.world.relation.prepare.PropertyPrepare;
import com.slfuture.utility.wechart.pay.Payment;
import com.slfuture.utility.wechart.pay.structure.PayResult;
import org.apache.log4j.Logger;

/**
 * 支付相关
 */
public class Pay {
    /**
     * 日志对象
     */
    private static Logger logger = Logger.getLogger(Pay.class);
    /**
     * 支付百分比0-100之间
     */
    public final static int PAY_PERCENT = 90;
    /**
     * 处理流
     */
    public static PipeLine pipeLine = null;


    /**
     * 获取处理流对象
     *
     * @return 处理流对象
     */
    public static PipeLine pipeLine() {
        if(null != pipeLine) {
            return pipeLine;
        }
        synchronized (Pay.class) {
            if(null != pipeLine) {
                return pipeLine;
            }
            pipeLine = new PipeLine();
            pipeLine.start(1, 1000);
        }
        return pipeLine;
    }

    /**
     * 获取总价中需要支付的金额
     *
     * @param amount 总价
     * @return 支付金额
     */
    public static int getPayAmount(int amount) {
        if(amount <= 0) {
            return 0;
        }
        return amount * PAY_PERCENT / 100;
    }

    /**
     * 处理通知回调
     *
     * @param data 通知数据
     */
    public static void doPay(String data) {
        if(null == data) {
            return;
        }
        PayResult payResult = Payment.onPay(data);
        if(null == payResult) {
            return;
        }
        if (PayResult.CODE_SUCCESS != payResult.code) {
            return;
        }
        Condition condition = new Condition();
        condition.prepareSelf = new PropertyPrepare("openId");
        condition.target = payResult.openId;
        IObject bidder = World.get("User", condition, IObject.class);
        if(null == bidder) {
            logger.error("no bidder be found in Pay.onPay(?)\ndata = " + data);
            return;
        }
        int bidderId = (Integer) bidder.property("id");
        //
        condition = new Condition();
        condition.prepareSelf = new PropertyPrepare("userId");
        condition.target = bidderId;
        Condition bidCondition = new Condition();
        bidCondition.prepareSelf = new PropertyPrepare("activityId");
        bidCondition.target = payResult.productId;
        bidCondition.put(true, condition);
        IObject bid = World.get("Bid", bidCondition, IObject.class);
        if(null == bid) {
            logger.error("no bid be found in Pay.onPay(?)\ndata = " + data);
            return;
        }
        Method method = new Method("onPay", new Class<?>[]{int.class});
        bid.invoke(method, payResult.amount);
    }

    /**
     * 通知回调
     *
     * @param data 通知数据
     */
    public static void onPay(final String data) {
        logger.info("Pay.onPay(?)\ndata = " + data);
        final String oData = data;
        try {
            pipeLine().supply(new IOperation<Void>() {
                @Override
                public Void onExecute() {
                    doPay(oData);
                    return null;
                }
            });
        }
        catch(Exception ex) {
            logger.error("Pay.onPay(" + oData + ") execute failed", ex);
        }
    }
}
