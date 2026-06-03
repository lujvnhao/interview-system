package com.interview.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类掌握率统计项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMasteryItem {
    private String category;
    private long total;
    private long mastered;
    private long unmastered;
    private int wrongCount;
    private double masteryRate;
}
