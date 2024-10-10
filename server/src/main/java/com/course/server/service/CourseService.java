package com.course.server.service;

import cn.hutool.core.map.MapUtil;
import com.course.server.domain.*;
import com.course.server.dto.*;
import com.course.server.enums.CourseStatusEnum;
import com.course.server.mapper.CourseContentMapper;
import com.course.server.mapper.CourseMapper;
import com.course.server.mapper.MemberSectionPassMapper;
import com.course.server.mapper.my.MyCourseMapper;
import com.course.server.mapper.my.MyQuestionMapper;
import com.course.server.util.CopyUtil;
import com.course.server.util.UuidUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

@Service
public class CourseService {

    private static final Logger LOG = LoggerFactory.getLogger(CourseService.class);

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private MyCourseMapper myCourseMapper;

    @Resource
    private CourseCategoryService courseCategoryService;

    @Resource
    private CourseContentMapper courseContentMapper;

    @Resource
    private TeacherService teacherService;

    @Resource
    private ChapterService chapterService;

    @Resource
    private SectionService sectionService;

    @Autowired
    private MemberSectionPassMapper memberSectionPassMapper;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MyQuestionMapper questionMapper;

    /**
     * 列表查询：关联课程分类表
     * @param pageDto
     */
    public void list(CoursePageDto pageDto) {
        PageHelper.startPage(pageDto.getPage(), pageDto.getSize());
        List<CourseDto> courseDtoList = myCourseMapper.list(pageDto);
        PageInfo<CourseDto> pageInfo = new PageInfo<>(courseDtoList);
        pageDto.setTotal(pageInfo.getTotal());
        pageDto.setList(courseDtoList);
    }

    /**
     * 新课列表查询，只查询已发布的，按创建日期倒序
     */
    public List<CourseDto> listNew(PageDto pageDto) {
        PageHelper.startPage(pageDto.getPage(), pageDto.getSize());
        CourseExample courseExample = new CourseExample();
        courseExample.createCriteria().andStatusEqualTo(CourseStatusEnum.PUBLISH.getCode());
        courseExample.setOrderByClause("created_at desc");
        List<Course> courseList = courseMapper.selectByExample(courseExample);
        return CopyUtil.copyList(courseList, CourseDto.class);
    }

    /**
     * 保存，id有值时更新，无值时新增
     */
    @Transactional
    public void save(CourseDto courseDto) {
        Course course = CopyUtil.copy(courseDto, Course.class);
        if (StringUtils.isEmpty(courseDto.getId())) {
            this.insert(course);
        } else {
            this.update(course);
        }

        // 批量保存课程分类
        courseCategoryService.saveBatch(course.getId(), courseDto.getCategorys());
    }

    /**
     * 新增
     */
    private void insert(Course course) {
        Date now = new Date();
        course.setCreatedAt(now);
        course.setUpdatedAt(now);
        course.setId(UuidUtil.getShortUuid());
        courseMapper.insert(course);
    }

    /**
     * 更新
     */
    private void update(Course course) {
        course.setUpdatedAt(new Date());
        courseMapper.updateByPrimaryKey(course);
    }

    /**
     * 删除
     */
    public void delete(String id) {
        courseMapper.deleteByPrimaryKey(id);
    }

    /**
     * 更新课程时长
     * @param courseId
     * @return
     */
    public void updateTime(String courseId) {
        LOG.info("更新课程时长：{}", courseId);
        myCourseMapper.updateTime(courseId);
    }

    /**
     * 查找课程内容
     */
    public CourseContentDto findContent(String id) {
        CourseContent content = courseContentMapper.selectByPrimaryKey(id);
        if (content == null) {
            return null;
        }
        return CopyUtil.copy(content, CourseContentDto.class);
    }

    /**
     * 保存课程内容，包含新增和修改
     */
    public int saveContent(CourseContentDto contentDto) {
        CourseContent content = CopyUtil.copy(contentDto, CourseContent.class);
        int i = courseContentMapper.updateByPrimaryKeyWithBLOBs(content);
        if (i == 0) {
            i = courseContentMapper.insert(content);
        }
        return i;
    }

    /**
     * 排序
     * @param sortDto
     */
    @Transactional
    public void sort(SortDto sortDto) {
        // 修改当前记录的排序值
        myCourseMapper.updateSort(sortDto);

        // 如果排序值变大
        if (sortDto.getNewSort() > sortDto.getOldSort()) {
            myCourseMapper.moveSortsForward(sortDto);
        }

        // 如果排序值变小
        if (sortDto.getNewSort() < sortDto.getOldSort()) {
            myCourseMapper.moveSortsBackward(sortDto);
        }
    }

    /**
     * 查找某一课程，供web模块用，只能查已发布的
     *
     * @param token
     * @param id
     * @return
     */
    public CourseDto findCourse(String token, String id) {
        Course course = courseMapper.selectByPrimaryKey(id);
        if (course == null || !CourseStatusEnum.PUBLISH.getCode().equals(course.getStatus())) {
            return null;
        }
        CourseDto courseDto = CopyUtil.copy(course, CourseDto.class);

        // 查询内容
        CourseContent content = courseContentMapper.selectByPrimaryKey(id);
        if (content != null) {
            courseDto.setContent(content.getContent());
        }

        // 查找讲师信息
        TeacherDto teacherDto = teacherService.findById(courseDto.getTeacherId());
        courseDto.setTeacher(teacherDto);

        // 查找章信息
        List<ChapterDto> chapterDtoList = chapterService.listByCourse(id);
        courseDto.setChapters(chapterDtoList);

        // 查找节信息
        List<SectionDto> sectionDtoList = sectionService.listByCourse(id);
        courseDto.setSections(sectionDtoList);

        LoginMemberDto loginMember = memberService.getLoginMember(token);
        MemberDto currentMember = memberService.findByMobile(loginMember.getMobile());
        if (currentMember.getDoQuestion().equals(0)) {
            return courseDto;
        }

        // 设置每章小节题目数量
        List<Map<String, Object>> sectionQuestionNumList = questionMapper.countGroupBySection();
        Map<String, Long> sectionQuestionNumMap = new HashMap<>();
        sectionQuestionNumList.forEach(map -> {
            String sectionId = MapUtil.getStr(map, "section_id");
            Long num = MapUtil.getLong(map, "num");
            sectionQuestionNumMap.put(sectionId, num);
        });
        sectionDtoList.forEach(sectionDto -> {
            sectionDto.setQuestionNum(sectionQuestionNumMap.getOrDefault(sectionDto.getId(), 0L));
            if (sectionDto.getQuestionNum() == 0L) sectionDto.setIsPass(1);
            else sectionDto.setIsPass(0);
        });

        // 查找当前用户的小节的答题通过记录
        MemberSectionPassExample memberSectionPassExample = new MemberSectionPassExample();
        memberSectionPassExample.createCriteria().andMemberIdEqualTo(loginMember.getId());
        List<MemberSectionPass> sectionPassList = memberSectionPassMapper.selectByExample(memberSectionPassExample);

        // 设置小节的通过状态
        for (MemberSectionPass memberSectionPass : sectionPassList) {
            Optional<SectionDto> sectionOptional = sectionDtoList.stream().filter(section -> section.getId().equals(memberSectionPass.getSectionId())).findAny();
            if(sectionOptional.isPresent()) {
                SectionDto section = sectionOptional.get();
                section.setIsPass(memberSectionPass.getPass());
            }
        }

        // 设置大章通过状态
        for (ChapterDto chapterDto : chapterDtoList) {
            if (chapterDto.getDoQuestion().equals(1)) chapterDto.setIsPass(sectionDtoList.stream().filter(section -> chapterDto.getId().equals(section.getChapterId()))
                    .anyMatch(section -> section.getIsPass().equals(0)) ? 0 : 1);
            else chapterDto.setIsPass(1);
        }

        return courseDto;
    }
}
