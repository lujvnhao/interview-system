import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// 按需引入图标（仅注册实际使用的图标，减少打包体积 ~500KB）
import {
  ArrowDown, Check, Close, Collection, CollectionTag,
  DataAnalysis, Delete, Document, DocumentDelete, Download,
  EditPen, Expand, Flag, Fold, FolderOpened,
  HomeFilled, Link, List, Plus, Refresh,
  Right, Search, Setting, Sort, Star,
  StarFilled, Tickets, Timer, Upload, VideoPlay, Warning, WarningFilled
} from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'

const app = createApp(App)

const icons = {
  ArrowDown, Check, Close, Collection, CollectionTag,
  DataAnalysis, Delete, Document, DocumentDelete, Download,
  EditPen, Expand, Flag, Fold, FolderOpened,
  HomeFilled, Link, List, Plus, Refresh,
  Right, Search, Setting, Sort, Star,
  StarFilled, Tickets, Timer, Upload, VideoPlay, Warning, WarningFilled
}

for (const [key, component] of Object.entries(icons)) {
  app.component(key, component)
}

app.use(ElementPlus, { locale: zhCn })
app.use(router)
app.mount('#app')
