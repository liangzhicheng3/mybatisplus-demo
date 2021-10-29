package com.liangzhicheng.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = false)
@Data
public class User extends Model<User> {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 名称
     */
    @TableField(condition = SqlCondition.LIKE)
    private String name;

    /**
     * 年龄
     */
    @TableField(condition = "%s &gt; #{%s}") //这里相当于大于，其中&gt;是字符实体
    private Integer age;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 直属上级id
     */
    private Long managerId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
