package com.course.server.mapper;

import com.course.server.domain.Configuration;

import java.util.List;


public interface ConfigurationMapper {



    /**
     * 查询配置表列表
     *
     * @param Configuration 配置表
     * @return 配置表集合
     */
    public List<Configuration> selectConfigurationList(Configuration Configuration);

    /**
     * 新增配置表
     *
     * @param Configuration 配置表
     * @return 结果
     */
    public int insertConfiguration(Configuration Configuration);

    /**
     * 修改配置表
     *
     * @param Configuration 配置表
     * @return 结果
     */
    public int updateConfiguration(Configuration Configuration);

    /**
     * 删除配置表
     *
     * @param id 配置表主键
     * @return 结果
     */
    public int deleteConfigurationById(Long id);

}
