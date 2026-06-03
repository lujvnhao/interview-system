package com.interview.controller;

import com.interview.common.Result;
import com.interview.dto.QuestionDTO;
import com.interview.dto.ReviewResultDTO;
import com.interview.entity.Question;
import com.interview.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 题目管理控制器
 */
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 允许前端跨域
public class QuestionController {

    private final QuestionService questionService;

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
            @RequestParam(required = false) Boolean favorite) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Question> result = questionService.list(pageable, keyword, category, tag, mastered, favorite);

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
}
