package com.interview.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 备份文件信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupInfoVO {
    private String id;
    private String type;
    private String label;
    private String exportedAt;
    private String modifiedAt;
    private String path;
    private String questionsFile;
    private String tagsFile;
    private long questionsSize;
    private long tagsSize;
    private long totalSize;
    private int questionCount;
    private int tagCount;
}
