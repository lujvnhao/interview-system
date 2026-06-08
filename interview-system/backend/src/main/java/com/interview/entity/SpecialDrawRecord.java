package com.interview.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 专项抽题记录。
 * 目前用于记录算法题当天已抽取题目，保证同一天内不会重复出现。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "special_draw_record", indexes = {
        @Index(name = "idx_special_draw_category_date", columnList = "category,drawDate"),
        @Index(name = "idx_special_draw_question_date", columnList = "questionId,drawDate")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_special_draw_daily_question", columnNames = {"category", "questionId", "drawDate"})
})
public class SpecialDrawRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private LocalDate drawDate;

    private Integer batchNo;

    private Integer drawOrder;

    private Boolean reviewed;

    private Boolean reviewResult;

    private LocalDateTime reviewTime;

    @Column(updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (batchNo == null) batchNo = 1;
        if (drawOrder == null) drawOrder = 1;
        if (reviewed == null) reviewed = false;
        createTime = LocalDateTime.now();
    }
}
