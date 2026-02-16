<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'
import { Reading, House, Grid, Fold, Expand, Setting, ChatLineSquare, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const isLoginPage = computed(() => {
  return route.path === '/login' || route.name === 'login'
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '退出确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    userStore.logoutAction()
    router.push('/login')
  } catch {
    // cancelled
  }
}
</script>

<template>
  <div v-if="isLoginPage" class="app-wrapper">
    <RouterView />
  </div>

  <el-container v-else class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside-menu">
      <div class="logo">
        <h2 v-show="!isCollapse">云章·经籍</h2>
        <h2 v-show="isCollapse">云</h2>
      </div>
      <el-menu
          router
          :default-active="$route.path"
          class="el-menu-vertical"
          :collapse="isCollapse"
          :collapse-transition="false"
          unique-opened
      >
        <el-menu-item index="/">
          <el-icon><House /></el-icon>
          <template #title>总览</template>
        </el-menu-item>

        <el-sub-menu index="content">
          <template #title>
            <el-icon><Grid /></el-icon>
            <span>典籍修撰</span>
          </template>
          <el-menu-item index="/books">书目编目</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="knowledge">
          <template #title>
            <el-icon><Reading /></el-icon>
            <span>文渊阁</span>
          </template>
          <el-menu-item index="/knowledge/ingest">入库校验</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="chat">
          <template #title>
            <el-icon><ChatLineSquare /></el-icon>
            <span>清谈雅集</span>
          </template>
          <el-menu-item index="/chat/sessions">会话管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
          <span class="page-title">{{ $route.meta.title || '承道' }}</span>
        </div>
        <div class="header-right">
          <div class="user-info">
            <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <span class="username">掌书官</span>
          </div>
          <el-button type="danger" link class="logout-btn" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出
          </el-button>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <RouterView v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </RouterView>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
/* 宋风配色 */
:root {
  --color-ink: #2c2521;      /* 墨褐 */
  --color-paper: #fcf9f2;    /* 宣纸白 */
  --color-gold: #cfb078;     /* 泥金 */
  --color-border: #e6e0d5;   /* 浅灰褐 */
  --color-text-main: #2b2b2b;
  --color-text-sub: #5d5d5d;
}

.app-wrapper {
  width: 100%;
  height: 100vh;
  font-family: "Noto Serif SC", "Songti SC", serif;
}

.layout-container {
  height: 100vh;
}

.aside-menu {
  background-color: #2c2521; /* 墨色背景 */
  color: #d1c7b7;
  border-right: 1px solid #4a3f35;
  display: flex;
  flex-direction: column;
  transition: width 0.4s cubic-bezier(0.25, 1, 0.5, 1);
  overflow-x: hidden;
  box-shadow: 2px 0 8px rgba(0,0,0,0.15);
  z-index: 10;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #1e1916; /* 深墨 */
  border-bottom: 1px solid #4a3f35;
  overflow: hidden;
}

.logo h2 {
  margin: 0;
  color: #cfb078; /* 泥金 */
  font-family: "KaiTi", "STKaiti", serif; /* 楷体 */
  font-size: 20px;
  font-weight: bold;
  letter-spacing: 2px;
  white-space: nowrap;
  text-shadow: 0 1px 2px rgba(0,0,0,0.5);
}

.el-menu-vertical {
  border-right: none;
  flex: 1;
  background-color: transparent;
}

/* 覆盖 Element Menu 默认色以适配暗色侧边栏 */
:deep(.el-menu) {
  background-color: transparent !important;
  border-right: none;
}
:deep(.el-menu-item), :deep(.el-sub-menu__title) {
  color: #a89f91 !important;
  font-family: "Noto Serif SC", serif;
}
:deep(.el-menu-item:hover), :deep(.el-sub-menu__title:hover) {
  color: #f7f4ed !important;
  background-color: #3e3630 !important;
}
:deep(.el-menu-item.is-active) {
  color: #cfb078 !important; /* 泥金高亮 */
  background-color: #362e29 !important;
  border-right: 3px solid #cfb078;
  font-weight: 600;
}

.header {
  background-color: #fcf9f2; /* 极浅米黄 */
  border-bottom: 1px solid #e6e0d5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-family: "KaiTi", serif;
  font-size: 18px;
  color: #2b2b2b;
  font-weight: bold;
}

.collapse-btn {
  font-size: 22px;
  cursor: pointer;
  transition: 0.3s;
  color: #5d5d5d;
}

.collapse-btn:hover {
  color: #4a6c6f; /* 汝窑青 */
  transform: scale(1.1);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logout-btn {
  font-family: "Noto Serif SC", serif;
  font-size: 14px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: rgba(0,0,0,0.03);
}

.username {
  font-size: 14px;
  color: #606266;
  font-family: "Noto Serif SC", serif;
}

.main-content {
  background-color: #f0f2f5; /* 保持浅灰底色适配内容区 */
  padding: 24px;
  overflow-y: auto;
  background-image: url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0IiBoZWlnaHQ9IjQiPgo8cmVjdCB3aWR0aD0iNCIgaGVpZ2h0PSI0IiBmaWxsPSIjZjdmNGVkIi8+CjxwYXRoIGQ9Ik0wIDBMNCA0Wk00IDBMMCA0WiIgc3Ryb2tlPSIjZjBlYmU1IiBzdHJva2Utd2lkdGg9IjAuNSIvPgo8L3N2Zz4='); /* 复用微弱纹理 */
}

/* fade-transform transition */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.5s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
