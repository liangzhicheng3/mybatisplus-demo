package com.liangzhicheng.config.db;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictFillStrategy(metaObject, "createTime", LocalDateTime::now);
        this.strictFillStrategy(metaObject, "updateTime", LocalDateTime::now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictFillStrategy(metaObject, "updateTime", LocalDateTime::now);
    }

}
