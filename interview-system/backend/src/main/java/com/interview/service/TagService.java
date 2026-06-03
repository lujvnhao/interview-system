package com.interview.service;

import com.interview.entity.TagConfig;

import java.util.List;
import java.util.Map;

/**
 * 标签服务接口
 */
public interface TagService {

    /** 获取所有标签（系统标签 + 题目中标签合并） */
    List<String> listTags();

    /** 新增标签 */
    TagConfig createTag(String name);

    /** 删除标签（同时清除所有题目中的该标签） */
    void deleteTag(Long id);

    /** 按名称删除标签 */
    void deleteTagByName(String name);

    /** 批量给指定题目添加标签 */
    Map<String, Object> batchAddTag(List<Long> questionIds, String tagName);

    /** 获取所有未打标签的题目 */
    List<Map<String, Object>> emptyTagQuestions();
}
