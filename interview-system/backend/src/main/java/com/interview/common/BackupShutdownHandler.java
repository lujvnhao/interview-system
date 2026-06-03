package com.interview.common;

import com.interview.service.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

/**
 * 应用关闭时自动导出数据备份
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BackupShutdownHandler {

    private final BackupService backupService;

    @PreDestroy
    public void onShutdown() {
        log.info("应用正在关闭，执行最终数据备份...");
        try {
            backupService.exportBackup();
            log.info("关闭前备份完成");
        } catch (Exception e) {
            log.error("关闭前备份失败", e);
        }
    }
}
