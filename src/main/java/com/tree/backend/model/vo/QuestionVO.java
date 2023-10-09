package com.tree.backend.model.vo;

import cn.hutool.json.JSONUtil;
import com.tree.backend.model.dto.question.JudgeConfig;
import com.tree.backend.model.entity.Question;
import lombok.Data;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.BeanUtils;

import javax.json.Json;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题目
 * 过滤不能传给前端的属性，同时减少传给前端对象的大小
 */
@Data
public class  QuestionVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 创建题目用户 id
     */
    private Long userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表，直接返回java对象，前端就不用再去解析json对象
     */
    private List<String> tags;

    /**
     * 判题配置
     */
    private JudgeConfig judgeConfig;

    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户封装类,用于查找题目创建人的信息
     */
    private UserVO userVO;

    /**
     * 包装类转对象
     * @param questionVO
     * @return
     */
    public static Question voToObj(QuestionVO questionVO) {
        if (questionVO == null) {
            return null;
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionVO, question);
//        在Question中，因为要存到数据库中，tags是一个String对象
//        而在QuetionVo中，为了前端更方便的使用tags，我们返回的是一个List对象，因此需要进行转换
//        这里直接将数组转为字符串，存到数据库里
        List<String> tagList = questionVO.getTags();
        if (tagList != null) {
            question.setTags(JSONUtil.toJsonStr(tagList));
        }

        JudgeConfig voJudgeConfig = questionVO.getJudgeConfig();
        if (voJudgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(voJudgeConfig));
        }
        return question;
    }

    /**
     * 对象转包装类
     * @param question
     * @return
     */
    public static QuestionVO objToVo(Question question) {
        if (question == null) {
            return null;
        }
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);
        // 转换成包装类
//        这里将Json字符串转为Java对象
        List<String> tagList = JSONUtil.toList(question.getTags(), String.class);
        questionVO.setTags(tagList);

        String judgeConfig = question.getJudgeConfig();
        questionVO.setJudgeConfig(JSONUtil.toBean(judgeConfig, JudgeConfig.class));
        return questionVO;
    }

    private static final long serialVersionUID = 1L;
}