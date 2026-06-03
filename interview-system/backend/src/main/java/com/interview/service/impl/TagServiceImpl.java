package com.interview.service.impl;

import com.interview.common.exception.BusinessException;
import com.interview.common.exception.NotFoundException;
import com.interview.entity.Question;
import com.interview.entity.TagConfig;
import com.interview.repository.QuestionRepository;
import com.interview.repository.TagConfigRepository;
import com.interview.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 标签服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagConfigRepository tagConfigRepository;
    private final QuestionRepository questionRepository;

    @Override
    public List<String> listTags() {
        Set<String> all = tagConfigRepository.findAll()
                .stream().map(TagConfig::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        questionRepository.findAll().stream()
                .map(Question::getTags)
                .filter(t -> t != null && !t.isBlank())
                .flatMap(t -> Arrays.stream(t.split(",")))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .forEach(all::add);

        return new ArrayList<>(all);
    }

    @Override
    @Transactional
    public TagConfig createTag(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("标签名不能为空");
        }
        name = name.trim();
        if (tagConfigRepository.existsByName(name)) {
            throw new BusinessException("标签已存在");
        }
        TagConfig tag = TagConfig.builder().name(name).build();
        return tagConfigRepository.save(tag);
    }

    @Override
    @Transactional
    public void deleteTag(Long id) {
        TagConfig tag = tagConfigRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("标签", id));
        String tagName = tag.getName();
        tagConfigRepository.delete(tag);
        removeTagFromAllQuestions(tagName);
    }

    @Override
    @Transactional
    public void deleteTagByName(String name) {
        tagConfigRepository.deleteByName(name);
        removeTagFromAllQuestions(name);
    }

    @Override
    @Transactional
    public Map<String, Object> batchAddTag(List<Long> questionIds, String tagName) {
        if (questionIds == null || questionIds.isEmpty()) {
            throw new BusinessException("请选择至少一道题目");
        }
        if (tagName == null || tagName.isBlank()) {
            throw new BusinessException("标签名不能为空");
        }
        tagName = tagName.trim();

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
        return result;
    }

    @Override
    public List<Map<String, Object>> emptyTagQuestions() {
        return questionRepository.findAll().stream()
                .filter(q -> q.getTags() == null || q.getTags().isBlank())
                .map(q -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", q.getId());
                    m.put("question", q.getQuestion());
                    m.put("category", q.getCategory());
                    return m;
                })
                .collect(Collectors.toList());
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
