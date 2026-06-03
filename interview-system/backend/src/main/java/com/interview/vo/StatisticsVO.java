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
    private double masteryRate;
    private Map<String, Long> categoryDistribution;
    private List<CategoryMasteryItem> categoryMastery;
    private List<TrendItem> reviewTrend;
    private List<TrendItem> wrongGrowthTrend;
    private List<WeakCategoryItem> weakCategoryRank;
    private List<WrongRankItem> wrongRank;
    private List<RecentReviewItem> recentReviews;
}
