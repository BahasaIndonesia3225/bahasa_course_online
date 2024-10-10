package com.course.server.dto;

import java.util.Map;

public class ExamAnswerDto {

    private String sectionId;

    private Map<String, String> answer;

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public Map<String, String> getAnswer() {
        return answer;
    }

    public void setAnswer(Map<String, String> answer) {
        this.answer = answer;
    }
}
