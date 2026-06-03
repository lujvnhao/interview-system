package com.interview.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 今日复习概览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewOverviewVO {
    private long dueTodayCount;
    private long highRiskCount;
    private long longUnreviewedCount;
}
