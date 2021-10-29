package com.liangzhicheng;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liangzhicheng.entity.User;
import com.liangzhicheng.entity.User2;
import com.liangzhicheng.mappers.User2Mapper;
import com.liangzhicheng.mappers.UserMapper;
import com.liangzhicheng.service.IUserService;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test {

    @Resource
    private UserMapper userMapper;
    @Resource
    private User2Mapper user2Mapper;
    @Resource
    private IUserService userService;

    @org.junit.Test
    public void test1(){
        List<User> list = userMapper.selectList(null);
        list.forEach(System.out::println);
    }

    /**
     * 当某个表的列特别多，而select的时候只需要选取个别列，查询出的结果也没必要封装成Java实体类对象时
     * （只查部分列时，封装成实体后，实体对象中的很多属性会是null），
     * 则可以用selectMaps，获取到指定的列后，再自行进行处理即可
     */
    @org.junit.Test
    public void test2(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("id", "name", "email")
                .likeRight("name","黄");
        List<Map<String, Object>> mapList = userMapper.selectMaps(wrapper);
        mapList.forEach(System.out::println);
    }

    /**
     * 数据统计
     * 按照直属上级进行分组，查询每组的平均年龄，最大年龄，最小年龄
     */
    @org.junit.Test
    public void test3(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("manager_id", "AVG(age) avg_age", "MIN(age) min_age", "MAX(age) max_age")
                .groupBy("manager_id")
                .having("SUM(age) < {0}", 500);
        List<Map<String, Object>> mapList = userMapper.selectMaps(wrapper);
        mapList.forEach(System.out::println);
    }

    /**
     * 只会返回第一个字段（第一列）的值，其他字段会被舍弃
     */
    @org.junit.Test
    public void test4(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("id", "name")
                .like("name", "黄");
        List<Object> objectList = userMapper.selectObjs(wrapper);
        objectList.forEach(System.out::println);
    }

    /**
     * 查询满足条件的总数，注意，使用这个方法，不能调用QueryWrapper的select方法设置要查询的列。这个方法会自动添加select count(1)
     */
    @org.junit.Test
    public void test5(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like("name", "黄");
        Integer count = userMapper.selectCount(wrapper);
        System.out.println(count);
    }



    /**
     * getOne中第二参数指定为false，使得在查到了多行记录时，不抛出异常，而返回第一条记录
     */
    @org.junit.Test
    public void test6(){
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery();
        wrapper.gt(User::getAge, 28);
        User user = userService.getOne(wrapper, false);
        System.out.println(user);
    }

    /**
     * 链式调用
     */
    @org.junit.Test
    public void test7(){
        List<User> list = userService.lambdaQuery()
                .gt(User::getAge, 39)
                .likeRight(User::getName, "吴")
                .list();
        list.forEach(System.out::println);
    }
    @org.junit.Test
    public void test8(){
        userService.lambdaUpdate()
                .gt(User::getAge, 39)
                .likeRight(User::getName, "小")
                .set(User::getEmail, "w39@baomidou.com")
                .update();
    }
    @org.junit.Test
    public void test9(){
        userService.lambdaUpdate()
                .like(User::getName, "小菜")
                .remove();
    }

    /**
     * 名字中包含成，且年龄小于25
     * SELECT * FROM user WHERE name LIKE '%佳%' AND age < 25
     */
    public void test10(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like("name", "成")
                .lt("age", 25);
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(System.out::println);
    }

    /**
     * 姓名为黄姓，且年龄大于等于20，小于等于40，且email字段不为空
     * SELECT * FROM user WHERE name LIKE '黄%' AND age BETWEEN 20 AND 40 AND email IS NOT NULL
     */
    public void test11(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.likeRight("name", "黄")
                .between("age", 20, 40)
                .isNotNull("email");
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(System.out::println);
    }

    /**
     * 姓名为黄姓，或者年龄大于等于40，按照年龄降序排列，年龄相同则按照id升序排列
     * SELECT * FROM user WHERE name LIKE '黄%' OR age >= 40 ORDER BY age DESC, id ASC
     */
    public void test12(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.likeRight("name", "黄")
                .or()
                .ge("age", 40)
                .orderByDesc("age")
                .orderByAsc("id");
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(System.out::println);
    }

    /**
     * 创建日期为2021年3月22日，并且直属上级的名字为李姓
     * SELECT * FROM user WHERE DATE_FORMAT(create_time,'%Y-%m-%d') = '2021-03-22' AND manager_id IN (SELECT id FROM user WHERE name LIKE '李%')
     */
    public void test13(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
//        wrapper.apply("date_format(create_time, '%Y-%m-%d') = '2021-03-22'") //这种方式做字符串拼接，这个日期是一个外部参数时，这种方式有sql注入的风险
        wrapper.apply("DATE_FORMAT(create_time, '%Y-%m-%d') = {0}", "2021-03-22") //采用{index}这种方式动态传参，可防止sql注入
                .inSql("manager_id", "SELECT id FROM user WHERE name LIKE '李%'");
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(System.out::println);
    }

    /**
     * 名字为王姓，并且（年龄小于40，或者邮箱不为空）
     * SELECT * FROM user WHERE name LIKE '王%' AND (age < 40 OR email IS NOT NULL)
     */
    public void test14(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.likeRight("name", "王")
                .and(q -> q.lt("age", 40)
                        .or()
                        .isNotNull("email")
                );
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(System.out::println);
    }

    /**
     * 名字为王姓，或者（年龄小于40并且年龄大于20并且邮箱不为空）
     * SELECT * FROM user WHERE name LIKE '王%' OR (age < 40 AND age > 20 AND email IS NOT NULL)
     */
    public void test15(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.likeRight("name", "王")
                .or(q -> q.lt("age",40)
                        .gt("age",20)
                        .isNotNull("email")
                );
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(System.out::println);
    }

    /**
     * (年龄小于40或者邮箱不为空) 并且名字为王姓
     * SELECT * FROM user WHERE (age < 40 OR email IS NOT NULL) AND name LIKE '王%'
     */
    public void test16(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.nested(q -> q.lt("age", 40)
                        .or()
                        .isNotNull("email")
                )
                .likeRight("name", "王");
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(System.out::println);
    }

    /**
     * 年龄为30，31，34，35
     * SELECT * FROM user WHERE age IN (30,31,34,35)
     */
    public void test17(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.in("age", Arrays.asList(30,31,34,35));
        //或
        wrapper.inSql("age","30,31,34,35");
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(System.out::println);
    }

    /**
     * 年龄为30，31，34，35, 返回满足条件的第一条记录
     * SELECT * FROM user WHERE age IN (30,31,34,35) LIMIT 1
     */
    public void test18(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.in("age", Arrays.asList(30,31,34,35))
                .last("LIMIT 1");
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(System.out::println);
    }

    /**
     * 选出id, name, age, email, 等同于排除 manager_id 和 create_time
     * 当列特别多，而只需要排除个别列时，可以采用重载的select方法，指定需要排除的列
     */
    public void test19(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select(User.class, info -> {
            String columnName = info.getColumn();
            return !"create_time".equals(columnName) && !"manager_id".equals(columnName);
        });
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(System.out::println);
    }

    /**
     * 实体对象作为条件，改变等值匹配的行为，则可以在实体类中用@TableField注解进行配置
     * SqlCondition中提供的配置比较有限，当需要<或>等拼接方式，则需要自己定义
     */
    @org.junit.Test
    public void test20(){
        User user = new User();
        user.setName("梁");
        user.setAge(18);
        QueryWrapper<User> wrapper = new QueryWrapper<>(user);
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(System.out::println);
    }

    /**
     * allEq方法传入一个map，用来做等值匹配
     */
    @org.junit.Test
    public void test21(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        Map<String, Object> param = new HashMap<>();
        param.put("age", 40);
        param.put("name", "成成");
        wrapper.allEq(param);
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(System.out::println);
    }
    /**
     * 当allEq方法传入的Map中有value为null的元素时，默认会设置为is null
     */
    @org.junit.Test
    public void test22(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        Map<String, Object> param = new HashMap<>();
        param.put("age", 40);
        param.put("name", null);
        wrapper.allEq(param);
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(System.out::println);
    }
    /**
     * 若想忽略map中value为null的元素，可以在调用allEq时，设置参数boolean null2IsNull为false
     */
    @org.junit.Test
    public void test23(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        Map<String, Object> param = new HashMap<>();
        param.put("age", 40);
        param.put("name", null);
        wrapper.allEq(param, false);
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(System.out::println);
    }
    /**
     * 若想要在执行allEq时，过滤掉Map中的某些元素，可以调用allEq的重载方法allEq(BiPredicate<R, V> filter, Map<R, V> params)
     */
    @org.junit.Test
    public void test24(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        Map<String, Object> param = new HashMap<>();
        param.put("age", 40);
        param.put("name", "成成");
        wrapper.allEq((k,v) -> !"name".equals(k), param); // 过滤掉map中key为name的元素
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(System.out::println);
    }

    /**
     * lambda表达式
     */
    @org.junit.Test
    public void test25(){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(User::getName, "梁")
                .lt(User::getAge, 30);
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(System.out::println);
    }
    /**
     * lambda表达式（链式）
     */
    @org.junit.Test
    public void test26(){
        LambdaQueryChainWrapper<User> chainWrapper = new LambdaQueryChainWrapper<>(userMapper);
        List<User> list = chainWrapper
                .like(User::getName, "梁")
                .gt(User::getAge, 30)
                .list();
        list.forEach(System.out::println);
    }

    /**
     * 根据entity实体对象和wrapper条件构造器进行更新
     */
    @org.junit.Test
    public void test27(){
        User user = new User();
        user.setName("朱砂痣");
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.between(User::getAge, 26,31)
                .likeRight(User::getName,"吴");
        userMapper.update(user, wrapper);
    }
    /**
     * 根据entity实体对象传入wrapper，即用实体对象构造where条件
     */
    @org.junit.Test
    public void test28(){
        User whereUser = new User();
        whereUser.setAge(40);
        whereUser.setName("梁");
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>(whereUser);
        User user = new User();
        user.setEmail("share@baomidou.com");
        user.setManagerId(10L);
        userMapper.update(user, wrapper);
    }
    /**
     * lambda表达式（链式）
     */
    @org.junit.Test
    public void test29(){
        LambdaUpdateChainWrapper<User> wrapper = new LambdaUpdateChainWrapper<>(userMapper);
        wrapper.likeRight(User::getEmail, "share")
                .like(User::getName, "飞飞")
                .set(User::getEmail, "ff@baomidou.com")
                .update();
    }
    /**
     * 由于BaseMapper提供的2个更新方法都是传入一个实体对象去执行更新，这在需要更新的列比较多时还好，
     * 若想要更新的只有那么一列，或者两列，则创建一个实体对象就显得有点麻烦，
     * 针对这种情况，UpdateWrapper提供有set方法，可以手动拼接SQL中的SET语句，此时可以不必传入实体对象
     */
    @org.junit.Test
    public void test30(){
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.likeRight(User::getEmail, "share")
                .set(User::getManagerId, 9L);
        userMapper.update(null, wrapper);
    }



    /**
     * 分页查询，配置MybatisPlusInterceptor分页插件
     */
    @org.junit.Test
    public void test31(){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(User::getAge, 28);
        Page<User> page = new Page<>(1, 2);
//        Page<User> page = new Page<>(1, 2, false); //不查总记录数，仅查分页结果，第三个参数searchCount修改为false
        Page<User> userPage = userMapper.selectPage(page, wrapper);
        System.out.println("总记录数 = " + userPage.getTotal());
        System.out.println("总页数 = " + userPage.getPages());
        System.out.println("当前页码 = " + userPage.getCurrent());
        List<User> records = userPage.getRecords();
        records.forEach(System.out::println);
    }



    /**
     * ActiveRecord模式，通过操作实体对象，直接操作数据库表
     * 让实体类User继承自Model
     * 直接调用实体对象上的方法
     */
    @org.junit.Test
    public void test32(){
        User user = new User();
        user.setId(16L);
        user.setName("易成大大");
        user.setAge(18);
        user.setEmail("ar@baomidou.com");
        user.setManagerId(1L);
        user.setCreateTime(LocalDateTime.now());
        boolean result = user.insert();
        System.out.println(result);
    }
    @org.junit.Test
    public void test33(){
        User user = new User();
        user.setId(16L);
        boolean result = user.deleteById();
        System.out.println(result);
    }
    @org.junit.Test
    public void test34(){
        User user = new User();
        user.setId(16L);
        user.setName("易成666");
        boolean result = user.updateById();
        System.out.println(result);
    }
    @org.junit.Test
    public void test35(){
        User user = new User();
        user.setId(16L);
        User result = user.selectById();
        System.out.println(result);
    }



    /**
     * application.yml配置文件中可添加tablePrefix：添加表名前缀
     * Mysql中表修改xxx_user，但Java实体类保持不变
     */
    @org.junit.Test
    public void test36(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like("name", "易");
        Integer count = userMapper.selectCount(wrapper);
        System.out.println(count);
    }



    /**
     * 逻辑删除
     * 在application.yml中进行逻辑删除的相关配置
     * 在列表查询时会自动过滤逻辑已删除条件
     * 若想查询的列不包含逻辑删除字段，可在实体类中通过@TableField进行配置（@TableField(select = false)）
     * 若想对某些表进行单独配置逻辑删除，在实体类的对应字段上使用@TableLogic（@TableLogic(value = "0", delval = "1")）
     *
     * 开启mybatis-plus的逻辑删除后，会对sql产生的影响:
     * select语句：追加where条件，过滤掉已删除的数据
     * update语句：追加where条件，防止更新到已删除的数据
     * delete语句：转变为update语句
     *
     * 自定义sql不受逻辑删除配置影响
     */
    @org.junit.Test
    public void test37(){
        int id = user2Mapper.deleteById(7);
        System.out.println("row = " + id);
    }



    /**
     * 自动填充时间
     * 在实体类中时间字段上，通过@TableField设置自动填充
     * 实现自动填充处理器MybatisPlusMetaObjectHandler
     */
    @org.junit.Test
    public void test38() {
        User2 user = new User2();
        user.setId(8L);
        user.setName("易成333");
        user.setAge(18);
        user.setEmail("yd@baomidou.com");
        user.setManagerId(1L);
        user2Mapper.insert(user);
    }
    @org.junit.Test
    public void test39(){
        User2 user = new User2();
        user.setId(8L);
        user.setName("易成666");
        user.setAge(99);
        user2Mapper.updateById(user);
    }



    /**
     * 乐观锁机制
     * 配置MybatisPlusInterceptor乐观锁插件
     * 在实体类中表示版本的字段上添加注解@Version
     *
     * 仅支持updateById(id)与update(entity, wrapper)方法
     */
    @org.junit.Test
    public void test40(){
        int version = 1; //假设这个version是先前查询时获得
        User2 user = new User2();
        user.setId(8L);
        user.setEmail("version@baomidou.com");
        user.setVersion(version);
        int i = user2Mapper.updateById(user);
    }


    /**
     * 性能分析插件
     * 引入maven依赖（p6spy）
     * 修改application.yml配置文件：
     *                           url: jdbc:p6spy:mysql://...
     *                           driver-class-name: com.p6spy.engine.spy.P6SpyDriver #换成p6spy的驱动
     * 在src/main/resources资源目录下添加spy.properties
     */
    @org.junit.Test
    public void test41(){
        List<User2> list = user2Mapper.selectList(null);
        list.forEach(System.out::println);
    }



    /**
     * 多租户SQL处理，配置MybatisPlusInterceptor多租户插件
     */
    @org.junit.Test
    public void test42(){
        LambdaQueryWrapper<User2> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(User2::getName, "王")
                .select(User2::getName, User2::getAge, User2::getEmail, User2::getManagerId);
        user2Mapper.selectList(wrapper);
    }



    /**
     * 动态表名SQL处理，配置MybatisPlusInterceptor动态表名拦截器插件
     */
    @org.junit.Test
    public void test43(){
        user2Mapper.selectList(null);
    }

}
