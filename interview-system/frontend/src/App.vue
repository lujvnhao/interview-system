<template>
  <div class="app-shell">
    <aside :class="['app-sidebar', { collapsed: isCollapse }]">
      <div class="sidebar-brand" @click="$router.push('/')">
        <div class="brand-mark">
          <svg viewBox="0 0 32 32" fill="none">
            <rect x="2" y="2" width="28" height="28" rx="6" stroke="currentColor" stroke-width="2.5"/>
            <path d="M9 12h14M9 17h10M9 22h6" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
          </svg>
        </div>
        <span v-show="!isCollapse" class="brand-text">
          <span class="brand-text-main">面试</span>
          <span class="brand-text-sub">抽背</span>
        </span>
      </div>

      <nav class="sidebar-nav">
        <router-link to="/" class="nav-item" :class="{ active: route.path === '/' }">
          <span class="nav-icon"><HomeFilled /></span>
          <span class="nav-label">抽背首页</span>
        </router-link>
        <router-link to="/questions" class="nav-item" :class="{ active: route.path === '/questions' }">
          <span class="nav-icon"><Collection /></span>
          <span class="nav-label">题库管理</span>
        </router-link>
        <router-link to="/favorites" class="nav-item" :class="{ active: route.path === '/favorites' }">
          <span class="nav-icon"><Star /></span>
          <span class="nav-label">收藏题目</span>
        </router-link>
        <router-link to="/unmastered" class="nav-item" :class="{ active: route.path === '/unmastered' }">
          <span class="nav-icon"><WarningFilled /></span>
          <span class="nav-label">错题集</span>
        </router-link>
        <router-link to="/statistics" class="nav-item" :class="{ active: route.path === '/statistics' }">
          <span class="nav-icon"><DataAnalysis /></span>
          <span class="nav-label">学习诊断</span>
        </router-link>
        <router-link to="/backups" class="nav-item" :class="{ active: route.path === '/backups' }">
          <span class="nav-icon"><FolderOpened /></span>
          <span class="nav-label">备份管理</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <button class="collapse-toggle" @click="isCollapse = !isCollapse">
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </button>
      </div>
    </aside>

    <main class="app-main">
      <router-view v-slot="{ Component }">
        <keep-alive include="Home" :max="3">
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
/* ═══════════════════════════════════════════
   DESIGN SYSTEM — "夜读书房" Warm Dark Library
   ═══════════════════════════════════════════ */

:root {
  /* ── Palette ── */
  --color-bg:            #111110;
  --color-bg-elevated:   #1a1a17;
  --color-bg-surface:    #1e1e1b;
  --color-bg-hover:      #262622;
  --color-border:        #2d2d28;
  --color-border-light:  #353530;

  --color-ink:           #e6e2d8;
  --color-ink-strong:    #f5f1e8;
  --color-ink-muted:     #8c887e;
  --color-ink-faint:     #5c5852;

  --color-accent:        #c9a96e;
  --color-accent-strong: #deb870;
  --color-accent-dim:    #8c744a;
  --color-accent-glow:   rgba(201, 169, 110, 0.12);

  --color-green:         #7a9a7e;
  --color-green-soft:    rgba(122, 154, 126, 0.12);
  --color-red:           #c07263;
  --color-red-soft:      rgba(192, 114, 99, 0.12);
  --color-blue:          #7a93a8;
  --color-blue-soft:     rgba(122, 147, 168, 0.12);
  --color-amber:         #c49a5c;
  --color-amber-soft:    rgba(196, 154, 92, 0.12);

  /* ── Typography ── */
  --font-body:   "LXGW WenKai", "Noto Serif SC", "STSong", "Songti SC", "SimSun", "KaiTi", serif;
  --font-heading: "LXGW WenKai", "Noto Serif SC", "STSong", "Songti SC", serif;
  --font-mono:   "JetBrains Mono", "Fira Code", "SF Mono", "Menlo", monospace;
  --font-ui:     -apple-system, BlinkMacSystemFont, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;

  /* ── Spacing ── */
  --radius-sm: 4px;
  --radius-md: 6px;
  --radius-lg: 10px;

  /* ── Shadows ── */
  --shadow-sm:  0 1px 0 var(--color-border);
  --shadow-md:  0 1px 0 var(--color-border), 0 4px 16px rgba(0,0,0,0.4);
  --shadow-lg:  0 1px 0 var(--color-border), 0 8px 32px rgba(0,0,0,0.5);
  --glow-accent: 0 0 0 1px rgba(201, 169, 110, 0.2), 0 0 20px rgba(201, 169, 110, 0.06);

  /* ── Transitions ── */
  --ease-out: cubic-bezier(0.16, 1, 0.3, 1);
  --ease-spring: cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* ═══════════════════════════════════════════
   RESET & BASE
   ═══════════════════════════════════════════ */

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html {
  background: var(--color-bg);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

body {
  min-width: 320px;
  color: var(--color-ink);
  font-family: var(--font-body);
  font-size: 15px;
  line-height: 1.7;
  background:
    radial-gradient(ellipse 80% 60% at 50% -10%, rgba(201, 169, 110, 0.04) 0%, transparent 60%),
    var(--color-bg);
}

button, input, textarea, select {
  font: inherit;
  color: inherit;
}

/* ═══════════════════════════════════════════
   ELEMENT PLUS OVERRIDES
   ═══════════════════════════════════════════ */

/* Card */
.el-card {
  background: var(--color-bg-surface) !important;
  border: 1px solid var(--color-border) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--shadow-sm) !important;
  color: var(--color-ink) !important;
}
.el-card__header {
  padding: 18px 22px !important;
  font-family: var(--font-heading);
  font-size: 15px;
  font-weight: 700;
  color: var(--color-ink-strong) !important;
  border-bottom: 1px solid var(--color-border) !important;
}
.el-card__body { padding: 22px !important; }

/* Button */
.el-button {
  border-radius: var(--radius-md) !important;
  font-family: var(--font-ui) !important;
  font-weight: 600 !important;
  transition: all 0.2s var(--ease-out) !important;
}
.el-button--primary {
  --el-button-bg-color: var(--color-accent);
  --el-button-border-color: var(--color-accent);
  --el-button-hover-bg-color: var(--color-accent-strong);
  --el-button-hover-border-color: var(--color-accent-strong);
  --el-button-active-bg-color: var(--color-accent-dim);
  --el-button-active-border-color: var(--color-accent-dim);
  --el-button-text-color: #1a1a17;
  color: #1a1a17 !important;
  font-weight: 700 !important;
  box-shadow: 0 0 0 0 rgba(201, 169, 110, 0) !important;
}
.el-button--primary:hover {
  box-shadow: 0 4px 20px rgba(201, 169, 110, 0.25) !important;
  transform: translateY(-1px);
}

.el-button--default {
  --el-button-bg-color: var(--color-bg-elevated);
  --el-button-border-color: var(--color-border);
  --el-button-hover-bg-color: var(--color-bg-hover);
  --el-button-hover-border-color: var(--color-border-light);
  --el-button-text-color: var(--color-ink);
  color: var(--color-ink) !important;
}

/* Input / Select / Textarea */
.el-input__wrapper,
.el-select__wrapper,
.el-textarea__inner {
  background: var(--color-bg-elevated) !important;
  border-radius: var(--radius-md) !important;
  box-shadow: 0 0 0 1px var(--color-border) inset !important;
  color: var(--color-ink) !important;
  transition: box-shadow 0.2s var(--ease-out) !important;
}
.el-input__wrapper.is-focus,
.el-select__wrapper.is-focused,
.el-textarea__inner:focus {
  box-shadow: 0 0 0 1px var(--color-accent) inset, 0 0 0 3px var(--color-accent-glow) !important;
}
.el-input__inner,
.el-select .el-input__inner {
  color: var(--color-ink) !important;
}
.el-input__inner::placeholder,
.el-textarea__inner::placeholder {
  color: var(--color-ink-faint) !important;
}

/* Dialog */
.el-dialog {
  background: var(--color-bg-surface) !important;
  border: 1px solid var(--color-border) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--shadow-lg) !important;
}
.el-dialog__header {
  margin: 0 !important;
  padding: 20px 24px 16px !important;
  border-bottom: 1px solid var(--color-border);
  color: var(--color-ink-strong) !important;
}
.el-dialog__title { color: var(--color-ink-strong) !important; font-family: var(--font-heading); }
.el-dialog__body { padding: 22px 24px !important; }
.el-dialog__footer { padding: 16px 24px 20px !important; border-top: 1px solid var(--color-border); }

/* Tag */
.el-tag {
  border-radius: var(--radius-sm) !important;
  font-family: var(--font-ui) !important;
  font-weight: 600 !important;
}

/* Pagination */
.el-pagination {
  --el-pagination-bg-color: var(--color-bg-surface);
  --el-pagination-text-color: var(--color-ink-muted);
  --el-pagination-hover-color: var(--color-accent);
}
.el-pagination .el-pager li {
  color: var(--color-ink-muted) !important;
  background: transparent !important;
}
.el-pagination .el-pager li.is-active {
  background: var(--color-accent) !important;
  color: #1a1a17 !important;
}

/* Table */
.el-table {
  --el-table-bg-color: var(--color-bg-surface);
  --el-table-tr-bg-color: var(--color-bg-surface);
  --el-table-border-color: var(--color-border);
  --el-table-header-bg-color: var(--color-bg-elevated);
  --el-table-row-hover-bg-color: var(--color-bg-hover);
  --el-table-text-color: var(--color-ink);
  --el-table-header-text-color: var(--color-ink-muted);
  color: var(--color-ink) !important;
  background: var(--color-bg-surface) !important;
}
.el-table th.el-table__cell {
  background: var(--color-bg-elevated) !important;
  color: var(--color-ink-muted) !important;
  font-family: var(--font-ui) !important;
  font-size: 12px !important;
  font-weight: 700 !important;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}
.el-table tr { background: var(--color-bg-surface) !important; }
.el-table .el-table__row:hover > td.el-table__cell {
  background: var(--color-bg-hover) !important;
}

/* Message Box */
.el-message-box {
  background: var(--color-bg-surface) !important;
  border: 1px solid var(--color-border) !important;
  border-radius: var(--radius-lg) !important;
}
.el-message-box__title { color: var(--color-ink-strong) !important; }
.el-message-box__message { color: var(--color-ink) !important; }

/* Empty */
.el-empty__description p { color: var(--color-ink-muted) !important; }

/* Popover / Select dropdown */
.el-popper {
  background: var(--color-bg-surface) !important;
  border: 1px solid var(--color-border) !important;
  border-radius: var(--radius-md) !important;
  box-shadow: var(--shadow-md) !important;
}
.el-select-dropdown__item {
  color: var(--color-ink) !important;
}
.el-select-dropdown__item.hover,
.el-select-dropdown__item:hover {
  background: var(--color-bg-hover) !important;
}
.el-select-dropdown__item.selected {
  color: var(--color-accent) !important;
  font-weight: 600;
}

/* Loading mask */
.el-loading-mask {
  background: rgba(17, 17, 16, 0.7) !important;
}

/* ═══════════════════════════════════════════
   SHELL LAYOUT
   ═══════════════════════════════════════════ */

.app-shell {
  display: flex;
  min-height: 100vh;
}

/* ── Sidebar ── */

.app-sidebar {
  position: sticky;
  top: 0;
  display: flex;
  flex-shrink: 0;
  flex-direction: column;
  width: 240px;
  min-height: 100vh;
  background:
    radial-gradient(ellipse 100% 100% at 50% 0%, rgba(201, 169, 110, 0.04) 0%, transparent 50%),
    linear-gradient(180deg, #171714 0%, #131310 100%);
  border-right: 1px solid var(--color-border);
  transition: width 0.3s var(--ease-out);
  z-index: 10;
}

.app-sidebar.collapsed { width: 68px; }

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 72px;
  padding: 18px;
  cursor: pointer;
  border-bottom: 1px solid var(--color-border);
  transition: padding 0.3s var(--ease-out);
}

.brand-mark {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  color: var(--color-accent);
  background: rgba(201, 169, 110, 0.1);
  border: 1.5px solid rgba(201, 169, 110, 0.25);
  border-radius: var(--radius-md);
}
.brand-mark svg { width: 18px; height: 18px; }

.brand-text {
  display: flex;
  align-items: baseline;
  gap: 3px;
  overflow: hidden;
  white-space: nowrap;
}
.brand-text-main {
  font-family: var(--font-heading);
  font-size: 18px;
  font-weight: 800;
  color: var(--color-ink-strong);
  letter-spacing: 0.02em;
}
.brand-text-sub {
  font-family: var(--font-heading);
  font-size: 13px;
  font-weight: 600;
  color: var(--color-accent);
  letter-spacing: 0.08em;
}

/* ── Nav items ── */

.sidebar-nav {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 2px;
  padding: 12px 8px;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  height: 42px;
  padding: 0 12px;
  color: var(--color-ink-muted);
  font-family: var(--font-ui);
  font-size: 13.5px;
  font-weight: 600;
  text-decoration: none;
  white-space: nowrap;
  border-radius: var(--radius-md);
  transition: all 0.2s var(--ease-out);
  overflow: hidden;
}

.nav-item:hover {
  color: var(--color-ink);
  background: var(--color-bg-hover);
}

.nav-item.active {
  color: var(--color-accent);
  background: rgba(201, 169, 110, 0.08);
}

.nav-item.active::before {
  content: "";
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  background: var(--color-accent);
  border-radius: 0 3px 3px 0;
}

.nav-icon {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  font-size: 18px;
  width: 20px;
  justify-content: center;
}

.nav-item.active .nav-icon { color: var(--color-accent); }

.app-sidebar.collapsed .brand-text,
.app-sidebar.collapsed .nav-label { opacity: 0; pointer-events: none; }

.app-sidebar.collapsed .brand-mark { width: 32px; height: 32px; }
.app-sidebar.collapsed .brand-mark svg { width: 16px; height: 16px; }

/* ── Sidebar footer ── */

.sidebar-footer {
  display: flex;
  justify-content: center;
  padding: 12px 8px 16px;
  border-top: 1px solid var(--color-border);
}

.collapse-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  padding: 0;
  color: var(--color-ink-muted);
  cursor: pointer;
  background: rgba(255,255,255,0.03);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  transition: all 0.2s var(--ease-out);
}
.collapse-toggle:hover {
  color: var(--color-accent);
  background: var(--color-bg-hover);
  border-color: var(--color-border-light);
}
.collapse-toggle svg { width: 16px; height: 16px; }

/* ── Main content ── */

.app-main {
  flex: 1;
  min-width: 0;
  min-height: 100vh;
  padding: 32px 36px 48px;
  overflow-x: hidden;
}

/* ═══════════════════════════════════════════
   SHARED PAGE ELEMENTS
   ═══════════════════════════════════════════ */

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 20px;
  margin-bottom: 24px;
  border-bottom: 1px solid var(--color-border);
}

.page-title {
  display: flex;
  align-items: center;
  gap: 14px;
}

.title-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  font-size: 20px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: var(--color-bg-elevated);
  flex-shrink: 0;
}

.page-title h2 {
  font-family: var(--font-heading);
  font-size: 26px;
  font-weight: 800;
  color: var(--color-ink-strong);
  letter-spacing: 0.01em;
  line-height: 1.2;
}

.header-desc {
  display: inline-block;
  margin-top: 4px;
  font-family: var(--font-ui);
  font-size: 13px;
  color: var(--color-ink-muted);
  font-weight: 500;
}

/* ── Responsive ── */

@media (max-width: 860px) {
  .app-shell { display: block; }
  .app-sidebar, .app-sidebar.collapsed {
    position: sticky;
    z-index: 10;
    width: 100%;
    min-height: auto;
  }
  .sidebar-brand { min-height: 56px; padding: 12px 14px; }
  .sidebar-nav { flex-direction: row; gap: 4px; padding: 8px 10px; overflow-x: auto; }
  .nav-item { flex: 0 0 auto; height: 36px; font-size: 12px; }
  .sidebar-footer { display: none; }
  .app-sidebar.collapsed .brand-text,
  .app-sidebar.collapsed .nav-label { opacity: 1; pointer-events: auto; }
  .app-main { padding: 20px 16px 32px; }
}
</style>
