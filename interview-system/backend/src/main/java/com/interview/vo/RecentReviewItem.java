package com.interview.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentReviewItem {
    private Long id;
    private String question;
    private String category;
    private LocalDateTime lastReviewTime;
}
