package com.interview.controller;

import com.interview.common.Result;
import com.interview.dto.QuestionDTO;
import com.interview.dto.ReviewResultDTO;
import com.interview.entity.Question;
import com.interview.service.BackupService;
import com.interview.service.QuestionService;
import com.interview.vo.BackupInfoVO;
import com.interview.vo.ReviewOverviewVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 题目管理控制器
 */
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final BackupService backupService;

    private static final Set<String> LIST_SORT_FIELDS = Set.of(
            "createTime", "wrongCount", "reviewCount", "lastReviewTime", "weight"
    );

    // ── CRUD ──

    /** 新增题目 */
    @PostMapping
    public Result<Question> create(@Valid @RequestBody QuestionDTO dto) {
        return Result.success("新增成功", questionService.create(dto));
    }

    /** 修改题目 */
    @PutMapping("/{id}")
    public Result<Question> update(@PathVariable Long id, @Valid @RequestBody QuestionDTO dto) {
        return Result.success("修改成功", questionService.update(id, dto));
    }

    /** 删除题目 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        questionService.delete(id);
        return Result.success("删除成功");
    }

    /** 批量删除 */
    @DeleteMapping("/batch")
    public Result<?> batchDelete(@RequestBody List<Long> ids) {
        questionService.batchDelete(ids);
        return Result.success("批量删除成功");
    }

    /** 查询题目详情 */
    @GetMapping("/{id}")
    public Result<Question> getById(@PathVariable Long id) {
        return Result.success(questionService.getById(id));
    }

    /** 分页查询题目列表（支持筛选） */
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Boolean mastered,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(required = false) Boolean emptyTag,
            @RequestParam(required = false) Boolean noAnswer,
            @RequestParam(required = false) Boolean hotTag,
            @RequestParam(required = false) Boolean longUnreviewed,
            @RequestParam(defaultValue = "14") Integer staleDays,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = LIST_SORT_FIELDS.contains(sortBy) ? sortBy : "createTime";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort.Order order = Sort.Order.by(safeSortBy)
                .with(direction)
                .nullsLast();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(order));
        Page<Question> result = questionService.list(pageable, keyword, category, tag,
                mastered, favorite, emptyTag, noAnswer, hotTag, longUnreviewed, staleDays);

        return Result.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "currentPage", page,
                "pageSize", size
        ));
    }

    // ── 抽题 ──

    /** 根据权重随机抽题 */
    @GetMapping("/random")
    public Result<Question> random(
            @RequestParam(defaultValue = "all") String mode,
            @RequestParam(required = false) String category) {
        return Result.success(questionService.randomQuestion(mode, category));
    }

    /** 今日应复习题目列表 */
    @GetMapping("/due-today")
    public Result<Map<String, Object>> dueToday(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Question> result = questionService.dueToday(pageable, category);
        return Result.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "currentPage", page,
                "pageSize", size
        ));
    }

    /** 首页今日复习概览 */
    @GetMapping("/review-overview")
    public Result<ReviewOverviewVO> reviewOverview() {
        return Result.success(questionService.reviewOverview());
    }

    // ── 复习反馈 ──

    /** 提交掌握/没掌握结果 */
    @PostMapping("/{id}/review")
    public Result<Question> submitReview(@PathVariable Long id, @RequestBody ReviewResultDTO dto) {
        return Result.success(questionService.submitReview(id, dto));
    }

    // ── 答案 ──

    /** 补充或修改答案 */
    @PutMapping("/{id}/answer")
    public Result<Question> updateAnswer(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String answer = body.get("answer");
        return Result.success("答案更新成功", questionService.updateAnswer(id, answer));
    }

    // ── 收藏 ──

    /** 收藏/取消收藏 */
    @PostMapping("/{id}/favorite")
    public Result<Question> toggleFavorite(@PathVariable Long id) {
        Question q = questionService.toggleFavorite(id);
        String msg = q.getFavorite() ? "已收藏" : "已取消收藏";
        return Result.success(msg, q);
    }

    /** 收藏题目列表 */
    @GetMapping("/favorites")
    public Result<Map<String, Object>> favorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "updateTime"));
        Page<Question> result = questionService.favorites(pageable);
        return Result.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "currentPage", page,
                "pageSize", size
        ));
    }

    // ── 未掌握 ──

    /** 未掌握题目列表 */
    @GetMapping("/unmastered")
    public Result<Map<String, Object>> unmastered(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "wrongCount"));
        Page<Question> result = questionService.unmastered(pageable);
        return Result.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "currentPage", page,
                "pageSize", size
        ));
    }

    // ── 统计 ──

    /** 获取统计信息 */
    @GetMapping("/statistics")
    public Result<?> statistics() {
        return Result.success(questionService.statistics());
    }

    // ── 导入导出 ──

    /** 导出所有题目为 JSON */
    @GetMapping("/export/json")
    public Result<List<Question>> exportJson() {
        return Result.success(questionService.exportAll());
    }

    /** 从 JSON 导入题目 */
    @PostMapping("/import/json")
    public Result<Map<String, Object>> importJson(@RequestBody List<QuestionDTO> dtos) {
        return Result.success("导入完成", questionService.importFromJson(dtos));
    }

    // ── 分类与标签 ──

    /** 获取所有分类 */
    @GetMapping("/categories")
    public Result<List<String>> categories() {
        return Result.success(questionService.allCategories());
    }

    /** 获取所有标签 */
    @GetMapping("/tags")
    public Result<List<String>> tags() {
        return Result.success(questionService.allTags());
    }

    // ── 备份与恢复 ──

    /** 查询备份列表 */
    @GetMapping("/backup/list")
    public Result<List<BackupInfoVO>> listBackups() {
        return Result.success(backupService.listBackups());
    }

    /** 创建手动备份快照 */
    @PostMapping("/backup/create")
    public Result<BackupInfoVO> createBackup() {
        return Result.success("备份已创建", backupService.createManualBackup());
    }

    /** 手动触发数据备份到 data/backup/ */
    @PostMapping("/backup/export")
    public Result<BackupInfoVO> triggerBackup() {
        return Result.success("备份已导出到 data/backup/", backupService.createManualBackup());
    }

    /** 从指定备份恢复题库和标签 */
    @PostMapping("/backup/{backupId}/restore")
    public Result<BackupInfoVO> restoreBackup(@PathVariable String backupId) {
        return Result.success("数据已从备份恢复", backupService.restoreBackup(backupId));
    }

    /** 从当前自动备份恢复数据 */
    @PostMapping("/backup/restore")
    public Result<?> triggerRestore() {
        return Result.success("数据已从备份恢复", backupService.restoreBackup("current"));
    }

    /** 获取备份目录绝对路径 */
    @GetMapping("/backup/dir")
    public Result<String> getBackupDir() {
        return Result.success(backupService.getBackupDirPath());
    }

    /** 在 Finder 中打开备份目录 */
    @PostMapping("/backup/open-dir")
    public Result<?> openBackupDir() {
        backupService.openBackupDirInFinder();
        return Result.success("已在 Finder 中打开备份目录");
    }
}
