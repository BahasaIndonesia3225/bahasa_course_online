package com.course.server.mapper;

import com.course.server.domain.LoginDeviceInfo;
import com.course.server.domain.LoginDeviceInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LoginDeviceInfoMapper {
    long countByExample(LoginDeviceInfoExample example);

    int deleteByExample(LoginDeviceInfoExample example);

    int deleteByPrimaryKey(String id);

    int insert(LoginDeviceInfo record);

    int insertSelective(LoginDeviceInfo record);

    List<LoginDeviceInfo> selectByExample(LoginDeviceInfoExample example);

    LoginDeviceInfo selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") LoginDeviceInfo record, @Param("example") LoginDeviceInfoExample example);

    int updateByExample(@Param("record") LoginDeviceInfo record, @Param("example") LoginDeviceInfoExample example);

    int updateByPrimaryKeySelective(LoginDeviceInfo record);

    int updateByPrimaryKey(LoginDeviceInfo record);
}