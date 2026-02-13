<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-header">
        <div class="logo-container">
          <el-icon class="logo-icon" :size="48" color="#cfb078"><Reading /></el-icon>
          <span class="logo-text">云章·经籍</span>
        </div>
        <h2 class="welcome-text">承道以文 · 鉴古知今</h2>
      </div>
      
      <el-card class="login-card" shadow="never">
        <el-form 
          ref="loginFormRef" 
          :model="loginForm" 
          :rules="loginRules" 
          label-width="0"
          size="large"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="掌书官账号" 
              prefix-icon="User" 
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="通行口令"
              prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              :loading="loading" 
              class="login-button" 
              @click="handleLogin"
              auto-insert-space
            >
              入 阁
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <div class="login-footer">
        <span>乙巳年 · 云章书院制</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock, Reading } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = reactive<FormRules>({
  username: [{ required: true, message: '请输入掌书官账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入通行口令', trigger: 'blur' }]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.loginAction(loginForm)
        if (success) {
          ElMessage.success('入阁成功')
          router.push('/')
        }
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  /* 墨绿渐变底色 */
  background-color: #2c3e50; 
  background-image: 
    linear-gradient(rgba(44, 62, 80, 0.9), rgba(44, 62, 80, 0.95)),
    url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI2MCIgaGVpZ2h0PSI2MCI+CjxnIHN0cm9rZT0iIzU1NSIgc3Ryb2tlLXdpZHRoPSIxIiBmaWxsPSJub25lIiBvcGFjaXR5PSIwLjEiPgo8cGF0aCBkPSJNMzAgMEwzMCA2ME0wIDMwTDYwIDMwIi8+CjwvZz4KPC9zdmc+');
  position: relative;
  overflow: hidden;
}

.login-content {
  width: 100%;
  max-width: 440px;
  padding: 40px;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 16px;
}

.logo-text {
  font-family: "KaiTi", "STKaiti", serif;
  font-size: 32px;
  font-weight: bold;
  color: #cfb078; /* 泥金 */
  letter-spacing: 4px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.5);
}

.welcome-text {
  font-family: "Noto Serif SC", serif;
  font-size: 18px;
  color: #a89f91;
  font-weight: normal;
  margin: 0;
  letter-spacing: 2px;
  opacity: 0.8;
}

.login-card {
  border-radius: 4px;
  border: 1px solid #4a3f35;
  background: rgba(44, 37, 33, 0.85); /* 深墨半透明 */
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  padding: 20px 10px;
}

/* 输入框定制 */
:deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.05);
  box-shadow: 0 0 0 1px #5d5d5d inset;
  transition: all 0.3s;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #cfb078 inset !important; /* 聚焦泥金色 */
  background-color: rgba(255, 255, 255, 0.1);
}

:deep(.el-input__inner) {
  color: #f7f4ed;
  font-family: "Noto Serif SC", serif;
}

.el-input {
  --el-input-height: 48px;
}

/* 按钮定制 */
.login-button {
  width: 100%;
  height: 48px;
  font-size: 18px;
  font-family: "KaiTi", serif;
  font-weight: bold;
  border-radius: 4px;
  margin-top: 10px;
  background-color: #cfb078; /* 泥金按钮 */
  border-color: #cfb078;
  color: #2c2521;
  transition: all 0.3s;
}

.login-button:hover {
  background-color: #e0c28b;
  border-color: #e0c28b;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(207, 176, 120, 0.3);
}

.login-button:active {
  transform: translateY(0);
}

.login-footer {
  margin-top: 40px;
  text-align: center;
  color: #5d5d5d;
  font-size: 13px;
  font-family: "Noto Serif SC", serif;
  letter-spacing: 1px;
}

/* 装饰性背景纹理 - 类似云海或墨韵 */
.login-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(74, 108, 111, 0.15) 0%, transparent 60%); /* 汝窑青光晕 */
  animation: bg-breathe 20s infinite alternate;
  pointer-events: none;
}

@keyframes bg-breathe {
  0% { transform: scale(1); opacity: 0.5; }
  100% { transform: scale(1.1); opacity: 0.8; }
}
</style>
