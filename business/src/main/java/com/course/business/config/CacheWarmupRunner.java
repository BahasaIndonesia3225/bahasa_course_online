package com.course.business.config;

import com.alibaba.fastjson.JSON;
import com.course.server.domain.LoginDeviceInfo;
import com.course.server.dto.Constants;
import com.course.server.service.LoginDeviceInfoService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CacheWarmupRunner implements ApplicationRunner {

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    @Resource
    private LoginDeviceInfoService loginDeviceInfoService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<LoginDeviceInfo> loginDeviceInfoList = loginDeviceInfoService.listAll();
        Map<String, List<LoginDeviceInfo>> map = loginDeviceInfoList.stream().collect(Collectors.groupingBy(LoginDeviceInfo::getMemberId));
        map.forEach((k, v) -> {
            String cacheKey = Constants.DEVICE_MAP_KEY + k;
            redisTemplate.delete(cacheKey);
            redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(v));
        });
    }
}
