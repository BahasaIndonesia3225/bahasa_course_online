package com.course.business.controller.web;

import com.alibaba.fastjson.JSON;
import com.course.server.domain.LoginDeviceInfo;
import com.course.server.domain.Member;
import com.course.server.dto.*;
import com.course.server.enums.SmsUseEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.service.LoginDeviceInfoService;
import com.course.server.service.MemberService;
import com.course.server.service.SmsService;
import com.course.server.util.UuidUtil;
import com.course.server.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController("webMemberController")
@RequestMapping("/web/member")
public class MemberController {

    private static final Logger LOG = LoggerFactory.getLogger(MemberController.class);
    public static final String BUSINESS_NAME = "会员";

    @Resource
    private MemberService memberService;

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    @Resource
    private SmsService smsService;

    @Resource
    private LoginDeviceInfoService loginDeviceInfoService;



    /**
     *  用户绑定手机号码发送短信
     * @param smsDto
     * @return
     */
    @RequestMapping(value = "/sendUser", method = RequestMethod.POST)
    public ResponseDto sendUser(@RequestBody SmsDto smsDto) {

        ResponseDto responseDto = new ResponseDto();
        responseDto.setContent(smsService.sendCode(smsDto));
        // TODO 调第三方短信接口发送短信
        return responseDto;
    }

    /**
     *  用户修改手机号码
     * @param smsDto
     * @return
     */
    @RequestMapping(value = "/updateUser", method = RequestMethod.POST)
    public ResponseDto updateUser(@RequestBody SmsDto smsDto,@RequestHeader("token") String token) {

        ResponseDto responseDto = new ResponseDto();
        Member memberDto = memberService.findByPhone(smsDto.getMobile());
        if (memberDto != null) {
            throw new BusinessException(BusinessExceptionCode.USER_PHONE_LOGIN_NAME_EXIST);
        }
        if (!smsDto.getCode().equals(redisTemplate.opsForValue().get(smsDto.getMobile()))){
            throw new BusinessException(BusinessExceptionCode.MOBILE_CODE_EXPIRED);
        }
        //修改手机号码
        responseDto.setContent(memberService.updateMobile(token,smsDto.getMobile()));
        // TODO 调第三方短信接口发送短信
        return responseDto;
    }

    /**
     * 保存，id有值时更新，无值时新增
     */
    @PostMapping("/register")
    public ResponseDto register(@RequestBody MemberDto memberDto) {
        // 保存校验
        ValidatorUtil.require(memberDto.getMobile(), "手机号");
        ValidatorUtil.length(memberDto.getMobile(), "手机号", 11, 11);
        ValidatorUtil.require(memberDto.getPassword(), "密码");
        ValidatorUtil.length(memberDto.getName(), "昵称", 1, 50);
        ValidatorUtil.length(memberDto.getPhoto(), "头像url", 1, 200);

        // 密码加密
        memberDto.setPassword(DigestUtils.md5DigestAsHex(memberDto.getPassword().getBytes()));

        // 校验短信验证码
        SmsDto smsDto = new SmsDto();
        smsDto.setMobile(memberDto.getMobile());
        smsDto.setCode(memberDto.getSmsCode());
        smsDto.setUse(SmsUseEnum.REGISTER.getCode());
        smsService.validCode(smsDto);
        LOG.info("短信验证码校验通过");

        ResponseDto responseDto = new ResponseDto();
        memberService.save(memberDto);
        responseDto.setContent(memberDto);
        return responseDto;
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public ResponseDto login(@RequestBody MemberDto memberDto) {
        LOG.info("用户登录开始");
        memberDto.setPassword(DigestUtils.md5DigestAsHex(memberDto.getPassword().getBytes()));
        ResponseDto responseDto = new ResponseDto();

        // 根据验证码token去获取缓存中的验证码，和用户输入的验证码是否一致
        String imageCode = (String) redisTemplate.opsForValue().get(memberDto.getImageCodeToken());
        LOG.info("从redis中获取到的验证码：{}", imageCode);
        if (StringUtils.isEmpty(imageCode)) {
            responseDto.setSuccess(false);
            responseDto.setMessage("验证码已过期");
            LOG.info("用户登录失败，验证码已过期");
            return responseDto;
        }
        if (!imageCode.toLowerCase().equals(memberDto.getImageCode().toLowerCase())) {
            responseDto.setSuccess(false);
            responseDto.setMessage("验证码不对");
            LOG.info("用户登录失败，验证码不对");
            return responseDto;
        } else {
            // 验证通过后，移除验证码
            redisTemplate.delete(memberDto.getImageCodeToken());
        }

        LoginMemberDto loginMemberDto = memberService.login(memberDto);
        String token = UuidUtil.getShortUuid();
        loginMemberDto.setToken(token);
        redisTemplate.opsForValue().set(token, JSON.toJSONString(loginMemberDto), 3600, TimeUnit.SECONDS);
        responseDto.setContent(loginMemberDto);
        return responseDto;
    }

    /**
     * 登录
     */
    @PostMapping("/signIn")
    public ResponseDto signIn(@RequestBody MemberDto memberDto) {
        LOG.info("用户登录开始");
        memberDto.setPassword(DigestUtils.md5DigestAsHex(memberDto.getPassword().getBytes()));
        ResponseDto responseDto = new ResponseDto();
        LoginMemberDto loginMemberDto = memberService.login(memberDto);
        String mobile = memberDto.getMobile();
        String token = UuidUtil.getShortUuid();
        loginMemberDto.setToken(token);
        String key = Constants.ONLINE_KEY + mobile + ":" + memberDto.getDeviceId();
        Object o = redisTemplate.opsForValue().get(key);
        if (o != null) {
            String oToken = String.valueOf(o);
            redisTemplate.delete(oToken);
        }
        if (loginMemberDto.getFlag().equals("0")) {
            redisTemplate.opsForValue().set(key, loginMemberDto.getToken(), 12, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(loginMemberDto.getToken(), JSON.toJSONString(loginMemberDto), 12, TimeUnit.HOURS);
            responseDto.setContent(loginMemberDto);
            responseDto.setSuccess(false);
            responseDto.setCode("A0100");
            return responseDto;
        }
        redisTemplate.opsForValue().set(key, loginMemberDto.getToken(), 12, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(loginMemberDto.getToken(), JSON.toJSONString(loginMemberDto), 12, TimeUnit.HOURS);
        responseDto.setContent(loginMemberDto);
        return responseDto;
    }

    /**
     * 退出登录
     */
    @GetMapping("/logout/{token}")
    public ResponseDto logout(@PathVariable String token) {
        ResponseDto responseDto = new ResponseDto();
        redisTemplate.delete(token);
        LOG.info("从redis中删除token:{}", token);
        return responseDto;
    }

    /**
     * 获取登陆用户信息
     */
    @GetMapping("/getUser")
    public ResponseDto getUser(@RequestHeader("token") String token) {
        ResponseDto responseDto = new ResponseDto();
        LoginMemberDto loginMember = memberService.getLoginMember(token);
        responseDto.setContent(memberService.selectByMobile(loginMember.getMobile()));
        return responseDto;
    }

    /**
     * 校验手机号是否存在
     * 存在则success=true，不存在则success=false
     */
    @GetMapping(value = "/is-mobile-exist/{mobile}")
    public ResponseDto isMobileExist(@PathVariable(value = "mobile") String mobile) throws BusinessException {
        LOG.info("查询手机号是否存在开始");
        ResponseDto responseDto = new ResponseDto();
        MemberDto memberDto = memberService.findByMobile(mobile);
        if (memberDto == null) {
            responseDto.setSuccess(false);
        } else {
            responseDto.setSuccess(true);
        }
        return responseDto;
    }

    @PostMapping("/reset-password")
    public ResponseDto resetPassword(@RequestBody MemberDto memberDto) throws BusinessException {
        LOG.info("会员密码重置开始:");
        memberDto.setPassword(DigestUtils.md5DigestAsHex(memberDto.getPassword().getBytes()));
        ResponseDto<MemberDto> responseDto = new ResponseDto();

        // 校验短信验证码
        SmsDto smsDto = new SmsDto();
        smsDto.setMobile(memberDto.getMobile());
        smsDto.setCode(memberDto.getSmsCode());
        smsDto.setUse(SmsUseEnum.FORGET.getCode());
        smsService.validCode(smsDto);
        LOG.info("短信验证码校验通过");

        // 重置密码
        memberService.resetPassword(memberDto);

        return responseDto;
    }

    /**
     * 数量查询
     */
    @GetMapping("/device-count/{memberId}")
    public ResponseDto count(@PathVariable String memberId) {
        ResponseDto responseDto = new ResponseDto();
        long count = loginDeviceInfoService.countByMemberId(memberId);
        responseDto.setContent(count);
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
}
