package com.course.business.controller.admin;


import com.course.server.domain.Configuration;
import com.course.server.dto.CategoryDto;
import com.course.server.dto.ChapterDto;
import com.course.server.dto.ResponseDto;
import com.course.server.service.ConfigurationService;

import com.course.server.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/admin/configuration")
public class ConfigurationController {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationController.class);
    public static final String BUSINESS_NAME = "短信验证码";

    @Resource
    private ConfigurationService configurationService;

    /**
     * 列表查询
     */
    @GetMapping("/all")
    public ResponseDto all(Configuration config) {
        ResponseDto responseDto = new ResponseDto();
        List<Configuration> categoryDtoList = configurationService.selectConfigurationList(config);
        responseDto.setContent(categoryDtoList);
        return responseDto;
    }

    /**
     * 保存，id有值时更新，无值时新增
     */
    @PostMapping("/save")
    public ResponseDto save(@RequestBody Configuration config) {
        ResponseDto responseDto = new ResponseDto();
        if (config.getId()!=null){
            responseDto.setContent(configurationService.updateConfiguration(config));
        }else {
            responseDto.setContent(configurationService.insertConfiguration(config));
        }
        return responseDto;
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public ResponseDto delete(@PathVariable Long id) {
        ResponseDto responseDto = new ResponseDto();
        configurationService.deleteConfigurationById(id);
        return responseDto;
    }
}
