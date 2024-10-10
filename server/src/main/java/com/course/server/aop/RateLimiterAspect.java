package com.course.server.aop;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.course.server.enums.LimitType;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.util.IpUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimiterAspect {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    public static final Logger log = LoggerFactory.getLogger(RateLimiterAspect.class);

    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter) {
        // 在 {time} 秒内仅允许访问 {count} 次。
        int time = rateLimiter.time();
        int count = rateLimiter.count();
        // 根据用户IP（可选）和接口方法，构造key
        String combineKey = getCombineKey(rateLimiter, point);
        // 限流逻辑实现
        ZSetOperations<Object, Object> zSetOperations = redisTemplate.opsForZSet();
        // 记录本次访问的时间结点
        long currentMs = System.currentTimeMillis();
        zSetOperations.add(combineKey, String.valueOf(currentMs), currentMs);
        // 这一步是为了防止一直存在于内存中
        redisTemplate.expire(combineKey, time, TimeUnit.SECONDS);
        // 移除{time}秒之前的访问记录（滑动窗口思想）
        zSetOperations.removeRangeByScore(combineKey, 0, currentMs - time * 1000L);
        // 获得当前窗口内的访问记录数
        Long curCount = zSetOperations.zCard(combineKey);
        // 限流判断
        if (curCount != null && curCount > count) {
            log.error("[RateLimiter] 限制请求数： {}, 当前请求数： {} , 缓存key： {}", count, curCount, combineKey);
            throw new BusinessException(BusinessExceptionCode.ACCESS_RATE_LIMITER);
        }
    }

    public String getCombineKey(RateLimiter rateLimiter, JoinPoint point) {
        StringBuilder stringBuilder = new StringBuilder(rateLimiter.key());
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (rateLimiter.limitType() == LimitType.IP) {
            stringBuilder.append(IpUtil.getIp(request)).append("-");
        } else if (rateLimiter.limitType() == LimitType.USER) {
            String token = request.getHeader("token");
            Object object = redisTemplate.opsForValue().get(token);
            JSONObject loginUserDto = JSONUtil.parseObj(String.valueOf(object));
            String username = loginUserDto.getStr("mobile", loginUserDto.getStr("loginName"));
            stringBuilder.append(username).append("-");
        }
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        stringBuilder.append(targetClass.getName()).append("-").append(method.getName());
        return stringBuilder.toString();
    }
}
