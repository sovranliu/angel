package com.slfuture.angel.wechart.structure;

/**
 * 订单结果
 */
public class OrderResult {
    /**
     * 结果代码：成功
     */
    public final static int CODE_SUCCESS = 0;
    /**
     * 结果代码：重复支付
     */
    public final static int CODE_REPEATED = -1;
    /**
     * 结果代码：过期
     */
    public final static int CODE_EXPIRE = -2;


    /**
     * 结果代码
     */
    public int code = 0;
    /**
     * 订单号
     */
    public String tradeNo = null;
    /**
     * 预支付交易会话标识
     */
    public String prepayId = null;
    /**
     * 随机字符串
     */
    public String nonceString = null;
    /**
     * 签名方式
     */
    public String signType = "MD5";
    /**
     * 时间戳
     */
    public long timestamp = 0;
    /**
     * 签名
     */
    public String signature = null;


    /**
     * 属性
     */
    public int code() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public String tradeNo() {
        return tradeNo;
    }
    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }
    public String prepayId() {
        return prepayId;
    }
    public void setPrepayId(String prepayId) {
        this.prepayId = prepayId;
    }
    public String payPackage() {
        return "prepay_id=" + prepayId;
    }
    public String nonceString() {
        return nonceString;
    }
    public void setNonceString(String nonceString) {
        this.nonceString = nonceString;
    }
    public String signType() {
        return signType;
    }
    public void setSignType(String signType) {
        this.signType = signType;
    }
    public long timestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String signature() {
        return signature;
    }
    public void setSignature(String signature) {
        this.signature = signature;
    }
}
