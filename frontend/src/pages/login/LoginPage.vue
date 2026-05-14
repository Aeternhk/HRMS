<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import { useUserStore } from '@/store'
import * as authApi from '@/api/modules/auth'
import type { FormInstanceFunctions } from 'tdesign-vue-next'

// SVG 图标组件
const UserIcon = () => h('svg', { viewBox: '0 0 24 24', fill: 'currentColor', width: '1em', height: '1em' }, [
  h('path', { d: 'M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z' })
])
const LockIcon = () => h('svg', { viewBox: '0 0 24 24', fill: 'currentColor', width: '1em', height: '1em' }, [
  h('path', { d: 'M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z' })
])
const CodeIcon = () => h('svg', { viewBox: '0 0 24 24', fill: 'currentColor', width: '1em', height: '1em' }, [
  h('path', { d: 'M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z' })
])

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstanceFunctions>()
const loading = ref(false)

const formData = ref({
  username: '',
  password: '',
  verifyCode: '',
  rememberMe: false
})

const captchaId = ref('')
const captchaImage = ref('')

// 获取验证码
const loadCaptcha = async () => {
  try {
    const res = await authApi.getCaptcha()
    captchaId.value = res.captchaId
    captchaImage.value = res.captchaImage
  } catch (e) {
    // 降级：使用简单验证码
    captchaImage.value = ''
  }
}

// 表单验证规则
const rules = {
  username: [{ required: true, message: '请输入用户名', type: 'error' }],
  password: [
    { required: true, message: '请输入密码', type: 'error' },
    { min: 6, message: '密码至少6个字符', type: 'warning' }
  ],
  verifyCode: [{ required: true, message: '请输入验证码', type: 'error' }]
}

// 刷新验证码
const refreshVerifyCode = () => {
  loadCaptcha()
}

// 登录处理
const handleLogin = async () => {
  try {
    await formRef.value?.validate()
  } catch (e) {
    return
  }

  loading.value = true

  try {
    await userStore.login(
      formData.value.username,
      formData.value.password,
      formData.value.verifyCode
    )
    
    MessagePlugin.success('登录成功，欢迎回来！')
    
    // 跳转到首页或之前的页面
    const redirect = router.currentRoute.value.query.redirect as string
    router.push(redirect || '/dashboard')
  } catch (error: any) {
    MessagePlugin.error(error.message || '登录失败，请检查账号密码')
    refreshVerifyCode()
  } finally {
    loading.value = false
  }
}

// 刷新验证码（错误时）
const refreshVerifycha = () => {
  formData.value.verifyCode = ''
  loadCaptcha()
}

// 主题切换
const isDarkMode = ref(false)
const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
  document.documentElement.setAttribute('theme', isDarkMode.value ? 'dark' : 'light')
}

// 回车登录
const handleKeyEnter = () => {
  handleLogin()
}

onMounted(() => {
  // 如果已登录，直接跳转
  if (userStore.isLoggedIn) {
    router.push('/dashboard')
    return
  }
  loadCaptcha()
})
</script>

<template>
  <div :class="['login-page', { 'dark-mode': isDarkMode }]">
    <!-- 左侧品牌区域 -->
    <div class="login-left">
      <div class="brand-content">
        <div class="logo-wrapper">
          <svg viewBox="0 0 48 48" class="logo-icon">
            <circle cx="24" cy="24" r="22" fill="currentColor" opacity="0.1"/>
            <path d="M24 4C12.95 4 4 12.95 4 24s8.95 20 20 20 20-8.95 20-20S35.05 4 24 4zm0 36c-8.82 0-16-7.18-16-16S15.18 8 24 8s16 7.18 16 16-7.18 16-16 16z" fill="currentColor"/>
            <path d="M24 14c-5.52 0-10 4.48-10 10s4.48 10 10 10 10-4.48 10-10-4.48-10-10-10zm0 16c-3.31 0-6-2.69-6-6s2.69-6 6-6 6 2.69 6 6-2.69 6-6 6z" fill="currentColor"/>
          </svg>
        </div>
        <h1 class="brand-title">HRMS</h1>
        <p class="brand-subtitle">人力资源管理系统</p>
        <div class="brand-features">
          <div class="feature-item">
            <svg viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
            </svg>
            <span>员工信息管理</span>
          </div>
          <div class="feature-item">
            <svg viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
              <path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/>
            </svg>
            <span>智能考勤统计</span>
          </div>
          <div class="feature-item">
            <svg viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
              <path d="M3.5 18.49l6-6.01 4 4L22 6.92l-1.41-1.41-7.09 7.97-4-4L2 16.99z"/>
            </svg>
            <span>数据可视化分析</span>
          </div>
          <div class="feature-item">
            <svg viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
              <path d="M18 13h-5v5c0 .55-.45 1-1 1s-1-.45-1-1v-5H6c-.55 0-1-.45-1-1s.45-1 1-1h5V6c0-.55.45-1 1-1s1 .45 1 1v5h5c.55 0 1 .45 1 1s-.45 1-1 1z"/>
            </svg>
            <span>在线投票评选</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单区域 -->
    <div class="login-right">
      <div class="login-form-wrapper">
        <!-- 主题切换按钮 -->
        <button class="theme-toggle" @click="toggleTheme" :title="isDarkMode ? '切换亮色主题' : '切换暗色主题'">
          <svg v-if="isDarkMode" viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
            <path d="M6.76 4.84l-1.8-1.79-1.41 1.41 1.79 1.79 1.42-1.41zM4 10.5H1v2h3v-2zm9-9.95h-2V3.5h2V.55zm7.45 3.91l-1.41-1.41-1.79 1.79 1.41 1.41 1.79-1.79zm-3.21 13.7l1.79 1.8 1.41-1.41-1.8-1.79-1.4 1.4zM20 10.5v2h3v-2h-3zm-8-5c-3.31 0-6 2.69-6 6s2.69 6 6 6 6-2.69 6-6-2.69-6-6-6zm-1 16.95h2V19.5h-2v2.95zm-7.45-3.91l1.41 1.41 1.79-1.8-1.41-1.41-1.79 1.8z"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
            <path d="M9.37 5.51c-.18.64-.27 1.31-.27 1.99 0 4.08 3.32 7.4 7.4 7.4.68 0 1.35-.09 1.99-.27C17.45 17.19 14.93 19 12 19c-3.86 0-7-3.14-7-7 0-2.93 1.81-5.45 4.37-6.49zM12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9c0-.46-.04-.92-.1-1.36-.98 1.37-2.58 2.26-4.4 2.26-2.98 0-5.4-2.42-5.4-5.4 0-1.81.89-3.42 2.26-4.4-.44-.06-.9-.1-1.36-.1z"/>
          </svg>
        </button>

        <div class="form-header">
          <h2 class="form-title">欢迎登录</h2>
          <p class="form-desc">请输入您的账号信息</p>
        </div>

        <t-form
          ref="formRef"
          :data="formData"
          :rules="rules"
          labelWidth="0"
          class="login-form"
          @submit="handleLogin"
        >
          <!-- 用户名 -->
          <t-form-item name="username">
            <t-input
              v-model="formData.username"
              placeholder="工号 / 手机号"
              size="large"
              clearable
              autocomplete="username"
            >
              <template #prefix-icon>
                <UserIcon />
              </template>
            </t-input>
          </t-form-item>

          <!-- 密码 -->
          <t-form-item name="password">
            <t-input
              v-model="formData.password"
              type="password"
              placeholder="密码"
              size="large"
              clearable
              autocomplete="current-password"
              @enter-key="handleKeyEnter"
            >
              <template #prefix-icon>
                <LockIcon />
              </template>
            </t-input>
          </t-form-item>

          <!-- 验证码 -->
          <t-form-item name="verifyCode">
            <div class="verify-code-wrapper">
              <t-input
                v-model="formData.verifyCode"
                placeholder="验证码"
                size="large"
                :style="{ flex: 1 }"
                maxlength="4"
                @enter-key="handleKeyEnter"
              >
                <template #prefix-icon>
                  <CodeIcon />
                </template>
              </t-input>
              <div class="verify-code-img" @click="refreshVerifyCode" title="点击刷新验证码">
                <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
                <span v-else class="captcha-placeholder">验证码</span>
              </div>
            </div>
          </t-form-item>

          <!-- 记住登录 -->
          <div class="form-options">
            <t-checkbox v-model="formData.rememberMe">记住登录状态</t-checkbox>
            <a class="forgot-link" @click="MessagePlugin.info('请联系管理员重置密码')">忘记密码？</a>
          </div>

          <!-- 登录按钮 -->
          <t-form-item>
            <t-button
              type="submit"
              theme="primary"
              size="large"
              block
              :loading="loading"
            >
              登 录
            </t-button>
          </t-form-item>
        </t-form>

        <div class="form-footer">
          <span>还没有账号？</span>
          <a class="register-link" @click="MessagePlugin.info('请联系管理员开通账号')">联系管理员开通</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  background: #ffffff;
}

/* 左侧品牌区域 */
.login-left {
  flex: 0 0 45%;
  background: linear-gradient(135deg, #0052d9 0%, #4364f7 50%, #6fb1fc 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;
}

.login-left::before {
  content: '';
  position: absolute;
  width: 500px;
  height: 500px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  top: -200px;
  right: -200px;
}

.login-left::after {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
  bottom: -100px;
  left: -100px;
}

.brand-content {
  position: relative;
  z-index: 1;
  color: #ffffff;
  text-align: center;
}

.logo-wrapper {
  width: 100px;
  height: 100px;
  margin: 0 auto 24px;
  color: #ffffff;
}

.logo-icon {
  width: 100%;
  height: 100%;
}

.brand-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 8px;
  letter-spacing: 4px;
}

.brand-subtitle {
  font-size: 18px;
  margin: 0 0 48px;
  opacity: 0.9;
}

.brand-features {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  text-align: left;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  backdrop-filter: blur(10px);
  transition: transform 0.2s ease, background 0.2s ease;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.feature-item span {
  font-size: 14px;
}

/* 右侧表单区域 */
.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
}

.login-form-wrapper {
  width: 100%;
  max-width: 400px;
  position: relative;
}

.theme-toggle {
  position: absolute;
  top: -60px;
  right: 0;
  width: 40px;
  height: 40px;
  border: none;
  background: #f5f7fa;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease;
}

.theme-toggle:hover {
  background: #e7eaf0;
}

.form-header {
  margin-bottom: 32px;
}

.form-title {
  font-size: 28px;
  font-weight: 600;
  color: #000000;
  margin: 0 0 8px;
}

.form-desc {
  font-size: 14px;
  color: #666666;
  margin: 0;
}

.login-form {
  margin-bottom: 24px;
}

.verify-code-wrapper {
  display: flex;
  gap: 12px;
  width: 100%;
}

.verify-code-img {
  width: 120px;
  height: 40px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  background: #f5f7fa;
  border: 1px solid #dcdcdc;
  display: flex;
  align-items: center;
  justify-content: center;
}

.verify-code-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-placeholder {
  font-size: 12px;
  color: #999;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.forgot-link {
  color: #0052d9;
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;
}

.forgot-link:hover {
  text-decoration: underline;
}

.form-footer {
  text-align: center;
  font-size: 14px;
  color: #666666;
}

.register-link {
  color: #0052d9;
  text-decoration: none;
  cursor: pointer;
  margin-left: 4px;
}

.register-link:hover {
  text-decoration: underline;
}

/* 暗色模式 */
.dark-mode .login-left {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

.dark-mode .form-title {
  color: #ffffff;
}

.dark-mode .form-desc {
  color: #999999;
}

.dark-mode .theme-toggle {
  background: #2d2d2d;
  color: #ffffff;
}

.dark-mode .theme-toggle:hover {
  background: #3d3d3d;
}

/* 响应式布局 */
@media (max-width: 992px) {
  .login-page {
    flex-direction: column;
  }

  .login-left {
    flex: none;
    padding: 40px;
  }

  .brand-title {
    font-size: 36px;
  }

  .brand-features {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .login-right {
    padding: 40px 20px;
  }
}

@media (max-width: 576px) {
  .login-left {
    padding: 24px;
  }

  .brand-title {
    font-size: 28px;
  }

  .brand-subtitle {
    font-size: 14px;
    margin-bottom: 24px;
  }

  .feature-item {
    padding: 12px;
    font-size: 12px;
  }

  .login-right {
    padding: 20px 16px;
  }

  .form-title {
    font-size: 24px;
  }
}
</style>
