package com.interview.repository;

import com.interview.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 题目仓库
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {

    /** 根据问题内容查找（用于去重） */
    Optional<Question> findByQuestion(String question);

    /** 查询收藏题目 */
    Page<Question> findByFavoriteTrue(Pageable pageable);

    /** 查询错题集（没掌握且至少点过一次"没掌握"） */
    Page<Question> findByMasteredFalseAndWrongCountGreaterThan(int minWrongCount, Pageable pageable);

    /** 查询所有错题 */
    List<Question> findByMasteredFalseAndWrongCountGreaterThan(int minWrongCount);

    /** 按分类查询 */
    Page<Question> findByCategory(String category, Pageable pageable);

    /** 按标签模糊匹配 */
    @Query("SELECT q FROM Question q WHERE q.tags LIKE %:tag%")
    Page<Question> findByTagLike(@Param("tag") String tag, Pageable pageable);

    /** 今日复习数量 */
    @Query("SELECT COUNT(q) FROM Question q WHERE q.lastReviewTime >= :startOfDay")
    long countTodayReviews(@Param("startOfDay") LocalDateTime startOfDay);

    /** 各分类题目数量 */
    @Query("SELECT q.category, COUNT(q) FROM Question q GROUP BY q.category")
    List<Object[]> countByCategory();

    /** 错题排行榜（按wrongCount降序） */
    List<Question> findTop10ByWrongCountGreaterThanOrderByWrongCountDesc(int minWrongCount);

    /** 最近复习记录 */
    List<Question> findTop10ByLastReviewTimeIsNotNullOrderByLastReviewTimeDesc();

    /** 查询所有（用于导出） */
    List<Question> findAllByOrderByIdAsc();
}
