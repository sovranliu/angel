package com.slfuture.angel.wechart.utility;

import com.slfuture.carrie.base.type.Table;
import com.slfuture.carrie.utility.config.Configuration;
import com.slfuture.carrie.utility.config.core.IConfig;
import com.slfuture.carrie.utility.template.Context;
import com.slfuture.carrie.utility.template.VelocityTemplate;

/**
 * 渲染器
 */
public class Render {
    /**
     * 实例对象
     */
    private static Render instance = null;
    /**
     * 模板映射
     */
    private Table<String, VelocityTemplate> templateMap = null;


    /**
     * 隐藏构造函数
     */
    private Render() {
    }

    /**
     * 获取实例对象
     *
     * @return 实例对象
     */
    public static Render instance() {
        if(null == instance) {
            synchronized (Render.class) {
                if(null == instance) {
                    instance = new Render();
                    instance.templateMap = new Table<String, VelocityTemplate>();
                    for(IConfig conf : Configuration.root().visits("/logic/templates/template")) {
                        VelocityTemplate template = new VelocityTemplate();
                        template.setContent(conf.get(null));
                        instance.templateMap.put(conf.getString("id"), template);
                    }
                }
            }
        }
        return instance;
    }

    /**
     * 渲染
     *
     * @param id 模板ID
     * @param context 上下文
     * @return 返回内容
     */
    public String render(String id, Context context) {
        return templateMap.get(id).render(context);
    }
}
