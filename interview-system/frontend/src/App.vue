<template>
  <div class="app-shell">
    <!-- 侧边栏 -->
    <aside :class="['app-sidebar', { collapsed: isCollapse }]">
      <div class="sidebar-brand" @click="$router.push('/')">
        <div class="brand-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
            <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
          </svg>
        </div>
        <span v-show="!isCollapse" class="brand-text">面试抽背</span>
      </div>

      <nav class="sidebar-nav">
        <router-link to="/" class="nav-item" :class="{ active: route.path === '/' }">
          <span class="nav-icon"><HomeFilled /></span>
          <span class="nav-label">抽背首页</span>
          <span v-if="route.path === '/'" class="nav-indicator"></span>
        </router-link>
        <router-link to="/questions" class="nav-item" :class="{ active: route.path === '/questions' }">
          <span class="nav-icon"><Collection /></span>
          <span class="nav-label">题库管理</span>
          <span v-if="route.path === '/questions'" class="nav-indicator"></span>
        </router-link>
        <router-link to="/favorites" class="nav-item" :class="{ active: route.path === '/favorites' }">
          <span class="nav-icon"><Star /></span>
          <span class="nav-label">收藏题目</span>
          <span v-if="route.path === '/favorites'" class="nav-indicator"></span>
        </router-link>
        <router-link to="/unmastered" class="nav-item" :class="{ active: route.path === '/unmastered' }">
          <span class="nav-icon"><WarningFilled /></span>
          <span class="nav-label">错题集</span>
          <span v-if="route.path === '/unmastered'" class="nav-indicator"></span>
        </router-link>
        <router-link to="/statistics" class="nav-item" :class="{ active: route.path === '/statistics' }">
          <span class="nav-icon"><DataAnalysis /></span>
          <span class="nav-label">学习诊断</span>
          <span v-if="route.path === '/statistics'" class="nav-indicator"></span>
        </router-link>
        <router-link to="/backups" class="nav-item" :class="{ active: route.path === '/backups' }">
          <span class="nav-icon"><FolderOpened /></span>
          <span class="nav-label">备份管理</span>
          <span v-if="route.path === '/backups'" class="nav-indicator"></span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <button class="collapse-toggle" @click="isCollapse = !isCollapse">
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <keep-alive>
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isCollapse = ref(false)
</script>

<style>
:root {
  --color-page: #f4f6f8;
  --color-surface: #ffffff;
  --color-surface-soft: #f8faf9;
  --color-border: #dfe5e8;
  --color-ink: #1f2933;
  --color-muted: #667085;
  --color-primary: #0f766e;
  --color-primary-strong: #0b5f59;
  --color-primary-soft: #e6f4f1;
  --color-blue-soft: #eaf2ff;
  --color-amber-soft: #fff4df;
  --shadow-subtle: 0 1px 2px rgba(16, 24, 40, 0.06), 0 10px 24px rgba(16, 24, 40, 0.04);
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html {
  background: var(--color-page);
}

body {
  min-width: 320px;
  color: var(--color-ink);
  font-family: Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
    "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  background:
    linear-gradient(180deg, #eef5f3 0, #f6f7f9 260px, var(--color-page) 100%);
  -webkit-font-smoothing: antialiased;
}

button,
input,
textarea,
select {
  font: inherit;
}

/* Element Plus baseline */
.el-card {
  border: 1px solid var(--color-border) !important;
  border-radius: 8px !important;
  box-shadow: var(--shadow-subtle) !important;
}

.el-card__header {
  padding: 16px 18px !important;
  color: var(--color-ink);
  font-size: 15px;
  font-weight: 700;
  border-bottom: 1px solid var(--color-border) !important;
}

.el-card__body {
  padding: 18px !important;
}

.el-button {
  border-radius: 8px !important;
  font-weight: 600 !important;
}

.el-button--primary {
  --el-button-bg-color: var(--color-primary);
  --el-button-border-color: var(--color-primary);
  --el-button-hover-bg-color: var(--color-primary-strong);
  --el-button-hover-border-color: var(--color-primary-strong);
  --el-button-active-bg-color: var(--color-primary-strong);
  --el-button-active-border-color: var(--color-primary-strong);
  box-shadow: 0 8px 18px rgba(15, 118, 110, 0.18) !important;
}

.el-button--success {
  --el-button-bg-color: #16a34a;
  --el-button-border-color: #16a34a;
  --el-button-hover-bg-color: #15803d;
  --el-button-hover-border-color: #15803d;
}

.el-input__wrapper,
.el-select__wrapper,
.el-textarea__inner,
.el-input-number .el-input__wrapper {
  border-radius: 8px !important;
  box-shadow: 0 0 0 1px var(--color-border) inset !important;
}

.el-input__wrapper.is-focus,
.el-select__wrapper.is-focused,
.el-textarea__inner:focus {
  box-shadow: 0 0 0 1px var(--color-primary) inset, 0 0 0 3px rgba(15, 118, 110, 0.12) !important;
}

.el-dialog {
  border-radius: 8px !important;
  overflow: hidden;
}

.el-dialog__header {
  margin: 0 !important;
  padding: 18px 22px 14px !important;
  border-bottom: 1px solid var(--color-border);
}

.el-dialog__body {
  padding: 20px 22px !important;
}

.el-dialog__footer {
  padding: 14px 22px 18px !important;
  border-top: 1px solid var(--color-border);
}

.el-tag {
  border-radius: 6px !important;
  font-weight: 600 !important;
}

.el-pagination {
  --el-pagination-hover-color: var(--color-primary);
}

.el-table {
  --el-table-border-color: #edf1f3;
  --el-table-header-bg-color: #f6f8f9;
  color: #344054 !important;
}

.el-table th.el-table__cell {
  color: #5f6f7d !important;
  font-size: 13px !important;
  font-weight: 700 !important;
  background: #f6f8f9 !important;
}

.el-table .el-table__row:hover > td.el-table__cell {
  background: #f6fbfa !important;
}

.el-table .cell {
  line-height: 1.55;
}

/* Shell */
.app-shell {
  display: flex;
  min-height: 100vh;
}

.app-sidebar {
  position: sticky;
  top: 0;
  display: flex;
  flex-shrink: 0;
  flex-direction: column;
  width: 240px;
  min-height: 100vh;
  color: #d8dee6;
  background:
    linear-gradient(180deg, rgba(15, 118, 110, 0.18), rgba(15, 118, 110, 0) 210px),
    #171b22;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  transition: width 0.24s ease;
}

.app-sidebar.collapsed {
  width: 72px;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 70px;
  padding: 16px 18px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-icon {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  color: #ecfeff;
  background: linear-gradient(135deg, #0f766e, #2563eb);
  border-radius: 8px;
  box-shadow: 0 10px 22px rgba(15, 118, 110, 0.28);
}

.brand-icon svg {
  width: 19px;
  height: 19px;
}

.brand-text {
  overflow: hidden;
  color: #f8fafc;
  font-size: 16px;
  font-weight: 800;
  letter-spacing: 0;
  white-space: nowrap;
}

.sidebar-nav {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 4px;
  padding: 14px 10px;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 42px;
  padding: 10px 12px;
  overflow: hidden;
  color: #a7b0bd;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  white-space: nowrap;
  border-radius: 8px;
  transition: color 0.18s ease, background 0.18s ease;
}

.nav-item:hover {
  color: #f8fafc;
  background: rgba(255, 255, 255, 0.07);
}

.nav-item.active {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.11);
}

.nav-item.active::before {
  position: absolute;
  top: 9px;
  bottom: 9px;
  left: 0;
  width: 3px;
  content: "";
  background: #5eead4;
  border-radius: 0 4px 4px 0;
}

.nav-indicator {
  position: absolute;
  right: 12px;
  width: 6px;
  height: 6px;
  background: #5eead4;
  border-radius: 999px;
}

.nav-icon {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  font-size: 18px;
}

.app-sidebar.collapsed .brand-text,
.app-sidebar.collapsed .nav-label,
.app-sidebar.collapsed .nav-indicator {
  opacity: 0;
}

.sidebar-footer {
  display: flex;
  justify-content: center;
  padding: 12px 10px 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.collapse-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  min-height: 40px;
  padding: 0;
  color: #a7b0bd;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  transition: color 0.18s ease, background 0.18s ease;
}

.collapse-toggle:hover {
  color: #f8fafc;
  background: rgba(255, 255, 255, 0.11);
}

.collapse-toggle svg {
  width: 18px;
  height: 18px;
}

.app-main {
  flex: 1;
  min-width: 0;
  min-height: 100vh;
  padding: 28px 32px 40px;
  overflow-x: hidden;
}

@media (max-width: 860px) {
  .app-shell {
    display: block;
  }

  .app-sidebar,
  .app-sidebar.collapsed {
    z-index: 10;
    width: 100%;
    min-height: auto;
  }

  .sidebar-brand {
    min-height: 58px;
    padding: 10px 14px;
  }

  .sidebar-nav {
    flex-direction: row;
    gap: 6px;
    padding: 8px 10px 10px;
    overflow-x: auto;
  }

  .nav-item {
    flex: 0 0 auto;
    min-height: 38px;
  }

  .nav-indicator,
  .sidebar-footer {
    display: none;
  }

  .app-sidebar.collapsed .brand-text,
  .app-sidebar.collapsed .nav-label {
    opacity: 1;
  }

  .app-main {
    padding: 18px 14px 28px;
  }
}
</style>
