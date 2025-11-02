package com.course.business.controller.admin;

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
@RequestMapping("/admin/notice")
public class NoticeApiController {



    @Resource
    private NoticeService noticeService;

    /**
     * 列表查询
     */
    @GetMapping("/all")
    public ResponseDto all(Long id) {
        ResponseDto responseDto = new ResponseDto();
        responseDto.setContent(noticeService.selectNotice(id));
        return responseDto;
    }



    /**
     * 保存，id有值时更新，无值时新增
     */
    @PostMapping("/save")
    public ResponseDto save(@RequestBody Notice notice) {

        ResponseDto responseDto = new ResponseDto();

        responseDto.setContent(noticeService.updateNotice(notice));
        return responseDto;
    }


    @GetMapping({"/peacock"})
    public ResponseDto peacock() {
        ResponseDto responseDto = new ResponseDto();
        responseDto.setContent(noticeService.selectNotice(2L));
        return responseDto;
    }

    @PostMapping({"/savePeacock"})
    public ResponseDto savePeacock(@RequestBody Notice notice) {
        ResponseDto responseDto = new ResponseDto();
        notice.setId("2");
        responseDto.setContent(noticeService.updateNotice(notice));
        return responseDto;
    }

}
