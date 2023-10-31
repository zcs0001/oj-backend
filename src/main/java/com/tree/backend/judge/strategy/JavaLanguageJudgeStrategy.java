package com.tree.backend.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.tree.backend.judge.codesandbox.model.JudgeInfo;
import com.tree.backend.model.dto.question.JudgeCase;
import com.tree.backend.model.dto.question.JudgeConfig;
import com.tree.backend.model.entity.Question;
import com.tree.backend.model.enums.JudgeInfoMessageEnum;

import java.util.List;
import java.util.Optional;

/**
 * java判题策略
 */
public class JavaLanguageJudgeStrategy implements JudgeStrategy{
    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {

        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        //安全地获取执行内存值和时间值，避免了可能的 NullPointerException。如果执行内存值为 null，则使用默认值 0L。
        Long executedMemory = Optional.ofNullable(judgeInfo.getMemory()).orElse(0L);
        Long executedTime = Optional.ofNullable(judgeInfo.getTime()).orElse(0L);

        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();

        Question question = judgeContext.getQuestion();
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();
        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;

        JudgeInfo judgeInfoResponse = new JudgeInfo();
        judgeInfoResponse.setMemory(executedMemory);
        judgeInfoResponse.setTime(executedTime);
        // 先判断沙箱执行的结果输出数量是否和预期输出数量相等
        if (outputList.size() != inputList.size()) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getText());
            return judgeInfoResponse;
        }
        // 依次判断每一项输出和预期输出是否相等
        for (int i = 0; i < judgeCaseList.size(); i++) {
            JudgeCase judgeCase = judgeCaseList.get(i);
            if (!judgeCase.getOutput().equals(outputList.get(i))) {
                judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getText());
                return judgeInfoResponse;
            }
        }
        // 判断题目限制
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long needMemoryLimit = judgeConfig.getMemoryLimit();
        Long needTimeLimit = judgeConfig.getTimeLimit();
        if (executedMemory > needMemoryLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getText());
            return judgeInfoResponse;
        }
        // Java 程序本身需要额外执行 5 秒钟
        long JAVA_PROGRAM_TIME_COST = 5000L;
        if ((executedTime - JAVA_PROGRAM_TIME_COST) > needTimeLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getText());
            return judgeInfoResponse;
        }
//        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getText());
        return judgeInfoResponse;
    }
}
