package com.course.server.mapper;

import com.course.server.domain.WatchHistory;

import java.util.List;

public interface WatchHistoryMapper {

    //根据用户ID查询观看记录
    public List<WatchHistory> selectByMemberId(String memberId);


    //新增观看记录
    public int insert(WatchHistory watchHistory);


    //删除观看记录
    public int deleteByMemberId(String memberId);

    /**
     * 根据用户ID查询记录总条数
     */
    public int countByMemberId(String memberId);
}
