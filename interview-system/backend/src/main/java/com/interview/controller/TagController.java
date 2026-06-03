package com.interview.controller;

import com.interview.common.Result;
import com.interview.entity.TagConfig;
import com.interview.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 标签管理控制器
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /** 获取所有标签（系统标签 + 题目中已有的标签合并） */
    @GetMapping
    public Result<List<String>> list() {
        return Result.success(tagService.listTags());
    }

    /** 新增标签 */
    @PostMapping
    public Result<TagConfig> create(@RequestBody Map<String, String> body) {
        TagConfig tag = tagService.createTag(body.get("name"));
        return Result.success("新增成功", tag);
    }

    /** 删除标签（同时清除所有题目中的该标签） */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success("删除成功，已清除相关题目中的该标签");
    }

    /** 按名称删除标签（同时清除所有题目中的该标签） */
    @DeleteMapping("/name/{name}")
    public Result<?> deleteByName(@PathVariable String name) {
        tagService.deleteTagByName(name);
        return Result.success("删除成功，已清除相关题目中的该标签");
    }

    /** 批量给指定题目添加标签 */
    @PostMapping("/batch-add")
    public Result<Map<String, Object>> batchAddTag(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> questionIdsRaw = (List<Integer>) body.get("questionIds");
        String tagName = (String) body.get("tagName");

        List<Long> questionIds = questionIdsRaw.stream()
                .map(Long::valueOf)
                .collect(java.util.stream.Collectors.toList());

        Map<String, Object> result = tagService.batchAddTag(questionIds, tagName);
        return Result.success("成功为 " + result.get("updated") + " 道题目添加标签", result);
    }

    /** 找出所有标签为空的题目 */
    @GetMapping("/empty-tag-questions")
    public Result<List<Map<String, Object>>> emptyTagQuestions() {
        return Result.success(tagService.emptyTagQuestions());
    }
}
