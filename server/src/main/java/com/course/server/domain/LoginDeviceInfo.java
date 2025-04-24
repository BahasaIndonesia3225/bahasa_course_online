package com.course.server.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

public class LoginDeviceInfo implements Serializable {
    private static final long serialVersionUID = 5262845669828463983L;

    private String id;

    private String memberId;

    private String deviceId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date loginTime;

    private Integer deviceType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", memberId=").append(memberId);
        sb.append(", deviceId=").append(deviceId);
        sb.append(", loginTime=").append(loginTime);
        sb.append(", deviceType=").append(deviceType);
        sb.append("]");
        return sb.toString();
    }
}