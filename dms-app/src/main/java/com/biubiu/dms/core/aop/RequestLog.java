package com.biubiu.dms.core.aop;


import com.alibaba.fastjson.JSONObject;
import com.biubiu.dms.core.annotation.SystemLog;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.net.URLDecoder;

@Component
@Aspect
public class RequestLog {


    public static final Logger logger = LoggerFactory.getLogger(RequestLog.class);

    /**
     * Define a pointcut
     */
    @Pointcut("@annotation(com.biubiu.dms.core.annotation.SystemLog)")
    public void controllerLog() {}

    /**
     * Print Log before controller
     * @param joinPoint
     */
    @Before("controllerLog()")
    public void before(JoinPoint joinPoint) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        logger.info("请求IP：{}", request.getRemoteAddr());
        logger.info("请求路径：{}", URLDecoder.decode(request.getRequestURL().toString(), "UTF-8"));
        logger.info("请求方式：{}", request.getMethod());
        logger.info("方法描述：{}", getMethodDescription(joinPoint));
        logger.info("请求参数：{}", JSONObject.toJSONString(request.getParameterMap()));

    }

    /**
     * Print the time that request method execution spend
     * @param joinPoint
     * @throws Throwable
     */
    @Around("controllerLog()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        //获取方法上的注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = joinPoint.getTarget().getClass().getMethod(signature.getName(), signature.getMethod().getParameterTypes());
        SystemLog systemLog = method.getAnnotation(SystemLog.class);
        Object[] args = joinPoint.getArgs();
        Object retVal;
        try {
            retVal = joinPoint.proceed(args);
            long endTime = System.currentTimeMillis();
            logger.info("执行时间：{} ms\n\t", endTime - startTime);
        } catch(Throwable e) {
            throw e;
        }
        return retVal;
    }

    /**
     * Print exception
     * @param ex
     */
    @AfterThrowing(throwing = "ex", pointcut = "controllerLog()")
    public void afterThrowing(Throwable ex) {
        logger.error("发生异常：{}", ex.toString());
    }

    /**
     * Acquire the description for annotation target method
     * @param joinPoint
     * @return
     * @throws Exception
     */
    protected String getMethodDescription(JoinPoint joinPoint) throws Exception {
        String targetName = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] arguments = joinPoint.getArgs();
        Class<?> targetClass = Class.forName(targetName);
        Method[] methods = targetClass.getMethods();

        String description = "";
        for (Method method : methods) {
            if(method.getName().equals(methodName)) {
                Class<?>[] clazzs = method.getParameterTypes();
                if(clazzs.length == arguments.length) {
                    description = method.getAnnotation(SystemLog.class).description();
                    break;
                }
            }
        }
        return description;
    }
}
