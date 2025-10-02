package com.course.business.controller.web;

import com.course.server.dto.ResponseDto;
import com.course.server.dto.SmsDto;
import com.course.server.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RestController("webSmsController")
@RequestMapping("/web/sms")
public class SmsController {

    private static final Logger LOG = LoggerFactory.getLogger(SmsController.class);
    public static final String BUSINESS_NAME = "短信验证码";

    @Resource
    private SmsService smsService;

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public ResponseDto send(@RequestBody SmsDto smsDto) {
        LOG.info("发送短信请求开始: {}", smsDto);
        ResponseDto responseDto = new ResponseDto();
        smsService.sendCode(smsDto);
        LOG.info("发送短信请求结束");
        return responseDto;
    }

    @RequestMapping(value = {"/upload"}, method = {RequestMethod.POST})
    public ResponseDto valid(@RequestParam(value = "multiFile", required = false) MultipartFile multiFile) {
        ResponseDto responseDto = new ResponseDto();
        String s = uploadToServer(multiFile);
        responseDto.setContent(s);
        return responseDto;
    }

    public static String uploadToServer(MultipartFile multiFile) {
        String uploadPath = "bahasaindo.com/oss/";
        String uploadFileName = String.valueOf(UUID.randomUUID());
        File file = new File(uploadPath);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            if (!mkdirs) {
                return null;
            }
        }

        InputStream ins = null;
        FileOutputStream outs = null;

        try {
            ins = multiFile.getInputStream();
            outs = new FileOutputStream("/www/wwwroot/www.bahasaindo.com/oss/" + uploadFileName + "." + multiFile.getOriginalFilename().split("\\.")[1]);
            byte[] bytes = new byte[1024];

            int len;
            while ((len = ins.read(bytes)) != -1) {
                outs.write(bytes, 0, len);
            }

            outs.close();
            return uploadPath + uploadFileName + "." + multiFile.getOriginalFilename().split("\\.")[1];
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
