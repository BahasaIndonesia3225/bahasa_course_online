package com.course.server.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class WatchHistory {

    private Long id;

    private String memberId;

    private String courseName;

    private String courseId;

    private Integer time;

    private String courseVod;

//    @JsonFormat(shape = JsonFormat.Shape.ANY,locale = "zh_CN",pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT")
    private String creatorTime;
}
