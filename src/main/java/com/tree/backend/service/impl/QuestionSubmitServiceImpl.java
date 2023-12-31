package com.tree.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tree.backend.common.ErrorCode;
import com.tree.backend.constant.CommonConstant;
import com.tree.backend.exception.BusinessException;
import com.tree.backend.exception.ThrowUtils;
import com.tree.backend.judge.JudgeService;
import com.tree.backend.model.dto.questionsumbit.QuestionSubmitAddRequest;
import com.tree.backend.model.dto.questionsumbit.QuestionSubmitQueryRequest;
import com.tree.backend.model.entity.Question;
import com.tree.backend.model.entity.QuestionSubmit;
import com.tree.backend.model.entity.User;
import com.tree.backend.model.enums.QuestionSubmitLanguageEnum;
import com.tree.backend.model.enums.QuestionSubmitStatusEnum;
import com.tree.backend.model.vo.QuestionSubmitVO;
import com.tree.backend.service.QuestionService;
import com.tree.backend.service.QuestionSubmitService;
import com.tree.backend.mapper.QuestionSubmitMapper;
import com.tree.backend.service.UserService;
import com.tree.backend.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
* @author TheTree
* @description 针对表【question_submit(题目提交)】的数据库操作Service实现
* @createDate 2023-10-09 13:11:03
*/
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
    implements QuestionSubmitService{
    @Resource
    private UserService userService;

    @Resource
    private QuestionService questionService;

    @Resource
    @Lazy
    private JudgeService judgeService;
//    @Resource
//    private CodeMqProducer codeMqProducer;


    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest 请求包装类
     * @param loginUser                登录用户
     * @return 提交题目 id
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        Long questionId = questionSubmitAddRequest.getQuestionId();
        String submitLanguage = questionSubmitAddRequest.getSubmitLanguage();
        String submitCode = questionSubmitAddRequest.getSubmitCode();
        // 校验编程语言是否正确
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(submitLanguage);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }

        // 判断问题是否存在，根据ID获取问题
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 设置提交数
        Question updateQuestion = questionService.getById(questionId);
        synchronized (question.getSubmitNum()) {
            int submitNum = question.getSubmitNum() + 1;
            updateQuestion.setSubmitNum(submitNum);
            boolean save = questionService.updateById(updateQuestion);
            if (!save) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据保存失败");
            }
        }

        // 是否已提交题目
        long userId = loginUser.getId();
        // 每个用户串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setSubmitCode(submitCode);
        questionSubmit.setSubmitLanguage(submitLanguage);
        // 设置初始状态
        questionSubmit.setSubmitStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);
//        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "数据保存失败");

        Long questionSubmitId = questionSubmit.getId();
//        // 生产者发送消息
//        codeMqProducer.sendMessage(CODE_EXCHANGE_NAME, CODE_ROUTING_KEY, String.valueOf(questionSubmitId));
        // 执行判题服务\
        /**
         * 在保存之后，按理讲应该先判断是否保存成功，也就是用户对于该题是否已经提交过代码
         * 而这里使用CompletableFuture.runAsync实现异步操作，在一个新线程中执行判题操作，120行的判断操作也不影响执行
         * 如果判题时间较长，在判题没有结束之前，此方法也会得到返回结果，返回具有上述设置初始信息的题目提交信息questionSubmitId，返回的结果在前端显示为
         * JudgeInfo：{null,null,null},SubmitStatus=QuestionSubmitStatusEnum.WAITING,也就是判题中
         * 优势：
         * 判题服务是异步执行的，不会阻塞主线程，提高了系统的并发性和响应性。
         * 判题服务的执行与判断是否提交过题目的逻辑并发执行，可以提高系统的效率。
         * 劣势：
         * 如果判题服务执行失败，可能会导致用户已经提交的记录没有被判定，需要额外的处理来处理这种异常情况。已在doJudge中通过抛出异常实现
         */
         CompletableFuture.runAsync(() -> {
             System.out.println("Executing doJudge");
             judgeService.doJudge(questionSubmitId);
             System.out.println("doJudge completed");
         });
        if (!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"数据插入失败");
        }
        return questionSubmitId;
    }

    /**
     * 获取查询包装类（用户根据哪些字段查询，根据前端传来的请求对象）
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {


        Long questionId = questionSubmitQueryRequest.getQuestionId();
        String submitLanguage = questionSubmitQueryRequest.getSubmitLanguage();
        Integer submitStatus = questionSubmitQueryRequest.getSubmitStatus();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }

        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(submitLanguage), "submitLanguage", submitLanguage);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(submitStatus) != null, "submitStatus", submitStatus);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取查询封装类（单个）
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        // 脱敏：仅本人和管理员能看见自己（提交 userId 和登录用户 id 不同）提交的代码
        long userId = loginUser.getId();
        // 处理脱敏
        if (userId != questionSubmit.getUserId() && !userService.isAdmin(loginUser)) {
            questionSubmitVO.setSubmitCode(null);
        }
        return questionSubmitVO;
    }

    /**
     * 获取查询脱敏信息
     * @param questionSubmitPage 题目提交分页
     * @param loginUser          直接获取到用户信息，减少查询数据库
     * @return
     */
    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream()
                .map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser))
                .collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }
}




