package com.tree.backend.aop;

import com.tree.backend.common.ErrorCode;
import com.tree.backend.service.UserService;
import com.tree.backend.annotation.AuthCheck;
import com.tree.backend.exception.BusinessException;
import com.tree.backend.model.entity.User;
import com.tree.backend.model.enums.UserRoleEnum;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @Aspect: 声明这是一个切面类。
 * 在Spring AOP中，@Around 注解用于定义环绕通知（Around Advice），而 @Aspect 注解用于标识一个类为切面类。
 * 切面类（Aspect）是一个包含 切点 和 通知 的类，而通知可以是前置通知（@Before）、后置通知（@After）、环绕通知（@Around）等。
 * @Around 注解用于定义环绕通知，它能够在目标方法执行前后都进行处理，包括决定是否继续执行目标方法。
 *
 *
 * @Component: 将该类标记为 Spring 的组件，使其能够被自动扫描并纳入 Spring 容器管
 * 这个拦截器主要用于在方法执行前进行权限校验，确保调用该方法的用户具备指定的权限。
 * 这是通过在 AOP 切面中使用 @Around 注解实现的，拦截了带有 @AuthCheck 注解的方法，并在执行前进行了权限检查。
 */

/**
 * 权限校验 AOP
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     * @Around("@annotation(authCheck)"): 通过 @Around 注解声明一个环绕通知，拦截带有 @AuthCheck 注解的方法。
     * @annotation(authCheck) 表示匹配带有 AuthCheck 注解的方法。
     * ProceedingJoinPoint joinPoint: 代表被拦截的方法。
     * AuthCheck authCheck: 通过方法参数获取 @AuthCheck 注解的实例，以便获取其中的权限信息。
     *
     * 从当前请求的上下文中获取 HttpServletRequest 对象。
     * 通过 userService.getLoginUser(request) 获取当前登录用户的信息。
     * 获取 AuthCheck 注解中定义的必须具备的角色 mustRole。
     * 如果 mustRole 不为空，说明该方法需要进行权限校验，根据用户的角色进行校验。
     * 如果用户角色不满足要求，抛出 BusinessException 异常，表示权限不足，返回错误码 ErrorCode.NO_AUTH_ERROR。
     * 如果权限校验通过，调用 joinPoint.proceed() 继续执行被拦截的方法。
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 必须有该权限才通过
        if (StringUtils.isNotBlank(mustRole)) {
            UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
            if (mustUserRoleEnum == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            String userRole = loginUser.getUserRole();
            // 如果被封号，直接拒绝
            if (UserRoleEnum.BAN.equals(mustUserRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 必须有管理员权限
            if (UserRoleEnum.ADMIN.equals(mustUserRoleEnum)) {
                if (!mustRole.equals(userRole)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
                }
            }
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}

