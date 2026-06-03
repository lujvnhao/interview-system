package com.interview.common;

import com.interview.service.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时备份任务 — 防止意外崩溃导致数据丢失
 * 默认每 30 分钟执行一次
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledBackupTask {

    private final BackupService backupService;

    @Scheduled(fixedRateString = "${app.backup.interval-ms:1800000}")
    public void periodicBackup() {
        log.debug("执行定时备份...");
        backupService.exportBackup();
    }
}
