package com.course.business.controller.admin;

import com.course.server.domain.ExamPaperDto;
import com.course.server.dto.QuestionDto;
import com.course.server.dto.ResponseDto;
import com.course.server.service.ExamPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/section/exam")
public class ExamPaperController {

    @Autowired
    private ExamPaperService examPaperService;

    /**
     * 列表查询
     */
    @GetMapping("/list")
    public ResponseDto<List<QuestionDto>> list(@RequestParam("sectionId") String sectionId) {
        ResponseDto<List<QuestionDto>> responseDto = new ResponseDto<>();
        List<QuestionDto> questions = examPaperService.list(sectionId);
        responseDto.setContent(questions);
        return responseDto;
    }

    /**
     * 保存，id有值时更新，无值时新增
     */
    @PostMapping("/save")
    public ResponseDto<Void> save(@RequestBody ExamPaperDto examPaper) {
        examPaperService.save(examPaper);
        return new ResponseDto<>();
    }
}
