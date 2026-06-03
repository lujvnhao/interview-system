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
          <span class="nav-label">统计分析</span>
          <span v-if="route.path === '/statistics'" class="nav-indicator"></span>
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
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isCollapse = ref(false)
</script>

<style>
/* ── 全局重置 ── */
* { margin: 0; padding: 0; box-sizing: border-box; }

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  background: #f1f5f9;
  -webkit-font-smoothing: antialiased;
}

/* ── Element Plus 全局覆盖 ── */
.el-card {
  border-radius: 16px !important;
  border: none !important;
  box-shadow: 0 1px 3px rgba(0,0,0,.04), 0 1px 2px rgba(0,0,0,.06) !important;
  transition: box-shadow 0.25s;
}
.el-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,.06), 0 2px 6px rgba(0,0,0,.04) !important;
}
.el-card__header {
  border-bottom: 1px solid #f1f5f9 !important;
  font-weight: 600;
  font-size: 15px;
  color: #1e293b;
  padding: 18px 24px !important;
}
.el-card__body { padding: 24px !important; }

.el-button--primary {
  background: #6366f1 !important;
  border-color: #6366f1 !important;
  box-shadow: 0 2px 8px rgba(99,102,241,.25) !important;
}
.el-button--primary:hover {
  background: #4f46e5 !important;
  border-color: #4f46e5 !important;
  box-shadow: 0 4px 16px rgba(99,102,241,.35) !important;
}
.el-button--success {
  background: #10b981 !important;
  border-color: #10b981 !important;
  box-shadow: 0 2px 8px rgba(16,185,129,.25) !important;
}
.el-button--danger {
  box-shadow: 0 2px 8px rgba(239,68,68,.2) !important;
}

.el-tag { border-radius: 6px !important; font-weight: 500 !important; }

.el-pagination { --el-pagination-hover-color: #6366f1; }

.el-table th.el-table__cell {
  background: #f8fafc !important;
  color: #64748b !important;
  font-weight: 600 !important;
  font-size: 13px !important;
}
.el-table .el-table__row:hover > td.el-table__cell { background: #f8fafc !important; }

/* ── 壳层布局 ── */
.app-shell {
  display: flex;
  min-height: 100vh;
}

/* ── 侧边栏 ── */
.app-sidebar {
  width: 232px;
  min-height: 100vh;
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  display: flex;
  flex-direction: column;
  transition: width 0.28s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  position: sticky;
  top: 0;
}
.app-sidebar.collapsed { width: 70px; }

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 20px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,.06);
}
.brand-icon {
  width: 36px; height: 36px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.brand-icon svg { width: 18px; height: 18px; }
.brand-text {
  font-size: 16px; font-weight: 700; color: #f1f5f9;
  letter-spacing: .5px; white-space: nowrap;
}

.sidebar-nav { flex: 1; padding: 12px 10px; display: flex; flex-direction: column; gap: 2px; }

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 14px;
  border-radius: 10px;
  color: #94a3b8;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  position: relative;
  transition: all .18s;
  white-space: nowrap;
  overflow: hidden;
}
.nav-item:hover { color: #e2e8f0; background: rgba(255,255,255,.05); }
.nav-item.active { color: #fff; background: rgba(99,102,241,.2); }
.nav-indicator {
  position: absolute; right: 10px;
  width: 6px; height: 6px; border-radius: 50%; background: #818cf8;
}
.nav-icon { font-size: 18px; flex-shrink: 0; display: flex; align-items: center; }

.sidebar-footer {
  padding: 12px 10px;
  border-top: 1px solid rgba(255,255,255,.06);
}
.collapse-toggle {
  width: 100%; padding: 8px;
  background: rgba(255,255,255,.04);
  border: none; border-radius: 8px;
  color: #94a3b8; cursor: pointer; font-size: 16px;
  display: flex; align-items: center; justify-content: center;
  transition: all .18s;
}
.collapse-toggle:hover { background: rgba(255,255,255,.08); color: #e2e8f0; }

/* ── 主区域 ── */
.app-main {
  flex: 1;
  background: #f1f5f9;
  padding: 28px 32px;
  min-height: 100vh;
  overflow-x: hidden;
}
</style>
