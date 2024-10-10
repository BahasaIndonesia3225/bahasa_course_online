package com.course.business.controller.admin;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.course.server.domain.LoginDeviceInfo;
import com.course.server.domain.Member;
import com.course.server.dto.MemberDto;
import com.course.server.dto.MemberPageDto;
import com.course.server.dto.ResponseDto;
import com.course.server.service.LoginDeviceInfoService;
import com.course.server.service.MemberService;
import com.course.server.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/admin/member")
public class MemberController {

    private static final Logger LOG = LoggerFactory.getLogger(MemberController.class);
    public static final String BUSINESS_NAME = "会员";

    @Resource
    private MemberService memberService;

    @Resource
    private LoginDeviceInfoService loginDeviceInfoService;

    /**
     * 列表查询
     */
    @PostMapping("/list")
    public ResponseDto list(@RequestBody MemberPageDto pageDto) {
        ResponseDto responseDto = new ResponseDto();
        memberService.list(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/save")
    public ResponseDto save(@RequestBody MemberDto memberDto) {
        // 保存校验
        ValidatorUtil.require(memberDto.getMobile(), "手机号");
//        ValidatorUtil.length(memberDto.getMobile(), "手机号", 11, 11);
        ValidatorUtil.require(memberDto.getPassword(), "密码");
        ValidatorUtil.length(memberDto.getName(), "昵称", 1, 50);
        ValidatorUtil.length(memberDto.getPhoto(), "头像url", 1, 200);
        ResponseDto responseDto = new ResponseDto();
        memberService.save(memberDto);
        responseDto.setContent(memberDto);
        return responseDto;
    }

    @DeleteMapping("/delete/{id}")
    public ResponseDto delete(@PathVariable String id) {
        ResponseDto responseDto = new ResponseDto();
        memberService.delete(id);
        return responseDto;
    }

    /**
     * 列表查询
     */
    @GetMapping("/device-list/{memberId}")
    public ResponseDto list(@PathVariable String memberId) {
        ResponseDto responseDto = new ResponseDto();
        List<LoginDeviceInfo> loginDeviceInfoList = loginDeviceInfoService.list(memberId);
        responseDto.setContent(loginDeviceInfoList);
        return responseDto;
    }

    /**
     * 删除
     */
    @DeleteMapping("/device-delete/{id}")
    public ResponseDto deleteDevice(@PathVariable String id) {
        ResponseDto responseDto = new ResponseDto();
        loginDeviceInfoService.delete(id);
        return responseDto;
    }

    /**
     * 导出
     */
    @PostMapping("/export")
    public void exportMember(HttpServletResponse response) throws UnsupportedEncodingException {
        List<Member> memberList = memberService.listAll();
        // 通过工具类创建writer，默认创建xls格式
        ExcelWriter writer = ExcelUtil.getWriter();
        writer.setOnlyAlias(true);
        //自定义标题别名
        writer.addHeaderAlias("mobile", "用户名");
        writer.addHeaderAlias("password", "密码");
        writer.addHeaderAlias("name", "昵称");
        writer.addHeaderAlias("payStatus", "支付状态");
        writer.addHeaderAlias("role", "会员角色");
        writer.addHeaderAlias("doQuestion", "是否需要答题");
        writer.addHeaderAlias("deviceLimitNum", "设备限制数量");
        // 一次性写出内容，使用默认样式，强制输出标题
        writer.write(memberList, true);
        writer.autoSizeColumnAll();
        //response为HttpServletResponse对象
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        //文件名
        String name = URLEncoder.encode("会员信息_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + name + ".xls");
        //out为OutputStream，需要写出到的目标流
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            writer.flush(out, true);
        } catch (IOException e) {
            LOG.error("导出会员信息失败");
        } finally {
            // 关闭writer，释放内存
            writer.close();
        }
        //此处记得关闭输出Servlet流
        IoUtil.close(out);
    }

    /**
     * 导入
     */
    @PostMapping("/import")
    public ResponseDto importMember(MultipartFile file) throws IOException {
        ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
        reader.addHeaderAlias("用户名", "mobile");
        reader.addHeaderAlias("密码", "password");
        reader.addHeaderAlias("昵称", "name");
        reader.addHeaderAlias("支付状态", "payStatus");
        reader.addHeaderAlias("会员角色", "role");
        reader.addHeaderAlias("是否需要答题", "doQuestion");
        reader.addHeaderAlias("设备限制数量", "deviceLimitNum");
        List<Member> memberList = reader.readAll(Member.class);
        memberService.saveAll(memberList);
        return new ResponseDto();
    }
}
