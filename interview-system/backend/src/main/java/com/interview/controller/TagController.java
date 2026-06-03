package com.interview.controller;

import com.interview.common.Result;
import com.interview.entity.TagConfig;
import com.interview.entity.Question;
import com.interview.repository.QuestionRepository;
import com.interview.repository.TagConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 标签管理控制器
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TagController {

    private final TagConfigRepository tagConfigRepository;
    private final QuestionRepository questionRepository;

    /** 获取所有标签（系统标签 + 题目中已有的标签合并） */
    @GetMapping
    public Result<List<String>> list() {
        Set<String> all = tagConfigRepository.findAll()
                .stream().map(TagConfig::getName).collect(Collectors.toCollection(LinkedHashSet::new));

        questionRepository.findAll().stream()
                .map(Question::getTags)
                .filter(t -> t != null && !t.isBlank())
                .flatMap(t -> Arrays.stream(t.split(",")))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .forEach(all::add);

        return Result.success(new ArrayList<>(all));
    }

    /** 新增标签 */
    @PostMapping
    @Transactional
    public Result<TagConfig> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return Result.paramError("标签名不能为空");
        }
        name = name.trim();
        if (tagConfigRepository.existsByName(name)) {
            return Result.paramError("标签已存在");
        }
        TagConfig tag = TagConfig.builder().name(name).build();
        tagConfigRepository.save(tag);
        return Result.success("新增成功", tag);
    }

    /** 删除标签（同时清除所有题目中的该标签） */
    @DeleteMapping("/{id}")
    @Transactional
    public Result<?> delete(@PathVariable Long id) {
        TagConfig tag = tagConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("标签不存在"));
        String tagName = tag.getName();
        tagConfigRepository.delete(tag);
        removeTagFromAllQuestions(tagName);
        return Result.success("删除成功，已清除相关题目中的该标签");
    }

    /** 按名称删除标签（同时清除所有题目中的该标签） */
    @DeleteMapping("/name/{name}")
    @Transactional
    public Result<?> deleteByName(@PathVariable String name) {
        tagConfigRepository.deleteByName(name);
        removeTagFromAllQuestions(name);
        return Result.success("删除成功，已清除相关题目中的该标签");
    }

    /** 批量给指定题目添加标签 */
    @PostMapping("/batch-add")
    @Transactional
    public Result<Map<String, Object>> batchAddTag(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> questionIdsRaw = (List<Integer>) body.get("questionIds");
        String tagName = (String) body.get("tagName");

        if (questionIdsRaw == null || questionIdsRaw.isEmpty()) {
            return Result.paramError("请选择至少一道题目");
        }
        if (tagName == null || tagName.isBlank()) {
            return Result.paramError("标签名不能为空");
        }
        tagName = tagName.trim();

        List<Long> questionIds = questionIdsRaw.stream().map(Long::valueOf).collect(Collectors.toList());
        List<Question> questions = questionRepository.findAllById(questionIds);

        int updated = 0;
        for (Question q : questions) {
            String currentTags = q.getTags() != null ? q.getTags() : "";
            Set<String> tagSet = new LinkedHashSet<>(
                    Arrays.stream(currentTags.split(","))
                            .map(String::trim)
                            .filter(t -> !t.isEmpty())
                            .collect(Collectors.toList())
            );
            if (tagSet.add(tagName)) {
                q.setTags(String.join(",", tagSet));
                questionRepository.save(q);
                updated++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("updated", updated);
        result.put("total", questions.size());
        return Result.success("成功为 " + updated + " 道题目添加标签", result);
    }

    /** 找出所有标签为空的题目 */
    @GetMapping("/empty-tag-questions")
    public Result<List<Map<String, Object>>> emptyTagQuestions() {
        List<Map<String, Object>> list = questionRepository.findAll().stream()
                .filter(q -> q.getTags() == null || q.getTags().isBlank())
                .map(q -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", q.getId());
                    m.put("question", q.getQuestion());
                    m.put("category", q.getCategory());
                    return m;
                })
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /** 清除所有题目中的指定标签 */
    private void removeTagFromAllQuestions(String tagName) {
        List<Question> all = questionRepository.findAll();
        for (Question q : all) {
            String tags = q.getTags();
            if (tags == null || tags.isBlank()) continue;

            Set<String> tagSet = new LinkedHashSet<>(
                    Arrays.stream(tags.split(","))
                            .map(String::trim)
                            .filter(t -> !t.isEmpty() && !t.equals(tagName))
                            .collect(Collectors.toList())
            );

            String newTags = String.join(",", tagSet);
            if (!newTags.equals(tags)) {
                q.setTags(newTags.isEmpty() ? "" : newTags);
                questionRepository.save(q);
            }
        }
    }
}
