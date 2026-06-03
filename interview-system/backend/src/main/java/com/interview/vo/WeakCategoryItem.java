package com.interview.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 薄弱分类排行项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeakCategoryItem {
    private String category;
    private long total;
    private long mastered;
    private long unmastered;
    private long reviewCount;
    private int wrongCount;
    private double masteryRate;
    private double riskScore;
}
