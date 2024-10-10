package com.course.server.mapper.my;

import java.util.List;
import java.util.Map;

public interface MyQuestionMapper {

    List<Map<String, Object>> countGroupBySection();
}
