#!/bin/bash
# ============================================================
# 面试系统 — 数据同步脚本
# 将 data/backup/ 中的 JSON 备份提交并推送到 GitHub
#
# 用法：
#   ./sync-to-github.sh                      # 使用默认 commit message
#   ./sync-to-github.sh "feat: 更新题库数据"   # 自定义 commit message
# ============================================================

set -e

REPO_ROOT="$(cd "$(dirname "$0")" && pwd)"
BACKUP_PATH="interview-system/backend/data/backup"
COMMIT_MSG="${1:-chore(data): 同步题库备份数据 $(date '+%Y-%m-%d %H:%M')}"

cd "$REPO_ROOT"

# 检查是否在 git 仓库中
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    echo "❌ 错误：当前目录不是 git 仓库"
    exit 1
fi

# 检查备份目录是否存在
if [ ! -d "$BACKUP_PATH" ]; then
    echo "⚠️  备份目录 $BACKUP_PATH 不存在，请先启动应用生成备份数据"
    exit 1
fi

# 检查是否有变更
if git diff --quiet -- "$BACKUP_PATH/" && git diff --cached --quiet -- "$BACKUP_PATH/"; then
    echo "✅ 备份目录无变更，跳过提交"
    exit 0
fi

# 提交
echo "📦 正在提交备份数据..."
git add "$BACKUP_PATH/"
git commit -m "$COMMIT_MSG"

echo "✅ 已提交: $COMMIT_MSG"

# 推送
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
UPSTREAM=$(git rev-parse --abbrev-ref --symbolic-full-name "@{upstream}" 2>/dev/null || true)

if [ -n "$UPSTREAM" ]; then
    echo "🚀 正在推送到 $UPSTREAM ..."
    git push
    echo "✅ 推送完成！"
else
    echo "⚠️  分支 '$CURRENT_BRANCH' 尚未设置上游远程"
    echo "   请运行: git push -u origin $CURRENT_BRANCH"
    exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  📋 数据同步完成！"
echo "  仓库: https://github.com/lujvnhao/interview-system"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
