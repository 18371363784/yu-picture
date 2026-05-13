<template>
  <div class="auth-page">
    <div class="auth-aurora" aria-hidden="true">
      <div class="blob blob-a" />
      <div class="blob blob-b" />
      <div class="blob blob-c" />
      <div class="auth-grid" />
    </div>
    <div class="auth-card yx-glass-panel">
      <h2 class="title">秒寻图仓 · 注册</h2>
      <p class="desc">企业级智能协同云图库</p>
      <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
        <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" size="large" />
        </a-form-item>
        <a-form-item
          name="userPassword"
          :rules="[
            { required: true, message: '请输入密码' },
            { min: 8, message: '密码长度不能小于 8 位' },
          ]"
        >
          <a-input-password
            v-model:value="formState.userPassword"
            placeholder="请输入密码"
            size="large"
          />
        </a-form-item>
        <a-form-item
          name="checkPassword"
          :rules="[
            { required: true, message: '请输入确认密码' },
            { min: 8, message: '确认密码长度不能小于 8 位' },
          ]"
        >
          <a-input-password
            v-model:value="formState.checkPassword"
            placeholder="请输入确认密码"
            size="large"
          />
        </a-form-item>
        <div class="tips">
          已有账号？
          <RouterLink to="/user/login">去登录</RouterLink>
        </div>
        <a-form-item>
          <a-button type="primary" html-type="submit" size="large" block>注册</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { userRegisterUsingPost } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import router from '@/router'

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const handleSubmit = async (values: any) => {
  if (values.userPassword !== values.checkPassword) {
    message.error('两次输入的密码不一致')
    return
  }
  const res = await userRegisterUsingPost(values)
  if (res.data.code === 0 && res.data.data) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.data.message)
  }
}
</script>

<style scoped>
.auth-page {
  position: relative;
  min-height: calc(100vh - 88px);
  margin: -24px -28px;
  padding: clamp(28px, 6vh, 64px) 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.auth-aurora {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(64px);
  opacity: 0.65;
  animation: floaty 14s ease-in-out infinite;
}

.blob-a {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #ddd6fe 0%, #bae6fd 55%, transparent 70%);
  top: -8%;
  right: -6%;
}

.blob-b {
  width: 360px;
  height: 360px;
  background: radial-gradient(circle, #a5f3fc 0%, #c7d2fe 50%, transparent 70%);
  bottom: -12%;
  left: -8%;
  animation-delay: -5s;
}

.blob-c {
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, #fbcfe8 0%, #e9d5ff 60%, transparent 72%);
  top: 38%;
  left: 28%;
  opacity: 0.5;
  animation-delay: -8s;
}

@keyframes floaty {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(-20px, 18px) scale(1.03);
  }
}

.auth-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(14, 165, 233, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(14, 165, 233, 0.05) 1px, transparent 1px);
  background-size: 44px 44px;
  mask-image: radial-gradient(ellipse 75% 55% at 50% 48%, black 15%, transparent 100%);
}

.auth-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  padding: 2rem 2rem 1.75rem;
}

.title {
  text-align: center;
  margin: 0 0 0.35rem;
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--yx-text-title, #0f172a);
}

.desc {
  text-align: center;
  margin: 0 0 1.5rem;
  font-size: 0.9rem;
  letter-spacing: 0.06em;
  color: var(--yx-text-muted, #94a3b8);
}

.tips {
  color: var(--yx-text-muted, #94a3b8);
  text-align: right;
  font-size: 0.8125rem;
  margin-bottom: 1rem;
}

.tips a {
  font-weight: 600;
  color: var(--yx-primary, #6366f1);
}
</style>
