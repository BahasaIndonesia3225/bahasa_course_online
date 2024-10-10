package com.course.server.domain;

import com.course.server.dto.QuestionDto;

import java.util.List;

public class ExamPaperDto {
    private String sectionId;

    private List<QuestionDto> questions;

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public List<QuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDto> questions) {
        this.questions = questions;
    }
}
