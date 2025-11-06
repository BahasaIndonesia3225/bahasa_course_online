package com.course.server.mapper;

import com.course.server.domain.Comment;
import java.util.List;

public interface CommentMapper {
    int insert(Comment comment);

    List<Comment> selectComments(Comment comment);

    int update(Comment comment);

    long selectCommentCount(Comment comment);

    int delete(Long id);

    int deleteComment(Long id);
}
