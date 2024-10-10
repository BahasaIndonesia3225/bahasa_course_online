package com.course.server.domain;

public class Chapter {
    private String id;

    private String courseId;

    private String name;

    private Integer doQuestion;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDoQuestion() {
        return doQuestion;
    }

    public void setDoQuestion(Integer doQuestion) {
        this.doQuestion = doQuestion;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", courseId=").append(courseId);
        sb.append(", name=").append(name);
        sb.append(", doQuestion=").append(doQuestion);
        sb.append("]");
        return sb.toString();
    }
}