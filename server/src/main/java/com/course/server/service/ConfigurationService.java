package com.course.server.service;

import com.course.server.domain.Configuration;
import com.course.server.mapper.ConfigurationMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ConfigurationService {

    @Resource
    private ConfigurationMapper configMapper;

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    /**
     * 项目启动时，初始化配置表到缓存
     */
    @PostConstruct
    public void init()
    {
        loadingDictCache();
    }
    /**
     * 初始化配置表到缓存
     */
    public void loadingDictCache()
    {
        Configuration configuration = new Configuration();
        List<Configuration> configurations = configMapper.selectConfigurationList(configuration);
        configurations.stream().forEach(c -> {
            redisTemplate.opsForValue().set(c.getConfigurationKey(), c.getConfigurationValue());
        });
    }
    /**
     * 查询配置表列表
     *
     * @param Configuration 配置表
     * @return 配置表集合
     */
    public List<Configuration> selectConfigurationList(Configuration Configuration){
        return configMapper.selectConfigurationList(Configuration);
    }

    /**
     * 新增配置表
     *
     * @param Configuration 配置表
     * @return 结果
     */
    public int insertConfiguration(Configuration Configuration){
        redisTemplate.opsForValue().set(Configuration.getConfigurationKey(), Configuration.getConfigurationValue());
        return configMapper.insertConfiguration(Configuration);
    }

    /**
     * 修改配置表
     *
     * @param Configuration 配置表
     * @return 结果
     */
    public int updateConfiguration(Configuration Configuration){
        redisTemplate.opsForValue().set(Configuration.getConfigurationKey(), Configuration.getConfigurationValue());
        return configMapper.updateConfiguration(Configuration);
    }

    /**
     * 删除配置表
     *
     * @param id 配置表主键
     * @return 结果
     */
    public int deleteConfigurationById(Long id){
        return configMapper.deleteConfigurationById(id);
    }
}
