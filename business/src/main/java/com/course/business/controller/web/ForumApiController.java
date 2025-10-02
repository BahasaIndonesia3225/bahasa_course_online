

package com.course.business.controller.web;

import cn.hutool.core.thread.ThreadUtil;
import com.course.server.domain.BanRecord;
import com.course.server.domain.Comment;
import com.course.server.domain.Forum;
import com.course.server.domain.Member;
import com.course.server.dto.LoginMemberDto;
import com.course.server.dto.ResponseDto;
import com.course.server.service.BanRecordService;
import com.course.server.service.ForumService;
import com.course.server.service.MemberService;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/web/forum"})
public class ForumApiController {
    @Resource
    private ForumService forumService;
    @Resource
    private MemberService memberService;
    @Resource
    private BanRecordService banRecordService;

    @RequestMapping({"/listForum"})
    public ResponseDto list(Forum forum) {
        ResponseDto responseDto = new ResponseDto();
        forum.setState("1");
        responseDto.setContent(forumService.selectForums(forum));
        return responseDto;
    }

    @RequestMapping({"/saveForum"})
    public ResponseDto save(HttpServletRequest request, @RequestHeader("token") String token, @RequestBody Forum forum) {
        ResponseDto responseDto = new ResponseDto();
        String ip = request.getRemoteAddr();
        forum.setIp(ip);
        LoginMemberDto loginMember = memberService.getLoginMember(token);
        Member member = memberService.selectByMobile(loginMember.getMobile());
        if (member.getState().equals("0")) {
            responseDto.setSuccess(false);
            responseDto.setMessage("您的发帖功能已禁用，请联系老师！");
            return responseDto;
        } else {
            forum.setMemberId(loginMember.getId());
            forum.setState("0");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            forum.setCreate(formatter.format(new Date()));
            boolean contains = SensitiveWordHelper.contains(forum.getContent());
            if (contains) {
                responseDto.setSuccess(false);
                responseDto.setMessage("您的内容中含有敏感词汇，发帖功能已禁用，请联系老师！");
                ThreadUtil.execAsync(() -> {
                    List<String> all = SensitiveWordHelper.findAll(forum.getContent());
                    memberService.updateMemberState(loginMember.getId());
                    BanRecord banRecord = new BanRecord();
                    banRecord.setMemberId(loginMember.getId());
                    banRecord.setMemberName(loginMember.getName());
                    banRecord.setReason("用户填入内容：" + forum.getContent() + " 含有敏感词汇：" + all);
                    banRecord.setBanTime(formatter.format(new Date()));
                    banRecordService.insert(banRecord);
                });
                return responseDto;
            } else {
                forumService.insertForum(forum);
                responseDto.setContent(forum);
                return responseDto;
            }
        }
    }

    @RequestMapping({"/listComment"})
    public ResponseDto listComment(Comment forum) {
        ResponseDto responseDto = new ResponseDto();
        forum.setState("1");
        responseDto.setContent(forumService.selectComments(forum));
        return responseDto;
    }

    @RequestMapping({"/saveComment"})
    public ResponseDto save(HttpServletRequest request, @RequestHeader("token") String token, @RequestBody Comment forum) {
        ResponseDto responseDto = new ResponseDto();
        String ip = request.getRemoteAddr();
        forum.setIp(ip);
        LoginMemberDto loginMember = memberService.getLoginMember(token);
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ft.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        forum.setCreate(ft.format(new Date()));
        boolean contains = SensitiveWordHelper.contains(forum.getContent());
        if (contains) {
            responseDto.setSuccess(false);
            responseDto.setMessage("包含敏感词");
            ThreadUtil.execAsync(() -> {
                memberService.updateMemberState(loginMember.getId());
                BanRecord banRecord = new BanRecord();
                banRecord.setMemberId(loginMember.getId());
                banRecord.setMemberName(loginMember.getName());
                banRecord.setReason(forum.getContent());
                banRecord.setBanTime(ft.format(new Date()));
                banRecordService.insert(banRecord);
            });
            return responseDto;
        } else {
            forum.setMemberId(loginMember.getId());
            forum.setState("0");
            forumService.insertComment(forum);
            responseDto.setContent(forum);
            return responseDto;
        }
    }
}
