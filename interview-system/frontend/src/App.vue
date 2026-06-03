<template>
  <div class="app-shell">
    <aside :class="['app-sidebar', { collapsed: isCollapse }]">
      <div class="sidebar-brand" @click="$router.push('/')">
        <div class="brand-mark">
          <span class="brand-char">面</span>
        </div>
        <span v-show="!isCollapse" class="brand-text">
          面试<span class="brand-accent">抽背</span>
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
          <Fold v-if="!isCollapse" /><Expand v-else />
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
   DESIGN SYSTEM —「编辑室」Editorial Print
   暖白纸底 + 朱红印泥 + 靛蓝墨色 + 衬线标题
   ═══════════════════════════════════════════ */

:root {
  /* ── Palette ── */
  --color-page:          #f7f4ed;
  --color-surface:       #ffffff;
  --color-surface-soft:  #faf9f5;
  --color-surface-warm:  #fdf9f3;
  --color-bg-surface:    #ffffff;
  --color-bg-elevated:   #faf9f5;
  --color-bg-hover:      #f5f2ea;
  --color-accent-dim:    #d4837a;
  --color-border:        #e6e1d8;
  --color-border-strong: #d4cec2;

  --color-ink:           #2c2a35;
  --color-ink-strong:    #1a1822;
  --color-ink-muted:     #7a766a;
  --color-ink-faint:     #b5b0a4;

  --color-accent:        #c43a31;
  --color-accent-strong: #a82d25;
  --color-accent-soft:   #fef0ee;
  --color-accent-glow:   rgba(196, 58, 49, 0.08);

  --color-green:         #2d6a4f;
  --color-green-soft:    #ecf7f0;
  --color-red:           #c43a31;
  --color-red-soft:      #fef0ee;
  --color-blue:          #2c5282;
  --color-blue-soft:     #edf3fa;
  --color-amber:         #9a6700;
  --color-amber-soft:    #fdf6e8;

  /* ── Typography ── */
  --font-heading: "DM Serif Display", "Noto Serif SC", "STSong", "Songti SC", "SimSun", serif;
  --font-body:   "Noto Serif SC", "STSong", "Songti SC", "KaiTi", "SimSun", serif;
  --font-mono:   "JetBrains Mono", "Fira Code", "SF Mono", "Menlo", monospace;
  --font-ui:     -apple-system, BlinkMacSystemFont, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;

  /* ── Spacing ── */
  --radius-sm: 3px;
  --radius-md: 6px;
  --radius-lg: 0px;

  /* ── Shadows ── */
  --shadow-xs:  0 1px 2px rgba(0,0,0,0.03);
  --shadow-sm:  0 1px 3px rgba(0,0,0,0.04), 0 1px 2px rgba(0,0,0,0.03);
  --shadow-md:  0 4px 16px rgba(0,0,0,0.05), 0 1px 3px rgba(0,0,0,0.04);
  --shadow-lg:  0 12px 32px rgba(0,0,0,0.06), 0 2px 6px rgba(0,0,0,0.04);

  /* ── Transitions ── */
  --ease-out:   cubic-bezier(0.22, 0.61, 0.36, 1);
  --ease-spring: cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* ═══════════════════════════════════════════
   RESET & BASE
   ═══════════════════════════════════════════ */

* { box-sizing: border-box; margin: 0; padding: 0; }

html {
  background: var(--color-page);
  -webkit-font-smoothing: antialiased;
}

body {
  min-width: 320px;
  color: var(--color-ink);
  font-family: var(--font-body);
  font-size: 15px;
  line-height: 1.75;
  background:
    linear-gradient(180deg, #fdf9f3 0%, var(--color-page) 120px, var(--color-page) 100%);
}

button, input, textarea, select { font: inherit; color: inherit; }

/* ═══════════════════════════════════════════
   ELEMENT PLUS OVERRIDES
   ═══════════════════════════════════════════ */

.el-card {
  background: var(--color-surface) !important;
  border: 1px solid var(--color-border) !important;
  border-radius: var(--radius-md) !important;
  box-shadow: var(--shadow-sm) !important;
  color: var(--color-ink) !important;
}
.el-card__header {
  padding: 18px 22px !important;
  font-family: var(--font-heading);
  font-size: 16px; font-weight: 700;
  color: var(--color-ink-strong) !important;
  border-bottom: 1px solid var(--color-border) !important;
  background: var(--color-surface-soft) !important;
}
.el-card__body { padding: 22px !important; }

/* Button */
.el-button {
  border-radius: var(--radius-sm) !important;
  font-family: var(--font-ui) !important;
  font-weight: 600 !important;
  letter-spacing: 0.01em;
  transition: all 0.18s var(--ease-out) !important;
}
.el-button--primary {
  --el-button-bg-color: var(--color-accent);
  --el-button-border-color: var(--color-accent);
  --el-button-hover-bg-color: var(--color-accent-strong);
  --el-button-hover-border-color: var(--color-accent-strong);
  --el-button-active-bg-color: var(--color-accent-strong);
  color: #fff !important;
  font-weight: 700 !important;
  box-shadow: 0 2px 0 rgba(168,45,37,0.2) !important;
}
.el-button--primary:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(196,58,49,0.25) !important; }

.el-button--default {
  --el-button-bg-color: var(--color-surface);
  --el-button-border-color: var(--color-border);
  --el-button-hover-bg-color: var(--color-surface-soft);
  --el-button-hover-border-color: var(--color-border-strong);
}

/* Input / Select / Textarea */
.el-input__wrapper,
.el-select__wrapper,
.el-textarea__inner {
  background: var(--color-surface) !important;
  border-radius: var(--radius-sm) !important;
  box-shadow: 0 0 0 1px var(--color-border) inset !important;
  transition: box-shadow 0.18s var(--ease-out) !important;
}
.el-input__wrapper.is-focus,
.el-select__wrapper.is-focused,
.el-textarea__inner:focus {
  box-shadow: 0 0 0 1px var(--color-ink) inset, 0 0 0 3px rgba(0,0,0,0.04) !important;
}
.el-input__inner::placeholder,
.el-textarea__inner::placeholder { color: var(--color-ink-faint) !important; }

/* Dialog */
.el-dialog {
  background: var(--color-surface) !important;
  border: 1px solid var(--color-border) !important;
  border-radius: var(--radius-md) !important;
  box-shadow: var(--shadow-lg) !important;
}
.el-dialog__header { margin: 0 !important; padding: 22px 26px 18px !important; border-bottom: 1px solid var(--color-border); }
.el-dialog__title { font-family: var(--font-heading); color: var(--color-ink-strong) !important; font-size: 18px !important; }
.el-dialog__body { padding: 24px 26px !important; }
.el-dialog__footer { padding: 16px 26px 20px !important; border-top: 1px solid var(--color-border); }

/* Tag */
.el-tag { border-radius: var(--radius-sm) !important; font-family: var(--font-ui) !important; font-weight: 600 !important; }

/* Table */
.el-table {
  --el-table-bg-color: var(--color-surface);
  --el-table-tr-bg-color: var(--color-surface);
  --el-table-border-color: var(--color-border);
  --el-table-header-bg-color: var(--color-surface-soft);
  --el-table-row-hover-bg-color: #fdf9f3;
  --el-table-text-color: var(--color-ink);
  --el-table-header-text-color: var(--color-ink-muted);
  color: var(--color-ink) !important;
  background: var(--color-surface) !important;
}
.el-table th.el-table__cell {
  background: var(--color-surface-soft) !important;
  color: var(--color-ink-muted) !important;
  font-family: var(--font-ui) !important;
  font-size: 11px !important; font-weight: 700 !important;
  letter-spacing: 0.05em; text-transform: uppercase;
}
.el-table .el-table__row:hover > td.el-table__cell { background: #fdf9f3 !important; }

/* Message Box */
.el-message-box { background: var(--color-surface) !important; border: 1px solid var(--color-border) !important; border-radius: var(--radius-md) !important; }
.el-message-box__title { font-family: var(--font-heading); color: var(--color-ink-strong) !important; }

/* Popover / Select dropdown */
.el-popper {
  background: var(--color-surface) !important;
  border: 1px solid var(--color-border) !important;
  border-radius: var(--radius-sm) !important;
  box-shadow: var(--shadow-md) !important;
}
.el-select-dropdown__item.hover { background: var(--color-surface-soft) !important; }
.el-select-dropdown__item.selected { color: var(--color-accent) !important; font-weight: 600; }

/* Pagination */
.el-pager li.is-active {
  background: var(--color-ink) !important;
  color: #fff !important;
}

/* ═══════════════════════════════════════════
   SHELL LAYOUT
   ═══════════════════════════════════════════ */

.app-shell { display: flex; min-height: 100vh; }

/* ── Sidebar ── */
.app-sidebar {
  position: sticky; top: 0;
  display: flex; flex-shrink: 0; flex-direction: column;
  width: 232px; min-height: 100vh;
  background: var(--color-ink-strong);
  border-right: none;
  transition: width 0.28s var(--ease-out);
  z-index: 10;
}
.app-sidebar.collapsed { width: 64px; }

.sidebar-brand {
  display: flex; align-items: center; gap: 11px;
  min-height: 70px; padding: 18px 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}

.brand-mark {
  display: flex; flex-shrink: 0; align-items: center; justify-content: center;
  width: 34px; height: 34px;
  background: var(--color-accent);
  border-radius: var(--radius-sm);
}
.brand-char {
  font-family: var(--font-heading);
  font-size: 18px; font-weight: 700; color: #fff;
  line-height: 1;
}

.brand-text {
  font-family: var(--font-heading);
  font-size: 18px; font-weight: 800;
  color: #f0ede5; letter-spacing: 0.03em;
  white-space: nowrap; overflow: hidden;
}
.brand-accent { color: #e8926a; }

/* Nav */
.sidebar-nav { display: flex; flex: 1; flex-direction: column; gap: 1px; padding: 10px 8px; }

.nav-item {
  position: relative;
  display: flex; align-items: center; gap: 10px;
  height: 40px; padding: 0 12px;
  color: rgba(255,255,255,0.55);
  font-family: var(--font-ui); font-size: 13px; font-weight: 600;
  text-decoration: none; white-space: nowrap;
  border-radius: var(--radius-sm);
  transition: all 0.18s var(--ease-out);
}
.nav-item:hover { color: #fff; background: rgba(255,255,255,0.06); }

.nav-item.active {
  color: #fff;
  background: rgba(196,58,49,0.3);
}
.nav-item.active::before {
  content: ""; position: absolute; left: 0; top: 6px; bottom: 6px;
  width: 2px; background: #e8926a; border-radius: 0 2px 2px 0;
}
.nav-icon { display: flex; flex-shrink: 0; align-items: center; font-size: 17px; width: 18px; justify-content: center; }

.app-sidebar.collapsed .brand-text,
.app-sidebar.collapsed .nav-label { opacity: 0; pointer-events: none; }

/* Sidebar footer */
.sidebar-footer {
  display: flex; justify-content: center;
  padding: 10px 8px 14px;
  border-top: 1px solid rgba(255,255,255,0.08);
}
.collapse-toggle {
  display: flex; align-items: center; justify-content: center;
  width: 36px; height: 36px; padding: 0;
  color: rgba(255,255,255,0.4); cursor: pointer;
  background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08);
  border-radius: var(--radius-sm);
  transition: all 0.18s var(--ease-out);
}
.collapse-toggle:hover { color: #fff; background: rgba(255,255,255,0.08); }
.collapse-toggle svg { width: 15px; height: 15px; }

/* ── Main content ── */
.app-main { flex: 1; min-width: 0; min-height: 100vh; padding: 30px 34px 44px; overflow-x: hidden; }

/* ═══════════════════════════════════════════
   SHARED PAGE ELEMENTS
   ═══════════════════════════════════════════ */

.page-header {
  display: flex; align-items: flex-end; justify-content: space-between; gap: 16px;
  padding-bottom: 18px; margin-bottom: 24px;
  border-bottom: 2px solid var(--color-ink-strong);
}
.page-title { display: flex; align-items: center; gap: 14px; }

.title-icon {
  display: flex; align-items: center; justify-content: center;
  width: 40px; height: 40px; font-size: 18px;
  background: var(--color-surface); border: 1px solid var(--color-border);
  border-radius: var(--radius-sm); flex-shrink: 0;
  color: var(--color-ink-muted);
}

.page-title h2 {
  font-family: var(--font-heading);
  font-size: 28px; font-weight: 800; color: var(--color-ink-strong);
  letter-spacing: -0.01em; line-height: 1.15;
}

.header-desc {
  display: inline-block; margin-top: 3px;
  font-family: var(--font-ui); font-size: 13px; color: var(--color-ink-muted); font-weight: 500;
}

/* ── Responsive ── */
@media (max-width: 860px) {
  .app-shell { display: block; }
  .app-sidebar, .app-sidebar.collapsed {
    position: sticky; width: 100%; min-height: auto;
  }
  .sidebar-brand { min-height: 52px; padding: 10px 12px; }
  .sidebar-nav { flex-direction: row; gap: 2px; padding: 6px 8px; overflow-x: auto; }
  .nav-item { flex: 0 0 auto; height: 34px; font-size: 12px; }
  .sidebar-footer { display: none; }
  .app-sidebar.collapsed .brand-text,
  .app-sidebar.collapsed .nav-label { opacity: 1; pointer-events: auto; }
  .app-main { padding: 18px 14px 28px; }
}
</style>
