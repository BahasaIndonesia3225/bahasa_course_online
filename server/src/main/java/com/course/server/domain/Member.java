package com.course.server.domain;

import java.util.Date;

public class Member {
    private String id;

    private String mobile;

    private String password;

    private String name;

    private String photo;

    private Date registerTime;

    private Integer payStatus;

    private Integer role;

    private Integer doQuestion;

    private Integer deviceLimitNum;

    private String phone;

    private Integer userType;

    private Integer licenseType;

    private Integer loginType;

    private String ip;

    private String lng;

    private String lat;

    private String state;

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(Integer licenseType) {
        this.licenseType = licenseType;
    }

    public Integer getLoginType() {
        return loginType;
    }

    public void setLoginType(Integer loginType) {
        this.loginType = loginType;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Date getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(Date registerTime) {
        this.registerTime = registerTime;
    }

    public Integer getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(Integer payStatus) {
        this.payStatus = payStatus;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Integer getDoQuestion() {
        return doQuestion;
    }

    public void setDoQuestion(Integer doQuestion) {
        this.doQuestion = doQuestion;
    }

    public Integer getDeviceLimitNum() {
        return deviceLimitNum;
    }

    public void setDeviceLimitNum(Integer deviceLimitNum) {
        this.deviceLimitNum = deviceLimitNum;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", mobile=").append(mobile);
        sb.append(", password=").append(password);
        sb.append(", name=").append(name);
        sb.append(", photo=").append(photo);
        sb.append(", registerTime=").append(registerTime);
        sb.append(", payStatus=").append(payStatus);
        sb.append(", role=").append(role);
        sb.append(", doQuestion=").append(doQuestion);
        sb.append(", deviceLimitNum=").append(deviceLimitNum);
        sb.append(", userType=").append(userType);
        sb.append(", phone=").append(phone);
        sb.append("]");
        return sb.toString();
    }
}