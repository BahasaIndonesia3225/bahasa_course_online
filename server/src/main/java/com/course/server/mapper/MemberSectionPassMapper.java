package com.course.server.mapper;

import com.course.server.domain.MemberSectionPass;
import com.course.server.domain.MemberSectionPassExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MemberSectionPassMapper {
    long countByExample(MemberSectionPassExample example);

    int deleteByExample(MemberSectionPassExample example);

    int deleteByPrimaryKey(String id);

    int insert(MemberSectionPass record);

    int insertSelective(MemberSectionPass record);

    List<MemberSectionPass> selectByExample(MemberSectionPassExample example);

    MemberSectionPass selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") MemberSectionPass record, @Param("example") MemberSectionPassExample example);

    int updateByExample(@Param("record") MemberSectionPass record, @Param("example") MemberSectionPassExample example);

    int updateByPrimaryKeySelective(MemberSectionPass record);

    int updateByPrimaryKey(MemberSectionPass record);

    int deleteByUser(String memberId);

    MemberSectionPass selectByMemberId(String memberId);
}