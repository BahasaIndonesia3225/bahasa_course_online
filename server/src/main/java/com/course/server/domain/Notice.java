package com.course.server.domain;

import lombok.Data;

import java.util.Date;

@Data
public class Notice {
    private String id;

    private String remark;



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


}
