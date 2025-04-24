package com.course.server.service;

import com.course.server.domain.WatchHistory;
import com.course.server.mapper.WatchHistoryMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class WatchHistoryService {

    @Resource
    private WatchHistoryMapper watchHistoryMapper;


    public int insert(WatchHistory watchHistory) {
        int insert = watchHistoryMapper.insert(watchHistory);
        if (watchHistoryMapper.countByMemberId(watchHistory.getMemberId())>100){
            watchHistoryMapper.deleteByMemberId(watchHistory.getMemberId());
        }

        return insert;
    }



    public List<WatchHistory> selectByMemberId(String memberId) {
        return watchHistoryMapper.selectByMemberId(memberId);
    }
}
