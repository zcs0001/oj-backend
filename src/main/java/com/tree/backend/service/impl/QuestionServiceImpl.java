package com.tree.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tree.backend.model.entity.Question;
import com.tree.backend.service.QuestionService;
import com.tree.backend.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

/**
* @author TheTree
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2023-10-09 13:10:15
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService{

}




