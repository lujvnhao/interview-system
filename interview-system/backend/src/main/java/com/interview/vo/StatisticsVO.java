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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryMasteryItem {
        private String category;
        private long total;
        private long mastered;
        private long unmastered;
        private int wrongCount;
        private double masteryRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendItem {
        private String date;
        private String label;
        private long value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeakCategoryItem {
        private String category;
        private long total;
        private long mastered;
        private long unmastered;
        private long reviewCount;
        private int wrongCount;
        private double masteryRate;
        private double riskScore;
    }
}
