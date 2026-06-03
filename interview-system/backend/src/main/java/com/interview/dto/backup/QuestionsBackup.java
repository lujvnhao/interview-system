package com.interview.dto.backup;

import com.interview.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 题目备份数据容器
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionsBackup {
    /** 备份格式版本，用于未来迁移 */
    @Builder.Default
    private int version = 1;

    /** 导出时间 ISO-8601 */
    private String exportedAt;

    /** 全部题目 */
    private List<Question> questions;
}
