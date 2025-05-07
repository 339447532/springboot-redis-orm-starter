# 如何使用 springboot-redis-orm-starter

这个 Redis ORM Starter 是一个用于 Spring Boot 项目的启动器，它提供了一种简单的方式来使用 Redis 进行对象关系映射。下面是使用这个
starter 的详细步骤：

## 1. 添加依赖

首先，在你的 Spring Boot 项目的 pom.xml 文件中添加以下依赖：

```xml
<dependency>
    <groupId>com.asd.redis.orm</groupId>
    <artifactId>springboot-redis-orm-starter</artifactId>
    <version>2.5.15</version>
</dependency>
```

## 2. 配置 Redis

在你的 Spring Boot 项目的 application.properties 或 application.yml 文件中添加以下配置：

```properties
# Redis 连接配置
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=
spring.redis.database=0
# Redis ORM 配置
redis.orm.key-prefix=myapp:  # 键前缀，默认为空
redis.orm.default-expire-time=3600  # 默认过期时间（秒），-1表示永不过期
redis.orm.enable-cache=true  # 是否启用缓存
redis.orm.cache-size=1000  # 缓存大小
```

## 3. 创建实体类

创建一个使用 Redis ORM 注解的实体类：

```java
package com.example.entity;

import annotation.com.asd.redis.orm.RedisEntity;
import annotation.com.asd.redis.orm.RedisField;
import annotation.com.asd.redis.orm.RedisId;

@RedisEntity(prefix = "user", expire = 3600)
public class User {

    @RedisId(type = RedisId.IdType.AUTO)  // 自动生成ID
    private Long id;

    @RedisField
    private String username;

    @RedisField
    private String email;

    @RedisField(ignore = true)  // 忽略该字段，不会存储到Redis
    private transient String tempField;

    // getter 和 setter 方法
    // ...
}
```

## 4. 启用 Redis Mapper 扫描

在你的主应用类或配置类上添加 @RedisMapperScan 注解，以启用 Redis Mapper 扫描：

```java
package com.example;

import annotation.com.asd.redis.orm.RedisMapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RedisMapperScan(basePackages = "com.asd.redis.orm.mapper")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 5. 使用 RedisOrmTemplate

直接注入 RedisOrmTemplate 并使用其方法：

```java
package com.example.service;

import com.asd.redis.orm.model.Page;
import com.example.entity.User;
import core.com.asd.redis.orm.RedisOrmTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private RedisOrmTemplate redisOrmTemplate;

    // 保存用户
    public User saveUser(User user) {
        return redisOrmTemplate.save(user);
    }

    // 批量保存用户
    public List<User> saveUsers(List<User> users) {
        return redisOrmTemplate.saveBatch(users);
    }

    // 根据ID获取用户
    public User getUserById(Long id) {
        return redisOrmTemplate.getById(User.class, id);
    }

    // 批量获取用户
    public List<User> getUsersByIds(List<Long> ids) {
        return redisOrmTemplate.listByIds(User.class, ids);
    }

    // 更新用户
    public boolean updateUser(User user) {
        return redisOrmTemplate.updateById(user);
    }

    // 删除用户
    public boolean deleteUser(Long id) {
        return redisOrmTemplate.removeById(User.class, id);
    }

   // 分页查询
   public model.com.asd.redis.orm.Page<User> getUserPage(long current, long size) {
      return redisOrmTemplate.page(User.class, current, size);
   }

   // 计数
   public long countUsers() {
      return redisOrmTemplate.count(User.class);
   }

   // 根据条件查询用户（带排序）
   public List<User> getUsersByCondition(User condition, String orderBy, boolean isAsc) {
      if (condition == null) {
         List<User> users = redisOrmTemplate.list(User.class);
         return redisOrmTemplate.sort(users, orderBy, isAsc);
      }
      return redisOrmTemplate.listByCondition(User.class, condition, orderBy, isAsc);
   }

   // 分页查询（带排序）
   public Page<User> getUserPage(long current, long size, String orderBy, boolean isAsc) {
      return redisOrmTemplate.page(User.class, current, size, orderBy, isAsc);
   }

   // 条件分页查询（带排序）
   public Page<User> getUserPageByCondition(User condition, long current, long size, String orderBy, boolean isAsc) {
      return redisOrmTemplate.pageByCondition(User.class, condition, current, size, orderBy, isAsc);
   }

}
``` 

## 主要功能

1. 实体注解 ：

    - @RedisEntity ：标记类为 Redis 实体，可设置键前缀和过期时间
    - @RedisId ：标记字段为实体 ID，支持 UUID、自动递增和手动输入三种方式 如：@RedisId(type = RedisId.IdType.AUTO)自动生成ID
    - @RedisField ：标记字段为实体属性，可设置是否忽略 @RedisField(ignore = true)

2. 核心操作 ：

   - 保存实体： save(entity)
   - 批量保存： saveBatch(entities)
   - 根据 ID 查询： getById(entityClass, id)
   - 批量查询： listByIds(entityClass, ids)
   - 更新实体： updateById(entity)
   - 批量更新： updateBatchById(entities)
   - 删除实体： removeById(entityClass, id)
   - 批量删除： removeByIds(entityClass, ids)
   - 分页查询： page(entityClass, current, size)
   - 计数： count(entityClass)
   - 条件对象查询列表： selectByCondition(entity)
   - 条件对象查询列表（带排序）： selectByCondition(entity, orderBy, isAsc)
   - 条件对象查询分页： selectPageByCondition(entity, current, size)
   - 条件对象查询分页（带排序）： selectPageByCondition(entity, current, size, orderBy, isAsc)
   - 条件对象查询总记录数： selectCountByCondition(entity)

3. 配置选项 ：

   - redis.orm.key-prefix ：键前缀
   - redis.orm.default-expire-time ：默认过期时间
   - redis.orm.enable-cache ：是否启用缓存
   - redis.orm.cache-size ：缓存大小

4. 排序功能：

   - 支持对查询结果进行排序
   - 可以指定任意字段作为排序字段
   - 支持升序（ASC）和降序（DESC）排序
   - 排序示例：
     ```java
     // 按年龄升序查询用户
     List<User> users = userMapper.selectByCondition(null, "age", true);
     
     // 按姓名升序查询用户，再按年龄降序排序
     List<User> users = userMapper.selectByCondition(null, "name", true);
     users = userMapper.sort(users, "age", false);
     
     // 按用户名升序查询匹配条件的用户
     User condition = new User();
     condition.setStatus("active");
     List<User> activeUsers = getUsersByCondition(condition, "username", true);
    
     // 按最后登录时间降序分页查询匹配条件的用户
   Page<User> activePage = getUserPageByCondition(condition, 1, 10, "lastLoginTime", false);
     ```
   - 排序特性：
     - 支持所有可比较类型（实现了Comparable接口的类型）
     - 对于不可比较的类型，使用toString()结果进行比较
     - 空值处理（null值会根据排序方向被放置在最前或最后）
     - 支持与分页查询结合使用

这个 starter 提供了类似于 MyBatis-Plus 的操作体验，但是针对 Redis 数据库，使得在 Spring Boot 项目中使用 Redis
进行对象存储变得简单高效。新增的排序功能让数据查询更加灵活，能够满足更多的业务场景需求。
