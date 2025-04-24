package com.course.server.service;

import com.alibaba.fastjson.JSONArray;
import com.course.server.domain.LoginDeviceInfo;
import com.course.server.domain.LoginDeviceInfoExample;
import com.course.server.dto.Constants;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.mapper.LoginDeviceInfoMapper;
import com.course.server.util.UuidUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class LoginDeviceInfoService {

    @Resource
    private LoginDeviceInfoMapper loginDeviceInfoMapper;

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    public void saveLoginDevice(String memberId, String deviceId, Integer deviceType) {
        LoginDeviceInfo loginDeviceInfo = new LoginDeviceInfo();
        loginDeviceInfo.setId(UuidUtil.getShortUuid());
        loginDeviceInfo.setLoginTime(new Date());
        loginDeviceInfo.setMemberId(memberId);
        loginDeviceInfo.setDeviceId(deviceId);
        loginDeviceInfo.setDeviceType(deviceType);
        loginDeviceInfoMapper.insert(loginDeviceInfo);

        List<LoginDeviceInfo> cacheLoginDeviceInfoList;
        Object deviceInfoList = redisTemplate.opsForValue().get(Constants.DEVICE_MAP_KEY + memberId);
        if (deviceInfoList == null) cacheLoginDeviceInfoList = new ArrayList<>();
        else cacheLoginDeviceInfoList = JSONArray.parseArray(String.valueOf(deviceInfoList), LoginDeviceInfo.class);
        cacheLoginDeviceInfoList.add(loginDeviceInfo);
        redisTemplate.opsForValue().set(Constants.DEVICE_MAP_KEY + memberId, JSONArray.toJSON(cacheLoginDeviceInfoList));
    }

    public long countByMemberIdAndDeviceId(String memberId, String deviceId) {
        LoginDeviceInfoExample loginDeviceInfoExample = new LoginDeviceInfoExample();
        loginDeviceInfoExample.createCriteria().andMemberIdEqualTo(memberId).andDeviceIdEqualTo(deviceId);
        return loginDeviceInfoMapper.countByExample(loginDeviceInfoExample);
    }

    public long countByMemberId(String memberId) {
        LoginDeviceInfoExample loginDeviceInfoExample = new LoginDeviceInfoExample();
        loginDeviceInfoExample.createCriteria().andMemberIdEqualTo(memberId);
        return loginDeviceInfoMapper.countByExample(loginDeviceInfoExample);
    }

    public List<LoginDeviceInfo> list(String memberId) {
        LoginDeviceInfoExample loginDeviceInfoExample = new LoginDeviceInfoExample();
        loginDeviceInfoExample.createCriteria().andMemberIdEqualTo(memberId);
        return loginDeviceInfoMapper.selectByExample(loginDeviceInfoExample);
    }

    public void delete(String id) {
        LoginDeviceInfoExample loginDeviceInfoExample = new LoginDeviceInfoExample();
        loginDeviceInfoExample.createCriteria().andIdEqualTo(id);
        List<LoginDeviceInfo> loginDeviceInfoList = loginDeviceInfoMapper.selectByExample(loginDeviceInfoExample);
        if (CollectionUtils.isEmpty(loginDeviceInfoList))
            throw new BusinessException(BusinessExceptionCode.LOGIN_DEVICE_NOT_FOUND);
        loginDeviceInfoMapper.deleteByExample(loginDeviceInfoExample);
        LoginDeviceInfo loginDeviceInfo = loginDeviceInfoList.get(0);
        String memberId = loginDeviceInfo.getMemberId();
        List<LoginDeviceInfo> cacheLoginDeviceInfoList;
        Object deviceInfoList = redisTemplate.opsForValue().get(Constants.DEVICE_MAP_KEY + memberId);
        if (deviceInfoList == null) cacheLoginDeviceInfoList = new ArrayList<>();
        else cacheLoginDeviceInfoList = JSONArray.parseArray(String.valueOf(deviceInfoList), LoginDeviceInfo.class);
        cacheLoginDeviceInfoList.removeIf(l -> l.getDeviceId().equals(loginDeviceInfo.getDeviceId()) && l.getMemberId().equals(memberId));
        redisTemplate.opsForValue().set(Constants.DEVICE_MAP_KEY + memberId, JSONArray.toJSON(cacheLoginDeviceInfoList));
    }

    public List<LoginDeviceInfo> listAll() {
        return loginDeviceInfoMapper.selectByExample(new LoginDeviceInfoExample());
    }
}
