//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.course.server.service;

import com.course.server.domain.Comment;
import com.course.server.domain.Forum;
import com.course.server.dto.PageDto;
import com.course.server.mapper.CommentMapper;
import com.course.server.mapper.ForumMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ForumService {
    @Resource
    private ForumMapper forumMapper;
    @Resource
    private CommentMapper commentMapper;

    public int insertForum(Forum forum) {
        return this.forumMapper.insert(forum);
    }

    public int updateForum(Forum forum) {
        return this.forumMapper.update(forum);
    }

    public PageDto selectForums(Forum forum) {
        PageDto pageDto = new PageDto();
        pageDto.setPage(forum.getPage());
        pageDto.setSize(forum.getSize());
        forum.setPage(pageDto.getPage() * forum.getSize());
        List<Forum> forums = this.forumMapper.selectForums(forum);
        pageDto.setList(forums);
        pageDto.setTotal(this.forumMapper.selectForumsCount(forum));
        return pageDto;
    }

    public PageDto selectComments(Comment comment) {
        PageDto pageDto = new PageDto();
        pageDto.setPage(comment.getPage());
        pageDto.setSize(comment.getSize());
        comment.setPage(pageDto.getPage() * comment.getSize());
        List<Comment> comments = this.commentMapper.selectComments(comment);
        pageDto.setList(comments);
        pageDto.setTotal(this.commentMapper.selectCommentCount(comment));
        return pageDto;
    }

    public int updateComment(Comment comment) {
        return this.commentMapper.update(comment);
    }

    public int insertComment(Comment comment) {
        return this.commentMapper.insert(comment);
    }

    public int delete(Long id) {
        this.commentMapper.deleteComment(id);
        return this.forumMapper.delete(id);
    }

    public int deleteComment(Long id) {
        return this.commentMapper.delete(id);
    }
}
