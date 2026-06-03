package com.interview.service;

import com.interview.dto.QuestionDTO;
import com.interview.dto.ReviewResultDTO;
import com.interview.entity.Question;
import com.interview.vo.StatisticsVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 题目服务接口
 */
public interface QuestionService {

    // ── CRUD ──
    Question create(QuestionDTO dto);
    Question update(Long id, QuestionDTO dto);
    void delete(Long id);
    void batchDelete(List<Long> ids);
    Question getById(Long id);
    Page<Question> list(Pageable pageable, String keyword, String category, String tag,
                        Boolean mastered, Boolean favorite);

    // ── 抽题 ──
    Question randomQuestion(String mode, String category);

    // ── 复习反馈 ──
    Question submitReview(Long id, ReviewResultDTO dto);

    // ── 答案 ──
    Question updateAnswer(Long id, String answer);

    // ── 收藏 ──
    Question toggleFavorite(Long id);

    // ── 收藏列表 ──
    Page<Question> favorites(Pageable pageable);

    // ── 未掌握列表 ──
    Page<Question> unmastered(Pageable pageable);

    // ── 统计 ──
    StatisticsVO statistics();

    // ── 导入导出 ──
    List<Question> exportAll();
    Map<String, Object> importFromJson(List<QuestionDTO> dtos);

    // ── 默认分类和标签 ──
    List<String> allCategories();
    List<String> allTags();
}
