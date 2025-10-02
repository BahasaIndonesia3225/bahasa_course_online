package com.course.business.controller.web;

import com.course.server.domain.Notice;
import com.course.server.dto.PageDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.TeacherDto;
import com.course.server.service.NoticeService;
import com.course.server.service.TeacherService;
import com.course.server.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/web/notice")
public class NoticeController {



    @Resource
    private NoticeService noticeService;

    @PostMapping({"/all"})
    public ResponseDto all() {
        ResponseDto responseDto = new ResponseDto();
        responseDto.setContent(this.noticeService.selectNotice(1L));
        return responseDto;
    }

    @PostMapping({"/peacock"})
    public ResponseDto peacock() {
        ResponseDto responseDto = new ResponseDto();
        Notice notice = this.noticeService.selectNotice(2L);
        responseDto.setContent(notice);
        return responseDto;
    }





}
