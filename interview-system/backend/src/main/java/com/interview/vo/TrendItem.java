package com.interview.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 趋势数据项（复习趋势、错题增长趋势）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendItem {
    private String date;
    private String label;
    private long value;
}
