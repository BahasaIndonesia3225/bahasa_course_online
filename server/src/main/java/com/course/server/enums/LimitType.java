package com.course.server.enums;

public enum LimitType {
    /**
     * 默认策略
     */
    DEFAULT,
    /**
     * 根据IP进行限流
     */
    IP,
    /**
     * 用户
     */
    USER
}
