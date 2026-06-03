package com.interview.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 面试题实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "question_bank", indexes = {
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_mastered", columnList = "mastered"),
        @Index(name = "idx_favorite", columnList = "favorite"),
        @Index(name = "idx_wrong_count", columnList = "wrongCount"),
        @Index(name = "idx_last_review_time", columnList = "lastReviewTime"),
        @Index(name = "idx_mastered_wrong", columnList = "mastered,wrongCount")
})
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 问题内容 */
    @Column(nullable = false, length = 2000)
    private String question;

    /** 标准答案 */
    @Column(columnDefinition = "TEXT")
    private String answer;

    /** 分类 */
    @Column(length = 100)
    private String category;

    /** 标签，逗号分隔 */
    @Column(length = 500)
    private String tags;

    /** 是否掌握 */
    @Builder.Default
    private Boolean mastered = false;

    /** 是否收藏 */
    @Builder.Default
    private Boolean favorite = false;

    /** 抽题权重 */
    @Builder.Default
    private Integer weight = 10;

    /** 没掌握次数 */
    @Builder.Default
    private Integer wrongCount = 0;

    /** 掌握次数 */
    @Builder.Default
    private Integer correctCount = 0;

    /** 总复习次数 */
    @Builder.Default
    private Integer reviewCount = 0;

    /** 上次复习时间 */
    private LocalDateTime lastReviewTime;

    /** 创建时间 */
    @Column(updatable = false)
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
