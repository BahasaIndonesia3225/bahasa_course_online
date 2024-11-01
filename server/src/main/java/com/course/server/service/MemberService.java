package com.course.server.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONUtil;
import com.course.server.domain.Member;
import com.course.server.domain.MemberExample;
import com.course.server.domain.MemberSectionPass;
import com.course.server.domain.Section;
import com.course.server.dto.LoginMemberDto;
import com.course.server.dto.MemberDto;
import com.course.server.dto.MemberPageDto;
import com.course.server.enums.MemberRoleEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.mapper.MemberMapper;
import com.course.server.mapper.MemberSectionPassMapper;
import com.course.server.mapper.SectionMapper;
import com.course.server.util.CopyUtil;
import com.course.server.util.UuidUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private static final Logger LOG = LoggerFactory.getLogger(MemberService.class);

    @Resource
    private MemberMapper memberMapper;

    @Resource
    private LoginDeviceInfoService loginDeviceInfoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MemberSectionPassMapper sectionPassMapper;

    @Autowired
    private SectionMapper sectionMapper;

    /**
     * 列表查询
     */
    public void list(MemberPageDto pageDto) {
        PageHelper.startPage(pageDto.getPage(), pageDto.getSize());
        MemberExample memberExample = new MemberExample();
        MemberExample.Criteria criteria = memberExample.createCriteria();
        if (!StringUtils.isEmpty(pageDto.getMobile())) criteria.andMobileLike('%' + pageDto.getMobile() + '%');
        if (!StringUtils.isEmpty(pageDto.getName())) criteria.andNameLike('%' + pageDto.getName() + '%');
        List<Member> memberList = memberMapper.selectByExample(memberExample);

        PageInfo<Member> pageInfo = new PageInfo<>(memberList);
        pageDto.setTotal(pageInfo.getTotal());
        List<MemberDto> memberDtoList = CopyUtil.copyList(memberList, MemberDto.class);
        memberDtoList.forEach( member -> {
            MemberSectionPass memberSectionPass = sectionPassMapper.selectByMemberId(member.getId());
            if (memberSectionPass!=null){
                member.setChoice(memberSectionPass.getSectionId());
                Section section = sectionMapper.selectByPrimaryKey(memberSectionPass.getSectionId());
                if (section!=null) member.setTitle(section.getTitle());
            }
        });

        pageDto.setList(memberDtoList);
    }
    /**
     * 按手机号查找
     * @param mobile
     * @return
     */
    public Member findByPhone(String mobile) {

        return memberMapper.findByPhone(mobile);
    }
    /**
     * 保存，id有值时更新，无值时新增
     */
    public void save(MemberDto memberDto) {
        Member member = CopyUtil.copy(memberDto, Member.class);
        MemberExample example = new MemberExample();
        MemberExample.Criteria criteria = example.createCriteria();
        criteria.andMobileEqualTo(member.getMobile());

        if (!StringUtils.isEmpty(memberDto.getId())) criteria.andIdNotEqualTo(memberDto.getId());
        if (memberMapper.countByExample(example) > 0) throw new BusinessException(BusinessExceptionCode.USER_LOGIN_NAME_EXIST);
        if (StringUtils.isEmpty(memberDto.getId())) {
            // 密码加密
            member.setPassword(DigestUtils.md5DigestAsHex(memberDto.getPassword().getBytes()));
            this.insert(member);
        } else {
            Member byPrimaryKey = memberMapper.selectByPrimaryKey(memberDto.getId());
            if (byPrimaryKey == null) throw new BusinessException(BusinessExceptionCode.USER_NOT_FOUND);
            if (!byPrimaryKey.getPassword().equals(memberDto.getPassword()))
                member.setPassword(DigestUtils.md5DigestAsHex(memberDto.getPassword().getBytes()));
            this.update(member);
        }

        if (memberDto.getChoice()!=null){
            ThreadUtil.execAsync(()->{
                Section section = sectionMapper.selectByPrimaryKey(memberDto.getChoice());
                List<Section> sections=sectionMapper.selectByTime(section.getCreatedAt());
                sectionPassMapper.deleteByUser(member.getId());
                for (Section s:
                        sections) {
                    MemberSectionPass memberSectionPass=new MemberSectionPass();
                    memberSectionPass.setId(UuidUtil.getShortUuid());
                    memberSectionPass.setMemberId(member.getId());
                    memberSectionPass.setSectionId(s.getId());
                    memberSectionPass.setCreateAt(new Date());
                    memberSectionPass.setPass(1);
                    sectionPassMapper.insert(memberSectionPass);
                }
            });
        }
    }

    /**
     * 新增
     */
    private void insert(Member member) {
        Date now = new Date();
        member.setId(UuidUtil.getShortUuid());
        member.setRegisterTime(now);
        memberMapper.insert(member);
    }

    /**
     * 更新
     */
    private void update(Member member) {
        memberMapper.updateByPrimaryKey(member);
    }

    /**
     * 删除
     */
    public void delete(String id) {
        memberMapper.deleteByPrimaryKey(id);
    }

    /**
     * 按手机号查找
     * @param mobile
     * @return
     */
    public MemberDto findByMobile(String mobile) {
        Member member = this.selectByMobile(mobile);
        return CopyUtil.copy(member, MemberDto.class);
    }

    /**
     * 按手机号查找
     * @param mobile
     * @return
     */
    public Member selectByMobile(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return null;
        }
        MemberExample example = new MemberExample();
        example.createCriteria().andMobileEqualTo(mobile);
        List<Member> memberList = memberMapper.selectByExample(example);
        if (memberList == null || memberList.size() == 0) {
            return null;
        } else {
            return memberList.get(0);
        }

    }

    /**
     * 登录
     * @param memberDto
     */
    public LoginMemberDto login(MemberDto memberDto) {
        Member member = selectByMobile(memberDto.getMobile());
        if (member == null) {
            LOG.info("手机号不存在, {}", memberDto.getMobile());
            throw new BusinessException(BusinessExceptionCode.LOGIN_MEMBER_ERROR);
        } else {
            if (member.getPassword().equals(memberDto.getPassword())) {
                // 登录成功
                LoginMemberDto loginMemberDto = CopyUtil.copy(member, LoginMemberDto.class);
                String memberId = loginMemberDto.getId();
                String deviceId = memberDto.getDeviceId();
                Integer deviceType = memberDto.getDeviceType();
                String deviceName = memberDto.getDeviceName();
                loginMemberDto.setFlag("1");
                loginMemberDto.setDeviceId(memberDto.getDeviceId());
                if (MemberRoleEnum.ADMINISTRATOR.getCode().equals(member.getRole())){return loginMemberDto;}
                // 判断设备ID是否存在
                if (deviceId == null || deviceType == null || loginDeviceInfoService.countByMemberIdAndDeviceId(memberId, deviceId) <= 0) {
                    // 判断登陆设备是否达到上限
                    long deviceNumber = loginDeviceInfoService.countByMemberId(memberId);
                    if (deviceNumber >= member.getDeviceLimitNum()){
                        loginMemberDto.setFlag("0");
                        return loginMemberDto;
                    }
                    if(deviceType != null && deviceId != null) loginDeviceInfoService.saveLoginDevice(memberId, deviceId, deviceType,deviceName);
                }

                return loginMemberDto;
            } else {
                LOG.info("密码不对, 输入密码：{}, 数据库密码：{}", memberDto.getPassword(), member.getPassword());
                throw new BusinessException(BusinessExceptionCode.LOGIN_MEMBER_ERROR);
            }
        }
    }

    /**
     * 重置密码
     */
    public void resetPassword(MemberDto memberDto) throws BusinessException {
        Member memberDb = this.selectByMobile(memberDto.getMobile());
        if (memberDb == null) {
            throw new BusinessException(BusinessExceptionCode.MEMBER_NOT_EXIST);
        } else {
            Member member = new Member();
            member.setId(memberDb.getId());
            member.setPassword(memberDto.getPassword());
            memberMapper.updateByPrimaryKeySelective(member);
        }
    }

    public List<Member> listAll() {
        MemberExample example = new MemberExample();
        List<Member> members = memberMapper.selectByExample(example);
        return members;
    }

    public void saveAll(List<Member> memberList) {
        if (CollUtil.isEmpty(memberList)) return;

        Set<String> mobileSet = memberList.stream().map(Member::getMobile).collect(Collectors.toSet());
        MemberExample example = new MemberExample();
        example.createCriteria().andMobileIn(new ArrayList<>(mobileSet));
        memberMapper.deleteByExample(example);

        for (Member member : memberList) {
            insert(member);
        }
    }

    public LoginMemberDto getLoginMember(String token) {
        Object object = redisTemplate.opsForValue().get(token);
        if(object == null) throw new BusinessException(BusinessExceptionCode.MEMBER_NOT_EXIST);
        return JSONUtil.toBean(String.valueOf(object), LoginMemberDto.class);
    }

    public int updateMobile(String token, String mobile) {
        LoginMemberDto loginMember = this.getLoginMember(token);
        Member member = new Member();
        member.setId(loginMember.getId());
        member.setPhone(mobile);
        return memberMapper.updateByPrimaryKeySelective(member);
    }

    public Object updateUserLat(String token, MemberDto memberDto) {
        LoginMemberDto loginMember = this.getLoginMember(token);
        Member member = new Member();
        member.setId(loginMember.getId());
        member.setLat(memberDto.getLat());
        member.setLng(memberDto.getLng());
        return memberMapper.updateByPrimaryKeySelective(member);
    }

    public List<MemberDto> listH5(MemberDto memberDto,String token) {
        MemberExample memberExample = new MemberExample();
        MemberExample.Criteria criteria = memberExample.createCriteria();
        if (!StringUtils.isEmpty(memberDto.getMobile())) criteria.andMobileLike('%' + memberDto.getMobile() + '%');
        if (!StringUtils.isEmpty(memberDto.getName())) criteria.andNameLike('%' + memberDto.getName() + '%');
         memberExample.setId(this.getLoginMember(token).getId());
        List<Member> memberList = memberMapper.selectByExampleH5(memberExample);
        List<MemberDto> memberDtoList = CopyUtil.copyList(memberList, MemberDto.class);
        memberDtoList.forEach( member -> {
            MemberSectionPass memberSectionPass = sectionPassMapper.selectByMemberId(member.getId());
            if (memberSectionPass!=null){
                member.setChoice(memberSectionPass.getSectionId());
                Section section = sectionMapper.selectByPrimaryKey(memberSectionPass.getSectionId());
                if (section!=null) member.setTitle(section.getTitle());
            }
        });
        return memberDtoList;
    }
}
