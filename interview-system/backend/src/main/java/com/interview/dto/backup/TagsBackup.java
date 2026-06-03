package com.interview.dto.backup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 标签备份数据容器
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagsBackup {
    /** 备份格式版本 */
    @Builder.Default
    private int version = 1;

    /** 导出时间 ISO-8601 */
    private String exportedAt;

    /** 全部标签名 */
    private List<String> tags;
}
