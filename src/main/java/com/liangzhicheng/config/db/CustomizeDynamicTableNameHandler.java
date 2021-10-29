package com.liangzhicheng.config.db;

import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;

import java.util.HashMap;
import java.util.Random;

/**
 * 动态表名处理器
 */
public class CustomizeDynamicTableNameHandler {

    public DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor(){
        DynamicTableNameInnerInterceptor interceptor = new DynamicTableNameInnerInterceptor();
        HashMap<String, TableNameHandler> resultMap = new HashMap<>();
        //对于user2表，进行动态表名设置
        resultMap.put("xxx_user2", (sql, tableName) -> {
            String value = "_";
            int random = new Random().nextInt(2) + 1;
            return tableName + value + random; //若返回null，则不会进行动态表名替换，还是会使用user2
        });
        interceptor.setTableNameHandlerMap(resultMap);
        return interceptor;
    }

}
