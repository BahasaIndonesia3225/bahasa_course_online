package com.course.server.mapper;

import com.course.server.domain.Notice;

public interface NoticeMapper {

    Notice selectNotice();

    int updateNotice(Notice record);

}
