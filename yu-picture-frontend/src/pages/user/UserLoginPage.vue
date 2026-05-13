<template>
  <div class="auth-page">
    <div class="auth-aurora" aria-hidden="true">
      <div class="blob blob-a" />
      <div class="blob blob-b" />
      <div class="blob blob-c" />
      <div class="auth-grid" />
    </div>
    <div class="auth-card yx-glass-panel">
      <h2 class="title">秒寻图仓 · 登录</h2>
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
        <div class="tips">
          没有账号？
          <RouterLink to="/user/register">去注册</RouterLink>
        </div>
        <a-form-item>
          <a-button type="primary" html-type="submit" size="large" block>登录</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { userLoginUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const loginUserStore = useLoginUserStore()

const handleSubmit = async (values: any) => {
  const res = await userLoginUsingPost(values)
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.auth-page {
  position: relative;
  min-height: calc(100vh - 88px);
  margin: -24px -28px;
  padding: clamp(32px, 8vh, 72px) 24px;
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
  width: 420px;
  height: 420px;
  background: radial-gradient(circle, #c7d2fe 0%, #a5f3fc 55%, transparent 70%);
  top: -12%;
  left: -8%;
}

.blob-b {
  width: 380px;
  height: 380px;
  background: radial-gradient(circle, #e9d5ff 0%, #fbcfe8 50%, transparent 70%);
  bottom: -10%;
  right: -5%;
  animation-delay: -4s;
}

.blob-c {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #99f6e4 0%, #bfdbfe 60%, transparent 72%);
  top: 40%;
  left: 35%;
  opacity: 0.45;
  animation-delay: -7s;
}

@keyframes floaty {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(24px, -16px) scale(1.04);
  }
  66% {
    transform: translate(-18px, 12px) scale(0.98);
  }
}

.auth-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(99, 102, 241, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99, 102, 241, 0.06) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: radial-gradient(ellipse 70% 60% at 50% 45%, black 20%, transparent 100%);
}

.auth-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  padding: 2.25rem 2rem 2rem;
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
  margin: 0 0 1.75rem;
  font-size: 0.9rem;
  letter-spacing: 0.06em;
  color: var(--yx-text-muted, #94a3b8);
}

.tips {
  color: var(--yx-text-muted, #94a3b8);
  text-align: right;
  font-size: 0.8125rem;
  margin-bottom: 1rem;
  letter-spacing: 0.02em;
}

.tips a {
  font-weight: 600;
  color: var(--yx-primary, #6366f1);
}
</style>
