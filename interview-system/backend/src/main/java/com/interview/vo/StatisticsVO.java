package com.interview.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 统计信息 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsVO {
    private long totalQuestions;
    private long masteredQuestions;
    private long unmasteredQuestions;
    private long favoriteQuestions;
    private long todayReviewCount;
    private Map<String, Long> categoryDistribution;
    private List<WrongRankItem> wrongRank;
    private List<RecentReviewItem> recentReviews;
}
