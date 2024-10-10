package com.course.server.exception;

public enum BusinessExceptionCode {

    MEMBER_NOT_EXIST("A0001","会员不存在"),
    USER_LOGIN_NAME_EXIST("A0002","登录名已存在"),
    LOGIN_USER_ERROR("A0003","用户名不存在或密码错误"),
    LOGIN_MEMBER_ERROR("A0004","手机号不存在或密码错误"),
    MOBILE_CODE_TOO_FREQUENT("A0005","短信请求过于频繁"),
    MOBILE_CODE_ERROR("A0006","短信验证码不正确"),
    MOBILE_CODE_EXPIRED("A0007","短信验证码不存在或已过期，请重新发送短信"),
    USER_NOT_FOUND("A0008","用户不存在"),
    LOGIN_DEVICE_UPPER_LIMIT("A0100","登陆设备以达到上限，请联系管理员清除不常用设备"),
    LOGIN_DEVICE_NOT_FOUND("A0101","登陆设备不存在"),
    ACCESS_RATE_LIMITER("A0201","访问过于频繁，请稍候再试"),

    USER_PHONE_LOGIN_NAME_EXIST("A0009","手机号码已存在"),
    ;

    private String code;

    private String desc;

    BusinessExceptionCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
