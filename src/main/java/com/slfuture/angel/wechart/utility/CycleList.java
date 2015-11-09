package com.slfuture.angel.wechart.utility;

/**
 * 循环链表
 */
public class CycleList<T> {
    /**
     * 链表元素
     */
    public class Item<T> {
        /**
         * 元素对象
         */
        public T item;
        /**
         * 索引号
         */
        public int index = 0;
    }


    /**
     * 链表数组
     */
    private Item[] list = null;
    /**
     * 下一个要插入的元素位置
     */
    private int index = 0;


    /**
     * 构造函数
     *
     * @param length 链表长度
     */
    public CycleList(int length) {
        list = new Item[length];
    }

    /**
     * 插入元素
     *
     * @param item 元素对象
     */
    public void push(T item) {
        list.
    }
}
