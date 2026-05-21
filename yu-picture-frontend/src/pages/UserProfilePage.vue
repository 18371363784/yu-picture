<template>
  <div id="userProfilePage" class="yx-page-shell">
    <h2 class="yx-page-title">个人主页</h2>
    <p class="yx-page-sub">管理你的个人信息与账户资料</p>
    <div class="yx-divider-dot"><span /><span /></div>

    <a-row :gutter="[24, 24]">
      <!-- 左侧：头像 + 统计卡片 -->
      <a-col :xs="24" :md="8">
        <div class="profile-sidebar">
          <!-- 头像区域 -->
          <div class="avatar-section">
            <a-upload
              :show-upload-list="false"
              :before-upload="beforeAvatarUpload"
              :custom-request="handleAvatarUpload"
              accept="image/jpeg,image/png,image/webp"
            >
              <div class="avatar-wrapper">
                <a-avatar :size="120" :src="profile.userAvatar" />
                <div class="avatar-overlay">
                  <CameraOutlined />
                  <span>更换头像</span>
                </div>
              </div>
            </a-upload>
            <h3 class="user-name">{{ profile.userName || '未设置昵称' }}</h3>
            <a-tag :color="roleColor">{{ roleText }}</a-tag>
          </div>

          <!-- 统计卡片 -->
          <div class="stats-grid">
            <div class="yx-stat-card">
              <div class="yx-stat-icon amber">
                <ClockCircleOutlined />
              </div>
              <div>
                <div class="yx-stat-value">{{ daysSinceRegister }}</div>
                <div class="yx-stat-label">注册天数</div>
              </div>
            </div>
            <div class="yx-stat-card">
              <div class="yx-stat-icon green">
                <CrownOutlined />
              </div>
              <div>
                <div class="yx-stat-value">{{ profile.vipNumber || '-' }}</div>
                <div class="yx-stat-label">会员编号</div>
              </div>
            </div>
            <div class="yx-stat-card" v-if="vipExpireText">
              <div class="yx-stat-icon" :class="isVipActive ? 'green' : 'red'">
                <SafetyOutlined />
              </div>
              <div>
                <div class="yx-stat-value">{{ vipExpireText }}</div>
                <div class="yx-stat-label">会员到期</div>
              </div>
            </div>
          </div>
        </div>
      </a-col>

      <!-- 右侧：表单 -->
      <a-col :xs="24" :md="16">
        <a-card title="基本信息" :bordered="false" class="profile-card">
          <template #extra>
            <a-button
              type="primary"
              :loading="saving"
              :icon="h(SaveOutlined)"
              @click="handleSave"
            >
              保存
            </a-button>
          </template>

          <a-form layout="vertical" :model="form" ref="formRef">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="用户账号">
                  <a-input :value="profile.userAccount" disabled />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="用户角色">
                  <a-tag :color="roleColor">{{ roleText }}</a-tag>
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="昵称" name="userName">
                  <a-input
                    v-model:value="form.userName"
                    placeholder="请输入昵称"
                    :maxlength="20"
                    show-count
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="性别" name="gender">
                  <a-select v-model:value="form.gender" placeholder="请选择性别">
                    <a-select-option :value="0">未知</a-select-option>
                    <a-select-option :value="1">
                      <ManOutlined /> 男
                    </a-select-option>
                    <a-select-option :value="2">
                      <WomanOutlined /> 女
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="手机号" name="phone">
                  <a-input
                    v-model:value="form.phone"
                    placeholder="请输入手机号"
                    :maxlength="11"
                  >
                    <template #prefix><PhoneOutlined /></template>
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="邮箱" name="email">
                  <a-input
                    v-model:value="form.email"
                    placeholder="请输入邮箱"
                  >
                    <template #prefix><MailOutlined /></template>
                  </a-input>
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="生日" name="birthday">
                  <a-date-picker
                    v-model:value="form.birthday"
                    style="width: 100%"
                    placeholder="请选择生日"
                    value-format="YYYY-MM-DD"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="注册时间">
                  <a-input :value="profile.createTime" disabled />
                </a-form-item>
              </a-col>
            </a-row>

            <a-form-item label="个人简介" name="userProfile">
              <a-textarea
                v-model:value="form.userProfile"
                placeholder="介绍一下自己吧..."
                :rows="4"
                :maxlength="200"
                show-count
              />
            </a-form-item>
          </a-form>
        </a-card>

        <!-- 安全设置卡片 -->
        <a-card title="安全设置" :bordered="false" class="profile-card" style="margin-top: 16px">
          <a-list :split="false">
            <a-list-item>
              <a-list-item-meta
                title="账户密码"
                description="已设置，定期更换密码可保护账户安全"
              />
              <template #extra>
                <a-tag color="green">已设置</a-tag>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta
                title="手机验证"
                :description="profile.phone ? `已绑定：${profile.phone}` : '未绑定手机号'"
              />
              <template #extra>
                <a-tag :color="profile.phone ? 'green' : 'default'">
                  {{ profile.phone ? '已绑定' : '未绑定' }}
                </a-tag>
              </template>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta
                title="邮箱验证"
                :description="profile.email ? `已绑定：${profile.email}` : '未绑定邮箱'"
              />
              <template #extra>
                <a-tag :color="profile.email ? 'green' : 'default'">
                  {{ profile.email ? '已绑定' : '未绑定' }}
                </a-tag>
              </template>
            </a-list-item>
          </a-list>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue'
import {
  getMyProfileUsingGet,
  updateMyProfileUsingPost,
  uploadAvatarUsingPost,
} from '@/api/userController'
import { message } from 'ant-design-vue'
import {
  CameraOutlined,
  ClockCircleOutlined,
  CrownOutlined,
  MailOutlined,
  ManOutlined,
  PhoneOutlined,
  SafetyOutlined,
  SaveOutlined,
  WomanOutlined,
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { toAbsolutePictureUrl } from '@/utils'

const profile = ref<API.UserVO>({})
const saving = ref(false)
const form = reactive<API.UpdateMyProfileRequest>({})

const roleColor = computed(() => {
  switch (profile.value.userRole) {
    case 'admin': return 'red'
    case 'vip': return 'gold'
    default: return 'blue'
  }
})

const roleText = computed(() => {
  switch (profile.value.userRole) {
    case 'admin': return '管理员'
    case 'vip': return 'VIP会员'
    default: return '普通用户'
  }
})

const daysSinceRegister = computed(() => {
  if (!profile.value.createTime) return '-'
  return dayjs().diff(dayjs(profile.value.createTime), 'day')
})

const vipExpireText = computed(() => {
  if (!profile.value.vipExpireTime) return ''
  return dayjs(profile.value.vipExpireTime).format('YYYY-MM-DD')
})

const isVipActive = computed(() => {
  if (!profile.value.vipExpireTime) return false
  return dayjs(profile.value.vipExpireTime).isAfter(dayjs())
})

const fetchProfile = async () => {
  try {
    const res = await getMyProfileUsingGet()
    if (res.data.code === 0 && res.data.data) {
      profile.value = res.data.data
      form.userName = res.data.data.userName
      form.userProfile = res.data.data.userProfile
      form.gender = res.data.data.gender
      form.phone = res.data.data.phone
      form.email = res.data.data.email
      form.birthday = res.data.data.birthday
    }
  } catch (e: any) {
    message.error('获取个人信息失败')
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const res = await updateMyProfileUsingPost(form)
    if (res.data.code === 0) {
      message.success('保存成功')
      await fetchProfile()
    } else {
      message.error(res.data.message || '保存失败')
    }
  } catch (e: any) {
    message.error('保存失败')
  }
  saving.value = false
}

const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const handleAvatarUpload = async (options: any) => {
  try {
    const res = await uploadAvatarUsingPost(options.file)
    if (res.data.code === 0 && res.data.data) {
      profile.value.userAvatar = res.data.data
      message.success('头像更换成功')
    } else {
      message.error(res.data.message || '上传失败')
    }
  } catch (e: any) {
    message.error('头像上传失败')
  }
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
#userProfilePage {
  max-width: 1000px;
  margin: 0 auto;
}

.profile-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.avatar-section {
  text-align: center;
  padding: 28px 20px 20px;
  border-radius: var(--yx-radius-lg);
  background: rgba(255,255,255,0.5);
  border: 1px solid rgba(255,255,255,0.7);
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-wrapper :deep(.ant-avatar) {
  transition: filter 0.25s ease;
}

.avatar-wrapper:hover :deep(.ant-avatar) {
  filter: brightness(0.7);
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  background: rgba(0,0,0,0.45);
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.25s ease;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.user-name {
  margin: 12px 0 8px;
  font-size: 1.15rem;
  font-weight: 600;
  color: var(--yx-text-title);
}

.stats-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.profile-card {
  border-radius: var(--yx-radius-lg) !important;
  border: 1px solid rgba(255,255,255,0.7) !important;
}

.profile-card :deep(.ant-card-head) {
  border-bottom: 1px solid rgba(148,163,184,0.15);
}

.profile-card :deep(.ant-card-head-title) {
  font-weight: 600;
  color: var(--yx-text-title);
}
</style>
