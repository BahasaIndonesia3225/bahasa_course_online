package com.course.server.mapper;

import com.course.server.domain.Notice;

public interface NoticeMapper {

    Notice selectNotice(Long id);

    int updateNotice(Notice record);

}
