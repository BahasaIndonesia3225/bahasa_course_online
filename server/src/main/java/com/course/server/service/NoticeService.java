package com.course.server.service;

import com.course.server.domain.File;
import com.course.server.domain.FileExample;
import com.course.server.domain.Member;
import com.course.server.domain.Notice;
import com.course.server.dto.FileDto;
import com.course.server.dto.PageDto;
import com.course.server.mapper.FileMapper;
import com.course.server.mapper.NoticeMapper;
import com.course.server.util.CopyUtil;
import com.course.server.util.UuidUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class NoticeService {

    @Resource
    private NoticeMapper noticeMapper;

    /**
     * 查询通知
     */
    public Notice selectNotice() {

        return noticeMapper.selectNotice();
    }

    /**
     * 修改通知
     */
    public int updateNotice(Notice notice) {
        return noticeMapper.updateNotice(notice);
    }
}
