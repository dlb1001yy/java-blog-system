# 关于缺少 Mapper XML 文件的分析

## 问题分析

用户询问：`src/main\resources` 目录下缺少 mapper 文件，项目启动时会报错吗？

## 当前状态

1. **配置情况**：
   - `application.yaml` 中配置了 `mybatis-plus.mapper-locations: classpath*:/mapper/**/*.xml`
   - `src/main/resources` 目录下只有 `application.yaml`，没有 `mapper` 目录和 XML 文件

2. **Mapper 接口情况**：
   - 所有 Mapper 接口（ArticleMapper、CategoryMapper、UserMapper 等）都继承自 `BaseMapper<T>`
   - 所有接口都没有自定义方法，也没有 `@Select`、`@Insert` 等注解
   - 所有 CRUD 操作都由 MyBatis Plus 内置实现

## 结论

**项目启动不会报错。**

原因：
1. `mapper-locations` 配置只是告诉 MyBatis 在哪里查找 XML 文件，如果路径不存在或没有 XML 文件，MyBatis 会跳过，不会抛出异常
2. 所有 Mapper 接口都继承 `BaseMapper<T>`，基础的 CRUD 操作（insert、update、delete、select 等）由 MyBatis Plus 自动实现，不需要 XML 文件
3. 没有自定义 SQL 语句需要 XML 文件来映射

## 建议

如果后续需要自定义 SQL 查询（如复杂的多表联查），可以：
1. 在 `src/main/resources/mapper/` 目录下创建对应的 XML 文件
2. 或者在 Mapper 接口方法上使用 `@Select`、`@Insert` 等注解

当前项目配置可以正常运行，无需额外创建 XML mapper 文件。
