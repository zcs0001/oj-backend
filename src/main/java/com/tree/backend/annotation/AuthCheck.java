package com.tree.backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 * 使用@interface自定义注解
 * @Target(ElementType.METHOD): 这表示该注解可以应用在方法上，用于描述方法级别的信息。
 * @Retention(RetentionPolicy.RUNTIME): 这表示注解将在运行时保留，这样可以通过反射机制在运行时获取到注解信息。
 * 使用方法：在方法前使用@AuthCheck(mustRole = "ADMIN")，这样，在调用方法之前，系统会器检查当前用户是否具备 "ADMIN" 角色的权限。
 *  * 是通过AuthCheck结合使用AOP的拦截来实现权限校验功能的
 */

/**
 * 权限校验
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须有某个角色
     */
    String mustRole() default "";

}

