package com.tree.backend.judge;

import com.tree.backend.judge.codesandbox.model.JudgeInfo;
import com.tree.backend.judge.strategy.DefaultJudgeStrategy;
import com.tree.backend.judge.strategy.JavaLanguageJudgeStrategy;
import com.tree.backend.judge.strategy.JudgeContext;
import com.tree.backend.judge.strategy.JudgeStrategy;
import com.tree.backend.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 * @author Shier
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getSubmitLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)) {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }
}