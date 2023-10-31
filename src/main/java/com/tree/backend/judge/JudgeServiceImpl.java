package com.tree.backend.judge;

import cn.hutool.json.JSONUtil;
import com.tree.backend.common.ErrorCode;
import com.tree.backend.exception.BusinessException;
import com.tree.backend.judge.codesandbox.CodeSandBoxProxy;
import com.tree.backend.judge.codesandbox.CodeSandbox;
import com.tree.backend.judge.codesandbox.CodeSandboxFactory;
import com.tree.backend.judge.codesandbox.model.ExecuteCodeRequest;
import com.tree.backend.judge.codesandbox.model.ExecuteCodeResponse;
import com.tree.backend.judge.codesandbox.model.JudgeInfo;
import com.tree.backend.judge.strategy.JudgeContext;
import com.tree.backend.model.dto.question.JudgeCase;
import com.tree.backend.model.entity.Question;
import com.tree.backend.model.entity.QuestionSubmit;
import com.tree.backend.model.enums.QuestionSubmitStatusEnum;
import com.tree.backend.service.QuestionService;
import com.tree.backend.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private JudgeManager judgeManager;

    /**
     * 这是Spring框架的@Value注解，用于从配置文件中读取属性值。
     * 具体来说，这行代码的作用是从配置文件中读取名为codesandbox.type的属性值，如果该属性值不存在，则使用默认值"example"。
     * 在这里，:example表示默认值是"example"。
     */
    @Value("${codesandbox.type:example}")
    private String judgeType;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {

        // 1、传入题目的提交 id，获取到对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }

        // 通过提交的信息中的题目id 获取到题目的全部信息
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (questionId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 2、因为对于该题目的初始状态设置了等待中，且是异步执行的，不好准确判断判题服务执行到什么地方了，因此需要进行判断。
        // 如果题目提交状态不为等待中,说明该题目状态改变，进入正在判题中状态或判题结束,不允许再次提交判题请求。
        if (!questionSubmit.getSubmitStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        // 3、判题状态仍然是等待中，更改判题（题目提交）的状态为 “判题中”，防止重复执行，也能让用户即时看到状态
        QuestionSubmit updateQuestionSubmit = new QuestionSubmit();
        updateQuestionSubmit.setId(questionSubmitId);
        updateQuestionSubmit.setSubmitStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean updateStatus = questionSubmitService.updateById(updateQuestionSubmit);
        if (!updateStatus) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新失败");
        }

        //4、调用沙箱，获取到执行结果
        // 代码沙箱静态工厂创建代码沙箱示例
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(judgeType);
        // 使用代码对代码沙箱的功能进行增强
        codeSandbox = new CodeSandBoxProxy(codeSandbox);

        String submitLanguage = questionSubmit.getSubmitLanguage();
        String submitCode = questionSubmit.getSubmitCode();
        // 获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCasesList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        // 通过Lambda表达式获取到每个题目的输入用例
        List<String> inputList = judgeCasesList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        // 调用沙箱
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(submitCode)
                .language(submitLanguage)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        List<String> outputList = executeCodeResponse.getOutputList();

        // 5、根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaseList(judgeCasesList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);

        // 进入到代码沙箱，执行程序，返回执行结果
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
        // 6、修改判题结果
        updateQuestionSubmit = new QuestionSubmit();
        updateQuestionSubmit.setId(questionSubmitId);
        updateQuestionSubmit.setSubmitStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        updateQuestionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        updateStatus = questionSubmitService.updateById(updateQuestionSubmit);
        if (!updateStatus) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新失败");
        }
        // 再次查询数据库，返回最新提交信息
        QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionId);
        return questionSubmitResult;
    }
}
