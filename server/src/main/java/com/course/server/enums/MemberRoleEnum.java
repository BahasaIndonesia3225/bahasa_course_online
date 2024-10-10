package com.course.server.enums;

public enum MemberRoleEnum {

    NORMAL(1, "普通用户"),
    ADMINISTRATOR(2, "超级管理员");

    private Integer code;

    private String desc;

    MemberRoleEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
