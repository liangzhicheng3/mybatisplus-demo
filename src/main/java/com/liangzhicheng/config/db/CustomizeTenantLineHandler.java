package com.liangzhicheng.config.db;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

import java.util.ArrayList;
import java.util.List;

/**
 * 多租户处理器
 */
public class CustomizeTenantLineHandler implements TenantLineHandler {

    /**
     * 返回租户id的值，固定写为1，一般是从当前上下文中取出一个租户id（cookie、token、缓存）
     * @return Expression
     */
    @Override
    public Expression getTenantId() {
        return new LongValue(1L);
    }

    /**
     * 通常会将表示租户id的列名，需要排除租户id的表等信息，封装到一个配置类中（如TenantConfig）
     * @return String
     */
    @Override
    public String getTenantIdColumn() {
        //返回表中的表示租户id的列名
        return "manager_id";
    }

    /**
     * default方法，默认返回false表示所有表都需要拼多租户条件
     * @param tableName
     * @return boolean
     */
    @Override
    public boolean ignoreTable(String tableName) {
        return !this.listTableName().contains(tableName);
    }

    /**
     * 配置表名采用多租户过滤
     * @return
     */
    private List<String> listTableName(){
        List<String> resultList = new ArrayList<String>();
        resultList.add("xxx_user2"); //表名不为user2的表，不拼接多租户条件
        return resultList;
    }

}
