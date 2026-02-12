<script setup lang="ts">
import { RouterView } from 'vue-router'
import { Reading, House, Grid } from '@element-plus/icons-vue'
</script>

<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside-menu">
      <div class="logo">
        <h2>Literature Admin</h2>
      </div>
      <el-menu
          router
          :default-active="$route.path"
          class="el-menu-vertical"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
      >
        <el-menu-item index="/">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <el-sub-menu index="content">
          <template #title>
            <el-icon><Grid /></el-icon>
            <span>内容管理</span>
          </template>
          <el-menu-item index="/books">书籍列表</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="knowledge">
          <template #title>
            <el-icon><Reading /></el-icon>
            <span>知识库管理</span>
          </template>
          <el-menu-item index="/knowledge/ingest">书籍入库</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <span>Admin</span>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside-menu {
  background-color: #304156;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3649;
}

.logo h2 {
  margin: 0;
  color: #fff;
  font-size: 18px;
}

.el-menu-vertical {
  border-right: none;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
