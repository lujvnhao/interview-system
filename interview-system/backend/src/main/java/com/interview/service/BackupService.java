package com.interview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.dto.backup.QuestionsBackup;
import com.interview.dto.backup.TagsBackup;
import com.interview.entity.Question;
import com.interview.entity.TagConfig;
import com.interview.repository.QuestionRepository;
import com.interview.repository.TagConfigRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 数据备份服务 — 将题库数据导出为 JSON 文件，方便 Git 跟踪和灾备恢复
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {

    private final QuestionRepository questionRepository;
    private final TagConfigRepository tagConfigRepository;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    @Value("${app.backup.dir:data/backup}")
    private String backupDirPath;

    private final Object lock = new Object();
    private Path backupDir;

    @PostConstruct
    public void init() {
        backupDir = Path.of(backupDirPath).toAbsolutePath().normalize();

        try {
            Files.createDirectories(backupDir);
            log.info("备份目录已就绪: {}", backupDir);
        } catch (IOException e) {
            log.warn("无法创建备份目录: {}", backupDir, e);
        }
    }

    /**
     * 导出全部题目和标签到 JSON 文件（原子写入）
     */
    public void exportBackup() {
        synchronized (lock) {
            try {
                String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                // 导出题目
                List<Question> questions = questionRepository.findAllByOrderByIdAsc();
                QuestionsBackup qb = QuestionsBackup.builder()
                        .version(1)
                        .exportedAt(now)
                        .questions(questions)
                        .build();

                Path questionsFile = backupDir.resolve("questions.json");
                Path tempQuestions = backupDir.resolve("questions.json.tmp");
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempQuestions.toFile(), qb);
                Files.move(tempQuestions, questionsFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

                // 导出标签
                List<String> tags = tagConfigRepository.findAll()
                        .stream()
                        .map(TagConfig::getName)
                        .collect(Collectors.toList());

                TagsBackup tb = TagsBackup.builder()
                        .version(1)
                        .exportedAt(now)
                        .tags(tags)
                        .build();

                Path tagsFile = backupDir.resolve("tags.json");
                Path tempTags = backupDir.resolve("tags.json.tmp");
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempTags.toFile(), tb);
                Files.move(tempTags, tagsFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

                log.info("备份已导出: {} 道题目, {} 个标签 → {}", questions.size(), tags.size(), backupDir);
            } catch (Exception e) {
                log.error("备份导出失败", e);
            }
        }
    }

    /**
     * 加载题目备份，失败时返回 empty
     */
    public Optional<QuestionsBackup> loadQuestionsBackup() {
        Path file = backupDir.resolve("questions.json");
        if (!Files.exists(file)) {
            log.info("题目备份文件不存在: {}", file);
            return Optional.empty();
        }
        try {
            QuestionsBackup backup = objectMapper.readValue(file.toFile(), QuestionsBackup.class);
            if (backup.getQuestions() == null || backup.getQuestions().isEmpty()) {
                log.warn("题目备份文件为空或格式异常");
                return Optional.empty();
            }
            log.info("成功加载题目备份: {} 道题目 (导出时间: {})", backup.getQuestions().size(), backup.getExportedAt());
            return Optional.of(backup);
        } catch (Exception e) {
            log.warn("题目备份文件解析失败: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 加载标签备份，失败时返回 empty
     */
    public Optional<TagsBackup> loadTagsBackup() {
        Path file = backupDir.resolve("tags.json");
        if (!Files.exists(file)) {
            log.info("标签备份文件不存在: {}", file);
            return Optional.empty();
        }
        try {
            TagsBackup backup = objectMapper.readValue(file.toFile(), TagsBackup.class);
            if (backup.getTags() == null || backup.getTags().isEmpty()) {
                log.warn("标签备份文件为空或格式异常");
                return Optional.empty();
            }
            log.info("成功加载标签备份: {} 个标签 (导出时间: {})", backup.getTags().size(), backup.getExportedAt());
            return Optional.of(backup);
        } catch (Exception e) {
            log.warn("标签备份文件解析失败: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 从备份恢复数据到数据库
     * 先恢复标签，再恢复题目。创建新实体（不保留原始 ID），避免主键冲突。
     */
    @Transactional
    public void restoreFromBackup(QuestionsBackup qb, TagsBackup tb) {
        log.info("开始从备份恢复数据...");

        // 1. 恢复标签
        int tagCount = 0;
        for (String tagName : tb.getTags()) {
            if (tagName == null || tagName.isBlank()) continue;
            if (!tagConfigRepository.existsByName(tagName.trim())) {
                tagConfigRepository.save(TagConfig.builder().name(tagName.trim()).build());
                tagCount++;
            }
        }
        log.info("已恢复 {} 个标签", tagCount);

        // 2. 恢复题目 — 构建不含 ID 的新实体
        int questionCount = 0;
        for (Question src : qb.getQuestions()) {
            Question q = Question.builder()
                    .question(src.getQuestion())
                    .answer(src.getAnswer())
                    .category(src.getCategory() != null ? src.getCategory() : "未分类")
                    .tags(src.getTags() != null ? src.getTags() : "")
                    .mastered(src.getMastered() != null ? src.getMastered() : false)
                    .favorite(src.getFavorite() != null ? src.getFavorite() : false)
                    .weight(src.getWeight() != null ? src.getWeight() : 10)
                    .wrongCount(src.getWrongCount() != null ? src.getWrongCount() : 0)
                    .correctCount(src.getCorrectCount() != null ? src.getCorrectCount() : 0)
                    .reviewCount(src.getReviewCount() != null ? src.getReviewCount() : 0)
                    .lastReviewTime(src.getLastReviewTime())
                    .build();
            entityManager.persist(q);
            questionCount++;
        }

        log.info("已恢复 {} 道题目", questionCount);
    }
}
