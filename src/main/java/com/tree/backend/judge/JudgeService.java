package com.tree.backend.judge;

import com.tree.backend.model.entity.QuestionSubmit;

public interface JudgeService {


    QuestionSubmit doJudge(long questionSubmitId);

}