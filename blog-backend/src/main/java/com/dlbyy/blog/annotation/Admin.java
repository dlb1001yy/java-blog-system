package com.dlbyy.blog.annotation;

import java.lang.annotation.*;

/**
 * 后台操作审计注解，标注于后台写接口，由 OperationLogAspect 切面记录操作日志
 * <p>
 * 配合 {@code OperationLogAspect} 切面，记录操作人、请求方法、URI、参数、
 * 客户端 IP、执行结果与耗时，异步落库至 sys_operation_log 表。
 *
 * <pre>
 * 示例：
 * {@code @Admin("新增文章")}
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Admin {

    /**
     * 操作描述，建议使用具体中文描述（如“新增文章”、“删除分类”）
     */
    String value() default "";
}
