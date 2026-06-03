package com.interview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.common.exception.BusinessException;
import com.interview.dto.backup.QuestionsBackup;
import com.interview.dto.backup.TagsBackup;
import com.interview.entity.Question;
import com.interview.entity.TagConfig;
import com.interview.repository.QuestionRepository;
import com.interview.repository.TagConfigRepository;
import com.interview.vo.BackupInfoVO;
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
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static final String CURRENT_BACKUP_ID = "current";
    private static final String SNAPSHOTS_DIR = "snapshots";
    private static final DateTimeFormatter SNAPSHOT_ID_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

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
                QuestionsBackup qb = buildQuestionsBackup(now);
                TagsBackup tb = buildTagsBackup(now);
                writeBackupPair(backupDir, qb, tb);

                log.info("备份已导出: {} 道题目, {} 个标签 → {}", qb.getQuestions().size(), tb.getTags().size(), backupDir);
            } catch (Exception e) {
                log.error("备份导出失败", e);
            }
        }
    }

    /**
     * 创建手动快照，同时刷新当前自动备份文件。
     */
    public BackupInfoVO createManualBackup() {
        synchronized (lock) {
            try {
                String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String backupId = LocalDateTime.now().format(SNAPSHOT_ID_FORMATTER);
                QuestionsBackup qb = buildQuestionsBackup(now);
                TagsBackup tb = buildTagsBackup(now);

                writeBackupPair(backupDir, qb, tb);
                Path snapshotDir = backupDir.resolve(SNAPSHOTS_DIR).resolve(backupId);
                writeBackupPair(snapshotDir, qb, tb);

                log.info("手动备份已创建: {} → {}", backupId, snapshotDir);
                return buildBackupInfo(backupId, "manual", snapshotDir)
                        .orElseThrow(() -> new IllegalStateException("备份文件创建失败"));
            } catch (Exception e) {
                log.error("手动备份创建失败", e);
                throw new IllegalStateException("手动备份创建失败: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 列出当前备份和所有手动快照。
     */
    public List<BackupInfoVO> listBackups() {
        List<BackupInfoVO> backups = new ArrayList<>();
        buildBackupInfo(CURRENT_BACKUP_ID, "current", backupDir).ifPresent(backups::add);

        Path snapshotsDir = backupDir.resolve(SNAPSHOTS_DIR);
        if (Files.isDirectory(snapshotsDir)) {
            try (Stream<Path> stream = Files.list(snapshotsDir)) {
                stream.filter(Files::isDirectory)
                        .forEach(path -> buildBackupInfo(path.getFileName().toString(), "manual", path)
                                .ifPresent(backups::add));
            } catch (IOException e) {
                log.warn("读取手动备份列表失败: {}", e.getMessage());
            }
        }

        backups.sort(Comparator.comparing(BackupInfoVO::getExportedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return backups;
    }

    /**
     * 加载题目备份，失败时返回 empty
     */
    public Optional<QuestionsBackup> loadQuestionsBackup() {
        return loadQuestionsBackup(CURRENT_BACKUP_ID);
    }

    public Optional<QuestionsBackup> loadQuestionsBackup(String backupId) {
        Path file = resolveBackupDir(backupId).resolve("questions.json");
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
        return loadTagsBackup(CURRENT_BACKUP_ID);
    }

    public Optional<TagsBackup> loadTagsBackup(String backupId) {
        Path file = resolveBackupDir(backupId).resolve("tags.json");
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

    /**
     * 用指定备份替换当前题库和标签。该方法是危险操作，只给管理接口调用。
     */
    @Transactional
    public BackupInfoVO restoreBackup(String backupId) {
        QuestionsBackup qb = loadQuestionsBackup(backupId)
                .orElseThrow(() -> new BusinessException("题目备份不存在或已损坏"));
        TagsBackup tb = loadTagsBackup(backupId)
                .orElseThrow(() -> new BusinessException("标签备份不存在或已损坏"));

        questionRepository.deleteAllInBatch();
        tagConfigRepository.deleteAllInBatch();
        entityManager.flush();

        for (String tagName : tb.getTags()) {
            if (tagName == null || tagName.isBlank()) continue;
            entityManager.persist(TagConfig.builder().name(tagName.trim()).build());
        }

        for (Question src : qb.getQuestions()) {
            if (src.getQuestion() == null || src.getQuestion().isBlank()) continue;
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
        }

        log.info("已从备份 {} 替换恢复: {} 道题目, {} 个标签", backupId, qb.getQuestions().size(), tb.getTags().size());
        return buildBackupInfo(backupId, CURRENT_BACKUP_ID.equals(backupId) ? "current" : "manual", resolveBackupDir(backupId))
                .orElseThrow(() -> new IllegalArgumentException("备份文件不存在或已损坏"));
    }

    private QuestionsBackup buildQuestionsBackup(String exportedAt) {
        return QuestionsBackup.builder()
                .version(1)
                .exportedAt(exportedAt)
                .questions(questionRepository.findAllByOrderByIdAsc())
                .build();
    }

    private TagsBackup buildTagsBackup(String exportedAt) {
        List<String> tags = tagConfigRepository.findAll()
                .stream()
                .map(TagConfig::getName)
                .collect(Collectors.toList());
        return TagsBackup.builder()
                .version(1)
                .exportedAt(exportedAt)
                .tags(tags)
                .build();
    }

    private void writeBackupPair(Path targetDir, QuestionsBackup qb, TagsBackup tb) throws IOException {
        Files.createDirectories(targetDir);

        Path questionsFile = targetDir.resolve("questions.json");
        Path tempQuestions = targetDir.resolve("questions.json.tmp");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempQuestions.toFile(), qb);
        Files.move(tempQuestions, questionsFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

        Path tagsFile = targetDir.resolve("tags.json");
        Path tempTags = targetDir.resolve("tags.json.tmp");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempTags.toFile(), tb);
        Files.move(tempTags, tagsFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    private Optional<BackupInfoVO> buildBackupInfo(String id, String type, Path dir) {
        Path questionsFile = dir.resolve("questions.json");
        Path tagsFile = dir.resolve("tags.json");
        if (!Files.exists(questionsFile) || !Files.exists(tagsFile)) {
            return Optional.empty();
        }

        try {
            QuestionsBackup qb = objectMapper.readValue(questionsFile.toFile(), QuestionsBackup.class);
            TagsBackup tb = objectMapper.readValue(tagsFile.toFile(), TagsBackup.class);
            FileTime modifiedTime = Files.getLastModifiedTime(questionsFile).compareTo(Files.getLastModifiedTime(tagsFile)) >= 0
                    ? Files.getLastModifiedTime(questionsFile)
                    : Files.getLastModifiedTime(tagsFile);
            long questionsSize = Files.size(questionsFile);
            long tagsSize = Files.size(tagsFile);
            String exportedAt = qb.getExportedAt() != null ? qb.getExportedAt() : tb.getExportedAt();

            return Optional.of(BackupInfoVO.builder()
                    .id(id)
                    .type(type)
                    .label(CURRENT_BACKUP_ID.equals(id) ? "当前自动备份" : "手动备份 " + id)
                    .exportedAt(exportedAt)
                    .modifiedAt(modifiedTime.toInstant().toString())
                    .path(relativeBackupPath(dir))
                    .questionsFile("questions.json")
                    .tagsFile("tags.json")
                    .questionsSize(questionsSize)
                    .tagsSize(tagsSize)
                    .totalSize(questionsSize + tagsSize)
                    .questionCount(qb.getQuestions() != null ? qb.getQuestions().size() : 0)
                    .tagCount(tb.getTags() != null ? tb.getTags().size() : 0)
                    .build());
        } catch (Exception e) {
            log.warn("备份文件信息读取失败: {} ({})", dir, e.getMessage());
            return Optional.empty();
        }
    }

    private Path resolveBackupDir(String backupId) {
        if (backupId == null || backupId.isBlank() || CURRENT_BACKUP_ID.equals(backupId)) {
            return backupDir;
        }
        if (!backupId.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("非法备份 ID");
        }
        Path snapshotsDir = backupDir.resolve(SNAPSHOTS_DIR).normalize();
        Path resolved = snapshotsDir.resolve(backupId).normalize();
        if (!resolved.startsWith(snapshotsDir)) {
            throw new IllegalArgumentException("非法备份路径");
        }
        return resolved;
    }

    private String relativeBackupPath(Path dir) {
        Path normalized = dir.toAbsolutePath().normalize();
        if (normalized.equals(backupDir)) {
            return ".";
        }
        return backupDir.relativize(normalized).toString();
    }
}
