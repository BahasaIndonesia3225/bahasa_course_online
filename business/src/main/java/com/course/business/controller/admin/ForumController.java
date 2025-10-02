//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.course.business.controller.admin;

import com.course.server.domain.BanRecord;
import com.course.server.domain.Comment;
import com.course.server.domain.Forum;
import com.course.server.dto.ResponseDto;
import com.course.server.service.BanRecordService;
import com.course.server.service.ForumService;
import com.course.server.service.MemberService;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/admin/forum"})
public class ForumController {
    @Resource
    private ForumService forumService;
    @Resource
    private BanRecordService banRecordService;
    @Resource
    private MemberService memberService;

    @RequestMapping({"/listForum"})
    public ResponseDto list(Forum forum) {
        ResponseDto responseDto = new ResponseDto();
        responseDto.setContent(this.forumService.selectForums(forum));
        return responseDto;
    }

    @RequestMapping({"/auditForum"})
    public ResponseDto audit(@RequestBody Forum forum) {
        ResponseDto responseDto = new ResponseDto();
        this.forumService.updateForum(forum);
        return responseDto;
    }

    @RequestMapping({"/deleteForum"})
    public ResponseDto delete(Long id) {
        ResponseDto responseDto = new ResponseDto();
        this.forumService.delete(id);
        return responseDto;
    }

    @RequestMapping({"/listComment"})
    public ResponseDto listComment(Comment forum) {
        ResponseDto responseDto = new ResponseDto();
        responseDto.setContent(this.forumService.selectComments(forum));
        return responseDto;
    }

    @RequestMapping({"/auditComment"})
    public ResponseDto auditComment(@RequestBody Comment forum) {
        ResponseDto responseDto = new ResponseDto();
        this.forumService.updateComment(forum);
        return responseDto;
    }

    @RequestMapping({"/listBanRecord"})
    public ResponseDto listBanRecord(BanRecord banRecord) {
        ResponseDto responseDto = new ResponseDto();
        responseDto.setContent(this.banRecordService.selectBanRecords(banRecord));
        return responseDto;
    }

    @RequestMapping({"/deleteComment"})
    public ResponseDto deleteComment(Long id) {
        ResponseDto responseDto = new ResponseDto();
        this.forumService.deleteComment(id);
        return responseDto;
    }

    @RequestMapping({"/unban"})
    public ResponseDto unban(String memberId) {
        ResponseDto responseDto = new ResponseDto();
        this.memberService.updateMemberUnState(memberId);
        return responseDto;
    }
}
