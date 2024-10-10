package com.course.business.controller.web;

import com.course.server.dto.ExamAnswerDto;
import com.course.server.dto.QuestionDto;
import com.course.server.dto.QuestionOptionDto;
import com.course.server.dto.ResponseDto;
import com.course.server.service.ExamPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController("webExamPaperController")
@RequestMapping("/web/section/exam")
public class ExamPaperController {

    @Autowired
    private ExamPaperService examPaperService;

    /**
     * 列表查询
     */
    @GetMapping("/list")
    public ResponseDto<List<QuestionDto>> list(@RequestParam("sectionId") String sectionId) {
        ResponseDto<List<QuestionDto>> responseDto = new ResponseDto<>();
        List<QuestionDto> questions = examPaperService.listApi(sectionId);
        // 打乱顺序
        for (QuestionDto question : questions) {
            List<QuestionOptionDto> options = question.getQuestionOptions();
            // 答案设置空
            options.forEach(o -> o.setAnswer(null));
            Collections.shuffle(options);
        }
        Collections.shuffle(questions);
        responseDto.setContent(questions);
        return responseDto;
    }

    /**
     * 答题
     */
    @PostMapping("/submit")
    public ResponseDto<Boolean> submitExam(@RequestHeader("token") String token,
                                           @RequestBody ExamAnswerDto examAnswer) {
        boolean isPass = examPaperService.submitExam(token, examAnswer);
        ResponseDto<Boolean> response = new ResponseDto<>();
        response.setContent(isPass);
        return response;
    }
}
