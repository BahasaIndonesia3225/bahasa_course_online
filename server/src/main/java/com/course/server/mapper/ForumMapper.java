//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.course.server.mapper;

import com.course.server.domain.Forum;
import java.util.List;

public interface ForumMapper {
    int insert(Forum forum);

    List<Forum> selectForums(Forum forum);

    int update(Forum forum);

    long selectForumsCount(Forum forum);

    int delete(Long id);
}
