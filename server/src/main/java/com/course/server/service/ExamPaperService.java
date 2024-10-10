package com.course.server.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.course.server.domain.*;
import com.course.server.dto.*;
import com.course.server.exception.ValidatorException;
import com.course.server.mapper.MemberSectionPassMapper;
import com.course.server.mapper.QuestionMapper;
import com.course.server.mapper.QuestionOptionMapper;
import com.course.server.util.CopyUtil;
import com.course.server.util.ValidatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExamPaperService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionOptionMapper questionOptionMapper;

    @Autowired
    private MemberSectionPassMapper memberSectionPassMapper;

    @Autowired
    private MemberService memberService;

    @Resource
    private RedisTemplate redisTemplate;

    public List<QuestionDto> list(String sectionId) {
        // 查找当前小节所有问题
        QuestionExample example = new QuestionExample();
        example.createCriteria().andSectionIdEqualTo(sectionId);
//        //从缓存中拿取设置随机函数的配置 否则设置默认值
//        Object o = redisTemplate.opsForValue().get(Constants.CONFiG_KEY);
//        if (o!=null){
//            example.setOrderByClause("RAND() LIMIT "+o);
//        }else{
//            example.setOrderByClause("RAND() LIMIT 10");
//        }

        List<Question> questions = questionMapper.selectByExample(example);

        if (CollectionUtil.isEmpty(questions)) return Collections.emptyList();

        // 查找当前小节的所有选项
        List<String> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        QuestionOptionExample optionExample = new QuestionOptionExample();
        optionExample.createCriteria().andQuestionIdIn(questionIds);
        List<QuestionOption> questionOptionList = questionOptionMapper.selectByExample(optionExample);
        List<QuestionDto> questionDtoList = CopyUtil.copyList(questions, QuestionDto.class);
        if (CollectionUtil.isEmpty(questionOptionList)) return questionDtoList;

        // 将选项插入对应小节问题
        List<QuestionOptionDto> questionOptionDtoList = CopyUtil.copyList(questionOptionList, QuestionOptionDto.class);
        Map<String, List<QuestionOptionDto>> optionMap = questionOptionDtoList.stream().collect(Collectors.groupingBy(QuestionOptionDto::getQuestionId));
        questionDtoList.forEach(question -> question.setQuestionOptions(optionMap.get(question.getId())));

        return questionDtoList;
    }

    @Transactional
    public void save(ExamPaperDto examPaper) {
        String sectionId = examPaper.getSectionId();
        ValidatorUtil.require(sectionId, "小节ID");

        // 先删除在插入
        QuestionExample example = new QuestionExample();
        example.createCriteria().andSectionIdEqualTo(sectionId);
        List<Question> questionList = questionMapper.selectByExample(example);
        if (CollectionUtil.isNotEmpty(questionList)) {
            List<String> questionIds = questionList.stream().map(Question::getId).collect(Collectors.toList());
            QuestionOptionExample optionExample = new QuestionOptionExample();
            optionExample.createCriteria().andQuestionIdIn(questionIds);
            questionOptionMapper.deleteByExample(optionExample);
            questionMapper.deleteByExample(example);
        }

        List<QuestionDto> questions = examPaper.getQuestions();
        if (CollectionUtil.isEmpty(questions)) return;

        for (QuestionDto questionDto : questions) {
            String content = questionDto.getContent();
            List<QuestionOptionDto> optionDtoList = questionDto.getQuestionOptions();
            if (StrUtil.isBlank(content)) throw new ValidatorException("存在内容为空的题目，请检查后重试");
            if (CollectionUtil.isEmpty(optionDtoList)) throw new ValidatorException("存在没有选项的题目，请检查后重试");

            Question question = CopyUtil.copy(questionDto, Question.class);
            question.setId(IdUtil.getSnowflakeNextIdStr());
            question.setSectionId(sectionId);
            questionMapper.insertSelective(question);

            List<QuestionOption> options = CopyUtil.copyList(optionDtoList, QuestionOption.class);
            int answer = 0;
            for (QuestionOption option : options) {
                if (StrUtil.isBlank(option.getContent()))
                    throw new ValidatorException("存在内容为空的选项，请检查后重试");
                option.setId(IdUtil.getSnowflakeNextIdStr());
                option.setQuestionId(question.getId());
                if (option.getAnswer() != null && option.getAnswer() == 1) {
                    if (answer == 1) throw new ValidatorException("存在题目同时存在两个答案，请检查后重试");
                    answer = 1;
                }
                questionOptionMapper.insertSelective(option);
            }

            if (answer == 0) throw new ValidatorException("存在题目没有正确答案，请检查后重试");
        }
    }

    public boolean submitExam(String token, ExamAnswerDto examAnswer) {
        String sectionId = examAnswer.getSectionId();

        QuestionExample example = new QuestionExample();
        example.createCriteria().andSectionIdEqualTo(sectionId);
        List<Question> questionList = questionMapper.selectByExample(example);

        if(CollectionUtil.isEmpty(questionList)) return true;

        List<String> questionIds = questionList.stream().map(Question::getId).collect(Collectors.toList());
        QuestionOptionExample optionExample = new QuestionOptionExample();
        // 查询正确答案
        optionExample.createCriteria().andQuestionIdIn(questionIds).andAnswerEqualTo(1);
        List<QuestionOption> questionOptionList = questionOptionMapper.selectByExample(optionExample);

        Map<String, String> answer = examAnswer.getAnswer();
        for (Question question : questionList) {
            String answerId = answer.get(question.getId());
            if(StrUtil.isBlank(answerId)) return false;
            boolean isPass = questionOptionList.stream().anyMatch(option -> option.getQuestionId()
                    .equals(question.getId()) && option.getId().equals(answerId));
            if(!isPass) return false;
        }

        // 获取当前登录会员
        LoginMemberDto loginMember = memberService.getLoginMember(token);
        // 获取通过记录
        MemberSectionPassExample memberSectionPassExample = new MemberSectionPassExample();
        memberSectionPassExample.createCriteria().andMemberIdEqualTo(loginMember.getId())
                .andSectionIdEqualTo(sectionId);
        // 若已通过则不需要再次添加通过记录
        long passCount = memberSectionPassMapper.countByExample(memberSectionPassExample);
        if (passCount > 0) return true;

        MemberSectionPass memberSectionPass = new MemberSectionPass();
        memberSectionPass.setId(IdUtil.getSnowflakeNextIdStr());
        memberSectionPass.setSectionId(sectionId);
        memberSectionPass.setMemberId(loginMember.getId());
        memberSectionPass.setPass(1);
        return memberSectionPassMapper.insert(memberSectionPass) > 0;
    }

    public List<QuestionDto> listApi(String sectionId) {
        // 查找当前小节所有问题
        QuestionExample example = new QuestionExample();
        example.createCriteria().andSectionIdEqualTo(sectionId);
        //从缓存中拿取设置随机函数的配置 否则设置默认值
        Object o = redisTemplate.opsForValue().get(Constants.CONFiG_KEY);
        if (o!=null){
            example.setOrderByClause("RAND() LIMIT "+o);
        }else{
            example.setOrderByClause("RAND() LIMIT 10");
        }

        List<Question> questions = questionMapper.selectByExample(example);

        if (CollectionUtil.isEmpty(questions)) return Collections.emptyList();

        // 查找当前小节的所有选项
        List<String> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        QuestionOptionExample optionExample = new QuestionOptionExample();
        optionExample.createCriteria().andQuestionIdIn(questionIds);
        List<QuestionOption> questionOptionList = questionOptionMapper.selectByExample(optionExample);
        List<QuestionDto> questionDtoList = CopyUtil.copyList(questions, QuestionDto.class);
        if (CollectionUtil.isEmpty(questionOptionList)) return questionDtoList;

        // 将选项插入对应小节问题
        List<QuestionOptionDto> questionOptionDtoList = CopyUtil.copyList(questionOptionList, QuestionOptionDto.class);
        Map<String, List<QuestionOptionDto>> optionMap = questionOptionDtoList.stream().collect(Collectors.groupingBy(QuestionOptionDto::getQuestionId));
        questionDtoList.forEach(question -> question.setQuestionOptions(optionMap.get(question.getId())));

        return questionDtoList;
    }
}
