package com.tree.backend.aop;

import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 请求响应日志 AOP
 * 这个拦截器主要用于记录每个请求的开始和结束，以及请求的执行时间。
 * @Aspect: 这是一个AspectJ切面，用于定义切面。
 * @Component: 将该类声明为Spring的一个组件，由Spring进行管理。
 * @Slf4j: 使用Lombok注解，自动生成Slf4j日志。
 **/
@Aspect
@Component
@Slf4j
public class LogInterceptor {

    /**
     * 执行拦截
     * @Around: 这是一个环绕通知，表示在目标方法执行前后都会执行。
     *
     * StopWatch: 用于计时，记录方法执行时间。
     * RequestContextHolder: Spring提供的用于获取当前请求上下文的工具类。
     * 生成唯一请求ID（UUID）和获取请求路径。
     * 记录请求参数和请求日志。
     * 执行目标方法（即被拦截的Controller方法）。
     * 记录响应日志，包括请求ID和方法执行时间。
     */
    @Around("execution(* com.tree.backend.controller.*.*(..))")
    public Object doInterceptor(ProceedingJoinPoint point) throws Throwable {
        // 计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 获取请求路径
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 生成请求唯一 id
        String requestId = UUID.randomUUID().toString();
        String url = httpServletRequest.getRequestURI();
        // 获取请求参数
        Object[] args = point.getArgs();
        String reqParam = "[" + StringUtils.join(args, ", ") + "]";
        // 输出请求日志
        log.info("request start，id: {}, path: {}, ip: {}, params: {}", requestId, url,
                httpServletRequest.getRemoteHost(), reqParam);
        // 执行原方法
        Object result = point.proceed();
        // 输出响应日志
        stopWatch.stop();
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        log.info("request end, id: {}, cost: {}ms", requestId, totalTimeMillis);
        return result;
    }
}

